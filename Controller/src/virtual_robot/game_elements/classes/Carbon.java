package virtual_robot.game_elements.classes;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.shape.Circle;
import org.dyn4j.collision.CategoryFilter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.MassType;
import virtual_robot.controller.Filters;
import virtual_robot.controller.*;
import virtual_robot.dyn4j.Dyn4jUtil;
import virtual_robot.dyn4j.FixtureData;
import virtual_robot.games.CarbonCapture;

import java.util.ArrayList;
import java.util.List;

@GameElementConfig(name = "Carbon", filename = "carbon", forGame = CarbonCapture.class, numInstances = 100)
public class Carbon extends VirtualGameElement {
    public static final List<Carbon> carbons = new ArrayList<>();  // All Carbon objects
    public static final List<Carbon> carbonsOffField = new ArrayList<>();

    @FXML
    private Circle carbon;

    public enum CarbonStatus {
        NORMAL(true, 100, CARBON_FILTER),
        FLYING(true, 0, CARBON_FLYING_FILTER),
        ROLLING(true, 0.6, CARBON_FILTER),
        CONTROLLED(false, 0, CARBON_FILTER),
        CAPTURED(true, 0.8, CARBON_CAPTURED_FILTER), //Only these are available to human player
        OFF_FIELD(false, 0.0, CARBON_FILTER);

        private final boolean inPlay;             // Is carbon controlled by dyn4j physics world?
        private final double linearDamping;       // effectively, the friction between floor and world
        private final CategoryFilter filter;      // Filter for collisions between carbon and other bodies

        CarbonStatus(boolean inPlay, double linearDamping, CategoryFilter filter){
            this.inPlay = inPlay;
            this.linearDamping = linearDamping;
            this.filter = filter;
        }

        public boolean isInPlay() { return inPlay; }
        public double getLinearDamping() { return linearDamping; }
        public CategoryFilter getFilter() { return filter; }
    }

    private CarbonStatus status = CarbonStatus.OFF_FIELD;
    private CarbonStatus nextStatus = CarbonStatus.OFF_FIELD;
    Body body;
    BodyFixture carbonFixture;

    public static long CARBON_CATEGORY = 2048;
    public static long CAPTURED_CARBON_CATEGORY = 4096;

    //Standard carbon filter: collide with everything that is allowed to collide with carbon except for the central Sink
    public static final CategoryFilter CARBON_FILTER = new CategoryFilter(CARBON_CATEGORY, Filters.MASK_ALL & ~Sink.SINK_CATEGORY);

    //Flying carbon filter: collide with NOTHING
    public static final CategoryFilter CARBON_FLYING_FILTER = new CategoryFilter(CARBON_CATEGORY, Sink.SINK_CATEGORY & ~CARBON_CATEGORY);
    public static final CategoryFilter CARBON_CAPTURED_FILTER = new CategoryFilter(CAPTURED_CARBON_CATEGORY, Filters.MASK_ALL & ~CARBON_CATEGORY);

    public void initialize(){
        super.initialize();
    }

    @Override
    public void setUpDisplayGroup(Group group) {
        super.setUpDisplayGroup(group);
    }

    @Override
    public synchronized void updateState(double millis) {

        /*
         * Obtain the location and heading of the carbon from the dyn4j body,
         * and convert to pixels for subsequent display.
         */
        x = body.getTransform().getTranslationX() * VirtualField.PIXELS_PER_METER;
        y = body.getTransform().getTranslationY() * VirtualField.PIXELS_PER_METER;
        headingRadians = body.getTransform().getRotationAngle();

        setStatus(nextStatus);
    }

    /*
     * No special display handling for Carbon. In fact, don't really need to override the parent method.
     */
    @Override
    public synchronized void updateDisplay() {
        super.updateDisplay();
    }

    /**
     * Instantiate elementBody, and set its parent to this carbon. Also save this in an alias: body.
     * Add a fixture with circular shape to the body. Initial status: OFF_FIELD.
     */
    @Override
    public void setUpBody(){
        /*
         * Use Dyn4jUtil.createBody to generate and configure a dyn4j Body, using the outerCircle from the .fxml
         * file. Note that we save a reference to the BodyFixture. this is needed so that the collision
         * filter can be changed as carbon status changes.
         */
        elementBody = Dyn4jUtil.createBody(carbon, this, 0, 0,
                new FixtureData(CARBON_FILTER, 0.5, 0.3, 0.05));
        elementBody.setMassType(MassType.NORMAL);
        elementBody.setAngularDamping(0.4);

//        elementBody = Dyn4jUtil.createBody(carbon, this, 0, 0, new FixtureData(CARBON_FILTER, 1.0, 0.0, 0));
        body = elementBody;     //Just an alias
        carbonFixture = body.getFixture(0);
        carbonFixture.setRestitutionVelocity(0.0);
        this.setStatus(CarbonStatus.OFF_FIELD);
    }

    /**
     * Set velocity of the Carbon, in inches per second.
     * @param vx x velocity in inches
     * @param vy y velocity in inches
     */
    public void setVelocityInchesPerSec(double vx, double vy) {
        this.body.setLinearVelocity(vx / VirtualField.INCHES_PER_METER, vy / VirtualField.INCHES_PER_METER);
    }

    /**
     * Set the velocity of the Carbon, in meters per second.
     * @param vx x velocity in meters
     * @param vy y velocity in meters
     */
    public void setVelocityMetersPerSec(double vx, double vy){
        this.body.setLinearVelocity(vx, vy);
    }

    public void setStatus(Carbon.CarbonStatus status){
        // Update both the status and nextStatus fields to match the status argument
        this.status = status;
        this.nextStatus = status;
        // Add/remove to/from the physics world and the display, depending on the value of inPlay for the new status.
        setOnField(status.isInPlay());
        // Update the carbonsOffField list if necessary (it keeps track of which carbons are available to human player)
        if (status == Carbon.CarbonStatus.OFF_FIELD && !carbonsOffField.contains(this)) {
            carbonsOffField.add(this);
            Compressor.capturedCarbons.remove(this);
        } else if (status != CarbonStatus.OFF_FIELD){
            carbonsOffField.remove(this);
        }

        // Set the linearDamping and the collision filter corresponding to the new status.
        body.setLinearDamping(status.getLinearDamping());
        carbonFixture.setFilter(status.getFilter());
    }
    public void setNextStatus(CarbonStatus nextStatus) { this.nextStatus = nextStatus; }

    public CarbonStatus getStatus() { return status; }

}

package virtual_robot.game_elements.classes;

import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import org.dyn4j.collision.CategoryFilter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.MassType;
import org.dyn4j.world.NarrowphaseCollisionData;
import org.dyn4j.world.listener.CollisionListenerAdapter;
import virtual_robot.controller.Filters;
import virtual_robot.controller.GameElementConfig;
import virtual_robot.controller.VirtualField;
import virtual_robot.controller.VirtualGameElement;
import virtual_robot.dyn4j.Dyn4jUtil;
import virtual_robot.dyn4j.FixtureData;
import virtual_robot.games.CarbonCapture;

import java.util.ArrayList;
import java.util.List;

@GameElementConfig(name = "Compressor", filename = "compressor", forGame = CarbonCapture.class, numInstances = 4)
public class Compressor extends VirtualGameElement {
    public Group sides;
    public Rectangle sensor;
    private Body compressorBody = null;
    private BodyFixture compressorSensor;
    public static List<Compressor> compressors = new ArrayList<>();
    public static List<Carbon> capturedCarbons = new ArrayList<>();
    private Carbon carbonToLoad;


    // Category and filter for collisions
    public static final long COMPRESSOR_CATEGORY = 1024;
    public static final CategoryFilter COMPRESSOR_FILTER = new CategoryFilter(COMPRESSOR_CATEGORY, Filters.MASK_ALL);

    @Override
    public void initialize(){
        super.initialize();
        world.addCollisionListener(new CollisionListenerAdapter<Body, BodyFixture>(){
            @Override
            public boolean collision(NarrowphaseCollisionData<Body, BodyFixture> collision) {
                return handleNarrowPhaseCollisions(collision);
            }
        });
    }

    @Override
    public void updateState(double millis) {
        x = compressorBody.getTransform().getTranslationX() * VirtualField.PIXELS_PER_METER;
        y = compressorBody.getTransform().getTranslationY() * VirtualField.PIXELS_PER_METER;
        headingRadians = compressorBody.getTransform().getRotationAngle();

        if(carbonToLoad != null)captureCarbon();

    }
    @Override
    public synchronized void updateDisplay() {
        super.updateDisplay();
    }

    @Override
    public void setUpBody() {
        elementBody = Dyn4jUtil.createBody(sides, this, 0, 0, new FixtureData(COMPRESSOR_FILTER, 1, 0, 0));
//      Creating the sensor Fixture that keeps track of the captured carbons
        compressorSensor = Dyn4jUtil.createFixture(sensor, 0, 0, true,
                new FixtureData(new CategoryFilter(Carbon.CARBON_CATEGORY, -1), 1, 0, 0));
        compressorSensor.setSensor(true);
        elementBody.addFixture(compressorSensor);
        compressorBody = elementBody;
        compressorBody.setMass(MassType.INFINITE);
    }

    /**
     * Listener method to handle Narrowphase collision. This method will look specifically for collisions that cause
     * the robot to control a previously un-controlled game element. This method is called DURING the world
     * update.
     * Note that when ring collision with the intake is detected, the ring body is not immediately removed from
     * the dyn4j world. During so DURING the world update is not recommended (per dyn4j docs), and does cause
     * problems. Instead, save a reference to the ring to be loaded, and handle AFTER the world update, within
     * the updateStateAndSensors method.
     *
     * @param collision the NarrowphaseCollisionData object
     * @return True to allow collision resolution to continue; False to terminate collision resolution.
     */
    private boolean handleNarrowPhaseCollisions(NarrowphaseCollisionData<Body, BodyFixture> collision){
        BodyFixture f1 = collision.getFixture1();
        BodyFixture f2 = collision.getFixture2();
        if(f1 == compressorSensor || f2 == compressorSensor){
            Body b = f1 == compressorSensor ? collision.getBody2() : collision.getBody1();
            if (b.getUserData() instanceof Carbon){
                carbonToLoad = (Carbon) b.getUserData();
                return false;
            }

        }
        return true;
    }

    private void captureCarbon(){
        if (carbonToLoad.getStatus() != Carbon.CarbonStatus.FLYING){
            if (!capturedCarbons.contains(carbonToLoad)) {
                carbonToLoad.setStatus(Carbon.CarbonStatus.CAPTURED);
                capturedCarbons.add(carbonToLoad);
            }
        }
        carbonToLoad = null;
    }
}
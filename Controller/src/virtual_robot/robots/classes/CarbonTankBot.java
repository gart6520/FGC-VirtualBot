package virtual_robot.robots.classes;

import com.qualcomm.robotcore.hardware.DcMotorExImpl;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.ServoImpl;
import com.qualcomm.robotcore.hardware.configuration.MotorType;
import com.qualcomm.robotcore.util.ElapsedTime;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import org.dyn4j.collision.CategoryFilter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.NarrowphaseCollisionData;
import org.dyn4j.world.listener.CollisionListenerAdapter;
import virtual_robot.controller.BotConfig;
import virtual_robot.controller.Filters;
import virtual_robot.controller.VirtualField;
import virtual_robot.dyn4j.Dyn4jUtil;
import virtual_robot.dyn4j.FixtureData;
import virtual_robot.dyn4j.Hinge;
import virtual_robot.game_elements.classes.*;

import java.util.ArrayList;
import java.util.List;

@BotConfig(name = "Carbon Tank Bot", filename = "carbon_tank_bot")
public class CarbonTankBot extends TwoWheelPhysicsBase{
    private static final int MAG_SIZE = 5;
    private static final int COOLDOWN = 250;

    private final List<Carbon> loadedCarbons = new ArrayList<>();     //List of loaded rings
    private Carbon carbonToLoad = null;                         //Ring pending loading, if any
    @FXML private Group loadedCarbonGroup;      // Group to represent loaded rings in the UI

    private BodyFixture intakeFixture = null;

    private ServoImpl leftServo = null;
    private ServoImpl rightServo = null;
    private DcMotorExImpl intakeMotor;
    private DcMotorExImpl shooterMotor;

    // The backServoArm object is instantiated during loading via a fx:id property.
//    @FXML Rectangle leftServoArm;
//    @FXML Rectangle rightServoArm;
    @FXML Rectangle kickerServoRect;
    @FXML Rectangle intake;

    // dyn4j Body for the arm
    Body leftArmBody;
    Body rightArmBody;

    // Hinge joint for the arm
    Hinge leftArmHinge;
    Hinge rightArmHinge;

    // Arm Angle in degrees
    double leftArmAngleDegrees = 0;
    double rightArmAngleDegrees = 0;
    private double kickerServoRectDegrees = 0;

    private CategoryFilter ARM_FILTER = new CategoryFilter(Filters.ARM,
            Filters.MASK_ALL & ~SinkBarrier.SINK_CATEGORY & ~Sink.SINK_CATEGORY
                    & ~Filters.CHASSIS & ~Filters.ARM);

    // States of the system that pushes loaded rings into the shooter mechanism
    enum KickerState {LOADING, LOADED, SHOOTING, SHOOT, SHOT_DONE}

    private ServoImpl kickerServo;
    ElapsedTime kickerTimer = new ElapsedTime();
    private CarbonTankBot.KickerState kickerState = KickerState.LOADED;

    public CarbonTankBot(){
        super();
    }

    public void initialize(){
        super.initialize();
        hardwareMap.setActive(true);
        leftServo = (ServoImpl)hardwareMap.servo.get("left_servo");
        rightServo = (ServoImpl)hardwareMap.servo.get("right_servo");
        kickerServo = (ServoImpl)hardwareMap.servo.get("kicker_servo");
        intakeMotor = (DcMotorExImpl) hardwareMap.dcMotor.get("intake_motor");
        shooterMotor = (DcMotorExImpl)hardwareMap.dcMotor.get("shooter_motor");
        hardwareMap.setActive(false);

        kickerServoRect.getTransforms().add(new Rotate(0, 62, 37.5));
//        leftServoArm.getTransforms().add(new Rotate(0, 37.5, 67.5));
//        rightServoArm.getTransforms().add(new Rotate(0, 65, 2.5));
//
//
//        leftArmBody = Dyn4jUtil.createBody(leftServoArm, this,9, 9,
//                new FixtureData(Filters.CHASSIS_FILTER, 1, 0, 0.25));
//        rightArmBody = Dyn4jUtil.createBody(rightServoArm, this, 9, 9,
//                new FixtureData(Filters.CHASSIS_FILTER, 1, 0, 0.25));
//
//        world.addBody(leftArmBody);
////        world.addBody(rightArmBody);
//
//        leftArmHinge = new Hinge(chassisBody, leftArmBody, new Vector2(0, -30), VirtualField.Unit.PIXEL);
//        rightArmHinge = new Hinge(chassisBody, rightArmBody, new Vector2(0, 30), VirtualField.Unit.PIXEL);
//
//        world.addJoint(leftArmHinge);
//        world.addJoint(rightArmHinge);

        intakeFixture = chassisBody.addFixture(new org.dyn4j.geometry.Rectangle(
                14 / VirtualField.INCHES_PER_METER, 4 / VirtualField.INCHES_PER_METER));
        intakeFixture.getShape().translate(0, -8.5 / VirtualField.INCHES_PER_METER);
//        intakeFixture = Dyn4jUtil.createFixture(intake, ((75.0-52.0/2))/ VirtualField.PIXELS_PER_INCH, (-63.0 /2 / VirtualField.PIXELS_PER_INCH), true, new FixtureData(1, 0, 0));
        intakeFixture.setSensor(true);
        chassisBody.addFixture(intakeFixture);

        world.addCollisionListener(new CollisionListenerAdapter<Body, BodyFixture>(){
            @Override
            public boolean collision(NarrowphaseCollisionData<Body, BodyFixture> collision) {
                return handleNarrowPhaseCollisions(collision);
            }
        });

        loadedCarbonGroup.setVisible(false);

    }

    protected void createHardwareMap(){
        super.createHardwareMap();
        hardwareMap.put("left_servo", new ServoImpl());
        hardwareMap.put("right_servo", new ServoImpl());
        hardwareMap.put("intake_motor", new DcMotorExImpl(MotorType.RevUltraPlanetaryOneToOne));
        hardwareMap.put("shooter_motor", new DcMotorExImpl(MotorType.RevUltraPlanetaryOneToOne));
        hardwareMap.put("kicker_servo", new ServoImpl());
    }

    public synchronized void updateStateAndSensors(double millis){
        super.updateStateAndSensors(millis);
//        leftArmAngleDegrees = -180.0 * leftServo.getInternalPosition();
//        rightArmAngleDegrees = 180.0 * rightServo.getInternalPosition();
//        leftArmHinge.setPosition(Math.toRadians(leftArmAngleDegrees));
//        rightArmHinge.setPosition(Math.toRadians(rightArmAngleDegrees));
        kickerServoRectDegrees = 30 * kickerServo.getInternalPosition();

        intakeMotor.update(millis);
        shooterMotor.update(millis);
        if(carbonToLoad != null)loadCarbon();

        /*
         * Handle shooting with the kicker servo
         */
        switch (kickerState){
            case LOADED:
                if (kickerServo.getInternalPosition() > 0.2) {
                    kickerState = KickerState.SHOOTING;
                    kickerTimer.reset();
                }
                break;
            case SHOOTING:
                if (kickerServo.getInternalPosition() <= 0.2) {
                    kickerState = KickerState.LOADING;
                    kickerTimer.reset();
                } else if (kickerServo.getInternalPosition() > 0.8 && kickerTimer.milliseconds() > 250){
                    kickerState = KickerState.SHOOT;
                    kickerTimer.reset();
                }
                break;
            case SHOOT:
                shootCarbon();
                kickerState = KickerState.SHOT_DONE;
                break;
            case SHOT_DONE:
                if (kickerServo.getInternalPosition() <= 0.8) {
                    kickerState = KickerState.LOADING;
                    kickerTimer.reset();
                }
                break;
            case LOADING:
                if (kickerServo.getInternalPosition() > 0.8) {
                    kickerState = KickerState.SHOT_DONE;
                    kickerTimer.reset();
                } else if (kickerServo.getInternalPosition() <= 0.2 && kickerTimer.milliseconds() > COOLDOWN){
                    kickerState = KickerState.LOADED;
                }
                break;
        }
    }

    public synchronized void updateDisplay(){
        super.updateDisplay();
//        ((Rotate)leftServoArm.getTransforms().get(0)).setAngle(leftArmAngleDegrees);
        ((Rotate)kickerServoRect.getTransforms().get(0)).setAngle(kickerServoRectDegrees);
//        ((Rotate)rightServoArm.getTransforms().get(0)).setAngle(rightArmAngleDegrees);
        loadedCarbonGroup.setVisible(!loadedCarbons.isEmpty());
    }

    public void powerDownAndReset(){
        super.powerDownAndReset();
    }

    /**
     * Listener method to handle Narrowphase collision. This method will look specifically for collisions that cause
     * the robot to control a previously un-controlled game element. This method is called DURING the world
     * update.
     *
     * Note that when ring collision with the intake is detected, the ring body is not immediately removed from
     * the dyn4j world. During so DURING the world update is not recommended (per dyn4j docs), and does cause
     * problems. Instead, save a reference to the ring to be loaded, and handle AFTER the world update, within
     * the updateStateAndSensors method.
     *
     * @param collision
     * @return True to allow collision resolution to continue; False to terminate collision resolution.
     */
    private boolean handleNarrowPhaseCollisions(NarrowphaseCollisionData<Body, BodyFixture> collision){
        BodyFixture f1 = collision.getFixture1();
        BodyFixture f2 = collision.getFixture2();
        if ((f1 == intakeFixture || f2 == intakeFixture) &&  loadedCarbons.size() < MAG_SIZE
                && carbonToLoad == null) {
            Body b = f1 == intakeFixture ? collision.getBody2() : collision.getBody1();
            double intakePower = intakeMotor.getPower();
            boolean intakeFwd = intakeMotor.getDirection() == DcMotorSimple.Direction.FORWARD;
            boolean intakeOn = intakePower > 0.5 && intakeFwd || intakePower < -0.5 && !intakeFwd;
            if (b.getUserData() instanceof Carbon && intakeOn){
                Carbon c = (Carbon)b.getUserData();
                if (!(c.getStatus() == Carbon.CarbonStatus.FLYING)) {
                    carbonToLoad = c;
                    return false;
                }
            }
        }
        return true;
    }

    private void loadCarbon(){
        carbonToLoad.setStatus(Carbon.CarbonStatus.CONTROLLED);
        if (!loadedCarbons.contains(carbonToLoad)) loadedCarbons.add(carbonToLoad);
        carbonToLoad = null;
    }

    private void shootCarbon(){
        double shooterPower = shooterMotor.getPower();
        double shooterVel = shooterMotor.getVelocity();
        boolean shooterFwd = shooterMotor.getDirection() == DcMotorSimple.Direction.FORWARD;
        boolean shooterOn = shooterPower > 0.5 && shooterFwd || shooterPower < -0.5 && !shooterFwd;
        if (loadedCarbons.isEmpty() || !shooterOn) return;
        Carbon r = loadedCarbons.remove(0);
        r.setStatus(Carbon.CarbonStatus.FLYING);
        // Initial position and velocity of the shot ring (meters and meters/sec)
        Vector2 pos = chassisBody.getWorldPoint(new Vector2(0, 12 / VirtualField.INCHES_PER_METER));
        Vector2 vel = chassisBody.getWorldVector(new Vector2(0, 60 / VirtualField.INCHES_PER_METER));
        vel.add(new Vector2(0, 12 / VirtualField.INCHES_PER_METER).cross(chassisBody.getAngularVelocity()));
        vel.multiply(Math.abs(shooterVel)/2240);
        r.setLocationMeters(pos);
        r.setVelocityMetersPerSec(vel.x, vel.y);
    }

}

package virtual_robot.games;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.NarrowphaseCollisionData;
import org.dyn4j.world.listener.CollisionListenerAdapter;
import virtual_robot.controller.Game;
import virtual_robot.controller.VirtualBot;
import virtual_robot.controller.VirtualField;
import virtual_robot.controller.VirtualGameElement;
import virtual_robot.game_elements.classes.Carbon;
import virtual_robot.game_elements.classes.Ring;
import virtual_robot.robots.classes.MecanumPhysicsBase;

import java.util.Random;

public class CarbonCapture extends Game {

    public static final Vector2 STARTER_STACK = new Vector2(36, -22.5);
    public static final Vector2 RING_RETURN = new Vector2(24, 67);

    public static final double RING_RETURN_VELOCITY = 20.0; // inches per second
    public static final double RING_RETURN_VELOCITY_VARIATION = 2.4; // inches per second

    public static final long RING_RELEASE_INTERVAL_MILLIS = 1500;
    public static final long RING_RELEASE_INTERVAL_MILLIS_VARIATION = 500;
    private boolean humanPlayerActive = true;
    private long nextRingReleaseTimeMillis = 0L;
    private int starterStackSize = -1;
    private final Random random = new Random();


    @Override
    public void initialize() {
        //Calling the super method causes the game elements to be created, populating the gameElements list.
        super.initialize();

        //Assign the game elements to the appropriate static final lists.
        for (VirtualGameElement e : gameElements) {
            System.out.println(e);

            if (e instanceof Carbon) {
                Carbon.carbons.add((Carbon) e);
            }
        }
        System.out.println(Carbon.carbons.size());

        /*
         * Add a collision listener to implement "special" handling of certain types of collision. For example,
         * this includes collisions involving rings that are stacked (to cause the stack to scatter). It should not
         * include collisions resulting in the robot controlling a game element: that should be handled within
         * the specific VirtualBot implementations.
         */
        world.addCollisionListener(new CollisionListenerAdapter<Body, BodyFixture>() {
            @Override
            public boolean collision(NarrowphaseCollisionData<Body, BodyFixture> collision) {
                return handleNarrowPhaseCollision(collision);
            }
        });

    }

    /**
     * Randomize starter stack size. Place wobbles and stacked rings on the field, and change
     * status of the stacked rings to STACKED (and all others OFF_FIELD).
     */
    @Override
    public void resetGameElements() {
        Carbon.carbonsOffField.clear();

        /*
         * Place all rings off-field, then put the appropriate number of rings back on the
         * field, in the ring stack.
         */

        for (Carbon r : Carbon.carbons) {
            r.setStatus(Carbon.CarbonStatus.OFF_FIELD);
        }
        updateDisplay();
    }

    @Override
    public boolean hasHumanPlayer() {
        return true;
    }

    @Override
    public boolean isHumanPlayerAuto() {
        return humanPlayerActive;
    }

    @Override
    public void setHumanPlayerAuto(boolean humanPlayerActive) {
        this.humanPlayerActive = humanPlayerActive;
    }

    /**
     * If the ring release interval has passed, and there are available off-field rings, deposit
     * a rolling ring at the release zone, with a randomized velocity.
     *
     * @param millis milliseconds since the previous update
     */
    @Override
    public void updateHumanPlayerState(double millis) {
        System.out.println(Carbon.carbonsOffField.size());
        if (Carbon.carbonsOffField.size() > 0) {
            Carbon r = Carbon.carbonsOffField.get(0);
            r.setLocationInches(RING_RETURN);
            r.setStatus(Carbon.CarbonStatus.ROLLING);
            Random random = new Random();
            double angle = (-45 - 90 * random.nextDouble()) * Math.PI / 180.0;
            double velocity = RING_RETURN_VELOCITY + RING_RETURN_VELOCITY_VARIATION * (0.5 - random.nextDouble());
            double vx = velocity * Math.cos(angle);
            double vy = velocity * Math.sin(angle);
            r.setVelocityInchesPerSec(vx, vy);
            System.out.println("HEY");
        }
            humanPlayerActionRequested = false;
    }


        /**
         * Narrowphase Collision event handler
         *
         * This will be called for all collisions, but needn't do any special processing for most of them. It
         * will handle:
         *  1) collision of any body with stacked rings--give stacked ring a nudge and cancel collision.
         *  2) collision of any body with a rolling ring--set ring to not rolling and continue with collision.
         *
         *  Return true to continue processing the collision, false to stop it.
         *
         *  Note: handling of collisions that result in the robot taking control of a game element should
         *  be handled by a listener set in the VirtualBot implementation.
         */
        private boolean handleNarrowPhaseCollision (NarrowphaseCollisionData < Body, BodyFixture > collision){

            boolean result = true;

            Body b1 = collision.getBody1();
            Body b2 = collision.getBody2();
            Object o1 = b1.getUserData();
            Object o2 = b2.getUserData();

            if (o1 instanceof Carbon) {
                Carbon r1 = (Carbon) o1;
                Vector2 vel1 = r1.getElementBody().getLinearVelocity();
                 if (r1.getStatus() == Carbon.CarbonStatus.ROLLING) {
                     r1.setVelocityMetersPerSec(-vel1.x / VirtualField.INCHES_PER_METER, -vel1.y / VirtualField.INCHES_PER_METER);
                     r1.setNextStatus(Carbon.CarbonStatus.ROLLING);
                }
            }

            if (o2 instanceof Carbon) {
                Carbon r2 = (Carbon) o2;
                Vector2 vel2 = r2.getElementBody().getLinearVelocity();
               if (r2.getStatus() == Carbon.CarbonStatus.ROLLING) {
                   r2.setVelocityMetersPerSec(-vel2.x / VirtualField.INCHES_PER_METER, -vel2.y / VirtualField.INCHES_PER_METER);
                   r2.setNextStatus(Carbon.CarbonStatus.ROLLING);
               }
            }

            if (o1 instanceof Carbon && o2 instanceof VirtualBot) {
                Carbon r1 = (Carbon) o1;
                Vector2 vel1 = r1.getElementBody().getLinearVelocity();
                if (r1.getStatus() == Carbon.CarbonStatus.ROLLING) {
                    r1.setVelocityMetersPerSec(-vel1.x / VirtualField.INCHES_PER_METER, -vel1.y / VirtualField.INCHES_PER_METER);
                    r1.setNextStatus(Carbon.CarbonStatus.ROLLING);
                }
            }

            if (o2 instanceof Carbon && o1 instanceof VirtualBot) {
                Carbon r2 = (Carbon) o2;
//                Vector2 velBot = ((VirtualBot) o1).getChassisBody().getLinearVelocity();
                Vector2 vel2 = r2.getElementBody().getLinearVelocity();
                if (r2.getStatus() == Carbon.CarbonStatus.ROLLING) {
                    r2.setVelocityMetersPerSec(-vel2.x / VirtualField.INCHES_PER_METER, -vel2.y / VirtualField.INCHES_PER_METER);
                    r2.setNextStatus(Carbon.CarbonStatus.ROLLING);
                }
            }


            return result;
        }

    }


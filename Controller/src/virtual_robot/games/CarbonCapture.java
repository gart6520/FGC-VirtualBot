package virtual_robot.games;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.NarrowphaseCollisionData;
import org.dyn4j.world.listener.CollisionListenerAdapter;
import virtual_robot.controller.*;
import virtual_robot.game_elements.classes.*;
//import virtual_robot.game_elements.classes.CompressorOpening;

import java.util.Random;

import static java.lang.Math.abs;

public class CarbonCapture extends Game {

    public static final Vector2 LEFT_CONTAINER = new Vector2(-(275.59-116.14)/2 + 50, 236.2/2);
    public static final Vector2 RIGHT_CONTAINER = new Vector2((275.59-116.14)/2 - 50, 236.2/2);

    public static final Vector2[] compressorLocs = {new Vector2(VirtualField.PIXELS_PER_INCH*119.5, VirtualField.PIXELS_PER_INCH*99),
            new Vector2(-VirtualField.PIXELS_PER_INCH*119, VirtualField.PIXELS_PER_INCH*99.5),
            new Vector2(-VirtualField.PIXELS_PER_INCH*120, -VirtualField.PIXELS_PER_INCH*100.8),
            new Vector2(VirtualField.PIXELS_PER_INCH*119.5, -VirtualField.PIXELS_PER_INCH*102),};


    public static final double CARBON_RETURN_VELOCITY = 75.0; // inches per second
    public static final double CARBON_RETURN_VELOCITY_VARIATION = 25; // inches per second

    private boolean humanPlayerActive = true;

    @Override
    public void initialize() {
        //Calling the super method causes the game elements to be created, populating the gameElements list.
        super.initialize();

        //Assign the game elements to the appropriate static final lists.
        for (VirtualGameElement e : gameElements) {

            if (e instanceof Carbon) {
                Carbon.carbons.add((Carbon) e);
            }
            if (e instanceof Compressor) {
                Compressor.compressors.add((Compressor) e);
            }
            if (e instanceof CompressorOpening) {
                CompressorOpening.openings.add((CompressorOpening) e);
            }
            if (e instanceof Sink){
                Sink.theSink = (Sink) e;
            }
            if (e instanceof SinkBarrier){
                SinkBarrier.theSinkBarrier = (SinkBarrier) e;
            }
        }

        /*
         * Add a collision listener to implement "special" handling of certain types of collision. For example,
         * this includes collisions involving rings that are stacked (to cause the stack to scatter). It should not
         * include collisions resulting in the robot controlling a game element: that should be handled within
         * the specific VirtualBot implementations.
         */
        world.addCollisionListener(new CollisionListenerAdapter<Body, BodyFixture>() {
            @Override
            public boolean collision(NarrowphaseCollisionData<Body, BodyFixture> collision) {
                return true;
            }
        });
        updateDisplay();

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
        for (int i = 0; i<Compressor.compressors.size(); i++) {
            Compressor c = Compressor.compressors.get(i);
            c.setLocation(compressorLocs[i].x, compressorLocs[i].y , 2.25*Math.PI/3 + (Math.PI/2)*i);
            c.setOnField(true);
        }
        for (int i = 0; i<CompressorOpening.openings.size(); i++) {
            CompressorOpening c = CompressorOpening.openings.get(i);
            c.setLocation(compressorLocs[i].x, compressorLocs[i].y , 2.25*Math.PI/3 + (Math.PI/2)*i);
            c.setOnField(true);
        }
        for (int i = 1; i<50; i++){
            Carbon r = Carbon.carbonsOffField.get(0);
            Random random = new Random();
            r.setLocationInches(RIGHT_CONTAINER);
            r.setStatus(Carbon.CarbonStatus.ROLLING);
            double angle = (-180 * random.nextDouble()) * Math.PI / 180.0;
            double velocity = CARBON_RETURN_VELOCITY + CARBON_RETURN_VELOCITY_VARIATION * (random.nextDouble());
            double vx = velocity * Math.cos(angle);
            double vy = velocity * Math.sin(angle);
            r.setVelocityInchesPerSec(vx, vy);
        }
        for (int i = 1; i<50; i++){
            Carbon r = Carbon.carbonsOffField.get(0);
            Random random = new Random();
            r.setLocationInches(LEFT_CONTAINER);
            r.setStatus(Carbon.CarbonStatus.ROLLING);
            double angle = (-180 * random.nextDouble()) * Math.PI / 180.0;
            double velocity = CARBON_RETURN_VELOCITY + CARBON_RETURN_VELOCITY_VARIATION * (random.nextDouble());
            double vx = velocity * Math.cos(angle);
            double vy = velocity * Math.sin(angle);
            r.setVelocityInchesPerSec(vx, vy);
        }

        Sink.theSink.setOnField(true);
        Sink.theSink.setLocation(0,0);

        SinkBarrier.theSinkBarrier.setOnField(true);
        SinkBarrier.theSinkBarrier.setLocation(0,0);
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
//
//            if (o1 instanceof Carbon && o2 instanceof Wall) {
//                System.out.println("HEYYY");
//                /*Elastic collision physics between the balls*/
//                Carbon r1 = (Carbon) o1;
//                Vector2 vel1 = r1.getElementBody().getLinearVelocity();
//                if (r1.getStatus() == Carbon.CarbonStatus.ROLLING) {
//                    r1.setVelocityMetersPerSec(vel1.x / VirtualField.INCHES_PER_METER, -vel1.y / VirtualField.INCHES_PER_METER);
//                    r1.setNextStatus(Carbon.CarbonStatus.ROLLING);
//                }
//            }
//
//            if (o2 instanceof Carbon && o1 instanceof Wall) {
//                /*Elastic collision physics between the balls*/
//                System.out.println("HEYYY");
//
//                Carbon r1 = (Carbon) o2;
//                Vector2 vel1 = r1.getElementBody().getLinearVelocity();
//                if (r1.getStatus() == Carbon.CarbonStatus.ROLLING) {
//                    r1.setVelocityMetersPerSec(vel1.x / VirtualField.INCHES_PER_METER, -vel1.y / VirtualField.INCHES_PER_METER);
//                    r1.setNextStatus(Carbon.CarbonStatus.ROLLING);
//                }
//            }
//
//            if (o2 instanceof Carbon) {
//                Carbon r2 = (Carbon) o2;
//                Vector2 vel2 = r2.getElementBody().getLinearVelocity();
//               if (r2.getStatus() == Carbon.CarbonStatus.ROLLING) {
//                   r2.setVelocityMetersPerSec(-vel2.x / VirtualField.INCHES_PER_METER, -vel2.y / VirtualField.INCHES_PER_METER);
//                   r2.setNextStatus(Carbon.CarbonStatus.ROLLING);
//               }
//            }

//            if (o1 instanceof Carbon && o2 instanceof VirtualBot) {
//                Carbon r1 = (Carbon) o1;
//                Vector2 velBot = ((VirtualBot) o2).getChassisBody().getLinearVelocity();
//                if(abs(velBot.getMagnitude()) > abs(maxBotVelocity.getMagnitude())){
////                    maxBotVelocity = velBot;
//                    System.out.println("HSHSHSSH");
//                }
//                if (r1.getStatus() == Carbon.CarbonStatus.ROLLING) {
//                    r1.setVelocityMetersPerSec(maxBotVelocity.x / VirtualField.INCHES_PER_METER, maxBotVelocity.x / VirtualField.INCHES_PER_METER);
//                    r1.setNextStatus(Carbon.CarbonStatus.ROLLING);
//                }
//                System.out.println(maxBotVelocity);
//                System.out.println(abs(velBot.getMagnitude()));
//                System.out.println(abs(maxBotVelocity.getMagnitude()));
//            }
//
//            if (o2 instanceof Carbon && o1 instanceof VirtualBot) {
//                Carbon r2 = (Carbon) o2;
////                Vector2 velBot = ((VirtualBot) o1).getChassisBody().getLinearVelocity();
//                Vector2 velBot = ((VirtualBot) o1).getChassisBody().getLinearVelocity();
//
//                if(abs(velBot.getMagnitude()) > abs(maxBotVelocity.getMagnitude())){
////                    maxBotVelocity = velBot;
//                    System.out.println("HYSYYWYW");
//                }
//                if (r2.getStatus() == Carbon.CarbonStatus.ROLLING) {
//                    r2.setVelocityMetersPerSec(maxBotVelocity.x / VirtualField.INCHES_PER_METER, maxBotVelocity.y / VirtualField.INCHES_PER_METER);
//                    r2.setNextStatus(Carbon.CarbonStatus.ROLLING);
//                }
//                System.out.println(abs(velBot.getMagnitude()));
//                System.out.println(abs(maxBotVelocity.getMagnitude()));
//            }

            return result;
        }

    }


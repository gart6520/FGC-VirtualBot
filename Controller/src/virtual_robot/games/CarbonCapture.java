package virtual_robot.games;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.NarrowphaseCollisionData;
import org.dyn4j.world.listener.CollisionListenerAdapter;
import virtual_robot.controller.*;
import virtual_robot.game_elements.classes.*;
import virtual_robot.util.Vector2D;
//import virtual_robot.game_elements.classes.CompressorOpening;

import java.util.Random;

public class CarbonCapture extends Game {

    public static final Vector2 LEFT_CONTAINER = new Vector2(-(275.59-116.14)/2, 236.2/2);
    public static final Vector2 RIGHT_CONTAINER = new Vector2((275.59-116.14)/2, 236.2/2);

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
                return handleNarrowPhaseCollision(collision);
            }
        });
        updateDisplay();

    }

    /**
     * Giving the Carbons initial velocity and placing - rotating the other Game Elements (Compressors, Sink etc.)
     */
    @Override
    public void resetGameElements() {
        Carbon.carbonsOffField.clear();

        /*
         * Place all carbons off-field.
         */

        for (Carbon r : Carbon.carbons) {
            r.setStatus(Carbon.CarbonStatus.OFF_FIELD);
        }
        /*
         * Place all compressors on their according positions with the correct rotation.
         */
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
        /*
         * Giving the Carbons initial velocity
         */
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
     * If a human player action is requested, a captured carbon will be given an initial velocity towards the sink
     *
     * @param millis milliseconds since the previous update
     */
    @Override
    public void updateHumanPlayerState(double millis) {
        if(Compressor.capturedCarbons.size()>0 && humanPlayerActionRequested){
            Carbon c = Compressor.capturedCarbons.get(0);
            c.setStatus(Carbon.CarbonStatus.FLYING);
            Vector2D velocity = c.getLocation().normalized().multiplied(-CARBON_RETURN_VELOCITY);
            c.setVelocityInchesPerSec(velocity.x, velocity.y);
            Compressor.capturedCarbons.remove(c);
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


            Body b1 = collision.getBody1();
            Body b2 = collision.getBody2();
            Object o1 = b1.getUserData();
            Object o2 = b2.getUserData();

            if (o1 instanceof Carbon && o2 instanceof Sink) {
                Carbon r1 = (Carbon) o1;
                if (r1.getStatus() == Carbon.CarbonStatus.FLYING) {
                    r1.setNextStatus(Carbon.CarbonStatus.OFF_FIELD);
                }
            }

            if (o2 instanceof Carbon && o1 instanceof Sink) {
                Carbon r2 = (Carbon) o2;
                if (r2.getStatus() == Carbon.CarbonStatus.FLYING) {
                    r2.setNextStatus(Carbon.CarbonStatus.OFF_FIELD);
                }
            }


            return true;
        }

    }


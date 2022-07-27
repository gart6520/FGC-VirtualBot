package virtual_robot.game_elements.classes;

import org.dyn4j.collision.CategoryFilter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.MassType;
import virtual_robot.controller.Filters;
import virtual_robot.controller.GameElementConfig;
import virtual_robot.controller.VirtualField;
import virtual_robot.controller.VirtualGameElement;
import virtual_robot.dyn4j.Dyn4jUtil;
import virtual_robot.dyn4j.FixtureData;
import virtual_robot.games.CarbonCapture;
/**
 * Just a Barrier Object that lives in the center of the field and collides with the robot so that they don't go under the sink completely.
 */
@GameElementConfig(name = "SinkBarrier", filename = "sink_barrier", forGame = CarbonCapture.class)
public class SinkBarrier extends VirtualGameElement {
    private Body sinkBody;

    public static SinkBarrier theSinkBarrier;


    public static final long SINK_CATEGORY = 432;
    public static final CategoryFilter SINK_BARRIER_FILTER = new CategoryFilter(SINK_CATEGORY, Filters.MASK_ALL);

    @Override
    public void updateState(double millis) {
        x = sinkBody.getTransform().getTranslationX() * VirtualField.PIXELS_PER_METER;
        y = sinkBody.getTransform().getTranslationY() * VirtualField.PIXELS_PER_METER;
        headingRadians = sinkBody.getTransform().getRotationAngle();
    }

    @Override
    public void setUpBody() {
        elementBody = Dyn4jUtil.createBody(displayGroup, this, 0, 0, new FixtureData(SINK_BARRIER_FILTER, 1, 0, 0));
        sinkBody = elementBody;
        sinkBody.setMass(MassType.INFINITE);
    }
}

package virtual_robot.game_elements.classes;

import javafx.fxml.FXML;
import javafx.scene.shape.Circle;
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

@GameElementConfig(name = "Sink", filename = "sink", forGame = CarbonCapture.class)
public class Sink extends VirtualGameElement {
    private Body sinkBody;

    public static Sink theSink;

//    An object that represents the inner circle of the sink. This is where the balls land
    @FXML
    private Circle innerCircle;

    public static final long SINK_CATEGORY = 512;
    public static final CategoryFilter SINK_FILTER = new CategoryFilter(SINK_CATEGORY, Filters.MASK_ALL & ~Filters.CHASSIS);

    @Override
    public void updateState(double millis) {
        x = sinkBody.getTransform().getTranslationX() * VirtualField.PIXELS_PER_METER;
        y = sinkBody.getTransform().getTranslationY() * VirtualField.PIXELS_PER_METER;
        headingRadians = sinkBody.getTransform().getRotationAngle();
    }

    @Override
    public void setUpBody() {
        elementBody = Dyn4jUtil.createBody(innerCircle, this, 0, 0, new FixtureData(SINK_FILTER, 1, 0, 0));
        sinkBody = elementBody;
        sinkBody.setMass(MassType.INFINITE);
    }
}

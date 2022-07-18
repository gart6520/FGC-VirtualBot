package virtual_robot.game_elements.classes;

import com.qualcomm.robotcore.util.MovingStatistics;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import org.dyn4j.collision.CategoryFilter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rotation;
import org.dyn4j.geometry.Transform;
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
    private Body compressorBody = null;
    private Body openingBody = null;
    public static List<Compressor> compressors = new ArrayList<>();


    // Category and filter for collisions
    public static final long COMPRESSOR_CATEGORY = 1024;
    public static final CategoryFilter COMPRESSOR_FILTER = new CategoryFilter(COMPRESSOR_CATEGORY, Filters.MASK_ALL);
//    public static final CategoryFilter OPENING_FILTER = new CategoryFilter(COMPRESSOR_CATEGORY, Filters.CHASSIS);

    @Override
    public void initialize(){
        super.initialize();
    }

    @Override
    public void updateState(double millis) {
        x = compressorBody.getTransform().getTranslationX() * VirtualField.PIXELS_PER_METER;
        y = compressorBody.getTransform().getTranslationY() * VirtualField.PIXELS_PER_METER;
        headingRadians = compressorBody.getTransform().getRotationAngle();
    }
    @Override
    public synchronized void updateDisplay() {
        super.updateDisplay();
    }

    @Override
    public void setUpBody() {

        elementBody = Dyn4jUtil.createBody(sides, this, 0, 0, new FixtureData(COMPRESSOR_FILTER, 1, 0, 0));
        compressorBody = elementBody;
        compressorBody.setMass(MassType.INFINITE);
    }

}

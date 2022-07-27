# A 2D simulator to help beginning Java programmers learn to program for the First Global Challenge.
### _From FGC Team Greece for the all the FGC community!_ 

New:  Carbon capture game adaptation, including the Carbon Capture game elements and two new robot 
configurations (Carbon Tank Bot and Carbon Omni Bot) with an intake and shooter mechanism, resembling the shooters showcased in FGC site. There is a test opmode available for this robot configuration.

If you prefer not to have game elements littering the field, then set the game to "NoGame" in the 
virtual_robot.config.Config.java file (in the Controller module).
You can also use a virtual Gamepad by setting changing the boolean value in virtual_robot.config.Config.java file (in the Controller module).

Includes Programming Board configuration to serve as a companion to the book "Learn Java For FTC", by Alan Smith. The
PDF can be [downloaded for free](https://github.com/alan412/LearnJavaForFTC) or you can purchase the paperback on
[Amazon](https://www.amazon.com/dp/B08DBVKXLZ).

![](/readme_image.JPG)
### **Detailed installation instructions can be found [here](/Detailed%20Installation%20Instructions.pdf)**

This is a JavaFX application developed using the (free) IntelliJ IDEA Community Edition IDE. The repository can be downloaded
and unzipped, then opened with IntelliJ. It can also be run using Android Studio 
(see this [video](https://www.youtube.com/watch?v=pmaT9Twbmao)).

Multiple robot configurations are available. XDriveBot has four corner-mounted Omni-wheels, a rear arm driven by a cr-servo, 
and three dead-wheel encoders. TwoWheelBot has a two wheel chassis, a rear servo, along with many sensors to play with.

Each robot can be thought of as the competition allowed size.  For the two-wheel bot and mecanum wheel bots, the distance between
the centers of the right and left wheels is 16 inches. 
For the X-Drive bot, the distance between the centers of any two adjacent wheels is 14.5 inches. Each motor has an
encoder. There is a downward-facing color sensor in the center of each robot. A BNO055 IMU is also 
included. Each robot also has distance sensors on the front, left, right and back sides. A small green rectangle 
indicates the front of each robot. Wheel diameters are all 4 inches. For the robots with dead-wheel encoders 
(MecanumBot and XDriveBot), the forward-reverse encoder wheels are mounted 6 inches to the right and left of center, 
while the X-direction (i.e., right-left) encoder wheel is mounted at the center. The dead-wheels are two inches in 
diameter. Positioning of the dead-wheels can be changed easily in the robot configuration classes.

The field can be thought of the official field size. The field graphic (currently the Carbon Capture field)
is obtained from a bitmap (.bmp) image. The color sensor detects the field color beneath the center of the
robot. The field graphic is easily changed by providing a different .bmp image in the virtual_robot.config.Config class.
The .bmp image is the freight_field648.bmp file in the virtual_robot.assets folder. If a different .bmp image is used,
it must be at least as wide and as tall as the field dimensions (currently 648 x 648 pixels to fit on the screen of
most laptops). The Config class also allows selection between the use of "real" hardware gamepads versus a
"virtual gamepad".

In addition to the robot configurations described above, there is an additional configuration called
"ProgrammingBoard". It is meant to emulate the programming board described in the book "Learn Java For FTC", by
Alan Smith.  (The PDF can be [downloaded for free](https://github.com/alan412/LearnJavaForFTC) or you can purchase 
the paperback on [Amazon](https://www.amazon.com/dp/B08DBVKXLZ).) It is a board with several hardware devices 
attached: DcMotor, Servo, Potentiometer, Touch Sensor, and a Color-Distance Sensor. It also has a BNO055 IMU. 
The board doesn't move around the field, but it can be rotated (to test the IMU) by dragging the board chassis.

An abridged approximation of the FTC SDK is provided.

User-defined OpModes must be placed in the org.firstinspires.ftc.teamcode package, and must extend OpMode 
(or LinearOpMode). OpModes are registered by placing a @TeleOp or @Autonomous annotation immediately above the class 
declaration.

The OpMode (and therefore LinearOpMode) class in the simulator provides access to:

  1. A HardwareMap object, which in turn provides access to the DCMotor objects, the BNO055 IMU sensor, distance 
     sensors, the servo, and the color sensor;
  2. Two GamePads(actual hardware gamepads, though there is an option to use a "virtual gamepad" -- see Log of 
     Changes below);
  3. A Telemetry object.

An approximation of the FTC SDK's ElapsedTime class is provided in the time package.

Several example OpModes are provided in the org.firstinspires.ftc.teamcode package. To minimize clutter, a number 
of sample op modes are currently disabled; they can be re-enabled by commenting out the @Disabled annotation. A 
number of robot configurations are also disabled. A robot configuration can be re-enabled by finding its class 
in the virtual_robot.robots.classes package, and un-commenting its @BotConfig annotation.

To use:

  1. Make sure you have the Java 8 JDK installed on your PC. Also, install the free Community Edition of JetBrains
     IntelliJ IDEA.
  2. Download the virtual_robot .zip, and extract contents. Open the project in IntelliJ. You'll see three modules in
     the project (Controller, TeamCode, and virtual_robot) -- the only module you'll need to touch is TeamCode. It
     contains the org.firstinspires.ftc.teamcode package.
  3. Write your OpModes in the org.firstinspires.ftc.teamcode package; make sure to include a @TeleOp or @Autonomous 
     annotation. These must extend the OpMode class (may either extend OpMode OR LinearOpMode). OpMode must provide 
     init() and loop() methods; LinearOpMode must provide runOpMode() method.
  4. Make sure at least one gamepad is plugged in to the computer.
  5. Run the application (by clicking the green arrowhead at the toolbar).
  6. Press start-A or start-B on gamepad(s) to select which is gamepad1 vs. gamepad2.
  7. Use Configuration dropdown box to select a robot configuration. The configuration will be displayed.
  8. Use the Op Mode drop down box to select the desired OpMode.
  9. Prior to initialization, position the robot on the field by left-mouse-clicking the field (for robot position),
     and right-mouse-clicking (for robot orientation).
  10. Use the INIT/START/STOP button as you would on the FTC Driver Station.
  11. If desired use the sliders to introduce random and systematic motor error, and inertia.


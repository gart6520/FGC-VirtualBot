package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.*;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.navigation.*;

@TeleOp(name = "Tank Demo Op Mode", group = "Carbon")
public class CarbonTankOpMode extends OpMode {

    private DcMotor left = null;
    private DcMotor right = null;
    private DcMotorEx intakeMotor = null;
    private DcMotorEx shooterMotor = null;
    private BNO055IMU imu = null;
    private Servo leftServo = null;
    private Servo rightServo = null;
    private Servo shooterServo = null;

    private ElapsedTime et = null;
    private int waitForStartTime = 0;

    private boolean openScoop = false;

    public void init(){
        left = hardwareMap.dcMotor.get("left_motor");
        right = hardwareMap.dcMotor.get("right_motor");
        left.setDirection(DcMotor.Direction.REVERSE);

        intakeMotor = hardwareMap.get(DcMotorEx.class, "intake_motor");
        shooterMotor = hardwareMap.get(DcMotorEx.class, "shooter_motor");

        imu = hardwareMap.get(BNO055IMU.class, "imu");
        leftServo = hardwareMap.servo.get("left_servo");
        rightServo = hardwareMap.servo.get("right_servo");
        shooterServo = hardwareMap.servo.get("kicker_servo");

        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.accelerationIntegrationAlgorithm = null;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.calibrationData = null;
        parameters.calibrationDataFile = "";
        parameters.loggingEnabled = false;
        parameters.loggingTag = "Who cares.";
        imu.initialize(parameters);

        et = new ElapsedTime();
    }

    public void init_loop(){
        if (et.milliseconds() >= 1000) {
            waitForStartTime++;
            et.reset();
        }
        telemetry.addData("Press Start to Continue"," %d", waitForStartTime);
    }

    public void loop(){
        double drive = -gamepad1.left_stick_y;
        double turn = gamepad1.right_stick_x;

        if (gamepad1.a) { intakeMotor.setPower(1); }
        if(gamepad1.y){ intakeMotor.setPower(0); }

        if (gamepad1.b) { intakeMotor.setPower(0); }
        if(gamepad1.x){ shooterMotor.setPower(1); }

        if(gamepad1.right_bumper){
            shooterServo.setPosition(0.95);
            pause(100);
            shooterServo.setPosition(0);
        }
        if(gamepad1.dpad_up){
            leftServo.setPosition(0.5);
        }
        if(gamepad1.dpad_down){
            leftServo.setPosition(0);
        }


        left.setPower(drive + turn);
        right.setPower(drive - turn);

        telemetry.addData("Drivetrain", "Left %.2f Right %.2f", left.getPower(), right.getPower());
        telemetry.addData("Shooter", "Power %.2f Speed %.2f", shooterMotor.getPower(), shooterMotor.getVelocity());
        telemetry.addData("Intake", "Power %.2f", intakeMotor.getPower());
        telemetry.addData("Scoop", "Open %b", openScoop);

    }

    private void pause(long ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

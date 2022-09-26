package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Subsystems.DriveBase;
import org.firstinspires.ftc.teamcode.Subsystems.IMU;

public class Robot {
    private DriveBase driveBase;
    private IMU imu;
    private Gamepad gamepad;
    private Telemetry telemetry;

    public Robot(OpMode opMode) {
        gamepad = opMode.gamepad1;
        telemetry = opMode.telemetry;
        driveBase = new DriveBase(opMode);
        imu = new IMU(opMode);
    }

    public void init() {
        driveBase.init();
        imu.init();
    }

    public void loop() {
        double leftSpeed = -1;
        double rightSpeed = -1;
        driveBase.setSpeed(leftSpeed, rightSpeed);
    }
}

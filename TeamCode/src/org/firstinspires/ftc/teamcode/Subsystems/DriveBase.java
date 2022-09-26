package org.firstinspires.ftc.teamcode.Subsystems;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class DriveBase {
    private DcMotorEx left;
    private DcMotorEx right;
    private HardwareMap hardwareMap;

    public DriveBase(OpMode opMode) {
        hardwareMap = opMode.hardwareMap;
    }

    public void init() {
        left = hardwareMap.get(DcMotorEx.class, "left_motor");
        right = hardwareMap.get(DcMotorEx.class, "right_motor");

        left.setDirection(DcMotorSimple.Direction.FORWARD);
        right.setDirection(DcMotorSimple.Direction.REVERSE);

        left.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        right.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public void setSpeed(double leftSpeed, double rightSpeed) {
        left.setPower(leftSpeed);
        right.setPower(rightSpeed);
    }

    public int getLeftPosition() {
        return left.getCurrentPosition();
    }

    public int getRightPosition() {
        return right.getCurrentPosition();
    }
    
}


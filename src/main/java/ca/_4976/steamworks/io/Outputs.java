package ca._4976.steamworks.io;

import ca._4976.library.AsynchronousRobot;
import ca._4976.library.outputs.PWMMotor;

public class Outputs {

    public PWMMotor driveLeftFront;
    public PWMMotor driveLeftRear;
    public PWMMotor driveRightFront;
    public PWMMotor driveRightRear;

    public Outputs(AsynchronousRobot module) {

        driveLeftFront = new PWMMotor(module, 1, 0.02);
        driveLeftRear = new PWMMotor(module, 2, 0.02);
        driveRightFront = new PWMMotor(module, 0, 0.02);
        driveRightRear = new PWMMotor(module, 3, 0.02);
    }
}
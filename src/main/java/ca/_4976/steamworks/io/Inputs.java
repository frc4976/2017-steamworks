package ca._4976.steamworks.io;

import ca._4976.library.AsynchronousRobot;
import ca._4976.library.inputs.DigitalEncoder;
import ca._4976.library.inputs.Digital;

public class Inputs {

    public DigitalEncoder driveLeft;
    public DigitalEncoder driveRight;
    public DigitalEncoder shooter_encoder;

    public Digital bottomOfHE, bottomOfSHE, topOfSHE, TESTINGSWITCH;
    public Digital winchSensor;
    public Digital gearSense;

    public Inputs(AsynchronousRobot module) {

        driveRight = new DigitalEncoder(module, 0, 1);
        driveLeft = new DigitalEncoder(module, 2, 3);
        shooter_encoder = new DigitalEncoder(module, 12, 13);

        driveLeft.setReversed(true);
        driveRight.setReversed(true);

        driveLeft.setScalar(0.00031179492);
        driveRight.setScalar(0.00031179492);

        driveLeft.setMinRate(0);
        driveRight.setMinRate(0);

        bottomOfHE = new Digital(module, 4);
        bottomOfSHE = new Digital(module, 5);
        topOfSHE = new Digital(module, 6);
        TESTINGSWITCH = new Digital(module, 9);
        gearSense = new Digital(module, 7);
        winchSensor = new Digital(module, 8);

    }
}

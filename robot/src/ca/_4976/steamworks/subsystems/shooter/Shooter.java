package ca._4976.steamworks.subsystems.shooter;

import ca._4976.library.Evaluable;
import ca._4976.library.listeners.ButtonListener;
import ca._4976.library.listeners.RobotStateListener;
import ca._4976.steamworks.Robot;
import com.ctre.CANTalon;

public class Shooter {

    private Config config = new Config();
    private Robot robot;
    private boolean enableCorrection = false;

    private int selection = 0;

    public Shooter(Robot robot) {

        this.robot = robot;

        config.setListener(() -> {

            System.out.println("<Shooter>\t");
            System.out.println("\t\tTarget RPM:\t" + getTargetRPM());
            System.out.println("\t\tHood Position:\t" + robot.outputs.hood.get());

            if (robot.outputs.shooter.get() != 0) robot.outputs.shooter.set(config.targetSpeed[selection]);

            robot.outputs.hood.set(config.hoodPosition[selection]);

            if (!robot.vision.goal.isRunning() && robot.outputs.pivot.get() == 0) {

                robot.outputs.pivot.changeControlMode(CANTalon.TalonControlMode.Position);
                robot.outputs.pivot.set(config.turretPosition[selection]);
            }
        });

        robot.addListener(new RobotStateListener() {

            @Override public void disabledInit() {

                robot.vision.goal.stop();
                robot.outputs.shooter.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
                robot.outputs.shooter.set(0);
            }
        });

        robot.operator.A.addListener(new ButtonListener() {

            @Override public void pressed() {

                if (robot.outputs.shooter.get() == 0) {

                    System.out.println("<Shooter> Priming the Shooter.");

                    if (robot.status.pivotEncoderFunctional) robot.vision.goal.start();

                    else System.out.println("<Shooter> Turret encoder not functional automated functions disabled.");

                    robot.outputs.shooter.changeControlMode(CANTalon.TalonControlMode.Speed);
                    robot.outputs.shooterSlave.changeControlMode(CANTalon.TalonControlMode.Follower);
                    robot.outputs.shooterSlave.set(12);
                    robot.outputs.shooter.set(config.targetSpeed[selection]);

                } else if (!robot.vision.goal.isRunning() && robot.status.pivotEncoderFunctional) {

                    System.out.println("<Shooter> Looking for target.");

                    robot.vision.goal.start();

                } else {

                    System.out.println("<Shooter> Stopping the Shooter.");

                    robot.vision.goal.stop();
                    robot.outputs.shooter.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
                    robot.outputs.shooter.set(0);
                }
            }

            @Override public void held() {

                enableCorrection = true;
                robot.drive.setLimiter(.5);

                System.out.println("<Shooter> Priming the Shooter.");

                if (robot.status.pivotEncoderFunctional) {

                    robot.vision.goal.enableCorrectRPM(true);
                    robot.vision.goal.start();
                }

                else System.out.println("<Shooter> Turret encoder not functional automated functions disabled.");

                robot.outputs.shooter.changeControlMode(CANTalon.TalonControlMode.Speed);
                robot.outputs.shooterSlave.changeControlMode(CANTalon.TalonControlMode.Follower);
                robot.outputs.shooterSlave.set(12);
                robot.outputs.shooter.set(config.targetSpeed[selection]);
            }

            @Override public void falling() {

                enableCorrection = false;
                robot.vision.goal.enableCorrectRPM(false);
                robot.drive.setLimiter(1);
            }
        });

        robot.operator.B.addListener(new ButtonListener() {

            @Override public void pressed() {

                if (Math.abs(robot.outputs.shooter.getError()) < config.targetError[selection]) {

                    System.out.println("<Shooter> Taking a shot.");

                    robot.elevator.run();

                    robot.runNextLoop(() -> { if (!robot.operator.B.get()) robot.elevator.stop(); }, 100);
                }
            }

            @Override public void held() {

                if (Math.abs(robot.outputs.shooter.getError()) < config.targetError[selection]) {

                    System.out.println("<Shooter> Beginning to shoot.");

                    new Evaluable() {

                        @Override public void eval() {

                            if (Math.abs(robot.outputs.shooter.getError()) > config.targetError[selection]) {

                                if (robot.elevator.isRunning())
                                    System.err.println("<Shooter> WARN: (RPM) too low to fire.");

                                robot.elevator.stop();

                            } else if (robot.operator.B.get() || robot.isAutonomous()) robot.elevator.run();

                            if (robot.operator.B.get()) robot.runNextLoop(this);
                        }

                    }.eval();

                } else System.err.println("<Shooter> WARN: (RPM) too low to fire.");
            }

            @Override public void falling() {

                if (robot.elevator.isRunning()) System.out.println("<Shooter> Finished shooting.");

                robot.elevator.stop();
            }
        });

        robot.operator.X.addListener(new ButtonListener() {

            @Override public void pressed() {

                robot.vision.goal.stop();
                robot.outputs.shooter.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
                robot.outputs.shooter.set(0);

                System.out.println("<Shooter> Stopping the shooter.");
            }
        });

        robot.operator.LEFT.addListener(new ButtonListener() {

            @Override public void pressed() {

                selection = 0;
                config.runListener();
            }
        });

        robot.operator.UP.addListener(new ButtonListener() {

            @Override public void pressed() {

                selection = 1;
	            config.runListener();
            }
        });

        robot.operator.RIGHT.addListener(new ButtonListener() {

            @Override public void pressed() {

                selection = 2;
	            config.runListener();
            }
        });

        robot.operator.DOWN.addListener(new ButtonListener() {

            @Override public void pressed() {

                selection = 3;
	            config.runListener();
            }
        });

        robot.operator.LH.addListener(value -> {

            robot.outputs.pivot.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
            robot.outputs.pivot.set(Math.abs(value) > 0.4 ? value * -0.3 : 0);
        });

        robot.operator.LV.addListener(value -> new Evaluable() {

            @Override public void eval() {

                if (value == robot.operator.LV.get() && value != 0 && Math.abs(value) > 0.4) {

                    config.targetSpeed[selection] = config.targetSpeed[selection] - value * 2;

                    if (robot.outputs.shooter.get() != 0) robot.outputs.shooter.set(config.targetSpeed[selection]);

                    robot.runNextLoop(this);

                } else if (Math.abs(value) <= 0.4) {

                    if (config.targetSpeed[selection] != ((int) config.targetSpeed[selection] / 10) * 10) {

                        config.targetSpeed[selection] = ((int) config.targetSpeed[selection] / 10) * 10;

                        if (robot.outputs.shooter.get() != 0) robot.outputs.shooter.set(config.targetSpeed[selection]);

                        System.out.println("<Shooter> Target Speed Set to: " + config.targetSpeed[selection]);
                    }
                }
            }

        }.eval());
    }

    public double getTargetRPM() {

        if (robot.outputs.shooter.getControlMode() == CANTalon.TalonControlMode.Speed) {

            return robot.outputs.shooter.getSetpoint();

        } else return config.targetSpeed[selection];
    }

    public void correctRPM(double correction) {

        if (robot.outputs.shooter.get() != 0 && enableCorrection) {

            robot.outputs.shooter.set(config.targetSpeed[selection] + correction);
        }
    }

    public void setTargetRPM(double speed) {

        config.targetError[4] = config.targetError[selection];
        selection = 4;
        config.targetSpeed[selection] = speed;
    }

    public void run() {

        robot.outputs.shooter.changeControlMode(CANTalon.TalonControlMode.Speed);
        robot.outputs.shooterSlave.changeControlMode(CANTalon.TalonControlMode.Follower);
        robot.outputs.shooterSlave.set(12);
        robot.outputs.shooter.set(config.targetSpeed[selection]);
    }
}
/* Authors: Arin Khare, Kai Vernooy
 */

package org.firstinspires.ftc.teamcode;

import java.util.concurrent.TimeUnit;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.Telemetry;


/** A completely encompassing class of all functionality of the robot. An OpMode should interface through an instance of
 *  this class in order to send or receive any data with the real robot.
 */
public class RobotManager {

    // These indicate both the tasks and the starting position. For example, DUCK_CAROUSEL has the robot start close to
    // the carousel and deliver the duck.
    public enum NavigationMode {DUCK_CAROUSEL, DUCK_WAREHOUSE, NO_DUCK_CAROUSEL, NO_DUCK_WAREHOUSE, TELEOP}
    public enum AllianceColor {BLUE, RED}

    public Robot robot;

    public MechanismDriving mechanismDriving;
    public Navigation navigation;
    public ComputerVision computerVision;

    private GamepadWrapper gamepads, previousStateGamepads;

    private Telemetry telemetry;
    private ElapsedTime elapsedTime;

    public RobotManager(HardwareMap hardwareMap, Gamepad gamepad1, Gamepad gamepad2,
                        NavigationMode navigationMode, AllianceColor allianceColor,
                        Telemetry telemetry, ElapsedTime elapsedTime) {

        elapsedTime.reset();
        navigation = new Navigation(navigationMode, allianceColor);
        mechanismDriving = new MechanismDriving(allianceColor);

        robot = new Robot(hardwareMap, telemetry, elapsedTime);

        computerVision = new ComputerVision(hardwareMap, new AutonPipeline(robot, telemetry, allianceColor));

        gamepads = new GamepadWrapper(gamepad1, gamepad2);
        previousStateGamepads = new GamepadWrapper();
        previousStateGamepads.copyGamepads(gamepads);
    }

    // TELE-OP
    // =======

    /** Determine new robot desired states based on controller input (checks for button releases)
     */
    public void readControllerInputs() {
        // Carousel
        if (getButtonRelease(GamepadWrapper.DriverAction.START_STOP_CAROUSEL)) {
            switch (robot.desiredCarouselState) {
                case STOPPED:
                    robot.desiredCarouselState = Robot.CarouselState.SPINNING;
                    break;
                case SPINNING:
                    robot.desiredCarouselState = Robot.CarouselState.STOPPED;
                    break;
            }
        }

        // Claw
        if (getButtonRelease(GamepadWrapper.DriverAction.OPEN_CLAW)) {
            robot.desiredClawState = Robot.ClawState.OPEN;
        }
        if (getButtonRelease(GamepadWrapper.DriverAction.CLOSE_CLAW)) {
            robot.desiredClawState = Robot.ClawState.CLOSED;
        }

        // Linear slides
        if (getButtonRelease(GamepadWrapper.DriverAction.SET_SLIDES_RETRACTED)) {
            robot.desiredSlidesState = Robot.SlidesState.RETRACTED;
        }
        if (getButtonRelease(GamepadWrapper.DriverAction.SET_SLIDES_L1)) {
            robot.desiredSlidesState = Robot.SlidesState.L1;
        }
        if (getButtonRelease(GamepadWrapper.DriverAction.SET_SLIDES_L2)) {
            robot.desiredSlidesState = Robot.SlidesState.L2;
        }
        if (getButtonRelease(GamepadWrapper.DriverAction.SET_SLIDES_L3)) {
            robot.desiredSlidesState = Robot.SlidesState.L3;
        }
        if (getButtonRelease(GamepadWrapper.DriverAction.SET_SLIDES_CAPPING)) {
            robot.desiredSlidesState = Robot.SlidesState.CAPPING;
        }

        // Fine movement/rotation.
        if (getButtonRelease(GamepadWrapper.DriverAction.CHANGE_MOVEMENT_MODE)) {
            robot.fineMovement = !robot.fineMovement;
        }
        if (getButtonRelease(GamepadWrapper.DriverAction.CHANGE_ROTATION_MODE)) {
            robot.fineRotation = !robot.fineRotation;
        }

        previousStateGamepads.copyGamepads(gamepads);
    }

    /** Calls all non-blocking FSM methods to read from state and act accordingly.
     */
    public void driveMechanisms() {
        mechanismDriving.updateCarousel(robot);
        mechanismDriving.updateClaw(robot);
        mechanismDriving.updateSlides(robot);
    }

    /** Changes drivetrain motor inputs based off the controller inputs.
     */
    public void maneuver() {
        boolean movedStraight = navigation.moveStraight(
                gamepads.getButtonState(GamepadWrapper.DriverAction.MOVE_STRAIGHT_FORWARD),
                gamepads.getButtonState(GamepadWrapper.DriverAction.MOVE_STRAIGHT_BACKWARD),
                gamepads.getButtonState(GamepadWrapper.DriverAction.MOVE_STRAIGHT_LEFT),
                gamepads.getButtonState(GamepadWrapper.DriverAction.MOVE_STRAIGHT_RIGHT),
                robot
        );
        if (!movedStraight) {
            navigation.maneuver(gamepads.getJoystickValues(), robot);
        }
    }

    /** Determines whether the button for a particular action was released in the current OpMode iteration.
     */
    private boolean getButtonRelease(GamepadWrapper.DriverAction action) {
        return !gamepads.getButtonState(action) && previousStateGamepads.getButtonState(action);
    }

    // AUTONOMOUS
    // ==========

    /** Moves the robot to the next point of interest.
     */
    public void travelToNextPOI() {}

    /** Determines the position of the capstone on the barcode.
     */
    public Robot.SlidesState readBarcode() {
        robot.barcodeScanResult = -1;
        robot.barcodeScanState = Robot.BarcodeScanState.SCAN;
        robot.numBarcodeAttempts = 0;

        while (robot.barcodeScanState == Robot.BarcodeScanState.SCAN) {
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            }
            catch (InterruptedException e) {}
        }

        Robot.SlidesState slidesState = Robot.SlidesState.L1;
        switch (robot.barcodeScanResult) {
            case 1:
                slidesState = Robot.SlidesState.L1;
                break;
            case 2:
                slidesState = Robot.SlidesState.L2;
                break;
            case 3:
                slidesState = Robot.SlidesState.L3;
                break;
        }
        return slidesState;
    }


    // Each of these methods manually sets the robot state so that a specific task is started, and forces these tasks to
    // be synchronous by repeatedly calling the mechanism driving methods. These are to be used in an autonomous OpMode.

    /** Delivers a duck by spinning the carousel.
     */
    public void deliverDuck() {
        robot.desiredCarouselState = Robot.CarouselState.SPINNING;
        mechanismDriving.updateCarousel(robot);
        double startingTime = robot.elapsedTime.milliseconds();
        // Sleep for MechanismDriving.DUCK_SPIN_TIME milliseconds.
        while (robot.elapsedTime.milliseconds() - startingTime < MechanismDriving.DUCK_SPIN_TIME) {}
        robot.desiredCarouselState = Robot.CarouselState.STOPPED;
        mechanismDriving.updateCarousel(robot);
    }

    /** Grabs a cube piece of freight using the claw.
     */
    public void openClaw() {
        robot.desiredClawState = Robot.ClawState.OPEN;
        mechanismDriving.updateClaw(robot);
        try {
            Thread.sleep(MechanismDriving.CLAW_SERVO_TIME);
        } catch (InterruptedException e) {}
    }


    /** Delivers a piece of freight to a particular level of the alliance shipping hub.
     *
     *  @param level the level to which the cargo needs to be delivered.
     */
    public void deliverToShippingHub(Robot.SlidesState level) {
        robot.desiredSlidesState = level;
        boolean extended = mechanismDriving.updateSlides(robot);
        while (!extended) {
            extended = mechanismDriving.updateSlides(robot);
        }
        robot.desiredClawState = Robot.ClawState.OPEN;
        double startingTime = robot.elapsedTime.milliseconds();
        while (robot.elapsedTime.milliseconds() - startingTime < MechanismDriving.CLAW_SERVO_TIME) {}
    }
}

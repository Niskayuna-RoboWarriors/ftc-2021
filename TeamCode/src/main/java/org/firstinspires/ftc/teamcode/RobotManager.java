/* Authors: Arin Khare, Kai Vernooy
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Gamepad;
import org.firstinspires.ftc.robotcore.external.Telemetry;


/** A completely encompassing class of all functionality of the robot. An OpMode should interface through an instance of
 *  this class in order to send or receive any data with the real robot.
 */
public class RobotManager {

    public Robot robot;

    public MechanismDriving mechanismDriving;
    public Navigation navigation;

    private GamepadWrapper gamepads, previousStateGamepads;

    private Telemetry telemetry;

    public RobotManager(HardwareMap hardwareMap, Gamepad gamepad1, Gamepad gamepad2,
                        Navigation.NavigationMode navMode, Navigation.AllianceColor allianceColor,
                        Telemetry telemetry) {

        navigation = new Navigation(navMode, allianceColor);
        mechanismDriving = new MechanismDriving();

        robot = new Robot(hardwareMap);

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
        if (getButtonRelease(GamepadWrapper.DriverAction.SET_CLAW_CUBE)) {
            robot.desiredClawState = Robot.ClawState.CUBE;
        }
        if (getButtonRelease(GamepadWrapper.DriverAction.SET_CLAW_SPHERE)) {
            robot.desiredClawState = Robot.ClawState.SPHERE;
        }
        if (getButtonRelease(GamepadWrapper.DriverAction.OPEN_CLAW)) {
            robot.desiredClawState = Robot.ClawState.OPEN;
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
    public void maneuver() {}

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
     *  @return 0 indicates the position closest to the hub, 1 indicates the middle position, 2 indicates the position
     *          farthest from the hub.
     *
     *  TODO: figure out a CV system that lets us implement this as a synchronous method; or, figure out a better system.
     */
    public int readBarcode() { return 0; }

    // Each of these methods manually sets the robot state so that a specific task is started, and forces these tasks to
    // be synchronous by repeatedly calling the mechanism driving methods. These are to be used in an autonomous OpMode.

    /** Delivers a duck by spinning the carousel.
     */
    public void deliverDuck() {
        robot.desiredCarouselState = Robot.CarouselState.SPINNING;
        mechanismDriving.updateCarousel(robot);
        try {
            Thread.sleep(MechanismDriving.DUCK_SPIN_TIME);
        } catch (InterruptedException e) {}
        robot.desiredCarouselState = Robot.CarouselState.STOPPED;
        mechanismDriving.updateCarousel(robot);
    }

    /** Grabs a cube piece of freight using the claw.
     */
    public void grabCube() {
        robot.desiredClawState = Robot.ClawState.CUBE;
        mechanismDriving.updateClaw(robot);
        try {
            Thread.sleep(MechanismDriving.CLAW_SERVO_TIME);
        } catch (InterruptedException e) {}
    }

    /** Grabs a sphere piece of freight using the claw.
     */
    public void grabSphere() {
        robot.desiredClawState = Robot.ClawState.SPHERE;
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
        try {
            Thread.sleep(MechanismDriving.CLAW_SERVO_TIME);
        } catch (InterruptedException e) {}
    }
}

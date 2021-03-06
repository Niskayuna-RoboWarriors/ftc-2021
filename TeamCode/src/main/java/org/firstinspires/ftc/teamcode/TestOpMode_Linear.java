 package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;


/**
 * This file contains an minimal example of a Linear "OpMode". An OpMode is a 'program' that runs in either
 * the autonomous or the teleop period of an FTC match. The names of OpModes appear on the menu
 * of the FTC Driver Station. When an selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic manual Drive Teleop for a two wheeled robot
 * It includes all the skeletal structure that all linear OpModes contain.
 */

@TeleOp(name="Basic: test Linear OpMode", group="Linear Opmode")
public class TestOpMode_Linear extends LinearOpMode {

    // Declare OpMode members.
    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor frontLeftDrive = null,frontRightDrive = null,backLeftDrive = null,backRightDrive = null;
    private double dPadPower=1;

    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone)
        frontLeftDrive = hardwareMap.get(DcMotor.class, "front_left");
        frontRightDrive = hardwareMap.get(DcMotor.class, "front_right");
        backLeftDrive = hardwareMap.get(DcMotor.class, "back_left");
        backRightDrive = hardwareMap.get(DcMotor.class, "back_right");

        // Most robots need the motor on one side to be reversed to drive forward
        // Reverse the motor that runs backwards when connected directly to the battery
        frontLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        frontRightDrive.setDirection(DcMotor.Direction.REVERSE);
        backLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        backRightDrive.setDirection(DcMotor.Direction.REVERSE);

        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        runtime.reset();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {

            // Setup a variable for each drive wheel to save power level for telemetry
            double g1StickLX,g1StickLY,g1StickLDirection;//gamepad 1 left stick position varibles
            double frontLeftPower = 0,frontRightPower = 0,backLeftPower = 0,BackRightPower = 0,generalPower=0;//wheel motor power vatibles

            // Choose to drive using either Tank Mode, or POV Mode
            // Comment out the method that's not used.  The default below is POV.

            // POV Mode uses left stick to go forward, and right stick to turn.
            // - This uses basic math to combine motions and is easier to drive straight.
            g1StickLY = -gamepad1.left_stick_y;
            g1StickLX  =  gamepad1.left_stick_x;

            g1StickLDirection=Math.atan2(g1StickLY,g1StickLX);//get the angle the left stick is at

            generalPower=Range.clip(Math.sqrt(Math.pow(g1StickLX,2)+Math.pow(g1StickLY,2)),0,1);
            if(generalPower<=0.05){//joystick dead zone
                generalPower=0;
            }

            //set the power for each wheel absed on the angle of the stick and how far the stick is from center
            frontLeftPower = Range.clip(Math.sin(g1StickLDirection)+Math.cos(g1StickLDirection),-1,1)*generalPower;
            frontRightPower = Range.clip(Math.sin(g1StickLDirection)-Math.cos(g1StickLDirection),-1,1)*generalPower;
            backLeftPower = Range.clip(Math.sin(g1StickLDirection)-Math.cos(g1StickLDirection),-1,1)*generalPower;
            BackRightPower= Range.clip(Math.sin(g1StickLDirection)+Math.cos(g1StickLDirection),-1,1)*generalPower;

            // conditionals to overwrite wheel powers if dpad buttons are pressed
            if(gamepad1.dpad_up){
                frontLeftPower  = dPadPower;
                frontRightPower = dPadPower;
                backLeftPower   = dPadPower;
                BackRightPower  = dPadPower;
            }
            if(gamepad1.dpad_down){
                frontLeftPower  = -dPadPower;
                frontRightPower = -dPadPower;
                backLeftPower   = -dPadPower;
                BackRightPower  = -dPadPower;
            }
            if(gamepad1.dpad_right){
                frontLeftPower  = dPadPower;
                frontRightPower = -dPadPower;
                backLeftPower   = -dPadPower;
                BackRightPower  = dPadPower;
            }
            if(gamepad1.dpad_left){
                frontLeftPower  = -dPadPower;
                frontRightPower = dPadPower;
                backLeftPower   = dPadPower;
                BackRightPower  = -dPadPower;
            }

            // Send calculated power to wheels
            frontLeftDrive.setPower(frontLeftPower);
            frontRightDrive.setPower(frontRightPower);
            backLeftDrive.setPower(backLeftPower);
            backRightDrive.setPower(BackRightPower);

            // Show the elapsed game time wheel powe,stick direction and stick postition.
            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("gamepad 1 left stick position",g1StickLDirection+" radians "+Math.toDegrees(g1StickLDirection)+" degrees");
            telemetry.addData("Motors", "left (%.2f), right (%.2f)", frontLeftPower ,frontRightPower ,backLeftPower ,BackRightPower,generalPower);
            telemetry.addData("joystick positions", g1StickLX+" "+g1StickLY);
            telemetry.update();
        }
    }
}

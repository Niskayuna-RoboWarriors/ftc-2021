package org.firstinspires.ftc.teamcode;


import com.qualcomm.robotcore.hardware.DcMotor;

/** Estimates the robot's position based on the encoders on the drivetrain's motors. This will require a baseline
 *  position to add onto.
 */
public class EncoderPositioning {
    /** Checks the robot's encoders to get an estimate of the distance and direction traveled.
     *  @return a position in the form of a vector from the origin that can be added to an existing measurement
     */
    public Position updateDeltaEstimate(Robot robot) {
        // Read encoder values and use them to create a Position that represents the robot's movement relative to the last time the encoders were read.
        // Reset encoder values to zero.
        // call submitEstimate

        // 280 encoder counts per revolution
        double ADEncCount = LEFT_FRONT_DRIVE_MOTOR.getCurrentPosition()+RIGHT_BACK_DRIVE_MOTOR.getCurrentPosition();
        double BCEncCount = LEFT_BACK_DRIVE_MOTOR.getCurrentPosition()+RIGHT_FRONT_DRIVE_MOTOR.getCurrentPosition();
        double ADangle = Math.acos((2*ADEncCount+Math.sqrt(8-4(ADEncCount*ADEncCount)))/4);
        double BCangle = Math.acos((2*BCEncCount+Math.sqrt(8-4(BCEncCount*BCEncCount)))/4);
        double SecondXcor = PositionManager.getXcor() + BCEncCount * Math.cos(BCangle);
        double SecondYcor = PositionManager.getYcor() + BCEncCount * Math.sin(BCangle);
        double ThirdXcor = SecondXcor - ADEncCount*Math.cos(ADangle);
        double ThirdYcor = SecondYcor + ADEncCount*Math.sin(ADangle);
        PositionManager.setXcor(ThirdXcor);
        PositionManager.setYcor(ThirdYcor);
        return null;
    }


    /** Updates the encoder's position estimate in the robot's PositionManager
     */
    private void submitEstimate(Robot robot, Position delta) {
        robot.position.updateEncoderPosition(delta);
    }
    
    
    /** Resets the encoder values to zero.
     */
    private void resetEncoders(Robot robot) {}
}

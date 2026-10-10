// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.revrobotics.spark.SparkLowLevel;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkLowLevel.PeriodicFrame;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.PS4Controller.Axis;
import edu.wpi.first.wpilibj.motorcontrol.Spark;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.StringLogEntry;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;

/**
 * The methods in this class are called automatically corresponding to each mode, as described in
 * the TimedRobot documentation. If you change the name of this class or the package after creating
 * this project, you must also update the Main.java file in the project.
 */
public class Robot extends TimedRobot {
  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  public Robot() {}

  //원본 백업

  private SparkMax leftMotor1 = new SparkMax(4, MotorType.kBrushed);
  private SparkMax leftMotor2 = new SparkMax(3,  MotorType.kBrushed);
  private SparkMax rightMotor1 = new SparkMax(2, MotorType.kBrushed);
  private SparkMax rightMotor2 = new SparkMax(1,  MotorType.kBrushed);

  private Joystick joystick1 = new Joystick(0);

  private double leftY = 0;
  private double leftX = 0;
  private double rightY = 0;

  private double elevatorPower = 0;
  private double handPower = 0;

  private double speed = 0;
  private double turn = 0;
  private double leftPower = 0;
  private double rightPower = 0;


  @Override
  public void robotPeriodic() {}

  @Override
  public void autonomousInit() {}

  @Override
  public void autonomousPeriodic() {
  }

  @Override
  public void teleopInit() {
  }
  @Override
  public void teleopPeriodic() {

    leftY = joystick1.getRawAxis(1);
    // double rightX = joystick1.getRawAxis(4);
    leftX = joystick1.getRawAxis(0);
    rightY = joystick1.getRawAxis(5);

    speed = -leftY * 0.15;
    turn = leftX * 0.15;
    leftPower = speed + turn;
    rightPower = speed - turn;
    
    leftMotor1.set(-leftPower);
    leftMotor2.set(-leftPower);
    rightMotor1.set(rightPower);
    rightMotor2.set(rightPower);
  }

  @Override 
  public void disabledInit() {}

  @Override
  public void disabledPeriodic() {}

  @Override
  public void testInit() {}

  @Override
  public void testPeriodic() {}

  @Override
  public void simulationInit() {}

  @Override
  public void simulationPeriodic() {}
  
}
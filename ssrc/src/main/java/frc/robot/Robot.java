package frc.robot;

import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;

public class Robot extends TimedRobot {

  // 구동 모터 CAN ID 1~4
  private final SparkMax leftMotor1 =
      new SparkMax(4, MotorType.kBrushed);
  private final SparkMax leftMotor2 =
      new SparkMax(3, MotorType.kBrushed);
  private final SparkMax rightMotor1 =
      new SparkMax(2, MotorType.kBrushed);
  private final SparkMax rightMotor2 =
      new SparkMax(1, MotorType.kBrushed);

  // 슈터 모터 CAN ID 5~6
  private final SparkMax shooterMotor1 =
      new SparkMax(5, MotorType.kBrushless);
  private final SparkMax shooterMotor2 =
      new SparkMax(6, MotorType.kBrushless);

  // 조이스틱 포트 0
  private final Joystick joystick1 = new Joystick(0);

  // 주행 출력
  private double speed;
  private double turn;
  private double leftPower;
  private double rightPower;

  // 슈터 속도: 0.0 ~ 1.0
  private static final double SHOOTER_SPEED = 0.30;

  @Override
  public void robotInit() {
    shooterMotor1.set(0);
    shooterMotor2.set(0);
  }

  @Override
  public void autonomousInit() {
    stopAllMotors();
  }

  @Override
  public void autonomousPeriodic() {
    // 자율주행 코드는 추후 추가
    stopAllMotors();
  }

  @Override
  public void teleopInit() {
    stopAllMotors();
  }

  @Override
  public void teleopPeriodic() {

    // 기존 주행 조작
    double leftY = joystick1.getRawAxis(1);
    double leftX = joystick1.getRawAxis(0);

    speed = -leftY * 0.15;
    turn = leftX * 0.15;

    leftPower = speed + turn;
    rightPower = speed - turn;

    // 모터 출력 제한
    leftPower = Math.max(-1.0, Math.min(1.0, leftPower));
    rightPower = Math.max(-1.0, Math.min(1.0, rightPower));

    leftMotor1.set(-leftPower);
    leftMotor2.set(-leftPower);
    rightMotor1.set(rightPower);
    rightMotor2.set(rightPower);

    // 조이스틱 버튼 1을 누르면 슈터 작동
    if (joystick1.getRawButton(1)) {
      shooterMotor1.set(SHOOTER_SPEED);
      shooterMotor2.set(SHOOTER_SPEED);
    } else {
      shooterMotor1.set(0);
      shooterMotor2.set(0);
    }
  }

  private void stopAllMotors() {
    leftMotor1.set(0);
    leftMotor2.set(0);
    rightMotor1.set(0);
    rightMotor2.set(0);
    shooterMotor1.set(0);
    shooterMotor2.set(0);
  }

  @Override
  public void disabledInit() {
    stopAllMotors();
  }

  @Override
  public void testInit() {
    stopAllMotors();
  }
}

package frc.robot;

import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;

public class Robot extends TimedRobot {

    // =========================
    // MOTOR
    // =========================

    // 1~4 : 구동
    private final SparkMax leftMotor1  = new SparkMax(4, MotorType.kBrushed);
    private final SparkMax leftMotor2  = new SparkMax(3, MotorType.kBrushed);
    private final SparkMax rightMotor1 = new SparkMax(2, MotorType.kBrushed);
    private final SparkMax rightMotor2 = new SparkMax(1, MotorType.kBrushed);

    // 5~6 : 슈터
    private final SparkMax shooterMotor1 = new SparkMax(5, MotorType.kBrushed);
    private final SparkMax shooterMotor2 = new SparkMax(6, MotorType.kBrushed);

    private final Joystick joystick1 = new Joystick(0);

    // =========================
    // LIMELIGHT
    // =========================

    private final NetworkTable limelight =
            NetworkTableInstance.getDefault().getTable("limelight");

    // 파이프라인
    // 0 = 노란 게임피스
    // 1 = 우리 골/득점 구조물
    private static final int BALL_PIPELINE = 0;
    private static final int GOAL_PIPELINE = 1;

    // =========================
    // 설정값
    // =========================

    private static final double AUTO_DRIVE_SPEED = 0.28;
    private static final double AUTO_TURN_SPEED = 0.22;

    // 게임피스가 화면 가운데에 들어왔다고 보는 오차
    private static final double BALL_AIM_ERROR = 3.0;

    // 게임피스가 충분히 가까워졌다고 보는 면적
    // 실제 로봇에서 반드시 조정해야 함.
    private static final double BALL_CLOSE_AREA = 7.0;

    // 골 앞에서 슈터를 작동시키기 시작하는 목표 면적
    private static final double GOAL_CLOSE_AREA = 8.0;

    private static final double SHOOTER_POWER = 0.75;

    // 자율 타이머
    private final Timer autoTimer = new Timer();

    // 자율 단계
    private enum AutoState {
        FIND_BALL,
        COLLECT_BALL,
        FIND_GOAL,
        DRIVE_TO_GOAL,
        SHOOT,
        DONE
    }

    private AutoState autoState = AutoState.FIND_BALL;

    // =========================
    // 공통 모터 제어
    // =========================

    private void drive(double left, double right) {
        leftMotor1.set(-left);
        leftMotor2.set(-left);

        rightMotor1.set(right);
        rightMotor2.set(right);
    }

    private void stopDrive() {
        drive(0, 0);
    }

    private void stopShooter() {
        shooterMotor1.set(0);
        shooterMotor2.set(0);
    }

    // 두 슈터가 서로 반대 방향으로 돌아가는 구조라고 가정
    private void runShooter(double power) {
        shooterMotor1.set(power);
        shooterMotor2.set(-power);
    }

    // =========================
    // LIMELIGHT 값
    // =========================

    private boolean hasTarget() {
        return limelight.getEntry("tv").getDouble(0) > 0.5;
    }

    private double getTx() {
        return limelight.getEntry("tx").getDouble(0);
    }

    private double getTa() {
        return limelight.getEntry("ta").getDouble(0);
    }

    private void setPipeline(int pipeline) {
        limelight.getEntry("pipeline").setNumber(pipeline);
    }

    // =========================
    // 시작
    // =========================

    @Override
    public void robotInit() {
        stopDrive();
        stopShooter();
    }

    // =========================
    // AUTONOMOUS
    // =========================

    @Override
    public void autonomousInit() {
        autoTimer.restart();

        autoState = AutoState.FIND_BALL;
        setPipeline(BALL_PIPELINE);

        stopDrive();
        stopShooter();
    }

    @Override
    public void autonomousPeriodic() {

        switch (autoState) {

            // -------------------------
            // 1. 노란 공 찾기
            // -------------------------
            case FIND_BALL:

                setPipeline(BALL_PIPELINE);

                if (!hasTarget()) {
                    // 공이 안 보이면 천천히 제자리 회전
                    drive(AUTO_TURN_SPEED, AUTO_TURN_SPEED);
                    return;
                }

                autoState = AutoState.COLLECT_BALL;
                break;

            // -------------------------
            // 2. 공 방향으로 이동
            // -------------------------
            case COLLECT_BALL: {

                double tx = getTx();
                double ta = getTa();

                if (!hasTarget()) {
                    autoState = AutoState.FIND_BALL;
                    return;
                }

                // 공이 충분히 가까우면 획득 완료로 간주
                if (ta >= BALL_CLOSE_AREA) {
                    stopDrive();

                    // 잠깐 멈춘 뒤 골 탐색 단계로
                    if (autoTimer.get() > 1.0) {
                        autoState = AutoState.FIND_GOAL;
                        setPipeline(GOAL_PIPELINE);
                    }
                    return;
                }

                // 공이 왼쪽이면 좌회전
                if (tx < -BALL_AIM_ERROR) {
                    drive(-AUTO_TURN_SPEED, AUTO_TURN_SPEED);
                }

                // 공이 오른쪽이면 우회전
                else if (tx > BALL_AIM_ERROR) {
                    drive(AUTO_TURN_SPEED, -AUTO_TURN_SPEED);
                }

                // 가운데면 직진
                else {
                    drive(AUTO_DRIVE_SPEED, AUTO_DRIVE_SPEED);
                }

                break;
            }

            // -------------------------
            // 3. 우리 골 찾기
            // -------------------------
            case FIND_GOAL:

                setPipeline(GOAL_PIPELINE);

                if (!hasTarget()) {
                    // 골을 못 찾으면 천천히 회전
                    drive(AUTO_TURN_SPEED, AUTO_TURN_SPEED);
                    return;
                }

                autoState = AutoState.DRIVE_TO_GOAL;
                break;

            // -------------------------
            // 4. 골 방향으로 이동
            // -------------------------
            case DRIVE_TO_GOAL: {

                double tx = getTx();
                double ta = getTa();

                if (!hasTarget()) {
                    autoState = AutoState.FIND_GOAL;
                    return;
                }

                // 골에 충분히 가까워짐
                if (ta >= GOAL_CLOSE_AREA) {
                    stopDrive();
                    autoState = AutoState.SHOOT;
                    autoTimer.restart();
                    return;
                }

                if (tx < -BALL_AIM_ERROR) {
                    drive(-AUTO_TURN_SPEED, AUTO_TURN_SPEED);
                }

                else if (tx > BALL_AIM_ERROR) {
                    drive(AUTO_TURN_SPEED, -AUTO_TURN_SPEED);
                }

                else {
                    drive(AUTO_DRIVE_SPEED, AUTO_DRIVE_SPEED);
                }

                break;
            }

            // -------------------------
            // 5. 슈팅
            // -------------------------
            case SHOOT:

                stopDrive();
                runShooter(SHOOTER_POWER);

                // 1.5초 동안 슈터 작동
                if (autoTimer.get() >= 1.5) {
                    stopShooter();
                    autoState = AutoState.DONE;
                }

                break;

            // -------------------------
            // 6. 종료
            // -------------------------
            case DONE:
                stopDrive();
                stopShooter();
                break;
        }
    }

    // =========================
    // TELEOP
    // =========================

    @Override
    public void teleopInit() {
        stopDrive();
        stopShooter();
    }

    @Override
    public void teleopPeriodic() {

        // 기존 조종
        double leftY = joystick1.getRawAxis(1);
        double leftX = joystick1.getRawAxis(0);

        double speed = -leftY * 0.15;
        double turn = leftX * 0.15;

        double leftPower = speed + turn;
        double rightPower = speed - turn;

        drive(leftPower, rightPower);

        // 버튼 1을 누르는 동안 슈터 작동
        if (joystick1.getRawButton(1)) {
            runShooter(SHOOTER_POWER);
        } else {
            stopShooter();
        }
    }

    // =========================
    // DISABLED / TEST
    // =========================

    @Override
    public void disabledInit() {
        stopDrive();
        stopShooter();
    }

    @Override
    public void testInit() {
        stopDrive();
        stopShooter();
    }
}

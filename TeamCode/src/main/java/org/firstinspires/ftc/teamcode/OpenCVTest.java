package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvPipeline;

@Autonomous
public class OpenCVTest extends LinearOpMode {
    private final ElapsedTime runtime = new ElapsedTime();
    private int renderMode = 0;
    private Gamepad prevGamepad1 = new Gamepad();

    OpenCvCamera webcam;

    @Override
    public void runOpMode() {
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        webcam = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam1"), cameraMonitorViewId);
        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
        {
            @Override
            public void onOpened()
            {
                webcam.setViewportRenderer(OpenCvCamera.ViewportRenderer.GPU_ACCELERATED);
                webcam.startStreaming(640, 480);
                webcam.setPipeline(new ChannelSwitchingPipeline());

                waitForStart();

                while (opModeIsActive()) {

                    if (gamepad1.left_bumper && !prevGamepad1.left_bumper) {
                        renderMode--;
                    }
                    if (gamepad1.right_bumper && !prevGamepad1.right_bumper) {
                        renderMode++;
                    }

                    if (renderMode < 0) {
                        renderMode = 6;
                    }
                    else if (renderMode > 6) {
                        renderMode = 0;
                    }

                    try { prevGamepad1.copy(gamepad1); } catch (RobotCoreException ignored) {}
                    telemetry.addData("Time:", runtime);
                    telemetry.addData("Render mode:", renderMode);
                    telemetry.update();
                    sleep(20);
                }
            }
            @Override
            public void onError(int errorCode)
            {
                telemetry.addData("Error opening camera. Code:", errorCode);
                telemetry.update();
            }
        });
    }

    class ChannelSwitchingPipeline extends OpenCvPipeline {
        Mat redMat = new Mat();
        Mat greenMat = new Mat();
        Mat blueMat = new Mat();
        Mat redThreshMat = new Mat();
        Mat greenThreshMat = new Mat();
        Mat blueThreshMat = new Mat();

        @Override
        public Mat processFrame(Mat input) {
            Core.extractChannel(input, redMat, 0);
            Core.extractChannel(input, greenMat, 1);
            Core.extractChannel(input, blueMat, 2);

            Imgproc.threshold(redMat, redThreshMat, 100, 255, Imgproc.THRESH_BINARY_INV);
            Imgproc.threshold(greenMat, greenThreshMat, 100, 255, Imgproc.THRESH_BINARY_INV);
            Imgproc.threshold(blueMat, blueThreshMat, 100, 255, Imgproc.THRESH_BINARY_INV);

            switch (renderMode) {
                case 1: { return redMat; }
                case 2: { return greenMat; }
                case 3: { return blueMat; }
                case 4: { return redThreshMat; }
                case 5: { return greenThreshMat; }
                case 6: { return blueThreshMat; }
                default: { return input; }
            }
        }
    }
}

package gq.icctv.icctv;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

public class CameraView implements SurfaceHolder.Callback {

    private static int HEIGHT = 640;
    private static int WIDTH = 480;
    private static double FPS = 25.0;
    private static int buffersCount = 5;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera = null;
    private Camera.Size cameraSize;
    private List<int[]> cameraSupportedFps;
    private List<Camera.Size> cameraSupportedSizes;
    private StreamingEncoder streamingEncoder;

    public CameraView (SurfaceView s) {
        surfaceView = s;
        surfaceHolder = s.getHolder();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        streamingEncoder = new StreamingEncoder(s);
        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();
        cameraSize = camera.new Size(0, 0);
        Camera.Parameters cameraParameters = camera.getParameters();
        cameraSupportedFps = cameraParameters.getSupportedPreviewFpsRange();
        cameraSupportedSizes = cameraParameters.getSupportedPreviewSizes();
        cameraSize = cameraSupportedSizes.get(cameraSupportedSizes.size() / 2);
        cameraParameters.setPreviewSize(cameraSize.width, cameraSize.height);
        camera.setParameters(cameraParameters);

        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (Exception e) {
            e.printStackTrace();
        }

        camera.setPreviewCallbackWithBuffer(null);
        camera.startPreview();
        camera.stopPreview();
        setupCamera();
        // nativeInitMediaEncoder(cameraView.Width(), cameraView.Height());
        camera.startPreview();
    }

    private void setupCamera() {
        int targetSizeIndex = getClosestSupportedSizeIndex(WIDTH, HEIGHT);
        cameraSize.width = cameraSupportedSizes.get(targetSizeIndex).width;
        cameraSize.height = cameraSupportedSizes.get(targetSizeIndex).height;

        int targetFpsIndex = getClosestSupportedFpsIndex(FPS);
        int targetMaxFrameRate = cameraSupportedFps.get(targetFpsIndex)[0];
        int targetMinFrameRate = cameraSupportedFps.get(targetFpsIndex)[1];

        Camera.Parameters cameraParameters = camera.getParameters();
        cameraParameters.setPreviewSize(cameraSize.width, cameraSize.height);
        cameraParameters.setPreviewFormat(ImageFormat.NV21);
        cameraParameters.setPreviewFpsRange(targetMaxFrameRate, targetMinFrameRate);
        camera.setParameters(cameraParameters);

        // Allocate preview frame buffers
        PixelFormat pixelFormat = new PixelFormat();
        PixelFormat.getPixelFormatInfo(ImageFormat.NV21, pixelFormat);
        int bufferSize = cameraSize.width * cameraSize.height * pixelFormat.bitsPerPixel / 8;
        byte[] buffer = null;
        for(int i = 0; i < buffersCount; i++) {
            buffer = new byte[bufferSize];
            camera.addCallbackBuffer(buffer);
        }

        camera.setPreviewCallbackWithBuffer(streamingEncoder);
    }

    private int getClosestSupportedSizeIndex(int w, int h) {
        double diff = Math.abs(cameraSupportedSizes.get(0).width * cameraSupportedSizes.get(0).height - w * h);
        int targetIndex = 0;
        for(int i = 1; i < cameraSupportedSizes.size(); i++) {
            double newDiff =  Math.abs(cameraSupportedSizes.get(i).width * cameraSupportedSizes.get(i).height - w * h);
            if ( newDiff < diff) {
                diff = newDiff;
                targetIndex = i;
            }
        }
        return targetIndex;
    }

    private int getClosestSupportedFpsIndex(double fps) {
        double diff = Math.abs(cameraSupportedFps.get(0)[0] * cameraSupportedFps.get(0)[1] - fps * fps * 1000 * 1000);
        int targetIndex = 0;
        for(int i = 1; i < cameraSupportedFps.size(); i++) {
            double newDiff = Math.abs(cameraSupportedFps.get(i)[0] * cameraSupportedFps.get(i)[1] - fps * fps * 1000 * 1000);
            if ( newDiff < diff) {
                diff = newDiff;
                targetIndex = i;
            }
        }
        return targetIndex;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}

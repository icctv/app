package gq.icctv.icctv;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

public class CameraView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraView";
    private static int WIDTH = 1280;
    private static int HEIGHT = 720;
    private static double FPS = 30.0;
    private static int buffersCount = 1;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera = null;
    private Camera.Size cameraSize;
    private List<int[]> cameraSupportedFps;
    private List<Camera.Size> cameraSupportedSizes;
    private StreamingEncoder streamingEncoder;
    private Sender sender;

    public CameraView (SurfaceView s, Sender sn) {
        surfaceView = s;
        sender = sn;
        surfaceHolder = s.getHolder();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
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
        camera.startPreview();
    }

    private void setupCamera() {
        Log.i(TAG, "Supported features: " + camera.getParameters().flatten());

        int targetSizeIndex = getClosestSupportedSizeIndex(WIDTH, HEIGHT);
        cameraSize.width = cameraSupportedSizes.get(targetSizeIndex).width;
        cameraSize.height = cameraSupportedSizes.get(targetSizeIndex).height;

        int targetFpsIndex = getClosestSupportedFpsIndex(FPS);
        int targetMinFps = cameraSupportedFps.get(targetFpsIndex)[0];
        int targetMaxFps = cameraSupportedFps.get(targetFpsIndex)[1];

        Log.i(TAG, "Setting up camera w=" + cameraSize.width + " h=" + cameraSize.height + " fpsRange=" + targetMinFps / 1000 + "-" + targetMaxFps / 1000);

        Camera.Parameters cameraParameters = camera.getParameters();
        cameraParameters.setPreviewSize(cameraSize.width, cameraSize.height);
        cameraParameters.setPreviewFormat(ImageFormat.NV21);
        cameraParameters.setPreviewFpsRange(targetMinFps, targetMaxFps);
        camera.setParameters(cameraParameters);

        // Allocate preview frame buffers
        PixelFormat pixelFormat = new PixelFormat();
        PixelFormat.getPixelFormatInfo(ImageFormat.NV21, pixelFormat);
        int bufferSize = cameraSize.width * cameraSize.height * pixelFormat.bitsPerPixel / 8;

        Log.i(TAG, "Pixel format NV21 bitsPerPixel=" + pixelFormat.bitsPerPixel + " bufferSize=" + bufferSize);
        Log.i(TAG, "Allocating " + buffersCount + " preview buffers");
        for(int i = 0; i < buffersCount; i++) {
            byte[] buffer = new byte[bufferSize];
            camera.addCallbackBuffer(buffer);
        }
        Log.i(TAG, "Allocated " + buffersCount + " preview buffers");

        streamingEncoder = new StreamingEncoder(surfaceView, sender);
        camera.setPreviewCallbackWithBuffer(streamingEncoder);
    }

    private int getClosestSupportedSizeIndex(int w, int h) {
        int candidatesSize = cameraSupportedSizes.size();
        int targetIndex = 0;

        int candidateWidth = cameraSupportedSizes.get(0).width;
        int candidateHeight = cameraSupportedSizes.get(0).height;

        double diff = Math.abs(candidateWidth * candidateHeight - w * h);
        Log.i(TAG, "Size candidate 1/" + candidatesSize + ": " + candidateWidth + "x" + candidateHeight);

        for(int i = 1; i < candidatesSize; i++) {
            candidateWidth = cameraSupportedSizes.get(i).width;
            candidateHeight = cameraSupportedSizes.get(i).height;

            Log.i(TAG, "Size candidate " + (i + 1) + "/" + candidatesSize + ": " + candidateWidth + "x" + candidateHeight);
            double newDiff =  Math.abs(candidateWidth * candidateWidth - w * h);
            if (newDiff < diff) {
                diff = newDiff;
                targetIndex = i;
            }
        }
        Log.i(TAG, "Picked size candidate " + (targetIndex + 1) +
                " (" + cameraSupportedSizes.get(targetIndex).width + "x" + cameraSupportedSizes.get(targetIndex).height + ") " +
                "because it is closest to requested size " + WIDTH + "x" + HEIGHT);
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
        if (streamingEncoder != null) {
            streamingEncoder.release();
        }
    }
}

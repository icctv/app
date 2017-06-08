package gq.icctv.icctv;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

public class CameraView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraView";
    private static int WIDTH = 176;
    private static int HEIGHT = 144;
    private static double FPS = 30.0;
    private static int buffersCount = 1;

    private SurfaceView cameraPreview;
    private SurfaceHolder surfaceHolder;
    private Camera camera = null;
    private Camera.Size cameraSize;
    private List<int[]> cameraSupportedFps;
    private List<Camera.Size> cameraSupportedSizes;
    private StreamingEncoder streamingEncoder;

    public CameraView (SurfaceView cameraPreview) {
        this.cameraPreview = cameraPreview;
        start();
    }

    private void start() {
        initializeSurface();
        startCamera();
    }

    private void initializeSurface() {
        surfaceHolder = cameraPreview.getHolder();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        cameraPreview.setLayoutParams(new LinearLayout.LayoutParams(WIDTH, HEIGHT));
        surfaceHolder.setFixedSize(WIDTH, HEIGHT);
        surfaceHolder.addCallback(this);
    }

    private void startCamera() {
        Log.i(TAG, "Starting camera");
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

        Log.i(TAG, "Starting camera preview");
        camera.startPreview();
    }

    private void setupCamera() {
        Log.i(TAG, "Setting up camera");
        Log.d(TAG, "Supported features: " + camera.getParameters().flatten());

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

        streamingEncoder = new StreamingEncoder(cameraPreview);
        if (streamingEncoder.initialize()) {
            camera.setPreviewCallbackWithBuffer(streamingEncoder);
        }
    }

    public void release() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        if (surfaceHolder != null) {
            surfaceHolder.removeCallback(this);
            surfaceHolder = null;
        }
        if (cameraPreview != null) {
            cameraPreview = null;
        }
        if (streamingEncoder != null) {
            streamingEncoder.release();
            streamingEncoder = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "Surface created");
        startCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "Surface changed: " + width + "x" + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "Surface destroyed");
        release();
    }

    private int getClosestSupportedSizeIndex(int w, int h) {
        int candidatesSize = cameraSupportedSizes.size();
        int targetIndex = 0;

        int candidateWidth = cameraSupportedSizes.get(0).width;
        int candidateHeight = cameraSupportedSizes.get(0).height;

        double diff = Math.abs(candidateWidth * candidateHeight - w * h);
        Log.d(TAG, "Size candidate 1/" + candidatesSize + ": " + candidateWidth + "x" + candidateHeight);

        for(int i = 1; i < candidatesSize; i++) {
            candidateWidth = cameraSupportedSizes.get(i).width;
            candidateHeight = cameraSupportedSizes.get(i).height;

            Log.d(TAG, "Size candidate " + (i + 1) + "/" + candidatesSize + ": " + candidateWidth + "x" + candidateHeight);
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
}

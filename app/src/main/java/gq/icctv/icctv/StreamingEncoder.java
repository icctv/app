package gq.icctv.icctv;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class StreamingEncoder implements Camera.PreviewCallback {

    private static final String TAG = "StreamingEncoder";

    private SurfaceView surfaceView;
    private int surfaceHeight = 0;
    private int surfaceWidth = 0;
    int bufferSize = 0;
    byte[] pixelsBuffer = null;
    ExecutorService threadPool;
    private ReentrantLock reentrantLock = new ReentrantLock();
    private boolean busy = false;

    public StreamingEncoder (SurfaceView s, int bs) {
        bufferSize = bs;
        surfaceView = s;
        threadPool = Executors.newFixedThreadPool(3);

        surfaceWidth = surfaceView.getWidth();
        surfaceHeight = surfaceView.getHeight();

        if (surfaceWidth == 0) {
            return;
        }

        Log.i(TAG, "Initializing encoder for preview surface w=" + surfaceWidth + " h=" + surfaceHeight);

        int outWidth = surfaceWidth;
        int outHeight = surfaceHeight;
        int bitrate = outWidth * 3500; // Estimate

        pixelsBuffer = new byte[bufferSize];

        nativeInitialize(surfaceWidth, surfaceHeight, outWidth, outHeight, bitrate);
    }

    public void release() {
        nativeRelease();
    }

    private native int nativeInitialize(int inWidth, int inHeight, int outWidth, int outHeight, int bitrate);
    public native void onPreviewFrame(byte[] pixels, Camera camera);
    private native void nativeRelease();
    private native String getConfiguration();

    static {
        System.loadLibrary("encoder");
    }
}

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
    StreamingEncoderTask streamingEncoderTask;
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
        streamingEncoderTask = new StreamingEncoderTask();

        nativeInitialize(surfaceWidth, surfaceHeight, outWidth, outHeight, bitrate);
    }


    public void onPreviewFrame(byte[] pixels, Camera camera) {
        reentrantLock.lock();
        encode(pixels);
        camera.addCallbackBuffer(pixels);
        reentrantLock.unlock();
    }

    private void encode(byte[] pixels) {
        // Skip this frame if we're still busy encoding the last one
        if (busy) {
            Log.i(TAG, "Skipped frame");
            return;
        } else {
            busy = true;
        }

        if (pixels.length != pixelsBuffer.length) {
            Log.e(TAG, "Buffer size mismatch, copying " + pixels.length + " pixels into buffer sized " + pixelsBuffer.length);
        }

        // This is fast (<<1ms), don't worry about bottleneck here
        System.arraycopy(pixels, 0, pixelsBuffer, 0, pixels.length);

        threadPool.execute(streamingEncoderTask);
    }

    private class StreamingEncoderTask implements Runnable {
        private static final String TAG = "StreamingEncoderTask";

        @Override
        public void run() {
            nativeEncode(pixelsBuffer);
            busy = false;
        }
    }

    public void release() {
        nativeRelease();
    }

    private native int nativeInitialize(int inWidth, int inHeight, int outWidth, int outHeight, int bitrate);
    private native int nativeEncode(byte[] rgb_pixels);
    private native void nativeRelease();
    private native String getConfiguration();

    static {
        System.loadLibrary("encoder");
    }
}

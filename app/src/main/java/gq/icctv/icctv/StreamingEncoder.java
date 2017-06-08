package gq.icctv.icctv;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceView;

public class StreamingEncoder implements Camera.PreviewCallback {

    private static final String TAG = "StreamingEncoder";
    private static final int FRAME_BUFFER_SIZE = (1024 * 1024);

    private SurfaceView surfaceView;
    private int surfaceHeight = 0;
    private int surfaceWidth = 0;

    public StreamingEncoder (SurfaceView s) {
        surfaceView = s;
    }

    public boolean initialize() {
        surfaceWidth = surfaceView.getWidth();
        surfaceHeight = surfaceView.getHeight();

        if (surfaceWidth == 0) {
            return false;
        }

        Log.i(TAG, "Initializing encoder for preview surface w=" + surfaceWidth + " h=" + surfaceHeight);

        int outWidth = surfaceWidth;
        int outHeight = surfaceHeight;
        int bitrate = outWidth * 3500; // Estimate

        int ok = nativeInitialize(surfaceWidth, surfaceHeight, outWidth, outHeight, bitrate, FRAME_BUFFER_SIZE);
        if (ok != 1) {
            Log.e(TAG, "Failed to initialize native encoder, error code was: " + ok);
            return false;
        }

        return true;
    }

    // This is a callback method that is invoked by native code
    public void onEncodedFrame(byte[] frame) {
        // noop
    }

    public void release() {
        nativeRelease();
    }

    private native int nativeInitialize(int inWidth, int inHeight, int outWidth, int outHeight, int bitrate, int frameBufferSize);
    public native void onPreviewFrame(byte[] pixels, Camera camera);
    private native void nativeRelease();

    static {
        System.loadLibrary("encoder");
    }
}

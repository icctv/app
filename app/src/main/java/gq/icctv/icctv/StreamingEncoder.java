package gq.icctv.icctv;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceView;

public class StreamingEncoder implements Camera.PreviewCallback {

    private static final String TAG = "StreamingEncoder";

    private SurfaceView surfaceView;
    private int surfaceHeight = 0;
    private int surfaceWidth = 0;

    public StreamingEncoder (SurfaceView s) {
        surfaceView = s;

        surfaceWidth = surfaceView.getWidth();
        surfaceHeight = surfaceView.getHeight();

        if (surfaceWidth == 0) {
            return;
        }

        Log.i(TAG, "Initializing encoder for preview surface w=" + surfaceWidth + " h=" + surfaceHeight);

        int outWidth = surfaceWidth;
        int outHeight = surfaceHeight;
        int bitrate = outWidth * 3500; // Estimate

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

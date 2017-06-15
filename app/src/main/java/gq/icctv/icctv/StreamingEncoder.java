package gq.icctv.icctv;

import android.hardware.Camera;
import android.util.Log;

class StreamingEncoder implements Camera.PreviewCallback {

    private static final String TAG = "StreamingEncoder";
    private static final int FRAME_BUFFER_SIZE = (1024 * 1024);

    private int actualHeight = 0;
    private int actualWidth = 0;
    private IngestPoint ingestPoint;

    class InitializationException extends RuntimeException {}

    StreamingEncoder (int actualWidth, int actualHeight, IngestPoint ingestPoint) {
        this.actualWidth = actualWidth;
        this.actualHeight = actualHeight;
        this.ingestPoint = ingestPoint;
    }

    boolean initialize() throws InitializationException {
        Log.i(TAG, "Initializing encoder for preview surface w=" + actualWidth + " h=" + actualHeight + " streaming to " + ingestPoint.url);

        int outWidth = actualWidth;
        int outHeight = actualHeight;
        int bitrate = outWidth * 3500; // Estimate

        int ok = nativeInitialize(actualWidth, actualHeight, outWidth, outHeight, bitrate, FRAME_BUFFER_SIZE, ingestPoint.url);
        if (ok != 1) {
            Log.e(TAG, "Failed to initialize native encoder, error code was: " + ok);
            throw new InitializationException();
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

    private native int nativeInitialize(int inWidth, int inHeight, int outWidth, int outHeight, int bitrate, int frameBufferSize, String ingestUrl);
    public native void onPreviewFrame(byte[] pixels, Camera camera);
    private native void nativeRelease();

    static {
        System.loadLibrary("encoder");
    }
}

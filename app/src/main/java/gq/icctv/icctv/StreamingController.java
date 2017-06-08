package gq.icctv.icctv;

import android.content.Context;
import android.graphics.Camera;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.LinearLayout;

public class StreamingController implements NetworkController.Callback, CameraView.Callback {

    private final static String TAG = "StreamingController";

    StreamingStatusCallback statusCallback;
    CameraView cameraView;
    NetworkController networkController;
    SurfaceView cameraPreview;
    StreamingEncoder streamingEncoder;
    IngestPoint ingestPoint;

    StreamingController(StreamingStatusCallback ctx, SurfaceView cameraPreview) {
        this.statusCallback = ctx;
        this.cameraPreview = cameraPreview;
        this.networkController = new NetworkController((Context) ctx, this);

        statusCallback.onStatusChanged(StreamingStatus.INITIAL);
    }

    public void start() {
        Log.i(TAG, "Starting");
        statusCallback.onStatusChanged(StreamingStatus.HELLO);
        networkController.hello();
    }

    @Override
    public void onHello(NetworkController.HelloResponse hello) {
        Log.i(TAG, "onHello");
        statusCallback.onStatusChanged(StreamingStatus.TRYING);

        // TODO: Fallback to other ingest points
        ingestPoint = hello.in.get(0);

        // The width/height we received from the Relay is the resolution we'd like to use,
        // but the camera might not support these exact values. This is why we have to wait until
        // the onCameraReady callback where we get passed the actual supported closest resolution.
        cameraPreview.setLayoutParams(new LinearLayout.LayoutParams(ingestPoint.width, ingestPoint.height));
        cameraView = new CameraView(cameraPreview, ingestPoint.width, ingestPoint.height, this);
        new Thread(cameraView).start();
    }

    // Okay, the camera has been initialized and here it tells us the actual resolution
    // that it is going to use. The next step is to fire up our StreamingEncoder.
    @Override
    public void onCameraReady(int actualWidth, int actualHeight) {
        streamingEncoder = new StreamingEncoder(actualWidth, actualHeight, ingestPoint);
        streamingEncoder.initialize();

        cameraView.setPreviewCallback(streamingEncoder);
    }

    public void stop() {
        Log.i(TAG, "Stopping");
        statusCallback.onStatusChanged(StreamingStatus.STOPPING);

        if (cameraView != null) cameraView.interrupt();
        if (streamingEncoder != null) streamingEncoder.release();

        statusCallback.onStatusChanged(StreamingStatus.STOPPED);
    }

    public void debug() {
        Log.i(TAG, "Debug called");

        stop();
        start();
    }
}

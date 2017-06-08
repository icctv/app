package gq.icctv.icctv;

import android.content.Context;
import android.graphics.Camera;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.LinearLayout;

public class StreamingController implements NetworkControllerCallback {

    private final static String TAG = "StreamingController";

    StreamingStatusCallback statusCallback;
    CameraView cameraView;
    NetworkController networkController;
    SurfaceView cameraPreview;
    StreamingEncoder streamingEncoder;

    StreamingController(StreamingStatusCallback ctx, SurfaceView cameraPreview) {
        this.statusCallback = ctx;
        this.cameraPreview = cameraPreview;
        this.networkController = new NetworkController((Context) ctx, this);
    }

    public void start() {
        Log.i(TAG, "Starting");
        statusCallback.onStatusChanged(StreamingStatus.INITIAL);
        networkController.hello();
    }

    @Override
    public void onHello(NetworkController.HelloResponse hello) {
        // TODO: Fallback to other ingest points
        IngestPoint ingestPoint = hello.in.get(0);

        streamingEncoder = new StreamingEncoder(ingestPoint.width, ingestPoint.height);
        streamingEncoder.initialize();

        startCamera(ingestPoint, streamingEncoder);
    }

    public void stop() {
        Log.i(TAG, "Stopping");

        if (cameraView != null) cameraView.interrupt();
        if (streamingEncoder != null) streamingEncoder.release();
    }

    public void debug() {
        Log.i(TAG, "Debug called");

        stop();
        start();
    }

    private void startCamera(IngestPoint ingestPoint, StreamingEncoder streamingEncoder) {
        cameraPreview.setLayoutParams(new LinearLayout.LayoutParams(ingestPoint.width, ingestPoint.height));
        cameraView = new CameraView(cameraPreview, ingestPoint.width, ingestPoint.height, streamingEncoder);
        new Thread(cameraView).start();
    }
}

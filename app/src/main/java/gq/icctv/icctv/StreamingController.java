package gq.icctv.icctv;

import android.content.Context;
import android.graphics.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.LinearLayout;

public class StreamingController implements Runnable, NetworkController.Callback, CameraView.Callback {

    private final static String TAG = "StreamingController";

    private String uuid;
    private Callback statusCallback;
    private CameraView cameraView;
    private NetworkController networkController;
    private SurfaceView cameraPreview;
    private StreamingEncoder streamingEncoder;
    private IngestPoint ingestPoint;
    private Thread currentThread;
    private Handler mainHandler;

    public enum Status {
        INITIAL,
        HELLO,
        INITIALIZING_CAMERA,
        INITIALIZING_ENCODER,
        STREAMING,
        STOPPING,
        STOPPED
    }

    public interface Callback {
        void onStatusChanged(Status status);
    }

    StreamingController(String uuid, SurfaceView cameraPreview, Callback statusCallback) {
        this.uuid = uuid;
        this.statusCallback = statusCallback;
        this.cameraPreview = cameraPreview;
        this.networkController = new NetworkController(uuid, this);
        this.mainHandler = new Handler(((Context) statusCallback).getMainLooper());

        setStatus(Status.INITIAL);
    }

    private void setStatus(final Status newStatus) {
        Log.i(TAG, "Status changes to: " + newStatus);
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                statusCallback.onStatusChanged(newStatus);
            }
        });
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        currentThread = Thread.currentThread();

        start();

        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.i(TAG, "Thread was interrupted");
                break;
            }

            if (currentThread.isInterrupted()) {
                Log.i(TAG, "Thread was interrupted from outside");
                break;
            }
        }

        stop();
    }


    public void start() {
        Log.i(TAG, "Starting");
        setStatus(Status.HELLO);
        networkController.hello();
    }

    @Override
    public void onHello(NetworkController.HelloResponse hello) {
        Log.i(TAG, "onHello");
        setStatus(Status.INITIALIZING_CAMERA);

        // TODO: Fallback to other ingest points
        ingestPoint = hello.in.get(0);

        // The width/height we received from the Relay is the resolution we'd like to use,
        // but the camera might not support these exact values. This is why we have to wait until
        // the onCameraReady callback where we get passed the actual supported closest resolution.
        cameraPreview.setLayoutParams(new LinearLayout.LayoutParams(ingestPoint.width, ingestPoint.height));
        cameraView = new CameraView(cameraPreview, ingestPoint.width, ingestPoint.height, this);
        cameraView.initializeSurface();
    }

    // Okay, the camera has been initialized and here it tells us the actual resolution
    // that it is going to use. The next step is to fire up our StreamingEncoder.
    @Override
    public void onCameraReady(int actualWidth, int actualHeight) {
        setStatus(Status.INITIALIZING_ENCODER);

        streamingEncoder = new StreamingEncoder(actualWidth, actualHeight, ingestPoint);
        streamingEncoder.initialize();

        setStatus(Status.STREAMING);
        cameraView.setPreviewCallback(streamingEncoder);
    }

    public void interrupt() {
        currentThread.interrupt();
    }

    private void stop() {
        Log.i(TAG, "Stopping");
        setStatus(Status.STOPPING);

        if (cameraView != null) cameraView.release();
        if (streamingEncoder != null) streamingEncoder.release();

        setStatus(Status.STOPPED);
    }
}

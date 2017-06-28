package gq.icctv.icctv;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.LinearLayout;

class StreamingController implements Runnable, NetworkController.Callback, CameraView.Callback {

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

    enum Status {
        INITIAL,
        HELLO,
        INITIALIZING_CAMERA,
        INITIALIZING_ENCODER,
        STREAMING,
        STOPPING,
        STOPPED,
        ERROR
    }

    public interface Callback {
        void onStatusChanged(Status status);
        void onUrlChanged(String url);

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

    private void setChannelUrl(final String url){
        Log.i(TAG, "new url appeared");
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                statusCallback.onUrlChanged(url);
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

    private void start() {
        Log.i(TAG, "Starting");
        setStatus(Status.HELLO);
        networkController.hello();

        Log.i(TAG, "currendThread: " + currentThread.toString() + " === " + Thread.currentThread().toString());
    }

    @Override
    public void onHello(NetworkController.HelloResponse hello) {
        Log.i(TAG, "onHello");

        Log.i(TAG, "currentThread: " + currentThread.toString() + " === " + Thread.currentThread().toString());

        Log.d(TAG, hello.out);
        setChannelUrl(hello.out);

        if (hello == null) {
            Log.e(TAG, "onHello returned null response (malformed JSON or server error)");
            fail();
            return;
        }

        // TODO: Fallback to other ingest points
        if (hello.in == null) {
            Log.e(TAG, "Ingest points are null");
            fail();
            return;
        }
        ingestPoint = hello.in.get(0);

        initializeCamera(ingestPoint.width, ingestPoint.height);
    }

    // The width/height is the resolution we'd like to use,
    // but the camera might not support these exact values. This is why we have to wait until
    // the onCameraReady callback where we get passed the actual supported closest resolution.
    private void initializeCamera(final int width, final int height) {
        setStatus(Status.INITIALIZING_CAMERA);

        // Remember we can only touch the UI from the main thread
        // TODO: Maybe call this again in onCameraReady to apply the actual dimensions to the preview?
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                cameraPreview.setLayoutParams(new LinearLayout.LayoutParams(width, height));
            }
        });

        try {
            cameraView = new CameraView(cameraPreview, width, height, this);
            cameraView.initializeSurface();
        } catch (CameraView.InitializationException e) {
            Log.e(TAG, "Failed to initialize camera surface");
            fail();
        }

    }

    // Okay, the camera has been initialized and here it tells us the actual resolution
    // that it is going to use. The next step is to fire up our StreamingEncoder.
    @Override
    public void onCameraReady(int actualWidth, int actualHeight) {
        setStatus(Status.INITIALIZING_ENCODER);

        streamingEncoder = new StreamingEncoder(actualWidth, actualHeight, ingestPoint);

        try {
            streamingEncoder.initialize();
            setStatus(Status.STREAMING);
            cameraView.setPreviewCallback(streamingEncoder);
        } catch (StreamingEncoder.InitializationException e) {
            fail();
        }
    }

    void interrupt() {
        currentThread.interrupt();
    }

    private void fail() {
        Log.e(TAG, "Retrying after error");
        setStatus(Status.ERROR);
        stop();
        start();
    }

    private void stop() {
        Log.i(TAG, "Stopping");
        setStatus(Status.STOPPING);

        if (cameraView != null) cameraView.release();
        if (streamingEncoder != null) streamingEncoder.release();

        setStatus(Status.STOPPED);
    }

    public NetworkController getNetworkController(){
        return this.networkController;
    }
}

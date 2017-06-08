package gq.icctv.icctv;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity implements StreamingStatusCallback {

    private static final String TAG = "MainActivity";

    private StreamingController streamingController;
    private PermissionsManager permissionsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SurfaceView cameraPreview = (SurfaceView) findViewById(R.id.camera_preview);

        permissionsManager = new PermissionsManager(this, MainActivity.this);
        streamingController = new StreamingController(this, cameraPreview);

        if (permissionsManager.check()) {
            streamingController.start();
        } else {
            permissionsManager.request();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (permissionsManager.handleRequestPermissionsResult(requestCode, permissions, grantResults)) {
            streamingController.start();
        } else {
            // TODO: Handle denied camera permission
        }
    }

    @Override
    public void onStatusChanged(StreamingStatus status) {
        Log.i(TAG, "Streaming status changed to " + status);
    }

    @Override
    protected void onStop() {
        super.onStop();
        streamingController.stop();
    }

    public void debug(View btn) { streamingController.debug(); }

}

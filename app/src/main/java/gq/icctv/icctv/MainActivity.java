package gq.icctv.icctv;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private CameraView cameraView;
    private PermissionsManager permissionsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Hello");
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionsManager = new PermissionsManager(this, MainActivity.this);

        if (permissionsManager.check()) {
            startCamera();
        } else {
            permissionsManager.request();
        }
    }

    private void startCamera() {
        if (cameraView == null) {
            Log.i(TAG, "Starting camera");
            int width = 176;
            int height = 144;
            SurfaceView cameraPreview = (SurfaceView) findViewById(R.id.camera_preview);
            cameraPreview.setLayoutParams(new LinearLayout.LayoutParams(width, height));
            cameraView = new CameraView(cameraPreview, width, height);
            new Thread(cameraView).start();
        }
    }

    private void releaseCamera() {
        if (cameraView != null) {
            Log.i(TAG, "Releasing camera");
            cameraView.interrupt();
            cameraView = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (permissionsManager.handleRequestPermissionsResult(requestCode, permissions, grantResults)) {
            startCamera();
        } else {
            // TODO: Handle denied camera permission
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseCamera();
    }

    public void debugStart(View btn) {
        startCamera();
    }

    public void debugRelease(View btn) {
        releaseCamera();
    }
}

package gq.icctv.icctv;

import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import org.java_websocket.drafts.Draft_17;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private CameraView cameraView;
    private PermissionsManager permissionsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MainActivity", "Hello");
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
        SurfaceView cameraSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        cameraView = new CameraView(cameraSurfaceView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (permissionsManager.handleRequestPermissionsResult(requestCode, permissions, grantResults)) {
            startCamera();
        } else {
            // TODO: Handle denied camera permission
        }
    }
}

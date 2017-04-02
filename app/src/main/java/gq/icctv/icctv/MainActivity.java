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
    private Sender sender;
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
            startSender();
            startCamera();
        } else {
            permissionsManager.request();
        }
    }

    private void startSender() {
        try {
            sender = new Sender(new URI(Sender.RELAY_URL), new Draft_17());
            sender.connect();
        } catch (URISyntaxException e) {
            Log.e(TAG, "Malformed URL: " + Sender.RELAY_URL);
            e.printStackTrace();
        }
    }

    private void startCamera() {
        SurfaceView cameraSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        cameraView = new CameraView(cameraSurfaceView, sender);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermissionsManager.PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                } else {
                    // TODO: Handle denied camera permission
                }
                return;
            }
        }
    }
}

package gq.icctv.icctv;

import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;

public class MainActivity extends AppCompatActivity implements StreamingController.Callback {

    private static final String TAG = "MainActivity";

    private StreamingController streamingController;
    private PermissionsManager permissionsManager;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SurfaceView cameraPreview = (SurfaceView) findViewById(R.id.camera_preview);
        statusText = (TextView) findViewById(R.id.status_text);

        String uuid = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        AndroidNetworking.initialize(this);

        permissionsManager = new PermissionsManager(this, MainActivity.this);
        streamingController = new StreamingController(uuid, cameraPreview, this);

        if (permissionsManager.check()) {
            startStream();
        } else {
            permissionsManager.request();
        }
    }

    private void startStream() {
        new Thread(streamingController).start();
    }

    private void stopStream() {
        if (streamingController != null) streamingController.interrupt();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (permissionsManager.handleRequestPermissionsResult(requestCode, permissions, grantResults)) {
            startStream();
        } else {
            // TODO: Handle denied camera permission
        }
    }

    @Override
    public void onStatusChanged(StreamingController.Status status) {
        statusText.setText(status.toString());
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopStream();
    }

    public void debug(View btn) {
        stopStream();
        startStream();
    }

}

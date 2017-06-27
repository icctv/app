package gq.icctv.icctv;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;

import java.io.IOException;

import gq.icctv.icctv.server.http.CustomHttpServer;
import gq.icctv.icctv.server.websocket.DebugWebSocketServer;
import gq.icctv.icctv.server.websocket.MyHttpServer;
import gq.icctv.icctv.server.websocket.NanoWSD;

public class MainActivity extends AppCompatActivity implements StreamingController.Callback {

    private static final String TAG = "MainActivity";

    private StreamingController streamingController;
    private PermissionsManager permissionsManager;
    private TextView statusText;
    private CustomHttpServer server;
    private MyHttpServer wsserver;


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
    public void onResume(){
        super.onResume();

        int httpPort = 1337;
        int websocketPort = 1338;

        try {
            server = new CustomHttpServer(httpPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Start socket

        //NanoWSD ws = new DebugWebSocketServer(websocketPort,true);
        try {
            wsserver = new MyHttpServer(getApplicationContext(), websocketPort);
            wsserver.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        try {
            ws.start();

            Log.d("WS", "WebSocket started!!!");
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        String ipAddress = Formatter.formatIpAddress(ip);

        TextView tv = (TextView)findViewById(R.id.txt_address);
        tv.setText("Http:    " + ipAddress +  ":" +  String.valueOf(httpPort) + "\nSocket: " + ipAddress + ":" + websocketPort);

        //while(ws.isAlive());

        try {
            System.in.read();
        } catch (Exception e) {
            Log.d("websocket", e.getMessage());
        }
        //ws.stop();
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

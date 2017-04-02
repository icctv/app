package gq.icctv.icctv;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class Sender extends WebSocketClient {

    private static final String TAG = "Sender";
    public static final String RELAY_URL = "ws://192.168.1.108:3001/a";

    public Sender(URI serverURI, Draft draft) {
        super(serverURI, draft);
    }

    public void sendFrame(byte[] frame) {
        if (getConnection().isOpen()) {
            getConnection().send(frame);
        } else {
            Log.e(TAG, "Connection not open, discarding frame");
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i(TAG, "onOpen: handshakedata=" + handshakedata.toString());
    }

    @Override
    public void onMessage(String message) {
        Log.i(TAG, "onMessage: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i(TAG, "onClose: code=" + code + " reason=" + reason + " remote=" + remote);
    }

    @Override
    public void onError(Exception ex) {
        Log.e(TAG, "onError");
        ex.printStackTrace();
    }
}

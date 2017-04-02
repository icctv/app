package gq.icctv.icctv;

import android.util.Log;

public class Sender {

    private static final String TAG = "Sender";

    public void sendFrame(byte[] frame) {
        Log.i(TAG, "sendFrame " + frame[0] + "," + frame[30]);
    }
}

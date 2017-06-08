package gq.icctv.icctv;

import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class NetworkController {

    private static final String TAG = "NetworkController";

    private static String BASE_URL = "http://192.168.1.107:8080";
    private static String uuid;
    private Callback callback;

    NetworkController(String uuid, Callback callback) {
        this.uuid = uuid;
        this.callback = callback;
    }

    public void hello() {
        Log.i(TAG, "POST /hello/{uuid}");
        AndroidNetworking.post(BASE_URL + "/hello/{uuid}")
            .addPathParameter("uuid", uuid)
            .build()
            .getAsParsed(new TypeToken<HelloResponse>() {}, new ParsedRequestListener<HelloResponse>() {
                @Override
                public void onResponse(HelloResponse hello) {
                    Log.i(TAG, "Got Hello response");
                    callback.onHello(hello);
                }
                @Override
                public void onError(ANError anError) {
                    // TODO: Handle error
                    Log.e(TAG, "Error " + anError.getErrorCode() + " - " + anError.getMessage());
                    Log.e(TAG, "Response Body: " + anError.getErrorBody());
                }
            });
    }

    class HelloResponse {
        String out;
        List<IngestPoint> in;

        HelloResponse() {}
    }

    public interface Callback {
        void onHello(NetworkController.HelloResponse hello);
    }
}

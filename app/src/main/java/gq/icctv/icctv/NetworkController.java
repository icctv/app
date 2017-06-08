package gq.icctv.icctv;

import android.content.Context;
import android.provider.Settings;
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

    NetworkController(Context ctx) {
        uuid = Settings.Secure.getString(ctx.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        AndroidNetworking.initialize(ctx);

    }

    public void hello() {
        Log.i(TAG, "Requesting stuff");
        AndroidNetworking.post(BASE_URL + "/hello/{uuid}")
            .addPathParameter("uuid", uuid)
            .build()
            .getAsParsed(new TypeToken<HelloResponse>() {}, new ParsedRequestListener<HelloResponse>() {
                @Override
                public void onResponse(HelloResponse hello) {
                    Log.d(TAG, "RESPONSE out = " + hello.out);

                    Log.d(TAG, "RESPONSE first ingest point protocol = " + hello.in.get(0).protocol);
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
}

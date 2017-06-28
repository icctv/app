package gq.icctv.icctv;

import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.List;

class NetworkController {

    private static final String TAG = "NetworkController";

    //private String BASE_URL = "http://192.168.1.107:8080";
    private String BASE_URL = "http://10.0.0.141:8080";
    //private String BASE_URL = "http://google.com";
    //private String BASE_URL = "http://172.20.10.2:8080";
    private String uuid;
    private Callback callback;

    NetworkController(String uuid, Callback callback) {
        this.uuid = uuid;
        this.callback = callback;
    }

    void hello() {
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

    void setPassword(String pw){
        JSONObject jo = new JSONObject();
        try{
            jo.put("password", pw);
        } catch (Exception e){
            Log.d("json", e.getMessage());
        }

        AndroidNetworking.post(BASE_URL + "/protect/{uuid}")
                .addPathParameter("uuid", uuid)
                .addJSONObjectBody(jo)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                    @Override
                    public void onError(ANError error) {
                        Log.d("reddis-pw", error.getMessage());
                    }
                });
    }

    void deletePassword(){
        AndroidNetworking.delete(BASE_URL + "/protect/{uuid}")
                .addPathParameter("uuid", uuid)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                    @Override
                    public void onError(ANError error) {
                        Log.d("reddis-pw", error.getMessage());
                    }
                });
    }

    class HelloResponse {
        String out;
        List<IngestPoint> in;

        HelloResponse() {}
    }

    interface Callback {
        void onHello(NetworkController.HelloResponse hello);
    }

}

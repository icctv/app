package gq.icctv.icctv.server.http;

import android.content.Context;
import android.renderscript.ScriptGroup;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import gq.icctv.icctv.server.http.content.CookieHandler;
import gq.icctv.icctv.server.http.request.Method;
import gq.icctv.icctv.server.http.response.IStatus;
import gq.icctv.icctv.server.http.response.Response;
import gq.icctv.icctv.server.http.response.Status;

/**
 * Created by Richard on 09/06/2017.
 */

public class CustomHttpServer extends NanoHTTPD {
    private Context ctx;
    private int Port;

    public CustomHttpServer(int port, Context ctx) throws IOException {
        super(port);
        this.Port = port;
        this.ctx = ctx;
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    /*
    @Override
    public Response serve(IHTTPSession session) {
        String msg = "<html><body><h1>Hello server</h1>\n";
        Map<String, String> parms = session.getParms();
        if (parms.get("username") == null) {
            msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
        } else {
            msg += "<p>Hello, " + parms.get("username") + "!</p>";
        }
        return Response.newFixedLengthResponse(msg + "</body></html>\n");
    }*/

    @Override
    public Response serve(IHTTPSession ihs){
        String uri = ihs.getUri();
        Log.d("http", "SERVER :: URI " + uri);

        final StringBuilder buf = new StringBuilder();
        for(Map.Entry<String, String> kv: ihs.getHeaders().entrySet()){
            buf.append(kv.getKey() + " : " + kv.getValue() + "\n");
        }

        InputStream mbuffer = null;
        long rndBuffer = 0;

        try {
            if(uri.contains(".js")){
                mbuffer = this.ctx.getAssets().open(uri.substring(1));
                return new Response(Status.OK,"text/javascript", mbuffer, rndBuffer);
            } else if(uri.contains(".css")){
                mbuffer = this.ctx.getAssets().open(uri.substring(1));
                return new Response(Status.OK, "text/css", mbuffer, rndBuffer);
            } else if(uri.contains(".png")){
                mbuffer = this.ctx.getAssets().open(uri.substring(1));
                return new Response(Status.OK, "image/png", mbuffer, rndBuffer);
            } else {
                if(ihs.getUri().equals("/")){
                    mbuffer = ctx.getAssets().open("index.html");
                }else{
                    mbuffer = ctx.getAssets().open(uri.substring(1));
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        Response r = null;
        try {
            r = new Response(Status.ACCEPTED, "text/html", mbuffer, rndBuffer);
        } catch (Exception e){

        }


        return r;
    }

    public byte[] readBytes(InputStream inputStream) throws IOException {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }
}
package gq.icctv.icctv.server.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gq.icctv.icctv.server.http.response.Response;
import gq.icctv.icctv.server.websocket.CustomWebSocket;
import gq.icctv.icctv.server.websocket.WebSocket;

/**
 * Created by Richard on 09/06/2017.
 */

public class CustomHttpServer extends NanoHTTPD {


    public CustomHttpServer(int port) throws IOException {
        super(port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

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
    }
}
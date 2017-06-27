package gq.icctv.icctv.server.websocket;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gq.icctv.icctv.server.http.IHTTPSession;
import gq.icctv.icctv.server.http.response.Response;

/**
 * Created by Richard on 27/06/2017.
 */

public class MyHttpServer extends NanoWSD {
    private Context context;
    private int port;
    private List<CustomWebSocket> connections = new ArrayList<>();

    public MyHttpServer(Context context, int port) throws IOException {
        super(port);
        this.port = port;
        this.context = context;
    }

    public List<CustomWebSocket> getConnections() {
        return this.connections;
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        return new CustomWebSocket(handshake, this);
    }


}

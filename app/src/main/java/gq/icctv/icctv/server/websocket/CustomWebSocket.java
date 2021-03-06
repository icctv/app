package gq.icctv.icctv.server.websocket;

import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import gq.icctv.icctv.server.http.IHTTPSession;

/**
 * Created by Richard on 27/06/2017.
 */

public class CustomWebSocket extends WebSocket {

    MyHttpServer httpServer;
    IHTTPSession httpSession;
    String cwsId;

    public CustomWebSocket(IHTTPSession handshakeRequest, MyHttpServer httpServer) {
        super(handshakeRequest);
        this.httpSession = handshakeRequest;
        this.httpServer = httpServer;
        this.cwsId = UUID.randomUUID().toString();
    }

    @Override
    protected void onOpen() {
        httpServer.getConnections().add(this);
        Log.d("WS", "opened!");
    }

    @Override
    protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
        this.httpServer.getConnections().remove(this);
        Log.d("WS", "closed");
    }

    @Override
    protected void onMessage(WebSocketFrame message) {
        Log.d("WS-Msg", message.toString() + " + " + this.cwsId);
        httpServer.distributeMessage(message, this);
    }

    @Override
    protected void onPong(WebSocketFrame pong) {
        Log.d("WS-Pong", "PONG!");
    }

    @Override
    protected void onException(IOException exception) {
        Log.d("WS-Exc", exception.getMessage());
    }
}
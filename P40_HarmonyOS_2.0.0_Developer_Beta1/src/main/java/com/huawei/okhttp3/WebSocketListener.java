package com.huawei.okhttp3;

import com.huawei.okio.ByteString;
import javax.annotation.Nullable;

@Deprecated
public abstract class WebSocketListener {
    public void onOpen(WebSocket webSocket, Response response) {
    }

    public void onMessage(WebSocket webSocket, String text) {
    }

    public void onMessage(WebSocket webSocket, ByteString bytes) {
    }

    public void onClosing(WebSocket webSocket, int code, String reason) {
    }

    public void onClosed(WebSocket webSocket, int code, String reason) {
    }

    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
    }
}

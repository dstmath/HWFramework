package com.android.okhttp;

import java.net.Socket;

public interface Connection {
    Handshake getHandshake();

    Protocol getProtocol();

    Route getRoute();

    Socket getSocket();
}

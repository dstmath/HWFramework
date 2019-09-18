package com.huawei.okhttp3;

import java.net.Socket;
import javax.annotation.Nullable;

public interface Connection {
    @Nullable
    Handshake handshake();

    Protocol protocol();

    Route route();

    Socket socket();
}

package com.huawei.nearbysdk;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;

public interface NearbySocket {
    boolean close();

    int getBusinessId();

    int getBusinessType();

    int getChannel();

    InputStream getInputStream();

    SocketAddress getLocalSocketIpAddress();

    OutputStream getOutputStream();

    int getPort();

    NearbyDevice getRemoteNearbyDevice();

    SocketAddress getRemoteSocketIpAddress();

    int getSecurityType();

    String getServiceUuid();

    String getTag();

    boolean registerSocketStatus(SocketStatusListener socketStatusListener);

    void shutdownInput();

    void shutdownOutput();
}

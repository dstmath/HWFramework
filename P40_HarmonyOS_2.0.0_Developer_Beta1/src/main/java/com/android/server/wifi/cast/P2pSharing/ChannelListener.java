package com.android.server.wifi.cast.P2pSharing;

public interface ChannelListener {
    void onChannelEvent(int i);

    void onDataReceived(byte[] bArr);

    void onPortGet(int i);
}

package com.android.server.wifi.cast.P2pSharing;

import android.content.Context;

public interface P2pSharingInterface extends ChannelListener {
    void onP2pStateChanged(boolean z);

    void release();

    void setContext(Context context);

    void setP2pSharingListener(P2pSharingListener p2pSharingListener);

    void setUpTempConnection();
}

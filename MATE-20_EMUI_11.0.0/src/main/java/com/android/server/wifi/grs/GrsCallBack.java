package com.android.server.wifi.grs;

import com.android.server.wifi.grs.requestremote.GrsResponse;

public interface GrsCallBack {
    void onFailure();

    void onResponse(GrsResponse grsResponse);
}

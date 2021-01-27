package com.huawei.networkit.grs;

import com.huawei.networkit.grs.requestremote.GrsResponse;

public interface GrsCallBack {
    void onFailure();

    void onResponse(GrsResponse grsResponse);
}

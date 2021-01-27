package com.huawei.android.location;

import android.os.Bundle;

public interface HwHigeoCallbackInterface {
    void onCellBatchingCallback(int i, Bundle bundle);

    void onCellFenceCallback(int i, Bundle bundle);

    void onGeoFenceCallback(int i, Bundle bundle);

    void onHigeoEventCallback(int i, Bundle bundle);

    void onMmDataRequest(int i, Bundle bundle);

    void onWifiFenceCallback(int i, Bundle bundle);
}

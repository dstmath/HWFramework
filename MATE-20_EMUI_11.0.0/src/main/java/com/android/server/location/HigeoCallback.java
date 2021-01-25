package com.android.server.location;

import android.os.Bundle;

public interface HigeoCallback {
    void arStatusCallback(long j, int i, int i2, int i3);

    void gpsStateCallback(int i);

    void onRequestCellDb(Bundle bundle);

    void onSendExcept2Lbs(int i);

    void quickTtffCommandCallback(int i);

    void setChrDataCallback(int i, int i2, String str);

    void wifiCommandCallback(int i);

    void wlanScanCommandCallback(int i, int i2, int i3);
}

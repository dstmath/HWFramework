package com.android.server.location;

import android.location.Location;

public interface WifiFenceCallback {
    void onFusedLbsServiceDied();

    void onWififenceAddCb(int i, int i2);

    void onWififencePauseCb(int i, int i2);

    void onWififenceRemoveCb(int i, int i2);

    void onWififenceResumeCb(int i, int i2);

    void onWififenceStatusCb(int i, Location location);

    void onWififenceTransitionCb(int i, Location location, int i2, long j);
}

package com.android.server.location;

import android.os.Bundle;

public interface GeoFenceCallback {
    void geofenceAddResultCb(Bundle bundle);

    void geofenceRemoveResultCb(Bundle bundle);

    void gnssGeofencePauseCb(Bundle bundle);

    void gnssGeofenceResumeCb(Bundle bundle);

    void gnssGeofenceStatusCb(Bundle bundle);

    void gnssGeofenceTransitionCb(Bundle bundle);

    void onFusedLbsServiceDied();

    void onGetCurrentLocationCb(Bundle bundle);
}

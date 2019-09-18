package com.android.location.provider;

import android.hardware.location.GeofenceHardware;
import android.hardware.location.IGeofenceHardware;
import android.location.IGeofenceProvider;
import android.os.IBinder;

public abstract class GeofenceProvider {
    /* access modifiers changed from: private */
    public GeofenceHardware mGeofenceHardware;
    private IGeofenceProvider.Stub mProvider = new IGeofenceProvider.Stub() {
        public void setGeofenceHardware(IGeofenceHardware hardwareProxy) {
            GeofenceHardware unused = GeofenceProvider.this.mGeofenceHardware = new GeofenceHardware(hardwareProxy);
            GeofenceProvider.this.onGeofenceHardwareChange(GeofenceProvider.this.mGeofenceHardware);
        }
    };

    public abstract void onGeofenceHardwareChange(GeofenceHardware geofenceHardware);

    public IBinder getBinder() {
        return this.mProvider;
    }
}

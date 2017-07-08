package com.android.location.provider;

import android.hardware.location.GeofenceHardware;
import android.hardware.location.IGeofenceHardware;
import android.location.IGeofenceProvider.Stub;
import android.os.IBinder;

public abstract class GeofenceProvider {
    private GeofenceHardware mGeofenceHardware;
    private Stub mProvider;

    public abstract void onGeofenceHardwareChange(GeofenceHardware geofenceHardware);

    public GeofenceProvider() {
        this.mProvider = new Stub() {
            public void setGeofenceHardware(IGeofenceHardware hardwareProxy) {
                GeofenceProvider.this.mGeofenceHardware = new GeofenceHardware(hardwareProxy);
                GeofenceProvider.this.onGeofenceHardwareChange(GeofenceProvider.this.mGeofenceHardware);
            }
        };
    }

    public IBinder getBinder() {
        return this.mProvider;
    }
}

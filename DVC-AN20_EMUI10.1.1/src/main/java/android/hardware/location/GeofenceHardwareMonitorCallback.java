package android.hardware.location;

import android.annotation.SystemApi;
import android.location.Location;

@SystemApi
public abstract class GeofenceHardwareMonitorCallback {
    @Deprecated
    public void onMonitoringSystemChange(int monitoringType, boolean available, Location location) {
    }

    public void onMonitoringSystemChange(GeofenceHardwareMonitorEvent event) {
    }
}

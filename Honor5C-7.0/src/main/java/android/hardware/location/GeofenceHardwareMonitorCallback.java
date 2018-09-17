package android.hardware.location;

import android.location.Location;

public abstract class GeofenceHardwareMonitorCallback {
    @Deprecated
    public void onMonitoringSystemChange(int monitoringType, boolean available, Location location) {
    }

    public void onMonitoringSystemChange(GeofenceHardwareMonitorEvent event) {
    }
}

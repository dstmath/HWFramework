package android.hardware.location;

import android.annotation.SystemApi;
import android.location.Location;

@SystemApi
public abstract class GeofenceHardwareCallback {
    public void onGeofenceTransition(int geofenceId, int transition, Location location, long timestamp, int monitoringType) {
    }

    public void onGeofenceAdd(int geofenceId, int status) {
    }

    public void onGeofenceRemove(int geofenceId, int status) {
    }

    public void onGeofencePause(int geofenceId, int status) {
    }

    public void onGeofenceResume(int geofenceId, int status) {
    }
}

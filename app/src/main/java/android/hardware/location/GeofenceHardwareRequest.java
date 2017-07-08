package android.hardware.location;

import android.os.health.HealthKeys;
import android.rms.iaware.Events;

public final class GeofenceHardwareRequest {
    static final int GEOFENCE_TYPE_CIRCLE = 0;
    private int mLastTransition;
    private double mLatitude;
    private double mLongitude;
    private int mMonitorTransitions;
    private int mNotificationResponsiveness;
    private double mRadius;
    private int mSourceTechnologies;
    private int mType;
    private int mUnknownTimer;

    public GeofenceHardwareRequest() {
        this.mLastTransition = 4;
        this.mUnknownTimer = HealthKeys.BASE_PROCESS;
        this.mMonitorTransitions = 7;
        this.mNotificationResponsiveness = Events.EVENT_BASE_APP;
        this.mSourceTechnologies = 1;
    }

    private void setCircularGeofence(double latitude, double longitude, double radius) {
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mRadius = radius;
        this.mType = 0;
    }

    public static GeofenceHardwareRequest createCircularGeofence(double latitude, double longitude, double radius) {
        GeofenceHardwareRequest geofenceRequest = new GeofenceHardwareRequest();
        geofenceRequest.setCircularGeofence(latitude, longitude, radius);
        return geofenceRequest;
    }

    public void setLastTransition(int lastTransition) {
        this.mLastTransition = lastTransition;
    }

    public void setUnknownTimer(int unknownTimer) {
        this.mUnknownTimer = unknownTimer;
    }

    public void setMonitorTransitions(int monitorTransitions) {
        this.mMonitorTransitions = monitorTransitions;
    }

    public void setNotificationResponsiveness(int notificationResponsiveness) {
        this.mNotificationResponsiveness = notificationResponsiveness;
    }

    public void setSourceTechnologies(int sourceTechnologies) {
        int sanitizedSourceTechnologies = sourceTechnologies & 31;
        if (sanitizedSourceTechnologies == 0) {
            throw new IllegalArgumentException("At least one valid source technology must be set.");
        }
        this.mSourceTechnologies = sanitizedSourceTechnologies;
    }

    public double getLatitude() {
        return this.mLatitude;
    }

    public double getLongitude() {
        return this.mLongitude;
    }

    public double getRadius() {
        return this.mRadius;
    }

    public int getMonitorTransitions() {
        return this.mMonitorTransitions;
    }

    public int getUnknownTimer() {
        return this.mUnknownTimer;
    }

    public int getNotificationResponsiveness() {
        return this.mNotificationResponsiveness;
    }

    public int getLastTransition() {
        return this.mLastTransition;
    }

    public int getSourceTechnologies() {
        return this.mSourceTechnologies;
    }

    int getType() {
        return this.mType;
    }
}

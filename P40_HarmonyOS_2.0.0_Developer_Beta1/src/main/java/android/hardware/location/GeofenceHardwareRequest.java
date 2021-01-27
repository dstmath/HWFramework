package android.hardware.location;

import android.annotation.SystemApi;

@SystemApi
public final class GeofenceHardwareRequest {
    static final int GEOFENCE_TYPE_CIRCLE = 0;
    private int mLastTransition = 4;
    private double mLatitude;
    private double mLongitude;
    private int mMonitorTransitions = 7;
    private int mNotificationResponsiveness = 5000;
    private double mRadius;
    private int mSourceTechnologies = 1;
    private int mType;
    private int mUnknownTimer = 30000;

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
        if (sanitizedSourceTechnologies != 0) {
            this.mSourceTechnologies = sanitizedSourceTechnologies;
            return;
        }
        throw new IllegalArgumentException("At least one valid source technology must be set.");
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

    /* access modifiers changed from: package-private */
    public int getType() {
        return this.mType;
    }
}

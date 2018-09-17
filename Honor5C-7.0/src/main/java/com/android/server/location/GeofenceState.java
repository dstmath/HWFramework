package com.android.server.location;

import android.app.PendingIntent;
import android.location.Geofence;
import android.location.Location;

public class GeofenceState {
    public static final int FLAG_ENTER = 1;
    public static final int FLAG_EXIT = 2;
    private static final int STATE_INSIDE = 1;
    private static final int STATE_OUTSIDE = 2;
    private static final int STATE_UNKNOWN = 0;
    public final int mAllowedResolutionLevel;
    double mDistanceToCenter;
    public final long mExpireAt;
    public final Geofence mFence;
    public final PendingIntent mIntent;
    private final Location mLocation;
    public final String mPackageName;
    int mState;
    public final int mUid;

    public GeofenceState(Geofence fence, long expireAt, int allowedResolutionLevel, int uid, String packageName, PendingIntent intent) {
        this.mState = 0;
        this.mDistanceToCenter = Double.MAX_VALUE;
        this.mFence = fence;
        this.mExpireAt = expireAt;
        this.mAllowedResolutionLevel = allowedResolutionLevel;
        this.mUid = uid;
        this.mPackageName = packageName;
        this.mIntent = intent;
        this.mLocation = new Location("");
        this.mLocation.setLatitude(fence.getLatitude());
        this.mLocation.setLongitude(fence.getLongitude());
    }

    public int processLocation(Location location) {
        boolean inside;
        this.mDistanceToCenter = (double) this.mLocation.distanceTo(location);
        int prevState = this.mState;
        if (this.mDistanceToCenter <= ((double) Math.max(this.mFence.getRadius(), location.getAccuracy()))) {
            inside = true;
        } else {
            inside = false;
        }
        if (inside) {
            this.mState = STATE_INSIDE;
            if (prevState != STATE_INSIDE) {
                return STATE_INSIDE;
            }
        }
        this.mState = STATE_OUTSIDE;
        if (prevState == STATE_INSIDE) {
            return STATE_OUTSIDE;
        }
        return 0;
    }

    public double getDistanceToBoundary() {
        if (Double.compare(this.mDistanceToCenter, Double.MAX_VALUE) == 0) {
            return Double.MAX_VALUE;
        }
        return Math.abs(((double) this.mFence.getRadius()) - this.mDistanceToCenter);
    }

    public String toString() {
        String state;
        switch (this.mState) {
            case STATE_INSIDE /*1*/:
                state = "IN";
                break;
            case STATE_OUTSIDE /*2*/:
                state = "OUT";
                break;
            default:
                state = "?";
                break;
        }
        return String.format("%s d=%.0f %s", new Object[]{this.mFence.toString(), Double.valueOf(this.mDistanceToCenter), state});
    }
}

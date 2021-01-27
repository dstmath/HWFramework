package com.android.server.location;

import android.content.Context;
import android.database.ContentObserver;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.security.SecureRandom;

public class LocationFudger {
    private static final int APPROXIMATE_METERS_PER_DEGREE_AT_EQUATOR = 111000;
    private static final long CHANGE_INTERVAL_MS = 3600000;
    private static final double CHANGE_PER_INTERVAL = 0.03d;
    private static final String COARSE_ACCURACY_CONFIG_NAME = "locationCoarseAccuracy";
    private static final boolean D = false;
    private static final float DEFAULT_ACCURACY_IN_METERS = 2000.0f;
    public static final long FASTEST_INTERVAL_MS = 600000;
    private static final double MAX_LATITUDE = 89.999990990991d;
    private static final float MINIMUM_ACCURACY_IN_METERS = 200.0f;
    private static final double NEW_WEIGHT = 0.03d;
    private static final double PREVIOUS_WEIGHT = Math.sqrt(0.9991d);
    private static final String TAG = "LocationFudge";
    private float mAccuracyInMeters;
    private final Context mContext;
    private double mGridSizeInMeters;
    private final Object mLock = new Object();
    private long mNextInterval;
    private double mOffsetLatitudeMeters;
    private double mOffsetLongitudeMeters;
    private final SecureRandom mRandom = new SecureRandom();
    private final ContentObserver mSettingsObserver;
    private double mStandardDeviationInMeters;

    public LocationFudger(Context context, Handler handler) {
        this.mContext = context;
        this.mSettingsObserver = new ContentObserver(handler) {
            /* class com.android.server.location.LocationFudger.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                LocationFudger locationFudger = LocationFudger.this;
                locationFudger.setAccuracyInMeters(locationFudger.loadCoarseAccuracy());
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(COARSE_ACCURACY_CONFIG_NAME), false, this.mSettingsObserver);
        float accuracy = loadCoarseAccuracy();
        synchronized (this.mLock) {
            setAccuracyInMetersLocked(accuracy);
            this.mOffsetLatitudeMeters = nextOffsetLocked();
            this.mOffsetLongitudeMeters = nextOffsetLocked();
            this.mNextInterval = SystemClock.elapsedRealtime() + 3600000;
        }
    }

    public Location getOrCreate(Location location) {
        synchronized (this.mLock) {
            Location coarse = location.getExtraLocation("coarseLocation");
            if (coarse == null) {
                return addCoarseLocationExtraLocked(location);
            } else if (coarse.getAccuracy() >= this.mAccuracyInMeters) {
                return coarse;
            } else {
                return addCoarseLocationExtraLocked(location);
            }
        }
    }

    private Location addCoarseLocationExtraLocked(Location location) {
        Location coarse = createCoarseLocked(location);
        location.setExtraLocation("coarseLocation", coarse);
        return coarse;
    }

    private Location createCoarseLocked(Location fine) {
        Location coarse = new Location(fine);
        coarse.removeBearing();
        coarse.removeSpeed();
        coarse.removeAltitude();
        coarse.setExtras(null);
        double lat = coarse.getLatitude();
        double lon = coarse.getLongitude();
        double lat2 = wrapLatitude(lat);
        double lon2 = wrapLongitude(lon);
        updateRandomOffsetLocked();
        double lon3 = lon2 + metersToDegreesLongitude(this.mOffsetLongitudeMeters, lat2);
        double lat3 = wrapLatitude(lat2 + metersToDegreesLatitude(this.mOffsetLatitudeMeters));
        double lon4 = wrapLongitude(lon3);
        double latGranularity = metersToDegreesLatitude(this.mGridSizeInMeters);
        double lat4 = ((double) Math.round(lat3 / latGranularity)) * latGranularity;
        double lonGranularity = metersToDegreesLongitude(this.mGridSizeInMeters, lat4);
        double lat5 = wrapLatitude(lat4);
        double lon5 = wrapLongitude(((double) Math.round(lon4 / lonGranularity)) * lonGranularity);
        coarse.setLatitude(lat5);
        coarse.setLongitude(lon5);
        coarse.setAccuracy(Math.max(this.mAccuracyInMeters, coarse.getAccuracy()));
        return coarse;
    }

    private void updateRandomOffsetLocked() {
        long now = SystemClock.elapsedRealtime();
        if (now >= this.mNextInterval) {
            this.mNextInterval = 3600000 + now;
            this.mOffsetLatitudeMeters *= PREVIOUS_WEIGHT;
            this.mOffsetLatitudeMeters += nextOffsetLocked() * 0.03d;
            this.mOffsetLongitudeMeters *= PREVIOUS_WEIGHT;
            this.mOffsetLongitudeMeters += nextOffsetLocked() * 0.03d;
        }
    }

    private double nextOffsetLocked() {
        return this.mRandom.nextGaussian() * this.mStandardDeviationInMeters;
    }

    private static double wrapLatitude(double lat) {
        if (lat > MAX_LATITUDE) {
            lat = MAX_LATITUDE;
        }
        if (lat < -89.999990990991d) {
            return -89.999990990991d;
        }
        return lat;
    }

    private static double wrapLongitude(double lon) {
        double lon2 = lon % 360.0d;
        if (lon2 >= 180.0d) {
            lon2 -= 360.0d;
        }
        if (lon2 < -180.0d) {
            return lon2 + 360.0d;
        }
        return lon2;
    }

    private static double metersToDegreesLatitude(double distance) {
        return distance / 111000.0d;
    }

    private static double metersToDegreesLongitude(double distance, double lat) {
        return (distance / 111000.0d) / Math.cos(Math.toRadians(lat));
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println(String.format("offset: %.0f, %.0f (meters)", Double.valueOf(this.mOffsetLongitudeMeters), Double.valueOf(this.mOffsetLatitudeMeters)));
    }

    private void setAccuracyInMetersLocked(float accuracyInMeters) {
        this.mAccuracyInMeters = Math.max(accuracyInMeters, (float) MINIMUM_ACCURACY_IN_METERS);
        this.mGridSizeInMeters = (double) this.mAccuracyInMeters;
        this.mStandardDeviationInMeters = this.mGridSizeInMeters / 4.0d;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAccuracyInMeters(float accuracyInMeters) {
        synchronized (this.mLock) {
            setAccuracyInMetersLocked(accuracyInMeters);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private float loadCoarseAccuracy() {
        String newSetting = Settings.Secure.getString(this.mContext.getContentResolver(), COARSE_ACCURACY_CONFIG_NAME);
        if (newSetting == null) {
            return DEFAULT_ACCURACY_IN_METERS;
        }
        try {
            return Float.parseFloat(newSetting);
        } catch (NumberFormatException e) {
            return DEFAULT_ACCURACY_IN_METERS;
        }
    }
}

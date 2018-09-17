package com.android.server.location;

import android.content.Context;
import android.database.ContentObserver;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings.Secure;
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
            public void onChange(boolean selfChange) {
                LocationFudger.this.setAccuracyInMeters(LocationFudger.this.loadCoarseAccuracy());
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor(COARSE_ACCURACY_CONFIG_NAME), false, this.mSettingsObserver);
        float accuracy = loadCoarseAccuracy();
        synchronized (this.mLock) {
            setAccuracyInMetersLocked(accuracy);
            this.mOffsetLatitudeMeters = nextOffsetLocked();
            this.mOffsetLongitudeMeters = nextOffsetLocked();
            this.mNextInterval = SystemClock.elapsedRealtime() + CHANGE_INTERVAL_MS;
        }
    }

    public Location getOrCreate(Location location) {
        synchronized (this.mLock) {
            Location coarse = location.getExtraLocation("coarseLocation");
            Location addCoarseLocationExtraLocked;
            if (coarse == null) {
                addCoarseLocationExtraLocked = addCoarseLocationExtraLocked(location);
                return addCoarseLocationExtraLocked;
            } else if (coarse.getAccuracy() < this.mAccuracyInMeters) {
                addCoarseLocationExtraLocked = addCoarseLocationExtraLocked(location);
                return addCoarseLocationExtraLocked;
            } else {
                return coarse;
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
        lat = wrapLatitude(lat);
        lon = wrapLongitude(lon);
        updateRandomOffsetLocked();
        lon += metersToDegreesLongitude(this.mOffsetLongitudeMeters, lat);
        lat = wrapLatitude(lat + metersToDegreesLatitude(this.mOffsetLatitudeMeters));
        lon = wrapLongitude(lon);
        double latGranularity = metersToDegreesLatitude(this.mGridSizeInMeters);
        lat = ((double) Math.round(lat / latGranularity)) * latGranularity;
        double lonGranularity = metersToDegreesLongitude(this.mGridSizeInMeters, lat);
        lon = ((double) Math.round(lon / lonGranularity)) * lonGranularity;
        lat = wrapLatitude(lat);
        lon = wrapLongitude(lon);
        coarse.setLatitude(lat);
        coarse.setLongitude(lon);
        coarse.setAccuracy(Math.max(this.mAccuracyInMeters, coarse.getAccuracy()));
        return coarse;
    }

    private void updateRandomOffsetLocked() {
        long now = SystemClock.elapsedRealtime();
        if (now >= this.mNextInterval) {
            this.mNextInterval = CHANGE_INTERVAL_MS + now;
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
        lon %= 360.0d;
        if (lon >= 180.0d) {
            lon -= 360.0d;
        }
        if (lon < -180.0d) {
            return lon + 360.0d;
        }
        return lon;
    }

    private static double metersToDegreesLatitude(double distance) {
        return distance / 111000.0d;
    }

    private static double metersToDegreesLongitude(double distance, double lat) {
        return (distance / 111000.0d) / Math.cos(Math.toRadians(lat));
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println(String.format("offset: %.0f, %.0f (meters)", new Object[]{Double.valueOf(this.mOffsetLongitudeMeters), Double.valueOf(this.mOffsetLatitudeMeters)}));
    }

    private void setAccuracyInMetersLocked(float accuracyInMeters) {
        this.mAccuracyInMeters = Math.max(accuracyInMeters, MINIMUM_ACCURACY_IN_METERS);
        this.mGridSizeInMeters = (double) this.mAccuracyInMeters;
        this.mStandardDeviationInMeters = this.mGridSizeInMeters / 4.0d;
    }

    private void setAccuracyInMeters(float accuracyInMeters) {
        synchronized (this.mLock) {
            setAccuracyInMetersLocked(accuracyInMeters);
        }
    }

    private float loadCoarseAccuracy() {
        String newSetting = Secure.getString(this.mContext.getContentResolver(), COARSE_ACCURACY_CONFIG_NAME);
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

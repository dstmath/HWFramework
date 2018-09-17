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
    private static final double PREVIOUS_WEIGHT = 0.0d;
    private static final String TAG = "LocationFudge";
    private float mAccuracyInMeters;
    private final Context mContext;
    private double mGridSizeInMeters;
    private final Object mLock;
    private long mNextInterval;
    private double mOffsetLatitudeMeters;
    private double mOffsetLongitudeMeters;
    private final SecureRandom mRandom;
    private final ContentObserver mSettingsObserver;
    private double mStandardDeviationInMeters;

    /* renamed from: com.android.server.location.LocationFudger.1 */
    class AnonymousClass1 extends ContentObserver {
        AnonymousClass1(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            LocationFudger.this.setAccuracyInMeters(LocationFudger.this.loadCoarseAccuracy());
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.LocationFudger.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.LocationFudger.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.LocationFudger.<clinit>():void");
    }

    public LocationFudger(Context context, Handler handler) {
        this.mLock = new Object();
        this.mRandom = new SecureRandom();
        this.mContext = context;
        this.mSettingsObserver = new AnonymousClass1(handler);
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor(COARSE_ACCURACY_CONFIG_NAME), D, this.mSettingsObserver);
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
            this.mOffsetLatitudeMeters += nextOffsetLocked() * NEW_WEIGHT;
            this.mOffsetLongitudeMeters *= PREVIOUS_WEIGHT;
            this.mOffsetLongitudeMeters += nextOffsetLocked() * NEW_WEIGHT;
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

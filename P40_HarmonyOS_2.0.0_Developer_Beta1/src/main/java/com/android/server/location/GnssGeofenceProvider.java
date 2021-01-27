package com.android.server.location;

import android.location.IGpsGeofenceHardware;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;

/* access modifiers changed from: package-private */
public class GnssGeofenceProvider extends IGpsGeofenceHardware.Stub {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "GnssGeofenceProvider";
    @GuardedBy({"mLock"})
    private final SparseArray<GeofenceEntry> mGeofenceEntries;
    private final Object mLock;
    @GuardedBy({"mLock"})
    private final GnssGeofenceProviderNative mNative;

    /* access modifiers changed from: private */
    public static native boolean native_add_geofence(int i, double d, double d2, double d3, int i2, int i3, int i4, int i5);

    /* access modifiers changed from: private */
    public static native boolean native_is_geofence_supported();

    /* access modifiers changed from: private */
    public static native boolean native_pause_geofence(int i);

    /* access modifiers changed from: private */
    public static native boolean native_remove_geofence(int i);

    /* access modifiers changed from: private */
    public static native boolean native_resume_geofence(int i, int i2);

    /* access modifiers changed from: private */
    public static class GeofenceEntry {
        public int geofenceId;
        public int lastTransition;
        public double latitude;
        public double longitude;
        public int monitorTransitions;
        public int notificationResponsiveness;
        public boolean paused;
        public double radius;
        public int unknownTimer;

        private GeofenceEntry() {
        }
    }

    GnssGeofenceProvider() {
        this(new GnssGeofenceProviderNative());
    }

    @VisibleForTesting
    GnssGeofenceProvider(GnssGeofenceProviderNative gnssGeofenceProviderNative) {
        this.mLock = new Object();
        this.mGeofenceEntries = new SparseArray<>();
        this.mNative = gnssGeofenceProviderNative;
    }

    /* access modifiers changed from: package-private */
    public void resumeIfStarted() {
        if (DEBUG) {
            Log.d(TAG, "resumeIfStarted");
        }
        synchronized (this.mLock) {
            for (int i = 0; i < this.mGeofenceEntries.size(); i++) {
                GeofenceEntry entry = this.mGeofenceEntries.valueAt(i);
                if (this.mNative.addGeofence(entry.geofenceId, entry.latitude, entry.longitude, entry.radius, entry.lastTransition, entry.monitorTransitions, entry.notificationResponsiveness, entry.unknownTimer) && entry.paused) {
                    this.mNative.pauseGeofence(entry.geofenceId);
                }
            }
        }
    }

    public boolean isHardwareGeofenceSupported() {
        boolean isGeofenceSupported;
        synchronized (this.mLock) {
            isGeofenceSupported = this.mNative.isGeofenceSupported();
        }
        return isGeofenceSupported;
    }

    public boolean addCircularHardwareGeofence(int geofenceId, double latitude, double longitude, double radius, int lastTransition, int monitorTransitions, int notificationResponsiveness, int unknownTimer) {
        synchronized (this.mLock) {
            try {
                boolean added = this.mNative.addGeofence(geofenceId, latitude, longitude, radius, lastTransition, monitorTransitions, notificationResponsiveness, unknownTimer);
                if (added) {
                    GeofenceEntry entry = new GeofenceEntry();
                    entry.geofenceId = geofenceId;
                    try {
                        entry.latitude = latitude;
                        try {
                            entry.longitude = longitude;
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                    try {
                        entry.radius = radius;
                        try {
                            entry.lastTransition = lastTransition;
                            try {
                                entry.monitorTransitions = monitorTransitions;
                                try {
                                    entry.notificationResponsiveness = notificationResponsiveness;
                                    entry.unknownTimer = unknownTimer;
                                    this.mGeofenceEntries.put(geofenceId, entry);
                                } catch (Throwable th3) {
                                    th = th3;
                                    throw th;
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                throw th;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        throw th;
                    }
                }
                return added;
            } catch (Throwable th7) {
                th = th7;
                throw th;
            }
        }
    }

    public boolean removeHardwareGeofence(int geofenceId) {
        boolean removed;
        synchronized (this.mLock) {
            removed = this.mNative.removeGeofence(geofenceId);
            if (removed) {
                this.mGeofenceEntries.remove(geofenceId);
            }
        }
        return removed;
    }

    public boolean pauseHardwareGeofence(int geofenceId) {
        boolean paused;
        GeofenceEntry entry;
        synchronized (this.mLock) {
            paused = this.mNative.pauseGeofence(geofenceId);
            if (paused && (entry = this.mGeofenceEntries.get(geofenceId)) != null) {
                entry.paused = true;
            }
        }
        return paused;
    }

    public boolean resumeHardwareGeofence(int geofenceId, int monitorTransitions) {
        boolean resumed;
        GeofenceEntry entry;
        synchronized (this.mLock) {
            resumed = this.mNative.resumeGeofence(geofenceId, monitorTransitions);
            if (resumed && (entry = this.mGeofenceEntries.get(geofenceId)) != null) {
                entry.paused = false;
                entry.monitorTransitions = monitorTransitions;
            }
        }
        return resumed;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class GnssGeofenceProviderNative {
        GnssGeofenceProviderNative() {
        }

        public boolean isGeofenceSupported() {
            Log.d(GnssGeofenceProvider.TAG, "calling isGeofenceSupported");
            return GnssGeofenceProvider.native_is_geofence_supported();
        }

        public boolean addGeofence(int geofenceId, double latitude, double longitude, double radius, int lastTransition, int monitorTransitions, int notificationResponsiveness, int unknownTimer) {
            Log.d(GnssGeofenceProvider.TAG, "calling addGeofence, geofenceId=" + geofenceId);
            return GnssGeofenceProvider.native_add_geofence(geofenceId, latitude, longitude, radius, lastTransition, monitorTransitions, notificationResponsiveness, unknownTimer);
        }

        public boolean removeGeofence(int geofenceId) {
            Log.d(GnssGeofenceProvider.TAG, "calling removeGeofence, geofenceId=" + geofenceId);
            return GnssGeofenceProvider.native_remove_geofence(geofenceId);
        }

        public boolean resumeGeofence(int geofenceId, int transitions) {
            Log.d(GnssGeofenceProvider.TAG, "calling resumeGeofence, geofenceId=" + geofenceId);
            return GnssGeofenceProvider.native_resume_geofence(geofenceId, transitions);
        }

        public boolean pauseGeofence(int geofenceId) {
            Log.d(GnssGeofenceProvider.TAG, "calling pauseGeofence, geofenceId=" + geofenceId);
            return GnssGeofenceProvider.native_pause_geofence(geofenceId);
        }
    }
}

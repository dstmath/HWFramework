package com.android.server.location;

import android.location.IGpsGeofenceHardware;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.location.GnssGeofenceProvider;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

class GnssGeofenceProvider extends IGpsGeofenceHardware.Stub {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "GnssGeofenceProvider";
    private HandlerThread mGeoFenceThread;
    private final SparseArray<GeofenceEntry> mGeofenceEntries;
    private final Handler mHandler;
    private final GnssGeofenceProviderNative mNative;

    private static class GeofenceEntry {
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

    @VisibleForTesting
    static class GnssGeofenceProviderNative {
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

    GnssGeofenceProvider(Looper looper) {
        this(looper, new GnssGeofenceProviderNative());
    }

    @VisibleForTesting
    GnssGeofenceProvider(Looper looper, GnssGeofenceProviderNative gnssGeofenceProviderNative) {
        this.mGeofenceEntries = new SparseArray<>();
        this.mGeoFenceThread = new HandlerThread("GeoFenceThread");
        this.mGeoFenceThread.start();
        this.mHandler = new Handler(this.mGeoFenceThread.getLooper());
        this.mNative = gnssGeofenceProviderNative;
    }

    /* access modifiers changed from: package-private */
    public void resumeIfStarted() {
        if (DEBUG) {
            Log.d(TAG, "resumeIfStarted");
        }
        this.mHandler.post(new Runnable() {
            public final void run() {
                GnssGeofenceProvider.lambda$resumeIfStarted$0(GnssGeofenceProvider.this);
            }
        });
    }

    public static /* synthetic */ void lambda$resumeIfStarted$0(GnssGeofenceProvider gnssGeofenceProvider) {
        for (int i = 0; i < gnssGeofenceProvider.mGeofenceEntries.size(); i++) {
            GeofenceEntry entry = gnssGeofenceProvider.mGeofenceEntries.valueAt(i);
            if (gnssGeofenceProvider.mNative.addGeofence(entry.geofenceId, entry.latitude, entry.longitude, entry.radius, entry.lastTransition, entry.monitorTransitions, entry.notificationResponsiveness, entry.unknownTimer) && entry.paused) {
                gnssGeofenceProvider.mNative.pauseGeofence(entry.geofenceId);
            }
        }
    }

    private boolean runOnHandlerThread(Callable<Boolean> callable) {
        FutureTask<Boolean> futureTask = new FutureTask<>(callable);
        this.mHandler.post(futureTask);
        try {
            return futureTask.get().booleanValue();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "Failed running callable.", e);
            return false;
        }
    }

    public boolean isHardwareGeofenceSupported() {
        GnssGeofenceProviderNative gnssGeofenceProviderNative = this.mNative;
        Objects.requireNonNull(gnssGeofenceProviderNative);
        return runOnHandlerThread(new Callable() {
            public final Object call() {
                return Boolean.valueOf(GnssGeofenceProvider.GnssGeofenceProviderNative.this.isGeofenceSupported());
            }
        });
    }

    public boolean addCircularHardwareGeofence(int geofenceId, double latitude, double longitude, double radius, int lastTransition, int monitorTransitions, int notificationResponsiveness, int unknownTimer) {
        $$Lambda$GnssGeofenceProvider$n5osOgh5pgunifw_x5yjaRzShkA r0 = new Callable(geofenceId, latitude, longitude, radius, lastTransition, monitorTransitions, notificationResponsiveness, unknownTimer) {
            private final /* synthetic */ int f$1;
            private final /* synthetic */ double f$2;
            private final /* synthetic */ double f$3;
            private final /* synthetic */ double f$4;
            private final /* synthetic */ int f$5;
            private final /* synthetic */ int f$6;
            private final /* synthetic */ int f$7;
            private final /* synthetic */ int f$8;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r5;
                this.f$4 = r7;
                this.f$5 = r9;
                this.f$6 = r10;
                this.f$7 = r11;
                this.f$8 = r12;
            }

            public final Object call() {
                return GnssGeofenceProvider.lambda$addCircularHardwareGeofence$1(GnssGeofenceProvider.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8);
            }
        };
        return runOnHandlerThread(r0);
    }

    public static /* synthetic */ Boolean lambda$addCircularHardwareGeofence$1(GnssGeofenceProvider gnssGeofenceProvider, int geofenceId, double latitude, double longitude, double radius, int lastTransition, int monitorTransitions, int notificationResponsiveness, int unknownTimer) throws Exception {
        GnssGeofenceProvider gnssGeofenceProvider2 = gnssGeofenceProvider;
        int i = geofenceId;
        boolean added = gnssGeofenceProvider2.mNative.addGeofence(i, latitude, longitude, radius, lastTransition, monitorTransitions, notificationResponsiveness, unknownTimer);
        if (added) {
            GeofenceEntry entry = new GeofenceEntry();
            entry.geofenceId = i;
            entry.latitude = latitude;
            entry.longitude = longitude;
            entry.radius = radius;
            entry.lastTransition = lastTransition;
            entry.monitorTransitions = monitorTransitions;
            entry.notificationResponsiveness = notificationResponsiveness;
            entry.unknownTimer = unknownTimer;
            gnssGeofenceProvider2.mGeofenceEntries.put(i, entry);
        } else {
            double d = latitude;
            double d2 = longitude;
            double d3 = radius;
            int i2 = lastTransition;
            int i3 = monitorTransitions;
            int i4 = notificationResponsiveness;
            int i5 = unknownTimer;
        }
        return Boolean.valueOf(added);
    }

    public boolean removeHardwareGeofence(int geofenceId) {
        return runOnHandlerThread(new Callable(geofenceId) {
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final Object call() {
                return GnssGeofenceProvider.lambda$removeHardwareGeofence$2(GnssGeofenceProvider.this, this.f$1);
            }
        });
    }

    public static /* synthetic */ Boolean lambda$removeHardwareGeofence$2(GnssGeofenceProvider gnssGeofenceProvider, int geofenceId) throws Exception {
        boolean removed = gnssGeofenceProvider.mNative.removeGeofence(geofenceId);
        if (removed) {
            gnssGeofenceProvider.mGeofenceEntries.remove(geofenceId);
        }
        return Boolean.valueOf(removed);
    }

    public boolean pauseHardwareGeofence(int geofenceId) {
        return runOnHandlerThread(new Callable(geofenceId) {
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final Object call() {
                return GnssGeofenceProvider.lambda$pauseHardwareGeofence$3(GnssGeofenceProvider.this, this.f$1);
            }
        });
    }

    public static /* synthetic */ Boolean lambda$pauseHardwareGeofence$3(GnssGeofenceProvider gnssGeofenceProvider, int geofenceId) throws Exception {
        boolean paused = gnssGeofenceProvider.mNative.pauseGeofence(geofenceId);
        if (paused) {
            GeofenceEntry entry = gnssGeofenceProvider.mGeofenceEntries.get(geofenceId);
            if (entry != null) {
                entry.paused = true;
            }
        }
        return Boolean.valueOf(paused);
    }

    public boolean resumeHardwareGeofence(int geofenceId, int monitorTransitions) {
        return runOnHandlerThread(new Callable(geofenceId, monitorTransitions) {
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final Object call() {
                return GnssGeofenceProvider.lambda$resumeHardwareGeofence$4(GnssGeofenceProvider.this, this.f$1, this.f$2);
            }
        });
    }

    public static /* synthetic */ Boolean lambda$resumeHardwareGeofence$4(GnssGeofenceProvider gnssGeofenceProvider, int geofenceId, int monitorTransitions) throws Exception {
        boolean resumed = gnssGeofenceProvider.mNative.resumeGeofence(geofenceId, monitorTransitions);
        if (resumed) {
            GeofenceEntry entry = gnssGeofenceProvider.mGeofenceEntries.get(geofenceId);
            if (entry != null) {
                entry.paused = false;
                entry.monitorTransitions = monitorTransitions;
            }
        }
        return Boolean.valueOf(resumed);
    }
}

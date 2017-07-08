package com.android.server.location;

import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.PendingIntent.OnFinished;
import android.content.Context;
import android.content.Intent;
import android.location.Geofence;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import com.android.server.LocationManagerService;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GeofenceManager implements LocationListener, OnFinished {
    private static final boolean D = false;
    private static final long MAX_AGE_NANOS = 300000000000L;
    private static final long MAX_INTERVAL_MS = 7200000;
    private static final int MAX_SPEED_M_S = 100;
    private static final long MIN_INTERVAL_MS = 60000;
    private static final int MSG_UPDATE_FENCES = 1;
    private static final String TAG = "GeofenceManager";
    private final AppOpsManager mAppOps;
    private final LocationBlacklist mBlacklist;
    private final Context mContext;
    private List<GeofenceState> mFences;
    private final GeofenceHandler mHandler;
    private Location mLastLocationUpdate;
    private final LocationManager mLocationManager;
    private long mLocationUpdateInterval;
    private Object mLock;
    private boolean mPendingUpdate;
    private boolean mReceivingLocationUpdates;
    private final WakeLock mWakeLock;

    private final class GeofenceHandler extends Handler {
        public GeofenceHandler() {
            super(true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GeofenceManager.MSG_UPDATE_FENCES /*1*/:
                    GeofenceManager.this.updateFences();
                default:
            }
        }
    }

    public GeofenceManager(Context context, LocationBlacklist blacklist) {
        this.mLock = new Object();
        this.mFences = new LinkedList();
        this.mContext = context;
        this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(MSG_UPDATE_FENCES, TAG);
        this.mHandler = new GeofenceHandler();
        this.mBlacklist = blacklist;
        GpsFreezeProc.getInstance().registerFreezeListener(new GpsFreezeListener() {
            public void onFreezeProChange() {
                if (!GeofenceManager.this.mPendingUpdate) {
                    GeofenceManager.this.mPendingUpdate = true;
                    GeofenceManager.this.mHandler.sendEmptyMessage(GeofenceManager.MSG_UPDATE_FENCES);
                }
            }
        });
    }

    public void addFence(LocationRequest request, Geofence geofence, PendingIntent intent, int allowedResolutionLevel, int uid, String packageName) {
        GeofenceState state = new GeofenceState(geofence, request.getExpireAt(), allowedResolutionLevel, uid, packageName, intent);
        synchronized (this.mLock) {
            for (int i = this.mFences.size() - 1; i >= 0; i--) {
                GeofenceState w = (GeofenceState) this.mFences.get(i);
                if (geofence.equals(w.mFence) && intent.equals(w.mIntent)) {
                    this.mFences.remove(i);
                    break;
                }
            }
            this.mFences.add(state);
            scheduleUpdateFencesLocked();
        }
    }

    public void removeFence(Geofence fence, PendingIntent intent) {
        synchronized (this.mLock) {
            Iterator<GeofenceState> iter = this.mFences.iterator();
            while (iter.hasNext()) {
                GeofenceState state = (GeofenceState) iter.next();
                if (state.mIntent.equals(intent)) {
                    if (fence == null) {
                        iter.remove();
                    } else if (fence.equals(state.mFence)) {
                        iter.remove();
                    }
                }
            }
            scheduleUpdateFencesLocked();
        }
    }

    public void removeFence(String packageName) {
        synchronized (this.mLock) {
            Iterator<GeofenceState> iter = this.mFences.iterator();
            while (iter.hasNext()) {
                if (((GeofenceState) iter.next()).mPackageName.equals(packageName)) {
                    iter.remove();
                }
            }
            scheduleUpdateFencesLocked();
        }
    }

    private void removeExpiredFencesLocked() {
        long time = SystemClock.elapsedRealtime();
        Iterator<GeofenceState> iter = this.mFences.iterator();
        while (iter.hasNext()) {
            if (((GeofenceState) iter.next()).mExpireAt < time) {
                iter.remove();
            }
        }
    }

    private void scheduleUpdateFencesLocked() {
        if (!this.mPendingUpdate) {
            this.mPendingUpdate = true;
            this.mHandler.sendEmptyMessage(MSG_UPDATE_FENCES);
        }
    }

    private Location getFreshLocationLocked() {
        Location location;
        if (this.mReceivingLocationUpdates) {
            location = this.mLastLocationUpdate;
        } else {
            location = null;
        }
        if (location == null && !this.mFences.isEmpty()) {
            location = this.mLocationManager.getLastLocation();
        }
        if (location != null && SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos() <= MAX_AGE_NANOS) {
            return location;
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateFences() {
        List<PendingIntent> enterIntents = new LinkedList();
        List<PendingIntent> exitIntents = new LinkedList();
        synchronized (this.mLock) {
            this.mPendingUpdate = D;
            removeExpiredFencesLocked();
            Location location = getFreshLocationLocked();
            double minFenceDistance = Double.MAX_VALUE;
            boolean needUpdates = D;
            for (GeofenceState state : this.mFences) {
                if (!this.mBlacklist.isBlacklisted(state.mPackageName)) {
                    if (!GpsFreezeProc.getInstance().isFreeze(state.mPackageName)) {
                        if (LocationManagerService.resolutionLevelToOp(state.mAllowedResolutionLevel) >= 0) {
                            if (this.mAppOps.noteOpNoThrow(MSG_UPDATE_FENCES, state.mUid, state.mPackageName) != 0) {
                            }
                        }
                        needUpdates = true;
                        if (location != null) {
                            int event = state.processLocation(location);
                            if ((event & MSG_UPDATE_FENCES) != 0) {
                                enterIntents.add(state.mIntent);
                            }
                            if ((event & 2) != 0) {
                                exitIntents.add(state.mIntent);
                            }
                            double fenceDistance = state.getDistanceToBoundary();
                            if (fenceDistance < minFenceDistance) {
                                minFenceDistance = fenceDistance;
                            }
                        }
                    }
                }
            }
            if (needUpdates) {
                long intervalMs;
                if (location == null || Double.compare(minFenceDistance, Double.MAX_VALUE) == 0) {
                    intervalMs = MIN_INTERVAL_MS;
                } else {
                    intervalMs = (long) Math.min(7200000.0d, Math.max(60000.0d, (1000.0d * minFenceDistance) / 100.0d));
                }
                if (this.mReceivingLocationUpdates) {
                }
                this.mReceivingLocationUpdates = true;
                this.mLocationUpdateInterval = intervalMs;
                this.mLastLocationUpdate = location;
                LocationRequest request = new LocationRequest();
                request.setInterval(intervalMs).setFastestInterval(0);
                this.mLocationManager.requestLocationUpdates(request, this, this.mHandler.getLooper());
            } else if (this.mReceivingLocationUpdates) {
                this.mReceivingLocationUpdates = D;
                this.mLocationUpdateInterval = 0;
                this.mLastLocationUpdate = null;
                this.mLocationManager.removeUpdates(this);
            }
        }
        for (PendingIntent intent : exitIntents) {
            sendIntentExit(intent);
        }
        for (PendingIntent intent2 : enterIntents) {
            sendIntentEnter(intent2);
        }
    }

    private void sendIntentEnter(PendingIntent pendingIntent) {
        Intent intent = new Intent();
        intent.putExtra("entering", true);
        sendIntent(pendingIntent, intent);
    }

    private void sendIntentExit(PendingIntent pendingIntent) {
        Intent intent = new Intent();
        intent.putExtra("entering", D);
        sendIntent(pendingIntent, intent);
    }

    private void sendIntent(PendingIntent pendingIntent, Intent intent) {
        this.mWakeLock.acquire();
        try {
            pendingIntent.send(this.mContext, 0, intent, this, null, "android.permission.ACCESS_FINE_LOCATION");
        } catch (CanceledException e) {
            removeFence(null, pendingIntent);
            this.mWakeLock.release();
        }
    }

    public void onLocationChanged(Location location) {
        synchronized (this.mLock) {
            if (this.mReceivingLocationUpdates) {
                this.mLastLocationUpdate = location;
            }
            if (this.mPendingUpdate) {
                this.mHandler.removeMessages(MSG_UPDATE_FENCES);
            } else {
                this.mPendingUpdate = true;
            }
        }
        updateFences();
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }

    public void onSendFinished(PendingIntent pendingIntent, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
        this.mWakeLock.release();
    }

    public void dump(PrintWriter pw) {
        pw.println("  Geofences:");
        for (GeofenceState state : this.mFences) {
            pw.append("    ");
            pw.append(state.mPackageName);
            pw.append(" ");
            pw.append(state.mFence.toString());
            pw.append("\n");
        }
    }
}

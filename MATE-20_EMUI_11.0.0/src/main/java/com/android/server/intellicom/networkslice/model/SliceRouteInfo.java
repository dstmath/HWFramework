package com.android.server.intellicom.networkslice.model;

import android.util.Log;
import com.android.server.intellicom.networkslice.model.TrafficDescriptors;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SliceRouteInfo {
    private static final byte MATCH_ALL = 1;
    private static final String SEPARATOR_FOR_NORMAL_DATA = ",";
    private static final String TAG = "SliceRouteInfo";
    private FqdnIps mFqdnIps;
    private final Set<Integer> mSignedUids = new HashSet();
    private final Set<Integer> mUids = new HashSet();
    private final Set<Integer> mUsedUids = new HashSet();
    private final Set<FqdnIps> mWaittingFqdnIps = new HashSet();

    public boolean isBindCompleted(int uid, FqdnIps fqdnIps, TrafficDescriptors.RouteBindType routeBindType) {
        if (routeBindType == null) {
            return true;
        }
        int i = AnonymousClass1.$SwitchMap$com$android$server$intellicom$networkslice$model$TrafficDescriptors$RouteBindType[routeBindType.ordinal()];
        if (i == 1) {
            return isUidBindCompleted(uid);
        }
        if (i == 2) {
            return isNoNewFqdnIp(fqdnIps);
        }
        if (i != 3) {
            log("Invalid TrafficDescriptors RouteBindType.");
            return true;
        } else if (!isUidBindCompleted(uid) || !isNoNewFqdnIp(fqdnIps)) {
            return false;
        } else {
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.intellicom.networkslice.model.SliceRouteInfo$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$intellicom$networkslice$model$TrafficDescriptors$RouteBindType = new int[TrafficDescriptors.RouteBindType.values().length];

        static {
            try {
                $SwitchMap$com$android$server$intellicom$networkslice$model$TrafficDescriptors$RouteBindType[TrafficDescriptors.RouteBindType.UID_TDS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$intellicom$networkslice$model$TrafficDescriptors$RouteBindType[TrafficDescriptors.RouteBindType.IP_TDS.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$server$intellicom$networkslice$model$TrafficDescriptors$RouteBindType[TrafficDescriptors.RouteBindType.UID_IP_TDS.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    public void clearUids() {
        this.mUids.clear();
    }

    public void addUid(int uid) {
        this.mUids.add(Integer.valueOf(uid));
    }

    public void removeUid(int uid) {
        this.mUids.remove(Integer.valueOf(uid));
    }

    public String getUidsStr() {
        return (String) this.mUids.stream().map($$Lambda$SliceRouteInfo$5SPy5OmgYvXvJKcf7_i9gPGOi6I.INSTANCE).collect(Collectors.joining(","));
    }

    public void addUsedUid(int uid) {
        this.mUsedUids.add(Integer.valueOf(uid));
    }

    public void clearUsedUids() {
        this.mUsedUids.clear();
    }

    public void removeUsedUid(int uid) {
        this.mUsedUids.remove(Integer.valueOf(uid));
    }

    public void clearWaittingFqdnIps() {
        this.mWaittingFqdnIps.clear();
    }

    public FqdnIps getFqdnIps() {
        return this.mFqdnIps;
    }

    public void setFqdnIps(FqdnIps fqdnIps) {
        this.mFqdnIps = fqdnIps;
    }

    public Set<FqdnIps> getWaittingFqdnIps() {
        return this.mWaittingFqdnIps;
    }

    public void setWaittingFqdnIps(Set<FqdnIps> waittingFqdnIps) {
        if (waittingFqdnIps != null) {
            this.mWaittingFqdnIps.clear();
            this.mWaittingFqdnIps.addAll(waittingFqdnIps);
        }
    }

    public Set<Integer> getUsedUids() {
        return Collections.unmodifiableSet(this.mUsedUids);
    }

    public Set<Integer> getUids() {
        return Collections.unmodifiableSet(this.mUids);
    }

    public void setUids(Set<Integer> uids) {
        if (uids != null) {
            this.mUids.clear();
            this.mUids.addAll(uids);
        }
    }

    public void addSignedUid(int uid) {
        this.mSignedUids.add(Integer.valueOf(uid));
    }

    public void removeSignedUid(int uid) {
        this.mSignedUids.remove(Integer.valueOf(uid));
    }

    public Set<Integer> getSignedUids() {
        return Collections.unmodifiableSet(this.mSignedUids);
    }

    public String toString() {
        return "SliceRouteInfo{mUids=" + this.mUids + ", mUsedUids=" + this.mUsedUids + ", mWaittingFqdnIps=" + this.mWaittingFqdnIps + ", mFqdnIps=" + this.mFqdnIps + "}";
    }

    private boolean isNoNewFqdnIp(FqdnIps fqdnIps) {
        FqdnIps fqdnIps2 = this.mFqdnIps;
        if (fqdnIps2 == null) {
            return true;
        }
        return fqdnIps2.getNewFqdnIps(fqdnIps).isEmpty();
    }

    private boolean isUidBindCompleted(int uid) {
        return this.mUids.contains(Integer.valueOf(uid)) || this.mSignedUids.contains(Integer.valueOf(uid));
    }

    private void log(String msg) {
        Log.i(TAG, msg);
    }
}

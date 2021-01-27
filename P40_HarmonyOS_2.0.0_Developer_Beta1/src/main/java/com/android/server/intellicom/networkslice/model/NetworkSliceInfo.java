package com.android.server.intellicom.networkslice.model;

import android.net.NetworkRequest;
import android.os.Messenger;
import android.util.Log;
import com.android.server.intellicom.networkslice.css.NetworkSliceCallback;
import huawei.net.NetworkRequestExt;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkSliceInfo {
    public static final int INVALID_NET_ID = -1;
    private static final String TAG = "NetworkSliceInfo";
    private Map<Integer, Messenger> mMessengers = new ConcurrentHashMap();
    private int mNetId = -1;
    private NetworkSliceCallback mNetworkCallback;
    private int mNetworkCapability;
    private NetworkRequest mNetworkRequest;
    private Map<Integer, NetworkRequest> mNetworkRequests = new ConcurrentHashMap();
    private RouteSelectionDescriptor mRouteSelectionDescriptor;
    private Map<TrafficDescriptors, SliceRouteInfo> mSliceRouteInfos = new ConcurrentHashMap();
    private TrafficDescriptors mTempTrafficDescriptors;

    public enum ParaType {
        NETWORK_CALLBACK,
        NETWORK_REQUEST,
        ROUTE_SELECTION_DESCRIPTOR,
        NETWORK_REQUEST_ID
    }

    public boolean isBindCompleted(int uid, FqdnIps fqdnIps, TrafficDescriptors tds) {
        if (tds == null) {
            return true;
        }
        SliceRouteInfo sri = this.mSliceRouteInfos.get(tds);
        if (sri == null) {
            return false;
        }
        return sri.isBindCompleted(uid, fqdnIps, tds.getRouteBindType());
    }

    public void clear() {
        this.mNetId = -1;
        this.mNetworkCallback = null;
        this.mTempTrafficDescriptors = null;
        this.mRouteSelectionDescriptor = null;
        this.mNetworkRequests.clear();
        this.mMessengers.clear();
        this.mSliceRouteInfos.clear();
    }

    public void clearUsedUids() {
        for (SliceRouteInfo sri : this.mSliceRouteInfos.values()) {
            sri.clearUsedUids();
        }
    }

    public void clearUids() {
        for (SliceRouteInfo sri : this.mSliceRouteInfos.values()) {
            sri.clearUids();
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.intellicom.networkslice.model.NetworkSliceInfo$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$intellicom$networkslice$model$NetworkSliceInfo$ParaType = new int[ParaType.values().length];

        static {
            try {
                $SwitchMap$com$android$server$intellicom$networkslice$model$NetworkSliceInfo$ParaType[ParaType.NETWORK_REQUEST.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$intellicom$networkslice$model$NetworkSliceInfo$ParaType[ParaType.NETWORK_CALLBACK.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$server$intellicom$networkslice$model$NetworkSliceInfo$ParaType[ParaType.ROUTE_SELECTION_DESCRIPTOR.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$server$intellicom$networkslice$model$NetworkSliceInfo$ParaType[ParaType.NETWORK_REQUEST_ID.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public boolean isRightNetworkSlice(Object o, ParaType type) {
        try {
            int i = AnonymousClass1.$SwitchMap$com$android$server$intellicom$networkslice$model$NetworkSliceInfo$ParaType[type.ordinal()];
            if (i != 1) {
                if (i != 2) {
                    if (i != 3) {
                        if (!(i == 4 && (o instanceof Integer))) {
                            return false;
                        }
                        int reqId = ((Integer) o).intValue();
                        for (Integer num : this.mNetworkRequests.keySet()) {
                            if (reqId == num.intValue()) {
                                return true;
                            }
                        }
                        return false;
                    } else if (o != null) {
                        return ((RouteSelectionDescriptor) o).equals(getRouteSelectionDescriptor());
                    } else {
                        if (getRouteSelectionDescriptor() == null) {
                            return true;
                        }
                        return false;
                    }
                } else if (o != null) {
                    return ((NetworkSliceCallback) o).equals(getNetworkCallback());
                } else {
                    if (getNetworkCallback() == null) {
                        return true;
                    }
                    return false;
                }
            } else if (o != null) {
                return ((NetworkRequest) o).equals(getNetworkRequest());
            } else {
                if (getNetworkRequest() == null) {
                    return true;
                }
                return false;
            }
        } catch (ClassCastException e) {
            return false;
        }
    }

    public void putCreatedNetworkRequest(NetworkRequest networkRequest) {
        NetworkRequestExt networkRequestExt = new NetworkRequestExt();
        networkRequestExt.setNetworkRequest(networkRequest);
        this.mNetworkRequests.put(Integer.valueOf(networkRequestExt.getRequestId()), networkRequest);
    }

    public NetworkRequest getNetworkRequestByRequestId(int requestId) {
        return this.mNetworkRequests.get(Integer.valueOf(requestId));
    }

    public void mergeFqdnIps(FqdnIps newFqdnIps, TrafficDescriptors tds) {
        SliceRouteInfo sri = getSliceRouteInfo(tds);
        if (sri != null && sri.getFqdnIps() != null) {
            sri.getFqdnIps().mergeFqdnIps(newFqdnIps);
        }
    }

    public void addMessenger(int requestId, Messenger messenger) {
        this.mMessengers.put(Integer.valueOf(requestId), messenger);
    }

    public Messenger getMessenger(int requestId) {
        return this.mMessengers.get(Integer.valueOf(requestId));
    }

    public void clearWaittingFqdnIps(TrafficDescriptors tds) {
        SliceRouteInfo sri = getSliceRouteInfo(tds);
        if (sri != null) {
            sri.clearWaittingFqdnIps();
        }
    }

    public boolean isMatchAll() {
        RouteSelectionDescriptor routeSelectionDescriptor = this.mRouteSelectionDescriptor;
        if (routeSelectionDescriptor == null) {
            return false;
        }
        return routeSelectionDescriptor.isMatchAll();
    }

    public boolean isIpTriad(TrafficDescriptors tds) {
        if (tds == null) {
            return false;
        }
        return tds.isIpTriad();
    }

    public int getNetId() {
        return this.mNetId;
    }

    public void setNetId(int netId) {
        this.mNetId = netId;
    }

    public int getNetworkCapability() {
        return this.mNetworkCapability;
    }

    public void setNetworkCapability(int networkCapability) {
        this.mNetworkCapability = networkCapability;
    }

    public NetworkSliceCallback getNetworkCallback() {
        return this.mNetworkCallback;
    }

    public void setNetworkCallback(NetworkSliceCallback networkCallback) {
        this.mNetworkCallback = networkCallback;
    }

    public NetworkRequest getNetworkRequest() {
        return this.mNetworkRequest;
    }

    public void setNetworkRequest(NetworkRequest networkRequest) {
        this.mNetworkRequest = networkRequest;
    }

    public RouteSelectionDescriptor getRouteSelectionDescriptor() {
        return this.mRouteSelectionDescriptor;
    }

    public void setRouteSelectionDescriptor(RouteSelectionDescriptor routeSelectionDescriptor) {
        this.mRouteSelectionDescriptor = routeSelectionDescriptor;
    }

    public Map<Integer, Messenger> getMessengers() {
        return this.mMessengers;
    }

    public Set<Integer> getUsedUids(TrafficDescriptors tds) {
        SliceRouteInfo sri = getSliceRouteInfo(tds);
        if (sri != null) {
            return sri.getUsedUids();
        }
        return null;
    }

    public void addUsedUid(int uid, TrafficDescriptors tds) {
        SliceRouteInfo sri = getSliceRouteInfo(tds);
        if (sri != null) {
            sri.addUsedUid(uid);
        }
    }

    public void removeUsedUid(int uid, TrafficDescriptors tds) {
        SliceRouteInfo sri = getSliceRouteInfo(tds);
        if (sri != null) {
            sri.removeUsedUid(uid);
        }
    }

    public boolean isInUsedUids(int uid, TrafficDescriptors tds) {
        SliceRouteInfo sri = getSliceRouteInfo(tds);
        if (sri != null) {
            return sri.getUsedUids().contains(Integer.valueOf(uid));
        }
        return false;
    }

    public boolean isUsedUidEmpty(TrafficDescriptors tds) {
        SliceRouteInfo sri = getSliceRouteInfo(tds);
        if (sri != null) {
            return sri.getUsedUids().isEmpty();
        }
        return true;
    }

    public void addUsedUids(Set<Integer> uids, TrafficDescriptors tds) {
        SliceRouteInfo sri = getSliceRouteInfo(tds);
        if (!(sri == null || uids == null)) {
            for (Integer num : uids) {
                sri.addUsedUid(num.intValue());
            }
        }
    }

    public Set<Integer> getUids(TrafficDescriptors tds) {
        SliceRouteInfo sri = getSliceRouteInfo(tds);
        if (sri != null) {
            return sri.getUids();
        }
        return null;
    }

    public void addUid(int uid, TrafficDescriptors tds) {
        SliceRouteInfo sri = getSliceRouteInfo(tds);
        if (sri != null) {
            sri.addUid(uid);
        }
    }

    public void addUids(Set<Integer> uids, TrafficDescriptors tds) {
        SliceRouteInfo sri = getSliceRouteInfo(tds);
        if (!(sri == null || uids == null)) {
            for (Integer num : uids) {
                sri.addUid(num.intValue());
            }
        }
    }

    public void removeUid(int uid, TrafficDescriptors tds) {
        SliceRouteInfo sri = getSliceRouteInfo(tds);
        if (sri != null) {
            sri.removeUid(uid);
        }
    }

    public void replaceUids(TrafficDescriptors tds, Set<Integer> uids) {
        SliceRouteInfo sri = getSliceRouteInfo(tds);
        if (sri != null && sri.getUids() != null) {
            sri.setUids(uids);
        }
    }

    public FqdnIps getFqdnIps(TrafficDescriptors tds) {
        SliceRouteInfo sri = getSliceRouteInfo(tds);
        if (sri != null) {
            return sri.getFqdnIps();
        }
        return null;
    }

    public void setFqdnIps(FqdnIps fqdnIps, TrafficDescriptors tds) {
        SliceRouteInfo sri = getSliceRouteInfo(tds);
        if (sri != null) {
            sri.setFqdnIps(fqdnIps);
        }
    }

    public Set<FqdnIps> getWaittingFqdnIps(TrafficDescriptors tds) {
        SliceRouteInfo sri = getSliceRouteInfo(tds);
        if (sri != null) {
            return sri.getWaittingFqdnIps();
        }
        return null;
    }

    public void setWaittingFqdnIps(Set<FqdnIps> waittingFqdnIps, TrafficDescriptors tds) {
        SliceRouteInfo sri = getSliceRouteInfo(tds);
        if (sri != null) {
            sri.setWaittingFqdnIps(waittingFqdnIps);
        }
    }

    public void cacheTrafficDescriptors(TrafficDescriptors tds) {
        this.mSliceRouteInfos.put(tds, new SliceRouteInfo());
    }

    public void setTempTrafficDescriptors(TrafficDescriptors tds) {
        this.mTempTrafficDescriptors = tds;
    }

    public TrafficDescriptors getTempTrafficDescriptors() {
        return this.mTempTrafficDescriptors;
    }

    public SliceRouteInfo getSliceRouteInfo(TrafficDescriptors tds) {
        if (tds == null) {
            return null;
        }
        return this.mSliceRouteInfos.get(tds);
    }

    public Map<TrafficDescriptors, SliceRouteInfo> getSliceRouteInfos() {
        return this.mSliceRouteInfos;
    }

    public boolean isUidRouteBindType(TrafficDescriptors tds) {
        if (tds == null) {
            return false;
        }
        return tds.isUidRouteBindType();
    }

    public void clearSliceRouteInfos() {
        this.mSliceRouteInfos.clear();
    }

    public Set<Integer> getSignedUids(TrafficDescriptors tds) {
        SliceRouteInfo sri = getSliceRouteInfo(tds);
        return sri != null ? sri.getSignedUids() : new HashSet();
    }

    public void addSignedUid(int uid, TrafficDescriptors tds) {
        SliceRouteInfo sri = getSliceRouteInfo(tds);
        if (sri != null) {
            sri.addSignedUid(uid);
        }
    }

    public void removeSignedUid(int uid, TrafficDescriptors tds) {
        SliceRouteInfo sri = getSliceRouteInfo(tds);
        if (sri != null) {
            sri.removeSignedUid(uid);
        }
    }

    public String toString() {
        return "NetworkSliceInfo{mNetId=" + this.mNetId + ", mNetworkCapability=" + this.mNetworkCapability + ", mNetworkRequest=" + this.mNetworkRequest + ", mRouteSelectionDescriptor=" + this.mRouteSelectionDescriptor + ", mSliceRouteInfos=" + this.mSliceRouteInfos + '}';
    }

    private void log(String msg) {
        Log.i(TAG, msg);
    }
}

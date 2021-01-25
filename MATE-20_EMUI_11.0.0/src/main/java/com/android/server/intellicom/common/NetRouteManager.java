package com.android.server.intellicom.common;

import android.common.HwFrameworkFactory;
import android.net.wifi.HwInnerNetworkManagerImpl;
import android.util.Slog;
import com.android.server.hidata.HwHidataJniAdapter;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import libcore.net.event.NetworkEventDispatcher;

public class NetRouteManager {
    private static final int BIND_SUCCESS = 0;
    private static final int DEFAULT_SOCKET_STRATEGY = 2;
    private static final int INVALID_NETID = -1;
    private static final int ROOT_UID = 0;
    private static final int SK_TCP_CLOSE = 1;
    private static final int SK_UDP_RX_ERROR = 4;
    private static final int SK_UDP_TX_ERROR = 2;
    private static final String TAG = "NetRouteManager";
    private static final int UNBIND_NETID = 0;
    private static NetRouteManager sInstance = null;
    private HwInnerNetworkManagerImpl mHwInnerNMImpl = HwFrameworkFactory.getHwInnerNetworkManager();
    private Map<Integer, Integer> mUidNetIdBindList = new ConcurrentHashMap();

    private NetRouteManager() {
    }

    public static NetRouteManager getInstance() {
        NetRouteManager netRouteManager;
        synchronized (NetRouteManager.class) {
            if (sInstance == null) {
                sInstance = new NetRouteManager();
            }
            netRouteManager = sInstance;
        }
        return netRouteManager;
    }

    public boolean bindUidProcessToNetwork(int netId, int uid) {
        if (netId <= -1) {
            loge("bindUidProcessToNetwork, invalid netId: " + netId);
            return false;
        } else if (uid <= 0) {
            loge("bindUidProcessToNetwork, invalid uid: " + uid);
            return false;
        } else if (!this.mUidNetIdBindList.containsKey(Integer.valueOf(uid)) || this.mUidNetIdBindList.get(Integer.valueOf(uid)).intValue() != netId) {
            return bindUidProcessToNetworkInner(netId, uid, false);
        } else {
            log("uid:" + uid + " already bind to netId:" + netId + ", return.");
            return true;
        }
    }

    public boolean unbindUidProcessToNetwork(int uid) {
        if (this.mUidNetIdBindList.containsKey(Integer.valueOf(uid))) {
            return bindUidProcessToNetworkInner(0, uid, true);
        }
        log("uid:" + uid + " already unbind, return.");
        return true;
    }

    public boolean unbindAllUidProcessToNetwork(int netId) {
        if (netId == -1) {
            return unbindAllUidProcess();
        }
        if (this.mUidNetIdBindList.isEmpty() || !this.mUidNetIdBindList.containsValue(Integer.valueOf(netId))) {
            log("all binded uids has unbinded, return");
            return true;
        }
        Iterator<Map.Entry<Integer, Integer>> it = this.mUidNetIdBindList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Integer> item = it.next();
            if (item.getValue().intValue() == netId) {
                if (!bindUidProcessToNetworkInner(0, item.getKey().intValue(), false)) {
                    loge("unbindAllUidProcessToNetwork uid:" + item.getKey() + " unbind failed, return.");
                    return false;
                }
                it.remove();
            }
        }
        log("unbindAllUidProcessToNetwork success.");
        return true;
    }

    public boolean isUidProcessBindedToNetwork(int netId, int uid) {
        if (netId == -1) {
            return this.mUidNetIdBindList.containsKey(Integer.valueOf(uid));
        }
        return this.mUidNetIdBindList.containsKey(Integer.valueOf(uid)) && this.mUidNetIdBindList.get(Integer.valueOf(uid)).intValue() == netId;
    }

    public boolean isAllUidProcessUnbindToNetwork(int netId) {
        if (netId == -1) {
            return this.mUidNetIdBindList.isEmpty();
        }
        return !this.mUidNetIdBindList.containsValue(Integer.valueOf(netId));
    }

    private boolean bindUidProcessToNetworkInner(int netId, int uid, boolean needRemove) {
        int ret = HwHidataJniAdapter.getInstance().bindUidProcessToNetwork(netId, uid);
        log("bindUidProcessToNetworkInner, uid bind/unbind result:" + ret);
        if (ret == 0) {
            if (netId == 0) {
                log("uid:" + uid + " unbind success.");
                if (needRemove) {
                    this.mUidNetIdBindList.remove(Integer.valueOf(uid));
                }
            } else {
                log("uid:" + uid + " bind to network:" + netId + " success.");
                this.mUidNetIdBindList.put(Integer.valueOf(uid), Integer.valueOf(netId));
            }
            InetAddress.clearDnsCache();
            NetworkEventDispatcher.getInstance().onNetworkConfigurationChanged();
            closeProcessSocketForUid(uid);
            return true;
        } else if (netId == 0) {
            loge("uid:" + uid + " unbind failed.");
            return false;
        } else {
            loge("uid:" + uid + " bind to:" + netId + " failed.");
            return false;
        }
    }

    private boolean unbindAllUidProcess() {
        if (this.mUidNetIdBindList.isEmpty()) {
            return true;
        }
        Iterator<Map.Entry<Integer, Integer>> it = this.mUidNetIdBindList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Integer> item = it.next();
            if (!bindUidProcessToNetworkInner(0, item.getKey().intValue(), false)) {
                loge("unbindAllUidProcess uid:" + item.getKey() + " unbind failed, return.");
                return false;
            }
            it.remove();
        }
        log("unbindAllUidProcess success.");
        return true;
    }

    private void closeProcessSocketForUid(int uid) {
        if (this.mHwInnerNMImpl == null || uid <= 0) {
            loge("Failed to close socket for invalid param");
        }
        log("closeProcessSocketForUid, uid:" + uid);
        this.mHwInnerNMImpl.closeSocketsForUid(uid);
        HwHidataJniAdapter.getInstance().handleSocketStrategy(2, uid);
    }

    private static void log(String s) {
        Slog.i(TAG, s);
    }

    private static void loge(String s) {
        Slog.e(TAG, s);
    }
}

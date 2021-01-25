package com.huawei.wifi2;

import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HwWifiIfaceManager {
    private static final String TAG = "HwWifiIfaceManager";
    private final Object mIfaceLock = new Object();
    private HashMap<Integer, HwWifiIface> mIfaces = new HashMap<>();
    private int mNextId;

    public HwWifiIface allocateIface(int type) {
        HwWifiIface iface;
        synchronized (this.mIfaceLock) {
            iface = new HwWifiIface(this.mNextId, type);
            this.mIfaces.put(Integer.valueOf(this.mNextId), iface);
            this.mNextId++;
        }
        return iface;
    }

    public HwWifiIface removeIface(int id) {
        HwWifiIface remove;
        synchronized (this.mIfaceLock) {
            remove = this.mIfaces.remove(Integer.valueOf(id));
        }
        return remove;
    }

    public HwWifiIface getIface(int id) {
        return this.mIfaces.get(Integer.valueOf(id));
    }

    public HwWifiIface getIface(String ifaceName) {
        synchronized (this.mIfaceLock) {
            for (HwWifiIface iface : this.mIfaces.values()) {
                if (TextUtils.equals(iface.name, ifaceName)) {
                    return iface;
                }
            }
            return null;
        }
    }

    public Iterator<Integer> getIfaceIdIter() {
        return this.mIfaces.keySet().iterator();
    }

    public boolean hasAnyIface() {
        return !this.mIfaces.isEmpty();
    }

    public boolean hasAnyIfaceOfType(int type) {
        synchronized (this.mIfaceLock) {
            for (HwWifiIface iface : this.mIfaces.values()) {
                if (iface.type == type) {
                    return true;
                }
            }
            return false;
        }
    }

    public HwWifiIface findAnyIfaceOfType(int type) {
        synchronized (this.mIfaceLock) {
            for (HwWifiIface iface : this.mIfaces.values()) {
                if (iface.type == type) {
                    return iface;
                }
            }
            return null;
        }
    }

    public boolean hasAnyStaIfaceForConnectivity() {
        return hasAnyIfaceOfType(1);
    }

    public boolean hasAnyStaIfaceForScan() {
        return hasAnyIfaceOfType(2);
    }

    public boolean hasAnyApIface() {
        return hasAnyIfaceOfType(0);
    }

    public String findAnyStaIfaceName() {
        HwWifiIface iface = findAnyIfaceOfType(1);
        if (iface == null) {
            iface = findAnyIfaceOfType(2);
        }
        if (iface == null) {
            return null;
        }
        return iface.name;
    }

    public String findAnyApIfaceName() {
        HwWifiIface iface = findAnyIfaceOfType(0);
        if (iface == null) {
            return null;
        }
        return iface.name;
    }

    public HwWifiIface removeExistingIface(int newIfaceId) {
        HwWifiIface removedIface;
        synchronized (this.mIfaceLock) {
            removedIface = null;
            if (this.mIfaces.size() > 2) {
                HwHiLog.i(TAG, false, "More than one existing interface found", new Object[0]);
            }
            Iterator<Map.Entry<Integer, HwWifiIface>> iter = this.mIfaces.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Integer, HwWifiIface> entry = iter.next();
                if (entry.getKey().intValue() != newIfaceId) {
                    removedIface = entry.getValue();
                    iter.remove();
                }
            }
        }
        return removedIface;
    }
}

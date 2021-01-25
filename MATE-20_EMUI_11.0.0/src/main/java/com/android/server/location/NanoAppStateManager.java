package com.android.server.location;

import android.hardware.contexthub.V1_0.HubAppInfo;
import android.hardware.location.NanoAppInstanceInfo;
import android.util.Log;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/* access modifiers changed from: package-private */
public class NanoAppStateManager {
    private static final boolean ENABLE_LOG_DEBUG = true;
    private static final String TAG = "NanoAppStateManager";
    private final HashMap<Integer, NanoAppInstanceInfo> mNanoAppHash = new HashMap<>();
    private int mNextHandle = 0;

    NanoAppStateManager() {
    }

    /* access modifiers changed from: package-private */
    public synchronized NanoAppInstanceInfo getNanoAppInstanceInfo(int nanoAppHandle) {
        return this.mNanoAppHash.get(Integer.valueOf(nanoAppHandle));
    }

    /* access modifiers changed from: package-private */
    public synchronized void foreachNanoAppInstanceInfo(Consumer<NanoAppInstanceInfo> consumer) {
        for (NanoAppInstanceInfo info : this.mNanoAppHash.values()) {
            consumer.accept(info);
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized int getNanoAppHandle(int contextHubId, long nanoAppId) {
        for (NanoAppInstanceInfo info : this.mNanoAppHash.values()) {
            if (info.getContexthubId() == contextHubId && info.getAppId() == nanoAppId) {
                return info.getHandle();
            }
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public synchronized void addNanoAppInstance(int contextHubId, long nanoAppId, int nanoAppVersion) {
        removeNanoAppInstance(contextHubId, nanoAppId);
        if (this.mNanoAppHash.size() == Integer.MAX_VALUE) {
            Log.e(TAG, "Error adding nanoapp instance: max limit exceeded");
            return;
        }
        int nanoAppHandle = this.mNextHandle;
        int i = 0;
        while (true) {
            if (i > Integer.MAX_VALUE) {
                break;
            }
            int i2 = 0;
            if (!this.mNanoAppHash.containsKey(Integer.valueOf(nanoAppHandle))) {
                this.mNanoAppHash.put(Integer.valueOf(nanoAppHandle), new NanoAppInstanceInfo(nanoAppHandle, nanoAppId, nanoAppVersion, contextHubId));
                if (nanoAppHandle != Integer.MAX_VALUE) {
                    i2 = nanoAppHandle + 1;
                }
                this.mNextHandle = i2;
            } else {
                if (nanoAppHandle != Integer.MAX_VALUE) {
                    i2 = nanoAppHandle + 1;
                }
                nanoAppHandle = i2;
                i++;
            }
        }
        Log.v(TAG, "Added app instance with handle " + nanoAppHandle + " to hub " + contextHubId + ": ID=0x" + Long.toHexString(nanoAppId) + ", version=0x" + Integer.toHexString(nanoAppVersion));
    }

    /* access modifiers changed from: package-private */
    public synchronized void removeNanoAppInstance(int contextHubId, long nanoAppId) {
        this.mNanoAppHash.remove(Integer.valueOf(getNanoAppHandle(contextHubId, nanoAppId)));
    }

    /* access modifiers changed from: package-private */
    public synchronized void updateCache(int contextHubId, List<HubAppInfo> nanoAppInfoList) {
        HashSet<Long> nanoAppIdSet = new HashSet<>();
        for (HubAppInfo appInfo : nanoAppInfoList) {
            handleQueryAppEntry(contextHubId, appInfo.appId, appInfo.version);
            nanoAppIdSet.add(Long.valueOf(appInfo.appId));
        }
        Iterator<NanoAppInstanceInfo> iterator = this.mNanoAppHash.values().iterator();
        while (iterator.hasNext()) {
            NanoAppInstanceInfo info = iterator.next();
            if (info.getContexthubId() == contextHubId && !nanoAppIdSet.contains(Long.valueOf(info.getAppId()))) {
                iterator.remove();
            }
        }
    }

    private void handleQueryAppEntry(int contextHubId, long nanoAppId, int nanoAppVersion) {
        int nanoAppHandle = getNanoAppHandle(contextHubId, nanoAppId);
        if (nanoAppHandle == -1) {
            addNanoAppInstance(contextHubId, nanoAppId, nanoAppVersion);
        } else if (this.mNanoAppHash.get(Integer.valueOf(nanoAppHandle)).getAppVersion() != nanoAppVersion) {
            this.mNanoAppHash.put(Integer.valueOf(nanoAppHandle), new NanoAppInstanceInfo(nanoAppHandle, nanoAppId, nanoAppVersion, contextHubId));
            Log.v(TAG, "Updated app instance with handle " + nanoAppHandle + " at hub " + contextHubId + ": ID=0x" + Long.toHexString(nanoAppId) + ", version=0x" + Integer.toHexString(nanoAppVersion));
        }
    }
}

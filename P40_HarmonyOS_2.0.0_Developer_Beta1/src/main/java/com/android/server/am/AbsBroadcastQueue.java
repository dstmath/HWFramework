package com.android.server.am;

import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

public abstract class AbsBroadcastQueue {
    public boolean enqueueProxyBroadcast(boolean isParallel, BroadcastRecord broadcastRecord, Object target) {
        return false;
    }

    public long proxyBroadcast(List<String> list, boolean isProxy) {
        return 0;
    }

    public long proxyBroadcastByPid(List<Integer> list, boolean isProxy) {
        return 0;
    }

    public void setProxyBroadcastActions(List<String> list) {
    }

    public void setActionExcludePkg(String action, String pkg) {
    }

    public void proxyBroadcastConfig(int type, String key, List<String> list) {
    }

    public void handleMaybeTimeoutBr() {
    }

    public AbsHwMtmBroadcastResourceManager getMtmBRManager() {
        return null;
    }

    public boolean getMtmBRManagerEnabled(int featureType) {
        return false;
    }

    public boolean uploadRadarMessage(int scene, Bundle data) {
        return false;
    }

    public ArrayList<Integer> getIawareDumpData() {
        return null;
    }

    public void reportMediaButtonToAware(BroadcastRecord broadcastRecord, Object target) {
    }
}

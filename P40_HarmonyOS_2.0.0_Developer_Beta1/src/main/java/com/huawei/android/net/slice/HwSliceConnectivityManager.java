package com.huawei.android.net.slice;

import android.net.ConnectivityManager;
import android.os.Binder;
import com.android.internal.util.Preconditions;
import huawei.android.net.HwConnectivityExManager;
import huawei.android.net.slice.AppInfoCallback;
import huawei.android.net.slice.NetworkSliceStateListener;
import huawei.android.net.slice.TrafficDescriptor;

public class HwSliceConnectivityManager {
    public static final int ERROR_CODE_ILLEGAL_INPUT = 2;
    public static final int ERROR_CODE_NOT_SUPPORT_NR_SLICE = 1;
    public static final int ERROR_CODE_NO_AVAILABLE_SLICE = 4;
    public static final int ERROR_CODE_PERMISSION_DENIED = 3;
    public static final int REQUEST_SUCCESS = 0;

    private HwSliceConnectivityManager() {
    }

    public static HwSliceConnectivityManager getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public boolean isNetworkSliceSupported() {
        return HwConnectivityExManager.getDefault().isNetworkSliceSupported();
    }

    public void initAppInfo(String appId, AppInfoCallback appInfoCallback) {
        Preconditions.checkNotNull(appInfoCallback, "appInfoCallback null");
        HwConnectivityExManager.getDefault().initAppInfo(appId, Binder.getCallingUid(), appInfoCallback);
    }

    public boolean registerListener(NetworkSliceStateListener networkSliceStateListener) {
        Preconditions.checkNotNull(networkSliceStateListener, "networkSliceStateListener null");
        return HwConnectivityExManager.getDefault().registerListener(Binder.getCallingUid(), networkSliceStateListener);
    }

    public boolean unregisterListener(NetworkSliceStateListener networkSliceStateListener) {
        Preconditions.checkNotNull(networkSliceStateListener, "networkSliceStateListener null");
        return HwConnectivityExManager.getDefault().unregisterListener(Binder.getCallingUid(), networkSliceStateListener);
    }

    public int requestNetworkSlice(TrafficDescriptor trafficDescriptor, ConnectivityManager.NetworkCallback networkCallback, int timeoutMs) {
        return HwConnectivityExManager.getDefault().requestNetworkSlice(Binder.getCallingUid(), trafficDescriptor, networkCallback, timeoutMs);
    }

    public boolean releaseNetworkSlice(ConnectivityManager.NetworkCallback networkCallback) {
        return HwConnectivityExManager.getDefault().releaseNetworkSlice(Binder.getCallingUid(), networkCallback);
    }

    private static class SingletonInstance {
        private static final HwSliceConnectivityManager INSTANCE = new HwSliceConnectivityManager();

        private SingletonInstance() {
        }
    }
}

package com.android.server;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.NetworkRequest;
import android.os.Binder;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Messenger;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.connectivity.usbp2p.UsbP2pManager;
import com.android.server.intellicom.common.HwAppStateObserver;
import com.android.server.intellicom.common.HwSettingsObserver;
import com.android.server.intellicom.common.NetLinkManager;
import com.android.server.intellicom.common.NetRouteManager;
import com.android.server.intellicom.common.PermissionHelper;
import com.android.server.intellicom.networkslice.HwNetworkSliceManager;
import com.android.server.intellicom.smartdualcard.SmartDualCardRecommendNotify;
import huawei.android.net.IConnectivityExManager;
import huawei.android.net.slice.IAppInfoCallback;
import huawei.android.net.slice.INetworkSliceStateListener;
import huawei.android.net.slice.TrafficDescriptor;
import huawei.com.android.server.HiEventProxy;

public class HwConnectivityExService extends IConnectivityExManager.Stub {
    private static final boolean IS_NR_SLICES_SUPPORTED = HwFrameworkFactory.getHwInnerTelephonyManager().isNrSlicesSupported();
    private static final boolean IS_SMART_DUAL_CARD_PROP_ENABLE = SystemProperties.getBoolean("ro.odm.smart_dual_card", false);
    private static final String TAG = "HwConnectivityExService";
    private static final int UNBIND_NETID = 0;
    private static String mSmartKeyguardLevel = "normal_level";
    private static boolean useCtrlSocket = SystemProperties.getBoolean("ro.config.hw_useCtrlSocket", false);
    private final Context mContext;
    private boolean mIsFixedAddress = false;
    private final INetworkManagementService mNms;
    private final UsbP2pManager mUsbP2pManager;

    public HwConnectivityExService(Context context) {
        this.mContext = context;
        this.mNms = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        this.mUsbP2pManager = UsbP2pManager.getInstance();
        this.mUsbP2pManager.init(context, this.mNms);
        if (IS_NR_SLICES_SUPPORTED) {
            HwAppStateObserver.getInstance().register(this.mContext);
            PermissionHelper.getInstance().init(this.mContext);
            HiEventProxy.getInstance().init(this.mContext);
        }
        if (IS_SMART_DUAL_CARD_PROP_ENABLE) {
            HwSettingsObserver.getInstance().init(this.mContext);
            NetLinkManager.getInstance().init(this.mContext);
            SmartDualCardRecommendNotify.getInstance().init(this.mContext);
        }
    }

    public void setSmartKeyguardLevel(String level) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        setStaticSmartKeyguardLevel(level);
    }

    private static void setStaticSmartKeyguardLevel(String level) {
        mSmartKeyguardLevel = level;
        Slog.d(TAG, "set mSmartKeyguardLevel = " + mSmartKeyguardLevel);
    }

    public void setApIpv4AddressFixed(boolean isFixed) {
        Slog.d(TAG, "Calling pid is " + Binder.getCallingPid() + " isFixed " + isFixed);
        int checkCallingOrSelfPermission = this.mContext.checkCallingOrSelfPermission("com.huawei.wifi.permission.WIFI_APIPV4FIXED");
        this.mContext.getPackageManager();
        if (checkCallingOrSelfPermission != 0) {
            Slog.e(TAG, "No com.huawei.wifi.permission.WIFI_APIPV4FIXED permission");
        } else {
            this.mIsFixedAddress = isFixed;
        }
    }

    public boolean isApIpv4AddressFixed() {
        Slog.d(TAG, "Calling pid is " + Binder.getCallingPid() + " isFixed " + this.mIsFixedAddress);
        int checkCallingOrSelfPermission = this.mContext.checkCallingOrSelfPermission("com.huawei.wifi.permission.WIFI_APIPV4FIXED");
        this.mContext.getPackageManager();
        if (checkCallingOrSelfPermission == 0) {
            return this.mIsFixedAddress;
        }
        Slog.e(TAG, "No com.huawei.wifi.permission.WIFI_APIPV4FIXED permission");
        return false;
    }

    private static void setUseCtrlSocketStatic(boolean flag) {
        useCtrlSocket = flag;
    }

    public void setUseCtrlSocket(boolean flag) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        setUseCtrlSocketStatic(flag);
        Slog.d(TAG, "set useCtrlSocket.");
    }

    public boolean bindUidProcessToNetwork(int netId, int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (netId != 0) {
            return NetRouteManager.getInstance().bindUidProcessToNetwork(netId, uid);
        }
        return NetRouteManager.getInstance().unbindUidProcessToNetwork(uid);
    }

    public boolean unbindAllUidProcessToNetwork(int netId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        return NetRouteManager.getInstance().unbindAllUidProcessToNetwork(netId);
    }

    public boolean isUidProcessBindedToNetwork(int netId, int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        return NetRouteManager.getInstance().isUidProcessBindedToNetwork(netId, uid);
    }

    public boolean isAllUidProcessUnbindToNetwork(int netId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        return NetRouteManager.getInstance().isAllUidProcessUnbindToNetwork(netId);
    }

    public int getNetIdBySlotId(int slotId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        return NetLinkManager.getInstance().getNetIdBySlotId(slotId);
    }

    public boolean isNetworkSliceSupported() {
        return IS_NR_SLICES_SUPPORTED;
    }

    public void initAppInfo(String appId, int uid, IAppInfoCallback appInfoCallback) {
        PermissionHelper.getInstance().initAppInfo(appId, uid, appInfoCallback);
    }

    public boolean registerListener(int uid, INetworkSliceStateListener networkSliceStateListener) {
        if (hasPermissionForSlice(uid)) {
            return HwNetworkSliceManager.getInstance().registerListener(networkSliceStateListener);
        }
        return false;
    }

    public boolean unregisterListener(int uid, INetworkSliceStateListener networkSliceStateListener) {
        if (hasPermissionForSlice(uid)) {
            return HwNetworkSliceManager.getInstance().unregisterListener(networkSliceStateListener);
        }
        return false;
    }

    public NetworkRequest requestNetworkSlice(int uid, TrafficDescriptor trafficDescriptor, Messenger messenger, int timeoutMs) {
        if (!hasPermissionForSlice(uid)) {
            return null;
        }
        return HwNetworkSliceManager.getInstance().requestNetworkSliceForSignedApp(uid, trafficDescriptor, messenger, timeoutMs);
    }

    public boolean releaseNetworkSlice(int uid, int requestId) {
        if (!hasPermissionForSlice(uid)) {
            return false;
        }
        return HwNetworkSliceManager.getInstance().releaseNetworkSliceBySignedApp(uid, requestId);
    }

    public int getUsbP2pState() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", "No permissions to getUsbP2pState");
        return this.mUsbP2pManager.getUsbP2pState();
    }

    public int requestForUsbP2p(int requestId, Messenger messenger, IBinder binder) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_NETWORK_STATE", "No permissions to requestForUsbP2p");
        return this.mUsbP2pManager.requestForUsbP2p(requestId, messenger, binder);
    }

    public int listenForUsbP2p(int listenId, Messenger messenger, IBinder binder) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", "No permissions to listenForUsbP2p");
        return this.mUsbP2pManager.listenForUsbP2p(listenId, messenger, binder);
    }

    public void releaseUsbP2pRequest(int requestId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", "No permissions to releaseUsbP2pRequest");
        this.mUsbP2pManager.releaseUsbP2pRequest(requestId);
    }

    public boolean hasPermissionForSlice(int uid) {
        boolean hasKitPermission = PermissionHelper.getInstance().hasPermission(uid);
        boolean hasAccessNetworkStatePermission = true;
        try {
            this.mContext.enforceCallingPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        } catch (SecurityException e) {
            hasAccessNetworkStatePermission = false;
        }
        return hasAccessNetworkStatePermission || hasKitPermission;
    }
}

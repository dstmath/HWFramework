package com.android.server.display;

import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.pc.IHwPCManager;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.display.DisplayAdapter;
import com.huawei.android.hardware.display.HwWifiDisplayParameters;
import java.util.ArrayList;

public final class HwDisplayManagerServiceEx implements IHwDisplayManagerServiceEx {
    private static final String HW_VR_SUFFIX = "-Src";
    private static final boolean IS_VR_ENABLE = SystemProperties.getBoolean("ro.vr.surport", false);
    static final String TAG = "HwDisplayManagerServiceEx";
    private int mChannelId;
    final Context mContext;
    IHwDisplayManagerInner mDmsInner = null;
    private DefaultHwVrDisplayAdapter mHwVrDisplayAdapter;
    private HwWifiDisplayParameters mHwWifiDisplayParameters;

    public HwDisplayManagerServiceEx(IHwDisplayManagerInner dms, Context context) {
        this.mDmsInner = dms;
        this.mContext = context;
        this.mChannelId = 0;
    }

    public void startWifiDisplayScan(int channelId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to start wifi display scans");
        int callingPid = Binder.getCallingPid();
        long token = Binder.clearCallingIdentity();
        try {
            this.mChannelId = channelId;
            this.mDmsInner.startWifiDisplayScanInner(callingPid, channelId);
        } finally {
            Binder.restoreCallingIdentity(token);
            this.mChannelId = 0;
        }
    }

    private void connectWifiDisplayInternal(String deviceAddress, HwWifiDisplayParameters parameters) {
        synchronized (this.mDmsInner.getLock()) {
            if (this.mDmsInner.getWifiDisplayAdapter() != null) {
                this.mDmsInner.getWifiDisplayAdapter().requestConnectLocked(deviceAddress, parameters);
            }
        }
    }

    public void connectWifiDisplay(String address, HwWifiDisplayParameters parameters) {
        if (address != null) {
            this.mContext.enforceCallingPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to connect to a wifi display");
            long token = Binder.clearCallingIdentity();
            try {
                connectWifiDisplayInternal(address, parameters);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new IllegalArgumentException("address must not be null");
        }
    }

    private void checkVerificationResultInternal(boolean isRight) {
        synchronized (this.mDmsInner.getLock()) {
            if (this.mDmsInner.getWifiDisplayAdapter() != null) {
                this.mDmsInner.getWifiDisplayAdapter().checkVerificationResultLocked(isRight);
            }
        }
    }

    public void checkVerificationResult(boolean isRight) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to start wifi display play");
        long token = Binder.clearCallingIdentity();
        try {
            checkVerificationResultInternal(isRight);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean checkPermissionForHwMultiDisplay(int uid) {
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager == null) {
            return false;
        }
        try {
            if (pcManager.checkPermissionForHwMultiDisplay(uid)) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            HwPCUtils.log(TAG, "RemoteException checkPermissionForHwMultiDisplay");
            return false;
        }
    }

    private void sendWifiDisplayActionInternal(String action) {
        synchronized (this.mDmsInner.getLock()) {
            if (this.mDmsInner.getWifiDisplayAdapter() != null) {
                this.mDmsInner.getWifiDisplayAdapter().sendWifiDisplayActionLocked(action);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean sendWifiDisplayAction(String action) {
        this.mContext.enforceCallingPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to send wifi display action");
        if (!"HWE_DLNA_START".equals(action) && !"HWE_DLNA_STOP".equals(action)) {
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            sendWifiDisplayActionInternal(action);
            Binder.restoreCallingIdentity(token);
            return true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    public void setHwWifiDisplayParameters(HwWifiDisplayParameters parameters) {
        this.mHwWifiDisplayParameters = parameters;
    }

    public HwWifiDisplayParameters getHwWifiDisplayParameters() {
        if (this.mHwWifiDisplayParameters == null) {
            this.mHwWifiDisplayParameters = new HwWifiDisplayParameters();
        }
        return this.mHwWifiDisplayParameters;
    }

    public boolean registerHwVrDisplayAdapterIfNeedLocked(ArrayList<DisplayAdapter> adapters, Handler handler, DisplayAdapter.Listener listener, Handler uiHandler) {
        Slog.e(TAG, "registerHwVrDisplayAdapterIfNeedLocked start");
        if (adapters == null || handler == null || listener == null || uiHandler == null || !IS_VR_ENABLE) {
            return false;
        }
        this.mHwVrDisplayAdapter = HwVrDisplayServiceFactory.loadFactory().getHwVrDisplayAdapter(DisplayManagerServiceExUtils.createSyncRootEx(this.mDmsInner.getLock()), this.mContext, handler, DisplayAdapterExUtils.createListenerEx(listener), uiHandler);
        adapters.add(DisplayAdapterBridgeUtils.createDisplayAdapterBridge(this.mHwVrDisplayAdapter));
        this.mHwVrDisplayAdapter.registerLocked();
        return true;
    }

    public LogicalDisplay getVrVirtualDisplayIfNeed(SparseArray<LogicalDisplay> logicalDisplays, String displayName, LogicalDisplay display) {
        String name;
        if (logicalDisplays == null) {
            return display;
        }
        int i = logicalDisplays.size();
        while (true) {
            int i2 = i - 1;
            if (i <= 0) {
                return display;
            }
            LogicalDisplay logicalDisplay = logicalDisplays.valueAt(i2);
            if (!(logicalDisplay == null || (name = logicalDisplay.getDisplayInfoLocked().name) == null)) {
                if ((displayName + HW_VR_SUFFIX).equals(name)) {
                    return logicalDisplay;
                }
            }
            i = i2;
        }
    }

    public boolean createVrDisplay(String displayName, int[] displayParams) {
        DefaultHwVrDisplayAdapter defaultHwVrDisplayAdapter = this.mHwVrDisplayAdapter;
        if (defaultHwVrDisplayAdapter == null) {
            return false;
        }
        return defaultHwVrDisplayAdapter.createVrDisplay(displayName, displayParams);
    }

    public boolean destroyVrDisplay(String displayName) {
        DefaultHwVrDisplayAdapter defaultHwVrDisplayAdapter = this.mHwVrDisplayAdapter;
        if (defaultHwVrDisplayAdapter == null) {
            return false;
        }
        return defaultHwVrDisplayAdapter.destroyVrDisplay(displayName);
    }

    public boolean destroyAllVrDisplay() {
        DefaultHwVrDisplayAdapter defaultHwVrDisplayAdapter = this.mHwVrDisplayAdapter;
        if (defaultHwVrDisplayAdapter == null) {
            return false;
        }
        return defaultHwVrDisplayAdapter.destroyAllVrDisplay();
    }
}

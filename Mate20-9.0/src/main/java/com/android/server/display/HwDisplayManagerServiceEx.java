package com.android.server.display;

import android.content.Context;
import android.os.Binder;

public final class HwDisplayManagerServiceEx implements IHwDisplayManagerServiceEx {
    static final String TAG = "HwDisplayManagerServiceEx";
    private int mChannelId;
    final Context mContext;
    IHwDisplayManagerInner mIDmsInner = null;

    public HwDisplayManagerServiceEx(IHwDisplayManagerInner dms, Context context) {
        this.mIDmsInner = dms;
        this.mContext = context;
        this.mChannelId = 0;
    }

    public void startWifiDisplayScan(int channelId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to start wifi display scans");
        int callingPid = Binder.getCallingPid();
        long token = Binder.clearCallingIdentity();
        try {
            this.mChannelId = channelId;
            this.mIDmsInner.startWifiDisplayScanInner(callingPid, channelId);
        } finally {
            Binder.restoreCallingIdentity(token);
            this.mChannelId = 0;
        }
    }

    private void connectWifiDisplayInternal(String deviceAddress, String verificaitonCode) {
        synchronized (this.mIDmsInner.getLock()) {
            if (this.mIDmsInner.getWifiDisplayAdapter() != null) {
                this.mIDmsInner.getWifiDisplayAdapter().requestConnectLocked(deviceAddress, verificaitonCode);
            }
        }
    }

    public void connectWifiDisplay(String address, String verificaitonCode) {
        if (address != null) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to connect to a wifi display");
            long token = Binder.clearCallingIdentity();
            try {
                connectWifiDisplayInternal(address, verificaitonCode);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new IllegalArgumentException("address must not be null");
        }
    }

    private void checkVerificationResultInternal(boolean isRight) {
        synchronized (this.mIDmsInner.getLock()) {
            if (this.mIDmsInner.getWifiDisplayAdapter() != null) {
                this.mIDmsInner.getWifiDisplayAdapter().checkVerificationResultLocked(isRight);
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

    private void sendWifiDisplayActionInternal(String action) {
        synchronized (this.mIDmsInner.getLock()) {
            if (this.mIDmsInner.getWifiDisplayAdapter() != null) {
                this.mIDmsInner.getWifiDisplayAdapter().sendWifiDisplayActionLocked(action);
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
}

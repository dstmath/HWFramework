package com.android.server.display;

import android.os.RemoteException;
import android.pc.IHwPCManager;
import android.util.HwPCUtils;
import android.util.Slog;

public class HwWifiDisplayAdapterEx implements IHwWifiDisplayAdapterEx {
    public static final String TAG = "HwWifiDisplayAdapterEx";
    IWifiDisplayAdapterInner mWfda;

    public HwWifiDisplayAdapterEx(IWifiDisplayAdapterInner wfda) {
        this.mWfda = wfda;
    }

    public void requestStartScanLocked(final int channelId) {
        this.mWfda.getHandlerInner().post(new Runnable() {
            public void run() {
                if (HwWifiDisplayAdapterEx.this.mWfda.getmDisplayControllerInner() != null) {
                    HwWifiDisplayAdapterEx.this.mWfda.getmDisplayControllerInner().requestStartScan(channelId);
                }
            }
        });
    }

    public void setConnectParameters(String address) {
        boolean isSupportHdcp = this.mWfda.getmPersistentDataStoreInner().isHdcpSupported(address);
        boolean isUibcError = this.mWfda.getmPersistentDataStoreInner().isUibcException(address);
        Slog.d(TAG, "requestConnectLocked: isSupportHdcp " + isSupportHdcp + ", uibc error " + isUibcError);
        this.mWfda.getmDisplayControllerInner().setConnectParameters(isSupportHdcp, isUibcError, null);
    }

    public void requestConnectLocked(final String address, final String verificaitonCode) {
        this.mWfda.getHandlerInner().post(new Runnable() {
            public void run() {
                if (HwWifiDisplayAdapterEx.this.mWfda.getmDisplayControllerInner() != null) {
                    HwWifiDisplayAdapterEx.this.mWfda.getmDisplayControllerInner().requestConnect(address);
                    boolean isSupportHdcp = HwWifiDisplayAdapterEx.this.mWfda.getmPersistentDataStoreInner().isHdcpSupported(address);
                    boolean isUibcError = HwWifiDisplayAdapterEx.this.mWfda.getmPersistentDataStoreInner().isUibcException(address);
                    Slog.d(HwWifiDisplayAdapterEx.TAG, "requestConnectLocked: isSupportHdcp " + isSupportHdcp + ", uibc error " + isUibcError);
                    HwWifiDisplayAdapterEx.this.mWfda.getmDisplayControllerInner().setConnectParameters(isSupportHdcp, isUibcError, verificaitonCode);
                }
            }
        });
    }

    public void checkVerificationResultLocked(final boolean isRight) {
        this.mWfda.getHandlerInner().post(new Runnable() {
            public void run() {
                if (HwWifiDisplayAdapterEx.this.mWfda.getmDisplayControllerInner() != null) {
                    HwWifiDisplayAdapterEx.this.mWfda.getmDisplayControllerInner().checkVerificationResult(isRight);
                }
            }
        });
    }

    public void sendWifiDisplayActionLocked(final String action) {
        this.mWfda.getHandlerInner().post(new Runnable() {
            public void run() {
                if (HwWifiDisplayAdapterEx.this.mWfda.getmDisplayControllerInner() != null) {
                    HwWifiDisplayAdapterEx.this.mWfda.getmDisplayControllerInner().sendWifiDisplayAction(action);
                }
            }
        });
    }

    public void LaunchMKForWifiMode() {
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.LaunchMKForWifiMode();
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "RemoteException LaunchMKForWifiMode");
            }
        }
    }
}

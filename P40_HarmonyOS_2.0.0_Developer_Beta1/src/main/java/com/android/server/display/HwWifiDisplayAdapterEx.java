package com.android.server.display;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.display.WifiDisplay;
import android.os.RemoteException;
import android.os.UserHandle;
import android.pc.IHwPCManager;
import android.util.HwPCUtils;
import android.util.Slog;
import com.android.server.LocalServices;
import com.huawei.android.hardware.display.HwWifiDisplayParameters;

public class HwWifiDisplayAdapterEx implements IHwWifiDisplayAdapterEx {
    private static final String ACTION_DISPLAY_DATA = "com.huawei.hardware.display.action.DISPLAY_DATA";
    private static final String ACTION_WIFI_DISPLAY_CASTING = "com.huawei.hardware.display.action.WIFI_DISPLAY_CASTING";
    private static final int BASE_HIGH = 1080;
    private static final String EXTRA_DISPLAY_DATA = "com.huawei.airsharing.extra.EXTRA_DISPLAY_DATA";
    private static final int MSG_SEND_CAST_CHANGE_BROADCAST = 2;
    private static final String SEND_AIRSHARING_BROADCAST = "com.huawei.android.airsharing";
    private static final String TAG = "HwWifiDisplayAdapterEx";
    private static final String WIFI_DISPLAY_CASTING_PERMISSION = "com.huawei.wfd.permission.ACCESS_WIFI_DISPLAY_CASTING";
    private int mConnectionFailedReason = -1;
    private final Context mContext;
    private IWifiDisplayAdapterInner mWfda;

    public HwWifiDisplayAdapterEx(IWifiDisplayAdapterInner wfda, Context context) {
        this.mWfda = wfda;
        this.mContext = context;
    }

    public void requestStartScanLocked(final int channelId) {
        this.mWfda.getHandlerInner().post(new Runnable() {
            /* class com.android.server.display.HwWifiDisplayAdapterEx.AnonymousClass1 */

            @Override // java.lang.Runnable
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
        this.mWfda.getmDisplayControllerInner().setConnectParameters(isSupportHdcp, isUibcError, (HwWifiDisplayParameters) null);
    }

    public void requestConnectLocked(final String address, final HwWifiDisplayParameters parameters) {
        this.mWfda.getHandlerInner().post(new Runnable() {
            /* class com.android.server.display.HwWifiDisplayAdapterEx.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                if (HwWifiDisplayAdapterEx.this.mWfda.getmDisplayControllerInner() != null) {
                    HwWifiDisplayAdapterEx.this.mWfda.getmDisplayControllerInner().requestConnect(address);
                    boolean isSupportHdcp = HwWifiDisplayAdapterEx.this.mWfda.getmPersistentDataStoreInner().isHdcpSupported(address);
                    boolean isUibcError = HwWifiDisplayAdapterEx.this.mWfda.getmPersistentDataStoreInner().isUibcException(address);
                    Slog.d(HwWifiDisplayAdapterEx.TAG, "requestConnectLocked: isSupportHdcp " + isSupportHdcp + ", uibc error " + isUibcError);
                    HwWifiDisplayAdapterEx.this.mWfda.getmDisplayControllerInner().setConnectParameters(isSupportHdcp, isUibcError, parameters);
                }
            }
        });
    }

    public void setConnectionFailedReason(int reason) {
        this.mConnectionFailedReason = reason;
        if (this.mWfda.getmActiveDisplayInner() != null && reason == 8) {
            this.mWfda.getmPersistentDataStoreInner().addUibcExceptionDevice(this.mWfda.getmActiveDisplayInner().getDeviceAddress());
        }
    }

    public void displayCasting(WifiDisplay display) {
        WifiDisplay display2 = this.mWfda.getmPersistentDataStoreInner().applyWifiDisplayAlias(display);
        if (this.mWfda.getmActiveDisplayStateInner() != 2 || this.mWfda.getmActiveDisplayInner() == null || !this.mWfda.getmActiveDisplayInner().equals(display2)) {
            Slog.d(TAG, "onDisplayCasting mActiveDisplayState " + this.mWfda.getmActiveDisplayStateInner());
            return;
        }
        Slog.d(TAG, "onDisplayCasting .....");
        this.mWfda.getHandlerInner().sendEmptyMessage(2);
        this.mWfda.getHandlerInner().post($$Lambda$HwWifiDisplayAdapterEx$Kbe90E34RyMBC7xQuUwr0AhFn54.INSTANCE);
    }

    static /* synthetic */ void lambda$displayCasting$0() {
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.LaunchMKForWifiMode();
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "RemoteException LaunchMKForWifiMode");
            }
        }
    }

    public int getConnectionFailReason(boolean isReset) {
        int failedReason = this.mConnectionFailedReason;
        if (isReset) {
            this.mConnectionFailedReason = -1;
        }
        if (failedReason != -1) {
            Slog.d(TAG, "handleSendStatusChangeBroadcast, connection failed reason is " + failedReason);
        }
        return failedReason;
    }

    public void handleSendCastingBroadcast() {
        Intent intent = new Intent(ACTION_WIFI_DISPLAY_CASTING);
        intent.addFlags(1073741824);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, WIFI_DISPLAY_CASTING_PERMISSION);
    }

    public void handleSendDisplayDataBroadcast(String inputDataType) {
        Slog.d(TAG, "display data ready to send receives");
        Intent intent = new Intent(ACTION_DISPLAY_DATA);
        intent.putExtra(EXTRA_DISPLAY_DATA, inputDataType);
        intent.setPackage(SEND_AIRSHARING_BROADCAST);
        intent.addFlags(1073741824);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, WIFI_DISPLAY_CASTING_PERMISSION);
    }

    public void updateDensityForPcMode(DisplayDeviceInfo info) {
        if (info != null && HwPCUtils.enabled()) {
            info.densityDpi = ((info.width < info.height ? info.width : info.height) * 160) / BASE_HIGH;
            Slog.i(TAG, "PC mode densityDpi:" + info.densityDpi);
            info.xDpi = (float) info.densityDpi;
            info.yDpi = (float) info.densityDpi;
        }
    }

    public void checkVerificationResultLocked(final boolean isRight) {
        this.mWfda.getHandlerInner().post(new Runnable() {
            /* class com.android.server.display.HwWifiDisplayAdapterEx.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                if (HwWifiDisplayAdapterEx.this.mWfda.getmDisplayControllerInner() != null) {
                    HwWifiDisplayAdapterEx.this.mWfda.getmDisplayControllerInner().checkVerificationResult(isRight);
                }
            }
        });
    }

    public void sendWifiDisplayActionLocked(final String action) {
        this.mWfda.getHandlerInner().post(new Runnable() {
            /* class com.android.server.display.HwWifiDisplayAdapterEx.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                if (HwWifiDisplayAdapterEx.this.mWfda.getmDisplayControllerInner() != null) {
                    HwWifiDisplayAdapterEx.this.mWfda.getmDisplayControllerInner().sendWifiDisplayAction(action);
                }
            }
        });
    }

    public void setHwWifiDisplayParameters(HwWifiDisplayParameters parameters) {
        DisplayManagerInternal dmi = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
        if (dmi != null) {
            dmi.setHwWifiDisplayParameters(parameters);
        }
    }
}

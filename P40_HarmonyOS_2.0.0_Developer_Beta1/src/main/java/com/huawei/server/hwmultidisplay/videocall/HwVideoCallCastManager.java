package com.huawei.server.hwmultidisplay.videocall;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.HwPCUtils;
import android.view.Display;
import com.huawei.android.hardware.display.HwWifiDisplayParameters;
import com.huawei.android.hardware.display.IDisplayManagerExt;
import com.huawei.android.hardware.display.IHwDisplayManager;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.view.DisplayEx;
import com.huawei.server.hwmultidisplay.power.HwMultiDisplayPowerManager;

public class HwVideoCallCastManager {
    private static final Object LOCK = new Object();
    private static final String TAG = "HwVideoCallCastManager";
    private static volatile HwVideoCallCastManager sInstance = null;
    private boolean mIsVideoCallCastMode = false;
    private int mVideoCallDispalyId = -1;

    public static HwVideoCallCastManager getDefault() {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new HwVideoCallCastManager();
                }
            }
        }
        return sInstance;
    }

    public boolean isConnForVideoCall(Context context, int displayId) {
        IBinder binder;
        if (context == null || displayId == 0) {
            return false;
        }
        if (!this.mIsVideoCallCastMode || this.mVideoCallDispalyId == displayId) {
            if (isWifiDisplay(context, displayId) && (binder = ServiceManagerEx.getService("display")) != null) {
                long ident = Binder.clearCallingIdentity();
                try {
                    HwWifiDisplayParameters parameters = IHwDisplayManager.Stub.asInterface(IDisplayManagerExt.getHwInnerService(binder)).getHwWifiDisplayParameters();
                    if (parameters != null && (parameters.getProjectionScene() == 2 || parameters.getProjectionScene() == 4)) {
                        HwPCUtils.log(TAG, "isConnForVideoCall, return true.");
                        Binder.restoreCallingIdentity(ident);
                        return true;
                    }
                } catch (RemoteException e) {
                    HwPCUtils.log(TAG, "isConnForVideoCall, RemoteException.");
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
                Binder.restoreCallingIdentity(ident);
            }
            return false;
        }
        HwPCUtils.log(TAG, "isConnForVideoCall, Already connected to another display.");
        return false;
    }

    public boolean isDisplayForVideoCall(int displayId) {
        return this.mIsVideoCallCastMode && this.mVideoCallDispalyId == displayId;
    }

    public boolean isInCastModeForVideoCall() {
        return this.mIsVideoCallCastMode;
    }

    public void onDisplayAdded(Context context, int displayId, Handler handler) {
        HwPCUtils.log(TAG, "onDisplayAdded, displayId=" + displayId);
        this.mVideoCallDispalyId = displayId;
        this.mIsVideoCallCastMode = true;
    }

    public void onDisplayRemoved(Context context, int displayId) {
        HwPCUtils.log(TAG, "onDisplayRemoved, displayId=" + displayId);
        if (this.mIsVideoCallCastMode && this.mVideoCallDispalyId == displayId) {
            this.mIsVideoCallCastMode = false;
            this.mVideoCallDispalyId = -1;
            HwMultiDisplayPowerManager.getDefault().lockScreenWhenDisconnected();
        }
    }

    private boolean isWifiDisplay(Context context, int displayId) {
        Display display = ((DisplayManager) context.getSystemService("display")).getDisplay(displayId);
        return display != null && DisplayEx.getType(display) == 3;
    }
}

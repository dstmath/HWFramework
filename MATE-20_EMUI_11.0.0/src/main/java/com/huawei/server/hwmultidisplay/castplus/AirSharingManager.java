package com.huawei.server.hwmultidisplay.castplus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.HwPCUtils;
import android.view.Display;
import com.huawei.android.view.DisplayEx;
import com.huawei.hwpartpowerofficeservices.BuildConfig;

public class AirSharingManager {
    private static final String AIRSHARING_PKG_NAME = "com.huawei.android.airsharing";
    private static final String CAST_PLUS_VIRTUALDISPLAY_NAME = "CastPlusDisplay";
    private static final int DELAY_FOR_REBIND_SERVICE = 800;
    private static final int INVALID_DISPLAY_ID = -1;
    private static final String MIRRORSHARE_PKG_NAME = "com.huawei.android.mirrorshare";
    private static final String TAG = "AirSharingManager";
    private int mCastPlusDisplayId = -1;
    private final ServiceConnection mConnAirSharing = new ServiceConnection() {
        /* class com.huawei.server.hwmultidisplay.castplus.AirSharingManager.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            HwPCUtils.log(AirSharingManager.TAG, "airSharing onServiceConnected");
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            HwPCUtils.log(AirSharingManager.TAG, "airSharing onServiceDisconnected");
            if (AirSharingManager.this.mHandler == null) {
                HwPCUtils.log(AirSharingManager.TAG, "err mHandler is null");
            } else {
                AirSharingManager.this.mHandler.postDelayed(new Runnable() {
                    /* class com.huawei.server.hwmultidisplay.castplus.AirSharingManager.AnonymousClass1.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        if (AirSharingManager.this.mCastPlusDisplayId != -1) {
                            int castPlusDisplayId = AirSharingManager.this.mCastPlusDisplayId;
                            AirSharingManager.this.mCastPlusDisplayId = -1;
                            AirSharingManager.this.bindAirSharingService(castPlusDisplayId);
                        }
                    }
                }, 800);
            }
        }
    };
    private Context mContext = null;
    private Handler mHandler = null;

    public AirSharingManager(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
    }

    public static boolean isCastPlusDisplay(Context context, int displayId) {
        HwPCUtils.log(TAG, "isCastPlusDisplay in, displayId: " + displayId);
        if (context == null) {
            HwPCUtils.log(TAG, "err context is null");
            return false;
        } else if (!(context.getSystemService("display") instanceof DisplayManager)) {
            HwPCUtils.log(TAG, "err instanceof DisplayManager is failed");
            return false;
        } else {
            DisplayManager displayManager = (DisplayManager) context.getSystemService("display");
            if (displayManager == null) {
                HwPCUtils.log(TAG, "err displayManager is null");
                return false;
            }
            Display display = displayManager.getDisplay(displayId);
            if (display == null) {
                HwPCUtils.log(TAG, "err display is null");
                return false;
            }
            String name = display.getName();
            int type = DisplayEx.getType(display);
            HwPCUtils.log(TAG, "displayType: " + type + ", displayName: " + name);
            if (type != 5 || !CAST_PLUS_VIRTUALDISPLAY_NAME.equals(name)) {
                return false;
            }
            return true;
        }
    }

    public boolean isDisplayBound(int displayId) {
        HwPCUtils.log(TAG, "isDisplayBound in, displayId: " + displayId + ", mCastPlusDisplayId: " + this.mCastPlusDisplayId);
        return this.mCastPlusDisplayId == displayId;
    }

    public boolean bindAirSharingService(int displayId) {
        HwPCUtils.log(TAG, "bindAirSharingService in, displayId: " + displayId + ", mCastPlusDisplayId: " + this.mCastPlusDisplayId);
        if (this.mCastPlusDisplayId != -1) {
            HwPCUtils.log(TAG, "airsharing service has bind");
            return false;
        }
        String pkgName = getAirSharingPackageName();
        if (TextUtils.isEmpty(pkgName)) {
            HwPCUtils.log(TAG, "bind airSharing service failed because pkgName is empty");
            return false;
        }
        ComponentName component = new ComponentName(pkgName, "com.huawei.android.airsharing.service.PlayerService");
        Intent intent = new Intent();
        intent.setComponent(component);
        if (!bindService(this.mContext, this.mConnAirSharing, intent, true)) {
            HwPCUtils.log(TAG, "bind airSharing service failed");
            return false;
        }
        this.mCastPlusDisplayId = displayId;
        return true;
    }

    public boolean unbindAirSharingService(int displayId) {
        HwPCUtils.log(TAG, "unbindAirSharingService in, displayId: " + displayId + ", mCastPlusDisplayId: " + this.mCastPlusDisplayId);
        this.mCastPlusDisplayId = -1;
        Context context = this.mContext;
        if (context == null) {
            HwPCUtils.log(TAG, "unbindService failed because mContext is null");
            return false;
        }
        ServiceConnection serviceConnection = this.mConnAirSharing;
        if (serviceConnection == null) {
            HwPCUtils.log(TAG, "unbindService failed because mConnAirSharing is null");
            return false;
        }
        try {
            context.unbindService(serviceConnection);
            return true;
        } catch (IllegalArgumentException e) {
            HwPCUtils.log(TAG, "unbind airSharing service failed with IllegalArgumentException");
            return false;
        }
    }

    private String getAirSharingPackageName() {
        PackageManager packageManager;
        Context context = this.mContext;
        if (context == null || (packageManager = context.getPackageManager()) == null) {
            return BuildConfig.FLAVOR;
        }
        try {
            packageManager.getApplicationInfo(AIRSHARING_PKG_NAME, 0);
            HwPCUtils.log(TAG, "airSharing pkg can be found");
            return AIRSHARING_PKG_NAME;
        } catch (PackageManager.NameNotFoundException e) {
            HwPCUtils.log(TAG, "airSharing pkg cannot be found");
            try {
                packageManager.getApplicationInfo(MIRRORSHARE_PKG_NAME, 0);
                HwPCUtils.log(TAG, "mirrorSharing pkg can be found");
                return MIRRORSHARE_PKG_NAME;
            } catch (PackageManager.NameNotFoundException e2) {
                HwPCUtils.log(TAG, "mirrorSharing pkg cannot be found");
                return BuildConfig.FLAVOR;
            }
        }
    }

    private boolean bindService(Context context, ServiceConnection connection, Intent intent, boolean isBindImportant) {
        HwPCUtils.log(TAG, "bindService in, isBindImportant: " + isBindImportant);
        if (context == null) {
            HwPCUtils.log(TAG, "bindService failed because context is null");
            return false;
        } else if (connection == null) {
            HwPCUtils.log(TAG, "bindService failed because connection is null");
            return false;
        } else if (intent == null) {
            HwPCUtils.log(TAG, "bindService failed because intent is null");
            return false;
        } else {
            if (isBindImportant) {
                try {
                    context.bindService(intent, connection, 65);
                } catch (IllegalArgumentException e) {
                    HwPCUtils.log(TAG, "bindService failed with IllegalArgumentException");
                    return false;
                } catch (SecurityException e2) {
                    HwPCUtils.log(TAG, "bindService failed with SecurityException");
                    return false;
                }
            } else {
                context.bindService(intent, connection, 1);
            }
            return true;
        }
    }
}

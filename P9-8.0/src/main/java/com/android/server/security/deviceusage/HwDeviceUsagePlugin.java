package com.android.server.security.deviceusage;

import android.content.Context;
import android.os.IBinder;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.core.IHwSecurityPlugin.Creator;
import huawei.android.security.IHwDeviceUsagePlugin.Stub;

public class HwDeviceUsagePlugin extends Stub implements IHwSecurityPlugin {
    public static final boolean CHINA_RELEASE_VERSION = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    public static final Creator CREATOR = new Creator() {
        public IHwSecurityPlugin createPlugin(Context context) {
            if (HwDeviceUsagePlugin.HW_DEBUG) {
                Slog.d(HwDeviceUsagePlugin.TAG, "createPlugin");
            }
            return new HwDeviceUsagePlugin(context);
        }

        public String getPluginPermission() {
            return HwDeviceUsagePlugin.MANAGE_USE_SECURITY;
        }
    };
    private static final boolean HW_DEBUG;
    public static final boolean IS_PHONE = SystemProperties.get("ro.build.characteristics").equals(MemoryConstant.MEM_SCENE_DEFAULT);
    private static final String MANAGE_USE_SECURITY = "com.huawei.permission.MANAGE_USE_SECURITY";
    private static final String TAG = "HwDeviceUsagePlugin";
    private Context mContext;
    private HwDeviceUsageCollection mHwDeviceUsageCollection;
    private ActivationMonitor mMonitor = new ActivationMonitor(this.mContext);

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HW_DEBUG = isLoggable;
    }

    public HwDeviceUsagePlugin(Context context) {
        this.mContext = context;
    }

    public IBinder asBinder() {
        return this;
    }

    public void onStart() {
        if (HW_DEBUG) {
            Slog.d(TAG, "HwDeviceUsagePlugin is Start");
        }
        this.mMonitor.start();
        this.mHwDeviceUsageCollection = new HwDeviceUsageCollection(this.mContext);
        boolean openFlag = (CHINA_RELEASE_VERSION && IS_PHONE) ? this.mHwDeviceUsageCollection.getOpenFlag() : false;
        if (openFlag) {
            this.mHwDeviceUsageCollection.onStart();
        }
    }

    public void onStop() {
    }

    public long getScreenOnTime() {
        checkPermission(MANAGE_USE_SECURITY);
        if (this.mHwDeviceUsageCollection == null) {
            return -1;
        }
        return this.mHwDeviceUsageCollection.getScreenOnTime();
    }

    public long getChargeTime() {
        checkPermission(MANAGE_USE_SECURITY);
        if (this.mHwDeviceUsageCollection == null) {
            return -1;
        }
        return this.mHwDeviceUsageCollection.getChargeTime();
    }

    public long getTalkTime() {
        checkPermission(MANAGE_USE_SECURITY);
        if (this.mHwDeviceUsageCollection == null) {
            return -1;
        }
        return this.mHwDeviceUsageCollection.getTalkTime();
    }

    public long getFristUseTime() {
        checkPermission(MANAGE_USE_SECURITY);
        if (this.mHwDeviceUsageCollection == null) {
            return -1;
        }
        return this.mHwDeviceUsageCollection.getFristUseTime();
    }

    public void setOpenFlag(int flag) {
        checkPermission(MANAGE_USE_SECURITY);
        if (this.mHwDeviceUsageCollection != null) {
            this.mHwDeviceUsageCollection.setOpenFlag(flag);
        }
    }

    public void setScreenOnTime(long time) {
        checkPermission(MANAGE_USE_SECURITY);
        if (this.mHwDeviceUsageCollection != null) {
            this.mHwDeviceUsageCollection.setScreenOnTime(time);
        }
    }

    public void setChargeTime(long time) {
        checkPermission(MANAGE_USE_SECURITY);
        if (this.mHwDeviceUsageCollection != null) {
            this.mHwDeviceUsageCollection.setChargeTime(time);
        }
    }

    public void setTalkTime(long time) {
        checkPermission(MANAGE_USE_SECURITY);
        if (this.mHwDeviceUsageCollection != null) {
            this.mHwDeviceUsageCollection.setTalkTime(time);
        }
    }

    public void setFristUseTime(long time) {
        checkPermission(MANAGE_USE_SECURITY);
        if (this.mHwDeviceUsageCollection != null) {
            this.mHwDeviceUsageCollection.setFristUseTime(time);
        }
    }

    private void checkPermission(String permission) {
        this.mContext.enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }

    public boolean isDeviceActivated() {
        checkPermission(MANAGE_USE_SECURITY);
        if (this.mMonitor.isActivated()) {
            return true;
        }
        this.mMonitor.reStart(ActivationMonitor.SIX_HOURS_MS);
        return false;
    }

    public void resetActivation() {
        checkPermission(MANAGE_USE_SECURITY);
        this.mMonitor.resetActivation();
    }

    public void detectActivationWithDuration(long duration) {
        checkPermission(MANAGE_USE_SECURITY);
        this.mMonitor.reStart(duration);
    }
}

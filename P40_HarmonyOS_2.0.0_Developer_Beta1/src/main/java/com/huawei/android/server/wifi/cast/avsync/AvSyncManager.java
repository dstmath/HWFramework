package com.huawei.android.server.wifi.cast.avsync;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNative;
import com.huawei.android.server.wifi.cast.avsync.AvSyncLatencyRepository;
import com.huawei.android.server.wifi.cast.avsync.AvSyncMonitor;

public class AvSyncManager implements AvSyncMonitor.IAvSyncListener {
    private static final int CMD_ID_WRITE_AV_SYNC_LATENCY_CONFIG = 174;
    private static final int INT_BYTE_SIZE = 4;
    private static final int RIGHT_SHIFT = 8;
    private static final String STA_INTERFACE = "wlan0";
    private static final String TAG = "AvSyncManager";
    private static volatile AvSyncManager sAvSyncManager;
    private AvSyncLatencyRepository mAvSyncLatencyRepository;
    private Context mContext;
    private int mLastLatencyConfig = -1;
    private Looper mLooper;
    private AvSyncMonitor mMonitor;
    private AvSyncLatencyRepository.IAvSyncParseResultListener mParseResultListener = new AvSyncLatencyRepository.IAvSyncParseResultListener() {
        /* class com.huawei.android.server.wifi.cast.avsync.AvSyncManager.AnonymousClass1 */

        @Override // com.huawei.android.server.wifi.cast.avsync.AvSyncLatencyRepository.IAvSyncParseResultListener
        public void onSuccess() {
            if (AvSyncManager.this.mMonitor != null) {
                AvSyncManager avSyncManager = AvSyncManager.this;
                avSyncManager.handleAvSync(avSyncManager.mMonitor.getTopAppPkgName(), AvSyncManager.this.mMonitor.getCastType());
            }
        }

        @Override // com.huawei.android.server.wifi.cast.avsync.AvSyncLatencyRepository.IAvSyncParseResultListener
        public void onFailed() {
            HwHiLog.i(AvSyncManager.TAG, false, "parse latency has failed", new Object[0]);
        }
    };

    private AvSyncManager(Context ctx, Looper loop) {
        this.mContext = ctx;
        this.mLooper = loop;
    }

    public static AvSyncManager createAvSyncManager(Context context, Looper loop) {
        if (sAvSyncManager == null) {
            synchronized (AvSyncManager.class) {
                if (sAvSyncManager == null) {
                    sAvSyncManager = new AvSyncManager(context, loop);
                }
            }
        }
        return sAvSyncManager;
    }

    public void init() {
        setAvSyncLatency(0);
        this.mMonitor = new AvSyncMonitor(this);
        this.mAvSyncLatencyRepository = new AvSyncLatencyRepository(this.mLooper);
        this.mMonitor.init();
        this.mAvSyncLatencyRepository.setParseResultListener(this.mParseResultListener);
        HwHiLog.i(TAG, false, "AvSyncManager is started", new Object[0]);
    }

    private void setAvSyncLatency(int latency) {
        if (latency >= 0 && this.mLastLatencyConfig != latency) {
            byte[] buffer = int2ByteArr(latency);
            WifiNative wifiNative = WifiInjector.getInstance().getWifiNative();
            if (wifiNative == null || wifiNative.mHwWifiNativeEx == null) {
                HwHiLog.i(TAG, false, "wifiNative or mHwWifiNativeEx is null", new Object[0]);
                return;
            }
            HwHiLog.i(TAG, false, "write av sync latency latency = %{public}d", new Object[]{Integer.valueOf(latency)});
            int ret = wifiNative.mHwWifiNativeEx.sendCmdToDriver(STA_INTERFACE, (int) CMD_ID_WRITE_AV_SYNC_LATENCY_CONFIG, buffer);
            if (ret < 0) {
                HwHiLog.i(TAG, false, "write av sync latency failed, ret = %{public}d", new Object[]{Integer.valueOf(ret)});
            } else {
                this.mLastLatencyConfig = latency;
            }
        }
    }

    @Override // com.huawei.android.server.wifi.cast.avsync.AvSyncMonitor.IAvSyncListener
    public void onAvSyncEvent(String packageName, int castType, int event) {
        AvSyncLatencyRepository avSyncLatencyRepository;
        if (event != 1 || (avSyncLatencyRepository = this.mAvSyncLatencyRepository) == null) {
            handleAvSync(packageName, castType);
        } else {
            avSyncLatencyRepository.readLatencyConfig();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAvSync(String packageName, int castType) {
        int appVersionCode = getAppVersionCode(packageName);
        HwHiLog.i(TAG, false, "currentPackageName  = %{public}s, versionCode = %{public}d, castType =  %{public}d", new Object[]{packageName, Integer.valueOf(appVersionCode), Integer.valueOf(castType)});
        if (!this.mMonitor.isCastScene() || TextUtils.isEmpty(packageName) || castType == -1 || appVersionCode == -1) {
            setAvSyncLatency(0);
        } else {
            setAvSyncLatency(this.mAvSyncLatencyRepository.getAvSyncLatency(packageName, appVersionCode, castType));
        }
    }

    private int getAppVersionCode(String packageName) {
        PackageInfo packageInfo;
        try {
            if (TextUtils.isEmpty(packageName) || (packageInfo = this.mContext.getPackageManager().getPackageInfo(packageName, 0)) == null) {
                return -1;
            }
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            HwHiLog.i(TAG, false, "%{public}s can not get versionCode", new Object[]{packageName});
            return -1;
        }
    }

    private byte[] int2ByteArr(int value) {
        byte[] arr = new byte[4];
        for (int i = 0; i < 4; i++) {
            arr[i] = (byte) ((value >> (i * 8)) & 255);
        }
        return arr;
    }
}

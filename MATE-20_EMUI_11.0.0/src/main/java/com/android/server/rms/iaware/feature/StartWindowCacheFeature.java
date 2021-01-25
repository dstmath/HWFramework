package com.android.server.rms.iaware.feature;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.IAwareCMSManager;
import com.android.server.rms.iaware.HwStartWindowCache;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.huawei.android.content.ContextEx;
import com.huawei.android.internal.util.MemInfoReaderExt;
import com.huawei.android.os.UserHandleEx;
import java.util.Map;

public class StartWindowCacheFeature extends RFeature {
    private static final String APS_RESOLUTION_CHANGE_ACTION = "huawei.intent.action.APS_RESOLUTION_CHANGE_ACTION";
    private static final String APS_RESOLUTION_CHANGE_PERSISSIONS = "huawei.intent.permissions.APS_RESOLUTION_CHANGE_ACTION";
    private static final int BASE_VERSION = 5;
    private static final int DEFAULT_TOPN = 10;
    private static final long HIGH_END_DEVICE_THRESHOLD = 6144;
    private static final int INVALID_VALUE = -1;
    private static final String TAG = "StartWindowCacheFeature";
    private static final String TAG_CONFIG_NAME = "TopNConfig";
    private static final String TAG_FEATURE_NAME = "StartWindowCache";
    private static final String TAG_INVALID_TIME = "invalidTime";
    private static final String TAG_RAM_SIZE = "ramsize";
    private static final String TAG_TOPN = "topN";
    private int mInvalidTimeMinute = -1;
    private boolean mIsEnabled = false;
    private BroadcastReceiver mResolutionChangeReceiver = new BroadcastReceiver() {
        /* class com.android.server.rms.iaware.feature.StartWindowCacheFeature.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            HwStartWindowCache.getInstance().notifyResolutionChange();
        }
    };
    private int mTopN = -1;

    public StartWindowCacheFeature(Context context, AwareConstant.FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        if (this.mIsEnabled) {
            this.mContext.unregisterReceiver(this.mResolutionChangeReceiver);
            HwStartWindowCache.getInstance().deinit();
        }
        this.mIsEnabled = false;
        AwareLog.i(TAG, "StartWindowCacheFeature disable");
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enableFeatureEx(int realVersion) {
        if (realVersion < 5 || this.mContext == null) {
            return false;
        }
        readCustConfig();
        if (this.mTopN == -1 && shouldDeviceEnableByDefault()) {
            this.mTopN = 10;
        }
        if (this.mTopN <= 0) {
            return false;
        }
        if (this.mIsEnabled) {
            this.mContext.unregisterReceiver(this.mResolutionChangeReceiver);
        }
        ContextEx.registerReceiverAsUser(this.mContext, this.mResolutionChangeReceiver, UserHandleEx.ALL, new IntentFilter(APS_RESOLUTION_CHANGE_ACTION), APS_RESOLUTION_CHANGE_PERSISSIONS, (Handler) null);
        HwStartWindowCache.getInstance().init(this.mTopN, this.mInvalidTimeMinute);
        this.mIsEnabled = true;
        return true;
    }

    private void readCustConfig() {
        Map<String, String> configPropertries;
        AwareConfig configList = null;
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService == null) {
                AwareLog.w(TAG, "can not find service awareservice.");
                return;
            }
            configList = IAwareCMSManager.getCustConfig(awareService, TAG_FEATURE_NAME, TAG_CONFIG_NAME);
            if (configList == null) {
                AwareLog.i(TAG, "readCustConfig failure, no config use default setting");
                return;
            }
            MemoryUtils.initialRamSizeLowerBound();
            for (AwareConfig.Item item : configList.getConfigList()) {
                if (!(item == null || item.getProperties() == null || (configPropertries = item.getProperties()) == null)) {
                    String ramSize = configPropertries.get("ramsize");
                    if (MemoryUtils.checkRamSize(ramSize)) {
                        AwareLog.d(TAG, "loadStartWindowCache success: ramsize: " + ramSize);
                        readItemConfig(item);
                        return;
                    }
                }
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "getConfig RemoteException!");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x004c A[Catch:{ NumberFormatException -> 0x0097 }] */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0075 A[Catch:{ NumberFormatException -> 0x0097 }] */
    private void readItemConfig(AwareConfig.Item item) {
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            if (subItem != null) {
                String itemName = subItem.getName();
                String itemValue = subItem.getValue();
                if (!(itemName == null || itemValue == null)) {
                    char c = 65535;
                    try {
                        int hashCode = itemName.hashCode();
                        if (hashCode != -953162396) {
                            if (hashCode == 3565977 && itemName.equals(TAG_TOPN)) {
                                c = 0;
                                if (c == 0) {
                                    this.mTopN = Integer.parseInt(itemValue.trim());
                                    AwareLog.i(TAG, "apply topN config = " + this.mTopN);
                                } else if (c == 1) {
                                    this.mInvalidTimeMinute = Integer.parseInt(itemValue.trim());
                                    AwareLog.i(TAG, "apply invalidTime config = " + this.mInvalidTimeMinute + "min");
                                }
                            }
                        } else if (itemName.equals(TAG_INVALID_TIME)) {
                            c = 1;
                            if (c == 0) {
                            }
                        }
                        if (c == 0) {
                        }
                    } catch (NumberFormatException e) {
                        AwareLog.e(TAG, "readItem error!");
                    }
                }
            }
        }
    }

    private boolean shouldDeviceEnableByDefault() {
        MemInfoReaderExt memInfoReader = new MemInfoReaderExt();
        memInfoReader.readMemInfo();
        return memInfoReader.getTotalSize() / MemoryConstant.MB_SIZE > HIGH_END_DEVICE_THRESHOLD;
    }
}

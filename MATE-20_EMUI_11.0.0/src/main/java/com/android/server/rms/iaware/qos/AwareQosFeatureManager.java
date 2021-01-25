package com.android.server.rms.iaware.qos;

import android.os.IBinder;
import android.os.RemoteException;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwareCMSManager;
import android.rms.iaware.IAwaredConnection;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.huawei.android.os.ProcessExt;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public final class AwareQosFeatureManager {
    private static final int BYTE_BUFFER_SIZE = 8;
    private static final String CONFIG_SUB_SWITCH = "QosConfig";
    private static final String FEATURE_ITEM = "featureName";
    private static final String FEATURE_NAME = "Qos";
    private static final String FEATURE_SUB_NAME = "QosSwitch";
    private static final String LABEL_SWITCH = "switch";
    private static final Object LOCAL_LOCK = new Object();
    private static final int MSG_BASE_VALUE = 100;
    private static final int MSG_QOS_SCHED_SWITCH = 181;
    private static final int QOS_SCHED_SET_ENABLE_FLAG = 1;
    private static final String TAG = "AwareQosFeatureManager";
    private static AwareQosFeatureManager sInstance = null;
    private int mSwitchOfFeature = 0;

    private AwareQosFeatureManager() {
        getAwareQosFeatureSwitch();
    }

    public static AwareQosFeatureManager getInstance() {
        AwareQosFeatureManager awareQosFeatureManager;
        synchronized (LOCAL_LOCK) {
            if (sInstance == null) {
                sInstance = new AwareQosFeatureManager();
            }
            awareQosFeatureManager = sInstance;
        }
        return awareQosFeatureManager;
    }

    private void getAwareQosFeatureSwitch() {
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                AwareConfig switchConfig = IAwareCMSManager.getCustConfig(awareService, FEATURE_NAME, CONFIG_SUB_SWITCH);
                if (switchConfig == null) {
                    switchConfig = IAwareCMSManager.getConfig(awareService, FEATURE_NAME, CONFIG_SUB_SWITCH);
                }
                getSwitchConfig(switchConfig);
                return;
            }
            AwareLog.w(TAG, "getAwareConfig can not find service awareService.");
        } catch (RemoteException e) {
            AwareLog.e(TAG, "getAwareConfig RemoteException");
        }
    }

    private void getSwitchConfig(AwareConfig awareConfig) {
        Map<String, String> configProperties;
        if (awareConfig == null) {
            AwareLog.w(TAG, "get cust aware config failed!");
            return;
        }
        List<AwareConfig.Item> itemList = awareConfig.getConfigList();
        if (itemList != null) {
            for (AwareConfig.Item item : itemList) {
                if (!(item == null || (configProperties = item.getProperties()) == null)) {
                    String featureName = configProperties.get(FEATURE_ITEM);
                    if (featureName != null) {
                        parseSwitchValue(item, featureName);
                    } else {
                        return;
                    }
                }
            }
        }
    }

    private void parseSwitchValue(AwareConfig.Item item, String featureName) {
        List<AwareConfig.SubItem> subItemList = item.getSubItemList();
        if (subItemList == null) {
            AwareLog.w(TAG, "get sub switch config item failed!");
            return;
        }
        for (AwareConfig.SubItem subItem : subItemList) {
            if (subItem != null) {
                String itemName = subItem.getName();
                String itemValue = subItem.getValue();
                if ("switch".equals(itemName)) {
                    parseAllSwitch(itemValue, featureName);
                }
            }
        }
    }

    private void parseAllSwitch(String data, String featureName) {
        if (data != null && FEATURE_SUB_NAME.equals(featureName)) {
            try {
                this.mSwitchOfFeature = Integer.parseInt(data);
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "parseAllSwitch parseInt QosSwitch failed!");
            }
        }
    }

    private void setQosSwitch(int value) {
        AwareLog.i(TAG, "setQosSwitch, value=" + value);
        if ((value & 1) != 0) {
            ProcessExt.setQosSched(true);
        } else {
            ProcessExt.setQosSched(false);
        }
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MSG_QOS_SCHED_SWITCH);
        buffer.putInt(value);
        boolean isResCode = IAwaredConnection.getInstance().sendPacket(buffer.array(), 0, buffer.position());
        if (!isResCode) {
            AwareLog.e(TAG, "setQosSwitch: sendPacket failed, switch: " + value + ", send error code: " + isResCode);
        }
    }

    public void enable() {
        setQosSwitch(this.mSwitchOfFeature);
        AwareBinderSchedManager.getInstance().enable(this.mSwitchOfFeature);
    }

    public void disable() {
        setQosSwitch(0);
        AwareBinderSchedManager.getInstance().disable();
    }

    public boolean doCheckPreceptible(int pid) {
        ProcessInfo procInfo;
        String pkgName;
        int type;
        if (!AwareNetQosSchedManager.getAwareNetQosSchedSwitch() || (procInfo = ProcessInfoCollector.getInstance().getProcessInfo(pid)) == null || !AwareAppKeyBackgroup.getInstance().checkKeyBackgroupByState(5, procInfo.mPid, procInfo.mUid, procInfo.mPackageName) || AwareAppAssociate.getInstance().isVisibleWindow(pid) || AwareIntelligentRecg.getInstance().isScreenRecordEx(procInfo) || AwareAppKeyBackgroup.getInstance().checkKeyBackgroupByState(2, procInfo.mPid, procInfo.mUid, procInfo.mPackageName) || AwareAppKeyBackgroup.getInstance().checkKeyBackgroupByState(3, procInfo.mPid, procInfo.mUid, procInfo.mPackageName) || (pkgName = getAppPkgName(pid, procInfo.mUid)) == null || (type = AppTypeRecoManager.getInstance().getAppType(pkgName)) == 0 || type == -1) {
            return true;
        }
        AwareLog.d(TAG, "doCheckPreceptible " + pkgName + " pid=" + pid + " is unpreceptible");
        return false;
    }

    private String getAppPkgName(int pid, int uid) {
        if (pid > 0) {
            return InnerUtils.getAwarePkgName(pid);
        }
        return InnerUtils.getPackageNameByUid(uid);
    }

    public void setAppVipThreadForQos(int pid, List<Integer> threads) {
        if (!((this.mSwitchOfFeature & 1) == 0 || threads == null || pid <= 0)) {
            for (int i = 0; i < threads.size(); i++) {
                ProcessExt.setThreadQosPolicy(threads.get(i).intValue(), 10);
            }
        }
    }
}

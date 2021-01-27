package com.android.server.rms.iaware.qos;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.IAwareCMSManager;
import android.rms.iaware.IAwaredConnection;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.rms.collector.ResourceCollector;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.huawei.android.os.ProcessExt;
import com.huawei.libcore.io.IoUtilsEx;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public final class AwareQosFeatureManager {
    private static final String BAIDU_INPUT_PACKAGE_NAME = "com.baidu.input_huawei";
    private static final int BYTE_BUFFER_SIZE = 8;
    private static final String CONFIG_SUB_SWITCH = "QosConfig";
    private static final int DEFAULT_DELAY = 500;
    private static final String FEATURE_ITEM = "featureName";
    private static final String FEATURE_NAME = "Qos";
    private static final String FEATURE_SUB_NAME = "QosSwitch";
    private static final String LABEL_SWITCH = "switch";
    private static final Object LOCAL_LOCK = new Object();
    private static final int MSG_BASE_VALUE = 100;
    private static final int MSG_QOS_SCHED_SWITCH = 181;
    private static final int MSG_REPORT_DATA = 1;
    private static final int MSG_SET_INPUT = 2;
    private static final int QOS_SCHED_SET_ENABLE_FLAG = 1;
    private static final String TAG = "AwareQosFeatureManager";
    private static AwareQosFeatureManager sInstance = null;
    private AwareSchedHandler mAwareSchedHandler = null;
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
        this.mAwareSchedHandler = new AwareSchedHandler();
        AwareBinderSchedManager.getInstance().enable(this.mSwitchOfFeature);
    }

    public void disable() {
        setQosSwitch(0);
        AwareBinderSchedManager.getInstance().disable();
    }

    public void reportData(CollectData data) {
        if ((this.mSwitchOfFeature & 1) != 0 && this.mAwareSchedHandler != null) {
            Message msg = Message.obtain();
            msg.what = 1;
            msg.obj = data;
            this.mAwareSchedHandler.sendMessage(msg);
        }
    }

    private boolean isVipGroup(int pid) {
        try {
            String lineString = IoUtilsEx.readFileAsString("/proc/" + pid + "/cpuset");
            if (lineString == null || !lineString.contains("vip")) {
                return false;
            }
            return true;
        } catch (IOException e) {
            AwareLog.w(TAG, "exception IOException!");
            return false;
        }
    }

    private void handleShowInput(int pid) {
        Message msg = this.mAwareSchedHandler.obtainMessage();
        msg.what = 2;
        msg.arg1 = pid;
        this.mAwareSchedHandler.sendMessageDelayed(msg, 500);
        ResourceCollector.setProcessQosPolicy(pid, 10);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setInputVip(Message msg) {
        int pid = msg.arg1;
        boolean isVip = isVipGroup(pid);
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "setInputVip pid:" + pid + " isVip: " + isVip);
        }
        if (isVip) {
            ResourceCollector.setProcessQosPolicy(pid, 10);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleReportData(CollectData data) {
        Bundle appAssoc;
        if (data != null && AwareConstant.ResourceType.getResourceType(data.getResId()) == AwareConstant.ResourceType.RESOURCE_SHOW_INPUTMETHOD && (appAssoc = data.getBundle()) != null) {
            int eventId = appAssoc.getInt("eventid");
            int pid = appAssoc.getInt("callPid");
            int uid = appAssoc.getInt("callUid");
            String pkg = getAppPkgName(pid, uid);
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "handleReportData pid: " + pid + " uid: " + uid + " pkg: " + pkg);
            }
            if (eventId == 34 && pid != 0 && BAIDU_INPUT_PACKAGE_NAME.equals(pkg)) {
                handleShowInput(pid);
            }
        }
    }

    /* access modifiers changed from: private */
    public class AwareSchedHandler extends Handler {
        private AwareSchedHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if ((AwareQosFeatureManager.this.mSwitchOfFeature & 1) != 0) {
                if (msg == null) {
                    AwareLog.w(AwareQosFeatureManager.TAG, "AwareSchedHandler, msg is null, error!");
                    return;
                }
                int i = msg.what;
                if (i != 1) {
                    if (i != 2) {
                        AwareLog.i(AwareQosFeatureManager.TAG, "AwareSchedHandler, default branch, msg.what is " + msg.what);
                        return;
                    }
                    AwareQosFeatureManager.this.setInputVip(msg);
                } else if (msg.obj instanceof CollectData) {
                    AwareQosFeatureManager.this.handleReportData((CollectData) msg.obj);
                }
            }
        }
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

package com.android.server.rms.iaware.qos;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.IAwareCMSManager;
import android.rms.iaware.IAwaredConnection;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareNetQosSchedManager {
    private static final String CLICK_LIMIT_DELAY = "ClickLimitDelay";
    private static final int CONFIG_NETQOS_ENABLE = 1;
    private static final String CONFIG_PRIORITY_PERIOD = "PriorityPeriod";
    private static final String CONFIG_SUB_SWITCH = "NetQosConfig";
    private static final String CONFIG_SWITCH = "switch";
    private static final int DEFAULT_CLICK_LOW_PRIOVITY_LIMIT_DELAY = 2000;
    private static final int DEFAULT_SLIDE_LOW_PRIOVITY_LIMIT_DELAY = 5000;
    private static final String FEATURE = "feature";
    private static final String FEATURE_NAME = "NetQosSched";
    private static final String FEATURE_NETQOS = "netqos";
    private static final Object LOCK = new Object();
    private static final int MSG_LOW_PRIOVITY_LIMIT_DELAY = 2;
    private static final int MSG_REPORT_DATA = 1;
    private static final int NETQOS_MSG_LEN = 8;
    private static final int NETQOS_SET_LOW_LIMIT = 406;
    private static final int NETQOS_SET_PERIOD = 405;
    private static final int NETQOS_SET_SWTICH = 404;
    private static final String SETTING_SECURE_VPN_WORK_VALUE = "wifipro_network_vpn_state";
    private static final String SLIDE_LIMIT_DELAY = "SlideLimitDelay";
    private static final String TAG = "AwareNetQosSchedManager";
    private static AwareNetQosSchedManager sInstance = null;
    private static boolean sNetQosSchedSwitch = false;
    private AwareNetQosSchedHandler mAwareNetQosSchedHandler = null;
    private int mClickLimitDelay = DEFAULT_CLICK_LOW_PRIOVITY_LIMIT_DELAY;
    private AtomicBoolean mFeatureEnable = new AtomicBoolean(false);
    private boolean mIsLowLimitEnable = false;
    private boolean mIsVpnWorking = false;
    private int mNetQosSchedPeriod = 0;
    private int mSlideLimitDelay = DEFAULT_SLIDE_LOW_PRIOVITY_LIMIT_DELAY;
    private int mVpnReference = 0;

    private AwareNetQosSchedManager() {
    }

    public static AwareNetQosSchedManager getInstance() {
        AwareNetQosSchedManager awareNetQosSchedManager;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new AwareNetQosSchedManager();
            }
            awareNetQosSchedManager = sInstance;
        }
        return awareNetQosSchedManager;
    }

    public void enable() {
        getAwareNetQosSchedFeatureConfig();
        if (!sNetQosSchedSwitch) {
            AwareLog.i(TAG, " mNetQosSchedSwitch disable");
            return;
        }
        this.mAwareNetQosSchedHandler = new AwareNetQosSchedHandler();
        setNetQosConfig(404, sNetQosSchedSwitch ? 1 : 0);
        setNetQosConfig(NETQOS_SET_PERIOD, this.mNetQosSchedPeriod);
        this.mFeatureEnable.set(true);
    }

    public void disable() {
        this.mFeatureEnable.set(false);
        sNetQosSchedSwitch = false;
        this.mAwareNetQosSchedHandler = null;
        this.mVpnReference = 0;
        this.mIsVpnWorking = false;
        setNetQosConfig(404, 0);
    }

    public void reportData(CollectData data) {
        if (!this.mFeatureEnable.get()) {
            AwareLog.d(TAG, "reportData : mNetQosSchedSwitch disable");
            return;
        }
        Message msg = Message.obtain();
        msg.what = 1;
        msg.obj = data;
        this.mAwareNetQosSchedHandler.sendMessage(msg);
    }

    private void getAwareNetQosSchedFeatureConfig() {
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                AwareConfig config = IAwareCMSManager.getCustConfig(awareService, FEATURE_NAME, CONFIG_SUB_SWITCH);
                if (config == null) {
                    config = IAwareCMSManager.getConfig(awareService, FEATURE_NAME, CONFIG_SUB_SWITCH);
                }
                parseNetQosSchedConfig(config);
                return;
            }
            AwareLog.w(TAG, "getAwareConfig can not find service awareService.");
        } catch (RemoteException e) {
            AwareLog.e(TAG, "getAwareConfig RemoteException");
        }
    }

    private void parseNetQosSchedConfig(AwareConfig config) {
        Map<String, String> configProperties;
        if (config == null) {
            AwareLog.w(TAG, "getNetQosSched config fail.");
            return;
        }
        List<AwareConfig.Item> itemList = config.getConfigList();
        if (itemList != null) {
            for (AwareConfig.Item item : itemList) {
                if (!(item == null || (configProperties = item.getProperties()) == null)) {
                    String featureName = configProperties.get(FEATURE);
                    if (featureName == null) {
                        return;
                    }
                    if (FEATURE_NETQOS.equals(featureName)) {
                        parseNetQosConfig(item);
                    } else {
                        AwareLog.d(TAG, "unknown feature name !");
                    }
                }
            }
        }
    }

    private void parseNetQosConfig(AwareConfig.Item item) {
        List<AwareConfig.SubItem> subItemList = item.getSubItemList();
        if (subItemList == null) {
            AwareLog.w(TAG, "get netqos config item failed!");
            return;
        }
        for (AwareConfig.SubItem subItem : subItemList) {
            if (subItem != null) {
                String itemName = subItem.getName();
                String itemValue = subItem.getValue();
                if ("switch".equals(itemName)) {
                    boolean z = true;
                    if (parseNetQosConfigValue(itemValue) != 1) {
                        z = false;
                    }
                    sNetQosSchedSwitch = z;
                } else if (CONFIG_PRIORITY_PERIOD.equals(itemName)) {
                    this.mNetQosSchedPeriod = parseNetQosConfigValue(itemValue);
                } else if (CLICK_LIMIT_DELAY.equals(itemName)) {
                    int delay = parseNetQosConfigValue(itemValue);
                    if (delay <= 0) {
                        AwareLog.w(TAG, "parseNetQosConfig itemName " + itemName + " value " + delay + " failed!");
                    } else {
                        this.mClickLimitDelay = delay;
                    }
                } else if (SLIDE_LIMIT_DELAY.equals(itemName)) {
                    int delay2 = parseNetQosConfigValue(itemValue);
                    if (delay2 <= 0) {
                        AwareLog.w(TAG, "parseNetQosConfig itemName " + itemName + " value " + delay2 + " failed!");
                    } else {
                        this.mSlideLimitDelay = delay2;
                    }
                } else {
                    AwareLog.w(TAG, "parseNetQosConfig itemName " + itemName + " failed!");
                }
            }
        }
    }

    private int parseNetQosConfigValue(String data) {
        if (data == null) {
            return 0;
        }
        try {
            return Integer.parseInt(data);
        } catch (NumberFormatException e) {
            AwareLog.w(TAG, "parseNetQosConfigValue parseInt value failed!");
            return 0;
        }
    }

    private void setNetQosConfig(int cmd, int value) {
        AwareLog.d(TAG, "setNetQosConfig cmd = " + cmd + " value " + value);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(cmd);
        buffer.putInt(value);
        boolean resCode = IAwaredConnection.getInstance().sendPacket(buffer.array(), 0, buffer.position());
        if (!resCode) {
            AwareLog.e(TAG, "setNetQosConfig: sendPacket failed, cmd: " + cmd + ", value: " + value + ", send error code: " + resCode);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setNetQosLowLimit(boolean enable) {
        AwareLog.d(TAG, "setNetQosLowLimit, enable " + enable + " mIsLowLimitEnable " + this.mIsLowLimitEnable);
        if (this.mIsLowLimitEnable != enable) {
            setNetQosConfig(NETQOS_SET_LOW_LIMIT, enable ? 1 : 0);
            this.mIsLowLimitEnable = enable;
        }
    }

    private void handleScrollBegin() {
        if (this.mIsVpnWorking) {
            AwareLog.d(TAG, "vpn is working, ignore scroll begin");
            return;
        }
        setNetQosLowLimit(true);
        this.mAwareNetQosSchedHandler.removeMessages(2);
        AwareLog.d(TAG, "scroll begin");
    }

    private void handleScrollFinish() {
        if (this.mIsVpnWorking) {
            AwareLog.d(TAG, "vpn is working, ignore scroll finish");
            return;
        }
        this.mAwareNetQosSchedHandler.removeMessages(2);
        this.mAwareNetQosSchedHandler.sendEmptyMessageDelayed(2, (long) this.mSlideLimitDelay);
        AwareLog.d(TAG, "scroll finish, delay " + this.mSlideLimitDelay + " ms stop limit");
    }

    private void handleFlingBegin(int duration) {
        if (this.mIsVpnWorking) {
            AwareLog.d(TAG, "vpn is working, ignore fling begin");
            return;
        }
        setNetQosLowLimit(true);
        this.mAwareNetQosSchedHandler.removeMessages(2);
        int delayTime = this.mSlideLimitDelay + duration;
        this.mAwareNetQosSchedHandler.sendEmptyMessageDelayed(2, (long) delayTime);
        AwareLog.d(TAG, "flinging begin duration " + duration + ", delay " + delayTime + " ms stop limit");
    }

    private void handleTouchEvent() {
        if (this.mIsVpnWorking) {
            AwareLog.d(TAG, "vpn is working, ignore touch event");
            return;
        }
        setNetQosLowLimit(true);
        this.mAwareNetQosSchedHandler.removeMessages(2);
        this.mAwareNetQosSchedHandler.sendEmptyMessageDelayed(2, (long) this.mClickLimitDelay);
        AwareLog.d(TAG, "touch event, delay " + this.mClickLimitDelay + " ms stop limit");
    }

    private void handleSceneRecData(Bundle bundle) {
        if (bundle != null) {
            switch (bundle.getInt("relationType")) {
                case 13:
                    handleScrollBegin();
                    return;
                case 14:
                    handleScrollFinish();
                    return;
                case 15:
                    int duration = bundle.getInt("scroll_duration");
                    if (duration > 0) {
                        handleFlingBegin(duration);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private void handleInputData(int event) {
        if (event == 10001) {
            handleTouchEvent();
        }
    }

    private AttrSegments parseCollectData(CollectData data) {
        String eventData = data.getData();
        AttrSegments.Builder builder = new AttrSegments.Builder();
        builder.addCollectData(eventData);
        return builder.build();
    }

    private void handleVpnConnectData(boolean enable) {
        boolean isWorking = true;
        if (enable) {
            this.mVpnReference++;
        } else {
            int i = this.mVpnReference;
            if (i > 0) {
                this.mVpnReference = i - 1;
            } else {
                AwareLog.w(TAG, "mVpnReference error " + this.mVpnReference);
                return;
            }
        }
        AwareLog.d(TAG, "mVpnReference " + this.mVpnReference);
        if (this.mVpnReference <= 0) {
            isWorking = false;
        }
        if (isWorking != this.mIsVpnWorking) {
            this.mIsVpnWorking = isWorking;
            if (isWorking) {
                setNetQosLowLimit(false);
            }
            this.mAwareNetQosSchedHandler.removeMessages(2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleReportData(CollectData data) {
        if (data != null) {
            AwareConstant.ResourceType type = AwareConstant.ResourceType.getResourceType(data.getResId());
            if (type == AwareConstant.ResourceType.RESOURCE_SCENE_REC) {
                Bundle bundle = data.getBundle();
                if (bundle != null) {
                    handleSceneRecData(bundle);
                }
            } else if (type == AwareConstant.ResourceType.RES_INPUT) {
                AttrSegments attrSegments = parseCollectData(data);
                if (attrSegments.isValid()) {
                    handleInputData(attrSegments.getEvent().intValue());
                }
            } else if (type == AwareConstant.ResourceType.RESOURCE_VPN_CONN) {
                Bundle bundle2 = data.getBundle();
                if (bundle2 != null) {
                    handleVpnConnectData(bundle2.getBoolean("vpn_state"));
                }
            } else {
                AwareLog.d(TAG, "unknown type " + type);
            }
        }
    }

    /* access modifiers changed from: private */
    public class AwareNetQosSchedHandler extends Handler {
        private AwareNetQosSchedHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (!AwareNetQosSchedManager.this.mFeatureEnable.get()) {
                AwareLog.i(AwareNetQosSchedManager.TAG, "AwareNetQosSchedHandler, feature mRunning is false, return!");
            } else if (msg == null) {
                AwareLog.i(AwareNetQosSchedManager.TAG, "AwareNetQosSchedHandler, msg is null, error!");
            } else {
                int i = msg.what;
                if (i != 1) {
                    if (i != 2) {
                        AwareLog.i(AwareNetQosSchedManager.TAG, "AwareNetQosSchedHandler, default branch, msg.what is " + msg.what);
                        return;
                    }
                    AwareNetQosSchedManager.this.setNetQosLowLimit(false);
                } else if (msg.obj instanceof CollectData) {
                    AwareNetQosSchedManager.this.handleReportData((CollectData) msg.obj);
                }
            }
        }
    }

    public static boolean getAwareNetQosSchedSwitch() {
        return sNetQosSchedSwitch;
    }
}

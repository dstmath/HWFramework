package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.AwareNRTConstant;
import android.rms.iaware.CollectData;
import android.rms.iaware.IDeviceSettingCallback;
import android.rms.iaware.LogIAware;
import android.rms.iaware.NetLocationStrategy;
import android.util.ArrayMap;
import com.android.server.rms.iaware.AwareCallback;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.cpu.CpuCustBaseConfig;
import com.android.server.rms.iaware.dev.BleSchedFeatureRt;
import com.android.server.rms.iaware.dev.DevSchedCallbackManager;
import com.android.server.rms.iaware.dev.DevSchedFeatureBase;
import com.android.server.rms.iaware.dev.FeatureXmlConfigParserRt;
import com.android.server.rms.iaware.dev.LcdSchedFeatureRt;
import com.android.server.rms.iaware.dev.NetLocationSchedFeatureRt;
import com.android.server.rms.iaware.dev.PhoneStatusRecong;
import com.android.server.rms.iaware.dev.ScreenOnWakelockSchedFeatureRt;
import com.huawei.android.app.IHwActivityNotifierEx;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class DevSchedFeatureRt extends RFeature {
    private static final String APP_SWITCH_REASON = "appSwitch";
    private static final int BASE_VERSION_THREE = 3;
    private static final int BASE_VERSION_TWO = 2;
    public static final String BLE_FEATURE = "ble_iconnect_nearby";
    private static final String DISABLE_VALUE = "0";
    private static final String ENABLE_VALUE = "1";
    public static final String LCD_FEATURE = "LcdCommon";
    public static final String MODEM_FEATURE = "modem";
    private static final int MSG_NAVI_STATUS = 4;
    private static final int MSG_REPORT_DATA = 1;
    private static final int MSG_TOP_APP_SWITCH = 5;
    private static final String TAG = "DevSchedFeatureRt";
    private static final String TOP_APP_INTENT_RESON = "android.intent.extra.REASON";
    private static final String TOP_APP_PID = "toPid";
    private static final String TOP_APP_PKG = "toPackage";
    private static final String TOP_APP_UID = "toUid";
    private static final String TOP_DISPLAY_ID = "toDisplayId";
    public static final String WAKELOCK_FEATURE = "ScreenOnWakelock";
    public static final String WIFI_FEATURE = "wifi";
    private static AtomicBoolean sRunning = new AtomicBoolean(false);
    private static Map<String, Class<?>> sSubFeatureCustObj = new ArrayMap();
    private static Map<String, DevSchedFeatureBase> sSubFeatureObjMap = new ArrayMap();
    private static Map<String, Class<?>> sSubFeaturePlatformObj = new ArrayMap();
    private AwareStateCallback mAwareStateCallback = null;
    private Context mContext = null;
    private DevSchedHandler mDevSchedHandler = null;
    private TopAppCallBack mTopAppCallBack = null;

    static {
        sSubFeatureCustObj.put(LCD_FEATURE, LcdSchedFeatureRt.class);
    }

    public DevSchedFeatureRt(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        this.mContext = context;
        this.mDevSchedHandler = new DevSchedHandler();
        AwareLog.d(TAG, "create DevSchedFeatureRt success.");
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enableFeatureEx(int realVersion) {
        AwareLog.d(TAG, "enableFeatureEx realVersion=" + realVersion);
        if (realVersion >= 3) {
            enableAwareThree();
        } else if (realVersion >= 2) {
            enableAwareTwo();
        } else {
            AwareLog.i(TAG, "enableFeatureEx failed, realVersion: " + realVersion);
            return false;
        }
        registerTopAppCallBack();
        sRunning.set(true);
        return true;
    }

    private void enableAwareTwo() {
        sSubFeatureObjMap.clear();
        createEnabledSubFeature(sSubFeatureCustObj, true);
        createEnabledSubFeature(sSubFeaturePlatformObj, false);
        PhoneStatusRecong.getInstance().init(this.mContext);
        PhoneStatusRecong.getInstance().connectService();
        subscribleEvents();
        registerStateCallback();
        AwareLog.d(TAG, "enableAwareTwo SUCCESS");
    }

    private void enableAwareThree() {
        sSubFeaturePlatformObj.put(WIFI_FEATURE, NetLocationSchedFeatureRt.class);
        sSubFeaturePlatformObj.put(MODEM_FEATURE, NetLocationSchedFeatureRt.class);
        sSubFeatureCustObj.put(BLE_FEATURE, BleSchedFeatureRt.class);
        sSubFeaturePlatformObj.put(WAKELOCK_FEATURE, ScreenOnWakelockSchedFeatureRt.class);
        enableAwareTwo();
        AwareLog.d(TAG, "enableAwareThree SUCCESS");
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        AwareLog.d(TAG, "DevSchedFeatureRt, enable.");
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        AwareLog.d(TAG, "DevSchedFeatureRt, disable.");
        sRunning.set(false);
        unregisterStateCallback();
        unSubscribeEvents();
        PhoneStatusRecong.getInstance().disconnectService();
        unregisterTopAppCallBack();
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        Message msg = Message.obtain();
        msg.what = 1;
        msg.obj = data;
        this.mDevSchedHandler.sendMessage(msg);
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean custConfigUpdate() {
        return true;
    }

    private void subscribleEvents() {
        if (this.mDataRegister != null) {
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
        }
    }

    private void unSubscribeEvents() {
        if (this.mDataRegister != null) {
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
        }
    }

    /* access modifiers changed from: private */
    public class TopAppCallBack extends IHwActivityNotifierEx {
        private TopAppCallBack() {
        }

        public void call(Bundle extras) {
            if (extras != null && DevSchedFeatureRt.APP_SWITCH_REASON.equals(extras.getString(DevSchedFeatureRt.TOP_APP_INTENT_RESON))) {
                Message msg = Message.obtain();
                msg.what = 5;
                msg.obj = extras;
                DevSchedFeatureRt.this.mDevSchedHandler.sendMessage(msg);
            }
        }
    }

    private void registerTopAppCallBack() {
        if (this.mTopAppCallBack == null) {
            this.mTopAppCallBack = new TopAppCallBack();
            AwareCallback.getInstance().registerActivityNotifier(this.mTopAppCallBack, APP_SWITCH_REASON);
        }
    }

    private void unregisterTopAppCallBack() {
        if (this.mTopAppCallBack != null) {
            AwareCallback.getInstance().unregisterActivityNotifier(this.mTopAppCallBack, APP_SWITCH_REASON);
            this.mTopAppCallBack = null;
        }
    }

    private boolean isConfigEnable(FeatureXmlConfigParserRt.FeatureXmlConfig config) {
        return config == null || !config.subSwitch;
    }

    private void createAndInitFeatureInstance(String subFeature, Class<?> classObj, FeatureXmlConfigParserRt.FeatureXmlConfig config) {
        try {
            Object obj = classObj.getConstructors()[0].newInstance(this.mContext, subFeature);
            if (obj == null) {
                return;
            }
            if (obj instanceof DevSchedFeatureBase) {
                DevSchedFeatureBase subFeatureObj = (DevSchedFeatureBase) obj;
                subFeatureObj.readFeatureConfig(config);
                sSubFeatureObjMap.put(subFeature, subFeatureObj);
            }
        } catch (ArrayIndexOutOfBoundsException | IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException err) {
            AwareLog.e(TAG, "createEnabledSubFeature Exception " + err.getMessage());
        }
    }

    private void createEnabledSubFeature(Map<String, Class<?>> featureMap, boolean isCust) {
        String subFeature;
        Class<?> classObj;
        if (!featureMap.isEmpty()) {
            for (Map.Entry<String, Class<?>> entry : featureMap.entrySet()) {
                if (!(entry == null || (subFeature = entry.getKey()) == null)) {
                    FeatureXmlConfigParserRt.FeatureXmlConfig config = FeatureXmlConfigParserRt.parseFeatureXmlConfig(subFeature, !isCust ? 1 : 0);
                    if (!isConfigEnable(config) && (classObj = entry.getValue()) != null) {
                        createAndInitFeatureInstance(subFeature, classObj, config);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class DevSchedHandler extends Handler {
        private DevSchedHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (!DevSchedFeatureRt.sRunning.get()) {
                AwareLog.i(DevSchedFeatureRt.TAG, "DevSchedHandler, feature sRunning is false, return!");
            } else if (msg == null) {
                AwareLog.e(DevSchedFeatureRt.TAG, "DevSchedHandler, msg is null, error!");
            } else {
                Object dataObj = msg.obj;
                int i = msg.what;
                if (i != 1) {
                    if (i == 4) {
                        DevSchedFeatureRt.this.handleNaviStatus(msg.arg1);
                    } else if (i != 5) {
                        AwareLog.i(DevSchedFeatureRt.TAG, "DevSchedHandler, default branch, msg.what is " + msg.what);
                    } else if (dataObj instanceof Bundle) {
                        DevSchedFeatureRt.this.handleTopAppSwitch((Bundle) dataObj);
                    }
                } else if (dataObj instanceof CollectData) {
                    DevSchedFeatureRt.this.handleReportData((CollectData) dataObj);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleReportData(CollectData data) {
        if (data != null) {
            AwareConstant.ResourceType type = AwareConstant.ResourceType.getResourceType(data.getResId());
            if (type == AwareConstant.ResourceType.RESOURCE_SCREEN_ON || type == AwareConstant.ResourceType.RESOURCE_SCREEN_OFF) {
                DevSchedFeatureBase.setScreenState(type == AwareConstant.ResourceType.RESOURCE_SCREEN_ON ? DevSchedFeatureBase.ScreenState.ScreenOn : DevSchedFeatureBase.ScreenState.ScreenOff);
                for (DevSchedFeatureBase subFeatureObj : sSubFeatureObjMap.values()) {
                    if (subFeatureObj != null) {
                        subFeatureObj.handleScreenStateChange(type == AwareConstant.ResourceType.RESOURCE_SCREEN_ON ? DevSchedFeatureBase.ScreenState.ScreenOn : DevSchedFeatureBase.ScreenState.ScreenOff);
                    }
                }
                return;
            }
            AwareLog.d(TAG, "no register event");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNaviStatus(int eventType) {
        boolean isInNavi = true;
        if (eventType != 1) {
            isInNavi = false;
        }
        for (DevSchedFeatureBase subFeatureObj : sSubFeatureObjMap.values()) {
            if (subFeatureObj != null) {
                subFeatureObj.handleNaviStatus(isInNavi);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleTopAppSwitch(Bundle bundle) {
        if (bundle != null) {
            int pid = bundle.getInt(TOP_APP_PID, -1);
            int uid = bundle.getInt(TOP_APP_UID, -1);
            int displayId = bundle.getInt(TOP_DISPLAY_ID, -1);
            String pkgName = bundle.getString(TOP_APP_PKG);
            if (pid < 0 || uid < 0 || pkgName == null) {
                AwareLog.w(TAG, "handleTopAppSwitch pid or uid or pkgName invalid!");
                return;
            }
            reportTopAppToAwareNrt(pid, uid, displayId, pkgName);
            for (DevSchedFeatureBase subFeatureObj : sSubFeatureObjMap.values()) {
                if (subFeatureObj != null) {
                    subFeatureObj.handleTopAppChange(pid, uid, pkgName);
                }
            }
        }
    }

    private void reportTopAppToAwareNrt(int pid, int uid, int displayId, String pkgName) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(String.valueOf(pid));
        stringBuffer.append(CpuCustBaseConfig.CPUCONFIG_INVALID_STR);
        stringBuffer.append(String.valueOf(uid));
        stringBuffer.append(CpuCustBaseConfig.CPUCONFIG_INVALID_STR);
        stringBuffer.append(pkgName);
        stringBuffer.append(CpuCustBaseConfig.CPUCONFIG_INVALID_STR);
        stringBuffer.append(String.valueOf(displayId));
        LogIAware.report(AwareNRTConstant.FIRST_DEV_SCHED_EVENT_ID, stringBuffer.toString());
    }

    public static NetLocationStrategy getNetLocationStrategy(String pkgName, int uid, int type) {
        String feature;
        if (!sRunning.get()) {
            AwareLog.i(TAG, "getNetLocationStrategy, sRunning is false, return!");
            return null;
        }
        if (type == 1) {
            feature = WIFI_FEATURE;
        } else if (type != 2) {
            AwareLog.i(TAG, "getNetLocationStrategy, Wrong Location type, return!");
            return null;
        } else {
            feature = MODEM_FEATURE;
        }
        DevSchedFeatureBase netLocationFeature = sSubFeatureObjMap.get(feature);
        if (netLocationFeature == null) {
            AwareLog.i(TAG, "netLocationFeature is null. feature:" + feature);
            return null;
        } else if (netLocationFeature instanceof NetLocationSchedFeatureRt) {
            return ((NetLocationSchedFeatureRt) netLocationFeature).getNetLocationStrategy(pkgName, uid);
        } else {
            AwareLog.i(TAG, "netLocationFeature is not obj of NetLocationSchedFeatureRt, wifiFeature : " + netLocationFeature.getClass().getName());
            return null;
        }
    }

    public static boolean isAwarePreventWakelockScreenOn(String pkgName, String tag) {
        if (!sRunning.get()) {
            AwareLog.i(TAG, "isAwarePreventWakelockScreenOn, sRunning is false, return!");
            return false;
        }
        DevSchedFeatureBase wakelockFeature = sSubFeatureObjMap.get(WAKELOCK_FEATURE);
        if (wakelockFeature == null) {
            AwareLog.i(TAG, "wakelockFeature is null.");
            return false;
        } else if (wakelockFeature instanceof ScreenOnWakelockSchedFeatureRt) {
            return ((ScreenOnWakelockSchedFeatureRt) wakelockFeature).isAwarePreventWakelockScreenOn(pkgName, tag);
        } else {
            AwareLog.i(TAG, "wakelockFeature is not obj of ScreenOnWakelockSchedFeatureRt, wakelockFeature : " + wakelockFeature.getClass().getName());
            return false;
        }
    }

    private void registerStateCallback() {
        if (this.mAwareStateCallback == null) {
            this.mAwareStateCallback = new AwareStateCallback();
            AwareAppKeyBackgroup.getInstance().registerStateCallback(this.mAwareStateCallback, 3);
        }
    }

    private void unregisterStateCallback() {
        if (this.mAwareStateCallback != null) {
            AwareAppKeyBackgroup.getInstance().unregisterStateCallback(this.mAwareStateCallback, 3);
            this.mAwareStateCallback = null;
        }
    }

    /* access modifiers changed from: private */
    public class AwareStateCallback implements AwareAppKeyBackgroup.IAwareStateCallback {
        private AwareStateCallback() {
        }

        @Override // com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup.IAwareStateCallback
        public void onStateChanged(int stateType, int eventType, int pid, int uid) {
            if (!DevSchedFeatureRt.sRunning.get() || DevSchedFeatureRt.this.mDevSchedHandler == null) {
                AwareLog.i(DevSchedFeatureRt.TAG, "DevSchedHandler, feature sRunning is false, return!");
            } else if (stateType == 3) {
                Message msg = Message.obtain();
                msg.what = 4;
                msg.arg1 = eventType;
                DevSchedFeatureRt.this.mDevSchedHandler.sendMessage(msg);
            }
        }
    }

    public static void registerDevModeMethod(int deviceId, IDeviceSettingCallback callback, Bundle args) {
        if (!sRunning.get()) {
            AwareLog.i(TAG, "registerDevModeMethod, sRunning is false, return!");
        } else {
            DevSchedCallbackManager.getInstance().registerDevModeMethod(deviceId, callback, args);
        }
    }

    public static void unregisterDevModeMethod(int deviceId, IDeviceSettingCallback callback, Bundle args) {
        if (!sRunning.get()) {
            AwareLog.i(TAG, "unregisterDevModeMethod, sRunning is false, return!");
        } else {
            DevSchedCallbackManager.getInstance().unregisterDevModeMethod(deviceId, callback, args);
        }
    }

    public static boolean isDeviceIdAvailable(int deviceId) {
        if (!sRunning.get()) {
            AwareLog.i(TAG, "isDeviceIdAvailable, sRunning is false, return!");
            return false;
        } else if (getDeviceObjById(deviceId) == null) {
            return false;
        } else {
            return true;
        }
    }

    public static void sendCurrentDeviceMode(int deviceId) {
        if (!sRunning.get()) {
            AwareLog.i(TAG, "sendCurrentDeviceMode, sRunning is false, return!");
            return;
        }
        DevSchedFeatureBase deviceObj = getDeviceObjById(deviceId);
        if (deviceObj == null) {
            AwareLog.i(TAG, "sendCurrentDeviceMode, no deviceObj for deviceId:" + deviceId);
            return;
        }
        deviceObj.sendCurrentDeviceMode();
    }

    private static DevSchedFeatureBase getDeviceObjById(int deviceId) {
        if (deviceId == -1) {
            return null;
        }
        for (DevSchedFeatureBase subFeatureObj : sSubFeatureObjMap.values()) {
            if (subFeatureObj != null && subFeatureObj.getDeviceId() == deviceId) {
                return subFeatureObj;
            }
        }
        return null;
    }
}

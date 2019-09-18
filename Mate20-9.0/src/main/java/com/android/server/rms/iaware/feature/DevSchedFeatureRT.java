package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.IDeviceSettingCallback;
import android.rms.iaware.NetLocationStrategy;
import android.util.ArrayMap;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.dev.BLESchedFeatureRT;
import com.android.server.rms.iaware.dev.DevSchedCallbackManager;
import com.android.server.rms.iaware.dev.DevSchedFeatureBase;
import com.android.server.rms.iaware.dev.DevXmlConfig;
import com.android.server.rms.iaware.dev.GpsSchedFeatureRT;
import com.android.server.rms.iaware.dev.LCDSchedFeatureRT;
import com.android.server.rms.iaware.dev.NetLocationSchedFeatureRT;
import com.android.server.rms.iaware.dev.PhoneStatusRecong;
import com.android.server.rms.iaware.dev.ScreenOnWakelockSchedFeatureRT;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class DevSchedFeatureRT extends RFeature {
    private static final int BASE_VERSION2 = 2;
    private static final int BASE_VERSION3 = 3;
    private static final String BLE_FEATURE = "ble";
    private static final String DISABLE_VALUE = "0";
    private static final int DUMP_WIFI_ARG_LENGTH = 4;
    public static final int DUMP_WIFI_ARG_LENGTH_STEP = 2;
    private static final String ENABLE_VALUE = "1";
    private static final String GPS_FEATURE = "gps";
    private static final String LCD_FEATURE = "lcd";
    public static final String MODEM_FEATURE = "modem";
    private static final int MSG_NAVI_STATUS = 4;
    private static final int MSG_REPORT_DATA = 1;
    private static final int MSG_UPDATE_CONFIG = 2;
    private static final String TAG = "DevSchedFeatureRT";
    private static final String WAKELOCK_FEATURE = "wakelock";
    private static final String WIFI_DUMPSYS_PARAM = "--test-dev-wifi";
    public static final String WIFI_FEATURE = "wifi";
    /* access modifiers changed from: private */
    public static final AtomicBoolean mRunning = new AtomicBoolean(false);
    private static final Map<String, Class<?>> mSubFeatureObj = new ArrayMap();
    private static final Map<String, DevSchedFeatureBase> mSubFeatureObjMap = new ArrayMap();
    private AwareStateCallback mAwareStateCallback = null;
    private Context mContext = null;
    /* access modifiers changed from: private */
    public DevSchedHandler mDevSchedHandler = null;
    private final Map<String, String> mSubFeatureSwitch = new ArrayMap();

    private class AwareStateCallback implements AwareAppKeyBackgroup.IAwareStateCallback {
        private AwareStateCallback() {
        }

        public void onStateChanged(int stateType, int eventType, int pid, int uid) {
            if (!DevSchedFeatureRT.mRunning.get() || DevSchedFeatureRT.this.mDevSchedHandler == null) {
                AwareLog.i(DevSchedFeatureRT.TAG, "DevSchedHandler, feature mRunning is false, return!");
            } else if (stateType == 3) {
                Message msg = Message.obtain();
                msg.what = 4;
                msg.arg1 = eventType;
                DevSchedFeatureRT.this.mDevSchedHandler.sendMessage(msg);
            }
        }
    }

    private class DevSchedHandler extends Handler {
        private DevSchedHandler() {
        }

        public void handleMessage(Message msg) {
            if (!DevSchedFeatureRT.mRunning.get()) {
                AwareLog.i(DevSchedFeatureRT.TAG, "DevSchedHandler, feature mRunning is false, return!");
            } else if (msg == null) {
                AwareLog.e(DevSchedFeatureRT.TAG, "DevSchedHandler, msg is null, error!");
            } else {
                int i = msg.what;
                if (i != 4) {
                    switch (i) {
                        case 1:
                            if (msg.obj instanceof CollectData) {
                                DevSchedFeatureRT.this.handlerReportData((CollectData) msg.obj);
                                break;
                            } else {
                                return;
                            }
                        case 2:
                            DevSchedFeatureRT.this.handlerUpdateCustConfig();
                            break;
                        default:
                            AwareLog.i(DevSchedFeatureRT.TAG, "DevSchedHandler, default branch, msg.what is " + msg.what);
                            return;
                    }
                } else {
                    DevSchedFeatureRT.this.handlerNaviStatus(msg.arg1);
                }
            }
        }
    }

    static {
        mSubFeatureObj.put(GPS_FEATURE, GpsSchedFeatureRT.class);
        mSubFeatureObj.put(LCD_FEATURE, LCDSchedFeatureRT.class);
    }

    public DevSchedFeatureRT(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        this.mContext = context;
        this.mDevSchedHandler = new DevSchedHandler();
        AwareLog.d(TAG, "create DevSchedFeatureRT success.");
    }

    public boolean enableFeatureEx(int realVersion) {
        AwareLog.d(TAG, "enableFeatureEx realVersion=" + realVersion);
        if (realVersion >= 3) {
            enableIAware3();
        } else if (realVersion >= 2) {
            enableIAware2();
        } else {
            AwareLog.i(TAG, "enableFeatureEx failed, realVersion: " + realVersion);
            return false;
        }
        mRunning.set(true);
        return true;
    }

    private void enableIAware2() {
        DevXmlConfig.loadSubFeatureSwitch(this.mSubFeatureSwitch);
        createEnabledSubFeature(mSubFeatureObjMap);
        PhoneStatusRecong.getInstance().init(this.mContext);
        PhoneStatusRecong.getInstance().connectService();
        subscribleEvents();
        registerStateCallback();
        AwareLog.d(TAG, "enableIAware2 SUCCESS");
    }

    private void enableIAware3() {
        mSubFeatureObj.put(WIFI_FEATURE, NetLocationSchedFeatureRT.class);
        mSubFeatureObj.put(MODEM_FEATURE, NetLocationSchedFeatureRT.class);
        mSubFeatureObj.put(BLE_FEATURE, BLESchedFeatureRT.class);
        mSubFeatureObj.put(WAKELOCK_FEATURE, ScreenOnWakelockSchedFeatureRT.class);
        enableIAware2();
        AwareLog.d(TAG, "enableIAware3 SUCCESS");
    }

    public boolean enable() {
        AwareLog.d(TAG, "DevSchedFeatureRT, enable.");
        return false;
    }

    public boolean disable() {
        AwareLog.d(TAG, "DevSchedFeatureRT, disable.");
        mRunning.set(false);
        unregisterStateCallback();
        unSubscribeEvents();
        PhoneStatusRecong.getInstance().disconnectService();
        return true;
    }

    public boolean reportData(CollectData data) {
        Message msg = Message.obtain();
        msg.what = 1;
        msg.obj = data;
        this.mDevSchedHandler.sendMessage(msg);
        return true;
    }

    public boolean custConfigUpdate() {
        this.mDevSchedHandler.sendEmptyMessage(2);
        return true;
    }

    private void subscribleEvents() {
        if (this.mIRDataRegister != null) {
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RES_APP, this.mFeatureType);
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
        }
    }

    private void unSubscribeEvents() {
        if (this.mIRDataRegister != null) {
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RES_APP, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
        }
    }

    private void createEnabledSubFeature(Map<String, DevSchedFeatureBase> subFeatureObjMap) {
        if (subFeatureObjMap != null) {
            subFeatureObjMap.clear();
            if (mSubFeatureObj.size() != 0) {
                for (Map.Entry<String, Class<?>> entry : mSubFeatureObj.entrySet()) {
                    if (entry != null) {
                        String subFeature = entry.getKey();
                        if (subFeature != null && isSubFeatureEnable(subFeature)) {
                            Class<?> classObj = entry.getValue();
                            if (classObj != null) {
                                try {
                                    subFeatureObjMap.put(subFeature, (DevSchedFeatureBase) classObj.getConstructors()[0].newInstance(new Object[]{this.mContext, subFeature}));
                                } catch (IllegalArgumentException e) {
                                    AwareLog.e(TAG, "createEnabledSubFeature IllegalArgumentException");
                                } catch (IllegalAccessException e2) {
                                    AwareLog.e(TAG, "createEnabledSubFeature IllegalAccessException");
                                } catch (InstantiationException e3) {
                                    AwareLog.e(TAG, "createEnabledSubFeature InstantiationException");
                                } catch (InvocationTargetException e4) {
                                    AwareLog.e(TAG, "createEnabledSubFeature InvocationTargetException");
                                } catch (ArrayIndexOutOfBoundsException e5) {
                                    AwareLog.e(TAG, "createEnabledSubFeature ArrayIndexOutOfBoundsException");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isSubFeatureEnable(String subFeature) {
        if (subFeature == null || subFeature.isEmpty() || !"1".equals(this.mSubFeatureSwitch.get(subFeature))) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void handlerReportData(CollectData data) {
        if (data != null) {
            long timestamp = data.getTimeStamp();
            AwareConstant.ResourceType type = AwareConstant.ResourceType.getResourceType(data.getResId());
            if (type == AwareConstant.ResourceType.RES_APP) {
                String eventData = data.getData();
                AttrSegments.Builder builder = new AttrSegments.Builder();
                builder.addCollectData(eventData);
                AttrSegments attrSegments = builder.build();
                if (!attrSegments.isValid()) {
                    AwareLog.e(TAG, "Invalid collectData, or event");
                    return;
                }
                for (DevSchedFeatureBase subFeatureObj : mSubFeatureObjMap.values()) {
                    if (subFeatureObj != null) {
                        subFeatureObj.handleResAppData(timestamp, attrSegments.getEvent().intValue(), attrSegments);
                    }
                }
            } else if (type == AwareConstant.ResourceType.RESOURCE_SCREEN_ON || type == AwareConstant.ResourceType.RESOURCE_SCREEN_OFF) {
                DevSchedFeatureBase.setScreenState(type == AwareConstant.ResourceType.RESOURCE_SCREEN_ON ? DevSchedFeatureBase.ScreenState.ScreenOn : DevSchedFeatureBase.ScreenState.ScreenOff);
                for (DevSchedFeatureBase subFeatureObj2 : mSubFeatureObjMap.values()) {
                    if (subFeatureObj2 != null) {
                        subFeatureObj2.handScreenStateChange(type == AwareConstant.ResourceType.RESOURCE_SCREEN_ON ? DevSchedFeatureBase.ScreenState.ScreenOn : DevSchedFeatureBase.ScreenState.ScreenOff);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handlerUpdateCustConfig() {
        for (DevSchedFeatureBase subFeatureObj : mSubFeatureObjMap.values()) {
            if (subFeatureObj != null) {
                subFeatureObj.handleUpdateCustConfig();
            }
        }
    }

    /* access modifiers changed from: private */
    public void handlerNaviStatus(int eventType) {
        boolean isInNavi = true;
        if (1 != eventType) {
            isInNavi = false;
        }
        for (DevSchedFeatureBase subFeatureObj : mSubFeatureObjMap.values()) {
            if (subFeatureObj != null) {
                subFeatureObj.handlerNaviStatus(isInNavi);
            }
        }
    }

    public static NetLocationStrategy getNetLocationStrategy(String pkgName, int uid, int type) {
        String feature;
        if (!mRunning.get()) {
            AwareLog.i(TAG, "getNetLocationStrategy, mRunning is false, return!");
            return null;
        }
        switch (type) {
            case 1:
                feature = WIFI_FEATURE;
                break;
            case 2:
                feature = MODEM_FEATURE;
                break;
            default:
                AwareLog.i(TAG, "getNetLocationStrategy, Wrong Location type, return!");
                return null;
        }
        DevSchedFeatureBase netLocationFeature = mSubFeatureObjMap.get(feature);
        if (netLocationFeature == null) {
            AwareLog.i(TAG, "netLocationFeature is null. feature:" + feature);
            return null;
        } else if (netLocationFeature instanceof NetLocationSchedFeatureRT) {
            return ((NetLocationSchedFeatureRT) netLocationFeature).getNetLocationStrategy(pkgName, uid);
        } else {
            AwareLog.i(TAG, "netLocationFeature is not obj of NetLocationSchedFeatureRT, wifiFeature : " + netLocationFeature.getClass().getName());
            return null;
        }
    }

    public static boolean isAwarePreventWakelockScreenOn(String pkgName, String tag) {
        if (!mRunning.get()) {
            AwareLog.i(TAG, "isAwarePreventWakelockScreenOn, mRunning is false, return!");
            return false;
        }
        DevSchedFeatureBase wakelockFeature = mSubFeatureObjMap.get(WAKELOCK_FEATURE);
        if (wakelockFeature == null) {
            AwareLog.i(TAG, "wakelockFeature is null.");
            return false;
        } else if (wakelockFeature instanceof ScreenOnWakelockSchedFeatureRT) {
            return ((ScreenOnWakelockSchedFeatureRT) wakelockFeature).isAwarePreventWakelockScreenOn(pkgName, tag);
        } else {
            AwareLog.i(TAG, "wakelockFeature is not obj of ScreenOnWakelockSchedFeatureRT, wakelockFeature : " + wakelockFeature.getClass().getName());
            return false;
        }
    }

    public static int getAppTypeForLCD(String pkgName) {
        if (!mRunning.get()) {
            AwareLog.i(TAG, "getAppTypeForLCD, mRunning is false, return!");
            return 255;
        }
        DevSchedFeatureBase lcdFeature = mSubFeatureObjMap.get(LCD_FEATURE);
        if (lcdFeature == null) {
            AwareLog.i(TAG, "lcdFeature is null.");
            return 255;
        } else if (lcdFeature instanceof LCDSchedFeatureRT) {
            return ((LCDSchedFeatureRT) lcdFeature).getAppType(pkgName);
        } else {
            AwareLog.i(TAG, "lcdFeature is not obj of LCDSchedFeatureRT, lcdFeature : " + lcdFeature.getClass().getName());
            return 255;
        }
    }

    public static final boolean doDumpsys(RFeature feature, FileDescriptor fd, PrintWriter pw, String[] args) {
        if (!mRunning.get()) {
            AwareLog.i(TAG, "doDumpsys, mRunning is false, return!");
            return false;
        }
        if (!(args != null && args.length > 0 && feature != null && (feature instanceof DevSchedFeatureRT))) {
            return false;
        }
        if (WIFI_DUMPSYS_PARAM.equals(args[0])) {
            doDumpsysWifi(args);
        }
        return true;
    }

    private static void doDumpsysWifi(String[] args) {
        if (args == null) {
            return;
        }
        if (args.length == 4 || args.length == 6) {
            PhoneStatusRecong.getInstance().doDumpsys(args);
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

    public static void registerDevModeMethod(int deviceId, IDeviceSettingCallback callback, Bundle args) {
        if (!mRunning.get()) {
            AwareLog.i(TAG, "registerDevModeMethod, mRunning is false, return!");
        } else {
            DevSchedCallbackManager.getInstance().registerDevModeMethod(deviceId, callback, args);
        }
    }

    public static void unregisterDevModeMethod(int deviceId, IDeviceSettingCallback callback, Bundle args) {
        if (!mRunning.get()) {
            AwareLog.i(TAG, "unregisterDevModeMethod, mRunning is false, return!");
        } else {
            DevSchedCallbackManager.getInstance().unregisterDevModeMethod(deviceId, callback, args);
        }
    }

    public static boolean checkDeviceIdAvailable(int deviceId) {
        if (!mRunning.get()) {
            AwareLog.i(TAG, "checkDeviceIdAvailable, mRunning is false, return!");
            return false;
        } else if (getDeviceObjById(deviceId) == null) {
            return false;
        } else {
            return true;
        }
    }

    public static void sendCurrentDeviceMode(int deviceId) {
        if (!mRunning.get()) {
            AwareLog.i(TAG, "sendCurrentDeviceMode, mRunning is false, return!");
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
        if (-1 == deviceId) {
            return null;
        }
        for (DevSchedFeatureBase subFeatureObj : mSubFeatureObjMap.values()) {
            if (subFeatureObj != null && subFeatureObj.getDeviceId() == deviceId) {
                return subFeatureObj;
            }
        }
        return null;
    }
}

package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.NetLocationStrategy;
import android.util.ArrayMap;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup.IAwareStateCallback;
import com.android.server.rms.iaware.dev.DevSchedFeatureBase;
import com.android.server.rms.iaware.dev.DevXmlConfig;
import com.android.server.rms.iaware.dev.GpsSchedFeatureRT;
import com.android.server.rms.iaware.dev.NetLocationSchedFeatureRT;
import com.android.server.rms.iaware.dev.PhoneStatusRecong;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import com.android.server.rms.iaware.memory.data.content.AttrSegments.Builder;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public class DevSchedFeatureRT extends RFeature {
    private static final int BASE_VERSION = 2;
    private static final String DISABLE_VALUE = "0";
    private static final int DUMP_ARG_LENGTH = 4;
    private static final String ENABLE_VALUE = "1";
    private static final String GPS_FEATURE = "gps";
    private static final String MODEM_FEATURE = "modem";
    private static final int MSG_NAVI_STATUS = 4;
    private static final int MSG_REPORT_DATA = 1;
    private static final int MSG_UPDATE_CONFIG = 2;
    private static final String TAG = "DevSchedFeatureRT";
    private static final String WIFI_FEATURE = "wifi";
    private static final AtomicBoolean mRunning = new AtomicBoolean(false);
    private static final Map<String, Class<?>> mSubFeatureObj = new ArrayMap();
    private static final Map<String, DevSchedFeatureBase> mSubFeatureObjMap = new ArrayMap();
    private AwareStateCallback mAwareStateCallback = null;
    private Context mContext = null;
    private DevSchedHandler mDevSchedHandler = null;
    private final Map<String, String> mSubFeatureSwitch = new ArrayMap();

    private class AwareStateCallback implements IAwareStateCallback {
        /* synthetic */ AwareStateCallback(DevSchedFeatureRT this$0, AwareStateCallback -this1) {
            this();
        }

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
        /* synthetic */ DevSchedHandler(DevSchedFeatureRT this$0, DevSchedHandler -this1) {
            this();
        }

        private DevSchedHandler() {
        }

        public void handleMessage(Message msg) {
            if (!DevSchedFeatureRT.mRunning.get()) {
                AwareLog.i(DevSchedFeatureRT.TAG, "DevSchedHandler, feature mRunning is false, return!");
            } else if (msg == null) {
                AwareLog.e(DevSchedFeatureRT.TAG, "DevSchedHandler, msg is null, error!");
            } else {
                switch (msg.what) {
                    case 1:
                        if (msg.obj instanceof CollectData) {
                            DevSchedFeatureRT.this.handlerReportData(msg.obj);
                            break;
                        }
                        return;
                    case 4:
                        DevSchedFeatureRT.this.handlerNaviStatus(msg.arg1);
                        break;
                    default:
                        AwareLog.i(DevSchedFeatureRT.TAG, "DevSchedHandler, default branch, msg.what is " + msg.what);
                        return;
                }
            }
        }
    }

    static {
        mSubFeatureObj.put(GPS_FEATURE, GpsSchedFeatureRT.class);
        mSubFeatureObj.put(WIFI_FEATURE, NetLocationSchedFeatureRT.class);
        mSubFeatureObj.put(MODEM_FEATURE, NetLocationSchedFeatureRT.class);
    }

    public DevSchedFeatureRT(Context context, FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        this.mContext = context;
        this.mDevSchedHandler = new DevSchedHandler(this, null);
        DevXmlConfig.loadSubFeatureSwitch(this.mSubFeatureSwitch);
        createEnabledSubFeature(mSubFeatureObjMap);
        PhoneStatusRecong.getInstance().connectService(this.mContext);
        AwareLog.d(TAG, "create DevSchedFeatureRT success.");
    }

    public boolean enableFeatureEx(int realVersion) {
        AwareLog.d(TAG, "enableFeatureEx realVersion=" + realVersion);
        if (realVersion < 2) {
            AwareLog.i(TAG, "enableFeatureEx failed, realVersion: " + realVersion + ", baseVersion: " + 2);
            return false;
        }
        subscribleEvents();
        registerStateCallback();
        mRunning.set(true);
        return true;
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
        return true;
    }

    public boolean reportData(CollectData data) {
        Message msg = Message.obtain();
        msg.what = 1;
        msg.obj = data;
        this.mDevSchedHandler.sendMessage(msg);
        return true;
    }

    private void subscribleEvents() {
        if (this.mIRDataRegister != null) {
            this.mIRDataRegister.subscribeData(ResourceType.RES_APP, this.mFeatureType);
        }
    }

    private void unSubscribeEvents() {
        if (this.mIRDataRegister != null) {
            this.mIRDataRegister.unSubscribeData(ResourceType.RES_APP, this.mFeatureType);
        }
    }

    private void createEnabledSubFeature(Map<String, DevSchedFeatureBase> subFeatureObjMap) {
        if (subFeatureObjMap != null) {
            subFeatureObjMap.clear();
            if (mSubFeatureObj.size() != 0) {
                for (Entry<String, Class<?>> entry : mSubFeatureObj.entrySet()) {
                    if (entry != null) {
                        String subFeature = (String) entry.getKey();
                        if (subFeature != null && isSubFeatureEnable(subFeature)) {
                            Class<?> classObj = (Class) entry.getValue();
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
        if (subFeature == null || subFeature.isEmpty()) {
            return false;
        }
        if ("1".equals((String) this.mSubFeatureSwitch.get(subFeature))) {
            return true;
        }
        return false;
    }

    private void handlerReportData(CollectData data) {
        if (data != null) {
            long timestamp = data.getTimeStamp();
            if (ResourceType.getResourceType(data.getResId()) == ResourceType.RES_APP) {
                String eventData = data.getData();
                Builder builder = new Builder();
                builder.addCollectData(eventData);
                AttrSegments attrSegments = builder.build();
                if (attrSegments == null || (attrSegments.isValid() ^ 1) != 0) {
                    AwareLog.e(TAG, "Invalid collectData, or event");
                    return;
                }
                for (DevSchedFeatureBase subFeatureObj : mSubFeatureObjMap.values()) {
                    if (subFeatureObj != null) {
                        subFeatureObj.handleResAppData(timestamp, attrSegments.getEvent().intValue(), attrSegments);
                    }
                }
            }
        }
    }

    private void handlerNaviStatus(int eventType) {
        boolean isInNavi = 1 == eventType;
        for (DevSchedFeatureBase subFeatureObj : mSubFeatureObjMap.values()) {
            if (subFeatureObj != null) {
                subFeatureObj.handlerNaviStatus(isInNavi);
            }
        }
    }

    public static NetLocationStrategy getNetLocationStrategy(String pkgName, int uid, int type) {
        if (mRunning.get()) {
            String feature;
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
            DevSchedFeatureBase netLocationFeature = (DevSchedFeatureBase) mSubFeatureObjMap.get(feature);
            if (netLocationFeature == null) {
                AwareLog.i(TAG, "netLocationFeature is null.");
                return null;
            } else if (netLocationFeature instanceof NetLocationSchedFeatureRT) {
                return ((NetLocationSchedFeatureRT) netLocationFeature).getNetLocationStrategy(pkgName, uid);
            } else {
                AwareLog.i(TAG, "netLocationFeature is not obj of NetLocationSchedFeatureRT, wifiFeature : " + netLocationFeature.getClass().getName());
                return null;
            }
        }
        AwareLog.i(TAG, "getNetLocationStrategy, mRunning is false, return!");
        return null;
    }

    public static final boolean doDumpsys(RFeature feature, FileDescriptor fd, PrintWriter pw, String[] args) {
        if (mRunning.get()) {
            if (args != null && args.length == 4 && feature != null && (feature instanceof DevSchedFeatureRT)) {
                if (!"--test-Dev".equals(args[0])) {
                    return false;
                }
                PhoneStatusRecong.getInstance().doDumpsys(args);
            }
            return true;
        }
        AwareLog.i(TAG, "doDumpsys, mRunning is false, return!");
        return false;
    }

    private void registerStateCallback() {
        if (this.mAwareStateCallback == null) {
            this.mAwareStateCallback = new AwareStateCallback(this, null);
            AwareAppKeyBackgroup.getInstance().registerStateCallback(this.mAwareStateCallback, 3);
        }
    }

    private void unregisterStateCallback() {
        if (this.mAwareStateCallback != null) {
            AwareAppKeyBackgroup.getInstance().unregisterStateCallback(this.mAwareStateCallback, 3);
            this.mAwareStateCallback = null;
        }
    }
}

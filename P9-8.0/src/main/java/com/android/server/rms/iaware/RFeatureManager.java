package com.android.server.rms.iaware;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.DumpData;
import android.rms.iaware.IAwaredConnection;
import android.rms.iaware.ICMSManager;
import android.rms.iaware.ICMSManager.Stub;
import android.rms.iaware.IReportDataCallback;
import android.rms.iaware.StatisticsData;
import android.util.Xml;
import com.android.internal.os.SomeArgs;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.feature.RFeature;
import com.huawei.android.app.HwActivityManager;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;

public class RFeatureManager implements IRDataRegister {
    private static final String CUST_PATH_DIR = "/data/cust/xml";
    private static final int FEATURE_MAX_DUMP_SIZE = 5;
    public static final int FEATURE_STATUS_DISABLE = 0;
    public static final int FEATURE_STATUS_ENABLE = 1;
    private static final String FEATURE_SWITCH_FILE_NAME = "iAwareFeatureSwitch.xml";
    private static int IAWARE_VERSION_DEFAULT = 1;
    private static String KEY_IAWARE_VERSION = "iAwareVersion";
    private static final int MSG_CONFIG_UPDATE = 3;
    private static final int MSG_ENABLE_DISABLE_FEATURE = 1;
    private static final int MSG_INIT_FEATURE = 0;
    private static final int MSG_REPORT_DATA = 2;
    private static final int MSG_REPORT_DATA_CALLBACK = 4;
    private static final int MSG_UPDATE_ARG_CONFIG = 0;
    private static final int MSG_UPDATE_ARG_CUST_CONFIG = 1;
    private static final String TAG = "RFeatureManager";
    private static final int UNKNOWN_FEATURE_STATUS = -1;
    private static final String USER_HABIT_RECV_TRAIN_COMPLETED_PERMISSION = "com.huawei.iaware.userhabit.USERHABIT_PERMISSION";
    private static final String USER_HABIT_TRAIN_COMPLETED_ACTION = "com.huawei.iaware.userhabit.TRAIN_COMPLETED";
    private static final String XML_TAG_FEATURE_ID = "featureid";
    private static final String XML_TAG_FEATURE_SWITCH = "switch";
    private static final String XML_TAG_ITEM = "item";
    private static final String mAPPMngFeatureName = "com.android.server.rms.iaware.feature.APPMngFeature";
    private static final FeatureType mAPPMngFeatureType = FeatureType.FEATURE_APPMNG;
    private Context mContext = null;
    private final String[] mFeatureNames = new String[]{"com.android.server.rms.iaware.feature.AppRecgFeature", "com.android.server.rms.iaware.cpu.CPUFeature", "com.android.server.rms.iaware.srms.ResourceFeature", "com.android.server.rms.iaware.feature.APPHiberFeature", "com.android.server.rms.iaware.feature.MemoryFeature", "com.android.server.rms.iaware.feature.IOFeature", "com.android.server.rms.iaware.srms.BroadcastFeature", "com.android.server.rms.iaware.feature.VsyncFeature", "com.android.server.rms.iaware.feature.MemoryFeature2", "com.android.server.rms.iaware.srms.AppStartupFeature", "com.android.server.rms.iaware.feature.AppFakeFeature", "com.android.server.rms.iaware.feature.NetworkFeature", "com.android.server.rms.iaware.feature.APPFreezeFeature", "com.android.server.rms.iaware.feature.APPIoLimitFeature", "com.android.server.rms.iaware.srms.AppCleanupFeature", "com.android.server.rms.iaware.feature.DevSchedFeatureRT"};
    private final FeatureType[] mFeatureTypes = new FeatureType[]{FeatureType.FEATURE_INTELLI_REC, FeatureType.FEATURE_CPU, FeatureType.FEATURE_RESOURCE, FeatureType.FEATURE_APPHIBER, FeatureType.FEATURE_MEMORY, FeatureType.FEATURE_IO, FeatureType.FEATURE_BROADCAST, FeatureType.FEATURE_VSYNC, FeatureType.FEATURE_MEMORY2, FeatureType.FEATURE_APPSTARTUP, FeatureType.FEATURE_RECG_FAKEACTIVITY, FeatureType.FEATURE_NETWORK_TCP, FeatureType.FEATURE_APPFREEZE, FeatureType.FEATURE_IO_LIMIT, FeatureType.FEATURE_APPCLEANUP, FeatureType.FEATURE_DEVSCHED};
    private boolean mIAwareEnabled = false;
    private int mIAwareVersion = IAWARE_VERSION_DEFAULT;
    private XmlPullParser mParser = null;
    private IAwareDeviceStateReceiver mReceiver = null;
    private Map<FeatureType, FeatureWrapper> mRegisteredFeatures = null;
    private ReportedDataHandler mReportedDataHandler;
    private Map<ResourceType, ArrayList<FeatureType>> mSubscribeDataMap = null;
    private final int mSubscribedFeatureTypeNum;
    private final FeatureType[] mSubscribedFeatureTypes;

    private static class FeatureWrapper {
        private boolean mFeatureEnabled;
        private RFeature mFeatureInstance;
        private int mFeatureVersion;

        public FeatureWrapper(RFeature instance, boolean status, int version) {
            this.mFeatureInstance = instance;
            this.mFeatureEnabled = status;
            this.mFeatureVersion = version;
        }

        public void setFeatureEnabled(boolean enable) {
            this.mFeatureEnabled = enable;
        }

        public boolean getFeatureEnabled() {
            return this.mFeatureEnabled;
        }

        public int getFeatureVersion() {
            return this.mFeatureVersion;
        }

        public RFeature getFeatureInstance() {
            return this.mFeatureInstance;
        }
    }

    private class IAwareDeviceStateReceiver extends BroadcastReceiver {
        /* synthetic */ IAwareDeviceStateReceiver(RFeatureManager this$0, IAwareDeviceStateReceiver -this1) {
            this();
        }

        private IAwareDeviceStateReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                AwareLog.e(RFeatureManager.TAG, "BroadcastReceiver error parameters!");
                return;
            }
            String action = intent.getAction();
            int resIDHabitStat;
            long curtime;
            String pkgName;
            Bundle bdl;
            if ("android.intent.action.SCREEN_ON".equals(action)) {
                RFeatureManager.this.reportData(new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_SCREEN_ON), System.currentTimeMillis(), action));
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                RFeatureManager.this.reportData(new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_SCREEN_OFF), System.currentTimeMillis(), action));
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                RFeatureManager.this.reportData(new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_BOOT_COMPLETED), System.currentTimeMillis(), action));
            } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                if (intent.getData() != null) {
                    resIDHabitStat = ResourceType.getReousrceId(ResourceType.RESOURCE_USERHABIT);
                    curtime = System.currentTimeMillis();
                    pkgName = intent.getData().getSchemeSpecificPart();
                    int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                    bdl = new Bundle();
                    bdl.putString(AwareUserHabit.USERHABIT_PACKAGE_NAME, pkgName);
                    bdl.putInt("uid", uid);
                    bdl.putInt(AwareUserHabit.USERHABIT_INSTALL_APP_UPDATE, 2);
                    RFeatureManager.this.reportData(new CollectData(resIDHabitStat, curtime, bdl));
                }
            } else if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                if (intent.getData() != null) {
                    resIDHabitStat = ResourceType.getReousrceId(ResourceType.RESOURCE_USERHABIT);
                    curtime = System.currentTimeMillis();
                    pkgName = intent.getData().getSchemeSpecificPart();
                    bdl = new Bundle();
                    bdl.putString(AwareUserHabit.USERHABIT_PACKAGE_NAME, pkgName);
                    bdl.putInt(AwareUserHabit.USERHABIT_INSTALL_APP_UPDATE, 1);
                    RFeatureManager.this.reportData(new CollectData(resIDHabitStat, curtime, bdl));
                }
            } else if (RFeatureManager.USER_HABIT_TRAIN_COMPLETED_ACTION.equals(action)) {
                resIDHabitStat = ResourceType.getReousrceId(ResourceType.RESOURCE_USERHABIT);
                curtime = System.currentTimeMillis();
                bdl = new Bundle();
                bdl.putInt(AwareUserHabit.USERHABIT_INSTALL_APP_UPDATE, 3);
                RFeatureManager.this.reportData(new CollectData(resIDHabitStat, curtime, bdl));
            } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                RFeatureManager.this.reportData(new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_USER_PRESENT), System.currentTimeMillis(), action));
            }
        }
    }

    private final class ReportedDataHandler extends Handler {
        public ReportedDataHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            ICMSManager awareservice;
            switch (msg.what) {
                case 0:
                    registerFeatures(msg.getData());
                    awareservice = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
                    if (awareservice == null) {
                        AwareLog.e(RFeatureManager.TAG, "get IAwareCMSService failed.");
                        break;
                    }
                    try {
                        RFeatureManager.this.mIAwareEnabled = awareservice.isIAwareEnabled();
                        if (RFeatureManager.this.mIAwareEnabled) {
                            registerAPPMngFeature(1);
                        } else {
                            registerAPPMngFeature(0);
                        }
                        if (RFeatureManager.this.mIAwareEnabled && RFeatureManager.this.mReceiver == null) {
                            RFeatureManager.this.mReceiver = new IAwareDeviceStateReceiver(RFeatureManager.this, null);
                            RFeatureManager.this.registerRDABroadcastReceiver();
                            AwareLog.d(RFeatureManager.TAG, "register RDA broadcast Receiver");
                            break;
                        }
                    } catch (RemoteException e) {
                        AwareLog.e(RFeatureManager.TAG, "call isIAwareEnabled failed." + e.getMessage());
                        break;
                    }
                case 1:
                    FeatureType featureType = FeatureType.getFeatureType(msg.arg2);
                    AwareLog.d(RFeatureManager.TAG, "handler message featureType:" + featureType.name() + "  status:" + msg.arg1);
                    if (RFeatureManager.this.mRegisteredFeatures.containsKey(featureType)) {
                        boolean status = msg.arg1 == 1;
                        FeatureWrapper feature = (FeatureWrapper) RFeatureManager.this.mRegisteredFeatures.get(featureType);
                        if (feature != null && feature.getFeatureInstance() != null) {
                            if (feature.getFeatureEnabled() != status) {
                                controlFeature(feature.getFeatureInstance(), status, feature.getFeatureVersion(), featureType, false);
                                feature.setFeatureEnabled(status);
                                awareservice = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
                                if (awareservice == null) {
                                    AwareLog.e(RFeatureManager.TAG, "get IAwareCMSService failed.");
                                    break;
                                }
                                try {
                                    RFeatureManager.this.mIAwareEnabled = awareservice.isIAwareEnabled();
                                    if (!RFeatureManager.this.mIAwareEnabled || RFeatureManager.this.mReceiver != null) {
                                        if (!(RFeatureManager.this.mIAwareEnabled || RFeatureManager.this.mReceiver == null)) {
                                            enableAPPMngFeature(false);
                                            RFeatureManager.this.mContext.unregisterReceiver(RFeatureManager.this.mReceiver);
                                            AwareLog.d(RFeatureManager.TAG, "unregister RDA broadcast Receiver");
                                            RFeatureManager.this.mReceiver = null;
                                            RFeatureManager.this.mReportedDataHandler.removeMessages(2);
                                            IAwaredConnection.getInstance().destroy();
                                            break;
                                        }
                                    }
                                    enableAPPMngFeature(true);
                                    RFeatureManager.this.mReceiver = new IAwareDeviceStateReceiver(RFeatureManager.this, null);
                                    RFeatureManager.this.registerRDABroadcastReceiver();
                                    AwareLog.d(RFeatureManager.TAG, "register RDA broadcast Receiver");
                                    break;
                                } catch (RemoteException e2) {
                                    AwareLog.e(RFeatureManager.TAG, "call isIAwareEnabled failed." + e2.getMessage());
                                    break;
                                }
                            }
                            return;
                        }
                        AwareLog.e(RFeatureManager.TAG, "handleMessage ENABLE_DISABLE_FEATURE feature null: " + featureType.name());
                        return;
                    }
                    break;
                case 2:
                    deliveryDataToFeatures(msg.obj, null);
                    break;
                case 3:
                    notifyUpdate(msg.arg1);
                    break;
                case 4:
                    SomeArgs args = msg.obj;
                    CollectData collectData = args.arg1;
                    IReportDataCallback callback = args.arg2;
                    args.recycle();
                    deliveryDataToFeatures(collectData, callback);
                    break;
            }
        }

        private void enableAPPMngFeature(boolean enabled) {
            AwareLog.d(RFeatureManager.TAG, "Enable APPMng Feature enable = " + enabled);
            FeatureWrapper feature = (FeatureWrapper) RFeatureManager.this.mRegisteredFeatures.get(RFeatureManager.mAPPMngFeatureType);
            if (feature == null || feature.getFeatureInstance() == null) {
                AwareLog.e(RFeatureManager.TAG, "enableAPPMngFeature feature null!");
            } else if (feature.getFeatureEnabled() != enabled) {
                if (enabled) {
                    feature.getFeatureInstance().enable();
                } else {
                    feature.getFeatureInstance().disable();
                }
                feature.setFeatureEnabled(enabled);
            }
        }

        private void registerFeatures(Bundle bundle) {
            int featureNum = RFeatureManager.this.mFeatureNames.length;
            if (featureNum == 0) {
                AwareLog.e(RFeatureManager.TAG, "There is no feature will be registered.");
                return;
            }
            RFeatureManager.this.mIAwareVersion = getIAwareVersion(bundle);
            for (int index = 0; index < featureNum; index++) {
                int[] featureInfo = RFeatureManager.this.getFeatureInfoFromBundle(bundle, RFeatureManager.this.mFeatureTypes[index].name());
                FeatureWrapper wrapper = createFeatureWrapper(RFeatureManager.this.mFeatureNames[index], RFeatureManager.this.mFeatureTypes[index], featureInfo[0], featureInfo[1]);
                if (wrapper != null) {
                    RFeatureManager.this.mRegisteredFeatures.put(RFeatureManager.this.mFeatureTypes[index], wrapper);
                }
            }
        }

        private int getIAwareVersion(Bundle bundle) {
            if (bundle == null) {
                return RFeatureManager.IAWARE_VERSION_DEFAULT;
            }
            return bundle.getInt(RFeatureManager.KEY_IAWARE_VERSION, RFeatureManager.IAWARE_VERSION_DEFAULT);
        }

        private void registerAPPMngFeature(int featureStatus) {
            FeatureWrapper wrapper = createFeatureWrapper(RFeatureManager.mAPPMngFeatureName, RFeatureManager.mAPPMngFeatureType, featureStatus, RFeatureManager.IAWARE_VERSION_DEFAULT);
            if (wrapper != null) {
                RFeatureManager.this.mRegisteredFeatures.put(RFeatureManager.mAPPMngFeatureType, wrapper);
            }
        }

        private void controlFeature(RFeature feature, boolean enable, int version, FeatureType featureType, boolean isInit) {
            AwareLog.d(RFeatureManager.TAG, "iAware2.0: feature id is " + featureType.ordinal());
            boolean useOld = version <= RFeatureManager.IAWARE_VERSION_DEFAULT;
            if (isInit && useOld) {
                AwareLog.d(RFeatureManager.TAG, "iAware2.0: controlFeature use default init!");
                iAware1InitFeature(feature, enable, featureType);
                return;
            }
            newControlFeature(feature, enable, version);
        }

        private void iAware1InitFeature(RFeature feature, boolean enable, FeatureType featureType) {
            if (enable) {
                feature.enable();
            } else if (featureType == FeatureType.FEATURE_RESOURCE) {
                feature.disable();
            }
        }

        private void newControlFeature(RFeature feature, boolean enable, int version) {
            AwareLog.d(RFeatureManager.TAG, "iAware2.0: newControlFeature feature version is " + version);
            if (!enable) {
                AwareLog.d(RFeatureManager.TAG, "iAware2.0: disable!");
                feature.disable();
            } else if (version > RFeatureManager.IAWARE_VERSION_DEFAULT) {
                int realVersion = Math.min(RFeatureManager.this.mIAwareVersion, version);
                AwareLog.d(RFeatureManager.TAG, "iAware2.0: use enable Ex! realVersion is " + realVersion);
                feature.enableFeatureEx(realVersion);
            } else {
                AwareLog.d(RFeatureManager.TAG, "iAware2.0: use old enable!");
                feature.enable();
            }
        }

        private FeatureWrapper createFeatureWrapper(String featureName, FeatureType featureType, int featureStatus, int featureVersion) {
            AwareLog.d(RFeatureManager.TAG, "createFeatureWrapper name = " + featureName + " type = " + featureType);
            try {
                Constructor<?>[] featureConstructor = Class.forName(featureName).getConstructors();
                try {
                    AwareLog.d(RFeatureManager.TAG, "createFeatureWrapper constructor = " + featureConstructor[0].getName());
                    RFeature feature = (RFeature) featureConstructor[0].newInstance(new Object[]{RFeatureManager.this.mContext, featureType, RFeatureManager.this});
                    AwareLog.d(RFeatureManager.TAG, "createFeatureWrapper featureStatus = " + featureStatus);
                    boolean status = featureStatus == 1;
                    controlFeature(feature, status, featureVersion, featureType, true);
                    return new FeatureWrapper(feature, status, featureVersion);
                } catch (IllegalArgumentException e) {
                    AwareLog.e(RFeatureManager.TAG, "createFeatureWrapper newInstance IllegalArgumentException");
                    return null;
                } catch (IllegalAccessException e2) {
                    AwareLog.e(RFeatureManager.TAG, "createFeatureWrapper newInstance IllegalAccessException");
                    return null;
                } catch (InstantiationException e3) {
                    AwareLog.e(RFeatureManager.TAG, "createFeatureWrapper newInstance InstantiationException");
                    return null;
                } catch (InvocationTargetException e4) {
                    AwareLog.e(RFeatureManager.TAG, "createFeatureWrapper newInstance InvocationTargetException");
                    return null;
                }
            } catch (ClassNotFoundException e5) {
                AwareLog.e(RFeatureManager.TAG, "createFeatureWrapper forName ClassNotFoundException");
                return null;
            }
        }

        /* JADX WARNING: Missing block: B:23:0x007c, code:
            r7 = 0;
     */
        /* JADX WARNING: Missing block: B:24:0x007d, code:
            if (r7 >= r11) goto L_0x00c2;
     */
        /* JADX WARNING: Missing block: B:25:0x007f, code:
            r4 = (com.android.server.rms.iaware.RFeatureManager.FeatureWrapper) com.android.server.rms.iaware.RFeatureManager.-get9(r16.this$0).get(com.android.server.rms.iaware.RFeatureManager.-get13(r16.this$0)[r7]);
     */
        /* JADX WARNING: Missing block: B:26:0x0097, code:
            if (r4 == null) goto L_0x00b0;
     */
        /* JADX WARNING: Missing block: B:28:0x009d, code:
            if (r4.getFeatureInstance() == null) goto L_0x00b0;
     */
        /* JADX WARNING: Missing block: B:30:0x00a3, code:
            if (r4.getFeatureEnabled() == false) goto L_0x00b0;
     */
        /* JADX WARNING: Missing block: B:31:0x00a5, code:
            if (r18 != null) goto L_0x00b6;
     */
        /* JADX WARNING: Missing block: B:32:0x00a7, code:
            r4.getFeatureInstance().reportData(r17);
     */
        /* JADX WARNING: Missing block: B:33:0x00b0, code:
            r7 = r7 + 1;
     */
        /* JADX WARNING: Missing block: B:37:0x00b6, code:
            r4.getFeatureInstance().reportDataWithCallback(r17, r18);
     */
        /* JADX WARNING: Missing block: B:38:0x00c2, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void deliveryDataToFeatures(CollectData data, IReportDataCallback callback) {
            Throwable th;
            ResourceType resType = ResourceType.getResourceType(data.getResId());
            int index = 0;
            synchronized (RFeatureManager.this.mSubscribeDataMap) {
                try {
                    List<FeatureType> currlist = (List) RFeatureManager.this.mSubscribeDataMap.get(resType);
                    if (currlist != null) {
                        int subcribedFeatureNum = currlist.size();
                        if (subcribedFeatureNum < RFeatureManager.this.mSubscribedFeatureTypeNum) {
                            Iterator feature$iterator = currlist.iterator();
                            while (true) {
                                int index2;
                                try {
                                    index2 = index;
                                    if (!feature$iterator.hasNext()) {
                                        break;
                                    }
                                    index = index2 + 1;
                                    RFeatureManager.this.mSubscribedFeatureTypes[index2] = (FeatureType) feature$iterator.next();
                                } catch (Throwable th2) {
                                    th = th2;
                                    index = index2;
                                    throw th;
                                }
                            }
                        }
                        AwareLog.e(RFeatureManager.TAG, "deliveryDataToFeatures subscribed too much features!");
                        return;
                    }
                    AwareLog.d(RFeatureManager.TAG, "deliveryDataToFeatures no subscribed features resType = " + resType.name());
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }

        private void notifyUpdate(int type) {
            if (type == 0) {
                notifyConfigUpdate();
            } else if (type == 1) {
                notifyCustConfigUpdate();
            } else {
                AwareLog.w(RFeatureManager.TAG, "notify type wrong!");
            }
        }

        private void notifyConfigUpdate() {
            AwareLog.d(RFeatureManager.TAG, "notifyConfigUpdate");
            for (FeatureWrapper feature : RFeatureManager.this.mRegisteredFeatures.values()) {
                if (feature == null || feature.getFeatureInstance() == null) {
                    AwareLog.e(RFeatureManager.TAG, "notifyConfigUpdate feature null!");
                } else if (!feature.getFeatureInstance().configUpdate()) {
                    AwareLog.e(RFeatureManager.TAG, "notifyConfigUpdate return false");
                }
            }
        }

        private void notifyCustConfigUpdate() {
            AwareLog.d(RFeatureManager.TAG, "notifyCustConfigUpdate");
            for (FeatureWrapper feature : RFeatureManager.this.mRegisteredFeatures.values()) {
                if (feature == null || feature.getFeatureInstance() == null) {
                    AwareLog.e(RFeatureManager.TAG, "notifyCustConfigUpdate feature null!");
                } else {
                    AwareLog.d(RFeatureManager.TAG, feature.getFeatureInstance() + " notifyCustConfigUpdate return " + feature.getFeatureInstance().custConfigUpdate());
                }
            }
        }
    }

    public boolean subscribeData(ResourceType resType, FeatureType featureType) {
        if (resType == null || featureType == null) {
            AwareLog.e(TAG, "subscribeData: error parameters!");
            return false;
        }
        AwareLog.d(TAG, "subscribeData resType = " + resType.name() + " featureType = " + featureType.name());
        synchronized (this.mSubscribeDataMap) {
            ArrayList<FeatureType> currlist = (ArrayList) this.mSubscribeDataMap.get(resType);
            if (currlist == null) {
                currlist = new ArrayList();
                this.mSubscribeDataMap.put(resType, currlist);
            }
            if (!currlist.contains(featureType)) {
                currlist.add(featureType);
            }
        }
        return true;
    }

    public boolean unSubscribeData(ResourceType resType, FeatureType featureType) {
        if (resType == null || featureType == null) {
            AwareLog.e(TAG, "unSubscribeData: error parameters!");
            return false;
        }
        AwareLog.d(TAG, "unSubscribeData resType = " + resType.name() + " featureType = " + featureType.name());
        synchronized (this.mSubscribeDataMap) {
            List<FeatureType> currlist = (List) this.mSubscribeDataMap.get(resType);
            if (currlist != null) {
                currlist.remove(featureType);
                if (currlist.size() == 0) {
                    this.mSubscribeDataMap.remove(resType);
                }
            }
        }
        return true;
    }

    public FeatureType[] getFeatureTypes() {
        return (FeatureType[]) this.mFeatureTypes.clone();
    }

    public int getFeatureStatus(int featureid) {
        FeatureType type = FeatureType.getFeatureType(featureid);
        if (type == FeatureType.FEATURE_INVALIDE_TYPE) {
            AwareLog.e(TAG, "getFeatureStatus invalid feature type");
            return 0;
        }
        FeatureWrapper wrapper = (FeatureWrapper) this.mRegisteredFeatures.get(type);
        if (wrapper == null) {
            AwareLog.e(TAG, "getFeatureStatus feature wrapper null, featureid = " + featureid);
            return 0;
        } else if (wrapper.getFeatureEnabled()) {
            return 1;
        } else {
            return 0;
        }
    }

    public void enableFeature(int type) {
        AwareLog.d(TAG, "enableFeature type = " + type);
        Message enableMessage = Message.obtain();
        enableMessage.what = 1;
        enableMessage.arg1 = 1;
        enableMessage.arg2 = type;
        this.mReportedDataHandler.sendMessage(enableMessage);
    }

    public void disableFeature(int type) {
        AwareLog.d(TAG, "disableFeature type = " + type);
        Message disableMessage = Message.obtain();
        disableMessage.what = 1;
        disableMessage.arg1 = 0;
        disableMessage.arg2 = type;
        this.mReportedDataHandler.sendMessage(disableMessage);
    }

    public void reportData(CollectData data) {
        Message dataMessage = Message.obtain();
        dataMessage.what = 2;
        dataMessage.obj = data;
        this.mReportedDataHandler.sendMessage(dataMessage);
    }

    public void reportDataWithCallback(CollectData data, IReportDataCallback callback) {
        AwareLog.d(TAG, "reportDataWithCallback");
        Message dataMessage = Message.obtain();
        dataMessage.what = 4;
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = data;
        args.arg2 = callback;
        dataMessage.obj = args;
        this.mReportedDataHandler.sendMessage(dataMessage);
    }

    public void init(Bundle bundle) {
        AwareLog.d(TAG, "init bundle = " + bundle);
        Message initMessage = Message.obtain();
        initMessage.what = 0;
        initMessage.setData(bundle);
        this.mReportedDataHandler.sendMessage(initMessage);
    }

    public boolean isResourceNeeded(ResourceType resourceType) {
        return this.mIAwareEnabled;
    }

    public int isFeatureEnabled(int featureId) {
        if (FeatureType.getFeatureType(featureId) != FeatureType.FEATURE_INVALIDE_TYPE) {
            return parseFeatureSwitchFormCustFile(featureId);
        }
        AwareLog.e(TAG, "Enabling error feature id!");
        return -1;
    }

    public boolean configUpdate() {
        AwareLog.d(TAG, "configUpdate ");
        Message msg = Message.obtain();
        msg.what = 3;
        msg.arg1 = 0;
        return this.mReportedDataHandler.sendMessage(msg);
    }

    public boolean custConfigUpdate() {
        AwareLog.d(TAG, "custConfigUpdate ");
        Message msg = Message.obtain();
        msg.what = 3;
        msg.arg1 = 1;
        return this.mReportedDataHandler.sendMessage(msg);
    }

    public int getDumpData(int time, List<DumpData> dumpData) {
        AwareLog.d(TAG, "getDumpData");
        for (FeatureWrapper v : this.mRegisteredFeatures.values()) {
            if (v == null || v.getFeatureInstance() == null) {
                AwareLog.e(TAG, "getDumpData feature null!");
            } else {
                ArrayList<DumpData> featureDumpData = v.getFeatureInstance().getDumpData(time);
                if (featureDumpData != null) {
                    int dumpDataSize = featureDumpData.size();
                    if (dumpDataSize > 5) {
                        AwareLog.e(TAG, "RDA getDumpData more than 5 items, size = " + dumpDataSize + " , id = " + ((DumpData) featureDumpData.get(0)).getFeatureId());
                        dumpDataSize = 5;
                    }
                    for (int index = 0; index < dumpDataSize; index++) {
                        dumpData.add((DumpData) featureDumpData.get(index));
                    }
                }
            }
        }
        return dumpData.size();
    }

    public int getStatisticsData(List<StatisticsData> statisticsData) {
        AwareLog.d(TAG, "getStatisticsData");
        for (FeatureWrapper v : this.mRegisteredFeatures.values()) {
            if (v == null || v.getFeatureInstance() == null) {
                AwareLog.e(TAG, "getStatisticsData feature null!");
            } else {
                ArrayList<StatisticsData> featureStatisticsData = v.getFeatureInstance().getStatisticsData();
                if (featureStatisticsData != null) {
                    statisticsData.addAll(featureStatisticsData);
                }
            }
        }
        return statisticsData.size();
    }

    public String saveBigData(int featureId, boolean clear) {
        AwareLog.d(TAG, "rt saveBigData");
        FeatureWrapper fw = (FeatureWrapper) this.mRegisteredFeatures.get(FeatureType.getFeatureType(featureId));
        if (fw == null) {
            AwareLog.d(TAG, "null FeatureWrapper");
            return null;
        }
        RFeature v = fw.getFeatureInstance();
        if (v != null) {
            return v.saveBigData(clear);
        }
        AwareLog.d(TAG, "null RFeature");
        return null;
    }

    public String fetchBigDataByVersion(int iawareVer, int featureId, boolean beta, boolean clear) {
        AwareLog.d(TAG, "fetchBigDataByVersion iVer = " + iawareVer + ", fId = " + featureId + ", beta = " + beta);
        FeatureWrapper featureWrapper = (FeatureWrapper) this.mRegisteredFeatures.get(FeatureType.getFeatureType(featureId));
        RFeature feature = featureWrapper != null ? featureWrapper.getFeatureInstance() : null;
        if (feature != null) {
            return feature.getBigDataByVersion(iawareVer, beta, clear);
        }
        return null;
    }

    public RFeatureManager(Context context, HandlerThread handlerThread) {
        AwareLog.d(TAG, "RFeatureManager created");
        this.mContext = context;
        this.mRegisteredFeatures = new HashMap();
        this.mSubscribeDataMap = new HashMap();
        this.mReportedDataHandler = new ReportedDataHandler(handlerThread.getLooper());
        this.mSubscribedFeatureTypeNum = this.mFeatureTypes.length + 1;
        this.mSubscribedFeatureTypes = new FeatureType[this.mSubscribedFeatureTypeNum];
        this.mParser = Xml.newPullParser();
        HwActivityManager.registerDAMonitorCallback(new AwareAmsMonitorCallback());
    }

    public RFeature getRegisteredFeature(FeatureType type) {
        if (!this.mRegisteredFeatures.containsKey(type)) {
            return null;
        }
        FeatureWrapper feature = (FeatureWrapper) this.mRegisteredFeatures.get(type);
        if (feature != null) {
            return feature.getFeatureInstance();
        }
        AwareLog.e(TAG, "getRegisteredFeature feature null!");
        return null;
    }

    private void registerRDABroadcastReceiver() {
        IntentFilter deviceStates = new IntentFilter();
        deviceStates.addAction("android.intent.action.SCREEN_ON");
        deviceStates.addAction("android.intent.action.SCREEN_OFF");
        deviceStates.setPriority(1000);
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, deviceStates, null, null);
        IntentFilter bootStates = new IntentFilter();
        bootStates.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, bootStates, null, null);
        IntentFilter appStates = new IntentFilter();
        appStates.addAction("android.intent.action.PACKAGE_ADDED");
        appStates.addAction("android.intent.action.PACKAGE_REMOVED");
        appStates.addDataScheme("package");
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, appStates, null, null);
        IntentFilter completedTrain = new IntentFilter();
        completedTrain.addAction(USER_HABIT_TRAIN_COMPLETED_ACTION);
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, completedTrain, USER_HABIT_RECV_TRAIN_COMPLETED_PERMISSION, null);
        IntentFilter presentFilter = new IntentFilter();
        presentFilter.addAction("android.intent.action.USER_PRESENT");
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, presentFilter, null, null);
    }

    public int[] getFeatureInfoFromBundle(Bundle bundle, String key) {
        int[] info = bundle.getIntArray(key);
        if (info != null && info.length == 2) {
            return info;
        }
        return new int[]{0, IAWARE_VERSION_DEFAULT};
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x0087 A:{SYNTHETIC, Splitter: B:38:0x0087} */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x010b A:{SYNTHETIC, Splitter: B:72:0x010b} */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x012a A:{SYNTHETIC, Splitter: B:82:0x012a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int parseFeatureSwitchFormCustFile(int featureId) {
        IOException ioe;
        Throwable th;
        Exception e;
        InputStream is = null;
        XmlPullParser parser = null;
        try {
            File custConfigFile = loadCustFeatureSwitchFile();
            if (custConfigFile.exists()) {
                InputStream is2 = new FileInputStream(custConfigFile);
                try {
                    parser = this.mParser;
                    parser.setInput(is2, StandardCharsets.UTF_8.name());
                    is = is2;
                } catch (IOException e2) {
                    ioe = e2;
                    is = is2;
                    try {
                        AwareLog.e(TAG, "read xml failed, error:" + ioe.getMessage());
                        if (is != null) {
                        }
                        return -1;
                    } catch (Throwable th2) {
                        th = th2;
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e3) {
                                AwareLog.e(TAG, "close file input stream fail!!");
                            }
                        }
                        throw th;
                    }
                } catch (Exception e4) {
                    e = e4;
                    is = is2;
                    AwareLog.e(TAG, "read xml failed, error:" + e.getMessage());
                    if (is != null) {
                    }
                    return -1;
                } catch (Throwable th3) {
                    th = th3;
                    is = is2;
                    if (is != null) {
                    }
                    throw th;
                }
            }
            if (parser == null) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e5) {
                        AwareLog.e(TAG, "close file input stream fail!!");
                    }
                }
                return -1;
            }
            boolean enterItemTag = false;
            Map configItem = null;
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                String tagName = parser.getName();
                if (eventType == 2) {
                    if (XML_TAG_ITEM.equals(tagName)) {
                        enterItemTag = true;
                        configItem = new HashMap();
                    } else if (tagName != null && enterItemTag) {
                        String value = parser.nextText();
                        if (configItem != null) {
                            configItem.put(tagName, value);
                        }
                    }
                } else if (eventType != 3) {
                    continue;
                } else if (XML_TAG_ITEM.equals(tagName)) {
                    AwareLog.d(TAG, "exit item");
                    enterItemTag = false;
                    if (configItem != null && ((String) configItem.get(XML_TAG_FEATURE_ID)).equals(Integer.toString(featureId))) {
                        int parseInt = Integer.parseInt((String) configItem.get(XML_TAG_FEATURE_SWITCH));
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e6) {
                                AwareLog.e(TAG, "close file input stream fail!!");
                            }
                        }
                        return parseInt;
                    }
                } else {
                    continue;
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e7) {
                    AwareLog.e(TAG, "close file input stream fail!!");
                }
            }
            return -1;
        } catch (IOException e8) {
            ioe = e8;
            AwareLog.e(TAG, "read xml failed, error:" + ioe.getMessage());
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e9) {
                    AwareLog.e(TAG, "close file input stream fail!!");
                }
            }
            return -1;
        } catch (Exception e10) {
            e = e10;
            AwareLog.e(TAG, "read xml failed, error:" + e.getMessage());
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e11) {
                    AwareLog.e(TAG, "close file input stream fail!!");
                }
            }
            return -1;
        }
    }

    File loadCustFeatureSwitchFile() {
        try {
            File cfg = HwCfgFilePolicy.getCfgFile("xml/iAwareFeatureSwitch.xml", 0);
            if (cfg != null) {
                AwareLog.d(TAG, "cust switch file path is " + cfg.getAbsolutePath());
                return cfg;
            }
        } catch (NoClassDefFoundError e) {
            AwareLog.e(TAG, "loadCustFeatureSwitchFile NoClassDefFoundError : HwCfgFilePolicy ");
        }
        return new File(CUST_PATH_DIR + File.separator + FEATURE_SWITCH_FILE_NAME);
    }
}

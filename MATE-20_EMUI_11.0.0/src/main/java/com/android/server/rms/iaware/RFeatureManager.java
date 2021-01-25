package com.android.server.rms.iaware;

import android.appwidget.HwAppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hwrme.HwResMngEngine;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.DumpData;
import android.rms.iaware.IAwareCMSManager;
import android.rms.iaware.IAwaredConnection;
import android.rms.iaware.IReportDataCallback;
import android.rms.iaware.StatisticsData;
import android.util.Xml;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.feature.RFeature;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.content.ContextEx;
import com.huawei.android.internal.os.SomeArgsEx;
import com.huawei.android.os.HwPowerManager;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.ZygoteInitEx;
import com.huawei.android.view.HwWindowManager;
import com.huawei.dalvik.system.VMRuntimeExt;
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
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class RFeatureManager implements IRDataRegister {
    private static final String ACTION_TOPM_LEVEL = "com.huawei.android.ActivityTopAlgorithm.TOPM_LEVEL";
    private static final String APP_MNG_FEATURE_NAME = "com.android.server.rms.iaware.feature.AppMngFeature";
    private static final String CUST_PATH_DIR = "/data/cust/xml";
    private static final String DATA_SCHEME = "package";
    private static final int FEATURE_MAX_DUMP_SIZE = 5;
    public static final int FEATURE_STATUS_DISABLE = 0;
    public static final int FEATURE_STATUS_ENABLE = 1;
    private static final String FEATURE_SWITCH_FILE_NAME = "iAwareFeatureSwitch.xml";
    private static final int GC_MIN_INTERVAL = 300000;
    private static final int IAWARE_VERSION_DEFAULT = 1;
    private static final String KEY_IAWARE_VERSION = "iAwareVersion";
    private static final int MSG_CONFIG_UPDATE = 3;
    private static final int MSG_ENABLE_DISABLE_FEATURE = 1;
    private static final int MSG_INIT_FEATURE = 0;
    private static final int MSG_REPORT_DATA = 2;
    private static final int MSG_REPORT_DATA_CALLBACK = 4;
    private static final int MSG_TRIGGER_GC = 5;
    private static final int MSG_UPDATE_ARG_CONFIG = 0;
    private static final int MSG_UPDATE_ARG_CUST_CONFIG = 1;
    private static final String TAG = "RFeatureManager";
    private static final String TOP_ACTIVITY_RECV_PERMISSION = "com.huawei.android.ActivityTopAlgorithm.TOPM_PERMISSION";
    private static final int TRIGGER_MAX_DELAY_MILLIS = 5000;
    private static final int UNKNOWN_FEATURE_STATUS = -1;
    private static final String USER_HABIT_RECV_TRAIN_COMPLETED_PERMISSION = "com.huawei.iaware.userhabit.USERHABIT_PERMISSION";
    private static final String USER_HABIT_TRAIN_COMPLETED_ACTION = "com.huawei.iaware.userhabit.TRAIN_COMPLETED";
    private static final String XML_TAG_FEATURE_ID = "featureid";
    private static final String XML_TAG_FEATURE_SWITCH = "switch";
    private static final String XML_TAG_ITEM = "item";
    private boolean mAwareEnabled = false;
    private int mAwareVersion = 1;
    private AwareAwsiMonitorCallback mAwsiCallback = null;
    private Context mContext = null;
    private final String[] mFeatureNames = {"com.android.server.rms.iaware.feature.AppRecgFeature", "com.android.server.rms.iaware.cpu.CpuFeature", "com.android.server.rms.iaware.srms.ResourceFeature", "com.android.server.rms.iaware.feature.AppHiberFeature", "com.android.server.rms.iaware.feature.MemoryFeature", "com.android.server.rms.iaware.feature.IoFeature", "com.android.server.rms.iaware.srms.BroadcastFeature", "com.android.server.rms.iaware.feature.VsyncFeature", "com.android.server.rms.iaware.feature.MemoryFeatureEx", "com.android.server.rms.iaware.srms.AppStartupFeature", "com.android.server.rms.iaware.feature.AppFakeFeature", "com.android.server.rms.iaware.feature.NetworkFeature", "com.android.server.rms.iaware.feature.AppFreezeFeature", "com.android.server.rms.iaware.feature.AppIoLimitFeature", "com.android.server.rms.iaware.srms.AppCleanupFeature", "com.android.server.rms.iaware.feature.DevSchedFeatureRt", "com.android.server.rms.iaware.feature.AlarmManagerFeature", "com.android.server.rms.iaware.feature.BlitParallelFeature", "com.android.server.rms.iaware.srms.BroadcastExFeature", "com.android.server.rms.iaware.feature.SysLoadFeature", "com.android.server.rms.iaware.feature.AppQuickStartFeature", "com.android.server.rms.iaware.feature.StartWindowFeature", "com.android.server.rms.iaware.feature.PreloadResourceFeature", "com.android.server.rms.iaware.feature.AppCpuLimitFeature", "com.android.server.rms.iaware.feature.AwareQosFeature", "com.android.server.rms.iaware.feature.SwitchCleanFeature", "com.android.server.rms.iaware.feature.AppSceneMngFeature", "com.android.server.rms.iaware.feature.ProfileFeature", "com.android.server.rms.iaware.feature.SceneRecogFeature", "com.android.server.rms.iaware.feature.AppAccurateRecgFeature", "com.android.server.rms.iaware.feature.NetQosSchedFeature", "com.android.server.rms.iaware.feature.StartWindowCacheFeature", "com.android.server.rms.iaware.feature.ComponentPreloadFeature", "com.android.server.rms.iaware.feature.DisplaySmoothFeature"};
    private final AwareConstant.FeatureType[] mFeatureTypes = {AwareConstant.FeatureType.FEATURE_INTELLI_REC, AwareConstant.FeatureType.FEATURE_CPU, AwareConstant.FeatureType.FEATURE_RESOURCE, AwareConstant.FeatureType.FEATURE_APPHIBER, AwareConstant.FeatureType.FEATURE_MEMORY, AwareConstant.FeatureType.FEATURE_IO, AwareConstant.FeatureType.FEATURE_BROADCAST, AwareConstant.FeatureType.FEATURE_VSYNC, AwareConstant.FeatureType.FEATURE_MEMORY2, AwareConstant.FeatureType.FEATURE_APPSTARTUP, AwareConstant.FeatureType.FEATURE_RECG_FAKEACTIVITY, AwareConstant.FeatureType.FEATURE_NETWORK_TCP, AwareConstant.FeatureType.FEATURE_APPFREEZE, AwareConstant.FeatureType.FEATURE_IO_LIMIT, AwareConstant.FeatureType.FEATURE_APPCLEANUP, AwareConstant.FeatureType.FEATURE_DEVSCHED, AwareConstant.FeatureType.FEATURE_ALARM_MANAGER, AwareConstant.FeatureType.FEATURE_BLIT_PARALLEL, AwareConstant.FeatureType.FEATURE_BROADCASTEX, AwareConstant.FeatureType.FEATURE_SYSLOAD, AwareConstant.FeatureType.FEATURE_APP_QUICKSTART, AwareConstant.FeatureType.FEATURE_STARTWINDOW, AwareConstant.FeatureType.PRELOADRESOURCE, AwareConstant.FeatureType.FEATURE_CPU_LIMIT, AwareConstant.FeatureType.FEATURE_QOS, AwareConstant.FeatureType.FEATURE_SWITCH_CLEAN, AwareConstant.FeatureType.FEATURE_APPSCENEMNG, AwareConstant.FeatureType.FEATURE_PROFILE, AwareConstant.FeatureType.FEATURE_SCENE_RECOG, AwareConstant.FeatureType.FEATURE_APPACCURATE_RECG, AwareConstant.FeatureType.FEATURE_NETQOS, AwareConstant.FeatureType.FEATURE_START_WINDOW_CACHE, AwareConstant.FeatureType.FEATURE_COMPONENT_PRELOAD, AwareConstant.FeatureType.FEATURE_DISPLAY_SMOOTH};
    private long mLastGcTime = 0;
    private AwareDeviceStateReceiver mReceiver = null;
    private boolean mRegisterAwmSuccess = false;
    private boolean mRegisterWmSuccess = false;
    private Map<AwareConstant.FeatureType, FeatureWrapper> mRegisteredFeatures = null;
    private ReportedDataHandler mReportedDataHandler;
    private final Map<AwareConstant.ResourceType, ArrayList<AwareConstant.FeatureType>> mSubscribeDataMap;
    private final int mSubscribedFeatureTypeNum;
    private final AwareConstant.FeatureType[] mSubscribedFeatureTypes;
    private int mWmRetryTime = 50;
    private AwareWmsMonitorCallback mWmsCallback = null;

    static /* synthetic */ int access$510(RFeatureManager x0) {
        int i = x0.mWmRetryTime;
        x0.mWmRetryTime = i - 1;
        return i;
    }

    public RFeatureManager(Context context, HandlerThread handlerThread) {
        AwareLog.d(TAG, "RFeatureManager created");
        this.mContext = context;
        this.mRegisteredFeatures = new HashMap();
        this.mSubscribeDataMap = new HashMap();
        this.mReportedDataHandler = new ReportedDataHandler(handlerThread.getLooper());
        this.mSubscribedFeatureTypeNum = this.mFeatureTypes.length + 1;
        this.mSubscribedFeatureTypes = new AwareConstant.FeatureType[this.mSubscribedFeatureTypeNum];
        HwActivityManager.registerDAMonitorCallback(new AwareAmsMonitorCallback());
        HwActivityTaskManager.registerAtmDAMonitorCallback(new AwareAtmsMonitorCallback());
        this.mWmsCallback = new AwareWmsMonitorCallback();
        this.mAwsiCallback = new AwareAwsiMonitorCallback();
        this.mReportedDataHandler.post(new CallbackRegistration());
        HwPowerManager.registerPowerMonitorCallback(new AwarePowerMonitorCallback());
    }

    @Override // com.android.server.rms.iaware.IRDataRegister
    public boolean subscribeData(AwareConstant.ResourceType resType, AwareConstant.FeatureType featureType) {
        if (resType == null || featureType == null) {
            AwareLog.e(TAG, "subscribeData: error parameters!");
            return false;
        }
        AwareLog.d(TAG, "subscribeData resType = " + resType.name() + " featureType = " + featureType.name());
        synchronized (this.mSubscribeDataMap) {
            ArrayList<AwareConstant.FeatureType> currlist = this.mSubscribeDataMap.get(resType);
            if (currlist == null) {
                currlist = new ArrayList<>();
                this.mSubscribeDataMap.put(resType, currlist);
            }
            if (!currlist.contains(featureType)) {
                currlist.add(featureType);
            }
        }
        return true;
    }

    @Override // com.android.server.rms.iaware.IRDataRegister
    public boolean unSubscribeData(AwareConstant.ResourceType resType, AwareConstant.FeatureType featureType) {
        if (resType == null || featureType == null) {
            AwareLog.e(TAG, "unSubscribeData: error parameters!");
            return false;
        }
        AwareLog.d(TAG, "unSubscribeData resType = " + resType.name() + " featureType = " + featureType.name());
        synchronized (this.mSubscribeDataMap) {
            List<AwareConstant.FeatureType> currlist = this.mSubscribeDataMap.get(resType);
            if (currlist != null) {
                currlist.remove(featureType);
                if (currlist.size() == 0) {
                    this.mSubscribeDataMap.remove(resType);
                }
            }
        }
        return true;
    }

    public AwareConstant.FeatureType[] getFeatureTypes() {
        return (AwareConstant.FeatureType[]) this.mFeatureTypes.clone();
    }

    public int getFeatureStatus(int featureId) {
        AwareConstant.FeatureType type = AwareConstant.FeatureType.getFeatureType(featureId);
        if (type == AwareConstant.FeatureType.FEATURE_INVALIDE_TYPE) {
            AwareLog.e(TAG, "getFeatureStatus invalid feature type");
            return 0;
        }
        FeatureWrapper wrapper = this.mRegisteredFeatures.get(type);
        if (wrapper == null) {
            AwareLog.e(TAG, "getFeatureStatus feature wrapper null, featureid = " + featureId);
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
        SomeArgsEx args = SomeArgsEx.obtain();
        args.setArg1(data);
        args.setArg2(callback);
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

    public boolean isResourceNeeded(AwareConstant.ResourceType resourceType) {
        return this.mAwareEnabled;
    }

    public int isFeatureEnabled(int featureId) {
        if (AwareConstant.FeatureType.getFeatureType(featureId) != AwareConstant.FeatureType.FEATURE_INVALIDE_TYPE) {
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
        for (FeatureWrapper wrapper : this.mRegisteredFeatures.values()) {
            if (wrapper == null || wrapper.getFeatureInstance() == null) {
                AwareLog.e(TAG, "getDumpData feature null!");
            } else {
                ArrayList<DumpData> featureDumpData = wrapper.getFeatureInstance().getDumpData(time);
                if (featureDumpData != null) {
                    int dumpDataSize = featureDumpData.size();
                    if (dumpDataSize > 5) {
                        AwareLog.e(TAG, "RDA getDumpData more than 5 items, size = " + dumpDataSize + " , id = " + featureDumpData.get(0).getFeatureId());
                        dumpDataSize = 5;
                    }
                    for (int index = 0; index < dumpDataSize; index++) {
                        dumpData.add(featureDumpData.get(index));
                    }
                }
            }
        }
        return dumpData.size();
    }

    public int getStatisticsData(List<StatisticsData> statisticsData) {
        AwareLog.d(TAG, "getStatisticsData");
        for (FeatureWrapper wrapper : this.mRegisteredFeatures.values()) {
            if (wrapper == null || wrapper.getFeatureInstance() == null) {
                AwareLog.e(TAG, "getStatisticsData feature null!");
            } else {
                ArrayList<StatisticsData> featureStatisticsData = wrapper.getFeatureInstance().getStatisticsData();
                if (featureStatisticsData != null) {
                    statisticsData.addAll(featureStatisticsData);
                }
            }
        }
        return statisticsData.size();
    }

    public String saveBigData(int featureId, boolean clear) {
        AwareLog.d(TAG, "rt saveBigData");
        FeatureWrapper fw = this.mRegisteredFeatures.get(AwareConstant.FeatureType.getFeatureType(featureId));
        if (fw == null) {
            AwareLog.d(TAG, "null FeatureWrapper");
            return null;
        }
        RFeature feature = fw.getFeatureInstance();
        if (feature != null) {
            return feature.saveBigData(clear);
        }
        AwareLog.d(TAG, "null RFeature");
        return null;
    }

    public String fetchBigDataByVersion(int iawareVersion, int featureId, boolean beta, boolean clear) {
        AwareLog.d(TAG, "fetchBigDataByVersion iVer = " + iawareVersion + ", fId = " + featureId + ", beta = " + beta);
        FeatureWrapper featureWrapper = this.mRegisteredFeatures.get(AwareConstant.FeatureType.getFeatureType(featureId));
        RFeature feature = featureWrapper != null ? featureWrapper.getFeatureInstance() : null;
        if (feature != null) {
            return feature.getBigDataByVersion(iawareVersion, beta, clear);
        }
        return null;
    }

    public String fetchDftDataByVersion(int iawareVer, int featureId, boolean beta, boolean clear, boolean betaEncode) {
        AwareLog.d(TAG, "fetchDftDataByVersion iVer = " + iawareVer + ", fId = " + featureId + ", beta = " + beta + ", betaEncode=" + betaEncode);
        FeatureWrapper featureWrapper = this.mRegisteredFeatures.get(AwareConstant.FeatureType.getFeatureType(featureId));
        RFeature feature = featureWrapper != null ? featureWrapper.getFeatureInstance() : null;
        if (feature != null) {
            return feature.getDftDataByVersion(iawareVer, beta, clear, betaEncode);
        }
        return null;
    }

    private class CallbackRegistration implements Runnable {
        private CallbackRegistration() {
        }

        @Override // java.lang.Runnable
        public void run() {
            RFeatureManager rFeatureManager = RFeatureManager.this;
            boolean z = false;
            rFeatureManager.mRegisterWmSuccess = rFeatureManager.mRegisterWmSuccess || HwWindowManager.registerWMMonitorCallback(RFeatureManager.this.mWmsCallback);
            RFeatureManager rFeatureManager2 = RFeatureManager.this;
            if (rFeatureManager2.mRegisterAwmSuccess || HwAppWidgetManager.registerAWSIMonitorCallback(RFeatureManager.this.mAwsiCallback)) {
                z = true;
            }
            rFeatureManager2.mRegisterAwmSuccess = z;
            if ((!RFeatureManager.this.mRegisterAwmSuccess || !RFeatureManager.this.mRegisterWmSuccess) && RFeatureManager.access$510(RFeatureManager.this) > 0) {
                RFeatureManager.this.mReportedDataHandler.postDelayed(this, 200);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class FeatureWrapper {
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

    /* access modifiers changed from: private */
    public final class ReportedDataHandler extends Handler {
        public ReportedDataHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                initFeature(msg);
            } else if (i == 1) {
                handleFeatureSwitch(msg);
            } else if (i == 2) {
                deliveryDataToFeatures((CollectData) msg.obj, null);
            } else if (i == 3) {
                notifyUpdate(msg.arg1);
            } else if (i != 4) {
                if (i == 5) {
                    long tmp = SystemClock.elapsedRealtime();
                    long diff = tmp - RFeatureManager.this.mLastGcTime;
                    if (diff <= 0 || diff >= 300000) {
                        AwareLog.d(RFeatureManager.TAG, "trigger gc when screen off for maple. ");
                        Runtime.getRuntime().gc();
                        VMRuntimeExt.notifySaveCyclePattern();
                        RFeatureManager.this.mLastGcTime = tmp;
                        return;
                    }
                    AwareLog.d(RFeatureManager.TAG, "trigger gc too frequently. ");
                }
            } else if (msg.obj instanceof SomeArgsEx) {
                SomeArgsEx args = (SomeArgsEx) msg.obj;
                CollectData collectData = null;
                IReportDataCallback callback = null;
                if (args.arg1() instanceof CollectData) {
                    collectData = (CollectData) args.arg1();
                }
                if (args.arg2() instanceof IReportDataCallback) {
                    callback = (IReportDataCallback) args.arg2();
                }
                args.recycle();
                if (collectData != null) {
                    deliveryDataToFeatures(collectData, callback);
                }
            }
        }

        private void initFeature(Message msg) {
            registerFeatures(msg.getData());
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                try {
                    RFeatureManager.this.mAwareEnabled = IAwareCMSManager.isIAwareEnabled(awareService);
                    if (RFeatureManager.this.mAwareEnabled) {
                        registerAppMngFeature(1);
                    } else {
                        registerAppMngFeature(0);
                    }
                    if (RFeatureManager.this.mAwareEnabled && RFeatureManager.this.mReceiver == null) {
                        RFeatureManager.this.mReceiver = new AwareDeviceStateReceiver();
                        RFeatureManager.this.registerRdaBroadcastReceiver();
                        AwareLog.d(RFeatureManager.TAG, "register RDA broadcast Receiver");
                    }
                } catch (RemoteException e) {
                    AwareLog.e(RFeatureManager.TAG, "call isIAwareEnabled failed." + e.getMessage());
                }
            } else {
                AwareLog.e(RFeatureManager.TAG, "get IAwareCMSService failed.");
            }
        }

        private void handleFeatureSwitch(Message msg) {
            AwareConstant.FeatureType featureType = AwareConstant.FeatureType.getFeatureType(msg.arg2);
            AwareLog.d(RFeatureManager.TAG, "handler message featureType:" + featureType.name() + " status:" + msg.arg1);
            if (RFeatureManager.this.mRegisteredFeatures.containsKey(featureType)) {
                boolean status = msg.arg1 == 1;
                FeatureWrapper feature = (FeatureWrapper) RFeatureManager.this.mRegisteredFeatures.get(featureType);
                if (feature == null || feature.getFeatureInstance() == null) {
                    AwareLog.e(RFeatureManager.TAG, "handleMessage ENABLE_DISABLE_FEATURE feature null: " + featureType.name());
                } else if (feature.getFeatureEnabled() != status) {
                    controlFeature(feature.getFeatureInstance(), status, feature.getFeatureVersion(), featureType, false);
                    feature.setFeatureEnabled(status);
                    IBinder awareService = IAwareCMSManager.getICMSManager();
                    if (awareService != null) {
                        try {
                            RFeatureManager.this.mAwareEnabled = IAwareCMSManager.isIAwareEnabled(awareService);
                            if (RFeatureManager.this.mAwareEnabled && RFeatureManager.this.mReceiver == null) {
                                enableAppMngFeature(true);
                                RFeatureManager.this.mReceiver = new AwareDeviceStateReceiver();
                                RFeatureManager.this.registerRdaBroadcastReceiver();
                                AwareLog.d(RFeatureManager.TAG, "register RDA broadcast Receiver");
                            } else if (!RFeatureManager.this.mAwareEnabled && RFeatureManager.this.mReceiver != null) {
                                enableAppMngFeature(false);
                                RFeatureManager.this.mContext.unregisterReceiver(RFeatureManager.this.mReceiver);
                                AwareLog.d(RFeatureManager.TAG, "unregister RDA broadcast Receiver");
                                RFeatureManager.this.mReceiver = null;
                                RFeatureManager.this.mReportedDataHandler.removeMessages(2);
                                IAwaredConnection.getInstance().destroy();
                            }
                        } catch (RemoteException e) {
                            AwareLog.e(RFeatureManager.TAG, "call isIAwareEnabled failed");
                        }
                    } else {
                        AwareLog.e(RFeatureManager.TAG, "get IAwareCMSService failed.");
                    }
                }
            }
        }

        private void enableAppMngFeature(boolean enabled) {
            AwareLog.d(RFeatureManager.TAG, "Enable APPMng Feature enable = " + enabled);
            FeatureWrapper feature = (FeatureWrapper) RFeatureManager.this.mRegisteredFeatures.get(AwareConstant.FeatureType.FEATURE_APPMNG);
            if (feature == null || feature.getFeatureInstance() == null) {
                AwareLog.e(RFeatureManager.TAG, "enableAppMngFeature feature null!");
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
            RFeatureManager.this.mAwareVersion = getAwareVersion(bundle);
            for (int index = 0; index < featureNum; index++) {
                RFeatureManager rFeatureManager = RFeatureManager.this;
                int[] featureInfo = rFeatureManager.getFeatureInfoFromBundle(bundle, rFeatureManager.mFeatureTypes[index].name());
                FeatureWrapper wrapper = createFeatureWrapper(RFeatureManager.this.mFeatureNames[index], RFeatureManager.this.mFeatureTypes[index], featureInfo[0], featureInfo[1]);
                if (wrapper != null) {
                    RFeatureManager.this.mRegisteredFeatures.put(RFeatureManager.this.mFeatureTypes[index], wrapper);
                }
            }
        }

        private int getAwareVersion(Bundle bundle) {
            if (bundle == null) {
                return 1;
            }
            return bundle.getInt(RFeatureManager.KEY_IAWARE_VERSION, 1);
        }

        private void registerAppMngFeature(int status) {
            FeatureWrapper wrapper = createFeatureWrapper(RFeatureManager.APP_MNG_FEATURE_NAME, AwareConstant.FeatureType.FEATURE_APPMNG, status, 1);
            if (wrapper != null) {
                RFeatureManager.this.mRegisteredFeatures.put(AwareConstant.FeatureType.FEATURE_APPMNG, wrapper);
            }
        }

        private void controlFeature(RFeature feature, boolean enable, int version, AwareConstant.FeatureType featureType, boolean isInit) {
            AwareLog.d(RFeatureManager.TAG, "iAware2.0: feature id is " + featureType.getValue());
            boolean useOld = true;
            if (version > 1) {
                useOld = false;
            }
            if (!isInit || !useOld) {
                newControlFeature(feature, enable, version);
                return;
            }
            AwareLog.d(RFeatureManager.TAG, "iAware2.0: controlFeature use default init!");
            iAware1InitFeature(feature, enable, featureType);
        }

        private void iAware1InitFeature(RFeature feature, boolean enable, AwareConstant.FeatureType featureType) {
            if (enable) {
                feature.enable();
            } else if (featureType == AwareConstant.FeatureType.FEATURE_RESOURCE) {
                feature.disable();
            }
        }

        private void newControlFeature(RFeature feature, boolean enable, int version) {
            AwareLog.d(RFeatureManager.TAG, "iAware2.0: newControlFeature feature version is " + version);
            if (!enable) {
                AwareLog.d(RFeatureManager.TAG, "iAware2.0: disable!");
                feature.disable();
            } else if (version > 1) {
                int realVersion = Math.min(RFeatureManager.this.mAwareVersion, version);
                AwareLog.d(RFeatureManager.TAG, "iAware2.0: use enable Ex! realVersion is " + realVersion);
                feature.enableFeatureEx(realVersion);
            } else {
                AwareLog.d(RFeatureManager.TAG, "iAware2.0: use old enable!");
                feature.enable();
            }
        }

        private FeatureWrapper createFeatureWrapper(String featureName, AwareConstant.FeatureType featureType, int featureStatus, int featureVersion) {
            RFeature feature;
            AwareLog.d(RFeatureManager.TAG, "createFeatureWrapper name = " + featureName + " type = " + featureType);
            try {
                Constructor<?>[] featureConstructor = Class.forName(featureName).getConstructors();
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append("createFeatureWrapper constructor = ");
                    boolean status = false;
                    sb.append(featureConstructor[0].getName());
                    AwareLog.d(RFeatureManager.TAG, sb.toString());
                    Object instance = featureConstructor[0].newInstance(RFeatureManager.this.mContext, featureType, RFeatureManager.this);
                    if (instance instanceof RFeature) {
                        feature = (RFeature) instance;
                    } else {
                        feature = null;
                    }
                    if (feature == null) {
                        AwareLog.e(RFeatureManager.TAG, "createFeatureWrapper feature fail");
                        return null;
                    }
                    AwareLog.d(RFeatureManager.TAG, "createFeatureWrapper featureStatus = " + featureStatus);
                    if (featureStatus == 1) {
                        status = true;
                    }
                    controlFeature(feature, status, featureVersion, featureType, true);
                    return new FeatureWrapper(feature, status, featureVersion);
                } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException e) {
                    AwareLog.e(RFeatureManager.TAG, "createFeatureWrapper newInstance exception " + e.getMessage());
                    return null;
                }
            } catch (ClassNotFoundException e2) {
                AwareLog.e(RFeatureManager.TAG, "createFeatureWrapper forName ClassNotFoundException");
                return null;
            }
        }

        private void deliveryDataToFeatures(CollectData data, IReportDataCallback callback) {
            Throwable th;
            int subcribedFeatureNum;
            AwareConstant.ResourceType resType = AwareConstant.ResourceType.getResourceType(data.getResId());
            int index = 0;
            synchronized (RFeatureManager.this.mSubscribeDataMap) {
                try {
                    List<AwareConstant.FeatureType> curList = (List) RFeatureManager.this.mSubscribeDataMap.get(resType);
                    if (curList != null) {
                        subcribedFeatureNum = curList.size();
                        if (subcribedFeatureNum < RFeatureManager.this.mSubscribedFeatureTypeNum) {
                            for (AwareConstant.FeatureType feature : curList) {
                                int index2 = index + 1;
                                try {
                                    RFeatureManager.this.mSubscribedFeatureTypes[index] = feature;
                                    index = index2;
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            }
                        } else {
                            AwareLog.e(RFeatureManager.TAG, "deliveryDataToFeatures subscribed too much features!");
                            return;
                        }
                    } else {
                        AwareLog.d(RFeatureManager.TAG, "deliveryDataToFeatures no subscribed features resType = " + resType.name());
                        return;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
            for (int index3 = 0; index3 < subcribedFeatureNum; index3++) {
                FeatureWrapper feature2 = (FeatureWrapper) RFeatureManager.this.mRegisteredFeatures.get(RFeatureManager.this.mSubscribedFeatureTypes[index3]);
                if (!(feature2 == null || feature2.getFeatureInstance() == null || !feature2.getFeatureEnabled())) {
                    if (callback == null) {
                        feature2.getFeatureInstance().reportData(data);
                    } else {
                        feature2.getFeatureInstance().reportDataWithCallback(data, callback);
                    }
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
                    boolean result = feature.getFeatureInstance().custConfigUpdate();
                    AwareLog.d(RFeatureManager.TAG, feature.getFeatureInstance() + " notifyCustConfigUpdate return " + result);
                }
            }
        }
    }

    public RFeature getRegisteredFeature(AwareConstant.FeatureType type) {
        if (!this.mRegisteredFeatures.containsKey(type)) {
            return null;
        }
        FeatureWrapper feature = this.mRegisteredFeatures.get(type);
        if (feature != null) {
            return feature.getFeatureInstance();
        }
        AwareLog.e(TAG, "getRegisteredFeature feature null!");
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerRdaBroadcastReceiver() {
        IntentFilter deviceStates = new IntentFilter();
        deviceStates.addAction("android.intent.action.SCREEN_ON");
        deviceStates.addAction("android.intent.action.SCREEN_OFF");
        deviceStates.setPriority(1000);
        ContextEx.registerReceiverAsUser(this.mContext, this.mReceiver, UserHandleEx.ALL, deviceStates, (String) null, (Handler) null);
        IntentFilter bootStates = new IntentFilter();
        bootStates.addAction("android.intent.action.BOOT_COMPLETED");
        ContextEx.registerReceiverAsUser(this.mContext, this.mReceiver, UserHandleEx.ALL, bootStates, (String) null, (Handler) null);
        IntentFilter appStates = new IntentFilter();
        appStates.addAction("android.intent.action.PACKAGE_ADDED");
        appStates.addAction("android.intent.action.PACKAGE_REMOVED");
        appStates.addDataScheme(DATA_SCHEME);
        ContextEx.registerReceiverAsUser(this.mContext, this.mReceiver, UserHandleEx.ALL, appStates, (String) null, (Handler) null);
        IntentFilter completedTrain = new IntentFilter();
        completedTrain.addAction(USER_HABIT_TRAIN_COMPLETED_ACTION);
        ContextEx.registerReceiverAsUser(this.mContext, this.mReceiver, UserHandleEx.ALL, completedTrain, USER_HABIT_RECV_TRAIN_COMPLETED_PERMISSION, (Handler) null);
        IntentFilter presentFilter = new IntentFilter();
        presentFilter.addAction("android.intent.action.USER_PRESENT");
        ContextEx.registerReceiverAsUser(this.mContext, this.mReceiver, UserHandleEx.ALL, presentFilter, (String) null, (Handler) null);
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_REMOVED");
        ContextEx.registerReceiverAsUser(this.mContext, this.mReceiver, UserHandleEx.ALL, userFilter, (String) null, (Handler) null);
        ContextEx.registerReceiverAsUser(this.mContext, this.mReceiver, UserHandleEx.ALL, new IntentFilter("android.intent.action.ACTION_SHUTDOWN"), (String) null, (Handler) null);
        IntentFilter topLevelFilter = new IntentFilter();
        topLevelFilter.addAction(ACTION_TOPM_LEVEL);
        ContextEx.registerReceiverAsUser(this.mContext, this.mReceiver, UserHandleEx.ALL, topLevelFilter, TOP_ACTIVITY_RECV_PERMISSION, (Handler) null);
        IntentFilter dataClearFilter = new IntentFilter();
        dataClearFilter.addAction("android.intent.action.PACKAGE_DATA_CLEARED");
        dataClearFilter.addDataScheme(DATA_SCHEME);
        ContextEx.registerReceiverAsUser(this.mContext, this.mReceiver, UserHandleEx.ALL, dataClearFilter, (String) null, (Handler) null);
    }

    public int[] getFeatureInfoFromBundle(Bundle bundle, String key) {
        int[] info = null;
        if (bundle != null) {
            try {
                info = bundle.getIntArray(key);
            } catch (ArrayIndexOutOfBoundsException e) {
                AwareLog.e(TAG, "ArrayIndexOutOfBoundsException in getFeatureInfoFromBundle");
            }
        }
        return (info == null || info.length != 2) ? new int[]{0, 1} : info;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void triggerGc(boolean cancel) {
        if (ZygoteInitEx.isMygote()) {
            Message dataMessage = Message.obtain();
            if (this.mReportedDataHandler.hasMessages(5)) {
                this.mReportedDataHandler.removeMessages(5);
            }
            if (!cancel) {
                dataMessage.what = 5;
                this.mReportedDataHandler.sendMessageDelayed(dataMessage, 5000);
            }
        }
    }

    /* access modifiers changed from: private */
    public class AwareDeviceStateReceiver extends BroadcastReceiver {
        private AwareDeviceStateReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                AwareLog.e(RFeatureManager.TAG, "BroadcastReceiver error parameters!");
                return;
            }
            String action = intent.getAction();
            if (action != null) {
                if ("android.intent.action.SCREEN_ON".equals(action)) {
                    RFeatureManager.this.reportData(new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_SCREEN_ON), System.currentTimeMillis(), action));
                    RFeatureManager.this.triggerGc(true);
                    HwResMngEngine.getInstance().sendMmEvent(2, 0);
                } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    RFeatureManager.this.reportData(new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF), System.currentTimeMillis(), action));
                    RFeatureManager.this.triggerGc(false);
                    HwResMngEngine.getInstance().sendMmEvent(3, 0);
                } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                    handleBootCompleted(action);
                } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                    handlePackageRemoved(intent);
                } else if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                    handlePackageAdded(intent);
                } else if (RFeatureManager.USER_HABIT_TRAIN_COMPLETED_ACTION.equals(action)) {
                    handleHabitTrainCompleted();
                } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                    handleUserPresent(action);
                } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                    handleUserRemoved(intent);
                } else if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                    handleActionShutdown(action);
                } else if (RFeatureManager.ACTION_TOPM_LEVEL.equals(action)) {
                    handleTopM(intent);
                } else if ("android.intent.action.PACKAGE_DATA_CLEARED".equals(action)) {
                    handlePkgClearData(intent);
                }
            }
        }

        private void handlePkgClearData(Intent intent) {
            if (intent.getData() != null) {
                int id = AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_PKG_CLEAR_DATA);
                long curTime = System.currentTimeMillis();
                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                Bundle bundle = new Bundle();
                bundle.putInt("android.intent.extra.UID", uid);
                RFeatureManager.this.reportData(new CollectData(id, curTime, bundle));
            }
        }

        private void handleUserPresent(String action) {
            RFeatureManager.this.reportData(new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_USER_PRESENT), System.currentTimeMillis(), action));
        }

        private void handleTopM(Intent intent) {
            int resIdTopM = AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_TOP_ACTIVITY);
            long curTime = System.currentTimeMillis();
            Bundle bdl = intent.getExtras();
            if (bdl == null) {
                AwareLog.d(RFeatureManager.TAG, "bdl is null");
                return;
            }
            RFeatureManager.this.reportData(new CollectData(resIdTopM, curTime, bdl));
        }

        private void handleActionShutdown(String action) {
            RFeatureManager.this.reportData(new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_SHUTDOWN), System.currentTimeMillis(), action));
        }

        private void handleBootCompleted(String action) {
            RFeatureManager.this.reportData(new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_BOOT_COMPLETED), System.currentTimeMillis(), action));
        }

        private void handlePackageRemoved(Intent intent) {
            if (intent.getData() != null) {
                int resIdHabitStat = AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_USERHABIT);
                long curTime = System.currentTimeMillis();
                String pkgName = intent.getData().getSchemeSpecificPart();
                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                Bundle bdl = new Bundle();
                bdl.putString(AwareUserHabit.USER_HABIT_PACKAGE_NAME, pkgName);
                bdl.putInt("uid", uid);
                bdl.putInt(AwareUserHabit.USER_HABIT_INSTALL_APP_UPDATE, 2);
                RFeatureManager.this.reportData(new CollectData(resIdHabitStat, curTime, bdl));
                HwResMngEngine.getInstance().sendMmEvent(4, uid);
            }
        }

        private void handlePackageAdded(Intent intent) {
            if (intent.getData() != null) {
                int resIdHabitStat = AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_USERHABIT);
                long curTime = System.currentTimeMillis();
                String pkgName = intent.getData().getSchemeSpecificPart();
                Bundle bdl = new Bundle();
                bdl.putString(AwareUserHabit.USER_HABIT_PACKAGE_NAME, pkgName);
                bdl.putInt(AwareUserHabit.USER_HABIT_INSTALL_APP_UPDATE, 1);
                RFeatureManager.this.reportData(new CollectData(resIdHabitStat, curTime, bdl));
            }
        }

        private void handleHabitTrainCompleted() {
            int resIdHabitStat = AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_USERHABIT);
            long curTime = System.currentTimeMillis();
            Bundle bdl = new Bundle();
            bdl.putInt(AwareUserHabit.USER_HABIT_INSTALL_APP_UPDATE, 3);
            RFeatureManager.this.reportData(new CollectData(resIdHabitStat, curTime, bdl));
        }

        private void handleUserRemoved(Intent intent) {
            int resIdAssoc = AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC);
            long curTime = System.currentTimeMillis();
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
            Bundle bdl = new Bundle();
            bdl.putInt("relationType", 29);
            bdl.putInt("userid", userId);
            RFeatureManager.this.reportData(new CollectData(resIdAssoc, curTime, bdl));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        android.rms.iaware.AwareLog.e(com.android.server.rms.iaware.RFeatureManager.TAG, "read xml failed, error: exception");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00a0, code lost:
        com.android.server.rms.iaware.CommonUtils.closeStream(r2, com.android.server.rms.iaware.RFeatureManager.TAG, "close file input stream fail!!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00a4, code lost:
        return -1;
     */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x009a A[ExcHandler: IOException | NumberFormatException | XmlPullParserException (e java.lang.Throwable), Splitter:B:7:0x002c] */
    private int parseFeatureSwitchFormCustFile(int featureId) {
        InputStream inputStream = null;
        XmlPullParser parser = Xml.newPullParser();
        File custConfigFile = loadCustFeatureSwitchFile();
        if (custConfigFile.exists()) {
            inputStream = new FileInputStream(custConfigFile);
            parser.setInput(inputStream, StandardCharsets.UTF_8.name());
        }
        if (inputStream == null) {
            CommonUtils.closeStream(inputStream, TAG, "close file input stream fail!!");
            return -1;
        }
        boolean enterItemTag = false;
        Map<String, String> configItem = null;
        try {
            int eventType = parser.getEventType();
            while (eventType != 1) {
                String tagName = parser.getName();
                if (eventType == 2) {
                    if (XML_TAG_ITEM.equals(tagName)) {
                        enterItemTag = true;
                        configItem = new HashMap<>();
                        eventType = parser.next();
                    } else if (tagName != null && enterItemTag) {
                        addToConfigItem(configItem, tagName, parser.nextText());
                    }
                }
                if (eventType == 3 && XML_TAG_ITEM.equals(tagName)) {
                    AwareLog.d(TAG, "exit item");
                    enterItemTag = false;
                    if (configItem != null && configItem.get(XML_TAG_FEATURE_ID).equals(Integer.toString(featureId))) {
                        int parseInt = Integer.parseInt(configItem.get("switch"));
                        CommonUtils.closeStream(inputStream, TAG, "close file input stream fail!!");
                        return parseInt;
                    }
                }
                eventType = parser.next();
            }
            CommonUtils.closeStream(inputStream, TAG, "close file input stream fail!!");
            return -1;
        } catch (IOException | NumberFormatException | XmlPullParserException e) {
        } catch (Throwable th) {
            CommonUtils.closeStream(inputStream, TAG, "close file input stream fail!!");
            throw th;
        }
    }

    private void addToConfigItem(Map<String, String> configItem, String tagName, String value) {
        if (configItem != null) {
            configItem.put(tagName, value);
        }
    }

    /* access modifiers changed from: package-private */
    public File loadCustFeatureSwitchFile() {
        try {
            File cfg = HwCfgFilePolicy.getCfgFile("xml/iAwareFeatureSwitch.xml", 0);
            if (cfg != null) {
                AwareLog.d(TAG, "find cust switch file.");
                return cfg;
            }
        } catch (NoClassDefFoundError e) {
            AwareLog.e(TAG, "loadCustFeatureSwitchFile NoClassDefFoundError : HwCfgFilePolicy ");
        }
        return new File(CUST_PATH_DIR + File.separator + FEATURE_SWITCH_FILE_NAME);
    }
}

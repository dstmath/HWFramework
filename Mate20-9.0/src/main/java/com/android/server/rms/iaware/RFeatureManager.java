package com.android.server.rms.iaware;

import android.appwidget.HwAppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.DumpData;
import android.rms.iaware.IAwareCMSManager;
import android.rms.iaware.IAwaredConnection;
import android.rms.iaware.IReportDataCallback;
import android.rms.iaware.StatisticsData;
import android.util.Xml;
import com.android.internal.os.SomeArgs;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.feature.RFeature;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.os.HwPowerManager;
import com.huawei.android.view.HwWindowManager;
import dalvik.system.VMRuntime;
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

public class RFeatureManager implements IRDataRegister {
    private static final String CUST_PATH_DIR = "/data/cust/xml";
    private static final int FEATURE_MAX_DUMP_SIZE = 5;
    public static final int FEATURE_STATUS_DISABLE = 0;
    public static final int FEATURE_STATUS_ENABLE = 1;
    private static final String FEATURE_SWITCH_FILE_NAME = "iAwareFeatureSwitch.xml";
    private static final int GC_MIN_INTERVAL = 300000;
    /* access modifiers changed from: private */
    public static int IAWARE_VERSION_DEFAULT = 1;
    /* access modifiers changed from: private */
    public static String KEY_IAWARE_VERSION = "iAwareVersion";
    private static final int MSG_CONFIG_UPDATE = 3;
    private static final int MSG_ENABLE_DISABLE_FEATURE = 1;
    private static final int MSG_INIT_FEATURE = 0;
    private static final int MSG_REPORT_DATA = 2;
    private static final int MSG_REPORT_DATA_CALLBACK = 4;
    private static final int MSG_TRIGGER_GC = 5;
    private static final int MSG_UPDATE_ARG_CONFIG = 0;
    private static final int MSG_UPDATE_ARG_CUST_CONFIG = 1;
    private static final String TAG = "RFeatureManager";
    private static final int TRIGGER_MAX_DELAY_MILLIS = 5000;
    private static final int UNKNOWN_FEATURE_STATUS = -1;
    private static final String USER_HABIT_RECV_TRAIN_COMPLETED_PERMISSION = "com.huawei.iaware.userhabit.USERHABIT_PERMISSION";
    private static final String USER_HABIT_TRAIN_COMPLETED_ACTION = "com.huawei.iaware.userhabit.TRAIN_COMPLETED";
    private static final String XML_TAG_FEATURE_ID = "featureid";
    private static final String XML_TAG_FEATURE_SWITCH = "switch";
    private static final String XML_TAG_ITEM = "item";
    private static final String mAPPMngFeatureName = "com.android.server.rms.iaware.feature.APPMngFeature";
    /* access modifiers changed from: private */
    public static final AwareConstant.FeatureType mAPPMngFeatureType = AwareConstant.FeatureType.FEATURE_APPMNG;
    private static boolean sIsMygote = (System.getenv("MAPLE_RUNTIME") != null);
    /* access modifiers changed from: private */
    public AwareAWSIMonitorCallback mAwsiCallback = null;
    /* access modifiers changed from: private */
    public Context mContext = null;
    /* access modifiers changed from: private */
    public final String[] mFeatureNames = {"com.android.server.rms.iaware.feature.AppRecgFeature", "com.android.server.rms.iaware.cpu.CPUFeature", "com.android.server.rms.iaware.srms.ResourceFeature", "com.android.server.rms.iaware.feature.APPHiberFeature", "com.android.server.rms.iaware.feature.MemoryFeature", "com.android.server.rms.iaware.feature.IOFeature", "com.android.server.rms.iaware.srms.BroadcastFeature", "com.android.server.rms.iaware.feature.VsyncFeature", "com.android.server.rms.iaware.feature.MemoryFeature2", "com.android.server.rms.iaware.srms.AppStartupFeature", "com.android.server.rms.iaware.feature.AppFakeFeature", "com.android.server.rms.iaware.feature.NetworkFeature", "com.android.server.rms.iaware.feature.APPFreezeFeature", "com.android.server.rms.iaware.feature.APPIoLimitFeature", "com.android.server.rms.iaware.srms.AppCleanupFeature", "com.android.server.rms.iaware.feature.DevSchedFeatureRT", "com.android.server.rms.iaware.feature.AlarmManagerFeature", "com.android.server.rms.iaware.feature.BlitParallelFeature", "com.android.server.rms.iaware.srms.BroadcastExFeature", "com.android.server.rms.iaware.feature.SysLoadFeature", "com.android.server.rms.iaware.feature.AppQuickStartFeature", "com.android.server.rms.iaware.feature.NetworkTcpNodelayFeature", "com.android.server.rms.iaware.feature.StartWindowFeature", "com.android.server.rms.iaware.feature.PreloadResourceFeature", "com.android.server.rms.iaware.feature.AppCpuLimitFeature"};
    /* access modifiers changed from: private */
    public final AwareConstant.FeatureType[] mFeatureTypes = {AwareConstant.FeatureType.FEATURE_INTELLI_REC, AwareConstant.FeatureType.FEATURE_CPU, AwareConstant.FeatureType.FEATURE_RESOURCE, AwareConstant.FeatureType.FEATURE_APPHIBER, AwareConstant.FeatureType.FEATURE_MEMORY, AwareConstant.FeatureType.FEATURE_IO, AwareConstant.FeatureType.FEATURE_BROADCAST, AwareConstant.FeatureType.FEATURE_VSYNC, AwareConstant.FeatureType.FEATURE_MEMORY2, AwareConstant.FeatureType.FEATURE_APPSTARTUP, AwareConstant.FeatureType.FEATURE_RECG_FAKEACTIVITY, AwareConstant.FeatureType.FEATURE_NETWORK_TCP, AwareConstant.FeatureType.FEATURE_APPFREEZE, AwareConstant.FeatureType.FEATURE_IO_LIMIT, AwareConstant.FeatureType.FEATURE_APPCLEANUP, AwareConstant.FeatureType.FEATURE_DEVSCHED, AwareConstant.FeatureType.FEATURE_ALARM_MANAGER, AwareConstant.FeatureType.FEATURE_BLIT_PARALLEL, AwareConstant.FeatureType.FEATURE_BROADCASTEX, AwareConstant.FeatureType.FEATURE_SYSLOAD, AwareConstant.FeatureType.FEATURE_APP_QUICKSTART, AwareConstant.FeatureType.FEATURE_NETWORK_TCP_NODELAY, AwareConstant.FeatureType.FEATURE_STARTWINDOW, AwareConstant.FeatureType.PRELOADRESOURCE, AwareConstant.FeatureType.FEATURE_CPU_LIMIT};
    /* access modifiers changed from: private */
    public boolean mIAwareEnabled = false;
    /* access modifiers changed from: private */
    public int mIAwareVersion = IAWARE_VERSION_DEFAULT;
    private XmlPullParser mParser = null;
    /* access modifiers changed from: private */
    public IAwareDeviceStateReceiver mReceiver = null;
    /* access modifiers changed from: private */
    public boolean mRegisterAWMSuccess = false;
    /* access modifiers changed from: private */
    public boolean mRegisterWMSuccess = false;
    /* access modifiers changed from: private */
    public Map<AwareConstant.FeatureType, FeatureWrapper> mRegisteredFeatures = null;
    /* access modifiers changed from: private */
    public ReportedDataHandler mReportedDataHandler;
    /* access modifiers changed from: private */
    public Map<AwareConstant.ResourceType, ArrayList<AwareConstant.FeatureType>> mSubscribeDataMap = null;
    /* access modifiers changed from: private */
    public final int mSubscribedFeatureTypeNum;
    /* access modifiers changed from: private */
    public final AwareConstant.FeatureType[] mSubscribedFeatureTypes;
    private int mWMRetryTime = 50;
    /* access modifiers changed from: private */
    public AwareWmsMonitorCallback mWmsCallback = null;
    /* access modifiers changed from: private */
    public long sLastGcTime = 0;

    private class CallbackRegistration implements Runnable {
        private CallbackRegistration() {
        }

        public void run() {
            boolean z = true;
            boolean unused = RFeatureManager.this.mRegisterWMSuccess = RFeatureManager.this.mRegisterWMSuccess || HwWindowManager.registerWMMonitorCallback(RFeatureManager.this.mWmsCallback);
            RFeatureManager rFeatureManager = RFeatureManager.this;
            if (!RFeatureManager.this.mRegisterAWMSuccess && !HwAppWidgetManager.registerAWSIMonitorCallback(RFeatureManager.this.mAwsiCallback)) {
                z = false;
            }
            boolean unused2 = rFeatureManager.mRegisterAWMSuccess = z;
            if ((!RFeatureManager.this.mRegisterAWMSuccess || !RFeatureManager.this.mRegisterWMSuccess) && RFeatureManager.access$510(RFeatureManager.this) > 0) {
                RFeatureManager.this.mReportedDataHandler.postDelayed(this, 200);
            }
        }
    }

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
        private IAwareDeviceStateReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                AwareLog.e(RFeatureManager.TAG, "BroadcastReceiver error parameters!");
                return;
            }
            String action = intent.getAction();
            if ("android.intent.action.SCREEN_ON".equals(action)) {
                RFeatureManager.this.reportData(new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_SCREEN_ON), System.currentTimeMillis(), action));
                RFeatureManager.this.triggerGc(true);
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                RFeatureManager.this.reportData(new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF), System.currentTimeMillis(), action));
                RFeatureManager.this.triggerGc(false);
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                RFeatureManager.this.reportData(new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_BOOT_COMPLETED), System.currentTimeMillis(), action));
            } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                if (intent.getData() != null) {
                    int resIDHabitStat = AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_USERHABIT);
                    long curtime = System.currentTimeMillis();
                    String pkgName = intent.getData().getSchemeSpecificPart();
                    int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                    Bundle bdl = new Bundle();
                    bdl.putString(AwareUserHabit.USERHABIT_PACKAGE_NAME, pkgName);
                    bdl.putInt("uid", uid);
                    bdl.putInt(AwareUserHabit.USERHABIT_INSTALL_APP_UPDATE, 2);
                    RFeatureManager.this.reportData(new CollectData(resIDHabitStat, curtime, bdl));
                }
            } else if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                if (intent.getData() != null) {
                    int resIDHabitStat2 = AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_USERHABIT);
                    long curtime2 = System.currentTimeMillis();
                    String pkgName2 = intent.getData().getSchemeSpecificPart();
                    Bundle bdl2 = new Bundle();
                    bdl2.putString(AwareUserHabit.USERHABIT_PACKAGE_NAME, pkgName2);
                    bdl2.putInt(AwareUserHabit.USERHABIT_INSTALL_APP_UPDATE, 1);
                    RFeatureManager.this.reportData(new CollectData(resIDHabitStat2, curtime2, bdl2));
                }
            } else if (RFeatureManager.USER_HABIT_TRAIN_COMPLETED_ACTION.equals(action)) {
                int resIDHabitStat3 = AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_USERHABIT);
                long curtime3 = System.currentTimeMillis();
                Bundle bdl3 = new Bundle();
                bdl3.putInt(AwareUserHabit.USERHABIT_INSTALL_APP_UPDATE, 3);
                RFeatureManager.this.reportData(new CollectData(resIDHabitStat3, curtime3, bdl3));
            } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                RFeatureManager.this.reportData(new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_USER_PRESENT), System.currentTimeMillis(), action));
            } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                int resIDAssoc = AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC);
                long curtime4 = System.currentTimeMillis();
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                Bundle bdl4 = new Bundle();
                bdl4.putInt("relationType", 29);
                bdl4.putInt("userid", userId);
                RFeatureManager.this.reportData(new CollectData(resIDAssoc, curtime4, bdl4));
            } else if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                RFeatureManager.this.reportData(new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_SHUTDOWN), System.currentTimeMillis(), action));
            }
        }
    }

    private final class ReportedDataHandler extends Handler {
        public ReportedDataHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    registerFeatures(msg.getData());
                    IBinder awareservice = IAwareCMSManager.getICMSManager();
                    if (awareservice == null) {
                        AwareLog.e(RFeatureManager.TAG, "get IAwareCMSService failed.");
                        break;
                    } else {
                        try {
                            boolean unused = RFeatureManager.this.mIAwareEnabled = IAwareCMSManager.isIAwareEnabled(awareservice);
                            if (RFeatureManager.this.mIAwareEnabled) {
                                registerAPPMngFeature(1);
                            } else {
                                registerAPPMngFeature(0);
                            }
                            if (RFeatureManager.this.mIAwareEnabled && RFeatureManager.this.mReceiver == null) {
                                IAwareDeviceStateReceiver unused2 = RFeatureManager.this.mReceiver = new IAwareDeviceStateReceiver();
                                RFeatureManager.this.registerRDABroadcastReceiver();
                                AwareLog.d(RFeatureManager.TAG, "register RDA broadcast Receiver");
                                break;
                            }
                        } catch (RemoteException e) {
                            AwareLog.e(RFeatureManager.TAG, "call isIAwareEnabled failed." + e.getMessage());
                            break;
                        }
                    }
                case 1:
                    AwareConstant.FeatureType featureType = AwareConstant.FeatureType.getFeatureType(msg.arg2);
                    AwareLog.d(RFeatureManager.TAG, "handler message featureType:" + featureType.name() + "  status:" + msg.arg1);
                    if (RFeatureManager.this.mRegisteredFeatures.containsKey(featureType)) {
                        boolean status = msg.arg1 == 1;
                        FeatureWrapper feature = (FeatureWrapper) RFeatureManager.this.mRegisteredFeatures.get(featureType);
                        if (feature != null && feature.getFeatureInstance() != null) {
                            if (feature.getFeatureEnabled() != status) {
                                controlFeature(feature.getFeatureInstance(), status, feature.getFeatureVersion(), featureType, false);
                                feature.setFeatureEnabled(status);
                                IBinder awareservice2 = IAwareCMSManager.getICMSManager();
                                if (awareservice2 == null) {
                                    AwareLog.e(RFeatureManager.TAG, "get IAwareCMSService failed.");
                                    break;
                                } else {
                                    try {
                                        boolean unused3 = RFeatureManager.this.mIAwareEnabled = IAwareCMSManager.isIAwareEnabled(awareservice2);
                                        if (!RFeatureManager.this.mIAwareEnabled || RFeatureManager.this.mReceiver != null) {
                                            if (!RFeatureManager.this.mIAwareEnabled && RFeatureManager.this.mReceiver != null) {
                                                enableAPPMngFeature(false);
                                                RFeatureManager.this.mContext.unregisterReceiver(RFeatureManager.this.mReceiver);
                                                AwareLog.d(RFeatureManager.TAG, "unregister RDA broadcast Receiver");
                                                IAwareDeviceStateReceiver unused4 = RFeatureManager.this.mReceiver = null;
                                                RFeatureManager.this.mReportedDataHandler.removeMessages(2);
                                                IAwaredConnection.getInstance().destroy();
                                                break;
                                            }
                                        } else {
                                            enableAPPMngFeature(true);
                                            IAwareDeviceStateReceiver unused5 = RFeatureManager.this.mReceiver = new IAwareDeviceStateReceiver();
                                            RFeatureManager.this.registerRDABroadcastReceiver();
                                            AwareLog.d(RFeatureManager.TAG, "register RDA broadcast Receiver");
                                            break;
                                        }
                                    } catch (RemoteException e2) {
                                        AwareLog.e(RFeatureManager.TAG, "call isIAwareEnabled failed." + e2.getMessage());
                                        break;
                                    }
                                }
                            } else {
                                return;
                            }
                        } else {
                            AwareLog.e(RFeatureManager.TAG, "handleMessage ENABLE_DISABLE_FEATURE feature null: " + featureType.name());
                            return;
                        }
                    }
                    break;
                case 2:
                    deliveryDataToFeatures((CollectData) msg.obj, null);
                    break;
                case 3:
                    notifyUpdate(msg.arg1);
                    break;
                case 4:
                    SomeArgs args = (SomeArgs) msg.obj;
                    args.recycle();
                    deliveryDataToFeatures((CollectData) args.arg1, (IReportDataCallback) args.arg2);
                    break;
                case 5:
                    long tmp = SystemClock.elapsedRealtime();
                    long diff = tmp - RFeatureManager.this.sLastGcTime;
                    if (diff > 0 && diff < HwArbitrationDEFS.DelayTimeMillisB) {
                        AwareLog.d(RFeatureManager.TAG, "trigger gc too frequently. ");
                        break;
                    } else {
                        AwareLog.d(RFeatureManager.TAG, "trigger gc when screen off for maple. ");
                        Runtime.getRuntime().gc();
                        VMRuntime.getRuntime();
                        VMRuntime.notifySaveCyclePattern();
                        long unused6 = RFeatureManager.this.sLastGcTime = tmp;
                        break;
                    }
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
            int unused = RFeatureManager.this.mIAwareVersion = getIAwareVersion(bundle);
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

        private void controlFeature(RFeature feature, boolean enable, int version, AwareConstant.FeatureType featureType, boolean isInit) {
            AwareLog.d(RFeatureManager.TAG, "iAware2.0: feature id is " + featureType.ordinal());
            boolean useOld = version <= RFeatureManager.IAWARE_VERSION_DEFAULT;
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
            } else if (version > RFeatureManager.IAWARE_VERSION_DEFAULT) {
                int realVersion = Math.min(RFeatureManager.this.mIAwareVersion, version);
                AwareLog.d(RFeatureManager.TAG, "iAware2.0: use enable Ex! realVersion is " + realVersion);
                feature.enableFeatureEx(realVersion);
            } else {
                AwareLog.d(RFeatureManager.TAG, "iAware2.0: use old enable!");
                feature.enable();
            }
        }

        private FeatureWrapper createFeatureWrapper(String featureName, AwareConstant.FeatureType featureType, int featureStatus, int featureVersion) {
            AwareConstant.FeatureType featureType2 = featureType;
            int i = featureStatus;
            StringBuilder sb = new StringBuilder();
            sb.append("createFeatureWrapper name = ");
            String str = featureName;
            sb.append(str);
            sb.append(" type = ");
            sb.append(featureType2);
            AwareLog.d(RFeatureManager.TAG, sb.toString());
            try {
                Constructor<?>[] featureConstructor = Class.forName(str).getConstructors();
                try {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("createFeatureWrapper constructor = ");
                    boolean z = false;
                    sb2.append(featureConstructor[0].getName());
                    AwareLog.d(RFeatureManager.TAG, sb2.toString());
                    RFeature feature = (RFeature) featureConstructor[0].newInstance(new Object[]{RFeatureManager.this.mContext, featureType2, RFeatureManager.this});
                    AwareLog.d(RFeatureManager.TAG, "createFeatureWrapper featureStatus = " + i);
                    if (i == 1) {
                        z = true;
                    }
                    boolean status = z;
                    controlFeature(feature, status, featureVersion, featureType2, true);
                    return new FeatureWrapper(feature, status, featureVersion);
                } catch (IllegalArgumentException e) {
                    int i2 = featureVersion;
                    AwareLog.e(RFeatureManager.TAG, "createFeatureWrapper newInstance IllegalArgumentException");
                    return null;
                } catch (IllegalAccessException e2) {
                    int i3 = featureVersion;
                    AwareLog.e(RFeatureManager.TAG, "createFeatureWrapper newInstance IllegalAccessException");
                    return null;
                } catch (InstantiationException e3) {
                    int i4 = featureVersion;
                    AwareLog.e(RFeatureManager.TAG, "createFeatureWrapper newInstance InstantiationException");
                    return null;
                } catch (InvocationTargetException e4) {
                    int i5 = featureVersion;
                    AwareLog.e(RFeatureManager.TAG, "createFeatureWrapper newInstance InvocationTargetException");
                    return null;
                }
            } catch (ClassNotFoundException e5) {
                int i6 = featureVersion;
                ClassNotFoundException classNotFoundException = e5;
                AwareLog.e(RFeatureManager.TAG, "createFeatureWrapper forName ClassNotFoundException");
                return null;
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:19:0x004e, code lost:
            r3 = 0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x004f, code lost:
            if (r3 >= r2) goto L_0x0087;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0051, code lost:
            r5 = (com.android.server.rms.iaware.RFeatureManager.FeatureWrapper) com.android.server.rms.iaware.RFeatureManager.access$1100(r10.this$0).get(com.android.server.rms.iaware.RFeatureManager.access$2200(r10.this$0)[r3]);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0065, code lost:
            if (r5 == null) goto L_0x0084;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x006b, code lost:
            if (r5.getFeatureInstance() == null) goto L_0x0084;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x0071, code lost:
            if (r5.getFeatureEnabled() == false) goto L_0x0084;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:0x0073, code lost:
            if (r12 != null) goto L_0x007d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:0x0075, code lost:
            r5.getFeatureInstance().reportData(r11);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:0x007d, code lost:
            r5.getFeatureInstance().reportDataWithCallback(r11, r12);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x0084, code lost:
            r3 = r3 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x0087, code lost:
            return;
         */
        private void deliveryDataToFeatures(CollectData data, IReportDataCallback callback) {
            AwareConstant.ResourceType resType = AwareConstant.ResourceType.getResourceType(data.getResId());
            int index = 0;
            synchronized (RFeatureManager.this.mSubscribeDataMap) {
                try {
                    List<AwareConstant.FeatureType> currlist = (List) RFeatureManager.this.mSubscribeDataMap.get(resType);
                    if (currlist != null) {
                        int subcribedFeatureNum = currlist.size();
                        if (subcribedFeatureNum < RFeatureManager.this.mSubscribedFeatureTypeNum) {
                            for (AwareConstant.FeatureType feature : currlist) {
                                int index2 = index + 1;
                                try {
                                    RFeatureManager.this.mSubscribedFeatureTypes[index] = feature;
                                    index = index2;
                                } catch (Throwable th) {
                                    th = th;
                                    int i = index2;
                                    throw th;
                                }
                            }
                        } else {
                            AwareLog.e(RFeatureManager.TAG, "deliveryDataToFeatures subscribed too much features!");
                        }
                    } else {
                        AwareLog.d(RFeatureManager.TAG, "deliveryDataToFeatures no subscribed features resType = " + resType.name());
                    }
                } catch (Throwable th2) {
                    th = th2;
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
                    boolean notifyResult = feature.getFeatureInstance().custConfigUpdate();
                    AwareLog.d(RFeatureManager.TAG, feature.getFeatureInstance() + " notifyCustConfigUpdate return " + notifyResult);
                }
            }
        }
    }

    static /* synthetic */ int access$510(RFeatureManager x0) {
        int i = x0.mWMRetryTime;
        x0.mWMRetryTime = i - 1;
        return i;
    }

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

    public int getFeatureStatus(int featureid) {
        AwareConstant.FeatureType type = AwareConstant.FeatureType.getFeatureType(featureid);
        if (type == AwareConstant.FeatureType.FEATURE_INVALIDE_TYPE) {
            AwareLog.e(TAG, "getFeatureStatus invalid feature type");
            return 0;
        }
        FeatureWrapper wrapper = this.mRegisteredFeatures.get(type);
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

    public boolean isResourceNeeded(AwareConstant.ResourceType resourceType) {
        return this.mIAwareEnabled;
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
        for (FeatureWrapper v : this.mRegisteredFeatures.values()) {
            if (v != null && v.getFeatureInstance() != null) {
                ArrayList<DumpData> featureDumpData = v.getFeatureInstance().getDumpData(time);
                if (featureDumpData != null) {
                    int dumpDataSize = featureDumpData.size();
                    int index = 0;
                    if (dumpDataSize > 5) {
                        AwareLog.e(TAG, "RDA getDumpData more than 5 items, size = " + dumpDataSize + " , id = " + featureDumpData.get(0).getFeatureId());
                        dumpDataSize = 5;
                    }
                    while (true) {
                        int index2 = index;
                        if (index2 >= dumpDataSize) {
                            break;
                        }
                        dumpData.add(featureDumpData.get(index2));
                        index = index2 + 1;
                    }
                }
            } else {
                AwareLog.e(TAG, "getDumpData feature null!");
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
        FeatureWrapper fw = this.mRegisteredFeatures.get(AwareConstant.FeatureType.getFeatureType(featureId));
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
        FeatureWrapper featureWrapper = this.mRegisteredFeatures.get(AwareConstant.FeatureType.getFeatureType(featureId));
        RFeature feature = featureWrapper != null ? featureWrapper.getFeatureInstance() : null;
        if (feature != null) {
            return feature.getBigDataByVersion(iawareVer, beta, clear);
        }
        return null;
    }

    public String fetchDFTDataByVersion(int iawareVer, int featureId, boolean beta, boolean clear, boolean betaEncode) {
        AwareLog.d(TAG, "fetchDFTDataByVersion iVer = " + iawareVer + ", fId = " + featureId + ", beta = " + beta + ", betaEncode=" + betaEncode);
        FeatureWrapper featureWrapper = this.mRegisteredFeatures.get(AwareConstant.FeatureType.getFeatureType(featureId));
        RFeature feature = featureWrapper != null ? featureWrapper.getFeatureInstance() : null;
        if (feature != null) {
            return feature.getDFTDataByVersion(iawareVer, beta, clear, betaEncode);
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
        this.mSubscribedFeatureTypes = new AwareConstant.FeatureType[this.mSubscribedFeatureTypeNum];
        this.mParser = Xml.newPullParser();
        HwActivityManager.registerDAMonitorCallback(new AwareAmsMonitorCallback());
        this.mWmsCallback = new AwareWmsMonitorCallback();
        this.mAwsiCallback = new AwareAWSIMonitorCallback();
        this.mReportedDataHandler.post(new CallbackRegistration());
        HwPowerManager.registerPowerMonitorCallback(new AwarePowerMonitorCallback());
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
    public void registerRDABroadcastReceiver() {
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
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, userFilter, null, null);
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.ACTION_SHUTDOWN"), null, null);
    }

    public int[] getFeatureInfoFromBundle(Bundle bundle, String key) {
        int[] info = bundle.getIntArray(key);
        if (info != null && info.length == 2) {
            return info;
        }
        return new int[]{0, IAWARE_VERSION_DEFAULT};
    }

    /* access modifiers changed from: private */
    public void triggerGc(boolean cancel) {
        if (sIsMygote) {
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

    private int parseFeatureSwitchFormCustFile(int featureId) {
        InputStream is = null;
        XmlPullParser parser = null;
        try {
            File custConfigFile = loadCustFeatureSwitchFile();
            if (custConfigFile.exists()) {
                is = new FileInputStream(custConfigFile);
                parser = this.mParser;
                parser.setInput(is, StandardCharsets.UTF_8.name());
            }
            if (parser == null) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        AwareLog.e(TAG, "close file input stream fail!!");
                    }
                }
                return -1;
            }
            boolean enterItemTag = false;
            Map<String, String> configItem = null;
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                String tagName = parser.getName();
                if (eventType == 2) {
                    if (XML_TAG_ITEM.equals(tagName)) {
                        enterItemTag = true;
                        configItem = new HashMap<>();
                    } else if (tagName != null && enterItemTag) {
                        String value = parser.nextText();
                        if (configItem != null) {
                            configItem.put(tagName, value);
                        }
                    }
                } else if (eventType == 3 && XML_TAG_ITEM.equals(tagName)) {
                    AwareLog.d(TAG, "exit item");
                    enterItemTag = false;
                    if (configItem != null && configItem.get(XML_TAG_FEATURE_ID).equals(Integer.toString(featureId))) {
                        int parseInt = Integer.parseInt(configItem.get("switch"));
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e2) {
                                AwareLog.e(TAG, "close file input stream fail!!");
                            }
                        }
                        return parseInt;
                    }
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e3) {
                    AwareLog.e(TAG, "close file input stream fail!!");
                }
            }
            return -1;
        } catch (IOException ioe) {
            AwareLog.e(TAG, "read xml failed, error:" + ioe.getMessage());
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e4) {
                    AwareLog.e(TAG, "close file input stream fail!!");
                }
            }
            return -1;
        } catch (Exception e5) {
            AwareLog.e(TAG, "read xml failed, error:" + e5.getMessage());
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e6) {
                    AwareLog.e(TAG, "close file input stream fail!!");
                }
            }
            return -1;
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e7) {
                    AwareLog.e(TAG, "close file input stream fail!!");
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public File loadCustFeatureSwitchFile() {
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

package com.android.server.rms.iaware.feature;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.IAwareCMSManager;
import android.rms.iaware.IAwaredConnection;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.internal.util.MemInfoReader;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.huawei.android.app.HwActivityManager;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppQuickStartFeature extends RFeature {
    private static final String AQS_EXCTPRD_ACTNAME = "ActName";
    private static final String AQS_EXCTPRD_APPNAME = "AppName";
    private static final String AQS_EXCTPRD_COLLECT_TIMEOUT = "CollectTimeout";
    private static final String AQS_EXCTPRD_EXCLUDED_APP = "ExcludedApp";
    private static final String AQS_EXCTPRD_INCLUDED_ACT = "IncludedAct";
    private static final String AQS_EXCTPRD_LAUNCH_TIMEOUT = "LaunchTimeout";
    private static final String AQS_EXCTPRD_NAME = "ExactPreread";
    private static final String AQS_EXCTPRD_PAUSE_DELAY = "PauseDelay";
    private static final String AQS_EXCTPRD_PAUSE_TIMEOUT = "PauseTimeout";
    private static final String AQS_EXCTPRD_RAMSIZE = "ramsize";
    private static final String AQS_EXCTPRD_SWITCH = "Switch";
    private static final String AQS_EXCTPRD_TYPE = "type";
    private static final String AQS_FEATURENAME = "AppQuickStart";
    private static final int FEATURE_MIN_VERSION = 3;
    private static final String TAG = "AppQuickStart";
    private static boolean mExactPrereadFeature = false;
    private static final ArraySet<String> mExctprdExcApps = new ArraySet<>();
    private static final ArraySet<String> mExctprdIncActs = new ArraySet<>();
    private static final AtomicBoolean mRunning = new AtomicBoolean(false);
    private ExactPrereadWorkHandler mExactPrereadWorkHandler;
    private int mLastColdBootAppID = -1;
    private String mLastColdBootPkgName = null;
    private long mLastDisplayBeginTime = -1;
    private long mLastInputTime = 0;

    /* renamed from: com.android.server.rms.iaware.feature.AppQuickStartFeature$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$rms$iaware$AwareConstant$ResourceType = new int[AwareConstant.ResourceType.values().length];

        static {
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RES_APP.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RES_INPUT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_USERHABIT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_SHUTDOWN.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private final class ExactPrereadWorkHandler extends Handler {
        private static final int INVALID_ID = -1;
        private static final String INVALID_NAME = "";
        private static final int INVALID_TYPE = -1;
        private static final int MAX_LAUNCH_TIMEOUT = 20000;
        private static final int MAX_PAUSECOLLECT_TIMEOUT = 20000;
        private static final int MAX_PAUSEDELAY_TIMEOUT = 3000;
        private static final int MAX_PAUSE_TIMEOUT = 10000;
        private static final int MIN_LAUNCH_TIMEOUT = 5000;
        private static final int MSG_DELAY_PAUSE_TIMEOUT = 300;
        private static final int MSG_LAUNCH_TIMEOUT = 100;
        private static final int MSG_PAUSE_COLLECT_TIMEOUT = 400;
        private static final int MSG_PAUSE_TIMEOUT = 200;
        private int mDelayPauseTimeout = 0;
        private int mLaunchTimeout = 10000;
        private int mPauseCollectTimeout = 8000;
        private int mPauseTimeout = 1000;
        private ExactPrereadWork mWork;

        private class ExactPrereadWork {
            public int mAppId;
            public String mOwnerName;
            public int mPid;
            public int mType;

            public ExactPrereadWork(int appId, int pid, String ownerName, int type) {
                this.mAppId = appId;
                this.mPid = pid;
                this.mOwnerName = ownerName;
                this.mType = type;
            }

            public void workReset() {
                this.mAppId = -1;
                this.mPid = -1;
                this.mOwnerName = "";
                this.mType = -1;
            }
        }

        public ExactPrereadWorkHandler(Long totalMemMb) {
            ExactPrereadWork exactPrereadWork = new ExactPrereadWork(-1, -1, "", -1);
            this.mWork = exactPrereadWork;
            if (totalMemMb.longValue() > 4096) {
                this.mDelayPauseTimeout = 200;
                this.mPauseTimeout = 3000;
            } else if (totalMemMb.longValue() > 2048) {
                this.mDelayPauseTimeout = 0;
                this.mPauseTimeout = 2000;
            } else if (totalMemMb.longValue() > 1024) {
                this.mDelayPauseTimeout = 0;
                this.mPauseTimeout = 1500;
            } else {
                this.mDelayPauseTimeout = 0;
                this.mPauseTimeout = 1000;
            }
        }

        /* access modifiers changed from: private */
        public void setWorkCollectTime(int launchTimeout, int collectTimeout, int pauseTimeout, int pauseDelay) {
            if (launchTimeout >= 0) {
                if (launchTimeout < MIN_LAUNCH_TIMEOUT) {
                    this.mLaunchTimeout = MIN_LAUNCH_TIMEOUT;
                } else if (launchTimeout > 20000) {
                    this.mLaunchTimeout = 20000;
                } else {
                    this.mLaunchTimeout = launchTimeout;
                }
            }
            if (collectTimeout >= 0) {
                if (collectTimeout > 20000) {
                    this.mPauseCollectTimeout = 20000;
                } else {
                    this.mPauseCollectTimeout = collectTimeout;
                }
            }
            if (pauseTimeout >= 0) {
                if (pauseTimeout > 10000) {
                    this.mPauseTimeout = 10000;
                } else {
                    this.mPauseTimeout = pauseTimeout;
                }
            }
            if (this.mPauseTimeout > this.mPauseCollectTimeout) {
                this.mPauseTimeout = this.mPauseCollectTimeout;
            }
            if (pauseDelay >= 0) {
                if (pauseDelay > 3000) {
                    this.mDelayPauseTimeout = 3000;
                } else {
                    this.mDelayPauseTimeout = pauseDelay;
                }
            }
            if (this.mDelayPauseTimeout > this.mPauseTimeout) {
                this.mDelayPauseTimeout = this.mPauseTimeout;
            }
            AwareLog.i("AppQuickStart", "setWorkCollectTime, launchTimeout: " + this.mLaunchTimeout + ", collectTimeout: " + this.mPauseCollectTimeout + ", pauseTimeout: " + this.mPauseTimeout + ", pauseDelay: " + this.mDelayPauseTimeout);
        }

        private boolean hasValidWorkExisted() {
            if (this.mWork.mAppId == -1 || this.mWork.mPid == -1 || this.mWork.mType == -1 || this.mWork.mOwnerName.equals("")) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: private */
        public boolean hasWorkExisted(int appId, String ownerName) {
            if (this.mWork.mAppId != appId || !this.mWork.mOwnerName.equals(ownerName)) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: private */
        public boolean hasPauseWorkExisted() {
            if (this.mWork.mType == 200 || this.mWork.mType == 400) {
                return true;
            }
            return false;
        }

        /* access modifiers changed from: private */
        public void startCollectWork(int appId, int pid, String ownerName) {
            if (ownerName != null) {
                removeMessages(100);
                removeMessages(200);
                removeMessages(400);
                removeMessages(300);
                if (!hasWorkExisted(appId, ownerName)) {
                    stopCurrentWork();
                    this.mWork.mAppId = appId;
                    this.mWork.mOwnerName = ownerName;
                } else if (this.mWork.mType == 100 && this.mWork.mPid != pid) {
                    AwareLog.d("AppQuickStart", "workingset: startCollectWork, curPid: " + pid + ", oldPid: " + this.mWork.mPid);
                    AppQuickStartFeature.this.exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_PAUSE_COLLECT, 0, this.mWork.mAppId, this.mWork.mPid, this.mWork.mOwnerName);
                }
                this.mWork.mPid = pid;
                this.mWork.mType = 100;
                Message message = Message.obtain();
                message.obj = this.mWork;
                message.what = 100;
                sendMessageDelayed(message, (long) this.mLaunchTimeout);
            }
        }

        private void sendPauseMsg() {
            this.mWork.mType = 200;
            Message message = Message.obtain();
            message.what = 200;
            sendMessageDelayed(message, (long) (this.mPauseTimeout - this.mDelayPauseTimeout));
        }

        /* access modifiers changed from: private */
        public void sendDelayPauseMsg() {
            removeMessages(100);
            this.mWork.mType = 300;
            Message message = Message.obtain();
            message.what = 300;
            sendMessageDelayed(message, (long) this.mDelayPauseTimeout);
        }

        private void sendPauseCollectMsg() {
            this.mWork.mType = 400;
            Message message = Message.obtain();
            message.what = 400;
            sendMessageDelayed(message, (long) (this.mPauseCollectTimeout - this.mPauseTimeout));
        }

        /* access modifiers changed from: private */
        public void stopCurrentWork() {
            if (hasValidWorkExisted()) {
                AppQuickStartFeature.this.exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_STOP, 0, this.mWork.mAppId, this.mWork.mPid, this.mWork.mOwnerName);
                this.mWork.workReset();
                removeMessages(100);
                removeMessages(200);
                removeMessages(400);
                removeMessages(300);
            }
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int i = msg.what;
            if (i == 100) {
                AppQuickStartFeature.this.exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_ABORT, 0, this.mWork.mAppId, this.mWork.mPid, this.mWork.mOwnerName);
                this.mWork.workReset();
            } else if (i == 200) {
                sendPauseCollectMsg();
                AppQuickStartFeature.this.exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_PAUSE_COLLECT, 0, this.mWork.mAppId, this.mWork.mPid, this.mWork.mOwnerName);
            } else if (i == 300) {
                sendPauseMsg();
                AppQuickStartFeature.this.exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_PAUSE, 0, this.mWork.mAppId, this.mWork.mPid, this.mWork.mOwnerName);
            } else if (i == 400) {
                AppQuickStartFeature.this.exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_STOP, 0, this.mWork.mAppId, this.mWork.mPid, this.mWork.mOwnerName);
                this.mWork.workReset();
            }
        }
    }

    public AppQuickStartFeature(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        MemInfoReader minfo = new MemInfoReader();
        minfo.readMemInfo();
        Long totalMemMb = Long.valueOf(minfo.getTotalSize() / MemoryConstant.MB_SIZE);
        this.mExactPrereadWorkHandler = new ExactPrereadWorkHandler(totalMemMb);
        loadExctprdConfig(false, totalMemMb);
        loadExctprdConfig(true, totalMemMb);
    }

    public boolean enable() {
        AwareLog.i("AppQuickStart", "AppQuickStartFeature is a iaware3.0 feature, don't allow enable!");
        return false;
    }

    public boolean enableFeatureEx(int realVersion) {
        if (realVersion < 3) {
            AwareLog.i("AppQuickStart", "the min version of AppQuickStartFeature is 3, but current version is " + realVersion + ", don't allow enable!");
            return false;
        }
        AwareLog.i("AppQuickStart", "AppQuickStartFeature enabled");
        subscribleEvents();
        enableAppQuickStart();
        return true;
    }

    public boolean disable() {
        AwareLog.i("AppQuickStart", "AppQuickStartFeature disabled");
        unSubscribeEvents();
        disableAppQuickStart();
        return true;
    }

    private static void enableAppQuickStart() {
        mRunning.set(true);
    }

    private static void disableAppQuickStart() {
        mRunning.set(false);
    }

    public boolean reportData(CollectData data) {
        boolean ret = false;
        if (!mRunning.get() || data == null) {
            AwareLog.e("AppQuickStart", "DataDispatch not start");
            return false;
        } else if (!mExactPrereadFeature) {
            return false;
        } else {
            long timestamp = data.getTimeStamp();
            switch (AnonymousClass1.$SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.getResourceType(data.getResId()).ordinal()]) {
                case 1:
                    AttrSegments attrSegments = parseCollectData(data);
                    if (attrSegments.isValid()) {
                        ret = appDataHandle(attrSegments.getEvent().intValue(), attrSegments);
                        break;
                    }
                    break;
                case 2:
                    AttrSegments attrSegments2 = parseCollectData(data);
                    if (attrSegments2.isValid()) {
                        ret = inputDataHandle(timestamp, attrSegments2.getEvent().intValue(), attrSegments2);
                        break;
                    }
                    break;
                case 3:
                    Bundle bundle = data.getBundle();
                    if (bundle != null && 2 == bundle.getInt(AwareUserHabit.USERHABIT_INSTALL_APP_UPDATE)) {
                        String pkgName = bundle.getString(AwareUserHabit.USERHABIT_PACKAGE_NAME);
                        if (pkgName != null) {
                            clearRecordsOfPackage(UserHandle.getAppId(bundle.getInt("uid")), pkgName);
                        }
                        ret = true;
                        break;
                    }
                case 4:
                    exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_BACKUP, 0, 0, 0, "All");
                    ret = true;
                    break;
                default:
                    AwareLog.e("AppQuickStart", "Invalid ResourceType");
                    break;
            }
            return ret;
        }
    }

    private boolean appDataHandle(int event, AttrSegments attrSegments) {
        boolean ret = false;
        ArrayMap<String, String> appInfo = attrSegments.getSegment("calledApp");
        if (appInfo == null) {
            AwareLog.w("AppQuickStart", "appInfo is NULL");
            return false;
        }
        if (event == 15001) {
            ret = handleProcessLaunchBegin(appInfo);
        } else if (event == 15005) {
            ret = handleActivityBegin(appInfo);
        } else if (event == 15013) {
            ret = handleDisplayedBegin(appInfo);
        } else if (event == 85001) {
            ret = handleProcessLaunchFinish(appInfo);
        } else if (event == 85013) {
            ret = handleDisplayedFinish(appInfo);
        }
        return ret;
    }

    private AwareConfig getConfig(String featureName, String configName, boolean isCustConfig) {
        AwareConfig configList = null;
        if (featureName == null || featureName.equals("") || configName == null || configName.equals("")) {
            AwareLog.w("AppQuickStart", "featureName or configName is null");
            return null;
        }
        try {
            IBinder awareservice = IAwareCMSManager.getICMSManager();
            if (awareservice != null) {
                configList = isCustConfig ? IAwareCMSManager.getCustConfig(awareservice, featureName, configName) : IAwareCMSManager.getConfig(awareservice, featureName, configName);
            } else {
                AwareLog.w("AppQuickStart", "can not find service awareservice.");
            }
        } catch (RemoteException e) {
            AwareLog.e("AppQuickStart", "getConfig RemoteException!");
        }
        return configList;
    }

    private boolean checkRamSize(String ramSize, Long totalMemMb) {
        if (ramSize == null) {
            return false;
        }
        try {
            long ramSizeL = Long.parseLong(ramSize.trim());
            if (totalMemMb.longValue() > ramSizeL || totalMemMb.longValue() <= ramSizeL - 1024) {
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            AwareLog.e("AppQuickStart", "parse ramsize error!");
            return false;
        }
    }

    private void loadExctprdConfig(boolean isCustConfig, Long totalMemMb) {
        AwareLog.i("AppQuickStart", "loadExctprdConfig isCustConfig = " + isCustConfig);
        AwareConfig configList = getConfig("AppQuickStart", AQS_EXCTPRD_NAME, isCustConfig);
        if (configList == null) {
            AwareLog.w("AppQuickStart", "loadExctprdConfig failure, configList is null!");
            return;
        }
        for (AwareConfig.Item item : configList.getConfigList()) {
            if (item == null || item.getProperties() == null) {
                AwareLog.w("AppQuickStart", "loadExctprdConfig skip a item because it is null!");
            } else {
                Map<String, String> configPropertries = item.getProperties();
                String typeName = configPropertries.get("type");
                if (typeName != null) {
                    char c = 65535;
                    int hashCode = typeName.hashCode();
                    if (hashCode != -1805606060) {
                        if (hashCode != -1423802409) {
                            if (hashCode == 1943461398 && typeName.equals(AQS_EXCTPRD_INCLUDED_ACT)) {
                                c = 2;
                            }
                        } else if (typeName.equals(AQS_EXCTPRD_EXCLUDED_APP)) {
                            c = 1;
                        }
                    } else if (typeName.equals(AQS_EXCTPRD_SWITCH)) {
                        c = 0;
                    }
                    switch (c) {
                        case 0:
                            applyExctprdSwitchConfig(item);
                            break;
                        case 1:
                            applyExctprdExcludedAppsConfig(item);
                            break;
                        case 2:
                            applyExctprdIncludedActsConfig(item);
                            break;
                    }
                } else {
                    String ramSize = configPropertries.get("ramsize");
                    if (checkRamSize(ramSize, totalMemMb)) {
                        applyExctprdCollectTimeConfig(item);
                    } else {
                        AwareLog.d("AppQuickStart", "checkRamSize failure! ramSize: " + ramSize + " totalMemMb: " + totalMemMb);
                    }
                }
            }
        }
    }

    private static void applyExctprdSwitchConfig(AwareConfig.Item item) {
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (!(itemName == null || itemValue == null)) {
                char c = 65535;
                try {
                    if (itemName.hashCode() == -1805606060) {
                        if (itemName.equals(AQS_EXCTPRD_SWITCH)) {
                            c = 0;
                        }
                    }
                    if (c != 0) {
                        AwareLog.w("AppQuickStart", "applyExctprdSwitchConfig no such configuration. " + itemName);
                    } else {
                        boolean z = true;
                        if (Integer.parseInt(itemValue.trim()) != 1) {
                            z = false;
                        }
                        mExactPrereadFeature = z;
                        AwareLog.i("AppQuickStart", "applyExctprdSwitchConfig Switch = " + itemValue);
                    }
                } catch (NumberFormatException e) {
                    AwareLog.e("AppQuickStart", "parse applyExctprdSwitchConfig error!");
                }
            }
        }
    }

    private static void applyExctprdExcludedAppsConfig(AwareConfig.Item item) {
        mExctprdExcApps.clear();
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (!(itemName == null || itemValue == null)) {
                char c = 65535;
                try {
                    if (itemName.hashCode() == 870516780) {
                        if (itemName.equals(AQS_EXCTPRD_APPNAME)) {
                            c = 0;
                        }
                    }
                    if (c != 0) {
                        AwareLog.w("AppQuickStart", "applyExctprdExcludedAppsConfig no such configuration." + itemName);
                    } else {
                        mExctprdExcApps.add(itemValue.trim());
                        AwareLog.i("AppQuickStart", "applyExctprdExcludedAppsConfig AppName = " + itemValue);
                    }
                } catch (NumberFormatException e) {
                    AwareLog.e("AppQuickStart", "parse applyExctprdExcludedAppsConfig error!");
                }
            }
        }
    }

    private static void applyExctprdIncludedActsConfig(AwareConfig.Item item) {
        mExctprdIncActs.clear();
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (!(itemName == null || itemValue == null)) {
                char c = 65535;
                try {
                    if (itemName.hashCode() == 502031901) {
                        if (itemName.equals(AQS_EXCTPRD_ACTNAME)) {
                            c = 0;
                        }
                    }
                    if (c != 0) {
                        AwareLog.w("AppQuickStart", "applyExctprdIncludedActsConfig no such configuration. " + itemName);
                    } else {
                        mExctprdIncActs.add(itemValue.trim());
                        AwareLog.i("AppQuickStart", "applyExctprdIncludedActsConfig ActName = " + itemValue);
                    }
                } catch (NumberFormatException e) {
                    AwareLog.e("AppQuickStart", "parse applyExctprdIncludedActsConfig error!");
                }
            }
        }
    }

    private void applyExctprdCollectTimeConfig(AwareConfig.Item item) {
        int launchTimeout = -1;
        int collectTimeout = -1;
        int pauseTimeout = -1;
        int delayPause = -1;
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (itemName != null && itemValue != null) {
                char c = 65535;
                try {
                    int hashCode = itemName.hashCode();
                    if (hashCode != 590910123) {
                        if (hashCode != 1545794062) {
                            if (hashCode != 1920911693) {
                                if (hashCode == 1974045943) {
                                    if (itemName.equals(AQS_EXCTPRD_COLLECT_TIMEOUT)) {
                                        c = 2;
                                    }
                                }
                            } else if (itemName.equals(AQS_EXCTPRD_PAUSE_DELAY)) {
                                c = 0;
                            }
                        } else if (itemName.equals(AQS_EXCTPRD_LAUNCH_TIMEOUT)) {
                            c = 3;
                        }
                    } else if (itemName.equals(AQS_EXCTPRD_PAUSE_TIMEOUT)) {
                        c = 1;
                    }
                    switch (c) {
                        case 0:
                            delayPause = Integer.parseInt(itemValue.trim());
                            AwareLog.i("AppQuickStart", "applyExctprdCollectTimeConfig PauseDelay = " + delayPause);
                            break;
                        case 1:
                            pauseTimeout = Integer.parseInt(itemValue.trim());
                            if (delayPause <= pauseTimeout) {
                                AwareLog.i("AppQuickStart", "applyExctprdCollectTimeConfig PauseTimeout = " + pauseTimeout);
                                break;
                            } else {
                                AwareLog.w("AppQuickStart", "applyExctprdCollectTimeConfig invalid parameters: PauseDelay=" + delayPause + ", " + AQS_EXCTPRD_PAUSE_TIMEOUT + "=" + pauseTimeout);
                                return;
                            }
                        case 2:
                            collectTimeout = Integer.parseInt(itemValue.trim());
                            if (pauseTimeout <= collectTimeout) {
                                if (delayPause <= collectTimeout) {
                                    AwareLog.i("AppQuickStart", "applyExctprdCollectTimeConfig CollectTimeout = " + collectTimeout);
                                    break;
                                }
                            }
                            AwareLog.w("AppQuickStart", "applyExctprdCollectTimeConfig invalid parameters: PauseDelay=" + delayPause + ", " + AQS_EXCTPRD_PAUSE_TIMEOUT + "=" + pauseTimeout + ", " + AQS_EXCTPRD_COLLECT_TIMEOUT + "=" + collectTimeout);
                            return;
                        case 3:
                            launchTimeout = Integer.parseInt(itemValue.trim());
                            AwareLog.i("AppQuickStart", "applyExctprdCollectTimeConfig LaunchTimeout = " + launchTimeout);
                            break;
                        default:
                            AwareLog.w("AppQuickStart", "applyExctprdCollectTimeConfig no such configuration. " + itemName);
                            break;
                    }
                } catch (NumberFormatException e) {
                    AwareLog.e("AppQuickStart", "parse applyExctprdCollectTimeConfig error!");
                }
            } else {
                return;
            }
        }
        this.mExactPrereadWorkHandler.setWorkCollectTime(launchTimeout, collectTimeout, pauseTimeout, delayPause);
    }

    private void clearRecordsOfPackage(int appId, String packageName) {
        exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_CLEAN, 0, appId, 0, packageName);
        int actSize = mExctprdIncActs.size();
        for (int idx = 0; idx < actSize; idx++) {
            String actName = mExctprdIncActs.valueAt(idx);
            if (actName != null) {
                ComponentName cpName = ComponentName.unflattenFromString(actName);
                if (cpName != null && packageName.equals(cpName.getPackageName())) {
                    exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_CLEAN, 0, appId, 0, actName.replace('/', '.'));
                }
            }
        }
    }

    private boolean handleActivityBegin(ArrayMap<String, String> appInfo) {
        String appName = appInfo.get("packageName");
        String activityName = appInfo.get("activityName");
        String processName = appInfo.get("processName");
        try {
            int uid = Integer.parseInt(appInfo.get("uid"));
            try {
                int appId = UserHandle.getAppId(uid);
                tryStopPreviousOwnerWorks(appId, appName);
                if (appName == null) {
                    return false;
                }
                if ((!HwActivityManager.isProcessExistPidsSelfLocked(processName, uid)) && !mExctprdExcApps.contains(appName)) {
                    AwareLog.d("AppQuickStart", "workingset: handleActivityBegin, appId: " + appId + ", pkgName: " + appName + ", className: " + activityName);
                    exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_PREREAD, 1, appId, 0, appName);
                    this.mLastColdBootAppID = appId;
                    this.mLastColdBootPkgName = appName;
                }
                return true;
            } catch (NumberFormatException e) {
                NumberFormatException numberFormatException = e;
                int i = uid;
                AwareLog.e("AppQuickStart", "uid is not right");
                return false;
            }
        } catch (NumberFormatException e2) {
            AwareLog.e("AppQuickStart", "uid is not right");
            return false;
        }
    }

    private boolean handleProcessLaunchBegin(ArrayMap<String, String> appInfo) {
        if ("activity".equals(appInfo.get("launchMode"))) {
            try {
                String packageName = appInfo.get("packageName");
                int appId = UserHandle.getAppId(Integer.parseInt(appInfo.get("uid")));
                tryStopPreviousOwnerWorks(appId, packageName);
                if (packageName != null && !mExctprdExcApps.contains(packageName)) {
                    AwareLog.d("AppQuickStart", "workingset: handleProcessLaunchBegin, appId: " + appId + ", packageName: " + packageName);
                    exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_PREREAD, 1, appId, 0, packageName);
                    this.mLastColdBootAppID = appId;
                    this.mLastColdBootPkgName = packageName;
                }
            } catch (NumberFormatException e) {
                AwareLog.e("AppQuickStart", "handleProcessLaunchBegin get info failed!");
                return false;
            }
        }
        return true;
    }

    private boolean handleProcessLaunchFinish(ArrayMap<String, String> appInfo) {
        if ("activity".equals(appInfo.get("launchMode"))) {
            try {
                String packageName = appInfo.get("packageName");
                int uid = Integer.parseInt(appInfo.get("uid"));
                int pid = Integer.parseInt(appInfo.get("pid"));
                int appId = UserHandle.getAppId(uid);
                tryStopPreviousOwnerWorks(appId, packageName);
                if (packageName != null && !mExctprdExcApps.contains(packageName)) {
                    AwareLog.d("AppQuickStart", "workingset: handleProcessLaunchFinish, appId: " + appId + ", pid: " + pid + ", packageName: " + packageName);
                    this.mExactPrereadWorkHandler.startCollectWork(appId, pid, packageName);
                    exactPrereadSendCmd(350, 1, appId, pid, packageName);
                    this.mLastColdBootAppID = appId;
                    this.mLastColdBootPkgName = packageName;
                    this.mLastDisplayBeginTime = SystemClock.uptimeMillis();
                }
            } catch (NumberFormatException e) {
                AwareLog.e("AppQuickStart", "handleProcessLaunchFinish get info failed!");
                return false;
            }
        }
        return true;
    }

    private void tryStopPreviousOwnerWorks(int appId, String appName) {
        if (this.mLastColdBootAppID != -1 && this.mLastColdBootPkgName != null) {
            if (this.mLastInputTime > this.mLastDisplayBeginTime || appId != this.mLastColdBootAppID || !this.mLastColdBootPkgName.equals(appName)) {
                this.mExactPrereadWorkHandler.stopCurrentWork();
                this.mLastColdBootAppID = -1;
                this.mLastColdBootPkgName = null;
            }
        }
    }

    private boolean handleDisplayedBegin(ArrayMap<String, String> appInfo) {
        try {
            String activityName = appInfo.get("activityName");
            String packageName = null;
            if (activityName != null) {
                ComponentName cpName = ComponentName.unflattenFromString(activityName);
                if (cpName != null) {
                    packageName = cpName.getPackageName();
                }
            }
            int uid = Integer.parseInt(appInfo.get("uid"));
            int pid = Integer.parseInt(appInfo.get("pid"));
            int appId = UserHandle.getAppId(uid);
            if (activityName == null || packageName == null) {
                return false;
            }
            AwareLog.d("AppQuickStart", "workingset: handleDisplayedBegin, appId: " + appId + ", pid: " + pid + ", activityName: " + activityName);
            if (this.mExactPrereadWorkHandler.hasWorkExisted(appId, packageName)) {
                if (this.mExactPrereadWorkHandler.hasPauseWorkExisted()) {
                    exactPrereadSendCmd(350, 1, appId, pid, packageName);
                }
                this.mExactPrereadWorkHandler.startCollectWork(appId, pid, packageName);
                this.mLastDisplayBeginTime = SystemClock.uptimeMillis();
            } else if (mExctprdIncActs.contains(activityName)) {
                String changedActivityName = activityName.replace('/', '.');
                this.mExactPrereadWorkHandler.startCollectWork(appId, pid, changedActivityName);
                exactPrereadSendCmd(350, 0, appId, pid, changedActivityName);
            }
            return true;
        } catch (NumberFormatException e) {
            AwareLog.e("AppQuickStart", "handleDisplayedBegin get pid or time failed");
            return false;
        }
    }

    private boolean handleDisplayedFinish(ArrayMap<String, String> appInfo) {
        try {
            String activityName = appInfo.get("activityName");
            String packageName = null;
            if (activityName != null) {
                ComponentName cpName = ComponentName.unflattenFromString(activityName);
                if (cpName != null) {
                    packageName = cpName.getPackageName();
                }
            }
            int uid = Integer.parseInt(appInfo.get("uid"));
            int pid = Integer.parseInt(appInfo.get("pid"));
            int appId = UserHandle.getAppId(uid);
            if (activityName == null || packageName == null) {
                return false;
            }
            AwareLog.d("AppQuickStart", "workingset: handleDisplayedFinish, appId: " + appId + ", pid: " + pid + ", activityName: " + activityName);
            if (this.mExactPrereadWorkHandler.hasWorkExisted(appId, packageName)) {
                this.mExactPrereadWorkHandler.sendDelayPauseMsg();
            } else {
                if (this.mExactPrereadWorkHandler.hasWorkExisted(appId, activityName.replace('/', '.'))) {
                    this.mExactPrereadWorkHandler.sendDelayPauseMsg();
                }
            }
            return true;
        } catch (NumberFormatException e) {
            AwareLog.e("AppQuickStart", "handleDisplayedFinish get pid or time failed");
            return false;
        }
    }

    private boolean inputDataHandle(long timestamp, int event, AttrSegments attrSegments) {
        if (event == 10001 || event == 80001) {
            this.mLastInputTime = timestamp;
            return true;
        }
        AwareLog.w("AppQuickStart", "Input event invalid");
        return false;
    }

    private AttrSegments parseCollectData(CollectData data) {
        String eventData = data.getData();
        AttrSegments.Builder builder = new AttrSegments.Builder();
        builder.addCollectData(eventData);
        return builder.build();
    }

    public boolean configUpdate() {
        return true;
    }

    private void subscribleEvents() {
        if (this.mIRDataRegister != null) {
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RES_APP, this.mFeatureType);
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RES_DEV_STATUS, this.mFeatureType);
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RES_INPUT, this.mFeatureType);
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_USERHABIT, this.mFeatureType);
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SHUTDOWN, this.mFeatureType);
        }
    }

    private void unSubscribeEvents() {
        if (this.mIRDataRegister != null) {
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RES_APP, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RES_DEV_STATUS, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RES_INPUT, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_USERHABIT, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SHUTDOWN, this.mFeatureType);
        }
    }

    public static boolean isExactPrereadFeatureEnable() {
        return mExactPrereadFeature && mRunning.get();
    }

    /* access modifiers changed from: private */
    public void exactPrereadSendCmd(int action, int coldboot, int appId, int pid, String ownerName) {
        AwareLog.d("AppQuickStart", "workingset, action: " + action + ", coldboot:" + coldboot + ", appId: " + appId + ", pid: " + pid + ", " + ownerName);
        try {
            byte[] nameBytes = ownerName.getBytes("UTF-8");
            if (nameBytes.length <= 0 || nameBytes.length > 256) {
                AwareLog.w("AppQuickStart", "ComponentName is invalid!");
                return;
            }
            ByteBuffer buffer = ByteBuffer.allocate(16 + nameBytes.length);
            buffer.putInt(action);
            buffer.putInt(coldboot);
            buffer.putInt(appId);
            buffer.putInt(pid);
            buffer.put(nameBytes);
            IAwaredConnection.getInstance().sendPacket(buffer.array());
        } catch (UnsupportedEncodingException e) {
            AwareLog.w("AppQuickStart", "UnsupportedEncodingException: transform ComponentName failed!");
        }
    }
}

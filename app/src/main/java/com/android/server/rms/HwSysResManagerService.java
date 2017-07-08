package com.android.server.rms;

import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.app.mtm.MultiTaskPolicy;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.IContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.MessageQueue;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.rms.HwSysResource;
import android.rms.IHwSysResManager.Stub;
import android.rms.IProcessStateChangeObserver;
import android.rms.IUpdateWhiteListCallback;
import android.rms.config.ResourceConfig;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.CollectData;
import android.rms.iaware.DumpData;
import android.rms.iaware.IReportDataCallback;
import android.rms.iaware.LogIAware;
import android.rms.iaware.RPolicyData;
import android.rms.iaware.StatisticsData;
import android.rms.resource.PidsResource;
import android.util.Log;
import android.util.Slog;
import com.android.internal.os.SomeArgs;
import com.android.server.SystemService;
import com.android.server.location.HwGpsPowerTracker;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.collector.ProcMemInfoReader;
import com.android.server.rms.config.HwConfigReader;
import com.android.server.rms.handler.HwSysResHandler;
import com.android.server.rms.handler.ResourceDispatcher;
import com.android.server.rms.iaware.RDAService;
import com.android.server.rms.iaware.RDMEDispatcher;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.io.IOStatsService;
import com.android.server.rms.record.AppUsageFileRecord;
import com.android.server.rms.record.EventParser;
import com.android.server.rms.record.ResourceOverloadRecord;
import com.android.server.rms.record.ResourceRecordStore;
import com.android.server.rms.record.ResourceUtils;
import com.android.server.rms.resource.HwSysInnerResImpl;
import com.android.server.rms.statistic.HwStatisticCtl;
import com.android.server.rms.test.TestCase;
import com.android.server.rms.utils.Utils;
import com.android.server.security.trustcircle.IOTController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HwSysResManagerService extends SystemService {
    private static final int MSG_CLEAN_MAP = 3;
    private static final int MSG_DISPATCH_PROCESS_LAUNCHER = 8;
    private static final int MSG_FLINER_DATA = 9;
    private static final int MSG_INIT = 1;
    private static final int MSG_IO_SERVICE = 6;
    private static final int MSG_NOTIFY_STATUS = 4;
    private static final int MSG_REPORT_STATUS = 2;
    private static final int MSG_SAMPLE = 5;
    private static final int MSG_WHITENAME_FILE_UPDATE = 7;
    private static final int PIDS_MONITOR_MASK = 1;
    private static final int SURFACEFLINGER_TRANSE_CODE = 2001;
    private static final String TAG = "RMS.HwSysResManagerService";
    private static boolean enableIaware;
    private static boolean enableRms;
    private static HwSysResManagerService mSelf;
    private AppUsageFileRecord mAppUsageFile;
    private final AtomicBoolean mCloudFileUpdate;
    private HwConfigReader mConfig;
    private final Context mContext;
    private final Handler mHandler;
    private Callback mHandlerCallback;
    private IOStatsService mIOStatsService;
    private final ProcMemInfoReader mProcMemInfoReader;
    private IProcessObserver mProcessObserver;
    private RemoteCallbackList<IProcessStateChangeObserver> mProcessStateChangeObserver;
    private long mPtr;
    private RDAService mRDAService;
    private BroadcastReceiver mReceiver;
    private RemoteCallbackList<IUpdateWhiteListCallback> mRegisteredResourceCallback;
    private ResourceRecordStore mResourceRecordStore;
    private final IBinder mService;
    private boolean mServiceReady;
    private HwStatisticCtl mStatisticCtl;
    private HashMap<String, Long> mThirdAppOverloadRecord;
    private int mThirdAppUploadInterval;

    private native long nativeInit(HwSysResManagerService hwSysResManagerService, MessageQueue messageQueue, int i);

    static {
        enableRms = SystemProperties.getBoolean("ro.config.enable_rms", false);
        enableIaware = SystemProperties.getBoolean("persist.sys.enable_iaware", false);
    }

    public HwSysResManagerService(Context context) {
        super(context);
        this.mThirdAppOverloadRecord = new HashMap();
        this.mResourceRecordStore = new ResourceRecordStore();
        this.mAppUsageFile = null;
        this.mServiceReady = false;
        this.mIOStatsService = null;
        this.mRDAService = null;
        this.mRegisteredResourceCallback = new RemoteCallbackList();
        this.mCloudFileUpdate = new AtomicBoolean(false);
        this.mProcMemInfoReader = new ProcMemInfoReader();
        this.mProcessStateChangeObserver = new RemoteCallbackList();
        this.mHandlerCallback = new Callback() {
            public boolean handleMessage(Message msg) {
                boolean statisticDisable = false;
                if (Utils.DEBUG) {
                    Log.d(HwSysResManagerService.TAG, "handleMessage /" + msg.what);
                }
                switch (msg.what) {
                    case HwSysResManagerService.PIDS_MONITOR_MASK /*1*/:
                        HwSysResManagerService.this.handleInitService();
                        return true;
                    case HwSysResManagerService.MSG_REPORT_STATUS /*2*/:
                        HwSysResManagerService.this.mResourceRecordStore.recordResourceOverloadStatus(msg);
                        return true;
                    case HwSysResManagerService.MSG_CLEAN_MAP /*3*/:
                        HwSysResManagerService.this.mResourceRecordStore.cleanResourceRecordMap(msg);
                        return true;
                    case HwSysResManagerService.MSG_NOTIFY_STATUS /*4*/:
                        HwSysResManagerService.this.mResourceRecordStore.notifyResourceStatus(msg);
                        return true;
                    case HwSysResManagerService.MSG_SAMPLE /*5*/:
                        if ((Utils.RMSVERSION & HwSysResManagerService.PIDS_MONITOR_MASK) == 0 && (Utils.RMSVERSION & HwSysResManagerService.MSG_REPORT_STATUS) == 0) {
                            statisticDisable = true;
                        }
                        if (!statisticDisable) {
                            int baseSamplePeriod = HwSysResManagerService.this.mConfig.getSampleBasePeriod();
                            if (HwSysResManagerService.this.mStatisticCtl != null && baseSamplePeriod > 0) {
                                HwSysResManagerService.this.mStatisticCtl.statisticGroups();
                                HwSysResManagerService.this.mHandler.sendEmptyMessageDelayed(HwSysResManagerService.MSG_SAMPLE, (long) baseSamplePeriod);
                            }
                        }
                        return true;
                    case HwSysResManagerService.MSG_IO_SERVICE /*6*/:
                        if (HwSysResManagerService.enableRms && (Utils.RMSVERSION & HwSysResManagerService.MSG_DISPATCH_PROCESS_LAUNCHER) != 0) {
                            HwSysResManagerService.this.mIOStatsService.startIOStatsService();
                        }
                        return true;
                    case HwSysResManagerService.MSG_WHITENAME_FILE_UPDATE /*7*/:
                        HwSysResManagerService.this.mCloudFileUpdate.getAndSet(HwSysResManagerService.this.mConfig.updateResConfig());
                        return true;
                    case HwSysResManagerService.MSG_DISPATCH_PROCESS_LAUNCHER /*8*/:
                        HwSysResManagerService.this.handleDispatchMessage(msg);
                        return true;
                    case HwSysResManagerService.MSG_FLINER_DATA /*9*/:
                        LogIAware.report(2045, String.valueOf(msg.arg1));
                        return true;
                    default:
                        return false;
                }
            }
        };
        this.mService = new Stub() {
            public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
                int resIDGestureScroll;
                Bundle bundleArgs;
                long curtime;
                switch (code) {
                    case HwSysResManagerService.SURFACEFLINGER_TRANSE_CODE /*2001*/:
                        data.enforceInterface("android.rms.IHwSysResManager");
                        reportExceptFrame(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 15007:
                        data.enforceInterface("android.rms.IHwSysResManager");
                        resIDGestureScroll = ResourceType.getReousrceId(ResourceType.RESOURCE_SCENE_REC);
                        bundleArgs = new Bundle();
                        curtime = System.currentTimeMillis();
                        bundleArgs.putInt("relationType", 13);
                        reportData(new CollectData(resIDGestureScroll, curtime, bundleArgs));
                        return true;
                    case 15009:
                        data.enforceInterface("android.rms.IHwSysResManager");
                        int duration = data.readInt();
                        resIDGestureScroll = ResourceType.getReousrceId(ResourceType.RESOURCE_SCENE_REC);
                        bundleArgs = new Bundle();
                        curtime = System.currentTimeMillis();
                        bundleArgs.putInt("relationType", 15);
                        bundleArgs.putInt("scroll_duration", duration);
                        reportData(new CollectData(resIDGestureScroll, curtime, bundleArgs));
                        return true;
                    case 85007:
                        data.enforceInterface("android.rms.IHwSysResManager");
                        resIDGestureScroll = ResourceType.getReousrceId(ResourceType.RESOURCE_SCENE_REC);
                        bundleArgs = new Bundle();
                        curtime = System.currentTimeMillis();
                        bundleArgs.putInt("relationType", 14);
                        reportData(new CollectData(resIDGestureScroll, curtime, bundleArgs));
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            }

            public ResourceConfig[] getResourceConfig(int resourceType) throws RemoteException {
                if (!HwSysResManagerService.this.isServiceReady()) {
                    return null;
                }
                int subTypeNum = HwSysResManagerService.this.mConfig.getSubTypeNum(resourceType);
                ResourceConfig[] config = null;
                if (subTypeNum > 0) {
                    config = new ResourceConfig[subTypeNum];
                }
                if (config != null) {
                    for (int i = 0; i < subTypeNum; i += HwSysResManagerService.PIDS_MONITOR_MASK) {
                        config[i] = HwSysResManagerService.this.mConfig.getResConfig(resourceType, i);
                    }
                }
                return config;
            }

            public boolean registerResourceUpdateCallback(IUpdateWhiteListCallback updateCallback) throws RemoteException {
                if (!HwSysResManagerService.this.isServiceReady()) {
                    return false;
                }
                if (ResourceUtils.checkSysProcPermission(Binder.getCallingPid(), Binder.getCallingUid()) != 0) {
                    Log.e(HwSysResManagerService.TAG, "Register resource update callback Process Permission error!");
                    return false;
                }
                synchronized (HwSysResManagerService.this.mRegisteredResourceCallback) {
                    HwSysResManagerService.this.mRegisteredResourceCallback.register(updateCallback);
                }
                return true;
            }

            public List<String> getIAwareProtectList(int num) {
                if (!HwSysResManagerService.this.checkServiceReadyAndPermission()) {
                    return null;
                }
                AwareUserHabit habit = AwareUserHabit.getInstance();
                if (habit != null && habit.isEnable()) {
                    return habit.getForceProtectApps(num);
                }
                if (Utils.DEBUG) {
                    Log.d(HwSysResManagerService.TAG, "habit is null or habit is disable");
                }
                return null;
            }

            public String getWhiteList(int resourceType, int type) throws RemoteException {
                if (HwSysResManagerService.this.checkPermissionForWhiteList(resourceType)) {
                    return HwSysResManagerService.this.mConfig.getWhiteList(resourceType, type);
                }
                return null;
            }

            public void triggerUpdateWhiteList() throws RemoteException {
                if (HwSysResManagerService.this.checkServiceReadyAndPermission()) {
                    HwSysResManagerService.this.updateWhiteList();
                }
            }

            public List<String> getLongTimeRunningApps() {
                if (!HwSysResManagerService.this.checkServiceReadyAndPermission()) {
                    return null;
                }
                List<String> list = null;
                AwareUserHabit habit = AwareUserHabit.getInstance();
                if (habit != null && habit.isEnable()) {
                    list = habit.recognizeLongTimeRunningApps();
                }
                return list;
            }

            public List<String> getMostFrequentUsedApps(int n, int minCount) {
                if (!HwSysResManagerService.this.checkPermissionForMultiUserMode()) {
                    return null;
                }
                List<String> list = null;
                AwareUserHabit habit = AwareUserHabit.getInstance();
                if (habit != null && habit.isEnable()) {
                    list = habit.getMostFrequentUsedApp(n, minCount);
                }
                return list;
            }

            public void recordResourceOverloadStatus(int uid, String pkg, int resourceType, int speedOverloadNum, int speedOverLoadPeroid, int countOverLoadNum) throws RemoteException {
                if (HwSysResManagerService.this.isServiceReady()) {
                    String newPkgInfo = pkg + "_" + speedOverloadNum;
                    Message msg = HwSysResManagerService.this.mHandler.obtainMessage();
                    msg.what = HwSysResManagerService.MSG_REPORT_STATUS;
                    msg.arg1 = uid;
                    msg.arg2 = resourceType;
                    SomeArgs args = SomeArgs.obtain();
                    if (resourceType != 19) {
                        newPkgInfo = pkg;
                    }
                    args.arg1 = newPkgInfo;
                    if (resourceType == 19) {
                        speedOverloadNum = HwSysResManagerService.this.mAppUsageFile.getUsageTimeforUpload(pkg);
                    }
                    args.argi1 = speedOverloadNum;
                    args.argi2 = speedOverLoadPeroid;
                    args.argi3 = countOverLoadNum;
                    msg.obj = args;
                    HwSysResManagerService.this.mHandler.sendMessage(msg);
                }
            }

            public void clearResourceStatus(int uid, int resourceType) throws RemoteException {
                if (!HwSysResManagerService.this.isServiceReady()) {
                    return;
                }
                if (ResourceUtils.checkSysProcPermission(Binder.getCallingPid(), Binder.getCallingUid()) != 0) {
                    Log.e(HwSysResManagerService.TAG, "Process Permission error!");
                    return;
                }
                Message msg = HwSysResManagerService.this.mHandler.obtainMessage();
                msg.what = HwSysResManagerService.MSG_CLEAN_MAP;
                msg.arg1 = uid;
                msg.arg2 = resourceType;
                HwSysResManagerService.this.mHandler.sendMessage(msg);
            }

            public void notifyResourceStatus(int resourceType, String resourceName, int resourceStatus, Bundle bd) throws RemoteException {
                if (!HwSysResManagerService.this.isServiceReady()) {
                    return;
                }
                if (ResourceUtils.checkSysProcPermission(Binder.getCallingPid(), Binder.getCallingUid()) != 0) {
                    Log.e(HwSysResManagerService.TAG, "Process Permission error!");
                    return;
                }
                Message msg = HwSysResManagerService.this.mHandler.obtainMessage();
                msg.what = HwSysResManagerService.MSG_NOTIFY_STATUS;
                msg.arg1 = resourceType;
                msg.arg2 = resourceStatus;
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = resourceName;
                args.arg2 = bd;
                msg.obj = args;
                HwSysResManagerService.this.mHandler.sendMessage(msg);
            }

            public void dispatch(int resourceType, MultiTaskPolicy policy) throws RemoteException {
                if (!HwSysResManagerService.this.isServiceReady()) {
                    return;
                }
                if (ResourceUtils.checkSysProcPermission(Binder.getCallingPid(), Binder.getCallingUid()) != 0) {
                    Log.e(HwSysResManagerService.TAG, "Process Permission error!");
                    return;
                }
                HwSysResHandler resHandler = ResourceDispatcher.dispath(resourceType, HwSysResManagerService.this.mContext);
                if (resHandler != null) {
                    resHandler.execute(policy);
                }
            }

            protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
                if (HwSysResManagerService.this.getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                    pw.println("Permission Denial: can't dump HwResourceManager service from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                } else if (Binder.getCallingUid() > IOTController.TYPE_MASTER || !TestCase.test(HwSysResManagerService.this.mContext, pw, args)) {
                    HwSysResManagerService.this.mRDAService.dump(fd, pw, args);
                    HwSysResManagerService.this.mResourceRecordStore.dumpImpl(pw);
                    CompactJobService.dumpLog(fd, pw, args);
                    HwSysInnerResImpl.getResource(20).dump(fd, pw);
                }
            }

            public int acquireSysRes(int resourceType, Uri uri, IContentObserver observer, Bundle args) {
                HwSysResource resource = HwSysInnerResImpl.getResource(resourceType);
                int uid = args.getInt("Uid");
                if (resource == null || ResourceUtils.checkAppUidPermission(uid) != 0) {
                    return HwSysResManagerService.MSG_REPORT_STATUS;
                }
                return resource.acquire(uri, observer, args);
            }

            public void reportData(CollectData data) {
                if (HwSysResManagerService.this.checkServiceReadyAndPermission()) {
                    HwSysResManagerService.this.mRDAService.reportData(data);
                }
            }

            public void reportDataWithCallback(CollectData data, IReportDataCallback callback) throws RemoteException {
                if (HwSysResManagerService.this.checkServiceReadyAndPermission()) {
                    HwSysResManagerService.this.mRDAService.reportDataWithCallback(data, callback);
                }
            }

            public void enableFeature(int type) {
                if (HwSysResManagerService.this.checkServiceReadyAndPermission()) {
                    HwSysResManagerService.this.mRDAService.enableFeature(type);
                }
            }

            public void disableFeature(int type) {
                if (HwSysResManagerService.this.checkServiceReadyAndPermission()) {
                    HwSysResManagerService.this.mRDAService.disableFeature(type);
                }
            }

            public boolean configUpdate() {
                if (HwSysResManagerService.this.checkServiceReadyAndPermission()) {
                    return HwSysResManagerService.this.mRDAService.configUpdate();
                }
                return false;
            }

            public void init(Bundle args) {
                if (HwSysResManagerService.this.checkServiceReadyAndPermission()) {
                    HwSysResManagerService.this.mRDAService.init(args);
                }
            }

            public void dispatchRPolicy(RPolicyData policy) {
                if (HwSysResManagerService.this.checkServiceReadyAndPermission()) {
                    RDMEDispatcher.dispatchRPolicy(policy);
                }
            }

            public boolean isResourceNeeded(int resourceid) {
                if (HwSysResManagerService.this.isServiceReady()) {
                    return HwSysResManagerService.this.mRDAService.isResourceNeeded(resourceid);
                }
                return false;
            }

            public int getDumpData(int time, List<DumpData> dumpData) {
                if (HwSysResManagerService.this.checkServiceReadyAndPermission()) {
                    return HwSysResManagerService.this.mRDAService.getDumpData(time, dumpData);
                }
                return 0;
            }

            public int getStatisticsData(List<StatisticsData> statisticsData) {
                if (HwSysResManagerService.this.checkServiceReadyAndPermission()) {
                    return HwSysResManagerService.this.mRDAService.getStatisticsData(statisticsData);
                }
                return 0;
            }

            public String saveBigData(int featureId, boolean clear) {
                if (HwSysResManagerService.this.checkServiceReadyAndPermission()) {
                    return HwSysResManagerService.this.mRDAService.saveBigData(featureId, clear);
                }
                return null;
            }

            public void updateFakeForegroundList(List<String> processList) {
                if (HwSysResManagerService.this.checkServiceReadyAndPermission()) {
                    HwSysResManagerService.this.mRDAService.updateFakeForegroundList(processList);
                }
            }

            public boolean isFakeForegroundProcess(String process) {
                if (HwSysResManagerService.this.checkServiceReadyAndPermission()) {
                    return HwSysResManagerService.this.mRDAService.isFakeForegroundProcess(process);
                }
                return false;
            }

            public boolean isEnableFakeForegroundControl() {
                if (HwSysResManagerService.this.checkServiceReadyAndPermission()) {
                    return HwSysResManagerService.this.mRDAService.isEnableFakeForegroundControl();
                }
                return false;
            }

            public int getPid(String procName) {
                if (!HwSysResManagerService.this.checkServiceReadyAndPermission()) {
                    return 0;
                }
                String[] procCommands = new String[HwSysResManagerService.PIDS_MONITOR_MASK];
                procCommands[0] = procName;
                return HwSysResManagerService.this.mProcMemInfoReader.getPidForProcName(procCommands);
            }

            public long getPss(int pid) {
                if (HwSysResManagerService.this.checkServiceReadyAndPermission()) {
                    return 1024 * HwSysResManagerService.this.mProcMemInfoReader.getProcessPssByPID(pid);
                }
                return 0;
            }

            public long getMemAvaliable() {
                if (!HwSysResManagerService.this.checkServiceReadyAndPermission()) {
                    return 0;
                }
                long memAvaliable = 0;
                MemoryReader memoryReader = MemoryReader.getInstance();
                if (memoryReader != null) {
                    memAvaliable = memoryReader.getMemAvailable();
                }
                return memAvaliable;
            }

            public void reportExceptFrame(int skippedFrames) {
                if (HwSysResManagerService.this.checkServiceReadyAndPermission()) {
                    Message msg = HwSysResManagerService.this.mHandler.obtainMessage();
                    msg.what = HwSysResManagerService.MSG_FLINER_DATA;
                    msg.arg1 = skippedFrames;
                    HwSysResManagerService.this.mHandler.sendMessage(msg);
                }
            }

            public boolean registerProcessStateChangeObserver(IProcessStateChangeObserver observer) throws RemoteException {
                if (!HwSysResManagerService.this.isServiceReady()) {
                    return false;
                }
                if (ResourceUtils.checkSysProcPermission(Binder.getCallingPid(), Binder.getCallingUid()) != 0) {
                    Log.e(HwSysResManagerService.TAG, "Register Process Launcher Observer Process Permission error!");
                    return false;
                }
                synchronized (HwSysResManagerService.this.mProcessStateChangeObserver) {
                    HwSysResManagerService.this.mProcessStateChangeObserver.register(observer);
                }
                return true;
            }

            public boolean unRegisterProcessStateChangeObserver(IProcessStateChangeObserver observer) throws RemoteException {
                if (!HwSysResManagerService.this.isServiceReady()) {
                    return false;
                }
                if (ResourceUtils.checkSysProcPermission(Binder.getCallingPid(), Binder.getCallingUid()) != 0) {
                    Log.e(HwSysResManagerService.TAG, "unRegister Process Launcher Observer Process Permission error!");
                    return false;
                }
                synchronized (HwSysResManagerService.this.mProcessStateChangeObserver) {
                    HwSysResManagerService.this.mProcessStateChangeObserver.unregister(observer);
                }
                return true;
            }

            public void noteProcessStart(String packageName, String processName, int pid, int uid, boolean started, String launcherMode, String reason) {
                if (HwSysResManagerService.this.checkServiceReadyAndPermission() && ResourceUtils.getProcessTypeId(uid, packageName, -1) == 0 && "activity".equals(launcherMode)) {
                    Message msg = HwSysResManagerService.this.mHandler.obtainMessage();
                    msg.what = HwSysResManagerService.MSG_DISPATCH_PROCESS_LAUNCHER;
                    msg.arg1 = uid;
                    msg.arg2 = pid;
                    SomeArgs args = SomeArgs.obtain();
                    args.arg1 = packageName;
                    args.arg2 = processName;
                    args.arg3 = Boolean.valueOf(started);
                    args.arg4 = launcherMode;
                    args.arg5 = reason;
                    msg.obj = args;
                    HwSysResManagerService.this.mHandler.sendMessage(msg);
                }
            }
        };
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                    if (HwSysResManagerService.this.mAppUsageFile != null) {
                        HwSysResManagerService.this.mAppUsageFile.saveUsageInfo();
                    }
                } else if ("android.intent.action.PACKAGE_REMOVED".equals(action) || "android.intent.action.PACKAGE_REPLACED".equals(action)) {
                    String pkg = intent.getData().getSchemeSpecificPart();
                    if (Utils.DEBUG) {
                        Log.i(HwSysResManagerService.TAG, " receiver for  " + intent.getAction() + "  pkg " + pkg);
                    }
                    if (HwSysResManagerService.this.mAppUsageFile != null) {
                        HwSysResManagerService.this.mAppUsageFile.updateUsageInfo(pkg);
                    }
                    int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                    if (Utils.DEBUG || Log.HWLog) {
                        Log.i(HwSysResManagerService.TAG, " remove uid " + uid);
                    }
                    Message msg = HwSysResManagerService.this.mHandler.obtainMessage();
                    msg.arg1 = uid;
                    msg.arg2 = 19;
                    HwSysResManagerService.this.mResourceRecordStore.cleanResourceRecordMap(msg);
                    synchronized (HwSysResManagerService.this.mThirdAppOverloadRecord) {
                        HwSysResManagerService.this.mThirdAppOverloadRecord.remove(pkg);
                    }
                }
            }
        };
        this.mProcessObserver = new IProcessObserver.Stub() {
            public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
                if (!(foregroundActivities || HwSysResManagerService.this.mResourceRecordStore == null)) {
                    HwSysResManagerService.this.mResourceRecordStore.handleOverloadResource(uid, pid, 35, 0);
                    HwSysResManagerService.this.mResourceRecordStore.handleOverloadResource(uid, pid, 36, 0);
                }
                if (Utils.DEBUG) {
                    Log.d(HwSysResManagerService.TAG, "onForegroundActivitiesChanged pid = " + pid + ", uid = " + uid + ", foregroundActivities=" + foregroundActivities);
                }
            }

            public void onProcessStateChanged(int pid, int uid, int procState) {
            }

            public void onProcessDied(int pid, int uid) {
                if (Utils.DEBUG) {
                    Log.i(HwSysResManagerService.TAG, "onProcessDied pid = " + pid + ", uid = " + uid);
                }
                HwSysResManagerService.this.mResourceRecordStore.cleanResRecordAppDied(uid, pid);
            }
        };
        this.mContext = context;
        setInstance(this);
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper(), this.mHandlerCallback);
        if (enableRms && (Utils.RMSVERSION & MSG_DISPATCH_PROCESS_LAUNCHER) != 0) {
            if (Utils.HWFLOW) {
                Log.i(TAG, "create the IOStatsService instance");
            }
            this.mIOStatsService = IOStatsService.getInstance(context, handlerThread.getLooper());
        }
        this.mRDAService = new RDAService(this.mContext, handlerThread);
    }

    private static void setInstance(HwSysResManagerService service) {
        mSelf = service;
    }

    private boolean checkServiceReadyAndPermission() {
        if (isServiceReady()) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            if (ResourceUtils.checkSysProcPermission(pid, uid) == 0) {
                return true;
            }
            Log.e(TAG, "checkServiceReadyAndPermission permission denied! pid = " + pid + " ,uid = " + uid);
            return false;
        }
        Log.e(TAG, "checkServiceReadyAndPermission service not ready!");
        return false;
    }

    private boolean checkPermissionForMultiUserMode() {
        if (!isServiceReady()) {
            return false;
        }
        int pid = Binder.getCallingPid();
        int uid = UserHandle.getAppId(Binder.getCallingUid());
        if (ResourceUtils.checkSysProcPermission(pid, uid) == 0) {
            return true;
        }
        Log.e(TAG, "checkPermissionForMultiUserMode permission denied! pid = " + pid + " ,uid = " + uid);
        return false;
    }

    private boolean checkPermissionForWhiteList(int resourcetype) {
        if (!isServiceReady()) {
            return false;
        }
        if (resourcetype == 12 || ResourceUtils.checkSysProcPermission(Binder.getCallingPid(), Binder.getCallingUid()) == 0) {
            return true;
        }
        Log.e(TAG, "Process Permission error!");
        return false;
    }

    public void onStart() {
        if (Utils.DEBUG || Utils.HWFLOW) {
            Log.i(TAG, "publishBinderService()");
        }
        publishBinderService("hwsysresmanager", this.mService);
    }

    public void onBootPhase(int phase) {
        if (phase == 550 && this.mHandler != null) {
            this.mHandler.sendEmptyMessage(PIDS_MONITOR_MASK);
            this.mHandler.sendEmptyMessage(MSG_IO_SERVICE);
        }
    }

    public void cloudFileUpate() {
        if (Utils.DEBUG || Utils.HWFLOW || Log.HWLog) {
            Log.d(TAG, "cloudFileUpate()");
        }
        Message msg = this.mHandler.obtainMessage();
        msg.what = MSG_WHITENAME_FILE_UPDATE;
        this.mHandler.sendMessage(msg);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleDispatchMessage(Message msg) {
        if (msg != null) {
            int uid = msg.arg1;
            int pid = msg.arg2;
            SomeArgs args = msg.obj;
            String packageName = args.arg1;
            String processName = args.arg2;
            boolean started = ((Boolean) args.arg3).booleanValue();
            String launcherMode = args.arg4;
            String reason = args.arg5;
            args.recycle();
            if (this.mResourceRecordStore.isOverloadResourceRecord(uid, pid, 19)) {
                if (Utils.HWFLOW) {
                    Log.i(TAG, "noteProcessStart -uid =" + uid + " should be forbidden!");
                }
                synchronized (this.mThirdAppOverloadRecord) {
                    Long record = (Long) this.mThirdAppOverloadRecord.get(packageName);
                    long currentTime = SystemClock.uptimeMillis();
                    if (Utils.DEBUG && record != null) {
                        Log.d(TAG, "pkg " + packageName + " inteval " + (currentTime - record.longValue()));
                    }
                    if (record != null) {
                    }
                    if (record == null) {
                        dispatchProcessLauncher(packageName, processName, pid, uid, started, launcherMode, reason);
                        this.mThirdAppOverloadRecord.put(packageName, Long.valueOf(currentTime));
                        if (Utils.DEBUG) {
                            Log.d(TAG, "got it. pkg " + packageName + " report time " + currentTime);
                        }
                    }
                }
                msg.arg2 = 19;
                this.mResourceRecordStore.cleanResourceRecordMap(msg);
            }
        }
    }

    public void handleInitService() {
        boolean statisticDisable = true;
        this.mConfig = new HwConfigReader();
        if (this.mConfig.loadResConfig()) {
            this.mServiceReady = true;
        } else {
            Log.e(TAG, "handleInitService read xml file error");
        }
        if ((Utils.RMSVERSION & PIDS_MONITOR_MASK) != 0) {
            statisticDisable = false;
        } else if ((Utils.RMSVERSION & MSG_REPORT_STATUS) != 0) {
            statisticDisable = false;
        }
        if (!statisticDisable) {
            int baseSamplePeriod = this.mConfig.getSampleBasePeriod();
            if (baseSamplePeriod > 0) {
                this.mStatisticCtl = new HwStatisticCtl(this.mConfig);
                this.mStatisticCtl.init();
                this.mHandler.sendEmptyMessageDelayed(MSG_SAMPLE, (long) baseSamplePeriod);
            }
        }
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mProcessObserver);
        } catch (RemoteException e) {
            Slog.e(TAG, "RMS register process observer failed");
        }
        PidsResource pids = (PidsResource) HwFrameworkFactory.getHwResource(16);
        if (pids != null) {
            StringBuffer buffer = new StringBuffer(String.valueOf(this.mConfig.getResourceThreshold(16, 0)));
            buffer.append(",");
            buffer.append(String.valueOf(this.mConfig.getResourceStrategy(16, 0)));
            if (Utils.DEBUG) {
                Log.d(TAG, "handleInitService Pids=" + buffer.toString());
            }
            pids.init(buffer.toString());
        }
        try {
            System.loadLibrary("sysrms_jni");
            this.mPtr = nativeInit(this, this.mHandler.getLooper().getQueue(), PIDS_MONITOR_MASK);
        } catch (UnsatisfiedLinkError e2) {
            this.mServiceReady = false;
            Slog.e(TAG, "libsysrms_jni library not found!");
        }
        if (enableRms) {
            CompactJobService.schedule(this.mContext);
        }
        setSRMSFeature();
        monitorUsageInfo();
        this.mThirdAppUploadInterval = this.mConfig.getResourceThreshold(19, MSG_CLEAN_MAP);
    }

    private void monitorUsageInfo() {
        boolean appUsageFileEnabled = false;
        if ((Utils.RMSVERSION & 16) != 0) {
            appUsageFileEnabled = true;
        }
        if (appUsageFileEnabled) {
            IntentFilter pkgFilter = new IntentFilter();
            pkgFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
            this.mContext.registerReceiver(this.mReceiver, pkgFilter, null, this.mHandler);
            pkgFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            pkgFilter.addAction("android.intent.action.PACKAGE_REPLACED");
            pkgFilter.addDataScheme(ControlScope.PACKAGE_ELEMENT_KEY);
            this.mContext.registerReceiver(this.mReceiver, pkgFilter, null, this.mHandler);
        }
        this.mAppUsageFile = new AppUsageFileRecord(this.mContext, this.mConfig.getResourceMaxPeroid(19, MSG_CLEAN_MAP), appUsageFileEnabled);
        if (appUsageFileEnabled) {
            this.mAppUsageFile.loadUsageInfo();
        }
    }

    private void setSRMSFeature() {
        if (this.mRDAService != null) {
            int srmsFeature = this.mRDAService.isFeatureEnabled(FeatureType.getFeatureId(FeatureType.FEATURE_RESOURCE));
            if (srmsFeature == 0) {
                SystemProperties.set("persist.sys.srms.enable", "false");
            } else if (PIDS_MONITOR_MASK == srmsFeature) {
                SystemProperties.set("persist.sys.srms.enable", "true");
            } else {
                Log.e(TAG, "get SRMS feature failed");
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateWhiteList() {
        if (Utils.DEBUG || Log.HWLog) {
            Log.d(TAG, "updateWhiteList(), CloudFileUpdate=" + this.mCloudFileUpdate.get());
        }
        if (enableIaware && this.mCloudFileUpdate.get()) {
            this.mCloudFileUpdate.getAndSet(false);
            synchronized (this.mRegisteredResourceCallback) {
                int len = this.mRegisteredResourceCallback.beginBroadcast();
                for (int i = 0; i < len; i += PIDS_MONITOR_MASK) {
                    try {
                        ((IUpdateWhiteListCallback) this.mRegisteredResourceCallback.getBroadcastItem(i)).update();
                    } catch (RemoteException e) {
                        Log.e(TAG, "Trigger the rms client registered callback error");
                    }
                }
                if (Utils.DEBUG) {
                    Log.d(TAG, "updateWhiteList Compact trigger cloud config update all registered resource  [num:" + len + "]");
                }
                this.mRegisteredResourceCallback.finishBroadcast();
            }
            return;
        }
        Log.e(TAG, "Compact trigger cloud config update, get config file failed   isEnable:" + enableIaware);
    }

    public static HwSysResManagerService self() {
        return mSelf;
    }

    private boolean isServiceReady() {
        return this.mServiceReady;
    }

    private void dispatchMonitorEvent(String event) {
        Log.w(TAG, "dispatchMonitorEvent" + event);
        ArrayList<ResourceOverloadRecord> recordList = EventParser.parser(this.mContext, event);
        if (recordList.size() > 0) {
            for (ResourceOverloadRecord r : recordList) {
                long id = ResourceUtils.getResourceId(r.getUid(), r.getPid(), 16);
                if (!this.mResourceRecordStore.hasResourceStatusRecord(id)) {
                    this.mResourceRecordStore.uploadResourceStatusRecord(id, r);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void dispatchProcessLauncher(String packageName, String processName, int pid, int uid, boolean started, String launcherMode, String reason) {
        if (Utils.DEBUG || Log.HWINFO) {
            Log.i(TAG, "begin diapatch Process observer! pkg " + packageName);
        }
        synchronized (this.mProcessStateChangeObserver) {
            int len = this.mProcessStateChangeObserver.beginBroadcast();
            for (int i = 0; i < len; i += PIDS_MONITOR_MASK) {
                IProcessStateChangeObserver observer = (IProcessStateChangeObserver) this.mProcessStateChangeObserver.getBroadcastItem(i);
                if (observer != null) {
                    try {
                        if (Utils.DEBUG || Log.HWINFO) {
                            Log.d(TAG, "calling dispatchProcessLauncher, pkg " + packageName);
                        }
                        observer.onProcessLauncher(packageName, processName, pid, uid, started, launcherMode, reason);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Trigger the rms client registered observer error");
                    }
                }
            }
            this.mProcessStateChangeObserver.finishBroadcast();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dispatchProcessDiedOverload(String packageName, int uid) {
        if (Utils.DEBUG || Log.HWINFO) {
            Log.i(TAG, "begin diapatch Process Died Overload! pkg " + packageName);
        }
        Bundle data = new Bundle();
        data.putString(HwGpsPowerTracker.DEL_PKG, packageName);
        synchronized (this.mProcessStateChangeObserver) {
            int len = this.mProcessStateChangeObserver.beginBroadcast();
            for (int i = 0; i < len; i += PIDS_MONITOR_MASK) {
                IProcessStateChangeObserver observer = (IProcessStateChangeObserver) this.mProcessStateChangeObserver.getBroadcastItem(i);
                if (observer != null) {
                    try {
                        if (Utils.DEBUG || Log.HWINFO) {
                            Log.d(TAG, "calling observer.onProcessDiedOverload, pkg " + packageName);
                        }
                        observer.onProcessDiedOverload(data);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Trigger the rms client registered observer error");
                    }
                }
            }
            this.mProcessStateChangeObserver.finishBroadcast();
        }
    }
}

package com.android.server.rms;

import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.app.IProcessObserver.Stub;
import android.app.mtm.MultiTaskPolicy;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ParceledListSlice;
import android.database.IContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.rms.HwSysResource;
import android.rms.IHwSysResManager;
import android.rms.IProcessStateChangeObserver;
import android.rms.IUpdateWhiteListCallback;
import android.rms.config.ResourceConfig;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.CollectData;
import android.rms.iaware.DumpData;
import android.rms.iaware.IReportDataCallback;
import android.rms.iaware.LogIAware;
import android.rms.iaware.NetLocationStrategy;
import android.rms.iaware.RPolicyData;
import android.rms.iaware.StatisticsData;
import android.rms.iaware.memrepair.MemRepairPkgInfo;
import android.rms.resource.PidsResource;
import android.rms.utils.Utils;
import android.util.Log;
import android.util.Slog;
import com.android.internal.os.SomeArgs;
import com.android.server.SystemService;
import com.android.server.devicepolicy.StorageUtils;
import com.android.server.display.HwEyeProtectionDividedTimeControl;
import com.android.server.location.HwGpsPowerTracker;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.collector.ProcMemInfoReader;
import com.android.server.rms.config.HwConfigReader;
import com.android.server.rms.dump.DumpCase;
import com.android.server.rms.handler.HwSysResHandler;
import com.android.server.rms.handler.ResourceDispatcher;
import com.android.server.rms.iaware.RDAService;
import com.android.server.rms.iaware.RDMEDispatcher;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.appmng.FreezeDataManager;
import com.android.server.rms.iaware.cpu.CPUFullscreenMgr;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.io.IOStatsService;
import com.android.server.rms.memrepair.MemRepairPolicy;
import com.android.server.rms.record.AppUsageFileRecord;
import com.android.server.rms.record.ResourceRecordStore;
import com.android.server.rms.record.ResourceUtils;
import com.android.server.rms.resource.HwSysInnerResImpl;
import com.android.server.security.tsmagent.logic.spi.tsm.laser.LaserTSMServiceImpl;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HwSysResManagerService extends SystemService {
    private static final String APP_UID = "uid";
    private static long BIGDATAINFOS_UPLOAD_PERIOD = 0;
    public static final int MSG_ADD_BIGDATA_INFOS = 12;
    private static final int MSG_CLEAN_MAP = 3;
    private static final int MSG_DISPATCH_PROCESS_LAUNCHER = 8;
    private static final int MSG_FLINER_DATA = 9;
    private static final int MSG_INIT = 1;
    private static final int MSG_IO_SERVICE = 6;
    private static final int MSG_NOTIFY_STATUS = 4;
    private static final int MSG_REPORT_APPTYPE_DATA = 10;
    private static final int MSG_REPORT_STATUS = 2;
    private static final int MSG_SAMPLE = 5;
    public static final int MSG_UPLOAD_BIGDATA_INFOS = 11;
    private static final int MSG_WHITENAME_FILE_UPDATE = 7;
    private static final int SURFACEFLINGER_TRANSE_CODE = 2001;
    private static final String TAG = "RMS.HwSysResManagerService";
    private static boolean enableIaware = SystemProperties.getBoolean("persist.sys.enable_iaware", false);
    private static boolean enableRms = SystemProperties.getBoolean("ro.config.enable_rms", false);
    private static HwSysResManagerService mSelf;
    private AppUsageFileRecord mAppUsageFile = null;
    private final AtomicBoolean mCloudFileUpdate = new AtomicBoolean(false);
    private HwConfigReader mConfig;
    private final Context mContext;
    private final Handler mHandler;
    private Callback mHandlerCallback = new Callback() {
        public boolean handleMessage(Message msg) {
            if (Utils.DEBUG) {
                Log.d(HwSysResManagerService.TAG, "handleMessage /" + msg.what);
            }
            switch (msg.what) {
                case 1:
                    HwSysResManagerService.this.handleInitService();
                    return true;
                case 2:
                    HwSysResManagerService.this.mResourceRecordStore.recordResourceOverloadStatus(msg);
                    return true;
                case 3:
                    HwSysResManagerService.this.mResourceRecordStore.cleanResourceRecordMap(msg);
                    return true;
                case 4:
                    HwSysResManagerService.this.mResourceRecordStore.notifyResourceStatus(msg);
                    return true;
                case 6:
                    if (HwSysResManagerService.enableRms && (Utils.RMSVERSION & 1) != 0) {
                        HwSysResManagerService.this.mIOStatsService.startIOStatsService();
                    }
                    return true;
                case 7:
                    HwSysResManagerService.this.mCloudFileUpdate.getAndSet(HwSysResManagerService.this.mConfig.updateResConfig());
                    return true;
                case 8:
                    HwSysResManagerService.this.handleDispatchMessage(msg);
                    return true;
                case 9:
                    LogIAware.report(2045, String.valueOf(msg.arg1));
                    return true;
                case 10:
                    Bundle args = msg.getData();
                    String pkgName = args.getString(AwareIntelligentRecg.CMP_PKGNAME);
                    if ("APPTYPE_INIT_ACTION".equals(pkgName)) {
                        AppTypeRecoManager.getInstance().init(HwSysResManagerService.this.mContext);
                        return true;
                    }
                    int apptype = args.getInt("appType");
                    boolean status = args.getBoolean("appsSatus");
                    int attr = args.getInt("appAttr");
                    if (status) {
                        AppTypeRecoManager.getInstance().addAppType(pkgName, apptype, attr);
                    } else {
                        AppTypeRecoManager.getInstance().removeAppType(pkgName);
                    }
                    return true;
                case 11:
                    HwSysResManagerService.this.mResourceRecordStore.uploadBigDataInfos();
                    HwSysResManagerService.this.repeatlyCheckUploadBigDataInfos();
                    return true;
                case 12:
                    HwSysResManagerService.this.mResourceRecordStore.createAndCheckUploadBigDataInfos(msg);
                    return true;
                default:
                    return false;
            }
        }
    };
    private IOStatsService mIOStatsService = null;
    private final ProcMemInfoReader mProcMemInfoReader = new ProcMemInfoReader();
    private IProcessObserver mProcessObserver = new Stub() {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (!(foregroundActivities || HwSysResManagerService.this.mResourceRecordStore == null)) {
                HwSysResManagerService.this.mResourceRecordStore.handleOverloadResource(uid, pid, 29, 0);
                HwSysResManagerService.this.mResourceRecordStore.handleOverloadResource(uid, pid, 30, 0);
                HwSysResManagerService.this.mResourceRecordStore.handleOverloadResource(uid, pid, 13, 0);
            }
            if (Utils.DEBUG) {
                Log.d(HwSysResManagerService.TAG, "onForegroundActivitiesChanged pid = " + pid + ", uid = " + uid + ", foregroundActivities=" + foregroundActivities);
            }
        }

        public void onProcessDied(int pid, int uid) {
            if (Utils.DEBUG) {
                Log.i(HwSysResManagerService.TAG, "onProcessDied pid = " + pid + ", uid = " + uid);
            }
            HwSysResManagerService.this.mResourceRecordStore.cleanResRecordAppDied(uid, pid);
        }
    };
    private RemoteCallbackList<IProcessStateChangeObserver> mProcessStateChangeObserver = new RemoteCallbackList();
    private RDAService mRDAService = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
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
                msg.arg2 = 18;
                HwSysResManagerService.this.mResourceRecordStore.cleanResourceRecordMap(msg);
                synchronized (HwSysResManagerService.this.mThirdAppOverloadRecord) {
                    HwSysResManagerService.this.mThirdAppOverloadRecord.remove(pkg);
                }
            }
        }
    };
    private RemoteCallbackList<IUpdateWhiteListCallback> mRegisteredResourceCallback = new RemoteCallbackList();
    private ResourceRecordStore mResourceRecordStore;
    private final IBinder mService = new IHwSysResManager.Stub() {
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int resIDGestureScroll;
            Bundle bundleArgs;
            int duration;
            long curtime;
            Bundle bundle;
            switch (code) {
                case 2001:
                    data.enforceInterface("android.rms.IHwSysResManager");
                    reportExceptFrame(data.readInt());
                    reply.writeNoException();
                    return true;
                case 15007:
                    data.enforceInterface("android.rms.IHwSysResManager");
                    resIDGestureScroll = ResourceType.getReousrceId(ResourceType.RESOURCE_SCENE_REC);
                    bundleArgs = new Bundle();
                    duration = data.readInt();
                    int diff = data.readInt();
                    curtime = System.currentTimeMillis();
                    bundleArgs.putInt("relationType", 13);
                    bundleArgs.putInt("scroll_duration", duration);
                    bundleArgs.putInt("movedif", diff);
                    reportData(new CollectData(resIDGestureScroll, curtime, bundleArgs));
                    return true;
                case 15009:
                    data.enforceInterface("android.rms.IHwSysResManager");
                    duration = data.readInt();
                    resIDGestureScroll = ResourceType.getReousrceId(ResourceType.RESOURCE_SCENE_REC);
                    bundleArgs = new Bundle();
                    curtime = System.currentTimeMillis();
                    bundleArgs.putInt("relationType", 15);
                    bundleArgs.putInt("scroll_duration", duration);
                    reportData(new CollectData(resIDGestureScroll, curtime, bundleArgs));
                    return true;
                case 15015:
                    data.enforceInterface("android.rms.IHwSysResManager");
                    int dexpid = data.readInt();
                    int ppid = data.readInt();
                    String pkgName = data.readString();
                    bundle = new Bundle();
                    bundle.putInt("eventId", 2);
                    bundle.putInt("dexopt_pid", dexpid);
                    bundle.putInt("ppid", ppid);
                    bundle.putString(AwareUserHabit.USERHABIT_PACKAGE_NAME, pkgName);
                    reportData(new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_INSTALLER_MANAGER), System.currentTimeMillis(), bundle));
                    return true;
                case 15016:
                    data.enforceInterface("android.rms.IHwSysResManager");
                    reportData(new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_INSTALLER_MANAGER), System.currentTimeMillis(), data.readBundle()));
                    return true;
                case 20017:
                    data.enforceInterface("android.rms.IHwSysResManager");
                    int uid = data.readInt();
                    resIDGestureScroll = ResourceType.getReousrceId(ResourceType.RESOURCE_MEDIA_BTN);
                    bundleArgs = new Bundle();
                    curtime = System.currentTimeMillis();
                    bundleArgs.putInt("eventid", 20017);
                    bundleArgs.putInt("callUid", uid);
                    reportData(new CollectData(resIDGestureScroll, curtime, bundleArgs));
                    return true;
                case 30000:
                    data.enforceInterface("android.rms.IHwSysResManager");
                    bundle = data.readBundle();
                    if (bundle != null) {
                        CPUFullscreenMgr.getInstance().fullscreenChange(bundle.getInt("pid", 0), bundle.getBoolean("isFullscreen", false));
                    }
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
                for (int i = 0; i < subTypeNum; i++) {
                    config[i] = HwSysResManagerService.this.mConfig.getResConfig(resourceType, i);
                }
            }
            return config;
        }

        public boolean registerResourceUpdateCallback(IUpdateWhiteListCallback updateCallback) throws RemoteException {
            if (updateCallback == null || (HwSysResManagerService.this.checkServiceReadyAndPermission(10) ^ 1) != 0) {
                return false;
            }
            synchronized (HwSysResManagerService.this.mRegisteredResourceCallback) {
                HwSysResManagerService.this.mRegisteredResourceCallback.register(updateCallback);
            }
            return true;
        }

        public List<String> getIAwareProtectList(int num) {
            if (!HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
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
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(resourceType)) {
                return HwSysResManagerService.this.mConfig.getWhiteList(resourceType, type);
            }
            return null;
        }

        public void triggerUpdateWhiteList() throws RemoteException {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                HwSysResManagerService.this.updateWhiteList();
            }
        }

        public List<String> getLongTimeRunningApps() {
            if (!HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
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
            if (!HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                return null;
            }
            List<String> list = null;
            AwareUserHabit habit = AwareUserHabit.getInstance();
            if (habit != null && habit.isEnable()) {
                list = habit.getMostFrequentUsedApp(n, minCount);
            }
            return list;
        }

        public void reportAppType(String pkgName, int appType, boolean status, int attr) {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                Message msg = HwSysResManagerService.this.mHandler.obtainMessage();
                msg.what = 10;
                Bundle data = msg.getData();
                data.putString(AwareIntelligentRecg.CMP_PKGNAME, pkgName);
                data.putInt("appType", appType);
                data.putBoolean("appsSatus", status);
                data.putInt("appAttr", attr);
                HwSysResManagerService.this.mHandler.sendMessage(msg);
            }
        }

        public void reportHabitData(ParceledListSlice habitData) {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10) && habitData != null) {
                List<Bundle> list = habitData.getList();
                if (list.size() >= 1) {
                    Bundle bundle = (Bundle) list.get(0);
                    if (bundle != null) {
                        AwareUserHabit habit = AwareUserHabit.getInstance();
                        if (habit != null && habit.isEnable()) {
                            habit.reportHabitData(bundle);
                        }
                    }
                }
            }
        }

        public void recordResourceOverloadStatus(int uid, String pkg, int resourceType, int overloadNum, int speedOverLoadPeriod, int totalNum, Bundle extra) {
            if (HwSysResManagerService.this.isServiceReady()) {
                Message msg = HwSysResManagerService.this.mHandler.obtainMessage();
                msg.what = 2;
                msg.arg1 = uid;
                msg.arg2 = resourceType;
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = pkg;
                args.argi1 = overloadNum;
                args.argi2 = speedOverLoadPeriod;
                args.argi3 = totalNum;
                msg.obj = args;
                if (extra != null) {
                    if (Utils.DEBUG) {
                        Log.e(HwSysResManagerService.TAG, "recordResourceOverloadStatus has NonNull Bundle");
                    }
                    if (resourceType == 18) {
                        extra.putInt("third_party_app_usetime", HwSysResManagerService.this.mAppUsageFile.getUsageTimeforUpload(pkg));
                    }
                    msg.setData(extra);
                }
                HwSysResManagerService.this.mHandler.sendMessage(msg);
            }
        }

        public void clearResourceStatus(int uid, int resourceType) throws RemoteException {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                Message msg = HwSysResManagerService.this.mHandler.obtainMessage();
                msg.what = 3;
                msg.arg1 = uid;
                msg.arg2 = resourceType;
                HwSysResManagerService.this.mHandler.sendMessage(msg);
            }
        }

        public void notifyResourceStatus(int resourceType, String resourceName, int resourceStatus, Bundle bd) throws RemoteException {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                Message msg = HwSysResManagerService.this.mHandler.obtainMessage();
                msg.what = 4;
                msg.arg1 = resourceType;
                msg.arg2 = resourceStatus;
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = resourceName;
                args.arg2 = bd;
                msg.obj = args;
                HwSysResManagerService.this.mHandler.sendMessage(msg);
            }
        }

        public void dispatch(int resourceType, MultiTaskPolicy policy) throws RemoteException {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                HwSysResHandler resHandler = ResourceDispatcher.dispath(resourceType, HwSysResManagerService.this.mContext);
                if (resHandler != null) {
                    resHandler.execute(policy);
                }
            }
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (HwSysResManagerService.this.getContext().checkCallingPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump HwResourceManager service from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            } else if (Binder.getCallingUid() > 1000 || !DumpCase.dump(HwSysResManagerService.this.mContext, pw, args)) {
                HwSysResManagerService.this.mRDAService.dump(fd, pw, args);
                HwSysResManagerService.this.mResourceRecordStore.dumpImpl(pw);
                CompactJobService.dumpLog(fd, pw, args);
            }
        }

        public int acquireSysRes(int resourceType, Uri uri, IContentObserver observer, Bundle args) {
            HwSysResource resource = HwSysInnerResImpl.getResource(resourceType);
            int uid = args.getInt("uid");
            if (resource == null || ResourceUtils.checkAppUidPermission(uid) != 0) {
                return 2;
            }
            return resource.acquire(uri, observer, args);
        }

        public void reportData(CollectData data) {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                HwSysResManagerService.this.mRDAService.reportData(data);
            }
        }

        public void reportDataWithCallback(CollectData data, IReportDataCallback callback) throws RemoteException {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                HwSysResManagerService.this.mRDAService.reportDataWithCallback(data, callback);
            }
        }

        public void enableFeature(int type) {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                HwSysResManagerService.this.mRDAService.enableFeature(type);
            }
        }

        public void disableFeature(int type) {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                HwSysResManagerService.this.mRDAService.disableFeature(type);
            }
        }

        public boolean configUpdate() {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                return HwSysResManagerService.this.mRDAService.configUpdate();
            }
            return false;
        }

        public boolean custConfigUpdate() {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                return HwSysResManagerService.this.mRDAService.custConfigUpdate();
            }
            return false;
        }

        public void init(Bundle args) {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                HwSysResManagerService.this.mRDAService.init(args);
            }
        }

        public void dispatchRPolicy(RPolicyData policy) {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
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
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                return HwSysResManagerService.this.mRDAService.getDumpData(time, dumpData);
            }
            return 0;
        }

        public int getStatisticsData(List<StatisticsData> statisticsData) {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                return HwSysResManagerService.this.mRDAService.getStatisticsData(statisticsData);
            }
            return 0;
        }

        public String saveBigData(int featureId, boolean clear) {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                return HwSysResManagerService.this.mRDAService.saveBigData(featureId, clear);
            }
            return null;
        }

        public String fetchBigDataByVersion(int iVer, int fId, boolean beta, boolean clear) {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                return HwSysResManagerService.this.mRDAService.fetchBigDataByVersion(iVer, fId, beta, clear);
            }
            return null;
        }

        public void updateFakeForegroundList(List<String> processList) {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                HwSysResManagerService.this.mRDAService.updateFakeForegroundList(processList);
            }
        }

        public boolean isFakeForegroundProcess(String process) {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                return HwSysResManagerService.this.mRDAService.isFakeForegroundProcess(process);
            }
            return false;
        }

        public boolean isEnableFakeForegroundControl() {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                return HwSysResManagerService.this.mRDAService.isEnableFakeForegroundControl();
            }
            return false;
        }

        public int getPid(String procName) {
            if (!HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                return 0;
            }
            return HwSysResManagerService.this.mProcMemInfoReader.getPidForProcName(new String[]{procName});
        }

        public long getPss(int pid) {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                return 1024 * HwSysResManagerService.this.mProcMemInfoReader.getProcessPssByPID(pid);
            }
            return 0;
        }

        public long getMemAvaliable() {
            if (!HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
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
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                Message msg = HwSysResManagerService.this.mHandler.obtainMessage();
                msg.what = 9;
                msg.arg1 = skippedFrames;
                HwSysResManagerService.this.mHandler.sendMessage(msg);
            }
        }

        public boolean registerProcessStateChangeObserver(IProcessStateChangeObserver observer) throws RemoteException {
            if (!HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                return false;
            }
            synchronized (HwSysResManagerService.this.mProcessStateChangeObserver) {
                HwSysResManagerService.this.mProcessStateChangeObserver.register(observer);
            }
            return true;
        }

        public boolean unRegisterProcessStateChangeObserver(IProcessStateChangeObserver observer) throws RemoteException {
            if (!HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                return false;
            }
            synchronized (HwSysResManagerService.this.mProcessStateChangeObserver) {
                HwSysResManagerService.this.mProcessStateChangeObserver.unregister(observer);
            }
            return true;
        }

        public void noteProcessStart(String packageName, String processName, int pid, int uid, boolean started, String launcherMode, String reason) {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10) && ResourceUtils.getProcessTypeId(uid, packageName, -1) == 0 && "activity".equals(launcherMode)) {
                Message msg = HwSysResManagerService.this.mHandler.obtainMessage();
                msg.what = 8;
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

        public List<MemRepairPkgInfo> getMemRepairProcGroup(int sceneType) {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                return MemRepairPolicy.getInstance().getMemRepairPolicy(sceneType);
            }
            return null;
        }

        public List<String> getFrequentIM(int count) {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10)) {
                return FreezeDataManager.getInstance().getFrequentIM(count);
            }
            return null;
        }

        public NetLocationStrategy getNetLocationStrategy(String pkgName, int uid, int type) {
            if (HwSysResManagerService.this.checkServiceReadyAndPermission(10) || (checkClientPermission(1001) ^ 1) == 0) {
                return DevSchedFeatureRT.getNetLocationStrategy(pkgName, uid, type);
            }
            return null;
        }

        private boolean checkClientPermission(int uid) {
            boolean z = false;
            if (HwSysResManagerService.this.isServiceReady()) {
                if (UserHandle.getAppId(Binder.getCallingUid()) % LaserTSMServiceImpl.EXCUTE_OTA_RESULT_SUCCESS == uid) {
                    z = true;
                }
                return z;
            }
            Log.e(HwSysResManagerService.TAG, "service not ready!");
            return false;
        }
    };
    private boolean mServiceReady = false;
    private HashMap<String, Long> mThirdAppOverloadRecord = new HashMap();
    private int mThirdAppUploadInterval;

    static {
        long compactPeriodInterval;
        if (Utils.DEBUG) {
            compactPeriodInterval = (long) Utils.getCompactPeriodInterval();
        } else {
            compactPeriodInterval = HwEyeProtectionDividedTimeControl.DAY_IN_MIllIS;
        }
        BIGDATAINFOS_UPLOAD_PERIOD = compactPeriodInterval;
    }

    public HwSysResManagerService(Context context) {
        super(context);
        this.mContext = context;
        setInstance(this);
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper(), this.mHandlerCallback);
        this.mResourceRecordStore = ResourceRecordStore.getInstance(context);
        this.mResourceRecordStore.setMessageHandler(this.mHandler);
        repeatlyCheckUploadBigDataInfos();
        if (enableRms && (Utils.RMSVERSION & 1) != 0) {
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

    private boolean checkServiceReadyAndPermission(int resourceType) {
        if (!isServiceReady()) {
            Log.e(TAG, "checkServiceReadyAndPermission service not ready!");
            return false;
        } else if (resourceType == 12 || resourceType == 16) {
            return true;
        } else {
            int pid = Binder.getCallingPid();
            int uid = UserHandle.getAppId(Binder.getCallingUid());
            if (ResourceUtils.checkSysProcPermission(pid, uid) == 0) {
                return true;
            }
            Log.e(TAG, "Process Permission error! pid:" + pid + " uid:" + uid + " Process.myPid:" + Process.myPid());
            return false;
        }
    }

    public void onStart() {
        if (Utils.DEBUG || Utils.HWFLOW) {
            Log.i(TAG, "publishBinderService()");
        }
        publishBinderService("hwsysresmanager", this.mService);
    }

    public void onBootPhase(int phase) {
        if (phase == 550 && this.mHandler != null) {
            this.mHandler.sendEmptyMessage(1);
            this.mHandler.sendEmptyMessage(6);
        }
    }

    public void cloudFileUpate() {
        if (Utils.DEBUG || Utils.HWFLOW || Log.HWLog) {
            Log.d(TAG, "cloudFileUpate()");
        }
        Message msg = this.mHandler.obtainMessage();
        msg.what = 7;
        this.mHandler.sendMessage(msg);
    }

    public IHwSysResManager getHwSysResManagerService() {
        if (this.mService != null) {
            return IHwSysResManager.Stub.asInterface(this.mService);
        }
        return null;
    }

    private void repeatlyCheckUploadBigDataInfos() {
        if (!(this.mHandler == null || this.mHandler.hasMessages(11))) {
            Message msg = Message.obtain();
            msg.what = 11;
            this.mHandler.sendMessageDelayed(msg, BIGDATAINFOS_UPLOAD_PERIOD);
        }
    }

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
            if (this.mResourceRecordStore.isOverloadResourceRecord(uid, pid, 18)) {
                if (Utils.HWFLOW) {
                    Log.i(TAG, "noteProcessStart -uid =" + uid + " should be forbidden!");
                }
                synchronized (this.mThirdAppOverloadRecord) {
                    Long record = (Long) this.mThirdAppOverloadRecord.get(packageName);
                    long currentTime = SystemClock.uptimeMillis();
                    if (Utils.DEBUG && record != null) {
                        Log.d(TAG, "pkg " + packageName + " inteval " + (currentTime - record.longValue()));
                    }
                    if ((record != null && currentTime - record.longValue() >= ((long) this.mThirdAppUploadInterval)) || record == null) {
                        dispatchProcessLauncher(packageName, processName, pid, uid, started, launcherMode, reason);
                        this.mThirdAppOverloadRecord.put(packageName, Long.valueOf(currentTime));
                        if (Utils.DEBUG) {
                            Log.d(TAG, "got it. pkg " + packageName + " report time " + currentTime);
                        }
                    }
                }
                msg.arg2 = 18;
                this.mResourceRecordStore.cleanResourceRecordMap(msg);
            }
        }
    }

    public void handleInitService() {
        this.mConfig = new HwConfigReader();
        if (this.mConfig.loadResConfig()) {
            this.mServiceReady = true;
        } else {
            Log.e(TAG, "handleInitService read xml file error");
        }
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mProcessObserver);
        } catch (RemoteException e) {
            Slog.e(TAG, "RMS register process observer failed");
        }
        initPidCtrollGroup();
        try {
            System.loadLibrary("sysrms_jni");
        } catch (UnsatisfiedLinkError e2) {
            this.mServiceReady = false;
            Slog.e(TAG, "libsysrms_jni library not found!");
        }
        if (enableRms) {
            CompactJobService.schedule(this.mContext);
        }
        setSRMSFeature();
        setAppStartupFeature();
        monitorUsageInfo();
        this.mThirdAppUploadInterval = this.mConfig.getResourceThreshold(18, 3);
    }

    private void monitorUsageInfo() {
        boolean appUsageFileEnabled = (Utils.RMSVERSION & 2) != 0;
        if (appUsageFileEnabled) {
            IntentFilter pkgFilter = new IntentFilter();
            pkgFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
            this.mContext.registerReceiver(this.mReceiver, pkgFilter, null, this.mHandler);
            pkgFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            pkgFilter.addAction("android.intent.action.PACKAGE_REPLACED");
            pkgFilter.addDataScheme("package");
            this.mContext.registerReceiver(this.mReceiver, pkgFilter, null, this.mHandler);
        }
        this.mAppUsageFile = new AppUsageFileRecord(this.mContext, this.mConfig.getResourceMaxPeroid(18, 3), appUsageFileEnabled);
        if (appUsageFileEnabled) {
            this.mAppUsageFile.loadUsageInfo();
        }
    }

    private void setSRMSFeature() {
        if (this.mRDAService != null) {
            int srmsFeature = this.mRDAService.isFeatureEnabled(FeatureType.getFeatureId(FeatureType.FEATURE_RESOURCE));
            if (srmsFeature == 0) {
                SystemProperties.set("persist.sys.srms.enable", StorageUtils.SDCARD_RWMOUNTED_STATE);
            } else if (1 == srmsFeature) {
                SystemProperties.set("persist.sys.srms.enable", StorageUtils.SDCARD_ROMOUNTED_STATE);
            } else {
                Log.e(TAG, "get SRMS feature failed");
            }
        }
    }

    private void setAppStartupFeature() {
        if (this.mRDAService != null) {
            int startupFeature = this.mRDAService.isFeatureEnabled(FeatureType.getFeatureId(FeatureType.FEATURE_APPSTARTUP));
            if (startupFeature == 0) {
                SystemProperties.set("persist.sys.appstart.enable", StorageUtils.SDCARD_RWMOUNTED_STATE);
            } else if (1 == startupFeature) {
                SystemProperties.set("persist.sys.appstart.enable", StorageUtils.SDCARD_ROMOUNTED_STATE);
            }
            Log.i(TAG, "setAppStartupFeature: " + startupFeature);
        }
    }

    public void updateWhiteList() {
        if (Utils.DEBUG || Log.HWLog) {
            Log.d(TAG, "updateWhiteList(), CloudFileUpdate=" + this.mCloudFileUpdate.get());
        }
        if (enableIaware && this.mCloudFileUpdate.get()) {
            this.mCloudFileUpdate.getAndSet(false);
            synchronized (this.mRegisteredResourceCallback) {
                int len = this.mRegisteredResourceCallback.beginBroadcast();
                for (int i = 0; i < len; i++) {
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

    private void dispatchProcessLauncher(String packageName, String processName, int pid, int uid, boolean started, String launcherMode, String reason) {
        if (Utils.DEBUG || Log.HWINFO) {
            Log.i(TAG, "begin diapatch Process observer! pkg " + packageName);
        }
        synchronized (this.mProcessStateChangeObserver) {
            int len = this.mProcessStateChangeObserver.beginBroadcast();
            for (int i = 0; i < len; i++) {
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

    public void dispatchProcessDiedOverload(String packageName, int uid) {
        if (Utils.DEBUG || Log.HWINFO) {
            Log.i(TAG, "begin diapatch Process Died Overload! pkg " + packageName);
        }
        Bundle data = new Bundle();
        data.putString(HwGpsPowerTracker.DEL_PKG, packageName);
        synchronized (this.mProcessStateChangeObserver) {
            int len = this.mProcessStateChangeObserver.beginBroadcast();
            for (int i = 0; i < len; i++) {
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

    private void initPidCtrollGroup() {
        if (Utils.DEBUG) {
            Log.d(TAG, "initPidCtrollGroup");
        }
        PidsResource pids = (PidsResource) HwFrameworkFactory.getHwResource(15);
        String[] thresholds = getPidCgroupConfig();
        if (pids != null && thresholds != null) {
            pids.init(thresholds);
            ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
            try {
                pids.acquire(Process.myPid(), "system_server", 2);
            } finally {
                StrictMode.setThreadPolicy(savedPolicy);
            }
        }
    }

    private String[] getPidCgroupConfig() {
        int subTypeNum = this.mConfig.getSubTypeNum(15);
        String[] thresholds = null;
        if (subTypeNum > 0) {
            thresholds = new String[subTypeNum];
        }
        if (thresholds != null) {
            for (int i = 0; i < subTypeNum; i++) {
                StringBuffer buffer = new StringBuffer(String.valueOf(this.mConfig.getResourceThreshold(15, i)));
                buffer.append(",");
                buffer.append(String.valueOf(this.mConfig.getResourceStrategy(15, i)));
                if (Utils.DEBUG) {
                    Log.d(TAG, "handleInitService Pids [%d" + i + "]=" + buffer.toString());
                }
                thresholds[i] = buffer.toString();
            }
        }
        return thresholds;
    }
}

package com.android.server.am;

import android.app.AppGlobals;
import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.content.IIntentReceiver;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Flog;
import android.util.Slog;
import com.android.server.HwServiceFactory;
import com.android.server.UiModeManagerService;
import com.android.server.pm.PackageDexOptimizer;
import com.huawei.pgmng.common.Utils;
import com.huawei.pgmng.log.LogPower;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HwBroadcastQueue extends BroadcastQueue {
    static final boolean DEBUG_CONSUMPTION = false;
    private static final int MIN_SEND_MSG_INTERVAL = 1000;
    private static final String MMS_PACKAGE_NAME = "com.android.mms";
    static final String TAG = "HwBroadcastQueue";
    private static final int TYPE_CONFIG_APP_ADD_ACTION = 6;
    private static final int TYPE_CONFIG_APP_REMOVE_ACTION = 7;
    private static final int TYPE_CONFIG_CLEAR = 0;
    private static final int TYPE_CONFIG_DROP_BC_ACTION = 2;
    private static final int TYPE_CONFIG_DROP_BC_BY_PID = 3;
    private static final int TYPE_CONFIG_MAX_PROXY_BC = 4;
    private static final int TYPE_CONFIG_PROXY_BC_ACTION = 1;
    private static final int TYPE_CONFIG_SAME_KIND_ACTION = 5;
    private static boolean mProxyFeature = true;
    private int MAX_PROXY_BROADCAST = 10;
    private boolean enableUploadRadar = true;
    private final HashMap<String, Set<String>> mActionExcludePkgs = new HashMap<>();
    private ArrayList<String> mActionWhiteList = new ArrayList<>();
    private final HashMap<String, ArrayList<String>> mAppAddProxyActions = new HashMap<>();
    private final HashMap<String, ArrayList<String>> mAppDropActions = new HashMap<>();
    private final HashMap<String, ArrayList<String>> mAppProxyActions = new HashMap<>();
    private final HashMap<String, ArrayList<String>> mAppRemoveProxyActions = new HashMap<>();
    private IBinder mAwareService = null;
    private HwFrameworkMonitor mBroadcastMonitor = HwFrameworkFactory.getHwFrameworkMonitor();
    private HwBroadcastRadarUtil mBroadcastRadarUtil;
    private ArrayList<BroadcastRecord> mCopyOrderedBroadcasts;
    private AbsHwMtmBroadcastResourceManager mHwMtmBroadcastResourceManager = null;
    private long mLastTime = SystemClock.uptimeMillis();
    final ArrayList<BroadcastRecord> mOrderedPendingBroadcasts = new ArrayList<>();
    final ArrayList<BroadcastRecord> mParallelPendingBroadcasts = new ArrayList<>();
    private final HashMap<Integer, ArrayList<String>> mProcessDropActions = new HashMap<>();
    private final ArrayList<String> mProxyActions = new ArrayList<>();
    final ArrayList<String> mProxyBroadcastPkgs = new ArrayList<>();
    final HashMap<String, Integer> mProxyPkgsCount = new HashMap<>();
    private HashMap<String, BroadcastRadarRecord> mRadarBroadcastMap;
    HashMap<String, String> mSameKindsActionList = new HashMap<String, String>() {
        {
            put("android.intent.action.SCREEN_ON", "android.intent.action.SCREEN_OFF");
        }
    };

    static class BroadcastRadarRecord {
        String actionName;
        int count;
        String packageName;

        public BroadcastRadarRecord() {
            this.actionName = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            this.packageName = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            this.count = 0;
        }

        public BroadcastRadarRecord(String actionName2, String packageName2, int count2) {
            this.actionName = actionName2;
            this.packageName = packageName2;
            this.count = count2;
        }
    }

    HwBroadcastQueue(ActivityManagerService service, Handler handler, String name, long timeoutPeriod, boolean allowDelayBehindServices) {
        super(service, handler, name, timeoutPeriod, allowDelayBehindServices);
        String closeSwitcher = SystemProperties.get("persist.sys.pg_close_action", null);
        if (closeSwitcher != null && closeSwitcher.contains("proxy_bc")) {
            Slog.w(TAG, "close proxy bc ");
            mProxyFeature = false;
        }
        this.mHwMtmBroadcastResourceManager = HwServiceFactory.getMtmBRManager(this);
        this.mCopyOrderedBroadcasts = new ArrayList<>();
        this.mBroadcastRadarUtil = new HwBroadcastRadarUtil();
        this.mRadarBroadcastMap = new HashMap<>();
        initActionWhiteList();
    }

    private boolean canTrim(BroadcastRecord r1, BroadcastRecord r2) {
        if (r1 == null || r2 == null || r1.intent == null || r2.intent == null || r1.intent.getAction() == null || r2.intent.getAction() == null) {
            return false;
        }
        Object o1 = r1.receivers.get(0);
        Object o2 = r2.receivers.get(0);
        String pkg1 = getPkg(o1);
        String pkg2 = getPkg(o2);
        if (pkg1 != null && !pkg1.equals(pkg2)) {
            return false;
        }
        if (o1 != o2) {
            if (!(o1 instanceof BroadcastFilter) || !(o2 instanceof BroadcastFilter)) {
                if (!(o1 instanceof ResolveInfo) || !(o2 instanceof ResolveInfo)) {
                    return false;
                }
                ResolveInfo info1 = (ResolveInfo) o1;
                ResolveInfo info2 = (ResolveInfo) o2;
                if (!(info1.activityInfo == info2.activityInfo && info1.providerInfo == info2.providerInfo && info1.serviceInfo == info2.serviceInfo)) {
                    return false;
                }
            } else if (((BroadcastFilter) o1).receiverList != ((BroadcastFilter) o2).receiverList) {
                return false;
            }
        }
        String action1 = r1.intent.getAction();
        String action2 = r2.intent.getAction();
        if (action1.equals(action2)) {
            return true;
        }
        String a1 = this.mSameKindsActionList.get(action1);
        String a2 = this.mSameKindsActionList.get(action2);
        if ((a1 == null || !a1.equals(action2)) && (a2 == null || !a2.equals(action1))) {
            return false;
        }
        return true;
    }

    private void trimAndEnqueueBroadcast(boolean trim, boolean isParallel, BroadcastRecord r, String recevier) {
        int count = 0;
        if (this.mProxyPkgsCount.containsKey(recevier)) {
            count = this.mProxyPkgsCount.get(recevier).intValue();
        }
        if (isParallel) {
            Iterator it = this.mParallelPendingBroadcasts.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                BroadcastRecord br = it.next();
                if (trim && canTrim(r, br)) {
                    it.remove();
                    count--;
                    break;
                }
            }
            this.mParallelPendingBroadcasts.add(r);
        } else {
            Iterator it2 = this.mOrderedPendingBroadcasts.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                BroadcastRecord br2 = it2.next();
                if (trim && canTrim(r, br2)) {
                    it2.remove();
                    count--;
                    break;
                }
            }
            this.mOrderedPendingBroadcasts.add(r);
        }
        int count2 = count + 1;
        this.mProxyPkgsCount.put(recevier, Integer.valueOf(count2));
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v(TAG, "trim and enqueue " + this.mQueueName + " Parallel:(" + this.mParallelPendingBroadcasts.size() + ") Ordered:(" + this.mOrderedPendingBroadcasts.size() + ")(" + r + ")");
        }
        if (count2 % this.MAX_PROXY_BROADCAST == 0) {
            Slog.i(TAG, "proxy max broadcasts, notify pg. recevier:" + recevier);
            notifyPG("overflow_bc", recevier, -1);
            if (count2 > this.MAX_PROXY_BROADCAST + 10) {
                Slog.w(TAG, "warnning, proxy more broadcast, notify pg. recevier:" + recevier);
                notifyPG("overflow_exception", recevier, -1);
            }
        }
    }

    private boolean shouldProxy(String pkg, int pid) {
        boolean z = false;
        if (!mProxyFeature) {
            return false;
        }
        if (pkg != null) {
            z = this.mProxyBroadcastPkgs.contains(pkg);
        }
        return z;
    }

    private void notifyPG(String action, String pkg, int pid) {
        Utils.handleTimeOut(action, pkg, String.valueOf(pid));
    }

    private boolean shouldNotifyPG(String action, String receiverPkg) {
        if (action == null || receiverPkg == null) {
            return true;
        }
        ArrayList<String> proxyActions = (ArrayList) this.mProxyActions.clone();
        if (this.mAppProxyActions.containsKey(receiverPkg)) {
            proxyActions = this.mAppProxyActions.get(receiverPkg);
        }
        ArrayList<String> addBCActions = null;
        if (this.mAppAddProxyActions.containsKey(receiverPkg)) {
            addBCActions = this.mAppAddProxyActions.get(receiverPkg);
        }
        if (proxyActions != null && !proxyActions.contains(action) && (addBCActions == null || !addBCActions.contains(action))) {
            return true;
        }
        if (this.mActionExcludePkgs.containsKey(action)) {
            Set<String> pkgs = this.mActionExcludePkgs.get(action);
            if (pkgs != null && pkgs.contains(receiverPkg)) {
                return true;
            }
        }
        ArrayList<String> removeBCActions = null;
        if (this.mAppRemoveProxyActions.containsKey(receiverPkg)) {
            removeBCActions = this.mAppRemoveProxyActions.get(receiverPkg);
        }
        if (removeBCActions == null || !removeBCActions.contains(action)) {
            return false;
        }
        return true;
    }

    public void reportMediaButtonToAware(BroadcastRecord r, Object target) {
        if (r != null && r.intent != null && target != null && "android.intent.action.MEDIA_BUTTON".equals(r.intent.getAction())) {
            long curTime = SystemClock.uptimeMillis();
            if (curTime - this.mLastTime >= 1000) {
                this.mLastTime = curTime;
                int uid = getUid(target);
                if (this.mAwareService == null) {
                    this.mAwareService = ServiceManager.getService("hwsysresmanager");
                }
                if (this.mAwareService != null) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    try {
                        data.writeInterfaceToken("android.rms.IHwSysResManager");
                        data.writeInt(uid);
                        this.mAwareService.transact(20017, data, reply, 0);
                        reply.readException();
                    } catch (RemoteException e) {
                        Slog.e(TAG, "mAwareService ontransact " + e.getMessage());
                    } catch (Throwable th) {
                        data.recycle();
                        reply.recycle();
                        throw th;
                    }
                    data.recycle();
                    reply.recycle();
                } else {
                    Slog.e(TAG, "mAwareService is not start");
                }
            }
        }
    }

    public boolean enqueueProxyBroadcast(boolean isParallel, BroadcastRecord r, Object target) {
        boolean z = isParallel;
        BroadcastRecord broadcastRecord = r;
        Object obj = target;
        if (obj == null) {
            return false;
        }
        String pkg = getPkg(obj);
        int pid = getPid(obj);
        int uid = getUid(obj);
        if (pkg == null || !shouldProxy(pkg, pid)) {
            return false;
        }
        List<Object> receiver = new ArrayList<>();
        receiver.add(obj);
        IIntentReceiver resultTo = broadcastRecord.resultTo;
        if (!z && broadcastRecord.resultTo != null) {
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.v(TAG, "reset resultTo null");
            }
            resultTo = null;
        }
        IIntentReceiver resultTo2 = resultTo;
        String action = broadcastRecord.intent.getAction();
        boolean notify = shouldNotifyPG(action, pkg);
        if (notify) {
            if (action != null && action.contains("|")) {
                action = action.replaceAll("[|]", PackageDexOptimizer.SKIP_SHARED_LIBRARY_CHECK);
            }
            LogPower.push(148, action, pkg, String.valueOf(pid), new String[]{broadcastRecord.callerPackage});
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.v(TAG, "enqueueProxyBroadcast notify pg broadcast:" + action + " pkg:" + pkg + " pid:" + pid + " uid:" + uid);
            }
        }
        String action2 = action;
        int uid2 = uid;
        int pid2 = pid;
        String action3 = action2;
        BroadcastRecord proxyBR = new BroadcastRecord(broadcastRecord.queue, broadcastRecord.intent, broadcastRecord.callerApp, broadcastRecord.callerPackage, broadcastRecord.callingPid, broadcastRecord.callingUid, broadcastRecord.callerInstantApp, broadcastRecord.resolvedType, broadcastRecord.requiredPermissions, broadcastRecord.appOp, broadcastRecord.options, receiver, resultTo2, broadcastRecord.resultCode, broadcastRecord.resultData, broadcastRecord.resultExtras, broadcastRecord.ordered, broadcastRecord.sticky, broadcastRecord.initialSticky, broadcastRecord.userId);
        String pkg2 = pkg;
        trimAndEnqueueBroadcast(!notify, isParallel, proxyBR, pkg2);
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v(TAG, "enqueueProxyBroadcast enqueue broadcast:" + action3 + " pkg:" + pkg2 + " pid:" + pid2 + " uid:" + uid2);
        } else {
            int i = pid2;
            String str = action3;
        }
        return true;
    }

    public void setProxyBCActions(List<String> actions) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                this.mProxyActions.clear();
                if (actions != null) {
                    if ("foreground".equals(this.mQueueName)) {
                        Slog.i(TAG, "set default proxy broadcast actions:" + actions);
                    }
                    this.mProxyActions.addAll(actions);
                }
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
    }

    public void setActionExcludePkg(String action, String pkg) {
        Set<String> pkgs;
        if (action == null && pkg == null) {
            Slog.w(TAG, "clear mActionExcludePkgs");
            this.mActionExcludePkgs.clear();
        } else if (action == null || pkg == null) {
            Slog.w(TAG, "setActionExcludePkg invaild param");
        } else {
            if (this.mActionExcludePkgs.containsKey(action)) {
                pkgs = this.mActionExcludePkgs.get(action);
                pkgs.add(pkg);
            } else {
                pkgs = new HashSet<>();
                pkgs.add(pkg);
            }
            this.mActionExcludePkgs.put(action, pkgs);
        }
    }

    public void proxyBCConfig(int type, String key, List<String> value) {
        if (!mProxyFeature) {
            Slog.w(TAG, "proxy bc not support");
            return;
        }
        if ("foreground".equals(this.mQueueName)) {
            Slog.i(TAG, String.format("proxy %s bc config [%d][%s][", new Object[]{this.mQueueName, Integer.valueOf(type), key}) + value + "]");
        }
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                switch (type) {
                    case 0:
                        clearConfigLocked();
                        break;
                    case 1:
                        configProxyBCActionsLocked(key, value);
                        break;
                    case 2:
                        configDropBCActionsLocked(key, value);
                        break;
                    case 3:
                        configDropBCByPidLocked(key, value);
                        break;
                    case 4:
                        configMaxProxyBCLocked(key);
                        break;
                    case 5:
                        configSameActionLocked(key, value);
                        break;
                    case 6:
                        configAddAppProxyBCActions(key, value);
                        break;
                    case 7:
                        configRemoveAppProxyBCActions(key, value);
                        break;
                }
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
    }

    private void setAppProxyBCActions(String pkg, List<String> actions) {
        if (pkg != null) {
            if ("foreground".equals(this.mQueueName)) {
                Slog.i(TAG, "set " + pkg + " proxy broadcast actions:" + actions);
            }
            if (actions != null) {
                this.mAppProxyActions.put(pkg, new ArrayList<>(actions));
                return;
            }
            this.mAppProxyActions.put(pkg, null);
        }
    }

    private void configAddAppProxyBCActions(String pkg, List<String> actions) {
        if (pkg == null) {
            Slog.e(TAG, "config add app bc actions error");
            return;
        }
        if (actions == null || actions.size() == 0) {
            this.mAppAddProxyActions.remove(pkg);
        } else {
            this.mAppAddProxyActions.put(pkg, new ArrayList<>(actions));
        }
    }

    private void configRemoveAppProxyBCActions(String pkg, List<String> actions) {
        if (pkg == null) {
            Slog.e(TAG, "config remove app bc actions error");
            return;
        }
        if (actions == null || actions.size() == 0) {
            this.mAppRemoveProxyActions.remove(pkg);
        } else {
            this.mAppRemoveProxyActions.put(pkg, new ArrayList<>(actions));
        }
    }

    private void configProxyBCActionsLocked(String pkg, List<String> actions) {
        if (pkg == null && actions == null) {
            Slog.i(TAG, "invaild parameter for config proxy bc actions");
            return;
        }
        if (pkg == null) {
            setProxyBCActions(actions);
        } else {
            setAppProxyBCActions(pkg, actions);
        }
    }

    private void configDropBCActionsLocked(String pkg, List<String> actions) {
        if (pkg == null) {
            Slog.e(TAG, "config drop bc actions error");
            return;
        }
        if ("foreground".equals(this.mQueueName)) {
            Slog.i(TAG, pkg + " drop actions:" + actions);
        }
        if (actions == null) {
            this.mAppDropActions.put(pkg, null);
        } else {
            this.mAppDropActions.put(pkg, new ArrayList<>(actions));
        }
    }

    private void configDropBCByPidLocked(String pid, List<String> actions) {
        if (pid == null) {
            Slog.e(TAG, "config drop bc actions by pid error");
            return;
        }
        try {
            Integer iPid = Integer.valueOf(Integer.parseInt(pid));
            if ("foreground".equals(this.mQueueName)) {
                Slog.i(TAG, iPid + " drop actions:" + actions);
            }
            if (actions == null) {
                this.mProcessDropActions.put(iPid, null);
            } else {
                this.mProcessDropActions.put(iPid, new ArrayList<>(actions));
            }
        } catch (Exception e) {
            Slog.w(TAG, e.getMessage());
        }
    }

    private void configMaxProxyBCLocked(String count) {
        if (count == null) {
            Slog.e(TAG, "config max proxy broadcast error");
            return;
        }
        try {
            this.MAX_PROXY_BROADCAST = Integer.parseInt(count);
            if ("foreground".equals(this.mQueueName)) {
                Slog.i(TAG, "set max proxy broadcast :" + this.MAX_PROXY_BROADCAST);
            }
        } catch (Exception e) {
            Slog.w(TAG, e.getMessage());
        }
    }

    private void configSameActionLocked(String action1, List<String> actions) {
        if (action1 == null || actions == null) {
            Slog.e(TAG, "invaild parameter for config same kind action");
            return;
        }
        for (String action2 : actions) {
            this.mSameKindsActionList.put(action1, action2);
        }
    }

    private void clearConfigLocked() {
        if ("foreground".equals(this.mQueueName)) {
            Slog.i(TAG, "clear all config");
        }
        this.mProxyActions.clear();
        this.mAppProxyActions.clear();
        this.mAppDropActions.clear();
        this.mProcessDropActions.clear();
        this.mActionExcludePkgs.clear();
        this.mSameKindsActionList.clear();
        this.mAppAddProxyActions.clear();
        this.mAppRemoveProxyActions.clear();
    }

    private boolean dropActionLocked(String pkg, int pid, BroadcastRecord br) {
        String action = br.intent.getAction();
        if (pid == -1 || isAlivePid(pid)) {
            if (this.mProcessDropActions.containsKey(Integer.valueOf(pid))) {
                ArrayList<String> actions = this.mProcessDropActions.get(Integer.valueOf(pid));
                if (actions == null) {
                    Slog.i(TAG, "process " + pid + " cache, drop all proxy broadcast, now drop :" + br);
                    return true;
                } else if (action != null && actions.contains(action)) {
                    Slog.i(TAG, "process " + pid + " cache, drop list broadcast, now drop :" + br);
                    return true;
                }
            }
            if (this.mAppDropActions.containsKey(pkg)) {
                ArrayList<String> dropActions = this.mAppDropActions.get(pkg);
                if (dropActions == null) {
                    Slog.i(TAG, "pkg " + pkg + " cache, drop all proxy broadcast, now drop " + br);
                    return true;
                } else if (action != null && dropActions.contains(action)) {
                    Slog.i(TAG, "pkg " + pkg + " cache, drop list broadcast, now drop " + br);
                    return true;
                }
            }
            return false;
        }
        Slog.i(TAG, "process " + pid + " has died, drop " + br);
        return true;
    }

    private boolean isAlivePid(int pid) {
        return new File("/proc/" + pid).exists();
    }

    public long proxyBroadcast(List<String> pkgs, boolean proxy) {
        long delay;
        List<String> pkgList;
        if (!mProxyFeature) {
            Slog.w(TAG, "proxy bc not support");
            return -1;
        }
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            StringBuilder sb = new StringBuilder();
            sb.append(proxy ? "proxy " : "unproxy ");
            sb.append(this.mQueueName);
            sb.append(" broadcast  pkgs:");
            sb.append(pkgs);
            Slog.d(TAG, sb.toString());
        }
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                delay = 0;
                boolean pending = this.mPendingBroadcastTimeoutMessage;
                new ArrayList();
                if (proxy) {
                    List<String> pkgList2 = pkgs;
                    for (String pkg : pkgList2) {
                        if (!this.mProxyBroadcastPkgs.contains(pkg)) {
                            this.mProxyBroadcastPkgs.add(pkg);
                        }
                    }
                    if (pending && this.mOrderedBroadcasts.size() > 0) {
                        BroadcastRecord r = (BroadcastRecord) this.mOrderedBroadcasts.get(0);
                        if (r.nextReceiver >= 1) {
                            String pkg2 = getPkg(r.receivers.get(r.nextReceiver - 1));
                            if (pkg2 != null && pkgList2.contains(pkg2)) {
                                delay = this.mTimeoutPeriod;
                            }
                        }
                    }
                } else {
                    if (pkgs != null) {
                        pkgList = pkgs;
                    } else {
                        pkgList = (ArrayList) this.mProxyBroadcastPkgs.clone();
                    }
                    ArrayList<BroadcastRecord> orderedProxyBroadcasts = new ArrayList<>();
                    ArrayList<BroadcastRecord> parallelProxyBroadcasts = new ArrayList<>();
                    proxyBroadcastInnerLocked(this.mParallelPendingBroadcasts, pkgList, parallelProxyBroadcasts);
                    proxyBroadcastInnerLocked(this.mOrderedPendingBroadcasts, pkgList, orderedProxyBroadcasts);
                    this.mProcessDropActions.clear();
                    this.mProxyBroadcastPkgs.removeAll(pkgList);
                    for (String pkg3 : pkgList) {
                        if (this.mProxyPkgsCount.containsKey(pkg3)) {
                            this.mProxyPkgsCount.remove(pkg3);
                        }
                    }
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v(TAG, "unproxy " + this.mQueueName + " Broadcast pkg Parallel Broadcasts (" + this.mParallelBroadcasts + ")");
                    }
                    for (int i = 0; i < parallelProxyBroadcasts.size(); i++) {
                        this.mParallelBroadcasts.add(i, parallelProxyBroadcasts.get(i));
                    }
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST != 0) {
                        Slog.v(TAG, "unproxy " + this.mQueueName + " Broadcast pkg Parallel Broadcasts (" + this.mParallelBroadcasts + ")");
                    }
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v(TAG, "unproxy " + this.mQueueName + " Broadcast pkg Ordered Broadcasts (" + this.mOrderedBroadcasts + ")");
                    }
                    if (pending) {
                        movePendingBroadcastToProxyList(this.mOrderedBroadcasts, orderedProxyBroadcasts, pkgList);
                    }
                    for (int i2 = 0; i2 < orderedProxyBroadcasts.size(); i2++) {
                        if (pending) {
                            this.mOrderedBroadcasts.add(i2 + 1, orderedProxyBroadcasts.get(i2));
                        } else {
                            this.mOrderedBroadcasts.add(i2, orderedProxyBroadcasts.get(i2));
                        }
                    }
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST != 0) {
                        Slog.v(TAG, "unproxy " + this.mQueueName + " Broadcast pkg Ordered Broadcasts (" + this.mOrderedBroadcasts + ")");
                    }
                    if (parallelProxyBroadcasts.size() > 0 || orderedProxyBroadcasts.size() > 0) {
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            Slog.v(TAG, "unproxy " + this.mQueueName + " Broadcast pkg Parallel Broadcasts (" + parallelProxyBroadcasts.size() + ")(" + parallelProxyBroadcasts + ")");
                        }
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            Slog.v(TAG, "unproxy " + this.mQueueName + " Broadcast pkg Ordered Broadcasts (" + orderedProxyBroadcasts.size() + ")(" + orderedProxyBroadcasts + ")");
                        }
                        scheduleBroadcastsLocked();
                    }
                }
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        return delay;
    }

    private void proxyBroadcastInnerLocked(ArrayList<BroadcastRecord> pendingBroadcasts, List<String> unProxyPkgs, ArrayList<BroadcastRecord> unProxyBroadcasts) {
        Iterator it = pendingBroadcasts.iterator();
        while (it.hasNext()) {
            BroadcastRecord br = it.next();
            Object nextReceiver = br.receivers.get(0);
            String proxyPkg = getPkg(nextReceiver);
            if (proxyPkg != null && unProxyPkgs.contains(proxyPkg)) {
                int pid = getPid(nextReceiver);
                it.remove();
                if (!dropActionLocked(proxyPkg, pid, br)) {
                    unProxyBroadcasts.add(br);
                }
            }
        }
    }

    private String getPkg(Object target) {
        if (target instanceof BroadcastFilter) {
            return ((BroadcastFilter) target).packageName;
        }
        if (!(target instanceof ResolveInfo)) {
            return null;
        }
        ResolveInfo info = (ResolveInfo) target;
        if (info.activityInfo == null || info.activityInfo.applicationInfo == null) {
            return null;
        }
        return info.activityInfo.applicationInfo.packageName;
    }

    private int getPid(Object target) {
        if (target instanceof BroadcastFilter) {
            BroadcastFilter filter = (BroadcastFilter) target;
            if (filter.receiverList == null) {
                return -1;
            }
            int pid = filter.receiverList.pid;
            if (pid > 0 || filter.receiverList.app == null) {
                return pid;
            }
            return filter.receiverList.app.pid;
        }
        boolean z = target instanceof ResolveInfo;
        return -1;
    }

    private int getUid(Object target) {
        if (target instanceof BroadcastFilter) {
            BroadcastFilter filter = (BroadcastFilter) target;
            if (filter.receiverList == null) {
                return -1;
            }
            int uid = filter.receiverList.uid;
            if (uid > 0 || filter.receiverList.app == null) {
                return uid;
            }
            return filter.receiverList.app.uid;
        } else if (!(target instanceof ResolveInfo)) {
            return -1;
        } else {
            ResolveInfo info = (ResolveInfo) target;
            if (info.activityInfo == null || info.activityInfo.applicationInfo == null) {
                return -1;
            }
            return info.activityInfo.applicationInfo.uid;
        }
    }

    private void movePendingBroadcastToProxyList(ArrayList<BroadcastRecord> orderedBroadcasts, ArrayList<BroadcastRecord> orderedProxyBroadcasts, List<String> pkgList) {
        int i;
        List<Object> needMoveReceivers;
        List<Object> receivers;
        int numReceivers;
        int recIdx;
        HwBroadcastQueue hwBroadcastQueue = this;
        List<String> list = pkgList;
        if (orderedProxyBroadcasts.size() == 0 || orderedBroadcasts.size() == 0) {
            ArrayList<BroadcastRecord> arrayList = orderedProxyBroadcasts;
            HwBroadcastQueue hwBroadcastQueue2 = hwBroadcastQueue;
            return;
        }
        BroadcastRecord r = orderedBroadcasts.get(0);
        List<Object> needMoveReceivers2 = new ArrayList<>();
        List<Object> receivers2 = r.receivers;
        if (receivers2 == null) {
            ArrayList<BroadcastRecord> arrayList2 = orderedProxyBroadcasts;
            HwBroadcastQueue hwBroadcastQueue3 = hwBroadcastQueue;
            List<Object> list2 = needMoveReceivers2;
            List<Object> list3 = receivers2;
        } else if (list == null) {
            ArrayList<BroadcastRecord> arrayList3 = orderedProxyBroadcasts;
            HwBroadcastQueue hwBroadcastQueue4 = hwBroadcastQueue;
            ArrayList arrayList4 = needMoveReceivers2;
            List<Object> list4 = receivers2;
        } else {
            int recIdx2 = r.nextReceiver;
            int numReceivers2 = receivers2.size();
            int i2 = recIdx2;
            while (i2 < numReceivers2) {
                Object target = receivers2.get(i2);
                String pkg = hwBroadcastQueue.getPkg(target);
                if (pkg == null || !list.contains(pkg)) {
                    ArrayList<BroadcastRecord> arrayList5 = orderedProxyBroadcasts;
                    needMoveReceivers = needMoveReceivers2;
                    receivers = receivers2;
                    recIdx = recIdx2;
                    numReceivers = numReceivers2;
                    i = i2;
                } else {
                    needMoveReceivers2.add(target);
                    List<Object> receiver = new ArrayList<>();
                    receiver.add(target);
                    recIdx = recIdx2;
                    numReceivers = numReceivers2;
                    Object obj = target;
                    String str = pkg;
                    receivers = receivers2;
                    needMoveReceivers = needMoveReceivers2;
                    i = i2;
                    BroadcastRecord r1 = new BroadcastRecord(r.queue, r.intent, r.callerApp, r.callerPackage, r.callingPid, r.callingUid, r.callerInstantApp, r.resolvedType, r.requiredPermissions, r.appOp, r.options, receiver, null, r.resultCode, r.resultData, r.resultExtras, r.ordered, r.sticky, r.initialSticky, r.userId);
                    orderedProxyBroadcasts.add(r1);
                }
                i2 = i + 1;
                recIdx2 = recIdx;
                numReceivers2 = numReceivers;
                receivers2 = receivers;
                needMoveReceivers2 = needMoveReceivers;
                hwBroadcastQueue = this;
                list = pkgList;
                ArrayList<BroadcastRecord> arrayList6 = orderedBroadcasts;
            }
            ArrayList<BroadcastRecord> arrayList7 = orderedProxyBroadcasts;
            List<Object> receivers3 = receivers2;
            int i3 = recIdx2;
            int i4 = numReceivers2;
            List<Object> needMoveReceivers3 = needMoveReceivers2;
            if (needMoveReceivers3.size() > 0) {
                receivers3.removeAll(needMoveReceivers3);
                Slog.v(TAG, "unproxy " + this.mQueueName + ", moving receivers in Ordered Broadcasts (" + r + ") to proxyList, Move receivers : " + needMoveReceivers3);
            }
        }
    }

    public AbsHwMtmBroadcastResourceManager getMtmBRManager() {
        return this.mHwMtmBroadcastResourceManager;
    }

    public boolean getMtmBRManagerEnabled(int featureType) {
        return this.mService.getIawareResourceFeature(featureType) && getMtmBRManager() != null;
    }

    public boolean uploadRadarMessage(int scene, Bundle data) {
        if (scene == 2801) {
            int i = 0;
            while (i < this.mOrderedBroadcasts.size()) {
                try {
                    this.mCopyOrderedBroadcasts.add((BroadcastRecord) this.mOrderedBroadcasts.get(i));
                    i++;
                } catch (Exception e) {
                    Slog.w(TAG, e.getMessage());
                } catch (Throwable th) {
                    this.mCopyOrderedBroadcasts.clear();
                    throw th;
                }
            }
            handleBroadcastQueueOverlength(this.mCopyOrderedBroadcasts);
            this.mCopyOrderedBroadcasts.clear();
            return true;
        } else if (scene != 2803) {
            return false;
        } else {
            handleReceiverTimeOutRadar();
            return true;
        }
    }

    public void enqueueOrderedBroadcastLocked(BroadcastRecord r) {
        super.enqueueOrderedBroadcastLocked(r);
        if (SystemClock.uptimeMillis() > 1800000) {
            int brSize = this.mOrderedBroadcasts.size();
            if (!this.enableUploadRadar && brSize < 150) {
                this.enableUploadRadar = true;
                Flog.i(104, "enable radar when current queue size is " + brSize + ".");
            }
            if (this.enableUploadRadar && brSize >= 150) {
                uploadRadarMessage(HwBroadcastRadarUtil.SCENE_DEF_BROADCAST_OVERLENGTH, null);
                this.enableUploadRadar = false;
                Flog.i(104, "disable radar after radar uploaded, current size is " + brSize + ".");
            }
        }
    }

    private void initActionWhiteList() {
        this.mActionWhiteList.add("android.net.wifi.SCAN_RESULTS");
        this.mActionWhiteList.add("android.net.wifi.WIFI_STATE_CHANGED");
        this.mActionWhiteList.add("android.net.conn.CONNECTIVITY_CHANGE");
        this.mActionWhiteList.add("android.intent.action.TIME_TICK");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v18, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v6, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    private void handleBroadcastQueueOverlength(ArrayList<BroadcastRecord> copyOrderedBroadcasts) {
        String curReceiverName = null;
        String curReceiverPkgName = null;
        boolean isContainsMMS = false;
        Object obj = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        Object obj2 = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        for (int i = 0; i < copyOrderedBroadcasts.size(); i++) {
            BroadcastRecord br = copyOrderedBroadcasts.get(i);
            if (!(br == null || br.intent == null)) {
                if (br.nextReceiver > 0) {
                    Object curReceiver = br.receivers.get(br.nextReceiver - 1);
                    if (curReceiver instanceof BroadcastFilter) {
                        curReceiverPkgName = ((BroadcastFilter) curReceiver).packageName;
                    } else if (curReceiver instanceof ResolveInfo) {
                        ResolveInfo info = (ResolveInfo) curReceiver;
                        if (info.activityInfo != null) {
                            curReceiverName = info.activityInfo.applicationInfo.name;
                            curReceiverPkgName = info.activityInfo.applicationInfo.packageName;
                        }
                    }
                }
                String callerPkg = br.callerPackage;
                String broadcastAction = br.intent.getAction();
                if (MMS_PACKAGE_NAME.equals(callerPkg) || "android.provider.Telephony.SMS_DELIVER".equals(broadcastAction) || "android.provider.Telephony.SMS_RECEIVED".equals(broadcastAction)) {
                    isContainsMMS = true;
                }
                BroadcastRadarRecord broadcastRadarRecord = this.mRadarBroadcastMap.get(broadcastAction);
                if (broadcastRadarRecord == null) {
                    broadcastRadarRecord = new BroadcastRadarRecord(broadcastAction, callerPkg, 0);
                }
                broadcastRadarRecord.count++;
                this.mRadarBroadcastMap.put(broadcastAction, broadcastRadarRecord);
            }
        }
        String mostFrequentAction = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        int maxNum = 0;
        for (Map.Entry<String, BroadcastRadarRecord> curActionEntry : this.mRadarBroadcastMap.entrySet()) {
            int curBroadcastNum = curActionEntry.getValue().count;
            if (curBroadcastNum > maxNum) {
                maxNum = curBroadcastNum;
                mostFrequentAction = curActionEntry.getKey();
            }
        }
        BroadcastRadarRecord brRecord = this.mRadarBroadcastMap.get(mostFrequentAction);
        if (this.mActionWhiteList.contains(brRecord.actionName)) {
            Flog.i(104, "The action[" + brRecord.actionName + "] should be ignored for order broadcast queue overlength.");
            return;
        }
        String versionName = UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
        if (curReceiverPkgName != null) {
            try {
                if (!curReceiverPkgName.isEmpty()) {
                    PackageInfo packageInfo = AppGlobals.getPackageManager().getPackageInfo(curReceiverPkgName, 16384, 0);
                    if (packageInfo != null) {
                        versionName = packageInfo.versionName;
                    }
                }
            } catch (Exception e) {
                Slog.e(TAG, e.getMessage());
            }
        }
        Bundle data = new Bundle();
        data.putString("package", brRecord.packageName);
        data.putString(HwBroadcastRadarUtil.KEY_ACTION, brRecord.actionName);
        data.putInt(HwBroadcastRadarUtil.KEY_ACTION_COUNT, brRecord.count);
        data.putString(HwBroadcastRadarUtil.KEY_RECEIVER, curReceiverName);
        data.putString(HwBroadcastRadarUtil.KEY_VERSION_NAME, versionName);
        data.putBoolean(HwBroadcastRadarUtil.KEY_MMS_BROADCAST_FLAG, isContainsMMS);
        this.mBroadcastRadarUtil.handleBroadcastQueueOverlength(data);
        if (this.mBroadcastMonitor != null) {
            this.mBroadcastMonitor.monitor(907400002, data);
        }
        this.mRadarBroadcastMap.clear();
    }

    private void handleReceiverTimeOutRadar() {
        if (this.mOrderedBroadcasts.size() == 0) {
            Slog.w(TAG, "handleReceiverTimeOutRadar, but mOrderedBroadcasts is empty");
            return;
        }
        BroadcastRecord r = (BroadcastRecord) this.mOrderedBroadcasts.get(0);
        if (r.receivers == null || r.nextReceiver <= 0) {
            Slog.w(TAG, "handleReceiverTimeOutRadar Timeout on receiver, but receiver is invalid.");
            return;
        }
        String pkg = null;
        String receiverName = null;
        String actionName = null;
        int uid = 0;
        long receiverTime = SystemClock.uptimeMillis() - r.receiverTime;
        if (r.intent != null) {
            actionName = r.intent.getAction();
            if (receiverTime < ((r.intent.getFlags() & 268435456) != 0 ? 2000 : 5000)) {
                Slog.w(TAG, "current receiver should not report timeout.");
                return;
            }
        }
        Object curReceiver = r.receivers.get(r.nextReceiver - 1);
        Flog.i(104, "receiver " + curReceiver + " took " + receiverTime + "ms when receive " + r);
        if (curReceiver instanceof BroadcastFilter) {
            pkg = ((BroadcastFilter) curReceiver).packageName;
        } else if (curReceiver instanceof ResolveInfo) {
            ResolveInfo info = (ResolveInfo) curReceiver;
            if (info.activityInfo != null) {
                receiverName = info.activityInfo.name;
                if (info.activityInfo.applicationInfo != null) {
                    uid = info.activityInfo.applicationInfo.uid;
                    pkg = info.activityInfo.applicationInfo.packageName;
                }
            }
        }
        if (SystemClock.uptimeMillis() > 1800000) {
            String versionName = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            if (pkg != null) {
                try {
                    if (!pkg.isEmpty()) {
                        PackageInfo packageInfo = AppGlobals.getPackageManager().getPackageInfo(pkg, 16384, UserHandle.getUserId(uid));
                        if (packageInfo != null) {
                            versionName = packageInfo.versionName;
                        }
                    }
                } catch (Exception e) {
                    Slog.e(TAG, e.getMessage());
                }
            }
            Bundle data = new Bundle();
            data.putString("package", pkg);
            data.putString(HwBroadcastRadarUtil.KEY_RECEIVER, receiverName);
            data.putString(HwBroadcastRadarUtil.KEY_ACTION, actionName);
            data.putString(HwBroadcastRadarUtil.KEY_VERSION_NAME, versionName);
            data.putFloat(HwBroadcastRadarUtil.KEY_RECEIVE_TIME, ((float) receiverTime) / 1000.0f);
            data.putParcelable(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT, r.intent);
            if (this.mBroadcastMonitor != null) {
                this.mBroadcastMonitor.monitor(907400003, data);
            }
            this.mBroadcastRadarUtil.handleReceiverTimeOut(data);
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean dumpLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage, boolean needSep) {
        boolean ret = super.dumpLocked(fd, pw, args, opti, dumpAll, dumpPackage, needSep);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        pw.println();
        if (mProxyFeature) {
            pw.println("  Proxy broadcast [" + this.mQueueName + "] pkg:" + this.mProxyBroadcastPkgs);
            StringBuilder sb = new StringBuilder();
            sb.append("    Default proxy actions :");
            sb.append(this.mProxyActions);
            pw.println(sb.toString());
            pw.println("    APP proxy actions :");
            for (Map.Entry entry : this.mAppProxyActions.entrySet()) {
                String app = (String) entry.getKey();
                Object actions = entry.getValue();
                if (actions == null) {
                    pw.println("        " + app + " null");
                } else {
                    pw.println("        " + app + " " + ((ArrayList) actions));
                }
            }
            pw.println("    Same kind actions :");
            for (Map.Entry entry2 : this.mSameKindsActionList.entrySet()) {
                pw.println("        " + ((String) entry2.getKey()) + " <-> " + ((String) entry2.getValue()));
            }
            pw.println("    APP drop actions :");
            for (Map.Entry entry3 : this.mAppDropActions.entrySet()) {
                String app2 = (String) entry3.getKey();
                Object actions2 = entry3.getValue();
                if (actions2 == null) {
                    pw.println("        " + app2 + " null");
                } else {
                    pw.println("        " + app2 + " " + ((ArrayList) actions2));
                }
            }
            pw.println("    APP add proxy actions :");
            for (Map.Entry entry4 : this.mAppAddProxyActions.entrySet()) {
                pw.println("        " + ((String) entry4.getKey()) + " " + ((ArrayList) entry4.getValue()));
            }
            pw.println("    APP remove proxy actions :");
            for (Map.Entry entry5 : this.mAppRemoveProxyActions.entrySet()) {
                pw.println("        " + ((String) entry5.getKey()) + " " + ((ArrayList) entry5.getValue()));
            }
            pw.println("    Process drop actions :");
            for (Map.Entry entry6 : this.mProcessDropActions.entrySet()) {
                Integer process = (Integer) entry6.getKey();
                Object actions3 = entry6.getValue();
                if (actions3 == null) {
                    pw.println("        " + process + " null");
                } else {
                    pw.println("        " + process + " " + ((ArrayList) actions3));
                }
            }
            pw.println("    Proxy pkgs broadcast count:");
            for (Map.Entry entry7 : this.mProxyPkgsCount.entrySet()) {
                pw.println("        " + ((String) entry7.getKey()) + " " + ((Integer) entry7.getValue()));
            }
            pw.println("    Action exclude pkg:");
            for (Map.Entry entry8 : this.mActionExcludePkgs.entrySet()) {
                pw.println("        " + ((String) entry8.getKey()) + " " + ((Set) entry8.getValue()));
            }
            pw.println("    MAX_PROXY_BROADCAST:" + this.MAX_PROXY_BROADCAST);
            pw.println("  Proxy Parallel Broadcast:" + this.mParallelPendingBroadcasts.size());
            if (this.mParallelPendingBroadcasts.size() <= 20) {
                Iterator<BroadcastRecord> it = this.mParallelPendingBroadcasts.iterator();
                while (it.hasNext()) {
                    it.next().dump(pw, "    ", sdf);
                }
            }
            pw.println("  Proxy Ordered Broadcast:" + this.mOrderedPendingBroadcasts.size());
            if (this.mOrderedPendingBroadcasts.size() <= 20) {
                Iterator<BroadcastRecord> it2 = this.mOrderedPendingBroadcasts.iterator();
                while (it2.hasNext()) {
                    it2.next().dump(pw, "    ", sdf);
                }
            }
        }
        return ret;
    }

    public ArrayList<Integer> getIawareDumpData() {
        ArrayList<Integer> queueSizes = new ArrayList<>();
        queueSizes.add(Integer.valueOf(this.mParallelBroadcasts.size()));
        queueSizes.add(Integer.valueOf(this.mOrderedBroadcasts.size()));
        return queueSizes;
    }
}

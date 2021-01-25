package com.android.server.rms.iaware.memory.policy;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwareCMSManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.iaware.AwareServiceThread;
import com.huawei.internal.os.BackgroundThreadEx;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

public class CachedMemoryCleanPolicy {
    private static final String BUNDLE_KEY_PKG = "PACKAGE";
    private static final String BUNDLE_KEY_PROC = "PROCESS";
    private static final boolean DEBUG = SystemPropertiesEx.getBoolean("persist.sys.iaware.cachedmemory.debug", false);
    private static final int KILL_START_MAX_COUNT = 3;
    private static final long KILL_START_MAX_TIME = 10000;
    private static final Object LOCK = new Object();
    private static final int MSG_APP_UPDATE = 3;
    private static final int MSG_FG_CHANGED = 5;
    private static final int MSG_PROC_START = 0;
    private static final int MSG_RECG_RECORD = 1;
    private static final int MSG_RECG_RESULT = 2;
    private static final int MSG_USER_SWITCH = 4;
    private static final String SEPARATOR = "#";
    private static final String TAG = "CachedMemory";
    private static CachedMemoryCleanPolicy sInstance;
    private boolean mCachedMemoryEnable = false;
    private final ArraySet<Integer> mFgUids = new ArraySet<>();
    private MyHandler mHandler;
    private boolean mIsAbroadArea = AwareDefaultConfigList.isAbroadArea();
    private int mKillStartMaxCount = 3;
    private long mKillStartMaxTime = 10000;
    private final ArraySet<String> mProtectProc = new ArraySet<>();
    private final ArrayMap<String, ArrayMap<String, Integer>> mRecgRecord = new ArrayMap<>();
    private final ArrayMap<String, ArrayMap<String, Long>> mRecordTemp = new ArrayMap<>();

    /* access modifiers changed from: private */
    public class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg == null) {
                AwareLog.w(CachedMemoryCleanPolicy.TAG, "msg is null");
                return;
            }
            int i = msg.what;
            if (i == 0) {
                CachedMemoryCleanPolicy.this.handlerProcStart(msg);
            } else if (i == 1) {
                CachedMemoryCleanPolicy.this.handlerRecgRecord(msg);
            } else if (i == 2) {
                CachedMemoryCleanPolicy.this.handlerRecgResult(msg);
            } else if (i == 3) {
                CachedMemoryCleanPolicy.this.handlerForAppUpdate(msg);
            } else if (i == 4) {
                CachedMemoryCleanPolicy.this.handlerForAppSwitch();
            } else if (i == 5) {
                CachedMemoryCleanPolicy.this.handlerForFgChanged(msg);
            }
        }
    }

    private CachedMemoryCleanPolicy() {
    }

    public static CachedMemoryCleanPolicy getInstance() {
        CachedMemoryCleanPolicy cachedMemoryCleanPolicy;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new CachedMemoryCleanPolicy();
            }
            cachedMemoryCleanPolicy = sInstance;
        }
        return cachedMemoryCleanPolicy;
    }

    public void initialize() {
        AwareLog.i(TAG, "initialize.");
        if (!this.mIsAbroadArea && initHandler()) {
            loadConfig();
        }
    }

    public void deInitialize() {
        AwareLog.i(TAG, "deInitialize.");
        this.mCachedMemoryEnable = false;
    }

    private void loadConfig() {
        AwareConfig.Item curMemItem;
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.CACHED_MOMORY_CLEAN);
        if (configList != null && (curMemItem = MemoryUtils.getCurrentMemItem(configList, true)) != null) {
            loadDataFromCurMemItem(curMemItem);
        }
    }

    private AwareConfig getConfig(String featureName, String configName) {
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                return IAwareCMSManager.getConfig(awareService, featureName, configName);
            }
            AwareLog.w(TAG, "can not find service awareService.");
            return null;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "MemoryFeature getConfig RemoteException");
            return null;
        }
    }

    private void loadDataFromCurMemItem(AwareConfig.Item item) {
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            if (subItem != null) {
                String itemName = subItem.getName();
                String itemValue = subItem.getValue();
                if (!(itemName == null || itemValue == null)) {
                    char c = 65535;
                    int hashCode = itemName.hashCode();
                    if (hashCode != -1141965361) {
                        if (hashCode != -870986281) {
                            if (hashCode == 1072041485 && itemName.equals(MemoryConstant.CACHED_MOMORY_KILL_START_MAX_TIME)) {
                                c = 1;
                            }
                        } else if (itemName.equals(MemoryConstant.CACHED_MEMORY_SWITCH)) {
                            c = 0;
                        }
                    } else if (itemName.equals(MemoryConstant.CACHED_MOMORY_KILL_START_MAX_COUNT)) {
                        c = 2;
                    }
                    if (c == 0) {
                        this.mCachedMemoryEnable = "1".equals(itemValue.trim());
                    } else if (c == 1) {
                        try {
                            long time = Long.parseLong(itemValue.trim());
                            if (time >= 0) {
                                this.mKillStartMaxTime = time;
                            }
                        } catch (NumberFormatException e) {
                            AwareLog.e(TAG, "loadDataFromCurMemItem NumberFormatException");
                        }
                    } else if (c == 2) {
                        try {
                            int count = Integer.parseInt(itemValue.trim());
                            if (count >= 0) {
                                this.mKillStartMaxCount = count;
                            }
                        } catch (NumberFormatException e2) {
                            AwareLog.e(TAG, "loadDataFromCurMemItem NumberFormatException");
                        }
                    }
                }
            }
        }
    }

    private boolean initHandler() {
        Looper looper = AwareServiceThread.getInstance().getLooper();
        if (looper != null) {
            this.mHandler = new MyHandler(looper);
            return true;
        }
        Looper looper2 = BackgroundThreadEx.getLooper();
        if (looper2 == null) {
            return false;
        }
        this.mHandler = new MyHandler(looper2);
        return true;
    }

    public boolean isCachedMemoryEnable() {
        return this.mCachedMemoryEnable;
    }

    public void dumpInfo(PrintWriter pw) {
        if (pw != null) {
            pw.println("mIsAbroadArea : " + this.mIsAbroadArea);
            pw.println("mCachedMemoryEnable : " + this.mCachedMemoryEnable);
            pw.println("DEBUG : " + DEBUG);
            pw.println("mKillStartMaxTime : " + this.mKillStartMaxTime);
            pw.println("mKillStartMaxCount : " + this.mKillStartMaxCount);
            pw.println("[mProtectProc:]");
            synchronized (this.mProtectProc) {
                Iterator<String> it = this.mProtectProc.iterator();
                while (it.hasNext()) {
                    pw.println(it.next());
                }
            }
            pw.println("[mRecgRecord:]");
            synchronized (this.mRecgRecord) {
                pw.println(this.mRecgRecord);
            }
            pw.println("[mRecordTemp:]");
            synchronized (this.mRecordTemp) {
                for (Map.Entry<String, ArrayMap<String, Long>> pkgRecordInfo : this.mRecordTemp.entrySet()) {
                    dumpPkgRecordInfoLock(pkgRecordInfo, pw);
                }
            }
        }
    }

    private void dumpPkgRecordInfoLock(Map.Entry<String, ArrayMap<String, Long>> pkgRecordInfo, PrintWriter pw) {
        if (pkgRecordInfo != null) {
            String pkg = pkgRecordInfo.getKey();
            ArrayMap<String, Long> procInfo = pkgRecordInfo.getValue();
            if (!(pkg == null || procInfo == null || procInfo.isEmpty())) {
                pw.println(" pkg : " + pkg);
                for (Map.Entry<String, Long> entry : procInfo.entrySet()) {
                    if (entry != null) {
                        String proc = entry.getKey();
                        Long time = entry.getValue();
                        if (!(proc == null || time == null)) {
                            long delTime = System.currentTimeMillis() - time.longValue();
                            pw.println("  " + proc + " : " + delTime);
                        }
                    }
                }
            }
        }
    }

    public boolean isCachedProtect(String pkg, String proc) {
        boolean contains;
        if (!this.mCachedMemoryEnable || pkg == null || proc == null) {
            return false;
        }
        synchronized (this.mProtectProc) {
            ArraySet<String> arraySet = this.mProtectProc;
            contains = arraySet.contains(pkg + "#" + proc);
        }
        return contains;
    }

    public boolean isFilterUid(int uid) {
        synchronized (this.mFgUids) {
            if (this.mFgUids.contains(Integer.valueOf(uid))) {
                return true;
            }
        }
        if (uid <= 10000) {
            return true;
        }
        return AwareAppAssociate.getInstance().isSystemUnRemoveApp(uid);
    }

    public void notifyProcessStart(String pkgName, String process, int uid) {
        if (this.mCachedMemoryEnable && this.mHandler != null && !isFilterUid(uid) && !isCachedProtect(pkgName, process)) {
            Bundle bundle = new Bundle();
            bundle.putString(BUNDLE_KEY_PKG, pkgName);
            bundle.putString(BUNDLE_KEY_PROC, process);
            Message msg = this.mHandler.obtainMessage(0);
            msg.arg1 = uid;
            msg.setData(bundle);
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerProcStart(Message msg) {
        Bundle bundle = msg.getData();
        if (bundle != null) {
            int userId = UserHandleEx.getUserId(msg.arg1);
            String pkg = bundle.getString(BUNDLE_KEY_PKG);
            String proc = bundle.getString(BUNDLE_KEY_PROC);
            if (pkg != null && proc != null) {
                synchronized (this.mRecordTemp) {
                    String pkgInfo = "" + userId + "#" + pkg;
                    ArrayMap<String, Long> procInfo = this.mRecordTemp.get(pkgInfo);
                    if (procInfo != null) {
                        Long time = procInfo.get(proc);
                        if (time != null) {
                            if (System.currentTimeMillis() - time.longValue() < this.mKillStartMaxTime) {
                                sendRecgRecordMsg(bundle);
                            }
                            procInfo.remove(proc);
                            if (procInfo.isEmpty()) {
                                this.mRecordTemp.remove(pkgInfo);
                            } else {
                                this.mRecordTemp.put(pkgInfo, procInfo);
                            }
                        }
                    }
                }
            }
        }
    }

    private void sendRecgRecordMsg(Bundle bundle) {
        MyHandler myHandler = this.mHandler;
        if (myHandler != null) {
            Message msg = myHandler.obtainMessage(1);
            msg.setData(bundle);
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerRecgRecord(Message msg) {
        int newCount;
        Bundle bundle = msg.getData();
        if (bundle != null) {
            String pkg = bundle.getString(BUNDLE_KEY_PKG);
            String proc = bundle.getString(BUNDLE_KEY_PROC);
            if (pkg != null && proc != null) {
                synchronized (this.mRecgRecord) {
                    ArrayMap<String, Integer> procInfo = this.mRecgRecord.get(pkg);
                    if (procInfo == null) {
                        procInfo = new ArrayMap<>();
                    }
                    Integer count = procInfo.get(proc);
                    if (count == null) {
                        newCount = 1;
                    } else {
                        newCount = count.intValue() + 1;
                    }
                    if (newCount < this.mKillStartMaxCount) {
                        procInfo.put(proc, Integer.valueOf(newCount));
                        this.mRecgRecord.put(pkg, procInfo);
                    } else {
                        procInfo.remove(proc);
                        if (procInfo.isEmpty()) {
                            this.mRecgRecord.remove(pkg);
                        } else {
                            this.mRecgRecord.put(pkg, procInfo);
                        }
                        sendRecgResultMsg(bundle);
                    }
                }
            }
        }
    }

    private void sendRecgResultMsg(Bundle bundle) {
        MyHandler myHandler = this.mHandler;
        if (myHandler != null) {
            Message msg = myHandler.obtainMessage(2);
            msg.setData(bundle);
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerRecgResult(Message msg) {
        Bundle bundle = msg.getData();
        if (bundle != null) {
            String pkg = bundle.getString(BUNDLE_KEY_PKG);
            String proc = bundle.getString(BUNDLE_KEY_PROC);
            if (pkg != null && proc != null) {
                synchronized (this.mProtectProc) {
                    ArraySet<String> arraySet = this.mProtectProc;
                    arraySet.add(pkg + "#" + proc);
                }
            }
        }
    }

    public void reportAppUpdate(int eventId, Bundle args) {
        MyHandler myHandler;
        if (this.mCachedMemoryEnable && args != null && (myHandler = this.mHandler) != null) {
            Message msg = myHandler.obtainMessage();
            msg.what = 3;
            msg.arg1 = eventId;
            msg.setData(args);
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerForAppUpdate(Message msg) {
        String pkgName;
        Bundle args = msg.getData();
        if (args != null) {
            int i = msg.arg1;
            if ((i == 1 || i == 2) && (pkgName = args.getString(AwareUserHabit.USER_HABIT_PACKAGE_NAME)) != null) {
                clearDataForPkg(pkgName);
            }
        }
    }

    private void clearDataForPkg(String pkgName) {
        synchronized (this.mProtectProc) {
            Iterator<String> protectProcIt = this.mProtectProc.iterator();
            while (protectProcIt.hasNext()) {
                String str = protectProcIt.next();
                if (str != null && str.contains(pkgName)) {
                    protectProcIt.remove();
                }
            }
        }
        clearRecordTmpData(pkgName);
        synchronized (this.mRecgRecord) {
            this.mRecgRecord.remove(pkgName);
        }
    }

    private void clearRecordTmpData(String pkgName) {
        if (pkgName != null) {
            synchronized (this.mRecordTemp) {
                Iterator<Map.Entry<String, ArrayMap<String, Long>>> recordTempIt = this.mRecordTemp.entrySet().iterator();
                while (recordTempIt.hasNext()) {
                    String name = recordTempIt.next().getKey();
                    if (name != null && name.contains(pkgName)) {
                        recordTempIt.remove();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerForAppSwitch() {
        synchronized (this.mRecordTemp) {
            this.mRecordTemp.clear();
        }
        synchronized (this.mRecgRecord) {
            this.mRecgRecord.clear();
        }
    }

    public void initUserSwitch() {
        MyHandler myHandler;
        if (this.mCachedMemoryEnable && (myHandler = this.mHandler) != null) {
            Message msg = myHandler.obtainMessage();
            msg.what = 4;
            this.mHandler.sendMessage(msg);
        }
    }

    public void updateCachedMemoryCleanRecord(int uid, String pkg, String procName, boolean isAdd) {
        if (this.mCachedMemoryEnable && pkg != null && procName != null && !isFilterUid(uid)) {
            int userId = UserHandleEx.getUserId(uid);
            synchronized (this.mRecordTemp) {
                String pkgInfo = "" + userId + "#" + pkg;
                ArrayMap<String, Long> procInfo = this.mRecordTemp.get(pkgInfo);
                if (procInfo == null) {
                    procInfo = new ArrayMap<>();
                }
                if (isAdd) {
                    procInfo.put(procName, Long.valueOf(System.currentTimeMillis()));
                } else {
                    procInfo.remove(procName);
                }
                if (procInfo.isEmpty()) {
                    this.mRecordTemp.remove(pkgInfo);
                } else {
                    this.mRecordTemp.put(pkgInfo, procInfo);
                }
            }
        }
    }

    public void dumpCachedKill(PrintWriter pw, int pid) {
        if (pw != null) {
            if (ProcessCleaner.getInstance().killProcess(pid, true, "CachedClean")) {
                pw.println("Kill " + pid + " successful!");
                return;
            }
            pw.println("Kill " + pid + " fail!");
        }
    }

    public void dumpCachedKillWithRecord(PrintWriter pw, int pid, int uid, String pkg, String proc) {
        if (pw != null) {
            updateCachedMemoryCleanRecord(uid, pkg, proc, true);
            if (ProcessCleaner.getInstance().killProcess(pid, true, "CachedClean")) {
                pw.println("Kill " + pid + " successful!");
                return;
            }
            updateCachedMemoryCleanRecord(uid, pkg, proc, false);
            pw.println("Kill " + pid + " fail!");
        }
    }

    public boolean needPrintLog() {
        return this.mCachedMemoryEnable && DEBUG;
    }

    public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        if (this.mCachedMemoryEnable) {
            if (foregroundActivities) {
                synchronized (this.mFgUids) {
                    this.mFgUids.add(Integer.valueOf(uid));
                }
                sendFgChangedMsg(pid);
                return;
            }
            synchronized (this.mFgUids) {
                this.mFgUids.remove(Integer.valueOf(uid));
            }
        }
    }

    private void sendFgChangedMsg(int pid) {
        MyHandler myHandler = this.mHandler;
        if (myHandler != null) {
            Message msg = myHandler.obtainMessage();
            msg.what = 5;
            msg.arg1 = pid;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerForFgChanged(Message msg) {
        clearRecordTmpData(InnerUtils.getAwarePkgName(msg.arg1));
    }
}

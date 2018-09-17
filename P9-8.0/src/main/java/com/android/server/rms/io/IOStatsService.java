package com.android.server.rms.io;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.rms.io.IIOStatsServiceManager.Stub;
import android.rms.utils.Utils;
import android.util.Log;
import android.util.SparseArray;
import com.android.server.rms.CompactJobService;
import com.android.server.rms.defraggler.IODefraggler;
import com.android.server.rms.io.IOFileRotator.Rewriter;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import libcore.io.IoUtils;

public class IOStatsService extends Stub {
    private static final String DUMP_ALL_TAG = "ALL";
    private static final boolean DUMP_BEFORE_DELETE = true;
    private static final String DUMP_PENDING_TAG = "PENDING";
    private static final String DUMP_TYPE_FULL = "--full";
    private static final String DUMP_TYPE_PENDING = "--pending";
    private static final Object[] EMPTY_OBJECT = new Object[0];
    private static final int INSTALL_TYPE_HW_PREINSTALL = 1;
    private static final int INSTALL_TYPE_INVALID = -1;
    private static final int INSTALL_TYPE_SYSTEM_APP = 2;
    private static final int INSTALL_TYPE_THIRDPARTY_APP = 3;
    private static final String IOS_STATS_FILE_PREFIX = "io_stats";
    private static final long IO_STATS_FILE_DELETED_PERIOD = 864000000;
    private static final long IO_STATS_FILE_ROTATE_PERIOD = 86400000;
    private static final String IO_STATS_PATH = "data/system/iostats";
    private static final int MSG_INIT = 1;
    private static final int MSG_LOAD_INIT_DATA = 3;
    private static final int MSG_READ_IO_STATS = 6;
    private static final int MSG_SAVE_FORCE_WRITE = 4;
    private static final int MSG_SCREEN_OFF_SAVE = 5;
    private static final int MSG_STATS_MONITOR = 2;
    private static final int NOT_3RD_PARTY_UID = -1;
    private static final long PERIOD_TIME_READ_STATS = 3600000;
    public static final int QUERY_RESULT_FAIL = -1;
    public static final int QUERY_TYPE_READ = 1;
    public static final int QUERY_TYPE_READ_WRITE = 3;
    public static final int QUERY_TYPE_WRITE = 2;
    private static final long SAVE_PERIOD_TIME = 3600000;
    private static final String TAG = "RMS.IO.IOStatsService";
    private static final String TAG_IOSTATS_DUMP = "iostats_dump";
    private static IOStatsService mIOStatsService = null;
    private static final Object mLock = new Object();
    private List<Integer> mAllUidsMonitoredList = new ArrayList();
    private IOStatsCollection mCompleteCollection = null;
    private Context mContext = null;
    private DropBoxManager mDropBox;
    private Handler mHandler = null;
    private Callback mHandlerCallback = new Callback() {
        public boolean handleMessage(Message msg) {
            if (msg == null) {
                Log.e(IOStatsService.TAG, "handleMessage,the msg is null");
                return false;
            }
            switch (msg.what) {
                case 1:
                    IOStatsService.this.handleInitService();
                    return true;
                case 3:
                    IOStatsService.this.loadIOStatsFromDisk();
                    IOStatsService.this.loadAllUidsMonitored();
                    IOStatsService.this.writeMonitoredUidsToKernel();
                    CompactJobService.addDefragglers(new IODefraggler());
                    return true;
                case 4:
                    IOStatsService.this.saveIOStatsAndLatestUids(true);
                    return true;
                case 5:
                    IOStatsService.this.saveIOStatsAndLatestUids(false);
                    IOStatsService.this.mLastScreenOffTime = System.currentTimeMillis();
                    return true;
                case 6:
                    IOStatsService.this.periodReadTask();
                    IOStatsService.this.periodRunReadTask();
                    return true;
                default:
                    return true;
            }
        }
    };
    private IOExceptionHandle mIOExceptionHandle = null;
    private boolean mIsServiceReady = false;
    private long mLastScreenOffTime = 0;
    private IOStatsCollection mLastSnapShotCollecton = null;
    private Looper mLooper = null;
    private PackageManager mPM = null;
    private BroadcastReceiver mPackageReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            IOStatsService.this.notifyUidChanged(false, intent, "PackageReceiver");
        }
    };
    private List<Integer> mPendingAddUidList = new ArrayList();
    private IOStatsCollection mPendingCollection = null;
    private List<Integer> mPendingDeleteUidList = new ArrayList();
    private CombiningRewriter mPendingRewriter = null;
    private List<Integer> mQueryTypeList = null;
    private IOFileRotator mRotator = null;
    private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i(IOStatsService.TAG, "onReceive:screen off");
            IOStatsService.this.mHandler.sendEmptyMessage(5);
        }
    };
    private BroadcastReceiver mShutdownReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i(IOStatsService.TAG, "onReceive:shut down");
            IOStatsService.this.readStatsFromKernel();
            IOStatsService.this.mHandler.sendEmptyMessage(4);
        }
    };
    private Hashtable<Integer, String> mUidPkgTable = new Hashtable();
    private BroadcastReceiver mUidRemoveReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            IOStatsService.this.notifyUidChanged(true, intent, "UidRemoveReceiver");
        }
    };

    private static class CombiningRewriter implements Rewriter {
        private final IOStatsCollection mCollection;

        public CombiningRewriter(IOStatsCollection collection) {
            this.mCollection = collection;
        }

        public void reset() {
        }

        public void read(InputStream in) throws IOException {
            this.mCollection.read(in);
        }

        public boolean shouldWrite() {
            return true;
        }

        public void write(OutputStream out) throws IOException {
            this.mCollection.write(new DataOutputStream(out));
        }
    }

    private IOStatsService(Context context, Looper looper) {
        Log.i(TAG, "IOStatsService construct");
        this.mContext = context;
        this.mLooper = looper;
        this.mQueryTypeList = new ArrayList();
        this.mQueryTypeList.add(Integer.valueOf(1));
        this.mQueryTypeList.add(Integer.valueOf(2));
        this.mQueryTypeList.add(Integer.valueOf(3));
    }

    public static IOStatsService getInstance(Context context, Looper looper) {
        IOStatsService iOStatsService;
        synchronized (mLock) {
            if (mIOStatsService == null) {
                mIOStatsService = new IOStatsService(context, looper);
                Log.i(TAG, "add the IOStatsService");
                ServiceManager.addService("iostatsservice", mIOStatsService);
            }
            iOStatsService = mIOStatsService;
        }
        return iOStatsService;
    }

    public void startIOStatsService() {
        Log.i(TAG, "startIOStatsService");
        if (this.mLooper == null) {
            Log.e(TAG, "startIOStatsService,the looper is null");
            return;
        }
        if (this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages(null);
        }
        this.mHandler = new Handler(this.mLooper, this.mHandlerCallback);
        this.mHandler.sendEmptyMessage(1);
    }

    private void periodRunReadTask() {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(6), WifiProCommonUtils.RECHECK_DELAYED_MS);
    }

    private void handleInitService() {
        Log.i(TAG, "initialize the Service");
        if (this.mContext == null) {
            Log.e(TAG, "handleInitService,the context is null");
            return;
        }
        try {
            this.mIOExceptionHandle = new IOExceptionHandle(this);
            this.mRotator = new IOFileRotator(new File(IO_STATS_PATH), IOS_STATS_FILE_PREFIX, 86400000, IO_STATS_FILE_DELETED_PERIOD);
            this.mRotator.removeFilesWhenOverFlow();
            this.mDropBox = (DropBoxManager) this.mContext.getSystemService("dropbox");
            this.mPM = this.mContext.getPackageManager();
            this.mPendingCollection = new IOStatsCollection();
            this.mPendingRewriter = new CombiningRewriter(this.mPendingCollection);
            this.mCompleteCollection = new IOStatsCollection();
            this.mLastSnapShotCollecton = new IOStatsCollection();
            this.mHandler.sendEmptyMessage(3);
            this.mContext.registerReceiver(this.mScreenOffReceiver, new IntentFilter("android.intent.action.SCREEN_OFF"), null, this.mHandler);
            IntentFilter packageFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            packageFilter.addDataScheme("package");
            this.mContext.registerReceiver(this.mPackageReceiver, packageFilter, null, this.mHandler);
            this.mContext.registerReceiver(this.mUidRemoveReceiver, new IntentFilter("android.intent.action.UID_REMOVED"), null, this.mHandler);
            this.mContext.registerReceiver(this.mShutdownReceiver, new IntentFilter("android.intent.action.ACTION_SHUTDOWN"), null, this.mHandler);
            periodRunReadTask();
            this.mIsServiceReady = true;
        } catch (Exception ex) {
            Log.e(TAG, "handleInitService,error message:" + ex.getMessage());
        }
    }

    public int query(int uid) throws RemoteException {
        return 0;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext == null) {
            Log.e(TAG, "dump,the context is null");
        } else if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump IOStatsService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        } else if (Binder.getCallingUid() > 1000) {
            dumpIOStatsFromDisk(pw, args);
        }
    }

    private void dumpIOStatsFromDisk(PrintWriter pw, String[] args) {
        if (args == null || args.length == 0) {
            Log.e(TAG, "dumpIOStatsFromDisk,no argument");
            return;
        }
        try {
            pw.println("begin dumpIOStatsFromDisk");
            for (String argValue : args) {
                dumpIOStatsCollection(argValue, pw);
            }
            pw.println("end dumpIOStatsFromDisk");
        } catch (RuntimeException e) {
            pw.println("RuntimeException,fail to dumpIOStatsFromDisk:" + e.getMessage());
        } catch (Exception e2) {
            pw.println("fail to dumpIOStatsFromDisk");
        }
    }

    private void dumpIOStatsCollection(String tag, PrintWriter pw) throws IOException {
        if (DUMP_TYPE_FULL.equals(tag) || (DUMP_TYPE_PENDING.equals(tag) ^ 1) == 0) {
            IOStatsCollection collection;
            if (DUMP_TYPE_FULL.equals(tag)) {
                collection = new IOStatsCollection();
                this.mRotator.readMatching(collection, Long.MIN_VALUE, Long.MAX_VALUE);
            } else {
                collection = this.mPendingCollection;
            }
            if (collection == null) {
                pw.println(tag + " is empty");
            } else {
                collection.dump(pw);
            }
        }
    }

    public SparseArray<IOStatsHistory> getAllIOStatsCollection() {
        if (this.mIsServiceReady) {
            IOStatsCollection allCollection = this.mCompleteCollection.clone();
            allCollection.addHistories(this.mPendingCollection.clone());
            return allCollection.getIOStatsHistoryMap();
        }
        Log.e(TAG, "getAllIOStatsCollection,the service is not ready");
        return null;
    }

    private void readStatsFromKernel() {
        Log.i(TAG, "readStatsFromKernel");
        if (this.mIsServiceReady) {
            IOStatsCollection collection = KernelIOStats.readUidIOStatsFromKernel(this.mUidPkgTable);
            if (collection.getTotalBytes() == 0) {
                Log.i(TAG, "readStatsFromKernel,the IO Stats information is empty ");
                return;
            }
            SparseArray<IOStatsHistory> currentIOStats = collection.getIOStatsHistoryMap();
            SparseArray<IOStatsHistory> lastIOStats = this.mLastSnapShotCollecton.getIOStatsHistoryMap();
            int curIOStatsSize = currentIOStats.size();
            for (int index = 0; index < curIOStatsSize; index++) {
                int uid = currentIOStats.keyAt(index);
                IOStatsHistory additionHistory = ((IOStatsHistory) currentIOStats.get(uid)).subtractFirstEntry((IOStatsHistory) lastIOStats.get(uid));
                if (additionHistory != null) {
                    this.mPendingCollection.recordHistory(additionHistory);
                    if (Utils.DEBUG) {
                        Log.d(TAG, "add uid:" + uid + ",totalBytes in pending is " + this.mPendingCollection.getTotalBytes());
                    }
                }
            }
            this.mLastSnapShotCollecton = collection;
            return;
        }
        Log.e(TAG, "readStatsFromKernel,the service is not ready");
    }

    public void periodReadTask() {
        readStatsFromKernel();
    }

    public boolean periodMonitorTask() {
        if (this.mIsServiceReady) {
            Log.i(TAG, "do the periodMonitorTask");
            ExceptionData exceptionData = this.mIOExceptionHandle.checkIfExistException();
            if (exceptionData == null) {
                Log.i(TAG, "periodMonitorTask,no exception occurs");
                return true;
            }
            this.mIOExceptionHandle.handleIOException(exceptionData);
            return true;
        }
        Log.e(TAG, "periodMonitorTask,the service isn't ready");
        return false;
    }

    public void interruptMonitorTask() {
        if (this.mIsServiceReady) {
            Log.i(TAG, "do the interruptMonitorTask");
            this.mIOExceptionHandle.interrupt();
            return;
        }
        Log.e(TAG, "interruptMonitorTask,the service isn't ready");
    }

    public int query(long startTime, long endTime, int uid, String packageName, int queryType) {
        if (!this.mIsServiceReady) {
            Log.e(TAG, "query,the service is not ready");
            return -1;
        } else if (startTime > endTime) {
            Log.e(TAG, String.format("query,starttime is bigger than endtime:time period:%d - %d", new Object[]{Long.valueOf(startTime), Long.valueOf(endTime)}));
            return -1;
        } else if (this.mQueryTypeList.contains(Integer.valueOf(queryType))) {
            int uidQuery;
            if (uid > 0) {
                uidQuery = uid;
            } else {
                uidQuery = getUidByPackageName(packageName);
            }
            IOStatsHistory history = (IOStatsHistory) this.mCompleteCollection.getIOStatsHistoryMap().get(uidQuery);
            if (history != null) {
                return history.queryNumberOfAccessingIO(queryType, startTime, endTime);
            }
            Log.e(TAG, "not find any history of uid:" + uid + ",package:" + packageName);
            return -1;
        } else {
            Log.e(TAG, String.format("query,querytype is invalid:%d", new Object[]{Integer.valueOf(queryType)}));
            return -1;
        }
    }

    private int getUidByPackageName(String packageName) {
        if (packageName == null || packageName.length() == 0) {
            return 0;
        }
        int uidQuery = 0;
        for (Entry<Integer, String> keyPair : this.mUidPkgTable.entrySet()) {
            if (packageName.equals(keyPair.getValue())) {
                uidQuery = ((Integer) keyPair.getKey()).intValue();
                break;
            }
        }
        Log.i(TAG, "query,the uid found is " + uidQuery + ",package:" + packageName);
        return uidQuery;
    }

    private void loadIOStatsFromDisk() {
        try {
            Log.i(TAG, "loadIOStatsFromDisk");
            this.mRotator.readMatching(this.mCompleteCollection, Long.MIN_VALUE, Long.MAX_VALUE);
        } catch (IOException e) {
            Log.wtf(TAG, "fail to loadIOStatsFromDisk,IO Exception occurs", e);
            recoverFromWtf();
        } catch (OutOfMemoryError e2) {
            Log.wtf(TAG, "fail to loadIOStatsFromDisk,OutOfMemoryError occurs", e2);
            recoverFromWtf();
        }
    }

    private void recoverFromWtf() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            this.mRotator.dumpAll(os);
        } catch (IOException e) {
            os.reset();
        } finally {
            IoUtils.closeQuietly(os);
        }
        if (this.mDropBox != null) {
            this.mDropBox.addData(TAG_IOSTATS_DUMP, os.toByteArray(), 0);
        }
        this.mRotator.deleteAll();
    }

    private void loadAllUidsMonitored() {
        Log.i(TAG, "loadAllUidsMonitored");
        if (this.mPM == null) {
            Log.e(TAG, "loadAllUidsMonitored,the PowerManager is null");
            return;
        }
        this.mAllUidsMonitoredList.clear();
        this.mUidPkgTable.clear();
        addThirdPartyUid(this.mPM.getInstalledApplications(0));
        Log.i(TAG, String.format("loadAllUidsMonitored, the number of third party's apks is %d", new Object[]{Integer.valueOf(this.mAllUidsMonitoredList.size())}));
    }

    private void addThirdPartyUid(List<ApplicationInfo> appList) {
        if (appList == null || appList.size() == 0) {
            Log.e(TAG, "no app is installed");
            return;
        }
        for (ApplicationInfo appInfo : appList) {
            if (appInfo != null && checkIfThirdPartyApp(appInfo.packageName)) {
                Log.d(TAG, "third party apk,packageName:" + appInfo.packageName + ",uid:" + appInfo.uid);
                this.mAllUidsMonitoredList.add(Integer.valueOf(appInfo.uid));
                this.mPendingAddUidList.add(Integer.valueOf(appInfo.uid));
                if (!this.mUidPkgTable.containsKey(Integer.valueOf(appInfo.uid))) {
                    this.mUidPkgTable.put(Integer.valueOf(appInfo.uid), appInfo.packageName);
                }
            }
        }
    }

    private boolean checkIfThirdPartyApp(String packageName) {
        boolean z = true;
        if (packageName == null || packageName.isEmpty()) {
            Log.e(TAG, "checkIfThirdPartyApp, the packageName is empty");
            return false;
        }
        try {
            String[] splitArray = packageName.split(":");
            if (splitArray.length == 0) {
                Log.e(TAG, "checkIfThirdPartyApp,packageName :" + packageName + " is invalid");
                return false;
            }
            int installType = getAppType(this.mPM.getApplicationInfo(splitArray.length > 1 ? splitArray[1] : splitArray[0], 0));
            if (!(installType == 1 || installType == 3)) {
                z = false;
            }
            return z;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "checkIfThirdPartyApp," + packageName + " not found in the apklist");
            return false;
        } catch (Exception e2) {
            Log.e(TAG, "checkIfThirdPartyApp,the Other Exception occurs,:" + packageName);
            return false;
        }
    }

    private int getAppType(ApplicationInfo info) {
        if (info == null) {
            Log.e(TAG, "getAppType,the ApplicationInfo is null ");
            return -1;
        }
        try {
            int hwFlags = ((Integer) ApplicationInfo.class.getField("hwFlags").get(info)).intValue();
            boolean systemFlagCheck = (info.flags & 1) != 0;
            boolean preInstalledFlagCheck = (HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM & hwFlags) != 0;
            if (systemFlagCheck && preInstalledFlagCheck) {
                return 1;
            }
            if (systemFlagCheck) {
                return 2;
            }
            return 3;
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "getAppType,NoSuchFieldException occurs");
            return -1;
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, "getAppType,IllegalArgumentException occurs");
            return -1;
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "getAppType,IllegalAccessException occurs");
            return -1;
        } catch (RuntimeException e4) {
            Log.e(TAG, "getAppType,RuntimeException occurs");
            return -1;
        } catch (Exception e5) {
            Log.e(TAG, "getAppType,the other Exception occurs");
            return -1;
        }
    }

    private void writeMonitoredUidsToKernel() {
        Log.i(TAG, "writeMonitoredUidsToKernel");
        KernelIOStats.writeUidList(this.mPendingDeleteUidList, this.mPendingAddUidList);
        this.mPendingDeleteUidList.clear();
        this.mPendingAddUidList.clear();
    }

    private void writeToStatsFile(boolean isForceWrite) {
        int totalBytes = this.mPendingCollection.getTotalBytes();
        if (totalBytes == 0) {
            if (Utils.DEBUG) {
                Log.d(TAG, "no pending bytes to upload");
            }
            return;
        }
        SparseArray<IOStatsHistory> pendingHistoryMap = this.mPendingCollection.getIOStatsHistoryMap();
        SparseArray<IOStatsHistory> overFlowHistoryMap = removeOverFlowFromPending(totalBytes, pendingHistoryMap);
        long currentTimeMillis = System.currentTimeMillis();
        forcePersistLocked(currentTimeMillis);
        if (overFlowHistoryMap.size() > 0) {
            handleOverFlowSizeHistory(isForceWrite, pendingHistoryMap, overFlowHistoryMap, currentTimeMillis);
        }
    }

    private void handleOverFlowSizeHistory(boolean isForceWrite, SparseArray<IOStatsHistory> pendingHistoryMap, SparseArray<IOStatsHistory> overFlowHistoryMap, long currentTimeMillis) {
        Log.i(TAG, "handleOverFlowSizeHistory");
        int overFlowHistoryMapSize = overFlowHistoryMap.size();
        for (int index = 0; index < overFlowHistoryMapSize; index++) {
            int uidKey = overFlowHistoryMap.keyAt(index);
            pendingHistoryMap.put(uidKey, ((IOStatsHistory) overFlowHistoryMap.get(uidKey)).clone());
        }
        this.mRotator.forceFile(currentTimeMillis, System.currentTimeMillis());
        if (isForceWrite) {
            Log.i(TAG, "handleOverFlowSizeHistory,force to write all the pending datas");
            forcePersistLocked(currentTimeMillis);
        }
    }

    private SparseArray<IOStatsHistory> removeOverFlowFromPending(int totalPendingBytes, SparseArray<IOStatsHistory> pendingHistoryMap) {
        long availableBytes = this.mRotator.getAvailableBytesInActiveFile(System.currentTimeMillis());
        if (((long) totalPendingBytes) <= availableBytes) {
            if (Utils.DEBUG) {
                Log.d(TAG, "the file is enough for the pending bytes");
            }
            return new SparseArray();
        }
        int index;
        SparseArray<IOStatsHistory> overFlowHistoryMap = new SparseArray();
        int totalBytesNum = 0;
        int pendingHistoryMapSize = pendingHistoryMap.size();
        for (index = 0; index < pendingHistoryMapSize; index++) {
            int uid = pendingHistoryMap.keyAt(index);
            IOStatsHistory history = (IOStatsHistory) pendingHistoryMap.get(uid);
            totalBytesNum = (int) (((long) totalBytesNum) + history.getTotalBytesNum());
            if (((long) totalBytesNum) > availableBytes) {
                overFlowHistoryMap.put(uid, history.clone());
            }
        }
        int overFlowHistoryMapSize = overFlowHistoryMap.size();
        for (index = 0; index < overFlowHistoryMapSize; index++) {
            pendingHistoryMap.remove(overFlowHistoryMap.keyAt(index));
        }
        return overFlowHistoryMap;
    }

    private void forcePersistLocked(long currentTimeMillis) {
        try {
            Log.i(TAG, "forcePersistLocked");
            IOStatsCollection clonePendingCollection = this.mPendingCollection.clone();
            this.mRotator.rewriteActive(this.mPendingRewriter, currentTimeMillis);
            this.mRotator.maybeRotate(currentTimeMillis);
            this.mRotator.removeFilesWhenOverFlow();
            this.mCompleteCollection.addHistories(clonePendingCollection);
            this.mPendingCollection.reset();
        } catch (IOException e) {
            Log.wtf(TAG, "problem persisting pending stats", e);
            recoverFromWtf();
        } catch (OutOfMemoryError e2) {
            Log.wtf(TAG, "problem persisting pending stats", e2);
            recoverFromWtf();
        }
    }

    public void saveIOStatsAndLatestUids(boolean isForceWrite) {
        if (!this.mIsServiceReady) {
            Log.e(TAG, "saveIOStatsAndLatestUids,the service is not ready");
        } else if (isForceWrite || System.currentTimeMillis() - this.mLastScreenOffTime >= WifiProCommonUtils.RECHECK_DELAYED_MS) {
            Log.i(TAG, "saveIOStatsAndLatestUids,isForceWrite:" + isForceWrite);
            writeToStatsFile(isForceWrite);
        } else {
            Log.i(TAG, "saveIOStatsAndLatestUids,the time interval is too shorter than the last screen off");
        }
    }

    private Object[] getUidFromIntentExtras(Intent intent) {
        if (intent == null) {
            Log.e(TAG, "getUidFromIntentExtras,intent is null");
            return EMPTY_OBJECT;
        }
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            Log.e(TAG, "getUidFromIntentExtras,bundle is null");
            return EMPTY_OBJECT;
        }
        Object[] result = new Object[2];
        int uid = bundle.getInt("android.intent.extra.UID");
        if (!"android.intent.action.UID_REMOVED".equals(intent.getAction())) {
            String packageName = intent.getDataString();
            if (checkIfThirdPartyApp(packageName)) {
                result[0] = Integer.valueOf(uid);
                result[1] = packageName;
                if (Utils.DEBUG) {
                    Log.d(TAG, "third party apk:" + packageName);
                }
                return result;
            }
            Log.i(TAG, packageName + " is not the 3rd party apk");
            return EMPTY_OBJECT;
        } else if (this.mAllUidsMonitoredList.contains(Integer.valueOf(uid))) {
            result[0] = Integer.valueOf(uid);
            result[1] = "";
            return result;
        } else {
            Log.i(TAG, "apk (uid:" + uid + ") is not the 3rd party apk");
            return EMPTY_OBJECT;
        }
    }

    private void notifyUidChanged(boolean isUidRemoval, Intent intent, String flowTAG) {
        Object[] result = getUidFromIntentExtras(intent);
        if (result.length < 2) {
            Log.e(TAG, flowTAG + ",uid is invalid");
            return;
        }
        try {
            refreshMonitoredUids(isUidRemoval, Integer.parseInt(result[0].toString()), result[1].toString());
        } catch (Exception ex) {
            Log.e(TAG, "notifyUidChanged,An Exception occurs:" + ex.getMessage());
        }
    }

    public void refreshMonitoredUids(boolean isUidRemoval, int uid, String pkgName) {
        Log.i(TAG, "refreshMonitoredUids,isUidRemoval:" + isUidRemoval + ",uid:" + uid);
        if (this.mIsServiceReady) {
            if (isUidRemoval) {
                this.mPendingDeleteUidList.add(Integer.valueOf(uid));
                this.mAllUidsMonitoredList.remove(Integer.valueOf(uid));
            } else {
                this.mPendingAddUidList.add(Integer.valueOf(uid));
                this.mAllUidsMonitoredList.add(Integer.valueOf(uid));
                if (!this.mUidPkgTable.containsKey(Integer.valueOf(uid))) {
                    this.mUidPkgTable.put(Integer.valueOf(uid), pkgName);
                }
            }
            writeMonitoredUidsToKernel();
            return;
        }
        Log.e(TAG, "refreshMonitoredUids,the service is not ready");
    }
}

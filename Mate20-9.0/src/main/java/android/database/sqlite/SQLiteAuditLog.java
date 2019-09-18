package android.database.sqlite;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SQLiteAuditLog {
    private static final int HW_AUDIT_CONNECTION = 128;
    private static int HW_AUDIT_DBG_AUDITOBSERVER = 1;
    private static int HW_AUDIT_DBG_FLAGS = 16;
    private static int HW_AUDIT_DBG_PRINT = 8;
    private static int HW_AUDIT_DBG_REGISTEROBSERVER = 2;
    private static int HW_AUDIT_DBG_WARNING = 4;
    private static final int HW_AUDIT_END_SQL = 1;
    private static final int HW_AUDIT_END_STAT = 0;
    private static final int HW_AUDIT_EQP = 8;
    private static final int HW_AUDIT_GRPORDERBY = 16;
    private static final int HW_AUDIT_MEMORY = 256;
    private static final int HW_AUDIT_SIMPLE = 1;
    private static final int HW_AUDIT_SQL = 4;
    private static final int HW_AUDIT_STAT1 = 64;
    private static final int HW_AUDIT_TRANSLOCK = 32;
    private static final int HW_AUDIT_VERBOSE = 2;
    private static final String TAG = "SQLiteAuditLog";
    private static final String mAuditAuditProviderUriStr = "content://com.huawei.providers.sqlaudit/item";
    private static final Uri mAuditProviderUri = Uri.parse(mAuditAuditProviderUriStr);
    private static final String mAuditProvidermethodGet = "METHOD_GET_AUDIT_CONFIGURATION_INFOMATION_FROM_ACTIVATION_APP";
    private static int mDebugFlags = SystemProperties.getInt("log.tag.SQLiteAuditLog", 0);
    private static final String mPkgNameSQLAuditProvider = "com.huawei.providers.sqlaudit";
    private HashMap<String, Record> hashMap;
    private int mAttachFileCount;
    private int mAuditFlags;
    private long mAuditPtr = 0;
    private int mBeginMemUsed;
    private int mCallingPid;
    private String mCallingProcess;
    private int mConnctionCount;
    private long mConnectionPtr = 0;
    private int mCpuNum;
    private String mCurProcess;
    private Record mCurrRec = null;
    private int mEndMemUsed;
    private ArrayList<String> mEqp;
    private String mErrMsg;
    private int mFileReadCount;
    private int mFileReadTime;
    private ArrayList<String> mGrpOrderbyEqp;
    private String mMainDbFile;
    private int mMaxMemUsed;
    private int mPagerMemUsed;
    private int mPid;
    private String mPkgName;
    private int mReadPageCount;
    private int mReadPageTime;
    private int mResultRowNum;
    private int mSortRowNum;
    private int mSortTime;
    private ArrayList<String> mSql;
    private ArrayList<String> mStat1;
    private int mTransLockTime;
    private int mTravelRowNum;
    private int mVbdeStep;
    private String mVdbeBeginDateTime;
    private int mVdbeCommitTime;
    private int mVdbeExecRet;
    private int mVdbeExecTime;
    private int mVdbeTranTime;
    private ContentResolver resolver = null;

    private static class AuditLog {
        public static final String APPNAME = "app_name";
        public static final String ATTACHFILECOUNT = "attach_file_count";
        public static final String BEGINMEMUSED = "begin_mem_used";
        public static final String CALLINGPID = "calling_pid";
        public static final String CALLINGPROCESSNAME = "calling_process_name";
        public static final String CONNECTIONCOUNT = "conn_count";
        public static final String CPUFREQINFO = "cpu_freq_info";
        public static final String CPUNUM = "cpu_num";
        public static final String ENDMEMUSED = "end_mem_used";
        public static final String EQP = "eqp";
        public static final String ERRMSG = "err_msg";
        public static final String FEATUREID = "feature_id";
        public static final String FILEREADCOUNT = "file_read_count";
        public static final String FILEREADTIME = "file_read_time";
        public static final String GROUPORDERBYEQP = "group_order_by_eqp";
        public static final String MAINDBFILE = "main_db_file";
        public static final String MAXMEMUSED = "max_mem_used";
        public static final String PAGERMEMUSED = "pager_mem_used";
        public static final String PID = "pid";
        public static final String PKGNAME = "pkg_name";
        public static final String READPAGECOUNT = "read_page_count";
        public static final String READPAGETIME = "read_page_time";
        public static final String RESULTROWNUM = "result_row_num";
        public static final String SORTROWNUM = "sort_row_num";
        public static final String SORTTIME = "sort_time";
        public static final String SQL = "sql_stmt";
        public static final String STAT = "stat";
        public static final String TRANSLOCKTIME = "trans_lock_time";
        public static final String TRAVELROWNUM = "travel_row_num";
        public static final String VBDESTEP = "vbde_step";
        public static final String VDBEBEGINDATETIME = "vdbe_begin_date_time";
        public static final String VDBECOMMITTIME = "vdbe_commit_time";
        public static final String VDBEEXECRET = "vdbe_exec_ret";
        public static final String VDBEEXECTIME = "vdbe_exec_time";
        public static final String VDBETRANTIME = "vdbe_tran_time";

        private AuditLog() {
        }
    }

    private static class Record {
        public static final String APP_NAME_ID = "AppName";
        public static final String DATABASE_NAME_ID = "dataBaseName";
        public static final String GROUP_ORDER_AUDIT_RECORD_SQL_ID = "groupOrderAudit_recordSQL";
        public static final String GROUP_ORDER_AUDIT_TIME_THRESHOLD_SQL_ID = "groupOrderAudit_timeThresholdSQL";
        public static final String TIME_AUDIT_RECORD_EXEC_PLAN_ID = "timeAudit_recordExecPlan";
        public static final String TIME_AUDIT_RECORD_SQL_ID = "timeAudit_recordSQL";
        public static final String TIME_AUDIT_TIME_THRESHOLD_SQL_ID = "timeAudit_timeThresholdSQL";
        public static final String TIME_AUDIT_TURN_ON_DETAILED_STATISTICS_ID = "timeAudit_turnOnDetailedStatistics";
        public static final String TRANS_LOCK_AUDIT_RECORD_SQL_ID = "transLockAudit_recordSQL";
        public static final String TRANS_LOCK_AUDIT_TIME_THRESHOLD_TRANSACTION_LOCK_ID = "transLockAudit_timeThresholdTransactionLock";
        public static final String convLinkAudit_ID = "convLinkAudit";
        public static final String groupOrderAudit_ID = "groupOrderAudit";
        public static final String memManageAudit_ID = "memManageAudit";
        public static final String statInfoAudit_ID = "statInfoAudit";
        public static final String timeAudit_ID = "timeAudit";
        public static final String transLockAudit_ID = "transLockAudit";
        public boolean convLinkAudit;
        public boolean groupOrderAudit;
        public boolean groupOrderAudit_recordSQL;
        public int groupOrderAudit_timeThresholdSQL;
        public boolean memManageAudit;
        public boolean statInfoAudit;
        public boolean timeAudit;
        public boolean timeAudit_recordExecPlan;
        public boolean timeAudit_recordSQL;
        public int timeAudit_timeThresholdSQL;
        public boolean timeAudit_turnOnDetailedStatistics;
        public boolean transLockAudit;
        public boolean transLockAudit_recordSQL;
        public int transLockAudit_timeThresholdTransactionLock;

        private Record() {
            this.timeAudit = false;
            this.timeAudit_timeThresholdSQL = 0;
            this.timeAudit_recordSQL = false;
            this.timeAudit_recordExecPlan = false;
            this.timeAudit_turnOnDetailedStatistics = false;
            this.statInfoAudit = false;
            this.groupOrderAudit = false;
            this.groupOrderAudit_timeThresholdSQL = 0;
            this.groupOrderAudit_recordSQL = false;
            this.transLockAudit = false;
            this.transLockAudit_timeThresholdTransactionLock = 0;
            this.transLockAudit_recordSQL = false;
            this.convLinkAudit = false;
            this.memManageAudit = false;
        }
    }

    private static native long nativeAuditConfigure(long j, long j2, SQLiteAuditLog sQLiteAuditLog, boolean z);

    public SQLiteAuditLog(String mainFile, long connectionPtr) {
        execSQLiteAuditLog(mainFile, connectionPtr, ActivityThread.currentApplication());
    }

    private void execSQLiteAuditLog(String mainFile, long connectionPtr, Application ap) {
        if (ap == null) {
            if ((mDebugFlags & HW_AUDIT_DBG_WARNING) != 0) {
                Log.w(TAG, Process.myPid() + ":currentApplication is null");
            }
            return;
        }
        Context ctx = ap.getApplicationContext();
        if (ctx == null) {
            if ((mDebugFlags & HW_AUDIT_DBG_WARNING) != 0) {
                Log.w(TAG, Process.myPid() + ":getApplicationContext is null");
            }
        } else if (isAuditProviderSystem(ctx)) {
            this.mPkgName = ctx.getPackageName();
            this.mPid = Process.myPid();
            this.resolver = ctx.getContentResolver();
            if (this.mPkgName.equals(mPkgNameSQLAuditProvider) || this.resolver == null) {
                if (this.resolver == null && (mDebugFlags & HW_AUDIT_DBG_WARNING) != 0) {
                    Log.w(TAG, this.mPid + ":getContentResolver is null");
                }
                return;
            }
            this.mCurProcess = Process.getCmdlineForPid(this.mPid);
            this.hashMap = new HashMap<>();
            this.mAuditPtr = 0;
            this.mVdbeExecTime = 0;
            this.mCpuNum = 0;
            this.mVdbeTranTime = 0;
            this.mVdbeExecRet = 0;
            this.mResultRowNum = 0;
            this.mTravelRowNum = 0;
            this.mVbdeStep = 0;
            this.mSql = new ArrayList<>();
            this.mErrMsg = null;
            this.mConnctionCount = 0;
            this.mBeginMemUsed = 0;
            this.mEndMemUsed = 0;
            this.mMaxMemUsed = 0;
            this.mPagerMemUsed = 0;
            this.mSortTime = 0;
            this.mReadPageTime = 0;
            this.mReadPageCount = 0;
            this.mFileReadCount = 0;
            this.mFileReadTime = 0;
            this.mMainDbFile = mainFile;
            this.mTransLockTime = 0;
            this.mVdbeCommitTime = 0;
            this.mAttachFileCount = 0;
            this.mSortRowNum = 0;
            this.mEqp = new ArrayList<>();
            this.mStat1 = new ArrayList<>();
            this.mGrpOrderbyEqp = new ArrayList<>();
            this.mConnectionPtr = connectionPtr;
            this.mAuditPtr = 0;
            onObserverChange();
        }
    }

    private boolean isAuditProviderSystem(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        if (pm == null) {
            if ((mDebugFlags & HW_AUDIT_DBG_WARNING) != 0) {
                Log.w(TAG, Process.myPid() + ":getPackageManager return null");
            }
            return false;
        }
        try {
            PackageInfo pkgInfo = pm.getPackageInfo(mPkgNameSQLAuditProvider, 8);
            if (pkgInfo == null || pkgInfo.sharedUserId == null) {
                if ((mDebugFlags & HW_AUDIT_DBG_WARNING) != 0) {
                    Log.w(TAG, Process.myPid() + ":audit provider pkgInfo or sharedUserId == null");
                }
                return false;
            } else if (!pkgInfo.sharedUserId.equals("android.uid.system")) {
                if ((mDebugFlags & HW_AUDIT_DBG_WARNING) != 0) {
                    Log.w(TAG, Process.myPid() + ":audit provider sharedUserId != android.uid.system");
                }
                return false;
            } else {
                ProviderInfo[] providerInfo = pkgInfo.providers;
                if (providerInfo == null || providerInfo.length != 1) {
                    if ((mDebugFlags & HW_AUDIT_DBG_WARNING) != 0) {
                        Log.w(TAG, Process.myPid() + ":audit providerInfo is null or length != 1");
                    }
                    return false;
                } else if (providerInfo[0].authority.equals(mPkgNameSQLAuditProvider)) {
                    return true;
                } else {
                    if ((mDebugFlags & HW_AUDIT_DBG_WARNING) != 0) {
                        Log.w(TAG, Process.myPid() + ":audit provider authority != mPkgNameSQLAuditProvider");
                    }
                    return false;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            if ((mDebugFlags & HW_AUDIT_DBG_WARNING) != 0) {
                Log.w(TAG, Process.myPid() + ":getPackageInfo catch name not found exception");
            }
            return false;
        }
    }

    public void disableAudit() {
        if (this.mAuditPtr != 0 && this.mConnectionPtr != 0) {
            nativeAuditConfigure(0, this.mAuditPtr, null, false);
            this.mAuditPtr = 0;
        }
    }

    public void enableAudit() {
        if (this.mAuditPtr == 0 && this.mConnectionPtr != 0) {
            this.mAuditPtr = nativeAuditConfigure(this.mConnectionPtr, 0, this, true);
        }
    }

    private void onObserverChange() {
        synchronized (this.hashMap) {
            getRecordsFromProvider(this.mMainDbFile);
        }
    }

    private int getAuditFlags() {
        if (this.hashMap.size() == 0) {
            return 0;
        }
        this.mCallingPid = Binder.getCallingPid();
        if (this.mCallingPid != this.mPid) {
            this.mCallingProcess = getProcessName(this.mCallingPid);
        } else {
            this.mCallingProcess = this.mCurProcess;
        }
        this.mCurrRec = getRecord(this.mCallingProcess);
        this.mAuditFlags = getRetVal(this.mCurrRec);
        if ((mDebugFlags & HW_AUDIT_DBG_FLAGS) != 0) {
            Log.d(TAG, this.mCallingProcess + "(" + this.mCallingPid + "):" + this.mMainDbFile + " mAuditFlags:" + this.mAuditFlags);
        }
        return this.mAuditFlags;
    }

    private void getRecordsFromProvider(String dataBaseName) {
        boolean[] transLockAudit_recordSQL_array;
        boolean[] memManageAudit_array;
        boolean[] transLockAudit_array;
        Bundle args = new Bundle();
        Bundle bundle = null;
        args.putString(Record.DATABASE_NAME_ID, dataBaseName);
        try {
            bundle = this.resolver.call(mAuditProviderUri, mAuditProvidermethodGet, null, args);
        } catch (IllegalArgumentException e) {
            if ((mDebugFlags & HW_AUDIT_DBG_WARNING) != 0) {
                Log.w(TAG, this.mPkgName + " getRecordsFromProvider failed");
            }
        }
        if (bundle == null) {
            if ((mDebugFlags & HW_AUDIT_DBG_WARNING) != 0) {
                Log.w(TAG, "getRecordsFromProvider resolver.call return null");
            }
            return;
        }
        String[] AppName_array = bundle.getStringArray(Record.APP_NAME_ID);
        String[] dataBaseName_array = bundle.getStringArray(Record.DATABASE_NAME_ID);
        boolean[] timeAudit_array = bundle.getBooleanArray(Record.timeAudit_ID);
        int[] timeAudit_timeThresholdSQL_array = bundle.getIntArray(Record.TIME_AUDIT_TIME_THRESHOLD_SQL_ID);
        boolean[] timeAudit_recordSQL_array = bundle.getBooleanArray(Record.TIME_AUDIT_RECORD_SQL_ID);
        boolean[] timeAudit_recordExecPlan_array = bundle.getBooleanArray(Record.TIME_AUDIT_RECORD_EXEC_PLAN_ID);
        boolean[] timeAudit_turnOnDetailedStatistics_array = bundle.getBooleanArray(Record.TIME_AUDIT_TURN_ON_DETAILED_STATISTICS_ID);
        boolean[] statInfoAudit_array = bundle.getBooleanArray(Record.statInfoAudit_ID);
        boolean[] groupOrderAudit_array = bundle.getBooleanArray(Record.groupOrderAudit_ID);
        int[] groupOrderAudit_timeThresholdSQL_array = bundle.getIntArray(Record.GROUP_ORDER_AUDIT_TIME_THRESHOLD_SQL_ID);
        boolean[] groupOrderAudit_recordSQL_array = bundle.getBooleanArray(Record.GROUP_ORDER_AUDIT_RECORD_SQL_ID);
        boolean[] transLockAudit_array2 = bundle.getBooleanArray(Record.transLockAudit_ID);
        Bundle bundle2 = args;
        int[] transLockAudit_timeThresholdTransactionLock_array = bundle.getIntArray(Record.TRANS_LOCK_AUDIT_TIME_THRESHOLD_TRANSACTION_LOCK_ID);
        boolean[] transLockAudit_recordSQL_array2 = bundle.getBooleanArray(Record.TRANS_LOCK_AUDIT_RECORD_SQL_ID);
        boolean[] convLinkAudit_array = bundle.getBooleanArray(Record.convLinkAudit_ID);
        boolean[] convLinkAudit_array2 = bundle.getBooleanArray(Record.memManageAudit_ID);
        if (AppName_array == null || dataBaseName_array == null || timeAudit_array == null || timeAudit_timeThresholdSQL_array == null || timeAudit_recordSQL_array == null || timeAudit_recordExecPlan_array == null || timeAudit_turnOnDetailedStatistics_array == null || statInfoAudit_array == null || groupOrderAudit_array == null || groupOrderAudit_timeThresholdSQL_array == null || groupOrderAudit_recordSQL_array == null || transLockAudit_array2 == null || transLockAudit_timeThresholdTransactionLock_array == null || transLockAudit_recordSQL_array2 == null || convLinkAudit_array == null) {
            boolean[] memManageAudit_array2 = convLinkAudit_array2;
            boolean[] zArr = transLockAudit_recordSQL_array2;
            int[] iArr = transLockAudit_timeThresholdTransactionLock_array;
            Bundle bundle3 = bundle;
            boolean[] zArr2 = transLockAudit_array2;
            String[] strArr = dataBaseName_array;
            boolean[] zArr3 = convLinkAudit_array;
            String str = dataBaseName;
        } else if (convLinkAudit_array2 == null) {
            boolean[] zArr4 = convLinkAudit_array2;
            boolean[] zArr5 = transLockAudit_recordSQL_array2;
            int[] iArr2 = transLockAudit_timeThresholdTransactionLock_array;
            Bundle bundle4 = bundle;
            boolean[] zArr6 = transLockAudit_array2;
            String[] strArr2 = dataBaseName_array;
            boolean[] zArr7 = convLinkAudit_array;
            String str2 = dataBaseName;
        } else {
            Bundle bundle5 = bundle;
            boolean[] memManageAudit_array3 = convLinkAudit_array2;
            if (AppName_array.length == dataBaseName_array.length && AppName_array.length == timeAudit_array.length && AppName_array.length == timeAudit_timeThresholdSQL_array.length && AppName_array.length == timeAudit_recordSQL_array.length && AppName_array.length == timeAudit_recordExecPlan_array.length && AppName_array.length == timeAudit_turnOnDetailedStatistics_array.length && AppName_array.length == statInfoAudit_array.length && AppName_array.length == groupOrderAudit_array.length && AppName_array.length == groupOrderAudit_timeThresholdSQL_array.length && AppName_array.length == groupOrderAudit_recordSQL_array.length && AppName_array.length == transLockAudit_array2.length && AppName_array.length == transLockAudit_timeThresholdTransactionLock_array.length && AppName_array.length == transLockAudit_recordSQL_array2.length) {
                String[] dataBaseName_array2 = dataBaseName_array;
                boolean[] convLinkAudit_array3 = convLinkAudit_array;
                if (AppName_array.length == convLinkAudit_array3.length) {
                    boolean[] convLinkAudit_array4 = convLinkAudit_array3;
                    boolean[] memManageAudit_array4 = memManageAudit_array3;
                    if (AppName_array.length != memManageAudit_array4.length) {
                        boolean[] zArr8 = transLockAudit_recordSQL_array2;
                        int[] iArr3 = transLockAudit_timeThresholdTransactionLock_array;
                        boolean[] zArr9 = transLockAudit_array2;
                        boolean[] zArr10 = memManageAudit_array4;
                        String str3 = dataBaseName;
                    } else {
                        this.hashMap.clear();
                        if ((mDebugFlags & HW_AUDIT_DBG_AUDITOBSERVER) != 0) {
                            memManageAudit_array = memManageAudit_array4;
                            StringBuilder sb = new StringBuilder();
                            transLockAudit_recordSQL_array = transLockAudit_recordSQL_array2;
                            sb.append(this.mCurProcess);
                            sb.append("(");
                            sb.append(this.mPid);
                            sb.append(")getting conf,record number: ");
                            sb.append(AppName_array.length);
                            Log.d(TAG, sb.toString());
                        } else {
                            transLockAudit_recordSQL_array = transLockAudit_recordSQL_array2;
                            memManageAudit_array = memManageAudit_array4;
                        }
                        int k = 0;
                        while (k < AppName_array.length) {
                            Record record = new Record();
                            record.timeAudit = timeAudit_array[k];
                            record.timeAudit_timeThresholdSQL = timeAudit_timeThresholdSQL_array[k];
                            record.timeAudit_recordSQL = timeAudit_recordSQL_array[k];
                            record.timeAudit_recordExecPlan = timeAudit_recordExecPlan_array[k];
                            record.timeAudit_turnOnDetailedStatistics = timeAudit_turnOnDetailedStatistics_array[k];
                            record.statInfoAudit = statInfoAudit_array[k];
                            record.groupOrderAudit = groupOrderAudit_array[k];
                            record.groupOrderAudit_timeThresholdSQL = groupOrderAudit_timeThresholdSQL_array[k];
                            record.groupOrderAudit_recordSQL = groupOrderAudit_recordSQL_array[k];
                            record.transLockAudit = transLockAudit_array2[k];
                            record.transLockAudit_timeThresholdTransactionLock = transLockAudit_timeThresholdTransactionLock_array[k];
                            record.transLockAudit_recordSQL = transLockAudit_recordSQL_array[k];
                            record.convLinkAudit = convLinkAudit_array4[k];
                            record.memManageAudit = memManageAudit_array[k];
                            int[] transLockAudit_timeThresholdTransactionLock_array2 = transLockAudit_timeThresholdTransactionLock_array;
                            if ("ALL".equals(dataBaseName_array2[k])) {
                                HashMap<String, Record> hashMap2 = this.hashMap;
                                StringBuilder sb2 = new StringBuilder();
                                transLockAudit_array = transLockAudit_array2;
                                sb2.append(AppName_array[k]);
                                sb2.append(dataBaseName_array2[k]);
                                hashMap2.put(sb2.toString(), record);
                                String str4 = dataBaseName;
                            } else {
                                transLockAudit_array = transLockAudit_array2;
                                HashMap<String, Record> hashMap3 = this.hashMap;
                                hashMap3.put(AppName_array[k] + dataBaseName, record);
                            }
                            k++;
                            transLockAudit_timeThresholdTransactionLock_array = transLockAudit_timeThresholdTransactionLock_array2;
                            transLockAudit_array2 = transLockAudit_array;
                        }
                        boolean[] zArr11 = transLockAudit_array2;
                        String str5 = dataBaseName;
                        return;
                    }
                } else {
                    int[] iArr4 = transLockAudit_timeThresholdTransactionLock_array;
                    boolean[] zArr12 = convLinkAudit_array3;
                    boolean[] zArr13 = transLockAudit_array2;
                    boolean[] zArr14 = memManageAudit_array3;
                    String str6 = dataBaseName;
                }
            } else {
                int[] iArr5 = transLockAudit_timeThresholdTransactionLock_array;
                boolean[] zArr15 = transLockAudit_array2;
                String[] strArr3 = dataBaseName_array;
                boolean[] zArr16 = convLinkAudit_array;
                boolean[] zArr17 = memManageAudit_array3;
                String str7 = dataBaseName;
            }
            return;
        }
        if ((mDebugFlags & HW_AUDIT_DBG_WARNING) != 0) {
            Log.w(TAG, "getRecordsFromProvider get bundle value return null");
        }
    }

    private Record getRecord(String AppName) {
        Record record;
        synchronized (this.hashMap) {
            HashMap<String, Record> hashMap2 = this.hashMap;
            record = hashMap2.get(AppName + this.mMainDbFile);
            if (record == null) {
                HashMap<String, Record> hashMap3 = this.hashMap;
                record = hashMap3.get("ALL" + this.mMainDbFile);
            }
            if (record == null) {
                HashMap<String, Record> hashMap4 = this.hashMap;
                record = hashMap4.get(AppName + "ALL");
            }
            if (record == null) {
                record = this.hashMap.get("ALLALL");
            }
        }
        return record;
    }

    private int getRetVal(Record record) {
        int retVal = 0;
        if (record == null) {
            return 0;
        }
        if (record.timeAudit) {
            retVal = 0 | 1;
            if (record.timeAudit_turnOnDetailedStatistics) {
                retVal |= 2;
            }
            if (record.timeAudit_recordExecPlan) {
                retVal |= 8;
            }
            if (record.timeAudit_recordSQL) {
                retVal |= 4;
            }
            if (record.convLinkAudit) {
                retVal |= 128;
            }
            if (record.memManageAudit) {
                retVal |= 256;
            }
        }
        if (record.groupOrderAudit) {
            retVal |= 16;
            if (record.groupOrderAudit_recordSQL) {
                retVal |= 4;
            }
        }
        if (record.transLockAudit) {
            retVal |= 32;
            if (record.transLockAudit_recordSQL) {
                retVal |= 4;
            }
        }
        if (record.statInfoAudit) {
            retVal |= 64;
        }
        return retVal;
    }

    private void persistTimeAudit() {
        if (this.mCurrRec != null && this.mCurrRec.timeAudit && this.mVdbeExecTime >= this.mCurrRec.timeAudit_timeThresholdSQL) {
            ContentValues values = new ContentValues();
            values.put(AuditLog.PID, Integer.valueOf(this.mPid));
            values.put(AuditLog.FEATUREID, (Integer) 1);
            values.put(AuditLog.PKGNAME, this.mPkgName);
            values.put(AuditLog.MAINDBFILE, this.mMainDbFile);
            values.put(AuditLog.VDBEEXECTIME, Integer.valueOf(this.mVdbeExecTime));
            values.put(AuditLog.VDBECOMMITTIME, Integer.valueOf(this.mVdbeCommitTime));
            values.put(AuditLog.VDBETRANTIME, Integer.valueOf(this.mVdbeTranTime));
            values.put(AuditLog.CPUFREQINFO, getCpuFreqInfoForPid(this.mCpuNum));
            values.put(AuditLog.CPUNUM, Integer.valueOf(this.mCpuNum));
            values.put(AuditLog.FILEREADCOUNT, Integer.valueOf(this.mFileReadCount));
            values.put(AuditLog.READPAGECOUNT, Integer.valueOf(this.mReadPageCount));
            values.put(AuditLog.TRAVELROWNUM, Integer.valueOf(this.mTravelRowNum));
            values.put(AuditLog.RESULTROWNUM, Integer.valueOf(this.mResultRowNum));
            values.put(AuditLog.VBDESTEP, Integer.valueOf(this.mVbdeStep));
            values.put(AuditLog.VDBEBEGINDATETIME, this.mVdbeBeginDateTime);
            if (this.mCurrRec.memManageAudit) {
                values.put(AuditLog.BEGINMEMUSED, Integer.valueOf(this.mBeginMemUsed));
                values.put(AuditLog.ENDMEMUSED, Integer.valueOf(this.mEndMemUsed));
                values.put(AuditLog.MAXMEMUSED, Integer.valueOf(this.mMaxMemUsed));
                values.put(AuditLog.PAGERMEMUSED, Integer.valueOf(this.mPagerMemUsed));
            }
            if (this.mCallingPid != this.mPid) {
                values.put(AuditLog.CALLINGPID, Integer.valueOf(this.mCallingPid));
                values.put(AuditLog.APPNAME, this.mCurProcess);
                values.put(AuditLog.CALLINGPROCESSNAME, this.mCallingProcess);
            } else {
                values.put(AuditLog.APPNAME, this.mCallingProcess);
                values.put(AuditLog.CALLINGPROCESSNAME, this.mCallingProcess);
            }
            if (this.mCurrRec.timeAudit_recordExecPlan && this.mEqp.size() > 0) {
                values.put(AuditLog.EQP, this.mEqp.toString());
            }
            if (this.mCurrRec.timeAudit_turnOnDetailedStatistics) {
                values.put(AuditLog.FILEREADTIME, Integer.valueOf(this.mFileReadTime));
                values.put(AuditLog.READPAGETIME, Integer.valueOf(this.mReadPageTime));
                values.put(AuditLog.SORTTIME, Integer.valueOf(this.mSortTime));
            }
            if (this.mCurrRec.timeAudit_recordSQL) {
                values.put(AuditLog.SQL, this.mSql.toString());
            }
            if (this.mCurrRec.convLinkAudit) {
                values.put(AuditLog.ATTACHFILECOUNT, Integer.valueOf(this.mAttachFileCount));
                values.put(AuditLog.CONNECTIONCOUNT, Integer.valueOf(this.mConnctionCount));
            }
            try {
                this.resolver.insert(mAuditProviderUri, values);
            } catch (IllegalArgumentException e) {
                if ((mDebugFlags & HW_AUDIT_DBG_WARNING) != 0) {
                    Log.w(TAG, "insert time aduit failed");
                }
            }
        }
    }

    private void persistStatInfo() {
        if (this.mCurrRec.statInfoAudit && this.mStat1.size() != 0) {
            ContentValues values = new ContentValues();
            values.put(AuditLog.PID, Integer.valueOf(this.mPid));
            values.put(AuditLog.FEATUREID, (Integer) 2);
            values.put(AuditLog.PKGNAME, this.mPkgName);
            values.put(AuditLog.APPNAME, this.mCurProcess);
            values.put(AuditLog.MAINDBFILE, this.mMainDbFile);
            values.put(AuditLog.STAT, this.mStat1.toString());
            values.put(AuditLog.VDBEBEGINDATETIME, this.mVdbeBeginDateTime);
            try {
                this.resolver.insert(mAuditProviderUri, values);
            } catch (IllegalArgumentException e) {
                if ((mDebugFlags & HW_AUDIT_DBG_WARNING) != 0) {
                    Log.w(TAG, "insert stat info failed");
                }
            }
        }
    }

    private void persistGroupOrderAudit() {
        if (this.mCurrRec.groupOrderAudit && this.mVdbeExecTime >= this.mCurrRec.groupOrderAudit_timeThresholdSQL && this.mGrpOrderbyEqp.size() != 0) {
            ContentValues values = new ContentValues();
            values.put(AuditLog.PID, Integer.valueOf(this.mPid));
            values.put(AuditLog.FEATUREID, (Integer) 3);
            values.put(AuditLog.PKGNAME, this.mPkgName);
            values.put(AuditLog.MAINDBFILE, this.mMainDbFile);
            values.put(AuditLog.SORTROWNUM, Integer.valueOf(this.mSortRowNum));
            values.put(AuditLog.SORTTIME, Integer.valueOf(this.mSortTime));
            values.put(AuditLog.GROUPORDERBYEQP, this.mGrpOrderbyEqp.toString());
            values.put(AuditLog.VDBEEXECTIME, Integer.valueOf(this.mVdbeExecTime));
            values.put(AuditLog.VDBEBEGINDATETIME, this.mVdbeBeginDateTime);
            if (this.mCallingPid != this.mPid) {
                values.put(AuditLog.CALLINGPID, Integer.valueOf(this.mCallingPid));
                values.put(AuditLog.APPNAME, this.mCurProcess);
                values.put(AuditLog.CALLINGPROCESSNAME, this.mCallingProcess);
            } else {
                values.put(AuditLog.APPNAME, this.mCallingProcess);
                values.put(AuditLog.CALLINGPROCESSNAME, this.mCallingProcess);
            }
            if (this.mCurrRec.groupOrderAudit_recordSQL) {
                values.put(AuditLog.SQL, this.mSql.toString());
            }
            try {
                this.resolver.insert(mAuditProviderUri, values);
            } catch (IllegalArgumentException e) {
                if ((mDebugFlags & HW_AUDIT_DBG_WARNING) != 0) {
                    Log.w(TAG, "insert group order by failed");
                }
            }
        }
    }

    private void persistTransLockAudit() {
        if (this.mCurrRec.transLockAudit && this.mTransLockTime >= this.mCurrRec.transLockAudit_timeThresholdTransactionLock) {
            ContentValues values = new ContentValues();
            values.put(AuditLog.PID, Integer.valueOf(this.mPid));
            values.put(AuditLog.FEATUREID, (Integer) 4);
            values.put(AuditLog.PKGNAME, this.mPkgName);
            values.put(AuditLog.MAINDBFILE, this.mMainDbFile);
            values.put(AuditLog.TRANSLOCKTIME, Integer.valueOf(this.mTransLockTime));
            values.put(AuditLog.VDBEEXECRET, Integer.valueOf(this.mVdbeExecRet));
            values.put(AuditLog.VDBEBEGINDATETIME, this.mVdbeBeginDateTime);
            if (this.mErrMsg != null) {
                values.put(AuditLog.ERRMSG, this.mErrMsg);
            }
            if (this.mCallingPid != this.mPid) {
                values.put(AuditLog.CALLINGPID, Integer.valueOf(this.mCallingPid));
                values.put(AuditLog.APPNAME, this.mCurProcess);
                values.put(AuditLog.CALLINGPROCESSNAME, this.mCallingProcess);
            } else {
                values.put(AuditLog.APPNAME, this.mCallingProcess);
                values.put(AuditLog.CALLINGPROCESSNAME, this.mCallingProcess);
            }
            if (this.mCurrRec.transLockAudit_recordSQL) {
                values.put(AuditLog.SQL, this.mSql.toString());
            }
            try {
                this.resolver.insert(mAuditProviderUri, values);
            } catch (IllegalArgumentException e) {
                if ((mDebugFlags & HW_AUDIT_DBG_WARNING) != 0) {
                    Log.w(TAG, "insert trans lock failed");
                }
            }
        }
    }

    private void persistAuditInfo(int type) {
        if (this.mCurrRec != null) {
            if (type == 1) {
                persistTimeAudit();
                persistGroupOrderAudit();
                persistTransLockAudit();
            }
            if (type == 0) {
                persistStatInfo();
            }
        }
    }

    private void printTimeAudit() {
        Record record = this.mCurrRec;
        if (record != null) {
            if (record.timeAudit) {
                Log.d(TAG, "timeAudit:");
                Log.d(TAG, "VdbeExec Time:" + this.mVdbeExecTime + " VdbeTran Time:" + this.mVdbeTranTime + " CPU Freq Info:" + getCpuFreqInfoForPid(this.mCpuNum) + " Cpu Core ID:" + this.mCpuNum + " FileRead Count:" + this.mFileReadCount + " ReadPage Count:" + this.mReadPageCount + " TravelRow Num:" + this.mTravelRowNum + " ResultRow Num:" + this.mResultRowNum + " SortRow Num:" + this.mSortRowNum + " Vdbe Step:" + this.mVbdeStep);
                if (record.timeAudit_recordExecPlan && this.mEqp.size() > 0) {
                    Log.d(TAG, "record exec plan:" + this.mEqp.toString());
                }
                if (record.timeAudit_turnOnDetailedStatistics) {
                    Log.d(TAG, "detailed statistics");
                    Log.d(TAG, "FileRead Time:" + this.mFileReadTime + " ReadPage Time:" + this.mReadPageTime + " Sort Time:" + this.mSortTime);
                }
            }
            if (record.timeAudit_recordSQL || record.groupOrderAudit_recordSQL || record.transLockAudit_recordSQL) {
                Log.d(TAG, "record SQL statements:" + this.mSql.toString());
            }
        }
    }

    private void printAuditLog4Conf() {
        Record record = this.mCurrRec;
        if (record != null && (mDebugFlags & HW_AUDIT_DBG_PRINT) != 0) {
            Log.d(TAG, "---------------------------------------------------------------------");
            Log.d(TAG, this.mVdbeBeginDateTime + ":" + this.mPkgName + "-" + this.mCurProcess + "(" + this.mPid + ") mAuditFlags=" + this.mAuditFlags);
            if (this.mCallingPid != this.mPid) {
                Log.d(TAG, "Calling Process:" + this.mCallingProcess + "(" + this.mCallingPid + ")");
            }
            printTimeAudit();
            if (record.statInfoAudit) {
                Log.d(TAG, "statInfoAudit:");
                if (this.mStat1.size() > 0) {
                    Log.d(TAG, "stat:" + this.mStat1.toString());
                } else {
                    Log.d(TAG, "stat:none");
                }
            }
            if (record.groupOrderAudit) {
                Log.d(TAG, "groupOrderAudit:");
                Log.d(TAG, "SortRow Num:" + this.mSortRowNum + " Sort Time:" + this.mSortTime);
                int count = this.mGrpOrderbyEqp.size();
                for (int idx = 0; idx < count; idx++) {
                    Log.d(TAG, this.mGrpOrderbyEqp.get(idx));
                }
                Log.d(TAG, "VdbeExec return:" + this.mVdbeExecRet);
                if (this.mErrMsg != null) {
                    Log.d(TAG, "Error Msg:" + this.mErrMsg);
                }
            }
            if (record.transLockAudit != 0) {
                Log.d(TAG, "transLockAudit:");
                Log.d(TAG, "TransLock Time:" + this.mTransLockTime);
                Log.d(TAG, this.mPkgName + "-" + this.mCurProcess + "(" + this.mPid + ")");
                if (this.mCallingPid != this.mPid) {
                    Log.d(TAG, "Calling Process:" + this.mCallingProcess + "(" + this.mCallingPid + ")");
                }
            }
            if (record.convLinkAudit) {
                Log.d(TAG, "convLinkAudit:");
                Log.d(TAG, "Con Num:" + this.mConnctionCount + " AttachFile Count:" + this.mAttachFileCount);
            }
            if (record.memManageAudit) {
                Log.d(TAG, Record.memManageAudit_ID);
                Log.d(TAG, "Begin MemUsed:" + this.mBeginMemUsed + " End MemUsed:" + this.mEndMemUsed + " Max MemUsed:" + this.mMaxMemUsed + " Pager MemUsed:" + this.mPagerMemUsed);
            }
            Log.d(TAG, "---------------------------------------------------------------------");
        }
    }

    private void clearAuditLog() {
        this.mEqp.clear();
        this.mGrpOrderbyEqp.clear();
        this.mSql.clear();
        this.mErrMsg = null;
        this.mVdbeBeginDateTime = null;
        this.mVdbeTranTime = 0;
        this.mVdbeExecTime = 0;
        this.mResultRowNum = 0;
        this.mSortTime = 0;
        this.mReadPageTime = 0;
        this.mCpuNum = 0;
        this.mTravelRowNum = 0;
        this.mVbdeStep = 0;
        this.mReadPageCount = 0;
        this.mFileReadCount = 0;
        this.mFileReadTime = 0;
        this.mTransLockTime = 0;
        this.mVdbeCommitTime = 0;
        this.mSortRowNum = 0;
    }

    private void putEqpLog(String eqp) {
        this.mEqp.add(eqp);
    }

    private void putGrpOrderby(String grpOrderbyEqp) {
        this.mGrpOrderbyEqp.add(grpOrderbyEqp);
    }

    private void putSql(String sql) {
        this.mSql.add(sql);
    }

    private void putStat1(String stat) {
        this.mStat1.add(stat);
    }

    private static final String getCpuFreqInfoForPid(int cpu) {
        String[] outStrings = new String[1];
        Process.readProcFile("/sys/devices/system/cpu/cpu" + cpu + "/cpufreq/scaling_cur_freq", new int[]{4128}, outStrings, null, null);
        return outStrings[0];
    }

    private static String getProcessName(int pid) {
        List<ActivityManager.RunningAppProcessInfo> appList = null;
        try {
            appList = ActivityManagerNative.getDefault().getRunningAppProcesses();
        } catch (RemoteException e) {
            if ((mDebugFlags & HW_AUDIT_DBG_WARNING) != 0) {
                Log.w(TAG, "getProcessName for " + pid + " failed");
            }
        }
        if (appList != null) {
            for (ActivityManager.RunningAppProcessInfo running : appList) {
                if (running.pid == pid) {
                    return running.processName;
                }
            }
        }
        String name = Process.getCmdlineForPid(pid);
        if (name == null) {
            return "unknown process name";
        }
        return name;
    }

    private void endAuditLog(int type) {
        printAuditLog4Conf();
        persistAuditInfo(type);
        if (type == 1) {
            clearAuditLog();
        }
    }
}

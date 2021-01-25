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
import android.provider.SettingsStringUtil;
import android.rms.AppAssociate;
import android.util.Log;
import com.android.internal.content.NativeLibraryHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SQLiteAuditLog {
    private static final String AUDIT_PROVIDER_METHOD_GET = "METHOD_GET_AUDIT_CONFIGURATION_INFOMATION_FROM_ACTIVATION_APP";
    private static final Uri AUDIT_PROVIDER_URI = Uri.parse(AUDIT_PROVIDER_URI_STR);
    private static final String AUDIT_PROVIDER_URI_STR = "content://com.huawei.providers.sqlaudit/item";
    private static final int HW_AUDIT_CONNECTION = 128;
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
    private static final String SQL_AUDIT_PROVIDER = "com.huawei.providers.sqlaudit";
    private static final String TAG = "SQLiteAuditLog";
    private static int hwAuditDbgAuditobserver = 1;
    private static int hwAuditDbgFlags = 16;
    private static int hwAuditDbgPrint = 8;
    private static int hwAuditDbgRegisterobserver = 2;
    private static int hwAuditDbgWarning = 4;
    private static int sDebugFlags = SystemProperties.getInt("log.tag.SQLiteAuditLog", 0);
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
    private ArrayList<String> mEqpList;
    private String mErrMsg;
    private int mFileReadCount;
    private int mFileReadTime;
    private ArrayList<String> mGrpOrderbyEqpList;
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
    private ArrayList<String> mStat1List;
    private int mTransLockTime;
    private int mTravelRowNum;
    private int mVbdeStep;
    private String mVdbeBeginDateTime;
    private int mVdbeCommitTime;
    private int mVdbeExecRet;
    private int mVdbeExecTime;
    private int mVdbeTranTime;
    private ContentResolver resolver = null;
    private ArrayList<String> sqlList;

    private static native long nativeAuditConfigure(long j, long j2, SQLiteAuditLog sQLiteAuditLog, boolean z);

    public SQLiteAuditLog(String mainFile, long connectionPtr) {
        execSqliteAuditLog(mainFile, connectionPtr, ActivityThread.currentApplication());
    }

    /* access modifiers changed from: private */
    public static class Record {
        private static final String APP_NAME_ID = "AppName";
        private static final String CONV_LINK_AUDIT_ID = "convLinkAudit";
        private static final String DATABASE_NAME_ID = "dataBaseName";
        private static final String GROUP_ORDER_AUDIT_ID = "groupOrderAudit";
        private static final String GROUP_ORDER_AUDIT_RECORD_SQL_ID = "groupOrderAudit_recordSQL";
        private static final String GROUP_ORDER_AUDIT_TIME_THRESHOLD_SQL_ID = "groupOrderAudit_timeThresholdSQL";
        private static final String MEM_MANAGE_AUDIT_ID = "memManageAudit";
        private static final String STAT_INFO_AUDIT_ID = "statInfoAudit";
        private static final String TIME_AUDIT_ID = "timeAudit";
        private static final String TIME_AUDIT_RECORD_EXEC_PLAN_ID = "timeAudit_recordExecPlan";
        private static final String TIME_AUDIT_RECORD_SQL_ID = "timeAudit_recordSQL";
        private static final String TIME_AUDIT_TIME_THRESHOLD_SQL_ID = "timeAudit_timeThresholdSQL";
        private static final String TIME_AUDIT_TURN_ON_DETAILED_STATISTICS_ID = "timeAudit_turnOnDetailedStatistics";
        private static final String TRANS_LOCK_AUDIT_ID = "transLockAudit";
        private static final String TRANS_LOCK_AUDIT_RECORD_SQL_ID = "transLockAudit_recordSQL";
        private static final String TRANS_LOCK_AUDIT_TIME_THRESHOLD_TRANSACTION_LOCK_ID = "transLockAudit_timeThresholdTransactionLock";
        private int groupOrderAuditTimeThresholdSql;
        private boolean isConvLinkAudit;
        private boolean isGroupOrderAudit;
        private boolean isGroupOrderAuditRecordSql;
        private boolean isMemManageAudit;
        private boolean isStatInfoAudit;
        private boolean isTimeAudit;
        private boolean isTimeAuditRecordExecPlan;
        private boolean isTimeAuditRecordSql;
        private boolean isTimeAuditTurnOnDetailedStatistics;
        private boolean isTransLockAudit;
        private boolean isTransLockAuditRecordSql;
        private int timeAuditTimeThresholdSql;
        private int transLockAuditTimeThresholdTransactionLock;

        private Record() {
            this.isTimeAudit = false;
            this.timeAuditTimeThresholdSql = 0;
            this.isTimeAuditRecordSql = false;
            this.isTimeAuditRecordExecPlan = false;
            this.isTimeAuditTurnOnDetailedStatistics = false;
            this.isStatInfoAudit = false;
            this.isGroupOrderAudit = false;
            this.groupOrderAuditTimeThresholdSql = 0;
            this.isGroupOrderAuditRecordSql = false;
            this.isTransLockAudit = false;
            this.transLockAuditTimeThresholdTransactionLock = 0;
            this.isTransLockAuditRecordSql = false;
            this.isConvLinkAudit = false;
            this.isMemManageAudit = false;
        }
    }

    private static class AuditLog {
        private static final String APPNAME = "app_name";
        private static final String ATTACHFILECOUNT = "attach_file_count";
        private static final String BEGINMEMUSED = "begin_mem_used";
        private static final String CALLINGPID = "calling_pid";
        private static final String CALLINGPROCESSNAME = "calling_process_name";
        private static final String CONNECTIONCOUNT = "conn_count";
        private static final String CPUFREQINFO = "cpu_freq_info";
        private static final String CPUNUM = "cpu_num";
        private static final String ENDMEMUSED = "end_mem_used";
        private static final String EQP = "eqp";
        private static final String ERRMSG = "err_msg";
        private static final String FEATUREID = "feature_id";
        private static final String FILEREADCOUNT = "file_read_count";
        private static final String FILEREADTIME = "file_read_time";
        private static final String GROUPORDERBYEQP = "group_order_by_eqp";
        private static final String MAINDBFILE = "main_db_file";
        private static final String MAXMEMUSED = "max_mem_used";
        private static final String PAGERMEMUSED = "pager_mem_used";
        private static final String PID = "pid";
        private static final String PKGNAME = "pkg_name";
        private static final String READPAGECOUNT = "read_page_count";
        private static final String READPAGETIME = "read_page_time";
        private static final String RESULTROWNUM = "result_row_num";
        private static final String SORTROWNUM = "sort_row_num";
        private static final String SORTTIME = "sort_time";
        private static final String SQL = "sql_stmt";
        private static final String STAT = "stat";
        private static final String TRANSLOCKTIME = "trans_lock_time";
        private static final String TRAVELROWNUM = "travel_row_num";
        private static final String VBDESTEP = "vbde_step";
        private static final String VDBEBEGINDATETIME = "vdbe_begin_date_time";
        private static final String VDBECOMMITTIME = "vdbe_commit_time";
        private static final String VDBEEXECRET = "vdbe_exec_ret";
        private static final String VDBEEXECTIME = "vdbe_exec_time";
        private static final String VDBETRANTIME = "vdbe_tran_time";

        private AuditLog() {
        }
    }

    private void execSqliteAuditLog(String mainFile, long connectionPtr, Application ap) {
        if (ap != null) {
            Context ctx = ap.getApplicationContext();
            if (ctx == null) {
                if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                    Log.w(TAG, Process.myPid() + ":getApplicationContext is null");
                }
            } else if (isAuditProviderSystem(ctx)) {
                this.mPkgName = ctx.getPackageName();
                this.mPid = Process.myPid();
                this.resolver = ctx.getContentResolver();
                if (!SQL_AUDIT_PROVIDER.equals(this.mPkgName) && this.resolver != null) {
                    initialAudit(mainFile, connectionPtr);
                } else if (this.resolver == null && (sDebugFlags & hwAuditDbgWarning) != 0) {
                    Log.w(TAG, this.mPid + ":getContentResolver is null");
                }
            }
        } else if ((sDebugFlags & hwAuditDbgWarning) != 0) {
            Log.w(TAG, Process.myPid() + ":currentApplication is null");
        }
    }

    private void initialAudit(String mainFile, long connectionPtr) {
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
        this.sqlList = new ArrayList<>();
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
        this.mEqpList = new ArrayList<>();
        this.mStat1List = new ArrayList<>();
        this.mGrpOrderbyEqpList = new ArrayList<>();
        this.mConnectionPtr = connectionPtr;
        this.mAuditPtr = 0;
        onObserverChange();
    }

    private boolean isAuditProviderSystem(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        if (pm == null) {
            if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                Log.w(TAG, Process.myPid() + ":getPackageManager return null");
            }
            return false;
        }
        try {
            PackageInfo pkgInfo = pm.getPackageInfo(SQL_AUDIT_PROVIDER, 8);
            if (pkgInfo == null || pkgInfo.sharedUserId == null) {
                if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                    Log.w(TAG, Process.myPid() + ":audit provider pkgInfo or sharedUserId == null");
                }
                return false;
            } else if (!pkgInfo.sharedUserId.equals("android.uid.system")) {
                if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                    Log.w(TAG, Process.myPid() + ":audit provider sharedUserId != android.uid.system");
                }
                return false;
            } else {
                ProviderInfo[] providerInfos = pkgInfo.providers;
                if (providerInfos == null || providerInfos.length != 1) {
                    if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                        Log.w(TAG, Process.myPid() + ":audit providerInfos is null or length != 1");
                    }
                    return false;
                } else if (providerInfos[0].authority.equals(SQL_AUDIT_PROVIDER)) {
                    return true;
                } else {
                    if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                        Log.w(TAG, Process.myPid() + ":audit provider authority != mPkgNameSQLAuditProvider");
                    }
                    return false;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                Log.w(TAG, Process.myPid() + ":getPackageInfo catch name not found exception");
            }
            return false;
        }
    }

    public void disableAudit() {
        long j = this.mAuditPtr;
        if (j != 0 && this.mConnectionPtr != 0) {
            nativeAuditConfigure(0, j, null, false);
            this.mAuditPtr = 0;
        }
    }

    public void enableAudit() {
        if (this.mAuditPtr == 0) {
            long j = this.mConnectionPtr;
            if (j != 0) {
                this.mAuditPtr = nativeAuditConfigure(j, 0, this, true);
            }
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
        int i = this.mCallingPid;
        if (i != this.mPid) {
            this.mCallingProcess = getProcessName(i);
        } else {
            this.mCallingProcess = this.mCurProcess;
        }
        this.mCurrRec = getRecord(this.mCallingProcess);
        this.mAuditFlags = getRetVal(this.mCurrRec);
        if ((sDebugFlags & hwAuditDbgFlags) != 0) {
            Log.d(TAG, this.mCallingProcess + "(" + this.mCallingPid + "):" + this.mMainDbFile + " mAuditFlags:" + this.mAuditFlags);
        }
        return this.mAuditFlags;
    }

    /* JADX WARNING: Removed duplicated region for block: B:143:0x03f5  */
    /* JADX WARNING: Removed duplicated region for block: B:155:? A[RETURN, SYNTHETIC] */
    private void getRecordsFromProvider(String dataBaseName) {
        String str;
        String[] dataBaseNameArray;
        boolean[] timeAuditArray;
        int[] timeAuditTimeThresholdSqlArray;
        boolean[] timeAuditRecordSqlArray;
        boolean[] timeAuditRecordExecPlanArray;
        boolean[] timeAuditTurnOnDetailedStatisticsArray;
        boolean[] statInfoAuditArray;
        boolean[] groupOrderAuditArray;
        int[] groupOrderAuditTimeThresholdSqlArray;
        boolean[] transLockAuditArray;
        boolean[] transLockAuditRecordSqlArray;
        String str2;
        boolean[] memManageAuditArray;
        boolean[] transLockAuditRecordSqlArray2;
        Bundle args = new Bundle();
        Bundle bundle = null;
        args.putString("dataBaseName", dataBaseName);
        try {
            bundle = this.resolver.call(AUDIT_PROVIDER_URI, AUDIT_PROVIDER_METHOD_GET, (String) null, args);
        } catch (IllegalArgumentException e) {
            if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                Log.w(TAG, this.mPkgName + " getRecordsFromProvider failed");
            }
        }
        if (bundle != null) {
            try {
                String[] appNameArray = bundle.getStringArray("AppName");
                try {
                    dataBaseNameArray = bundle.getStringArray("dataBaseName");
                } catch (ArrayIndexOutOfBoundsException e2) {
                    str = TAG;
                    if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                    }
                }
                try {
                    timeAuditArray = bundle.getBooleanArray("timeAudit");
                    timeAuditTimeThresholdSqlArray = bundle.getIntArray("timeAudit_timeThresholdSQL");
                    timeAuditRecordSqlArray = bundle.getBooleanArray("timeAudit_recordSQL");
                    timeAuditRecordExecPlanArray = bundle.getBooleanArray("timeAudit_recordExecPlan");
                    timeAuditTurnOnDetailedStatisticsArray = bundle.getBooleanArray("timeAudit_turnOnDetailedStatistics");
                    statInfoAuditArray = bundle.getBooleanArray("statInfoAudit");
                    groupOrderAuditArray = bundle.getBooleanArray("groupOrderAudit");
                    try {
                        groupOrderAuditTimeThresholdSqlArray = bundle.getIntArray("groupOrderAudit_timeThresholdSQL");
                    } catch (ArrayIndexOutOfBoundsException e3) {
                        str = TAG;
                        if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e4) {
                    str = TAG;
                    if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                    }
                }
                try {
                    boolean[] groupOrderAuditRecordSqlArray = bundle.getBooleanArray("groupOrderAudit_recordSQL");
                    try {
                        transLockAuditArray = bundle.getBooleanArray("transLockAudit");
                    } catch (ArrayIndexOutOfBoundsException e5) {
                        str = TAG;
                        if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                        }
                    }
                    try {
                        int[] transLockAuditTimeThresholdTransactionLockArray = bundle.getIntArray("transLockAudit_timeThresholdTransactionLock");
                        try {
                            transLockAuditRecordSqlArray = bundle.getBooleanArray("transLockAudit_recordSQL");
                        } catch (ArrayIndexOutOfBoundsException e6) {
                            str = TAG;
                            if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                            }
                        }
                        try {
                            boolean[] convLinkAuditArray = bundle.getBooleanArray("convLinkAudit");
                            try {
                                boolean[] memManageAuditArray2 = bundle.getBooleanArray("memManageAudit");
                                if (appNameArray == null || dataBaseNameArray == null || timeAuditArray == null || timeAuditTimeThresholdSqlArray == null || timeAuditRecordSqlArray == null || timeAuditRecordExecPlanArray == null || timeAuditTurnOnDetailedStatisticsArray == null || statInfoAuditArray == null || groupOrderAuditArray == null) {
                                    str2 = TAG;
                                } else if (groupOrderAuditTimeThresholdSqlArray == null) {
                                    str2 = TAG;
                                } else if (groupOrderAuditRecordSqlArray == null) {
                                    str2 = TAG;
                                } else if (transLockAuditArray != null) {
                                    int[] transLockAuditTimeThresholdTransactionLockArray2 = transLockAuditTimeThresholdTransactionLockArray;
                                    if (transLockAuditTimeThresholdTransactionLockArray2 != null) {
                                        boolean[] transLockAuditRecordSqlArray3 = transLockAuditRecordSqlArray;
                                        if (transLockAuditRecordSqlArray3 == null) {
                                            str2 = TAG;
                                        } else if (convLinkAuditArray == null) {
                                            str2 = TAG;
                                        } else if (memManageAuditArray2 == null) {
                                            str2 = TAG;
                                        } else if (appNameArray.length != dataBaseNameArray.length || appNameArray.length != timeAuditArray.length || appNameArray.length != timeAuditTimeThresholdSqlArray.length || appNameArray.length != timeAuditRecordSqlArray.length || appNameArray.length != timeAuditRecordExecPlanArray.length || appNameArray.length != timeAuditTurnOnDetailedStatisticsArray.length || appNameArray.length != statInfoAuditArray.length || appNameArray.length != groupOrderAuditArray.length || appNameArray.length != groupOrderAuditTimeThresholdSqlArray.length || appNameArray.length != groupOrderAuditRecordSqlArray.length || appNameArray.length != transLockAuditArray.length || appNameArray.length != transLockAuditTimeThresholdTransactionLockArray2.length || appNameArray.length != transLockAuditRecordSqlArray3.length) {
                                            return;
                                        } else {
                                            if (appNameArray.length != convLinkAuditArray.length) {
                                                return;
                                            }
                                            if (appNameArray.length == memManageAuditArray2.length) {
                                                this.hashMap.clear();
                                                if ((sDebugFlags & hwAuditDbgAuditobserver) != 0) {
                                                    StringBuilder sb = new StringBuilder();
                                                    memManageAuditArray = memManageAuditArray2;
                                                    sb.append(this.mCurProcess);
                                                    sb.append("(");
                                                    sb.append(this.mPid);
                                                    sb.append(")getting conf,record number: ");
                                                    sb.append(appNameArray.length);
                                                    Log.d(TAG, sb.toString());
                                                } else {
                                                    memManageAuditArray = memManageAuditArray2;
                                                }
                                                int k = 0;
                                                while (k < appNameArray.length) {
                                                    Record record = new Record();
                                                    record.isTimeAudit = timeAuditArray[k];
                                                    record.timeAuditTimeThresholdSql = timeAuditTimeThresholdSqlArray[k];
                                                    record.isTimeAuditRecordSql = timeAuditRecordSqlArray[k];
                                                    record.isTimeAuditRecordExecPlan = timeAuditRecordExecPlanArray[k];
                                                    record.isTimeAuditTurnOnDetailedStatistics = timeAuditTurnOnDetailedStatisticsArray[k];
                                                    record.isStatInfoAudit = statInfoAuditArray[k];
                                                    record.isGroupOrderAudit = groupOrderAuditArray[k];
                                                    record.groupOrderAuditTimeThresholdSql = groupOrderAuditTimeThresholdSqlArray[k];
                                                    record.isGroupOrderAuditRecordSql = groupOrderAuditRecordSqlArray[k];
                                                    record.isTransLockAudit = transLockAuditArray[k];
                                                    record.transLockAuditTimeThresholdTransactionLock = transLockAuditTimeThresholdTransactionLockArray2[k];
                                                    record.isTransLockAuditRecordSql = transLockAuditRecordSqlArray3[k];
                                                    record.isConvLinkAudit = convLinkAuditArray[k];
                                                    record.isMemManageAudit = memManageAuditArray[k];
                                                    if ("ALL".equals(dataBaseNameArray[k])) {
                                                        HashMap<String, Record> hashMap2 = this.hashMap;
                                                        StringBuilder sb2 = new StringBuilder();
                                                        transLockAuditRecordSqlArray2 = transLockAuditRecordSqlArray3;
                                                        sb2.append(appNameArray[k]);
                                                        sb2.append(dataBaseNameArray[k]);
                                                        hashMap2.put(sb2.toString(), record);
                                                    } else {
                                                        transLockAuditRecordSqlArray2 = transLockAuditRecordSqlArray3;
                                                        HashMap<String, Record> hashMap3 = this.hashMap;
                                                        hashMap3.put(appNameArray[k] + dataBaseName, record);
                                                    }
                                                    k++;
                                                    transLockAuditTimeThresholdTransactionLockArray2 = transLockAuditTimeThresholdTransactionLockArray2;
                                                    transLockAuditRecordSqlArray3 = transLockAuditRecordSqlArray2;
                                                    appNameArray = appNameArray;
                                                }
                                                return;
                                            }
                                            return;
                                        }
                                    } else {
                                        str2 = TAG;
                                    }
                                } else {
                                    str2 = TAG;
                                }
                                if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                                    Log.w(str2, "getRecordsFromProvider get bundle value return null");
                                }
                            } catch (ArrayIndexOutOfBoundsException e7) {
                                str = TAG;
                            }
                        } catch (ArrayIndexOutOfBoundsException e8) {
                            str = TAG;
                            if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException e9) {
                        str = TAG;
                        if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e10) {
                    str = TAG;
                    if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e11) {
                str = TAG;
                if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                    Log.w(str, "An exception occurred when getting information from the bundle.");
                }
            }
        } else if ((sDebugFlags & hwAuditDbgWarning) != 0) {
            Log.w(TAG, "getRecordsFromProvider resolver.call return null");
        }
    }

    private Record getRecord(String appName) {
        Record record;
        synchronized (this.hashMap) {
            HashMap<String, Record> hashMap2 = this.hashMap;
            record = hashMap2.get(appName + this.mMainDbFile);
            if (record == null) {
                HashMap<String, Record> hashMap3 = this.hashMap;
                record = hashMap3.get("ALL" + this.mMainDbFile);
            }
            if (record == null) {
                HashMap<String, Record> hashMap4 = this.hashMap;
                record = hashMap4.get(appName + "ALL");
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
        if (record.isTimeAudit) {
            retVal = 0 | 1;
            if (record.isTimeAuditTurnOnDetailedStatistics) {
                retVal |= 2;
            }
            if (record.isTimeAuditRecordExecPlan) {
                retVal |= 8;
            }
            if (record.isTimeAuditRecordSql) {
                retVal |= 4;
            }
            if (record.isConvLinkAudit) {
                retVal |= 128;
            }
            if (record.isMemManageAudit) {
                retVal |= 256;
            }
        }
        if (record.isGroupOrderAudit) {
            retVal |= 16;
            if (record.isGroupOrderAuditRecordSql) {
                retVal |= 4;
            }
        }
        if (record.isTransLockAudit) {
            retVal |= 32;
            if (record.isTransLockAuditRecordSql) {
                retVal |= 4;
            }
        }
        if (record.isStatInfoAudit) {
            return retVal | 64;
        }
        return retVal;
    }

    private void persistTimeAudit() {
        Record record = this.mCurrRec;
        if (record != null && record.isTimeAudit && this.mVdbeExecTime >= this.mCurrRec.timeAuditTimeThresholdSql) {
            ContentValues values = new ContentValues();
            values.put(AppAssociate.ASSOC_PID, Integer.valueOf(this.mPid));
            values.put("feature_id", (Integer) 1);
            values.put("pkg_name", this.mPkgName);
            values.put("main_db_file", this.mMainDbFile);
            values.put("vdbe_exec_time", Integer.valueOf(this.mVdbeExecTime));
            values.put("vdbe_commit_time", Integer.valueOf(this.mVdbeCommitTime));
            values.put("vdbe_tran_time", Integer.valueOf(this.mVdbeTranTime));
            values.put("cpu_freq_info", getCpuFreqInfoForPid(this.mCpuNum));
            values.put("cpu_num", Integer.valueOf(this.mCpuNum));
            values.put("file_read_count", Integer.valueOf(this.mFileReadCount));
            values.put("read_page_count", Integer.valueOf(this.mReadPageCount));
            values.put("travel_row_num", Integer.valueOf(this.mTravelRowNum));
            values.put("result_row_num", Integer.valueOf(this.mResultRowNum));
            values.put("vbde_step", Integer.valueOf(this.mVbdeStep));
            values.put("vdbe_begin_date_time", this.mVdbeBeginDateTime);
            if (this.mCurrRec.isMemManageAudit) {
                values.put("begin_mem_used", Integer.valueOf(this.mBeginMemUsed));
                values.put("end_mem_used", Integer.valueOf(this.mEndMemUsed));
                values.put("max_mem_used", Integer.valueOf(this.mMaxMemUsed));
                values.put("pager_mem_used", Integer.valueOf(this.mPagerMemUsed));
            }
            putProcessValues(values);
            if (this.mCurrRec.isTimeAuditRecordExecPlan && this.mEqpList.size() > 0) {
                values.put("eqp", this.mEqpList.toString());
            }
            if (this.mCurrRec.isTimeAuditTurnOnDetailedStatistics) {
                values.put("file_read_time", Integer.valueOf(this.mFileReadTime));
                values.put("read_page_time", Integer.valueOf(this.mReadPageTime));
                values.put("sort_time", Integer.valueOf(this.mSortTime));
            }
            if (this.mCurrRec.isTimeAuditRecordSql) {
                values.put("sql_stmt", this.sqlList.toString());
            }
            if (this.mCurrRec.isConvLinkAudit) {
                values.put("attach_file_count", Integer.valueOf(this.mAttachFileCount));
                values.put("conn_count", Integer.valueOf(this.mConnctionCount));
            }
            putValuesToResolver(values);
        }
    }

    private void putValuesToResolver(ContentValues values) {
        try {
            this.resolver.insert(AUDIT_PROVIDER_URI, values);
        } catch (IllegalArgumentException e) {
            if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                Log.w(TAG, "insert time audit failed");
            }
        }
    }

    private void persistStatInfo() {
        if (this.mCurrRec.isStatInfoAudit && this.mStat1List.size() != 0) {
            ContentValues values = new ContentValues();
            values.put(AppAssociate.ASSOC_PID, Integer.valueOf(this.mPid));
            values.put("feature_id", (Integer) 2);
            values.put("pkg_name", this.mPkgName);
            values.put("app_name", this.mCurProcess);
            values.put("main_db_file", this.mMainDbFile);
            values.put("stat", this.mStat1List.toString());
            values.put("vdbe_begin_date_time", this.mVdbeBeginDateTime);
            try {
                this.resolver.insert(AUDIT_PROVIDER_URI, values);
            } catch (IllegalArgumentException e) {
                if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                    Log.w(TAG, "insert stat info failed");
                }
            }
        }
    }

    private void persistGroupOrderAudit() {
        if (this.mCurrRec.isGroupOrderAudit && this.mVdbeExecTime >= this.mCurrRec.groupOrderAuditTimeThresholdSql && this.mGrpOrderbyEqpList.size() != 0) {
            ContentValues values = new ContentValues();
            values.put(AppAssociate.ASSOC_PID, Integer.valueOf(this.mPid));
            values.put("feature_id", (Integer) 3);
            values.put("pkg_name", this.mPkgName);
            values.put("main_db_file", this.mMainDbFile);
            values.put("sort_row_num", Integer.valueOf(this.mSortRowNum));
            values.put("sort_time", Integer.valueOf(this.mSortTime));
            values.put("group_order_by_eqp", this.mGrpOrderbyEqpList.toString());
            values.put("vdbe_exec_time", Integer.valueOf(this.mVdbeExecTime));
            values.put("vdbe_begin_date_time", this.mVdbeBeginDateTime);
            putProcessValues(values);
            if (this.mCurrRec.isGroupOrderAuditRecordSql) {
                values.put("sql_stmt", this.sqlList.toString());
            }
            try {
                this.resolver.insert(AUDIT_PROVIDER_URI, values);
            } catch (IllegalArgumentException e) {
                if ((sDebugFlags & hwAuditDbgWarning) != 0) {
                    Log.w(TAG, "insert group order by failed");
                }
            }
        }
    }

    private void persistTransLockAudit() {
        if (this.mCurrRec.isTransLockAudit && this.mTransLockTime >= this.mCurrRec.transLockAuditTimeThresholdTransactionLock) {
            ContentValues values = new ContentValues();
            values.put(AppAssociate.ASSOC_PID, Integer.valueOf(this.mPid));
            values.put("feature_id", (Integer) 4);
            values.put("pkg_name", this.mPkgName);
            values.put("main_db_file", this.mMainDbFile);
            values.put("trans_lock_time", Integer.valueOf(this.mTransLockTime));
            values.put("vdbe_exec_ret", Integer.valueOf(this.mVdbeExecRet));
            values.put("vdbe_begin_date_time", this.mVdbeBeginDateTime);
            String str = this.mErrMsg;
            if (str != null) {
                values.put("err_msg", str);
            }
            putProcessValues(values);
            if (this.mCurrRec.isTransLockAuditRecordSql) {
                values.put("sql_stmt", this.sqlList.toString());
            }
            try {
                this.resolver.insert(AUDIT_PROVIDER_URI, values);
            } catch (IllegalArgumentException e) {
                if ((sDebugFlags & hwAuditDbgWarning) != 0) {
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
            if (record.isTimeAudit) {
                Log.d(TAG, "timeAudit:");
                Log.d(TAG, "VdbeExec Time:" + this.mVdbeExecTime + " VdbeTran Time:" + this.mVdbeTranTime + " CPU Freq Info:" + getCpuFreqInfoForPid(this.mCpuNum) + " Cpu Core ID:" + this.mCpuNum + " FileRead Count:" + this.mFileReadCount + " ReadPage Count:" + this.mReadPageCount + " TravelRow Num:" + this.mTravelRowNum + " ResultRow Num:" + this.mResultRowNum + " SortRow Num:" + this.mSortRowNum + " Vdbe Step:" + this.mVbdeStep);
                if (record.isTimeAuditRecordExecPlan && this.mEqpList.size() > 0) {
                    Log.d(TAG, "record exec plan:" + this.mEqpList.toString());
                }
                if (record.isTimeAuditTurnOnDetailedStatistics) {
                    Log.d(TAG, "detailed statistics");
                    Log.d(TAG, "FileRead Time:" + this.mFileReadTime + " ReadPage Time:" + this.mReadPageTime + " Sort Time:" + this.mSortTime);
                }
            }
            if (record.isTimeAuditRecordSql || record.isGroupOrderAuditRecordSql || record.isTransLockAuditRecordSql) {
                Log.d(TAG, "record SQL statements:" + this.sqlList.toString());
            }
        }
    }

    private void printAuditLog4Conf() {
        Record record = this.mCurrRec;
        if (!(record == null || (sDebugFlags & hwAuditDbgPrint) == 0)) {
            Log.d(TAG, "---------------------------------------------------------------------");
            Log.d(TAG, this.mVdbeBeginDateTime + SettingsStringUtil.DELIMITER + this.mPkgName + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + this.mCurProcess + "(" + this.mPid + ") mAuditFlags=" + this.mAuditFlags);
            if (this.mCallingPid != this.mPid) {
                Log.d(TAG, "Calling Process:" + this.mCallingProcess + "(" + this.mCallingPid + ")");
            }
            printTimeAudit();
            if (record.isStatInfoAudit) {
                Log.d(TAG, "statInfoAudit:");
                if (this.mStat1List.size() > 0) {
                    Log.d(TAG, "statInfoAudit stat:" + this.mStat1List.toString());
                } else {
                    Log.d(TAG, "statInfoAudit stat:none");
                }
            }
            if (record.isGroupOrderAudit) {
                Log.d(TAG, "groupOrderAudit:");
                Log.d(TAG, "SortRow Num:" + this.mSortRowNum + " Sort Time:" + this.mSortTime);
                int count = this.mGrpOrderbyEqpList.size();
                for (int idx = 0; idx < count; idx++) {
                    Log.d(TAG, this.mGrpOrderbyEqpList.get(idx));
                }
                Log.d(TAG, "VdbeExec return:" + this.mVdbeExecRet);
                if (this.mErrMsg != null) {
                    Log.d(TAG, "Error Msg:" + this.mErrMsg);
                }
            }
            if (record.isTransLockAudit) {
                Log.d(TAG, "transLockAudit:");
                Log.d(TAG, "TransLock Time:" + this.mTransLockTime);
                Log.d(TAG, this.mPkgName + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + this.mCurProcess + "(" + this.mPid + ")");
                if (this.mCallingPid != this.mPid) {
                    Log.d(TAG, "Calling Process:" + this.mCallingProcess + "(" + this.mCallingPid + ")");
                }
            }
            if (record.isConvLinkAudit) {
                Log.d(TAG, "convLinkAudit:");
                Log.d(TAG, "Con Num:" + this.mConnctionCount + " AttachFile Count:" + this.mAttachFileCount);
            }
            if (record.isMemManageAudit) {
                Log.d(TAG, "memManageAudit");
                Log.d(TAG, "Begin MemUsed:" + this.mBeginMemUsed + " End MemUsed:" + this.mEndMemUsed + " Max MemUsed:" + this.mMaxMemUsed + " Pager MemUsed:" + this.mPagerMemUsed);
            }
            Log.d(TAG, "---------------------------------------------------------------------");
        }
    }

    private void clearAuditLog() {
        this.mEqpList.clear();
        this.mGrpOrderbyEqpList.clear();
        this.sqlList.clear();
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
        this.mEqpList.add(eqp);
    }

    private void putGrpOrderby(String grpOrderbyEqp) {
        this.mGrpOrderbyEqpList.add(grpOrderbyEqp);
    }

    private void putSql(String sql) {
        this.sqlList.add(sql);
    }

    private void putStat1(String stat) {
        this.mStat1List.add(stat);
    }

    private static String getCpuFreqInfoForPid(int cpu) {
        String[] outStrings = new String[1];
        Process.readProcFile("/sys/devices/system/cpu/cpu" + cpu + "/cpufreq/scaling_cur_freq", new int[]{4128}, outStrings, null, null);
        return outStrings[0];
    }

    private static String getProcessName(int pid) {
        List<ActivityManager.RunningAppProcessInfo> appList = null;
        try {
            appList = ActivityManagerNative.getDefault().getRunningAppProcesses();
        } catch (RemoteException e) {
            if ((sDebugFlags & hwAuditDbgWarning) != 0) {
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

    private void putProcessValues(ContentValues values) {
        int i = this.mCallingPid;
        if (i != this.mPid) {
            values.put("calling_pid", Integer.valueOf(i));
            values.put("app_name", this.mCurProcess);
            values.put("calling_process_name", this.mCallingProcess);
            return;
        }
        values.put("app_name", this.mCallingProcess);
        values.put("calling_process_name", this.mCallingProcess);
    }
}

package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.HwTelephony;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import android.util.SparseArray;
import com.android.internal.telephony.HwHiCureDetection;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.dataconnection.ApnContextEx;
import com.huawei.internal.telephony.dataconnection.DcTrackerEx;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class HwHiCureDetection {
    private static final String ACTION_HICURE_DETECTION = "huawei.intent.action.HI_DATA_CHECK";
    private static final String ACTION_HICURE_RESULT = "huawei.intent.action.HICURE_RESULT";
    private static final int BEARER_EHRPD = 13;
    private static final int BEARER_LTE = 14;
    public static final int BLOCK_LEVEL_EXTREMELY_BAD = 1;
    public static final int BLOCK_LEVEL_SLIGHTLY_BAD = 0;
    public static final int CHINA_MCC = 156;
    private static final String DEFAULT_APN_TYPE = "default";
    private static final int DEFAULT_PHONE_COUNT = 3;
    public static final int DIAGNOSE_RESULT_APN = 3;
    public static final int DIAGNOSE_RESULT_ATACH = 5;
    public static final int DIAGNOSE_RESULT_CONNETCT = 4;
    public static final int DIAGNOSE_RESULT_DATA_SWITCH = 1;
    public static final int DIAGNOSE_RESULT_OTHER = 0;
    public static final int DIAGNOSE_RESULT_ROAMING_SWITCH = 2;
    private static final int EVENT_NETWORK_REJINFO_IND = 1;
    private static final int EVENT_TYR_DATA_DETECT = 0;
    private static final String EXTRA_DIAGNOSE_RESULT = "extra_diagnose_result";
    private static final String EXTRA_HICURE_FAULT_ID = "extra_fault_id";
    private static final String EXTRA_METHOD = "extra_method";
    private static final String EXTRA_RESULT = "extra_result";
    private static final String EXTRA_SUB_ID = "sub_id";
    private static final String EXTRA_TMER_LENGTH = "extra_timer_result";
    private static final int FIVE_SECONDS = 5000;
    public static final int INVALID_MCC = 999;
    public static final int METHOD_NULL = 0;
    public static final int OPEN_DATA_SWITCH = 1;
    public static final int OPEN_ROAMING_SWITCH = 2;
    private static final String PERMISSION_NOTIFY_HICURE_RESULT = "huawei.permission.NOTIFY_HICURE_RESULT";
    private static final String PERMISSION_SEND_HICURE_DETECTION = "huawei.permission.SMART_NOTIFY_FAULT";
    private static final String PHONE_FAULT_ID = "FFFFFFFFF";
    public static final int REATACH = 5;
    public static final int RECONNETCT = 4;
    public static final int RESET_APN = 3;
    public static final int RESULT_CONN_PENDGIN = 4;
    public static final int RESULT_FAIL = 0;
    public static final int RESULT_FAIL_TIMER_LEHGTH = 180;
    public static final int RESULT_NORMAL = 2;
    public static final int RESULT_NORMAL_TIMER_LENGTH = 30;
    public static final int RESULT_NOT_CURE = 3;
    public static final int RESULT_NOT_CURE_TIMER_LENGTH = 60;
    public static final int RESULT_PENDING_STATUS_TIMER_LENGTH = 1;
    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_SUCCESS_TIMER_LENGTH = 30;
    private static final String SYSTEM_SERVER_NAME = "com.huawei.systemserver";
    private static final String TAG = "HwHiCureDetection";
    private static final int THREE_SECONDS = 3000;
    private static final int TWO_SECONDS = 2000;
    private static final String WHERE_BEARER_UNSPECIFIED = " and bearer=0";
    private static HwHiCureDetection mHwHiCureDetection;
    private Context mContext;
    private DetectionHandler mDetectionHandler;
    private HiCureBroadcastReceiver mHiCureBroadcastReceiver;
    private SparseArray<PhoneExt> mPhoneMap = new SparseArray<>();
    private SparseArray<RejectInfo> mRejctCauses = new SparseArray<>(3);

    private HwHiCureDetection(Context context) {
        this.mContext = context;
        HandlerThread detectionHandlerThread = new HandlerThread("DetectionHandler");
        detectionHandlerThread.start();
        this.mDetectionHandler = new DetectionHandler(detectionHandlerThread.getLooper());
        this.mHiCureBroadcastReceiver = new HiCureBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_HICURE_DETECTION);
        this.mContext.registerReceiver(this.mHiCureBroadcastReceiver, filter, PERMISSION_SEND_HICURE_DETECTION, this.mDetectionHandler);
    }

    public static synchronized HwHiCureDetection createHwHiCureDetection(Context context) {
        HwHiCureDetection hwHiCureDetection;
        synchronized (HwHiCureDetection.class) {
            if (mHwHiCureDetection == null) {
                mHwHiCureDetection = new HwHiCureDetection(context);
            }
            hwHiCureDetection = mHwHiCureDetection;
        }
        return hwHiCureDetection;
    }

    /* access modifiers changed from: private */
    public static void tryStartDetect(Intent intent) {
        if (intent == null) {
            RlogEx.e(TAG, "tryStartDetect: intent is null");
            return;
        }
        RlogEx.i(TAG, "tryStartDetect: intent = " + intent);
        HwHiCureDetection hwHiCureDetection = mHwHiCureDetection;
        if (hwHiCureDetection != null) {
            hwHiCureDetection.mDetectionHandler.sendEmptyMessage(0);
        }
    }

    public void put(int phoneId, PhoneExt phoneExt) {
        registerNetRejectInfo(phoneId, phoneExt);
        this.mPhoneMap.put(phoneId, phoneExt);
        RlogEx.i(TAG, "put: phoneId = " + phoneId + " phone = " + phoneExt);
    }

    private void registerNetRejectInfo(int phoneId, PhoneExt phone) {
        PhoneExt cachePhone = this.mPhoneMap.get(phoneId, null);
        if (cachePhone != null) {
            cachePhone.getCi().unSetOnNetReject(this.mDetectionHandler);
        }
        if (phone != null) {
            phone.getCi().setOnNetReject(this.mDetectionHandler, 1, Integer.valueOf(phoneId));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void parseNetworkRejInfo(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar.getException() != null) {
            RlogEx.w(TAG, "there is an exception");
            return;
        }
        int phoneId = ((Integer) ar.getUserObj()).intValue();
        String[] datas = (String[]) ar.getResult();
        if (datas.length <= 3) {
            RlogEx.w(TAG, "parseNetworkRejInfo: length is invalid:" + datas.length);
            return;
        }
        try {
            int rejectInfoCause = Integer.parseInt(datas[2]);
            if (Integer.parseInt(datas[3]) == 2) {
                this.mRejctCauses.put(phoneId, new RejectInfo(System.currentTimeMillis(), rejectInfoCause));
                RlogEx.i(TAG, "parseNetworkRejInfo:" + this.mRejctCauses);
            }
        } catch (NumberFormatException e) {
            RlogEx.e(TAG, "error parsing NetworkReject fail!");
        }
    }

    private boolean canPerformReattach(int subId) {
        if (!isCallIdle() || isDoRecovery() || !isPhoneTypeGsm(subId) || !isReattachAllowed(subId)) {
            return false;
        }
        return true;
    }

    private boolean isPhoneTypeGsm(int subId) {
        PhoneExt phone = this.mPhoneMap.get(SubscriptionManagerEx.getPhoneId(subId), null);
        return phone != null && phone.isPhoneTypeGsm();
    }

    private boolean isReattachAllowed(int subId) {
        RejectInfo rejectInfo = this.mRejctCauses.get(SubscriptionManagerEx.getPhoneId(subId), null);
        if (rejectInfo == null) {
            return true;
        }
        RlogEx.i(TAG, "isReattachAllowed rejectInfo = " + rejectInfo);
        if (System.currentTimeMillis() - rejectInfo.mHappenTime > 30000) {
            return true;
        }
        return Stream.of((Object[]) new Integer[]{9, 10, 54}).noneMatch(new Predicate() {
            /* class com.android.internal.telephony.$$Lambda$HwHiCureDetection$Q7ReDMM8_M7fhTa82QdgN_pCNg */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return HwHiCureDetection.lambda$isReattachAllowed$0(HwHiCureDetection.RejectInfo.this, (Integer) obj);
            }
        });
    }

    static /* synthetic */ boolean lambda$isReattachAllowed$0(RejectInfo rejectInfo, Integer code) {
        return code.intValue() == rejectInfo.mCause;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startDataDetect() {
        int subId = getDefaultDataSubId();
        if (subId == Integer.MAX_VALUE) {
            RlogEx.e(TAG, "tryStartDetect: subId is invalid");
            return;
        }
        RlogEx.i(TAG, "startDataDetect");
        if (isDataLinkNormally()) {
            notifyHicureResult(3, 30, 0, 0);
            RlogEx.i(TAG, "Data has connectioned again.");
        } else if (!isCallIdle() || isDoRecovery()) {
            RlogEx.i(TAG, "in call or doRecovery");
            notifyHicureResult(3, 30, 0, 0);
        } else if (!canPerformHicure() || checkDataSubChanged(subId)) {
            RlogEx.i(TAG, "sub changed or in disconneting or connecting");
            notifyHicureResult(4, 1, 0, 0);
        } else {
            int cureMethod = 3;
            int diagnoseRresult = 3;
            if (needRestoreApn(subId)) {
                restoreDefaultApn(subId);
                delay(5000);
            } else if (canPerformReconnect(subId)) {
                reConnectData(subId);
                diagnoseRresult = 4;
                cureMethod = 4;
                delay(2000);
            } else {
                diagnoseRresult = 0;
                cureMethod = 0;
            }
            if (isDataLinkNormally()) {
                notifyHicureResult(1, 30, diagnoseRresult, cureMethod);
            } else if (!canPerformHicure() || checkDataSubChanged(subId)) {
                RlogEx.i(TAG, "sub changed or in disconneting or connecting");
                notifyHicureResult(4, 1, 0, 0);
            } else if (canPerformReattach(subId)) {
                reAttach(subId);
                delay(3000);
                if (isDataLinkNormally()) {
                    notifyHicureResult(1, 30, 5, 5);
                } else {
                    notifyHicureResult(0, RESULT_FAIL_TIMER_LEHGTH, 0, 0);
                }
            } else {
                RlogEx.i(TAG, "can't perform reattach.");
                notifyHicureResult(4, 1, 0, 0);
            }
        }
    }

    private boolean canPerformReconnect(int subId) {
        DcTrackerEx dcTracker = getDcTracker(SubscriptionManagerEx.getPhoneId(subId));
        if (dcTracker == null) {
            RlogEx.e(TAG, "canPerformReconnect: get dct error. subId = " + subId);
            return false;
        }
        ApnContextEx apnContextEx = dcTracker.getApnContextByType(DEFAULT_APN_TYPE);
        if (apnContextEx == null || apnContextEx.getState() != ApnContextEx.StateEx.RETRYING) {
            return false;
        }
        return true;
    }

    private boolean checkDataSubChanged(int subId) {
        return getDefaultDataSubId() != subId;
    }

    private boolean isPdpConnectPending(String apnType) {
        DcTrackerEx dcTracker;
        int phoneCount = TelephonyManagerEx.getPhoneCount();
        int i = 0;
        while (i < phoneCount && (dcTracker = getDcTracker(i)) != null) {
            DcTrackerEx.State state = dcTracker.getState(apnType);
            RlogEx.i(TAG, "isPdpConnectPending: [" + i + "] state[" + state + "],apnType: " + apnType);
            if (state == DcTrackerEx.State.CONNECTING || state == DcTrackerEx.State.DISCONNECTING || state == DcTrackerEx.State.CONNECTED) {
                return true;
            }
            i++;
        }
        return false;
    }

    private boolean canPerformHicure() {
        for (String apnType : new String[]{DEFAULT_APN_TYPE, "mms", "supl", "xcap", "dun", "hipri", "fota", "cbs", "ia", "bip0", "bip1", "bip2", "bip3", "bip4", "bip5", "bip6", "internaldefault"}) {
            if (isPdpConnectPending(apnType)) {
                return false;
            }
        }
        return true;
    }

    private boolean needRestoreApn(int subId) {
        List<ApnInfo> apnInfos = getApnList(subId);
        boolean hasEditedApn = apnInfos.stream().anyMatch($$Lambda$HwHiCureDetection$mzTkmDIJCQb7Q_N1EetjHZiya14.INSTANCE);
        boolean hasDefaultApn = apnInfos.stream().anyMatch($$Lambda$HwHiCureDetection$g3c00sKLtWBg4gjCWswbskSCjY.INSTANCE);
        boolean hasSelectApn = hasSelectedApn(subId);
        RlogEx.i(TAG, "needRestoreApn: hasEditedApn[" + hasEditedApn + "] hasDefaultApn[" + hasDefaultApn + "] hasSelectApn[" + hasSelectApn + "]");
        return !hasEditedApn && (!hasDefaultApn || !hasSelectApn);
    }

    private boolean isDataLinkNormally() {
        return (isDataConnected() || isWifiConnected()) && isNetworkOnline();
    }

    private void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            RlogEx.e(TAG, "InterruptedException happened");
        }
    }

    private DcTrackerEx getDcTracker(int phoneId) {
        PhoneExt phone = this.mPhoneMap.get(phoneId, null);
        if (phone != null) {
            return phone.getDcTracker();
        }
        RlogEx.e(TAG, "getDcTracker: phone is null");
        return null;
    }

    private int getDefaultDataSubId() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "multi_sim_data_call", HwSignalStrength.WCDMA_STRENGTH_INVALID);
    }

    private boolean isCallIdle() {
        DcTrackerEx dcTracker = getDcTracker(0);
        if (dcTracker == null || dcTracker.isPhoneStateIdle()) {
            return true;
        }
        return false;
    }

    private boolean isDoRecovery() {
        if (Settings.System.getInt(this.mContext.getContentResolver(), "radio.data.stall.recovery.action", 0) != 0) {
            return true;
        }
        return false;
    }

    private void reConnectData(int subId) {
        DcTrackerEx dcTracker = getDcTracker(SubscriptionManagerEx.getPhoneId(subId));
        if (dcTracker == null) {
            RlogEx.e(TAG, "reConnectData: dcTracker is null");
            return;
        }
        RlogEx.i(TAG, "reConnectData");
        dcTracker.setupDataOnAllConnectableApns("pdpReset");
    }

    private void reAttach(int subId) {
        PhoneExt phone = this.mPhoneMap.get(SubscriptionManagerEx.getPhoneId(subId), null);
        RlogEx.i(TAG, "reAttach");
        if (phone != null) {
            phone.reRegisterNetwork((Message) null);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0046, code lost:
        if (0 == 0) goto L_0x0049;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0049, code lost:
        com.huawei.android.telephony.RlogEx.i(com.android.internal.telephony.HwHiCureDetection.TAG, "hasSelectedApn: " + r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x005d, code lost:
        if (r2 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x005f, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0037, code lost:
        if (r3 != null) goto L_0x0039;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0039, code lost:
        r3.close();
     */
    private boolean hasSelectedApn(int subId) {
        String apn = null;
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(ContentUris.withAppendedId(Uri.parse("content://telephony/carriers/preferapn"), (long) subId), new String[]{"apn"}, null, null, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                apn = cursor.getString(0);
            }
        } catch (SQLException | IllegalArgumentException e) {
            RlogEx.e(TAG, "hasSelectedApn fail:");
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0074, code lost:
        if (r10 != null) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x008d, code lost:
        if (r10 == null) goto L_0x0090;
     */
    private List<ApnInfo> getApnList(int subId) {
        String str;
        List<ApnInfo> apnList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(ContentUris.withAppendedId(Uri.parse("content://telephony/carriers/subId"), (long) subId), new String[]{"apn", "type", "edited"}, getOperatorNumericSelection(subId), null, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    ApnInfo apnInfo = new ApnInfo();
                    boolean z = false;
                    boolean isNull = cursor.isNull(0);
                    String str2 = BuildConfig.FLAVOR;
                    if (isNull) {
                        str = str2;
                    } else {
                        try {
                            str = cursor.getString(0);
                        } catch (SQLException | IllegalArgumentException e) {
                            RlogEx.e(TAG, "getApnList exception");
                        }
                    }
                    apnInfo.mName = str;
                    if (!cursor.isNull(1)) {
                        str2 = cursor.getString(1);
                    }
                    apnInfo.mType = str2;
                    if ((cursor.isNull(2) ? 0 : cursor.getInt(2)) == 1) {
                        z = true;
                    }
                    apnInfo.mEdited = z;
                    apnList.add(apnInfo);
                }
            }
        } catch (Exception e2) {
            RlogEx.e(TAG, "Other exception.");
            if (0 != 0) {
                cursor.close();
            }
            RlogEx.i(TAG, "getApnList: " + apnList);
            return apnList;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private String getOperatorNumericSelection(int subId) {
        String operator = TelephonyManagerEx.getSimOperator(subId);
        if (operator == null || operator.isEmpty()) {
            return BuildConfig.FLAVOR;
        }
        boolean chinaTelcomCard = HwTelephonyManagerInner.getDefault().isCTSimCard(SubscriptionManagerEx.getSlotIndex(subId));
        if (chinaTelcomCard) {
            operator = SystemPropertiesEx.get("ro.cdma.home.operator.numeric", "46003");
        }
        String where = "numeric=\"" + operator + "\"";
        if (chinaTelcomCard) {
            where = where + getTelecomOperatorNumericSelection(subId);
        }
        RlogEx.i(TAG, "getOperatorNumericSelection: " + where);
        return where;
    }

    private String getTelecomOperatorNumericSelection(int subId) {
        String telecomSelection;
        int networkType = TelephonyManagerEx.getNetworkType(subId);
        if (networkType == BEARER_EHRPD || networkType == BEARER_LTE) {
            telecomSelection = " and ((visible = 1 and (bearer=14 or bearer=13)";
        } else {
            telecomSelection = " and ((visible = 1" + WHERE_BEARER_UNSPECIFIED;
        }
        return BuildConfig.FLAVOR + (telecomSelection + ") or visible is null)");
    }

    private boolean isDataConnected() {
        return TelephonyManagerEx.getDataState() == 2;
    }

    private boolean isNetworkOnline() {
        Process ipProcess;
        Runtime runtime = Runtime.getRuntime();
        try {
            if (isOverSeaUser()) {
                ipProcess = runtime.exec("ping -c 3 www.google.com");
            } else {
                ipProcess = runtime.exec("ping -c 3 www.baidu.com");
            }
            if (ipProcess.waitFor() == 0) {
                return true;
            }
            return false;
        } catch (IOException | InterruptedException e) {
            RlogEx.e(TAG, "Exception happened");
            return false;
        }
    }

    private boolean isOverSeaUser() {
        return SystemPropertiesEx.getInt("ro.config.hw_optb", INVALID_MCC) != 156;
    }

    private boolean isWifiConnected() {
        NetworkInfo networkInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        int networkType = networkInfo.getType();
        if (!networkInfo.isConnected() || networkType != 1) {
            return false;
        }
        return true;
    }

    private void notifyHicureResult(int result, int timerLength, int diagnoseRresult, int cureMethod) {
        Intent intent = new Intent(ACTION_HICURE_RESULT);
        intent.setPackage(SYSTEM_SERVER_NAME);
        intent.setFlags(1073741824);
        intent.putExtra(EXTRA_RESULT, result);
        intent.putExtra(EXTRA_TMER_LENGTH, timerLength);
        intent.putExtra(EXTRA_DIAGNOSE_RESULT, diagnoseRresult);
        intent.putExtra(EXTRA_METHOD, cureMethod);
        intent.putExtra(EXTRA_HICURE_FAULT_ID, PHONE_FAULT_ID);
        this.mContext.sendBroadcastAsUser(intent, UserHandleEx.ALL, PERMISSION_NOTIFY_HICURE_RESULT);
        RlogEx.i(TAG, "notifyHicureResult: intent = " + intent);
    }

    private void restoreDefaultApn(int subId) {
        RlogEx.i(TAG, "restoreDefaultApn");
        this.mContext.getContentResolver().delete(ContentUris.withAppendedId(Uri.parse("content://telephony/carriers/restore/subId"), (long) subId), null, null);
    }

    public static class HiCureBroadcastReceiver extends BroadcastReceiver {
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                RlogEx.e(HwHiCureDetection.TAG, "onReceive: context or intent is null");
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                RlogEx.e(HwHiCureDetection.TAG, "onReceive: action is null");
                return;
            }
            RlogEx.i(HwHiCureDetection.TAG, "onReceive: action = " + action);
            char c = 65535;
            if (action.hashCode() == -1881257756 && action.equals(HwHiCureDetection.ACTION_HICURE_DETECTION)) {
                c = 0;
            }
            if (c == 0) {
                HwHiCureDetection.tryStartDetect(intent);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class RejectInfo {
        int mCause;
        long mHappenTime;

        RejectInfo(long happenTime, int cause) {
            this.mHappenTime = happenTime;
            this.mCause = cause;
        }

        public String toString() {
            return "RejectInfo{mHappenTime=" + this.mHappenTime + ", mCause=" + this.mCause + '}';
        }
    }

    /* access modifiers changed from: private */
    public static class ApnInfo {
        boolean mEdited;
        String mName;
        String mType;

        private ApnInfo() {
        }

        public String toString() {
            return "Name[" + this.mName + "] Type[" + this.mType + "] Edited[" + this.mEdited + "]";
        }
    }

    /* access modifiers changed from: private */
    public class DetectionHandler extends Handler {
        public DetectionHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            RlogEx.i(HwHiCureDetection.TAG, "handleMessage: msg.what = " + msg.what);
            int i = msg.what;
            if (i == 0) {
                HwHiCureDetection.this.startDataDetect();
            } else if (i == 1) {
                HwHiCureDetection.this.parseNetworkRejInfo(msg);
            }
        }
    }
}

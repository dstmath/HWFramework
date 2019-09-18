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
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.HwTelephony;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.util.SparseArray;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.vsim.HwVSimConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HwHiCureDetection {
    private static final String ACTION_HICURE_DETECTION = "huawei.intent.action.HI_DATA_CHECK";
    private static final String ACTION_HICURE_RESULT = "huawei.intent.action.HICURE_RESULT";
    private static final int BEARER_EHRPD = 13;
    private static final int BEARER_LTE = 14;
    public static final int BLOCK_LEVEL_EXTREMELY_BAD = 1;
    public static final int BLOCK_LEVEL_SLIGHTLY_BAD = 0;
    public static final int CHINA_MCC = 156;
    private static final String DEFAULT_APN_TYPE = "default";
    public static final int DIAGNOSE_RESULT_APN = 3;
    public static final int DIAGNOSE_RESULT_ATACH = 5;
    public static final int DIAGNOSE_RESULT_CONNETCT = 4;
    public static final int DIAGNOSE_RESULT_DATA_SWITCH = 1;
    public static final int DIAGNOSE_RESULT_OTHER = 0;
    public static final int DIAGNOSE_RESULT_ROAMING_SWITCH = 2;
    private static final int EVENT_TYR_DATA_DETECT = 0;
    private static final String EXTRA_DIAGNOSE_RESULT = "extra_diagnose_result";
    private static final String EXTRA_METHOD = "extra_method";
    private static final String EXTRA_RESULT = "extra_result";
    private static final String EXTRA_SUB_ID = "sub_id";
    private static final String EXTRA_TMER_LENGTH = "extra_timer_result";
    private static final int FIVE_SECONDS = 5000;
    private static final String HIDATA_PACKAGE_NAME = "android";
    public static final int INVALID_MCC = 999;
    public static final int METHOD_NULL = 0;
    public static final int OPEN_DATA_SWITCH = 1;
    public static final int OPEN_ROAMING_SWITCH = 2;
    private static final String PERMISSION_NOTIFY_HICURE_RESULT = "huawei.permission.NOTIFY_HICURE_RESULT";
    private static final String PERMISSION_SEND_HICURE_DETECTION = "huawei.permission.SEND_HICURE_DETECTION";
    public static final int REATACH = 5;
    public static final int RECONNETCT = 4;
    public static final int RESET_APN = 3;
    public static final int RESULT_FAIL = 0;
    public static final int RESULT_FAIL_TIMER_LEHGTH = 180;
    public static final int RESULT_NORMAL = 2;
    public static final int RESULT_NORMAL_TIMER_LENGTH = 30;
    public static final int RESULT_NOT_CURE = 3;
    public static final int RESULT_NOT_CURE_TIMER_LENGTH = 60;
    public static final int RESULT_PENDING_STATUS_TIMER_LENGTH = 1;
    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_SUCCESS_TIMER_LENGTH = 30;
    private static final String TAG = "HwHiCureDetection";
    private static final int THREE_SECONDS = 3000;
    private static final int TWO_SECONDS = 2000;
    private static final String WHERE_BEARER_UNSPECIFIED = " and bearer=0";
    private static HwHiCureDetection mHwHiCureDetection;
    private Context mContext;
    private DetectionHandler mDetectionHandler;
    private HiCureBroadcastReceiver mHiCureBroadcastReceiver;
    private SparseArray<Phone> mPhoneMap = new SparseArray<>();

    private static class ApnInfo {
        /* access modifiers changed from: package-private */
        public boolean mEdited;
        String mName;
        String mType;

        private ApnInfo() {
        }

        public String toString() {
            return "Name[" + this.mName + "] Type[" + this.mType + "] Edited[" + this.mEdited + "]";
        }
    }

    private class DetectionHandler extends Handler {
        public DetectionHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Rlog.d(HwHiCureDetection.TAG, "handleMessage: msg.what = " + msg.what);
            if (msg.what == 0) {
                HwHiCureDetection.this.startDataDetect();
            }
        }
    }

    public static class HiCureBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                Rlog.e(HwHiCureDetection.TAG, "onReceive: action is null");
                return;
            }
            Rlog.d(HwHiCureDetection.TAG, "onReceive: action = " + action);
            char c = 65535;
            if (action.hashCode() == -1881257756 && action.equals(HwHiCureDetection.ACTION_HICURE_DETECTION)) {
                c = 0;
            }
            if (c == 0) {
                HwHiCureDetection.tryStartDetect(intent);
            }
        }
    }

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

    public void put(int subId, Phone phone) {
        this.mPhoneMap.put(subId, phone);
        Rlog.d(TAG, "put: subId = " + subId + " phone = " + phone);
    }

    /* access modifiers changed from: private */
    public static void tryStartDetect(Intent intent) {
        if (intent == null) {
            Rlog.e(TAG, "tryStartDetect: intent is null");
            return;
        }
        Rlog.d(TAG, "tryStartDetect: intent = " + intent);
        if (mHwHiCureDetection != null) {
            mHwHiCureDetection.mDetectionHandler.sendEmptyMessage(0);
        }
    }

    /* access modifiers changed from: private */
    public void startDataDetect() {
        int subId = getDefaultDataSubId();
        if (subId == Integer.MAX_VALUE) {
            Rlog.e(TAG, "tryStartDetect: subId is invalid");
            return;
        }
        Rlog.d(TAG, "startDataDetect");
        if (isDataLinkNormally()) {
            notifyHicureResult(3, 30, 0, 0);
            Rlog.d(TAG, "Data has connectioned again.");
        } else if (!isCallIdle() || isDoRecovery()) {
            Rlog.d(TAG, "in call or doRecovery");
            notifyHicureResult(3, 30, 0, 0);
        } else if (!canPerformHicure() || checkDataSubChanged(subId)) {
            Rlog.d(TAG, "sub changed or in disconneting or connecting");
            notifyHicureResult(3, 1, 0, 0);
        } else {
            int cureMethod = 3;
            int diagnoseRresult = 3;
            if (needRestoreApn(subId)) {
                restoreDefaultApn(subId);
                delay(HwVSimConstants.WAIT_FOR_SIM_STATUS_CHANGED_UNSOL_TIMEOUT);
            } else {
                reConnectData(subId);
                diagnoseRresult = 4;
                cureMethod = 4;
                delay(HwVSimConstants.GET_MODEM_SUPPORT_VERSION_INTERVAL);
            }
            if (isDataLinkNormally()) {
                notifyHicureResult(1, 30, diagnoseRresult, cureMethod);
            } else if (!canPerformHicure() || checkDataSubChanged(subId)) {
                Rlog.d(TAG, "sub changed or in disconneting or connecting");
                notifyHicureResult(3, 1, 0, 0);
            } else {
                reAttach(subId);
                delay(3000);
                if (isDataLinkNormally()) {
                    notifyHicureResult(1, 30, 5, 5);
                } else {
                    notifyHicureResult(0, RESULT_FAIL_TIMER_LEHGTH, 0, 0);
                }
            }
        }
    }

    private boolean checkDataSubChanged(int subId) {
        return getDefaultDataSubId() != subId;
    }

    private boolean isPdpConnectPending(String apnType) {
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        for (int i = 0; i < phoneCount; i++) {
            DcTracker dcTracker = getDcTracker(i);
            if (dcTracker == null) {
                return false;
            }
            DctConstants.State state = dcTracker.getState(apnType);
            Rlog.d(TAG, "isPdpConnectPending: [" + i + "] state[" + state + "],apnType: " + apnType);
            if (state == DctConstants.State.CONNECTING || state == DctConstants.State.DISCONNECTING || state == DctConstants.State.CONNECTED) {
                return true;
            }
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
        boolean hasEditedApn = apnInfos.stream().anyMatch($$Lambda$HwHiCureDetection$0FfuK8zOHT6ksMDg7VY81iP3RsM.INSTANCE);
        boolean hasDefaultApn = apnInfos.stream().anyMatch($$Lambda$HwHiCureDetection$mzTkmDIJCQb7Q_N1EetjHZiya14.INSTANCE);
        boolean hasSelectApn = hasSelectedApn(subId);
        Rlog.i(TAG, "needRestoreApn: hasEditedApn[" + hasEditedApn + "] hasDefaultApn[" + hasDefaultApn + "] hasSelectApn[" + hasSelectApn + "]");
        return !hasEditedApn && (!hasDefaultApn || !hasSelectApn);
    }

    private boolean isDataLinkNormally() {
        return (isDataConnected() || isWifiConnected()) && isNetworkOnline();
    }

    private void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Rlog.e(TAG, "InterruptedException happened");
        }
    }

    private GsmCdmaCallTracker getGsmCdmaCallTracker(int subId) {
        Phone phone = this.mPhoneMap.get(subId, null);
        if (phone == null) {
            Rlog.e(TAG, "getGsmCdmaCallTracker: phone is null");
            return null;
        } else if (phone instanceof GsmCdmaPhone) {
            return ((GsmCdmaPhone) phone).mCT;
        } else {
            return null;
        }
    }

    private ServiceStateTracker getServiceStateTracker(int subId) {
        Phone phone = this.mPhoneMap.get(subId, null);
        if (phone == null) {
            Rlog.e(TAG, "getServiceStateTracker: phone is null");
            return null;
        } else if (phone instanceof GsmCdmaPhone) {
            return ((GsmCdmaPhone) phone).mSST;
        } else {
            return null;
        }
    }

    private DcTracker getDcTracker(int subId) {
        Phone phone = this.mPhoneMap.get(subId, null);
        if (phone != null) {
            return phone.mDcTracker;
        }
        Rlog.e(TAG, "getDcTracker: phone is null");
        return null;
    }

    private int getDefaultDataSubId() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "multi_sim_data_call", Integer.MAX_VALUE);
    }

    private boolean isCallIdle() {
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        for (int i = 0; i < phoneCount; i++) {
            GsmCdmaCallTracker gsmCdmaCallTracker = getGsmCdmaCallTracker(i);
            if (gsmCdmaCallTracker == null || gsmCdmaCallTracker.mState != PhoneConstants.State.IDLE) {
                return false;
            }
        }
        return true;
    }

    private boolean isDoRecovery() {
        if (Settings.System.getInt(this.mContext.getContentResolver(), "radio.data.stall.recovery.action", 0) != 0) {
            return true;
        }
        return false;
    }

    private void reConnectData(int subId) {
        DcTracker dcTracker = getDcTracker(subId);
        if (dcTracker == null) {
            Rlog.e(TAG, "reConnectData: dcTracker is null");
            return;
        }
        Rlog.d(TAG, "reConnectData");
        dcTracker.cleanUpAllConnections("pdpReset");
    }

    private void reAttach(int subId) {
        ServiceStateTracker sst = getServiceStateTracker(subId);
        if (sst == null) {
            Rlog.e(TAG, "reAttach: sst is null");
            return;
        }
        Rlog.d(TAG, "reAttach");
        sst.reRegisterNetwork(null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0058, code lost:
        if (r2 == null) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x005b, code lost:
        android.telephony.Rlog.d(TAG, "hasSelectedApn: " + r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0071, code lost:
        if (r1 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0035, code lost:
        if (r2 != null) goto L_0x0037;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0037, code lost:
        r2.close();
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
            Rlog.e(TAG, "hasSelectedApn fail:" + e.getMessage());
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0073, code lost:
        if (r10 != null) goto L_0x0075;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x009d, code lost:
        if (r10 == null) goto L_0x00a0;
     */
    private List<ApnInfo> getApnList(int subId) {
        List<ApnInfo> apnList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(ContentUris.withAppendedId(Uri.parse("content://telephony/carriers/subId"), (long) subId), new String[]{"apn", HwVSimConstants.EXTRA_NETWORK_SCAN_TYPE, "edited"}, getOperatorNumericSelection(subId), null, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    ApnInfo apnInfo = new ApnInfo();
                    boolean z = false;
                    apnInfo.mName = cursor.isNull(0) ? "" : cursor.getString(0);
                    apnInfo.mType = cursor.isNull(1) ? "" : cursor.getString(1);
                    if ((cursor.isNull(2) ? 0 : cursor.getInt(2)) == 1) {
                        z = true;
                    }
                    apnInfo.mEdited = z;
                    apnList.add(apnInfo);
                }
            }
        } catch (SQLException | IllegalArgumentException e) {
            Rlog.e(TAG, "getApnList:" + e.getMessage());
        } catch (Exception e2) {
            e2.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
            Rlog.d(TAG, "getApnList: " + apnList);
            return apnList;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private String getOperatorNumericSelection(int subId) {
        String operator = TelephonyManager.getDefault().getSimOperator(subId);
        if (operator == null || operator.isEmpty()) {
            return "";
        }
        boolean chinaTelcomCard = isChinaTelcomCard(operator);
        if (chinaTelcomCard) {
            operator = SystemProperties.get("ro.cdma.home.operator.numeric", "46003");
        }
        String where = "numeric=\"" + operator + "\"";
        if (chinaTelcomCard) {
            where = where + getTelecomOperatorNumericSelection(subId);
        }
        Rlog.d(TAG, "getOperatorNumericSelection: " + where);
        return where;
    }

    private boolean isChinaTelcomCard(String simOperator) {
        for (String plmn : new String[]{"46003", "46005", "46011"}) {
            if (plmn.equals(simOperator)) {
                return true;
            }
        }
        return false;
    }

    private String getTelecomOperatorNumericSelection(int subId) {
        String telecomSelection;
        switch (TelephonyManager.getDefault().getNetworkType(subId)) {
            case 13:
            case 14:
                telecomSelection = " and ((visible = 1" + " and (bearer=14 or bearer=13)";
                break;
            default:
                telecomSelection = " and ((visible = 1" + WHERE_BEARER_UNSPECIFIED;
                break;
        }
        String telecomSelection2 = telecomSelection + ") or visible is null)";
        return "" + telecomSelection2;
    }

    private boolean isDataConnected() {
        if (TelephonyManager.getDefault().getDataState() == 2) {
            return true;
        }
        return false;
    }

    private boolean isNetworkOnline() {
        Process ipProcess;
        Runtime runtime = Runtime.getRuntime();
        boolean z = false;
        try {
            if (isOverSeaUser()) {
                ipProcess = runtime.exec("ping -c 3 www.google.com");
            } else {
                ipProcess = runtime.exec("ping -c 3 www.baidu.com");
            }
            if (ipProcess.waitFor() == 0) {
                z = true;
            }
            return z;
        } catch (IOException | InterruptedException e) {
            Rlog.e(TAG, "Exception happened");
            return false;
        }
    }

    private boolean isOverSeaUser() {
        if (SystemProperties.getInt("ro.config.hw_optb", INVALID_MCC) != 156) {
            return true;
        }
        return false;
    }

    private boolean isWifiConnected() {
        NetworkInfo networkInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        boolean z = false;
        if (networkInfo == null) {
            return false;
        }
        int networkType = networkInfo.getType();
        if (networkInfo.isConnected() && networkType == 1) {
            z = true;
        }
        return z;
    }

    private void notifyHicureResult(int result, int timerLength, int diagnoseRresult, int cureMethod) {
        Intent intent = new Intent(ACTION_HICURE_RESULT);
        intent.setPackage(HIDATA_PACKAGE_NAME);
        intent.setFlags(1073741824);
        intent.putExtra(EXTRA_RESULT, result);
        intent.putExtra(EXTRA_TMER_LENGTH, timerLength);
        intent.putExtra(EXTRA_DIAGNOSE_RESULT, diagnoseRresult);
        intent.putExtra(EXTRA_METHOD, cureMethod);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, PERMISSION_NOTIFY_HICURE_RESULT);
        Rlog.d(TAG, "notifyHicureResult: intent = " + intent);
    }

    private void restoreDefaultApn(int subId) {
        Rlog.d(TAG, "restoreDefaultApn");
        this.mContext.getContentResolver().delete(ContentUris.withAppendedId(Uri.parse("content://telephony/carriers/restore/subId"), (long) subId), null, null);
    }
}

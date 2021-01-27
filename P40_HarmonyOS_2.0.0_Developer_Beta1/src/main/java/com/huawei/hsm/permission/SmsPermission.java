package com.huawei.hsm.permission;

import android.app.PendingIntent;
import android.content.Context;
import android.hsm.HwSystemManager;
import android.os.Binder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.permission.IHoldNotifier;
import java.util.ArrayList;
import java.util.List;

public class SmsPermission {
    public static final int AUTHENTICATE_RESULT_ALLOW = 0;
    public static final int AUTHENTICATE_RESULT_ALLOW_FOREVER = 1;
    public static final int AUTHENTICATE_RESULT_DISALLOW = 2;
    public static final int AUTHENTICATE_RESULT_DISALLOW_FOREVER = 3;
    private static final String DIVIDER_CHAR = ":";
    public static final String KEY_AUTHENTICATE_RESULT = "authenticate_result";
    public static final String KEY_SMS_ID = "sms_id";
    public static final int NEED_AUTHENTICATE = 4;
    public static final int PARAMETER_INVALID = 6;
    private static final int RESULT_ERROR_GENERIC_FAILURE = 1;
    public static final int SERVICE_EXCEPTION = 5;
    private static final String TAG = "SmsPermission";
    private static boolean isControl = SystemPropertiesEx.getBoolean("ro.config.hw_wirenetcontrol", false);
    private static SmsPermission mSmsPermission;
    private Context mContext = null;
    private int mLastSmsId;
    private final NotifierBinder mNotifierBinder = new NotifierBinder();
    private HwSystemManager.Notifier mSmsNotifier;

    /* access modifiers changed from: private */
    public final class NotifierBinder extends IHoldNotifier.Stub {
        private NotifierBinder() {
        }

        public int notifyResult(String owner, Bundle bundle) {
            if (SmsPermission.this.mSmsNotifier != null) {
                Log.d(SmsPermission.TAG, "notifyAuthResultOfSmsSend authenticate complete!");
                SmsPermission.this.mSmsNotifier.notifyResult(bundle);
                return 0;
            }
            Log.e(SmsPermission.TAG, "notifyAuthResultOfSmsSend Notifier is NULL!");
            return 0;
        }
    }

    private SmsPermission() {
    }

    public static SmsPermission getInstance() {
        SmsPermission smsPermission;
        synchronized (SmsPermission.class) {
            if (mSmsPermission == null) {
                mSmsPermission = new SmsPermission();
            }
            smsPermission = mSmsPermission;
        }
        return smsPermission;
    }

    public boolean isMmsBlocked() {
        if (!isControl) {
            return false;
        }
        int selectionResult = StubController.holdForGetPermissionSelection(32, Binder.getCallingUid(), Binder.getCallingPid(), null);
        if (selectionResult == 0) {
            Log.e(TAG, "Get selection error");
            return false;
        } else if (2 == selectionResult) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isSmsBlocked(String destAddr, String smsBody, PendingIntent sentIntent) {
        return false;
    }

    public boolean isSmsBlocked(String destAddr, String smsBody, List<PendingIntent> list) {
        return false;
    }

    /* JADX WARN: Type inference failed for: r0v3, types: [com.huawei.hsm.permission.SmsPermission$NotifierBinder, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void authenticateSmsSend(HwSystemManager.Notifier callback, int uidOf3RdApk, int smsId, String smsBody, String smsAddress) {
        Log.d(TAG, "authenticateSmsSend  uid = " + uidOf3RdApk + " smsid = " + smsId);
        if (isParamInvalidForAuth(callback, uidOf3RdApk, smsId, smsBody, smsAddress)) {
            Log.d(TAG, "authenticateSmsSend PARAM IS INVALID!");
            if (callback != null) {
                callback.notifyResult(packDataOfException(smsId, 6));
                return;
            }
            return;
        }
        this.mSmsNotifier = callback;
        this.mLastSmsId = smsId;
        if (StubController.authenticateSmsSend(this.mNotifierBinder, uidOf3RdApk, smsId, smsBody, smsAddress) != 0) {
            Log.e(TAG, "authenticateSmsSend call service exception! sms id = " + smsId);
            callback.notifyResult(packDataOfException(smsId, 5));
        }
    }

    private static Bundle packDataOfException(int smsId, int result) {
        Bundle bundle = new Bundle();
        ArrayList<Integer> smsIdList = new ArrayList<>();
        smsIdList.add(Integer.valueOf(smsId));
        bundle.putIntegerArrayList(KEY_SMS_ID, smsIdList);
        ArrayList<Integer> resultList = new ArrayList<>();
        resultList.add(Integer.valueOf(result));
        bundle.putIntegerArrayList(KEY_AUTHENTICATE_RESULT, resultList);
        return bundle;
    }

    private static boolean isParamInvalidForAuth(HwSystemManager.Notifier notifyResult, int callingUid, int smsId, String smsBody, String smsAddress) {
        return notifyResult == null || callingUid < 0 || Binder.getCallingUid() != callingUid || TextUtils.isEmpty(smsAddress);
    }

    private static void sendFakeIntents(List<PendingIntent> sentIntents) {
        if (!(sentIntents == null || sentIntents.isEmpty())) {
            int intentSize = sentIntents.size();
            for (int i = 0; i < intentSize; i++) {
                sendFakeIntent(sentIntents.get(i));
            }
        }
    }

    private static void sendFakeIntent(PendingIntent PI) {
        if (PI != null) {
            try {
                PI.send(1);
            } catch (PendingIntent.CanceledException e) {
                Log.e(TAG, "sendFakeIntent canceled exception");
            }
        }
    }
}

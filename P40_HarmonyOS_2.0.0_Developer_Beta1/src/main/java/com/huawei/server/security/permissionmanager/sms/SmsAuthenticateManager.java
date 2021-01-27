package com.huawei.server.security.permissionmanager.sms;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Telephony;
import android.text.TextUtils;
import com.huawei.android.content.ContextEx;
import com.huawei.android.content.pm.PackageManagerExt;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.util.SlogEx;
import com.huawei.permission.IHoldNotifier;
import com.huawei.server.security.permissionmanager.sms.smsutils.Utils;
import java.util.ArrayList;

public class SmsAuthenticateManager {
    private static final String ACTION_SMS_AUTHENTICATE = "com.huawei.systemmanager.action.RESET_SMS_AUTHENTICATE";
    public static final int AUTHENTICATE_RESULT_ALLOW = 0;
    public static final int AUTHENTICATE_RESULT_ALLOW_FOREVER = 1;
    public static final int AUTHENTICATE_RESULT_DISALLOW = 2;
    public static final int AUTHENTICATE_RESULT_DISALLOW_FOREVER = 3;
    public static final int AUTHENTICATE_RESULT_LOCKED = 5;
    private static final String AUTH_ACTIVITY_CLASS_NAME = "com.huawei.securitycenter.permission.ui.activity.smsauth.RequestAuthActivity";
    private static final String AUTH_RESULT_KEYS = "authenticate_result";
    private static final String ETS_TEST_SMS_APP_VMALL = "com.vmall.client";
    private static final int EVT_ON_AUTH_RESULT = 1001;
    private static final int EVT_REQUEST_AUTH = 1000;
    private static final int FIRST_PKG_IN_LIST = 0;
    private static final String HUAWEI_SMS_APPLICATION = "com.android.mms";
    private static final int INITIAL_CAPACITY = 16;
    private static final int IS_TEST_APP_TRANSACTION = 1004;
    private static final String KEY_AUTHENTICATE_RESULT = "sms_authenticate_result_list";
    private static final String KEY_SMS_ID = "sms_id_list";
    private static final int MAX_APPLICATION_UID = 10000;
    private static final int MIN_APPLICATION_UID = 10000;
    public static final int NEED_AUTHENTICATE = 4;
    private static final String SMS_ID_KEYS = "sms_id";
    private static final Object SMS_LOCK = new Object();
    private static final String SYSTEM_MANAGER_PKGNAME = "com.huawei.systemmanager";
    private static final String TAG = "SmsAuthenticateManager";
    private static SmsAuthenticateManager sInstance;
    private ArrayList<SmsAuthenticateInfo> mAuthRequestSmsInfoList = new ArrayList<>(16);
    private ArrayList<SmsAuthenticateInfo> mAuthResultSmsInfoList = new ArrayList<>(16);
    private final Context mContext;
    private Handler mHandler;
    private SmsSendBlockDataMgr mSmsDataManager;

    private SmsAuthenticateManager(Context context) {
        SlogEx.v(TAG, "CREATE SmsAuthenticateManager");
        this.mContext = context;
        this.mSmsDataManager = SmsSendBlockDataMgr.getInstance(context);
        HandlerThread cmdThread = new HandlerThread("SmsAuthenticateTask", -8);
        cmdThread.start();
        this.mHandler = new MyHandler(cmdThread.getLooper());
    }

    public static SmsAuthenticateManager getInstance(Context context) {
        SmsAuthenticateManager smsAuthenticateManager;
        synchronized (SMS_LOCK) {
            if (sInstance == null) {
                sInstance = new SmsAuthenticateManager(context);
            }
            smsAuthenticateManager = sInstance;
        }
        return smsAuthenticateManager;
    }

    private class MyHandler extends Handler {
        private MyHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == SmsAuthenticateManager.EVT_REQUEST_AUTH) {
                SmsAuthenticateManager.this.startAuthenticate();
            } else if (i == SmsAuthenticateManager.EVT_ON_AUTH_RESULT) {
                SmsAuthenticateManager.this.sendAuthenticateResult();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startAuthenticate() {
        SlogEx.v(TAG, "startAuthenticate request size = " + this.mAuthRequestSmsInfoList.size());
        if (this.mAuthRequestSmsInfoList.isEmpty()) {
            SlogEx.e(TAG, "startAuthenticate the quest list is NULL!");
            return;
        }
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.huawei.systemmanager", AUTH_ACTIVITY_CLASS_NAME));
        intent.setFlags(805306368);
        try {
            ContextEx.startActivityAsUser(this.mContext, intent, (Bundle) null, UserHandleEx.getUserHandle(-2));
        } catch (ActivityNotFoundException e) {
            SlogEx.e(TAG, "startAuthenticate activity not available");
        } catch (Exception e2) {
            SlogEx.e(TAG, "startAuthenticate get exception");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendAuthenticateResult() {
        SmsAuthenticateInfo info;
        Bundle authResult = packAuthResultData();
        synchronized (SMS_LOCK) {
            if (this.mAuthResultSmsInfoList.isEmpty()) {
                SlogEx.e(TAG, "sendAuthenticateResult the result list is empty!");
                return;
            } else {
                info = this.mAuthResultSmsInfoList.remove(0);
                this.mAuthResultSmsInfoList.clear();
            }
        }
        SlogEx.v(TAG, "sendAuthenticateResult info = " + info);
        if (info == null || info.getCallback() == null) {
            SlogEx.e(TAG, "sendAuthenticateResult NO AUTH RESULT data!");
            return;
        }
        try {
            IHoldNotifier callback = IHoldNotifier.Stub.asInterface(info.getCallback());
            if (callback != null) {
                callback.notifyResult((String) null, authResult);
            }
        } catch (Exception e) {
            SlogEx.e(TAG, "sendAuthenticateResult exception");
        }
    }

    private Bundle packAuthResultData() {
        ArrayList<Integer> smsIdList = new ArrayList<>(16);
        ArrayList<Integer> authResultList = new ArrayList<>(16);
        Bundle bundle = new Bundle();
        synchronized (SMS_LOCK) {
            if (this.mAuthResultSmsInfoList.isEmpty()) {
                SlogEx.e(TAG, "packAuthResultData the result list is empty!");
                return bundle;
            }
            int size = this.mAuthResultSmsInfoList.size();
            for (int i = 0; i < size; i++) {
                SmsAuthenticateInfo info = this.mAuthResultSmsInfoList.get(i);
                smsIdList.add(Integer.valueOf(info.getSmsId()));
                authResultList.add(Integer.valueOf(info.getAuthenticateResult()));
            }
            bundle.putIntegerArrayList(SMS_ID_KEYS, smsIdList);
            bundle.putIntegerArrayList(AUTH_RESULT_KEYS, authResultList);
            return bundle;
        }
    }

    private void addAuthResultToList(SmsAuthenticateInfo smsInfo) {
        if (smsInfo != null) {
            synchronized (SMS_LOCK) {
                this.mAuthResultSmsInfoList.add(smsInfo);
            }
        }
    }

    private void addAuthDataToList(SmsAuthenticateInfo smsInfo) {
        if (smsInfo != null) {
            synchronized (SMS_LOCK) {
                this.mAuthRequestSmsInfoList.add(smsInfo);
            }
            SlogEx.v(TAG, "addAuthDataToList size = " + this.mAuthRequestSmsInfoList.size() + " SMS INFO = " + smsInfo);
        }
    }

    public void onAuthenticateResult(int result) {
        SlogEx.v(TAG, "onAuthenticateResult result = " + result);
        processAuthResultSync(result);
        this.mHandler.obtainMessage(EVT_ON_AUTH_RESULT).sendToTarget();
    }

    public static String authCodeToString(int result) {
        if (result == 0) {
            return "allow_this_time";
        }
        if (result == 1) {
            return "allow_forever";
        }
        if (result == 2) {
            return "disallow_this_time";
        }
        if (result == 3) {
            return "disallow_forever";
        }
        if (result != 4) {
            return null;
        }
        return "need_authenticate";
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x00af A[LOOP:1: B:26:0x00ad->B:27:0x00af, LOOP_END] */
    private void processAuthResultSync(int result) {
        int resultSize;
        int j;
        synchronized (SMS_LOCK) {
            if (this.mAuthRequestSmsInfoList.isEmpty()) {
                SlogEx.e(TAG, "processAuthResultSync mAuthenticateSmsInfoList is empty!");
                return;
            }
            this.mAuthResultSmsInfoList.clear();
            int sizeOfAllSmsInfo = this.mAuthRequestSmsInfoList.size();
            SlogEx.v(TAG, "processAuthResultSync size of request list: " + sizeOfAllSmsInfo);
            if (result != 0) {
                if (result != 1) {
                    if (result != 2) {
                        if (result != 3) {
                            if (result == 5) {
                                for (int i = 0; i < sizeOfAllSmsInfo; i++) {
                                    SmsAuthenticateInfo temp = this.mAuthRequestSmsInfoList.get(i);
                                    temp.setAuthenticateResult(2);
                                    this.mAuthResultSmsInfoList.add(temp);
                                }
                            }
                            resultSize = this.mAuthResultSmsInfoList.size();
                            SlogEx.v(TAG, "processAuthResultSync mAuthResultSmsInfoList SIZE = " + resultSize + " result = " + result);
                            for (j = 0; j < resultSize; j++) {
                                this.mAuthRequestSmsInfoList.remove(this.mAuthResultSmsInfoList.get(j));
                            }
                        }
                    }
                }
                SlogEx.v(TAG, "processAuthResultSync, result forever: " + result);
                processAllowDisallowForever(result, sizeOfAllSmsInfo);
                resultSize = this.mAuthResultSmsInfoList.size();
                SlogEx.v(TAG, "processAuthResultSync mAuthResultSmsInfoList SIZE = " + resultSize + " result = " + result);
                while (j < resultSize) {
                }
            }
            SmsAuthenticateInfo completedAuthSms = this.mAuthRequestSmsInfoList.remove(0);
            completedAuthSms.setAuthenticateResult(result);
            this.mAuthResultSmsInfoList.add(completedAuthSms);
            resultSize = this.mAuthResultSmsInfoList.size();
            SlogEx.v(TAG, "processAuthResultSync mAuthResultSmsInfoList SIZE = " + resultSize + " result = " + result);
            while (j < resultSize) {
            }
        }
    }

    private void processAllowDisallowForever(int result, int sizeOfAllSmsInfo) {
        String packageName = this.mAuthRequestSmsInfoList.get(0).getPackageName();
        for (int i = 0; i < sizeOfAllSmsInfo; i++) {
            SmsAuthenticateInfo temp = this.mAuthRequestSmsInfoList.get(i);
            if (temp.isSameApp(packageName)) {
                temp.setAuthenticateResult(result);
                this.mAuthResultSmsInfoList.add(temp);
            }
        }
        this.mSmsDataManager.addAuthResultForApk(packageName, result, false);
    }

    private boolean isDefaultSmsApp(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        if (packageName.equals(HUAWEI_SMS_APPLICATION)) {
            return true;
        }
        String defaultPkgName = Telephony.Sms.getDefaultSmsPackage(this.mContext);
        SlogEx.v(TAG, "isDefaultSmsApp in-app is " + packageName + " default sms app is " + defaultPkgName);
        return packageName.equals(defaultPkgName);
    }

    private static ApplicationInfo getAppInfoByUid(Context context, int inUid) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            SlogEx.e(TAG, "getAppInfoByUid get PMG failed!");
            return null;
        }
        try {
            String[] pkgName = packageManager.getPackagesForUid(inUid);
            if (pkgName != null) {
                if (pkgName.length > 0) {
                    SlogEx.v(TAG, "getAppInfoByUid = " + inUid + " pkgName = " + pkgName[0]);
                    return PackageManagerExt.getApplicationInfoAsUser(packageManager, pkgName[0], 0, UserHandleEx.getUserId(inUid));
                }
            }
            SlogEx.e(TAG, "getAppInfoByUid pkgName = null");
            return null;
        } catch (PackageManager.NameNotFoundException e) {
            SlogEx.e(TAG, "getAppInfoByUid NameNotFoundException");
            return null;
        }
    }

    private int checkDefaultAuthConfig(int uid) {
        ApplicationInfo appInfo = getAppInfoByUid(this.mContext, uid);
        if (appInfo == null) {
            SlogEx.e(TAG, "checkDefaultAuthConfig appInfo is null");
            return 2;
        }
        if ((isAbroad() || isDefaultSmsApp(appInfo.packageName)) || isSystemAppInternal(this.mContext, uid, false)) {
            return 1;
        }
        return SmsSendBlockDataMgr.getInstance(this.mContext).getDefaultConfigForApk(appInfo.packageName, UserHandleEx.getUserId(uid));
    }

    private boolean isAbroad() {
        return !SystemPropertiesEx.get("ro.config.hw_optb", "0").equals("156");
    }

    private static boolean isCallerInvalid(int passedUid) {
        int callerUid = Binder.getCallingUid();
        return UserHandleEx.getAppId(UserHandleEx.getUserHandle(-2), callerUid) >= 10000 && passedUid != callerUid;
    }

    private static boolean isSystemAppInternal(Context context, int callUid, boolean notUsed) {
        PackageManager packageManager;
        if (isCallerInvalid(callUid) || UserHandleEx.getAppId(UserHandleEx.getUserHandle(-2), callUid) < 10000 || context == null || (packageManager = context.getPackageManager()) == null) {
            return true;
        }
        try {
            String[] pkgName = packageManager.getPackagesForUid(callUid);
            if (pkgName != null) {
                if (pkgName.length > 0) {
                    ApplicationInfo appInfo = PackageManagerExt.getApplicationInfoAsUser(packageManager, pkgName[0], 0, UserHandleEx.getUserId(callUid));
                    if (appInfo == null) {
                        SlogEx.e(TAG, "appInfo = null");
                        return true;
                    }
                    String packageName = appInfo.packageName;
                    long identity = Binder.clearCallingIdentity();
                    try {
                        if (!Utils.isPackageShouldMonitor(context, packageName, appInfo.uid)) {
                            return true;
                        }
                        Binder.restoreCallingIdentity(identity);
                        return false;
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            }
            SlogEx.e(TAG, "pkgName = null");
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            SlogEx.e(TAG, "isSystemAppInternal get Exception!");
            return true;
        }
    }

    public void requestAuthenticate(IBinder callback, int uidOf3rdApk, int smsId, String smsBody, String smsAddress) {
        SmsAuthenticateInfo smsInfo;
        if (smsBody.equals("ets test sms block")) {
            if (smsAddress.equals("ets test sms address")) {
                if (callback != null) {
                    SlogEx.e(TAG, "requestAuthenticate not ets test");
                    return;
                }
                this.mAuthRequestSmsInfoList.add(new SmsAuthenticateInfo(callback, smsId, smsBody, ETS_TEST_SMS_APP_VMALL, null));
                return;
            }
        }
        int smsPermission = checkDefaultAuthConfig(uidOf3rdApk);
        SlogEx.v(TAG, "requestAuthenticate uid = " + uidOf3rdApk + " permission = " + authCodeToString(smsPermission));
        ApplicationInfo appInfo = getAppInfoByUid(this.mContext, uidOf3rdApk);
        if (appInfo == null) {
            SlogEx.e(TAG, "requestAuthenticate APP INFO IS NULL!");
            smsInfo = new SmsAuthenticateInfo(callback, smsId, smsBody, null, null);
        } else {
            smsInfo = new SmsAuthenticateInfo(callback, smsId, smsBody, appInfo.packageName, (String) appInfo.loadLabel(this.mContext.getPackageManager()));
        }
        if (smsPermission == 4) {
            addAuthDataToList(smsInfo);
            this.mHandler.obtainMessage(EVT_REQUEST_AUTH).sendToTarget();
            return;
        }
        long identify = Binder.clearCallingIdentity();
        try {
            smsInfo.setAuthenticateResult(smsPermission);
            addAuthResultToList(smsInfo);
            sendAuthenticateResult();
        } finally {
            Binder.restoreCallingIdentity(identify);
        }
    }

    public SmsAuthenticateInfo getSmsAuthInfo() {
        SlogEx.v(TAG, "getSmsAuthInfo SIZE = " + this.mAuthRequestSmsInfoList.size());
        synchronized (SMS_LOCK) {
            if (this.mAuthRequestSmsInfoList.size() <= 0) {
                return null;
            }
            return this.mAuthRequestSmsInfoList.get(0);
        }
    }

    public void onServiceProcessStop() {
        SmsSendBlockDataMgr.getInstance(this.mContext).unregisterAllListener();
    }
}

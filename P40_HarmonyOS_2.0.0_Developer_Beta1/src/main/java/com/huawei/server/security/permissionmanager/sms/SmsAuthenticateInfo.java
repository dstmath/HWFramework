package com.huawei.server.security.permissionmanager.sms;

import android.os.IBinder;

public class SmsAuthenticateInfo {
    private String mAppName;
    private int mAuthenticateResult = -1;
    private IBinder mCallback;
    private String mPackageName;
    private String mSmsBody;
    private int mSmsId;

    public SmsAuthenticateInfo(IBinder callback, int smsId, String smsBody, String packageName, String appName) {
        this.mCallback = callback;
        this.mSmsId = smsId;
        this.mSmsBody = smsBody;
        this.mPackageName = packageName;
        this.mAppName = appName;
    }

    public IBinder getCallback() {
        return this.mCallback;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public boolean isSameApp(String packageName) {
        String str = this.mPackageName;
        return str != null && str.equals(packageName);
    }

    public void setAuthenticateResult(int authenticateResult) {
        this.mAuthenticateResult = authenticateResult;
    }

    public int getAuthenticateResult() {
        return this.mAuthenticateResult;
    }

    public int getSmsId() {
        return this.mSmsId;
    }

    public String getAppName() {
        return this.mAppName;
    }

    public String getSmsBody() {
        return this.mSmsBody;
    }

    public String toString() {
        return "SmsAuthenticateInfo appName : " + this.mAppName + " packageName : " + this.mPackageName + " sms id : " + this.mSmsId;
    }
}

package com.huawei.android.app.admin;

import android.text.TextUtils;

public class ExchangeAccount {
    private static final int CALENDAR_1_MONTH = 2;
    private static final int CALENDAR_2_WEEKS = 1;
    private static final int CALENDAR_3_MONTH = 3;
    private static final int CALENDAR_6_MONTH = 4;
    private static final int CALENDAR_ALL = 5;
    private static final int FREQUENCY_15_MIN = 15;
    private static final int FREQUENCY_1_HOUR = 60;
    private static final int FREQUENCY_30_MIN = 30;
    private static final int FREQUENCY_NEVER = -1;
    private static final int FREQUENCY_PUSH = -2;
    private static final int LOOKBACK_1_DAY = 1;
    private static final int LOOKBACK_1_MONTH = 5;
    private static final int LOOKBACK_1_WEEKS = 3;
    private static final int LOOKBACK_2_WEEKS = 4;
    private static final int LOOKBACK_3_DAYS = 2;
    private static final int LOOKBACK_ALL = 6;
    private static final int[] sCalendarEntries = new int[]{1, 2, 3, 4, 5};
    private static final int[] sIntervalEntries = new int[]{-2, -1, 15, 30, FREQUENCY_1_HOUR};
    private static final int[] sLookbackEntries = new int[]{1, 2, 3, 4, 5, 6};
    public boolean mAcceptAllCertificates;
    public int mCalendarPeriod = 1;
    public boolean mDisplayHtmlDisabled;
    public String mDisplayName;
    public String mEasDomain;
    public String mEasUser;
    public String mEmailAddress;
    public boolean mForwardEmailDisabled;
    public boolean mIsDefault;
    public String mProtocolVersion;
    public boolean mScreenShotDisabled;
    public String mSenderName;
    public String mServerAddress;
    public String mServerPassword;
    public String mServerPathPrefix;
    public String mSignature;
    public int mSyncInterval = -2;
    public int mSyncLookback = 3;
    public boolean mUseSSL = true;
    public boolean mUseTLS;

    public ExchangeAccount(String emailAddress, String easUser, String easDomain, String serverAddress, String serverPassword) {
        if (!isAddressValid(emailAddress) || TextUtils.isEmpty(serverAddress)) {
            throw new IllegalArgumentException("Invalid Parameters!");
        }
        this.mEmailAddress = emailAddress;
        this.mEasUser = easUser;
        this.mEasDomain = easDomain;
        this.mServerAddress = serverAddress;
        this.mServerPassword = serverPassword;
    }

    public ExchangeAccount(String displayName, String emailAddress, String easUser, String easDomain, int syncLookback, int syncInterval, boolean isDefault, String senderName, String protocolVersion, String signature, String serverAddress, boolean useSSL, boolean useTLS, boolean acceptAllCertificates, String serverPassword, String serverPathPrefix, int calendarPeriod, boolean displayHtmlDisabled, boolean forwardEmailDisabled, boolean screenShotDisabled) {
        if (isAddressValid(emailAddress) && !TextUtils.isEmpty(serverAddress) && (isSyncIntervalValid(syncInterval) ^ 1) == 0 && (isSyncLookbackValid(syncLookback) ^ 1) == 0 && (isPeriodCalendarValid(calendarPeriod) ^ 1) == 0) {
            this.mDisplayName = displayName;
            this.mEmailAddress = emailAddress;
            this.mEasUser = easUser;
            this.mEasDomain = easDomain;
            this.mSyncLookback = syncLookback;
            this.mSyncInterval = syncInterval;
            this.mIsDefault = isDefault;
            this.mSenderName = senderName;
            this.mProtocolVersion = protocolVersion;
            this.mSignature = signature;
            this.mServerAddress = serverAddress;
            this.mUseSSL = useSSL;
            this.mUseTLS = useTLS;
            this.mAcceptAllCertificates = acceptAllCertificates;
            this.mServerPassword = serverPassword;
            this.mServerPathPrefix = serverPathPrefix;
            this.mCalendarPeriod = calendarPeriod;
            this.mDisplayHtmlDisabled = displayHtmlDisabled;
            this.mForwardEmailDisabled = forwardEmailDisabled;
            this.mScreenShotDisabled = screenShotDisabled;
            return;
        }
        throw new IllegalArgumentException("Invalid Parameters!");
    }

    private boolean isAddressValid(String emailAddress) {
        if (emailAddress == null || emailAddress.indexOf("@") == -1) {
            return false;
        }
        return true;
    }

    private boolean isSyncIntervalValid(int syncInterval) {
        for (int i : sIntervalEntries) {
            if (syncInterval == i) {
                return true;
            }
        }
        return false;
    }

    private boolean isSyncLookbackValid(int syncLookback) {
        for (int i : sLookbackEntries) {
            if (syncLookback == i) {
                return true;
            }
        }
        return false;
    }

    private boolean isPeriodCalendarValid(int calendarPeriod) {
        for (int i : sCalendarEntries) {
            if (calendarPeriod == i) {
                return true;
            }
        }
        return false;
    }
}

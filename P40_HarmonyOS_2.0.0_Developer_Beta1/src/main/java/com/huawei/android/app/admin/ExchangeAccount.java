package com.huawei.android.app.admin;

import android.text.TextUtils;

public class ExchangeAccount {
    private static final int CALENDAR_1_MONTH = 2;
    private static final int CALENDAR_2_WEEKS = 1;
    private static final int CALENDAR_3_MONTH = 3;
    private static final int CALENDAR_6_MONTH = 4;
    private static final int CALENDAR_ALL = 5;
    private static final int DISABLE_SYNC = 0;
    private static final int ENABLE_SYNC = 1;
    private static final int FORCE_SYNC = 2;
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
    public boolean mAcceptAllCertificates;
    public int mCalendarPeriod;
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
    public int mSyncCalendar;
    public int mSyncContacts;
    public int mSyncInterval;
    public int mSyncLookback;
    public boolean mUseSSL;
    public boolean mUseTLS;

    public ExchangeAccount() {
        this.mCalendarPeriod = 1;
        this.mSyncInterval = FREQUENCY_PUSH;
        this.mSyncLookback = 3;
        this.mUseSSL = true;
        this.mSyncCalendar = 1;
        this.mSyncContacts = 1;
    }

    public ExchangeAccount(String emailAddress, String easUser, String easDomain, String serverAddress, String serverPassword) {
        this(null, emailAddress, easUser, easDomain, 3, FREQUENCY_PUSH, false, null, null, null, serverAddress, true, false, false, serverPassword, null, 1, false, false, false, 1, 1);
    }

    public ExchangeAccount(String displayName, String emailAddress, String easUser, String easDomain, int syncLookback, int syncInterval, boolean isDefault, String senderName, String protocolVersion, String signature, String serverAddress, boolean isUseSsl, boolean isUseTls, boolean isAcceptAllCertificates, String serverPassword, String serverPathPrefix, int calendarPeriod, boolean isDisplayHtmlDisabled, boolean isForwardEmailDisabled, boolean isScreenShotDisabled) {
        this(displayName, emailAddress, easUser, easDomain, syncLookback, syncInterval, isDefault, senderName, protocolVersion, signature, serverAddress, isUseSsl, isUseTls, isAcceptAllCertificates, serverPassword, serverPathPrefix, calendarPeriod, isDisplayHtmlDisabled, isForwardEmailDisabled, isScreenShotDisabled, 1, 1);
    }

    public ExchangeAccount(String displayName, String emailAddress, String easUser, String easDomain, int syncLookback, int syncInterval, boolean isDefault, String senderName, String protocolVersion, String signature, String serverAddress, boolean isUseSsl, boolean isUseTls, boolean isAcceptAllCertificates, String serverPassword, String serverPathPrefix, int calendarPeriod, boolean isDisplayHtmlDisabled, boolean isForwardEmailDisabled, boolean isScreenShotDisabled, int syncCalendar, int syncContacts) {
        this.mCalendarPeriod = 1;
        this.mSyncInterval = FREQUENCY_PUSH;
        this.mSyncLookback = 3;
        this.mUseSSL = true;
        this.mSyncCalendar = 1;
        this.mSyncContacts = 1;
        if (isAddressValid(emailAddress) && !TextUtils.isEmpty(serverAddress)) {
            if (isSyncValueValid(syncCalendar) && isSyncValueValid(syncContacts)) {
                if (isSyncIntervalValid(syncInterval) && isSyncLookbackValid(syncLookback)) {
                    if (isPeriodCalendarValid(calendarPeriod)) {
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
                        this.mUseSSL = isUseSsl;
                        this.mUseTLS = isUseTls;
                        this.mAcceptAllCertificates = isAcceptAllCertificates;
                        this.mServerPassword = serverPassword;
                        this.mServerPathPrefix = serverPathPrefix;
                        this.mCalendarPeriod = calendarPeriod;
                        this.mDisplayHtmlDisabled = isDisplayHtmlDisabled;
                        this.mForwardEmailDisabled = isForwardEmailDisabled;
                        this.mScreenShotDisabled = isScreenShotDisabled;
                        this.mSyncCalendar = syncCalendar;
                        this.mSyncContacts = syncContacts;
                        return;
                    }
                }
                throw new IllegalArgumentException("Invalid sync Parameters!");
            }
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
        for (int item : new int[]{FREQUENCY_PUSH, -1, FREQUENCY_15_MIN, FREQUENCY_30_MIN, FREQUENCY_1_HOUR}) {
            if (syncInterval == item) {
                return true;
            }
        }
        return false;
    }

    private boolean isSyncLookbackValid(int syncLookback) {
        for (int item : new int[]{1, 2, 3, 4, 5, 6}) {
            if (syncLookback == item) {
                return true;
            }
        }
        return false;
    }

    private boolean isPeriodCalendarValid(int calendarPeriod) {
        for (int item : new int[]{1, 2, 3, 4, 5}) {
            if (calendarPeriod == item) {
                return true;
            }
        }
        return false;
    }

    private boolean isSyncValueValid(int syncValue) {
        for (int item : new int[]{0, 1, 2}) {
            if (syncValue == item) {
                return true;
            }
        }
        return false;
    }
}

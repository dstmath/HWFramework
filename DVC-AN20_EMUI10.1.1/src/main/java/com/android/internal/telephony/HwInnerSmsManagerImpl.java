package com.android.internal.telephony;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.cdma.CdmaSMSDispatcher;
import com.android.internal.telephony.cdma.HwCdmaSMSDispatcher;
import com.android.internal.telephony.cdma.HwCustPlusAndIddNddConvertUtils;
import com.android.internal.telephony.cdma.HwSmsMessage;
import com.android.internal.telephony.cdma.sms.CdmaSmsAddress;
import com.android.internal.telephony.gsm.GsmInboundSmsHandler;
import com.android.internal.telephony.gsm.GsmSMSDispatcher;
import com.android.internal.telephony.gsm.HwGsmSMSDispatcher;
import com.google.android.mms.pdu.EncodedStringValue;
import com.huawei.internal.telephony.SmsHeaderEx;
import com.huawei.internal.telephony.SmsMessageBaseEx;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import huawei.android.telephony.CallerInfoHW;
import huawei.cust.HwCfgFilePolicy;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.PatternSyntaxException;

public class HwInnerSmsManagerImpl extends DefaultHwInnerSmsManager {
    private static final String BLOCK_TYPE = "BLOCKED_TYPE";
    private static final String BLUETOOTH_PACKAGE_NAME = "com.android.bluetooth";
    private static final String COLUMN_IS_PRIVATE = "is_private";
    private static final String CONTACTS_PACKAGE_NAME = "com.android.contacts";
    private static final String DAY_MODE = "day_mode";
    private static final String DAY_MODE_TIME = "day_mode_time";
    private static final int DECEMBER = 11;
    private static final String DEFAULT_SMS_APPLICATION = "com.android.mms";
    private static final String DELIVER_CHANNEL_INFO = SystemProperties.get("ro.config.hw_channel_info", "0,0,460,1,0");
    public static final int ERROR_BASE_MMS = 1300;
    public static final int ERR_SMS_RECEIVE = 1312;
    public static final int ERR_SMS_SEND = 1311;
    private static final int EVENT_SEND_SMS_OVERLOAD = 20;
    private static final String HANDLE_KEY_SMSINTENT = "HANDLE_SMS_INTENT";
    private static final String HANDLE_KEY_WAP_PUSH_INTENT = "HANDLE_WAP_PUSH_INTENT";
    private static final String INCOMING_DAY_LIMIT = "incoming_day_limit";
    private static final String INCOMING_MONTH_LIMIT = "incoming_month_limit";
    private static final String INCOMING_SMS_LIMIT = "incoming_limit";
    private static final String INCOMING_WEEK_LIMIT = "incoming_week_limit";
    private static final String INIT_VALUE = "0";
    private static final int IOT_VERSION_INDEX = 4;
    private static final boolean IS_CHINATELECOM;
    private static final int LIMIT_INIT_NUM = 0;
    private static final String LIMIT_OF_DAY = "limit_number_day";
    private static final String LIMIT_OF_MONTH = "limit_number_month";
    private static final String LIMIT_OF_WEEK = "limit_number_week";
    public static final String LOCAL_NUMBER_FROM_DB = "localNumberFromDb";
    private static final String LOG_TAG = "HwInnerSmsManagerImpl";
    private static final int MAX_SMS_LIST_DEFAULT = 30;
    private static final String MESSAGE_STRING_NAME = "app_label";
    private static final String MMS_ACTIVITY_NAME = "com.android.mms.ui.ComposeMessageActivity";
    private static final String MMS_PACKAGE_NAME = "com.android.mms";
    private static final String MODE_INIT_VALUE = "false";
    private static final String MONTH_MODE = "month_mode";
    private static final String MONTH_MODE_TIME = "month_mode_time";
    static int Message_Reference_Num = 0;
    private static final String NEW_CONTACTS_PACKAGE_NAME = "com.huawei.contacts";
    private static final String OUTGOING_DAY_LIMIT = "outgoing_day_limit";
    private static final String OUTGOING_MONTH_LIMIT = "outgoing_month_limit";
    private static final String OUTGOING_SMS_LIMIT = "outgoing_limit";
    private static final String OUTGOING_WEEK_LIMIT = "outgoing_week_limit";
    private static final long PARTIAL_SEGMENT_EXPIRE_TIME = (((long) SystemProperties.getInt("ro.config.hw_drop_oldsms_day", 3)) * 86400000);
    private static boolean PLUS_TRANFER_IN_AP = (!HwModemCapability.isCapabilitySupport(2));
    private static final String POLICY_KEY = "value";
    private static final Uri RAW_URI = Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, "raw");
    private static final String RESOURCE_TYPE_STRING = "string";
    private static final String SELECT_BY_REFERENCE = "address = ? AND reference_number = ? AND count = ? AND deleted = 0";
    private static final int SEND_SMS_OVERlOAD_COUNT = 50;
    private static final int SEND_SMS_OVERlOAD_DURATION_TIMEOUT = 3600000;
    private static final int SMS_BROADCAST_DURATION_TIMEOUT = 180000;
    private static final int SMS_INSERTDB_DURATION_TIMEOUT = 60000;
    private static int SUBSCRIPTION_FOR_NONE_VARIABLE_METHODS = -1;
    private static final String TELECOM_PACKAGE_NAME = "com.android.server.telecom";
    private static final boolean TIP_PREMIUM_SHORT_CODE = SystemProperties.getBoolean("ro.huawei.flag.tip_premium", true);
    private static final Uri URI_PRIVATE_NUMBER = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "private_contacts");
    private static final String USED_OF_DAY = "used_number_day";
    private static final String USED_OF_MONTH = "used_number_month";
    private static final String USED_OF_WEEK = "used_number_week";
    private static final String WEEK_MODE = "week_mode";
    private static final String WEEK_MODE_TIME = "week_mode_time";
    private static HwInnerSmsManager mInstance;
    private AtomicInteger mAlreadyReceivedSms = new AtomicInteger(0);
    private AtomicInteger mAlreadySentSms = new AtomicInteger(0);
    private HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();
    private Handler mHwHandler = new Handler() {
        /* class com.android.internal.telephony.HwInnerSmsManagerImpl.AnonymousClass3 */

        public void handleMessage(Message msg) {
            if (msg.what == HwInnerSmsManagerImpl.EVENT_SEND_SMS_OVERLOAD) {
                if ((msg.obj != null) && (msg.obj instanceof SMSDispatcher)) {
                    HwInnerSmsManagerImpl.this.handleSendSmsOverLoad((SMSDispatcher) msg.obj);
                }
            }
        }
    };
    private ContentObserver mInsertObserver = new ContentObserver(new Handler()) {
        /* class com.android.internal.telephony.HwInnerSmsManagerImpl.AnonymousClass2 */

        public void onChange(boolean selfUpdate) {
            HwInnerSmsManagerImpl.this.mAlreadyReceivedSms.getAndDecrement();
            if (HwInnerSmsManagerImpl.this.mAlreadyReceivedSms.get() < 0) {
                HwInnerSmsManagerImpl.this.mAlreadyReceivedSms.set(0);
            }
        }
    };
    private boolean mIsAlreadyDurationTimeout = false;
    private KeyguardManager mKeyguardManager;
    private SmsInterceptionService mSmsInterceptionService = null;
    private List<byte[]> mSmsList = new ArrayList();
    private int mSubIdForReceivedSms = -1;
    private Runnable mUpdateCountRunner = new Runnable() {
        /* class com.android.internal.telephony.HwInnerSmsManagerImpl.AnonymousClass1 */

        public void run() {
            Context context;
            if (HwInnerSmsManagerImpl.this.mAlreadyReceivedSms.get() > 0) {
                String msg = "sms receive fail:" + HwInnerSmsManagerImpl.this.getDefaultSmsApplicationName();
                if (PhoneFactory.getDefaultPhone() != null) {
                    context = PhoneFactory.getDefaultPhone().getContext();
                } else {
                    context = null;
                }
                HwRadarUtils.report(context, 1312, msg, HwInnerSmsManagerImpl.this.mSubIdForReceivedSms);
            }
            HwInnerSmsManagerImpl.this.mAlreadyReceivedSms.set(0);
        }
    };

    static {
        boolean z = false;
        if (SystemProperties.get("ro.config.hw_opta", INIT_VALUE).equals("92") && SystemProperties.get("ro.config.hw_optb", INIT_VALUE).equals("156")) {
            z = true;
        }
        IS_CHINATELECOM = z;
    }

    public static HwInnerSmsManager getDefault() {
        if (mInstance == null) {
            mInstance = new HwInnerSmsManagerImpl();
        }
        return mInstance;
    }

    private static void setMessageReferenceNum(int value) {
        Message_Reference_Num = value;
    }

    public int getMessageRefrenceNumber() {
        int i = Message_Reference_Num;
        if (255 <= i) {
            setMessageReferenceNum(0);
        } else {
            setMessageReferenceNum(i + 1);
        }
        return Message_Reference_Num;
    }

    public byte[] getUserDataHeaderForGsm(int seqNum, int maxNum, int MessageReferenceNum) {
        byte[] udh = new byte[5];
        if (seqNum > maxNum) {
            return null;
        }
        udh[0] = 0;
        udh[1] = 3;
        udh[2] = (byte) MessageReferenceNum;
        udh[3] = (byte) maxNum;
        udh[4] = (byte) seqNum;
        Rlog.i("android/SmsMessage", "maxNum:" + maxNum + ";seqNum:" + seqNum + ";MR:" + ((int) udh[2]));
        return udh;
    }

    public SmsMessage.SubmitPdu getSubmitPdu(String scAddress, String timeStamps, String destinationAddress, String message, byte[] udh) {
        return getSubmitPdu(scAddress, timeStamps, destinationAddress, message, udh, SUBSCRIPTION_FOR_NONE_VARIABLE_METHODS);
    }

    public SmsMessage.SubmitPdu getSubmitPdu(String scAddress, String timeStamps, String destinationAddress, String message, byte[] udh, int subscription) {
        SmsMessageBase.SubmitPduBase spb;
        if ((TelephonyManager.getDefault().isMultiSimEnabled() ? TelephonyManager.getDefault().getCurrentPhoneTypeForSlot(subscription) : TelephonyManager.getDefault().getPhoneType()) == 2) {
            spb = HwSmsMessage.getSubmitDeliverPdu(true, (String) null, destinationAddress, message, SmsHeaderEx.fromByteArray(udh)).getSubmitPduBase();
        } else {
            spb = com.android.internal.telephony.gsm.SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, false, udh);
        }
        if (spb == null) {
            return null;
        }
        return new SmsMessage.SubmitPdu(spb);
    }

    public SmsMessage.SubmitPdu getDeliverPdu(String scAddress, String scTimeStamp, String origAddress, String message, byte[] udh) {
        return getDeliverPdu(scAddress, scTimeStamp, origAddress, message, udh, SUBSCRIPTION_FOR_NONE_VARIABLE_METHODS);
    }

    public SmsMessage.SubmitPdu getDeliverPdu(String scAddress, String scTimeStamp, String origAddress, String message, byte[] udh, int subscription) {
        SmsMessageBaseEx.SubmitPduBaseEx spb;
        if ((TelephonyManager.getDefault().isMultiSimEnabled() ? TelephonyManager.getDefault().getCurrentPhoneTypeForSlot(subscription) : TelephonyManager.getDefault().getPhoneType()) == 2) {
            spb = HwSmsMessage.getSubmitDeliverPdu(false, scTimeStamp, origAddress, message, SmsHeaderEx.fromByteArray(udh));
        } else {
            spb = com.android.internal.telephony.gsm.HwSmsMessage.getDeliverPdu(scAddress, scTimeStamp, origAddress, message, udh);
        }
        if (spb == null) {
            return null;
        }
        return new SmsMessage.SubmitPdu(spb.getSubmitPduBase());
    }

    public SmsMessage createFromEfRecord(int index, byte[] data, int subscription) {
        SmsMessageBase wrappedMessage;
        if (TelephonyManager.getDefault().getCurrentPhoneTypeForSlot(subscription) == 2) {
            wrappedMessage = com.android.internal.telephony.cdma.SmsMessage.createFromEfRecord(index, data);
        } else {
            wrappedMessage = com.android.internal.telephony.gsm.SmsMessage.createFromEfRecord(index, data);
        }
        Rlog.e(LOG_TAG, "createFromEfRecord(): wrappedMessage=" + wrappedMessage);
        if (wrappedMessage != null) {
            return new SmsMessage(wrappedMessage);
        }
        return null;
    }

    public boolean handleWapPushExtraMimeType(String mimeType) {
        return HwWspTypeDecoderEx.CONTENT_TYPE_B_CONNECT_WBXML.endsWith(mimeType);
    }

    public GsmSMSDispatcher createHwGsmSMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController, GsmInboundSmsHandler gsmInboundSmsHandler) {
        return new HwGsmSMSDispatcher(phone, smsDispatchersController, gsmInboundSmsHandler);
    }

    public CdmaSMSDispatcher createHwCdmaSMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController) {
        return new HwCdmaSMSDispatcher(phone, smsDispatchersController);
    }

    public boolean shouldSendReceivedActionInPrivacyMode(InboundSmsHandler handler, Context context, Intent intent, String deleteWhere, String[] deleteWhereArgs) {
        KeyguardManager keyguardManager;
        this.mKeyguardManager = (KeyguardManager) context.getSystemService("keyguard");
        String action = intent.getAction();
        String number = null;
        if (!(SystemProperties.getBoolean("ro.config.hw_privacymode", false) && 1 == Settings.Secure.getInt(context.getContentResolver(), "privacy_mode_on", 0) && (Settings.Secure.getInt(context.getContentResolver(), "privacy_mode_state", 0) == 1 || ((keyguardManager = this.mKeyguardManager) != null && keyguardManager.isKeyguardLocked())))) {
            return true;
        }
        if ("android.provider.Telephony.SMS_DELIVER".equals(action)) {
            SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            if (msgs != null && msgs.length > 0) {
                number = msgs[0].getOriginatingAddress();
            }
        } else if (handler.isWapPushDeliverActionAndMmsMessage(intent)) {
            Rlog.i(LOG_TAG, "isWapPushDeliverActionAndMmsMessage is true");
            number = handler.getNumberIfWapPushDeliverActionAndMmsMessage(intent);
        } else {
            Rlog.d(LOG_TAG, "ignore action, do nothing");
            return true;
        }
        if (number == null || !isPrivacyNumber(context, number)) {
            return true;
        }
        Rlog.i(LOG_TAG, "sms originating address: xxxxxx is private, do not send received action.");
        deleteFromRawTable(context, deleteWhere, deleteWhereArgs);
        handler.sendMessage(InboundSmsHandler.getBroadcastCompleteEventHw());
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0037, code lost:
        if (r8 != null) goto L_0x0039;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0050, code lost:
        if (0 == 0) goto L_0x0053;
     */
    private boolean isPrivacyNumber(Context context, String number) {
        Cursor c = null;
        boolean isPrivate = false;
        try {
            c = context.getContentResolver().query(URI_PRIVATE_NUMBER.buildUpon().appendEncodedPath(number).build(), new String[]{COLUMN_IS_PRIVATE}, null, null, null);
            if (c != null && c.moveToFirst()) {
                boolean z = false;
                if (c.getInt(0) == 1) {
                    z = true;
                }
                isPrivate = z;
            }
        } catch (SQLException e) {
            Rlog.e(LOG_TAG, "isPrivacyNumber cause SQLException!");
        } catch (Exception e2) {
            Rlog.e(LOG_TAG, "isPrivacyNumber cause exception!");
            if (0 != 0) {
                c.close();
            }
            StringBuilder sb = new StringBuilder();
            sb.append("The number is private or not: ");
            sb.append(isPrivate ? "Yes" : "No");
            Rlog.i(LOG_TAG, sb.toString());
            return isPrivate;
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    private void deleteFromRawTable(Context context, String deleteWhere, String[] deleteWhereArgs) {
        int rows = context.getContentResolver().delete(RAW_URI, deleteWhere, deleteWhereArgs);
        if (rows == 0) {
            Rlog.e(LOG_TAG, "No rows were deleted from raw table!");
            return;
        }
        Rlog.d(LOG_TAG, "Deleted " + rows + " rows from raw table.");
    }

    public void createSmsInterceptionService(Context context) {
        Rlog.d(LOG_TAG, "createSmsInterceptionService ...");
        this.mSmsInterceptionService = SmsInterceptionService.getDefault(context);
    }

    public boolean newSmsShouldBeIntercepted(Context context, Intent intent, InboundSmsHandler handler, String deleteWhere, String[] deleteWhereArgs, boolean isWapPush) {
        try {
            Bundle smsInfo = new Bundle();
            if (isWapPush) {
                smsInfo.putParcelable(HANDLE_KEY_WAP_PUSH_INTENT, intent);
            } else {
                smsInfo.putParcelable(HANDLE_KEY_SMSINTENT, intent);
            }
            if (!this.mSmsInterceptionService.dispatchNewSmsToInterceptionProcess(smsInfo, isWapPush)) {
                return false;
            }
            Rlog.i(LOG_TAG, "sms is intercepted ...");
            deleteFromRawTable(context, deleteWhere, deleteWhereArgs);
            handler.sendMessage(InboundSmsHandler.getBroadcastCompleteEventHw());
            return true;
        } catch (ConcurrentModificationException e) {
            Rlog.e(LOG_TAG, "Get ConcurrentModificationException newSmsShouldBeIntercepted");
            return false;
        } catch (Exception e2) {
            Rlog.e(LOG_TAG, "Get exception while communicate with sms interception service in newSmsShouldBeIntercepted");
            return false;
        }
    }

    public boolean isMatchSMSPattern(String address, String policyTag, int phoneId) {
        if (address == null || TextUtils.isEmpty(policyTag)) {
            Rlog.e(LOG_TAG, "address or policyTag is null!");
            return false;
        }
        Bundle bundle = this.mDpm.getPolicy((ComponentName) null, policyTag);
        if (bundle == null) {
            Rlog.e(LOG_TAG, "bundle is null!");
            return false;
        }
        String pattern = bundle.getString(POLICY_KEY);
        if (TextUtils.isEmpty(pattern)) {
            Rlog.e(LOG_TAG, "pattern is null!");
            return false;
        }
        boolean isMatchPattern = false;
        String countryIso = null;
        String formatNum = null;
        if (TelephonyManager.getDefault() != null) {
            countryIso = TelephonyManager.getDefault().getNetworkCountryIsoForPhone(phoneId);
        }
        if (countryIso != null) {
            Rlog.i(LOG_TAG, "countryIso is not null.");
            String formatNum2 = PhoneNumberUtils.formatNumberToE164(address, countryIso.toUpperCase(Locale.getDefault()));
            int len = CallerInfoHW.getInstance().getIntlPrefixAndCCLen(formatNum2);
            if (len <= 0 || formatNum2 == null) {
                Rlog.i(LOG_TAG, "can not format address");
                formatNum = address;
            } else {
                formatNum = formatNum2.substring(len);
            }
        }
        if (!TextUtils.isEmpty(formatNum)) {
            try {
                isMatchPattern = formatNum.matches(pattern);
            } catch (PatternSyntaxException e) {
                Rlog.e(LOG_TAG, "pattern has PatternSyntaxException.");
                return false;
            }
        }
        if (!isMatchPattern) {
            try {
                isMatchPattern = address.matches(pattern);
            } catch (PatternSyntaxException e2) {
                Rlog.e(LOG_TAG, "pattern has exception match address.");
                return false;
            }
        }
        Rlog.i(LOG_TAG, "isMatchPattern : " + isMatchPattern);
        return isMatchPattern;
    }

    public void sendGoogleSmsBlockedRecord(Intent intent) {
        try {
            Bundle smsInfo = new Bundle();
            if (intent.getBooleanExtra("isWapPush", false)) {
                smsInfo.putParcelable(HANDLE_KEY_WAP_PUSH_INTENT, intent);
                smsInfo.putInt(BLOCK_TYPE, 3);
            } else {
                smsInfo.putParcelable(HANDLE_KEY_SMSINTENT, intent);
                smsInfo.putInt(BLOCK_TYPE, 2);
            }
            if (this.mSmsInterceptionService != null) {
                this.mSmsInterceptionService.sendNumberBlockedRecord(smsInfo);
            }
        } catch (ConcurrentModificationException e) {
            Rlog.e(LOG_TAG, "Get ConcurrentModificationException");
        } catch (Exception e2) {
            Rlog.e(LOG_TAG, "Get exception while communicate with sms interception service in sendGoogleSmsBlockedRecord");
        }
    }

    public String getAppNameByPid(int pid, Context context) {
        if (context == null) {
            Rlog.i(LOG_TAG, "context is null");
            return "";
        }
        List runningApps = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
        if (runningApps == null || runningApps.size() <= 0) {
            return "";
        }
        for (ActivityManager.RunningAppProcessInfo appInfo : runningApps) {
            if (pid == appInfo.pid) {
                Rlog.i(LOG_TAG, "pid: " + appInfo.pid + " processName: " + appInfo.processName);
                return appInfo.processName;
            }
        }
        return "";
    }

    public PackageInfo getPackageInfoByPid(PackageInfo appInfo, PackageManager pm, String[] packageNames, Context context) {
        if (packageNames == null || packageNames.length <= 1) {
            return appInfo;
        }
        try {
            String packageNameByPid = getAppNameByPid(Binder.getCallingPid(), context);
            if (!"".equals(packageNameByPid)) {
                return pm.getPackageInfo(packageNameByPid, 64);
            }
            return appInfo;
        } catch (PackageManager.NameNotFoundException e) {
            Rlog.e(LOG_TAG, "Exception when got package info");
            return appInfo;
        }
    }

    public boolean checkShouldWriteSmsPackage(String packageName, Context context) {
        if (packageName == null || "".equals(packageName)) {
            Rlog.e(LOG_TAG, "checkShouldWriteSmsPackage packageName not exist");
            return false;
        }
        String defaultSmsPackage = null;
        ComponentName component = SmsApplication.getDefaultSmsApplication(context, false);
        if (component != null) {
            defaultSmsPackage = component.getPackageName();
        }
        Rlog.d(LOG_TAG, "checkShouldWriteSmsPackage defaultSmsPackage: " + defaultSmsPackage + ", current package: " + packageName);
        if ((defaultSmsPackage != null && defaultSmsPackage.equals(packageName)) || packageName.equals(BLUETOOTH_PACKAGE_NAME)) {
            return false;
        }
        String shouldWriteSmsPackageString = null;
        try {
            shouldWriteSmsPackageString = Settings.System.getString(context.getContentResolver(), "should_write_sms_package");
            Rlog.i(LOG_TAG, "checkShouldWriteSmsPackage cust: " + shouldWriteSmsPackageString);
        } catch (IllegalArgumentException e) {
            Rlog.e(LOG_TAG, "IllegalArgumentException when got name value");
        } catch (Exception e2) {
            Rlog.e(LOG_TAG, "Exception when got name value", e2);
        }
        if (shouldWriteSmsPackageString == null || "".equals(shouldWriteSmsPackageString)) {
            shouldWriteSmsPackageString = "com.huawei.vassistant,com.hellotext.hello,com.android.emergency";
        }
        for (String s : shouldWriteSmsPackageString.split(",")) {
            if (packageName.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public boolean currentSubIsChinaTelecomSim(int phoneId) {
        return IS_CHINATELECOM || HwTelephonyManagerInner.getDefault().isCTSimCard(phoneId);
    }

    public boolean isLimitNumOfSmsEnabled(boolean isOutgoing) {
        return isDayMode(isOutgoing) || isWeekMode(isOutgoing) || isMonthMode(isOutgoing);
    }

    public void updateSmsUsedNum(Context context, boolean isOutgoing) {
        String policyName;
        if (context == null) {
            Rlog.e(LOG_TAG, "context is null");
            return;
        }
        if (isOutgoing) {
            policyName = OUTGOING_SMS_LIMIT;
        } else {
            policyName = INCOMING_SMS_LIMIT;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(policyName, 0);
        if (sharedPreferences == null) {
            Rlog.e(LOG_TAG, "get sharedPreferences failed");
            return;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        long nowTime = System.currentTimeMillis();
        long shareTimeDay = sharedPreferences.getLong(DAY_MODE_TIME, getStartTime(isOutgoing, DAY_MODE));
        if (isSameDay(nowTime, shareTimeDay)) {
            editor.putInt(USED_OF_DAY, sharedPreferences.getInt(USED_OF_DAY, 0) + 1);
            editor.putLong(DAY_MODE_TIME, shareTimeDay);
        } else {
            editor.putInt(USED_OF_DAY, 1);
            editor.putLong(DAY_MODE_TIME, nowTime);
        }
        long shareTimeWeek = sharedPreferences.getLong(WEEK_MODE_TIME, getStartTime(isOutgoing, WEEK_MODE));
        if (isSameWeek(nowTime, shareTimeWeek)) {
            editor.putInt(USED_OF_WEEK, sharedPreferences.getInt(USED_OF_WEEK, 0) + 1);
            editor.putLong(WEEK_MODE_TIME, shareTimeWeek);
        } else {
            editor.putInt(USED_OF_WEEK, 1);
            editor.putLong(WEEK_MODE_TIME, nowTime);
        }
        long shareTimeMonth = sharedPreferences.getLong(MONTH_MODE_TIME, getStartTime(isOutgoing, MONTH_MODE));
        if (isSameMonth(nowTime, shareTimeMonth)) {
            editor.putInt(USED_OF_MONTH, sharedPreferences.getInt(USED_OF_MONTH, 0) + 1);
            editor.putLong(MONTH_MODE_TIME, shareTimeMonth);
        } else {
            editor.putInt(USED_OF_MONTH, 1);
            editor.putLong(MONTH_MODE_TIME, nowTime);
        }
        editor.apply();
    }

    public boolean isExceedSMSLimit(Context context, boolean isOutgoing) {
        String str;
        int usedNumOfWeek;
        SharedPreferences sharedPreferences = null;
        String policyName = isOutgoing ? OUTGOING_SMS_LIMIT : INCOMING_SMS_LIMIT;
        if (context != null) {
            sharedPreferences = context.getSharedPreferences(policyName, 0);
        }
        if (sharedPreferences == null) {
            return false;
        }
        int usedNumOfDay = sharedPreferences.getInt(USED_OF_DAY, 0);
        int usedNumOfWeek2 = sharedPreferences.getInt(USED_OF_WEEK, 0);
        int usedNumOfMonth = sharedPreferences.getInt(USED_OF_MONTH, 0);
        int limitNumOfDay = getLimitNum(isOutgoing, DAY_MODE);
        int limitNumOfWeek = getLimitNum(isOutgoing, WEEK_MODE);
        int limitNumOfMonth = getLimitNum(isOutgoing, MONTH_MODE);
        long nowTime = System.currentTimeMillis();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (editor == null) {
            Rlog.e(LOG_TAG, "editor is null");
            return false;
        }
        if (isMonthMode(isOutgoing)) {
            str = USED_OF_WEEK;
            usedNumOfWeek = usedNumOfWeek2;
            if (!isSameMonth(nowTime, sharedPreferences.getLong(MONTH_MODE_TIME, getStartTime(isOutgoing, MONTH_MODE)))) {
                Rlog.i(LOG_TAG, "it is next month, so reset data");
                editor.putInt(USED_OF_MONTH, 0);
                editor.putLong(MONTH_MODE_TIME, nowTime);
            } else if (usedNumOfMonth >= limitNumOfMonth) {
                return true;
            }
        } else {
            str = USED_OF_WEEK;
            usedNumOfWeek = usedNumOfWeek2;
        }
        if (isWeekMode(isOutgoing)) {
            if (!isSameWeek(nowTime, sharedPreferences.getLong(WEEK_MODE_TIME, getStartTime(isOutgoing, WEEK_MODE)))) {
                Rlog.i(LOG_TAG, "it is next week, so reset data");
                editor.putInt(str, 0);
                editor.putLong(WEEK_MODE_TIME, nowTime);
            } else if (usedNumOfWeek >= limitNumOfWeek) {
                return true;
            }
        }
        if (!isDayMode(isOutgoing)) {
            return false;
        }
        if (!isSameDay(nowTime, sharedPreferences.getLong(DAY_MODE_TIME, getStartTime(isOutgoing, DAY_MODE)))) {
            Rlog.i(LOG_TAG, "it is next day, so reset data");
            editor.putInt(USED_OF_DAY, 0);
            editor.putLong(DAY_MODE_TIME, nowTime);
            return false;
        } else if (usedNumOfDay >= limitNumOfDay) {
            return true;
        } else {
            return false;
        }
    }

    private long getStartTime(boolean isOutgoing, String timeMode) {
        String policyName;
        String policyName2;
        String policyName3;
        if (TextUtils.isEmpty(timeMode)) {
            return PARTIAL_SEGMENT_EXPIRE_TIME;
        }
        char c = 65535;
        int hashCode = timeMode.hashCode();
        if (hashCode != -2105529586) {
            if (hashCode != -1628741630) {
                if (hashCode == 1931104358 && timeMode.equals(DAY_MODE)) {
                    c = 0;
                }
            } else if (timeMode.equals(MONTH_MODE)) {
                c = 2;
            }
        } else if (timeMode.equals(WEEK_MODE)) {
            c = 1;
        }
        if (c == 0) {
            if (isOutgoing) {
                policyName = OUTGOING_DAY_LIMIT;
            } else {
                policyName = INCOMING_DAY_LIMIT;
            }
            Bundle bundleDay = this.mDpm.getPolicy((ComponentName) null, policyName);
            if (bundleDay != null) {
                return getStartTimeByTimeMode(DAY_MODE_TIME, bundleDay);
            }
            return PARTIAL_SEGMENT_EXPIRE_TIME;
        } else if (c == 1) {
            if (isOutgoing) {
                policyName2 = OUTGOING_WEEK_LIMIT;
            } else {
                policyName2 = INCOMING_WEEK_LIMIT;
            }
            Bundle bundleWeek = this.mDpm.getPolicy((ComponentName) null, policyName2);
            if (bundleWeek != null) {
                return getStartTimeByTimeMode(WEEK_MODE_TIME, bundleWeek);
            }
            return PARTIAL_SEGMENT_EXPIRE_TIME;
        } else if (c != 2) {
            return PARTIAL_SEGMENT_EXPIRE_TIME;
        } else {
            if (isOutgoing) {
                policyName3 = OUTGOING_MONTH_LIMIT;
            } else {
                policyName3 = INCOMING_MONTH_LIMIT;
            }
            Bundle bundleMonth = this.mDpm.getPolicy((ComponentName) null, policyName3);
            if (bundleMonth != null) {
                return getStartTimeByTimeMode(MONTH_MODE_TIME, bundleMonth);
            }
            return PARTIAL_SEGMENT_EXPIRE_TIME;
        }
    }

    private long getStartTimeByTimeMode(String timeMode, Bundle modeBundle) {
        if (modeBundle == null) {
            return PARTIAL_SEGMENT_EXPIRE_TIME;
        }
        try {
            return Long.parseLong(modeBundle.getString(timeMode, INIT_VALUE));
        } catch (NumberFormatException e) {
            Rlog.e(LOG_TAG, "getStartTimeByTimeMode NumberFormatException");
            return PARTIAL_SEGMENT_EXPIRE_TIME;
        }
    }

    private int getLimitNum(boolean isOutgoing, String timeMode) {
        if (TextUtils.isEmpty(timeMode)) {
            return 0;
        }
        char c = 65535;
        int hashCode = timeMode.hashCode();
        if (hashCode != -2105529586) {
            if (hashCode != -1628741630) {
                if (hashCode == 1931104358 && timeMode.equals(DAY_MODE)) {
                    c = 0;
                }
            } else if (timeMode.equals(MONTH_MODE)) {
                c = 2;
            }
        } else if (timeMode.equals(WEEK_MODE)) {
            c = 1;
        }
        if (c == 0) {
            return getDayModeLimitNum(isOutgoing);
        }
        if (c == 1) {
            return getWeekModeLimitNum(isOutgoing);
        }
        if (c != 2) {
            return 0;
        }
        return getMonthModeLimitNum(isOutgoing);
    }

    private int getDayModeLimitNum(boolean isOutgoing) {
        String policyName;
        if (isOutgoing) {
            policyName = OUTGOING_DAY_LIMIT;
        } else {
            policyName = INCOMING_DAY_LIMIT;
        }
        Bundle bundleDay = this.mDpm.getPolicy((ComponentName) null, policyName);
        if (bundleDay == null) {
            return 0;
        }
        try {
            return Integer.parseInt(bundleDay.getString(LIMIT_OF_DAY, INIT_VALUE));
        } catch (NumberFormatException e) {
            Rlog.e(LOG_TAG, "DAY_MODE NumberFormatException");
            return 0;
        }
    }

    private int getWeekModeLimitNum(boolean isOutgoing) {
        String policyName;
        if (isOutgoing) {
            policyName = OUTGOING_WEEK_LIMIT;
        } else {
            policyName = INCOMING_WEEK_LIMIT;
        }
        Bundle bundleWeek = this.mDpm.getPolicy((ComponentName) null, policyName);
        if (bundleWeek == null) {
            return 0;
        }
        try {
            return Integer.parseInt(bundleWeek.getString(LIMIT_OF_WEEK, INIT_VALUE));
        } catch (NumberFormatException e) {
            Rlog.e(LOG_TAG, "WEEK_MODE NumberFormatException");
            return 0;
        }
    }

    private int getMonthModeLimitNum(boolean isOutgoing) {
        String policyName;
        if (isOutgoing) {
            policyName = OUTGOING_MONTH_LIMIT;
        } else {
            policyName = INCOMING_MONTH_LIMIT;
        }
        Bundle bundleMonth = this.mDpm.getPolicy((ComponentName) null, policyName);
        if (bundleMonth == null) {
            return 0;
        }
        try {
            return Integer.parseInt(bundleMonth.getString(LIMIT_OF_MONTH, INIT_VALUE));
        } catch (NumberFormatException e) {
            Rlog.e(LOG_TAG, "MONTH_MODE NumberFormatException");
            return 0;
        }
    }

    private boolean isDayMode(boolean isOutgoing) {
        Bundle bundle;
        if (isOutgoing) {
            bundle = this.mDpm.getPolicy((ComponentName) null, OUTGOING_DAY_LIMIT);
        } else {
            bundle = this.mDpm.getPolicy((ComponentName) null, INCOMING_DAY_LIMIT);
        }
        if (bundle != null) {
            return Boolean.parseBoolean(bundle.getString(DAY_MODE, MODE_INIT_VALUE));
        }
        return false;
    }

    private boolean isWeekMode(boolean isOutgoing) {
        Bundle bundle;
        if (isOutgoing) {
            bundle = this.mDpm.getPolicy((ComponentName) null, OUTGOING_WEEK_LIMIT);
        } else {
            bundle = this.mDpm.getPolicy((ComponentName) null, INCOMING_WEEK_LIMIT);
        }
        if (bundle != null) {
            return Boolean.parseBoolean(bundle.getString(WEEK_MODE, MODE_INIT_VALUE));
        }
        return false;
    }

    private boolean isMonthMode(boolean isOutgoing) {
        Bundle bundle;
        if (isOutgoing) {
            bundle = this.mDpm.getPolicy((ComponentName) null, OUTGOING_MONTH_LIMIT);
        } else {
            bundle = this.mDpm.getPolicy((ComponentName) null, INCOMING_MONTH_LIMIT);
        }
        if (bundle != null) {
            return Boolean.parseBoolean(bundle.getString(MONTH_MODE, MODE_INIT_VALUE));
        }
        return false;
    }

    private boolean isSameDay(long nowTime, long sharedTime) {
        if (TextUtils.isEmpty(String.valueOf(nowTime)) || TextUtils.isEmpty(String.valueOf(sharedTime))) {
            Rlog.e(LOG_TAG, "get time failed");
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(nowTime);
        int nowDay = calendar.get(6);
        int nowYear = calendar.get(1);
        calendar.setTimeInMillis(sharedTime);
        int sharedDay = calendar.get(6);
        if (nowYear == calendar.get(1) && sharedDay == nowDay) {
            return true;
        }
        return false;
    }

    private boolean isSameWeek(long nowTime, long sharedTime) {
        if (TextUtils.isEmpty(String.valueOf(nowTime)) || TextUtils.isEmpty(String.valueOf(sharedTime))) {
            Rlog.e(LOG_TAG, "get time failed");
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(2);
        calendar.setTimeInMillis(nowTime);
        int nowWeek = calendar.get(3);
        int nowYear = calendar.get(1);
        calendar.setTimeInMillis(sharedTime);
        int sharedWeek = calendar.get(3);
        int subYear = nowYear - calendar.get(1);
        if (subYear == 0 && nowWeek == sharedWeek) {
            return true;
        }
        if (subYear != 1) {
            Rlog.d(LOG_TAG, "more than 1 year");
            return false;
        } else if (calendar.get(2) == DECEMBER && sharedWeek == nowWeek) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isSameMonth(long nowTime, long sharedTime) {
        if (TextUtils.isEmpty(String.valueOf(nowTime)) || TextUtils.isEmpty(String.valueOf(sharedTime))) {
            Rlog.e(LOG_TAG, "get time failed");
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(nowTime);
        int nowMonth = calendar.get(2);
        int nowYear = calendar.get(1);
        calendar.setTimeInMillis(sharedTime);
        int sharedMonth = calendar.get(2);
        if (nowYear == calendar.get(1) && nowMonth == sharedMonth) {
            return true;
        }
        return false;
    }

    public SmsUsageMonitor createHwSmsUsageMonitor(Context context, Phone phone) {
        return new HwSmsUsageMonitor(context, phone);
    }

    public void report(Context context, int errorType, String content, int subId) {
        HwRadarUtils.report(context, errorType, content, subId);
    }

    public void addInboxInsertObserver(Context context) {
        context.getContentResolver().registerContentObserver(Uri.parse("content://sms/inbox-insert"), true, this.mInsertObserver);
    }

    public void triggerInboxInsertDoneDetect(Intent intent, boolean isClass0, Handler handler) {
        if (intent != null && "android.provider.Telephony.SMS_DELIVER".equals(intent.getAction()) && !isClass0) {
            this.mSubIdForReceivedSms = intent.getIntExtra("subscription", SubscriptionManager.getDefaultSmsSubscriptionId());
            this.mAlreadyReceivedSms.getAndIncrement();
            if (handler != null) {
                handler.removeCallbacks(this.mUpdateCountRunner);
                handler.postDelayed(this.mUpdateCountRunner, 60000);
            }
        }
    }

    public void reportSmsReceiveTimeout(Context context, int durationMillis) {
        if (!this.mIsAlreadyDurationTimeout && durationMillis >= SMS_BROADCAST_DURATION_TIMEOUT) {
            this.mIsAlreadyDurationTimeout = true;
            HwRadarUtils.report(context, 1312, "sms receive timeout:" + durationMillis + getDefaultSmsApplicationName(), this.mSubIdForReceivedSms);
        }
    }

    public void triggerSendSmsOverLoadCheck(SMSDispatcher smsDispatcher) {
        this.mAlreadySentSms.getAndIncrement();
        if (this.mAlreadySentSms.get() == 1) {
            this.mHwHandler.removeMessages(EVENT_SEND_SMS_OVERLOAD);
            Handler handler = this.mHwHandler;
            handler.sendMessageDelayed(handler.obtainMessage(EVENT_SEND_SMS_OVERLOAD, smsDispatcher), 3600000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSendSmsOverLoad(SMSDispatcher smsDispatcher) {
        Context context;
        if (this.mAlreadySentSms.get() > SEND_SMS_OVERlOAD_COUNT) {
            boolean findThirdApp = false;
            StringBuffer buf = new StringBuffer();
            buf.append(getDefaultSmsApplicationName());
            buf.append(" SendSmsOverLoad");
            int sendSmsCount = smsDispatcher.getPackageSendSmsCount().size();
            for (int i = 0; i < sendSmsCount; i++) {
                String packageName = (String) smsDispatcher.getPackageSendSmsCount().valueAt(i);
                if (!getDefaultSmsApplicationName().equals(packageName) && !"com.android.mms".equals(packageName)) {
                    buf.append(" ");
                    buf.append(packageName);
                    findThirdApp = true;
                }
            }
            if (findThirdApp) {
                if (PhoneFactory.getDefaultPhone() != null) {
                    context = PhoneFactory.getDefaultPhone().getContext();
                } else {
                    context = null;
                }
                HwRadarUtils.report(context, HwRadarUtils.ERR_SMS_SEND_BACKGROUND, buf.toString(), smsDispatcher.getSubId());
            }
        }
        this.mAlreadySentSms.set(0);
        smsDispatcher.clearPackageSendSmsCount();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getDefaultSmsApplicationName() {
        Context context = null;
        if (PhoneFactory.getDefaultPhone() != null) {
            context = PhoneFactory.getDefaultPhone().getContext();
        }
        ComponentName componentName = SmsApplication.getDefaultSmsApplication(context, false);
        if (componentName != null) {
            return componentName.getPackageName();
        }
        return "";
    }

    public boolean isSentSmsFromRejectCall(PendingIntent sentIntent) {
        if (sentIntent == null || sentIntent.getIntent() == null || !TELECOM_PACKAGE_NAME.equals(sentIntent.getIntent().getStringExtra("packageName"))) {
            return false;
        }
        Rlog.i(LOG_TAG, "isSentSmsFromRejectCall");
        return true;
    }

    public boolean hasSameSmsPdu(byte[] pdu, int phoneId) {
        SmsStorageMonitor monitor;
        Rlog.i(LOG_TAG, "check if there is a same pdu in mSmsList.");
        byte[] newPdu = new byte[(pdu.length + 1)];
        System.arraycopy(pdu, 0, newPdu, 0, pdu.length);
        newPdu[pdu.length] = (byte) phoneId;
        Phone phone = PhoneFactory.getDefaultPhone();
        if (phone == null || (monitor = phone.mSmsStorageMonitor) == null || monitor.isStorageAvailable()) {
            synchronized (this.mSmsList) {
                for (byte[] oldPdu : this.mSmsList) {
                    if (Arrays.equals(newPdu, oldPdu)) {
                        return true;
                    }
                }
                this.mSmsList.add(newPdu);
                Rlog.i(LOG_TAG, "mSmsList.size() = " + this.mSmsList.size());
                if (this.mSmsList.size() > 30) {
                    Rlog.i(LOG_TAG, "mSmsList.size() > MAX_SMS_LIST_DEFAULT");
                    this.mSmsList.remove(0);
                }
                return false;
            }
        }
        Rlog.e(LOG_TAG, "storage is full, not to save pdu and not intercept!");
        return false;
    }

    public CharSequence getAppLabel(Context context, String appPackage, int userId, SMSDispatcher smsDispatcher) {
        if (CONTACTS_PACKAGE_NAME.equals(appPackage) || NEW_CONTACTS_PACKAGE_NAME.equals(appPackage)) {
            return getMsgAppLabel(context, appPackage, userId, smsDispatcher);
        }
        return smsDispatcher.getAppLabelHw(appPackage, userId);
    }

    private CharSequence getMsgAppLabel(Context context, String appPackage, int userId, SMSDispatcher smsDispatcher) {
        PackageManager pm = context.getPackageManager();
        try {
            int resId = pm.getResourcesForApplicationAsUser(appPackage, userId).getIdentifier(MESSAGE_STRING_NAME, RESOURCE_TYPE_STRING, appPackage);
            CharSequence lmsgApplabel = null;
            if (resId != 0) {
                lmsgApplabel = pm.getText(appPackage, resId, null);
            }
            if (resId == 0 || lmsgApplabel == null) {
                return smsDispatcher.getAppLabelHw(appPackage, userId);
            }
            return lmsgApplabel;
        } catch (PackageManager.NameNotFoundException e) {
            Rlog.e(LOG_TAG, "PackageManager Name Not Found for package " + appPackage);
            return appPackage;
        }
    }

    public void scanAndDeleteOlderPartialMessages(InboundSmsTracker tracker, ContentResolver resolver) {
        Rlog.i(LOG_TAG, "scanAndDeleteOlderPartialMessages, count = " + tracker.getMessageCount());
        if (tracker.getMessageCount() != 1) {
            String address = tracker.getAddress();
            String refNumber = Integer.toString(tracker.getReferenceNumber());
            String count = Integer.toString(tracker.getMessageCount());
            int delCount = 0;
            try {
                delCount = resolver.delete(RAW_URI, new StringBuilder("date < " + (tracker.getTimestamp() - PARTIAL_SEGMENT_EXPIRE_TIME) + " AND " + SELECT_BY_REFERENCE).toString(), new String[]{address, refNumber, count});
            } catch (SQLException e) {
                Rlog.e(LOG_TAG, "scanAndDeleteOlderPartialMessages got SQLException");
            } catch (Exception e2) {
                Rlog.e(LOG_TAG, "scanAndDeleteOlderPartialMessages got exception");
            }
            if (delCount > 0) {
                Rlog.i(LOG_TAG, "scanAndDeleteOlderPartialMessages: delete " + delCount + " raw sms older than " + SystemProperties.getInt("ro.config.hw_drop_oldsms_day", 3) + " days");
            }
        }
    }

    public int dealBlacklistSms(Context context, SmsMessage sms, int result) {
        int finalResult = result;
        if (sms.mWrappedSmsMessage.blacklistFlag) {
            finalResult = -1;
            String defaultSmsApplicationName = "";
            ComponentName componentName = SmsApplication.getDefaultSmsApplication(context, false);
            if (componentName != null) {
                defaultSmsApplicationName = componentName.getPackageName();
            }
            report(context, 1312, "receive a blacklist sms, modem has acked it, fw need't reply" + defaultSmsApplicationName, 0);
        }
        return finalResult;
    }

    public void filterMyNumber(Context context, boolean groupMmsEnabled, HashSet<String> recipients, HashMap<Integer, EncodedStringValue[]> addressMap, int slotId) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        ContentResolver contentResolver = context.getContentResolver();
        if (groupMmsEnabled && recipients.size() != 1 && recipients.size() <= 2) {
            String myNumber = telephonyManager.getLine1Number(SubscriptionController.getInstance().getSubIdUsingPhoneId(slotId));
            if (TextUtils.isEmpty(myNumber)) {
                myNumber = Settings.Secure.getString(contentResolver, "localNumberFromDb_" + slotId);
            }
            if (TextUtils.isEmpty(myNumber)) {
                EncodedStringValue[] arrayTo = addressMap.get(151);
                EncodedStringValue[] arrayFrom = addressMap.get(137);
                if (!(arrayTo == null || arrayFrom == null)) {
                    String numberFrom = "";
                    int length = arrayFrom.length;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            break;
                        }
                        EncodedStringValue v = arrayFrom[i];
                        if (!(v == null || TextUtils.isEmpty(v.getString()))) {
                            numberFrom = v.getString();
                            break;
                        }
                        i++;
                    }
                    for (EncodedStringValue v2 : arrayTo) {
                        if (v2 != null && !numberFrom.equals(v2.getString()) && recipients.contains(v2.getString())) {
                            recipients.remove(v2.getString());
                            return;
                        }
                    }
                }
            }
        }
    }

    public boolean getTipPremiumFromConfig(int phoneId) {
        boolean valueFromProp = TIP_PREMIUM_SHORT_CODE;
        Boolean valueFromCard = (Boolean) HwCfgFilePolicy.getValue("flag_tip_premium", phoneId, Boolean.class);
        Rlog.d(LOG_TAG, "getTipPremiumFromSimValue, phoneId: " + phoneId + ", card: " + valueFromCard + ", prop: " + valueFromProp);
        return valueFromCard != null ? valueFromCard.booleanValue() : valueFromProp;
    }

    public boolean isIOTVersion() {
        String[] info = DELIVER_CHANNEL_INFO.split(",");
        int result = 0;
        if (info.length > 4 && info[4] != null) {
            try {
                result = Integer.parseInt(info[4]);
            } catch (NumberFormatException e) {
                Rlog.e(LOG_TAG, "Exception while parsing Integer");
            }
        }
        return result != 0;
    }

    public void addNumberPlusSign(CdmaSmsAddress addr, byte[] data) {
        if (PLUS_TRANFER_IN_AP) {
            String number = HwCustPlusAndIddNddConvertUtils.replaceIddNddWithPlusForSms(new String(addr.origBytes, Charset.defaultCharset()));
            if (addr.ton == 1 && number != null && number.length() > 0 && number.charAt(0) != '+') {
                Rlog.i(LOG_TAG, "newFromParcel ton == SmsAddress.TON_INTERNATIONAL");
                number = "+" + number;
            }
            if (number != null) {
                addr.origBytes = number.getBytes(Charset.defaultCharset());
            } else {
                addr.origBytes = data;
            }
            addr.numberOfDigits = addr.origBytes.length;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v1, resolved type: android.content.Intent */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v1, types: [byte[][], java.io.Serializable] */
    public void dispatchCTAutoRegSmsPdu(Context context, SmsMessageBase smsb, int subId, Handler handler) {
        ?? r0 = {smsb.getUserData()};
        Intent intent = new Intent("android.provider.Telephony.CT_AUTO_REG_RECV_CONFIRM_ACK");
        intent.putExtra("pdus", (Serializable) r0);
        intent.putExtra("SendSubscription", subId);
        intent.addFlags(134217728);
        context.sendOrderedBroadcast(intent, "android.permission.RECEIVE_SMS", null, handler, -1, null, null);
        Rlog.i(LOG_TAG, "dispatchCTAutoRegSmsPdus end. Broadcast send to apk!");
    }

    public int handleExtendTeleService(int teleService, CdmaSMSDispatcher smsDispatcher, SmsMessageBase smsb) {
        if (teleService != 65002) {
            if (teleService != 65005) {
                return 4;
            }
            if ((92 == SystemProperties.getInt("ro.config.hw_opta", 0) || 999 == SystemProperties.getInt("ro.config.hw_opta", 0)) && 156 == SystemProperties.getInt("ro.config.hw_optb", 0)) {
                Rlog.d(LOG_TAG, "CT's AutoRegSms notification!");
                dispatchCTAutoRegSmsPdu(smsDispatcher.mContext, smsb, smsDispatcher.getSubId(), smsDispatcher);
                return 1;
            }
        }
        return 0;
    }
}

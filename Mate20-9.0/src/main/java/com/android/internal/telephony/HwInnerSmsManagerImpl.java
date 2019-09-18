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
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.cdma.CdmaSMSDispatcher;
import com.android.internal.telephony.cdma.HwCdmaSMSDispatcher;
import com.android.internal.telephony.cdma.HwCustPlusAndIddNddConvertUtils;
import com.android.internal.telephony.cdma.HwSmsMessage;
import com.android.internal.telephony.cdma.sms.CdmaSmsAddress;
import com.android.internal.telephony.cdma.sms.HwBearerData;
import com.android.internal.telephony.gsm.GsmInboundSmsHandler;
import com.android.internal.telephony.gsm.GsmSMSDispatcher;
import com.android.internal.telephony.gsm.HwGsmSMSDispatcher;
import com.android.internal.telephony.gsm.SmsMessageUtils;
import com.google.android.mms.pdu.EncodedStringValue;
import com.huawei.utils.reflect.HwReflectUtils;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import huawei.android.telephony.CallerInfoHW;
import huawei.android.telephony.wrapper.WrapperFactory;
import huawei.cust.HwCfgFilePolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.PatternSyntaxException;

public class HwInnerSmsManagerImpl implements HwInnerSmsManager {
    private static final String BLOCK_TYPE = "BLOCKED_TYPE";
    private static final String BLUETOOTH_PACKAGE_NAME = "com.android.bluetooth";
    private static final Class<?> CLASS_ContentType = HwReflectUtils.getClass("com.google.android.mms.ContentType");
    private static final Class<?> CLASS_EncodedStringValue = HwReflectUtils.getClass("com.google.android.mms.pdu.EncodedStringValue");
    private static final Class<?> CLASS_GenericPdu = HwReflectUtils.getClass("com.google.android.mms.pdu.GenericPdu");
    private static final Class<?> CLASS_PduHeaders = HwReflectUtils.getClass("com.google.android.mms.pdu.PduHeaders");
    private static final Class<?> CLASS_PduParser = HwReflectUtils.getClass("com.google.android.mms.pdu.PduParser");
    private static final String COLUMN_IS_PRIVATE = "is_private";
    private static final Constructor<?> CONSTRUCTOR_PduParser = HwReflectUtils.getConstructor(CLASS_PduParser, new Class[]{byte[].class});
    private static final String CONTACTS_PACKAGE_NAME = "com.android.contacts";
    private static final String CONTACT_PACKAGE_NAME = "com.android.contacts";
    private static final String DAY_MODE = "day_mode";
    private static final String DAY_MODE_TIME = "day_mode_time";
    private static final int DECEMBER = 11;
    private static final String DEFAULT_SMS_APPLICATION = "com.android.mms";
    private static final String DELIVER_CHANNEL_INFO = SystemProperties.get("ro.config.hw_channel_info", "0,0,460,1,0");
    public static final int ERROR_BASE_MMS = 1300;
    public static final int ERR_SMS_RECEIVE = 1312;
    public static final int ERR_SMS_SEND = 1311;
    private static final int EVENT_SEND_SMS_OVERLOAD = 20;
    private static final Field FIELD_MESSAGE_TYPE_NOTIFICATION_IND = HwReflectUtils.getField(CLASS_PduHeaders, "MESSAGE_TYPE_NOTIFICATION_IND");
    private static final Field FIELD_MMS_MESSAGE = HwReflectUtils.getField(CLASS_ContentType, "MMS_MESSAGE");
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
    private static final int MESSAGE_TYPE_NOTIFICATION_IND = HwReflectUtils.objectToInt(HwReflectUtils.getFieldValue(null, FIELD_MESSAGE_TYPE_NOTIFICATION_IND));
    private static final Method METHOD_getFrom = HwReflectUtils.getMethod(CLASS_GenericPdu, "getFrom", new Class[0]);
    private static final Method METHOD_getMessageType = HwReflectUtils.getMethod(CLASS_GenericPdu, "getMessageType", new Class[0]);
    private static final Method METHOD_getString = HwReflectUtils.getMethod(CLASS_EncodedStringValue, "getString", new Class[0]);
    private static final Method METHOD_parse = HwReflectUtils.getMethod(CLASS_PduParser, "parse", new Class[0]);
    private static final String MMS_ACTIVITY_NAME = "com.android.mms.ui.ComposeMessageActivity";
    private static final String MMS_MESSAGE = HwReflectUtils.objectToString(HwReflectUtils.getFieldValue(null, FIELD_MMS_MESSAGE));
    private static final String MMS_PACKAGE_NAME = "com.android.mms";
    private static final String MODE_INIT_VALUE = "false";
    private static final String MONTH_MODE = "month_mode";
    private static final String MONTH_MODE_TIME = "month_mode_time";
    static int Message_Reference_Num = 0;
    private static final String OUTGOING_DAY_LIMIT = "outgoing_day_limit";
    private static final String OUTGOING_MONTH_LIMIT = "outgoing_month_limit";
    private static final String OUTGOING_SMS_LIMIT = "outgoing_limit";
    private static final String OUTGOING_WEEK_LIMIT = "outgoing_week_limit";
    private static final long PARTIAL_SEGMENT_EXPIRE_TIME = (86400000 * ((long) SystemProperties.getInt("ro.config.hw_drop_oldsms_day", 3)));
    private static boolean PLUS_TRANFER_IN_AP = (!HwModemCapability.isCapabilitySupport(2));
    private static final String POLICY_KEY = "value";
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
    private static SmsMessageUtils gsmSmsMessageUtils = new SmsMessageUtils();
    private static HwInnerSmsManager mInstance = new HwInnerSmsManagerImpl();
    private static final Uri sRawUri = Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, "raw");
    /* access modifiers changed from: private */
    public AtomicInteger mAlreadyReceivedSms = new AtomicInteger(0);
    private AtomicInteger mAlreadySentSms = new AtomicInteger(0);
    private HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();
    private Handler mHwHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 20) {
                if ((msg.obj != null) && (msg.obj instanceof SMSDispatcher)) {
                    HwInnerSmsManagerImpl.this.handleSendSmsOverLoad((SMSDispatcher) msg.obj);
                }
            }
        }
    };
    private ContentObserver mInsertObserver = new ContentObserver(new Handler()) {
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
    /* access modifiers changed from: private */
    public int mSubIdForReceivedSms = -1;
    private Runnable mUpdateCountRunner = new Runnable() {
        public void run() {
            if (HwInnerSmsManagerImpl.this.mAlreadyReceivedSms.get() > 0) {
                HwRadarUtils.report(PhoneFactory.getDefaultPhone() != null ? PhoneFactory.getDefaultPhone().getContext() : null, 1312, "sms receive fail:" + HwInnerSmsManagerImpl.this.getDefaultSmsApplicationName(), HwInnerSmsManagerImpl.this.mSubIdForReceivedSms);
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
        return mInstance;
    }

    private static void setMessageReferenceNum(int value) {
        Message_Reference_Num = value;
    }

    public int getMessageRefrenceNumber() {
        if (255 <= Message_Reference_Num) {
            setMessageReferenceNum(0);
        } else {
            setMessageReferenceNum(Message_Reference_Num + 1);
        }
        return Message_Reference_Num;
    }

    public byte[] getUserDataHeaderForGsm(int seqNum, int maxNum, int MessageReferenceNum) {
        byte[] UDH = new byte[5];
        if (seqNum > maxNum) {
            return null;
        }
        UDH[0] = 0;
        UDH[1] = 3;
        UDH[2] = (byte) MessageReferenceNum;
        UDH[3] = (byte) maxNum;
        UDH[4] = (byte) seqNum;
        Rlog.d("android/SmsMessage", "maxNum:" + maxNum + ";seqNum:" + seqNum + ";MR:" + UDH[2]);
        return UDH;
    }

    public SmsMessage.SubmitPdu getSubmitPdu(String scAddress, String timeStamps, String destinationAddress, String message, byte[] UDH) {
        return getSubmitPdu(scAddress, timeStamps, destinationAddress, message, UDH, SUBSCRIPTION_FOR_NONE_VARIABLE_METHODS);
    }

    public SmsMessage.SubmitPdu getSubmitPdu(String scAddress, String timeStamps, String destinationAddress, String message, byte[] UDH, int subscription) {
        int activePhone;
        SmsMessageBase.SubmitPduBase spb;
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            activePhone = TelephonyManager.getDefault().getCurrentPhoneType(subscription);
        } else {
            activePhone = TelephonyManager.getDefault().getPhoneType();
        }
        if (2 == activePhone) {
            SmsHeader header = null;
            if (UDH != null) {
                header = SmsHeader.fromByteArray(UDH);
            }
            spb = HwSmsMessage.getSubmitDeliverPdu(true, null, destinationAddress, message, header);
        } else {
            spb = com.android.internal.telephony.gsm.SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, false, UDH);
        }
        if (spb == null) {
            return null;
        }
        return new SmsMessage.SubmitPdu(spb);
    }

    public SmsMessage.SubmitPdu getDeliverPdu(String scAddress, String scTimeStamp, String origAddress, String message, byte[] UDH) {
        return getDeliverPdu(scAddress, scTimeStamp, origAddress, message, UDH, SUBSCRIPTION_FOR_NONE_VARIABLE_METHODS);
    }

    public SmsMessage.SubmitPdu getDeliverPdu(String scAddress, String scTimeStamp, String origAddress, String message, byte[] UDH, int subscription) {
        int activePhone;
        SmsHeader header;
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            activePhone = TelephonyManager.getDefault().getCurrentPhoneType(subscription);
        } else {
            activePhone = TelephonyManager.getDefault().getPhoneType();
        }
        if (2 == activePhone) {
            SmsHeader header2 = null;
            if (UDH != null) {
                header2 = SmsHeader.fromByteArray(UDH);
            }
            header = HwSmsMessage.getSubmitDeliverPdu(false, scTimeStamp, origAddress, message, header2);
        } else {
            header = com.android.internal.telephony.gsm.HwSmsMessage.getDeliverPdu(scAddress, scTimeStamp, origAddress, message, UDH);
        }
        if (header == null) {
            return null;
        }
        return new SmsMessage.SubmitPdu(header);
    }

    public SmsMessage createFromEfRecord(int index, byte[] data, int subscription) {
        SmsMessageBase wrappedMessage;
        if (WrapperFactory.getMSimTelephonyManagerWrapper().getCurrentPhoneType(subscription) == 2) {
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

    public WspTypeDecoder createHwWspTypeDecoder(byte[] pdu) {
        return new HwWspTypeDecoder(pdu);
    }

    public boolean handleWapPushExtraMimeType(String mimeType) {
        return HwWspTypeDecoder.CONTENT_TYPE_B_CONNECT_WBXML.endsWith(mimeType);
    }

    public WapPushOverSms createHwWapPushOverSms(Phone phone, SMSDispatcher smsDispatcher) {
        return new HwWapPushOverSms(phone, smsDispatcher);
    }

    public WapPushOverSms createHwWapPushOverSms(Context context) {
        return new HwWapPushOverSms(context);
    }

    public IccSmsInterfaceManager createHwIccSmsInterfaceManager(Phone phone) {
        return new HwIccSmsInterfaceManager(phone);
    }

    public GsmSMSDispatcher createHwGsmSMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController, GsmInboundSmsHandler gsmInboundSmsHandler) {
        return new HwGsmSMSDispatcher(phone, smsDispatchersController, gsmInboundSmsHandler);
    }

    public CdmaSMSDispatcher createHwCdmaSMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController) {
        return new HwCdmaSMSDispatcher(phone, smsDispatchersController);
    }

    public GsmAlphabet.TextEncodingDetails calcTextEncodingDetailsEx(CharSequence msg, boolean force7BitEncoding) {
        return HwBearerData.calcTextEncodingDetailsEx(msg, force7BitEncoding);
    }

    public boolean shouldSendReceivedActionInPrivacyMode(InboundSmsHandler handler, Context context, Intent intent, String deleteWhere, String[] deleteWhereArgs) {
        Context context2 = context;
        this.mKeyguardManager = (KeyguardManager) context2.getSystemService("keyguard");
        String action = intent.getAction();
        String number = null;
        if (SystemProperties.getBoolean("ro.config.hw_privacymode", false) && 1 == Settings.Secure.getInt(context.getContentResolver(), "privacy_mode_on", 0) && (1 == Settings.Secure.getInt(context.getContentResolver(), "privacy_mode_state", 0) || (this.mKeyguardManager != null && this.mKeyguardManager.isKeyguardLocked()))) {
            if ("android.provider.Telephony.SMS_DELIVER".equals(action)) {
                SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                if (msgs != null && msgs.length > 0) {
                    number = msgs[0].getOriginatingAddress();
                }
                Intent intent2 = intent;
            } else if (!"android.provider.Telephony.WAP_PUSH_DELIVER".equals(action) || !MMS_MESSAGE.equals(intent.getType())) {
                InboundSmsHandler inboundSmsHandler = handler;
                Intent intent3 = intent;
                String str = deleteWhere;
                String[] strArr = deleteWhereArgs;
                Rlog.d(LOG_TAG, "ignore action, do nothing");
                return true;
            } else {
                try {
                    Object pdu = HwReflectUtils.invoke(HwReflectUtils.newInstance(CONSTRUCTOR_PduParser, new Object[]{intent.getByteArrayExtra("data")}), METHOD_parse, new Object[0]);
                    if (pdu != null && MESSAGE_TYPE_NOTIFICATION_IND == ((Integer) HwReflectUtils.invoke(pdu, METHOD_getMessageType, new Object[0])).intValue()) {
                        Object fromValue = HwReflectUtils.invoke(pdu, METHOD_getFrom, new Object[0]);
                        if (fromValue != null) {
                            number = (String) HwReflectUtils.invoke(fromValue, METHOD_getString, new Object[0]);
                        }
                    }
                } catch (Exception ex) {
                    Rlog.e(LOG_TAG, "get mms original number cause exception!" + ex.toString());
                }
            }
            if (number == null || !isPrivacyNumber(context2, number)) {
                InboundSmsHandler inboundSmsHandler2 = handler;
                String str2 = deleteWhere;
                String[] strArr2 = deleteWhereArgs;
                return true;
            }
            Rlog.d(LOG_TAG, "sms originating address: xxxxxx is private, do not send received action.");
            deleteFromRawTable(context2, deleteWhere, deleteWhereArgs);
            handler.sendMessage(3);
            return false;
        }
        InboundSmsHandler inboundSmsHandler3 = handler;
        Intent intent4 = intent;
        String str3 = deleteWhere;
        String[] strArr3 = deleteWhereArgs;
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0036, code lost:
        if (r7 != null) goto L_0x0038;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0038, code lost:
        r7.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0059, code lost:
        if (r7 == null) goto L_0x005c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x005c, code lost:
        r2 = new java.lang.StringBuilder();
        r2.append("The number is private or not: ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0068, code lost:
        if (r9 == false) goto L_0x006d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x006a, code lost:
        r3 = "Yes";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x006d, code lost:
        r3 = "No";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x006f, code lost:
        r2.append(r3);
        android.telephony.Rlog.d(LOG_TAG, r2.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0079, code lost:
        return r9;
     */
    private boolean isPrivacyNumber(Context context, String number) {
        Cursor c = null;
        boolean isPrivate = false;
        try {
            c = context.getContentResolver().query(URI_PRIVATE_NUMBER.buildUpon().appendEncodedPath(number).build(), new String[]{COLUMN_IS_PRIVATE}, null, null, null);
            if (c != null && c.moveToFirst()) {
                boolean z = true;
                if (c.getInt(0) != 1) {
                    z = false;
                }
                isPrivate = z;
            }
        } catch (Exception ex) {
            Rlog.e(LOG_TAG, "isPrivacyNumber cause exception!" + ex.toString());
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }

    private void deleteFromRawTable(Context context, String deleteWhere, String[] deleteWhereArgs) {
        int rows = context.getContentResolver().delete(sRawUri, deleteWhere, deleteWhereArgs);
        if (rows == 0) {
            Rlog.e(LOG_TAG, "No rows were deleted from raw table!");
            return;
        }
        Rlog.d(LOG_TAG, "Deleted " + rows + " rows from raw table.");
    }

    public boolean useCdmaFormatForMoSms() {
        return android.telephony.SmsMessageUtils.useCdmaFormatForMoSms(SUBSCRIPTION_FOR_NONE_VARIABLE_METHODS);
    }

    public ArrayList<String> fragmentText(String text) {
        return SmsMessage.fragmentText(text, SUBSCRIPTION_FOR_NONE_VARIABLE_METHODS);
    }

    public boolean isCdmaVoice() {
        return android.telephony.SmsMessageUtils.isCdmaVoice(SUBSCRIPTION_FOR_NONE_VARIABLE_METHODS);
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
            if (this.mSmsInterceptionService.dispatchNewSmsToInterceptionProcess(smsInfo, isWapPush)) {
                Rlog.d(LOG_TAG, "sms is intercepted ...");
                deleteFromRawTable(context, deleteWhere, deleteWhereArgs);
                handler.sendMessage(3);
                return true;
            }
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "Get exception while communicate with sms interception service: " + e.getMessage());
        }
        return false;
    }

    public boolean isMatchSMSPattern(String address, String policyTag, int phoneId) {
        if (address == null || TextUtils.isEmpty(policyTag)) {
            Rlog.d(LOG_TAG, "address or policyTag is null!");
            return false;
        }
        Bundle bundle = this.mDpm.getPolicy(null, policyTag);
        if (bundle == null) {
            Rlog.d(LOG_TAG, "bundle is null!");
            return false;
        }
        String pattern = bundle.getString(POLICY_KEY);
        if (TextUtils.isEmpty(pattern)) {
            Rlog.d(LOG_TAG, "pattern is null!");
            return false;
        }
        boolean isMatchPattern = false;
        String countryIso = null;
        String formatNum = null;
        if (TelephonyManager.getDefault() != null) {
            countryIso = TelephonyManager.getDefault().getNetworkCountryIso(phoneId);
        }
        Rlog.d(LOG_TAG, "countryIso : " + countryIso);
        if (countryIso != null) {
            String formatNum2 = PhoneNumberUtils.formatNumberToE164(address, countryIso.toUpperCase(Locale.getDefault()));
            int len = CallerInfoHW.getInstance().getIntlPrefixAndCCLen(formatNum2);
            if (len <= 0 || formatNum2 == null) {
                Rlog.d(LOG_TAG, "can not format address");
                formatNum = address;
            } else {
                formatNum = formatNum2.substring(len);
            }
        }
        if (TextUtils.isEmpty(formatNum) == 0) {
            try {
                isMatchPattern = formatNum.matches(pattern);
            } catch (PatternSyntaxException e) {
                Rlog.d(LOG_TAG, "pattern has exception : " + e);
                return false;
            }
        }
        if (!isMatchPattern) {
            try {
                isMatchPattern = address.matches(pattern);
            } catch (PatternSyntaxException e2) {
                Rlog.d(LOG_TAG, "pattern has exception match address: " + e2);
                return false;
            }
        }
        Rlog.d(LOG_TAG, "isMatchPattern : " + isMatchPattern);
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
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "Get exception while communicate with sms interception service: " + e.getMessage());
        }
    }

    public String getAppNameByPid(int pid, Context context) {
        String processName = "";
        if (context == null) {
            Rlog.d(LOG_TAG, "context is null");
            return processName;
        }
        List runningApps = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
        if (runningApps != null && runningApps.size() > 0) {
            Iterator i = runningApps.iterator();
            while (true) {
                if (!i.hasNext()) {
                    break;
                }
                ActivityManager.RunningAppProcessInfo appInfo = i.next();
                if (pid == appInfo.pid) {
                    Rlog.d(LOG_TAG, "pid: " + appInfo.pid + " processName: " + appInfo.processName);
                    processName = appInfo.processName;
                    break;
                }
            }
        }
        return processName;
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
            e.printStackTrace();
            return appInfo;
        }
    }

    public boolean checkShouldWriteSmsPackage(String packageName, Context context) {
        boolean ret = false;
        int i = 0;
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
            Rlog.d(LOG_TAG, "checkShouldWriteSmsPackage cust: " + shouldWriteSmsPackageString);
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "Exception when got name value", e);
        }
        if (shouldWriteSmsPackageString == null || "".equals(shouldWriteSmsPackageString)) {
            shouldWriteSmsPackageString = "com.huawei.vassistant,com.hellotext.hello,com.android.emergency";
        }
        String[] shouldWriteSmsPackageArray = shouldWriteSmsPackageString.split(",");
        int length = shouldWriteSmsPackageArray.length;
        while (true) {
            if (i >= length) {
                break;
            } else if (packageName.equalsIgnoreCase(shouldWriteSmsPackageArray[i])) {
                ret = true;
                break;
            } else {
                i++;
            }
        }
        return ret;
    }

    public boolean currentSubIsChinaTelecomSim(int phoneId) {
        return IS_CHINATELECOM || HwTelephonyManagerInner.getDefault().isCTSimCard(phoneId);
    }

    public boolean isLimitNumOfSmsEnabled(boolean isOutgoing) {
        return isDayMode(isOutgoing) || isWeekMode(isOutgoing) || isMonthMode(isOutgoing);
    }

    public void updateSmsUsedNum(Context context, boolean isOutgoing) {
        String policyName;
        Context context2 = context;
        boolean z = isOutgoing;
        if (context2 == null) {
            Rlog.e(LOG_TAG, "context is null");
            return;
        }
        if (z) {
            policyName = OUTGOING_SMS_LIMIT;
        } else {
            policyName = INCOMING_SMS_LIMIT;
        }
        SharedPreferences sharedPreferences = context2.getSharedPreferences(policyName, 0);
        if (sharedPreferences == null) {
            Rlog.e(LOG_TAG, "get sharedPreferences failed");
            return;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        long nowTime = System.currentTimeMillis();
        long shareTimeDay = sharedPreferences.getLong(DAY_MODE_TIME, getStartTime(z, DAY_MODE));
        if (isSameDay(nowTime, shareTimeDay)) {
            editor.putInt(USED_OF_DAY, sharedPreferences.getInt(USED_OF_DAY, 0) + 1);
            editor.putLong(DAY_MODE_TIME, shareTimeDay);
        } else {
            editor.putInt(USED_OF_DAY, 1);
            editor.putLong(DAY_MODE_TIME, nowTime);
        }
        long shareTimeWeek = sharedPreferences.getLong(WEEK_MODE_TIME, getStartTime(z, WEEK_MODE));
        if (isSameWeek(nowTime, shareTimeWeek)) {
            editor.putInt(USED_OF_WEEK, sharedPreferences.getInt(USED_OF_WEEK, 0) + 1);
            editor.putLong(WEEK_MODE_TIME, shareTimeWeek);
        } else {
            editor.putInt(USED_OF_WEEK, 1);
            editor.putLong(WEEK_MODE_TIME, nowTime);
        }
        long j = shareTimeDay;
        long shareTimeDay2 = sharedPreferences.getLong(MONTH_MODE_TIME, getStartTime(z, MONTH_MODE));
        if (isSameMonth(nowTime, shareTimeDay2)) {
            editor.putInt(USED_OF_MONTH, sharedPreferences.getInt(USED_OF_MONTH, 0) + 1);
            editor.putLong(MONTH_MODE_TIME, shareTimeDay2);
        } else {
            editor.putInt(USED_OF_MONTH, 1);
            editor.putLong(MONTH_MODE_TIME, nowTime);
        }
        editor.apply();
    }

    public boolean isExceedSMSLimit(Context context, boolean isOutgoing) {
        int usedNumOfWeek;
        int usedNumOfDay;
        Context context2 = context;
        boolean z = isOutgoing;
        SharedPreferences sharedPreferences = null;
        String policyName = z ? OUTGOING_SMS_LIMIT : INCOMING_SMS_LIMIT;
        if (context2 != null) {
            sharedPreferences = context2.getSharedPreferences(policyName, 0);
        }
        if (sharedPreferences != null) {
            int usedNumOfDay2 = sharedPreferences.getInt(USED_OF_DAY, 0);
            int usedNumOfWeek2 = sharedPreferences.getInt(USED_OF_WEEK, 0);
            int usedNumOfMonth = sharedPreferences.getInt(USED_OF_MONTH, 0);
            int limitNumOfDay = getLimitNum(z, DAY_MODE);
            int limitNumOfWeek = getLimitNum(z, WEEK_MODE);
            int limitNumOfMonth = getLimitNum(z, MONTH_MODE);
            long nowTime = System.currentTimeMillis();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (editor == null) {
                Rlog.e(LOG_TAG, "editor is null");
                return false;
            }
            if (isMonthMode(z)) {
                usedNumOfDay = usedNumOfDay2;
                usedNumOfWeek = usedNumOfWeek2;
                if (!isSameMonth(nowTime, sharedPreferences.getLong(MONTH_MODE_TIME, getStartTime(z, MONTH_MODE)))) {
                    Rlog.d(LOG_TAG, "it is next month, so reset data");
                    editor.putInt(USED_OF_MONTH, 0);
                    editor.putLong(MONTH_MODE_TIME, nowTime);
                } else if (usedNumOfMonth >= limitNumOfMonth) {
                    return true;
                }
            } else {
                usedNumOfDay = usedNumOfDay2;
                usedNumOfWeek = usedNumOfWeek2;
            }
            if (!isWeekMode(z)) {
            } else if (!isSameWeek(nowTime, sharedPreferences.getLong(WEEK_MODE_TIME, getStartTime(z, WEEK_MODE)))) {
                Rlog.d(LOG_TAG, "it is next week, so reset data");
                editor.putInt(USED_OF_WEEK, 0);
                editor.putLong(WEEK_MODE_TIME, nowTime);
                int i = usedNumOfWeek;
            } else if (usedNumOfWeek >= limitNumOfWeek) {
                return true;
            }
            if (isDayMode(z)) {
                int i2 = usedNumOfMonth;
                if (!isSameDay(nowTime, sharedPreferences.getLong(DAY_MODE_TIME, getStartTime(z, DAY_MODE)))) {
                    Rlog.d(LOG_TAG, "it is next day, so reset data");
                    editor.putInt(USED_OF_DAY, 0);
                    editor.putLong(DAY_MODE_TIME, nowTime);
                } else if (usedNumOfDay >= limitNumOfDay) {
                    return true;
                }
            }
        }
        return false;
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
        switch (c) {
            case 0:
                if (isOutgoing) {
                    policyName = OUTGOING_DAY_LIMIT;
                } else {
                    policyName = INCOMING_DAY_LIMIT;
                }
                return Long.parseLong(this.mDpm.getPolicy(null, policyName).getString(DAY_MODE_TIME, INIT_VALUE));
            case 1:
                if (isOutgoing) {
                    policyName2 = OUTGOING_WEEK_LIMIT;
                } else {
                    policyName2 = INCOMING_WEEK_LIMIT;
                }
                return Long.parseLong(this.mDpm.getPolicy(null, policyName2).getString(WEEK_MODE_TIME, INIT_VALUE));
            case 2:
                if (isOutgoing) {
                    policyName3 = OUTGOING_MONTH_LIMIT;
                } else {
                    policyName3 = INCOMING_MONTH_LIMIT;
                }
                return Long.parseLong(this.mDpm.getPolicy(null, policyName3).getString(MONTH_MODE_TIME, INIT_VALUE));
            default:
                return PARTIAL_SEGMENT_EXPIRE_TIME;
        }
    }

    private int getLimitNum(boolean isOutgoing, String timeMode) {
        String policyName;
        String policyName2;
        String policyName3;
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
        switch (c) {
            case 0:
                if (isOutgoing) {
                    policyName = OUTGOING_DAY_LIMIT;
                } else {
                    policyName = INCOMING_DAY_LIMIT;
                }
                Bundle bundleDay = this.mDpm.getPolicy(null, policyName);
                if (bundleDay != null) {
                    return Integer.parseInt(bundleDay.getString(LIMIT_OF_DAY, INIT_VALUE));
                }
                return 0;
            case 1:
                if (isOutgoing) {
                    policyName2 = OUTGOING_WEEK_LIMIT;
                } else {
                    policyName2 = INCOMING_WEEK_LIMIT;
                }
                Bundle bundleWeek = this.mDpm.getPolicy(null, policyName2);
                if (bundleWeek != null) {
                    return Integer.parseInt(bundleWeek.getString(LIMIT_OF_WEEK, INIT_VALUE));
                }
                return 0;
            case 2:
                if (isOutgoing) {
                    policyName3 = OUTGOING_MONTH_LIMIT;
                } else {
                    policyName3 = INCOMING_MONTH_LIMIT;
                }
                Bundle bundleMonth = this.mDpm.getPolicy(null, policyName3);
                if (bundleMonth != null) {
                    return Integer.parseInt(bundleMonth.getString(LIMIT_OF_MONTH, INIT_VALUE));
                }
                return 0;
            default:
                return 0;
        }
    }

    private boolean isDayMode(boolean isOutgoing) {
        Bundle bundle;
        if (isOutgoing) {
            bundle = this.mDpm.getPolicy(null, OUTGOING_DAY_LIMIT);
        } else {
            bundle = this.mDpm.getPolicy(null, INCOMING_DAY_LIMIT);
        }
        if (bundle != null) {
            return Boolean.parseBoolean(bundle.getString(DAY_MODE, MODE_INIT_VALUE));
        }
        return false;
    }

    private boolean isWeekMode(boolean isOutgoing) {
        Bundle bundle;
        if (isOutgoing) {
            bundle = this.mDpm.getPolicy(null, OUTGOING_WEEK_LIMIT);
        } else {
            bundle = this.mDpm.getPolicy(null, INCOMING_WEEK_LIMIT);
        }
        if (bundle != null) {
            return Boolean.parseBoolean(bundle.getString(WEEK_MODE, MODE_INIT_VALUE));
        }
        return false;
    }

    private boolean isMonthMode(boolean isOutgoing) {
        Bundle bundle;
        if (isOutgoing) {
            bundle = this.mDpm.getPolicy(null, OUTGOING_MONTH_LIMIT);
        } else {
            bundle = this.mDpm.getPolicy(null, INCOMING_MONTH_LIMIT);
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
        boolean isSameWeek = false;
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(2);
        calendar.setTimeInMillis(nowTime);
        int nowWeek = calendar.get(3);
        int nowYear = calendar.get(1);
        calendar.setTimeInMillis(sharedTime);
        int sharedWeek = calendar.get(3);
        int subYear = nowYear - calendar.get(1);
        if (subYear == 0 && nowWeek == sharedWeek) {
            isSameWeek = true;
        } else if (subYear == 1 && calendar.get(2) == 11 && sharedWeek == nowWeek) {
            isSameWeek = true;
        }
        return isSameWeek;
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
        if (1 == this.mAlreadySentSms.get()) {
            this.mHwHandler.removeMessages(20);
            this.mHwHandler.sendMessageDelayed(this.mHwHandler.obtainMessage(20, smsDispatcher), 3600000);
        }
    }

    /* access modifiers changed from: private */
    public void handleSendSmsOverLoad(SMSDispatcher smsDispatcher) {
        if (this.mAlreadySentSms.get() > 50) {
            StringBuffer buf = new StringBuffer();
            buf.append(getDefaultSmsApplicationName());
            buf.append(" SendSmsOverLoad");
            int sendSmsCount = smsDispatcher.getPackageSendSmsCount().size();
            boolean findThirdApp = false;
            for (int i = 0; i < sendSmsCount; i++) {
                String packageName = (String) smsDispatcher.getPackageSendSmsCount().valueAt(i);
                if (!getDefaultSmsApplicationName().equals(packageName) && !"com.android.mms".equals(packageName)) {
                    buf.append(" ");
                    buf.append(packageName);
                    findThirdApp = true;
                }
            }
            if (findThirdApp) {
                HwRadarUtils.report(PhoneFactory.getDefaultPhone() != null ? PhoneFactory.getDefaultPhone().getContext() : null, HwRadarUtils.ERR_SMS_SEND_BACKGROUND, buf.toString(), smsDispatcher.getSubId());
            }
        }
        this.mAlreadySentSms.set(0);
        smsDispatcher.clearPackageSendSmsCount();
    }

    /* access modifiers changed from: private */
    public String getDefaultSmsApplicationName() {
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
        Rlog.d(LOG_TAG, "isSentSmsFromRejectCall");
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0077, code lost:
        return false;
     */
    public boolean hasSameSmsPdu(byte[] pdu) {
        Rlog.d(LOG_TAG, "check if there is a same pdu in mSmsList.");
        Phone phone = PhoneFactory.getDefaultPhone();
        if (phone != null) {
            SmsStorageMonitor monitor = phone.mSmsStorageMonitor;
            if (monitor != null && !monitor.isStorageAvailable()) {
                Rlog.d(LOG_TAG, "storage is full, not to save pdu and not intercept!");
                return false;
            }
        }
        synchronized (this.mSmsList) {
            for (byte[] oldPdu : this.mSmsList) {
                if (Arrays.equals(pdu, oldPdu)) {
                    return true;
                }
            }
            this.mSmsList.add(pdu);
            Rlog.d(LOG_TAG, "mSmsList.size() = " + this.mSmsList.size());
            if (this.mSmsList.size() > 30) {
                Rlog.d(LOG_TAG, "mSmsList.size() > MAX_SMS_LIST_DEFAULT");
                this.mSmsList.remove(0);
            }
        }
    }

    public CharSequence getAppLabel(Context context, String appPackage, int userId, SMSDispatcher smsDispatcher) {
        if ("com.android.contacts".equals(appPackage)) {
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
        String address = tracker.getAddress();
        String refNumber = Integer.toString(tracker.getReferenceNumber());
        String count = Integer.toString(tracker.getMessageCount());
        int delCount = 0;
        try {
            delCount = resolver.delete(sRawUri, new StringBuilder("date < " + (tracker.getTimestamp() - PARTIAL_SEGMENT_EXPIRE_TIME) + " AND " + SELECT_BY_REFERENCE).toString(), new String[]{address, refNumber, count});
        } catch (SQLException e) {
            Rlog.e(LOG_TAG, "scanAndDeleteOlderPartialMessages got SQLException");
        } catch (Exception e2) {
            Rlog.e(LOG_TAG, "scanAndDeleteOlderPartialMessages got exception");
        }
        if (delCount > 0) {
            Rlog.d(LOG_TAG, "scanAndDeleteOlderPartialMessages: delete " + delCount + " raw sms older than " + SystemProperties.getInt("ro.config.hw_drop_oldsms_day", 3) + " days");
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

    public void filterMyNumber(Context context, boolean groupMmsEnabled, HashSet<String> recipients, HashMap<Integer, EncodedStringValue[]> addressMap, int subId) {
        HashSet<String> hashSet = recipients;
        HashMap<Integer, EncodedStringValue[]> hashMap = addressMap;
        int i = subId;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        ContentResolver contentResolver = context.getContentResolver();
        if (groupMmsEnabled && recipients.size() != 1 && recipients.size() <= 2) {
            String myNumber = telephonyManager.getLine1Number(i);
            if (TextUtils.isEmpty(myNumber)) {
                myNumber = Settings.Secure.getString(contentResolver, "localNumberFromDb_" + i);
            }
            if (TextUtils.isEmpty(myNumber)) {
                EncodedStringValue[] array_to = hashMap.get(151);
                EncodedStringValue[] array_from = hashMap.get(137);
                if (array_to != null && array_from != null) {
                    String number_from = "";
                    int length = array_from.length;
                    int i2 = 0;
                    int i3 = 0;
                    while (true) {
                        if (i3 >= length) {
                            break;
                        }
                        EncodedStringValue v = array_from[i3];
                        if (v != null && !TextUtils.isEmpty(v.getString())) {
                            number_from = v.getString();
                            break;
                        }
                        i3++;
                    }
                    int length2 = array_to.length;
                    while (true) {
                        if (i2 >= length2) {
                            break;
                        }
                        EncodedStringValue v2 = array_to[i2];
                        if (v2 != null && !number_from.equals(v2.getString()) && hashSet.contains(v2.getString())) {
                            hashSet.remove(v2.getString());
                            break;
                        }
                        i2++;
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
        if (4 < info.length && info[4] != null) {
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
                Rlog.d(LOG_TAG, "newFromParcel ton == SmsAddress.TON_INTERNATIONAL");
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

    /* JADX WARNING: type inference failed for: r0v1, types: [byte[][], java.io.Serializable] */
    private void dispatchCTAutoRegSmsPdus(Context context, com.android.internal.telephony.cdma.SmsMessage sms, int subId, Handler handler) {
        ? r0 = {sms.getUserData()};
        Intent intent = new Intent("android.provider.Telephony.CT_AUTO_REG_RECV_CONFIRM_ACK");
        intent.putExtra("pdus", r0);
        intent.putExtra("CdmaSubscription", subId);
        intent.addFlags(134217728);
        context.sendOrderedBroadcast(intent, "android.permission.RECEIVE_SMS", null, handler, -1, null, null);
        Rlog.d(LOG_TAG, "dispatchCTAutoRegSmsPdus end. Broadcast send to apk!");
    }

    public int handleExtendTeleService(int teleService, CdmaSMSDispatcher smsDispatcher, com.android.internal.telephony.cdma.SmsMessage sms) {
        if (teleService != 65002) {
            if (teleService != 65005) {
                return 4;
            }
            if ((92 == SystemProperties.getInt("ro.config.hw_opta", 0) || 999 == SystemProperties.getInt("ro.config.hw_opta", 0)) && 156 == SystemProperties.getInt("ro.config.hw_optb", 0)) {
                Rlog.d(LOG_TAG, "CT's AutoRegSms notification!");
                dispatchCTAutoRegSmsPdus(smsDispatcher.mContext, sms, smsDispatcher.getSubId(), smsDispatcher);
                return 1;
            }
        }
        return 0;
    }
}

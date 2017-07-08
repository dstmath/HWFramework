package com.android.internal.telephony;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.provider.SettingsEx.Systemex;
import android.provider.Telephony.Sms.Intents;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.SmsMessage.SubmitPdu;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.ISms.Stub;
import com.android.internal.telephony.SmsMessageBase.SubmitPduBase;
import com.android.internal.telephony.cdma.CdmaSMSDispatcher;
import com.android.internal.telephony.cdma.HwCdmaSMSDispatcher;
import com.android.internal.telephony.cdma.HwSmsMessage;
import com.android.internal.telephony.cdma.sms.BearerData;
import com.android.internal.telephony.cdma.sms.HwBearerData;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.gsm.GsmInboundSmsHandler;
import com.android.internal.telephony.gsm.GsmSMSDispatcher;
import com.android.internal.telephony.gsm.HwGsmSMSDispatcher;
import com.android.internal.telephony.gsm.SmsMessage;
import com.android.internal.telephony.gsm.SmsMessage.PduParser;
import com.android.internal.telephony.gsm.SmsMessageUtils;
import com.android.internal.util.BitwiseOutputStream;
import com.android.internal.util.BitwiseOutputStream.AccessException;
import com.huawei.utils.reflect.HwReflectUtils;
import huawei.android.telephony.wrapper.WrapperFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class HwInnerSmsManagerImpl implements HwInnerSmsManager {
    private static final String BLOCK_TYPE = "BLOCKED_TYPE";
    private static final String BLUETOOTH_PACKAGE_NAME = "com.android.bluetooth";
    private static final Class<?> CLASS_ContentType = null;
    private static final Class<?> CLASS_EncodedStringValue = null;
    private static final Class<?> CLASS_GenericPdu = null;
    private static final Class<?> CLASS_PduHeaders = null;
    private static final Class<?> CLASS_PduParser = null;
    private static final String COLUMN_IS_PRIVATE = "is_private";
    private static final Constructor<?> CONSTRUCTOR_PduParser = null;
    private static final String CONTACT_PACKAGE_NAME = "com.android.contacts";
    private static final Field FIELD_MESSAGE_TYPE_NOTIFICATION_IND = null;
    private static final Field FIELD_MMS_MESSAGE = null;
    private static final String HANDLE_KEY_SMSINTENT = "HANDLE_SMS_INTENT";
    private static final String HANDLE_KEY_WAP_PUSH_INTENT = "HANDLE_WAP_PUSH_INTENT";
    private static final boolean IS_CHINATELECOM = false;
    private static final String LOG_TAG = "HwInnerSmsManagerImpl";
    private static final int MESSAGE_TYPE_NOTIFICATION_IND = 0;
    private static final Method METHOD_getFrom = null;
    private static final Method METHOD_getMessageType = null;
    private static final Method METHOD_getString = null;
    private static final Method METHOD_parse = null;
    private static final String MMS_ACTIVITY_NAME = "com.android.mms.ui.ComposeMessageActivity";
    private static final String MMS_MESSAGE = null;
    private static final String MMS_PACKAGE_NAME = "com.android.mms";
    static int Message_Reference_Num = 0;
    public static final int SMS_GW_VP_ABSOLUTE_FORMAT = 24;
    public static final int SMS_GW_VP_ENHANCED_FORMAT = 8;
    public static final int SMS_GW_VP_RELATIVE_FORMAT = 16;
    private static int SUBSCRIPTION_FOR_NONE_VARIABLE_METHODS;
    private static final Uri URI_PRIVATE_NUMBER = null;
    private static SmsMessageUtils gsmSmsMessageUtils;
    private static HwInnerSmsManager mInstance;
    private static final Uri sRawUri = null;
    private KeyguardManager mKeyguardManager;
    private SmsInterceptionService mSmsInterceptionService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwInnerSmsManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwInnerSmsManagerImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwInnerSmsManagerImpl.<clinit>():void");
    }

    public HwInnerSmsManagerImpl() {
        this.mSmsInterceptionService = null;
    }

    public static HwInnerSmsManager getDefault() {
        return mInstance;
    }

    public byte[] getNewbyte() {
        if (2 == TelephonyManager.getDefault().getPhoneType()) {
            return new byte[254];
        }
        return new byte[175];
    }

    public String getSmscAddr() {
        String smscAddr = null;
        try {
            ISms simISms = Stub.asInterface(ServiceManager.getService("isms"));
            if (simISms != null) {
                smscAddr = simISms.getSmscAddr();
            }
        } catch (RemoteException e) {
        }
        return smscAddr;
    }

    public boolean setSmscAddr(String smscAddr) {
        boolean ret = IS_CHINATELECOM;
        try {
            ISms simISms = Stub.asInterface(ServiceManager.getService("isms"));
            if (simISms != null) {
                ret = simISms.setSmscAddr(smscAddr);
            }
        } catch (RemoteException e) {
        }
        return ret;
    }

    public String getSmscAddr(long subId) {
        String smscAddr = null;
        try {
            ISms simISms = Stub.asInterface(ServiceManager.getService("isms"));
            if (simISms != null) {
                smscAddr = simISms.getSmscAddrForSubscriber(subId);
            }
        } catch (RemoteException e) {
        }
        return smscAddr;
    }

    public boolean setSmscAddr(long subId, String smscAddr) {
        boolean ret = IS_CHINATELECOM;
        try {
            ISms simISms = Stub.asInterface(ServiceManager.getService("isms"));
            if (simISms != null) {
                ret = simISms.setSmscAddrForSubscriber(subId, smscAddr);
            }
        } catch (RemoteException e) {
        }
        return ret;
    }

    public ArrayList<String> fragmentForEmptyText() {
        ArrayList<String> result = new ArrayList(1);
        result.add("");
        return result;
    }

    private static void setMessageReferenceNum(int value) {
        Message_Reference_Num = value;
    }

    public int getMessageRefrenceNumber() {
        if (HwSubscriptionManager.SUB_INIT_STATE <= Message_Reference_Num) {
            setMessageReferenceNum(MESSAGE_TYPE_NOTIFICATION_IND);
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
        UDH[MESSAGE_TYPE_NOTIFICATION_IND] = (byte) 0;
        UDH[1] = (byte) 3;
        UDH[2] = (byte) MessageReferenceNum;
        UDH[3] = (byte) maxNum;
        UDH[4] = (byte) seqNum;
        Rlog.d("android/SmsMessage", "maxNum:" + maxNum + ";seqNum:" + seqNum + ";MR:" + UDH[2]);
        return UDH;
    }

    public SubmitPdu getSubmitPdu(String scAddress, String timeStamps, String destinationAddress, String message, byte[] UDH) {
        return getSubmitPdu(scAddress, timeStamps, destinationAddress, message, UDH, SUBSCRIPTION_FOR_NONE_VARIABLE_METHODS);
    }

    public SubmitPdu getSubmitPdu(String scAddress, String timeStamps, String destinationAddress, String message, byte[] UDH, int subscription) {
        int activePhone;
        SubmitPduBase spb;
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
            spb = SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, IS_CHINATELECOM, UDH);
        }
        if (spb == null) {
            return null;
        }
        return new SubmitPdu(spb);
    }

    public SubmitPdu getDeliverPdu(String scAddress, String scTimeStamp, String origAddress, String message, byte[] UDH) {
        return getDeliverPdu(scAddress, scTimeStamp, origAddress, message, UDH, SUBSCRIPTION_FOR_NONE_VARIABLE_METHODS);
    }

    public SubmitPdu getDeliverPdu(String scAddress, String scTimeStamp, String origAddress, String message, byte[] UDH, int subscription) {
        int activePhone;
        SubmitPduBase spb;
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
            spb = HwSmsMessage.getSubmitDeliverPdu(IS_CHINATELECOM, scTimeStamp, origAddress, message, header);
        } else {
            spb = com.android.internal.telephony.gsm.HwSmsMessage.getDeliverPdu(scAddress, scTimeStamp, origAddress, message, UDH);
        }
        if (spb == null) {
            return null;
        }
        return new SubmitPdu(spb);
    }

    public android.telephony.SmsMessage createFromEfRecord(int index, byte[] data, int subscription) {
        SmsMessageBase wrappedMessage;
        if (WrapperFactory.getMSimTelephonyManagerWrapper().getCurrentPhoneType(subscription) == 2) {
            wrappedMessage = com.android.internal.telephony.cdma.SmsMessage.createFromEfRecord(index, data);
        } else {
            wrappedMessage = SmsMessage.createFromEfRecord(index, data);
        }
        Rlog.e(LOG_TAG, "createFromEfRecord(): wrappedMessage=" + wrappedMessage);
        if (wrappedMessage != null) {
            return new android.telephony.SmsMessage(wrappedMessage);
        }
        return null;
    }

    public boolean parseGsmSmsSubmit(SmsMessage smsMessage, int mti, Object parcel, int firstByte) {
        PduParser p = (PduParser) parcel;
        if (1 != mti) {
            return IS_CHINATELECOM;
        }
        p.getByte();
        SmsMessageBaseUtils.setOriginatingAddress(smsMessage, p.getAddress());
        gsmSmsMessageUtils.setProtocolIdentifier(smsMessage, p.getByte());
        gsmSmsMessageUtils.setDataCodingScheme(smsMessage, p.getByte());
        p.mCur += getValidityPeriod(firstByte);
        gsmSmsMessageUtils.parseUserData(smsMessage, p, (firstByte & 64) == 64 ? true : IS_CHINATELECOM);
        return true;
    }

    private int getValidityPeriod(int firstByte) {
        switch (firstByte & SMS_GW_VP_ABSOLUTE_FORMAT) {
            case SMS_GW_VP_ENHANCED_FORMAT /*8*/:
            case SMS_GW_VP_ABSOLUTE_FORMAT /*24*/:
                return 7;
            case SMS_GW_VP_RELATIVE_FORMAT /*16*/:
                return 1;
            default:
                Rlog.e("PduParser", "unsupported validity format.");
                return MESSAGE_TYPE_NOTIFICATION_IND;
        }
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

    public GsmSMSDispatcher createHwGsmSMSDispatcher(Phone phone, SmsStorageMonitor storageMonitor, SmsUsageMonitor usageMonitor, ImsSMSDispatcher imsSMSDispatcher, GsmInboundSmsHandler gsmInboundSmsHandler) {
        return new HwGsmSMSDispatcher(phone, storageMonitor, usageMonitor, imsSMSDispatcher, gsmInboundSmsHandler);
    }

    public CdmaSMSDispatcher createHwCdmaSMSDispatcher(Phone phone, SmsStorageMonitor storageMonitor, SmsUsageMonitor usageMonitor, ImsSMSDispatcher imsSMSDispatcher) {
        return new HwCdmaSMSDispatcher(phone, storageMonitor, usageMonitor, imsSMSDispatcher);
    }

    public boolean encode7bitMultiSms(UserData uData, byte[] udhData, boolean force) {
        return HwBearerData.encode7bitMultiSms(uData, udhData, force);
    }

    public void encodeMsgCenterTimeStampCheck(BearerData bData, BitwiseOutputStream outStream) throws AccessException {
        HwBearerData.encodeMsgCenterTimeStampCheck(bData, outStream);
    }

    public TextEncodingDetails calcTextEncodingDetailsEx(CharSequence msg, boolean force7BitEncoding) {
        return HwBearerData.calcTextEncodingDetailsEx(msg, force7BitEncoding);
    }

    public boolean shouldSendReceivedActionInPrivacyMode(InboundSmsHandler handler, Context context, Intent intent, String deleteWhere, String[] deleteWhereArgs) {
        this.mKeyguardManager = (KeyguardManager) context.getSystemService("keyguard");
        String action = intent.getAction();
        String number = null;
        if (!SystemProperties.getBoolean("ro.config.hw_privacymode", IS_CHINATELECOM) || 1 != Secure.getInt(context.getContentResolver(), "privacy_mode_on", MESSAGE_TYPE_NOTIFICATION_IND) || (1 != Secure.getInt(context.getContentResolver(), "privacy_mode_state", MESSAGE_TYPE_NOTIFICATION_IND) && (this.mKeyguardManager == null || !this.mKeyguardManager.isKeyguardLocked()))) {
            return true;
        }
        if ("android.provider.Telephony.SMS_DELIVER".equals(action)) {
            number = Intents.getMessagesFromIntent(intent)[MESSAGE_TYPE_NOTIFICATION_IND].getOriginatingAddress();
        } else if ("android.provider.Telephony.WAP_PUSH_DELIVER".equals(action) && MMS_MESSAGE.equals(intent.getType())) {
            try {
                byte[] pushData = intent.getByteArrayExtra("data");
                Object pdu = HwReflectUtils.invoke(HwReflectUtils.newInstance(CONSTRUCTOR_PduParser, new Object[]{pushData}), METHOD_parse, new Object[MESSAGE_TYPE_NOTIFICATION_IND]);
                if (pdu != null && MESSAGE_TYPE_NOTIFICATION_IND == ((Integer) HwReflectUtils.invoke(pdu, METHOD_getMessageType, new Object[MESSAGE_TYPE_NOTIFICATION_IND])).intValue()) {
                    Object fromValue = HwReflectUtils.invoke(pdu, METHOD_getFrom, new Object[MESSAGE_TYPE_NOTIFICATION_IND]);
                    if (fromValue != null) {
                        number = (String) HwReflectUtils.invoke(fromValue, METHOD_getString, new Object[MESSAGE_TYPE_NOTIFICATION_IND]);
                    }
                }
            } catch (Exception ex) {
                Rlog.e(LOG_TAG, "get mms original number cause exception!" + ex.toString());
            }
        } else {
            Rlog.d(LOG_TAG, "ignore action, do nothing");
            return true;
        }
        if (number == null || !isPrivacyNumber(context, number)) {
            return true;
        }
        Rlog.d(LOG_TAG, "sms originating address: xxxxxx is private, do not send received action.");
        deleteFromRawTable(context, deleteWhere, deleteWhereArgs);
        handler.sendMessage(3);
        return IS_CHINATELECOM;
    }

    private boolean isPrivacyNumber(Context context, String number) {
        Uri queryUri = URI_PRIVATE_NUMBER.buildUpon().appendEncodedPath(number).build();
        Cursor cursor = null;
        boolean isPrivate = IS_CHINATELECOM;
        try {
            cursor = context.getContentResolver().query(queryUri, new String[]{COLUMN_IS_PRIVATE}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                isPrivate = cursor.getInt(MESSAGE_TYPE_NOTIFICATION_IND) == 1 ? true : IS_CHINATELECOM;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception ex) {
            Rlog.e(LOG_TAG, "isPrivacyNumber cause exception!" + ex.toString());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        Rlog.d(LOG_TAG, "The number is private or not: " + (isPrivate ? "Yes" : "No"));
        return isPrivate;
    }

    private void deleteFromRawTable(Context context, String deleteWhere, String[] deleteWhereArgs) {
        int rows = context.getContentResolver().delete(sRawUri, deleteWhere, deleteWhereArgs);
        if (rows == 0) {
            Rlog.e(LOG_TAG, "No rows were deleted from raw table!");
        } else {
            Rlog.d(LOG_TAG, "Deleted " + rows + " rows from raw table.");
        }
    }

    public boolean shouldSetDefaultApplicationForPackage(String packageName, Context context) {
        String hwMmsPackageName = getHwMmsPackageName(context);
        Rlog.d(LOG_TAG, "current packageName: " + packageName + ", hwMmsPackageName: " + hwMmsPackageName);
        return (!SystemProperties.getBoolean("ro.config.hw_privacymode", IS_CHINATELECOM) || 1 != Secure.getInt(context.getContentResolver(), "privacy_mode_on", MESSAGE_TYPE_NOTIFICATION_IND) || 1 != Secure.getInt(context.getContentResolver(), "privacy_mode_state", MESSAGE_TYPE_NOTIFICATION_IND) || packageName == null || hwMmsPackageName == null || packageName.equals(hwMmsPackageName)) ? true : IS_CHINATELECOM;
    }

    private static String getHwMmsPackageName(Context context) {
        Intent intent = new Intent();
        intent.setClassName(CONTACT_PACKAGE_NAME, MMS_ACTIVITY_NAME);
        if (context.getPackageManager().resolveActivity(intent, MESSAGE_TYPE_NOTIFICATION_IND) == null) {
            return MMS_PACKAGE_NAME;
        }
        return CONTACT_PACKAGE_NAME;
    }

    public String getUserDataGSM8Bit(PduParser p, int septetCount) {
        return com.android.internal.telephony.gsm.HwSmsMessage.getUserDataGSM8Bit(p, septetCount);
    }

    public void parseRUIMPdu(com.android.internal.telephony.cdma.SmsMessage msg, byte[] pdu) {
        HwSmsMessage.parseRUIMPdu(msg, pdu);
    }

    public boolean useCdmaFormatForMoSms() {
        return android.telephony.SmsMessageUtils.useCdmaFormatForMoSms(SUBSCRIPTION_FOR_NONE_VARIABLE_METHODS);
    }

    public ArrayList<String> fragmentText(String text) {
        return android.telephony.SmsMessage.fragmentText(text, SUBSCRIPTION_FOR_NONE_VARIABLE_METHODS);
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
        return IS_CHINATELECOM;
    }

    public void sendGoogleSmsBlockedRecord(Intent intent) {
        try {
            Bundle smsInfo = new Bundle();
            if (intent.getBooleanExtra("isWapPush", IS_CHINATELECOM)) {
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

    private String getAppNameByPid(int pid, Context context) {
        String processName = "";
        for (RunningAppProcessInfo appInfo : ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses()) {
            if (pid == appInfo.pid) {
                Rlog.d(LOG_TAG, "pid: " + appInfo.pid + " processName: " + appInfo.processName);
                return appInfo.processName;
            }
        }
        return processName;
    }

    public PackageInfo getPackageInfoByPid(PackageInfo appInfo, PackageManager pm, String[] packageNames, Context context) {
        if (packageNames != null && packageNames.length > 1) {
            try {
                String packageNameByPid = getAppNameByPid(Binder.getCallingPid(), context);
                if (!"".equals(packageNameByPid)) {
                    appInfo = pm.getPackageInfo(packageNameByPid, 64);
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return appInfo;
    }

    public void doubleSmsStatusCheck(com.android.internal.telephony.cdma.SmsMessage msg) {
        HwSmsMessage.doubleSmsStatusCheck(msg);
    }

    public int getCdmaSub() {
        return HwSmsMessage.getCdmaSub();
    }

    public boolean checkShouldWriteSmsPackage(String packageName, Context context) {
        int i = MESSAGE_TYPE_NOTIFICATION_IND;
        boolean ret = IS_CHINATELECOM;
        if (packageName == null || "".equals(packageName)) {
            Rlog.e(LOG_TAG, "checkShouldWriteSmsPackage packageName not exist");
            return IS_CHINATELECOM;
        }
        String defaultSmsPackage = null;
        ComponentName component = SmsApplication.getDefaultSmsApplication(context, IS_CHINATELECOM);
        if (component != null) {
            defaultSmsPackage = component.getPackageName();
        }
        Rlog.d(LOG_TAG, "checkShouldWriteSmsPackage defaultSmsPackage: " + defaultSmsPackage + ", current package: " + packageName);
        if ((defaultSmsPackage != null && defaultSmsPackage.equals(packageName)) || packageName.equals(BLUETOOTH_PACKAGE_NAME)) {
            return IS_CHINATELECOM;
        }
        String str = null;
        try {
            str = Systemex.getString(context.getContentResolver(), "should_write_sms_package");
            Rlog.d(LOG_TAG, "checkShouldWriteSmsPackage cust: " + str);
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "Exception when got name value", e);
        }
        if (str == null || "".equals(str)) {
            str = "com.huawei.vassistant,com.hellotext.hello";
        }
        String[] shouldWriteSmsPackageArray = str.split(",");
        int length = shouldWriteSmsPackageArray.length;
        while (i < length) {
            if (packageName.equalsIgnoreCase(shouldWriteSmsPackageArray[i])) {
                ret = true;
                break;
            }
            i++;
        }
        return ret;
    }

    public boolean currentSubIsChinaTelecomSim(int phoneId) {
        boolean isCTSimCard;
        if (HwTelephonyManagerInner.getDefault().isFullNetworkSupported()) {
            isCTSimCard = HwTelephonyManagerInner.getDefault().isCTSimCard(phoneId);
        } else {
            isCTSimCard = IS_CHINATELECOM;
        }
        return !IS_CHINATELECOM ? isCTSimCard : true;
    }

    public boolean allowToSetSmsWritePermission(String packageName) {
        if (CONTACT_PACKAGE_NAME.equalsIgnoreCase(packageName) || MMS_PACKAGE_NAME.equalsIgnoreCase(packageName)) {
            return true;
        }
        return IS_CHINATELECOM;
    }
}

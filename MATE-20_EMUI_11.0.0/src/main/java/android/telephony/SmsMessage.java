package android.telephony;

import android.annotation.UnsupportedAppUsage;
import android.content.res.Resources;
import android.os.Binder;
import android.text.TextUtils;
import com.android.internal.R;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.Sms7BitEncodingTranslator;
import com.android.internal.telephony.SmsConstants;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;

public class SmsMessage {
    public static final int ENCODING_16BIT = 3;
    public static final int ENCODING_7BIT = 1;
    public static final int ENCODING_8BIT = 2;
    public static final int ENCODING_KSC5601 = 4;
    public static final int ENCODING_UNKNOWN = 0;
    public static final String FORMAT_3GPP = "3gpp";
    public static final String FORMAT_3GPP2 = "3gpp2";
    private static final String LOG_TAG = "SmsMessage";
    public static final int MAX_USER_DATA_BYTES = 140;
    public static final int MAX_USER_DATA_BYTES_WITH_HEADER = 134;
    public static final int MAX_USER_DATA_SEPTETS = 160;
    public static final int MAX_USER_DATA_SEPTETS_WITH_HEADER = 153;
    private static final int SUBSCRIPTION_FOR_NONE_VARIABLE_METHODS = -1;
    private static boolean mIsNoEmsSupportConfigListLoaded = false;
    private static NoEmsSupportConfig[] mNoEmsSupportConfigList = null;
    @UnsupportedAppUsage
    private int mSubId = 0;
    @UnsupportedAppUsage
    public SmsMessageBase mWrappedSmsMessage;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Format {
    }

    public enum MessageClass {
        UNKNOWN,
        CLASS_0,
        CLASS_1,
        CLASS_2,
        CLASS_3
    }

    @UnsupportedAppUsage
    public void setSubId(int subId) {
        this.mSubId = subId;
    }

    @UnsupportedAppUsage
    public int getSubId() {
        return this.mSubId;
    }

    public static class SubmitPdu {
        public byte[] encodedMessage;
        public byte[] encodedScAddress;

        public String toString() {
            return "SubmitPdu: encodedScAddress = " + Arrays.toString(this.encodedScAddress) + ", encodedMessage = " + Arrays.toString(this.encodedMessage);
        }

        public SubmitPdu(SmsMessageBase.SubmitPduBase spb) {
            this.encodedMessage = spb.encodedMessage;
            this.encodedScAddress = spb.encodedScAddress;
        }
    }

    public SmsMessage(SmsMessageBase smb) {
        this.mWrappedSmsMessage = smb;
    }

    @Deprecated
    public static SmsMessage createFromPdu(byte[] pdu) {
        return createFromPdu(pdu, 2 == TelephonyManager.getDefault().getCurrentPhoneType() ? "3gpp2" : "3gpp");
    }

    public static SmsMessage createFromPdu(byte[] pdu, String format) {
        return createFromPdu(pdu, format, true);
    }

    private static SmsMessage createFromPdu(byte[] pdu, String format, boolean fallbackToOtherFormat) {
        SmsMessageBase wrappedMessage;
        if (pdu == null) {
            Rlog.i(LOG_TAG, "createFromPdu(): pdu is null");
            return null;
        }
        String otherFormat = "3gpp2".equals(format) ? "3gpp" : "3gpp2";
        if ("3gpp2".equals(format)) {
            wrappedMessage = com.android.internal.telephony.cdma.SmsMessage.createFromPdu(pdu);
        } else if ("3gpp".equals(format)) {
            wrappedMessage = com.android.internal.telephony.gsm.SmsMessage.createFromPdu(pdu);
        } else {
            Rlog.e(LOG_TAG, "createFromPdu(): unsupported message format " + format);
            return null;
        }
        if (wrappedMessage != null) {
            return new SmsMessage(wrappedMessage);
        }
        if (fallbackToOtherFormat) {
            return createFromPdu(pdu, otherFormat, false);
        }
        Rlog.e(LOG_TAG, "createFromPdu(): wrappedMessage is null");
        return null;
    }

    public static SmsMessage newFromCMT(byte[] pdu) {
        SmsMessageBase wrappedMessage = com.android.internal.telephony.gsm.SmsMessage.newFromCMT(pdu);
        if (wrappedMessage != null) {
            return new SmsMessage(wrappedMessage);
        }
        Rlog.e(LOG_TAG, "newFromCMT(): wrappedMessage is null");
        return null;
    }

    public static SmsMessage createFromEfRecord(int index, byte[] data) {
        SmsMessageBase wrappedMessage;
        if (isCdmaVoice()) {
            wrappedMessage = com.android.internal.telephony.cdma.SmsMessage.createFromEfRecord(index, data);
        } else {
            wrappedMessage = com.android.internal.telephony.gsm.SmsMessage.createFromEfRecord(index, data);
        }
        if (wrappedMessage != null) {
            return new SmsMessage(wrappedMessage);
        }
        Rlog.e(LOG_TAG, "createFromEfRecord(): wrappedMessage is null");
        return null;
    }

    public static SmsMessage createFromEfRecord(int index, byte[] data, int subId) {
        SmsMessageBase wrappedMessage;
        if (isCdmaVoice(subId)) {
            wrappedMessage = com.android.internal.telephony.cdma.SmsMessage.createFromEfRecord(index, data);
        } else {
            wrappedMessage = com.android.internal.telephony.gsm.SmsMessage.createFromEfRecord(index, data);
        }
        if (wrappedMessage != null) {
            return new SmsMessage(wrappedMessage);
        }
        return null;
    }

    public static int getTPLayerLengthForPDU(String pdu) {
        if (isCdmaVoice()) {
            return com.android.internal.telephony.cdma.SmsMessage.getTPLayerLengthForPDU(pdu);
        }
        return com.android.internal.telephony.gsm.SmsMessage.getTPLayerLengthForPDU(pdu);
    }

    public static int[] calculateLength(CharSequence msgBody, boolean use7bitOnly) {
        return calculateLength(msgBody, use7bitOnly, SmsManager.getDefaultSmsSubscriptionId());
    }

    public static int[] calculateLength(CharSequence msgBody, boolean use7bitOnly, int subId) {
        GsmAlphabet.TextEncodingDetails ted;
        if (useCdmaFormatForMoSms(subId)) {
            ted = com.android.internal.telephony.cdma.SmsMessage.calculateLength(msgBody, use7bitOnly, true);
        } else {
            ted = com.android.internal.telephony.gsm.SmsMessage.calculateLength(msgBody, use7bitOnly);
        }
        return new int[]{ted.msgCount, ted.codeUnitCount, ted.codeUnitsRemaining, ted.codeUnitSize};
    }

    @UnsupportedAppUsage
    public static ArrayList<String> fragmentText(String text) {
        return fragmentText(text, SmsManager.getDefaultSmsSubscriptionId());
    }

    public static ArrayList<String> fragmentText(String text, int subId) {
        GsmAlphabet.TextEncodingDetails ted;
        int udhLength;
        int nextPos;
        int udhLength2;
        boolean isCdma = useCdmaFormatForMoSms(subId);
        if (isCdma) {
            ted = com.android.internal.telephony.cdma.SmsMessage.calculateLength(text, false, true);
        } else {
            ted = com.android.internal.telephony.gsm.SmsMessage.calculateLength(text, false);
        }
        if (ted.codeUnitSize == 1) {
            if (ted.languageTable != 0 && ted.languageShiftTable != 0) {
                udhLength2 = 7;
            } else if (ted.languageTable == 0 && ted.languageShiftTable == 0) {
                udhLength2 = 0;
            } else {
                udhLength2 = 4;
            }
            if (ted.msgCount > 1) {
                udhLength2 += 6;
            }
            if (udhLength2 != 0) {
                udhLength2++;
            }
            udhLength = 160 - udhLength2;
        } else if (ted.msgCount > 1) {
            udhLength = 134;
            if (!hasEmsSupport() && ted.msgCount < 10) {
                udhLength = 134 - 2;
            }
        } else {
            udhLength = 140;
        }
        String newMsgBody = null;
        if (Resources.getSystem().getBoolean(R.bool.config_sms_force_7bit_encoding)) {
            newMsgBody = Sms7BitEncodingTranslator.translate(text, isCdma);
        }
        if (TextUtils.isEmpty(newMsgBody)) {
            newMsgBody = text;
        }
        int pos = 0;
        int textLen = newMsgBody.length();
        ArrayList<String> result = new ArrayList<>(ted.msgCount);
        while (true) {
            if (pos >= textLen) {
                break;
            }
            if (ted.codeUnitSize != 1) {
                nextPos = SmsMessageBase.findNextUnicodePosition(pos, udhLength, newMsgBody);
            } else if (!isCdma || !useCdmaFormatForMoSms(subId) || ted.msgCount != 1) {
                nextPos = GsmAlphabet.findGsmSeptetLimitIndex(newMsgBody, pos, udhLength, ted.languageTable, ted.languageShiftTable);
            } else {
                nextPos = Math.min(udhLength, textLen - pos) + pos;
            }
            if (nextPos <= pos || nextPos > textLen) {
                break;
            }
            result.add(newMsgBody.substring(pos, nextPos));
            pos = nextPos;
        }
        Rlog.e(LOG_TAG, "fragmentText failed (" + pos + " >= " + nextPos + " or " + nextPos + " >= " + textLen + ")");
        return result;
    }

    public static int[] calculateLength(String messageBody, boolean use7bitOnly) {
        return calculateLength((CharSequence) messageBody, use7bitOnly);
    }

    public static int[] calculateLength(String messageBody, boolean use7bitOnly, int subId) {
        return calculateLength((CharSequence) messageBody, use7bitOnly, subId);
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested) {
        return getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, SmsManager.getDefaultSmsSubscriptionId());
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested, int subId) {
        SmsMessageBase.SubmitPduBase spb;
        if (useCdmaFormatForMoSms(subId)) {
            spb = com.android.internal.telephony.cdma.SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, (SmsHeader) null);
        } else {
            spb = com.android.internal.telephony.gsm.SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested);
        }
        return new SubmitPdu(spb);
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, short destinationPort, byte[] data, boolean statusReportRequested) {
        SmsMessageBase.SubmitPduBase spb;
        if (useCdmaFormatForMoSms()) {
            spb = com.android.internal.telephony.cdma.SmsMessage.getSubmitPdu(scAddress, destinationAddress, destinationPort, data, statusReportRequested);
        } else {
            spb = com.android.internal.telephony.gsm.SmsMessage.getSubmitPdu(scAddress, destinationAddress, destinationPort, data, statusReportRequested);
        }
        return new SubmitPdu(spb);
    }

    public String getServiceCenterAddress() {
        return this.mWrappedSmsMessage.getServiceCenterAddress();
    }

    public String getOriginatingAddress() {
        return this.mWrappedSmsMessage.getOriginatingAddress();
    }

    public String getDisplayOriginatingAddress() {
        return this.mWrappedSmsMessage.getDisplayOriginatingAddress();
    }

    public String getMessageBody() {
        return this.mWrappedSmsMessage.getMessageBody();
    }

    /* renamed from: android.telephony.SmsMessage$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$SmsConstants$MessageClass = new int[SmsConstants.MessageClass.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$SmsConstants$MessageClass[SmsConstants.MessageClass.CLASS_0.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$SmsConstants$MessageClass[SmsConstants.MessageClass.CLASS_1.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$SmsConstants$MessageClass[SmsConstants.MessageClass.CLASS_2.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$SmsConstants$MessageClass[SmsConstants.MessageClass.CLASS_3.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public MessageClass getMessageClass() {
        int i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$SmsConstants$MessageClass[this.mWrappedSmsMessage.getMessageClass().ordinal()];
        if (i == 1) {
            return MessageClass.CLASS_0;
        }
        if (i == 2) {
            return MessageClass.CLASS_1;
        }
        if (i == 3) {
            return MessageClass.CLASS_2;
        }
        if (i != 4) {
            return MessageClass.UNKNOWN;
        }
        return MessageClass.CLASS_3;
    }

    public String getDisplayMessageBody() {
        return this.mWrappedSmsMessage.getDisplayMessageBody();
    }

    public String getPseudoSubject() {
        return this.mWrappedSmsMessage.getPseudoSubject();
    }

    public long getTimestampMillis() {
        return this.mWrappedSmsMessage.getTimestampMillis();
    }

    public boolean isEmail() {
        return this.mWrappedSmsMessage.isEmail();
    }

    public String getEmailBody() {
        return this.mWrappedSmsMessage.getEmailBody();
    }

    public String getEmailFrom() {
        return this.mWrappedSmsMessage.getEmailFrom();
    }

    public int getProtocolIdentifier() {
        return this.mWrappedSmsMessage.getProtocolIdentifier();
    }

    public boolean isReplace() {
        return this.mWrappedSmsMessage.isReplace();
    }

    public boolean isCphsMwiMessage() {
        return this.mWrappedSmsMessage.isCphsMwiMessage();
    }

    public boolean isMWIClearMessage() {
        return this.mWrappedSmsMessage.isMWIClearMessage();
    }

    public boolean isMWISetMessage() {
        return this.mWrappedSmsMessage.isMWISetMessage();
    }

    public boolean isMwiDontStore() {
        return this.mWrappedSmsMessage.isMwiDontStore();
    }

    public byte[] getUserData() {
        return this.mWrappedSmsMessage.getUserData();
    }

    public byte[] getPdu() {
        return this.mWrappedSmsMessage.getPdu();
    }

    @Deprecated
    public int getStatusOnSim() {
        return this.mWrappedSmsMessage.getStatusOnIcc();
    }

    public int getStatusOnIcc() {
        return this.mWrappedSmsMessage.getStatusOnIcc();
    }

    @Deprecated
    public int getIndexOnSim() {
        return this.mWrappedSmsMessage.getIndexOnIcc();
    }

    public int getIndexOnIcc() {
        return this.mWrappedSmsMessage.getIndexOnIcc();
    }

    public int getStatus() {
        return this.mWrappedSmsMessage.getStatus();
    }

    public boolean isStatusReportMessage() {
        return this.mWrappedSmsMessage.isStatusReportMessage();
    }

    public boolean isReplyPathPresent() {
        return this.mWrappedSmsMessage.isReplyPathPresent();
    }

    @UnsupportedAppUsage
    private static boolean useCdmaFormatForMoSms() {
        return useCdmaFormatForMoSms(SmsManager.getDefaultSmsSubscriptionId());
    }

    @UnsupportedAppUsage
    private static boolean useCdmaFormatForMoSms(int subId) {
        SmsManager smsManager = SmsManager.getSmsManagerForSubscriptionId(subId);
        if (!smsManager.isImsSmsSupported()) {
            return isCdmaVoice(subId);
        }
        return "3gpp2".equals(smsManager.getImsSmsFormat());
    }

    private static boolean isCdmaVoice() {
        return isCdmaVoice(SmsManager.getDefaultSmsSubscriptionId());
    }

    /* JADX INFO: finally extract failed */
    public static boolean hasEmsSupport() {
        if (!isNoEmsSupportConfigListExisted()) {
            return true;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            String simOperator = TelephonyManager.getDefault().getSimOperatorNumeric();
            String gid = TelephonyManager.getDefault().getGroupIdLevel1();
            Binder.restoreCallingIdentity(identity);
            if (!TextUtils.isEmpty(simOperator)) {
                NoEmsSupportConfig[] noEmsSupportConfigArr = mNoEmsSupportConfigList;
                for (NoEmsSupportConfig currentConfig : noEmsSupportConfigArr) {
                    if (simOperator.startsWith(currentConfig.mOperatorNumber) && (TextUtils.isEmpty(currentConfig.mGid1) || (!TextUtils.isEmpty(currentConfig.mGid1) && currentConfig.mGid1.equalsIgnoreCase(gid)))) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    public static boolean shouldAppendPageNumberAsPrefix() {
        if (!isNoEmsSupportConfigListExisted()) {
            return false;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            String simOperator = TelephonyManager.getDefault().getSimOperatorNumeric();
            String gid = TelephonyManager.getDefault().getGroupIdLevel1();
            Binder.restoreCallingIdentity(identity);
            NoEmsSupportConfig[] noEmsSupportConfigArr = mNoEmsSupportConfigList;
            for (NoEmsSupportConfig currentConfig : noEmsSupportConfigArr) {
                if (simOperator.startsWith(currentConfig.mOperatorNumber) && (TextUtils.isEmpty(currentConfig.mGid1) || (!TextUtils.isEmpty(currentConfig.mGid1) && currentConfig.mGid1.equalsIgnoreCase(gid)))) {
                    return currentConfig.mIsPrefix;
                }
            }
            return false;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public static class NoEmsSupportConfig {
        String mGid1;
        boolean mIsPrefix;
        String mOperatorNumber;

        public NoEmsSupportConfig(String[] config) {
            this.mOperatorNumber = config[0];
            this.mIsPrefix = "prefix".equals(config[1]);
            this.mGid1 = config.length > 2 ? config[2] : null;
        }

        public String toString() {
            return "NoEmsSupportConfig { mOperatorNumber = " + this.mOperatorNumber + ", mIsPrefix = " + this.mIsPrefix + ", mGid1 = " + this.mGid1 + " }";
        }
    }

    private static boolean isNoEmsSupportConfigListExisted() {
        Resources r;
        if (!mIsNoEmsSupportConfigListLoaded && (r = Resources.getSystem()) != null) {
            String[] listArray = r.getStringArray(R.array.no_ems_support_sim_operators);
            if (listArray != null && listArray.length > 0) {
                mNoEmsSupportConfigList = new NoEmsSupportConfig[listArray.length];
                for (int i = 0; i < listArray.length; i++) {
                    mNoEmsSupportConfigList[i] = new NoEmsSupportConfig(listArray[i].split(";"));
                }
            }
            mIsNoEmsSupportConfigListLoaded = true;
        }
        NoEmsSupportConfig[] noEmsSupportConfigArr = mNoEmsSupportConfigList;
        if (noEmsSupportConfigArr == null || noEmsSupportConfigArr.length == 0) {
            return false;
        }
        return true;
    }

    public static boolean handleMSimBySubscrition(int subscription) {
        return -1 != subscription;
    }

    public static boolean isCdmaVoiceBySubscrition(int subscription) {
        return subscription == 0;
    }

    private static boolean isCdmaVoice(int subId) {
        return 2 == TelephonyManager.getDefault().getCurrentPhoneType(subId);
    }

    public static boolean useCdmaFormatForMoSmsHw(int subscription) {
        return useCdmaFormatForMoSms(subscription);
    }

    public static boolean isCdmaVoiceHw(int subscription) {
        return isCdmaVoice(subscription);
    }

    public static boolean useCdmaFormatForMoSmsEx() {
        return useCdmaFormatForMoSms();
    }

    public String getRecipientAddress() {
        return this.mWrappedSmsMessage.getRecipientAddress();
    }
}

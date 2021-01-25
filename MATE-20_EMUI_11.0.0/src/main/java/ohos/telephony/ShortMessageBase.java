package ohos.telephony;

import android.content.res.Resources;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.Sms7BitEncodingTranslator;
import com.android.internal.telephony.SmsConstants;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.cdma.SmsMessage;
import java.util.ArrayList;
import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class ShortMessageBase {
    private static final int ENCODING_16BIT = 3;
    private static final int ENCODING_7BIT = 1;
    private static final int ENCODING_8BIT = 2;
    private static final int ENCODING_UNKNOWN = 0;
    private static final String FORMAT_3GPP = "3gpp";
    private static final String FORMAT_3GPP2 = "3gpp2";
    private static final int MAX_USER_DATA_BYTES = 140;
    private static final int MAX_USER_DATA_BYTES_WITH_HEADER = 134;
    private static final int MAX_USER_DATA_SEPTETS = 160;
    private static final HiLogLabel TAG = new HiLogLabel(3, TelephonyUtils.LOG_ID_TELEPHONY, "ShortMessageBase");
    private static boolean hasCheckedShortMessageConfig = false;
    private static ShortMessageConfig[] shortMessageConfigList = null;
    private SmsMessageBase wrappedShortMessage;

    /* access modifiers changed from: private */
    public static class ShortMessageConfig {
        String gid1;
        boolean isPrefix;
        String operator;

        public ShortMessageConfig(String[] strArr) {
            if (strArr != null) {
                boolean z = false;
                String str = "";
                this.operator = strArr.length > 0 ? strArr[0] : str;
                this.isPrefix = strArr.length > 1 ? "prefix".equals(strArr[1]) : z;
                this.gid1 = strArr.length > 2 ? strArr[2] : str;
            }
        }

        public String toString() {
            return "ShortMessageConfig { operator = " + this.operator + ", isPrefix = " + this.isPrefix + ", gid1 = " + this.gid1 + " }";
        }
    }

    private ShortMessageBase(SmsMessageBase smsMessageBase) {
        this.wrappedShortMessage = smsMessageBase;
    }

    public int getProtocolId() {
        return this.wrappedShortMessage.getProtocolIdentifier();
    }

    /* renamed from: ohos.telephony.ShortMessageBase$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$SmsConstants$MessageClass = new int[SmsConstants.MessageClass.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$SmsConstants$MessageClass[SmsConstants.MessageClass.CLASS_0.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$SmsConstants$MessageClass[SmsConstants.MessageClass.CLASS_1.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$SmsConstants$MessageClass[SmsConstants.MessageClass.CLASS_2.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$SmsConstants$MessageClass[SmsConstants.MessageClass.CLASS_3.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    public int getMessageClass() {
        int i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$SmsConstants$MessageClass[this.wrappedShortMessage.getMessageClass().ordinal()];
        int i2 = 1;
        if (i != 1) {
            i2 = 2;
            if (i != 2) {
                i2 = 3;
                if (i != 3) {
                    i2 = 4;
                    if (i != 4) {
                        return 0;
                    }
                }
            }
        }
        return i2;
    }

    public static Optional<ShortMessageBase> createMessage(byte[] bArr, String str) {
        SmsMessage smsMessage;
        if (bArr == null) {
            HiLog.error(TAG, "createMessage(): pdu is null", new Object[0]);
            return Optional.empty();
        }
        if (FORMAT_3GPP2.equals(str)) {
            smsMessage = SmsMessage.createFromPdu(bArr);
        } else if (FORMAT_3GPP.equals(str)) {
            smsMessage = com.android.internal.telephony.gsm.SmsMessage.createFromPdu(bArr);
        } else {
            HiLog.error(TAG, "createMessage(): unsupported message format ", new Object[0]);
            return Optional.empty();
        }
        if (smsMessage != null) {
            return Optional.of(new ShortMessageBase(smsMessage));
        }
        return Optional.empty();
    }

    private static ArrayList<String> emptyList() {
        ArrayList<String> arrayList = new ArrayList<>(1);
        arrayList.add("");
        return arrayList;
    }

    private static int computeGsmSeptetLimit(GsmAlphabet.TextEncodingDetails textEncodingDetails) {
        int i;
        if (textEncodingDetails.codeUnitSize == 1) {
            if (textEncodingDetails.languageTable == 0 && textEncodingDetails.languageShiftTable == 0) {
                i = MAX_USER_DATA_SEPTETS;
            } else {
                i = (textEncodingDetails.languageTable == 0 || textEncodingDetails.languageShiftTable == 0) ? 156 : 153;
            }
            if (textEncodingDetails.msgCount > 1) {
                i -= 6;
            }
            return i != MAX_USER_DATA_SEPTETS ? i - 1 : i;
        } else if (textEncodingDetails.msgCount <= 1) {
            return MAX_USER_DATA_BYTES;
        } else {
            if (isLongShortMessageSupported() || textEncodingDetails.msgCount >= 10) {
                return MAX_USER_DATA_BYTES_WITH_HEADER;
            }
            return 132;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0096, code lost:
        ohos.hiviewdfx.HiLog.error(ohos.telephony.ShortMessageBase.TAG, "splitMessage failed (%{public}d <= %{public}d or >= %{public}d)", java.lang.Integer.valueOf(r8), java.lang.Integer.valueOf(r7), java.lang.Integer.valueOf(r5));
     */
    public static ArrayList<String> splitMessageBySlotId(String str, int i) {
        GsmAlphabet.TextEncodingDetails textEncodingDetails;
        int i2;
        if (str == null || str.length() == 0) {
            return emptyList();
        }
        boolean isMoSmsInCdmaSpecification = isMoSmsInCdmaSpecification(i);
        if (isMoSmsInCdmaSpecification) {
            textEncodingDetails = SmsMessage.calculateLength(str, false, true);
        } else {
            textEncodingDetails = com.android.internal.telephony.gsm.SmsMessage.calculateLength(str, false);
        }
        if (textEncodingDetails == null) {
            return emptyList();
        }
        int computeGsmSeptetLimit = computeGsmSeptetLimit(textEncodingDetails);
        String str2 = null;
        Resources system = Resources.getSystem();
        if (system != null && system.getBoolean(17891526)) {
            str2 = Sms7BitEncodingTranslator.translate(str, isMoSmsInCdmaSpecification);
        }
        if (!(str2 == null || str2.length() == 0)) {
            str = str2;
        }
        int length = str.length();
        ArrayList<String> arrayList = new ArrayList<>(textEncodingDetails.msgCount);
        int i3 = 0;
        while (true) {
            if (i3 >= length) {
                break;
            }
            if (textEncodingDetails.codeUnitSize != 1) {
                i2 = SmsMessageBase.findNextUnicodePosition(i3, computeGsmSeptetLimit, str);
            } else if (!isMoSmsInCdmaSpecification) {
                i2 = GsmAlphabet.findGsmSeptetLimitIndex(str, i3, computeGsmSeptetLimit, textEncodingDetails.languageTable, textEncodingDetails.languageShiftTable);
            } else if (!isMoSmsInCdmaSpecification(i)) {
                i2 = GsmAlphabet.findGsmSeptetLimitIndex(str, i3, computeGsmSeptetLimit, textEncodingDetails.languageTable, textEncodingDetails.languageShiftTable);
            } else if (textEncodingDetails.msgCount != 1) {
                i2 = GsmAlphabet.findGsmSeptetLimitIndex(str, i3, computeGsmSeptetLimit, textEncodingDetails.languageTable, textEncodingDetails.languageShiftTable);
            } else {
                i2 = Math.min(computeGsmSeptetLimit, length - i3) + i3;
            }
            if (i2 <= i3 || i2 > length) {
                break;
            }
            arrayList.add(str.substring(i3, i2));
            i3 = i2;
        }
        return arrayList;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x002e A[ORIG_RETURN, RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:16:? A[RETURN, SYNTHETIC] */
    private static boolean isMoSmsInCdmaSpecification(int i) {
        int i2;
        ShortMessageManager instance = ShortMessageManager.getInstance(null);
        if (instance.isImsSmsSupported()) {
            return FORMAT_3GPP2.equals(instance.getImsShortMessageFormat());
        }
        String telephonyProperty = TelephonyUtils.getTelephonyProperty(i, TelephonyUtils.PROPERTY_CURRENT_PHONE_TYPE, "");
        if (telephonyProperty != null && !telephonyProperty.isEmpty()) {
            try {
                i2 = Integer.parseInt(telephonyProperty);
            } catch (NumberFormatException unused) {
                HiLog.error(TAG, "prop value is invalid !", new Object[0]);
            }
            if (i2 != 2) {
                return true;
            }
            return false;
        }
        i2 = 0;
        if (i2 != 2) {
        }
    }

    private static boolean isLongShortMessageSupported() {
        if (!initShortMessageConfig()) {
            return true;
        }
        SimInfoManager instance = SimInfoManager.getInstance(null);
        ShortMessageManager instance2 = ShortMessageManager.getInstance(null);
        String simOperatorNumeric = instance.getSimOperatorNumeric(instance2.getDefaultSmsSlotId());
        String simGid1 = instance.getSimGid1(instance2.getDefaultSmsSlotId());
        if (!(simOperatorNumeric == null || simOperatorNumeric.length() == 0)) {
            ShortMessageConfig[] shortMessageConfigArr = shortMessageConfigList;
            for (ShortMessageConfig shortMessageConfig : shortMessageConfigArr) {
                if (simOperatorNumeric.startsWith(shortMessageConfig.operator) && (shortMessageConfig.gid1 == null || shortMessageConfig.gid1.length() == 0 || shortMessageConfig.gid1.equalsIgnoreCase(simGid1))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean initShortMessageConfig() {
        Resources system = Resources.getSystem();
        if (hasCheckedShortMessageConfig || system == null) {
            ShortMessageConfig[] shortMessageConfigArr = shortMessageConfigList;
            return (shortMessageConfigArr == null || shortMessageConfigArr.length == 0) ? false : true;
        }
        hasCheckedShortMessageConfig = true;
        String[] stringArray = system.getStringArray(17236094);
        if (stringArray == null || stringArray.length <= 0) {
            return false;
        }
        shortMessageConfigList = new ShortMessageConfig[stringArray.length];
        for (int i = 0; i < stringArray.length; i++) {
            shortMessageConfigList[i] = new ShortMessageConfig(stringArray[i].split(";"));
        }
        return true;
    }

    public String getVisibleMessageBody() {
        return this.wrappedShortMessage.getDisplayMessageBody();
    }

    public String getVisibleRawAddress() {
        return this.wrappedShortMessage.getDisplayOriginatingAddress();
    }

    public String getScAddress() {
        return this.wrappedShortMessage.getServiceCenterAddress();
    }

    public long getScTimestamp() {
        return this.wrappedShortMessage.getTimestampMillis();
    }

    public byte[] getUserRawData() {
        return this.wrappedShortMessage.getUserData();
    }

    public boolean isEmailMessage() {
        return this.wrappedShortMessage.isEmail();
    }

    public boolean isReplaceMessage() {
        return this.wrappedShortMessage.isReplace();
    }

    public boolean hasReplyPath() {
        return this.wrappedShortMessage.isReplyPathPresent();
    }

    public String getEmailMessageBody() {
        return this.wrappedShortMessage.getEmailBody();
    }

    public String getEmailAddress() {
        return this.wrappedShortMessage.getEmailFrom();
    }
}

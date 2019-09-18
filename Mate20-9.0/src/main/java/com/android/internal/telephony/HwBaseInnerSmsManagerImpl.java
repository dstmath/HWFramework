package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.BaseBundle;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.cdma.sms.BearerData;
import com.android.internal.telephony.cdma.sms.CdmaSmsAddress;
import com.android.internal.telephony.cdma.sms.HwBearerData;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.gsm.HwSmsMessage;
import com.android.internal.telephony.gsm.SmsMessage;
import com.android.internal.telephony.gsm.SmsMessageUtils;
import com.android.internal.util.BitwiseOutputStream;
import java.util.ArrayList;

public class HwBaseInnerSmsManagerImpl implements HwBaseInnerSmsManager {
    public static final String CONFIG_ALLOW_ENABLING_WAP_PUSH_SI = "allowEnablingWapPushSI";
    public static final String CONFIG_ENABLE_WAP_PUSH_SI = "enableWapPushSI";
    public static final String CONFIG_GROUP_CHAT_DEFAULT_TO_MMS = "groupChatDefaultsToMMS";
    public static final String CONFIG_SMS_DELIVERY_REPORT_SETTING = "smsDeliveryReportSettingOnByDefault";
    public static final String CONFIG_SMS_USES_SIMPLE_CHARACTERS_ONLY = "smsUsesSimpleCharactersOnly";
    public static final String CONFIG_USE_CUSTOM_USER_AGENT = "useCustomUserAgent";
    private static final String CONTACT_PACKAGE_NAME = "com.android.contacts";
    private static final String LOG_TAG = "HwBaseInnerSmsManagerImpl";
    private static final String MMS_ACTIVITY_NAME = "com.android.mms.ui.ComposeMessageActivity";
    private static final String MMS_PACKAGE_NAME = "com.android.mms";
    public static final int SMS_GW_VP_ABSOLUTE_FORMAT = 24;
    public static final int SMS_GW_VP_ENHANCED_FORMAT = 8;
    public static final int SMS_GW_VP_RELATIVE_FORMAT = 16;
    private static SmsMessageUtils gsmSmsMessageUtils = new SmsMessageUtils();
    private static final boolean isAllowedCsFw = SystemProperties.getBoolean("ro.config.hw_bastet_csfw", false);
    private static HwBaseInnerSmsManager mInstance = new HwBaseInnerSmsManagerImpl();

    public static HwBaseInnerSmsManager getDefault() {
        return mInstance;
    }

    public boolean shouldSetDefaultApplicationForPackage(String packageName, Context context) {
        String hwMmsPackageName = getHwMmsPackageName(context);
        Rlog.d(LOG_TAG, "current packageName: " + packageName + ", hwMmsPackageName: " + hwMmsPackageName);
        return !SystemProperties.getBoolean("ro.config.hw_privacymode", false) || 1 != Settings.Secure.getInt(context.getContentResolver(), "privacy_mode_on", 0) || 1 != Settings.Secure.getInt(context.getContentResolver(), "privacy_mode_state", 0) || packageName == null || hwMmsPackageName == null || packageName.equals(hwMmsPackageName);
    }

    private static String getHwMmsPackageName(Context context) {
        Intent intent = new Intent();
        intent.setClassName(CONTACT_PACKAGE_NAME, "com.android.mms.ui.ComposeMessageActivity");
        if (context.getPackageManager().resolveActivity(intent, 0) == null) {
            return MMS_PACKAGE_NAME;
        }
        return CONTACT_PACKAGE_NAME;
    }

    public boolean allowToSetSmsWritePermission(String packageName) {
        if (CONTACT_PACKAGE_NAME.equalsIgnoreCase(packageName) || MMS_PACKAGE_NAME.equalsIgnoreCase(packageName)) {
            return true;
        }
        return false;
    }

    public boolean parseGsmSmsSubmit(SmsMessage smsMessage, int mti, Object parcel, int firstByte) {
        SmsMessage.PduParser p = (SmsMessage.PduParser) parcel;
        boolean hasUserDataHeader = false;
        if (1 != mti) {
            return false;
        }
        p.getByte();
        SmsMessageBaseUtils.setOriginatingAddress(smsMessage, p.getAddress());
        gsmSmsMessageUtils.setProtocolIdentifier(smsMessage, p.getByte());
        gsmSmsMessageUtils.setDataCodingScheme(smsMessage, p.getByte());
        p.mCur += getValidityPeriod(firstByte);
        if ((firstByte & 64) == 64) {
            hasUserDataHeader = true;
        }
        gsmSmsMessageUtils.parseUserData(smsMessage, p, hasUserDataHeader);
        return true;
    }

    private int getValidityPeriod(int firstByte) {
        int ValidityPeriod = firstByte & 24;
        if (ValidityPeriod != 8) {
            if (ValidityPeriod == 16) {
                return 1;
            }
            if (ValidityPeriod != 24) {
                Rlog.e("PduParser", "unsupported validity format.");
                return 0;
            }
        }
        return 7;
    }

    public String getUserDataGSM8Bit(SmsMessage.PduParser p, int septetCount) {
        return HwSmsMessage.getUserDataGSM8Bit(p, septetCount);
    }

    public void parseRUIMPdu(com.android.internal.telephony.cdma.SmsMessage msg, byte[] pdu) {
        com.android.internal.telephony.cdma.HwSmsMessage.parseRUIMPdu(msg, pdu);
    }

    public boolean encode7bitMultiSms(UserData uData, byte[] udhData, boolean force) {
        return HwBearerData.encode7bitMultiSms(uData, udhData, force);
    }

    public void encodeMsgCenterTimeStampCheck(BearerData bData, BitwiseOutputStream outStream) throws BitwiseOutputStream.AccessException {
        HwBearerData.encodeMsgCenterTimeStampCheck(bData, outStream);
    }

    public void doubleSmsStatusCheck(com.android.internal.telephony.cdma.SmsMessage msg) {
        com.android.internal.telephony.cdma.HwSmsMessage.doubleSmsStatusCheck(msg);
    }

    public int getCdmaSub() {
        return com.android.internal.telephony.cdma.HwSmsMessage.getCdmaSub();
    }

    public ArrayList<String> fragmentForEmptyText() {
        ArrayList<String> result = new ArrayList<>(1);
        result.add("");
        return result;
    }

    public byte[] getNewbyte() {
        if (2 == TelephonyManager.getDefault().getPhoneType()) {
            return new byte[254];
        }
        return new byte[PduHeaders.START];
    }

    public void putExtraDataToConfig(BaseBundle config, Bundle filtered) {
        if (config == null || filtered == null) {
            Rlog.e(LOG_TAG, "putExtraDataToConfig config or filter is null, return.");
            return;
        }
        filtered.putBoolean(CONFIG_SMS_USES_SIMPLE_CHARACTERS_ONLY, config.getBoolean(CONFIG_SMS_USES_SIMPLE_CHARACTERS_ONLY));
        filtered.putBoolean(CONFIG_GROUP_CHAT_DEFAULT_TO_MMS, config.getBoolean(CONFIG_GROUP_CHAT_DEFAULT_TO_MMS));
        filtered.putBoolean(CONFIG_USE_CUSTOM_USER_AGENT, config.getBoolean(CONFIG_USE_CUSTOM_USER_AGENT));
        filtered.putBoolean(CONFIG_SMS_DELIVERY_REPORT_SETTING, config.getBoolean(CONFIG_SMS_DELIVERY_REPORT_SETTING));
        filtered.putBoolean(CONFIG_ENABLE_WAP_PUSH_SI, config.getBoolean(CONFIG_ENABLE_WAP_PUSH_SI));
        filtered.putBoolean(CONFIG_ALLOW_ENABLING_WAP_PUSH_SI, config.getBoolean(CONFIG_ALLOW_ENABLING_WAP_PUSH_SI));
    }

    public boolean checkSmsBlacklistFlag(SmsMessage.PduParser p) {
        int firstByteValue;
        if (!isAllowedCsFw) {
            return false;
        }
        Rlog.d(LOG_TAG, "parsePdu FirstByteValue: " + firstByteValue);
        if (255 != firstByteValue) {
            return false;
        }
        p.mCur++;
        Rlog.d(LOG_TAG, "parsePdu blacklistFlag: " + true + "p.mCur: " + p.mCur);
        return true;
    }

    public CdmaSmsAddress parseForQcom(String address) {
        CdmaSmsAddress addr = new CdmaSmsAddress();
        addr.address = address;
        addr.digitMode = 0;
        addr.ton = 0;
        addr.numberMode = 0;
        addr.numberPlan = 0;
        byte[] origBytes = null;
        if (address.indexOf(43) != -1) {
            addr.digitMode = 1;
            addr.ton = 1;
            addr.numberMode = 0;
            addr.numberPlan = 1;
        }
        if (address.indexOf(64) != -1) {
            addr.digitMode = 1;
            addr.ton = 2;
            addr.numberMode = 1;
        }
        String filteredAddr = CdmaSmsAddress.filterNumericSugarHw(address);
        if (addr.digitMode == 0) {
            if (filteredAddr != null) {
                origBytes = CdmaSmsAddress.parseToDtmf(filteredAddr);
            }
            if (origBytes == null) {
                addr.digitMode = 1;
            }
        }
        if (addr.digitMode == 1) {
            if (filteredAddr == null) {
                filteredAddr = CdmaSmsAddress.filterWhitespaceHw(address);
            }
            origBytes = UserData.stringToAscii(filteredAddr);
            if (origBytes == null) {
                return null;
            }
        }
        if (origBytes != null) {
            addr.origBytes = origBytes;
            addr.numberOfDigits = origBytes.length;
        }
        return addr;
    }
}

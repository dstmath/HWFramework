package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.BaseBundle;
import android.os.Bundle;
import android.provider.Settings;
import com.android.internal.telephony.cdma.sms.HwBearerData;
import com.android.internal.telephony.gsm.HwSmsMessage;
import com.android.internal.telephony.gsm.SmsMessageUtils;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.cdma.sms.BearerDataEx;
import com.huawei.internal.telephony.cdma.sms.CdmaSmsAddressEx;
import com.huawei.internal.telephony.cdma.sms.UserDataEx;
import com.huawei.internal.telephony.gsm.SmsMessageEx;
import com.huawei.internal.util.BitwiseOutputStreamEx;
import java.util.ArrayList;

public class HwBaseInnerSmsManagerImpl extends DefaultHwBaseInnerSmsManager {
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
    private static final String NEW_CONTACT_PACKAGE_NAME = "com.huawei.contacts";
    public static final int SMS_GW_VP_ABSOLUTE_FORMAT = 24;
    public static final int SMS_GW_VP_ENHANCED_FORMAT = 8;
    public static final int SMS_GW_VP_RELATIVE_FORMAT = 16;
    private static SmsMessageUtils gsmSmsMessageUtils = new SmsMessageUtils();
    private static final boolean isAllowedCsFw = SystemPropertiesEx.getBoolean("ro.config.hw_bastet_csfw", false);
    private static HwBaseInnerSmsManager mInstance = new HwBaseInnerSmsManagerImpl();

    public static HwBaseInnerSmsManager getDefault() {
        return mInstance;
    }

    public boolean shouldSetDefaultApplicationForPackage(String packageName, Context context) {
        if (context == null || packageName == null) {
            return false;
        }
        String hwMmsPackageName = getHwMmsPackageName(context);
        RlogEx.i(LOG_TAG, "current packageName: " + packageName + ", hwMmsPackageName: " + hwMmsPackageName);
        String hwMmsPackageNameNew = getHwMmsPackageNameNew(context);
        if (!SystemPropertiesEx.getBoolean("ro.config.hw_privacymode", false) || 1 != Settings.Secure.getInt(context.getContentResolver(), "privacy_mode_on", 0) || 1 != Settings.Secure.getInt(context.getContentResolver(), "privacy_mode_state", 0) || packageName.equals(hwMmsPackageName) || packageName.equals(hwMmsPackageNameNew)) {
            return true;
        }
        return false;
    }

    private static String getHwMmsPackageName(Context context) {
        Intent intent = new Intent();
        intent.setClassName(CONTACT_PACKAGE_NAME, MMS_ACTIVITY_NAME);
        if (context == null || context.getPackageManager() == null || context.getPackageManager().resolveActivity(intent, 0) == null) {
            return MMS_PACKAGE_NAME;
        }
        return CONTACT_PACKAGE_NAME;
    }

    private static String getHwMmsPackageNameNew(Context context) {
        Intent intent = new Intent();
        intent.setClassName(NEW_CONTACT_PACKAGE_NAME, MMS_ACTIVITY_NAME);
        if (context == null || context.getPackageManager() == null || context.getPackageManager().resolveActivity(intent, 0) == null) {
            return MMS_PACKAGE_NAME;
        }
        return NEW_CONTACT_PACKAGE_NAME;
    }

    public boolean allowToSetSmsWritePermission(String packageName) {
        if (CONTACT_PACKAGE_NAME.equalsIgnoreCase(packageName) || NEW_CONTACT_PACKAGE_NAME.equalsIgnoreCase(packageName) || MMS_PACKAGE_NAME.equalsIgnoreCase(packageName)) {
            return true;
        }
        return false;
    }

    public boolean parseGsmSmsSubmit(SmsMessageEx smsMessage, int mti, Object parcel, int firstByte) {
        SmsMessageEx.PduParserEx pduParserEx = parcel instanceof SmsMessageEx.PduParserEx ? (SmsMessageEx.PduParserEx) parcel : null;
        boolean hasUserDataHeader = false;
        if (1 != mti || pduParserEx == null) {
            return false;
        }
        pduParserEx.getByteHw();
        SmsMessageBaseUtils.setOriginatingAddress(smsMessage, pduParserEx.getAddressHw());
        gsmSmsMessageUtils.setProtocolIdentifier(smsMessage, pduParserEx.getByteHw());
        gsmSmsMessageUtils.setDataCodingScheme(smsMessage, pduParserEx.getByteHw());
        pduParserEx.setCurHw(pduParserEx.getCurHw() + getValidityPeriod(firstByte));
        if ((firstByte & 64) == 64) {
            hasUserDataHeader = true;
        }
        gsmSmsMessageUtils.parseUserData(smsMessage, pduParserEx, hasUserDataHeader);
        return true;
    }

    private int getValidityPeriod(int firstByte) {
        int ValidityPeriod = firstByte & 24;
        if (ValidityPeriod == 8) {
            return 7;
        }
        if (ValidityPeriod == 16) {
            return 1;
        }
        if (ValidityPeriod == 24) {
            return 7;
        }
        RlogEx.e("PduParserEx", "unsupported validity format.");
        return 0;
    }

    public String getUserDataGSM8Bit(SmsMessageEx.PduParserEx p, int septetCount) {
        return HwSmsMessage.getUserDataGSM8Bit(p, septetCount);
    }

    public void parseRUIMPdu(com.huawei.internal.telephony.cdma.SmsMessageEx msg, byte[] pdu) {
        com.android.internal.telephony.cdma.HwSmsMessage.parseRUIMPdu(msg, pdu);
    }

    public boolean encode7bitMultiSms(UserDataEx uData, byte[] udhData, boolean force) {
        return HwBearerData.encode7bitMultiSms(uData, udhData, force);
    }

    public void encodeMsgCenterTimeStampCheck(BearerDataEx bData, BitwiseOutputStreamEx outStream) throws BitwiseOutputStreamEx.AccessExceptionEx {
        HwBearerData.encodeMsgCenterTimeStampCheck(bData, outStream);
    }

    public void doubleSmsStatusCheck(com.huawei.internal.telephony.cdma.SmsMessageEx msg) {
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
        if (2 == TelephonyManagerEx.getDefault().getPhoneType()) {
            return new byte[254];
        }
        return new byte[175];
    }

    public void putExtraDataToConfig(BaseBundle config, Bundle filtered) {
        if (config == null || filtered == null) {
            RlogEx.e(LOG_TAG, "putExtraDataToConfig config or filter is null, return.");
            return;
        }
        filtered.putBoolean(CONFIG_SMS_USES_SIMPLE_CHARACTERS_ONLY, config.getBoolean(CONFIG_SMS_USES_SIMPLE_CHARACTERS_ONLY));
        filtered.putBoolean(CONFIG_GROUP_CHAT_DEFAULT_TO_MMS, config.getBoolean(CONFIG_GROUP_CHAT_DEFAULT_TO_MMS));
        filtered.putBoolean(CONFIG_USE_CUSTOM_USER_AGENT, config.getBoolean(CONFIG_USE_CUSTOM_USER_AGENT));
        filtered.putBoolean(CONFIG_SMS_DELIVERY_REPORT_SETTING, config.getBoolean(CONFIG_SMS_DELIVERY_REPORT_SETTING));
        filtered.putBoolean(CONFIG_ENABLE_WAP_PUSH_SI, config.getBoolean(CONFIG_ENABLE_WAP_PUSH_SI));
        filtered.putBoolean(CONFIG_ALLOW_ENABLING_WAP_PUSH_SI, config.getBoolean(CONFIG_ALLOW_ENABLING_WAP_PUSH_SI));
    }

    public boolean checkSmsBlacklistFlag(SmsMessageEx.PduParserEx p) {
        if (p == null || !isAllowedCsFw) {
            return false;
        }
        int firstByteValue = p.getPduHw()[p.getCurHw()] & 255;
        RlogEx.i(LOG_TAG, "parsePdu FirstByteValue: " + firstByteValue);
        if (255 != firstByteValue) {
            return false;
        }
        p.setCurHw(p.getCurHw() + 1);
        RlogEx.i(LOG_TAG, "parsePdu blacklistFlag: " + true + "p.mCur: " + p.getCurHw());
        return true;
    }

    public CdmaSmsAddressEx parseForQcom(String address) {
        CdmaSmsAddressEx addr = new CdmaSmsAddressEx();
        if (address == null) {
            return addr;
        }
        addr.setSmsAddress(address);
        addr.setDigitMode(0);
        addr.setTon(0);
        addr.setNumberMode(0);
        addr.setNumberPlan(0);
        byte[] origBytes = null;
        if (address.indexOf(43) != -1) {
            addr.setDigitMode(1);
            addr.setTon(1);
            addr.setNumberMode(0);
            addr.setNumberPlan(1);
        }
        if (address.indexOf(64) != -1) {
            addr.setDigitMode(1);
            addr.setTon(2);
            addr.setNumberMode(1);
        }
        String filteredAddr = CdmaSmsAddressEx.filterNumericSugarHw(address);
        if (addr.getDigitMode() == 0) {
            if (filteredAddr != null) {
                origBytes = CdmaSmsAddressEx.parseToDtmf(filteredAddr);
            }
            if (origBytes == null) {
                addr.setDigitMode(1);
            }
        }
        if (addr.getDigitMode() == 1) {
            if (filteredAddr == null) {
                filteredAddr = CdmaSmsAddressEx.filterWhitespaceHw(address);
            }
            origBytes = UserDataEx.stringToAscii(filteredAddr);
            if (origBytes == null) {
                return null;
            }
        }
        if (origBytes != null) {
            addr.setOrigBytes(origBytes);
            addr.setNumberOfDigits(origBytes.length);
        }
        return addr;
    }
}

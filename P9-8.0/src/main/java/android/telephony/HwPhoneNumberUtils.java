package android.telephony;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.text.TextUtils;
import com.android.internal.telephony.HwTelephonyProperties;
import huawei.android.telephony.wrapper.WrapperFactory;
import java.util.Locale;

public class HwPhoneNumberUtils {
    private static final boolean IS_SUPPORT_LONG_VMNUM = SystemProperties.getBoolean("ro.config.hw_support_long_vmNum", false);
    private static final String LOG_TAG = "HwPhoneNumberUtils";
    private static boolean isAirplaneModeOn = false;

    public static String custExtraNumbers(long subId, String numbers) {
        String custNumbers;
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            custNumbers = SystemProperties.get(HwTelephonyProperties.PROPERTY_GLOBAL_CUST_ECCLIST + SubscriptionManager.getSlotIndex((int) subId), "");
        } else {
            custNumbers = SystemProperties.get(HwTelephonyProperties.PROPERTY_GLOBAL_CUST_ECCLIST, "");
        }
        if (TextUtils.isEmpty(custNumbers)) {
            return numbers;
        }
        return numbers + "," + custNumbers;
    }

    public static boolean skipHardcodeNumbers() {
        if (WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled() || TelephonyManager.getDefault().getPhoneType() != 2) {
            return false;
        }
        return true;
    }

    public static boolean isVoiceMailNumber(String number) {
        boolean z = true;
        if (isLongVoiceMailNumber(number)) {
            return true;
        }
        String vmNumberSub2 = "";
        try {
            String vmNumber;
            if (WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
                vmNumber = WrapperFactory.getMSimTelephonyManagerWrapper().getVoiceMailNumber(0);
                vmNumberSub2 = WrapperFactory.getMSimTelephonyManagerWrapper().getVoiceMailNumber(1);
            } else {
                vmNumber = TelephonyManager.getDefault().getVoiceMailNumber();
            }
            number = PhoneNumberUtils.extractNetworkPortionAlt(number);
            if (TextUtils.isEmpty(number)) {
                z = false;
            } else if (!PhoneNumberUtils.compare(number, vmNumber)) {
                z = PhoneNumberUtils.compare(number, vmNumberSub2);
            }
            return z;
        } catch (SecurityException e) {
            return false;
        }
    }

    public static boolean useVoiceMailNumberFeature() {
        return true;
    }

    public static boolean isHwCustNotEmergencyNumber(Context mContext, String number) {
        boolean z = true;
        if (System.getInt(mContext.getContentResolver(), "airplane_mode_on", 0) != 1) {
            z = false;
        }
        isAirplaneModeOn = z;
        number = PhoneNumberUtils.extractNetworkPortionAlt(number);
        String noSimAir = SystemProperties.get("ro.config.dist_nosim_airplane", "false");
        if (number == null || !"true".equals(noSimAir)) {
            return false;
        }
        if ((number.equals("110") || number.equals("119")) && isAirplaneModeOn) {
            return true;
        }
        return false;
    }

    public static boolean isCustomProcess() {
        return "true".equals(System.getProperty("custom_number_formatter", "false"));
    }

    public static String stripBrackets(String number) {
        if (TextUtils.isEmpty(number)) {
            return number;
        }
        StringBuilder result = new StringBuilder();
        int numLenght = number.length();
        int i = 0;
        while (i < numLenght) {
            if (!(number.charAt(i) == '(' || number.charAt(i) == ')')) {
                result.append(number.charAt(i));
            }
            i++;
        }
        return result.toString();
    }

    public static boolean isRemoveSeparateOnSK() {
        return SystemProperties.get("ro.config.noFormateCountry", "").contains(Locale.getDefault().getCountry());
    }

    public static String removeAllSeparate(String number) {
        if (TextUtils.isEmpty(number)) {
            return number;
        }
        StringBuilder result = new StringBuilder();
        int numLenght = number.length();
        for (int i = 0; i < numLenght; i++) {
            if (PhoneNumberUtils.isNonSeparator(number.charAt(i))) {
                result.append(number.charAt(i));
            }
        }
        return result.toString();
    }

    public static int getNewRememberedPos(int rememberedPos, String formatted) {
        if (TextUtils.isEmpty(formatted)) {
            return rememberedPos;
        }
        int numSeparate = 0;
        int formattedLength = formatted.length();
        int i = 0;
        while (i < rememberedPos && i < formattedLength) {
            if (!PhoneNumberUtils.isNonSeparator(formatted.charAt(i))) {
                numSeparate++;
            }
            i++;
        }
        return rememberedPos - numSeparate;
    }

    public static boolean isCustRemoveSep() {
        return "true".equals(SystemProperties.get("ro.config.number_remove_sep", "false"));
    }

    private static boolean isLongVoiceMailNumber(String number) {
        boolean z = false;
        if (!IS_SUPPORT_LONG_VMNUM) {
            return false;
        }
        String vmNumberSub2 = "";
        try {
            String vmNumber;
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                vmNumber = SystemProperties.get("gsm.hw.cust.longvmnum0", "");
                vmNumberSub2 = SystemProperties.get("gsm.hw.cust.longvmnum1", "");
            } else {
                vmNumber = SystemProperties.get(HwTelephonyProperties.PROPERTY_CUST_LONG_VMNUM, "");
            }
            if (TextUtils.isEmpty(vmNumber) && TextUtils.isEmpty(vmNumberSub2)) {
                return false;
            }
            number = PhoneNumberUtils.extractNetworkPortionAlt(number);
            if (!TextUtils.isEmpty(number)) {
                z = !number.equals(vmNumber) ? number.equals(vmNumberSub2) : true;
            }
            return z;
        } catch (SecurityException e) {
            return false;
        }
    }

    public static boolean isLongVoiceMailNumber(int subId, String number) {
        boolean z = false;
        if (!IS_SUPPORT_LONG_VMNUM) {
            return false;
        }
        try {
            String vmNumber;
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                vmNumber = SystemProperties.get(HwTelephonyProperties.PROPERTY_CUST_LONG_VMNUM + subId, "");
            } else {
                vmNumber = SystemProperties.get(HwTelephonyProperties.PROPERTY_CUST_LONG_VMNUM, "");
            }
            if (TextUtils.isEmpty(vmNumber)) {
                return false;
            }
            number = PhoneNumberUtils.extractNetworkPortionAlt(number);
            if (!TextUtils.isEmpty(number)) {
                z = number.equals(vmNumber);
            }
            return z;
        } catch (SecurityException e) {
            return false;
        }
    }
}

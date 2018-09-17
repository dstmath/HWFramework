package android.telephony;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.text.TextUtils;
import com.android.internal.telephony.HwTelephonyProperties;
import huawei.android.telephony.wrapper.WrapperFactory;
import java.util.Locale;

public class HwPhoneNumberUtils {
    private static final boolean IS_SUPPORT_LONG_VMNUM = false;
    private static final String LOG_TAG = "HwPhoneNumberUtils";
    private static boolean isAirplaneModeOn;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.HwPhoneNumberUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.HwPhoneNumberUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.HwPhoneNumberUtils.<clinit>():void");
    }

    public static String custExtraNumbers(long subId, String numbers) {
        String custNumbers;
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            custNumbers = SystemProperties.get(HwTelephonyProperties.PROPERTY_GLOBAL_CUST_ECCLIST + SubscriptionManager.getSlotId((int) subId), "");
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
            return IS_SUPPORT_LONG_VMNUM;
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
                z = IS_SUPPORT_LONG_VMNUM;
            } else if (!PhoneNumberUtils.compare(number, vmNumber)) {
                z = PhoneNumberUtils.compare(number, vmNumberSub2);
            }
            return z;
        } catch (SecurityException e) {
            return IS_SUPPORT_LONG_VMNUM;
        }
    }

    public static boolean useVoiceMailNumberFeature() {
        return true;
    }

    public static boolean isHwCustNotEmergencyNumber(Context mContext, String number) {
        boolean z = true;
        if (System.getInt(mContext.getContentResolver(), "airplane_mode_on", 0) != 1) {
            z = IS_SUPPORT_LONG_VMNUM;
        }
        isAirplaneModeOn = z;
        number = PhoneNumberUtils.extractNetworkPortionAlt(number);
        String noSimAir = SystemProperties.get("ro.config.dist_nosim_airplane", "false");
        if (number == null || !"true".equals(noSimAir)) {
            return IS_SUPPORT_LONG_VMNUM;
        }
        if ((number.equals("110") || number.equals("119")) && isAirplaneModeOn) {
            return true;
        }
        return IS_SUPPORT_LONG_VMNUM;
    }

    public static boolean isCustomProcess() {
        return "true".equals(System.getProperty("custom_number_formatter", "false"));
    }

    public static String stripBrackets(String number) {
        if (TextUtils.isEmpty(number)) {
            return number;
        }
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < number.length()) {
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
        for (int i = 0; i < number.length(); i++) {
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
        int i = 0;
        while (i < rememberedPos && i < formatted.length()) {
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
        boolean z = IS_SUPPORT_LONG_VMNUM;
        if (!IS_SUPPORT_LONG_VMNUM) {
            return IS_SUPPORT_LONG_VMNUM;
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
                return IS_SUPPORT_LONG_VMNUM;
            }
            number = PhoneNumberUtils.extractNetworkPortionAlt(number);
            if (!TextUtils.isEmpty(number)) {
                z = !number.equals(vmNumber) ? number.equals(vmNumberSub2) : true;
            }
            return z;
        } catch (SecurityException e) {
            return IS_SUPPORT_LONG_VMNUM;
        }
    }

    public static boolean isLongVoiceMailNumber(int subId, String number) {
        boolean z = IS_SUPPORT_LONG_VMNUM;
        if (!IS_SUPPORT_LONG_VMNUM) {
            return IS_SUPPORT_LONG_VMNUM;
        }
        try {
            String vmNumber;
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                vmNumber = SystemProperties.get(HwTelephonyProperties.PROPERTY_CUST_LONG_VMNUM + subId, "");
            } else {
                vmNumber = SystemProperties.get(HwTelephonyProperties.PROPERTY_CUST_LONG_VMNUM, "");
            }
            if (TextUtils.isEmpty(vmNumber)) {
                return IS_SUPPORT_LONG_VMNUM;
            }
            number = PhoneNumberUtils.extractNetworkPortionAlt(number);
            if (!TextUtils.isEmpty(number)) {
                z = number.equals(vmNumber);
            }
            return z;
        } catch (SecurityException e) {
            return IS_SUPPORT_LONG_VMNUM;
        }
    }
}

package android.telephony;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import com.android.internal.telephony.HwTelephonyProperties;
import huawei.android.provider.HwSettings;
import huawei.android.telephony.wrapper.WrapperFactory;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;
import java.util.Locale;

public class HwPhoneNumberUtils {
    public static final int CCWA_NUMBER_INTERNATIONAL = 1;
    private static final boolean IS_SUPPORT_LONG_VMNUM = SystemProperties.getBoolean("ro.config.hw_support_long_vmNum", false);
    private static final String LOG_TAG = "HwPhoneNumberUtils";
    public static final int TOA_International = 145;
    public static final int TOA_Unknown = 129;
    private static boolean isAirplaneModeOn = false;
    private static final ArrayList<MccNumberMatch> table = initMccMatchTable();

    static class MccNumberMatch {
        private String mCc;
        private String mIdd;
        private int mMcc;
        private String mNdd;
        private String[] mSpcs;

        MccNumberMatch(int mcc, String idd, String cc, String ndd) {
            this.mMcc = mcc;
            this.mIdd = idd;
            this.mCc = cc;
            this.mNdd = ndd;
        }

        MccNumberMatch(int mcc, String idd, String cc, String ndd, String spcList) {
            this.mMcc = mcc;
            this.mIdd = idd;
            this.mCc = cc;
            this.mNdd = ndd;
            if (spcList != null) {
                this.mSpcs = spcList.split(",");
            }
        }

        public int getMcc() {
            return this.mMcc;
        }

        public String getIdd() {
            return this.mIdd;
        }

        public String getCc() {
            return this.mCc;
        }

        public String getNdd() {
            return this.mNdd;
        }

        public String[] getSpcs() {
            return this.mSpcs;
        }
    }

    public static String custExtraNumbers(long subId, String numbers) {
        String custNumbers;
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            int slotId = SubscriptionManager.getSlotIndex((int) subId);
            custNumbers = SystemProperties.get(HwTelephonyProperties.PROPERTY_GLOBAL_CUST_ECCLIST + slotId, "");
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
        String vmNumber;
        boolean z = true;
        if (isLongVoiceMailNumber(number)) {
            return true;
        }
        String vmNumberSub2 = "";
        try {
            if (WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
                vmNumber = WrapperFactory.getMSimTelephonyManagerWrapper().getVoiceMailNumber(0);
                vmNumberSub2 = WrapperFactory.getMSimTelephonyManagerWrapper().getVoiceMailNumber(1);
            } else {
                vmNumber = TelephonyManager.getDefault().getVoiceMailNumber();
            }
            String number2 = PhoneNumberUtils.extractNetworkPortionAlt(number);
            if (TextUtils.isEmpty(number2) || (!PhoneNumberUtils.compare(number2, vmNumber) && !PhoneNumberUtils.compare(number2, vmNumberSub2))) {
                z = false;
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
        if (Settings.System.getInt(mContext.getContentResolver(), "airplane_mode_on", 0) != 1) {
            z = false;
        }
        isAirplaneModeOn = z;
        String number2 = PhoneNumberUtils.extractNetworkPortionAlt(number);
        String noSimAir = SystemProperties.get("ro.config.dist_nosim_airplane", "false");
        if (number2 == null || !"true".equals(noSimAir)) {
            return false;
        }
        if ((number2.equals("110") || number2.equals("119")) && isAirplaneModeOn) {
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
        for (int i = 0; i < numLenght; i++) {
            if (!(number.charAt(i) == '(' || number.charAt(i) == ')')) {
                result.append(number.charAt(i));
            }
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
        String vmNumber;
        boolean z = false;
        if (!getHwSupportLongVmnum()) {
            return false;
        }
        String vmNumberSub2 = "";
        try {
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                vmNumber = SystemProperties.get("gsm.hw.cust.longvmnum0", "");
                vmNumberSub2 = SystemProperties.get("gsm.hw.cust.longvmnum1", "");
            } else {
                vmNumber = SystemProperties.get(HwTelephonyProperties.PROPERTY_CUST_LONG_VMNUM, "");
            }
            if (TextUtils.isEmpty(vmNumber) && TextUtils.isEmpty(vmNumberSub2)) {
                return false;
            }
            String number2 = PhoneNumberUtils.extractNetworkPortionAlt(number);
            if (!TextUtils.isEmpty(number2) && (number2.equals(vmNumber) || number2.equals(vmNumberSub2))) {
                z = true;
            }
            return z;
        } catch (SecurityException e) {
            return false;
        }
    }

    public static boolean isLongVoiceMailNumber(int subId, String number) {
        String vmNumber;
        boolean z = false;
        if (!getHwSupportLongVmnum(subId)) {
            return false;
        }
        try {
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                vmNumber = SystemProperties.get(HwTelephonyProperties.PROPERTY_CUST_LONG_VMNUM + subId, "");
            } else {
                vmNumber = SystemProperties.get(HwTelephonyProperties.PROPERTY_CUST_LONG_VMNUM, "");
            }
            if (TextUtils.isEmpty(vmNumber)) {
                return false;
            }
            String number2 = PhoneNumberUtils.extractNetworkPortionAlt(number);
            if (!TextUtils.isEmpty(number2) && number2.equals(vmNumber)) {
                z = true;
            }
            return z;
        } catch (SecurityException e) {
            return false;
        }
    }

    private static boolean getHwSupportLongVmnum() {
        boolean result = IS_SUPPORT_LONG_VMNUM;
        Boolean valueFromCard1 = (Boolean) HwCfgFilePolicy.getValue("hw_support_long_vmNum", 0, Boolean.class);
        boolean z = true;
        if (valueFromCard1 != null) {
            result = result || valueFromCard1.booleanValue();
        }
        Boolean valueFromCard2 = (Boolean) HwCfgFilePolicy.getValue("hw_support_long_vmNum", 1, Boolean.class);
        if (valueFromCard2 != null) {
            if (!result && !valueFromCard2.booleanValue()) {
                z = false;
            }
            result = z;
        }
        Rlog.d(LOG_TAG, "getHwSupportLongVmnum, card1:" + valueFromCard1 + "card2:" + valueFromCard2 + ", prop:" + valueFromProp);
        return result;
    }

    private static boolean getHwSupportLongVmnum(int subId) {
        Boolean valueFromCard = (Boolean) HwCfgFilePolicy.getValue("hw_support_long_vmNum", subId, Boolean.class);
        boolean valueFromProp = IS_SUPPORT_LONG_VMNUM;
        Rlog.d(LOG_TAG, "getHwSupportLongVmnum, subId:" + subId + ", card:" + valueFromCard + ", prop:" + valueFromProp);
        return valueFromCard != null ? valueFromCard.booleanValue() : valueFromProp;
    }

    private static ArrayList<MccNumberMatch> initMccMatchTable() {
        ArrayList<MccNumberMatch> tempTable = new ArrayList<>();
        MccNumberMatch mccNumberMatch = new MccNumberMatch(460, "00", "86", HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_OFF, "13,15,18,17,14,10649");
        tempTable.add(mccNumberMatch);
        MccNumberMatch mccNumberMatch2 = new MccNumberMatch(404, "00", "91", HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_OFF, "99");
        tempTable.add(mccNumberMatch2);
        return tempTable;
    }

    private static MccNumberMatch getRecordByMcc(int mcc) {
        int iTableSize = table.size();
        for (int i = 0; i < iTableSize; i++) {
            if (mcc == table.get(i).getMcc()) {
                return table.get(i);
            }
        }
        return null;
    }

    public static String convertPlusByMcc(String number, int mcc) {
        MccNumberMatch record = getRecordByMcc(mcc);
        StringBuilder strBuilder = new StringBuilder();
        if (record == null || number == null || !number.startsWith("+") || !number.startsWith(record.getCc(), 1)) {
            return number;
        }
        String realNum = number.substring(1 + record.getCc().length());
        if (!beginWith(realNum, record.getSpcs())) {
            strBuilder.append(record.getNdd());
        }
        strBuilder.append(realNum);
        return strBuilder.toString();
    }

    public static boolean isMobileNumber(String number, int mcc) {
        MccNumberMatch record = getRecordByMcc(mcc);
        if (record == null || number == null || number.length() == 0) {
            return false;
        }
        boolean isMobileNum = false;
        int spcIndex = 0;
        if (number.startsWith("+") && number.startsWith(record.getCc(), 1)) {
            spcIndex = 1 + record.getCc().length();
        } else if (number.startsWith(record.getIdd()) && number.startsWith(record.getCc(), record.getIdd().length())) {
            spcIndex = record.getIdd().length() + record.getCc().length();
        }
        if (beginWith(number.substring(spcIndex), record.getSpcs())) {
            isMobileNum = true;
        }
        Rlog.d(LOG_TAG, "plus: isMobileNumber = " + isMobileNum);
        return isMobileNum;
    }

    private static boolean beginWith(String wholeStr, String[] subStrs) {
        if (wholeStr == null || subStrs == null || subStrs.length <= 0) {
            return false;
        }
        for (int i = 0; i < subStrs.length; i++) {
            if (subStrs[i].length() > 0 && wholeStr.startsWith(subStrs[i])) {
                return true;
            }
        }
        return false;
    }

    public static int getToaFromNumberType(int numType) {
        if (1 == numType) {
            return 145;
        }
        return 129;
    }
}

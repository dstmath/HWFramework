package android.telephony;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import com.android.internal.telephony.HwTelephonyProperties;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.PhoneNumberUtilsEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephony.BuildConfig;
import huawei.android.telephony.wrapper.WrapperFactory;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;
import java.util.Locale;

public class HwPhoneNumberUtils {
    public static final int CCWA_NUMBER_INTERNATIONAL = 1;
    private static final String LOG_TAG = "HwPhoneNumberUtils";
    public static final int TOA_International = 145;
    public static final int TOA_Unknown = 129;
    private static boolean isAirplaneModeOn = false;
    private static final ArrayList<MccNumberMatch> table = initMccMatchTable();

    public static String custExtraNumbers(long subId, String numbers) {
        String custNumbers;
        if (TelephonyManagerEx.isMultiSimEnabled()) {
            int slotId = SubscriptionManagerEx.getSlotIndex((int) subId);
            custNumbers = SystemPropertiesEx.get(HwTelephonyProperties.PROPERTY_GLOBAL_CUST_ECCLIST + slotId, BuildConfig.FLAVOR);
        } else {
            custNumbers = SystemPropertiesEx.get(HwTelephonyProperties.PROPERTY_GLOBAL_CUST_ECCLIST, BuildConfig.FLAVOR);
        }
        if (TextUtils.isEmpty(custNumbers)) {
            return numbers;
        }
        return numbers + "," + custNumbers;
    }

    public static boolean skipHardcodeNumbers() {
        if (WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled() || TelephonyManagerEx.getDefault().getPhoneType() != 2) {
            return false;
        }
        return true;
    }

    public static boolean isVoiceMailNumber(String number) {
        String vmNumber;
        if (isLongVoiceMailNumberForSlotId(0, number) || isLongVoiceMailNumberForSlotId(1, number)) {
            return true;
        }
        String vmNumberSub2 = BuildConfig.FLAVOR;
        try {
            if (WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
                vmNumber = WrapperFactory.getMSimTelephonyManagerWrapper().getVoiceMailNumber(0);
                vmNumberSub2 = WrapperFactory.getMSimTelephonyManagerWrapper().getVoiceMailNumber(1);
            } else {
                vmNumber = TelephonyManagerEx.getDefault().getVoiceMailNumber();
            }
            String number2 = PhoneNumberUtilsEx.extractNetworkPortionAlt(number);
            if (!TextUtils.isEmpty(number2)) {
                return PhoneNumberUtils.compare(number2, vmNumber) || PhoneNumberUtils.compare(number2, vmNumberSub2);
            }
            return false;
        } catch (SecurityException e) {
            return false;
        }
    }

    public static boolean useVoiceMailNumberFeature() {
        return true;
    }

    public static boolean isHwCustNotEmergencyNumber(Context context, String number) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        if (Settings.System.getInt(context.getContentResolver(), "airplane_mode_on", 0) == 1) {
            z = true;
        }
        isAirplaneModeOn = z;
        String number2 = PhoneNumberUtilsEx.extractNetworkPortionAlt(number);
        String noSimAir = SystemPropertiesEx.get("ro.config.dist_nosim_airplane", "false");
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
        return SystemPropertiesEx.get("ro.config.noFormateCountry", BuildConfig.FLAVOR).contains(Locale.getDefault().getCountry());
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
        return "true".equals(SystemPropertiesEx.get("ro.config.number_remove_sep", "false"));
    }

    private static boolean isLongVoiceMailNumberForSlotId(int slotId, String number) {
        String number2 = PhoneNumberUtilsEx.extractNetworkPortionAlt(number);
        return !TextUtils.isEmpty(number2) && number2.equals(HwCfgFilePolicy.getValue("hw_cust_long_vmNum", slotId, String.class));
    }

    public static boolean isLongVoiceMailNumber(int subId, String number) {
        return isLongVoiceMailNumberForSlotId(SubscriptionManagerEx.getSlotIndex(subId), number);
    }

    private static ArrayList<MccNumberMatch> initMccMatchTable() {
        ArrayList<MccNumberMatch> tempTable = new ArrayList<>();
        tempTable.add(new MccNumberMatch(460, "00", "86", "0", "13,15,18,17,14,10649"));
        tempTable.add(new MccNumberMatch(404, "00", "91", "0", "99"));
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
        String realNum = number.substring(record.getCc().length() + 1);
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
            spcIndex = record.getCc().length() + 1;
        } else if (!number.startsWith(record.getIdd()) || !number.startsWith(record.getCc(), record.getIdd().length())) {
            RlogEx.d(LOG_TAG, "spcIndex = 0");
        } else {
            spcIndex = record.getIdd().length() + record.getCc().length();
        }
        if (beginWith(number.substring(spcIndex), record.getSpcs())) {
            isMobileNum = true;
        }
        RlogEx.d(LOG_TAG, "plus: isMobileNumber = " + isMobileNum);
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
            return TOA_International;
        }
        return TOA_Unknown;
    }

    /* access modifiers changed from: package-private */
    public static class MccNumberMatch {
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
}

package ohos.telephony;

import android.content.res.Resources;
import android.telephony.CallerInfoHW;
import android.telephony.PhoneNumberUtils;
import ohos.data.dataability.ContentProviderConverter;
import ohos.data.resultset.ResultSet;

public final class TelephonyAdapt {
    private static final String EMPTY_STRING = "";
    private static final int INVALID_INDEX = -1;

    public static boolean hasSmsCapbility() {
        Resources system = Resources.getSystem();
        if (system != null) {
            return system.getBoolean(17891524);
        }
        return true;
    }

    public static boolean isVoiceCap() {
        Resources system = Resources.getSystem();
        if (system != null) {
            return system.getBoolean(17891573);
        }
        return true;
    }

    public static String formatPhoneNumber(String str, String str2) {
        return str == null ? "" : PhoneNumberUtils.formatNumber(str, str2);
    }

    public static String formatPhoneNumber(String str, String str2, String str3) {
        return str == null ? "" : PhoneNumberUtils.formatNumber(str, str2, str3);
    }

    public static String formatPhoneNumberToE164(String str, String str2) {
        return str == null ? "" : PhoneNumberUtils.formatNumberToE164(str, str2);
    }

    public static String getCountryIsoFromDbNumber(String str) {
        CallerInfoHW instance = CallerInfoHW.getInstance();
        return instance != null ? instance.getCountryIsoFromDbNumber(str) : "";
    }

    public static int getCallerIndex(ResultSet resultSet, String str) {
        CallerInfoHW instance = CallerInfoHW.getInstance();
        if (instance == null || resultSet == null) {
            return -1;
        }
        return instance.getCallerIndex(ContentProviderConverter.resultSetToCursor(resultSet), str);
    }
}

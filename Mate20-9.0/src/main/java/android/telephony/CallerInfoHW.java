package android.telephony;

import android.database.Cursor;

public class CallerInfoHW {
    public static final int MIN_MATCH = 7;
    private static final String TAG = "CallerInfo";
    private static final CallerInfoHW sInstance = new CallerInfoHW();

    public static synchronized CallerInfoHW getInstance() {
        CallerInfoHW callerInfoHW;
        synchronized (CallerInfoHW.class) {
            callerInfoHW = sInstance;
        }
        return callerInfoHW;
    }

    public String getCountryIsoFromDbNumber(String number) {
        return huawei.android.telephony.CallerInfoHW.getInstance().getCountryIsoFromDbNumber(number);
    }

    public int getIntlPrefixAndCCLen(String number) {
        return huawei.android.telephony.CallerInfoHW.getInstance().getIntlPrefixAndCCLen(number);
    }

    public boolean compareNums(String num1, String netIso1, String num2, String netIso2) {
        return huawei.android.telephony.CallerInfoHW.getInstance().compareNums(num1, netIso1, num2, netIso2);
    }

    public boolean compareNums(String num1, String num2) {
        return huawei.android.telephony.CallerInfoHW.getInstance().compareNums(num1, num2);
    }

    public int getCallerIndex(Cursor cursor, String compNum) {
        return huawei.android.telephony.CallerInfoHW.getInstance().getCallerIndex(cursor, compNum);
    }

    public int getCallerIndex(Cursor cursor, String compNum, String columnName) {
        return huawei.android.telephony.CallerInfoHW.getInstance().getCallerIndex(cursor, compNum, columnName);
    }

    public int getCallerIndex(Cursor cursor, String compNum, String columnName, String countryIso) {
        return huawei.android.telephony.CallerInfoHW.getInstance().getCallerIndex(cursor, compNum, columnName, countryIso);
    }
}

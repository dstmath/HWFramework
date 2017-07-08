package com.android.internal.http;

import android.text.format.Time;
import com.android.internal.telephony.AbstractRILConstants;
import com.android.internal.telephony.RILConstants;
import com.hisi.perfhub.PerfHub;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.indexsearch.IndexSearchConstants;
import com.huawei.pgmng.log.LogPower;
import com.huawei.pgmng.plug.PGSdk;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HttpDateTime {
    private static final Pattern HTTP_DATE_ANSIC_PATTERN = null;
    private static final String HTTP_DATE_ANSIC_REGEXP = "[ ]([A-Za-z]{3,9})[ ]+([0-9]{1,2})[ ]([0-9]{1,2}:[0-9][0-9]:[0-9][0-9])[ ]([0-9]{2,4})";
    private static final Pattern HTTP_DATE_RFC_PATTERN = null;
    private static final String HTTP_DATE_RFC_REGEXP = "([0-9]{1,2})[- ]([A-Za-z]{3,9})[- ]([0-9]{2,4})[ ]([0-9]{1,2}:[0-9][0-9]:[0-9][0-9])";

    private static class TimeOfDay {
        int hour;
        int minute;
        int second;

        TimeOfDay(int h, int m, int s) {
            this.hour = h;
            this.minute = m;
            this.second = s;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.http.HttpDateTime.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.http.HttpDateTime.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.http.HttpDateTime.<clinit>():void");
    }

    public static long parse(String timeString) throws IllegalArgumentException {
        int date;
        int month;
        int year;
        TimeOfDay timeOfDay;
        Matcher rfcMatcher = HTTP_DATE_RFC_PATTERN.matcher(timeString);
        if (rfcMatcher.find()) {
            date = getDate(rfcMatcher.group(1));
            month = getMonth(rfcMatcher.group(2));
            year = getYear(rfcMatcher.group(3));
            timeOfDay = getTime(rfcMatcher.group(4));
        } else {
            Matcher ansicMatcher = HTTP_DATE_ANSIC_PATTERN.matcher(timeString);
            if (ansicMatcher.find()) {
                month = getMonth(ansicMatcher.group(1));
                date = getDate(ansicMatcher.group(2));
                timeOfDay = getTime(ansicMatcher.group(3));
                year = getYear(ansicMatcher.group(4));
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (year >= AbstractRILConstants.RIL_REQUEST_HW_VSIM_GET_SIM_STATE) {
            year = AbstractRILConstants.RIL_REQUEST_HW_VSIM_GET_SIM_STATE;
            month = 0;
            date = 1;
        }
        Time time = new Time(Time.TIMEZONE_UTC);
        time.set(timeOfDay.second, timeOfDay.minute, timeOfDay.hour, date, month, year);
        return time.toMillis(false);
    }

    private static int getDate(String dateString) {
        if (dateString.length() == 2) {
            return ((dateString.charAt(0) - 48) * 10) + (dateString.charAt(1) - 48);
        }
        return dateString.charAt(0) - 48;
    }

    private static int getMonth(String monthString) {
        switch (((Character.toLowerCase(monthString.charAt(0)) + Character.toLowerCase(monthString.charAt(1))) + Character.toLowerCase(monthString.charAt(2))) - 291) {
            case PGSdk.TYPE_SCRLOCK /*9*/:
                return 11;
            case PGSdk.TYPE_CLOCK /*10*/:
                return 1;
            case HwPerformance.PERF_TAG_DEF_L_CPU_MAX /*22*/:
                return 0;
            case PerfHub.PERF_TAG_DEF_GPU_MAX /*26*/:
                return 7;
            case PerfHub.PERF_TAG_DEF_HMP_UP_THRES /*29*/:
                return 2;
            case IndexSearchConstants.INDEX_BUILD_FLAG_INTERNAL_FILE /*32*/:
                return 3;
            case PerfHub.PERF_TAG_AVL_B_CPU_FREQ_LIST /*35*/:
                return 9;
            case PerfHub.PERF_TAG_AVL_GPU_FREQ_LIST /*36*/:
                return 4;
            case PerfHub.PERF_TAG_AVL_DDR_FREQ_LIST /*37*/:
                return 8;
            case StatisticalConstant.TYPE_SINGLEHAND_START /*40*/:
                return 6;
            case StatisticalConstant.TYPE_SINGLEHAND_EXIT /*42*/:
                return 5;
            case IndexSearchConstants.INDEX_BUILD_FLAG_EXTERNAL_FILE /*48*/:
                return 10;
            default:
                throw new IllegalArgumentException();
        }
    }

    private static int getYear(String yearString) {
        if (yearString.length() == 2) {
            int year = ((yearString.charAt(0) - 48) * 10) + (yearString.charAt(1) - 48);
            if (year >= 70) {
                return year + 1900;
            }
            return year + LogPower.FIRST_IAWARE_TAG;
        } else if (yearString.length() == 3) {
            return ((((yearString.charAt(0) - 48) * 100) + ((yearString.charAt(1) - 48) * 10)) + (yearString.charAt(2) - 48)) + 1900;
        } else {
            if (yearString.length() == 4) {
                return ((((yearString.charAt(0) - 48) * RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED) + ((yearString.charAt(1) - 48) * 100)) + ((yearString.charAt(2) - 48) * 10)) + (yearString.charAt(3) - 48);
            }
            return 1970;
        }
    }

    private static TimeOfDay getTime(String timeString) {
        int i = 1;
        int hour = timeString.charAt(0) - 48;
        if (timeString.charAt(1) != ':') {
            hour = (hour * 10) + (timeString.charAt(1) - 48);
            i = 1 + 1;
        }
        i++;
        int i2 = i + 1;
        i = (i2 + 1) + 1;
        i2 = i + 1;
        i = i2 + 1;
        return new TimeOfDay(hour, ((timeString.charAt(i) - 48) * 10) + (timeString.charAt(i2) - 48), ((timeString.charAt(i) - 48) * 10) + (timeString.charAt(i2) - 48));
    }
}

package com.huawei.wallet.sdk.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import com.huawei.wallet.sdk.common.log.LogC;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@SuppressLint({"SimpleDateFormat"})
public class TimeUtil {
    public static final String MONTH_TO_SECOND_NO_LINE = "MMddHHmmss";
    public static final String YEAR_TO_DATE = "yyyy-MM-dd";
    public static final String YEAR_TO_MONTH = "yyyyMM";
    public static final String YEAR_TO_MSEL = "yyyy-MM-dd-hh-mm-ss-SSS";
    public static final String YEAR_TO_MSEL_FOR_SPLASH = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String YEAR_TO_MSEL_NO_LINE = "yyyyMMddHHmmssSSS";
    public static final String YEAR_TO_SECOND_24 = "yyyy-MM-dd HH:mm:ss";
    public static final String YEAR_TO_SECOND_NO_LINE = "yyyyMMddHHmmss";
    public static final String YEAR_TO_SECOND_WITH_SPRIT_NO_ZERO = "yyyy/M/d HH:mm:ss";

    public static String getFormatTime(String format) {
        SimpleDateFormat sdf;
        if (TextUtils.isEmpty(format)) {
            sdf = new SimpleDateFormat(YEAR_TO_DATE);
        } else {
            sdf = new SimpleDateFormat(format);
        }
        return sdf.format(new Date());
    }

    public static String creatTime() {
        return "" + System.currentTimeMillis();
    }

    public static Calendar getRigthTime(String timeFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(YEAR_TO_SECOND_24);
        if (TextUtils.isEmpty(timeFormat)) {
            return cal;
        }
        int month = -1;
        Date date = null;
        try {
            date = sdf.parse(timeFormat);
            month = date.getMonth();
        } catch (ParseException e) {
            LogC.e("date parse error.", false);
        }
        if (-1 < month && month < 13) {
            cal.setTime(date);
        }
        return cal;
    }

    public static String getRechargeFormatTime(String timeFormat, Context context) {
        SimpleDateFormat sdf;
        if (timeFormat.length() > 15) {
            return getFormatRigthTime(timeFormat, context);
        }
        Calendar cal = getWithoutYearRightTime(timeFormat);
        Locale newLocale = context.getResources().getConfiguration().locale;
        String locale = newLocale.toString();
        if (locale.equals("it_IT")) {
            sdf = new SimpleDateFormat("dd/MM HH:mm:ss");
        } else if (locale.equals("en_US")) {
            sdf = new SimpleDateFormat("MM/dd HH:mm:ss");
        } else if (locale.equals("ru_RU")) {
            sdf = new SimpleDateFormat("dd/MM HH:mm:ss");
        } else if (newLocale.getLanguage().equalsIgnoreCase("en")) {
            sdf = new SimpleDateFormat("dd/MM HH:mm:ss");
        } else if (newLocale.getLanguage().equalsIgnoreCase("zh")) {
            sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
        } else {
            sdf = new SimpleDateFormat("MM/dd HH:mm:ss");
        }
        return sdf.format(cal.getTime());
    }

    public static Calendar getWithoutYearRightTime(String timeFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
        if (TextUtils.isEmpty(timeFormat)) {
            return cal;
        }
        int month = -1;
        Date date = null;
        try {
            date = sdf.parse(timeFormat);
            month = date.getMonth();
        } catch (ParseException e) {
            LogC.e("date parse error.", false);
        }
        if (-1 < month && month < 13) {
            cal.setTime(date);
        }
        return cal;
    }

    public static String getToAccountTime(String tradeTime, boolean bSameFormate) {
        long timeStart;
        SimpleDateFormat formatter = new SimpleDateFormat(YEAR_TO_SECOND_24);
        long timeStart2 = -1;
        if (bSameFormate) {
            try {
                timeStart = formatter.parse(tradeTime).getTime();
            } catch (ParseException e) {
                LogC.e("getToAccountTime", (Throwable) e, false);
            }
        } else {
            timeStart = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(tradeTime).getTime();
        }
        timeStart2 = timeStart + 86400000;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStart2);
        String[] str = formatter.format(calendar.getTime()).split(" ");
        return str[0] + " 23:59:59";
    }

    public static Date parseString2Date(String dateStr, String format) {
        try {
            return new SimpleDateFormat(format).parse(dateStr);
        } catch (ParseException e) {
            LogC.e("parseDateStr ParseException dateStr : " + dateStr, false);
            return null;
        }
    }

    public static String formatDate2String(Date d, String format) {
        if (d == null) {
            return null;
        }
        return new SimpleDateFormat(format).format(d);
    }

    public static String formatDate2StringZh(Date d, String format) {
        if (d == null) {
            return null;
        }
        return new SimpleDateFormat(format, Locale.CHINA).format(d);
    }

    public static String parseFormatString2String(String date, String format, String newFormat) {
        Date d = parseString2Date(date, format);
        if (d == null) {
            return date;
        }
        return formatDate2String(d, newFormat);
    }

    public static String getFormatRigthTime(String timeFormat, Context context) {
        SimpleDateFormat sdf;
        Calendar cal = getRigthTime(timeFormat);
        Locale newLocale = context.getResources().getConfiguration().locale;
        String locale = newLocale.toString();
        if (locale.equals("it_IT")) {
            sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        } else if (locale.equals("en_US")) {
            sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        } else if (locale.equals("ru_RU")) {
            sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        } else if (newLocale.getLanguage().equalsIgnoreCase("en")) {
            sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        } else if (newLocale.getLanguage().equalsIgnoreCase("zh")) {
            sdf = new SimpleDateFormat(YEAR_TO_SECOND_24);
        } else {
            sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        }
        return sdf.format(cal.getTime());
    }

    private static Calendar getCalTime(String date) {
        Calendar time = Calendar.getInstance();
        if (!TextUtils.isEmpty(date)) {
            String[] strs = date.split("/");
            if (strs.length == 3) {
                try {
                    time.set(1, Integer.parseInt(strs[0]));
                    time.set(2, Integer.parseInt(strs[1]) - 1);
                    time.set(5, Integer.parseInt(strs[2]));
                } catch (NumberFormatException e) {
                    LogC.e("date parse error.", false);
                }
            }
        }
        return time;
    }

    public static String getValidTime(String date, Context context) {
        SimpleDateFormat sdf;
        Calendar mCalendar = getCalTime(date);
        Locale newLocale = context.getResources().getConfiguration().locale;
        String locale = newLocale.toString();
        if (locale.equals("it_IT")) {
            sdf = new SimpleDateFormat("dd/MM/yyyy");
        } else if (locale.equals("en_US")) {
            sdf = new SimpleDateFormat("MM/dd/yyyy");
        } else if (locale.equals("ru_RU")) {
            sdf = new SimpleDateFormat("dd/MM/yyyy");
        } else if (newLocale.getLanguage().equalsIgnoreCase("en")) {
            sdf = new SimpleDateFormat("dd/MM/yyyy");
        } else if (newLocale.getLanguage().equalsIgnoreCase("zh")) {
            sdf = new SimpleDateFormat(YEAR_TO_DATE);
        } else {
            sdf = new SimpleDateFormat("yyyy/MM/dd");
        }
        return sdf.format(mCalendar.getTime());
    }
}

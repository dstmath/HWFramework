package com.huawei.i18n.tmr.datetime.utils;

import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.i18n.tmr.datetime.data.LocaleParam;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataConvertTool {
    private static String getParamWithoutB(String content, String locale) {
        return new LocaleParam(locale).getWithoutB(content);
    }

    public static String replace(String content, String locale, String localeBackup) {
        return replace(replace(content, locale), localeBackup);
    }

    private static String replace(String str, String locale) {
        String content = str;
        StringBuffer sb = new StringBuffer();
        String lam = getParamWithoutB("param_am", locale);
        if (lam != null && !lam.trim().isEmpty()) {
            sb.append(lam);
            sb.append("|");
        }
        sb.append(getParamWithoutB("param_am", "en"));
        Matcher matcher = Pattern.compile(sb.toString(), 2).matcher(content);
        while (matcher.find()) {
            content = content.replace(matcher.group(), " am ");
        }
        StringBuffer sb2 = new StringBuffer();
        String lpm = getParamWithoutB("param_pm", locale);
        if (lpm != null && !lpm.trim().isEmpty()) {
            sb2.append(lpm);
            sb2.append("|");
        }
        sb2.append(getParamWithoutB("param_pm", "en"));
        Matcher matcherAmPm = Pattern.compile(sb2.toString(), 2).matcher(content);
        while (matcherAmPm.find()) {
            content = content.replace(matcherAmPm.group(), " pm ");
        }
        StringBuffer stringBuffer = new StringBuffer();
        String lmm = getParamWithoutB("param_mm", locale);
        if (lpm != null && !lmm.trim().isEmpty()) {
            stringBuffer.append(lmm);
            stringBuffer.append("|");
        }
        stringBuffer.append(getParamWithoutB("param_mm", "en"));
        Matcher mmpmMatcher = Pattern.compile(stringBuffer.toString(), 2).matcher(content);
        while (mmpmMatcher.find()) {
            content = content.replace(mmpmMatcher.group(), " mm ");
        }
        return content;
    }

    public static int convertE(String content, String locale, String localeBackup) {
        int week = convertE(content, locale);
        if (week == -1) {
            return convertE(content, localeBackup);
        }
        return week;
    }

    private static int convertE(String str, String locale) {
        String content = str.replaceAll("\\s+", "\\\\s+").replaceAll("\\.", "\\\\.");
        if (content.trim().isEmpty()) {
            return -1;
        }
        String paramE = getParamWithoutB("param_E", locale);
        if ("be".equals(locale) || "kk".equals(locale)) {
            paramE = paramE.replaceAll("\\\\b", StorageManagerExt.INVALID_KEY_DESC);
        }
        if (paramE != null && !paramE.trim().isEmpty()) {
            String[] weeks = paramE.trim().split("\\|");
            for (int i = 0; i < weeks.length; i++) {
                if (content.equalsIgnoreCase(weeks[i].trim())) {
                    if (i == 0) {
                        return 6;
                    } else {
                        return i - 1;
                    }
                }
            }
        }
        String weekStr = getParamWithoutB("param_EEEE", locale);
        if (weekStr != null && !weekStr.trim().isEmpty()) {
            String[] weekArr = weekStr.trim().split("\\|");
            for (int i2 = 0; i2 < weekArr.length; i2++) {
                if (content.equalsIgnoreCase(weekArr[i2].trim())) {
                    if (i2 == 0) {
                        return 6;
                    } else {
                        return i2 - 1;
                    }
                }
            }
        }
        return convertEofBelarus(locale, content, -1);
    }

    private static int convertEofBelarus(String locale, String content, int week) {
        String specialWeek = getParamWithoutB("param_E2", locale);
        if (specialWeek != null && !specialWeek.trim().isEmpty()) {
            String[] weekArr = specialWeek.trim().split("\\|");
            for (int i = 0; i < weekArr.length; i++) {
                if (content.equalsIgnoreCase(weekArr[i].trim())) {
                    if (i == 0) {
                        return 6;
                    } else {
                        return i - 1;
                    }
                }
            }
        }
        return week;
    }

    public static int calRelDays(String content, String locale, String localeBackup) {
        int week = calRelDays(content, locale);
        if (week == -1) {
            return calRelDays(content, localeBackup);
        }
        return week;
    }

    private static int calRelDays(String content, String locale) {
        int result;
        String text = content.trim().replaceAll("\\s+", "\\\\s+").replaceAll("\\.", "\\\\.");
        String[] weeks = getParamWithoutB("param_thisweek", locale).split("\\|");
        String[] nweeks = getParamWithoutB("param_nextweek", locale).split("\\|");
        for (int i = 0; i < weeks.length; i++) {
            if (text.equalsIgnoreCase(weeks[i].trim())) {
                if (i == 0) {
                    return 6;
                } else {
                    return i - 1;
                }
            }
        }
        for (int i2 = 0; i2 < nweeks.length; i2++) {
            if (text.equalsIgnoreCase(nweeks[i2].trim())) {
                if (i2 == 0) {
                    result = 6;
                } else if (i2 == 7) {
                    result = 13;
                } else {
                    result = i2 - 1;
                }
                return result + 7;
            }
        }
        return -1;
    }

    public static int calTextDay(String content, String locale, String localeBackup) {
        int day = calTextDay(content, locale);
        if (day == -1) {
            return calTextDay(content, localeBackup);
        }
        return day;
    }

    private static int calTextDay(String text, String locale) {
        String[] days = getParamWithoutB("param_days", locale).split("\\|");
        int result = -1;
        for (int i = 0; i < days.length; i++) {
            if (Pattern.compile(days[i].trim(), 2).matcher(text).matches()) {
                result = i;
            }
        }
        return result;
    }

    public static int convertMMM(String monthStr, String locale, String localeBackup) {
        if (monthStr == null || locale == null) {
            return -1;
        }
        int num = convertMMM(monthStr, locale);
        if (num == -1) {
            return convertMMM(monthStr, localeBackup);
        }
        return num;
    }

    private static int convertMMM(String monthStr, String locale) {
        String mmm = monthStr.replaceAll("\\s+", "\\\\s+").replaceAll("\\.", "\\\\.");
        String mc = getParamWithoutB("param_MMM", locale);
        String mmc = getParamWithoutB("param_MMMM", locale);
        if (mc != null && !StorageManagerExt.INVALID_KEY_DESC.equals(mc.trim())) {
            String[] mcs = mc.split("\\|");
            for (int i = 0; i < mcs.length; i++) {
                if (mcs[i].trim().equalsIgnoreCase(mmm)) {
                    return i;
                }
            }
        }
        if (mmc != null && !StorageManagerExt.INVALID_KEY_DESC.equals(mmc.trim())) {
            String[] mcs2 = mmc.split("\\|");
            for (int i2 = 0; i2 < mcs2.length; i2++) {
                if (mcs2[i2].trim().equalsIgnoreCase(mmm)) {
                    return i2;
                }
            }
        }
        return -1;
    }

    public static int convertRelText(String monthStr, String locale, String name, String localeBackup) {
        int num = convertRelText(monthStr, locale, name);
        if (num == -1) {
            return convertRelText(monthStr, localeBackup, name);
        }
        return num;
    }

    private static int convertRelText(String monthStr, String locale, String name) {
        String mmm = monthStr.replaceAll("\\s+", "\\\\s+").replaceAll("\\.", "\\\\.");
        String mc = getParamWithoutB(name, locale);
        if (mc != null && !StorageManagerExt.INVALID_KEY_DESC.equals(mc.trim())) {
            String[] mcs = mc.split("\\|");
            int add = 0;
            for (int i = 0; i < mcs.length; i++) {
                if (i == 0 && mcs[i].trim().equals(LocaleParam.SAVE_OR)) {
                    add = -1;
                } else if (mcs[i].trim().equalsIgnoreCase(mmm)) {
                    return i + add;
                }
            }
        }
        return -1;
    }
}

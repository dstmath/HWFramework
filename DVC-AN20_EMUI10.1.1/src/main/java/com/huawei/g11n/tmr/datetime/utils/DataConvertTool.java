package com.huawei.g11n.tmr.datetime.utils;

import com.huawei.uikit.effect.BuildConfig;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataConvertTool {
    private static String getParamWithoutB(String pName, String locale) {
        return new LocaleParam(locale).getWithoutB(pName);
    }

    public static String replace(String content, String locale, String localeBk) {
        return replace(replace(content, locale), localeBk);
    }

    public static int convertE(String content, String locale, String localeBk) {
        int r = convertE(content, locale);
        if (r == -1) {
            return convertE(content, localeBk);
        }
        return r;
    }

    public static int calRelDays(String text, String locale, String localeBk) {
        int r = calRelDays(text, locale);
        if (r == -1) {
            return calRelDays(text, localeBk);
        }
        return r;
    }

    public static int calTextDay(String text, String locale, String localeBk) {
        int r = calTextDay(text, locale);
        if (r == -1) {
            return calTextDay(text, localeBk);
        }
        return r;
    }

    public static int convertMMM(String mstr, String locale, String localeBk) {
        if (mstr == null || locale == null) {
            return -1;
        }
        int r = convertMMM(mstr, locale);
        if (r == -1) {
            return convertMMM(mstr, localeBk);
        }
        return r;
    }

    public static int convertRelText(String mstr, String locale, String name, String localeBk) {
        int r = convertRelText(mstr, locale, name);
        if (r == -1) {
            return convertRelText(mstr, localeBk, name);
        }
        return r;
    }

    private static String replace(String content, String locale) {
        StringBuffer sb = new StringBuffer();
        String lam = getParamWithoutB("param_am", locale);
        if (lam != null && !lam.trim().isEmpty()) {
            sb.append(lam);
            sb.append("|");
        }
        sb.append(getParamWithoutB("param_am", "en"));
        String content2 = content;
        Matcher m = Pattern.compile(sb.toString(), 2).matcher(content2);
        while (m.find()) {
            content2 = content2.replace(m.group(), " am ");
        }
        StringBuffer sb2 = new StringBuffer();
        String lpm = getParamWithoutB("param_pm", locale);
        if (lpm != null && !lpm.trim().isEmpty()) {
            sb2.append(lpm);
            sb2.append("|");
        }
        sb2.append(getParamWithoutB("param_pm", "en"));
        Matcher m2 = Pattern.compile(sb2.toString(), 2).matcher(content2);
        while (m2.find()) {
            content2 = content2.replace(m2.group(), " pm ");
        }
        StringBuffer sb3 = new StringBuffer();
        String lmm = getParamWithoutB("param_mm", locale);
        if (lpm != null && !lmm.trim().isEmpty()) {
            sb3.append(lmm);
            sb3.append("|");
        }
        sb3.append(getParamWithoutB("param_mm", "en"));
        Matcher m3 = Pattern.compile(sb3.toString(), 2).matcher(content2);
        while (m3.find()) {
            content2 = content2.replace(m3.group(), " mm ");
        }
        return content2;
    }

    private static int convertE(String content, String locale) {
        String content2 = content.replaceAll("\\s+", "\\\\s+").replaceAll("\\.", "\\\\.");
        if (content2.trim().isEmpty()) {
            return -1;
        }
        String e = getParamWithoutB("param_E", locale);
        if (locale.equals("be") || locale.equals("kk")) {
            e = e.replaceAll("\\\\b", BuildConfig.FLAVOR);
        }
        if (e != null && !e.trim().isEmpty()) {
            String[] a = e.trim().split("\\|");
            for (int i = 0; i < a.length; i++) {
                if (content2.equalsIgnoreCase(a[i].trim())) {
                    if (i == 0) {
                        return 6;
                    } else {
                        return i - 1;
                    }
                }
            }
        }
        String ee = getParamWithoutB("param_EEEE", locale);
        if (ee != null && !ee.trim().isEmpty()) {
            String[] a2 = ee.trim().split("\\|");
            for (int i2 = 0; i2 < a2.length; i2++) {
                if (content2.equalsIgnoreCase(a2[i2].trim())) {
                    if (i2 == 0) {
                        return 6;
                    } else {
                        return i2 - 1;
                    }
                }
            }
        }
        String e2 = getParamWithoutB("param_E2", locale);
        if (e2 != null && !e2.trim().isEmpty()) {
            String[] a3 = e2.trim().split("\\|");
            for (int i3 = 0; i3 < a3.length; i3++) {
                if (content2.equalsIgnoreCase(a3[i3].trim())) {
                    if (i3 == 0) {
                        return 6;
                    } else {
                        return i3 - 1;
                    }
                }
            }
        }
        return -1;
    }

    private static int calRelDays(String text, String locale) {
        int result;
        String text2 = text.trim().replaceAll("\\s+", "\\\\s+").replaceAll("\\.", "\\\\.");
        String[] weeks = getParamWithoutB("param_thisweek", locale).split("\\|");
        String[] nweeks = getParamWithoutB("param_nextweek", locale).split("\\|");
        for (int i = 0; i < weeks.length; i++) {
            if (text2.equalsIgnoreCase(weeks[i].trim())) {
                if (i == 0) {
                    return 6;
                } else {
                    return i - 1;
                }
            }
        }
        for (int i2 = 0; i2 < nweeks.length; i2++) {
            if (text2.equalsIgnoreCase(nweeks[i2].trim())) {
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

    private static int convertMMM(String mstr, String locale) {
        String mmm = mstr.replaceAll("\\s+", "\\\\s+").replaceAll("\\.", "\\\\.");
        String mc = getParamWithoutB("param_MMM", locale);
        String mmc = getParamWithoutB("param_MMMM", locale);
        if (mc != null && !mc.trim().equals(BuildConfig.FLAVOR)) {
            String[] mcs = mc.split("\\|");
            for (int i = 0; i < mcs.length; i++) {
                if (mcs[i].trim().equalsIgnoreCase(mmm)) {
                    return i;
                }
            }
        }
        if (mmc != null && !mmc.trim().equals(BuildConfig.FLAVOR)) {
            String[] mcs2 = mmc.split("\\|");
            for (int i2 = 0; i2 < mcs2.length; i2++) {
                if (mcs2[i2].trim().equalsIgnoreCase(mmm)) {
                    return i2;
                }
            }
        }
        return -1;
    }

    private static int convertRelText(String mstr, String locale, String name) {
        String mmm = mstr.replaceAll("\\s+", "\\\\s+").replaceAll("\\.", "\\\\.");
        String mc = getParamWithoutB(name, locale);
        if (mc != null && !mc.trim().equals(BuildConfig.FLAVOR)) {
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

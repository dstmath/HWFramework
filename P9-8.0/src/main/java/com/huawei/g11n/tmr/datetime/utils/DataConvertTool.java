package com.huawei.g11n.tmr.datetime.utils;

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
        if (r != -1) {
            return r;
        }
        return convertE(content, localeBk);
    }

    public static int calRelDays(String text, String locale, String localeBk) {
        int r = calRelDays(text, locale);
        if (r != -1) {
            return r;
        }
        return calRelDays(text, localeBk);
    }

    public static int calTextDay(String text, String locale, String localeBk) {
        int r = calTextDay(text, locale);
        if (r != -1) {
            return r;
        }
        return calTextDay(text, localeBk);
    }

    public static int convertMMM(String mstr, String locale, String localeBk) {
        if (mstr == null || locale == null) {
            return -1;
        }
        int r = convertMMM(mstr, locale);
        if (r == -1) {
            r = convertMMM(mstr, localeBk);
        }
        return r;
    }

    public static int convertRelText(String mstr, String locale, String name, String localeBk) {
        int r = convertRelText(mstr, locale, name);
        if (r != -1) {
            return r;
        }
        return convertRelText(mstr, localeBk, name);
    }

    private static String replace(String content, String locale) {
        StringBuffer sb = new StringBuffer();
        String lam = getParamWithoutB("param_am", locale);
        if (!(lam == null || lam.trim().isEmpty())) {
            sb.append(lam).append("|");
        }
        sb.append(getParamWithoutB("param_am", "en"));
        Matcher m = Pattern.compile(sb.toString(), 2).matcher(content);
        while (m.find()) {
            content = content.replace(m.group(), " am ");
        }
        StringBuffer sb2 = new StringBuffer();
        String lpm = getParamWithoutB("param_pm", locale);
        if (!(lpm == null || lpm.trim().isEmpty())) {
            sb2.append(lpm).append("|");
        }
        sb2.append(getParamWithoutB("param_pm", "en"));
        Matcher m2 = Pattern.compile(sb2.toString(), 2).matcher(content);
        while (m2.find()) {
            content = content.replace(m2.group(), " pm ");
        }
        StringBuffer sb3 = new StringBuffer();
        String lmm = getParamWithoutB("param_mm", locale);
        if (!(lpm == null || lmm.trim().isEmpty())) {
            sb3.append(lmm).append("|");
        }
        sb3.append(getParamWithoutB("param_mm", "en"));
        Matcher m3 = Pattern.compile(sb3.toString(), 2).matcher(content);
        while (m3.find()) {
            content = content.replace(m3.group(), " mm ");
        }
        return content;
    }

    private static int convertE(String content, String locale) {
        content = content.replaceAll("\\s+", "\\\\s+").replaceAll("\\.", "\\\\.");
        if (content.trim().isEmpty()) {
            return -1;
        }
        String[] a;
        int i;
        int result;
        String e = getParamWithoutB("param_E", locale);
        if (locale.equals("be") || locale.equals("kk")) {
            e = e.replaceAll("\\\\b", "");
        }
        if (!(e == null || e.trim().isEmpty())) {
            a = e.trim().split("\\|");
            for (i = 0; i < a.length; i++) {
                if (content.equalsIgnoreCase(a[i].trim())) {
                    if (i != 0) {
                        result = i - 1;
                    } else {
                        result = 6;
                    }
                    return result;
                }
            }
        }
        String ee = getParamWithoutB("param_EEEE", locale);
        if (!(ee == null || ee.trim().isEmpty())) {
            a = ee.trim().split("\\|");
            for (i = 0; i < a.length; i++) {
                if (content.equalsIgnoreCase(a[i].trim())) {
                    if (i != 0) {
                        result = i - 1;
                    } else {
                        result = 6;
                    }
                    return result;
                }
            }
        }
        String e2 = getParamWithoutB("param_E2", locale);
        if (!(e2 == null || e2.trim().isEmpty())) {
            a = e2.trim().split("\\|");
            for (i = 0; i < a.length; i++) {
                if (content.equalsIgnoreCase(a[i].trim())) {
                    if (i != 0) {
                        result = i - 1;
                    } else {
                        result = 6;
                    }
                    return result;
                }
            }
        }
        return -1;
    }

    private static int calRelDays(String text, String locale) {
        int i;
        int result;
        text = text.trim().replaceAll("\\s+", "\\\\s+").replaceAll("\\.", "\\\\.");
        String[] weeks = getParamWithoutB("param_thisweek", locale).split("\\|");
        String[] nweeks = getParamWithoutB("param_nextweek", locale).split("\\|");
        for (i = 0; i < weeks.length; i++) {
            if (text.equalsIgnoreCase(weeks[i].trim())) {
                if (i != 0) {
                    result = i - 1;
                } else {
                    result = 6;
                }
                return result;
            }
        }
        for (i = 0; i < nweeks.length; i++) {
            if (text.equalsIgnoreCase(nweeks[i].trim())) {
                if (i == 0) {
                    result = 6;
                } else if (i != 7) {
                    result = i - 1;
                } else {
                    result = 13;
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
        String[] mcs;
        int i;
        String mmm = mstr.replaceAll("\\s+", "\\\\s+").replaceAll("\\.", "\\\\.");
        String mc = getParamWithoutB("param_MMM", locale);
        String mmc = getParamWithoutB("param_MMMM", locale);
        if (!(mc == null || mc.trim().equals(""))) {
            mcs = mc.split("\\|");
            for (i = 0; i < mcs.length; i++) {
                if (mcs[i].trim().equalsIgnoreCase(mmm)) {
                    return i;
                }
            }
        }
        if (!(mmc == null || mmc.trim().equals(""))) {
            mcs = mmc.split("\\|");
            for (i = 0; i < mcs.length; i++) {
                if (mcs[i].trim().equalsIgnoreCase(mmm)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static int convertRelText(String mstr, String locale, String name) {
        String mmm = mstr.replaceAll("\\s+", "\\\\s+").replaceAll("\\.", "\\\\.");
        String mc = getParamWithoutB(name, locale);
        if (!(mc == null || mc.trim().equals(""))) {
            String[] mcs = mc.split("\\|");
            int add = 0;
            int i = 0;
            while (i < mcs.length) {
                if (i == 0 && mcs[i].trim().equals(LocaleParam.SAVE_OR)) {
                    add = -1;
                } else if (mcs[i].trim().equalsIgnoreCase(mmm)) {
                    return i + add;
                }
                i++;
            }
        }
        return -1;
    }
}

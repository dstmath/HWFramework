package com.huawei.g11n.tmr.datetime.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataConvertTool {
    private static String getParamWithoutB(String str, String str2) {
        return new LocaleParam(str2).getWithoutB(str);
    }

    public static String replace(String str, String str2, String str3) {
        return replace(replace(str, str2), str3);
    }

    public static int convertE(String str, String str2, String str3) {
        int convertE = convertE(str, str2);
        if (convertE != -1) {
            return convertE;
        }
        return convertE(str, str3);
    }

    public static int calRelDays(String str, String str2, String str3) {
        int calRelDays = calRelDays(str, str2);
        if (calRelDays != -1) {
            return calRelDays;
        }
        return calRelDays(str, str3);
    }

    public static int calTextDay(String str, String str2, String str3) {
        int calTextDay = calTextDay(str, str2);
        if (calTextDay != -1) {
            return calTextDay;
        }
        return calTextDay(str, str3);
    }

    public static int convertMMM(String str, String str2, String str3) {
        if (str == null || str2 == null) {
            return -1;
        }
        int convertMMM = convertMMM(str, str2);
        if (convertMMM == -1) {
            convertMMM = convertMMM(str, str3);
        }
        return convertMMM;
    }

    public static int convertRelText(String str, String str2, String str3, String str4) {
        int convertRelText = convertRelText(str, str2, str3);
        if (convertRelText != -1) {
            return convertRelText;
        }
        return convertRelText(str, str4, str3);
    }

    private static String replace(String str, String str2) {
        StringBuffer stringBuffer = new StringBuffer();
        String paramWithoutB = getParamWithoutB("param_am", str2);
        if (!(paramWithoutB == null || paramWithoutB.trim().isEmpty())) {
            stringBuffer.append(paramWithoutB).append("|");
        }
        stringBuffer.append(getParamWithoutB("param_am", "en"));
        Matcher matcher = Pattern.compile(stringBuffer.toString(), 2).matcher(str);
        while (matcher.find()) {
            str = str.replace(matcher.group(), " am ");
        }
        stringBuffer = new StringBuffer();
        paramWithoutB = getParamWithoutB("param_pm", str2);
        if (!(paramWithoutB == null || paramWithoutB.trim().isEmpty())) {
            stringBuffer.append(paramWithoutB).append("|");
        }
        stringBuffer.append(getParamWithoutB("param_pm", "en"));
        matcher = Pattern.compile(stringBuffer.toString(), 2).matcher(str);
        while (matcher.find()) {
            str = str.replace(matcher.group(), " pm ");
        }
        return str;
    }

    private static int convertE(String str, String str2) {
        int i = 6;
        int i2 = 0;
        String replaceAll = str.replaceAll("\\s+", "\\\\s+").replaceAll("\\.", "\\\\.");
        if (replaceAll.trim().isEmpty()) {
            return -1;
        }
        String[] split;
        int i3;
        String paramWithoutB = getParamWithoutB("param_E", str2);
        if (str2.equals("be") || str2.equals("kk")) {
            paramWithoutB = paramWithoutB.replaceAll("\\\\b", "");
        }
        if (!(paramWithoutB == null || paramWithoutB.trim().isEmpty())) {
            split = paramWithoutB.trim().split("\\|");
            for (i3 = 0; i3 < split.length; i3++) {
                if (replaceAll.equalsIgnoreCase(split[i3].trim())) {
                    if (i3 != 0) {
                        i = i3 - 1;
                    }
                    return i;
                }
            }
        }
        paramWithoutB = getParamWithoutB("param_EEEE", str2);
        if (!(paramWithoutB == null || paramWithoutB.trim().isEmpty())) {
            split = paramWithoutB.trim().split("\\|");
            for (i3 = 0; i3 < split.length; i3++) {
                if (replaceAll.equalsIgnoreCase(split[i3].trim())) {
                    if (i3 != 0) {
                        i = i3 - 1;
                    }
                    return i;
                }
            }
        }
        paramWithoutB = getParamWithoutB("param_E2", str2);
        if (!(paramWithoutB == null || paramWithoutB.trim().isEmpty())) {
            String[] split2 = paramWithoutB.trim().split("\\|");
            while (i2 < split2.length) {
                if (replaceAll.equalsIgnoreCase(split2[i2].trim())) {
                    if (i2 != 0) {
                        i = i2 - 1;
                    }
                    return i;
                }
                i2++;
            }
        }
        return -1;
    }

    private static int calRelDays(String str, String str2) {
        int i = 6;
        int i2 = 0;
        String replaceAll = str.trim().replaceAll("\\s+", "\\\\s+").replaceAll("\\.", "\\\\.");
        String[] split = getParamWithoutB("param_thisweek", str2).split("\\|");
        String[] split2 = getParamWithoutB("param_nextweek", str2).split("\\|");
        for (int i3 = 0; i3 < split.length; i3++) {
            if (replaceAll.equalsIgnoreCase(split[i3].trim())) {
                if (i3 != 0) {
                    i = i3 - 1;
                }
                return i;
            }
        }
        while (i2 < split2.length) {
            if (replaceAll.equalsIgnoreCase(split2[i2].trim())) {
                if (i2 != 0) {
                    if (i2 != 7) {
                        i = i2 - 1;
                    } else {
                        i = 13;
                    }
                }
                return i + 7;
            }
            i2++;
        }
        return -1;
    }

    private static int calTextDay(String str, String str2) {
        String[] split = getParamWithoutB("param_days", str2).split("\\|");
        int i = -1;
        for (int i2 = 0; i2 < split.length; i2++) {
            if (Pattern.compile(split[i2].trim(), 2).matcher(str).matches()) {
                i = i2;
            }
        }
        return i;
    }

    private static int convertMMM(String str, String str2) {
        int i = 0;
        String replaceAll = str.replaceAll("\\s+", "\\\\s+").replaceAll("\\.", "\\\\.");
        String paramWithoutB = getParamWithoutB("param_MMM", str2);
        String paramWithoutB2 = getParamWithoutB("param_MMMM", str2);
        if (!(paramWithoutB == null || paramWithoutB.trim().equals(""))) {
            String[] split = paramWithoutB.split("\\|");
            for (int i2 = 0; i2 < split.length; i2++) {
                if (split[i2].trim().equalsIgnoreCase(replaceAll)) {
                    return i2;
                }
            }
        }
        if (!(paramWithoutB2 == null || paramWithoutB2.trim().equals(""))) {
            String[] split2 = paramWithoutB2.split("\\|");
            while (i < split2.length) {
                if (split2[i].trim().equalsIgnoreCase(replaceAll)) {
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

    private static int convertRelText(String str, String str2, String str3) {
        int i = 0;
        String replaceAll = str.replaceAll("\\s+", "\\\\s+").replaceAll("\\.", "\\\\.");
        String paramWithoutB = getParamWithoutB(str3, str2);
        if (!(paramWithoutB == null || paramWithoutB.trim().equals(""))) {
            String[] split = paramWithoutB.split("\\|");
            int i2 = 0;
            while (i2 < split.length) {
                if (i2 == 0 && split[i2].trim().equals(LocaleParam.SAVE_OR)) {
                    i = -1;
                } else if (split[i2].trim().equalsIgnoreCase(replaceAll)) {
                    return i + i2;
                }
                i2++;
            }
        }
        return -1;
    }
}

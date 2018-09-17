package com.huawei.g11n.tmr.datetime.parse;

import com.huawei.g11n.tmr.RuleInit;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_zh_hans;
import com.huawei.g11n.tmr.datetime.utils.DataConvertTool;
import com.huawei.g11n.tmr.datetime.utils.DatePeriod;
import com.huawei.g11n.tmr.datetime.utils.DateTime;
import com.huawei.g11n.tmr.datetime.utils.StringConvert;
import huawei.android.provider.HwSettings.System;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateParse {
    private static final HashMap<Integer, Integer> name2Method = null;
    private String locale;
    private String localeBk;
    private RuleInit rules;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.g11n.tmr.datetime.parse.DateParse.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.g11n.tmr.datetime.parse.DateParse.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.g11n.tmr.datetime.parse.DateParse.<clinit>():void");
    }

    public DateParse(String str, String str2, RuleInit ruleInit) {
        this.locale = str;
        this.localeBk = str2;
        this.rules = ruleInit;
    }

    public DatePeriod parse(String str, String str2, long j) {
        DatePeriod datePeriod = null;
        Integer valueOf = Integer.valueOf(str2);
        if (!name2Method.containsKey(valueOf)) {
            return null;
        }
        DateTime parseWeek;
        Integer num = (Integer) name2Method.get(valueOf);
        if (num.equals(Integer.valueOf(2))) {
            parseWeek = parseWeek(str, valueOf, j);
        } else if (num.equals(Integer.valueOf(3))) {
            parseWeek = parseED(str, valueOf);
        } else if (num.equals(Integer.valueOf(4))) {
            parseWeek = parseDMMMY(str);
        } else if (num.equals(Integer.valueOf(5))) {
            parseWeek = parseYMMMD(str);
        } else if (num.equals(Integer.valueOf(6))) {
            parseWeek = parseMMMDY(str);
        } else if (num.equals(Integer.valueOf(7))) {
            parseWeek = parseYMD(str, valueOf);
        } else if (num.equals(Integer.valueOf(8))) {
            parseWeek = parseTime(str, valueOf);
        } else if (num.equals(Integer.valueOf(9))) {
            parseWeek = parseAH(str, valueOf);
        } else if (num.equals(Integer.valueOf(10))) {
            parseWeek = parseAHMZ(str, valueOf);
        } else if (num.equals(Integer.valueOf(12))) {
            parseWeek = parseE(str, j);
        } else if (num.equals(Integer.valueOf(1))) {
            parseWeek = parseDay(str, valueOf, j);
        } else if (num.equals(Integer.valueOf(20))) {
            parseWeek = parseZAHM(str, valueOf, j);
        } else if (num.equals(Integer.valueOf(21))) {
            parseWeek = parseZhYMDE(str, valueOf, j);
        } else if (num.equals(Integer.valueOf(22))) {
            parseWeek = parseFullEU(str, valueOf, j);
        } else if (num.equals(Integer.valueOf(23))) {
            parseWeek = parseMyE(str, j);
        } else if (num.equals(Integer.valueOf(24))) {
            parseWeek = parseYDMMM(str, j);
        } else if (num.equals(Integer.valueOf(25))) {
            parseWeek = parseBOYMMMD(str);
        } else if (num.equals(Integer.valueOf(27))) {
            parseWeek = parseBOZAHM(str);
        } else if (num.equals(Integer.valueOf(29))) {
            parseWeek = parseAMPM(str);
        } else {
            parseWeek = null;
        }
        if (parseWeek != null) {
            datePeriod = new DatePeriod(parseWeek);
        }
        if (num.equals(Integer.valueOf(13))) {
            datePeriod = parseDurMMMDY(str, valueOf);
        } else if (num.equals(Integer.valueOf(14))) {
            datePeriod = parseDateDurDmy2(str, valueOf);
        } else if (num.equals(Integer.valueOf(15))) {
            datePeriod = parseDateDurYMD(str, valueOf);
        } else if (num.equals(Integer.valueOf(16))) {
            datePeriod = parseDateDurYMD2(str, valueOf);
        } else if (num.equals(Integer.valueOf(18))) {
            datePeriod = parseDurMMMDY2(str, valueOf);
        } else if (num.equals(Integer.valueOf(26))) {
            datePeriod = parseBoDurYMMMD(str, valueOf);
        } else if (num.equals(Integer.valueOf(28))) {
            datePeriod = parseLVDurYDDMMM(str, valueOf);
        }
        return datePeriod;
    }

    private DateTime parseAMPM(String str) {
        int parseInt;
        int parseInt2;
        int i;
        DateTime dateTime;
        String amPm = new LocaleParamGet_zh_hans().getAmPm(str);
        if (amPm == null || amPm.trim().equals("")) {
            amPm = "08:00";
        }
        try {
            String substring = amPm.substring(0, 2);
            String substring2 = amPm.substring(3, 5);
            parseInt = Integer.parseInt(substring);
            try {
                parseInt2 = Integer.parseInt(substring2);
                i = parseInt;
            } catch (Throwable th) {
                parseInt2 = 0;
                i = parseInt;
                dateTime = new DateTime();
                dateTime.setTime(i, parseInt2, 0, "", "", true);
                return dateTime;
            }
        } catch (Throwable th2) {
            parseInt = 8;
            parseInt2 = 0;
            i = parseInt;
            dateTime = new DateTime();
            dateTime.setTime(i, parseInt2, 0, "", "", true);
            return dateTime;
        }
        dateTime = new DateTime();
        dateTime.setTime(i, parseInt2, 0, "", "", true);
        return dateTime;
    }

    private DatePeriod parseLVDurYDDMMM(String str, Integer num) {
        String group;
        String group2;
        String group3;
        String str2;
        int parseInt;
        int parseInt2;
        int parseInt3;
        Matcher matcher = this.rules.getDetectByKey(Integer.valueOf(41005)).matcher(str);
        if (matcher.find()) {
            group = matcher.group(6);
            group2 = matcher.group(8);
            group3 = matcher.group(9);
            String group4 = matcher.group(3);
            str2 = group;
            group = group4;
            String str3 = group2;
            group2 = group3;
            group3 = str3;
        } else {
            group = null;
            group2 = null;
            group3 = null;
            str2 = null;
        }
        if (str2 != null) {
            parseInt = Integer.parseInt(str2.trim());
        } else {
            parseInt = -1;
        }
        if (group3 != null) {
            parseInt2 = Integer.parseInt(group3.trim());
        } else {
            parseInt2 = -1;
        }
        if (group2 != null) {
            group2 = group2.trim();
        } else {
            group2 = "";
        }
        if (group != null) {
            parseInt3 = Integer.parseInt(group.trim());
        } else {
            parseInt3 = -1;
        }
        int convertMMM = DataConvertTool.convertMMM(group2, this.locale, this.localeBk);
        if (parseInt == -1 || parseInt2 == -1 || convertMMM == -1) {
            return null;
        }
        DateTime dateTime = new DateTime();
        dateTime.setDay(parseInt3, convertMMM, parseInt);
        DateTime dateTime2 = new DateTime();
        dateTime2.setDay(parseInt3, convertMMM, parseInt2);
        return new DatePeriod(dateTime, dateTime2);
    }

    private DateTime parseBOZAHM(String str) {
        Matcher matcher = this.rules.getParseByKey(Integer.valueOf(913)).matcher(DataConvertTool.replace(str, this.locale, this.localeBk));
        DateTime dateTime = new DateTime();
        if (matcher.find()) {
            String group = matcher.group(2);
            String group2 = matcher.group(7) == null ? "" : matcher.group(7);
            String group3 = matcher.group(8) == null ? "00" : matcher.group(8);
            String group4 = matcher.group(9) == null ? "00" : matcher.group(9);
            String group5 = matcher.group(11) == null ? "00" : matcher.group(11);
            String str2 = "";
            if (!(group == null || group.trim().isEmpty())) {
                str2 = handleZ(group);
            }
            dateTime.setTime(Integer.parseInt(group3), Integer.parseInt(group4), Integer.parseInt(group5), group2, str2, true);
        }
        return dateTime;
    }

    private DatePeriod parseBoDurYMMMD(String str, Integer num) {
        String group;
        String group2;
        String group3;
        String str2;
        int parseInt;
        int parseInt2;
        int parseInt3;
        Matcher matcher = this.rules.getDetectByKey(Integer.valueOf(41003)).matcher(str);
        if (matcher.find()) {
            group = matcher.group(7);
            group2 = matcher.group(9);
            group3 = matcher.group(5);
            String group4 = matcher.group(3);
            str2 = group;
            group = group4;
            String str3 = group2;
            group2 = group3;
            group3 = str3;
        } else {
            group = null;
            group2 = null;
            group3 = null;
            str2 = null;
        }
        if (str2 != null) {
            parseInt = Integer.parseInt(str2.trim());
        } else {
            parseInt = -1;
        }
        if (group3 != null) {
            parseInt2 = Integer.parseInt(group3.trim());
        } else {
            parseInt2 = -1;
        }
        if (group2 != null) {
            group2 = group2.trim();
        } else {
            group2 = "";
        }
        if (group != null) {
            parseInt3 = Integer.parseInt(group.trim());
        } else {
            parseInt3 = -1;
        }
        int convertMMM = DataConvertTool.convertMMM(group2, this.locale, this.localeBk);
        if (parseInt == -1 || parseInt2 == -1 || convertMMM == -1) {
            return null;
        }
        DateTime dateTime = new DateTime();
        dateTime.setDay(parseInt3, convertMMM, parseInt);
        DateTime dateTime2 = new DateTime();
        dateTime2.setDay(parseInt3, convertMMM, parseInt2);
        return new DatePeriod(dateTime, dateTime2);
    }

    private DateTime parseBOYMMMD(String str) {
        int parseInt;
        int convertMMM;
        int i;
        Matcher matcher = this.rules.getDetectByKey(Integer.valueOf(21024)).matcher(str);
        if (matcher.find()) {
            String group = matcher.group(4);
            String group2 = matcher.group(9);
            String group3 = matcher.group(6);
            String group4 = matcher.group(11);
            String group5 = matcher.group(12);
            if (group != null) {
                group2 = group;
            } else if (group2 == null) {
                group2 = null;
            }
            if (group3 != null) {
                group4 = group3;
            } else if (group4 == null) {
                group4 = null;
            }
            parseInt = Integer.parseInt(group5.trim());
            convertMMM = DataConvertTool.convertMMM(group4, this.locale, this.localeBk);
            int i2;
            if (group2 == null || group2.trim().equals("")) {
                i = -1;
                i2 = convertMMM;
                convertMMM = parseInt;
                parseInt = i2;
            } else {
                i = Integer.parseInt(group2.trim());
                if (i < 100 && i > -1) {
                    i += System.AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT;
                    i2 = parseInt;
                    parseInt = convertMMM;
                    convertMMM = i2;
                } else {
                    i2 = parseInt;
                    parseInt = convertMMM;
                    convertMMM = i2;
                }
            }
        } else {
            convertMMM = -1;
            parseInt = -1;
            i = -1;
        }
        if (parseInt == -1) {
            return null;
        }
        DateTime dateTime = new DateTime();
        dateTime.setDay(i, parseInt, convertMMM);
        return dateTime;
    }

    private DateTime parseYDMMM(String str, long j) {
        int parseInt;
        int convertMMM;
        int i;
        Matcher matcher = this.rules.getParseByKey(Integer.valueOf(926)).matcher(str);
        if (matcher.find()) {
            String group = matcher.group(3) == null ? "-1" : matcher.group(3);
            String group2 = matcher.group(7) == null ? "-1" : matcher.group(7);
            parseInt = Integer.parseInt((matcher.group(6) == null ? "-1" : matcher.group(6)).trim());
            convertMMM = DataConvertTool.convertMMM(group2, this.locale, this.localeBk);
            if (group == null || group.trim().equals("")) {
                i = parseInt;
                parseInt = -1;
            } else {
                i = Integer.parseInt(group.trim());
                int i2;
                if (i < 100 && i > -1) {
                    i2 = parseInt;
                    parseInt = i + System.AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT;
                    i = i2;
                } else {
                    i2 = parseInt;
                    parseInt = i;
                    i = i2;
                }
            }
        } else {
            i = -1;
            convertMMM = -1;
            parseInt = -1;
        }
        if (convertMMM == -1) {
            return null;
        }
        DateTime dateTime = new DateTime();
        dateTime.setDay(parseInt, convertMMM, i);
        return dateTime;
    }

    private DateTime parseMyE(String str, long j) {
        return parseE(str.replaceAll("\u1014\u1031\u1037\u104a", ""), j);
    }

    private DateTime parseFullEU(String str, Integer num, long j) {
        int parseInt;
        int convertMMM;
        int i;
        Matcher matcher = this.rules.getParseByKey(Integer.valueOf(925)).matcher(str);
        if (matcher.find()) {
            String group = matcher.group(3) == null ? "-1" : matcher.group(3);
            String group2 = matcher.group(5) == null ? "-1" : matcher.group(5);
            parseInt = Integer.parseInt((matcher.group(7) == null ? "-1" : matcher.group(7)).trim());
            convertMMM = DataConvertTool.convertMMM(group2, this.locale, this.localeBk);
            if (group == null || group.trim().equals("")) {
                i = parseInt;
                parseInt = -1;
            } else {
                i = Integer.parseInt(group.trim());
                int i2;
                if (i < 100 && i > -1) {
                    i2 = parseInt;
                    parseInt = i + System.AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT;
                    i = i2;
                } else {
                    i2 = parseInt;
                    parseInt = i;
                    i = i2;
                }
            }
        } else {
            i = -1;
            convertMMM = -1;
            parseInt = -1;
        }
        if (convertMMM == -1) {
            return null;
        }
        DateTime dateTime = new DateTime();
        dateTime.setDay(parseInt, convertMMM, i);
        return dateTime;
    }

    private DateTime parseZhYMDE(String str, Integer num, long j) {
        int i = -1;
        Matcher matcher = this.rules.getDetectByKey(Integer.valueOf(21014)).matcher(str);
        DateTime dateTime = new DateTime();
        if (matcher.find()) {
            int i2;
            int parseInt;
            String group = matcher.group(2) == null ? "-1" : matcher.group(2);
            String group2 = matcher.group(6) == null ? "-1" : matcher.group(6);
            String group3 = matcher.group(8) == null ? "-1" : matcher.group(8);
            StringConvert stringConvert = new StringConvert();
            Calendar instance = Calendar.getInstance();
            instance.setTime(new Date(j));
            if (group.equals("-1")) {
                i2 = -1;
            } else if (stringConvert.isDigit(group, this.locale)) {
                i2 = Integer.parseInt(stringConvert.convertDigit(group, this.locale));
            } else {
                i2 = DataConvertTool.convertRelText(group, this.locale, "param_textyear", this.localeBk) + instance.get(1);
            }
            if (stringConvert.isDigit(group2, this.locale)) {
                parseInt = Integer.parseInt(stringConvert.convertDigit(group2, this.locale)) - 1;
            } else {
                parseInt = DataConvertTool.convertRelText(group2, this.locale, "param_textmonth", this.localeBk) + instance.get(2);
            }
            if (stringConvert.isDigit(group3, this.locale)) {
                i = Integer.parseInt(stringConvert.convertDigit(group3, this.locale));
            }
            dateTime.setDay(i2, parseInt, i);
        }
        return dateTime;
    }

    private DateTime parseDay(String str, Integer num, long j) {
        int calTextDay = DataConvertTool.calTextDay(str, this.locale, this.localeBk);
        DateTime dateTime = new DateTime();
        if (calTextDay == -1) {
            return dateTime;
        }
        dateTime.setDayByAddDays(calTextDay, j);
        return dateTime;
    }

    private DateTime parseAH(String str, Integer num) {
        Matcher matcher = this.rules.getParseByKey(Integer.valueOf(912)).matcher(DataConvertTool.replace(str, this.locale, this.localeBk));
        if (!matcher.find()) {
            return null;
        }
        String group = matcher.group(2);
        String group2 = matcher.group(3);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(group2.trim()).append(":00");
        return parseHMS(stringBuffer.toString(), group == null ? "" : group.trim());
    }

    private DateTime parseE(String str, long j) {
        DateTime dateTime = new DateTime();
        if (str == null || str.trim().equals("")) {
            return dateTime;
        }
        dateTime.setDayByWeekValue(DataConvertTool.convertE(str.replace("(", "").replace(")", ""), this.locale, this.localeBk), j);
        return dateTime;
    }

    private DateTime parseAHMZ(String str, Integer num) {
        CharSequence replace = DataConvertTool.replace(str, this.locale, this.localeBk);
        Pattern parseByKey = this.rules.getParseByKey(Integer.valueOf(908));
        if (num.intValue() == 31007) {
            parseByKey = this.rules.getParseByKey(Integer.valueOf(909));
        }
        Matcher matcher = parseByKey.matcher(replace);
        DateTime dateTime = new DateTime();
        if (matcher.find()) {
            String group = matcher.group(6);
            String str2 = "";
            if (!(group == null || group.trim().isEmpty())) {
                str2 = group;
            }
            String group2 = matcher.group(1) == null ? "" : matcher.group(1);
            group = matcher.group(2) == null ? "00" : matcher.group(2);
            String group3 = matcher.group(3) == null ? "00" : matcher.group(3);
            String group4 = matcher.group(5) == null ? "00" : matcher.group(5);
            String str3 = "";
            if (!str2.trim().isEmpty()) {
                str3 = handleZ(str2);
            }
            dateTime.setTime(Integer.parseInt(group), Integer.parseInt(group3), Integer.parseInt(group4), group2, str3, true);
        }
        return dateTime;
    }

    private DateTime parseZAHM(String str, Integer num, long j) {
        CharSequence replace = DataConvertTool.replace(str, this.locale, this.localeBk);
        StringConvert stringConvert = new StringConvert();
        Matcher matcher = this.rules.getParseByKey(Integer.valueOf(910)).matcher(replace);
        DateTime dateTime = new DateTime();
        if (matcher.find()) {
            String group = matcher.group(2);
            if (!(group == null || group.trim().isEmpty())) {
                dateTime.setDayByWeekValue(DataConvertTool.convertE(group, this.locale, this.localeBk), j);
            }
            String group2 = matcher.group(4);
            group = "";
            if (!(group2 == null || group2.trim().isEmpty())) {
                group = group2;
            }
            String group3 = matcher.group(9) == null ? "" : matcher.group(9);
            group2 = matcher.group(10) == null ? "00" : matcher.group(10);
            String group4 = matcher.group(12) == null ? "00" : matcher.group(12);
            String group5 = matcher.group(14) == null ? "00" : matcher.group(14);
            String str2 = "";
            if (!group.trim().isEmpty()) {
                str2 = handleZ(group);
            }
            dateTime.setTime(Integer.parseInt(stringConvert.convertDigit(group2, this.locale)), Integer.parseInt(stringConvert.convertDigit(group4, this.locale)), Integer.parseInt(stringConvert.convertDigit(group5, this.locale)), group3, str2, true);
        }
        return dateTime;
    }

    private DateTime parseWeek(String str, Integer num, long j) {
        DateTime dateTime = new DateTime();
        if (!(str == null || str.trim().isEmpty())) {
            int calRelDays = DataConvertTool.calRelDays(str, this.locale, this.localeBk);
            if (calRelDays == -1) {
                return dateTime;
            }
            dateTime.setDayByWeekValue(calRelDays, j);
        }
        return dateTime;
    }

    private DateTime parseHMS(String str, String str2) {
        String group;
        String str3;
        Matcher matcher = this.rules.getParseByKey(Integer.valueOf(911)).matcher(str);
        String str4 = "00";
        String str5 = "00";
        String str6 = "00";
        if (matcher.find()) {
            str4 = matcher.group(1) == null ? "00" : matcher.group(1);
            str5 = matcher.group(2) == null ? "00" : matcher.group(2);
            group = matcher.group(4) == null ? "00" : matcher.group(4);
            str6 = str5;
            str5 = str4;
        } else {
            group = str6;
            str6 = str5;
            str5 = str4;
        }
        DateTime dateTime = new DateTime();
        int parseInt = Integer.parseInt(str5);
        int parseInt2 = Integer.parseInt(str6);
        int parseInt3 = Integer.parseInt(group);
        if (str2 == null) {
            str3 = "";
        } else {
            str3 = str2;
        }
        dateTime.setTime(parseInt, parseInt2, parseInt3, str3, "", true);
        return dateTime;
    }

    private DateTime parseED(String str, Integer num) {
        Matcher matcher = this.rules.getParseByKey(Integer.valueOf(906)).matcher(new StringConvert().convertDigit(str, this.locale));
        String str2 = System.FINGERSENSE_KNUCKLE_GESTURE_OFF;
        if (matcher.find()) {
            str2 = matcher.group(1);
        }
        int parseInt = Integer.parseInt(str2.trim());
        DateTime dateTime = new DateTime();
        dateTime.setDay(-1, -1, parseInt);
        return dateTime;
    }

    private DateTime parseYMD(String str, Integer num) {
        Pattern parseByKey = this.rules.getParseByKey(Integer.valueOf(904));
        Pattern parseByKey2 = this.rules.getParseByKey(Integer.valueOf(905));
        Matcher matcher = parseByKey.matcher(str);
        Matcher matcher2 = parseByKey2.matcher(str);
        String str2 = "-1";
        String str3 = "-1";
        String str4 = "-1";
        if (matcher.find()) {
            if (num.intValue() == 20016 || num.intValue() == 21015 || num.intValue() == 21002) {
                str2 = matcher.group(3);
                str3 = matcher.group(2);
                str4 = matcher.group(1);
            } else if (num.intValue() == 20015 || num.intValue() == 21001) {
                str2 = matcher.group(2);
                str3 = matcher.group(1);
                str4 = matcher.group(3);
            } else {
                str2 = matcher.group(1);
                str3 = matcher.group(2);
                str4 = matcher.group(3);
            }
        } else if (matcher2.find()) {
            if (num.intValue() == 20016 || num.intValue() == 21002) {
                str2 = matcher2.group(2);
                str3 = matcher2.group(1);
            } else if (num.intValue() == 20015 || num.intValue() == 21001 || num.intValue() == 21014 || num.intValue() == 21015) {
                str2 = matcher2.group(2);
                str3 = matcher2.group(1);
            } else {
                str2 = matcher2.group(1);
                str3 = matcher2.group(2);
            }
        }
        int parseInt = Integer.parseInt(str2.trim());
        int parseInt2 = Integer.parseInt(str3.trim()) - 1;
        int parseInt3 = Integer.parseInt(str4.trim());
        if (parseInt3 < 100 && parseInt3 > -1) {
            parseInt3 += System.AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT;
        }
        DateTime dateTime = new DateTime();
        dateTime.setDay(parseInt3, parseInt2, parseInt);
        return dateTime;
    }

    private DateTime parseDMMMY(String str) {
        int parseInt;
        int convertMMM;
        int i;
        Matcher matcher = this.rules.getParseByKey(Integer.valueOf(903)).matcher(str);
        if (matcher.find()) {
            String group = matcher.group(1);
            String group2 = matcher.group(2);
            String group3 = matcher.group(4);
            parseInt = Integer.parseInt(group.trim());
            convertMMM = DataConvertTool.convertMMM(group2, this.locale, this.localeBk);
            if (group3 == null || group3.trim().equals("")) {
                i = -1;
            } else {
                i = Integer.parseInt(group3.trim());
                if (i < 100 && i > -1) {
                    i += System.AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT;
                }
            }
        } else {
            parseInt = -1;
            convertMMM = -1;
            i = -1;
        }
        if (convertMMM == -1) {
            return null;
        }
        DateTime dateTime = new DateTime();
        dateTime.setDay(i, convertMMM, parseInt);
        return dateTime;
    }

    private DateTime parseYMMMD(String str) {
        int parseInt;
        int convertMMM;
        int i;
        Matcher matcher = this.rules.getParseByKey(Integer.valueOf(901)).matcher(str);
        if (matcher.find()) {
            String group = matcher.group(5);
            String group2 = matcher.group(4);
            String group3 = matcher.group(2);
            parseInt = Integer.parseInt(group.trim());
            convertMMM = DataConvertTool.convertMMM(group2, this.locale, this.localeBk);
            if (group3 == null || group3.trim().equals("")) {
                i = -1;
            } else {
                i = Integer.parseInt(group3.trim());
                if (i < 100 && i > -1) {
                    i += System.AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT;
                }
            }
        } else {
            parseInt = -1;
            convertMMM = -1;
            i = -1;
        }
        if (convertMMM == -1) {
            return null;
        }
        DateTime dateTime = new DateTime();
        dateTime.setDay(i, convertMMM, parseInt);
        return dateTime;
    }

    private DateTime parseMMMDY(String str) {
        int parseInt;
        int convertMMM;
        int i;
        Matcher matcher = this.rules.getParseByKey(Integer.valueOf(902)).matcher(str);
        if (matcher.find()) {
            String group = matcher.group(2);
            String group2 = matcher.group(1);
            String group3 = matcher.group(4);
            parseInt = Integer.parseInt(group.trim());
            convertMMM = DataConvertTool.convertMMM(group2, this.locale, this.localeBk);
            if (group3 == null || group3.trim().equals("")) {
                i = parseInt;
                parseInt = convertMMM;
                convertMMM = -1;
            } else {
                i = Integer.parseInt(group3.trim());
                if (i < 100 && i > -1) {
                    i += System.AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT;
                }
                int i2 = parseInt;
                parseInt = convertMMM;
                convertMMM = i;
                i = i2;
            }
        } else {
            i = -1;
            parseInt = -1;
            convertMMM = -1;
        }
        if (parseInt == -1) {
            return null;
        }
        DateTime dateTime = new DateTime();
        dateTime.setDay(convertMMM, parseInt, i);
        return dateTime;
    }

    private DateTime parseTime(String str, Integer num) {
        boolean z = false;
        DateTime dateTime = new DateTime();
        CharSequence replace = DataConvertTool.replace(str, this.locale, this.localeBk);
        if (num.equals(Integer.valueOf(31001))) {
            z = true;
        }
        Matcher matcher = this.rules.getParseByKey(Integer.valueOf(907)).matcher(replace);
        if (matcher.find()) {
            String group = matcher.group(1) == null ? "" : matcher.group(1);
            String group2 = matcher.group(2) == null ? "00" : matcher.group(2);
            String group3 = matcher.group(3) == null ? "00" : matcher.group(3);
            String group4 = matcher.group(5) == null ? "00" : matcher.group(5);
            String group5 = matcher.group(6) == null ? "" : matcher.group(6);
            String group6 = matcher.group(7) == null ? "" : matcher.group(7);
            String str2 = "";
            if (!group.equals("")) {
                group5 = group;
            } else if (group5.equals("")) {
                group5 = str2;
            }
            group = "";
            if (group6 == null || group6.trim().isEmpty()) {
                group6 = group;
            } else {
                group6 = handleZ(group6);
            }
            dateTime.setTime(Integer.parseInt(group2), Integer.parseInt(group3), Integer.parseInt(group4), group5, group6, z);
        }
        return dateTime;
    }

    private String handleZ(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        Matcher matcher = Pattern.compile("(GMT){0,1}([+-])([0-1][0-9]|2[0-3]):{0,1}([0-5][0-9]|60)").matcher(str);
        String group;
        String group2;
        if (matcher.find()) {
            group = matcher.group(3);
            if (group == null || group.trim().isEmpty()) {
                group = "00";
            } else if (group.trim().length() < 2) {
                group = new StringBuilder(System.FINGERSENSE_KNUCKLE_GESTURE_OFF).append(group.trim()).toString();
            }
            group2 = matcher.group(4);
            if (group2 == null || group2.trim().isEmpty()) {
                group2 = "00";
            } else if (group2.trim().length() < 2) {
                group2 = new StringBuilder(System.FINGERSENSE_KNUCKLE_GESTURE_OFF).append(group2.trim()).toString();
            }
            stringBuffer.append(matcher.group(2)).append(group).append(group2);
            return stringBuffer.toString();
        }
        matcher = Pattern.compile("(GMT){0,1}([+-])(1{0,1}[0-9]|2[0-3]):?([0-5][0-9]|60){0,1}").matcher(str);
        if (matcher.find()) {
            group = matcher.group(3);
            if (group == null || group.trim().isEmpty()) {
                group = "00";
            } else if (group.trim().length() < 2) {
                group = new StringBuilder(System.FINGERSENSE_KNUCKLE_GESTURE_OFF).append(group.trim()).toString();
            }
            group2 = matcher.group(4);
            if (group2 == null || group2.trim().isEmpty()) {
                group2 = "00";
            } else if (group2.trim().length() < 2) {
                group2 = new StringBuilder(System.FINGERSENSE_KNUCKLE_GESTURE_OFF).append(group2.trim()).toString();
            }
            stringBuffer.append(matcher.group(2)).append(group).append(group2);
        }
        return stringBuffer.toString();
    }

    public DatePeriod parseDurMMMDY2(String str, Integer num) {
        String group;
        String group2;
        String group3;
        String group4;
        String str2;
        int parseInt;
        int parseInt2;
        int parseInt3;
        Matcher matcher = this.rules.getParseByKey(Integer.valueOf(917)).matcher(str);
        if (matcher.find()) {
            group = matcher.group(1);
            group2 = matcher.group(3);
            group3 = matcher.group(2);
            group4 = matcher.group(4);
            String group5 = matcher.group(6);
            str2 = group;
            group = group5;
            String str3 = group2;
            group2 = group4;
            group4 = str3;
        } else {
            group = null;
            group2 = null;
            group3 = null;
            group4 = null;
            str2 = null;
        }
        if (str2 != null) {
            parseInt = Integer.parseInt(str2.trim());
        } else {
            parseInt = -1;
        }
        if (group4 != null) {
            parseInt2 = Integer.parseInt(group4.trim());
        } else {
            parseInt2 = -1;
        }
        if (group3 != null) {
            group3 = group3.trim();
        } else {
            group3 = "";
        }
        if (group2 != null) {
            group2 = group2.trim();
        } else {
            group2 = "";
        }
        if (group != null) {
            parseInt3 = Integer.parseInt(group.trim());
        } else {
            parseInt3 = -1;
        }
        int convertMMM = DataConvertTool.convertMMM(group3, this.locale, this.localeBk);
        int convertMMM2 = DataConvertTool.convertMMM(group2, this.locale, this.localeBk);
        if (parseInt == -1 || parseInt2 == -1 || convertMMM == -1 || convertMMM2 == -1) {
            return null;
        }
        DateTime dateTime = new DateTime();
        dateTime.setDay(parseInt3, convertMMM, parseInt);
        DateTime dateTime2 = new DateTime();
        dateTime2.setDay(parseInt3, convertMMM2, parseInt2);
        return new DatePeriod(dateTime, dateTime2);
    }

    public DatePeriod parseDurMMMDY(String str, Integer num) {
        String group;
        String group2;
        String group3;
        String str2;
        int parseInt;
        int parseInt2;
        int parseInt3;
        Matcher matcher;
        String group4;
        String str3;
        if (num.intValue() == 40001) {
            matcher = this.rules.getParseByKey(Integer.valueOf(915)).matcher(str);
            if (matcher.find()) {
                group = matcher.group(1);
                group2 = matcher.group(2);
                group3 = matcher.group(3);
                group4 = matcher.group(5);
                str2 = group;
                group = group4;
                str3 = group2;
                group2 = group3;
                group3 = str3;
            } else {
                group = null;
                group2 = null;
                group3 = null;
                str2 = null;
            }
        } else if (num.intValue() == 40005 || num.intValue() == 41006) {
            matcher = this.rules.getParseByKey(Integer.valueOf(916)).matcher(str);
            if (matcher.find()) {
                group = matcher.group(2);
                group2 = matcher.group(3);
                group3 = matcher.group(1);
                group4 = matcher.group(4);
                str2 = group;
                group = group4;
                str3 = group2;
                group2 = group3;
                group3 = str3;
            } else {
                group = null;
                group2 = null;
                group3 = null;
                str2 = null;
            }
        } else if (num.intValue() == 40002 || num.intValue() == 41001 || num.intValue() == 41004) {
            matcher = this.rules.getParseByKey(Integer.valueOf(921)).matcher(str);
            if (matcher.find()) {
                group = matcher.group(5);
                group2 = matcher.group(6);
                group3 = matcher.group(4);
                group4 = matcher.group(2);
                str2 = group;
                group = group4;
                str3 = group2;
                group2 = group3;
                group3 = str3;
            } else {
                group = null;
                group2 = null;
                group3 = null;
                str2 = null;
            }
        } else {
            group = null;
            group2 = null;
            group3 = null;
            str2 = null;
        }
        if (str2 != null) {
            parseInt = Integer.parseInt(str2.trim());
        } else {
            parseInt = -1;
        }
        if (group3 != null) {
            parseInt2 = Integer.parseInt(group3.trim());
        } else {
            parseInt2 = -1;
        }
        if (group2 != null) {
            group2 = group2.trim();
        } else {
            group2 = "";
        }
        if (group != null) {
            parseInt3 = Integer.parseInt(group.trim());
        } else {
            parseInt3 = -1;
        }
        int convertMMM = DataConvertTool.convertMMM(group2, this.locale, this.localeBk);
        if (parseInt == -1 || parseInt2 == -1 || convertMMM == -1) {
            return null;
        }
        DateTime dateTime = new DateTime();
        dateTime.setDay(parseInt3, convertMMM, parseInt);
        DateTime dateTime2 = new DateTime();
        dateTime2.setDay(parseInt3, convertMMM, parseInt2);
        return new DatePeriod(dateTime, dateTime2);
    }

    public DatePeriod parseDateDurDmy2(String str, Integer num) {
        DatePeriod datePeriod;
        Matcher matcher = this.rules.getDetectByKey(Integer.valueOf(40003)).matcher(str);
        if (matcher.find()) {
            int parseInt;
            int parseInt2;
            int parseInt3;
            String group = matcher.group(2);
            String group2 = matcher.group(4);
            String group3 = matcher.group(8);
            String group4 = matcher.group(11);
            int parseInt4 = group != null ? Integer.parseInt(group.trim()) : -1;
            if (group2 != null) {
                parseInt = Integer.parseInt(group2.trim());
            } else {
                parseInt = -1;
            }
            if (group3 != null) {
                parseInt2 = Integer.parseInt(group3.trim());
            } else {
                parseInt2 = -1;
            }
            if (group4 != null) {
                parseInt3 = Integer.parseInt(group4.trim());
            } else {
                parseInt3 = -1;
            }
            if (parseInt4 == -1 || parseInt == -1 || parseInt2 == -1) {
                return null;
            }
            DateTime dateTime = new DateTime();
            dateTime.setDay(parseInt3, parseInt2 - 1, parseInt4);
            DateTime dateTime2 = new DateTime();
            dateTime2.setDay(parseInt3, parseInt2 - 1, parseInt);
            datePeriod = new DatePeriod(dateTime, dateTime2);
        } else {
            datePeriod = null;
        }
        return datePeriod;
    }

    public DatePeriod parseDateDurYMD(String str, Integer num) {
        DatePeriod datePeriod;
        Matcher matcher = this.rules.getParseByKey(Integer.valueOf(920)).matcher(str);
        if (matcher.find()) {
            int parseInt = matcher.group(2) == null ? -1 : Integer.parseInt(matcher.group(2));
            int parseInt2 = matcher.group(3) == null ? -1 : Integer.parseInt(matcher.group(3));
            int parseInt3 = matcher.group(4) == null ? -1 : Integer.parseInt(matcher.group(4));
            int parseInt4 = matcher.group(6) == null ? -1 : Integer.parseInt(matcher.group(6));
            int parseInt5 = matcher.group(7) == null ? -1 : Integer.parseInt(matcher.group(7));
            if (parseInt3 == -1 || parseInt5 == -1 || parseInt2 == -1) {
                return null;
            }
            DateTime dateTime = new DateTime();
            dateTime.setDay(parseInt, parseInt2 - 1, parseInt3);
            DateTime dateTime2 = new DateTime();
            if (parseInt4 == -1) {
                parseInt4 = parseInt2;
            }
            dateTime2.setDay(parseInt, parseInt4 - 1, parseInt5);
            datePeriod = new DatePeriod(dateTime, dateTime2);
        } else {
            datePeriod = null;
        }
        return datePeriod;
    }

    public DatePeriod parseDateDurYMD2(String str, Integer num) {
        DatePeriod datePeriod;
        Matcher matcher = this.rules.getParseByKey(Integer.valueOf(924)).matcher(str);
        if (matcher.find()) {
            int parseInt;
            int parseInt2;
            int parseInt3;
            String group = matcher.group(12);
            String group2 = matcher.group(14);
            String group3 = matcher.group(8);
            String group4 = matcher.group(3);
            int parseInt4 = group != null ? Integer.parseInt(group.trim()) : -1;
            if (group2 != null) {
                parseInt = Integer.parseInt(group2.trim());
            } else {
                parseInt = -1;
            }
            if (group3 != null) {
                parseInt2 = Integer.parseInt(group3.trim());
            } else {
                parseInt2 = -1;
            }
            if (group4 != null) {
                parseInt3 = Integer.parseInt(group4.trim());
            } else {
                parseInt3 = -1;
            }
            if (parseInt4 == -1 || parseInt == -1 || parseInt2 == -1) {
                return null;
            }
            DateTime dateTime = new DateTime();
            dateTime.setDay(parseInt3, parseInt2 - 1, parseInt4);
            DateTime dateTime2 = new DateTime();
            dateTime2.setDay(parseInt3, parseInt2 - 1, parseInt);
            datePeriod = new DatePeriod(dateTime, dateTime2);
        } else {
            datePeriod = null;
        }
        return datePeriod;
    }
}

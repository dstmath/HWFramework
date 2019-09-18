package com.huawei.g11n.tmr.datetime.parse;

import com.huawei.g11n.tmr.RuleInit;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_zh_hans;
import com.huawei.g11n.tmr.datetime.utils.DataConvertTool;
import com.huawei.g11n.tmr.datetime.utils.DatePeriod;
import com.huawei.g11n.tmr.datetime.utils.DateTime;
import com.huawei.g11n.tmr.datetime.utils.StringConvert;
import huawei.android.provider.HwSettings;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateParse {
    private static final HashMap<Integer, Integer> name2Method = new HashMap<Integer, Integer>() {
        {
            put(20010, 1);
            put(20011, 2);
            put(21009, 3);
            put(21017, 3);
            put(21018, 3);
            put(20006, 3);
            put(20007, 3);
            put(21023, 3);
            put(20013, 3);
            put(21007, 4);
            put(21005, 4);
            put(21006, 4);
            put(21008, 4);
            put(21010, 4);
            put(21011, 4);
            put(21012, 4);
            put(20001, 4);
            put(21020, 4);
            put(21029, 4);
            put(21036, 4);
            put(21037, 22);
            put(21038, 4);
            put(21039, 7);
            put(21040, 5);
            put(20005, 4);
            put(21016, 5);
            put(20008, 5);
            put(21028, 5);
            put(21025, 5);
            put(21015, 5);
            put(20012, 6);
            put(21013, 7);
            put(21014, 21);
            put(20014, 7);
            put(20015, 7);
            put(20016, 7);
            put(21004, 7);
            put(21003, 7);
            put(21002, 7);
            put(21021, 22);
            put(21019, 22);
            put(21022, 22);
            put(21030, 4);
            put(21031, 4);
            put(21032, 4);
            put(30001, 8);
            put(31002, 8);
            put(31001, 8);
            put(31003, 9);
            put(31004, 10);
            put(31005, 20);
            put(31006, 10);
            put(31007, 10);
            put(31008, 27);
            put(31009, 29);
            put(31010, 20);
            put(31011, 8);
            put(31012, 8);
            put(31013, 8);
            put(31014, 8);
            put(31015, 8);
            put(31016, 8);
            put(20009, 12);
            put(21026, 23);
            put(21033, 12);
            put(21034, 3);
            put(21035, 7);
            put(40005, 13);
            put(40001, 13);
            put(40002, 13);
            put(40003, 14);
            put(41001, 15);
            put(40004, 16);
            put(40006, 16);
            put(41002, 18);
            put(41006, 13);
            put(21027, 24);
            put(21024, 25);
            put(41003, 26);
            put(41004, 13);
            put(41005, 28);
            put(41007, 13);
        }
    };
    private String locale;
    private String localeBk;
    private RuleInit rules;

    public DateParse(String locale2, String localeBk2, RuleInit rules2) {
        this.locale = locale2;
        this.localeBk = localeBk2;
        this.rules = rules2;
    }

    public DatePeriod parse(String content, String n, long defaultTime) {
        Integer name = Integer.valueOf(n);
        if (!name2Method.containsKey(name)) {
            return null;
        }
        Integer method = name2Method.get(name);
        DateTime dt = null;
        DatePeriod dp = null;
        if (method.equals(2)) {
            dt = parseWeek(content, name, defaultTime);
        } else if (method.equals(3)) {
            dt = parseED(content, name);
        } else if (method.equals(4)) {
            dt = parseDMMMY(content);
        } else if (method.equals(5)) {
            dt = parseYMMMD(content);
        } else if (method.equals(6)) {
            dt = parseMMMDY(content);
        } else if (method.equals(7)) {
            dt = parseYMD(content, name);
        } else if (method.equals(8)) {
            dt = parseTime(content, name);
        } else if (method.equals(9)) {
            dt = parseAH(content, name);
        } else if (method.equals(10)) {
            dt = parseAHMZ(content, name);
        } else if (method.equals(12)) {
            dt = parseE(content, defaultTime);
        } else if (method.equals(1)) {
            dt = parseDay(content, name, defaultTime);
        } else if (method.equals(20)) {
            dt = parseZAHM(content, name, defaultTime);
        } else if (method.equals(21)) {
            dt = parseZhYMDE(content, name, defaultTime);
        } else if (method.equals(22)) {
            dt = parseFullEU(content, name, defaultTime);
        } else if (method.equals(23)) {
            dt = parseMyE(content, defaultTime);
        } else if (method.equals(24)) {
            dt = parseYDMMM(content, defaultTime);
        } else if (method.equals(25)) {
            dt = parseBOYMMMD(content);
        } else if (method.equals(27)) {
            dt = parseBOZAHM(content);
        } else if (method.equals(29)) {
            dt = parseAMPM(content);
        }
        if (dt != null) {
            dp = new DatePeriod(dt);
        }
        if (method.equals(13)) {
            dp = parseDurMMMDY(content, name);
        } else if (method.equals(14)) {
            dp = parseDateDurDmy2(content, name);
        } else if (method.equals(15)) {
            dp = parseDateDurYMD(content, name);
        } else if (method.equals(16)) {
            dp = parseDateDurYMD2(content, name);
        } else if (method.equals(18)) {
            dp = parseDurMMMDY2(content, name);
        } else if (method.equals(26)) {
            dp = parseBoDurYMMMD(content, name);
        } else if (method.equals(28)) {
            dp = parseLVDurYDDMMM(content, name);
        }
        return dp;
    }

    private DateTime parseAMPM(String content) {
        String time = new LocaleParamGet_zh_hans().getAmPm(content);
        if (time == null || time.trim().equals("")) {
            time = "08:00";
        }
        int h = 8;
        int m = 0;
        try {
            String hs = time.substring(0, 2);
            String ms = time.substring(3, 5);
            h = Integer.parseInt(hs);
            m = Integer.parseInt(ms);
        } catch (Throwable th) {
        }
        DateTime dateTime = new DateTime();
        DateTime dt1 = dateTime;
        dateTime.setTime(h, m, 0, "", "", true);
        return dt1;
    }

    private DatePeriod parseLVDurYDDMMM(String content, Integer name) {
        String dstr = null;
        String d2str = null;
        String mstr = null;
        String ystr = null;
        Matcher m = this.rules.getDetectByKey(41005).matcher(content);
        if (m.find()) {
            dstr = m.group(6);
            d2str = m.group(8);
            mstr = m.group(9);
            ystr = m.group(3);
        }
        int d1 = dstr == null ? -1 : Integer.parseInt(dstr.trim());
        int d2 = d2str == null ? -1 : Integer.parseInt(d2str.trim());
        String ms = mstr == null ? "" : mstr.trim();
        int y = ystr == null ? -1 : Integer.parseInt(ystr.trim());
        int m1 = DataConvertTool.convertMMM(ms, this.locale, this.localeBk);
        if (d1 == -1 || d2 == -1 || m1 == -1) {
            return null;
        }
        DateTime dt1 = new DateTime();
        dt1.setDay(y, m1, d1);
        DateTime dt2 = new DateTime();
        dt2.setDay(y, m1, d2);
        return new DatePeriod(dt1, dt2);
    }

    private DateTime parseBOZAHM(String content) {
        Matcher match = this.rules.getParseByKey(913).matcher(DataConvertTool.replace(content, this.locale, this.localeBk));
        DateTime dt = new DateTime();
        if (match.find()) {
            String z = match.group(2);
            String am = match.group(7) != null ? match.group(7) : "";
            String h = match.group(8) != null ? match.group(8) : "00";
            String m = match.group(9) != null ? match.group(9) : "00";
            String s = match.group(11) != null ? match.group(11) : "00";
            String gmt = "";
            if (z != null && !z.trim().isEmpty()) {
                gmt = handleZ(z);
            }
            dt.setTime(Integer.parseInt(h), Integer.parseInt(m), Integer.parseInt(s), am, gmt, true);
        }
        return dt;
    }

    private DatePeriod parseBoDurYMMMD(String content, Integer name) {
        String dstr = null;
        String d2str = null;
        String mstr = null;
        String ystr = null;
        Matcher m = this.rules.getDetectByKey(41003).matcher(content);
        if (m.find()) {
            dstr = m.group(7);
            d2str = m.group(9);
            mstr = m.group(5);
            ystr = m.group(3);
        }
        int d1 = dstr == null ? -1 : Integer.parseInt(dstr.trim());
        int d2 = d2str == null ? -1 : Integer.parseInt(d2str.trim());
        String ms = mstr == null ? "" : mstr.trim();
        int y = ystr == null ? -1 : Integer.parseInt(ystr.trim());
        int m1 = DataConvertTool.convertMMM(ms, this.locale, this.localeBk);
        if (d1 == -1 || d2 == -1 || m1 == -1) {
            return null;
        }
        DateTime dt1 = new DateTime();
        dt1.setDay(y, m1, d1);
        DateTime dt2 = new DateTime();
        dt2.setDay(y, m1, d2);
        return new DatePeriod(dt1, dt2);
    }

    private DateTime parseBOYMMMD(String content) {
        int ty = -1;
        int m = -1;
        int d = -1;
        Matcher match = this.rules.getDetectByKey(21024).matcher(content);
        if (match.find()) {
            String y1str = match.group(4);
            String y2str = match.group(9);
            String m1str = match.group(6);
            String m2str = match.group(11);
            String dstr = match.group(12);
            String ystr = null;
            String mstr = null;
            if (y1str != null) {
                ystr = y1str;
            } else if (y2str != null) {
                ystr = y2str;
            }
            if (m1str != null) {
                mstr = m1str;
            } else if (m2str != null) {
                mstr = m2str;
            }
            d = Integer.parseInt(dstr.trim());
            m = DataConvertTool.convertMMM(mstr, this.locale, this.localeBk);
            if (ystr == null || ystr.trim().equals("")) {
                ty = -1;
            } else {
                ty = Integer.parseInt(ystr.trim());
                if (ty < 100 && ty > -1) {
                    ty += 2000;
                }
            }
        }
        if (m == -1) {
            return null;
        }
        DateTime date = new DateTime();
        date.setDay(ty, m, d);
        return date;
    }

    private DateTime parseYDMMM(String content, long defaultTime) {
        Matcher ma = this.rules.getParseByKey(926).matcher(content);
        int y = -1;
        int m = -1;
        int d = -1;
        if (ma.find()) {
            String ystr = ma.group(3) != null ? ma.group(3) : "-1";
            String mstr = ma.group(7) != null ? ma.group(7) : "-1";
            d = Integer.parseInt((ma.group(6) != null ? ma.group(6) : "-1").trim());
            m = DataConvertTool.convertMMM(mstr, this.locale, this.localeBk);
            if (ystr != null && !ystr.trim().equals("")) {
                int ty = Integer.parseInt(ystr.trim());
                y = (ty >= 100 || ty <= -1) ? ty : ty + 2000;
            }
        }
        if (m == -1) {
            return null;
        }
        DateTime date = new DateTime();
        date.setDay(y, m, d);
        return date;
    }

    private DateTime parseMyE(String content, long defaultTime) {
        return parseE(content.replaceAll("နေ့၊", ""), defaultTime);
    }

    private DateTime parseFullEU(String content, Integer name, long defaultTime) {
        Matcher ma = this.rules.getParseByKey(925).matcher(content);
        int y = -1;
        int m = -1;
        int d = -1;
        if (ma.find()) {
            String ystr = ma.group(3) != null ? ma.group(3) : "-1";
            String mstr = ma.group(5) != null ? ma.group(5) : "-1";
            d = Integer.parseInt((ma.group(7) != null ? ma.group(7) : "-1").trim());
            m = DataConvertTool.convertMMM(mstr, this.locale, this.localeBk);
            if (ystr != null && !ystr.trim().equals("")) {
                int ty = Integer.parseInt(ystr.trim());
                y = (ty >= 100 || ty <= -1) ? ty : ty + 2000;
            }
        }
        if (m == -1) {
            return null;
        }
        DateTime date = new DateTime();
        date.setDay(y, m, d);
        return date;
    }

    private DateTime parseZhYMDE(String content, Integer name, long defaultTime) {
        int add;
        Pattern p = this.rules.getDetectByKey(21014);
        Matcher ma = p.matcher(content);
        DateTime dt = new DateTime();
        if (ma.find()) {
            String y = ma.group(2) != null ? ma.group(2) : "-1";
            String m = ma.group(6) != null ? ma.group(6) : "-1";
            String d = ma.group(8) != null ? ma.group(8) : "-1";
            int year = -1;
            int day = -1;
            StringConvert c = new StringConvert();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(defaultTime));
            String y2 = y;
            Pattern pattern = p;
            if (!y2.equals("-1")) {
                if (c.isDigit(y2, this.locale)) {
                    year = Integer.parseInt(c.convertDigit(y2, this.locale));
                } else {
                    year = cal.get(1) + DataConvertTool.convertRelText(y2, this.locale, "param_textyear", this.localeBk);
                }
            }
            if (c.isDigit(m, this.locale)) {
                add = Integer.parseInt(c.convertDigit(m, this.locale)) - 1;
            } else {
                add = DataConvertTool.convertRelText(m, this.locale, "param_textmonth", this.localeBk) + cal.get(2);
            }
            if (c.isDigit(d, this.locale)) {
                day = Integer.parseInt(c.convertDigit(d, this.locale));
            }
            dt.setDay(year, add, day);
        } else {
            long j = defaultTime;
            Pattern pattern2 = p;
        }
        return dt;
    }

    private DateTime parseDay(String content, Integer name, long defaultTime) {
        int add = DataConvertTool.calTextDay(content, this.locale, this.localeBk);
        DateTime dt = new DateTime();
        if (add == -1) {
            return dt;
        }
        dt.setDayByAddDays(add, defaultTime);
        return dt;
    }

    private DateTime parseAH(String content, Integer name) {
        Matcher match = this.rules.getParseByKey(912).matcher(DataConvertTool.replace(content, this.locale, this.localeBk));
        if (!match.find()) {
            return null;
        }
        String am = match.group(2);
        String ah = match.group(3);
        StringBuffer sb = new StringBuffer();
        sb.append(ah.trim());
        sb.append(":00");
        return parseHMS(sb.toString(), am != null ? am.trim() : "");
    }

    private DateTime parseE(String content, long defaultTime) {
        DateTime dt = new DateTime();
        if (content == null || content.trim().equals("")) {
            return dt;
        }
        dt.setDayByWeekValue(DataConvertTool.convertE(content.replace("(", "").replace(")", ""), this.locale, this.localeBk), defaultTime);
        return dt;
    }

    private DateTime parseAHMZ(String content, Integer name) {
        String content2 = DataConvertTool.replace(content, this.locale, this.localeBk);
        Pattern p = this.rules.getParseByKey(908);
        if (name.intValue() == 31007) {
            p = this.rules.getParseByKey(909);
        }
        Matcher match = p.matcher(content2);
        DateTime dt = new DateTime();
        if (match.find()) {
            String z2 = match.group(6);
            String z = "";
            if (z2 != null && !z2.trim().isEmpty()) {
                z = z2;
            }
            String z3 = z;
            String am = match.group(1) != null ? match.group(1) : "";
            String h = match.group(2) != null ? match.group(2) : "00";
            String m = match.group(3) != null ? match.group(3) : "00";
            String s = match.group(5) != null ? match.group(5) : "00";
            String gmt = "";
            if (!z3.trim().isEmpty()) {
                gmt = handleZ(z3);
            }
            String str = s;
            dt.setTime(Integer.parseInt(h), Integer.parseInt(m), Integer.parseInt(s), am, gmt, true);
        }
        return dt;
    }

    private DateTime parseZAHM(String content, Integer name, long defaultTime) {
        String gmt;
        String content2 = DataConvertTool.replace(content, this.locale, this.localeBk);
        StringConvert c = new StringConvert();
        Matcher match = this.rules.getParseByKey(910).matcher(content2);
        DateTime dt = new DateTime();
        if (match.find()) {
            String e = match.group(2);
            if (e == null || e.trim().isEmpty()) {
                long j = defaultTime;
            } else {
                dt.setDayByWeekValue(DataConvertTool.convertE(e, this.locale, this.localeBk), defaultTime);
            }
            String z1 = match.group(4);
            String z = "";
            if (z1 != null && !z1.trim().isEmpty()) {
                z = z1;
            }
            String z2 = z;
            String am = match.group(9) != null ? match.group(9) : "";
            String h = match.group(10) != null ? match.group(10) : "00";
            String m = match.group(12) != null ? match.group(12) : "00";
            String s = match.group(14) != null ? match.group(14) : "00";
            String str = content2;
            if (!z2.trim().isEmpty()) {
                gmt = handleZ(z2);
            } else {
                gmt = "";
            }
            int parseInt = Integer.parseInt(c.convertDigit(h, this.locale));
            int parseInt2 = Integer.parseInt(c.convertDigit(m, this.locale));
            int parseInt3 = Integer.parseInt(c.convertDigit(s, this.locale));
            String str2 = s;
            int i = parseInt;
            String str3 = m;
            int i2 = parseInt2;
            String str4 = h;
            int i3 = parseInt3;
            String str5 = z2;
            String str6 = z1;
            dt.setTime(i, i2, i3, am, gmt, true);
        } else {
            long j2 = defaultTime;
            String str7 = content2;
        }
        return dt;
    }

    private DateTime parseWeek(String content, Integer name, long defaultTime) {
        String text = content;
        DateTime dt = new DateTime();
        if (text != null && !text.trim().isEmpty()) {
            int rel = DataConvertTool.calRelDays(text, this.locale, this.localeBk);
            if (rel == -1) {
                return dt;
            }
            dt.setDayByWeekValue(rel, defaultTime);
        }
        return dt;
    }

    private DateTime parseHMS(String content, String ampm) {
        Matcher match = this.rules.getParseByKey(911).matcher(content);
        String hs = "00";
        String ms = "00";
        String ss = "00";
        if (match.find()) {
            hs = match.group(1) != null ? match.group(1) : "00";
            ms = match.group(2) != null ? match.group(2) : "00";
            ss = match.group(4) != null ? match.group(4) : "00";
            Integer.parseInt(hs);
        }
        DateTime dt = new DateTime();
        dt.setTime(Integer.parseInt(hs), Integer.parseInt(ms), Integer.parseInt(ss), ampm != null ? ampm : "", "", true);
        return dt;
    }

    private DateTime parseED(String content, Integer name) {
        Matcher match = this.rules.getParseByKey(906).matcher(new StringConvert().convertDigit(content, this.locale));
        String dstr = HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_OFF;
        if (match.find()) {
            dstr = match.group(1);
        }
        int d = Integer.parseInt(dstr.trim());
        DateTime date = new DateTime();
        date.setDay(-1, -1, d);
        return date;
    }

    private DateTime parseYMD(String content, Integer name) {
        int i;
        int ty;
        String str = content;
        Pattern p = this.rules.getParseByKey(904);
        Pattern p2 = this.rules.getParseByKey(905);
        Matcher match = p.matcher(str);
        Matcher match2 = p2.matcher(str);
        String dstr = "-1";
        String mstr = "-1";
        String ystr = "-1";
        if (match.find()) {
            if (name.intValue() == 20016 || name.intValue() == 21015 || name.intValue() == 21002 || name.intValue() == 21039) {
                dstr = match.group(3);
                mstr = match.group(2);
                ystr = match.group(1);
            } else if (name.intValue() == 20015 || name.intValue() == 21001) {
                dstr = match.group(2);
                mstr = match.group(1);
                ystr = match.group(3);
            } else {
                i = 1;
                dstr = match.group(1);
                mstr = match.group(2);
                ystr = match.group(3);
                int d = Integer.parseInt(dstr.trim());
                int m = Integer.parseInt(mstr.trim()) - i;
                ty = Integer.parseInt(ystr.trim());
                if (ty < 100 && ty > -1) {
                    ty += 2000;
                }
                DateTime date = new DateTime();
                date.setDay(ty, m, d);
                return date;
            }
        } else if (match2.find()) {
            if (name.intValue() == 20016 || name.intValue() == 21002) {
                i = 1;
                dstr = match2.group(2);
                mstr = match2.group(1);
                int d2 = Integer.parseInt(dstr.trim());
                int m2 = Integer.parseInt(mstr.trim()) - i;
                ty = Integer.parseInt(ystr.trim());
                ty += 2000;
                DateTime date2 = new DateTime();
                date2.setDay(ty, m2, d2);
                return date2;
            }
            if (name.intValue() == 20015 || name.intValue() == 21001 || name.intValue() == 21014 || name.intValue() == 21015) {
                i = 1;
                dstr = match2.group(2);
                mstr = match2.group(1);
            } else {
                i = 1;
                dstr = match2.group(1);
                mstr = match2.group(2);
            }
            int d22 = Integer.parseInt(dstr.trim());
            int m22 = Integer.parseInt(mstr.trim()) - i;
            ty = Integer.parseInt(ystr.trim());
            ty += 2000;
            DateTime date22 = new DateTime();
            date22.setDay(ty, m22, d22);
            return date22;
        }
        i = 1;
        int d222 = Integer.parseInt(dstr.trim());
        int m222 = Integer.parseInt(mstr.trim()) - i;
        ty = Integer.parseInt(ystr.trim());
        ty += 2000;
        DateTime date222 = new DateTime();
        date222.setDay(ty, m222, d222);
        return date222;
    }

    private DateTime parseDMMMY(String content) {
        int y = -1;
        int m = -1;
        int d = -1;
        Matcher match = this.rules.getParseByKey(903).matcher(content);
        if (match.find()) {
            String dstr = match.group(1);
            String mstr = match.group(2);
            String ystr = match.group(4);
            d = Integer.parseInt(dstr.trim());
            m = DataConvertTool.convertMMM(mstr, this.locale, this.localeBk);
            if (ystr != null && !ystr.trim().equals("")) {
                int ty = Integer.parseInt(ystr.trim());
                y = (ty >= 100 || ty <= -1) ? ty : ty + 2000;
            }
        }
        if (m == -1) {
            return null;
        }
        DateTime date = new DateTime();
        date.setDay(y, m, d);
        return date;
    }

    private DateTime parseYMMMD(String content) {
        int y = -1;
        int m = -1;
        int d = -1;
        Matcher match = this.rules.getParseByKey(901).matcher(content);
        if (match.find()) {
            String dstr = match.group(5);
            String mstr = match.group(4);
            String ystr = match.group(2);
            d = Integer.parseInt(dstr.trim());
            m = DataConvertTool.convertMMM(mstr, this.locale, this.localeBk);
            if (ystr != null && !ystr.trim().equals("")) {
                int ty = Integer.parseInt(ystr.trim());
                y = (ty >= 100 || ty <= -1) ? ty : ty + 2000;
            }
        }
        if (m == -1) {
            return null;
        }
        DateTime date = new DateTime();
        date.setDay(y, m, d);
        return date;
    }

    private DateTime parseMMMDY(String content) {
        int y = -1;
        int m = -1;
        int d = -1;
        Matcher match = this.rules.getParseByKey(902).matcher(content);
        if (match.find()) {
            String dstr = match.group(2);
            String mstr = match.group(1);
            String ystr = match.group(4);
            d = Integer.parseInt(dstr.trim());
            m = DataConvertTool.convertMMM(mstr, this.locale, this.localeBk);
            if (ystr != null && !ystr.trim().equals("")) {
                int ty = Integer.parseInt(ystr.trim());
                if (ty < 100 && ty > -1) {
                    ty += 2000;
                }
                y = ty;
            }
        }
        if (m == -1) {
            return null;
        }
        DateTime date = new DateTime();
        date.setDay(y, m, d);
        return date;
    }

    private DateTime parseTime(String content, Integer name) {
        DateTime dt = new DateTime();
        String content2 = DataConvertTool.replace(content, this.locale, this.localeBk);
        boolean isBefore = false;
        if (name.equals(31001)) {
            isBefore = true;
        }
        boolean isBefore2 = isBefore;
        Matcher m = this.rules.getParseByKey(907).matcher(content2);
        if (m.find()) {
            String apstr1 = m.group(1) != null ? m.group(1) : "";
            String hstr = m.group(2) != null ? m.group(2) : "00";
            String mstr = m.group(3) != null ? m.group(3) : "00";
            String sstr = m.group(5) != null ? m.group(5) : "00";
            String apstr2 = m.group(6) != null ? m.group(6) : "";
            String zstr = m.group(7) != null ? m.group(7) : "";
            String am = "";
            if (!apstr1.equals("")) {
                am = apstr1;
            } else if (!apstr2.equals("")) {
                am = apstr2;
            }
            String am2 = am;
            String gmt = "";
            if (zstr != null && !zstr.trim().isEmpty()) {
                gmt = handleZ(zstr);
            }
            String str = zstr;
            String str2 = apstr2;
            String str3 = sstr;
            String str4 = mstr;
            dt.setTime(Integer.parseInt(hstr), Integer.parseInt(mstr), Integer.parseInt(sstr), am2, gmt, isBefore2);
        }
        return dt;
    }

    private String handleZ(String context) {
        StringBuffer gmt = new StringBuffer();
        Matcher m1 = Pattern.compile("(GMT){0,1}([+-])([0-1][0-9]|2[0-3]):{0,1}([0-5][0-9]|60)").matcher(context);
        if (m1.find()) {
            String hs = m1.group(3);
            if (hs == null || hs.trim().isEmpty()) {
                hs = "00";
            } else if (hs.trim().length() < 2) {
                hs = HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_OFF + hs.trim();
            }
            String ms = m1.group(4);
            if (ms == null || ms.trim().isEmpty()) {
                ms = "00";
            } else if (ms.trim().length() < 2) {
                ms = HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_OFF + ms.trim();
            }
            gmt.append(m1.group(2));
            gmt.append(hs);
            gmt.append(ms);
            return gmt.toString();
        }
        Matcher m2 = Pattern.compile("(GMT){0,1}([+-])(1{0,1}[0-9]|2[0-3]):?([0-5][0-9]|60){0,1}").matcher(context);
        if (m2.find()) {
            String hs2 = m2.group(3);
            if (hs2 == null || hs2.trim().isEmpty()) {
                hs2 = "00";
            } else if (hs2.trim().length() < 2) {
                hs2 = HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_OFF + hs2.trim();
            }
            String ms2 = m2.group(4);
            if (ms2 == null || ms2.trim().isEmpty()) {
                ms2 = "00";
            } else if (ms2.trim().length() < 2) {
                ms2 = HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_OFF + ms2.trim();
            }
            gmt.append(m2.group(2));
            gmt.append(hs2);
            gmt.append(ms2);
        }
        return gmt.toString();
    }

    public DatePeriod parseDurMMMDY2(String content, Integer name) {
        String dstr = null;
        String d2str = null;
        String mstr = null;
        String m2str = null;
        String ystr = null;
        Matcher m = this.rules.getParseByKey(917).matcher(content);
        if (m.find()) {
            dstr = m.group(1);
            d2str = m.group(3);
            mstr = m.group(2);
            m2str = m.group(4);
            ystr = m.group(6);
        }
        int d1 = dstr == null ? -1 : Integer.parseInt(dstr.trim());
        int d2 = d2str == null ? -1 : Integer.parseInt(d2str.trim());
        String ms = mstr == null ? "" : mstr.trim();
        String ms2 = m2str == null ? "" : m2str.trim();
        int y = ystr == null ? -1 : Integer.parseInt(ystr.trim());
        int m1 = DataConvertTool.convertMMM(ms, this.locale, this.localeBk);
        String str = dstr;
        int m2 = DataConvertTool.convertMMM(ms2, this.locale, this.localeBk);
        if (d1 == -1 || d2 == -1 || m1 == -1) {
        } else if (m2 == -1) {
            int i = m2;
        } else {
            DateTime dt1 = new DateTime();
            dt1.setDay(y, m1, d1);
            DateTime dt2 = new DateTime();
            dt2.setDay(y, m2, d2);
            int i2 = m2;
            return new DatePeriod(dt1, dt2);
        }
        return null;
    }

    public DatePeriod parseDurMMMDY(String content, Integer name) {
        String dstr = null;
        String d2str = null;
        String mstr = null;
        String ystr = null;
        if (name.intValue() == 40001 || name.intValue() == 41007) {
            Matcher m = this.rules.getParseByKey(915).matcher(content);
            if (m.find()) {
                dstr = m.group(1);
                d2str = m.group(2);
                mstr = m.group(3);
                ystr = m.group(5);
            }
        } else if (name.intValue() == 40005 || name.intValue() == 41006) {
            Matcher m2 = this.rules.getParseByKey(916).matcher(content);
            if (m2.find()) {
                dstr = m2.group(2);
                d2str = m2.group(3);
                mstr = m2.group(1);
                ystr = m2.group(4);
            }
        } else if (name.intValue() == 40002 || name.intValue() == 41001 || name.intValue() == 41004) {
            Matcher m3 = this.rules.getParseByKey(921).matcher(content);
            if (m3.find()) {
                dstr = m3.group(5);
                d2str = m3.group(6);
                mstr = m3.group(4);
                ystr = m3.group(2);
            }
        }
        int d1 = dstr == null ? -1 : Integer.parseInt(dstr.trim());
        int d2 = d2str == null ? -1 : Integer.parseInt(d2str.trim());
        String ms = mstr == null ? "" : mstr.trim();
        int y = ystr == null ? -1 : Integer.parseInt(ystr.trim());
        int m4 = DataConvertTool.convertMMM(ms, this.locale, this.localeBk);
        if (d1 == -1 || d2 == -1 || m4 == -1) {
            return null;
        }
        DateTime dt1 = new DateTime();
        dt1.setDay(y, m4, d1);
        DateTime dt2 = new DateTime();
        dt2.setDay(y, m4, d2);
        return new DatePeriod(dt1, dt2);
    }

    public DatePeriod parseDateDurDmy2(String content, Integer name) {
        Matcher m = this.rules.getDetectByKey(40003).matcher(content);
        DatePeriod dp = null;
        if (m.find()) {
            String dstr = m.group(2);
            String d2str = m.group(4);
            String mstr = m.group(8);
            String ystr = m.group(11);
            int d1 = dstr == null ? -1 : Integer.parseInt(dstr.trim());
            int d2 = d2str == null ? -1 : Integer.parseInt(d2str.trim());
            int m1 = mstr == null ? -1 : Integer.parseInt(mstr.trim());
            int y1 = ystr == null ? -1 : Integer.parseInt(ystr.trim());
            if (d1 == -1 || d2 == -1 || m1 == -1) {
                return null;
            }
            DateTime b = new DateTime();
            b.setDay(y1, m1 - 1, d1);
            DateTime e = new DateTime();
            e.setDay(y1, m1 - 1, d2);
            dp = new DatePeriod(b, e);
        }
        return dp;
    }

    public DatePeriod parseDateDurYMD(String content, Integer name) {
        Pattern p = this.rules.getParseByKey(920);
        Matcher m = p.matcher(content);
        DatePeriod dp = null;
        if (m.find()) {
            int y1 = m.group(2) != null ? Integer.parseInt(m.group(2)) : -1;
            int m1 = m.group(3) != null ? Integer.parseInt(m.group(3)) : -1;
            int d1 = m.group(4) != null ? Integer.parseInt(m.group(4)) : -1;
            int m2 = m.group(6) != null ? Integer.parseInt(m.group(6)) : -1;
            int d2 = m.group(7) != null ? Integer.parseInt(m.group(7)) : -1;
            if (d1 == -1 || d2 == -1) {
            } else if (m1 == -1) {
                Pattern pattern = p;
            } else {
                DateTime b = new DateTime();
                Pattern pattern2 = p;
                b.setDay(y1, m1 - 1, d1);
                DateTime e = new DateTime();
                if (m2 == -1) {
                    m2 = m1;
                }
                e.setDay(y1, m2 - 1, d2);
                dp = new DatePeriod(b, e);
            }
            return null;
        }
        return dp;
    }

    public DatePeriod parseDateDurYMD2(String content, Integer name) {
        Matcher m = this.rules.getParseByKey(924).matcher(content);
        DatePeriod dp = null;
        if (m.find()) {
            String dstr = m.group(12);
            String d2str = m.group(14);
            String mstr = m.group(8);
            String ystr = m.group(3);
            int d1 = dstr == null ? -1 : Integer.parseInt(dstr.trim());
            int d2 = d2str == null ? -1 : Integer.parseInt(d2str.trim());
            int m1 = mstr == null ? -1 : Integer.parseInt(mstr.trim());
            int y1 = ystr == null ? -1 : Integer.parseInt(ystr.trim());
            if (d1 == -1 || d2 == -1 || m1 == -1) {
                return null;
            }
            DateTime b = new DateTime();
            b.setDay(y1, m1 - 1, d1);
            DateTime e = new DateTime();
            e.setDay(y1, m1 - 1, d2);
            dp = new DatePeriod(b, e);
        }
        return dp;
    }
}

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
    private static final HashMap<Integer, Integer> name2Method = new HashMap<Integer, Integer>() {
        {
            put(Integer.valueOf(20010), Integer.valueOf(1));
            put(Integer.valueOf(20011), Integer.valueOf(2));
            put(Integer.valueOf(21009), Integer.valueOf(3));
            put(Integer.valueOf(21017), Integer.valueOf(3));
            put(Integer.valueOf(21018), Integer.valueOf(3));
            put(Integer.valueOf(20006), Integer.valueOf(3));
            put(Integer.valueOf(20007), Integer.valueOf(3));
            put(Integer.valueOf(21023), Integer.valueOf(3));
            put(Integer.valueOf(20013), Integer.valueOf(3));
            put(Integer.valueOf(21007), Integer.valueOf(4));
            put(Integer.valueOf(21005), Integer.valueOf(4));
            put(Integer.valueOf(21006), Integer.valueOf(4));
            put(Integer.valueOf(21008), Integer.valueOf(4));
            put(Integer.valueOf(21010), Integer.valueOf(4));
            put(Integer.valueOf(21011), Integer.valueOf(4));
            put(Integer.valueOf(21012), Integer.valueOf(4));
            put(Integer.valueOf(20001), Integer.valueOf(4));
            put(Integer.valueOf(21020), Integer.valueOf(4));
            put(Integer.valueOf(21029), Integer.valueOf(4));
            put(Integer.valueOf(21036), Integer.valueOf(4));
            put(Integer.valueOf(21037), Integer.valueOf(22));
            put(Integer.valueOf(21038), Integer.valueOf(4));
            put(Integer.valueOf(21039), Integer.valueOf(7));
            put(Integer.valueOf(21040), Integer.valueOf(5));
            put(Integer.valueOf(20005), Integer.valueOf(4));
            put(Integer.valueOf(21016), Integer.valueOf(5));
            put(Integer.valueOf(20008), Integer.valueOf(5));
            put(Integer.valueOf(21028), Integer.valueOf(5));
            put(Integer.valueOf(21025), Integer.valueOf(5));
            put(Integer.valueOf(21015), Integer.valueOf(5));
            put(Integer.valueOf(20012), Integer.valueOf(6));
            put(Integer.valueOf(21013), Integer.valueOf(7));
            put(Integer.valueOf(21014), Integer.valueOf(21));
            put(Integer.valueOf(20014), Integer.valueOf(7));
            put(Integer.valueOf(20015), Integer.valueOf(7));
            put(Integer.valueOf(20016), Integer.valueOf(7));
            put(Integer.valueOf(21004), Integer.valueOf(7));
            put(Integer.valueOf(21003), Integer.valueOf(7));
            put(Integer.valueOf(21002), Integer.valueOf(7));
            put(Integer.valueOf(21021), Integer.valueOf(22));
            put(Integer.valueOf(21019), Integer.valueOf(22));
            put(Integer.valueOf(21022), Integer.valueOf(22));
            put(Integer.valueOf(21030), Integer.valueOf(4));
            put(Integer.valueOf(21031), Integer.valueOf(4));
            put(Integer.valueOf(21032), Integer.valueOf(4));
            put(Integer.valueOf(30001), Integer.valueOf(8));
            put(Integer.valueOf(31002), Integer.valueOf(8));
            put(Integer.valueOf(31001), Integer.valueOf(8));
            put(Integer.valueOf(31003), Integer.valueOf(9));
            put(Integer.valueOf(31004), Integer.valueOf(10));
            put(Integer.valueOf(31005), Integer.valueOf(20));
            put(Integer.valueOf(31006), Integer.valueOf(10));
            put(Integer.valueOf(31007), Integer.valueOf(10));
            put(Integer.valueOf(31008), Integer.valueOf(27));
            put(Integer.valueOf(31009), Integer.valueOf(29));
            put(Integer.valueOf(31010), Integer.valueOf(20));
            put(Integer.valueOf(31011), Integer.valueOf(8));
            put(Integer.valueOf(31012), Integer.valueOf(8));
            put(Integer.valueOf(31013), Integer.valueOf(8));
            put(Integer.valueOf(31014), Integer.valueOf(8));
            put(Integer.valueOf(31015), Integer.valueOf(8));
            put(Integer.valueOf(31016), Integer.valueOf(8));
            put(Integer.valueOf(20009), Integer.valueOf(12));
            put(Integer.valueOf(21026), Integer.valueOf(23));
            put(Integer.valueOf(21033), Integer.valueOf(12));
            put(Integer.valueOf(21034), Integer.valueOf(3));
            put(Integer.valueOf(21035), Integer.valueOf(7));
            put(Integer.valueOf(40005), Integer.valueOf(13));
            put(Integer.valueOf(40001), Integer.valueOf(13));
            put(Integer.valueOf(40002), Integer.valueOf(13));
            put(Integer.valueOf(40003), Integer.valueOf(14));
            put(Integer.valueOf(41001), Integer.valueOf(15));
            put(Integer.valueOf(40004), Integer.valueOf(16));
            put(Integer.valueOf(40006), Integer.valueOf(16));
            put(Integer.valueOf(41002), Integer.valueOf(18));
            put(Integer.valueOf(41006), Integer.valueOf(13));
            put(Integer.valueOf(21027), Integer.valueOf(24));
            put(Integer.valueOf(21024), Integer.valueOf(25));
            put(Integer.valueOf(41003), Integer.valueOf(26));
            put(Integer.valueOf(41004), Integer.valueOf(13));
            put(Integer.valueOf(41005), Integer.valueOf(28));
            put(Integer.valueOf(41007), Integer.valueOf(13));
        }
    };
    private String locale;
    private String localeBk;
    private RuleInit rules;

    public DateParse(String locale, String localeBk, RuleInit rules) {
        this.locale = locale;
        this.localeBk = localeBk;
        this.rules = rules;
    }

    public DatePeriod parse(String content, String n, long defaultTime) {
        Integer name = Integer.valueOf(n);
        if (!name2Method.containsKey(name)) {
            return null;
        }
        Integer method = (Integer) name2Method.get(name);
        DateTime dt = null;
        DatePeriod dp = null;
        if (method.equals(Integer.valueOf(2))) {
            dt = parseWeek(content, name, defaultTime);
        } else if (method.equals(Integer.valueOf(3))) {
            dt = parseED(content, name);
        } else if (method.equals(Integer.valueOf(4))) {
            dt = parseDMMMY(content);
        } else if (method.equals(Integer.valueOf(5))) {
            dt = parseYMMMD(content);
        } else if (method.equals(Integer.valueOf(6))) {
            dt = parseMMMDY(content);
        } else if (method.equals(Integer.valueOf(7))) {
            dt = parseYMD(content, name);
        } else if (method.equals(Integer.valueOf(8))) {
            dt = parseTime(content, name);
        } else if (method.equals(Integer.valueOf(9))) {
            dt = parseAH(content, name);
        } else if (method.equals(Integer.valueOf(10))) {
            dt = parseAHMZ(content, name);
        } else if (method.equals(Integer.valueOf(12))) {
            dt = parseE(content, defaultTime);
        } else if (method.equals(Integer.valueOf(1))) {
            dt = parseDay(content, name, defaultTime);
        } else if (method.equals(Integer.valueOf(20))) {
            dt = parseZAHM(content, name, defaultTime);
        } else if (method.equals(Integer.valueOf(21))) {
            dt = parseZhYMDE(content, name, defaultTime);
        } else if (method.equals(Integer.valueOf(22))) {
            dt = parseFullEU(content, name, defaultTime);
        } else if (method.equals(Integer.valueOf(23))) {
            dt = parseMyE(content, defaultTime);
        } else if (method.equals(Integer.valueOf(24))) {
            dt = parseYDMMM(content, defaultTime);
        } else if (method.equals(Integer.valueOf(25))) {
            dt = parseBOYMMMD(content);
        } else if (method.equals(Integer.valueOf(27))) {
            dt = parseBOZAHM(content);
        } else if (method.equals(Integer.valueOf(29))) {
            dt = parseAMPM(content);
        }
        if (dt != null) {
            dp = new DatePeriod(dt);
        }
        if (method.equals(Integer.valueOf(13))) {
            dp = parseDurMMMDY(content, name);
        } else if (method.equals(Integer.valueOf(14))) {
            dp = parseDateDurDmy2(content, name);
        } else if (method.equals(Integer.valueOf(15))) {
            dp = parseDateDurYMD(content, name);
        } else if (method.equals(Integer.valueOf(16))) {
            dp = parseDateDurYMD2(content, name);
        } else if (method.equals(Integer.valueOf(18))) {
            dp = parseDurMMMDY2(content, name);
        } else if (method.equals(Integer.valueOf(26))) {
            dp = parseBoDurYMMMD(content, name);
        } else if (method.equals(Integer.valueOf(28))) {
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
        DateTime dt1 = new DateTime();
        dt1.setTime(h, m, 0, "", "", true);
        return dt1;
    }

    private DatePeriod parseLVDurYDDMMM(String content, Integer name) {
        String dstr = null;
        String d2str = null;
        String mstr = null;
        String ystr = null;
        Matcher m = this.rules.getDetectByKey(Integer.valueOf(41005)).matcher(content);
        if (m.find()) {
            dstr = m.group(6);
            d2str = m.group(8);
            mstr = m.group(9);
            ystr = m.group(3);
        }
        int d1 = dstr != null ? Integer.parseInt(dstr.trim()) : -1;
        int d2 = d2str != null ? Integer.parseInt(d2str.trim()) : -1;
        String ms = mstr != null ? mstr.trim() : "";
        int y = ystr != null ? Integer.parseInt(ystr.trim()) : -1;
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
        Matcher match = this.rules.getParseByKey(Integer.valueOf(913)).matcher(DataConvertTool.replace(content, this.locale, this.localeBk));
        DateTime dt = new DateTime();
        if (match.find()) {
            String z = match.group(2);
            String am = match.group(7) == null ? "" : match.group(7);
            String h = match.group(8) == null ? "00" : match.group(8);
            String m = match.group(9) == null ? "00" : match.group(9);
            String s = match.group(11) == null ? "00" : match.group(11);
            String gmt = "";
            if (!(z == null || z.trim().isEmpty())) {
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
        Matcher m = this.rules.getDetectByKey(Integer.valueOf(41003)).matcher(content);
        if (m.find()) {
            dstr = m.group(7);
            d2str = m.group(9);
            mstr = m.group(5);
            ystr = m.group(3);
        }
        int d1 = dstr != null ? Integer.parseInt(dstr.trim()) : -1;
        int d2 = d2str != null ? Integer.parseInt(d2str.trim()) : -1;
        String ms = mstr != null ? mstr.trim() : "";
        int y = ystr != null ? Integer.parseInt(ystr.trim()) : -1;
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
        int y = -1;
        int m = -1;
        int d = -1;
        Matcher match = this.rules.getDetectByKey(Integer.valueOf(21024)).matcher(content);
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
            if (!(ystr == null || ystr.trim().equals(""))) {
                int ty = Integer.parseInt(ystr.trim());
                y = (ty < 100 && ty > -1) ? ty + System.AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT : ty;
            }
        }
        if (m == -1) {
            return null;
        }
        DateTime date = new DateTime();
        date.setDay(y, m, d);
        return date;
    }

    private DateTime parseYDMMM(String content, long defaultTime) {
        Matcher ma = this.rules.getParseByKey(Integer.valueOf(926)).matcher(content);
        int y = -1;
        int m = -1;
        int d = -1;
        if (ma.find()) {
            String ystr = ma.group(3) == null ? "-1" : ma.group(3);
            String mstr = ma.group(7) == null ? "-1" : ma.group(7);
            d = Integer.parseInt((ma.group(6) == null ? "-1" : ma.group(6)).trim());
            m = DataConvertTool.convertMMM(mstr, this.locale, this.localeBk);
            if (!(ystr == null || ystr.trim().equals(""))) {
                int ty = Integer.parseInt(ystr.trim());
                y = (ty < 100 && ty > -1) ? ty + System.AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT : ty;
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
        Matcher ma = this.rules.getParseByKey(Integer.valueOf(925)).matcher(content);
        int y = -1;
        int m = -1;
        int d = -1;
        if (ma.find()) {
            String ystr = ma.group(3) == null ? "-1" : ma.group(3);
            String mstr = ma.group(5) == null ? "-1" : ma.group(5);
            d = Integer.parseInt((ma.group(7) == null ? "-1" : ma.group(7)).trim());
            m = DataConvertTool.convertMMM(mstr, this.locale, this.localeBk);
            if (!(ystr == null || ystr.trim().equals(""))) {
                int ty = Integer.parseInt(ystr.trim());
                y = (ty < 100 && ty > -1) ? ty + System.AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT : ty;
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
        Matcher ma = this.rules.getDetectByKey(Integer.valueOf(21014)).matcher(content);
        DateTime dt = new DateTime();
        if (ma.find()) {
            int month;
            String y = ma.group(2) == null ? "-1" : ma.group(2);
            String m = ma.group(6) == null ? "-1" : ma.group(6);
            String d = ma.group(8) == null ? "-1" : ma.group(8);
            int year = -1;
            int day = -1;
            StringConvert c = new StringConvert();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(defaultTime));
            if (!y.equals("-1")) {
                if (c.isDigit(y, this.locale)) {
                    year = Integer.parseInt(c.convertDigit(y, this.locale));
                } else {
                    year = cal.get(1) + DataConvertTool.convertRelText(y, this.locale, "param_textyear", this.localeBk);
                }
            }
            if (c.isDigit(m, this.locale)) {
                month = Integer.parseInt(c.convertDigit(m, this.locale)) - 1;
            } else {
                month = cal.get(2) + DataConvertTool.convertRelText(m, this.locale, "param_textmonth", this.localeBk);
            }
            if (c.isDigit(d, this.locale)) {
                day = Integer.parseInt(c.convertDigit(d, this.locale));
            }
            dt.setDay(year, month, day);
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
        Matcher match = this.rules.getParseByKey(Integer.valueOf(912)).matcher(DataConvertTool.replace(content, this.locale, this.localeBk));
        if (!match.find()) {
            return null;
        }
        String am = match.group(2);
        String ah = match.group(3);
        StringBuffer sb = new StringBuffer();
        sb.append(ah.trim()).append(":00");
        return parseHMS(sb.toString(), am == null ? "" : am.trim());
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
        content = DataConvertTool.replace(content, this.locale, this.localeBk);
        Pattern p = this.rules.getParseByKey(Integer.valueOf(908));
        if (name.intValue() == 31007) {
            p = this.rules.getParseByKey(Integer.valueOf(909));
        }
        Matcher match = p.matcher(content);
        DateTime dt = new DateTime();
        if (match.find()) {
            String z2 = match.group(6);
            String z = "";
            if (!(z2 == null || z2.trim().isEmpty())) {
                z = z2;
            }
            String am = match.group(1) == null ? "" : match.group(1);
            String h = match.group(2) == null ? "00" : match.group(2);
            String m = match.group(3) == null ? "00" : match.group(3);
            String s = match.group(5) == null ? "00" : match.group(5);
            String gmt = "";
            if (!z.trim().isEmpty()) {
                gmt = handleZ(z);
            }
            dt.setTime(Integer.parseInt(h), Integer.parseInt(m), Integer.parseInt(s), am, gmt, true);
        }
        return dt;
    }

    private DateTime parseZAHM(String content, Integer name, long defaultTime) {
        content = DataConvertTool.replace(content, this.locale, this.localeBk);
        StringConvert c = new StringConvert();
        Matcher match = this.rules.getParseByKey(Integer.valueOf(910)).matcher(content);
        DateTime dt = new DateTime();
        if (match.find()) {
            String e = match.group(2);
            if (!(e == null || e.trim().isEmpty())) {
                dt.setDayByWeekValue(DataConvertTool.convertE(e, this.locale, this.localeBk), defaultTime);
            }
            String z1 = match.group(4);
            String z = "";
            if (!(z1 == null || z1.trim().isEmpty())) {
                z = z1;
            }
            String am = match.group(9) == null ? "" : match.group(9);
            String h = match.group(10) == null ? "00" : match.group(10);
            String m = match.group(12) == null ? "00" : match.group(12);
            String s = match.group(14) == null ? "00" : match.group(14);
            String gmt = "";
            if (!z.trim().isEmpty()) {
                gmt = handleZ(z);
            }
            dt.setTime(Integer.parseInt(c.convertDigit(h, this.locale)), Integer.parseInt(c.convertDigit(m, this.locale)), Integer.parseInt(c.convertDigit(s, this.locale)), am, gmt, true);
        }
        return dt;
    }

    private DateTime parseWeek(String content, Integer name, long defaultTime) {
        String text = content;
        DateTime dt = new DateTime();
        if (!(content == null || content.trim().isEmpty())) {
            int rel = DataConvertTool.calRelDays(content, this.locale, this.localeBk);
            if (rel == -1) {
                return dt;
            }
            dt.setDayByWeekValue(rel, defaultTime);
        }
        return dt;
    }

    private DateTime parseHMS(String content, String ampm) {
        String str;
        Matcher match = this.rules.getParseByKey(Integer.valueOf(911)).matcher(content);
        String hs = "00";
        String ms = "00";
        String ss = "00";
        if (match.find()) {
            hs = match.group(1) == null ? "00" : match.group(1);
            ms = match.group(2) == null ? "00" : match.group(2);
            ss = match.group(4) == null ? "00" : match.group(4);
            Integer.parseInt(hs);
        }
        DateTime dt = new DateTime();
        int parseInt = Integer.parseInt(hs);
        int parseInt2 = Integer.parseInt(ms);
        int parseInt3 = Integer.parseInt(ss);
        if (ampm == null) {
            str = "";
        } else {
            str = ampm;
        }
        dt.setTime(parseInt, parseInt2, parseInt3, str, "", true);
        return dt;
    }

    private DateTime parseED(String content, Integer name) {
        Matcher match = this.rules.getParseByKey(Integer.valueOf(906)).matcher(new StringConvert().convertDigit(content, this.locale));
        String dstr = System.FINGERSENSE_KNUCKLE_GESTURE_OFF;
        if (match.find()) {
            dstr = match.group(1);
        }
        int d = Integer.parseInt(dstr.trim());
        DateTime date = new DateTime();
        date.setDay(-1, -1, d);
        return date;
    }

    private DateTime parseYMD(String content, Integer name) {
        Pattern p = this.rules.getParseByKey(Integer.valueOf(904));
        Pattern p2 = this.rules.getParseByKey(Integer.valueOf(905));
        Matcher match = p.matcher(content);
        Matcher match2 = p2.matcher(content);
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
                dstr = match.group(1);
                mstr = match.group(2);
                ystr = match.group(3);
            }
        } else if (match2.find()) {
            if (name.intValue() == 20016 || name.intValue() == 21002) {
                dstr = match2.group(2);
                mstr = match2.group(1);
            } else if (name.intValue() == 20015 || name.intValue() == 21001 || name.intValue() == 21014 || name.intValue() == 21015) {
                dstr = match2.group(2);
                mstr = match2.group(1);
            } else {
                dstr = match2.group(1);
                mstr = match2.group(2);
            }
        }
        int d = Integer.parseInt(dstr.trim());
        int m = Integer.parseInt(mstr.trim()) - 1;
        int ty = Integer.parseInt(ystr.trim());
        if (ty < 100 && ty > -1) {
            ty += System.AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT;
        }
        DateTime date = new DateTime();
        date.setDay(ty, m, d);
        return date;
    }

    private DateTime parseDMMMY(String content) {
        int y = -1;
        int m = -1;
        int d = -1;
        Matcher match = this.rules.getParseByKey(Integer.valueOf(903)).matcher(content);
        if (match.find()) {
            String dstr = match.group(1);
            String mstr = match.group(2);
            String ystr = match.group(4);
            d = Integer.parseInt(dstr.trim());
            m = DataConvertTool.convertMMM(mstr, this.locale, this.localeBk);
            if (!(ystr == null || ystr.trim().equals(""))) {
                int ty = Integer.parseInt(ystr.trim());
                y = (ty < 100 && ty > -1) ? ty + System.AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT : ty;
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
        Matcher match = this.rules.getParseByKey(Integer.valueOf(901)).matcher(content);
        if (match.find()) {
            String dstr = match.group(5);
            String mstr = match.group(4);
            String ystr = match.group(2);
            d = Integer.parseInt(dstr.trim());
            m = DataConvertTool.convertMMM(mstr, this.locale, this.localeBk);
            if (!(ystr == null || ystr.trim().equals(""))) {
                int ty = Integer.parseInt(ystr.trim());
                y = (ty < 100 && ty > -1) ? ty + System.AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT : ty;
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
        Matcher match = this.rules.getParseByKey(Integer.valueOf(902)).matcher(content);
        if (match.find()) {
            String dstr = match.group(2);
            String mstr = match.group(1);
            String ystr = match.group(4);
            d = Integer.parseInt(dstr.trim());
            m = DataConvertTool.convertMMM(mstr, this.locale, this.localeBk);
            if (!(ystr == null || ystr.trim().equals(""))) {
                int ty = Integer.parseInt(ystr.trim());
                if (ty < 100 && ty > -1) {
                    ty += System.AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT;
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
        content = DataConvertTool.replace(content, this.locale, this.localeBk);
        boolean isBefore = false;
        if (name.equals(Integer.valueOf(31001))) {
            isBefore = true;
        }
        Matcher m = this.rules.getParseByKey(Integer.valueOf(907)).matcher(content);
        if (m.find()) {
            String apstr1 = m.group(1) == null ? "" : m.group(1);
            String hstr = m.group(2) == null ? "00" : m.group(2);
            String mstr = m.group(3) == null ? "00" : m.group(3);
            String sstr = m.group(5) == null ? "00" : m.group(5);
            String apstr2 = m.group(6) == null ? "" : m.group(6);
            String zstr = m.group(7) == null ? "" : m.group(7);
            String am = "";
            if (!apstr1.equals("")) {
                am = apstr1;
            } else if (!apstr2.equals("")) {
                am = apstr2;
            }
            String gmt = "";
            if (!(zstr == null || zstr.trim().isEmpty())) {
                gmt = handleZ(zstr);
            }
            dt.setTime(Integer.parseInt(hstr), Integer.parseInt(mstr), Integer.parseInt(sstr), am, gmt, isBefore);
        }
        return dt;
    }

    private String handleZ(String context) {
        StringBuffer gmt = new StringBuffer();
        Matcher m1 = Pattern.compile("(GMT){0,1}([+-])([0-1][0-9]|2[0-3]):{0,1}([0-5][0-9]|60)").matcher(context);
        String hs;
        String ms;
        if (m1.find()) {
            hs = m1.group(3);
            if (hs == null || hs.trim().isEmpty()) {
                hs = "00";
            } else if (hs.trim().length() < 2) {
                hs = new StringBuilder(System.FINGERSENSE_KNUCKLE_GESTURE_OFF).append(hs.trim()).toString();
            }
            ms = m1.group(4);
            if (ms == null || ms.trim().isEmpty()) {
                ms = "00";
            } else if (ms.trim().length() < 2) {
                ms = new StringBuilder(System.FINGERSENSE_KNUCKLE_GESTURE_OFF).append(ms.trim()).toString();
            }
            gmt.append(m1.group(2)).append(hs).append(ms);
            return gmt.toString();
        }
        Matcher m2 = Pattern.compile("(GMT){0,1}([+-])(1{0,1}[0-9]|2[0-3]):?([0-5][0-9]|60){0,1}").matcher(context);
        if (m2.find()) {
            hs = m2.group(3);
            if (hs == null || hs.trim().isEmpty()) {
                hs = "00";
            } else if (hs.trim().length() < 2) {
                hs = new StringBuilder(System.FINGERSENSE_KNUCKLE_GESTURE_OFF).append(hs.trim()).toString();
            }
            ms = m2.group(4);
            if (ms == null || ms.trim().isEmpty()) {
                ms = "00";
            } else if (ms.trim().length() < 2) {
                ms = new StringBuilder(System.FINGERSENSE_KNUCKLE_GESTURE_OFF).append(ms.trim()).toString();
            }
            gmt.append(m2.group(2)).append(hs).append(ms);
        }
        return gmt.toString();
    }

    public DatePeriod parseDurMMMDY2(String content, Integer name) {
        String dstr = null;
        String d2str = null;
        String mstr = null;
        String m2str = null;
        String ystr = null;
        Matcher m = this.rules.getParseByKey(Integer.valueOf(917)).matcher(content);
        if (m.find()) {
            dstr = m.group(1);
            d2str = m.group(3);
            mstr = m.group(2);
            m2str = m.group(4);
            ystr = m.group(6);
        }
        int d1 = dstr != null ? Integer.parseInt(dstr.trim()) : -1;
        int d2 = d2str != null ? Integer.parseInt(d2str.trim()) : -1;
        String ms = mstr != null ? mstr.trim() : "";
        String ms2 = m2str != null ? m2str.trim() : "";
        int y = ystr != null ? Integer.parseInt(ystr.trim()) : -1;
        int m1 = DataConvertTool.convertMMM(ms, this.locale, this.localeBk);
        int m2 = DataConvertTool.convertMMM(ms2, this.locale, this.localeBk);
        if (d1 == -1 || d2 == -1 || m1 == -1 || m2 == -1) {
            return null;
        }
        DateTime dt1 = new DateTime();
        dt1.setDay(y, m1, d1);
        DateTime dt2 = new DateTime();
        dt2.setDay(y, m2, d2);
        return new DatePeriod(dt1, dt2);
    }

    public DatePeriod parseDurMMMDY(String content, Integer name) {
        String dstr = null;
        String d2str = null;
        String mstr = null;
        String ystr = null;
        Matcher m;
        if (name.intValue() == 40001 || name.intValue() == 41007) {
            m = this.rules.getParseByKey(Integer.valueOf(915)).matcher(content);
            if (m.find()) {
                dstr = m.group(1);
                d2str = m.group(2);
                mstr = m.group(3);
                ystr = m.group(5);
            }
        } else if (name.intValue() == 40005 || name.intValue() == 41006) {
            m = this.rules.getParseByKey(Integer.valueOf(916)).matcher(content);
            if (m.find()) {
                dstr = m.group(2);
                d2str = m.group(3);
                mstr = m.group(1);
                ystr = m.group(4);
            }
        } else if (name.intValue() == 40002 || name.intValue() == 41001 || name.intValue() == 41004) {
            m = this.rules.getParseByKey(Integer.valueOf(921)).matcher(content);
            if (m.find()) {
                dstr = m.group(5);
                d2str = m.group(6);
                mstr = m.group(4);
                ystr = m.group(2);
            }
        }
        int d1 = dstr != null ? Integer.parseInt(dstr.trim()) : -1;
        int d2 = d2str != null ? Integer.parseInt(d2str.trim()) : -1;
        String ms = mstr != null ? mstr.trim() : "";
        int y = ystr != null ? Integer.parseInt(ystr.trim()) : -1;
        int m2 = DataConvertTool.convertMMM(ms, this.locale, this.localeBk);
        if (d1 == -1 || d2 == -1 || m2 == -1) {
            return null;
        }
        DateTime dt1 = new DateTime();
        dt1.setDay(y, m2, d1);
        DateTime dt2 = new DateTime();
        dt2.setDay(y, m2, d2);
        return new DatePeriod(dt1, dt2);
    }

    public DatePeriod parseDateDurDmy2(String content, Integer name) {
        Matcher m = this.rules.getDetectByKey(Integer.valueOf(40003)).matcher(content);
        DatePeriod dp = null;
        if (m.find()) {
            String dstr = m.group(2);
            String d2str = m.group(4);
            String mstr = m.group(8);
            String ystr = m.group(11);
            int d1 = dstr != null ? Integer.parseInt(dstr.trim()) : -1;
            int d2 = d2str != null ? Integer.parseInt(d2str.trim()) : -1;
            int m1 = mstr != null ? Integer.parseInt(mstr.trim()) : -1;
            int y1 = ystr != null ? Integer.parseInt(ystr.trim()) : -1;
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
        Matcher m = this.rules.getParseByKey(Integer.valueOf(920)).matcher(content);
        DatePeriod dp = null;
        if (m.find()) {
            int y1 = m.group(2) == null ? -1 : Integer.parseInt(m.group(2));
            int m1 = m.group(3) == null ? -1 : Integer.parseInt(m.group(3));
            int d1 = m.group(4) == null ? -1 : Integer.parseInt(m.group(4));
            int m2 = m.group(6) == null ? -1 : Integer.parseInt(m.group(6));
            int d2 = m.group(7) == null ? -1 : Integer.parseInt(m.group(7));
            if (d1 == -1 || d2 == -1 || m1 == -1) {
                return null;
            }
            DateTime b = new DateTime();
            b.setDay(y1, m1 - 1, d1);
            DateTime e = new DateTime();
            if (m2 == -1) {
                m2 = m1;
            }
            e.setDay(y1, m2 - 1, d2);
            dp = new DatePeriod(b, e);
        }
        return dp;
    }

    public DatePeriod parseDateDurYMD2(String content, Integer name) {
        Matcher m = this.rules.getParseByKey(Integer.valueOf(924)).matcher(content);
        DatePeriod dp = null;
        if (m.find()) {
            String dstr = m.group(12);
            String d2str = m.group(14);
            String mstr = m.group(8);
            String ystr = m.group(3);
            int d1 = dstr != null ? Integer.parseInt(dstr.trim()) : -1;
            int d2 = d2str != null ? Integer.parseInt(d2str.trim()) : -1;
            int m1 = mstr != null ? Integer.parseInt(mstr.trim()) : -1;
            int y1 = ystr != null ? Integer.parseInt(ystr.trim()) : -1;
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

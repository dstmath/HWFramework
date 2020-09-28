package com.huawei.g11n.tmr.datetime.parse;

import com.huawei.g11n.tmr.RuleInit;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_zh_hans;
import com.huawei.g11n.tmr.datetime.utils.DataConvertTool;
import com.huawei.g11n.tmr.datetime.utils.DatePeriod;
import com.huawei.g11n.tmr.datetime.utils.DateTime;
import com.huawei.g11n.tmr.datetime.utils.StringConvert;
import com.huawei.uikit.effect.BuildConfig;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateParse {
    private static final HashMap<Integer, Integer> name2Method = new HashMap<Integer, Integer>() {
        /* class com.huawei.g11n.tmr.datetime.parse.DateParse.AnonymousClass1 */

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
            put(21041, 24);
            put(41008, 13);
            put(31017, 3);
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
            return parseDurMMMDY(content, name);
        }
        if (method.equals(14)) {
            return parseDateDurDmy2(content, name);
        }
        if (method.equals(15)) {
            return parseDateDurYMD(content, name);
        }
        if (method.equals(16)) {
            return parseDateDurYMD2(content, name);
        }
        if (method.equals(18)) {
            return parseDurMMMDY2(content, name);
        }
        if (method.equals(26)) {
            return parseBoDurYMMMD(content, name);
        }
        if (method.equals(28)) {
            return parseLVDurYDDMMM(content, name);
        }
        return dp;
    }

    private DateTime parseAMPM(String content) {
        String time = new LocaleParamGet_zh_hans().getAmPm(content);
        if (time == null || time.trim().equals(BuildConfig.FLAVOR)) {
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
        dt1.setTime(h, m, 0, BuildConfig.FLAVOR, BuildConfig.FLAVOR, true);
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
        String ms = mstr == null ? BuildConfig.FLAVOR : mstr.trim();
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
            String am = match.group(7) != null ? match.group(7) : BuildConfig.FLAVOR;
            String s = "00";
            String h = match.group(8) != null ? match.group(8) : s;
            String m = match.group(9) != null ? match.group(9) : s;
            if (match.group(11) != null) {
                s = match.group(11);
            }
            dt.setTime(Integer.parseInt(h), Integer.parseInt(m), Integer.parseInt(s), am, (z == null || z.trim().isEmpty()) ? BuildConfig.FLAVOR : handleZ(z), true);
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
        String ms = mstr == null ? BuildConfig.FLAVOR : mstr.trim();
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
        int y;
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
            if (ystr == null || ystr.trim().equals(BuildConfig.FLAVOR)) {
                y = -1;
            } else {
                int ty = Integer.parseInt(ystr.trim());
                if (ty >= 100 || ty <= -1) {
                    y = ty;
                } else {
                    y = ty + 2000;
                }
            }
        } else {
            y = -1;
        }
        if (m == -1) {
            return null;
        }
        DateTime date = new DateTime();
        date.setDay(y, m, d);
        return date;
    }

    private DateTime parseYDMMM(String content, long defaultTime) {
        Matcher ma = this.rules.getParseByKey(926).matcher(content);
        int y = -1;
        int m = -1;
        int d = -1;
        if (ma.find()) {
            String dstr = "-1";
            String ystr = ma.group(3) != null ? ma.group(3) : dstr;
            String mstr = ma.group(7) != null ? ma.group(7) : dstr;
            if (ma.group(6) != null) {
                dstr = ma.group(6);
            }
            d = Integer.parseInt(dstr.trim());
            m = DataConvertTool.convertMMM(mstr, this.locale, this.localeBk);
            if (ystr != null && !ystr.trim().equals(BuildConfig.FLAVOR)) {
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
        return parseE(content.replaceAll("နေ့၊", BuildConfig.FLAVOR), defaultTime);
    }

    private DateTime parseFullEU(String content, Integer name, long defaultTime) {
        Matcher ma = this.rules.getParseByKey(925).matcher(content);
        int y = -1;
        int m = -1;
        int d = -1;
        if (ma.find()) {
            String dstr = "-1";
            String ystr = ma.group(3) != null ? ma.group(3) : dstr;
            String mstr = ma.group(5) != null ? ma.group(5) : dstr;
            if (ma.group(7) != null) {
                dstr = ma.group(7);
            }
            d = Integer.parseInt(dstr.trim());
            m = DataConvertTool.convertMMM(mstr, this.locale, this.localeBk);
            if (ystr != null && !ystr.trim().equals(BuildConfig.FLAVOR)) {
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
        String m;
        String d;
        int add;
        Matcher ma = this.rules.getDetectByKey(21014).matcher(content);
        DateTime dt = new DateTime();
        if (ma.find()) {
            String y = ma.group(2) != null ? ma.group(2) : "-1";
            if (ma.group(6) != null) {
                m = ma.group(6);
            } else {
                m = "-1";
            }
            if (ma.group(8) != null) {
                d = ma.group(8);
            } else {
                d = "-1";
            }
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
                add = Integer.parseInt(c.convertDigit(m, this.locale)) - 1;
            } else {
                add = cal.get(2) + DataConvertTool.convertRelText(m, this.locale, "param_textmonth", this.localeBk);
            }
            if (c.isDigit(d, this.locale)) {
                day = Integer.parseInt(c.convertDigit(d, this.locale));
            }
            dt.setDay(year, add, day);
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
        return parseHMS(sb.toString(), am != null ? am.trim() : BuildConfig.FLAVOR);
    }

    private DateTime parseE(String content, long defaultTime) {
        DateTime dt = new DateTime();
        if (content == null || content.trim().equals(BuildConfig.FLAVOR)) {
            return dt;
        }
        dt.setDayByWeekValue(DataConvertTool.convertE(content.replace("(", BuildConfig.FLAVOR).replace(")", BuildConfig.FLAVOR), this.locale, this.localeBk), defaultTime);
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
            String z = (z2 == null || z2.trim().isEmpty()) ? BuildConfig.FLAVOR : z2;
            String am = match.group(1) != null ? match.group(1) : BuildConfig.FLAVOR;
            String s = "00";
            String h = match.group(2) != null ? match.group(2) : s;
            String m = match.group(3) != null ? match.group(3) : s;
            if (match.group(5) != null) {
                s = match.group(5);
            }
            dt.setTime(Integer.parseInt(h), Integer.parseInt(m), Integer.parseInt(s), am, !z.trim().isEmpty() ? handleZ(z) : BuildConfig.FLAVOR, true);
        }
        return dt;
    }

    private DateTime parseZAHM(String content, Integer name, long defaultTime) {
        String content2 = DataConvertTool.replace(content, this.locale, this.localeBk);
        StringConvert c = new StringConvert();
        Matcher match = this.rules.getParseByKey(910).matcher(content2);
        DateTime dt = new DateTime();
        if (match.find()) {
            String e = match.group(2);
            if (e != null && !e.trim().isEmpty()) {
                dt.setDayByWeekValue(DataConvertTool.convertE(e, this.locale, this.localeBk), defaultTime);
            }
            String z1 = match.group(4);
            String z = (z1 == null || z1.trim().isEmpty()) ? BuildConfig.FLAVOR : z1;
            String am = match.group(9) != null ? match.group(9) : BuildConfig.FLAVOR;
            String s = "00";
            String h = match.group(10) != null ? match.group(10) : s;
            String m = match.group(12) != null ? match.group(12) : s;
            if (match.group(14) != null) {
                s = match.group(14);
            }
            dt.setTime(Integer.parseInt(c.convertDigit(h, this.locale)), Integer.parseInt(c.convertDigit(m, this.locale)), Integer.parseInt(c.convertDigit(s, this.locale)), am, !z.trim().isEmpty() ? handleZ(z) : BuildConfig.FLAVOR, true);
        }
        return dt;
    }

    private DateTime parseWeek(String content, Integer name, long defaultTime) {
        int rel;
        DateTime dt = new DateTime();
        if (content == null || content.trim().isEmpty() || (rel = DataConvertTool.calRelDays(content, this.locale, this.localeBk)) == -1) {
            return dt;
        }
        dt.setDayByWeekValue(rel, defaultTime);
        return dt;
    }

    private DateTime parseHMS(String content, String ampm) {
        Matcher match = this.rules.getParseByKey(911).matcher(content);
        String hs = "00";
        String ms = "00";
        String ss = "00";
        if (match.find()) {
            String str = "00";
            hs = match.group(1) != null ? match.group(1) : str;
            ms = match.group(2) != null ? match.group(2) : str;
            if (match.group(4) != null) {
                str = match.group(4);
            }
            ss = str;
            Integer.parseInt(hs);
        }
        DateTime dt = new DateTime();
        dt.setTime(Integer.parseInt(hs), Integer.parseInt(ms), Integer.parseInt(ss), ampm != null ? ampm : BuildConfig.FLAVOR, BuildConfig.FLAVOR, true);
        return dt;
    }

    private DateTime parseED(String content, Integer name) {
        Matcher match = this.rules.getParseByKey(906).matcher(new StringConvert().convertDigit(content, this.locale));
        String dstr = "0";
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
        int i2;
        int i3;
        int i4;
        Pattern p = this.rules.getParseByKey(904);
        Pattern p2 = this.rules.getParseByKey(905);
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
                i = 1;
            } else {
                if (name.intValue() == 20015) {
                    i4 = 2;
                } else if (name.intValue() == 21001) {
                    i4 = 2;
                } else {
                    dstr = match.group(1);
                    mstr = match.group(2);
                    ystr = match.group(3);
                    i = 1;
                }
                dstr = match.group(i4);
                mstr = match.group(1);
                ystr = match.group(3);
                i = 1;
            }
        } else if (match2.find()) {
            if (name.intValue() == 20016) {
                i2 = 2;
                i = 1;
            } else if (name.intValue() == 21002) {
                i2 = 2;
                i = 1;
            } else {
                if (name.intValue() == 20015 || name.intValue() == 21001 || name.intValue() == 21014) {
                    i3 = 2;
                } else if (name.intValue() == 21015) {
                    i3 = 2;
                } else {
                    dstr = match2.group(1);
                    mstr = match2.group(2);
                    i = 1;
                }
                dstr = match2.group(i3);
                i = 1;
                mstr = match2.group(1);
            }
            dstr = match2.group(i2);
            mstr = match2.group(i);
        } else {
            i = 1;
        }
        int d = Integer.parseInt(dstr.trim());
        int m = Integer.parseInt(mstr.trim()) - i;
        int ty = Integer.parseInt(ystr.trim());
        if (ty < 100 && ty > -1) {
            ty += 2000;
        }
        DateTime date = new DateTime();
        date.setDay(ty, m, d);
        return date;
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
            if (ystr != null && !ystr.trim().equals(BuildConfig.FLAVOR)) {
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
            if (ystr != null && !ystr.trim().equals(BuildConfig.FLAVOR)) {
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
            if (ystr != null && !ystr.trim().equals(BuildConfig.FLAVOR)) {
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
        boolean isBefore;
        String am;
        DateTime dt = new DateTime();
        String content2 = DataConvertTool.replace(content, this.locale, this.localeBk);
        if (name.equals(31001)) {
            isBefore = true;
        } else {
            isBefore = false;
        }
        Matcher m = this.rules.getParseByKey(907).matcher(content2);
        if (m.find()) {
            String apstr1 = m.group(1) != null ? m.group(1) : BuildConfig.FLAVOR;
            String sstr = "00";
            String hstr = m.group(2) != null ? m.group(2) : sstr;
            String mstr = m.group(3) != null ? m.group(3) : sstr;
            if (m.group(5) != null) {
                sstr = m.group(5);
            }
            String apstr2 = m.group(6) != null ? m.group(6) : BuildConfig.FLAVOR;
            String zstr = m.group(7) != null ? m.group(7) : BuildConfig.FLAVOR;
            String am2 = BuildConfig.FLAVOR;
            if (!apstr1.equals(BuildConfig.FLAVOR)) {
                am2 = apstr1;
            } else if (!apstr2.equals(BuildConfig.FLAVOR)) {
                am = apstr2;
                dt.setTime(Integer.parseInt(hstr), Integer.parseInt(mstr), Integer.parseInt(sstr), am, (zstr != null || zstr.trim().isEmpty()) ? BuildConfig.FLAVOR : handleZ(zstr), isBefore);
            }
            am = am2;
            dt.setTime(Integer.parseInt(hstr), Integer.parseInt(mstr), Integer.parseInt(sstr), am, (zstr != null || zstr.trim().isEmpty()) ? BuildConfig.FLAVOR : handleZ(zstr), isBefore);
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
                hs = "0" + hs.trim();
            }
            String ms = m1.group(4);
            if (ms == null || ms.trim().isEmpty()) {
                ms = "00";
            } else if (ms.trim().length() < 2) {
                ms = "0" + ms.trim();
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
                hs2 = "0" + hs2.trim();
            }
            String ms2 = m2.group(4);
            if (ms2 == null || ms2.trim().isEmpty()) {
                ms2 = "00";
            } else if (ms2.trim().length() < 2) {
                ms2 = "0" + ms2.trim();
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
        String ms2 = BuildConfig.FLAVOR;
        String ms = mstr == null ? ms2 : mstr.trim();
        if (m2str != null) {
            ms2 = m2str.trim();
        }
        int y = ystr == null ? -1 : Integer.parseInt(ystr.trim());
        int m1 = DataConvertTool.convertMMM(ms, this.locale, this.localeBk);
        int m2 = DataConvertTool.convertMMM(ms2, this.locale, this.localeBk);
        if (d1 == -1 || d2 == -1 || m1 == -1) {
            return null;
        }
        if (m2 == -1) {
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
        if (name.intValue() == 40001 || name.intValue() == 41007 || name.intValue() == 41008) {
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
        String ms = mstr == null ? BuildConfig.FLAVOR : mstr.trim();
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
        if (!m.find()) {
            return null;
        }
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
        return new DatePeriod(b, e);
    }

    public DatePeriod parseDateDurYMD(String content, Integer name) {
        Matcher m = this.rules.getParseByKey(920).matcher(content);
        if (!m.find()) {
            return null;
        }
        int y1 = m.group(2) != null ? Integer.parseInt(m.group(2)) : -1;
        int m1 = m.group(3) != null ? Integer.parseInt(m.group(3)) : -1;
        int d1 = m.group(4) != null ? Integer.parseInt(m.group(4)) : -1;
        int m2 = m.group(6) != null ? Integer.parseInt(m.group(6)) : -1;
        int d2 = m.group(7) != null ? Integer.parseInt(m.group(7)) : -1;
        if (d1 == -1 || d2 == -1) {
            return null;
        }
        if (m1 == -1) {
            return null;
        }
        DateTime b = new DateTime();
        b.setDay(y1, m1 - 1, d1);
        DateTime e = new DateTime();
        if (m2 == -1) {
            m2 = m1;
        }
        e.setDay(y1, m2 - 1, d2);
        return new DatePeriod(b, e);
    }

    public DatePeriod parseDateDurYMD2(String content, Integer name) {
        Matcher m = this.rules.getParseByKey(924).matcher(content);
        if (!m.find()) {
            return null;
        }
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
        return new DatePeriod(b, e);
    }
}

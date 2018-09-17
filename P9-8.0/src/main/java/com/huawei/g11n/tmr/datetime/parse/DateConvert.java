package com.huawei.g11n.tmr.datetime.parse;

import com.huawei.g11n.tmr.Filter;
import com.huawei.g11n.tmr.Match;
import com.huawei.g11n.tmr.datetime.utils.DatePeriod;
import com.huawei.g11n.tmr.datetime.utils.LocaleParam;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateConvert {
    private String locale;

    public DateConvert(String locale) {
        this.locale = locale;
    }

    public DatePeriod filterByParse(String content, List<Match> ms, String ps, String dts) {
        if (ms == null || ms.isEmpty()) {
            return null;
        }
        DatePeriod result;
        if (ms.size() != 1) {
            result = convertMutilMatch(content, ms, ps, dts).getDp();
        } else {
            result = ((Match) ms.get(0)).getDp();
        }
        return result;
    }

    private int nestDealDate(String content, Match curren, List<Match> list, int pptype) {
        int result = 0;
        Match next = (Match) list.get(0);
        int type = Filter.getType(next.getRegex());
        if (type != 1 && type != 5 && type != 6) {
            return 0;
        }
        if (type == Filter.getType(curren.getRegex()) || type == pptype) {
            return 0;
        }
        boolean isThree = false;
        String ss = content.substring(curren.getEnd(), next.getBegin());
        if (LocaleParam.isRelDates(ss, this.locale) || ss.trim().equals("(")) {
            if (list.size() > 1) {
                if (nestDealDate(content, next, list.subList(1, list.size()), curren.getType()) == 1) {
                    isThree = true;
                }
            }
            boolean flag15 = false;
            if (ss.trim().equals("(")) {
                Matcher bcm = Pattern.compile("\\s*\\((.*?)\\s*\\)").matcher(content.substring(curren.getEnd()));
                String d = null;
                if (bcm.lookingAt()) {
                    d = bcm.group(1);
                }
                int end = !isThree ? next.getEnd() : ((Match) list.get(1)).getEnd();
                if (d == null) {
                    flag15 = false;
                } else {
                    flag15 = d.trim().equals(content.substring(next.getBegin(), end).trim());
                }
            }
            if (LocaleParam.isRelDates(ss, this.locale) || flag15) {
                result = !isThree ? 1 : 2;
            }
        }
        return result;
    }

    private List<Match> filterDate(String content, List<Match> ms) {
        List<Match> result = new ArrayList();
        int i = 0;
        while (i < ms.size()) {
            Match m = (Match) ms.get(i);
            int type = Filter.getType(m.getRegex());
            if (type == 1 || type == 5 || type == 6 || type == 7) {
                int hasNum = (ms.size() - 1) - i;
                List<Match> sub = null;
                if (hasNum > 1) {
                    sub = ms.subList(i + 1, i + 3);
                } else if (hasNum == 1) {
                    sub = ms.subList(i + 1, i + 2);
                }
                if (hasNum == 0 || sub == null) {
                    result.add(m);
                } else {
                    int status = nestDealDate(content, m, sub, -1);
                    m.setType(1);
                    Match change = null;
                    if (status != 0) {
                        if (status == 1) {
                            i++;
                            if (Filter.getType(m.getRegex()) > Filter.getType(((Match) ms.get(i)).getRegex())) {
                                change = (Match) ms.get(i);
                            }
                        } else if (status == 2) {
                            i += 2;
                            int ct = Filter.getType(m.getRegex());
                            int nt = Filter.getType(((Match) ms.get(i - 1)).getRegex());
                            int nt2 = Filter.getType(((Match) ms.get(i)).getRegex());
                            if (nt < nt2 && nt < ct) {
                                change = (Match) ms.get(i - 1);
                            } else if (nt2 < nt && nt2 < ct) {
                                change = (Match) ms.get(i);
                            }
                        }
                        Match e = (Match) ms.get(i);
                        int add = 0;
                        int be = content.indexOf(40, m.getEnd());
                        if (be != -1 && be < e.getBegin()) {
                            int end = content.indexOf(41, e.getEnd());
                            if (end != -1 && content.substring(e.getEnd(), end + 1).trim().equals(")")) {
                                add = (end - e.getEnd()) + 1;
                            }
                        }
                        m.setEnd(e.getEnd() + add);
                        if (change != null) {
                            m.getDp().setBegin(change.getDp().getBegin());
                        }
                        result.add(m);
                    } else {
                        result.add(m);
                    }
                }
            } else {
                result.add(m);
            }
            i++;
        }
        return result;
    }

    private List<Match> filterPeriod(String content, List<Match> ms, String ps) {
        Pattern rel = Pattern.compile("\\.?\\s*(-{1,2}|~|起?至|到|au|–|—|～|تا|देखि|да|па|থেকে|ຫາ" + ps + ")\\s*", 2);
        Iterator<Match> it = ms.iterator();
        Match curren = null;
        while (it.hasNext()) {
            Match m = (Match) it.next();
            if (curren != null) {
                int ctype = curren.getDp().getType();
                int ntype = m.getDp().getType();
                if ((ctype != ntype || (ctype != 1 && ctype != 2 && ctype != 0)) && (ctype != 0 || ntype != 2)) {
                    curren = m;
                } else if (rel.matcher(content.substring(curren.getEnd(), m.getBegin())).matches()) {
                    curren.setEnd(m.getEnd());
                    curren.setType(3);
                    if (ctype == 2 && ntype == 2) {
                        curren.setIsTimePeriod(true);
                    }
                    curren.getDp().setEnd(m.getDp().getBegin());
                    it.remove();
                }
            } else {
                curren = m;
            }
        }
        return ms;
    }

    private List<Match> filterDateTime(String content, List<Match> ms, String DTBridge) {
        Pattern dt = Pattern.compile("\\s*(at|às|،‏|u|kl\\.|को|的|o|à|a\\s+les|ve|la|pada|kl|στις|alle|jam|ຂອງວັນທີ" + DTBridge + ")\\s*", 2);
        Iterator<Match> it = ms.iterator();
        Match curren = null;
        while (it.hasNext()) {
            Match m = (Match) it.next();
            if (curren != null) {
                int ctype = curren.getDp().getType();
                int type = m.getDp().getType();
                curren.setType(ctype);
                m.setType(type);
                if ((ctype == 1 && type == 2) || ((ctype == 1 && type == 5) || ((ctype == 2 && type == 1) || (ctype == 5 && type == 1)))) {
                    boolean flag;
                    String ss = content.substring(curren.getEnd(), m.getBegin());
                    if (ss.trim().equals("")) {
                        flag = true;
                    } else {
                        flag = dt.matcher(ss).matches();
                    }
                    if (flag) {
                        curren.setEnd(m.getEnd());
                        if (ctype == 1 && type == 2) {
                            curren.getDp().getBegin().setTime(m.getDp().getBegin().getTime());
                        } else if (ctype == 2 && type == 1) {
                            curren.getDp().getBegin().setDay(m.getDp().getBegin().getDate());
                        } else if (ctype == 1 && type == 5) {
                            curren.getDp().getBegin().setTime(m.getDp().getBegin().getTime());
                            curren.getDp().setEnd(m.getDp().getEnd());
                        } else if (ctype == 5 && type == 1) {
                            curren.getDp().getBegin().setDay(m.getDp().getBegin().getDate());
                            curren.getDp().getEnd().setDay(m.getDp().getBegin().getDate());
                        }
                        it.remove();
                    } else {
                        boolean change = true;
                        if (ctype == 2 && type == 1) {
                            boolean flag15;
                            int add = 0;
                            Matcher bcm = Pattern.compile("\\s*\\((.*?)\\s*\\)").matcher(content.substring(curren.getEnd()));
                            String d = null;
                            if (bcm.lookingAt()) {
                                d = bcm.group(1);
                                add = bcm.group().length();
                            }
                            if (d == null) {
                                flag15 = false;
                            } else {
                                flag15 = d.trim().equals(content.substring(m.getBegin(), m.getEnd()).trim());
                            }
                            if (flag15) {
                                curren.setEnd(curren.getEnd() + add);
                                curren.getDp().getBegin().setDay(m.getDp().getBegin().getDate());
                                it.remove();
                                change = false;
                            }
                        }
                        if (ctype == 1 && type == 2) {
                            String bStr = content.substring(0, curren.getBegin());
                            String eStr = content.substring(curren.getEnd(), m.getBegin());
                            if (bStr.trim().endsWith("(") && eStr.trim().startsWith(")")) {
                                curren.setBegin(bStr.lastIndexOf(40));
                                curren.setEnd(m.getEnd());
                                curren.getDp().getBegin().setTime(m.getDp().getBegin().getTime());
                                it.remove();
                                change = false;
                            }
                        }
                        if (change) {
                            curren = m;
                        }
                    }
                } else {
                    curren = m;
                }
            } else {
                curren = m;
            }
        }
        return ms;
    }

    private Match filterDateTimePunc(String content, List<Match> ms) {
        Iterator<Match> it = ms.iterator();
        Match curren = null;
        while (it.hasNext()) {
            Match m = (Match) it.next();
            if (curren != null) {
                int ctype = curren.getType();
                int type = m.getType();
                boolean flag = false;
                if ((ctype == 1 && type == 2) || ((ctype == 1 && type == 3 && m.isTimePeriod()) || ((ctype == 2 && type == 1) || (ctype == 3 && curren.isTimePeriod() && type == 1)))) {
                    String ss = content.substring(curren.getEnd(), m.getBegin());
                    if (ss.trim().equals("，") || ss.trim().equals(",")) {
                        flag = true;
                    }
                    if (flag) {
                        curren.setEnd(m.getEnd());
                        if ((ctype == 1 && type == 2) || (ctype == 2 && type == 1)) {
                            curren.setType(0);
                            curren.getDp().getBegin().setTime(m.getDp().getBegin().getTime());
                        } else {
                            curren.setType(3);
                            curren.getDp().getBegin().setTime(m.getDp().getBegin().getTime());
                            curren.getDp().setEnd(m.getDp().getEnd());
                        }
                        it.remove();
                    } else {
                        curren = m;
                    }
                } else {
                    curren = m;
                }
            } else {
                curren = m;
            }
        }
        return curren;
    }

    private Match convertMutilMatch(String content, List<Match> ms, String ps, String dts) {
        return filterDateTimePunc(content, filterPeriod(content, filterDateTime(content, filterDate(content, ms), dts), ps));
    }

    public List<Date> convert(DatePeriod dp, long rTime) {
        List<Date> result = new ArrayList();
        if (dp == null) {
            return null;
        }
        int status = dp.getType();
        int year;
        int month;
        int day;
        int hour;
        int min;
        int sec;
        String tm;
        String tz;
        StringBuffer t;
        StringBuffer s;
        Calendar c;
        Calendar c2;
        if (status == 0) {
            year = dp.getBegin().getDate().getYear();
            month = dp.getBegin().getDate().getMonth();
            day = dp.getBegin().getDate().getDay();
            hour = dp.getBegin().getTime().getClock();
            min = dp.getBegin().getTime().getMinute();
            sec = dp.getBegin().getTime().getSecond();
            tm = dp.getBegin().getTime() == null ? "" : dp.getBegin().getTime().getMark();
            tz = dp.getBegin().getTime() == null ? "" : dp.getBegin().getTime().getTimezone();
            t = new StringBuffer();
            s = new StringBuffer();
            if (hour > 12 && !tm.trim().equals("")) {
                tm = "";
            }
            s.append(hour == -1 ? "00" : Integer.valueOf(hour)).append(":").append(min == -1 ? "00" : Integer.valueOf(min)).append(":").append(sec == -1 ? "00" : Integer.valueOf(sec));
            if (tm.equals("")) {
                t.append("HH").append(":mm:ss");
            } else {
                t.append("hh").append(":mm:ss").append(" a");
                s.append(" ").append(tm);
            }
            if (!tz.equals("")) {
                t.append(" Z");
                s.append(" ").append(tz);
            }
            Date d1 = null;
            try {
                d1 = new SimpleDateFormat(t.toString(), Locale.ENGLISH).parse(s.toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            c = Calendar.getInstance();
            c2 = Calendar.getInstance();
            c2.setTime(new Date(rTime));
            if (d1 != null) {
                c.setTime(d1);
            }
            if (year == -1) {
                c.set(1, c2.get(1));
            } else {
                c.set(1, year);
            }
            if (month == -1) {
                c.set(2, c2.get(2));
            } else {
                c.set(2, month);
            }
            if (day == -1) {
                c.set(5, c2.get(5));
            } else {
                c.set(5, day);
            }
            result.add(c.getTime());
        } else if (status == 1) {
            year = dp.getBegin().getDate().getYear();
            month = dp.getBegin().getDate().getMonth();
            day = dp.getBegin().getDate().getDay();
            c = Calendar.getInstance();
            c2 = Calendar.getInstance();
            c2.setTime(new Date(rTime));
            if (year == -1) {
                c.set(1, c2.get(1));
            } else {
                c.set(1, year);
            }
            if (month == -1) {
                c.set(2, c2.get(2));
            } else {
                c.set(2, month);
            }
            if (day == -1) {
                c.set(5, c2.get(5));
            } else {
                c.set(5, day);
            }
            c.set(11, 0);
            c.set(12, 0);
            c.set(13, 0);
            result.add(c.getTime());
        } else if (status == 2) {
            hour = dp.getBegin().getTime().getClock();
            min = dp.getBegin().getTime().getMinute();
            sec = dp.getBegin().getTime().getSecond();
            tm = dp.getBegin().getTime() == null ? "" : dp.getBegin().getTime().getMark();
            tz = dp.getBegin().getTime() == null ? "" : dp.getBegin().getTime().getTimezone();
            t = new StringBuffer();
            s = new StringBuffer();
            t.append("yyyy-MM-dd ");
            c = Calendar.getInstance();
            c.setTime(new Date(rTime));
            s.append(c.get(1)).append("-").append(c.get(2) + 1).append("-").append(c.get(5)).append(" ");
            if (hour > 12 && !tm.trim().equals("")) {
                tm = "";
            }
            s.append(hour == -1 ? "00" : Integer.valueOf(hour)).append(":").append(min == -1 ? "00" : Integer.valueOf(min)).append(":").append(sec == -1 ? "00" : Integer.valueOf(sec));
            if (tm.equals("")) {
                t.append("HH").append(":mm:ss");
            } else {
                t.append("hh").append(":mm:ss").append(" a");
                s.append(" ").append(tm);
            }
            if (!tz.equals("")) {
                t.append(" Z");
                s.append(" ").append(tz);
            }
            Object d12 = null;
            try {
                d12 = new SimpleDateFormat(t.toString(), Locale.ENGLISH).parse(s.toString());
            } catch (ParseException e2) {
                e2.printStackTrace();
            }
            result.add(d12);
        } else if (status > 2) {
            year = dp.getBegin().getDate() == null ? -1 : dp.getBegin().getDate().getYear();
            month = dp.getBegin().getDate() == null ? -1 : dp.getBegin().getDate().getMonth();
            day = dp.getBegin().getDate() == null ? -1 : dp.getBegin().getDate().getDay();
            hour = dp.getBegin().getTime() == null ? -1 : dp.getBegin().getTime().getClock();
            min = dp.getBegin().getTime() == null ? -1 : dp.getBegin().getTime().getMinute();
            sec = dp.getBegin().getTime() == null ? -1 : dp.getBegin().getTime().getSecond();
            tm = dp.getBegin().getTime() == null ? "" : dp.getBegin().getTime().getMark();
            tz = dp.getBegin().getTime() == null ? "" : dp.getBegin().getTime().getTimezone();
            int year2 = dp.getEnd().getDate() == null ? -1 : dp.getEnd().getDate().getYear();
            int month2 = dp.getEnd().getDate() == null ? -1 : dp.getEnd().getDate().getMonth();
            int day2 = dp.getEnd().getDate() == null ? -1 : dp.getEnd().getDate().getDay();
            int hour2 = dp.getEnd().getTime() == null ? -1 : dp.getEnd().getTime().getClock();
            int min2 = dp.getEnd().getTime() == null ? -1 : dp.getEnd().getTime().getMinute();
            int sec2 = dp.getEnd().getTime() == null ? -1 : dp.getEnd().getTime().getSecond();
            String tm2 = dp.getEnd().getTime() == null ? "" : dp.getEnd().getTime().getMark();
            String tz2 = dp.getEnd().getTime() == null ? "" : dp.getEnd().getTime().getTimezone();
            boolean isBefore1 = dp.getBegin().getTime() == null ? true : dp.getBegin().getTime().isMarkBefore();
            boolean isBefore2 = dp.getEnd().getTime() == null ? true : dp.getEnd().getTime().isMarkBefore();
            int dtst = dp.getBegin().getSatuts();
            int dt2st = dp.getEnd().getSatuts();
            if ((dtst == 0 || dtst == 1) && (dt2st == 0 || dt2st == 1)) {
                if (year != -1 && year2 == -1) {
                    year2 = year;
                }
                if (year == -1 && year2 != -1) {
                    year = year2;
                }
                if (month != -1 && month2 == -1) {
                    month2 = month;
                }
                if (month == -1 && month2 != -1) {
                    month = month2;
                }
            }
            if ((dtst == 0 || dtst == 1) && dt2st == 2) {
                if (year != -1 && year2 == -1) {
                    year2 = year;
                }
                if (month != -1 && month2 == -1) {
                    month2 = month;
                }
                if (day != -1 && day2 == -1) {
                    day2 = day;
                }
            }
            if ((dt2st == 0 || dt2st == 1) && dtst == 2) {
                if (year2 != -1 && year == -1) {
                    year = year2;
                }
                if (month2 != -1 && month == -1) {
                    month = month2;
                }
                if (day2 != -1 && day == -1) {
                    day = day2;
                }
            }
            String tzone = "";
            if (dtst == 0 || dtst == 2 || dt2st == 0 || dt2st == 2) {
                boolean isBefore;
                if (tm.trim().equals("") && !tm2.trim().equals("")) {
                    isBefore = isBefore2;
                    if (!(year == year2 && month == month2 && day == day2)) {
                        isBefore = true;
                    }
                    if (!isBefore) {
                        tm = tm2;
                    }
                } else if (tm2.trim().equals("") && !tm.trim().equals("")) {
                    isBefore = isBefore1;
                    if (year == year2 && month == month2 && day == day2) {
                        isBefore = true;
                    } else {
                        isBefore = false;
                    }
                    if (isBefore) {
                        tm2 = tm;
                    }
                }
                if (!tz2.equals("")) {
                    tzone = tz2;
                } else if (tz2.equals("") && !tz.equals("")) {
                    tzone = tz;
                }
            }
            c = Calendar.getInstance();
            t = new StringBuffer();
            s = new StringBuffer();
            if (hour > 12 && !tm.trim().equals("")) {
                tm = "";
            }
            s.append(hour == -1 ? "08" : Integer.valueOf(hour)).append(":").append(min == -1 ? "00" : Integer.valueOf(min)).append(":").append(sec == -1 ? "00" : Integer.valueOf(sec));
            if (tm.equals("")) {
                t.append("HH").append(":mm:ss");
            } else {
                t.append("hh").append(":mm:ss").append(" a");
                s.append(" ").append(tm);
            }
            if (!tzone.equals("")) {
                t.append(" Z");
                s.append(" ").append(tzone);
            }
            try {
                c.setTime(new SimpleDateFormat(t.toString(), Locale.ENGLISH).parse(s.toString()));
            } catch (ParseException e22) {
                e22.printStackTrace();
            }
            Date d = new Date(rTime);
            Calendar tc = Calendar.getInstance();
            tc.setTime(d);
            if (year == -1) {
                c.set(1, tc.get(1));
            } else {
                c.set(1, year);
            }
            if (month == -1) {
                c.set(2, tc.get(2));
            } else {
                c.set(2, month);
            }
            if (day == -1) {
                c.set(5, tc.get(5));
            } else {
                c.set(5, day);
            }
            result.add(c.getTime());
            c2 = Calendar.getInstance();
            StringBuffer t2 = new StringBuffer();
            StringBuffer s2 = new StringBuffer();
            if (hour2 > 12 && !tm2.trim().equals("")) {
                tm2 = "";
            }
            s2.append(hour2 == -1 ? "08" : Integer.valueOf(hour2)).append(":").append(min2 == -1 ? "00" : Integer.valueOf(min2)).append(":").append(sec2 == -1 ? "00" : Integer.valueOf(sec2));
            if (tm2.equals("")) {
                t2.append("HH").append(":mm:ss");
            } else {
                t2.append("hh").append(":mm:ss").append(" a");
                s2.append(" ").append(tm2);
            }
            if (!tzone.equals("")) {
                t2.append(" Z");
                s2.append(" ").append(tzone);
            }
            try {
                c2.setTime(new SimpleDateFormat(t2.toString(), Locale.ENGLISH).parse(s2.toString()));
            } catch (ParseException e222) {
                e222.printStackTrace();
            }
            if (year2 == -1) {
                c2.set(1, tc.get(1));
            } else {
                c2.set(1, year2);
            }
            if (month2 == -1) {
                c2.set(2, tc.get(2));
            } else {
                c2.set(2, month2);
            }
            if (day2 == -1) {
                c2.set(5, tc.get(5));
            } else {
                c2.set(5, day2);
            }
            result.add(c2.getTime());
        }
        return result;
    }
}

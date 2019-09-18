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

    public DateConvert(String locale2) {
        this.locale = locale2;
    }

    public DatePeriod filterByParse(String content, List<Match> ms, String ps, String dts) {
        DatePeriod result;
        if (ms == null || ms.isEmpty()) {
            return null;
        }
        if (ms.size() == 1) {
            result = ms.get(0).getDp();
        } else {
            result = convertMutilMatch(content, ms, ps, dts).getDp();
        }
        return result;
    }

    private int nestDealDate(String content, Match curren, List<Match> list, int pptype) {
        String str = content;
        List<Match> list2 = list;
        int result = 0;
        boolean z = false;
        Match next = list2.get(0);
        int type = Filter.getType(next.getRegex());
        if (type != 1 && type != 5 && type != 6) {
            return 0;
        }
        if (type == Filter.getType(curren.getRegex())) {
            int i = pptype;
        } else if (type != pptype) {
            boolean isThree = false;
            String ss = str.substring(curren.getEnd(), next.getBegin());
            if (LocaleParam.isRelDates(ss, this.locale) || ss.trim().equals("(")) {
                if (list.size() > 1 && nestDealDate(str, next, list2.subList(1, list.size()), curren.getType()) == 1) {
                    isThree = true;
                }
                boolean flag15 = false;
                if (ss.trim().equals("(")) {
                    Matcher bcm = Pattern.compile("\\s*\\((.*?)\\s*\\)").matcher(str.substring(curren.getEnd()));
                    String d = null;
                    if (bcm.lookingAt()) {
                        d = bcm.group(1);
                    }
                    int end = isThree ? list2.get(1).getEnd() : next.getEnd();
                    if (d != null) {
                        z = d.trim().equals(str.substring(next.getBegin(), end).trim());
                    }
                    flag15 = z;
                }
                if (LocaleParam.isRelDates(ss, this.locale) || flag15) {
                    result = isThree ? 2 : 1;
                }
            }
            return result;
        }
        return 0;
    }

    private List<Match> filterDate(String content, List<Match> ms) {
        String str = content;
        List<Match> list = ms;
        List<Match> result = new ArrayList<>();
        int i = 0;
        while (i < ms.size()) {
            Match m = list.get(i);
            int type = Filter.getType(m.getRegex());
            if (type == 1 || type == 5 || type == 6 || type == 7) {
                int hasNum = (ms.size() - 1) - i;
                List<Match> sub = null;
                if (hasNum > 1) {
                    sub = list.subList(i + 1, i + 3);
                } else if (hasNum == 1) {
                    sub = list.subList(i + 1, i + 2);
                }
                if (hasNum == 0 || sub == null) {
                    result.add(m);
                } else {
                    int status = nestDealDate(str, m, sub, -1);
                    m.setType(1);
                    Match change = null;
                    if (status == 0) {
                        result.add(m);
                    } else {
                        if (status == 1) {
                            i++;
                            if (Filter.getType(m.getRegex()) > Filter.getType(list.get(i).getRegex())) {
                                change = list.get(i);
                            }
                        } else if (status == 2) {
                            i += 2;
                            int ct = Filter.getType(m.getRegex());
                            int nt = Filter.getType(list.get(i - 1).getRegex());
                            int nt2 = Filter.getType(list.get(i).getRegex());
                            if (nt < nt2 && nt < ct) {
                                change = list.get(i - 1);
                            } else if (nt2 < nt && nt2 < ct) {
                                change = list.get(i);
                            }
                        }
                        Match e = list.get(i);
                        int add = 0;
                        int be = str.indexOf(40, m.getEnd());
                        if (be != -1 && be < e.getBegin()) {
                            int end = str.indexOf(41, e.getEnd());
                            if (end != -1 && str.substring(e.getEnd(), end + 1).trim().equals(")")) {
                                add = (end - e.getEnd()) + 1;
                            }
                        }
                        m.setEnd(e.getEnd() + add);
                        if (change != null) {
                            m.getDp().setBegin(change.getDp().getBegin());
                        }
                        result.add(m);
                    }
                }
            } else {
                result.add(m);
            }
            i++;
            str = content;
            list = ms;
        }
        return result;
    }

    private List<Match> filterPeriod(String content, List<Match> ms, String ps) {
        Pattern rel = Pattern.compile("\\.?\\s*(-{1,2}|~|起?至|到|au|–|—|～|تا|देखि|да|па|থেকে|ຫາ" + ps + ")\\s*", 2);
        Iterator<Match> it = ms.iterator();
        Match curren = null;
        while (it.hasNext()) {
            Match m = it.next();
            if (curren == null) {
                curren = m;
            } else {
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
            }
        }
        return ms;
    }

    private List<Match> filterDateTime(String content, List<Match> ms, String DTBridge) {
        Pattern dt;
        int i;
        boolean flag;
        boolean flag15;
        String str = content;
        int i2 = 2;
        Pattern dt2 = Pattern.compile("\\s*(at|às|،‏|u|kl\\.|को|的|o|à|a\\s+les|ve|la|pada|kl|στις|alle|jam|ຂອງວັນທີ" + DTBridge + ")\\s*", 2);
        Iterator<Match> it = ms.iterator();
        Match curren = null;
        while (it.hasNext()) {
            Match m = it.next();
            if (curren == null) {
                curren = m;
            } else {
                int ctype = curren.getDp().getType();
                int type = m.getDp().getType();
                curren.setType(ctype);
                m.setType(type);
                if ((ctype == 1 && type == i2) || ((ctype == 1 && type == 5) || ((ctype == i2 && type == 1) || (ctype == 5 && type == 1)))) {
                    String ss = str.substring(curren.getEnd(), m.getBegin());
                    if (ss.trim().equals("")) {
                        flag = true;
                    } else {
                        flag = dt2.matcher(ss).matches();
                    }
                    if (flag) {
                        curren.setEnd(m.getEnd());
                        if (ctype == 1 && type == i2) {
                            curren.getDp().getBegin().setTime(m.getDp().getBegin().getTime());
                        } else if (ctype == i2 && type == 1) {
                            curren.getDp().getBegin().setDay(m.getDp().getBegin().getDate());
                        } else if (ctype == 1 && type == 5) {
                            curren.getDp().getBegin().setTime(m.getDp().getBegin().getTime());
                            curren.getDp().setEnd(m.getDp().getEnd());
                        } else if (ctype == 5 && type == 1) {
                            curren.getDp().getBegin().setDay(m.getDp().getBegin().getDate());
                            curren.getDp().getEnd().setDay(m.getDp().getBegin().getDate());
                        }
                        it.remove();
                        dt = dt2;
                        i = i2;
                    } else {
                        boolean change = true;
                        if (ctype == i2 && type == 1) {
                            int add = 0;
                            Matcher bcm = Pattern.compile("\\s*\\((.*?)\\s*\\)").matcher(str.substring(curren.getEnd()));
                            String d = null;
                            if (bcm.lookingAt()) {
                                d = bcm.group(1);
                                add = bcm.group().length();
                            }
                            String d2 = d;
                            if (d2 != null) {
                                dt = dt2;
                                Matcher matcher = bcm;
                                flag15 = d2.trim().equals(str.substring(m.getBegin(), m.getEnd()).trim());
                            } else {
                                dt = dt2;
                                Matcher matcher2 = bcm;
                                flag15 = false;
                            }
                            if (flag15) {
                                curren.setEnd(curren.getEnd() + add);
                                curren.getDp().getBegin().setDay(m.getDp().getBegin().getDate());
                                it.remove();
                                change = false;
                            }
                        } else {
                            dt = dt2;
                        }
                        if (ctype == 1) {
                            i = 2;
                            if (type == 2) {
                                String bStr = str.substring(0, curren.getBegin());
                                String eStr = str.substring(curren.getEnd(), m.getBegin());
                                if (bStr.trim().endsWith("(") && eStr.trim().startsWith(")")) {
                                    curren.setBegin(bStr.lastIndexOf(40));
                                    curren.setEnd(m.getEnd());
                                    curren.getDp().getBegin().setTime(m.getDp().getBegin().getTime());
                                    it.remove();
                                    change = false;
                                }
                            }
                        } else {
                            i = 2;
                        }
                        if (change) {
                            curren = m;
                        }
                    }
                } else {
                    dt = dt2;
                    i = i2;
                    curren = m;
                }
                i2 = i;
                dt2 = dt;
                String str2 = DTBridge;
            }
        }
        return ms;
    }

    private Match filterDateTimePunc(String content, List<Match> ms) {
        Iterator<Match> it = ms.iterator();
        Match curren = null;
        while (it.hasNext()) {
            Match m = it.next();
            if (curren == null) {
                curren = m;
            } else {
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
                        if (ctype == 1 && type == 2) {
                            curren.setType(0);
                            curren.getDp().getBegin().setTime(m.getDp().getBegin().getTime());
                        } else if (ctype == 2 && type == 1) {
                            curren.setType(0);
                            curren.getDp().getBegin().setDay(m.getDp().getBegin().getDate());
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
            }
        }
        return curren;
    }

    private Match convertMutilMatch(String content, List<Match> ms, String ps, String dts) {
        return filterDateTimePunc(content, filterPeriod(content, filterDateTime(content, filterDate(content, ms), dts), ps));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:190:0x052c, code lost:
        if (r11 == 1) goto L_0x0530;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:242:0x0585, code lost:
        if (r10 != 2) goto L_0x0588;
     */
    /* JADX WARNING: Removed duplicated region for block: B:273:0x05ee  */
    /* JADX WARNING: Removed duplicated region for block: B:274:0x05f0  */
    /* JADX WARNING: Removed duplicated region for block: B:287:0x0634  */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0639  */
    /* JADX WARNING: Removed duplicated region for block: B:291:0x064e  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0653  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0664  */
    /* JADX WARNING: Removed duplicated region for block: B:296:0x0669  */
    /* JADX WARNING: Removed duplicated region for block: B:299:0x0676  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0690  */
    /* JADX WARNING: Removed duplicated region for block: B:303:0x06a4  */
    /* JADX WARNING: Removed duplicated region for block: B:311:0x06e7  */
    /* JADX WARNING: Removed duplicated region for block: B:312:0x06ec  */
    /* JADX WARNING: Removed duplicated region for block: B:315:0x06f7  */
    /* JADX WARNING: Removed duplicated region for block: B:316:0x06fc  */
    /* JADX WARNING: Removed duplicated region for block: B:319:0x0707  */
    /* JADX WARNING: Removed duplicated region for block: B:320:0x070c  */
    /* JADX WARNING: Removed duplicated region for block: B:328:0x0741  */
    /* JADX WARNING: Removed duplicated region for block: B:329:0x0746  */
    /* JADX WARNING: Removed duplicated region for block: B:332:0x075b  */
    /* JADX WARNING: Removed duplicated region for block: B:333:0x0760  */
    /* JADX WARNING: Removed duplicated region for block: B:336:0x0771  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0776  */
    /* JADX WARNING: Removed duplicated region for block: B:340:0x0783  */
    /* JADX WARNING: Removed duplicated region for block: B:341:0x079d  */
    /* JADX WARNING: Removed duplicated region for block: B:344:0x07b1  */
    /* JADX WARNING: Removed duplicated region for block: B:352:0x07e1  */
    /* JADX WARNING: Removed duplicated region for block: B:353:0x07e6  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x07f1  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x07f6  */
    /* JADX WARNING: Removed duplicated region for block: B:360:0x0801  */
    /* JADX WARNING: Removed duplicated region for block: B:361:0x0806  */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x0815  */
    /* JADX WARNING: Removed duplicated region for block: B:368:0x0826  */
    public List<Date> convert(DatePeriod dp, long rTime) {
        List<Date> result;
        int hour;
        String tm2;
        String tzone;
        Calendar c;
        StringBuffer t;
        Calendar c2;
        StringBuffer t2;
        boolean isBefore;
        StringBuffer t3;
        long j = rTime;
        List<Date> result2 = new ArrayList<>();
        if (dp == null) {
            return null;
        }
        int status = dp.getType();
        if (status == 0) {
            int year = dp.getBegin().getDate().getYear();
            int month = dp.getBegin().getDate().getMonth();
            int day = dp.getBegin().getDate().getDay();
            int hour2 = dp.getBegin().getTime().getClock();
            int min = dp.getBegin().getTime().getMinute();
            int sec = dp.getBegin().getTime().getSecond();
            String tm = dp.getBegin().getTime() != null ? dp.getBegin().getTime().getMark() : "";
            String tz = dp.getBegin().getTime() != null ? dp.getBegin().getTime().getTimezone() : "";
            StringBuffer t4 = new StringBuffer();
            StringBuffer s = new StringBuffer();
            if (hour2 > 12 && !tm.trim().equals("")) {
                tm = "";
            }
            StringBuffer s2 = s;
            s2.append(hour2 != -1 ? Integer.valueOf(hour2) : "00");
            s2.append(":");
            s2.append(min != -1 ? Integer.valueOf(min) : "00");
            s2.append(":");
            s2.append(sec != -1 ? Integer.valueOf(sec) : "00");
            if (!tm.equals("")) {
                t3 = t4;
                t3.append("hh");
                t3.append(":mm:ss");
                t3.append(" a");
                s2.append(" ");
                s2.append(tm);
            } else {
                t3 = t4;
                t3.append("HH");
                t3.append(":mm:ss");
            }
            if (!tz.equals("")) {
                t3.append(" Z");
                s2.append(" ");
                s2.append(tz);
            }
            int i = sec;
            String str = tm;
            SimpleDateFormat for1 = new SimpleDateFormat(t3.toString(), Locale.ENGLISH);
            Date d1 = null;
            try {
                d1 = for1.parse(s2.toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar c3 = Calendar.getInstance();
            StringBuffer stringBuffer = s2;
            Calendar c22 = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = for1;
            c22.setTime(new Date(j));
            if (d1 != null) {
                c3.setTime(d1);
            }
            if (year != -1) {
                c3.set(1, year);
                Date date = d1;
            } else {
                Date date2 = d1;
                c3.set(1, c22.get(1));
            }
            if (month != -1) {
                c3.set(2, month);
            } else {
                c3.set(2, c22.get(2));
            }
            if (day != -1) {
                c3.set(5, day);
            } else {
                c3.set(5, c22.get(5));
            }
            result2.add(c3.getTime());
        } else if (status == 1) {
            int year2 = dp.getBegin().getDate().getYear();
            int month2 = dp.getBegin().getDate().getMonth();
            int day2 = dp.getBegin().getDate().getDay();
            Calendar c4 = Calendar.getInstance();
            Calendar c23 = Calendar.getInstance();
            c23.setTime(new Date(j));
            if (year2 != -1) {
                c4.set(1, year2);
            } else {
                c4.set(1, c23.get(1));
            }
            if (month2 != -1) {
                c4.set(2, month2);
            } else {
                c4.set(2, c23.get(2));
            }
            if (day2 != -1) {
                c4.set(5, day2);
            } else {
                c4.set(5, c23.get(5));
            }
            c4.set(11, 0);
            c4.set(12, 0);
            c4.set(13, 0);
            result2.add(c4.getTime());
        } else if (status == 2) {
            int hour3 = dp.getBegin().getTime().getClock();
            int min2 = dp.getBegin().getTime().getMinute();
            int sec2 = dp.getBegin().getTime().getSecond();
            String tm3 = dp.getBegin().getTime() != null ? dp.getBegin().getTime().getMark() : "";
            String tz2 = dp.getBegin().getTime() != null ? dp.getBegin().getTime().getTimezone() : "";
            StringBuffer t5 = new StringBuffer();
            StringBuffer s3 = new StringBuffer();
            t5.append("yyyy-MM-dd ");
            Calendar c5 = Calendar.getInstance();
            c5.setTime(new Date(j));
            s3.append(c5.get(1));
            s3.append("-");
            s3.append(c5.get(2) + 1);
            s3.append("-");
            s3.append(c5.get(5));
            s3.append(" ");
            if (hour3 > 12 && !tm3.trim().equals("")) {
                tm3 = "";
            }
            String tm4 = tm3;
            s3.append(hour3 != -1 ? Integer.valueOf(hour3) : "00");
            s3.append(":");
            s3.append(min2 != -1 ? Integer.valueOf(min2) : "00");
            s3.append(":");
            s3.append(sec2 != -1 ? Integer.valueOf(sec2) : "00");
            if (!tm4.equals("")) {
                t5.append("hh");
                t5.append(":mm:ss");
                t5.append(" a");
                s3.append(" ");
                s3.append(tm4);
            } else {
                t5.append("HH");
                t5.append(":mm:ss");
            }
            if (!tz2.equals("")) {
                t5.append(" Z");
                s3.append(" ");
                s3.append(tz2);
            }
            Date d12 = null;
            try {
                d12 = new SimpleDateFormat(t5.toString(), Locale.ENGLISH).parse(s3.toString());
            } catch (ParseException e2) {
                e2.printStackTrace();
            }
            result2.add(d12);
        } else {
            if (status > 2) {
                int year3 = dp.getBegin().getDate() != null ? dp.getBegin().getDate().getYear() : -1;
                int month3 = dp.getBegin().getDate() != null ? dp.getBegin().getDate().getMonth() : -1;
                int day3 = dp.getBegin().getDate() != null ? dp.getBegin().getDate().getDay() : -1;
                int hour4 = dp.getBegin().getTime() != null ? dp.getBegin().getTime().getClock() : -1;
                int min3 = dp.getBegin().getTime() != null ? dp.getBegin().getTime().getMinute() : -1;
                int sec3 = dp.getBegin().getTime() != null ? dp.getBegin().getTime().getSecond() : -1;
                String tm5 = dp.getBegin().getTime() != null ? dp.getBegin().getTime().getMark() : "";
                String tz3 = dp.getBegin().getTime() != null ? dp.getBegin().getTime().getTimezone() : "";
                int year22 = dp.getEnd().getDate() != null ? dp.getEnd().getDate().getYear() : -1;
                int month22 = dp.getEnd().getDate() != null ? dp.getEnd().getDate().getMonth() : -1;
                int day22 = dp.getEnd().getDate() != null ? dp.getEnd().getDate().getDay() : -1;
                List<Date> result3 = result2;
                int hour22 = dp.getEnd().getTime() != null ? dp.getEnd().getTime().getClock() : -1;
                int status2 = status;
                int min22 = dp.getEnd().getTime() != null ? dp.getEnd().getTime().getMinute() : -1;
                int sec22 = dp.getEnd().getTime() != null ? dp.getEnd().getTime().getSecond() : -1;
                String tm22 = dp.getEnd().getTime() != null ? dp.getEnd().getTime().getMark() : "";
                int hour23 = hour22;
                String tz22 = dp.getEnd().getTime() != null ? dp.getEnd().getTime().getTimezone() : "";
                boolean isBefore1 = dp.getBegin().getTime() != null ? dp.getBegin().getTime().isMarkBefore() : true;
                boolean isBefore2 = dp.getEnd().getTime() != null ? dp.getEnd().getTime().isMarkBefore() : true;
                int sec4 = sec3;
                int sec5 = dp.getBegin().getSatuts();
                int min4 = min3;
                int min5 = dp.getEnd().getSatuts();
                int i2 = sec5 != 0 ? 1 : 1;
                if (min5 == 0 || min5 == i2) {
                    if (year3 != -1 && year22 == -1) {
                        year22 = year3;
                    }
                    if (year3 == -1 && year22 != -1) {
                        year3 = year22;
                    }
                    if (month3 != -1 && month22 == -1) {
                        month22 = month3;
                    }
                    if (month3 == -1 && month22 != -1) {
                        month3 = month22;
                    }
                }
                if ((sec5 == 0 || sec5 == 1) && min5 == 2) {
                    if (year3 != -1 && year22 == -1) {
                        year22 = year3;
                    }
                    if (month3 != -1 && month22 == -1) {
                        month22 = month3;
                    }
                    if (day3 != -1 && day22 == -1) {
                        day22 = day3;
                    }
                }
                if ((min5 == 0 || min5 == 1) && sec5 == 2) {
                    if (year22 != -1 && year3 == -1) {
                        year3 = year22;
                    }
                    if (month22 != -1 && month3 == -1) {
                        month3 = month22;
                    }
                    if (day22 != -1 && day3 == -1) {
                        day3 = day22;
                    }
                }
                int year4 = year3;
                String tzone2 = "";
                if (sec5 != 0) {
                    hour = hour4;
                    if (sec5 != 2) {
                        if (min5 != 0) {
                        }
                    }
                    String tzone3 = tzone2;
                    if (!tm5.trim().equals("") && !tm22.trim().equals("")) {
                        boolean isBefore3 = isBefore2;
                        if (!(year4 == year22 && month3 == month22 && day3 == day22)) {
                            isBefore3 = true;
                        }
                        if (!isBefore3) {
                            tm5 = tm22;
                        }
                    } else if (!tm22.trim().equals("") && !tm5.trim().equals("")) {
                        boolean z = isBefore1;
                        if (year4 == year22 && month3 == month22 && day3 == day22) {
                            isBefore = true;
                        } else {
                            isBefore = false;
                        }
                        if (isBefore) {
                            tm22 = tm5;
                        }
                    }
                    if (tz22.equals("")) {
                        tzone2 = tz22;
                    } else if (!tz22.equals("") || tz3.equals("")) {
                        tm2 = tm22;
                        tzone = tzone3;
                        boolean z2 = isBefore1;
                        c = Calendar.getInstance();
                        StringBuffer t6 = new StringBuffer();
                        StringBuffer s4 = new StringBuffer();
                        boolean z3 = isBefore2;
                        String str2 = tz22;
                        int hour5 = hour;
                        if (hour5 > 12 && !tm5.trim().equals("")) {
                            tm5 = "";
                        }
                        StringBuffer s5 = s4;
                        s5.append(hour5 == -1 ? Integer.valueOf(hour5) : "08");
                        s5.append(":");
                        int i3 = hour5;
                        int i4 = min5;
                        int hour6 = min4;
                        s5.append(hour6 == -1 ? Integer.valueOf(hour6) : "00");
                        s5.append(":");
                        int i5 = hour6;
                        int sec6 = sec4;
                        s5.append(sec6 == -1 ? Integer.valueOf(sec6) : "00");
                        if (tm5.equals("")) {
                            t = t6;
                            t.append("hh");
                            t.append(":mm:ss");
                            t.append(" a");
                            s5.append(" ");
                            s5.append(tm5);
                        } else {
                            t = t6;
                            t.append("HH");
                            t.append(":mm:ss");
                        }
                        if (!tzone.equals("")) {
                            t.append(" Z");
                            s5.append(" ");
                            s5.append(tzone);
                        }
                        int i6 = sec6;
                        StringBuffer stringBuffer2 = t;
                        SimpleDateFormat for12 = new SimpleDateFormat(t.toString(), Locale.ENGLISH);
                        c.setTime(for12.parse(s5.toString()));
                        SimpleDateFormat simpleDateFormat2 = for12;
                        StringBuffer stringBuffer3 = s5;
                        Calendar tc = Calendar.getInstance();
                        tc.setTime(new Date(rTime));
                        if (year4 == -1) {
                            c.set(1, year4);
                        } else {
                            c.set(1, tc.get(1));
                        }
                        if (month3 == -1) {
                            c.set(2, month3);
                        } else {
                            c.set(2, tc.get(2));
                        }
                        if (day3 == -1) {
                            c.set(5, day3);
                        } else {
                            c.set(5, tc.get(5));
                        }
                        c2 = Calendar.getInstance();
                        StringBuffer t22 = new StringBuffer();
                        StringBuffer s22 = new StringBuffer();
                        int i7 = year4;
                        int i8 = month3;
                        int hour24 = hour23;
                        if (hour24 > 12 && !tm2.trim().equals("")) {
                            tm2 = "";
                        }
                        StringBuffer s23 = s22;
                        s23.append(hour24 == -1 ? Integer.valueOf(hour24) : "08");
                        s23.append(":");
                        int i9 = hour24;
                        int i10 = day3;
                        int min23 = min22;
                        s23.append(min23 == -1 ? Integer.valueOf(min23) : "00");
                        s23.append(":");
                        int i11 = min23;
                        int sec23 = sec22;
                        s23.append(sec23 == -1 ? Integer.valueOf(sec23) : "00");
                        if (tm2.equals("")) {
                            t2 = t22;
                            t2.append("hh");
                            t2.append(":mm:ss");
                            t2.append(" a");
                            s23.append(" ");
                            s23.append(tm2);
                        } else {
                            t2 = t22;
                            t2.append("HH");
                            t2.append(":mm:ss");
                        }
                        if (!tzone.equals("")) {
                            t2.append(" Z");
                            s23.append(" ");
                            s23.append(tzone);
                        }
                        String str3 = tzone;
                        StringBuffer stringBuffer4 = t2;
                        SimpleDateFormat for2 = new SimpleDateFormat(t2.toString(), Locale.ENGLISH);
                        c2.setTime(for2.parse(s23.toString()));
                        if (year22 == -1) {
                            c2.set(1, year22);
                        } else {
                            c2.set(1, tc.get(1));
                        }
                        if (month22 == -1) {
                            c2.set(2, month22);
                        } else {
                            c2.set(2, tc.get(2));
                        }
                        if (day22 == -1) {
                            c2.set(5, day22);
                        } else {
                            c2.set(5, tc.get(5));
                        }
                        if (c.compareTo(c2) != 1) {
                            Calendar calendar = tc;
                            if (status2 == 5) {
                                SimpleDateFormat simpleDateFormat3 = for2;
                                c.add(5, -1);
                            } else {
                                SimpleDateFormat simpleDateFormat4 = for2;
                            }
                        } else {
                            SimpleDateFormat simpleDateFormat5 = for2;
                            int i12 = status2;
                        }
                        result = result3;
                        result.add(c.getTime());
                        result.add(c2.getTime());
                    } else {
                        tzone2 = tz3;
                    }
                } else {
                    hour = hour4;
                    String tzone32 = tzone2;
                    if (!tm5.trim().equals("")) {
                    }
                    if (!tm22.trim().equals("")) {
                    }
                    if (tz22.equals("")) {
                    }
                }
                tm2 = tm22;
                tzone = tzone2;
                boolean z22 = isBefore1;
                c = Calendar.getInstance();
                StringBuffer t62 = new StringBuffer();
                StringBuffer s42 = new StringBuffer();
                boolean z32 = isBefore2;
                String str22 = tz22;
                int hour52 = hour;
                tm5 = "";
                StringBuffer s52 = s42;
                s52.append(hour52 == -1 ? Integer.valueOf(hour52) : "08");
                s52.append(":");
                int i32 = hour52;
                int i42 = min5;
                int hour62 = min4;
                s52.append(hour62 == -1 ? Integer.valueOf(hour62) : "00");
                s52.append(":");
                int i52 = hour62;
                int sec62 = sec4;
                s52.append(sec62 == -1 ? Integer.valueOf(sec62) : "00");
                if (tm5.equals("")) {
                }
                if (!tzone.equals("")) {
                }
                int i62 = sec62;
                StringBuffer stringBuffer22 = t;
                SimpleDateFormat for122 = new SimpleDateFormat(t.toString(), Locale.ENGLISH);
                try {
                    c.setTime(for122.parse(s52.toString()));
                } catch (ParseException e3) {
                    e3.printStackTrace();
                }
                SimpleDateFormat simpleDateFormat22 = for122;
                StringBuffer stringBuffer32 = s52;
                Calendar tc2 = Calendar.getInstance();
                tc2.setTime(new Date(rTime));
                if (year4 == -1) {
                }
                if (month3 == -1) {
                }
                if (day3 == -1) {
                }
                c2 = Calendar.getInstance();
                StringBuffer t222 = new StringBuffer();
                StringBuffer s222 = new StringBuffer();
                int i72 = year4;
                int i82 = month3;
                int hour242 = hour23;
                tm2 = "";
                StringBuffer s232 = s222;
                s232.append(hour242 == -1 ? Integer.valueOf(hour242) : "08");
                s232.append(":");
                int i92 = hour242;
                int i102 = day3;
                int min232 = min22;
                s232.append(min232 == -1 ? Integer.valueOf(min232) : "00");
                s232.append(":");
                int i112 = min232;
                int sec232 = sec22;
                s232.append(sec232 == -1 ? Integer.valueOf(sec232) : "00");
                if (tm2.equals("")) {
                }
                if (!tzone.equals("")) {
                }
                String str32 = tzone;
                StringBuffer stringBuffer42 = t2;
                SimpleDateFormat for22 = new SimpleDateFormat(t2.toString(), Locale.ENGLISH);
                try {
                    c2.setTime(for22.parse(s232.toString()));
                } catch (ParseException e4) {
                    e4.printStackTrace();
                }
                if (year22 == -1) {
                }
                if (month22 == -1) {
                }
                if (day22 == -1) {
                }
                if (c.compareTo(c2) != 1) {
                }
                result = result3;
                result.add(c.getTime());
                result.add(c2.getTime());
            } else {
                result = result2;
            }
            return result;
        }
        result = result2;
        return result;
    }
}

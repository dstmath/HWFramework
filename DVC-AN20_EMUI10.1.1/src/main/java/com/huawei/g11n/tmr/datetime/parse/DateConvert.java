package com.huawei.g11n.tmr.datetime.parse;

import com.huawei.g11n.tmr.Filter;
import com.huawei.g11n.tmr.Match;
import com.huawei.g11n.tmr.datetime.utils.DatePeriod;
import com.huawei.g11n.tmr.datetime.utils.LocaleParam;
import com.huawei.uikit.effect.BuildConfig;
import huawei.android.provider.HanziToPinyin;
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
        if (ms == null || ms.isEmpty()) {
            return null;
        }
        if (ms.size() == 1) {
            return ms.get(0).getDp();
        }
        return convertMutilMatch(content, ms, ps, dts).getDp();
    }

    private int nestDealDate(String content, Match curren, List<Match> list, int pptype) {
        boolean z = false;
        Match next = list.get(0);
        int type = Filter.getType(next.getRegex());
        if (type != 1 && type != 5 && type != 6) {
            return 0;
        }
        if (type != Filter.getType(curren.getRegex())) {
            if (type != pptype) {
                boolean isThree = false;
                String ss = content.substring(curren.getEnd(), next.getBegin());
                if (!LocaleParam.isRelDates(ss, this.locale) && !ss.trim().equals("(")) {
                    return 0;
                }
                if (list.size() > 1 && nestDealDate(content, next, list.subList(1, list.size()), curren.getType()) == 1) {
                    isThree = true;
                }
                boolean flag15 = false;
                if (ss.trim().equals("(")) {
                    Matcher bcm = Pattern.compile("\\s*\\((.*?)\\s*\\)").matcher(content.substring(curren.getEnd()));
                    String d = null;
                    if (bcm.lookingAt()) {
                        d = bcm.group(1);
                    }
                    int end = isThree ? list.get(1).getEnd() : next.getEnd();
                    if (d != null) {
                        z = d.trim().equals(content.substring(next.getBegin(), end).trim());
                    }
                    flag15 = z;
                }
                if (!LocaleParam.isRelDates(ss, this.locale) && !flag15) {
                    return 0;
                }
                if (isThree) {
                    return 2;
                }
                return 1;
            }
        }
        return 0;
    }

    private List<Match> filterDate(String content, List<Match> ms) {
        int end;
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
                if (hasNum != 0) {
                    if (sub != null) {
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
                            if (be != -1 && be < e.getBegin() && (end = str.indexOf(41, e.getEnd())) != -1 && str.substring(e.getEnd(), end + 1).trim().equals(")")) {
                                add = (end - e.getEnd()) + 1;
                            }
                            m.setEnd(e.getEnd() + add);
                            if (change != null) {
                                m.getDp().setBegin(change.getDp().getBegin());
                            }
                            result.add(m);
                        }
                    }
                }
                result.add(m);
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
        boolean flag;
        Pattern dt;
        int i;
        boolean flag15;
        int i2 = 2;
        Pattern dt2 = Pattern.compile("\\s*(at|às|،‏|،|u|kl\\.|को|的|o|à|a\\s+les|ve|la|pada|kl|στις|alle|jam|ຂອງວັນທີ" + DTBridge + ")\\s*", 2);
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
                    String ss = content.substring(curren.getEnd(), m.getBegin());
                    if (ss.trim().equals(BuildConfig.FLAVOR)) {
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
                            Matcher bcm = Pattern.compile("\\s*\\((.*?)\\s*\\)").matcher(content.substring(curren.getEnd()));
                            String d = null;
                            if (bcm.lookingAt()) {
                                d = bcm.group(1);
                                add = bcm.group().length();
                            }
                            if (d != null) {
                                dt = dt2;
                                flag15 = d.trim().equals(content.substring(m.getBegin(), m.getEnd()).trim());
                            } else {
                                dt = dt2;
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
                        } else {
                            i = 2;
                        }
                        if (change) {
                            curren = m;
                            i2 = i;
                            dt2 = dt;
                        }
                    }
                    i2 = i;
                    dt2 = dt;
                } else {
                    curren = m;
                    i2 = i2;
                    dt2 = dt2;
                }
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
                            if (type == 3) {
                                curren.getDp().getBegin().setTime(m.getDp().getBegin().getTime());
                                curren.getDp().setEnd(m.getDp().getEnd());
                            } else {
                                curren.getDp().getBegin().setDay(m.getDp().getBegin().getDate());
                            }
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

    /* JADX INFO: Multiple debug info for r1v8 'tz2'  java.lang.String: [D('tz2' java.lang.String), D('tz' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r2v16 'month2'  int: [D('month2' int), D('day2' int)] */
    /* JADX INFO: Multiple debug info for r0v82 'year'  int: [D('year' int), D('month2' int)] */
    /* JADX WARNING: Code restructure failed: missing block: B:192:0x0556, code lost:
        if (r1 == 1) goto L_0x055c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:238:0x05d9, code lost:
        if (r3 == 1) goto L_0x05de;
     */
    /* JADX WARNING: Removed duplicated region for block: B:216:0x059e  */
    /* JADX WARNING: Removed duplicated region for block: B:219:0x05a9  */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x05ae  */
    /* JADX WARNING: Removed duplicated region for block: B:234:0x05cd  */
    /* JADX WARNING: Removed duplicated region for block: B:237:0x05d6  */
    /* JADX WARNING: Removed duplicated region for block: B:239:0x05dc  */
    /* JADX WARNING: Removed duplicated region for block: B:242:0x05e1  */
    /* JADX WARNING: Removed duplicated region for block: B:255:0x05fc  */
    /* JADX WARNING: Removed duplicated region for block: B:260:0x060d  */
    /* JADX WARNING: Removed duplicated region for block: B:263:0x061c  */
    /* JADX WARNING: Removed duplicated region for block: B:279:0x0652 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:284:0x065e  */
    /* JADX WARNING: Removed duplicated region for block: B:287:0x066c  */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0673  */
    /* JADX WARNING: Removed duplicated region for block: B:297:0x06a6  */
    /* JADX WARNING: Removed duplicated region for block: B:303:0x06bc  */
    /* JADX WARNING: Removed duplicated region for block: B:304:0x06c1  */
    /* JADX WARNING: Removed duplicated region for block: B:307:0x06d0  */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x06d5  */
    /* JADX WARNING: Removed duplicated region for block: B:311:0x06df  */
    /* JADX WARNING: Removed duplicated region for block: B:312:0x06e4  */
    /* JADX WARNING: Removed duplicated region for block: B:315:0x06ef  */
    /* JADX WARNING: Removed duplicated region for block: B:316:0x0711  */
    /* JADX WARNING: Removed duplicated region for block: B:319:0x072f  */
    /* JADX WARNING: Removed duplicated region for block: B:320:0x073d  */
    /* JADX WARNING: Removed duplicated region for block: B:328:0x0778  */
    /* JADX WARNING: Removed duplicated region for block: B:329:0x077d  */
    /* JADX WARNING: Removed duplicated region for block: B:332:0x0788  */
    /* JADX WARNING: Removed duplicated region for block: B:333:0x078d  */
    /* JADX WARNING: Removed duplicated region for block: B:336:0x0798  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x079d  */
    /* JADX WARNING: Removed duplicated region for block: B:340:0x07c1  */
    /* JADX WARNING: Removed duplicated region for block: B:346:0x07d7  */
    /* JADX WARNING: Removed duplicated region for block: B:347:0x07dc  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x07ef  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x07f4  */
    /* JADX WARNING: Removed duplicated region for block: B:354:0x0800  */
    /* JADX WARNING: Removed duplicated region for block: B:355:0x0805  */
    /* JADX WARNING: Removed duplicated region for block: B:358:0x0810  */
    /* JADX WARNING: Removed duplicated region for block: B:359:0x0822  */
    /* JADX WARNING: Removed duplicated region for block: B:362:0x0830  */
    /* JADX WARNING: Removed duplicated region for block: B:370:0x085c  */
    /* JADX WARNING: Removed duplicated region for block: B:371:0x0861  */
    /* JADX WARNING: Removed duplicated region for block: B:374:0x086d  */
    /* JADX WARNING: Removed duplicated region for block: B:375:0x0872  */
    /* JADX WARNING: Removed duplicated region for block: B:378:0x087e  */
    /* JADX WARNING: Removed duplicated region for block: B:379:0x0883  */
    /* JADX WARNING: Removed duplicated region for block: B:382:0x0892  */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x08a0  */
    public List<Date> convert(DatePeriod dp, long rTime) {
        String str;
        String str2;
        int year2;
        int month2;
        String str3;
        int day2;
        int day22;
        String str4;
        int day;
        int day3;
        int month;
        String str5;
        String tzone;
        String tz2;
        Calendar c;
        String tm;
        Integer num;
        Integer num2;
        int month22;
        String str6;
        String str7;
        StringBuffer t;
        String tm2;
        String str8;
        int year22;
        String str9;
        Calendar c2;
        String tm22;
        Integer num3;
        Integer num4;
        StringBuffer t2;
        int i;
        boolean isBefore;
        boolean isBefore2;
        int month23;
        int month24;
        int i2;
        int year;
        int month25;
        Integer num5;
        Integer num6;
        Integer num7;
        Integer num8;
        Integer num9;
        Integer num10;
        StringBuffer t3;
        List<Date> result = new ArrayList<>();
        if (dp == null) {
            return null;
        }
        int status = dp.getType();
        if (status == 0) {
            int year3 = dp.getBegin().getDate().getYear();
            int month3 = dp.getBegin().getDate().getMonth();
            int day4 = dp.getBegin().getDate().getDay();
            int hour = dp.getBegin().getTime().getClock();
            int min = dp.getBegin().getTime().getMinute();
            int sec = dp.getBegin().getTime().getSecond();
            String tm3 = dp.getBegin().getTime() != null ? dp.getBegin().getTime().getMark() : BuildConfig.FLAVOR;
            String tz = dp.getBegin().getTime() != null ? dp.getBegin().getTime().getTimezone() : BuildConfig.FLAVOR;
            StringBuffer t4 = new StringBuffer();
            StringBuffer s = new StringBuffer();
            String tm4 = (hour <= 12 || tm3.trim().equals(BuildConfig.FLAVOR)) ? tm3 : BuildConfig.FLAVOR;
            if (hour != -1) {
                num8 = Integer.valueOf(hour);
            } else {
                num8 = "00";
            }
            s.append(num8);
            s.append(":");
            if (min != -1) {
                num9 = Integer.valueOf(min);
            } else {
                num9 = "00";
            }
            s.append(num9);
            s.append(":");
            if (sec != -1) {
                num10 = Integer.valueOf(sec);
            } else {
                num10 = "00";
            }
            s.append(num10);
            if (!tm4.equals(BuildConfig.FLAVOR)) {
                t3 = t4;
                t3.append("hh");
                t3.append(":mm:ss");
                t3.append(" a");
                s.append(HanziToPinyin.Token.SEPARATOR);
                s.append(tm4);
            } else {
                t3 = t4;
                t3.append("HH");
                t3.append(":mm:ss");
            }
            if (!tz.equals(BuildConfig.FLAVOR)) {
                t3.append(" Z");
                s.append(HanziToPinyin.Token.SEPARATOR);
                s.append(tz);
            }
            Date d1 = null;
            try {
                d1 = new SimpleDateFormat(t3.toString(), Locale.ENGLISH).parse(s.toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar c3 = Calendar.getInstance();
            Calendar c22 = Calendar.getInstance();
            c22.setTime(new Date(rTime));
            if (d1 != null) {
                c3.setTime(d1);
            }
            if (year3 != -1) {
                c3.set(1, year3);
            } else {
                c3.set(1, c22.get(1));
            }
            if (month3 != -1) {
                c3.set(2, month3);
            } else {
                c3.set(2, c22.get(2));
            }
            if (day4 != -1) {
                c3.set(5, day4);
            } else {
                c3.set(5, c22.get(5));
            }
            result.add(c3.getTime());
            return result;
        } else if (status == 1) {
            int year4 = dp.getBegin().getDate().getYear();
            int month4 = dp.getBegin().getDate().getMonth();
            int day5 = dp.getBegin().getDate().getDay();
            Calendar c4 = Calendar.getInstance();
            Calendar c23 = Calendar.getInstance();
            c23.setTime(new Date(rTime));
            if (year4 != -1) {
                c4.set(1, year4);
            } else {
                c4.set(1, c23.get(1));
            }
            if (month4 != -1) {
                c4.set(2, month4);
            } else {
                c4.set(2, c23.get(2));
            }
            if (day5 != -1) {
                c4.set(5, day5);
            } else {
                c4.set(5, c23.get(5));
            }
            c4.set(11, 0);
            c4.set(12, 0);
            c4.set(13, 0);
            result.add(c4.getTime());
            return result;
        } else if (status == 2) {
            int hour2 = dp.getBegin().getTime().getClock();
            int min2 = dp.getBegin().getTime().getMinute();
            int sec2 = dp.getBegin().getTime().getSecond();
            String tm5 = dp.getBegin().getTime() != null ? dp.getBegin().getTime().getMark() : BuildConfig.FLAVOR;
            String tz3 = dp.getBegin().getTime() != null ? dp.getBegin().getTime().getTimezone() : BuildConfig.FLAVOR;
            StringBuffer t5 = new StringBuffer();
            StringBuffer s2 = new StringBuffer();
            t5.append("yyyy-MM-dd ");
            Calendar c5 = Calendar.getInstance();
            c5.setTime(new Date(rTime));
            s2.append(c5.get(1));
            s2.append("-");
            s2.append(c5.get(2) + 1);
            s2.append("-");
            s2.append(c5.get(5));
            s2.append(HanziToPinyin.Token.SEPARATOR);
            String tm6 = (hour2 <= 12 || tm5.trim().equals(BuildConfig.FLAVOR)) ? tm5 : BuildConfig.FLAVOR;
            if (hour2 != -1) {
                num5 = Integer.valueOf(hour2);
            } else {
                num5 = "00";
            }
            s2.append(num5);
            s2.append(":");
            if (min2 != -1) {
                num6 = Integer.valueOf(min2);
            } else {
                num6 = "00";
            }
            s2.append(num6);
            s2.append(":");
            if (sec2 != -1) {
                num7 = Integer.valueOf(sec2);
            } else {
                num7 = "00";
            }
            s2.append(num7);
            if (!tm6.equals(BuildConfig.FLAVOR)) {
                t5.append("hh");
                t5.append(":mm:ss");
                t5.append(" a");
                s2.append(HanziToPinyin.Token.SEPARATOR);
                s2.append(tm6);
            } else {
                t5.append("HH");
                t5.append(":mm:ss");
            }
            if (!tz3.equals(BuildConfig.FLAVOR)) {
                t5.append(" Z");
                s2.append(HanziToPinyin.Token.SEPARATOR);
                s2.append(tz3);
            }
            Date d12 = null;
            try {
                d12 = new SimpleDateFormat(t5.toString(), Locale.ENGLISH).parse(s2.toString());
            } catch (ParseException e2) {
                e2.printStackTrace();
            }
            result.add(d12);
            return result;
        } else if (status <= 2) {
            return result;
        } else {
            int year5 = dp.getBegin().getDate() != null ? dp.getBegin().getDate().getYear() : -1;
            int month5 = dp.getBegin().getDate() != null ? dp.getBegin().getDate().getMonth() : -1;
            int day6 = dp.getBegin().getDate() != null ? dp.getBegin().getDate().getDay() : -1;
            int hour3 = dp.getBegin().getTime() != null ? dp.getBegin().getTime().getClock() : -1;
            int min3 = dp.getBegin().getTime() != null ? dp.getBegin().getTime().getMinute() : -1;
            int sec3 = dp.getBegin().getTime() != null ? dp.getBegin().getTime().getSecond() : -1;
            String tm7 = dp.getBegin().getTime() != null ? dp.getBegin().getTime().getMark() : BuildConfig.FLAVOR;
            String tz4 = dp.getBegin().getTime() != null ? dp.getBegin().getTime().getTimezone() : BuildConfig.FLAVOR;
            int year23 = dp.getEnd().getDate() != null ? dp.getEnd().getDate().getYear() : -1;
            int month26 = dp.getEnd().getDate() != null ? dp.getEnd().getDate().getMonth() : -1;
            int day23 = dp.getEnd().getDate() != null ? dp.getEnd().getDate().getDay() : -1;
            int hour22 = dp.getEnd().getTime() != null ? dp.getEnd().getTime().getClock() : -1;
            int min22 = dp.getEnd().getTime() != null ? dp.getEnd().getTime().getMinute() : -1;
            int sec22 = dp.getEnd().getTime() != null ? dp.getEnd().getTime().getSecond() : -1;
            String tm23 = dp.getEnd().getTime() != null ? dp.getEnd().getTime().getMark() : BuildConfig.FLAVOR;
            String tz22 = dp.getEnd().getTime() != null ? dp.getEnd().getTime().getTimezone() : BuildConfig.FLAVOR;
            boolean isBefore1 = dp.getBegin().getTime() != null ? dp.getBegin().getTime().isMarkBefore() : true;
            boolean isBefore22 = dp.getEnd().getTime() != null ? dp.getEnd().getTime().isMarkBefore() : true;
            int dtst = dp.getBegin().getSatuts();
            int dt2st = dp.getEnd().getSatuts();
            if (dtst != 0) {
                str2 = " Z";
                i2 = 1;
            } else {
                str2 = " Z";
                i2 = 1;
            }
            if (dt2st == 0 || dt2st == i2) {
                if (year5 != -1) {
                    str = "HH";
                    year2 = year23;
                    if (year2 == -1) {
                        year2 = year5;
                    }
                } else {
                    str = "HH";
                    year2 = year23;
                }
                if (year5 == -1 && year2 != -1) {
                    year5 = year2;
                }
                if (month5 != -1) {
                    year = year5;
                    month25 = month26;
                    if (month25 == -1) {
                        month25 = month5;
                    }
                } else {
                    year = year5;
                    month25 = month26;
                }
                if (month5 != -1 || month25 == -1) {
                    month2 = month25;
                    year5 = year;
                } else {
                    month5 = month25;
                    month2 = month25;
                    year5 = year;
                }
                if (dtst == 0) {
                    str3 = HanziToPinyin.Token.SEPARATOR;
                    if (dtst != 1) {
                        month23 = month2;
                        month24 = day23;
                        day2 = month24;
                        day22 = month23;
                        if (dt2st == 0) {
                            str4 = " a";
                        } else {
                            str4 = " a";
                        }
                        if (dtst == 2) {
                            if (year2 != -1 && year5 == -1) {
                                year5 = year2;
                            }
                            if (day22 != -1 && month5 == -1) {
                                month5 = day22;
                            }
                            if (day2 != -1 && day6 == -1) {
                                day = day2;
                                day3 = month5;
                                month = year5;
                                if (dtst != 0) {
                                    str5 = ":mm:ss";
                                    if (!(dtst == 2 || dt2st == 0 || dt2st == 2)) {
                                        tzone = BuildConfig.FLAVOR;
                                        tz2 = tz4;
                                        c = Calendar.getInstance();
                                        StringBuffer t6 = new StringBuffer();
                                        StringBuffer s3 = new StringBuffer();
                                        tm = (hour3 > 12 || tm7.trim().equals(BuildConfig.FLAVOR)) ? tm7 : BuildConfig.FLAVOR;
                                        s3.append(hour3 == -1 ? Integer.valueOf(hour3) : "08");
                                        s3.append(":");
                                        if (min3 == -1) {
                                            num = Integer.valueOf(min3);
                                        } else {
                                            num = "00";
                                        }
                                        s3.append(num);
                                        s3.append(":");
                                        if (sec3 == -1) {
                                            num2 = Integer.valueOf(sec3);
                                        } else {
                                            num2 = "00";
                                        }
                                        s3.append(num2);
                                        if (tm.equals(BuildConfig.FLAVOR)) {
                                            t = t6;
                                            t.append("hh");
                                            str7 = str5;
                                            t.append(str7);
                                            str6 = str4;
                                            t.append(str6);
                                            month22 = day22;
                                            str8 = str3;
                                            s3.append(str8);
                                            s3.append(tm);
                                            tm2 = str;
                                        } else {
                                            t = t6;
                                            str7 = str5;
                                            str6 = str4;
                                            month22 = day22;
                                            str8 = str3;
                                            tm2 = str;
                                            t.append(tm2);
                                            t.append(str7);
                                        }
                                        if (tzone.equals(BuildConfig.FLAVOR)) {
                                            year22 = year2;
                                            str9 = str2;
                                            t.append(str9);
                                            s3.append(str8);
                                            s3.append(tzone);
                                        } else {
                                            year22 = year2;
                                            str9 = str2;
                                        }
                                        c.setTime(new SimpleDateFormat(t.toString(), Locale.ENGLISH).parse(s3.toString()));
                                        Date d = new Date(rTime);
                                        Calendar tc = Calendar.getInstance();
                                        tc.setTime(d);
                                        if (month == -1) {
                                            c.set(1, month);
                                        } else {
                                            c.set(1, tc.get(1));
                                        }
                                        if (day3 == -1) {
                                            c.set(2, day3);
                                        } else {
                                            c.set(2, tc.get(2));
                                        }
                                        if (day == -1) {
                                            c.set(5, day);
                                        } else {
                                            c.set(5, tc.get(5));
                                        }
                                        c2 = Calendar.getInstance();
                                        StringBuffer t22 = new StringBuffer();
                                        StringBuffer s22 = new StringBuffer();
                                        tm22 = (hour22 > 12 || tm23.trim().equals(BuildConfig.FLAVOR)) ? tm23 : BuildConfig.FLAVOR;
                                        s22.append(hour22 == -1 ? Integer.valueOf(hour22) : "08");
                                        s22.append(":");
                                        if (min22 == -1) {
                                            num3 = Integer.valueOf(min22);
                                        } else {
                                            num3 = "00";
                                        }
                                        s22.append(num3);
                                        s22.append(":");
                                        if (sec22 == -1) {
                                            num4 = Integer.valueOf(sec22);
                                        } else {
                                            num4 = "00";
                                        }
                                        s22.append(num4);
                                        if (tm22.equals(BuildConfig.FLAVOR)) {
                                            t2 = t22;
                                            t2.append("hh");
                                            t2.append(str7);
                                            t2.append(str6);
                                            s22.append(str8);
                                            s22.append(tm22);
                                        } else {
                                            t2 = t22;
                                            t2.append(tm2);
                                            t2.append(str7);
                                        }
                                        if (!tzone.equals(BuildConfig.FLAVOR)) {
                                            t2.append(str9);
                                            s22.append(str8);
                                            s22.append(tzone);
                                        }
                                        c2.setTime(new SimpleDateFormat(t2.toString(), Locale.ENGLISH).parse(s22.toString()));
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
                                        if (day2 == -1) {
                                            i = 5;
                                            c2.set(5, day2);
                                        } else {
                                            i = 5;
                                            c2.set(5, tc.get(5));
                                        }
                                        if (c.compareTo(c2) != 1) {
                                            if (status == i) {
                                                c.add(i, -1);
                                            }
                                        }
                                        result.add(c.getTime());
                                        result.add(c2.getTime());
                                        return result;
                                    }
                                } else {
                                    str5 = ":mm:ss";
                                }
                                if (!tm7.trim().equals(BuildConfig.FLAVOR) && !tm23.trim().equals(BuildConfig.FLAVOR)) {
                                    if (month == year2 && day3 == day22 && day == day2) {
                                        isBefore2 = isBefore22;
                                    } else {
                                        isBefore2 = true;
                                    }
                                    if (!isBefore2) {
                                        tm7 = tm23;
                                    }
                                } else if (tm23.trim().equals(BuildConfig.FLAVOR) && !tm7.trim().equals(BuildConfig.FLAVOR)) {
                                    if (month != year2 && day3 == day22 && day == day2) {
                                        isBefore = true;
                                    } else {
                                        isBefore = false;
                                    }
                                    if (isBefore) {
                                        tm23 = tm7;
                                    }
                                }
                                if (!tz22.equals(BuildConfig.FLAVOR)) {
                                    tzone = tz22;
                                    tz2 = tz4;
                                } else {
                                    if (tz22.equals(BuildConfig.FLAVOR)) {
                                        tz2 = tz4;
                                        if (!tz2.equals(BuildConfig.FLAVOR)) {
                                            tzone = tz2;
                                        }
                                    } else {
                                        tz2 = tz4;
                                    }
                                    tzone = BuildConfig.FLAVOR;
                                }
                                c = Calendar.getInstance();
                                StringBuffer t62 = new StringBuffer();
                                StringBuffer s32 = new StringBuffer();
                                if (hour3 > 12) {
                                }
                                s32.append(hour3 == -1 ? Integer.valueOf(hour3) : "08");
                                s32.append(":");
                                if (min3 == -1) {
                                }
                                s32.append(num);
                                s32.append(":");
                                if (sec3 == -1) {
                                }
                                s32.append(num2);
                                if (tm.equals(BuildConfig.FLAVOR)) {
                                }
                                if (tzone.equals(BuildConfig.FLAVOR)) {
                                }
                                c.setTime(new SimpleDateFormat(t.toString(), Locale.ENGLISH).parse(s32.toString()));
                                Date d2 = new Date(rTime);
                                Calendar tc2 = Calendar.getInstance();
                                tc2.setTime(d2);
                                if (month == -1) {
                                }
                                if (day3 == -1) {
                                }
                                if (day == -1) {
                                }
                                c2 = Calendar.getInstance();
                                StringBuffer t222 = new StringBuffer();
                                StringBuffer s222 = new StringBuffer();
                                if (hour22 > 12) {
                                }
                                s222.append(hour22 == -1 ? Integer.valueOf(hour22) : "08");
                                s222.append(":");
                                if (min22 == -1) {
                                }
                                s222.append(num3);
                                s222.append(":");
                                if (sec22 == -1) {
                                }
                                s222.append(num4);
                                if (tm22.equals(BuildConfig.FLAVOR)) {
                                }
                                if (!tzone.equals(BuildConfig.FLAVOR)) {
                                }
                                c2.setTime(new SimpleDateFormat(t2.toString(), Locale.ENGLISH).parse(s222.toString()));
                                if (year22 == -1) {
                                }
                                if (month22 == -1) {
                                }
                                if (day2 == -1) {
                                }
                                if (c.compareTo(c2) != 1) {
                                }
                                result.add(c.getTime());
                                result.add(c2.getTime());
                                return result;
                            }
                        }
                        day = day6;
                        day3 = month5;
                        month = year5;
                        if (dtst != 0) {
                        }
                        if (!tm7.trim().equals(BuildConfig.FLAVOR)) {
                        }
                        if (month != year2) {
                        }
                        isBefore = false;
                        if (isBefore) {
                        }
                        if (!tz22.equals(BuildConfig.FLAVOR)) {
                        }
                        c = Calendar.getInstance();
                        StringBuffer t622 = new StringBuffer();
                        StringBuffer s322 = new StringBuffer();
                        if (hour3 > 12) {
                        }
                        s322.append(hour3 == -1 ? Integer.valueOf(hour3) : "08");
                        s322.append(":");
                        if (min3 == -1) {
                        }
                        s322.append(num);
                        s322.append(":");
                        if (sec3 == -1) {
                        }
                        s322.append(num2);
                        if (tm.equals(BuildConfig.FLAVOR)) {
                        }
                        if (tzone.equals(BuildConfig.FLAVOR)) {
                        }
                        c.setTime(new SimpleDateFormat(t.toString(), Locale.ENGLISH).parse(s322.toString()));
                        Date d22 = new Date(rTime);
                        Calendar tc22 = Calendar.getInstance();
                        tc22.setTime(d22);
                        if (month == -1) {
                        }
                        if (day3 == -1) {
                        }
                        if (day == -1) {
                        }
                        c2 = Calendar.getInstance();
                        StringBuffer t2222 = new StringBuffer();
                        StringBuffer s2222 = new StringBuffer();
                        if (hour22 > 12) {
                        }
                        s2222.append(hour22 == -1 ? Integer.valueOf(hour22) : "08");
                        s2222.append(":");
                        if (min22 == -1) {
                        }
                        s2222.append(num3);
                        s2222.append(":");
                        if (sec22 == -1) {
                        }
                        s2222.append(num4);
                        if (tm22.equals(BuildConfig.FLAVOR)) {
                        }
                        if (!tzone.equals(BuildConfig.FLAVOR)) {
                        }
                        c2.setTime(new SimpleDateFormat(t2.toString(), Locale.ENGLISH).parse(s2222.toString()));
                        if (year22 == -1) {
                        }
                        if (month22 == -1) {
                        }
                        if (day2 == -1) {
                        }
                        if (c.compareTo(c2) != 1) {
                        }
                        result.add(c.getTime());
                        result.add(c2.getTime());
                        return result;
                    }
                } else {
                    str3 = HanziToPinyin.Token.SEPARATOR;
                }
                if (dt2st != 2) {
                    if (year5 != -1 && year2 == -1) {
                        year2 = year5;
                    }
                    if (month5 != -1 && month2 == -1) {
                        month2 = month5;
                    }
                    if (day6 != -1) {
                        month23 = month2;
                        month24 = day23;
                        if (month24 == -1) {
                            day22 = month23;
                            day2 = day6;
                            if (dt2st == 0) {
                            }
                            if (dtst == 2) {
                            }
                            day = day6;
                            day3 = month5;
                            month = year5;
                            if (dtst != 0) {
                            }
                            if (!tm7.trim().equals(BuildConfig.FLAVOR)) {
                            }
                            if (month != year2) {
                            }
                            isBefore = false;
                            if (isBefore) {
                            }
                            if (!tz22.equals(BuildConfig.FLAVOR)) {
                            }
                            c = Calendar.getInstance();
                            StringBuffer t6222 = new StringBuffer();
                            StringBuffer s3222 = new StringBuffer();
                            if (hour3 > 12) {
                            }
                            s3222.append(hour3 == -1 ? Integer.valueOf(hour3) : "08");
                            s3222.append(":");
                            if (min3 == -1) {
                            }
                            s3222.append(num);
                            s3222.append(":");
                            if (sec3 == -1) {
                            }
                            s3222.append(num2);
                            if (tm.equals(BuildConfig.FLAVOR)) {
                            }
                            if (tzone.equals(BuildConfig.FLAVOR)) {
                            }
                            c.setTime(new SimpleDateFormat(t.toString(), Locale.ENGLISH).parse(s3222.toString()));
                            Date d222 = new Date(rTime);
                            Calendar tc222 = Calendar.getInstance();
                            tc222.setTime(d222);
                            if (month == -1) {
                            }
                            if (day3 == -1) {
                            }
                            if (day == -1) {
                            }
                            c2 = Calendar.getInstance();
                            StringBuffer t22222 = new StringBuffer();
                            StringBuffer s22222 = new StringBuffer();
                            if (hour22 > 12) {
                            }
                            s22222.append(hour22 == -1 ? Integer.valueOf(hour22) : "08");
                            s22222.append(":");
                            if (min22 == -1) {
                            }
                            s22222.append(num3);
                            s22222.append(":");
                            if (sec22 == -1) {
                            }
                            s22222.append(num4);
                            if (tm22.equals(BuildConfig.FLAVOR)) {
                            }
                            if (!tzone.equals(BuildConfig.FLAVOR)) {
                            }
                            c2.setTime(new SimpleDateFormat(t2.toString(), Locale.ENGLISH).parse(s22222.toString()));
                            if (year22 == -1) {
                            }
                            if (month22 == -1) {
                            }
                            if (day2 == -1) {
                            }
                            if (c.compareTo(c2) != 1) {
                            }
                            result.add(c.getTime());
                            result.add(c2.getTime());
                            return result;
                        }
                    } else {
                        month23 = month2;
                        month24 = day23;
                    }
                } else {
                    month23 = month2;
                    month24 = day23;
                }
                day2 = month24;
                day22 = month23;
                if (dt2st == 0) {
                }
                if (dtst == 2) {
                }
                day = day6;
                day3 = month5;
                month = year5;
                if (dtst != 0) {
                }
                if (!tm7.trim().equals(BuildConfig.FLAVOR)) {
                }
                if (month != year2) {
                }
                isBefore = false;
                if (isBefore) {
                }
                if (!tz22.equals(BuildConfig.FLAVOR)) {
                }
                c = Calendar.getInstance();
                StringBuffer t62222 = new StringBuffer();
                StringBuffer s32222 = new StringBuffer();
                if (hour3 > 12) {
                }
                s32222.append(hour3 == -1 ? Integer.valueOf(hour3) : "08");
                s32222.append(":");
                if (min3 == -1) {
                }
                s32222.append(num);
                s32222.append(":");
                if (sec3 == -1) {
                }
                s32222.append(num2);
                if (tm.equals(BuildConfig.FLAVOR)) {
                }
                if (tzone.equals(BuildConfig.FLAVOR)) {
                }
                c.setTime(new SimpleDateFormat(t.toString(), Locale.ENGLISH).parse(s32222.toString()));
                Date d2222 = new Date(rTime);
                Calendar tc2222 = Calendar.getInstance();
                tc2222.setTime(d2222);
                if (month == -1) {
                }
                if (day3 == -1) {
                }
                if (day == -1) {
                }
                c2 = Calendar.getInstance();
                StringBuffer t222222 = new StringBuffer();
                StringBuffer s222222 = new StringBuffer();
                if (hour22 > 12) {
                }
                s222222.append(hour22 == -1 ? Integer.valueOf(hour22) : "08");
                s222222.append(":");
                if (min22 == -1) {
                }
                s222222.append(num3);
                s222222.append(":");
                if (sec22 == -1) {
                }
                s222222.append(num4);
                if (tm22.equals(BuildConfig.FLAVOR)) {
                }
                if (!tzone.equals(BuildConfig.FLAVOR)) {
                }
                c2.setTime(new SimpleDateFormat(t2.toString(), Locale.ENGLISH).parse(s222222.toString()));
                if (year22 == -1) {
                }
                if (month22 == -1) {
                }
                if (day2 == -1) {
                }
                if (c.compareTo(c2) != 1) {
                }
                result.add(c.getTime());
                result.add(c2.getTime());
                return result;
            }
            str = "HH";
            year2 = year23;
            month2 = month26;
            if (dtst == 0) {
            }
            if (dt2st != 2) {
            }
            day2 = month24;
            day22 = month23;
            if (dt2st == 0) {
            }
            if (dtst == 2) {
            }
            day = day6;
            day3 = month5;
            month = year5;
            if (dtst != 0) {
            }
            if (!tm7.trim().equals(BuildConfig.FLAVOR)) {
            }
            if (month != year2) {
            }
            isBefore = false;
            if (isBefore) {
            }
            if (!tz22.equals(BuildConfig.FLAVOR)) {
            }
            c = Calendar.getInstance();
            StringBuffer t622222 = new StringBuffer();
            StringBuffer s322222 = new StringBuffer();
            if (hour3 > 12) {
            }
            s322222.append(hour3 == -1 ? Integer.valueOf(hour3) : "08");
            s322222.append(":");
            if (min3 == -1) {
            }
            s322222.append(num);
            s322222.append(":");
            if (sec3 == -1) {
            }
            s322222.append(num2);
            if (tm.equals(BuildConfig.FLAVOR)) {
            }
            if (tzone.equals(BuildConfig.FLAVOR)) {
            }
            try {
                c.setTime(new SimpleDateFormat(t.toString(), Locale.ENGLISH).parse(s322222.toString()));
            } catch (ParseException e3) {
                e3.printStackTrace();
            }
            Date d22222 = new Date(rTime);
            Calendar tc22222 = Calendar.getInstance();
            tc22222.setTime(d22222);
            if (month == -1) {
            }
            if (day3 == -1) {
            }
            if (day == -1) {
            }
            c2 = Calendar.getInstance();
            StringBuffer t2222222 = new StringBuffer();
            StringBuffer s2222222 = new StringBuffer();
            if (hour22 > 12) {
            }
            s2222222.append(hour22 == -1 ? Integer.valueOf(hour22) : "08");
            s2222222.append(":");
            if (min22 == -1) {
            }
            s2222222.append(num3);
            s2222222.append(":");
            if (sec22 == -1) {
            }
            s2222222.append(num4);
            if (tm22.equals(BuildConfig.FLAVOR)) {
            }
            if (!tzone.equals(BuildConfig.FLAVOR)) {
            }
            try {
                c2.setTime(new SimpleDateFormat(t2.toString(), Locale.ENGLISH).parse(s2222222.toString()));
            } catch (ParseException e4) {
                e4.printStackTrace();
            }
            if (year22 == -1) {
            }
            if (month22 == -1) {
            }
            if (day2 == -1) {
            }
            if (c.compareTo(c2) != 1) {
            }
            result.add(c.getTime());
            result.add(c2.getTime());
            return result;
        }
    }
}

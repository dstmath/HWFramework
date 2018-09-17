package com.huawei.g11n.tmr.datetime.parse;

import com.huawei.g11n.tmr.Filter;
import com.huawei.g11n.tmr.Match;
import com.huawei.g11n.tmr.datetime.utils.DatePeriod;
import com.huawei.g11n.tmr.datetime.utils.LocaleParam;
import huawei.android.provider.HanziToPinyin.Token;
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

    public DateConvert(String str) {
        this.locale = str;
    }

    public DatePeriod filterByParse(String str, List<Match> list, String str2, String str3) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        DatePeriod dp;
        if (list.size() != 1) {
            dp = convertMutilMatch(str, list, str2, str3).getDp();
        } else {
            dp = ((Match) list.get(0)).getDp();
        }
        return dp;
    }

    private int nestDealDate(String str, Match match, List<Match> list, int i) {
        int i2 = 0;
        Match match2 = (Match) list.get(0);
        int type = Filter.getType(match2.getRegex());
        if ((type != 1 && type != 5 && type != 6) || type == Filter.getType(match.getRegex()) || type == i) {
            return 0;
        }
        String substring = str.substring(match.getEnd(), match2.getBegin());
        if (LocaleParam.isRelDates(substring, this.locale) || substring.trim().equals("(")) {
            type = list.size() <= 1 ? 0 : nestDealDate(str, match2, list.subList(1, list.size()), match.getType()) != 1 ? 0 : 1;
            boolean equals;
            if (substring.trim().equals("(")) {
                String group;
                Matcher matcher = Pattern.compile("\\s*\\((.*?)\\s*\\)").matcher(str.substring(match.getEnd()));
                if (matcher.lookingAt()) {
                    group = matcher.group(1);
                } else {
                    group = null;
                }
                equals = group == null ? false : group.trim().equals(str.substring(match2.getBegin(), type == 0 ? match2.getEnd() : ((Match) list.get(1)).getEnd()).trim());
            } else {
                equals = false;
            }
            if (LocaleParam.isRelDates(substring, this.locale) || r0) {
                i2 = type == 0 ? 1 : 2;
            }
        }
        return i2;
    }

    private List<Match> filterDate(String str, List<Match> list) {
        List<Match> arrayList = new ArrayList();
        int i = 0;
        while (i < list.size()) {
            Match match = (Match) list.get(i);
            int type = Filter.getType(match.getRegex());
            if (type == 1 || type == 5 || type == 6 || type == 7) {
                List subList;
                int size = (list.size() - 1) - i;
                if (size > 1) {
                    subList = list.subList(i + 1, i + 3);
                } else if (size != 1) {
                    subList = null;
                } else {
                    subList = list.subList(i + 1, i + 2);
                }
                if (size == 0 || subList == null) {
                    arrayList.add(match);
                } else {
                    type = nestDealDate(str, match, subList, -1);
                    match.setType(1);
                    if (type != 0) {
                        Match match2;
                        int type2;
                        if (type == 1) {
                            type = i + 1;
                            if (Filter.getType(match.getRegex()) <= Filter.getType(((Match) list.get(type)).getRegex())) {
                                size = type;
                                match2 = null;
                            } else {
                                size = type;
                                match2 = (Match) list.get(type);
                            }
                        } else if (type != 2) {
                            match2 = null;
                            size = i;
                        } else {
                            type = i + 2;
                            size = Filter.getType(match.getRegex());
                            type2 = Filter.getType(((Match) list.get(type - 1)).getRegex());
                            i = Filter.getType(((Match) list.get(type)).getRegex());
                            if (type2 < i && type2 < size) {
                                size = type;
                                match2 = (Match) list.get(type - 1);
                            } else if (i < type2 && i < size) {
                                size = type;
                                match2 = (Match) list.get(type);
                            } else {
                                size = type;
                                match2 = null;
                            }
                        }
                        Match match3 = (Match) list.get(size);
                        type2 = str.indexOf(40, match.getEnd());
                        if (type2 != -1 && type2 < match3.getBegin()) {
                            type2 = str.indexOf(41, match3.getEnd());
                            if (type2 == -1) {
                                type2 = 0;
                            } else if (str.substring(match3.getEnd(), type2 + 1).trim().equals(")")) {
                                type2 = (type2 - match3.getEnd()) + 1;
                            } else {
                                type2 = 0;
                            }
                        } else {
                            type2 = 0;
                        }
                        match.setEnd(match3.getEnd() + type2);
                        if (match2 != null) {
                            match.getDp().setBegin(match2.getDp().getBegin());
                        }
                        arrayList.add(match);
                        i = size;
                    } else {
                        arrayList.add(match);
                    }
                }
            } else {
                arrayList.add(match);
            }
            i++;
        }
        return arrayList;
    }

    private Match filterPeriod(String str, List<Match> list, String str2) {
        Pattern compile = Pattern.compile("\\.?\\s*(-{1,2}|~|\u8d77?\u81f3|\u5230|au|\u2013|\u2014|\uff5e|\u062a\u0627|\u0926\u0947\u0916\u093f|\u0434\u0430|\u043f\u0430|\u09a5\u09c7\u0995\u09c7|\u0eab\u0eb2" + str2 + ")\\s*", 2);
        Iterator it = list.iterator();
        Match match = null;
        while (it.hasNext()) {
            Match match2 = (Match) it.next();
            if (match != null) {
                int type = match.getDp().getType();
                int type2 = match2.getDp().getType();
                if (type == type2) {
                    if (!(type == 1 || type == 2)) {
                        if (type == 0) {
                        }
                    }
                    if (compile.matcher(str.substring(match.getEnd(), match2.getBegin())).matches()) {
                        match.setEnd(match2.getEnd());
                        match.setType(3);
                        if (type == 2 && type2 == 2) {
                            match.setIsTimePeriod(true);
                        }
                        match.getDp().setEnd(match2.getDp().getBegin());
                        it.remove();
                    }
                }
                if (type == 0) {
                    if (type2 != 2) {
                    }
                    if (compile.matcher(str.substring(match.getEnd(), match2.getBegin())).matches()) {
                        match.setEnd(match2.getEnd());
                        match.setType(3);
                        match.setIsTimePeriod(true);
                        match.getDp().setEnd(match2.getDp().getBegin());
                        it.remove();
                    }
                }
            } else {
                match = match2;
            }
        }
        return match;
    }

    private List<Match> filterDateTime(String str, List<Match> list, String str2) {
        Pattern compile = Pattern.compile("\\s*(at|\u00e0s|\uff0c|,|\u060c\u200f|u|kl\\.|\u0915\u094b|\u7684|o|\u00e0|a\\s+les|ve|la|pada|kl|\u03c3\u03c4\u03b9\u03c2|alle|jam|\u0e82\u0ead\u0e87\u0ea7\u0eb1\u0e99\u0e97\u0eb5" + str2 + ")\\s*", 2);
        Iterator it = list.iterator();
        Match match = null;
        while (it.hasNext()) {
            Match match2 = (Match) it.next();
            if (match != null) {
                boolean z;
                int type = match.getDp().getType();
                int type2 = match2.getDp().getType();
                if (!(type == 1 && type2 == 2)) {
                    if (type != 1 || type2 != 5) {
                        if (type != 2 || type2 != 1) {
                            if (type == 5) {
                                if (type2 != 1) {
                                }
                            }
                            match = match2;
                        }
                    }
                }
                CharSequence substring = str.substring(match.getEnd(), match2.getBegin());
                if (substring.trim().equals("")) {
                    z = true;
                } else {
                    z = compile.matcher(substring).matches();
                }
                if (z) {
                    match.setEnd(match2.getEnd());
                    if (type == 1 && type2 == 2) {
                        match.getDp().getBegin().setTime(match2.getDp().getBegin().getTime());
                    } else if (type == 2 && type2 == 1) {
                        match.getDp().getBegin().setDay(match2.getDp().getBegin().getDate());
                    } else if (type == 1 && type2 == 5) {
                        match.getDp().getBegin().setTime(match2.getDp().getBegin().getTime());
                        match.getDp().setEnd(match2.getDp().getEnd());
                    } else if (type == 5 && type2 == 1) {
                        match.getDp().getBegin().setDay(match2.getDp().getBegin().getDate());
                        match.getDp().getEnd().setDay(match2.getDp().getBegin().getDate());
                    }
                    it.remove();
                } else {
                    Object obj;
                    if (type == 2 && type2 == 1) {
                        int i = 0;
                        Matcher matcher = Pattern.compile("\\s*\\((.*?)\\s*\\)").matcher(str.substring(match.getEnd()));
                        String str3 = null;
                        if (matcher.lookingAt()) {
                            str3 = matcher.group(1);
                            i = matcher.group().length();
                        }
                        if (str3 == null ? false : str3.trim().equals(str.substring(match2.getBegin(), match2.getEnd()).trim())) {
                            match.setEnd(match.getEnd() + i);
                            match.getDp().getBegin().setDay(match2.getDp().getBegin().getDate());
                            it.remove();
                            obj = null;
                        } else {
                            int i2 = 1;
                        }
                    } else {
                        obj = 1;
                    }
                    if (type == 1 && type2 == 2) {
                        String substring2 = str.substring(0, match.getBegin());
                        String substring3 = str.substring(match.getEnd(), match2.getBegin());
                        if (substring2.trim().endsWith("(") && substring3.trim().startsWith(")")) {
                            match.setBegin(substring2.lastIndexOf(40));
                            match.setEnd(match2.getEnd());
                            match.getDp().getBegin().setTime(match2.getDp().getBegin().getTime());
                            it.remove();
                            obj = null;
                        }
                    }
                    if (obj != null) {
                        match = match2;
                    }
                }
            } else {
                match = match2;
            }
        }
        return list;
    }

    private Match convertMutilMatch(String str, List<Match> list, String str2, String str3) {
        return filterPeriod(str, filterDateTime(str, filterDate(str, list), str3), str2);
    }

    public List<Date> convert(DatePeriod datePeriod, long j) {
        List arrayList = new ArrayList();
        if (datePeriod == null) {
            return null;
        }
        int type = datePeriod.getType();
        int year;
        int month;
        int day;
        int second;
        String mark;
        String timezone;
        StringBuffer stringBuffer;
        String str;
        Calendar instance;
        if (type == 0) {
            Date parse;
            year = datePeriod.getBegin().getDate().getYear();
            month = datePeriod.getBegin().getDate().getMonth();
            day = datePeriod.getBegin().getDate().getDay();
            int clock = datePeriod.getBegin().getTime().getClock();
            int minute = datePeriod.getBegin().getTime().getMinute();
            second = datePeriod.getBegin().getTime().getSecond();
            mark = datePeriod.getBegin().getTime() == null ? "" : datePeriod.getBegin().getTime().getMark();
            timezone = datePeriod.getBegin().getTime() == null ? "" : datePeriod.getBegin().getTime().getTimezone();
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer = new StringBuffer();
            str = (clock > 12 && !mark.trim().equals("")) ? "" : mark;
            stringBuffer.append(clock == -1 ? "00" : Integer.valueOf(clock)).append(":").append(minute == -1 ? "00" : Integer.valueOf(minute)).append(":").append(second == -1 ? "00" : Integer.valueOf(second));
            if (str.equals("")) {
                stringBuffer2.append("HH").append(":mm:ss");
            } else {
                stringBuffer2.append("hh").append(":mm:ss").append(" a");
                stringBuffer.append(Token.SEPARATOR).append(str);
            }
            if (!timezone.equals("")) {
                stringBuffer2.append(" Z");
                stringBuffer.append(Token.SEPARATOR).append(timezone);
            }
            try {
                parse = new SimpleDateFormat(stringBuffer2.toString(), Locale.ENGLISH).parse(stringBuffer.toString());
            } catch (ParseException e) {
                e.printStackTrace();
                parse = null;
            }
            Calendar instance2 = Calendar.getInstance();
            instance = Calendar.getInstance();
            instance.setTime(new Date(j));
            if (parse != null) {
                instance2.setTime(parse);
            }
            if (year == -1) {
                instance2.set(1, instance.get(1));
            } else {
                instance2.set(1, year);
            }
            if (month == -1) {
                instance2.set(2, instance.get(2));
            } else {
                instance2.set(2, month);
            }
            if (day == -1) {
                instance2.set(5, instance.get(5));
            } else {
                instance2.set(5, day);
            }
            arrayList.add(instance2.getTime());
        } else if (type == 1) {
            type = datePeriod.getBegin().getDate().getYear();
            r3 = datePeriod.getBegin().getDate().getMonth();
            r4 = datePeriod.getBegin().getDate().getDay();
            Calendar instance3 = Calendar.getInstance();
            r6 = Calendar.getInstance();
            r6.setTime(new Date(j));
            if (type == -1) {
                instance3.set(1, r6.get(1));
            } else {
                instance3.set(1, type);
            }
            if (r3 == -1) {
                instance3.set(2, r6.get(2));
            } else {
                instance3.set(2, r3);
            }
            if (r4 == -1) {
                instance3.set(5, r6.get(5));
            } else {
                instance3.set(5, r4);
            }
            instance3.set(11, 0);
            instance3.set(12, 0);
            instance3.set(13, 0);
            arrayList.add(instance3.getTime());
        } else if (type == 2) {
            Object parse2;
            year = datePeriod.getBegin().getTime().getClock();
            month = datePeriod.getBegin().getTime().getMinute();
            day = datePeriod.getBegin().getTime().getSecond();
            mark = datePeriod.getBegin().getTime() == null ? "" : datePeriod.getBegin().getTime().getMark();
            timezone = datePeriod.getBegin().getTime() == null ? "" : datePeriod.getBegin().getTime().getTimezone();
            StringBuffer stringBuffer3 = new StringBuffer();
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer3.append("yyyy-MM-dd ");
            instance = Calendar.getInstance();
            instance.setTime(new Date(j));
            stringBuffer4.append(instance.get(1)).append("-").append(instance.get(2) + 1).append("-").append(instance.get(5)).append(Token.SEPARATOR);
            str = (year > 12 && !mark.trim().equals("")) ? "" : mark;
            stringBuffer4.append(year == -1 ? "00" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
            if (str.equals("")) {
                stringBuffer3.append("HH").append(":mm:ss");
            } else {
                stringBuffer3.append("hh").append(":mm:ss").append(" a");
                stringBuffer4.append(Token.SEPARATOR).append(str);
            }
            if (!timezone.equals("")) {
                stringBuffer3.append(" Z");
                stringBuffer4.append(Token.SEPARATOR).append(timezone);
            }
            try {
                parse2 = new SimpleDateFormat(stringBuffer3.toString(), Locale.ENGLISH).parse(stringBuffer4.toString());
            } catch (ParseException e2) {
                e2.printStackTrace();
                parse2 = null;
            }
            arrayList.add(parse2);
        } else if (type > 2) {
            int i;
            int i2;
            String str2;
            Object obj;
            Calendar instance4;
            StringBuffer stringBuffer5;
            Date date;
            StringBuffer stringBuffer6;
            StringBuffer stringBuffer7;
            type = datePeriod.getBegin().getDate() == null ? -1 : datePeriod.getBegin().getDate().getYear();
            r3 = datePeriod.getBegin().getDate() == null ? -1 : datePeriod.getBegin().getDate().getMonth();
            r4 = datePeriod.getBegin().getDate() == null ? -1 : datePeriod.getBegin().getDate().getDay();
            year = datePeriod.getBegin().getTime() == null ? -1 : datePeriod.getBegin().getTime().getClock();
            month = datePeriod.getBegin().getTime() == null ? -1 : datePeriod.getBegin().getTime().getMinute();
            day = datePeriod.getBegin().getTime() == null ? -1 : datePeriod.getBegin().getTime().getSecond();
            String mark2 = datePeriod.getBegin().getTime() == null ? "" : datePeriod.getBegin().getTime().getMark();
            String timezone2 = datePeriod.getBegin().getTime() == null ? "" : datePeriod.getBegin().getTime().getTimezone();
            second = datePeriod.getEnd().getDate() == null ? -1 : datePeriod.getEnd().getDate().getYear();
            int month2 = datePeriod.getEnd().getDate() == null ? -1 : datePeriod.getEnd().getDate().getMonth();
            int day2 = datePeriod.getEnd().getDate() == null ? -1 : datePeriod.getEnd().getDate().getDay();
            int clock2 = datePeriod.getEnd().getTime() == null ? -1 : datePeriod.getEnd().getTime().getClock();
            int minute2 = datePeriod.getEnd().getTime() == null ? -1 : datePeriod.getEnd().getTime().getMinute();
            int second2 = datePeriod.getEnd().getTime() == null ? -1 : datePeriod.getEnd().getTime().getSecond();
            String mark3 = datePeriod.getEnd().getTime() == null ? "" : datePeriod.getEnd().getTime().getMark();
            String timezone3 = datePeriod.getEnd().getTime() == null ? "" : datePeriod.getEnd().getTime().getTimezone();
            if (datePeriod.getBegin().getTime() != null) {
                datePeriod.getBegin().getTime().isMarkBefore();
            }
            boolean isMarkBefore = datePeriod.getEnd().getTime() == null ? true : datePeriod.getEnd().getTime().isMarkBefore();
            int satuts = datePeriod.getBegin().getSatuts();
            int satuts2 = datePeriod.getEnd().getSatuts();
            if (satuts == 0 || satuts == 1) {
                if (satuts2 == 0 || satuts2 == 1) {
                    if (type != -1 && second == -1) {
                        second = type;
                    }
                    if (type == -1 && second != -1) {
                        type = second;
                    }
                    if (r3 != -1 && month2 == -1) {
                        month2 = r3;
                    }
                    if (r3 == -1 && month2 != -1) {
                        i = month2;
                        i2 = second;
                        r3 = month2;
                        second = type;
                    } else {
                        i = month2;
                        i2 = second;
                        second = type;
                    }
                    if (satuts == 0 || satuts == 1) {
                        if (satuts2 == 2) {
                            if (second != -1 && r20 == -1) {
                                i2 = second;
                            }
                            if (r3 != -1 && r19 == -1) {
                                i = r3;
                            }
                            if (r4 != -1 && day2 == -1) {
                                type = r4;
                            } else {
                                type = day2;
                            }
                            if (satuts2 == 0 || satuts2 == 1) {
                                if (satuts == 2) {
                                    if (i2 != -1 && second == -1) {
                                        second = i2;
                                    }
                                    if (i != -1 && r3 == -1) {
                                        r3 = i;
                                    }
                                    if (type != -1 && r4 == -1) {
                                        r4 = type;
                                    }
                                }
                            }
                            str2 = "";
                            if (satuts == 0 || satuts == 2 || satuts2 == 0 || satuts2 == 2) {
                                if (mark2.trim().equals("")) {
                                    if (!mark3.trim().equals("")) {
                                        if (second != i2 || r3 != i || r4 != type) {
                                            isMarkBefore = true;
                                        }
                                        if (!isMarkBefore) {
                                            mark2 = mark3;
                                        }
                                        if (timezone3.equals("")) {
                                            timezone2 = timezone3;
                                        } else {
                                            if (timezone3.equals("")) {
                                                if (timezone2.equals("")) {
                                                }
                                            }
                                            timezone2 = str2;
                                        }
                                    }
                                }
                                if (mark3.trim().equals("")) {
                                    if (!mark2.trim().equals("")) {
                                        if (second == i2 && r3 == i && r4 == type) {
                                            obj = 1;
                                        } else {
                                            obj = null;
                                        }
                                        if (obj != null) {
                                            mark3 = mark2;
                                        }
                                    }
                                }
                                if (timezone3.equals("")) {
                                    timezone2 = timezone3;
                                } else {
                                    if (timezone3.equals("")) {
                                        if (timezone2.equals("")) {
                                        }
                                    }
                                    timezone2 = str2;
                                }
                            } else {
                                timezone2 = str2;
                            }
                            instance4 = Calendar.getInstance();
                            stringBuffer = new StringBuffer();
                            stringBuffer5 = new StringBuffer();
                            if (year > 12) {
                                if (!mark2.trim().equals("")) {
                                    mark2 = "";
                                }
                            }
                            stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                            if (mark2.equals("")) {
                                stringBuffer.append("HH").append(":mm:ss");
                            } else {
                                stringBuffer.append("hh").append(":mm:ss").append(" a");
                                stringBuffer5.append(Token.SEPARATOR).append(mark2);
                            }
                            if (!timezone2.equals("")) {
                                stringBuffer.append(" Z");
                                stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                            }
                            instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                            date = new Date(j);
                            r6 = Calendar.getInstance();
                            r6.setTime(date);
                            if (second == -1) {
                                instance4.set(1, r6.get(1));
                            } else {
                                instance4.set(1, second);
                            }
                            if (r3 == -1) {
                                instance4.set(2, r6.get(2));
                            } else {
                                instance4.set(2, r3);
                            }
                            if (r4 == -1) {
                                instance4.set(5, r6.get(5));
                            } else {
                                instance4.set(5, r4);
                            }
                            arrayList.add(instance4.getTime());
                            instance = Calendar.getInstance();
                            stringBuffer6 = new StringBuffer();
                            stringBuffer7 = new StringBuffer();
                            if (clock2 > 12 && !r16.trim().equals("")) {
                                mark3 = "";
                            }
                            stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                            if (mark3.equals("")) {
                                stringBuffer6.append("HH").append(":mm:ss");
                            } else {
                                stringBuffer6.append("hh").append(":mm:ss").append(" a");
                                stringBuffer7.append(Token.SEPARATOR).append(mark3);
                            }
                            if (!timezone2.equals("")) {
                                stringBuffer6.append(" Z");
                                stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                            }
                            instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                            if (i2 == -1) {
                                instance.set(1, r6.get(1));
                            } else {
                                instance.set(1, i2);
                            }
                            if (i == -1) {
                                instance.set(2, r6.get(2));
                            } else {
                                instance.set(2, i);
                            }
                            if (type == -1) {
                                instance.set(5, r6.get(5));
                            } else {
                                instance.set(5, type);
                            }
                            arrayList.add(instance.getTime());
                        }
                    }
                    type = day2;
                    if (satuts2 == 0) {
                        str2 = "";
                        if (satuts == 0) {
                            timezone2 = str2;
                            instance4 = Calendar.getInstance();
                            stringBuffer = new StringBuffer();
                            stringBuffer5 = new StringBuffer();
                            if (year > 12) {
                                if (mark2.trim().equals("")) {
                                    mark2 = "";
                                }
                            }
                            if (year == -1) {
                            }
                            if (month == -1) {
                            }
                            if (day == -1) {
                            }
                            stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                            if (mark2.equals("")) {
                                stringBuffer.append("hh").append(":mm:ss").append(" a");
                                stringBuffer5.append(Token.SEPARATOR).append(mark2);
                            } else {
                                stringBuffer.append("HH").append(":mm:ss");
                            }
                            if (timezone2.equals("")) {
                                stringBuffer.append(" Z");
                                stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                            }
                            instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                            date = new Date(j);
                            r6 = Calendar.getInstance();
                            r6.setTime(date);
                            if (second == -1) {
                                instance4.set(1, second);
                            } else {
                                instance4.set(1, r6.get(1));
                            }
                            if (r3 == -1) {
                                instance4.set(2, r3);
                            } else {
                                instance4.set(2, r6.get(2));
                            }
                            if (r4 == -1) {
                                instance4.set(5, r4);
                            } else {
                                instance4.set(5, r6.get(5));
                            }
                            arrayList.add(instance4.getTime());
                            instance = Calendar.getInstance();
                            stringBuffer6 = new StringBuffer();
                            stringBuffer7 = new StringBuffer();
                            mark3 = "";
                            if (clock2 == -1) {
                            }
                            if (minute2 == -1) {
                            }
                            if (second2 == -1) {
                            }
                            stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                            if (mark3.equals("")) {
                                stringBuffer6.append("hh").append(":mm:ss").append(" a");
                                stringBuffer7.append(Token.SEPARATOR).append(mark3);
                            } else {
                                stringBuffer6.append("HH").append(":mm:ss");
                            }
                            if (timezone2.equals("")) {
                                stringBuffer6.append(" Z");
                                stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                            }
                            instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                            if (i2 == -1) {
                                instance.set(1, i2);
                            } else {
                                instance.set(1, r6.get(1));
                            }
                            if (i == -1) {
                                instance.set(2, i);
                            } else {
                                instance.set(2, r6.get(2));
                            }
                            if (type == -1) {
                                instance.set(5, type);
                            } else {
                                instance.set(5, r6.get(5));
                            }
                            arrayList.add(instance.getTime());
                        }
                        if (mark2.trim().equals("")) {
                            if (mark3.trim().equals("")) {
                                if (second != i2) {
                                    if (isMarkBefore) {
                                        mark2 = mark3;
                                    }
                                    if (timezone3.equals("")) {
                                        if (timezone3.equals("")) {
                                            if (timezone2.equals("")) {
                                            }
                                        }
                                        timezone2 = str2;
                                    } else {
                                        timezone2 = timezone3;
                                    }
                                    instance4 = Calendar.getInstance();
                                    stringBuffer = new StringBuffer();
                                    stringBuffer5 = new StringBuffer();
                                    if (year > 12) {
                                        if (mark2.trim().equals("")) {
                                            mark2 = "";
                                        }
                                    }
                                    if (year == -1) {
                                    }
                                    if (month == -1) {
                                    }
                                    if (day == -1) {
                                    }
                                    stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                                    if (mark2.equals("")) {
                                        stringBuffer.append("HH").append(":mm:ss");
                                    } else {
                                        stringBuffer.append("hh").append(":mm:ss").append(" a");
                                        stringBuffer5.append(Token.SEPARATOR).append(mark2);
                                    }
                                    if (timezone2.equals("")) {
                                        stringBuffer.append(" Z");
                                        stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                                    }
                                    instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                                    date = new Date(j);
                                    r6 = Calendar.getInstance();
                                    r6.setTime(date);
                                    if (second == -1) {
                                        instance4.set(1, r6.get(1));
                                    } else {
                                        instance4.set(1, second);
                                    }
                                    if (r3 == -1) {
                                        instance4.set(2, r6.get(2));
                                    } else {
                                        instance4.set(2, r3);
                                    }
                                    if (r4 == -1) {
                                        instance4.set(5, r6.get(5));
                                    } else {
                                        instance4.set(5, r4);
                                    }
                                    arrayList.add(instance4.getTime());
                                    instance = Calendar.getInstance();
                                    stringBuffer6 = new StringBuffer();
                                    stringBuffer7 = new StringBuffer();
                                    mark3 = "";
                                    if (clock2 == -1) {
                                    }
                                    if (minute2 == -1) {
                                    }
                                    if (second2 == -1) {
                                    }
                                    stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                                    if (mark3.equals("")) {
                                        stringBuffer6.append("HH").append(":mm:ss");
                                    } else {
                                        stringBuffer6.append("hh").append(":mm:ss").append(" a");
                                        stringBuffer7.append(Token.SEPARATOR).append(mark3);
                                    }
                                    if (timezone2.equals("")) {
                                        stringBuffer6.append(" Z");
                                        stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                                    }
                                    instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                                    if (i2 == -1) {
                                        instance.set(1, r6.get(1));
                                    } else {
                                        instance.set(1, i2);
                                    }
                                    if (i == -1) {
                                        instance.set(2, r6.get(2));
                                    } else {
                                        instance.set(2, i);
                                    }
                                    if (type == -1) {
                                        instance.set(5, r6.get(5));
                                    } else {
                                        instance.set(5, type);
                                    }
                                    arrayList.add(instance.getTime());
                                }
                                isMarkBefore = true;
                                if (isMarkBefore) {
                                    mark2 = mark3;
                                }
                                if (timezone3.equals("")) {
                                    timezone2 = timezone3;
                                } else {
                                    if (timezone3.equals("")) {
                                        if (timezone2.equals("")) {
                                        }
                                    }
                                    timezone2 = str2;
                                }
                                instance4 = Calendar.getInstance();
                                stringBuffer = new StringBuffer();
                                stringBuffer5 = new StringBuffer();
                                if (year > 12) {
                                    if (mark2.trim().equals("")) {
                                        mark2 = "";
                                    }
                                }
                                if (year == -1) {
                                }
                                if (month == -1) {
                                }
                                if (day == -1) {
                                }
                                stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                                if (mark2.equals("")) {
                                    stringBuffer.append("hh").append(":mm:ss").append(" a");
                                    stringBuffer5.append(Token.SEPARATOR).append(mark2);
                                } else {
                                    stringBuffer.append("HH").append(":mm:ss");
                                }
                                if (timezone2.equals("")) {
                                    stringBuffer.append(" Z");
                                    stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                                }
                                instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                                date = new Date(j);
                                r6 = Calendar.getInstance();
                                r6.setTime(date);
                                if (second == -1) {
                                    instance4.set(1, second);
                                } else {
                                    instance4.set(1, r6.get(1));
                                }
                                if (r3 == -1) {
                                    instance4.set(2, r3);
                                } else {
                                    instance4.set(2, r6.get(2));
                                }
                                if (r4 == -1) {
                                    instance4.set(5, r4);
                                } else {
                                    instance4.set(5, r6.get(5));
                                }
                                arrayList.add(instance4.getTime());
                                instance = Calendar.getInstance();
                                stringBuffer6 = new StringBuffer();
                                stringBuffer7 = new StringBuffer();
                                mark3 = "";
                                if (clock2 == -1) {
                                }
                                if (minute2 == -1) {
                                }
                                if (second2 == -1) {
                                }
                                stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                                if (mark3.equals("")) {
                                    stringBuffer6.append("hh").append(":mm:ss").append(" a");
                                    stringBuffer7.append(Token.SEPARATOR).append(mark3);
                                } else {
                                    stringBuffer6.append("HH").append(":mm:ss");
                                }
                                if (timezone2.equals("")) {
                                    stringBuffer6.append(" Z");
                                    stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                                }
                                instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                                if (i2 == -1) {
                                    instance.set(1, i2);
                                } else {
                                    instance.set(1, r6.get(1));
                                }
                                if (i == -1) {
                                    instance.set(2, i);
                                } else {
                                    instance.set(2, r6.get(2));
                                }
                                if (type == -1) {
                                    instance.set(5, type);
                                } else {
                                    instance.set(5, r6.get(5));
                                }
                                arrayList.add(instance.getTime());
                            }
                        }
                        if (mark3.trim().equals("")) {
                            if (mark2.trim().equals("")) {
                                if (second == i2) {
                                    obj = 1;
                                    if (obj != null) {
                                        mark3 = mark2;
                                    }
                                }
                                obj = null;
                                if (obj != null) {
                                    mark3 = mark2;
                                }
                            }
                        }
                        if (timezone3.equals("")) {
                            if (timezone3.equals("")) {
                                if (timezone2.equals("")) {
                                }
                            }
                            timezone2 = str2;
                        } else {
                            timezone2 = timezone3;
                        }
                        instance4 = Calendar.getInstance();
                        stringBuffer = new StringBuffer();
                        stringBuffer5 = new StringBuffer();
                        if (year > 12) {
                            if (mark2.trim().equals("")) {
                                mark2 = "";
                            }
                        }
                        if (year == -1) {
                        }
                        if (month == -1) {
                        }
                        if (day == -1) {
                        }
                        stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                        if (mark2.equals("")) {
                            stringBuffer.append("HH").append(":mm:ss");
                        } else {
                            stringBuffer.append("hh").append(":mm:ss").append(" a");
                            stringBuffer5.append(Token.SEPARATOR).append(mark2);
                        }
                        if (timezone2.equals("")) {
                            stringBuffer.append(" Z");
                            stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                        }
                        instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                        date = new Date(j);
                        r6 = Calendar.getInstance();
                        r6.setTime(date);
                        if (second == -1) {
                            instance4.set(1, r6.get(1));
                        } else {
                            instance4.set(1, second);
                        }
                        if (r3 == -1) {
                            instance4.set(2, r6.get(2));
                        } else {
                            instance4.set(2, r3);
                        }
                        if (r4 == -1) {
                            instance4.set(5, r6.get(5));
                        } else {
                            instance4.set(5, r4);
                        }
                        arrayList.add(instance4.getTime());
                        instance = Calendar.getInstance();
                        stringBuffer6 = new StringBuffer();
                        stringBuffer7 = new StringBuffer();
                        mark3 = "";
                        if (clock2 == -1) {
                        }
                        if (minute2 == -1) {
                        }
                        if (second2 == -1) {
                        }
                        stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                        if (mark3.equals("")) {
                            stringBuffer6.append("HH").append(":mm:ss");
                        } else {
                            stringBuffer6.append("hh").append(":mm:ss").append(" a");
                            stringBuffer7.append(Token.SEPARATOR).append(mark3);
                        }
                        if (timezone2.equals("")) {
                            stringBuffer6.append(" Z");
                            stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                        }
                        instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                        if (i2 == -1) {
                            instance.set(1, r6.get(1));
                        } else {
                            instance.set(1, i2);
                        }
                        if (i == -1) {
                            instance.set(2, r6.get(2));
                        } else {
                            instance.set(2, i);
                        }
                        if (type == -1) {
                            instance.set(5, r6.get(5));
                        } else {
                            instance.set(5, type);
                        }
                        arrayList.add(instance.getTime());
                    }
                    if (satuts == 2) {
                        second = i2;
                        r3 = i;
                        r4 = type;
                    }
                    str2 = "";
                    if (satuts == 0) {
                        timezone2 = str2;
                        instance4 = Calendar.getInstance();
                        stringBuffer = new StringBuffer();
                        stringBuffer5 = new StringBuffer();
                        if (year > 12) {
                            if (mark2.trim().equals("")) {
                                mark2 = "";
                            }
                        }
                        if (year == -1) {
                        }
                        if (month == -1) {
                        }
                        if (day == -1) {
                        }
                        stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                        if (mark2.equals("")) {
                            stringBuffer.append("hh").append(":mm:ss").append(" a");
                            stringBuffer5.append(Token.SEPARATOR).append(mark2);
                        } else {
                            stringBuffer.append("HH").append(":mm:ss");
                        }
                        if (timezone2.equals("")) {
                            stringBuffer.append(" Z");
                            stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                        }
                        instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                        date = new Date(j);
                        r6 = Calendar.getInstance();
                        r6.setTime(date);
                        if (second == -1) {
                            instance4.set(1, second);
                        } else {
                            instance4.set(1, r6.get(1));
                        }
                        if (r3 == -1) {
                            instance4.set(2, r3);
                        } else {
                            instance4.set(2, r6.get(2));
                        }
                        if (r4 == -1) {
                            instance4.set(5, r4);
                        } else {
                            instance4.set(5, r6.get(5));
                        }
                        arrayList.add(instance4.getTime());
                        instance = Calendar.getInstance();
                        stringBuffer6 = new StringBuffer();
                        stringBuffer7 = new StringBuffer();
                        mark3 = "";
                        if (clock2 == -1) {
                        }
                        if (minute2 == -1) {
                        }
                        if (second2 == -1) {
                        }
                        stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                        if (mark3.equals("")) {
                            stringBuffer6.append("hh").append(":mm:ss").append(" a");
                            stringBuffer7.append(Token.SEPARATOR).append(mark3);
                        } else {
                            stringBuffer6.append("HH").append(":mm:ss");
                        }
                        if (timezone2.equals("")) {
                            stringBuffer6.append(" Z");
                            stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                        }
                        instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                        if (i2 == -1) {
                            instance.set(1, i2);
                        } else {
                            instance.set(1, r6.get(1));
                        }
                        if (i == -1) {
                            instance.set(2, i);
                        } else {
                            instance.set(2, r6.get(2));
                        }
                        if (type == -1) {
                            instance.set(5, type);
                        } else {
                            instance.set(5, r6.get(5));
                        }
                        arrayList.add(instance.getTime());
                    }
                    if (mark2.trim().equals("")) {
                        if (mark3.trim().equals("")) {
                            if (second != i2) {
                                if (isMarkBefore) {
                                    mark2 = mark3;
                                }
                                if (timezone3.equals("")) {
                                    timezone2 = timezone3;
                                } else {
                                    if (timezone3.equals("")) {
                                        if (timezone2.equals("")) {
                                        }
                                    }
                                    timezone2 = str2;
                                }
                                instance4 = Calendar.getInstance();
                                stringBuffer = new StringBuffer();
                                stringBuffer5 = new StringBuffer();
                                if (year > 12) {
                                    if (mark2.trim().equals("")) {
                                        mark2 = "";
                                    }
                                }
                                if (year == -1) {
                                }
                                if (month == -1) {
                                }
                                if (day == -1) {
                                }
                                stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                                if (mark2.equals("")) {
                                    stringBuffer.append("HH").append(":mm:ss");
                                } else {
                                    stringBuffer.append("hh").append(":mm:ss").append(" a");
                                    stringBuffer5.append(Token.SEPARATOR).append(mark2);
                                }
                                if (timezone2.equals("")) {
                                    stringBuffer.append(" Z");
                                    stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                                }
                                instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                                date = new Date(j);
                                r6 = Calendar.getInstance();
                                r6.setTime(date);
                                if (second == -1) {
                                    instance4.set(1, r6.get(1));
                                } else {
                                    instance4.set(1, second);
                                }
                                if (r3 == -1) {
                                    instance4.set(2, r6.get(2));
                                } else {
                                    instance4.set(2, r3);
                                }
                                if (r4 == -1) {
                                    instance4.set(5, r6.get(5));
                                } else {
                                    instance4.set(5, r4);
                                }
                                arrayList.add(instance4.getTime());
                                instance = Calendar.getInstance();
                                stringBuffer6 = new StringBuffer();
                                stringBuffer7 = new StringBuffer();
                                mark3 = "";
                                if (clock2 == -1) {
                                }
                                if (minute2 == -1) {
                                }
                                if (second2 == -1) {
                                }
                                stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                                if (mark3.equals("")) {
                                    stringBuffer6.append("HH").append(":mm:ss");
                                } else {
                                    stringBuffer6.append("hh").append(":mm:ss").append(" a");
                                    stringBuffer7.append(Token.SEPARATOR).append(mark3);
                                }
                                if (timezone2.equals("")) {
                                    stringBuffer6.append(" Z");
                                    stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                                }
                                instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                                if (i2 == -1) {
                                    instance.set(1, r6.get(1));
                                } else {
                                    instance.set(1, i2);
                                }
                                if (i == -1) {
                                    instance.set(2, r6.get(2));
                                } else {
                                    instance.set(2, i);
                                }
                                if (type == -1) {
                                    instance.set(5, r6.get(5));
                                } else {
                                    instance.set(5, type);
                                }
                                arrayList.add(instance.getTime());
                            }
                            isMarkBefore = true;
                            if (isMarkBefore) {
                                mark2 = mark3;
                            }
                            if (timezone3.equals("")) {
                                if (timezone3.equals("")) {
                                    if (timezone2.equals("")) {
                                    }
                                }
                                timezone2 = str2;
                            } else {
                                timezone2 = timezone3;
                            }
                            instance4 = Calendar.getInstance();
                            stringBuffer = new StringBuffer();
                            stringBuffer5 = new StringBuffer();
                            if (year > 12) {
                                if (mark2.trim().equals("")) {
                                    mark2 = "";
                                }
                            }
                            if (year == -1) {
                            }
                            if (month == -1) {
                            }
                            if (day == -1) {
                            }
                            stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                            if (mark2.equals("")) {
                                stringBuffer.append("hh").append(":mm:ss").append(" a");
                                stringBuffer5.append(Token.SEPARATOR).append(mark2);
                            } else {
                                stringBuffer.append("HH").append(":mm:ss");
                            }
                            if (timezone2.equals("")) {
                                stringBuffer.append(" Z");
                                stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                            }
                            instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                            date = new Date(j);
                            r6 = Calendar.getInstance();
                            r6.setTime(date);
                            if (second == -1) {
                                instance4.set(1, second);
                            } else {
                                instance4.set(1, r6.get(1));
                            }
                            if (r3 == -1) {
                                instance4.set(2, r3);
                            } else {
                                instance4.set(2, r6.get(2));
                            }
                            if (r4 == -1) {
                                instance4.set(5, r4);
                            } else {
                                instance4.set(5, r6.get(5));
                            }
                            arrayList.add(instance4.getTime());
                            instance = Calendar.getInstance();
                            stringBuffer6 = new StringBuffer();
                            stringBuffer7 = new StringBuffer();
                            mark3 = "";
                            if (clock2 == -1) {
                            }
                            if (minute2 == -1) {
                            }
                            if (second2 == -1) {
                            }
                            stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                            if (mark3.equals("")) {
                                stringBuffer6.append("hh").append(":mm:ss").append(" a");
                                stringBuffer7.append(Token.SEPARATOR).append(mark3);
                            } else {
                                stringBuffer6.append("HH").append(":mm:ss");
                            }
                            if (timezone2.equals("")) {
                                stringBuffer6.append(" Z");
                                stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                            }
                            instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                            if (i2 == -1) {
                                instance.set(1, i2);
                            } else {
                                instance.set(1, r6.get(1));
                            }
                            if (i == -1) {
                                instance.set(2, i);
                            } else {
                                instance.set(2, r6.get(2));
                            }
                            if (type == -1) {
                                instance.set(5, type);
                            } else {
                                instance.set(5, r6.get(5));
                            }
                            arrayList.add(instance.getTime());
                        }
                    }
                    if (mark3.trim().equals("")) {
                        if (mark2.trim().equals("")) {
                            if (second == i2) {
                                obj = 1;
                                if (obj != null) {
                                    mark3 = mark2;
                                }
                            }
                            obj = null;
                            if (obj != null) {
                                mark3 = mark2;
                            }
                        }
                    }
                    if (timezone3.equals("")) {
                        timezone2 = timezone3;
                    } else {
                        if (timezone3.equals("")) {
                            if (timezone2.equals("")) {
                            }
                        }
                        timezone2 = str2;
                    }
                    instance4 = Calendar.getInstance();
                    stringBuffer = new StringBuffer();
                    stringBuffer5 = new StringBuffer();
                    if (year > 12) {
                        if (mark2.trim().equals("")) {
                            mark2 = "";
                        }
                    }
                    if (year == -1) {
                    }
                    if (month == -1) {
                    }
                    if (day == -1) {
                    }
                    stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                    if (mark2.equals("")) {
                        stringBuffer.append("HH").append(":mm:ss");
                    } else {
                        stringBuffer.append("hh").append(":mm:ss").append(" a");
                        stringBuffer5.append(Token.SEPARATOR).append(mark2);
                    }
                    if (timezone2.equals("")) {
                        stringBuffer.append(" Z");
                        stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                    }
                    instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                    date = new Date(j);
                    r6 = Calendar.getInstance();
                    r6.setTime(date);
                    if (second == -1) {
                        instance4.set(1, r6.get(1));
                    } else {
                        instance4.set(1, second);
                    }
                    if (r3 == -1) {
                        instance4.set(2, r6.get(2));
                    } else {
                        instance4.set(2, r3);
                    }
                    if (r4 == -1) {
                        instance4.set(5, r6.get(5));
                    } else {
                        instance4.set(5, r4);
                    }
                    arrayList.add(instance4.getTime());
                    instance = Calendar.getInstance();
                    stringBuffer6 = new StringBuffer();
                    stringBuffer7 = new StringBuffer();
                    mark3 = "";
                    if (clock2 == -1) {
                    }
                    if (minute2 == -1) {
                    }
                    if (second2 == -1) {
                    }
                    stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                    if (mark3.equals("")) {
                        stringBuffer6.append("HH").append(":mm:ss");
                    } else {
                        stringBuffer6.append("hh").append(":mm:ss").append(" a");
                        stringBuffer7.append(Token.SEPARATOR).append(mark3);
                    }
                    if (timezone2.equals("")) {
                        stringBuffer6.append(" Z");
                        stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                    }
                    instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                    if (i2 == -1) {
                        instance.set(1, r6.get(1));
                    } else {
                        instance.set(1, i2);
                    }
                    if (i == -1) {
                        instance.set(2, r6.get(2));
                    } else {
                        instance.set(2, i);
                    }
                    if (type == -1) {
                        instance.set(5, r6.get(5));
                    } else {
                        instance.set(5, type);
                    }
                    arrayList.add(instance.getTime());
                }
            }
            i = month2;
            i2 = second;
            second = type;
            if (satuts == 0) {
                type = day2;
                if (satuts2 == 0) {
                    str2 = "";
                    if (satuts == 0) {
                        timezone2 = str2;
                        instance4 = Calendar.getInstance();
                        stringBuffer = new StringBuffer();
                        stringBuffer5 = new StringBuffer();
                        if (year > 12) {
                            if (mark2.trim().equals("")) {
                                mark2 = "";
                            }
                        }
                        if (year == -1) {
                        }
                        if (month == -1) {
                        }
                        if (day == -1) {
                        }
                        stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                        if (mark2.equals("")) {
                            stringBuffer.append("hh").append(":mm:ss").append(" a");
                            stringBuffer5.append(Token.SEPARATOR).append(mark2);
                        } else {
                            stringBuffer.append("HH").append(":mm:ss");
                        }
                        if (timezone2.equals("")) {
                            stringBuffer.append(" Z");
                            stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                        }
                        instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                        date = new Date(j);
                        r6 = Calendar.getInstance();
                        r6.setTime(date);
                        if (second == -1) {
                            instance4.set(1, second);
                        } else {
                            instance4.set(1, r6.get(1));
                        }
                        if (r3 == -1) {
                            instance4.set(2, r3);
                        } else {
                            instance4.set(2, r6.get(2));
                        }
                        if (r4 == -1) {
                            instance4.set(5, r4);
                        } else {
                            instance4.set(5, r6.get(5));
                        }
                        arrayList.add(instance4.getTime());
                        instance = Calendar.getInstance();
                        stringBuffer6 = new StringBuffer();
                        stringBuffer7 = new StringBuffer();
                        mark3 = "";
                        if (clock2 == -1) {
                        }
                        if (minute2 == -1) {
                        }
                        if (second2 == -1) {
                        }
                        stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                        if (mark3.equals("")) {
                            stringBuffer6.append("hh").append(":mm:ss").append(" a");
                            stringBuffer7.append(Token.SEPARATOR).append(mark3);
                        } else {
                            stringBuffer6.append("HH").append(":mm:ss");
                        }
                        if (timezone2.equals("")) {
                            stringBuffer6.append(" Z");
                            stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                        }
                        instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                        if (i2 == -1) {
                            instance.set(1, i2);
                        } else {
                            instance.set(1, r6.get(1));
                        }
                        if (i == -1) {
                            instance.set(2, i);
                        } else {
                            instance.set(2, r6.get(2));
                        }
                        if (type == -1) {
                            instance.set(5, type);
                        } else {
                            instance.set(5, r6.get(5));
                        }
                        arrayList.add(instance.getTime());
                    }
                    if (mark2.trim().equals("")) {
                        if (mark3.trim().equals("")) {
                            if (second != i2) {
                                if (isMarkBefore) {
                                    mark2 = mark3;
                                }
                                if (timezone3.equals("")) {
                                    if (timezone3.equals("")) {
                                        if (timezone2.equals("")) {
                                        }
                                    }
                                    timezone2 = str2;
                                } else {
                                    timezone2 = timezone3;
                                }
                                instance4 = Calendar.getInstance();
                                stringBuffer = new StringBuffer();
                                stringBuffer5 = new StringBuffer();
                                if (year > 12) {
                                    if (mark2.trim().equals("")) {
                                        mark2 = "";
                                    }
                                }
                                if (year == -1) {
                                }
                                if (month == -1) {
                                }
                                if (day == -1) {
                                }
                                stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                                if (mark2.equals("")) {
                                    stringBuffer.append("HH").append(":mm:ss");
                                } else {
                                    stringBuffer.append("hh").append(":mm:ss").append(" a");
                                    stringBuffer5.append(Token.SEPARATOR).append(mark2);
                                }
                                if (timezone2.equals("")) {
                                    stringBuffer.append(" Z");
                                    stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                                }
                                instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                                date = new Date(j);
                                r6 = Calendar.getInstance();
                                r6.setTime(date);
                                if (second == -1) {
                                    instance4.set(1, r6.get(1));
                                } else {
                                    instance4.set(1, second);
                                }
                                if (r3 == -1) {
                                    instance4.set(2, r6.get(2));
                                } else {
                                    instance4.set(2, r3);
                                }
                                if (r4 == -1) {
                                    instance4.set(5, r6.get(5));
                                } else {
                                    instance4.set(5, r4);
                                }
                                arrayList.add(instance4.getTime());
                                instance = Calendar.getInstance();
                                stringBuffer6 = new StringBuffer();
                                stringBuffer7 = new StringBuffer();
                                mark3 = "";
                                if (clock2 == -1) {
                                }
                                if (minute2 == -1) {
                                }
                                if (second2 == -1) {
                                }
                                stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                                if (mark3.equals("")) {
                                    stringBuffer6.append("HH").append(":mm:ss");
                                } else {
                                    stringBuffer6.append("hh").append(":mm:ss").append(" a");
                                    stringBuffer7.append(Token.SEPARATOR).append(mark3);
                                }
                                if (timezone2.equals("")) {
                                    stringBuffer6.append(" Z");
                                    stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                                }
                                instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                                if (i2 == -1) {
                                    instance.set(1, r6.get(1));
                                } else {
                                    instance.set(1, i2);
                                }
                                if (i == -1) {
                                    instance.set(2, r6.get(2));
                                } else {
                                    instance.set(2, i);
                                }
                                if (type == -1) {
                                    instance.set(5, r6.get(5));
                                } else {
                                    instance.set(5, type);
                                }
                                arrayList.add(instance.getTime());
                            }
                            isMarkBefore = true;
                            if (isMarkBefore) {
                                mark2 = mark3;
                            }
                            if (timezone3.equals("")) {
                                timezone2 = timezone3;
                            } else {
                                if (timezone3.equals("")) {
                                    if (timezone2.equals("")) {
                                    }
                                }
                                timezone2 = str2;
                            }
                            instance4 = Calendar.getInstance();
                            stringBuffer = new StringBuffer();
                            stringBuffer5 = new StringBuffer();
                            if (year > 12) {
                                if (mark2.trim().equals("")) {
                                    mark2 = "";
                                }
                            }
                            if (year == -1) {
                            }
                            if (month == -1) {
                            }
                            if (day == -1) {
                            }
                            stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                            if (mark2.equals("")) {
                                stringBuffer.append("hh").append(":mm:ss").append(" a");
                                stringBuffer5.append(Token.SEPARATOR).append(mark2);
                            } else {
                                stringBuffer.append("HH").append(":mm:ss");
                            }
                            if (timezone2.equals("")) {
                                stringBuffer.append(" Z");
                                stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                            }
                            instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                            date = new Date(j);
                            r6 = Calendar.getInstance();
                            r6.setTime(date);
                            if (second == -1) {
                                instance4.set(1, second);
                            } else {
                                instance4.set(1, r6.get(1));
                            }
                            if (r3 == -1) {
                                instance4.set(2, r3);
                            } else {
                                instance4.set(2, r6.get(2));
                            }
                            if (r4 == -1) {
                                instance4.set(5, r4);
                            } else {
                                instance4.set(5, r6.get(5));
                            }
                            arrayList.add(instance4.getTime());
                            instance = Calendar.getInstance();
                            stringBuffer6 = new StringBuffer();
                            stringBuffer7 = new StringBuffer();
                            mark3 = "";
                            if (clock2 == -1) {
                            }
                            if (minute2 == -1) {
                            }
                            if (second2 == -1) {
                            }
                            stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                            if (mark3.equals("")) {
                                stringBuffer6.append("hh").append(":mm:ss").append(" a");
                                stringBuffer7.append(Token.SEPARATOR).append(mark3);
                            } else {
                                stringBuffer6.append("HH").append(":mm:ss");
                            }
                            if (timezone2.equals("")) {
                                stringBuffer6.append(" Z");
                                stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                            }
                            instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                            if (i2 == -1) {
                                instance.set(1, i2);
                            } else {
                                instance.set(1, r6.get(1));
                            }
                            if (i == -1) {
                                instance.set(2, i);
                            } else {
                                instance.set(2, r6.get(2));
                            }
                            if (type == -1) {
                                instance.set(5, type);
                            } else {
                                instance.set(5, r6.get(5));
                            }
                            arrayList.add(instance.getTime());
                        }
                    }
                    if (mark3.trim().equals("")) {
                        if (mark2.trim().equals("")) {
                            if (second == i2) {
                                obj = 1;
                                if (obj != null) {
                                    mark3 = mark2;
                                }
                            }
                            obj = null;
                            if (obj != null) {
                                mark3 = mark2;
                            }
                        }
                    }
                    if (timezone3.equals("")) {
                        if (timezone3.equals("")) {
                            if (timezone2.equals("")) {
                            }
                        }
                        timezone2 = str2;
                    } else {
                        timezone2 = timezone3;
                    }
                    instance4 = Calendar.getInstance();
                    stringBuffer = new StringBuffer();
                    stringBuffer5 = new StringBuffer();
                    if (year > 12) {
                        if (mark2.trim().equals("")) {
                            mark2 = "";
                        }
                    }
                    if (year == -1) {
                    }
                    if (month == -1) {
                    }
                    if (day == -1) {
                    }
                    stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                    if (mark2.equals("")) {
                        stringBuffer.append("HH").append(":mm:ss");
                    } else {
                        stringBuffer.append("hh").append(":mm:ss").append(" a");
                        stringBuffer5.append(Token.SEPARATOR).append(mark2);
                    }
                    if (timezone2.equals("")) {
                        stringBuffer.append(" Z");
                        stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                    }
                    instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                    date = new Date(j);
                    r6 = Calendar.getInstance();
                    r6.setTime(date);
                    if (second == -1) {
                        instance4.set(1, r6.get(1));
                    } else {
                        instance4.set(1, second);
                    }
                    if (r3 == -1) {
                        instance4.set(2, r6.get(2));
                    } else {
                        instance4.set(2, r3);
                    }
                    if (r4 == -1) {
                        instance4.set(5, r6.get(5));
                    } else {
                        instance4.set(5, r4);
                    }
                    arrayList.add(instance4.getTime());
                    instance = Calendar.getInstance();
                    stringBuffer6 = new StringBuffer();
                    stringBuffer7 = new StringBuffer();
                    mark3 = "";
                    if (clock2 == -1) {
                    }
                    if (minute2 == -1) {
                    }
                    if (second2 == -1) {
                    }
                    stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                    if (mark3.equals("")) {
                        stringBuffer6.append("HH").append(":mm:ss");
                    } else {
                        stringBuffer6.append("hh").append(":mm:ss").append(" a");
                        stringBuffer7.append(Token.SEPARATOR).append(mark3);
                    }
                    if (timezone2.equals("")) {
                        stringBuffer6.append(" Z");
                        stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                    }
                    instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                    if (i2 == -1) {
                        instance.set(1, r6.get(1));
                    } else {
                        instance.set(1, i2);
                    }
                    if (i == -1) {
                        instance.set(2, r6.get(2));
                    } else {
                        instance.set(2, i);
                    }
                    if (type == -1) {
                        instance.set(5, r6.get(5));
                    } else {
                        instance.set(5, type);
                    }
                    arrayList.add(instance.getTime());
                }
                if (satuts == 2) {
                    second = i2;
                    r3 = i;
                    r4 = type;
                }
                str2 = "";
                if (satuts == 0) {
                    timezone2 = str2;
                    instance4 = Calendar.getInstance();
                    stringBuffer = new StringBuffer();
                    stringBuffer5 = new StringBuffer();
                    if (year > 12) {
                        if (mark2.trim().equals("")) {
                            mark2 = "";
                        }
                    }
                    if (year == -1) {
                    }
                    if (month == -1) {
                    }
                    if (day == -1) {
                    }
                    stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                    if (mark2.equals("")) {
                        stringBuffer.append("hh").append(":mm:ss").append(" a");
                        stringBuffer5.append(Token.SEPARATOR).append(mark2);
                    } else {
                        stringBuffer.append("HH").append(":mm:ss");
                    }
                    if (timezone2.equals("")) {
                        stringBuffer.append(" Z");
                        stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                    }
                    instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                    date = new Date(j);
                    r6 = Calendar.getInstance();
                    r6.setTime(date);
                    if (second == -1) {
                        instance4.set(1, second);
                    } else {
                        instance4.set(1, r6.get(1));
                    }
                    if (r3 == -1) {
                        instance4.set(2, r3);
                    } else {
                        instance4.set(2, r6.get(2));
                    }
                    if (r4 == -1) {
                        instance4.set(5, r4);
                    } else {
                        instance4.set(5, r6.get(5));
                    }
                    arrayList.add(instance4.getTime());
                    instance = Calendar.getInstance();
                    stringBuffer6 = new StringBuffer();
                    stringBuffer7 = new StringBuffer();
                    mark3 = "";
                    if (clock2 == -1) {
                    }
                    if (minute2 == -1) {
                    }
                    if (second2 == -1) {
                    }
                    stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                    if (mark3.equals("")) {
                        stringBuffer6.append("hh").append(":mm:ss").append(" a");
                        stringBuffer7.append(Token.SEPARATOR).append(mark3);
                    } else {
                        stringBuffer6.append("HH").append(":mm:ss");
                    }
                    if (timezone2.equals("")) {
                        stringBuffer6.append(" Z");
                        stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                    }
                    instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                    if (i2 == -1) {
                        instance.set(1, i2);
                    } else {
                        instance.set(1, r6.get(1));
                    }
                    if (i == -1) {
                        instance.set(2, i);
                    } else {
                        instance.set(2, r6.get(2));
                    }
                    if (type == -1) {
                        instance.set(5, type);
                    } else {
                        instance.set(5, r6.get(5));
                    }
                    arrayList.add(instance.getTime());
                }
                if (mark2.trim().equals("")) {
                    if (mark3.trim().equals("")) {
                        if (second != i2) {
                            if (isMarkBefore) {
                                mark2 = mark3;
                            }
                            if (timezone3.equals("")) {
                                timezone2 = timezone3;
                            } else {
                                if (timezone3.equals("")) {
                                    if (timezone2.equals("")) {
                                    }
                                }
                                timezone2 = str2;
                            }
                            instance4 = Calendar.getInstance();
                            stringBuffer = new StringBuffer();
                            stringBuffer5 = new StringBuffer();
                            if (year > 12) {
                                if (mark2.trim().equals("")) {
                                    mark2 = "";
                                }
                            }
                            if (year == -1) {
                            }
                            if (month == -1) {
                            }
                            if (day == -1) {
                            }
                            stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                            if (mark2.equals("")) {
                                stringBuffer.append("HH").append(":mm:ss");
                            } else {
                                stringBuffer.append("hh").append(":mm:ss").append(" a");
                                stringBuffer5.append(Token.SEPARATOR).append(mark2);
                            }
                            if (timezone2.equals("")) {
                                stringBuffer.append(" Z");
                                stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                            }
                            instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                            date = new Date(j);
                            r6 = Calendar.getInstance();
                            r6.setTime(date);
                            if (second == -1) {
                                instance4.set(1, r6.get(1));
                            } else {
                                instance4.set(1, second);
                            }
                            if (r3 == -1) {
                                instance4.set(2, r6.get(2));
                            } else {
                                instance4.set(2, r3);
                            }
                            if (r4 == -1) {
                                instance4.set(5, r6.get(5));
                            } else {
                                instance4.set(5, r4);
                            }
                            arrayList.add(instance4.getTime());
                            instance = Calendar.getInstance();
                            stringBuffer6 = new StringBuffer();
                            stringBuffer7 = new StringBuffer();
                            mark3 = "";
                            if (clock2 == -1) {
                            }
                            if (minute2 == -1) {
                            }
                            if (second2 == -1) {
                            }
                            stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                            if (mark3.equals("")) {
                                stringBuffer6.append("HH").append(":mm:ss");
                            } else {
                                stringBuffer6.append("hh").append(":mm:ss").append(" a");
                                stringBuffer7.append(Token.SEPARATOR).append(mark3);
                            }
                            if (timezone2.equals("")) {
                                stringBuffer6.append(" Z");
                                stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                            }
                            instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                            if (i2 == -1) {
                                instance.set(1, r6.get(1));
                            } else {
                                instance.set(1, i2);
                            }
                            if (i == -1) {
                                instance.set(2, r6.get(2));
                            } else {
                                instance.set(2, i);
                            }
                            if (type == -1) {
                                instance.set(5, r6.get(5));
                            } else {
                                instance.set(5, type);
                            }
                            arrayList.add(instance.getTime());
                        }
                        isMarkBefore = true;
                        if (isMarkBefore) {
                            mark2 = mark3;
                        }
                        if (timezone3.equals("")) {
                            if (timezone3.equals("")) {
                                if (timezone2.equals("")) {
                                }
                            }
                            timezone2 = str2;
                        } else {
                            timezone2 = timezone3;
                        }
                        instance4 = Calendar.getInstance();
                        stringBuffer = new StringBuffer();
                        stringBuffer5 = new StringBuffer();
                        if (year > 12) {
                            if (mark2.trim().equals("")) {
                                mark2 = "";
                            }
                        }
                        if (year == -1) {
                        }
                        if (month == -1) {
                        }
                        if (day == -1) {
                        }
                        stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                        if (mark2.equals("")) {
                            stringBuffer.append("hh").append(":mm:ss").append(" a");
                            stringBuffer5.append(Token.SEPARATOR).append(mark2);
                        } else {
                            stringBuffer.append("HH").append(":mm:ss");
                        }
                        if (timezone2.equals("")) {
                            stringBuffer.append(" Z");
                            stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                        }
                        instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                        date = new Date(j);
                        r6 = Calendar.getInstance();
                        r6.setTime(date);
                        if (second == -1) {
                            instance4.set(1, second);
                        } else {
                            instance4.set(1, r6.get(1));
                        }
                        if (r3 == -1) {
                            instance4.set(2, r3);
                        } else {
                            instance4.set(2, r6.get(2));
                        }
                        if (r4 == -1) {
                            instance4.set(5, r4);
                        } else {
                            instance4.set(5, r6.get(5));
                        }
                        arrayList.add(instance4.getTime());
                        instance = Calendar.getInstance();
                        stringBuffer6 = new StringBuffer();
                        stringBuffer7 = new StringBuffer();
                        mark3 = "";
                        if (clock2 == -1) {
                        }
                        if (minute2 == -1) {
                        }
                        if (second2 == -1) {
                        }
                        stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                        if (mark3.equals("")) {
                            stringBuffer6.append("hh").append(":mm:ss").append(" a");
                            stringBuffer7.append(Token.SEPARATOR).append(mark3);
                        } else {
                            stringBuffer6.append("HH").append(":mm:ss");
                        }
                        if (timezone2.equals("")) {
                            stringBuffer6.append(" Z");
                            stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                        }
                        instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                        if (i2 == -1) {
                            instance.set(1, i2);
                        } else {
                            instance.set(1, r6.get(1));
                        }
                        if (i == -1) {
                            instance.set(2, i);
                        } else {
                            instance.set(2, r6.get(2));
                        }
                        if (type == -1) {
                            instance.set(5, type);
                        } else {
                            instance.set(5, r6.get(5));
                        }
                        arrayList.add(instance.getTime());
                    }
                }
                if (mark3.trim().equals("")) {
                    if (mark2.trim().equals("")) {
                        if (second == i2) {
                            obj = 1;
                            if (obj != null) {
                                mark3 = mark2;
                            }
                        }
                        obj = null;
                        if (obj != null) {
                            mark3 = mark2;
                        }
                    }
                }
                if (timezone3.equals("")) {
                    timezone2 = timezone3;
                } else {
                    if (timezone3.equals("")) {
                        if (timezone2.equals("")) {
                        }
                    }
                    timezone2 = str2;
                }
                instance4 = Calendar.getInstance();
                stringBuffer = new StringBuffer();
                stringBuffer5 = new StringBuffer();
                if (year > 12) {
                    if (mark2.trim().equals("")) {
                        mark2 = "";
                    }
                }
                if (year == -1) {
                }
                if (month == -1) {
                }
                if (day == -1) {
                }
                stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                if (mark2.equals("")) {
                    stringBuffer.append("HH").append(":mm:ss");
                } else {
                    stringBuffer.append("hh").append(":mm:ss").append(" a");
                    stringBuffer5.append(Token.SEPARATOR).append(mark2);
                }
                if (timezone2.equals("")) {
                    stringBuffer.append(" Z");
                    stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                }
                instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                date = new Date(j);
                r6 = Calendar.getInstance();
                r6.setTime(date);
                if (second == -1) {
                    instance4.set(1, r6.get(1));
                } else {
                    instance4.set(1, second);
                }
                if (r3 == -1) {
                    instance4.set(2, r6.get(2));
                } else {
                    instance4.set(2, r3);
                }
                if (r4 == -1) {
                    instance4.set(5, r6.get(5));
                } else {
                    instance4.set(5, r4);
                }
                arrayList.add(instance4.getTime());
                instance = Calendar.getInstance();
                stringBuffer6 = new StringBuffer();
                stringBuffer7 = new StringBuffer();
                mark3 = "";
                if (clock2 == -1) {
                }
                if (minute2 == -1) {
                }
                if (second2 == -1) {
                }
                stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                if (mark3.equals("")) {
                    stringBuffer6.append("HH").append(":mm:ss");
                } else {
                    stringBuffer6.append("hh").append(":mm:ss").append(" a");
                    stringBuffer7.append(Token.SEPARATOR).append(mark3);
                }
                if (timezone2.equals("")) {
                    stringBuffer6.append(" Z");
                    stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                }
                instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                if (i2 == -1) {
                    instance.set(1, r6.get(1));
                } else {
                    instance.set(1, i2);
                }
                if (i == -1) {
                    instance.set(2, r6.get(2));
                } else {
                    instance.set(2, i);
                }
                if (type == -1) {
                    instance.set(5, r6.get(5));
                } else {
                    instance.set(5, type);
                }
                arrayList.add(instance.getTime());
            }
            if (satuts2 == 2) {
                i2 = second;
                i = r3;
                if (r4 != -1) {
                    type = r4;
                    if (satuts2 == 0) {
                        str2 = "";
                        if (satuts == 0) {
                            timezone2 = str2;
                            instance4 = Calendar.getInstance();
                            stringBuffer = new StringBuffer();
                            stringBuffer5 = new StringBuffer();
                            if (year > 12) {
                                if (mark2.trim().equals("")) {
                                    mark2 = "";
                                }
                            }
                            if (year == -1) {
                            }
                            if (month == -1) {
                            }
                            if (day == -1) {
                            }
                            stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                            if (mark2.equals("")) {
                                stringBuffer.append("hh").append(":mm:ss").append(" a");
                                stringBuffer5.append(Token.SEPARATOR).append(mark2);
                            } else {
                                stringBuffer.append("HH").append(":mm:ss");
                            }
                            if (timezone2.equals("")) {
                                stringBuffer.append(" Z");
                                stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                            }
                            instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                            date = new Date(j);
                            r6 = Calendar.getInstance();
                            r6.setTime(date);
                            if (second == -1) {
                                instance4.set(1, second);
                            } else {
                                instance4.set(1, r6.get(1));
                            }
                            if (r3 == -1) {
                                instance4.set(2, r3);
                            } else {
                                instance4.set(2, r6.get(2));
                            }
                            if (r4 == -1) {
                                instance4.set(5, r4);
                            } else {
                                instance4.set(5, r6.get(5));
                            }
                            arrayList.add(instance4.getTime());
                            instance = Calendar.getInstance();
                            stringBuffer6 = new StringBuffer();
                            stringBuffer7 = new StringBuffer();
                            mark3 = "";
                            if (clock2 == -1) {
                            }
                            if (minute2 == -1) {
                            }
                            if (second2 == -1) {
                            }
                            stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                            if (mark3.equals("")) {
                                stringBuffer6.append("hh").append(":mm:ss").append(" a");
                                stringBuffer7.append(Token.SEPARATOR).append(mark3);
                            } else {
                                stringBuffer6.append("HH").append(":mm:ss");
                            }
                            if (timezone2.equals("")) {
                                stringBuffer6.append(" Z");
                                stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                            }
                            instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                            if (i2 == -1) {
                                instance.set(1, i2);
                            } else {
                                instance.set(1, r6.get(1));
                            }
                            if (i == -1) {
                                instance.set(2, i);
                            } else {
                                instance.set(2, r6.get(2));
                            }
                            if (type == -1) {
                                instance.set(5, type);
                            } else {
                                instance.set(5, r6.get(5));
                            }
                            arrayList.add(instance.getTime());
                        }
                        if (mark2.trim().equals("")) {
                            if (mark3.trim().equals("")) {
                                if (second != i2) {
                                    if (isMarkBefore) {
                                        mark2 = mark3;
                                    }
                                    if (timezone3.equals("")) {
                                        if (timezone3.equals("")) {
                                            if (timezone2.equals("")) {
                                            }
                                        }
                                        timezone2 = str2;
                                    } else {
                                        timezone2 = timezone3;
                                    }
                                    instance4 = Calendar.getInstance();
                                    stringBuffer = new StringBuffer();
                                    stringBuffer5 = new StringBuffer();
                                    if (year > 12) {
                                        if (mark2.trim().equals("")) {
                                            mark2 = "";
                                        }
                                    }
                                    if (year == -1) {
                                    }
                                    if (month == -1) {
                                    }
                                    if (day == -1) {
                                    }
                                    stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                                    if (mark2.equals("")) {
                                        stringBuffer.append("HH").append(":mm:ss");
                                    } else {
                                        stringBuffer.append("hh").append(":mm:ss").append(" a");
                                        stringBuffer5.append(Token.SEPARATOR).append(mark2);
                                    }
                                    if (timezone2.equals("")) {
                                        stringBuffer.append(" Z");
                                        stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                                    }
                                    instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                                    date = new Date(j);
                                    r6 = Calendar.getInstance();
                                    r6.setTime(date);
                                    if (second == -1) {
                                        instance4.set(1, r6.get(1));
                                    } else {
                                        instance4.set(1, second);
                                    }
                                    if (r3 == -1) {
                                        instance4.set(2, r6.get(2));
                                    } else {
                                        instance4.set(2, r3);
                                    }
                                    if (r4 == -1) {
                                        instance4.set(5, r6.get(5));
                                    } else {
                                        instance4.set(5, r4);
                                    }
                                    arrayList.add(instance4.getTime());
                                    instance = Calendar.getInstance();
                                    stringBuffer6 = new StringBuffer();
                                    stringBuffer7 = new StringBuffer();
                                    mark3 = "";
                                    if (clock2 == -1) {
                                    }
                                    if (minute2 == -1) {
                                    }
                                    if (second2 == -1) {
                                    }
                                    stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                                    if (mark3.equals("")) {
                                        stringBuffer6.append("HH").append(":mm:ss");
                                    } else {
                                        stringBuffer6.append("hh").append(":mm:ss").append(" a");
                                        stringBuffer7.append(Token.SEPARATOR).append(mark3);
                                    }
                                    if (timezone2.equals("")) {
                                        stringBuffer6.append(" Z");
                                        stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                                    }
                                    instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                                    if (i2 == -1) {
                                        instance.set(1, r6.get(1));
                                    } else {
                                        instance.set(1, i2);
                                    }
                                    if (i == -1) {
                                        instance.set(2, r6.get(2));
                                    } else {
                                        instance.set(2, i);
                                    }
                                    if (type == -1) {
                                        instance.set(5, r6.get(5));
                                    } else {
                                        instance.set(5, type);
                                    }
                                    arrayList.add(instance.getTime());
                                }
                                isMarkBefore = true;
                                if (isMarkBefore) {
                                    mark2 = mark3;
                                }
                                if (timezone3.equals("")) {
                                    timezone2 = timezone3;
                                } else {
                                    if (timezone3.equals("")) {
                                        if (timezone2.equals("")) {
                                        }
                                    }
                                    timezone2 = str2;
                                }
                                instance4 = Calendar.getInstance();
                                stringBuffer = new StringBuffer();
                                stringBuffer5 = new StringBuffer();
                                if (year > 12) {
                                    if (mark2.trim().equals("")) {
                                        mark2 = "";
                                    }
                                }
                                if (year == -1) {
                                }
                                if (month == -1) {
                                }
                                if (day == -1) {
                                }
                                stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                                if (mark2.equals("")) {
                                    stringBuffer.append("hh").append(":mm:ss").append(" a");
                                    stringBuffer5.append(Token.SEPARATOR).append(mark2);
                                } else {
                                    stringBuffer.append("HH").append(":mm:ss");
                                }
                                if (timezone2.equals("")) {
                                    stringBuffer.append(" Z");
                                    stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                                }
                                instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                                date = new Date(j);
                                r6 = Calendar.getInstance();
                                r6.setTime(date);
                                if (second == -1) {
                                    instance4.set(1, second);
                                } else {
                                    instance4.set(1, r6.get(1));
                                }
                                if (r3 == -1) {
                                    instance4.set(2, r3);
                                } else {
                                    instance4.set(2, r6.get(2));
                                }
                                if (r4 == -1) {
                                    instance4.set(5, r4);
                                } else {
                                    instance4.set(5, r6.get(5));
                                }
                                arrayList.add(instance4.getTime());
                                instance = Calendar.getInstance();
                                stringBuffer6 = new StringBuffer();
                                stringBuffer7 = new StringBuffer();
                                mark3 = "";
                                if (clock2 == -1) {
                                }
                                if (minute2 == -1) {
                                }
                                if (second2 == -1) {
                                }
                                stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                                if (mark3.equals("")) {
                                    stringBuffer6.append("hh").append(":mm:ss").append(" a");
                                    stringBuffer7.append(Token.SEPARATOR).append(mark3);
                                } else {
                                    stringBuffer6.append("HH").append(":mm:ss");
                                }
                                if (timezone2.equals("")) {
                                    stringBuffer6.append(" Z");
                                    stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                                }
                                instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                                if (i2 == -1) {
                                    instance.set(1, i2);
                                } else {
                                    instance.set(1, r6.get(1));
                                }
                                if (i == -1) {
                                    instance.set(2, i);
                                } else {
                                    instance.set(2, r6.get(2));
                                }
                                if (type == -1) {
                                    instance.set(5, type);
                                } else {
                                    instance.set(5, r6.get(5));
                                }
                                arrayList.add(instance.getTime());
                            }
                        }
                        if (mark3.trim().equals("")) {
                            if (mark2.trim().equals("")) {
                                if (second == i2) {
                                    obj = 1;
                                    if (obj != null) {
                                        mark3 = mark2;
                                    }
                                }
                                obj = null;
                                if (obj != null) {
                                    mark3 = mark2;
                                }
                            }
                        }
                        if (timezone3.equals("")) {
                            if (timezone3.equals("")) {
                                if (timezone2.equals("")) {
                                }
                            }
                            timezone2 = str2;
                        } else {
                            timezone2 = timezone3;
                        }
                        instance4 = Calendar.getInstance();
                        stringBuffer = new StringBuffer();
                        stringBuffer5 = new StringBuffer();
                        if (year > 12) {
                            if (mark2.trim().equals("")) {
                                mark2 = "";
                            }
                        }
                        if (year == -1) {
                        }
                        if (month == -1) {
                        }
                        if (day == -1) {
                        }
                        stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                        if (mark2.equals("")) {
                            stringBuffer.append("HH").append(":mm:ss");
                        } else {
                            stringBuffer.append("hh").append(":mm:ss").append(" a");
                            stringBuffer5.append(Token.SEPARATOR).append(mark2);
                        }
                        if (timezone2.equals("")) {
                            stringBuffer.append(" Z");
                            stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                        }
                        instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                        date = new Date(j);
                        r6 = Calendar.getInstance();
                        r6.setTime(date);
                        if (second == -1) {
                            instance4.set(1, r6.get(1));
                        } else {
                            instance4.set(1, second);
                        }
                        if (r3 == -1) {
                            instance4.set(2, r6.get(2));
                        } else {
                            instance4.set(2, r3);
                        }
                        if (r4 == -1) {
                            instance4.set(5, r6.get(5));
                        } else {
                            instance4.set(5, r4);
                        }
                        arrayList.add(instance4.getTime());
                        instance = Calendar.getInstance();
                        stringBuffer6 = new StringBuffer();
                        stringBuffer7 = new StringBuffer();
                        mark3 = "";
                        if (clock2 == -1) {
                        }
                        if (minute2 == -1) {
                        }
                        if (second2 == -1) {
                        }
                        stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                        if (mark3.equals("")) {
                            stringBuffer6.append("HH").append(":mm:ss");
                        } else {
                            stringBuffer6.append("hh").append(":mm:ss").append(" a");
                            stringBuffer7.append(Token.SEPARATOR).append(mark3);
                        }
                        if (timezone2.equals("")) {
                            stringBuffer6.append(" Z");
                            stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                        }
                        instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                        if (i2 == -1) {
                            instance.set(1, r6.get(1));
                        } else {
                            instance.set(1, i2);
                        }
                        if (i == -1) {
                            instance.set(2, r6.get(2));
                        } else {
                            instance.set(2, i);
                        }
                        if (type == -1) {
                            instance.set(5, r6.get(5));
                        } else {
                            instance.set(5, type);
                        }
                        arrayList.add(instance.getTime());
                    }
                    if (satuts == 2) {
                        second = i2;
                        r3 = i;
                        r4 = type;
                    }
                    str2 = "";
                    if (satuts == 0) {
                        timezone2 = str2;
                        instance4 = Calendar.getInstance();
                        stringBuffer = new StringBuffer();
                        stringBuffer5 = new StringBuffer();
                        if (year > 12) {
                            if (mark2.trim().equals("")) {
                                mark2 = "";
                            }
                        }
                        if (year == -1) {
                        }
                        if (month == -1) {
                        }
                        if (day == -1) {
                        }
                        stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                        if (mark2.equals("")) {
                            stringBuffer.append("hh").append(":mm:ss").append(" a");
                            stringBuffer5.append(Token.SEPARATOR).append(mark2);
                        } else {
                            stringBuffer.append("HH").append(":mm:ss");
                        }
                        if (timezone2.equals("")) {
                            stringBuffer.append(" Z");
                            stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                        }
                        instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                        date = new Date(j);
                        r6 = Calendar.getInstance();
                        r6.setTime(date);
                        if (second == -1) {
                            instance4.set(1, second);
                        } else {
                            instance4.set(1, r6.get(1));
                        }
                        if (r3 == -1) {
                            instance4.set(2, r3);
                        } else {
                            instance4.set(2, r6.get(2));
                        }
                        if (r4 == -1) {
                            instance4.set(5, r4);
                        } else {
                            instance4.set(5, r6.get(5));
                        }
                        arrayList.add(instance4.getTime());
                        instance = Calendar.getInstance();
                        stringBuffer6 = new StringBuffer();
                        stringBuffer7 = new StringBuffer();
                        mark3 = "";
                        if (clock2 == -1) {
                        }
                        if (minute2 == -1) {
                        }
                        if (second2 == -1) {
                        }
                        stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                        if (mark3.equals("")) {
                            stringBuffer6.append("hh").append(":mm:ss").append(" a");
                            stringBuffer7.append(Token.SEPARATOR).append(mark3);
                        } else {
                            stringBuffer6.append("HH").append(":mm:ss");
                        }
                        if (timezone2.equals("")) {
                            stringBuffer6.append(" Z");
                            stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                        }
                        instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                        if (i2 == -1) {
                            instance.set(1, i2);
                        } else {
                            instance.set(1, r6.get(1));
                        }
                        if (i == -1) {
                            instance.set(2, i);
                        } else {
                            instance.set(2, r6.get(2));
                        }
                        if (type == -1) {
                            instance.set(5, type);
                        } else {
                            instance.set(5, r6.get(5));
                        }
                        arrayList.add(instance.getTime());
                    }
                    if (mark2.trim().equals("")) {
                        if (mark3.trim().equals("")) {
                            if (second != i2) {
                                if (isMarkBefore) {
                                    mark2 = mark3;
                                }
                                if (timezone3.equals("")) {
                                    timezone2 = timezone3;
                                } else {
                                    if (timezone3.equals("")) {
                                        if (timezone2.equals("")) {
                                        }
                                    }
                                    timezone2 = str2;
                                }
                                instance4 = Calendar.getInstance();
                                stringBuffer = new StringBuffer();
                                stringBuffer5 = new StringBuffer();
                                if (year > 12) {
                                    if (mark2.trim().equals("")) {
                                        mark2 = "";
                                    }
                                }
                                if (year == -1) {
                                }
                                if (month == -1) {
                                }
                                if (day == -1) {
                                }
                                stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                                if (mark2.equals("")) {
                                    stringBuffer.append("HH").append(":mm:ss");
                                } else {
                                    stringBuffer.append("hh").append(":mm:ss").append(" a");
                                    stringBuffer5.append(Token.SEPARATOR).append(mark2);
                                }
                                if (timezone2.equals("")) {
                                    stringBuffer.append(" Z");
                                    stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                                }
                                instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                                date = new Date(j);
                                r6 = Calendar.getInstance();
                                r6.setTime(date);
                                if (second == -1) {
                                    instance4.set(1, r6.get(1));
                                } else {
                                    instance4.set(1, second);
                                }
                                if (r3 == -1) {
                                    instance4.set(2, r6.get(2));
                                } else {
                                    instance4.set(2, r3);
                                }
                                if (r4 == -1) {
                                    instance4.set(5, r6.get(5));
                                } else {
                                    instance4.set(5, r4);
                                }
                                arrayList.add(instance4.getTime());
                                instance = Calendar.getInstance();
                                stringBuffer6 = new StringBuffer();
                                stringBuffer7 = new StringBuffer();
                                mark3 = "";
                                if (clock2 == -1) {
                                }
                                if (minute2 == -1) {
                                }
                                if (second2 == -1) {
                                }
                                stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                                if (mark3.equals("")) {
                                    stringBuffer6.append("HH").append(":mm:ss");
                                } else {
                                    stringBuffer6.append("hh").append(":mm:ss").append(" a");
                                    stringBuffer7.append(Token.SEPARATOR).append(mark3);
                                }
                                if (timezone2.equals("")) {
                                    stringBuffer6.append(" Z");
                                    stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                                }
                                instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                                if (i2 == -1) {
                                    instance.set(1, r6.get(1));
                                } else {
                                    instance.set(1, i2);
                                }
                                if (i == -1) {
                                    instance.set(2, r6.get(2));
                                } else {
                                    instance.set(2, i);
                                }
                                if (type == -1) {
                                    instance.set(5, r6.get(5));
                                } else {
                                    instance.set(5, type);
                                }
                                arrayList.add(instance.getTime());
                            }
                            isMarkBefore = true;
                            if (isMarkBefore) {
                                mark2 = mark3;
                            }
                            if (timezone3.equals("")) {
                                if (timezone3.equals("")) {
                                    if (timezone2.equals("")) {
                                    }
                                }
                                timezone2 = str2;
                            } else {
                                timezone2 = timezone3;
                            }
                            instance4 = Calendar.getInstance();
                            stringBuffer = new StringBuffer();
                            stringBuffer5 = new StringBuffer();
                            if (year > 12) {
                                if (mark2.trim().equals("")) {
                                    mark2 = "";
                                }
                            }
                            if (year == -1) {
                            }
                            if (month == -1) {
                            }
                            if (day == -1) {
                            }
                            stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                            if (mark2.equals("")) {
                                stringBuffer.append("hh").append(":mm:ss").append(" a");
                                stringBuffer5.append(Token.SEPARATOR).append(mark2);
                            } else {
                                stringBuffer.append("HH").append(":mm:ss");
                            }
                            if (timezone2.equals("")) {
                                stringBuffer.append(" Z");
                                stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                            }
                            instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                            date = new Date(j);
                            r6 = Calendar.getInstance();
                            r6.setTime(date);
                            if (second == -1) {
                                instance4.set(1, second);
                            } else {
                                instance4.set(1, r6.get(1));
                            }
                            if (r3 == -1) {
                                instance4.set(2, r3);
                            } else {
                                instance4.set(2, r6.get(2));
                            }
                            if (r4 == -1) {
                                instance4.set(5, r4);
                            } else {
                                instance4.set(5, r6.get(5));
                            }
                            arrayList.add(instance4.getTime());
                            instance = Calendar.getInstance();
                            stringBuffer6 = new StringBuffer();
                            stringBuffer7 = new StringBuffer();
                            mark3 = "";
                            if (clock2 == -1) {
                            }
                            if (minute2 == -1) {
                            }
                            if (second2 == -1) {
                            }
                            stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                            if (mark3.equals("")) {
                                stringBuffer6.append("hh").append(":mm:ss").append(" a");
                                stringBuffer7.append(Token.SEPARATOR).append(mark3);
                            } else {
                                stringBuffer6.append("HH").append(":mm:ss");
                            }
                            if (timezone2.equals("")) {
                                stringBuffer6.append(" Z");
                                stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                            }
                            instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                            if (i2 == -1) {
                                instance.set(1, i2);
                            } else {
                                instance.set(1, r6.get(1));
                            }
                            if (i == -1) {
                                instance.set(2, i);
                            } else {
                                instance.set(2, r6.get(2));
                            }
                            if (type == -1) {
                                instance.set(5, type);
                            } else {
                                instance.set(5, r6.get(5));
                            }
                            arrayList.add(instance.getTime());
                        }
                    }
                    if (mark3.trim().equals("")) {
                        if (mark2.trim().equals("")) {
                            if (second == i2) {
                                obj = 1;
                                if (obj != null) {
                                    mark3 = mark2;
                                }
                            }
                            obj = null;
                            if (obj != null) {
                                mark3 = mark2;
                            }
                        }
                    }
                    if (timezone3.equals("")) {
                        timezone2 = timezone3;
                    } else {
                        if (timezone3.equals("")) {
                            if (timezone2.equals("")) {
                            }
                        }
                        timezone2 = str2;
                    }
                    instance4 = Calendar.getInstance();
                    stringBuffer = new StringBuffer();
                    stringBuffer5 = new StringBuffer();
                    if (year > 12) {
                        if (mark2.trim().equals("")) {
                            mark2 = "";
                        }
                    }
                    if (year == -1) {
                    }
                    if (month == -1) {
                    }
                    if (day == -1) {
                    }
                    stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                    if (mark2.equals("")) {
                        stringBuffer.append("HH").append(":mm:ss");
                    } else {
                        stringBuffer.append("hh").append(":mm:ss").append(" a");
                        stringBuffer5.append(Token.SEPARATOR).append(mark2);
                    }
                    if (timezone2.equals("")) {
                        stringBuffer.append(" Z");
                        stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                    }
                    instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                    date = new Date(j);
                    r6 = Calendar.getInstance();
                    r6.setTime(date);
                    if (second == -1) {
                        instance4.set(1, r6.get(1));
                    } else {
                        instance4.set(1, second);
                    }
                    if (r3 == -1) {
                        instance4.set(2, r6.get(2));
                    } else {
                        instance4.set(2, r3);
                    }
                    if (r4 == -1) {
                        instance4.set(5, r6.get(5));
                    } else {
                        instance4.set(5, r4);
                    }
                    arrayList.add(instance4.getTime());
                    instance = Calendar.getInstance();
                    stringBuffer6 = new StringBuffer();
                    stringBuffer7 = new StringBuffer();
                    mark3 = "";
                    if (clock2 == -1) {
                    }
                    if (minute2 == -1) {
                    }
                    if (second2 == -1) {
                    }
                    stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                    if (mark3.equals("")) {
                        stringBuffer6.append("HH").append(":mm:ss");
                    } else {
                        stringBuffer6.append("hh").append(":mm:ss").append(" a");
                        stringBuffer7.append(Token.SEPARATOR).append(mark3);
                    }
                    if (timezone2.equals("")) {
                        stringBuffer6.append(" Z");
                        stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                    }
                    instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                    if (i2 == -1) {
                        instance.set(1, r6.get(1));
                    } else {
                        instance.set(1, i2);
                    }
                    if (i == -1) {
                        instance.set(2, r6.get(2));
                    } else {
                        instance.set(2, i);
                    }
                    if (type == -1) {
                        instance.set(5, r6.get(5));
                    } else {
                        instance.set(5, type);
                    }
                    arrayList.add(instance.getTime());
                }
                type = day2;
                if (satuts2 == 0) {
                    str2 = "";
                    if (satuts == 0) {
                        timezone2 = str2;
                        instance4 = Calendar.getInstance();
                        stringBuffer = new StringBuffer();
                        stringBuffer5 = new StringBuffer();
                        if (year > 12) {
                            if (mark2.trim().equals("")) {
                                mark2 = "";
                            }
                        }
                        if (year == -1) {
                        }
                        if (month == -1) {
                        }
                        if (day == -1) {
                        }
                        stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                        if (mark2.equals("")) {
                            stringBuffer.append("hh").append(":mm:ss").append(" a");
                            stringBuffer5.append(Token.SEPARATOR).append(mark2);
                        } else {
                            stringBuffer.append("HH").append(":mm:ss");
                        }
                        if (timezone2.equals("")) {
                            stringBuffer.append(" Z");
                            stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                        }
                        instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                        date = new Date(j);
                        r6 = Calendar.getInstance();
                        r6.setTime(date);
                        if (second == -1) {
                            instance4.set(1, second);
                        } else {
                            instance4.set(1, r6.get(1));
                        }
                        if (r3 == -1) {
                            instance4.set(2, r3);
                        } else {
                            instance4.set(2, r6.get(2));
                        }
                        if (r4 == -1) {
                            instance4.set(5, r4);
                        } else {
                            instance4.set(5, r6.get(5));
                        }
                        arrayList.add(instance4.getTime());
                        instance = Calendar.getInstance();
                        stringBuffer6 = new StringBuffer();
                        stringBuffer7 = new StringBuffer();
                        mark3 = "";
                        if (clock2 == -1) {
                        }
                        if (minute2 == -1) {
                        }
                        if (second2 == -1) {
                        }
                        stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                        if (mark3.equals("")) {
                            stringBuffer6.append("hh").append(":mm:ss").append(" a");
                            stringBuffer7.append(Token.SEPARATOR).append(mark3);
                        } else {
                            stringBuffer6.append("HH").append(":mm:ss");
                        }
                        if (timezone2.equals("")) {
                            stringBuffer6.append(" Z");
                            stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                        }
                        instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                        if (i2 == -1) {
                            instance.set(1, i2);
                        } else {
                            instance.set(1, r6.get(1));
                        }
                        if (i == -1) {
                            instance.set(2, i);
                        } else {
                            instance.set(2, r6.get(2));
                        }
                        if (type == -1) {
                            instance.set(5, type);
                        } else {
                            instance.set(5, r6.get(5));
                        }
                        arrayList.add(instance.getTime());
                    }
                    if (mark2.trim().equals("")) {
                        if (mark3.trim().equals("")) {
                            if (second != i2) {
                                if (isMarkBefore) {
                                    mark2 = mark3;
                                }
                                if (timezone3.equals("")) {
                                    if (timezone3.equals("")) {
                                        if (timezone2.equals("")) {
                                        }
                                    }
                                    timezone2 = str2;
                                } else {
                                    timezone2 = timezone3;
                                }
                                instance4 = Calendar.getInstance();
                                stringBuffer = new StringBuffer();
                                stringBuffer5 = new StringBuffer();
                                if (year > 12) {
                                    if (mark2.trim().equals("")) {
                                        mark2 = "";
                                    }
                                }
                                if (year == -1) {
                                }
                                if (month == -1) {
                                }
                                if (day == -1) {
                                }
                                stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                                if (mark2.equals("")) {
                                    stringBuffer.append("HH").append(":mm:ss");
                                } else {
                                    stringBuffer.append("hh").append(":mm:ss").append(" a");
                                    stringBuffer5.append(Token.SEPARATOR).append(mark2);
                                }
                                if (timezone2.equals("")) {
                                    stringBuffer.append(" Z");
                                    stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                                }
                                instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                                date = new Date(j);
                                r6 = Calendar.getInstance();
                                r6.setTime(date);
                                if (second == -1) {
                                    instance4.set(1, r6.get(1));
                                } else {
                                    instance4.set(1, second);
                                }
                                if (r3 == -1) {
                                    instance4.set(2, r6.get(2));
                                } else {
                                    instance4.set(2, r3);
                                }
                                if (r4 == -1) {
                                    instance4.set(5, r6.get(5));
                                } else {
                                    instance4.set(5, r4);
                                }
                                arrayList.add(instance4.getTime());
                                instance = Calendar.getInstance();
                                stringBuffer6 = new StringBuffer();
                                stringBuffer7 = new StringBuffer();
                                mark3 = "";
                                if (clock2 == -1) {
                                }
                                if (minute2 == -1) {
                                }
                                if (second2 == -1) {
                                }
                                stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                                if (mark3.equals("")) {
                                    stringBuffer6.append("HH").append(":mm:ss");
                                } else {
                                    stringBuffer6.append("hh").append(":mm:ss").append(" a");
                                    stringBuffer7.append(Token.SEPARATOR).append(mark3);
                                }
                                if (timezone2.equals("")) {
                                    stringBuffer6.append(" Z");
                                    stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                                }
                                instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                                if (i2 == -1) {
                                    instance.set(1, r6.get(1));
                                } else {
                                    instance.set(1, i2);
                                }
                                if (i == -1) {
                                    instance.set(2, r6.get(2));
                                } else {
                                    instance.set(2, i);
                                }
                                if (type == -1) {
                                    instance.set(5, r6.get(5));
                                } else {
                                    instance.set(5, type);
                                }
                                arrayList.add(instance.getTime());
                            }
                            isMarkBefore = true;
                            if (isMarkBefore) {
                                mark2 = mark3;
                            }
                            if (timezone3.equals("")) {
                                timezone2 = timezone3;
                            } else {
                                if (timezone3.equals("")) {
                                    if (timezone2.equals("")) {
                                    }
                                }
                                timezone2 = str2;
                            }
                            instance4 = Calendar.getInstance();
                            stringBuffer = new StringBuffer();
                            stringBuffer5 = new StringBuffer();
                            if (year > 12) {
                                if (mark2.trim().equals("")) {
                                    mark2 = "";
                                }
                            }
                            if (year == -1) {
                            }
                            if (month == -1) {
                            }
                            if (day == -1) {
                            }
                            stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                            if (mark2.equals("")) {
                                stringBuffer.append("hh").append(":mm:ss").append(" a");
                                stringBuffer5.append(Token.SEPARATOR).append(mark2);
                            } else {
                                stringBuffer.append("HH").append(":mm:ss");
                            }
                            if (timezone2.equals("")) {
                                stringBuffer.append(" Z");
                                stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                            }
                            instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                            date = new Date(j);
                            r6 = Calendar.getInstance();
                            r6.setTime(date);
                            if (second == -1) {
                                instance4.set(1, second);
                            } else {
                                instance4.set(1, r6.get(1));
                            }
                            if (r3 == -1) {
                                instance4.set(2, r3);
                            } else {
                                instance4.set(2, r6.get(2));
                            }
                            if (r4 == -1) {
                                instance4.set(5, r4);
                            } else {
                                instance4.set(5, r6.get(5));
                            }
                            arrayList.add(instance4.getTime());
                            instance = Calendar.getInstance();
                            stringBuffer6 = new StringBuffer();
                            stringBuffer7 = new StringBuffer();
                            mark3 = "";
                            if (clock2 == -1) {
                            }
                            if (minute2 == -1) {
                            }
                            if (second2 == -1) {
                            }
                            stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                            if (mark3.equals("")) {
                                stringBuffer6.append("hh").append(":mm:ss").append(" a");
                                stringBuffer7.append(Token.SEPARATOR).append(mark3);
                            } else {
                                stringBuffer6.append("HH").append(":mm:ss");
                            }
                            if (timezone2.equals("")) {
                                stringBuffer6.append(" Z");
                                stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                            }
                            instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                            if (i2 == -1) {
                                instance.set(1, i2);
                            } else {
                                instance.set(1, r6.get(1));
                            }
                            if (i == -1) {
                                instance.set(2, i);
                            } else {
                                instance.set(2, r6.get(2));
                            }
                            if (type == -1) {
                                instance.set(5, type);
                            } else {
                                instance.set(5, r6.get(5));
                            }
                            arrayList.add(instance.getTime());
                        }
                    }
                    if (mark3.trim().equals("")) {
                        if (mark2.trim().equals("")) {
                            if (second == i2) {
                                obj = 1;
                                if (obj != null) {
                                    mark3 = mark2;
                                }
                            }
                            obj = null;
                            if (obj != null) {
                                mark3 = mark2;
                            }
                        }
                    }
                    if (timezone3.equals("")) {
                        if (timezone3.equals("")) {
                            if (timezone2.equals("")) {
                            }
                        }
                        timezone2 = str2;
                    } else {
                        timezone2 = timezone3;
                    }
                    instance4 = Calendar.getInstance();
                    stringBuffer = new StringBuffer();
                    stringBuffer5 = new StringBuffer();
                    if (year > 12) {
                        if (mark2.trim().equals("")) {
                            mark2 = "";
                        }
                    }
                    if (year == -1) {
                    }
                    if (month == -1) {
                    }
                    if (day == -1) {
                    }
                    stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                    if (mark2.equals("")) {
                        stringBuffer.append("HH").append(":mm:ss");
                    } else {
                        stringBuffer.append("hh").append(":mm:ss").append(" a");
                        stringBuffer5.append(Token.SEPARATOR).append(mark2);
                    }
                    if (timezone2.equals("")) {
                        stringBuffer.append(" Z");
                        stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                    }
                    instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                    date = new Date(j);
                    r6 = Calendar.getInstance();
                    r6.setTime(date);
                    if (second == -1) {
                        instance4.set(1, r6.get(1));
                    } else {
                        instance4.set(1, second);
                    }
                    if (r3 == -1) {
                        instance4.set(2, r6.get(2));
                    } else {
                        instance4.set(2, r3);
                    }
                    if (r4 == -1) {
                        instance4.set(5, r6.get(5));
                    } else {
                        instance4.set(5, r4);
                    }
                    arrayList.add(instance4.getTime());
                    instance = Calendar.getInstance();
                    stringBuffer6 = new StringBuffer();
                    stringBuffer7 = new StringBuffer();
                    mark3 = "";
                    if (clock2 == -1) {
                    }
                    if (minute2 == -1) {
                    }
                    if (second2 == -1) {
                    }
                    stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                    if (mark3.equals("")) {
                        stringBuffer6.append("HH").append(":mm:ss");
                    } else {
                        stringBuffer6.append("hh").append(":mm:ss").append(" a");
                        stringBuffer7.append(Token.SEPARATOR).append(mark3);
                    }
                    if (timezone2.equals("")) {
                        stringBuffer6.append(" Z");
                        stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                    }
                    instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                    if (i2 == -1) {
                        instance.set(1, r6.get(1));
                    } else {
                        instance.set(1, i2);
                    }
                    if (i == -1) {
                        instance.set(2, r6.get(2));
                    } else {
                        instance.set(2, i);
                    }
                    if (type == -1) {
                        instance.set(5, r6.get(5));
                    } else {
                        instance.set(5, type);
                    }
                    arrayList.add(instance.getTime());
                }
                if (satuts == 2) {
                    second = i2;
                    r3 = i;
                    r4 = type;
                }
                str2 = "";
                if (satuts == 0) {
                    timezone2 = str2;
                    instance4 = Calendar.getInstance();
                    stringBuffer = new StringBuffer();
                    stringBuffer5 = new StringBuffer();
                    if (year > 12) {
                        if (mark2.trim().equals("")) {
                            mark2 = "";
                        }
                    }
                    if (year == -1) {
                    }
                    if (month == -1) {
                    }
                    if (day == -1) {
                    }
                    stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                    if (mark2.equals("")) {
                        stringBuffer.append("hh").append(":mm:ss").append(" a");
                        stringBuffer5.append(Token.SEPARATOR).append(mark2);
                    } else {
                        stringBuffer.append("HH").append(":mm:ss");
                    }
                    if (timezone2.equals("")) {
                        stringBuffer.append(" Z");
                        stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                    }
                    instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                    date = new Date(j);
                    r6 = Calendar.getInstance();
                    r6.setTime(date);
                    if (second == -1) {
                        instance4.set(1, second);
                    } else {
                        instance4.set(1, r6.get(1));
                    }
                    if (r3 == -1) {
                        instance4.set(2, r3);
                    } else {
                        instance4.set(2, r6.get(2));
                    }
                    if (r4 == -1) {
                        instance4.set(5, r4);
                    } else {
                        instance4.set(5, r6.get(5));
                    }
                    arrayList.add(instance4.getTime());
                    instance = Calendar.getInstance();
                    stringBuffer6 = new StringBuffer();
                    stringBuffer7 = new StringBuffer();
                    mark3 = "";
                    if (clock2 == -1) {
                    }
                    if (minute2 == -1) {
                    }
                    if (second2 == -1) {
                    }
                    stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                    if (mark3.equals("")) {
                        stringBuffer6.append("hh").append(":mm:ss").append(" a");
                        stringBuffer7.append(Token.SEPARATOR).append(mark3);
                    } else {
                        stringBuffer6.append("HH").append(":mm:ss");
                    }
                    if (timezone2.equals("")) {
                        stringBuffer6.append(" Z");
                        stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                    }
                    instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                    if (i2 == -1) {
                        instance.set(1, i2);
                    } else {
                        instance.set(1, r6.get(1));
                    }
                    if (i == -1) {
                        instance.set(2, i);
                    } else {
                        instance.set(2, r6.get(2));
                    }
                    if (type == -1) {
                        instance.set(5, type);
                    } else {
                        instance.set(5, r6.get(5));
                    }
                    arrayList.add(instance.getTime());
                }
                if (mark2.trim().equals("")) {
                    if (mark3.trim().equals("")) {
                        if (second != i2) {
                            if (isMarkBefore) {
                                mark2 = mark3;
                            }
                            if (timezone3.equals("")) {
                                timezone2 = timezone3;
                            } else {
                                if (timezone3.equals("")) {
                                    if (timezone2.equals("")) {
                                    }
                                }
                                timezone2 = str2;
                            }
                            instance4 = Calendar.getInstance();
                            stringBuffer = new StringBuffer();
                            stringBuffer5 = new StringBuffer();
                            if (year > 12) {
                                if (mark2.trim().equals("")) {
                                    mark2 = "";
                                }
                            }
                            if (year == -1) {
                            }
                            if (month == -1) {
                            }
                            if (day == -1) {
                            }
                            stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                            if (mark2.equals("")) {
                                stringBuffer.append("HH").append(":mm:ss");
                            } else {
                                stringBuffer.append("hh").append(":mm:ss").append(" a");
                                stringBuffer5.append(Token.SEPARATOR).append(mark2);
                            }
                            if (timezone2.equals("")) {
                                stringBuffer.append(" Z");
                                stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                            }
                            instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                            date = new Date(j);
                            r6 = Calendar.getInstance();
                            r6.setTime(date);
                            if (second == -1) {
                                instance4.set(1, r6.get(1));
                            } else {
                                instance4.set(1, second);
                            }
                            if (r3 == -1) {
                                instance4.set(2, r6.get(2));
                            } else {
                                instance4.set(2, r3);
                            }
                            if (r4 == -1) {
                                instance4.set(5, r6.get(5));
                            } else {
                                instance4.set(5, r4);
                            }
                            arrayList.add(instance4.getTime());
                            instance = Calendar.getInstance();
                            stringBuffer6 = new StringBuffer();
                            stringBuffer7 = new StringBuffer();
                            mark3 = "";
                            if (clock2 == -1) {
                            }
                            if (minute2 == -1) {
                            }
                            if (second2 == -1) {
                            }
                            stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                            if (mark3.equals("")) {
                                stringBuffer6.append("HH").append(":mm:ss");
                            } else {
                                stringBuffer6.append("hh").append(":mm:ss").append(" a");
                                stringBuffer7.append(Token.SEPARATOR).append(mark3);
                            }
                            if (timezone2.equals("")) {
                                stringBuffer6.append(" Z");
                                stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                            }
                            instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                            if (i2 == -1) {
                                instance.set(1, r6.get(1));
                            } else {
                                instance.set(1, i2);
                            }
                            if (i == -1) {
                                instance.set(2, r6.get(2));
                            } else {
                                instance.set(2, i);
                            }
                            if (type == -1) {
                                instance.set(5, r6.get(5));
                            } else {
                                instance.set(5, type);
                            }
                            arrayList.add(instance.getTime());
                        }
                        isMarkBefore = true;
                        if (isMarkBefore) {
                            mark2 = mark3;
                        }
                        if (timezone3.equals("")) {
                            if (timezone3.equals("")) {
                                if (timezone2.equals("")) {
                                }
                            }
                            timezone2 = str2;
                        } else {
                            timezone2 = timezone3;
                        }
                        instance4 = Calendar.getInstance();
                        stringBuffer = new StringBuffer();
                        stringBuffer5 = new StringBuffer();
                        if (year > 12) {
                            if (mark2.trim().equals("")) {
                                mark2 = "";
                            }
                        }
                        if (year == -1) {
                        }
                        if (month == -1) {
                        }
                        if (day == -1) {
                        }
                        stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                        if (mark2.equals("")) {
                            stringBuffer.append("hh").append(":mm:ss").append(" a");
                            stringBuffer5.append(Token.SEPARATOR).append(mark2);
                        } else {
                            stringBuffer.append("HH").append(":mm:ss");
                        }
                        if (timezone2.equals("")) {
                            stringBuffer.append(" Z");
                            stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                        }
                        instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                        date = new Date(j);
                        r6 = Calendar.getInstance();
                        r6.setTime(date);
                        if (second == -1) {
                            instance4.set(1, second);
                        } else {
                            instance4.set(1, r6.get(1));
                        }
                        if (r3 == -1) {
                            instance4.set(2, r3);
                        } else {
                            instance4.set(2, r6.get(2));
                        }
                        if (r4 == -1) {
                            instance4.set(5, r4);
                        } else {
                            instance4.set(5, r6.get(5));
                        }
                        arrayList.add(instance4.getTime());
                        instance = Calendar.getInstance();
                        stringBuffer6 = new StringBuffer();
                        stringBuffer7 = new StringBuffer();
                        mark3 = "";
                        if (clock2 == -1) {
                        }
                        if (minute2 == -1) {
                        }
                        if (second2 == -1) {
                        }
                        stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                        if (mark3.equals("")) {
                            stringBuffer6.append("hh").append(":mm:ss").append(" a");
                            stringBuffer7.append(Token.SEPARATOR).append(mark3);
                        } else {
                            stringBuffer6.append("HH").append(":mm:ss");
                        }
                        if (timezone2.equals("")) {
                            stringBuffer6.append(" Z");
                            stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                        }
                        instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                        if (i2 == -1) {
                            instance.set(1, i2);
                        } else {
                            instance.set(1, r6.get(1));
                        }
                        if (i == -1) {
                            instance.set(2, i);
                        } else {
                            instance.set(2, r6.get(2));
                        }
                        if (type == -1) {
                            instance.set(5, type);
                        } else {
                            instance.set(5, r6.get(5));
                        }
                        arrayList.add(instance.getTime());
                    }
                }
                if (mark3.trim().equals("")) {
                    if (mark2.trim().equals("")) {
                        if (second == i2) {
                            obj = 1;
                            if (obj != null) {
                                mark3 = mark2;
                            }
                        }
                        obj = null;
                        if (obj != null) {
                            mark3 = mark2;
                        }
                    }
                }
                if (timezone3.equals("")) {
                    timezone2 = timezone3;
                } else {
                    if (timezone3.equals("")) {
                        if (timezone2.equals("")) {
                        }
                    }
                    timezone2 = str2;
                }
                instance4 = Calendar.getInstance();
                stringBuffer = new StringBuffer();
                stringBuffer5 = new StringBuffer();
                if (year > 12) {
                    if (mark2.trim().equals("")) {
                        mark2 = "";
                    }
                }
                if (year == -1) {
                }
                if (month == -1) {
                }
                if (day == -1) {
                }
                stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                if (mark2.equals("")) {
                    stringBuffer.append("HH").append(":mm:ss");
                } else {
                    stringBuffer.append("hh").append(":mm:ss").append(" a");
                    stringBuffer5.append(Token.SEPARATOR).append(mark2);
                }
                if (timezone2.equals("")) {
                    stringBuffer.append(" Z");
                    stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                }
                instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                date = new Date(j);
                r6 = Calendar.getInstance();
                r6.setTime(date);
                if (second == -1) {
                    instance4.set(1, r6.get(1));
                } else {
                    instance4.set(1, second);
                }
                if (r3 == -1) {
                    instance4.set(2, r6.get(2));
                } else {
                    instance4.set(2, r3);
                }
                if (r4 == -1) {
                    instance4.set(5, r6.get(5));
                } else {
                    instance4.set(5, r4);
                }
                arrayList.add(instance4.getTime());
                instance = Calendar.getInstance();
                stringBuffer6 = new StringBuffer();
                stringBuffer7 = new StringBuffer();
                mark3 = "";
                if (clock2 == -1) {
                }
                if (minute2 == -1) {
                }
                if (second2 == -1) {
                }
                stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                if (mark3.equals("")) {
                    stringBuffer6.append("HH").append(":mm:ss");
                } else {
                    stringBuffer6.append("hh").append(":mm:ss").append(" a");
                    stringBuffer7.append(Token.SEPARATOR).append(mark3);
                }
                if (timezone2.equals("")) {
                    stringBuffer6.append(" Z");
                    stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                }
                instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                if (i2 == -1) {
                    instance.set(1, r6.get(1));
                } else {
                    instance.set(1, i2);
                }
                if (i == -1) {
                    instance.set(2, r6.get(2));
                } else {
                    instance.set(2, i);
                }
                if (type == -1) {
                    instance.set(5, r6.get(5));
                } else {
                    instance.set(5, type);
                }
                arrayList.add(instance.getTime());
            }
            type = day2;
            if (satuts2 == 0) {
                str2 = "";
                if (satuts == 0) {
                    timezone2 = str2;
                    instance4 = Calendar.getInstance();
                    stringBuffer = new StringBuffer();
                    stringBuffer5 = new StringBuffer();
                    if (year > 12) {
                        if (mark2.trim().equals("")) {
                            mark2 = "";
                        }
                    }
                    if (year == -1) {
                    }
                    if (month == -1) {
                    }
                    if (day == -1) {
                    }
                    stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                    if (mark2.equals("")) {
                        stringBuffer.append("hh").append(":mm:ss").append(" a");
                        stringBuffer5.append(Token.SEPARATOR).append(mark2);
                    } else {
                        stringBuffer.append("HH").append(":mm:ss");
                    }
                    if (timezone2.equals("")) {
                        stringBuffer.append(" Z");
                        stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                    }
                    instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                    date = new Date(j);
                    r6 = Calendar.getInstance();
                    r6.setTime(date);
                    if (second == -1) {
                        instance4.set(1, second);
                    } else {
                        instance4.set(1, r6.get(1));
                    }
                    if (r3 == -1) {
                        instance4.set(2, r3);
                    } else {
                        instance4.set(2, r6.get(2));
                    }
                    if (r4 == -1) {
                        instance4.set(5, r4);
                    } else {
                        instance4.set(5, r6.get(5));
                    }
                    arrayList.add(instance4.getTime());
                    instance = Calendar.getInstance();
                    stringBuffer6 = new StringBuffer();
                    stringBuffer7 = new StringBuffer();
                    mark3 = "";
                    if (clock2 == -1) {
                    }
                    if (minute2 == -1) {
                    }
                    if (second2 == -1) {
                    }
                    stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                    if (mark3.equals("")) {
                        stringBuffer6.append("hh").append(":mm:ss").append(" a");
                        stringBuffer7.append(Token.SEPARATOR).append(mark3);
                    } else {
                        stringBuffer6.append("HH").append(":mm:ss");
                    }
                    if (timezone2.equals("")) {
                        stringBuffer6.append(" Z");
                        stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                    }
                    instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                    if (i2 == -1) {
                        instance.set(1, i2);
                    } else {
                        instance.set(1, r6.get(1));
                    }
                    if (i == -1) {
                        instance.set(2, i);
                    } else {
                        instance.set(2, r6.get(2));
                    }
                    if (type == -1) {
                        instance.set(5, type);
                    } else {
                        instance.set(5, r6.get(5));
                    }
                    arrayList.add(instance.getTime());
                }
                if (mark2.trim().equals("")) {
                    if (mark3.trim().equals("")) {
                        if (second != i2) {
                            if (isMarkBefore) {
                                mark2 = mark3;
                            }
                            if (timezone3.equals("")) {
                                if (timezone3.equals("")) {
                                    if (timezone2.equals("")) {
                                    }
                                }
                                timezone2 = str2;
                            } else {
                                timezone2 = timezone3;
                            }
                            instance4 = Calendar.getInstance();
                            stringBuffer = new StringBuffer();
                            stringBuffer5 = new StringBuffer();
                            if (year > 12) {
                                if (mark2.trim().equals("")) {
                                    mark2 = "";
                                }
                            }
                            if (year == -1) {
                            }
                            if (month == -1) {
                            }
                            if (day == -1) {
                            }
                            stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                            if (mark2.equals("")) {
                                stringBuffer.append("HH").append(":mm:ss");
                            } else {
                                stringBuffer.append("hh").append(":mm:ss").append(" a");
                                stringBuffer5.append(Token.SEPARATOR).append(mark2);
                            }
                            if (timezone2.equals("")) {
                                stringBuffer.append(" Z");
                                stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                            }
                            instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                            date = new Date(j);
                            r6 = Calendar.getInstance();
                            r6.setTime(date);
                            if (second == -1) {
                                instance4.set(1, r6.get(1));
                            } else {
                                instance4.set(1, second);
                            }
                            if (r3 == -1) {
                                instance4.set(2, r6.get(2));
                            } else {
                                instance4.set(2, r3);
                            }
                            if (r4 == -1) {
                                instance4.set(5, r6.get(5));
                            } else {
                                instance4.set(5, r4);
                            }
                            arrayList.add(instance4.getTime());
                            instance = Calendar.getInstance();
                            stringBuffer6 = new StringBuffer();
                            stringBuffer7 = new StringBuffer();
                            mark3 = "";
                            if (clock2 == -1) {
                            }
                            if (minute2 == -1) {
                            }
                            if (second2 == -1) {
                            }
                            stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                            if (mark3.equals("")) {
                                stringBuffer6.append("HH").append(":mm:ss");
                            } else {
                                stringBuffer6.append("hh").append(":mm:ss").append(" a");
                                stringBuffer7.append(Token.SEPARATOR).append(mark3);
                            }
                            if (timezone2.equals("")) {
                                stringBuffer6.append(" Z");
                                stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                            }
                            instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                            if (i2 == -1) {
                                instance.set(1, r6.get(1));
                            } else {
                                instance.set(1, i2);
                            }
                            if (i == -1) {
                                instance.set(2, r6.get(2));
                            } else {
                                instance.set(2, i);
                            }
                            if (type == -1) {
                                instance.set(5, r6.get(5));
                            } else {
                                instance.set(5, type);
                            }
                            arrayList.add(instance.getTime());
                        }
                        isMarkBefore = true;
                        if (isMarkBefore) {
                            mark2 = mark3;
                        }
                        if (timezone3.equals("")) {
                            timezone2 = timezone3;
                        } else {
                            if (timezone3.equals("")) {
                                if (timezone2.equals("")) {
                                }
                            }
                            timezone2 = str2;
                        }
                        instance4 = Calendar.getInstance();
                        stringBuffer = new StringBuffer();
                        stringBuffer5 = new StringBuffer();
                        if (year > 12) {
                            if (mark2.trim().equals("")) {
                                mark2 = "";
                            }
                        }
                        if (year == -1) {
                        }
                        if (month == -1) {
                        }
                        if (day == -1) {
                        }
                        stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                        if (mark2.equals("")) {
                            stringBuffer.append("hh").append(":mm:ss").append(" a");
                            stringBuffer5.append(Token.SEPARATOR).append(mark2);
                        } else {
                            stringBuffer.append("HH").append(":mm:ss");
                        }
                        if (timezone2.equals("")) {
                            stringBuffer.append(" Z");
                            stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                        }
                        instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                        date = new Date(j);
                        r6 = Calendar.getInstance();
                        r6.setTime(date);
                        if (second == -1) {
                            instance4.set(1, second);
                        } else {
                            instance4.set(1, r6.get(1));
                        }
                        if (r3 == -1) {
                            instance4.set(2, r3);
                        } else {
                            instance4.set(2, r6.get(2));
                        }
                        if (r4 == -1) {
                            instance4.set(5, r4);
                        } else {
                            instance4.set(5, r6.get(5));
                        }
                        arrayList.add(instance4.getTime());
                        instance = Calendar.getInstance();
                        stringBuffer6 = new StringBuffer();
                        stringBuffer7 = new StringBuffer();
                        mark3 = "";
                        if (clock2 == -1) {
                        }
                        if (minute2 == -1) {
                        }
                        if (second2 == -1) {
                        }
                        stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                        if (mark3.equals("")) {
                            stringBuffer6.append("hh").append(":mm:ss").append(" a");
                            stringBuffer7.append(Token.SEPARATOR).append(mark3);
                        } else {
                            stringBuffer6.append("HH").append(":mm:ss");
                        }
                        if (timezone2.equals("")) {
                            stringBuffer6.append(" Z");
                            stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                        }
                        instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                        if (i2 == -1) {
                            instance.set(1, i2);
                        } else {
                            instance.set(1, r6.get(1));
                        }
                        if (i == -1) {
                            instance.set(2, i);
                        } else {
                            instance.set(2, r6.get(2));
                        }
                        if (type == -1) {
                            instance.set(5, type);
                        } else {
                            instance.set(5, r6.get(5));
                        }
                        arrayList.add(instance.getTime());
                    }
                }
                if (mark3.trim().equals("")) {
                    if (mark2.trim().equals("")) {
                        if (second == i2) {
                            obj = 1;
                            if (obj != null) {
                                mark3 = mark2;
                            }
                        }
                        obj = null;
                        if (obj != null) {
                            mark3 = mark2;
                        }
                    }
                }
                if (timezone3.equals("")) {
                    if (timezone3.equals("")) {
                        if (timezone2.equals("")) {
                        }
                    }
                    timezone2 = str2;
                } else {
                    timezone2 = timezone3;
                }
                instance4 = Calendar.getInstance();
                stringBuffer = new StringBuffer();
                stringBuffer5 = new StringBuffer();
                if (year > 12) {
                    if (mark2.trim().equals("")) {
                        mark2 = "";
                    }
                }
                if (year == -1) {
                }
                if (month == -1) {
                }
                if (day == -1) {
                }
                stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                if (mark2.equals("")) {
                    stringBuffer.append("HH").append(":mm:ss");
                } else {
                    stringBuffer.append("hh").append(":mm:ss").append(" a");
                    stringBuffer5.append(Token.SEPARATOR).append(mark2);
                }
                if (timezone2.equals("")) {
                    stringBuffer.append(" Z");
                    stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                }
                instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                date = new Date(j);
                r6 = Calendar.getInstance();
                r6.setTime(date);
                if (second == -1) {
                    instance4.set(1, r6.get(1));
                } else {
                    instance4.set(1, second);
                }
                if (r3 == -1) {
                    instance4.set(2, r6.get(2));
                } else {
                    instance4.set(2, r3);
                }
                if (r4 == -1) {
                    instance4.set(5, r6.get(5));
                } else {
                    instance4.set(5, r4);
                }
                arrayList.add(instance4.getTime());
                instance = Calendar.getInstance();
                stringBuffer6 = new StringBuffer();
                stringBuffer7 = new StringBuffer();
                mark3 = "";
                if (clock2 == -1) {
                }
                if (minute2 == -1) {
                }
                if (second2 == -1) {
                }
                stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                if (mark3.equals("")) {
                    stringBuffer6.append("HH").append(":mm:ss");
                } else {
                    stringBuffer6.append("hh").append(":mm:ss").append(" a");
                    stringBuffer7.append(Token.SEPARATOR).append(mark3);
                }
                if (timezone2.equals("")) {
                    stringBuffer6.append(" Z");
                    stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                }
                instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                if (i2 == -1) {
                    instance.set(1, r6.get(1));
                } else {
                    instance.set(1, i2);
                }
                if (i == -1) {
                    instance.set(2, r6.get(2));
                } else {
                    instance.set(2, i);
                }
                if (type == -1) {
                    instance.set(5, r6.get(5));
                } else {
                    instance.set(5, type);
                }
                arrayList.add(instance.getTime());
            }
            if (satuts == 2) {
                second = i2;
                r3 = i;
                r4 = type;
            }
            str2 = "";
            if (satuts == 0) {
                timezone2 = str2;
                instance4 = Calendar.getInstance();
                stringBuffer = new StringBuffer();
                stringBuffer5 = new StringBuffer();
                if (year > 12) {
                    if (mark2.trim().equals("")) {
                        mark2 = "";
                    }
                }
                if (year == -1) {
                }
                if (month == -1) {
                }
                if (day == -1) {
                }
                stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                if (mark2.equals("")) {
                    stringBuffer.append("hh").append(":mm:ss").append(" a");
                    stringBuffer5.append(Token.SEPARATOR).append(mark2);
                } else {
                    stringBuffer.append("HH").append(":mm:ss");
                }
                if (timezone2.equals("")) {
                    stringBuffer.append(" Z");
                    stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                }
                instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                date = new Date(j);
                r6 = Calendar.getInstance();
                r6.setTime(date);
                if (second == -1) {
                    instance4.set(1, second);
                } else {
                    instance4.set(1, r6.get(1));
                }
                if (r3 == -1) {
                    instance4.set(2, r3);
                } else {
                    instance4.set(2, r6.get(2));
                }
                if (r4 == -1) {
                    instance4.set(5, r4);
                } else {
                    instance4.set(5, r6.get(5));
                }
                arrayList.add(instance4.getTime());
                instance = Calendar.getInstance();
                stringBuffer6 = new StringBuffer();
                stringBuffer7 = new StringBuffer();
                mark3 = "";
                if (clock2 == -1) {
                }
                if (minute2 == -1) {
                }
                if (second2 == -1) {
                }
                stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                if (mark3.equals("")) {
                    stringBuffer6.append("hh").append(":mm:ss").append(" a");
                    stringBuffer7.append(Token.SEPARATOR).append(mark3);
                } else {
                    stringBuffer6.append("HH").append(":mm:ss");
                }
                if (timezone2.equals("")) {
                    stringBuffer6.append(" Z");
                    stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                }
                instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                if (i2 == -1) {
                    instance.set(1, i2);
                } else {
                    instance.set(1, r6.get(1));
                }
                if (i == -1) {
                    instance.set(2, i);
                } else {
                    instance.set(2, r6.get(2));
                }
                if (type == -1) {
                    instance.set(5, type);
                } else {
                    instance.set(5, r6.get(5));
                }
                arrayList.add(instance.getTime());
            }
            if (mark2.trim().equals("")) {
                if (mark3.trim().equals("")) {
                    if (second != i2) {
                        if (isMarkBefore) {
                            mark2 = mark3;
                        }
                        if (timezone3.equals("")) {
                            timezone2 = timezone3;
                        } else {
                            if (timezone3.equals("")) {
                                if (timezone2.equals("")) {
                                }
                            }
                            timezone2 = str2;
                        }
                        instance4 = Calendar.getInstance();
                        stringBuffer = new StringBuffer();
                        stringBuffer5 = new StringBuffer();
                        if (year > 12) {
                            if (mark2.trim().equals("")) {
                                mark2 = "";
                            }
                        }
                        if (year == -1) {
                        }
                        if (month == -1) {
                        }
                        if (day == -1) {
                        }
                        stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                        if (mark2.equals("")) {
                            stringBuffer.append("HH").append(":mm:ss");
                        } else {
                            stringBuffer.append("hh").append(":mm:ss").append(" a");
                            stringBuffer5.append(Token.SEPARATOR).append(mark2);
                        }
                        if (timezone2.equals("")) {
                            stringBuffer.append(" Z");
                            stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                        }
                        instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                        date = new Date(j);
                        r6 = Calendar.getInstance();
                        r6.setTime(date);
                        if (second == -1) {
                            instance4.set(1, r6.get(1));
                        } else {
                            instance4.set(1, second);
                        }
                        if (r3 == -1) {
                            instance4.set(2, r6.get(2));
                        } else {
                            instance4.set(2, r3);
                        }
                        if (r4 == -1) {
                            instance4.set(5, r6.get(5));
                        } else {
                            instance4.set(5, r4);
                        }
                        arrayList.add(instance4.getTime());
                        instance = Calendar.getInstance();
                        stringBuffer6 = new StringBuffer();
                        stringBuffer7 = new StringBuffer();
                        mark3 = "";
                        if (clock2 == -1) {
                        }
                        if (minute2 == -1) {
                        }
                        if (second2 == -1) {
                        }
                        stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                        if (mark3.equals("")) {
                            stringBuffer6.append("HH").append(":mm:ss");
                        } else {
                            stringBuffer6.append("hh").append(":mm:ss").append(" a");
                            stringBuffer7.append(Token.SEPARATOR).append(mark3);
                        }
                        if (timezone2.equals("")) {
                            stringBuffer6.append(" Z");
                            stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                        }
                        instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                        if (i2 == -1) {
                            instance.set(1, r6.get(1));
                        } else {
                            instance.set(1, i2);
                        }
                        if (i == -1) {
                            instance.set(2, r6.get(2));
                        } else {
                            instance.set(2, i);
                        }
                        if (type == -1) {
                            instance.set(5, r6.get(5));
                        } else {
                            instance.set(5, type);
                        }
                        arrayList.add(instance.getTime());
                    }
                    isMarkBefore = true;
                    if (isMarkBefore) {
                        mark2 = mark3;
                    }
                    if (timezone3.equals("")) {
                        if (timezone3.equals("")) {
                            if (timezone2.equals("")) {
                            }
                        }
                        timezone2 = str2;
                    } else {
                        timezone2 = timezone3;
                    }
                    instance4 = Calendar.getInstance();
                    stringBuffer = new StringBuffer();
                    stringBuffer5 = new StringBuffer();
                    if (year > 12) {
                        if (mark2.trim().equals("")) {
                            mark2 = "";
                        }
                    }
                    if (year == -1) {
                    }
                    if (month == -1) {
                    }
                    if (day == -1) {
                    }
                    stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
                    if (mark2.equals("")) {
                        stringBuffer.append("hh").append(":mm:ss").append(" a");
                        stringBuffer5.append(Token.SEPARATOR).append(mark2);
                    } else {
                        stringBuffer.append("HH").append(":mm:ss");
                    }
                    if (timezone2.equals("")) {
                        stringBuffer.append(" Z");
                        stringBuffer5.append(Token.SEPARATOR).append(timezone2);
                    }
                    instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
                    date = new Date(j);
                    r6 = Calendar.getInstance();
                    r6.setTime(date);
                    if (second == -1) {
                        instance4.set(1, second);
                    } else {
                        instance4.set(1, r6.get(1));
                    }
                    if (r3 == -1) {
                        instance4.set(2, r3);
                    } else {
                        instance4.set(2, r6.get(2));
                    }
                    if (r4 == -1) {
                        instance4.set(5, r4);
                    } else {
                        instance4.set(5, r6.get(5));
                    }
                    arrayList.add(instance4.getTime());
                    instance = Calendar.getInstance();
                    stringBuffer6 = new StringBuffer();
                    stringBuffer7 = new StringBuffer();
                    mark3 = "";
                    if (clock2 == -1) {
                    }
                    if (minute2 == -1) {
                    }
                    if (second2 == -1) {
                    }
                    stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
                    if (mark3.equals("")) {
                        stringBuffer6.append("hh").append(":mm:ss").append(" a");
                        stringBuffer7.append(Token.SEPARATOR).append(mark3);
                    } else {
                        stringBuffer6.append("HH").append(":mm:ss");
                    }
                    if (timezone2.equals("")) {
                        stringBuffer6.append(" Z");
                        stringBuffer7.append(Token.SEPARATOR).append(timezone2);
                    }
                    instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
                    if (i2 == -1) {
                        instance.set(1, i2);
                    } else {
                        instance.set(1, r6.get(1));
                    }
                    if (i == -1) {
                        instance.set(2, i);
                    } else {
                        instance.set(2, r6.get(2));
                    }
                    if (type == -1) {
                        instance.set(5, type);
                    } else {
                        instance.set(5, r6.get(5));
                    }
                    arrayList.add(instance.getTime());
                }
            }
            if (mark3.trim().equals("")) {
                if (mark2.trim().equals("")) {
                    if (second == i2) {
                        obj = 1;
                        if (obj != null) {
                            mark3 = mark2;
                        }
                    }
                    obj = null;
                    if (obj != null) {
                        mark3 = mark2;
                    }
                }
            }
            if (timezone3.equals("")) {
                timezone2 = timezone3;
            } else {
                if (timezone3.equals("")) {
                    if (timezone2.equals("")) {
                    }
                }
                timezone2 = str2;
            }
            instance4 = Calendar.getInstance();
            stringBuffer = new StringBuffer();
            stringBuffer5 = new StringBuffer();
            if (year > 12) {
                if (mark2.trim().equals("")) {
                    mark2 = "";
                }
            }
            if (year == -1) {
            }
            if (month == -1) {
            }
            if (day == -1) {
            }
            stringBuffer5.append(year == -1 ? "08" : Integer.valueOf(year)).append(":").append(month == -1 ? "00" : Integer.valueOf(month)).append(":").append(day == -1 ? "00" : Integer.valueOf(day));
            if (mark2.equals("")) {
                stringBuffer.append("HH").append(":mm:ss");
            } else {
                stringBuffer.append("hh").append(":mm:ss").append(" a");
                stringBuffer5.append(Token.SEPARATOR).append(mark2);
            }
            if (timezone2.equals("")) {
                stringBuffer.append(" Z");
                stringBuffer5.append(Token.SEPARATOR).append(timezone2);
            }
            try {
                instance4.setTime(new SimpleDateFormat(stringBuffer.toString(), Locale.ENGLISH).parse(stringBuffer5.toString()));
            } catch (ParseException e3) {
                e3.printStackTrace();
            }
            date = new Date(j);
            r6 = Calendar.getInstance();
            r6.setTime(date);
            if (second == -1) {
                instance4.set(1, r6.get(1));
            } else {
                instance4.set(1, second);
            }
            if (r3 == -1) {
                instance4.set(2, r6.get(2));
            } else {
                instance4.set(2, r3);
            }
            if (r4 == -1) {
                instance4.set(5, r6.get(5));
            } else {
                instance4.set(5, r4);
            }
            arrayList.add(instance4.getTime());
            instance = Calendar.getInstance();
            stringBuffer6 = new StringBuffer();
            stringBuffer7 = new StringBuffer();
            mark3 = "";
            if (clock2 == -1) {
            }
            if (minute2 == -1) {
            }
            if (second2 == -1) {
            }
            stringBuffer7.append(clock2 == -1 ? "08" : Integer.valueOf(clock2)).append(":").append(minute2 == -1 ? "00" : Integer.valueOf(minute2)).append(":").append(second2 == -1 ? "00" : Integer.valueOf(second2));
            if (mark3.equals("")) {
                stringBuffer6.append("HH").append(":mm:ss");
            } else {
                stringBuffer6.append("hh").append(":mm:ss").append(" a");
                stringBuffer7.append(Token.SEPARATOR).append(mark3);
            }
            if (timezone2.equals("")) {
                stringBuffer6.append(" Z");
                stringBuffer7.append(Token.SEPARATOR).append(timezone2);
            }
            try {
                instance.setTime(new SimpleDateFormat(stringBuffer6.toString(), Locale.ENGLISH).parse(stringBuffer7.toString()));
            } catch (ParseException e4) {
                e4.printStackTrace();
            }
            if (i2 == -1) {
                instance.set(1, r6.get(1));
            } else {
                instance.set(1, i2);
            }
            if (i == -1) {
                instance.set(2, r6.get(2));
            } else {
                instance.set(2, i);
            }
            if (type == -1) {
                instance.set(5, r6.get(5));
            } else {
                instance.set(5, type);
            }
            arrayList.add(instance.getTime());
        }
        return arrayList;
    }
}

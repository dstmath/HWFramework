package com.huawei.i18n.tmr.datetime.parse;

import android.util.Log;
import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.i18n.tmr.datetime.Filter;
import com.huawei.i18n.tmr.datetime.Match;
import com.huawei.i18n.tmr.datetime.data.LocaleParam;
import com.huawei.i18n.tmr.datetime.utils.DatePeriod;
import com.huawei.i18n.tmr.datetime.utils.DateTime;
import huawei.android.provider.HanziToPinyin;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateConvert {
    private static final String TAG = "DateConvert";
    private String locale;

    /* access modifiers changed from: private */
    public enum DateOrder {
        BEGIN,
        END
    }

    public enum DateCombine {
        NOT_COMBINE(0),
        TWO_COMBINE(1),
        ALL_COMBINE(2);
        
        private int value;

        private DateCombine(int value2) {
            this.value = value2;
        }

        public int getValue() {
            return this.value;
        }
    }

    public DateConvert(String locale2) {
        this.locale = locale2;
    }

    public Optional<DatePeriod> filterByParse(String content, List<Match> matchs, String toSign, String connecter) {
        if (matchs == null || matchs.isEmpty()) {
            return Optional.empty();
        }
        DatePeriod result = null;
        if (matchs.size() == 1) {
            result = matchs.get(0).getDp();
        } else {
            Match match = convertMultiMatch(content, matchs, toSign, connecter);
            if (match != null) {
                result = match.getDp();
            }
        }
        return Optional.ofNullable(result);
    }

    public int nestDealDate(String content, Match current, List<Match> list, int preType) {
        Match nextMatch = list.get(0);
        int nextType = Filter.getType(nextMatch.getRegex());
        if (nextType != Filter.FilterType.TYPE_DATE.getValue() && nextType != Filter.FilterType.TYPE_TODAY.getValue() && nextType != Filter.FilterType.TYPE_WEEK.getValue()) {
            return 0;
        }
        if (nextType != Filter.getType(current.getRegex())) {
            if (nextType != preType) {
                boolean isThree = false;
                String ss = content.substring(current.getEnd(), nextMatch.getBegin());
                if (!LocaleParam.isRelDates(ss, this.locale) && !"(".equals(ss.trim())) {
                    return 0;
                }
                if (list.size() > 1 && nestDealDate(content, nextMatch, list.subList(1, list.size()), current.getType()) == DateCombine.TWO_COMBINE.getValue()) {
                    isThree = true;
                }
                boolean isBrackets = false;
                if ("(".equals(ss.trim())) {
                    Matcher brackMatch = Pattern.compile("\\s*\\((.*?)\\s*\\)").matcher(content.substring(current.getEnd()));
                    String str = null;
                    if (brackMatch.lookingAt()) {
                        str = brackMatch.group(1);
                    }
                    isBrackets = str != null && str.trim().equals(content.substring(nextMatch.getBegin(), isThree ? list.get(1).getEnd() : nextMatch.getEnd()).trim());
                }
                if (!LocaleParam.isRelDates(ss, this.locale) && !isBrackets) {
                    return 0;
                }
                if (isThree) {
                    return DateCombine.ALL_COMBINE.getValue();
                }
                return DateCombine.TWO_COMBINE.getValue();
            }
        }
        return 0;
    }

    private List<Match> filterToJoinDates(String content, List<Match> matchList) {
        Match change;
        List<Match> result = new ArrayList<>();
        int i = 0;
        while (i < matchList.size()) {
            Match match = matchList.get(i);
            int type = Filter.getType(match.getRegex());
            if (type == Filter.FilterType.TYPE_DATE.getValue() || type == Filter.FilterType.TYPE_TODAY.getValue() || type == Filter.FilterType.TYPE_WEEK.getValue() || type == Filter.FilterType.TYPE_SEVEN.getValue()) {
                int hasNum = (matchList.size() - 1) - i;
                List<Match> sub = null;
                if (hasNum > 1) {
                    sub = matchList.subList(i + 1, i + 3);
                } else if (hasNum == 1) {
                    sub = matchList.subList(i + 1, i + 2);
                }
                if (hasNum == 0 || sub == null) {
                    result.add(match);
                } else {
                    int status = nestDealDate(content, match, sub, Filter.FilterType.TYPE_NULL.getValue());
                    match.setType(Filter.FilterType.TYPE_DATE.getValue());
                    if (status == DateCombine.NOT_COMBINE.getValue()) {
                        result.add(match);
                    } else {
                        if (status == DateCombine.TWO_COMBINE.getValue()) {
                            i++;
                            change = twoCombine(matchList.get(i), match, null);
                        } else {
                            i += 2;
                            Optional<Match> allCombineMatch = allCombine(matchList, i);
                            change = allCombineMatch.isPresent() ? allCombineMatch.get() : null;
                        }
                        filterBrackets(content, matchList.get(i), match);
                        if (change != null) {
                            match.getDp().setBegin(change.getDp().getBegin());
                        }
                        result.add(match);
                    }
                }
            } else {
                result.add(match);
            }
            i++;
        }
        return result;
    }

    private Optional<Match> allCombine(List<Match> matchList, int index) {
        if (index >= matchList.size() || index < 2) {
            return Optional.empty();
        }
        int currentType = Filter.getType(matchList.get(index - 2).getRegex());
        int nextType = Filter.getType(matchList.get(index - 1).getRegex());
        int nextType2 = Filter.getType(matchList.get(index).getRegex());
        if (nextType < nextType2 && nextType < currentType) {
            return Optional.of(matchList.get(index - 1));
        }
        if (nextType2 >= nextType || nextType2 >= currentType) {
            return Optional.empty();
        }
        return Optional.of(matchList.get(index));
    }

    private Match twoCombine(Match nextMatch, Match match, Match matchChange) {
        return Filter.getType(match.getRegex()) > Filter.getType(nextMatch.getRegex()) ? nextMatch : matchChange;
    }

    private void filterBrackets(String content, Match endMatch, Match match) {
        int end;
        int add = 0;
        int be = content.indexOf(40, match.getEnd());
        if (be != -1 && be < endMatch.getBegin() && (end = content.indexOf(41, endMatch.getEnd())) != -1 && ")".equals(content.substring(endMatch.getEnd(), end + 1).trim())) {
            add = (end - endMatch.getEnd()) + 1;
        }
        match.setEnd(endMatch.getEnd() + add);
    }

    private List<Match> filterPeriod(String content, List<Match> matchList, String toSign) {
        Pattern pattern = Pattern.compile("\\.?\\s*(-{1,2}|~|起?至|到|au|–|—|～|تا|देखि|да|па|থেকে|ຫາ" + toSign + ")\\s*", 2);
        Iterator<Match> matchIterator = matchList.iterator();
        Match current = null;
        while (matchIterator.hasNext()) {
            Match match = matchIterator.next();
            if (current == null) {
                current = match;
            } else {
                int currentType = current.getDp().getType();
                int ntype = match.getDp().getType();
                if ((currentType != ntype || (currentType != DatePeriod.DatePeriodType.TYPE_DATE.getValue() && currentType != DatePeriod.DatePeriodType.TYPE_TIME.getValue() && currentType != DatePeriod.DatePeriodType.TYPE_DATETIME.getValue())) && (currentType != DatePeriod.DatePeriodType.TYPE_DATETIME.getValue() || ntype != DatePeriod.DatePeriodType.TYPE_TIME.getValue())) {
                    current = match;
                } else if (pattern.matcher(content.substring(current.getEnd(), match.getBegin())).matches()) {
                    current.setEnd(match.getEnd());
                    current.setType(DatePeriod.DatePeriodType.TYPE_DATETIME_DUR.getValue());
                    if (currentType == DatePeriod.DatePeriodType.TYPE_TIME.getValue() && ntype == DatePeriod.DatePeriodType.TYPE_TIME.getValue()) {
                        current.setIsTimePeriod(true);
                    }
                    current.getDp().setEnd(match.getDp().getBegin());
                    matchIterator.remove();
                }
            }
        }
        return matchList;
    }

    private List<Match> filterDateTime(String content, List<Match> matchList, String joiner) {
        boolean isJoiner;
        Pattern pattern = Pattern.compile("\\s*(at|às|،‏|،|u|kl\\.|को|的|o|à|a\\s+les|ve|la|pada|kl|στις|alle|jam|ຂອງວັນທີ" + joiner + ")\\s*", 2);
        Iterator<Match> matchIterator = matchList.iterator();
        Match lastMatch = null;
        while (matchIterator.hasNext()) {
            Match currentMatch = matchIterator.next();
            if (lastMatch == null) {
                lastMatch = currentMatch;
            } else {
                int lastType = lastMatch.getDp().getType();
                int currentType = currentMatch.getDp().getType();
                lastMatch.setType(lastType);
                currentMatch.setType(currentType);
                if ((lastType == DatePeriod.DatePeriodType.TYPE_DATE.getValue() && currentType == DatePeriod.DatePeriodType.TYPE_TIME.getValue()) || ((lastType == DatePeriod.DatePeriodType.TYPE_DATE.getValue() && currentType == DatePeriod.DatePeriodType.TYPE_TIME_DUR.getValue()) || ((lastType == DatePeriod.DatePeriodType.TYPE_TIME.getValue() && currentType == DatePeriod.DatePeriodType.TYPE_DATE.getValue()) || (lastType == DatePeriod.DatePeriodType.TYPE_TIME_DUR.getValue() && currentType == DatePeriod.DatePeriodType.TYPE_DATE.getValue())))) {
                    String joinerStr = content.substring(lastMatch.getEnd(), currentMatch.getBegin());
                    if (StorageManagerExt.INVALID_KEY_DESC.equals(joinerStr.trim())) {
                        isJoiner = true;
                    } else {
                        isJoiner = pattern.matcher(joinerStr).matches();
                    }
                    if (isJoiner) {
                        filterJoiner(matchIterator, lastMatch, currentMatch);
                    } else {
                        lastMatch = filterNotJoiner(content, matchIterator, lastMatch, currentMatch);
                    }
                } else {
                    lastMatch = currentMatch;
                }
            }
        }
        return matchList;
    }

    private Match filterNotJoiner(String content, Iterator<Match> matchIterator, Match match, Match currentMatch) {
        boolean isChange = true;
        int lastType = match.getType();
        int currentType = currentMatch.getType();
        if (lastType == Filter.FilterType.TYPE_TIME.getValue()) {
            int add = 0;
            Matcher matcher = Pattern.compile("\\s*\\((.*?)\\s*\\)").matcher(content.substring(match.getEnd()));
            String group = null;
            boolean isExit = true;
            if (matcher.lookingAt()) {
                group = matcher.group(1);
                add = matcher.group().length();
            }
            if (group == null || !group.trim().equals(content.substring(currentMatch.getBegin(), currentMatch.getEnd()).trim())) {
                isExit = false;
            }
            if (isExit) {
                match.setEnd(match.getEnd() + add);
                match.getDp().getBegin().setDay(currentMatch.getDp().getBegin().getDate());
                matchIterator.remove();
                isChange = false;
            }
        }
        if (lastType == Filter.FilterType.TYPE_DATE.getValue() && currentType == Filter.FilterType.TYPE_TIME.getValue()) {
            String beginStr = content.substring(0, match.getBegin());
            String endStr = content.substring(match.getEnd(), currentMatch.getBegin());
            if (beginStr.trim().endsWith("(") && endStr.trim().startsWith(")")) {
                match.setBegin(beginStr.lastIndexOf(40));
                match.setEnd(currentMatch.getEnd());
                match.getDp().getBegin().setTime(currentMatch.getDp().getBegin().getTime());
                matchIterator.remove();
                isChange = false;
            }
        }
        return isChange ? currentMatch : match;
    }

    private void filterJoiner(Iterator<Match> matchIterator, Match lastMatch, Match currentMatch) {
        int lastType = lastMatch.getType();
        int currentType = currentMatch.getType();
        lastMatch.setEnd(currentMatch.getEnd());
        if (lastType == Filter.FilterType.TYPE_DATE.getValue() && currentType == Filter.FilterType.TYPE_TIME.getValue()) {
            lastMatch.getDp().getBegin().setTime(currentMatch.getDp().getBegin().getTime());
        } else if (lastType == Filter.FilterType.TYPE_TIME.getValue()) {
            lastMatch.getDp().getBegin().setDay(currentMatch.getDp().getBegin().getDate());
        } else if (lastType == Filter.FilterType.TYPE_DATE.getValue()) {
            lastMatch.getDp().getBegin().setTime(currentMatch.getDp().getBegin().getTime());
            lastMatch.getDp().setEnd(currentMatch.getDp().getEnd());
        } else {
            lastMatch.getDp().getBegin().setDay(currentMatch.getDp().getBegin().getDate());
            lastMatch.getDp().getEnd().setDay(currentMatch.getDp().getBegin().getDate());
        }
        matchIterator.remove();
    }

    private Match filterDateTimePunc(String content, List<Match> matchList) {
        Iterator<Match> matchIterator = matchList.iterator();
        Match current = null;
        while (matchIterator.hasNext()) {
            Match match = matchIterator.next();
            if (current == null) {
                current = match;
            } else {
                int ctype = current.getType();
                int type = match.getType();
                boolean isJoiner = false;
                if ((ctype == Filter.FilterType.TYPE_DATE.getValue() && type == Filter.FilterType.TYPE_TIME.getValue()) || ((ctype == Filter.FilterType.TYPE_DATE.getValue() && type == 3 && match.isTimePeriod()) || ((ctype == Filter.FilterType.TYPE_TIME.getValue() && type == Filter.FilterType.TYPE_DATE.getValue()) || (ctype == 3 && current.isTimePeriod() && type == Filter.FilterType.TYPE_DATE.getValue())))) {
                    String ss = content.substring(current.getEnd(), match.getBegin());
                    if ("，".equals(ss.trim()) || ",".equals(ss.trim())) {
                        isJoiner = true;
                    }
                    if (isJoiner) {
                        current.setEnd(match.getEnd());
                        if (ctype == Filter.FilterType.TYPE_DATE.getValue() && type == Filter.FilterType.TYPE_TIME.getValue()) {
                            current.setType(Filter.FilterType.TYPE_DATETIME.getValue());
                            current.getDp().getBegin().setTime(match.getDp().getBegin().getTime());
                        } else if (ctype == Filter.FilterType.TYPE_TIME.getValue() && type == Filter.FilterType.TYPE_DATE.getValue()) {
                            current.setType(Filter.FilterType.TYPE_DATETIME.getValue());
                            current.getDp().getBegin().setDay(match.getDp().getBegin().getDate());
                        } else {
                            current.setType(Filter.FilterType.TYPE_TIME_PERIOD.getValue());
                            if (type == Filter.FilterType.TYPE_TIME_PERIOD.getValue()) {
                                current.getDp().getBegin().setTime(match.getDp().getBegin().getTime());
                                current.getDp().setEnd(match.getDp().getEnd());
                            } else {
                                current.getDp().getBegin().setDay(match.getDp().getBegin().getDate());
                            }
                        }
                        matchIterator.remove();
                    } else {
                        current = match;
                    }
                } else {
                    current = match;
                }
            }
        }
        return current;
    }

    private Match convertMultiMatch(String content, List<Match> matchList, String joiner, String connecter) {
        return filterDateTimePunc(content, filterPeriod(content, filterDateTime(content, filterToJoinDates(content, matchList), connecter), joiner));
    }

    public List<Date> convert(DatePeriod datePeriod, long relativeTime) {
        List<Date> result = new ArrayList<>();
        if (datePeriod == null) {
            return Collections.emptyList();
        }
        int type = datePeriod.getType();
        if (type == DatePeriod.DatePeriodType.TYPE_DATETIME.getValue()) {
            covertDateTime(datePeriod, relativeTime, result);
        } else if (type == DatePeriod.DatePeriodType.TYPE_DATE.getValue()) {
            covertDate(datePeriod, relativeTime, result);
        } else if (type == DatePeriod.DatePeriodType.TYPE_TIME.getValue()) {
            covertTime(datePeriod, relativeTime, result);
        } else if (type <= DatePeriod.DatePeriodType.TYPE_TIME.getValue()) {
            return result;
        } else {
            convertDur(datePeriod, relativeTime, result);
        }
        return result;
    }

    private void convertDur(DatePeriod datePeriod, long relativeTime, List<Date> result) {
        Calendar beginCalendar = getCalendar(datePeriod, relativeTime, DateOrder.BEGIN);
        Calendar endCalendar = getCalendar(datePeriod, relativeTime, DateOrder.END);
        int type = datePeriod.getType();
        if (beginCalendar.compareTo(endCalendar) == 1 && type == DatePeriod.DatePeriodType.TYPE_TIME_DUR.getValue()) {
            beginCalendar.add(5, -1);
        }
        result.add(beginCalendar.getTime());
        result.add(endCalendar.getTime());
    }

    private String getTimeZone(DatePeriod datePeriod) {
        String beginTimeZone = datePeriod.getBegin().getTime() != null ? datePeriod.getBegin().getTime().getTimezone() : StorageManagerExt.INVALID_KEY_DESC;
        String endTimeZone = datePeriod.getEnd().getTime() != null ? datePeriod.getEnd().getTime().getTimezone() : StorageManagerExt.INVALID_KEY_DESC;
        if (!isExitTime(datePeriod.getBegin().getType(), datePeriod.getEnd().getType())) {
            return StorageManagerExt.INVALID_KEY_DESC;
        }
        if (!StorageManagerExt.INVALID_KEY_DESC.equals(endTimeZone)) {
            return endTimeZone;
        }
        if (!StorageManagerExt.INVALID_KEY_DESC.equals(beginTimeZone)) {
            return beginTimeZone;
        }
        return StorageManagerExt.INVALID_KEY_DESC;
    }

    private Calendar getCalendar(DatePeriod datePeriod, long relativeTime, DateOrder type) {
        Calendar calendar = getTimeCalendar(datePeriod, type);
        addYMD(datePeriod, calendar, relativeTime, type);
        return calendar;
    }

    private Calendar getTimeCalendar(DatePeriod datePeriod, DateOrder type) {
        StringBuffer pattern = new StringBuffer();
        getTimePattern(pattern, datePeriod, type);
        StringBuffer dateStr = new StringBuffer();
        getTimeStr(datePeriod, type, dateStr);
        return formatCalendar(pattern, dateStr);
    }

    private void getTimePattern(StringBuffer pattern, DatePeriod datePeriod, DateOrder type) {
        int hour;
        int i = -1;
        if (type == DateOrder.BEGIN) {
            if (datePeriod.getBegin().getTime() != null) {
                i = datePeriod.getBegin().getTime().getClock();
            }
            hour = i;
        } else {
            if (datePeriod.getEnd().getTime() != null) {
                i = datePeriod.getEnd().getTime().getClock();
            }
            hour = i;
        }
        String timeMark = getTimeMark(datePeriod, type);
        if (hour > 12 && !StorageManagerExt.INVALID_KEY_DESC.equals(timeMark.trim())) {
            timeMark = StorageManagerExt.INVALID_KEY_DESC;
        }
        getTimePattern(pattern, timeMark, getTimeZone(datePeriod));
    }

    private void getTimePattern(StringBuffer pattern, String timeMark, String timeZone) {
        if (!StorageManagerExt.INVALID_KEY_DESC.equals(timeMark)) {
            pattern.append("hh");
            pattern.append(":mm:ss");
            pattern.append(" a");
        } else {
            pattern.append("HH");
            pattern.append(":mm:ss");
        }
        if (!StorageManagerExt.INVALID_KEY_DESC.equals(timeZone)) {
            pattern.append(" Z");
        }
    }

    private void getTimePattern(DatePeriod datePeriod, StringBuffer pattern) {
        DateTime.Time time = datePeriod.getBegin().getTime();
        String timeZone = StorageManagerExt.INVALID_KEY_DESC;
        String timeMark = time != null ? datePeriod.getBegin().getTime().getMark() : timeZone;
        if (datePeriod.getBegin().getTime().getClock() > 12 && !timeZone.equals(timeMark.trim())) {
            timeMark = StorageManagerExt.INVALID_KEY_DESC;
        }
        if (datePeriod.getBegin().getTime() != null) {
            timeZone = datePeriod.getBegin().getTime().getTimezone();
        }
        getTimePattern(pattern, timeMark, timeZone);
    }

    private void getTimeStr(DatePeriod datePeriod, DateOrder type, StringBuffer dateStr) {
        int sec;
        int min;
        int hour;
        if (type == DateOrder.BEGIN) {
            hour = datePeriod.getBegin().getTime() != null ? datePeriod.getBegin().getTime().getClock() : -1;
            min = datePeriod.getBegin().getTime() != null ? datePeriod.getBegin().getTime().getMinute() : -1;
            sec = datePeriod.getBegin().getTime() != null ? datePeriod.getBegin().getTime().getSecond() : -1;
        } else {
            hour = datePeriod.getEnd().getTime() != null ? datePeriod.getEnd().getTime().getClock() : -1;
            min = datePeriod.getEnd().getTime() != null ? datePeriod.getEnd().getTime().getMinute() : -1;
            sec = datePeriod.getEnd().getTime() != null ? datePeriod.getEnd().getTime().getSecond() : -1;
        }
        String timeMark = getTimeMark(datePeriod, type);
        if (hour > 12 && !StorageManagerExt.INVALID_KEY_DESC.equals(timeMark.trim())) {
            timeMark = StorageManagerExt.INVALID_KEY_DESC;
        }
        String timeZone = getTimeZone(datePeriod);
        dateStr.append(hour != -1 ? Integer.valueOf(hour) : "08");
        dateStr.append(":");
        Object obj = "00";
        dateStr.append(min != -1 ? Integer.valueOf(min) : obj);
        dateStr.append(":");
        if (sec != -1) {
            obj = Integer.valueOf(sec);
        }
        dateStr.append(obj);
        if (!StorageManagerExt.INVALID_KEY_DESC.equals(timeMark)) {
            dateStr.append(HanziToPinyin.Token.SEPARATOR);
            dateStr.append(timeMark);
        }
        if (!StorageManagerExt.INVALID_KEY_DESC.equals(timeZone)) {
            dateStr.append(HanziToPinyin.Token.SEPARATOR);
            dateStr.append(timeZone);
        }
    }

    private void getTimeStr(DatePeriod datePeriod, StringBuffer dateStr) {
        int hour = datePeriod.getBegin().getTime().getClock();
        int min = datePeriod.getBegin().getTime().getMinute();
        DateTime.Time time = datePeriod.getBegin().getTime();
        String timeZone = StorageManagerExt.INVALID_KEY_DESC;
        String timeMark = time != null ? datePeriod.getBegin().getTime().getMark() : timeZone;
        if (hour > 12 && !timeZone.equals(timeMark.trim())) {
            timeMark = StorageManagerExt.INVALID_KEY_DESC;
        }
        DateTime.Time time2 = new DateTime.Time();
        int sec = datePeriod.getBegin().getTime().getSecond();
        time2.setClock(hour);
        time2.setMinute(min);
        time2.setSecond(sec);
        time2.setMark(timeMark);
        if (datePeriod.getBegin().getTime() != null) {
            timeZone = datePeriod.getBegin().getTime().getTimezone();
        }
        time2.setTimezone(timeZone);
        time2.setMarkBefore(false);
        getTimeStr(dateStr, time2);
    }

    private void getTimeStr(StringBuffer dateStr, DateTime.Time time) {
        Object obj = "00";
        dateStr.append(time.getClock() != -1 ? Integer.valueOf(time.getClock()) : obj);
        dateStr.append(":");
        dateStr.append(time.getMinute() != -1 ? Integer.valueOf(time.getMinute()) : obj);
        dateStr.append(":");
        if (time.getSecond() != -1) {
            obj = Integer.valueOf(time.getSecond());
        }
        dateStr.append(obj);
        if (!StorageManagerExt.INVALID_KEY_DESC.equals(time.getMark())) {
            dateStr.append(HanziToPinyin.Token.SEPARATOR);
            dateStr.append(time.getMark());
        }
        if (!StorageManagerExt.INVALID_KEY_DESC.equals(time.getTimezone())) {
            dateStr.append(HanziToPinyin.Token.SEPARATOR);
            dateStr.append(time.getTimezone());
        }
    }

    private Calendar formatCalendar(StringBuffer pattern, StringBuffer parseSource) {
        Date date = formatDate(pattern.toString(), parseSource.toString());
        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
        }
        return calendar;
    }

    private void addYMD(DatePeriod datePeriod, Calendar calendar, long relativeTime, DateOrder type) {
        addYMD(calendar, relativeTime, getYear(datePeriod, type), getMonth(datePeriod, type), getDay(datePeriod, type));
    }

    private void addYMD(DatePeriod datePeriod, Calendar calendar, long relativeTime) {
        addYMD(calendar, relativeTime, datePeriod.getBegin().getDate().getYear(), datePeriod.getBegin().getDate().getMonth(), datePeriod.getBegin().getDate().getDay());
    }

    private void addYMD(Calendar calendar, long relativeTime, int year, int month, int day) {
        Calendar relativeCalendar = Calendar.getInstance();
        relativeCalendar.setTime(new Date(relativeTime));
        if (year != -1) {
            calendar.set(1, year);
        } else {
            calendar.set(1, relativeCalendar.get(1));
        }
        if (month != -1) {
            calendar.set(2, month);
        } else {
            calendar.set(2, relativeCalendar.get(2));
        }
        if (day != -1) {
            calendar.set(5, day);
        } else {
            calendar.set(5, relativeCalendar.get(5));
        }
    }

    private String getTimeMark(DatePeriod datePeriod, DateOrder type) {
        String beginMark = datePeriod.getBegin().getTime() != null ? datePeriod.getBegin().getTime().getMark() : StorageManagerExt.INVALID_KEY_DESC;
        String endMark = datePeriod.getEnd().getTime() != null ? datePeriod.getEnd().getTime().getMark() : StorageManagerExt.INVALID_KEY_DESC;
        int beginYear = getYear(datePeriod, DateOrder.BEGIN);
        int beginMonth = getMonth(datePeriod, DateOrder.BEGIN);
        int beginDay = getDay(datePeriod, DateOrder.BEGIN);
        int endYear = getYear(datePeriod, DateOrder.END);
        int endMonth = getMonth(datePeriod, DateOrder.END);
        int endDay = getDay(datePeriod, DateOrder.END);
        DateTime.Day begin = new DateTime.Day(beginYear, beginMonth, beginDay);
        DateTime.Day end = new DateTime.Day(endYear, endMonth, endDay);
        if (type == DateOrder.BEGIN) {
            return getBeginMark(datePeriod, beginMark, endMark, begin, end);
        }
        return getEndMark(datePeriod, beginMark, endMark, begin, end);
    }

    private String getEndMark(DatePeriod datePeriod, String beginMark, String endTimeMark, DateTime.Day begin, DateTime.Day end) {
        if (!isExitTime(datePeriod.getBegin().getType(), datePeriod.getEnd().getType()) || !StorageManagerExt.INVALID_KEY_DESC.equals(endTimeMark.trim()) || StorageManagerExt.INVALID_KEY_DESC.equals(beginMark.trim())) {
            return endTimeMark;
        }
        return begin.getYear() == end.getYear() && begin.getMonth() == end.getMonth() && begin.getDay() == end.getDay() ? beginMark : endTimeMark;
    }

    private String getBeginMark(DatePeriod datePeriod, String beginTimeMark, String endMark, DateTime.Day begin, DateTime.Day end) {
        boolean isBefore2 = datePeriod.getEnd().getTime() == null || datePeriod.getEnd().getTime().isMarkBefore();
        if (!isExitTime(datePeriod.getBegin().getType(), datePeriod.getEnd().getType()) || !StorageManagerExt.INVALID_KEY_DESC.equals(beginTimeMark.trim()) || StorageManagerExt.INVALID_KEY_DESC.equals(endMark.trim())) {
            return beginTimeMark;
        }
        boolean isBefore = isBefore2;
        if (!(begin.getYear() == end.getYear() && begin.getMonth() == end.getMonth() && begin.getDay() == end.getDay())) {
            isBefore = true;
        }
        return !isBefore ? endMark : beginTimeMark;
    }

    private boolean isExitTime(DatePeriod.DatePeriodType beginStatus, DatePeriod.DatePeriodType endStatus) {
        return beginStatus == DatePeriod.DatePeriodType.TYPE_DATETIME || beginStatus == DatePeriod.DatePeriodType.TYPE_TIME || endStatus == DatePeriod.DatePeriodType.TYPE_DATETIME || endStatus == DatePeriod.DatePeriodType.TYPE_TIME;
    }

    private boolean isExitDates(DatePeriod.DatePeriodType beginStatus, DatePeriod.DatePeriodType endStatus) {
        return (beginStatus == DatePeriod.DatePeriodType.TYPE_DATETIME || beginStatus == DatePeriod.DatePeriodType.TYPE_DATE) && (endStatus == DatePeriod.DatePeriodType.TYPE_DATETIME || endStatus == DatePeriod.DatePeriodType.TYPE_DATE);
    }

    private boolean isBeginExitDate(DatePeriod.DatePeriodType beginStatus, DatePeriod.DatePeriodType endStatus) {
        return (beginStatus == DatePeriod.DatePeriodType.TYPE_DATETIME || beginStatus == DatePeriod.DatePeriodType.TYPE_DATE) && endStatus == DatePeriod.DatePeriodType.TYPE_TIME;
    }

    private boolean isEndEixtDate(DatePeriod.DatePeriodType beginStatus, DatePeriod.DatePeriodType endStatus) {
        return (endStatus == DatePeriod.DatePeriodType.TYPE_DATETIME || endStatus == DatePeriod.DatePeriodType.TYPE_DATE) && beginStatus == DatePeriod.DatePeriodType.TYPE_TIME;
    }

    private int getDay(DatePeriod datePeriod, DateOrder type) {
        int day = datePeriod.getBegin().getDate() != null ? datePeriod.getBegin().getDate().getDay() : -1;
        int day2 = datePeriod.getEnd().getDate() != null ? datePeriod.getEnd().getDate().getDay() : -1;
        return type == DateOrder.END ? (!isBeginExitDate(datePeriod.getBegin().getType(), datePeriod.getEnd().getType()) || day == -1 || day2 != -1) ? day2 : day : (!isEndEixtDate(datePeriod.getBegin().getType(), datePeriod.getEnd().getType()) || day2 == -1 || day != -1) ? day : day2;
    }

    private int getMonth(DatePeriod datePeriod, DateOrder type) {
        int month = datePeriod.getBegin().getDate() != null ? datePeriod.getBegin().getDate().getMonth() : -1;
        int month2 = datePeriod.getEnd().getDate() != null ? datePeriod.getEnd().getDate().getMonth() : -1;
        if (type == DateOrder.BEGIN) {
            if (isExitDates(datePeriod.getBegin().getType(), datePeriod.getEnd().getType()) && month == -1 && month2 != -1) {
                month = month2;
            }
            return (!isEndEixtDate(datePeriod.getBegin().getType(), datePeriod.getEnd().getType()) || month2 == -1 || month != -1) ? month : month2;
        }
        if (isExitDates(datePeriod.getBegin().getType(), datePeriod.getEnd().getType()) && month != -1 && month2 == -1) {
            month2 = month;
        }
        return (!isBeginExitDate(datePeriod.getBegin().getType(), datePeriod.getEnd().getType()) || month == -1 || month2 != -1) ? month2 : month;
    }

    private int getYear(DatePeriod datePeriod, DateOrder type) {
        int year = datePeriod.getBegin().getDate() != null ? datePeriod.getBegin().getDate().getYear() : -1;
        int year2 = datePeriod.getEnd().getDate() != null ? datePeriod.getEnd().getDate().getYear() : -1;
        if (type == DateOrder.BEGIN) {
            if (isExitDates(datePeriod.getBegin().getType(), datePeriod.getEnd().getType()) && year == -1 && year2 != -1) {
                year = year2;
            }
            return (!isEndEixtDate(datePeriod.getBegin().getType(), datePeriod.getEnd().getType()) || year2 == -1 || year != -1) ? year : year2;
        }
        if (isExitDates(datePeriod.getBegin().getType(), datePeriod.getEnd().getType()) && year != -1 && year2 == -1) {
            year2 = year;
        }
        return (!isBeginExitDate(datePeriod.getBegin().getType(), datePeriod.getEnd().getType()) || year == -1 || year2 != -1) ? year2 : year;
    }

    private void covertDate(DatePeriod datePeriod, long relativeTime, List<Date> result) {
        Calendar calendar = Calendar.getInstance();
        addYMD(datePeriod, calendar, relativeTime);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        result.add(calendar.getTime());
    }

    private void covertDateTime(DatePeriod datePeriod, long relativeTime, List<Date> result) {
        StringBuffer pattern = new StringBuffer();
        getTimePattern(datePeriod, pattern);
        StringBuffer dateStr = new StringBuffer();
        getTimeStr(datePeriod, dateStr);
        Calendar calendar = formatCalendar(pattern, dateStr);
        addYMD(datePeriod, calendar, relativeTime);
        result.add(calendar.getTime());
    }

    private void covertTime(DatePeriod datePeriod, long relativeTime, List<Date> result) {
        result.add(formatDate(getDateTimePattern(datePeriod), getDateTimeStr(datePeriod, relativeTime)));
    }

    private Date formatDate(String pattern, String dateStr) {
        try {
            return new SimpleDateFormat(pattern, Locale.ENGLISH).parse(dateStr);
        } catch (ParseException e) {
            Log.e(TAG, "formatDate ParseException");
            return null;
        }
    }

    private String getDateTimePattern(DatePeriod datePeriod) {
        StringBuffer pattern = new StringBuffer();
        pattern.append("yyyy-MM-dd ");
        getTimePattern(datePeriod, pattern);
        return pattern.toString();
    }

    private String getDateTimeStr(DatePeriod datePeriod, long relativeTime) {
        Calendar relativeCalendar = Calendar.getInstance();
        relativeCalendar.setTime(new Date(relativeTime));
        StringBuffer dateStr = new StringBuffer();
        dateStr.append(relativeCalendar.get(1));
        dateStr.append("-");
        dateStr.append(relativeCalendar.get(2) + 1);
        dateStr.append("-");
        dateStr.append(relativeCalendar.get(5));
        dateStr.append(HanziToPinyin.Token.SEPARATOR);
        getTimeStr(datePeriod, dateStr);
        return dateStr.toString();
    }
}

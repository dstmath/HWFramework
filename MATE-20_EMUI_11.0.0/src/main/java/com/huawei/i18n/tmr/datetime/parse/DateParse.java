package com.huawei.i18n.tmr.datetime.parse;

import android.util.Log;
import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.i18n.tmr.datetime.RuleInit;
import com.huawei.i18n.tmr.datetime.data.LocaleParamGetZhHans;
import com.huawei.i18n.tmr.datetime.utils.DataConvertTool;
import com.huawei.i18n.tmr.datetime.utils.DatePeriod;
import com.huawei.i18n.tmr.datetime.utils.DateTime;
import com.huawei.i18n.tmr.datetime.utils.StringConvert;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateParse {
    private static final HashMap<Integer, Integer> NAME_TO_METHOD = new HashMap<Integer, Integer>() {
        /* class com.huawei.i18n.tmr.datetime.parse.DateParse.AnonymousClass1 */

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
            put(31018, 10);
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
    private static final String TAG = "DateParse";
    private String locale;
    private String localeBackup;
    private RuleInit rules;

    public DateParse(String locale2, String localeBackup2, RuleInit rules2) {
        this.locale = locale2;
        this.localeBackup = localeBackup2;
        this.rules = rules2;
    }

    public Optional<DatePeriod> parse(String content, String key, long defaultTime) {
        Integer name = Integer.valueOf(key);
        if (!NAME_TO_METHOD.containsKey(name)) {
            return Optional.empty();
        }
        Integer method = NAME_TO_METHOD.get(name);
        DateTime dateTime = null;
        if (method.equals(2)) {
            dateTime = parseWeek(content, name, defaultTime);
        } else if (method.equals(3)) {
            dateTime = parseED(content, name);
        } else {
            DateTime dateTime2 = null;
            if (method.equals(4)) {
                if (parseDMMMY(content).isPresent()) {
                    dateTime2 = parseDMMMY(content).get();
                }
                dateTime = dateTime2;
            } else if (method.equals(5)) {
                Optional<DateTime> optionalDateTime = parseYMMMD(content);
                if (optionalDateTime.isPresent()) {
                    dateTime2 = optionalDateTime.get();
                }
                dateTime = dateTime2;
            } else if (method.equals(6)) {
                if (parseMMMDY(content).isPresent()) {
                    dateTime2 = parseMMMDY(content).get();
                }
                dateTime = dateTime2;
            } else if (method.equals(7)) {
                dateTime = parseYMD(content, name);
            } else if (method.equals(8)) {
                dateTime = parseTime(content, name);
            } else if (method.equals(9)) {
                dateTime = parseAH(content, name);
            } else if (method.equals(10)) {
                dateTime = parseAHMZ(content, name);
            } else if (method.equals(12)) {
                dateTime = parseE(content, defaultTime);
            } else if (method.equals(1)) {
                dateTime = parseDay(content, name, defaultTime);
            } else if (method.equals(20)) {
                dateTime = parseZAHM(content, name, defaultTime);
            } else if (method.equals(21)) {
                dateTime = parseZhYMDE(content, name, defaultTime);
            } else if (method.equals(22)) {
                if (parseFullEU(content, name, defaultTime).isPresent()) {
                    dateTime2 = parseFullEU(content, name, defaultTime).get();
                }
                dateTime = dateTime2;
            } else if (method.equals(23)) {
                dateTime = parseMyE(content, defaultTime);
            } else if (method.equals(24)) {
                if (parseYDMMM(content, defaultTime).isPresent()) {
                    dateTime2 = parseYDMMM(content, defaultTime).get();
                }
                dateTime = dateTime2;
            } else if (method.equals(25)) {
                if (parseBOYMMMD(content).isPresent()) {
                    dateTime2 = parseBOYMMMD(content).get();
                }
                dateTime = dateTime2;
            } else if (method.equals(27)) {
                dateTime = parseBOZAHM(content);
            } else if (method.equals(29)) {
                dateTime = parseAMPM(content);
            }
        }
        return getDatePeriod(content, name, method, dateTime);
    }

    private Optional<DatePeriod> getDatePeriod(String content, Integer name, Integer method, DateTime dateTime) {
        DatePeriod datePeriod = null;
        if (dateTime != null) {
            datePeriod = new DatePeriod(dateTime);
        }
        DatePeriod datePeriod2 = null;
        if (method.equals(13)) {
            if (parseDurMMMDY(content, name).isPresent()) {
                datePeriod2 = parseDurMMMDY(content, name).get();
            }
            datePeriod = datePeriod2;
        } else if (method.equals(14)) {
            if (parseDateDurDmy2(content, name).isPresent()) {
                datePeriod2 = parseDateDurDmy2(content, name).get();
            }
            datePeriod = datePeriod2;
        } else if (method.equals(15)) {
            if (parseDateDurYMD(content, name).isPresent()) {
                datePeriod2 = parseDateDurYMD(content, name).get();
            }
            datePeriod = datePeriod2;
        } else if (method.equals(16)) {
            if (parseDateDurYMD2(content, name).isPresent()) {
                datePeriod2 = parseDateDurYMD2(content, name).get();
            }
            datePeriod = datePeriod2;
        } else if (method.equals(18)) {
            if (parseDurMMMDY2(content, name).isPresent()) {
                datePeriod2 = parseDurMMMDY2(content, name).get();
            }
            datePeriod = datePeriod2;
        } else if (method.equals(26)) {
            if (parseBoDurYMMMD(content, name).isPresent()) {
                datePeriod2 = parseBoDurYMMMD(content, name).get();
            }
            datePeriod = datePeriod2;
        } else if (method.equals(28)) {
            if (parseLVDurYDDMMM(content, name).isPresent()) {
                datePeriod2 = parseLVDurYDDMMM(content, name).get();
            }
            datePeriod = datePeriod2;
        }
        return Optional.ofNullable(datePeriod);
    }

    private DateTime parseAMPM(String content) {
        String time = new LocaleParamGetZhHans().getAmPm(content);
        if (time == null || StorageManagerExt.INVALID_KEY_DESC.equals(time.trim())) {
            time = "08:00";
        }
        int hour = 8;
        int min = 0;
        try {
            String hs = time.substring(0, 2);
            String ms = time.substring(3, 5);
            hour = Integer.parseInt(hs);
            min = Integer.parseInt(ms);
        } catch (NumberFormatException e) {
            Log.e(TAG, "parseAMPM Error");
        }
        DateTime.Time tempTime = new DateTime.Time();
        tempTime.setClock(hour);
        tempTime.setMinute(min);
        tempTime.setSecond(0);
        tempTime.setMark(StorageManagerExt.INVALID_KEY_DESC);
        tempTime.setTimezone(StorageManagerExt.INVALID_KEY_DESC);
        tempTime.setMarkBefore(true);
        DateTime dateTime = new DateTime();
        dateTime.setTime(tempTime, true);
        return dateTime;
    }

    private Optional<DatePeriod> parseLVDurYDDMMM(String content, Integer name) {
        String day1Str = null;
        String day2Str = null;
        String monthStr = null;
        String yearStr = null;
        Matcher matcher = this.rules.getDetectByKey(41005).matcher(content);
        if (matcher.find()) {
            day1Str = matcher.group(6);
            day2Str = matcher.group(8);
            monthStr = matcher.group(9);
            yearStr = matcher.group(3);
        }
        int day1 = day1Str == null ? -1 : Integer.parseInt(day1Str.trim());
        int day2 = day2Str == null ? -1 : Integer.parseInt(day2Str.trim());
        String month = monthStr == null ? StorageManagerExt.INVALID_KEY_DESC : monthStr.trim();
        int year = yearStr == null ? -1 : Integer.parseInt(yearStr.trim());
        int num = DataConvertTool.convertMMM(month, this.locale, this.localeBackup);
        if (day1 == -1 || day2 == -1 || num == -1) {
            return Optional.empty();
        }
        DateTime begin = new DateTime();
        begin.setDay(year, num, day1);
        DateTime end = new DateTime();
        end.setDay(year, num, day2);
        return Optional.of(new DatePeriod(begin, end));
    }

    private DateTime parseBOZAHM(String str) {
        Matcher match = this.rules.getParseByKey(913).matcher(DataConvertTool.replace(str, this.locale, this.localeBackup));
        DateTime dateTime = new DateTime();
        if (match.find()) {
            String zone = match.group(2);
            String gmt = StorageManagerExt.INVALID_KEY_DESC;
            if (zone != null && !zone.trim().isEmpty()) {
                gmt = handleZ(zone);
            }
            String sec = "00";
            String hour = match.group(8) != null ? match.group(8) : sec;
            String min = match.group(9) != null ? match.group(9) : sec;
            if (match.group(11) != null) {
                sec = match.group(11);
            }
            String am = match.group(7) != null ? match.group(7) : StorageManagerExt.INVALID_KEY_DESC;
            DateTime.Time time = new DateTime.Time();
            time.setClock(Integer.parseInt(hour));
            time.setMinute(Integer.parseInt(min));
            time.setSecond(Integer.parseInt(sec));
            time.setMark(am);
            time.setTimezone(gmt);
            time.setMarkBefore(true);
            dateTime.setTime(time, true);
        }
        return dateTime;
    }

    private Optional<DatePeriod> parseBoDurYMMMD(String content, Integer name) {
        String dayStr = null;
        String day2Str = null;
        String monthStr = null;
        String yearStr = null;
        Matcher matcher = this.rules.getDetectByKey(41003).matcher(content);
        if (matcher.find()) {
            dayStr = matcher.group(7);
            day2Str = matcher.group(9);
            monthStr = matcher.group(5);
            yearStr = matcher.group(3);
        }
        int day1 = dayStr == null ? -1 : Integer.parseInt(dayStr.trim());
        int day2 = day2Str == null ? -1 : Integer.parseInt(day2Str.trim());
        String ms = monthStr == null ? StorageManagerExt.INVALID_KEY_DESC : monthStr.trim();
        int year = yearStr == null ? -1 : Integer.parseInt(yearStr.trim());
        int num = DataConvertTool.convertMMM(ms, this.locale, this.localeBackup);
        if (day1 == -1 || day2 == -1 || num == -1) {
            return Optional.empty();
        }
        DateTime begin = new DateTime();
        begin.setDay(year, num, day1);
        DateTime end = new DateTime();
        end.setDay(year, num, day2);
        return Optional.of(new DatePeriod(begin, end));
    }

    private Optional<DateTime> parseBOYMMMD(String content) {
        int year;
        int month = -1;
        int day = -1;
        Matcher match = this.rules.getDetectByKey(21024).matcher(content);
        if (match.find()) {
            String yearOne = match.group(4);
            String yearTwo = match.group(9);
            String month1 = match.group(6);
            String month2 = match.group(11);
            String dayStr = match.group(12);
            String yearStr = null;
            String monthStr = null;
            if (yearOne != null) {
                yearStr = yearOne;
            } else if (yearTwo != null) {
                yearStr = yearTwo;
            }
            if (month1 != null) {
                monthStr = month1;
            } else if (month2 != null) {
                monthStr = month2;
            }
            day = Integer.parseInt(dayStr.trim());
            month = DataConvertTool.convertMMM(monthStr, this.locale, this.localeBackup);
            if (yearStr == null || StorageManagerExt.INVALID_KEY_DESC.equals(yearStr.trim())) {
                year = -1;
            } else {
                int ty = Integer.parseInt(yearStr.trim());
                if (ty >= 100 || ty <= -1) {
                    year = ty;
                } else {
                    year = ty + 2000;
                }
            }
        } else {
            year = -1;
        }
        if (month == -1) {
            return Optional.empty();
        }
        DateTime date = new DateTime();
        date.setDay(year, month, day);
        return Optional.of(date);
    }

    private Optional<DateTime> parseYDMMM(String content, long defaultTime) {
        Matcher matcher = this.rules.getParseByKey(926).matcher(content);
        int year = -1;
        int month = -1;
        int day = -1;
        if (matcher.find()) {
            String dayStr = "-1";
            String yearStr = matcher.group(3) != null ? matcher.group(3) : dayStr;
            String monthStr = matcher.group(7) != null ? matcher.group(7) : dayStr;
            if (matcher.group(6) != null) {
                dayStr = matcher.group(6);
            }
            day = Integer.parseInt(dayStr.trim());
            month = DataConvertTool.convertMMM(monthStr, this.locale, this.localeBackup);
            if (yearStr != null && !StorageManagerExt.INVALID_KEY_DESC.equals(yearStr.trim())) {
                int ty = Integer.parseInt(yearStr.trim());
                year = (ty >= 100 || ty <= -1) ? ty : ty + 2000;
            }
        }
        if (month == -1) {
            return Optional.empty();
        }
        DateTime date = new DateTime();
        date.setDay(year, month, day);
        return Optional.of(date);
    }

    private DateTime parseMyE(String str, long defaultTime) {
        return parseE(str.replaceAll("နေ့၊", StorageManagerExt.INVALID_KEY_DESC), defaultTime);
    }

    private Optional<DateTime> parseFullEU(String content, Integer name, long defaultTime) {
        Matcher matcher = this.rules.getParseByKey(925).matcher(content);
        int year = -1;
        int month = -1;
        int day = -1;
        if (matcher.find()) {
            String dayStr = "-1";
            String yearStr = matcher.group(3) != null ? matcher.group(3) : dayStr;
            String monthStr = matcher.group(5) != null ? matcher.group(5) : dayStr;
            if (matcher.group(7) != null) {
                dayStr = matcher.group(7);
            }
            day = Integer.parseInt(dayStr.trim());
            month = DataConvertTool.convertMMM(monthStr, this.locale, this.localeBackup);
            if (yearStr != null && !StorageManagerExt.INVALID_KEY_DESC.equals(yearStr.trim())) {
                int ty = Integer.parseInt(yearStr.trim());
                year = (ty >= 100 || ty <= -1) ? ty : ty + 2000;
            }
        }
        if (month == -1) {
            return Optional.empty();
        }
        DateTime date = new DateTime();
        date.setDay(year, month, day);
        return Optional.of(date);
    }

    private DateTime parseZhYMDE(String content, Integer name, long defaultTime) {
        int year;
        int i;
        int add;
        Matcher matcher = this.rules.getDetectByKey(21014).matcher(content);
        DateTime dateTime = new DateTime();
        if (matcher.find()) {
            String maDay = "-1";
            String maYear = matcher.group(2) != null ? matcher.group(2) : maDay;
            String maMonth = matcher.group(6) != null ? matcher.group(6) : maDay;
            int day = -1;
            StringConvert stringConvert = new StringConvert();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(defaultTime));
            if (maDay.equals(maYear)) {
                year = -1;
                i = 1;
            } else if (stringConvert.isDigit(maYear, this.locale)) {
                year = Integer.parseInt(stringConvert.convertDigit(maYear, this.locale));
                i = 1;
            } else {
                i = 1;
                year = cal.get(1) + DataConvertTool.convertRelText(maYear, this.locale, "param_textyear", this.localeBackup);
            }
            if (stringConvert.isDigit(maMonth, this.locale)) {
                add = Integer.parseInt(stringConvert.convertDigit(maMonth, this.locale)) - i;
            } else {
                add = cal.get(2) + DataConvertTool.convertRelText(maMonth, this.locale, "param_textmonth", this.localeBackup);
            }
            if (matcher.group(8) != null) {
                maDay = matcher.group(8);
            }
            if (stringConvert.isDigit(maDay, this.locale)) {
                day = Integer.parseInt(stringConvert.convertDigit(maDay, this.locale));
            }
            dateTime.setDay(year, add, day);
        }
        return dateTime;
    }

    private DateTime parseDay(String content, Integer name, long defaultTime) {
        int add = DataConvertTool.calTextDay(content, this.locale, this.localeBackup);
        DateTime dateTime = new DateTime();
        if (add == -1) {
            return dateTime;
        }
        dateTime.setDayByAddDays(add, defaultTime);
        return dateTime;
    }

    private DateTime parseAH(String str, Integer name) {
        Matcher match = this.rules.getParseByKey(912).matcher(DataConvertTool.replace(str, this.locale, this.localeBackup));
        if (!match.find()) {
            return null;
        }
        String am = match.group(2);
        String ah = match.group(3);
        StringBuffer sb = new StringBuffer();
        sb.append(ah.trim());
        sb.append(":00");
        return parseHMS(sb.toString(), am != null ? am.trim() : StorageManagerExt.INVALID_KEY_DESC);
    }

    private DateTime parseE(String str, long defaultTime) {
        DateTime dateTime = new DateTime();
        if (str == null || StorageManagerExt.INVALID_KEY_DESC.equals(str.trim())) {
            return dateTime;
        }
        dateTime.setDayByWeekValue(DataConvertTool.convertE(str.replace("(", StorageManagerExt.INVALID_KEY_DESC).replace(")", StorageManagerExt.INVALID_KEY_DESC), this.locale, this.localeBackup), defaultTime);
        return dateTime;
    }

    private DateTime parseAHMZ(String str, Integer name) {
        String content = DataConvertTool.replace(str, this.locale, this.localeBackup);
        Pattern pattern = this.rules.getParseByKey(908);
        if (name.intValue() == 31007) {
            pattern = this.rules.getParseByKey(909);
        }
        Matcher match = pattern.matcher(content);
        DateTime dateTime = new DateTime();
        if (match.find()) {
            String z2 = match.group(6);
            String zone = StorageManagerExt.INVALID_KEY_DESC;
            if (z2 != null && !z2.trim().isEmpty()) {
                zone = z2;
            }
            String gmt = StorageManagerExt.INVALID_KEY_DESC;
            if (!zone.trim().isEmpty()) {
                gmt = handleZ(zone);
            }
            String am = match.group(1) != null ? match.group(1) : StorageManagerExt.INVALID_KEY_DESC;
            String sec = "00";
            String hour = match.group(2) != null ? match.group(2) : sec;
            String min = match.group(3) != null ? match.group(3) : sec;
            if (match.group(5) != null) {
                sec = match.group(5);
            }
            DateTime.Time time = new DateTime.Time();
            time.setClock(Integer.parseInt(hour));
            time.setMinute(Integer.parseInt(min));
            time.setSecond(Integer.parseInt(sec));
            time.setMark(am);
            time.setTimezone(gmt);
            time.setMarkBefore(true);
            dateTime.setTime(time, true);
        }
        return dateTime;
    }

    private DateTime parseZAHM(String str, Integer name, long defaultTime) {
        String content = DataConvertTool.replace(str, this.locale, this.localeBackup);
        StringConvert stringConvert = new StringConvert();
        Matcher match = this.rules.getParseByKey(910).matcher(content);
        DateTime dateTime = new DateTime();
        if (match.find()) {
            String group2 = match.group(2);
            if (group2 != null && !group2.trim().isEmpty()) {
                dateTime.setDayByWeekValue(DataConvertTool.convertE(group2, this.locale, this.localeBackup), defaultTime);
            }
            String zoneOne = match.group(4);
            String zone = StorageManagerExt.INVALID_KEY_DESC;
            if (zoneOne != null && !zoneOne.trim().isEmpty()) {
                zone = zoneOne;
            }
            String sec = "00";
            String hour = match.group(10) != null ? match.group(10) : sec;
            String min = match.group(12) != null ? match.group(12) : sec;
            if (match.group(14) != null) {
                sec = match.group(14);
            }
            String gmt = StorageManagerExt.INVALID_KEY_DESC;
            if (!zone.trim().isEmpty()) {
                gmt = handleZ(zone);
            }
            String am = match.group(9) != null ? match.group(9) : StorageManagerExt.INVALID_KEY_DESC;
            DateTime.Time time = new DateTime.Time();
            time.setClock(Integer.parseInt(stringConvert.convertDigit(hour, this.locale)));
            time.setMinute(Integer.parseInt(stringConvert.convertDigit(min, this.locale)));
            time.setSecond(Integer.parseInt(stringConvert.convertDigit(sec, this.locale)));
            time.setMark(am);
            time.setTimezone(gmt);
            time.setMarkBefore(true);
            dateTime.setTime(time, true);
        }
        return dateTime;
    }

    private DateTime parseWeek(String content, Integer name, long defaultTime) {
        int rel;
        DateTime dateTime = new DateTime();
        if (content == null || content.trim().isEmpty() || (rel = DataConvertTool.calRelDays(content, this.locale, this.localeBackup)) == -1) {
            return dateTime;
        }
        dateTime.setDayByWeekValue(rel, defaultTime);
        return dateTime;
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
        }
        DateTime.Time time = new DateTime.Time();
        time.setClock(Integer.parseInt(hs));
        time.setMinute(Integer.parseInt(ms));
        time.setSecond(Integer.parseInt(ss));
        time.setMark(ampm != null ? ampm : StorageManagerExt.INVALID_KEY_DESC);
        time.setTimezone(StorageManagerExt.INVALID_KEY_DESC);
        time.setMarkBefore(true);
        DateTime dateTime = new DateTime();
        dateTime.setTime(time, true);
        return dateTime;
    }

    private DateTime parseED(String str, Integer name) {
        Matcher match = this.rules.getParseByKey(906).matcher(new StringConvert().convertDigit(str, this.locale));
        String dayStr = "0";
        if (match.find()) {
            dayStr = match.group(1);
        }
        int day = Integer.parseInt(dayStr.trim());
        DateTime date = new DateTime();
        date.setDay(-1, -1, day);
        return date;
    }

    private DateTime parseYMD(String content, Integer name) {
        Pattern parseYmd1 = this.rules.getParseByKey(904);
        Pattern parseYmd2 = this.rules.getParseByKey(905);
        Matcher matchYmd1 = parseYmd1.matcher(content);
        Matcher matchYmd2 = parseYmd2.matcher(content);
        if (matchYmd1.find()) {
            return getYmd1DateTime(name, matchYmd1);
        }
        if (matchYmd2.find()) {
            return getYmd2DateTime(name, matchYmd2);
        }
        return new DateTime();
    }

    private DateTime getYmd2DateTime(Integer name, Matcher matchYmd2) {
        String monthStr;
        String dayStr;
        if (name.intValue() == 20016 || name.intValue() == 21002) {
            dayStr = matchYmd2.group(2);
            monthStr = matchYmd2.group(1);
        } else if (name.intValue() == 20015 || name.intValue() == 21001 || name.intValue() == 21014 || name.intValue() == 21015) {
            dayStr = matchYmd2.group(2);
            monthStr = matchYmd2.group(1);
        } else {
            dayStr = matchYmd2.group(1);
            monthStr = matchYmd2.group(2);
        }
        if (dayStr == null || monthStr == null) {
            return new DateTime();
        }
        return getDateTime(dayStr, monthStr, "-1");
    }

    private DateTime getYmd1DateTime(Integer name, Matcher matchYmd1) {
        String monthStr;
        String monthStr2;
        String dayStr;
        if (name.intValue() == 20016 || name.intValue() == 21015 || name.intValue() == 21002 || name.intValue() == 21039) {
            dayStr = matchYmd1.group(3);
            String monthStr3 = matchYmd1.group(2);
            monthStr = matchYmd1.group(1);
            monthStr2 = monthStr3;
        } else if (name.intValue() == 20015 || name.intValue() == 21001) {
            dayStr = matchYmd1.group(2);
            monthStr2 = matchYmd1.group(1);
            monthStr = matchYmd1.group(3);
        } else {
            dayStr = matchYmd1.group(1);
            monthStr2 = matchYmd1.group(2);
            monthStr = matchYmd1.group(3);
        }
        if (dayStr == null || monthStr2 == null || monthStr == null) {
            return new DateTime();
        }
        return getDateTime(dayStr, monthStr2, monthStr);
    }

    private DateTime getDateTime(String dayStr, String monthStr, String yearStr) {
        int day = Integer.parseInt(dayStr.trim());
        int month = Integer.parseInt(monthStr.trim()) - 1;
        int year = Integer.parseInt(yearStr.trim());
        if (year < 100 && year > -1) {
            year += 2000;
        }
        DateTime date = new DateTime();
        date.setDay(year, month, day);
        return date;
    }

    private Optional<DateTime> parseDMMMY(String content) {
        int year = -1;
        int month = -1;
        int day = -1;
        Matcher match = this.rules.getParseByKey(903).matcher(content);
        if (match.find()) {
            String dayStr = match.group(1);
            String monthStr = match.group(2);
            String yearStr = match.group(4);
            day = Integer.parseInt(dayStr.trim());
            month = DataConvertTool.convertMMM(monthStr, this.locale, this.localeBackup);
            if (yearStr != null && !StorageManagerExt.INVALID_KEY_DESC.equals(yearStr.trim())) {
                int ty = Integer.parseInt(yearStr.trim());
                year = (ty >= 100 || ty <= -1) ? ty : ty + 2000;
            }
        }
        if (month == -1) {
            return Optional.empty();
        }
        DateTime date = new DateTime();
        date.setDay(year, month, day);
        return Optional.of(date);
    }

    private Optional<DateTime> parseYMMMD(String content) {
        int year = -1;
        int month = -1;
        int day = -1;
        Matcher match = this.rules.getParseByKey(901).matcher(content);
        if (match.find()) {
            String dayStr = match.group(5);
            String monthStr = match.group(4);
            String yearStr = match.group(2);
            day = Integer.parseInt(dayStr.trim());
            month = DataConvertTool.convertMMM(monthStr, this.locale, this.localeBackup);
            if (yearStr != null && !StorageManagerExt.INVALID_KEY_DESC.equals(yearStr.trim())) {
                int ty = Integer.parseInt(yearStr.trim());
                year = (ty >= 100 || ty <= -1) ? ty : ty + 2000;
            }
        }
        if (month == -1) {
            return Optional.empty();
        }
        DateTime date = new DateTime();
        date.setDay(year, month, day);
        return Optional.of(date);
    }

    private Optional<DateTime> parseMMMDY(String content) {
        int year = -1;
        int month = -1;
        int day = -1;
        Matcher match = this.rules.getParseByKey(902).matcher(content);
        if (match.find()) {
            String dayStr = match.group(2);
            String monthStr = match.group(1);
            String yearStr = match.group(4);
            day = Integer.parseInt(dayStr.trim());
            month = DataConvertTool.convertMMM(monthStr, this.locale, this.localeBackup);
            if (yearStr != null && !StorageManagerExt.INVALID_KEY_DESC.equals(yearStr.trim())) {
                int ty = Integer.parseInt(yearStr.trim());
                if (ty < 100 && ty > -1) {
                    ty += 2000;
                }
                year = ty;
            }
        }
        if (month == -1) {
            return Optional.empty();
        }
        DateTime date = new DateTime();
        date.setDay(year, month, day);
        return Optional.of(date);
    }

    private DateTime parseTime(String str, Integer name) {
        DateTime dateTime = new DateTime();
        String content = DataConvertTool.replace(str, this.locale, this.localeBackup);
        boolean isBefore = false;
        if (name.equals(31001)) {
            isBefore = true;
        }
        Matcher matcher = this.rules.getParseByKey(907).matcher(content);
        if (matcher.find()) {
            String amPmOne = matcher.group(1) != null ? matcher.group(1) : StorageManagerExt.INVALID_KEY_DESC;
            String amPmTwo = matcher.group(6) != null ? matcher.group(6) : StorageManagerExt.INVALID_KEY_DESC;
            String zstr = matcher.group(7) != null ? matcher.group(7) : StorageManagerExt.INVALID_KEY_DESC;
            String am = StorageManagerExt.INVALID_KEY_DESC;
            if (!StorageManagerExt.INVALID_KEY_DESC.equals(amPmOne)) {
                am = amPmOne;
            } else if (!StorageManagerExt.INVALID_KEY_DESC.equals(amPmTwo)) {
                am = amPmTwo;
            }
            String gmt = StorageManagerExt.INVALID_KEY_DESC;
            if (zstr != null && !zstr.trim().isEmpty()) {
                gmt = handleZ(zstr);
            }
            String sstr = "00";
            String hstr = matcher.group(2) != null ? matcher.group(2) : sstr;
            String monthStr = matcher.group(3) != null ? matcher.group(3) : sstr;
            if (matcher.group(5) != null) {
                sstr = matcher.group(5);
            }
            DateTime.Time time = new DateTime.Time();
            time.setClock(Integer.parseInt(hstr));
            time.setMinute(Integer.parseInt(monthStr));
            time.setSecond(Integer.parseInt(sstr));
            time.setMark(am);
            time.setTimezone(gmt);
            time.setMarkBefore(isBefore);
            dateTime.setTime(time, true);
        }
        return dateTime;
    }

    private String handleZ(String context) {
        StringBuffer gmt = new StringBuffer();
        Matcher matcher = Pattern.compile("(GMT){0,1}([+-])([0-1][0-9]|2[0-3]):{0,1}([0-5][0-9]|60)").matcher(context);
        if (matcher.find()) {
            String hs = matcher.group(3);
            if (hs == null || hs.trim().isEmpty()) {
                hs = "00";
            } else if (hs.trim().length() < 2) {
                hs = "0" + hs.trim();
            }
            String ms = matcher.group(4);
            if (ms == null || ms.trim().isEmpty()) {
                ms = "00";
            } else if (ms.trim().length() < 2) {
                ms = "0" + ms.trim();
            }
            gmt.append(matcher.group(2));
            gmt.append(hs);
            gmt.append(ms);
            return gmt.toString();
        }
        Matcher gmtMatcher = Pattern.compile("(GMT){0,1}([+-])(1{0,1}[0-9]|2[0-3]):?([0-5][0-9]|60){0,1}").matcher(context);
        if (gmtMatcher.find()) {
            String hs2 = gmtMatcher.group(3);
            if (hs2 == null || hs2.trim().isEmpty()) {
                hs2 = "00";
            } else if (hs2.trim().length() < 2) {
                hs2 = "0" + hs2.trim();
            }
            String ms2 = gmtMatcher.group(4);
            if (ms2 == null || ms2.trim().isEmpty()) {
                ms2 = "00";
            } else if (ms2.trim().length() < 2) {
                ms2 = "0" + ms2.trim();
            }
            gmt.append(gmtMatcher.group(2));
            gmt.append(hs2);
            gmt.append(ms2);
        }
        return gmt.toString();
    }

    private Optional<DatePeriod> parseDurMMMDY2(String content, Integer name) {
        String dayStr = null;
        String day2Str = null;
        String monthStr = null;
        String m2str = null;
        String yearStr = null;
        Matcher matcher = this.rules.getParseByKey(917).matcher(content);
        if (matcher.find()) {
            dayStr = matcher.group(1);
            day2Str = matcher.group(3);
            monthStr = matcher.group(2);
            m2str = matcher.group(4);
            yearStr = matcher.group(6);
        }
        int day1 = dayStr == null ? -1 : Integer.parseInt(dayStr.trim());
        int day2 = day2Str == null ? -1 : Integer.parseInt(day2Str.trim());
        String ms2 = StorageManagerExt.INVALID_KEY_DESC;
        String ms = monthStr == null ? ms2 : monthStr.trim();
        if (m2str != null) {
            ms2 = m2str.trim();
        }
        int year = yearStr == null ? -1 : Integer.parseInt(yearStr.trim());
        int month1 = DataConvertTool.convertMMM(ms, this.locale, this.localeBackup);
        int month2 = DataConvertTool.convertMMM(ms2, this.locale, this.localeBackup);
        if (day1 != -1 && day2 != -1 && month1 != -1) {
            if (month2 != -1) {
                DateTime dateTimeBegin = new DateTime();
                dateTimeBegin.setDay(year, month1, day1);
                DateTime dateTimeEnd = new DateTime();
                dateTimeEnd.setDay(year, month2, day2);
                return Optional.of(new DatePeriod(dateTimeBegin, dateTimeEnd));
            }
        }
        return Optional.empty();
    }

    private Optional<DatePeriod> parseDurMMMDY(String content, Integer name) {
        String dayStr = null;
        String day2Str = null;
        String monthStr = null;
        String yearStr = null;
        if (name.intValue() == 40001 || name.intValue() == 41007 || name.intValue() == 41008) {
            Matcher matcher = this.rules.getParseByKey(915).matcher(content);
            if (matcher.find()) {
                dayStr = matcher.group(1);
                day2Str = matcher.group(2);
                monthStr = matcher.group(3);
                yearStr = matcher.group(5);
            }
        } else if (name.intValue() == 40005 || name.intValue() == 41006) {
            Matcher matcher2 = this.rules.getParseByKey(916).matcher(content);
            if (matcher2.find()) {
                dayStr = matcher2.group(2);
                day2Str = matcher2.group(3);
                monthStr = matcher2.group(1);
                yearStr = matcher2.group(4);
            }
        } else if (name.intValue() == 40002 || name.intValue() == 41001 || name.intValue() == 41004) {
            Matcher matcher3 = this.rules.getParseByKey(921).matcher(content);
            if (matcher3.find()) {
                dayStr = matcher3.group(5);
                day2Str = matcher3.group(6);
                monthStr = matcher3.group(4);
                yearStr = matcher3.group(2);
            }
        }
        int day1 = dayStr == null ? -1 : Integer.parseInt(dayStr.trim());
        int day2 = day2Str == null ? -1 : Integer.parseInt(day2Str.trim());
        String ms = monthStr == null ? StorageManagerExt.INVALID_KEY_DESC : monthStr.trim();
        int year = yearStr == null ? -1 : Integer.parseInt(yearStr.trim());
        int num = DataConvertTool.convertMMM(ms, this.locale, this.localeBackup);
        if (day1 == -1 || day2 == -1 || num == -1) {
            return Optional.empty();
        }
        DateTime begin = new DateTime();
        begin.setDay(year, num, day1);
        DateTime end = new DateTime();
        end.setDay(year, num, day2);
        return Optional.of(new DatePeriod(begin, end));
    }

    private Optional<DatePeriod> parseDateDurDmy2(String content, Integer name) {
        Matcher matcher = this.rules.getDetectByKey(40003).matcher(content);
        DatePeriod datePeriod = null;
        if (matcher.find()) {
            String dayStr = matcher.group(2);
            String day2Str = matcher.group(4);
            String monthStr = matcher.group(8);
            String yearStr = matcher.group(11);
            int day1 = dayStr == null ? -1 : Integer.parseInt(dayStr.trim());
            int day2 = day2Str == null ? -1 : Integer.parseInt(day2Str.trim());
            int month1 = monthStr == null ? -1 : Integer.parseInt(monthStr.trim());
            int year = yearStr == null ? -1 : Integer.parseInt(yearStr.trim());
            if (day1 == -1 || day2 == -1 || month1 == -1) {
                return Optional.empty();
            }
            DateTime begin = new DateTime();
            begin.setDay(year, month1 - 1, day1);
            DateTime end = new DateTime();
            end.setDay(year, month1 - 1, day2);
            datePeriod = new DatePeriod(begin, end);
        }
        return Optional.ofNullable(datePeriod);
    }

    private Optional<DatePeriod> parseDateDurYMD(String content, Integer name) {
        Matcher matcher = this.rules.getParseByKey(920).matcher(content);
        DatePeriod datePeriod = null;
        if (matcher.find()) {
            int year = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : -1;
            int month1 = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : -1;
            int day1 = matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : -1;
            int month2 = matcher.group(6) != null ? Integer.parseInt(matcher.group(6)) : -1;
            int day2 = matcher.group(7) != null ? Integer.parseInt(matcher.group(7)) : -1;
            if (day1 == -1 || day2 == -1 || month1 == -1) {
                return Optional.empty();
            }
            DateTime begin = new DateTime();
            begin.setDay(year, month1 - 1, day1);
            DateTime end = new DateTime();
            if (month2 == -1) {
                month2 = month1;
            }
            end.setDay(year, month2 - 1, day2);
            datePeriod = new DatePeriod(begin, end);
        }
        return Optional.ofNullable(datePeriod);
    }

    private Optional<DatePeriod> parseDateDurYMD2(String content, Integer name) {
        Matcher matcher = this.rules.getParseByKey(924).matcher(content);
        DatePeriod datePeriod = null;
        if (matcher.find()) {
            String dayStr = matcher.group(12);
            String day2Str = matcher.group(14);
            String monthStr = matcher.group(8);
            String yearStr = matcher.group(3);
            int beginDay = dayStr == null ? -1 : Integer.parseInt(dayStr.trim());
            int endDay = day2Str == null ? -1 : Integer.parseInt(day2Str.trim());
            int month = monthStr == null ? -1 : Integer.parseInt(monthStr.trim());
            int year = yearStr == null ? -1 : Integer.parseInt(yearStr.trim());
            if (beginDay == -1 || endDay == -1 || month == -1) {
                return Optional.empty();
            }
            DateTime begin = new DateTime();
            begin.setDay(year, month - 1, beginDay);
            DateTime end = new DateTime();
            end.setDay(year, month - 1, endDay);
            datePeriod = new DatePeriod(begin, end);
        }
        return Optional.ofNullable(datePeriod);
    }
}

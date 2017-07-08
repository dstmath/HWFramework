package android.text.format;

import android.util.TimeFormatException;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.telephony.AbstractRILConstants;
import com.android.internal.telephony.RILConstants;
import com.huawei.android.statistical.StatisticalConstant;
import java.io.IOException;
import java.util.Locale;
import java.util.TimeZone;
import libcore.util.ZoneInfo;
import libcore.util.ZoneInfo.WallTime;
import libcore.util.ZoneInfoDB;

@Deprecated
public class Time {
    private static final int[] DAYS_PER_MONTH = null;
    public static final int EPOCH_JULIAN_DAY = 2440588;
    public static final int FRIDAY = 5;
    public static final int HOUR = 3;
    public static final int MINUTE = 2;
    public static final int MONDAY = 1;
    public static final int MONDAY_BEFORE_JULIAN_EPOCH = 2440585;
    public static final int MONTH = 5;
    public static final int MONTH_DAY = 4;
    public static final int SATURDAY = 6;
    public static final int SECOND = 1;
    public static final int SUNDAY = 0;
    public static final int THURSDAY = 4;
    public static final String TIMEZONE_UTC = "UTC";
    public static final int TUESDAY = 2;
    public static final int WEDNESDAY = 3;
    public static final int WEEK_DAY = 7;
    public static final int WEEK_NUM = 9;
    public static final int YEAR = 6;
    public static final int YEAR_DAY = 8;
    private static final String Y_M_D = "%Y-%m-%d";
    private static final String Y_M_D_T_H_M_S_000 = "%Y-%m-%dT%H:%M:%S.000";
    private static final String Y_M_D_T_H_M_S_000_Z = "%Y-%m-%dT%H:%M:%S.000Z";
    private static final int[] sThursdayOffset = null;
    public boolean allDay;
    private TimeCalculator calculator;
    public long gmtoff;
    public int hour;
    public int isDst;
    public int minute;
    public int month;
    public int monthDay;
    public int second;
    public String timezone;
    public int weekDay;
    public int year;
    public int yearDay;

    private static class TimeCalculator {
        public String timezone;
        public final WallTime wallTime;
        private ZoneInfo zoneInfo;

        public TimeCalculator(String timezoneId) {
            this.zoneInfo = lookupZoneInfo(timezoneId);
            this.wallTime = new WallTime();
        }

        public long toMillis(boolean ignoreDst) {
            if (ignoreDst) {
                this.wallTime.setIsDst(-1);
            }
            int r = this.wallTime.mktime(this.zoneInfo);
            if (r == -1) {
                return -1;
            }
            return ((long) r) * 1000;
        }

        public void setTimeInMillis(long millis) {
            int intSeconds = (int) (millis / 1000);
            updateZoneInfoFromTimeZone();
            this.wallTime.localtime(intSeconds, this.zoneInfo);
        }

        public String format(String format) {
            if (format == null) {
                format = "%c";
            }
            return new TimeFormatter().format(format, this.wallTime, this.zoneInfo);
        }

        private void updateZoneInfoFromTimeZone() {
            if (!this.zoneInfo.getID().equals(this.timezone)) {
                this.zoneInfo = lookupZoneInfo(this.timezone);
            }
        }

        private static ZoneInfo lookupZoneInfo(String timezoneId) {
            try {
                ZoneInfo zoneInfo = ZoneInfoDB.getInstance().makeTimeZone(timezoneId);
                if (zoneInfo == null) {
                    zoneInfo = ZoneInfoDB.getInstance().makeTimeZone("GMT");
                }
                if (zoneInfo != null) {
                    return zoneInfo;
                }
                throw new AssertionError("GMT not found: \"" + timezoneId + "\"");
            } catch (IOException e) {
                throw new AssertionError("Error loading timezone: \"" + timezoneId + "\"", e);
            }
        }

        public void switchTimeZone(String timezone) {
            int seconds = this.wallTime.mktime(this.zoneInfo);
            this.timezone = timezone;
            updateZoneInfoFromTimeZone();
            this.wallTime.localtime(seconds, this.zoneInfo);
        }

        public String format2445(boolean hasTime) {
            int i;
            if (hasTime) {
                i = 16;
            } else {
                i = Time.YEAR_DAY;
            }
            char[] buf = new char[i];
            int n = this.wallTime.getYear();
            buf[Time.SUNDAY] = toChar(n / RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED);
            n %= RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED;
            buf[Time.SECOND] = toChar(n / 100);
            n %= 100;
            buf[Time.TUESDAY] = toChar(n / 10);
            buf[Time.WEDNESDAY] = toChar(n % 10);
            n = this.wallTime.getMonth() + Time.SECOND;
            buf[Time.THURSDAY] = toChar(n / 10);
            buf[Time.MONTH] = toChar(n % 10);
            n = this.wallTime.getMonthDay();
            buf[Time.YEAR] = toChar(n / 10);
            buf[Time.WEEK_DAY] = toChar(n % 10);
            if (!hasTime) {
                return new String(buf, Time.SUNDAY, Time.YEAR_DAY);
            }
            buf[Time.YEAR_DAY] = 'T';
            n = this.wallTime.getHour();
            buf[Time.WEEK_NUM] = toChar(n / 10);
            buf[10] = toChar(n % 10);
            n = this.wallTime.getMinute();
            buf[11] = toChar(n / 10);
            buf[12] = toChar(n % 10);
            n = this.wallTime.getSecond();
            buf[13] = toChar(n / 10);
            buf[14] = toChar(n % 10);
            if (!Time.TIMEZONE_UTC.equals(this.timezone)) {
                return new String(buf, Time.SUNDAY, 15);
            }
            buf[15] = 'Z';
            return new String(buf, Time.SUNDAY, 16);
        }

        private char toChar(int n) {
            return (n < 0 || n > Time.WEEK_NUM) ? ' ' : (char) (n + 48);
        }

        public String toStringInternal() {
            return String.format("%04d%02d%02dT%02d%02d%02d%s(%d,%d,%d,%d,%d)", new Object[]{Integer.valueOf(this.wallTime.getYear()), Integer.valueOf(this.wallTime.getMonth() + Time.SECOND), Integer.valueOf(this.wallTime.getMonthDay()), Integer.valueOf(this.wallTime.getHour()), Integer.valueOf(this.wallTime.getMinute()), Integer.valueOf(this.wallTime.getSecond()), this.timezone, Integer.valueOf(this.wallTime.getWeekDay()), Integer.valueOf(this.wallTime.getYearDay()), Integer.valueOf(this.wallTime.getGmtOffset()), Integer.valueOf(this.wallTime.getIsDst()), Long.valueOf(toMillis(false) / 1000)});
        }

        public static int compare(TimeCalculator aObject, TimeCalculator bObject) {
            int i = Time.SUNDAY;
            if (aObject.timezone.equals(bObject.timezone)) {
                int diff = aObject.wallTime.getYear() - bObject.wallTime.getYear();
                if (diff != 0) {
                    return diff;
                }
                diff = aObject.wallTime.getMonth() - bObject.wallTime.getMonth();
                if (diff != 0) {
                    return diff;
                }
                diff = aObject.wallTime.getMonthDay() - bObject.wallTime.getMonthDay();
                if (diff != 0) {
                    return diff;
                }
                diff = aObject.wallTime.getHour() - bObject.wallTime.getHour();
                if (diff != 0) {
                    return diff;
                }
                diff = aObject.wallTime.getMinute() - bObject.wallTime.getMinute();
                if (diff != 0) {
                    return diff;
                }
                diff = aObject.wallTime.getSecond() - bObject.wallTime.getSecond();
                if (diff != 0) {
                    return diff;
                }
                return Time.SUNDAY;
            }
            long diff2 = aObject.toMillis(false) - bObject.toMillis(false);
            if (diff2 < 0) {
                i = -1;
            } else if (diff2 > 0) {
                i = Time.SECOND;
            }
            return i;
        }

        public void copyFieldsToTime(Time time) {
            time.second = this.wallTime.getSecond();
            time.minute = this.wallTime.getMinute();
            time.hour = this.wallTime.getHour();
            time.monthDay = this.wallTime.getMonthDay();
            time.month = this.wallTime.getMonth();
            time.year = this.wallTime.getYear();
            time.weekDay = this.wallTime.getWeekDay();
            time.yearDay = this.wallTime.getYearDay();
            time.isDst = this.wallTime.getIsDst();
            time.gmtoff = (long) this.wallTime.getGmtOffset();
        }

        public void copyFieldsFromTime(Time time) {
            this.wallTime.setSecond(time.second);
            this.wallTime.setMinute(time.minute);
            this.wallTime.setHour(time.hour);
            this.wallTime.setMonthDay(time.monthDay);
            this.wallTime.setMonth(time.month);
            this.wallTime.setYear(time.year);
            this.wallTime.setWeekDay(time.weekDay);
            this.wallTime.setYearDay(time.yearDay);
            this.wallTime.setIsDst(time.isDst);
            this.wallTime.setGmtOffset((int) time.gmtoff);
            if (!time.allDay || (time.second == 0 && time.minute == 0 && time.hour == 0)) {
                this.timezone = time.timezone;
                updateZoneInfoFromTimeZone();
                return;
            }
            throw new IllegalArgumentException("allDay is true but sec, min, hour are not 0.");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.format.Time.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.format.Time.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.text.format.Time.<clinit>():void");
    }

    public Time(String timezoneId) {
        if (timezoneId == null) {
            throw new NullPointerException("timezoneId is null!");
        }
        initialize(timezoneId);
    }

    public Time() {
        initialize(TimeZone.getDefault().getID());
    }

    public Time(Time other) {
        initialize(other.timezone);
        set(other);
    }

    private void initialize(String timezoneId) {
        this.timezone = timezoneId;
        this.year = 1970;
        this.monthDay = SECOND;
        this.isDst = -1;
        this.calculator = new TimeCalculator(timezoneId);
    }

    public long normalize(boolean ignoreDst) {
        this.calculator.copyFieldsFromTime(this);
        long timeInMillis = this.calculator.toMillis(ignoreDst);
        this.calculator.copyFieldsToTime(this);
        return timeInMillis;
    }

    public void switchTimezone(String timezone) {
        this.calculator.copyFieldsFromTime(this);
        this.calculator.switchTimeZone(timezone);
        this.calculator.copyFieldsToTime(this);
        this.timezone = timezone;
    }

    public int getActualMaximum(int field) {
        int i = 28;
        int y;
        switch (field) {
            case SECOND /*1*/:
                return 59;
            case TUESDAY /*2*/:
                return 59;
            case WEDNESDAY /*3*/:
                return 23;
            case THURSDAY /*4*/:
                int n = DAYS_PER_MONTH[this.month];
                if (n != 28) {
                    return n;
                }
                y = this.year;
                if (y % THURSDAY == 0 && (y % 100 != 0 || y % StatisticalConstant.TYPE_WIFI_HiLink_CONNECT_ACTION == 0)) {
                    i = 29;
                }
                return i;
            case MONTH /*5*/:
                return 11;
            case YEAR /*6*/:
                return AbstractRILConstants.RIL_REQUEST_HW_VSIM_SET_SIM_STATE;
            case WEEK_DAY /*7*/:
                return YEAR;
            case YEAR_DAY /*8*/:
                y = this.year;
                i = (y % THURSDAY != 0 || (y % 100 == 0 && y % StatisticalConstant.TYPE_WIFI_HiLink_CONNECT_ACTION != 0)) ? MetricsEvent.ACTION_QS_EDIT_MOVE_SPEC : MetricsEvent.ACTION_QS_EDIT_MOVE;
                return i;
            case WEEK_NUM /*9*/:
                throw new RuntimeException("WEEK_NUM not implemented");
            default:
                throw new RuntimeException("bad field=" + field);
        }
    }

    public void clear(String timezoneId) {
        if (timezoneId == null) {
            throw new NullPointerException("timezone is null!");
        }
        this.timezone = timezoneId;
        this.allDay = false;
        this.second = SUNDAY;
        this.minute = SUNDAY;
        this.hour = SUNDAY;
        this.monthDay = SUNDAY;
        this.month = SUNDAY;
        this.year = SUNDAY;
        this.weekDay = SUNDAY;
        this.yearDay = SUNDAY;
        this.gmtoff = 0;
        this.isDst = -1;
    }

    public static int compare(Time a, Time b) {
        if (a == null) {
            throw new NullPointerException("a == null");
        } else if (b == null) {
            throw new NullPointerException("b == null");
        } else {
            a.calculator.copyFieldsFromTime(a);
            b.calculator.copyFieldsFromTime(b);
            return TimeCalculator.compare(a.calculator, b.calculator);
        }
    }

    public String format(String format) {
        this.calculator.copyFieldsFromTime(this);
        return this.calculator.format(format);
    }

    public String toString() {
        TimeCalculator calculator = new TimeCalculator(this.timezone);
        calculator.copyFieldsFromTime(this);
        return calculator.toStringInternal();
    }

    public boolean parse(String s) {
        if (s == null) {
            throw new NullPointerException("time string is null");
        } else if (!parseInternal(s)) {
            return false;
        } else {
            this.timezone = TIMEZONE_UTC;
            return true;
        }
    }

    private boolean parseInternal(String s) {
        int len = s.length();
        if (len < YEAR_DAY) {
            throw new TimeFormatException("String is too short: \"" + s + "\" Expected at least 8 characters.");
        }
        boolean inUtc = false;
        this.year = ((getChar(s, SUNDAY, RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED) + getChar(s, SECOND, 100)) + getChar(s, TUESDAY, 10)) + getChar(s, WEDNESDAY, SECOND);
        this.month = (getChar(s, THURSDAY, 10) + getChar(s, MONTH, SECOND)) - 1;
        this.monthDay = getChar(s, YEAR, 10) + getChar(s, WEEK_DAY, SECOND);
        if (len <= YEAR_DAY) {
            this.allDay = true;
            this.hour = SUNDAY;
            this.minute = SUNDAY;
            this.second = SUNDAY;
        } else if (len < 15) {
            throw new TimeFormatException("String is too short: \"" + s + "\" If there are more than 8 characters there must be at least" + " 15.");
        } else {
            checkChar(s, YEAR_DAY, 'T');
            this.allDay = false;
            this.hour = getChar(s, WEEK_NUM, 10) + getChar(s, 10, SECOND);
            this.minute = getChar(s, 11, 10) + getChar(s, 12, SECOND);
            this.second = getChar(s, 13, 10) + getChar(s, 14, SECOND);
            if (len > 15) {
                checkChar(s, 15, 'Z');
                inUtc = true;
            }
        }
        this.weekDay = SUNDAY;
        this.yearDay = SUNDAY;
        this.isDst = -1;
        this.gmtoff = 0;
        return inUtc;
    }

    private void checkChar(String s, int spos, char expected) {
        char c = s.charAt(spos);
        if (c != expected) {
            Object[] objArr = new Object[THURSDAY];
            objArr[SUNDAY] = Integer.valueOf(c);
            objArr[SECOND] = Integer.valueOf(spos);
            objArr[TUESDAY] = Integer.valueOf(expected);
            objArr[WEDNESDAY] = Character.valueOf(expected);
            throw new TimeFormatException(String.format("Unexpected character 0x%02d at pos=%d.  Expected 0x%02d ('%c').", objArr));
        }
    }

    private static int getChar(String s, int spos, int mul) {
        char c = s.charAt(spos);
        if (Character.isDigit(c)) {
            return Character.getNumericValue(c) * mul;
        }
        throw new TimeFormatException("Parse error at pos=" + spos);
    }

    public boolean parse3339(String s) {
        if (s == null) {
            throw new NullPointerException("time string is null");
        } else if (!parse3339Internal(s)) {
            return false;
        } else {
            this.timezone = TIMEZONE_UTC;
            return true;
        }
    }

    private boolean parse3339Internal(String s) {
        int len = s.length();
        if (len < 10) {
            throw new TimeFormatException("String too short --- expected at least 10 characters.");
        }
        boolean inUtc = false;
        this.year = ((getChar(s, SUNDAY, RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED) + getChar(s, SECOND, 100)) + getChar(s, TUESDAY, 10)) + getChar(s, WEDNESDAY, SECOND);
        checkChar(s, THURSDAY, '-');
        this.month = (getChar(s, MONTH, 10) + getChar(s, YEAR, SECOND)) - 1;
        checkChar(s, WEEK_DAY, '-');
        this.monthDay = getChar(s, YEAR_DAY, 10) + getChar(s, WEEK_NUM, SECOND);
        if (len >= 19) {
            checkChar(s, 10, 'T');
            this.allDay = false;
            int hour = getChar(s, 11, 10) + getChar(s, 12, SECOND);
            checkChar(s, 13, ':');
            int minute = getChar(s, 14, 10) + getChar(s, 15, SECOND);
            checkChar(s, 16, ':');
            this.second = getChar(s, 17, 10) + getChar(s, 18, SECOND);
            int tzIndex = 19;
            if (19 < len && s.charAt(19) == '.') {
                do {
                    tzIndex += SECOND;
                    if (tzIndex >= len) {
                        break;
                    }
                } while (Character.isDigit(s.charAt(tzIndex)));
            }
            int offset = SUNDAY;
            if (len > tzIndex) {
                Object[] objArr;
                char c = s.charAt(tzIndex);
                switch (c) {
                    case StatisticalConstant.TYPE_SINGLEHAND_ENTER_2S_EXIT /*43*/:
                        offset = -1;
                        break;
                    case RILConstants.RIL_REQUEST_QUERY_NETWORK_SELECTION_MODE /*45*/:
                        offset = SECOND;
                        break;
                    case StatisticalConstant.TYPE_TOUCH_FORCE_END /*90*/:
                        offset = SUNDAY;
                        break;
                    default:
                        objArr = new Object[TUESDAY];
                        objArr[SUNDAY] = Integer.valueOf(c);
                        objArr[SECOND] = Integer.valueOf(tzIndex);
                        throw new TimeFormatException(String.format("Unexpected character 0x%02d at position %d.  Expected + or -", objArr));
                }
                inUtc = true;
                if (offset != 0) {
                    if (len < tzIndex + YEAR) {
                        objArr = new Object[SECOND];
                        objArr[SUNDAY] = Integer.valueOf(tzIndex + YEAR);
                        throw new TimeFormatException(String.format("Unexpected length; should be %d characters", objArr));
                    }
                    hour += (getChar(s, tzIndex + SECOND, 10) + getChar(s, tzIndex + TUESDAY, SECOND)) * offset;
                    minute += (getChar(s, tzIndex + THURSDAY, 10) + getChar(s, tzIndex + MONTH, SECOND)) * offset;
                }
            }
            this.hour = hour;
            this.minute = minute;
            if (offset != 0) {
                normalize(false);
            }
        } else {
            this.allDay = true;
            this.hour = SUNDAY;
            this.minute = SUNDAY;
            this.second = SUNDAY;
        }
        this.weekDay = SUNDAY;
        this.yearDay = SUNDAY;
        this.isDst = -1;
        this.gmtoff = 0;
        return inUtc;
    }

    public static String getCurrentTimezone() {
        return TimeZone.getDefault().getID();
    }

    public void setToNow() {
        set(System.currentTimeMillis());
    }

    public long toMillis(boolean ignoreDst) {
        this.calculator.copyFieldsFromTime(this);
        return this.calculator.toMillis(ignoreDst);
    }

    public void set(long millis) {
        this.allDay = false;
        this.calculator.timezone = this.timezone;
        this.calculator.setTimeInMillis(millis);
        this.calculator.copyFieldsToTime(this);
    }

    public String format2445() {
        this.calculator.copyFieldsFromTime(this);
        return this.calculator.format2445(!this.allDay);
    }

    public void set(Time that) {
        this.timezone = that.timezone;
        this.allDay = that.allDay;
        this.second = that.second;
        this.minute = that.minute;
        this.hour = that.hour;
        this.monthDay = that.monthDay;
        this.month = that.month;
        this.year = that.year;
        this.weekDay = that.weekDay;
        this.yearDay = that.yearDay;
        this.isDst = that.isDst;
        this.gmtoff = that.gmtoff;
    }

    public void set(int second, int minute, int hour, int monthDay, int month, int year) {
        this.allDay = false;
        this.second = second;
        this.minute = minute;
        this.hour = hour;
        this.monthDay = monthDay;
        this.month = month;
        this.year = year;
        this.weekDay = SUNDAY;
        this.yearDay = SUNDAY;
        this.isDst = -1;
        this.gmtoff = 0;
    }

    public void set(int monthDay, int month, int year) {
        this.allDay = true;
        this.second = SUNDAY;
        this.minute = SUNDAY;
        this.hour = SUNDAY;
        this.monthDay = monthDay;
        this.month = month;
        this.year = year;
        this.weekDay = SUNDAY;
        this.yearDay = SUNDAY;
        this.isDst = -1;
        this.gmtoff = 0;
    }

    public boolean before(Time that) {
        return compare(this, that) < 0;
    }

    public boolean after(Time that) {
        return compare(this, that) > 0;
    }

    public int getWeekNumber() {
        int closestThursday = this.yearDay + sThursdayOffset[this.weekDay];
        if (closestThursday >= 0 && closestThursday <= MetricsEvent.ACTION_QS_EDIT_MOVE_SPEC) {
            return (closestThursday / WEEK_DAY) + SECOND;
        }
        Time temp = new Time(this);
        temp.monthDay += sThursdayOffset[this.weekDay];
        temp.normalize(true);
        return (temp.yearDay / WEEK_DAY) + SECOND;
    }

    public String format3339(boolean allDay) {
        if (allDay) {
            return format(Y_M_D);
        }
        if (TIMEZONE_UTC.equals(this.timezone)) {
            return format(Y_M_D_T_H_M_S_000_Z);
        }
        String base = format(Y_M_D_T_H_M_S_000);
        String sign = this.gmtoff < 0 ? NativeLibraryHelper.CLEAR_ABI_OVERRIDE : "+";
        int offset = (int) Math.abs(this.gmtoff);
        int minutes = (offset % 3600) / 60;
        int hours = offset / 3600;
        Object[] objArr = new Object[THURSDAY];
        objArr[SUNDAY] = base;
        objArr[SECOND] = sign;
        objArr[TUESDAY] = Integer.valueOf(hours);
        objArr[WEDNESDAY] = Integer.valueOf(minutes);
        return String.format(Locale.US, "%s%s%02d:%02d", objArr);
    }

    public static boolean isEpoch(Time time) {
        if (getJulianDay(time.toMillis(true), 0) == EPOCH_JULIAN_DAY) {
            return true;
        }
        return false;
    }

    public static int getJulianDay(long millis, long gmtoff) {
        return ((int) ((millis + (gmtoff * 1000)) / DateUtils.DAY_IN_MILLIS)) + EPOCH_JULIAN_DAY;
    }

    public long setJulianDay(int julianDay) {
        long millis = ((long) (julianDay - EPOCH_JULIAN_DAY)) * DateUtils.DAY_IN_MILLIS;
        set(millis);
        this.monthDay += julianDay - getJulianDay(millis, this.gmtoff);
        this.hour = SUNDAY;
        this.minute = SUNDAY;
        this.second = SUNDAY;
        return normalize(true);
    }

    public static int getWeeksSinceEpochFromJulianDay(int julianDay, int firstDayOfWeek) {
        int diff = 4 - firstDayOfWeek;
        if (diff < 0) {
            diff += WEEK_DAY;
        }
        return (julianDay - (EPOCH_JULIAN_DAY - diff)) / WEEK_DAY;
    }

    public static int getJulianMondayFromWeeksSinceEpoch(int week) {
        return (week * WEEK_DAY) + MONDAY_BEFORE_JULIAN_EPOCH;
    }
}

package java.text;

import java.io.InvalidObjectException;
import java.text.Format;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.TimeZone;
import libcore.icu.ICU;

public abstract class DateFormat extends Format {
    public static final int AM_PM_FIELD = 14;
    public static final int DATE_FIELD = 3;
    public static final int DAY_OF_WEEK_FIELD = 9;
    public static final int DAY_OF_WEEK_IN_MONTH_FIELD = 11;
    public static final int DAY_OF_YEAR_FIELD = 10;
    public static final int DEFAULT = 2;
    public static final int ERA_FIELD = 0;
    public static final int FULL = 0;
    public static final int HOUR0_FIELD = 16;
    public static final int HOUR1_FIELD = 15;
    public static final int HOUR_OF_DAY0_FIELD = 5;
    public static final int HOUR_OF_DAY1_FIELD = 4;
    public static final int LONG = 1;
    public static final int MEDIUM = 2;
    public static final int MILLISECOND_FIELD = 8;
    public static final int MINUTE_FIELD = 6;
    public static final int MONTH_FIELD = 2;
    public static final int SECOND_FIELD = 7;
    public static final int SHORT = 3;
    public static final int TIMEZONE_FIELD = 17;
    public static final int WEEK_OF_MONTH_FIELD = 13;
    public static final int WEEK_OF_YEAR_FIELD = 12;
    public static final int YEAR_FIELD = 1;
    public static Boolean is24Hour = null;
    private static final long serialVersionUID = 7218322306649953788L;
    protected Calendar calendar;
    protected NumberFormat numberFormat;

    public static class Field extends Format.Field {
        public static final Field AM_PM = new Field("am pm", 9);
        public static final Field DAY_OF_MONTH = new Field("day of month", 5);
        public static final Field DAY_OF_WEEK = new Field("day of week", 7);
        public static final Field DAY_OF_WEEK_IN_MONTH = new Field("day of week in month", 8);
        public static final Field DAY_OF_YEAR = new Field("day of year", 6);
        public static final Field ERA = new Field("era", 0);
        public static final Field HOUR0 = new Field("hour", 10);
        public static final Field HOUR1 = new Field("hour 1", -1);
        public static final Field HOUR_OF_DAY0 = new Field("hour of day", 11);
        public static final Field HOUR_OF_DAY1 = new Field("hour of day 1", -1);
        public static final Field MILLISECOND = new Field("millisecond", 14);
        public static final Field MINUTE = new Field("minute", 12);
        public static final Field MONTH = new Field("month", 2);
        public static final Field SECOND = new Field("second", 13);
        public static final Field TIME_ZONE = new Field("time zone", -1);
        public static final Field WEEK_OF_MONTH = new Field("week of month", 4);
        public static final Field WEEK_OF_YEAR = new Field("week of year", 3);
        public static final Field YEAR = new Field("year", 1);
        private static final Field[] calendarToFieldMapping = new Field[17];
        private static final Map<String, Field> instanceMap = new HashMap(18);
        private static final long serialVersionUID = 7441350119349544720L;
        private int calendarField;

        public static Field ofCalendarField(int calendarField2) {
            if (calendarField2 >= 0 && calendarField2 < calendarToFieldMapping.length) {
                return calendarToFieldMapping[calendarField2];
            }
            throw new IllegalArgumentException("Unknown Calendar constant " + calendarField2);
        }

        protected Field(String name, int calendarField2) {
            super(name);
            this.calendarField = calendarField2;
            if (getClass() == Field.class) {
                instanceMap.put(name, this);
                if (calendarField2 >= 0) {
                    calendarToFieldMapping[calendarField2] = this;
                }
            }
        }

        public int getCalendarField() {
            return this.calendarField;
        }

        /* access modifiers changed from: protected */
        public Object readResolve() throws InvalidObjectException {
            if (getClass() == Field.class) {
                Object instance = instanceMap.get(getName());
                if (instance != null) {
                    return instance;
                }
                throw new InvalidObjectException("unknown attribute name");
            }
            throw new InvalidObjectException("subclass didn't correctly implement readResolve");
        }
    }

    public abstract StringBuffer format(Date date, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract Date parse(String str, ParsePosition parsePosition);

    public final StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        if (obj instanceof Date) {
            return format((Date) obj, toAppendTo, fieldPosition);
        }
        if (obj instanceof Number) {
            return format(new Date(((Number) obj).longValue()), toAppendTo, fieldPosition);
        }
        throw new IllegalArgumentException("Cannot format given Object as a Date");
    }

    public final String format(Date date) {
        return format(date, new StringBuffer(), DontCareFieldPosition.INSTANCE).toString();
    }

    public Date parse(String source) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Date result = parse(source, pos);
        if (pos.index != 0) {
            return result;
        }
        throw new ParseException("Unparseable date: \"" + source + "\"", pos.errorIndex);
    }

    public Object parseObject(String source, ParsePosition pos) {
        return parse(source, pos);
    }

    public static final DateFormat getTimeInstance() {
        return get(2, 0, 1, Locale.getDefault(Locale.Category.FORMAT));
    }

    public static final DateFormat getTimeInstance(int style) {
        return get(style, 0, 1, Locale.getDefault(Locale.Category.FORMAT));
    }

    public static final DateFormat getTimeInstance(int style, Locale aLocale) {
        return get(style, 0, 1, aLocale);
    }

    public static final DateFormat getDateInstance() {
        return get(0, 2, 2, Locale.getDefault(Locale.Category.FORMAT));
    }

    public static final DateFormat getDateInstance(int style) {
        return get(0, style, 2, Locale.getDefault(Locale.Category.FORMAT));
    }

    public static final DateFormat getDateInstance(int style, Locale aLocale) {
        return get(0, style, 2, aLocale);
    }

    public static final DateFormat getDateTimeInstance() {
        return get(2, 2, 3, Locale.getDefault(Locale.Category.FORMAT));
    }

    public static final DateFormat getDateTimeInstance(int dateStyle, int timeStyle) {
        return get(timeStyle, dateStyle, 3, Locale.getDefault(Locale.Category.FORMAT));
    }

    public static final DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale aLocale) {
        return get(timeStyle, dateStyle, 3, aLocale);
    }

    public static final DateFormat getInstance() {
        return getDateTimeInstance(3, 3);
    }

    public static final void set24HourTimePref(Boolean is24Hour2) {
        is24Hour = is24Hour2;
    }

    public static Locale[] getAvailableLocales() {
        return ICU.getAvailableLocales();
    }

    public void setCalendar(Calendar newCalendar) {
        this.calendar = newCalendar;
    }

    public Calendar getCalendar() {
        return this.calendar;
    }

    public void setNumberFormat(NumberFormat newNumberFormat) {
        this.numberFormat = newNumberFormat;
    }

    public NumberFormat getNumberFormat() {
        return this.numberFormat;
    }

    public void setTimeZone(TimeZone zone) {
        this.calendar.setTimeZone(zone);
    }

    public TimeZone getTimeZone() {
        return this.calendar.getTimeZone();
    }

    public void setLenient(boolean lenient) {
        this.calendar.setLenient(lenient);
    }

    public boolean isLenient() {
        return this.calendar.isLenient();
    }

    public int hashCode() {
        return this.numberFormat.hashCode();
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DateFormat other = (DateFormat) obj;
        if (!(this.calendar.getFirstDayOfWeek() == other.calendar.getFirstDayOfWeek() && this.calendar.getMinimalDaysInFirstWeek() == other.calendar.getMinimalDaysInFirstWeek() && this.calendar.isLenient() == other.calendar.isLenient() && this.calendar.getTimeZone().equals(other.calendar.getTimeZone()) && this.numberFormat.equals(other.numberFormat))) {
            z = false;
        }
        return z;
    }

    public Object clone() {
        DateFormat other = (DateFormat) super.clone();
        other.calendar = (Calendar) this.calendar.clone();
        other.numberFormat = (NumberFormat) this.numberFormat.clone();
        return other;
    }

    private static DateFormat get(int timeStyle, int dateStyle, int flags, Locale loc) {
        if ((flags & 1) == 0) {
            timeStyle = -1;
        } else if (timeStyle < 0 || timeStyle > 3) {
            throw new IllegalArgumentException("Illegal time style " + timeStyle);
        }
        if ((flags & 2) == 0) {
            dateStyle = -1;
        } else if (dateStyle < 0 || dateStyle > 3) {
            throw new IllegalArgumentException("Illegal date style " + dateStyle);
        }
        try {
            return new SimpleDateFormat(timeStyle, dateStyle, loc);
        } catch (MissingResourceException e) {
            return new SimpleDateFormat("M/d/yy h:mm a");
        }
    }

    protected DateFormat() {
    }
}

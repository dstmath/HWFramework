package java.text;

import java.io.InvalidObjectException;
import java.text.spi.DateFormatProvider;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.TimeZone;
import sun.util.LocaleServiceProviderPool;
import sun.util.LocaleServiceProviderPool.LocalizedObjectGetter;

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

    private static class DateFormatGetter implements LocalizedObjectGetter<DateFormatProvider, DateFormat> {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        private static final DateFormatGetter INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.text.DateFormat.DateFormatGetter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.text.DateFormat.DateFormatGetter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.text.DateFormat.DateFormatGetter.<clinit>():void");
        }

        private DateFormatGetter() {
        }

        public DateFormat getObject(DateFormatProvider dateFormatProvider, Locale locale, String key, Object... params) {
            if (!-assertionsDisabled) {
                if ((params.length == DateFormat.SHORT ? DateFormat.YEAR_FIELD : DateFormat.FULL) == 0) {
                    throw new AssertionError();
                }
            }
            int timeStyle = ((Integer) params[DateFormat.FULL]).intValue();
            int dateStyle = ((Integer) params[DateFormat.YEAR_FIELD]).intValue();
            switch (((Integer) params[DateFormat.MONTH_FIELD]).intValue()) {
                case DateFormat.YEAR_FIELD /*1*/:
                    return dateFormatProvider.getTimeInstance(timeStyle, locale);
                case DateFormat.MONTH_FIELD /*2*/:
                    return dateFormatProvider.getDateInstance(dateStyle, locale);
                case DateFormat.SHORT /*3*/:
                    return dateFormatProvider.getDateTimeInstance(dateStyle, timeStyle, locale);
                default:
                    if (-assertionsDisabled) {
                        return null;
                    }
                    throw new AssertionError((Object) "should not happen");
            }
        }
    }

    public static class Field extends java.text.Format.Field {
        public static final Field AM_PM = null;
        public static final Field DAY_OF_MONTH = null;
        public static final Field DAY_OF_WEEK = null;
        public static final Field DAY_OF_WEEK_IN_MONTH = null;
        public static final Field DAY_OF_YEAR = null;
        public static final Field ERA = null;
        public static final Field HOUR0 = null;
        public static final Field HOUR1 = null;
        public static final Field HOUR_OF_DAY0 = null;
        public static final Field HOUR_OF_DAY1 = null;
        public static final Field MILLISECOND = null;
        public static final Field MINUTE = null;
        public static final Field MONTH = null;
        public static final Field SECOND = null;
        public static final Field TIME_ZONE = null;
        public static final Field WEEK_OF_MONTH = null;
        public static final Field WEEK_OF_YEAR = null;
        public static final Field YEAR = null;
        private static final Field[] calendarToFieldMapping = null;
        private static final Map instanceMap = null;
        private static final long serialVersionUID = 7441350119349544720L;
        private int calendarField;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.text.DateFormat.Field.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.text.DateFormat.Field.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.text.DateFormat.Field.<clinit>():void");
        }

        public static Field ofCalendarField(int calendarField) {
            if (calendarField >= 0 && calendarField < calendarToFieldMapping.length) {
                return calendarToFieldMapping[calendarField];
            }
            throw new IllegalArgumentException("Unknown Calendar constant " + calendarField);
        }

        protected Field(String name, int calendarField) {
            super(name);
            this.calendarField = calendarField;
            if (getClass() == Field.class) {
                instanceMap.put(name, this);
                if (calendarField >= 0) {
                    calendarToFieldMapping[calendarField] = this;
                }
            }
        }

        public int getCalendarField() {
            return this.calendarField;
        }

        protected Object readResolve() throws InvalidObjectException {
            if (getClass() != Field.class) {
                throw new InvalidObjectException("subclass didn't correctly implement readResolve");
            }
            Object instance = instanceMap.get(getName());
            if (instance != null) {
                return instance;
            }
            throw new InvalidObjectException("unknown attribute name");
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
        ParsePosition pos = new ParsePosition(FULL);
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
        return get(MONTH_FIELD, FULL, YEAR_FIELD, Locale.getDefault(Category.FORMAT));
    }

    public static final DateFormat getTimeInstance(int style) {
        return get(style, FULL, YEAR_FIELD, Locale.getDefault(Category.FORMAT));
    }

    public static final DateFormat getTimeInstance(int style, Locale aLocale) {
        return get(style, FULL, YEAR_FIELD, aLocale);
    }

    public static final DateFormat getDateInstance() {
        return get(FULL, MONTH_FIELD, MONTH_FIELD, Locale.getDefault(Category.FORMAT));
    }

    public static final DateFormat getDateInstance(int style) {
        return get(FULL, style, MONTH_FIELD, Locale.getDefault(Category.FORMAT));
    }

    public static final DateFormat getDateInstance(int style, Locale aLocale) {
        return get(FULL, style, MONTH_FIELD, aLocale);
    }

    public static final DateFormat getDateTimeInstance() {
        return get(MONTH_FIELD, MONTH_FIELD, SHORT, Locale.getDefault(Category.FORMAT));
    }

    public static final DateFormat getDateTimeInstance(int dateStyle, int timeStyle) {
        return get(timeStyle, dateStyle, SHORT, Locale.getDefault(Category.FORMAT));
    }

    public static final DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale aLocale) {
        return get(timeStyle, dateStyle, SHORT, aLocale);
    }

    public static final DateFormat getInstance() {
        return getDateTimeInstance(SHORT, SHORT);
    }

    public static final void set24HourTimePref(boolean is24Hour) {
        is24Hour = Boolean.valueOf(is24Hour);
    }

    public static Locale[] getAvailableLocales() {
        return LocaleServiceProviderPool.getPool(DateFormatProvider.class).getAvailableLocales();
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
        boolean z = false;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DateFormat other = (DateFormat) obj;
        if (this.calendar.getFirstDayOfWeek() == other.calendar.getFirstDayOfWeek() && this.calendar.getMinimalDaysInFirstWeek() == other.calendar.getMinimalDaysInFirstWeek() && this.calendar.isLenient() == other.calendar.isLenient() && this.calendar.getTimeZone().equals(other.calendar.getTimeZone())) {
            z = this.numberFormat.equals(other.numberFormat);
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
        if ((flags & YEAR_FIELD) == 0) {
            timeStyle = -1;
        } else if (timeStyle < 0 || timeStyle > SHORT) {
            throw new IllegalArgumentException("Illegal time style " + timeStyle);
        }
        if ((flags & MONTH_FIELD) == 0) {
            dateStyle = -1;
        } else if (dateStyle < 0 || dateStyle > SHORT) {
            throw new IllegalArgumentException("Illegal date style " + dateStyle);
        }
        try {
            LocaleServiceProviderPool pool = LocaleServiceProviderPool.getPool(DateFormatProvider.class);
            if (pool.hasProviders()) {
                LocalizedObjectGetter -get0 = DateFormatGetter.INSTANCE;
                Object[] objArr = new Object[SHORT];
                objArr[FULL] = Integer.valueOf(timeStyle);
                objArr[YEAR_FIELD] = Integer.valueOf(dateStyle);
                objArr[MONTH_FIELD] = Integer.valueOf(flags);
                DateFormat providersInstance = (DateFormat) pool.getLocalizedObject(-get0, loc, objArr);
                if (providersInstance != null) {
                    return providersInstance;
                }
            }
            return new SimpleDateFormat(timeStyle, dateStyle, loc);
        } catch (MissingResourceException e) {
            return new SimpleDateFormat("M/d/yy h:mm a");
        }
    }

    protected DateFormat() {
    }
}

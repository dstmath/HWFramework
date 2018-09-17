package android.icu.text;

import android.icu.impl.ICUResourceBundle;
import android.icu.impl.RelativeDateFormat;
import android.icu.text.DisplayContext.Type;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

public abstract class DateFormat extends UFormat {
    public static final String ABBR_GENERIC_TZ = "v";
    public static final String ABBR_MONTH = "MMM";
    public static final String ABBR_MONTH_DAY = "MMMd";
    public static final String ABBR_MONTH_WEEKDAY_DAY = "MMMEd";
    public static final String ABBR_QUARTER = "QQQ";
    public static final String ABBR_SPECIFIC_TZ = "z";
    @Deprecated
    public static final String ABBR_STANDALONE_MONTH = "LLL";
    public static final String ABBR_UTC_TZ = "ZZZZ";
    public static final String ABBR_WEEKDAY = "E";
    public static final int AM_PM_FIELD = 14;
    public static final int DATE_FIELD = 3;
    @Deprecated
    public static final List<String> DATE_SKELETONS = null;
    public static final String DAY = "d";
    public static final int DAY_OF_WEEK_FIELD = 9;
    public static final int DAY_OF_WEEK_IN_MONTH_FIELD = 11;
    public static final int DAY_OF_YEAR_FIELD = 10;
    public static final int DEFAULT = 2;
    public static final int DOW_LOCAL_FIELD = 19;
    public static final int ERA_FIELD = 0;
    public static final int EXTENDED_YEAR_FIELD = 20;
    public static final int FIELD_COUNT = 36;
    public static final int FRACTIONAL_SECOND_FIELD = 8;
    public static final int FULL = 0;
    public static final String GENERIC_TZ = "vvvv";
    public static final String HOUR = "j";
    public static final int HOUR0_FIELD = 16;
    public static final int HOUR1_FIELD = 15;
    public static final String HOUR24 = "H";
    public static final String HOUR24_MINUTE = "Hm";
    public static final String HOUR24_MINUTE_SECOND = "Hms";
    @Deprecated
    public static final String HOUR_GENERIC_TZ = "jv";
    public static final String HOUR_MINUTE = "jm";
    @Deprecated
    public static final String HOUR_MINUTE_GENERIC_TZ = "jmv";
    public static final String HOUR_MINUTE_SECOND = "jms";
    @Deprecated
    public static final String HOUR_MINUTE_TZ = "jmz";
    public static final int HOUR_OF_DAY0_FIELD = 5;
    public static final int HOUR_OF_DAY1_FIELD = 4;
    @Deprecated
    public static final String HOUR_TZ = "jz";
    public static final int JULIAN_DAY_FIELD = 21;
    public static final String LOCATION_TZ = "VVVV";
    public static final int LONG = 1;
    public static final int MEDIUM = 2;
    public static final int MILLISECONDS_IN_DAY_FIELD = 22;
    public static final int MILLISECOND_FIELD = 8;
    public static final String MINUTE = "m";
    public static final int MINUTE_FIELD = 6;
    public static final String MINUTE_SECOND = "ms";
    public static final String MONTH = "MMMM";
    public static final String MONTH_DAY = "MMMMd";
    public static final int MONTH_FIELD = 2;
    public static final String MONTH_WEEKDAY_DAY = "MMMMEEEEd";
    public static final int NONE = -1;
    public static final String NUM_MONTH = "M";
    public static final String NUM_MONTH_DAY = "Md";
    public static final String NUM_MONTH_WEEKDAY_DAY = "MEd";
    public static final String QUARTER = "QQQQ";
    public static final int QUARTER_FIELD = 27;
    @Deprecated
    static final int RELATED_YEAR = 34;
    public static final int RELATIVE = 128;
    public static final int RELATIVE_DEFAULT = 130;
    public static final int RELATIVE_FULL = 128;
    public static final int RELATIVE_LONG = 129;
    public static final int RELATIVE_MEDIUM = 130;
    public static final int RELATIVE_SHORT = 131;
    public static final String SECOND = "s";
    public static final int SECOND_FIELD = 7;
    public static final int SHORT = 3;
    public static final String SPECIFIC_TZ = "zzzz";
    public static final int STANDALONE_DAY_FIELD = 25;
    @Deprecated
    public static final String STANDALONE_MONTH = "LLLL";
    public static final int STANDALONE_MONTH_FIELD = 26;
    public static final int STANDALONE_QUARTER_FIELD = 28;
    public static final int TIMEZONE_FIELD = 17;
    public static final int TIMEZONE_GENERIC_FIELD = 24;
    public static final int TIMEZONE_ISO_FIELD = 32;
    public static final int TIMEZONE_ISO_LOCAL_FIELD = 33;
    public static final int TIMEZONE_LOCALIZED_GMT_OFFSET_FIELD = 31;
    public static final int TIMEZONE_RFC_FIELD = 23;
    public static final int TIMEZONE_SPECIAL_FIELD = 29;
    public static final int TIME_SEPARATOR = 35;
    @Deprecated
    public static final List<String> TIME_SKELETONS = null;
    public static final String WEEKDAY = "EEEE";
    public static final int WEEK_OF_MONTH_FIELD = 13;
    public static final int WEEK_OF_YEAR_FIELD = 12;
    public static final String YEAR = "y";
    public static final String YEAR_ABBR_MONTH = "yMMM";
    public static final String YEAR_ABBR_MONTH_DAY = "yMMMd";
    public static final String YEAR_ABBR_MONTH_WEEKDAY_DAY = "yMMMEd";
    public static final String YEAR_ABBR_QUARTER = "yQQQ";
    public static final int YEAR_FIELD = 1;
    public static final String YEAR_MONTH = "yMMMM";
    public static final String YEAR_MONTH_DAY = "yMMMMd";
    public static final String YEAR_MONTH_WEEKDAY_DAY = "yMMMMEEEEd";
    public static final int YEAR_NAME_FIELD = 30;
    public static final String YEAR_NUM_MONTH = "yM";
    public static final String YEAR_NUM_MONTH_DAY = "yMd";
    public static final String YEAR_NUM_MONTH_WEEKDAY_DAY = "yMEd";
    public static final String YEAR_QUARTER = "yQQQQ";
    public static final int YEAR_WOY_FIELD = 18;
    @Deprecated
    public static final List<String> ZONE_SKELETONS = null;
    static final int currentSerialVersion = 1;
    private static final long serialVersionUID = 7218322306649953788L;
    private EnumSet<BooleanAttribute> booleanAttributes;
    protected Calendar calendar;
    private DisplayContext capitalizationSetting;
    protected NumberFormat numberFormat;
    private int serialVersionOnStream;

    public static class Field extends java.text.Format.Field {
        public static final Field AM_PM = null;
        private static final Field[] CAL_FIELDS = null;
        private static final int CAL_FIELD_COUNT = 0;
        public static final Field DAY_OF_MONTH = null;
        public static final Field DAY_OF_WEEK = null;
        public static final Field DAY_OF_WEEK_IN_MONTH = null;
        public static final Field DAY_OF_YEAR = null;
        public static final Field DOW_LOCAL = null;
        public static final Field ERA = null;
        public static final Field EXTENDED_YEAR = null;
        private static final Map<String, Field> FIELD_NAME_MAP = null;
        public static final Field HOUR0 = null;
        public static final Field HOUR1 = null;
        public static final Field HOUR_OF_DAY0 = null;
        public static final Field HOUR_OF_DAY1 = null;
        public static final Field JULIAN_DAY = null;
        public static final Field MILLISECOND = null;
        public static final Field MILLISECONDS_IN_DAY = null;
        public static final Field MINUTE = null;
        public static final Field MONTH = null;
        public static final Field QUARTER = null;
        @Deprecated
        public static final Field RELATED_YEAR = null;
        public static final Field SECOND = null;
        public static final Field TIME_SEPARATOR = null;
        public static final Field TIME_ZONE = null;
        public static final Field WEEK_OF_MONTH = null;
        public static final Field WEEK_OF_YEAR = null;
        public static final Field YEAR = null;
        public static final Field YEAR_WOY = null;
        private static final long serialVersionUID = -3627456821000730829L;
        private final int calendarField;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.DateFormat.Field.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.DateFormat.Field.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DateFormat.Field.<clinit>():void");
        }

        protected Field(String name, int calendarField) {
            super(name);
            this.calendarField = calendarField;
            if (getClass() == Field.class) {
                FIELD_NAME_MAP.put(name, this);
                if (calendarField >= 0 && calendarField < CAL_FIELD_COUNT) {
                    CAL_FIELDS[calendarField] = this;
                }
            }
        }

        public static Field ofCalendarField(int calendarField) {
            if (calendarField >= 0 && calendarField < CAL_FIELD_COUNT) {
                return CAL_FIELDS[calendarField];
            }
            throw new IllegalArgumentException("Calendar field number is out of range");
        }

        public int getCalendarField() {
            return this.calendarField;
        }

        protected Object readResolve() throws InvalidObjectException {
            if (getClass() != Field.class) {
                throw new InvalidObjectException("A subclass of DateFormat.Field must implement readResolve.");
            }
            Object o = FIELD_NAME_MAP.get(getName());
            if (o != null) {
                return o;
            }
            throw new InvalidObjectException("Unknown attribute name.");
        }
    }

    public enum BooleanAttribute {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.DateFormat.BooleanAttribute.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.DateFormat.BooleanAttribute.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DateFormat.BooleanAttribute.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.DateFormat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.DateFormat.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DateFormat.<clinit>():void");
    }

    public abstract StringBuffer format(Calendar calendar, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract void parse(String str, Calendar calendar, ParsePosition parsePosition);

    public final StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        if (obj instanceof Calendar) {
            return format((Calendar) obj, toAppendTo, fieldPosition);
        }
        if (obj instanceof Date) {
            return format((Date) obj, toAppendTo, fieldPosition);
        }
        if (obj instanceof Number) {
            return format(new Date(((Number) obj).longValue()), toAppendTo, fieldPosition);
        }
        throw new IllegalArgumentException("Cannot format given Object (" + obj.getClass().getName() + ") as a Date");
    }

    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        this.calendar.setTime(date);
        return format(this.calendar, toAppendTo, fieldPosition);
    }

    public final String format(Date date) {
        return format(date, new StringBuffer(64), new FieldPosition(FULL)).toString();
    }

    public Date parse(String text) throws ParseException {
        ParsePosition pos = new ParsePosition(FULL);
        Date result = parse(text, pos);
        if (pos.getIndex() != 0) {
            return result;
        }
        throw new ParseException("Unparseable date: \"" + text + "\"", pos.getErrorIndex());
    }

    public Date parse(String text, ParsePosition pos) {
        Date result = null;
        int start = pos.getIndex();
        TimeZone tzsav = this.calendar.getTimeZone();
        this.calendar.clear();
        parse(text, this.calendar, pos);
        if (pos.getIndex() != start) {
            try {
                result = this.calendar.getTime();
            } catch (IllegalArgumentException e) {
                pos.setIndex(start);
                pos.setErrorIndex(start);
            }
        }
        this.calendar.setTimeZone(tzsav);
        return result;
    }

    public Object parseObject(String source, ParsePosition pos) {
        return parse(source, pos);
    }

    public static final DateFormat getTimeInstance() {
        return get(NONE, MONTH_FIELD, ULocale.getDefault(Category.FORMAT), null);
    }

    public static final DateFormat getTimeInstance(int style) {
        return get(NONE, style, ULocale.getDefault(Category.FORMAT), null);
    }

    public static final DateFormat getTimeInstance(int style, Locale aLocale) {
        return get(NONE, style, ULocale.forLocale(aLocale), null);
    }

    public static final DateFormat getTimeInstance(int style, ULocale locale) {
        return get(NONE, style, locale, null);
    }

    public static final DateFormat getDateInstance() {
        return get(MONTH_FIELD, NONE, ULocale.getDefault(Category.FORMAT), null);
    }

    public static final DateFormat getDateInstance(int style) {
        return get(style, NONE, ULocale.getDefault(Category.FORMAT), null);
    }

    public static final DateFormat getDateInstance(int style, Locale aLocale) {
        return get(style, NONE, ULocale.forLocale(aLocale), null);
    }

    public static final DateFormat getDateInstance(int style, ULocale locale) {
        return get(style, NONE, locale, null);
    }

    public static final DateFormat getDateTimeInstance() {
        return get(MONTH_FIELD, MONTH_FIELD, ULocale.getDefault(Category.FORMAT), null);
    }

    public static final DateFormat getDateTimeInstance(int dateStyle, int timeStyle) {
        return get(dateStyle, timeStyle, ULocale.getDefault(Category.FORMAT), null);
    }

    public static final DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale aLocale) {
        return get(dateStyle, timeStyle, ULocale.forLocale(aLocale), null);
    }

    public static final DateFormat getDateTimeInstance(int dateStyle, int timeStyle, ULocale locale) {
        return get(dateStyle, timeStyle, locale, null);
    }

    public static final DateFormat getInstance() {
        return getDateTimeInstance(SHORT, SHORT);
    }

    public static Locale[] getAvailableLocales() {
        return ICUResourceBundle.getAvailableLocales();
    }

    public static ULocale[] getAvailableULocales() {
        return ICUResourceBundle.getAvailableULocales();
    }

    public void setCalendar(Calendar newCalendar) {
        this.calendar = newCalendar;
    }

    public Calendar getCalendar() {
        return this.calendar;
    }

    public void setNumberFormat(NumberFormat newNumberFormat) {
        this.numberFormat = newNumberFormat;
        this.numberFormat.setParseIntegerOnly(true);
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
        setBooleanAttribute(BooleanAttribute.PARSE_ALLOW_NUMERIC, lenient);
        setBooleanAttribute(BooleanAttribute.PARSE_ALLOW_WHITESPACE, lenient);
    }

    public boolean isLenient() {
        if (this.calendar.isLenient() && getBooleanAttribute(BooleanAttribute.PARSE_ALLOW_NUMERIC)) {
            return getBooleanAttribute(BooleanAttribute.PARSE_ALLOW_WHITESPACE);
        }
        return false;
    }

    public void setCalendarLenient(boolean lenient) {
        this.calendar.setLenient(lenient);
    }

    public boolean isCalendarLenient() {
        return this.calendar.isLenient();
    }

    public DateFormat setBooleanAttribute(BooleanAttribute key, boolean value) {
        if (key.equals(BooleanAttribute.PARSE_PARTIAL_MATCH)) {
            key = BooleanAttribute.PARSE_PARTIAL_LITERAL_MATCH;
        }
        if (value) {
            this.booleanAttributes.add(key);
        } else {
            this.booleanAttributes.remove(key);
        }
        return this;
    }

    public boolean getBooleanAttribute(BooleanAttribute key) {
        if (key == BooleanAttribute.PARSE_PARTIAL_MATCH) {
            key = BooleanAttribute.PARSE_PARTIAL_LITERAL_MATCH;
        }
        return this.booleanAttributes.contains(key);
    }

    public void setContext(DisplayContext context) {
        if (context.type() == Type.CAPITALIZATION) {
            this.capitalizationSetting = context;
        }
    }

    public DisplayContext getContext(Type type) {
        return (type != Type.CAPITALIZATION || this.capitalizationSetting == null) ? DisplayContext.CAPITALIZATION_NONE : this.capitalizationSetting;
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
        if ((this.calendar == null && other.calendar == null) || !(this.calendar == null || other.calendar == null || !this.calendar.isEquivalentTo(other.calendar))) {
            if (!(this.numberFormat == null && other.numberFormat == null)) {
                if (!(this.numberFormat == null || other.numberFormat == null || !this.numberFormat.equals(other.numberFormat))) {
                }
            }
            if (this.capitalizationSetting != other.capitalizationSetting) {
                z = false;
            }
            return z;
        }
        z = false;
        return z;
    }

    public Object clone() {
        DateFormat other = (DateFormat) super.clone();
        other.calendar = (Calendar) this.calendar.clone();
        if (this.numberFormat != null) {
            other.numberFormat = (NumberFormat) this.numberFormat.clone();
        }
        return other;
    }

    private static DateFormat get(int dateStyle, int timeStyle, ULocale loc, Calendar cal) {
        if ((timeStyle != NONE && (timeStyle & RELATIVE_FULL) > 0) || (dateStyle != NONE && (dateStyle & RELATIVE_FULL) > 0)) {
            return new RelativeDateFormat(timeStyle, dateStyle, loc, cal);
        }
        if (timeStyle < NONE || timeStyle > SHORT) {
            throw new IllegalArgumentException("Illegal time style " + timeStyle);
        } else if (dateStyle < NONE || dateStyle > SHORT) {
            throw new IllegalArgumentException("Illegal date style " + dateStyle);
        } else {
            if (cal == null) {
                cal = Calendar.getInstance(loc);
            }
            try {
                DateFormat result = cal.getDateTimeFormat(dateStyle, timeStyle, loc);
                result.setLocale(cal.getLocale(ULocale.VALID_LOCALE), cal.getLocale(ULocale.ACTUAL_LOCALE));
                return result;
            } catch (MissingResourceException e) {
                return new SimpleDateFormat("M/d/yy h:mm a");
            }
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (this.serialVersionOnStream < currentSerialVersion) {
            this.capitalizationSetting = DisplayContext.CAPITALIZATION_NONE;
        }
        if (this.booleanAttributes == null) {
            this.booleanAttributes = EnumSet.allOf(BooleanAttribute.class);
        }
        this.serialVersionOnStream = currentSerialVersion;
    }

    protected DateFormat() {
        this.booleanAttributes = EnumSet.allOf(BooleanAttribute.class);
        this.capitalizationSetting = DisplayContext.CAPITALIZATION_NONE;
        this.serialVersionOnStream = currentSerialVersion;
    }

    public static final DateFormat getDateInstance(Calendar cal, int dateStyle, Locale locale) {
        return getDateTimeInstance(cal, dateStyle, (int) NONE, ULocale.forLocale(locale));
    }

    public static final DateFormat getDateInstance(Calendar cal, int dateStyle, ULocale locale) {
        return getDateTimeInstance(cal, dateStyle, (int) NONE, locale);
    }

    public static final DateFormat getTimeInstance(Calendar cal, int timeStyle, Locale locale) {
        return getDateTimeInstance(cal, (int) NONE, timeStyle, ULocale.forLocale(locale));
    }

    public static final DateFormat getTimeInstance(Calendar cal, int timeStyle, ULocale locale) {
        return getDateTimeInstance(cal, (int) NONE, timeStyle, locale);
    }

    public static final DateFormat getDateTimeInstance(Calendar cal, int dateStyle, int timeStyle, Locale locale) {
        return getDateTimeInstance(dateStyle, timeStyle, ULocale.forLocale(locale));
    }

    public static final DateFormat getDateTimeInstance(Calendar cal, int dateStyle, int timeStyle, ULocale locale) {
        if (cal != null) {
            return get(dateStyle, timeStyle, locale, cal);
        }
        throw new IllegalArgumentException("Calendar must be supplied");
    }

    public static final DateFormat getInstance(Calendar cal, Locale locale) {
        return getDateTimeInstance(cal, (int) SHORT, (int) SHORT, ULocale.forLocale(locale));
    }

    public static final DateFormat getInstance(Calendar cal, ULocale locale) {
        return getDateTimeInstance(cal, (int) SHORT, (int) SHORT, locale);
    }

    public static final DateFormat getInstance(Calendar cal) {
        return getInstance(cal, ULocale.getDefault(Category.FORMAT));
    }

    public static final DateFormat getDateInstance(Calendar cal, int dateStyle) {
        return getDateInstance(cal, dateStyle, ULocale.getDefault(Category.FORMAT));
    }

    public static final DateFormat getTimeInstance(Calendar cal, int timeStyle) {
        return getTimeInstance(cal, timeStyle, ULocale.getDefault(Category.FORMAT));
    }

    public static final DateFormat getDateTimeInstance(Calendar cal, int dateStyle, int timeStyle) {
        return getDateTimeInstance(cal, dateStyle, timeStyle, ULocale.getDefault(Category.FORMAT));
    }

    public static final DateFormat getInstanceForSkeleton(String skeleton) {
        return getPatternInstance(skeleton, ULocale.getDefault(Category.FORMAT));
    }

    public static final DateFormat getInstanceForSkeleton(String skeleton, Locale locale) {
        return getPatternInstance(skeleton, ULocale.forLocale(locale));
    }

    public static final DateFormat getInstanceForSkeleton(String skeleton, ULocale locale) {
        return new SimpleDateFormat(DateTimePatternGenerator.getInstance(locale).getBestPattern(skeleton), locale);
    }

    public static final DateFormat getInstanceForSkeleton(Calendar cal, String skeleton, Locale locale) {
        return getPatternInstance(cal, skeleton, ULocale.forLocale(locale));
    }

    public static final DateFormat getInstanceForSkeleton(Calendar cal, String skeleton, ULocale locale) {
        SimpleDateFormat format = new SimpleDateFormat(DateTimePatternGenerator.getInstance(locale).getBestPattern(skeleton), locale);
        format.setCalendar(cal);
        return format;
    }

    public static final DateFormat getPatternInstance(String skeleton) {
        return getInstanceForSkeleton(skeleton);
    }

    public static final DateFormat getPatternInstance(String skeleton, Locale locale) {
        return getInstanceForSkeleton(skeleton, locale);
    }

    public static final DateFormat getPatternInstance(String skeleton, ULocale locale) {
        return getInstanceForSkeleton(skeleton, locale);
    }

    public static final DateFormat getPatternInstance(Calendar cal, String skeleton, Locale locale) {
        return getInstanceForSkeleton(cal, skeleton, locale);
    }

    public static final DateFormat getPatternInstance(Calendar cal, String skeleton, ULocale locale) {
        return getInstanceForSkeleton(cal, skeleton, locale);
    }
}

package java.time.format;

import java.io.IOException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.time.DateTimeException;
import java.time.Period;
import java.time.ZoneId;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQuery;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class DateTimeFormatter {
    public static final DateTimeFormatter BASIC_ISO_DATE = new DateTimeFormatterBuilder().parseCaseInsensitive().appendValue(ChronoField.YEAR, 4).appendValue(ChronoField.MONTH_OF_YEAR, 2).appendValue(ChronoField.DAY_OF_MONTH, 2).optionalStart().appendOffset("+HHMMss", "Z").toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
    public static final DateTimeFormatter ISO_DATE = new DateTimeFormatterBuilder().parseCaseInsensitive().append(ISO_LOCAL_DATE).optionalStart().appendOffsetId().toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
    public static final DateTimeFormatter ISO_DATE_TIME = new DateTimeFormatterBuilder().append(ISO_LOCAL_DATE_TIME).optionalStart().appendOffsetId().optionalStart().appendLiteral('[').parseCaseSensitive().appendZoneRegionId().appendLiteral(']').toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
    public static final DateTimeFormatter ISO_INSTANT = new DateTimeFormatterBuilder().parseCaseInsensitive().appendInstant().toFormatter(ResolverStyle.STRICT, null);
    public static final DateTimeFormatter ISO_LOCAL_DATE = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD).appendLiteral('-').appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral('-').appendValue(ChronoField.DAY_OF_MONTH, 2).toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
    public static final DateTimeFormatter ISO_LOCAL_DATE_TIME = new DateTimeFormatterBuilder().parseCaseInsensitive().append(ISO_LOCAL_DATE).appendLiteral('T').append(ISO_LOCAL_TIME).toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
    public static final DateTimeFormatter ISO_LOCAL_TIME = new DateTimeFormatterBuilder().appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral(':').appendValue(ChronoField.MINUTE_OF_HOUR, 2).optionalStart().appendLiteral(':').appendValue(ChronoField.SECOND_OF_MINUTE, 2).optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).toFormatter(ResolverStyle.STRICT, null);
    public static final DateTimeFormatter ISO_OFFSET_DATE = new DateTimeFormatterBuilder().parseCaseInsensitive().append(ISO_LOCAL_DATE).appendOffsetId().toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
    public static final DateTimeFormatter ISO_OFFSET_DATE_TIME = new DateTimeFormatterBuilder().parseCaseInsensitive().append(ISO_LOCAL_DATE_TIME).appendOffsetId().toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
    public static final DateTimeFormatter ISO_OFFSET_TIME = new DateTimeFormatterBuilder().parseCaseInsensitive().append(ISO_LOCAL_TIME).appendOffsetId().toFormatter(ResolverStyle.STRICT, null);
    public static final DateTimeFormatter ISO_ORDINAL_DATE = new DateTimeFormatterBuilder().parseCaseInsensitive().appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD).appendLiteral('-').appendValue(ChronoField.DAY_OF_YEAR, 3).optionalStart().appendOffsetId().toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
    public static final DateTimeFormatter ISO_TIME = new DateTimeFormatterBuilder().parseCaseInsensitive().append(ISO_LOCAL_TIME).optionalStart().appendOffsetId().toFormatter(ResolverStyle.STRICT, null);
    public static final DateTimeFormatter ISO_WEEK_DATE = new DateTimeFormatterBuilder().parseCaseInsensitive().appendValue(IsoFields.WEEK_BASED_YEAR, 4, 10, SignStyle.EXCEEDS_PAD).appendLiteral("-W").appendValue(IsoFields.WEEK_OF_WEEK_BASED_YEAR, 2).appendLiteral('-').appendValue(ChronoField.DAY_OF_WEEK, 1).optionalStart().appendOffsetId().toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
    public static final DateTimeFormatter ISO_ZONED_DATE_TIME = new DateTimeFormatterBuilder().append(ISO_OFFSET_DATE_TIME).optionalStart().appendLiteral('[').parseCaseSensitive().appendZoneRegionId().appendLiteral(']').toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
    private static final TemporalQuery<Period> PARSED_EXCESS_DAYS = new -$Lambda$0XYNFtuSwmPr7fM6_PYPS6Yf4g0();
    private static final TemporalQuery<Boolean> PARSED_LEAP_SECOND = new TemporalQuery() {
        public final Object queryFrom(TemporalAccessor temporalAccessor) {
            return $m$0(temporalAccessor);
        }
    };
    public static final DateTimeFormatter RFC_1123_DATE_TIME;
    private final Chronology chrono;
    private final DecimalStyle decimalStyle;
    private final Locale locale;
    private final CompositePrinterParser printerParser;
    private final Set<TemporalField> resolverFields;
    private final ResolverStyle resolverStyle;
    private final ZoneId zone;

    static class ClassicFormat extends Format {
        private final DateTimeFormatter formatter;
        private final TemporalQuery<?> parseType;

        public ClassicFormat(DateTimeFormatter formatter, TemporalQuery<?> parseType) {
            this.formatter = formatter;
            this.parseType = parseType;
        }

        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            Objects.requireNonNull(obj, "obj");
            Objects.requireNonNull((Object) toAppendTo, "toAppendTo");
            Objects.requireNonNull((Object) pos, "pos");
            if (obj instanceof TemporalAccessor) {
                pos.setBeginIndex(0);
                pos.setEndIndex(0);
                try {
                    this.formatter.formatTo((TemporalAccessor) obj, toAppendTo);
                    return toAppendTo;
                } catch (RuntimeException ex) {
                    throw new IllegalArgumentException(ex.getMessage(), ex);
                }
            }
            throw new IllegalArgumentException("Format target must implement TemporalAccessor");
        }

        public Object parseObject(String text) throws ParseException {
            Objects.requireNonNull((Object) text, "text");
            try {
                if (this.parseType == null) {
                    return this.formatter.parseResolved0(text, null);
                }
                return this.formatter.parse((CharSequence) text, this.parseType);
            } catch (DateTimeParseException ex) {
                throw new ParseException(ex.getMessage(), ex.getErrorIndex());
            } catch (RuntimeException ex2) {
                throw ((ParseException) new ParseException(ex2.getMessage(), 0).initCause(ex2));
            }
        }

        public Object parseObject(String text, ParsePosition pos) {
            Objects.requireNonNull((Object) text, "text");
            try {
                DateTimeParseContext context = this.formatter.parseUnresolved0(text, pos);
                if (context == null) {
                    if (pos.getErrorIndex() < 0) {
                        pos.setErrorIndex(0);
                    }
                    return null;
                }
                try {
                    TemporalAccessor resolved = context.toResolved(this.formatter.resolverStyle, this.formatter.resolverFields);
                    if (this.parseType == null) {
                        return resolved;
                    }
                    return resolved.query(this.parseType);
                } catch (RuntimeException e) {
                    pos.setErrorIndex(0);
                    return null;
                }
            } catch (IndexOutOfBoundsException e2) {
                if (pos.getErrorIndex() < 0) {
                    pos.setErrorIndex(0);
                }
                return null;
            }
        }
    }

    public static DateTimeFormatter ofPattern(String pattern) {
        return new DateTimeFormatterBuilder().appendPattern(pattern).toFormatter();
    }

    public static DateTimeFormatter ofPattern(String pattern, Locale locale) {
        return new DateTimeFormatterBuilder().appendPattern(pattern).toFormatter(locale);
    }

    public static DateTimeFormatter ofLocalizedDate(FormatStyle dateStyle) {
        Objects.requireNonNull((Object) dateStyle, "dateStyle");
        return new DateTimeFormatterBuilder().appendLocalized(dateStyle, null).toFormatter(ResolverStyle.SMART, IsoChronology.INSTANCE);
    }

    public static DateTimeFormatter ofLocalizedTime(FormatStyle timeStyle) {
        Objects.requireNonNull((Object) timeStyle, "timeStyle");
        return new DateTimeFormatterBuilder().appendLocalized(null, timeStyle).toFormatter(ResolverStyle.SMART, IsoChronology.INSTANCE);
    }

    public static DateTimeFormatter ofLocalizedDateTime(FormatStyle dateTimeStyle) {
        Objects.requireNonNull((Object) dateTimeStyle, "dateTimeStyle");
        return new DateTimeFormatterBuilder().appendLocalized(dateTimeStyle, dateTimeStyle).toFormatter(ResolverStyle.SMART, IsoChronology.INSTANCE);
    }

    public static DateTimeFormatter ofLocalizedDateTime(FormatStyle dateStyle, FormatStyle timeStyle) {
        Objects.requireNonNull((Object) dateStyle, "dateStyle");
        Objects.requireNonNull((Object) timeStyle, "timeStyle");
        return new DateTimeFormatterBuilder().appendLocalized(dateStyle, timeStyle).toFormatter(ResolverStyle.SMART, IsoChronology.INSTANCE);
    }

    static {
        Map dow = new HashMap();
        dow.put(Long.valueOf(1), "Mon");
        dow.put(Long.valueOf(2), "Tue");
        dow.put(Long.valueOf(3), "Wed");
        dow.put(Long.valueOf(4), "Thu");
        dow.put(Long.valueOf(5), "Fri");
        dow.put(Long.valueOf(6), "Sat");
        dow.put(Long.valueOf(7), "Sun");
        Map moy = new HashMap();
        moy.put(Long.valueOf(1), "Jan");
        moy.put(Long.valueOf(2), "Feb");
        moy.put(Long.valueOf(3), "Mar");
        moy.put(Long.valueOf(4), "Apr");
        moy.put(Long.valueOf(5), "May");
        moy.put(Long.valueOf(6), "Jun");
        moy.put(Long.valueOf(7), "Jul");
        moy.put(Long.valueOf(8), "Aug");
        moy.put(Long.valueOf(9), "Sep");
        moy.put(Long.valueOf(10), "Oct");
        moy.put(Long.valueOf(11), "Nov");
        moy.put(Long.valueOf(12), "Dec");
        RFC_1123_DATE_TIME = new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient().optionalStart().appendText(ChronoField.DAY_OF_WEEK, dow).appendLiteral(", ").optionalEnd().appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE).appendLiteral(' ').appendText(ChronoField.MONTH_OF_YEAR, moy).appendLiteral(' ').appendValue(ChronoField.YEAR, 4).appendLiteral(' ').appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral(':').appendValue(ChronoField.MINUTE_OF_HOUR, 2).optionalStart().appendLiteral(':').appendValue(ChronoField.SECOND_OF_MINUTE, 2).optionalEnd().appendLiteral(' ').appendOffset("+HHMM", "GMT").toFormatter(ResolverStyle.SMART, IsoChronology.INSTANCE);
    }

    public static final TemporalQuery<Period> parsedExcessDays() {
        return PARSED_EXCESS_DAYS;
    }

    static /* synthetic */ Period lambda$-java_time_format_DateTimeFormatter_61156(TemporalAccessor t) {
        if (t instanceof Parsed) {
            return ((Parsed) t).excessDays;
        }
        return Period.ZERO;
    }

    public static final TemporalQuery<Boolean> parsedLeapSecond() {
        return PARSED_LEAP_SECOND;
    }

    static /* synthetic */ Boolean lambda$-java_time_format_DateTimeFormatter_63118(TemporalAccessor t) {
        if (t instanceof Parsed) {
            return Boolean.valueOf(((Parsed) t).leapSecond);
        }
        return Boolean.FALSE;
    }

    DateTimeFormatter(CompositePrinterParser printerParser, Locale locale, DecimalStyle decimalStyle, ResolverStyle resolverStyle, Set<TemporalField> resolverFields, Chronology chrono, ZoneId zone) {
        this.printerParser = (CompositePrinterParser) Objects.requireNonNull((Object) printerParser, "printerParser");
        this.resolverFields = resolverFields;
        this.locale = (Locale) Objects.requireNonNull((Object) locale, "locale");
        this.decimalStyle = (DecimalStyle) Objects.requireNonNull((Object) decimalStyle, "decimalStyle");
        this.resolverStyle = (ResolverStyle) Objects.requireNonNull((Object) resolverStyle, "resolverStyle");
        this.chrono = chrono;
        this.zone = zone;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public DateTimeFormatter withLocale(Locale locale) {
        if (this.locale.equals(locale)) {
            return this;
        }
        return new DateTimeFormatter(this.printerParser, locale, this.decimalStyle, this.resolverStyle, this.resolverFields, this.chrono, this.zone);
    }

    public DecimalStyle getDecimalStyle() {
        return this.decimalStyle;
    }

    public DateTimeFormatter withDecimalStyle(DecimalStyle decimalStyle) {
        if (this.decimalStyle.equals(decimalStyle)) {
            return this;
        }
        return new DateTimeFormatter(this.printerParser, this.locale, decimalStyle, this.resolverStyle, this.resolverFields, this.chrono, this.zone);
    }

    public Chronology getChronology() {
        return this.chrono;
    }

    public DateTimeFormatter withChronology(Chronology chrono) {
        if (Objects.equals(this.chrono, chrono)) {
            return this;
        }
        return new DateTimeFormatter(this.printerParser, this.locale, this.decimalStyle, this.resolverStyle, this.resolverFields, chrono, this.zone);
    }

    public ZoneId getZone() {
        return this.zone;
    }

    public DateTimeFormatter withZone(ZoneId zone) {
        if (Objects.equals(this.zone, zone)) {
            return this;
        }
        return new DateTimeFormatter(this.printerParser, this.locale, this.decimalStyle, this.resolverStyle, this.resolverFields, this.chrono, zone);
    }

    public ResolverStyle getResolverStyle() {
        return this.resolverStyle;
    }

    public DateTimeFormatter withResolverStyle(ResolverStyle resolverStyle) {
        Objects.requireNonNull((Object) resolverStyle, "resolverStyle");
        if (Objects.equals(this.resolverStyle, resolverStyle)) {
            return this;
        }
        return new DateTimeFormatter(this.printerParser, this.locale, this.decimalStyle, resolverStyle, this.resolverFields, this.chrono, this.zone);
    }

    public Set<TemporalField> getResolverFields() {
        return this.resolverFields;
    }

    public DateTimeFormatter withResolverFields(TemporalField... resolverFields) {
        Set fields = null;
        if (resolverFields != null) {
            fields = Collections.unmodifiableSet(new HashSet(Arrays.asList(resolverFields)));
        }
        if (Objects.equals(this.resolverFields, fields)) {
            return this;
        }
        return new DateTimeFormatter(this.printerParser, this.locale, this.decimalStyle, this.resolverStyle, fields, this.chrono, this.zone);
    }

    public DateTimeFormatter withResolverFields(Set<TemporalField> resolverFields) {
        if (Objects.equals(this.resolverFields, resolverFields)) {
            return this;
        }
        if (resolverFields != null) {
            resolverFields = Collections.unmodifiableSet(new HashSet((Collection) resolverFields));
        }
        return new DateTimeFormatter(this.printerParser, this.locale, this.decimalStyle, this.resolverStyle, resolverFields, this.chrono, this.zone);
    }

    public String format(TemporalAccessor temporal) {
        StringBuilder buf = new StringBuilder(32);
        formatTo(temporal, buf);
        return buf.toString();
    }

    public void formatTo(TemporalAccessor temporal, Appendable appendable) {
        Objects.requireNonNull((Object) temporal, "temporal");
        Objects.requireNonNull((Object) appendable, "appendable");
        try {
            DateTimePrintContext context = new DateTimePrintContext(temporal, this);
            if (appendable instanceof StringBuilder) {
                this.printerParser.format(context, (StringBuilder) appendable);
                return;
            }
            CharSequence buf = new StringBuilder(32);
            this.printerParser.format(context, buf);
            appendable.append(buf);
        } catch (IOException ex) {
            throw new DateTimeException(ex.getMessage(), ex);
        }
    }

    public TemporalAccessor parse(CharSequence text) {
        Objects.requireNonNull((Object) text, "text");
        try {
            return parseResolved0(text, null);
        } catch (DateTimeParseException ex) {
            throw ex;
        } catch (RuntimeException ex2) {
            throw createError(text, ex2);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x0017 A:{Splitter: B:1:0x000c, ExcHandler: java.time.format.DateTimeParseException (r0_1 'ex' java.lang.RuntimeException)} */
    /* JADX WARNING: Missing block: B:7:0x0017, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:8:0x0018, code:
            throw r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public TemporalAccessor parse(CharSequence text, ParsePosition position) {
        Objects.requireNonNull((Object) text, "text");
        Objects.requireNonNull((Object) position, "position");
        try {
            return parseResolved0(text, position);
        } catch (RuntimeException ex) {
        } catch (RuntimeException ex2) {
            throw createError(text, ex2);
        }
    }

    public <T> T parse(CharSequence text, TemporalQuery<T> query) {
        Objects.requireNonNull((Object) text, "text");
        Objects.requireNonNull((Object) query, "query");
        try {
            return parseResolved0(text, null).query(query);
        } catch (DateTimeParseException ex) {
            throw ex;
        } catch (RuntimeException ex2) {
            throw createError(text, ex2);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x003a A:{Splitter: B:5:0x001a, ExcHandler: java.time.format.DateTimeParseException (r1_0 'ex' java.time.format.DateTimeParseException)} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:17:0x003a, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:18:0x003b, code:
            throw r1;
     */
    /* JADX WARNING: Missing block: B:19:0x003c, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:21:0x0041, code:
            throw createError(r8, r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public TemporalAccessor parseBest(CharSequence text, TemporalQuery<?>... queries) {
        Objects.requireNonNull((Object) text, "text");
        Objects.requireNonNull((Object) queries, "queries");
        if (queries.length < 2) {
            throw new IllegalArgumentException("At least two queries must be specified");
        }
        int i;
        try {
            TemporalAccessor resolved = parseResolved0(text, null);
            i = 0;
            while (i < queries.length) {
                return (TemporalAccessor) resolved.query(queries[i]);
            }
            throw new DateTimeException("Unable to convert parsed text using any of the specified queries");
        } catch (RuntimeException e) {
            i++;
        } catch (DateTimeParseException ex) {
        }
    }

    private DateTimeParseException createError(CharSequence text, RuntimeException ex) {
        String abbr;
        if (text.length() > 64) {
            abbr = text.subSequence(0, 64).toString() + "...";
        } else {
            abbr = text.toString();
        }
        return new DateTimeParseException("Text '" + abbr + "' could not be parsed: " + ex.getMessage(), text, 0, ex);
    }

    private TemporalAccessor parseResolved0(CharSequence text, ParsePosition position) {
        ParsePosition pos = position != null ? position : new ParsePosition(0);
        DateTimeParseContext context = parseUnresolved0(text, pos);
        if (context != null && pos.getErrorIndex() < 0 && (position != null || pos.getIndex() >= text.length())) {
            return context.toResolved(this.resolverStyle, this.resolverFields);
        }
        String abbr;
        if (text.length() > 64) {
            abbr = text.subSequence(0, 64).toString() + "...";
        } else {
            abbr = text.toString();
        }
        if (pos.getErrorIndex() >= 0) {
            throw new DateTimeParseException("Text '" + abbr + "' could not be parsed at index " + pos.getErrorIndex(), text, pos.getErrorIndex());
        }
        throw new DateTimeParseException("Text '" + abbr + "' could not be parsed, unparsed text found at index " + pos.getIndex(), text, pos.getIndex());
    }

    public TemporalAccessor parseUnresolved(CharSequence text, ParsePosition position) {
        DateTimeParseContext context = parseUnresolved0(text, position);
        if (context == null) {
            return null;
        }
        return context.toUnresolved();
    }

    private DateTimeParseContext parseUnresolved0(CharSequence text, ParsePosition position) {
        Objects.requireNonNull((Object) text, "text");
        Objects.requireNonNull((Object) position, "position");
        DateTimeParseContext context = new DateTimeParseContext(this);
        int pos = this.printerParser.parse(context, text, position.getIndex());
        if (pos < 0) {
            position.setErrorIndex(~pos);
            return null;
        }
        position.setIndex(pos);
        return context;
    }

    CompositePrinterParser toPrinterParser(boolean optional) {
        return this.printerParser.withOptional(optional);
    }

    public Format toFormat() {
        return new ClassicFormat(this, null);
    }

    public Format toFormat(TemporalQuery<?> parseQuery) {
        Objects.requireNonNull((Object) parseQuery, "parseQuery");
        return new ClassicFormat(this, parseQuery);
    }

    public String toString() {
        String pattern = this.printerParser.toString();
        if (pattern.startsWith("[")) {
            return pattern;
        }
        return pattern.substring(1, pattern.length() - 1);
    }
}

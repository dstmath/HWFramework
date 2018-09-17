package java.time.format;

import android.icu.impl.ZoneMeta;
import android.icu.text.LocaleDisplayNames;
import android.icu.text.TimeZoneNames;
import android.icu.text.TimeZoneNames.NameType;
import android.icu.util.Calendar;
import android.icu.util.ULocale;
import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Types;
import java.text.ParsePosition;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.ValueRange;
import java.time.temporal.WeekFields;
import java.time.zone.ZoneRulesProvider;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sun.util.locale.LanguageTag;

public final class DateTimeFormatterBuilder {
    private static final Map<Character, TemporalField> FIELD_MAP = new HashMap();
    static final Comparator<String> LENGTH_SORT = new Comparator<String>() {
        public int compare(String str1, String str2) {
            return str1.length() == str2.length() ? str1.compareTo(str2) : str1.length() - str2.length();
        }
    };
    private static final TemporalQuery<ZoneId> QUERY_REGION_ONLY = new -$Lambda$a1qgTVeqygBScuVh6yzVLwY_4Ag();
    private DateTimeFormatterBuilder active;
    private final boolean optional;
    private char padNextChar;
    private int padNextWidth;
    private final DateTimeFormatterBuilder parent;
    private final List<DateTimePrinterParser> printerParsers;
    private int valueParserIndex;

    interface DateTimePrinterParser {
        boolean format(DateTimePrintContext dateTimePrintContext, StringBuilder stringBuilder);

        int parse(DateTimeParseContext dateTimeParseContext, CharSequence charSequence, int i);
    }

    static final class CharLiteralPrinterParser implements DateTimePrinterParser {
        private final char literal;

        CharLiteralPrinterParser(char literal) {
            this.literal = literal;
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            buf.append(this.literal);
            return true;
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            if (position == text.length()) {
                return ~position;
            }
            char ch = text.charAt(position);
            if (ch == this.literal || (!context.isCaseSensitive() && (Character.toUpperCase(ch) == Character.toUpperCase(this.literal) || Character.toLowerCase(ch) == Character.toLowerCase(this.literal)))) {
                return position + 1;
            }
            return ~position;
        }

        public String toString() {
            if (this.literal == '\'') {
                return "''";
            }
            return "'" + this.literal + "'";
        }
    }

    static final class ChronoPrinterParser implements DateTimePrinterParser {
        private final TextStyle textStyle;

        ChronoPrinterParser(TextStyle textStyle) {
            this.textStyle = textStyle;
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Chronology chrono = (Chronology) context.getValue(TemporalQueries.chronology());
            if (chrono == null) {
                return false;
            }
            if (this.textStyle == null) {
                buf.append(chrono.getId());
            } else {
                buf.append(getChronologyName(chrono, context.getLocale()));
            }
            return true;
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            if (position < 0 || position > text.length()) {
                throw new IndexOutOfBoundsException();
            }
            Chronology bestMatch = null;
            int matchLen = -1;
            for (Chronology chrono : Chronology.getAvailableChronologies()) {
                String name;
                if (this.textStyle == null) {
                    name = chrono.getId();
                } else {
                    name = getChronologyName(chrono, context.getLocale());
                }
                int nameLen = name.length();
                if (nameLen > matchLen && context.subSequenceEquals(text, position, name, 0, nameLen)) {
                    bestMatch = chrono;
                    matchLen = nameLen;
                }
            }
            if (bestMatch == null) {
                return ~position;
            }
            context.setParsed(bestMatch);
            return position + matchLen;
        }

        private String getChronologyName(Chronology chrono, Locale locale) {
            String name = LocaleDisplayNames.getInstance(ULocale.forLocale(locale)).keyValueDisplayName("calendar", chrono.getCalendarType());
            return name != null ? name : chrono.getId();
        }
    }

    static final class CompositePrinterParser implements DateTimePrinterParser {
        private final boolean optional;
        private final DateTimePrinterParser[] printerParsers;

        CompositePrinterParser(List<DateTimePrinterParser> printerParsers, boolean optional) {
            this((DateTimePrinterParser[]) printerParsers.toArray(new DateTimePrinterParser[printerParsers.size()]), optional);
        }

        CompositePrinterParser(DateTimePrinterParser[] printerParsers, boolean optional) {
            this.printerParsers = printerParsers;
            this.optional = optional;
        }

        public CompositePrinterParser withOptional(boolean optional) {
            if (optional == this.optional) {
                return this;
            }
            return new CompositePrinterParser(this.printerParsers, optional);
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            int length = buf.length();
            if (this.optional) {
                context.startOptional();
            }
            try {
                DateTimePrinterParser[] dateTimePrinterParserArr = this.printerParsers;
                int i = 0;
                int length2 = dateTimePrinterParserArr.length;
                while (i < length2) {
                    if (dateTimePrinterParserArr[i].format(context, buf)) {
                        i++;
                    } else {
                        buf.setLength(length);
                        return true;
                    }
                }
                if (this.optional) {
                    context.endOptional();
                }
                return true;
            } finally {
                if (this.optional) {
                    context.endOptional();
                }
            }
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int i = 0;
            if (this.optional) {
                context.startOptional();
                int pos = position;
                for (DateTimePrinterParser pp : this.printerParsers) {
                    pos = pp.parse(context, text, pos);
                    if (pos < 0) {
                        context.endOptional(false);
                        return position;
                    }
                }
                context.endOptional(true);
                return pos;
            }
            DateTimePrinterParser[] dateTimePrinterParserArr = this.printerParsers;
            int length = dateTimePrinterParserArr.length;
            while (i < length) {
                position = dateTimePrinterParserArr[i].parse(context, text, position);
                if (position < 0) {
                    break;
                }
                i++;
            }
            return position;
        }

        public String toString() {
            StringBuilder buf = new StringBuilder();
            if (this.printerParsers != null) {
                buf.append(this.optional ? "[" : "(");
                for (Object pp : this.printerParsers) {
                    buf.append(pp);
                }
                buf.append(this.optional ? "]" : ")");
            }
            return buf.toString();
        }
    }

    static class DefaultValueParser implements DateTimePrinterParser {
        private final TemporalField field;
        private final long value;

        DefaultValueParser(TemporalField field, long value) {
            this.field = field;
            this.value = value;
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            return true;
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            if (context.getParsed(this.field) == null) {
                context.setParsedField(this.field, this.value, position, position);
            }
            return position;
        }
    }

    static final class FractionPrinterParser implements DateTimePrinterParser {
        private final boolean decimalPoint;
        private final TemporalField field;
        private final int maxWidth;
        private final int minWidth;

        FractionPrinterParser(TemporalField field, int minWidth, int maxWidth, boolean decimalPoint) {
            Objects.requireNonNull((Object) field, "field");
            if (!field.range().isFixed()) {
                throw new IllegalArgumentException("Field must have a fixed set of values: " + field);
            } else if (minWidth < 0 || minWidth > 9) {
                throw new IllegalArgumentException("Minimum width must be from 0 to 9 inclusive but was " + minWidth);
            } else if (maxWidth < 1 || maxWidth > 9) {
                throw new IllegalArgumentException("Maximum width must be from 1 to 9 inclusive but was " + maxWidth);
            } else if (maxWidth < minWidth) {
                throw new IllegalArgumentException("Maximum width must exceed or equal the minimum width but " + maxWidth + " < " + minWidth);
            } else {
                this.field = field;
                this.minWidth = minWidth;
                this.maxWidth = maxWidth;
                this.decimalPoint = decimalPoint;
            }
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long value = context.getValue(this.field);
            if (value == null) {
                return false;
            }
            DecimalStyle decimalStyle = context.getDecimalStyle();
            BigDecimal fraction = convertToFraction(value.longValue());
            if (fraction.scale() != 0) {
                String str = decimalStyle.convertNumberToI18N(fraction.setScale(Math.min(Math.max(fraction.scale(), this.minWidth), this.maxWidth), RoundingMode.FLOOR).toPlainString().substring(2));
                if (this.decimalPoint) {
                    buf.append(decimalStyle.getDecimalSeparator());
                }
                buf.append(str);
            } else if (this.minWidth > 0) {
                if (this.decimalPoint) {
                    buf.append(decimalStyle.getDecimalSeparator());
                }
                for (int i = 0; i < this.minWidth; i++) {
                    buf.append(decimalStyle.getZeroDigit());
                }
            }
            return true;
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int effectiveMin = context.isStrict() ? this.minWidth : 0;
            int effectiveMax = context.isStrict() ? this.maxWidth : 9;
            int length = text.length();
            if (position == length) {
                if (effectiveMin > 0) {
                    position = ~position;
                }
                return position;
            }
            if (this.decimalPoint) {
                if (text.charAt(position) != context.getDecimalStyle().getDecimalSeparator()) {
                    if (effectiveMin > 0) {
                        position = ~position;
                    }
                    return position;
                }
                position++;
            }
            int minEndPos = position + effectiveMin;
            if (minEndPos > length) {
                return ~position;
            }
            int maxEndPos = Math.min(position + effectiveMax, length);
            int total = 0;
            int pos = position;
            while (true) {
                int pos2 = pos;
                if (pos2 >= maxEndPos) {
                    pos = pos2;
                    break;
                }
                pos = pos2 + 1;
                int digit = context.getDecimalStyle().convertToDigit(text.charAt(pos2));
                if (digit >= 0) {
                    total = (total * 10) + digit;
                } else if (pos < minEndPos) {
                    return ~position;
                } else {
                    pos--;
                }
            }
            return context.setParsedField(this.field, convertFromFraction(new BigDecimal(total).movePointLeft(pos - position)), position, pos);
        }

        private BigDecimal convertToFraction(long value) {
            ValueRange range = this.field.range();
            range.checkValidValue(value, this.field);
            BigDecimal minBD = BigDecimal.valueOf(range.getMinimum());
            BigDecimal fraction = BigDecimal.valueOf(value).subtract(minBD).divide(BigDecimal.valueOf(range.getMaximum()).subtract(minBD).add(BigDecimal.ONE), 9, RoundingMode.FLOOR);
            return fraction.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : fraction.stripTrailingZeros();
        }

        private long convertFromFraction(BigDecimal fraction) {
            ValueRange range = this.field.range();
            BigDecimal minBD = BigDecimal.valueOf(range.getMinimum());
            return fraction.multiply(BigDecimal.valueOf(range.getMaximum()).subtract(minBD).add(BigDecimal.ONE)).setScale(0, RoundingMode.FLOOR).add(minBD).longValueExact();
        }

        public String toString() {
            return "Fraction(" + this.field + "," + this.minWidth + "," + this.maxWidth + (this.decimalPoint ? ",DecimalPoint" : "") + ")";
        }
    }

    static final class InstantPrinterParser implements DateTimePrinterParser {
        private static final long SECONDS_0000_TO_1970 = 62167219200L;
        private static final long SECONDS_PER_10000_YEARS = 315569520000L;
        private final int fractionalDigits;

        InstantPrinterParser(int fractionalDigits) {
            this.fractionalDigits = fractionalDigits;
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long inSecs = context.getValue((TemporalField) ChronoField.INSTANT_SECONDS);
            Long inNanos = null;
            if (context.getTemporal().isSupported(ChronoField.NANO_OF_SECOND)) {
                inNanos = Long.valueOf(context.getTemporal().getLong(ChronoField.NANO_OF_SECOND));
            }
            if (inSecs == null) {
                return false;
            }
            long inSec = inSecs.longValue();
            int inNano = ChronoField.NANO_OF_SECOND.checkValidIntValue(inNanos != null ? inNanos.longValue() : 0);
            long zeroSecs;
            long hi;
            Object ldt;
            if (inSec >= -62167219200L) {
                zeroSecs = (inSec - SECONDS_PER_10000_YEARS) + SECONDS_0000_TO_1970;
                hi = Math.floorDiv(zeroSecs, (long) SECONDS_PER_10000_YEARS) + 1;
                ldt = LocalDateTime.ofEpochSecond(Math.floorMod(zeroSecs, (long) SECONDS_PER_10000_YEARS) - SECONDS_0000_TO_1970, 0, ZoneOffset.UTC);
                if (hi > 0) {
                    buf.append('+').append(hi);
                }
                buf.append(ldt);
                if (ldt.getSecond() == 0) {
                    buf.append(":00");
                }
            } else {
                zeroSecs = inSec + SECONDS_0000_TO_1970;
                hi = zeroSecs / SECONDS_PER_10000_YEARS;
                long lo = zeroSecs % SECONDS_PER_10000_YEARS;
                ldt = LocalDateTime.ofEpochSecond(lo - SECONDS_0000_TO_1970, 0, ZoneOffset.UTC);
                int pos = buf.length();
                buf.append(ldt);
                if (ldt.getSecond() == 0) {
                    buf.append(":00");
                }
                if (hi < 0) {
                    if (ldt.getYear() == -10000) {
                        buf.replace(pos, pos + 2, Long.toString(hi - 1));
                    } else if (lo == 0) {
                        buf.insert(pos, hi);
                    } else {
                        buf.insert(pos + 1, Math.abs(hi));
                    }
                }
            }
            if ((this.fractionalDigits < 0 && inNano > 0) || this.fractionalDigits > 0) {
                buf.append('.');
                int div = 100000000;
                int i = 0;
                while (true) {
                    if ((this.fractionalDigits != -1 || inNano <= 0) && ((this.fractionalDigits != -2 || (inNano <= 0 && i % 3 == 0)) && i >= this.fractionalDigits)) {
                        break;
                    }
                    int digit = inNano / div;
                    buf.append((char) (digit + 48));
                    inNano -= digit * div;
                    div /= 10;
                    i++;
                }
            }
            buf.append('Z');
            return true;
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            CompositePrinterParser parser = new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_DATE).appendLiteral('T').appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral(':').appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral(':').appendValue(ChronoField.SECOND_OF_MINUTE, 2).appendFraction(ChronoField.NANO_OF_SECOND, this.fractionalDigits < 0 ? 0 : this.fractionalDigits, this.fractionalDigits < 0 ? 9 : this.fractionalDigits, true).appendLiteral('Z').toFormatter().toPrinterParser(false);
            DateTimeParseContext newContext = context.copy();
            int pos = parser.parse(newContext, text, position);
            if (pos < 0) {
                return pos;
            }
            long yearParsed = newContext.getParsed(ChronoField.YEAR).longValue();
            int month = newContext.getParsed(ChronoField.MONTH_OF_YEAR).intValue();
            int day = newContext.getParsed(ChronoField.DAY_OF_MONTH).intValue();
            int hour = newContext.getParsed(ChronoField.HOUR_OF_DAY).intValue();
            int min = newContext.getParsed(ChronoField.MINUTE_OF_HOUR).intValue();
            Long secVal = newContext.getParsed(ChronoField.SECOND_OF_MINUTE);
            Long nanoVal = newContext.getParsed(ChronoField.NANO_OF_SECOND);
            int sec = secVal != null ? secVal.intValue() : 0;
            int nano = nanoVal != null ? nanoVal.intValue() : 0;
            int days = 0;
            if (hour == 24 && min == 0 && sec == 0 && nano == 0) {
                hour = 0;
                days = 1;
            } else if (hour == 23 && min == 59 && sec == 60) {
                context.setParsedLeapSecond();
                sec = 59;
            }
            try {
                int successPos = pos;
                return context.setParsedField(ChronoField.NANO_OF_SECOND, (long) nano, position, context.setParsedField(ChronoField.INSTANT_SECONDS, LocalDateTime.of(((int) yearParsed) % 10000, month, day, hour, min, sec, 0).plusDays((long) days).toEpochSecond(ZoneOffset.UTC) + Math.multiplyExact(yearParsed / 10000, (long) SECONDS_PER_10000_YEARS), position, pos));
            } catch (RuntimeException e) {
                return ~position;
            }
        }

        public String toString() {
            return "Instant()";
        }
    }

    static final class LocalizedOffsetIdPrinterParser implements DateTimePrinterParser {
        private final TextStyle style;

        LocalizedOffsetIdPrinterParser(TextStyle style) {
            this.style = style;
        }

        private static StringBuilder appendHMS(StringBuilder buf, int t) {
            return buf.append((char) ((t / 10) + 48)).append((char) ((t % 10) + 48));
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long offsetSecs = context.getValue(ChronoField.OFFSET_SECONDS);
            if (offsetSecs == null) {
                return false;
            }
            String gmtText = "GMT";
            if (gmtText != null) {
                buf.append(gmtText);
            }
            int totalSecs = Math.toIntExact(offsetSecs.longValue());
            if (totalSecs != 0) {
                int absHours = Math.abs((totalSecs / 3600) % 100);
                int absMinutes = Math.abs((totalSecs / 60) % 60);
                int absSeconds = Math.abs(totalSecs % 60);
                buf.append(totalSecs < 0 ? LanguageTag.SEP : "+");
                if (this.style == TextStyle.FULL) {
                    appendHMS(buf, absHours);
                    buf.append(':');
                    appendHMS(buf, absMinutes);
                    if (absSeconds != 0) {
                        buf.append(':');
                        appendHMS(buf, absSeconds);
                    }
                } else {
                    if (absHours >= 10) {
                        buf.append((char) ((absHours / 10) + 48));
                    }
                    buf.append((char) ((absHours % 10) + 48));
                    if (!(absMinutes == 0 && absSeconds == 0)) {
                        buf.append(':');
                        appendHMS(buf, absMinutes);
                        if (absSeconds != 0) {
                            buf.append(':');
                            appendHMS(buf, absSeconds);
                        }
                    }
                }
            }
            return true;
        }

        int getDigit(CharSequence text, int position) {
            char c = text.charAt(position);
            if (c < '0' || c > '9') {
                return -1;
            }
            return c - 48;
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int pos = position;
            int end = position + text.length();
            String gmtText = "GMT";
            if (gmtText != null) {
                if (!context.subSequenceEquals(text, position, gmtText, 0, gmtText.length())) {
                    return ~position;
                }
                pos = position + gmtText.length();
            }
            if (pos == end) {
                return context.setParsedField(ChronoField.OFFSET_SECONDS, 0, position, pos);
            }
            int negative;
            int h;
            char sign = text.charAt(pos);
            if (sign == '+') {
                negative = 1;
            } else if (sign == '-') {
                negative = -1;
            } else {
                return context.setParsedField(ChronoField.OFFSET_SECONDS, 0, position, pos);
            }
            pos++;
            int m = 0;
            int s = 0;
            int i;
            int h2;
            int m1;
            int m2;
            int s1;
            int s2;
            if (this.style == TextStyle.FULL) {
                i = pos + 1;
                int h1 = getDigit(text, pos);
                pos = i + 1;
                h2 = getDigit(text, i);
                if (h1 >= 0 && h2 >= 0) {
                    i = pos + 1;
                    if (text.charAt(pos) != ':') {
                        pos = i;
                    } else {
                        h = (h1 * 10) + h2;
                        pos = i + 1;
                        m1 = getDigit(text, i);
                        i = pos + 1;
                        m2 = getDigit(text, pos);
                        if (m1 < 0 || m2 < 0) {
                            return ~position;
                        }
                        m = (m1 * 10) + m2;
                        if (i + 2 >= end || text.charAt(i) != ':') {
                            pos = i;
                        } else {
                            s1 = getDigit(text, i + 1);
                            s2 = getDigit(text, i + 2);
                            if (s1 < 0 || s2 < 0) {
                                pos = i;
                            } else {
                                s = (s1 * 10) + s2;
                                pos = i + 3;
                            }
                        }
                    }
                }
                return ~position;
            }
            i = pos + 1;
            h = getDigit(text, pos);
            if (h < 0) {
                return ~position;
            }
            if (i < end) {
                h2 = getDigit(text, i);
                if (h2 >= 0) {
                    h = (h * 10) + h2;
                    pos = i + 1;
                } else {
                    pos = i;
                }
                if (pos + 2 < end && text.charAt(pos) == ':' && pos + 2 < end && text.charAt(pos) == ':') {
                    m1 = getDigit(text, pos + 1);
                    m2 = getDigit(text, pos + 2);
                    if (m1 >= 0 && m2 >= 0) {
                        m = (m1 * 10) + m2;
                        pos += 3;
                        if (pos + 2 < end && text.charAt(pos) == ':') {
                            s1 = getDigit(text, pos + 1);
                            s2 = getDigit(text, pos + 2);
                            if (s1 >= 0 && s2 >= 0) {
                                s = (s1 * 10) + s2;
                                pos += 3;
                            }
                        }
                    }
                }
            } else {
                pos = i;
            }
            return context.setParsedField(ChronoField.OFFSET_SECONDS, ((long) negative) * (((((long) h) * 3600) + (((long) m) * 60)) + ((long) s)), position, pos);
        }

        public String toString() {
            return "LocalizedOffset(" + this.style + ")";
        }
    }

    static final class LocalizedPrinterParser implements DateTimePrinterParser {
        private static final ConcurrentMap<String, DateTimeFormatter> FORMATTER_CACHE = new ConcurrentHashMap(16, 0.75f, 2);
        private final FormatStyle dateStyle;
        private final FormatStyle timeStyle;

        LocalizedPrinterParser(FormatStyle dateStyle, FormatStyle timeStyle) {
            this.dateStyle = dateStyle;
            this.timeStyle = timeStyle;
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            return formatter(context.getLocale(), Chronology.from(context.getTemporal())).toPrinterParser(false).format(context, buf);
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            return formatter(context.getLocale(), context.getEffectiveChronology()).toPrinterParser(false).parse(context, text, position);
        }

        private DateTimeFormatter formatter(Locale locale, Chronology chrono) {
            String key = chrono.getId() + '|' + locale.toString() + '|' + this.dateStyle + this.timeStyle;
            DateTimeFormatter formatter = (DateTimeFormatter) FORMATTER_CACHE.get(key);
            if (formatter != null) {
                return formatter;
            }
            formatter = new DateTimeFormatterBuilder().appendPattern(DateTimeFormatterBuilder.getLocalizedDateTimePattern(this.dateStyle, this.timeStyle, chrono, locale)).toFormatter(locale);
            DateTimeFormatter old = (DateTimeFormatter) FORMATTER_CACHE.putIfAbsent(key, formatter);
            if (old != null) {
                return old;
            }
            return formatter;
        }

        public String toString() {
            return "Localized(" + (this.dateStyle != null ? this.dateStyle : "") + "," + (this.timeStyle != null ? this.timeStyle : "") + ")";
        }
    }

    static class NumberPrinterParser implements DateTimePrinterParser {
        private static final /* synthetic */ int[] -java-time-format-SignStyleSwitchesValues = null;
        static final long[] EXCEED_POINTS = new long[]{0, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000, 10000000000L};
        final TemporalField field;
        final int maxWidth;
        final int minWidth;
        private final SignStyle signStyle;
        final int subsequentWidth;

        private static /* synthetic */ int[] -getjava-time-format-SignStyleSwitchesValues() {
            if (-java-time-format-SignStyleSwitchesValues != null) {
                return -java-time-format-SignStyleSwitchesValues;
            }
            int[] iArr = new int[SignStyle.values().length];
            try {
                iArr[SignStyle.ALWAYS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[SignStyle.EXCEEDS_PAD.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[SignStyle.NEVER.ordinal()] = 5;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[SignStyle.NORMAL.ordinal()] = 3;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[SignStyle.NOT_NEGATIVE.ordinal()] = 4;
            } catch (NoSuchFieldError e5) {
            }
            -java-time-format-SignStyleSwitchesValues = iArr;
            return iArr;
        }

        NumberPrinterParser(TemporalField field, int minWidth, int maxWidth, SignStyle signStyle) {
            this.field = field;
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
            this.signStyle = signStyle;
            this.subsequentWidth = 0;
        }

        protected NumberPrinterParser(TemporalField field, int minWidth, int maxWidth, SignStyle signStyle, int subsequentWidth) {
            this.field = field;
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
            this.signStyle = signStyle;
            this.subsequentWidth = subsequentWidth;
        }

        NumberPrinterParser withFixedWidth() {
            if (this.subsequentWidth == -1) {
                return this;
            }
            return new NumberPrinterParser(this.field, this.minWidth, this.maxWidth, this.signStyle, -1);
        }

        NumberPrinterParser withSubsequentWidth(int subsequentWidth) {
            return new NumberPrinterParser(this.field, this.minWidth, this.maxWidth, this.signStyle, this.subsequentWidth + subsequentWidth);
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long valueLong = context.getValue(this.field);
            if (valueLong == null) {
                return false;
            }
            long value = getValue(context, valueLong.longValue());
            DecimalStyle decimalStyle = context.getDecimalStyle();
            String str = value == Long.MIN_VALUE ? "9223372036854775808" : Long.toString(Math.abs(value));
            if (str.length() > this.maxWidth) {
                throw new DateTimeException("Field " + this.field + " cannot be printed as the value " + value + " exceeds the maximum print width of " + this.maxWidth);
            }
            str = decimalStyle.convertNumberToI18N(str);
            if (value >= 0) {
                switch (-getjava-time-format-SignStyleSwitchesValues()[this.signStyle.ordinal()]) {
                    case 1:
                        buf.append(decimalStyle.getPositiveSign());
                        break;
                    case 2:
                        if (this.minWidth < 19 && value >= EXCEED_POINTS[this.minWidth]) {
                            buf.append(decimalStyle.getPositiveSign());
                            break;
                        }
                }
            }
            switch (-getjava-time-format-SignStyleSwitchesValues()[this.signStyle.ordinal()]) {
                case 1:
                case 2:
                case 3:
                    buf.append(decimalStyle.getNegativeSign());
                    break;
                case 4:
                    throw new DateTimeException("Field " + this.field + " cannot be printed as the value " + value + " cannot be negative according to the SignStyle");
            }
            for (int i = 0; i < this.minWidth - str.length(); i++) {
                buf.append(decimalStyle.getZeroDigit());
            }
            buf.append(str);
            return true;
        }

        long getValue(DateTimePrintContext context, long value) {
            return value;
        }

        boolean isFixedWidth(DateTimeParseContext context) {
            if (this.subsequentWidth != -1) {
                return this.subsequentWidth > 0 && this.minWidth == this.maxWidth && this.signStyle == SignStyle.NOT_NEGATIVE;
            } else {
                return true;
            }
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int length = text.length();
            if (position == length) {
                return ~position;
            }
            char sign = text.charAt(position);
            boolean negative = false;
            boolean positive = false;
            if (sign == context.getDecimalStyle().getPositiveSign()) {
                if (!this.signStyle.parse(true, context.isStrict(), this.minWidth == this.maxWidth)) {
                    return ~position;
                }
                positive = true;
                position++;
            } else if (sign == context.getDecimalStyle().getNegativeSign()) {
                if (!this.signStyle.parse(false, context.isStrict(), this.minWidth == this.maxWidth)) {
                    return ~position;
                }
                negative = true;
                position++;
            } else if (this.signStyle == SignStyle.ALWAYS && context.isStrict()) {
                return ~position;
            }
            int effMinWidth = (context.isStrict() || isFixedWidth(context)) ? this.minWidth : 1;
            int minEndPos = position + effMinWidth;
            if (minEndPos > length) {
                return ~position;
            }
            int i = (context.isStrict() || isFixedWidth(context)) ? this.maxWidth : 9;
            int effMaxWidth = i + Math.max(this.subsequentWidth, 0);
            long total = 0;
            BigInteger totalBig = null;
            int pos = position;
            int pass = 0;
            while (pass < 2) {
                int maxEndPos = Math.min(pos + effMaxWidth, length);
                while (true) {
                    int pos2 = pos;
                    if (pos2 >= maxEndPos) {
                        pos = pos2;
                        break;
                    }
                    pos = pos2 + 1;
                    int digit = context.getDecimalStyle().convertToDigit(text.charAt(pos2));
                    if (digit < 0) {
                        pos--;
                        if (pos < minEndPos) {
                            return ~position;
                        }
                    } else if (pos - position > 18) {
                        if (totalBig == null) {
                            totalBig = BigInteger.valueOf(total);
                        }
                        totalBig = totalBig.multiply(BigInteger.TEN).add(BigInteger.valueOf((long) digit));
                    } else {
                        total = (10 * total) + ((long) digit);
                    }
                }
                if (this.subsequentWidth <= 0 || pass != 0) {
                    break;
                }
                effMaxWidth = Math.max(effMinWidth, (pos - position) - this.subsequentWidth);
                pos = position;
                total = 0;
                totalBig = null;
                pass++;
            }
            if (negative) {
                if (totalBig != null) {
                    if (totalBig.equals(BigInteger.ZERO) && context.isStrict()) {
                        return ~(position - 1);
                    }
                    totalBig = totalBig.negate();
                } else if (total == 0 && context.isStrict()) {
                    return ~(position - 1);
                } else {
                    total = -total;
                }
            } else if (this.signStyle == SignStyle.EXCEEDS_PAD && context.isStrict()) {
                int parseLen = pos - position;
                if (positive) {
                    if (parseLen <= this.minWidth) {
                        return ~(position - 1);
                    }
                } else if (parseLen > this.minWidth) {
                    return ~position;
                }
            }
            if (totalBig == null) {
                return setValue(context, total, position, pos);
            }
            if (totalBig.bitLength() > 63) {
                totalBig = totalBig.divide(BigInteger.TEN);
                pos--;
            }
            return setValue(context, totalBig.longValue(), position, pos);
        }

        int setValue(DateTimeParseContext context, long value, int errorPos, int successPos) {
            return context.setParsedField(this.field, value, errorPos, successPos);
        }

        public String toString() {
            if (this.minWidth == 1 && this.maxWidth == 19 && this.signStyle == SignStyle.NORMAL) {
                return "Value(" + this.field + ")";
            }
            if (this.minWidth == this.maxWidth && this.signStyle == SignStyle.NOT_NEGATIVE) {
                return "Value(" + this.field + "," + this.minWidth + ")";
            }
            return "Value(" + this.field + "," + this.minWidth + "," + this.maxWidth + "," + this.signStyle + ")";
        }
    }

    static final class OffsetIdPrinterParser implements DateTimePrinterParser {
        static final OffsetIdPrinterParser INSTANCE_ID_Z = new OffsetIdPrinterParser("+HH:MM:ss", "Z");
        static final OffsetIdPrinterParser INSTANCE_ID_ZERO = new OffsetIdPrinterParser("+HH:MM:ss", "0");
        static final String[] PATTERNS = new String[]{"+HH", "+HHmm", "+HH:mm", "+HHMM", "+HH:MM", "+HHMMss", "+HH:MM:ss", "+HHMMSS", "+HH:MM:SS"};
        private final String noOffsetText;
        private final int type;

        OffsetIdPrinterParser(String pattern, String noOffsetText) {
            Objects.requireNonNull((Object) pattern, "pattern");
            Objects.requireNonNull((Object) noOffsetText, "noOffsetText");
            this.type = checkPattern(pattern);
            this.noOffsetText = noOffsetText;
        }

        private int checkPattern(String pattern) {
            for (int i = 0; i < PATTERNS.length; i++) {
                if (PATTERNS[i].equals(pattern)) {
                    return i;
                }
            }
            throw new IllegalArgumentException("Invalid zone offset pattern: " + pattern);
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long offsetSecs = context.getValue(ChronoField.OFFSET_SECONDS);
            if (offsetSecs == null) {
                return false;
            }
            int totalSecs = Math.toIntExact(offsetSecs.longValue());
            if (totalSecs == 0) {
                buf.append(this.noOffsetText);
            } else {
                int absHours = Math.abs((totalSecs / 3600) % 100);
                int absMinutes = Math.abs((totalSecs / 60) % 60);
                int absSeconds = Math.abs(totalSecs % 60);
                int bufPos = buf.length();
                int output = absHours;
                buf.append(totalSecs < 0 ? LanguageTag.SEP : "+").append((char) ((absHours / 10) + 48)).append((char) ((absHours % 10) + 48));
                if (this.type >= 3 || (this.type >= 1 && absMinutes > 0)) {
                    buf.append(this.type % 2 == 0 ? ":" : "").append((char) ((absMinutes / 10) + 48)).append((char) ((absMinutes % 10) + 48));
                    output = absHours + absMinutes;
                    if (this.type >= 7 || (this.type >= 5 && absSeconds > 0)) {
                        buf.append(this.type % 2 == 0 ? ":" : "").append((char) ((absSeconds / 10) + 48)).append((char) ((absSeconds % 10) + 48));
                        output += absSeconds;
                    }
                }
                if (output == 0) {
                    buf.setLength(bufPos);
                    buf.append(this.noOffsetText);
                }
            }
            return true;
        }

        /* JADX WARNING: Removed duplicated region for block: B:30:0x00a0  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int length = text.length();
            int noOffsetLen = this.noOffsetText.length();
            if (noOffsetLen == 0) {
                if (position == length) {
                    return context.setParsedField(ChronoField.OFFSET_SECONDS, 0, position, position);
                }
            } else if (position == length) {
                return ~position;
            } else {
                if (context.subSequenceEquals(text, position, this.noOffsetText, 0, noOffsetLen)) {
                    return context.setParsedField(ChronoField.OFFSET_SECONDS, 0, position, position + noOffsetLen);
                }
            }
            char sign = text.charAt(position);
            if (sign == '+' || sign == '-') {
                boolean parseNumber;
                int negative = sign == '-' ? -1 : 1;
                int[] array = new int[4];
                array[0] = position + 1;
                if (!parseNumber(array, 1, text, true)) {
                    if (!parseNumber(array, 2, text, this.type >= 3)) {
                        parseNumber = parseNumber(array, 3, text, false);
                        if (!parseNumber) {
                            return context.setParsedField(ChronoField.OFFSET_SECONDS, ((long) negative) * (((((long) array[1]) * 3600) + (((long) array[2]) * 60)) + ((long) array[3])), position, array[0]);
                        }
                    }
                }
                parseNumber = true;
                if (parseNumber) {
                }
            }
            if (noOffsetLen != 0) {
                return ~position;
            }
            return context.setParsedField(ChronoField.OFFSET_SECONDS, 0, position, position + noOffsetLen);
        }

        private boolean parseNumber(int[] array, int arrayIndex, CharSequence parseText, boolean required) {
            if ((this.type + 3) / 2 < arrayIndex) {
                return false;
            }
            int pos = array[0];
            if (this.type % 2 == 0 && arrayIndex > 1) {
                if (pos + 1 > parseText.length() || parseText.charAt(pos) != ':') {
                    return required;
                }
                pos++;
            }
            if (pos + 2 > parseText.length()) {
                return required;
            }
            int pos2 = pos + 1;
            char ch1 = parseText.charAt(pos);
            pos = pos2 + 1;
            char ch2 = parseText.charAt(pos2);
            if (ch1 < '0' || ch1 > '9' || ch2 < '0' || ch2 > '9') {
                return required;
            }
            int value = ((ch1 - 48) * 10) + (ch2 - 48);
            if (value < 0 || value > 59) {
                return required;
            }
            array[arrayIndex] = value;
            array[0] = pos;
            return false;
        }

        public String toString() {
            return "Offset(" + PATTERNS[this.type] + ",'" + this.noOffsetText.replace((CharSequence) "'", (CharSequence) "''") + "')";
        }
    }

    static final class PadPrinterParserDecorator implements DateTimePrinterParser {
        private final char padChar;
        private final int padWidth;
        private final DateTimePrinterParser printerParser;

        PadPrinterParserDecorator(DateTimePrinterParser printerParser, int padWidth, char padChar) {
            this.printerParser = printerParser;
            this.padWidth = padWidth;
            this.padChar = padChar;
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            int preLen = buf.length();
            if (!this.printerParser.format(context, buf)) {
                return false;
            }
            int len = buf.length() - preLen;
            if (len > this.padWidth) {
                throw new DateTimeException("Cannot print as output of " + len + " characters exceeds pad width of " + this.padWidth);
            }
            for (int i = 0; i < this.padWidth - len; i++) {
                buf.insert(preLen, this.padChar);
            }
            return true;
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            boolean strict = context.isStrict();
            if (position > text.length()) {
                throw new IndexOutOfBoundsException();
            } else if (position == text.length()) {
                return ~position;
            } else {
                int endPos = position + this.padWidth;
                if (endPos > text.length()) {
                    if (strict) {
                        return ~position;
                    }
                    endPos = text.length();
                }
                int pos = position;
                while (pos < endPos && context.charEquals(text.charAt(pos), this.padChar)) {
                    pos++;
                }
                int resultPos = this.printerParser.parse(context, text.subSequence(0, endPos), pos);
                if (resultPos == endPos || !strict) {
                    return resultPos;
                }
                return ~(position + pos);
            }
        }

        public String toString() {
            return "Pad(" + this.printerParser + "," + this.padWidth + (this.padChar == ' ' ? ")" : ",'" + this.padChar + "')");
        }
    }

    static class PrefixTree {
        protected char c0;
        protected PrefixTree child;
        protected String key;
        protected PrefixTree sibling;
        protected String value;

        private static class CI extends PrefixTree {
            /* synthetic */ CI(String k, String v, PrefixTree child, CI -this3) {
                this(k, v, child);
            }

            private CI(String k, String v, PrefixTree child) {
                super(k, v, child, null);
            }

            protected CI newNode(String k, String v, PrefixTree child) {
                return new CI(k, v, child);
            }

            protected boolean isEqual(char c1, char c2) {
                return DateTimeParseContext.charEqualsIgnoreCase(c1, c2);
            }

            protected boolean prefixOf(CharSequence text, int off, int end) {
                int length = this.key.length();
                if (length > end - off) {
                    return false;
                }
                int i = 0;
                while (true) {
                    int off0 = i;
                    int len = length;
                    int off2 = off;
                    length = len - 1;
                    if (len <= 0) {
                        return true;
                    }
                    i = off0 + 1;
                    off = off2 + 1;
                    if (!isEqual(this.key.charAt(off0), text.charAt(off2))) {
                        return false;
                    }
                }
            }
        }

        private static class LENIENT extends CI {
            private LENIENT(String k, String v, PrefixTree child) {
                super(k, v, child, null);
            }

            protected CI newNode(String k, String v, PrefixTree child) {
                return new LENIENT(k, v, child);
            }

            private boolean isLenientChar(char c) {
                return c == ' ' || c == '_' || c == '/';
            }

            protected String toKey(String k) {
                int i = 0;
                while (i < k.length()) {
                    if (isLenientChar(k.charAt(i))) {
                        StringBuilder sb = new StringBuilder(k.length());
                        sb.append((CharSequence) k, 0, i);
                        while (true) {
                            i++;
                            if (i >= k.length()) {
                                return sb.toString();
                            }
                            if (!isLenientChar(k.charAt(i))) {
                                sb.append(k.charAt(i));
                            }
                        }
                    } else {
                        i++;
                    }
                }
                return k;
            }

            public String match(CharSequence text, ParsePosition pos) {
                int off = pos.getIndex();
                int end = text.length();
                int len = this.key.length();
                int koff = 0;
                int off2 = off;
                while (koff < len && off2 < end) {
                    if (isLenientChar(text.charAt(off2))) {
                        off2++;
                    } else {
                        int koff2 = koff + 1;
                        off = off2 + 1;
                        if (!isEqual(this.key.charAt(koff), text.charAt(off2))) {
                            return null;
                        }
                        koff = koff2;
                        off2 = off;
                    }
                }
                if (koff != len) {
                    return null;
                }
                if (this.child != null && off2 != end) {
                    int off0 = off2;
                    while (off0 < end && isLenientChar(text.charAt(off0))) {
                        off0++;
                    }
                    if (off0 < end) {
                        PrefixTree c = this.child;
                        while (!isEqual(c.c0, text.charAt(off0))) {
                            c = c.sibling;
                            if (c == null) {
                                break;
                            }
                        }
                        pos.setIndex(off0);
                        String found = c.match(text, pos);
                        if (found != null) {
                            return found;
                        }
                    }
                }
                pos.setIndex(off2);
                return this.value;
            }
        }

        /* synthetic */ PrefixTree(String k, String v, PrefixTree child, PrefixTree -this3) {
            this(k, v, child);
        }

        private PrefixTree(String k, String v, PrefixTree child) {
            this.key = k;
            this.value = v;
            this.child = child;
            if (k.length() == 0) {
                this.c0 = 65535;
            } else {
                this.c0 = this.key.charAt(0);
            }
        }

        public static PrefixTree newTree(DateTimeParseContext context) {
            if (context.isCaseSensitive()) {
                return new PrefixTree("", null, null);
            }
            return new CI("", null, null, null);
        }

        public static PrefixTree newTree(Set<String> keys, DateTimeParseContext context) {
            PrefixTree tree = newTree(context);
            for (String k : keys) {
                tree.add0(k, k);
            }
            return tree;
        }

        public PrefixTree copyTree() {
            PrefixTree copy = new PrefixTree(this.key, this.value, null);
            if (this.child != null) {
                copy.child = this.child.copyTree();
            }
            if (this.sibling != null) {
                copy.sibling = this.sibling.copyTree();
            }
            return copy;
        }

        public boolean add(String k, String v) {
            return add0(k, v);
        }

        private boolean add0(String k, String v) {
            k = toKey(k);
            int prefixLen = prefixLength(k);
            if (prefixLen != this.key.length()) {
                PrefixTree n1 = newNode(this.key.substring(prefixLen), this.value, this.child);
                this.key = k.substring(0, prefixLen);
                this.child = n1;
                if (prefixLen < k.length()) {
                    this.child.sibling = newNode(k.substring(prefixLen), v, null);
                    this.value = null;
                } else {
                    this.value = v;
                }
                return true;
            } else if (prefixLen < k.length()) {
                PrefixTree c;
                String subKey = k.substring(prefixLen);
                for (c = this.child; c != null; c = c.sibling) {
                    if (isEqual(c.c0, subKey.charAt(0))) {
                        return c.add0(subKey, v);
                    }
                }
                c = newNode(subKey, v, null);
                c.sibling = this.child;
                this.child = c;
                return true;
            } else {
                this.value = v;
                return true;
            }
        }

        public String match(CharSequence text, int off, int end) {
            if (!prefixOf(text, off, end)) {
                return null;
            }
            if (this.child != null) {
                off += this.key.length();
                if (off != end) {
                    PrefixTree c = this.child;
                    while (!isEqual(c.c0, text.charAt(off))) {
                        c = c.sibling;
                        if (c == null) {
                        }
                    }
                    String found = c.match(text, off, end);
                    if (found != null) {
                        return found;
                    }
                    return this.value;
                }
            }
            return this.value;
        }

        public String match(CharSequence text, ParsePosition pos) {
            int off = pos.getIndex();
            int end = text.length();
            if (!prefixOf(text, off, end)) {
                return null;
            }
            off += this.key.length();
            if (this.child != null && off != end) {
                PrefixTree c = this.child;
                while (!isEqual(c.c0, text.charAt(off))) {
                    c = c.sibling;
                    if (c == null) {
                        break;
                    }
                }
                pos.setIndex(off);
                String found = c.match(text, pos);
                if (found != null) {
                    return found;
                }
            }
            pos.setIndex(off);
            return this.value;
        }

        protected String toKey(String k) {
            return k;
        }

        protected PrefixTree newNode(String k, String v, PrefixTree child) {
            return new PrefixTree(k, v, child);
        }

        protected boolean isEqual(char c1, char c2) {
            return c1 == c2;
        }

        protected boolean prefixOf(CharSequence text, int off, int end) {
            if (text instanceof String) {
                return ((String) text).startsWith(this.key, off);
            }
            int length = this.key.length();
            if (length > end - off) {
                return false;
            }
            int i = 0;
            while (true) {
                int off0 = i;
                int len = length;
                int off2 = off;
                length = len - 1;
                if (len <= 0) {
                    return true;
                }
                i = off0 + 1;
                off = off2 + 1;
                if (!isEqual(this.key.charAt(off0), text.charAt(off2))) {
                    return false;
                }
            }
        }

        /* JADX WARNING: Missing block: B:9:0x0023, code:
            return r0;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private int prefixLength(String k) {
            int off = 0;
            while (off < k.length() && off < this.key.length() && isEqual(k.charAt(off), this.key.charAt(off))) {
                off++;
            }
            return off;
        }
    }

    static final class ReducedPrinterParser extends NumberPrinterParser {
        static final LocalDate BASE_DATE = LocalDate.of((int) Types.JAVA_OBJECT, 1, 1);
        private final ChronoLocalDate baseDate;
        private final int baseValue;

        /* synthetic */ ReducedPrinterParser(TemporalField field, int minWidth, int maxWidth, int baseValue, ChronoLocalDate baseDate, int subsequentWidth, ReducedPrinterParser -this6) {
            this(field, minWidth, maxWidth, baseValue, baseDate, subsequentWidth);
        }

        ReducedPrinterParser(TemporalField field, int minWidth, int maxWidth, int baseValue, ChronoLocalDate baseDate) {
            this(field, minWidth, maxWidth, baseValue, baseDate, 0);
            if (minWidth < 1 || minWidth > 10) {
                throw new IllegalArgumentException("The minWidth must be from 1 to 10 inclusive but was " + minWidth);
            } else if (maxWidth < 1 || maxWidth > 10) {
                throw new IllegalArgumentException("The maxWidth must be from 1 to 10 inclusive but was " + minWidth);
            } else if (maxWidth < minWidth) {
                throw new IllegalArgumentException("Maximum width must exceed or equal the minimum width but " + maxWidth + " < " + minWidth);
            } else if (baseDate != null) {
            } else {
                if (!field.range().isValidValue((long) baseValue)) {
                    throw new IllegalArgumentException("The base value must be within the range of the field");
                } else if (((long) baseValue) + EXCEED_POINTS[maxWidth] > 2147483647L) {
                    throw new DateTimeException("Unable to add printer-parser as the range exceeds the capacity of an int");
                }
            }
        }

        private ReducedPrinterParser(TemporalField field, int minWidth, int maxWidth, int baseValue, ChronoLocalDate baseDate, int subsequentWidth) {
            super(field, minWidth, maxWidth, SignStyle.NOT_NEGATIVE, subsequentWidth);
            this.baseValue = baseValue;
            this.baseDate = baseDate;
        }

        long getValue(DateTimePrintContext context, long value) {
            long absValue = Math.abs(value);
            int baseValue = this.baseValue;
            if (this.baseDate != null) {
                baseValue = Chronology.from(context.getTemporal()).date(this.baseDate).get(this.field);
            }
            if (value < ((long) baseValue) || value >= ((long) baseValue) + EXCEED_POINTS[this.minWidth]) {
                return absValue % EXCEED_POINTS[this.maxWidth];
            }
            return absValue % EXCEED_POINTS[this.minWidth];
        }

        /* renamed from: setValue */
        int lambda$-java_time_format_DateTimeFormatterBuilder$ReducedPrinterParser_132487(DateTimeParseContext context, long value, int errorPos, int successPos) {
            int baseValue = this.baseValue;
            if (this.baseDate != null) {
                baseValue = context.getEffectiveChronology().date(this.baseDate).get(this.field);
                long initialValue = value;
                context.addChronoChangedListener(new java.time.format.-$Lambda$a1qgTVeqygBScuVh6yzVLwY_4Ag.AnonymousClass1(errorPos, successPos, value, this, context));
            }
            if (successPos - errorPos == this.minWidth && value >= 0) {
                long range = EXCEED_POINTS[this.minWidth];
                long basePart = ((long) baseValue) - (((long) baseValue) % range);
                if (baseValue > 0) {
                    value += basePart;
                } else {
                    value = basePart - value;
                }
                if (value < ((long) baseValue)) {
                    value += range;
                }
            }
            return context.setParsedField(this.field, value, errorPos, successPos);
        }

        ReducedPrinterParser withFixedWidth() {
            if (this.subsequentWidth == -1) {
                return this;
            }
            return new ReducedPrinterParser(this.field, this.minWidth, this.maxWidth, this.baseValue, this.baseDate, -1);
        }

        ReducedPrinterParser withSubsequentWidth(int subsequentWidth) {
            return new ReducedPrinterParser(this.field, this.minWidth, this.maxWidth, this.baseValue, this.baseDate, this.subsequentWidth + subsequentWidth);
        }

        boolean isFixedWidth(DateTimeParseContext context) {
            if (context.isStrict()) {
                return super.isFixedWidth(context);
            }
            return false;
        }

        public String toString() {
            return "ReducedValue(" + this.field + "," + this.minWidth + "," + this.maxWidth + "," + (this.baseDate != null ? this.baseDate : Integer.valueOf(this.baseValue)) + ")";
        }
    }

    enum SettingsParser implements DateTimePrinterParser {
        SENSITIVE,
        INSENSITIVE,
        STRICT,
        LENIENT;

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            return true;
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            switch (ordinal()) {
                case 0:
                    context.setCaseSensitive(true);
                    break;
                case 1:
                    context.setCaseSensitive(false);
                    break;
                case 2:
                    context.setStrict(true);
                    break;
                case 3:
                    context.setStrict(false);
                    break;
            }
            return position;
        }

        public String toString() {
            switch (ordinal()) {
                case 0:
                    return "ParseCaseSensitive(true)";
                case 1:
                    return "ParseCaseSensitive(false)";
                case 2:
                    return "ParseStrict(true)";
                case 3:
                    return "ParseStrict(false)";
                default:
                    throw new IllegalStateException("Unreachable");
            }
        }
    }

    static final class StringLiteralPrinterParser implements DateTimePrinterParser {
        private final String literal;

        StringLiteralPrinterParser(String literal) {
            this.literal = literal;
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            buf.append(this.literal);
            return true;
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            if (position > text.length() || position < 0) {
                throw new IndexOutOfBoundsException();
            }
            if (context.subSequenceEquals(text, position, this.literal, 0, this.literal.length())) {
                return this.literal.length() + position;
            }
            return ~position;
        }

        public String toString() {
            return "'" + this.literal.replace((CharSequence) "'", (CharSequence) "''") + "'";
        }
    }

    static final class TextPrinterParser implements DateTimePrinterParser {
        private final TemporalField field;
        private volatile NumberPrinterParser numberPrinterParser;
        private final DateTimeTextProvider provider;
        private final TextStyle textStyle;

        TextPrinterParser(TemporalField field, TextStyle textStyle, DateTimeTextProvider provider) {
            this.field = field;
            this.textStyle = textStyle;
            this.provider = provider;
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long value = context.getValue(this.field);
            if (value == null) {
                return false;
            }
            String text;
            Chronology chrono = (Chronology) context.getTemporal().query(TemporalQueries.chronology());
            if (chrono == null || chrono == IsoChronology.INSTANCE) {
                text = this.provider.getText(this.field, value.longValue(), this.textStyle, context.getLocale());
            } else {
                text = this.provider.getText(chrono, this.field, value.longValue(), this.textStyle, context.getLocale());
            }
            if (text == null) {
                return numberPrinterParser().format(context, buf);
            }
            buf.append(text);
            return true;
        }

        public int parse(DateTimeParseContext context, CharSequence parseText, int position) {
            int length = parseText.length();
            if (position < 0 || position > length) {
                throw new IndexOutOfBoundsException();
            }
            Iterator<Entry<String, Long>> it;
            TextStyle style = context.isStrict() ? this.textStyle : null;
            Chronology chrono = context.getEffectiveChronology();
            if (chrono == null || chrono == IsoChronology.INSTANCE) {
                it = this.provider.getTextIterator(this.field, style, context.getLocale());
            } else {
                it = this.provider.getTextIterator(chrono, this.field, style, context.getLocale());
            }
            if (it != null) {
                while (it.hasNext()) {
                    Entry<String, Long> entry = (Entry) it.next();
                    String itText = (String) entry.getKey();
                    if (context.subSequenceEquals(itText, 0, parseText, position, itText.length())) {
                        return context.setParsedField(this.field, ((Long) entry.getValue()).longValue(), position, position + itText.length());
                    }
                }
                if (context.isStrict()) {
                    return ~position;
                }
            }
            return numberPrinterParser().parse(context, parseText, position);
        }

        private NumberPrinterParser numberPrinterParser() {
            if (this.numberPrinterParser == null) {
                this.numberPrinterParser = new NumberPrinterParser(this.field, 1, 19, SignStyle.NORMAL);
            }
            return this.numberPrinterParser;
        }

        public String toString() {
            if (this.textStyle == TextStyle.FULL) {
                return "Text(" + this.field + ")";
            }
            return "Text(" + this.field + "," + this.textStyle + ")";
        }
    }

    static final class WeekBasedFieldPrinterParser implements DateTimePrinterParser {
        private char chr;
        private int count;

        WeekBasedFieldPrinterParser(char chr, int count) {
            this.chr = chr;
            this.count = count;
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            return printerParser(context.getLocale()).format(context, buf);
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            return printerParser(context.getLocale()).parse(context, text, position);
        }

        private DateTimePrinterParser printerParser(Locale locale) {
            TemporalField field;
            int i;
            WeekFields weekDef = WeekFields.of(locale);
            switch (this.chr) {
                case 'W':
                    field = weekDef.weekOfMonth();
                    break;
                case 'Y':
                    field = weekDef.weekBasedYear();
                    if (this.count == 2) {
                        return new ReducedPrinterParser(field, 2, 2, 0, ReducedPrinterParser.BASE_DATE, 0, null);
                    }
                    return new NumberPrinterParser(field, this.count, 19, this.count < 4 ? SignStyle.NORMAL : SignStyle.EXCEEDS_PAD, -1);
                case 'c':
                case 'e':
                    field = weekDef.dayOfWeek();
                    break;
                case 'w':
                    field = weekDef.weekOfWeekBasedYear();
                    break;
                default:
                    throw new IllegalStateException("unreachable");
            }
            if (this.count == 2) {
                i = 2;
            } else {
                i = 1;
            }
            return new NumberPrinterParser(field, i, 2, SignStyle.NOT_NEGATIVE);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(30);
            sb.append("Localized(");
            if (this.chr != 'Y') {
                switch (this.chr) {
                    case 'W':
                        sb.append("WeekOfMonth");
                        break;
                    case 'c':
                    case 'e':
                        sb.append("DayOfWeek");
                        break;
                    case 'w':
                        sb.append("WeekOfWeekBasedYear");
                        break;
                }
                sb.append(",");
                sb.append(this.count);
            } else if (this.count == 1) {
                sb.append("WeekBasedYear");
            } else if (this.count == 2) {
                sb.append("ReducedValue(WeekBasedYear,2,2,2000-01-01)");
            } else {
                sb.append("WeekBasedYear,").append(this.count).append(",").append(19).append(",").append(this.count < 4 ? SignStyle.NORMAL : SignStyle.EXCEEDS_PAD);
            }
            sb.append(")");
            return sb.toString();
        }
    }

    static class ZoneIdPrinterParser implements DateTimePrinterParser {
        private static volatile Entry<Integer, PrefixTree> cachedPrefixTree;
        private static volatile Entry<Integer, PrefixTree> cachedPrefixTreeCI;
        private final String description;
        private final TemporalQuery<ZoneId> query;

        ZoneIdPrinterParser(TemporalQuery<ZoneId> query, String description) {
            this.query = query;
            this.description = description;
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            ZoneId zone = (ZoneId) context.getValue(this.query);
            if (zone == null) {
                return false;
            }
            buf.append(zone.getId());
            return true;
        }

        protected PrefixTree getTree(DateTimeParseContext context) {
            Throwable th;
            Set<String> regionIds = ZoneRulesProvider.getAvailableZoneIds();
            int regionIdsSize = regionIds.size();
            Entry<Integer, PrefixTree> cached = context.isCaseSensitive() ? cachedPrefixTree : cachedPrefixTreeCI;
            if (cached == null || ((Integer) cached.getKey()).intValue() != regionIdsSize) {
                synchronized (this) {
                    try {
                        Entry<Integer, PrefixTree> cached2 = context.isCaseSensitive() ? cachedPrefixTree : cachedPrefixTreeCI;
                        if (cached2 != null) {
                            try {
                                if (((Integer) cached2.getKey()).intValue() == regionIdsSize) {
                                    cached = cached2;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                cached = cached2;
                                throw th;
                            }
                        }
                        cached = new SimpleImmutableEntry(Integer.valueOf(regionIdsSize), PrefixTree.newTree(regionIds, context));
                        if (context.isCaseSensitive()) {
                            cachedPrefixTree = cached;
                        } else {
                            cachedPrefixTreeCI = cached;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
            }
            return (PrefixTree) cached.getValue();
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int length = text.length();
            if (position > length) {
                throw new IndexOutOfBoundsException();
            } else if (position == length) {
                return ~position;
            } else {
                char nextChar = text.charAt(position);
                if (nextChar == '+' || nextChar == '-') {
                    return parseOffsetBased(context, text, position, position, OffsetIdPrinterParser.INSTANCE_ID_Z);
                }
                if (length >= position + 2) {
                    char nextNextChar = text.charAt(position + 1);
                    if (context.charEquals(nextChar, 'U') && context.charEquals(nextNextChar, 'T')) {
                        if (length < position + 3 || !context.charEquals(text.charAt(position + 2), 'C')) {
                            return parseOffsetBased(context, text, position, position + 2, OffsetIdPrinterParser.INSTANCE_ID_ZERO);
                        }
                        return parseOffsetBased(context, text, position, position + 3, OffsetIdPrinterParser.INSTANCE_ID_ZERO);
                    } else if (context.charEquals(nextChar, 'G') && length >= position + 3 && context.charEquals(nextNextChar, 'M') && context.charEquals(text.charAt(position + 2), 'T')) {
                        return parseOffsetBased(context, text, position, position + 3, OffsetIdPrinterParser.INSTANCE_ID_ZERO);
                    }
                }
                PrefixTree tree = getTree(context);
                ParsePosition ppos = new ParsePosition(position);
                String parsedZoneId = tree.match(text, ppos);
                if (parsedZoneId != null) {
                    context.setParsed(ZoneId.of(parsedZoneId));
                    return ppos.getIndex();
                } else if (!context.charEquals(nextChar, 'Z')) {
                    return ~position;
                } else {
                    context.setParsed(ZoneOffset.UTC);
                    return position + 1;
                }
            }
        }

        private int parseOffsetBased(DateTimeParseContext context, CharSequence text, int prefixPos, int position, OffsetIdPrinterParser parser) {
            String prefix = text.toString().substring(prefixPos, position).toUpperCase();
            if (position >= text.length()) {
                context.setParsed(ZoneId.of(prefix));
                return position;
            } else if (text.charAt(position) == '0' && prefix.equals("GMT")) {
                context.setParsed(ZoneId.of("GMT0"));
                return position + 1;
            } else if (text.charAt(position) == '0' || context.charEquals(text.charAt(position), 'Z')) {
                context.setParsed(ZoneId.of(prefix));
                return position;
            } else {
                DateTimeParseContext newContext = context.copy();
                int endPos = parser.parse(newContext, text, position);
                if (endPos < 0) {
                    try {
                        if (parser == OffsetIdPrinterParser.INSTANCE_ID_Z) {
                            return ~prefixPos;
                        }
                        context.setParsed(ZoneId.of(prefix));
                        return position;
                    } catch (DateTimeException e) {
                        return ~prefixPos;
                    }
                }
                context.setParsed(ZoneId.ofOffset(prefix, ZoneOffset.ofTotalSeconds((int) newContext.getParsed(ChronoField.OFFSET_SECONDS).longValue())));
                return endPos;
            }
        }

        public String toString() {
            return this.description;
        }
    }

    static final class ZoneTextPrinterParser extends ZoneIdPrinterParser {
        private static final int DST = 1;
        private static final NameType[] FULL_TYPES = new NameType[]{NameType.LONG_STANDARD, NameType.LONG_DAYLIGHT, NameType.LONG_GENERIC};
        private static final int GENERIC = 2;
        private static final NameType[] SHORT_TYPES = new NameType[]{NameType.SHORT_STANDARD, NameType.SHORT_DAYLIGHT, NameType.SHORT_GENERIC};
        private static final int STD = 0;
        private static final NameType[] TYPES = new NameType[]{NameType.LONG_STANDARD, NameType.SHORT_STANDARD, NameType.LONG_DAYLIGHT, NameType.SHORT_DAYLIGHT, NameType.LONG_GENERIC, NameType.SHORT_GENERIC};
        private static final Map<String, SoftReference<Map<Locale, String[]>>> cache = new ConcurrentHashMap();
        private final Map<Locale, Entry<Integer, SoftReference<PrefixTree>>> cachedTree = new HashMap();
        private final Map<Locale, Entry<Integer, SoftReference<PrefixTree>>> cachedTreeCI = new HashMap();
        private Set<String> preferredZones;
        private final TextStyle textStyle;

        ZoneTextPrinterParser(TextStyle textStyle, Set<ZoneId> preferredZones) {
            super(TemporalQueries.zone(), "ZoneText(" + textStyle + ")");
            this.textStyle = (TextStyle) Objects.requireNonNull((Object) textStyle, "textStyle");
            if (preferredZones != null && preferredZones.size() != 0) {
                this.preferredZones = new HashSet();
                for (ZoneId id : preferredZones) {
                    this.preferredZones.add(id.getId());
                }
            }
        }

        /* JADX WARNING: Missing block: B:13:0x0041, code:
            if (r6 != null) goto L_0x0043;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private String getDisplayName(String id, int type, Locale locale) {
            if (this.textStyle == TextStyle.NARROW) {
                return null;
            }
            String[] names;
            SoftReference<Map<Locale, String[]>> ref = (SoftReference) cache.get(id);
            Map perLocale = null;
            if (ref != null) {
                perLocale = (Map) ref.get();
                if (perLocale != null) {
                    names = (String[]) perLocale.get(locale);
                }
            }
            TimeZoneNames timeZoneNames = TimeZoneNames.getInstance(locale);
            names = new String[(TYPES.length + 1)];
            names[0] = id;
            timeZoneNames.getDisplayNames(ZoneMeta.getCanonicalCLDRID(id), TYPES, System.currentTimeMillis(), names, 1);
            if (names == null) {
                return null;
            }
            if (names[1] == null || names[2] == null || names[3] == null || names[4] == null) {
                String str;
                TimeZone tz = TimeZone.getTimeZone(id);
                String stdString = TimeZone.createGmtOffsetString(true, true, tz.getRawOffset());
                String dstString = TimeZone.createGmtOffsetString(true, true, tz.getRawOffset() + tz.getDSTSavings());
                if (names[1] != null) {
                    str = names[1];
                } else {
                    str = stdString;
                }
                names[1] = str;
                if (names[2] != null) {
                    stdString = names[2];
                }
                names[2] = stdString;
                if (names[3] != null) {
                    str = names[3];
                } else {
                    str = dstString;
                }
                names[3] = str;
                if (names[4] != null) {
                    dstString = names[4];
                }
                names[4] = dstString;
            }
            if (names[5] == null) {
                names[5] = names[0];
            }
            if (names[6] == null) {
                names[6] = names[0];
            }
            if (perLocale == null) {
                perLocale = new ConcurrentHashMap();
            }
            perLocale.put(locale, names);
            cache.put(id, new SoftReference(perLocale));
            switch (type) {
                case 0:
                    return names[this.textStyle.zoneNameStyleIndex() + 1];
                case 1:
                    return names[this.textStyle.zoneNameStyleIndex() + 3];
                default:
                    return names[this.textStyle.zoneNameStyleIndex() + 5];
            }
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            int i = 0;
            ZoneId zone = (ZoneId) context.getValue(TemporalQueries.zoneId());
            if (zone == null) {
                return false;
            }
            String zname = zone.getId();
            if (!(zone instanceof ZoneOffset)) {
                TemporalAccessor dt = context.getTemporal();
                if (!dt.isSupported(ChronoField.INSTANT_SECONDS)) {
                    i = 2;
                } else if (zone.getRules().isDaylightSavings(Instant.from(dt))) {
                    i = 1;
                }
                String name = getDisplayName(zname, i, context.getLocale());
                if (name != null) {
                    zname = name;
                }
            }
            buf.append(zname);
            return true;
        }

        /* JADX WARNING: Missing block: B:12:0x0047, code:
            if (r22 == null) goto L_0x0049;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected PrefixTree getTree(DateTimeParseContext context) {
            if (this.textStyle == TextStyle.NARROW) {
                return super.getTree(context);
            }
            PrefixTree tree;
            String zid;
            int i;
            Locale locale = context.getLocale();
            boolean isCaseSensitive = context.isCaseSensitive();
            Set<String> regionIds = ZoneRulesProvider.getAvailableZoneIds();
            int regionIdsSize = regionIds.size();
            Map<Locale, Entry<Integer, SoftReference<PrefixTree>>> cached = isCaseSensitive ? this.cachedTree : this.cachedTreeCI;
            Entry<Integer, SoftReference<PrefixTree>> entry = (Entry) cached.get(locale);
            if (entry != null && ((Integer) entry.getKey()).intValue() == regionIdsSize) {
                tree = (PrefixTree) ((SoftReference) entry.getValue()).get();
            }
            tree = PrefixTree.newTree(context);
            TimeZoneNames timeZoneNames = TimeZoneNames.getInstance(locale);
            long now = System.currentTimeMillis();
            NameType[] types = this.textStyle == TextStyle.FULL ? FULL_TYPES : SHORT_TYPES;
            String[] names = new String[types.length];
            for (String zid2 : regionIds) {
                tree.add(zid2, zid2);
                zid2 = ZoneName.toZid(zid2, locale);
                timeZoneNames.getDisplayNames(zid2, types, now, names, 0);
                for (i = 0; i < names.length; i++) {
                    if (names[i] != null) {
                        tree.add(names[i], zid2);
                    }
                }
            }
            if (this.preferredZones != null) {
                for (String zid22 : regionIds) {
                    if (this.preferredZones.contains(zid22)) {
                        timeZoneNames.getDisplayNames(ZoneName.toZid(zid22, locale), types, now, names, 0);
                        for (i = 0; i < names.length; i++) {
                            if (names[i] != null) {
                                tree.add(names[i], zid22);
                            }
                        }
                    }
                }
            }
            cached.put(locale, new SimpleImmutableEntry(Integer.valueOf(regionIdsSize), new SoftReference(tree)));
            return tree;
        }
    }

    static {
        FIELD_MAP.put(Character.valueOf('G'), ChronoField.ERA);
        FIELD_MAP.put(Character.valueOf('y'), ChronoField.YEAR_OF_ERA);
        FIELD_MAP.put(Character.valueOf('u'), ChronoField.YEAR);
        FIELD_MAP.put(Character.valueOf('Q'), IsoFields.QUARTER_OF_YEAR);
        FIELD_MAP.put(Character.valueOf('q'), IsoFields.QUARTER_OF_YEAR);
        FIELD_MAP.put(Character.valueOf('M'), ChronoField.MONTH_OF_YEAR);
        FIELD_MAP.put(Character.valueOf('L'), ChronoField.MONTH_OF_YEAR);
        FIELD_MAP.put(Character.valueOf('D'), ChronoField.DAY_OF_YEAR);
        FIELD_MAP.put(Character.valueOf('d'), ChronoField.DAY_OF_MONTH);
        FIELD_MAP.put(Character.valueOf('F'), ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH);
        FIELD_MAP.put(Character.valueOf('E'), ChronoField.DAY_OF_WEEK);
        FIELD_MAP.put(Character.valueOf('c'), ChronoField.DAY_OF_WEEK);
        FIELD_MAP.put(Character.valueOf('e'), ChronoField.DAY_OF_WEEK);
        FIELD_MAP.put(Character.valueOf('a'), ChronoField.AMPM_OF_DAY);
        FIELD_MAP.put(Character.valueOf('H'), ChronoField.HOUR_OF_DAY);
        FIELD_MAP.put(Character.valueOf('k'), ChronoField.CLOCK_HOUR_OF_DAY);
        FIELD_MAP.put(Character.valueOf('K'), ChronoField.HOUR_OF_AMPM);
        FIELD_MAP.put(Character.valueOf('h'), ChronoField.CLOCK_HOUR_OF_AMPM);
        FIELD_MAP.put(Character.valueOf('m'), ChronoField.MINUTE_OF_HOUR);
        FIELD_MAP.put(Character.valueOf('s'), ChronoField.SECOND_OF_MINUTE);
        FIELD_MAP.put(Character.valueOf('S'), ChronoField.NANO_OF_SECOND);
        FIELD_MAP.put(Character.valueOf('A'), ChronoField.MILLI_OF_DAY);
        FIELD_MAP.put(Character.valueOf('n'), ChronoField.NANO_OF_SECOND);
        FIELD_MAP.put(Character.valueOf('N'), ChronoField.NANO_OF_DAY);
    }

    static /* synthetic */ ZoneId lambda$-java_time_format_DateTimeFormatterBuilder_6842(TemporalAccessor temporal) {
        ZoneId zone = (ZoneId) temporal.query(TemporalQueries.zoneId());
        return (zone == null || (zone instanceof ZoneOffset)) ? null : zone;
    }

    public static String getLocalizedDateTimePattern(FormatStyle dateStyle, FormatStyle timeStyle, Chronology chrono, Locale locale) {
        Objects.requireNonNull((Object) locale, "locale");
        Objects.requireNonNull((Object) chrono, "chrono");
        if (dateStyle != null || timeStyle != null) {
            return Calendar.getDateTimeFormatString(ULocale.forLocale(locale), chrono.getCalendarType(), convertStyle(dateStyle), convertStyle(timeStyle));
        }
        throw new IllegalArgumentException("Either dateStyle or timeStyle must be non-null");
    }

    private static int convertStyle(FormatStyle style) {
        if (style == null) {
            return -1;
        }
        return style.ordinal();
    }

    public DateTimeFormatterBuilder() {
        this.active = this;
        this.printerParsers = new ArrayList();
        this.valueParserIndex = -1;
        this.parent = null;
        this.optional = false;
    }

    private DateTimeFormatterBuilder(DateTimeFormatterBuilder parent, boolean optional) {
        this.active = this;
        this.printerParsers = new ArrayList();
        this.valueParserIndex = -1;
        this.parent = parent;
        this.optional = optional;
    }

    public DateTimeFormatterBuilder parseCaseSensitive() {
        appendInternal(SettingsParser.SENSITIVE);
        return this;
    }

    public DateTimeFormatterBuilder parseCaseInsensitive() {
        appendInternal(SettingsParser.INSENSITIVE);
        return this;
    }

    public DateTimeFormatterBuilder parseStrict() {
        appendInternal(SettingsParser.STRICT);
        return this;
    }

    public DateTimeFormatterBuilder parseLenient() {
        appendInternal(SettingsParser.LENIENT);
        return this;
    }

    public DateTimeFormatterBuilder parseDefaulting(TemporalField field, long value) {
        Objects.requireNonNull((Object) field, "field");
        appendInternal(new DefaultValueParser(field, value));
        return this;
    }

    public DateTimeFormatterBuilder appendValue(TemporalField field) {
        Objects.requireNonNull((Object) field, "field");
        appendValue(new NumberPrinterParser(field, 1, 19, SignStyle.NORMAL));
        return this;
    }

    public DateTimeFormatterBuilder appendValue(TemporalField field, int width) {
        Objects.requireNonNull((Object) field, "field");
        if (width < 1 || width > 19) {
            throw new IllegalArgumentException("The width must be from 1 to 19 inclusive but was " + width);
        }
        appendValue(new NumberPrinterParser(field, width, width, SignStyle.NOT_NEGATIVE));
        return this;
    }

    public DateTimeFormatterBuilder appendValue(TemporalField field, int minWidth, int maxWidth, SignStyle signStyle) {
        if (minWidth == maxWidth && signStyle == SignStyle.NOT_NEGATIVE) {
            return appendValue(field, maxWidth);
        }
        Objects.requireNonNull((Object) field, "field");
        Objects.requireNonNull((Object) signStyle, "signStyle");
        if (minWidth < 1 || minWidth > 19) {
            throw new IllegalArgumentException("The minimum width must be from 1 to 19 inclusive but was " + minWidth);
        } else if (maxWidth < 1 || maxWidth > 19) {
            throw new IllegalArgumentException("The maximum width must be from 1 to 19 inclusive but was " + maxWidth);
        } else if (maxWidth < minWidth) {
            throw new IllegalArgumentException("The maximum width must exceed or equal the minimum width but " + maxWidth + " < " + minWidth);
        } else {
            appendValue(new NumberPrinterParser(field, minWidth, maxWidth, signStyle));
            return this;
        }
    }

    public DateTimeFormatterBuilder appendValueReduced(TemporalField field, int width, int maxWidth, int baseValue) {
        Objects.requireNonNull((Object) field, "field");
        appendValue(new ReducedPrinterParser(field, width, maxWidth, baseValue, null));
        return this;
    }

    public DateTimeFormatterBuilder appendValueReduced(TemporalField field, int width, int maxWidth, ChronoLocalDate baseDate) {
        Objects.requireNonNull((Object) field, "field");
        Objects.requireNonNull((Object) baseDate, "baseDate");
        appendValue(new ReducedPrinterParser(field, width, maxWidth, 0, baseDate));
        return this;
    }

    private DateTimeFormatterBuilder appendValue(NumberPrinterParser pp) {
        if (this.active.valueParserIndex >= 0) {
            int activeValueParser = this.active.valueParserIndex;
            NumberPrinterParser basePP = (NumberPrinterParser) this.active.printerParsers.get(activeValueParser);
            if (pp.minWidth == pp.maxWidth && pp.signStyle == SignStyle.NOT_NEGATIVE) {
                basePP = basePP.withSubsequentWidth(pp.maxWidth);
                appendInternal(pp.withFixedWidth());
                this.active.valueParserIndex = activeValueParser;
            } else {
                basePP = basePP.withFixedWidth();
                this.active.valueParserIndex = appendInternal(pp);
            }
            this.active.printerParsers.set(activeValueParser, basePP);
        } else {
            this.active.valueParserIndex = appendInternal(pp);
        }
        return this;
    }

    public DateTimeFormatterBuilder appendFraction(TemporalField field, int minWidth, int maxWidth, boolean decimalPoint) {
        appendInternal(new FractionPrinterParser(field, minWidth, maxWidth, decimalPoint));
        return this;
    }

    public DateTimeFormatterBuilder appendText(TemporalField field) {
        return appendText(field, TextStyle.FULL);
    }

    public DateTimeFormatterBuilder appendText(TemporalField field, TextStyle textStyle) {
        Objects.requireNonNull((Object) field, "field");
        Objects.requireNonNull((Object) textStyle, "textStyle");
        appendInternal(new TextPrinterParser(field, textStyle, DateTimeTextProvider.getInstance()));
        return this;
    }

    public DateTimeFormatterBuilder appendText(TemporalField field, Map<Long, String> textLookup) {
        Objects.requireNonNull((Object) field, "field");
        Objects.requireNonNull((Object) textLookup, "textLookup");
        final LocaleStore store = new LocaleStore(Collections.singletonMap(TextStyle.FULL, new LinkedHashMap((Map) textLookup)));
        appendInternal(new TextPrinterParser(field, TextStyle.FULL, new DateTimeTextProvider() {
            public String getText(TemporalField field, long value, TextStyle style, Locale locale) {
                return store.getText(value, style);
            }

            public Iterator<Entry<String, Long>> getTextIterator(TemporalField field, TextStyle style, Locale locale) {
                return store.getTextIterator(style);
            }
        }));
        return this;
    }

    public DateTimeFormatterBuilder appendInstant() {
        appendInternal(new InstantPrinterParser(-2));
        return this;
    }

    public DateTimeFormatterBuilder appendInstant(int fractionalDigits) {
        if (fractionalDigits < -1 || fractionalDigits > 9) {
            throw new IllegalArgumentException("The fractional digits must be from -1 to 9 inclusive but was " + fractionalDigits);
        }
        appendInternal(new InstantPrinterParser(fractionalDigits));
        return this;
    }

    public DateTimeFormatterBuilder appendOffsetId() {
        appendInternal(OffsetIdPrinterParser.INSTANCE_ID_Z);
        return this;
    }

    public DateTimeFormatterBuilder appendOffset(String pattern, String noOffsetText) {
        appendInternal(new OffsetIdPrinterParser(pattern, noOffsetText));
        return this;
    }

    public DateTimeFormatterBuilder appendLocalizedOffset(TextStyle style) {
        Objects.requireNonNull((Object) style, "style");
        if (style == TextStyle.FULL || style == TextStyle.SHORT) {
            appendInternal(new LocalizedOffsetIdPrinterParser(style));
            return this;
        }
        throw new IllegalArgumentException("Style must be either full or short");
    }

    public DateTimeFormatterBuilder appendZoneId() {
        appendInternal(new ZoneIdPrinterParser(TemporalQueries.zoneId(), "ZoneId()"));
        return this;
    }

    public DateTimeFormatterBuilder appendZoneRegionId() {
        appendInternal(new ZoneIdPrinterParser(QUERY_REGION_ONLY, "ZoneRegionId()"));
        return this;
    }

    public DateTimeFormatterBuilder appendZoneOrOffsetId() {
        appendInternal(new ZoneIdPrinterParser(TemporalQueries.zone(), "ZoneOrOffsetId()"));
        return this;
    }

    public DateTimeFormatterBuilder appendZoneText(TextStyle textStyle) {
        appendInternal(new ZoneTextPrinterParser(textStyle, null));
        return this;
    }

    public DateTimeFormatterBuilder appendZoneText(TextStyle textStyle, Set<ZoneId> preferredZones) {
        Objects.requireNonNull((Object) preferredZones, "preferredZones");
        appendInternal(new ZoneTextPrinterParser(textStyle, preferredZones));
        return this;
    }

    public DateTimeFormatterBuilder appendChronologyId() {
        appendInternal(new ChronoPrinterParser(null));
        return this;
    }

    public DateTimeFormatterBuilder appendChronologyText(TextStyle textStyle) {
        Objects.requireNonNull((Object) textStyle, "textStyle");
        appendInternal(new ChronoPrinterParser(textStyle));
        return this;
    }

    public DateTimeFormatterBuilder appendLocalized(FormatStyle dateStyle, FormatStyle timeStyle) {
        if (dateStyle == null && timeStyle == null) {
            throw new IllegalArgumentException("Either the date or time style must be non-null");
        }
        appendInternal(new LocalizedPrinterParser(dateStyle, timeStyle));
        return this;
    }

    public DateTimeFormatterBuilder appendLiteral(char literal) {
        appendInternal(new CharLiteralPrinterParser(literal));
        return this;
    }

    public DateTimeFormatterBuilder appendLiteral(String literal) {
        Objects.requireNonNull((Object) literal, "literal");
        if (literal.length() > 0) {
            if (literal.length() == 1) {
                appendInternal(new CharLiteralPrinterParser(literal.charAt(0)));
            } else {
                appendInternal(new StringLiteralPrinterParser(literal));
            }
        }
        return this;
    }

    public DateTimeFormatterBuilder append(DateTimeFormatter formatter) {
        Objects.requireNonNull((Object) formatter, "formatter");
        appendInternal(formatter.toPrinterParser(false));
        return this;
    }

    public DateTimeFormatterBuilder appendOptional(DateTimeFormatter formatter) {
        Objects.requireNonNull((Object) formatter, "formatter");
        appendInternal(formatter.toPrinterParser(true));
        return this;
    }

    public DateTimeFormatterBuilder appendPattern(String pattern) {
        Objects.requireNonNull((Object) pattern, "pattern");
        parsePattern(pattern);
        return this;
    }

    private void parsePattern(String pattern) {
        int pos = 0;
        while (pos < pattern.length()) {
            char cur = pattern.charAt(pos);
            int pos2;
            int start;
            if ((cur >= 'A' && cur <= 'Z') || (cur >= 'a' && cur <= 'z')) {
                pos2 = pos + 1;
                start = pos;
                while (pos2 < pattern.length() && pattern.charAt(pos2) == cur) {
                    pos2++;
                }
                int count = pos2 - pos;
                if (cur == 'p') {
                    int pad = 0;
                    if (pos2 < pattern.length()) {
                        cur = pattern.charAt(pos2);
                        if ((cur < 'A' || cur > 'Z') && (cur < 'a' || cur > 'z')) {
                            pos = pos2;
                        } else {
                            pad = count;
                            pos = pos2 + 1;
                            start = pos2;
                            while (pos < pattern.length() && pattern.charAt(pos) == cur) {
                                pos++;
                            }
                            count = pos - pos2;
                        }
                    } else {
                        pos = pos2;
                    }
                    if (pad == 0) {
                        throw new IllegalArgumentException("Pad letter 'p' must be followed by valid pad pattern: " + pattern);
                    }
                    padNext(pad);
                } else {
                    pos = pos2;
                }
                TemporalField field = (TemporalField) FIELD_MAP.get(Character.valueOf(cur));
                if (field != null) {
                    parseField(cur, count, field);
                } else if (cur == 'z') {
                    if (count > 4) {
                        throw new IllegalArgumentException("Too many pattern letters: " + cur);
                    } else if (count == 4) {
                        appendZoneText(TextStyle.FULL);
                    } else {
                        appendZoneText(TextStyle.SHORT);
                    }
                } else if (cur == 'V') {
                    if (count != 2) {
                        throw new IllegalArgumentException("Pattern letter count must be 2: " + cur);
                    }
                    appendZoneId();
                } else if (cur == 'Z') {
                    if (count < 4) {
                        appendOffset("+HHMM", "+0000");
                    } else if (count == 4) {
                        appendLocalizedOffset(TextStyle.FULL);
                    } else if (count == 5) {
                        appendOffset("+HH:MM:ss", "Z");
                    } else {
                        throw new IllegalArgumentException("Too many pattern letters: " + cur);
                    }
                } else if (cur == 'O') {
                    if (count == 1) {
                        appendLocalizedOffset(TextStyle.SHORT);
                    } else if (count == 4) {
                        appendLocalizedOffset(TextStyle.FULL);
                    } else {
                        throw new IllegalArgumentException("Pattern letter count must be 1 or 4: " + cur);
                    }
                } else if (cur == 'X') {
                    if (count > 5) {
                        throw new IllegalArgumentException("Too many pattern letters: " + cur);
                    }
                    appendOffset(OffsetIdPrinterParser.PATTERNS[(count == 1 ? 0 : 1) + count], "Z");
                } else if (cur == Locale.PRIVATE_USE_EXTENSION) {
                    if (count > 5) {
                        throw new IllegalArgumentException("Too many pattern letters: " + cur);
                    }
                    String zero = count == 1 ? "+00" : count % 2 == 0 ? "+0000" : "+00:00";
                    appendOffset(OffsetIdPrinterParser.PATTERNS[(count == 1 ? 0 : 1) + count], zero);
                } else if (cur == 'W') {
                    if (count > 1) {
                        throw new IllegalArgumentException("Too many pattern letters: " + cur);
                    }
                    appendInternal(new WeekBasedFieldPrinterParser(cur, count));
                } else if (cur == 'w') {
                    if (count > 2) {
                        throw new IllegalArgumentException("Too many pattern letters: " + cur);
                    }
                    appendInternal(new WeekBasedFieldPrinterParser(cur, count));
                } else if (cur == 'Y') {
                    appendInternal(new WeekBasedFieldPrinterParser(cur, count));
                } else {
                    throw new IllegalArgumentException("Unknown pattern letter: " + cur);
                }
                pos--;
            } else if (cur == '\'') {
                pos2 = pos + 1;
                start = pos;
                while (pos2 < pattern.length()) {
                    if (pattern.charAt(pos2) == '\'') {
                        if (pos2 + 1 >= pattern.length() || pattern.charAt(pos2 + 1) != '\'') {
                            break;
                        }
                        pos2++;
                    }
                    pos2++;
                }
                if (pos2 >= pattern.length()) {
                    throw new IllegalArgumentException("Pattern ends with an incomplete string literal: " + pattern);
                }
                String str = pattern.substring(pos + 1, pos2);
                if (str.length() == 0) {
                    appendLiteral('\'');
                    pos = pos2;
                } else {
                    appendLiteral(str.replace((CharSequence) "''", (CharSequence) "'"));
                    pos = pos2;
                }
            } else if (cur == '[') {
                optionalStart();
            } else if (cur == ']') {
                if (this.active.parent == null) {
                    throw new IllegalArgumentException("Pattern invalid as it contains ] without previous [");
                }
                optionalEnd();
            } else if (cur == '{' || cur == '}' || cur == '#') {
                throw new IllegalArgumentException("Pattern includes reserved character: '" + cur + "'");
            } else {
                appendLiteral(cur);
            }
            pos++;
        }
    }

    private void parseField(char cur, int count, TemporalField field) {
        boolean standalone = false;
        switch (cur) {
            case 'D':
                if (count == 1) {
                    appendValue(field);
                    return;
                } else if (count <= 3) {
                    appendValue(field, count);
                    return;
                } else {
                    throw new IllegalArgumentException("Too many pattern letters: " + cur);
                }
            case 'E':
            case 'M':
            case 'Q':
            case 'e':
                break;
            case Types.DATALINK /*70*/:
                if (count == 1) {
                    appendValue(field);
                    return;
                }
                throw new IllegalArgumentException("Too many pattern letters: " + cur);
            case 'G':
                switch (count) {
                    case 1:
                    case 2:
                    case 3:
                        appendText(field, TextStyle.SHORT);
                        return;
                    case 4:
                        appendText(field, TextStyle.FULL);
                        return;
                    case 5:
                        appendText(field, TextStyle.NARROW);
                        return;
                    default:
                        throw new IllegalArgumentException("Too many pattern letters: " + cur);
                }
            case 'H':
            case 'K':
            case 'd':
            case 'h':
            case 'k':
            case 'm':
            case 's':
                if (count == 1) {
                    appendValue(field);
                    return;
                } else if (count == 2) {
                    appendValue(field, count);
                    return;
                } else {
                    throw new IllegalArgumentException("Too many pattern letters: " + cur);
                }
            case 'L':
            case 'q':
                break;
            case 'S':
                appendFraction(ChronoField.NANO_OF_SECOND, count, count, false);
                return;
            case 'a':
                if (count == 1) {
                    appendText(field, TextStyle.SHORT);
                    return;
                }
                throw new IllegalArgumentException("Too many pattern letters: " + cur);
            case 'c':
                if (count == 2) {
                    throw new IllegalArgumentException("Invalid pattern \"cc\"");
                }
                break;
            case 'u':
            case 'y':
                if (count == 2) {
                    appendValueReduced(field, 2, 2, ReducedPrinterParser.BASE_DATE);
                    return;
                } else if (count < 4) {
                    appendValue(field, count, 19, SignStyle.NORMAL);
                    return;
                } else {
                    appendValue(field, count, 19, SignStyle.EXCEEDS_PAD);
                    return;
                }
            default:
                if (count == 1) {
                    appendValue(field);
                    return;
                } else {
                    appendValue(field, count);
                    return;
                }
        }
        standalone = true;
        switch (count) {
            case 1:
            case 2:
                if (cur == 'c' || cur == 'e') {
                    appendInternal(new WeekBasedFieldPrinterParser(cur, count));
                    return;
                } else if (cur == 'E') {
                    appendText(field, TextStyle.SHORT);
                    return;
                } else if (count == 1) {
                    appendValue(field);
                    return;
                } else {
                    appendValue(field, 2);
                    return;
                }
            case 3:
                appendText(field, standalone ? TextStyle.SHORT_STANDALONE : TextStyle.SHORT);
                return;
            case 4:
                appendText(field, standalone ? TextStyle.FULL_STANDALONE : TextStyle.FULL);
                return;
            case 5:
                appendText(field, standalone ? TextStyle.NARROW_STANDALONE : TextStyle.NARROW);
                return;
            default:
                throw new IllegalArgumentException("Too many pattern letters: " + cur);
        }
    }

    public DateTimeFormatterBuilder padNext(int padWidth) {
        return padNext(padWidth, ' ');
    }

    public DateTimeFormatterBuilder padNext(int padWidth, char padChar) {
        if (padWidth < 1) {
            throw new IllegalArgumentException("The pad width must be at least one but was " + padWidth);
        }
        this.active.padNextWidth = padWidth;
        this.active.padNextChar = padChar;
        this.active.valueParserIndex = -1;
        return this;
    }

    public DateTimeFormatterBuilder optionalStart() {
        this.active.valueParserIndex = -1;
        this.active = new DateTimeFormatterBuilder(this.active, true);
        return this;
    }

    public DateTimeFormatterBuilder optionalEnd() {
        if (this.active.parent == null) {
            throw new IllegalStateException("Cannot call optionalEnd() as there was no previous call to optionalStart()");
        }
        if (this.active.printerParsers.size() > 0) {
            CompositePrinterParser cpp = new CompositePrinterParser(this.active.printerParsers, this.active.optional);
            this.active = this.active.parent;
            appendInternal(cpp);
        } else {
            this.active = this.active.parent;
        }
        return this;
    }

    private int appendInternal(DateTimePrinterParser pp) {
        Objects.requireNonNull((Object) pp, "pp");
        if (this.active.padNextWidth > 0) {
            if (pp != null) {
                pp = new PadPrinterParserDecorator(pp, this.active.padNextWidth, this.active.padNextChar);
            }
            this.active.padNextWidth = 0;
            this.active.padNextChar = 0;
        }
        this.active.printerParsers.add(pp);
        this.active.valueParserIndex = -1;
        return this.active.printerParsers.size() - 1;
    }

    public DateTimeFormatter toFormatter() {
        return toFormatter(Locale.getDefault(Category.FORMAT));
    }

    public DateTimeFormatter toFormatter(Locale locale) {
        return toFormatter(locale, ResolverStyle.SMART, null);
    }

    DateTimeFormatter toFormatter(ResolverStyle resolverStyle, Chronology chrono) {
        return toFormatter(Locale.getDefault(Category.FORMAT), resolverStyle, chrono);
    }

    private DateTimeFormatter toFormatter(Locale locale, ResolverStyle resolverStyle, Chronology chrono) {
        Objects.requireNonNull((Object) locale, "locale");
        while (this.active.parent != null) {
            optionalEnd();
        }
        return new DateTimeFormatter(new CompositePrinterParser(this.printerParsers, false), locale, DecimalStyle.STANDARD, resolverStyle, null, chrono, null);
    }
}

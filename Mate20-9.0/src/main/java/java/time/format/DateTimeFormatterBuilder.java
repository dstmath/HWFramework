package java.time.format;

import android.icu.impl.ZoneMeta;
import android.icu.text.LocaleDisplayNames;
import android.icu.text.TimeZoneNames;
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
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeTextProvider;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.ValueRange;
import java.time.temporal.WeekFields;
import java.time.zone.ZoneRulesProvider;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import sun.util.locale.LanguageTag;

public final class DateTimeFormatterBuilder {
    private static final Map<Character, TemporalField> FIELD_MAP = new HashMap();
    static final Comparator<String> LENGTH_SORT = new Comparator<String>() {
        public int compare(String str1, String str2) {
            return str1.length() == str2.length() ? str1.compareTo(str2) : str1.length() - str2.length();
        }
    };
    private static final TemporalQuery<ZoneId> QUERY_REGION_ONLY = $$Lambda$DateTimeFormatterBuilder$MGACNxm6552EiylPRPw4dyNXKo.INSTANCE;
    private DateTimeFormatterBuilder active;
    private final boolean optional;
    private char padNextChar;
    private int padNextWidth;
    private final DateTimeFormatterBuilder parent;
    private final List<DateTimePrinterParser> printerParsers;
    private int valueParserIndex;

    static final class CharLiteralPrinterParser implements DateTimePrinterParser {
        private final char literal;

        CharLiteralPrinterParser(char literal2) {
            this.literal = literal2;
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

        ChronoPrinterParser(TextStyle textStyle2) {
            this.textStyle = textStyle2;
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
            String name;
            int i = position;
            if (i < 0 || i > text.length()) {
                DateTimeParseContext dateTimeParseContext = context;
                throw new IndexOutOfBoundsException();
            }
            Chronology bestMatch = null;
            int matchLen = -1;
            for (Chronology chrono : Chronology.getAvailableChronologies()) {
                if (this.textStyle == null) {
                    name = chrono.getId();
                } else {
                    name = getChronologyName(chrono, context.getLocale());
                }
                String name2 = name;
                int nameLen = name2.length();
                if (nameLen > matchLen && context.subSequenceEquals(text, i, name2, 0, nameLen)) {
                    bestMatch = chrono;
                    matchLen = nameLen;
                }
            }
            if (bestMatch == null) {
                return ~i;
            }
            context.setParsed(bestMatch);
            return i + matchLen;
        }

        private String getChronologyName(Chronology chrono, Locale locale) {
            String name = LocaleDisplayNames.getInstance(ULocale.forLocale(locale)).keyValueDisplayName("calendar", chrono.getCalendarType());
            return name != null ? name : chrono.getId();
        }
    }

    static final class CompositePrinterParser implements DateTimePrinterParser {
        private final boolean optional;
        private final DateTimePrinterParser[] printerParsers;

        CompositePrinterParser(List<DateTimePrinterParser> printerParsers2, boolean optional2) {
            this((DateTimePrinterParser[]) printerParsers2.toArray(new DateTimePrinterParser[printerParsers2.size()]), optional2);
        }

        CompositePrinterParser(DateTimePrinterParser[] printerParsers2, boolean optional2) {
            this.printerParsers = printerParsers2;
            this.optional = optional2;
        }

        public CompositePrinterParser withOptional(boolean optional2) {
            if (optional2 == this.optional) {
                return this;
            }
            return new CompositePrinterParser(this.printerParsers, optional2);
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            int length = buf.length();
            if (this.optional) {
                context.startOptional();
            }
            try {
                for (DateTimePrinterParser pp : this.printerParsers) {
                    if (!pp.format(context, buf)) {
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
            for (DateTimePrinterParser pp2 : this.printerParsers) {
                position = pp2.parse(context, text, position);
                if (position < 0) {
                    break;
                }
            }
            return position;
        }

        public String toString() {
            StringBuilder buf = new StringBuilder();
            if (this.printerParsers != null) {
                buf.append(this.optional ? "[" : "(");
                for (DateTimePrinterParser pp : this.printerParsers) {
                    buf.append((Object) pp);
                }
                buf.append(this.optional ? "]" : ")");
            }
            return buf.toString();
        }
    }

    interface DateTimePrinterParser {
        boolean format(DateTimePrintContext dateTimePrintContext, StringBuilder sb);

        int parse(DateTimeParseContext dateTimeParseContext, CharSequence charSequence, int i);
    }

    static class DefaultValueParser implements DateTimePrinterParser {
        private final TemporalField field;
        private final long value;

        DefaultValueParser(TemporalField field2, long value2) {
            this.field = field2;
            this.value = value2;
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

        FractionPrinterParser(TemporalField field2, int minWidth2, int maxWidth2, boolean decimalPoint2) {
            Objects.requireNonNull(field2, "field");
            if (!field2.range().isFixed()) {
                throw new IllegalArgumentException("Field must have a fixed set of values: " + field2);
            } else if (minWidth2 < 0 || minWidth2 > 9) {
                throw new IllegalArgumentException("Minimum width must be from 0 to 9 inclusive but was " + minWidth2);
            } else if (maxWidth2 < 1 || maxWidth2 > 9) {
                throw new IllegalArgumentException("Maximum width must be from 1 to 9 inclusive but was " + maxWidth2);
            } else if (maxWidth2 >= minWidth2) {
                this.field = field2;
                this.minWidth = minWidth2;
                this.maxWidth = maxWidth2;
                this.decimalPoint = decimalPoint2;
            } else {
                throw new IllegalArgumentException("Maximum width must exceed or equal the minimum width but " + maxWidth2 + " < " + minWidth2);
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
            int pos;
            int position2 = position;
            int effectiveMin = context.isStrict() ? this.minWidth : 0;
            int effectiveMax = context.isStrict() ? this.maxWidth : 9;
            int length = text.length();
            if (position2 == length) {
                return effectiveMin > 0 ? ~position2 : position2;
            }
            if (this.decimalPoint) {
                if (text.charAt(position) != context.getDecimalStyle().getDecimalSeparator()) {
                    return effectiveMin > 0 ? ~position2 : position2;
                }
                position2++;
            }
            int minEndPos = position2 + effectiveMin;
            if (minEndPos > length) {
                return ~position2;
            }
            int maxEndPos = Math.min(position2 + effectiveMax, length);
            int total = 0;
            int total2 = position2;
            while (true) {
                if (total2 >= maxEndPos) {
                    CharSequence charSequence = text;
                    pos = total2;
                    break;
                }
                int pos2 = total2 + 1;
                int digit = context.getDecimalStyle().convertToDigit(text.charAt(total2));
                if (digit >= 0) {
                    total = (total * 10) + digit;
                    total2 = pos2;
                } else if (pos2 < minEndPos) {
                    return ~position2;
                } else {
                    pos = pos2 - 1;
                }
            }
            BigDecimal fraction = new BigDecimal(total).movePointLeft(pos - position2);
            BigDecimal bigDecimal = fraction;
            return context.setParsedField(this.field, convertFromFraction(fraction), position2, pos);
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
            String decimal = this.decimalPoint ? ",DecimalPoint" : "";
            return "Fraction(" + this.field + "," + this.minWidth + "," + this.maxWidth + decimal + ")";
        }
    }

    static final class InstantPrinterParser implements DateTimePrinterParser {
        private static final long SECONDS_0000_TO_1970 = 62167219200L;
        private static final long SECONDS_PER_10000_YEARS = 315569520000L;
        private final int fractionalDigits;

        InstantPrinterParser(int fractionalDigits2) {
            this.fractionalDigits = fractionalDigits2;
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            int div;
            int i;
            int i2;
            StringBuilder sb = buf;
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
            if (inSec >= -62167219200L) {
                long j = inSec;
                long zeroSecs = (inSec - SECONDS_PER_10000_YEARS) + SECONDS_0000_TO_1970;
                long hi = Math.floorDiv(zeroSecs, (long) SECONDS_PER_10000_YEARS) + 1;
                LocalDateTime ldt = LocalDateTime.ofEpochSecond(Math.floorMod(zeroSecs, (long) SECONDS_PER_10000_YEARS) - SECONDS_0000_TO_1970, 0, ZoneOffset.UTC);
                if (hi > 0) {
                    sb.append('+');
                    sb.append(hi);
                }
                sb.append((Object) ldt);
                if (ldt.getSecond() == 0) {
                    sb.append(":00");
                }
                Long l = inSecs;
            } else {
                long zeroSecs2 = inSec + SECONDS_0000_TO_1970;
                long hi2 = zeroSecs2 / SECONDS_PER_10000_YEARS;
                long lo = zeroSecs2 % SECONDS_PER_10000_YEARS;
                LocalDateTime ldt2 = LocalDateTime.ofEpochSecond(lo - SECONDS_0000_TO_1970, 0, ZoneOffset.UTC);
                int pos = buf.length();
                sb.append((Object) ldt2);
                if (ldt2.getSecond() == 0) {
                    sb.append(":00");
                }
                if (hi2 >= 0) {
                    Long l2 = inNanos;
                } else if (ldt2.getYear() == -10000) {
                    Long l3 = inSecs;
                    sb.replace(pos, pos + 2, Long.toString(hi2 - 1));
                } else {
                    if (lo == 0) {
                        sb.insert(pos, hi2);
                    } else {
                        Long l4 = inNanos;
                        sb.insert(pos + 1, Math.abs(hi2));
                    }
                }
                if ((this.fractionalDigits < 0 && inNano > 0) || this.fractionalDigits > 0) {
                    sb.append('.');
                    div = 100000000;
                    i = 0;
                    while (true) {
                        i2 = i;
                        if ((this.fractionalDigits == -1 || inNano <= 0) && ((this.fractionalDigits != -2 || (inNano <= 0 && i2 % 3 == 0)) && i2 >= this.fractionalDigits)) {
                            break;
                        }
                        int digit = inNano / div;
                        sb.append((char) (digit + 48));
                        inNano -= digit * div;
                        div /= 10;
                        i = i2 + 1;
                    }
                }
                sb.append('Z');
                return true;
            }
            sb.append('.');
            div = 100000000;
            i = 0;
            while (true) {
                i2 = i;
                if (this.fractionalDigits == -1) {
                }
                break;
                int digit2 = inNano / div;
                sb.append((char) (digit2 + 48));
                inNano -= digit2 * div;
                div /= 10;
                i = i2 + 1;
            }
            sb.append('Z');
            return true;
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int sec;
            int days;
            int sec2;
            int nano;
            int min;
            Long nanoVal;
            Long secVal;
            long yearParsed;
            int i = position;
            int nano2 = 0;
            int minDigits = this.fractionalDigits < 0 ? 0 : this.fractionalDigits;
            CompositePrinterParser parser = new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_DATE).appendLiteral('T').appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral(':').appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral(':').appendValue(ChronoField.SECOND_OF_MINUTE, 2).appendFraction(ChronoField.NANO_OF_SECOND, minDigits, this.fractionalDigits < 0 ? 9 : this.fractionalDigits, true).appendLiteral('Z').toFormatter().toPrinterParser(false);
            DateTimeParseContext newContext = context.copy();
            int pos = parser.parse(newContext, text, i);
            if (pos < 0) {
                return pos;
            }
            long yearParsed2 = newContext.getParsed(ChronoField.YEAR).longValue();
            int month = newContext.getParsed(ChronoField.MONTH_OF_YEAR).intValue();
            int day = newContext.getParsed(ChronoField.DAY_OF_MONTH).intValue();
            int hour = newContext.getParsed(ChronoField.HOUR_OF_DAY).intValue();
            int min2 = newContext.getParsed(ChronoField.MINUTE_OF_HOUR).intValue();
            Long secVal2 = newContext.getParsed(ChronoField.SECOND_OF_MINUTE);
            Long nanoVal2 = newContext.getParsed(ChronoField.NANO_OF_SECOND);
            int sec3 = secVal2 != null ? secVal2.intValue() : 0;
            if (nanoVal2 != null) {
                nano2 = nanoVal2.intValue();
            }
            int days2 = 0;
            if (hour == 24 && min2 == 0 && sec3 == 0 && nano2 == 0) {
                hour = 0;
                days2 = 1;
            } else if (hour == 23 && min2 == 59 && sec3 == 60) {
                context.setParsedLeapSecond();
                sec = 59;
                days = 0;
                sec2 = hour;
                int i2 = minDigits;
                int i3 = sec2;
                nano = nano2;
                try {
                    min = min2;
                    nanoVal = nanoVal2;
                    try {
                        secVal = secVal2;
                        yearParsed = yearParsed2;
                        long instantSecs = LocalDateTime.of(((int) yearParsed2) % 10000, month, day, sec2, min2, sec, 0).plusDays((long) days).toEpochSecond(ZoneOffset.UTC) + Math.multiplyExact(yearParsed2 / 10000, (long) SECONDS_PER_10000_YEARS);
                        Long l = nanoVal;
                        DateTimeParseContext dateTimeParseContext = context;
                        Long l2 = secVal;
                        long j = yearParsed;
                        long j2 = instantSecs;
                        int i4 = min;
                        int nano3 = i;
                        return dateTimeParseContext.setParsedField(ChronoField.NANO_OF_SECOND, (long) nano, nano3, dateTimeParseContext.setParsedField(ChronoField.INSTANT_SECONDS, instantSecs, nano3, pos));
                    } catch (RuntimeException e) {
                        Long l3 = secVal2;
                        long j3 = yearParsed2;
                        int i5 = nano;
                        int i6 = min;
                        Long l4 = nanoVal;
                        return ~i;
                    }
                } catch (RuntimeException e2) {
                    Long l5 = secVal2;
                    long j4 = yearParsed2;
                    int i7 = min2;
                    Long l6 = nanoVal2;
                    int i8 = nano;
                    return ~i;
                }
            }
            sec2 = hour;
            sec = sec3;
            days = days2;
            int i22 = minDigits;
            try {
                int i32 = sec2;
                nano = nano2;
                min = min2;
                nanoVal = nanoVal2;
                secVal = secVal2;
                yearParsed = yearParsed2;
                try {
                    long instantSecs2 = LocalDateTime.of(((int) yearParsed2) % 10000, month, day, sec2, min2, sec, 0).plusDays((long) days).toEpochSecond(ZoneOffset.UTC) + Math.multiplyExact(yearParsed2 / 10000, (long) SECONDS_PER_10000_YEARS);
                    Long l7 = nanoVal;
                    DateTimeParseContext dateTimeParseContext2 = context;
                    Long l22 = secVal;
                    long j5 = yearParsed;
                    long j22 = instantSecs2;
                    int i42 = min;
                    int nano32 = i;
                    return dateTimeParseContext2.setParsedField(ChronoField.NANO_OF_SECOND, (long) nano, nano32, dateTimeParseContext2.setParsedField(ChronoField.INSTANT_SECONDS, instantSecs2, nano32, pos));
                } catch (RuntimeException e3) {
                    int i9 = nano;
                    int i10 = min;
                    Long l8 = nanoVal;
                    long j6 = yearParsed;
                    Long l9 = secVal;
                    return ~i;
                }
            } catch (RuntimeException e4) {
                int i11 = sec2;
                int hour2 = nano2;
                Long l10 = secVal2;
                long j7 = yearParsed2;
                int i12 = min2;
                Long l11 = nanoVal2;
                return ~i;
            }
        }

        public String toString() {
            return "Instant()";
        }
    }

    static final class LocalizedOffsetIdPrinterParser implements DateTimePrinterParser {
        private final TextStyle style;

        LocalizedOffsetIdPrinterParser(TextStyle style2) {
            this.style = style2;
        }

        private static StringBuilder appendHMS(StringBuilder buf, int t) {
            buf.append((char) ((t / 10) + 48));
            buf.append((char) ((t % 10) + 48));
            return buf;
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long offsetSecs = context.getValue((TemporalField) ChronoField.OFFSET_SECONDS);
            if (offsetSecs == null) {
                return false;
            }
            buf.append("GMT");
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

        /* access modifiers changed from: package-private */
        public int getDigit(CharSequence text, int position) {
            char c = text.charAt(position);
            if (c < '0' || c > '9') {
                return -1;
            }
            return c - '0';
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int negative;
            int pos;
            int pos2;
            int m;
            int h;
            CharSequence charSequence = text;
            int i = position;
            int pos3 = i;
            int end = pos3 + text.length();
            if (!context.subSequenceEquals(charSequence, pos3, "GMT", 0, "GMT".length())) {
                return ~i;
            }
            int pos4 = pos3 + "GMT".length();
            if (pos4 == end) {
                return context.setParsedField(ChronoField.OFFSET_SECONDS, 0, i, pos4);
            }
            char sign = charSequence.charAt(pos4);
            if (sign == '+') {
                negative = 1;
            } else if (sign == '-') {
                negative = -1;
            } else {
                return context.setParsedField(ChronoField.OFFSET_SECONDS, 0, i, pos4);
            }
            int negative2 = negative;
            int pos5 = pos4 + 1;
            int m2 = 0;
            int s = 0;
            if (this.style == TextStyle.FULL) {
                int pos6 = pos5 + 1;
                int h1 = getDigit(charSequence, pos5);
                int pos7 = pos6 + 1;
                int h2 = getDigit(charSequence, pos6);
                if (h1 < 0 || h2 < 0) {
                    int m1 = pos7;
                } else {
                    int pos8 = pos7 + 1;
                    if (charSequence.charAt(pos7) == 58) {
                        h = (h1 * 10) + h2;
                        int h3 = pos8 + 1;
                        int m12 = getDigit(charSequence, pos8);
                        int pos9 = h3 + 1;
                        int m22 = getDigit(charSequence, h3);
                        if (m12 < 0) {
                            int i2 = m22;
                        } else if (m22 < 0) {
                            int i3 = m22;
                        } else {
                            int m3 = (m12 * 10) + m22;
                            if (pos9 + 2 < end) {
                                int i4 = m22;
                                if (charSequence.charAt(pos9) == ':') {
                                    int s1 = getDigit(charSequence, pos9 + 1);
                                    int s2 = getDigit(charSequence, pos9 + 2);
                                    if (s1 >= 0 && s2 >= 0) {
                                        s = (s1 * 10) + s2;
                                        pos9 += 3;
                                    }
                                }
                            }
                            m = m3;
                            pos = pos9;
                            pos2 = s;
                        }
                        return ~i;
                    }
                }
                return ~i;
            }
            int pos10 = pos5 + 1;
            h = getDigit(charSequence, pos5);
            if (h < 0) {
                return ~i;
            }
            if (pos10 < end) {
                int h22 = getDigit(charSequence, pos10);
                if (h22 >= 0) {
                    pos10++;
                    h = (h * 10) + h22;
                }
                if (pos10 + 2 < end && charSequence.charAt(pos10) == ':' && pos10 + 2 < end && charSequence.charAt(pos10) == ':') {
                    int m13 = getDigit(charSequence, pos10 + 1);
                    int m23 = getDigit(charSequence, pos10 + 2);
                    if (m13 >= 0 && m23 >= 0) {
                        m2 = (m13 * 10) + m23;
                        pos10 += 3;
                        if (pos10 + 2 < end && charSequence.charAt(pos10) == ':') {
                            int s12 = getDigit(charSequence, pos10 + 1);
                            int s22 = getDigit(charSequence, pos10 + 2);
                            if (s12 >= 0 && s22 >= 0) {
                                s = (s12 * 10) + s22;
                                pos10 += 3;
                            }
                        }
                    }
                }
            }
            m = m2;
            pos2 = s;
            pos = pos10;
            return context.setParsedField(ChronoField.OFFSET_SECONDS, ((long) negative2) * ((((long) h) * 3600) + (((long) m) * 60) + ((long) pos2)), i, pos);
        }

        public String toString() {
            return "LocalizedOffset(" + this.style + ")";
        }
    }

    static final class LocalizedPrinterParser implements DateTimePrinterParser {
        private static final ConcurrentMap<String, DateTimeFormatter> FORMATTER_CACHE = new ConcurrentHashMap(16, 0.75f, 2);
        private final FormatStyle dateStyle;
        private final FormatStyle timeStyle;

        LocalizedPrinterParser(FormatStyle dateStyle2, FormatStyle timeStyle2) {
            this.dateStyle = dateStyle2;
            this.timeStyle = timeStyle2;
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            return formatter(context.getLocale(), Chronology.from(context.getTemporal())).toPrinterParser(false).format(context, buf);
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            return formatter(context.getLocale(), context.getEffectiveChronology()).toPrinterParser(false).parse(context, text, position);
        }

        private DateTimeFormatter formatter(Locale locale, Chronology chrono) {
            String key = chrono.getId() + '|' + locale.toString() + '|' + this.dateStyle + this.timeStyle;
            DateTimeFormatter formatter = FORMATTER_CACHE.get(key);
            if (formatter != null) {
                return formatter;
            }
            DateTimeFormatter formatter2 = new DateTimeFormatterBuilder().appendPattern(DateTimeFormatterBuilder.getLocalizedDateTimePattern(this.dateStyle, this.timeStyle, chrono, locale)).toFormatter(locale);
            DateTimeFormatter old = FORMATTER_CACHE.putIfAbsent(key, formatter2);
            if (old != null) {
                return old;
            }
            return formatter2;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Localized(");
            sb.append(this.dateStyle != null ? this.dateStyle : "");
            sb.append(",");
            sb.append(this.timeStyle != null ? this.timeStyle : "");
            sb.append(")");
            return sb.toString();
        }
    }

    static class NumberPrinterParser implements DateTimePrinterParser {
        static final long[] EXCEED_POINTS = {0, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000, 10000000000L};
        final TemporalField field;
        final int maxWidth;
        final int minWidth;
        /* access modifiers changed from: private */
        public final SignStyle signStyle;
        final int subsequentWidth;

        NumberPrinterParser(TemporalField field2, int minWidth2, int maxWidth2, SignStyle signStyle2) {
            this.field = field2;
            this.minWidth = minWidth2;
            this.maxWidth = maxWidth2;
            this.signStyle = signStyle2;
            this.subsequentWidth = 0;
        }

        protected NumberPrinterParser(TemporalField field2, int minWidth2, int maxWidth2, SignStyle signStyle2, int subsequentWidth2) {
            this.field = field2;
            this.minWidth = minWidth2;
            this.maxWidth = maxWidth2;
            this.signStyle = signStyle2;
            this.subsequentWidth = subsequentWidth2;
        }

        /* access modifiers changed from: package-private */
        public NumberPrinterParser withFixedWidth() {
            if (this.subsequentWidth == -1) {
                return this;
            }
            NumberPrinterParser numberPrinterParser = new NumberPrinterParser(this.field, this.minWidth, this.maxWidth, this.signStyle, -1);
            return numberPrinterParser;
        }

        /* access modifiers changed from: package-private */
        public NumberPrinterParser withSubsequentWidth(int subsequentWidth2) {
            NumberPrinterParser numberPrinterParser = new NumberPrinterParser(this.field, this.minWidth, this.maxWidth, this.signStyle, this.subsequentWidth + subsequentWidth2);
            return numberPrinterParser;
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long valueLong = context.getValue(this.field);
            if (valueLong == null) {
                return false;
            }
            long value = getValue(context, valueLong.longValue());
            DecimalStyle decimalStyle = context.getDecimalStyle();
            String str = value == Long.MIN_VALUE ? "9223372036854775808" : Long.toString(Math.abs(value));
            if (str.length() <= this.maxWidth) {
                String str2 = decimalStyle.convertNumberToI18N(str);
                if (value >= 0) {
                    switch (this.signStyle) {
                        case EXCEEDS_PAD:
                            if (this.minWidth < 19 && value >= EXCEED_POINTS[this.minWidth]) {
                                buf.append(decimalStyle.getPositiveSign());
                                break;
                            }
                        case ALWAYS:
                            buf.append(decimalStyle.getPositiveSign());
                            break;
                    }
                } else {
                    switch (this.signStyle) {
                        case EXCEEDS_PAD:
                        case ALWAYS:
                        case NORMAL:
                            buf.append(decimalStyle.getNegativeSign());
                            break;
                        case NOT_NEGATIVE:
                            throw new DateTimeException("Field " + this.field + " cannot be printed as the value " + value + " cannot be negative according to the SignStyle");
                    }
                }
                for (int i = 0; i < this.minWidth - str2.length(); i++) {
                    buf.append(decimalStyle.getZeroDigit());
                }
                buf.append(str2);
                return true;
            }
            throw new DateTimeException("Field " + this.field + " cannot be printed as the value " + value + " exceeds the maximum print width of " + this.maxWidth);
        }

        /* access modifiers changed from: package-private */
        public long getValue(DateTimePrintContext context, long value) {
            return value;
        }

        /* access modifiers changed from: package-private */
        public boolean isFixedWidth(DateTimeParseContext context) {
            return this.subsequentWidth == -1 || (this.subsequentWidth > 0 && this.minWidth == this.maxWidth && this.signStyle == SignStyle.NOT_NEGATIVE);
        }

        /* JADX WARNING: Removed duplicated region for block: B:106:0x0185  */
        /* JADX WARNING: Removed duplicated region for block: B:111:0x01a3  */
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int pos;
            long total;
            BigInteger totalBig;
            long total2;
            int length;
            char sign;
            char sign2;
            int i = position;
            int length2 = text.length();
            if (i == length2) {
                return ~i;
            }
            char sign3 = text.charAt(position);
            boolean negative = false;
            boolean positive = false;
            int pass = 0;
            int i2 = 1;
            if (sign3 == context.getDecimalStyle().getPositiveSign()) {
                if (!this.signStyle.parse(true, context.isStrict(), this.minWidth == this.maxWidth)) {
                    return ~i;
                }
                positive = true;
                i++;
            } else if (sign3 == context.getDecimalStyle().getNegativeSign()) {
                if (!this.signStyle.parse(false, context.isStrict(), this.minWidth == this.maxWidth)) {
                    return ~i;
                }
                negative = true;
                i++;
            } else if (this.signStyle == SignStyle.ALWAYS && context.isStrict()) {
                return ~i;
            }
            int position2 = i;
            boolean negative2 = negative;
            boolean positive2 = positive;
            if (context.isStrict() || isFixedWidth(context)) {
                i2 = this.minWidth;
            }
            int effMinWidth = i2;
            int minEndPos = position2 + effMinWidth;
            if (minEndPos > length2) {
                return ~position2;
            }
            long total3 = 0;
            BigInteger totalBig2 = null;
            int pos2 = position2;
            int effMaxWidth = ((context.isStrict() || isFixedWidth(context)) ? this.maxWidth : 9) + Math.max(this.subsequentWidth, 0);
            while (true) {
                int effMaxWidth2 = pass;
                if (effMaxWidth2 >= 2) {
                    char c = sign3;
                    pos = pos2;
                    break;
                }
                int maxEndPos = Math.min(pos2 + effMaxWidth, length2);
                while (true) {
                    if (pos2 >= maxEndPos) {
                        total2 = total3;
                        int i3 = maxEndPos;
                        length = length2;
                        sign = sign3;
                        pos = pos2;
                        break;
                    }
                    int pos3 = pos2 + 1;
                    length = length2;
                    char ch = text.charAt(pos2);
                    int maxEndPos2 = maxEndPos;
                    int maxEndPos3 = context.getDecimalStyle().convertToDigit(ch);
                    if (maxEndPos3 < 0) {
                        pos = pos3 - 1;
                        if (pos < minEndPos) {
                            char c2 = ch;
                            return ~position2;
                        }
                        total2 = total3;
                        sign = sign3;
                    } else {
                        char c3 = ch;
                        if (pos3 - position2 > 18) {
                            if (totalBig2 == null) {
                                totalBig2 = BigInteger.valueOf(total3);
                            }
                            sign2 = sign3;
                            totalBig2 = totalBig2.multiply(BigInteger.TEN).add(BigInteger.valueOf((long) maxEndPos3));
                        } else {
                            sign2 = sign3;
                            long j = total3;
                            total3 = ((long) maxEndPos3) + (10 * total3);
                        }
                        pos2 = pos3;
                        length2 = length;
                        maxEndPos = maxEndPos2;
                        sign3 = sign2;
                    }
                }
                if (this.subsequentWidth <= 0 || effMaxWidth2 != 0) {
                    total3 = total2;
                } else {
                    effMaxWidth = Math.max(effMinWidth, (pos - position2) - this.subsequentWidth);
                    pos2 = position2;
                    totalBig2 = null;
                    pass = effMaxWidth2 + 1;
                    total3 = 0;
                    length2 = length;
                    sign3 = sign;
                }
            }
            if (negative2) {
                if (totalBig2 != null) {
                    if (totalBig2.equals(BigInteger.ZERO) && context.isStrict()) {
                        return ~(position2 - 1);
                    }
                    totalBig2 = totalBig2.negate();
                } else if (total3 == 0 && context.isStrict()) {
                    return ~(position2 - 1);
                } else {
                    total = -total3;
                    totalBig = totalBig2;
                    if (totalBig != null) {
                        return setValue(context, total, position2, pos);
                    }
                    if (totalBig.bitLength() > 63) {
                        totalBig = totalBig.divide(BigInteger.TEN);
                        pos--;
                    }
                    return setValue(context, totalBig.longValue(), position2, pos);
                }
            } else if (this.signStyle == SignStyle.EXCEEDS_PAD && context.isStrict()) {
                int parseLen = pos - position2;
                if (positive2) {
                    if (parseLen <= this.minWidth) {
                        return ~(position2 - 1);
                    }
                } else if (parseLen > this.minWidth) {
                    return ~position2;
                }
            }
            total = total3;
            totalBig = totalBig2;
            if (totalBig != null) {
            }
        }

        /* access modifiers changed from: package-private */
        public int setValue(DateTimeParseContext context, long value, int errorPos, int successPos) {
            return context.setParsedField(this.field, value, errorPos, successPos);
        }

        public String toString() {
            if (this.minWidth == 1 && this.maxWidth == 19 && this.signStyle == SignStyle.NORMAL) {
                return "Value(" + this.field + ")";
            } else if (this.minWidth == this.maxWidth && this.signStyle == SignStyle.NOT_NEGATIVE) {
                return "Value(" + this.field + "," + this.minWidth + ")";
            } else {
                return "Value(" + this.field + "," + this.minWidth + "," + this.maxWidth + "," + this.signStyle + ")";
            }
        }
    }

    static final class OffsetIdPrinterParser implements DateTimePrinterParser {
        static final OffsetIdPrinterParser INSTANCE_ID_Z = new OffsetIdPrinterParser("+HH:MM:ss", "Z");
        static final OffsetIdPrinterParser INSTANCE_ID_ZERO = new OffsetIdPrinterParser("+HH:MM:ss", "0");
        static final String[] PATTERNS = {"+HH", "+HHmm", "+HH:mm", "+HHMM", "+HH:MM", "+HHMMss", "+HH:MM:ss", "+HHMMSS", "+HH:MM:SS"};
        private final String noOffsetText;
        private final int type;

        OffsetIdPrinterParser(String pattern, String noOffsetText2) {
            Objects.requireNonNull(pattern, "pattern");
            Objects.requireNonNull(noOffsetText2, "noOffsetText");
            this.type = checkPattern(pattern);
            this.noOffsetText = noOffsetText2;
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
            Long offsetSecs = context.getValue((TemporalField) ChronoField.OFFSET_SECONDS);
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
                buf.append(totalSecs < 0 ? LanguageTag.SEP : "+");
                buf.append((char) ((absHours / 10) + 48));
                buf.append((char) ((absHours % 10) + 48));
                if (this.type >= 3 || (this.type >= 1 && absMinutes > 0)) {
                    buf.append(this.type % 2 == 0 ? ":" : "");
                    buf.append((char) ((absMinutes / 10) + 48));
                    buf.append((char) ((absMinutes % 10) + 48));
                    output += absMinutes;
                    if (this.type >= 7 || (this.type >= 5 && absSeconds > 0)) {
                        buf.append(this.type % 2 == 0 ? ":" : "");
                        buf.append((char) ((absSeconds / 10) + 48));
                        buf.append((char) ((absSeconds % 10) + 48));
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

        /* JADX WARNING: Removed duplicated region for block: B:32:0x007e  */
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            boolean z;
            CharSequence charSequence = text;
            int i = position;
            int length = text.length();
            int noOffsetLen = this.noOffsetText.length();
            if (noOffsetLen == 0) {
                if (i == length) {
                    return context.setParsedField(ChronoField.OFFSET_SECONDS, 0, i, i);
                }
            } else if (i == length) {
                return ~i;
            } else {
                if (context.subSequenceEquals(charSequence, i, this.noOffsetText, 0, noOffsetLen)) {
                    return context.setParsedField(ChronoField.OFFSET_SECONDS, 0, i, i + noOffsetLen);
                }
            }
            char sign = text.charAt(position);
            if (sign == '+' || sign == '-') {
                int negative = sign == '-' ? -1 : 1;
                int[] array = new int[4];
                array[0] = i + 1;
                if (!parseNumber(array, 1, charSequence, true)) {
                    if (!parseNumber(array, 2, charSequence, this.type >= 3) && !parseNumber(array, 3, charSequence, false)) {
                        z = false;
                        if (!z) {
                            return context.setParsedField(ChronoField.OFFSET_SECONDS, ((long) negative) * ((((long) array[1]) * 3600) + (((long) array[2]) * 60) + ((long) array[3])), i, array[0]);
                        }
                    }
                }
                z = true;
                if (!z) {
                }
            }
            if (noOffsetLen != 0) {
                return ~i;
            }
            return context.setParsedField(ChronoField.OFFSET_SECONDS, 0, i, i + noOffsetLen);
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
            int pos3 = pos2 + 1;
            char ch2 = parseText.charAt(pos2);
            if (ch1 < '0' || ch1 > '9' || ch2 < '0' || ch2 > '9') {
                return required;
            }
            int value = ((ch1 - '0') * 10) + (ch2 - '0');
            if (value < 0 || value > 59) {
                return required;
            }
            array[arrayIndex] = value;
            array[0] = pos3;
            return false;
        }

        public String toString() {
            String converted = this.noOffsetText.replace((CharSequence) "'", (CharSequence) "''");
            return "Offset(" + PATTERNS[this.type] + ",'" + converted + "')";
        }
    }

    static final class PadPrinterParserDecorator implements DateTimePrinterParser {
        private final char padChar;
        private final int padWidth;
        private final DateTimePrinterParser printerParser;

        PadPrinterParserDecorator(DateTimePrinterParser printerParser2, int padWidth2, char padChar2) {
            this.printerParser = printerParser2;
            this.padWidth = padWidth2;
            this.padChar = padChar2;
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            int preLen = buf.length();
            if (!this.printerParser.format(context, buf)) {
                return false;
            }
            int len = buf.length() - preLen;
            if (len <= this.padWidth) {
                for (int i = 0; i < this.padWidth - len; i++) {
                    buf.insert(preLen, this.padChar);
                }
                return true;
            }
            throw new DateTimeException("Cannot print as output of " + len + " characters exceeds pad width of " + this.padWidth);
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            boolean strict = context.isStrict();
            if (position > text.length()) {
                throw new IndexOutOfBoundsException();
            } else if (position == text.length()) {
                return ~position;
            } else {
                int endPos = this.padWidth + position;
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
            String str;
            StringBuilder sb = new StringBuilder();
            sb.append("Pad(");
            sb.append((Object) this.printerParser);
            sb.append(",");
            sb.append(this.padWidth);
            if (this.padChar == ' ') {
                str = ")";
            } else {
                str = ",'" + this.padChar + "')";
            }
            sb.append(str);
            return sb.toString();
        }
    }

    static class PrefixTree {
        protected char c0;
        protected PrefixTree child;
        protected String key;
        protected PrefixTree sibling;
        protected String value;

        private static class CI extends PrefixTree {
            private CI(String k, String v, PrefixTree child) {
                super(k, v, child);
            }

            /* access modifiers changed from: protected */
            public CI newNode(String k, String v, PrefixTree child) {
                return new CI(k, v, child);
            }

            /* access modifiers changed from: protected */
            public boolean isEqual(char c1, char c2) {
                return DateTimeParseContext.charEqualsIgnoreCase(c1, c2);
            }

            /* access modifiers changed from: protected */
            public boolean prefixOf(CharSequence text, int off, int end) {
                int off2 = this.key.length();
                if (off2 > end - off) {
                    return false;
                }
                int off3 = off;
                int off4 = 0;
                while (true) {
                    int len = off2 - 1;
                    if (off2 <= 0) {
                        return true;
                    }
                    int off0 = off4 + 1;
                    int off5 = off3 + 1;
                    if (!isEqual(this.key.charAt(off4), text.charAt(off3))) {
                        return false;
                    }
                    off3 = off5;
                    off2 = len;
                    off4 = off0;
                }
            }
        }

        private static class LENIENT extends CI {
            private LENIENT(String k, String v, PrefixTree child) {
                super(k, v, child);
            }

            /* access modifiers changed from: protected */
            public CI newNode(String k, String v, PrefixTree child) {
                return new LENIENT(k, v, child);
            }

            private boolean isLenientChar(char c) {
                return c == ' ' || c == '_' || c == '/';
            }

            /* access modifiers changed from: protected */
            public String toKey(String k) {
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
                while (koff < len && off < end) {
                    if (isLenientChar(text.charAt(off))) {
                        off++;
                    } else {
                        int koff2 = koff + 1;
                        int off2 = off + 1;
                        if (isEqual(this.key.charAt(koff), text.charAt(off)) == 0) {
                            return null;
                        }
                        off = off2;
                        koff = koff2;
                    }
                }
                if (koff != len) {
                    return null;
                }
                if (this.child != null && off != end) {
                    int off0 = off;
                    while (off0 < end && isLenientChar(text.charAt(off0))) {
                        off0++;
                    }
                    if (off0 < end) {
                        PrefixTree c = this.child;
                        while (true) {
                            if (!isEqual(c.c0, text.charAt(off0))) {
                                c = c.sibling;
                                if (c == null) {
                                    break;
                                }
                            } else {
                                pos.setIndex(off0);
                                String found = c.match(text, pos);
                                if (found != null) {
                                    return found;
                                }
                            }
                        }
                    }
                }
                pos.setIndex(off);
                return this.value;
            }
        }

        private PrefixTree(String k, String v, PrefixTree child2) {
            this.key = k;
            this.value = v;
            this.child = child2;
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
            return new CI("", null, null);
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
            String k2 = toKey(k);
            int prefixLen = prefixLength(k2);
            if (prefixLen != this.key.length()) {
                PrefixTree n1 = newNode(this.key.substring(prefixLen), this.value, this.child);
                this.key = k2.substring(0, prefixLen);
                this.child = n1;
                if (prefixLen < k2.length()) {
                    this.child.sibling = newNode(k2.substring(prefixLen), v, null);
                    this.value = null;
                } else {
                    this.value = v;
                }
                return true;
            } else if (prefixLen < k2.length()) {
                String subKey = k2.substring(prefixLen);
                for (PrefixTree c = this.child; c != null; c = c.sibling) {
                    if (isEqual(c.c0, subKey.charAt(0))) {
                        return c.add0(subKey, v);
                    }
                }
                PrefixTree c2 = newNode(subKey, v, null);
                c2.sibling = this.child;
                this.child = c2;
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
                int length = this.key.length() + off;
                int off2 = length;
                if (length != end) {
                    PrefixTree c = this.child;
                    while (!isEqual(c.c0, text.charAt(off2))) {
                        c = c.sibling;
                        if (c == null) {
                        }
                    }
                    String found = c.match(text, off2, end);
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
            int off2 = off + this.key.length();
            if (this.child != null && off2 != end) {
                PrefixTree c = this.child;
                while (true) {
                    if (!isEqual(c.c0, text.charAt(off2))) {
                        c = c.sibling;
                        if (c == null) {
                            break;
                        }
                    } else {
                        pos.setIndex(off2);
                        String found = c.match(text, pos);
                        if (found != null) {
                            return found;
                        }
                    }
                }
            }
            pos.setIndex(off2);
            return this.value;
        }

        /* access modifiers changed from: protected */
        public String toKey(String k) {
            return k;
        }

        /* access modifiers changed from: protected */
        public PrefixTree newNode(String k, String v, PrefixTree child2) {
            return new PrefixTree(k, v, child2);
        }

        /* access modifiers changed from: protected */
        public boolean isEqual(char c1, char c2) {
            return c1 == c2;
        }

        /* access modifiers changed from: protected */
        public boolean prefixOf(CharSequence text, int off, int end) {
            if (text instanceof String) {
                return ((String) text).startsWith(this.key, off);
            }
            int off2 = this.key.length();
            if (off2 > end - off) {
                return false;
            }
            int off3 = off;
            int off4 = 0;
            while (true) {
                int len = off2 - 1;
                if (off2 <= 0) {
                    return true;
                }
                int off0 = off4 + 1;
                int off5 = off3 + 1;
                if (!isEqual(this.key.charAt(off4), text.charAt(off3))) {
                    return false;
                }
                off3 = off5;
                off2 = len;
                off4 = off0;
            }
        }

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

        ReducedPrinterParser(TemporalField field, int minWidth, int maxWidth, int baseValue2, ChronoLocalDate baseDate2) {
            this(field, minWidth, maxWidth, baseValue2, baseDate2, 0);
            if (minWidth < 1 || minWidth > 10) {
                throw new IllegalArgumentException("The minWidth must be from 1 to 10 inclusive but was " + minWidth);
            } else if (maxWidth < 1 || maxWidth > 10) {
                throw new IllegalArgumentException("The maxWidth must be from 1 to 10 inclusive but was " + minWidth);
            } else if (maxWidth < minWidth) {
                throw new IllegalArgumentException("Maximum width must exceed or equal the minimum width but " + maxWidth + " < " + minWidth);
            } else if (baseDate2 != null) {
            } else {
                if (!field.range().isValidValue((long) baseValue2)) {
                    throw new IllegalArgumentException("The base value must be within the range of the field");
                } else if (((long) baseValue2) + EXCEED_POINTS[maxWidth] > 2147483647L) {
                    throw new DateTimeException("Unable to add printer-parser as the range exceeds the capacity of an int");
                }
            }
        }

        private ReducedPrinterParser(TemporalField field, int minWidth, int maxWidth, int baseValue2, ChronoLocalDate baseDate2, int subsequentWidth) {
            super(field, minWidth, maxWidth, SignStyle.NOT_NEGATIVE, subsequentWidth);
            this.baseValue = baseValue2;
            this.baseDate = baseDate2;
        }

        /* access modifiers changed from: package-private */
        public long getValue(DateTimePrintContext context, long value) {
            long absValue = Math.abs(value);
            int baseValue2 = this.baseValue;
            if (this.baseDate != null) {
                baseValue2 = Chronology.from(context.getTemporal()).date(this.baseDate).get(this.field);
            }
            if (value < ((long) baseValue2) || value >= ((long) baseValue2) + EXCEED_POINTS[this.minWidth]) {
                return absValue % EXCEED_POINTS[this.maxWidth];
            }
            return absValue % EXCEED_POINTS[this.minWidth];
        }

        /* access modifiers changed from: package-private */
        public int setValue(DateTimeParseContext context, long value, int errorPos, int successPos) {
            int baseValue2 = this.baseValue;
            if (this.baseDate != null) {
                baseValue2 = context.getEffectiveChronology().date(this.baseDate).get(this.field);
                $$Lambda$DateTimeFormatterBuilder$ReducedPrinterParser$O7fxxUm4cHduGbldToNj0T92oIo r2 = new Consumer(context, value, errorPos, successPos) {
                    private final /* synthetic */ DateTimeParseContext f$1;
                    private final /* synthetic */ long f$2;
                    private final /* synthetic */ int f$3;
                    private final /* synthetic */ int f$4;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r5;
                        this.f$4 = r6;
                    }

                    public final void accept(Object obj) {
                        DateTimeFormatterBuilder.ReducedPrinterParser.this.setValue(this.f$1, this.f$2, this.f$3, this.f$4);
                    }
                };
                context.addChronoChangedListener(r2);
            }
            if (successPos - errorPos == this.minWidth && value >= 0) {
                long range = EXCEED_POINTS[this.minWidth];
                long basePart = ((long) baseValue2) - (((long) baseValue2) % range);
                if (baseValue2 > 0) {
                    value += basePart;
                } else {
                    value = basePart - value;
                }
                if (value < ((long) baseValue2)) {
                    value += range;
                }
            }
            return context.setParsedField(this.field, value, errorPos, successPos);
        }

        /* access modifiers changed from: package-private */
        public ReducedPrinterParser withFixedWidth() {
            if (this.subsequentWidth == -1) {
                return this;
            }
            ReducedPrinterParser reducedPrinterParser = new ReducedPrinterParser(this.field, this.minWidth, this.maxWidth, this.baseValue, this.baseDate, -1);
            return reducedPrinterParser;
        }

        /* access modifiers changed from: package-private */
        public ReducedPrinterParser withSubsequentWidth(int subsequentWidth) {
            ReducedPrinterParser reducedPrinterParser = new ReducedPrinterParser(this.field, this.minWidth, this.maxWidth, this.baseValue, this.baseDate, this.subsequentWidth + subsequentWidth);
            return reducedPrinterParser;
        }

        /* access modifiers changed from: package-private */
        public boolean isFixedWidth(DateTimeParseContext context) {
            if (!context.isStrict()) {
                return false;
            }
            return super.isFixedWidth(context);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ReducedValue(");
            sb.append((Object) this.field);
            sb.append(",");
            sb.append(this.minWidth);
            sb.append(",");
            sb.append(this.maxWidth);
            sb.append(",");
            sb.append(this.baseDate != null ? this.baseDate : Integer.valueOf(this.baseValue));
            sb.append(")");
            return sb.toString();
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

        StringLiteralPrinterParser(String literal2) {
            this.literal = literal2;
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            buf.append(this.literal);
            return true;
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            if (position > text.length() || position < 0) {
                throw new IndexOutOfBoundsException();
            }
            if (!context.subSequenceEquals(text, position, this.literal, 0, this.literal.length())) {
                return ~position;
            }
            return this.literal.length() + position;
        }

        public String toString() {
            String converted = this.literal.replace((CharSequence) "'", (CharSequence) "''");
            return "'" + converted + "'";
        }
    }

    static final class TextPrinterParser implements DateTimePrinterParser {
        private final TemporalField field;
        private volatile NumberPrinterParser numberPrinterParser;
        private final DateTimeTextProvider provider;
        private final TextStyle textStyle;

        TextPrinterParser(TemporalField field2, TextStyle textStyle2, DateTimeTextProvider provider2) {
            this.field = field2;
            this.textStyle = textStyle2;
            this.provider = provider2;
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            String text;
            Long value = context.getValue(this.field);
            if (value == null) {
                return false;
            }
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
            Iterator<Map.Entry<String, Long>> it;
            int i = position;
            int length = parseText.length();
            if (i < 0 || i > length) {
                DateTimeParseContext dateTimeParseContext = context;
                CharSequence charSequence = parseText;
                throw new IndexOutOfBoundsException();
            }
            TextStyle style = context.isStrict() ? this.textStyle : null;
            Chronology chrono = context.getEffectiveChronology();
            if (chrono == null || chrono == IsoChronology.INSTANCE) {
                it = this.provider.getTextIterator(this.field, style, context.getLocale());
            } else {
                it = this.provider.getTextIterator(chrono, this.field, style, context.getLocale());
            }
            Iterator<Map.Entry<String, Long>> it2 = it;
            if (it2 != null) {
                while (it2.hasNext()) {
                    Map.Entry<String, Long> entry = it2.next();
                    String itText = entry.getKey();
                    if (context.subSequenceEquals(itText, 0, parseText, i, itText.length())) {
                        return context.setParsedField(this.field, entry.getValue().longValue(), i, i + itText.length());
                    }
                }
                if (context.isStrict()) {
                    return ~i;
                }
            }
            return numberPrinterParser().parse(context, parseText, i);
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

        WeekBasedFieldPrinterParser(char chr2, int count2) {
            this.chr = chr2;
            this.count = count2;
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            return printerParser(context.getLocale()).format(context, buf);
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            return printerParser(context.getLocale()).parse(context, text, position);
        }

        private DateTimePrinterParser printerParser(Locale locale) {
            TemporalField field;
            WeekFields weekDef = WeekFields.of(locale);
            char c = this.chr;
            if (c == 'W') {
                field = weekDef.weekOfMonth();
            } else if (c == 'Y') {
                TemporalField field2 = weekDef.weekBasedYear();
                if (this.count == 2) {
                    ReducedPrinterParser reducedPrinterParser = new ReducedPrinterParser(field2, 2, 2, 0, ReducedPrinterParser.BASE_DATE, 0);
                    return reducedPrinterParser;
                }
                NumberPrinterParser numberPrinterParser = new NumberPrinterParser(field2, this.count, 19, this.count < 4 ? SignStyle.NORMAL : SignStyle.EXCEEDS_PAD, -1);
                return numberPrinterParser;
            } else if (c == 'c' || c == 'e') {
                field = weekDef.dayOfWeek();
            } else if (c == 'w') {
                field = weekDef.weekOfWeekBasedYear();
            } else {
                throw new IllegalStateException("unreachable");
            }
            return new NumberPrinterParser(field, this.count == 2 ? 2 : 1, 2, SignStyle.NOT_NEGATIVE);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(30);
            sb.append("Localized(");
            if (this.chr != 'Y') {
                char c = this.chr;
                if (c == 'W') {
                    sb.append("WeekOfMonth");
                } else if (c == 'c' || c == 'e') {
                    sb.append("DayOfWeek");
                } else if (c == 'w') {
                    sb.append("WeekOfWeekBasedYear");
                }
                sb.append(",");
                sb.append(this.count);
            } else if (this.count == 1) {
                sb.append("WeekBasedYear");
            } else if (this.count == 2) {
                sb.append("ReducedValue(WeekBasedYear,2,2,2000-01-01)");
            } else {
                sb.append("WeekBasedYear,");
                sb.append(this.count);
                sb.append(",");
                sb.append(19);
                sb.append(",");
                sb.append((Object) this.count < 4 ? SignStyle.NORMAL : SignStyle.EXCEEDS_PAD);
            }
            sb.append(")");
            return sb.toString();
        }
    }

    static class ZoneIdPrinterParser implements DateTimePrinterParser {
        private static volatile Map.Entry<Integer, PrefixTree> cachedPrefixTree;
        private static volatile Map.Entry<Integer, PrefixTree> cachedPrefixTreeCI;
        private final String description;
        private final TemporalQuery<ZoneId> query;

        ZoneIdPrinterParser(TemporalQuery<ZoneId> query2, String description2) {
            this.query = query2;
            this.description = description2;
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            ZoneId zone = (ZoneId) context.getValue(this.query);
            if (zone == null) {
                return false;
            }
            buf.append(zone.getId());
            return true;
        }

        /* access modifiers changed from: protected */
        public PrefixTree getTree(DateTimeParseContext context) {
            Set<String> regionIds = ZoneRulesProvider.getAvailableZoneIds();
            int regionIdsSize = regionIds.size();
            Map.Entry<Integer, PrefixTree> cached = context.isCaseSensitive() ? cachedPrefixTree : cachedPrefixTreeCI;
            if (cached == null || cached.getKey().intValue() != regionIdsSize) {
                synchronized (this) {
                    Map.Entry<Integer, PrefixTree> cached2 = context.isCaseSensitive() ? cachedPrefixTree : cachedPrefixTreeCI;
                    if (cached2 == null || cached2.getKey().intValue() != regionIdsSize) {
                        cached2 = new AbstractMap.SimpleImmutableEntry<>(Integer.valueOf(regionIdsSize), PrefixTree.newTree(regionIds, context));
                        if (context.isCaseSensitive()) {
                            cachedPrefixTree = cached2;
                        } else {
                            cachedPrefixTreeCI = cached2;
                        }
                    }
                }
            }
            return cached.getValue();
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
                    if (!context.charEquals(nextChar, 'U') || !context.charEquals(nextNextChar, 'T')) {
                        if (context.charEquals(nextChar, 'G') && length >= position + 3 && context.charEquals(nextNextChar, 'M') && context.charEquals(text.charAt(position + 2), 'T')) {
                            return parseOffsetBased(context, text, position, position + 3, OffsetIdPrinterParser.INSTANCE_ID_ZERO);
                        }
                    } else if (length < position + 3 || !context.charEquals(text.charAt(position + 2), 'C')) {
                        return parseOffsetBased(context, text, position, position + 2, OffsetIdPrinterParser.INSTANCE_ID_ZERO);
                    } else {
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
                    context.setParsed((ZoneId) ZoneOffset.UTC);
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
                } else {
                    context.setParsed(ZoneId.ofOffset(prefix, ZoneOffset.ofTotalSeconds((int) newContext.getParsed(ChronoField.OFFSET_SECONDS).longValue())));
                    return endPos;
                }
            }
        }

        public String toString() {
            return this.description;
        }
    }

    static final class ZoneTextPrinterParser extends ZoneIdPrinterParser {
        private static final int DST = 1;
        private static final TimeZoneNames.NameType[] FULL_TYPES = {TimeZoneNames.NameType.LONG_STANDARD, TimeZoneNames.NameType.LONG_DAYLIGHT, TimeZoneNames.NameType.LONG_GENERIC};
        private static final int GENERIC = 2;
        private static final TimeZoneNames.NameType[] SHORT_TYPES = {TimeZoneNames.NameType.SHORT_STANDARD, TimeZoneNames.NameType.SHORT_DAYLIGHT, TimeZoneNames.NameType.SHORT_GENERIC};
        private static final int STD = 0;
        private static final TimeZoneNames.NameType[] TYPES = {TimeZoneNames.NameType.LONG_STANDARD, TimeZoneNames.NameType.SHORT_STANDARD, TimeZoneNames.NameType.LONG_DAYLIGHT, TimeZoneNames.NameType.SHORT_DAYLIGHT, TimeZoneNames.NameType.LONG_GENERIC, TimeZoneNames.NameType.SHORT_GENERIC};
        private static final Map<String, SoftReference<Map<Locale, String[]>>> cache = new ConcurrentHashMap();
        private final Map<Locale, Map.Entry<Integer, SoftReference<PrefixTree>>> cachedTree = new HashMap();
        private final Map<Locale, Map.Entry<Integer, SoftReference<PrefixTree>>> cachedTreeCI = new HashMap();
        private Set<String> preferredZones;
        private final TextStyle textStyle;

        /* JADX WARNING: Illegal instructions before constructor call */
        ZoneTextPrinterParser(TextStyle textStyle2, Set<ZoneId> preferredZones2) {
            super(r0, "ZoneText(" + textStyle2 + ")");
            TemporalQuery<ZoneId> zone = TemporalQueries.zone();
            this.textStyle = (TextStyle) Objects.requireNonNull(textStyle2, "textStyle");
            if (preferredZones2 != null && preferredZones2.size() != 0) {
                this.preferredZones = new HashSet();
                for (ZoneId id : preferredZones2) {
                    this.preferredZones.add(id.getId());
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:46:0x00d5  */
        /* JADX WARNING: Removed duplicated region for block: B:48:0x00e0  */
        /* JADX WARNING: Removed duplicated region for block: B:50:0x00ea  */
        private String getDisplayName(String id, int type, Locale locale) {
            String[] names;
            String str = id;
            Locale locale2 = locale;
            if (this.textStyle != TextStyle.NARROW) {
                SoftReference<Map<Locale, String[]>> ref = cache.get(str);
                Map<Locale, String[]> perLocale = null;
                if (ref != null) {
                    Map<Locale, String[]> map = ref.get();
                    perLocale = map;
                    if (map != null) {
                        String[] strArr = perLocale.get(locale2);
                        String[] names2 = strArr;
                        if (strArr != null) {
                            names = names2;
                            switch (type) {
                                case 0:
                                    return names[this.textStyle.zoneNameStyleIndex() + 1];
                                case 1:
                                    return names[this.textStyle.zoneNameStyleIndex() + 3];
                                default:
                                    return names[this.textStyle.zoneNameStyleIndex() + 5];
                            }
                        }
                    }
                }
                TimeZoneNames timeZoneNames = TimeZoneNames.getInstance(locale);
                String[] names3 = new String[(TYPES.length + 1)];
                names3[0] = str;
                names = names3;
                timeZoneNames.getDisplayNames(ZoneMeta.getCanonicalCLDRID(id), TYPES, System.currentTimeMillis(), names3, 1);
                if (names[1] == null || names[2] == null || names[3] == null || names[4] == null) {
                    TimeZone tz = TimeZone.getTimeZone(id);
                    String stdString = TimeZone.createGmtOffsetString(true, true, tz.getRawOffset());
                    String dstString = TimeZone.createGmtOffsetString(true, true, tz.getRawOffset() + tz.getDSTSavings());
                    names[1] = names[1] != null ? names[1] : stdString;
                    names[2] = names[2] != null ? names[2] : stdString;
                    names[3] = names[3] != null ? names[3] : dstString;
                    names[4] = names[4] != null ? names[4] : dstString;
                }
                if (names[5] == null) {
                    names[5] = names[0];
                }
                if (names[6] == null) {
                    names[6] = names[0];
                }
                if (perLocale == null) {
                    perLocale = new ConcurrentHashMap<>();
                }
                perLocale.put(locale2, names);
                cache.put(str, new SoftReference(perLocale));
                switch (type) {
                    case 0:
                        break;
                    case 1:
                        break;
                }
            } else {
                return null;
            }
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            ZoneId zone = (ZoneId) context.getValue(TemporalQueries.zoneId());
            int i = 0;
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

        /* access modifiers changed from: protected */
        public PrefixTree getTree(DateTimeParseContext context) {
            PrefixTree tree;
            String[] names;
            String zid;
            ZoneTextPrinterParser zoneTextPrinterParser = this;
            if (zoneTextPrinterParser.textStyle == TextStyle.NARROW) {
                return super.getTree(context);
            }
            Locale locale = context.getLocale();
            boolean isCaseSensitive = context.isCaseSensitive();
            Set<String> regionIds = ZoneRulesProvider.getAvailableZoneIds();
            int regionIdsSize = regionIds.size();
            Map<Locale, Map.Entry<Integer, SoftReference<PrefixTree>>> cached = isCaseSensitive ? zoneTextPrinterParser.cachedTree : zoneTextPrinterParser.cachedTreeCI;
            Map.Entry<Integer, SoftReference<PrefixTree>> entry = cached.get(locale);
            Map.Entry<Integer, SoftReference<PrefixTree>> entry2 = entry;
            if (entry != null && entry2.getKey().intValue() == regionIdsSize) {
                PrefixTree prefixTree = (PrefixTree) entry2.getValue().get();
                tree = prefixTree;
                if (prefixTree != null) {
                    boolean z = isCaseSensitive;
                    return tree;
                }
            }
            tree = PrefixTree.newTree(context);
            TimeZoneNames timeZoneNames = TimeZoneNames.getInstance(locale);
            long now = System.currentTimeMillis();
            TimeZoneNames.NameType[] types = zoneTextPrinterParser.textStyle == TextStyle.FULL ? FULL_TYPES : SHORT_TYPES;
            String[] names2 = new String[types.length];
            Iterator<String> it = regionIds.iterator();
            while (true) {
                int i = 0;
                if (!it.hasNext()) {
                    break;
                }
                String zid2 = it.next();
                tree.add(zid2, zid2);
                String zid3 = ZoneName.toZid(zid2, locale);
                Iterator<String> it2 = it;
                String zid4 = zid3;
                String[] names3 = names2;
                TimeZoneNames.NameType[] types2 = types;
                timeZoneNames.getDisplayNames(zid3, types, now, names2, 0);
                while (true) {
                    int i2 = i;
                    names = names3;
                    if (i2 >= names.length) {
                        break;
                    }
                    if (names[i2] != null) {
                        zid = zid4;
                        tree.add(names[i2], zid);
                    } else {
                        zid = zid4;
                    }
                    i = i2 + 1;
                    zid4 = zid;
                    names3 = names;
                }
                names2 = names;
                it = it2;
                types = types2;
            }
            TimeZoneNames.NameType[] types3 = types;
            String[] names4 = names2;
            if (zoneTextPrinterParser.preferredZones != null) {
                Iterator<String> it3 = regionIds.iterator();
                while (it3.hasNext()) {
                    String zid5 = it3.next();
                    if (zoneTextPrinterParser.preferredZones.contains(zid5)) {
                        String zid6 = zid5;
                        Iterator<String> it4 = it3;
                        boolean isCaseSensitive2 = isCaseSensitive;
                        String[] names5 = names4;
                        timeZoneNames.getDisplayNames(ZoneName.toZid(zid5, locale), types3, now, names4, 0);
                        for (int i3 = 0; i3 < names5.length; i3++) {
                            if (names5[i3] != null) {
                                tree.add(names5[i3], zid6);
                            }
                        }
                        names4 = names5;
                        it3 = it4;
                        isCaseSensitive = isCaseSensitive2;
                        zoneTextPrinterParser = this;
                    }
                }
            }
            String[] strArr = names4;
            cached.put(locale, new AbstractMap.SimpleImmutableEntry(Integer.valueOf(regionIdsSize), new SoftReference(tree)));
            return tree;
        }
    }

    static {
        FIELD_MAP.put('G', ChronoField.ERA);
        FIELD_MAP.put('y', ChronoField.YEAR_OF_ERA);
        FIELD_MAP.put('u', ChronoField.YEAR);
        FIELD_MAP.put('Q', IsoFields.QUARTER_OF_YEAR);
        FIELD_MAP.put('q', IsoFields.QUARTER_OF_YEAR);
        FIELD_MAP.put('M', ChronoField.MONTH_OF_YEAR);
        FIELD_MAP.put('L', ChronoField.MONTH_OF_YEAR);
        FIELD_MAP.put('D', ChronoField.DAY_OF_YEAR);
        FIELD_MAP.put('d', ChronoField.DAY_OF_MONTH);
        FIELD_MAP.put('F', ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH);
        FIELD_MAP.put('E', ChronoField.DAY_OF_WEEK);
        FIELD_MAP.put('c', ChronoField.DAY_OF_WEEK);
        FIELD_MAP.put('e', ChronoField.DAY_OF_WEEK);
        FIELD_MAP.put('a', ChronoField.AMPM_OF_DAY);
        FIELD_MAP.put('H', ChronoField.HOUR_OF_DAY);
        FIELD_MAP.put('k', ChronoField.CLOCK_HOUR_OF_DAY);
        FIELD_MAP.put('K', ChronoField.HOUR_OF_AMPM);
        FIELD_MAP.put('h', ChronoField.CLOCK_HOUR_OF_AMPM);
        FIELD_MAP.put('m', ChronoField.MINUTE_OF_HOUR);
        FIELD_MAP.put('s', ChronoField.SECOND_OF_MINUTE);
        FIELD_MAP.put('S', ChronoField.NANO_OF_SECOND);
        FIELD_MAP.put('A', ChronoField.MILLI_OF_DAY);
        FIELD_MAP.put('n', ChronoField.NANO_OF_SECOND);
        FIELD_MAP.put('N', ChronoField.NANO_OF_DAY);
    }

    static /* synthetic */ ZoneId lambda$static$0(TemporalAccessor temporal) {
        ZoneId zone = (ZoneId) temporal.query(TemporalQueries.zoneId());
        if (zone == null || (zone instanceof ZoneOffset)) {
            return null;
        }
        return zone;
    }

    public static String getLocalizedDateTimePattern(FormatStyle dateStyle, FormatStyle timeStyle, Chronology chrono, Locale locale) {
        Objects.requireNonNull(locale, "locale");
        Objects.requireNonNull(chrono, "chrono");
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

    private DateTimeFormatterBuilder(DateTimeFormatterBuilder parent2, boolean optional2) {
        this.active = this;
        this.printerParsers = new ArrayList();
        this.valueParserIndex = -1;
        this.parent = parent2;
        this.optional = optional2;
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
        Objects.requireNonNull(field, "field");
        appendInternal(new DefaultValueParser(field, value));
        return this;
    }

    public DateTimeFormatterBuilder appendValue(TemporalField field) {
        Objects.requireNonNull(field, "field");
        appendValue(new NumberPrinterParser(field, 1, 19, SignStyle.NORMAL));
        return this;
    }

    public DateTimeFormatterBuilder appendValue(TemporalField field, int width) {
        Objects.requireNonNull(field, "field");
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
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(signStyle, "signStyle");
        if (minWidth < 1 || minWidth > 19) {
            throw new IllegalArgumentException("The minimum width must be from 1 to 19 inclusive but was " + minWidth);
        } else if (maxWidth < 1 || maxWidth > 19) {
            throw new IllegalArgumentException("The maximum width must be from 1 to 19 inclusive but was " + maxWidth);
        } else if (maxWidth >= minWidth) {
            appendValue(new NumberPrinterParser(field, minWidth, maxWidth, signStyle));
            return this;
        } else {
            throw new IllegalArgumentException("The maximum width must exceed or equal the minimum width but " + maxWidth + " < " + minWidth);
        }
    }

    public DateTimeFormatterBuilder appendValueReduced(TemporalField field, int width, int maxWidth, int baseValue) {
        Objects.requireNonNull(field, "field");
        ReducedPrinterParser reducedPrinterParser = new ReducedPrinterParser(field, width, maxWidth, baseValue, null);
        appendValue((NumberPrinterParser) reducedPrinterParser);
        return this;
    }

    public DateTimeFormatterBuilder appendValueReduced(TemporalField field, int width, int maxWidth, ChronoLocalDate baseDate) {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(baseDate, "baseDate");
        ReducedPrinterParser reducedPrinterParser = new ReducedPrinterParser(field, width, maxWidth, 0, baseDate);
        appendValue((NumberPrinterParser) reducedPrinterParser);
        return this;
    }

    private DateTimeFormatterBuilder appendValue(NumberPrinterParser pp) {
        NumberPrinterParser basePP;
        if (this.active.valueParserIndex >= 0) {
            int activeValueParser = this.active.valueParserIndex;
            NumberPrinterParser basePP2 = (NumberPrinterParser) this.active.printerParsers.get(activeValueParser);
            if (pp.minWidth == pp.maxWidth && pp.signStyle == SignStyle.NOT_NEGATIVE) {
                basePP = basePP2.withSubsequentWidth(pp.maxWidth);
                appendInternal(pp.withFixedWidth());
                this.active.valueParserIndex = activeValueParser;
            } else {
                basePP = basePP2.withFixedWidth();
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
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(textStyle, "textStyle");
        appendInternal(new TextPrinterParser(field, textStyle, DateTimeTextProvider.getInstance()));
        return this;
    }

    public DateTimeFormatterBuilder appendText(TemporalField field, Map<Long, String> textLookup) {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(textLookup, "textLookup");
        final DateTimeTextProvider.LocaleStore store = new DateTimeTextProvider.LocaleStore(Collections.singletonMap(TextStyle.FULL, new LinkedHashMap<>((Map<? extends Long, ? extends String>) textLookup)));
        appendInternal(new TextPrinterParser(field, TextStyle.FULL, new DateTimeTextProvider() {
            public String getText(TemporalField field, long value, TextStyle style, Locale locale) {
                return store.getText(value, style);
            }

            public Iterator<Map.Entry<String, Long>> getTextIterator(TemporalField field, TextStyle style, Locale locale) {
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
        Objects.requireNonNull(style, "style");
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
        Objects.requireNonNull(preferredZones, "preferredZones");
        appendInternal(new ZoneTextPrinterParser(textStyle, preferredZones));
        return this;
    }

    public DateTimeFormatterBuilder appendChronologyId() {
        appendInternal(new ChronoPrinterParser(null));
        return this;
    }

    public DateTimeFormatterBuilder appendChronologyText(TextStyle textStyle) {
        Objects.requireNonNull(textStyle, "textStyle");
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
        Objects.requireNonNull(literal, "literal");
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
        Objects.requireNonNull(formatter, "formatter");
        appendInternal(formatter.toPrinterParser(false));
        return this;
    }

    public DateTimeFormatterBuilder appendOptional(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        appendInternal(formatter.toPrinterParser(true));
        return this;
    }

    public DateTimeFormatterBuilder appendPattern(String pattern) {
        Objects.requireNonNull(pattern, "pattern");
        parsePattern(pattern);
        return this;
    }

    private void parsePattern(String pattern) {
        int pos = 0;
        while (pos < pattern.length()) {
            char cur = pattern.charAt(pos);
            if ((cur >= 'A' && cur <= 'Z') || (cur >= 'a' && cur <= 'z')) {
                int pos2 = pos + 1;
                while (pos2 < pattern.length() && pattern.charAt(pos2) == cur) {
                    pos2++;
                }
                int count = pos2 - pos;
                if (cur == 'p') {
                    int pad = 0;
                    if (pos2 < pattern.length()) {
                        cur = pattern.charAt(pos2);
                        if ((cur >= 'A' && cur <= 'Z') || (cur >= 'a' && cur <= 'z')) {
                            pad = count;
                            int pos3 = pos2 + 1;
                            int start = pos2;
                            while (pos3 < pattern.length() && pattern.charAt(pos3) == cur) {
                                pos3++;
                            }
                            pos2 = pos3;
                            count = pos3 - start;
                        }
                    }
                    if (pad != 0) {
                        padNext(pad);
                    } else {
                        throw new IllegalArgumentException("Pad letter 'p' must be followed by valid pad pattern: " + pattern);
                    }
                }
                TemporalField field = FIELD_MAP.get(Character.valueOf(cur));
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
                    if (count == 2) {
                        appendZoneId();
                    } else {
                        throw new IllegalArgumentException("Pattern letter count must be 2: " + cur);
                    }
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
                    if (count <= 5) {
                        appendOffset(OffsetIdPrinterParser.PATTERNS[(count == 1 ? 0 : 1) + count], "Z");
                    } else {
                        throw new IllegalArgumentException("Too many pattern letters: " + cur);
                    }
                } else if (cur == 'x') {
                    if (count <= 5) {
                        appendOffset(OffsetIdPrinterParser.PATTERNS[(count == 1 ? 0 : 1) + count], count == 1 ? "+00" : count % 2 == 0 ? "+0000" : "+00:00");
                    } else {
                        throw new IllegalArgumentException("Too many pattern letters: " + cur);
                    }
                } else if (cur == 'W') {
                    if (count <= 1) {
                        appendInternal(new WeekBasedFieldPrinterParser(cur, count));
                    } else {
                        throw new IllegalArgumentException("Too many pattern letters: " + cur);
                    }
                } else if (cur == 'w') {
                    if (count <= 2) {
                        appendInternal(new WeekBasedFieldPrinterParser(cur, count));
                    } else {
                        throw new IllegalArgumentException("Too many pattern letters: " + cur);
                    }
                } else if (cur == 'Y') {
                    appendInternal(new WeekBasedFieldPrinterParser(cur, count));
                } else {
                    throw new IllegalArgumentException("Unknown pattern letter: " + cur);
                }
                pos = pos2 - 1;
            } else if (cur == '\'') {
                int pos4 = pos + 1;
                while (pos4 < pattern.length()) {
                    if (pattern.charAt(pos4) == '\'') {
                        if (pos4 + 1 >= pattern.length() || pattern.charAt(pos4 + 1) != '\'') {
                            break;
                        }
                        pos4++;
                    }
                    pos4++;
                }
                if (pos4 < pattern.length()) {
                    String str = pattern.substring(pos + 1, pos4);
                    if (str.length() == 0) {
                        appendLiteral('\'');
                    } else {
                        appendLiteral(str.replace((CharSequence) "''", (CharSequence) "'"));
                    }
                    pos = pos4;
                } else {
                    throw new IllegalArgumentException("Pattern ends with an incomplete string literal: " + pattern);
                }
            } else if (cur == '[') {
                optionalStart();
            } else if (cur == ']') {
                if (this.active.parent != null) {
                    optionalEnd();
                } else {
                    throw new IllegalArgumentException("Pattern invalid as it contains ] without previous [");
                }
            } else if (cur == '{' || cur == '}' || cur == '#') {
                throw new IllegalArgumentException("Pattern includes reserved character: '" + cur + "'");
            } else {
                appendLiteral(cur);
            }
            pos++;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x006a, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x006c, code lost:
        if (r6 != 1) goto L_0x0073;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x006e, code lost:
        appendValue(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0073, code lost:
        if (r6 != 2) goto L_0x007a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0075, code lost:
        appendValue(r7, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0090, code lost:
        throw new java.lang.IllegalArgumentException("Too many pattern letters: " + r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00de, code lost:
        switch(r6) {
            case 1: goto L_0x0119;
            case 2: goto L_0x0119;
            case 3: goto L_0x010e;
            case 4: goto L_0x0103;
            case 5: goto L_0x00f8;
            default: goto L_0x00e1;
        };
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00f7, code lost:
        throw new java.lang.IllegalArgumentException("Too many pattern letters: " + r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00f8, code lost:
        if (r0 == false) goto L_0x00fd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00fa, code lost:
        r1 = java.time.format.TextStyle.NARROW_STANDALONE;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00fd, code lost:
        r1 = java.time.format.TextStyle.NARROW;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00ff, code lost:
        appendText(r7, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0103, code lost:
        if (r0 == false) goto L_0x0108;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0105, code lost:
        r1 = java.time.format.TextStyle.FULL_STANDALONE;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0108, code lost:
        r1 = java.time.format.TextStyle.FULL;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x010a, code lost:
        appendText(r7, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x010e, code lost:
        if (r0 == false) goto L_0x0113;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0110, code lost:
        r1 = java.time.format.TextStyle.SHORT_STANDALONE;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0113, code lost:
        r1 = java.time.format.TextStyle.SHORT;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0115, code lost:
        appendText(r7, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x011b, code lost:
        if (r5 == 'c') goto L_0x0136;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x011f, code lost:
        if (r5 != 'e') goto L_0x0122;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0124, code lost:
        if (r5 != 'E') goto L_0x012c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0126, code lost:
        appendText(r7, java.time.format.TextStyle.SHORT);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x012c, code lost:
        if (r6 != 1) goto L_0x0132;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x012e, code lost:
        appendValue(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0132, code lost:
        appendValue(r7, 2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0136, code lost:
        appendInternal(new java.time.format.DateTimeFormatterBuilder.WeekBasedFieldPrinterParser(r5, r6));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:?, code lost:
        return;
     */
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
                break;
            case Types.DATALINK:
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
                break;
            default:
                switch (cur) {
                    case 'K':
                        break;
                    case 'L':
                        break;
                    case 'M':
                        break;
                    default:
                        switch (cur) {
                            case 'c':
                                if (count == 2) {
                                    throw new IllegalArgumentException("Invalid pattern \"cc\"");
                                }
                                break;
                            case 'd':
                                break;
                            case 'e':
                                break;
                            default:
                                switch (cur) {
                                    case 'Q':
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
                                    case 'h':
                                    case 'k':
                                    case 'm':
                                    case 's':
                                        break;
                                    case 'q':
                                        break;
                                    case 'u':
                                    case 'y':
                                        if (count == 2) {
                                            appendValueReduced(field, 2, 2, (ChronoLocalDate) ReducedPrinterParser.BASE_DATE);
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
                        }
                }
        }
    }

    public DateTimeFormatterBuilder padNext(int padWidth) {
        return padNext(padWidth, ' ');
    }

    public DateTimeFormatterBuilder padNext(int padWidth, char padChar) {
        if (padWidth >= 1) {
            this.active.padNextWidth = padWidth;
            this.active.padNextChar = padChar;
            this.active.valueParserIndex = -1;
            return this;
        }
        throw new IllegalArgumentException("The pad width must be at least one but was " + padWidth);
    }

    public DateTimeFormatterBuilder optionalStart() {
        this.active.valueParserIndex = -1;
        this.active = new DateTimeFormatterBuilder(this.active, true);
        return this;
    }

    public DateTimeFormatterBuilder optionalEnd() {
        if (this.active.parent != null) {
            if (this.active.printerParsers.size() > 0) {
                CompositePrinterParser cpp = new CompositePrinterParser(this.active.printerParsers, this.active.optional);
                this.active = this.active.parent;
                appendInternal(cpp);
            } else {
                this.active = this.active.parent;
            }
            return this;
        }
        throw new IllegalStateException("Cannot call optionalEnd() as there was no previous call to optionalStart()");
    }

    private int appendInternal(DateTimePrinterParser pp) {
        Objects.requireNonNull(pp, "pp");
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
        return toFormatter(Locale.getDefault(Locale.Category.FORMAT));
    }

    public DateTimeFormatter toFormatter(Locale locale) {
        return toFormatter(locale, ResolverStyle.SMART, null);
    }

    /* access modifiers changed from: package-private */
    public DateTimeFormatter toFormatter(ResolverStyle resolverStyle, Chronology chrono) {
        return toFormatter(Locale.getDefault(Locale.Category.FORMAT), resolverStyle, chrono);
    }

    private DateTimeFormatter toFormatter(Locale locale, ResolverStyle resolverStyle, Chronology chrono) {
        Objects.requireNonNull(locale, "locale");
        while (this.active.parent != null) {
            optionalEnd();
        }
        DateTimeFormatter dateTimeFormatter = new DateTimeFormatter(new CompositePrinterParser(this.printerParsers, false), locale, DecimalStyle.STANDARD, resolverStyle, null, chrono, null);
        return dateTimeFormatter;
    }
}

package android.icu.text;

import android.icu.impl.CacheBase;
import android.icu.impl.DontCareFieldPosition;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleFormatterImpl;
import android.icu.impl.SoftCache;
import android.icu.impl.StandardPlural;
import android.icu.impl.UResource;
import android.icu.impl.coll.CollationSettings;
import android.icu.lang.UCharacter;
import android.icu.text.DisplayContext;
import android.icu.text.MessagePattern;
import android.icu.util.ICUException;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.lang.reflect.Array;
import java.text.FieldPosition;
import java.util.EnumMap;
import java.util.Locale;

public final class RelativeDateTimeFormatter {
    private static final Cache cache = new Cache();
    /* access modifiers changed from: private */
    public static final Style[] fallbackCache = new Style[3];
    private final BreakIterator breakIterator;
    private final DisplayContext capitalizationContext;
    private final String combinedDateAndTime;
    private final DateFormatSymbols dateFormatSymbols;
    private final ULocale locale;
    private final NumberFormat numberFormat;
    private final EnumMap<Style, EnumMap<RelativeUnit, String[][]>> patternMap;
    private final PluralRules pluralRules;
    private final EnumMap<Style, EnumMap<AbsoluteUnit, EnumMap<Direction, String>>> qualitativeUnitMap;
    private final Style style;
    private int[] styleToDateFormatSymbolsWidth = {1, 3, 2};

    public enum AbsoluteUnit {
        SUNDAY,
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY,
        DAY,
        WEEK,
        MONTH,
        YEAR,
        NOW,
        QUARTER
    }

    private static class Cache {
        private final CacheBase<String, RelativeDateTimeFormatterData, ULocale> cache;

        private Cache() {
            this.cache = new SoftCache<String, RelativeDateTimeFormatterData, ULocale>() {
                /* access modifiers changed from: protected */
                public RelativeDateTimeFormatterData createInstance(String key, ULocale locale) {
                    return new Loader(locale).load();
                }
            };
        }

        public RelativeDateTimeFormatterData get(ULocale locale) {
            return this.cache.getInstance(locale.toString(), locale);
        }
    }

    public enum Direction {
        LAST_2,
        LAST,
        THIS,
        NEXT,
        NEXT_2,
        PLAIN
    }

    private static class Loader {
        private final ULocale ulocale;

        public Loader(ULocale ulocale2) {
            this.ulocale = ulocale2;
        }

        private String getDateTimePattern(ICUResourceBundle r) {
            String calType = r.getStringWithFallback("calendar/default");
            if (calType == null || calType.equals("")) {
                calType = "gregorian";
            }
            ICUResourceBundle patternsRb = r.findWithFallback("calendar/" + calType + "/DateTimePatterns");
            if (patternsRb == null && calType.equals("gregorian")) {
                patternsRb = r.findWithFallback("calendar/gregorian/DateTimePatterns");
            }
            if (patternsRb == null || patternsRb.getSize() < 9) {
                return "{1} {0}";
            }
            if (patternsRb.get(8).getType() == 8) {
                return patternsRb.get(8).getString(0);
            }
            return patternsRb.getString(8);
        }

        public RelativeDateTimeFormatterData load() {
            RelDateTimeDataSink sink = new RelDateTimeDataSink();
            ICUResourceBundle r = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, this.ulocale);
            r.getAllItemsWithFallback("fields", sink);
            for (Style testStyle : Style.values()) {
                Style newStyle1 = RelativeDateTimeFormatter.fallbackCache[testStyle.ordinal()];
                if (newStyle1 != null) {
                    Style newStyle2 = RelativeDateTimeFormatter.fallbackCache[newStyle1.ordinal()];
                    if (!(newStyle2 == null || RelativeDateTimeFormatter.fallbackCache[newStyle2.ordinal()] == null)) {
                        throw new IllegalStateException("Style fallback too deep");
                    }
                }
            }
            return new RelativeDateTimeFormatterData(sink.qualitativeUnitMap, sink.styleRelUnitPatterns, getDateTimePattern(r));
        }
    }

    private static final class RelDateTimeDataSink extends UResource.Sink {
        int pastFutureIndex;
        EnumMap<Style, EnumMap<AbsoluteUnit, EnumMap<Direction, String>>> qualitativeUnitMap = new EnumMap<>(Style.class);
        StringBuilder sb = new StringBuilder();
        Style style;
        EnumMap<Style, EnumMap<RelativeUnit, String[][]>> styleRelUnitPatterns = new EnumMap<>(Style.class);
        DateTimeUnit unit;

        private enum DateTimeUnit {
            SECOND(RelativeUnit.SECONDS, null),
            MINUTE(RelativeUnit.MINUTES, null),
            HOUR(RelativeUnit.HOURS, null),
            DAY(RelativeUnit.DAYS, AbsoluteUnit.DAY),
            WEEK(RelativeUnit.WEEKS, AbsoluteUnit.WEEK),
            MONTH(RelativeUnit.MONTHS, AbsoluteUnit.MONTH),
            QUARTER(RelativeUnit.QUARTERS, AbsoluteUnit.QUARTER),
            YEAR(RelativeUnit.YEARS, AbsoluteUnit.YEAR),
            SUNDAY(null, AbsoluteUnit.SUNDAY),
            MONDAY(null, AbsoluteUnit.MONDAY),
            TUESDAY(null, AbsoluteUnit.TUESDAY),
            WEDNESDAY(null, AbsoluteUnit.WEDNESDAY),
            THURSDAY(null, AbsoluteUnit.THURSDAY),
            FRIDAY(null, AbsoluteUnit.FRIDAY),
            SATURDAY(null, AbsoluteUnit.SATURDAY);
            
            AbsoluteUnit absUnit;
            RelativeUnit relUnit;

            private DateTimeUnit(RelativeUnit relUnit2, AbsoluteUnit absUnit2) {
                this.relUnit = relUnit2;
                this.absUnit = absUnit2;
            }

            /* access modifiers changed from: private */
            public static final DateTimeUnit orNullFromString(CharSequence keyword) {
                switch (keyword.length()) {
                    case 3:
                        if ("day".contentEquals(keyword)) {
                            return DAY;
                        }
                        if ("sun".contentEquals(keyword)) {
                            return SUNDAY;
                        }
                        if ("mon".contentEquals(keyword)) {
                            return MONDAY;
                        }
                        if ("tue".contentEquals(keyword)) {
                            return TUESDAY;
                        }
                        if ("wed".contentEquals(keyword)) {
                            return WEDNESDAY;
                        }
                        if ("thu".contentEquals(keyword)) {
                            return THURSDAY;
                        }
                        if ("fri".contentEquals(keyword)) {
                            return FRIDAY;
                        }
                        if ("sat".contentEquals(keyword)) {
                            return SATURDAY;
                        }
                        break;
                    case 4:
                        if ("hour".contentEquals(keyword)) {
                            return HOUR;
                        }
                        if ("week".contentEquals(keyword)) {
                            return WEEK;
                        }
                        if ("year".contentEquals(keyword)) {
                            return YEAR;
                        }
                        break;
                    case 5:
                        if ("month".contentEquals(keyword)) {
                            return MONTH;
                        }
                        break;
                    case 6:
                        if ("minute".contentEquals(keyword)) {
                            return MINUTE;
                        }
                        if ("second".contentEquals(keyword)) {
                            return SECOND;
                        }
                        break;
                    case 7:
                        if ("quarter".contentEquals(keyword)) {
                            return QUARTER;
                        }
                        break;
                }
                return null;
            }
        }

        private Style styleFromKey(UResource.Key key) {
            if (key.endsWith("-short")) {
                return Style.SHORT;
            }
            if (key.endsWith("-narrow")) {
                return Style.NARROW;
            }
            return Style.LONG;
        }

        private Style styleFromAlias(UResource.Value value) {
            String s = value.getAliasString();
            if (s.endsWith("-short")) {
                return Style.SHORT;
            }
            if (s.endsWith("-narrow")) {
                return Style.NARROW;
            }
            return Style.LONG;
        }

        private static int styleSuffixLength(Style style2) {
            switch (style2) {
                case SHORT:
                    return 6;
                case NARROW:
                    return 7;
                default:
                    return 0;
            }
        }

        public void consumeTableRelative(UResource.Key key, UResource.Value value) {
            UResource.Table unitTypesTable = value.getTable();
            for (int i = 0; unitTypesTable.getKeyAndValue(i, key, value); i++) {
                if (value.getType() == 0) {
                    String valueString = value.getString();
                    EnumMap<AbsoluteUnit, EnumMap<Direction, String>> absMap = this.qualitativeUnitMap.get(this.style);
                    if (this.unit.relUnit != RelativeUnit.SECONDS || !key.contentEquals(AndroidHardcodedSystemProperties.JAVA_VERSION)) {
                        Direction keyDirection = RelativeDateTimeFormatter.keyToDirection(key);
                        if (keyDirection != null) {
                            AbsoluteUnit absUnit = this.unit.absUnit;
                            if (absUnit != null) {
                                if (absMap == null) {
                                    absMap = new EnumMap<>(AbsoluteUnit.class);
                                    this.qualitativeUnitMap.put(this.style, absMap);
                                }
                                EnumMap<Direction, String> dirMap = absMap.get(absUnit);
                                if (dirMap == null) {
                                    dirMap = new EnumMap<>(Direction.class);
                                    absMap.put(absUnit, dirMap);
                                }
                                if (dirMap.get(keyDirection) == null) {
                                    dirMap.put(keyDirection, value.getString());
                                }
                            }
                        }
                    } else {
                        EnumMap<Direction, String> unitStrings = absMap.get(AbsoluteUnit.NOW);
                        if (unitStrings == null) {
                            unitStrings = new EnumMap<>(Direction.class);
                            absMap.put(AbsoluteUnit.NOW, unitStrings);
                        }
                        if (unitStrings.get(Direction.PLAIN) == null) {
                            unitStrings.put(Direction.PLAIN, valueString);
                        }
                    }
                }
            }
        }

        public void consumeTableRelativeTime(UResource.Key key, UResource.Value value) {
            if (this.unit.relUnit != null) {
                UResource.Table unitTypesTable = value.getTable();
                for (int i = 0; unitTypesTable.getKeyAndValue(i, key, value); i++) {
                    if (key.contentEquals("past")) {
                        this.pastFutureIndex = 0;
                    } else if (key.contentEquals("future")) {
                        this.pastFutureIndex = 1;
                    }
                    consumeTimeDetail(key, value);
                }
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v3, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v6, resolved type: java.lang.String[][]} */
        /* JADX WARNING: Multi-variable type inference failed */
        public void consumeTimeDetail(UResource.Key key, UResource.Value value) {
            UResource.Table unitTypesTable = value.getTable();
            EnumMap<RelativeUnit, String[][]> unitPatterns = this.styleRelUnitPatterns.get(this.style);
            if (unitPatterns == null) {
                unitPatterns = new EnumMap<>(RelativeUnit.class);
                this.styleRelUnitPatterns.put(this.style, unitPatterns);
            }
            String[][] patterns = unitPatterns.get(this.unit.relUnit);
            if (patterns == null) {
                patterns = Array.newInstance(String.class, new int[]{2, StandardPlural.COUNT});
                unitPatterns.put(this.unit.relUnit, patterns);
            }
            for (int i = 0; unitTypesTable.getKeyAndValue(i, key, value); i++) {
                if (value.getType() == 0) {
                    int pluralIndex = StandardPlural.indexFromString(key.toString());
                    if (patterns[this.pastFutureIndex][pluralIndex] == null) {
                        patterns[this.pastFutureIndex][pluralIndex] = SimpleFormatterImpl.compileToStringMinMaxArguments(value.getString(), this.sb, 0, 1);
                    }
                }
            }
        }

        private void handlePlainDirection(UResource.Key key, UResource.Value value) {
            AbsoluteUnit absUnit = this.unit.absUnit;
            if (absUnit != null) {
                EnumMap<AbsoluteUnit, EnumMap<Direction, String>> unitMap = this.qualitativeUnitMap.get(this.style);
                if (unitMap == null) {
                    unitMap = new EnumMap<>(AbsoluteUnit.class);
                    this.qualitativeUnitMap.put(this.style, unitMap);
                }
                EnumMap<Direction, String> dirMap = unitMap.get(absUnit);
                if (dirMap == null) {
                    dirMap = new EnumMap<>(Direction.class);
                    unitMap.put(absUnit, dirMap);
                }
                if (dirMap.get(Direction.PLAIN) == null) {
                    dirMap.put(Direction.PLAIN, value.toString());
                }
            }
        }

        public void consumeTimeUnit(UResource.Key key, UResource.Value value) {
            UResource.Table unitTypesTable = value.getTable();
            for (int i = 0; unitTypesTable.getKeyAndValue(i, key, value); i++) {
                if (key.contentEquals("dn") && value.getType() == 0) {
                    handlePlainDirection(key, value);
                }
                if (value.getType() == 2) {
                    if (key.contentEquals("relative")) {
                        consumeTableRelative(key, value);
                    } else if (key.contentEquals("relativeTime")) {
                        consumeTableRelativeTime(key, value);
                    }
                }
            }
        }

        private void handleAlias(UResource.Key key, UResource.Value value, boolean noFallback) {
            Style sourceStyle = styleFromKey(key);
            if (DateTimeUnit.orNullFromString(key.substring(0, key.length() - styleSuffixLength(sourceStyle))) != null) {
                Style targetStyle = styleFromAlias(value);
                if (sourceStyle != targetStyle) {
                    if (RelativeDateTimeFormatter.fallbackCache[sourceStyle.ordinal()] == null) {
                        RelativeDateTimeFormatter.fallbackCache[sourceStyle.ordinal()] = targetStyle;
                    } else if (RelativeDateTimeFormatter.fallbackCache[sourceStyle.ordinal()] != targetStyle) {
                        throw new ICUException("Inconsistent style fallback for style " + sourceStyle + " to " + targetStyle);
                    }
                    return;
                }
                throw new ICUException("Invalid style fallback from " + sourceStyle + " to itself");
            }
        }

        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            if (value.getType() != 3) {
                UResource.Table table = value.getTable();
                for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                    if (value.getType() == 3) {
                        handleAlias(key, value, noFallback);
                    } else {
                        this.style = styleFromKey(key);
                        this.unit = DateTimeUnit.orNullFromString(key.substring(0, key.length() - styleSuffixLength(this.style)));
                        if (this.unit != null) {
                            consumeTimeUnit(key, value);
                        }
                    }
                }
            }
        }

        RelDateTimeDataSink() {
        }
    }

    private static class RelativeDateTimeFormatterData {
        public final String dateTimePattern;
        public final EnumMap<Style, EnumMap<AbsoluteUnit, EnumMap<Direction, String>>> qualitativeUnitMap;
        EnumMap<Style, EnumMap<RelativeUnit, String[][]>> relUnitPatternMap;

        public RelativeDateTimeFormatterData(EnumMap<Style, EnumMap<AbsoluteUnit, EnumMap<Direction, String>>> qualitativeUnitMap2, EnumMap<Style, EnumMap<RelativeUnit, String[][]>> relUnitPatternMap2, String dateTimePattern2) {
            this.qualitativeUnitMap = qualitativeUnitMap2;
            this.relUnitPatternMap = relUnitPatternMap2;
            this.dateTimePattern = dateTimePattern2;
        }
    }

    public enum RelativeDateTimeUnit {
        YEAR,
        QUARTER,
        MONTH,
        WEEK,
        DAY,
        HOUR,
        MINUTE,
        SECOND,
        SUNDAY,
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY
    }

    public enum RelativeUnit {
        SECONDS,
        MINUTES,
        HOURS,
        DAYS,
        WEEKS,
        MONTHS,
        YEARS,
        QUARTERS
    }

    public enum Style {
        LONG,
        SHORT,
        NARROW;
        
        private static final int INDEX_COUNT = 3;
    }

    public static RelativeDateTimeFormatter getInstance() {
        return getInstance(ULocale.getDefault(), null, Style.LONG, DisplayContext.CAPITALIZATION_NONE);
    }

    public static RelativeDateTimeFormatter getInstance(ULocale locale2) {
        return getInstance(locale2, null, Style.LONG, DisplayContext.CAPITALIZATION_NONE);
    }

    public static RelativeDateTimeFormatter getInstance(Locale locale2) {
        return getInstance(ULocale.forLocale(locale2));
    }

    public static RelativeDateTimeFormatter getInstance(ULocale locale2, NumberFormat nf) {
        return getInstance(locale2, nf, Style.LONG, DisplayContext.CAPITALIZATION_NONE);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v5, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v2, resolved type: android.icu.text.NumberFormat} */
    /* JADX WARNING: Multi-variable type inference failed */
    public static RelativeDateTimeFormatter getInstance(ULocale locale2, NumberFormat nf, Style style2, DisplayContext capitalizationContext2) {
        NumberFormat nf2;
        BreakIterator breakIterator2;
        RelativeDateTimeFormatterData data = cache.get(locale2);
        if (nf == null) {
            nf2 = NumberFormat.getInstance(locale2);
        } else {
            nf2 = nf.clone();
        }
        EnumMap<Style, EnumMap<AbsoluteUnit, EnumMap<Direction, String>>> enumMap = data.qualitativeUnitMap;
        EnumMap<Style, EnumMap<RelativeUnit, String[][]>> enumMap2 = data.relUnitPatternMap;
        String str = data.dateTimePattern;
        PluralRules forLocale = PluralRules.forLocale(locale2);
        if (capitalizationContext2 == DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE) {
            breakIterator2 = BreakIterator.getSentenceInstance(locale2);
        } else {
            breakIterator2 = null;
        }
        RelativeDateTimeFormatter relativeDateTimeFormatter = new RelativeDateTimeFormatter(enumMap, enumMap2, str, forLocale, nf2, style2, capitalizationContext2, breakIterator2, locale2);
        return relativeDateTimeFormatter;
    }

    public static RelativeDateTimeFormatter getInstance(Locale locale2, NumberFormat nf) {
        return getInstance(ULocale.forLocale(locale2), nf);
    }

    public String format(double quantity, Direction direction, RelativeUnit unit) {
        String result;
        if (direction == Direction.LAST || direction == Direction.NEXT) {
            int pastFutureIndex = direction == Direction.NEXT ? 1 : 0;
            synchronized (this.numberFormat) {
                StringBuffer formatStr = new StringBuffer();
                result = SimpleFormatterImpl.formatCompiledPattern(getRelativeUnitPluralPattern(this.style, unit, pastFutureIndex, QuantityFormatter.selectPlural(Double.valueOf(quantity), this.numberFormat, this.pluralRules, formatStr, DontCareFieldPosition.INSTANCE)), formatStr);
            }
            return adjustForContext(result);
        }
        throw new IllegalArgumentException("direction must be NEXT or LAST");
    }

    public String formatNumeric(double offset, RelativeDateTimeUnit unit) {
        RelativeUnit relunit = RelativeUnit.SECONDS;
        switch (unit) {
            case YEAR:
                relunit = RelativeUnit.YEARS;
                break;
            case QUARTER:
                relunit = RelativeUnit.QUARTERS;
                break;
            case MONTH:
                relunit = RelativeUnit.MONTHS;
                break;
            case WEEK:
                relunit = RelativeUnit.WEEKS;
                break;
            case DAY:
                relunit = RelativeUnit.DAYS;
                break;
            case HOUR:
                relunit = RelativeUnit.HOURS;
                break;
            case MINUTE:
                relunit = RelativeUnit.MINUTES;
                break;
            case SECOND:
                break;
            default:
                throw new UnsupportedOperationException("formatNumeric does not currently support RelativeUnit.SUNDAY..SATURDAY");
        }
        Direction direction = Direction.NEXT;
        if (offset < 0.0d) {
            direction = Direction.LAST;
            offset = -offset;
        }
        String result = format(offset, direction, relunit);
        return result != null ? result : "";
    }

    public String format(Direction direction, AbsoluteUnit unit) {
        String result;
        if (unit != AbsoluteUnit.NOW || direction == Direction.PLAIN) {
            if (direction != Direction.PLAIN || AbsoluteUnit.SUNDAY.ordinal() > unit.ordinal() || unit.ordinal() > AbsoluteUnit.SATURDAY.ordinal()) {
                result = getAbsoluteUnitString(this.style, unit, direction);
            } else {
                result = this.dateFormatSymbols.getWeekdays(1, this.styleToDateFormatSymbolsWidth[this.style.ordinal()])[(unit.ordinal() - AbsoluteUnit.SUNDAY.ordinal()) + 1];
            }
            if (result != null) {
                return adjustForContext(result);
            }
            return null;
        }
        throw new IllegalArgumentException("NOW can only accept direction PLAIN.");
    }

    public String format(double offset, RelativeDateTimeUnit unit) {
        boolean useNumeric = true;
        Direction direction = Direction.THIS;
        if (offset > -2.1d && offset < 2.1d) {
            double offsetx100 = 100.0d * offset;
            int intoffsetx100 = offsetx100 < 0.0d ? (int) (offsetx100 - 0.5d) : (int) (0.5d + offsetx100);
            if (intoffsetx100 == -200) {
                direction = Direction.LAST_2;
                useNumeric = false;
            } else if (intoffsetx100 == -100) {
                direction = Direction.LAST;
                useNumeric = false;
            } else if (intoffsetx100 == 0) {
                useNumeric = false;
            } else if (intoffsetx100 == 100) {
                direction = Direction.NEXT;
                useNumeric = false;
            } else if (intoffsetx100 == 200) {
                direction = Direction.NEXT_2;
                useNumeric = false;
            }
        }
        AbsoluteUnit absunit = AbsoluteUnit.NOW;
        switch (unit) {
            case YEAR:
                absunit = AbsoluteUnit.YEAR;
                break;
            case QUARTER:
                absunit = AbsoluteUnit.QUARTER;
                break;
            case MONTH:
                absunit = AbsoluteUnit.MONTH;
                break;
            case WEEK:
                absunit = AbsoluteUnit.WEEK;
                break;
            case DAY:
                absunit = AbsoluteUnit.DAY;
                break;
            case SECOND:
                if (direction != Direction.THIS) {
                    useNumeric = true;
                    break;
                } else {
                    direction = Direction.PLAIN;
                    break;
                }
            case SUNDAY:
                absunit = AbsoluteUnit.SUNDAY;
                break;
            case MONDAY:
                absunit = AbsoluteUnit.MONDAY;
                break;
            case TUESDAY:
                absunit = AbsoluteUnit.TUESDAY;
                break;
            case WEDNESDAY:
                absunit = AbsoluteUnit.WEDNESDAY;
                break;
            case THURSDAY:
                absunit = AbsoluteUnit.THURSDAY;
                break;
            case FRIDAY:
                absunit = AbsoluteUnit.FRIDAY;
                break;
            case SATURDAY:
                absunit = AbsoluteUnit.SATURDAY;
                break;
            default:
                useNumeric = true;
                break;
        }
        if (!useNumeric) {
            String result = format(direction, absunit);
            if (result != null && result.length() > 0) {
                return result;
            }
        }
        return formatNumeric(offset, unit);
    }

    private String getAbsoluteUnitString(Style style2, AbsoluteUnit unit, Direction direction) {
        Style style3;
        do {
            EnumMap<AbsoluteUnit, EnumMap<Direction, String>> unitMap = this.qualitativeUnitMap.get(style2);
            if (unitMap != null) {
                EnumMap<Direction, String> dirMap = unitMap.get(unit);
                if (dirMap != null) {
                    String result = dirMap.get(direction);
                    if (result != null) {
                        return result;
                    }
                }
            }
            style3 = fallbackCache[style2.ordinal()];
            style2 = style3;
        } while (style3 != null);
        return null;
    }

    public String combineDateAndTime(String relativeDateString, String timeString) {
        MessageFormat msgFmt = new MessageFormat("");
        msgFmt.applyPattern(this.combinedDateAndTime, MessagePattern.ApostropheMode.DOUBLE_REQUIRED);
        return msgFmt.format(new Object[]{timeString, relativeDateString}, new StringBuffer(128), new FieldPosition(0)).toString();
    }

    public NumberFormat getNumberFormat() {
        NumberFormat numberFormat2;
        synchronized (this.numberFormat) {
            numberFormat2 = (NumberFormat) this.numberFormat.clone();
        }
        return numberFormat2;
    }

    public DisplayContext getCapitalizationContext() {
        return this.capitalizationContext;
    }

    public Style getFormatStyle() {
        return this.style;
    }

    private String adjustForContext(String originalFormattedString) {
        String titleCase;
        if (this.breakIterator == null || originalFormattedString.length() == 0 || !UCharacter.isLowerCase(UCharacter.codePointAt((CharSequence) originalFormattedString, 0))) {
            return originalFormattedString;
        }
        synchronized (this.breakIterator) {
            titleCase = UCharacter.toTitleCase(this.locale, originalFormattedString, this.breakIterator, (int) CollationSettings.CASE_FIRST_AND_UPPER_MASK);
        }
        return titleCase;
    }

    private RelativeDateTimeFormatter(EnumMap<Style, EnumMap<AbsoluteUnit, EnumMap<Direction, String>>> qualitativeUnitMap2, EnumMap<Style, EnumMap<RelativeUnit, String[][]>> patternMap2, String combinedDateAndTime2, PluralRules pluralRules2, NumberFormat numberFormat2, Style style2, DisplayContext capitalizationContext2, BreakIterator breakIterator2, ULocale locale2) {
        this.qualitativeUnitMap = qualitativeUnitMap2;
        this.patternMap = patternMap2;
        this.combinedDateAndTime = combinedDateAndTime2;
        this.pluralRules = pluralRules2;
        this.numberFormat = numberFormat2;
        this.style = style2;
        if (capitalizationContext2.type() == DisplayContext.Type.CAPITALIZATION) {
            this.capitalizationContext = capitalizationContext2;
            this.breakIterator = breakIterator2;
            this.locale = locale2;
            this.dateFormatSymbols = new DateFormatSymbols(locale2);
            return;
        }
        throw new IllegalArgumentException(capitalizationContext2.toString());
    }

    private String getRelativeUnitPluralPattern(Style style2, RelativeUnit unit, int pastFutureIndex, StandardPlural pluralForm) {
        if (pluralForm != StandardPlural.OTHER) {
            String formatter = getRelativeUnitPattern(style2, unit, pastFutureIndex, pluralForm);
            if (formatter != null) {
                return formatter;
            }
        }
        return getRelativeUnitPattern(style2, unit, pastFutureIndex, StandardPlural.OTHER);
    }

    private String getRelativeUnitPattern(Style style2, RelativeUnit unit, int pastFutureIndex, StandardPlural pluralForm) {
        Style style3;
        int pluralIndex = pluralForm.ordinal();
        do {
            EnumMap<RelativeUnit, String[][]> unitMap = this.patternMap.get(style2);
            if (unitMap != null) {
                String[][] spfCompiledPatterns = unitMap.get(unit);
                if (!(spfCompiledPatterns == null || spfCompiledPatterns[pastFutureIndex][pluralIndex] == null)) {
                    return spfCompiledPatterns[pastFutureIndex][pluralIndex];
                }
            }
            style3 = fallbackCache[style2.ordinal()];
            style2 = style3;
        } while (style3 != null);
        return null;
    }

    /* access modifiers changed from: private */
    public static Direction keyToDirection(UResource.Key key) {
        if (key.contentEquals("-2")) {
            return Direction.LAST_2;
        }
        if (key.contentEquals("-1")) {
            return Direction.LAST;
        }
        if (key.contentEquals(AndroidHardcodedSystemProperties.JAVA_VERSION)) {
            return Direction.THIS;
        }
        if (key.contentEquals("1")) {
            return Direction.NEXT;
        }
        if (key.contentEquals("2")) {
            return Direction.NEXT_2;
        }
        return null;
    }
}

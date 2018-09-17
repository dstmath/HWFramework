package android.icu.text;

import android.icu.impl.CacheBase;
import android.icu.impl.DontCareFieldPosition;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleFormatterImpl;
import android.icu.impl.SoftCache;
import android.icu.impl.StandardPlural;
import android.icu.impl.UResource.Key;
import android.icu.impl.UResource.Sink;
import android.icu.impl.UResource.Table;
import android.icu.impl.UResource.Value;
import android.icu.lang.UCharacter;
import android.icu.text.DisplayContext.Type;
import android.icu.util.ICUException;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.lang.reflect.Array;
import java.util.EnumMap;
import java.util.Locale;

public final class RelativeDateTimeFormatter {
    private static final /* synthetic */ int[] -android-icu-text-RelativeDateTimeFormatter$RelativeDateTimeUnitSwitchesValues = null;
    private static final Cache cache = new Cache();
    private static final Style[] fallbackCache = new Style[3];
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
    private int[] styleToDateFormatSymbolsWidth = new int[]{1, 3, 2};

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

        /* synthetic */ Cache(Cache -this0) {
            this();
        }

        private Cache() {
            this.cache = new SoftCache<String, RelativeDateTimeFormatterData, ULocale>() {
                protected RelativeDateTimeFormatterData createInstance(String key, ULocale locale) {
                    return new Loader(locale).load();
                }
            };
        }

        public RelativeDateTimeFormatterData get(ULocale locale) {
            return (RelativeDateTimeFormatterData) this.cache.getInstance(locale.toString(), locale);
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

        public Loader(ULocale ulocale) {
            this.ulocale = ulocale;
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

    private static final class RelDateTimeDataSink extends Sink {
        private static final /* synthetic */ int[] -android-icu-text-RelativeDateTimeFormatter$StyleSwitchesValues = null;
        int pastFutureIndex;
        EnumMap<Style, EnumMap<AbsoluteUnit, EnumMap<Direction, String>>> qualitativeUnitMap = new EnumMap(Style.class);
        StringBuilder sb = new StringBuilder();
        Style style;
        EnumMap<Style, EnumMap<RelativeUnit, String[][]>> styleRelUnitPatterns = new EnumMap(Style.class);
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

            private DateTimeUnit(RelativeUnit relUnit, AbsoluteUnit absUnit) {
                this.relUnit = relUnit;
                this.absUnit = absUnit;
            }

            private static final DateTimeUnit orNullFromString(CharSequence keyword) {
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

        private static /* synthetic */ int[] -getandroid-icu-text-RelativeDateTimeFormatter$StyleSwitchesValues() {
            if (-android-icu-text-RelativeDateTimeFormatter$StyleSwitchesValues != null) {
                return -android-icu-text-RelativeDateTimeFormatter$StyleSwitchesValues;
            }
            int[] iArr = new int[Style.values().length];
            try {
                iArr[Style.LONG.ordinal()] = 3;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Style.NARROW.ordinal()] = 1;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Style.SHORT.ordinal()] = 2;
            } catch (NoSuchFieldError e3) {
            }
            -android-icu-text-RelativeDateTimeFormatter$StyleSwitchesValues = iArr;
            return iArr;
        }

        private Style styleFromKey(Key key) {
            if (key.endsWith("-short")) {
                return Style.SHORT;
            }
            if (key.endsWith("-narrow")) {
                return Style.NARROW;
            }
            return Style.LONG;
        }

        private Style styleFromAlias(Value value) {
            String s = value.getAliasString();
            if (s.endsWith("-short")) {
                return Style.SHORT;
            }
            if (s.endsWith("-narrow")) {
                return Style.NARROW;
            }
            return Style.LONG;
        }

        private static int styleSuffixLength(Style style) {
            switch (-getandroid-icu-text-RelativeDateTimeFormatter$StyleSwitchesValues()[style.ordinal()]) {
                case 1:
                    return 7;
                case 2:
                    return 6;
                default:
                    return 0;
            }
        }

        public void consumeTableRelative(Key key, Value value) {
            Table unitTypesTable = value.getTable();
            for (int i = 0; unitTypesTable.getKeyAndValue(i, key, value); i++) {
                if (value.getType() == 0) {
                    String valueString = value.getString();
                    EnumMap<AbsoluteUnit, EnumMap<Direction, String>> absMap = (EnumMap) this.qualitativeUnitMap.get(this.style);
                    if (this.unit.relUnit == RelativeUnit.SECONDS && key.contentEquals(AndroidHardcodedSystemProperties.JAVA_VERSION)) {
                        EnumMap<Direction, String> unitStrings = (EnumMap) absMap.get(AbsoluteUnit.NOW);
                        if (unitStrings == null) {
                            unitStrings = new EnumMap(Direction.class);
                            absMap.put(AbsoluteUnit.NOW, unitStrings);
                        }
                        if (unitStrings.get(Direction.PLAIN) == null) {
                            unitStrings.put(Direction.PLAIN, valueString);
                        }
                    } else {
                        Direction keyDirection = RelativeDateTimeFormatter.keyToDirection(key);
                        if (keyDirection != null) {
                            AbsoluteUnit absUnit = this.unit.absUnit;
                            if (absUnit != null) {
                                if (absMap == null) {
                                    absMap = new EnumMap(AbsoluteUnit.class);
                                    this.qualitativeUnitMap.put(this.style, absMap);
                                }
                                EnumMap<Direction, String> dirMap = (EnumMap) absMap.get(absUnit);
                                if (dirMap == null) {
                                    dirMap = new EnumMap(Direction.class);
                                    absMap.put(absUnit, dirMap);
                                }
                                if (dirMap.get(keyDirection) == null) {
                                    dirMap.put(keyDirection, value.getString());
                                }
                            }
                        }
                    }
                }
            }
        }

        public void consumeTableRelativeTime(Key key, Value value) {
            if (this.unit.relUnit != null) {
                Table unitTypesTable = value.getTable();
                for (int i = 0; unitTypesTable.getKeyAndValue(i, key, value); i++) {
                    if (key.contentEquals("past")) {
                        this.pastFutureIndex = 0;
                    } else if (key.contentEquals("future")) {
                        this.pastFutureIndex = 1;
                    } else {
                    }
                    consumeTimeDetail(key, value);
                }
            }
        }

        public void consumeTimeDetail(Key key, Value value) {
            Table unitTypesTable = value.getTable();
            EnumMap<RelativeUnit, String[][]> unitPatterns = (EnumMap) this.styleRelUnitPatterns.get(this.style);
            if (unitPatterns == null) {
                unitPatterns = new EnumMap(RelativeUnit.class);
                this.styleRelUnitPatterns.put(this.style, unitPatterns);
            }
            String[][] patterns = (String[][]) unitPatterns.get(this.unit.relUnit);
            if (patterns == null) {
                patterns = (String[][]) Array.newInstance(String.class, new int[]{2, StandardPlural.COUNT});
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

        private void handlePlainDirection(Key key, Value value) {
            AbsoluteUnit absUnit = this.unit.absUnit;
            if (absUnit != null) {
                EnumMap<AbsoluteUnit, EnumMap<Direction, String>> unitMap = (EnumMap) this.qualitativeUnitMap.get(this.style);
                if (unitMap == null) {
                    unitMap = new EnumMap(AbsoluteUnit.class);
                    this.qualitativeUnitMap.put(this.style, unitMap);
                }
                EnumMap<Direction, String> dirMap = (EnumMap) unitMap.get(absUnit);
                if (dirMap == null) {
                    dirMap = new EnumMap(Direction.class);
                    unitMap.put(absUnit, dirMap);
                }
                if (dirMap.get(Direction.PLAIN) == null) {
                    dirMap.put(Direction.PLAIN, value.toString());
                }
            }
        }

        public void consumeTimeUnit(Key key, Value value) {
            Table unitTypesTable = value.getTable();
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

        private void handleAlias(Key key, Value value, boolean noFallback) {
            Style sourceStyle = styleFromKey(key);
            if (DateTimeUnit.orNullFromString(key.substring(0, key.length() - styleSuffixLength(sourceStyle))) != null) {
                Style targetStyle = styleFromAlias(value);
                if (sourceStyle == targetStyle) {
                    throw new ICUException("Invalid style fallback from " + sourceStyle + " to itself");
                }
                if (RelativeDateTimeFormatter.fallbackCache[sourceStyle.ordinal()] == null) {
                    RelativeDateTimeFormatter.fallbackCache[sourceStyle.ordinal()] = targetStyle;
                } else if (RelativeDateTimeFormatter.fallbackCache[sourceStyle.ordinal()] != targetStyle) {
                    throw new ICUException("Inconsistent style fallback for style " + sourceStyle + " to " + targetStyle);
                }
            }
        }

        public void put(Key key, Value value, boolean noFallback) {
            if (value.getType() != 3) {
                Table table = value.getTable();
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

        public RelativeDateTimeFormatterData(EnumMap<Style, EnumMap<AbsoluteUnit, EnumMap<Direction, String>>> qualitativeUnitMap, EnumMap<Style, EnumMap<RelativeUnit, String[][]>> relUnitPatternMap, String dateTimePattern) {
            this.qualitativeUnitMap = qualitativeUnitMap;
            this.relUnitPatternMap = relUnitPatternMap;
            this.dateTimePattern = dateTimePattern;
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

    private static /* synthetic */ int[] -getandroid-icu-text-RelativeDateTimeFormatter$RelativeDateTimeUnitSwitchesValues() {
        if (-android-icu-text-RelativeDateTimeFormatter$RelativeDateTimeUnitSwitchesValues != null) {
            return -android-icu-text-RelativeDateTimeFormatter$RelativeDateTimeUnitSwitchesValues;
        }
        int[] iArr = new int[RelativeDateTimeUnit.values().length];
        try {
            iArr[RelativeDateTimeUnit.DAY.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[RelativeDateTimeUnit.FRIDAY.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[RelativeDateTimeUnit.HOUR.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[RelativeDateTimeUnit.MINUTE.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[RelativeDateTimeUnit.MONDAY.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[RelativeDateTimeUnit.MONTH.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[RelativeDateTimeUnit.QUARTER.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[RelativeDateTimeUnit.SATURDAY.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[RelativeDateTimeUnit.SECOND.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[RelativeDateTimeUnit.SUNDAY.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[RelativeDateTimeUnit.THURSDAY.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[RelativeDateTimeUnit.TUESDAY.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[RelativeDateTimeUnit.WEDNESDAY.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[RelativeDateTimeUnit.WEEK.ordinal()] = 14;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[RelativeDateTimeUnit.YEAR.ordinal()] = 15;
        } catch (NoSuchFieldError e15) {
        }
        -android-icu-text-RelativeDateTimeFormatter$RelativeDateTimeUnitSwitchesValues = iArr;
        return iArr;
    }

    public static RelativeDateTimeFormatter getInstance() {
        return getInstance(ULocale.getDefault(), null, Style.LONG, DisplayContext.CAPITALIZATION_NONE);
    }

    public static RelativeDateTimeFormatter getInstance(ULocale locale) {
        return getInstance(locale, null, Style.LONG, DisplayContext.CAPITALIZATION_NONE);
    }

    public static RelativeDateTimeFormatter getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale));
    }

    public static RelativeDateTimeFormatter getInstance(ULocale locale, NumberFormat nf) {
        return getInstance(locale, nf, Style.LONG, DisplayContext.CAPITALIZATION_NONE);
    }

    public static RelativeDateTimeFormatter getInstance(ULocale locale, NumberFormat nf, Style style, DisplayContext capitalizationContext) {
        BreakIterator breakIterator = null;
        RelativeDateTimeFormatterData data = cache.get(locale);
        if (nf == null) {
            nf = NumberFormat.getInstance(locale);
        } else {
            nf = (NumberFormat) nf.clone();
        }
        EnumMap enumMap = data.qualitativeUnitMap;
        EnumMap enumMap2 = data.relUnitPatternMap;
        String compileToStringMinMaxArguments = SimpleFormatterImpl.compileToStringMinMaxArguments(data.dateTimePattern, new StringBuilder(), 2, 2);
        PluralRules forLocale = PluralRules.forLocale(locale);
        if (capitalizationContext == DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE) {
            breakIterator = BreakIterator.getSentenceInstance(locale);
        }
        return new RelativeDateTimeFormatter(enumMap, enumMap2, compileToStringMinMaxArguments, forLocale, nf, style, capitalizationContext, breakIterator, locale);
    }

    public static RelativeDateTimeFormatter getInstance(Locale locale, NumberFormat nf) {
        return getInstance(ULocale.forLocale(locale), nf);
    }

    public String format(double quantity, Direction direction, RelativeUnit unit) {
        if (direction == Direction.LAST || direction == Direction.NEXT) {
            String result;
            int pastFutureIndex = direction == Direction.NEXT ? 1 : 0;
            synchronized (this.numberFormat) {
                result = SimpleFormatterImpl.formatCompiledPattern(getRelativeUnitPluralPattern(this.style, unit, pastFutureIndex, QuantityFormatter.selectPlural(Double.valueOf(quantity), this.numberFormat, this.pluralRules, new StringBuffer(), DontCareFieldPosition.INSTANCE)), new StringBuffer());
            }
            return adjustForContext(result);
        }
        throw new IllegalArgumentException("direction must be NEXT or LAST");
    }

    public String formatNumeric(double offset, RelativeDateTimeUnit unit) {
        RelativeUnit relunit = RelativeUnit.SECONDS;
        switch (-getandroid-icu-text-RelativeDateTimeFormatter$RelativeDateTimeUnitSwitchesValues()[unit.ordinal()]) {
            case 1:
                relunit = RelativeUnit.DAYS;
                break;
            case 3:
                relunit = RelativeUnit.HOURS;
                break;
            case 4:
                relunit = RelativeUnit.MINUTES;
                break;
            case 6:
                relunit = RelativeUnit.MONTHS;
                break;
            case 7:
                relunit = RelativeUnit.QUARTERS;
                break;
            case 9:
                break;
            case 14:
                relunit = RelativeUnit.WEEKS;
                break;
            case 15:
                relunit = RelativeUnit.YEARS;
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
        if (unit != AbsoluteUnit.NOW || direction == Direction.PLAIN) {
            String result;
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
            double offsetx100 = offset * 100.0d;
            switch (offsetx100 < 0.0d ? (int) (offsetx100 - 0.5d) : (int) (offsetx100 + 0.5d)) {
                case -200:
                    direction = Direction.LAST_2;
                    useNumeric = false;
                    break;
                case -100:
                    direction = Direction.LAST;
                    useNumeric = false;
                    break;
                case 0:
                    useNumeric = false;
                    break;
                case 100:
                    direction = Direction.NEXT;
                    useNumeric = false;
                    break;
                case 200:
                    direction = Direction.NEXT_2;
                    useNumeric = false;
                    break;
            }
        }
        AbsoluteUnit absunit = AbsoluteUnit.NOW;
        switch (-getandroid-icu-text-RelativeDateTimeFormatter$RelativeDateTimeUnitSwitchesValues()[unit.ordinal()]) {
            case 1:
                absunit = AbsoluteUnit.DAY;
                break;
            case 2:
                absunit = AbsoluteUnit.FRIDAY;
                break;
            case 5:
                absunit = AbsoluteUnit.MONDAY;
                break;
            case 6:
                absunit = AbsoluteUnit.MONTH;
                break;
            case 7:
                absunit = AbsoluteUnit.QUARTER;
                break;
            case 8:
                absunit = AbsoluteUnit.SATURDAY;
                break;
            case 9:
                if (direction != Direction.THIS) {
                    useNumeric = true;
                    break;
                }
                direction = Direction.PLAIN;
                break;
            case 10:
                absunit = AbsoluteUnit.SUNDAY;
                break;
            case 11:
                absunit = AbsoluteUnit.THURSDAY;
                break;
            case 12:
                absunit = AbsoluteUnit.TUESDAY;
                break;
            case 13:
                absunit = AbsoluteUnit.WEDNESDAY;
                break;
            case 14:
                absunit = AbsoluteUnit.WEEK;
                break;
            case 15:
                absunit = AbsoluteUnit.YEAR;
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

    private String getAbsoluteUnitString(Style style, AbsoluteUnit unit, Direction direction) {
        do {
            EnumMap<AbsoluteUnit, EnumMap<Direction, String>> unitMap = (EnumMap) this.qualitativeUnitMap.get(style);
            if (unitMap != null) {
                EnumMap<Direction, String> dirMap = (EnumMap) unitMap.get(unit);
                if (dirMap != null) {
                    String result = (String) dirMap.get(direction);
                    if (result != null) {
                        return result;
                    }
                }
            }
            style = fallbackCache[style.ordinal()];
        } while (style != null);
        return null;
    }

    public String combineDateAndTime(String relativeDateString, String timeString) {
        return SimpleFormatterImpl.formatCompiledPattern(this.combinedDateAndTime, timeString, relativeDateString);
    }

    public NumberFormat getNumberFormat() {
        NumberFormat numberFormat;
        synchronized (this.numberFormat) {
            numberFormat = (NumberFormat) this.numberFormat.clone();
        }
        return numberFormat;
    }

    public DisplayContext getCapitalizationContext() {
        return this.capitalizationContext;
    }

    public Style getFormatStyle() {
        return this.style;
    }

    private String adjustForContext(String originalFormattedString) {
        if (this.breakIterator == null || originalFormattedString.length() == 0 || (UCharacter.isLowerCase(UCharacter.codePointAt((CharSequence) originalFormattedString, 0)) ^ 1) != 0) {
            return originalFormattedString;
        }
        String toTitleCase;
        synchronized (this.breakIterator) {
            toTitleCase = UCharacter.toTitleCase(this.locale, originalFormattedString, this.breakIterator, 768);
        }
        return toTitleCase;
    }

    private RelativeDateTimeFormatter(EnumMap<Style, EnumMap<AbsoluteUnit, EnumMap<Direction, String>>> qualitativeUnitMap, EnumMap<Style, EnumMap<RelativeUnit, String[][]>> patternMap, String combinedDateAndTime, PluralRules pluralRules, NumberFormat numberFormat, Style style, DisplayContext capitalizationContext, BreakIterator breakIterator, ULocale locale) {
        this.qualitativeUnitMap = qualitativeUnitMap;
        this.patternMap = patternMap;
        this.combinedDateAndTime = combinedDateAndTime;
        this.pluralRules = pluralRules;
        this.numberFormat = numberFormat;
        this.style = style;
        if (capitalizationContext.type() != Type.CAPITALIZATION) {
            throw new IllegalArgumentException(capitalizationContext.toString());
        }
        this.capitalizationContext = capitalizationContext;
        this.breakIterator = breakIterator;
        this.locale = locale;
        this.dateFormatSymbols = new DateFormatSymbols(locale);
    }

    private String getRelativeUnitPluralPattern(Style style, RelativeUnit unit, int pastFutureIndex, StandardPlural pluralForm) {
        if (pluralForm != StandardPlural.OTHER) {
            String formatter = getRelativeUnitPattern(style, unit, pastFutureIndex, pluralForm);
            if (formatter != null) {
                return formatter;
            }
        }
        return getRelativeUnitPattern(style, unit, pastFutureIndex, StandardPlural.OTHER);
    }

    private String getRelativeUnitPattern(Style style, RelativeUnit unit, int pastFutureIndex, StandardPlural pluralForm) {
        int pluralIndex = pluralForm.ordinal();
        do {
            EnumMap<RelativeUnit, String[][]> unitMap = (EnumMap) this.patternMap.get(style);
            if (unitMap != null) {
                String[][] spfCompiledPatterns = (String[][]) unitMap.get(unit);
                if (!(spfCompiledPatterns == null || spfCompiledPatterns[pastFutureIndex][pluralIndex] == null)) {
                    return spfCompiledPatterns[pastFutureIndex][pluralIndex];
                }
            }
            style = fallbackCache[style.ordinal()];
        } while (style != null);
        return null;
    }

    private static Direction keyToDirection(Key key) {
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

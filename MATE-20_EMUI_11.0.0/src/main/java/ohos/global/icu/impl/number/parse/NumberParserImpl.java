package ohos.global.icu.impl.number.parse;

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import ohos.global.icu.impl.StringSegment;
import ohos.global.icu.impl.number.AffixPatternProvider;
import ohos.global.icu.impl.number.CurrencyPluralInfoAffixProvider;
import ohos.global.icu.impl.number.CustomSymbolCurrency;
import ohos.global.icu.impl.number.DecimalFormatProperties;
import ohos.global.icu.impl.number.Grouper;
import ohos.global.icu.impl.number.PatternStringParser;
import ohos.global.icu.impl.number.PropertiesAffixPatternProvider;
import ohos.global.icu.impl.number.RoundingUtils;
import ohos.global.icu.number.NumberFormatter;
import ohos.global.icu.number.Scale;
import ohos.global.icu.text.DecimalFormatSymbols;
import ohos.global.icu.util.Currency;
import ohos.global.icu.util.CurrencyAmount;
import ohos.global.icu.util.ULocale;

public class NumberParserImpl {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private boolean frozen;
    private final List<NumberParseMatcher> matchers = new ArrayList();
    private final int parseFlags;

    public static NumberParserImpl createSimpleParser(ULocale uLocale, String str, int i) {
        NumberParserImpl numberParserImpl = new NumberParserImpl(i);
        Currency instance = Currency.getInstance("USD");
        DecimalFormatSymbols instance2 = DecimalFormatSymbols.getInstance(uLocale);
        IgnorablesMatcher instance3 = IgnorablesMatcher.getInstance(i);
        AffixTokenMatcherFactory affixTokenMatcherFactory = new AffixTokenMatcherFactory();
        affixTokenMatcherFactory.currency = instance;
        affixTokenMatcherFactory.symbols = instance2;
        affixTokenMatcherFactory.ignorables = instance3;
        affixTokenMatcherFactory.locale = uLocale;
        affixTokenMatcherFactory.parseFlags = i;
        PatternStringParser.ParsedPatternInfo parseToPatternInfo = PatternStringParser.parseToPatternInfo(str);
        AffixMatcher.createMatchers(parseToPatternInfo, numberParserImpl, affixTokenMatcherFactory, instance3, i);
        Grouper withLocaleData = Grouper.forStrategy(NumberFormatter.GroupingStrategy.AUTO).withLocaleData(uLocale, parseToPatternInfo);
        numberParserImpl.addMatcher(instance3);
        numberParserImpl.addMatcher(DecimalMatcher.getInstance(instance2, withLocaleData, i));
        numberParserImpl.addMatcher(MinusSignMatcher.getInstance(instance2, false));
        numberParserImpl.addMatcher(PlusSignMatcher.getInstance(instance2, false));
        numberParserImpl.addMatcher(PercentMatcher.getInstance(instance2));
        numberParserImpl.addMatcher(PermilleMatcher.getInstance(instance2));
        numberParserImpl.addMatcher(NanMatcher.getInstance(instance2, i));
        numberParserImpl.addMatcher(InfinityMatcher.getInstance(instance2));
        numberParserImpl.addMatcher(PaddingMatcher.getInstance("@"));
        numberParserImpl.addMatcher(ScientificMatcher.getInstance(instance2, withLocaleData));
        numberParserImpl.addMatcher(CombinedCurrencyMatcher.getInstance(instance, instance2, i));
        numberParserImpl.addMatcher(new RequireNumberValidator());
        numberParserImpl.freeze();
        return numberParserImpl;
    }

    public static Number parseStatic(String str, ParsePosition parsePosition, DecimalFormatProperties decimalFormatProperties, DecimalFormatSymbols decimalFormatSymbols) {
        NumberParserImpl createParserFromProperties = createParserFromProperties(decimalFormatProperties, decimalFormatSymbols, false);
        ParsedNumber parsedNumber = new ParsedNumber();
        createParserFromProperties.parse(str, true, parsedNumber);
        if (parsedNumber.success()) {
            parsePosition.setIndex(parsedNumber.charEnd);
            return parsedNumber.getNumber();
        }
        parsePosition.setErrorIndex(parsedNumber.charEnd);
        return null;
    }

    public static CurrencyAmount parseStaticCurrency(String str, ParsePosition parsePosition, DecimalFormatProperties decimalFormatProperties, DecimalFormatSymbols decimalFormatSymbols) {
        NumberParserImpl createParserFromProperties = createParserFromProperties(decimalFormatProperties, decimalFormatSymbols, true);
        ParsedNumber parsedNumber = new ParsedNumber();
        createParserFromProperties.parse(str, true, parsedNumber);
        if (parsedNumber.success()) {
            parsePosition.setIndex(parsedNumber.charEnd);
            return new CurrencyAmount(parsedNumber.getNumber(), Currency.getInstance(parsedNumber.currencyCode));
        }
        parsePosition.setErrorIndex(parsedNumber.charEnd);
        return null;
    }

    public static NumberParserImpl createDefaultParserForLocale(ULocale uLocale) {
        return createParserFromProperties(PatternStringParser.parseToProperties("0"), DecimalFormatSymbols.getInstance(uLocale), false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0076  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0084  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00d8  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x0134  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x013e  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x014c  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x0167  */
    public static NumberParserImpl createParserFromProperties(DecimalFormatProperties decimalFormatProperties, DecimalFormatSymbols decimalFormatSymbols, boolean z) {
        AffixPatternProvider affixPatternProvider;
        int i;
        NumberParserImpl numberParserImpl;
        String padString;
        Scale scaleFromProperties;
        int i2;
        int i3;
        ULocale uLocale = decimalFormatSymbols.getULocale();
        if (decimalFormatProperties.getCurrencyPluralInfo() == null) {
            affixPatternProvider = new PropertiesAffixPatternProvider(decimalFormatProperties);
        } else {
            affixPatternProvider = new CurrencyPluralInfoAffixProvider(decimalFormatProperties.getCurrencyPluralInfo(), decimalFormatProperties);
        }
        Currency resolve = CustomSymbolCurrency.resolve(decimalFormatProperties.getCurrency(), uLocale, decimalFormatSymbols);
        DecimalFormatProperties.ParseMode parseMode = decimalFormatProperties.getParseMode();
        if (parseMode == null) {
            parseMode = DecimalFormatProperties.ParseMode.LENIENT;
        }
        Grouper forProperties = Grouper.forProperties(decimalFormatProperties);
        boolean z2 = true;
        int i4 = !decimalFormatProperties.getParseCaseSensitive() ? 1 : 0;
        if (decimalFormatProperties.getParseIntegerOnly()) {
            i4 |= 16;
        }
        if (decimalFormatProperties.getParseToBigDecimal()) {
            i4 |= 4096;
        }
        if (decimalFormatProperties.getSignAlwaysShown()) {
            i4 |= 1024;
        }
        if (parseMode == DecimalFormatProperties.ParseMode.JAVA_COMPATIBILITY) {
            i2 = i4 | 4 | 256 | 512;
            i3 = 65536;
        } else if (parseMode == DecimalFormatProperties.ParseMode.STRICT) {
            i2 = i4 | 8 | 4 | 256 | 512;
            i3 = 32768;
        } else {
            i = i4 | 128;
            if (forProperties.getPrimary() <= 0) {
                i |= 32;
            }
            if (z || affixPatternProvider.hasCurrencySign()) {
                i |= 2;
            }
            if (!z) {
                i |= 8192;
            }
            numberParserImpl = new NumberParserImpl(i);
            IgnorablesMatcher instance = IgnorablesMatcher.getInstance(i);
            AffixTokenMatcherFactory affixTokenMatcherFactory = new AffixTokenMatcherFactory();
            affixTokenMatcherFactory.currency = resolve;
            affixTokenMatcherFactory.symbols = decimalFormatSymbols;
            affixTokenMatcherFactory.ignorables = instance;
            affixTokenMatcherFactory.locale = uLocale;
            affixTokenMatcherFactory.parseFlags = i;
            AffixMatcher.createMatchers(affixPatternProvider, numberParserImpl, affixTokenMatcherFactory, instance, i);
            if (z || affixPatternProvider.hasCurrencySign()) {
                numberParserImpl.addMatcher(CombinedCurrencyMatcher.getInstance(resolve, decimalFormatSymbols, i));
            }
            if (parseMode == DecimalFormatProperties.ParseMode.LENIENT && affixPatternProvider.containsSymbolType(-3)) {
                numberParserImpl.addMatcher(PercentMatcher.getInstance(decimalFormatSymbols));
            }
            if (parseMode == DecimalFormatProperties.ParseMode.LENIENT && affixPatternProvider.containsSymbolType(-4)) {
                numberParserImpl.addMatcher(PermilleMatcher.getInstance(decimalFormatSymbols));
            }
            if (parseMode == DecimalFormatProperties.ParseMode.LENIENT) {
                numberParserImpl.addMatcher(PlusSignMatcher.getInstance(decimalFormatSymbols, false));
                numberParserImpl.addMatcher(MinusSignMatcher.getInstance(decimalFormatSymbols, false));
            }
            numberParserImpl.addMatcher(NanMatcher.getInstance(decimalFormatSymbols, i));
            numberParserImpl.addMatcher(InfinityMatcher.getInstance(decimalFormatSymbols));
            padString = decimalFormatProperties.getPadString();
            if (padString != null && !instance.getSet().contains(padString)) {
                numberParserImpl.addMatcher(PaddingMatcher.getInstance(padString));
            }
            numberParserImpl.addMatcher(instance);
            numberParserImpl.addMatcher(DecimalMatcher.getInstance(decimalFormatSymbols, forProperties, i));
            if (!decimalFormatProperties.getParseNoExponent() || decimalFormatProperties.getMinimumExponentDigits() > 0) {
                numberParserImpl.addMatcher(ScientificMatcher.getInstance(decimalFormatSymbols, forProperties));
            }
            numberParserImpl.addMatcher(new RequireNumberValidator());
            if (parseMode != DecimalFormatProperties.ParseMode.LENIENT) {
                numberParserImpl.addMatcher(new RequireAffixValidator());
            }
            if (z) {
                numberParserImpl.addMatcher(new RequireCurrencyValidator());
            }
            if (decimalFormatProperties.getDecimalPatternMatchRequired()) {
                if (!decimalFormatProperties.getDecimalSeparatorAlwaysShown() && decimalFormatProperties.getMaximumFractionDigits() == 0) {
                    z2 = false;
                }
                numberParserImpl.addMatcher(RequireDecimalSeparatorValidator.getInstance(z2));
            }
            scaleFromProperties = RoundingUtils.scaleFromProperties(decimalFormatProperties);
            if (scaleFromProperties != null) {
                numberParserImpl.addMatcher(new MultiplierParseHandler(scaleFromProperties));
            }
            numberParserImpl.freeze();
            return numberParserImpl;
        }
        i = i2 | i3;
        if (forProperties.getPrimary() <= 0) {
        }
        i |= 2;
        if (!z) {
        }
        numberParserImpl = new NumberParserImpl(i);
        IgnorablesMatcher instance2 = IgnorablesMatcher.getInstance(i);
        AffixTokenMatcherFactory affixTokenMatcherFactory2 = new AffixTokenMatcherFactory();
        affixTokenMatcherFactory2.currency = resolve;
        affixTokenMatcherFactory2.symbols = decimalFormatSymbols;
        affixTokenMatcherFactory2.ignorables = instance2;
        affixTokenMatcherFactory2.locale = uLocale;
        affixTokenMatcherFactory2.parseFlags = i;
        AffixMatcher.createMatchers(affixPatternProvider, numberParserImpl, affixTokenMatcherFactory2, instance2, i);
        numberParserImpl.addMatcher(CombinedCurrencyMatcher.getInstance(resolve, decimalFormatSymbols, i));
        numberParserImpl.addMatcher(PercentMatcher.getInstance(decimalFormatSymbols));
        numberParserImpl.addMatcher(PermilleMatcher.getInstance(decimalFormatSymbols));
        if (parseMode == DecimalFormatProperties.ParseMode.LENIENT) {
        }
        numberParserImpl.addMatcher(NanMatcher.getInstance(decimalFormatSymbols, i));
        numberParserImpl.addMatcher(InfinityMatcher.getInstance(decimalFormatSymbols));
        padString = decimalFormatProperties.getPadString();
        numberParserImpl.addMatcher(PaddingMatcher.getInstance(padString));
        numberParserImpl.addMatcher(instance2);
        numberParserImpl.addMatcher(DecimalMatcher.getInstance(decimalFormatSymbols, forProperties, i));
        numberParserImpl.addMatcher(ScientificMatcher.getInstance(decimalFormatSymbols, forProperties));
        numberParserImpl.addMatcher(new RequireNumberValidator());
        if (parseMode != DecimalFormatProperties.ParseMode.LENIENT) {
        }
        if (z) {
        }
        if (decimalFormatProperties.getDecimalPatternMatchRequired()) {
        }
        scaleFromProperties = RoundingUtils.scaleFromProperties(decimalFormatProperties);
        if (scaleFromProperties != null) {
        }
        numberParserImpl.freeze();
        return numberParserImpl;
    }

    public NumberParserImpl(int i) {
        this.parseFlags = i;
        this.frozen = false;
    }

    public void addMatcher(NumberParseMatcher numberParseMatcher) {
        this.matchers.add(numberParseMatcher);
    }

    public void addMatchers(Collection<? extends NumberParseMatcher> collection) {
        this.matchers.addAll(collection);
    }

    public void freeze() {
        this.frozen = true;
    }

    public int getParseFlags() {
        return this.parseFlags;
    }

    public void parse(String str, boolean z, ParsedNumber parsedNumber) {
        parse(str, 0, z, parsedNumber);
    }

    public void parse(String str, int i, boolean z, ParsedNumber parsedNumber) {
        StringSegment stringSegment = new StringSegment(str, (this.parseFlags & 1) != 0);
        stringSegment.adjustOffset(i);
        if (z) {
            parseGreedy(stringSegment, parsedNumber);
        } else if ((this.parseFlags & 16384) != 0) {
            parseLongestRecursive(stringSegment, parsedNumber, 1);
        } else {
            parseLongestRecursive(stringSegment, parsedNumber, -100);
        }
        for (NumberParseMatcher numberParseMatcher : this.matchers) {
            numberParseMatcher.postProcess(parsedNumber);
        }
        parsedNumber.postProcess();
    }

    private void parseGreedy(StringSegment stringSegment, ParsedNumber parsedNumber) {
        while (true) {
            int i = 0;
            while (true) {
                if (i < this.matchers.size() && stringSegment.length() != 0) {
                    NumberParseMatcher numberParseMatcher = this.matchers.get(i);
                    if (numberParseMatcher.smokeTest(stringSegment)) {
                        int offset = stringSegment.getOffset();
                        numberParseMatcher.match(stringSegment, parsedNumber);
                        if (stringSegment.getOffset() != offset) {
                        }
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    private void parseLongestRecursive(StringSegment stringSegment, ParsedNumber parsedNumber, int i) {
        if (!(stringSegment.length() == 0 || i == 0)) {
            ParsedNumber parsedNumber2 = new ParsedNumber();
            parsedNumber2.copyFrom(parsedNumber);
            ParsedNumber parsedNumber3 = new ParsedNumber();
            int offset = stringSegment.getOffset();
            for (int i2 = 0; i2 < this.matchers.size(); i2++) {
                NumberParseMatcher numberParseMatcher = this.matchers.get(i2);
                if (numberParseMatcher.smokeTest(stringSegment)) {
                    int i3 = 0;
                    while (i3 < stringSegment.length()) {
                        i3 += Character.charCount(stringSegment.codePointAt(i3));
                        parsedNumber3.copyFrom(parsedNumber2);
                        stringSegment.setLength(i3);
                        boolean match = numberParseMatcher.match(stringSegment, parsedNumber3);
                        stringSegment.resetLength();
                        if (stringSegment.getOffset() - offset == i3) {
                            parseLongestRecursive(stringSegment, parsedNumber3, i + 1);
                            if (parsedNumber3.isBetterThan(parsedNumber)) {
                                parsedNumber.copyFrom(parsedNumber3);
                            }
                        }
                        stringSegment.setOffset(offset);
                        if (!match) {
                            break;
                        }
                    }
                }
            }
        }
    }

    public String toString() {
        return "<NumberParserImpl matchers=" + this.matchers.toString() + ">";
    }
}

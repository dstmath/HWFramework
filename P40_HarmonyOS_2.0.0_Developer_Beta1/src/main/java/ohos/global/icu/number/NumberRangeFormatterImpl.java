package ohos.global.icu.number;

import java.util.MissingResourceException;
import ohos.global.icu.impl.FormattedStringBuilder;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.PatternProps;
import ohos.global.icu.impl.SimpleFormatterImpl;
import ohos.global.icu.impl.UResource;
import ohos.global.icu.impl.number.DecimalQuantity;
import ohos.global.icu.impl.number.MacroProps;
import ohos.global.icu.impl.number.MicroProps;
import ohos.global.icu.impl.number.Modifier;
import ohos.global.icu.impl.number.SimpleModifier;
import ohos.global.icu.impl.number.range.PrefixInfixSuffixLengthHelper;
import ohos.global.icu.impl.number.range.RangeMacroProps;
import ohos.global.icu.impl.number.range.StandardPluralRanges;
import ohos.global.icu.number.NumberRangeFormatter;
import ohos.global.icu.text.NumberFormat;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

class NumberRangeFormatterImpl {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    SimpleModifier fApproximatelyModifier;
    final NumberRangeFormatter.RangeCollapse fCollapse;
    final NumberRangeFormatter.RangeIdentityFallback fIdentityFallback;
    final StandardPluralRanges fPluralRanges;
    String fRangePattern;
    final boolean fSameFormatters;
    final NumberFormatterImpl formatterImpl1;
    final NumberFormatterImpl formatterImpl2;

    /* access modifiers changed from: package-private */
    public int identity2d(NumberRangeFormatter.RangeIdentityFallback rangeIdentityFallback, NumberRangeFormatter.RangeIdentityResult rangeIdentityResult) {
        return rangeIdentityFallback.ordinal() | (rangeIdentityResult.ordinal() << 4);
    }

    /* access modifiers changed from: private */
    public static final class NumberRangeDataSink extends UResource.Sink {
        String approximatelyPattern;
        String rangePattern;
        StringBuilder sb;

        NumberRangeDataSink(StringBuilder sb2) {
            this.sb = sb2;
        }

        @Override // ohos.global.icu.impl.UResource.Sink
        public void put(UResource.Key key, UResource.Value value, boolean z) {
            UResource.Table table = value.getTable();
            for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                if (key.contentEquals("range") && !hasRangeData()) {
                    this.rangePattern = SimpleFormatterImpl.compileToStringMinMaxArguments(value.getString(), this.sb, 2, 2);
                }
                if (key.contentEquals("approximately") && !hasApproxData()) {
                    this.approximatelyPattern = SimpleFormatterImpl.compileToStringMinMaxArguments(value.getString(), this.sb, 1, 1);
                }
            }
        }

        private boolean hasRangeData() {
            return this.rangePattern != null;
        }

        private boolean hasApproxData() {
            return this.approximatelyPattern != null;
        }

        public boolean isComplete() {
            return hasRangeData() && hasApproxData();
        }

        public void fillInDefaults() {
            if (!hasRangeData()) {
                this.rangePattern = SimpleFormatterImpl.compileToStringMinMaxArguments("{0}–{1}", this.sb, 2, 2);
            }
            if (!hasApproxData()) {
                this.approximatelyPattern = SimpleFormatterImpl.compileToStringMinMaxArguments("~{0}", this.sb, 1, 1);
            }
        }
    }

    private static void getNumberRangeData(ULocale uLocale, String str, NumberRangeFormatterImpl numberRangeFormatterImpl) {
        StringBuilder sb = new StringBuilder();
        NumberRangeDataSink numberRangeDataSink = new NumberRangeDataSink(sb);
        ICUResourceBundle bundleInstance = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, uLocale);
        sb.append("NumberElements/");
        sb.append(str);
        sb.append("/miscPatterns");
        try {
            bundleInstance.getAllItemsWithFallback(sb.toString(), numberRangeDataSink);
        } catch (MissingResourceException unused) {
        }
        if (!numberRangeDataSink.isComplete()) {
            bundleInstance.getAllItemsWithFallback("NumberElements/latn/miscPatterns", numberRangeDataSink);
        }
        numberRangeDataSink.fillInDefaults();
        numberRangeFormatterImpl.fRangePattern = numberRangeDataSink.rangePattern;
        numberRangeFormatterImpl.fApproximatelyModifier = new SimpleModifier(numberRangeDataSink.approximatelyPattern, null, false);
    }

    public NumberRangeFormatterImpl(RangeMacroProps rangeMacroProps) {
        MacroProps macroProps;
        MacroProps macroProps2;
        NumberRangeFormatter.RangeIdentityFallback rangeIdentityFallback;
        if (rangeMacroProps.formatter1 != null) {
            macroProps = rangeMacroProps.formatter1.resolve();
        } else {
            macroProps = NumberFormatter.withLocale(rangeMacroProps.loc).resolve();
        }
        this.formatterImpl1 = new NumberFormatterImpl(macroProps);
        if (rangeMacroProps.formatter2 != null) {
            macroProps2 = rangeMacroProps.formatter2.resolve();
        } else {
            macroProps2 = NumberFormatter.withLocale(rangeMacroProps.loc).resolve();
        }
        this.formatterImpl2 = new NumberFormatterImpl(macroProps2);
        this.fSameFormatters = rangeMacroProps.sameFormatters != 0;
        this.fCollapse = rangeMacroProps.collapse != null ? rangeMacroProps.collapse : NumberRangeFormatter.RangeCollapse.AUTO;
        if (rangeMacroProps.identityFallback != null) {
            rangeIdentityFallback = rangeMacroProps.identityFallback;
        } else {
            rangeIdentityFallback = NumberRangeFormatter.RangeIdentityFallback.APPROXIMATELY;
        }
        this.fIdentityFallback = rangeIdentityFallback;
        String str = this.formatterImpl1.getRawMicroProps().nsName;
        if (str == null || !str.equals(this.formatterImpl2.getRawMicroProps().nsName)) {
            throw new IllegalArgumentException("Both formatters must have same numbering system");
        }
        getNumberRangeData(rangeMacroProps.loc, str, this);
        this.fPluralRanges = new StandardPluralRanges(rangeMacroProps.loc);
    }

    public FormattedNumberRange format(DecimalQuantity decimalQuantity, DecimalQuantity decimalQuantity2, boolean z) {
        MicroProps microProps;
        NumberRangeFormatter.RangeIdentityResult rangeIdentityResult;
        FormattedStringBuilder formattedStringBuilder = new FormattedStringBuilder();
        MicroProps preProcess = this.formatterImpl1.preProcess(decimalQuantity);
        if (this.fSameFormatters) {
            microProps = this.formatterImpl1.preProcess(decimalQuantity2);
        } else {
            microProps = this.formatterImpl2.preProcess(decimalQuantity2);
        }
        if (!preProcess.modInner.semanticallyEquivalent(microProps.modInner) || !preProcess.modMiddle.semanticallyEquivalent(microProps.modMiddle) || !preProcess.modOuter.semanticallyEquivalent(microProps.modOuter)) {
            formatRange(decimalQuantity, decimalQuantity2, formattedStringBuilder, preProcess, microProps);
            return new FormattedNumberRange(formattedStringBuilder, decimalQuantity, decimalQuantity2, NumberRangeFormatter.RangeIdentityResult.NOT_EQUAL);
        }
        if (z) {
            rangeIdentityResult = NumberRangeFormatter.RangeIdentityResult.EQUAL_BEFORE_ROUNDING;
        } else if (decimalQuantity.equals(decimalQuantity2)) {
            rangeIdentityResult = NumberRangeFormatter.RangeIdentityResult.EQUAL_AFTER_ROUNDING;
        } else {
            rangeIdentityResult = NumberRangeFormatter.RangeIdentityResult.NOT_EQUAL;
        }
        int identity2d = identity2d(this.fIdentityFallback, rangeIdentityResult);
        if (!(identity2d == 0 || identity2d == 1)) {
            if (identity2d != 2) {
                if (identity2d != 3) {
                    switch (identity2d) {
                        case 16:
                            break;
                        case 17:
                        case 18:
                            break;
                        default:
                            switch (identity2d) {
                            }
                        case 19:
                            formatRange(decimalQuantity, decimalQuantity2, formattedStringBuilder, preProcess, microProps);
                            break;
                    }
                    return new FormattedNumberRange(formattedStringBuilder, decimalQuantity, decimalQuantity2, rangeIdentityResult);
                }
                formatRange(decimalQuantity, decimalQuantity2, formattedStringBuilder, preProcess, microProps);
                return new FormattedNumberRange(formattedStringBuilder, decimalQuantity, decimalQuantity2, rangeIdentityResult);
            }
            formatApproximately(decimalQuantity, decimalQuantity2, formattedStringBuilder, preProcess, microProps);
            return new FormattedNumberRange(formattedStringBuilder, decimalQuantity, decimalQuantity2, rangeIdentityResult);
        }
        formatSingleValue(decimalQuantity, decimalQuantity2, formattedStringBuilder, preProcess, microProps);
        return new FormattedNumberRange(formattedStringBuilder, decimalQuantity, decimalQuantity2, rangeIdentityResult);
    }

    private void formatSingleValue(DecimalQuantity decimalQuantity, DecimalQuantity decimalQuantity2, FormattedStringBuilder formattedStringBuilder, MicroProps microProps, MicroProps microProps2) {
        if (this.fSameFormatters) {
            NumberFormatterImpl.writeAffixes(microProps, formattedStringBuilder, 0, NumberFormatterImpl.writeNumber(microProps, decimalQuantity, formattedStringBuilder, 0));
        } else {
            formatRange(decimalQuantity, decimalQuantity2, formattedStringBuilder, microProps, microProps2);
        }
    }

    private void formatApproximately(DecimalQuantity decimalQuantity, DecimalQuantity decimalQuantity2, FormattedStringBuilder formattedStringBuilder, MicroProps microProps, MicroProps microProps2) {
        if (this.fSameFormatters) {
            int writeNumber = NumberFormatterImpl.writeNumber(microProps, decimalQuantity, formattedStringBuilder, 0);
            int apply = writeNumber + microProps.modInner.apply(formattedStringBuilder, 0, writeNumber);
            int apply2 = apply + microProps.modMiddle.apply(formattedStringBuilder, 0, apply);
            microProps.modOuter.apply(formattedStringBuilder, 0, apply2 + this.fApproximatelyModifier.apply(formattedStringBuilder, 0, apply2));
            return;
        }
        formatRange(decimalQuantity, decimalQuantity2, formattedStringBuilder, microProps, microProps2);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.global.icu.number.NumberRangeFormatterImpl$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$number$NumberRangeFormatter$RangeCollapse = new int[NumberRangeFormatter.RangeCollapse.values().length];

        static {
            try {
                $SwitchMap$ohos$global$icu$number$NumberRangeFormatter$RangeCollapse[NumberRangeFormatter.RangeCollapse.ALL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberRangeFormatter$RangeCollapse[NumberRangeFormatter.RangeCollapse.AUTO.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberRangeFormatter$RangeCollapse[NumberRangeFormatter.RangeCollapse.UNIT.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0074  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0081  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00ad  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00c9  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00f2  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x010c  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0134  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x014e  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0176  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0190  */
    private void formatRange(DecimalQuantity decimalQuantity, DecimalQuantity decimalQuantity2, FormattedStringBuilder formattedStringBuilder, MicroProps microProps, MicroProps microProps2) {
        boolean z;
        boolean z2;
        boolean z3;
        PrefixInfixSuffixLengthHelper prefixInfixSuffixLengthHelper;
        int i = AnonymousClass1.$SwitchMap$ohos$global$icu$number$NumberRangeFormatter$RangeCollapse[this.fCollapse.ordinal()];
        boolean z4 = false;
        if (i == 1 || i == 2 || i == 3) {
            z3 = microProps.modOuter.semanticallyEquivalent(microProps2.modOuter);
            if (!z3) {
                z2 = false;
            } else {
                z2 = microProps.modMiddle.semanticallyEquivalent(microProps2.modMiddle);
                if (z2) {
                    Modifier modifier = microProps.modMiddle;
                    if (this.fCollapse != NumberRangeFormatter.RangeCollapse.UNIT ? !(this.fCollapse != NumberRangeFormatter.RangeCollapse.AUTO || modifier.getCodePointCount() > 1) : !(modifier.containsField(NumberFormat.Field.CURRENCY) || modifier.containsField(NumberFormat.Field.PERCENT))) {
                        z2 = false;
                    }
                    if (z2 && this.fCollapse == NumberRangeFormatter.RangeCollapse.ALL) {
                        z = microProps.modInner.semanticallyEquivalent(microProps2.modInner);
                        prefixInfixSuffixLengthHelper = new PrefixInfixSuffixLengthHelper();
                        SimpleModifier.formatTwoArgPattern(this.fRangePattern, formattedStringBuilder, 0, prefixInfixSuffixLengthHelper, null);
                        boolean z5 = z && microProps.modInner.getCodePointCount() > 0;
                        boolean z6 = z2 && microProps.modMiddle.getCodePointCount() > 0;
                        if (!z3 && microProps.modOuter.getCodePointCount() > 0) {
                            z4 = true;
                        }
                        if (z5 || z6 || z4) {
                            if (!PatternProps.isWhiteSpace(formattedStringBuilder.charAt(prefixInfixSuffixLengthHelper.index1()))) {
                                prefixInfixSuffixLengthHelper.lengthInfix += formattedStringBuilder.insertCodePoint(prefixInfixSuffixLengthHelper.index1(), 32, null);
                            }
                            if (!PatternProps.isWhiteSpace(formattedStringBuilder.charAt(prefixInfixSuffixLengthHelper.index2() - 1))) {
                                prefixInfixSuffixLengthHelper.lengthInfix += formattedStringBuilder.insertCodePoint(prefixInfixSuffixLengthHelper.index2(), 32, null);
                            }
                        }
                        prefixInfixSuffixLengthHelper.length1 += NumberFormatterImpl.writeNumber(microProps, decimalQuantity, formattedStringBuilder, prefixInfixSuffixLengthHelper.index0());
                        prefixInfixSuffixLengthHelper.length2 += NumberFormatterImpl.writeNumber(microProps2, decimalQuantity2, formattedStringBuilder, prefixInfixSuffixLengthHelper.index2());
                        if (z) {
                            prefixInfixSuffixLengthHelper.lengthInfix += resolveModifierPlurals(microProps.modInner, microProps2.modInner).apply(formattedStringBuilder, prefixInfixSuffixLengthHelper.index0(), prefixInfixSuffixLengthHelper.index3());
                        } else {
                            prefixInfixSuffixLengthHelper.length1 += microProps.modInner.apply(formattedStringBuilder, prefixInfixSuffixLengthHelper.index0(), prefixInfixSuffixLengthHelper.index1());
                            prefixInfixSuffixLengthHelper.length2 += microProps2.modInner.apply(formattedStringBuilder, prefixInfixSuffixLengthHelper.index2(), prefixInfixSuffixLengthHelper.index3());
                        }
                        if (z2) {
                            prefixInfixSuffixLengthHelper.lengthInfix += resolveModifierPlurals(microProps.modMiddle, microProps2.modMiddle).apply(formattedStringBuilder, prefixInfixSuffixLengthHelper.index0(), prefixInfixSuffixLengthHelper.index3());
                        } else {
                            prefixInfixSuffixLengthHelper.length1 += microProps.modMiddle.apply(formattedStringBuilder, prefixInfixSuffixLengthHelper.index0(), prefixInfixSuffixLengthHelper.index1());
                            prefixInfixSuffixLengthHelper.length2 += microProps2.modMiddle.apply(formattedStringBuilder, prefixInfixSuffixLengthHelper.index2(), prefixInfixSuffixLengthHelper.index3());
                        }
                        if (z3) {
                            prefixInfixSuffixLengthHelper.lengthInfix += resolveModifierPlurals(microProps.modOuter, microProps2.modOuter).apply(formattedStringBuilder, prefixInfixSuffixLengthHelper.index0(), prefixInfixSuffixLengthHelper.index3());
                            return;
                        }
                        prefixInfixSuffixLengthHelper.length1 += microProps.modOuter.apply(formattedStringBuilder, prefixInfixSuffixLengthHelper.index0(), prefixInfixSuffixLengthHelper.index1());
                        prefixInfixSuffixLengthHelper.length2 += microProps2.modOuter.apply(formattedStringBuilder, prefixInfixSuffixLengthHelper.index2(), prefixInfixSuffixLengthHelper.index3());
                        return;
                    }
                }
                z = false;
                prefixInfixSuffixLengthHelper = new PrefixInfixSuffixLengthHelper();
                SimpleModifier.formatTwoArgPattern(this.fRangePattern, formattedStringBuilder, 0, prefixInfixSuffixLengthHelper, null);
                if (z) {
                }
                if (z2) {
                }
                z4 = true;
                if (!PatternProps.isWhiteSpace(formattedStringBuilder.charAt(prefixInfixSuffixLengthHelper.index1()))) {
                }
                if (!PatternProps.isWhiteSpace(formattedStringBuilder.charAt(prefixInfixSuffixLengthHelper.index2() - 1))) {
                }
                prefixInfixSuffixLengthHelper.length1 += NumberFormatterImpl.writeNumber(microProps, decimalQuantity, formattedStringBuilder, prefixInfixSuffixLengthHelper.index0());
                prefixInfixSuffixLengthHelper.length2 += NumberFormatterImpl.writeNumber(microProps2, decimalQuantity2, formattedStringBuilder, prefixInfixSuffixLengthHelper.index2());
                if (z) {
                }
                if (z2) {
                }
                if (z3) {
                }
            }
        } else {
            z3 = false;
            z2 = false;
        }
        z = z2;
        prefixInfixSuffixLengthHelper = new PrefixInfixSuffixLengthHelper();
        SimpleModifier.formatTwoArgPattern(this.fRangePattern, formattedStringBuilder, 0, prefixInfixSuffixLengthHelper, null);
        if (z) {
        }
        if (z2) {
        }
        z4 = true;
        if (!PatternProps.isWhiteSpace(formattedStringBuilder.charAt(prefixInfixSuffixLengthHelper.index1()))) {
        }
        if (!PatternProps.isWhiteSpace(formattedStringBuilder.charAt(prefixInfixSuffixLengthHelper.index2() - 1))) {
        }
        prefixInfixSuffixLengthHelper.length1 += NumberFormatterImpl.writeNumber(microProps, decimalQuantity, formattedStringBuilder, prefixInfixSuffixLengthHelper.index0());
        prefixInfixSuffixLengthHelper.length2 += NumberFormatterImpl.writeNumber(microProps2, decimalQuantity2, formattedStringBuilder, prefixInfixSuffixLengthHelper.index2());
        if (z) {
        }
        if (z2) {
        }
        if (z3) {
        }
    }

    /* access modifiers changed from: package-private */
    public Modifier resolveModifierPlurals(Modifier modifier, Modifier modifier2) {
        Modifier.Parameters parameters;
        Modifier.Parameters parameters2 = modifier.getParameters();
        if (parameters2 == null || (parameters = modifier2.getParameters()) == null) {
            return modifier;
        }
        return parameters2.obj.getModifier(parameters2.signum, this.fPluralRanges.resolve(parameters2.plural, parameters.plural));
    }
}

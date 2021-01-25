package ohos.global.icu.impl.number;

import java.util.EnumMap;
import java.util.Map;
import java.util.MissingResourceException;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.global.icu.impl.CurrencyData;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.SimpleFormatterImpl;
import ohos.global.icu.impl.StandardPlural;
import ohos.global.icu.impl.UResource;
import ohos.global.icu.impl.number.Modifier;
import ohos.global.icu.number.NumberFormatter;
import ohos.global.icu.text.NumberFormat;
import ohos.global.icu.text.PluralRules;
import ohos.global.icu.util.Currency;
import ohos.global.icu.util.ICUException;
import ohos.global.icu.util.MeasureUnit;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

public class LongNameHandler implements MicroPropsGenerator, ModifierStore {
    private static final int ARRAY_LENGTH = (StandardPlural.COUNT + 2);
    private static final int DNAM_INDEX = StandardPlural.COUNT;
    private static final int PER_INDEX = (StandardPlural.COUNT + 1);
    private final Map<StandardPlural, SimpleModifier> modifiers;
    private final MicroPropsGenerator parent;
    private final PluralRules rules;

    /* access modifiers changed from: private */
    public static int getIndex(String str) {
        if (str.equals("dnam")) {
            return DNAM_INDEX;
        }
        if (str.equals("per")) {
            return PER_INDEX;
        }
        return StandardPlural.fromString(str).ordinal();
    }

    private static String getWithPlural(String[] strArr, StandardPlural standardPlural) {
        String str = strArr[standardPlural.ordinal()];
        if (str == null) {
            str = strArr[StandardPlural.OTHER.ordinal()];
        }
        if (str != null) {
            return str;
        }
        throw new ICUException("Could not find data in 'other' plural variant");
    }

    /* access modifiers changed from: private */
    public static final class PluralTableSink extends UResource.Sink {
        String[] outArray;

        public PluralTableSink(String[] strArr) {
            this.outArray = strArr;
        }

        @Override // ohos.global.icu.impl.UResource.Sink
        public void put(UResource.Key key, UResource.Value value, boolean z) {
            UResource.Table table = value.getTable();
            for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                int index = LongNameHandler.getIndex(key.toString());
                if (this.outArray[index] == null) {
                    this.outArray[index] = value.getString();
                }
            }
        }
    }

    private static void getMeasureData(ULocale uLocale, MeasureUnit measureUnit, NumberFormatter.UnitWidth unitWidth, String[] strArr) {
        PluralTableSink pluralTableSink = new PluralTableSink(strArr);
        ICUResourceBundle bundleInstance = UResourceBundle.getBundleInstance(ICUData.ICU_UNIT_BASE_NAME, uLocale);
        StringBuilder sb = new StringBuilder();
        sb.append("units");
        if (unitWidth == NumberFormatter.UnitWidth.NARROW) {
            sb.append("Narrow");
        } else if (unitWidth == NumberFormatter.UnitWidth.SHORT) {
            sb.append("Short");
        }
        sb.append(PsuedoNames.PSEUDONAME_ROOT);
        sb.append(measureUnit.getType());
        sb.append(PsuedoNames.PSEUDONAME_ROOT);
        if (measureUnit.getSubtype().endsWith("-person")) {
            sb.append((CharSequence) measureUnit.getSubtype(), 0, measureUnit.getSubtype().length() - 7);
        } else {
            sb.append(measureUnit.getSubtype());
        }
        try {
            bundleInstance.getAllItemsWithFallback(sb.toString(), pluralTableSink);
        } catch (MissingResourceException e) {
            throw new IllegalArgumentException("No data for unit " + measureUnit + ", width " + unitWidth, e);
        }
    }

    private static void getCurrencyLongNameData(ULocale uLocale, Currency currency, String[] strArr) {
        for (Map.Entry<String, String> entry : CurrencyData.provider.getInstance(uLocale, true).getUnitPatterns().entrySet()) {
            String key = entry.getKey();
            strArr[getIndex(key)] = entry.getValue().replace("{1}", currency.getName(uLocale, 2, key, (boolean[]) null));
        }
    }

    private static String getPerUnitFormat(ULocale uLocale, NumberFormatter.UnitWidth unitWidth) {
        ICUResourceBundle bundleInstance = UResourceBundle.getBundleInstance(ICUData.ICU_UNIT_BASE_NAME, uLocale);
        StringBuilder sb = new StringBuilder();
        sb.append("units");
        if (unitWidth == NumberFormatter.UnitWidth.NARROW) {
            sb.append("Narrow");
        } else if (unitWidth == NumberFormatter.UnitWidth.SHORT) {
            sb.append("Short");
        }
        sb.append("/compound/per");
        try {
            return bundleInstance.getStringWithFallback(sb.toString());
        } catch (MissingResourceException unused) {
            throw new IllegalArgumentException("Could not find x-per-y format for " + uLocale + ", width " + unitWidth);
        }
    }

    private LongNameHandler(Map<StandardPlural, SimpleModifier> map, PluralRules pluralRules, MicroPropsGenerator microPropsGenerator) {
        this.modifiers = map;
        this.rules = pluralRules;
        this.parent = microPropsGenerator;
    }

    public static String getUnitDisplayName(ULocale uLocale, MeasureUnit measureUnit, NumberFormatter.UnitWidth unitWidth) {
        String[] strArr = new String[ARRAY_LENGTH];
        getMeasureData(uLocale, measureUnit, unitWidth, strArr);
        return strArr[DNAM_INDEX];
    }

    public static LongNameHandler forCurrencyLongNames(ULocale uLocale, Currency currency, PluralRules pluralRules, MicroPropsGenerator microPropsGenerator) {
        String[] strArr = new String[ARRAY_LENGTH];
        getCurrencyLongNameData(uLocale, currency, strArr);
        LongNameHandler longNameHandler = new LongNameHandler(new EnumMap(StandardPlural.class), pluralRules, microPropsGenerator);
        longNameHandler.simpleFormatsToModifiers(strArr, NumberFormat.Field.CURRENCY);
        return longNameHandler;
    }

    public static LongNameHandler forMeasureUnit(ULocale uLocale, MeasureUnit measureUnit, MeasureUnit measureUnit2, NumberFormatter.UnitWidth unitWidth, PluralRules pluralRules, MicroPropsGenerator microPropsGenerator) {
        if (measureUnit2 != null) {
            MeasureUnit resolveUnitPerUnit = MeasureUnit.resolveUnitPerUnit(measureUnit, measureUnit2);
            if (resolveUnitPerUnit == null) {
                return forCompoundUnit(uLocale, measureUnit, measureUnit2, unitWidth, pluralRules, microPropsGenerator);
            }
            measureUnit = resolveUnitPerUnit;
        }
        String[] strArr = new String[ARRAY_LENGTH];
        getMeasureData(uLocale, measureUnit, unitWidth, strArr);
        LongNameHandler longNameHandler = new LongNameHandler(new EnumMap(StandardPlural.class), pluralRules, microPropsGenerator);
        longNameHandler.simpleFormatsToModifiers(strArr, NumberFormat.Field.MEASURE_UNIT);
        return longNameHandler;
    }

    private static LongNameHandler forCompoundUnit(ULocale uLocale, MeasureUnit measureUnit, MeasureUnit measureUnit2, NumberFormatter.UnitWidth unitWidth, PluralRules pluralRules, MicroPropsGenerator microPropsGenerator) {
        String str;
        String[] strArr = new String[ARRAY_LENGTH];
        getMeasureData(uLocale, measureUnit, unitWidth, strArr);
        String[] strArr2 = new String[ARRAY_LENGTH];
        getMeasureData(uLocale, measureUnit2, unitWidth, strArr2);
        int i = PER_INDEX;
        if (strArr2[i] != null) {
            str = strArr2[i];
        } else {
            String perUnitFormat = getPerUnitFormat(uLocale, unitWidth);
            StringBuilder sb = new StringBuilder();
            str = SimpleFormatterImpl.formatCompiledPattern(SimpleFormatterImpl.compileToStringMinMaxArguments(perUnitFormat, sb, 2, 2), "{0}", SimpleFormatterImpl.getTextWithNoArguments(SimpleFormatterImpl.compileToStringMinMaxArguments(getWithPlural(strArr2, StandardPlural.ONE), sb, 1, 1)).trim());
        }
        LongNameHandler longNameHandler = new LongNameHandler(new EnumMap(StandardPlural.class), pluralRules, microPropsGenerator);
        longNameHandler.multiSimpleFormatsToModifiers(strArr, str, NumberFormat.Field.MEASURE_UNIT);
        return longNameHandler;
    }

    private void simpleFormatsToModifiers(String[] strArr, NumberFormat.Field field) {
        StringBuilder sb = new StringBuilder();
        for (StandardPlural standardPlural : StandardPlural.VALUES) {
            String compileToStringMinMaxArguments = SimpleFormatterImpl.compileToStringMinMaxArguments(getWithPlural(strArr, standardPlural), sb, 0, 1);
            Modifier.Parameters parameters = new Modifier.Parameters();
            parameters.obj = this;
            parameters.signum = 0;
            parameters.plural = standardPlural;
            this.modifiers.put(standardPlural, new SimpleModifier(compileToStringMinMaxArguments, field, false, parameters));
        }
    }

    private void multiSimpleFormatsToModifiers(String[] strArr, String str, NumberFormat.Field field) {
        StringBuilder sb = new StringBuilder();
        String compileToStringMinMaxArguments = SimpleFormatterImpl.compileToStringMinMaxArguments(str, sb, 1, 1);
        for (StandardPlural standardPlural : StandardPlural.VALUES) {
            String compileToStringMinMaxArguments2 = SimpleFormatterImpl.compileToStringMinMaxArguments(SimpleFormatterImpl.formatCompiledPattern(compileToStringMinMaxArguments, getWithPlural(strArr, standardPlural)), sb, 0, 1);
            Modifier.Parameters parameters = new Modifier.Parameters();
            parameters.obj = this;
            parameters.signum = 0;
            parameters.plural = standardPlural;
            this.modifiers.put(standardPlural, new SimpleModifier(compileToStringMinMaxArguments2, field, false, parameters));
        }
    }

    @Override // ohos.global.icu.impl.number.MicroPropsGenerator
    public MicroProps processQuantity(DecimalQuantity decimalQuantity) {
        MicroProps processQuantity = this.parent.processQuantity(decimalQuantity);
        processQuantity.modOuter = this.modifiers.get(RoundingUtils.getPluralSafe(processQuantity.rounder, this.rules, decimalQuantity));
        return processQuantity;
    }

    @Override // ohos.global.icu.impl.number.ModifierStore
    public Modifier getModifier(int i, StandardPlural standardPlural) {
        return this.modifiers.get(standardPlural);
    }
}

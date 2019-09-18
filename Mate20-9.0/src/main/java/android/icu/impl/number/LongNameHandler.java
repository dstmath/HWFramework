package android.icu.impl.number;

import android.icu.impl.CurrencyData;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleFormatterImpl;
import android.icu.impl.StandardPlural;
import android.icu.impl.UResource;
import android.icu.number.NumberFormatter;
import android.icu.text.NumberFormat;
import android.icu.text.PluralRules;
import android.icu.util.Currency;
import android.icu.util.ICUException;
import android.icu.util.MeasureUnit;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.util.EnumMap;
import java.util.Map;

public class LongNameHandler implements MicroPropsGenerator {
    private final Map<StandardPlural, SimpleModifier> modifiers;
    private final MicroPropsGenerator parent;
    private final PluralRules rules;

    private static final class PluralTableSink extends UResource.Sink {
        Map<StandardPlural, String> output;

        public PluralTableSink(Map<StandardPlural, String> output2) {
            this.output = output2;
        }

        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table pluralsTable = value.getTable();
            for (int i = 0; pluralsTable.getKeyAndValue(i, key, value); i++) {
                if (!key.contentEquals("dnam") && !key.contentEquals("per")) {
                    StandardPlural plural = StandardPlural.fromString(key);
                    if (!this.output.containsKey(plural)) {
                        this.output.put(plural, value.getString());
                    }
                }
            }
        }
    }

    private static void getMeasureData(ULocale locale, MeasureUnit unit, NumberFormatter.UnitWidth width, Map<StandardPlural, String> output) {
        PluralTableSink sink = new PluralTableSink(output);
        ICUResourceBundle resource = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_UNIT_BASE_NAME, locale);
        StringBuilder key = new StringBuilder();
        key.append("units");
        if (width == NumberFormatter.UnitWidth.NARROW) {
            key.append("Narrow");
        } else if (width == NumberFormatter.UnitWidth.SHORT) {
            key.append("Short");
        }
        key.append("/");
        key.append(unit.getType());
        key.append("/");
        key.append(unit.getSubtype());
        resource.getAllItemsWithFallback(key.toString(), sink);
    }

    private static void getCurrencyLongNameData(ULocale locale, Currency currency, Map<StandardPlural, String> output) {
        for (Map.Entry<String, String> e : CurrencyData.provider.getInstance(locale, true).getUnitPatterns().entrySet()) {
            output.put(StandardPlural.fromString(e.getKey()), e.getValue().replace("{1}", currency.getName(locale, 2, e.getKey(), (boolean[]) null)));
        }
    }

    private LongNameHandler(Map<StandardPlural, SimpleModifier> modifiers2, PluralRules rules2, MicroPropsGenerator parent2) {
        this.modifiers = modifiers2;
        this.rules = rules2;
        this.parent = parent2;
    }

    public static LongNameHandler forCurrencyLongNames(ULocale locale, Currency currency, PluralRules rules2, MicroPropsGenerator parent2) {
        Map<StandardPlural, String> simpleFormats = new EnumMap<>(StandardPlural.class);
        getCurrencyLongNameData(locale, currency, simpleFormats);
        Map<StandardPlural, SimpleModifier> modifiers2 = new EnumMap<>(StandardPlural.class);
        simpleFormatsToModifiers(simpleFormats, null, modifiers2);
        return new LongNameHandler(modifiers2, rules2, parent2);
    }

    public static LongNameHandler forMeasureUnit(ULocale locale, MeasureUnit unit, NumberFormatter.UnitWidth width, PluralRules rules2, MicroPropsGenerator parent2) {
        Map<StandardPlural, String> simpleFormats = new EnumMap<>(StandardPlural.class);
        getMeasureData(locale, unit, width, simpleFormats);
        Map<StandardPlural, SimpleModifier> modifiers2 = new EnumMap<>(StandardPlural.class);
        simpleFormatsToModifiers(simpleFormats, null, modifiers2);
        return new LongNameHandler(modifiers2, rules2, parent2);
    }

    private static void simpleFormatsToModifiers(Map<StandardPlural, String> simpleFormats, NumberFormat.Field field, Map<StandardPlural, SimpleModifier> output) {
        StringBuilder sb = new StringBuilder();
        for (StandardPlural plural : StandardPlural.VALUES) {
            String simpleFormat = simpleFormats.get(plural);
            if (simpleFormat == null) {
                simpleFormat = simpleFormats.get(StandardPlural.OTHER);
            }
            if (simpleFormat != null) {
                output.put(plural, new SimpleModifier(SimpleFormatterImpl.compileToStringMinMaxArguments(simpleFormat, sb, 1, 1), null, false));
            } else {
                throw new ICUException("Could not find data in 'other' plural variant with field " + field);
            }
        }
    }

    public MicroProps processQuantity(DecimalQuantity quantity) {
        MicroProps micros = this.parent.processQuantity(quantity);
        DecimalQuantity copy = quantity.createCopy();
        micros.rounding.apply(copy);
        micros.modOuter = this.modifiers.get(copy.getStandardPlural(this.rules));
        return micros;
    }
}

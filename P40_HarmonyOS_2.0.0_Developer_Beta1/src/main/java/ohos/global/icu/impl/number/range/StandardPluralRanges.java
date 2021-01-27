package ohos.global.icu.impl.number.range;

import java.util.MissingResourceException;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.StandardPlural;
import ohos.global.icu.impl.UResource;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

public class StandardPluralRanges {
    StandardPlural[] flatTriples;
    int numTriples = 0;

    /* access modifiers changed from: private */
    public static final class PluralRangesDataSink extends UResource.Sink {
        StandardPluralRanges output;

        PluralRangesDataSink(StandardPluralRanges standardPluralRanges) {
            this.output = standardPluralRanges;
        }

        @Override // ohos.global.icu.impl.UResource.Sink
        public void put(UResource.Key key, UResource.Value value, boolean z) {
            UResource.Array array = value.getArray();
            this.output.setCapacity(array.getSize());
            for (int i = 0; array.getValue(i, value); i++) {
                UResource.Array array2 = value.getArray();
                array2.getValue(0, value);
                StandardPlural fromString = StandardPlural.fromString(value.getString());
                array2.getValue(1, value);
                StandardPlural fromString2 = StandardPlural.fromString(value.getString());
                array2.getValue(2, value);
                this.output.addPluralRange(fromString, fromString2, StandardPlural.fromString(value.getString()));
            }
        }
    }

    private static void getPluralRangesData(ULocale uLocale, StandardPluralRanges standardPluralRanges) {
        StringBuilder sb = new StringBuilder();
        ICUResourceBundle bundleInstance = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "pluralRanges");
        sb.append("locales/");
        sb.append(uLocale.getLanguage());
        try {
            String stringWithFallback = bundleInstance.getStringWithFallback(sb.toString());
            sb.setLength(0);
            sb.append("rules/");
            sb.append(stringWithFallback);
            bundleInstance.getAllItemsWithFallback(sb.toString(), new PluralRangesDataSink(standardPluralRanges));
        } catch (MissingResourceException unused) {
        }
    }

    public StandardPluralRanges(ULocale uLocale) {
        getPluralRangesData(uLocale, this);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addPluralRange(StandardPlural standardPlural, StandardPlural standardPlural2, StandardPlural standardPlural3) {
        StandardPlural[] standardPluralArr = this.flatTriples;
        int i = this.numTriples;
        standardPluralArr[i * 3] = standardPlural;
        standardPluralArr[(i * 3) + 1] = standardPlural2;
        standardPluralArr[(i * 3) + 2] = standardPlural3;
        this.numTriples = i + 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCapacity(int i) {
        this.flatTriples = new StandardPlural[(i * 3)];
    }

    public StandardPlural resolve(StandardPlural standardPlural, StandardPlural standardPlural2) {
        for (int i = 0; i < this.numTriples; i++) {
            StandardPlural[] standardPluralArr = this.flatTriples;
            int i2 = i * 3;
            if (standardPlural == standardPluralArr[i2] && standardPlural2 == standardPluralArr[i2 + 1]) {
                return standardPluralArr[i2 + 2];
            }
        }
        return StandardPlural.OTHER;
    }
}

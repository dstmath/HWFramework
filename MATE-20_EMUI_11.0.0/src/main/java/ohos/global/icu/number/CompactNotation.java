package ohos.global.icu.number;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import ohos.global.icu.impl.number.CompactData;
import ohos.global.icu.impl.number.DecimalQuantity;
import ohos.global.icu.impl.number.MicroProps;
import ohos.global.icu.impl.number.MicroPropsGenerator;
import ohos.global.icu.impl.number.MutablePatternModifier;
import ohos.global.icu.impl.number.PatternStringParser;
import ohos.global.icu.text.CompactDecimalFormat;
import ohos.global.icu.text.NumberFormat;
import ohos.global.icu.text.PluralRules;
import ohos.global.icu.util.ULocale;

public class CompactNotation extends Notation {
    final Map<String, Map<String, String>> compactCustomData;
    final CompactDecimalFormat.CompactStyle compactStyle;

    @Deprecated
    public static CompactNotation forCustomData(Map<String, Map<String, String>> map) {
        return new CompactNotation(map);
    }

    CompactNotation(CompactDecimalFormat.CompactStyle compactStyle2) {
        this.compactCustomData = null;
        this.compactStyle = compactStyle2;
    }

    CompactNotation(Map<String, Map<String, String>> map) {
        this.compactStyle = null;
        this.compactCustomData = map;
    }

    /* access modifiers changed from: package-private */
    public MicroPropsGenerator withLocaleData(ULocale uLocale, String str, CompactData.CompactType compactType, PluralRules pluralRules, MutablePatternModifier mutablePatternModifier, MicroPropsGenerator microPropsGenerator) {
        return new CompactHandler(uLocale, str, compactType, pluralRules, mutablePatternModifier, microPropsGenerator);
    }

    private static class CompactHandler implements MicroPropsGenerator {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        final CompactData data;
        final MicroPropsGenerator parent;
        final Map<String, MutablePatternModifier.ImmutablePatternModifier> precomputedMods;
        final PluralRules rules;

        private CompactHandler(CompactNotation compactNotation, ULocale uLocale, String str, CompactData.CompactType compactType, PluralRules pluralRules, MutablePatternModifier mutablePatternModifier, MicroPropsGenerator microPropsGenerator) {
            this.rules = pluralRules;
            this.parent = microPropsGenerator;
            this.data = new CompactData();
            if (compactNotation.compactStyle != null) {
                this.data.populate(uLocale, str, compactNotation.compactStyle, compactType);
            } else {
                this.data.populate(compactNotation.compactCustomData);
            }
            if (mutablePatternModifier != null) {
                this.precomputedMods = new HashMap();
                precomputeAllModifiers(mutablePatternModifier);
                return;
            }
            this.precomputedMods = null;
        }

        private void precomputeAllModifiers(MutablePatternModifier mutablePatternModifier) {
            HashSet<String> hashSet = new HashSet();
            this.data.getUniquePatterns(hashSet);
            for (String str : hashSet) {
                mutablePatternModifier.setPatternInfo(PatternStringParser.parseToPatternInfo(str), NumberFormat.Field.COMPACT);
                this.precomputedMods.put(str, mutablePatternModifier.createImmutable());
            }
        }

        @Override // ohos.global.icu.impl.number.MicroPropsGenerator
        public MicroProps processQuantity(DecimalQuantity decimalQuantity) {
            MicroProps processQuantity = this.parent.processQuantity(decimalQuantity);
            int i = 0;
            if (decimalQuantity.isZeroish()) {
                processQuantity.rounder.apply(decimalQuantity);
            } else {
                int chooseMultiplierAndApply = processQuantity.rounder.chooseMultiplierAndApply(decimalQuantity, this.data);
                if (!decimalQuantity.isZeroish()) {
                    i = decimalQuantity.getMagnitude();
                }
                i -= chooseMultiplierAndApply;
            }
            String pattern = this.data.getPattern(i, decimalQuantity.getStandardPlural(this.rules));
            if (pattern != null) {
                Map<String, MutablePatternModifier.ImmutablePatternModifier> map = this.precomputedMods;
                if (map != null) {
                    map.get(pattern).applyToMicros(processQuantity, decimalQuantity);
                } else {
                    ((MutablePatternModifier) processQuantity.modMiddle).setPatternInfo(PatternStringParser.parseToPatternInfo(pattern), NumberFormat.Field.COMPACT);
                }
            }
            processQuantity.rounder = Precision.constructPassThrough();
            return processQuantity;
        }
    }
}

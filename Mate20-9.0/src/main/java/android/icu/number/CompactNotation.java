package android.icu.number;

import android.icu.impl.number.CompactData;
import android.icu.impl.number.DecimalQuantity;
import android.icu.impl.number.MicroProps;
import android.icu.impl.number.MicroPropsGenerator;
import android.icu.impl.number.MutablePatternModifier;
import android.icu.impl.number.PatternStringParser;
import android.icu.text.CompactDecimalFormat;
import android.icu.text.PluralRules;
import android.icu.util.ULocale;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CompactNotation extends Notation {
    final Map<String, Map<String, String>> compactCustomData;
    final CompactDecimalFormat.CompactStyle compactStyle;

    private static class CompactHandler implements MicroPropsGenerator {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        final CompactData data;
        final MicroPropsGenerator parent;
        final Map<String, CompactModInfo> precomputedMods;
        final PluralRules rules;

        private static class CompactModInfo {
            public MutablePatternModifier.ImmutablePatternModifier mod;
            public int numDigits;

            private CompactModInfo() {
            }
        }

        static {
            Class<CompactNotation> cls = CompactNotation.class;
        }

        private CompactHandler(CompactNotation notation, ULocale locale, String nsName, CompactData.CompactType compactType, PluralRules rules2, MutablePatternModifier buildReference, MicroPropsGenerator parent2) {
            this.rules = rules2;
            this.parent = parent2;
            this.data = new CompactData();
            if (notation.compactStyle != null) {
                this.data.populate(locale, nsName, notation.compactStyle, compactType);
            } else {
                this.data.populate(notation.compactCustomData);
            }
            if (buildReference != null) {
                this.precomputedMods = new HashMap();
                precomputeAllModifiers(buildReference);
                return;
            }
            this.precomputedMods = null;
        }

        private void precomputeAllModifiers(MutablePatternModifier buildReference) {
            Set<String> allPatterns = new HashSet<>();
            this.data.getUniquePatterns(allPatterns);
            for (String patternString : allPatterns) {
                CompactModInfo info = new CompactModInfo();
                PatternStringParser.ParsedPatternInfo patternInfo = PatternStringParser.parseToPatternInfo(patternString);
                buildReference.setPatternInfo(patternInfo);
                info.mod = buildReference.createImmutable();
                info.numDigits = patternInfo.positive.integerTotal;
                this.precomputedMods.put(patternString, info);
            }
        }

        public MicroProps processQuantity(DecimalQuantity quantity) {
            int multiplier;
            MicroProps micros = this.parent.processQuantity(quantity);
            if (quantity.isZero()) {
                multiplier = 0;
                micros.rounding.apply(quantity);
            } else {
                multiplier = (quantity.isZero() ? 0 : quantity.getMagnitude()) - micros.rounding.chooseMultiplierAndApply(quantity, this.data);
            }
            String patternString = this.data.getPattern(multiplier, quantity.getStandardPlural(this.rules));
            if (patternString != null) {
                if (this.precomputedMods != null) {
                    CompactModInfo info = this.precomputedMods.get(patternString);
                    info.mod.applyToMicros(micros, quantity);
                    int numDigits = info.numDigits;
                } else {
                    PatternStringParser.ParsedPatternInfo patternInfo = PatternStringParser.parseToPatternInfo(patternString);
                    ((MutablePatternModifier) micros.modMiddle).setPatternInfo(patternInfo);
                    int numDigits2 = patternInfo.positive.integerTotal;
                }
            }
            micros.rounding = Rounder.constructPassThrough();
            return micros;
        }
    }

    CompactNotation(CompactDecimalFormat.CompactStyle compactStyle2) {
        this.compactCustomData = null;
        this.compactStyle = compactStyle2;
    }

    CompactNotation(Map<String, Map<String, String>> compactCustomData2) {
        this.compactStyle = null;
        this.compactCustomData = compactCustomData2;
    }

    /* access modifiers changed from: package-private */
    public MicroPropsGenerator withLocaleData(ULocale locale, String nsName, CompactData.CompactType compactType, PluralRules rules, MutablePatternModifier buildReference, MicroPropsGenerator parent) {
        CompactHandler compactHandler = new CompactHandler(locale, nsName, compactType, rules, buildReference, parent);
        return compactHandler;
    }
}

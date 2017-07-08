package android.icu.text;

import android.icu.text.Transliterator.Position;
import android.icu.util.AnnualTimeZoneRule;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class RuleBasedTransliterator extends Transliterator {
    private Data data;

    static class Data {
        public TransliterationRuleSet ruleSet;
        Map<String, char[]> variableNames;
        Object[] variables;
        char variablesBase;

        public Data() {
            this.variableNames = new HashMap();
            this.ruleSet = new TransliterationRuleSet();
        }

        public UnicodeMatcher lookupMatcher(int standIn) {
            int i = standIn - this.variablesBase;
            return (i < 0 || i >= this.variables.length) ? null : (UnicodeMatcher) this.variables[i];
        }

        public UnicodeReplacer lookupReplacer(int standIn) {
            int i = standIn - this.variablesBase;
            return (i < 0 || i >= this.variables.length) ? null : (UnicodeReplacer) this.variables[i];
        }
    }

    RuleBasedTransliterator(String ID, Data data, UnicodeFilter filter) {
        super(ID, filter);
        this.data = data;
        setMaximumContextLength(data.ruleSet.getMaximumContextLength());
    }

    @Deprecated
    protected void handleTransliterate(Replaceable text, Position index, boolean incremental) {
        synchronized (this.data) {
            int loopCount = 0;
            int loopLimit = (index.limit - index.start) << 4;
            if (loopLimit < 0) {
                loopLimit = AnnualTimeZoneRule.MAX_YEAR;
            }
            while (index.start < index.limit && loopCount <= loopLimit && this.data.ruleSet.transliterate(text, index, incremental)) {
                loopCount++;
            }
        }
    }

    @Deprecated
    public String toRules(boolean escapeUnprintable) {
        return this.data.ruleSet.toRules(escapeUnprintable);
    }

    @Deprecated
    public void addSourceTargetSet(UnicodeSet filter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        this.data.ruleSet.addSourceTargetSet(filter, sourceSet, targetSet);
    }

    @Deprecated
    public Transliterator safeClone() {
        UnicodeFilter filter = getFilter();
        if (filter != null && (filter instanceof UnicodeSet)) {
            filter = new UnicodeSet((UnicodeSet) filter);
        }
        return new RuleBasedTransliterator(getID(), this.data, filter);
    }
}

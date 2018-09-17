package android.icu.text;

import android.icu.lang.UCharacter.UnicodeBlock;
import android.icu.text.Transliterator.Position;
import java.util.ArrayList;
import java.util.List;

class TransliterationRuleSet {
    private int[] index;
    private int maxContextLength = 0;
    private List<TransliterationRule> ruleVector = new ArrayList();
    private TransliterationRule[] rules;

    public int getMaximumContextLength() {
        return this.maxContextLength;
    }

    public void addRule(TransliterationRule rule) {
        this.ruleVector.add(rule);
        int len = rule.getAnteContextLength();
        if (len > this.maxContextLength) {
            this.maxContextLength = len;
        }
        this.rules = null;
    }

    public void freeze() {
        int j;
        int x;
        int n = this.ruleVector.size();
        this.index = new int[UnicodeBlock.EARLY_DYNASTIC_CUNEIFORM_ID];
        List<TransliterationRule> v = new ArrayList(n * 2);
        int[] indexValue = new int[n];
        for (j = 0; j < n; j++) {
            indexValue[j] = ((TransliterationRule) this.ruleVector.get(j)).getIndexValue();
        }
        for (x = 0; x < 256; x++) {
            this.index[x] = v.size();
            for (j = 0; j < n; j++) {
                if (indexValue[j] < 0) {
                    TransliterationRule r = (TransliterationRule) this.ruleVector.get(j);
                    if (r.matchesIndexValue(x)) {
                        v.add(r);
                    }
                } else if (indexValue[j] == x) {
                    v.add((TransliterationRule) this.ruleVector.get(j));
                }
            }
        }
        this.index[256] = v.size();
        this.rules = new TransliterationRule[v.size()];
        v.toArray(this.rules);
        StringBuilder errors = null;
        for (x = 0; x < 256; x++) {
            for (j = this.index[x]; j < this.index[x + 1] - 1; j++) {
                TransliterationRule r1 = this.rules[j];
                for (int k = j + 1; k < this.index[x + 1]; k++) {
                    TransliterationRule r2 = this.rules[k];
                    if (r1.masks(r2)) {
                        if (errors == null) {
                            errors = new StringBuilder();
                        } else {
                            errors.append("\n");
                        }
                        errors.append("Rule ").append(r1).append(" masks ").append(r2);
                    }
                }
            }
        }
        if (errors != null) {
            throw new IllegalArgumentException(errors.toString());
        }
    }

    public boolean transliterate(Replaceable text, Position pos, boolean incremental) {
        int indexByte = text.char32At(pos.start) & 255;
        int i = this.index[indexByte];
        while (i < this.index[indexByte + 1]) {
            switch (this.rules[i].matchAndReplace(text, pos, incremental)) {
                case 1:
                    return false;
                case 2:
                    return true;
                default:
                    i++;
            }
        }
        pos.start += UTF16.getCharCount(text.char32At(pos.start));
        return true;
    }

    String toRules(boolean escapeUnprintable) {
        int count = this.ruleVector.size();
        StringBuilder ruleSource = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i != 0) {
                ruleSource.append(10);
            }
            ruleSource.append(((TransliterationRule) this.ruleVector.get(i)).toRule(escapeUnprintable));
        }
        return ruleSource.toString();
    }

    void addSourceTargetSet(UnicodeSet filter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        UnicodeSet currentFilter = new UnicodeSet(filter);
        UnicodeSet revisiting = new UnicodeSet();
        int count = this.ruleVector.size();
        for (int i = 0; i < count; i++) {
            ((TransliterationRule) this.ruleVector.get(i)).addSourceTargetSet(currentFilter, sourceSet, targetSet, revisiting.clear());
            currentFilter.addAll(revisiting);
        }
    }
}

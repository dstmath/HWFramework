package android.icu.text;

import android.icu.text.Transliterator.Position;
import java.util.List;

class CompoundTransliterator extends Transliterator {
    private int numAnonymousRBTs;
    private Transliterator[] trans;

    CompoundTransliterator(List<Transliterator> list) {
        this(list, 0);
    }

    CompoundTransliterator(List<Transliterator> list, int numAnonymousRBTs) {
        super("", null);
        this.numAnonymousRBTs = 0;
        this.trans = null;
        init(list, 0, false);
        this.numAnonymousRBTs = numAnonymousRBTs;
    }

    CompoundTransliterator(String id, UnicodeFilter filter2, Transliterator[] trans2, int numAnonymousRBTs2) {
        super(id, filter2);
        this.numAnonymousRBTs = 0;
        this.trans = trans2;
        this.numAnonymousRBTs = numAnonymousRBTs2;
    }

    private void init(List<Transliterator> list, int direction, boolean fixReverseID) {
        int count = list.size();
        this.trans = new Transliterator[count];
        int i = 0;
        while (i < count) {
            this.trans[i] = (Transliterator) list.get(direction == 0 ? i : (count - 1) - i);
            i++;
        }
        if (direction == 1 && fixReverseID) {
            StringBuilder newID = new StringBuilder();
            for (i = 0; i < count; i++) {
                if (i > 0) {
                    newID.append(';');
                }
                newID.append(this.trans[i].getID());
            }
            setID(newID.toString());
        }
        computeMaximumContextLength();
    }

    public int getCount() {
        return this.trans.length;
    }

    public Transliterator getTransliterator(int index) {
        return this.trans[index];
    }

    private static void _smartAppend(StringBuilder buf, char c) {
        if (buf.length() != 0 && buf.charAt(buf.length() - 1) != c) {
            buf.append(c);
        }
    }

    public String toRules(boolean escapeUnprintable) {
        StringBuilder rulesSource = new StringBuilder();
        if (this.numAnonymousRBTs >= 1 && getFilter() != null) {
            rulesSource.append("::").append(getFilter().toPattern(escapeUnprintable)).append(';');
        }
        int i = 0;
        while (i < this.trans.length) {
            String rule;
            if (this.trans[i].getID().startsWith("%Pass")) {
                rule = this.trans[i].toRules(escapeUnprintable);
                if (this.numAnonymousRBTs > 1 && i > 0 && this.trans[i - 1].getID().startsWith("%Pass")) {
                    rule = "::Null;" + rule;
                }
            } else {
                rule = this.trans[i].getID().indexOf(59) >= 0 ? this.trans[i].toRules(escapeUnprintable) : this.trans[i].baseToRules(escapeUnprintable);
            }
            _smartAppend(rulesSource, 10);
            rulesSource.append(rule);
            _smartAppend(rulesSource, ';');
            i++;
        }
        return rulesSource.toString();
    }

    public void addSourceTargetSet(UnicodeSet filter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        UnicodeSet myFilter = new UnicodeSet(getFilterAsUnicodeSet(filter));
        UnicodeSet tempTargetSet = new UnicodeSet();
        for (Transliterator addSourceTargetSet : this.trans) {
            tempTargetSet.clear();
            addSourceTargetSet.addSourceTargetSet(myFilter, sourceSet, tempTargetSet);
            targetSet.addAll(tempTargetSet);
            myFilter.addAll(tempTargetSet);
        }
    }

    protected void handleTransliterate(Replaceable text, Position index, boolean incremental) {
        if (this.trans.length < 1) {
            index.start = index.limit;
            return;
        }
        int compoundLimit = index.limit;
        int compoundStart = index.start;
        int delta = 0;
        int i = 0;
        while (i < this.trans.length) {
            index.start = compoundStart;
            int limit = index.limit;
            if (index.start == index.limit) {
                break;
            }
            this.trans[i].filteredTransliterate(text, index, incremental);
            if (incremental || index.start == index.limit) {
                delta += index.limit - limit;
                if (incremental) {
                    index.limit = index.start;
                }
                i++;
            } else {
                throw new RuntimeException("ERROR: Incomplete non-incremental transliteration by " + this.trans[i].getID());
            }
        }
        index.limit = compoundLimit + delta;
    }

    private void computeMaximumContextLength() {
        int max = 0;
        for (Transliterator maximumContextLength : this.trans) {
            int len = maximumContextLength.getMaximumContextLength();
            if (len > max) {
                max = len;
            }
        }
        setMaximumContextLength(max);
    }

    public Transliterator safeClone() {
        UnicodeFilter filter = getFilter();
        if (filter != null && (filter instanceof UnicodeSet)) {
            filter = new UnicodeSet((UnicodeSet) filter);
        }
        return new CompoundTransliterator(getID(), filter, this.trans, this.numAnonymousRBTs);
    }
}

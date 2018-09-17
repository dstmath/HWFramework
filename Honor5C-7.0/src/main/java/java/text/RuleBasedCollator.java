package java.text;

import libcore.icu.CollationKeyICU;

public class RuleBasedCollator extends Collator {
    RuleBasedCollator(android.icu.text.RuleBasedCollator wrapper) {
        super(wrapper);
    }

    public RuleBasedCollator(String rules) throws ParseException {
        if (rules == null) {
            throw new NullPointerException("rules == null");
        }
        try {
            this.icuColl = new android.icu.text.RuleBasedCollator(rules);
        } catch (Exception e) {
            if (e instanceof ParseException) {
                throw ((ParseException) e);
            }
            throw new ParseException(e.getMessage(), -1);
        }
    }

    public String getRules() {
        return collAsICU().getRules();
    }

    public CollationElementIterator getCollationElementIterator(String source) {
        if (source != null) {
            return new CollationElementIterator(collAsICU().getCollationElementIterator(source));
        }
        throw new NullPointerException("source == null");
    }

    public CollationElementIterator getCollationElementIterator(CharacterIterator source) {
        if (source != null) {
            return new CollationElementIterator(collAsICU().getCollationElementIterator(source));
        }
        throw new NullPointerException("source == null");
    }

    public synchronized int compare(String source, String target) {
        if (source == null || target == null) {
            throw new NullPointerException();
        }
        return this.icuColl.compare(source, target);
    }

    public synchronized CollationKey getCollationKey(String source) {
        if (source == null) {
            return null;
        }
        return new CollationKeyICU(source, this.icuColl.getCollationKey(source));
    }

    public Object clone() {
        return super.clone();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return super.equals(obj);
    }

    public int hashCode() {
        return this.icuColl.hashCode();
    }

    private android.icu.text.RuleBasedCollator collAsICU() {
        return (android.icu.text.RuleBasedCollator) this.icuColl;
    }
}

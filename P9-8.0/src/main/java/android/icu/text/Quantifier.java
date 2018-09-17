package android.icu.text;

import android.icu.impl.Utility;

class Quantifier implements UnicodeMatcher {
    public static final int MAX = Integer.MAX_VALUE;
    private UnicodeMatcher matcher;
    private int maxCount;
    private int minCount;

    public Quantifier(UnicodeMatcher theMatcher, int theMinCount, int theMaxCount) {
        if (theMatcher == null || theMinCount < 0 || theMaxCount < 0 || theMinCount > theMaxCount) {
            throw new IllegalArgumentException();
        }
        this.matcher = theMatcher;
        this.minCount = theMinCount;
        this.maxCount = theMaxCount;
    }

    public int matches(Replaceable text, int[] offset, int limit, boolean incremental) {
        int start = offset[0];
        int count = 0;
        while (count < this.maxCount) {
            int pos = offset[0];
            int m = this.matcher.matches(text, offset, limit, incremental);
            if (m == 2) {
                count++;
                if (pos == offset[0]) {
                    break;
                }
            } else if (incremental && m == 1) {
                return 1;
            }
        }
        if (incremental && offset[0] == limit) {
            return 1;
        }
        if (count >= this.minCount) {
            return 2;
        }
        offset[0] = start;
        return 0;
    }

    public String toPattern(boolean escapeUnprintable) {
        StringBuilder result = new StringBuilder();
        result.append(this.matcher.toPattern(escapeUnprintable));
        if (this.minCount == 0) {
            if (this.maxCount == 1) {
                return result.append('?').toString();
            }
            if (this.maxCount == Integer.MAX_VALUE) {
                return result.append('*').toString();
            }
        } else if (this.minCount == 1 && this.maxCount == Integer.MAX_VALUE) {
            return result.append('+').toString();
        }
        result.append('{');
        result.append(Utility.hex((long) this.minCount, 1));
        result.append(',');
        if (this.maxCount != Integer.MAX_VALUE) {
            result.append(Utility.hex((long) this.maxCount, 1));
        }
        result.append('}');
        return result.toString();
    }

    public boolean matchesIndexValue(int v) {
        return this.minCount != 0 ? this.matcher.matchesIndexValue(v) : true;
    }

    public void addMatchSetTo(UnicodeSet toUnionTo) {
        if (this.maxCount > 0) {
            this.matcher.addMatchSetTo(toUnionTo);
        }
    }
}

package android.icu.text;

import android.icu.impl.Utility;

class StringMatcher implements UnicodeMatcher, UnicodeReplacer {
    private final Data data;
    private int matchLimit;
    private int matchStart;
    private String pattern;
    private int segmentNumber;

    public StringMatcher(String theString, int segmentNum, Data theData) {
        this.data = theData;
        this.pattern = theString;
        this.matchLimit = -1;
        this.matchStart = -1;
        this.segmentNumber = segmentNum;
    }

    public StringMatcher(String theString, int start, int limit, int segmentNum, Data theData) {
        this(theString.substring(start, limit), segmentNum, theData);
    }

    public int matches(Replaceable text, int[] offset, int limit, boolean incremental) {
        int[] cursor = new int[]{offset[0]};
        int i;
        char keyChar;
        UnicodeMatcher subm;
        int m;
        if (limit < cursor[0]) {
            for (i = this.pattern.length() - 1; i >= 0; i--) {
                keyChar = this.pattern.charAt(i);
                subm = this.data.lookupMatcher(keyChar);
                if (subm != null) {
                    m = subm.matches(text, cursor, limit, incremental);
                    if (m != 2) {
                        return m;
                    }
                } else if (cursor[0] <= limit || keyChar != text.charAt(cursor[0])) {
                    return 0;
                } else {
                    cursor[0] = cursor[0] - 1;
                }
            }
            if (this.matchStart < 0) {
                this.matchStart = cursor[0] + 1;
                this.matchLimit = offset[0] + 1;
            }
        } else {
            for (i = 0; i < this.pattern.length(); i++) {
                if (incremental && cursor[0] == limit) {
                    return 1;
                }
                keyChar = this.pattern.charAt(i);
                subm = this.data.lookupMatcher(keyChar);
                if (subm != null) {
                    m = subm.matches(text, cursor, limit, incremental);
                    if (m != 2) {
                        return m;
                    }
                } else if (cursor[0] >= limit || keyChar != text.charAt(cursor[0])) {
                    return 0;
                } else {
                    cursor[0] = cursor[0] + 1;
                }
            }
            this.matchStart = offset[0];
            this.matchLimit = cursor[0];
        }
        offset[0] = cursor[0];
        return 2;
    }

    public String toPattern(boolean escapeUnprintable) {
        StringBuffer result = new StringBuffer();
        StringBuffer quoteBuf = new StringBuffer();
        if (this.segmentNumber > 0) {
            result.append('(');
        }
        for (int i = 0; i < this.pattern.length(); i++) {
            int keyChar = this.pattern.charAt(i);
            UnicodeMatcher m = this.data.lookupMatcher(keyChar);
            if (m == null) {
                Utility.appendToRule(result, keyChar, false, escapeUnprintable, quoteBuf);
            } else {
                Utility.appendToRule(result, m.toPattern(escapeUnprintable), true, escapeUnprintable, quoteBuf);
            }
        }
        if (this.segmentNumber > 0) {
            result.append(')');
        }
        Utility.appendToRule(result, -1, true, escapeUnprintable, quoteBuf);
        return result.toString();
    }

    public boolean matchesIndexValue(int v) {
        boolean z = true;
        if (this.pattern.length() == 0) {
            return true;
        }
        int c = UTF16.charAt(this.pattern, 0);
        UnicodeMatcher m = this.data.lookupMatcher(c);
        if (m != null) {
            z = m.matchesIndexValue(v);
        } else if ((c & 255) != v) {
            z = false;
        }
        return z;
    }

    public void addMatchSetTo(UnicodeSet toUnionTo) {
        int i = 0;
        while (i < this.pattern.length()) {
            int ch = UTF16.charAt(this.pattern, i);
            UnicodeMatcher matcher = this.data.lookupMatcher(ch);
            if (matcher == null) {
                toUnionTo.add(ch);
            } else {
                matcher.addMatchSetTo(toUnionTo);
            }
            i += UTF16.getCharCount(ch);
        }
    }

    public int replace(Replaceable text, int start, int limit, int[] cursor) {
        int outLen = 0;
        int dest = limit;
        if (this.matchStart >= 0 && this.matchStart != this.matchLimit) {
            text.copy(this.matchStart, this.matchLimit, limit);
            outLen = this.matchLimit - this.matchStart;
        }
        text.replace(start, limit, "");
        return outLen;
    }

    public String toReplacerPattern(boolean escapeUnprintable) {
        StringBuffer rule = new StringBuffer("$");
        Utility.appendNumber(rule, this.segmentNumber, 10, 1);
        return rule.toString();
    }

    public void resetMatch() {
        this.matchLimit = -1;
        this.matchStart = -1;
    }

    public void addReplacementSetTo(UnicodeSet toUnionTo) {
    }
}

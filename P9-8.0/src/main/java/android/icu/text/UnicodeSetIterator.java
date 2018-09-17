package android.icu.text;

import java.util.Iterator;

public class UnicodeSetIterator {
    public static int IS_STRING = -1;
    public int codepoint;
    public int codepointEnd;
    @Deprecated
    protected int endElement;
    private int endRange = 0;
    @Deprecated
    protected int nextElement;
    private int range = 0;
    private UnicodeSet set;
    public String string;
    private Iterator<String> stringIterator = null;

    public UnicodeSetIterator(UnicodeSet set) {
        reset(set);
    }

    public UnicodeSetIterator() {
        reset(new UnicodeSet());
    }

    public boolean next() {
        int i;
        if (this.nextElement <= this.endElement) {
            i = this.nextElement;
            this.nextElement = i + 1;
            this.codepointEnd = i;
            this.codepoint = i;
            return true;
        } else if (this.range < this.endRange) {
            i = this.range + 1;
            this.range = i;
            loadRange(i);
            i = this.nextElement;
            this.nextElement = i + 1;
            this.codepointEnd = i;
            this.codepoint = i;
            return true;
        } else if (this.stringIterator == null) {
            return false;
        } else {
            this.codepoint = IS_STRING;
            this.string = (String) this.stringIterator.next();
            if (!this.stringIterator.hasNext()) {
                this.stringIterator = null;
            }
            return true;
        }
    }

    public boolean nextRange() {
        if (this.nextElement <= this.endElement) {
            this.codepointEnd = this.endElement;
            this.codepoint = this.nextElement;
            this.nextElement = this.endElement + 1;
            return true;
        } else if (this.range < this.endRange) {
            int i = this.range + 1;
            this.range = i;
            loadRange(i);
            this.codepointEnd = this.endElement;
            this.codepoint = this.nextElement;
            this.nextElement = this.endElement + 1;
            return true;
        } else if (this.stringIterator == null) {
            return false;
        } else {
            this.codepoint = IS_STRING;
            this.string = (String) this.stringIterator.next();
            if (!this.stringIterator.hasNext()) {
                this.stringIterator = null;
            }
            return true;
        }
    }

    public void reset(UnicodeSet uset) {
        this.set = uset;
        reset();
    }

    public void reset() {
        this.endRange = this.set.getRangeCount() - 1;
        this.range = 0;
        this.endElement = -1;
        this.nextElement = 0;
        if (this.endRange >= 0) {
            loadRange(this.range);
        }
        this.stringIterator = null;
        if (this.set.strings != null) {
            this.stringIterator = this.set.strings.iterator();
            if (!this.stringIterator.hasNext()) {
                this.stringIterator = null;
            }
        }
    }

    public String getString() {
        if (this.codepoint != IS_STRING) {
            return UTF16.valueOf(this.codepoint);
        }
        return this.string;
    }

    @Deprecated
    public UnicodeSet getSet() {
        return this.set;
    }

    @Deprecated
    protected void loadRange(int aRange) {
        this.nextElement = this.set.getRangeStart(aRange);
        this.endElement = this.set.getRangeEnd(aRange);
    }
}

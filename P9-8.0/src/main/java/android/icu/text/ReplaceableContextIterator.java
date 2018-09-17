package android.icu.text;

import android.icu.impl.UCaseProps.ContextIterator;

class ReplaceableContextIterator implements ContextIterator {
    protected int contextLimit = 0;
    protected int contextStart = 0;
    protected int cpLimit = 0;
    protected int cpStart = 0;
    protected int dir = 0;
    protected int index = 0;
    protected int limit = 0;
    protected boolean reachedLimit = false;
    protected Replaceable rep = null;

    ReplaceableContextIterator() {
    }

    public void setText(Replaceable rep) {
        this.rep = rep;
        int length = rep.length();
        this.contextLimit = length;
        this.limit = length;
        this.contextStart = 0;
        this.index = 0;
        this.cpLimit = 0;
        this.cpStart = 0;
        this.dir = 0;
        this.reachedLimit = false;
    }

    public void setIndex(int index) {
        this.cpLimit = index;
        this.cpStart = index;
        this.index = 0;
        this.dir = 0;
        this.reachedLimit = false;
    }

    public int getCaseMapCPStart() {
        return this.cpStart;
    }

    public void setLimit(int lim) {
        if (lim < 0 || lim > this.rep.length()) {
            this.limit = this.rep.length();
        } else {
            this.limit = lim;
        }
        this.reachedLimit = false;
    }

    public void setContextLimits(int contextStart, int contextLimit) {
        if (contextStart < 0) {
            this.contextStart = 0;
        } else if (contextStart <= this.rep.length()) {
            this.contextStart = contextStart;
        } else {
            this.contextStart = this.rep.length();
        }
        if (contextLimit < this.contextStart) {
            this.contextLimit = this.contextStart;
        } else if (contextLimit <= this.rep.length()) {
            this.contextLimit = contextLimit;
        } else {
            this.contextLimit = this.rep.length();
        }
        this.reachedLimit = false;
    }

    public int nextCaseMapCP() {
        if (this.cpLimit >= this.limit) {
            return -1;
        }
        this.cpStart = this.cpLimit;
        int c = this.rep.char32At(this.cpLimit);
        this.cpLimit += UTF16.getCharCount(c);
        return c;
    }

    public int replace(String text) {
        int delta = text.length() - (this.cpLimit - this.cpStart);
        this.rep.replace(this.cpStart, this.cpLimit, text);
        this.cpLimit += delta;
        this.limit += delta;
        this.contextLimit += delta;
        return delta;
    }

    public boolean didReachLimit() {
        return this.reachedLimit;
    }

    public void reset(int direction) {
        if (direction > 0) {
            this.dir = 1;
            this.index = this.cpLimit;
        } else if (direction < 0) {
            this.dir = -1;
            this.index = this.cpStart;
        } else {
            this.dir = 0;
            this.index = 0;
        }
        this.reachedLimit = false;
    }

    public int next() {
        int c;
        if (this.dir > 0) {
            if (this.index < this.contextLimit) {
                c = this.rep.char32At(this.index);
                this.index += UTF16.getCharCount(c);
                return c;
            }
            this.reachedLimit = true;
        } else if (this.dir < 0 && this.index > this.contextStart) {
            c = this.rep.char32At(this.index - 1);
            this.index -= UTF16.getCharCount(c);
            return c;
        }
        return -1;
    }
}

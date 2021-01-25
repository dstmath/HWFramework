package ohos.global.icu.text;

import ohos.global.icu.impl.UCaseProps;

class ReplaceableContextIterator implements UCaseProps.ContextIterator {
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

    public void setText(Replaceable replaceable) {
        this.rep = replaceable;
        int length = replaceable.length();
        this.contextLimit = length;
        this.limit = length;
        this.contextStart = 0;
        this.index = 0;
        this.cpLimit = 0;
        this.cpStart = 0;
        this.dir = 0;
        this.reachedLimit = false;
    }

    public void setIndex(int i) {
        this.cpLimit = i;
        this.cpStart = i;
        this.index = 0;
        this.dir = 0;
        this.reachedLimit = false;
    }

    public int getCaseMapCPStart() {
        return this.cpStart;
    }

    public void setLimit(int i) {
        if (i < 0 || i > this.rep.length()) {
            this.limit = this.rep.length();
        } else {
            this.limit = i;
        }
        this.reachedLimit = false;
    }

    public void setContextLimits(int i, int i2) {
        if (i < 0) {
            this.contextStart = 0;
        } else if (i <= this.rep.length()) {
            this.contextStart = i;
        } else {
            this.contextStart = this.rep.length();
        }
        int i3 = this.contextStart;
        if (i2 < i3) {
            this.contextLimit = i3;
        } else if (i2 <= this.rep.length()) {
            this.contextLimit = i2;
        } else {
            this.contextLimit = this.rep.length();
        }
        this.reachedLimit = false;
    }

    public int nextCaseMapCP() {
        int i = this.cpLimit;
        if (i >= this.limit) {
            return -1;
        }
        this.cpStart = i;
        int char32At = this.rep.char32At(i);
        this.cpLimit += UTF16.getCharCount(char32At);
        return char32At;
    }

    public int replace(String str) {
        int length = str.length();
        int i = this.cpLimit;
        int i2 = this.cpStart;
        int i3 = length - (i - i2);
        this.rep.replace(i2, i, str);
        this.cpLimit += i3;
        this.limit += i3;
        this.contextLimit += i3;
        return i3;
    }

    public boolean didReachLimit() {
        return this.reachedLimit;
    }

    @Override // ohos.global.icu.impl.UCaseProps.ContextIterator
    public void reset(int i) {
        if (i > 0) {
            this.dir = 1;
            this.index = this.cpLimit;
        } else if (i < 0) {
            this.dir = -1;
            this.index = this.cpStart;
        } else {
            this.dir = 0;
            this.index = 0;
        }
        this.reachedLimit = false;
    }

    @Override // ohos.global.icu.impl.UCaseProps.ContextIterator
    public int next() {
        int i;
        int i2 = this.dir;
        if (i2 > 0) {
            int i3 = this.index;
            if (i3 < this.contextLimit) {
                int char32At = this.rep.char32At(i3);
                this.index += UTF16.getCharCount(char32At);
                return char32At;
            }
            this.reachedLimit = true;
            return -1;
        } else if (i2 >= 0 || (i = this.index) <= this.contextStart) {
            return -1;
        } else {
            int char32At2 = this.rep.char32At(i - 1);
            this.index -= UTF16.getCharCount(char32At2);
            return char32At2;
        }
    }
}

package android.icu.impl;

public final class Differ<T> {
    private int EQUALSIZE;
    private int STACKSIZE;
    private T[] a;
    private int aCount;
    private int aLine;
    private int aTop;
    private T[] b;
    private int bCount;
    private int bLine;
    private int bTop;
    private T last;
    private int maxSame;
    private T next;

    public Differ(int stackSize, int matchCount) {
        this.last = null;
        this.next = null;
        this.aCount = 0;
        this.bCount = 0;
        this.aLine = 1;
        this.bLine = 1;
        this.maxSame = 0;
        this.aTop = 0;
        this.bTop = 0;
        this.STACKSIZE = stackSize;
        this.EQUALSIZE = matchCount;
        this.a = new Object[(stackSize + matchCount)];
        this.b = new Object[(stackSize + matchCount)];
    }

    public void add(T aStr, T bStr) {
        addA(aStr);
        addB(bStr);
    }

    public void addA(T aStr) {
        flush();
        Object[] objArr = this.a;
        int i = this.aCount;
        this.aCount = i + 1;
        objArr[i] = aStr;
    }

    public void addB(T bStr) {
        flush();
        Object[] objArr = this.b;
        int i = this.bCount;
        this.bCount = i + 1;
        objArr[i] = bStr;
    }

    public int getALine(int offset) {
        return (this.aLine + this.maxSame) + offset;
    }

    public T getA(int offset) {
        if (offset < 0) {
            return this.last;
        }
        if (offset > this.aTop - this.maxSame) {
            return this.next;
        }
        return this.a[offset];
    }

    public int getACount() {
        return this.aTop - this.maxSame;
    }

    public int getBCount() {
        return this.bTop - this.maxSame;
    }

    public int getBLine(int offset) {
        return (this.bLine + this.maxSame) + offset;
    }

    public T getB(int offset) {
        if (offset < 0) {
            return this.last;
        }
        if (offset > this.bTop - this.maxSame) {
            return this.next;
        }
        return this.b[offset];
    }

    public void checkMatch(boolean finalPass) {
        int max = this.aCount;
        if (max > this.bCount) {
            max = this.bCount;
        }
        int i = 0;
        while (i < max && this.a[i].equals(this.b[i])) {
            i++;
        }
        this.maxSame = i;
        int i2 = this.maxSame;
        this.bTop = i2;
        this.aTop = i2;
        if (this.maxSame > 0) {
            this.last = this.a[this.maxSame - 1];
        }
        this.next = null;
        if (finalPass) {
            this.aTop = this.aCount;
            this.bTop = this.bCount;
            this.next = null;
        } else if (this.aCount - this.maxSame >= this.EQUALSIZE && this.bCount - this.maxSame >= this.EQUALSIZE) {
            int match = find(this.a, this.aCount - this.EQUALSIZE, this.aCount, this.b, this.maxSame, this.bCount);
            if (match != -1) {
                this.aTop = this.aCount - this.EQUALSIZE;
                this.bTop = match;
                this.next = this.a[this.aTop];
                return;
            }
            match = find(this.b, this.bCount - this.EQUALSIZE, this.bCount, this.a, this.maxSame, this.aCount);
            if (match != -1) {
                this.bTop = this.bCount - this.EQUALSIZE;
                this.aTop = match;
                this.next = this.b[this.bTop];
                return;
            }
            if (this.aCount >= this.STACKSIZE || this.bCount >= this.STACKSIZE) {
                this.aCount = (this.aCount + this.maxSame) / 2;
                this.bCount = (this.bCount + this.maxSame) / 2;
                this.next = null;
            }
        }
    }

    public int find(T[] aArr, int aStart, int aEnd, T[] bArr, int bStart, int bEnd) {
        int len = aEnd - aStart;
        int bEndMinus = bEnd - len;
        int i = bStart;
        while (i <= bEndMinus) {
            int j = 0;
            while (j < len) {
                if (bArr[i + j].equals(aArr[aStart + j])) {
                    j++;
                } else {
                    i++;
                }
            }
            return i;
        }
        return -1;
    }

    private void flush() {
        if (this.aTop != 0) {
            int newCount = this.aCount - this.aTop;
            System.arraycopy(this.a, this.aTop, this.a, 0, newCount);
            this.aCount = newCount;
            this.aLine += this.aTop;
            this.aTop = 0;
        }
        if (this.bTop != 0) {
            newCount = this.bCount - this.bTop;
            System.arraycopy(this.b, this.bTop, this.b, 0, newCount);
            this.bCount = newCount;
            this.bLine += this.bTop;
            this.bTop = 0;
        }
    }
}

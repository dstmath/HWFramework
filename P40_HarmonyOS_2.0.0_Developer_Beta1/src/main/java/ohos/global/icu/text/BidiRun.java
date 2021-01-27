package ohos.global.icu.text;

public class BidiRun {
    int insertRemove;
    byte level;
    int limit;
    int start;

    BidiRun() {
        this(0, 0, (byte) 0);
    }

    BidiRun(int i, int i2, byte b) {
        this.start = i;
        this.limit = i2;
        this.level = b;
    }

    /* access modifiers changed from: package-private */
    public void copyFrom(BidiRun bidiRun) {
        this.start = bidiRun.start;
        this.limit = bidiRun.limit;
        this.level = bidiRun.level;
        this.insertRemove = bidiRun.insertRemove;
    }

    public int getStart() {
        return this.start;
    }

    public int getLimit() {
        return this.limit;
    }

    public int getLength() {
        return this.limit - this.start;
    }

    public byte getEmbeddingLevel() {
        return this.level;
    }

    public boolean isOddRun() {
        return (this.level & 1) == 1;
    }

    public boolean isEvenRun() {
        return (this.level & 1) == 0;
    }

    public byte getDirection() {
        return (byte) (this.level & 1);
    }

    public String toString() {
        return "BidiRun " + this.start + " - " + this.limit + " @ " + ((int) this.level);
    }
}

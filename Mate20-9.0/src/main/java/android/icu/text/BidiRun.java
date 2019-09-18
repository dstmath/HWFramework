package android.icu.text;

public class BidiRun {
    int insertRemove;
    byte level;
    int limit;
    int start;

    BidiRun() {
        this(0, 0, (byte) 0);
    }

    BidiRun(int start2, int limit2, byte embeddingLevel) {
        this.start = start2;
        this.limit = limit2;
        this.level = embeddingLevel;
    }

    /* access modifiers changed from: package-private */
    public void copyFrom(BidiRun run) {
        this.start = run.start;
        this.limit = run.limit;
        this.level = run.level;
        this.insertRemove = run.insertRemove;
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
        return "BidiRun " + this.start + " - " + this.limit + " @ " + this.level;
    }
}

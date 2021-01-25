package ohos.net;

public final class UidRange {
    public int start;
    public int stop;

    public UidRange(int i, int i2) {
        this.start = i;
        this.stop = i2;
    }

    public boolean contains(int i) {
        return this.start <= i && i <= this.stop;
    }

    public boolean containsRange(UidRange uidRange) {
        return uidRange != null && this.start <= uidRange.start && uidRange.stop <= this.stop;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UidRange)) {
            return false;
        }
        UidRange uidRange = (UidRange) obj;
        return this.start == uidRange.start && this.stop == uidRange.stop;
    }

    public int hashCode() {
        return ((209 + this.start) * 19) + this.stop;
    }
}

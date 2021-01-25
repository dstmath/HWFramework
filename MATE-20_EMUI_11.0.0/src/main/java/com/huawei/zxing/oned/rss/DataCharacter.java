package com.huawei.zxing.oned.rss;

public class DataCharacter {
    private final int checksumPortion;
    private final int value;

    public DataCharacter(int value2, int checksumPortion2) {
        this.value = value2;
        this.checksumPortion = checksumPortion2;
    }

    public final int getValue() {
        return this.value;
    }

    public final int getChecksumPortion() {
        return this.checksumPortion;
    }

    public final String toString() {
        return this.value + "(" + this.checksumPortion + ')';
    }

    public final boolean equals(Object o) {
        if (!(o instanceof DataCharacter)) {
            return false;
        }
        DataCharacter that = (DataCharacter) o;
        if (this.value == that.value && this.checksumPortion == that.checksumPortion) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return this.value ^ this.checksumPortion;
    }
}

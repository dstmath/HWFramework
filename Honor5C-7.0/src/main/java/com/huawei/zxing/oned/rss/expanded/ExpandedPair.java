package com.huawei.zxing.oned.rss.expanded;

import com.huawei.zxing.oned.rss.DataCharacter;
import com.huawei.zxing.oned.rss.FinderPattern;

final class ExpandedPair {
    private final FinderPattern finderPattern;
    private final DataCharacter leftChar;
    private final boolean mayBeLast;
    private final DataCharacter rightChar;

    ExpandedPair(DataCharacter leftChar, DataCharacter rightChar, FinderPattern finderPattern, boolean mayBeLast) {
        this.leftChar = leftChar;
        this.rightChar = rightChar;
        this.finderPattern = finderPattern;
        this.mayBeLast = mayBeLast;
    }

    boolean mayBeLast() {
        return this.mayBeLast;
    }

    DataCharacter getLeftChar() {
        return this.leftChar;
    }

    DataCharacter getRightChar() {
        return this.rightChar;
    }

    FinderPattern getFinderPattern() {
        return this.finderPattern;
    }

    public boolean mustBeLast() {
        return this.rightChar == null;
    }

    public String toString() {
        return "[ " + this.leftChar + " , " + this.rightChar + " : " + (this.finderPattern == null ? "null" : Integer.valueOf(this.finderPattern.getValue())) + " ]";
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof ExpandedPair)) {
            return false;
        }
        ExpandedPair that = (ExpandedPair) o;
        if (equalsOrNull(this.leftChar, that.leftChar) && equalsOrNull(this.rightChar, that.rightChar)) {
            z = equalsOrNull(this.finderPattern, that.finderPattern);
        }
        return z;
    }

    private static boolean equalsOrNull(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        } else {
            return o1.equals(o2);
        }
    }

    public int hashCode() {
        return (hashNotNull(this.leftChar) ^ hashNotNull(this.rightChar)) ^ hashNotNull(this.finderPattern);
    }

    private static int hashNotNull(Object o) {
        return o == null ? 0 : o.hashCode();
    }
}

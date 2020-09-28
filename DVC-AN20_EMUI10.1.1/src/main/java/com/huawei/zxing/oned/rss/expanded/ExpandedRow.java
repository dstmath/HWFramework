package com.huawei.zxing.oned.rss.expanded;

import java.util.ArrayList;
import java.util.List;

final class ExpandedRow {
    private final List<ExpandedPair> pairs;
    private final int rowNumber;
    private final boolean wasReversed;

    ExpandedRow(List<ExpandedPair> pairs2, int rowNumber2, boolean wasReversed2) {
        this.pairs = new ArrayList(pairs2);
        this.rowNumber = rowNumber2;
        this.wasReversed = wasReversed2;
    }

    /* access modifiers changed from: package-private */
    public List<ExpandedPair> getPairs() {
        return this.pairs;
    }

    /* access modifiers changed from: package-private */
    public int getRowNumber() {
        return this.rowNumber;
    }

    /* access modifiers changed from: package-private */
    public boolean isReversed() {
        return this.wasReversed;
    }

    /* access modifiers changed from: package-private */
    public boolean isEquivalent(List<ExpandedPair> otherPairs) {
        return this.pairs.equals(otherPairs);
    }

    public String toString() {
        return "{ " + this.pairs + " }";
    }

    public boolean equals(Object o) {
        if (!(o instanceof ExpandedRow)) {
            return false;
        }
        ExpandedRow that = (ExpandedRow) o;
        if (!this.pairs.equals(that.getPairs()) || this.wasReversed != that.wasReversed) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.pairs.hashCode() ^ Boolean.valueOf(this.wasReversed).hashCode();
    }
}

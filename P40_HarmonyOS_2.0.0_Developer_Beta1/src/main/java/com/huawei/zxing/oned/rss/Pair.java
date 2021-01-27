package com.huawei.zxing.oned.rss;

final class Pair extends DataCharacter {
    private int count;
    private final FinderPattern finderPattern;

    Pair(int value, int checksumPortion, FinderPattern finderPattern2) {
        super(value, checksumPortion);
        this.finderPattern = finderPattern2;
    }

    /* access modifiers changed from: package-private */
    public FinderPattern getFinderPattern() {
        return this.finderPattern;
    }

    /* access modifiers changed from: package-private */
    public int getCount() {
        return this.count;
    }

    /* access modifiers changed from: package-private */
    public void incrementCount() {
        this.count++;
    }
}

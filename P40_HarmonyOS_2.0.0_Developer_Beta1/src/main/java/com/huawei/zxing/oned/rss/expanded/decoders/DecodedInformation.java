package com.huawei.zxing.oned.rss.expanded.decoders;

final class DecodedInformation extends DecodedObject {
    private final String newString;
    private final boolean remaining;
    private final int remainingValue;

    DecodedInformation(int newPosition, String newString2) {
        super(newPosition);
        this.newString = newString2;
        this.remaining = false;
        this.remainingValue = 0;
    }

    DecodedInformation(int newPosition, String newString2, int remainingValue2) {
        super(newPosition);
        this.remaining = true;
        this.remainingValue = remainingValue2;
        this.newString = newString2;
    }

    /* access modifiers changed from: package-private */
    public String getNewString() {
        return this.newString;
    }

    /* access modifiers changed from: package-private */
    public boolean isRemaining() {
        return this.remaining;
    }

    /* access modifiers changed from: package-private */
    public int getRemainingValue() {
        return this.remainingValue;
    }
}

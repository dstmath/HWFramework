package com.huawei.zxing.oned.rss.expanded.decoders;

/* access modifiers changed from: package-private */
public final class DecodedChar extends DecodedObject {
    static final char FNC1 = '$';
    private final char value;

    DecodedChar(int newPosition, char value2) {
        super(newPosition);
        this.value = value2;
    }

    /* access modifiers changed from: package-private */
    public char getValue() {
        return this.value;
    }

    /* access modifiers changed from: package-private */
    public boolean isFNC1() {
        return this.value == '$';
    }
}

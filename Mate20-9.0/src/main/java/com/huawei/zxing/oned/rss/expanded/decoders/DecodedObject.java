package com.huawei.zxing.oned.rss.expanded.decoders;

abstract class DecodedObject {
    private final int newPosition;

    DecodedObject(int newPosition2) {
        this.newPosition = newPosition2;
    }

    /* access modifiers changed from: package-private */
    public final int getNewPosition() {
        return this.newPosition;
    }
}

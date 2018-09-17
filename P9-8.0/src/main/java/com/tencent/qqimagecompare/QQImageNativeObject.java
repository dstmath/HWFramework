package com.tencent.qqimagecompare;

public abstract class QQImageNativeObject {
    protected long mThisC;

    protected abstract long createNativeObject();

    protected abstract void destroyNativeObject(long j);

    public void finish() {
        if (this.mThisC != 0) {
            destroyNativeObject(this.mThisC);
            this.mThisC = 0;
        }
    }

    public long getThisPointAddressInC() {
        return this.mThisC;
    }

    public void init() {
        if (this.mThisC == 0) {
            this.mThisC = createNativeObject();
        }
    }
}

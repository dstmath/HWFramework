package com.tencent.qqimagecompare;

import android.graphics.Bitmap;
import java.nio.ByteBuffer;

public class QQImageFeatureHSV extends QQImageNativeObject {
    private native int CompareC(long j, QQImageFeatureHSV qQImageFeatureHSV);

    private static native void FreeSerializationBufferC(ByteBuffer byteBuffer);

    private native int GetImageFeatureC(long j, Bitmap bitmap);

    private native ByteBuffer SerializationC(long j);

    private native int UnserializationC(long j, byte[] bArr);

    public int compare(QQImageFeatureHSV qQImageFeatureHSV) {
        return CompareC(this.mThisC, qQImageFeatureHSV);
    }

    protected native long createNativeObject();

    protected native void destroyNativeObject(long j);

    public int getImageFeature(Bitmap bitmap) {
        return GetImageFeatureC(this.mThisC, bitmap);
    }

    public byte[] serialization() {
        ByteBuffer SerializationC = SerializationC(this.mThisC);
        byte[] bArr = new byte[SerializationC.limit()];
        SerializationC.get(bArr);
        FreeSerializationBufferC(SerializationC);
        return bArr;
    }

    public int unserialization(byte[] bArr) {
        return UnserializationC(this.mThisC, bArr);
    }
}

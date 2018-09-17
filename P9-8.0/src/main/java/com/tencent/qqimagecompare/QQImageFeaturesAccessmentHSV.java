package com.tencent.qqimagecompare;

import java.util.ArrayList;

public class QQImageFeaturesAccessmentHSV extends QQImageNativeObject {

    public enum eDimensionType {
        Sharpness,
        Lightness
    }

    private static native void AddDimensionC(long j, int i, int i2);

    private static native int GetFeaturesRankC(long j, long[] jArr, int[] iArr);

    public void addDimension(eDimensionType edimensiontype, int i) {
        int i2 = 0;
        switch (edimensiontype) {
            case Sharpness:
                i2 = 1;
                break;
            case Lightness:
                i2 = 2;
                break;
        }
        AddDimensionC(this.mThisC, i2, i);
    }

    protected native long createNativeObject();

    protected native void destroyNativeObject(long j);

    public int[] getFeaturesRanks(ArrayList<QQImageFeatureHSV> arrayList) {
        int size = arrayList.size();
        int[] iArr = new int[size];
        long[] jArr = new long[size];
        for (int i = 0; i < size; i++) {
            jArr[i] = ((QQImageFeatureHSV) arrayList.get(i)).mThisC;
        }
        GetFeaturesRankC(this.mThisC, jArr, iArr);
        return iArr;
    }
}

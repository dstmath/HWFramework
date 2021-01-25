package com.huawei.nb.kv;

import android.os.Parcelable;

public interface Key extends Parcelable {
    Integer dType();

    @Override // java.lang.Object
    String toString();

    Integer vType();

    void vType(Integer num);

    boolean verify();
}

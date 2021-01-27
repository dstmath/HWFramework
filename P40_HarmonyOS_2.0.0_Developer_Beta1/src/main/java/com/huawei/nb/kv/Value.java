package com.huawei.nb.kv;

import android.os.Parcelable;

public interface Value extends Parcelable {
    Integer dType();

    @Override // java.lang.Object
    String toString();

    boolean verify();
}

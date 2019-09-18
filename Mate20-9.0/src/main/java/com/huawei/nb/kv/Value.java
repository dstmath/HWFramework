package com.huawei.nb.kv;

import android.os.Parcelable;

public interface Value extends Parcelable {
    Integer dType();

    String toString();

    boolean verify();
}

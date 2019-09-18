package com.huawei.nb.kv;

import android.os.Parcelable;

public interface Key extends Parcelable {
    Integer dType();

    String toString();

    Integer vType();

    void vType(Integer num);

    boolean verify();
}

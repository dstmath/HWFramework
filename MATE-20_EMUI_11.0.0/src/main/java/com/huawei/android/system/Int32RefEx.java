package com.huawei.android.system;

import android.system.Int32Ref;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class Int32RefEx {
    private Int32Ref mInt32Ref;

    public Int32RefEx(int num) {
        this.mInt32Ref = new Int32Ref(num);
    }

    public int getValue() {
        return this.mInt32Ref.value;
    }

    public Int32Ref getInner() {
        return this.mInt32Ref;
    }
}

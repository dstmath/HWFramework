package com.huawei.android.graphics;

import android.graphics.MaskFilter;
import android.graphics.TableMaskFilter;

public class TableMaskFilterEx {
    public static MaskFilter createClipTable(int min, int max) {
        return TableMaskFilter.CreateClipTable(min, max);
    }
}

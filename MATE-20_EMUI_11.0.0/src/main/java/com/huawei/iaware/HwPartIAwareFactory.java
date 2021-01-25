package com.huawei.iaware;

import android.common.HwFrameworkFactory;
import android.graphics.IAwareBitmapCacher;

public class HwPartIAwareFactory {
    private HwPartIAwareFactory() {
    }

    public static IAwareBitmapCacher getHwIAwareBitmapCacher() {
        return HwFrameworkFactory.getHwIAwareBitmapCacher();
    }
}

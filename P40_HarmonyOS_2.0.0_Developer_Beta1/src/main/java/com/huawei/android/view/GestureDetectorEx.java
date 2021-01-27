package com.huawei.android.view;

import android.view.GestureDetector;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class GestureDetectorEx {
    private GestureDetectorEx() {
    }

    public static void setCustomLongpressTimeout(GestureDetector gestureDetector, int time) {
        gestureDetector.setCustomLongpressTimeout(time);
    }
}

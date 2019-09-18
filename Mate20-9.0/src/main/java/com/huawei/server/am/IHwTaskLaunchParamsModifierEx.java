package com.huawei.server.am;

import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.DisplayInfo;
import java.util.HashMap;

public interface IHwTaskLaunchParamsModifierEx {
    HashMap<String, Integer> computeDefaultParaForFreeForm(Rect rect, DisplayMetrics displayMetrics, DisplayInfo displayInfo);
}

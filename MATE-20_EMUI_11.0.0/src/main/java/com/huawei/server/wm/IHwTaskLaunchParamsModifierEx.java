package com.huawei.server.wm;

import android.content.Context;
import com.android.server.wm.ActivityDisplay;
import java.util.HashMap;

public interface IHwTaskLaunchParamsModifierEx {
    HashMap<String, Integer> computeDefaultParaForFreeForm(ActivityDisplay activityDisplay, Context context);
}

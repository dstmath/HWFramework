package com.huawei.server.pc.whiltestrategy;

import android.content.Context;
import java.util.Map;

public interface AppStrategy {
    Map<String, Integer> getAppList(Context context);

    int getAppState(String str, Context context);
}

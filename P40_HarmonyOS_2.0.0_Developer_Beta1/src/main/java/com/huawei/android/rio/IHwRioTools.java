package com.huawei.android.rio;

import android.content.Context;
import com.huawei.android.app.HwRioClientInfoEx;

public interface IHwRioTools {
    void initContext(Context context);

    String loadConfig(HwRioClientInfoEx hwRioClientInfoEx, int i);

    void setHotUpdateFilePath(String str);
}

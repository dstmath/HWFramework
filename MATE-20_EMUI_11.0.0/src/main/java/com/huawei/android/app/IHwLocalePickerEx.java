package com.huawei.android.app;

import android.content.Context;
import java.util.ArrayList;

public interface IHwLocalePickerEx {
    ArrayList<String> getWhiteLanguage(Context context);

    boolean isBlackLanguage(Context context, String str);
}

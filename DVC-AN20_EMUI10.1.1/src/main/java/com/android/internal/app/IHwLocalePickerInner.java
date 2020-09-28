package com.android.internal.app;

import android.content.Context;
import java.util.Set;

public interface IHwLocalePickerInner {
    Set<String> getRealLocaleListEx(Context context, String[] strArr);
}

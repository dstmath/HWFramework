package com.huawei.android.content;

import android.content.ComponentName;

public class ComponentNameEx {
    public static void appendShortString(StringBuilder sb, String packageName, String className) {
        ComponentName.appendShortString(sb, packageName, className);
    }
}

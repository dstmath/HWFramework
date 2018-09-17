package com.huawei.android.widget;

import android.widget.TextCopyFinishedListener;
import android.widget.TextView;

public class TextViewEx {
    public static void trySelectAllAndShowEditor(TextView tv) {
        tv.trySelectAllAndShowEditor();
    }

    public static void addTextCopyFinishedListener(TextView tv, TextCopyFinishedListener listener) {
        tv.addTextCopyFinishedListener(listener);
    }

    public static void setEditorHideFlag(TextView tv, int flag) {
    }
}

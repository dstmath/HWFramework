package com.huawei.android.widget;

import android.widget.TextCopyFinishedListener;
import android.widget.TextView;

public class TextViewEx {
    public static void trySelectAllAndShowEditor(TextView textView) {
        if (textView != null) {
            textView.trySelectAllAndShowEditor();
        }
    }

    public static void addTextCopyFinishedListener(TextView textView, TextCopyFinishedListener listener) {
        if (textView != null) {
            textView.addTextCopyFinishedListener(listener);
        }
    }

    public static void setEditorHideFlag(TextView textView, int flag) {
    }
}

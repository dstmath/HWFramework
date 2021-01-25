package com.huawei.ace.plugin.editing;

public interface TextInputDelegate {
    void performAction(int i, TextInputAction textInputAction);

    void updateEditingState(int i, String str, int i2, int i3, int i4, int i5);
}

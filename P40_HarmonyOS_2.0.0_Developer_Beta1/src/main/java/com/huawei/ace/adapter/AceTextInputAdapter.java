package com.huawei.ace.adapter;

import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class AceTextInputAdapter {
    private InputConnection inputConnection;
    private EditorInfo outAttrs;

    public void setEditorInfo(EditorInfo editorInfo) {
        this.outAttrs = editorInfo;
    }

    public EditorInfo getEditorInfo() {
        return this.outAttrs;
    }

    public void setInputConnection(InputConnection inputConnection2) {
        this.inputConnection = inputConnection2;
    }

    public InputConnection getInputConnection() {
        return this.inputConnection;
    }
}

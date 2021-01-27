package com.huawei.ace.plugin.editing;

import ohos.utils.fastjson.JSONObject;

public class TextEditState {
    private final int selectionEnd;
    private final int selectionStart;
    private final String text;

    public static TextEditState fromJson(JSONObject jSONObject) {
        return new TextEditState(jSONObject.getString("text"), jSONObject.getIntValue("selectionStart"), jSONObject.getIntValue("selectionEnd"));
    }

    private TextEditState(String str, int i, int i2) {
        this.text = str;
        this.selectionStart = i;
        this.selectionEnd = i2;
    }

    public String getText() {
        return this.text;
    }

    public int getSelectionStart() {
        return this.selectionStart;
    }

    public int getSelectionEnd() {
        return this.selectionEnd;
    }
}

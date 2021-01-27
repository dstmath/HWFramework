package com.huawei.ace.plugin.editing;

import ohos.utils.fastjson.JSONObject;

public class TextInputConfiguration {
    private final TextInputAction action;
    private final String actionLabel;
    private final boolean autoCorrect;
    private final String capitalization;
    private final String keyboardAppearance;
    private final boolean obscure;
    private final TextInputType type;

    public static TextInputConfiguration fromJson(JSONObject jSONObject) {
        return new TextInputConfiguration(TextInputType.of(jSONObject.getInteger("type")), jSONObject.getBooleanValue("obscureText"), TextInputAction.of(jSONObject.getInteger("action")), jSONObject.getString("actionLabel"), jSONObject.getBooleanValue("autoCorrect"), jSONObject.getString("capitalization"), jSONObject.getString("keyboardAppearance"));
    }

    public TextInputType getType() {
        return this.type;
    }

    public boolean isObscure() {
        return this.obscure;
    }

    public TextInputAction getAction() {
        return this.action;
    }

    public String getActionLabel() {
        return this.actionLabel;
    }

    public boolean canAutoCorrect() {
        return this.autoCorrect;
    }

    public String getCapitalization() {
        return this.capitalization;
    }

    public String getKeyboardAppearance() {
        return this.keyboardAppearance;
    }

    private TextInputConfiguration(TextInputType textInputType, boolean z, TextInputAction textInputAction, String str, boolean z2, String str2, String str3) {
        this.type = textInputType;
        this.obscure = z;
        this.action = textInputAction;
        this.actionLabel = str;
        this.autoCorrect = z2;
        this.capitalization = str2;
        this.keyboardAppearance = str3;
    }
}

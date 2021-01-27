package com.huawei.ace.plugin.editing;

import com.huawei.ace.runtime.ALog;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.JSONObject;

public abstract class TextInputPluginBase {
    private static final int CLIENT_ID_NONE = -1;
    private static final String LOG_TAG = "Ace_IME";
    private int clientId = -1;
    private TextInputConfiguration config;

    /* access modifiers changed from: private */
    public static native void performAction(int i, int i2);

    /* access modifiers changed from: private */
    public static native void updateEditingState(int i, String str);

    /* access modifiers changed from: protected */
    public abstract void hideTextInput();

    /* access modifiers changed from: protected */
    public native void nativeInit();

    /* access modifiers changed from: protected */
    public void onClosed() {
    }

    /* access modifiers changed from: protected */
    public abstract void onInited();

    /* access modifiers changed from: protected */
    public void onSetTextEditingState(TextEditState textEditState) {
    }

    /* access modifiers changed from: protected */
    public abstract void showTextInput(boolean z);

    public TextInputPluginBase() {
        try {
            nativeInit();
        } catch (UnsatisfiedLinkError unused) {
            ALog.d(LOG_TAG, "nativeInit method is not found.");
        }
    }

    public static class Delegate implements TextInputDelegate {
        @Override // com.huawei.ace.plugin.editing.TextInputDelegate
        public void updateEditingState(int i, String str, int i2, int i3, int i4, int i5) {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("text", str);
            jSONObject.put("selectionStart", Integer.valueOf(i2));
            jSONObject.put("selectionEnd", Integer.valueOf(i3));
            jSONObject.put("composingStart", Integer.valueOf(i4));
            jSONObject.put("composingEnd", Integer.valueOf(i5));
            TextInputPluginBase.updateEditingState(i, jSONObject.toString());
        }

        @Override // com.huawei.ace.plugin.editing.TextInputDelegate
        public void performAction(int i, TextInputAction textInputAction) {
            TextInputPluginBase.performAction(i, textInputAction.getValue());
        }
    }

    /* access modifiers changed from: protected */
    public boolean hasClient() {
        return this.clientId != -1;
    }

    /* access modifiers changed from: protected */
    public int clientId() {
        return this.clientId;
    }

    /* access modifiers changed from: protected */
    public TextInputConfiguration getConfiguration() {
        return this.config;
    }

    private void setTextInputClient(int i, String str) {
        this.clientId = i;
        try {
            JSONObject parseObject = JSON.parseObject(str);
            if (parseObject != null) {
                this.config = TextInputConfiguration.fromJson(parseObject);
            }
        } catch (JSONException unused) {
            ALog.w(LOG_TAG, "failed parse editing config json");
        }
        onInited();
    }

    private void setTextInputEditingState(String str) {
        try {
            JSONObject parseObject = JSON.parseObject(str);
            if (parseObject == null) {
                ALog.w(LOG_TAG, "setTextInputEditingState invalid state");
            } else {
                onSetTextEditingState(TextEditState.fromJson(parseObject));
            }
        } catch (JSONException unused) {
            ALog.w(LOG_TAG, "failed parse editing state json");
        }
    }

    private void clearTextInputClient() {
        this.clientId = -1;
        onClosed();
    }
}

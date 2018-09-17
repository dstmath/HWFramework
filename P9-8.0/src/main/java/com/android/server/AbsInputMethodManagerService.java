package com.android.server;

import android.content.Context;
import android.view.inputmethod.InputMethodInfo;
import com.android.internal.view.IInputMethodManager.Stub;

public abstract class AbsInputMethodManagerService extends Stub {
    public boolean bFlag = false;

    protected boolean isFlagExists(int userId) {
        return true;
    }

    protected void createFlagIfNecessary(int userId) {
    }

    protected void ensureEnableSystemIME(String id, InputMethodInfo imi, Context context, int userId) {
    }

    protected boolean isSecureIME(String packageName) {
        return false;
    }

    protected void updateSecureIMEStatus() {
    }

    protected boolean shouldBuildInputMethodList(String packageName) {
        return true;
    }

    protected void switchUserExtra(int userId) {
    }

    protected int getNaviBarEnabledDefValue() {
        return 1;
    }

    protected void setPanWriteInputEnable(boolean isWriteInput) {
    }
}

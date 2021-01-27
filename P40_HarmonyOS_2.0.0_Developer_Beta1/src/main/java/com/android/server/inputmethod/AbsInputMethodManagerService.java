package com.android.server.inputmethod;

import android.content.Context;
import android.view.inputmethod.InputMethodInfo;
import com.android.internal.view.IInputMethodManager;

public abstract class AbsInputMethodManagerService extends IInputMethodManager.Stub {
    public boolean bFlag = false;

    /* access modifiers changed from: protected */
    public boolean isFlagExists(int userId) {
        return true;
    }

    /* access modifiers changed from: protected */
    public void createFlagIfNecessary(int userId) {
    }

    /* access modifiers changed from: protected */
    public boolean ensureEnableSystemIME(String id, InputMethodInfo imi, Context context, int userId) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isSecureIME(String packageName) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void updateSecureIMEStatus() {
    }

    /* access modifiers changed from: protected */
    public boolean shouldBuildInputMethodList(String packageName) {
        return true;
    }

    /* access modifiers changed from: protected */
    public void switchUserExtra(int userId) {
    }

    /* access modifiers changed from: protected */
    public int getNaviBarEnabledDefValue() {
        return 1;
    }
}

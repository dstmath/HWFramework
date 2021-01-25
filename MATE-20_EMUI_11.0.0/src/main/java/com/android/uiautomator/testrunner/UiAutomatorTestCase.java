package com.android.uiautomator.testrunner;

import android.app.ActivityThread;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import com.android.uiautomator.core.UiDevice;
import junit.framework.TestCase;

@Deprecated
public class UiAutomatorTestCase extends TestCase {
    private static final String DISABLE_IME = "disable_ime";
    private static final String DUMMY_IME_PACKAGE = "com.android.testing.dummyime";
    private static final int NOT_A_SUBTYPE_ID = -1;
    private IAutomationSupport mAutomationSupport;
    private Bundle mParams;
    private boolean mShouldDisableIme = false;
    private UiDevice mUiDevice;

    /* access modifiers changed from: protected */
    @Override // junit.framework.TestCase
    public void setUp() throws Exception {
        super.setUp();
        this.mShouldDisableIme = "true".equals(this.mParams.getString(DISABLE_IME));
        if (this.mShouldDisableIme) {
            setDummyIme();
        }
    }

    /* access modifiers changed from: protected */
    @Override // junit.framework.TestCase
    public void tearDown() throws Exception {
        if (this.mShouldDisableIme) {
            restoreActiveIme();
        }
        super.tearDown();
    }

    public UiDevice getUiDevice() {
        return this.mUiDevice;
    }

    public Bundle getParams() {
        return this.mParams;
    }

    public IAutomationSupport getAutomationSupport() {
        return this.mAutomationSupport;
    }

    /* access modifiers changed from: package-private */
    public void setUiDevice(UiDevice uiDevice) {
        this.mUiDevice = uiDevice;
    }

    /* access modifiers changed from: package-private */
    public void setParams(Bundle params) {
        this.mParams = params;
    }

    /* access modifiers changed from: package-private */
    public void setAutomationSupport(IAutomationSupport automationSupport) {
        this.mAutomationSupport = automationSupport;
    }

    public void sleep(long ms) {
        SystemClock.sleep(ms);
    }

    private void setDummyIme() {
        Context context = ActivityThread.currentApplication();
        if (context != null) {
            String id = null;
            for (InputMethodInfo info : ((InputMethodManager) context.getSystemService("input_method")).getInputMethodList()) {
                if (DUMMY_IME_PACKAGE.equals(info.getComponent().getPackageName())) {
                    id = info.getId();
                }
            }
            if (id == null) {
                throw new RuntimeException(String.format("Required testing fixture missing: IME package (%s)", DUMMY_IME_PACKAGE));
            } else if (context.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == 0) {
                ContentResolver resolver = context.getContentResolver();
                Settings.Secure.putInt(resolver, "selected_input_method_subtype", NOT_A_SUBTYPE_ID);
                Settings.Secure.putString(resolver, "default_input_method", id);
            }
        } else {
            throw new RuntimeException("ActivityThread.currentApplication() is null.");
        }
    }

    private void restoreActiveIme() {
    }
}

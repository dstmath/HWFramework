package com.android.uiautomator.testrunner;

import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.view.inputmethod.InputMethodInfo;
import com.android.internal.view.IInputMethodManager;
import com.android.uiautomator.core.UiDevice;
import junit.framework.TestCase;

@Deprecated
public class UiAutomatorTestCase extends TestCase {
    private static final String DISABLE_IME = "disable_ime";
    private static final String DUMMY_IME_PACKAGE = "com.android.testing.dummyime";
    private IAutomationSupport mAutomationSupport;
    private Bundle mParams;
    private boolean mShouldDisableIme = false;
    private UiDevice mUiDevice;

    /* access modifiers changed from: protected */
    public void setUp() throws Exception {
        super.setUp();
        this.mShouldDisableIme = "true".equals(this.mParams.getString(DISABLE_IME));
        if (this.mShouldDisableIme) {
            setDummyIme();
        }
    }

    /* access modifiers changed from: protected */
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

    private void setDummyIme() throws RemoteException {
        IInputMethodManager im = IInputMethodManager.Stub.asInterface(ServiceManager.getService("input_method"));
        String id = null;
        for (InputMethodInfo info : im.getInputMethodList()) {
            if (DUMMY_IME_PACKAGE.equals(info.getComponent().getPackageName())) {
                id = info.getId();
            }
        }
        if (id != null) {
            im.setInputMethod(null, id);
        } else {
            throw new RuntimeException(String.format("Required testing fixture missing: IME package (%s)", new Object[]{DUMMY_IME_PACKAGE}));
        }
    }

    private void restoreActiveIme() throws RemoteException {
    }
}

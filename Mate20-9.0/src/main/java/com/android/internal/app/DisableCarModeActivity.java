package com.android.internal.app;

import android.app.Activity;
import android.app.IUiModeManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class DisableCarModeActivity extends Activity {
    private static final String TAG = "DisableCarModeActivity";

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            IUiModeManager.Stub.asInterface(ServiceManager.getService("uimode")).disableCarMode(1);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to disable car mode", e);
        }
        finish();
    }
}

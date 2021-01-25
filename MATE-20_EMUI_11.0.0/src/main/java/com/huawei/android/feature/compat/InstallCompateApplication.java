package com.huawei.android.feature.compat;

import android.app.Application;
import android.content.Context;

public class InstallCompateApplication extends Application {
    /* access modifiers changed from: protected */
    @Override // android.content.ContextWrapper
    public void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        InstallCompat.install(context);
    }
}

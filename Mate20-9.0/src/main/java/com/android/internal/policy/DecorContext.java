package com.android.internal.policy;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;
import android.view.WindowManagerImpl;
import java.lang.ref.WeakReference;

class DecorContext extends ContextThemeWrapper {
    private WeakReference<Context> mActivityContext;
    private Resources mActivityResources;
    private PhoneWindow mPhoneWindow;
    private WindowManager mWindowManager;

    public DecorContext(Context context, Context activityContext) {
        super(context, null);
        this.mActivityContext = new WeakReference<>(activityContext);
        this.mActivityResources = activityContext.getResources();
    }

    /* access modifiers changed from: package-private */
    public void setPhoneWindow(PhoneWindow phoneWindow) {
        this.mPhoneWindow = phoneWindow;
        this.mWindowManager = null;
    }

    public Object getSystemService(String name) {
        if (!"window".equals(name)) {
            return super.getSystemService(name);
        }
        if (this.mWindowManager == null) {
            this.mWindowManager = ((WindowManagerImpl) super.getSystemService("window")).createLocalWindowManager(this.mPhoneWindow);
        }
        return this.mWindowManager;
    }

    public Resources getResources() {
        Context activityContext = (Context) this.mActivityContext.get();
        if (activityContext != null) {
            this.mActivityResources = activityContext.getResources();
        }
        return this.mActivityResources;
    }

    public AssetManager getAssets() {
        return this.mActivityResources.getAssets();
    }
}

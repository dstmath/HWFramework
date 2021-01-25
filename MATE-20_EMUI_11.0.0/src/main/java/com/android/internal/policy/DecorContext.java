package com.android.internal.policy;

import android.content.AutofillOptions;
import android.content.ContentCaptureOptions;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;
import android.view.WindowManagerImpl;
import android.view.contentcapture.ContentCaptureManager;
import com.android.internal.annotations.VisibleForTesting;
import java.lang.ref.WeakReference;

@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public class DecorContext extends ContextThemeWrapper {
    private WeakReference<Context> mActivityContext;
    private Resources mActivityResources;
    private ContentCaptureManager mContentCaptureManager;
    private PhoneWindow mPhoneWindow;
    private WindowManager mWindowManager;

    @VisibleForTesting
    public DecorContext(Context context, Context activityContext) {
        super(context.createDisplayContext(activityContext.getDisplay()), (Resources.Theme) null);
        this.mActivityContext = new WeakReference<>(activityContext);
        this.mActivityResources = activityContext.getResources();
    }

    /* access modifiers changed from: package-private */
    public void setPhoneWindow(PhoneWindow phoneWindow) {
        this.mPhoneWindow = phoneWindow;
        this.mWindowManager = null;
    }

    @Override // android.view.ContextThemeWrapper, android.content.ContextWrapper, android.content.Context
    public Object getSystemService(String name) {
        Context activityContext;
        if ("window".equals(name)) {
            if (this.mWindowManager == null) {
                this.mWindowManager = ((WindowManagerImpl) super.getSystemService("window")).createLocalWindowManager(this.mPhoneWindow);
            }
            return this.mWindowManager;
        } else if (!"content_capture".equals(name)) {
            return super.getSystemService(name);
        } else {
            if (this.mContentCaptureManager == null && (activityContext = this.mActivityContext.get()) != null) {
                this.mContentCaptureManager = (ContentCaptureManager) activityContext.getSystemService(name);
            }
            return this.mContentCaptureManager;
        }
    }

    @Override // android.view.ContextThemeWrapper, android.content.ContextWrapper, android.content.Context
    public Resources getResources() {
        Context activityContext = this.mActivityContext.get();
        if (activityContext != null) {
            this.mActivityResources = activityContext.getResources();
        }
        return this.mActivityResources;
    }

    @Override // android.view.ContextThemeWrapper, android.content.ContextWrapper, android.content.Context
    public AssetManager getAssets() {
        return this.mActivityResources.getAssets();
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public AutofillOptions getAutofillOptions() {
        Context activityContext = this.mActivityContext.get();
        if (activityContext != null) {
            return activityContext.getAutofillOptions();
        }
        return null;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public ContentCaptureOptions getContentCaptureOptions() {
        Context activityContext = this.mActivityContext.get();
        if (activityContext != null) {
            return activityContext.getContentCaptureOptions();
        }
        return null;
    }
}

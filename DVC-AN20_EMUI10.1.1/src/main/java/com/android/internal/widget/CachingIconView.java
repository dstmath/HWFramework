package com.android.internal.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.widget.ImageView;
import android.widget.RemoteViews;
import java.util.Objects;

@RemoteViews.RemoteView
public class CachingIconView extends ImageView {
    private int mDesiredVisibility;
    private boolean mForceHidden;
    private boolean mInternalSetDrawable;
    private String mLastPackage;
    private int mLastResId;

    @UnsupportedAppUsage
    public CachingIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override // android.widget.ImageView
    @RemotableViewMethod(asyncImpl = "setImageIconAsync")
    public void setImageIcon(Icon icon) {
        if (!testAndSetCache(icon)) {
            this.mInternalSetDrawable = true;
            super.setImageIcon(icon);
            this.mInternalSetDrawable = false;
        }
    }

    @Override // android.widget.ImageView
    public Runnable setImageIconAsync(Icon icon) {
        resetCache();
        return super.setImageIconAsync(icon);
    }

    @Override // android.widget.ImageView
    @RemotableViewMethod(asyncImpl = "setImageResourceAsync")
    public void setImageResource(int resId) {
        if (!testAndSetCache(resId)) {
            this.mInternalSetDrawable = true;
            super.setImageResource(resId);
            this.mInternalSetDrawable = false;
        }
    }

    @Override // android.widget.ImageView
    public Runnable setImageResourceAsync(int resId) {
        resetCache();
        return super.setImageResourceAsync(resId);
    }

    @Override // android.widget.ImageView
    @RemotableViewMethod(asyncImpl = "setImageURIAsync")
    public void setImageURI(Uri uri) {
        resetCache();
        super.setImageURI(uri);
    }

    @Override // android.widget.ImageView
    public Runnable setImageURIAsync(Uri uri) {
        resetCache();
        return super.setImageURIAsync(uri);
    }

    @Override // android.widget.ImageView
    public void setImageDrawable(Drawable drawable) {
        if (!this.mInternalSetDrawable) {
            resetCache();
        }
        super.setImageDrawable(drawable);
    }

    @Override // android.widget.ImageView
    @RemotableViewMethod
    public void setImageBitmap(Bitmap bm) {
        resetCache();
        super.setImageBitmap(bm);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetCache();
    }

    private synchronized boolean testAndSetCache(Icon icon) {
        boolean isCached = false;
        if (icon != null) {
            if (icon.getType() == 2) {
                String iconPackage = normalizeIconPackage(icon);
                if (this.mLastResId != 0 && icon.getResId() == this.mLastResId && Objects.equals(iconPackage, this.mLastPackage)) {
                    isCached = true;
                }
                this.mLastPackage = iconPackage;
                this.mLastResId = icon.getResId();
                return isCached;
            }
        }
        resetCache();
        return false;
    }

    private synchronized boolean testAndSetCache(int resId) {
        boolean isCached;
        if (resId != 0) {
            try {
                if (this.mLastResId != 0) {
                    isCached = resId == this.mLastResId && this.mLastPackage == null;
                    this.mLastPackage = null;
                    this.mLastResId = resId;
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        isCached = false;
        this.mLastPackage = null;
        this.mLastResId = resId;
        return isCached;
    }

    private String normalizeIconPackage(Icon icon) {
        if (icon == null) {
            return null;
        }
        String pkg = icon.getResPackage();
        if (!TextUtils.isEmpty(pkg) && !pkg.equals(this.mContext.getPackageName())) {
            return pkg;
        }
        return null;
    }

    private synchronized void resetCache() {
        this.mLastResId = 0;
        this.mLastPackage = null;
    }

    public void setForceHidden(boolean forceHidden) {
        this.mForceHidden = forceHidden;
        updateVisibility();
    }

    @Override // android.widget.ImageView, android.view.View
    @RemotableViewMethod
    public void setVisibility(int visibility) {
        this.mDesiredVisibility = visibility;
        updateVisibility();
    }

    private void updateVisibility() {
        int visibility;
        if (this.mDesiredVisibility != 0 || !this.mForceHidden) {
            visibility = this.mDesiredVisibility;
        } else {
            visibility = 4;
        }
        super.setVisibility(visibility);
    }
}

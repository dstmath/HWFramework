package com.android.internal.widget;

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
import android.widget.RemoteViews.RemoteView;
import libcore.util.Objects;

@RemoteView
public class CachingIconView extends ImageView {
    private int mDesiredVisibility;
    private boolean mForceHidden;
    private boolean mInternalSetDrawable;
    private String mLastPackage;
    private int mLastResId;

    public CachingIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @RemotableViewMethod(asyncImpl = "setImageIconAsync")
    public void setImageIcon(Icon icon) {
        if (!testAndSetCache(icon)) {
            this.mInternalSetDrawable = true;
            super.setImageIcon(icon);
            this.mInternalSetDrawable = false;
        }
    }

    public Runnable setImageIconAsync(Icon icon) {
        resetCache();
        return super.setImageIconAsync(icon);
    }

    @RemotableViewMethod(asyncImpl = "setImageResourceAsync")
    public void setImageResource(int resId) {
        if (!testAndSetCache(resId)) {
            this.mInternalSetDrawable = true;
            super.setImageResource(resId);
            this.mInternalSetDrawable = false;
        }
    }

    public Runnable setImageResourceAsync(int resId) {
        resetCache();
        return super.setImageResourceAsync(resId);
    }

    @RemotableViewMethod(asyncImpl = "setImageURIAsync")
    public void setImageURI(Uri uri) {
        resetCache();
        super.setImageURI(uri);
    }

    public Runnable setImageURIAsync(Uri uri) {
        resetCache();
        return super.setImageURIAsync(uri);
    }

    public void setImageDrawable(Drawable drawable) {
        if (!this.mInternalSetDrawable) {
            resetCache();
        }
        super.setImageDrawable(drawable);
    }

    @RemotableViewMethod
    public void setImageBitmap(Bitmap bm) {
        resetCache();
        super.setImageBitmap(bm);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetCache();
    }

    private synchronized boolean testAndSetCache(Icon icon) {
        if (icon != null) {
            if (icon.getType() == 2) {
                boolean isCached;
                String iconPackage = normalizeIconPackage(icon);
                if (this.mLastResId == 0 || icon.getResId() != this.mLastResId) {
                    isCached = false;
                } else {
                    isCached = Objects.equal(iconPackage, this.mLastPackage);
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
            if (this.mLastResId != 0) {
                isCached = resId == this.mLastResId && this.mLastPackage == null;
                this.mLastPackage = null;
                this.mLastResId = resId;
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
        if (TextUtils.isEmpty(pkg) || pkg.equals(this.mContext.getPackageName())) {
            return null;
        }
        return pkg;
    }

    private synchronized void resetCache() {
        this.mLastResId = 0;
        this.mLastPackage = null;
    }

    public void setForceHidden(boolean forceHidden) {
        this.mForceHidden = forceHidden;
        updateVisibility();
    }

    @RemotableViewMethod
    public void setVisibility(int visibility) {
        this.mDesiredVisibility = visibility;
        updateVisibility();
    }

    private void updateVisibility() {
        int visibility;
        if (this.mDesiredVisibility == 0 && this.mForceHidden) {
            visibility = 4;
        } else {
            visibility = this.mDesiredVisibility;
        }
        super.setVisibility(visibility);
    }
}

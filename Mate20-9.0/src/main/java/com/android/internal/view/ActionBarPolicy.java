package com.android.internal.view;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import com.android.internal.R;

public class ActionBarPolicy {
    private Context mContext;

    public static ActionBarPolicy get(Context context) {
        return new ActionBarPolicy(context);
    }

    private ActionBarPolicy(Context context) {
        this.mContext = context;
    }

    public int getMaxActionButtons() {
        Configuration config = this.mContext.getResources().getConfiguration();
        int width = config.screenWidthDp;
        int height = config.screenHeightDp;
        if (config.smallestScreenWidthDp > 600 || ((width > 960 && height > 720) || (width > 720 && height > 960))) {
            return 5;
        }
        if (width >= 500 || ((width > 640 && height > 480) || (width > 480 && height > 640))) {
            return 4;
        }
        if (width >= 360) {
            return 3;
        }
        return 2;
    }

    public boolean showsOverflowMenuButton() {
        return true;
    }

    public int getEmbeddedMenuWidthLimit() {
        return this.mContext.getResources().getDisplayMetrics().widthPixels / 2;
    }

    public boolean hasEmbeddedTabs() {
        if (this.mContext.getApplicationInfo().targetSdkVersion >= 16) {
            return this.mContext.getResources().getBoolean(17956865);
        }
        Configuration configuration = this.mContext.getResources().getConfiguration();
        int width = configuration.screenWidthDp;
        return configuration.orientation == 2 || width >= 480 || (width >= 640 && configuration.screenHeightDp >= 480);
    }

    public int getTabContainerHeight() {
        TypedArray a = this.mContext.obtainStyledAttributes(null, R.styleable.ActionBar, 16843470, 0);
        int height = a.getLayoutDimension(4, 0);
        Resources r = this.mContext.getResources();
        if (!hasEmbeddedTabs()) {
            height = Math.min(height, r.getDimensionPixelSize(17104916));
        }
        a.recycle();
        return height;
    }

    public boolean enableHomeButtonByDefault() {
        return this.mContext.getApplicationInfo().targetSdkVersion < 14;
    }

    public int getStackedTabMaxWidth() {
        return this.mContext.getResources().getDimensionPixelSize(17104917);
    }
}

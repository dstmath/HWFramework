package com.android.internal.view;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import com.android.internal.R;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

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
        if (config.smallestScreenWidthDp > 600 || ((width > 960 && height > MetricsEvent.ACTION_PERMISSION_DENIED_RECEIVE_WAP_PUSH) || (width > MetricsEvent.ACTION_PERMISSION_DENIED_RECEIVE_WAP_PUSH && height > 960))) {
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
        boolean z = true;
        if (this.mContext.getApplicationInfo().targetSdkVersion >= 16) {
            return this.mContext.getResources().getBoolean(R.bool.action_bar_embed_tabs);
        }
        Configuration configuration = this.mContext.getResources().getConfiguration();
        int width = configuration.screenWidthDp;
        int height = configuration.screenHeightDp;
        if (configuration.orientation != 2 && width < 480 && (width < 640 || height < 480)) {
            z = false;
        }
        return z;
    }

    public int getTabContainerHeight() {
        TypedArray a = this.mContext.obtainStyledAttributes(null, R.styleable.ActionBar, R.attr.actionBarStyle, 0);
        int height = a.getLayoutDimension(4, 0);
        Resources r = this.mContext.getResources();
        if (!hasEmbeddedTabs()) {
            height = Math.min(height, r.getDimensionPixelSize(R.dimen.action_bar_stacked_max_height));
        }
        a.recycle();
        return height;
    }

    public boolean enableHomeButtonByDefault() {
        return this.mContext.getApplicationInfo().targetSdkVersion < 14;
    }

    public int getStackedTabMaxWidth() {
        return this.mContext.getResources().getDimensionPixelSize(R.dimen.action_bar_stacked_tab_max_width);
    }
}

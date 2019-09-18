package com.android.server.forcerotation;

import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import com.android.server.pm.PackageManagerService;

public class HwForceRotationLayout implements View.OnSystemUiVisibilityChangeListener {
    private static final String KEY_ENABLE_NAVBAR_DB = "enable_navbar";
    private static final String TAG = "ForceRotationLayout";
    private final int NAV_BAR_HEIGHT_LAND;
    private final int STATUS_BAR_HEIGHT;
    /* access modifiers changed from: private */
    public Context mContext;
    private Display mDisplay;
    /* access modifiers changed from: private */
    public boolean mHwHideNavBar;
    private Rect mLayoutFrame;
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            HwForceRotationLayout hwForceRotationLayout = HwForceRotationLayout.this;
            boolean z = false;
            if (Settings.System.getInt(HwForceRotationLayout.this.mContext.getContentResolver(), HwForceRotationLayout.KEY_ENABLE_NAVBAR_DB, 0) <= 0) {
                z = true;
            }
            boolean unused = hwForceRotationLayout.mHwHideNavBar = z;
            HwForceRotationLayout.this.calculateFrame();
        }
    };
    private int mSplitNavigationBarDp;
    private int mSystemUiVisibility;

    public HwForceRotationLayout(Context context, Handler uiHandler, HwForceRotationManagerService service) {
        this.mContext = context;
        this.STATUS_BAR_HEIGHT = getResource(context, "status_bar_height", 48);
        this.NAV_BAR_HEIGHT_LAND = getResource(context, "navigation_bar_width", 96);
        this.mHwHideNavBar = Settings.System.getInt(this.mContext.getContentResolver(), KEY_ENABLE_NAVBAR_DB, 0) <= 0;
        this.mSplitNavigationBarDp = 600;
        calculateFrame();
        registerObserver();
    }

    /* access modifiers changed from: private */
    public void calculateFrame() {
        this.mLayoutFrame = new Rect();
        this.mDisplay = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        this.mDisplay.getRealMetrics(dm);
        if (dm.widthPixels >= dm.heightPixels) {
            int shortSize = dm.heightPixels;
            int longSize = dm.widthPixels;
            boolean mNavigationBarCanMove = (shortSize * 160) / dm.densityDpi <= this.mSplitNavigationBarDp;
            int statusBarH = getStatusBarHeight();
            int navBarH = getNavBarHeight();
            Slog.v(TAG, "statusBarH = " + statusBarH + ", navBarH = " + navBarH + ", mNavigationBarCanMove =" + mNavigationBarCanMove);
            if (mNavigationBarCanMove) {
                this.mLayoutFrame.left = ((longSize - shortSize) - navBarH) / 2;
                this.mLayoutFrame.top = statusBarH;
                this.mLayoutFrame.right = ((longSize + shortSize) - navBarH) / 2;
                this.mLayoutFrame.bottom = shortSize;
            } else {
                this.mLayoutFrame.left = (longSize - shortSize) / 2;
                this.mLayoutFrame.top = statusBarH;
                this.mLayoutFrame.right = this.mLayoutFrame.left + shortSize;
                this.mLayoutFrame.bottom = shortSize - navBarH;
            }
        }
    }

    public Rect getForceRotationLayout() {
        if (this.mLayoutFrame == null || this.mLayoutFrame.width() == 0 || this.mLayoutFrame.height() == 0 || this.mLayoutFrame.left == 0) {
            calculateFrame();
        }
        return this.mLayoutFrame;
    }

    public int getStatusBarHeight() {
        return this.STATUS_BAR_HEIGHT;
    }

    public int getNavBarHeight() {
        if (this.mHwHideNavBar || this.mSystemUiVisibility != 0) {
            return 0;
        }
        return this.NAV_BAR_HEIGHT_LAND;
    }

    private int getResource(Context context, String name, int defaultVal) {
        Resources resources = context.getResources();
        int val = defaultVal;
        int resourceId = resources.getIdentifier(name, "dimen", PackageManagerService.PLATFORM_PACKAGE_NAME);
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return val;
    }

    public void onSystemUiVisibilityChange(int visibility) {
        this.mSystemUiVisibility = visibility;
        calculateFrame();
    }

    private void registerObserver() {
        if (this.mContext != null) {
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(KEY_ENABLE_NAVBAR_DB), true, this.mObserver);
        }
    }
}

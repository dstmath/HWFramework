package android.util;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.os.UserHandle;
import android.os.UserManager;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;

public class IconDrawableFactory {
    @VisibleForTesting
    public static final int[] CORP_BADGE_COLORS = {R.color.profile_badge_1, R.color.profile_badge_2, R.color.profile_badge_3};
    protected final Context mContext;
    protected final boolean mEmbedShadow;
    protected final LauncherIcons mLauncherIcons;
    protected final PackageManager mPm;
    protected final UserManager mUm;

    private IconDrawableFactory(Context context, boolean embedShadow) {
        this.mContext = context;
        this.mPm = context.getPackageManager();
        this.mUm = (UserManager) context.getSystemService(UserManager.class);
        this.mLauncherIcons = new LauncherIcons(context);
        this.mEmbedShadow = embedShadow;
    }

    /* access modifiers changed from: protected */
    public boolean needsBadging(ApplicationInfo appInfo, int userId) {
        return appInfo.isInstantApp() || this.mUm.isManagedProfile(userId);
    }

    @UnsupportedAppUsage
    public Drawable getBadgedIcon(ApplicationInfo appInfo) {
        return getBadgedIcon(appInfo, UserHandle.getUserId(appInfo.uid));
    }

    public Drawable getBadgedIcon(ApplicationInfo appInfo, int userId) {
        return getBadgedIcon(appInfo, appInfo, userId);
    }

    @UnsupportedAppUsage
    public Drawable getBadgedIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo, int userId) {
        Drawable icon = this.mPm.loadUnbadgedItemIcon(itemInfo, appInfo);
        if (isClonedProfile(userId)) {
            return HwThemeManager.getClonedDrawable(this.mContext, icon);
        }
        if (!this.mEmbedShadow && !needsBadging(appInfo, userId)) {
            return icon;
        }
        Drawable icon2 = getShadowedIcon(icon);
        if (appInfo.isInstantApp()) {
            icon2 = this.mLauncherIcons.getBadgedDrawable(icon2, R.drawable.ic_instant_icon_badge_bolt, Resources.getSystem().getColor(R.color.instant_app_badge, null));
        }
        if (this.mUm.isManagedProfile(userId)) {
            return this.mLauncherIcons.getBadgedDrawable(icon2, R.drawable.ic_corp_icon_badge_case, getUserBadgeColor(this.mUm, userId));
        }
        return icon2;
    }

    public Drawable getShadowedIcon(Drawable icon) {
        return this.mLauncherIcons.wrapIconDrawableWithShadow(icon);
    }

    public static int getUserBadgeColor(UserManager um, int userId) {
        int badge = um.getManagedProfileBadge(userId);
        if (badge < 0) {
            badge = 0;
        }
        int[] iArr = CORP_BADGE_COLORS;
        return Resources.getSystem().getColor(iArr[badge % iArr.length], null);
    }

    @UnsupportedAppUsage
    public static IconDrawableFactory newInstance(Context context) {
        return new IconDrawableFactory(context, true);
    }

    public static IconDrawableFactory newInstance(Context context, boolean embedShadow) {
        return new IconDrawableFactory(context, embedShadow);
    }

    private boolean isClonedProfile(int userId) {
        if (userId == 0) {
            return false;
        }
        try {
            return this.mUm.getUserInfo(userId).isClonedProfile();
        } catch (Exception e) {
            return false;
        }
    }
}

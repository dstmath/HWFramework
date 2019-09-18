package android.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.os.UserHandle;
import android.os.UserManager;
import com.android.internal.annotations.VisibleForTesting;

public class IconDrawableFactory {
    @VisibleForTesting
    public static final int[] CORP_BADGE_COLORS = {17170733, 17170734, 17170735};
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

    public Drawable getBadgedIcon(ApplicationInfo appInfo) {
        return getBadgedIcon(appInfo, UserHandle.getUserId(appInfo.uid));
    }

    public Drawable getBadgedIcon(ApplicationInfo appInfo, int userId) {
        return getBadgedIcon(appInfo, appInfo, userId);
    }

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
            icon2 = this.mLauncherIcons.getBadgedDrawable(icon2, 17302407, Resources.getSystem().getColor(17170613, null));
        }
        if (this.mUm.isManagedProfile(userId)) {
            icon2 = this.mLauncherIcons.getBadgedDrawable(icon2, 17302343, getUserBadgeColor(this.mUm, userId));
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
        return Resources.getSystem().getColor(CORP_BADGE_COLORS[badge % CORP_BADGE_COLORS.length], null);
    }

    public static IconDrawableFactory newInstance(Context context) {
        return new IconDrawableFactory(context, true);
    }

    public static IconDrawableFactory newInstance(Context context, boolean embedShadow) {
        return new IconDrawableFactory(context, embedShadow);
    }

    private boolean isClonedProfile(int userId) {
        boolean isClonedProfile = false;
        if (userId == 0) {
            return false;
        }
        try {
            isClonedProfile = this.mUm.getUserInfo(userId).isClonedProfile();
        } catch (Exception e) {
        }
        return isClonedProfile;
    }
}

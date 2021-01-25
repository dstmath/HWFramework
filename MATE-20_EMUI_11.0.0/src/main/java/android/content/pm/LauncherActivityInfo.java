package android.content.pm;

import android.annotation.UnsupportedAppUsage;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;

public class LauncherActivityInfo {
    private static final String TAG = "LauncherActivityInfo";
    @UnsupportedAppUsage
    private ActivityInfo mActivityInfo;
    private ComponentName mComponentName;
    private final PackageManager mPm;
    private UserHandle mUser;

    LauncherActivityInfo(Context context, ActivityInfo info, UserHandle user) {
        this(context);
        this.mActivityInfo = info;
        this.mComponentName = new ComponentName(info.packageName, info.name);
        this.mUser = user;
    }

    LauncherActivityInfo(Context context) {
        this.mPm = context.getPackageManager();
    }

    public ComponentName getComponentName() {
        return this.mComponentName;
    }

    public UserHandle getUser() {
        return this.mUser;
    }

    public CharSequence getLabel() {
        return this.mActivityInfo.loadLabel(this.mPm);
    }

    public Drawable getIcon(int density) {
        int iconRes = this.mActivityInfo.getIconResource();
        Drawable icon = null;
        if (!(density == 0 || iconRes == 0)) {
            try {
                icon = this.mPm.getResourcesForApplication(this.mActivityInfo.applicationInfo).getDrawableForDensity(iconRes, density);
            } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            }
        }
        if (icon == null) {
            return this.mActivityInfo.loadIcon(this.mPm);
        }
        return icon;
    }

    public int getApplicationFlags() {
        return this.mActivityInfo.applicationInfo.flags;
    }

    public ApplicationInfo getApplicationInfo() {
        return this.mActivityInfo.applicationInfo;
    }

    public long getFirstInstallTime() {
        try {
            return this.mPm.getPackageInfo(this.mActivityInfo.packageName, 8192).firstInstallTime;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    public String getName() {
        return this.mActivityInfo.name;
    }

    public Drawable getBadgedIcon(int density) {
        return this.mPm.getUserBadgedIcon(getIcon(density), this.mUser);
    }
}

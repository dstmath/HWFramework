package android.webkit;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.os.UserManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class UserPackage {
    private static final String TAG = "UserPackage";
    private final PackageInfo mPackageInfo;
    private final UserInfo mUserInfo;

    public UserPackage(UserInfo user, PackageInfo packageInfo) {
        this.mUserInfo = user;
        this.mPackageInfo = packageInfo;
    }

    public static List<UserPackage> getPackageInfosAllUsers(Context context, String packageName, int packageFlags) {
        List<UserInfo> users = getAllUsers(context);
        List<UserPackage> userPackages = new ArrayList(users.size());
        for (UserInfo user : users) {
            PackageInfo packageInfo = null;
            try {
                packageInfo = context.getPackageManager().getPackageInfoAsUser(packageName, packageFlags, user.id);
            } catch (NameNotFoundException e) {
                Log.e(TAG, "Could not find package in function getPackageInfosAllUsers");
            }
            userPackages.add(new UserPackage(user, packageInfo));
        }
        return userPackages;
    }

    public boolean isEnabledPackage() {
        if (this.mPackageInfo == null) {
            return false;
        }
        return this.mPackageInfo.applicationInfo.enabled;
    }

    public boolean isInstalledPackage() {
        boolean z = false;
        if (this.mPackageInfo == null) {
            return false;
        }
        if ((this.mPackageInfo.applicationInfo.flags & 8388608) != 0 && (this.mPackageInfo.applicationInfo.privateFlags & 1) == 0) {
            z = true;
        }
        return z;
    }

    public static boolean hasCorrectTargetSdkVersion(PackageInfo packageInfo) {
        return packageInfo.applicationInfo.targetSdkVersion >= 26;
    }

    public UserInfo getUserInfo() {
        return this.mUserInfo;
    }

    public PackageInfo getPackageInfo() {
        return this.mPackageInfo;
    }

    private static List<UserInfo> getAllUsers(Context context) {
        return ((UserManager) context.getSystemService("user")).getUsers(false);
    }
}

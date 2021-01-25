package com.huawei.android.os;

import android.content.pm.UserInfo;
import android.graphics.Bitmap;
import android.os.IUserManager;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.huawei.android.content.pm.UserInfoEx;
import java.util.ArrayList;
import java.util.List;

public class UserManagerEx {
    public static final int FLAG_HW_HIDDENSPACE = 33554432;
    public static final int MKDIR_FOR_USER_TRANSACTION = 1001;
    private static final int PARENT_ID = 2147483646;
    private static final int[] SYSTEM_RESERVED_USERIDS = {PARENT_ID};
    public static final String TAG = "UserManagerEx";

    public static boolean isHwHiddenSpace(UserInfoEx userInfoEx) {
        return (userInfoEx == null || userInfoEx.getUserInfo() == null || (userInfoEx.getUserInfo().flags & 33554432) != 33554432) ? false : true;
    }

    public static Bitmap getUserIcon(UserManager userManager, int userHandle) {
        if (userManager != null) {
            return userManager.getUserIcon(userHandle);
        }
        return null;
    }

    public static UserInfoEx getUserInfoEx(UserManager userManager, int userHandle) {
        UserInfo userInfo;
        if (userManager == null || (userInfo = userManager.getUserInfo(userHandle)) == null) {
            return null;
        }
        return new UserInfoEx(userInfo);
    }

    public static List<UserInfoEx> getProfiles(UserManager userManager, int userHandle) {
        List<UserInfoEx> exProfiles = new ArrayList<>();
        if (userManager == null) {
            return exProfiles;
        }
        for (UserInfo profile : userManager.getProfiles(userHandle)) {
            if (profile != null) {
                exProfiles.add(new UserInfoEx(profile));
            }
        }
        return exProfiles;
    }

    public static UserInfoEx createUser(UserManager userManager, String name, int flags) {
        UserInfo info;
        if (userManager == null || (info = userManager.createUser(name, flags)) == null) {
            return null;
        }
        return new UserInfoEx(info);
    }

    public static boolean removeUser(UserManager userManager, int userHandle) {
        if (userManager != null) {
            return userManager.removeUser(userHandle);
        }
        return false;
    }

    public static UserInfoEx createProfileForUser(UserManager userManager, String name, int flags, int userHandle) {
        UserInfo userInfo;
        if (userManager == null || (userInfo = userManager.createProfileForUser(name, flags, userHandle)) == null) {
            return null;
        }
        return new UserInfoEx(userInfo);
    }

    public static String getUserInfoName(UserInfoEx userInfoEx) {
        if (userInfoEx == null || userInfoEx.getUserInfo() == null) {
            return null;
        }
        return userInfoEx.getUserInfo().name;
    }

    public static boolean isUserInfoValid(UserInfoEx userInfoEx) {
        if (userInfoEx == null || userInfoEx.getUserInfo() == null) {
            return false;
        }
        return true;
    }

    public static int getUserInfoId(UserInfoEx userInfoEx) {
        if (userInfoEx == null || userInfoEx.getUserInfo() == null) {
            return UserHandleEx.getUndefinedUserId();
        }
        return userInfoEx.getUserInfo().id;
    }

    public boolean isUserInfoManagedProfile(UserInfoEx userInfoEx) {
        if (userInfoEx == null || userInfoEx.getUserInfo() == null) {
            return false;
        }
        return userInfoEx.getUserInfo().isManagedProfile();
    }

    public boolean isUserInfoClonedProfile(UserInfoEx userInfoEx) {
        if (userInfoEx == null || userInfoEx.getUserInfo() == null) {
            return false;
        }
        return userInfoEx.getUserInfo().isClonedProfile();
    }

    public static boolean isSystemReservedUserId(int userId) {
        Log.i(TAG, "enter framework isSystemReservedUserId");
        int index = -1;
        int i = 0;
        while (true) {
            int[] iArr = SYSTEM_RESERVED_USERIDS;
            if (i >= iArr.length) {
                break;
            } else if (userId == iArr[i]) {
                index = i;
                Log.i(TAG, "framework isSystemReservedUserId index = i");
                break;
            } else {
                i++;
            }
        }
        Log.i(TAG, "framework isSystemReservedUserId end");
        return index != -1;
    }

    public static void createUserDir(int userId) {
        Log.i(TAG, "enter framework createUserDir");
        if (isSystemReservedUserId(userId)) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInt(userId);
                data.writeInterfaceToken("android.os.IUserManager");
                IUserManager.Stub.asInterface(ServiceManager.getService("user")).asBinder().transact(1001, data, reply, 0);
                reply.readException();
                Log.i(TAG, "framework createUserDir success");
            } catch (RemoteException e) {
                Log.w(TAG, "Could not createUserDir", e);
            } catch (Throwable th) {
                data.recycle();
                reply.recycle();
                throw th;
            }
            data.recycle();
            reply.recycle();
            Log.i(TAG, "framework createUserDir end");
        }
    }

    public static boolean hasUserRestriction(UserManager userManager, String restrictionKey, UserHandle userHandle) {
        if (userManager == null) {
            return false;
        }
        return userManager.hasUserRestriction(restrictionKey, userHandle);
    }

    public static boolean isAdminUser(UserManager userManager) {
        if (userManager == null) {
            return false;
        }
        return userManager.isAdminUser();
    }

    public static List<UserInfoEx> getUsers(UserManager userManager) {
        List<UserInfoEx> exProfiles = new ArrayList<>();
        if (userManager == null) {
            return exProfiles;
        }
        for (UserInfo userInfo : userManager.getUsers()) {
            if (userInfo != null) {
                exProfiles.add(new UserInfoEx(userInfo));
            }
        }
        return exProfiles;
    }

    public static UserInfoEx getProfileParent(UserManager userManager, int userHandle) {
        UserInfo ui;
        if (userManager == null || (ui = userManager.getProfileParent(userHandle)) == null) {
            return null;
        }
        return new UserInfoEx(ui);
    }
}

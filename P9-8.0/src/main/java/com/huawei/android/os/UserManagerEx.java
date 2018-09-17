package com.huawei.android.os;

import android.content.pm.UserInfo;
import android.graphics.Bitmap;
import android.os.IUserManager.Stub;
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
    private static final int[] SYSTEM_RESERVED_USERIDS = new int[]{PARENT_ID};
    public static final String TAG = "UserManagerEx";

    public static boolean isHwHiddenSpace(UserInfoEx userInfoEx) {
        return (userInfoEx == null || userInfoEx.getUserInfo() == null || (userInfoEx.getUserInfo().flags & 33554432) != 33554432) ? false : true;
    }

    public static Bitmap getUserIcon(UserManager userManager, int userHandle) {
        return userManager.getUserIcon(userHandle);
    }

    public static UserInfoEx getUserInfoEx(UserManager userManager, int userHandle) {
        if (userManager != null) {
            UserInfo userInfo = userManager.getUserInfo(userHandle);
            if (userInfo != null) {
                return new UserInfoEx(userInfo);
            }
        }
        return null;
    }

    public static List<UserInfoEx> getProfiles(UserManager userManager, int userHandle) {
        List<UserInfoEx> exProfiles = new ArrayList();
        if (userManager == null) {
            return exProfiles;
        }
        List<UserInfo> profiles = userManager.getProfiles(userHandle);
        int size = profiles.size();
        for (int i = 0; i < size; i++) {
            if (profiles.get(i) != null) {
                exProfiles.add(new UserInfoEx((UserInfo) profiles.get(i)));
            }
        }
        return exProfiles;
    }

    public static UserInfoEx createUser(UserManager userManager, String name, int flags) {
        UserInfoEx userInfoEx = null;
        if (userManager == null) {
            return null;
        }
        UserInfo info = userManager.createUser(name, flags);
        if (info != null) {
            userInfoEx = new UserInfoEx(info);
        }
        return userInfoEx;
    }

    public static boolean removeUser(UserManager userManager, int userHandle) {
        if (userManager != null) {
            return userManager.removeUser(userHandle);
        }
        return false;
    }

    public static UserInfoEx createProfileForUser(UserManager userManager, String name, int flags, int userHandle) {
        UserInfo userInfo = userManager.createProfileForUser(name, flags, userHandle);
        if (userInfo != null) {
            return new UserInfoEx(userInfo);
        }
        return null;
    }

    public static String getUserInfoName(UserInfoEx userInfoEx) {
        if (userInfoEx == null) {
            return null;
        }
        return userInfoEx.getUserInfo().name;
    }

    public static boolean isUserInfoValid(UserInfoEx userInfoEx) {
        boolean z = false;
        if (userInfoEx == null) {
            return false;
        }
        if (userInfoEx.getUserInfo() != null) {
            z = true;
        }
        return z;
    }

    public static int getUserInfoId(UserInfoEx userInfoEx) {
        if (userInfoEx == null) {
            return UserHandleEx.getUndefinedUserId();
        }
        return userInfoEx.getUserInfo().id;
    }

    public boolean isUserInfoManagedProfile(UserInfoEx userInfoEx) {
        if (userInfoEx == null) {
            return false;
        }
        return userInfoEx.getUserInfo().isManagedProfile();
    }

    public boolean isUserInfoClonedProfile(UserInfoEx userInfoEx) {
        return userInfoEx.getUserInfo().isClonedProfile();
    }

    public static boolean isSystemReservedUserId(int userId) {
        Log.i(TAG, "enter framework isSystemReservedUserId");
        int index = -1;
        for (int i = 0; i < SYSTEM_RESERVED_USERIDS.length; i++) {
            if (userId == SYSTEM_RESERVED_USERIDS[i]) {
                index = i;
                Log.i(TAG, "framework isSystemReservedUserId index = i");
                break;
            }
        }
        Log.i(TAG, "framework isSystemReservedUserId end");
        if (index != -1) {
            return true;
        }
        return false;
    }

    public static void createUserDir(int userId) {
        Log.i(TAG, "enter framework createUserDir");
        if (isSystemReservedUserId(userId)) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInt(userId);
                data.writeInterfaceToken("android.os.IUserManager");
                Stub.asInterface(ServiceManager.getService("user")).asBinder().transact(1001, data, reply, 0);
                reply.readException();
                Log.i(TAG, "framework createUserDir success");
            } catch (RemoteException e) {
                Log.w(TAG, "Could not createUserDir", e);
            } finally {
                data.recycle();
                reply.recycle();
            }
            Log.i(TAG, "framework createUserDir end");
        }
    }

    public static boolean hasUserRestriction(UserManager userManager, String restrictionKey, UserHandle userHandle) {
        return userManager.hasUserRestriction(restrictionKey, userHandle);
    }

    public static boolean isAdminUser(UserManager userManager) {
        if (userManager == null) {
            return false;
        }
        return userManager.isAdminUser();
    }

    public static List<UserInfoEx> getUsers(UserManager userManager) {
        List<UserInfoEx> exProfiles = new ArrayList();
        if (userManager == null) {
            return exProfiles;
        }
        List<UserInfo> listUserInfo = userManager.getUsers();
        int size = listUserInfo.size();
        for (int i = 0; i < size; i++) {
            if (listUserInfo.get(i) != null) {
                exProfiles.add(new UserInfoEx((UserInfo) listUserInfo.get(i)));
            }
        }
        return exProfiles;
    }
}

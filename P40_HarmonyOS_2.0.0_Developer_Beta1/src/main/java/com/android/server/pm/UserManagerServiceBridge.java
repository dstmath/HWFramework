package com.android.server.pm;

import android.content.Context;
import android.content.pm.UserInfo;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.android.content.pm.UserInfoExt;
import java.util.ArrayList;
import java.util.List;

public class UserManagerServiceBridge extends UserManagerService {
    private UserManagerServiceEx userManagerServiceEx;

    public UserManagerServiceBridge(Context context, PackageManagerServiceEx pm, UserDataPreparerEx userDataPreparer, Object packagesLock) {
        super(context, pm.getPackageManagerSerivce(), userDataPreparer.getUserDataPreparer(), packagesLock);
    }

    public void setUserManagerServiceEx(UserManagerServiceEx userManagerServiceEx2) {
        this.userManagerServiceEx = userManagerServiceEx2;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        UserManagerServiceEx userManagerServiceEx2 = this.userManagerServiceEx;
        if (userManagerServiceEx2 != null) {
            return userManagerServiceEx2.onTransact(code, data, reply, flags);
        }
        return UserManagerServiceBridge.super.onTransact(code, data, reply, flags);
    }

    public boolean onTransactEx(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return UserManagerServiceBridge.super.onTransact(code, data, reply, flags);
    }

    public UserInfo createProfileForUser(String name, int flags, int userId, String[] disallowedPackages) {
        UserManagerServiceEx userManagerServiceEx2 = this.userManagerServiceEx;
        if (userManagerServiceEx2 == null) {
            return UserManagerServiceBridge.super.createProfileForUser(name, flags, userId, disallowedPackages);
        }
        UserInfoExt userInfoExt = userManagerServiceEx2.createProfileForUser(name, flags, userId, disallowedPackages);
        if (userInfoExt == null) {
            return null;
        }
        return userInfoExt.getUserInfo();
    }

    public UserInfo createProfileForUserEx(String name, int flags, int userId, String[] disallowedPackages) {
        return UserManagerServiceBridge.super.createProfileForUser(name, flags, userId, disallowedPackages);
    }

    public UserInfo createUser(String name, int flags) {
        UserManagerServiceEx userManagerServiceEx2 = this.userManagerServiceEx;
        if (userManagerServiceEx2 == null) {
            return UserManagerServiceBridge.super.createUser(name, flags);
        }
        UserInfoExt userInfoEx = userManagerServiceEx2.createUser(name, flags);
        if (userInfoEx == null) {
            return null;
        }
        return userInfoEx.getUserInfo();
    }

    public UserInfo createUserEx(String name, int flags) {
        return UserManagerServiceBridge.super.createUser(name, flags);
    }

    public boolean isClonedProfile(int userId) {
        UserManagerServiceEx userManagerServiceEx2 = this.userManagerServiceEx;
        if (userManagerServiceEx2 != null) {
            return userManagerServiceEx2.isClonedProfile(userId);
        }
        return UserManagerServiceBridge.super.isClonedProfile(userId);
    }

    public boolean isClonedProfileEx(int userId) {
        return UserManagerServiceBridge.super.isClonedProfile(userId);
    }

    public UserInfo getUserInfo(int userId) {
        UserManagerServiceEx userManagerServiceEx2 = this.userManagerServiceEx;
        if (userManagerServiceEx2 != null) {
            return userManagerServiceEx2.getUserInfo(userId).getUserInfo();
        }
        return UserManagerServiceBridge.super.getUserInfo(userId);
    }

    public UserInfoExt getUserInfoEx(int userId) {
        UserInfoExt userInfoEx = new UserInfoExt();
        userInfoEx.setUserInfo(UserManagerServiceBridge.super.getUserInfo(userId));
        return userInfoEx;
    }

    public static UserInfo getUserInfoStatic(int userId) {
        return UserManagerService.getInstance().getUserInfo(userId);
    }

    public int[] getUserIds() {
        UserManagerServiceEx userManagerServiceEx2 = this.userManagerServiceEx;
        if (userManagerServiceEx2 != null) {
            return userManagerServiceEx2.getUserIds();
        }
        return UserManagerServiceBridge.super.getUserIds();
    }

    public int[] getUserIdsEx() {
        return UserManagerServiceBridge.super.getUserIds();
    }

    public List<UserInfo> getProfiles(int userId, boolean enabledOnly) {
        if (this.userManagerServiceEx == null) {
            return UserManagerServiceBridge.super.getProfiles(userId, enabledOnly);
        }
        List<UserInfo> users = new ArrayList<>();
        List<UserInfoExt> userExtList = this.userManagerServiceEx.getProfiles(userId, enabledOnly);
        int i = 0;
        while (userExtList != null && i < userExtList.size()) {
            users.add(userExtList.get(i).getUserInfo());
            i++;
        }
        return users;
    }

    public List<UserInfo> getProfilesEx(int userId, boolean enabledOnly) {
        return UserManagerServiceBridge.super.getProfiles(userId, enabledOnly);
    }

    public Object getmPm() {
        UserManagerServiceEx userManagerServiceEx2 = this.userManagerServiceEx;
        if (userManagerServiceEx2 != null) {
            return userManagerServiceEx2.getmPm();
        }
        return ((UserManagerService) this).mPm;
    }

    public Object getmPmEx() {
        return ((UserManagerService) this).mPm;
    }

    public List<UserInfo> getUsers(boolean excludeDying) {
        if (this.userManagerServiceEx == null) {
            return UserManagerServiceBridge.super.getUsers(excludeDying);
        }
        List<UserInfo> users = new ArrayList<>();
        List<UserInfoExt> userExtList = this.userManagerServiceEx.getUsers(excludeDying);
        int i = 0;
        while (userExtList != null && i < userExtList.size()) {
            users.add(userExtList.get(i).getUserInfo());
            i++;
        }
        return users;
    }

    public List<UserInfo> getUsersEx(boolean excludeDying) {
        return UserManagerServiceBridge.super.getUsers(excludeDying);
    }

    public void finishRemoveUser(int userHandle) {
        UserManagerServiceEx userManagerServiceEx2 = this.userManagerServiceEx;
        if (userManagerServiceEx2 != null) {
            userManagerServiceEx2.finishRemoveUser(userHandle);
        } else {
            UserManagerServiceBridge.super.finishRemoveUser(userHandle);
        }
    }

    public void finishRemoveUserEx(int userHandle) {
        UserManagerServiceBridge.super.finishRemoveUser(userHandle);
    }

    public void setUserRestriction(String key, boolean value, int userId) {
        UserManagerServiceEx userManagerServiceEx2 = this.userManagerServiceEx;
        if (userManagerServiceEx2 != null) {
            userManagerServiceEx2.setUserRestriction(key, value, userId);
        } else {
            UserManagerServiceBridge.super.setUserRestriction(key, value, userId);
        }
    }

    public void setUserRestrictionEx(String key, boolean value, int userId) {
        UserManagerServiceBridge.super.setUserRestriction(key, value, userId);
    }
}

package com.android.server.pm;

import android.content.Context;
import android.content.pm.UserInfo;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.android.content.pm.UserInfoExt;
import java.util.ArrayList;
import java.util.List;

public class UserManagerServiceEx {
    private static UserManagerServiceEx sInstance;
    private UserManagerServiceBridge managerServiceBridge;

    public UserManagerServiceEx(Context context, PackageManagerServiceEx pm, UserDataPreparerEx userDataPreparer, Object packagesLock) {
        this.managerServiceBridge = new UserManagerServiceBridge(context, pm, userDataPreparer, packagesLock);
        this.managerServiceBridge.setUserManagerServiceEx(this);
    }

    public UserManagerServiceEx() {
    }

    public void setUserManagerService(UserManagerService userManagerService) {
        if (userManagerService instanceof UserManagerServiceBridge) {
            this.managerServiceBridge = (UserManagerServiceBridge) userManagerService;
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return this.managerServiceBridge.onTransactEx(code, data, reply, flags);
    }

    public UserInfoExt createProfileForUser(String name, int flags, int userId, String[] disallowedPackages) {
        UserInfoExt userInfoEx = new UserInfoExt();
        userInfoEx.setUserInfo(this.managerServiceBridge.createProfileForUserEx(name, flags, userId, disallowedPackages));
        return userInfoEx;
    }

    public UserInfoExt createUser(String name, int flags) {
        UserInfo userInfo = this.managerServiceBridge.createUserEx(name, flags);
        if (userInfo == null) {
            return null;
        }
        UserInfoExt userInfoEx = new UserInfoExt();
        userInfoEx.setUserInfo(userInfo);
        return userInfoEx;
    }

    public boolean isClonedProfile(int userId) {
        return this.managerServiceBridge.isClonedProfileEx(userId);
    }

    public UserInfoExt getUserInfo(int userId) {
        return this.managerServiceBridge.getUserInfoEx(userId);
    }

    public static UserInfoExt getUserInfoStatic(int userId) {
        UserInfoExt userInfoEx = new UserInfoExt();
        userInfoEx.setUserInfo(UserManagerServiceBridge.getUserInfoStatic(userId));
        return userInfoEx;
    }

    public static synchronized UserManagerServiceEx getInstance() {
        UserManagerServiceEx userManagerServiceEx;
        synchronized (UserManagerServiceEx.class) {
            if (sInstance == null) {
                sInstance = new UserManagerServiceEx();
            }
            userManagerServiceEx = sInstance;
        }
        return userManagerServiceEx;
    }

    public UserManagerService getUserManagerService() {
        return this.managerServiceBridge;
    }

    public int[] getUserIds() {
        return this.managerServiceBridge.getUserIdsEx();
    }

    public List<UserInfoExt> getProfiles(int userId, boolean enabledOnly) {
        List<UserInfo> infoList = this.managerServiceBridge.getProfilesEx(userId, enabledOnly);
        List<UserInfoExt> infoExList = new ArrayList<>();
        for (UserInfo userInfo : infoList) {
            UserInfoExt infoEx = new UserInfoExt();
            infoEx.setUserInfo(userInfo);
            infoExList.add(infoEx);
        }
        return infoExList;
    }

    public PackageManagerServiceEx getmPm() {
        PackageManagerServiceEx packageManagerServiceEx = new PackageManagerServiceEx();
        packageManagerServiceEx.setPackageManagerService((PackageManagerService) this.managerServiceBridge.getmPmEx());
        return packageManagerServiceEx;
    }

    public List<UserInfoExt> getUsers(boolean excludeDying) {
        List<UserInfo> infoList = this.managerServiceBridge.getUsersEx(excludeDying);
        List<UserInfoExt> infoExList = new ArrayList<>();
        for (UserInfo userInfo : infoList) {
            UserInfoExt infoEx = new UserInfoExt();
            infoEx.setUserInfo(userInfo);
            infoExList.add(infoEx);
        }
        return infoExList;
    }

    public void finishRemoveUser(int userHandle) {
        this.managerServiceBridge.finishRemoveUserEx(userHandle);
    }

    public void setUserRestriction(String key, boolean value, int userId) {
        this.managerServiceBridge.setUserRestrictionEx(key, value, userId);
    }
}

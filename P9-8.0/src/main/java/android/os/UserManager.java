package android.os;

import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcelable.Creator;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import com.android.internal.os.RoSystemProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private static final String ACTION_CREATE_USER = "android.os.action.CREATE_USER";
    public static final String ACTION_USER_RESTRICTIONS_CHANGED = "android.os.action.USER_RESTRICTIONS_CHANGED";
    public static final String ALLOW_PARENT_PROFILE_APP_LINKING = "allow_parent_profile_app_linking";
    public static final String DISALLOW_ADD_MANAGED_PROFILE = "no_add_managed_profile";
    public static final String DISALLOW_ADD_USER = "no_add_user";
    public static final String DISALLOW_ADJUST_VOLUME = "no_adjust_volume";
    public static final String DISALLOW_APPS_CONTROL = "no_control_apps";
    public static final String DISALLOW_AUTOFILL = "no_autofill";
    public static final String DISALLOW_BLUETOOTH = "no_bluetooth";
    public static final String DISALLOW_BLUETOOTH_SHARING = "no_bluetooth_sharing";
    public static final String DISALLOW_CAMERA = "no_camera";
    public static final String DISALLOW_CONFIG_BLUETOOTH = "no_config_bluetooth";
    public static final String DISALLOW_CONFIG_CELL_BROADCASTS = "no_config_cell_broadcasts";
    public static final String DISALLOW_CONFIG_CREDENTIALS = "no_config_credentials";
    public static final String DISALLOW_CONFIG_MOBILE_NETWORKS = "no_config_mobile_networks";
    public static final String DISALLOW_CONFIG_TETHERING = "no_config_tethering";
    public static final String DISALLOW_CONFIG_VPN = "no_config_vpn";
    public static final String DISALLOW_CONFIG_WIFI = "no_config_wifi";
    public static final String DISALLOW_CREATE_WINDOWS = "no_create_windows";
    public static final String DISALLOW_CROSS_PROFILE_COPY_PASTE = "no_cross_profile_copy_paste";
    public static final String DISALLOW_DATA_ROAMING = "no_data_roaming";
    public static final String DISALLOW_DEBUGGING_FEATURES = "no_debugging_features";
    public static final String DISALLOW_FACTORY_RESET = "no_factory_reset";
    public static final String DISALLOW_FUN = "no_fun";
    public static final String DISALLOW_INSTALL_APPS = "no_install_apps";
    public static final String DISALLOW_INSTALL_UNKNOWN_SOURCES = "no_install_unknown_sources";
    public static final String DISALLOW_MODIFY_ACCOUNTS = "no_modify_accounts";
    public static final String DISALLOW_MOUNT_PHYSICAL_MEDIA = "no_physical_media";
    public static final String DISALLOW_NETWORK_RESET = "no_network_reset";
    @Deprecated
    public static final String DISALLOW_OEM_UNLOCK = "no_oem_unlock";
    public static final String DISALLOW_OUTGOING_BEAM = "no_outgoing_beam";
    public static final String DISALLOW_OUTGOING_CALLS = "no_outgoing_calls";
    public static final String DISALLOW_RECORD_AUDIO = "no_record_audio";
    public static final String DISALLOW_REMOVE_MANAGED_PROFILE = "no_remove_managed_profile";
    public static final String DISALLOW_REMOVE_USER = "no_remove_user";
    public static final String DISALLOW_RUN_IN_BACKGROUND = "no_run_in_background";
    public static final String DISALLOW_SAFE_BOOT = "no_safe_boot";
    public static final String DISALLOW_SET_USER_ICON = "no_set_user_icon";
    public static final String DISALLOW_SET_WALLPAPER = "no_set_wallpaper";
    public static final String DISALLOW_SHARE_LOCATION = "no_share_location";
    public static final String DISALLOW_SMS = "no_sms";
    public static final String DISALLOW_UNINSTALL_APPS = "no_uninstall_apps";
    public static final String DISALLOW_UNMUTE_DEVICE = "disallow_unmute_device";
    public static final String DISALLOW_UNMUTE_MICROPHONE = "no_unmute_microphone";
    public static final String DISALLOW_USB_FILE_TRANSFER = "no_usb_file_transfer";
    public static final String DISALLOW_WALLPAPER = "no_wallpaper";
    public static final String ENSURE_VERIFY_APPS = "ensure_verify_apps";
    public static final String EXTRA_USER_ACCOUNT_NAME = "android.os.extra.USER_ACCOUNT_NAME";
    public static final String EXTRA_USER_ACCOUNT_OPTIONS = "android.os.extra.USER_ACCOUNT_OPTIONS";
    public static final String EXTRA_USER_ACCOUNT_TYPE = "android.os.extra.USER_ACCOUNT_TYPE";
    public static final String EXTRA_USER_NAME = "android.os.extra.USER_NAME";
    public static final String KEY_RESTRICTIONS_PENDING = "restrictions_pending";
    public static final int PIN_VERIFICATION_FAILED_INCORRECT = -3;
    public static final int PIN_VERIFICATION_FAILED_NOT_SET = -2;
    public static final int PIN_VERIFICATION_SUCCESS = -1;
    public static final int RESTRICTION_NOT_SET = 0;
    public static final int RESTRICTION_SOURCE_DEVICE_OWNER = 2;
    public static final int RESTRICTION_SOURCE_PROFILE_OWNER = 4;
    public static final int RESTRICTION_SOURCE_SYSTEM = 1;
    private static final String TAG = "UserManager";
    public static final int USER_CREATION_FAILED_NOT_PERMITTED = 1;
    public static final int USER_CREATION_FAILED_NO_MORE_USERS = 2;
    private final Context mContext;
    private Boolean mIsManagedProfileCached;
    private final IUserManager mService;

    public static final class EnforcingUser implements Parcelable {
        public static final Creator<EnforcingUser> CREATOR = new Creator<EnforcingUser>() {
            public EnforcingUser createFromParcel(Parcel in) {
                return new EnforcingUser(in, null);
            }

            public EnforcingUser[] newArray(int size) {
                return new EnforcingUser[size];
            }
        };
        private final int userId;
        private final int userRestrictionSource;

        public EnforcingUser(int userId, int userRestrictionSource) {
            this.userId = userId;
            this.userRestrictionSource = userRestrictionSource;
        }

        private EnforcingUser(Parcel in) {
            this.userId = in.readInt();
            this.userRestrictionSource = in.readInt();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.userId);
            dest.writeInt(this.userRestrictionSource);
        }

        public UserHandle getUserHandle() {
            return UserHandle.of(this.userId);
        }

        public int getUserRestrictionSource() {
            return this.userRestrictionSource;
        }
    }

    public static UserManager get(Context context) {
        return (UserManager) context.getSystemService(Context.USER_SERVICE);
    }

    public UserManager(Context context, IUserManager service) {
        this.mService = service;
        this.mContext = context.getApplicationContext();
    }

    public static boolean supportsMultipleUsers() {
        if (getMaxSupportedUsers() > 1) {
            return SystemProperties.getBoolean("fw.show_multiuserui", Resources.getSystem().getBoolean(17956955));
        }
        return false;
    }

    public static boolean isSplitSystemUser() {
        return RoSystemProperties.FW_SYSTEM_USER_SPLIT;
    }

    public static boolean isGuestUserEphemeral() {
        return Resources.getSystem().getBoolean(17956969);
    }

    public boolean canSwitchUsers() {
        boolean allowUserSwitchingWhenSystemUserLocked = Global.getInt(this.mContext.getContentResolver(), "allow_user_switching_when_system_user_locked", 0) != 0;
        boolean isSystemUserUnlocked = isUserUnlocked(UserHandle.SYSTEM);
        boolean inCall = TelephonyManager.getDefault().getCallState() != 0;
        if (allowUserSwitchingWhenSystemUserLocked || isSystemUserUnlocked) {
            return inCall ^ 1;
        }
        return false;
    }

    public int getUserHandle() {
        return UserHandle.myUserId();
    }

    public String getUserName() {
        try {
            return this.mService.getUserInfo(getUserHandle()).name;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isUserNameSet() {
        try {
            return this.mService.isUserNameSet(getUserHandle());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isUserAGoat() {
        return this.mContext.getPackageManager().isPackageAvailable("com.coffeestainstudios.goatsimulator");
    }

    public boolean isPrimaryUser() {
        UserInfo user = getUserInfo(UserHandle.myUserId());
        return user != null ? user.isPrimary() : false;
    }

    public boolean isSystemUser() {
        return UserHandle.myUserId() == 0;
    }

    public boolean isAdminUser() {
        return isUserAdmin(UserHandle.myUserId());
    }

    public boolean isUserAdmin(int userId) {
        UserInfo user = getUserInfo(userId);
        return user != null ? user.isAdmin() : false;
    }

    public boolean isLinkedUser() {
        try {
            return this.mService.isRestricted();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean canHaveRestrictedProfile(int userId) {
        try {
            return this.mService.canHaveRestrictedProfile(userId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isGuestUser(int id) {
        UserInfo user = getUserInfo(id);
        return user != null ? user.isGuest() : false;
    }

    public boolean isGuestUser() {
        UserInfo user = getUserInfo(UserHandle.myUserId());
        return user != null ? user.isGuest() : false;
    }

    public boolean isDemoUser() {
        try {
            return this.mService.isDemoUser(UserHandle.myUserId());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isManagedProfile() {
        if (this.mIsManagedProfileCached != null) {
            return this.mIsManagedProfileCached.booleanValue();
        }
        try {
            this.mIsManagedProfileCached = Boolean.valueOf(this.mService.isManagedProfile(UserHandle.myUserId()));
            return this.mIsManagedProfileCached.booleanValue();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isManagedProfile(int userId) {
        if (userId == UserHandle.myUserId()) {
            return isManagedProfile();
        }
        try {
            return this.mService.isManagedProfile(userId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int getManagedProfileBadge(int userId) {
        try {
            return this.mService.getManagedProfileBadge(userId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isEphemeralUser() {
        return isUserEphemeral(UserHandle.myUserId());
    }

    public boolean isUserEphemeral(int userId) {
        UserInfo user = getUserInfo(userId);
        return user != null ? user.isEphemeral() : false;
    }

    public boolean isUserRunning(UserHandle user) {
        return isUserRunning(user.getIdentifier());
    }

    public boolean isUserRunning(int userId) {
        try {
            return this.mService.isUserRunning(userId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isUserRunningOrStopping(UserHandle user) {
        try {
            return ActivityManager.getService().isUserRunning(user.getIdentifier(), 1);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isUserUnlocked() {
        return isUserUnlocked(Process.myUserHandle());
    }

    public boolean isUserUnlocked(UserHandle user) {
        return isUserUnlocked(user.getIdentifier());
    }

    public boolean isUserUnlocked(int userId) {
        try {
            return this.mService.isUserUnlocked(userId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isUserUnlockingOrUnlocked(UserHandle user) {
        return isUserUnlockingOrUnlocked(user.getIdentifier());
    }

    public boolean isUserUnlockingOrUnlocked(int userId) {
        try {
            return this.mService.isUserUnlockingOrUnlocked(userId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public UserInfo getUserInfo(int userHandle) {
        try {
            return this.mService.getUserInfo(userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public int getUserRestrictionSource(String restrictionKey, UserHandle userHandle) {
        try {
            return this.mService.getUserRestrictionSource(restrictionKey, userHandle.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<EnforcingUser> getUserRestrictionSources(String restrictionKey, UserHandle userHandle) {
        try {
            return this.mService.getUserRestrictionSources(restrictionKey, userHandle.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public Bundle getUserRestrictions() {
        return getUserRestrictions(Process.myUserHandle());
    }

    public Bundle getUserRestrictions(UserHandle userHandle) {
        try {
            return this.mService.getUserRestrictions(userHandle.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean hasBaseUserRestriction(String restrictionKey, UserHandle userHandle) {
        try {
            return this.mService.hasBaseUserRestriction(restrictionKey, userHandle.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void setUserRestrictions(Bundle restrictions) {
        throw new UnsupportedOperationException("This method is no longer supported");
    }

    @Deprecated
    public void setUserRestrictions(Bundle restrictions, UserHandle userHandle) {
        throw new UnsupportedOperationException("This method is no longer supported");
    }

    @Deprecated
    public void setUserRestriction(String key, boolean value) {
        setUserRestriction(key, value, Process.myUserHandle());
    }

    @Deprecated
    public void setUserRestriction(String key, boolean value, UserHandle userHandle) {
        try {
            this.mService.setUserRestriction(key, value, userHandle.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean hasUserRestriction(String restrictionKey) {
        return hasUserRestriction(restrictionKey, Process.myUserHandle());
    }

    public boolean hasUserRestriction(String restrictionKey, UserHandle userHandle) {
        try {
            return this.mService.hasUserRestriction(restrictionKey, userHandle.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public long getSerialNumberForUser(UserHandle user) {
        return (long) getUserSerialNumber(user.getIdentifier());
    }

    public UserHandle getUserForSerialNumber(long serialNumber) {
        int ident = getUserHandle((int) serialNumber);
        return ident >= 0 ? new UserHandle(ident) : null;
    }

    public UserInfo createUser(String name, int flags) {
        try {
            UserInfo user = this.mService.createUser(name, flags);
            if (!(user == null || (user.isAdmin() ^ 1) == 0 || (user.isHwHiddenSpace() ^ 1) == 0)) {
                this.mService.setUserRestriction(DISALLOW_SMS, true, user.id);
                if (!user.isRepairMode()) {
                    this.mService.setUserRestriction(DISALLOW_OUTGOING_CALLS, true, user.id);
                }
            }
            return user;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public UserInfo createGuest(Context context, String name) {
        try {
            UserInfo guest = this.mService.createUser(name, 4);
            if (guest != null) {
                Secure.putStringForUser(context.getContentResolver(), "skip_first_use_hints", "1", guest.id);
            }
            return guest;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public UserInfo createProfileForUser(String name, int flags, int userHandle) {
        return createProfileForUser(name, flags, userHandle, null);
    }

    public UserInfo createProfileForUser(String name, int flags, int userHandle, String[] disallowedPackages) {
        try {
            return this.mService.createProfileForUser(name, flags, userHandle, disallowedPackages);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public UserInfo createProfileForUserEvenWhenDisallowed(String name, int flags, int userHandle, String[] disallowedPackages) {
        try {
            return this.mService.createProfileForUserEvenWhenDisallowed(name, flags, userHandle, disallowedPackages);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public UserInfo createRestrictedProfile(String name) {
        try {
            UserHandle parentUserHandle = Process.myUserHandle();
            UserInfo user = this.mService.createRestrictedProfile(name, parentUserHandle.getIdentifier());
            if (user != null) {
                AccountManager.get(this.mContext).addSharedAccountsFromParentUser(parentUserHandle, UserHandle.of(user.id));
            }
            return user;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public static Intent createUserCreationIntent(String userName, String accountName, String accountType, PersistableBundle accountOptions) {
        Intent intent = new Intent(ACTION_CREATE_USER);
        if (userName != null) {
            intent.putExtra(EXTRA_USER_NAME, userName);
        }
        if (accountName == null || accountType != null) {
            if (accountName != null) {
                intent.putExtra(EXTRA_USER_ACCOUNT_NAME, accountName);
            }
            if (accountType != null) {
                intent.putExtra(EXTRA_USER_ACCOUNT_TYPE, accountType);
            }
            if (accountOptions != null) {
                intent.putExtra(EXTRA_USER_ACCOUNT_OPTIONS, (Parcelable) accountOptions);
            }
            return intent;
        }
        throw new IllegalArgumentException("accountType must be specified if accountName is specified");
    }

    public String getSeedAccountName() {
        try {
            return this.mService.getSeedAccountName();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public String getSeedAccountType() {
        try {
            return this.mService.getSeedAccountType();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public PersistableBundle getSeedAccountOptions() {
        try {
            return this.mService.getSeedAccountOptions();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setSeedAccountData(int userId, String accountName, String accountType, PersistableBundle accountOptions) {
        try {
            this.mService.setSeedAccountData(userId, accountName, accountType, accountOptions, true);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void clearSeedAccountData() {
        try {
            this.mService.clearSeedAccountData();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean markGuestForDeletion(int userHandle) {
        try {
            return this.mService.markGuestForDeletion(userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setUserEnabled(int userHandle) {
        try {
            this.mService.setUserEnabled(userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void evictCredentialEncryptionKey(int userHandle) {
        try {
            this.mService.evictCredentialEncryptionKey(userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int getUserCount() {
        List<UserInfo> users = getUsers();
        return users != null ? users.size() : 1;
    }

    public List<UserInfo> getUsers() {
        try {
            return this.mService.getUsers(false);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public long[] getSerialNumbersOfUsers(boolean excludeDying) {
        try {
            List<UserInfo> users = this.mService.getUsers(excludeDying);
            long[] result = new long[users.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = (long) ((UserInfo) users.get(i)).serialNumber;
            }
            return result;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public String getUserAccount(int userHandle) {
        try {
            return this.mService.getUserAccount(userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setUserAccount(int userHandle, String accountName) {
        try {
            this.mService.setUserAccount(userHandle, accountName);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public UserInfo getPrimaryUser() {
        try {
            return this.mService.getPrimaryUser();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean canAddMoreUsers() {
        List<UserInfo> users = getUsers(true);
        int totalUserCount = users.size();
        int aliveUserCount = 0;
        for (int i = 0; i < totalUserCount; i++) {
            UserInfo user = (UserInfo) users.get(i);
            if (!(user.isGuest() || (user.isClonedProfile() ^ 1) == 0)) {
                aliveUserCount++;
            }
        }
        if (aliveUserCount < getMaxSupportedUsers()) {
            return true;
        }
        return false;
    }

    public boolean canAddMoreManagedProfiles(int userId, boolean allowedToRemoveOne) {
        try {
            return this.mService.canAddMoreManagedProfiles(userId, allowedToRemoveOne);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<UserInfo> getProfiles(int userHandle) {
        try {
            return this.mService.getProfiles(userHandle, false);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isSameProfileGroup(int userId, int otherUserId) {
        try {
            return this.mService.isSameProfileGroup(userId, otherUserId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<UserInfo> getEnabledProfiles(int userHandle) {
        try {
            return this.mService.getProfiles(userHandle, true);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<UserHandle> getUserProfiles() {
        int[] userIds = getProfileIds(UserHandle.myUserId(), true);
        List<UserHandle> result = new ArrayList(userIds.length);
        for (int userId : userIds) {
            result.add(UserHandle.of(userId));
        }
        return result;
    }

    public int[] getProfileIds(int userId, boolean enabledOnly) {
        try {
            return this.mService.getProfileIds(userId, enabledOnly);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int[] getProfileIdsWithDisabled(int userId) {
        return getProfileIds(userId, false);
    }

    public int[] getEnabledProfileIds(int userId) {
        return getProfileIds(userId, true);
    }

    public int getCredentialOwnerProfile(int userHandle) {
        try {
            return this.mService.getCredentialOwnerProfile(userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public UserInfo getProfileParent(int userHandle) {
        try {
            return this.mService.getProfileParent(userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setQuietModeEnabled(int userHandle, boolean enableQuietMode) {
        try {
            this.mService.setQuietModeEnabled(userHandle, enableQuietMode);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isQuietModeEnabled(UserHandle userHandle) {
        try {
            return this.mService.isQuietModeEnabled(userHandle.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean trySetQuietModeDisabled(int userHandle, IntentSender target) {
        try {
            return this.mService.trySetQuietModeDisabled(userHandle, target);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public Drawable getBadgedIconForUser(Drawable icon, UserHandle user) {
        return this.mContext.getPackageManager().getUserBadgedIcon(icon, user);
    }

    public Drawable getBadgedDrawableForUser(Drawable badgedDrawable, UserHandle user, Rect badgeLocation, int badgeDensity) {
        return this.mContext.getPackageManager().getUserBadgedDrawableForDensity(badgedDrawable, user, badgeLocation, badgeDensity);
    }

    public CharSequence getBadgedLabelForUser(CharSequence label, UserHandle user) {
        return this.mContext.getPackageManager().getUserBadgedLabel(label, user);
    }

    public List<UserInfo> getUsers(boolean excludeDying) {
        try {
            return this.mService.getUsers(excludeDying);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean removeUser(int userHandle) {
        try {
            return this.mService.removeUser(userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean removeUserEvenWhenDisallowed(int userHandle) {
        try {
            return this.mService.removeUserEvenWhenDisallowed(userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setUserName(int userHandle, String name) {
        try {
            this.mService.setUserName(userHandle, name);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setUserIcon(int userHandle, Bitmap icon) {
        try {
            this.mService.setUserIcon(userHandle, icon);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public Bitmap getUserIcon(int userHandle) {
        ParcelFileDescriptor fd;
        try {
            fd = this.mService.getUserIcon(userHandle);
            if (fd == null) {
                return null;
            }
            Bitmap decodeFileDescriptor = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor());
            try {
                fd.close();
            } catch (IOException e) {
            }
            return decodeFileDescriptor;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        } catch (Throwable th) {
            try {
                fd.close();
            } catch (IOException e2) {
            }
        }
    }

    public static int getMaxSupportedUsers() {
        if (Build.ID.startsWith("JVP") || ActivityManager.isLowRamDeviceStatic()) {
            return 1;
        }
        return SystemProperties.getInt("fw.max_users", Resources.getSystem().getInteger(17694815));
    }

    public boolean isUserSwitcherEnabled() {
        if (!supportsMultipleUsers() || isDeviceInDemoMode(this.mContext)) {
            return false;
        }
        List<UserInfo> users = getUsers(true);
        if (users == null) {
            return false;
        }
        int switchableUserCount = 0;
        for (UserInfo user : users) {
            if (user.supportsSwitchToByUser()) {
                switchableUserCount++;
            }
        }
        boolean guestEnabled = ((DevicePolicyManager) this.mContext.getSystemService(DevicePolicyManager.class)).getGuestUserDisabled(null) ^ 1;
        if (switchableUserCount > 1) {
            guestEnabled = true;
        }
        return guestEnabled;
    }

    public static boolean isDeviceInDemoMode(Context context) {
        return Global.getInt(context.getContentResolver(), "device_demo_mode", 0) > 0;
    }

    public int getUserSerialNumber(int userHandle) {
        try {
            return this.mService.getUserSerialNumber(userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int getUserHandle(int userSerialNumber) {
        try {
            return this.mService.getUserHandle(userSerialNumber);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public Bundle getApplicationRestrictions(String packageName) {
        try {
            return this.mService.getApplicationRestrictions(packageName);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public Bundle getApplicationRestrictions(String packageName, UserHandle user) {
        try {
            return this.mService.getApplicationRestrictionsForUser(packageName, user.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setApplicationRestrictions(String packageName, Bundle restrictions, UserHandle user) {
        try {
            this.mService.setApplicationRestrictions(packageName, restrictions, user.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public boolean setRestrictionsChallenge(String newPin) {
        return false;
    }

    public void setDefaultGuestRestrictions(Bundle restrictions) {
        try {
            this.mService.setDefaultGuestRestrictions(restrictions);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public Bundle getDefaultGuestRestrictions() {
        try {
            return this.mService.getDefaultGuestRestrictions();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public long getUserCreationTime(UserHandle userHandle) {
        try {
            return this.mService.getUserCreationTime(userHandle.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean someUserHasSeedAccount(String accountName, String accountType) {
        try {
            return this.mService.someUserHasSeedAccount(accountName, accountType);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }
}

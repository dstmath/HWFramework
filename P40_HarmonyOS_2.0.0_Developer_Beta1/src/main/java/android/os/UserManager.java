package android.os;

import android.accounts.AccountManager;
import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
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
import android.os.Parcelable;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.android.internal.R;
import com.android.internal.os.RoSystemProperties;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private static final String ACTION_CREATE_USER = "android.os.action.CREATE_USER";
    @SystemApi
    public static final String ACTION_USER_RESTRICTIONS_CHANGED = "android.os.action.USER_RESTRICTIONS_CHANGED";
    public static final String ALLOW_PARENT_PROFILE_APP_LINKING = "allow_parent_profile_app_linking";
    public static final String DISALLOW_ADD_MANAGED_PROFILE = "no_add_managed_profile";
    public static final String DISALLOW_ADD_USER = "no_add_user";
    public static final String DISALLOW_ADJUST_VOLUME = "no_adjust_volume";
    public static final String DISALLOW_AIRPLANE_MODE = "no_airplane_mode";
    public static final String DISALLOW_AMBIENT_DISPLAY = "no_ambient_display";
    public static final String DISALLOW_APPS_CONTROL = "no_control_apps";
    public static final String DISALLOW_AUTOFILL = "no_autofill";
    public static final String DISALLOW_BLUETOOTH = "no_bluetooth";
    public static final String DISALLOW_BLUETOOTH_SHARING = "no_bluetooth_sharing";
    public static final String DISALLOW_CAMERA = "no_camera";
    public static final String DISALLOW_CONFIG_BLUETOOTH = "no_config_bluetooth";
    public static final String DISALLOW_CONFIG_BRIGHTNESS = "no_config_brightness";
    public static final String DISALLOW_CONFIG_CELL_BROADCASTS = "no_config_cell_broadcasts";
    public static final String DISALLOW_CONFIG_CREDENTIALS = "no_config_credentials";
    public static final String DISALLOW_CONFIG_DATE_TIME = "no_config_date_time";
    public static final String DISALLOW_CONFIG_LOCALE = "no_config_locale";
    public static final String DISALLOW_CONFIG_LOCATION = "no_config_location";
    public static final String DISALLOW_CONFIG_MOBILE_NETWORKS = "no_config_mobile_networks";
    public static final String DISALLOW_CONFIG_PRIVATE_DNS = "disallow_config_private_dns";
    public static final String DISALLOW_CONFIG_SCREEN_TIMEOUT = "no_config_screen_timeout";
    public static final String DISALLOW_CONFIG_TETHERING = "no_config_tethering";
    public static final String DISALLOW_CONFIG_VPN = "no_config_vpn";
    public static final String DISALLOW_CONFIG_WIFI = "no_config_wifi";
    public static final String DISALLOW_CONTENT_CAPTURE = "no_content_capture";
    public static final String DISALLOW_CONTENT_SUGGESTIONS = "no_content_suggestions";
    public static final String DISALLOW_CREATE_WINDOWS = "no_create_windows";
    public static final String DISALLOW_CROSS_PROFILE_COPY_PASTE = "no_cross_profile_copy_paste";
    public static final String DISALLOW_DATA_ROAMING = "no_data_roaming";
    public static final String DISALLOW_DEBUGGING_FEATURES = "no_debugging_features";
    public static final String DISALLOW_FACTORY_RESET = "no_factory_reset";
    public static final String DISALLOW_FUN = "no_fun";
    public static final String DISALLOW_INSTALL_APPS = "no_install_apps";
    public static final String DISALLOW_INSTALL_UNKNOWN_SOURCES = "no_install_unknown_sources";
    public static final String DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY = "no_install_unknown_sources_globally";
    public static final String DISALLOW_MODIFY_ACCOUNTS = "no_modify_accounts";
    public static final String DISALLOW_MOUNT_PHYSICAL_MEDIA = "no_physical_media";
    public static final String DISALLOW_NETWORK_RESET = "no_network_reset";
    @SystemApi
    @Deprecated
    public static final String DISALLOW_OEM_UNLOCK = "no_oem_unlock";
    public static final String DISALLOW_OUTGOING_BEAM = "no_outgoing_beam";
    public static final String DISALLOW_OUTGOING_CALLS = "no_outgoing_calls";
    public static final String DISALLOW_PRINTING = "no_printing";
    @UnsupportedAppUsage
    public static final String DISALLOW_RECORD_AUDIO = "no_record_audio";
    public static final String DISALLOW_REMOVE_MANAGED_PROFILE = "no_remove_managed_profile";
    public static final String DISALLOW_REMOVE_USER = "no_remove_user";
    @SystemApi
    public static final String DISALLOW_RUN_IN_BACKGROUND = "no_run_in_background";
    public static final String DISALLOW_SAFE_BOOT = "no_safe_boot";
    public static final String DISALLOW_SET_USER_ICON = "no_set_user_icon";
    public static final String DISALLOW_SET_WALLPAPER = "no_set_wallpaper";
    public static final String DISALLOW_SHARE_INTO_MANAGED_PROFILE = "no_sharing_into_profile";
    public static final String DISALLOW_SHARE_LOCATION = "no_share_location";
    public static final String DISALLOW_SMS = "no_sms";
    public static final String DISALLOW_SYSTEM_ERROR_DIALOGS = "no_system_error_dialogs";
    public static final String DISALLOW_UNIFIED_PASSWORD = "no_unified_password";
    public static final String DISALLOW_UNINSTALL_APPS = "no_uninstall_apps";
    public static final String DISALLOW_UNMUTE_DEVICE = "disallow_unmute_device";
    public static final String DISALLOW_UNMUTE_MICROPHONE = "no_unmute_microphone";
    public static final String DISALLOW_USB_FILE_TRANSFER = "no_usb_file_transfer";
    public static final String DISALLOW_USER_SWITCH = "no_user_switch";
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
    @SystemApi
    public static final int RESTRICTION_NOT_SET = 0;
    @SystemApi
    public static final int RESTRICTION_SOURCE_DEVICE_OWNER = 2;
    @SystemApi
    public static final int RESTRICTION_SOURCE_PROFILE_OWNER = 4;
    @SystemApi
    public static final int RESTRICTION_SOURCE_SYSTEM = 1;
    @SystemApi
    public static final int SWITCHABILITY_STATUS_OK = 0;
    @SystemApi
    public static final int SWITCHABILITY_STATUS_SYSTEM_USER_LOCKED = 4;
    @SystemApi
    public static final int SWITCHABILITY_STATUS_USER_IN_CALL = 1;
    @SystemApi
    public static final int SWITCHABILITY_STATUS_USER_SWITCH_DISALLOWED = 2;
    private static final String TAG = "UserManager";
    public static final int USER_CREATION_FAILED_NOT_PERMITTED = 1;
    public static final int USER_CREATION_FAILED_NO_MORE_USERS = 2;
    public static final int USER_OPERATION_ERROR_CURRENT_USER = 4;
    public static final int USER_OPERATION_ERROR_LOW_STORAGE = 5;
    public static final int USER_OPERATION_ERROR_MANAGED_PROFILE = 2;
    public static final int USER_OPERATION_ERROR_MAX_RUNNING_USERS = 3;
    public static final int USER_OPERATION_ERROR_MAX_USERS = 6;
    public static final int USER_OPERATION_ERROR_UNKNOWN = 1;
    public static final int USER_OPERATION_SUCCESS = 0;
    private final Context mContext;
    private Boolean mIsManagedProfileCached;
    @UnsupportedAppUsage
    private final IUserManager mService;

    @Retention(RetentionPolicy.SOURCE)
    public @interface UserOperationResult {
    }

    @SystemApi
    @Retention(RetentionPolicy.SOURCE)
    public @interface UserRestrictionSource {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface UserSwitchabilityResult {
    }

    public static class UserOperationException extends RuntimeException {
        private final int mUserOperationResult;

        public UserOperationException(String message, int userOperationResult) {
            super(message);
            this.mUserOperationResult = userOperationResult;
        }

        public int getUserOperationResult() {
            return this.mUserOperationResult;
        }
    }

    @UnsupportedAppUsage
    public static UserManager get(Context context) {
        return (UserManager) context.getSystemService("user");
    }

    public UserManager(Context context, IUserManager service) {
        this.mService = service;
        this.mContext = context.getApplicationContext();
    }

    public static boolean supportsMultipleUsers() {
        if (getMaxSupportedUsers() <= 1 || !SystemProperties.getBoolean("fw.show_multiuserui", Resources.getSystem().getBoolean(R.bool.config_enableMultiUserUI))) {
            return false;
        }
        return true;
    }

    public static boolean isSplitSystemUser() {
        return RoSystemProperties.FW_SYSTEM_USER_SPLIT;
    }

    public static boolean isGuestUserEphemeral() {
        return Resources.getSystem().getBoolean(R.bool.config_guestUserEphemeral);
    }

    @UnsupportedAppUsage
    @Deprecated
    public boolean canSwitchUsers() {
        return ((Settings.Global.getInt(this.mContext.getContentResolver(), Settings.Global.ALLOW_USER_SWITCHING_WHEN_SYSTEM_USER_LOCKED, 0) != 0) || isUserUnlocked(UserHandle.SYSTEM)) && !(TelephonyManager.getDefault().getCallState() != 0) && !hasUserRestriction(DISALLOW_USER_SWITCH);
    }

    @SystemApi
    public int getUserSwitchability() {
        boolean allowUserSwitchingWhenSystemUserLocked = false;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), Settings.Global.ALLOW_USER_SWITCHING_WHEN_SYSTEM_USER_LOCKED, 0) != 0) {
            allowUserSwitchingWhenSystemUserLocked = true;
        }
        boolean systemUserUnlocked = isUserUnlocked(UserHandle.SYSTEM);
        int flags = 0;
        if (((TelephonyManager) this.mContext.getSystemService("phone")).getCallState() != 0) {
            flags = 0 | 1;
        }
        if (hasUserRestriction(DISALLOW_USER_SWITCH)) {
            flags |= 2;
        }
        if (allowUserSwitchingWhenSystemUserLocked || systemUserUnlocked) {
            return flags;
        }
        return flags | 4;
    }

    @UnsupportedAppUsage
    public int getUserHandle() {
        return UserHandle.myUserId();
    }

    public String getUserName() {
        try {
            return this.mService.getUserName();
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

    @SystemApi
    public boolean isPrimaryUser() {
        UserInfo user = getUserInfo(UserHandle.myUserId());
        return user != null && user.isPrimary();
    }

    public boolean isSystemUser() {
        return UserHandle.myUserId() == 0;
    }

    @SystemApi
    public boolean isAdminUser() {
        return isUserAdmin(UserHandle.myUserId());
    }

    @UnsupportedAppUsage
    public boolean isUserAdmin(int userId) {
        UserInfo user = getUserInfo(userId);
        return user != null && user.isAdmin();
    }

    @UnsupportedAppUsage
    @Deprecated
    public boolean isLinkedUser() {
        return isRestrictedProfile();
    }

    @SystemApi
    public boolean isRestrictedProfile() {
        try {
            return this.mService.isRestricted();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean isRestrictedProfile(UserHandle user) {
        try {
            return this.mService.getUserInfo(user.getIdentifier()).isRestricted();
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

    @SystemApi
    public boolean hasRestrictedProfiles() {
        try {
            return this.mService.hasRestrictedProfiles();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public boolean isGuestUser(int id) {
        UserInfo user = getUserInfo(id);
        return user != null && user.isGuest();
    }

    @SystemApi
    public boolean isGuestUser() {
        UserInfo user = getUserInfo(UserHandle.myUserId());
        return user != null && user.isGuest();
    }

    public boolean isDemoUser() {
        try {
            return this.mService.isDemoUser(UserHandle.myUserId());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean isManagedProfile() {
        Boolean bool = this.mIsManagedProfileCached;
        if (bool != null) {
            return bool.booleanValue();
        }
        try {
            this.mIsManagedProfileCached = Boolean.valueOf(this.mService.isManagedProfile(UserHandle.myUserId()));
            return this.mIsManagedProfileCached.booleanValue();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
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
        return user != null && user.isEphemeral();
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

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public long getUserStartRealtime() {
        try {
            return this.mService.getUserStartRealtime();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public long getUserUnlockRealtime() {
        try {
            return this.mService.getUserUnlockRealtime();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public UserInfo getUserInfo(int userHandle) {
        try {
            return this.mService.getUserInfo(userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    @Deprecated
    public int getUserRestrictionSource(String restrictionKey, UserHandle userHandle) {
        try {
            return this.mService.getUserRestrictionSource(restrictionKey, userHandle.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
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

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public boolean hasUserRestriction(String restrictionKey, UserHandle userHandle) {
        try {
            return this.mService.hasUserRestriction(restrictionKey, userHandle.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean hasUserRestrictionOnAnyUser(String restrictionKey) {
        try {
            return this.mService.hasUserRestrictionOnAnyUser(restrictionKey);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public long getSerialNumberForUser(UserHandle user) {
        return (long) getUserSerialNumber(user.getIdentifier());
    }

    public UserHandle getUserForSerialNumber(long serialNumber) {
        int ident = getUserHandle((int) serialNumber);
        if (ident >= 0) {
            return new UserHandle(ident);
        }
        return null;
    }

    @UnsupportedAppUsage
    public UserInfo createUser(String name, int flags) {
        try {
            UserInfo user = this.mService.createUser(name, flags);
            if (user != null && !user.isAdmin() && !user.isDemo()) {
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
                Settings.Secure.putStringForUser(context.getContentResolver(), Settings.Secure.SKIP_FIRST_USE_HINTS, "1", guest.id);
            }
            return guest;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
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
                intent.putExtra(EXTRA_USER_ACCOUNT_OPTIONS, accountOptions);
            }
            return intent;
        }
        throw new IllegalArgumentException("accountType must be specified if accountName is specified");
    }

    @SystemApi
    public String getSeedAccountName() {
        try {
            return this.mService.getSeedAccountName();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public String getSeedAccountType() {
        try {
            return this.mService.getSeedAccountType();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
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

    @SystemApi
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

    public void setUserEnabled(int userId) {
        try {
            this.mService.setUserEnabled(userId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setUserAdmin(int userHandle) {
        try {
            this.mService.setUserAdmin(userHandle);
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
        if (users != null) {
            return users.size();
        }
        return 1;
    }

    @UnsupportedAppUsage
    public List<UserInfo> getUsers() {
        try {
            return this.mService.getUsers(false);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public long[] getSerialNumbersOfUsers(boolean excludeDying) {
        try {
            List<UserInfo> users = this.mService.getUsers(excludeDying);
            long[] result = new long[users.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = (long) users.get(i).serialNumber;
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
            UserInfo user = users.get(i);
            if (!user.isGuest() && !user.isClonedProfile() && !user.isHwKidsUser()) {
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

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public List<UserInfo> getEnabledProfiles(int userHandle) {
        try {
            return this.mService.getProfiles(userHandle, true);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<UserHandle> getUserProfiles() {
        int[] userIds = getProfileIds(UserHandle.myUserId(), true);
        List<UserHandle> result = new ArrayList<>(userIds.length);
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

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public UserInfo getProfileParent(int userHandle) {
        try {
            return this.mService.getProfileParent(userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public UserHandle getProfileParent(UserHandle user) {
        UserInfo info = getProfileParent(user.getIdentifier());
        if (info == null) {
            return null;
        }
        return UserHandle.of(info.id);
    }

    public boolean requestQuietModeEnabled(boolean enableQuietMode, UserHandle userHandle) {
        return requestQuietModeEnabled(enableQuietMode, userHandle, null);
    }

    public boolean requestQuietModeEnabled(boolean enableQuietMode, UserHandle userHandle, IntentSender target) {
        try {
            return this.mService.requestQuietModeEnabled(this.mContext.getPackageName(), enableQuietMode, userHandle.getIdentifier(), target);
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

    public Drawable getBadgedIconForUser(Drawable icon, UserHandle user) {
        return this.mContext.getPackageManager().getUserBadgedIcon(icon, user);
    }

    public Drawable getBadgedDrawableForUser(Drawable badgedDrawable, UserHandle user, Rect badgeLocation, int badgeDensity) {
        return this.mContext.getPackageManager().getUserBadgedDrawableForDensity(badgedDrawable, user, badgeLocation, badgeDensity);
    }

    public CharSequence getBadgedLabelForUser(CharSequence label, UserHandle user) {
        return this.mContext.getPackageManager().getUserBadgedLabel(label, user);
    }

    @UnsupportedAppUsage
    public List<UserInfo> getUsers(boolean excludeDying) {
        try {
            return this.mService.getUsers(excludeDying);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public boolean removeUser(int userHandle) {
        try {
            return this.mService.removeUser(userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean removeUser(UserHandle user) {
        if (user != null) {
            return removeUser(user.getIdentifier());
        }
        throw new IllegalArgumentException("user cannot be null");
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

    @SystemApi
    public void setUserName(String name) {
        setUserName(getUserHandle(), name);
    }

    public void setUserIcon(int userHandle, Bitmap icon) {
        try {
            this.mService.setUserIcon(userHandle, icon);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void setUserIcon(Bitmap icon) {
        setUserIcon(getUserHandle(), icon);
    }

    @UnsupportedAppUsage
    public Bitmap getUserIcon(int userHandle) {
        try {
            ParcelFileDescriptor fd = this.mService.getUserIcon(userHandle);
            if (fd == null) {
                return null;
            }
            try {
                return BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor());
            } finally {
                try {
                    fd.close();
                } catch (IOException e) {
                }
            }
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public Bitmap getUserIcon() {
        return getUserIcon(getUserHandle());
    }

    @UnsupportedAppUsage
    public static int getMaxSupportedUsers() {
        if (Build.ID.startsWith("JVP")) {
            return 1;
        }
        if (!ActivityManager.isLowRamDeviceStatic() || (Resources.getSystem().getConfiguration().uiMode & 15) == 4) {
            return SystemProperties.getInt("fw.max_users", Resources.getSystem().getInteger(R.integer.config_multiuserMaximumUsers));
        }
        return 1;
    }

    public boolean isUserSwitcherEnabled() {
        List<UserInfo> users;
        if (!supportsMultipleUsers() || hasUserRestriction(DISALLOW_USER_SWITCH) || isDeviceInDemoMode(this.mContext)) {
            return false;
        }
        if (!(Settings.Global.getInt(this.mContext.getContentResolver(), Settings.Global.USER_SWITCHER_ENABLED, 1) != 0) || (users = getUsers(true)) == null) {
            return false;
        }
        int switchableUserCount = 0;
        for (UserInfo user : users) {
            if (user.supportsSwitchToByUser()) {
                switchableUserCount++;
            }
        }
        boolean guestEnabled = !((DevicePolicyManager) this.mContext.getSystemService(DevicePolicyManager.class)).getGuestUserDisabled(null);
        if (switchableUserCount > 1 || guestEnabled) {
            return true;
        }
        return false;
    }

    @UnsupportedAppUsage
    public static boolean isDeviceInDemoMode(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.DEVICE_DEMO_MODE, 0) > 0;
    }

    @UnsupportedAppUsage
    public int getUserSerialNumber(int userHandle) {
        try {
            return this.mService.getUserSerialNumber(userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
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

    @SystemApi
    public static final class EnforcingUser implements Parcelable {
        public static final Parcelable.Creator<EnforcingUser> CREATOR = new Parcelable.Creator<EnforcingUser>() {
            /* class android.os.UserManager.EnforcingUser.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public EnforcingUser createFromParcel(Parcel in) {
                return new EnforcingUser(in);
            }

            @Override // android.os.Parcelable.Creator
            public EnforcingUser[] newArray(int size) {
                return new EnforcingUser[size];
            }
        };
        private final int userId;
        private final int userRestrictionSource;

        public EnforcingUser(int userId2, int userRestrictionSource2) {
            this.userId = userId2;
            this.userRestrictionSource = userRestrictionSource2;
        }

        private EnforcingUser(Parcel in) {
            this.userId = in.readInt();
            this.userRestrictionSource = in.readInt();
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
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
}

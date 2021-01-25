package com.android.server.pm;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IStopUserCallback;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyEventLogger;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutServiceInternal;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.hwtheme.HwThemeManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.IProgressListener;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerInternal;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.security.GateKeeper;
import android.service.gatekeeper.IGateKeeperService;
import android.text.TextUtils;
import android.util.AtomicFile;
import android.util.Flog;
import android.util.IntArray;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IAppOpsService;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.LocalServices;
import com.android.server.LockGuard;
import com.android.server.SystemService;
import com.android.server.am.UserState;
import com.android.server.pm.UserManagerService;
import com.android.server.storage.DeviceStorageMonitorInternal;
import com.android.server.wm.ActivityTaskManagerInternal;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class UserManagerService extends AbsUserManagerService {
    private static final int ALLOWED_FLAGS_FOR_CREATE_USERS_PERMISSION = 812;
    private static final String ATTR_CREATION_TIME = "created";
    private static final String ATTR_FLAGS = "flags";
    private static final String ATTR_GUEST_TO_REMOVE = "guestToRemove";
    private static final String ATTR_ICON_PATH = "icon";
    private static final String ATTR_ID = "id";
    private static final String ATTR_KEY = "key";
    private static final String ATTR_LAST_LOGGED_IN_FINGERPRINT = "lastLoggedInFingerprint";
    private static final String ATTR_LAST_LOGGED_IN_FINGERPRINTEX = "lastLoggedInFingerprintEx";
    private static final String ATTR_LAST_LOGGED_IN_TIME = "lastLoggedIn";
    private static final String ATTR_MULTIPLE = "m";
    private static final String ATTR_NEXT_SERIAL_NO = "nextSerialNumber";
    private static final String ATTR_PARTIAL = "partial";
    private static final String ATTR_PROFILE_BADGE = "profileBadge";
    private static final String ATTR_PROFILE_GROUP_ID = "profileGroupId";
    private static final String ATTR_RESTRICTED_PROFILE_PARENT_ID = "restrictedProfileParentId";
    private static final String ATTR_SEED_ACCOUNT_NAME = "seedAccountName";
    private static final String ATTR_SEED_ACCOUNT_TYPE = "seedAccountType";
    private static final String ATTR_SERIAL_NO = "serialNumber";
    private static final String ATTR_TYPE_BOOLEAN = "b";
    private static final String ATTR_TYPE_BUNDLE = "B";
    private static final String ATTR_TYPE_BUNDLE_ARRAY = "BA";
    private static final String ATTR_TYPE_INTEGER = "i";
    private static final String ATTR_TYPE_STRING = "s";
    private static final String ATTR_TYPE_STRING_ARRAY = "sa";
    private static final String ATTR_USER_VERSION = "version";
    private static final String ATTR_VALUE_TYPE = "type";
    static final boolean DBG = false;
    private static final boolean DBG_WITH_STACKTRACE = false;
    private static final long EPOCH_PLUS_30_YEARS = 946080000000L;
    private static final String LOG_TAG = "UserManagerService";
    @VisibleForTesting
    static final int MAX_MANAGED_PROFILES = 1;
    @VisibleForTesting
    static final int MAX_RECENTLY_REMOVED_IDS_SIZE = 100;
    @VisibleForTesting
    static final int MAX_USER_ID = 21474;
    @VisibleForTesting
    static final int MIN_USER_ID = 10;
    private static final boolean RELEASE_DELETED_USER_ID = false;
    private static final int REPAIR_MODE_USER_ID = 127;
    private static final String RESTRICTIONS_FILE_PREFIX = "res_";
    private static final String SUW_FRP_STATE = "hw_suw_frp_state";
    private static final String TAG_ACCOUNT = "account";
    private static final String TAG_DEVICE_OWNER_USER_ID = "deviceOwnerUserId";
    private static final String TAG_DEVICE_POLICY_GLOBAL_RESTRICTIONS = "device_policy_global_restrictions";
    private static final String TAG_DEVICE_POLICY_RESTRICTIONS = "device_policy_restrictions";
    private static final String TAG_ENTRY = "entry";
    private static final String TAG_GLOBAL_RESTRICTION_OWNER_ID = "globalRestrictionOwnerUserId";
    private static final String TAG_GUEST_RESTRICTIONS = "guestRestrictions";
    private static final String TAG_LAST_REQUEST_QUIET_MODE_ENABLED_CALL = "lastRequestQuietModeEnabledCall";
    private static final String TAG_NAME = "name";
    private static final String TAG_RESTRICTIONS = "restrictions";
    private static final String TAG_SEED_ACCOUNT_OPTIONS = "seedAccountOptions";
    private static final String TAG_USER = "user";
    private static final String TAG_USERS = "users";
    private static final String TAG_VALUE = "value";
    private static final String TRON_DEMO_CREATED = "users_demo_created";
    private static final String TRON_GUEST_CREATED = "users_guest_created";
    private static final String TRON_USER_CREATED = "users_user_created";
    private static final String USER_INFO_DIR = ("system" + File.separator + "users");
    private static final String USER_LIST_FILENAME = "userlist.xml";
    private static final String USER_PHOTO_FILENAME = "photo.png";
    private static final String USER_PHOTO_FILENAME_TMP = "photo.png.tmp";
    private static final int USER_VERSION = 7;
    static final int WRITE_USER_DELAY = 2000;
    static final int WRITE_USER_MSG = 1;
    private static final String XML_SUFFIX = ".xml";
    private static final IBinder mUserRestriconToken = new Binder();
    private static UserManagerService sInstance;
    private final String ACTION_DISABLE_QUIET_MODE_AFTER_UNLOCK;
    private final boolean DEFAULT_VALUE;
    private final String PROPERTIES_PRIVACY_SUPPORT_IUDF;
    private boolean isOwnerNameChanged;
    boolean isSupportISec;
    private IAppOpsService mAppOpsService;
    private final Object mAppRestrictionsLock;
    @GuardedBy({"mRestrictionsLock"})
    private final SparseArray<Bundle> mAppliedUserRestrictions;
    @GuardedBy({"mRestrictionsLock"})
    private final SparseArray<Bundle> mBaseUserRestrictions;
    @GuardedBy({"mRestrictionsLock"})
    private final SparseArray<Bundle> mCachedEffectiveUserRestrictions;
    private final LinkedList<Integer> mClonedProfileRecentlyRemovedIds;
    private final Context mContext;
    @GuardedBy({"mRestrictionsLock"})
    private int mDeviceOwnerUserId;
    @GuardedBy({"mRestrictionsLock"})
    private final SparseArray<Bundle> mDevicePolicyGlobalUserRestrictions;
    @GuardedBy({"mRestrictionsLock"})
    private final SparseArray<Bundle> mDevicePolicyLocalUserRestrictions;
    private final BroadcastReceiver mDisableQuietModeCallback;
    @GuardedBy({"mUsersLock"})
    private boolean mForceEphemeralUsers;
    @GuardedBy({"mGuestRestrictions"})
    private final Bundle mGuestRestrictions;
    private final Handler mHandler;
    private boolean mHasClonedProfile;
    @GuardedBy({"mUsersLock"})
    private boolean mIsDeviceManaged;
    @GuardedBy({"mUsersLock"})
    private final SparseBooleanArray mIsUserManaged;
    private final LocalService mLocalService;
    private final LockPatternUtils mLockPatternUtils;
    @GuardedBy({"mPackagesLock"})
    private int mNextSerialNumber;
    private final Object mPackagesLock;
    protected final PackageManagerService mPm;
    @GuardedBy({"mUsersLock"})
    private final LinkedList<Integer> mRecentlyRemovedIds;
    @GuardedBy({"mUsersLock"})
    private final SparseBooleanArray mRemovingUserIds;
    private final Object mRestrictionsLock;
    private final UserDataPreparer mUserDataPreparer;
    @GuardedBy({"mUsersLock"})
    private int[] mUserIds;
    private final File mUserListFile;
    @GuardedBy({"mUserRestrictionsListeners"})
    private final ArrayList<UserManagerInternal.UserRestrictionsListener> mUserRestrictionsListeners;
    @GuardedBy({"mUserStates"})
    private final SparseIntArray mUserStates;
    private int mUserVersion;
    @GuardedBy({"mUsersLock"})
    private final SparseArray<UserData> mUsers;
    private final File mUsersDir;
    private final Object mUsersLock;

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class UserData {
        String account;
        UserInfo info;
        private long mLastRequestQuietModeEnabledMillis;
        boolean persistSeedData;
        String seedAccountName;
        PersistableBundle seedAccountOptions;
        String seedAccountType;
        long startRealtime;
        long unlockRealtime;

        UserData() {
        }

        /* access modifiers changed from: package-private */
        public void setLastRequestQuietModeEnabledMillis(long millis) {
            this.mLastRequestQuietModeEnabledMillis = millis;
        }

        /* access modifiers changed from: package-private */
        public long getLastRequestQuietModeEnabledMillis() {
            return this.mLastRequestQuietModeEnabledMillis;
        }

        /* access modifiers changed from: package-private */
        public void clearSeedAccountData() {
            this.seedAccountName = null;
            this.seedAccountType = null;
            this.seedAccountOptions = null;
            this.persistSeedData = false;
        }
    }

    /* access modifiers changed from: private */
    public class DisableQuietModeUserUnlockedCallback extends IProgressListener.Stub {
        private final IntentSender mTarget;

        public DisableQuietModeUserUnlockedCallback(IntentSender target) {
            Preconditions.checkNotNull(target);
            this.mTarget = target;
        }

        public void onStarted(int id, Bundle extras) {
        }

        public void onProgress(int id, int progress, Bundle extras) {
        }

        public void onFinished(int id, Bundle extras) {
            try {
                UserManagerService.this.mContext.startIntentSender(this.mTarget, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                Slog.e(UserManagerService.LOG_TAG, "Failed to start the target in the callback", e);
            }
        }
    }

    public static UserManagerService getInstance() {
        UserManagerService userManagerService;
        synchronized (UserManagerService.class) {
            userManagerService = sInstance;
        }
        return userManagerService;
    }

    public static class LifeCycle extends SystemService {
        private UserManagerService mUms;

        public LifeCycle(Context context) {
            super(context);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.pm.UserManagerService$LifeCycle */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r0v1, types: [com.android.server.pm.UserManagerService, android.os.IBinder] */
        /* JADX WARNING: Unknown variable types count: 1 */
        @Override // com.android.server.SystemService
        public void onStart() {
            this.mUms = UserManagerService.getInstance();
            publishBinderService(UserManagerService.TAG_USER, this.mUms);
        }

        @Override // com.android.server.SystemService
        public void onBootPhase(int phase) {
            if (phase == 550) {
                this.mUms.cleanupPartialUsers();
            }
        }

        @Override // com.android.server.SystemService
        public void onStartUser(int userHandle) {
            synchronized (this.mUms.mUsersLock) {
                UserData user = this.mUms.getUserDataLU(userHandle);
                if (user != null) {
                    user.startRealtime = SystemClock.elapsedRealtime();
                }
            }
        }

        @Override // com.android.server.SystemService
        public void onUnlockUser(int userHandle) {
            synchronized (this.mUms.mUsersLock) {
                UserData user = this.mUms.getUserDataLU(userHandle);
                if (user != null) {
                    user.unlockRealtime = SystemClock.elapsedRealtime();
                }
            }
        }

        @Override // com.android.server.SystemService
        public void onStopUser(int userHandle) {
            synchronized (this.mUms.mUsersLock) {
                UserData user = this.mUms.getUserDataLU(userHandle);
                if (user != null) {
                    user.startRealtime = 0;
                    user.unlockRealtime = 0;
                }
            }
        }
    }

    @VisibleForTesting
    UserManagerService(Context context) {
        this(context, null, null, new Object(), context.getCacheDir());
    }

    UserManagerService(Context context, PackageManagerService pm, UserDataPreparer userDataPreparer, Object packagesLock) {
        this(context, pm, userDataPreparer, packagesLock, Environment.getDataDirectory());
    }

    private UserManagerService(Context context, PackageManagerService pm, UserDataPreparer userDataPreparer, Object packagesLock, File dataDir) {
        this.mUsersLock = LockGuard.installNewLock(2);
        this.mRestrictionsLock = new Object();
        this.mAppRestrictionsLock = new Object();
        this.DEFAULT_VALUE = false;
        this.PROPERTIES_PRIVACY_SUPPORT_IUDF = "ro.config.support_iudf";
        this.isSupportISec = SystemProperties.getBoolean("ro.config.support_iudf", false);
        this.mHasClonedProfile = false;
        this.mUsers = new SparseArray<>();
        this.mBaseUserRestrictions = new SparseArray<>();
        this.mCachedEffectiveUserRestrictions = new SparseArray<>();
        this.mAppliedUserRestrictions = new SparseArray<>();
        this.mDevicePolicyGlobalUserRestrictions = new SparseArray<>();
        this.mDeviceOwnerUserId = -10000;
        this.mDevicePolicyLocalUserRestrictions = new SparseArray<>();
        this.mGuestRestrictions = new Bundle();
        this.mRemovingUserIds = new SparseBooleanArray();
        this.mRecentlyRemovedIds = new LinkedList<>();
        this.mClonedProfileRecentlyRemovedIds = new LinkedList<>();
        this.mUserVersion = 0;
        this.mIsUserManaged = new SparseBooleanArray();
        this.mUserRestrictionsListeners = new ArrayList<>();
        this.ACTION_DISABLE_QUIET_MODE_AFTER_UNLOCK = "com.android.server.pm.DISABLE_QUIET_MODE_AFTER_UNLOCK";
        this.mDisableQuietModeCallback = new BroadcastReceiver() {
            /* class com.android.server.pm.UserManagerService.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("com.android.server.pm.DISABLE_QUIET_MODE_AFTER_UNLOCK".equals(intent.getAction())) {
                    BackgroundThread.getHandler().post(new Runnable(intent.getIntExtra("android.intent.extra.USER_ID", -10000), (IntentSender) intent.getParcelableExtra("android.intent.extra.INTENT")) {
                        /* class com.android.server.pm.$$Lambda$UserManagerService$1$DQ_02g7kZ7QrJXO6aCATwE6DYCE */
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ IntentSender f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            UserManagerService.AnonymousClass1.this.lambda$onReceive$0$UserManagerService$1(this.f$1, this.f$2);
                        }
                    });
                }
            }

            public /* synthetic */ void lambda$onReceive$0$UserManagerService$1(int userHandle, IntentSender target) {
                UserManagerService.this.setQuietModeEnabled(userHandle, false, target, null);
            }
        };
        this.mUserStates = new SparseIntArray();
        this.isOwnerNameChanged = false;
        this.mContext = context;
        this.mPm = pm;
        this.mPackagesLock = packagesLock;
        this.mHandler = new MainHandler();
        this.mUserDataPreparer = userDataPreparer;
        synchronized (this.mPackagesLock) {
            this.mUsersDir = new File(dataDir, USER_INFO_DIR);
            this.mUsersDir.mkdirs();
            new File(this.mUsersDir, String.valueOf(0)).mkdirs();
            FileUtils.setPermissions(this.mUsersDir.toString(), 509, -1, -1);
            this.mUserListFile = new File(this.mUsersDir, USER_LIST_FILENAME);
            initDefaultGuestRestrictions();
            readUserListLP();
            sInstance = this;
            UserInfo info = getUserInfoLU(0);
            if (!(info == null || info.name == null)) {
                this.isOwnerNameChanged = !info.name.equals(this.mContext.getResources().getString(17040715));
            }
        }
        this.mLocalService = new LocalService();
        LocalServices.addService(UserManagerInternal.class, this.mLocalService);
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mUserStates.put(0, 0);
    }

    /* access modifiers changed from: package-private */
    public void systemReady() {
        this.mAppOpsService = IAppOpsService.Stub.asInterface(ServiceManager.getService("appops"));
        synchronized (this.mRestrictionsLock) {
            applyUserRestrictionsLR(0);
        }
        UserInfo currentGuestUser = findCurrentGuestUser();
        if (currentGuestUser != null && !hasUserRestriction("no_config_wifi", currentGuestUser.id)) {
            setUserRestriction("no_config_wifi", true, currentGuestUser.id);
        }
        this.mContext.registerReceiver(this.mDisableQuietModeCallback, new IntentFilter("com.android.server.pm.DISABLE_QUIET_MODE_AFTER_UNLOCK"), null, this.mHandler);
    }

    /* access modifiers changed from: package-private */
    public void cleanupPartialUsers() {
        ArrayList<UserInfo> partials = new ArrayList<>();
        synchronized (this.mUsersLock) {
            int userSize = this.mUsers.size();
            for (int i = 0; i < userSize; i++) {
                UserInfo ui = this.mUsers.valueAt(i).info;
                if ((ui.partial || ui.guestToRemove || ui.isEphemeral()) && i != 0) {
                    partials.add(ui);
                    addRemovingUserIdLocked(ui.id);
                    ui.partial = true;
                }
            }
        }
        int partialsSize = partials.size();
        for (int i2 = 0; i2 < partialsSize; i2++) {
            UserInfo ui2 = partials.get(i2);
            Slog.w(LOG_TAG, "Removing partially created user " + ui2.id + " (name=" + ui2.name + ")");
            removeUserState(ui2.id);
        }
    }

    public String getUserAccount(int userId) {
        String str;
        checkManageUserAndAcrossUsersFullPermission("get user account");
        synchronized (this.mUsersLock) {
            str = this.mUsers.get(userId).account;
        }
        return str;
    }

    public void setUserAccount(int userId, String accountName) {
        checkManageUserAndAcrossUsersFullPermission("set user account");
        UserData userToUpdate = null;
        synchronized (this.mPackagesLock) {
            synchronized (this.mUsersLock) {
                UserData userData = this.mUsers.get(userId);
                if (userData == null) {
                    Slog.e(LOG_TAG, "User not found for setting user account: u" + userId);
                    return;
                } else if (!Objects.equals(userData.account, accountName)) {
                    userData.account = accountName;
                    userToUpdate = userData;
                }
            }
            if (userToUpdate != null) {
                writeUserLP(userToUpdate);
            }
        }
    }

    public UserInfo getPrimaryUser() {
        checkManageUsersPermission("query users");
        synchronized (this.mUsersLock) {
            int userSize = this.mUsers.size();
            for (int i = 0; i < userSize; i++) {
                UserInfo ui = this.mUsers.valueAt(i).info;
                if (ui.isPrimary() && !this.mRemovingUserIds.get(ui.id)) {
                    return ui;
                }
            }
            return null;
        }
    }

    public List<UserInfo> getUsers(boolean excludeDying) {
        UserData userData;
        checkManageOrCreateUsersPermission("query users");
        ArrayList<UserInfo> users = new ArrayList<>(this.mUsers.size());
        ArrayList<UserData> tmpUserData = new ArrayList<>();
        synchronized (this.mUsersLock) {
            int userSize = this.mUsers.size();
            for (int i = 0; i < userSize; i++) {
                UserInfo ui = this.mUsers.valueAt(i).info;
                if (!ui.partial) {
                    if (!excludeDying || !this.mRemovingUserIds.get(ui.id)) {
                        users.add(userWithName(ui));
                    }
                    if (ui.id == 0 && !this.isOwnerNameChanged) {
                        boolean nameChanged = false;
                        String ownerName = this.mContext.getResources().getString(17040715);
                        if (!TextUtils.isEmpty(ui.name) && !ui.name.equals(ownerName)) {
                            nameChanged = true;
                        }
                        ui.name = ownerName;
                        if (nameChanged && (userData = getUserDataNoChecks(ui.id)) != null) {
                            userData.info = ui;
                            tmpUserData.add(userData);
                        }
                    }
                }
            }
        }
        synchronized (this.mPackagesLock) {
            Iterator<UserData> it = tmpUserData.iterator();
            while (it.hasNext()) {
                writeUserLP(it.next());
            }
        }
        return users;
    }

    public List<UserInfo> getProfiles(int userId, boolean enabledOnly) {
        List<UserInfo> profilesLU;
        boolean returnFullInfo = true;
        if (userId != UserHandle.getCallingUserId()) {
            checkManageOrCreateUsersPermission("getting profiles related to user " + userId);
        } else {
            returnFullInfo = hasManageUsersPermission();
        }
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mUsersLock) {
                profilesLU = getProfilesLU(userId, enabledOnly, returnFullInfo);
            }
            return profilesLU;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public int[] getProfileIds(int userId, boolean enabledOnly) {
        int[] array;
        if (userId != UserHandle.getCallingUserId()) {
            checkManageOrCreateUsersPermission("getting profiles related to user " + userId);
        }
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mUsersLock) {
                array = getProfileIdsLU(userId, enabledOnly).toArray();
            }
            return array;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    @GuardedBy({"mUsersLock"})
    private List<UserInfo> getProfilesLU(int userId, boolean enabledOnly, boolean fullInfo) {
        UserInfo userInfo;
        IntArray profileIds = getProfileIdsLU(userId, enabledOnly);
        ArrayList<UserInfo> users = new ArrayList<>(profileIds.size());
        for (int i = 0; i < profileIds.size(); i++) {
            UserInfo userInfo2 = this.mUsers.get(profileIds.get(i)).info;
            if (!fullInfo) {
                userInfo = new UserInfo(userInfo2);
                userInfo.name = null;
                userInfo.iconPath = null;
            } else {
                userInfo = userWithName(userInfo2);
            }
            users.add(userInfo);
        }
        return users;
    }

    @GuardedBy({"mUsersLock"})
    private IntArray getProfileIdsLU(int userId, boolean enabledOnly) {
        UserInfo user = getUserInfoLU(userId);
        IntArray result = new IntArray(this.mUsers.size());
        if (user == null) {
            return result;
        }
        int userSize = this.mUsers.size();
        for (int i = 0; i < userSize; i++) {
            UserInfo profile = this.mUsers.valueAt(i).info;
            if (isProfileOf(user, profile) && ((!enabledOnly || profile.isEnabled()) && !this.mRemovingUserIds.get(profile.id) && !profile.partial)) {
                result.add(profile.id);
            }
        }
        return result;
    }

    public int getCredentialOwnerProfile(int userHandle) {
        checkManageUsersPermission("get the credential owner");
        if (!this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userHandle)) {
            synchronized (this.mUsersLock) {
                UserInfo profileParent = getProfileParentLU(userHandle);
                if (profileParent != null) {
                    return profileParent.id;
                }
            }
        }
        return userHandle;
    }

    public boolean isSameProfileGroup(int userId, int otherUserId) {
        if (userId == otherUserId) {
            return true;
        }
        checkManageUsersPermission("check if in the same profile group");
        return isSameProfileGroupNoChecks(userId, otherUserId);
    }

    private boolean isSameProfileGroupNoChecks(int userId, int otherUserId) {
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userId);
            boolean z = false;
            if (userInfo != null) {
                if (userInfo.profileGroupId != -10000) {
                    UserInfo otherUserInfo = getUserInfoLU(otherUserId);
                    if (otherUserInfo != null) {
                        if (otherUserInfo.profileGroupId != -10000) {
                            if (userInfo.profileGroupId == otherUserInfo.profileGroupId) {
                                z = true;
                            }
                            return z;
                        }
                    }
                    return false;
                }
            }
            return false;
        }
    }

    public UserInfo getProfileParent(int userHandle) {
        UserInfo profileParentLU;
        checkManageUsersPermission("get the profile parent");
        synchronized (this.mUsersLock) {
            profileParentLU = getProfileParentLU(userHandle);
        }
        return profileParentLU;
    }

    public int getProfileParentId(int userHandle) {
        checkManageUsersPermission("get the profile parent");
        return this.mLocalService.getProfileParentId(userHandle);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mUsersLock"})
    private UserInfo getProfileParentLU(int userHandle) {
        int parentUserId;
        UserInfo profile = getUserInfoLU(userHandle);
        if (profile == null || (parentUserId = profile.profileGroupId) == userHandle || parentUserId == -10000) {
            return null;
        }
        return getUserInfoLU(parentUserId);
    }

    private static boolean isProfileOf(UserInfo user, UserInfo profile) {
        return user.id == profile.id || (user.profileGroupId != -10000 && user.profileGroupId == profile.profileGroupId);
    }

    private void broadcastProfileAvailabilityChanges(UserHandle profileHandle, UserHandle parentHandle, boolean inQuietMode) {
        Intent intent = new Intent();
        if (inQuietMode) {
            intent.setAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
        } else {
            intent.setAction("android.intent.action.MANAGED_PROFILE_AVAILABLE");
        }
        intent.putExtra("android.intent.extra.QUIET_MODE", inQuietMode);
        intent.putExtra("android.intent.extra.USER", profileHandle);
        intent.putExtra("android.intent.extra.user_handle", profileHandle.getIdentifier());
        intent.addFlags(1073741824);
        this.mContext.sendBroadcastAsUser(intent, parentHandle);
    }

    public boolean requestQuietModeEnabled(String callingPackage, boolean enableQuietMode, int userHandle, IntentSender target) {
        Preconditions.checkNotNull(callingPackage);
        if (!enableQuietMode || target == null) {
            boolean needToShowConfirmCredential = true;
            ensureCanModifyQuietMode(callingPackage, Binder.getCallingUid(), target != null);
            long identity = Binder.clearCallingIdentity();
            boolean result = false;
            if (enableQuietMode) {
                try {
                    setQuietModeEnabled(userHandle, true, target, callingPackage);
                    result = true;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            } else {
                if (!this.mLockPatternUtils.isSecure(userHandle) || StorageManager.isUserKeyUnlocked(userHandle)) {
                    needToShowConfirmCredential = false;
                }
                if (needToShowConfirmCredential) {
                    showConfirmCredentialToDisableQuietMode(userHandle, target);
                } else {
                    setQuietModeEnabled(userHandle, false, target, callingPackage);
                    result = true;
                }
            }
            Binder.restoreCallingIdentity(identity);
            return result;
        }
        throw new IllegalArgumentException("target should only be specified when we are disabling quiet mode.");
    }

    private void ensureCanModifyQuietMode(String callingPackage, int callingUid, boolean startIntent) {
        if (!hasManageUsersPermission()) {
            if (startIntent) {
                throw new SecurityException("MANAGE_USERS permission is required to start intent after disabling quiet mode.");
            } else if (!hasPermissionGranted("android.permission.MODIFY_QUIET_MODE", callingUid)) {
                verifyCallingPackage(callingPackage, callingUid);
                ShortcutServiceInternal shortcutInternal = (ShortcutServiceInternal) LocalServices.getService(ShortcutServiceInternal.class);
                if (shortcutInternal == null || !shortcutInternal.isForegroundDefaultLauncher(callingPackage, callingUid)) {
                    throw new SecurityException("Can't modify quiet mode, caller is neither foreground default launcher nor has MANAGE_USERS/MODIFY_QUIET_MODE permission");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setQuietModeEnabled(int userHandle, boolean enableQuietMode, IntentSender target, String callingPackage) {
        UserInfo profile;
        UserInfo parent;
        UserData profileUserData;
        synchronized (this.mUsersLock) {
            profile = getUserInfoLU(userHandle);
            parent = getProfileParentLU(userHandle);
            if (profile == null || !profile.isManagedProfile()) {
                throw new IllegalArgumentException("User " + userHandle + " is not a profile");
            } else if (profile.isQuietModeEnabled() == enableQuietMode) {
                Slog.i(LOG_TAG, "Quiet mode is already " + enableQuietMode);
                return;
            } else {
                profile.flags ^= 128;
                profileUserData = getUserDataLU(profile.id);
            }
        }
        synchronized (this.mPackagesLock) {
            writeUserLP(profileUserData);
        }
        IProgressListener callback = null;
        if (enableQuietMode) {
            try {
                ActivityManager.getService().stopUser(userHandle, true, (IStopUserCallback) null);
                ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).killForegroundAppsForUser(userHandle);
            } catch (RemoteException e) {
                e.rethrowAsRuntimeException();
            }
        } else {
            if (target != null) {
                callback = new DisableQuietModeUserUnlockedCallback(target);
            }
            ActivityManager.getService().startUserInBackgroundWithListener(userHandle, callback);
        }
        logQuietModeEnabled(userHandle, enableQuietMode, callingPackage);
        broadcastProfileAvailabilityChanges(profile.getUserHandle(), parent.getUserHandle(), enableQuietMode);
    }

    private void logQuietModeEnabled(int userHandle, boolean enableQuietMode, String callingPackage) {
        UserData userData;
        long period;
        synchronized (this.mUsersLock) {
            userData = getUserDataLU(userHandle);
        }
        if (userData != null) {
            long now = System.currentTimeMillis();
            if (userData.getLastRequestQuietModeEnabledMillis() != 0) {
                period = now - userData.getLastRequestQuietModeEnabledMillis();
            } else {
                period = now - userData.info.creationTime;
            }
            DevicePolicyEventLogger.createEvent(55).setStrings(new String[]{callingPackage}).setBoolean(enableQuietMode).setTimePeriod(period).write();
            userData.setLastRequestQuietModeEnabledMillis(now);
        }
    }

    public boolean isQuietModeEnabled(int userHandle) {
        UserInfo info;
        synchronized (this.mPackagesLock) {
            synchronized (this.mUsersLock) {
                info = getUserInfoLU(userHandle);
            }
            if (info == null || !info.isManagedProfile()) {
                return false;
            }
            return info.isQuietModeEnabled();
        }
    }

    private void showConfirmCredentialToDisableQuietMode(int userHandle, IntentSender target) {
        Intent unlockIntent = ((KeyguardManager) this.mContext.getSystemService("keyguard")).createConfirmDeviceCredentialIntent(null, null, userHandle);
        if (unlockIntent != null) {
            Intent callBackIntent = new Intent("com.android.server.pm.DISABLE_QUIET_MODE_AFTER_UNLOCK");
            if (target != null) {
                callBackIntent.putExtra("android.intent.extra.INTENT", target);
            }
            callBackIntent.putExtra("android.intent.extra.USER_ID", userHandle);
            callBackIntent.setPackage(this.mContext.getPackageName());
            callBackIntent.addFlags(268435456);
            unlockIntent.putExtra("android.intent.extra.INTENT", PendingIntent.getBroadcast(this.mContext, 0, callBackIntent, 1409286144).getIntentSender());
            unlockIntent.setFlags(276824064);
            this.mContext.startActivity(unlockIntent);
        }
    }

    public void setUserEnabled(int userId) {
        UserInfo info;
        checkManageUsersPermission("enable user");
        synchronized (this.mPackagesLock) {
            synchronized (this.mUsersLock) {
                info = getUserInfoLU(userId);
            }
            if (info != null && !info.isEnabled()) {
                info.flags ^= 64;
                writeUserLP(getUserDataLU(info.id));
            }
        }
    }

    public void setUserAdmin(int userId) {
        UserInfo info;
        checkManageUserAndAcrossUsersFullPermission("set user admin");
        synchronized (this.mPackagesLock) {
            synchronized (this.mUsersLock) {
                info = getUserInfoLU(userId);
            }
            if (info != null && !info.isAdmin()) {
                info.flags ^= 2;
                writeUserLP(getUserDataLU(info.id));
                setUserRestriction("no_sms", false, userId);
                setUserRestriction("no_outgoing_calls", false, userId);
            }
        }
    }

    public void evictCredentialEncryptionKey(int userId) {
        checkManageUsersPermission("evict CE key");
        IActivityManager am = ActivityManagerNative.getDefault();
        long identity = Binder.clearCallingIdentity();
        try {
            am.restartUserInBackground(userId);
            Binder.restoreCallingIdentity(identity);
        } catch (RemoteException re) {
            throw re.rethrowAsRuntimeException();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    public UserInfo getUserInfo(int userId) {
        UserInfo userWithName;
        checkManageOrCreateUsersPermission("query user");
        synchronized (this.mUsersLock) {
            userWithName = userWithName(getUserInfoLU(userId));
        }
        return userWithName;
    }

    private UserInfo userWithName(UserInfo orig) {
        if (orig == null || orig.name != null || orig.id != 0) {
            return orig;
        }
        UserInfo withName = new UserInfo(orig);
        withName.name = getOwnerName();
        return withName;
    }

    public int getManagedProfileBadge(int userId) {
        int i;
        checkManageOrInteractPermIfCallerInOtherProfileGroup(userId, "getManagedProfileBadge");
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userId);
            i = userInfo != null ? userInfo.profileBadge : 0;
        }
        return i;
    }

    public boolean isManagedProfile(int userId) {
        boolean z;
        checkManageOrInteractPermIfCallerInOtherProfileGroup(userId, "isManagedProfile");
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userId);
            z = userInfo != null && userInfo.isManagedProfile();
        }
        return z;
    }

    public boolean isUserUnlockingOrUnlocked(int userId) {
        checkManageOrInteractPermIfCallerInOtherProfileGroup(userId, "isUserUnlockingOrUnlocked");
        return this.mLocalService.isUserUnlockingOrUnlocked(userId);
    }

    public boolean isUserUnlocked(int userId) {
        checkManageOrInteractPermIfCallerInOtherProfileGroup(userId, "isUserUnlocked");
        return this.mLocalService.isUserUnlocked(userId);
    }

    public boolean isUserRunning(int userId) {
        checkManageOrInteractPermIfCallerInOtherProfileGroup(userId, "isUserRunning");
        return this.mLocalService.isUserRunning(userId);
    }

    public String getUserName() {
        String str;
        if (hasManageUsersOrPermission("android.permission.GET_ACCOUNTS_PRIVILEGED")) {
            int userId = UserHandle.getUserId(Binder.getCallingUid());
            synchronized (this.mUsersLock) {
                UserInfo userInfo = userWithName(getUserInfoLU(userId));
                str = userInfo == null ? "" : userInfo.name;
            }
            return str;
        }
        throw new SecurityException("You need MANAGE_USERS or GET_ACCOUNTS_PRIVILEGED permissions to: get user name");
    }

    public long getUserStartRealtime() {
        int userId = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mUsersLock) {
            UserData user = getUserDataLU(userId);
            if (user == null) {
                return 0;
            }
            return user.startRealtime;
        }
    }

    public long getUserUnlockRealtime() {
        synchronized (this.mUsersLock) {
            UserData user = getUserDataLU(UserHandle.getUserId(Binder.getCallingUid()));
            if (user == null) {
                return 0;
            }
            return user.unlockRealtime;
        }
    }

    private void checkManageOrInteractPermIfCallerInOtherProfileGroup(int userId, String name) {
        int callingUserId = UserHandle.getCallingUserId();
        if (callingUserId != userId && !isSameProfileGroupNoChecks(callingUserId, userId) && !hasManageUsersPermission() && !hasPermissionGranted("android.permission.INTERACT_ACROSS_USERS", Binder.getCallingUid())) {
            throw new SecurityException("You need INTERACT_ACROSS_USERS or MANAGE_USERS permission to: check " + name);
        }
    }

    public boolean isDemoUser(int userId) {
        boolean z;
        if (UserHandle.getCallingUserId() == userId || hasManageUsersPermission()) {
            synchronized (this.mUsersLock) {
                UserInfo userInfo = getUserInfoLU(userId);
                z = userInfo != null && userInfo.isDemo();
            }
            return z;
        }
        throw new SecurityException("You need MANAGE_USERS permission to query if u=" + userId + " is a demo user");
    }

    public boolean isRestricted() {
        boolean isRestricted;
        synchronized (this.mUsersLock) {
            isRestricted = getUserInfoLU(UserHandle.getCallingUserId()).isRestricted();
        }
        return isRestricted;
    }

    public boolean canHaveRestrictedProfile(int userId) {
        checkManageUsersPermission("canHaveRestrictedProfile");
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userId);
            boolean z = false;
            if (userInfo != null) {
                if (userInfo.canHaveProfile()) {
                    if (!userInfo.isAdmin()) {
                        return false;
                    }
                    if (!this.mIsDeviceManaged && !this.mIsUserManaged.get(userId)) {
                        z = true;
                    }
                    return z;
                }
            }
            return false;
        }
    }

    public boolean hasRestrictedProfiles() {
        checkManageUsersPermission("hasRestrictedProfiles");
        int callingUserId = UserHandle.getCallingUserId();
        synchronized (this.mUsersLock) {
            int userSize = this.mUsers.size();
            for (int i = 0; i < userSize; i++) {
                UserInfo profile = this.mUsers.valueAt(i).info;
                if (callingUserId != profile.id && profile.restrictedProfileParentId == callingUserId) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mUsersLock"})
    private UserInfo getUserInfoLU(int userId) {
        UserData userData = this.mUsers.get(userId);
        if (userData != null && userData.info.partial && !this.mRemovingUserIds.get(userId)) {
            Slog.w(LOG_TAG, "getUserInfo: unknown user #" + userId);
            return null;
        } else if (userData != null) {
            return userData.info;
        } else {
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mUsersLock"})
    private UserData getUserDataLU(int userId) {
        UserData userData = this.mUsers.get(userId);
        if (userData == null || !userData.info.partial || this.mRemovingUserIds.get(userId)) {
            return userData;
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private UserInfo getUserInfoNoChecks(int userId) {
        UserInfo userInfo;
        synchronized (this.mUsersLock) {
            UserData userData = this.mUsers.get(userId);
            userInfo = userData != null ? userData.info : null;
        }
        return userInfo;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private UserData getUserDataNoChecks(int userId) {
        UserData userData;
        synchronized (this.mUsersLock) {
            userData = this.mUsers.get(userId);
        }
        return userData;
    }

    public boolean exists(int userId) {
        return this.mLocalService.exists(userId);
    }

    public void setUserName(int userId, String name) {
        checkManageUsersPermission("rename users");
        boolean changed = false;
        synchronized (this.mPackagesLock) {
            UserData userData = getUserDataNoChecks(userId);
            if (userData != null) {
                if (!userData.info.partial) {
                    if (name != null && !name.equals(userData.info.name)) {
                        userData.info.name = name;
                        writeUserLP(userData);
                        changed = true;
                    }
                    if (name != null && userId == 0) {
                        if (this.mContext.getResources() == null || !name.equals(this.mContext.getResources().getString(17040715))) {
                            this.isOwnerNameChanged = true;
                        } else {
                            this.isOwnerNameChanged = false;
                        }
                    }
                }
            }
            Slog.w(LOG_TAG, "setUserName: unknown user #" + userId);
            return;
        }
        if (changed) {
            long ident = Binder.clearCallingIdentity();
            try {
                sendUserInfoChangedBroadcast(userId);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void setUserIcon(int userId, Bitmap bitmap) {
        checkManageUsersPermission("update users");
        if (hasUserRestriction("no_set_user_icon", userId)) {
            Log.w(LOG_TAG, "Cannot set user icon. DISALLOW_SET_USER_ICON is enabled.");
        } else {
            this.mLocalService.setUserIcon(userId, bitmap);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendUserInfoChangedBroadcast(int userId) {
        Intent changedIntent = new Intent("android.intent.action.USER_INFO_CHANGED");
        changedIntent.putExtra("android.intent.extra.user_handle", userId);
        changedIntent.addFlags(1073741824);
        this.mContext.sendBroadcastAsUser(changedIntent, UserHandle.ALL);
    }

    public ParcelFileDescriptor getUserIcon(int targetUserId) {
        if (hasManageUsersOrPermission("android.permission.GET_ACCOUNTS_PRIVILEGED")) {
            synchronized (this.mPackagesLock) {
                UserInfo targetUserInfo = getUserInfoNoChecks(targetUserId);
                if (targetUserInfo != null) {
                    if (!targetUserInfo.partial) {
                        int callingUserId = UserHandle.getCallingUserId();
                        int callingGroupId = getUserInfoNoChecks(callingUserId).profileGroupId;
                        boolean sameGroup = callingGroupId != -10000 && callingGroupId == targetUserInfo.profileGroupId;
                        if (callingUserId != targetUserId && !sameGroup) {
                            checkManageUsersPermission("get the icon of a user who is not related");
                        }
                        if (targetUserInfo.iconPath == null) {
                            return null;
                        }
                        String iconPath = targetUserInfo.iconPath;
                        try {
                            return ParcelFileDescriptor.open(new File(iconPath), 268435456);
                        } catch (FileNotFoundException e) {
                            Log.e(LOG_TAG, "Couldn't find icon file", e);
                            return null;
                        }
                    }
                }
                Slog.w(LOG_TAG, "getUserIcon: unknown user #" + targetUserId);
                return null;
            }
        }
        throw new SecurityException("You need MANAGE_USERS or GET_ACCOUNTS_PRIVILEGED permissions to: get user icon");
    }

    public void makeInitialized(int userId) {
        UserData userData;
        checkManageUsersPermission("makeInitialized");
        boolean scheduleWriteUser = false;
        synchronized (this.mUsersLock) {
            userData = this.mUsers.get(userId);
            if (userData != null) {
                if (!userData.info.partial) {
                    if ((userData.info.flags & 16) == 0) {
                        userData.info.flags |= 16;
                        scheduleWriteUser = true;
                    }
                }
            }
            Slog.w(LOG_TAG, "makeInitialized: unknown user #" + userId);
            return;
        }
        if (scheduleWriteUser) {
            scheduleWriteUser(userData);
        }
    }

    private void initDefaultGuestRestrictions() {
        synchronized (this.mGuestRestrictions) {
            if (this.mGuestRestrictions.isEmpty()) {
                this.mGuestRestrictions.putBoolean("no_config_wifi", true);
                this.mGuestRestrictions.putBoolean("no_install_unknown_sources", true);
                this.mGuestRestrictions.putBoolean("no_outgoing_calls", true);
                this.mGuestRestrictions.putBoolean("no_sms", true);
            }
        }
    }

    public Bundle getDefaultGuestRestrictions() {
        Bundle bundle;
        checkManageUsersPermission("getDefaultGuestRestrictions");
        synchronized (this.mGuestRestrictions) {
            bundle = new Bundle(this.mGuestRestrictions);
        }
        return bundle;
    }

    public void setDefaultGuestRestrictions(Bundle restrictions) {
        checkManageUsersPermission("setDefaultGuestRestrictions");
        synchronized (this.mGuestRestrictions) {
            this.mGuestRestrictions.clear();
            this.mGuestRestrictions.putAll(restrictions);
        }
        synchronized (this.mPackagesLock) {
            writeUserListLP();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDevicePolicyUserRestrictionsInner(int userId, Bundle restrictions, boolean isDeviceOwner, int cameraRestrictionScope) {
        boolean globalChanged;
        boolean localChanged;
        Bundle global = new Bundle();
        Bundle local = new Bundle();
        UserRestrictionsUtils.sortToGlobalAndLocal(restrictions, isDeviceOwner, cameraRestrictionScope, global, local);
        synchronized (this.mRestrictionsLock) {
            globalChanged = updateRestrictionsIfNeededLR(userId, global, this.mDevicePolicyGlobalUserRestrictions);
            localChanged = updateRestrictionsIfNeededLR(userId, local, this.mDevicePolicyLocalUserRestrictions);
            if (isDeviceOwner) {
                this.mDeviceOwnerUserId = userId;
            } else if (this.mDeviceOwnerUserId == userId) {
                this.mDeviceOwnerUserId = -10000;
            }
        }
        synchronized (this.mPackagesLock) {
            if (localChanged || globalChanged) {
                writeUserLP(getUserDataNoChecks(userId));
            }
        }
        synchronized (this.mRestrictionsLock) {
            if (globalChanged) {
                try {
                    applyUserRestrictionsForAllUsersLR();
                } catch (Throwable th) {
                    throw th;
                }
            } else if (localChanged) {
                applyUserRestrictionsLR(userId);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateRestrictionsIfNeededLR(int userId, Bundle restrictions, SparseArray<Bundle> restrictionsArray) {
        boolean changed = !UserRestrictionsUtils.areEqual(restrictionsArray.get(userId), restrictions);
        if (changed) {
            if (!UserRestrictionsUtils.isEmpty(restrictions)) {
                restrictionsArray.put(userId, restrictions);
            } else {
                restrictionsArray.delete(userId);
            }
        }
        return changed;
    }

    @GuardedBy({"mRestrictionsLock"})
    private Bundle computeEffectiveUserRestrictionsLR(int userId) {
        Bundle baseRestrictions = UserRestrictionsUtils.nonNull(this.mBaseUserRestrictions.get(userId));
        Bundle global = UserRestrictionsUtils.mergeAll(this.mDevicePolicyGlobalUserRestrictions);
        Bundle local = this.mDevicePolicyLocalUserRestrictions.get(userId);
        if (UserRestrictionsUtils.isEmpty(global) && UserRestrictionsUtils.isEmpty(local)) {
            return baseRestrictions;
        }
        Bundle effective = UserRestrictionsUtils.clone(baseRestrictions);
        UserRestrictionsUtils.merge(effective, global);
        UserRestrictionsUtils.merge(effective, local);
        return effective;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mRestrictionsLock"})
    private void invalidateEffectiveUserRestrictionsLR(int userId) {
        this.mCachedEffectiveUserRestrictions.remove(userId);
    }

    private Bundle getEffectiveUserRestrictions(int userId) {
        Bundle restrictions;
        synchronized (this.mRestrictionsLock) {
            restrictions = this.mCachedEffectiveUserRestrictions.get(userId);
            if (restrictions == null) {
                restrictions = computeEffectiveUserRestrictionsLR(userId);
                this.mCachedEffectiveUserRestrictions.put(userId, restrictions);
            }
        }
        return restrictions;
    }

    public boolean hasUserRestriction(String restrictionKey, int userId) {
        Bundle restrictions;
        if (UserRestrictionsUtils.isValidRestriction(restrictionKey) && (restrictions = getEffectiveUserRestrictions(userId)) != null && restrictions.getBoolean(restrictionKey)) {
            return true;
        }
        return false;
    }

    public boolean hasUserRestrictionOnAnyUser(String restrictionKey) {
        if (!UserRestrictionsUtils.isValidRestriction(restrictionKey)) {
            return false;
        }
        List<UserInfo> users = getUsers(true);
        for (int i = 0; i < users.size(); i++) {
            Bundle restrictions = getEffectiveUserRestrictions(users.get(i).id);
            if (restrictions != null && restrictions.getBoolean(restrictionKey)) {
                return true;
            }
        }
        return false;
    }

    public int getUserRestrictionSource(String restrictionKey, int userId) {
        List<UserManager.EnforcingUser> enforcingUsers = getUserRestrictionSources(restrictionKey, userId);
        int result = 0;
        for (int i = enforcingUsers.size() - 1; i >= 0; i--) {
            result |= enforcingUsers.get(i).getUserRestrictionSource();
        }
        return result;
    }

    public List<UserManager.EnforcingUser> getUserRestrictionSources(String restrictionKey, int userId) {
        checkManageUsersPermission("getUserRestrictionSource");
        if (!hasUserRestriction(restrictionKey, userId)) {
            return Collections.emptyList();
        }
        List<UserManager.EnforcingUser> result = new ArrayList<>();
        if (hasBaseUserRestriction(restrictionKey, userId)) {
            result.add(new UserManager.EnforcingUser(-10000, 1));
        }
        synchronized (this.mRestrictionsLock) {
            if (UserRestrictionsUtils.contains(this.mDevicePolicyLocalUserRestrictions.get(userId), restrictionKey)) {
                result.add(getEnforcingUserLocked(userId));
            }
            for (int i = this.mDevicePolicyGlobalUserRestrictions.size() - 1; i >= 0; i--) {
                int profileUserId = this.mDevicePolicyGlobalUserRestrictions.keyAt(i);
                if (UserRestrictionsUtils.contains(this.mDevicePolicyGlobalUserRestrictions.valueAt(i), restrictionKey)) {
                    result.add(getEnforcingUserLocked(profileUserId));
                }
            }
        }
        return result;
    }

    @GuardedBy({"mRestrictionsLock"})
    private UserManager.EnforcingUser getEnforcingUserLocked(int userId) {
        int source;
        if (this.mDeviceOwnerUserId == userId) {
            source = 2;
        } else {
            source = 4;
        }
        return new UserManager.EnforcingUser(userId, source);
    }

    public Bundle getUserRestrictions(int userId) {
        return UserRestrictionsUtils.clone(getEffectiveUserRestrictions(userId));
    }

    public boolean hasBaseUserRestriction(String restrictionKey, int userId) {
        checkManageUsersPermission("hasBaseUserRestriction");
        boolean z = false;
        if (!UserRestrictionsUtils.isValidRestriction(restrictionKey)) {
            return false;
        }
        synchronized (this.mRestrictionsLock) {
            Bundle bundle = this.mBaseUserRestrictions.get(userId);
            if (bundle != null && bundle.getBoolean(restrictionKey, false)) {
                z = true;
            }
        }
        return z;
    }

    public void setUserRestriction(String key, boolean value, int userId) {
        checkManageUsersPermission("setUserRestriction");
        if (UserRestrictionsUtils.isValidRestriction(key)) {
            synchronized (this.mRestrictionsLock) {
                Bundle newRestrictions = UserRestrictionsUtils.clone(this.mBaseUserRestrictions.get(userId));
                newRestrictions.putBoolean(key, value);
                updateUserRestrictionsInternalLR(newRestrictions, userId);
            }
        }
    }

    @GuardedBy({"mRestrictionsLock"})
    private void updateUserRestrictionsInternalLR(Bundle newBaseRestrictions, int userId) {
        Bundle prevAppliedRestrictions = UserRestrictionsUtils.nonNull(this.mAppliedUserRestrictions.get(userId));
        if (newBaseRestrictions != null) {
            boolean z = true;
            Preconditions.checkState(this.mBaseUserRestrictions.get(userId) != newBaseRestrictions);
            if (this.mCachedEffectiveUserRestrictions.get(userId) == newBaseRestrictions) {
                z = false;
            }
            Preconditions.checkState(z);
            if (updateRestrictionsIfNeededLR(userId, newBaseRestrictions, this.mBaseUserRestrictions)) {
                scheduleWriteUser(getUserDataNoChecks(userId));
            }
        }
        Bundle effective = computeEffectiveUserRestrictionsLR(userId);
        this.mCachedEffectiveUserRestrictions.put(userId, effective);
        if (this.mAppOpsService != null) {
            long identity = Binder.clearCallingIdentity();
            try {
                this.mAppOpsService.setUserRestrictions(effective, mUserRestriconToken, userId);
            } catch (RemoteException e) {
                Log.w(LOG_TAG, "Unable to notify AppOpsService of UserRestrictions");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            Binder.restoreCallingIdentity(identity);
        }
        propagateUserRestrictionsLR(userId, effective, prevAppliedRestrictions);
        this.mAppliedUserRestrictions.put(userId, new Bundle(effective));
    }

    private void propagateUserRestrictionsLR(final int userId, Bundle newRestrictions, Bundle prevRestrictions) {
        if (!UserRestrictionsUtils.areEqual(newRestrictions, prevRestrictions)) {
            final Bundle newRestrictionsFinal = new Bundle(newRestrictions);
            final Bundle prevRestrictionsFinal = new Bundle(prevRestrictions);
            this.mHandler.post(new Runnable() {
                /* class com.android.server.pm.UserManagerService.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    UserManagerInternal.UserRestrictionsListener[] listeners;
                    UserRestrictionsUtils.applyUserRestrictions(UserManagerService.this.mContext, userId, newRestrictionsFinal, prevRestrictionsFinal);
                    synchronized (UserManagerService.this.mUserRestrictionsListeners) {
                        listeners = new UserManagerInternal.UserRestrictionsListener[UserManagerService.this.mUserRestrictionsListeners.size()];
                        UserManagerService.this.mUserRestrictionsListeners.toArray(listeners);
                    }
                    for (UserManagerInternal.UserRestrictionsListener userRestrictionsListener : listeners) {
                        userRestrictionsListener.onUserRestrictionsChanged(userId, newRestrictionsFinal, prevRestrictionsFinal);
                    }
                    UserManagerService.this.mContext.sendBroadcastAsUser(new Intent("android.os.action.USER_RESTRICTIONS_CHANGED").setFlags(1073741824), UserHandle.of(userId));
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mRestrictionsLock"})
    public void applyUserRestrictionsLR(int userId) {
        updateUserRestrictionsInternalLR(null, userId);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mRestrictionsLock"})
    public void applyUserRestrictionsForAllUsersLR() {
        this.mCachedEffectiveUserRestrictions.clear();
        this.mHandler.post(new Runnable() {
            /* class com.android.server.pm.UserManagerService.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                try {
                    int[] runningUsers = ActivityManager.getService().getRunningUserIds();
                    synchronized (UserManagerService.this.mRestrictionsLock) {
                        for (int i : runningUsers) {
                            UserManagerService.this.applyUserRestrictionsLR(i);
                        }
                    }
                } catch (RemoteException e) {
                    Log.w(UserManagerService.LOG_TAG, "Unable to access ActivityManagerService");
                }
            }
        });
    }

    private boolean isUserLimitReached() {
        int count;
        synchronized (this.mUsersLock) {
            count = getAliveUsersExcludingGuestsCountLU();
        }
        return count >= UserManager.getMaxSupportedUsers();
    }

    public boolean canAddMoreManagedProfiles(int userId, boolean allowedToRemoveOne) {
        checkManageUsersPermission("check if more managed profiles can be added.");
        boolean z = false;
        if (ActivityManager.isLowRamDeviceStatic() || !this.mContext.getPackageManager().hasSystemFeature("android.software.managed_users")) {
            return false;
        }
        List<UserInfo> profiles = getProfiles(userId, false);
        Iterator<UserInfo> iterator = profiles.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isClonedProfile()) {
                iterator.remove();
            }
        }
        int managedProfilesCount = profiles.size() - 1;
        int profilesRemovedCount = (managedProfilesCount <= 0 || !allowedToRemoveOne) ? 0 : 1;
        if (managedProfilesCount - profilesRemovedCount >= getMaxManagedProfiles()) {
            return false;
        }
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userId);
            if (userInfo != null) {
                if (userInfo.canHaveProfile()) {
                    int usersCountAfterRemoving = getAliveUsersExcludingGuestsCountLU() - profilesRemovedCount;
                    if (usersCountAfterRemoving == 1 || usersCountAfterRemoving < UserManager.getMaxSupportedUsers()) {
                        z = true;
                    }
                    return z;
                }
            }
            return false;
        }
    }

    @GuardedBy({"mUsersLock"})
    private int getAliveUsersExcludingGuestsCountLU() {
        int aliveUserCount = 0;
        int totalUserCount = this.mUsers.size();
        for (int i = 0; i < totalUserCount; i++) {
            UserInfo user = this.mUsers.valueAt(i).info;
            if (!this.mRemovingUserIds.get(user.id) && !user.isGuest() && !user.isClonedProfile() && !user.isHwKidsUser()) {
                aliveUserCount++;
            }
        }
        return aliveUserCount;
    }

    private static final void checkManageUserAndAcrossUsersFullPermission(String message) {
        int uid = Binder.getCallingUid();
        if (uid != 1000 && uid != 0) {
            if (!hasPermissionGranted("android.permission.MANAGE_USERS", uid) || !hasPermissionGranted("android.permission.INTERACT_ACROSS_USERS_FULL", uid)) {
                throw new SecurityException("You need MANAGE_USERS and INTERACT_ACROSS_USERS_FULL permission to: " + message);
            }
        }
    }

    private static boolean hasPermissionGranted(String permission, int uid) {
        return ActivityManager.checkComponentPermission(permission, uid, -1, true) == 0;
    }

    private static final void checkManageUsersPermission(String message) {
        if (!hasManageUsersPermission()) {
            throw new SecurityException("You need MANAGE_USERS permission to: " + message);
        }
    }

    private static final void checkManageOrCreateUsersPermission(String message) {
        if (!hasManageOrCreateUsersPermission()) {
            throw new SecurityException("You either need MANAGE_USERS or CREATE_USERS permission to: " + message);
        }
    }

    private static final void checkManageOrCreateUsersPermission(int creationFlags) {
        if ((creationFlags & -813) == 0) {
            if (!hasManageOrCreateUsersPermission()) {
                throw new SecurityException("You either need MANAGE_USERS or CREATE_USERS permission to create an user with flags: " + creationFlags);
            }
        } else if (!hasManageUsersPermission()) {
            throw new SecurityException("You need MANAGE_USERS permission to create an user  with flags: " + creationFlags);
        }
    }

    private static final boolean hasManageUsersPermission() {
        int callingUid = Binder.getCallingUid();
        return UserHandle.isSameApp(callingUid, 1000) || callingUid == 0 || hasPermissionGranted("android.permission.MANAGE_USERS", callingUid);
    }

    private static final boolean hasManageUsersOrPermission(String alternativePermission) {
        int callingUid = Binder.getCallingUid();
        return UserHandle.isSameApp(callingUid, 1000) || callingUid == 0 || hasPermissionGranted("android.permission.MANAGE_USERS", callingUid) || hasPermissionGranted(alternativePermission, callingUid);
    }

    private static final boolean hasManageOrCreateUsersPermission() {
        return hasManageUsersOrPermission("android.permission.CREATE_USERS");
    }

    private static void checkSystemOrRoot(String message) {
        int uid = Binder.getCallingUid();
        if (!UserHandle.isSameApp(uid, 1000) && uid != 0) {
            throw new SecurityException("Only system may: " + message);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeBitmapLP(UserInfo info, Bitmap bitmap) {
        try {
            File dir = new File(this.mUsersDir, Integer.toString(info.id));
            File file = new File(dir, USER_PHOTO_FILENAME);
            File tmp = new File(dir, USER_PHOTO_FILENAME_TMP);
            if (!dir.exists()) {
                dir.mkdir();
                FileUtils.setPermissions(dir.getPath(), 505, -1, -1);
            }
            Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;
            FileOutputStream os = new FileOutputStream(tmp);
            if (bitmap.compress(compressFormat, 100, os) && tmp.renameTo(file) && SELinux.restorecon(file)) {
                info.iconPath = file.getAbsolutePath();
            }
            try {
                os.close();
            } catch (IOException e) {
            }
            tmp.delete();
        } catch (FileNotFoundException e2) {
            Slog.w(LOG_TAG, "Error setting photo for user ", e2);
        }
    }

    public int[] getUserIds() {
        int[] iArr;
        synchronized (this.mUsersLock) {
            iArr = this.mUserIds;
        }
        return iArr;
    }

    @GuardedBy({"mRestrictionsLock", "mPackagesLock"})
    private void readUserListLP() {
        XmlPullParser parser;
        int type;
        if (!this.mUserListFile.exists()) {
            fallbackToSingleUserLP();
            return;
        }
        FileInputStream fis = null;
        try {
            fis = new AtomicFile(this.mUserListFile).openRead();
            parser = Xml.newPullParser();
            parser.setInput(fis, StandardCharsets.UTF_8.name());
            if (type != 2) {
                Slog.e(LOG_TAG, "Unable to read user list");
                fallbackToSingleUserLP();
                IoUtils.closeQuietly(fis);
                return;
            }
            try {
                this.mNextSerialNumber = -1;
                if (parser.getName().equals("users")) {
                    String lastSerialNumber = parser.getAttributeValue(null, ATTR_NEXT_SERIAL_NO);
                    if (lastSerialNumber != null) {
                        this.mNextSerialNumber = Integer.parseInt(lastSerialNumber);
                    }
                    String versionNumber = parser.getAttributeValue(null, ATTR_USER_VERSION);
                    if (versionNumber != null) {
                        this.mUserVersion = Integer.parseInt(versionNumber);
                    }
                }
                Bundle oldDevicePolicyGlobalUserRestrictions = null;
                while (true) {
                    int type2 = parser.next();
                    if (type2 == 1) {
                        updateUserIds();
                        upgradeIfNecessaryLP(oldDevicePolicyGlobalUserRestrictions);
                        break;
                    } else if (type2 == 2) {
                        String name = parser.getName();
                        if (name.equals(TAG_USER)) {
                            UserData userData = readUserLP(Integer.parseInt(parser.getAttributeValue(null, ATTR_ID)));
                            if (userData != null) {
                                synchronized (this.mUsersLock) {
                                    this.mUsers.put(userData.info.id, userData);
                                    if (userData.info.isClonedProfile()) {
                                        Slog.i(LOG_TAG, "read user list, set mHasClonedProfile true.");
                                        this.mHasClonedProfile = true;
                                    }
                                    if (this.mNextSerialNumber < 0 || this.mNextSerialNumber <= userData.info.id) {
                                        this.mNextSerialNumber = userData.info.id + 1;
                                    }
                                }
                            } else if (this.mUsers.size() == 0) {
                                Slog.e(LOG_TAG, "Unable to read user 0 --try fallback");
                                fallbackToSingleUserLP();
                                IoUtils.closeQuietly(fis);
                                return;
                            }
                        } else if (name.equals(TAG_GUEST_RESTRICTIONS)) {
                            while (true) {
                                int type3 = parser.next();
                                if (type3 == 1 || type3 == 3) {
                                    break;
                                } else if (type3 == 2) {
                                    if (parser.getName().equals(TAG_RESTRICTIONS)) {
                                        synchronized (this.mGuestRestrictions) {
                                            UserRestrictionsUtils.readRestrictions(parser, this.mGuestRestrictions);
                                        }
                                    } else {
                                        continue;
                                    }
                                }
                            }
                        } else if (name.equals(TAG_DEVICE_OWNER_USER_ID) || name.equals(TAG_GLOBAL_RESTRICTION_OWNER_ID)) {
                            String ownerUserId = parser.getAttributeValue(null, ATTR_ID);
                            if (ownerUserId != null) {
                                this.mDeviceOwnerUserId = Integer.parseInt(ownerUserId);
                            }
                        } else if (name.equals(TAG_DEVICE_POLICY_RESTRICTIONS)) {
                            oldDevicePolicyGlobalUserRestrictions = UserRestrictionsUtils.readRestrictions(parser);
                        }
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                fallbackToSingleUserLP();
            }
            IoUtils.closeQuietly(fis);
            return;
        } catch (Exception e2) {
            Slog.e(LOG_TAG, "Unable to read user list, error: " + e2);
            fallbackToSingleUserLP();
        } catch (Throwable th) {
            IoUtils.closeQuietly(fis);
            throw th;
        }
        while (true) {
            type = parser.next();
            if (type == 2 || type == 1) {
                break;
            }
        }
    }

    @GuardedBy({"mRestrictionsLock", "mPackagesLock"})
    private void upgradeIfNecessaryLP(Bundle oldGlobalUserRestrictions) {
        int originalVersion = this.mUserVersion;
        int userVersion = this.mUserVersion;
        if (userVersion < 1) {
            UserData userData = getUserDataNoChecks(0);
            if ("Primary".equals(userData.info.name)) {
                userData.info.name = this.mContext.getResources().getString(17040715);
                scheduleWriteUser(userData);
            }
            userVersion = 1;
        }
        if (userVersion < 2) {
            UserData userData2 = getUserDataNoChecks(0);
            if ((userData2.info.flags & 16) == 0) {
                userData2.info.flags |= 16;
                scheduleWriteUser(userData2);
            }
            userVersion = 2;
        }
        if (userVersion < 4) {
            userVersion = 4;
        }
        if (userVersion < 5) {
            initDefaultGuestRestrictions();
            userVersion = 5;
        }
        if (userVersion < 6) {
            boolean splitSystemUser = UserManager.isSplitSystemUser();
            synchronized (this.mUsersLock) {
                for (int i = 0; i < this.mUsers.size(); i++) {
                    UserData userData3 = this.mUsers.valueAt(i);
                    if (!splitSystemUser && userData3.info.isRestricted() && userData3.info.restrictedProfileParentId == -10000) {
                        userData3.info.restrictedProfileParentId = 0;
                        scheduleWriteUser(userData3);
                    }
                }
            }
            userVersion = 6;
        }
        if (userVersion < 7) {
            synchronized (this.mRestrictionsLock) {
                if (!UserRestrictionsUtils.isEmpty(oldGlobalUserRestrictions) && this.mDeviceOwnerUserId != -10000) {
                    this.mDevicePolicyGlobalUserRestrictions.put(this.mDeviceOwnerUserId, oldGlobalUserRestrictions);
                }
                UserRestrictionsUtils.moveRestriction("ensure_verify_apps", this.mDevicePolicyLocalUserRestrictions, this.mDevicePolicyGlobalUserRestrictions);
            }
            userVersion = 7;
        }
        if (userVersion < 7) {
            Slog.w(LOG_TAG, "User version " + this.mUserVersion + " didn't upgrade as expected to 7");
            return;
        }
        this.mUserVersion = userVersion;
        if (originalVersion < this.mUserVersion) {
            writeUserListLP();
        }
    }

    @GuardedBy({"mPackagesLock", "mRestrictionsLock"})
    private void fallbackToSingleUserLP() {
        int flags = 16;
        if (!UserManager.isSplitSystemUser()) {
            flags = 16 | 3;
        }
        UserData userData = putUserInfo(new UserInfo(0, (String) null, (String) null, flags));
        this.mNextSerialNumber = 10;
        this.mUserVersion = 7;
        Bundle restrictions = new Bundle();
        try {
            String[] defaultFirstUserRestrictions = this.mContext.getResources().getStringArray(17236003);
            for (String userRestriction : defaultFirstUserRestrictions) {
                if (UserRestrictionsUtils.isValidRestriction(userRestriction)) {
                    restrictions.putBoolean(userRestriction, true);
                }
            }
        } catch (Resources.NotFoundException e) {
            Log.e(LOG_TAG, "Couldn't find resource: config_defaultFirstUserRestrictions", e);
        }
        if (!restrictions.isEmpty()) {
            synchronized (this.mRestrictionsLock) {
                this.mBaseUserRestrictions.append(0, restrictions);
            }
        }
        updateUserIds();
        initDefaultGuestRestrictions();
        writeUserLP(userData);
        writeUserListLP();
    }

    private String getOwnerName() {
        return this.mContext.getResources().getString(17040715);
    }

    private void scheduleWriteUser(UserData UserData2) {
        if (UserData2 != null && !this.mHandler.hasMessages(1, UserData2)) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, UserData2), 2000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeUserLP(UserData userData) {
        FileOutputStream fos = null;
        File file = this.mUsersDir;
        AtomicFile userFile = new AtomicFile(new File(file, userData.info.id + XML_SUFFIX));
        try {
            fos = userFile.startWrite();
            writeUserLP(userData, new BufferedOutputStream(fos));
            userFile.finishWrite(fos);
        } catch (Exception ioe) {
            Slog.e(LOG_TAG, "Error writing user info " + userData.info.id, ioe);
            userFile.failWrite(fos);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void writeUserLP(UserData userData, OutputStream os) throws IOException, XmlPullParserException {
        XmlSerializer serializer = new FastXmlSerializer();
        serializer.setOutput(os, StandardCharsets.UTF_8.name());
        serializer.startDocument(null, true);
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        UserInfo userInfo = userData.info;
        serializer.startTag(null, TAG_USER);
        serializer.attribute(null, ATTR_ID, Integer.toString(userInfo.id));
        serializer.attribute(null, ATTR_SERIAL_NO, Integer.toString(userInfo.serialNumber));
        serializer.attribute(null, ATTR_FLAGS, Integer.toString(userInfo.flags));
        serializer.attribute(null, ATTR_CREATION_TIME, Long.toString(userInfo.creationTime));
        serializer.attribute(null, ATTR_LAST_LOGGED_IN_TIME, Long.toString(userInfo.lastLoggedInTime));
        if (userInfo.lastLoggedInFingerprint != null) {
            serializer.attribute(null, ATTR_LAST_LOGGED_IN_FINGERPRINT, userInfo.lastLoggedInFingerprint);
        }
        if (userInfo.lastLoggedInFingerprintEx != null) {
            serializer.attribute(null, ATTR_LAST_LOGGED_IN_FINGERPRINTEX, userInfo.lastLoggedInFingerprintEx);
        }
        if (userInfo.iconPath != null) {
            serializer.attribute(null, ATTR_ICON_PATH, userInfo.iconPath);
        }
        if (userInfo.partial) {
            serializer.attribute(null, ATTR_PARTIAL, "true");
        }
        if (userInfo.guestToRemove) {
            serializer.attribute(null, ATTR_GUEST_TO_REMOVE, "true");
        }
        if (userInfo.profileGroupId != -10000) {
            serializer.attribute(null, ATTR_PROFILE_GROUP_ID, Integer.toString(userInfo.profileGroupId));
        }
        serializer.attribute(null, ATTR_PROFILE_BADGE, Integer.toString(userInfo.profileBadge));
        if (userInfo.restrictedProfileParentId != -10000) {
            serializer.attribute(null, ATTR_RESTRICTED_PROFILE_PARENT_ID, Integer.toString(userInfo.restrictedProfileParentId));
        }
        if (userData.persistSeedData) {
            if (userData.seedAccountName != null) {
                serializer.attribute(null, ATTR_SEED_ACCOUNT_NAME, userData.seedAccountName);
            }
            if (userData.seedAccountType != null) {
                serializer.attribute(null, ATTR_SEED_ACCOUNT_TYPE, userData.seedAccountType);
            }
        }
        if (userInfo.name != null) {
            serializer.startTag(null, "name");
            serializer.text(userInfo.name);
            serializer.endTag(null, "name");
        }
        synchronized (this.mRestrictionsLock) {
            UserRestrictionsUtils.writeRestrictions(serializer, this.mBaseUserRestrictions.get(userInfo.id), TAG_RESTRICTIONS);
            UserRestrictionsUtils.writeRestrictions(serializer, this.mDevicePolicyLocalUserRestrictions.get(userInfo.id), TAG_DEVICE_POLICY_RESTRICTIONS);
            UserRestrictionsUtils.writeRestrictions(serializer, this.mDevicePolicyGlobalUserRestrictions.get(userInfo.id), TAG_DEVICE_POLICY_GLOBAL_RESTRICTIONS);
        }
        if (userData.account != null) {
            serializer.startTag(null, TAG_ACCOUNT);
            serializer.text(userData.account);
            serializer.endTag(null, TAG_ACCOUNT);
        }
        if (userData.persistSeedData && userData.seedAccountOptions != null) {
            serializer.startTag(null, TAG_SEED_ACCOUNT_OPTIONS);
            userData.seedAccountOptions.saveToXml(serializer);
            serializer.endTag(null, TAG_SEED_ACCOUNT_OPTIONS);
        }
        if (userData.getLastRequestQuietModeEnabledMillis() != 0) {
            serializer.startTag(null, TAG_LAST_REQUEST_QUIET_MODE_ENABLED_CALL);
            serializer.text(String.valueOf(userData.getLastRequestQuietModeEnabledMillis()));
            serializer.endTag(null, TAG_LAST_REQUEST_QUIET_MODE_ENABLED_CALL);
        }
        serializer.endTag(null, TAG_USER);
        serializer.endDocument();
    }

    @GuardedBy({"mRestrictionsLock", "mPackagesLock"})
    private void writeUserListLP() {
        int[] userIdsToWrite;
        int i;
        AtomicFile userListFile = new AtomicFile(this.mUserListFile);
        try {
            FileOutputStream fos = userListFile.startWrite();
            OutputStream bos = new BufferedOutputStream(fos);
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(bos, StandardCharsets.UTF_8.name());
            serializer.startDocument(null, true);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, "users");
            serializer.attribute(null, ATTR_NEXT_SERIAL_NO, Integer.toString(this.mNextSerialNumber));
            serializer.attribute(null, ATTR_USER_VERSION, Integer.toString(this.mUserVersion));
            serializer.startTag(null, TAG_GUEST_RESTRICTIONS);
            synchronized (this.mGuestRestrictions) {
                UserRestrictionsUtils.writeRestrictions(serializer, this.mGuestRestrictions, TAG_RESTRICTIONS);
            }
            serializer.endTag(null, TAG_GUEST_RESTRICTIONS);
            serializer.startTag(null, TAG_DEVICE_OWNER_USER_ID);
            serializer.attribute(null, ATTR_ID, Integer.toString(this.mDeviceOwnerUserId));
            serializer.endTag(null, TAG_DEVICE_OWNER_USER_ID);
            synchronized (this.mUsersLock) {
                userIdsToWrite = new int[this.mUsers.size()];
                for (int i2 = 0; i2 < userIdsToWrite.length; i2++) {
                    userIdsToWrite[i2] = this.mUsers.valueAt(i2).info.id;
                }
            }
            for (int id : userIdsToWrite) {
                serializer.startTag(null, TAG_USER);
                serializer.attribute(null, ATTR_ID, Integer.toString(id));
                serializer.endTag(null, TAG_USER);
            }
            serializer.endTag(null, "users");
            serializer.endDocument();
            userListFile.finishWrite(fos);
        } catch (Exception e) {
            userListFile.failWrite(null);
            Slog.e(LOG_TAG, "Error writing user list");
        }
    }

    private UserData readUserLP(int id) {
        FileInputStream fis = null;
        try {
            File file = this.mUsersDir;
            fis = new AtomicFile(new File(file, Integer.toString(id) + XML_SUFFIX)).openRead();
            UserData readUserLP = readUserLP(id, fis);
            IoUtils.closeQuietly(fis);
            return readUserLP;
        } catch (IOException e) {
            Slog.e(LOG_TAG, "Error reading user list");
        } catch (XmlPullParserException e2) {
            Slog.e(LOG_TAG, "Error reading user list");
        } catch (Throwable th) {
            IoUtils.closeQuietly(fis);
            throw th;
        }
        IoUtils.closeQuietly(fis);
        return null;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public UserData readUserLP(int id, InputStream is) throws IOException, XmlPullParserException {
        int type;
        Bundle globalRestrictions;
        Bundle localRestrictions;
        Bundle baseRestrictions;
        long lastRequestQuietModeEnabledTimestamp;
        PersistableBundle seedAccountOptions;
        boolean persistSeedData;
        String seedAccountType;
        String seedAccountName;
        String account;
        int restrictedProfileParentId;
        boolean guestToRemove;
        boolean partial;
        long lastLoggedInTime;
        long creationTime;
        int serialNumber;
        int type2;
        String name;
        int profileGroupId;
        int profileBadge;
        Throwable th;
        int type3;
        String iconPath = null;
        long lastRequestQuietModeEnabledTimestamp2 = 0;
        String lastLoggedInFingerprint = null;
        String lastLoggedInFingerprintEx = null;
        boolean partial2 = false;
        boolean guestToRemove2 = false;
        boolean persistSeedData2 = false;
        PersistableBundle seedAccountOptions2 = null;
        Bundle baseRestrictions2 = null;
        Bundle localRestrictions2 = null;
        Bundle globalRestrictions2 = null;
        XmlPullParser parser = Xml.newPullParser();
        String name2 = null;
        parser.setInput(is, StandardCharsets.UTF_8.name());
        while (true) {
            int type4 = parser.next();
            if (type4 == 2) {
                type = type4;
                break;
            }
            type = type4;
            if (type == 1) {
                break;
            }
        }
        if (type != 2) {
            Slog.e(LOG_TAG, "Unable to read user " + id);
            return null;
        }
        if (type != 2 || !parser.getName().equals(TAG_USER)) {
            lastRequestQuietModeEnabledTimestamp = 0;
            profileBadge = 0;
            restrictedProfileParentId = -10000;
            partial = false;
            persistSeedData = false;
            seedAccountName = null;
            seedAccountType = null;
            seedAccountOptions = null;
            baseRestrictions = null;
            localRestrictions = null;
            globalRestrictions = null;
            name = null;
            type2 = 0;
            account = null;
            lastLoggedInTime = 0;
            profileGroupId = -10000;
            guestToRemove = false;
            creationTime = 0;
            serialNumber = id;
        } else if (readIntAttribute(parser, ATTR_ID, -1) != id) {
            Slog.e(LOG_TAG, "User id does not match the file name");
            return null;
        } else {
            int serialNumber2 = readIntAttribute(parser, ATTR_SERIAL_NO, id);
            int flags = readIntAttribute(parser, ATTR_FLAGS, 0);
            iconPath = parser.getAttributeValue(null, ATTR_ICON_PATH);
            long creationTime2 = readLongAttribute(parser, ATTR_CREATION_TIME, 0);
            long lastLoggedInTime2 = readLongAttribute(parser, ATTR_LAST_LOGGED_IN_TIME, 0);
            lastLoggedInFingerprint = parser.getAttributeValue(null, ATTR_LAST_LOGGED_IN_FINGERPRINT);
            lastLoggedInFingerprintEx = parser.getAttributeValue(null, ATTR_LAST_LOGGED_IN_FINGERPRINTEX);
            int profileGroupId2 = readIntAttribute(parser, ATTR_PROFILE_GROUP_ID, -10000);
            int profileBadge2 = readIntAttribute(parser, ATTR_PROFILE_BADGE, 0);
            int restrictedProfileParentId2 = readIntAttribute(parser, ATTR_RESTRICTED_PROFILE_PARENT_ID, -10000);
            if ("true".equals(parser.getAttributeValue(null, ATTR_PARTIAL))) {
                partial2 = true;
            }
            String tag = parser.getAttributeValue(null, ATTR_GUEST_TO_REMOVE);
            if ("true".equals(tag)) {
                guestToRemove2 = true;
            }
            String seedAccountName2 = parser.getAttributeValue(null, ATTR_SEED_ACCOUNT_NAME);
            String seedAccountType2 = parser.getAttributeValue(null, ATTR_SEED_ACCOUNT_TYPE);
            if (!(seedAccountName2 == null && seedAccountType2 == null)) {
                persistSeedData2 = true;
            }
            int outerDepth = parser.getDepth();
            String account2 = null;
            while (true) {
                int type5 = parser.next();
                if (type5 == 1) {
                    type3 = type5;
                    break;
                }
                type3 = type5;
                if (type3 == 3 && parser.getDepth() <= outerDepth) {
                    break;
                } else if (type3 == 3 || type3 == 4) {
                    tag = tag;
                    outerDepth = outerDepth;
                    account2 = account2;
                } else {
                    String tag2 = parser.getName();
                    if ("name".equals(tag2)) {
                        if (parser.next() == 4) {
                            name2 = parser.getText();
                            account2 = account2;
                        } else {
                            account2 = account2;
                        }
                    } else if (TAG_RESTRICTIONS.equals(tag2)) {
                        baseRestrictions2 = UserRestrictionsUtils.readRestrictions(parser);
                        account2 = account2;
                    } else if (TAG_DEVICE_POLICY_RESTRICTIONS.equals(tag2)) {
                        localRestrictions2 = UserRestrictionsUtils.readRestrictions(parser);
                        account2 = account2;
                    } else if (TAG_DEVICE_POLICY_GLOBAL_RESTRICTIONS.equals(tag2)) {
                        globalRestrictions2 = UserRestrictionsUtils.readRestrictions(parser);
                        account2 = account2;
                    } else if (TAG_ACCOUNT.equals(tag2)) {
                        if (parser.next() == 4) {
                            account2 = parser.getText();
                        } else {
                            account2 = account2;
                        }
                    } else if (TAG_SEED_ACCOUNT_OPTIONS.equals(tag2)) {
                        seedAccountOptions2 = PersistableBundle.restoreFromXml(parser);
                        persistSeedData2 = true;
                        account2 = account2;
                    } else if (!TAG_LAST_REQUEST_QUIET_MODE_ENABLED_CALL.equals(tag2)) {
                        account2 = account2;
                    } else if (parser.next() == 4) {
                        lastRequestQuietModeEnabledTimestamp2 = Long.parseLong(parser.getText());
                        account2 = account2;
                    } else {
                        account2 = account2;
                    }
                    tag = tag;
                    outerDepth = outerDepth;
                }
            }
            lastRequestQuietModeEnabledTimestamp = lastRequestQuietModeEnabledTimestamp2;
            profileBadge = profileBadge2;
            restrictedProfileParentId = restrictedProfileParentId2;
            partial = partial2;
            persistSeedData = persistSeedData2;
            seedAccountName = seedAccountName2;
            seedAccountType = seedAccountType2;
            seedAccountOptions = seedAccountOptions2;
            baseRestrictions = baseRestrictions2;
            localRestrictions = localRestrictions2;
            globalRestrictions = globalRestrictions2;
            name = name2;
            account = account2;
            lastLoggedInTime = lastLoggedInTime2;
            profileGroupId = profileGroupId2;
            guestToRemove = guestToRemove2;
            type2 = flags;
            creationTime = creationTime2;
            serialNumber = serialNumber2;
        }
        UserInfo userInfo = new UserInfo(id, name, iconPath, type2);
        userInfo.serialNumber = serialNumber;
        userInfo.creationTime = creationTime;
        userInfo.lastLoggedInTime = lastLoggedInTime;
        userInfo.lastLoggedInFingerprint = lastLoggedInFingerprint;
        userInfo.lastLoggedInFingerprintEx = lastLoggedInFingerprintEx;
        userInfo.partial = partial;
        userInfo.guestToRemove = guestToRemove;
        userInfo.profileGroupId = profileGroupId;
        userInfo.profileBadge = profileBadge;
        userInfo.restrictedProfileParentId = restrictedProfileParentId;
        UserData userData = new UserData();
        userData.info = userInfo;
        userData.account = account;
        userData.seedAccountName = seedAccountName;
        userData.seedAccountType = seedAccountType;
        userData.persistSeedData = persistSeedData;
        userData.seedAccountOptions = seedAccountOptions;
        userData.setLastRequestQuietModeEnabledMillis(lastRequestQuietModeEnabledTimestamp);
        synchronized (this.mRestrictionsLock) {
            if (baseRestrictions != null) {
                try {
                    this.mBaseUserRestrictions.put(id, baseRestrictions);
                } catch (Throwable th2) {
                    th = th2;
                }
            }
            if (localRestrictions != null) {
                try {
                    this.mDevicePolicyLocalUserRestrictions.put(id, localRestrictions);
                } catch (Throwable th3) {
                    th = th3;
                }
            }
            if (globalRestrictions != null) {
                try {
                    this.mDevicePolicyGlobalUserRestrictions.put(id, globalRestrictions);
                } catch (Throwable th4) {
                    th = th4;
                    throw th;
                }
            }
            return userData;
        }
    }

    private int readIntAttribute(XmlPullParser parser, String attr, int defaultValue) {
        String valueString = parser.getAttributeValue(null, attr);
        if (valueString == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(valueString);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private long readLongAttribute(XmlPullParser parser, String attr, long defaultValue) {
        String valueString = parser.getAttributeValue(null, attr);
        if (valueString == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(valueString);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static void cleanAppRestrictionsForPackageLAr(String pkg, int userId) {
        File resFile = new File(Environment.getUserSystemDirectory(userId), packageToRestrictionsFileName(pkg));
        if (resFile.exists()) {
            resFile.delete();
        }
    }

    public UserInfo createProfileForUser(String name, int flags, int userId, String[] disallowedPackages) {
        checkManageOrCreateUsersPermission(flags);
        return createUserInternal(name, flags, userId, disallowedPackages);
    }

    public UserInfo createProfileForUserEvenWhenDisallowed(String name, int flags, int userId, String[] disallowedPackages) {
        checkManageOrCreateUsersPermission(flags);
        return createUserInternalUnchecked(name, flags, userId, disallowedPackages);
    }

    public boolean removeUserEvenWhenDisallowed(int userHandle) {
        checkManageOrCreateUsersPermission("Only the system can remove users");
        return removeUserUnchecked(userHandle);
    }

    public UserInfo createUser(String name, int flags) {
        checkManageOrCreateUsersPermission(flags);
        return createUserInternal(name, flags, -10000);
    }

    private UserInfo createUserInternal(String name, int flags, int parentId) {
        return createUserInternal(name, flags, parentId, null);
    }

    private UserInfo createUserInternal(String name, int flags, int parentId, String[] disallowedPackages) {
        String restriction;
        if ((flags & 32) != 0) {
            restriction = "no_add_managed_profile";
        } else {
            restriction = "no_add_user";
        }
        if (!hasUserRestriction(restriction, UserHandle.getCallingUserId())) {
            return createUserInternalUnchecked(name, flags, parentId, disallowedPackages);
        }
        Log.w(LOG_TAG, "Cannot add user. " + restriction + " is enabled.");
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:179:0x025b, code lost:
        if (r6.info.isEphemeral() != false) goto L_0x026d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:272:0x03fa, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:278:0x0401, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:298:0x043e, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:315:0x0489, code lost:
        r0 = th;
     */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x01a1 A[SYNTHETIC, Splitter:B:118:0x01a1] */
    /* JADX WARNING: Removed duplicated region for block: B:149:0x0205 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:193:0x0294  */
    /* JADX WARNING: Removed duplicated region for block: B:194:0x0299  */
    /* JADX WARNING: Removed duplicated region for block: B:203:0x02c7 A[Catch:{ all -> 0x040f }] */
    private UserInfo createUserInternalUnchecked(String name, int flags, int parentId, String[] disallowedPackages) {
        long ident;
        Throwable th;
        Throwable th2;
        boolean isClonedProfile;
        boolean isDemo;
        int userId;
        int flags2;
        Throwable th3;
        UserInfo userInfo;
        long now;
        long ident2;
        UserData userData;
        String str;
        int flags3 = flags;
        if (((DeviceStorageMonitorInternal) LocalServices.getService(DeviceStorageMonitorInternal.class)).isMemoryLow()) {
            Log.w(LOG_TAG, "Cannot add user. Not enough space on disk.");
            return null;
        }
        boolean isGuest = (flags3 & 4) != 0;
        boolean isManagedProfile = (flags3 & 32) != 0;
        boolean isRestricted = (flags3 & 8) != 0;
        boolean isDemo2 = (flags3 & 512) != 0;
        boolean isRepairMode = (134217728 & flags3) != 0;
        long ident3 = Binder.clearCallingIdentity();
        boolean isKidsUser = (flags3 & 2048) != 0;
        boolean isClonedProfile2 = (67108864 & flags3) != 0;
        Flog.i(900, "Create user internal, flags= " + Integer.toHexString(flags) + " parentId= " + parentId + " isGuest= " + isGuest + " isManagedProfile= " + isManagedProfile);
        try {
            synchronized (this.mPackagesLock) {
                UserData parent = null;
                try {
                    try {
                        if (Settings.Secure.getInt(this.mContext.getContentResolver(), SUW_FRP_STATE, 0) == 1) {
                            try {
                                if (Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 1) {
                                    Log.w(LOG_TAG, "can not create new user before FRP unlock");
                                    Binder.restoreCallingIdentity(ident3);
                                    return null;
                                }
                            } catch (Throwable th4) {
                                th2 = th4;
                                ident = ident3;
                                while (true) {
                                    try {
                                        break;
                                    } catch (Throwable th5) {
                                        th2 = th5;
                                    }
                                }
                                throw th2;
                            }
                        }
                        if (!isRepairMode || Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 1) {
                            if (parentId != -10000) {
                                synchronized (this.mUsersLock) {
                                    parent = getUserDataLU(parentId);
                                }
                                if (parent == null) {
                                    Binder.restoreCallingIdentity(ident3);
                                    return null;
                                }
                            }
                            if (!isManagedProfile || canAddMoreManagedProfiles(parentId, false)) {
                                if (!isRepairMode && !isGuest && !isManagedProfile && !isDemo2) {
                                    try {
                                        if (isUserLimitReached()) {
                                            isClonedProfile = isClonedProfile2;
                                            if (!isClonedProfile && !isKidsUser) {
                                                try {
                                                    Log.e(LOG_TAG, "Cannot add user. Maximum user limit is reached.");
                                                    Binder.restoreCallingIdentity(ident3);
                                                    return null;
                                                } catch (Throwable th6) {
                                                    th2 = th6;
                                                    ident = ident3;
                                                    while (true) {
                                                        break;
                                                    }
                                                    throw th2;
                                                }
                                            }
                                            if (!isGuest && findCurrentGuestUser() != null) {
                                                Log.e(LOG_TAG, "Cannot add guest user. Guest user already exists.");
                                                Binder.restoreCallingIdentity(ident3);
                                                return null;
                                            } else if (isRestricted || UserManager.isSplitSystemUser() || parentId == 0) {
                                                if (isRestricted) {
                                                    try {
                                                        if (UserManager.isSplitSystemUser()) {
                                                            if (parent == null) {
                                                                Log.w(LOG_TAG, "Cannot add restricted profile - parent user must be specified");
                                                                Binder.restoreCallingIdentity(ident3);
                                                                return null;
                                                            } else if (!parent.info.canHaveProfile()) {
                                                                StringBuilder sb = new StringBuilder();
                                                                try {
                                                                    sb.append("Cannot add restricted profile - profiles cannot be created for the specified parent user id ");
                                                                    sb.append(parentId);
                                                                    Log.w(LOG_TAG, sb.toString());
                                                                    Binder.restoreCallingIdentity(ident3);
                                                                    return null;
                                                                } catch (Throwable th7) {
                                                                    th2 = th7;
                                                                    ident = ident3;
                                                                    while (true) {
                                                                        break;
                                                                    }
                                                                    throw th2;
                                                                }
                                                            } else {
                                                                isDemo = isDemo2;
                                                                if (UserManager.isSplitSystemUser() && !isGuest && !isManagedProfile && getPrimaryUser() == null) {
                                                                    flags3 |= 1;
                                                                    synchronized (this.mUsersLock) {
                                                                        if (!this.mIsDeviceManaged) {
                                                                            flags3 |= 2;
                                                                        }
                                                                    }
                                                                }
                                                                if (isRepairMode) {
                                                                    userId = REPAIR_MODE_USER_ID;
                                                                } else {
                                                                    try {
                                                                        userId = getNextAvailableId(isClonedProfile);
                                                                    } catch (Throwable th8) {
                                                                        th2 = th8;
                                                                        ident = ident3;
                                                                        while (true) {
                                                                            break;
                                                                        }
                                                                        throw th2;
                                                                    }
                                                                }
                                                                Environment.getUserSystemDirectory(userId).mkdirs();
                                                                boolean ephemeralGuests = Resources.getSystem().getBoolean(17891463);
                                                                try {
                                                                    synchronized (this.mUsersLock) {
                                                                        if (!isGuest || !ephemeralGuests) {
                                                                            try {
                                                                                if (!this.mForceEphemeralUsers) {
                                                                                    if (parent != null) {
                                                                                        try {
                                                                                        } catch (Throwable th9) {
                                                                                            th3 = th9;
                                                                                            flags2 = flags3;
                                                                                            ident = ident3;
                                                                                            while (true) {
                                                                                                try {
                                                                                                    break;
                                                                                                } catch (Throwable th10) {
                                                                                                    th3 = th10;
                                                                                                }
                                                                                            }
                                                                                            throw th3;
                                                                                        }
                                                                                    }
                                                                                    try {
                                                                                        userInfo = new UserInfo(userId, name, (String) null, flags3);
                                                                                        int i = this.mNextSerialNumber;
                                                                                        flags2 = flags3;
                                                                                        this.mNextSerialNumber = i + 1;
                                                                                        userInfo.serialNumber = i;
                                                                                        now = System.currentTimeMillis();
                                                                                        if (now <= EPOCH_PLUS_30_YEARS) {
                                                                                            ident = ident3;
                                                                                            ident2 = now;
                                                                                        } else {
                                                                                            ident = ident3;
                                                                                            ident2 = 0;
                                                                                        }
                                                                                    } catch (Throwable th11) {
                                                                                        th3 = th11;
                                                                                        flags2 = flags3;
                                                                                        ident = ident3;
                                                                                        while (true) {
                                                                                            break;
                                                                                        }
                                                                                        throw th3;
                                                                                    }
                                                                                    try {
                                                                                        userInfo.creationTime = ident2;
                                                                                        userInfo.partial = true;
                                                                                        userInfo.lastLoggedInFingerprint = Build.FINGERPRINT;
                                                                                        userInfo.lastLoggedInFingerprintEx = Build.FINGERPRINTEX;
                                                                                        if (isManagedProfile && parentId != -10000) {
                                                                                            userInfo.profileBadge = getFreeProfileBadgeLU(parentId);
                                                                                        }
                                                                                        userData = new UserData();
                                                                                        userData.info = userInfo;
                                                                                        this.mUsers.put(userId, userData);
                                                                                        if (isClonedProfile) {
                                                                                            Slog.i(LOG_TAG, "create cloned profile user, set mHasClonedProfile true.");
                                                                                            this.mHasClonedProfile = true;
                                                                                        }
                                                                                    } catch (Throwable th12) {
                                                                                        th3 = th12;
                                                                                        while (true) {
                                                                                            break;
                                                                                        }
                                                                                        throw th3;
                                                                                    }
                                                                                }
                                                                            } catch (Throwable th13) {
                                                                                th3 = th13;
                                                                                ident = ident3;
                                                                                flags2 = flags3;
                                                                                while (true) {
                                                                                    break;
                                                                                }
                                                                                throw th3;
                                                                            }
                                                                        }
                                                                        flags3 |= 256;
                                                                        try {
                                                                            userInfo = new UserInfo(userId, name, (String) null, flags3);
                                                                            int i2 = this.mNextSerialNumber;
                                                                            flags2 = flags3;
                                                                        } catch (Throwable th14) {
                                                                            th3 = th14;
                                                                            flags2 = flags3;
                                                                            ident = ident3;
                                                                            while (true) {
                                                                                break;
                                                                            }
                                                                            throw th3;
                                                                        }
                                                                        try {
                                                                            this.mNextSerialNumber = i2 + 1;
                                                                            userInfo.serialNumber = i2;
                                                                            now = System.currentTimeMillis();
                                                                            if (now <= EPOCH_PLUS_30_YEARS) {
                                                                            }
                                                                            userInfo.creationTime = ident2;
                                                                            userInfo.partial = true;
                                                                            userInfo.lastLoggedInFingerprint = Build.FINGERPRINT;
                                                                            userInfo.lastLoggedInFingerprintEx = Build.FINGERPRINTEX;
                                                                            userInfo.profileBadge = getFreeProfileBadgeLU(parentId);
                                                                            userData = new UserData();
                                                                            userData.info = userInfo;
                                                                            this.mUsers.put(userId, userData);
                                                                            if (isClonedProfile) {
                                                                            }
                                                                        } catch (Throwable th15) {
                                                                            th3 = th15;
                                                                            ident = ident3;
                                                                            while (true) {
                                                                                break;
                                                                            }
                                                                            throw th3;
                                                                        }
                                                                    }
                                                                    if (isKidsUser) {
                                                                        try {
                                                                            userInfo.partial = false;
                                                                        } catch (Throwable th16) {
                                                                            th2 = th16;
                                                                            flags3 = flags2;
                                                                            while (true) {
                                                                                break;
                                                                            }
                                                                            throw th2;
                                                                        }
                                                                    }
                                                                    writeUserLP(userData);
                                                                    writeUserListLP();
                                                                    if (parent != null) {
                                                                        if (!isManagedProfile) {
                                                                            if (!isClonedProfile) {
                                                                                if (isRestricted) {
                                                                                    if (parent.info.restrictedProfileParentId == -10000) {
                                                                                        parent.info.restrictedProfileParentId = parent.info.id;
                                                                                        writeUserLP(parent);
                                                                                    }
                                                                                    userInfo.restrictedProfileParentId = parent.info.restrictedProfileParentId;
                                                                                }
                                                                            }
                                                                        }
                                                                        if (parent.info.profileGroupId == -10000) {
                                                                            parent.info.profileGroupId = parent.info.id;
                                                                            writeUserLP(parent);
                                                                        }
                                                                        userInfo.profileGroupId = parent.info.profileGroupId;
                                                                    }
                                                                } catch (Throwable th17) {
                                                                    th2 = th17;
                                                                    ident = ident3;
                                                                    while (true) {
                                                                        break;
                                                                    }
                                                                    throw th2;
                                                                }
                                                            }
                                                        }
                                                    } catch (Throwable th18) {
                                                        th2 = th18;
                                                        ident = ident3;
                                                        while (true) {
                                                            break;
                                                        }
                                                        throw th2;
                                                    }
                                                }
                                                isDemo = isDemo2;
                                                flags3 |= 1;
                                                synchronized (this.mUsersLock) {
                                                }
                                            } else {
                                                Log.w(LOG_TAG, "Cannot add restricted profile - parent user must be owner");
                                                Binder.restoreCallingIdentity(ident3);
                                                return null;
                                            }
                                        }
                                    } catch (Throwable th19) {
                                        th2 = th19;
                                        ident = ident3;
                                        while (true) {
                                            break;
                                        }
                                        throw th2;
                                    }
                                }
                                isClonedProfile = isClonedProfile2;
                                if (!isGuest) {
                                }
                                if (isRestricted) {
                                }
                                if (isRestricted) {
                                }
                                isDemo = isDemo2;
                                try {
                                    flags3 |= 1;
                                } catch (Throwable th20) {
                                    th2 = th20;
                                    ident = ident3;
                                    while (true) {
                                        break;
                                    }
                                    throw th2;
                                }
                                try {
                                    synchronized (this.mUsersLock) {
                                    }
                                } catch (Throwable th21) {
                                    th2 = th21;
                                    ident = ident3;
                                    while (true) {
                                        break;
                                    }
                                    throw th2;
                                }
                            } else {
                                Log.e(LOG_TAG, "Cannot add more managed profiles for user " + parentId);
                                Binder.restoreCallingIdentity(ident3);
                                return null;
                            }
                        } else {
                            Log.w(LOG_TAG, "can not create repair mode user during start-up guide process");
                            Binder.restoreCallingIdentity(ident3);
                            return null;
                        }
                    } catch (Throwable th22) {
                        th2 = th22;
                        ident = ident3;
                        while (true) {
                            break;
                        }
                        throw th2;
                    }
                } catch (Throwable th23) {
                    th2 = th23;
                    ident = ident3;
                    while (true) {
                        break;
                    }
                    throw th2;
                }
            }
            try {
                StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
                if (this.isSupportISec) {
                    storage.createUserKeyISec(userId, userInfo.serialNumber, userInfo.isEphemeral());
                } else {
                    storage.createUserKey(userId, userInfo.serialNumber, userInfo.isEphemeral());
                }
                this.mUserDataPreparer.prepareUserData(userId, userInfo.serialNumber, 3);
                if (isKidsUser) {
                    Binder.restoreCallingIdentity(ident);
                    return userInfo;
                }
                try {
                    this.mPm.createNewUser(userId, disallowedPackages);
                    userInfo.partial = false;
                    synchronized (this.mPackagesLock) {
                        writeUserLP(userData);
                    }
                    updateUserIds();
                    Bundle restrictions = new Bundle();
                    if (isGuest) {
                        synchronized (this.mGuestRestrictions) {
                            restrictions.putAll(this.mGuestRestrictions);
                        }
                    }
                    synchronized (this.mRestrictionsLock) {
                        this.mBaseUserRestrictions.append(userId, restrictions);
                    }
                    this.mPm.onNewUserCreated(userId);
                    HwThemeManager.applyDefaultHwTheme(false, this.mContext, userId);
                    Intent addedIntent = new Intent("android.intent.action.USER_ADDED");
                    addedIntent.putExtra("android.intent.extra.user_handle", userId);
                    this.mContext.sendBroadcastAsUser(addedIntent, UserHandle.ALL, "android.permission.MANAGE_USERS");
                    Context context = this.mContext;
                    if (isGuest) {
                        str = TRON_GUEST_CREATED;
                    } else {
                        str = isDemo ? TRON_DEMO_CREATED : TRON_USER_CREATED;
                    }
                    MetricsLogger.count(context, str, 1);
                    Settings.Secure.putStringForUser(this.mContext.getContentResolver(), "display_density_forced", Integer.toString(SystemProperties.getInt("persist.sys.realdpi", SystemProperties.getInt("persist.sys.dpi", SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0))))), userId);
                    if (isRepairMode) {
                        SystemProperties.set("persist.sys.RepairMode", "true");
                    }
                    Binder.restoreCallingIdentity(ident);
                    return userInfo;
                } catch (Throwable th24) {
                    th = th24;
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            } catch (Throwable th25) {
                th = th25;
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } catch (Throwable th26) {
            th = th26;
            ident = ident3;
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
        while (true) {
        }
        while (true) {
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public UserData putUserInfo(UserInfo userInfo) {
        UserData userData = new UserData();
        userData.info = userInfo;
        synchronized (this.mUsersLock) {
            this.mUsers.put(userInfo.id, userData);
        }
        return userData;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void removeUserInfo(int userId) {
        synchronized (this.mUsersLock) {
            this.mUsers.remove(userId);
        }
    }

    public UserInfo createRestrictedProfile(String name, int parentUserId) {
        checkManageOrCreateUsersPermission("setupRestrictedProfile");
        UserInfo user = createProfileForUser(name, 8, parentUserId, null);
        if (user == null) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            setUserRestriction("no_modify_accounts", true, user.id);
            Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "location_mode", 0, user.id);
            setUserRestriction("no_share_location", true, user.id);
            return user;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private UserInfo findCurrentGuestUser() {
        synchronized (this.mUsersLock) {
            int size = this.mUsers.size();
            for (int i = 0; i < size; i++) {
                UserInfo user = this.mUsers.valueAt(i).info;
                if (!(!user.isGuest() || user.guestToRemove || this.mRemovingUserIds.get(user.id))) {
                    return user;
                }
            }
            return null;
        }
    }

    public boolean markGuestForDeletion(int userHandle) {
        UserData userData;
        checkManageUsersPermission("Only the system can remove users");
        if (getUserRestrictions(UserHandle.getCallingUserId()).getBoolean("no_remove_user", false)) {
            Log.w(LOG_TAG, "Cannot remove user. DISALLOW_REMOVE_USER is enabled.");
            return false;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mPackagesLock) {
                synchronized (this.mUsersLock) {
                    userData = this.mUsers.get(userHandle);
                    if (!(userHandle == 0 || userData == null)) {
                        if (this.mRemovingUserIds.get(userHandle)) {
                        }
                    }
                    return false;
                }
                if (!userData.info.isGuest()) {
                    Binder.restoreCallingIdentity(ident);
                    return false;
                }
                userData.info.guestToRemove = true;
                userData.info.flags |= 64;
                writeUserLP(userData);
                Binder.restoreCallingIdentity(ident);
                return true;
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean removeUser(int userHandle) {
        boolean isManagedProfile;
        Slog.i(LOG_TAG, "removeUser u" + userHandle);
        checkManageOrCreateUsersPermission("Only the system can remove users");
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userHandle);
            isManagedProfile = userInfo != null && userInfo.isManagedProfile();
        }
        String restriction = isManagedProfile ? "no_remove_managed_profile" : "no_remove_user";
        if (getUserRestrictions(UserHandle.getCallingUserId()).getBoolean(restriction, false)) {
            Log.w(LOG_TAG, "Cannot remove user. " + restriction + " is enabled.");
            return false;
        }
        if (isClonedProfile(userHandle)) {
            this.mHasClonedProfile = false;
        }
        return removeUserUnchecked(userHandle);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean removeUserUnchecked(int userHandle) {
        UserData userData;
        long ident = Binder.clearCallingIdentity();
        try {
            boolean z = false;
            if (ActivityManager.getCurrentUser() == userHandle) {
                Log.w(LOG_TAG, "Current user cannot be removed.");
                return false;
            }
            synchronized (this.mPackagesLock) {
                synchronized (this.mUsersLock) {
                    userData = this.mUsers.get(userHandle);
                    if (userHandle == 0) {
                        Log.e(LOG_TAG, "System user cannot be removed.");
                        Binder.restoreCallingIdentity(ident);
                        return false;
                    } else if (userData == null) {
                        Log.e(LOG_TAG, String.format("Cannot remove user %d, invalid user id provided.", Integer.valueOf(userHandle)));
                        Binder.restoreCallingIdentity(ident);
                        return false;
                    } else if (this.mRemovingUserIds.get(userHandle)) {
                        Log.e(LOG_TAG, String.format("User %d is already scheduled for removal.", Integer.valueOf(userHandle)));
                        Flog.i(900, "Removing user stopped, userHandle " + userHandle + " user " + userData);
                        Binder.restoreCallingIdentity(ident);
                        return false;
                    } else {
                        addRemovingUserIdLocked(userHandle);
                    }
                }
                userData.info.partial = true;
                userData.info.flags |= 64;
                writeUserLP(userData);
            }
            try {
                this.mAppOpsService.removeUser(userHandle);
            } catch (RemoteException e) {
                Log.w(LOG_TAG, "Unable to notify AppOpsService of removing user.", e);
            }
            if (((UserInfo) userData.info).profileGroupId != -10000 && userData.info.isManagedProfile()) {
                sendProfileRemovedBroadcast(userData.info.profileGroupId, userData.info.id);
            }
            Flog.i(900, "Stopping user " + userHandle);
            try {
                int res = ActivityManager.getService().stopUser(userHandle, true, new IStopUserCallback.Stub() {
                    /* class com.android.server.pm.UserManagerService.AnonymousClass4 */

                    public void userStopped(int userId) {
                        UserManagerService.this.finishRemoveUser(userId);
                    }

                    public void userStopAborted(int userId) {
                    }
                });
                if (res == 0 && userHandle == REPAIR_MODE_USER_ID) {
                    SystemProperties.set("persist.sys.RepairMode", "false");
                }
                if (res == 0) {
                    z = true;
                }
                Binder.restoreCallingIdentity(ident);
                return z;
            } catch (RemoteException e2) {
                Log.w(LOG_TAG, "Failed to stop user during removal.", e2);
                Binder.restoreCallingIdentity(ident);
                return false;
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mUsersLock"})
    @VisibleForTesting
    public void addRemovingUserIdLocked(int userId) {
        this.mRemovingUserIds.put(userId, true);
        if (userId < 128 || userId >= 148) {
            this.mRecentlyRemovedIds.add(Integer.valueOf(userId));
            if (this.mRecentlyRemovedIds.size() > 100) {
                this.mRecentlyRemovedIds.removeFirst();
                return;
            }
            return;
        }
        this.mClonedProfileRecentlyRemovedIds.add(Integer.valueOf(userId));
        if (this.mClonedProfileRecentlyRemovedIds.size() >= 20) {
            this.mClonedProfileRecentlyRemovedIds.removeFirst();
        }
    }

    /* access modifiers changed from: package-private */
    public void finishRemoveUser(final int userHandle) {
        Flog.i(900, "finishRemoveUser " + userHandle);
        long ident = Binder.clearCallingIdentity();
        try {
            Intent addedIntent = new Intent("android.intent.action.USER_REMOVED");
            addedIntent.putExtra("android.intent.extra.user_handle", userHandle);
            this.mContext.sendOrderedBroadcastAsUser(addedIntent, UserHandle.ALL, "android.permission.MANAGE_USERS", new BroadcastReceiver() {
                /* class com.android.server.pm.UserManagerService.AnonymousClass5 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    Flog.i(900, "USER_REMOVED broadcast sent, cleaning up user data " + userHandle);
                    new Thread() {
                        /* class com.android.server.pm.UserManagerService.AnonymousClass5.AnonymousClass1 */

                        @Override // java.lang.Thread, java.lang.Runnable
                        public void run() {
                            ((ActivityTaskManagerInternal) LocalServices.getService(ActivityTaskManagerInternal.class)).onUserStopped(userHandle);
                            UserManagerService.this.removeUserState(userHandle);
                        }
                    }.start();
                }
            }, null, -1, null, null);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeUserState(int userHandle) {
        Slog.i(LOG_TAG, "remove user state:" + userHandle);
        try {
            if (this.isSupportISec) {
                ((StorageManager) this.mContext.getSystemService(StorageManager.class)).destroyUserKeyISec(userHandle);
            } else {
                ((StorageManager) this.mContext.getSystemService(StorageManager.class)).destroyUserKey(userHandle);
            }
        } catch (IllegalStateException e) {
            Slog.i(LOG_TAG, "Destroying key for user " + userHandle + " failed, continuing anyway", e);
        }
        try {
            IGateKeeperService gk = GateKeeper.getService();
            if (gk != null) {
                gk.clearSecureUserId(userHandle);
            }
        } catch (Exception e2) {
            Slog.w(LOG_TAG, "unable to clear GK secure user id");
        }
        this.mPm.cleanUpUser(this, userHandle);
        this.mUserDataPreparer.destroyUserData(userHandle, 3);
        synchronized (this.mUsersLock) {
            this.mUsers.remove(userHandle);
            this.mIsUserManaged.delete(userHandle);
        }
        synchronized (this.mUserStates) {
            this.mUserStates.delete(userHandle);
        }
        synchronized (this.mRestrictionsLock) {
            this.mBaseUserRestrictions.remove(userHandle);
            this.mAppliedUserRestrictions.remove(userHandle);
            this.mCachedEffectiveUserRestrictions.remove(userHandle);
            this.mDevicePolicyLocalUserRestrictions.remove(userHandle);
            if (this.mDevicePolicyGlobalUserRestrictions.get(userHandle) != null) {
                this.mDevicePolicyGlobalUserRestrictions.remove(userHandle);
                applyUserRestrictionsForAllUsersLR();
            }
        }
        synchronized (this.mPackagesLock) {
            writeUserListLP();
        }
        File file = this.mUsersDir;
        new AtomicFile(new File(file, userHandle + XML_SUFFIX)).delete();
        updateUserIds();
    }

    private void sendProfileRemovedBroadcast(int parentUserId, int removedUserId) {
        Flog.i(900, "sendProfileRemovedBroadcast parentUserId=  " + parentUserId + " removedUserId= " + removedUserId);
        Intent managedProfileIntent = new Intent("android.intent.action.MANAGED_PROFILE_REMOVED");
        managedProfileIntent.addFlags(1342177280);
        managedProfileIntent.putExtra("android.intent.extra.USER", new UserHandle(removedUserId));
        managedProfileIntent.putExtra("android.intent.extra.user_handle", removedUserId);
        this.mContext.sendBroadcastAsUser(managedProfileIntent, new UserHandle(parentUserId), null);
    }

    public Bundle getApplicationRestrictions(String packageName) {
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.USERMANAGER_GETAPPLICATIONRESTRICTIONS);
        return getApplicationRestrictionsForUser(packageName, UserHandle.getCallingUserId());
    }

    public Bundle getApplicationRestrictionsForUser(String packageName, int userId) {
        Bundle readApplicationRestrictionsLAr;
        if (UserHandle.getCallingUserId() != userId || !UserHandle.isSameApp(Binder.getCallingUid(), getUidForPackage(packageName))) {
            checkSystemOrRoot("get application restrictions for other user/app " + packageName);
        }
        synchronized (this.mAppRestrictionsLock) {
            readApplicationRestrictionsLAr = readApplicationRestrictionsLAr(packageName, userId);
        }
        return readApplicationRestrictionsLAr;
    }

    public void setApplicationRestrictions(String packageName, Bundle restrictions, int userId) {
        checkSystemOrRoot("set application restrictions");
        if (restrictions != null) {
            restrictions.setDefusable(true);
        }
        synchronized (this.mAppRestrictionsLock) {
            if (restrictions != null) {
                if (!restrictions.isEmpty()) {
                    writeApplicationRestrictionsLAr(packageName, restrictions, userId);
                }
            }
            cleanAppRestrictionsForPackageLAr(packageName, userId);
        }
        Intent changeIntent = new Intent("android.intent.action.APPLICATION_RESTRICTIONS_CHANGED");
        changeIntent.setPackage(packageName);
        changeIntent.addFlags(1073741824);
        this.mContext.sendBroadcastAsUser(changeIntent, UserHandle.of(userId));
    }

    private int getUidForPackage(String packageName) {
        long ident = Binder.clearCallingIdentity();
        try {
            return this.mContext.getPackageManager().getApplicationInfo(packageName, DumpState.DUMP_CHANGES).uid;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    @GuardedBy({"mAppRestrictionsLock"})
    private static Bundle readApplicationRestrictionsLAr(String packageName, int userId) {
        return readApplicationRestrictionsLAr(new AtomicFile(new File(Environment.getUserSystemDirectory(userId), packageToRestrictionsFileName(packageName))));
    }

    @GuardedBy({"mAppRestrictionsLock"})
    @VisibleForTesting
    static Bundle readApplicationRestrictionsLAr(AtomicFile restrictionsFile) {
        Bundle restrictions = new Bundle();
        ArrayList<String> values = new ArrayList<>();
        if (!restrictionsFile.getBaseFile().exists()) {
            return restrictions;
        }
        FileInputStream fis = restrictionsFile.openRead();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(fis, StandardCharsets.UTF_8.name());
        XmlUtils.nextElement(parser);
        if (parser.getEventType() != 2) {
            Slog.e(LOG_TAG, "Unable to read restrictions file " + restrictionsFile.getBaseFile());
            IoUtils.closeQuietly(fis);
            return restrictions;
        }
        while (parser.next() != 1) {
            try {
                readEntry(restrictions, values, parser);
            } catch (IOException | XmlPullParserException e) {
                Log.w(LOG_TAG, "Error parsing " + restrictionsFile.getBaseFile(), e);
            } catch (Throwable th) {
                IoUtils.closeQuietly(fis);
                throw th;
            }
        }
        IoUtils.closeQuietly(fis);
        return restrictions;
    }

    private static void readEntry(Bundle restrictions, ArrayList<String> values, XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() == 2 && parser.getName().equals(TAG_ENTRY)) {
            String key = parser.getAttributeValue(null, ATTR_KEY);
            String valType = parser.getAttributeValue(null, "type");
            String multiple = parser.getAttributeValue(null, ATTR_MULTIPLE);
            if (multiple != null) {
                values.clear();
                int count = Integer.parseInt(multiple);
                while (count > 0) {
                    int type = parser.next();
                    if (type == 1) {
                        break;
                    } else if (type == 2 && parser.getName().equals(TAG_VALUE)) {
                        values.add(parser.nextText().trim());
                        count--;
                    }
                }
                String[] valueStrings = new String[values.size()];
                values.toArray(valueStrings);
                restrictions.putStringArray(key, valueStrings);
            } else if (ATTR_TYPE_BUNDLE.equals(valType)) {
                restrictions.putBundle(key, readBundleEntry(parser, values));
            } else if (ATTR_TYPE_BUNDLE_ARRAY.equals(valType)) {
                int outerDepth = parser.getDepth();
                ArrayList<Bundle> bundleList = new ArrayList<>();
                while (XmlUtils.nextElementWithin(parser, outerDepth)) {
                    bundleList.add(readBundleEntry(parser, values));
                }
                restrictions.putParcelableArray(key, (Parcelable[]) bundleList.toArray(new Bundle[bundleList.size()]));
            } else {
                String value = parser.nextText().trim();
                if (ATTR_TYPE_BOOLEAN.equals(valType)) {
                    restrictions.putBoolean(key, Boolean.parseBoolean(value));
                } else if (ATTR_TYPE_INTEGER.equals(valType)) {
                    restrictions.putInt(key, Integer.parseInt(value));
                } else {
                    restrictions.putString(key, value);
                }
            }
        }
    }

    private static Bundle readBundleEntry(XmlPullParser parser, ArrayList<String> values) throws IOException, XmlPullParserException {
        Bundle childBundle = new Bundle();
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            readEntry(childBundle, values, parser);
        }
        return childBundle;
    }

    @GuardedBy({"mAppRestrictionsLock"})
    private static void writeApplicationRestrictionsLAr(String packageName, Bundle restrictions, int userId) {
        writeApplicationRestrictionsLAr(restrictions, new AtomicFile(new File(Environment.getUserSystemDirectory(userId), packageToRestrictionsFileName(packageName))));
    }

    @GuardedBy({"mAppRestrictionsLock"})
    @VisibleForTesting
    static void writeApplicationRestrictionsLAr(Bundle restrictions, AtomicFile restrictionsFile) {
        FileOutputStream fos = null;
        try {
            fos = restrictionsFile.startWrite();
            OutputStream bos = new BufferedOutputStream(fos);
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(bos, StandardCharsets.UTF_8.name());
            serializer.startDocument(null, true);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, TAG_RESTRICTIONS);
            writeBundle(restrictions, serializer);
            serializer.endTag(null, TAG_RESTRICTIONS);
            serializer.endDocument();
            restrictionsFile.finishWrite(fos);
        } catch (Exception e) {
            restrictionsFile.failWrite(fos);
            Slog.e(LOG_TAG, "Error writing application restrictions list", e);
        }
    }

    private static void writeBundle(Bundle restrictions, XmlSerializer serializer) throws IOException {
        for (String key : restrictions.keySet()) {
            Object value = restrictions.get(key);
            serializer.startTag(null, TAG_ENTRY);
            serializer.attribute(null, ATTR_KEY, key);
            if (value instanceof Boolean) {
                serializer.attribute(null, "type", ATTR_TYPE_BOOLEAN);
                serializer.text(value.toString());
            } else if (value instanceof Integer) {
                serializer.attribute(null, "type", ATTR_TYPE_INTEGER);
                serializer.text(value.toString());
            } else {
                String str = "";
                if (value == null || (value instanceof String)) {
                    serializer.attribute(null, "type", ATTR_TYPE_STRING);
                    if (value != null) {
                        str = (String) value;
                    }
                    serializer.text(str);
                } else if (value instanceof Bundle) {
                    serializer.attribute(null, "type", ATTR_TYPE_BUNDLE);
                    writeBundle((Bundle) value, serializer);
                } else {
                    int i = 0;
                    if (value instanceof Parcelable[]) {
                        serializer.attribute(null, "type", ATTR_TYPE_BUNDLE_ARRAY);
                        Parcelable[] array = (Parcelable[]) value;
                        int length = array.length;
                        while (i < length) {
                            Parcelable parcelable = array[i];
                            if (parcelable instanceof Bundle) {
                                serializer.startTag(null, TAG_ENTRY);
                                serializer.attribute(null, "type", ATTR_TYPE_BUNDLE);
                                writeBundle((Bundle) parcelable, serializer);
                                serializer.endTag(null, TAG_ENTRY);
                                i++;
                            } else {
                                throw new IllegalArgumentException("bundle-array can only hold Bundles");
                            }
                        }
                        continue;
                    } else {
                        serializer.attribute(null, "type", ATTR_TYPE_STRING_ARRAY);
                        String[] values = (String[]) value;
                        serializer.attribute(null, ATTR_MULTIPLE, Integer.toString(values.length));
                        int length2 = values.length;
                        while (i < length2) {
                            String choice = values[i];
                            serializer.startTag(null, TAG_VALUE);
                            serializer.text(choice != null ? choice : str);
                            serializer.endTag(null, TAG_VALUE);
                            i++;
                        }
                    }
                }
            }
            serializer.endTag(null, TAG_ENTRY);
        }
    }

    public int getUserSerialNumber(int userHandle) {
        int i;
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userHandle);
            i = userInfo != null ? userInfo.serialNumber : -1;
        }
        return i;
    }

    public boolean isUserNameSet(int userHandle) {
        boolean z;
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userHandle);
            z = (userInfo == null || userInfo.name == null) ? false : true;
        }
        return z;
    }

    public int getUserHandle(int userSerialNumber) {
        synchronized (this.mUsersLock) {
            int[] iArr = this.mUserIds;
            for (int userId : iArr) {
                UserInfo info = getUserInfoLU(userId);
                if (info != null && info.serialNumber == userSerialNumber) {
                    return userId;
                }
            }
            return -1;
        }
    }

    public long getUserCreationTime(int userHandle) {
        int callingUserId = UserHandle.getCallingUserId();
        UserInfo userInfo = null;
        synchronized (this.mUsersLock) {
            if (callingUserId == userHandle) {
                userInfo = getUserInfoLU(userHandle);
            } else {
                UserInfo parent = getProfileParentLU(userHandle);
                if (parent != null && parent.id == callingUserId) {
                    userInfo = getUserInfoLU(userHandle);
                }
            }
        }
        if (userInfo != null) {
            return userInfo.creationTime;
        }
        throw new SecurityException("userHandle can only be the calling user or a managed profile associated with this user");
    }

    /* JADX INFO: Multiple debug info for r3v2 int[]: [D('i' int), D('newUsers' int[])] */
    private void updateUserIds() {
        int num = 0;
        synchronized (this.mUsersLock) {
            int userSize = this.mUsers.size();
            for (int i = 0; i < userSize; i++) {
                if (!this.mUsers.valueAt(i).info.partial) {
                    num++;
                }
            }
            int[] newUsers = new int[num];
            int n = 0;
            for (int i2 = 0; i2 < userSize; i2++) {
                if (!this.mUsers.valueAt(i2).info.partial) {
                    newUsers[n] = this.mUsers.keyAt(i2);
                    n++;
                }
            }
            this.mUserIds = newUsers;
        }
    }

    public void onBeforeStartUser(int userId) {
        UserInfo userInfo = getUserInfo(userId);
        if (userInfo != null) {
            int userSerial = userInfo.serialNumber;
            boolean migrateAppsData = !Build.FINGERPRINT.equals(userInfo.lastLoggedInFingerprint) || !Build.FINGERPRINTEX.equals(userInfo.lastLoggedInFingerprintEx);
            this.mUserDataPreparer.prepareUserData(userId, userSerial, 1);
            this.mPm.reconcileAppsData(userId, 1, migrateAppsData);
            if (userId != 0) {
                synchronized (this.mRestrictionsLock) {
                    applyUserRestrictionsLR(userId);
                }
            }
        }
    }

    public void onBeforeUnlockUser(int userId) {
        UserInfo userInfo = getUserInfo(userId);
        if (userInfo != null) {
            int userSerial = userInfo.serialNumber;
            boolean migrateAppsData = !Build.FINGERPRINT.equals(userInfo.lastLoggedInFingerprint) || !Build.FINGERPRINTEX.equals(userInfo.lastLoggedInFingerprintEx);
            this.mUserDataPreparer.prepareUserData(userId, userSerial, 2);
            this.mPm.reconcileAppsData(userId, 2, migrateAppsData);
            Slog.i(LOG_TAG, "Prepare app storage finished onBeforeUnlockUser " + userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void reconcileUsers(String volumeUuid) {
        this.mUserDataPreparer.reconcileUsers(volumeUuid, getUsers(true));
    }

    public void onUserLoggedIn(int userId) {
        UserData userData = getUserDataNoChecks(userId);
        if (userData == null || userData.info.partial) {
            Slog.w(LOG_TAG, "userForeground: unknown user #" + userId);
            return;
        }
        long now = System.currentTimeMillis();
        if (now > EPOCH_PLUS_30_YEARS) {
            userData.info.lastLoggedInTime = now;
        }
        userData.info.lastLoggedInFingerprint = Build.FINGERPRINT;
        userData.info.lastLoggedInFingerprintEx = Build.FINGERPRINTEX;
        scheduleWriteUser(userData);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getNextAvailableId() {
        return getNextAvailableId(false);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getNextAvailableId(boolean isClonedProfile) {
        int nextId;
        synchronized (this.mUsersLock) {
            nextId = scanNextAvailableIdLocked(isClonedProfile);
            if (nextId >= 0) {
                return nextId;
            }
            if (this.mRemovingUserIds.size() > 0) {
                Slog.i(LOG_TAG, "All available IDs are used. Recycling LRU ids.");
                if (isClonedProfile) {
                    for (int i = 128; i < 148; i++) {
                        if (this.mRemovingUserIds.get(i)) {
                            this.mRemovingUserIds.delete(i);
                        }
                    }
                    Iterator<Integer> it = this.mClonedProfileRecentlyRemovedIds.iterator();
                    while (it.hasNext()) {
                        this.mRemovingUserIds.put(it.next().intValue(), true);
                    }
                } else {
                    this.mRemovingUserIds.clear();
                    Iterator<Integer> it2 = this.mRecentlyRemovedIds.iterator();
                    while (it2.hasNext()) {
                        this.mRemovingUserIds.put(it2.next().intValue(), true);
                    }
                }
                nextId = scanNextAvailableIdLocked(isClonedProfile);
            }
        }
        if (nextId >= 0) {
            return nextId;
        }
        throw new IllegalStateException("No user id available!");
    }

    @GuardedBy({"mUsersLock"})
    private int scanNextAvailableIdLocked(boolean isClonedProfile) {
        int minUserId = isClonedProfile ? 128 : 10;
        int maxUserId = isClonedProfile ? 148 : MAX_USER_ID;
        for (int i = minUserId; i < maxUserId; i++) {
            if (!(this.mUsers.indexOfKey(i) >= 0 || this.mRemovingUserIds.get(i) || i == REPAIR_MODE_USER_ID || this.mUserDataPreparer.isUserIdInvalid(i) || ((i >= 125 && i <= 126) || (!isClonedProfile && i >= 128 && i <= 148)))) {
                return i;
            }
        }
        return -1;
    }

    private static String packageToRestrictionsFileName(String packageName) {
        return RESTRICTIONS_FILE_PREFIX + packageName + XML_SUFFIX;
    }

    public void setSeedAccountData(int userId, String accountName, String accountType, PersistableBundle accountOptions, boolean persist) {
        UserData userData;
        checkManageUsersPermission("Require MANAGE_USERS permission to set user seed data");
        synchronized (this.mPackagesLock) {
            synchronized (this.mUsersLock) {
                userData = getUserDataLU(userId);
                if (userData == null) {
                    Slog.e(LOG_TAG, "No such user for settings seed data u=" + userId);
                    return;
                }
                userData.seedAccountName = accountName;
                userData.seedAccountType = accountType;
                userData.seedAccountOptions = accountOptions;
                userData.persistSeedData = persist;
            }
            if (persist) {
                writeUserLP(userData);
            }
        }
    }

    public String getSeedAccountName() throws RemoteException {
        String str;
        checkManageUsersPermission("Cannot get seed account information");
        synchronized (this.mUsersLock) {
            str = getUserDataLU(UserHandle.getCallingUserId()).seedAccountName;
        }
        return str;
    }

    public String getSeedAccountType() throws RemoteException {
        String str;
        checkManageUsersPermission("Cannot get seed account information");
        synchronized (this.mUsersLock) {
            str = getUserDataLU(UserHandle.getCallingUserId()).seedAccountType;
        }
        return str;
    }

    public PersistableBundle getSeedAccountOptions() throws RemoteException {
        PersistableBundle persistableBundle;
        checkManageUsersPermission("Cannot get seed account information");
        synchronized (this.mUsersLock) {
            persistableBundle = getUserDataLU(UserHandle.getCallingUserId()).seedAccountOptions;
        }
        return persistableBundle;
    }

    public void clearSeedAccountData() throws RemoteException {
        checkManageUsersPermission("Cannot clear seed account information");
        synchronized (this.mPackagesLock) {
            synchronized (this.mUsersLock) {
                UserData userData = getUserDataLU(UserHandle.getCallingUserId());
                if (userData != null) {
                    userData.clearSeedAccountData();
                    writeUserLP(userData);
                }
            }
        }
    }

    public boolean someUserHasSeedAccount(String accountName, String accountType) throws RemoteException {
        checkManageUsersPermission("Cannot check seed account information");
        synchronized (this.mUsersLock) {
            int userSize = this.mUsers.size();
            for (int i = 0; i < userSize; i++) {
                UserData data = this.mUsers.valueAt(i);
                if (!data.info.isInitialized()) {
                    if (data.seedAccountName == null) {
                        continue;
                    } else if (data.seedAccountName.equals(accountName)) {
                        if (data.seedAccountType == null) {
                            continue;
                        } else if (data.seedAccountType.equals(accountType)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.pm.UserManagerService */
    /* JADX WARN: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new Shell().exec(this, in, out, err, args, callback, resultReceiver);
    }

    /* access modifiers changed from: package-private */
    public int onShellCommand(Shell shell, String cmd) {
        if (cmd == null) {
            return shell.handleDefaultCommands(cmd);
        }
        PrintWriter pw = shell.getOutPrintWriter();
        try {
            if ((cmd.hashCode() == 3322014 && cmd.equals("list")) ? false : true) {
                return shell.handleDefaultCommands(cmd);
            }
            return runList(pw);
        } catch (RemoteException e) {
            pw.println("Remote exception: " + e);
            return -1;
        }
    }

    private int runList(PrintWriter pw) throws RemoteException {
        IActivityManager am = ActivityManager.getService();
        List<UserInfo> users = getUsers(false);
        if (users == null) {
            pw.println("Error: couldn't get users");
            return 1;
        }
        pw.println("Users:");
        for (int i = 0; i < users.size(); i++) {
            String running = am.isUserRunning(users.get(i).id, 0) ? " running" : "";
            pw.println("\t" + users.get(i).toString() + running);
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x01df, code lost:
        r0 = th;
     */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        Throwable th;
        Object obj;
        Throwable th2;
        int i;
        long nowRealtime;
        int state;
        UserManagerService userManagerService = this;
        if (DumpUtils.checkDumpPermission(userManagerService.mContext, LOG_TAG, pw)) {
            long now = System.currentTimeMillis();
            long nowRealtime2 = SystemClock.elapsedRealtime();
            StringBuilder sb = new StringBuilder();
            synchronized (userManagerService.mPackagesLock) {
                try {
                    Object obj2 = userManagerService.mUsersLock;
                    synchronized (obj2) {
                        try {
                            pw.println("Users:");
                            int i2 = 0;
                            while (i2 < userManagerService.mUsers.size()) {
                                UserData userData = userManagerService.mUsers.valueAt(i2);
                                if (userData == null) {
                                    obj = obj2;
                                    i = i2;
                                    nowRealtime = nowRealtime2;
                                } else {
                                    UserInfo userInfo = userData.info;
                                    int userId = userInfo.id;
                                    pw.print("  ");
                                    pw.print(userInfo);
                                    pw.print(" serialNo=");
                                    pw.print(userInfo.serialNumber);
                                    if (userManagerService.mRemovingUserIds.get(userId)) {
                                        try {
                                            pw.print(" <removing> ");
                                        } catch (Throwable th3) {
                                            th2 = th3;
                                            obj = obj2;
                                        }
                                    }
                                    if (userInfo.partial) {
                                        pw.print(" <partial>");
                                    }
                                    pw.println();
                                    pw.print("    State: ");
                                    synchronized (userManagerService.mUserStates) {
                                        state = userManagerService.mUserStates.get(userId, -1);
                                    }
                                    pw.println(UserState.stateToString(state));
                                    pw.print("    Created: ");
                                    try {
                                        nowRealtime = nowRealtime2;
                                    } catch (Throwable th4) {
                                        th2 = th4;
                                        obj = obj2;
                                        throw th2;
                                    }
                                    try {
                                        dumpTimeAgo(pw, sb, now, userInfo.creationTime);
                                        pw.print("    Last logged in: ");
                                        obj = obj2;
                                        i = i2;
                                        try {
                                            dumpTimeAgo(pw, sb, now, userInfo.lastLoggedInTime);
                                            pw.print("    Last logged in fingerprint: ");
                                            pw.println(userInfo.lastLoggedInFingerprint);
                                            pw.print("    Last logged in fingerprintEx: ");
                                            pw.println(userInfo.lastLoggedInFingerprintEx);
                                            pw.print("    Start time: ");
                                            dumpTimeAgo(pw, sb, nowRealtime, userData.startRealtime);
                                            pw.print("    Unlock time: ");
                                            dumpTimeAgo(pw, sb, nowRealtime, userData.unlockRealtime);
                                            pw.print("    Has profile owner: ");
                                            userManagerService = this;
                                        } catch (Throwable th5) {
                                            th2 = th5;
                                            throw th2;
                                        }
                                    } catch (Throwable th6) {
                                        th2 = th6;
                                        obj = obj2;
                                        throw th2;
                                    }
                                    try {
                                        pw.println(userManagerService.mIsUserManaged.get(userId));
                                        pw.println("    Restrictions:");
                                        synchronized (userManagerService.mRestrictionsLock) {
                                            UserRestrictionsUtils.dumpRestrictions(pw, "      ", userManagerService.mBaseUserRestrictions.get(userInfo.id));
                                            pw.println("    Device policy global restrictions:");
                                            UserRestrictionsUtils.dumpRestrictions(pw, "      ", userManagerService.mDevicePolicyGlobalUserRestrictions.get(userInfo.id));
                                            pw.println("    Device policy local restrictions:");
                                            UserRestrictionsUtils.dumpRestrictions(pw, "      ", userManagerService.mDevicePolicyLocalUserRestrictions.get(userInfo.id));
                                            pw.println("    Effective restrictions:");
                                            UserRestrictionsUtils.dumpRestrictions(pw, "      ", userManagerService.mCachedEffectiveUserRestrictions.get(userInfo.id));
                                        }
                                        if (userData.account != null) {
                                            pw.print("    Account name: " + userData.account);
                                            pw.println();
                                        }
                                        if (userData.seedAccountName != null) {
                                            pw.print("    Seed account name: " + userData.seedAccountName);
                                            pw.println();
                                            if (userData.seedAccountType != null) {
                                                pw.print("         account type: " + userData.seedAccountType);
                                                pw.println();
                                            }
                                            if (userData.seedAccountOptions != null) {
                                                pw.print("         account options exist");
                                                pw.println();
                                            }
                                        }
                                    } catch (Throwable th7) {
                                        th2 = th7;
                                        throw th2;
                                    }
                                }
                                i2 = i + 1;
                                nowRealtime2 = nowRealtime;
                                obj2 = obj;
                            }
                            try {
                                pw.println();
                                pw.println("  Device owner id:" + userManagerService.mDeviceOwnerUserId);
                                pw.println();
                                pw.println("  Guest restrictions:");
                                synchronized (userManagerService.mGuestRestrictions) {
                                    UserRestrictionsUtils.dumpRestrictions(pw, "    ", userManagerService.mGuestRestrictions);
                                }
                                synchronized (userManagerService.mUsersLock) {
                                    pw.println();
                                    pw.println("  Device managed: " + userManagerService.mIsDeviceManaged);
                                    if (userManagerService.mRemovingUserIds.size() > 0) {
                                        pw.println();
                                        pw.println("  Recently removed userIds: " + userManagerService.mRecentlyRemovedIds);
                                    }
                                }
                                synchronized (userManagerService.mUserStates) {
                                    pw.println("  Started users state: " + userManagerService.mUserStates);
                                }
                                pw.println();
                                pw.println("  Max users: " + UserManager.getMaxSupportedUsers());
                                pw.println("  Supports switchable users: " + UserManager.supportsMultipleUsers());
                                pw.println("  All guests ephemeral: " + Resources.getSystem().getBoolean(17891463));
                                return;
                            } catch (Throwable th8) {
                                th = th8;
                                throw th;
                            }
                        } catch (Throwable th9) {
                            th2 = th9;
                            obj = obj2;
                            throw th2;
                        }
                    }
                } catch (Throwable th10) {
                    th = th10;
                    throw th;
                }
            }
        } else {
            return;
        }
        while (true) {
        }
    }

    private static void dumpTimeAgo(PrintWriter pw, StringBuilder sb, long nowTime, long time) {
        if (time == 0) {
            pw.println("<unknown>");
            return;
        }
        sb.setLength(0);
        TimeUtils.formatDuration(nowTime - time, sb);
        sb.append(" ago");
        pw.println(sb);
    }

    final class MainHandler extends Handler {
        MainHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                removeMessages(1, msg.obj);
                synchronized (UserManagerService.this.mPackagesLock) {
                    UserData userData = UserManagerService.this.getUserDataNoChecks(((UserData) msg.obj).info.id);
                    if (userData != null) {
                        UserManagerService.this.writeUserLP(userData);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isUserInitialized(int userId) {
        return this.mLocalService.isUserInitialized(userId);
    }

    /* access modifiers changed from: private */
    public class LocalService extends UserManagerInternal {
        private LocalService() {
        }

        public void setDevicePolicyUserRestrictions(int userId, Bundle restrictions, boolean isDeviceOwner, int cameraRestrictionScope) {
            UserManagerService.this.setDevicePolicyUserRestrictionsInner(userId, restrictions, isDeviceOwner, cameraRestrictionScope);
        }

        public Bundle getBaseUserRestrictions(int userId) {
            Bundle bundle;
            synchronized (UserManagerService.this.mRestrictionsLock) {
                bundle = (Bundle) UserManagerService.this.mBaseUserRestrictions.get(userId);
            }
            return bundle;
        }

        public void setBaseUserRestrictionsByDpmsForMigration(int userId, Bundle baseRestrictions) {
            synchronized (UserManagerService.this.mRestrictionsLock) {
                if (UserManagerService.this.updateRestrictionsIfNeededLR(userId, new Bundle(baseRestrictions), UserManagerService.this.mBaseUserRestrictions)) {
                    UserManagerService.this.invalidateEffectiveUserRestrictionsLR(userId);
                }
            }
            UserData userData = UserManagerService.this.getUserDataNoChecks(userId);
            synchronized (UserManagerService.this.mPackagesLock) {
                if (userData != null) {
                    UserManagerService.this.writeUserLP(userData);
                } else {
                    Slog.w(UserManagerService.LOG_TAG, "UserInfo not found for " + userId);
                }
            }
        }

        public boolean getUserRestriction(int userId, String key) {
            return UserManagerService.this.getUserRestrictions(userId).getBoolean(key);
        }

        public void addUserRestrictionsListener(UserManagerInternal.UserRestrictionsListener listener) {
            synchronized (UserManagerService.this.mUserRestrictionsListeners) {
                UserManagerService.this.mUserRestrictionsListeners.add(listener);
            }
        }

        public void removeUserRestrictionsListener(UserManagerInternal.UserRestrictionsListener listener) {
            synchronized (UserManagerService.this.mUserRestrictionsListeners) {
                UserManagerService.this.mUserRestrictionsListeners.remove(listener);
            }
        }

        public void setDeviceManaged(boolean isManaged) {
            synchronized (UserManagerService.this.mUsersLock) {
                UserManagerService.this.mIsDeviceManaged = isManaged;
            }
        }

        public void setUserManaged(int userId, boolean isManaged) {
            synchronized (UserManagerService.this.mUsersLock) {
                UserManagerService.this.mIsUserManaged.put(userId, isManaged);
            }
        }

        public void setUserIcon(int userId, Bitmap bitmap) {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (UserManagerService.this.mPackagesLock) {
                    UserData userData = UserManagerService.this.getUserDataNoChecks(userId);
                    if (userData != null) {
                        if (!userData.info.partial) {
                            UserManagerService.this.writeBitmapLP(userData.info, bitmap);
                            UserManagerService.this.writeUserLP(userData);
                            UserManagerService.this.sendUserInfoChangedBroadcast(userId);
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                    }
                    Slog.w(UserManagerService.LOG_TAG, "setUserIcon: unknown user #" + userId);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void setForceEphemeralUsers(boolean forceEphemeralUsers) {
            synchronized (UserManagerService.this.mUsersLock) {
                UserManagerService.this.mForceEphemeralUsers = forceEphemeralUsers;
            }
        }

        public void removeAllUsers() {
            if (ActivityManager.getCurrentUser() == 0) {
                UserManagerService.this.removeNonSystemUsers();
                return;
            }
            BroadcastReceiver userSwitchedReceiver = new BroadcastReceiver() {
                /* class com.android.server.pm.UserManagerService.LocalService.AnonymousClass1 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    if (intent.getIntExtra("android.intent.extra.user_handle", -10000) == 0) {
                        UserManagerService.this.mContext.unregisterReceiver(this);
                        UserManagerService.this.removeNonSystemUsers();
                    }
                }
            };
            IntentFilter userSwitchedFilter = new IntentFilter();
            userSwitchedFilter.addAction("android.intent.action.USER_SWITCHED");
            UserManagerService.this.mContext.registerReceiver(userSwitchedReceiver, userSwitchedFilter, null, UserManagerService.this.mHandler);
            ((ActivityManager) UserManagerService.this.mContext.getSystemService("activity")).switchUser(0);
        }

        public void onEphemeralUserStop(int userId) {
            synchronized (UserManagerService.this.mUsersLock) {
                UserInfo userInfo = UserManagerService.this.getUserInfoLU(userId);
                if (userInfo != null && userInfo.isEphemeral()) {
                    userInfo.flags |= 64;
                    if (userInfo.isGuest()) {
                        userInfo.guestToRemove = true;
                    }
                }
            }
        }

        public UserInfo createUserEvenWhenDisallowed(String name, int flags, String[] disallowedPackages) {
            UserInfo user = UserManagerService.this.createUserInternalUnchecked(name, flags, -10000, disallowedPackages);
            if (user != null && !user.isAdmin() && !user.isDemo()) {
                UserManagerService.this.setUserRestriction("no_sms", true, user.id);
                UserManagerService.this.setUserRestriction("no_outgoing_calls", true, user.id);
            }
            return user;
        }

        public boolean removeUserEvenWhenDisallowed(int userId) {
            return UserManagerService.this.removeUserUnchecked(userId);
        }

        public boolean isUserRunning(int userId) {
            boolean z;
            synchronized (UserManagerService.this.mUserStates) {
                z = UserManagerService.this.mUserStates.get(userId, -1) >= 0;
            }
            return z;
        }

        public void setUserState(int userId, int userState) {
            synchronized (UserManagerService.this.mUserStates) {
                UserManagerService.this.mUserStates.put(userId, userState);
            }
        }

        public void removeUserState(int userId) {
            synchronized (UserManagerService.this.mUserStates) {
                UserManagerService.this.mUserStates.delete(userId);
            }
        }

        public int[] getUserIds() {
            return UserManagerService.this.getUserIds();
        }

        public boolean isUserUnlockingOrUnlocked(int userId) {
            int state;
            synchronized (UserManagerService.this.mUserStates) {
                state = UserManagerService.this.mUserStates.get(userId, -1);
            }
            if (state == 4 || state == 5) {
                return StorageManager.isUserKeyUnlocked(userId);
            }
            return state == 2 || state == 3;
        }

        public boolean isUserUnlocked(int userId) {
            int state;
            synchronized (UserManagerService.this.mUserStates) {
                state = UserManagerService.this.mUserStates.get(userId, -1);
            }
            if (state == 4 || state == 5) {
                return StorageManager.isUserKeyUnlocked(userId);
            }
            return state == 3;
        }

        public boolean isUserInitialized(int userId) {
            UserInfo userInfo = getUserInfo(userId);
            if (userInfo == null || (userInfo.flags & 16) == 0) {
                return false;
            }
            return true;
        }

        public boolean exists(int userId) {
            return UserManagerService.this.getUserInfoNoChecks(userId) != null;
        }

        public boolean isProfileAccessible(int callingUserId, int targetUserId, String debugMsg, boolean throwSecurityException) {
            if (targetUserId == callingUserId) {
                return true;
            }
            synchronized (UserManagerService.this.mUsersLock) {
                UserInfo callingUserInfo = UserManagerService.this.getUserInfoLU(callingUserId);
                if ((callingUserInfo == null || callingUserInfo.isManagedProfile()) && throwSecurityException) {
                    throw new SecurityException(debugMsg + " for another profile " + targetUserId + " from " + callingUserId);
                }
                UserInfo targetUserInfo = UserManagerService.this.getUserInfoLU(targetUserId);
                if (targetUserInfo != null) {
                    if (targetUserInfo.isEnabled()) {
                        if (targetUserInfo.profileGroupId != -10000) {
                            if (targetUserInfo.profileGroupId == callingUserInfo.profileGroupId) {
                                return true;
                            }
                        }
                        if (!throwSecurityException) {
                            return false;
                        }
                        throw new SecurityException(debugMsg + " for unrelated profile " + targetUserId);
                    }
                }
                if (throwSecurityException) {
                    Slog.w(UserManagerService.LOG_TAG, debugMsg + " for disabled profile " + targetUserId + " from " + callingUserId);
                }
                return false;
            }
        }

        public int getProfileParentId(int userId) {
            synchronized (UserManagerService.this.mUsersLock) {
                UserInfo profileParent = UserManagerService.this.getProfileParentLU(userId);
                if (profileParent == null) {
                    return userId;
                }
                return profileParent.id;
            }
        }

        public boolean isSettingRestrictedForUser(String setting, int userId, String value, int callingUid) {
            return UserRestrictionsUtils.isSettingRestrictedForUser(UserManagerService.this.mContext, setting, userId, value, callingUid);
        }

        public boolean isClonedProfile(int userId) {
            boolean z;
            synchronized (UserManagerService.this.mUsersLock) {
                UserInfo userInfo = UserManagerService.this.getUserInfoLU(userId);
                z = userInfo != null && userInfo.isClonedProfile();
            }
            return z;
        }

        public UserInfo getUserInfo(int userId) {
            UserInfo userInfoLU;
            synchronized (UserManagerService.this.mUsersLock) {
                userInfoLU = UserManagerService.this.getUserInfoLU(userId);
            }
            return userInfoLU;
        }

        public boolean hasClonedProfile() {
            return UserManagerService.this.mHasClonedProfile;
        }

        public UserInfo findClonedProfile() {
            synchronized (UserManagerService.this.mUsersLock) {
                int size = UserManagerService.this.mUsers.size();
                for (int i = 0; i < size; i++) {
                    UserInfo user = ((UserData) UserManagerService.this.mUsers.valueAt(i)).info;
                    if (user.isClonedProfile() && !UserManagerService.this.mRemovingUserIds.get(user.id)) {
                        return user;
                    }
                }
                return null;
            }
        }

        public boolean isRemovingUser(int userId) {
            return UserManagerService.this.mRemovingUserIds.get(userId) && userId >= 128 && userId < 148;
        }

        public boolean isSameGroupForClone(int callingUserId, int targetUserId) {
            synchronized (UserManagerService.this.mUsersLock) {
                int size = UserManagerService.this.mUsers.size();
                int i = 0;
                while (true) {
                    boolean z = false;
                    if (i >= size) {
                        return false;
                    }
                    UserInfo user = ((UserData) UserManagerService.this.mUsers.valueAt(i)).info;
                    if (!user.isClonedProfile() || UserManagerService.this.mRemovingUserIds.get(user.id)) {
                        i++;
                    } else {
                        if ((callingUserId == user.id && targetUserId == user.profileGroupId) || ((targetUserId == user.id && callingUserId == user.profileGroupId) || (callingUserId == user.id && targetUserId == user.id))) {
                            z = true;
                        }
                        return z;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeNonSystemUsers() {
        ArrayList<UserInfo> usersToRemove = new ArrayList<>();
        synchronized (this.mUsersLock) {
            int userSize = this.mUsers.size();
            for (int i = 0; i < userSize; i++) {
                UserInfo ui = this.mUsers.valueAt(i).info;
                if (ui.id != 0) {
                    usersToRemove.add(ui);
                }
            }
        }
        Iterator<UserInfo> it = usersToRemove.iterator();
        while (it.hasNext()) {
            removeUser(it.next().id);
        }
    }

    private class Shell extends ShellCommand {
        private Shell() {
        }

        public int onCommand(String cmd) {
            return UserManagerService.this.onShellCommand(this, cmd);
        }

        public void onHelp() {
            PrintWriter pw = getOutPrintWriter();
            pw.println("User manager (user) commands:");
            pw.println("  help");
            pw.println("    Print this help text.");
            pw.println("");
            pw.println("  list");
            pw.println("    Prints all users on the system.");
        }
    }

    private static void debug(String message) {
        Log.d(LOG_TAG, message + "");
    }

    @VisibleForTesting
    static int getMaxManagedProfiles() {
        if (!Build.IS_DEBUGGABLE) {
            return 1;
        }
        return SystemProperties.getInt("persist.sys.max_profiles", 1);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mUsersLock"})
    @VisibleForTesting
    public int getFreeProfileBadgeLU(int parentUserId) {
        int maxManagedProfiles = getMaxManagedProfiles();
        boolean[] usedBadges = new boolean[maxManagedProfiles];
        int userSize = this.mUsers.size();
        for (int i = 0; i < userSize; i++) {
            UserInfo ui = this.mUsers.valueAt(i).info;
            if (ui.isManagedProfile() && ui.profileGroupId == parentUserId && !this.mRemovingUserIds.get(ui.id) && ui.profileBadge < maxManagedProfiles) {
                usedBadges[ui.profileBadge] = true;
            }
        }
        for (int i2 = 0; i2 < maxManagedProfiles; i2++) {
            if (!usedBadges[i2]) {
                return i2;
            }
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public boolean hasManagedProfile(int userId) {
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userId);
            int userSize = this.mUsers.size();
            for (int i = 0; i < userSize; i++) {
                UserInfo profile = this.mUsers.valueAt(i).info;
                if (userId != profile.id && isProfileOf(userInfo, profile)) {
                    return true;
                }
            }
            return false;
        }
    }

    private void verifyCallingPackage(String callingPackage, int callingUid) {
        if (this.mPm.getPackageUid(callingPackage, 0, UserHandle.getUserId(callingUid)) != callingUid) {
            throw new SecurityException("Specified package " + callingPackage + " does not match the calling uid " + callingUid);
        }
    }
}

package com.android.server.pm;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IStopUserCallback;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
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
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManager.EnforcingUser;
import android.os.UserManagerInternal;
import android.os.UserManagerInternal.UserRestrictionsListener;
import android.os.storage.StorageManager;
import android.provider.Settings.Secure;
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
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IAppOpsService.Stub;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.LocalServices;
import com.android.server.LockGuard;
import com.android.server.SystemService;
import com.android.server.am.UserState;
import com.android.server.storage.DeviceStorageMonitorInternal;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
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
import libcore.io.IoUtils;
import libcore.util.Objects;
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
    static final int MAX_MANAGED_PROFILES = 1;
    static final int MAX_RECENTLY_REMOVED_IDS_SIZE = 100;
    static final int MAX_USER_ID = 21474;
    static final int MIN_USER_ID = 10;
    private static final boolean RELEASE_DELETED_USER_ID = false;
    private static final int REPAIR_MODE_USER_ID = 127;
    private static final String RESTRICTIONS_FILE_PREFIX = "res_";
    private static final String TAG_ACCOUNT = "account";
    private static final String TAG_DEVICE_OWNER_USER_ID = "deviceOwnerUserId";
    private static final String TAG_DEVICE_POLICY_GLOBAL_RESTRICTIONS = "device_policy_global_restrictions";
    private static final String TAG_DEVICE_POLICY_RESTRICTIONS = "device_policy_restrictions";
    private static final String TAG_ENTRY = "entry";
    private static final String TAG_GLOBAL_RESTRICTION_OWNER_ID = "globalRestrictionOwnerUserId";
    private static final String TAG_GUEST_RESTRICTIONS = "guestRestrictions";
    private static final String TAG_NAME = "name";
    private static final String TAG_RESTRICTIONS = "restrictions";
    private static final String TAG_SEED_ACCOUNT_OPTIONS = "seedAccountOptions";
    private static final String TAG_USER = "user";
    private static final String TAG_USERS = "users";
    private static final String TAG_VALUE = "value";
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
    private boolean isOwnerNameChanged;
    private IAppOpsService mAppOpsService;
    @GuardedBy("mRestrictionsLock")
    private final SparseArray<Bundle> mAppliedUserRestrictions;
    @GuardedBy("mRestrictionsLock")
    private final SparseArray<Bundle> mBaseUserRestrictions;
    @GuardedBy("mRestrictionsLock")
    private final SparseArray<Bundle> mCachedEffectiveUserRestrictions;
    private final Context mContext;
    @GuardedBy("mRestrictionsLock")
    private int mDeviceOwnerUserId;
    @GuardedBy("mRestrictionsLock")
    private final SparseArray<Bundle> mDevicePolicyGlobalUserRestrictions;
    @GuardedBy("mRestrictionsLock")
    private final SparseArray<Bundle> mDevicePolicyLocalUserRestrictions;
    private final BroadcastReceiver mDisableQuietModeCallback;
    @GuardedBy("mUsersLock")
    private boolean mForceEphemeralUsers;
    @GuardedBy("mGuestRestrictions")
    private final Bundle mGuestRestrictions;
    private final Handler mHandler;
    @GuardedBy("mUsersLock")
    private boolean mIsDeviceManaged;
    @GuardedBy("mUsersLock")
    private final SparseBooleanArray mIsUserManaged;
    private final LocalService mLocalService;
    private final LockPatternUtils mLockPatternUtils;
    @GuardedBy("mPackagesLock")
    private int mNextSerialNumber;
    private final Object mPackagesLock;
    protected final PackageManagerService mPm;
    @GuardedBy("mUsersLock")
    private final LinkedList<Integer> mRecentlyRemovedIds;
    @GuardedBy("mUsersLock")
    private final SparseBooleanArray mRemovingUserIds;
    private final Object mRestrictionsLock;
    private final UserDataPreparer mUserDataPreparer;
    @GuardedBy("mUsersLock")
    private int[] mUserIds;
    private final File mUserListFile;
    @GuardedBy("mUserRestrictionsListeners")
    private final ArrayList<UserRestrictionsListener> mUserRestrictionsListeners;
    @GuardedBy("mUserStates")
    private final SparseIntArray mUserStates;
    private int mUserVersion;
    @GuardedBy("mUsersLock")
    private final SparseArray<UserData> mUsers;
    private final File mUsersDir;
    private final Object mUsersLock;

    public static class LifeCycle extends SystemService {
        private UserManagerService mUms;

        public LifeCycle(Context context) {
            super(context);
        }

        public void onStart() {
            this.mUms = UserManagerService.getInstance();
            publishBinderService(UserManagerService.TAG_USER, this.mUms);
        }

        public void onBootPhase(int phase) {
            if (phase == 550) {
                this.mUms.cleanupPartialUsers();
            }
        }
    }

    private class LocalService extends UserManagerInternal {
        /* synthetic */ LocalService(UserManagerService this$0, LocalService -this1) {
            this();
        }

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

        public void addUserRestrictionsListener(UserRestrictionsListener listener) {
            synchronized (UserManagerService.this.mUserRestrictionsListeners) {
                UserManagerService.this.mUserRestrictionsListeners.add(listener);
            }
        }

        public void removeUserRestrictionsListener(UserRestrictionsListener listener) {
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
                    if (userData == null || userData.info.partial) {
                        Slog.w(UserManagerService.LOG_TAG, "setUserIcon: unknown user #" + userId);
                    } else {
                        UserManagerService.this.writeBitmapLP(userData.info, bitmap);
                        UserManagerService.this.writeUserLP(userData);
                        UserManagerService.this.sendUserInfoChangedBroadcast(userId);
                        Binder.restoreCallingIdentity(ident);
                    }
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

        public UserInfo createUserEvenWhenDisallowed(String name, int flags) {
            UserInfo user = UserManagerService.this.createUserInternalUnchecked(name, flags, -10000, null);
            if (!(user == null || (user.isAdmin() ^ 1) == 0)) {
                UserManagerService.this.setUserRestriction("no_sms", true, user.id);
                UserManagerService.this.setUserRestriction("no_outgoing_calls", true, user.id);
            }
            return user;
        }

        public boolean removeUserEvenWhenDisallowed(int userId) {
            return UserManagerService.this.removeUserUnchecked(userId);
        }

        public boolean isUserRunning(int userId) {
            boolean z = false;
            synchronized (UserManagerService.this.mUserStates) {
                if (UserManagerService.this.mUserStates.get(userId, -1) >= 0) {
                    z = true;
                }
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
            boolean z = true;
            synchronized (UserManagerService.this.mUserStates) {
                int state = UserManagerService.this.mUserStates.get(userId, -1);
                if (!(state == 2 || state == 3)) {
                    z = false;
                }
            }
            return z;
        }

        public boolean isUserUnlocked(int userId) {
            boolean z;
            synchronized (UserManagerService.this.mUserStates) {
                z = UserManagerService.this.mUserStates.get(userId, -1) == 3;
            }
            return z;
        }

        public boolean isClonedProfile(int userId) {
            boolean isClonedProfile;
            synchronized (UserManagerService.this.mUsersLock) {
                UserInfo userInfo = UserManagerService.this.getUserInfoLU(userId);
                isClonedProfile = userInfo != null ? userInfo.isClonedProfile() : false;
            }
            return isClonedProfile;
        }

        public UserInfo getUserInfo(int userId) {
            UserInfo -wrap1;
            synchronized (UserManagerService.this.mUsersLock) {
                -wrap1 = UserManagerService.this.getUserInfoLU(userId);
            }
            return -wrap1;
        }

        public UserInfo findClonedProfile() {
            synchronized (UserManagerService.this.mUsersLock) {
                int size = UserManagerService.this.mUsers.size();
                int i = 0;
                while (i < size) {
                    UserInfo user = ((UserData) UserManagerService.this.mUsers.valueAt(i)).info;
                    if (!user.isClonedProfile() || (UserManagerService.this.mRemovingUserIds.get(user.id) ^ 1) == 0) {
                        i++;
                    } else {
                        return user;
                    }
                }
                return null;
            }
        }

        /* JADX WARNING: Missing block: B:15:0x0044, code:
            return r3;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean isSameGroupForClone(int callingUserId, int targetUserId) {
            synchronized (UserManagerService.this.mUsersLock) {
                int size = UserManagerService.this.mUsers.size();
                int i = 0;
                while (i < size) {
                    UserInfo user = ((UserData) UserManagerService.this.mUsers.valueAt(i)).info;
                    if (!user.isClonedProfile() || (UserManagerService.this.mRemovingUserIds.get(user.id) ^ 1) == 0) {
                        i++;
                    } else {
                        boolean z;
                        if (!(callingUserId == user.id && targetUserId == user.profileGroupId)) {
                            if (!(targetUserId == user.id && callingUserId == user.profileGroupId)) {
                                z = callingUserId == user.id && targetUserId == user.id;
                            }
                        }
                        z = true;
                    }
                }
                return false;
            }
        }
    }

    final class MainHandler extends Handler {
        MainHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    removeMessages(1, msg.obj);
                    synchronized (UserManagerService.this.mPackagesLock) {
                        UserData userData = UserManagerService.this.getUserDataNoChecks(((UserData) msg.obj).info.id);
                        if (userData != null) {
                            UserManagerService.this.writeUserLP(userData);
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class Shell extends ShellCommand {
        /* synthetic */ Shell(UserManagerService this$0, Shell -this1) {
            this();
        }

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

    static class UserData {
        String account;
        UserInfo info;
        boolean persistSeedData;
        String seedAccountName;
        PersistableBundle seedAccountOptions;
        String seedAccountType;

        UserData() {
        }

        void clearSeedAccountData() {
            this.seedAccountName = null;
            this.seedAccountType = null;
            this.seedAccountOptions = null;
            this.persistSeedData = false;
        }
    }

    public static UserManagerService getInstance() {
        UserManagerService userManagerService;
        synchronized (UserManagerService.class) {
            userManagerService = sInstance;
        }
        return userManagerService;
    }

    UserManagerService(Context context) {
        this(context, null, null, new Object(), context.getCacheDir());
    }

    UserManagerService(Context context, PackageManagerService pm, UserDataPreparer userDataPreparer, Object packagesLock) {
        this(context, pm, userDataPreparer, packagesLock, Environment.getDataDirectory());
    }

    private UserManagerService(Context context, PackageManagerService pm, UserDataPreparer userDataPreparer, Object packagesLock, File dataDir) {
        this.mUsersLock = LockGuard.installNewLock(2);
        this.mRestrictionsLock = new Object();
        this.mUsers = new SparseArray();
        this.mBaseUserRestrictions = new SparseArray();
        this.mCachedEffectiveUserRestrictions = new SparseArray();
        this.mAppliedUserRestrictions = new SparseArray();
        this.mDevicePolicyGlobalUserRestrictions = new SparseArray();
        this.mDeviceOwnerUserId = -10000;
        this.mDevicePolicyLocalUserRestrictions = new SparseArray();
        this.mGuestRestrictions = new Bundle();
        this.mRemovingUserIds = new SparseBooleanArray();
        this.mRecentlyRemovedIds = new LinkedList();
        this.mUserVersion = 0;
        this.mIsUserManaged = new SparseBooleanArray();
        this.mUserRestrictionsListeners = new ArrayList();
        this.ACTION_DISABLE_QUIET_MODE_AFTER_UNLOCK = "com.android.server.pm.DISABLE_QUIET_MODE_AFTER_UNLOCK";
        this.mDisableQuietModeCallback = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("com.android.server.pm.DISABLE_QUIET_MODE_AFTER_UNLOCK".equals(intent.getAction())) {
                    IntentSender target = (IntentSender) intent.getParcelableExtra("android.intent.extra.INTENT");
                    UserManagerService.this.setQuietModeEnabled(intent.getIntExtra("android.intent.extra.USER_ID", 0), false);
                    if (target != null) {
                        try {
                            UserManagerService.this.mContext.startIntentSender(target, null, 0, 0, 0);
                        } catch (SendIntentException e) {
                        }
                    }
                }
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
                this.isOwnerNameChanged = info.name.equals(this.mContext.getResources().getString(17040533)) ^ 1;
            }
        }
        this.mLocalService = new LocalService(this, null);
        LocalServices.addService(UserManagerInternal.class, this.mLocalService);
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mUserStates.put(0, 0);
    }

    void systemReady() {
        this.mAppOpsService = Stub.asInterface(ServiceManager.getService("appops"));
        synchronized (this.mRestrictionsLock) {
            applyUserRestrictionsLR(0);
        }
        UserInfo currentGuestUser = findCurrentGuestUser();
        if (!(currentGuestUser == null || (hasUserRestriction("no_config_wifi", currentGuestUser.id) ^ 1) == 0)) {
            setUserRestriction("no_config_wifi", true, currentGuestUser.id);
        }
        this.mContext.registerReceiver(this.mDisableQuietModeCallback, new IntentFilter("com.android.server.pm.DISABLE_QUIET_MODE_AFTER_UNLOCK"), null, this.mHandler);
    }

    void cleanupPartialUsers() {
        int i;
        UserInfo ui;
        ArrayList<UserInfo> partials = new ArrayList();
        synchronized (this.mUsersLock) {
            int userSize = this.mUsers.size();
            i = 0;
            while (i < userSize) {
                ui = ((UserData) this.mUsers.valueAt(i)).info;
                if ((ui.partial || ui.guestToRemove || ui.isEphemeral()) && i != 0) {
                    partials.add(ui);
                    addRemovingUserIdLocked(ui.id);
                    ui.partial = true;
                }
                i++;
            }
        }
        int partialsSize = partials.size();
        for (i = 0; i < partialsSize; i++) {
            ui = (UserInfo) partials.get(i);
            Slog.w(LOG_TAG, "Removing partially created user " + ui.id + " (name=" + ui.name + ")");
            removeUserState(ui.id);
        }
    }

    public String getUserAccount(int userId) {
        String str;
        checkManageUserAndAcrossUsersFullPermission("get user account");
        synchronized (this.mUsersLock) {
            str = ((UserData) this.mUsers.get(userId)).account;
        }
        return str;
    }

    /* JADX WARNING: Missing block: B:20:0x0040, code:
            if (r2 == null) goto L_0x0045;
     */
    /* JADX WARNING: Missing block: B:21:0x0042, code:
            writeUserLP(r2);
     */
    /* JADX WARNING: Missing block: B:23:0x0046, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setUserAccount(int userId, String accountName) {
        checkManageUserAndAcrossUsersFullPermission("set user account");
        UserData userToUpdate = null;
        synchronized (this.mPackagesLock) {
            synchronized (this.mUsersLock) {
                UserData userData = (UserData) this.mUsers.get(userId);
                if (userData == null) {
                    Slog.e(LOG_TAG, "User not found for setting user account: u" + userId);
                } else if (!Objects.equal(userData.account, accountName)) {
                    userData.account = accountName;
                    userToUpdate = userData;
                }
            }
        }
    }

    public UserInfo getPrimaryUser() {
        checkManageUsersPermission("query users");
        synchronized (this.mUsersLock) {
            int userSize = this.mUsers.size();
            int i = 0;
            while (i < userSize) {
                UserInfo ui = ((UserData) this.mUsers.valueAt(i)).info;
                if (!ui.isPrimary() || (this.mRemovingUserIds.get(ui.id) ^ 1) == 0) {
                    i++;
                } else {
                    return ui;
                }
            }
            return null;
        }
    }

    public List<UserInfo> getUsers(boolean excludeDying) {
        ArrayList<UserInfo> users;
        checkManageOrCreateUsersPermission("query users");
        synchronized (this.mUsersLock) {
            users = new ArrayList(this.mUsers.size());
            int userSize = this.mUsers.size();
            for (int i = 0; i < userSize; i++) {
                UserInfo ui = ((UserData) this.mUsers.valueAt(i)).info;
                if (!ui.partial) {
                    if (!(excludeDying && (this.mRemovingUserIds.get(ui.id) ^ 1) == 0)) {
                        users.add(userWithName(ui));
                    }
                    if (ui.id == 0 && (this.isOwnerNameChanged ^ 1) != 0) {
                        boolean nameChanged = false;
                        String ownerName = this.mContext.getResources().getString(17040533);
                        if (!(TextUtils.isEmpty(ui.name) || (ui.name.equals(ownerName) ^ 1) == 0)) {
                            nameChanged = true;
                        }
                        ui.name = ownerName;
                        if (nameChanged) {
                            UserData userData = getUserDataNoChecks(ui.id);
                            if (userData != null) {
                                userData.info = ui;
                                writeUserLP(userData);
                            }
                        }
                    }
                }
            }
        }
        return users;
    }

    public List<UserInfo> getProfiles(int userId, boolean enabledOnly) {
        List<UserInfo> list;
        boolean returnFullInfo = true;
        if (userId != UserHandle.getCallingUserId()) {
            list = "getting profiles related to user ";
            checkManageOrCreateUsersPermission(list + userId);
        } else {
            returnFullInfo = hasManageUsersPermission();
        }
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mUsersLock) {
                list = getProfilesLU(userId, enabledOnly, returnFullInfo);
            }
            return list;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public int[] getProfileIds(int userId, boolean enabledOnly) {
        if (userId != UserHandle.getCallingUserId()) {
            checkManageOrCreateUsersPermission("getting profiles related to user " + userId);
        }
        long ident = Binder.clearCallingIdentity();
        try {
            int[] toArray;
            synchronized (this.mUsersLock) {
                toArray = getProfileIdsLU(userId, enabledOnly).toArray();
            }
            return toArray;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private List<UserInfo> getProfilesLU(int userId, boolean enabledOnly, boolean fullInfo) {
        IntArray profileIds = getProfileIdsLU(userId, enabledOnly);
        ArrayList<UserInfo> users = new ArrayList(profileIds.size());
        for (int i = 0; i < profileIds.size(); i++) {
            UserInfo userInfo = ((UserData) this.mUsers.get(profileIds.get(i))).info;
            if (fullInfo) {
                userInfo = userWithName(userInfo);
            } else {
                UserInfo userInfo2 = new UserInfo(userInfo);
                userInfo2.name = null;
                userInfo2.iconPath = null;
                userInfo = userInfo2;
            }
            users.add(userInfo);
        }
        return users;
    }

    private IntArray getProfileIdsLU(int userId, boolean enabledOnly) {
        UserInfo user = getUserInfoLU(userId);
        IntArray result = new IntArray(this.mUsers.size());
        if (user == null) {
            return result;
        }
        int userSize = this.mUsers.size();
        for (int i = 0; i < userSize; i++) {
            UserInfo profile = ((UserData) this.mUsers.valueAt(i)).info;
            if (isProfileOf(user, profile) && !((enabledOnly && (profile.isEnabled() ^ 1) != 0) || this.mRemovingUserIds.get(profile.id) || profile.partial)) {
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
                    int i = profileParent.id;
                    return i;
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

    /* JADX WARNING: Missing block: B:8:0x0011, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:15:0x001d, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:21:0x0026, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isSameProfileGroupNoChecks(int userId, int otherUserId) {
        boolean z = false;
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userId);
            if (userInfo == null || userInfo.profileGroupId == -10000) {
            } else {
                UserInfo otherUserInfo = getUserInfoLU(otherUserId);
                if (otherUserInfo == null || otherUserInfo.profileGroupId == -10000) {
                } else if (userInfo.profileGroupId == otherUserInfo.profileGroupId) {
                    z = true;
                }
            }
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

    private UserInfo getProfileParentLU(int userHandle) {
        UserInfo profile = getUserInfoLU(userHandle);
        if (profile == null) {
            return null;
        }
        int parentUserId = profile.profileGroupId;
        if (parentUserId == userHandle || parentUserId == -10000) {
            return null;
        }
        return getUserInfoLU(parentUserId);
    }

    private static boolean isProfileOf(UserInfo user, UserInfo profile) {
        if (user.id == profile.id) {
            return true;
        }
        if (user.profileGroupId != -10000) {
            return user.profileGroupId == profile.profileGroupId;
        } else {
            return false;
        }
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

    public void setQuietModeEnabled(int userHandle, boolean enableQuietMode) {
        UserInfo profile;
        UserInfo parent;
        checkManageUsersPermission("silence profile");
        boolean changed = false;
        synchronized (this.mPackagesLock) {
            synchronized (this.mUsersLock) {
                profile = getUserInfoLU(userHandle);
                parent = getProfileParentLU(userHandle);
            }
            if (profile == null || (profile.isManagedProfile() ^ 1) != 0) {
                throw new IllegalArgumentException("User " + userHandle + " is not a profile");
            }
            if (profile.isQuietModeEnabled() != enableQuietMode) {
                profile.flags ^= 128;
                writeUserLP(getUserDataLU(profile.id));
                changed = true;
            }
        }
        if (changed) {
            long identity = Binder.clearCallingIdentity();
            if (enableQuietMode) {
                try {
                    ActivityManager.getService().stopUser(userHandle, true, null);
                    ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).killForegroundAppsForUser(userHandle);
                } catch (RemoteException e) {
                    Slog.e(LOG_TAG, "fail to start/stop user for quiet mode", e);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            } else {
                ActivityManager.getService().startUserInBackground(userHandle);
            }
            Binder.restoreCallingIdentity(identity);
            broadcastProfileAvailabilityChanges(profile.getUserHandle(), parent.getUserHandle(), enableQuietMode);
        }
    }

    public boolean isQuietModeEnabled(int userHandle) {
        synchronized (this.mPackagesLock) {
            UserInfo info;
            synchronized (this.mUsersLock) {
                info = getUserInfoLU(userHandle);
            }
            if (info == null || (info.isManagedProfile() ^ 1) != 0) {
                return false;
            }
            boolean isQuietModeEnabled = info.isQuietModeEnabled();
            return isQuietModeEnabled;
        }
    }

    public boolean trySetQuietModeDisabled(int userHandle, IntentSender target) {
        checkManageUsersPermission("silence profile");
        if (StorageManager.isUserKeyUnlocked(userHandle) || (this.mLockPatternUtils.isSecure(userHandle) ^ 1) != 0) {
            setQuietModeEnabled(userHandle, false);
            return true;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            Intent unlockIntent = ((KeyguardManager) this.mContext.getSystemService("keyguard")).createConfirmDeviceCredentialIntent(null, null, userHandle);
            if (unlockIntent == null) {
                return false;
            }
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
            Binder.restoreCallingIdentity(identity);
            return false;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void setUserEnabled(int userId) {
        checkManageUsersPermission("enable user");
        synchronized (this.mPackagesLock) {
            UserInfo info;
            synchronized (this.mUsersLock) {
                info = getUserInfoLU(userId);
            }
            if (!(info == null || (info.isEnabled() ^ 1) == 0)) {
                info.flags ^= 64;
                writeUserLP(getUserDataLU(info.id));
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
        int callingUserId = UserHandle.getCallingUserId();
        if (callingUserId == userId || (hasManageUsersPermission() ^ 1) == 0 || isSameProfileGroupNoChecks(callingUserId, userId)) {
            int i;
            synchronized (this.mUsersLock) {
                UserInfo userInfo = getUserInfoLU(userId);
                i = userInfo != null ? userInfo.profileBadge : 0;
            }
            return i;
        }
        throw new SecurityException("You need MANAGE_USERS permission to: check if specified user a managed profile outside your profile group");
    }

    public boolean isManagedProfile(int userId) {
        int callingUserId = UserHandle.getCallingUserId();
        if (callingUserId == userId || (hasManageUsersPermission() ^ 1) == 0 || isSameProfileGroupNoChecks(callingUserId, userId)) {
            boolean isManagedProfile;
            synchronized (this.mUsersLock) {
                UserInfo userInfo = getUserInfoLU(userId);
                isManagedProfile = userInfo != null ? userInfo.isManagedProfile() : false;
            }
            return isManagedProfile;
        }
        throw new SecurityException("You need MANAGE_USERS permission to: check if specified user a managed profile outside your profile group");
    }

    public boolean isUserUnlockingOrUnlocked(int userId) {
        checkManageOrInteractPermIfCallerInOtherProfileGroup(userId, "isUserUnlockingOrUnlocked");
        return this.mLocalService.isUserUnlockingOrUnlocked(userId);
    }

    public boolean isUserUnlocked(int userId) {
        checkManageOrInteractPermIfCallerInOtherProfileGroup(userId, "isUserUnlocked");
        return this.mLocalService.isUserUnlockingOrUnlocked(userId);
    }

    public boolean isUserRunning(int userId) {
        checkManageOrInteractPermIfCallerInOtherProfileGroup(userId, "isUserRunning");
        return this.mLocalService.isUserRunning(userId);
    }

    /* JADX WARNING: Missing block: B:6:0x0012, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void checkManageOrInteractPermIfCallerInOtherProfileGroup(int userId, String name) {
        int callingUserId = UserHandle.getCallingUserId();
        if (callingUserId != userId && !isSameProfileGroupNoChecks(callingUserId, userId) && !hasManageUsersPermission() && ActivityManager.checkComponentPermission("android.permission.INTERACT_ACROSS_USERS", Binder.getCallingUid(), -1, true) != 0) {
            throw new SecurityException("You need INTERACT_ACROSS_USERS or MANAGE_USERS permission to: check " + name);
        }
    }

    public boolean isDemoUser(int userId) {
        if (UserHandle.getCallingUserId() == userId || (hasManageUsersPermission() ^ 1) == 0) {
            boolean isDemo;
            synchronized (this.mUsersLock) {
                UserInfo userInfo = getUserInfoLU(userId);
                isDemo = userInfo != null ? userInfo.isDemo() : false;
            }
            return isDemo;
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

    /* JADX WARNING: Missing block: B:9:0x0019, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:21:0x002f, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean canHaveRestrictedProfile(int userId) {
        boolean z = false;
        checkManageUsersPermission("canHaveRestrictedProfile");
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userId);
            if (userInfo == null || (userInfo.canHaveProfile() ^ 1) != 0) {
            } else if (!userInfo.isAdmin()) {
                return false;
            } else if (!this.mIsDeviceManaged) {
                z = this.mIsUserManaged.get(userId) ^ 1;
            }
        }
    }

    private UserInfo getUserInfoLU(int userId) {
        UserInfo userInfo = null;
        UserData userData = (UserData) this.mUsers.get(userId);
        if (userData == null || !userData.info.partial || (this.mRemovingUserIds.get(userId) ^ 1) == 0) {
            if (userData != null) {
                userInfo = userData.info;
            }
            return userInfo;
        }
        Slog.w(LOG_TAG, "getUserInfo: unknown user #" + userId);
        return null;
    }

    private UserData getUserDataLU(int userId) {
        UserData userData = (UserData) this.mUsers.get(userId);
        if (userData == null || !userData.info.partial || (this.mRemovingUserIds.get(userId) ^ 1) == 0) {
            return userData;
        }
        return null;
    }

    private UserInfo getUserInfoNoChecks(int userId) {
        UserInfo userInfo = null;
        synchronized (this.mUsersLock) {
            UserData userData = (UserData) this.mUsers.get(userId);
            if (userData != null) {
                userInfo = userData.info;
            }
        }
        return userInfo;
    }

    private UserData getUserDataNoChecks(int userId) {
        UserData userData;
        synchronized (this.mUsersLock) {
            userData = (UserData) this.mUsers.get(userId);
        }
        return userData;
    }

    public boolean exists(int userId) {
        return getUserInfoNoChecks(userId) != null;
    }

    /* JADX WARNING: Missing block: B:23:0x006b, code:
            if (r0 == false) goto L_0x0070;
     */
    /* JADX WARNING: Missing block: B:24:0x006d, code:
            sendUserInfoChangedBroadcast(r7);
     */
    /* JADX WARNING: Missing block: B:25:0x0070, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setUserName(int userId, String name) {
        checkManageUsersPermission("rename users");
        boolean changed = false;
        synchronized (this.mPackagesLock) {
            UserData userData = getUserDataNoChecks(userId);
            if (userData == null || userData.info.partial) {
                Slog.w(LOG_TAG, "setUserName: unknown user #" + userId);
                return;
            }
            if (name != null) {
                if ((name.equals(userData.info.name) ^ 1) != 0) {
                    userData.info.name = name;
                    writeUserLP(userData);
                    changed = true;
                }
            }
            if (name != null && userId == 0) {
                if (this.mContext.getResources() == null || !name.equals(this.mContext.getResources().getString(17040533))) {
                    this.isOwnerNameChanged = true;
                } else {
                    this.isOwnerNameChanged = false;
                }
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

    private void sendUserInfoChangedBroadcast(int userId) {
        Intent changedIntent = new Intent("android.intent.action.USER_INFO_CHANGED");
        changedIntent.putExtra("android.intent.extra.user_handle", userId);
        changedIntent.addFlags(1073741824);
        this.mContext.sendBroadcastAsUser(changedIntent, UserHandle.ALL);
    }

    public ParcelFileDescriptor getUserIcon(int targetUserId) {
        synchronized (this.mPackagesLock) {
            UserInfo targetUserInfo = getUserInfoNoChecks(targetUserId);
            if (targetUserInfo == null || targetUserInfo.partial) {
                Slog.w(LOG_TAG, "getUserIcon: unknown user #" + targetUserId);
                return null;
            }
            int callingUserId = UserHandle.getCallingUserId();
            int callingGroupId = getUserInfoNoChecks(callingUserId).profileGroupId;
            boolean sameGroup = callingGroupId != -10000 ? callingGroupId == targetUserInfo.profileGroupId : false;
            if (!(callingUserId == targetUserId || (sameGroup ^ 1) == 0)) {
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

    /* JADX WARNING: Missing block: B:16:0x0048, code:
            if (r0 == false) goto L_0x004d;
     */
    /* JADX WARNING: Missing block: B:17:0x004a, code:
            scheduleWriteUser(r1);
     */
    /* JADX WARNING: Missing block: B:18:0x004d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void makeInitialized(int userId) {
        checkManageUsersPermission("makeInitialized");
        boolean scheduleWriteUser = false;
        synchronized (this.mUsersLock) {
            UserData userData = (UserData) this.mUsers.get(userId);
            if (userData == null || userData.info.partial) {
                Slog.w(LOG_TAG, "makeInitialized: unknown user #" + userId);
            } else if ((userData.info.flags & 16) == 0) {
                UserInfo userInfo = userData.info;
                userInfo.flags |= 16;
                scheduleWriteUser = true;
            }
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
                applyUserRestrictionsForAllUsersLR();
            } else if (localChanged) {
                applyUserRestrictionsLR(userId);
            }
        }
    }

    private boolean updateRestrictionsIfNeededLR(int userId, Bundle restrictions, SparseArray<Bundle> restrictionsArray) {
        boolean changed = UserRestrictionsUtils.areEqual((Bundle) restrictionsArray.get(userId), restrictions) ^ 1;
        if (changed) {
            if (UserRestrictionsUtils.isEmpty(restrictions)) {
                restrictionsArray.delete(userId);
            } else {
                restrictionsArray.put(userId, restrictions);
            }
        }
        return changed;
    }

    @GuardedBy("mRestrictionsLock")
    private Bundle computeEffectiveUserRestrictionsLR(int userId) {
        Bundle baseRestrictions = UserRestrictionsUtils.nonNull((Bundle) this.mBaseUserRestrictions.get(userId));
        Bundle global = UserRestrictionsUtils.mergeAll(this.mDevicePolicyGlobalUserRestrictions);
        Bundle local = (Bundle) this.mDevicePolicyLocalUserRestrictions.get(userId);
        if (UserRestrictionsUtils.isEmpty(global) && UserRestrictionsUtils.isEmpty(local)) {
            return baseRestrictions;
        }
        Bundle effective = UserRestrictionsUtils.clone(baseRestrictions);
        UserRestrictionsUtils.merge(effective, global);
        UserRestrictionsUtils.merge(effective, local);
        return effective;
    }

    @GuardedBy("mRestrictionsLock")
    private void invalidateEffectiveUserRestrictionsLR(int userId) {
        this.mCachedEffectiveUserRestrictions.remove(userId);
    }

    private Bundle getEffectiveUserRestrictions(int userId) {
        Bundle restrictions;
        synchronized (this.mRestrictionsLock) {
            restrictions = (Bundle) this.mCachedEffectiveUserRestrictions.get(userId);
            if (restrictions == null) {
                restrictions = computeEffectiveUserRestrictionsLR(userId);
                this.mCachedEffectiveUserRestrictions.put(userId, restrictions);
            }
        }
        return restrictions;
    }

    public boolean hasUserRestriction(String restrictionKey, int userId) {
        boolean z = false;
        if (!UserRestrictionsUtils.isValidRestriction(restrictionKey)) {
            return false;
        }
        Bundle restrictions = getEffectiveUserRestrictions(userId);
        if (restrictions != null) {
            z = restrictions.getBoolean(restrictionKey);
        }
        return z;
    }

    public int getUserRestrictionSource(String restrictionKey, int userId) {
        List<EnforcingUser> enforcingUsers = getUserRestrictionSources(restrictionKey, userId);
        int result = 0;
        for (int i = enforcingUsers.size() - 1; i >= 0; i--) {
            result |= ((EnforcingUser) enforcingUsers.get(i)).getUserRestrictionSource();
        }
        return result;
    }

    public List<EnforcingUser> getUserRestrictionSources(String restrictionKey, int userId) {
        checkManageUsersPermission("getUserRestrictionSource");
        if (!hasUserRestriction(restrictionKey, userId)) {
            return Collections.emptyList();
        }
        List<EnforcingUser> result = new ArrayList();
        if (hasBaseUserRestriction(restrictionKey, userId)) {
            result.add(new EnforcingUser(-10000, 1));
        }
        synchronized (this.mRestrictionsLock) {
            if (UserRestrictionsUtils.contains((Bundle) this.mDevicePolicyLocalUserRestrictions.get(userId), restrictionKey)) {
                result.add(getEnforcingUserLocked(userId));
            }
            for (int i = this.mDevicePolicyGlobalUserRestrictions.size() - 1; i >= 0; i--) {
                Bundle globalRestrictions = (Bundle) this.mDevicePolicyGlobalUserRestrictions.valueAt(i);
                int profileUserId = this.mDevicePolicyGlobalUserRestrictions.keyAt(i);
                if (UserRestrictionsUtils.contains(globalRestrictions, restrictionKey)) {
                    result.add(getEnforcingUserLocked(profileUserId));
                }
            }
        }
        return result;
    }

    private EnforcingUser getEnforcingUserLocked(int userId) {
        int source;
        if (this.mDeviceOwnerUserId == userId) {
            source = 2;
        } else {
            source = 4;
        }
        return new EnforcingUser(userId, source);
    }

    public Bundle getUserRestrictions(int userId) {
        return UserRestrictionsUtils.clone(getEffectiveUserRestrictions(userId));
    }

    public boolean hasBaseUserRestriction(String restrictionKey, int userId) {
        boolean z = false;
        checkManageUsersPermission("hasBaseUserRestriction");
        if (!UserRestrictionsUtils.isValidRestriction(restrictionKey)) {
            return false;
        }
        synchronized (this.mRestrictionsLock) {
            Bundle bundle = (Bundle) this.mBaseUserRestrictions.get(userId);
            if (bundle != null) {
                z = bundle.getBoolean(restrictionKey, false);
            }
        }
        return z;
    }

    public void setUserRestriction(String key, boolean value, int userId) {
        checkManageUsersPermission("setUserRestriction");
        if (UserRestrictionsUtils.isValidRestriction(key)) {
            synchronized (this.mRestrictionsLock) {
                Bundle newRestrictions = UserRestrictionsUtils.clone((Bundle) this.mBaseUserRestrictions.get(userId));
                newRestrictions.putBoolean(key, value);
                updateUserRestrictionsInternalLR(newRestrictions, userId);
            }
        }
    }

    @GuardedBy("mRestrictionsLock")
    private void updateUserRestrictionsInternalLR(Bundle newBaseRestrictions, int userId) {
        boolean z = true;
        Bundle prevAppliedRestrictions = UserRestrictionsUtils.nonNull((Bundle) this.mAppliedUserRestrictions.get(userId));
        if (newBaseRestrictions != null) {
            boolean z2;
            if (((Bundle) this.mBaseUserRestrictions.get(userId)) != newBaseRestrictions) {
                z2 = true;
            } else {
                z2 = false;
            }
            Preconditions.checkState(z2);
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
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
        propagateUserRestrictionsLR(userId, effective, prevAppliedRestrictions);
        this.mAppliedUserRestrictions.put(userId, new Bundle(effective));
    }

    private void propagateUserRestrictionsLR(final int userId, Bundle newRestrictions, Bundle prevRestrictions) {
        if (!UserRestrictionsUtils.areEqual(newRestrictions, prevRestrictions)) {
            final Bundle newRestrictionsFinal = new Bundle(newRestrictions);
            final Bundle prevRestrictionsFinal = new Bundle(prevRestrictions);
            this.mHandler.post(new Runnable() {
                public void run() {
                    UserRestrictionsListener[] listeners;
                    UserRestrictionsUtils.applyUserRestrictions(UserManagerService.this.mContext, userId, newRestrictionsFinal, prevRestrictionsFinal);
                    synchronized (UserManagerService.this.mUserRestrictionsListeners) {
                        listeners = new UserRestrictionsListener[UserManagerService.this.mUserRestrictionsListeners.size()];
                        UserManagerService.this.mUserRestrictionsListeners.toArray(listeners);
                    }
                    for (UserRestrictionsListener onUserRestrictionsChanged : listeners) {
                        onUserRestrictionsChanged.onUserRestrictionsChanged(userId, newRestrictionsFinal, prevRestrictionsFinal);
                    }
                    UserManagerService.this.mContext.sendBroadcastAsUser(new Intent("android.os.action.USER_RESTRICTIONS_CHANGED").setFlags(1073741824), UserHandle.of(userId));
                }
            });
        }
    }

    void applyUserRestrictionsLR(int userId) {
        updateUserRestrictionsInternalLR(null, userId);
    }

    @GuardedBy("mRestrictionsLock")
    void applyUserRestrictionsForAllUsersLR() {
        this.mCachedEffectiveUserRestrictions.clear();
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    int[] runningUsers = ActivityManager.getService().getRunningUserIds();
                    synchronized (UserManagerService.this.mRestrictionsLock) {
                        for (int applyUserRestrictionsLR : runningUsers) {
                            UserManagerService.this.applyUserRestrictionsLR(applyUserRestrictionsLR);
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

    /* JADX WARNING: Missing block: B:29:0x0065, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:37:0x0076, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean canAddMoreManagedProfiles(int userId, boolean allowedToRemoveOne) {
        checkManageUsersPermission("check if more managed profiles can be added.");
        if (ActivityManager.isLowRamDeviceStatic() || !this.mContext.getPackageManager().hasSystemFeature("android.software.managed_users")) {
            return false;
        }
        List<UserInfo> profiles = getProfiles(userId, false);
        Iterator<UserInfo> iterator = profiles.iterator();
        while (iterator.hasNext()) {
            if (((UserInfo) iterator.next()).isClonedProfile()) {
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
            if (userInfo == null || (userInfo.canHaveProfile() ^ 1) != 0) {
            } else {
                int usersCountAfterRemoving = getAliveUsersExcludingGuestsCountLU() - profilesRemovedCount;
                boolean z = usersCountAfterRemoving != 1 ? usersCountAfterRemoving < UserManager.getMaxSupportedUsers() : true;
            }
        }
    }

    private int getAliveUsersExcludingGuestsCountLU() {
        int aliveUserCount = 0;
        int totalUserCount = this.mUsers.size();
        for (int i = 0; i < totalUserCount; i++) {
            UserInfo user = ((UserData) this.mUsers.valueAt(i)).info;
            if (!(this.mRemovingUserIds.get(user.id) || (user.isGuest() ^ 1) == 0 || (user.isClonedProfile() ^ 1) == 0)) {
                aliveUserCount++;
            }
        }
        return aliveUserCount;
    }

    private static final void checkManageUserAndAcrossUsersFullPermission(String message) {
        int uid = Binder.getCallingUid();
        if (uid != 1000 && uid != 0 && ActivityManager.checkComponentPermission("android.permission.MANAGE_USERS", uid, -1, true) != 0 && ActivityManager.checkComponentPermission("android.permission.INTERACT_ACROSS_USERS_FULL", uid, -1, true) != 0) {
            throw new SecurityException("You need MANAGE_USERS and INTERACT_ACROSS_USERS_FULL permission to: " + message);
        }
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
        if (UserHandle.isSameApp(callingUid, 1000) || callingUid == 0 || ActivityManager.checkComponentPermission("android.permission.MANAGE_USERS", callingUid, -1, true) == 0) {
            return true;
        }
        return false;
    }

    private static final boolean hasManageOrCreateUsersPermission() {
        int callingUid = Binder.getCallingUid();
        if (UserHandle.isSameApp(callingUid, 1000) || callingUid == 0 || ActivityManager.checkComponentPermission("android.permission.MANAGE_USERS", callingUid, -1, true) == 0 || ActivityManager.checkComponentPermission("android.permission.CREATE_USERS", callingUid, -1, true) == 0) {
            return true;
        }
        return false;
    }

    private static void checkSystemOrRoot(String message) {
        int uid = Binder.getCallingUid();
        if (!UserHandle.isSameApp(uid, 1000) && uid != 0) {
            throw new SecurityException("Only system may: " + message);
        }
    }

    private void writeBitmapLP(UserInfo info, Bitmap bitmap) {
        try {
            File dir = new File(this.mUsersDir, Integer.toString(info.id));
            File file = new File(dir, USER_PHOTO_FILENAME);
            File tmp = new File(dir, USER_PHOTO_FILENAME_TMP);
            if (!dir.exists()) {
                dir.mkdir();
                FileUtils.setPermissions(dir.getPath(), 505, -1, -1);
            }
            CompressFormat compressFormat = CompressFormat.PNG;
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

    /* JADX WARNING: Removed duplicated region for block: B:46:0x00be A:{Splitter: B:5:0x0014, ExcHandler: java.io.IOException (e java.io.IOException), PHI: r1 } */
    /* JADX WARNING: Missing block: B:48:?, code:
            fallbackToSingleUserLP();
     */
    /* JADX WARNING: Missing block: B:49:0x00c2, code:
            libcore.io.IoUtils.closeQuietly(r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readUserListLP() {
        if (this.mUserListFile.exists()) {
            AutoCloseable fis = null;
            try {
                int type;
                fis = new AtomicFile(this.mUserListFile).openRead();
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fis, StandardCharsets.UTF_8.name());
                do {
                    type = parser.next();
                    if (type == 2) {
                        break;
                    }
                } while (type != 1);
                if (type != 2) {
                    Slog.e(LOG_TAG, "Unable to read user list");
                    fallbackToSingleUserLP();
                    IoUtils.closeQuietly(fis);
                    return;
                }
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
                    type = parser.next();
                    if (type == 1) {
                        updateUserIds();
                        upgradeIfNecessaryLP(oldDevicePolicyGlobalUserRestrictions);
                        IoUtils.closeQuietly(fis);
                        break;
                    } else if (type == 2) {
                        String name = parser.getName();
                        if (name.equals(TAG_USER)) {
                            UserData userData = readUserLP(Integer.parseInt(parser.getAttributeValue(null, ATTR_ID)));
                            if (userData != null) {
                                synchronized (this.mUsersLock) {
                                    this.mUsers.put(userData.info.id, userData);
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
                            do {
                                type = parser.next();
                                if (type == 1 || type == 3) {
                                    break;
                                }
                            } while (type != 2);
                            if (parser.getName().equals(TAG_RESTRICTIONS)) {
                                synchronized (this.mGuestRestrictions) {
                                    UserRestrictionsUtils.readRestrictions(parser, this.mGuestRestrictions);
                                }
                            } else {
                                continue;
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
            } catch (IOException e) {
            } catch (Exception e2) {
                Slog.e(LOG_TAG, "Unable to read user list, error: " + e2);
                fallbackToSingleUserLP();
                IoUtils.closeQuietly(fis);
            } catch (Throwable th) {
                IoUtils.closeQuietly(fis);
            }
        } else {
            fallbackToSingleUserLP();
        }
    }

    private void upgradeIfNecessaryLP(Bundle oldGlobalUserRestrictions) {
        UserData userData;
        int originalVersion = this.mUserVersion;
        int userVersion = this.mUserVersion;
        if (userVersion < 1) {
            userData = getUserDataNoChecks(0);
            if ("Primary".equals(userData.info.name)) {
                userData.info.name = this.mContext.getResources().getString(17040533);
                scheduleWriteUser(userData);
            }
            userVersion = 1;
        }
        if (userVersion < 2) {
            userData = getUserDataNoChecks(0);
            if ((userData.info.flags & 16) == 0) {
                UserInfo userInfo = userData.info;
                userInfo.flags |= 16;
                scheduleWriteUser(userData);
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
                    userData = (UserData) this.mUsers.valueAt(i);
                    if (!splitSystemUser && userData.info.isRestricted() && userData.info.restrictedProfileParentId == -10000) {
                        userData.info.restrictedProfileParentId = 0;
                        scheduleWriteUser(userData);
                    }
                }
            }
            userVersion = 6;
        }
        if (userVersion < 7) {
            synchronized (this.mRestrictionsLock) {
                if (!(UserRestrictionsUtils.isEmpty(oldGlobalUserRestrictions) || this.mDeviceOwnerUserId == -10000)) {
                    this.mDevicePolicyGlobalUserRestrictions.put(this.mDeviceOwnerUserId, oldGlobalUserRestrictions);
                }
                UserRestrictionsUtils.moveRestriction("ensure_verify_apps", this.mDevicePolicyLocalUserRestrictions, this.mDevicePolicyGlobalUserRestrictions);
            }
            userVersion = 7;
        }
        if (userVersion < 7) {
            Slog.w(LOG_TAG, "User version " + this.mUserVersion + " didn't upgrade as expected to " + 7);
            return;
        }
        this.mUserVersion = userVersion;
        if (originalVersion < this.mUserVersion) {
            writeUserListLP();
        }
    }

    private void fallbackToSingleUserLP() {
        int i = 0;
        int flags = 16;
        if (!UserManager.isSplitSystemUser()) {
            flags = 19;
        }
        UserData userData = putUserInfo(new UserInfo(0, null, null, flags));
        this.mNextSerialNumber = 10;
        this.mUserVersion = 7;
        Bundle restrictions = new Bundle();
        try {
            String[] defaultFirstUserRestrictions = this.mContext.getResources().getStringArray(17235999);
            int length = defaultFirstUserRestrictions.length;
            while (i < length) {
                String userRestriction = defaultFirstUserRestrictions[i];
                if (UserRestrictionsUtils.isValidRestriction(userRestriction)) {
                    restrictions.putBoolean(userRestriction, true);
                }
                i++;
            }
        } catch (NotFoundException e) {
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
        return this.mContext.getResources().getString(17040533);
    }

    private void scheduleWriteUser(UserData UserData) {
        if (!this.mHandler.hasMessages(1, UserData)) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, UserData), 2000);
        }
    }

    private void writeUserLP(UserData userData) {
        FileOutputStream fileOutputStream = null;
        AtomicFile userFile = new AtomicFile(new File(this.mUsersDir, userData.info.id + XML_SUFFIX));
        try {
            fileOutputStream = userFile.startWrite();
            writeUserLP(userData, new BufferedOutputStream(fileOutputStream));
            userFile.finishWrite(fileOutputStream);
        } catch (Exception ioe) {
            Slog.e(LOG_TAG, "Error writing user info " + userData.info.id, ioe);
            userFile.failWrite(fileOutputStream);
        }
    }

    void writeUserLP(UserData userData, OutputStream os) throws IOException, XmlPullParserException {
        XmlSerializer serializer = new FastXmlSerializer();
        serializer.setOutput(os, StandardCharsets.UTF_8.name());
        serializer.startDocument(null, Boolean.valueOf(true));
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
            serializer.startTag(null, TAG_NAME);
            serializer.text(userInfo.name);
            serializer.endTag(null, TAG_NAME);
        }
        synchronized (this.mRestrictionsLock) {
            UserRestrictionsUtils.writeRestrictions(serializer, (Bundle) this.mBaseUserRestrictions.get(userInfo.id), TAG_RESTRICTIONS);
            UserRestrictionsUtils.writeRestrictions(serializer, (Bundle) this.mDevicePolicyLocalUserRestrictions.get(userInfo.id), TAG_DEVICE_POLICY_RESTRICTIONS);
            UserRestrictionsUtils.writeRestrictions(serializer, (Bundle) this.mDevicePolicyGlobalUserRestrictions.get(userInfo.id), TAG_DEVICE_POLICY_GLOBAL_RESTRICTIONS);
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
        serializer.endTag(null, TAG_USER);
        serializer.endDocument();
    }

    private void writeUserListLP() {
        FileOutputStream fos = null;
        AtomicFile userListFile = new AtomicFile(this.mUserListFile);
        try {
            fos = userListFile.startWrite();
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(bos, StandardCharsets.UTF_8.name());
            serializer.startDocument(null, Boolean.valueOf(true));
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
                int[] userIdsToWrite = new int[this.mUsers.size()];
                for (int i = 0; i < userIdsToWrite.length; i++) {
                    userIdsToWrite[i] = ((UserData) this.mUsers.valueAt(i)).info.id;
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
            userListFile.failWrite(fos);
            Slog.e(LOG_TAG, "Error writing user list");
        }
    }

    private UserData readUserLP(int id) {
        AutoCloseable autoCloseable = null;
        UserData readUserLP;
        try {
            autoCloseable = new AtomicFile(new File(this.mUsersDir, Integer.toString(id) + XML_SUFFIX)).openRead();
            readUserLP = readUserLP(id, autoCloseable);
            return readUserLP;
        } catch (IOException e) {
            readUserLP = LOG_TAG;
            Slog.e(readUserLP, "Error reading user list");
            return null;
        } catch (XmlPullParserException e2) {
            readUserLP = LOG_TAG;
            Slog.e(readUserLP, "Error reading user list");
            return null;
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    UserData readUserLP(int id, InputStream is) throws IOException, XmlPullParserException {
        int type;
        int flags = 0;
        int serialNumber = id;
        String name = null;
        String account = null;
        String iconPath = null;
        long creationTime = 0;
        long lastLoggedInTime = 0;
        String lastLoggedInFingerprint = null;
        int profileGroupId = -10000;
        int profileBadge = 0;
        int restrictedProfileParentId = -10000;
        boolean partial = false;
        boolean guestToRemove = false;
        boolean persistSeedData = false;
        String seedAccountName = null;
        String seedAccountType = null;
        PersistableBundle seedAccountOptions = null;
        Object baseRestrictions = null;
        Object localRestrictions = null;
        Object globalRestrictions = null;
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(is, StandardCharsets.UTF_8.name());
        do {
            type = parser.next();
            if (type == 2) {
                break;
            }
        } while (type != 1);
        if (type != 2) {
            Slog.e(LOG_TAG, "Unable to read user " + id);
            return null;
        }
        if (type == 2 && parser.getName().equals(TAG_USER)) {
            if (readIntAttribute(parser, ATTR_ID, -1) == id) {
                serialNumber = readIntAttribute(parser, ATTR_SERIAL_NO, id);
                flags = readIntAttribute(parser, ATTR_FLAGS, 0);
                iconPath = parser.getAttributeValue(null, ATTR_ICON_PATH);
                creationTime = readLongAttribute(parser, ATTR_CREATION_TIME, 0);
                lastLoggedInTime = readLongAttribute(parser, ATTR_LAST_LOGGED_IN_TIME, 0);
                lastLoggedInFingerprint = parser.getAttributeValue(null, ATTR_LAST_LOGGED_IN_FINGERPRINT);
                profileGroupId = readIntAttribute(parser, ATTR_PROFILE_GROUP_ID, -10000);
                profileBadge = readIntAttribute(parser, ATTR_PROFILE_BADGE, 0);
                restrictedProfileParentId = readIntAttribute(parser, ATTR_RESTRICTED_PROFILE_PARENT_ID, -10000);
                if ("true".equals(parser.getAttributeValue(null, ATTR_PARTIAL))) {
                    partial = true;
                }
                if ("true".equals(parser.getAttributeValue(null, ATTR_GUEST_TO_REMOVE))) {
                    guestToRemove = true;
                }
                seedAccountName = parser.getAttributeValue(null, ATTR_SEED_ACCOUNT_NAME);
                seedAccountType = parser.getAttributeValue(null, ATTR_SEED_ACCOUNT_TYPE);
                if (!(seedAccountName == null && seedAccountType == null)) {
                    persistSeedData = true;
                }
                int outerDepth = parser.getDepth();
                while (true) {
                    type = parser.next();
                    if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                        break;
                    } else if (!(type == 3 || type == 4)) {
                        String tag = parser.getName();
                        if (TAG_NAME.equals(tag)) {
                            if (parser.next() == 4) {
                                name = parser.getText();
                            }
                        } else if (TAG_RESTRICTIONS.equals(tag)) {
                            baseRestrictions = UserRestrictionsUtils.readRestrictions(parser);
                        } else if (TAG_DEVICE_POLICY_RESTRICTIONS.equals(tag)) {
                            localRestrictions = UserRestrictionsUtils.readRestrictions(parser);
                        } else if (TAG_DEVICE_POLICY_GLOBAL_RESTRICTIONS.equals(tag)) {
                            globalRestrictions = UserRestrictionsUtils.readRestrictions(parser);
                        } else if (TAG_ACCOUNT.equals(tag)) {
                            if (parser.next() == 4) {
                                account = parser.getText();
                            }
                        } else if (TAG_SEED_ACCOUNT_OPTIONS.equals(tag)) {
                            seedAccountOptions = PersistableBundle.restoreFromXml(parser);
                            persistSeedData = true;
                        }
                    }
                }
            } else {
                Slog.e(LOG_TAG, "User id does not match the file name");
                return null;
            }
        }
        UserInfo userInfo = new UserInfo(id, name, iconPath, flags);
        userInfo.serialNumber = serialNumber;
        userInfo.creationTime = creationTime;
        userInfo.lastLoggedInTime = lastLoggedInTime;
        userInfo.lastLoggedInFingerprint = lastLoggedInFingerprint;
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
        synchronized (this.mRestrictionsLock) {
            if (baseRestrictions != null) {
                this.mBaseUserRestrictions.put(id, baseRestrictions);
            }
            if (localRestrictions != null) {
                this.mDevicePolicyLocalUserRestrictions.put(id, localRestrictions);
            }
            if (globalRestrictions != null) {
                this.mDevicePolicyGlobalUserRestrictions.put(id, globalRestrictions);
            }
        }
        return userData;
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

    private void cleanAppRestrictionsForPackage(String pkg, int userId) {
        synchronized (this.mPackagesLock) {
            File resFile = new File(Environment.getUserSystemDirectory(userId), packageToRestrictionsFileName(pkg));
            if (resFile.exists()) {
                resFile.delete();
            }
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

    /* JADX WARNING: Removed duplicated region for block: B:218:0x0472  */
    /* JADX WARNING: Missing block: B:163:0x024d, code:
            if (r22.info.isEphemeral() == false) goto L_0x0255;
     */
    /* JADX WARNING: Missing block: B:185:0x0326, code:
            ((android.os.storage.StorageManager) r34.mContext.getSystemService(android.os.storage.StorageManager.class)).createUserKey(r26, r0.serialNumber, r0.isEphemeral());
            r34.mUserDataPreparer.prepareUserData(r26, r0.serialNumber, 3);
            r34.mPm.createNewUser(r26, r38);
            r0.partial = false;
            r28 = r34.mPackagesLock;
     */
    /* JADX WARNING: Missing block: B:186:0x037f, code:
            monitor-enter(r28);
     */
    /* JADX WARNING: Missing block: B:188:?, code:
            writeUserLP(r25);
     */
    /* JADX WARNING: Missing block: B:190:?, code:
            monitor-exit(r28);
     */
    /* JADX WARNING: Missing block: B:191:0x0388, code:
            updateUserIds();
            r23 = new android.os.Bundle();
     */
    /* JADX WARNING: Missing block: B:192:0x0390, code:
            if (r15 == false) goto L_0x03a7;
     */
    /* JADX WARNING: Missing block: B:193:0x0392, code:
            r29 = r34.mGuestRestrictions;
     */
    /* JADX WARNING: Missing block: B:194:0x0398, code:
            monitor-enter(r29);
     */
    /* JADX WARNING: Missing block: B:196:?, code:
            r23.putAll(r34.mGuestRestrictions);
     */
    /* JADX WARNING: Missing block: B:198:?, code:
            monitor-exit(r29);
     */
    /* JADX WARNING: Missing block: B:199:0x03a7, code:
            r29 = r34.mRestrictionsLock;
     */
    /* JADX WARNING: Missing block: B:200:0x03ad, code:
            monitor-enter(r29);
     */
    /* JADX WARNING: Missing block: B:202:?, code:
            r34.mBaseUserRestrictions.append(r26, r23);
     */
    /* JADX WARNING: Missing block: B:204:?, code:
            monitor-exit(r29);
     */
    /* JADX WARNING: Missing block: B:205:0x03be, code:
            r34.mPm.onNewUserCreated(r26);
            android.hwtheme.HwThemeManager.applyDefaultHwTheme(false, r34.mContext, r26);
            r6 = new android.content.Intent("android.intent.action.USER_ADDED");
            r6.putExtra("android.intent.extra.user_handle", r26);
            r34.mContext.sendBroadcastAsUser(r6, android.os.UserHandle.ALL, "android.permission.MANAGE_USERS");
            r29 = r34.mContext;
     */
    /* JADX WARNING: Missing block: B:206:0x040a, code:
            if (r15 == false) goto L_0x04cb;
     */
    /* JADX WARNING: Missing block: B:207:0x040c, code:
            r28 = TRON_GUEST_CREATED;
     */
    /* JADX WARNING: Missing block: B:208:0x040f, code:
            com.android.internal.logging.MetricsLogger.count(r29, r28, 1);
            android.provider.Settings.Secure.putStringForUser(r34.mContext.getContentResolver(), "display_density_forced", java.lang.Integer.toString(android.os.SystemProperties.getInt("persist.sys.realdpi", android.os.SystemProperties.getInt("persist.sys.dpi", android.os.SystemProperties.getInt("ro.sf.real_lcd_density", android.os.SystemProperties.getInt("ro.sf.lcd_density", 0))))), r26);
     */
    /* JADX WARNING: Missing block: B:209:0x045a, code:
            if (r17 == false) goto L_0x0465;
     */
    /* JADX WARNING: Missing block: B:210:0x045c, code:
            android.os.SystemProperties.set("persist.sys.RepairMode", "true");
     */
    /* JADX WARNING: Missing block: B:211:0x0465, code:
            android.os.Binder.restoreCallingIdentity(r12);
     */
    /* JADX WARNING: Missing block: B:212:0x0468, code:
            return r0;
     */
    /* JADX WARNING: Missing block: B:237:0x04cb, code:
            r28 = TRON_USER_CREATED;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private UserInfo createUserInternalUnchecked(String name, int flags, int parentId, String[] disallowedPackages) {
        if (((DeviceStorageMonitorInternal) LocalServices.getService(DeviceStorageMonitorInternal.class)).isMemoryLow()) {
            Log.w(LOG_TAG, "Cannot add user. Not enough space on disk.");
            return null;
        } else if (ActivityManager.isLowRamDeviceStatic()) {
            return null;
        } else {
            boolean isGuest = (flags & 4) != 0;
            boolean isManagedProfile = (flags & 32) != 0;
            boolean isRestricted = (flags & 8) != 0;
            boolean isDemo = (flags & 512) != 0;
            boolean isRepairMode = (134217728 & flags) != 0;
            long ident = Binder.clearCallingIdentity();
            boolean isClonedProfile = (67108864 & flags) != 0;
            Flog.i(900, "Create user internal, flags= " + Integer.toHexString(flags) + " parentId= " + parentId + " isGuest= " + isGuest + " isManagedProfile= " + isManagedProfile);
            try {
                synchronized (this.mPackagesLock) {
                    UserData userData = null;
                    if (parentId != -10000) {
                        synchronized (this.mUsersLock) {
                            userData = getUserDataLU(parentId);
                        }
                        if (userData == null) {
                            Binder.restoreCallingIdentity(ident);
                            return null;
                        }
                    }
                    if (isManagedProfile) {
                        if ((canAddMoreManagedProfiles(parentId, false) ^ 1) != 0) {
                            Log.e(LOG_TAG, "Cannot add more managed profiles for user " + parentId);
                            Binder.restoreCallingIdentity(ident);
                            return null;
                        }
                    }
                    if (!(isRepairMode || (isGuest ^ 1) == 0 || (isManagedProfile ^ 1) == 0 || (isDemo ^ 1) == 0)) {
                        if (isUserLimitReached() && (isClonedProfile ^ 1) != 0) {
                            Binder.restoreCallingIdentity(ident);
                            return null;
                        }
                    }
                    if (isGuest) {
                        if (findCurrentGuestUser() != null) {
                            Binder.restoreCallingIdentity(ident);
                            return null;
                        }
                    }
                    if (isRestricted) {
                        if (!((UserManager.isSplitSystemUser() ^ 1) == 0 || parentId == 0)) {
                            Log.w(LOG_TAG, "Cannot add restricted profile - parent user must be owner");
                            Binder.restoreCallingIdentity(ident);
                            return null;
                        }
                    }
                    if (isRestricted) {
                        if (UserManager.isSplitSystemUser()) {
                            if (userData == null) {
                                Log.w(LOG_TAG, "Cannot add restricted profile - parent user must be specified");
                                Binder.restoreCallingIdentity(ident);
                                return null;
                            } else if (!userData.info.canHaveProfile()) {
                                Log.w(LOG_TAG, "Cannot add restricted profile - profiles cannot be created for the specified parent user id " + parentId);
                                Binder.restoreCallingIdentity(ident);
                                return null;
                            }
                        }
                    }
                    if (UserManager.isSplitSystemUser() || (flags & 256) == 0 || (flags & 512) != 0) {
                        int userId;
                        UserInfo userInfo;
                        UserData userData2;
                        if (UserManager.isSplitSystemUser() && (isGuest ^ 1) != 0 && (isManagedProfile ^ 1) != 0 && getPrimaryUser() == null) {
                            flags |= 1;
                            synchronized (this.mUsersLock) {
                                if (!this.mIsDeviceManaged) {
                                    flags |= 2;
                                }
                            }
                        }
                        if (isRepairMode) {
                            userId = REPAIR_MODE_USER_ID;
                        } else {
                            userId = getNextAvailableId();
                        }
                        Environment.getUserSystemDirectory(userId).mkdirs();
                        boolean ephemeralGuests = Resources.getSystem().getBoolean(17956969);
                        synchronized (this.mUsersLock) {
                            int i;
                            long now;
                            if (!(isGuest && ephemeralGuests)) {
                                if (!this.mForceEphemeralUsers) {
                                    if (userData != null) {
                                    }
                                    userInfo = new UserInfo(userId, name, null, flags);
                                    i = this.mNextSerialNumber;
                                    this.mNextSerialNumber = i + 1;
                                    userInfo.serialNumber = i;
                                    now = System.currentTimeMillis();
                                    if (now <= EPOCH_PLUS_30_YEARS) {
                                        now = 0;
                                    }
                                    userInfo.creationTime = now;
                                    userInfo.partial = true;
                                    userInfo.lastLoggedInFingerprint = Build.FINGERPRINT;
                                    if (isManagedProfile && parentId != -10000) {
                                        userInfo.profileBadge = getFreeProfileBadgeLU(parentId);
                                    }
                                    userData2 = new UserData();
                                    userData2.info = userInfo;
                                    this.mUsers.put(userId, userData2);
                                }
                            }
                            flags |= 256;
                            userInfo = new UserInfo(userId, name, null, flags);
                            i = this.mNextSerialNumber;
                            this.mNextSerialNumber = i + 1;
                            userInfo.serialNumber = i;
                            now = System.currentTimeMillis();
                            if (now <= EPOCH_PLUS_30_YEARS) {
                            }
                            userInfo.creationTime = now;
                            userInfo.partial = true;
                            userInfo.lastLoggedInFingerprint = Build.FINGERPRINT;
                            userInfo.profileBadge = getFreeProfileBadgeLU(parentId);
                            userData2 = new UserData();
                            userData2.info = userInfo;
                            this.mUsers.put(userId, userData2);
                        }
                        writeUserLP(userData2);
                        writeUserListLP();
                        if (userData != null) {
                            if (isManagedProfile || isClonedProfile) {
                                if (userData.info.profileGroupId == -10000) {
                                    userData.info.profileGroupId = userData.info.id;
                                    writeUserLP(userData);
                                }
                                userInfo.profileGroupId = userData.info.profileGroupId;
                            } else if (isRestricted) {
                                if (userData.info.restrictedProfileParentId == -10000) {
                                    userData.info.restrictedProfileParentId = userData.info.id;
                                    writeUserLP(userData);
                                }
                                userInfo.restrictedProfileParentId = userData.info.restrictedProfileParentId;
                            }
                        }
                    } else {
                        Log.e(LOG_TAG, "Ephemeral users are supported on split-system-user systems only.");
                        Binder.restoreCallingIdentity(ident);
                        return null;
                    }
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    UserData putUserInfo(UserInfo userInfo) {
        UserData userData = new UserData();
        userData.info = userInfo;
        synchronized (this.mUsers) {
            this.mUsers.put(userInfo.id, userData);
        }
        return userData;
    }

    void removeUserInfo(int userId) {
        synchronized (this.mUsers) {
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
            Secure.putIntForUser(this.mContext.getContentResolver(), "location_mode", 0, user.id);
            setUserRestriction("no_share_location", true, user.id);
            return user;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private UserInfo findCurrentGuestUser() {
        synchronized (this.mUsersLock) {
            int size = this.mUsers.size();
            int i = 0;
            while (i < size) {
                UserInfo user = ((UserData) this.mUsers.valueAt(i)).info;
                if (!user.isGuest() || (user.guestToRemove ^ 1) == 0 || (this.mRemovingUserIds.get(user.id) ^ 1) == 0) {
                    i++;
                } else {
                    return user;
                }
            }
            return null;
        }
    }

    /* JADX WARNING: Missing block: B:26:0x004e, code:
            if (r2.info.isGuest() != false) goto L_0x0060;
     */
    /* JADX WARNING: Missing block: B:29:0x0051, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
    /* JADX WARNING: Missing block: B:30:0x0054, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:42:?, code:
            r2.info.guestToRemove = true;
            r3 = r2.info;
            r3.flags |= 64;
            writeUserLP(r2);
     */
    /* JADX WARNING: Missing block: B:45:0x0071, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
    /* JADX WARNING: Missing block: B:46:0x0074, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean markGuestForDeletion(int userHandle) {
        checkManageUsersPermission("Only the system can remove users");
        if (getUserRestrictions(UserHandle.getCallingUserId()).getBoolean("no_remove_user", false)) {
            Log.w(LOG_TAG, "Cannot remove user. DISALLOW_REMOVE_USER is enabled.");
            return false;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mPackagesLock) {
                synchronized (this.mUsersLock) {
                    UserData userData = (UserData) this.mUsers.get(userHandle);
                    if (!(userHandle == 0 || userData == null)) {
                        if (!this.mRemovingUserIds.get(userHandle)) {
                        }
                    }
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        return false;
    }

    public boolean removeUser(int userHandle) {
        boolean isManagedProfile;
        Slog.i(LOG_TAG, "removeUser u" + userHandle);
        checkManageOrCreateUsersPermission("Only the system can remove users");
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userHandle);
            isManagedProfile = userInfo != null ? userInfo.isManagedProfile() : false;
        }
        String restriction = isManagedProfile ? "no_remove_managed_profile" : "no_remove_user";
        if (!getUserRestrictions(UserHandle.getCallingUserId()).getBoolean(restriction, false)) {
            return removeUserUnchecked(userHandle);
        }
        Log.w(LOG_TAG, "Cannot remove user. " + restriction + " is enabled.");
        return false;
    }

    /* JADX WARNING: Missing block: B:30:?, code:
            r11.mAppOpsService.removeUser(r12);
     */
    /* JADX WARNING: Missing block: B:61:0x00d9, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:63:?, code:
            android.util.Log.w(LOG_TAG, "Unable to notify AppOpsService of removing user", r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean removeUserUnchecked(int userHandle) {
        boolean z = true;
        long ident = Binder.clearCallingIdentity();
        try {
            UserData userData;
            if (ActivityManager.getCurrentUser() == userHandle) {
                Log.w(LOG_TAG, "Current user cannot be removed");
                return false;
            }
            synchronized (this.mPackagesLock) {
                synchronized (this.mUsersLock) {
                    userData = (UserData) this.mUsers.get(userHandle);
                    if (!(userHandle == 0 || userData == null)) {
                        if (!this.mRemovingUserIds.get(userHandle)) {
                            addRemovingUserIdLocked(userHandle);
                        }
                    }
                    Flog.i(900, "Removing user stopped, userHandle " + userHandle + " user " + userData);
                    Binder.restoreCallingIdentity(ident);
                    return false;
                }
            }
            userData.info.partial = true;
            UserInfo userInfo = userData.info;
            userInfo.flags |= 64;
            writeUserLP(userData);
            if (userData.info.profileGroupId != -10000 && userData.info.isManagedProfile()) {
                sendProfileRemovedBroadcast(userData.info.profileGroupId, userData.info.id);
            }
            Flog.i(900, "Stopping user " + userHandle);
            try {
                int res = ActivityManager.getService().stopUser(userHandle, true, new IStopUserCallback.Stub() {
                    public void userStopped(int userId) {
                        UserManagerService.this.finishRemoveUser(userId);
                    }

                    public void userStopAborted(int userId) {
                    }
                });
                if (res == 0 && userHandle == REPAIR_MODE_USER_ID) {
                    SystemProperties.set("persist.sys.RepairMode", "false");
                }
                if (res != 0) {
                    z = false;
                }
                Binder.restoreCallingIdentity(ident);
                return z;
            } catch (RemoteException e) {
                Binder.restoreCallingIdentity(ident);
                return false;
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    void addRemovingUserIdLocked(int userId) {
        this.mRemovingUserIds.put(userId, true);
        this.mRecentlyRemovedIds.add(Integer.valueOf(userId));
        if (this.mRecentlyRemovedIds.size() > 100) {
            this.mRecentlyRemovedIds.removeFirst();
        }
    }

    void finishRemoveUser(final int userHandle) {
        Flog.i(900, "finishRemoveUser " + userHandle);
        long ident = Binder.clearCallingIdentity();
        try {
            Intent addedIntent = new Intent("android.intent.action.USER_REMOVED");
            addedIntent.putExtra("android.intent.extra.user_handle", userHandle);
            this.mContext.sendOrderedBroadcastAsUser(addedIntent, UserHandle.ALL, "android.permission.MANAGE_USERS", new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    Flog.i(900, "USER_REMOVED broadcast sent, cleaning up user data " + userHandle);
                    final int i = userHandle;
                    new Thread() {
                        public void run() {
                            ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).onUserRemoved(i);
                            UserManagerService.this.removeUserState(i);
                        }
                    }.start();
                }
            }, null, -1, null, null);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void removeUserState(int userHandle) {
        try {
            ((StorageManager) this.mContext.getSystemService(StorageManager.class)).destroyUserKey(userHandle);
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
        new AtomicFile(new File(this.mUsersDir, userHandle + XML_SUFFIX)).delete();
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
        return getApplicationRestrictionsForUser(packageName, UserHandle.getCallingUserId());
    }

    public Bundle getApplicationRestrictionsForUser(String packageName, int userId) {
        Bundle readApplicationRestrictionsLP;
        if (!(UserHandle.getCallingUserId() == userId && (UserHandle.isSameApp(Binder.getCallingUid(), getUidForPackage(packageName)) ^ 1) == 0)) {
            checkSystemOrRoot("get application restrictions for other user/app " + packageName);
        }
        synchronized (this.mPackagesLock) {
            readApplicationRestrictionsLP = readApplicationRestrictionsLP(packageName, userId);
        }
        return readApplicationRestrictionsLP;
    }

    public void setApplicationRestrictions(String packageName, Bundle restrictions, int userId) {
        checkSystemOrRoot("set application restrictions");
        if (restrictions != null) {
            restrictions.setDefusable(true);
        }
        synchronized (this.mPackagesLock) {
            if (restrictions != null) {
                if (!restrictions.isEmpty()) {
                    writeApplicationRestrictionsLP(packageName, restrictions, userId);
                }
            }
            cleanAppRestrictionsForPackage(packageName, userId);
        }
        Intent changeIntent = new Intent("android.intent.action.APPLICATION_RESTRICTIONS_CHANGED");
        changeIntent.setPackage(packageName);
        changeIntent.addFlags(1073741824);
        this.mContext.sendBroadcastAsUser(changeIntent, UserHandle.of(userId));
    }

    private int getUidForPackage(String packageName) {
        long ident = Binder.clearCallingIdentity();
        int i;
        try {
            i = this.mContext.getPackageManager().getApplicationInfo(packageName, DumpState.DUMP_CHANGES).uid;
            return i;
        } catch (NameNotFoundException e) {
            i = -1;
            return i;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private Bundle readApplicationRestrictionsLP(String packageName, int userId) {
        return readApplicationRestrictionsLP(new AtomicFile(new File(Environment.getUserSystemDirectory(userId), packageToRestrictionsFileName(packageName))));
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x005e A:{Splitter: B:4:0x0016, ExcHandler: java.io.IOException (r0_0 'e' java.lang.Exception), PHI: r1 } */
    /* JADX WARNING: Missing block: B:14:0x005e, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:16:?, code:
            android.util.Log.w(LOG_TAG, "Error parsing " + r8.getBaseFile(), r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static Bundle readApplicationRestrictionsLP(AtomicFile restrictionsFile) {
        Bundle restrictions = new Bundle();
        ArrayList<String> values = new ArrayList();
        if (!restrictionsFile.getBaseFile().exists()) {
            return restrictions;
        }
        AutoCloseable autoCloseable = null;
        try {
            autoCloseable = restrictionsFile.openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(autoCloseable, StandardCharsets.UTF_8.name());
            XmlUtils.nextElement(parser);
            if (parser.getEventType() != 2) {
                Slog.e(LOG_TAG, "Unable to read restrictions file " + restrictionsFile.getBaseFile());
                return restrictions;
            }
            while (parser.next() != 1) {
                readEntry(restrictions, values, parser);
            }
            IoUtils.closeQuietly(autoCloseable);
            return restrictions;
        } catch (Exception e) {
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
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
                ArrayList<Bundle> bundleList = new ArrayList();
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

    private void writeApplicationRestrictionsLP(String packageName, Bundle restrictions, int userId) {
        writeApplicationRestrictionsLP(restrictions, new AtomicFile(new File(Environment.getUserSystemDirectory(userId), packageToRestrictionsFileName(packageName))));
    }

    static void writeApplicationRestrictionsLP(Bundle restrictions, AtomicFile restrictionsFile) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = restrictionsFile.startWrite();
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(bos, StandardCharsets.UTF_8.name());
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, TAG_RESTRICTIONS);
            writeBundle(restrictions, serializer);
            serializer.endTag(null, TAG_RESTRICTIONS);
            serializer.endDocument();
            restrictionsFile.finishWrite(fileOutputStream);
        } catch (Exception e) {
            restrictionsFile.failWrite(fileOutputStream);
            Slog.e(LOG_TAG, "Error writing application restrictions list", e);
        }
    }

    private static void writeBundle(Bundle restrictions, XmlSerializer serializer) throws IOException {
        for (String key : restrictions.keySet()) {
            Object value = restrictions.get(key);
            serializer.startTag(null, TAG_ENTRY);
            serializer.attribute(null, ATTR_KEY, key);
            int length;
            int i;
            if (value instanceof Boolean) {
                serializer.attribute(null, "type", ATTR_TYPE_BOOLEAN);
                serializer.text(value.toString());
            } else if (value instanceof Integer) {
                serializer.attribute(null, "type", ATTR_TYPE_INTEGER);
                serializer.text(value.toString());
            } else if (value == null || (value instanceof String)) {
                serializer.attribute(null, "type", ATTR_TYPE_STRING);
                serializer.text(value != null ? (String) value : "");
            } else if (value instanceof Bundle) {
                serializer.attribute(null, "type", ATTR_TYPE_BUNDLE);
                writeBundle((Bundle) value, serializer);
            } else if (value instanceof Parcelable[]) {
                serializer.attribute(null, "type", ATTR_TYPE_BUNDLE_ARRAY);
                Parcelable[] array = (Parcelable[]) value;
                length = array.length;
                i = 0;
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
                for (String choice : values) {
                    String choice2;
                    serializer.startTag(null, TAG_VALUE);
                    if (choice2 == null) {
                        choice2 = "";
                    }
                    serializer.text(choice2);
                    serializer.endTag(null, TAG_VALUE);
                }
            }
            serializer.endTag(null, TAG_ENTRY);
        }
    }

    public int getUserSerialNumber(int userHandle) {
        synchronized (this.mUsersLock) {
            if (exists(userHandle)) {
                int i = getUserInfoLU(userHandle).serialNumber;
                return i;
            }
            return -1;
        }
    }

    public boolean isUserNameSet(int userHandle) {
        boolean z = false;
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userHandle);
            if (!(userInfo == null || userInfo.name == null)) {
                z = true;
            }
        }
        return z;
    }

    public int getUserHandle(int userSerialNumber) {
        synchronized (this.mUsersLock) {
            int[] iArr = this.mUserIds;
            int i = 0;
            int length = iArr.length;
            while (i < length) {
                int userId = iArr[i];
                UserInfo info = getUserInfoLU(userId);
                if (info == null || info.serialNumber != userSerialNumber) {
                    i++;
                } else {
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

    private void updateUserIds() {
        int num = 0;
        synchronized (this.mUsersLock) {
            int i;
            int userSize = this.mUsers.size();
            for (i = 0; i < userSize; i++) {
                if (!((UserData) this.mUsers.valueAt(i)).info.partial) {
                    num++;
                }
            }
            int[] newUsers = new int[num];
            i = 0;
            int n = 0;
            while (i < userSize) {
                int n2;
                if (((UserData) this.mUsers.valueAt(i)).info.partial) {
                    n2 = n;
                } else {
                    n2 = n + 1;
                    newUsers[n] = this.mUsers.keyAt(i);
                }
                i++;
                n = n2;
            }
            this.mUserIds = newUsers;
        }
    }

    public void onBeforeStartUser(int userId) {
        UserInfo userInfo = getUserInfo(userId);
        if (userInfo != null) {
            boolean migrateAppsData = Build.FINGERPRINT.equals(userInfo.lastLoggedInFingerprint) ^ 1;
            this.mUserDataPreparer.prepareUserData(userId, userInfo.serialNumber, 1);
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
            boolean migrateAppsData = Build.FINGERPRINT.equals(userInfo.lastLoggedInFingerprint) ^ 1;
            this.mUserDataPreparer.prepareUserData(userId, userInfo.serialNumber, 2);
            this.mPm.reconcileAppsData(userId, 2, migrateAppsData);
        }
    }

    void reconcileUsers(String volumeUuid) {
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
        scheduleWriteUser(userData);
    }

    /* JADX WARNING: Missing block: B:21:0x0046, code:
            if (r0 >= 0) goto L_0x0051;
     */
    /* JADX WARNING: Missing block: B:23:0x0050, code:
            throw new java.lang.IllegalStateException("No user id available!");
     */
    /* JADX WARNING: Missing block: B:24:0x0051, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int getNextAvailableId() {
        synchronized (this.mUsersLock) {
            int nextId = scanNextAvailableIdLocked();
            if (nextId >= 0) {
                return nextId;
            } else if (this.mRemovingUserIds.size() > 0) {
                Slog.i(LOG_TAG, "All available IDs are used. Recycling LRU ids.");
                this.mRemovingUserIds.clear();
                for (Integer recentlyRemovedId : this.mRecentlyRemovedIds) {
                    this.mRemovingUserIds.put(recentlyRemovedId.intValue(), true);
                }
                nextId = scanNextAvailableIdLocked();
            }
        }
    }

    private int scanNextAvailableIdLocked() {
        int i = 10;
        while (i < MAX_USER_ID) {
            if (this.mUsers.indexOfKey(i) < 0 && (this.mRemovingUserIds.get(i) ^ 1) != 0 && i != REPAIR_MODE_USER_ID && (this.mUserDataPreparer.isUserIdInvalid(i) ^ 1) != 0) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private String packageToRestrictionsFileName(String packageName) {
        return RESTRICTIONS_FILE_PREFIX + packageName + XML_SUFFIX;
    }

    /* JADX WARNING: Missing block: B:17:0x0038, code:
            if (r11 == false) goto L_0x003d;
     */
    /* JADX WARNING: Missing block: B:18:0x003a, code:
            writeUserLP(r0);
     */
    /* JADX WARNING: Missing block: B:20:0x003e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setSeedAccountData(int userId, String accountName, String accountType, PersistableBundle accountOptions, boolean persist) {
        checkManageUsersPermission("Require MANAGE_USERS permission to set user seed data");
        synchronized (this.mPackagesLock) {
            synchronized (this.mUsersLock) {
                UserData userData = getUserDataLU(userId);
                if (userData == null) {
                    Slog.e(LOG_TAG, "No such user for settings seed data u=" + userId);
                    return;
                }
                userData.seedAccountName = accountName;
                userData.seedAccountType = accountType;
                userData.seedAccountOptions = accountOptions;
                userData.persistSeedData = persist;
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
                if (userData == null) {
                    return;
                }
                userData.clearSeedAccountData();
                writeUserLP(userData);
            }
        }
    }

    public boolean someUserHasSeedAccount(String accountName, String accountType) throws RemoteException {
        checkManageUsersPermission("Cannot check seed account information");
        synchronized (this.mUsersLock) {
            int userSize = this.mUsers.size();
            int i = 0;
            while (i < userSize) {
                UserData data = (UserData) this.mUsers.valueAt(i);
                if (data.info.isInitialized() || data.seedAccountName == null || (data.seedAccountName.equals(accountName) ^ 1) != 0 || data.seedAccountType == null || (data.seedAccountType.equals(accountType) ^ 1) != 0) {
                    i++;
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new Shell(this, null).exec(this, in, out, err, args, callback, resultReceiver);
    }

    int onShellCommand(Shell shell, String cmd) {
        if (cmd == null) {
            return shell.handleDefaultCommands(cmd);
        }
        PrintWriter pw = shell.getOutPrintWriter();
        try {
            if (cmd.equals("list")) {
                return runList(pw);
            }
        } catch (RemoteException e) {
            pw.println("Remote exception: " + e);
        }
        return -1;
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
            pw.println("\t" + ((UserInfo) users.get(i)).toString() + (am.isUserRunning(((UserInfo) users.get(i)).id, 0) ? " running" : ""));
        }
        return 0;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, LOG_TAG, pw)) {
            long now = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
            synchronized (this.mPackagesLock) {
                synchronized (this.mUsersLock) {
                    pw.println("Users:");
                    for (int i = 0; i < this.mUsers.size(); i++) {
                        UserData userData = (UserData) this.mUsers.valueAt(i);
                        if (userData != null) {
                            int state;
                            UserInfo userInfo = userData.info;
                            int userId = userInfo.id;
                            pw.print("  ");
                            pw.print(userInfo);
                            pw.print(" serialNo=");
                            pw.print(userInfo.serialNumber);
                            if (this.mRemovingUserIds.get(userId)) {
                                pw.print(" <removing> ");
                            }
                            if (userInfo.partial) {
                                pw.print(" <partial>");
                            }
                            pw.println();
                            pw.print("    State: ");
                            synchronized (this.mUserStates) {
                                state = this.mUserStates.get(userId, -1);
                            }
                            pw.println(UserState.stateToString(state));
                            pw.print("    Created: ");
                            if (userInfo.creationTime == 0) {
                                pw.println("<unknown>");
                            } else {
                                sb.setLength(0);
                                TimeUtils.formatDuration(now - userInfo.creationTime, sb);
                                sb.append(" ago");
                                pw.println(sb);
                            }
                            pw.print("    Last logged in: ");
                            if (userInfo.lastLoggedInTime == 0) {
                                pw.println("<unknown>");
                            } else {
                                sb.setLength(0);
                                TimeUtils.formatDuration(now - userInfo.lastLoggedInTime, sb);
                                sb.append(" ago");
                                pw.println(sb);
                            }
                            pw.print("    Last logged in fingerprint: ");
                            pw.println(userInfo.lastLoggedInFingerprint);
                            pw.print("    Has profile owner: ");
                            pw.println(this.mIsUserManaged.get(userId));
                            pw.println("    Restrictions:");
                            synchronized (this.mRestrictionsLock) {
                                UserRestrictionsUtils.dumpRestrictions(pw, "      ", (Bundle) this.mBaseUserRestrictions.get(userInfo.id));
                                pw.println("    Device policy global restrictions:");
                                UserRestrictionsUtils.dumpRestrictions(pw, "      ", (Bundle) this.mDevicePolicyGlobalUserRestrictions.get(userInfo.id));
                                pw.println("    Device policy local restrictions:");
                                UserRestrictionsUtils.dumpRestrictions(pw, "      ", (Bundle) this.mDevicePolicyLocalUserRestrictions.get(userInfo.id));
                                pw.println("    Effective restrictions:");
                                UserRestrictionsUtils.dumpRestrictions(pw, "      ", (Bundle) this.mCachedEffectiveUserRestrictions.get(userInfo.id));
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
                        }
                    }
                }
                pw.println();
                pw.println("  Device owner id:" + this.mDeviceOwnerUserId);
                pw.println();
                pw.println("  Guest restrictions:");
                synchronized (this.mGuestRestrictions) {
                    PrintWriter printWriter = pw;
                    UserRestrictionsUtils.dumpRestrictions(printWriter, "    ", this.mGuestRestrictions);
                }
                synchronized (this.mUsersLock) {
                    pw.println();
                    pw.println("  Device managed: " + this.mIsDeviceManaged);
                    if (this.mRemovingUserIds.size() > 0) {
                        pw.println();
                        pw.println("  Recently removed userIds: " + this.mRecentlyRemovedIds);
                    }
                }
                synchronized (this.mUserStates) {
                    pw.println("  Started users state: " + this.mUserStates);
                }
                pw.println();
                pw.println("  Max users: " + UserManager.getMaxSupportedUsers());
                pw.println("  Supports switchable users: " + UserManager.supportsMultipleUsers());
                pw.println("  All guests ephemeral: " + Resources.getSystem().getBoolean(17956969));
            }
        }
    }

    boolean isInitialized(int userId) {
        return (getUserInfo(userId).flags & 16) != 0;
    }

    private void removeNonSystemUsers() {
        UserInfo ui;
        ArrayList<UserInfo> usersToRemove = new ArrayList();
        synchronized (this.mUsersLock) {
            int userSize = this.mUsers.size();
            for (int i = 0; i < userSize; i++) {
                ui = ((UserData) this.mUsers.valueAt(i)).info;
                if (ui.id != 0) {
                    usersToRemove.add(ui);
                }
            }
        }
        for (UserInfo ui2 : usersToRemove) {
            removeUser(ui2.id);
        }
    }

    private static void debug(String message) {
        Log.d(LOG_TAG, message + "");
    }

    static int getMaxManagedProfiles() {
        if (Build.IS_DEBUGGABLE) {
            return SystemProperties.getInt("persist.sys.max_profiles", 1);
        }
        return 1;
    }

    int getFreeProfileBadgeLU(int parentUserId) {
        int i;
        int maxManagedProfiles = getMaxManagedProfiles();
        boolean[] usedBadges = new boolean[maxManagedProfiles];
        int userSize = this.mUsers.size();
        for (i = 0; i < userSize; i++) {
            UserInfo ui = ((UserData) this.mUsers.valueAt(i)).info;
            if (ui.isManagedProfile() && ui.profileGroupId == parentUserId && (this.mRemovingUserIds.get(ui.id) ^ 1) != 0 && ui.profileBadge < maxManagedProfiles) {
                usedBadges[ui.profileBadge] = true;
            }
        }
        for (i = 0; i < maxManagedProfiles; i++) {
            if (!usedBadges[i]) {
                return i;
            }
        }
        return 0;
    }

    boolean hasManagedProfile(int userId) {
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userId);
            int userSize = this.mUsers.size();
            int i = 0;
            while (i < userSize) {
                UserInfo profile = ((UserData) this.mUsers.valueAt(i)).info;
                if (userId == profile.id || !isProfileOf(userInfo, profile)) {
                    i++;
                } else {
                    return true;
                }
            }
            return false;
        }
    }
}

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
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.hwtheme.HwThemeManager;
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
import android.os.ShellCommand;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerInternal;
import android.os.UserManagerInternal.UserRestrictionsListener;
import android.os.storage.StorageManager;
import android.provider.Settings.Secure;
import android.security.GateKeeper;
import android.service.gatekeeper.IGateKeeperService;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
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
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.am.ProcessList;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import libcore.io.IoUtils;
import libcore.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class UserManagerService extends AbsUserManagerService {
    private static final int ALLOWED_FLAGS_FOR_CREATE_USERS_PERMISSION = 300;
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
    private static final int MAX_MANAGED_PROFILES = 1;
    private static final int MAX_USER_ID = 21474;
    private static final int MIN_USER_ID = 10;
    private static final String RESTRICTIONS_FILE_PREFIX = "res_";
    private static final String TAG_ACCOUNT = "account";
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
    private static final String USER_INFO_DIR = null;
    private static final String USER_LIST_FILENAME = "userlist.xml";
    private static final String USER_PHOTO_FILENAME = "photo.png";
    private static final String USER_PHOTO_FILENAME_TMP = "photo.png.tmp";
    private static final int USER_VERSION = 6;
    static final int WRITE_USER_DELAY = 2000;
    static final int WRITE_USER_MSG = 1;
    private static final String XATTR_SERIAL = "user.serial";
    private static final String XML_SUFFIX = ".xml";
    private static final IBinder mUserRestriconToken = null;
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
    private Bundle mDevicePolicyGlobalUserRestrictions;
    @GuardedBy("mRestrictionsLock")
    private final SparseArray<Bundle> mDevicePolicyLocalUserRestrictions;
    private final BroadcastReceiver mDisableQuietModeCallback;
    @GuardedBy("mUsersLock")
    private boolean mForceEphemeralUsers;
    @GuardedBy("mRestrictionsLock")
    private int mGlobalRestrictionOwnerUserId;
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
    private final PackageManagerService mPm;
    @GuardedBy("mUsersLock")
    private final SparseBooleanArray mRemovingUserIds;
    private final Object mRestrictionsLock;
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

    /* renamed from: com.android.server.pm.UserManagerService.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ Bundle val$newRestrictionsFinal;
        final /* synthetic */ Bundle val$prevRestrictionsFinal;
        final /* synthetic */ int val$userId;

        AnonymousClass2(int val$userId, Bundle val$newRestrictionsFinal, Bundle val$prevRestrictionsFinal) {
            this.val$userId = val$userId;
            this.val$newRestrictionsFinal = val$newRestrictionsFinal;
            this.val$prevRestrictionsFinal = val$prevRestrictionsFinal;
        }

        public void run() {
            UserRestrictionsUtils.applyUserRestrictions(UserManagerService.this.mContext, this.val$userId, this.val$newRestrictionsFinal, this.val$prevRestrictionsFinal);
            synchronized (UserManagerService.this.mUserRestrictionsListeners) {
                UserRestrictionsListener[] listeners = new UserRestrictionsListener[UserManagerService.this.mUserRestrictionsListeners.size()];
                UserManagerService.this.mUserRestrictionsListeners.toArray(listeners);
            }
            for (int i = 0; i < listeners.length; i += UserManagerService.WRITE_USER_MSG) {
                listeners[i].onUserRestrictionsChanged(this.val$userId, this.val$newRestrictionsFinal, this.val$prevRestrictionsFinal);
            }
        }
    }

    /* renamed from: com.android.server.pm.UserManagerService.5 */
    class AnonymousClass5 extends BroadcastReceiver {
        final /* synthetic */ int val$userHandle;

        /* renamed from: com.android.server.pm.UserManagerService.5.1 */
        class AnonymousClass1 extends Thread {
            final /* synthetic */ int val$userHandle;

            AnonymousClass1(int val$userHandle) {
                this.val$userHandle = val$userHandle;
            }

            public void run() {
                ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).onUserRemoved(this.val$userHandle);
                UserManagerService.this.removeUserState(this.val$userHandle);
            }
        }

        AnonymousClass5(int val$userHandle) {
            this.val$userHandle = val$userHandle;
        }

        public void onReceive(Context context, Intent intent) {
            Flog.i(900, "USER_REMOVED broadcast sent, cleaning up user data " + this.val$userHandle);
            new AnonymousClass1(this.val$userHandle).start();
        }
    }

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
            if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
                this.mUms.cleanupPartialUsers();
            }
        }
    }

    private class LocalService extends UserManagerInternal {
        private LocalService() {
        }

        public void setDevicePolicyUserRestrictions(int userId, Bundle localRestrictions, Bundle globalRestrictions) {
            UserManagerService.this.setDevicePolicyUserRestrictionsInner(userId, localRestrictions, globalRestrictions);
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
                UserManagerService.this.mBaseUserRestrictions.put(userId, new Bundle(baseRestrictions));
                UserManagerService.this.invalidateEffectiveUserRestrictionsLR(userId);
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
                        return;
                    }
                    UserManagerService.this.writeBitmapLP(userData.info, bitmap);
                    UserManagerService.this.writeUserLP(userData);
                    UserManagerService.this.sendUserInfoChangedBroadcast(userId);
                    Binder.restoreCallingIdentity(ident);
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
            UserInfo user = UserManagerService.this.createUserInternalUnchecked(name, flags, -10000);
            if (!(user == null || user.isAdmin())) {
                UserManagerService.this.setUserRestriction("no_sms", true, user.id);
                UserManagerService.this.setUserRestriction("no_outgoing_calls", true, user.id);
            }
            return user;
        }

        public boolean isUserRunning(int userId) {
            boolean z = UserManagerService.DBG_WITH_STACKTRACE;
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

        public boolean isUserUnlockingOrUnlocked(int userId) {
            boolean z = true;
            synchronized (UserManagerService.this.mUserStates) {
                int state = UserManagerService.this.mUserStates.get(userId, -1);
                if (!(state == 2 || state == 3)) {
                    z = UserManagerService.DBG_WITH_STACKTRACE;
                }
            }
            return z;
        }
    }

    final class MainHandler extends Handler {
        MainHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UserManagerService.WRITE_USER_MSG /*1*/:
                    removeMessages(UserManagerService.WRITE_USER_MSG, msg.obj);
                    synchronized (UserManagerService.this.mPackagesLock) {
                        UserData userData = UserManagerService.this.getUserDataNoChecks(((UserData) msg.obj).info.id);
                        if (userData != null) {
                            UserManagerService.this.writeUserLP(userData);
                        }
                        break;
                    }
                default:
            }
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

    private static class UserData {
        String account;
        UserInfo info;
        boolean persistSeedData;
        String seedAccountName;
        PersistableBundle seedAccountOptions;
        String seedAccountType;

        private UserData() {
        }

        void clearSeedAccountData() {
            this.seedAccountName = null;
            this.seedAccountType = null;
            this.seedAccountOptions = null;
            this.persistSeedData = UserManagerService.DBG_WITH_STACKTRACE;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.UserManagerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.UserManagerService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.UserManagerService.<clinit>():void");
    }

    public static UserManagerService getInstance() {
        UserManagerService userManagerService;
        synchronized (UserManagerService.class) {
            userManagerService = sInstance;
        }
        return userManagerService;
    }

    UserManagerService(File dataDir) {
        this(null, null, new Object(), dataDir);
    }

    UserManagerService(Context context, PackageManagerService pm, Object packagesLock) {
        this(context, pm, packagesLock, Environment.getDataDirectory());
    }

    private UserManagerService(Context context, PackageManagerService pm, Object packagesLock, File dataDir) {
        this.mUsersLock = new Object();
        this.mRestrictionsLock = new Object();
        this.mUsers = new SparseArray();
        this.mBaseUserRestrictions = new SparseArray();
        this.mCachedEffectiveUserRestrictions = new SparseArray();
        this.mAppliedUserRestrictions = new SparseArray();
        this.mGlobalRestrictionOwnerUserId = -10000;
        this.mDevicePolicyLocalUserRestrictions = new SparseArray();
        this.mGuestRestrictions = new Bundle();
        this.mRemovingUserIds = new SparseBooleanArray();
        this.mUserVersion = 0;
        this.mIsUserManaged = new SparseBooleanArray();
        this.mUserRestrictionsListeners = new ArrayList();
        this.ACTION_DISABLE_QUIET_MODE_AFTER_UNLOCK = "com.android.server.pm.DISABLE_QUIET_MODE_AFTER_UNLOCK";
        this.mDisableQuietModeCallback = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("com.android.server.pm.DISABLE_QUIET_MODE_AFTER_UNLOCK".equals(intent.getAction())) {
                    IntentSender target = (IntentSender) intent.getParcelableExtra("android.intent.extra.INTENT");
                    UserManagerService.this.setQuietModeEnabled(intent.getIntExtra("android.intent.extra.USER_ID", 0), UserManagerService.DBG_WITH_STACKTRACE);
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
        this.isOwnerNameChanged = DBG_WITH_STACKTRACE;
        this.mContext = context;
        this.mPm = pm;
        this.mPackagesLock = packagesLock;
        this.mHandler = new MainHandler();
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
                this.isOwnerNameChanged = info.name.equals(this.mContext.getResources().getString(17040668)) ? DBG_WITH_STACKTRACE : true;
            }
        }
        this.mLocalService = new LocalService();
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
        if (!(currentGuestUser == null || hasUserRestriction("no_config_wifi", currentGuestUser.id))) {
            setUserRestriction("no_config_wifi", true, currentGuestUser.id);
        }
        this.mContext.registerReceiver(this.mDisableQuietModeCallback, new IntentFilter("com.android.server.pm.DISABLE_QUIET_MODE_AFTER_UNLOCK"), null, this.mHandler);
    }

    void cleanupPartialUsers() {
        ArrayList<UserInfo> partials = new ArrayList();
        synchronized (this.mUsersLock) {
            int userSize = this.mUsers.size();
            int i = 0;
            while (i < userSize) {
                UserInfo ui = ((UserData) this.mUsers.valueAt(i)).info;
                if ((ui.partial || ui.guestToRemove || ui.isEphemeral()) && i != 0) {
                    partials.add(ui);
                }
                i += WRITE_USER_MSG;
            }
        }
        int partialsSize = partials.size();
        for (i = 0; i < partialsSize; i += WRITE_USER_MSG) {
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

    public void setUserAccount(int userId, String accountName) {
        checkManageUserAndAcrossUsersFullPermission("set user account");
        UserData userToUpdate = null;
        synchronized (this.mPackagesLock) {
            synchronized (this.mUsersLock) {
                UserData userData = (UserData) this.mUsers.get(userId);
                if (userData == null) {
                    Slog.e(LOG_TAG, "User not found for setting user account: u" + userId);
                    return;
                }
                if (!Objects.equal(userData.account, accountName)) {
                    userData.account = accountName;
                    userToUpdate = userData;
                }
                if (userToUpdate != null) {
                    writeUserLP(userToUpdate);
                }
                return;
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
                if (!ui.isPrimary() || this.mRemovingUserIds.get(ui.id)) {
                    i += WRITE_USER_MSG;
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
            for (int i = 0; i < userSize; i += WRITE_USER_MSG) {
                UserInfo ui = ((UserData) this.mUsers.valueAt(i)).info;
                if (!ui.partial) {
                    if (!(excludeDying && this.mRemovingUserIds.get(ui.id))) {
                        users.add(userWithName(ui));
                    }
                    if (ui.id == 0 && !this.isOwnerNameChanged) {
                        boolean nameChanged = DBG_WITH_STACKTRACE;
                        String ownerName = this.mContext.getResources().getString(17040668);
                        if (!(TextUtils.isEmpty(ui.name) || ui.name.equals(ownerName))) {
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
        boolean returnFullInfo = true;
        if (userId != UserHandle.getCallingUserId()) {
            List<UserInfo> list = "getting profiles related to user ";
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
            checkManageUsersPermission("getting profiles related to user " + userId);
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
        for (int i = 0; i < profileIds.size(); i += WRITE_USER_MSG) {
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
        for (int i = 0; i < userSize; i += WRITE_USER_MSG) {
            UserInfo profile = ((UserData) this.mUsers.valueAt(i)).info;
            if (isProfileOf(user, profile) && !((enabledOnly && !profile.isEnabled()) || this.mRemovingUserIds.get(profile.id) || profile.partial)) {
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
        boolean isSameProfileGroupLP;
        checkManageUsersPermission("check if in the same profile group");
        synchronized (this.mPackagesLock) {
            isSameProfileGroupLP = isSameProfileGroupLP(userId, otherUserId);
        }
        return isSameProfileGroupLP;
    }

    private boolean isSameProfileGroupLP(int userId, int otherUserId) {
        boolean z = DBG_WITH_STACKTRACE;
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userId);
            if (userInfo == null || userInfo.profileGroupId == -10000) {
                return DBG_WITH_STACKTRACE;
            }
            UserInfo otherUserInfo = getUserInfoLU(otherUserId);
            if (otherUserInfo == null || otherUserInfo.profileGroupId == -10000) {
                return DBG_WITH_STACKTRACE;
            }
            if (userInfo.profileGroupId == otherUserInfo.profileGroupId) {
                z = true;
            }
            return z;
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
        if (parentUserId == -10000) {
            return null;
        }
        return getUserInfoLU(parentUserId);
    }

    private static boolean isProfileOf(UserInfo user, UserInfo profile) {
        if (user.id == profile.id) {
            return true;
        }
        if (user.profileGroupId != -10000) {
            return user.profileGroupId == profile.profileGroupId ? true : DBG_WITH_STACKTRACE;
        } else {
            return DBG_WITH_STACKTRACE;
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
        checkManageUsersPermission("silence profile");
        boolean changed = DBG_WITH_STACKTRACE;
        synchronized (this.mPackagesLock) {
            synchronized (this.mUsersLock) {
                UserInfo profile = getUserInfoLU(userHandle);
                UserInfo parent = getProfileParentLU(userHandle);
            }
            if (profile == null || !profile.isManagedProfile()) {
                throw new IllegalArgumentException("User " + userHandle + " is not a profile");
            }
            if (profile.isQuietModeEnabled() != enableQuietMode) {
                profile.flags ^= DumpState.DUMP_PACKAGES;
                writeUserLP(getUserDataLU(profile.id));
                changed = true;
            }
        }
        if (changed) {
            long identity = Binder.clearCallingIdentity();
            if (enableQuietMode) {
                try {
                    ActivityManagerNative.getDefault().stopUser(userHandle, true, null);
                    ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).killForegroundAppsForUser(userHandle);
                } catch (RemoteException e) {
                    Slog.e(LOG_TAG, "fail to start/stop user for quiet mode", e);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            } else {
                ActivityManagerNative.getDefault().startUserInBackground(userHandle);
            }
            Binder.restoreCallingIdentity(identity);
            broadcastProfileAvailabilityChanges(profile.getUserHandle(), parent.getUserHandle(), enableQuietMode);
        }
    }

    public boolean isQuietModeEnabled(int userHandle) {
        synchronized (this.mPackagesLock) {
            synchronized (this.mUsersLock) {
                UserInfo info = getUserInfoLU(userHandle);
            }
            if (info == null || !info.isManagedProfile()) {
                return DBG_WITH_STACKTRACE;
            }
            boolean isQuietModeEnabled = info.isQuietModeEnabled();
            return isQuietModeEnabled;
        }
    }

    public boolean trySetQuietModeDisabled(int userHandle, IntentSender target) {
        checkManageUsersPermission("silence profile");
        if (StorageManager.isUserKeyUnlocked(userHandle) || !this.mLockPatternUtils.isSecure(userHandle)) {
            setQuietModeEnabled(userHandle, DBG_WITH_STACKTRACE);
            return true;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            Intent unlockIntent = ((KeyguardManager) this.mContext.getSystemService("keyguard")).createConfirmDeviceCredentialIntent(null, null, userHandle);
            if (unlockIntent == null) {
                return DBG_WITH_STACKTRACE;
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
            return DBG_WITH_STACKTRACE;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void setUserEnabled(int userId) {
        checkManageUsersPermission("enable user");
        synchronized (this.mPackagesLock) {
            synchronized (this.mUsersLock) {
                UserInfo info = getUserInfoLU(userId);
            }
            if (!(info == null || info.isEnabled())) {
                info.flags ^= 64;
                writeUserLP(getUserDataLU(info.id));
            }
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

    public boolean isManagedProfile(int userId) {
        boolean isManagedProfile;
        int callingUserId = UserHandle.getCallingUserId();
        if (!(callingUserId == userId || hasManageUsersPermission())) {
            synchronized (this.mPackagesLock) {
                if (isSameProfileGroupLP(callingUserId, userId)) {
                } else {
                    throw new SecurityException("You need MANAGE_USERS permission to: check if specified user a managed profile outside your profile group");
                }
            }
        }
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userId);
            if (userInfo != null) {
                isManagedProfile = userInfo.isManagedProfile();
            } else {
                isManagedProfile = DBG_WITH_STACKTRACE;
            }
        }
        return isManagedProfile;
    }

    public boolean isRestricted() {
        boolean isRestricted;
        synchronized (this.mUsersLock) {
            isRestricted = getUserInfoLU(UserHandle.getCallingUserId()).isRestricted();
        }
        return isRestricted;
    }

    public boolean canHaveRestrictedProfile(int userId) {
        boolean z = DBG_WITH_STACKTRACE;
        checkManageUsersPermission("canHaveRestrictedProfile");
        synchronized (this.mUsersLock) {
            UserInfo userInfo = getUserInfoLU(userId);
            if (userInfo == null || !userInfo.canHaveProfile()) {
                return DBG_WITH_STACKTRACE;
            } else if (userInfo.isAdmin()) {
                if (!(this.mIsDeviceManaged || this.mIsUserManaged.get(userId))) {
                    z = true;
                }
                return z;
            } else {
                return DBG_WITH_STACKTRACE;
            }
        }
    }

    private UserInfo getUserInfoLU(int userId) {
        UserInfo userInfo = null;
        UserData userData = (UserData) this.mUsers.get(userId);
        if (userData == null || !userData.info.partial || this.mRemovingUserIds.get(userId)) {
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
        if (userData == null || !userData.info.partial || this.mRemovingUserIds.get(userId)) {
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
        return getUserInfoNoChecks(userId) != null ? true : DBG_WITH_STACKTRACE;
    }

    public void setUserName(int userId, String name) {
        checkManageUsersPermission("rename users");
        boolean changed = DBG_WITH_STACKTRACE;
        synchronized (this.mPackagesLock) {
            UserData userData = getUserDataNoChecks(userId);
            if (userData == null || userData.info.partial) {
                Slog.w(LOG_TAG, "setUserName: unknown user #" + userId);
                return;
            }
            if (name != null) {
                if (!name.equals(userData.info.name)) {
                    userData.info.name = name;
                    writeUserLP(userData);
                    changed = true;
                }
            }
            if (name != null && userId == 0) {
                if (this.mContext.getResources() == null || !name.equals(this.mContext.getResources().getString(17040668))) {
                    this.isOwnerNameChanged = true;
                } else {
                    this.isOwnerNameChanged = DBG_WITH_STACKTRACE;
                }
            }
            if (changed) {
                sendUserInfoChangedBroadcast(userId);
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
            boolean sameGroup = callingGroupId != -10000 ? callingGroupId == targetUserInfo.profileGroupId ? true : DBG_WITH_STACKTRACE : DBG_WITH_STACKTRACE;
            if (!(callingUserId == targetUserId || sameGroup)) {
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

    public void makeInitialized(int userId) {
        checkManageUsersPermission("makeInitialized");
        boolean scheduleWriteUser = DBG_WITH_STACKTRACE;
        synchronized (this.mUsersLock) {
            UserData userData = (UserData) this.mUsers.get(userId);
            if (userData == null || userData.info.partial) {
                Slog.w(LOG_TAG, "makeInitialized: unknown user #" + userId);
                return;
            }
            if ((userData.info.flags & 16) == 0) {
                UserInfo userInfo = userData.info;
                userInfo.flags |= 16;
                scheduleWriteUser = true;
            }
            if (scheduleWriteUser) {
                scheduleWriteUser(userData);
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

    void setDevicePolicyUserRestrictionsInner(int userId, Bundle local, Bundle global) {
        Preconditions.checkNotNull(local);
        boolean globalChanged = DBG_WITH_STACKTRACE;
        synchronized (this.mRestrictionsLock) {
            if (global != null) {
                globalChanged = UserRestrictionsUtils.areEqual(this.mDevicePolicyGlobalUserRestrictions, global) ? DBG_WITH_STACKTRACE : true;
                if (globalChanged) {
                    this.mDevicePolicyGlobalUserRestrictions = global;
                }
                this.mGlobalRestrictionOwnerUserId = userId;
            } else if (this.mGlobalRestrictionOwnerUserId == userId) {
                this.mGlobalRestrictionOwnerUserId = -10000;
            }
            boolean localChanged = UserRestrictionsUtils.areEqual((Bundle) this.mDevicePolicyLocalUserRestrictions.get(userId), local) ? DBG_WITH_STACKTRACE : true;
            if (localChanged) {
                this.mDevicePolicyLocalUserRestrictions.put(userId, local);
            }
        }
        synchronized (this.mPackagesLock) {
            if (localChanged) {
                writeUserLP(getUserDataNoChecks(userId));
            }
            if (globalChanged) {
                writeUserListLP();
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

    @GuardedBy("mRestrictionsLock")
    private Bundle computeEffectiveUserRestrictionsLR(int userId) {
        Bundle baseRestrictions = UserRestrictionsUtils.nonNull((Bundle) this.mBaseUserRestrictions.get(userId));
        Bundle global = this.mDevicePolicyGlobalUserRestrictions;
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
        boolean z = DBG_WITH_STACKTRACE;
        if (!UserRestrictionsUtils.isValidRestriction(restrictionKey)) {
            return DBG_WITH_STACKTRACE;
        }
        Bundle restrictions = getEffectiveUserRestrictions(userId);
        if (restrictions != null) {
            z = restrictions.getBoolean(restrictionKey);
        }
        return z;
    }

    public int getUserRestrictionSource(String restrictionKey, int userId) {
        checkManageUsersPermission("getUserRestrictionSource");
        int result = 0;
        if (!hasUserRestriction(restrictionKey, userId)) {
            return 0;
        }
        if (hasBaseUserRestriction(restrictionKey, userId)) {
            result = WRITE_USER_MSG;
        }
        synchronized (this.mRestrictionsLock) {
            Bundle localRestrictions = (Bundle) this.mDevicePolicyLocalUserRestrictions.get(userId);
            if (!UserRestrictionsUtils.isEmpty(localRestrictions) && localRestrictions.getBoolean(restrictionKey)) {
                if (this.mGlobalRestrictionOwnerUserId == userId) {
                    result |= 2;
                } else {
                    result |= 4;
                }
            }
            if (!UserRestrictionsUtils.isEmpty(this.mDevicePolicyGlobalUserRestrictions) && this.mDevicePolicyGlobalUserRestrictions.getBoolean(restrictionKey)) {
                result |= 2;
            }
        }
        return result;
    }

    public Bundle getUserRestrictions(int userId) {
        return UserRestrictionsUtils.clone(getEffectiveUserRestrictions(userId));
    }

    public boolean hasBaseUserRestriction(String restrictionKey, int userId) {
        boolean z = DBG_WITH_STACKTRACE;
        checkManageUsersPermission("hasBaseUserRestriction");
        if (!UserRestrictionsUtils.isValidRestriction(restrictionKey)) {
            return DBG_WITH_STACKTRACE;
        }
        synchronized (this.mRestrictionsLock) {
            Bundle bundle = (Bundle) this.mBaseUserRestrictions.get(userId);
            if (bundle != null) {
                z = bundle.getBoolean(restrictionKey, DBG_WITH_STACKTRACE);
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
    private void updateUserRestrictionsInternalLR(Bundle newRestrictions, int userId) {
        boolean z = true;
        Bundle prevAppliedRestrictions = UserRestrictionsUtils.nonNull((Bundle) this.mAppliedUserRestrictions.get(userId));
        if (newRestrictions != null) {
            boolean z2;
            Bundle prevBaseRestrictions = (Bundle) this.mBaseUserRestrictions.get(userId);
            if (prevBaseRestrictions != newRestrictions) {
                z2 = true;
            } else {
                z2 = DBG_WITH_STACKTRACE;
            }
            Preconditions.checkState(z2);
            if (this.mCachedEffectiveUserRestrictions.get(userId) == newRestrictions) {
                z = DBG_WITH_STACKTRACE;
            }
            Preconditions.checkState(z);
            if (!UserRestrictionsUtils.areEqual(prevBaseRestrictions, newRestrictions)) {
                this.mBaseUserRestrictions.put(userId, newRestrictions);
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

    private void propagateUserRestrictionsLR(int userId, Bundle newRestrictions, Bundle prevRestrictions) {
        if (!UserRestrictionsUtils.areEqual(newRestrictions, prevRestrictions)) {
            this.mHandler.post(new AnonymousClass2(userId, new Bundle(newRestrictions), new Bundle(prevRestrictions)));
        }
    }

    void applyUserRestrictionsLR(int userId) {
        updateUserRestrictionsInternalLR(null, userId);
    }

    @GuardedBy("mRestrictionsLock")
    void applyUserRestrictionsForAllUsersLR() {
        this.mCachedEffectiveUserRestrictions.clear();
        this.mHandler.post(new Runnable() {
            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                try {
                    int[] runningUsers = ActivityManagerNative.getDefault().getRunningUserIds();
                    synchronized (UserManagerService.this.mRestrictionsLock) {
                        int i = 0;
                        while (true) {
                            if (i < runningUsers.length) {
                                UserManagerService.this.applyUserRestrictionsLR(runningUsers[i]);
                                i += UserManagerService.WRITE_USER_MSG;
                            }
                        }
                    }
                } catch (RemoteException e) {
                    Log.w(UserManagerService.LOG_TAG, "Unable to access ActivityManagerNative");
                }
            }
        });
    }

    private boolean isUserLimitReached() {
        int count;
        synchronized (this.mUsersLock) {
            count = getAliveUsersExcludingGuestsCountLU();
        }
        return count >= UserManager.getMaxSupportedUsers() ? true : DBG_WITH_STACKTRACE;
    }

    public boolean canAddMoreManagedProfiles(int userId, boolean allowedToRemoveOne) {
        boolean z = true;
        checkManageUsersPermission("check if more managed profiles can be added.");
        if (ActivityManager.isLowRamDeviceStatic() || !this.mContext.getPackageManager().hasSystemFeature("android.software.managed_users")) {
            return DBG_WITH_STACKTRACE;
        }
        int profilesRemovedCount;
        int managedProfilesCount = getProfiles(userId, true).size() - 1;
        if (managedProfilesCount <= 0 || !allowedToRemoveOne) {
            profilesRemovedCount = 0;
        } else {
            profilesRemovedCount = WRITE_USER_MSG;
        }
        if (managedProfilesCount - profilesRemovedCount >= WRITE_USER_MSG) {
            return DBG_WITH_STACKTRACE;
        }
        synchronized (this.mUsersLock) {
            if (getUserInfoLU(userId).canHaveProfile()) {
                int usersCountAfterRemoving = getAliveUsersExcludingGuestsCountLU() - profilesRemovedCount;
                if (usersCountAfterRemoving != WRITE_USER_MSG && usersCountAfterRemoving >= UserManager.getMaxSupportedUsers()) {
                    z = DBG_WITH_STACKTRACE;
                }
                return z;
            }
            return DBG_WITH_STACKTRACE;
        }
    }

    private int getAliveUsersExcludingGuestsCountLU() {
        int aliveUserCount = 0;
        int totalUserCount = this.mUsers.size();
        for (int i = 0; i < totalUserCount; i += WRITE_USER_MSG) {
            UserInfo user = ((UserData) this.mUsers.valueAt(i)).info;
            if (!(this.mRemovingUserIds.get(user.id) || user.isGuest() || user.partial)) {
                aliveUserCount += WRITE_USER_MSG;
            }
        }
        return aliveUserCount;
    }

    private static final void checkManageUserAndAcrossUsersFullPermission(String message) {
        int uid = Binder.getCallingUid();
        if (uid != ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE && uid != 0 && ActivityManager.checkComponentPermission("android.permission.MANAGE_USERS", uid, -1, true) != 0 && ActivityManager.checkComponentPermission("android.permission.INTERACT_ACROSS_USERS_FULL", uid, -1, true) != 0) {
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
        if ((creationFlags & -301) == 0) {
            if (!hasManageOrCreateUsersPermission()) {
                throw new SecurityException("You either need MANAGE_USERS or CREATE_USERS permission to create an user with flags: " + creationFlags);
            }
        } else if (!hasManageUsersPermission()) {
            throw new SecurityException("You need MANAGE_USERS permission to create an user  with flags: " + creationFlags);
        }
    }

    private static final boolean hasManageUsersPermission() {
        int callingUid = Binder.getCallingUid();
        if (UserHandle.isSameApp(callingUid, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) || callingUid == 0 || ActivityManager.checkComponentPermission("android.permission.MANAGE_USERS", callingUid, -1, true) == 0) {
            return true;
        }
        return DBG_WITH_STACKTRACE;
    }

    private static final boolean hasManageOrCreateUsersPermission() {
        int callingUid = Binder.getCallingUid();
        if (UserHandle.isSameApp(callingUid, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) || callingUid == 0 || ActivityManager.checkComponentPermission("android.permission.MANAGE_USERS", callingUid, -1, true) == 0 || ActivityManager.checkComponentPermission("android.permission.CREATE_USERS", callingUid, -1, true) == 0) {
            return true;
        }
        return DBG_WITH_STACKTRACE;
    }

    private static void checkSystemOrRoot(String message) {
        int uid = Binder.getCallingUid();
        if (!UserHandle.isSameApp(uid, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) && uid != 0) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readUserListLP() {
        if (this.mUserListFile.exists()) {
            AutoCloseable autoCloseable = null;
            try {
                int type;
                autoCloseable = new AtomicFile(this.mUserListFile).openRead();
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(autoCloseable, StandardCharsets.UTF_8.name());
                do {
                    type = parser.next();
                    if (type == 2) {
                        break;
                    }
                } while (type != WRITE_USER_MSG);
                if (type != 2) {
                    Slog.e(LOG_TAG, "Unable to read user list");
                    fallbackToSingleUserLP();
                    IoUtils.closeQuietly(autoCloseable);
                    return;
                }
                this.mNextSerialNumber = -1;
                if (parser.getName().equals(TAG_USERS)) {
                    String lastSerialNumber = parser.getAttributeValue(null, ATTR_NEXT_SERIAL_NO);
                    if (lastSerialNumber != null) {
                        this.mNextSerialNumber = Integer.parseInt(lastSerialNumber);
                    }
                    String versionNumber = parser.getAttributeValue(null, ATTR_USER_VERSION);
                    if (versionNumber != null) {
                        this.mUserVersion = Integer.parseInt(versionNumber);
                    }
                }
                Bundle newDevicePolicyGlobalUserRestrictions = new Bundle();
                while (true) {
                    type = parser.next();
                    if (type == WRITE_USER_MSG) {
                        break;
                    } else if (type == 2) {
                        String name = parser.getName();
                        if (name.equals(TAG_USER)) {
                            UserData userData = readUserLP(Integer.parseInt(parser.getAttributeValue(null, ATTR_ID)));
                            if (userData != null) {
                                synchronized (this.mUsersLock) {
                                    this.mUsers.put(userData.info.id, userData);
                                    if (this.mNextSerialNumber < 0 || this.mNextSerialNumber <= userData.info.id) {
                                        this.mNextSerialNumber = userData.info.id + WRITE_USER_MSG;
                                    }
                                }
                            } else if (this.mUsers.size() == 0) {
                                Slog.e(LOG_TAG, "Unable to read user 0 --try fallback");
                                fallbackToSingleUserLP();
                                IoUtils.closeQuietly(autoCloseable);
                                return;
                            }
                        } else if (name.equals(TAG_GUEST_RESTRICTIONS)) {
                            do {
                                type = parser.next();
                                if (!(type == WRITE_USER_MSG || type == 3)) {
                                }
                            } while (type != 2);
                            if (parser.getName().equals(TAG_RESTRICTIONS)) {
                                synchronized (this.mGuestRestrictions) {
                                    UserRestrictionsUtils.readRestrictions(parser, this.mGuestRestrictions);
                                }
                            } else if (parser.getName().equals(TAG_DEVICE_POLICY_RESTRICTIONS)) {
                                UserRestrictionsUtils.readRestrictions(parser, newDevicePolicyGlobalUserRestrictions);
                            }
                        } else if (name.equals(TAG_GLOBAL_RESTRICTION_OWNER_ID)) {
                            String ownerUserId = parser.getAttributeValue(null, ATTR_ID);
                            if (ownerUserId != null) {
                                this.mGlobalRestrictionOwnerUserId = Integer.parseInt(ownerUserId);
                            }
                        }
                    }
                }
                synchronized (this.mRestrictionsLock) {
                    this.mDevicePolicyGlobalUserRestrictions = newDevicePolicyGlobalUserRestrictions;
                }
                updateUserIds();
                upgradeIfNecessaryLP();
                IoUtils.closeQuietly(autoCloseable);
            } catch (IOException e) {
                fallbackToSingleUserLP();
                IoUtils.closeQuietly(autoCloseable);
            } catch (Exception e2) {
                Slog.e(LOG_TAG, "Unable to read user list, error: " + e2);
                fallbackToSingleUserLP();
                IoUtils.closeQuietly(autoCloseable);
            } catch (Throwable th) {
                IoUtils.closeQuietly(autoCloseable);
            }
        } else {
            fallbackToSingleUserLP();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void upgradeIfNecessaryLP() {
        UserData userData;
        int originalVersion = this.mUserVersion;
        int userVersion = this.mUserVersion;
        if (userVersion < WRITE_USER_MSG) {
            userData = getUserDataNoChecks(0);
            if ("Primary".equals(userData.info.name)) {
                userData.info.name = this.mContext.getResources().getString(17040668);
                scheduleWriteUser(userData);
            }
            userVersion = WRITE_USER_MSG;
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
        if (userVersion < USER_VERSION) {
            boolean splitSystemUser = UserManager.isSplitSystemUser();
            synchronized (this.mUsersLock) {
                int i = 0;
                while (true) {
                    if (i >= this.mUsers.size()) {
                        break;
                    }
                    userData = (UserData) this.mUsers.valueAt(i);
                    if (!splitSystemUser && userData.info.isRestricted() && userData.info.restrictedProfileParentId == -10000) {
                        userData.info.restrictedProfileParentId = 0;
                        scheduleWriteUser(userData);
                    }
                    i += WRITE_USER_MSG;
                }
            }
            userVersion = USER_VERSION;
        }
        if (userVersion < USER_VERSION) {
            Slog.w(LOG_TAG, "User version " + this.mUserVersion + " didn't upgrade as expected to " + USER_VERSION);
            return;
        }
        this.mUserVersion = userVersion;
        if (originalVersion < this.mUserVersion) {
            writeUserListLP();
        }
    }

    private void fallbackToSingleUserLP() {
        int flags = 16;
        if (!UserManager.isSplitSystemUser()) {
            flags = 19;
        }
        UserInfo system = new UserInfo(0, null, null, flags);
        UserData userData = new UserData();
        userData.info = system;
        synchronized (this.mUsersLock) {
            this.mUsers.put(system.id, userData);
        }
        this.mNextSerialNumber = MIN_USER_ID;
        this.mUserVersion = USER_VERSION;
        Bundle restrictions = new Bundle();
        synchronized (this.mRestrictionsLock) {
            this.mBaseUserRestrictions.append(0, restrictions);
        }
        updateUserIds();
        initDefaultGuestRestrictions();
        writeUserLP(userData);
        writeUserListLP();
    }

    private String getOwnerName() {
        return this.mContext.getResources().getString(17040668);
    }

    private void scheduleWriteUser(UserData UserData) {
        if (!this.mHandler.hasMessages(WRITE_USER_MSG, UserData)) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(WRITE_USER_MSG, UserData), 2000);
        }
    }

    private void writeUserLP(UserData userData) {
        FileOutputStream fileOutputStream = null;
        AtomicFile userFile = new AtomicFile(new File(this.mUsersDir, userData.info.id + XML_SUFFIX));
        try {
            fileOutputStream = userFile.startWrite();
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(bos, StandardCharsets.UTF_8.name());
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
            userFile.finishWrite(fileOutputStream);
        } catch (Exception ioe) {
            Slog.e(LOG_TAG, "Error writing user info " + userData.info.id, ioe);
            userFile.failWrite(fileOutputStream);
        }
    }

    private void writeUserListLP() {
        FileOutputStream fileOutputStream = null;
        AtomicFile userListFile = new AtomicFile(this.mUserListFile);
        try {
            int[] userIdsToWrite;
            fileOutputStream = userListFile.startWrite();
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(bos, StandardCharsets.UTF_8.name());
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, TAG_USERS);
            serializer.attribute(null, ATTR_NEXT_SERIAL_NO, Integer.toString(this.mNextSerialNumber));
            serializer.attribute(null, ATTR_USER_VERSION, Integer.toString(this.mUserVersion));
            serializer.startTag(null, TAG_GUEST_RESTRICTIONS);
            synchronized (this.mGuestRestrictions) {
                UserRestrictionsUtils.writeRestrictions(serializer, this.mGuestRestrictions, TAG_RESTRICTIONS);
            }
            serializer.endTag(null, TAG_GUEST_RESTRICTIONS);
            synchronized (this.mRestrictionsLock) {
                UserRestrictionsUtils.writeRestrictions(serializer, this.mDevicePolicyGlobalUserRestrictions, TAG_DEVICE_POLICY_RESTRICTIONS);
            }
            serializer.startTag(null, TAG_GLOBAL_RESTRICTION_OWNER_ID);
            serializer.attribute(null, ATTR_ID, Integer.toString(this.mGlobalRestrictionOwnerUserId));
            serializer.endTag(null, TAG_GLOBAL_RESTRICTION_OWNER_ID);
            synchronized (this.mUsersLock) {
                userIdsToWrite = new int[this.mUsers.size()];
                for (int i = 0; i < userIdsToWrite.length; i += WRITE_USER_MSG) {
                    userIdsToWrite[i] = ((UserData) this.mUsers.valueAt(i)).info.id;
                }
            }
            int length = userIdsToWrite.length;
            for (int i2 = 0; i2 < length; i2 += WRITE_USER_MSG) {
                int id = userIdsToWrite[i2];
                serializer.startTag(null, TAG_USER);
                serializer.attribute(null, ATTR_ID, Integer.toString(id));
                serializer.endTag(null, TAG_USER);
            }
            serializer.endTag(null, TAG_USERS);
            serializer.endDocument();
            userListFile.finishWrite(fileOutputStream);
        } catch (Exception e) {
            userListFile.failWrite(fileOutputStream);
            Slog.e(LOG_TAG, "Error writing user list");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private UserData readUserLP(int id) {
        int flags = 0;
        int serialNumber = id;
        String str = null;
        String str2 = null;
        String str3 = null;
        long creationTime = 0;
        long lastLoggedInTime = 0;
        String str4 = null;
        int profileGroupId = -10000;
        int restrictedProfileParentId = -10000;
        boolean partial = DBG_WITH_STACKTRACE;
        boolean guestToRemove = DBG_WITH_STACKTRACE;
        boolean persistSeedData = DBG_WITH_STACKTRACE;
        String str5 = null;
        String str6 = null;
        PersistableBundle persistableBundle = null;
        Bundle baseRestrictions = new Bundle();
        Bundle localRestrictions = new Bundle();
        FileInputStream fileInputStream = null;
        try {
            int type;
            fileInputStream = new AtomicFile(new File(this.mUsersDir, Integer.toString(id) + XML_SUFFIX)).openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fileInputStream, StandardCharsets.UTF_8.name());
            do {
                type = parser.next();
                if (type == 2) {
                    break;
                }
            } while (type != WRITE_USER_MSG);
            if (type != 2) {
                Slog.e(LOG_TAG, "Unable to read user " + id);
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                    }
                }
                return null;
            }
            if (type == 2) {
                if (parser.getName().equals(TAG_USER)) {
                    if (readIntAttribute(parser, ATTR_ID, -1) != id) {
                        Slog.e(LOG_TAG, "User id does not match the file name");
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e2) {
                            }
                        }
                        return null;
                    }
                    serialNumber = readIntAttribute(parser, ATTR_SERIAL_NO, id);
                    flags = readIntAttribute(parser, ATTR_FLAGS, 0);
                    str3 = parser.getAttributeValue(null, ATTR_ICON_PATH);
                    creationTime = readLongAttribute(parser, ATTR_CREATION_TIME, 0);
                    lastLoggedInTime = readLongAttribute(parser, ATTR_LAST_LOGGED_IN_TIME, 0);
                    str4 = parser.getAttributeValue(null, ATTR_LAST_LOGGED_IN_FINGERPRINT);
                    profileGroupId = readIntAttribute(parser, ATTR_PROFILE_GROUP_ID, -10000);
                    restrictedProfileParentId = readIntAttribute(parser, ATTR_RESTRICTED_PROFILE_PARENT_ID, -10000);
                    if ("true".equals(parser.getAttributeValue(null, ATTR_PARTIAL))) {
                        partial = true;
                    }
                    if ("true".equals(parser.getAttributeValue(null, ATTR_GUEST_TO_REMOVE))) {
                        guestToRemove = true;
                    }
                    str5 = parser.getAttributeValue(null, ATTR_SEED_ACCOUNT_NAME);
                    str6 = parser.getAttributeValue(null, ATTR_SEED_ACCOUNT_TYPE);
                    if (!(str5 == null && str6 == null)) {
                        persistSeedData = true;
                    }
                    int outerDepth = parser.getDepth();
                    while (true) {
                        type = parser.next();
                        if (type != WRITE_USER_MSG && (type != 3 || parser.getDepth() > outerDepth)) {
                            if (!(type == 3 || type == 4)) {
                                String tag = parser.getName();
                                if (!TAG_NAME.equals(tag)) {
                                    if (TAG_RESTRICTIONS.equals(tag)) {
                                        UserRestrictionsUtils.readRestrictions(parser, baseRestrictions);
                                    } else {
                                        if (TAG_DEVICE_POLICY_RESTRICTIONS.equals(tag)) {
                                            UserRestrictionsUtils.readRestrictions(parser, localRestrictions);
                                        } else {
                                            if (!TAG_ACCOUNT.equals(tag)) {
                                                if (TAG_SEED_ACCOUNT_OPTIONS.equals(tag)) {
                                                    persistableBundle = PersistableBundle.restoreFromXml(parser);
                                                    persistSeedData = true;
                                                }
                                            } else if (parser.next() == 4) {
                                                str2 = parser.getText();
                                            }
                                        }
                                    }
                                } else if (parser.next() == 4) {
                                    str = parser.getText();
                                }
                            }
                        }
                    }
                }
            }
            UserInfo userInfo = new UserInfo(id, str, str3, flags);
            userInfo.serialNumber = serialNumber;
            userInfo.creationTime = creationTime;
            userInfo.lastLoggedInTime = lastLoggedInTime;
            userInfo.lastLoggedInFingerprint = str4;
            userInfo.partial = partial;
            userInfo.guestToRemove = guestToRemove;
            userInfo.profileGroupId = profileGroupId;
            userInfo.restrictedProfileParentId = restrictedProfileParentId;
            UserData userData = new UserData();
            userData.info = userInfo;
            userData.account = str2;
            userData.seedAccountName = str5;
            userData.seedAccountType = str6;
            userData.persistSeedData = persistSeedData;
            userData.seedAccountOptions = persistableBundle;
            synchronized (this.mRestrictionsLock) {
                this.mBaseUserRestrictions.put(id, baseRestrictions);
                this.mDevicePolicyLocalUserRestrictions.put(id, localRestrictions);
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e3) {
                }
            }
            return userData;
        } catch (IOException e4) {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e5) {
                }
            }
        } catch (XmlPullParserException e6) {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e7) {
                }
            }
        } catch (Throwable th) {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e8) {
                }
            }
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

    private void cleanAppRestrictionsForPackage(String pkg, int userId) {
        synchronized (this.mPackagesLock) {
            File resFile = new File(Environment.getUserSystemDirectory(userId), packageToRestrictionsFileName(pkg));
            if (resFile.exists()) {
                resFile.delete();
            }
        }
    }

    public UserInfo createProfileForUser(String name, int flags, int userId) {
        checkManageOrCreateUsersPermission(flags);
        return createUserInternal(name, flags, userId);
    }

    public UserInfo createUser(String name, int flags) {
        checkManageOrCreateUsersPermission(flags);
        return createUserInternal(name, flags, -10000);
    }

    private UserInfo createUserInternal(String name, int flags, int parentId) {
        if (!hasUserRestriction("no_add_user", UserHandle.getCallingUserId())) {
            return createUserInternalUnchecked(name, flags, parentId);
        }
        Log.w(LOG_TAG, "Cannot add user. DISALLOW_ADD_USER is enabled.");
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private UserInfo createUserInternalUnchecked(String name, int flags, int parentId) {
        if (ActivityManager.isLowRamDeviceStatic()) {
            return null;
        }
        boolean isGuest = (flags & 4) != 0 ? true : DBG_WITH_STACKTRACE;
        boolean isManagedProfile = (flags & 32) != 0 ? true : DBG_WITH_STACKTRACE;
        boolean isRestricted = (flags & 8) != 0 ? true : DBG_WITH_STACKTRACE;
        long ident = Binder.clearCallingIdentity();
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
                    if (!canAddMoreManagedProfiles(parentId, DBG_WITH_STACKTRACE)) {
                        Log.e(LOG_TAG, "Cannot add more managed profiles for user " + parentId);
                        Binder.restoreCallingIdentity(ident);
                        return null;
                    }
                }
                if (!(isGuest || isManagedProfile)) {
                    if (isUserLimitReached()) {
                        Binder.restoreCallingIdentity(ident);
                        return null;
                    }
                }
                if (!isGuest || findCurrentGuestUser() == null) {
                    if (isRestricted) {
                        if (!(UserManager.isSplitSystemUser() || parentId == 0)) {
                            Log.w(LOG_TAG, "Cannot add restricted profile - parent user must be owner");
                            Binder.restoreCallingIdentity(ident);
                            return null;
                        }
                    }
                    if (isRestricted && UserManager.isSplitSystemUser()) {
                        if (userData == null) {
                            Log.w(LOG_TAG, "Cannot add restricted profile - parent user must be specified");
                            Binder.restoreCallingIdentity(ident);
                            return null;
                        }
                        if (!userData.info.canHaveProfile()) {
                            Log.w(LOG_TAG, "Cannot add restricted profile - profiles cannot be created for the specified parent user id " + parentId);
                            Binder.restoreCallingIdentity(ident);
                            return null;
                        }
                    }
                    if (UserManager.isSplitSystemUser() || (flags & DumpState.DUMP_SHARED_USERS) == 0) {
                        UserInfo userInfo;
                        int i;
                        UserData userData2;
                        if (!(!UserManager.isSplitSystemUser() || isGuest || isManagedProfile)) {
                            if (getPrimaryUser() == null) {
                                flags |= WRITE_USER_MSG;
                                synchronized (this.mUsersLock) {
                                    if (!this.mIsDeviceManaged) {
                                        flags |= 2;
                                    }
                                }
                            }
                        }
                        int userId = getNextAvailableId();
                        Environment.getUserSystemDirectory(userId).mkdirs();
                        boolean ephemeralGuests = Resources.getSystem().getBoolean(17957038);
                        synchronized (this.mUsersLock) {
                            long now;
                            if (!(isGuest && ephemeralGuests)) {
                                if (!this.mForceEphemeralUsers) {
                                    if (userData != null) {
                                    }
                                    userInfo = new UserInfo(userId, name, null, flags);
                                    i = this.mNextSerialNumber;
                                    this.mNextSerialNumber = i + WRITE_USER_MSG;
                                    userInfo.serialNumber = i;
                                    now = System.currentTimeMillis();
                                    if (now <= EPOCH_PLUS_30_YEARS) {
                                        now = 0;
                                    }
                                    userInfo.creationTime = now;
                                    userInfo.partial = true;
                                    userInfo.lastLoggedInFingerprint = Build.FINGERPRINT;
                                    userData2 = new UserData();
                                    userData2.info = userInfo;
                                    this.mUsers.put(userId, userData2);
                                }
                            }
                            flags |= DumpState.DUMP_SHARED_USERS;
                            userInfo = new UserInfo(userId, name, null, flags);
                            i = this.mNextSerialNumber;
                            this.mNextSerialNumber = i + WRITE_USER_MSG;
                            userInfo.serialNumber = i;
                            now = System.currentTimeMillis();
                            if (now <= EPOCH_PLUS_30_YEARS) {
                                now = 0;
                            }
                            userInfo.creationTime = now;
                            userInfo.partial = true;
                            userInfo.lastLoggedInFingerprint = Build.FINGERPRINT;
                            userData2 = new UserData();
                            userData2.info = userInfo;
                            this.mUsers.put(userId, userData2);
                        }
                        writeUserLP(userData2);
                        writeUserListLP();
                        if (userData != null) {
                            if (isManagedProfile) {
                                i = userData.info.profileGroupId;
                                if (r0 == -10000) {
                                    userData.info.profileGroupId = userData.info.id;
                                    writeUserLP(userData);
                                }
                                userInfo.profileGroupId = userData.info.profileGroupId;
                            } else if (isRestricted) {
                                i = userData.info.restrictedProfileParentId;
                                if (r0 == -10000) {
                                    userData.info.restrictedProfileParentId = userData.info.id;
                                    writeUserLP(userData);
                                }
                                userInfo.restrictedProfileParentId = userData.info.restrictedProfileParentId;
                            }
                        }
                        ((StorageManager) this.mContext.getSystemService(StorageManager.class)).createUserKey(userId, userInfo.serialNumber, userInfo.isEphemeral());
                        this.mPm.prepareUserData(userId, userInfo.serialNumber, 3);
                        this.mPm.createNewUser(userId);
                        userInfo.partial = DBG_WITH_STACKTRACE;
                        synchronized (this.mPackagesLock) {
                            writeUserLP(userData2);
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
                        HwThemeManager.applyDefaultHwTheme(DBG_WITH_STACKTRACE, this.mContext, userId);
                        Intent addedIntent = new Intent("android.intent.action.USER_ADDED");
                        addedIntent.putExtra("android.intent.extra.user_handle", userId);
                        this.mContext.sendBroadcastAsUser(addedIntent, UserHandle.ALL, "android.permission.MANAGE_USERS");
                        MetricsLogger.count(this.mContext, isGuest ? TRON_GUEST_CREATED : TRON_USER_CREATED, WRITE_USER_MSG);
                        Binder.restoreCallingIdentity(ident);
                        return userInfo;
                    }
                    Log.e(LOG_TAG, "Ephemeral users are supported on split-system-user systems only.");
                    Binder.restoreCallingIdentity(ident);
                    return null;
                }
                Binder.restoreCallingIdentity(ident);
                return null;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public UserInfo createRestrictedProfile(String name, int parentUserId) {
        checkManageOrCreateUsersPermission("setupRestrictedProfile");
        UserInfo user = createProfileForUser(name, 8, parentUserId);
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
                if (!user.isGuest() || user.guestToRemove || this.mRemovingUserIds.get(user.id)) {
                    i += WRITE_USER_MSG;
                } else {
                    return user;
                }
            }
            return null;
        }
    }

    public boolean markGuestForDeletion(int userHandle) {
        checkManageUsersPermission("Only the system can remove users");
        if (getUserRestrictions(UserHandle.getCallingUserId()).getBoolean("no_remove_user", DBG_WITH_STACKTRACE)) {
            Log.w(LOG_TAG, "Cannot remove user. DISALLOW_REMOVE_USER is enabled.");
            return DBG_WITH_STACKTRACE;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mPackagesLock) {
                synchronized (this.mUsersLock) {
                    UserData userData = (UserData) this.mUsers.get(userHandle);
                    if (!(userHandle == 0 || userData == null)) {
                        if (!this.mRemovingUserIds.get(userHandle)) {
                            if (userData.info.isGuest()) {
                                userData.info.guestToRemove = true;
                                UserInfo userInfo = userData.info;
                                userInfo.flags |= 64;
                                writeUserLP(userData);
                                Binder.restoreCallingIdentity(ident);
                                return true;
                            }
                            Binder.restoreCallingIdentity(ident);
                            return DBG_WITH_STACKTRACE;
                        }
                    }
                    return DBG_WITH_STACKTRACE;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean removeUser(int userHandle) {
        boolean z = true;
        checkManageOrCreateUsersPermission("Only the system can remove users");
        Slog.i(LOG_TAG, "removeUser " + userHandle + ", callingUid = " + Binder.getCallingUid() + ", callingPid = " + Binder.getCallingPid());
        if (getUserRestrictions(UserHandle.getCallingUserId()).getBoolean("no_remove_user", DBG_WITH_STACKTRACE)) {
            Log.w(LOG_TAG, "Cannot remove user. DISALLOW_REMOVE_USER is enabled.");
            return DBG_WITH_STACKTRACE;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            if (ActivityManager.getCurrentUser() == userHandle) {
                Log.w(LOG_TAG, "Current user cannot be removed");
                return DBG_WITH_STACKTRACE;
            }
            synchronized (this.mPackagesLock) {
                synchronized (this.mUsersLock) {
                    UserData userData = (UserData) this.mUsers.get(userHandle);
                    if (!(userHandle == 0 || userData == null)) {
                        if (!this.mRemovingUserIds.get(userHandle)) {
                            this.mRemovingUserIds.put(userHandle, true);
                            try {
                                this.mAppOpsService.removeUser(userHandle);
                            } catch (RemoteException e) {
                                Log.w(LOG_TAG, "Unable to notify AppOpsService of removing user", e);
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
                                if (ActivityManagerNative.getDefault().stopUser(userHandle, true, new IStopUserCallback.Stub() {
                                    public void userStopped(int userId) {
                                        UserManagerService.this.finishRemoveUser(userId);
                                    }

                                    public void userStopAborted(int userId) {
                                    }
                                }) != 0) {
                                    z = DBG_WITH_STACKTRACE;
                                }
                                Binder.restoreCallingIdentity(ident);
                                return z;
                            } catch (RemoteException e2) {
                                Binder.restoreCallingIdentity(ident);
                                return DBG_WITH_STACKTRACE;
                            }
                        }
                    }
                    Flog.i(900, "Removing user stopped, userHandle " + userHandle + " user " + userData);
                    Binder.restoreCallingIdentity(ident);
                    return DBG_WITH_STACKTRACE;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    void finishRemoveUser(int userHandle) {
        Flog.i(900, "finishRemoveUser " + userHandle);
        long ident = Binder.clearCallingIdentity();
        try {
            Intent addedIntent = new Intent("android.intent.action.USER_REMOVED");
            addedIntent.putExtra("android.intent.extra.user_handle", userHandle);
            this.mContext.sendOrderedBroadcastAsUser(addedIntent, UserHandle.ALL, "android.permission.MANAGE_USERS", new AnonymousClass5(userHandle), null, -1, null, null);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void removeUserState(int userHandle) {
        try {
            IGateKeeperService gk = GateKeeper.getService();
            if (gk != null) {
                gk.clearSecureUserId(userHandle);
            }
        } catch (Exception e) {
            Slog.w(LOG_TAG, "unable to clear GK secure user id");
        }
        this.mPm.cleanUpUser(this, userHandle);
        this.mPm.destroyUserData(userHandle, 3);
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
        }
        synchronized (this.mPackagesLock) {
            writeUserListLP();
        }
        new AtomicFile(new File(this.mUsersDir, userHandle + XML_SUFFIX)).delete();
        updateUserIds();
        try {
            ((StorageManager) this.mContext.getSystemService(StorageManager.class)).destroyUserKey(userHandle);
        } catch (IllegalStateException e2) {
            Slog.i(LOG_TAG, "Destroying key for user " + userHandle + " failed, continuing anyway", e2);
            Flog.i(900, "removeUserStateLocked started user is " + userHandle);
        }
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
        if (!(UserHandle.getCallingUserId() == userId && UserHandle.isSameApp(Binder.getCallingUid(), getUidForPackage(packageName)))) {
            checkSystemOrRoot("get application restrictions for other users/apps");
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
            i = this.mContext.getPackageManager().getApplicationInfo(packageName, DumpState.DUMP_PREFERRED_XML).uid;
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
            while (parser.next() != WRITE_USER_MSG) {
                readEntry(restrictions, values, parser);
            }
            IoUtils.closeQuietly(autoCloseable);
            return restrictions;
        } catch (Exception e) {
            Log.w(LOG_TAG, "Error parsing " + restrictionsFile.getBaseFile(), e);
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    private static void readEntry(Bundle restrictions, ArrayList<String> values, XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() == 2 && parser.getName().equals(TAG_ENTRY)) {
            String key = parser.getAttributeValue(null, ATTR_KEY);
            String valType = parser.getAttributeValue(null, ATTR_VALUE_TYPE);
            String multiple = parser.getAttributeValue(null, ATTR_MULTIPLE);
            if (multiple != null) {
                values.clear();
                int count = Integer.parseInt(multiple);
                while (count > 0) {
                    int type = parser.next();
                    if (type == WRITE_USER_MSG) {
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
            if (value instanceof Boolean) {
                serializer.attribute(null, ATTR_VALUE_TYPE, ATTR_TYPE_BOOLEAN);
                serializer.text(value.toString());
            } else if (value instanceof Integer) {
                serializer.attribute(null, ATTR_VALUE_TYPE, ATTR_TYPE_INTEGER);
                serializer.text(value.toString());
            } else if (value == null || (value instanceof String)) {
                serializer.attribute(null, ATTR_VALUE_TYPE, ATTR_TYPE_STRING);
                serializer.text(value != null ? (String) value : "");
            } else if (value instanceof Bundle) {
                serializer.attribute(null, ATTR_VALUE_TYPE, ATTR_TYPE_BUNDLE);
                writeBundle((Bundle) value, serializer);
            } else if (value instanceof Parcelable[]) {
                serializer.attribute(null, ATTR_VALUE_TYPE, ATTR_TYPE_BUNDLE_ARRAY);
                Parcelable[] array = (Parcelable[]) value;
                r9 = array.length;
                r7 = 0;
                while (r7 < r9) {
                    Parcelable parcelable = array[r7];
                    if (parcelable instanceof Bundle) {
                        serializer.startTag(null, TAG_ENTRY);
                        serializer.attribute(null, ATTR_VALUE_TYPE, ATTR_TYPE_BUNDLE);
                        writeBundle((Bundle) parcelable, serializer);
                        serializer.endTag(null, TAG_ENTRY);
                        r7 += WRITE_USER_MSG;
                    } else {
                        throw new IllegalArgumentException("bundle-array can only hold Bundles");
                    }
                }
                continue;
            } else {
                serializer.attribute(null, ATTR_VALUE_TYPE, ATTR_TYPE_STRING_ARRAY);
                String[] values = (String[]) value;
                serializer.attribute(null, ATTR_MULTIPLE, Integer.toString(values.length));
                r9 = values.length;
                for (r7 = 0; r7 < r9; r7 += WRITE_USER_MSG) {
                    String choice = values[r7];
                    serializer.startTag(null, TAG_VALUE);
                    if (choice == null) {
                        choice = "";
                    }
                    serializer.text(choice);
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

    public int getUserHandle(int userSerialNumber) {
        synchronized (this.mUsersLock) {
            int[] iArr = this.mUserIds;
            int i = 0;
            int length = iArr.length;
            while (i < length) {
                int userId = iArr[i];
                UserInfo info = getUserInfoLU(userId);
                if (info == null || info.serialNumber != userSerialNumber) {
                    i += WRITE_USER_MSG;
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
            for (i = 0; i < userSize; i += WRITE_USER_MSG) {
                if (!((UserData) this.mUsers.valueAt(i)).info.partial) {
                    num += WRITE_USER_MSG;
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
                    n2 = n + WRITE_USER_MSG;
                    newUsers[n] = this.mUsers.keyAt(i);
                }
                i += WRITE_USER_MSG;
                n = n2;
            }
            this.mUserIds = newUsers;
        }
    }

    public void onBeforeStartUser(int userId) {
        this.mPm.prepareUserData(userId, getUserSerialNumber(userId), WRITE_USER_MSG);
        this.mPm.reconcileAppsData(userId, WRITE_USER_MSG);
        if (userId != 0) {
            synchronized (this.mRestrictionsLock) {
                applyUserRestrictionsLR(userId);
            }
            UserInfo userInfo = getUserInfoNoChecks(userId);
            if (userInfo != null && !userInfo.isInitialized()) {
                this.mPm.onBeforeUserStartUninitialized(userId);
            }
        }
    }

    public void onBeforeUnlockUser(int userId) {
        this.mPm.prepareUserData(userId, getUserSerialNumber(userId), 2);
        this.mPm.reconcileAppsData(userId, 2);
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

    private int getNextAvailableId() {
        synchronized (this.mUsersLock) {
            int i = MIN_USER_ID;
            while (i < MAX_USER_ID) {
                if (this.mUsers.indexOfKey(i) >= 0 || this.mRemovingUserIds.get(i)) {
                    i += WRITE_USER_MSG;
                } else {
                    return i;
                }
            }
            throw new IllegalStateException("No user id available!");
        }
    }

    private String packageToRestrictionsFileName(String packageName) {
        return RESTRICTIONS_FILE_PREFIX + packageName + XML_SUFFIX;
    }

    public static void enforceSerialNumber(File file, int serialNumber) throws IOException {
        int foundSerial = getSerialNumber(file);
        Slog.v(LOG_TAG, "Found " + file + " with serial number " + foundSerial);
        if (foundSerial == -1) {
            Slog.d(LOG_TAG, "Serial number missing on " + file + "; assuming current is valid");
            try {
                setSerialNumber(file, serialNumber);
            } catch (IOException e) {
                Slog.w(LOG_TAG, "Failed to set serial number on " + file, e);
            }
        } else if (foundSerial != serialNumber) {
            throw new IOException("Found serial number " + foundSerial + " doesn't match expected " + serialNumber);
        }
    }

    private static void setSerialNumber(File file, int serialNumber) throws IOException {
        try {
            Os.setxattr(file.getAbsolutePath(), XATTR_SERIAL, Integer.toString(serialNumber).getBytes(StandardCharsets.UTF_8), OsConstants.XATTR_CREATE);
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    private static int getSerialNumber(File file) throws IOException {
        String serial;
        try {
            byte[] buf = new byte[DumpState.DUMP_SHARED_USERS];
            serial = new String(buf, 0, Os.getxattr(file.getAbsolutePath(), XATTR_SERIAL, buf));
            return Integer.parseInt(serial);
        } catch (NumberFormatException e) {
            throw new IOException("Bad serial number: " + serial);
        } catch (ErrnoException e2) {
            if (e2.errno == OsConstants.ENODATA) {
                return -1;
            }
            throw e2.rethrowAsIOException();
        }
    }

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
                if (persist) {
                    writeUserLP(userData);
                }
                return;
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
                return;
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
                if (data.info.isInitialized() || data.seedAccountName == null || !data.seedAccountName.equals(accountName) || data.seedAccountType == null || !data.seedAccountType.equals(accountType)) {
                    i += WRITE_USER_MSG;
                } else {
                    return true;
                }
            }
            return DBG_WITH_STACKTRACE;
        }
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) {
        new Shell().exec(this, in, out, err, args, resultReceiver);
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
        IActivityManager am = ActivityManagerNative.getDefault();
        List<UserInfo> users = getUsers(DBG_WITH_STACKTRACE);
        if (users == null) {
            pw.println("Error: couldn't get users");
            return WRITE_USER_MSG;
        }
        pw.println("Users:");
        for (int i = 0; i < users.size(); i += WRITE_USER_MSG) {
            pw.println("\t" + ((UserInfo) users.get(i)).toString() + (am.isUserRunning(((UserInfo) users.get(i)).id, 0) ? " running" : ""));
        }
        return 0;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump UserManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
            return;
        }
        long now = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        synchronized (this.mPackagesLock) {
            synchronized (this.mUsersLock) {
                pw.println("Users:");
                for (int i = 0; i < this.mUsers.size(); i += WRITE_USER_MSG) {
                    UserData userData = (UserData) this.mUsers.valueAt(i);
                    if (userData != null) {
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
            pw.println("  Device policy global restrictions:");
            synchronized (this.mRestrictionsLock) {
                PrintWriter printWriter = pw;
                UserRestrictionsUtils.dumpRestrictions(printWriter, "    ", this.mDevicePolicyGlobalUserRestrictions);
            }
            pw.println();
            pw.println("  Global restrictions owner id:" + this.mGlobalRestrictionOwnerUserId);
            pw.println();
            pw.println("  Guest restrictions:");
            synchronized (this.mGuestRestrictions) {
                printWriter = pw;
                UserRestrictionsUtils.dumpRestrictions(printWriter, "    ", this.mGuestRestrictions);
            }
            synchronized (this.mUsersLock) {
                pw.println();
                pw.println("  Device managed: " + this.mIsDeviceManaged);
            }
            synchronized (this.mUserStates) {
                pw.println("  Started users state: " + this.mUserStates);
            }
            pw.println();
            pw.println("  Max users: " + UserManager.getMaxSupportedUsers());
            pw.println("  Supports switchable users: " + UserManager.supportsMultipleUsers());
            pw.println("  All guests ephemeral: " + Resources.getSystem().getBoolean(17957038));
        }
    }

    boolean isInitialized(int userId) {
        return (getUserInfo(userId).flags & 16) != 0 ? true : DBG_WITH_STACKTRACE;
    }

    private void removeNonSystemUsers() {
        ArrayList<UserInfo> usersToRemove = new ArrayList();
        synchronized (this.mUsersLock) {
            int userSize = this.mUsers.size();
            for (int i = 0; i < userSize; i += WRITE_USER_MSG) {
                UserInfo ui = ((UserData) this.mUsers.valueAt(i)).info;
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
}

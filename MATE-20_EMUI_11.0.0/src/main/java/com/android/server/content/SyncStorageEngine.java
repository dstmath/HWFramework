package com.android.server.content;

import android.accounts.Account;
import android.accounts.AccountAndUser;
import android.accounts.AccountManager;
import android.app.backup.BackupManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ISyncStatusObserver;
import android.content.PeriodicSync;
import android.content.SyncInfo;
import android.content.SyncRequest;
import android.content.SyncStatusInfo;
import android.content.pm.PackageManager;
import android.hdm.HwDeviceManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.EventLog;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import android.widget.Toast;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.IntPair;
import com.android.server.UiModeManagerService;
import com.android.server.UiThread;
import com.android.server.content.SyncManager;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.Settings;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.voiceinteraction.DatabaseHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class SyncStorageEngine {
    private static final int ACCOUNTS_VERSION = 3;
    private static final double DEFAULT_FLEX_PERCENT_SYNC = 0.04d;
    private static final long DEFAULT_MIN_FLEX_ALLOWED_SECS = 5;
    private static final long DEFAULT_POLL_FREQUENCY_SECONDS = 86400;
    public static final int EVENT_START = 0;
    public static final int EVENT_STOP = 1;
    public static final int MAX_HISTORY = 100;
    public static final String MESG_CANCELED = "canceled";
    public static final String MESG_SUCCESS = "success";
    @VisibleForTesting
    static final long MILLIS_IN_4WEEKS = 2419200000L;
    private static final int MSG_WRITE_STATISTICS = 2;
    private static final int MSG_WRITE_STATUS = 1;
    public static final long NOT_IN_BACKOFF_MODE = -1;
    public static final String[] SOURCES = {"OTHER", "LOCAL", "POLL", "USER", "PERIODIC", "FEED"};
    public static final int SOURCE_FEED = 5;
    public static final int SOURCE_LOCAL = 1;
    public static final int SOURCE_OTHER = 0;
    public static final int SOURCE_PERIODIC = 4;
    public static final int SOURCE_POLL = 2;
    public static final int SOURCE_USER = 3;
    public static final int STATISTICS_FILE_END = 0;
    public static final int STATISTICS_FILE_ITEM = 101;
    public static final int STATISTICS_FILE_ITEM_OLD = 100;
    public static final int STATUS_FILE_END = 0;
    public static final int STATUS_FILE_ITEM = 100;
    private static final boolean SYNC_ENABLED_DEFAULT = false;
    private static final String TAG = "SyncManager";
    private static final String TAG_FILE = "SyncManagerFile";
    private static final long WRITE_STATISTICS_DELAY = 1800000;
    private static final long WRITE_STATUS_DELAY = 600000;
    private static final String XML_ATTR_ENABLED = "enabled";
    private static final String XML_ATTR_LISTEN_FOR_TICKLES = "listen-for-tickles";
    private static final String XML_ATTR_NEXT_AUTHORITY_ID = "nextAuthorityId";
    private static final String XML_ATTR_SYNC_RANDOM_OFFSET = "offsetInSeconds";
    private static final String XML_ATTR_USER = "user";
    private static final String XML_TAG_LISTEN_FOR_TICKLES = "listenForTickles";
    private static PeriodicSyncAddedListener mPeriodicSyncAddedListener;
    private static HashMap<String, String> sAuthorityRenames = new HashMap<>();
    private static volatile SyncStorageEngine sSyncStorageEngine = null;
    private final AtomicFile mAccountInfoFile;
    private final HashMap<AccountAndUser, AccountInfo> mAccounts = new HashMap<>();
    private final SparseArray<AuthorityInfo> mAuthorities = new SparseArray<>();
    private OnAuthorityRemovedListener mAuthorityRemovedListener;
    private final Calendar mCal;
    private final RemoteCallbackList<ISyncStatusObserver> mChangeListeners = new RemoteCallbackList<>();
    private final Context mContext;
    private final SparseArray<ArrayList<SyncInfo>> mCurrentSyncs = new SparseArray<>();
    private final DayStats[] mDayStats = new DayStats[28];
    private boolean mDefaultMasterSyncAutomatically;
    private boolean mGrantSyncAdaptersAccountAccess;
    private final MyHandler mHandler;
    private volatile boolean mIsClockValid;
    private final SyncLogger mLogger;
    private SparseArray<Boolean> mMasterSyncAutomatically = new SparseArray<>();
    private int mNextAuthorityId = 0;
    private int mNextHistoryId = 0;
    private final ArrayMap<ComponentName, SparseArray<AuthorityInfo>> mServices = new ArrayMap<>();
    private final AtomicFile mStatisticsFile;
    private final AtomicFile mStatusFile;
    private final ArrayList<SyncHistoryItem> mSyncHistory = new ArrayList<>();
    private int mSyncRandomOffset;
    private OnSyncRequestListener mSyncRequestListener;
    private final SparseArray<SyncStatusInfo> mSyncStatus = new SparseArray<>();
    private int mYear;
    private int mYearInDays;

    /* access modifiers changed from: package-private */
    public interface OnAuthorityRemovedListener {
        void onAuthorityRemoved(EndPoint endPoint);
    }

    /* access modifiers changed from: package-private */
    public interface OnSyncRequestListener {
        void onSyncRequest(EndPoint endPoint, int i, Bundle bundle, int i2, int i3, int i4);
    }

    /* access modifiers changed from: package-private */
    public interface PeriodicSyncAddedListener {
        void onPeriodicSyncAdded(EndPoint endPoint, Bundle bundle, long j, long j2);
    }

    public static class SyncHistoryItem {
        int authorityId;
        long downstreamActivity;
        long elapsedTime;
        int event;
        long eventTime;
        Bundle extras;
        int historyId;
        boolean initialization;
        String mesg;
        int reason;
        int source;
        int syncExemptionFlag;
        long upstreamActivity;
    }

    static {
        sAuthorityRenames.put("contacts", "com.android.contacts");
        sAuthorityRenames.put("calendar", "com.android.calendar");
    }

    /* access modifiers changed from: package-private */
    public static class AccountInfo {
        final AccountAndUser accountAndUser;
        final HashMap<String, AuthorityInfo> authorities = new HashMap<>();

        AccountInfo(AccountAndUser accountAndUser2) {
            this.accountAndUser = accountAndUser2;
        }
    }

    public static class EndPoint {
        public static final EndPoint USER_ALL_PROVIDER_ALL_ACCOUNTS_ALL = new EndPoint(null, null, -1);
        final Account account;
        final String provider;
        final int userId;

        public EndPoint(Account account2, String provider2, int userId2) {
            this.account = account2;
            this.provider = provider2;
            this.userId = userId2;
        }

        public boolean matchesSpec(EndPoint spec) {
            boolean accountsMatch;
            boolean providersMatch;
            int i = this.userId;
            int i2 = spec.userId;
            if (i != i2 && i != -1 && i2 != -1) {
                return false;
            }
            Account account2 = spec.account;
            if (account2 == null) {
                accountsMatch = true;
            } else {
                accountsMatch = this.account.equals(account2);
            }
            String str = spec.provider;
            if (str == null) {
                providersMatch = true;
            } else {
                providersMatch = this.provider.equals(str);
            }
            if (!accountsMatch || !providersMatch) {
                return false;
            }
            return true;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(this.account == null ? "ALL ACCS" : "XXXXXXXXX");
            sb.append(SliceClientPermissions.SliceAuthority.DELIMITER);
            String str = this.provider;
            if (str == null) {
                str = "ALL PDRS";
            }
            sb.append(str);
            sb.append(":u" + this.userId);
            return sb.toString();
        }

        public String toSafeString() {
            StringBuilder sb = new StringBuilder();
            Account account2 = this.account;
            sb.append(account2 == null ? "ALL ACCS" : SyncLogger.logSafe(account2));
            sb.append(SliceClientPermissions.SliceAuthority.DELIMITER);
            String str = this.provider;
            if (str == null) {
                str = "ALL PDRS";
            }
            sb.append(str);
            sb.append(":u" + this.userId);
            return sb.toString();
        }
    }

    public static class AuthorityInfo {
        public static final int NOT_INITIALIZED = -1;
        public static final int NOT_SYNCABLE = 0;
        public static final int SYNCABLE = 1;
        public static final int SYNCABLE_NOT_INITIALIZED = 2;
        public static final int SYNCABLE_NO_ACCOUNT_ACCESS = 3;
        public static final int UNDEFINED = -2;
        long backoffDelay;
        long backoffTime;
        long delayUntil;
        boolean enabled;
        final int ident;
        final ArrayList<PeriodicSync> periodicSyncs;
        int syncable;
        final EndPoint target;

        AuthorityInfo(AuthorityInfo toCopy) {
            this.target = toCopy.target;
            this.ident = toCopy.ident;
            this.enabled = toCopy.enabled;
            this.syncable = toCopy.syncable;
            this.backoffTime = toCopy.backoffTime;
            this.backoffDelay = toCopy.backoffDelay;
            this.delayUntil = toCopy.delayUntil;
            this.periodicSyncs = new ArrayList<>();
            Iterator<PeriodicSync> it = toCopy.periodicSyncs.iterator();
            while (it.hasNext()) {
                this.periodicSyncs.add(new PeriodicSync(it.next()));
            }
        }

        AuthorityInfo(EndPoint info, int id) {
            this.target = info;
            this.ident = id;
            this.enabled = false;
            this.periodicSyncs = new ArrayList<>();
            defaultInitialisation();
        }

        private void defaultInitialisation() {
            this.syncable = -1;
            this.backoffTime = -1;
            this.backoffDelay = -1;
            if (SyncStorageEngine.mPeriodicSyncAddedListener != null) {
                SyncStorageEngine.mPeriodicSyncAddedListener.onPeriodicSyncAdded(this.target, new Bundle(), SyncStorageEngine.DEFAULT_POLL_FREQUENCY_SECONDS, SyncStorageEngine.calculateDefaultFlexTime(SyncStorageEngine.DEFAULT_POLL_FREQUENCY_SECONDS));
            }
        }

        public String toString() {
            return this.target + ", enabled=" + this.enabled + ", syncable=" + this.syncable + ", backoff=" + this.backoffTime + ", delay=" + this.delayUntil;
        }
    }

    public static class DayStats {
        public final int day;
        public int failureCount;
        public long failureTime;
        public int successCount;
        public long successTime;

        public DayStats(int day2) {
            this.day = day2;
        }
    }

    /* access modifiers changed from: private */
    public static class AccountAuthorityValidator {
        private final AccountManager mAccountManager;
        private final SparseArray<Account[]> mAccountsCache = new SparseArray<>();
        private final PackageManager mPackageManager;
        private final SparseArray<ArrayMap<String, Boolean>> mProvidersPerUserCache = new SparseArray<>();

        AccountAuthorityValidator(Context context) {
            this.mAccountManager = (AccountManager) context.getSystemService(AccountManager.class);
            this.mPackageManager = context.getPackageManager();
        }

        /* access modifiers changed from: package-private */
        public boolean isAccountValid(Account account, int userId) {
            Account[] accountsForUser = this.mAccountsCache.get(userId);
            if (accountsForUser == null) {
                accountsForUser = this.mAccountManager.getAccountsAsUser(userId);
                this.mAccountsCache.put(userId, accountsForUser);
            }
            return ArrayUtils.contains(accountsForUser, account);
        }

        /* access modifiers changed from: package-private */
        public boolean isAuthorityValid(String authority, int userId) {
            ArrayMap<String, Boolean> authorityMap = this.mProvidersPerUserCache.get(userId);
            if (authorityMap == null) {
                authorityMap = new ArrayMap<>();
                this.mProvidersPerUserCache.put(userId, authorityMap);
            }
            if (!authorityMap.containsKey(authority)) {
                authorityMap.put(authority, Boolean.valueOf(this.mPackageManager.resolveContentProviderAsUser(authority, 786432, userId) != null));
            }
            return authorityMap.get(authority).booleanValue();
        }
    }

    private SyncStorageEngine(Context context, File dataDir, Looper looper) {
        this.mHandler = new MyHandler(looper);
        this.mContext = context;
        sSyncStorageEngine = this;
        this.mLogger = SyncLogger.getInstance();
        this.mCal = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"));
        this.mDefaultMasterSyncAutomatically = this.mContext.getResources().getBoolean(17891551);
        File syncDir = new File(new File(dataDir, "system"), "sync");
        syncDir.mkdirs();
        maybeDeleteLegacyPendingInfoLocked(syncDir);
        this.mAccountInfoFile = new AtomicFile(new File(syncDir, "accounts.xml"), "sync-accounts");
        this.mStatusFile = new AtomicFile(new File(syncDir, "status.bin"), "sync-status");
        this.mStatisticsFile = new AtomicFile(new File(syncDir, "stats.bin"), "sync-stats");
        readAccountInfoLocked();
        readStatusLocked();
        readStatisticsLocked();
        if (this.mLogger.enabled()) {
            int size = this.mAuthorities.size();
            this.mLogger.log("Loaded ", Integer.valueOf(size), " items");
            for (int i = 0; i < size; i++) {
                this.mLogger.log(this.mAuthorities.valueAt(i));
            }
        }
    }

    public static SyncStorageEngine newTestInstance(Context context) {
        return new SyncStorageEngine(context, context.getFilesDir(), Looper.getMainLooper());
    }

    public static void init(Context context, Looper looper) {
        if (sSyncStorageEngine == null) {
            sSyncStorageEngine = new SyncStorageEngine(context, Environment.getDataDirectory(), looper);
        }
    }

    public static SyncStorageEngine getSingleton() {
        if (sSyncStorageEngine != null) {
            return sSyncStorageEngine;
        }
        throw new IllegalStateException("not initialized");
    }

    /* access modifiers changed from: protected */
    public void setOnSyncRequestListener(OnSyncRequestListener listener) {
        if (this.mSyncRequestListener == null) {
            this.mSyncRequestListener = listener;
        }
    }

    /* access modifiers changed from: protected */
    public void setOnAuthorityRemovedListener(OnAuthorityRemovedListener listener) {
        if (this.mAuthorityRemovedListener == null) {
            this.mAuthorityRemovedListener = listener;
        }
    }

    /* access modifiers changed from: protected */
    public void setPeriodicSyncAddedListener(PeriodicSyncAddedListener listener) {
        if (mPeriodicSyncAddedListener == null) {
            mPeriodicSyncAddedListener = listener;
        }
    }

    /* access modifiers changed from: private */
    public class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                synchronized (SyncStorageEngine.this.mAuthorities) {
                    SyncStorageEngine.this.writeStatusLocked();
                }
            } else if (msg.what == 2) {
                synchronized (SyncStorageEngine.this.mAuthorities) {
                    SyncStorageEngine.this.writeStatisticsLocked();
                }
            }
        }
    }

    public int getSyncRandomOffset() {
        return this.mSyncRandomOffset;
    }

    public void addStatusChangeListener(int mask, int userId, ISyncStatusObserver callback) {
        synchronized (this.mAuthorities) {
            this.mChangeListeners.register(callback, Long.valueOf(IntPair.of(userId, mask)));
        }
    }

    public void removeStatusChangeListener(ISyncStatusObserver callback) {
        synchronized (this.mAuthorities) {
            this.mChangeListeners.unregister(callback);
        }
    }

    public static long calculateDefaultFlexTime(long syncTimeSeconds) {
        if (syncTimeSeconds < DEFAULT_MIN_FLEX_ALLOWED_SECS) {
            return 0;
        }
        if (syncTimeSeconds < DEFAULT_POLL_FREQUENCY_SECONDS) {
            return (long) (((double) syncTimeSeconds) * DEFAULT_FLEX_PERCENT_SYNC);
        }
        return 3456;
    }

    /* access modifiers changed from: package-private */
    public void reportChange(int which, int callingUserId) {
        ArrayList<ISyncStatusObserver> reports = null;
        synchronized (this.mAuthorities) {
            int i = this.mChangeListeners.beginBroadcast();
            while (i > 0) {
                i--;
                long cookie = ((Long) this.mChangeListeners.getBroadcastCookie(i)).longValue();
                int userId = IntPair.first(cookie);
                if ((which & IntPair.second(cookie)) != 0) {
                    if (callingUserId == userId) {
                        if (reports == null) {
                            reports = new ArrayList<>(i);
                        }
                        reports.add(this.mChangeListeners.getBroadcastItem(i));
                    }
                }
            }
            this.mChangeListeners.finishBroadcast();
        }
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "reportChange " + which + " to: " + reports);
        }
        if (reports != null) {
            int i2 = reports.size();
            while (i2 > 0) {
                i2--;
                try {
                    reports.get(i2).onStatusChanged(which);
                } catch (RemoteException e) {
                }
            }
        }
    }

    public boolean getSyncAutomatically(Account account, int userId, String providerName) {
        synchronized (this.mAuthorities) {
            boolean z = true;
            if (account != null) {
                AuthorityInfo authority = getAuthorityLocked(new EndPoint(account, providerName, userId), "getSyncAutomatically");
                if (authority == null || !authority.enabled) {
                    z = false;
                }
                return z;
            }
            int i = this.mAuthorities.size();
            while (i > 0) {
                i--;
                AuthorityInfo authorityInfo = this.mAuthorities.valueAt(i);
                if (authorityInfo.target.matchesSpec(new EndPoint(account, providerName, userId)) && authorityInfo.enabled) {
                    return true;
                }
            }
            return false;
        }
    }

    public void setSyncAutomatically(Account account, int userId, String providerName, boolean sync, int syncExemptionFlag, int callingUid, int callingPid) {
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.d("SyncManager", "setSyncAutomatically:  provider " + providerName + ", user " + userId + " -> " + sync);
        }
        this.mLogger.log("Set sync auto account=", account, " user=", Integer.valueOf(userId), " authority=", providerName, " value=", Boolean.toString(sync), " cuid=", Integer.valueOf(callingUid), " cpid=", Integer.valueOf(callingPid));
        synchronized (this.mAuthorities) {
            AuthorityInfo authority = getOrCreateAuthorityLocked(new EndPoint(account, providerName, userId), -1, false);
            if (authority.enabled == sync) {
                if (Log.isLoggable("SyncManager", 2)) {
                    Slog.d("SyncManager", "setSyncAutomatically: already set to " + sync + ", doing nothing");
                }
                return;
            } else if (!sync || account == null || !HwDeviceManager.disallowOp(25, account.type) || !HwDeviceManager.disallowOp(24, providerName)) {
                if (sync && authority.syncable == 2) {
                    authority.syncable = -1;
                }
                authority.enabled = sync;
                writeAccountInfoLocked();
            } else {
                Slog.i("SyncManager", "setSyncAutomatically() is not allowed for google account by MDM!");
                UiThread.getHandler().post(new Runnable() {
                    /* class com.android.server.content.SyncStorageEngine.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        Toast toast = Toast.makeText(SyncStorageEngine.this.mContext, SyncStorageEngine.this.mContext.getResources().getString(33685904), 1);
                        toast.getWindowParams().type = 2006;
                        toast.show();
                    }
                });
                return;
            }
        }
        if (sync) {
            requestSync(account, userId, -6, providerName, new Bundle(), syncExemptionFlag, callingUid, callingPid);
        }
        reportChange(1, userId);
        queueBackup();
    }

    public int getIsSyncable(Account account, int userId, String providerName) {
        synchronized (this.mAuthorities) {
            if (account != null) {
                AuthorityInfo authority = getAuthorityLocked(new EndPoint(account, providerName, userId), "get authority syncable");
                if (authority == null) {
                    return -1;
                }
                return authority.syncable;
            }
            int i = this.mAuthorities.size();
            while (i > 0) {
                i--;
                AuthorityInfo authorityInfo = this.mAuthorities.valueAt(i);
                if (authorityInfo.target != null && authorityInfo.target.provider.equals(providerName)) {
                    return authorityInfo.syncable;
                }
            }
            return -1;
        }
    }

    public void setIsSyncable(Account account, int userId, String providerName, int syncable, int callingUid, int callingPid) {
        setSyncableStateForEndPoint(new EndPoint(account, providerName, userId), syncable, callingUid, callingPid);
    }

    private void setSyncableStateForEndPoint(EndPoint target, int syncable, int callingUid, int callingPid) {
        Throwable th;
        AuthorityInfo aInfo;
        int syncable2;
        this.mLogger.log("Set syncable ", target, " value=", Integer.toString(syncable), " cuid=", Integer.valueOf(callingUid), " cpid=", Integer.valueOf(callingPid));
        synchronized (this.mAuthorities) {
            try {
                aInfo = getOrCreateAuthorityLocked(target, -1, false);
                if (syncable < -1) {
                    syncable2 = -1;
                } else {
                    syncable2 = syncable;
                }
                try {
                    if (Log.isLoggable("SyncManager", 2)) {
                        Slog.d("SyncManager", "setIsSyncable: " + aInfo.toString() + " -> " + syncable2);
                    }
                    if (aInfo.syncable == syncable2) {
                        if (Log.isLoggable("SyncManager", 2)) {
                            Slog.d("SyncManager", "setIsSyncable: already set to " + syncable2 + ", doing nothing");
                        }
                        return;
                    }
                    aInfo.syncable = syncable2;
                    writeAccountInfoLocked();
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (syncable2 == 1) {
            requestSync(aInfo, -5, new Bundle(), 0, callingUid, callingPid);
        }
        reportChange(1, target.userId);
    }

    public Pair<Long, Long> getBackoff(EndPoint info) {
        synchronized (this.mAuthorities) {
            AuthorityInfo authority = getAuthorityLocked(info, "getBackoff");
            if (authority == null) {
                return null;
            }
            return Pair.create(Long.valueOf(authority.backoffTime), Long.valueOf(authority.backoffDelay));
        }
    }

    public void setBackoff(EndPoint info, long nextSyncTime, long nextDelay) {
        boolean changed;
        int i;
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "setBackoff: " + info + " -> nextSyncTime " + nextSyncTime + ", nextDelay " + nextDelay);
        }
        synchronized (this.mAuthorities) {
            if (info.account != null) {
                if (info.provider != null) {
                    AuthorityInfo authorityInfo = getOrCreateAuthorityLocked(info, -1, true);
                    if (authorityInfo.backoffTime == nextSyncTime && authorityInfo.backoffDelay == nextDelay) {
                        changed = false;
                        i = 1;
                    } else {
                        authorityInfo.backoffTime = nextSyncTime;
                        authorityInfo.backoffDelay = nextDelay;
                        changed = true;
                        i = 1;
                    }
                }
            }
            i = 1;
            changed = setBackoffLocked(info.account, info.userId, info.provider, nextSyncTime, nextDelay);
        }
        if (changed) {
            reportChange(i, info.userId);
        }
    }

    private boolean setBackoffLocked(Account account, int userId, String providerName, long nextSyncTime, long nextDelay) {
        boolean changed = false;
        for (AccountInfo accountInfo : this.mAccounts.values()) {
            if (account == null || account.equals(accountInfo.accountAndUser.account) || userId == accountInfo.accountAndUser.userId) {
                for (AuthorityInfo authorityInfo : accountInfo.authorities.values()) {
                    if ((providerName == null || providerName.equals(authorityInfo.target.provider)) && !(authorityInfo.backoffTime == nextSyncTime && authorityInfo.backoffDelay == nextDelay)) {
                        authorityInfo.backoffTime = nextSyncTime;
                        authorityInfo.backoffDelay = nextDelay;
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    public void clearAllBackoffsLocked() {
        ArraySet<Integer> changedUserIds = new ArraySet<>();
        synchronized (this.mAuthorities) {
            for (AccountInfo accountInfo : this.mAccounts.values()) {
                for (AuthorityInfo authorityInfo : accountInfo.authorities.values()) {
                    if (authorityInfo.backoffTime != -1 || authorityInfo.backoffDelay != -1) {
                        if (Log.isLoggable("SyncManager", 2)) {
                            Slog.v("SyncManager", "clearAllBackoffsLocked: authority:" + authorityInfo.target + " account:" + accountInfo.accountAndUser.account.name + " user:" + accountInfo.accountAndUser.userId + " backoffTime was: " + authorityInfo.backoffTime + " backoffDelay was: " + authorityInfo.backoffDelay);
                        }
                        authorityInfo.backoffTime = -1;
                        authorityInfo.backoffDelay = -1;
                        changedUserIds.add(Integer.valueOf(accountInfo.accountAndUser.userId));
                    }
                }
            }
        }
        for (int i = changedUserIds.size() - 1; i > 0; i--) {
            reportChange(1, changedUserIds.valueAt(i).intValue());
        }
    }

    public long getDelayUntilTime(EndPoint info) {
        synchronized (this.mAuthorities) {
            AuthorityInfo authority = getAuthorityLocked(info, "getDelayUntil");
            if (authority == null) {
                return 0;
            }
            return authority.delayUntil;
        }
    }

    public void setDelayUntilTime(EndPoint info, long delayUntil) {
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "setDelayUntil: " + info + " -> delayUntil " + delayUntil);
        }
        synchronized (this.mAuthorities) {
            AuthorityInfo authority = getOrCreateAuthorityLocked(info, -1, true);
            if (authority.delayUntil != delayUntil) {
                authority.delayUntil = delayUntil;
                reportChange(1, info.userId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean restoreAllPeriodicSyncs() {
        if (mPeriodicSyncAddedListener == null) {
            return false;
        }
        synchronized (this.mAuthorities) {
            for (int i = 0; i < this.mAuthorities.size(); i++) {
                AuthorityInfo authority = this.mAuthorities.valueAt(i);
                Iterator<PeriodicSync> it = authority.periodicSyncs.iterator();
                while (it.hasNext()) {
                    PeriodicSync periodicSync = it.next();
                    mPeriodicSyncAddedListener.onPeriodicSyncAdded(authority.target, periodicSync.extras, periodicSync.period, periodicSync.flexTime);
                }
                authority.periodicSyncs.clear();
            }
            writeAccountInfoLocked();
        }
        return true;
    }

    public void setMasterSyncAutomatically(boolean flag, int userId, int syncExemptionFlag, int callingUid, int callingPid) {
        this.mLogger.log("Set master enabled=", Boolean.valueOf(flag), " user=", Integer.valueOf(userId), " cuid=", Integer.valueOf(callingUid), " cpid=", Integer.valueOf(callingPid));
        synchronized (this.mAuthorities) {
            Boolean auto = this.mMasterSyncAutomatically.get(userId);
            if (auto == null || !auto.equals(Boolean.valueOf(flag))) {
                this.mMasterSyncAutomatically.put(userId, Boolean.valueOf(flag));
                writeAccountInfoLocked();
            } else {
                return;
            }
        }
        if (flag) {
            requestSync(null, userId, -7, null, new Bundle(), syncExemptionFlag, callingUid, callingPid);
        }
        reportChange(1, userId);
        this.mContext.sendBroadcast(ContentResolver.ACTION_SYNC_CONN_STATUS_CHANGED);
        queueBackup();
    }

    public boolean getMasterSyncAutomatically(int userId) {
        boolean booleanValue;
        synchronized (this.mAuthorities) {
            Boolean auto = this.mMasterSyncAutomatically.get(userId);
            booleanValue = auto == null ? this.mDefaultMasterSyncAutomatically : auto.booleanValue();
        }
        return booleanValue;
    }

    public int getAuthorityCount() {
        int size;
        synchronized (this.mAuthorities) {
            size = this.mAuthorities.size();
        }
        return size;
    }

    public AuthorityInfo getAuthority(int authorityId) {
        AuthorityInfo authorityInfo;
        synchronized (this.mAuthorities) {
            authorityInfo = this.mAuthorities.get(authorityId);
        }
        return authorityInfo;
    }

    public boolean isSyncActive(EndPoint info) {
        synchronized (this.mAuthorities) {
            for (SyncInfo syncInfo : getCurrentSyncs(info.userId)) {
                AuthorityInfo ainfo = getAuthority(syncInfo.authorityId);
                if (ainfo != null && ainfo.target.matchesSpec(info)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void markPending(EndPoint info, boolean pendingValue) {
        synchronized (this.mAuthorities) {
            AuthorityInfo authority = getOrCreateAuthorityLocked(info, -1, true);
            if (authority != null) {
                getOrCreateSyncStatusLocked(authority.ident).pending = pendingValue;
                reportChange(2, info.userId);
            }
        }
    }

    public void removeStaleAccounts(Account[] currentAccounts, int userId) {
        synchronized (this.mAuthorities) {
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "Updating for new accounts...");
            }
            SparseArray<AuthorityInfo> removing = new SparseArray<>();
            Iterator<AccountInfo> accIt = this.mAccounts.values().iterator();
            while (accIt.hasNext()) {
                AccountInfo acc = accIt.next();
                if (acc.accountAndUser.userId == userId) {
                    if (currentAccounts == null || !ArrayUtils.contains(currentAccounts, acc.accountAndUser.account)) {
                        if (Log.isLoggable("SyncManager", 2)) {
                            Slog.v("SyncManager", "Account removed: " + acc.accountAndUser);
                        }
                        for (AuthorityInfo auth : acc.authorities.values()) {
                            removing.put(auth.ident, auth);
                        }
                        accIt.remove();
                    }
                }
            }
            int i = removing.size();
            if (i > 0) {
                while (i > 0) {
                    i--;
                    int ident = removing.keyAt(i);
                    AuthorityInfo auth2 = removing.valueAt(i);
                    if (this.mAuthorityRemovedListener != null) {
                        this.mAuthorityRemovedListener.onAuthorityRemoved(auth2.target);
                    }
                    this.mAuthorities.remove(ident);
                    int j = this.mSyncStatus.size();
                    while (j > 0) {
                        j--;
                        if (this.mSyncStatus.keyAt(j) == ident) {
                            this.mSyncStatus.remove(this.mSyncStatus.keyAt(j));
                        }
                    }
                    int j2 = this.mSyncHistory.size();
                    while (j2 > 0) {
                        j2--;
                        if (this.mSyncHistory.get(j2).authorityId == ident) {
                            this.mSyncHistory.remove(j2);
                        }
                    }
                }
                writeAccountInfoLocked();
                writeStatusLocked();
                writeStatisticsLocked();
            }
        }
    }

    public SyncInfo addActiveSync(SyncManager.ActiveSyncContext activeSyncContext) {
        SyncInfo syncInfo;
        synchronized (this.mAuthorities) {
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "setActiveSync: account= auth=" + activeSyncContext.mSyncOperation.target + " src=" + activeSyncContext.mSyncOperation.syncSource + " extras=" + activeSyncContext.mSyncOperation.extras);
            }
            AuthorityInfo authorityInfo = getOrCreateAuthorityLocked(activeSyncContext.mSyncOperation.target, -1, true);
            syncInfo = new SyncInfo(authorityInfo.ident, authorityInfo.target.account, authorityInfo.target.provider, activeSyncContext.mStartTime);
            getCurrentSyncs(authorityInfo.target.userId).add(syncInfo);
        }
        reportActiveChange(activeSyncContext.mSyncOperation.target.userId);
        return syncInfo;
    }

    public void removeActiveSync(SyncInfo syncInfo, int userId) {
        synchronized (this.mAuthorities) {
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "removeActiveSync: account=" + syncInfo.account + " user=" + userId + " auth=" + syncInfo.authority);
            }
            getCurrentSyncs(userId).remove(syncInfo);
        }
        reportActiveChange(userId);
    }

    public void reportActiveChange(int userId) {
        reportChange(4, userId);
    }

    public long insertStartSyncEvent(SyncOperation op, long now) {
        synchronized (this.mAuthorities) {
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "insertStartSyncEvent: " + op);
            }
            AuthorityInfo authority = getAuthorityLocked(op.target, "insertStartSyncEvent");
            if (authority == null) {
                return -1;
            }
            SyncHistoryItem item = new SyncHistoryItem();
            item.initialization = op.isInitialization();
            item.authorityId = authority.ident;
            int i = this.mNextHistoryId;
            this.mNextHistoryId = i + 1;
            item.historyId = i;
            if (this.mNextHistoryId < 0) {
                this.mNextHistoryId = 0;
            }
            item.eventTime = now;
            item.source = op.syncSource;
            item.reason = op.reason;
            item.extras = op.extras;
            item.event = 0;
            item.syncExemptionFlag = op.syncExemptionFlag;
            this.mSyncHistory.add(0, item);
            while (this.mSyncHistory.size() > 100) {
                this.mSyncHistory.remove(this.mSyncHistory.size() - 1);
            }
            long id = (long) item.historyId;
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "returning historyId " + id);
            }
            reportChange(8, op.target.userId);
            return id;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x0178 A[Catch:{ all -> 0x029d }] */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x019a A[Catch:{ all -> 0x029d }] */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0228 A[Catch:{ all -> 0x029d }] */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x025a A[Catch:{ all -> 0x029d }] */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x025e A[Catch:{ all -> 0x029d }] */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x0278 A[Catch:{ all -> 0x029d }] */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x027c A[Catch:{ all -> 0x029d }] */
    public void stopSyncEvent(long historyId, long elapsedTime, String resultMessage, long downstreamActivity, long upstreamActivity, int userId) {
        Throwable th;
        int i;
        boolean writeStatisticsNow;
        boolean writeStatusNow;
        boolean writeStatisticsNow2;
        synchronized (this.mAuthorities) {
            try {
                if (Log.isLoggable("SyncManager", 2)) {
                    Slog.v("SyncManager", "stopSyncEvent: historyId=" + historyId);
                }
                SyncHistoryItem item = null;
                int i2 = this.mSyncHistory.size();
                while (true) {
                    if (i2 <= 0) {
                        break;
                    }
                    i2--;
                    item = this.mSyncHistory.get(i2);
                    if (((long) item.historyId) == historyId) {
                        break;
                    }
                    item = null;
                }
                if (item == null) {
                    Slog.w("SyncManager", "stopSyncEvent: no history for id " + historyId);
                    return;
                }
                item.elapsedTime = elapsedTime;
                item.event = 1;
                item.mesg = resultMessage;
                item.downstreamActivity = downstreamActivity;
                try {
                    item.upstreamActivity = upstreamActivity;
                    SyncStatusInfo status = getOrCreateSyncStatusLocked(item.authorityId);
                    status.maybeResetTodayStats(isClockValid(), false);
                    status.totalStats.numSyncs++;
                    status.todayStats.numSyncs++;
                    status.totalStats.totalElapsedTime += elapsedTime;
                    status.todayStats.totalElapsedTime += elapsedTime;
                    int i3 = item.source;
                    if (i3 == 0) {
                        status.totalStats.numSourceOther++;
                        status.todayStats.numSourceOther++;
                    } else if (i3 == 1) {
                        status.totalStats.numSourceLocal++;
                        status.todayStats.numSourceLocal++;
                    } else if (i3 == 2) {
                        status.totalStats.numSourcePoll++;
                        status.todayStats.numSourcePoll++;
                    } else if (i3 == 3) {
                        status.totalStats.numSourceUser++;
                        status.todayStats.numSourceUser++;
                    } else if (i3 == 4) {
                        status.totalStats.numSourcePeriodic++;
                        status.todayStats.numSourcePeriodic++;
                    } else if (i3 == 5) {
                        status.totalStats.numSourceFeed++;
                        status.todayStats.numSourceFeed++;
                    }
                    int day = getCurrentDayLocked();
                    if (this.mDayStats[0] == null) {
                        this.mDayStats[0] = new DayStats(day);
                        writeStatisticsNow2 = false;
                        i = 0;
                    } else if (day != this.mDayStats[0].day) {
                        System.arraycopy(this.mDayStats, 0, this.mDayStats, 1, this.mDayStats.length - 1);
                        this.mDayStats[0] = new DayStats(day);
                        writeStatisticsNow = true;
                        i = 0;
                        DayStats ds = this.mDayStats[i];
                        long lastSyncTime = item.eventTime + elapsedTime;
                        if (!MESG_SUCCESS.equals(resultMessage)) {
                            writeStatusNow = false;
                            if (status.lastSuccessTime == 0 || status.lastFailureTime != 0) {
                                writeStatusNow = true;
                            }
                            status.setLastSuccess(item.source, lastSyncTime);
                            ds.successCount++;
                            ds.successTime += elapsedTime;
                        } else {
                            writeStatusNow = false;
                            if (!MESG_CANCELED.equals(resultMessage)) {
                                if (status.lastFailureTime == 0) {
                                    writeStatusNow = true;
                                }
                                status.totalStats.numFailures++;
                                status.todayStats.numFailures++;
                                status.setLastFailure(item.source, lastSyncTime, resultMessage);
                                ds.failureCount++;
                                ds.failureTime += elapsedTime;
                            } else {
                                status.totalStats.numCancels++;
                                status.todayStats.numCancels++;
                                writeStatusNow = true;
                            }
                        }
                        StringBuilder event = new StringBuilder();
                        event.append("" + resultMessage + " Source=" + SOURCES[item.source] + " Elapsed=");
                        SyncManager.formatDurationHMS(event, elapsedTime);
                        event.append(" Reason=");
                        event.append(SyncOperation.reasonToString(null, item.reason));
                        if (item.syncExemptionFlag != 0) {
                            event.append(" Exemption=");
                            int i4 = item.syncExemptionFlag;
                            if (i4 == 1) {
                                event.append("fg");
                            } else if (i4 != 2) {
                                event.append(item.syncExemptionFlag);
                            } else {
                                event.append("top");
                            }
                        }
                        event.append(" Extras=");
                        SyncOperation.extrasToStringBuilder(item.extras, event);
                        status.addEvent(event.toString());
                        if (!writeStatusNow) {
                            writeStatusLocked();
                        } else if (!this.mHandler.hasMessages(1)) {
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), 600000);
                        }
                        if (!writeStatisticsNow) {
                            writeStatisticsLocked();
                        } else if (!this.mHandler.hasMessages(2)) {
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), 1800000);
                        }
                        reportChange(8, userId);
                    } else {
                        writeStatisticsNow2 = false;
                        i = 0;
                        DayStats dayStats = this.mDayStats[0];
                    }
                    writeStatisticsNow = writeStatisticsNow2;
                    DayStats ds2 = this.mDayStats[i];
                    long lastSyncTime2 = item.eventTime + elapsedTime;
                    if (!MESG_SUCCESS.equals(resultMessage)) {
                    }
                    StringBuilder event2 = new StringBuilder();
                    event2.append("" + resultMessage + " Source=" + SOURCES[item.source] + " Elapsed=");
                    SyncManager.formatDurationHMS(event2, elapsedTime);
                    event2.append(" Reason=");
                    event2.append(SyncOperation.reasonToString(null, item.reason));
                    if (item.syncExemptionFlag != 0) {
                    }
                    event2.append(" Extras=");
                    SyncOperation.extrasToStringBuilder(item.extras, event2);
                    status.addEvent(event2.toString());
                    if (!writeStatusNow) {
                    }
                    if (!writeStatisticsNow) {
                    }
                    reportChange(8, userId);
                } catch (Throwable th2) {
                    th = th2;
                    while (true) {
                        try {
                            break;
                        } catch (Throwable th3) {
                            th = th3;
                        }
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    private List<SyncInfo> getCurrentSyncs(int userId) {
        List<SyncInfo> currentSyncsLocked;
        synchronized (this.mAuthorities) {
            currentSyncsLocked = getCurrentSyncsLocked(userId);
        }
        return currentSyncsLocked;
    }

    public List<SyncInfo> getCurrentSyncsCopy(int userId, boolean canAccessAccounts) {
        List<SyncInfo> syncsCopy;
        SyncInfo copy;
        synchronized (this.mAuthorities) {
            List<SyncInfo> syncs = getCurrentSyncsLocked(userId);
            syncsCopy = new ArrayList<>();
            for (SyncInfo sync : syncs) {
                if (!canAccessAccounts) {
                    copy = SyncInfo.createAccountRedacted(sync.authorityId, sync.authority, sync.startTime);
                } else {
                    copy = new SyncInfo(sync);
                }
                syncsCopy.add(copy);
            }
        }
        return syncsCopy;
    }

    private List<SyncInfo> getCurrentSyncsLocked(int userId) {
        ArrayList<SyncInfo> syncs = this.mCurrentSyncs.get(userId);
        if (syncs != null) {
            return syncs;
        }
        ArrayList<SyncInfo> syncs2 = new ArrayList<>();
        this.mCurrentSyncs.put(userId, syncs2);
        return syncs2;
    }

    public Pair<AuthorityInfo, SyncStatusInfo> getCopyOfAuthorityWithSyncStatus(EndPoint info) {
        Pair<AuthorityInfo, SyncStatusInfo> createCopyPairOfAuthorityWithSyncStatusLocked;
        synchronized (this.mAuthorities) {
            createCopyPairOfAuthorityWithSyncStatusLocked = createCopyPairOfAuthorityWithSyncStatusLocked(getOrCreateAuthorityLocked(info, -1, true));
        }
        return createCopyPairOfAuthorityWithSyncStatusLocked;
    }

    public SyncStatusInfo getStatusByAuthority(EndPoint info) {
        if (info.account == null || info.provider == null) {
            return null;
        }
        synchronized (this.mAuthorities) {
            int N = this.mSyncStatus.size();
            for (int i = 0; i < N; i++) {
                SyncStatusInfo cur = this.mSyncStatus.valueAt(i);
                AuthorityInfo ainfo = this.mAuthorities.get(cur.authorityId);
                if (ainfo != null && ainfo.target.matchesSpec(info)) {
                    return cur;
                }
            }
            return null;
        }
    }

    public boolean isSyncPending(EndPoint info) {
        synchronized (this.mAuthorities) {
            int N = this.mSyncStatus.size();
            for (int i = 0; i < N; i++) {
                SyncStatusInfo cur = this.mSyncStatus.valueAt(i);
                AuthorityInfo ainfo = this.mAuthorities.get(cur.authorityId);
                if (ainfo != null) {
                    if (ainfo.target.matchesSpec(info)) {
                        if (cur.pending) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    public ArrayList<SyncHistoryItem> getSyncHistory() {
        ArrayList<SyncHistoryItem> items;
        synchronized (this.mAuthorities) {
            int N = this.mSyncHistory.size();
            items = new ArrayList<>(N);
            for (int i = 0; i < N; i++) {
                items.add(this.mSyncHistory.get(i));
            }
        }
        return items;
    }

    public DayStats[] getDayStatistics() {
        DayStats[] ds;
        synchronized (this.mAuthorities) {
            ds = new DayStats[this.mDayStats.length];
            System.arraycopy(this.mDayStats, 0, ds, 0, ds.length);
        }
        return ds;
    }

    private Pair<AuthorityInfo, SyncStatusInfo> createCopyPairOfAuthorityWithSyncStatusLocked(AuthorityInfo authorityInfo) {
        return Pair.create(new AuthorityInfo(authorityInfo), new SyncStatusInfo(getOrCreateSyncStatusLocked(authorityInfo.ident)));
    }

    private int getCurrentDayLocked() {
        this.mCal.setTimeInMillis(System.currentTimeMillis());
        int dayOfYear = this.mCal.get(6);
        if (this.mYear != this.mCal.get(1)) {
            this.mYear = this.mCal.get(1);
            this.mCal.clear();
            this.mCal.set(1, this.mYear);
            this.mYearInDays = (int) (this.mCal.getTimeInMillis() / 86400000);
        }
        return this.mYearInDays + dayOfYear;
    }

    private AuthorityInfo getAuthorityLocked(EndPoint info, String tag) {
        AccountAndUser au = new AccountAndUser(info.account, info.userId);
        AccountInfo accountInfo = this.mAccounts.get(au);
        if (accountInfo == null) {
            if (tag != null && Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", tag + ": unknown account " + au);
            }
            return null;
        }
        AuthorityInfo authority = accountInfo.authorities.get(info.provider);
        if (authority != null) {
            return authority;
        }
        if (tag != null && Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", tag + ": unknown provider " + info.provider);
        }
        return null;
    }

    private AuthorityInfo getOrCreateAuthorityLocked(EndPoint info, int ident, boolean doWrite) {
        AccountAndUser au = new AccountAndUser(info.account, info.userId);
        AccountInfo account = this.mAccounts.get(au);
        if (account == null) {
            account = new AccountInfo(au);
            this.mAccounts.put(au, account);
        }
        AuthorityInfo authority = account.authorities.get(info.provider);
        if (authority != null) {
            return authority;
        }
        AuthorityInfo authority2 = createAuthorityLocked(info, ident, doWrite);
        account.authorities.put(info.provider, authority2);
        return authority2;
    }

    private AuthorityInfo createAuthorityLocked(EndPoint info, int ident, boolean doWrite) {
        if (ident < 0) {
            ident = this.mNextAuthorityId;
            this.mNextAuthorityId++;
            doWrite = true;
        }
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "created a new AuthorityInfo for " + info);
        }
        AuthorityInfo authority = new AuthorityInfo(info, ident);
        this.mAuthorities.put(ident, authority);
        if (doWrite) {
            writeAccountInfoLocked();
        }
        return authority;
    }

    public void removeAuthority(EndPoint info) {
        synchronized (this.mAuthorities) {
            removeAuthorityLocked(info.account, info.userId, info.provider, true);
        }
    }

    private void removeAuthorityLocked(Account account, int userId, String authorityName, boolean doWrite) {
        AuthorityInfo authorityInfo;
        AccountInfo accountInfo = this.mAccounts.get(new AccountAndUser(account, userId));
        if (accountInfo != null && (authorityInfo = accountInfo.authorities.remove(authorityName)) != null) {
            OnAuthorityRemovedListener onAuthorityRemovedListener = this.mAuthorityRemovedListener;
            if (onAuthorityRemovedListener != null) {
                onAuthorityRemovedListener.onAuthorityRemoved(authorityInfo.target);
            }
            this.mAuthorities.remove(authorityInfo.ident);
            if (doWrite) {
                writeAccountInfoLocked();
            }
        }
    }

    private SyncStatusInfo getOrCreateSyncStatusLocked(int authorityId) {
        SyncStatusInfo status = this.mSyncStatus.get(authorityId);
        if (status != null) {
            return status;
        }
        SyncStatusInfo status2 = new SyncStatusInfo(authorityId);
        this.mSyncStatus.put(authorityId, status2);
        return status2;
    }

    public void writeAllState() {
        synchronized (this.mAuthorities) {
            writeStatusLocked();
            writeStatisticsLocked();
        }
    }

    public boolean shouldGrantSyncAdaptersAccountAccess() {
        return this.mGrantSyncAdaptersAccountAccess;
    }

    public void clearAndReadState() {
        synchronized (this.mAuthorities) {
            this.mAuthorities.clear();
            this.mAccounts.clear();
            this.mServices.clear();
            this.mSyncStatus.clear();
            this.mSyncHistory.clear();
            readAccountInfoLocked();
            readStatusLocked();
            readStatisticsLocked();
            writeAccountInfoLocked();
            writeStatusLocked();
            writeStatisticsLocked();
        }
    }

    private void readAccountInfoLocked() {
        int version;
        String listen;
        int i;
        int id;
        int version2;
        int highestAuthorityId = -1;
        FileInputStream fis = null;
        try {
            FileInputStream fis2 = this.mAccountInfoFile.openRead();
            if (Log.isLoggable(TAG_FILE, 2)) {
                Slog.v(TAG_FILE, "Reading " + this.mAccountInfoFile.getBaseFile());
            }
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fis2, StandardCharsets.UTF_8.name());
            int eventType = parser.getEventType();
            while (eventType != 2 && eventType != 1) {
                eventType = parser.next();
            }
            if (eventType == 1) {
                Slog.i("SyncManager", "No initial accounts");
                this.mNextAuthorityId = Math.max(-1 + 1, this.mNextAuthorityId);
                if (fis2 != null) {
                    try {
                        fis2.close();
                    } catch (IOException e) {
                    }
                }
            } else {
                if ("accounts".equals(parser.getName())) {
                    String listen2 = parser.getAttributeValue(null, XML_ATTR_LISTEN_FOR_TICKLES);
                    String versionString = parser.getAttributeValue(null, "version");
                    if (versionString == null) {
                        version2 = 0;
                    } else {
                        try {
                            version2 = Integer.parseInt(versionString);
                        } catch (NumberFormatException e2) {
                            version = 0;
                        }
                    }
                    version = version2;
                    if (version < 3) {
                        this.mGrantSyncAdaptersAccountAccess = true;
                    }
                    String nextIdString = parser.getAttributeValue(null, XML_ATTR_NEXT_AUTHORITY_ID);
                    if (nextIdString == null) {
                        id = 0;
                    } else {
                        try {
                            id = Integer.parseInt(nextIdString);
                        } catch (NumberFormatException e3) {
                        }
                    }
                    this.mNextAuthorityId = Math.max(this.mNextAuthorityId, id);
                    String offsetString = parser.getAttributeValue(null, XML_ATTR_SYNC_RANDOM_OFFSET);
                    if (offsetString == null) {
                        i = 0;
                    } else {
                        try {
                            i = Integer.parseInt(offsetString);
                        } catch (NumberFormatException e4) {
                            this.mSyncRandomOffset = 0;
                        }
                    }
                    this.mSyncRandomOffset = i;
                    if (this.mSyncRandomOffset == 0) {
                        this.mSyncRandomOffset = new Random(System.currentTimeMillis()).nextInt(86400);
                    }
                    this.mMasterSyncAutomatically.put(0, Boolean.valueOf(listen2 == null || Boolean.parseBoolean(listen2)));
                    int eventType2 = parser.next();
                    AuthorityInfo authority = null;
                    PeriodicSync periodicSync = null;
                    AccountAuthorityValidator validator = new AccountAuthorityValidator(this.mContext);
                    while (true) {
                        if (eventType2 == 2) {
                            String tagName = parser.getName();
                            listen = listen2;
                            if (parser.getDepth() == 2) {
                                if ("authority".equals(tagName)) {
                                    authority = parseAuthority(parser, version, validator);
                                    periodicSync = null;
                                    if (authority == null) {
                                        EventLog.writeEvent(1397638484, "26513719", -1, "Malformed authority");
                                    } else if (authority.ident > highestAuthorityId) {
                                        highestAuthorityId = authority.ident;
                                    }
                                } else if (XML_TAG_LISTEN_FOR_TICKLES.equals(tagName)) {
                                    parseListenForTickles(parser);
                                }
                            } else if (parser.getDepth() == 3) {
                                if ("periodicSync".equals(tagName) && authority != null) {
                                    periodicSync = parsePeriodicSync(parser, authority);
                                }
                            } else if (parser.getDepth() == 4 && periodicSync != null && "extra".equals(tagName)) {
                                parseExtra(parser, periodicSync.extras);
                            }
                        } else {
                            listen = listen2;
                        }
                        eventType2 = parser.next();
                        if (eventType2 == 1) {
                            break;
                        }
                        listen2 = listen;
                    }
                }
                this.mNextAuthorityId = Math.max(highestAuthorityId + 1, this.mNextAuthorityId);
                if (fis2 != null) {
                    try {
                        fis2.close();
                    } catch (IOException e5) {
                    }
                }
                maybeMigrateSettingsForRenamedAuthorities();
            }
        } catch (XmlPullParserException e6) {
            Slog.w("SyncManager", "Error reading accounts", e6);
            this.mNextAuthorityId = Math.max(-1 + 1, this.mNextAuthorityId);
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e7) {
                }
            }
        } catch (IOException e8) {
            if (0 == 0) {
                Slog.i("SyncManager", "No initial accounts");
            } else {
                Slog.w("SyncManager", "Error reading accounts", e8);
            }
            this.mNextAuthorityId = Math.max(-1 + 1, this.mNextAuthorityId);
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e9) {
                }
            }
        } catch (Throwable th) {
            this.mNextAuthorityId = Math.max(-1 + 1, this.mNextAuthorityId);
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e10) {
                }
            }
            throw th;
        }
    }

    private void maybeDeleteLegacyPendingInfoLocked(File syncDir) {
        File file = new File(syncDir, "pending.bin");
        if (file.exists()) {
            file.delete();
        }
    }

    private boolean maybeMigrateSettingsForRenamedAuthorities() {
        boolean writeNeeded = false;
        ArrayList<AuthorityInfo> authoritiesToRemove = new ArrayList<>();
        int N = this.mAuthorities.size();
        for (int i = 0; i < N; i++) {
            AuthorityInfo authority = this.mAuthorities.valueAt(i);
            String newAuthorityName = sAuthorityRenames.get(authority.target.provider);
            if (newAuthorityName != null) {
                authoritiesToRemove.add(authority);
                if (authority.enabled) {
                    EndPoint newInfo = new EndPoint(authority.target.account, newAuthorityName, authority.target.userId);
                    if (getAuthorityLocked(newInfo, "cleanup") == null) {
                        getOrCreateAuthorityLocked(newInfo, -1, false).enabled = true;
                        writeNeeded = true;
                    }
                }
            }
        }
        Iterator<AuthorityInfo> it = authoritiesToRemove.iterator();
        while (it.hasNext()) {
            AuthorityInfo authorityInfo = it.next();
            removeAuthorityLocked(authorityInfo.target.account, authorityInfo.target.userId, authorityInfo.target.provider, false);
            writeNeeded = true;
        }
        return writeNeeded;
    }

    private void parseListenForTickles(XmlPullParser parser) {
        int userId = 0;
        try {
            userId = Integer.parseInt(parser.getAttributeValue(null, XML_ATTR_USER));
        } catch (NumberFormatException e) {
            Slog.e("SyncManager", "error parsing the user for listen-for-tickles", e);
        } catch (NullPointerException e2) {
            Slog.e("SyncManager", "the user in listen-for-tickles is null", e2);
        }
        String enabled = parser.getAttributeValue(null, XML_ATTR_ENABLED);
        this.mMasterSyncAutomatically.put(userId, Boolean.valueOf(enabled == null || Boolean.parseBoolean(enabled)));
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x0171  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x01a8  */
    private AuthorityInfo parseAuthority(XmlPullParser parser, int version, AccountAuthorityValidator validator) {
        String syncable;
        String str;
        boolean z;
        boolean z2;
        AuthorityInfo authority;
        int i;
        AuthorityInfo authority2;
        int id = -1;
        try {
            id = Integer.parseInt(parser.getAttributeValue(null, "id"));
        } catch (NumberFormatException e) {
            Slog.e("SyncManager", "error parsing the id of the authority", e);
        } catch (NullPointerException e2) {
            Slog.e("SyncManager", "the id of the authority is null", e2);
        }
        if (id < 0) {
            return null;
        }
        String authorityName = parser.getAttributeValue(null, "authority");
        String enabled = parser.getAttributeValue(null, XML_ATTR_ENABLED);
        String syncable2 = parser.getAttributeValue(null, "syncable");
        String accountName = parser.getAttributeValue(null, "account");
        String accountType = parser.getAttributeValue(null, DatabaseHelper.SoundModelContract.KEY_TYPE);
        String user = parser.getAttributeValue(null, XML_ATTR_USER);
        String packageName = parser.getAttributeValue(null, "package");
        String className = parser.getAttributeValue(null, "class");
        int userId = user == null ? 0 : Integer.parseInt(user);
        if (accountType == null && packageName == null) {
            accountType = "com.google";
            syncable = String.valueOf(-1);
        } else {
            syncable = syncable2;
        }
        AuthorityInfo authority3 = this.mAuthorities.get(id);
        if (Log.isLoggable(TAG_FILE, 2)) {
            str = "SyncManager";
            Slog.v(TAG_FILE, "Adding authority: account=" + accountName + " accountType=" + accountType + " auth=" + authorityName + " package=" + packageName + " class=" + className + " user=" + userId + " enabled=" + enabled + " syncable=" + syncable);
        } else {
            str = "SyncManager";
        }
        if (authority3 == null) {
            if (Log.isLoggable(TAG_FILE, 2)) {
                Slog.v(TAG_FILE, "Creating authority entry");
            }
            if (accountName == null || authorityName == null) {
                z2 = false;
                z = true;
                authority2 = authority3;
            } else {
                EndPoint info = new EndPoint(new Account(accountName, accountType), authorityName, userId);
                if (validator.isAccountValid(info.account, userId)) {
                    if (validator.isAuthorityValid(authorityName, userId)) {
                        AuthorityInfo authority4 = getOrCreateAuthorityLocked(info, id, false);
                        if (version > 0) {
                            authority4.periodicSyncs.clear();
                        }
                        authority = authority4;
                        z2 = false;
                        z = true;
                        if (authority == null) {
                            authority.enabled = (enabled == null || Boolean.parseBoolean(enabled)) ? z : z2;
                            if (syncable == null) {
                                i = -1;
                            } else {
                                try {
                                    i = Integer.parseInt(syncable);
                                } catch (NumberFormatException e3) {
                                    if (UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN.equals(syncable)) {
                                        authority.syncable = -1;
                                        return authority;
                                    }
                                    boolean z3 = Boolean.parseBoolean(syncable) ? z : z2;
                                    int i2 = z3 ? 1 : 0;
                                    int i3 = z3 ? 1 : 0;
                                    int i4 = z3 ? 1 : 0;
                                    authority.syncable = i2;
                                    return authority;
                                }
                            }
                            authority.syncable = i;
                            return authority;
                        }
                        Slog.w(str, "Failure adding authority: auth=" + authorityName + " enabled=" + enabled + " syncable=" + syncable);
                        return authority;
                    }
                }
                z2 = false;
                z = true;
                authority2 = authority3;
                EventLog.writeEvent(1397638484, "35028827", -1, "account:" + info.account + " provider:" + authorityName + " user:" + userId);
            }
        } else {
            z2 = false;
            z = true;
            authority2 = authority3;
        }
        authority = authority2;
        if (authority == null) {
        }
    }

    private PeriodicSync parsePeriodicSync(XmlPullParser parser, AuthorityInfo authorityInfo) {
        long flextime;
        Bundle extras = new Bundle();
        String periodValue = parser.getAttributeValue(null, "period");
        String flexValue = parser.getAttributeValue(null, "flex");
        try {
            long period = Long.parseLong(periodValue);
            try {
                flextime = Long.parseLong(flexValue);
            } catch (NumberFormatException e) {
                long flextime2 = calculateDefaultFlexTime(period);
                Slog.e("SyncManager", "Error formatting value parsed for periodic sync flex: " + flexValue + ", using default: " + flextime2);
                flextime = flextime2;
            } catch (NullPointerException e2) {
                long flextime3 = calculateDefaultFlexTime(period);
                Slog.d("SyncManager", "No flex time specified for this sync, using a default. period: " + period + " flex: " + flextime3);
                flextime = flextime3;
            }
            PeriodicSync periodicSync = new PeriodicSync(authorityInfo.target.account, authorityInfo.target.provider, extras, period, flextime);
            authorityInfo.periodicSyncs.add(periodicSync);
            return periodicSync;
        } catch (NumberFormatException e3) {
            Slog.e("SyncManager", "error parsing the period of a periodic sync", e3);
            return null;
        } catch (NullPointerException e4) {
            Slog.e("SyncManager", "the period of a periodic sync is null", e4);
            return null;
        }
    }

    private void parseExtra(XmlPullParser parser, Bundle extras) {
        String name = parser.getAttributeValue(null, Settings.ATTR_NAME);
        String type = parser.getAttributeValue(null, DatabaseHelper.SoundModelContract.KEY_TYPE);
        String value1 = parser.getAttributeValue(null, "value1");
        String value2 = parser.getAttributeValue(null, "value2");
        try {
            if ("long".equals(type)) {
                extras.putLong(name, Long.parseLong(value1));
            } else if ("integer".equals(type)) {
                extras.putInt(name, Integer.parseInt(value1));
            } else if ("double".equals(type)) {
                extras.putDouble(name, Double.parseDouble(value1));
            } else if ("float".equals(type)) {
                extras.putFloat(name, Float.parseFloat(value1));
            } else if ("boolean".equals(type)) {
                extras.putBoolean(name, Boolean.parseBoolean(value1));
            } else if ("string".equals(type)) {
                extras.putString(name, value1);
            } else if ("account".equals(type)) {
                extras.putParcelable(name, new Account(value1, value2));
            }
        } catch (NumberFormatException e) {
            Slog.e("SyncManager", "error parsing bundle value", e);
        } catch (NullPointerException e2) {
            Slog.e("SyncManager", "error parsing bundle value", e2);
        }
    }

    private void writeAccountInfoLocked() {
        if (Log.isLoggable(TAG_FILE, 2)) {
            Slog.v(TAG_FILE, "Writing new " + this.mAccountInfoFile.getBaseFile());
        }
        FileOutputStream fos = null;
        try {
            fos = this.mAccountInfoFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, StandardCharsets.UTF_8.name());
            out.startDocument(null, true);
            out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            out.startTag(null, "accounts");
            out.attribute(null, "version", Integer.toString(3));
            out.attribute(null, XML_ATTR_NEXT_AUTHORITY_ID, Integer.toString(this.mNextAuthorityId));
            out.attribute(null, XML_ATTR_SYNC_RANDOM_OFFSET, Integer.toString(this.mSyncRandomOffset));
            int M = this.mMasterSyncAutomatically.size();
            for (int m = 0; m < M; m++) {
                int userId = this.mMasterSyncAutomatically.keyAt(m);
                out.startTag(null, XML_TAG_LISTEN_FOR_TICKLES);
                out.attribute(null, XML_ATTR_USER, Integer.toString(userId));
                out.attribute(null, XML_ATTR_ENABLED, Boolean.toString(this.mMasterSyncAutomatically.valueAt(m).booleanValue()));
                out.endTag(null, XML_TAG_LISTEN_FOR_TICKLES);
            }
            int N = this.mAuthorities.size();
            for (int i = 0; i < N; i++) {
                AuthorityInfo authority = this.mAuthorities.valueAt(i);
                EndPoint info = authority.target;
                out.startTag(null, "authority");
                out.attribute(null, "id", Integer.toString(authority.ident));
                out.attribute(null, XML_ATTR_USER, Integer.toString(info.userId));
                out.attribute(null, XML_ATTR_ENABLED, Boolean.toString(authority.enabled));
                out.attribute(null, "account", info.account.name);
                out.attribute(null, DatabaseHelper.SoundModelContract.KEY_TYPE, info.account.type);
                out.attribute(null, "authority", info.provider);
                out.attribute(null, "syncable", Integer.toString(authority.syncable));
                out.endTag(null, "authority");
            }
            out.endTag(null, "accounts");
            out.endDocument();
            this.mAccountInfoFile.finishWrite(fos);
        } catch (IOException e1) {
            Slog.w("SyncManager", "Error writing accounts", e1);
            if (fos != null) {
                this.mAccountInfoFile.failWrite(fos);
            }
        }
    }

    private void readStatusLocked() {
        if (Log.isLoggable(TAG_FILE, 2)) {
            Slog.v(TAG_FILE, "Reading " + this.mStatusFile.getBaseFile());
        }
        try {
            byte[] data = this.mStatusFile.readFully();
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            while (true) {
                int token = in.readInt();
                if (token == 0) {
                    return;
                }
                if (token == 100) {
                    SyncStatusInfo status = new SyncStatusInfo(in);
                    if (this.mAuthorities.indexOfKey(status.authorityId) >= 0) {
                        status.pending = false;
                        if (Log.isLoggable(TAG_FILE, 2)) {
                            Slog.v(TAG_FILE, "Adding status for id " + status.authorityId);
                        }
                        this.mSyncStatus.put(status.authorityId, status);
                    }
                } else {
                    Slog.w("SyncManager", "Unknown status token: " + token);
                    return;
                }
            }
        } catch (IOException e) {
            Slog.i("SyncManager", "No initial status");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeStatusLocked() {
        if (Log.isLoggable(TAG_FILE, 2)) {
            Slog.v(TAG_FILE, "Writing new " + this.mStatusFile.getBaseFile());
        }
        this.mHandler.removeMessages(1);
        try {
            FileOutputStream fos = this.mStatusFile.startWrite();
            Parcel out = Parcel.obtain();
            int N = this.mSyncStatus.size();
            for (int i = 0; i < N; i++) {
                out.writeInt(100);
                this.mSyncStatus.valueAt(i).writeToParcel(out, 0);
            }
            out.writeInt(0);
            fos.write(out.marshall());
            out.recycle();
            this.mStatusFile.finishWrite(fos);
        } catch (IOException e1) {
            Slog.w("SyncManager", "Error writing status", e1);
            if (0 != 0) {
                this.mStatusFile.failWrite(null);
            }
        }
    }

    private void requestSync(AuthorityInfo authorityInfo, int reason, Bundle extras, int syncExemptionFlag, int callingUid, int callingPid) {
        OnSyncRequestListener onSyncRequestListener;
        if (Process.myUid() != 1000 || (onSyncRequestListener = this.mSyncRequestListener) == null) {
            SyncRequest.Builder req = new SyncRequest.Builder().syncOnce().setExtras(extras);
            req.setSyncAdapter(authorityInfo.target.account, authorityInfo.target.provider);
            ContentResolver.requestSync(req.build());
            return;
        }
        onSyncRequestListener.onSyncRequest(authorityInfo.target, reason, extras, syncExemptionFlag, callingUid, callingPid);
    }

    private void requestSync(Account account, int userId, int reason, String authority, Bundle extras, int syncExemptionFlag, int callingUid, int callingPid) {
        if (Process.myUid() == 1000) {
            OnSyncRequestListener onSyncRequestListener = this.mSyncRequestListener;
            if (onSyncRequestListener != null) {
                onSyncRequestListener.onSyncRequest(new EndPoint(account, authority, userId), reason, extras, syncExemptionFlag, callingUid, callingPid);
                return;
            }
        }
        ContentResolver.requestSync(account, authority, extras);
    }

    private void readStatisticsLocked() {
        try {
            byte[] data = this.mStatisticsFile.readFully();
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            int index = 0;
            while (true) {
                int token = in.readInt();
                if (token != 0) {
                    if (token != 101) {
                        if (token != 100) {
                            Slog.w("SyncManager", "Unknown stats token: " + token);
                            return;
                        }
                    }
                    int day = in.readInt();
                    if (token == 100) {
                        day = (day - 2009) + 14245;
                    }
                    DayStats ds = new DayStats(day);
                    ds.successCount = in.readInt();
                    ds.successTime = in.readLong();
                    ds.failureCount = in.readInt();
                    ds.failureTime = in.readLong();
                    if (index < this.mDayStats.length) {
                        this.mDayStats[index] = ds;
                        index++;
                    }
                } else {
                    return;
                }
            }
        } catch (IOException e) {
            Slog.i("SyncManager", "No initial statistics");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeStatisticsLocked() {
        if (Log.isLoggable(TAG_FILE, 2)) {
            Slog.v("SyncManager", "Writing new " + this.mStatisticsFile.getBaseFile());
        }
        this.mHandler.removeMessages(2);
        try {
            FileOutputStream fos = this.mStatisticsFile.startWrite();
            Parcel out = Parcel.obtain();
            int N = this.mDayStats.length;
            int i = 0;
            while (true) {
                if (i >= N) {
                    break;
                }
                DayStats ds = this.mDayStats[i];
                if (ds == null) {
                    break;
                }
                out.writeInt(101);
                out.writeInt(ds.day);
                out.writeInt(ds.successCount);
                out.writeLong(ds.successTime);
                out.writeInt(ds.failureCount);
                out.writeLong(ds.failureTime);
                i++;
            }
            out.writeInt(0);
            fos.write(out.marshall());
            out.recycle();
            this.mStatisticsFile.finishWrite(fos);
        } catch (IOException e1) {
            Slog.w("SyncManager", "Error writing stats", e1);
            if (0 != 0) {
                this.mStatisticsFile.failWrite(null);
            }
        }
    }

    public void queueBackup() {
        BackupManager.dataChanged(PackageManagerService.PLATFORM_PACKAGE_NAME);
    }

    public void setClockValid() {
        if (!this.mIsClockValid) {
            this.mIsClockValid = true;
            Slog.w("SyncManager", "Clock is valid now.");
        }
    }

    public boolean isClockValid() {
        return this.mIsClockValid;
    }

    public void resetTodayStats(boolean force) {
        if (force) {
            Log.w("SyncManager", "Force resetting today stats.");
        }
        synchronized (this.mAuthorities) {
            int N = this.mSyncStatus.size();
            for (int i = 0; i < N; i++) {
                this.mSyncStatus.valueAt(i).maybeResetTodayStats(isClockValid(), force);
            }
            writeStatusLocked();
        }
    }
}

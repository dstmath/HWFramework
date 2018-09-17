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
import android.content.SyncRequest.Builder;
import android.content.SyncStatusInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.hdm.HwDeviceManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.EventLog;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import android.widget.Toast;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.UiThread;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.audio.AudioService;
import com.android.server.usage.UnixCalendar;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
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

public class SyncStorageEngine extends Handler {
    private static final int ACCOUNTS_VERSION = 3;
    private static final double DEFAULT_FLEX_PERCENT_SYNC = 0.04d;
    private static final long DEFAULT_MIN_FLEX_ALLOWED_SECS = 5;
    private static final long DEFAULT_POLL_FREQUENCY_SECONDS = 86400;
    public static final int EVENT_START = 0;
    public static final int EVENT_STOP = 1;
    public static final int MAX_HISTORY = 100;
    public static final String MESG_CANCELED = "canceled";
    public static final String MESG_SUCCESS = "success";
    static final long MILLIS_IN_4WEEKS = 2419200000L;
    private static final int MSG_WRITE_STATISTICS = 2;
    private static final int MSG_WRITE_STATUS = 1;
    public static final long NOT_IN_BACKOFF_MODE = -1;
    public static final String[] SOURCES = new String[]{"SERVER", "LOCAL", "POLL", "USER", "PERIODIC", "SERVICE"};
    public static final int SOURCE_LOCAL = 1;
    public static final int SOURCE_PERIODIC = 4;
    public static final int SOURCE_POLL = 2;
    public static final int SOURCE_SERVER = 0;
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
    private static HashMap<String, String> sAuthorityRenames = new HashMap();
    private static volatile SyncStorageEngine sSyncStorageEngine = null;
    private final AtomicFile mAccountInfoFile;
    private final HashMap<AccountAndUser, AccountInfo> mAccounts = new HashMap();
    private final SparseArray<AuthorityInfo> mAuthorities = new SparseArray();
    private OnAuthorityRemovedListener mAuthorityRemovedListener;
    private final Calendar mCal;
    private final RemoteCallbackList<ISyncStatusObserver> mChangeListeners = new RemoteCallbackList();
    private final Context mContext;
    private final SparseArray<ArrayList<SyncInfo>> mCurrentSyncs = new SparseArray();
    private final DayStats[] mDayStats = new DayStats[28];
    private boolean mDefaultMasterSyncAutomatically;
    private boolean mGrantSyncAdaptersAccountAccess;
    private SparseArray<Boolean> mMasterSyncAutomatically = new SparseArray();
    private int mNextAuthorityId = 0;
    private int mNextHistoryId = 0;
    private final ArrayMap<ComponentName, SparseArray<AuthorityInfo>> mServices = new ArrayMap();
    private final AtomicFile mStatisticsFile;
    private final AtomicFile mStatusFile;
    private final ArrayList<SyncHistoryItem> mSyncHistory = new ArrayList();
    private int mSyncRandomOffset;
    private OnSyncRequestListener mSyncRequestListener;
    private final SparseArray<SyncStatusInfo> mSyncStatus = new SparseArray();
    private int mYear;
    private int mYearInDays;

    interface OnAuthorityRemovedListener {
        void onAuthorityRemoved(EndPoint endPoint);
    }

    interface OnSyncRequestListener {
        void onSyncRequest(EndPoint endPoint, int i, Bundle bundle);
    }

    interface PeriodicSyncAddedListener {
        void onPeriodicSyncAdded(EndPoint endPoint, Bundle bundle, long j, long j2);
    }

    private static class AccountAuthorityValidator {
        private final AccountManager mAccountManager;
        private final SparseArray<Account[]> mAccountsCache = new SparseArray();
        private final PackageManager mPackageManager;
        private final SparseArray<ArrayMap<String, Boolean>> mProvidersPerUserCache = new SparseArray();

        AccountAuthorityValidator(Context context) {
            this.mAccountManager = (AccountManager) context.getSystemService(AccountManager.class);
            this.mPackageManager = context.getPackageManager();
        }

        boolean isAccountValid(Account account, int userId) {
            Account[] accountsForUser = (Account[]) this.mAccountsCache.get(userId);
            if (accountsForUser == null) {
                accountsForUser = this.mAccountManager.getAccountsAsUser(userId);
                this.mAccountsCache.put(userId, accountsForUser);
            }
            return ArrayUtils.contains(accountsForUser, account);
        }

        boolean isAuthorityValid(String authority, int userId) {
            ArrayMap<String, Boolean> authorityMap = (ArrayMap) this.mProvidersPerUserCache.get(userId);
            if (authorityMap == null) {
                authorityMap = new ArrayMap();
                this.mProvidersPerUserCache.put(userId, authorityMap);
            }
            if (!authorityMap.containsKey(authority)) {
                authorityMap.put(authority, Boolean.valueOf(this.mPackageManager.resolveContentProviderAsUser(authority, 786432, userId) != null));
            }
            return ((Boolean) authorityMap.get(authority)).booleanValue();
        }
    }

    static class AccountInfo {
        final AccountAndUser accountAndUser;
        final HashMap<String, AuthorityInfo> authorities = new HashMap();

        AccountInfo(AccountAndUser accountAndUser) {
            this.accountAndUser = accountAndUser;
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
            this.periodicSyncs = new ArrayList();
            for (PeriodicSync sync : toCopy.periodicSyncs) {
                this.periodicSyncs.add(new PeriodicSync(sync));
            }
        }

        AuthorityInfo(EndPoint info, int id) {
            this.target = info;
            this.ident = id;
            this.enabled = false;
            this.periodicSyncs = new ArrayList();
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

        public DayStats(int day) {
            this.day = day;
        }
    }

    public static class EndPoint {
        public static final EndPoint USER_ALL_PROVIDER_ALL_ACCOUNTS_ALL = new EndPoint(null, null, -1);
        final Account account;
        final String provider;
        final int userId;

        public EndPoint(Account account, String provider, int userId) {
            this.account = account;
            this.provider = provider;
            this.userId = userId;
        }

        public boolean matchesSpec(EndPoint spec) {
            if (this.userId != spec.userId && this.userId != -1 && spec.userId != -1) {
                return false;
            }
            boolean accountsMatch;
            boolean providersMatch;
            if (spec.account == null) {
                accountsMatch = true;
            } else {
                accountsMatch = this.account.equals(spec.account);
            }
            if (spec.provider == null) {
                providersMatch = true;
            } else {
                providersMatch = this.provider.equals(spec.provider);
            }
            if (!accountsMatch) {
                providersMatch = false;
            }
            return providersMatch;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(this.account == null ? "ALL ACCS" : "XXXXXXXXX").append("/").append(this.provider == null ? "ALL PDRS" : this.provider);
            sb.append(":u").append(this.userId);
            return sb.toString();
        }
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
        long upstreamActivity;
    }

    static {
        sAuthorityRenames.put("contacts", "com.android.contacts");
        sAuthorityRenames.put("calendar", "com.android.calendar");
    }

    private SyncStorageEngine(Context context, File dataDir) {
        this.mContext = context;
        sSyncStorageEngine = this;
        this.mCal = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"));
        this.mDefaultMasterSyncAutomatically = this.mContext.getResources().getBoolean(17957031);
        File syncDir = new File(new File(dataDir, "system"), "sync");
        syncDir.mkdirs();
        maybeDeleteLegacyPendingInfoLocked(syncDir);
        this.mAccountInfoFile = new AtomicFile(new File(syncDir, "accounts.xml"));
        this.mStatusFile = new AtomicFile(new File(syncDir, "status.bin"));
        this.mStatisticsFile = new AtomicFile(new File(syncDir, "stats.bin"));
        readAccountInfoLocked();
        readStatusLocked();
        readStatisticsLocked();
        readAndDeleteLegacyAccountInfoLocked();
        writeAccountInfoLocked();
        writeStatusLocked();
        writeStatisticsLocked();
    }

    public static SyncStorageEngine newTestInstance(Context context) {
        return new SyncStorageEngine(context, context.getFilesDir());
    }

    public static void init(Context context) {
        if (sSyncStorageEngine == null) {
            sSyncStorageEngine = new SyncStorageEngine(context, Environment.getDataDirectory());
        }
    }

    public static SyncStorageEngine getSingleton() {
        if (sSyncStorageEngine != null) {
            return sSyncStorageEngine;
        }
        throw new IllegalStateException("not initialized");
    }

    protected void setOnSyncRequestListener(OnSyncRequestListener listener) {
        if (this.mSyncRequestListener == null) {
            this.mSyncRequestListener = listener;
        }
    }

    protected void setOnAuthorityRemovedListener(OnAuthorityRemovedListener listener) {
        if (this.mAuthorityRemovedListener == null) {
            this.mAuthorityRemovedListener = listener;
        }
    }

    protected void setPeriodicSyncAddedListener(PeriodicSyncAddedListener listener) {
        if (mPeriodicSyncAddedListener == null) {
            mPeriodicSyncAddedListener = listener;
        }
    }

    public void handleMessage(Message msg) {
        SparseArray sparseArray;
        if (msg.what == 1) {
            sparseArray = this.mAuthorities;
            synchronized (sparseArray) {
                writeStatusLocked();
            }
        } else if (msg.what == 2) {
            sparseArray = this.mAuthorities;
            synchronized (sparseArray) {
                writeStatisticsLocked();
            }
        } else {
            return;
        }
    }

    public int getSyncRandomOffset() {
        return this.mSyncRandomOffset;
    }

    public void addStatusChangeListener(int mask, ISyncStatusObserver callback) {
        synchronized (this.mAuthorities) {
            this.mChangeListeners.register(callback, Integer.valueOf(mask));
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

    /* JADX WARNING: Missing block: B:19:0x0040, code:
            if (android.util.Log.isLoggable("SyncManager", 2) == false) goto L_0x0067;
     */
    /* JADX WARNING: Missing block: B:20:0x0042, code:
            android.util.Slog.v("SyncManager", "reportChange " + r9 + " to: " + r4);
     */
    /* JADX WARNING: Missing block: B:21:0x0067, code:
            if (r4 == null) goto L_?;
     */
    /* JADX WARNING: Missing block: B:22:0x0069, code:
            r1 = r4.size();
     */
    /* JADX WARNING: Missing block: B:23:0x006d, code:
            if (r1 <= 0) goto L_0x0080;
     */
    /* JADX WARNING: Missing block: B:24:0x006f, code:
            r1 = r1 - 1;
     */
    /* JADX WARNING: Missing block: B:26:?, code:
            ((android.content.ISyncStatusObserver) r4.get(r1)).onStatusChanged(r9);
     */
    /* JADX WARNING: Missing block: B:44:?, code:
            return;
     */
    /* JADX WARNING: Missing block: B:45:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void reportChange(int which) {
        Throwable th;
        synchronized (this.mAuthorities) {
            try {
                int i = this.mChangeListeners.beginBroadcast();
                ArrayList<ISyncStatusObserver> reports = null;
                while (i > 0) {
                    i--;
                    ArrayList<ISyncStatusObserver> reports2;
                    try {
                        if ((((Integer) this.mChangeListeners.getBroadcastCookie(i)).intValue() & which) != 0) {
                            if (reports == null) {
                                reports2 = new ArrayList(i);
                            } else {
                                reports2 = reports;
                            }
                            reports2.add((ISyncStatusObserver) this.mChangeListeners.getBroadcastItem(i));
                            reports = reports2;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        reports2 = reports;
                        throw th;
                    }
                }
                this.mChangeListeners.finishBroadcast();
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0017, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean getSyncAutomatically(Account account, int userId, String providerName) {
        boolean z = false;
        synchronized (this.mAuthorities) {
            if (account != null) {
                AuthorityInfo authority = getAuthorityLocked(new EndPoint(account, providerName, userId), "getSyncAutomatically");
                if (authority != null) {
                    z = authority.enabled;
                }
            } else {
                int i = this.mAuthorities.size();
                while (i > 0) {
                    i--;
                    AuthorityInfo authorityInfo = (AuthorityInfo) this.mAuthorities.valueAt(i);
                    if (authorityInfo.target.matchesSpec(new EndPoint(account, providerName, userId)) && authorityInfo.enabled) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0078, code:
            return;
     */
    /* JADX WARNING: Missing block: B:30:0x00b5, code:
            if (r11 == false) goto L_0x00c4;
     */
    /* JADX WARNING: Missing block: B:31:0x00b7, code:
            requestSync(r8, r9, -6, r10, new android.os.Bundle());
     */
    /* JADX WARNING: Missing block: B:32:0x00c4, code:
            reportChange(1);
            queueBackup();
     */
    /* JADX WARNING: Missing block: B:33:0x00cb, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setSyncAutomatically(Account account, int userId, String providerName, boolean sync) {
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.d("SyncManager", "setSyncAutomatically:  provider " + providerName + ", user " + userId + " -> " + sync);
        }
        synchronized (this.mAuthorities) {
            AuthorityInfo authority = getOrCreateAuthorityLocked(new EndPoint(account, providerName, userId), -1, false);
            if (authority.enabled != sync) {
                if (sync && account != null) {
                    if (HwDeviceManager.disallowOp(25, account.type) && HwDeviceManager.disallowOp(24, providerName)) {
                        Slog.i("SyncManager", "setSyncAutomatically() is not allowed for google account by MDM!");
                        UiThread.getHandler().post(new Runnable() {
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
                    if (authority.syncable == 2) {
                        authority.syncable = -1;
                    }
                }
                authority.enabled = sync;
                writeAccountInfoLocked();
            } else if (Log.isLoggable("SyncManager", 2)) {
                Slog.d("SyncManager", "setSyncAutomatically: already set to " + sync + ", doing nothing");
            }
        }
    }

    public int getIsSyncable(Account account, int userId, String providerName) {
        synchronized (this.mAuthorities) {
            int i;
            if (account != null) {
                AuthorityInfo authority = getAuthorityLocked(new EndPoint(account, providerName, userId), "get authority syncable");
                if (authority == null) {
                    return -1;
                }
                i = authority.syncable;
                return i;
            }
            int i2 = this.mAuthorities.size();
            while (i2 > 0) {
                i2--;
                AuthorityInfo authorityInfo = (AuthorityInfo) this.mAuthorities.valueAt(i2);
                if (authorityInfo.target != null && authorityInfo.target.provider.equals(providerName)) {
                    i = authorityInfo.syncable;
                    return i;
                }
            }
            return -1;
        }
    }

    public void setIsSyncable(Account account, int userId, String providerName, int syncable) {
        setSyncableStateForEndPoint(new EndPoint(account, providerName, userId), syncable);
    }

    /* JADX WARNING: Missing block: B:16:0x0071, code:
            return;
     */
    /* JADX WARNING: Missing block: B:20:0x0078, code:
            if (r8 != 1) goto L_0x0083;
     */
    /* JADX WARNING: Missing block: B:21:0x007a, code:
            requestSync(r0, -5, new android.os.Bundle());
     */
    /* JADX WARNING: Missing block: B:22:0x0083, code:
            reportChange(1);
     */
    /* JADX WARNING: Missing block: B:23:0x0086, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setSyncableStateForEndPoint(EndPoint target, int syncable) {
        synchronized (this.mAuthorities) {
            AuthorityInfo aInfo = getOrCreateAuthorityLocked(target, -1, false);
            if (syncable < -1) {
                syncable = -1;
            }
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.d("SyncManager", "setIsSyncable: " + aInfo.toString() + " -> " + syncable);
            }
            if (aInfo.syncable != syncable) {
                aInfo.syncable = syncable;
                writeAccountInfoLocked();
            } else if (Log.isLoggable("SyncManager", 2)) {
                Slog.d("SyncManager", "setIsSyncable: already set to " + syncable + ", doing nothing");
            }
        }
    }

    public Pair<Long, Long> getBackoff(EndPoint info) {
        synchronized (this.mAuthorities) {
            AuthorityInfo authority = getAuthorityLocked(info, "getBackoff");
            if (authority != null) {
                Pair<Long, Long> create = Pair.create(Long.valueOf(authority.backoffTime), Long.valueOf(authority.backoffDelay));
                return create;
            }
            return null;
        }
    }

    public void setBackoff(EndPoint info, long nextSyncTime, long nextDelay) {
        boolean changed;
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "setBackoff: " + info + " -> nextSyncTime " + nextSyncTime + ", nextDelay " + nextDelay);
        }
        synchronized (this.mAuthorities) {
            if (info.account == null || info.provider == null) {
                changed = setBackoffLocked(info.account, info.userId, info.provider, nextSyncTime, nextDelay);
            } else {
                AuthorityInfo authorityInfo = getOrCreateAuthorityLocked(info, -1, true);
                if (authorityInfo.backoffTime == nextSyncTime && authorityInfo.backoffDelay == nextDelay) {
                    changed = false;
                } else {
                    authorityInfo.backoffTime = nextSyncTime;
                    authorityInfo.backoffDelay = nextDelay;
                    changed = true;
                }
            }
        }
        if (changed) {
            reportChange(1);
        }
    }

    private boolean setBackoffLocked(Account account, int userId, String providerName, long nextSyncTime, long nextDelay) {
        boolean changed = false;
        for (AccountInfo accountInfo : this.mAccounts.values()) {
            if (account == null || (account.equals(accountInfo.accountAndUser.account) ^ 1) == 0 || userId == accountInfo.accountAndUser.userId) {
                for (AuthorityInfo authorityInfo : accountInfo.authorities.values()) {
                    if ((providerName == null || (providerName.equals(authorityInfo.target.provider) ^ 1) == 0) && !(authorityInfo.backoffTime == nextSyncTime && authorityInfo.backoffDelay == nextDelay)) {
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
        boolean changed = false;
        synchronized (this.mAuthorities) {
            for (AccountInfo accountInfo : this.mAccounts.values()) {
                for (AuthorityInfo authorityInfo : accountInfo.authorities.values()) {
                    if (authorityInfo.backoffTime != -1 || authorityInfo.backoffDelay != -1) {
                        if (Log.isLoggable("SyncManager", 2)) {
                            Slog.v("SyncManager", "clearAllBackoffsLocked: authority:" + authorityInfo.target + " account:" + accountInfo.accountAndUser.account.name + " user:" + accountInfo.accountAndUser.userId + " backoffTime was: " + authorityInfo.backoffTime + " backoffDelay was: " + authorityInfo.backoffDelay);
                        }
                        authorityInfo.backoffTime = -1;
                        authorityInfo.backoffDelay = -1;
                        changed = true;
                    }
                }
            }
        }
        if (changed) {
            reportChange(1);
        }
    }

    public long getDelayUntilTime(EndPoint info) {
        synchronized (this.mAuthorities) {
            AuthorityInfo authority = getAuthorityLocked(info, "getDelayUntil");
            if (authority == null) {
                return 0;
            }
            long j = authority.delayUntil;
            return j;
        }
    }

    public void setDelayUntilTime(EndPoint info, long delayUntil) {
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "setDelayUntil: " + info + " -> delayUntil " + delayUntil);
        }
        synchronized (this.mAuthorities) {
            AuthorityInfo authority = getOrCreateAuthorityLocked(info, -1, true);
            if (authority.delayUntil == delayUntil) {
                return;
            }
            authority.delayUntil = delayUntil;
            reportChange(1);
        }
    }

    boolean restoreAllPeriodicSyncs() {
        if (mPeriodicSyncAddedListener == null) {
            return false;
        }
        synchronized (this.mAuthorities) {
            for (int i = 0; i < this.mAuthorities.size(); i++) {
                AuthorityInfo authority = (AuthorityInfo) this.mAuthorities.valueAt(i);
                for (PeriodicSync periodicSync : authority.periodicSyncs) {
                    mPeriodicSyncAddedListener.onPeriodicSyncAdded(authority.target, periodicSync.extras, periodicSync.period, periodicSync.flexTime);
                }
                authority.periodicSyncs.clear();
            }
            writeAccountInfoLocked();
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:12:0x0027, code:
            if (r8 == false) goto L_0x0035;
     */
    /* JADX WARNING: Missing block: B:13:0x0029, code:
            requestSync(null, r9, -7, null, new android.os.Bundle());
     */
    /* JADX WARNING: Missing block: B:14:0x0035, code:
            reportChange(1);
            r7.mContext.sendBroadcast(android.content.ContentResolver.ACTION_SYNC_CONN_STATUS_CHANGED);
            queueBackup();
     */
    /* JADX WARNING: Missing block: B:15:0x0043, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setMasterSyncAutomatically(boolean flag, int userId) {
        synchronized (this.mAuthorities) {
            Boolean auto = (Boolean) this.mMasterSyncAutomatically.get(userId);
            if (auto == null || !auto.equals(Boolean.valueOf(flag))) {
                this.mMasterSyncAutomatically.put(userId, Boolean.valueOf(flag));
                writeAccountInfoLocked();
            }
        }
    }

    public boolean getMasterSyncAutomatically(int userId) {
        boolean booleanValue;
        synchronized (this.mAuthorities) {
            Boolean auto = (Boolean) this.mMasterSyncAutomatically.get(userId);
            booleanValue = auto == null ? this.mDefaultMasterSyncAutomatically : auto.booleanValue();
        }
        return booleanValue;
    }

    public AuthorityInfo getAuthority(int authorityId) {
        AuthorityInfo authorityInfo;
        synchronized (this.mAuthorities) {
            authorityInfo = (AuthorityInfo) this.mAuthorities.get(authorityId);
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
            if (authority == null) {
                return;
            }
            getOrCreateSyncStatusLocked(authority.ident).pending = pendingValue;
            reportChange(2);
        }
    }

    /* JADX WARNING: Missing block: B:27:0x008e, code:
            if (r4 > 0) goto L_0x0090;
     */
    /* JADX WARNING: Missing block: B:28:0x0090, code:
            if (r4 <= 0) goto L_0x00e8;
     */
    /* JADX WARNING: Missing block: B:29:0x0092, code:
            r4 = r4 - 1;
            r5 = r7.keyAt(r4);
            r2 = (com.android.server.content.SyncStorageEngine.AuthorityInfo) r7.valueAt(r4);
     */
    /* JADX WARNING: Missing block: B:30:0x00a0, code:
            if (r12.mAuthorityRemovedListener == null) goto L_0x00a9;
     */
    /* JADX WARNING: Missing block: B:31:0x00a2, code:
            r12.mAuthorityRemovedListener.onAuthorityRemoved(r2.target);
     */
    /* JADX WARNING: Missing block: B:32:0x00a9, code:
            r12.mAuthorities.remove(r5);
            r6 = r12.mSyncStatus.size();
     */
    /* JADX WARNING: Missing block: B:33:0x00b4, code:
            if (r6 <= 0) goto L_0x00cc;
     */
    /* JADX WARNING: Missing block: B:34:0x00b6, code:
            r6 = r6 - 1;
     */
    /* JADX WARNING: Missing block: B:35:0x00be, code:
            if (r12.mSyncStatus.keyAt(r6) != r5) goto L_0x00b4;
     */
    /* JADX WARNING: Missing block: B:36:0x00c0, code:
            r12.mSyncStatus.remove(r12.mSyncStatus.keyAt(r6));
     */
    /* JADX WARNING: Missing block: B:37:0x00cc, code:
            r6 = r12.mSyncHistory.size();
     */
    /* JADX WARNING: Missing block: B:38:0x00d2, code:
            if (r6 <= 0) goto L_0x0090;
     */
    /* JADX WARNING: Missing block: B:39:0x00d4, code:
            r6 = r6 - 1;
     */
    /* JADX WARNING: Missing block: B:40:0x00e0, code:
            if (((com.android.server.content.SyncStorageEngine.SyncHistoryItem) r12.mSyncHistory.get(r6)).authorityId != r5) goto L_0x00d2;
     */
    /* JADX WARNING: Missing block: B:41:0x00e2, code:
            r12.mSyncHistory.remove(r6);
     */
    /* JADX WARNING: Missing block: B:42:0x00e8, code:
            writeAccountInfoLocked();
            writeStatusLocked();
            writeStatisticsLocked();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void doDatabaseCleanup(Account[] accounts, int userId) {
        synchronized (this.mAuthorities) {
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "Updating for new accounts...");
            }
            SparseArray<AuthorityInfo> removing = new SparseArray();
            Iterator<AccountInfo> accIt = this.mAccounts.values().iterator();
            while (accIt.hasNext()) {
                AccountInfo acc = (AccountInfo) accIt.next();
                if (!ArrayUtils.contains(accounts, acc.accountAndUser.account) && acc.accountAndUser.userId == userId) {
                    if (Log.isLoggable("SyncManager", 2)) {
                        Slog.v("SyncManager", "Account removed: " + acc.accountAndUser);
                    }
                    for (AuthorityInfo auth : acc.authorities.values()) {
                        removing.put(auth.ident, auth);
                    }
                    accIt.remove();
                }
            }
            int i = removing.size();
        }
    }

    public SyncInfo addActiveSync(ActiveSyncContext activeSyncContext) {
        SyncInfo syncInfo;
        synchronized (this.mAuthorities) {
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "setActiveSync: account= auth=" + activeSyncContext.mSyncOperation.target + " src=" + activeSyncContext.mSyncOperation.syncSource + " extras=" + activeSyncContext.mSyncOperation.extras);
            }
            AuthorityInfo authorityInfo = getOrCreateAuthorityLocked(activeSyncContext.mSyncOperation.target, -1, true);
            syncInfo = new SyncInfo(authorityInfo.ident, authorityInfo.target.account, authorityInfo.target.provider, activeSyncContext.mStartTime);
            getCurrentSyncs(authorityInfo.target.userId).add(syncInfo);
        }
        reportActiveChange();
        return syncInfo;
    }

    public void removeActiveSync(SyncInfo syncInfo, int userId) {
        synchronized (this.mAuthorities) {
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "removeActiveSync: account=" + syncInfo.account + " user=" + userId + " auth=" + syncInfo.authority);
            }
            getCurrentSyncs(userId).remove(syncInfo);
        }
        reportActiveChange();
    }

    public void reportActiveChange() {
        reportChange(4);
    }

    /* JADX WARNING: Missing block: B:28:0x00ae, code:
            reportChange(8);
     */
    /* JADX WARNING: Missing block: B:29:0x00b3, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
            this.mSyncHistory.add(0, item);
            while (this.mSyncHistory.size() > 100) {
                this.mSyncHistory.remove(this.mSyncHistory.size() - 1);
            }
            long id = (long) item.historyId;
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "returning historyId " + id);
            }
        }
    }

    /* JADX WARNING: Missing block: B:34:0x0104, code:
            reportChange(8);
     */
    /* JADX WARNING: Missing block: B:35:0x010b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void stopSyncEvent(long historyId, long elapsedTime, String resultMessage, long downstreamActivity, long upstreamActivity) {
        synchronized (this.mAuthorities) {
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "stopSyncEvent: historyId=" + historyId);
            }
            SyncHistoryItem item = null;
            int i = this.mSyncHistory.size();
            while (i > 0) {
                i--;
                item = (SyncHistoryItem) this.mSyncHistory.get(i);
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
            item.upstreamActivity = upstreamActivity;
            SyncStatusInfo status = getOrCreateSyncStatusLocked(item.authorityId);
            status.numSyncs++;
            status.totalElapsedTime += elapsedTime;
            switch (item.source) {
                case 0:
                    status.numSourceServer++;
                    break;
                case 1:
                    status.numSourceLocal++;
                    break;
                case 2:
                    status.numSourcePoll++;
                    break;
                case 3:
                    status.numSourceUser++;
                    break;
                case 4:
                    status.numSourcePeriodic++;
                    break;
            }
            boolean writeStatisticsNow = false;
            int day = getCurrentDayLocked();
            if (this.mDayStats[0] == null) {
                this.mDayStats[0] = new DayStats(day);
            } else if (day != this.mDayStats[0].day) {
                System.arraycopy(this.mDayStats, 0, this.mDayStats, 1, this.mDayStats.length - 1);
                this.mDayStats[0] = new DayStats(day);
                writeStatisticsNow = true;
            } else {
                DayStats dayStats = this.mDayStats[0];
            }
            DayStats ds = this.mDayStats[0];
            long lastSyncTime = item.eventTime + elapsedTime;
            boolean writeStatusNow = false;
            if (MESG_SUCCESS.equals(resultMessage)) {
                if (status.lastSuccessTime == 0 || status.lastFailureTime != 0) {
                    writeStatusNow = true;
                }
                status.lastSuccessTime = lastSyncTime;
                status.lastSuccessSource = item.source;
                status.lastFailureTime = 0;
                status.lastFailureSource = -1;
                status.lastFailureMesg = null;
                status.initialFailureTime = 0;
                ds.successCount++;
                ds.successTime += elapsedTime;
            } else if (!MESG_CANCELED.equals(resultMessage)) {
                if (status.lastFailureTime == 0) {
                    writeStatusNow = true;
                }
                status.lastFailureTime = lastSyncTime;
                status.lastFailureSource = item.source;
                status.lastFailureMesg = resultMessage;
                if (status.initialFailureTime == 0) {
                    status.initialFailureTime = lastSyncTime;
                }
                ds.failureCount++;
                ds.failureTime += elapsedTime;
            }
            if (writeStatusNow) {
                writeStatusLocked();
            } else if (!hasMessages(1)) {
                sendMessageDelayed(obtainMessage(1), 600000);
            }
            if (writeStatisticsNow) {
                writeStatisticsLocked();
            } else if (!hasMessages(2)) {
                sendMessageDelayed(obtainMessage(2), 1800000);
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
        synchronized (this.mAuthorities) {
            List<SyncInfo> syncs = getCurrentSyncsLocked(userId);
            syncsCopy = new ArrayList();
            for (SyncInfo sync : syncs) {
                SyncInfo copy;
                if (canAccessAccounts) {
                    copy = new SyncInfo(sync);
                } else {
                    copy = SyncInfo.createAccountRedacted(sync.authorityId, sync.authority, sync.startTime);
                }
                syncsCopy.add(copy);
            }
        }
        return syncsCopy;
    }

    private List<SyncInfo> getCurrentSyncsLocked(int userId) {
        ArrayList<SyncInfo> syncs = (ArrayList) this.mCurrentSyncs.get(userId);
        if (syncs != null) {
            return syncs;
        }
        syncs = new ArrayList();
        this.mCurrentSyncs.put(userId, syncs);
        return syncs;
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
            int i = 0;
            while (i < N) {
                SyncStatusInfo cur = (SyncStatusInfo) this.mSyncStatus.valueAt(i);
                AuthorityInfo ainfo = (AuthorityInfo) this.mAuthorities.get(cur.authorityId);
                if (ainfo == null || !ainfo.target.matchesSpec(info)) {
                    i++;
                } else {
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
                SyncStatusInfo cur = (SyncStatusInfo) this.mSyncStatus.valueAt(i);
                AuthorityInfo ainfo = (AuthorityInfo) this.mAuthorities.get(cur.authorityId);
                if (ainfo != null && ainfo.target.matchesSpec(info) && cur.pending) {
                    return true;
                }
            }
            return false;
        }
    }

    public ArrayList<SyncHistoryItem> getSyncHistory() {
        ArrayList<SyncHistoryItem> items;
        synchronized (this.mAuthorities) {
            int N = this.mSyncHistory.size();
            items = new ArrayList(N);
            for (int i = 0; i < N; i++) {
                items.add((SyncHistoryItem) this.mSyncHistory.get(i));
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
            this.mYearInDays = (int) (this.mCal.getTimeInMillis() / UnixCalendar.DAY_IN_MILLIS);
        }
        return this.mYearInDays + dayOfYear;
    }

    private AuthorityInfo getAuthorityLocked(EndPoint info, String tag) {
        AccountAndUser au = new AccountAndUser(info.account, info.userId);
        AccountInfo accountInfo = (AccountInfo) this.mAccounts.get(au);
        if (accountInfo == null) {
            if (tag != null && Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", tag + ": unknown account " + au);
            }
            return null;
        }
        AuthorityInfo authority = (AuthorityInfo) accountInfo.authorities.get(info.provider);
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
        AccountInfo account = (AccountInfo) this.mAccounts.get(au);
        if (account == null) {
            account = new AccountInfo(au);
            this.mAccounts.put(au, account);
        }
        AuthorityInfo authority = (AuthorityInfo) account.authorities.get(info.provider);
        if (authority != null) {
            return authority;
        }
        authority = createAuthorityLocked(info, ident, doWrite);
        account.authorities.put(info.provider, authority);
        return authority;
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
        AccountInfo accountInfo = (AccountInfo) this.mAccounts.get(new AccountAndUser(account, userId));
        if (accountInfo != null) {
            AuthorityInfo authorityInfo = (AuthorityInfo) accountInfo.authorities.remove(authorityName);
            if (authorityInfo != null) {
                if (this.mAuthorityRemovedListener != null) {
                    this.mAuthorityRemovedListener.onAuthorityRemoved(authorityInfo.target);
                }
                this.mAuthorities.remove(authorityInfo.ident);
                if (doWrite) {
                    writeAccountInfoLocked();
                }
            }
        }
    }

    private SyncStatusInfo getOrCreateSyncStatusLocked(int authorityId) {
        SyncStatusInfo status = (SyncStatusInfo) this.mSyncStatus.get(authorityId);
        if (status != null) {
            return status;
        }
        status = new SyncStatusInfo(authorityId);
        this.mSyncStatus.put(authorityId, status);
        return status;
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
            readAndDeleteLegacyAccountInfoLocked();
            writeAccountInfoLocked();
            writeStatusLocked();
            writeStatisticsLocked();
        }
    }

    private void readAccountInfoLocked() {
        int highestAuthorityId = -1;
        FileInputStream fis = null;
        try {
            fis = this.mAccountInfoFile.openRead();
            if (Log.isLoggable(TAG_FILE, 2)) {
                Slog.v(TAG_FILE, "Reading " + this.mAccountInfoFile.getBaseFile());
            }
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fis, StandardCharsets.UTF_8.name());
            int eventType = parser.getEventType();
            while (eventType != 2 && eventType != 1) {
                eventType = parser.next();
            }
            if (eventType == 1) {
                Slog.i("SyncManager", "No initial accounts");
                this.mNextAuthorityId = Math.max(0, this.mNextAuthorityId);
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                    }
                }
                return;
            }
            if ("accounts".equals(parser.getName())) {
                int version;
                String listen = parser.getAttributeValue(null, XML_ATTR_LISTEN_FOR_TICKLES);
                String versionString = parser.getAttributeValue(null, "version");
                if (versionString == null) {
                    version = 0;
                } else {
                    try {
                        version = Integer.parseInt(versionString);
                    } catch (NumberFormatException e2) {
                        version = 0;
                    }
                }
                if (version < 3) {
                    this.mGrantSyncAdaptersAccountAccess = true;
                }
                String nextIdString = parser.getAttributeValue(null, XML_ATTR_NEXT_AUTHORITY_ID);
                try {
                    this.mNextAuthorityId = Math.max(this.mNextAuthorityId, nextIdString == null ? 0 : Integer.parseInt(nextIdString));
                } catch (NumberFormatException e3) {
                }
                String offsetString = parser.getAttributeValue(null, XML_ATTR_SYNC_RANDOM_OFFSET);
                try {
                    this.mSyncRandomOffset = offsetString == null ? 0 : Integer.parseInt(offsetString);
                } catch (NumberFormatException e4) {
                    this.mSyncRandomOffset = 0;
                }
                if (this.mSyncRandomOffset == 0) {
                    this.mSyncRandomOffset = new Random(System.currentTimeMillis()).nextInt(86400);
                }
                this.mMasterSyncAutomatically.put(0, Boolean.valueOf(listen != null ? Boolean.parseBoolean(listen) : true));
                eventType = parser.next();
                AuthorityInfo authority = null;
                PeriodicSync periodicSync = null;
                AccountAuthorityValidator accountAuthorityValidator = new AccountAuthorityValidator(this.mContext);
                do {
                    if (eventType == 2) {
                        String tagName = parser.getName();
                        if (parser.getDepth() == 2) {
                            if ("authority".equals(tagName)) {
                                authority = parseAuthority(parser, version, accountAuthorityValidator);
                                periodicSync = null;
                                if (authority == null) {
                                    EventLog.writeEvent(1397638484, new Object[]{"26513719", Integer.valueOf(-1), "Malformed authority"});
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
                    }
                    eventType = parser.next();
                } while (eventType != 1);
            }
            this.mNextAuthorityId = Math.max(highestAuthorityId + 1, this.mNextAuthorityId);
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e5) {
                }
            }
            maybeMigrateSettingsForRenamedAuthorities();
        } catch (XmlPullParserException e6) {
            Slog.w("SyncManager", "Error reading accounts", e6);
            this.mNextAuthorityId = Math.max(highestAuthorityId + 1, this.mNextAuthorityId);
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e7) {
                }
            }
        } catch (IOException e8) {
            if (fis == null) {
                Slog.i("SyncManager", "No initial accounts");
            } else {
                Slog.w("SyncManager", "Error reading accounts", e8);
            }
            this.mNextAuthorityId = Math.max(highestAuthorityId + 1, this.mNextAuthorityId);
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e9) {
                }
            }
        } catch (Throwable th) {
            this.mNextAuthorityId = Math.max(highestAuthorityId + 1, this.mNextAuthorityId);
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e10) {
                }
            }
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
        ArrayList<AuthorityInfo> authoritiesToRemove = new ArrayList();
        int N = this.mAuthorities.size();
        for (int i = 0; i < N; i++) {
            AuthorityInfo authority = (AuthorityInfo) this.mAuthorities.valueAt(i);
            String newAuthorityName = (String) sAuthorityRenames.get(authority.target.provider);
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
        for (AuthorityInfo authorityInfo : authoritiesToRemove) {
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
        this.mMasterSyncAutomatically.put(userId, Boolean.valueOf(enabled != null ? Boolean.parseBoolean(enabled) : true));
    }

    private AuthorityInfo parseAuthority(XmlPullParser parser, int version, AccountAuthorityValidator validator) {
        AuthorityInfo authority = null;
        int id = -1;
        try {
            id = Integer.parseInt(parser.getAttributeValue(null, "id"));
        } catch (NumberFormatException e) {
            Slog.e("SyncManager", "error parsing the id of the authority", e);
        } catch (NullPointerException e2) {
            Slog.e("SyncManager", "the id of the authority is null", e2);
        }
        if (id >= 0) {
            String authorityName = parser.getAttributeValue(null, "authority");
            String enabled = parser.getAttributeValue(null, XML_ATTR_ENABLED);
            String syncable = parser.getAttributeValue(null, "syncable");
            String accountName = parser.getAttributeValue(null, "account");
            String accountType = parser.getAttributeValue(null, SoundModelContract.KEY_TYPE);
            String user = parser.getAttributeValue(null, XML_ATTR_USER);
            String packageName = parser.getAttributeValue(null, HwBroadcastRadarUtil.KEY_PACKAGE);
            String className = parser.getAttributeValue(null, AudioService.CONNECT_INTENT_KEY_DEVICE_CLASS);
            int userId = user == null ? 0 : Integer.parseInt(user);
            if (accountType == null && packageName == null) {
                accountType = "com.google";
                syncable = String.valueOf(-1);
            }
            authority = (AuthorityInfo) this.mAuthorities.get(id);
            if (Log.isLoggable(TAG_FILE, 2)) {
                Slog.v(TAG_FILE, "Adding authority: account=" + accountName + " accountType=" + accountType + " auth=" + authorityName + " package=" + packageName + " class=" + className + " user=" + userId + " enabled=" + enabled + " syncable=" + syncable);
            }
            if (authority == null) {
                if (Log.isLoggable(TAG_FILE, 2)) {
                    Slog.v(TAG_FILE, "Creating authority entry");
                }
                if (!(accountName == null || authorityName == null)) {
                    EndPoint info = new EndPoint(new Account(accountName, accountType), authorityName, userId);
                    if (validator.isAccountValid(info.account, userId) && validator.isAuthorityValid(authorityName, userId)) {
                        authority = getOrCreateAuthorityLocked(info, id, false);
                        if (version > 0) {
                            authority.periodicSyncs.clear();
                        }
                    } else {
                        EventLog.writeEvent(1397638484, new Object[]{"35028827", Integer.valueOf(-1), "account:" + info.account + " provider:" + authorityName + " user:" + userId});
                    }
                }
            }
            if (authority != null) {
                authority.enabled = enabled != null ? Boolean.parseBoolean(enabled) : true;
                try {
                    authority.syncable = syncable == null ? -1 : Integer.parseInt(syncable);
                } catch (NumberFormatException e3) {
                    if (Shell.NIGHT_MODE_STR_UNKNOWN.equals(syncable)) {
                        authority.syncable = -1;
                    } else {
                        authority.syncable = Boolean.parseBoolean(syncable) ? 1 : 0;
                    }
                }
            } else {
                Slog.w("SyncManager", "Failure adding authority: account=" + accountName + " auth=" + authorityName + " enabled=" + enabled + " syncable=" + syncable);
            }
        }
        return authority;
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
                flextime = calculateDefaultFlexTime(period);
                Slog.e("SyncManager", "Error formatting value parsed for periodic sync flex: " + flexValue + ", using default: " + flextime);
            } catch (NullPointerException e2) {
                flextime = calculateDefaultFlexTime(period);
                Slog.d("SyncManager", "No flex time specified for this sync, using a default. period: " + period + " flex: " + flextime);
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
        String name = parser.getAttributeValue(null, "name");
        String type = parser.getAttributeValue(null, SoundModelContract.KEY_TYPE);
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
        try {
            FileOutputStream fos = this.mAccountInfoFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, StandardCharsets.UTF_8.name());
            out.startDocument(null, Boolean.valueOf(true));
            out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            out.startTag(null, "accounts");
            out.attribute(null, "version", Integer.toString(3));
            out.attribute(null, XML_ATTR_NEXT_AUTHORITY_ID, Integer.toString(this.mNextAuthorityId));
            out.attribute(null, XML_ATTR_SYNC_RANDOM_OFFSET, Integer.toString(this.mSyncRandomOffset));
            int M = this.mMasterSyncAutomatically.size();
            for (int m = 0; m < M; m++) {
                int userId = this.mMasterSyncAutomatically.keyAt(m);
                Boolean listen = (Boolean) this.mMasterSyncAutomatically.valueAt(m);
                out.startTag(null, XML_TAG_LISTEN_FOR_TICKLES);
                out.attribute(null, XML_ATTR_USER, Integer.toString(userId));
                out.attribute(null, XML_ATTR_ENABLED, Boolean.toString(listen.booleanValue()));
                out.endTag(null, XML_TAG_LISTEN_FOR_TICKLES);
            }
            int N = this.mAuthorities.size();
            for (int i = 0; i < N; i++) {
                AuthorityInfo authority = (AuthorityInfo) this.mAuthorities.valueAt(i);
                EndPoint info = authority.target;
                out.startTag(null, "authority");
                out.attribute(null, "id", Integer.toString(authority.ident));
                out.attribute(null, XML_ATTR_USER, Integer.toString(info.userId));
                out.attribute(null, XML_ATTR_ENABLED, Boolean.toString(authority.enabled));
                out.attribute(null, "account", info.account.name);
                out.attribute(null, SoundModelContract.KEY_TYPE, info.account.type);
                out.attribute(null, "authority", info.provider);
                out.attribute(null, "syncable", Integer.toString(authority.syncable));
                out.endTag(null, "authority");
            }
            out.endTag(null, "accounts");
            out.endDocument();
            this.mAccountInfoFile.finishWrite(fos);
        } catch (IOException e1) {
            Slog.w("SyncManager", "Error writing accounts", e1);
            if (null != null) {
                this.mAccountInfoFile.failWrite(null);
            }
        }
    }

    static int getIntColumn(Cursor c, String name) {
        return c.getInt(c.getColumnIndex(name));
    }

    static long getLongColumn(Cursor c, String name) {
        return c.getLong(c.getColumnIndex(name));
    }

    private void readAndDeleteLegacyAccountInfoLocked() {
        File file = this.mContext.getDatabasePath("syncmanager.db");
        if (file.exists()) {
            String path = file.getPath();
            SQLiteDatabase db = null;
            try {
                db = SQLiteDatabase.openDatabase(path, null, 1);
            } catch (SQLiteException e) {
            }
            if (db != null) {
                AuthorityInfo authority;
                int i;
                boolean hasType = db.getVersion() >= 11;
                if (Log.isLoggable(TAG_FILE, 2)) {
                    Slog.v(TAG_FILE, "Reading legacy sync accounts db");
                }
                SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                qb.setTables("stats, status");
                HashMap<String, String> map = new HashMap();
                map.put("_id", "status._id as _id");
                map.put("account", "stats.account as account");
                if (hasType) {
                    map.put("account_type", "stats.account_type as account_type");
                }
                map.put("authority", "stats.authority as authority");
                map.put("totalElapsedTime", "totalElapsedTime");
                map.put("numSyncs", "numSyncs");
                map.put("numSourceLocal", "numSourceLocal");
                map.put("numSourcePoll", "numSourcePoll");
                map.put("numSourceServer", "numSourceServer");
                map.put("numSourceUser", "numSourceUser");
                map.put("lastSuccessSource", "lastSuccessSource");
                map.put("lastSuccessTime", "lastSuccessTime");
                map.put("lastFailureSource", "lastFailureSource");
                map.put("lastFailureTime", "lastFailureTime");
                map.put("lastFailureMesg", "lastFailureMesg");
                map.put("pending", "pending");
                qb.setProjectionMap(map);
                qb.appendWhere("stats._id = status.stats_id");
                Cursor c = qb.query(db, null, null, null, null, null, null);
                while (c.moveToNext()) {
                    String accountName = c.getString(c.getColumnIndex("account"));
                    String accountType = hasType ? c.getString(c.getColumnIndex("account_type")) : null;
                    if (accountType == null) {
                        accountType = "com.google";
                    }
                    authority = getOrCreateAuthorityLocked(new EndPoint(new Account(accountName, accountType), c.getString(c.getColumnIndex("authority")), 0), -1, false);
                    if (authority != null) {
                        boolean z;
                        i = this.mSyncStatus.size();
                        boolean found = false;
                        SyncStatusInfo st = null;
                        while (i > 0) {
                            i--;
                            st = (SyncStatusInfo) this.mSyncStatus.valueAt(i);
                            if (st.authorityId == authority.ident) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            SyncStatusInfo syncStatusInfo = new SyncStatusInfo(authority.ident);
                            this.mSyncStatus.put(authority.ident, syncStatusInfo);
                        }
                        st.totalElapsedTime = getLongColumn(c, "totalElapsedTime");
                        st.numSyncs = getIntColumn(c, "numSyncs");
                        st.numSourceLocal = getIntColumn(c, "numSourceLocal");
                        st.numSourcePoll = getIntColumn(c, "numSourcePoll");
                        st.numSourceServer = getIntColumn(c, "numSourceServer");
                        st.numSourceUser = getIntColumn(c, "numSourceUser");
                        st.numSourcePeriodic = 0;
                        st.lastSuccessSource = getIntColumn(c, "lastSuccessSource");
                        st.lastSuccessTime = getLongColumn(c, "lastSuccessTime");
                        st.lastFailureSource = getIntColumn(c, "lastFailureSource");
                        st.lastFailureTime = getLongColumn(c, "lastFailureTime");
                        st.lastFailureMesg = c.getString(c.getColumnIndex("lastFailureMesg"));
                        if (getIntColumn(c, "pending") != 0) {
                            z = true;
                        } else {
                            z = false;
                        }
                        st.pending = z;
                    }
                }
                c.close();
                qb = new SQLiteQueryBuilder();
                qb.setTables("settings");
                c = qb.query(db, null, null, null, null, null, null);
                while (c.moveToNext()) {
                    String name = c.getString(c.getColumnIndex("name"));
                    String value = c.getString(c.getColumnIndex("value"));
                    if (name != null) {
                        if (name.equals("listen_for_tickles")) {
                            setMasterSyncAutomatically(value != null ? Boolean.parseBoolean(value) : true, 0);
                        } else {
                            if (name.startsWith("sync_provider_")) {
                                String provider = name.substring("sync_provider_".length(), name.length());
                                i = this.mAuthorities.size();
                                while (i > 0) {
                                    i--;
                                    authority = (AuthorityInfo) this.mAuthorities.valueAt(i);
                                    if (authority.target.provider.equals(provider)) {
                                        authority.enabled = value != null ? Boolean.parseBoolean(value) : true;
                                        authority.syncable = 1;
                                    }
                                }
                            }
                        }
                    }
                }
                c.close();
                db.close();
                new File(path).delete();
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

    private void writeStatusLocked() {
        if (Log.isLoggable(TAG_FILE, 2)) {
            Slog.v(TAG_FILE, "Writing new " + this.mStatusFile.getBaseFile());
        }
        removeMessages(1);
        try {
            FileOutputStream fos = this.mStatusFile.startWrite();
            Parcel out = Parcel.obtain();
            int N = this.mSyncStatus.size();
            for (int i = 0; i < N; i++) {
                SyncStatusInfo status = (SyncStatusInfo) this.mSyncStatus.valueAt(i);
                out.writeInt(100);
                status.writeToParcel(out, 0);
            }
            out.writeInt(0);
            fos.write(out.marshall());
            out.recycle();
            this.mStatusFile.finishWrite(fos);
        } catch (IOException e1) {
            Slog.w("SyncManager", "Error writing status", e1);
            if (null != null) {
                this.mStatusFile.failWrite(null);
            }
        }
    }

    private void requestSync(AuthorityInfo authorityInfo, int reason, Bundle extras) {
        if (Process.myUid() != 1000 || this.mSyncRequestListener == null) {
            Builder req = new Builder().syncOnce().setExtras(extras);
            req.setSyncAdapter(authorityInfo.target.account, authorityInfo.target.provider);
            ContentResolver.requestSync(req.build());
            return;
        }
        this.mSyncRequestListener.onSyncRequest(authorityInfo.target, reason, extras);
    }

    private void requestSync(Account account, int userId, int reason, String authority, Bundle extras) {
        if (Process.myUid() != 1000 || this.mSyncRequestListener == null) {
            ContentResolver.requestSync(account, authority, extras);
        } else {
            this.mSyncRequestListener.onSyncRequest(new EndPoint(account, authority, userId), reason, extras);
        }
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
                if (token == 0) {
                    return;
                }
                if (token == 101 || token == 100) {
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
                    Slog.w("SyncManager", "Unknown stats token: " + token);
                    return;
                }
            }
        } catch (IOException e) {
            Slog.i("SyncManager", "No initial statistics");
        }
    }

    private void writeStatisticsLocked() {
        if (Log.isLoggable(TAG_FILE, 2)) {
            Slog.v("SyncManager", "Writing new " + this.mStatisticsFile.getBaseFile());
        }
        removeMessages(2);
        try {
            FileOutputStream fos = this.mStatisticsFile.startWrite();
            Parcel out = Parcel.obtain();
            for (DayStats ds : this.mDayStats) {
                if (ds == null) {
                    break;
                }
                out.writeInt(101);
                out.writeInt(ds.day);
                out.writeInt(ds.successCount);
                out.writeLong(ds.successTime);
                out.writeInt(ds.failureCount);
                out.writeLong(ds.failureTime);
            }
            out.writeInt(0);
            fos.write(out.marshall());
            out.recycle();
            this.mStatisticsFile.finishWrite(fos);
        } catch (IOException e1) {
            Slog.w("SyncManager", "Error writing stats", e1);
            if (null != null) {
                this.mStatisticsFile.failWrite(null);
            }
        }
    }

    public void queueBackup() {
        BackupManager.dataChanged("android");
    }
}

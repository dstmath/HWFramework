package com.android.server.content;

import android.accounts.Account;
import android.accounts.AccountAndUser;
import android.app.backup.BackupManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ISyncStatusObserver;
import android.content.PeriodicSync;
import android.content.SyncInfo;
import android.content.SyncRequest.Builder;
import android.content.SyncStatusInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.EventLog;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.am.ProcessList;
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
    private static final int ACCOUNTS_VERSION = 2;
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
    public static final String[] SOURCES = null;
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
    private static HashMap<String, String> sAuthorityRenames;
    private static volatile SyncStorageEngine sSyncStorageEngine;
    private final AtomicFile mAccountInfoFile;
    private final HashMap<AccountAndUser, AccountInfo> mAccounts;
    private final SparseArray<AuthorityInfo> mAuthorities;
    private OnAuthorityRemovedListener mAuthorityRemovedListener;
    private final Calendar mCal;
    private final RemoteCallbackList<ISyncStatusObserver> mChangeListeners;
    private final Context mContext;
    private final SparseArray<ArrayList<SyncInfo>> mCurrentSyncs;
    private final DayStats[] mDayStats;
    private boolean mDefaultMasterSyncAutomatically;
    private SparseArray<Boolean> mMasterSyncAutomatically;
    private int mNextAuthorityId;
    private int mNextHistoryId;
    private final ArrayMap<ComponentName, SparseArray<AuthorityInfo>> mServices;
    private final AtomicFile mStatisticsFile;
    private final AtomicFile mStatusFile;
    private final ArrayList<SyncHistoryItem> mSyncHistory;
    private int mSyncRandomOffset;
    private OnSyncRequestListener mSyncRequestListener;
    private final SparseArray<SyncStatusInfo> mSyncStatus;
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

    static class AccountInfo {
        final AccountAndUser accountAndUser;
        final HashMap<String, AuthorityInfo> authorities;

        AccountInfo(AccountAndUser accountAndUser) {
            this.authorities = new HashMap();
            this.accountAndUser = accountAndUser;
        }
    }

    public static class AuthorityInfo {
        public static final int NOT_INITIALIZED = -1;
        public static final int NOT_SYNCABLE = 0;
        public static final int SYNCABLE = 1;
        public static final int SYNCABLE_NOT_INITIALIZED = 2;
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
            this.enabled = SyncStorageEngine.SYNC_ENABLED_DEFAULT;
            this.periodicSyncs = new ArrayList();
            defaultInitialisation();
        }

        private void defaultInitialisation() {
            this.syncable = NOT_INITIALIZED;
            this.backoffTime = SyncStorageEngine.NOT_IN_BACKOFF_MODE;
            this.backoffDelay = SyncStorageEngine.NOT_IN_BACKOFF_MODE;
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
        public static final EndPoint USER_ALL_PROVIDER_ALL_ACCOUNTS_ALL = null;
        final Account account;
        final String provider;
        final int userId;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.content.SyncStorageEngine.EndPoint.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.content.SyncStorageEngine.EndPoint.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.content.SyncStorageEngine.EndPoint.<clinit>():void");
        }

        public EndPoint(Account account, String provider, int userId) {
            this.account = account;
            this.provider = provider;
            this.userId = userId;
        }

        public boolean matchesSpec(EndPoint spec) {
            if (this.userId != spec.userId && this.userId != -1 && spec.userId != -1) {
                return SyncStorageEngine.SYNC_ENABLED_DEFAULT;
            }
            boolean z;
            boolean z2;
            if (spec.account == null) {
                z = true;
            } else {
                z = this.account.equals(spec.account);
            }
            if (spec.provider == null) {
                z2 = true;
            } else {
                z2 = this.provider.equals(spec.provider);
            }
            if (!z) {
                z2 = SyncStorageEngine.SYNC_ENABLED_DEFAULT;
            }
            return z2;
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

        public SyncHistoryItem() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.content.SyncStorageEngine.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.content.SyncStorageEngine.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.content.SyncStorageEngine.<clinit>():void");
    }

    private SyncStorageEngine(Context context, File dataDir) {
        this.mAuthorities = new SparseArray();
        this.mAccounts = new HashMap();
        this.mCurrentSyncs = new SparseArray();
        this.mSyncStatus = new SparseArray();
        this.mSyncHistory = new ArrayList();
        this.mChangeListeners = new RemoteCallbackList();
        this.mServices = new ArrayMap();
        this.mNextAuthorityId = STATUS_FILE_END;
        this.mDayStats = new DayStats[28];
        this.mNextHistoryId = STATUS_FILE_END;
        this.mMasterSyncAutomatically = new SparseArray();
        this.mContext = context;
        sSyncStorageEngine = this;
        this.mCal = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"));
        this.mDefaultMasterSyncAutomatically = this.mContext.getResources().getBoolean(17956982);
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
        if (msg.what == SOURCE_LOCAL) {
            sparseArray = this.mAuthorities;
            synchronized (sparseArray) {
            }
            writeStatusLocked();
        } else if (msg.what == SOURCE_POLL) {
            sparseArray = this.mAuthorities;
            synchronized (sparseArray) {
            }
            writeStatisticsLocked();
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

    /* JADX WARNING: inconsistent code. */
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
                    }
                }
                this.mChangeListeners.finishBroadcast();
                if (Log.isLoggable(TAG, SOURCE_POLL)) {
                    Slog.v(TAG, "reportChange " + which + " to: " + reports);
                }
                if (reports != null) {
                    i = reports.size();
                    while (i > 0) {
                        i--;
                        try {
                            ((ISyncStatusObserver) reports.get(i)).onStatusChanged(which);
                        } catch (RemoteException e) {
                        }
                    }
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    public boolean getSyncAutomatically(Account account, int userId, String providerName) {
        boolean z = SYNC_ENABLED_DEFAULT;
        synchronized (this.mAuthorities) {
            if (account != null) {
                AuthorityInfo authority = getAuthorityLocked(new EndPoint(account, providerName, userId), "getSyncAutomatically");
                if (authority != null) {
                    z = authority.enabled;
                }
                return z;
            }
            int i = this.mAuthorities.size();
            while (i > 0) {
                i--;
                AuthorityInfo authorityInfo = (AuthorityInfo) this.mAuthorities.valueAt(i);
                if (authorityInfo.target.matchesSpec(new EndPoint(account, providerName, userId)) && authorityInfo.enabled) {
                    return true;
                }
            }
            return SYNC_ENABLED_DEFAULT;
        }
    }

    public void setSyncAutomatically(Account account, int userId, String providerName, boolean sync) {
        if (Log.isLoggable(TAG, SOURCE_POLL)) {
            Slog.d(TAG, "setSyncAutomatically:  provider " + providerName + ", user " + userId + " -> " + sync);
        }
        synchronized (this.mAuthorities) {
            AuthorityInfo authority = getOrCreateAuthorityLocked(new EndPoint(account, providerName, userId), -1, SYNC_ENABLED_DEFAULT);
            if (authority.enabled == sync) {
                if (Log.isLoggable(TAG, SOURCE_POLL)) {
                    Slog.d(TAG, "setSyncAutomatically: already set to " + sync + ", doing nothing");
                }
                return;
            }
            if (sync) {
                if (authority.syncable == SOURCE_POLL) {
                    authority.syncable = -1;
                }
            }
            authority.enabled = sync;
            writeAccountInfoLocked();
            if (sync) {
                requestSync(account, userId, -6, providerName, new Bundle());
            }
            reportChange(SOURCE_LOCAL);
            queueBackup();
        }
    }

    public int getIsSyncable(Account account, int userId, String providerName) {
        synchronized (this.mAuthorities) {
            if (account != null) {
                AuthorityInfo authority = getAuthorityLocked(new EndPoint(account, providerName, userId), "get authority syncable");
                if (authority == null) {
                    return -1;
                }
                int i = authority.syncable;
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

    private void setSyncableStateForEndPoint(EndPoint target, int syncable) {
        synchronized (this.mAuthorities) {
            AuthorityInfo aInfo = getOrCreateAuthorityLocked(target, -1, SYNC_ENABLED_DEFAULT);
            if (syncable < -1) {
                syncable = -1;
            }
            if (Log.isLoggable(TAG, SOURCE_POLL)) {
                Slog.d(TAG, "setIsSyncable: " + aInfo.toString() + " -> " + syncable);
            }
            if (aInfo.syncable == syncable) {
                if (Log.isLoggable(TAG, SOURCE_POLL)) {
                    Slog.d(TAG, "setIsSyncable: already set to " + syncable + ", doing nothing");
                }
                return;
            }
            aInfo.syncable = syncable;
            writeAccountInfoLocked();
            if (syncable == SOURCE_LOCAL) {
                requestSync(aInfo, -5, new Bundle());
            }
            reportChange(SOURCE_LOCAL);
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
        boolean backoffLocked;
        if (Log.isLoggable(TAG, SOURCE_POLL)) {
            Slog.v(TAG, "setBackoff: " + info + " -> nextSyncTime " + nextSyncTime + ", nextDelay " + nextDelay);
        }
        synchronized (this.mAuthorities) {
            if (info.account == null || info.provider == null) {
                backoffLocked = setBackoffLocked(info.account, info.userId, info.provider, nextSyncTime, nextDelay);
            } else {
                AuthorityInfo authorityInfo = getOrCreateAuthorityLocked(info, -1, true);
                if (authorityInfo.backoffTime == nextSyncTime && authorityInfo.backoffDelay == nextDelay) {
                    backoffLocked = SYNC_ENABLED_DEFAULT;
                } else {
                    authorityInfo.backoffTime = nextSyncTime;
                    authorityInfo.backoffDelay = nextDelay;
                    backoffLocked = true;
                }
            }
        }
        if (backoffLocked) {
            reportChange(SOURCE_LOCAL);
        }
    }

    private boolean setBackoffLocked(Account account, int userId, String providerName, long nextSyncTime, long nextDelay) {
        boolean changed = SYNC_ENABLED_DEFAULT;
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
        boolean changed = SYNC_ENABLED_DEFAULT;
        synchronized (this.mAuthorities) {
            for (AccountInfo accountInfo : this.mAccounts.values()) {
                for (AuthorityInfo authorityInfo : accountInfo.authorities.values()) {
                    if (authorityInfo.backoffTime != NOT_IN_BACKOFF_MODE || authorityInfo.backoffDelay != NOT_IN_BACKOFF_MODE) {
                        if (Log.isLoggable(TAG, SOURCE_POLL)) {
                            Slog.v(TAG, "clearAllBackoffsLocked: authority:" + authorityInfo.target + " account:" + accountInfo.accountAndUser.account.name + " user:" + accountInfo.accountAndUser.userId + " backoffTime was: " + authorityInfo.backoffTime + " backoffDelay was: " + authorityInfo.backoffDelay);
                        }
                        authorityInfo.backoffTime = NOT_IN_BACKOFF_MODE;
                        authorityInfo.backoffDelay = NOT_IN_BACKOFF_MODE;
                        changed = true;
                    }
                }
            }
        }
        if (changed) {
            reportChange(SOURCE_LOCAL);
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
        if (Log.isLoggable(TAG, SOURCE_POLL)) {
            Slog.v(TAG, "setDelayUntil: " + info + " -> delayUntil " + delayUntil);
        }
        synchronized (this.mAuthorities) {
            AuthorityInfo authority = getOrCreateAuthorityLocked(info, -1, true);
            if (authority.delayUntil == delayUntil) {
                return;
            }
            authority.delayUntil = delayUntil;
            reportChange(SOURCE_LOCAL);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean restoreAllPeriodicSyncs() {
        if (mPeriodicSyncAddedListener == null) {
            return SYNC_ENABLED_DEFAULT;
        }
        synchronized (this.mAuthorities) {
            int i = STATUS_FILE_END;
            while (true) {
                if (i < this.mAuthorities.size()) {
                    AuthorityInfo authority = (AuthorityInfo) this.mAuthorities.valueAt(i);
                    for (PeriodicSync periodicSync : authority.periodicSyncs) {
                        mPeriodicSyncAddedListener.onPeriodicSyncAdded(authority.target, periodicSync.extras, periodicSync.period, periodicSync.flexTime);
                    }
                    authority.periodicSyncs.clear();
                    i += SOURCE_LOCAL;
                } else {
                    writeAccountInfoLocked();
                }
            }
        }
        return true;
    }

    public void setMasterSyncAutomatically(boolean flag, int userId) {
        synchronized (this.mAuthorities) {
            Boolean auto = (Boolean) this.mMasterSyncAutomatically.get(userId);
            if (auto == null || !auto.equals(Boolean.valueOf(flag))) {
                this.mMasterSyncAutomatically.put(userId, Boolean.valueOf(flag));
                writeAccountInfoLocked();
                if (flag) {
                    requestSync(null, userId, -7, null, new Bundle());
                }
                reportChange(SOURCE_LOCAL);
                this.mContext.sendBroadcast(ContentResolver.ACTION_SYNC_CONN_STATUS_CHANGED);
                queueBackup();
                return;
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
            return SYNC_ENABLED_DEFAULT;
        }
    }

    public void markPending(EndPoint info, boolean pendingValue) {
        synchronized (this.mAuthorities) {
            AuthorityInfo authority = getOrCreateAuthorityLocked(info, -1, true);
            if (authority == null) {
                return;
            }
            getOrCreateSyncStatusLocked(authority.ident).pending = pendingValue;
            reportChange(SOURCE_POLL);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void doDatabaseCleanup(Account[] accounts, int userId) {
        synchronized (this.mAuthorities) {
            if (Log.isLoggable(TAG, SOURCE_POLL)) {
                Slog.v(TAG, "Updating for new accounts...");
            }
            SparseArray<AuthorityInfo> removing = new SparseArray();
            Iterator<AccountInfo> accIt = this.mAccounts.values().iterator();
            while (accIt.hasNext()) {
                AccountInfo acc = (AccountInfo) accIt.next();
                if (!ArrayUtils.contains(accounts, acc.accountAndUser.account) && acc.accountAndUser.userId == userId) {
                    if (Log.isLoggable(TAG, SOURCE_POLL)) {
                        Slog.v(TAG, "Account removed: " + acc.accountAndUser);
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
            if (Log.isLoggable(TAG, SOURCE_POLL)) {
                Slog.v(TAG, "setActiveSync: account= auth=" + activeSyncContext.mSyncOperation.target + " src=" + activeSyncContext.mSyncOperation.syncSource + " extras=" + activeSyncContext.mSyncOperation.extras);
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
            if (Log.isLoggable(TAG, SOURCE_POLL)) {
                Slog.v(TAG, "removeActiveSync: account=" + syncInfo.account + " user=" + userId + " auth=" + syncInfo.authority);
            }
            getCurrentSyncs(userId).remove(syncInfo);
        }
        reportActiveChange();
    }

    public void reportActiveChange() {
        reportChange(SOURCE_PERIODIC);
    }

    public long insertStartSyncEvent(SyncOperation op, long now) {
        synchronized (this.mAuthorities) {
            if (Log.isLoggable(TAG, SOURCE_POLL)) {
                Slog.v(TAG, "insertStartSyncEvent: " + op);
            }
            AuthorityInfo authority = getAuthorityLocked(op.target, "insertStartSyncEvent");
            if (authority == null) {
                return NOT_IN_BACKOFF_MODE;
            }
            SyncHistoryItem item = new SyncHistoryItem();
            item.initialization = op.isInitialization();
            item.authorityId = authority.ident;
            int i = this.mNextHistoryId;
            this.mNextHistoryId = i + SOURCE_LOCAL;
            item.historyId = i;
            if (this.mNextHistoryId < 0) {
                this.mNextHistoryId = STATUS_FILE_END;
            }
            item.eventTime = now;
            item.source = op.syncSource;
            item.reason = op.reason;
            item.extras = op.extras;
            item.event = STATUS_FILE_END;
            this.mSyncHistory.add(STATUS_FILE_END, item);
            while (this.mSyncHistory.size() > STATUS_FILE_ITEM) {
                this.mSyncHistory.remove(this.mSyncHistory.size() - 1);
            }
            long id = (long) item.historyId;
            if (Log.isLoggable(TAG, SOURCE_POLL)) {
                Slog.v(TAG, "returning historyId " + id);
            }
            reportChange(8);
            return id;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void stopSyncEvent(long historyId, long elapsedTime, String resultMessage, long downstreamActivity, long upstreamActivity) {
        synchronized (this.mAuthorities) {
            if (Log.isLoggable(TAG, SOURCE_POLL)) {
                Slog.v(TAG, "stopSyncEvent: historyId=" + historyId);
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
            if (item != null) {
                item.elapsedTime = elapsedTime;
                item.event = SOURCE_LOCAL;
                item.mesg = resultMessage;
                item.downstreamActivity = downstreamActivity;
                item.upstreamActivity = upstreamActivity;
                SyncStatusInfo status = getOrCreateSyncStatusLocked(item.authorityId);
                status.numSyncs += SOURCE_LOCAL;
                status.totalElapsedTime += elapsedTime;
                switch (item.source) {
                    case STATUS_FILE_END /*0*/:
                        status.numSourceServer += SOURCE_LOCAL;
                        break;
                    case SOURCE_LOCAL /*1*/:
                        status.numSourceLocal += SOURCE_LOCAL;
                        break;
                    case SOURCE_POLL /*2*/:
                        status.numSourcePoll += SOURCE_LOCAL;
                        break;
                    case SOURCE_USER /*3*/:
                        status.numSourceUser += SOURCE_LOCAL;
                        break;
                    case SOURCE_PERIODIC /*4*/:
                        status.numSourcePeriodic += SOURCE_LOCAL;
                        break;
                }
            }
            Slog.w(TAG, "stopSyncEvent: no history for id " + historyId);
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
            int i = STATUS_FILE_END;
            while (i < N) {
                SyncStatusInfo cur = (SyncStatusInfo) this.mSyncStatus.valueAt(i);
                AuthorityInfo ainfo = (AuthorityInfo) this.mAuthorities.get(cur.authorityId);
                if (ainfo == null || !ainfo.target.matchesSpec(info)) {
                    i += SOURCE_LOCAL;
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
            for (int i = STATUS_FILE_END; i < N; i += SOURCE_LOCAL) {
                SyncStatusInfo cur = (SyncStatusInfo) this.mSyncStatus.valueAt(i);
                AuthorityInfo ainfo = (AuthorityInfo) this.mAuthorities.get(cur.authorityId);
                if (ainfo != null && ainfo.target.matchesSpec(info) && cur.pending) {
                    return true;
                }
            }
            return SYNC_ENABLED_DEFAULT;
        }
    }

    public ArrayList<SyncHistoryItem> getSyncHistory() {
        ArrayList<SyncHistoryItem> items;
        synchronized (this.mAuthorities) {
            int N = this.mSyncHistory.size();
            items = new ArrayList(N);
            for (int i = STATUS_FILE_END; i < N; i += SOURCE_LOCAL) {
                items.add((SyncHistoryItem) this.mSyncHistory.get(i));
            }
        }
        return items;
    }

    public DayStats[] getDayStatistics() {
        DayStats[] ds;
        synchronized (this.mAuthorities) {
            ds = new DayStats[this.mDayStats.length];
            System.arraycopy(this.mDayStats, STATUS_FILE_END, ds, STATUS_FILE_END, ds.length);
        }
        return ds;
    }

    private Pair<AuthorityInfo, SyncStatusInfo> createCopyPairOfAuthorityWithSyncStatusLocked(AuthorityInfo authorityInfo) {
        return Pair.create(new AuthorityInfo(authorityInfo), new SyncStatusInfo(getOrCreateSyncStatusLocked(authorityInfo.ident)));
    }

    private int getCurrentDayLocked() {
        this.mCal.setTimeInMillis(System.currentTimeMillis());
        int dayOfYear = this.mCal.get(6);
        if (this.mYear != this.mCal.get(SOURCE_LOCAL)) {
            this.mYear = this.mCal.get(SOURCE_LOCAL);
            this.mCal.clear();
            this.mCal.set(SOURCE_LOCAL, this.mYear);
            this.mYearInDays = (int) (this.mCal.getTimeInMillis() / UnixCalendar.DAY_IN_MILLIS);
        }
        return this.mYearInDays + dayOfYear;
    }

    private AuthorityInfo getAuthorityLocked(EndPoint info, String tag) {
        AccountAndUser au = new AccountAndUser(info.account, info.userId);
        AccountInfo accountInfo = (AccountInfo) this.mAccounts.get(au);
        if (accountInfo == null) {
            if (tag != null && Log.isLoggable(TAG, SOURCE_POLL)) {
                Slog.v(TAG, tag + ": unknown account " + au);
            }
            return null;
        }
        AuthorityInfo authority = (AuthorityInfo) accountInfo.authorities.get(info.provider);
        if (authority != null) {
            return authority;
        }
        if (tag != null && Log.isLoggable(TAG, SOURCE_POLL)) {
            Slog.v(TAG, tag + ": unknown provider " + info.provider);
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
            this.mNextAuthorityId += SOURCE_LOCAL;
            doWrite = true;
        }
        if (Log.isLoggable(TAG, SOURCE_POLL)) {
            Slog.v(TAG, "created a new AuthorityInfo for " + info);
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
            if (Log.isLoggable(TAG_FILE, SOURCE_POLL)) {
                Slog.v(TAG_FILE, "Reading " + this.mAccountInfoFile.getBaseFile());
            }
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fis, StandardCharsets.UTF_8.name());
            int eventType = parser.getEventType();
            while (eventType != SOURCE_POLL && eventType != SOURCE_LOCAL) {
                eventType = parser.next();
            }
            if (eventType == SOURCE_LOCAL) {
                Slog.i(TAG, "No initial accounts");
                this.mNextAuthorityId = Math.max(STATUS_FILE_END, this.mNextAuthorityId);
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
                    version = STATUS_FILE_END;
                } else {
                    try {
                        version = Integer.parseInt(versionString);
                    } catch (NumberFormatException e2) {
                        version = STATUS_FILE_END;
                    }
                }
                String nextIdString = parser.getAttributeValue(null, XML_ATTR_NEXT_AUTHORITY_ID);
                try {
                    this.mNextAuthorityId = Math.max(this.mNextAuthorityId, nextIdString == null ? STATUS_FILE_END : Integer.parseInt(nextIdString));
                } catch (NumberFormatException e3) {
                }
                String offsetString = parser.getAttributeValue(null, XML_ATTR_SYNC_RANDOM_OFFSET);
                try {
                    this.mSyncRandomOffset = offsetString == null ? STATUS_FILE_END : Integer.parseInt(offsetString);
                } catch (NumberFormatException e4) {
                    this.mSyncRandomOffset = STATUS_FILE_END;
                }
                if (this.mSyncRandomOffset == 0) {
                    this.mSyncRandomOffset = new Random(System.currentTimeMillis()).nextInt(86400);
                }
                this.mMasterSyncAutomatically.put(STATUS_FILE_END, Boolean.valueOf(listen != null ? Boolean.parseBoolean(listen) : true));
                eventType = parser.next();
                AuthorityInfo authority = null;
                PeriodicSync periodicSync = null;
                do {
                    if (eventType == SOURCE_POLL) {
                        String tagName = parser.getName();
                        if (parser.getDepth() == SOURCE_POLL) {
                            if ("authority".equals(tagName)) {
                                authority = parseAuthority(parser, version);
                                periodicSync = null;
                                if (authority != null) {
                                    int i = authority.ident;
                                    if (r0 > highestAuthorityId) {
                                        highestAuthorityId = authority.ident;
                                    }
                                } else {
                                    String[] strArr = new Object[SOURCE_USER];
                                    strArr[STATUS_FILE_END] = "26513719";
                                    strArr[SOURCE_LOCAL] = Integer.valueOf(-1);
                                    strArr[SOURCE_POLL] = "Malformed authority";
                                    EventLog.writeEvent(1397638484, strArr);
                                }
                            } else {
                                if (XML_TAG_LISTEN_FOR_TICKLES.equals(tagName)) {
                                    parseListenForTickles(parser);
                                }
                            }
                        } else if (parser.getDepth() == SOURCE_USER) {
                            if ("periodicSync".equals(tagName) && authority != null) {
                                periodicSync = parsePeriodicSync(parser, authority);
                            }
                        } else if (parser.getDepth() == SOURCE_PERIODIC && periodicSync != null) {
                            if ("extra".equals(tagName)) {
                                parseExtra(parser, periodicSync.extras);
                            }
                        }
                    }
                    eventType = parser.next();
                } while (eventType != SOURCE_LOCAL);
            }
            this.mNextAuthorityId = Math.max(highestAuthorityId + SOURCE_LOCAL, this.mNextAuthorityId);
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e5) {
                }
            }
            maybeMigrateSettingsForRenamedAuthorities();
        } catch (XmlPullParserException e6) {
            Slog.w(TAG, "Error reading accounts", e6);
            this.mNextAuthorityId = Math.max(highestAuthorityId + SOURCE_LOCAL, this.mNextAuthorityId);
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e7) {
                }
            }
        } catch (IOException e8) {
            if (fis == null) {
                Slog.i(TAG, "No initial accounts");
            } else {
                Slog.w(TAG, "Error reading accounts", e8);
            }
            this.mNextAuthorityId = Math.max(highestAuthorityId + SOURCE_LOCAL, this.mNextAuthorityId);
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e9) {
                }
            }
        } catch (Throwable th) {
            this.mNextAuthorityId = Math.max(highestAuthorityId + SOURCE_LOCAL, this.mNextAuthorityId);
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
        boolean writeNeeded = SYNC_ENABLED_DEFAULT;
        ArrayList<AuthorityInfo> authoritiesToRemove = new ArrayList();
        int N = this.mAuthorities.size();
        for (int i = STATUS_FILE_END; i < N; i += SOURCE_LOCAL) {
            AuthorityInfo authority = (AuthorityInfo) this.mAuthorities.valueAt(i);
            String newAuthorityName = (String) sAuthorityRenames.get(authority.target.provider);
            if (newAuthorityName != null) {
                authoritiesToRemove.add(authority);
                if (authority.enabled) {
                    EndPoint newInfo = new EndPoint(authority.target.account, newAuthorityName, authority.target.userId);
                    if (getAuthorityLocked(newInfo, "cleanup") == null) {
                        getOrCreateAuthorityLocked(newInfo, -1, SYNC_ENABLED_DEFAULT).enabled = true;
                        writeNeeded = true;
                    }
                }
            }
        }
        for (AuthorityInfo authorityInfo : authoritiesToRemove) {
            removeAuthorityLocked(authorityInfo.target.account, authorityInfo.target.userId, authorityInfo.target.provider, SYNC_ENABLED_DEFAULT);
            writeNeeded = true;
        }
        return writeNeeded;
    }

    private void parseListenForTickles(XmlPullParser parser) {
        String user = parser.getAttributeValue(null, XML_ATTR_USER);
        int userId = STATUS_FILE_END;
        try {
            userId = Integer.parseInt(user);
        } catch (NumberFormatException e) {
            Slog.e(TAG, "error parsing the user for listen-for-tickles", e);
        } catch (NullPointerException e2) {
            Slog.e(TAG, "the user in listen-for-tickles is null", e2);
        }
        String enabled = parser.getAttributeValue(null, XML_ATTR_ENABLED);
        this.mMasterSyncAutomatically.put(userId, Boolean.valueOf(enabled != null ? Boolean.parseBoolean(enabled) : true));
    }

    private AuthorityInfo parseAuthority(XmlPullParser parser, int version) {
        AuthorityInfo authority = null;
        int id = -1;
        try {
            id = Integer.parseInt(parser.getAttributeValue(null, "id"));
        } catch (NumberFormatException e) {
            Slog.e(TAG, "error parsing the id of the authority", e);
        } catch (NullPointerException e2) {
            Slog.e(TAG, "the id of the authority is null", e2);
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
            int userId = user == null ? STATUS_FILE_END : Integer.parseInt(user);
            if (accountType == null && packageName == null) {
                accountType = "com.google";
                syncable = String.valueOf(-1);
            }
            authority = (AuthorityInfo) this.mAuthorities.get(id);
            if (Log.isLoggable(TAG_FILE, SOURCE_POLL)) {
                Slog.v(TAG_FILE, "Adding authority: account=" + accountName + " accountType=" + accountType + " auth=" + authorityName + " package=" + packageName + " class=" + className + " user=" + userId + " enabled=" + enabled + " syncable=" + syncable);
            }
            if (authority == null) {
                if (Log.isLoggable(TAG_FILE, SOURCE_POLL)) {
                    Slog.v(TAG_FILE, "Creating authority entry");
                }
                EndPoint endPoint = null;
                if (!(accountName == null || authorityName == null)) {
                    endPoint = new EndPoint(new Account(accountName, accountType), authorityName, userId);
                }
                if (endPoint != null) {
                    authority = getOrCreateAuthorityLocked(endPoint, id, SYNC_ENABLED_DEFAULT);
                    if (version > 0) {
                        authority.periodicSyncs.clear();
                    }
                }
            }
            if (authority != null) {
                authority.enabled = enabled != null ? Boolean.parseBoolean(enabled) : true;
                try {
                    authority.syncable = syncable == null ? -1 : Integer.parseInt(syncable);
                } catch (NumberFormatException e3) {
                    if ("unknown".equals(syncable)) {
                        authority.syncable = -1;
                    } else {
                        authority.syncable = Boolean.parseBoolean(syncable) ? SOURCE_LOCAL : STATUS_FILE_END;
                    }
                }
            } else {
                Slog.w(TAG, "Failure adding authority: account=" + accountName + " auth=" + authorityName + " enabled=" + enabled + " syncable=" + syncable);
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
                Slog.e(TAG, "Error formatting value parsed for periodic sync flex: " + flexValue + ", using default: " + flextime);
            } catch (NullPointerException e2) {
                flextime = calculateDefaultFlexTime(period);
                Slog.d(TAG, "No flex time specified for this sync, using a default. period: " + period + " flex: " + flextime);
            }
            PeriodicSync periodicSync = new PeriodicSync(authorityInfo.target.account, authorityInfo.target.provider, extras, period, flextime);
            authorityInfo.periodicSyncs.add(periodicSync);
            return periodicSync;
        } catch (NumberFormatException e3) {
            Slog.e(TAG, "error parsing the period of a periodic sync", e3);
            return null;
        } catch (NullPointerException e4) {
            Slog.e(TAG, "the period of a periodic sync is null", e4);
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
            Slog.e(TAG, "error parsing bundle value", e);
        } catch (NullPointerException e2) {
            Slog.e(TAG, "error parsing bundle value", e2);
        }
    }

    private void writeAccountInfoLocked() {
        if (Log.isLoggable(TAG_FILE, SOURCE_POLL)) {
            Slog.v(TAG_FILE, "Writing new " + this.mAccountInfoFile.getBaseFile());
        }
        try {
            FileOutputStream fos = this.mAccountInfoFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, StandardCharsets.UTF_8.name());
            out.startDocument(null, Boolean.valueOf(true));
            out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            out.startTag(null, "accounts");
            out.attribute(null, "version", Integer.toString(SOURCE_POLL));
            out.attribute(null, XML_ATTR_NEXT_AUTHORITY_ID, Integer.toString(this.mNextAuthorityId));
            out.attribute(null, XML_ATTR_SYNC_RANDOM_OFFSET, Integer.toString(this.mSyncRandomOffset));
            int M = this.mMasterSyncAutomatically.size();
            for (int m = STATUS_FILE_END; m < M; m += SOURCE_LOCAL) {
                int userId = this.mMasterSyncAutomatically.keyAt(m);
                Boolean listen = (Boolean) this.mMasterSyncAutomatically.valueAt(m);
                out.startTag(null, XML_TAG_LISTEN_FOR_TICKLES);
                out.attribute(null, XML_ATTR_USER, Integer.toString(userId));
                out.attribute(null, XML_ATTR_ENABLED, Boolean.toString(listen.booleanValue()));
                out.endTag(null, XML_TAG_LISTEN_FOR_TICKLES);
            }
            int N = this.mAuthorities.size();
            for (int i = STATUS_FILE_END; i < N; i += SOURCE_LOCAL) {
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
            Slog.w(TAG, "Error writing accounts", e1);
            if (STATUS_FILE_END != null) {
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
                db = SQLiteDatabase.openDatabase(path, null, SOURCE_LOCAL);
            } catch (SQLiteException e) {
            }
            if (db != null) {
                AuthorityInfo authority;
                int i;
                boolean hasType = db.getVersion() >= 11 ? true : SYNC_ENABLED_DEFAULT;
                if (Log.isLoggable(TAG_FILE, SOURCE_POLL)) {
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
                    authority = getOrCreateAuthorityLocked(new EndPoint(new Account(accountName, accountType), c.getString(c.getColumnIndex("authority")), STATUS_FILE_END), -1, SYNC_ENABLED_DEFAULT);
                    if (authority != null) {
                        boolean z;
                        i = this.mSyncStatus.size();
                        boolean found = SYNC_ENABLED_DEFAULT;
                        SyncStatusInfo syncStatusInfo = null;
                        while (i > 0) {
                            i--;
                            syncStatusInfo = (SyncStatusInfo) this.mSyncStatus.valueAt(i);
                            if (syncStatusInfo.authorityId == authority.ident) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            SyncStatusInfo syncStatusInfo2 = new SyncStatusInfo(authority.ident);
                            this.mSyncStatus.put(authority.ident, syncStatusInfo2);
                        }
                        syncStatusInfo.totalElapsedTime = getLongColumn(c, "totalElapsedTime");
                        syncStatusInfo.numSyncs = getIntColumn(c, "numSyncs");
                        syncStatusInfo.numSourceLocal = getIntColumn(c, "numSourceLocal");
                        syncStatusInfo.numSourcePoll = getIntColumn(c, "numSourcePoll");
                        syncStatusInfo.numSourceServer = getIntColumn(c, "numSourceServer");
                        syncStatusInfo.numSourceUser = getIntColumn(c, "numSourceUser");
                        syncStatusInfo.numSourcePeriodic = STATUS_FILE_END;
                        syncStatusInfo.lastSuccessSource = getIntColumn(c, "lastSuccessSource");
                        syncStatusInfo.lastSuccessTime = getLongColumn(c, "lastSuccessTime");
                        syncStatusInfo.lastFailureSource = getIntColumn(c, "lastFailureSource");
                        syncStatusInfo.lastFailureTime = getLongColumn(c, "lastFailureTime");
                        syncStatusInfo.lastFailureMesg = c.getString(c.getColumnIndex("lastFailureMesg"));
                        if (getIntColumn(c, "pending") != 0) {
                            z = true;
                        } else {
                            z = SYNC_ENABLED_DEFAULT;
                        }
                        syncStatusInfo.pending = z;
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
                            setMasterSyncAutomatically(value != null ? Boolean.parseBoolean(value) : true, STATUS_FILE_END);
                        } else {
                            if (name.startsWith("sync_provider_")) {
                                String provider = name.substring("sync_provider_".length(), name.length());
                                i = this.mAuthorities.size();
                                while (i > 0) {
                                    i--;
                                    authority = (AuthorityInfo) this.mAuthorities.valueAt(i);
                                    if (authority.target.provider.equals(provider)) {
                                        authority.enabled = value != null ? Boolean.parseBoolean(value) : true;
                                        authority.syncable = SOURCE_LOCAL;
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
        if (Log.isLoggable(TAG_FILE, SOURCE_POLL)) {
            Slog.v(TAG_FILE, "Reading " + this.mStatusFile.getBaseFile());
        }
        try {
            byte[] data = this.mStatusFile.readFully();
            Parcel in = Parcel.obtain();
            in.unmarshall(data, STATUS_FILE_END, data.length);
            in.setDataPosition(STATUS_FILE_END);
            while (true) {
                int token = in.readInt();
                if (token == 0) {
                    return;
                }
                if (token == STATUS_FILE_ITEM) {
                    SyncStatusInfo status = new SyncStatusInfo(in);
                    if (this.mAuthorities.indexOfKey(status.authorityId) >= 0) {
                        status.pending = SYNC_ENABLED_DEFAULT;
                        if (Log.isLoggable(TAG_FILE, SOURCE_POLL)) {
                            Slog.v(TAG_FILE, "Adding status for id " + status.authorityId);
                        }
                        this.mSyncStatus.put(status.authorityId, status);
                    }
                } else {
                    Slog.w(TAG, "Unknown status token: " + token);
                    return;
                }
            }
        } catch (IOException e) {
            Slog.i(TAG, "No initial status");
        }
    }

    private void writeStatusLocked() {
        if (Log.isLoggable(TAG_FILE, SOURCE_POLL)) {
            Slog.v(TAG_FILE, "Writing new " + this.mStatusFile.getBaseFile());
        }
        removeMessages(SOURCE_LOCAL);
        try {
            FileOutputStream fos = this.mStatusFile.startWrite();
            Parcel out = Parcel.obtain();
            int N = this.mSyncStatus.size();
            for (int i = STATUS_FILE_END; i < N; i += SOURCE_LOCAL) {
                SyncStatusInfo status = (SyncStatusInfo) this.mSyncStatus.valueAt(i);
                out.writeInt(STATUS_FILE_ITEM);
                status.writeToParcel(out, STATUS_FILE_END);
            }
            out.writeInt(STATUS_FILE_END);
            fos.write(out.marshall());
            out.recycle();
            this.mStatusFile.finishWrite(fos);
        } catch (IOException e1) {
            Slog.w(TAG, "Error writing status", e1);
            if (STATUS_FILE_END != null) {
                this.mStatusFile.failWrite(null);
            }
        }
    }

    private void requestSync(AuthorityInfo authorityInfo, int reason, Bundle extras) {
        if (Process.myUid() != ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE || this.mSyncRequestListener == null) {
            Builder req = new Builder().syncOnce().setExtras(extras);
            req.setSyncAdapter(authorityInfo.target.account, authorityInfo.target.provider);
            ContentResolver.requestSync(req.build());
            return;
        }
        this.mSyncRequestListener.onSyncRequest(authorityInfo.target, reason, extras);
    }

    private void requestSync(Account account, int userId, int reason, String authority, Bundle extras) {
        if (Process.myUid() != ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE || this.mSyncRequestListener == null) {
            ContentResolver.requestSync(account, authority, extras);
        } else {
            this.mSyncRequestListener.onSyncRequest(new EndPoint(account, authority, userId), reason, extras);
        }
    }

    private void readStatisticsLocked() {
        try {
            byte[] data = this.mStatisticsFile.readFully();
            Parcel in = Parcel.obtain();
            in.unmarshall(data, STATUS_FILE_END, data.length);
            in.setDataPosition(STATUS_FILE_END);
            int index = STATUS_FILE_END;
            while (true) {
                int token = in.readInt();
                if (token == 0) {
                    return;
                }
                if (token == STATISTICS_FILE_ITEM || token == STATUS_FILE_ITEM) {
                    int day = in.readInt();
                    if (token == STATUS_FILE_ITEM) {
                        day = (day - 2009) + 14245;
                    }
                    DayStats ds = new DayStats(day);
                    ds.successCount = in.readInt();
                    ds.successTime = in.readLong();
                    ds.failureCount = in.readInt();
                    ds.failureTime = in.readLong();
                    if (index < this.mDayStats.length) {
                        this.mDayStats[index] = ds;
                        index += SOURCE_LOCAL;
                    }
                } else {
                    Slog.w(TAG, "Unknown stats token: " + token);
                    return;
                }
            }
        } catch (IOException e) {
            Slog.i(TAG, "No initial statistics");
        }
    }

    private void writeStatisticsLocked() {
        if (Log.isLoggable(TAG_FILE, SOURCE_POLL)) {
            Slog.v(TAG, "Writing new " + this.mStatisticsFile.getBaseFile());
        }
        removeMessages(SOURCE_POLL);
        try {
            FileOutputStream fos = this.mStatisticsFile.startWrite();
            Parcel out = Parcel.obtain();
            int N = this.mDayStats.length;
            for (int i = STATUS_FILE_END; i < N; i += SOURCE_LOCAL) {
                DayStats ds = this.mDayStats[i];
                if (ds == null) {
                    break;
                }
                out.writeInt(STATISTICS_FILE_ITEM);
                out.writeInt(ds.day);
                out.writeInt(ds.successCount);
                out.writeLong(ds.successTime);
                out.writeInt(ds.failureCount);
                out.writeLong(ds.failureTime);
            }
            out.writeInt(STATUS_FILE_END);
            fos.write(out.marshall());
            out.recycle();
            this.mStatisticsFile.finishWrite(fos);
        } catch (IOException e1) {
            Slog.w(TAG, "Error writing stats", e1);
            if (STATUS_FILE_END != null) {
                this.mStatisticsFile.failWrite(null);
            }
        }
    }

    public void queueBackup() {
        BackupManager.dataChanged("android");
    }
}

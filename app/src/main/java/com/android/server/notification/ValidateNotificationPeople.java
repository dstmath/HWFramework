package com.android.server.notification;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.LruCache;
import android.util.Slog;
import com.android.internal.logging.MetricsLogger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ValidateNotificationPeople implements NotificationSignalExtractor {
    private static final boolean DEBUG = false;
    private static final boolean ENABLE_PEOPLE_VALIDATOR = true;
    private static final String[] LOOKUP_PROJECTION = null;
    private static final int MAX_PEOPLE = 10;
    static final float NONE = 0.0f;
    private static final int PEOPLE_CACHE_SIZE = 200;
    private static final String SETTING_ENABLE_PEOPLE_VALIDATOR = "validate_notification_people_enabled";
    static final float STARRED_CONTACT = 1.0f;
    private static final String TAG = "ValidateNoPeople";
    static final float VALID_CONTACT = 0.5f;
    private static final boolean VERBOSE = false;
    private Context mBaseContext;
    protected boolean mEnabled;
    private int mEvictionCount;
    private Handler mHandler;
    private ContentObserver mObserver;
    private LruCache<String, LookupResult> mPeopleCache;
    private NotificationUsageStats mUsageStats;
    private Map<Integer, Context> mUserToContextMap;

    /* renamed from: com.android.server.notification.ValidateNotificationPeople.1 */
    class AnonymousClass1 extends ContentObserver {
        AnonymousClass1(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange, Uri uri, int userId) {
            super.onChange(selfChange, uri, userId);
            if ((ValidateNotificationPeople.DEBUG || ValidateNotificationPeople.this.mEvictionCount % 100 == 0) && ValidateNotificationPeople.VERBOSE) {
                Slog.i(ValidateNotificationPeople.TAG, "mEvictionCount: " + ValidateNotificationPeople.this.mEvictionCount);
            }
            ValidateNotificationPeople.this.mPeopleCache.evictAll();
            ValidateNotificationPeople validateNotificationPeople = ValidateNotificationPeople.this;
            validateNotificationPeople.mEvictionCount = validateNotificationPeople.mEvictionCount + 1;
        }
    }

    /* renamed from: com.android.server.notification.ValidateNotificationPeople.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ PeopleRankingReconsideration val$prr;
        final /* synthetic */ Semaphore val$s;

        AnonymousClass2(PeopleRankingReconsideration val$prr, Semaphore val$s) {
            this.val$prr = val$prr;
            this.val$s = val$s;
        }

        public void run() {
            this.val$prr.work();
            this.val$s.release();
        }
    }

    private static class LookupResult {
        private static final long CONTACT_REFRESH_MILLIS = 3600000;
        private float mAffinity;
        private final long mExpireMillis;

        public LookupResult() {
            this.mAffinity = ValidateNotificationPeople.NONE;
            this.mExpireMillis = System.currentTimeMillis() + CONTACT_REFRESH_MILLIS;
        }

        public void mergeContact(Cursor cursor) {
            boolean isStarred = ValidateNotificationPeople.DEBUG;
            this.mAffinity = Math.max(this.mAffinity, ValidateNotificationPeople.VALID_CONTACT);
            int idIdx = cursor.getColumnIndex("_id");
            if (idIdx >= 0) {
                int id = cursor.getInt(idIdx);
                if (ValidateNotificationPeople.DEBUG) {
                    Slog.d(ValidateNotificationPeople.TAG, "contact _ID is: " + id);
                }
            } else {
                Slog.i(ValidateNotificationPeople.TAG, "invalid cursor: no _ID");
            }
            int starIdx = cursor.getColumnIndex("starred");
            if (starIdx >= 0) {
                if (cursor.getInt(starIdx) != 0) {
                    isStarred = ValidateNotificationPeople.ENABLE_PEOPLE_VALIDATOR;
                }
                if (isStarred) {
                    this.mAffinity = Math.max(this.mAffinity, ValidateNotificationPeople.STARRED_CONTACT);
                }
                if (ValidateNotificationPeople.DEBUG) {
                    Slog.d(ValidateNotificationPeople.TAG, "contact STARRED is: " + isStarred);
                }
            } else if (ValidateNotificationPeople.DEBUG) {
                Slog.d(ValidateNotificationPeople.TAG, "invalid cursor: no STARRED");
            }
        }

        private boolean isExpired() {
            return this.mExpireMillis < System.currentTimeMillis() ? ValidateNotificationPeople.ENABLE_PEOPLE_VALIDATOR : ValidateNotificationPeople.DEBUG;
        }

        private boolean isInvalid() {
            return this.mAffinity != ValidateNotificationPeople.NONE ? isExpired() : ValidateNotificationPeople.ENABLE_PEOPLE_VALIDATOR;
        }

        public float getAffinity() {
            if (isInvalid()) {
                return ValidateNotificationPeople.NONE;
            }
            return this.mAffinity;
        }
    }

    private class PeopleRankingReconsideration extends RankingReconsideration {
        private float mContactAffinity;
        private final Context mContext;
        private final LinkedList<String> mPendingLookups;
        private NotificationRecord mRecord;

        private PeopleRankingReconsideration(Context context, String key, LinkedList<String> pendingLookups) {
            super(key);
            this.mContactAffinity = ValidateNotificationPeople.NONE;
            this.mContext = context;
            this.mPendingLookups = pendingLookups;
        }

        public void work() {
            long start = SystemClock.elapsedRealtime();
            if (ValidateNotificationPeople.VERBOSE) {
                Slog.i(ValidateNotificationPeople.TAG, "Executing: validation for: " + this.mKey);
            }
            long timeStartMs = System.currentTimeMillis();
            for (String handle : this.mPendingLookups) {
                LookupResult lookupResult;
                Uri uri = Uri.parse(handle);
                if ("tel".equals(uri.getScheme())) {
                    if (ValidateNotificationPeople.DEBUG) {
                        Slog.d(ValidateNotificationPeople.TAG, "checking telephone URI: " + handle);
                    }
                    lookupResult = ValidateNotificationPeople.this.resolvePhoneContact(this.mContext, uri.getSchemeSpecificPart());
                } else if ("mailto".equals(uri.getScheme())) {
                    if (ValidateNotificationPeople.DEBUG) {
                        Slog.d(ValidateNotificationPeople.TAG, "checking mailto URI: " + handle);
                    }
                    lookupResult = ValidateNotificationPeople.this.resolveEmailContact(this.mContext, uri.getSchemeSpecificPart());
                } else if (handle.startsWith(Contacts.CONTENT_LOOKUP_URI.toString())) {
                    if (ValidateNotificationPeople.DEBUG) {
                        Slog.d(ValidateNotificationPeople.TAG, "checking lookup URI: " + handle);
                    }
                    lookupResult = ValidateNotificationPeople.this.searchContacts(this.mContext, uri);
                } else {
                    lookupResult = new LookupResult();
                    Slog.w(ValidateNotificationPeople.TAG, "unsupported URI " + handle);
                }
                if (lookupResult != null) {
                    synchronized (ValidateNotificationPeople.this.mPeopleCache) {
                        ValidateNotificationPeople.this.mPeopleCache.put(ValidateNotificationPeople.this.getCacheKey(this.mContext.getUserId(), handle), lookupResult);
                    }
                    if (ValidateNotificationPeople.DEBUG) {
                        Slog.d(ValidateNotificationPeople.TAG, "lookup contactAffinity is " + lookupResult.getAffinity());
                    }
                    this.mContactAffinity = Math.max(this.mContactAffinity, lookupResult.getAffinity());
                } else if (ValidateNotificationPeople.DEBUG) {
                    Slog.d(ValidateNotificationPeople.TAG, "lookupResult is null");
                }
            }
            if (ValidateNotificationPeople.DEBUG) {
                Slog.d(ValidateNotificationPeople.TAG, "Validation finished in " + (System.currentTimeMillis() - timeStartMs) + "ms");
            }
            if (this.mRecord != null) {
                ValidateNotificationPeople.this.mUsageStats.registerPeopleAffinity(this.mRecord, this.mContactAffinity > ValidateNotificationPeople.NONE ? ValidateNotificationPeople.ENABLE_PEOPLE_VALIDATOR : ValidateNotificationPeople.DEBUG, this.mContactAffinity == ValidateNotificationPeople.STARRED_CONTACT ? ValidateNotificationPeople.ENABLE_PEOPLE_VALIDATOR : ValidateNotificationPeople.DEBUG, ValidateNotificationPeople.DEBUG);
            }
            MetricsLogger.histogram(ValidateNotificationPeople.this.mBaseContext, "validate_people_lookup_latency", (int) (SystemClock.elapsedRealtime() - start));
        }

        public void applyChangesLocked(NotificationRecord operand) {
            operand.setContactAffinity(Math.max(this.mContactAffinity, operand.getContactAffinity()));
            if (ValidateNotificationPeople.VERBOSE) {
                Slog.i(ValidateNotificationPeople.TAG, "final affinity: " + operand.getContactAffinity());
            }
        }

        public float getContactAffinity() {
            return this.mContactAffinity;
        }

        public void setRecord(NotificationRecord record) {
            this.mRecord = record;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.notification.ValidateNotificationPeople.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.notification.ValidateNotificationPeople.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.notification.ValidateNotificationPeople.<clinit>():void");
    }

    private com.android.server.notification.ValidateNotificationPeople.LookupResult searchContacts(android.content.Context r10, android.net.Uri r11) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x003e in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r9 = this;
        r7 = new com.android.server.notification.ValidateNotificationPeople$LookupResult;
        r7.<init>();
        r6 = 0;
        r0 = r10.getContentResolver();	 Catch:{ Throwable -> 0x002f, all -> 0x0045 }
        r2 = LOOKUP_PROJECTION;	 Catch:{ Throwable -> 0x002f, all -> 0x0045 }
        r3 = 0;	 Catch:{ Throwable -> 0x002f, all -> 0x0045 }
        r4 = 0;	 Catch:{ Throwable -> 0x002f, all -> 0x0045 }
        r5 = 0;	 Catch:{ Throwable -> 0x002f, all -> 0x0045 }
        r1 = r11;	 Catch:{ Throwable -> 0x002f, all -> 0x0045 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Throwable -> 0x002f, all -> 0x0045 }
        if (r6 != 0) goto L_0x0025;	 Catch:{ Throwable -> 0x002f, all -> 0x0045 }
    L_0x0016:
        r0 = "ValidateNoPeople";	 Catch:{ Throwable -> 0x002f, all -> 0x0045 }
        r1 = "Null cursor from contacts query.";	 Catch:{ Throwable -> 0x002f, all -> 0x0045 }
        android.util.Slog.w(r0, r1);	 Catch:{ Throwable -> 0x002f, all -> 0x0045 }
        if (r6 == 0) goto L_0x0024;
    L_0x0021:
        r6.close();
    L_0x0024:
        return r7;
    L_0x0025:
        r0 = r6.moveToNext();	 Catch:{ Throwable -> 0x002f, all -> 0x0045 }
        if (r0 == 0) goto L_0x003f;	 Catch:{ Throwable -> 0x002f, all -> 0x0045 }
    L_0x002b:
        r7.mergeContact(r6);	 Catch:{ Throwable -> 0x002f, all -> 0x0045 }
        goto L_0x0025;
    L_0x002f:
        r8 = move-exception;
        r0 = "ValidateNoPeople";	 Catch:{ Throwable -> 0x002f, all -> 0x0045 }
        r1 = "Problem getting content resolver or performing contacts query.";	 Catch:{ Throwable -> 0x002f, all -> 0x0045 }
        android.util.Slog.w(r0, r1, r8);	 Catch:{ Throwable -> 0x002f, all -> 0x0045 }
        if (r6 == 0) goto L_0x003e;
    L_0x003b:
        r6.close();
    L_0x003e:
        return r7;
    L_0x003f:
        if (r6 == 0) goto L_0x003e;
    L_0x0041:
        r6.close();
        goto L_0x003e;
    L_0x0045:
        r0 = move-exception;
        if (r6 == 0) goto L_0x004b;
    L_0x0048:
        r6.close();
    L_0x004b:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.notification.ValidateNotificationPeople.searchContacts(android.content.Context, android.net.Uri):com.android.server.notification.ValidateNotificationPeople$LookupResult");
    }

    public void initialize(Context context, NotificationUsageStats usageStats) {
        if (DEBUG) {
            Slog.d(TAG, "Initializing  " + getClass().getSimpleName() + ".");
        }
        this.mUserToContextMap = new ArrayMap();
        this.mBaseContext = context;
        this.mUsageStats = usageStats;
        this.mPeopleCache = new LruCache(PEOPLE_CACHE_SIZE);
        this.mEnabled = 1 == Global.getInt(this.mBaseContext.getContentResolver(), SETTING_ENABLE_PEOPLE_VALIDATOR, 1) ? ENABLE_PEOPLE_VALIDATOR : DEBUG;
        if (this.mEnabled) {
            this.mHandler = new Handler();
            this.mObserver = new AnonymousClass1(this.mHandler);
            this.mBaseContext.getContentResolver().registerContentObserver(Contacts.CONTENT_URI, ENABLE_PEOPLE_VALIDATOR, this.mObserver, -1);
        }
    }

    public RankingReconsideration process(NotificationRecord record) {
        if (!this.mEnabled) {
            if (VERBOSE) {
                Slog.i(TAG, "disabled");
            }
            return null;
        } else if (record == null || record.getNotification() == null) {
            if (VERBOSE) {
                Slog.i(TAG, "skipping empty notification");
            }
            return null;
        } else if (record.getUserId() == -1) {
            if (VERBOSE) {
                Slog.i(TAG, "skipping global notification");
            }
            return null;
        } else {
            Context context = getContextAsUser(record.getUser());
            if (context != null) {
                return validatePeople(context, record);
            }
            if (VERBOSE) {
                Slog.i(TAG, "skipping notification that lacks a context");
            }
            return null;
        }
    }

    public void setConfig(RankingConfig config) {
    }

    public float getContactAffinity(UserHandle userHandle, Bundle extras, int timeoutMs, float timeoutAffinity) {
        if (DEBUG) {
            Slog.d(TAG, "checking affinity for " + userHandle);
        }
        if (extras == null) {
            return NONE;
        }
        String key = Long.toString(System.nanoTime());
        float[] affinityOut = new float[1];
        Context context = getContextAsUser(userHandle);
        if (context == null) {
            return NONE;
        }
        PeopleRankingReconsideration prr = validatePeople(context, key, extras, affinityOut);
        float affinity = affinityOut[0];
        if (prr != null) {
            Semaphore s = new Semaphore(0);
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new AnonymousClass2(prr, s));
            try {
                if (s.tryAcquire((long) timeoutMs, TimeUnit.MILLISECONDS)) {
                    affinity = Math.max(prr.getContactAffinity(), affinity);
                } else {
                    Slog.w(TAG, "Timeout while waiting for affinity: " + key + ". " + "Returning timeoutAffinity=" + timeoutAffinity);
                    return timeoutAffinity;
                }
            } catch (InterruptedException e) {
                Slog.w(TAG, "InterruptedException while waiting for affinity: " + key + ". " + "Returning affinity=" + affinity, e);
                return affinity;
            }
        }
        return affinity;
    }

    private Context getContextAsUser(UserHandle userHandle) {
        Context context = (Context) this.mUserToContextMap.get(Integer.valueOf(userHandle.getIdentifier()));
        if (context != null) {
            return context;
        }
        try {
            context = this.mBaseContext.createPackageContextAsUser("android", 0, userHandle);
            this.mUserToContextMap.put(Integer.valueOf(userHandle.getIdentifier()), context);
            return context;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "failed to create package context for lookups", e);
            return context;
        }
    }

    private RankingReconsideration validatePeople(Context context, NotificationRecord record) {
        boolean z = DEBUG;
        float[] affinityOut = new float[1];
        PeopleRankingReconsideration rr = validatePeople(context, record.getKey(), record.getNotification().extras, affinityOut);
        float affinity = affinityOut[0];
        record.setContactAffinity(affinity);
        if (rr == null) {
            NotificationUsageStats notificationUsageStats = this.mUsageStats;
            boolean z2 = affinity > NONE ? ENABLE_PEOPLE_VALIDATOR : DEBUG;
            if (affinity == STARRED_CONTACT) {
                z = ENABLE_PEOPLE_VALIDATOR;
            }
            notificationUsageStats.registerPeopleAffinity(record, z2, z, ENABLE_PEOPLE_VALIDATOR);
        } else {
            rr.setRecord(record);
        }
        return rr;
    }

    private PeopleRankingReconsideration validatePeople(Context context, String key, Bundle extras, float[] affinityOut) {
        long start = SystemClock.elapsedRealtime();
        float affinity = NONE;
        if (extras == null) {
            return null;
        }
        String[] people = getExtraPeople(extras);
        if (people == null || people.length == 0) {
            return null;
        }
        if (VERBOSE) {
            Slog.i(TAG, "Validating: " + key + " for " + context.getUserId());
        }
        LinkedList<String> pendingLookups = new LinkedList();
        int personIdx = 0;
        while (personIdx < people.length && personIdx < MAX_PEOPLE) {
            String handle = people[personIdx];
            if (!TextUtils.isEmpty(handle)) {
                synchronized (this.mPeopleCache) {
                    LookupResult lookupResult = (LookupResult) this.mPeopleCache.get(getCacheKey(context.getUserId(), handle));
                    if (lookupResult == null || lookupResult.isExpired()) {
                        pendingLookups.add(handle);
                    } else if (DEBUG) {
                        Slog.d(TAG, "using cached lookupResult");
                    }
                    if (lookupResult != null) {
                        affinity = Math.max(affinity, lookupResult.getAffinity());
                    }
                }
            }
            personIdx++;
        }
        affinityOut[0] = affinity;
        MetricsLogger.histogram(this.mBaseContext, "validate_people_cache_latency", (int) (SystemClock.elapsedRealtime() - start));
        if (pendingLookups.isEmpty()) {
            if (VERBOSE) {
                Slog.i(TAG, "final affinity: " + affinity);
            }
            return null;
        }
        if (DEBUG) {
            Slog.d(TAG, "Pending: future work scheduled for: " + key);
        }
        return new PeopleRankingReconsideration(context, key, pendingLookups, null);
    }

    private String getCacheKey(int userId, String handle) {
        return Integer.toString(userId) + ":" + handle;
    }

    public static String[] getExtraPeople(Bundle extras) {
        ArrayList people = extras.get("android.people");
        if (people instanceof String[]) {
            return (String[]) people;
        }
        int N;
        String[] array;
        int i;
        if (people instanceof ArrayList) {
            ArrayList<String> arrayList = people;
            if (arrayList.isEmpty()) {
                return null;
            }
            if (arrayList.get(0) instanceof String) {
                ArrayList<String> stringArray = arrayList;
                return (String[]) arrayList.toArray(new String[arrayList.size()]);
            } else if (!(arrayList.get(0) instanceof CharSequence)) {
                return null;
            } else {
                ArrayList<CharSequence> charSeqList = arrayList;
                N = arrayList.size();
                array = new String[N];
                for (i = 0; i < N; i++) {
                    array[i] = ((CharSequence) arrayList.get(i)).toString();
                }
                return array;
            }
        } else if (people instanceof String) {
            return new String[]{(String) people};
        } else if (people instanceof char[]) {
            return new String[]{new String((char[]) people)};
        } else if (people instanceof CharSequence) {
            return new String[]{((CharSequence) people).toString()};
        } else if (!(people instanceof CharSequence[])) {
            return null;
        } else {
            CharSequence[] charSeqArray = (CharSequence[]) people;
            N = charSeqArray.length;
            array = new String[N];
            for (i = 0; i < N; i++) {
                array[i] = charSeqArray[i].toString();
            }
            return array;
        }
    }

    private LookupResult resolvePhoneContact(Context context, String number) {
        return searchContacts(context, Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number)));
    }

    private LookupResult resolveEmailContact(Context context, String email) {
        return searchContacts(context, Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(email)));
    }
}

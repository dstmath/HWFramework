package com.android.server.notification;

import android.app.Person;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.LruCache;
import android.util.Slog;
import com.android.server.pm.PackageManagerService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ValidateNotificationPeople implements NotificationSignalExtractor {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final boolean ENABLE_PEOPLE_VALIDATOR = true;
    private static final String[] LOOKUP_PROJECTION = {"_id", "starred"};
    private static final int MAX_PEOPLE = 10;
    static final float NONE = 0.0f;
    private static final int PEOPLE_CACHE_SIZE = 200;
    private static final String SETTING_ENABLE_PEOPLE_VALIDATOR = "validate_notification_people_enabled";
    static final float STARRED_CONTACT = 1.0f;
    private static final String TAG = "ValidateNoPeople";
    static final float VALID_CONTACT = 0.5f;
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private Context mBaseContext;
    protected boolean mEnabled;
    private int mEvictionCount;
    private Handler mHandler;
    private ContentObserver mObserver;
    private LruCache<String, LookupResult> mPeopleCache;
    private NotificationUsageStats mUsageStats;
    private Map<Integer, Context> mUserToContextMap;

    static /* synthetic */ int access$108(ValidateNotificationPeople x0) {
        int i = x0.mEvictionCount;
        x0.mEvictionCount = i + 1;
        return i;
    }

    @Override // com.android.server.notification.NotificationSignalExtractor
    public void initialize(Context context, NotificationUsageStats usageStats) {
        if (DEBUG) {
            Slog.d(TAG, "Initializing  " + getClass().getSimpleName() + ".");
        }
        this.mUserToContextMap = new ArrayMap();
        this.mBaseContext = context;
        this.mUsageStats = usageStats;
        this.mPeopleCache = new LruCache<>(200);
        this.mEnabled = 1 == Settings.Global.getInt(this.mBaseContext.getContentResolver(), SETTING_ENABLE_PEOPLE_VALIDATOR, 1);
        if (this.mEnabled) {
            this.mHandler = new Handler();
            this.mObserver = new ContentObserver(this.mHandler) {
                /* class com.android.server.notification.ValidateNotificationPeople.AnonymousClass1 */

                @Override // android.database.ContentObserver
                public void onChange(boolean selfChange, Uri uri, int userId) {
                    super.onChange(selfChange, uri, userId);
                    if ((ValidateNotificationPeople.DEBUG || ValidateNotificationPeople.this.mEvictionCount % 100 == 0) && ValidateNotificationPeople.VERBOSE) {
                        Slog.i(ValidateNotificationPeople.TAG, "mEvictionCount: " + ValidateNotificationPeople.this.mEvictionCount);
                    }
                    ValidateNotificationPeople.this.mPeopleCache.evictAll();
                    ValidateNotificationPeople.access$108(ValidateNotificationPeople.this);
                }
            };
            this.mBaseContext.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, this.mObserver, -1);
        }
    }

    @Override // com.android.server.notification.NotificationSignalExtractor
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

    @Override // com.android.server.notification.NotificationSignalExtractor
    public void setConfig(RankingConfig config) {
    }

    @Override // com.android.server.notification.NotificationSignalExtractor
    public void setZenHelper(ZenModeHelper helper) {
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
        final PeopleRankingReconsideration prr = validatePeople(context, key, extras, null, affinityOut);
        float affinity = affinityOut[0];
        if (prr == null) {
            return affinity;
        }
        final Semaphore s = new Semaphore(0);
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            /* class com.android.server.notification.ValidateNotificationPeople.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                prr.work();
                s.release();
            }
        });
        try {
            if (s.tryAcquire((long) timeoutMs, TimeUnit.MILLISECONDS)) {
                return Math.max(prr.getContactAffinity(), affinity);
            }
            Slog.w(TAG, "Timeout while waiting for affinity: " + key + ". Returning timeoutAffinity=" + timeoutAffinity);
            return timeoutAffinity;
        } catch (InterruptedException e) {
            Slog.w(TAG, "InterruptedException while waiting for affinity: " + key + ". Returning affinity=" + affinity, e);
            return affinity;
        }
    }

    private Context getContextAsUser(UserHandle userHandle) {
        Context context = this.mUserToContextMap.get(Integer.valueOf(userHandle.getIdentifier()));
        if (context != null) {
            return context;
        }
        try {
            context = this.mBaseContext.createPackageContextAsUser(PackageManagerService.PLATFORM_PACKAGE_NAME, 0, userHandle);
            this.mUserToContextMap.put(Integer.valueOf(userHandle.getIdentifier()), context);
            return context;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "failed to create package context for lookups", e);
            return context;
        }
    }

    private RankingReconsideration validatePeople(Context context, NotificationRecord record) {
        float[] affinityOut = new float[1];
        PeopleRankingReconsideration rr = validatePeople(context, record.getKey(), record.getNotification().extras, record.getPeopleOverride(), affinityOut);
        boolean z = false;
        float affinity = affinityOut[0];
        record.setContactAffinity(affinity);
        if (rr == null) {
            NotificationUsageStats notificationUsageStats = this.mUsageStats;
            boolean z2 = affinity > NONE;
            if (affinity == 1.0f) {
                z = true;
            }
            notificationUsageStats.registerPeopleAffinity(record, z2, z, true);
        } else {
            rr.setRecord(record);
        }
        return rr;
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x0093  */
    private PeopleRankingReconsideration validatePeople(Context context, String key, Bundle extras, List<String> peopleOverride, float[] affinityOut) {
        float affinity;
        if (extras == null) {
            return null;
        }
        Set<String> people = new ArraySet<>(peopleOverride);
        String[] notificationPeople = getExtraPeople(extras);
        if (notificationPeople != null) {
            people.addAll(Arrays.asList(notificationPeople));
        }
        if (VERBOSE) {
            Slog.i(TAG, "Validating: " + key + " for " + context.getUserId());
        }
        LinkedList<String> pendingLookups = new LinkedList<>();
        Iterator<String> it = people.iterator();
        int personIdx = 0;
        float affinity2 = 0.0f;
        while (true) {
            if (!it.hasNext()) {
                affinity = affinity2;
                break;
            }
            String handle = it.next();
            if (!TextUtils.isEmpty(handle)) {
                synchronized (this.mPeopleCache) {
                    LookupResult lookupResult = this.mPeopleCache.get(getCacheKey(context.getUserId(), handle));
                    if (lookupResult != null) {
                        if (!lookupResult.isExpired()) {
                            if (DEBUG) {
                                Slog.d(TAG, "using cached lookupResult");
                            }
                            if (lookupResult != null) {
                                affinity2 = Math.max(affinity2, lookupResult.getAffinity());
                            }
                        }
                    }
                    pendingLookups.add(handle);
                    if (lookupResult != null) {
                    }
                }
                personIdx++;
                if (personIdx == 10) {
                    affinity = affinity2;
                    break;
                }
            }
        }
        affinityOut[0] = affinity;
        if (pendingLookups.isEmpty()) {
            if (VERBOSE) {
                Slog.i(TAG, "final affinity: " + affinity);
            }
            return null;
        }
        if (DEBUG) {
            Slog.d(TAG, "Pending: future work scheduled for: " + key);
        }
        return new PeopleRankingReconsideration(context, key, pendingLookups);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getCacheKey(int userId, String handle) {
        return Integer.toString(userId) + ":" + handle;
    }

    public static String[] getExtraPeople(Bundle extras) {
        return combineLists(getExtraPeopleForKey(extras, "android.people"), getExtraPeopleForKey(extras, "android.people.list"));
    }

    private static String[] combineLists(String[] first, String[] second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        ArraySet<String> people = new ArraySet<>(first.length + second.length);
        for (String person : first) {
            people.add(person);
        }
        for (String person2 : second) {
            people.add(person2);
        }
        return (String[]) people.toArray();
    }

    private static String[] getExtraPeopleForKey(Bundle extras, String key) {
        Object people = extras.get(key);
        if (people instanceof String[]) {
            return (String[]) people;
        }
        if (people instanceof ArrayList) {
            ArrayList arrayList = (ArrayList) people;
            if (arrayList.isEmpty()) {
                return null;
            }
            if (arrayList.get(0) instanceof String) {
                return (String[]) arrayList.toArray(new String[arrayList.size()]);
            }
            if (arrayList.get(0) instanceof CharSequence) {
                int N = arrayList.size();
                String[] array = new String[N];
                for (int i = 0; i < N; i++) {
                    array[i] = ((CharSequence) arrayList.get(i)).toString();
                }
                return array;
            } else if (!(arrayList.get(0) instanceof Person)) {
                return null;
            } else {
                int N2 = arrayList.size();
                String[] array2 = new String[N2];
                for (int i2 = 0; i2 < N2; i2++) {
                    array2[i2] = ((Person) arrayList.get(i2)).resolveToLegacyUri();
                }
                return array2;
            }
        } else if (people instanceof String) {
            return new String[]{(String) people};
        } else {
            if (people instanceof char[]) {
                return new String[]{new String((char[]) people)};
            }
            if (people instanceof CharSequence) {
                return new String[]{((CharSequence) people).toString()};
            }
            if (!(people instanceof CharSequence[])) {
                return null;
            }
            CharSequence[] charSeqArray = (CharSequence[]) people;
            int N3 = charSeqArray.length;
            String[] array3 = new String[N3];
            for (int i3 = 0; i3 < N3; i3++) {
                array3[i3] = charSeqArray[i3].toString();
            }
            return array3;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private LookupResult resolvePhoneContact(Context context, String number) {
        return searchContacts(context, Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number)));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private LookupResult resolveEmailContact(Context context, String email) {
        return searchContacts(context, Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_LOOKUP_URI, Uri.encode(email)));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003b, code lost:
        if (0 == 0) goto L_0x003e;
     */
    private LookupResult searchContacts(Context context, Uri lookupUri) {
        LookupResult lookupResult = new LookupResult();
        Cursor c = null;
        try {
            c = context.getContentResolver().query(lookupUri, LOOKUP_PROJECTION, null, null, null);
            if (c == null) {
                Slog.w(TAG, "Null cursor from contacts query.");
                if (c != null) {
                    c.close();
                }
                return lookupResult;
            }
            while (c.moveToNext()) {
                lookupResult.mergeContact(c);
            }
            c.close();
            return lookupResult;
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public static class LookupResult {
        private static final long CONTACT_REFRESH_MILLIS = 3600000;
        private float mAffinity = ValidateNotificationPeople.NONE;
        private final long mExpireMillis = (System.currentTimeMillis() + 3600000);

        public void mergeContact(Cursor cursor) {
            this.mAffinity = Math.max(this.mAffinity, (float) ValidateNotificationPeople.VALID_CONTACT);
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
                boolean isStarred = cursor.getInt(starIdx) != 0;
                if (isStarred) {
                    this.mAffinity = Math.max(this.mAffinity, 1.0f);
                }
                if (ValidateNotificationPeople.DEBUG) {
                    Slog.d(ValidateNotificationPeople.TAG, "contact STARRED is: " + isStarred);
                }
            } else if (ValidateNotificationPeople.DEBUG) {
                Slog.d(ValidateNotificationPeople.TAG, "invalid cursor: no STARRED");
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isExpired() {
            return this.mExpireMillis < System.currentTimeMillis();
        }

        private boolean isInvalid() {
            return this.mAffinity == ValidateNotificationPeople.NONE || isExpired();
        }

        public float getAffinity() {
            if (isInvalid()) {
                return ValidateNotificationPeople.NONE;
            }
            return this.mAffinity;
        }
    }

    /* access modifiers changed from: private */
    public class PeopleRankingReconsideration extends RankingReconsideration {
        private static final long LOOKUP_TIME = 1000;
        private float mContactAffinity;
        private final Context mContext;
        private final LinkedList<String> mPendingLookups;
        private NotificationRecord mRecord;

        private PeopleRankingReconsideration(Context context, String key, LinkedList<String> pendingLookups) {
            super(key, 1000);
            this.mContactAffinity = ValidateNotificationPeople.NONE;
            this.mContext = context;
            this.mPendingLookups = pendingLookups;
        }

        @Override // com.android.server.notification.RankingReconsideration
        public void work() {
            LookupResult lookupResult;
            if (ValidateNotificationPeople.VERBOSE) {
                Slog.i(ValidateNotificationPeople.TAG, "Executing: validation for: " + this.mKey);
            }
            long timeStartMs = System.currentTimeMillis();
            Iterator<String> it = this.mPendingLookups.iterator();
            while (it.hasNext()) {
                String handle = it.next();
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
                } else if (handle.startsWith(ContactsContract.Contacts.CONTENT_LOOKUP_URI.toString())) {
                    if (ValidateNotificationPeople.DEBUG) {
                        Slog.d(ValidateNotificationPeople.TAG, "checking lookup URI: " + handle);
                    }
                    lookupResult = ValidateNotificationPeople.this.searchContacts(this.mContext, uri);
                } else {
                    lookupResult = new LookupResult();
                    if (!com.android.server.pm.Settings.ATTR_NAME.equals(uri.getScheme())) {
                        Slog.w(ValidateNotificationPeople.TAG, "unsupported URI " + handle);
                    }
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
                NotificationUsageStats notificationUsageStats = ValidateNotificationPeople.this.mUsageStats;
                NotificationRecord notificationRecord = this.mRecord;
                boolean z = true;
                boolean z2 = this.mContactAffinity > ValidateNotificationPeople.NONE;
                if (this.mContactAffinity != 1.0f) {
                    z = false;
                }
                notificationUsageStats.registerPeopleAffinity(notificationRecord, z2, z, false);
            }
        }

        @Override // com.android.server.notification.RankingReconsideration
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
}

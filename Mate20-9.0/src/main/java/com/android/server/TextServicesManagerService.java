package com.android.server;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.view.textservice.SpellCheckerInfo;
import android.view.textservice.SpellCheckerSubtype;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.inputmethod.InputMethodUtils;
import com.android.internal.textservice.ISpellCheckerService;
import com.android.internal.textservice.ISpellCheckerServiceCallback;
import com.android.internal.textservice.ISpellCheckerSession;
import com.android.internal.textservice.ISpellCheckerSessionListener;
import com.android.internal.textservice.ITextServicesManager;
import com.android.internal.textservice.ITextServicesSessionListener;
import com.android.internal.textservice.LazyIntToIntMap;
import com.android.internal.util.DumpUtils;
import com.android.server.TextServicesManagerService;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import org.xmlpull.v1.XmlPullParserException;

public class TextServicesManagerService extends ITextServicesManager.Stub {
    private static final boolean DBG = false;
    /* access modifiers changed from: private */
    public static final String TAG = TextServicesManagerService.class.getSimpleName();
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private final TextServicesMonitor mMonitor;
    @GuardedBy("mLock")
    private final LazyIntToIntMap mSpellCheckerOwnerUserIdMap;
    /* access modifiers changed from: private */
    public final SparseArray<TextServicesData> mUserData = new SparseArray<>();
    private final UserManager mUserManager;

    private static final class ISpellCheckerServiceCallbackBinder extends ISpellCheckerServiceCallback.Stub {
        private final SpellCheckerBindGroup mBindGroup;
        private final SessionRequest mRequest;

        ISpellCheckerServiceCallbackBinder(SpellCheckerBindGroup bindGroup, SessionRequest request) {
            this.mBindGroup = bindGroup;
            this.mRequest = request;
        }

        public void onSessionCreated(ISpellCheckerSession newSession) {
            this.mBindGroup.onSessionCreated(newSession, this.mRequest);
        }
    }

    private static final class InternalDeathRecipients extends RemoteCallbackList<ISpellCheckerSessionListener> {
        private final SpellCheckerBindGroup mGroup;

        public InternalDeathRecipients(SpellCheckerBindGroup group) {
            this.mGroup = group;
        }

        public void onCallbackDied(ISpellCheckerSessionListener listener) {
            this.mGroup.removeListener(listener);
        }
    }

    private final class InternalServiceConnection implements ServiceConnection {
        /* access modifiers changed from: private */
        public final String mSciId;
        /* access modifiers changed from: private */
        public final HashMap<String, SpellCheckerBindGroup> mSpellCheckerBindGroups;

        public InternalServiceConnection(String id, HashMap<String, SpellCheckerBindGroup> spellCheckerBindGroups) {
            this.mSciId = id;
            this.mSpellCheckerBindGroups = spellCheckerBindGroups;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (TextServicesManagerService.this.mLock) {
                onServiceConnectedInnerLocked(name, service);
            }
        }

        private void onServiceConnectedInnerLocked(ComponentName name, IBinder service) {
            ISpellCheckerService spellChecker = ISpellCheckerService.Stub.asInterface(service);
            SpellCheckerBindGroup group = this.mSpellCheckerBindGroups.get(this.mSciId);
            if (group != null && this == group.mInternalConnection) {
                group.onServiceConnectedLocked(spellChecker);
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (TextServicesManagerService.this.mLock) {
                onServiceDisconnectedInnerLocked(name);
            }
        }

        private void onServiceDisconnectedInnerLocked(ComponentName name) {
            SpellCheckerBindGroup group = this.mSpellCheckerBindGroups.get(this.mSciId);
            if (group != null && this == group.mInternalConnection) {
                group.onServiceDisconnectedLocked();
            }
        }
    }

    public static final class Lifecycle extends SystemService {
        private TextServicesManagerService mService;

        public Lifecycle(Context context) {
            super(context);
            this.mService = new TextServicesManagerService(context);
        }

        /* JADX WARNING: type inference failed for: r1v0, types: [android.os.IBinder, com.android.server.TextServicesManagerService] */
        public void onStart() {
            publishBinderService("textservices", this.mService);
        }

        public void onStopUser(int userHandle) {
            this.mService.onStopUser(userHandle);
        }

        public void onUnlockUser(int userHandle) {
            this.mService.onUnlockUser(userHandle);
        }
    }

    private static final class SessionRequest {
        public final Bundle mBundle;
        public final String mLocale;
        public final ISpellCheckerSessionListener mScListener;
        public final ITextServicesSessionListener mTsListener;
        public final int mUid;

        SessionRequest(int uid, String locale, ITextServicesSessionListener tsListener, ISpellCheckerSessionListener scListener, Bundle bundle) {
            this.mUid = uid;
            this.mLocale = locale;
            this.mTsListener = tsListener;
            this.mScListener = scListener;
            this.mBundle = bundle;
        }
    }

    private final class SpellCheckerBindGroup {
        private final String TAG = SpellCheckerBindGroup.class.getSimpleName();
        /* access modifiers changed from: private */
        public boolean mConnected;
        /* access modifiers changed from: private */
        public final InternalServiceConnection mInternalConnection;
        /* access modifiers changed from: private */
        public final InternalDeathRecipients mListeners;
        /* access modifiers changed from: private */
        public final ArrayList<SessionRequest> mOnGoingSessionRequests = new ArrayList<>();
        /* access modifiers changed from: private */
        public final ArrayList<SessionRequest> mPendingSessionRequests = new ArrayList<>();
        /* access modifiers changed from: private */
        public ISpellCheckerService mSpellChecker;
        HashMap<String, SpellCheckerBindGroup> mSpellCheckerBindGroups;
        /* access modifiers changed from: private */
        public boolean mUnbindCalled;

        public SpellCheckerBindGroup(InternalServiceConnection connection) {
            this.mInternalConnection = connection;
            this.mListeners = new InternalDeathRecipients(this);
            this.mSpellCheckerBindGroups = connection.mSpellCheckerBindGroups;
        }

        public void onServiceConnectedLocked(ISpellCheckerService spellChecker) {
            if (!this.mUnbindCalled) {
                this.mSpellChecker = spellChecker;
                this.mConnected = true;
                try {
                    int size = this.mPendingSessionRequests.size();
                    for (int i = 0; i < size; i++) {
                        SessionRequest request = this.mPendingSessionRequests.get(i);
                        this.mSpellChecker.getISpellCheckerSession(request.mLocale, request.mScListener, request.mBundle, new ISpellCheckerServiceCallbackBinder(this, request));
                        this.mOnGoingSessionRequests.add(request);
                    }
                    this.mPendingSessionRequests.clear();
                } catch (RemoteException e) {
                    removeAllLocked();
                }
                cleanLocked();
            }
        }

        public void onServiceDisconnectedLocked() {
            this.mSpellChecker = null;
            this.mConnected = false;
        }

        public void removeListener(ISpellCheckerSessionListener listener) {
            synchronized (TextServicesManagerService.this.mLock) {
                this.mListeners.unregister(listener);
                Predicate<SessionRequest> removeCondition = new Predicate(listener.asBinder()) {
                    private final /* synthetic */ IBinder f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final boolean test(Object obj) {
                        return TextServicesManagerService.SpellCheckerBindGroup.lambda$removeListener$0(this.f$0, (TextServicesManagerService.SessionRequest) obj);
                    }
                };
                this.mPendingSessionRequests.removeIf(removeCondition);
                this.mOnGoingSessionRequests.removeIf(removeCondition);
                cleanLocked();
            }
        }

        static /* synthetic */ boolean lambda$removeListener$0(IBinder scListenerBinder, SessionRequest request) {
            return request.mScListener.asBinder() == scListenerBinder;
        }

        private void cleanLocked() {
            if (!this.mUnbindCalled && this.mListeners.getRegisteredCallbackCount() <= 0 && this.mPendingSessionRequests.isEmpty() && this.mOnGoingSessionRequests.isEmpty()) {
                String sciId = this.mInternalConnection.mSciId;
                if (this.mSpellCheckerBindGroups.get(sciId) == this) {
                    this.mSpellCheckerBindGroups.remove(sciId);
                }
                TextServicesManagerService.this.mContext.unbindService(this.mInternalConnection);
                this.mUnbindCalled = true;
            }
        }

        public void removeAllLocked() {
            Slog.e(this.TAG, "Remove the spell checker bind unexpectedly.");
            for (int i = this.mListeners.getRegisteredCallbackCount() - 1; i >= 0; i--) {
                this.mListeners.unregister(this.mListeners.getRegisteredCallbackItem(i));
            }
            this.mPendingSessionRequests.clear();
            this.mOnGoingSessionRequests.clear();
            cleanLocked();
        }

        public void getISpellCheckerSessionOrQueueLocked(SessionRequest request) {
            if (!this.mUnbindCalled) {
                this.mListeners.register(request.mScListener);
                if (!this.mConnected) {
                    this.mPendingSessionRequests.add(request);
                    return;
                }
                try {
                    this.mSpellChecker.getISpellCheckerSession(request.mLocale, request.mScListener, request.mBundle, new ISpellCheckerServiceCallbackBinder(this, request));
                    this.mOnGoingSessionRequests.add(request);
                } catch (RemoteException e) {
                    removeAllLocked();
                }
                cleanLocked();
            }
        }

        /* access modifiers changed from: package-private */
        public void onSessionCreated(ISpellCheckerSession newSession, SessionRequest request) {
            synchronized (TextServicesManagerService.this.mLock) {
                if (!this.mUnbindCalled) {
                    if (this.mOnGoingSessionRequests.remove(request)) {
                        try {
                            request.mTsListener.onServiceConnected(newSession);
                        } catch (RemoteException e) {
                        }
                    }
                    cleanLocked();
                }
            }
        }
    }

    private static class TextServicesData {
        private final Context mContext;
        private final ContentResolver mResolver;
        /* access modifiers changed from: private */
        public final HashMap<String, SpellCheckerBindGroup> mSpellCheckerBindGroups;
        /* access modifiers changed from: private */
        public final ArrayList<SpellCheckerInfo> mSpellCheckerList;
        /* access modifiers changed from: private */
        public final HashMap<String, SpellCheckerInfo> mSpellCheckerMap;
        public int mUpdateCount = 0;
        /* access modifiers changed from: private */
        public final int mUserId;

        public TextServicesData(int userId, Context context) {
            this.mUserId = userId;
            this.mSpellCheckerMap = new HashMap<>();
            this.mSpellCheckerList = new ArrayList<>();
            this.mSpellCheckerBindGroups = new HashMap<>();
            this.mContext = context;
            this.mResolver = context.getContentResolver();
        }

        private void putString(String key, String str) {
            Settings.Secure.putStringForUser(this.mResolver, key, str, this.mUserId);
        }

        private String getString(String key, String defaultValue) {
            String result = Settings.Secure.getStringForUser(this.mResolver, key, this.mUserId);
            return result != null ? result : defaultValue;
        }

        private void putInt(String key, int value) {
            Settings.Secure.putIntForUser(this.mResolver, key, value, this.mUserId);
        }

        private int getInt(String key, int defaultValue) {
            return Settings.Secure.getIntForUser(this.mResolver, key, defaultValue, this.mUserId);
        }

        private boolean getBoolean(String key, boolean defaultValue) {
            return getInt(key, defaultValue) == 1;
        }

        private void putSelectedSpellChecker(String sciId) {
            putString("selected_spell_checker", sciId);
        }

        private void putSelectedSpellCheckerSubtype(int hashCode) {
            putInt("selected_spell_checker_subtype", hashCode);
        }

        private String getSelectedSpellChecker() {
            return getString("selected_spell_checker", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        }

        public int getSelectedSpellCheckerSubtype(int defaultValue) {
            return getInt("selected_spell_checker_subtype", defaultValue);
        }

        public boolean isSpellCheckerEnabled() {
            int spellCheckFlag = 0;
            String supportCheckLanguage = Settings.Secure.getString(this.mContext.getContentResolver(), "check_language");
            if (!TextUtils.isEmpty(supportCheckLanguage) && supportCheckLanguage.contains(Locale.getDefault().getLanguage())) {
                spellCheckFlag = 1;
            }
            boolean z = true;
            if (spellCheckFlag != 1) {
                z = false;
            }
            return getBoolean("spell_checker_enabled", z);
        }

        public SpellCheckerInfo getCurrentSpellChecker() {
            String curSpellCheckerId = getSelectedSpellChecker();
            if (TextUtils.isEmpty(curSpellCheckerId)) {
                return null;
            }
            return this.mSpellCheckerMap.get(curSpellCheckerId);
        }

        public void setCurrentSpellChecker(SpellCheckerInfo sci) {
            if (sci != null) {
                putSelectedSpellChecker(sci.getId());
            } else {
                putSelectedSpellChecker(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            }
            putSelectedSpellCheckerSubtype(0);
        }

        /* access modifiers changed from: private */
        public void initializeTextServicesData() {
            this.mSpellCheckerList.clear();
            this.mSpellCheckerMap.clear();
            this.mUpdateCount++;
            List<ResolveInfo> services = this.mContext.getPackageManager().queryIntentServicesAsUser(new Intent("android.service.textservice.SpellCheckerService"), 128, this.mUserId);
            int N = services.size();
            for (int i = 0; i < N; i++) {
                ResolveInfo ri = services.get(i);
                ServiceInfo si = ri.serviceInfo;
                new ComponentName(si.packageName, si.name);
                if (!"android.permission.BIND_TEXT_SERVICE".equals(si.permission)) {
                    Slog.w(TextServicesManagerService.TAG, "Skipping text service " + compName + ": it does not require the permission " + "android.permission.BIND_TEXT_SERVICE");
                } else {
                    try {
                        SpellCheckerInfo sci = new SpellCheckerInfo(this.mContext, ri);
                        if (sci.getSubtypeCount() <= 0) {
                            Slog.w(TextServicesManagerService.TAG, "Skipping text service " + compName + ": it does not contain subtypes.");
                        } else {
                            this.mSpellCheckerList.add(sci);
                            this.mSpellCheckerMap.put(sci.getId(), sci);
                        }
                    } catch (XmlPullParserException e) {
                        Slog.w(TextServicesManagerService.TAG, "Unable to load the spell checker " + compName, e);
                    } catch (IOException e2) {
                        Slog.w(TextServicesManagerService.TAG, "Unable to load the spell checker " + compName, e2);
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        public void dump(PrintWriter pw) {
            int spellCheckerIndex = 0;
            pw.println("  User #" + this.mUserId);
            pw.println("  Spell Checkers:");
            pw.println("  Spell Checkers: mUpdateCount=" + this.mUpdateCount);
            for (SpellCheckerInfo info : this.mSpellCheckerMap.values()) {
                pw.println("  Spell Checker #" + spellCheckerIndex);
                info.dump(pw, "    ");
                spellCheckerIndex++;
            }
            pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            pw.println("  Spell Checker Bind Groups:");
            for (Map.Entry<String, SpellCheckerBindGroup> ent : this.mSpellCheckerBindGroups.entrySet()) {
                SpellCheckerBindGroup grp = ent.getValue();
                pw.println("    " + ent.getKey() + " " + grp + ":");
                StringBuilder sb = new StringBuilder();
                sb.append("      mInternalConnection=");
                sb.append(grp.mInternalConnection);
                pw.println(sb.toString());
                pw.println("      mSpellChecker=" + grp.mSpellChecker);
                pw.println("      mUnbindCalled=" + grp.mUnbindCalled);
                pw.println("      mConnected=" + grp.mConnected);
                int numPendingSessionRequests = grp.mPendingSessionRequests.size();
                for (int j = 0; j < numPendingSessionRequests; j++) {
                    SessionRequest req = (SessionRequest) grp.mPendingSessionRequests.get(j);
                    pw.println("      Pending Request #" + j + ":");
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("        mTsListener=");
                    sb2.append(req.mTsListener);
                    pw.println(sb2.toString());
                    pw.println("        mScListener=" + req.mScListener);
                    pw.println("        mScLocale=" + req.mLocale + " mUid=" + req.mUid);
                }
                int numOnGoingSessionRequests = grp.mOnGoingSessionRequests.size();
                for (int j2 = 0; j2 < numOnGoingSessionRequests; j2 = j2 + 1 + 1) {
                    SessionRequest req2 = (SessionRequest) grp.mOnGoingSessionRequests.get(j2);
                    pw.println("      On going Request #" + j2 + ":");
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append("        mTsListener=");
                    sb3.append(req2.mTsListener);
                    pw.println(sb3.toString());
                    pw.println("        mScListener=" + req2.mScListener);
                    pw.println("        mScLocale=" + req2.mLocale + " mUid=" + req2.mUid);
                }
                int N = grp.mListeners.getRegisteredCallbackCount();
                for (int j3 = 0; j3 < N; j3++) {
                    pw.println("      Listener #" + j3 + ":");
                    StringBuilder sb4 = new StringBuilder();
                    sb4.append("        mScListener=");
                    sb4.append(grp.mListeners.getRegisteredCallbackItem(j3));
                    pw.println(sb4.toString());
                    pw.println("        mGroup=" + grp);
                }
            }
        }
    }

    private final class TextServicesMonitor extends PackageMonitor {
        private TextServicesMonitor() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0066, code lost:
            return;
         */
        public void onSomePackagesChanged() {
            int userId = getChangingUserId();
            synchronized (TextServicesManagerService.this.mLock) {
                TextServicesData tsd = (TextServicesData) TextServicesManagerService.this.mUserData.get(userId);
                if (tsd != null) {
                    SpellCheckerInfo sci = tsd.getCurrentSpellChecker();
                    tsd.initializeTextServicesData();
                    if (tsd.isSpellCheckerEnabled()) {
                        if (sci == null) {
                            TextServicesManagerService.this.setCurrentSpellCheckerLocked(TextServicesManagerService.this.findAvailSystemSpellCheckerLocked(null, tsd), tsd);
                        } else {
                            String packageName = sci.getPackageName();
                            int change = isPackageDisappearing(packageName);
                            if (change == 3 || change == 2) {
                                SpellCheckerInfo availSci = TextServicesManagerService.this.findAvailSystemSpellCheckerLocked(packageName, tsd);
                                if (availSci == null || (availSci != null && !availSci.getId().equals(sci.getId()))) {
                                    TextServicesManagerService.this.setCurrentSpellCheckerLocked(availSci, tsd);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onStopUser(int userId) {
        synchronized (this.mLock) {
            this.mSpellCheckerOwnerUserIdMap.delete(userId);
            TextServicesData tsd = this.mUserData.get(userId);
            if (tsd != null) {
                unbindServiceLocked(tsd);
                this.mUserData.remove(userId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onUnlockUser(int userId) {
        synchronized (this.mLock) {
            initializeInternalStateLocked(userId);
        }
    }

    public TextServicesManagerService(Context context) {
        this.mContext = context;
        this.mUserManager = (UserManager) this.mContext.getSystemService(UserManager.class);
        this.mSpellCheckerOwnerUserIdMap = new LazyIntToIntMap(new IntUnaryOperator() {
            public final int applyAsInt(int i) {
                return TextServicesManagerService.lambda$new$0(TextServicesManagerService.this, i);
            }
        });
        this.mMonitor = new TextServicesMonitor();
        this.mMonitor.register(context, null, UserHandle.ALL, true);
    }

    public static /* synthetic */ int lambda$new$0(TextServicesManagerService textServicesManagerService, int callingUserId) {
        int i;
        long token = Binder.clearCallingIdentity();
        try {
            UserInfo parent = textServicesManagerService.mUserManager.getProfileParent(callingUserId);
            if (parent != null) {
                i = parent.id;
            } else {
                i = callingUserId;
            }
            return i;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void initializeInternalStateLocked(int userId) {
        if (userId == this.mSpellCheckerOwnerUserIdMap.get(userId)) {
            TextServicesData tsd = this.mUserData.get(userId);
            if (tsd == null) {
                tsd = new TextServicesData(userId, this.mContext);
                this.mUserData.put(userId, tsd);
            }
            tsd.initializeTextServicesData();
            if (tsd.getCurrentSpellChecker() == null) {
                setCurrentSpellCheckerLocked(findAvailSystemSpellCheckerLocked(null, tsd), tsd);
            }
        }
    }

    private boolean bindCurrentSpellCheckerService(Intent service, ServiceConnection conn, int flags, int userId) {
        if (service != null && conn != null) {
            return this.mContext.bindServiceAsUser(service, conn, flags, UserHandle.of(userId));
        }
        String str = TAG;
        Slog.e(str, "--- bind failed: service = " + service + ", conn = " + conn + ", userId =" + userId);
        return false;
    }

    private void unbindServiceLocked(TextServicesData tsd) {
        HashMap<String, SpellCheckerBindGroup> spellCheckerBindGroups = tsd.mSpellCheckerBindGroups;
        for (SpellCheckerBindGroup scbg : spellCheckerBindGroups.values()) {
            scbg.removeAllLocked();
        }
        spellCheckerBindGroups.clear();
    }

    /* access modifiers changed from: private */
    public SpellCheckerInfo findAvailSystemSpellCheckerLocked(String prefPackage, TextServicesData tsd) {
        String str = prefPackage;
        ArrayList<SpellCheckerInfo> spellCheckerList = new ArrayList<>();
        Iterator it = tsd.mSpellCheckerList.iterator();
        while (it.hasNext()) {
            SpellCheckerInfo sci = (SpellCheckerInfo) it.next();
            if ((1 & sci.getServiceInfo().applicationInfo.flags) != 0) {
                spellCheckerList.add(sci);
            }
        }
        int spellCheckersCount = spellCheckerList.size();
        if (spellCheckersCount == 0) {
            Slog.w(TAG, "no available spell checker services found");
            return null;
        }
        if (str != null) {
            for (int i = 0; i < spellCheckersCount; i++) {
                SpellCheckerInfo sci2 = spellCheckerList.get(i);
                if (str.equals(sci2.getPackageName())) {
                    return sci2;
                }
            }
        }
        ArrayList<Locale> suitableLocales = InputMethodUtils.getSuitableLocalesForSpellChecker(this.mContext.getResources().getConfiguration().locale);
        int localeCount = suitableLocales.size();
        for (int localeIndex = 0; localeIndex < localeCount; localeIndex++) {
            Locale locale = suitableLocales.get(localeIndex);
            for (int spellCheckersIndex = 0; spellCheckersIndex < spellCheckersCount; spellCheckersIndex++) {
                SpellCheckerInfo info = spellCheckerList.get(spellCheckersIndex);
                int subtypeCount = info.getSubtypeCount();
                for (int subtypeIndex = 0; subtypeIndex < subtypeCount; subtypeIndex++) {
                    if (locale.equals(InputMethodUtils.constructLocaleFromString(info.getSubtypeAt(subtypeIndex).getLocale()))) {
                        return info;
                    }
                }
            }
        }
        if (spellCheckersCount > 1) {
            Slog.w(TAG, "more than one spell checker service found, picking first");
        }
        return spellCheckerList.get(0);
    }

    public SpellCheckerInfo getCurrentSpellChecker(String locale) {
        int userId = UserHandle.getCallingUserId();
        synchronized (this.mLock) {
            TextServicesData tsd = getDataFromCallingUserIdLocked(userId);
            if (tsd == null) {
                return null;
            }
            SpellCheckerInfo currentSpellChecker = tsd.getCurrentSpellChecker();
            return currentSpellChecker;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002e, code lost:
        if (r6.getSubtypeCount() != 0) goto L_0x0032;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0032, code lost:
        if (r5 != 0) goto L_0x0037;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0034, code lost:
        if (r13 != false) goto L_0x0037;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0036, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0037, code lost:
        r1 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0038, code lost:
        if (r5 != 0) goto L_0x0060;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003a, code lost:
        r7 = com.android.internal.view.IInputMethodManager.Stub.asInterface(android.os.ServiceManager.getService("input_method"));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0044, code lost:
        if (r7 == null) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        r8 = r7.getCurrentInputMethodSubtype();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004b, code lost:
        if (r8 == null) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004d, code lost:
        r9 = r8.getLocale();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0055, code lost:
        if (android.text.TextUtils.isEmpty(r9) != false) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0057, code lost:
        r1 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x009b, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0028, code lost:
        if (r6 == null) goto L_0x009b;
     */
    public SpellCheckerSubtype getCurrentSpellCheckerSubtype(String locale, boolean allowImplicitlySelectedSubtype) {
        int i;
        int subtypeHashCode;
        SpellCheckerInfo sci;
        Locale systemLocale;
        String candidateLocale;
        int userId = UserHandle.getCallingUserId();
        synchronized (this.mLock) {
            TextServicesData tsd = getDataFromCallingUserIdLocked(userId);
            if (tsd == null) {
                return null;
            }
            subtypeHashCode = tsd.getSelectedSpellCheckerSubtype(0);
            sci = tsd.getCurrentSpellChecker();
            systemLocale = this.mContext.getResources().getConfiguration().locale;
        }
        if (candidateLocale == null) {
            candidateLocale = systemLocale.toString();
        }
        SpellCheckerSubtype candidate = null;
        for (i = 0; i < sci.getSubtypeCount(); i++) {
            SpellCheckerSubtype scs = sci.getSubtypeAt(i);
            if (subtypeHashCode == 0) {
                String scsLocale = scs.getLocale();
                if (candidateLocale.equals(scsLocale)) {
                    return scs;
                }
                if (candidate == null && candidateLocale.length() >= 2 && scsLocale.length() >= 2 && candidateLocale.startsWith(scsLocale)) {
                    candidate = scs;
                }
            } else if (scs.hashCode() == subtypeHashCode) {
                return scs;
            }
        }
        return candidate;
    }

    public void getSpellCheckerService(String sciId, String locale, ITextServicesSessionListener tsListener, ISpellCheckerSessionListener scListener, Bundle bundle) {
        String str = sciId;
        if (TextUtils.isEmpty(sciId) || tsListener == null || scListener == null) {
            Slog.e(TAG, "getSpellCheckerService: Invalid input.");
            return;
        }
        int callingUserId = UserHandle.getCallingUserId();
        synchronized (this.mLock) {
            TextServicesData tsd = getDataFromCallingUserIdLocked(callingUserId);
            if (tsd != null) {
                HashMap access$1600 = tsd.mSpellCheckerMap;
                if (access$1600.containsKey(str)) {
                    SpellCheckerInfo sci = (SpellCheckerInfo) access$1600.get(str);
                    HashMap access$1400 = tsd.mSpellCheckerBindGroups;
                    SpellCheckerBindGroup bindGroup = (SpellCheckerBindGroup) access$1400.get(str);
                    int uid = Binder.getCallingUid();
                    if (bindGroup == null) {
                        long ident = Binder.clearCallingIdentity();
                        try {
                            bindGroup = startSpellCheckerServiceInnerLocked(sci, tsd);
                            Binder.restoreCallingIdentity(ident);
                            if (bindGroup == null) {
                                return;
                            }
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(ident);
                            throw th;
                        }
                    }
                    SpellCheckerBindGroup bindGroup2 = bindGroup;
                    SessionRequest sessionRequest = r3;
                    HashMap hashMap = access$1400;
                    SessionRequest sessionRequest2 = new SessionRequest(uid, locale, tsListener, scListener, bundle);
                    bindGroup2.getISpellCheckerSessionOrQueueLocked(sessionRequest);
                }
            }
        }
    }

    public boolean isSpellCheckerEnabled() {
        int userId = UserHandle.getCallingUserId();
        synchronized (this.mLock) {
            TextServicesData tsd = getDataFromCallingUserIdLocked(userId);
            if (tsd == null) {
                return false;
            }
            boolean isSpellCheckerEnabled = tsd.isSpellCheckerEnabled();
            return isSpellCheckerEnabled;
        }
    }

    private SpellCheckerBindGroup startSpellCheckerServiceInnerLocked(SpellCheckerInfo info, TextServicesData tsd) {
        String sciId = info.getId();
        InternalServiceConnection connection = new InternalServiceConnection(sciId, tsd.mSpellCheckerBindGroups);
        Intent serviceIntent = new Intent("android.service.textservice.SpellCheckerService");
        serviceIntent.setComponent(info.getComponent());
        if (!bindCurrentSpellCheckerService(serviceIntent, connection, 8388609, tsd.mUserId)) {
            Slog.e(TAG, "Failed to get a spell checker service.");
            return null;
        }
        SpellCheckerBindGroup group = new SpellCheckerBindGroup(connection);
        tsd.mSpellCheckerBindGroups.put(sciId, group);
        return group;
    }

    public SpellCheckerInfo[] getEnabledSpellCheckers() {
        int callingUserId = UserHandle.getCallingUserId();
        synchronized (this.mLock) {
            TextServicesData tsd = getDataFromCallingUserIdLocked(callingUserId);
            if (tsd == null) {
                return null;
            }
            ArrayList<SpellCheckerInfo> spellCheckerList = tsd.mSpellCheckerList;
            SpellCheckerInfo[] spellCheckerInfoArr = (SpellCheckerInfo[]) spellCheckerList.toArray(new SpellCheckerInfo[spellCheckerList.size()]);
            return spellCheckerInfoArr;
        }
    }

    public void finishSpellCheckerService(ISpellCheckerSessionListener listener) {
        int userId = UserHandle.getCallingUserId();
        synchronized (this.mLock) {
            TextServicesData tsd = getDataFromCallingUserIdLocked(userId);
            if (tsd != null) {
                ArrayList<SpellCheckerBindGroup> removeList = new ArrayList<>();
                for (SpellCheckerBindGroup group : tsd.mSpellCheckerBindGroups.values()) {
                    if (group != null) {
                        removeList.add(group);
                    }
                }
                int removeSize = removeList.size();
                for (int i = 0; i < removeSize; i++) {
                    removeList.get(i).removeListener(listener);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void setCurrentSpellCheckerLocked(SpellCheckerInfo sci, TextServicesData tsd) {
        if (sci != null) {
            String id = sci.getId();
        }
        long ident = Binder.clearCallingIdentity();
        try {
            tsd.setCurrentSpellChecker(sci);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            if (args.length == 0 || (args.length == 1 && args[0].equals("-a"))) {
                synchronized (this.mLock) {
                    pw.println("Current Text Services Manager state:");
                    pw.println("  Users:");
                    int numOfUsers = this.mUserData.size();
                    for (int i = 0; i < numOfUsers; i++) {
                        this.mUserData.valueAt(i).dump(pw);
                    }
                }
            } else if (args.length != 2 || !args[0].equals("--user")) {
                pw.println("Invalid arguments to text services.");
            } else {
                int userId = Integer.parseInt(args[1]);
                if (this.mUserManager.getUserInfo(userId) == null) {
                    pw.println("Non-existent user.");
                    return;
                }
                TextServicesData tsd = this.mUserData.get(userId);
                if (tsd == null) {
                    pw.println("User needs to unlock first.");
                    return;
                }
                synchronized (this.mLock) {
                    pw.println("Current Text Services Manager state:");
                    pw.println("  User " + userId + ":");
                    tsd.dump(pw);
                }
            }
        }
    }

    private TextServicesData getDataFromCallingUserIdLocked(int callingUserId) {
        int spellCheckerOwnerUserId = this.mSpellCheckerOwnerUserIdMap.get(callingUserId);
        TextServicesData data = this.mUserData.get(spellCheckerOwnerUserId);
        if (spellCheckerOwnerUserId != callingUserId) {
            if (data == null) {
                return null;
            }
            SpellCheckerInfo info = data.getCurrentSpellChecker();
            if (info == null || (info.getServiceInfo().applicationInfo.flags & 1) == 0) {
                return null;
            }
            return data;
        }
        return data;
    }
}

package com.android.server.textservices;

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
import android.view.inputmethod.InputMethodSystemProperty;
import android.view.textservice.SpellCheckerInfo;
import android.view.textservice.SpellCheckerSubtype;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.inputmethod.SubtypeLocaleUtils;
import com.android.internal.textservice.ISpellCheckerService;
import com.android.internal.textservice.ISpellCheckerServiceCallback;
import com.android.internal.textservice.ISpellCheckerSession;
import com.android.internal.textservice.ISpellCheckerSessionListener;
import com.android.internal.textservice.ITextServicesManager;
import com.android.internal.textservice.ITextServicesSessionListener;
import com.android.internal.util.DumpUtils;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.textservices.TextServicesManagerService;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import org.xmlpull.v1.XmlPullParserException;

public class TextServicesManagerService extends ITextServicesManager.Stub {
    private static final boolean DBG = false;
    private static final String TAG = TextServicesManagerService.class.getSimpleName();
    private final Context mContext;
    private final Object mLock = new Object();
    private final TextServicesMonitor mMonitor;
    @GuardedBy({"mLock"})
    private final LazyIntToIntMap mSpellCheckerOwnerUserIdMap;
    private final SparseArray<TextServicesData> mUserData = new SparseArray<>();
    private final UserManager mUserManager;

    /* access modifiers changed from: private */
    public static class TextServicesData {
        private final Context mContext;
        private final ContentResolver mResolver;
        private final HashMap<String, SpellCheckerBindGroup> mSpellCheckerBindGroups;
        private final ArrayList<SpellCheckerInfo> mSpellCheckerList;
        private final HashMap<String, SpellCheckerInfo> mSpellCheckerMap;
        public int mUpdateCount = 0;
        private final int mUserId;

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
            return getInt(key, defaultValue ? 1 : 0) == 1;
        }

        private void putSelectedSpellChecker(String sciId) {
            putString("selected_spell_checker", sciId);
        }

        private void putSelectedSpellCheckerSubtype(int hashCode) {
            putInt("selected_spell_checker_subtype", hashCode);
        }

        private String getSelectedSpellChecker() {
            return getString("selected_spell_checker", "");
        }

        public int getSelectedSpellCheckerSubtype(int defaultValue) {
            return getInt("selected_spell_checker_subtype", defaultValue);
        }

        public boolean isSpellCheckerEnabled() {
            return getBoolean("spell_checker_enabled", true);
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
                putSelectedSpellChecker("");
            }
            putSelectedSpellCheckerSubtype(0);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void initializeTextServicesData() {
            this.mSpellCheckerList.clear();
            this.mSpellCheckerMap.clear();
            this.mUpdateCount++;
            List<ResolveInfo> services = this.mContext.getPackageManager().queryIntentServicesAsUser(new Intent("android.service.textservice.SpellCheckerService"), 128, this.mUserId);
            int N = services.size();
            for (int i = 0; i < N; i++) {
                ResolveInfo ri = services.get(i);
                ServiceInfo si = ri.serviceInfo;
                ComponentName compName = new ComponentName(si.packageName, si.name);
                if (!"android.permission.BIND_TEXT_SERVICE".equals(si.permission)) {
                    Slog.w(TextServicesManagerService.TAG, "Skipping text service " + compName + ": it does not require the permission android.permission.BIND_TEXT_SERVICE");
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
        /* access modifiers changed from: public */
        private void dump(PrintWriter pw) {
            int spellCheckerIndex = 0;
            pw.println("  User #" + this.mUserId);
            pw.println("  Spell Checkers:");
            pw.println("  Spell Checkers: mUpdateCount=" + this.mUpdateCount);
            for (SpellCheckerInfo info : this.mSpellCheckerMap.values()) {
                pw.println("  Spell Checker #" + spellCheckerIndex);
                info.dump(pw, "    ");
                spellCheckerIndex++;
            }
            pw.println("");
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
                int j = 0;
                while (j < numPendingSessionRequests) {
                    SessionRequest req = (SessionRequest) grp.mPendingSessionRequests.get(j);
                    pw.println("      Pending Request #" + j + ":");
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("        mTsListener=");
                    sb2.append(req.mTsListener);
                    pw.println(sb2.toString());
                    pw.println("        mScListener=" + req.mScListener);
                    pw.println("        mScLocale=" + req.mLocale + " mUid=" + req.mUid);
                    j++;
                    spellCheckerIndex = spellCheckerIndex;
                }
                int j2 = 0;
                for (int numOnGoingSessionRequests = grp.mOnGoingSessionRequests.size(); j2 < numOnGoingSessionRequests; numOnGoingSessionRequests = numOnGoingSessionRequests) {
                    SessionRequest req2 = (SessionRequest) grp.mOnGoingSessionRequests.get(j2);
                    pw.println("      On going Request #" + j2 + ":");
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append("        mTsListener=");
                    sb3.append(req2.mTsListener);
                    pw.println(sb3.toString());
                    pw.println("        mScListener=" + req2.mScListener);
                    pw.println("        mScLocale=" + req2.mLocale + " mUid=" + req2.mUid);
                    j2++;
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
                spellCheckerIndex = spellCheckerIndex;
            }
        }
    }

    public static final class Lifecycle extends SystemService {
        private TextServicesManagerService mService;

        public Lifecycle(Context context) {
            super(context);
            this.mService = new TextServicesManagerService(context);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.textservices.TextServicesManagerService$Lifecycle */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r0v1, types: [com.android.server.textservices.TextServicesManagerService, android.os.IBinder] */
        /* JADX WARNING: Unknown variable types count: 1 */
        @Override // com.android.server.SystemService
        public void onStart() {
            LocalServices.addService(TextServicesManagerInternal.class, new TextServicesManagerInternal() {
                /* class com.android.server.textservices.TextServicesManagerService.Lifecycle.AnonymousClass1 */

                @Override // com.android.server.textservices.TextServicesManagerInternal
                public SpellCheckerInfo getCurrentSpellCheckerForUser(int userId) {
                    return Lifecycle.this.mService.getCurrentSpellCheckerForUser(userId);
                }
            });
            publishBinderService("textservices", this.mService);
        }

        @Override // com.android.server.SystemService
        public void onStopUser(int userHandle) {
            this.mService.onStopUser(userHandle);
        }

        @Override // com.android.server.SystemService
        public void onUnlockUser(int userHandle) {
            this.mService.onUnlockUser(userHandle);
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
            /* class com.android.server.textservices.$$Lambda$TextServicesManagerService$otJ1a5fe5mYJvLrIEr3o1Ia1kSo */

            @Override // java.util.function.IntUnaryOperator
            public final int applyAsInt(int i) {
                return TextServicesManagerService.this.lambda$new$0$TextServicesManagerService(i);
            }
        });
        this.mMonitor = new TextServicesMonitor();
        this.mMonitor.register(context, null, UserHandle.ALL, true);
    }

    public /* synthetic */ int lambda$new$0$TextServicesManagerService(int callingUserId) {
        if (InputMethodSystemProperty.PER_PROFILE_IME_ENABLED) {
            return callingUserId;
        }
        long token = Binder.clearCallingIdentity();
        try {
            UserInfo parent = this.mUserManager.getProfileParent(callingUserId);
            return parent != null ? parent.id : callingUserId;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @GuardedBy({"mLock"})
    private void initializeInternalStateLocked(int userId) {
        if (InputMethodSystemProperty.PER_PROFILE_IME_ENABLED || userId == this.mSpellCheckerOwnerUserIdMap.get(userId)) {
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

    private final class TextServicesMonitor extends PackageMonitor {
        private TextServicesMonitor() {
        }

        public void onSomePackagesChanged() {
            SpellCheckerInfo availSci;
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
                            if ((change == 3 || change == 2) && ((availSci = TextServicesManagerService.this.findAvailSystemSpellCheckerLocked(packageName, tsd)) == null || !availSci.getId().equals(sci.getId()))) {
                                TextServicesManagerService.this.setCurrentSpellCheckerLocked(availSci, tsd);
                            }
                        }
                    }
                }
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
    /* access modifiers changed from: public */
    private SpellCheckerInfo findAvailSystemSpellCheckerLocked(String prefPackage, TextServicesData tsd) {
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
        if (prefPackage != null) {
            for (int i = 0; i < spellCheckersCount; i++) {
                SpellCheckerInfo sci2 = spellCheckerList.get(i);
                if (prefPackage.equals(sci2.getPackageName())) {
                    return sci2;
                }
            }
        }
        ArrayList<Locale> suitableLocales = LocaleUtils.getSuitableLocalesForSpellChecker(this.mContext.getResources().getConfiguration().locale);
        int localeCount = suitableLocales.size();
        for (int localeIndex = 0; localeIndex < localeCount; localeIndex++) {
            Locale locale = suitableLocales.get(localeIndex);
            for (int spellCheckersIndex = 0; spellCheckersIndex < spellCheckersCount; spellCheckersIndex++) {
                SpellCheckerInfo info = spellCheckerList.get(spellCheckersIndex);
                int subtypeCount = info.getSubtypeCount();
                for (int subtypeIndex = 0; subtypeIndex < subtypeCount; subtypeIndex++) {
                    if (locale.equals(SubtypeLocaleUtils.constructLocaleFromString(info.getSubtypeAt(subtypeIndex).getLocale()))) {
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private SpellCheckerInfo getCurrentSpellCheckerForUser(int userId) {
        SpellCheckerInfo currentSpellChecker;
        synchronized (this.mLock) {
            TextServicesData data = this.mUserData.get(this.mSpellCheckerOwnerUserIdMap.get(userId));
            currentSpellChecker = data != null ? data.getCurrentSpellChecker() : null;
        }
        return currentSpellChecker;
    }

    public SpellCheckerInfo getCurrentSpellChecker(int userId, String locale) {
        verifyUser(userId);
        synchronized (this.mLock) {
            TextServicesData tsd = getDataFromCallingUserIdLocked(userId);
            if (tsd == null) {
                return null;
            }
            return tsd.getCurrentSpellChecker();
        }
    }

    public SpellCheckerSubtype getCurrentSpellCheckerSubtype(int userId, boolean allowImplicitlySelectedSubtype) {
        int subtypeHashCode;
        SpellCheckerInfo sci;
        Locale systemLocale;
        verifyUser(userId);
        synchronized (this.mLock) {
            TextServicesData tsd = getDataFromCallingUserIdLocked(userId);
            if (tsd == null) {
                return null;
            }
            subtypeHashCode = tsd.getSelectedSpellCheckerSubtype(0);
            sci = tsd.getCurrentSpellChecker();
            systemLocale = this.mContext.getResources().getConfiguration().locale;
        }
        if (sci == null || sci.getSubtypeCount() == 0) {
            return null;
        }
        if (subtypeHashCode == 0 && !allowImplicitlySelectedSubtype) {
            return null;
        }
        int numSubtypes = sci.getSubtypeCount();
        if (subtypeHashCode != 0) {
            for (int i = 0; i < numSubtypes; i++) {
                SpellCheckerSubtype scs = sci.getSubtypeAt(i);
                if (scs.hashCode() == subtypeHashCode) {
                    return scs;
                }
            }
            return null;
        } else if (systemLocale == null) {
            return null;
        } else {
            SpellCheckerSubtype firstLanguageMatchingSubtype = null;
            for (int i2 = 0; i2 < sci.getSubtypeCount(); i2++) {
                SpellCheckerSubtype scs2 = sci.getSubtypeAt(i2);
                Locale scsLocale = scs2.getLocaleObject();
                if (Objects.equals(scsLocale, systemLocale)) {
                    return scs2;
                }
                if (firstLanguageMatchingSubtype == null && scsLocale != null && TextUtils.equals(systemLocale.getLanguage(), scsLocale.getLanguage())) {
                    firstLanguageMatchingSubtype = scs2;
                }
            }
            return firstLanguageMatchingSubtype;
        }
    }

    public void getSpellCheckerService(int userId, String sciId, String locale, ITextServicesSessionListener tsListener, ISpellCheckerSessionListener scListener, Bundle bundle) {
        SpellCheckerBindGroup bindGroup;
        verifyUser(userId);
        if (TextUtils.isEmpty(sciId) || tsListener == null || scListener == null) {
            Slog.e(TAG, "getSpellCheckerService: Invalid input.");
            return;
        }
        synchronized (this.mLock) {
            TextServicesData tsd = getDataFromCallingUserIdLocked(userId);
            if (tsd != null) {
                HashMap<String, SpellCheckerInfo> spellCheckerMap = tsd.mSpellCheckerMap;
                if (spellCheckerMap.containsKey(sciId)) {
                    SpellCheckerInfo sci = spellCheckerMap.get(sciId);
                    SpellCheckerBindGroup bindGroup2 = tsd.mSpellCheckerBindGroups.get(sciId);
                    int uid = Binder.getCallingUid();
                    if (bindGroup2 == null) {
                        long ident = Binder.clearCallingIdentity();
                        try {
                            SpellCheckerBindGroup bindGroup3 = startSpellCheckerServiceInnerLocked(sci, tsd);
                            if (bindGroup3 != null) {
                                bindGroup = bindGroup3;
                            } else {
                                return;
                            }
                        } finally {
                            Binder.restoreCallingIdentity(ident);
                        }
                    } else {
                        bindGroup = bindGroup2;
                    }
                    bindGroup.getISpellCheckerSessionOrQueueLocked(new SessionRequest(uid, locale, tsListener, scListener, bundle));
                }
            }
        }
    }

    public boolean isSpellCheckerEnabled(int userId) {
        verifyUser(userId);
        synchronized (this.mLock) {
            TextServicesData tsd = getDataFromCallingUserIdLocked(userId);
            if (tsd == null) {
                return false;
            }
            return tsd.isSpellCheckerEnabled();
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

    public SpellCheckerInfo[] getEnabledSpellCheckers(int userId) {
        verifyUser(userId);
        synchronized (this.mLock) {
            TextServicesData tsd = getDataFromCallingUserIdLocked(userId);
            if (tsd == null) {
                return null;
            }
            ArrayList<SpellCheckerInfo> spellCheckerList = tsd.mSpellCheckerList;
            return (SpellCheckerInfo[]) spellCheckerList.toArray(new SpellCheckerInfo[spellCheckerList.size()]);
        }
    }

    public void finishSpellCheckerService(int userId, ISpellCheckerSessionListener listener) {
        verifyUser(userId);
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

    private void verifyUser(int userId) {
        int callingUserId = UserHandle.getCallingUserId();
        if (userId != callingUserId) {
            Context context = this.mContext;
            context.enforceCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "Cross-user interaction requires INTERACT_ACROSS_USERS_FULL. userId=" + userId + " callingUserId=" + callingUserId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCurrentSpellCheckerLocked(SpellCheckerInfo sci, TextServicesData tsd) {
        if (sci != null) {
            sci.getId();
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

    @GuardedBy({"mLock"})
    private TextServicesData getDataFromCallingUserIdLocked(int callingUserId) {
        SpellCheckerInfo info;
        int spellCheckerOwnerUserId = this.mSpellCheckerOwnerUserIdMap.get(callingUserId);
        TextServicesData data = this.mUserData.get(spellCheckerOwnerUserId);
        if (InputMethodSystemProperty.PER_PROFILE_IME_ENABLED || spellCheckerOwnerUserId == callingUserId || (data != null && (info = data.getCurrentSpellChecker()) != null && (info.getServiceInfo().applicationInfo.flags & 1) != 0)) {
            return data;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public static final class SessionRequest {
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

    /* access modifiers changed from: private */
    public final class SpellCheckerBindGroup {
        private final String TAG = SpellCheckerBindGroup.class.getSimpleName();
        private boolean mConnected;
        private final InternalServiceConnection mInternalConnection;
        private final InternalDeathRecipients mListeners;
        private final ArrayList<SessionRequest> mOnGoingSessionRequests = new ArrayList<>();
        private final ArrayList<SessionRequest> mPendingSessionRequests = new ArrayList<>();
        private ISpellCheckerService mSpellChecker;
        HashMap<String, SpellCheckerBindGroup> mSpellCheckerBindGroups;
        private boolean mUnbindCalled;

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
                    /* class com.android.server.textservices.$$Lambda$TextServicesManagerService$SpellCheckerBindGroup$H2umvFNjpgILSC1ZJmUoLxzCdSk */
                    private final /* synthetic */ IBinder f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Predicate
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
                InternalDeathRecipients internalDeathRecipients = this.mListeners;
                internalDeathRecipients.unregister(internalDeathRecipients.getRegisteredCallbackItem(i));
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

    /* access modifiers changed from: private */
    public final class InternalServiceConnection implements ServiceConnection {
        private final String mSciId;
        private final HashMap<String, SpellCheckerBindGroup> mSpellCheckerBindGroups;

        public InternalServiceConnection(String id, HashMap<String, SpellCheckerBindGroup> spellCheckerBindGroups) {
            this.mSciId = id;
            this.mSpellCheckerBindGroups = spellCheckerBindGroups;
        }

        @Override // android.content.ServiceConnection
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

        @Override // android.content.ServiceConnection
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

    /* access modifiers changed from: private */
    public static final class InternalDeathRecipients extends RemoteCallbackList<ISpellCheckerSessionListener> {
        private final SpellCheckerBindGroup mGroup;

        public InternalDeathRecipients(SpellCheckerBindGroup group) {
            this.mGroup = group;
        }

        public void onCallbackDied(ISpellCheckerSessionListener listener) {
            this.mGroup.removeListener(listener);
        }
    }

    /* access modifiers changed from: private */
    public static final class ISpellCheckerServiceCallbackBinder extends ISpellCheckerServiceCallback.Stub {
        @GuardedBy({"mCallbackLock"})
        private WeakReference<SpellCheckerBindGroup> mBindGroup;
        private final Object mCallbackLock = new Object();
        @GuardedBy({"mCallbackLock"})
        private WeakReference<SessionRequest> mRequest;

        ISpellCheckerServiceCallbackBinder(SpellCheckerBindGroup bindGroup, SessionRequest request) {
            synchronized (this.mCallbackLock) {
                this.mBindGroup = new WeakReference<>(bindGroup);
                this.mRequest = new WeakReference<>(request);
            }
        }

        public void onSessionCreated(ISpellCheckerSession newSession) {
            SpellCheckerBindGroup group;
            SessionRequest request;
            synchronized (this.mCallbackLock) {
                if (this.mBindGroup != null) {
                    if (this.mRequest != null) {
                        group = this.mBindGroup.get();
                        request = this.mRequest.get();
                        this.mBindGroup = null;
                        this.mRequest = null;
                    }
                }
                return;
            }
            if (group != null && request != null) {
                group.onSessionCreated(newSession, request);
            }
        }
    }
}

package com.android.server;

import android.app.ActivityManagerNative;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Slog;
import android.view.inputmethod.InputMethodSubtype;
import android.view.textservice.SpellCheckerInfo;
import android.view.textservice.SpellCheckerSubtype;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.inputmethod.InputMethodUtils;
import com.android.internal.textservice.ISpellCheckerService;
import com.android.internal.textservice.ISpellCheckerSession;
import com.android.internal.textservice.ISpellCheckerSessionListener;
import com.android.internal.textservice.ITextServicesManager.Stub;
import com.android.internal.textservice.ITextServicesSessionListener;
import com.android.internal.view.IInputMethodManager;
import com.android.server.am.ProcessList;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import org.xmlpull.v1.XmlPullParserException;

public class TextServicesManagerService extends Stub {
    private static final boolean DBG = false;
    private static final String TAG = null;
    private final Context mContext;
    private final TextServicesMonitor mMonitor;
    private final TextServicesSettings mSettings;
    private final HashMap<String, SpellCheckerBindGroup> mSpellCheckerBindGroups;
    private final ArrayList<SpellCheckerInfo> mSpellCheckerList;
    private final HashMap<String, SpellCheckerInfo> mSpellCheckerMap;
    private boolean mSystemReady;
    private final UserManager mUserManager;

    private class InternalDeathRecipient implements DeathRecipient {
        public final Bundle mBundle;
        private final SpellCheckerBindGroup mGroup;
        public final ISpellCheckerSessionListener mScListener;
        public final String mScLocale;
        public final ITextServicesSessionListener mTsListener;
        public final int mUid;

        public InternalDeathRecipient(SpellCheckerBindGroup group, ITextServicesSessionListener tsListener, String scLocale, ISpellCheckerSessionListener scListener, int uid, Bundle bundle) {
            this.mTsListener = tsListener;
            this.mScListener = scListener;
            this.mScLocale = scLocale;
            this.mGroup = group;
            this.mUid = uid;
            this.mBundle = bundle;
        }

        public boolean hasSpellCheckerListener(ISpellCheckerSessionListener listener) {
            return listener.asBinder().equals(this.mScListener.asBinder());
        }

        public void binderDied() {
            this.mGroup.removeListener(this.mScListener);
        }
    }

    private class InternalServiceConnection implements ServiceConnection {
        private final Bundle mBundle;
        private final String mLocale;
        private final String mSciId;

        public InternalServiceConnection(String id, String locale, Bundle bundle) {
            this.mSciId = id;
            this.mLocale = locale;
            this.mBundle = bundle;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (TextServicesManagerService.this.mSpellCheckerMap) {
                onServiceConnectedInnerLocked(name, service);
            }
        }

        private void onServiceConnectedInnerLocked(ComponentName name, IBinder service) {
            ISpellCheckerService spellChecker = ISpellCheckerService.Stub.asInterface(service);
            SpellCheckerBindGroup group = (SpellCheckerBindGroup) TextServicesManagerService.this.mSpellCheckerBindGroups.get(this.mSciId);
            if (group != null && this == group.mInternalConnection) {
                group.onServiceConnected(spellChecker);
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (TextServicesManagerService.this.mSpellCheckerMap) {
                SpellCheckerBindGroup group = (SpellCheckerBindGroup) TextServicesManagerService.this.mSpellCheckerBindGroups.get(this.mSciId);
                if (group != null && this == group.mInternalConnection) {
                    TextServicesManagerService.this.mSpellCheckerBindGroups.remove(this.mSciId);
                }
            }
        }
    }

    public static final class Lifecycle extends SystemService {
        private TextServicesManagerService mService;

        public Lifecycle(Context context) {
            super(context);
            this.mService = new TextServicesManagerService(context);
        }

        public void onStart() {
            publishBinderService("textservices", this.mService);
        }

        public void onSwitchUser(int userHandle) {
            this.mService.onSwitchUser(userHandle);
        }

        public void onBootPhase(int phase) {
            if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
                this.mService.systemRunning();
            }
        }

        public void onUnlockUser(int userHandle) {
            this.mService.onUnlockUser(userHandle);
        }
    }

    private class SpellCheckerBindGroup {
        private final String TAG;
        public boolean mBound;
        public boolean mConnected;
        private final InternalServiceConnection mInternalConnection;
        private final CopyOnWriteArrayList<InternalDeathRecipient> mListeners;
        public ISpellCheckerService mSpellChecker;

        public SpellCheckerBindGroup(InternalServiceConnection connection, ITextServicesSessionListener listener, String locale, ISpellCheckerSessionListener scListener, int uid, Bundle bundle) {
            this.TAG = SpellCheckerBindGroup.class.getSimpleName();
            this.mListeners = new CopyOnWriteArrayList();
            this.mInternalConnection = connection;
            this.mBound = true;
            this.mConnected = false;
            addListener(listener, locale, scListener, uid, bundle);
        }

        public void onServiceConnected(ISpellCheckerService spellChecker) {
            for (InternalDeathRecipient listener : this.mListeners) {
                ISpellCheckerSession session = spellChecker.getISpellCheckerSession(listener.mScLocale, listener.mScListener, listener.mBundle);
                synchronized (TextServicesManagerService.this.mSpellCheckerMap) {
                    if (this.mListeners.contains(listener)) {
                        listener.mTsListener.onServiceConnected(session);
                    }
                    try {
                    } catch (RemoteException e) {
                        Slog.e(this.TAG, "Exception in getting the spell checker session.Reconnect to the spellchecker. ", e);
                        removeAll();
                        return;
                    }
                }
            }
            synchronized (TextServicesManagerService.this.mSpellCheckerMap) {
                this.mSpellChecker = spellChecker;
                this.mConnected = true;
            }
        }

        public InternalDeathRecipient addListener(ITextServicesSessionListener tsListener, String locale, ISpellCheckerSessionListener scListener, int uid, Bundle bundle) {
            synchronized (TextServicesManagerService.this.mSpellCheckerMap) {
                InternalDeathRecipient recipient;
                try {
                    int size = this.mListeners.size();
                    for (int i = 0; i < size; i++) {
                        if (((InternalDeathRecipient) this.mListeners.get(i)).hasSpellCheckerListener(scListener)) {
                            return null;
                        }
                    }
                    recipient = new InternalDeathRecipient(this, tsListener, locale, scListener, uid, bundle);
                    try {
                        scListener.asBinder().linkToDeath(recipient, 0);
                        this.mListeners.add(recipient);
                    } catch (RemoteException e) {
                    }
                } catch (RemoteException e2) {
                    recipient = null;
                } catch (Throwable th) {
                    Throwable th2 = th;
                    recipient = null;
                    throw th2;
                }
                try {
                    cleanLocked();
                    return recipient;
                } catch (Throwable th3) {
                    th2 = th3;
                    throw th2;
                }
            }
        }

        public void removeListener(ISpellCheckerSessionListener listener) {
            synchronized (TextServicesManagerService.this.mSpellCheckerMap) {
                int i;
                int size = this.mListeners.size();
                ArrayList<InternalDeathRecipient> removeList = new ArrayList();
                for (i = 0; i < size; i++) {
                    InternalDeathRecipient tempRecipient = (InternalDeathRecipient) this.mListeners.get(i);
                    if (tempRecipient.hasSpellCheckerListener(listener)) {
                        removeList.add(tempRecipient);
                    }
                }
                int removeSize = removeList.size();
                for (i = 0; i < removeSize; i++) {
                    InternalDeathRecipient idr = (InternalDeathRecipient) removeList.get(i);
                    idr.mScListener.asBinder().unlinkToDeath(idr, 0);
                    this.mListeners.remove(idr);
                }
                cleanLocked();
            }
        }

        private void cleanLocked() {
            if (this.mBound && this.mListeners.isEmpty()) {
                this.mBound = false;
                String sciId = this.mInternalConnection.mSciId;
                if (((SpellCheckerBindGroup) TextServicesManagerService.this.mSpellCheckerBindGroups.get(sciId)) == this) {
                    TextServicesManagerService.this.mSpellCheckerBindGroups.remove(sciId);
                }
                TextServicesManagerService.this.mContext.unbindService(this.mInternalConnection);
            }
        }

        public void removeAll() {
            Slog.e(this.TAG, "Remove the spell checker bind unexpectedly.");
            synchronized (TextServicesManagerService.this.mSpellCheckerMap) {
                int size = this.mListeners.size();
                for (int i = 0; i < size; i++) {
                    InternalDeathRecipient idr = (InternalDeathRecipient) this.mListeners.get(i);
                    idr.mScListener.asBinder().unlinkToDeath(idr, 0);
                }
                this.mListeners.clear();
                cleanLocked();
            }
        }
    }

    class TextServicesBroadcastReceiver extends BroadcastReceiver {
        TextServicesBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.USER_ADDED".equals(action) || "android.intent.action.USER_REMOVED".equals(action)) {
                TextServicesManagerService.this.updateCurrentProfileIds();
            } else {
                Slog.w(TextServicesManagerService.TAG, "Unexpected intent " + intent);
            }
        }
    }

    private class TextServicesMonitor extends PackageMonitor {
        private TextServicesMonitor() {
        }

        private boolean isChangingPackagesOfCurrentUser() {
            return getChangingUserId() == TextServicesManagerService.this.mSettings.getCurrentUserId();
        }

        public void onSomePackagesChanged() {
            if (isChangingPackagesOfCurrentUser()) {
                synchronized (TextServicesManagerService.this.mSpellCheckerMap) {
                    TextServicesManagerService.buildSpellCheckerMapLocked(TextServicesManagerService.this.mContext, TextServicesManagerService.this.mSpellCheckerList, TextServicesManagerService.this.mSpellCheckerMap, TextServicesManagerService.this.mSettings);
                    SpellCheckerInfo sci = TextServicesManagerService.this.getCurrentSpellChecker(null);
                    if (sci == null) {
                        return;
                    }
                    String packageName = sci.getPackageName();
                    int change = isPackageDisappearing(packageName);
                    if (!(change == 3 || change == 2)) {
                        if (isPackageModified(packageName)) {
                        }
                    }
                    sci = TextServicesManagerService.this.findAvailSpellCheckerLocked(packageName);
                    if (sci != null) {
                        TextServicesManagerService.this.setCurrentSpellCheckerLocked(sci.getId());
                    }
                }
            }
        }
    }

    private static class TextServicesSettings {
        private static final String SUPPORT_CHECK_LANGUAGE = null;
        private boolean mCopyOnWrite;
        private final HashMap<String, String> mCopyOnWriteDataStore;
        @GuardedBy("mLock")
        private int[] mCurrentProfileIds;
        private int mCurrentUserId;
        private Object mLock;
        private final ContentResolver mResolver;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.TextServicesManagerService.TextServicesSettings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.TextServicesManagerService.TextServicesSettings.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.TextServicesManagerService.TextServicesSettings.<clinit>():void");
        }

        public TextServicesSettings(ContentResolver resolver, int userId, boolean copyOnWrite) {
            this.mCurrentProfileIds = new int[0];
            this.mLock = new Object();
            this.mCopyOnWriteDataStore = new HashMap();
            this.mCopyOnWrite = false;
            this.mResolver = resolver;
            switchCurrentUser(userId, copyOnWrite);
        }

        public void switchCurrentUser(int userId, boolean copyOnWrite) {
            if (!(this.mCurrentUserId == userId && this.mCopyOnWrite == copyOnWrite)) {
                this.mCopyOnWriteDataStore.clear();
            }
            this.mCurrentUserId = userId;
            this.mCopyOnWrite = copyOnWrite;
        }

        private void putString(String key, String str) {
            if (this.mCopyOnWrite) {
                this.mCopyOnWriteDataStore.put(key, str);
            } else {
                Secure.putStringForUser(this.mResolver, key, str, this.mCurrentUserId);
            }
        }

        private String getString(String key, String defaultValue) {
            String result;
            if (this.mCopyOnWrite && this.mCopyOnWriteDataStore.containsKey(key)) {
                result = (String) this.mCopyOnWriteDataStore.get(key);
            } else {
                result = Secure.getStringForUser(this.mResolver, key, this.mCurrentUserId);
            }
            if (result != null) {
                return result;
            }
            return defaultValue;
        }

        private void putInt(String key, int value) {
            if (this.mCopyOnWrite) {
                this.mCopyOnWriteDataStore.put(key, String.valueOf(value));
            } else {
                Secure.putIntForUser(this.mResolver, key, value, this.mCurrentUserId);
            }
        }

        private int getInt(String key, int defaultValue) {
            if (!this.mCopyOnWrite || !this.mCopyOnWriteDataStore.containsKey(key)) {
                return Secure.getIntForUser(this.mResolver, key, defaultValue, this.mCurrentUserId);
            }
            String result = (String) this.mCopyOnWriteDataStore.get(key);
            return result != null ? Integer.parseInt(result) : 0;
        }

        private void putBoolean(String key, boolean value) {
            putInt(key, value ? 1 : 0);
        }

        private boolean getBoolean(String key, boolean defaultValue) {
            return getInt(key, defaultValue ? 1 : 0) == 1;
        }

        public void setCurrentProfileIds(int[] currentProfileIds) {
            synchronized (this.mLock) {
                this.mCurrentProfileIds = currentProfileIds;
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean isCurrentProfile(int userId) {
            synchronized (this.mLock) {
                if (userId == this.mCurrentUserId) {
                    return true;
                }
                int i = 0;
                while (true) {
                    if (i >= this.mCurrentProfileIds.length) {
                        return false;
                    } else if (userId == this.mCurrentProfileIds[i]) {
                        return true;
                    } else {
                        i++;
                    }
                }
            }
        }

        public int getCurrentUserId() {
            return this.mCurrentUserId;
        }

        public void putSelectedSpellChecker(String sciId) {
            if (TextUtils.isEmpty(sciId)) {
                putString("selected_spell_checker", null);
            } else {
                putString("selected_spell_checker", sciId);
            }
        }

        public void putSelectedSpellCheckerSubtype(int hashCode) {
            putInt("selected_spell_checker_subtype", hashCode);
        }

        public void setSpellCheckerEnabled(boolean enabled) {
            putBoolean("spell_checker_enabled", enabled);
        }

        public String getSelectedSpellChecker() {
            return getString("selected_spell_checker", "");
        }

        public int getSelectedSpellCheckerSubtype(int defaultValue) {
            return getInt("selected_spell_checker_subtype", defaultValue);
        }

        public boolean isSpellCheckerEnabled() {
            boolean z = true;
            int spellCheckFlag = 0;
            if (SUPPORT_CHECK_LANGUAGE.contains(Locale.getDefault().getLanguage())) {
                spellCheckFlag = 1;
            }
            String str = "spell_checker_enabled";
            if (spellCheckFlag != 1) {
                z = false;
            }
            return getBoolean(str, z);
        }

        public void dumpLocked(PrintWriter pw, String prefix) {
            pw.println(prefix + "mCurrentUserId=" + this.mCurrentUserId);
            pw.println(prefix + "mCurrentProfileIds=" + Arrays.toString(this.mCurrentProfileIds));
            pw.println(prefix + "mCopyOnWrite=" + this.mCopyOnWrite);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.TextServicesManagerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.TextServicesManagerService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.TextServicesManagerService.<clinit>():void");
    }

    void systemRunning() {
        synchronized (this.mSpellCheckerMap) {
            if (!this.mSystemReady) {
                this.mSystemReady = true;
                resetInternalState(this.mSettings.getCurrentUserId());
            }
        }
    }

    void onSwitchUser(int userId) {
        synchronized (this.mSpellCheckerMap) {
            resetInternalState(userId);
        }
    }

    void onUnlockUser(int userId) {
        synchronized (this.mSpellCheckerMap) {
            int currentUserId = this.mSettings.getCurrentUserId();
            if (userId != currentUserId) {
                return;
            }
            resetInternalState(currentUserId);
        }
    }

    public TextServicesManagerService(Context context) {
        this.mSpellCheckerMap = new HashMap();
        this.mSpellCheckerList = new ArrayList();
        this.mSpellCheckerBindGroups = new HashMap();
        this.mSystemReady = false;
        this.mContext = context;
        this.mUserManager = (UserManager) this.mContext.getSystemService(UserManager.class);
        IntentFilter broadcastFilter = new IntentFilter();
        broadcastFilter.addAction("android.intent.action.USER_ADDED");
        broadcastFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiver(new TextServicesBroadcastReceiver(), broadcastFilter);
        int userId = 0;
        try {
            userId = ActivityManagerNative.getDefault().getCurrentUser().id;
        } catch (RemoteException e) {
            Slog.w(TAG, "Couldn't get current user ID; guessing it's 0", e);
        }
        this.mMonitor = new TextServicesMonitor();
        this.mMonitor.register(context, null, true);
        boolean useCopyOnWriteSettings = (this.mSystemReady && this.mUserManager.isUserUnlockingOrUnlocked(userId)) ? false : true;
        this.mSettings = new TextServicesSettings(context.getContentResolver(), userId, useCopyOnWriteSettings);
        resetInternalState(userId);
    }

    private void resetInternalState(int userId) {
        boolean useCopyOnWriteSettings = (this.mSystemReady && this.mUserManager.isUserUnlockingOrUnlocked(userId)) ? false : true;
        this.mSettings.switchCurrentUser(userId, useCopyOnWriteSettings);
        updateCurrentProfileIds();
        unbindServiceLocked();
        buildSpellCheckerMapLocked(this.mContext, this.mSpellCheckerList, this.mSpellCheckerMap, this.mSettings);
        if (getCurrentSpellChecker(null) == null) {
            SpellCheckerInfo sci = findAvailSpellCheckerLocked(null);
            if (sci != null) {
                setCurrentSpellCheckerLocked(sci.getId());
            }
        }
    }

    void updateCurrentProfileIds() {
        this.mSettings.setCurrentProfileIds(this.mUserManager.getProfileIdsWithDisabled(this.mSettings.getCurrentUserId()));
    }

    private static void buildSpellCheckerMapLocked(Context context, ArrayList<SpellCheckerInfo> list, HashMap<String, SpellCheckerInfo> map, TextServicesSettings settings) {
        list.clear();
        map.clear();
        List<ResolveInfo> services = context.getPackageManager().queryIntentServicesAsUser(new Intent("android.service.textservice.SpellCheckerService"), DumpState.DUMP_PACKAGES, settings.getCurrentUserId());
        int N = services.size();
        for (int i = 0; i < N; i++) {
            ResolveInfo ri = (ResolveInfo) services.get(i);
            ServiceInfo si = ri.serviceInfo;
            ComponentName compName = new ComponentName(si.packageName, si.name);
            if ("android.permission.BIND_TEXT_SERVICE".equals(si.permission)) {
                try {
                    SpellCheckerInfo sci = new SpellCheckerInfo(context, ri);
                    if (sci.getSubtypeCount() <= 0) {
                        Slog.w(TAG, "Skipping text service " + compName + ": it does not contain subtypes.");
                    } else {
                        list.add(sci);
                        map.put(sci.getId(), sci);
                    }
                } catch (XmlPullParserException e) {
                    Slog.w(TAG, "Unable to load the spell checker " + compName, e);
                } catch (IOException e2) {
                    Slog.w(TAG, "Unable to load the spell checker " + compName, e2);
                }
            } else {
                Slog.w(TAG, "Skipping text service " + compName + ": it does not require the permission " + "android.permission.BIND_TEXT_SERVICE");
            }
        }
    }

    private boolean calledFromValidUser() {
        int uid = Binder.getCallingUid();
        int userId = UserHandle.getUserId(uid);
        if (uid == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE || userId == this.mSettings.getCurrentUserId()) {
            return true;
        }
        boolean isCurrentProfile = this.mSettings.isCurrentProfile(userId);
        if (this.mSettings.isCurrentProfile(userId)) {
            SpellCheckerInfo spellCheckerInfo = getCurrentSpellCheckerWithoutVerification();
            if (spellCheckerInfo != null) {
                boolean isSystemSpellChecker;
                if ((spellCheckerInfo.getServiceInfo().applicationInfo.flags & 1) != 0) {
                    isSystemSpellChecker = true;
                } else {
                    isSystemSpellChecker = false;
                }
                if (isSystemSpellChecker) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean bindCurrentSpellCheckerService(Intent service, ServiceConnection conn, int flags) {
        if (service != null && conn != null) {
            return this.mContext.bindServiceAsUser(service, conn, flags, new UserHandle(this.mSettings.getCurrentUserId()));
        }
        Slog.e(TAG, "--- bind failed: service = " + service + ", conn = " + conn);
        return false;
    }

    private void unbindServiceLocked() {
        for (SpellCheckerBindGroup scbg : this.mSpellCheckerBindGroups.values()) {
            scbg.removeAll();
        }
        this.mSpellCheckerBindGroups.clear();
    }

    private SpellCheckerInfo findAvailSpellCheckerLocked(String prefPackage) {
        int spellCheckersCount = this.mSpellCheckerList.size();
        if (spellCheckersCount == 0) {
            Slog.w(TAG, "no available spell checker services found");
            return null;
        }
        if (prefPackage != null) {
            for (int i = 0; i < spellCheckersCount; i++) {
                SpellCheckerInfo sci = (SpellCheckerInfo) this.mSpellCheckerList.get(i);
                if (prefPackage.equals(sci.getPackageName())) {
                    return sci;
                }
            }
        }
        ArrayList<Locale> suitableLocales = InputMethodUtils.getSuitableLocalesForSpellChecker(this.mContext.getResources().getConfiguration().locale);
        int localeCount = suitableLocales.size();
        for (int localeIndex = 0; localeIndex < localeCount; localeIndex++) {
            Locale locale = (Locale) suitableLocales.get(localeIndex);
            for (int spellCheckersIndex = 0; spellCheckersIndex < spellCheckersCount; spellCheckersIndex++) {
                SpellCheckerInfo info = (SpellCheckerInfo) this.mSpellCheckerList.get(spellCheckersIndex);
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
        return (SpellCheckerInfo) this.mSpellCheckerList.get(0);
    }

    public SpellCheckerInfo getCurrentSpellChecker(String locale) {
        if (calledFromValidUser()) {
            return getCurrentSpellCheckerWithoutVerification();
        }
        return null;
    }

    private SpellCheckerInfo getCurrentSpellCheckerWithoutVerification() {
        synchronized (this.mSpellCheckerMap) {
            String curSpellCheckerId = this.mSettings.getSelectedSpellChecker();
            if (TextUtils.isEmpty(curSpellCheckerId)) {
                return null;
            }
            SpellCheckerInfo spellCheckerInfo = (SpellCheckerInfo) this.mSpellCheckerMap.get(curSpellCheckerId);
            return spellCheckerInfo;
        }
    }

    public SpellCheckerSubtype getCurrentSpellCheckerSubtype(String locale, boolean allowImplicitlySelectedSubtype) {
        if (!calledFromValidUser()) {
            return null;
        }
        synchronized (this.mSpellCheckerMap) {
            int subtypeHashCode = this.mSettings.getSelectedSpellCheckerSubtype(0);
            SpellCheckerInfo sci = getCurrentSpellChecker(null);
            if (sci == null || sci.getSubtypeCount() == 0) {
                return null;
            } else if (subtypeHashCode != 0 || allowImplicitlySelectedSubtype) {
                String str = null;
                if (subtypeHashCode == 0) {
                    IInputMethodManager imm = IInputMethodManager.Stub.asInterface(ServiceManager.getService("input_method"));
                    if (imm != null) {
                        try {
                            InputMethodSubtype currentInputMethodSubtype = imm.getCurrentInputMethodSubtype();
                            if (currentInputMethodSubtype != null) {
                                String localeString = currentInputMethodSubtype.getLocale();
                                if (!TextUtils.isEmpty(localeString)) {
                                    str = localeString;
                                }
                            }
                        } catch (RemoteException e) {
                        }
                    }
                    if (str == null) {
                        str = this.mContext.getResources().getConfiguration().locale.toString();
                    }
                }
                SpellCheckerSubtype candidate = null;
                for (int i = 0; i < sci.getSubtypeCount(); i++) {
                    SpellCheckerSubtype scs = sci.getSubtypeAt(i);
                    if (subtypeHashCode == 0) {
                        String scsLocale = scs.getLocale();
                        if (str.equals(scsLocale)) {
                            return scs;
                        } else if (candidate != null) {
                            continue;
                        } else if (str.length() >= 2 && scsLocale.length() >= 2 && str.startsWith(scsLocale)) {
                            candidate = scs;
                        }
                    } else if (scs.hashCode() == subtypeHashCode) {
                        return scs;
                    }
                }
                return candidate;
            } else {
                return null;
            }
        }
    }

    public void getSpellCheckerService(String sciId, String locale, ITextServicesSessionListener tsListener, ISpellCheckerSessionListener scListener, Bundle bundle) {
        if (!calledFromValidUser() || !this.mSystemReady) {
            return;
        }
        if (TextUtils.isEmpty(sciId) || tsListener == null || scListener == null) {
            Slog.e(TAG, "getSpellCheckerService: Invalid input.");
            return;
        }
        synchronized (this.mSpellCheckerMap) {
            if (this.mSpellCheckerMap.containsKey(sciId)) {
                SpellCheckerInfo sci = (SpellCheckerInfo) this.mSpellCheckerMap.get(sciId);
                int uid = Binder.getCallingUid();
                if (this.mSpellCheckerBindGroups.containsKey(sciId)) {
                    SpellCheckerBindGroup bindGroup = (SpellCheckerBindGroup) this.mSpellCheckerBindGroups.get(sciId);
                    if (bindGroup != null) {
                        InternalDeathRecipient recipient = ((SpellCheckerBindGroup) this.mSpellCheckerBindGroups.get(sciId)).addListener(tsListener, locale, scListener, uid, bundle);
                        if (recipient == null) {
                            return;
                        }
                        if (((bindGroup.mSpellChecker == null ? 1 : 0) & bindGroup.mConnected) != 0) {
                            Slog.e(TAG, "The state of the spell checker bind group is illegal.");
                            bindGroup.removeAll();
                        } else if (bindGroup.mSpellChecker != null) {
                            try {
                                ISpellCheckerSession session = bindGroup.mSpellChecker.getISpellCheckerSession(recipient.mScLocale, recipient.mScListener, bundle);
                                if (session != null) {
                                    tsListener.onServiceConnected(session);
                                    return;
                                }
                                bindGroup.removeAll();
                            } catch (RemoteException e) {
                                Slog.e(TAG, "Exception in getting spell checker session: " + e);
                                bindGroup.removeAll();
                            } catch (Throwable th) {
                                Binder.restoreCallingIdentity(ident);
                            }
                        }
                    }
                }
                long ident = Binder.clearCallingIdentity();
                startSpellCheckerServiceInnerLocked(sci, locale, tsListener, scListener, uid, bundle);
                Binder.restoreCallingIdentity(ident);
                return;
            }
        }
    }

    public boolean isSpellCheckerEnabled() {
        if (!calledFromValidUser()) {
            return false;
        }
        boolean isSpellCheckerEnabledLocked;
        synchronized (this.mSpellCheckerMap) {
            isSpellCheckerEnabledLocked = isSpellCheckerEnabledLocked();
        }
        return isSpellCheckerEnabledLocked;
    }

    private void startSpellCheckerServiceInnerLocked(SpellCheckerInfo info, String locale, ITextServicesSessionListener tsListener, ISpellCheckerSessionListener scListener, int uid, Bundle bundle) {
        String sciId = info.getId();
        InternalServiceConnection connection = new InternalServiceConnection(sciId, locale, bundle);
        Intent serviceIntent = new Intent("android.service.textservice.SpellCheckerService");
        serviceIntent.setComponent(info.getComponent());
        if (bindCurrentSpellCheckerService(serviceIntent, connection, 33554433)) {
            this.mSpellCheckerBindGroups.put(sciId, new SpellCheckerBindGroup(connection, tsListener, locale, scListener, uid, bundle));
            return;
        }
        Slog.e(TAG, "Failed to get a spell checker service.");
    }

    public SpellCheckerInfo[] getEnabledSpellCheckers() {
        if (calledFromValidUser()) {
            return (SpellCheckerInfo[]) this.mSpellCheckerList.toArray(new SpellCheckerInfo[this.mSpellCheckerList.size()]);
        }
        return null;
    }

    public void finishSpellCheckerService(ISpellCheckerSessionListener listener) {
        if (calledFromValidUser()) {
            synchronized (this.mSpellCheckerMap) {
                ArrayList<SpellCheckerBindGroup> removeList = new ArrayList();
                for (SpellCheckerBindGroup group : this.mSpellCheckerBindGroups.values()) {
                    if (group != null) {
                        removeList.add(group);
                    }
                }
                int removeSize = removeList.size();
                for (int i = 0; i < removeSize; i++) {
                    ((SpellCheckerBindGroup) removeList.get(i)).removeListener(listener);
                }
            }
        }
    }

    public void setCurrentSpellChecker(String locale, String sciId) {
        if (calledFromValidUser()) {
            synchronized (this.mSpellCheckerMap) {
                if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
                    throw new SecurityException("Requires permission android.permission.WRITE_SECURE_SETTINGS");
                }
                setCurrentSpellCheckerLocked(sciId);
            }
        }
    }

    public void setCurrentSpellCheckerSubtype(String locale, int hashCode) {
        if (calledFromValidUser()) {
            synchronized (this.mSpellCheckerMap) {
                if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
                    throw new SecurityException("Requires permission android.permission.WRITE_SECURE_SETTINGS");
                }
                setCurrentSpellCheckerSubtypeLocked(hashCode);
            }
        }
    }

    public void setSpellCheckerEnabled(boolean enabled) {
        if (calledFromValidUser()) {
            synchronized (this.mSpellCheckerMap) {
                if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
                    throw new SecurityException("Requires permission android.permission.WRITE_SECURE_SETTINGS");
                }
                setSpellCheckerEnabledLocked(enabled);
            }
        }
    }

    private void setCurrentSpellCheckerLocked(String sciId) {
        if (!TextUtils.isEmpty(sciId) && this.mSpellCheckerMap.containsKey(sciId)) {
            SpellCheckerInfo currentSci = getCurrentSpellChecker(null);
            if (currentSci == null || !currentSci.getId().equals(sciId)) {
                long ident = Binder.clearCallingIdentity();
                try {
                    this.mSettings.putSelectedSpellChecker(sciId);
                    setCurrentSpellCheckerSubtypeLocked(0);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }

    private void setCurrentSpellCheckerSubtypeLocked(int hashCode) {
        SpellCheckerInfo sci = getCurrentSpellChecker(null);
        int tempHashCode = 0;
        int i = 0;
        while (sci != null && i < sci.getSubtypeCount()) {
            if (sci.getSubtypeAt(i).hashCode() == hashCode) {
                tempHashCode = hashCode;
                break;
            }
            i++;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            this.mSettings.putSelectedSpellCheckerSubtype(tempHashCode);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void setSpellCheckerEnabledLocked(boolean enabled) {
        long ident = Binder.clearCallingIdentity();
        try {
            this.mSettings.setSpellCheckerEnabled(enabled);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private boolean isSpellCheckerEnabledLocked() {
        long ident = Binder.clearCallingIdentity();
        try {
            boolean retval = this.mSettings.isSpellCheckerEnabled();
            return retval;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump TextServicesManagerService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        synchronized (this.mSpellCheckerMap) {
            pw.println("Current Text Services Manager state:");
            pw.println("  Spell Checkers:");
            int spellCheckerIndex = 0;
            for (SpellCheckerInfo info : this.mSpellCheckerMap.values()) {
                pw.println("  Spell Checker #" + spellCheckerIndex);
                info.dump(pw, "    ");
                spellCheckerIndex++;
            }
            pw.println("");
            pw.println("  Spell Checker Bind Groups:");
            for (Entry<String, SpellCheckerBindGroup> ent : this.mSpellCheckerBindGroups.entrySet()) {
                SpellCheckerBindGroup grp = (SpellCheckerBindGroup) ent.getValue();
                pw.println("    " + ((String) ent.getKey()) + " " + grp + ":");
                pw.println("      mInternalConnection=" + grp.mInternalConnection);
                pw.println("      mSpellChecker=" + grp.mSpellChecker);
                pw.println("      mBound=" + grp.mBound + " mConnected=" + grp.mConnected);
                int N = grp.mListeners.size();
                for (int i = 0; i < N; i++) {
                    InternalDeathRecipient listener = (InternalDeathRecipient) grp.mListeners.get(i);
                    pw.println("      Listener #" + i + ":");
                    pw.println("        mTsListener=" + listener.mTsListener);
                    pw.println("        mScListener=" + listener.mScListener);
                    pw.println("        mGroup=" + listener.mGroup);
                    pw.println("        mScLocale=" + listener.mScLocale + " mUid=" + listener.mUid);
                }
            }
            pw.println("");
            pw.println("  mSettings:");
            this.mSettings.dumpLocked(pw, "    ");
        }
    }

    private static String getStackTrace() {
        StringBuilder sb = new StringBuilder();
        try {
            throw new RuntimeException();
        } catch (RuntimeException e) {
            StackTraceElement[] frames = e.getStackTrace();
            for (int j = 1; j < frames.length; j++) {
                sb.append(frames[j].toString()).append("\n");
            }
            return sb.toString();
        }
    }
}

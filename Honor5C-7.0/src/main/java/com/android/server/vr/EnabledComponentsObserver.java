package com.android.server.vr;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.content.PackageMonitor;
import com.android.server.vr.SettingsObserver.SettingChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class EnabledComponentsObserver implements SettingChangeListener {
    public static final int DISABLED = -1;
    private static final String ENABLED_SERVICES_SEPARATOR = ":";
    public static final int NOT_INSTALLED = -2;
    public static final int NO_ERROR = 0;
    private static final String TAG = null;
    private final Context mContext;
    private final Set<EnabledComponentChangeListener> mEnabledComponentListeners;
    private final SparseArray<ArraySet<ComponentName>> mEnabledSet;
    private final SparseArray<ArraySet<ComponentName>> mInstalledSet;
    private final Object mLock;
    private final String mServiceName;
    private final String mServicePermission;
    private final String mSettingName;

    /* renamed from: com.android.server.vr.EnabledComponentsObserver.1 */
    static class AnonymousClass1 extends PackageMonitor {
        final /* synthetic */ EnabledComponentsObserver val$o;

        AnonymousClass1(EnabledComponentsObserver val$o) {
            this.val$o = val$o;
        }

        public void onSomePackagesChanged() {
            this.val$o.onPackagesChanged();
        }

        public void onPackageDisappeared(String packageName, int reason) {
            this.val$o.onPackagesChanged();
        }

        public void onPackageModified(String packageName) {
            this.val$o.onPackagesChanged();
        }

        public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
            this.val$o.onPackagesChanged();
            return super.onHandleForceStop(intent, packages, uid, doit);
        }
    }

    public interface EnabledComponentChangeListener {
        void onEnabledComponentChanged();
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.vr.EnabledComponentsObserver.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.vr.EnabledComponentsObserver.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.vr.EnabledComponentsObserver.<clinit>():void");
    }

    private EnabledComponentsObserver(Context context, String settingName, String servicePermission, String serviceName, Object lock, Collection<EnabledComponentChangeListener> listeners) {
        this.mInstalledSet = new SparseArray();
        this.mEnabledSet = new SparseArray();
        this.mEnabledComponentListeners = new ArraySet();
        this.mLock = lock;
        this.mContext = context;
        this.mSettingName = settingName;
        this.mServiceName = serviceName;
        this.mServicePermission = servicePermission;
        this.mEnabledComponentListeners.addAll(listeners);
    }

    public static EnabledComponentsObserver build(Context context, Handler handler, String settingName, Looper looper, String servicePermission, String serviceName, Object lock, Collection<EnabledComponentChangeListener> listeners) {
        SettingsObserver s = SettingsObserver.build(context, handler, settingName);
        EnabledComponentsObserver o = new EnabledComponentsObserver(context, settingName, servicePermission, serviceName, lock, listeners);
        new AnonymousClass1(o).register(context, looper, UserHandle.ALL, true);
        s.addListener(o);
        return o;
    }

    public void onPackagesChanged() {
        rebuildAll();
    }

    public void onSettingChanged() {
        rebuildAll();
    }

    public void onSettingRestored(String prevValue, String newValue, int userId) {
        rebuildAll();
    }

    public void onUsersChanged() {
        rebuildAll();
    }

    public void rebuildAll() {
        synchronized (this.mLock) {
            this.mInstalledSet.clear();
            this.mEnabledSet.clear();
            for (int i : getCurrentProfileIds()) {
                ArraySet<ComponentName> implementingPackages = loadComponentNamesForUser(i);
                ArraySet<ComponentName> packagesFromSettings = loadComponentNamesFromSetting(this.mSettingName, i);
                packagesFromSettings.retainAll(implementingPackages);
                this.mInstalledSet.put(i, implementingPackages);
                this.mEnabledSet.put(i, packagesFromSettings);
            }
        }
        sendSettingChanged();
    }

    public int isValid(ComponentName component, int userId) {
        synchronized (this.mLock) {
            ArraySet<ComponentName> installedComponents = (ArraySet) this.mInstalledSet.get(userId);
            if (installedComponents == null || !installedComponents.contains(component)) {
                return NOT_INSTALLED;
            }
            ArraySet<ComponentName> validComponents = (ArraySet) this.mEnabledSet.get(userId);
            if (validComponents == null || !validComponents.contains(component)) {
                return DISABLED;
            }
            return 0;
        }
    }

    public ArraySet<ComponentName> getInstalled(int userId) {
        ArraySet<ComponentName> arraySet;
        synchronized (this.mLock) {
            arraySet = (ArraySet) this.mInstalledSet.get(userId);
        }
        return arraySet;
    }

    public ArraySet<ComponentName> getEnabled(int userId) {
        ArraySet<ComponentName> arraySet;
        synchronized (this.mLock) {
            arraySet = (ArraySet) this.mEnabledSet.get(userId);
        }
        return arraySet;
    }

    private int[] getCurrentProfileIds() {
        UserManager userManager = (UserManager) this.mContext.getSystemService("user");
        if (userManager == null) {
            return null;
        }
        return userManager.getEnabledProfileIds(ActivityManager.getCurrentUser());
    }

    public static ArraySet<ComponentName> loadComponentNames(PackageManager pm, int userId, String serviceName, String permissionName) {
        ArraySet<ComponentName> installed = new ArraySet();
        List<ResolveInfo> installedServices = pm.queryIntentServicesAsUser(new Intent(serviceName), 132, userId);
        if (installedServices != null) {
            int count = installedServices.size();
            for (int i = 0; i < count; i++) {
                ServiceInfo info = ((ResolveInfo) installedServices.get(i)).serviceInfo;
                ComponentName component = new ComponentName(info.packageName, info.name);
                if (permissionName.equals(info.permission)) {
                    installed.add(component);
                } else {
                    Slog.w(TAG, "Skipping service " + info.packageName + "/" + info.name + ": it does not require the permission " + permissionName);
                }
            }
        }
        return installed;
    }

    private ArraySet<ComponentName> loadComponentNamesForUser(int userId) {
        return loadComponentNames(this.mContext.getPackageManager(), userId, this.mServiceName, this.mServicePermission);
    }

    private ArraySet<ComponentName> loadComponentNamesFromSetting(String settingName, int userId) {
        String settingValue = Secure.getStringForUser(this.mContext.getContentResolver(), settingName, userId);
        if (TextUtils.isEmpty(settingValue)) {
            return new ArraySet();
        }
        String[] restored = settingValue.split(ENABLED_SERVICES_SEPARATOR);
        ArraySet<ComponentName> result = new ArraySet(restored.length);
        for (String unflattenFromString : restored) {
            ComponentName value = ComponentName.unflattenFromString(unflattenFromString);
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }

    private void sendSettingChanged() {
        for (EnabledComponentChangeListener l : this.mEnabledComponentListeners) {
            l.onEnabledComponentChanged();
        }
    }
}

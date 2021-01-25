package com.android.server.infra;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.infra.AbstractMasterSystemService;
import com.android.server.infra.AbstractPerUserSystemService;
import java.io.PrintWriter;

public abstract class AbstractPerUserSystemService<S extends AbstractPerUserSystemService<S, M>, M extends AbstractMasterSystemService<M, S>> {
    @GuardedBy({"mLock"})
    private boolean mDisabled;
    protected final Object mLock;
    protected final M mMaster;
    @GuardedBy({"mLock"})
    private ServiceInfo mServiceInfo;
    @GuardedBy({"mLock"})
    private boolean mSetupComplete;
    protected final String mTag = getClass().getSimpleName();
    protected final int mUserId;

    protected AbstractPerUserSystemService(M master, Object lock, int userId) {
        this.mMaster = master;
        this.mLock = lock;
        this.mUserId = userId;
    }

    /* access modifiers changed from: protected */
    public ServiceInfo newServiceInfoLocked(ComponentName serviceComponent) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException("not overridden");
    }

    /* access modifiers changed from: protected */
    public void handlePackageUpdateLocked(String packageName) {
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mLock"})
    public boolean isEnabledLocked() {
        return this.mSetupComplete && this.mServiceInfo != null && !this.mDisabled;
    }

    /* access modifiers changed from: protected */
    public final boolean isDisabledByUserRestrictionsLocked() {
        return this.mDisabled;
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mLock"})
    public boolean updateLocked(boolean disabled) {
        boolean wasEnabled = isEnabledLocked();
        if (this.mMaster.verbose) {
            String str = this.mTag;
            Slog.v(str, "updateLocked(u=" + this.mUserId + "): wasEnabled=" + wasEnabled + ", mSetupComplete=" + this.mSetupComplete + ", disabled=" + disabled + ", mDisabled=" + this.mDisabled);
        }
        this.mSetupComplete = "1".equals(Settings.Secure.getStringForUser(getContext().getContentResolver(), "user_setup_complete", this.mUserId));
        this.mDisabled = disabled;
        updateServiceInfoLocked();
        return wasEnabled != isEnabledLocked();
    }

    /* access modifiers changed from: protected */
    public final ComponentName updateServiceInfoLocked() {
        ComponentName serviceComponent = null;
        if (this.mMaster.mServiceNameResolver != null) {
            ServiceInfo serviceInfo = null;
            String componentName = getComponentNameLocked();
            if (!TextUtils.isEmpty(componentName)) {
                try {
                    serviceComponent = ComponentName.unflattenFromString(componentName);
                    serviceInfo = AppGlobals.getPackageManager().getServiceInfo(serviceComponent, 0, this.mUserId);
                    if (serviceInfo == null) {
                        String str = this.mTag;
                        Slog.e(str, "Bad service name: " + componentName);
                    }
                } catch (RemoteException | RuntimeException e) {
                    String str2 = this.mTag;
                    Slog.e(str2, "Error getting service info for '" + componentName + "': " + e);
                    serviceInfo = null;
                }
            }
            if (serviceInfo != null) {
                try {
                    this.mServiceInfo = newServiceInfoLocked(serviceComponent);
                    if (this.mMaster.debug) {
                        String str3 = this.mTag;
                        Slog.d(str3, "Set component for user " + this.mUserId + " as " + serviceComponent + " and info as " + this.mServiceInfo);
                    }
                } catch (Exception e2) {
                    String str4 = this.mTag;
                    Slog.e(str4, "Bad ServiceInfo for '" + componentName + "': " + e2);
                    this.mServiceInfo = null;
                }
            } else {
                this.mServiceInfo = null;
                if (this.mMaster.debug) {
                    String str5 = this.mTag;
                    Slog.d(str5, "Reset component for user " + this.mUserId + ":" + componentName);
                }
            }
        }
        return serviceComponent;
    }

    public final int getUserId() {
        return this.mUserId;
    }

    public final M getMaster() {
        return this.mMaster;
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mLock"})
    public final int getServiceUidLocked() {
        ServiceInfo serviceInfo = this.mServiceInfo;
        if (serviceInfo != null) {
            return serviceInfo.applicationInfo.uid;
        }
        if (!this.mMaster.verbose) {
            return -1;
        }
        Slog.v(this.mTag, "getServiceUidLocked(): no mServiceInfo");
        return -1;
    }

    /* access modifiers changed from: protected */
    public final String getComponentNameLocked() {
        return this.mMaster.mServiceNameResolver.getServiceName(this.mUserId);
    }

    public final boolean isTemporaryServiceSetLocked() {
        return this.mMaster.mServiceNameResolver.isTemporary(this.mUserId);
    }

    /* access modifiers changed from: protected */
    public final void resetTemporaryServiceLocked() {
        this.mMaster.mServiceNameResolver.resetTemporaryService(this.mUserId);
    }

    public final ServiceInfo getServiceInfo() {
        return this.mServiceInfo;
    }

    public final ComponentName getServiceComponentName() {
        ComponentName componentName;
        synchronized (this.mLock) {
            componentName = this.mServiceInfo == null ? null : this.mServiceInfo.getComponentName();
        }
        return componentName;
    }

    public final String getServicePackageName() {
        ComponentName serviceComponent = getServiceComponentName();
        if (serviceComponent == null) {
            return null;
        }
        return serviceComponent.getPackageName();
    }

    @GuardedBy({"mLock"})
    public final CharSequence getServiceLabelLocked() {
        ServiceInfo serviceInfo = this.mServiceInfo;
        if (serviceInfo == null) {
            return null;
        }
        return serviceInfo.loadSafeLabel(getContext().getPackageManager(), 0.0f, 5);
    }

    @GuardedBy({"mLock"})
    public final Drawable getServiceIconLocked() {
        ServiceInfo serviceInfo = this.mServiceInfo;
        if (serviceInfo == null) {
            return null;
        }
        return serviceInfo.loadIcon(getContext().getPackageManager());
    }

    /* access modifiers changed from: protected */
    public final void removeSelfFromCacheLocked() {
        this.mMaster.removeCachedServiceLocked(this.mUserId);
    }

    public final boolean isDebug() {
        return this.mMaster.debug;
    }

    public final boolean isVerbose() {
        return this.mMaster.verbose;
    }

    public final int getTargedSdkLocked() {
        ServiceInfo serviceInfo = this.mServiceInfo;
        if (serviceInfo == null) {
            return 0;
        }
        return serviceInfo.applicationInfo.targetSdkVersion;
    }

    /* access modifiers changed from: protected */
    public final boolean isSetupCompletedLocked() {
        return this.mSetupComplete;
    }

    /* access modifiers changed from: protected */
    public final Context getContext() {
        return this.mMaster.getContext();
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mLock"})
    public void dumpLocked(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("User: ");
        pw.println(this.mUserId);
        if (this.mServiceInfo != null) {
            pw.print(prefix);
            pw.print("Service Label: ");
            pw.println(getServiceLabelLocked());
            pw.print(prefix);
            pw.print("Target SDK: ");
            pw.println(getTargedSdkLocked());
        }
        if (this.mMaster.mServiceNameResolver != null) {
            pw.print(prefix);
            pw.print("Name resolver: ");
            this.mMaster.mServiceNameResolver.dumpShort(pw, this.mUserId);
            pw.println();
        }
        pw.print(prefix);
        pw.print("Disabled by UserManager: ");
        pw.println(this.mDisabled);
        pw.print(prefix);
        pw.print("Setup complete: ");
        pw.println(this.mSetupComplete);
        if (this.mServiceInfo != null) {
            pw.print(prefix);
            pw.print("Service UID: ");
            pw.println(this.mServiceInfo.applicationInfo.uid);
        }
        pw.println();
    }
}

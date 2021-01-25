package com.android.server.infra;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerInternal;
import android.provider.Settings;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.Preconditions;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.infra.AbstractMasterSystemService;
import com.android.server.infra.AbstractPerUserSystemService;
import com.android.server.infra.ServiceNameResolver;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public abstract class AbstractMasterSystemService<M extends AbstractMasterSystemService<M, S>, S extends AbstractPerUserSystemService<S, M>> extends SystemService {
    public static final int PACKAGE_UPDATE_POLICY_NO_REFRESH = 0;
    public static final int PACKAGE_UPDATE_POLICY_REFRESH_EAGER = 2;
    public static final int PACKAGE_UPDATE_POLICY_REFRESH_LAZY = 1;
    public boolean debug;
    @GuardedBy({"mLock"})
    protected boolean mAllowInstantService;
    @GuardedBy({"mLock"})
    private final SparseBooleanArray mDisabledByUserRestriction;
    protected final Object mLock;
    private final int mPackageUpdatePolicy;
    protected final ServiceNameResolver mServiceNameResolver;
    @GuardedBy({"mLock"})
    private final SparseArray<S> mServicesCache;
    protected final String mTag;
    @GuardedBy({"mLock"})
    private SparseArray<String> mUpdatingPackageNames;
    public boolean verbose;

    @Retention(RetentionPolicy.SOURCE)
    public @interface PackageUpdatePolicy {
    }

    public interface Visitor<S> {
        void visit(S s);
    }

    /* access modifiers changed from: protected */
    public abstract S newServiceLocked(int i, boolean z);

    protected AbstractMasterSystemService(Context context, ServiceNameResolver serviceNameResolver, String disallowProperty) {
        this(context, serviceNameResolver, disallowProperty, 1);
    }

    protected AbstractMasterSystemService(Context context, ServiceNameResolver serviceNameResolver, String disallowProperty, int packageUpdatePolicy) {
        super(context);
        this.mTag = getClass().getSimpleName();
        this.mLock = new Object();
        this.verbose = false;
        this.debug = false;
        this.mServicesCache = new SparseArray<>();
        this.mPackageUpdatePolicy = packageUpdatePolicy;
        this.mServiceNameResolver = serviceNameResolver;
        ServiceNameResolver serviceNameResolver2 = this.mServiceNameResolver;
        if (serviceNameResolver2 != null) {
            serviceNameResolver2.setOnTemporaryServiceNameChangedCallback(new ServiceNameResolver.NameResolverListener() {
                /* class com.android.server.infra.$$Lambda$AbstractMasterSystemService$su3lJpEVIbLC7doP4eboTpqjxU */

                @Override // com.android.server.infra.ServiceNameResolver.NameResolverListener
                public final void onNameResolved(int i, String str, boolean z) {
                    AbstractMasterSystemService.this.lambda$new$0$AbstractMasterSystemService(i, str, z);
                }
            });
        }
        if (disallowProperty == null) {
            this.mDisabledByUserRestriction = null;
        } else {
            this.mDisabledByUserRestriction = new SparseBooleanArray();
            UserManagerInternal umi = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
            List<UserInfo> users = ((UserManager) context.getSystemService(UserManager.class)).getUsers();
            for (int i = 0; i < users.size(); i++) {
                int userId = users.get(i).id;
                boolean disabled = umi.getUserRestriction(userId, disallowProperty);
                if (disabled) {
                    String str = this.mTag;
                    Slog.i(str, "Disabling by restrictions user " + userId);
                    this.mDisabledByUserRestriction.put(userId, disabled);
                }
            }
            umi.addUserRestrictionsListener(new UserManagerInternal.UserRestrictionsListener(disallowProperty) {
                /* class com.android.server.infra.$$Lambda$AbstractMasterSystemService$_fKwVUP0pSfcMMlgRqoT4OPhxw */
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                public final void onUserRestrictionsChanged(int i, Bundle bundle, Bundle bundle2) {
                    AbstractMasterSystemService.this.lambda$new$1$AbstractMasterSystemService(this.f$1, i, bundle, bundle2);
                }
            });
        }
        startTrackingPackageChanges();
    }

    public /* synthetic */ void lambda$new$1$AbstractMasterSystemService(String disallowProperty, int userId, Bundle newRestrictions, Bundle prevRestrictions) {
        boolean disabledNow = newRestrictions.getBoolean(disallowProperty, false);
        synchronized (this.mLock) {
            if (this.mDisabledByUserRestriction.get(userId) != disabledNow || !this.debug) {
                String str = this.mTag;
                Slog.i(str, "Updating for user " + userId + ": disabled=" + disabledNow);
                this.mDisabledByUserRestriction.put(userId, disabledNow);
                updateCachedServiceLocked(userId, disabledNow);
                return;
            }
            String str2 = this.mTag;
            Slog.d(str2, "Restriction did not change for user " + userId);
        }
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 600) {
            new SettingsObserver(BackgroundThread.getHandler());
        }
    }

    @Override // com.android.server.SystemService
    public void onUnlockUser(int userId) {
        synchronized (this.mLock) {
            updateCachedServiceLocked(userId);
        }
    }

    @Override // com.android.server.SystemService
    public void onCleanupUser(int userId) {
        synchronized (this.mLock) {
            removeCachedServiceLocked(userId);
        }
    }

    public final boolean getAllowInstantService() {
        boolean z;
        enforceCallingPermissionForManagement();
        synchronized (this.mLock) {
            z = this.mAllowInstantService;
        }
        return z;
    }

    public final boolean isBindInstantServiceAllowed() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mAllowInstantService;
        }
        return z;
    }

    public final void setAllowInstantService(boolean mode) {
        String str = this.mTag;
        Slog.i(str, "setAllowInstantService(): " + mode);
        enforceCallingPermissionForManagement();
        synchronized (this.mLock) {
            this.mAllowInstantService = mode;
        }
    }

    public final void setTemporaryService(int userId, String componentName, int durationMs) {
        String str = this.mTag;
        Slog.i(str, "setTemporaryService(" + userId + ") to " + componentName + " for " + durationMs + "ms");
        enforceCallingPermissionForManagement();
        Preconditions.checkNotNull(componentName);
        int maxDurationMs = getMaximumTemporaryServiceDurationMs();
        if (durationMs <= maxDurationMs) {
            synchronized (this.mLock) {
                S oldService = peekServiceForUserLocked(userId);
                if (oldService != null) {
                    oldService.removeSelfFromCacheLocked();
                }
                this.mServiceNameResolver.setTemporaryService(userId, componentName, durationMs);
            }
            return;
        }
        throw new IllegalArgumentException("Max duration is " + maxDurationMs + " (called with " + durationMs + ")");
    }

    public final boolean setDefaultServiceEnabled(int userId, boolean enabled) {
        String str = this.mTag;
        Slog.i(str, "setDefaultServiceEnabled() for userId " + userId + ": " + enabled);
        enforceCallingPermissionForManagement();
        synchronized (this.mLock) {
            if (!this.mServiceNameResolver.setDefaultServiceEnabled(userId, enabled)) {
                if (this.verbose) {
                    String str2 = this.mTag;
                    Slog.v(str2, "setDefaultServiceEnabled(" + userId + "): already " + enabled);
                }
                return false;
            }
            S oldService = peekServiceForUserLocked(userId);
            if (oldService != null) {
                oldService.removeSelfFromCacheLocked();
            }
            updateCachedServiceLocked(userId);
            return true;
        }
    }

    public final boolean isDefaultServiceEnabled(int userId) {
        boolean isDefaultServiceEnabled;
        enforceCallingPermissionForManagement();
        synchronized (this.mLock) {
            isDefaultServiceEnabled = this.mServiceNameResolver.isDefaultServiceEnabled(userId);
        }
        return isDefaultServiceEnabled;
    }

    /* access modifiers changed from: protected */
    public int getMaximumTemporaryServiceDurationMs() {
        throw new UnsupportedOperationException("Not implemented by " + getClass());
    }

    public final void resetTemporaryService(int userId) {
        String str = this.mTag;
        Slog.i(str, "resetTemporaryService(): " + userId);
        enforceCallingPermissionForManagement();
        synchronized (this.mLock) {
            S service = getServiceForUserLocked(userId);
            if (service != null) {
                service.resetTemporaryServiceLocked();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void enforceCallingPermissionForManagement() {
        throw new UnsupportedOperationException("Not implemented by " + getClass());
    }

    /* access modifiers changed from: protected */
    public void registerForExtraSettingsChanges(ContentResolver resolver, ContentObserver observer) {
    }

    /* access modifiers changed from: protected */
    public void onSettingsChanged(int userId, String property) {
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mLock"})
    public S getServiceForUserLocked(int userId) {
        int resolvedUserId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, false, null, null);
        S service = this.mServicesCache.get(resolvedUserId);
        if (service == null) {
            boolean disabled = isDisabledLocked(userId);
            service = newServiceLocked(resolvedUserId, disabled);
            if (!disabled) {
                onServiceEnabledLocked(service, resolvedUserId);
            }
            this.mServicesCache.put(userId, service);
        }
        return service;
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mLock"})
    public S peekServiceForUserLocked(int userId) {
        return this.mServicesCache.get(ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, false, null, null));
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mLock"})
    public void updateCachedServiceLocked(int userId) {
        updateCachedServiceLocked(userId, isDisabledLocked(userId));
    }

    /* access modifiers changed from: protected */
    public boolean isDisabledLocked(int userId) {
        SparseBooleanArray sparseBooleanArray = this.mDisabledByUserRestriction;
        if (sparseBooleanArray == null) {
            return false;
        }
        return sparseBooleanArray.get(userId);
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mLock"})
    public S updateCachedServiceLocked(int userId, boolean disabled) {
        S service = getServiceForUserLocked(userId);
        if (service != null) {
            service.updateLocked(disabled);
            if (!service.isEnabledLocked()) {
                removeCachedServiceLocked(userId);
            } else {
                onServiceEnabledLocked(service, userId);
            }
        }
        return service;
    }

    /* access modifiers changed from: protected */
    public String getServiceSettingsProperty() {
        return null;
    }

    /* access modifiers changed from: protected */
    public void onServiceEnabledLocked(S s, int userId) {
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mLock"})
    public final S removeCachedServiceLocked(int userId) {
        S service = peekServiceForUserLocked(userId);
        if (service != null) {
            this.mServicesCache.delete(userId);
            onServiceRemoved(service, userId);
        }
        return service;
    }

    /* access modifiers changed from: protected */
    public void onServicePackageUpdatingLocked(int userId) {
        if (this.verbose) {
            String str = this.mTag;
            Slog.v(str, "onServicePackageUpdatingLocked(" + userId + ")");
        }
    }

    /* access modifiers changed from: protected */
    public void onServicePackageUpdatedLocked(int userId) {
        if (this.verbose) {
            String str = this.mTag;
            Slog.v(str, "onServicePackageUpdated(" + userId + ")");
        }
    }

    /* access modifiers changed from: protected */
    public void onServiceRemoved(S s, int userId) {
    }

    /* access modifiers changed from: protected */
    /* renamed from: onServiceNameChanged */
    public void lambda$new$0$AbstractMasterSystemService(int userId, String serviceName, boolean isTemporary) {
        synchronized (this.mLock) {
            updateCachedServiceLocked(userId);
        }
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mLock"})
    public void visitServicesLocked(Visitor<S> visitor) {
        int size = this.mServicesCache.size();
        for (int i = 0; i < size; i++) {
            visitor.visit(this.mServicesCache.valueAt(i));
        }
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mLock"})
    public void clearCacheLocked() {
        this.mServicesCache.clear();
    }

    /* access modifiers changed from: protected */
    public final void assertCalledByPackageOwner(String packageName) {
        Preconditions.checkNotNull(packageName);
        int uid = Binder.getCallingUid();
        String[] packages = getContext().getPackageManager().getPackagesForUid(uid);
        if (packages != null) {
            for (String candidate : packages) {
                if (packageName.equals(candidate)) {
                    return;
                }
            }
        }
        throw new SecurityException("UID " + uid + " does not own " + packageName);
    }

    /* access modifiers changed from: protected */
    public void dumpLocked(String prefix, PrintWriter pw) {
        boolean realDebug = this.debug;
        boolean realVerbose = this.verbose;
        try {
            this.verbose = true;
            this.debug = true;
            int size = this.mServicesCache.size();
            pw.print(prefix);
            pw.print("Debug: ");
            pw.print(realDebug);
            pw.print(" Verbose: ");
            pw.println(realVerbose);
            pw.print("Refresh on package update: ");
            pw.println(this.mPackageUpdatePolicy);
            if (this.mUpdatingPackageNames != null) {
                pw.print("Packages being updated: ");
                pw.println(this.mUpdatingPackageNames);
            }
            if (this.mServiceNameResolver != null) {
                pw.print(prefix);
                pw.print("Name resolver: ");
                this.mServiceNameResolver.dumpShort(pw);
                pw.println();
                List<UserInfo> users = ((UserManager) getContext().getSystemService(UserManager.class)).getUsers();
                for (int i = 0; i < users.size(); i++) {
                    int userId = users.get(i).id;
                    pw.print("    ");
                    pw.print(userId);
                    pw.print(": ");
                    this.mServiceNameResolver.dumpShort(pw, userId);
                    pw.println();
                }
            }
            pw.print(prefix);
            pw.print("Users disabled by restriction: ");
            pw.println(this.mDisabledByUserRestriction);
            pw.print(prefix);
            pw.print("Allow instant service: ");
            pw.println(this.mAllowInstantService);
            String settingsProperty = getServiceSettingsProperty();
            if (settingsProperty != null) {
                pw.print(prefix);
                pw.print("Settings property: ");
                pw.println(settingsProperty);
            }
            pw.print(prefix);
            pw.print("Cached services: ");
            if (size == 0) {
                pw.println("none");
            } else {
                pw.println(size);
                for (int i2 = 0; i2 < size; i2++) {
                    pw.print(prefix);
                    pw.print("Service at ");
                    pw.print(i2);
                    pw.println(": ");
                    this.mServicesCache.valueAt(i2).dumpLocked("    ", pw);
                    pw.println();
                }
            }
        } finally {
            this.debug = realDebug;
            this.verbose = realVerbose;
        }
    }

    private void startTrackingPackageChanges() {
        new PackageMonitor() {
            /* class com.android.server.infra.AbstractMasterSystemService.AnonymousClass1 */

            public void onPackageUpdateStarted(String packageName, int uid) {
                if (AbstractMasterSystemService.this.verbose) {
                    String str = AbstractMasterSystemService.this.mTag;
                    Slog.v(str, "onPackageUpdateStarted(): " + packageName);
                }
                String activePackageName = getActiveServicePackageNameLocked();
                if (packageName.equals(activePackageName)) {
                    int userId = getChangingUserId();
                    synchronized (AbstractMasterSystemService.this.mLock) {
                        if (AbstractMasterSystemService.this.mUpdatingPackageNames == null) {
                            AbstractMasterSystemService.this.mUpdatingPackageNames = new SparseArray(AbstractMasterSystemService.this.mServicesCache.size());
                        }
                        AbstractMasterSystemService.this.mUpdatingPackageNames.put(userId, packageName);
                        AbstractMasterSystemService.this.onServicePackageUpdatingLocked(userId);
                        if (AbstractMasterSystemService.this.mPackageUpdatePolicy != 0) {
                            if (AbstractMasterSystemService.this.debug) {
                                String str2 = AbstractMasterSystemService.this.mTag;
                                Slog.d(str2, "Removing service for user " + userId + " because package " + activePackageName + " is being updated");
                            }
                            AbstractMasterSystemService.this.removeCachedServiceLocked(userId);
                            if (AbstractMasterSystemService.this.mPackageUpdatePolicy == 2) {
                                if (AbstractMasterSystemService.this.debug) {
                                    String str3 = AbstractMasterSystemService.this.mTag;
                                    Slog.d(str3, "Eagerly recreating service for user " + userId);
                                }
                                AbstractMasterSystemService.this.getServiceForUserLocked(userId);
                            }
                        } else if (AbstractMasterSystemService.this.debug) {
                            String str4 = AbstractMasterSystemService.this.mTag;
                            Slog.d(str4, "Holding service for user " + userId + " while package " + activePackageName + " is being updated");
                        }
                    }
                }
            }

            public void onPackageUpdateFinished(String packageName, int uid) {
                String activePackageName;
                if (AbstractMasterSystemService.this.verbose) {
                    Slog.v(AbstractMasterSystemService.this.mTag, "onPackageUpdateFinished(): " + packageName);
                }
                int userId = getChangingUserId();
                synchronized (AbstractMasterSystemService.this.mLock) {
                    if (AbstractMasterSystemService.this.mUpdatingPackageNames == null) {
                        activePackageName = null;
                    } else {
                        activePackageName = (String) AbstractMasterSystemService.this.mUpdatingPackageNames.get(userId);
                    }
                    if (packageName.equals(activePackageName)) {
                        if (AbstractMasterSystemService.this.mUpdatingPackageNames != null) {
                            AbstractMasterSystemService.this.mUpdatingPackageNames.remove(userId);
                            if (AbstractMasterSystemService.this.mUpdatingPackageNames.size() == 0) {
                                AbstractMasterSystemService.this.mUpdatingPackageNames = null;
                            }
                        }
                        AbstractMasterSystemService.this.onServicePackageUpdatedLocked(userId);
                    } else {
                        handlePackageUpdateLocked(packageName);
                    }
                }
            }

            public void onPackageRemoved(String packageName, int uid) {
                ComponentName componentName;
                synchronized (AbstractMasterSystemService.this.mLock) {
                    int userId = getChangingUserId();
                    AbstractPerUserSystemService peekServiceForUserLocked = AbstractMasterSystemService.this.peekServiceForUserLocked(userId);
                    if (!(peekServiceForUserLocked == null || (componentName = peekServiceForUserLocked.getServiceComponentName()) == null || !packageName.equals(componentName.getPackageName()))) {
                        handleActiveServiceRemoved(userId);
                    }
                }
            }

            public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
                synchronized (AbstractMasterSystemService.this.mLock) {
                    String activePackageName = getActiveServicePackageNameLocked();
                    for (String pkg : packages) {
                        if (!pkg.equals(activePackageName)) {
                            handlePackageUpdateLocked(pkg);
                        } else if (!doit) {
                            return true;
                        } else {
                            AbstractMasterSystemService.this.removeCachedServiceLocked(getChangingUserId());
                        }
                    }
                    return false;
                }
            }

            private void handleActiveServiceRemoved(int userId) {
                synchronized (AbstractMasterSystemService.this.mLock) {
                    AbstractMasterSystemService.this.removeCachedServiceLocked(userId);
                }
                String serviceSettingsProperty = AbstractMasterSystemService.this.getServiceSettingsProperty();
                if (serviceSettingsProperty != null) {
                    Settings.Secure.putStringForUser(AbstractMasterSystemService.this.getContext().getContentResolver(), serviceSettingsProperty, null, userId);
                }
            }

            private String getActiveServicePackageNameLocked() {
                ComponentName serviceComponent;
                AbstractPerUserSystemService peekServiceForUserLocked = AbstractMasterSystemService.this.peekServiceForUserLocked(getChangingUserId());
                if (peekServiceForUserLocked == null || (serviceComponent = peekServiceForUserLocked.getServiceComponentName()) == null) {
                    return null;
                }
                return serviceComponent.getPackageName();
            }

            @GuardedBy({"mLock"})
            private void handlePackageUpdateLocked(String packageName) {
                AbstractMasterSystemService.this.visitServicesLocked(new Visitor(packageName) {
                    /* class com.android.server.infra.$$Lambda$AbstractMasterSystemService$1$TLhe3_2yHs5UB69Y7lf2s7OxJCo */
                    private final /* synthetic */ String f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // com.android.server.infra.AbstractMasterSystemService.Visitor
                    public final void visit(Object obj) {
                        ((AbstractPerUserSystemService) obj).handlePackageUpdateLocked(this.f$0);
                    }
                });
            }
        }.register(getContext(), (Looper) null, UserHandle.ALL, true);
    }

    private final class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
            ContentResolver resolver = AbstractMasterSystemService.this.getContext().getContentResolver();
            String serviceProperty = AbstractMasterSystemService.this.getServiceSettingsProperty();
            if (serviceProperty != null) {
                resolver.registerContentObserver(Settings.Secure.getUriFor(serviceProperty), false, this, -1);
            }
            resolver.registerContentObserver(Settings.Secure.getUriFor("user_setup_complete"), false, this, -1);
            AbstractMasterSystemService.this.registerForExtraSettingsChanges(resolver, this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri, int userId) {
            if (AbstractMasterSystemService.this.verbose) {
                String str = AbstractMasterSystemService.this.mTag;
                Slog.v(str, "onChange(): uri=" + uri + ", userId=" + userId);
            }
            String property = uri.getLastPathSegment();
            if (property.equals(AbstractMasterSystemService.this.getServiceSettingsProperty()) || property.equals("user_setup_complete")) {
                synchronized (AbstractMasterSystemService.this.mLock) {
                    AbstractMasterSystemService.this.updateCachedServiceLocked(userId);
                }
                return;
            }
            AbstractMasterSystemService.this.onSettingsChanged(userId, property);
        }
    }
}

package com.android.server.autofill;

import android.app.ActivityManagerInternal;
import android.app.ActivityThread;
import android.content.AutofillOptions;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.service.autofill.FillEventHistory;
import android.service.autofill.UserData;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.LocalLog;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillManagerInternal;
import android.view.autofill.AutofillValue;
import android.view.autofill.Helper;
import android.view.autofill.IAutoFillManager;
import android.view.autofill.IAutoFillManagerClient;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.infra.GlobalWhitelistState;
import com.android.internal.infra.WhitelistHelper;
import com.android.internal.os.IResultReceiver;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.Preconditions;
import com.android.internal.util.SyncResultReceiver;
import com.android.server.FgThread;
import com.android.server.LocalServices;
import com.android.server.autofill.ui.AutoFillUI;
import com.android.server.infra.AbstractMasterSystemService;
import com.android.server.infra.FrameworkResourcesServiceNameResolver;
import com.android.server.infra.SecureSettingsServiceNameResolver;
import com.android.server.infra.ServiceNameResolver;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class AutofillManagerService extends AbstractMasterSystemService<AutofillManagerService, AutofillManagerServiceImpl> {
    private static final char COMPAT_PACKAGE_DELIMITER = ':';
    private static final char COMPAT_PACKAGE_URL_IDS_BLOCK_BEGIN = '[';
    private static final char COMPAT_PACKAGE_URL_IDS_BLOCK_END = ']';
    private static final char COMPAT_PACKAGE_URL_IDS_DELIMITER = ',';
    private static final int DEFAULT_AUGMENTED_AUTOFILL_REQUEST_TIMEOUT_MILLIS = 5000;
    static final String RECEIVER_BUNDLE_EXTRA_SESSIONS = "sessions";
    private static final String TAG = "AutofillManagerService";
    private static final Object sLock = AutofillManagerService.class;
    @GuardedBy({"sLock"})
    private static int sPartitionMaxCount = 10;
    @GuardedBy({"sLock"})
    private static int sVisibleDatasetsMaxCount = 0;
    private final ActivityManagerInternal mAm = ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class));
    final FrameworkResourcesServiceNameResolver mAugmentedAutofillResolver;
    final AugmentedAutofillState mAugmentedAutofillState = new AugmentedAutofillState();
    @GuardedBy({"mLock"})
    int mAugmentedServiceIdleUnbindTimeoutMs;
    @GuardedBy({"mLock"})
    int mAugmentedServiceRequestTimeoutMs;
    private final AutofillCompatState mAutofillCompatState = new AutofillCompatState();
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.autofill.AutofillManagerService.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                if (Helper.sDebug) {
                    Slog.d(AutofillManagerService.TAG, "Close system dialogs");
                }
                synchronized (AutofillManagerService.this.mLock) {
                    AutofillManagerService.this.visitServicesLocked($$Lambda$AutofillManagerService$1$1WNu3tTkxodB_LsZ7dGIlvrPN0.INSTANCE);
                }
                AutofillManagerService.this.mUi.hideAll(null);
            }
        }
    };
    private final LocalService mLocalService = new LocalService();
    private final LocalLog mRequestsHistory = new LocalLog(20);
    @GuardedBy({"mLock"})
    private int mSupportedSmartSuggestionModes;
    private final AutoFillUI mUi = new AutoFillUI(ActivityThread.currentActivityThread().getSystemUiContext());
    private final LocalLog mUiLatencyHistory = new LocalLog(20);
    private final LocalLog mWtfHistory = new LocalLog(50);

    public AutofillManagerService(Context context) {
        super(context, new SecureSettingsServiceNameResolver(context, "autofill_service"), "no_autofill");
        DeviceConfig.addOnPropertiesChangedListener("autofill", ActivityThread.currentApplication().getMainExecutor(), new DeviceConfig.OnPropertiesChangedListener() {
            /* class com.android.server.autofill.$$Lambda$AutofillManagerService$AjdnAnVaegTp2pojE30m5yjqZx8 */

            public final void onPropertiesChanged(DeviceConfig.Properties properties) {
                AutofillManagerService.this.lambda$new$0$AutofillManagerService(properties);
            }
        });
        setLogLevelFromSettings();
        setMaxPartitionsFromSettings();
        setMaxVisibleDatasetsFromSettings();
        setDeviceConfigProperties();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        context.registerReceiver(this.mBroadcastReceiver, filter, null, FgThread.getHandler());
        this.mAugmentedAutofillResolver = new FrameworkResourcesServiceNameResolver(getContext(), 17039810);
        this.mAugmentedAutofillResolver.setOnTemporaryServiceNameChangedCallback(new ServiceNameResolver.NameResolverListener() {
            /* class com.android.server.autofill.$$Lambda$AutofillManagerService$6afarIdhLaYDLGebVyDMPu2nok */

            @Override // com.android.server.infra.ServiceNameResolver.NameResolverListener
            public final void onNameResolved(int i, String str, boolean z) {
                AutofillManagerService.this.lambda$new$1$AutofillManagerService(i, str, z);
            }
        });
        if (this.mSupportedSmartSuggestionModes != 0) {
            List<UserInfo> users = ((UserManager) getContext().getSystemService(UserManager.class)).getUsers();
            for (int i = 0; i < users.size(); i++) {
                int userId = users.get(i).id;
                getServiceForUserLocked(userId);
                this.mAugmentedAutofillState.setServiceInfo(userId, this.mAugmentedAutofillResolver.getServiceName(userId), this.mAugmentedAutofillResolver.isTemporary(userId));
            }
        }
    }

    public /* synthetic */ void lambda$new$0$AutofillManagerService(DeviceConfig.Properties properties) {
        onDeviceConfigChange(properties.getKeyset());
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractMasterSystemService
    public String getServiceSettingsProperty() {
        return "autofill_service";
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractMasterSystemService
    public void registerForExtraSettingsChanges(ContentResolver resolver, ContentObserver observer) {
        resolver.registerContentObserver(Settings.Global.getUriFor("autofill_compat_mode_allowed_packages"), false, observer, -1);
        resolver.registerContentObserver(Settings.Global.getUriFor("autofill_logging_level"), false, observer, -1);
        resolver.registerContentObserver(Settings.Global.getUriFor("autofill_max_partitions_size"), false, observer, -1);
        resolver.registerContentObserver(Settings.Global.getUriFor("autofill_max_visible_datasets"), false, observer, -1);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractMasterSystemService
    public void onSettingsChanged(int userId, String property) {
        char c;
        switch (property.hashCode()) {
            case -1848997872:
                if (property.equals("autofill_max_visible_datasets")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1299292969:
                if (property.equals("autofill_logging_level")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -1048937777:
                if (property.equals("autofill_max_partitions_size")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1670367536:
                if (property.equals("autofill_compat_mode_allowed_packages")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            setLogLevelFromSettings();
        } else if (c == 1) {
            setMaxPartitionsFromSettings();
        } else if (c != 2) {
            if (c != 4) {
                Slog.w(TAG, "Unexpected property (" + property + "); updating cache instead");
            }
            synchronized (this.mLock) {
                updateCachedServiceLocked(userId);
            }
        } else {
            setMaxVisibleDatasetsFromSettings();
        }
    }

    private void onDeviceConfigChange(Set<String> keys) {
        for (String key : keys) {
            char c = 65535;
            int hashCode = key.hashCode();
            if (hashCode != -1546842390) {
                if (hashCode != -987506216) {
                    if (hashCode == 1709136986 && key.equals("smart_suggestion_supported_modes")) {
                        c = 0;
                    }
                } else if (key.equals("augmented_service_request_timeout")) {
                    c = 2;
                }
            } else if (key.equals("augmented_service_idle_unbind_timeout")) {
                c = 1;
            }
            if (c == 0 || c == 1 || c == 2) {
                setDeviceConfigProperties();
            } else {
                Slog.i(this.mTag, "Ignoring change on " + key);
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: onAugmentedServiceNameChanged */
    public void lambda$new$1$AutofillManagerService(int userId, String serviceName, boolean isTemporary) {
        this.mAugmentedAutofillState.setServiceInfo(userId, serviceName, isTemporary);
        synchronized (this.mLock) {
            ((AutofillManagerServiceImpl) getServiceForUserLocked(userId)).updateRemoteAugmentedAutofillService();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractMasterSystemService
    public AutofillManagerServiceImpl newServiceLocked(int resolvedUserId, boolean disabled) {
        return new AutofillManagerServiceImpl(this, this.mLock, this.mUiLatencyHistory, this.mWtfHistory, resolvedUserId, this.mUi, this.mAutofillCompatState, disabled);
    }

    /* access modifiers changed from: protected */
    public void onServiceRemoved(AutofillManagerServiceImpl service, int userId) {
        service.destroyLocked();
        this.mAutofillCompatState.removeCompatibilityModeRequests(userId);
    }

    /* access modifiers changed from: protected */
    public void onServiceEnabledLocked(AutofillManagerServiceImpl service, int userId) {
        addCompatibilityModeRequestsLocked(service, userId);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractMasterSystemService
    public void enforceCallingPermissionForManagement() {
        getContext().enforceCallingPermission("android.permission.MANAGE_AUTO_FILL", TAG);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.autofill.AutofillManagerService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [android.os.IBinder, com.android.server.autofill.AutofillManagerService$AutoFillManagerServiceStub] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("autofill", new AutoFillManagerServiceStub());
        publishLocalService(AutofillManagerInternal.class, this.mLocalService);
    }

    @Override // com.android.server.SystemService
    public void onSwitchUser(int userHandle) {
        if (Helper.sDebug) {
            Slog.d(TAG, "Hiding UI when user switched");
        }
        this.mUi.hideAll(null);
    }

    /* access modifiers changed from: package-private */
    public int getSupportedSmartSuggestionModesLocked() {
        return this.mSupportedSmartSuggestionModes;
    }

    /* access modifiers changed from: package-private */
    public void logRequestLocked(String historyItem) {
        this.mRequestsHistory.log(historyItem);
    }

    /* access modifiers changed from: package-private */
    public boolean isInstantServiceAllowed() {
        return this.mAllowInstantService;
    }

    /* access modifiers changed from: package-private */
    public void destroySessions(int userId, IResultReceiver receiver) {
        Slog.i(TAG, "destroySessions() for userId " + userId);
        enforceCallingPermissionForManagement();
        synchronized (this.mLock) {
            if (userId != -1) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) peekServiceForUserLocked(userId);
                if (service != null) {
                    service.destroySessionsLocked();
                }
            } else {
                visitServicesLocked($$Lambda$AutofillManagerService$J4rMQC_cWRd6Td3UdzyhcfhT9xc.INSTANCE);
            }
        }
        try {
            receiver.send(0, new Bundle());
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: package-private */
    public void listSessions(int userId, IResultReceiver receiver) {
        Slog.i(TAG, "listSessions() for userId " + userId);
        enforceCallingPermissionForManagement();
        Bundle resultData = new Bundle();
        ArrayList<String> sessions = new ArrayList<>();
        synchronized (this.mLock) {
            if (userId != -1) {
                try {
                    AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) peekServiceForUserLocked(userId);
                    if (service != null) {
                        service.listSessionsLocked(sessions);
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                visitServicesLocked(new AbstractMasterSystemService.Visitor(sessions) {
                    /* class com.android.server.autofill.$$Lambda$AutofillManagerService$froT1eG0jUnRoVv7cbUMLtO1bho */
                    private final /* synthetic */ ArrayList f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // com.android.server.infra.AbstractMasterSystemService.Visitor
                    public final void visit(Object obj) {
                        ((AutofillManagerServiceImpl) obj).listSessionsLocked(this.f$0);
                    }
                });
            }
        }
        resultData.putStringArrayList(RECEIVER_BUNDLE_EXTRA_SESSIONS, sessions);
        try {
            receiver.send(0, resultData);
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: package-private */
    public void reset() {
        Slog.i(TAG, "reset()");
        enforceCallingPermissionForManagement();
        synchronized (this.mLock) {
            visitServicesLocked($$Lambda$AutofillManagerService$PR6iUwKxXatnzjgBDLARdxaGV3A.INSTANCE);
            clearCacheLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void setLogLevel(int level) {
        Slog.i(TAG, "setLogLevel(): " + level);
        enforceCallingPermissionForManagement();
        long token = Binder.clearCallingIdentity();
        try {
            Settings.Global.putInt(getContext().getContentResolver(), "autofill_logging_level", level);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void setLogLevelFromSettings() {
        int level = Settings.Global.getInt(getContext().getContentResolver(), "autofill_logging_level", AutofillManager.DEFAULT_LOGGING_LEVEL);
        boolean debug = false;
        boolean verbose = false;
        if (level != 0) {
            if (level == 4) {
                verbose = true;
                debug = true;
            } else if (level == 2) {
                debug = true;
            } else {
                Slog.w(TAG, "setLogLevelFromSettings(): invalid level: " + level);
            }
        }
        if (debug || Helper.sDebug) {
            Slog.d(TAG, "setLogLevelFromSettings(): level=" + level + ", debug=" + debug + ", verbose=" + verbose);
        }
        synchronized (this.mLock) {
            setLoggingLevelsLocked(debug, verbose);
        }
    }

    /* access modifiers changed from: package-private */
    public int getLogLevel() {
        enforceCallingPermissionForManagement();
        synchronized (this.mLock) {
            if (Helper.sVerbose) {
                return 4;
            }
            if (Helper.sDebug) {
                return 2;
            }
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public int getMaxPartitions() {
        int i;
        enforceCallingPermissionForManagement();
        synchronized (this.mLock) {
            i = sPartitionMaxCount;
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    public void setMaxPartitions(int max) {
        Slog.i(TAG, "setMaxPartitions(): " + max);
        enforceCallingPermissionForManagement();
        long token = Binder.clearCallingIdentity();
        try {
            Settings.Global.putInt(getContext().getContentResolver(), "autofill_max_partitions_size", max);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void setMaxPartitionsFromSettings() {
        int max = Settings.Global.getInt(getContext().getContentResolver(), "autofill_max_partitions_size", 10);
        if (Helper.sDebug) {
            Slog.d(TAG, "setMaxPartitionsFromSettings(): " + max);
        }
        synchronized (sLock) {
            sPartitionMaxCount = max;
        }
    }

    /* access modifiers changed from: package-private */
    public int getMaxVisibleDatasets() {
        int i;
        enforceCallingPermissionForManagement();
        synchronized (sLock) {
            i = sVisibleDatasetsMaxCount;
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    public void setMaxVisibleDatasets(int max) {
        Slog.i(TAG, "setMaxVisibleDatasets(): " + max);
        enforceCallingPermissionForManagement();
        long token = Binder.clearCallingIdentity();
        try {
            Settings.Global.putInt(getContext().getContentResolver(), "autofill_max_visible_datasets", max);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void setMaxVisibleDatasetsFromSettings() {
        int max = Settings.Global.getInt(getContext().getContentResolver(), "autofill_max_visible_datasets", 0);
        if (Helper.sDebug) {
            Slog.d(TAG, "setMaxVisibleDatasetsFromSettings(): " + max);
        }
        synchronized (sLock) {
            sVisibleDatasetsMaxCount = max;
        }
    }

    private void setDeviceConfigProperties() {
        synchronized (this.mLock) {
            this.mAugmentedServiceIdleUnbindTimeoutMs = DeviceConfig.getInt("autofill", "augmented_service_idle_unbind_timeout", 0);
            this.mAugmentedServiceRequestTimeoutMs = DeviceConfig.getInt("autofill", "augmented_service_request_timeout", (int) DEFAULT_AUGMENTED_AUTOFILL_REQUEST_TIMEOUT_MILLIS);
            this.mSupportedSmartSuggestionModes = DeviceConfig.getInt("autofill", "smart_suggestion_supported_modes", 1);
            if (this.verbose) {
                String str = this.mTag;
                Slog.v(str, "setDeviceConfigProperties(): augmentedIdleTimeout=" + this.mAugmentedServiceIdleUnbindTimeoutMs + ", augmentedRequestTimeout=" + this.mAugmentedServiceRequestTimeoutMs + ", smartSuggestionMode=" + AutofillManager.getSmartSuggestionModeToString(this.mSupportedSmartSuggestionModes));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void calculateScore(String algorithmName, String value1, String value2, RemoteCallback callback) {
        enforceCallingPermissionForManagement();
        new FieldClassificationStrategy(getContext(), -2).calculateScores(callback, Arrays.asList(AutofillValue.forText(value1)), new String[]{value2}, new String[]{null}, algorithmName, null, null, null);
    }

    /* access modifiers changed from: package-private */
    public Boolean getFullScreenMode() {
        enforceCallingPermissionForManagement();
        return Helper.sFullScreenMode;
    }

    /* access modifiers changed from: package-private */
    public void setFullScreenMode(Boolean mode) {
        enforceCallingPermissionForManagement();
        Helper.sFullScreenMode = mode;
    }

    /* access modifiers changed from: package-private */
    public void setTemporaryAugmentedAutofillService(int userId, String serviceName, int durationMs) {
        String str = this.mTag;
        Slog.i(str, "setTemporaryAugmentedAutofillService(" + userId + ") to " + serviceName + " for " + durationMs + "ms");
        enforceCallingPermissionForManagement();
        Preconditions.checkNotNull(serviceName);
        if (durationMs <= 120000) {
            this.mAugmentedAutofillResolver.setTemporaryService(userId, serviceName, durationMs);
            return;
        }
        throw new IllegalArgumentException("Max duration is 120000 (called with " + durationMs + ")");
    }

    /* access modifiers changed from: package-private */
    public void resetTemporaryAugmentedAutofillService(int userId) {
        enforceCallingPermissionForManagement();
        this.mAugmentedAutofillResolver.resetTemporaryService(userId);
    }

    /* access modifiers changed from: package-private */
    public boolean isDefaultAugmentedServiceEnabled(int userId) {
        enforceCallingPermissionForManagement();
        return this.mAugmentedAutofillResolver.isDefaultServiceEnabled(userId);
    }

    /* access modifiers changed from: package-private */
    public boolean setDefaultAugmentedServiceEnabled(int userId, boolean enabled) {
        String str = this.mTag;
        Slog.i(str, "setDefaultAugmentedServiceEnabled() for userId " + userId + ": " + enabled);
        enforceCallingPermissionForManagement();
        synchronized (this.mLock) {
            AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) getServiceForUserLocked(userId);
            if (service != null) {
                if (this.mAugmentedAutofillResolver.setDefaultServiceEnabled(userId, enabled)) {
                    service.updateRemoteAugmentedAutofillService();
                    return true;
                } else if (this.debug) {
                    Slog.d(TAG, "setDefaultAugmentedServiceEnabled(): already " + enabled);
                }
            }
            return false;
        }
    }

    private void setLoggingLevelsLocked(boolean debug, boolean verbose) {
        Helper.sDebug = debug;
        Helper.sDebug = debug;
        this.debug = debug;
        Helper.sVerbose = verbose;
        Helper.sVerbose = verbose;
        this.verbose = verbose;
    }

    private void addCompatibilityModeRequestsLocked(AutofillManagerServiceImpl service, int userId) {
        this.mAutofillCompatState.reset(userId);
        ArrayMap<String, Long> compatPackages = service.getCompatibilityPackagesLocked();
        if (!(compatPackages == null || compatPackages.isEmpty())) {
            Map<String, String[]> whiteListedPackages = getWhitelistedCompatModePackages();
            int compatPackageCount = compatPackages.size();
            for (int i = 0; i < compatPackageCount; i++) {
                String packageName = compatPackages.keyAt(i);
                if (whiteListedPackages == null || !whiteListedPackages.containsKey(packageName)) {
                    Slog.w(TAG, "Ignoring not whitelisted compat package " + packageName);
                } else {
                    Long maxVersionCode = compatPackages.valueAt(i);
                    if (maxVersionCode != null) {
                        this.mAutofillCompatState.addCompatibilityModeRequest(packageName, maxVersionCode.longValue(), whiteListedPackages.get(packageName), userId);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getWhitelistedCompatModePackagesFromSettings() {
        return Settings.Global.getString(getContext().getContentResolver(), "autofill_compat_mode_allowed_packages");
    }

    private Map<String, String[]> getWhitelistedCompatModePackages() {
        return getWhitelistedCompatModePackages(getWhitelistedCompatModePackagesFromSettings());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void send(IResultReceiver receiver, int value) {
        try {
            receiver.send(value, (Bundle) null);
        } catch (RemoteException e) {
            Slog.w(TAG, "Error async reporting result to client: " + e);
        }
    }

    private void send(IResultReceiver receiver, Bundle value) {
        try {
            receiver.send(0, value);
        } catch (RemoteException e) {
            Slog.w(TAG, "Error async reporting result to client: " + e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void send(IResultReceiver receiver, String value) {
        send(receiver, SyncResultReceiver.bundleFor(value));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void send(IResultReceiver receiver, String[] value) {
        send(receiver, SyncResultReceiver.bundleFor(value));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void send(IResultReceiver receiver, Parcelable value) {
        send(receiver, SyncResultReceiver.bundleFor(value));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void send(IResultReceiver receiver, boolean value) {
        send(receiver, value ? 1 : 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void send(IResultReceiver receiver, int value1, int value2) {
        try {
            receiver.send(value1, SyncResultReceiver.bundleFor(value2));
        } catch (RemoteException e) {
            Slog.w(TAG, "Error async reporting result to client: " + e);
        }
    }

    @VisibleForTesting
    static Map<String, String[]> getWhitelistedCompatModePackages(String setting) {
        List<String> urlBarIds;
        String packageName;
        if (TextUtils.isEmpty(setting)) {
            return null;
        }
        ArrayMap<String, String[]> compatPackages = new ArrayMap<>();
        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(COMPAT_PACKAGE_DELIMITER);
        splitter.setString(setting);
        while (splitter.hasNext()) {
            String packageBlock = splitter.next();
            int urlBlockIndex = packageBlock.indexOf(91);
            if (urlBlockIndex == -1) {
                packageName = packageBlock;
                urlBarIds = null;
            } else if (packageBlock.charAt(packageBlock.length() - 1) != ']') {
                Slog.w(TAG, "Ignoring entry '" + packageBlock + "' on '" + setting + "'because it does not end on '" + COMPAT_PACKAGE_URL_IDS_BLOCK_END + "'");
            } else {
                packageName = packageBlock.substring(0, urlBlockIndex);
                List<String> urlBarIds2 = new ArrayList<>();
                String urlBarIdsBlock = packageBlock.substring(urlBlockIndex + 1, packageBlock.length() - 1);
                if (Helper.sVerbose) {
                    Slog.v(TAG, "pkg:" + packageName + ": block:" + packageBlock + ": urls:" + urlBarIds2 + ": block:" + urlBarIdsBlock + ":");
                }
                TextUtils.SimpleStringSplitter splitter2 = new TextUtils.SimpleStringSplitter(COMPAT_PACKAGE_URL_IDS_DELIMITER);
                splitter2.setString(urlBarIdsBlock);
                while (splitter2.hasNext()) {
                    urlBarIds2.add(splitter2.next());
                }
                urlBarIds = urlBarIds2;
            }
            if (urlBarIds == null) {
                compatPackages.put(packageName, null);
            } else {
                String[] urlBarIdsArray = new String[urlBarIds.size()];
                urlBarIds.toArray(urlBarIdsArray);
                compatPackages.put(packageName, urlBarIdsArray);
            }
        }
        return compatPackages;
    }

    public static int getPartitionMaxCount() {
        int i;
        synchronized (sLock) {
            i = sPartitionMaxCount;
        }
        return i;
    }

    public static int getVisibleDatasetsMaxCount() {
        int i;
        synchronized (sLock) {
            i = sVisibleDatasetsMaxCount;
        }
        return i;
    }

    private final class LocalService extends AutofillManagerInternal {
        private LocalService() {
        }

        public void onBackKeyPressed() {
            if (Helper.sDebug) {
                Slog.d(AutofillManagerService.TAG, "onBackKeyPressed()");
            }
            AutofillManagerService.this.mUi.hideAll(null);
            synchronized (AutofillManagerService.this.mLock) {
                ((AutofillManagerServiceImpl) AutofillManagerService.this.getServiceForUserLocked(UserHandle.getCallingUserId())).onBackKeyPressed();
            }
        }

        public AutofillOptions getAutofillOptions(String packageName, long versionCode, int userId) {
            int loggingLevel;
            if (AutofillManagerService.this.verbose) {
                loggingLevel = 6;
            } else if (AutofillManagerService.this.debug) {
                loggingLevel = 2;
            } else {
                loggingLevel = 0;
            }
            AutofillOptions options = new AutofillOptions(loggingLevel, AutofillManagerService.this.mAutofillCompatState.isCompatibilityModeRequested(packageName, versionCode, userId));
            AutofillManagerService.this.mAugmentedAutofillState.injectAugmentedAutofillInfo(options, userId, packageName);
            return options;
        }

        public boolean isAugmentedAutofillServiceForUser(int callingUid, int userId) {
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service == null) {
                    return false;
                }
                return service.isAugmentedAutofillServiceForUserLocked(callingUid);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static final class PackageCompatState {
        private final long maxVersionCode;
        private final String[] urlBarResourceIds;

        PackageCompatState(long maxVersionCode2, String[] urlBarResourceIds2) {
            this.maxVersionCode = maxVersionCode2;
            this.urlBarResourceIds = urlBarResourceIds2;
        }

        public String toString() {
            return "maxVersionCode=" + this.maxVersionCode + ", urlBarResourceIds=" + Arrays.toString(this.urlBarResourceIds);
        }
    }

    /* access modifiers changed from: package-private */
    public static final class AutofillCompatState {
        private final Object mLock = new Object();
        @GuardedBy({"mLock"})
        private SparseArray<ArrayMap<String, PackageCompatState>> mUserSpecs;

        AutofillCompatState() {
        }

        /* access modifiers changed from: package-private */
        public boolean isCompatibilityModeRequested(String packageName, long versionCode, int userId) {
            synchronized (this.mLock) {
                boolean z = false;
                if (this.mUserSpecs == null) {
                    return false;
                }
                ArrayMap<String, PackageCompatState> userSpec = this.mUserSpecs.get(userId);
                if (userSpec == null) {
                    return false;
                }
                PackageCompatState metadata = userSpec.get(packageName);
                if (metadata == null) {
                    return false;
                }
                if (versionCode <= metadata.maxVersionCode) {
                    z = true;
                }
                return z;
            }
        }

        /* access modifiers changed from: package-private */
        public String[] getUrlBarResourceIds(String packageName, int userId) {
            synchronized (this.mLock) {
                if (this.mUserSpecs == null) {
                    return null;
                }
                ArrayMap<String, PackageCompatState> userSpec = this.mUserSpecs.get(userId);
                if (userSpec == null) {
                    return null;
                }
                PackageCompatState metadata = userSpec.get(packageName);
                if (metadata == null) {
                    return null;
                }
                return metadata.urlBarResourceIds;
            }
        }

        /* access modifiers changed from: package-private */
        public void addCompatibilityModeRequest(String packageName, long versionCode, String[] urlBarResourceIds, int userId) {
            synchronized (this.mLock) {
                if (this.mUserSpecs == null) {
                    this.mUserSpecs = new SparseArray<>();
                }
                ArrayMap<String, PackageCompatState> userSpec = this.mUserSpecs.get(userId);
                if (userSpec == null) {
                    userSpec = new ArrayMap<>();
                    this.mUserSpecs.put(userId, userSpec);
                }
                userSpec.put(packageName, new PackageCompatState(versionCode, urlBarResourceIds));
            }
        }

        /* access modifiers changed from: package-private */
        public void removeCompatibilityModeRequests(int userId) {
            synchronized (this.mLock) {
                if (this.mUserSpecs != null) {
                    this.mUserSpecs.remove(userId);
                    if (this.mUserSpecs.size() <= 0) {
                        this.mUserSpecs = null;
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void reset(int userId) {
            synchronized (this.mLock) {
                if (this.mUserSpecs != null) {
                    this.mUserSpecs.delete(userId);
                    int newSize = this.mUserSpecs.size();
                    if (newSize == 0) {
                        if (Helper.sVerbose) {
                            Slog.v(AutofillManagerService.TAG, "reseting mUserSpecs");
                        }
                        this.mUserSpecs = null;
                    } else if (Helper.sVerbose) {
                        Slog.v(AutofillManagerService.TAG, "mUserSpecs down to " + newSize);
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void dump(String prefix, PrintWriter pw) {
            synchronized (this.mLock) {
                if (this.mUserSpecs == null) {
                    pw.println("N/A");
                    return;
                }
                pw.println();
                String prefix2 = prefix + "  ";
                for (int i = 0; i < this.mUserSpecs.size(); i++) {
                    int user = this.mUserSpecs.keyAt(i);
                    pw.print(prefix);
                    pw.print("User: ");
                    pw.println(user);
                    ArrayMap<String, PackageCompatState> perUser = this.mUserSpecs.valueAt(i);
                    for (int j = 0; j < perUser.size(); j++) {
                        pw.print(prefix2);
                        pw.print(perUser.keyAt(j));
                        pw.print(": ");
                        pw.println(perUser.valueAt(j));
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static final class AugmentedAutofillState extends GlobalWhitelistState {
        @GuardedBy({"mGlobalWhitelistStateLock"})
        private final SparseArray<String> mServicePackages = new SparseArray<>();
        @GuardedBy({"mGlobalWhitelistStateLock"})
        private final SparseBooleanArray mTemporaryServices = new SparseBooleanArray();

        AugmentedAutofillState() {
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setServiceInfo(int userId, String serviceName, boolean isTemporary) {
            synchronized (this.mGlobalWhitelistStateLock) {
                if (isTemporary) {
                    this.mTemporaryServices.put(userId, true);
                } else {
                    this.mTemporaryServices.delete(userId);
                }
                if (serviceName != null) {
                    ComponentName componentName = ComponentName.unflattenFromString(serviceName);
                    if (componentName == null) {
                        Slog.w(AutofillManagerService.TAG, "setServiceInfo(): invalid name: " + serviceName);
                        this.mServicePackages.remove(userId);
                    } else {
                        this.mServicePackages.put(userId, componentName.getPackageName());
                    }
                } else {
                    this.mServicePackages.remove(userId);
                }
            }
        }

        public void injectAugmentedAutofillInfo(AutofillOptions options, int userId, String packageName) {
            synchronized (this.mGlobalWhitelistStateLock) {
                if (this.mWhitelisterHelpers != null) {
                    WhitelistHelper helper = (WhitelistHelper) this.mWhitelisterHelpers.get(userId);
                    if (helper != null) {
                        options.augmentedAutofillEnabled = helper.isWhitelisted(packageName);
                        options.whitelistedActivitiesForAugmentedAutofill = helper.getWhitelistedComponents(packageName);
                    }
                }
            }
        }

        public boolean isWhitelisted(int userId, ComponentName componentName) {
            synchronized (this.mGlobalWhitelistStateLock) {
                if (!AutofillManagerService.super.isWhitelisted(userId, componentName)) {
                    return false;
                }
                if (Build.IS_USER && this.mTemporaryServices.get(userId)) {
                    String packageName = componentName.getPackageName();
                    if (!packageName.equals(this.mServicePackages.get(userId))) {
                        Slog.w(AutofillManagerService.TAG, "Ignoring package " + packageName + " for augmented autofill while using temporary service " + this.mServicePackages.get(userId));
                        return false;
                    }
                }
                return true;
            }
        }

        public void dump(String prefix, PrintWriter pw) {
            AutofillManagerService.super.dump(prefix, pw);
            synchronized (this.mGlobalWhitelistStateLock) {
                if (this.mServicePackages.size() > 0) {
                    pw.print(prefix);
                    pw.print("Service packages: ");
                    pw.println(this.mServicePackages);
                }
                if (this.mTemporaryServices.size() > 0) {
                    pw.print(prefix);
                    pw.print("Temp services: ");
                    pw.println(this.mTemporaryServices);
                }
            }
        }
    }

    final class AutoFillManagerServiceStub extends IAutoFillManager.Stub {
        AutoFillManagerServiceStub() {
        }

        public void addClient(IAutoFillManagerClient client, ComponentName componentName, int userId, IResultReceiver receiver) {
            int flags = 0;
            synchronized (AutofillManagerService.this.mLock) {
                int enabledFlags = ((AutofillManagerServiceImpl) AutofillManagerService.this.getServiceForUserLocked(userId)).addClientLocked(client, componentName);
                if (enabledFlags != 0) {
                    flags = 0 | enabledFlags;
                }
                if (Helper.sDebug) {
                    flags |= 2;
                }
                if (Helper.sVerbose) {
                    flags |= 4;
                }
            }
            AutofillManagerService.this.send(receiver, flags);
        }

        public void removeClient(IAutoFillManagerClient client, int userId) {
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    service.removeClientLocked(client);
                } else if (Helper.sVerbose) {
                    Slog.v(AutofillManagerService.TAG, "removeClient(): no service for " + userId);
                }
            }
        }

        public void setAuthenticationResult(Bundle data, int sessionId, int authenticationId, int userId) {
            synchronized (AutofillManagerService.this.mLock) {
                ((AutofillManagerServiceImpl) AutofillManagerService.this.getServiceForUserLocked(userId)).setAuthenticationResultLocked(data, sessionId, authenticationId, getCallingUid());
            }
        }

        public void setHasCallback(int sessionId, int userId, boolean hasIt) {
            synchronized (AutofillManagerService.this.mLock) {
                ((AutofillManagerServiceImpl) AutofillManagerService.this.getServiceForUserLocked(userId)).setHasCallback(sessionId, getCallingUid(), hasIt);
            }
        }

        public void startSession(IBinder activityToken, IBinder appCallback, AutofillId autofillId, Rect bounds, AutofillValue value, int userId, boolean hasCallback, int flags, ComponentName componentName, boolean compatMode, IResultReceiver receiver) {
            Throwable th;
            long result;
            IBinder activityToken2 = (IBinder) Preconditions.checkNotNull(activityToken, "activityToken");
            IBinder appCallback2 = (IBinder) Preconditions.checkNotNull(appCallback, "appCallback");
            AutofillId autofillId2 = (AutofillId) Preconditions.checkNotNull(autofillId, "autoFillId");
            ComponentName componentName2 = (ComponentName) Preconditions.checkNotNull(componentName, "componentName");
            String packageName = (String) Preconditions.checkNotNull(componentName2.getPackageName());
            Preconditions.checkArgument(userId == UserHandle.getUserId(getCallingUid()), "userId");
            try {
                AutofillManagerService.this.getContext().getPackageManager().getPackageInfoAsUser(packageName, 0, userId);
                int taskId = AutofillManagerService.this.mAm.getTaskIdForActivity(activityToken2, false);
                synchronized (AutofillManagerService.this.mLock) {
                    try {
                        try {
                            result = ((AutofillManagerServiceImpl) AutofillManagerService.this.getServiceForUserLocked(userId)).startSessionLocked(activityToken2, taskId, getCallingUid(), appCallback2, autofillId2, bounds, value, hasCallback, componentName2, compatMode, AutofillManagerService.this.mAllowInstantService, flags);
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
                int sessionId = (int) result;
                int resultFlags = (int) (result >> 32);
                if (resultFlags != 0) {
                    AutofillManagerService.this.send(receiver, sessionId, resultFlags);
                } else {
                    AutofillManagerService.this.send(receiver, sessionId);
                }
            } catch (PackageManager.NameNotFoundException e) {
                throw new IllegalArgumentException(packageName + " is not a valid package", e);
            }
        }

        public void getFillEventHistory(IResultReceiver receiver) throws RemoteException {
            int userId = UserHandle.getCallingUserId();
            FillEventHistory fillEventHistory = null;
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    fillEventHistory = service.getFillEventHistory(getCallingUid());
                } else if (Helper.sVerbose) {
                    Slog.v(AutofillManagerService.TAG, "getFillEventHistory(): no service for " + userId);
                }
            }
            AutofillManagerService.this.send(receiver, fillEventHistory);
        }

        public void getUserData(IResultReceiver receiver) throws RemoteException {
            int userId = UserHandle.getCallingUserId();
            UserData userData = null;
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    userData = service.getUserData(getCallingUid());
                } else if (Helper.sVerbose) {
                    Slog.v(AutofillManagerService.TAG, "getUserData(): no service for " + userId);
                }
            }
            AutofillManagerService.this.send(receiver, userData);
        }

        public void getUserDataId(IResultReceiver receiver) throws RemoteException {
            int userId = UserHandle.getCallingUserId();
            UserData userData = null;
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    userData = service.getUserData(getCallingUid());
                } else if (Helper.sVerbose) {
                    Slog.v(AutofillManagerService.TAG, "getUserDataId(): no service for " + userId);
                }
            }
            AutofillManagerService.this.send(receiver, userData == null ? null : userData.getId());
        }

        public void setUserData(UserData userData) throws RemoteException {
            int userId = UserHandle.getCallingUserId();
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    service.setUserData(getCallingUid(), userData);
                } else if (Helper.sVerbose) {
                    Slog.v(AutofillManagerService.TAG, "setUserData(): no service for " + userId);
                }
            }
        }

        public void isFieldClassificationEnabled(IResultReceiver receiver) throws RemoteException {
            int userId = UserHandle.getCallingUserId();
            boolean enabled = false;
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    enabled = service.isFieldClassificationEnabled(getCallingUid());
                } else if (Helper.sVerbose) {
                    Slog.v(AutofillManagerService.TAG, "isFieldClassificationEnabled(): no service for " + userId);
                }
            }
            AutofillManagerService.this.send(receiver, enabled);
        }

        public void getDefaultFieldClassificationAlgorithm(IResultReceiver receiver) throws RemoteException {
            int userId = UserHandle.getCallingUserId();
            String algorithm = null;
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    algorithm = service.getDefaultFieldClassificationAlgorithm(getCallingUid());
                } else if (Helper.sVerbose) {
                    Slog.v(AutofillManagerService.TAG, "getDefaultFcAlgorithm(): no service for " + userId);
                }
            }
            AutofillManagerService.this.send(receiver, algorithm);
        }

        public void setAugmentedAutofillWhitelist(List<String> packages, List<ComponentName> activities, IResultReceiver receiver) throws RemoteException {
            int i;
            boolean ok;
            int userId = UserHandle.getCallingUserId();
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) AutofillManagerService.this.peekServiceForUserLocked(userId);
                i = 0;
                if (service != null) {
                    ok = service.setAugmentedAutofillWhitelistLocked(packages, activities, getCallingUid());
                } else {
                    if (Helper.sVerbose) {
                        Slog.v(AutofillManagerService.TAG, "setAugmentedAutofillWhitelist(): no service for " + userId);
                    }
                    ok = false;
                }
            }
            AutofillManagerService autofillManagerService = AutofillManagerService.this;
            if (!ok) {
                i = -1;
            }
            autofillManagerService.send(receiver, i);
        }

        public void getAvailableFieldClassificationAlgorithms(IResultReceiver receiver) throws RemoteException {
            int userId = UserHandle.getCallingUserId();
            String[] algorithms = null;
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    algorithms = service.getAvailableFieldClassificationAlgorithms(getCallingUid());
                } else if (Helper.sVerbose) {
                    Slog.v(AutofillManagerService.TAG, "getAvailableFcAlgorithms(): no service for " + userId);
                }
            }
            AutofillManagerService.this.send(receiver, algorithms);
        }

        public void getAutofillServiceComponentName(IResultReceiver receiver) throws RemoteException {
            int userId = UserHandle.getCallingUserId();
            ComponentName componentName = null;
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    componentName = service.getServiceComponentName();
                } else if (Helper.sVerbose) {
                    Slog.v(AutofillManagerService.TAG, "getAutofillServiceComponentName(): no service for " + userId);
                }
            }
            AutofillManagerService.this.send(receiver, componentName);
        }

        public void restoreSession(int sessionId, IBinder activityToken, IBinder appCallback, IResultReceiver receiver) throws RemoteException {
            int userId = UserHandle.getCallingUserId();
            IBinder activityToken2 = (IBinder) Preconditions.checkNotNull(activityToken, "activityToken");
            IBinder appCallback2 = (IBinder) Preconditions.checkNotNull(appCallback, "appCallback");
            boolean restored = false;
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    restored = service.restoreSession(sessionId, getCallingUid(), activityToken2, appCallback2);
                } else if (Helper.sVerbose) {
                    Slog.v(AutofillManagerService.TAG, "restoreSession(): no service for " + userId);
                }
            }
            AutofillManagerService.this.send(receiver, restored);
        }

        public void updateSession(int sessionId, AutofillId autoFillId, Rect bounds, AutofillValue value, int action, int flags, int userId) {
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    service.updateSessionLocked(sessionId, getCallingUid(), autoFillId, bounds, value, action, flags);
                } else if (Helper.sVerbose) {
                    Slog.v(AutofillManagerService.TAG, "updateSession(): no service for " + userId);
                }
            }
        }

        public void setAutofillFailure(int sessionId, List<AutofillId> ids, int userId) {
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    service.setAutofillFailureLocked(sessionId, getCallingUid(), ids);
                } else if (Helper.sVerbose) {
                    Slog.v(AutofillManagerService.TAG, "setAutofillFailure(): no service for " + userId);
                }
            }
        }

        public void finishSession(int sessionId, int userId) {
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    service.finishSessionLocked(sessionId, getCallingUid());
                } else if (Helper.sVerbose) {
                    Slog.v(AutofillManagerService.TAG, "finishSession(): no service for " + userId);
                }
            }
        }

        public void cancelSession(int sessionId, int userId) {
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    service.cancelSessionLocked(sessionId, getCallingUid());
                } else if (Helper.sVerbose) {
                    Slog.v(AutofillManagerService.TAG, "cancelSession(): no service for " + userId);
                }
            }
        }

        public void disableOwnedAutofillServices(int userId) {
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    service.disableOwnedAutofillServicesLocked(Binder.getCallingUid());
                } else if (Helper.sVerbose) {
                    Slog.v(AutofillManagerService.TAG, "cancelSession(): no service for " + userId);
                }
            }
        }

        public void isServiceSupported(int userId, IResultReceiver receiver) {
            boolean supported;
            synchronized (AutofillManagerService.this.mLock) {
                supported = !AutofillManagerService.this.isDisabledLocked(userId);
            }
            AutofillManagerService.this.send(receiver, supported);
        }

        public void isServiceEnabled(int userId, String packageName, IResultReceiver receiver) {
            boolean enabled = false;
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    enabled = Objects.equals(packageName, service.getServicePackageName());
                } else if (Helper.sVerbose) {
                    Slog.v(AutofillManagerService.TAG, "isServiceEnabled(): no service for " + userId);
                }
            }
            AutofillManagerService.this.send(receiver, enabled);
        }

        public void onPendingSaveUi(int operation, IBinder token) {
            Preconditions.checkNotNull(token, "token");
            Preconditions.checkArgument(operation == 1 || operation == 2, "invalid operation: %d", new Object[]{Integer.valueOf(operation)});
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) AutofillManagerService.this.peekServiceForUserLocked(UserHandle.getCallingUserId());
                if (service != null) {
                    service.onPendingSaveUi(operation, token);
                }
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(AutofillManagerService.this.getContext(), AutofillManagerService.TAG, pw)) {
                boolean showHistory = true;
                boolean showHistory2 = false;
                if (args != null) {
                    boolean uiOnly = false;
                    boolean showHistory3 = true;
                    for (String arg : args) {
                        char c = 65535;
                        int hashCode = arg.hashCode();
                        if (hashCode != 900765093) {
                            if (hashCode != 1098711592) {
                                if (hashCode == 1333069025 && arg.equals("--help")) {
                                    c = 2;
                                }
                            } else if (arg.equals("--no-history")) {
                                c = 0;
                            }
                        } else if (arg.equals("--ui-only")) {
                            c = 1;
                        }
                        if (c == 0) {
                            showHistory3 = false;
                        } else if (c == 1) {
                            uiOnly = true;
                        } else if (c != 2) {
                            Slog.w(AutofillManagerService.TAG, "Ignoring invalid dump arg: " + arg);
                        } else {
                            pw.println("Usage: dumpsys autofill [--ui-only|--no-history]");
                            return;
                        }
                    }
                    showHistory = showHistory3;
                    showHistory2 = uiOnly;
                }
                if (showHistory2) {
                    AutofillManagerService.this.mUi.dump(pw);
                    return;
                }
                boolean realDebug = Helper.sDebug;
                boolean realVerbose = Helper.sVerbose;
                try {
                    Helper.sVerbose = true;
                    Helper.sDebug = true;
                    synchronized (AutofillManagerService.this.mLock) {
                        pw.print("sDebug: ");
                        pw.print(realDebug);
                        pw.print(" sVerbose: ");
                        pw.println(realVerbose);
                        AutofillManagerService.this.dumpLocked("", pw);
                        AutofillManagerService.this.mAugmentedAutofillResolver.dumpShort(pw);
                        pw.println();
                        pw.print("Max partitions per session: ");
                        pw.println(AutofillManagerService.sPartitionMaxCount);
                        pw.print("Max visible datasets: ");
                        pw.println(AutofillManagerService.sVisibleDatasetsMaxCount);
                        if (Helper.sFullScreenMode != null) {
                            pw.print("Overridden full-screen mode: ");
                            pw.println(Helper.sFullScreenMode);
                        }
                        pw.println("User data constraints: ");
                        UserData.dumpConstraints("  ", pw);
                        AutofillManagerService.this.mUi.dump(pw);
                        pw.print("Autofill Compat State: ");
                        AutofillManagerService.this.mAutofillCompatState.dump("  ", pw);
                        pw.print("from settings: ");
                        pw.println(AutofillManagerService.this.getWhitelistedCompatModePackagesFromSettings());
                        if (AutofillManagerService.this.mSupportedSmartSuggestionModes != 0) {
                            pw.print("Smart Suggestion modes: ");
                            pw.println(AutofillManager.getSmartSuggestionModeToString(AutofillManagerService.this.mSupportedSmartSuggestionModes));
                        }
                        pw.print("Augmented Service Idle Unbind Timeout: ");
                        pw.println(AutofillManagerService.this.mAugmentedServiceIdleUnbindTimeoutMs);
                        pw.print("Augmented Service Request Timeout: ");
                        pw.println(AutofillManagerService.this.mAugmentedServiceRequestTimeoutMs);
                        if (showHistory) {
                            pw.println();
                            pw.println("Requests history:");
                            pw.println();
                            AutofillManagerService.this.mRequestsHistory.reverseDump(fd, pw, args);
                            pw.println();
                            pw.println("UI latency history:");
                            pw.println();
                            AutofillManagerService.this.mUiLatencyHistory.reverseDump(fd, pw, args);
                            pw.println();
                            pw.println("WTF history:");
                            pw.println();
                            AutofillManagerService.this.mWtfHistory.reverseDump(fd, pw, args);
                        }
                        pw.println("Augmented Autofill State: ");
                        AutofillManagerService.this.mAugmentedAutofillState.dump("  ", pw);
                    }
                } finally {
                    Helper.sDebug = realDebug;
                    Helper.sVerbose = realVerbose;
                }
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.autofill.AutofillManagerService$AutoFillManagerServiceStub */
        /* JADX WARN: Multi-variable type inference failed */
        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new AutofillManagerServiceShellCommand(AutofillManagerService.this).exec(this, in, out, err, args, callback, resultReceiver);
        }
    }
}

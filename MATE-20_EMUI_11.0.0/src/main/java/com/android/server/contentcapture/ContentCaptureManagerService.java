package com.android.server.contentcapture;

import android.app.ActivityManagerInternal;
import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.ContentCaptureOptions;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityPresentationInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.util.ArraySet;
import android.util.LocalLog;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.contentcapture.ContentCaptureCondition;
import android.view.contentcapture.ContentCaptureHelper;
import android.view.contentcapture.DataRemovalRequest;
import android.view.contentcapture.IContentCaptureManager;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.infra.GlobalWhitelistState;
import com.android.internal.os.IResultReceiver;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.Preconditions;
import com.android.internal.util.SyncResultReceiver;
import com.android.server.LocalServices;
import com.android.server.contentcapture.ContentCaptureManagerService;
import com.android.server.infra.AbstractMasterSystemService;
import com.android.server.infra.FrameworkResourcesServiceNameResolver;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public final class ContentCaptureManagerService extends AbstractMasterSystemService<ContentCaptureManagerService, ContentCapturePerUserService> {
    private static final int MAX_TEMP_SERVICE_DURATION_MS = 120000;
    static final String RECEIVER_BUNDLE_EXTRA_SESSIONS = "sessions";
    @GuardedBy({"mLock"})
    private ActivityManagerInternal mAm;
    @GuardedBy({"mLock"})
    int mDevCfgIdleFlushingFrequencyMs;
    @GuardedBy({"mLock"})
    int mDevCfgIdleUnbindTimeoutMs;
    @GuardedBy({"mLock"})
    int mDevCfgLogHistorySize;
    @GuardedBy({"mLock"})
    int mDevCfgLoggingLevel;
    @GuardedBy({"mLock"})
    int mDevCfgMaxBufferSize;
    @GuardedBy({"mLock"})
    int mDevCfgTextChangeFlushingFrequencyMs;
    @GuardedBy({"mLock"})
    private boolean mDisabledByDeviceConfig;
    @GuardedBy({"mLock"})
    private SparseBooleanArray mDisabledBySettings;
    final GlobalContentCaptureOptions mGlobalContentCaptureOptions = new GlobalContentCaptureOptions();
    private final LocalService mLocalService = new LocalService();
    final LocalLog mRequestsHistory;

    public ContentCaptureManagerService(Context context) {
        super(context, new FrameworkResourcesServiceNameResolver(context, 17039820), "no_content_capture", 0);
        DeviceConfig.addOnPropertiesChangedListener("content_capture", ActivityThread.currentApplication().getMainExecutor(), new DeviceConfig.OnPropertiesChangedListener() {
            /* class com.android.server.contentcapture.$$Lambda$ContentCaptureManagerService$4nadnpI0ImgQseJYN0WTE4IJ4s4 */

            public final void onPropertiesChanged(DeviceConfig.Properties properties) {
                ContentCaptureManagerService.this.lambda$new$0$ContentCaptureManagerService(properties);
            }
        });
        setDeviceConfigProperties();
        if (this.mDevCfgLogHistorySize > 0) {
            if (this.debug) {
                String str = this.mTag;
                Slog.d(str, "log history size: " + this.mDevCfgLogHistorySize);
            }
            this.mRequestsHistory = new LocalLog(this.mDevCfgLogHistorySize);
        } else {
            if (this.debug) {
                String str2 = this.mTag;
                Slog.d(str2, "disabled log history because size is " + this.mDevCfgLogHistorySize);
            }
            this.mRequestsHistory = null;
        }
        List<UserInfo> users = ((UserManager) getContext().getSystemService(UserManager.class)).getUsers();
        for (int i = 0; i < users.size(); i++) {
            int userId = users.get(i).id;
            if (!isEnabledBySettings(userId)) {
                String str3 = this.mTag;
                Slog.i(str3, "user " + userId + " disabled by settings");
                if (this.mDisabledBySettings == null) {
                    this.mDisabledBySettings = new SparseBooleanArray(1);
                }
                this.mDisabledBySettings.put(userId, true);
            }
            this.mGlobalContentCaptureOptions.setServiceInfo(userId, this.mServiceNameResolver.getServiceName(userId), this.mServiceNameResolver.isTemporary(userId));
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractMasterSystemService
    public ContentCapturePerUserService newServiceLocked(int resolvedUserId, boolean disabled) {
        return new ContentCapturePerUserService(this, this.mLock, disabled, resolvedUserId);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.contentcapture.ContentCaptureManagerService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.contentcapture.ContentCaptureManagerService$ContentCaptureManagerServiceStub, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("content_capture", new ContentCaptureManagerServiceStub());
        publishLocalService(ContentCaptureManagerInternal.class, this.mLocalService);
    }

    /* access modifiers changed from: protected */
    public void onServiceRemoved(ContentCapturePerUserService service, int userId) {
        service.destroyLocked();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractMasterSystemService
    public void onServicePackageUpdatingLocked(int userId) {
        ContentCapturePerUserService service = (ContentCapturePerUserService) getServiceForUserLocked(userId);
        if (service != null) {
            service.onPackageUpdatingLocked();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractMasterSystemService
    public void onServicePackageUpdatedLocked(int userId) {
        ContentCapturePerUserService service = (ContentCapturePerUserService) getServiceForUserLocked(userId);
        if (service != null) {
            service.onPackageUpdatedLocked();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractMasterSystemService
    public void onServiceNameChanged(int userId, String serviceName, boolean isTemporary) {
        this.mGlobalContentCaptureOptions.setServiceInfo(userId, serviceName, isTemporary);
        super.lambda$new$0$AbstractMasterSystemService(userId, serviceName, isTemporary);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractMasterSystemService
    public void enforceCallingPermissionForManagement() {
        getContext().enforceCallingPermission("android.permission.MANAGE_CONTENT_CAPTURE", this.mTag);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractMasterSystemService
    public int getMaximumTemporaryServiceDurationMs() {
        return MAX_TEMP_SERVICE_DURATION_MS;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractMasterSystemService
    public void registerForExtraSettingsChanges(ContentResolver resolver, ContentObserver observer) {
        resolver.registerContentObserver(Settings.Secure.getUriFor("content_capture_enabled"), false, observer, -1);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractMasterSystemService
    public void onSettingsChanged(int userId, String property) {
        if (((property.hashCode() == -322385022 && property.equals("content_capture_enabled")) ? (char) 0 : 65535) != 0) {
            String str = this.mTag;
            Slog.w(str, "Unexpected property (" + property + "); updating cache instead");
            return;
        }
        setContentCaptureFeatureEnabledBySettingsForUser(userId, isEnabledBySettings(userId));
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractMasterSystemService
    public boolean isDisabledLocked(int userId) {
        return this.mDisabledByDeviceConfig || isDisabledBySettingsLocked(userId) || super.isDisabledLocked(userId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDisabledBySettingsLocked(int userId) {
        SparseBooleanArray sparseBooleanArray = this.mDisabledBySettings;
        return sparseBooleanArray != null && sparseBooleanArray.get(userId);
    }

    private boolean isEnabledBySettings(int userId) {
        boolean enabled = true;
        if (Settings.Secure.getIntForUser(getContext().getContentResolver(), "content_capture_enabled", 1, userId) != 1) {
            enabled = false;
        }
        return enabled;
    }

    /* access modifiers changed from: private */
    /* renamed from: onDeviceConfigChange */
    public void lambda$new$0$ContentCaptureManagerService(DeviceConfig.Properties properties) {
        for (String key : properties.getKeyset()) {
            char c = 65535;
            switch (key.hashCode()) {
                case -1970239836:
                    if (key.equals("logging_level")) {
                        c = 1;
                        break;
                    }
                    break;
                case -302650995:
                    if (key.equals("service_explicitly_enabled")) {
                        c = 0;
                        break;
                    }
                    break;
                case -148969820:
                    if (key.equals("text_change_flush_frequency")) {
                        c = 5;
                        break;
                    }
                    break;
                case 227845607:
                    if (key.equals("log_history_size")) {
                        c = 4;
                        break;
                    }
                    break;
                case 1119140421:
                    if (key.equals("max_buffer_size")) {
                        c = 2;
                        break;
                    }
                    break;
                case 1568835651:
                    if (key.equals("idle_unbind_timeout")) {
                        c = 6;
                        break;
                    }
                    break;
                case 2068460406:
                    if (key.equals("idle_flush_frequency")) {
                        c = 3;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    setDisabledByDeviceConfig(properties.getString(key, (String) null));
                    return;
                case 1:
                    setLoggingLevelFromDeviceConfig();
                    return;
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    setFineTuneParamsFromDeviceConfig();
                    return;
                default:
                    String str = this.mTag;
                    Slog.i(str, "Ignoring change on " + key);
            }
        }
    }

    private void setFineTuneParamsFromDeviceConfig() {
        synchronized (this.mLock) {
            this.mDevCfgMaxBufferSize = DeviceConfig.getInt("content_capture", "max_buffer_size", 100);
            this.mDevCfgIdleFlushingFrequencyMs = DeviceConfig.getInt("content_capture", "idle_flush_frequency", 5000);
            this.mDevCfgTextChangeFlushingFrequencyMs = DeviceConfig.getInt("content_capture", "text_change_flush_frequency", 1000);
            this.mDevCfgLogHistorySize = DeviceConfig.getInt("content_capture", "log_history_size", 20);
            this.mDevCfgIdleUnbindTimeoutMs = DeviceConfig.getInt("content_capture", "idle_unbind_timeout", 0);
            if (this.verbose) {
                String str = this.mTag;
                Slog.v(str, "setFineTuneParamsFromDeviceConfig(): bufferSize=" + this.mDevCfgMaxBufferSize + ", idleFlush=" + this.mDevCfgIdleFlushingFrequencyMs + ", textFluxh=" + this.mDevCfgTextChangeFlushingFrequencyMs + ", logHistory=" + this.mDevCfgLogHistorySize + ", idleUnbindTimeoutMs=" + this.mDevCfgIdleUnbindTimeoutMs);
            }
        }
    }

    private void setLoggingLevelFromDeviceConfig() {
        this.mDevCfgLoggingLevel = DeviceConfig.getInt("content_capture", "logging_level", ContentCaptureHelper.getDefaultLoggingLevel());
        ContentCaptureHelper.setLoggingLevel(this.mDevCfgLoggingLevel);
        this.verbose = ContentCaptureHelper.sVerbose;
        this.debug = ContentCaptureHelper.sDebug;
        if (this.verbose) {
            String str = this.mTag;
            Slog.v(str, "setLoggingLevelFromDeviceConfig(): level=" + this.mDevCfgLoggingLevel + ", debug=" + this.debug + ", verbose=" + this.verbose);
        }
    }

    private void setDeviceConfigProperties() {
        setLoggingLevelFromDeviceConfig();
        setFineTuneParamsFromDeviceConfig();
        setDisabledByDeviceConfig(DeviceConfig.getProperty("content_capture", "service_explicitly_enabled"));
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x00ac  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00af  */
    private void setDisabledByDeviceConfig(String explicitlyEnabled) {
        boolean newDisabledValue;
        boolean disabled;
        if (this.verbose) {
            Slog.v(this.mTag, "setDisabledByDeviceConfig(): explicitlyEnabled=" + explicitlyEnabled);
        }
        List<UserInfo> users = ((UserManager) getContext().getSystemService(UserManager.class)).getUsers();
        if (explicitlyEnabled == null || !explicitlyEnabled.equalsIgnoreCase("false")) {
            newDisabledValue = false;
        } else {
            newDisabledValue = true;
        }
        synchronized (this.mLock) {
            if (this.mDisabledByDeviceConfig == newDisabledValue) {
                if (this.verbose) {
                    Slog.v(this.mTag, "setDisabledByDeviceConfig(): already " + newDisabledValue);
                }
                return;
            }
            this.mDisabledByDeviceConfig = newDisabledValue;
            Slog.i(this.mTag, "setDisabledByDeviceConfig(): set to " + this.mDisabledByDeviceConfig);
            for (int i = 0; i < users.size(); i++) {
                int userId = users.get(i).id;
                if (!this.mDisabledByDeviceConfig) {
                    if (!isDisabledBySettingsLocked(userId)) {
                        disabled = false;
                        String str = this.mTag;
                        StringBuilder sb = new StringBuilder();
                        sb.append("setDisabledByDeviceConfig(): updating service for user ");
                        sb.append(userId);
                        sb.append(" to ");
                        sb.append(!disabled ? "'disabled'" : "'enabled'");
                        Slog.i(str, sb.toString());
                        updateCachedServiceLocked(userId, disabled);
                    }
                }
                disabled = true;
                String str2 = this.mTag;
                StringBuilder sb2 = new StringBuilder();
                sb2.append("setDisabledByDeviceConfig(): updating service for user ");
                sb2.append(userId);
                sb2.append(" to ");
                sb2.append(!disabled ? "'disabled'" : "'enabled'");
                Slog.i(str2, sb2.toString());
                updateCachedServiceLocked(userId, disabled);
            }
        }
    }

    private void setContentCaptureFeatureEnabledBySettingsForUser(int userId, boolean enabled) {
        synchronized (this.mLock) {
            if (this.mDisabledBySettings == null) {
                this.mDisabledBySettings = new SparseBooleanArray();
            }
            boolean disabled = true;
            if (!(enabled ^ (!this.mDisabledBySettings.get(userId)))) {
                if (this.debug) {
                    String str = this.mTag;
                    Slog.d(str, "setContentCaptureFeatureEnabledForUser(): already " + enabled);
                }
                return;
            }
            if (enabled) {
                String str2 = this.mTag;
                Slog.i(str2, "setContentCaptureFeatureEnabled(): enabling service for user " + userId);
                this.mDisabledBySettings.delete(userId);
            } else {
                String str3 = this.mTag;
                Slog.i(str3, "setContentCaptureFeatureEnabled(): disabling service for user " + userId);
                this.mDisabledBySettings.put(userId, true);
            }
            if (enabled) {
                if (!this.mDisabledByDeviceConfig) {
                    disabled = false;
                }
            }
            updateCachedServiceLocked(userId, disabled);
        }
    }

    /* access modifiers changed from: package-private */
    public void destroySessions(int userId, IResultReceiver receiver) {
        String str = this.mTag;
        Slog.i(str, "destroySessions() for userId " + userId);
        enforceCallingPermissionForManagement();
        synchronized (this.mLock) {
            if (userId != -1) {
                ContentCapturePerUserService service = (ContentCapturePerUserService) peekServiceForUserLocked(userId);
                if (service != null) {
                    service.destroySessionsLocked();
                }
            } else {
                visitServicesLocked($$Lambda$ContentCaptureManagerService$jCIcV2sgwD7QUkNc6yfPd58T_U.INSTANCE);
            }
        }
        try {
            receiver.send(0, new Bundle());
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: package-private */
    public void listSessions(int userId, IResultReceiver receiver) {
        String str = this.mTag;
        Slog.i(str, "listSessions() for userId " + userId);
        enforceCallingPermissionForManagement();
        Bundle resultData = new Bundle();
        ArrayList<String> sessions = new ArrayList<>();
        synchronized (this.mLock) {
            if (userId != -1) {
                try {
                    ContentCapturePerUserService service = (ContentCapturePerUserService) peekServiceForUserLocked(userId);
                    if (service != null) {
                        service.listSessionsLocked(sessions);
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                visitServicesLocked(new AbstractMasterSystemService.Visitor(sessions) {
                    /* class com.android.server.contentcapture.$$Lambda$ContentCaptureManagerService$17qcaUpUsTMt5exVwDnmTmyrpJw */
                    private final /* synthetic */ ArrayList f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // com.android.server.infra.AbstractMasterSystemService.Visitor
                    public final void visit(Object obj) {
                        ((ContentCapturePerUserService) obj).listSessionsLocked(this.f$0);
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ActivityManagerInternal getAmInternal() {
        synchronized (this.mLock) {
            if (this.mAm == null) {
                this.mAm = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
            }
        }
        return this.mAm;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void assertCalledByServiceLocked(String methodName) {
        if (!isCalledByServiceLocked(methodName)) {
            throw new SecurityException("caller is not user's ContentCapture service");
        }
    }

    @GuardedBy({"mLock"})
    private boolean isCalledByServiceLocked(String methodName) {
        int userId = UserHandle.getCallingUserId();
        int callingUid = Binder.getCallingUid();
        String serviceName = this.mServiceNameResolver.getServiceName(userId);
        if (serviceName == null) {
            String str = this.mTag;
            Slog.e(str, methodName + ": called by UID " + callingUid + ", but there's no service set for user " + userId);
            return false;
        }
        ComponentName serviceComponent = ComponentName.unflattenFromString(serviceName);
        if (serviceComponent == null) {
            String str2 = this.mTag;
            Slog.w(str2, methodName + ": invalid service name: " + serviceName);
            return false;
        }
        String servicePackageName = serviceComponent.getPackageName();
        try {
            int serviceUid = getContext().getPackageManager().getPackageUidAsUser(servicePackageName, UserHandle.getCallingUserId());
            if (callingUid == serviceUid) {
                return true;
            }
            String str3 = this.mTag;
            Slog.e(str3, methodName + ": called by UID " + callingUid + ", but service UID is " + serviceUid);
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            String str4 = this.mTag;
            Slog.w(str4, methodName + ": could not verify UID for " + serviceName);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean throwsSecurityException(IResultReceiver result, Runnable runable) {
        try {
            runable.run();
            return false;
        } catch (SecurityException e) {
            try {
                result.send(-1, SyncResultReceiver.bundleFor(e.getMessage()));
                return true;
            } catch (RemoteException e2) {
                String str = this.mTag;
                Slog.w(str, "Unable to send security exception (" + e + "): ", e2);
                return true;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractMasterSystemService
    public void dumpLocked(String prefix, PrintWriter pw) {
        super.dumpLocked(prefix, pw);
        String prefix2 = prefix + "  ";
        pw.print(prefix);
        pw.print("Users disabled by Settings: ");
        pw.println(this.mDisabledBySettings);
        pw.print(prefix);
        pw.println("DeviceConfig Settings: ");
        pw.print(prefix2);
        pw.print("disabled: ");
        pw.println(this.mDisabledByDeviceConfig);
        pw.print(prefix2);
        pw.print("loggingLevel: ");
        pw.println(this.mDevCfgLoggingLevel);
        pw.print(prefix2);
        pw.print("maxBufferSize: ");
        pw.println(this.mDevCfgMaxBufferSize);
        pw.print(prefix2);
        pw.print("idleFlushingFrequencyMs: ");
        pw.println(this.mDevCfgIdleFlushingFrequencyMs);
        pw.print(prefix2);
        pw.print("textChangeFlushingFrequencyMs: ");
        pw.println(this.mDevCfgTextChangeFlushingFrequencyMs);
        pw.print(prefix2);
        pw.print("logHistorySize: ");
        pw.println(this.mDevCfgLogHistorySize);
        pw.print(prefix2);
        pw.print("idleUnbindTimeoutMs: ");
        pw.println(this.mDevCfgIdleUnbindTimeoutMs);
        pw.print(prefix);
        pw.println("Global Options:");
        this.mGlobalContentCaptureOptions.dump(prefix2, pw);
    }

    /* access modifiers changed from: package-private */
    public final class ContentCaptureManagerServiceStub extends IContentCaptureManager.Stub {
        ContentCaptureManagerServiceStub() {
        }

        public void startSession(IBinder activityToken, ComponentName componentName, int sessionId, int flags, IResultReceiver result) {
            Preconditions.checkNotNull(activityToken);
            Preconditions.checkNotNull(Integer.valueOf(sessionId));
            int userId = UserHandle.getCallingUserId();
            ActivityPresentationInfo activityPresentationInfo = ContentCaptureManagerService.this.getAmInternal().getActivityPresentationInfo(activityToken);
            synchronized (ContentCaptureManagerService.this.mLock) {
                ((ContentCapturePerUserService) ContentCaptureManagerService.this.getServiceForUserLocked(userId)).startSessionLocked(activityToken, activityPresentationInfo, sessionId, Binder.getCallingUid(), flags, result);
            }
        }

        public void finishSession(int sessionId) {
            Preconditions.checkNotNull(Integer.valueOf(sessionId));
            int userId = UserHandle.getCallingUserId();
            synchronized (ContentCaptureManagerService.this.mLock) {
                ((ContentCapturePerUserService) ContentCaptureManagerService.this.getServiceForUserLocked(userId)).finishSessionLocked(sessionId);
            }
        }

        public void getServiceComponentName(IResultReceiver result) {
            ComponentName connectedServiceComponentName;
            int userId = UserHandle.getCallingUserId();
            synchronized (ContentCaptureManagerService.this.mLock) {
                connectedServiceComponentName = ((ContentCapturePerUserService) ContentCaptureManagerService.this.getServiceForUserLocked(userId)).getServiceComponentName();
            }
            try {
                result.send(0, SyncResultReceiver.bundleFor(connectedServiceComponentName));
            } catch (RemoteException e) {
                String str = ContentCaptureManagerService.this.mTag;
                Slog.w(str, "Unable to send service component name: " + e);
            }
        }

        public void removeData(DataRemovalRequest request) {
            Preconditions.checkNotNull(request);
            ContentCaptureManagerService.this.assertCalledByPackageOwner(request.getPackageName());
            int userId = UserHandle.getCallingUserId();
            synchronized (ContentCaptureManagerService.this.mLock) {
                ((ContentCapturePerUserService) ContentCaptureManagerService.this.getServiceForUserLocked(userId)).removeDataLocked(request);
            }
        }

        public void isContentCaptureFeatureEnabled(IResultReceiver result) {
            int i;
            boolean enabled;
            synchronized (ContentCaptureManagerService.this.mLock) {
                if (!ContentCaptureManagerService.this.throwsSecurityException(result, new Runnable() {
                    /* class com.android.server.contentcapture.$$Lambda$ContentCaptureManagerService$ContentCaptureManagerServiceStub$vyDTyUUAt356my5WVtp7QPYv5gY */

                    @Override // java.lang.Runnable
                    public final void run() {
                        ContentCaptureManagerService.ContentCaptureManagerServiceStub.this.lambda$isContentCaptureFeatureEnabled$0$ContentCaptureManagerService$ContentCaptureManagerServiceStub();
                    }
                })) {
                    i = 1;
                    enabled = !ContentCaptureManagerService.this.mDisabledByDeviceConfig && !ContentCaptureManagerService.this.isDisabledBySettingsLocked(UserHandle.getCallingUserId());
                } else {
                    return;
                }
            }
            if (!enabled) {
                i = 2;
            }
            try {
                result.send(i, (Bundle) null);
            } catch (RemoteException e) {
                String str = ContentCaptureManagerService.this.mTag;
                Slog.w(str, "Unable to send isContentCaptureFeatureEnabled(): " + e);
            }
        }

        public /* synthetic */ void lambda$isContentCaptureFeatureEnabled$0$ContentCaptureManagerService$ContentCaptureManagerServiceStub() {
            ContentCaptureManagerService.this.assertCalledByServiceLocked("isContentCaptureFeatureEnabled()");
        }

        public void getServiceSettingsActivity(IResultReceiver result) {
            if (!ContentCaptureManagerService.this.throwsSecurityException(result, new Runnable() {
                /* class com.android.server.contentcapture.$$Lambda$ContentCaptureManagerService$ContentCaptureManagerServiceStub$6vI15KqJwo_ruaAABrGMvkwVRt4 */

                @Override // java.lang.Runnable
                public final void run() {
                    ContentCaptureManagerService.ContentCaptureManagerServiceStub.this.lambda$getServiceSettingsActivity$1$ContentCaptureManagerService$ContentCaptureManagerServiceStub();
                }
            })) {
                int userId = UserHandle.getCallingUserId();
                synchronized (ContentCaptureManagerService.this.mLock) {
                    ContentCapturePerUserService service = (ContentCapturePerUserService) ContentCaptureManagerService.this.getServiceForUserLocked(userId);
                    if (service != null) {
                        ComponentName componentName = service.getServiceSettingsActivityLocked();
                        try {
                            result.send(0, SyncResultReceiver.bundleFor(componentName));
                        } catch (RemoteException e) {
                            String str = ContentCaptureManagerService.this.mTag;
                            Slog.w(str, "Unable to send getServiceSettingsIntent(): " + e);
                        }
                    }
                }
            }
        }

        public /* synthetic */ void lambda$getServiceSettingsActivity$1$ContentCaptureManagerService$ContentCaptureManagerServiceStub() {
            ContentCaptureManagerService.this.enforceCallingPermissionForManagement();
        }

        public void getContentCaptureConditions(String packageName, IResultReceiver result) {
            ArrayList<ContentCaptureCondition> conditions;
            if (!ContentCaptureManagerService.this.throwsSecurityException(result, new Runnable(packageName) {
                /* class com.android.server.contentcapture.$$Lambda$ContentCaptureManagerService$ContentCaptureManagerServiceStub$QeDhsP4OR9GyoofNgVlcOk1so */
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ContentCaptureManagerService.ContentCaptureManagerServiceStub.this.lambda$getContentCaptureConditions$2$ContentCaptureManagerService$ContentCaptureManagerServiceStub(this.f$1);
                }
            })) {
                int userId = UserHandle.getCallingUserId();
                synchronized (ContentCaptureManagerService.this.mLock) {
                    ContentCapturePerUserService service = (ContentCapturePerUserService) ContentCaptureManagerService.this.getServiceForUserLocked(userId);
                    if (service == null) {
                        conditions = null;
                    } else {
                        conditions = ContentCaptureHelper.toList(service.getContentCaptureConditionsLocked(packageName));
                    }
                }
                try {
                    result.send(0, SyncResultReceiver.bundleFor(conditions));
                } catch (RemoteException e) {
                    String str = ContentCaptureManagerService.this.mTag;
                    Slog.w(str, "Unable to send getServiceComponentName(): " + e);
                }
            }
        }

        public /* synthetic */ void lambda$getContentCaptureConditions$2$ContentCaptureManagerService$ContentCaptureManagerServiceStub(String packageName) {
            ContentCaptureManagerService.this.assertCalledByPackageOwner(packageName);
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(ContentCaptureManagerService.this.getContext(), ContentCaptureManagerService.this.mTag, pw)) {
                boolean showHistory = true;
                if (args != null) {
                    boolean showHistory2 = true;
                    for (String arg : args) {
                        char c = 65535;
                        int hashCode = arg.hashCode();
                        if (hashCode != 1098711592) {
                            if (hashCode == 1333069025 && arg.equals("--help")) {
                                c = 1;
                            }
                        } else if (arg.equals("--no-history")) {
                            c = 0;
                        }
                        if (c == 0) {
                            showHistory2 = false;
                        } else if (c != 1) {
                            Slog.w(ContentCaptureManagerService.this.mTag, "Ignoring invalid dump arg: " + arg);
                        } else {
                            pw.println("Usage: dumpsys content_capture [--no-history]");
                            return;
                        }
                    }
                    showHistory = showHistory2;
                }
                synchronized (ContentCaptureManagerService.this.mLock) {
                    ContentCaptureManagerService.this.dumpLocked("", pw);
                }
                pw.print("Requests history: ");
                if (ContentCaptureManagerService.this.mRequestsHistory == null) {
                    pw.println("disabled by device config");
                } else if (showHistory) {
                    pw.println();
                    ContentCaptureManagerService.this.mRequestsHistory.reverseDump(fd, pw, args);
                    pw.println();
                } else {
                    pw.println();
                }
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.contentcapture.ContentCaptureManagerService$ContentCaptureManagerServiceStub */
        /* JADX WARN: Multi-variable type inference failed */
        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
            new ContentCaptureManagerServiceShellCommand(ContentCaptureManagerService.this).exec(this, in, out, err, args, callback, resultReceiver);
        }
    }

    private final class LocalService extends ContentCaptureManagerInternal {
        private LocalService() {
        }

        @Override // com.android.server.contentcapture.ContentCaptureManagerInternal
        public boolean isContentCaptureServiceForUser(int uid, int userId) {
            synchronized (ContentCaptureManagerService.this.mLock) {
                ContentCapturePerUserService service = (ContentCapturePerUserService) ContentCaptureManagerService.this.peekServiceForUserLocked(userId);
                if (service == null) {
                    return false;
                }
                return service.isContentCaptureServiceForUserLocked(uid);
            }
        }

        @Override // com.android.server.contentcapture.ContentCaptureManagerInternal
        public boolean sendActivityAssistData(int userId, IBinder activityToken, Bundle data) {
            synchronized (ContentCaptureManagerService.this.mLock) {
                ContentCapturePerUserService service = (ContentCapturePerUserService) ContentCaptureManagerService.this.peekServiceForUserLocked(userId);
                if (service == null) {
                    return false;
                }
                return service.sendActivityAssistDataLocked(activityToken, data);
            }
        }

        @Override // com.android.server.contentcapture.ContentCaptureManagerInternal
        public ContentCaptureOptions getOptionsForPackage(int userId, String packageName) {
            return ContentCaptureManagerService.this.mGlobalContentCaptureOptions.getOptions(userId, packageName);
        }

        @Override // com.android.server.contentcapture.ContentCaptureManagerInternal
        public void notifyActivityEvent(int userId, ComponentName activityComponent, int eventType) {
            synchronized (ContentCaptureManagerService.this.mLock) {
                ContentCapturePerUserService service = (ContentCapturePerUserService) ContentCaptureManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    service.onActivityEventLocked(activityComponent, eventType);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class GlobalContentCaptureOptions extends GlobalWhitelistState {
        @GuardedBy({"mGlobalWhitelistStateLock"})
        private final SparseArray<String> mServicePackages = new SparseArray<>();
        @GuardedBy({"mGlobalWhitelistStateLock"})
        private final SparseBooleanArray mTemporaryServices = new SparseBooleanArray();

        GlobalContentCaptureOptions() {
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
                        String str = ContentCaptureManagerService.this.mTag;
                        Slog.w(str, "setServiceInfo(): invalid name: " + serviceName);
                        this.mServicePackages.remove(userId);
                    } else {
                        this.mServicePackages.put(userId, componentName.getPackageName());
                    }
                } else {
                    this.mServicePackages.remove(userId);
                }
            }
        }

        @GuardedBy({"mGlobalWhitelistStateLock"})
        public ContentCaptureOptions getOptions(int userId, String packageName) {
            boolean packageWhitelisted;
            ArraySet<ComponentName> whitelistedComponents = null;
            synchronized (this.mGlobalWhitelistStateLock) {
                packageWhitelisted = isWhitelisted(userId, packageName);
                if (!packageWhitelisted && (whitelistedComponents = getWhitelistedComponents(userId, packageName)) == null && packageName.equals(this.mServicePackages.get(userId))) {
                    if (ContentCaptureManagerService.this.verbose) {
                        String str = ContentCaptureManagerService.this.mTag;
                        Slog.v(str, "getOptionsForPackage() lite for " + packageName);
                    }
                    return new ContentCaptureOptions(ContentCaptureManagerService.this.mDevCfgLoggingLevel);
                }
            }
            if (Build.IS_USER && ContentCaptureManagerService.this.mServiceNameResolver.isTemporary(userId) && !packageName.equals(this.mServicePackages.get(userId))) {
                String str2 = ContentCaptureManagerService.this.mTag;
                Slog.w(str2, "Ignoring package " + packageName + " while using temporary service " + this.mServicePackages.get(userId));
                return null;
            } else if (packageWhitelisted || whitelistedComponents != null) {
                ContentCaptureOptions options = new ContentCaptureOptions(ContentCaptureManagerService.this.mDevCfgLoggingLevel, ContentCaptureManagerService.this.mDevCfgMaxBufferSize, ContentCaptureManagerService.this.mDevCfgIdleFlushingFrequencyMs, ContentCaptureManagerService.this.mDevCfgTextChangeFlushingFrequencyMs, ContentCaptureManagerService.this.mDevCfgLogHistorySize, whitelistedComponents);
                if (ContentCaptureManagerService.this.verbose) {
                    String str3 = ContentCaptureManagerService.this.mTag;
                    Slog.v(str3, "getOptionsForPackage(" + packageName + "): " + options);
                }
                return options;
            } else {
                if (ContentCaptureManagerService.this.verbose) {
                    String str4 = ContentCaptureManagerService.this.mTag;
                    Slog.v(str4, "getOptionsForPackage(" + packageName + "): not whitelisted");
                }
                return null;
            }
        }

        public void dump(String prefix, PrintWriter pw) {
            ContentCaptureManagerService.super.dump(prefix, pw);
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
}

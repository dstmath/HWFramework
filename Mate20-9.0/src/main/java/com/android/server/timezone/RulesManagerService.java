package com.android.server.timezone;

import android.app.timezone.DistroFormatVersion;
import android.app.timezone.DistroRulesVersion;
import android.app.timezone.ICallback;
import android.app.timezone.IRulesManager;
import android.app.timezone.RulesState;
import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.EventLogTags;
import com.android.server.SystemService;
import com.android.timezone.distro.DistroException;
import com.android.timezone.distro.DistroVersion;
import com.android.timezone.distro.StagedDistroOperation;
import com.android.timezone.distro.TimeZoneDistro;
import com.android.timezone.distro.installer.TimeZoneDistroInstaller;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import libcore.icu.ICU;
import libcore.util.TimeZoneFinder;
import libcore.util.ZoneInfoDB;

public final class RulesManagerService extends IRulesManager.Stub {
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    static final DistroFormatVersion DISTRO_FORMAT_VERSION_SUPPORTED = new DistroFormatVersion(2, 1);
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    static final String REQUIRED_QUERY_PERMISSION = "android.permission.QUERY_TIME_ZONE_RULES";
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    static final String REQUIRED_UPDATER_PERMISSION = "android.permission.UPDATE_TIME_ZONE_RULES";
    private static final File SYSTEM_TZ_DATA_FILE = new File("/system/usr/share/zoneinfo/tzdata");
    private static final String TAG = "timezone.RulesManagerService";
    private static final File TZ_DATA_DIR = new File("/data/misc/zoneinfo");
    private final Executor mExecutor;
    /* access modifiers changed from: private */
    public final TimeZoneDistroInstaller mInstaller;
    /* access modifiers changed from: private */
    public final RulesManagerIntentHelper mIntentHelper;
    /* access modifiers changed from: private */
    public final AtomicBoolean mOperationInProgress = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public final PackageTracker mPackageTracker;
    private final PermissionHelper mPermissionHelper;

    private class InstallRunnable implements Runnable {
        private final ICallback mCallback;
        private final CheckToken mCheckToken;
        private final ParcelFileDescriptor mDistroParcelFileDescriptor;

        InstallRunnable(ParcelFileDescriptor distroParcelFileDescriptor, CheckToken checkToken, ICallback callback) {
            this.mDistroParcelFileDescriptor = distroParcelFileDescriptor;
            this.mCheckToken = checkToken;
            this.mCallback = callback;
        }

        public void run() {
            ParcelFileDescriptor pfd;
            EventLogTags.writeTimezoneInstallStarted(RulesManagerService.toStringOrNull(this.mCheckToken));
            boolean success = false;
            try {
                pfd = this.mDistroParcelFileDescriptor;
                int installerResult = RulesManagerService.this.mInstaller.stageInstallWithErrorCode(new TimeZoneDistro(new FileInputStream(pfd.getFileDescriptor(), false)));
                sendInstallNotificationIntentIfRequired(installerResult);
                int resultCode = mapInstallerResultToApiCode(installerResult);
                EventLogTags.writeTimezoneInstallComplete(RulesManagerService.toStringOrNull(this.mCheckToken), resultCode);
                RulesManagerService.this.sendFinishedStatus(this.mCallback, resultCode);
                success = true;
                if (pfd != null) {
                    pfd.close();
                }
            } catch (Exception e) {
                try {
                    Slog.w(RulesManagerService.TAG, "Failed to install distro.", e);
                    EventLogTags.writeTimezoneInstallComplete(RulesManagerService.toStringOrNull(this.mCheckToken), 1);
                    RulesManagerService.this.sendFinishedStatus(this.mCallback, 1);
                } catch (Throwable th) {
                    RulesManagerService.this.mPackageTracker.recordCheckResult(this.mCheckToken, success);
                    RulesManagerService.this.mOperationInProgress.set(false);
                    throw th;
                }
            } catch (Throwable th2) {
                r3.addSuppressed(th2);
            }
            RulesManagerService.this.mPackageTracker.recordCheckResult(this.mCheckToken, success);
            RulesManagerService.this.mOperationInProgress.set(false);
            return;
            throw th;
        }

        private void sendInstallNotificationIntentIfRequired(int installerResult) {
            if (installerResult == 0) {
                RulesManagerService.this.mIntentHelper.sendTimeZoneOperationStaged();
            }
        }

        private int mapInstallerResultToApiCode(int installerResult) {
            switch (installerResult) {
                case 0:
                    return 0;
                case 1:
                    return 2;
                case 2:
                    return 3;
                case 3:
                    return 4;
                case 4:
                    return 5;
                default:
                    return 1;
            }
        }
    }

    public static class Lifecycle extends SystemService {
        public Lifecycle(Context context) {
            super(context);
        }

        /* JADX WARNING: type inference failed for: r0v1, types: [com.android.server.timezone.RulesManagerService, java.lang.Object, android.os.IBinder] */
        public void onStart() {
            ? access$000 = RulesManagerService.create(getContext());
            access$000.start();
            publishBinderService("timezone", access$000);
            publishLocalService(RulesManagerService.class, access$000);
        }
    }

    private class UninstallRunnable implements Runnable {
        private final ICallback mCallback;
        private final CheckToken mCheckToken;

        UninstallRunnable(CheckToken checkToken, ICallback callback) {
            this.mCheckToken = checkToken;
            this.mCallback = callback;
        }

        /* JADX WARNING: Removed duplicated region for block: B:10:0x0024 A[Catch:{ Exception -> 0x003a, all -> 0x0038 }] */
        /* JADX WARNING: Removed duplicated region for block: B:11:0x0026 A[Catch:{ Exception -> 0x003a, all -> 0x0038 }] */
        public void run() {
            boolean z;
            EventLogTags.writeTimezoneUninstallStarted(RulesManagerService.toStringOrNull(this.mCheckToken));
            boolean packageTrackerStatus = false;
            try {
                int uninstallResult = RulesManagerService.this.mInstaller.stageUninstall();
                sendUninstallNotificationIntentIfRequired(uninstallResult);
                if (uninstallResult != 0) {
                    if (uninstallResult != 1) {
                        z = false;
                        packageTrackerStatus = z;
                        int callbackResultCode = !packageTrackerStatus ? 0 : 1;
                        EventLogTags.writeTimezoneUninstallComplete(RulesManagerService.toStringOrNull(this.mCheckToken), callbackResultCode);
                        RulesManagerService.this.sendFinishedStatus(this.mCallback, callbackResultCode);
                        RulesManagerService.this.mPackageTracker.recordCheckResult(this.mCheckToken, packageTrackerStatus);
                        RulesManagerService.this.mOperationInProgress.set(false);
                    }
                }
                z = true;
                packageTrackerStatus = z;
                if (!packageTrackerStatus) {
                }
                EventLogTags.writeTimezoneUninstallComplete(RulesManagerService.toStringOrNull(this.mCheckToken), callbackResultCode);
                RulesManagerService.this.sendFinishedStatus(this.mCallback, callbackResultCode);
            } catch (Exception e) {
                EventLogTags.writeTimezoneUninstallComplete(RulesManagerService.toStringOrNull(this.mCheckToken), 1);
                Slog.w(RulesManagerService.TAG, "Failed to uninstall distro.", e);
                RulesManagerService.this.sendFinishedStatus(this.mCallback, 1);
            } catch (Throwable th) {
                RulesManagerService.this.mPackageTracker.recordCheckResult(this.mCheckToken, false);
                RulesManagerService.this.mOperationInProgress.set(false);
                throw th;
            }
            RulesManagerService.this.mPackageTracker.recordCheckResult(this.mCheckToken, packageTrackerStatus);
            RulesManagerService.this.mOperationInProgress.set(false);
        }

        private void sendUninstallNotificationIntentIfRequired(int uninstallResult) {
            switch (uninstallResult) {
                case 0:
                    RulesManagerService.this.mIntentHelper.sendTimeZoneOperationStaged();
                    return;
                case 1:
                    RulesManagerService.this.mIntentHelper.sendTimeZoneOperationUnstaged();
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public static RulesManagerService create(Context context) {
        RulesManagerServiceHelperImpl helper = new RulesManagerServiceHelperImpl(context);
        RulesManagerService rulesManagerService = new RulesManagerService(helper, helper, helper, PackageTracker.create(context), new TimeZoneDistroInstaller(TAG, SYSTEM_TZ_DATA_FILE, TZ_DATA_DIR));
        return rulesManagerService;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    RulesManagerService(PermissionHelper permissionHelper, Executor executor, RulesManagerIntentHelper intentHelper, PackageTracker packageTracker, TimeZoneDistroInstaller timeZoneDistroInstaller) {
        this.mPermissionHelper = permissionHelper;
        this.mExecutor = executor;
        this.mIntentHelper = intentHelper;
        this.mPackageTracker = packageTracker;
        this.mInstaller = timeZoneDistroInstaller;
    }

    public void start() {
        this.mPackageTracker.start();
    }

    public RulesState getRulesState() {
        this.mPermissionHelper.enforceCallerHasPermission(REQUIRED_QUERY_PERMISSION);
        return getRulesStateInternal();
    }

    private RulesState getRulesStateInternal() {
        RulesState rulesState;
        synchronized (this) {
            DistroRulesVersion installedDistroRulesVersion = null;
            try {
                String systemRulesVersion = this.mInstaller.getSystemRulesVersion();
                int distroStatus = 0;
                try {
                    DistroVersion installedDistroVersion = this.mInstaller.getInstalledDistroVersion();
                    if (installedDistroVersion == null) {
                        distroStatus = 1;
                        installedDistroRulesVersion = null;
                    } else {
                        distroStatus = 2;
                        installedDistroRulesVersion = new DistroRulesVersion(installedDistroVersion.rulesVersion, installedDistroVersion.revision);
                    }
                } catch (DistroException | IOException e) {
                    Slog.w(TAG, "Failed to read installed distro.", e);
                } catch (Throwable installedDistroRulesVersion2) {
                    throw installedDistroRulesVersion2;
                }
                boolean operationInProgress = this.mOperationInProgress.get();
                DistroRulesVersion stagedDistroRulesVersion = null;
                int stagedOperationStatus = 0;
                if (!operationInProgress) {
                    try {
                        StagedDistroOperation stagedDistroOperation = this.mInstaller.getStagedDistroOperation();
                        if (stagedDistroOperation == null) {
                            stagedOperationStatus = 1;
                        } else if (stagedDistroOperation.isUninstall) {
                            stagedOperationStatus = 2;
                        } else {
                            stagedOperationStatus = 3;
                            DistroVersion stagedDistroVersion = stagedDistroOperation.distroVersion;
                            stagedDistroRulesVersion = new DistroRulesVersion(stagedDistroVersion.rulesVersion, stagedDistroVersion.revision);
                        }
                    } catch (DistroException | IOException e2) {
                        Slog.w(TAG, "Failed to read staged distro.", e2);
                    }
                }
                rulesState = new RulesState(systemRulesVersion, DISTRO_FORMAT_VERSION_SUPPORTED, operationInProgress, stagedOperationStatus, stagedDistroRulesVersion, distroStatus, installedDistroRulesVersion);
            } catch (IOException e3) {
                Slog.w(TAG, "Failed to read system rules", e3);
                return null;
            }
        }
        return rulesState;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004a, code lost:
        if (r8 == null) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004c, code lost:
        if (0 == 0) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r8.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0052, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0053, code lost:
        android.util.Slog.w(TAG, "Failed to close distroParcelFileDescriptor", r3);
     */
    public int requestInstall(ParcelFileDescriptor distroParcelFileDescriptor, byte[] checkTokenBytes, ICallback callback) {
        String str;
        boolean closeParcelFileDescriptorOnExit = true;
        try {
            this.mPermissionHelper.enforceCallerHasPermission(REQUIRED_UPDATER_PERMISSION);
            CheckToken checkToken = null;
            if (checkTokenBytes != null) {
                checkToken = createCheckTokenOrThrow(checkTokenBytes);
            }
            EventLogTags.writeTimezoneRequestInstall(toStringOrNull(checkToken));
            synchronized (this) {
                if (distroParcelFileDescriptor == null) {
                    throw new NullPointerException("distroParcelFileDescriptor == null");
                } else if (callback != null) {
                    try {
                        if (!this.mOperationInProgress.get()) {
                            this.mOperationInProgress.set(true);
                            this.mExecutor.execute(new InstallRunnable(distroParcelFileDescriptor, checkToken, callback));
                            try {
                            } catch (Throwable th) {
                                Throwable th2 = th;
                                closeParcelFileDescriptorOnExit = false;
                                th = th2;
                                throw th;
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                } else {
                    throw new NullPointerException("observer == null");
                }
            }
            return 1;
            return 0;
        } finally {
            if (distroParcelFileDescriptor != null && closeParcelFileDescriptorOnExit) {
                try {
                    distroParcelFileDescriptor.close();
                } catch (IOException e) {
                    str = "Failed to close distroParcelFileDescriptor";
                    Slog.w(TAG, str, e);
                }
            }
        }
    }

    public int requestUninstall(byte[] checkTokenBytes, ICallback callback) {
        this.mPermissionHelper.enforceCallerHasPermission(REQUIRED_UPDATER_PERMISSION);
        CheckToken checkToken = null;
        if (checkTokenBytes != null) {
            checkToken = createCheckTokenOrThrow(checkTokenBytes);
        }
        EventLogTags.writeTimezoneRequestUninstall(toStringOrNull(checkToken));
        synchronized (this) {
            if (callback == null) {
                throw new NullPointerException("callback == null");
            } else if (this.mOperationInProgress.get()) {
                return 1;
            } else {
                this.mOperationInProgress.set(true);
                this.mExecutor.execute(new UninstallRunnable(checkToken, callback));
                return 0;
            }
        }
    }

    /* access modifiers changed from: private */
    public void sendFinishedStatus(ICallback callback, int resultCode) {
        try {
            callback.onFinished(resultCode);
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to notify observer of result", e);
        }
    }

    public void requestNothing(byte[] checkTokenBytes, boolean success) {
        this.mPermissionHelper.enforceCallerHasPermission(REQUIRED_UPDATER_PERMISSION);
        CheckToken checkToken = null;
        if (checkTokenBytes != null) {
            checkToken = createCheckTokenOrThrow(checkTokenBytes);
        }
        EventLogTags.writeTimezoneRequestNothing(toStringOrNull(checkToken));
        this.mPackageTracker.recordCheckResult(checkToken, success);
        EventLogTags.writeTimezoneNothingComplete(toStringOrNull(checkToken));
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mPermissionHelper.checkDumpPermission(TAG, pw)) {
            RulesState rulesState = getRulesStateInternal();
            if (args != null && args.length == 2) {
                if ("-format_state".equals(args[0]) && args[1] != null) {
                    for (char c : args[1].toCharArray()) {
                        switch (c) {
                            case HdmiCecKeycode.CEC_KEYCODE_PAUSE_PLAY_FUNCTION:
                                pw.println("Active rules version (ICU, ZoneInfoDB, TimeZoneFinder): " + ICU.getTZDataVersion() + "," + ZoneInfoDB.getInstance().getVersion() + "," + TimeZoneFinder.getInstance().getIanaVersion());
                                break;
                            case HdmiCecKeycode.CEC_KEYCODE_PAUSE_RECORD_FUNCTION:
                                String value = "Unknown";
                                if (rulesState != null) {
                                    value = distroStatusToString(rulesState.getDistroStatus());
                                }
                                pw.println("Current install state: " + value);
                                break;
                            case 'i':
                                String value2 = "Unknown";
                                if (rulesState != null) {
                                    DistroRulesVersion installedRulesVersion = rulesState.getInstalledDistroRulesVersion();
                                    if (installedRulesVersion == null) {
                                        value2 = "<None>";
                                    } else {
                                        value2 = installedRulesVersion.toDumpString();
                                    }
                                }
                                pw.println("Installed rules version: " + value2);
                                break;
                            case 'o':
                                String value3 = "Unknown";
                                if (rulesState != null) {
                                    value3 = stagedOperationToString(rulesState.getStagedOperationType());
                                }
                                pw.println("Staged operation: " + value3);
                                break;
                            case 'p':
                                String value4 = "Unknown";
                                if (rulesState != null) {
                                    value4 = Boolean.toString(rulesState.isOperationInProgress());
                                }
                                pw.println("Operation in progress: " + value4);
                                break;
                            case HdmiCecKeycode.CEC_KEYCODE_F3_GREEN:
                                String value5 = "Unknown";
                                if (rulesState != null) {
                                    value5 = rulesState.getSystemRulesVersion();
                                }
                                pw.println("System rules version: " + value5);
                                break;
                            case HdmiCecKeycode.CEC_KEYCODE_F4_YELLOW:
                                String value6 = "Unknown";
                                if (rulesState != null) {
                                    DistroRulesVersion stagedDistroRulesVersion = rulesState.getStagedDistroRulesVersion();
                                    if (stagedDistroRulesVersion == null) {
                                        value6 = "<None>";
                                    } else {
                                        value6 = stagedDistroRulesVersion.toDumpString();
                                    }
                                }
                                pw.println("Staged rules version: " + value6);
                                break;
                            default:
                                pw.println("Unknown option: " + c);
                                break;
                        }
                    }
                    return;
                }
            }
            pw.println("RulesManagerService state: " + toString());
            pw.println("Active rules version (ICU, ZoneInfoDB, TimeZoneFinder): " + ICU.getTZDataVersion() + "," + ZoneInfoDB.getInstance().getVersion() + "," + TimeZoneFinder.getInstance().getIanaVersion());
            StringBuilder sb = new StringBuilder();
            sb.append("Distro state: ");
            sb.append(rulesState.toString());
            pw.println(sb.toString());
            this.mPackageTracker.dump(pw);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyIdle() {
        this.mPackageTracker.triggerUpdateIfNeeded(false);
    }

    public String toString() {
        return "RulesManagerService{mOperationInProgress=" + this.mOperationInProgress + '}';
    }

    private static CheckToken createCheckTokenOrThrow(byte[] checkTokenBytes) {
        try {
            return CheckToken.fromByteArray(checkTokenBytes);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read token bytes " + Arrays.toString(checkTokenBytes), e);
        }
    }

    private static String distroStatusToString(int distroStatus) {
        switch (distroStatus) {
            case 1:
                return "None";
            case 2:
                return "Installed";
            default:
                return "Unknown";
        }
    }

    private static String stagedOperationToString(int stagedOperationType) {
        switch (stagedOperationType) {
            case 1:
                return "None";
            case 2:
                return "Uninstall";
            case 3:
                return "Install";
            default:
                return "Unknown";
        }
    }

    /* access modifiers changed from: private */
    public static String toStringOrNull(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }
}

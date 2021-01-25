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
import com.android.server.hdmi.HdmiCecKeycode;
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
import libcore.timezone.TimeZoneDataFiles;
import libcore.timezone.TimeZoneFinder;
import libcore.timezone.TzDataSetVersion;
import libcore.timezone.ZoneInfoDB;

public final class RulesManagerService extends IRulesManager.Stub {
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    static final DistroFormatVersion DISTRO_FORMAT_VERSION_SUPPORTED = new DistroFormatVersion(TzDataSetVersion.currentFormatMajorVersion(), TzDataSetVersion.currentFormatMinorVersion());
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    static final String REQUIRED_QUERY_PERMISSION = "android.permission.QUERY_TIME_ZONE_RULES";
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    static final String REQUIRED_UPDATER_PERMISSION = "android.permission.UPDATE_TIME_ZONE_RULES";
    private static final String TAG = "timezone.RulesManagerService";
    private final Executor mExecutor;
    private final TimeZoneDistroInstaller mInstaller;
    private final RulesManagerIntentHelper mIntentHelper;
    private final AtomicBoolean mOperationInProgress = new AtomicBoolean(false);
    private final PackageTracker mPackageTracker;
    private final PermissionHelper mPermissionHelper;

    public static class Lifecycle extends SystemService {
        public Lifecycle(Context context) {
            super(context);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.timezone.RulesManagerService$Lifecycle */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r0v1, types: [com.android.server.timezone.RulesManagerService, java.lang.Object, android.os.IBinder] */
        /* JADX WARNING: Unknown variable types count: 1 */
        @Override // com.android.server.SystemService
        public void onStart() {
            ?? create = RulesManagerService.create(getContext());
            create.start();
            publishBinderService("timezone", create);
            publishLocalService(RulesManagerService.class, create);
        }
    }

    /* access modifiers changed from: private */
    public static RulesManagerService create(Context context) {
        RulesManagerServiceHelperImpl helper = new RulesManagerServiceHelperImpl(context);
        return new RulesManagerService(helper, helper, helper, PackageTracker.create(context), new TimeZoneDistroInstaller(TAG, new File(TimeZoneDataFiles.getRuntimeModuleTzVersionFile()), new File(TimeZoneDataFiles.getDataTimeZoneRootDir())));
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
            try {
                TzDataSetVersion baseVersion = this.mInstaller.readBaseVersion();
                int distroStatus = 0;
                DistroRulesVersion installedDistroRulesVersion = null;
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
                rulesState = new RulesState(baseVersion.rulesVersion, DISTRO_FORMAT_VERSION_SUPPORTED, operationInProgress, stagedOperationStatus, stagedDistroRulesVersion, distroStatus, installedDistroRulesVersion);
            } catch (IOException e3) {
                Slog.w(TAG, "Failed to read base rules version", e3);
                return null;
            }
        }
        return rulesState;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0026, code lost:
        if (1 == 0) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r7.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002c, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002d, code lost:
        android.util.Slog.w(com.android.server.timezone.RulesManagerService.TAG, "Failed to close distroParcelFileDescriptor", r2);
     */
    public int requestInstall(ParcelFileDescriptor distroParcelFileDescriptor, byte[] checkTokenBytes, ICallback callback) {
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
                } else if (callback == null) {
                    throw new NullPointerException("observer == null");
                } else if (!this.mOperationInProgress.get()) {
                    this.mOperationInProgress.set(true);
                    this.mExecutor.execute(new InstallRunnable(distroParcelFileDescriptor, checkToken, callback));
                }
            }
            if (0 != 0) {
                try {
                    distroParcelFileDescriptor.close();
                } catch (IOException e) {
                    Slog.w(TAG, "Failed to close distroParcelFileDescriptor", e);
                }
            }
            return 0;
            return 1;
        } catch (Throwable th) {
            if (!(distroParcelFileDescriptor == null || 1 == 0)) {
                try {
                    distroParcelFileDescriptor.close();
                } catch (IOException e2) {
                    Slog.w(TAG, "Failed to close distroParcelFileDescriptor", e2);
                }
            }
            throw th;
        }
    }

    private class InstallRunnable implements Runnable {
        private final ICallback mCallback;
        private final CheckToken mCheckToken;
        private final ParcelFileDescriptor mDistroParcelFileDescriptor;

        InstallRunnable(ParcelFileDescriptor distroParcelFileDescriptor, CheckToken checkToken, ICallback callback) {
            this.mDistroParcelFileDescriptor = distroParcelFileDescriptor;
            this.mCheckToken = checkToken;
            this.mCallback = callback;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x0044, code lost:
            r4 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0045, code lost:
            if (r2 != null) goto L_0x0047;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
            r2.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x004b, code lost:
            r5 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x004c, code lost:
            r3.addSuppressed(r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x004f, code lost:
            throw r4;
         */
        @Override // java.lang.Runnable
        public void run() {
            EventLogTags.writeTimezoneInstallStarted(RulesManagerService.toStringOrNull(this.mCheckToken));
            boolean success = false;
            try {
                ParcelFileDescriptor pfd = this.mDistroParcelFileDescriptor;
                int installerResult = RulesManagerService.this.mInstaller.stageInstallWithErrorCode(new TimeZoneDistro(new FileInputStream(pfd.getFileDescriptor(), false)));
                sendInstallNotificationIntentIfRequired(installerResult);
                int resultCode = mapInstallerResultToApiCode(installerResult);
                EventLogTags.writeTimezoneInstallComplete(RulesManagerService.toStringOrNull(this.mCheckToken), resultCode);
                RulesManagerService.this.sendFinishedStatus(this.mCallback, resultCode);
                success = true;
                pfd.close();
            } catch (Exception e) {
                Slog.w(RulesManagerService.TAG, "Failed to install distro.", e);
                EventLogTags.writeTimezoneInstallComplete(RulesManagerService.toStringOrNull(this.mCheckToken), 1);
                RulesManagerService.this.sendFinishedStatus(this.mCallback, 1);
            } catch (Throwable th) {
                RulesManagerService.this.mPackageTracker.recordCheckResult(this.mCheckToken, false);
                RulesManagerService.this.mOperationInProgress.set(false);
                throw th;
            }
            RulesManagerService.this.mPackageTracker.recordCheckResult(this.mCheckToken, success);
            RulesManagerService.this.mOperationInProgress.set(false);
        }

        private void sendInstallNotificationIntentIfRequired(int installerResult) {
            if (installerResult == 0) {
                RulesManagerService.this.mIntentHelper.sendTimeZoneOperationStaged();
            }
        }

        private int mapInstallerResultToApiCode(int installerResult) {
            if (installerResult == 0) {
                return 0;
            }
            if (installerResult == 1) {
                return 2;
            }
            if (installerResult == 2) {
                return 3;
            }
            if (installerResult == 3) {
                return 4;
            }
            if (installerResult != 4) {
                return 1;
            }
            return 5;
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
            if (callback != null) {
                try {
                    if (this.mOperationInProgress.get()) {
                        return 1;
                    }
                    this.mOperationInProgress.set(true);
                    this.mExecutor.execute(new UninstallRunnable(checkToken, callback));
                    return 0;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                throw new NullPointerException("callback == null");
            }
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
        @Override // java.lang.Runnable
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
            if (uninstallResult == 0) {
                RulesManagerService.this.mIntentHelper.sendTimeZoneOperationStaged();
            } else if (uninstallResult == 1) {
                RulesManagerService.this.mIntentHelper.sendTimeZoneOperationUnstaged();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendFinishedStatus(ICallback callback, int resultCode) {
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
                    char[] charArray = args[1].toCharArray();
                    for (char c : charArray) {
                        if (c == 'i') {
                            String value = "Unknown";
                            if (rulesState != null) {
                                DistroRulesVersion installedRulesVersion = rulesState.getInstalledDistroRulesVersion();
                                if (installedRulesVersion == null) {
                                    value = "<None>";
                                } else {
                                    value = installedRulesVersion.toDumpString();
                                }
                            }
                            pw.println("Installed rules version: " + value);
                        } else if (c == 't') {
                            String value2 = "Unknown";
                            if (rulesState != null) {
                                DistroRulesVersion stagedDistroRulesVersion = rulesState.getStagedDistroRulesVersion();
                                if (stagedDistroRulesVersion == null) {
                                    value2 = "<None>";
                                } else {
                                    value2 = stagedDistroRulesVersion.toDumpString();
                                }
                            }
                            pw.println("Staged rules version: " + value2);
                        } else if (c == 'o') {
                            String value3 = "Unknown";
                            if (rulesState != null) {
                                value3 = stagedOperationToString(rulesState.getStagedOperationType());
                            }
                            pw.println("Staged operation: " + value3);
                        } else if (c != 'p') {
                            switch (c) {
                                case HdmiCecKeycode.CEC_KEYCODE_PAUSE_PLAY_FUNCTION /* 97 */:
                                    pw.println("Active rules version (ICU, ZoneInfoDB, TimeZoneFinder): " + ICU.getTZDataVersion() + "," + ZoneInfoDB.getInstance().getVersion() + "," + TimeZoneFinder.getInstance().getIanaVersion());
                                    continue;
                                case HdmiCecKeycode.CEC_KEYCODE_RECORD_FUNCTION /* 98 */:
                                    String value4 = "Unknown";
                                    if (rulesState != null) {
                                        value4 = rulesState.getBaseRulesVersion();
                                    }
                                    pw.println("Base rules version: " + value4);
                                    continue;
                                case 'c':
                                    String value5 = "Unknown";
                                    if (rulesState != null) {
                                        value5 = distroStatusToString(rulesState.getDistroStatus());
                                    }
                                    pw.println("Current install state: " + value5);
                                    continue;
                                default:
                                    pw.println("Unknown option: " + c);
                                    continue;
                            }
                        } else {
                            String value6 = "Unknown";
                            if (rulesState != null) {
                                value6 = Boolean.toString(rulesState.isOperationInProgress());
                            }
                            pw.println("Operation in progress: " + value6);
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
        if (distroStatus == 1) {
            return "None";
        }
        if (distroStatus != 2) {
            return "Unknown";
        }
        return "Installed";
    }

    private static String stagedOperationToString(int stagedOperationType) {
        if (stagedOperationType == 1) {
            return "None";
        }
        if (stagedOperationType == 2) {
            return "Uninstall";
        }
        if (stagedOperationType != 3) {
            return "Unknown";
        }
        return "Install";
    }

    /* access modifiers changed from: private */
    public static String toStringOrNull(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }
}

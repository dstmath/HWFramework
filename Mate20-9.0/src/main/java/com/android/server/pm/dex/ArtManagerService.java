package com.android.server.pm.dex;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.content.pm.dex.ArtManager;
import android.content.pm.dex.ArtManagerInternal;
import android.content.pm.dex.DexMetadataHelper;
import android.content.pm.dex.IArtManager;
import android.content.pm.dex.ISnapshotRuntimeProfileCallback;
import android.content.pm.dex.PackageOptimizationInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.system.Os;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.RoSystemProperties;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.Preconditions;
import com.android.server.LocalServices;
import com.android.server.UiModeManagerService;
import com.android.server.pm.Installer;
import com.android.server.pm.PackageManagerServiceCompilerMapping;
import dalvik.system.DexFile;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.FileNotFoundException;
import libcore.io.IoUtils;

public class ArtManagerService extends IArtManager.Stub {
    private static final String BOOT_IMAGE_ANDROID_PACKAGE = "android";
    private static final String BOOT_IMAGE_PROFILE_NAME = "android.prof";
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "ArtManagerService";
    private static final int TRON_COMPILATION_FILTER_ASSUMED_VERIFIED = 2;
    private static final int TRON_COMPILATION_FILTER_ERROR = 0;
    private static final int TRON_COMPILATION_FILTER_EVERYTHING = 11;
    private static final int TRON_COMPILATION_FILTER_EVERYTHING_PROFILE = 10;
    private static final int TRON_COMPILATION_FILTER_EXTRACT = 3;
    private static final int TRON_COMPILATION_FILTER_FAKE_RUN_FROM_APK = 12;
    private static final int TRON_COMPILATION_FILTER_FAKE_RUN_FROM_APK_FALLBACK = 13;
    private static final int TRON_COMPILATION_FILTER_FAKE_RUN_FROM_VDEX_FALLBACK = 14;
    private static final int TRON_COMPILATION_FILTER_QUICKEN = 5;
    private static final int TRON_COMPILATION_FILTER_SPACE = 7;
    private static final int TRON_COMPILATION_FILTER_SPACE_PROFILE = 6;
    private static final int TRON_COMPILATION_FILTER_SPEED = 9;
    private static final int TRON_COMPILATION_FILTER_SPEED_PROFILE = 8;
    private static final int TRON_COMPILATION_FILTER_UNKNOWN = 1;
    private static final int TRON_COMPILATION_FILTER_VERIFY = 4;
    private static final int TRON_COMPILATION_REASON_AB_OTA = 6;
    private static final int TRON_COMPILATION_REASON_BG_DEXOPT = 5;
    private static final int TRON_COMPILATION_REASON_BG_SPEED_DEXOPT = 9;
    private static final int TRON_COMPILATION_REASON_BOOT = 3;
    private static final int TRON_COMPILATION_REASON_ERROR = 0;
    private static final int TRON_COMPILATION_REASON_FIRST_BOOT = 2;
    private static final int TRON_COMPILATION_REASON_INACTIVE = 7;
    private static final int TRON_COMPILATION_REASON_INSTALL = 4;
    private static final int TRON_COMPILATION_REASON_SHARED = 8;
    private static final int TRON_COMPILATION_REASON_UNKNOWN = 1;
    private final Context mContext;
    private final Handler mHandler = new Handler(BackgroundThread.getHandler().getLooper());
    private final Object mInstallLock;
    /* access modifiers changed from: private */
    @GuardedBy("mInstallLock")
    public final Installer mInstaller;
    private final IPackageManager mPackageManager;

    private class ArtManagerInternalImpl extends ArtManagerInternal {
        private ArtManagerInternalImpl() {
        }

        public PackageOptimizationInfo getPackageOptimizationInfo(ApplicationInfo info, String abi) {
            String compilationFilter;
            String compilationReason;
            String optInfo;
            String compilationReason2;
            try {
                String isa = VMRuntime.getInstructionSet(abi);
                if (System.getenv("MAPLE_RUNTIME") != null) {
                    String[] optInfo2 = ArtManagerService.this.mInstaller.getDexFileOptimizationInfo(new String[]{info.getBaseCodePath()}, new String[]{isa}, new int[]{info.uid});
                    if (optInfo2 == null || optInfo2.length != 2) {
                        compilationFilter = "error";
                        compilationReason2 = "error";
                    } else {
                        compilationFilter = optInfo2[0];
                        compilationReason2 = optInfo2[1];
                    }
                    optInfo = compilationReason2;
                } else {
                    DexFile.OptimizationInfo optInfo3 = DexFile.getDexFileOptimizationInfo(info.getBaseCodePath(), isa);
                    compilationFilter = optInfo3.getStatus();
                    optInfo = optInfo3.getReason();
                }
                compilationReason = optInfo;
            } catch (Installer.InstallerException e) {
                Slog.e(ArtManagerService.TAG, "Could not get optimizations status for " + info.getBaseCodePath(), e);
                compilationFilter = "error";
                compilationReason = "error";
            } catch (FileNotFoundException e2) {
                Slog.e(ArtManagerService.TAG, "Could not get optimizations status for " + info.getBaseCodePath(), e2);
                compilationFilter = "error";
                compilationReason = "error";
            } catch (IllegalArgumentException e3) {
                Slog.wtf(ArtManagerService.TAG, "Requested optimization status for " + info.getBaseCodePath() + " due to an invalid abi " + abi, e3);
                compilationFilter = "error";
                compilationReason = "error";
            }
            return new PackageOptimizationInfo(ArtManagerService.getCompilationFilterTronValue(compilationFilter), ArtManagerService.getCompilationReasonTronValue(compilationReason));
        }
    }

    static {
        verifyTronLoggingConstants();
    }

    public ArtManagerService(Context context, IPackageManager pm, Installer installer, Object installLock) {
        this.mContext = context;
        this.mPackageManager = pm;
        this.mInstaller = installer;
        this.mInstallLock = installLock;
        LocalServices.addService(ArtManagerInternal.class, new ArtManagerInternalImpl());
    }

    private boolean checkAndroidPermissions(int callingUid, String callingPackage) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_RUNTIME_PROFILES", TAG);
        int noteOp = ((AppOpsManager) this.mContext.getSystemService(AppOpsManager.class)).noteOp(43, callingUid, callingPackage);
        if (noteOp == 0) {
            return true;
        }
        if (noteOp != 3) {
            return false;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.PACKAGE_USAGE_STATS", TAG);
        return true;
    }

    private boolean checkShellPermissions(int profileType, String packageName, int callingUid) {
        boolean z = false;
        if (callingUid != 2000) {
            return false;
        }
        if (RoSystemProperties.DEBUGGABLE) {
            return true;
        }
        if (profileType == 1) {
            return false;
        }
        PackageInfo info = null;
        try {
            info = this.mPackageManager.getPackageInfo(packageName, 0, 0);
        } catch (RemoteException e) {
        }
        if (info == null) {
            return false;
        }
        if ((info.applicationInfo.flags & 2) == 2) {
            z = true;
        }
        return z;
    }

    public void snapshotRuntimeProfile(int profileType, String packageName, String codePath, ISnapshotRuntimeProfileCallback callback, String callingPackage) {
        int callingUid = Binder.getCallingUid();
        if (checkShellPermissions(profileType, packageName, callingUid) || checkAndroidPermissions(callingUid, callingPackage)) {
            Preconditions.checkNotNull(callback);
            boolean bootImageProfile = true;
            if (profileType != 1) {
                bootImageProfile = false;
            }
            if (!bootImageProfile) {
                Preconditions.checkStringNotEmpty(codePath);
                Preconditions.checkStringNotEmpty(packageName);
            }
            if (isRuntimeProfilingEnabled(profileType, callingPackage)) {
                if (DEBUG) {
                    Slog.d(TAG, "Requested snapshot for " + packageName + ":" + codePath);
                }
                if (bootImageProfile) {
                    snapshotBootImageProfile(callback);
                } else {
                    snapshotAppProfile(packageName, codePath, callback);
                }
                return;
            }
            throw new IllegalStateException("Runtime profiling is not enabled for " + profileType);
        }
        try {
            callback.onError(2);
        } catch (RemoteException e) {
        }
    }

    private void snapshotAppProfile(String packageName, String codePath, ISnapshotRuntimeProfileCallback callback) {
        PackageInfo info = null;
        try {
            info = this.mPackageManager.getPackageInfo(packageName, 0, 0);
        } catch (RemoteException e) {
        }
        if (info == null) {
            postError(callback, packageName, 0);
            return;
        }
        boolean pathFound = info.applicationInfo.getBaseCodePath().equals(codePath);
        String splitName = null;
        String[] splitCodePaths = info.applicationInfo.getSplitCodePaths();
        if (!pathFound && splitCodePaths != null) {
            int i = splitCodePaths.length - 1;
            while (true) {
                if (i < 0) {
                    break;
                } else if (splitCodePaths[i].equals(codePath)) {
                    pathFound = true;
                    splitName = info.applicationInfo.splitNames[i];
                    break;
                } else {
                    i--;
                }
            }
        }
        if (!pathFound) {
            postError(callback, packageName, 1);
            return;
        }
        int appId = UserHandle.getAppId(info.applicationInfo.uid);
        if (appId < 0) {
            postError(callback, packageName, 2);
            Slog.wtf(TAG, "AppId is -1 for package: " + packageName);
            return;
        }
        createProfileSnapshot(packageName, ArtManager.getProfileName(splitName), codePath, appId, callback);
        destroyProfileSnapshot(packageName, ArtManager.getProfileName(splitName));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0013, code lost:
        r0 = android.content.pm.dex.ArtManager.getProfileSnapshotFileForName(r8, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r2 = android.os.ParcelFileDescriptor.open(r0, 268435456);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001f, code lost:
        if (r2 == null) goto L_0x0030;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0029, code lost:
        if (r2.getFileDescriptor().valid() != false) goto L_0x002c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002c, code lost:
        postSuccess(r8, r2, r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0030, code lost:
        r4 = new java.lang.StringBuilder();
        r4.append("ParcelFileDescriptor.open returned an invalid descriptor for ");
        r4.append(r8);
        r4.append(":");
        r4.append(r0);
        r4.append(". isNull=");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004c, code lost:
        if (r2 != null) goto L_0x0050;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004e, code lost:
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0050, code lost:
        r5 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0051, code lost:
        r4.append(r5);
        android.util.Slog.wtf(TAG, r4.toString());
        postError(r12, r8, 2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005f, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0060, code lost:
        android.util.Slog.w(TAG, "Could not open snapshot profile for " + r8 + ":" + r0, r3);
        postError(r12, r8, 2);
     */
    private void createProfileSnapshot(String packageName, String profileName, String classpath, int appId, ISnapshotRuntimeProfileCallback callback) {
        synchronized (this.mInstallLock) {
            try {
                if (!this.mInstaller.createProfileSnapshot(appId, packageName, profileName, classpath)) {
                    postError(callback, packageName, 2);
                }
            } catch (Installer.InstallerException e) {
                postError(callback, packageName, 2);
            }
        }
    }

    private void destroyProfileSnapshot(String packageName, String profileName) {
        if (DEBUG) {
            Slog.d(TAG, "Destroying profile snapshot for" + packageName + ":" + profileName);
        }
        synchronized (this.mInstallLock) {
            try {
                this.mInstaller.destroyProfileSnapshot(packageName, profileName);
            } catch (Installer.InstallerException e) {
                Slog.e(TAG, "Failed to destroy profile snapshot for " + packageName + ":" + profileName, e);
            }
        }
    }

    public boolean isRuntimeProfilingEnabled(int profileType, String callingPackage) {
        int callingUid = Binder.getCallingUid();
        boolean z = false;
        if (callingUid != 2000 && !checkAndroidPermissions(callingUid, callingPackage)) {
            return false;
        }
        switch (profileType) {
            case 0:
                return SystemProperties.getBoolean("dalvik.vm.usejitprofiles", false);
            case 1:
                if ((Build.IS_USERDEBUG || Build.IS_ENG) && SystemProperties.getBoolean("dalvik.vm.usejitprofiles", false) && SystemProperties.getBoolean("dalvik.vm.profilebootimage", false)) {
                    z = true;
                }
                return z;
            default:
                throw new IllegalArgumentException("Invalid profile type:" + profileType);
        }
    }

    private void snapshotBootImageProfile(ISnapshotRuntimeProfileCallback callback) {
        createProfileSnapshot("android", BOOT_IMAGE_PROFILE_NAME, String.join(":", new CharSequence[]{Os.getenv("BOOTCLASSPATH"), Os.getenv("SYSTEMSERVERCLASSPATH")}), -1, callback);
        destroyProfileSnapshot("android", BOOT_IMAGE_PROFILE_NAME);
    }

    private void postError(ISnapshotRuntimeProfileCallback callback, String packageName, int errCode) {
        if (DEBUG) {
            Slog.d(TAG, "Failed to snapshot profile for " + packageName + " with error: " + errCode);
        }
        this.mHandler.post(new Runnable(callback, errCode, packageName) {
            private final /* synthetic */ ISnapshotRuntimeProfileCallback f$0;
            private final /* synthetic */ int f$1;
            private final /* synthetic */ String f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                ArtManagerService.lambda$postError$0(this.f$0, this.f$1, this.f$2);
            }
        });
    }

    static /* synthetic */ void lambda$postError$0(ISnapshotRuntimeProfileCallback callback, int errCode, String packageName) {
        try {
            callback.onError(errCode);
        } catch (Exception e) {
            Slog.w(TAG, "Failed to callback after profile snapshot for " + packageName, e);
        }
    }

    private void postSuccess(String packageName, ParcelFileDescriptor fd, ISnapshotRuntimeProfileCallback callback) {
        if (DEBUG) {
            Slog.d(TAG, "Successfully snapshot profile for " + packageName);
        }
        this.mHandler.post(new Runnable(fd, callback, packageName) {
            private final /* synthetic */ ParcelFileDescriptor f$0;
            private final /* synthetic */ ISnapshotRuntimeProfileCallback f$1;
            private final /* synthetic */ String f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                ArtManagerService.lambda$postSuccess$1(this.f$0, this.f$1, this.f$2);
            }
        });
    }

    static /* synthetic */ void lambda$postSuccess$1(ParcelFileDescriptor fd, ISnapshotRuntimeProfileCallback callback, String packageName) {
        try {
            if (fd.getFileDescriptor().valid()) {
                callback.onSuccess(fd);
            } else {
                Slog.wtf(TAG, "The snapshot FD became invalid before posting the result for " + packageName);
                callback.onError(2);
            }
        } catch (Exception e) {
            Slog.w(TAG, "Failed to call onSuccess after profile snapshot for " + packageName, e);
        } catch (Throwable th) {
            IoUtils.closeQuietly(fd);
            throw th;
        }
        IoUtils.closeQuietly(fd);
    }

    public void prepareAppProfiles(PackageParser.Package pkg, int user) {
        int appId = UserHandle.getAppId(pkg.applicationInfo.uid);
        if (user < 0) {
            Slog.wtf(TAG, "Invalid user id: " + user);
        } else if (appId < 0) {
            Slog.wtf(TAG, "Invalid app id: " + appId);
        } else {
            try {
                ArrayMap<String, String> codePathsProfileNames = getPackageProfileNames(pkg);
                int i = codePathsProfileNames.size() - 1;
                while (true) {
                    int i2 = i;
                    if (i2 < 0) {
                        break;
                    }
                    String codePath = codePathsProfileNames.keyAt(i2);
                    String profileName = codePathsProfileNames.valueAt(i2);
                    File dexMetadata = DexMetadataHelper.findDexMetadataForFile(new File(codePath));
                    String dexMetadataPath = dexMetadata == null ? null : dexMetadata.getAbsolutePath();
                    synchronized (this.mInstaller) {
                        if (!this.mInstaller.prepareAppProfile(pkg.packageName, user, appId, profileName, codePath, dexMetadataPath)) {
                            Slog.e(TAG, "Failed to prepare profile for " + pkg.packageName + ":" + codePath);
                        }
                    }
                    i = i2 - 1;
                }
            } catch (Installer.InstallerException e) {
                Slog.e(TAG, "Failed to prepare profile for " + pkg.packageName, e);
            }
        }
    }

    public void prepareAppProfiles(PackageParser.Package pkg, int[] user) {
        for (int prepareAppProfiles : user) {
            prepareAppProfiles(pkg, prepareAppProfiles);
        }
    }

    public void clearAppProfiles(PackageParser.Package pkg) {
        try {
            ArrayMap<String, String> packageProfileNames = getPackageProfileNames(pkg);
            for (int i = packageProfileNames.size() - 1; i >= 0; i--) {
                this.mInstaller.clearAppProfiles(pkg.packageName, packageProfileNames.valueAt(i));
            }
        } catch (Installer.InstallerException e) {
            Slog.w(TAG, String.valueOf(e));
        }
    }

    public void dumpProfiles(PackageParser.Package pkg) {
        int sharedGid = UserHandle.getSharedAppGid(pkg.applicationInfo.uid);
        try {
            ArrayMap<String, String> packageProfileNames = getPackageProfileNames(pkg);
            for (int i = packageProfileNames.size() - 1; i >= 0; i--) {
                String codePath = packageProfileNames.keyAt(i);
                String profileName = packageProfileNames.valueAt(i);
                synchronized (this.mInstallLock) {
                    this.mInstaller.dumpProfiles(sharedGid, pkg.packageName, profileName, codePath);
                }
            }
        } catch (Installer.InstallerException e) {
            Slog.w(TAG, "Failed to dump profiles", e);
        }
    }

    private ArrayMap<String, String> getPackageProfileNames(PackageParser.Package pkg) {
        ArrayMap<String, String> result = new ArrayMap<>();
        if ((pkg.applicationInfo.flags & 4) != 0) {
            result.put(pkg.baseCodePath, ArtManager.getProfileName(null));
        }
        if (!ArrayUtils.isEmpty(pkg.splitCodePaths)) {
            for (int i = 0; i < pkg.splitCodePaths.length; i++) {
                if ((pkg.splitFlags[i] & 4) != 0) {
                    result.put(pkg.splitCodePaths[i], ArtManager.getProfileName(pkg.splitNames[i]));
                }
            }
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    public static int getCompilationReasonTronValue(String compilationReason) {
        char c;
        switch (compilationReason.hashCode()) {
            case -1968171580:
                if (compilationReason.equals("bg-dexopt")) {
                    c = 5;
                    break;
                }
            case -1425983632:
                if (compilationReason.equals("ab-ota")) {
                    c = 6;
                    break;
                }
            case -903566235:
                if (compilationReason.equals("shared")) {
                    c = 8;
                    break;
                }
            case -284840886:
                if (compilationReason.equals(UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN)) {
                    c = 0;
                    break;
                }
            case -207505425:
                if (compilationReason.equals("first-boot")) {
                    c = 2;
                    break;
                }
            case 3029746:
                if (compilationReason.equals("boot")) {
                    c = 3;
                    break;
                }
            case 24665195:
                if (compilationReason.equals("inactive")) {
                    c = 7;
                    break;
                }
            case 96784904:
                if (compilationReason.equals("error")) {
                    c = 1;
                    break;
                }
            case 1022487562:
                if (compilationReason.equals("bg-speed-dexopt")) {
                    c = 9;
                    break;
                }
            case 1957569947:
                if (compilationReason.equals("install")) {
                    c = 4;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 1;
            case 1:
                return 0;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            case 7:
                return 7;
            case 8:
                return 8;
            case 9:
                return 9;
            default:
                return 1;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    public static int getCompilationFilterTronValue(String compilationFilter) {
        char c;
        String str = compilationFilter;
        switch (compilationFilter.hashCode()) {
            case -1957514039:
                if (str.equals("assume-verified")) {
                    c = 2;
                    break;
                }
            case -1803365233:
                if (str.equals("everything-profile")) {
                    c = 10;
                    break;
                }
            case -1305289599:
                if (str.equals("extract")) {
                    c = 3;
                    break;
                }
            case -1129892317:
                if (str.equals("speed-profile")) {
                    c = 8;
                    break;
                }
            case -902315795:
                if (str.equals("run-from-vdex-fallback")) {
                    c = 14;
                    break;
                }
            case -819951495:
                if (str.equals("verify")) {
                    c = 4;
                    break;
                }
            case -284840886:
                if (str.equals(UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN)) {
                    c = 1;
                    break;
                }
            case 96784904:
                if (str.equals("error")) {
                    c = 0;
                    break;
                }
            case 109637894:
                if (str.equals("space")) {
                    c = 7;
                    break;
                }
            case 109641799:
                if (str.equals("speed")) {
                    c = 9;
                    break;
                }
            case 348518370:
                if (str.equals("space-profile")) {
                    c = 6;
                    break;
                }
            case 401590963:
                if (str.equals("everything")) {
                    c = 11;
                    break;
                }
            case 658336598:
                if (str.equals("quicken")) {
                    c = 5;
                    break;
                }
            case 922064507:
                if (str.equals("run-from-apk")) {
                    c = 12;
                    break;
                }
            case 1906552308:
                if (str.equals("run-from-apk-fallback")) {
                    c = 13;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            case 7:
                return 7;
            case 8:
                return 8;
            case 9:
                return 9;
            case 10:
                return 10;
            case 11:
                return 11;
            case 12:
                return 12;
            case 13:
                return 13;
            case 14:
                return 14;
            default:
                return 1;
        }
    }

    private static void verifyTronLoggingConstants() {
        for (String reason : PackageManagerServiceCompilerMapping.REASON_STRINGS) {
            int value = getCompilationReasonTronValue(reason);
            if (value == 0 || value == 1) {
                throw new IllegalArgumentException("Compilation reason not configured for TRON logging: " + reason);
            }
        }
    }
}

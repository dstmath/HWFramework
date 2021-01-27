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
import com.android.internal.os.ZygoteInit;
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
    public static final String DEXOPT_REASON_WITH_DEX_METADATA_ANNOTATION = "-dm";
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
    private static final int TRON_COMPILATION_REASON_BOOT = 3;
    private static final int TRON_COMPILATION_REASON_ERROR = 0;
    private static final int TRON_COMPILATION_REASON_FIRST_BOOT = 2;
    private static final int TRON_COMPILATION_REASON_INACTIVE = 7;
    private static final int TRON_COMPILATION_REASON_INSTALL = 4;
    private static final int TRON_COMPILATION_REASON_INSTALL_WITH_DEX_METADATA = 9;
    private static final int TRON_COMPILATION_REASON_SHARED = 8;
    private static final int TRON_COMPILATION_REASON_UNKNOWN = 1;
    private final Context mContext;
    private final Handler mHandler = new Handler(BackgroundThread.getHandler().getLooper());
    private final Object mInstallLock;
    @GuardedBy({"mInstallLock"})
    private final Installer mInstaller;
    private final IPackageManager mPackageManager;

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
        if (info != null && (info.applicationInfo.flags & 2) == 2) {
            return true;
        }
        return false;
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
            } else {
                throw new IllegalStateException("Runtime profiling is not enabled for " + profileType);
            }
        } else {
            try {
                callback.onError(2);
            } catch (RemoteException e) {
            }
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

    private void createProfileSnapshot(String packageName, String profileName, String classpath, int appId, ISnapshotRuntimeProfileCallback callback) {
        synchronized (this.mInstallLock) {
            try {
                if (!this.mInstaller.createProfileSnapshot(appId, packageName, profileName, classpath)) {
                    postError(callback, packageName, 2);
                    return;
                }
            } catch (Installer.InstallerException e) {
                postError(callback, packageName, 2);
                return;
            }
        }
        File snapshotProfile = ArtManager.getProfileSnapshotFileForName(packageName, profileName);
        try {
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(snapshotProfile, 268435456);
            if (fd != null) {
                if (fd.getFileDescriptor().valid()) {
                    postSuccess(packageName, fd, callback);
                    return;
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("ParcelFileDescriptor.open returned an invalid descriptor for ");
            sb.append(packageName);
            sb.append(":");
            sb.append(snapshotProfile);
            sb.append(". isNull=");
            sb.append(fd == null);
            Slog.wtf(TAG, sb.toString());
            postError(callback, packageName, 2);
        } catch (FileNotFoundException e2) {
            Slog.w(TAG, "Could not open snapshot profile for " + packageName + ":" + snapshotProfile, e2);
            postError(callback, packageName, 2);
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
        if (callingUid != 2000 && !checkAndroidPermissions(callingUid, callingPackage)) {
            return false;
        }
        if (profileType == 0) {
            return SystemProperties.getBoolean("dalvik.vm.usejitprofiles", false);
        }
        if (profileType != 1) {
            throw new IllegalArgumentException("Invalid profile type:" + profileType);
        } else if ((Build.IS_USERDEBUG || Build.IS_ENG) && SystemProperties.getBoolean("dalvik.vm.usejitprofiles", false) && SystemProperties.getBoolean("dalvik.vm.profilebootimage", false)) {
            return true;
        } else {
            return false;
        }
    }

    private void snapshotBootImageProfile(ISnapshotRuntimeProfileCallback callback) {
        createProfileSnapshot("android", BOOT_IMAGE_PROFILE_NAME, String.join(":", Os.getenv("BOOTCLASSPATH"), Os.getenv("SYSTEMSERVERCLASSPATH")), -1, callback);
        destroyProfileSnapshot("android", BOOT_IMAGE_PROFILE_NAME);
    }

    private void postError(ISnapshotRuntimeProfileCallback callback, String packageName, int errCode) {
        if (DEBUG) {
            Slog.d(TAG, "Failed to snapshot profile for " + packageName + " with error: " + errCode);
        }
        this.mHandler.post(new Runnable(callback, errCode, packageName) {
            /* class com.android.server.pm.dex.$$Lambda$ArtManagerService$_rD0Y6OPSJHMdjTIOtucoGQ1xag */
            private final /* synthetic */ ISnapshotRuntimeProfileCallback f$0;
            private final /* synthetic */ int f$1;
            private final /* synthetic */ String f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
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
            /* class com.android.server.pm.dex.$$Lambda$ArtManagerService$MEVzUorlv4msZVFbA5NLti04g */
            private final /* synthetic */ ParcelFileDescriptor f$0;
            private final /* synthetic */ ISnapshotRuntimeProfileCallback f$1;
            private final /* synthetic */ String f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
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

    public void prepareAppProfiles(PackageParser.Package pkg, int user, boolean updateReferenceProfileContent) {
        String dexMetadataPath;
        Installer installer;
        int appId = UserHandle.getAppId(pkg.applicationInfo.uid);
        if (user < 0) {
            Slog.wtf(TAG, "Invalid user id: " + user);
        } else if (appId < 0) {
            Slog.wtf(TAG, "Invalid app id: " + appId);
        } else {
            try {
                ArrayMap<String, String> codePathsProfileNames = getPackageProfileNames(pkg);
                for (int i = codePathsProfileNames.size() - 1; i >= 0; i--) {
                    String codePath = codePathsProfileNames.keyAt(i);
                    String profileName = codePathsProfileNames.valueAt(i);
                    if (updateReferenceProfileContent) {
                        File dexMetadata = DexMetadataHelper.findDexMetadataForFile(new File(codePath));
                        dexMetadataPath = dexMetadata == null ? null : dexMetadata.getAbsolutePath();
                    } else {
                        dexMetadataPath = null;
                    }
                    Installer installer2 = this.mInstaller;
                    synchronized (installer2) {
                        try {
                            installer = installer2;
                            if (!this.mInstaller.prepareAppProfile(pkg.packageName, user, appId, profileName, codePath, dexMetadataPath)) {
                                Slog.e(TAG, "Failed to prepare profile for " + pkg.packageName + ":" + codePath);
                            }
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                }
            } catch (Installer.InstallerException e) {
                Slog.e(TAG, "Failed to prepare profile for " + pkg.packageName, e);
            }
        }
    }

    public void prepareAppProfiles(PackageParser.Package pkg, int[] user, boolean updateReferenceProfileContent) {
        for (int i : user) {
            prepareAppProfiles(pkg, i, updateReferenceProfileContent);
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

    public boolean compileLayouts(PackageParser.Package pkg) {
        boolean compileLayouts;
        try {
            String packageName = pkg.packageName;
            String apkPath = pkg.baseCodePath;
            ApplicationInfo appInfo = pkg.applicationInfo;
            String outDexFile = appInfo.dataDir + "/code_cache/compiled_view.dex";
            if (!appInfo.isPrivilegedApp() && !appInfo.isEmbeddedDexUsed()) {
                if (!appInfo.isDefaultToDeviceProtectedStorage()) {
                    Log.i("PackageManager", "Compiling layouts in " + packageName + " (" + apkPath + ") to " + outDexFile);
                    long callingId = Binder.clearCallingIdentity();
                    try {
                        synchronized (this.mInstallLock) {
                            compileLayouts = this.mInstaller.compileLayouts(apkPath, packageName, outDexFile, appInfo.uid);
                        }
                        return compileLayouts;
                    } finally {
                        Binder.restoreCallingIdentity(callingId);
                    }
                }
            }
            return false;
        } catch (Throwable e) {
            Log.e("PackageManager", "Failed to compile layouts", e);
            return false;
        }
    }

    private ArrayMap<String, String> getPackageProfileNames(PackageParser.Package pkg) {
        ArrayMap<String, String> result = new ArrayMap<>();
        if ((pkg.applicationInfo.flags & 4) != 0) {
            result.put(pkg.baseCodePath, ArtManager.getProfileName((String) null));
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
    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static int getCompilationReasonTronValue(String compilationReason) {
        char c;
        switch (compilationReason.hashCode()) {
            case -1968171580:
                if (compilationReason.equals("bg-dexopt")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -1425983632:
                if (compilationReason.equals("ab-ota")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -903566235:
                if (compilationReason.equals("shared")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -284840886:
                if (compilationReason.equals(UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -207505425:
                if (compilationReason.equals("first-boot")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 3029746:
                if (compilationReason.equals("boot")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 24665195:
                if (compilationReason.equals("inactive")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 96784904:
                if (compilationReason.equals("error")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 900392443:
                if (compilationReason.equals("install-dm")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 1957569947:
                if (compilationReason.equals("install")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
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
            case '\b':
                return 8;
            case '\t':
                return 9;
            default:
                return 1;
        }
    }

    /* access modifiers changed from: private */
    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static int getCompilationFilterTronValue(String compilationFilter) {
        char c;
        switch (compilationFilter.hashCode()) {
            case -1957514039:
                if (compilationFilter.equals("assume-verified")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1803365233:
                if (compilationFilter.equals("everything-profile")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case -1305289599:
                if (compilationFilter.equals("extract")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -1129892317:
                if (compilationFilter.equals("speed-profile")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -902315795:
                if (compilationFilter.equals("run-from-vdex-fallback")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -819951495:
                if (compilationFilter.equals("verify")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -284840886:
                if (compilationFilter.equals(UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 96784904:
                if (compilationFilter.equals("error")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 109637894:
                if (compilationFilter.equals("space")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 109641799:
                if (compilationFilter.equals("speed")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 348518370:
                if (compilationFilter.equals("space-profile")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 401590963:
                if (compilationFilter.equals("everything")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 658336598:
                if (compilationFilter.equals("quicken")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 922064507:
                if (compilationFilter.equals("run-from-apk")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 1906552308:
                if (compilationFilter.equals("run-from-apk-fallback")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
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
            case '\b':
                return 8;
            case '\t':
                return 9;
            case '\n':
                return 10;
            case 11:
                return 11;
            case '\f':
                return 12;
            case '\r':
                return 13;
            case 14:
                return 14;
            default:
                return 1;
        }
    }

    private static void verifyTronLoggingConstants() {
        for (int i = 0; i < PackageManagerServiceCompilerMapping.REASON_STRINGS.length; i++) {
            String reason = PackageManagerServiceCompilerMapping.REASON_STRINGS[i];
            int value = getCompilationReasonTronValue(reason);
            if (value == 0 || value == 1) {
                throw new IllegalArgumentException("Compilation reason not configured for TRON logging: " + reason);
            }
        }
    }

    private class ArtManagerInternalImpl extends ArtManagerInternal {
        private static final int OPTINFOS_LENGTH = 2;

        private ArtManagerInternalImpl() {
        }

        public PackageOptimizationInfo getPackageOptimizationInfo(ApplicationInfo info, String abi) {
            String compilationFilter;
            String compilationFilter2 = "error";
            try {
                String isa = VMRuntime.getInstructionSet(abi);
                if (ZygoteInit.sIsMygote) {
                    String[] optInfos = ArtManagerService.this.mInstaller.getDexFileOptimizationInfo(new String[]{info.getBaseCodePath()}, new String[]{isa}, new int[]{info.uid});
                    if (optInfos == null || optInfos.length != 2) {
                        compilationFilter = compilationFilter2;
                    } else {
                        compilationFilter = optInfos[0];
                        compilationFilter2 = optInfos[1];
                    }
                } else {
                    DexFile.OptimizationInfo optInfo = DexFile.getDexFileOptimizationInfo(info.getBaseCodePath(), isa);
                    String compilationFilter3 = optInfo.getStatus();
                    compilationFilter2 = optInfo.getReason();
                    compilationFilter = compilationFilter3;
                }
            } catch (Installer.InstallerException e) {
                Slog.e(ArtManagerService.TAG, "Could not get optimizations status for " + info.getBaseCodePath(), e);
                compilationFilter = "error";
                compilationFilter2 = "error";
            } catch (FileNotFoundException e2) {
                Slog.e(ArtManagerService.TAG, "Could not get optimizations status for " + info.getBaseCodePath(), e2);
                compilationFilter = "error";
                compilationFilter2 = "error";
            } catch (IllegalArgumentException e3) {
                Slog.wtf(ArtManagerService.TAG, "Requested optimization status for " + info.getBaseCodePath() + " due to an invalid abi " + abi, e3);
                compilationFilter = "error";
                compilationFilter2 = "error";
            }
            return new PackageOptimizationInfo(ArtManagerService.getCompilationFilterTronValue(compilationFilter), ArtManagerService.getCompilationReasonTronValue(compilationFilter2));
        }
    }
}

package com.android.server.pm;

import android.accounts.IAccountManager;
import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageInstaller;
import android.content.pm.IPackageManager;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.ParceledListSlice;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.pm.VersionedPackage;
import android.content.pm.dex.DexMetadataHelper;
import android.content.pm.dex.ISnapshotRuntimeProfileCallback;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.hdm.HwDeviceManager;
import android.net.Uri;
import android.net.util.NetworkConstants;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IUserManager;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.PrintWriterPrinter;
import com.android.internal.content.PackageHelper;
import com.android.internal.util.ArrayUtils;
import com.android.server.BatteryService;
import com.android.server.LocalServices;
import com.android.server.SystemConfig;
import com.android.server.UiModeManagerService;
import com.android.server.backup.internal.BackupHandler;
import com.android.server.net.NetworkPolicyManagerService;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.voiceinteraction.DatabaseHelper;
import com.android.server.wm.WindowManagerService;
import dalvik.system.DexFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import libcore.io.IoUtils;
import libcore.io.Streams;

class PackageManagerShellCommand extends ShellCommand {
    private static final String ART_PROFILE_SNAPSHOT_DEBUG_LOCATION = "/data/misc/profman/";
    private static final int REPAIR_MODE_USER_ID = 127;
    private static final String STDIN_PATH = "-";
    boolean mBrief;
    boolean mComponents;
    final IPackageManager mInterface;
    private final WeakHashMap<String, Resources> mResourceCache = new WeakHashMap<>();
    int mTargetUser;

    static class ClearDataObserver extends IPackageDataObserver.Stub {
        boolean finished;
        boolean result;

        ClearDataObserver() {
        }

        public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
            synchronized (this) {
                this.finished = true;
                this.result = succeeded;
                notifyAll();
            }
        }
    }

    private static class InstallParams {
        String installerPackageName;
        PackageInstaller.SessionParams sessionParams;
        int userId;

        private InstallParams() {
            this.userId = -1;
        }
    }

    private static class LocalIntentReceiver {
        private IIntentSender.Stub mLocalSender;
        /* access modifiers changed from: private */
        public final SynchronousQueue<Intent> mResult;

        private LocalIntentReceiver() {
            this.mResult = new SynchronousQueue<>();
            this.mLocalSender = new IIntentSender.Stub() {
                public void send(int code, Intent intent, String resolvedType, IBinder whitelistToken, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
                    try {
                        LocalIntentReceiver.this.mResult.offer(intent, 5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }

        public IntentSender getIntentSender() {
            return new IntentSender(this.mLocalSender);
        }

        public Intent getResult() {
            try {
                return this.mResult.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class SnapshotRuntimeProfileCallback extends ISnapshotRuntimeProfileCallback.Stub {
        private CountDownLatch mDoneSignal;
        /* access modifiers changed from: private */
        public int mErrCode;
        /* access modifiers changed from: private */
        public ParcelFileDescriptor mProfileReadFd;
        private boolean mSuccess;

        private SnapshotRuntimeProfileCallback() {
            this.mSuccess = false;
            this.mErrCode = -1;
            this.mProfileReadFd = null;
            this.mDoneSignal = new CountDownLatch(1);
        }

        public void onSuccess(ParcelFileDescriptor profileReadFd) {
            this.mSuccess = true;
            try {
                this.mProfileReadFd = profileReadFd.dup();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.mDoneSignal.countDown();
        }

        public void onError(int errCode) {
            this.mSuccess = false;
            this.mErrCode = errCode;
            this.mDoneSignal.countDown();
        }

        /* access modifiers changed from: package-private */
        public boolean waitTillDone() {
            boolean done = false;
            try {
                done = this.mDoneSignal.await(10000000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
            }
            if (!done || !this.mSuccess) {
                return false;
            }
            return true;
        }
    }

    PackageManagerShellCommand(PackageManagerService service) {
        this.mInterface = service;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    public int onCommand(String cmd) {
        char c;
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter pw = getOutPrintWriter();
        try {
            switch (cmd.hashCode()) {
                case -2102802879:
                    if (cmd.equals("set-harmful-app-warning")) {
                        c = '7';
                        break;
                    }
                case -1967190973:
                    if (cmd.equals("install-abandon")) {
                        c = 8;
                        break;
                    }
                case -1937348290:
                    if (cmd.equals("get-install-location")) {
                        c = 16;
                        break;
                    }
                case -1852006340:
                    if (cmd.equals("suspend")) {
                        c = '\"';
                        break;
                    }
                case -1846646502:
                    if (cmd.equals("get-max-running-users")) {
                        c = '2';
                        break;
                    }
                case -1741208611:
                    if (cmd.equals("set-installer")) {
                        c = '4';
                        break;
                    }
                case -1347307837:
                    if (cmd.equals("has-feature")) {
                        c = '6';
                        break;
                    }
                case -1298848381:
                    if (cmd.equals("enable")) {
                        c = 27;
                        break;
                    }
                case -1267782244:
                    if (cmd.equals("get-instantapp-resolver")) {
                        c = '5';
                        break;
                    }
                case -1231004208:
                    if (cmd.equals("resolve-activity")) {
                        c = 3;
                        break;
                    }
                case -1102348235:
                    if (cmd.equals("get-privapp-deny-permissions")) {
                        c = ')';
                        break;
                    }
                case -1091400553:
                    if (cmd.equals("get-oem-permissions")) {
                        c = '*';
                        break;
                    }
                case -1070704814:
                    if (cmd.equals("get-privapp-permissions")) {
                        c = '(';
                        break;
                    }
                case -1032029296:
                    if (cmd.equals("disable-user")) {
                        c = 29;
                        break;
                    }
                case -934343034:
                    if (cmd.equals("revoke")) {
                        c = '%';
                        break;
                    }
                case -919935069:
                    if (cmd.equals("dump-profiles")) {
                        c = 23;
                        break;
                    }
                case -840566949:
                    if (cmd.equals("unhide")) {
                        c = '!';
                        break;
                    }
                case -625596190:
                    if (cmd.equals("uninstall")) {
                        c = 25;
                        break;
                    }
                case -623224643:
                    if (cmd.equals("get-app-link")) {
                        c = ',';
                        break;
                    }
                case -539710980:
                    if (cmd.equals("create-user")) {
                        c = '.';
                        break;
                    }
                case -458695741:
                    if (cmd.equals("query-services")) {
                        c = 5;
                        break;
                    }
                case -444750796:
                    if (cmd.equals("bg-dexopt-job")) {
                        c = 22;
                        break;
                    }
                case -440994401:
                    if (cmd.equals("query-receivers")) {
                        c = 6;
                        break;
                    }
                case -339687564:
                    if (cmd.equals("remove-user")) {
                        c = '/';
                        break;
                    }
                case -220055275:
                    if (cmd.equals("set-permission-enforced")) {
                        c = '\'';
                        break;
                    }
                case -140205181:
                    if (cmd.equals("unsuspend")) {
                        c = '#';
                        break;
                    }
                case -132384343:
                    if (cmd.equals("install-commit")) {
                        c = 10;
                        break;
                    }
                case -129863314:
                    if (cmd.equals("install-create")) {
                        c = 11;
                        break;
                    }
                case -115000827:
                    if (cmd.equals("default-state")) {
                        c = 31;
                        break;
                    }
                case -87258188:
                    if (cmd.equals("move-primary-storage")) {
                        c = 18;
                        break;
                    }
                case 3095028:
                    if (cmd.equals("dump")) {
                        c = 1;
                        break;
                    }
                case 3202370:
                    if (cmd.equals("hide")) {
                        c = ' ';
                        break;
                    }
                case 3322014:
                    if (cmd.equals("list")) {
                        c = 2;
                        break;
                    }
                case 3433509:
                    if (cmd.equals("path")) {
                        c = 0;
                        break;
                    }
                case 18936394:
                    if (cmd.equals("move-package")) {
                        c = 17;
                        break;
                    }
                case 86600360:
                    if (cmd.equals("get-max-users")) {
                        c = '1';
                        break;
                    }
                case 94746189:
                    if (cmd.equals("clear")) {
                        c = 26;
                        break;
                    }
                case 98615580:
                    if (cmd.equals("grant")) {
                        c = '$';
                        break;
                    }
                case 107262333:
                    if (cmd.equals("install-existing")) {
                        c = 14;
                        break;
                    }
                case 139892533:
                    if (cmd.equals("get-harmful-app-warning")) {
                        c = '8';
                        break;
                    }
                case 287820022:
                    if (cmd.equals("install-remove")) {
                        c = 12;
                        break;
                    }
                case 359572742:
                    if (cmd.equals("reset-permissions")) {
                        c = '&';
                        break;
                    }
                case 467549856:
                    if (cmd.equals("snapshot-profile")) {
                        c = 24;
                        break;
                    }
                case 798023112:
                    if (cmd.equals("install-destroy")) {
                        c = 9;
                        break;
                    }
                case 826473335:
                    if (cmd.equals("uninstall-system-updates")) {
                        c = '9';
                        break;
                    }
                case 925176533:
                    if (cmd.equals("set-user-restriction")) {
                        c = '0';
                        break;
                    }
                case 925767985:
                    if (cmd.equals("set-app-link")) {
                        c = '+';
                        break;
                    }
                case 950491699:
                    if (cmd.equals("compile")) {
                        c = 19;
                        break;
                    }
                case 1053409810:
                    if (cmd.equals("query-activities")) {
                        c = 4;
                        break;
                    }
                case 1124603675:
                    if (cmd.equals("force-dex-opt")) {
                        c = 21;
                        break;
                    }
                case 1177857340:
                    if (cmd.equals("trim-caches")) {
                        c = '-';
                        break;
                    }
                case 1429366290:
                    if (cmd.equals("set-home-activity")) {
                        c = '3';
                        break;
                    }
                case 1538306349:
                    if (cmd.equals("install-write")) {
                        c = 13;
                        break;
                    }
                case 1671308008:
                    if (cmd.equals("disable")) {
                        c = 28;
                        break;
                    }
                case 1697997009:
                    if (cmd.equals("disable-until-used")) {
                        c = 30;
                        break;
                    }
                case 1746695602:
                    if (cmd.equals("set-install-location")) {
                        c = 15;
                        break;
                    }
                case 1783979817:
                    if (cmd.equals("reconcile-secondary-dex-files")) {
                        c = 20;
                        break;
                    }
                case 1957569947:
                    if (cmd.equals("install")) {
                        c = 7;
                        break;
                    }
            }
            c = 65535;
            switch (c) {
                case 0:
                    return runPath();
                case 1:
                    return runDump();
                case 2:
                    return runList();
                case 3:
                    return runResolveActivity();
                case 4:
                    return runQueryIntentActivities();
                case 5:
                    return runQueryIntentServices();
                case 6:
                    return runQueryIntentReceivers();
                case 7:
                    return runInstall();
                case 8:
                case 9:
                    return runInstallAbandon();
                case 10:
                    return runInstallCommit();
                case 11:
                    return runInstallCreate();
                case 12:
                    return runInstallRemove();
                case 13:
                    return runInstallWrite();
                case 14:
                    return runInstallExisting();
                case 15:
                    return runSetInstallLocation();
                case 16:
                    return runGetInstallLocation();
                case 17:
                    return runMovePackage();
                case 18:
                    return runMovePrimaryStorage();
                case WindowManagerService.H.REPORT_WINDOWS_CHANGE /*19*/:
                    return runCompile();
                case 20:
                    return runreconcileSecondaryDexFiles();
                case BackupHandler.MSG_OP_COMPLETE:
                    return runForceDexOpt();
                case WindowManagerService.H.REPORT_HARD_KEYBOARD_STATUS_CHANGE /*22*/:
                    return runDexoptJob();
                case WindowManagerService.H.BOOT_TIMEOUT /*23*/:
                    return runDumpProfiles();
                case 24:
                    return runSnapshotProfile();
                case WindowManagerService.H.SHOW_STRICT_MODE_VIOLATION /*25*/:
                    return runUninstall();
                case WindowManagerService.H.DO_ANIMATION_CALLBACK /*26*/:
                    int userId = UserHandle.getCallingUserId();
                    try {
                        userId = ActivityManager.getCurrentUser();
                    } catch (Exception e) {
                        pw.println("Exception:" + e.getMessage());
                    }
                    if (userId != REPAIR_MODE_USER_ID) {
                        return runClear();
                    }
                    pw.println("Failure [adb shell pm clear is not supported in REPAIR MODE]");
                    return -1;
                case 27:
                    return runSetEnabledSetting(1);
                case NetworkConstants.ARP_PAYLOAD_LEN:
                    return runSetEnabledSetting(2);
                case HdmiCecKeycode.CEC_KEYCODE_NUMBER_ENTRY_MODE:
                    return runSetEnabledSetting(3);
                case 30:
                    return runSetEnabledSetting(4);
                case HdmiCecKeycode.CEC_KEYCODE_NUMBER_12:
                    return runSetEnabledSetting(0);
                case ' ':
                    return runSetHiddenSetting(true);
                case '!':
                    return runSetHiddenSetting(false);
                case '\"':
                    return runSuspend(true);
                case '#':
                    return runSuspend(false);
                case '$':
                    return runGrantRevokePermission(true);
                case '%':
                    return runGrantRevokePermission(false);
                case '&':
                    return runResetPermissions();
                case '\'':
                    return runSetPermissionEnforced();
                case '(':
                    return runGetPrivappPermissions();
                case ')':
                    return runGetPrivappDenyPermissions();
                case HdmiCecKeycode.CEC_KEYCODE_DOT:
                    return runGetOemPermissions();
                case HdmiCecKeycode.CEC_KEYCODE_ENTER:
                    return runSetAppLink();
                case HdmiCecKeycode.CEC_KEYCODE_CLEAR:
                    return runGetAppLink();
                case NetworkPolicyManagerService.TYPE_RAPID:
                    return runTrimCaches();
                case WindowManagerService.H.WINDOW_REPLACEMENT_TIMEOUT /*46*/:
                    return runCreateUser();
                case '/':
                    return runRemoveUser();
                case '0':
                    return runSetUserRestriction();
                case '1':
                    return runGetMaxUsers();
                case HdmiCecKeycode.CEC_KEYCODE_PREVIOUS_CHANNEL:
                    return runGetMaxRunningUsers();
                case '3':
                    return runSetHomeActivity();
                case '4':
                    return runSetInstaller();
                case '5':
                    return runGetInstantAppResolver();
                case '6':
                    return runHasFeature();
                case '7':
                    return runSetHarmfulAppWarning();
                case '8':
                    return runGetHarmfulAppWarning();
                case WindowManagerService.H.NOTIFY_KEYGUARD_TRUSTED_CHANGED /*57*/:
                    return uninstallSystemUpdates();
                default:
                    String nextArg = getNextArg();
                    if (nextArg == null) {
                        if (cmd.equalsIgnoreCase("-l")) {
                            return runListPackages(false);
                        }
                        if (cmd.equalsIgnoreCase("-lf")) {
                            return runListPackages(true);
                        }
                    } else if (getNextArg() == null && cmd.equalsIgnoreCase("-p")) {
                        return displayPackageFilePath(nextArg, 0);
                    }
                    return handleDefaultCommands(cmd);
            }
        } catch (RemoteException e2) {
            pw.println("Remote exception: " + e2);
            return -1;
        }
        pw.println("Remote exception: " + e2);
        return -1;
    }

    private int uninstallSystemUpdates() {
        PrintWriter pw = getOutPrintWriter();
        List<String> failedUninstalls = new LinkedList<>();
        try {
            ParceledListSlice<ApplicationInfo> packages = this.mInterface.getInstalledApplications(DumpState.DUMP_DEXOPT, 0);
            IPackageInstaller installer = this.mInterface.getPackageInstaller();
            for (ApplicationInfo info : packages.getList()) {
                if (info.isUpdatedSystemApp()) {
                    pw.println("Uninstalling updates to " + info.packageName + "...");
                    LocalIntentReceiver receiver = new LocalIntentReceiver();
                    installer.uninstall(new VersionedPackage(info.packageName, info.versionCode), null, 0, receiver.getIntentSender(), 0);
                    if (receiver.getResult().getIntExtra("android.content.pm.extra.STATUS", 1) != 0) {
                        failedUninstalls.add(info.packageName);
                    }
                }
            }
            if (!failedUninstalls.isEmpty()) {
                pw.println("Failure [Couldn't uninstall packages: " + TextUtils.join(", ", failedUninstalls) + "]");
                return 0;
            }
            pw.println("Success");
            return 1;
        } catch (RemoteException e) {
            pw.println("Failure [" + e.getClass().getName() + " - " + e.getMessage() + "]");
            return 0;
        }
    }

    private void setParamsSize(InstallParams params, String inPath) {
        if (params.sessionParams.sizeBytes == -1 && !STDIN_PATH.equals(inPath)) {
            ParcelFileDescriptor fd = openFileForSystem(inPath, "r");
            if (fd != null) {
                try {
                    PackageParser.PackageLite packageLite = new PackageParser.PackageLite(null, PackageParser.parseApkLite(fd.getFileDescriptor(), inPath, 0), null, null, null, null, null, null);
                    params.sessionParams.setSize(PackageHelper.calculateInstalledSize(packageLite, params.sessionParams.abiOverride, fd.getFileDescriptor()));
                    try {
                        fd.close();
                    } catch (IOException e) {
                    }
                } catch (PackageParser.PackageParserException | IOException e2) {
                    PrintWriter errPrintWriter = getErrPrintWriter();
                    errPrintWriter.println("Error: Failed to parse APK file: " + inPath);
                    throw new IllegalArgumentException("Error: Failed to parse APK file: " + inPath, e2);
                } catch (Throwable th) {
                    try {
                        fd.close();
                    } catch (IOException e3) {
                    }
                    throw th;
                }
            } else {
                PrintWriter errPrintWriter2 = getErrPrintWriter();
                errPrintWriter2.println("Error: Can't open file: " + inPath);
                throw new IllegalArgumentException("Error: Can't open file: " + inPath);
            }
        }
    }

    private int displayPackageFilePath(String pckg, int userId) throws RemoteException {
        PackageInfo info = this.mInterface.getPackageInfo(pckg, 0, userId);
        if (info == null || info.applicationInfo == null) {
            return 1;
        }
        PrintWriter pw = getOutPrintWriter();
        pw.print("package:");
        pw.println(info.applicationInfo.sourceDir);
        if (!ArrayUtils.isEmpty(info.applicationInfo.splitSourceDirs)) {
            for (String splitSourceDir : info.applicationInfo.splitSourceDirs) {
                pw.print("package:");
                pw.println(splitSourceDir);
            }
        }
        return 0;
    }

    private int runPath() throws RemoteException {
        int userId = 0;
        String option = getNextOption();
        if (option != null && option.equals("--user")) {
            userId = UserHandle.parseUserArg(getNextArgRequired());
        }
        String pkg = getNextArgRequired();
        if (pkg != null) {
            return displayPackageFilePath(pkg, userId);
        }
        getErrPrintWriter().println("Error: no package specified");
        return 1;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private int runList() throws RemoteException {
        char c;
        PrintWriter pw = getOutPrintWriter();
        String type = getNextArg();
        if (type == null) {
            pw.println("Error: didn't specify type of data to list");
            return -1;
        }
        switch (type.hashCode()) {
            case -997447790:
                if (type.equals("permission-groups")) {
                    c = 5;
                    break;
                }
            case -807062458:
                if (type.equals("package")) {
                    c = 3;
                    break;
                }
            case -290659267:
                if (type.equals("features")) {
                    c = 0;
                    break;
                }
            case 111578632:
                if (type.equals(DatabaseHelper.SoundModelContract.KEY_USERS)) {
                    c = 7;
                    break;
                }
            case 544550766:
                if (type.equals("instrumentation")) {
                    c = 1;
                    break;
                }
            case 750867693:
                if (type.equals("packages")) {
                    c = 4;
                    break;
                }
            case 812757657:
                if (type.equals("libraries")) {
                    c = 2;
                    break;
                }
            case 1133704324:
                if (type.equals("permissions")) {
                    c = 6;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return runListFeatures();
            case 1:
                return runListInstrumentation();
            case 2:
                return runListLibraries();
            case 3:
            case 4:
                return runListPackages(false);
            case 5:
                return runListPermissionGroups();
            case 6:
                return runListPermissions();
            case 7:
                ServiceManager.getService("user").shellCommand(getInFileDescriptor(), getOutFileDescriptor(), getErrFileDescriptor(), new String[]{"list"}, getShellCallback(), adoptResultReceiver());
                return 0;
            default:
                pw.println("Error: unknown list type '" + type + "'");
                return -1;
        }
    }

    private int runListFeatures() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        List<FeatureInfo> list = this.mInterface.getSystemAvailableFeatures().getList();
        Collections.sort(list, new Comparator<FeatureInfo>() {
            public int compare(FeatureInfo o1, FeatureInfo o2) {
                if (o1.name == o2.name) {
                    return 0;
                }
                if (o1.name == null) {
                    return -1;
                }
                if (o2.name == null) {
                    return 1;
                }
                return o1.name.compareTo(o2.name);
            }
        });
        int count = list != null ? list.size() : 0;
        for (int p = 0; p < count; p++) {
            FeatureInfo fi = list.get(p);
            pw.print("feature:");
            if (fi.name != null) {
                pw.print(fi.name);
                if (fi.version > 0) {
                    pw.print("=");
                    pw.print(fi.version);
                }
                pw.println();
            } else {
                pw.println("reqGlEsVersion=0x" + Integer.toHexString(fi.reqGlEsVersion));
            }
        }
        return 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0025 A[Catch:{ RuntimeException -> 0x009f }] */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0044  */
    private int runListInstrumentation() throws RemoteException {
        boolean z;
        PrintWriter pw = getOutPrintWriter();
        boolean showSourceDir = false;
        String targetPackage = null;
        while (true) {
            try {
                String nextArg = getNextArg();
                String opt = nextArg;
                if (nextArg != null) {
                    if (opt.hashCode() == 1497) {
                        if (opt.equals("-f")) {
                            z = false;
                            if (z) {
                                showSourceDir = true;
                            } else if (opt.charAt(0) != '-') {
                                targetPackage = opt;
                            } else {
                                pw.println("Error: Unknown option: " + opt);
                                return -1;
                            }
                        }
                    }
                    z = true;
                    if (z) {
                    }
                } else {
                    List<InstrumentationInfo> list = this.mInterface.queryInstrumentation(targetPackage, 0).getList();
                    Collections.sort(list, new Comparator<InstrumentationInfo>() {
                        public int compare(InstrumentationInfo o1, InstrumentationInfo o2) {
                            return o1.targetPackage.compareTo(o2.targetPackage);
                        }
                    });
                    int count = list != null ? list.size() : 0;
                    for (int p = 0; p < count; p++) {
                        InstrumentationInfo ii = list.get(p);
                        pw.print("instrumentation:");
                        if (showSourceDir) {
                            pw.print(ii.sourceDir);
                            pw.print("=");
                        }
                        pw.print(new ComponentName(ii.packageName, ii.name).flattenToShortString());
                        pw.print(" (target=");
                        pw.print(ii.targetPackage);
                        pw.println(")");
                    }
                    return 0;
                }
            } catch (RuntimeException ex) {
                pw.println("Error: " + ex.toString());
                return -1;
            }
        }
    }

    private int runListLibraries() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        List<String> list = new ArrayList<>();
        String[] rawList = this.mInterface.getSystemSharedLibraryNames();
        for (String add : rawList) {
            list.add(add);
        }
        Collections.sort(list, new Comparator<String>() {
            public int compare(String o1, String o2) {
                if (o1 == o2) {
                    return 0;
                }
                if (o1 == null) {
                    return -1;
                }
                if (o2 == null) {
                    return 1;
                }
                return o1.compareTo(o2);
            }
        });
        int count = list.size();
        for (int p = 0; p < count; p++) {
            pw.print("library:");
            pw.println(list.get(p));
        }
        return 0;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x015c, code lost:
        if (r3.packageName.contains(r0) == 0) goto L_0x015f;
     */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x00db A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x00de A[Catch:{ RuntimeException -> 0x0122 }] */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x00ea A[Catch:{ RuntimeException -> 0x0122 }] */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x00f5 A[Catch:{ RuntimeException -> 0x0122 }] */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x00f9 A[Catch:{ RuntimeException -> 0x0122 }] */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x00fb A[Catch:{ RuntimeException -> 0x0122 }] */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x00fe A[Catch:{ RuntimeException -> 0x0122 }] */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0100 A[Catch:{ RuntimeException -> 0x0122 }] */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0102 A[Catch:{ RuntimeException -> 0x0122 }] */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0103 A[Catch:{ RuntimeException -> 0x0122 }] */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x0105 A[Catch:{ RuntimeException -> 0x0122 }] */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0107 A[Catch:{ RuntimeException -> 0x0122 }] */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x0109 A[Catch:{ RuntimeException -> 0x0122 }] */
    private int runListPackages(boolean showSourceDir) throws RemoteException {
        String filter;
        int userId;
        char c;
        PackageManagerShellCommand packageManagerShellCommand = this;
        PrintWriter pw = getOutPrintWriter();
        boolean showVersionCode = false;
        int uid = -1;
        boolean listInstaller = false;
        boolean showUid = false;
        boolean listSystem = false;
        boolean listThirdParty = false;
        boolean listDisabled = false;
        boolean listEnabled = false;
        boolean showSourceDir2 = showSourceDir;
        int getFlags = false;
        int userId2 = 0;
        while (true) {
            int userId3 = userId2;
            try {
                String nextOption = getNextOption();
                String opt = nextOption;
                if (nextOption != null) {
                    String opt2 = opt;
                    try {
                        int hashCode = opt2.hashCode();
                        if (hashCode != -493830763) {
                            if (hashCode != 1446) {
                                if (hashCode != 1480) {
                                    if (hashCode != 1500) {
                                        if (hashCode != 1503) {
                                            if (hashCode != 1510) {
                                                if (hashCode != 1512) {
                                                    if (hashCode != 43014832) {
                                                        if (hashCode != 1333469547) {
                                                            switch (hashCode) {
                                                                case 1495:
                                                                    if (opt2.equals("-d")) {
                                                                        c = 0;
                                                                        break;
                                                                    }
                                                                case 1496:
                                                                    if (opt2.equals("-e")) {
                                                                        c = 1;
                                                                        break;
                                                                    }
                                                                case 1497:
                                                                    if (opt2.equals("-f")) {
                                                                        c = 2;
                                                                        break;
                                                                    }
                                                            }
                                                        } else if (opt2.equals("--user")) {
                                                            c = 10;
                                                            switch (c) {
                                                                case 0:
                                                                    listDisabled = true;
                                                                    break;
                                                                case 1:
                                                                    listEnabled = true;
                                                                    break;
                                                                case 2:
                                                                    showSourceDir2 = true;
                                                                    break;
                                                                case 3:
                                                                    listInstaller = true;
                                                                    break;
                                                                case 4:
                                                                    break;
                                                                case 5:
                                                                    listSystem = true;
                                                                    break;
                                                                case 6:
                                                                    showUid = true;
                                                                    break;
                                                                case 7:
                                                                    getFlags |= 8192;
                                                                    break;
                                                                case 8:
                                                                    listThirdParty = true;
                                                                    break;
                                                                case 9:
                                                                    showVersionCode = true;
                                                                    break;
                                                                case 10:
                                                                    userId3 = UserHandle.parseUserArg(getNextArgRequired());
                                                                    break;
                                                                case 11:
                                                                    showUid = true;
                                                                    uid = Integer.parseInt(getNextArgRequired());
                                                                    break;
                                                                default:
                                                                    pw.println("Error: Unknown option: " + opt2);
                                                                    return -1;
                                                            }
                                                            userId2 = userId3;
                                                        }
                                                    } else if (opt2.equals("--uid")) {
                                                        c = 11;
                                                        switch (c) {
                                                            case 0:
                                                                break;
                                                            case 1:
                                                                break;
                                                            case 2:
                                                                break;
                                                            case 3:
                                                                break;
                                                            case 4:
                                                                break;
                                                            case 5:
                                                                break;
                                                            case 6:
                                                                break;
                                                            case 7:
                                                                break;
                                                            case 8:
                                                                break;
                                                            case 9:
                                                                break;
                                                            case 10:
                                                                break;
                                                            case 11:
                                                                break;
                                                        }
                                                        userId2 = userId3;
                                                    }
                                                } else if (opt2.equals("-u")) {
                                                    c = 7;
                                                    switch (c) {
                                                        case 0:
                                                            break;
                                                        case 1:
                                                            break;
                                                        case 2:
                                                            break;
                                                        case 3:
                                                            break;
                                                        case 4:
                                                            break;
                                                        case 5:
                                                            break;
                                                        case 6:
                                                            break;
                                                        case 7:
                                                            break;
                                                        case 8:
                                                            break;
                                                        case 9:
                                                            break;
                                                        case 10:
                                                            break;
                                                        case 11:
                                                            break;
                                                    }
                                                    userId2 = userId3;
                                                }
                                            } else if (opt2.equals("-s")) {
                                                c = 5;
                                                switch (c) {
                                                    case 0:
                                                        break;
                                                    case 1:
                                                        break;
                                                    case 2:
                                                        break;
                                                    case 3:
                                                        break;
                                                    case 4:
                                                        break;
                                                    case 5:
                                                        break;
                                                    case 6:
                                                        break;
                                                    case 7:
                                                        break;
                                                    case 8:
                                                        break;
                                                    case 9:
                                                        break;
                                                    case 10:
                                                        break;
                                                    case 11:
                                                        break;
                                                }
                                                userId2 = userId3;
                                            }
                                        } else if (opt2.equals("-l")) {
                                            c = 4;
                                            switch (c) {
                                                case 0:
                                                    break;
                                                case 1:
                                                    break;
                                                case 2:
                                                    break;
                                                case 3:
                                                    break;
                                                case 4:
                                                    break;
                                                case 5:
                                                    break;
                                                case 6:
                                                    break;
                                                case 7:
                                                    break;
                                                case 8:
                                                    break;
                                                case 9:
                                                    break;
                                                case 10:
                                                    break;
                                                case 11:
                                                    break;
                                            }
                                            userId2 = userId3;
                                        }
                                    } else if (opt2.equals("-i")) {
                                        c = 3;
                                        switch (c) {
                                            case 0:
                                                break;
                                            case 1:
                                                break;
                                            case 2:
                                                break;
                                            case 3:
                                                break;
                                            case 4:
                                                break;
                                            case 5:
                                                break;
                                            case 6:
                                                break;
                                            case 7:
                                                break;
                                            case 8:
                                                break;
                                            case 9:
                                                break;
                                            case 10:
                                                break;
                                            case 11:
                                                break;
                                        }
                                        userId2 = userId3;
                                    }
                                } else if (opt2.equals("-U")) {
                                    c = 6;
                                    switch (c) {
                                        case 0:
                                            break;
                                        case 1:
                                            break;
                                        case 2:
                                            break;
                                        case 3:
                                            break;
                                        case 4:
                                            break;
                                        case 5:
                                            break;
                                        case 6:
                                            break;
                                        case 7:
                                            break;
                                        case 8:
                                            break;
                                        case 9:
                                            break;
                                        case 10:
                                            break;
                                        case 11:
                                            break;
                                    }
                                    userId2 = userId3;
                                }
                            } else if (opt2.equals("-3")) {
                                c = 8;
                                switch (c) {
                                    case 0:
                                        break;
                                    case 1:
                                        break;
                                    case 2:
                                        break;
                                    case 3:
                                        break;
                                    case 4:
                                        break;
                                    case 5:
                                        break;
                                    case 6:
                                        break;
                                    case 7:
                                        break;
                                    case 8:
                                        break;
                                    case 9:
                                        break;
                                    case 10:
                                        break;
                                    case 11:
                                        break;
                                }
                                userId2 = userId3;
                            }
                        } else if (opt2.equals("--show-versioncode")) {
                            c = 9;
                            switch (c) {
                                case 0:
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    break;
                                case 4:
                                    break;
                                case 5:
                                    break;
                                case 6:
                                    break;
                                case 7:
                                    break;
                                case 8:
                                    break;
                                case 9:
                                    break;
                                case 10:
                                    break;
                                case 11:
                                    break;
                            }
                            userId2 = userId3;
                        }
                        c = 65535;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                            case 4:
                                break;
                            case 5:
                                break;
                            case 6:
                                break;
                            case 7:
                                break;
                            case 8:
                                break;
                            case 9:
                                break;
                            case 10:
                                break;
                            case 11:
                                break;
                        }
                        userId2 = userId3;
                    } catch (RuntimeException e) {
                        ex = e;
                        int i = getFlags;
                        int i2 = userId3;
                        pw.println("Error: " + ex.toString());
                        return -1;
                    }
                } else {
                    String filter2 = getNextArg();
                    ParceledListSlice<PackageInfo> slice = packageManagerShellCommand.mInterface.getInstalledPackages(getFlags, userId3);
                    List<PackageInfo> packages = slice.getList();
                    int i3 = getFlags;
                    int count = packages.size();
                    int p = 0;
                    while (true) {
                        ParceledListSlice<PackageInfo> slice2 = slice;
                        int p2 = p;
                        if (p2 < count) {
                            int count2 = count;
                            PackageInfo info = packages.get(p2);
                            if (filter2 != null) {
                                userId = userId3;
                            } else {
                                userId = userId3;
                                if (uid == -1 || info.applicationInfo.uid == uid) {
                                    boolean isSystem = (info.applicationInfo.flags & 1) != 0;
                                    if (listDisabled) {
                                        filter = filter2;
                                        if (info.applicationInfo.enabled) {
                                            p = p2 + 1;
                                            slice = slice2;
                                            count = count2;
                                            userId3 = userId;
                                            filter2 = filter;
                                            packageManagerShellCommand = this;
                                        }
                                    } else {
                                        filter = filter2;
                                    }
                                    if ((!listEnabled || info.applicationInfo.enabled) && ((!listSystem || isSystem) && (!listThirdParty || !isSystem))) {
                                        pw.print("package:");
                                        if (showSourceDir2) {
                                            pw.print(info.applicationInfo.sourceDir);
                                            pw.print("=");
                                        }
                                        pw.print(info.packageName);
                                        if (showVersionCode) {
                                            pw.print(" versionCode:");
                                            pw.print(info.applicationInfo.versionCode);
                                        }
                                        if (listInstaller) {
                                            pw.print("  installer=");
                                            pw.print(packageManagerShellCommand.mInterface.getInstallerPackageName(info.packageName));
                                        }
                                        if (showUid) {
                                            pw.print(" uid:");
                                            pw.print(info.applicationInfo.uid);
                                        }
                                        pw.println();
                                    }
                                    p = p2 + 1;
                                    slice = slice2;
                                    count = count2;
                                    userId3 = userId;
                                    filter2 = filter;
                                    packageManagerShellCommand = this;
                                }
                            }
                            filter = filter2;
                            p = p2 + 1;
                            slice = slice2;
                            count = count2;
                            userId3 = userId;
                            filter2 = filter;
                            packageManagerShellCommand = this;
                        } else {
                            int i4 = count;
                            int i5 = userId3;
                            return 0;
                        }
                    }
                }
            } catch (RuntimeException e2) {
                ex = e2;
                int i6 = getFlags;
                int i7 = userId3;
                pw.println("Error: " + ex.toString());
                return -1;
            }
        }
    }

    private int runListPermissionGroups() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        List<PermissionGroupInfo> pgs = this.mInterface.getAllPermissionGroups(0).getList();
        int count = pgs.size();
        for (int p = 0; p < count; p++) {
            pw.print("permission group:");
            pw.println(pgs.get(p).name);
        }
        return 0;
    }

    private int runListPermissions() throws RemoteException {
        PackageManagerShellCommand packageManagerShellCommand;
        boolean labels;
        PrintWriter pw = getOutPrintWriter();
        boolean groups = false;
        boolean userOnly = false;
        boolean summary = false;
        boolean labels2 = false;
        boolean dangerousOnly = false;
        while (true) {
            String nextOption = getNextOption();
            String opt = nextOption;
            if (nextOption != null) {
                char c = 65535;
                int hashCode = opt.hashCode();
                if (hashCode != 1495) {
                    if (hashCode != 1510) {
                        if (hashCode != 1512) {
                            switch (hashCode) {
                                case 1497:
                                    if (opt.equals("-f")) {
                                        c = 1;
                                        break;
                                    }
                                    break;
                                case 1498:
                                    if (opt.equals("-g")) {
                                        c = 2;
                                        break;
                                    }
                                    break;
                            }
                        } else if (opt.equals("-u")) {
                            c = 4;
                        }
                    } else if (opt.equals("-s")) {
                        c = 3;
                    }
                } else if (opt.equals("-d")) {
                    c = 0;
                }
                switch (c) {
                    case 0:
                        dangerousOnly = true;
                        continue;
                    case 1:
                        labels = true;
                        break;
                    case 2:
                        groups = true;
                        continue;
                    case 3:
                        groups = true;
                        labels = true;
                        summary = true;
                        break;
                    case 4:
                        userOnly = true;
                        continue;
                    default:
                        pw.println("Error: Unknown option: " + opt);
                        return 1;
                }
                labels2 = labels;
            } else {
                ArrayList<String> groupList = new ArrayList<>();
                if (groups) {
                    packageManagerShellCommand = this;
                    List<PermissionGroupInfo> infos = packageManagerShellCommand.mInterface.getAllPermissionGroups(0).getList();
                    int count = infos.size();
                    for (int i = 0; i < count; i++) {
                        groupList.add(infos.get(i).name);
                    }
                    groupList.add(null);
                } else {
                    packageManagerShellCommand = this;
                    groupList.add(getNextArg());
                }
                if (dangerousOnly) {
                    pw.println("Dangerous Permissions:");
                    pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                    packageManagerShellCommand.doListPermissions(groupList, groups, labels2, summary, 1, 1);
                    if (userOnly) {
                        pw.println("Normal Permissions:");
                        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                        doListPermissions(groupList, groups, labels2, summary, 0, 0);
                    }
                } else if (userOnly) {
                    pw.println("Dangerous and Normal Permissions:");
                    pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                    doListPermissions(groupList, groups, labels2, summary, 0, 1);
                } else {
                    pw.println("All Permissions:");
                    pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                    doListPermissions(groupList, groups, labels2, summary, -10000, 10000);
                }
                return 0;
            }
        }
    }

    private Intent parseIntentAndUser() throws URISyntaxException {
        this.mTargetUser = -2;
        this.mBrief = false;
        this.mComponents = false;
        Intent intent = Intent.parseCommandArgs(this, new Intent.CommandOptionHandler() {
            public boolean handleOption(String opt, ShellCommand cmd) {
                if ("--user".equals(opt)) {
                    PackageManagerShellCommand.this.mTargetUser = UserHandle.parseUserArg(cmd.getNextArgRequired());
                    return true;
                } else if ("--brief".equals(opt)) {
                    PackageManagerShellCommand.this.mBrief = true;
                    return true;
                } else if (!"--components".equals(opt)) {
                    return false;
                } else {
                    PackageManagerShellCommand.this.mComponents = true;
                    return true;
                }
            }
        });
        this.mTargetUser = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), this.mTargetUser, false, false, null, null);
        return intent;
    }

    private void printResolveInfo(PrintWriterPrinter pr, String prefix, ResolveInfo ri, boolean brief, boolean components) {
        ComponentName comp;
        if (brief || components) {
            if (ri.activityInfo != null) {
                comp = new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
            } else if (ri.serviceInfo != null) {
                comp = new ComponentName(ri.serviceInfo.packageName, ri.serviceInfo.name);
            } else if (ri.providerInfo != null) {
                comp = new ComponentName(ri.providerInfo.packageName, ri.providerInfo.name);
            } else {
                comp = null;
            }
            if (comp != null) {
                if (!components) {
                    pr.println(prefix + "priority=" + ri.priority + " preferredOrder=" + ri.preferredOrder + " match=0x" + Integer.toHexString(ri.match) + " specificIndex=" + ri.specificIndex + " isDefault=" + ri.isDefault);
                }
                pr.println(prefix + comp.flattenToShortString());
                return;
            }
        }
        ri.dump(pr, prefix);
    }

    private int runResolveActivity() {
        try {
            Intent intent = parseIntentAndUser();
            try {
                ResolveInfo ri = this.mInterface.resolveIntent(intent, intent.getType(), 0, this.mTargetUser);
                PrintWriter pw = getOutPrintWriter();
                if (ri == null) {
                    pw.println("No activity found");
                } else {
                    printResolveInfo(new PrintWriterPrinter(pw), BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, ri, this.mBrief, this.mComponents);
                }
                return 0;
            } catch (RemoteException e) {
                throw new RuntimeException("Failed calling service", e);
            }
        } catch (URISyntaxException e2) {
            throw new RuntimeException(e2.getMessage(), e2);
        }
    }

    private int runQueryIntentActivities() {
        try {
            Intent intent = parseIntentAndUser();
            try {
                List<ResolveInfo> result = this.mInterface.queryIntentActivities(intent, intent.getType(), 0, this.mTargetUser).getList();
                PrintWriter pw = getOutPrintWriter();
                if (result != null) {
                    if (result.size() > 0) {
                        if (!this.mComponents) {
                            pw.print(result.size());
                            pw.println(" activities found:");
                            PrintWriterPrinter pr = new PrintWriterPrinter(pw);
                            for (int i = 0; i < result.size(); i++) {
                                pw.print("  Activity #");
                                pw.print(i);
                                pw.println(":");
                                printResolveInfo(pr, "    ", result.get(i), this.mBrief, this.mComponents);
                            }
                        } else {
                            PrintWriterPrinter pr2 = new PrintWriterPrinter(pw);
                            for (int i2 = 0; i2 < result.size(); i2++) {
                                printResolveInfo(pr2, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, result.get(i2), this.mBrief, this.mComponents);
                            }
                        }
                        return 0;
                    }
                }
                pw.println("No activities found");
                return 0;
            } catch (RemoteException e) {
                throw new RuntimeException("Failed calling service", e);
            }
        } catch (URISyntaxException e2) {
            throw new RuntimeException(e2.getMessage(), e2);
        }
    }

    private int runQueryIntentServices() {
        try {
            Intent intent = parseIntentAndUser();
            try {
                List<ResolveInfo> result = this.mInterface.queryIntentServices(intent, intent.getType(), 0, this.mTargetUser).getList();
                PrintWriter pw = getOutPrintWriter();
                if (result != null) {
                    if (result.size() > 0) {
                        if (!this.mComponents) {
                            pw.print(result.size());
                            pw.println(" services found:");
                            PrintWriterPrinter pr = new PrintWriterPrinter(pw);
                            for (int i = 0; i < result.size(); i++) {
                                pw.print("  Service #");
                                pw.print(i);
                                pw.println(":");
                                printResolveInfo(pr, "    ", result.get(i), this.mBrief, this.mComponents);
                            }
                        } else {
                            PrintWriterPrinter pr2 = new PrintWriterPrinter(pw);
                            for (int i2 = 0; i2 < result.size(); i2++) {
                                printResolveInfo(pr2, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, result.get(i2), this.mBrief, this.mComponents);
                            }
                        }
                        return 0;
                    }
                }
                pw.println("No services found");
                return 0;
            } catch (RemoteException e) {
                throw new RuntimeException("Failed calling service", e);
            }
        } catch (URISyntaxException e2) {
            throw new RuntimeException(e2.getMessage(), e2);
        }
    }

    private int runQueryIntentReceivers() {
        try {
            Intent intent = parseIntentAndUser();
            try {
                List<ResolveInfo> result = this.mInterface.queryIntentReceivers(intent, intent.getType(), 0, this.mTargetUser).getList();
                PrintWriter pw = getOutPrintWriter();
                if (result != null) {
                    if (result.size() > 0) {
                        if (!this.mComponents) {
                            pw.print(result.size());
                            pw.println(" receivers found:");
                            PrintWriterPrinter pr = new PrintWriterPrinter(pw);
                            for (int i = 0; i < result.size(); i++) {
                                pw.print("  Receiver #");
                                pw.print(i);
                                pw.println(":");
                                printResolveInfo(pr, "    ", result.get(i), this.mBrief, this.mComponents);
                            }
                        } else {
                            PrintWriterPrinter pr2 = new PrintWriterPrinter(pw);
                            for (int i2 = 0; i2 < result.size(); i2++) {
                                printResolveInfo(pr2, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, result.get(i2), this.mBrief, this.mComponents);
                            }
                        }
                        return 0;
                    }
                }
                pw.println("No receivers found");
                return 0;
            } catch (RemoteException e) {
                throw new RuntimeException("Failed calling service", e);
            }
        } catch (URISyntaxException e2) {
            throw new RuntimeException(e2.getMessage(), e2);
        }
    }

    private int runInstall() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        InstallParams params = makeInstallParams();
        String inPath = getNextArg();
        setParamsSize(params, inPath);
        int sessionId = doCreateSession(params.sessionParams, params.installerPackageName, params.userId);
        boolean abandonSession = true;
        if (inPath == null) {
            try {
                if (params.sessionParams.sizeBytes == -1) {
                    pw.println("Error: must either specify a package size or an APK file");
                    return 1;
                }
            } finally {
                if (abandonSession) {
                    try {
                        doAbandonSession(sessionId, false);
                    } catch (Exception e) {
                    }
                }
            }
        }
        if (HwDeviceManager.disallowOp(6)) {
            System.err.println("Failure [MDM_FORBID_ADB_INSTALL]");
            if (abandonSession) {
                try {
                    doAbandonSession(sessionId, false);
                } catch (Exception e2) {
                }
            }
            return 1;
        }
        if (doWriteSplit(sessionId, inPath, params.sessionParams.sizeBytes, "base.apk", false) != 0) {
            if (abandonSession) {
                try {
                    doAbandonSession(sessionId, false);
                } catch (Exception e3) {
                }
            }
            return 1;
        } else if (doCommitSession(sessionId, false) != 0) {
            if (abandonSession) {
                try {
                    doAbandonSession(sessionId, false);
                } catch (Exception e4) {
                }
            }
            return 1;
        } else {
            abandonSession = false;
            pw.println("Success");
            if (abandonSession) {
                try {
                    doAbandonSession(sessionId, false);
                } catch (Exception e5) {
                }
            }
            return 0;
        }
    }

    private int runInstallAbandon() throws RemoteException {
        return doAbandonSession(Integer.parseInt(getNextArg()), true);
    }

    private int runInstallCommit() throws RemoteException {
        return doCommitSession(Integer.parseInt(getNextArg()), true);
    }

    private int runInstallCreate() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        InstallParams installParams = makeInstallParams();
        int sessionId = doCreateSession(installParams.sessionParams, installParams.installerPackageName, installParams.userId);
        pw.println("Success: created install session [" + sessionId + "]");
        return 0;
    }

    private int runInstallWrite() throws RemoteException {
        long sizeBytes = -1;
        while (true) {
            String nextOption = getNextOption();
            String opt = nextOption;
            if (nextOption == null) {
                return doWriteSplit(Integer.parseInt(getNextArg()), getNextArg(), sizeBytes, getNextArg(), true);
            } else if (opt.equals("-S")) {
                sizeBytes = Long.parseLong(getNextArg());
            } else {
                throw new IllegalArgumentException("Unknown option: " + opt);
            }
        }
    }

    private int runInstallRemove() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        int sessionId = Integer.parseInt(getNextArg());
        String splitName = getNextArg();
        if (splitName != null) {
            return doRemoveSplit(sessionId, splitName, true);
        }
        pw.println("Error: split name not specified");
        return 1;
    }

    private int runInstallExisting() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        int userId = 0;
        int installFlags = 0;
        while (true) {
            String nextOption = getNextOption();
            String opt = nextOption;
            if (nextOption != null) {
                char c = 65535;
                int hashCode = opt.hashCode();
                if (hashCode != -951415743) {
                    if (hashCode != 1051781117) {
                        if (hashCode != 1333024815) {
                            if (hashCode == 1333469547 && opt.equals("--user")) {
                                c = 0;
                            }
                        } else if (opt.equals("--full")) {
                            c = 3;
                        }
                    } else if (opt.equals("--ephemeral")) {
                        c = 1;
                    }
                } else if (opt.equals("--instant")) {
                    c = 2;
                }
                switch (c) {
                    case 0:
                        userId = UserHandle.parseUserArg(getNextArgRequired());
                        break;
                    case 1:
                    case 2:
                        installFlags = (installFlags | 2048) & -16385;
                        break;
                    case 3:
                        installFlags = (installFlags & -2049) | 16384;
                        break;
                    default:
                        pw.println("Error: Unknown option: " + opt);
                        return 1;
                }
            } else {
                String packageName = getNextArg();
                if (packageName == null) {
                    pw.println("Error: package name not specified");
                    return 1;
                }
                try {
                    if (this.mInterface.installExistingPackageAsUser(packageName, userId, installFlags, 0) != -3) {
                        pw.println("Package " + packageName + " installed for user: " + userId);
                        return 0;
                    }
                    throw new PackageManager.NameNotFoundException("Package " + packageName + " doesn't exist");
                } catch (PackageManager.NameNotFoundException | RemoteException e) {
                    pw.println(e.toString());
                    return 1;
                }
            }
        }
    }

    private int runSetInstallLocation() throws RemoteException {
        String arg = getNextArg();
        if (arg == null) {
            getErrPrintWriter().println("Error: no install location specified.");
            return 1;
        }
        try {
            if (this.mInterface.setInstallLocation(Integer.parseInt(arg))) {
                return 0;
            }
            getErrPrintWriter().println("Error: install location has to be a number.");
            return 1;
        } catch (NumberFormatException e) {
            getErrPrintWriter().println("Error: install location has to be a number.");
            return 1;
        }
    }

    private int runGetInstallLocation() throws RemoteException {
        int loc = this.mInterface.getInstallLocation();
        String locStr = "invalid";
        if (loc == 0) {
            locStr = UiModeManagerService.Shell.NIGHT_MODE_STR_AUTO;
        } else if (loc == 1) {
            locStr = "internal";
        } else if (loc == 2) {
            locStr = "external";
        }
        PrintWriter outPrintWriter = getOutPrintWriter();
        outPrintWriter.println(loc + "[" + locStr + "]");
        return 0;
    }

    public int runMovePackage() throws RemoteException {
        String packageName = getNextArg();
        if (packageName == null) {
            getErrPrintWriter().println("Error: package name not specified");
            return 1;
        }
        String volumeUuid = getNextArg();
        if ("internal".equals(volumeUuid)) {
            volumeUuid = null;
        }
        int moveId = this.mInterface.movePackage(packageName, volumeUuid);
        int status = this.mInterface.getMoveStatus(moveId);
        while (!PackageManager.isMoveStatusFinished(status)) {
            SystemClock.sleep(1000);
            status = this.mInterface.getMoveStatus(moveId);
        }
        if (status == -100) {
            getOutPrintWriter().println("Success");
            return 0;
        }
        PrintWriter errPrintWriter = getErrPrintWriter();
        errPrintWriter.println("Failure [" + status + "]");
        return 1;
    }

    public int runMovePrimaryStorage() throws RemoteException {
        String volumeUuid = getNextArg();
        if ("internal".equals(volumeUuid)) {
            volumeUuid = null;
        }
        int moveId = this.mInterface.movePrimaryStorage(volumeUuid);
        int status = this.mInterface.getMoveStatus(moveId);
        while (!PackageManager.isMoveStatusFinished(status)) {
            SystemClock.sleep(1000);
            status = this.mInterface.getMoveStatus(moveId);
        }
        if (status == -100) {
            getOutPrintWriter().println("Success");
            return 0;
        }
        PrintWriter errPrintWriter = getErrPrintWriter();
        errPrintWriter.println("Failure [" + status + "]");
        return 1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:147:0x00aa A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00bf  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00c6  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00ca  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00d2  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00d9  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00e0  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00e7  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x00eb  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00ef  */
    private int runCompile() throws RemoteException {
        String targetCompilerFilter;
        boolean z;
        List<String> packageNames;
        int index;
        boolean allPackages;
        List<String> packageNames2;
        String opt;
        boolean result;
        boolean z2;
        List<String> failedPackages;
        char c;
        boolean secondaryDex;
        PackageManagerShellCommand packageManagerShellCommand = this;
        PrintWriter pw = getOutPrintWriter();
        boolean checkProfiles = SystemProperties.getBoolean("dalvik.vm.usejitprofiles", false);
        boolean forceCompilation = false;
        boolean allPackages2 = false;
        String compilationReason = null;
        String compilerFilter = null;
        String compilationReason2 = null;
        String checkProfilesRaw = null;
        boolean secondaryDex2 = false;
        String split = null;
        while (true) {
            String nextOption = getNextOption();
            String opt2 = nextOption;
            if (nextOption != null) {
                int hashCode = opt2.hashCode();
                if (hashCode != -1615291473) {
                    if (hashCode != -1614046854) {
                        if (hashCode != 1492) {
                            if (hashCode != 1494) {
                                if (hashCode != 1497) {
                                    if (hashCode != 1504) {
                                        if (hashCode != 1509) {
                                            if (hashCode != 1269477022) {
                                                if (hashCode == 1690714782 && opt2.equals("--check-prof")) {
                                                    c = 5;
                                                    switch (c) {
                                                        case 0:
                                                            allPackages2 = true;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                        case 1:
                                                            compilationReason = 1;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                        case 2:
                                                            secondaryDex = true;
                                                            break;
                                                        case 3:
                                                            compilerFilter = getNextArgRequired();
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                        case 4:
                                                            compilationReason2 = getNextArgRequired();
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                        case 5:
                                                            checkProfilesRaw = getNextArgRequired();
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                        case 6:
                                                            secondaryDex = true;
                                                            compilationReason2 = "install";
                                                            compilationReason = 1;
                                                            break;
                                                        case 7:
                                                            secondaryDex2 = true;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                        case 8:
                                                            split = getNextArgRequired();
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                            continue;
                                                        default:
                                                            pw.println("Error: Unknown option: " + opt2);
                                                            return 1;
                                                    }
                                                    forceCompilation = secondaryDex;
                                                }
                                            } else if (opt2.equals("--secondary-dex")) {
                                                c = 7;
                                                switch (c) {
                                                    case 0:
                                                        break;
                                                    case 1:
                                                        break;
                                                    case 2:
                                                        break;
                                                    case 3:
                                                        break;
                                                    case 4:
                                                        break;
                                                    case 5:
                                                        break;
                                                    case 6:
                                                        break;
                                                    case 7:
                                                        break;
                                                    case 8:
                                                        break;
                                                }
                                                forceCompilation = secondaryDex;
                                            }
                                        } else if (opt2.equals("-r")) {
                                            c = 4;
                                            switch (c) {
                                                case 0:
                                                    break;
                                                case 1:
                                                    break;
                                                case 2:
                                                    break;
                                                case 3:
                                                    break;
                                                case 4:
                                                    break;
                                                case 5:
                                                    break;
                                                case 6:
                                                    break;
                                                case 7:
                                                    break;
                                                case 8:
                                                    break;
                                            }
                                            forceCompilation = secondaryDex;
                                        }
                                    } else if (opt2.equals("-m")) {
                                        c = 3;
                                        switch (c) {
                                            case 0:
                                                break;
                                            case 1:
                                                break;
                                            case 2:
                                                break;
                                            case 3:
                                                break;
                                            case 4:
                                                break;
                                            case 5:
                                                break;
                                            case 6:
                                                break;
                                            case 7:
                                                break;
                                            case 8:
                                                break;
                                        }
                                        forceCompilation = secondaryDex;
                                    }
                                } else if (opt2.equals("-f")) {
                                    c = 2;
                                    switch (c) {
                                        case 0:
                                            break;
                                        case 1:
                                            break;
                                        case 2:
                                            break;
                                        case 3:
                                            break;
                                        case 4:
                                            break;
                                        case 5:
                                            break;
                                        case 6:
                                            break;
                                        case 7:
                                            break;
                                        case 8:
                                            break;
                                    }
                                    forceCompilation = secondaryDex;
                                }
                            } else if (opt2.equals("-c")) {
                                c = 1;
                                switch (c) {
                                    case 0:
                                        break;
                                    case 1:
                                        break;
                                    case 2:
                                        break;
                                    case 3:
                                        break;
                                    case 4:
                                        break;
                                    case 5:
                                        break;
                                    case 6:
                                        break;
                                    case 7:
                                        break;
                                    case 8:
                                        break;
                                }
                                forceCompilation = secondaryDex;
                            }
                        } else if (opt2.equals("-a")) {
                            c = 0;
                            switch (c) {
                                case 0:
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    break;
                                case 4:
                                    break;
                                case 5:
                                    break;
                                case 6:
                                    break;
                                case 7:
                                    break;
                                case 8:
                                    break;
                            }
                            forceCompilation = secondaryDex;
                        }
                    } else if (opt2.equals("--split")) {
                        c = 8;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                            case 4:
                                break;
                            case 5:
                                break;
                            case 6:
                                break;
                            case 7:
                                break;
                            case 8:
                                break;
                        }
                        forceCompilation = secondaryDex;
                    }
                } else if (opt2.equals("--reset")) {
                    c = 6;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                        case 4:
                            break;
                        case 5:
                            break;
                        case 6:
                            break;
                        case 7:
                            break;
                        case 8:
                            break;
                    }
                    forceCompilation = secondaryDex;
                }
                c = 65535;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6:
                        break;
                    case 7:
                        break;
                    case 8:
                        break;
                }
                forceCompilation = secondaryDex;
            } else {
                if (checkProfilesRaw != null) {
                    if ("true".equals(checkProfilesRaw)) {
                        checkProfiles = true;
                    } else if ("false".equals(checkProfilesRaw)) {
                        checkProfiles = false;
                    } else {
                        pw.println("Invalid value for \"--check-prof\". Expected \"true\" or \"false\".");
                        return 1;
                    }
                }
                if (compilerFilter != null && compilationReason2 != null) {
                    pw.println("Cannot use compilation filter (\"-m\") and compilation reason (\"-r\") at the same time");
                    return 1;
                } else if (compilerFilter == null && compilationReason2 == null) {
                    pw.println("Cannot run without any of compilation filter (\"-m\") and compilation reason (\"-r\") at the same time");
                    return 1;
                } else if (allPackages2 && split != null) {
                    pw.println("-a cannot be specified together with --split");
                    return 1;
                } else if (!secondaryDex2 || split == null) {
                    if (compilerFilter == null) {
                        int reason = -1;
                        int i = 0;
                        while (true) {
                            if (i < PackageManagerServiceCompilerMapping.REASON_STRINGS.length) {
                                if (PackageManagerServiceCompilerMapping.REASON_STRINGS[i].equals(compilationReason2)) {
                                    reason = i;
                                } else {
                                    i++;
                                }
                            }
                        }
                        if (reason == -1) {
                            pw.println("Error: Unknown compilation reason: " + compilationReason2);
                            return 1;
                        }
                        targetCompilerFilter = PackageManagerServiceCompilerMapping.getCompilerFilterForReason(reason);
                    } else if (!DexFile.isValidCompilerFilter(compilerFilter)) {
                        pw.println("Error: \"" + compilerFilter + "\" is not a valid compilation filter.");
                        return 1;
                    } else {
                        targetCompilerFilter = compilerFilter;
                    }
                    if (allPackages2) {
                        packageNames = packageManagerShellCommand.mInterface.getAllPackages();
                        z = true;
                    } else {
                        String packageName = getNextArg();
                        if (packageName == null) {
                            pw.println("Error: package name not specified");
                            return 1;
                        }
                        z = true;
                        packageNames = Collections.singletonList(packageName);
                    }
                    List<String> failedPackages2 = new ArrayList<>();
                    int index2 = 0;
                    String str = compilerFilter;
                    Iterator<String> it = packageNames.iterator();
                    while (it.hasNext()) {
                        Iterator<String> it2 = it;
                        String packageName2 = it.next();
                        if (compilationReason != null) {
                            packageManagerShellCommand.mInterface.clearApplicationProfileData(packageName2);
                        }
                        if (allPackages2) {
                            StringBuilder sb = new StringBuilder();
                            allPackages = allPackages2;
                            int index3 = index2 + 1;
                            sb.append(index3);
                            index = index3;
                            sb.append(SliceClientPermissions.SliceAuthority.DELIMITER);
                            sb.append(packageNames.size());
                            sb.append(": ");
                            sb.append(packageName2);
                            pw.println(sb.toString());
                            pw.flush();
                        } else {
                            allPackages = allPackages2;
                            index = index2;
                        }
                        if (secondaryDex2) {
                            failedPackages = failedPackages2;
                            packageNames2 = packageNames;
                            opt = opt2;
                            result = packageManagerShellCommand.mInterface.performDexOptSecondary(packageName2, targetCompilerFilter, forceCompilation);
                            z2 = true;
                        } else {
                            List<String> failedPackages3 = failedPackages2;
                            IPackageManager iPackageManager = packageManagerShellCommand.mInterface;
                            packageNames2 = packageNames;
                            failedPackages = failedPackages3;
                            z2 = true;
                            opt = opt2;
                            result = iPackageManager.performDexOptMode(packageName2, checkProfiles, targetCompilerFilter, forceCompilation, true, split);
                        }
                        if (!result) {
                            failedPackages.add(packageName2);
                        }
                        failedPackages2 = failedPackages;
                        z = z2;
                        opt2 = opt;
                        it = it2;
                        allPackages2 = allPackages;
                        index2 = index;
                        packageNames = packageNames2;
                        packageManagerShellCommand = this;
                    }
                    List<String> failedPackages4 = failedPackages2;
                    List<String> list = packageNames;
                    boolean allPackages3 = z;
                    String str2 = opt2;
                    if (failedPackages4.isEmpty()) {
                        pw.println("Success");
                        return 0;
                    } else if (failedPackages4.size() == allPackages3) {
                        pw.println("Failure: package " + failedPackages4.get(0) + " could not be compiled");
                        return allPackages3;
                    } else {
                        pw.print("Failure: the following packages could not be compiled: ");
                        boolean is_first = true;
                        for (String packageName3 : failedPackages4) {
                            if (is_first) {
                                is_first = false;
                            } else {
                                pw.print(", ");
                            }
                            pw.print(packageName3);
                        }
                        pw.println();
                        return allPackages3;
                    }
                } else {
                    pw.println("--secondary-dex cannot be specified together with --split");
                    return 1;
                }
            }
        }
    }

    private int runreconcileSecondaryDexFiles() throws RemoteException {
        this.mInterface.reconcileSecondaryDexFiles(getNextArg());
        return 0;
    }

    public int runForceDexOpt() throws RemoteException {
        this.mInterface.forceDexOpt(getNextArgRequired());
        return 0;
    }

    private int runDexoptJob() throws RemoteException {
        List<String> packageNames = new ArrayList<>();
        while (true) {
            String nextArg = getNextArg();
            String arg = nextArg;
            if (nextArg == null) {
                break;
            }
            packageNames.add(arg);
        }
        return this.mInterface.runBackgroundDexoptJob(packageNames.isEmpty() ? null : packageNames) ? 0 : -1;
    }

    private int runDumpProfiles() throws RemoteException {
        this.mInterface.dumpProfiles(getNextArg());
        return 0;
    }

    private int runSnapshotProfile() throws RemoteException {
        InputStream inStream;
        String str;
        OutputStream outStream;
        Throwable th;
        Throwable th2;
        PrintWriter pw = getOutPrintWriter();
        String packageName = getNextArg();
        boolean isBootImage = PackageManagerService.PLATFORM_PACKAGE_NAME.equals(packageName);
        String codePath = null;
        while (true) {
            String nextArg = getNextArg();
            String opt = nextArg;
            boolean z = false;
            if (nextArg != null) {
                if (opt.hashCode() != -684928411 || !opt.equals("--code-path")) {
                    z = true;
                }
                if (z) {
                    pw.write("Unknown arg: " + opt);
                    return -1;
                } else if (isBootImage) {
                    pw.write("--code-path cannot be used for the boot image.");
                    return -1;
                } else {
                    codePath = getNextArg();
                }
            } else {
                String baseCodePath = null;
                if (!isBootImage) {
                    PackageInfo packageInfo = this.mInterface.getPackageInfo(packageName, 0, 0);
                    if (packageInfo == null) {
                        pw.write("Package not found " + packageName);
                        return -1;
                    }
                    baseCodePath = packageInfo.applicationInfo.getBaseCodePath();
                    if (codePath == null) {
                        codePath = baseCodePath;
                    }
                }
                String codePath2 = codePath;
                String baseCodePath2 = baseCodePath;
                SnapshotRuntimeProfileCallback callback = new SnapshotRuntimeProfileCallback();
                String callingPackage = Binder.getCallingUid() == 0 ? "root" : "com.android.shell";
                int profileType = isBootImage ? 1 : 0;
                if (!this.mInterface.getArtManager().isRuntimeProfilingEnabled(profileType, callingPackage)) {
                    pw.println("Error: Runtime profiling is not enabled");
                    return -1;
                }
                int i = profileType;
                String baseCodePath3 = baseCodePath2;
                this.mInterface.getArtManager().snapshotRuntimeProfile(profileType, packageName, codePath2, callback, callingPackage);
                SnapshotRuntimeProfileCallback callback2 = callback;
                if (!callback2.waitTillDone()) {
                    pw.println("Error: callback not called");
                    return callback2.mErrCode;
                }
                try {
                    inStream = new ParcelFileDescriptor.AutoCloseInputStream(callback2.mProfileReadFd);
                    if (!isBootImage) {
                        if (!Objects.equals(baseCodePath3, codePath2)) {
                            str = STDIN_PATH + new File(codePath2).getName();
                            String outputFileSuffix = str;
                            String outputProfilePath = ART_PROFILE_SNAPSHOT_DEBUG_LOCATION + packageName + outputFileSuffix + ".prof";
                            outStream = new FileOutputStream(outputProfilePath);
                            Streams.copy(inStream, outStream);
                            $closeResource(null, outStream);
                            Os.chmod(outputProfilePath, 420);
                            $closeResource(null, inStream);
                            return 0;
                        }
                    }
                    str = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                    String outputFileSuffix2 = str;
                    String outputProfilePath2 = ART_PROFILE_SNAPSHOT_DEBUG_LOCATION + packageName + outputFileSuffix2 + ".prof";
                    outStream = new FileOutputStream(outputProfilePath2);
                    try {
                        Streams.copy(inStream, outStream);
                        $closeResource(null, outStream);
                        Os.chmod(outputProfilePath2, 420);
                        $closeResource(null, inStream);
                        return 0;
                    } catch (Throwable th3) {
                        th = th3;
                    }
                } catch (ErrnoException | IOException e) {
                    pw.println("Error when reading the profile fd: " + e.getMessage());
                    e.printStackTrace(pw);
                    return -1;
                } catch (Throwable th4) {
                    th2 = th4;
                    throw th2;
                }
            }
        }
        $closeResource(th, outStream);
        throw th;
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    private int runUninstall() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        int flags = 0;
        int userId = -1;
        long versionCode = -1;
        while (true) {
            String nextOption = getNextOption();
            String opt = nextOption;
            char c = 65535;
            if (nextOption != null) {
                int hashCode = opt.hashCode();
                if (hashCode != 1502) {
                    if (hashCode != 1333469547) {
                        if (hashCode == 1884113221 && opt.equals("--versionCode")) {
                            c = 2;
                        }
                    } else if (opt.equals("--user")) {
                        c = 1;
                    }
                } else if (opt.equals("-k")) {
                    c = 0;
                }
                switch (c) {
                    case 0:
                        flags |= 1;
                        break;
                    case 1:
                        userId = UserHandle.parseUserArg(getNextArgRequired());
                        break;
                    case 2:
                        versionCode = Long.parseLong(getNextArgRequired());
                        break;
                    default:
                        pw.println("Error: Unknown option: " + opt);
                        return 1;
                }
            } else {
                String packageName = getNextArg();
                if (packageName == null) {
                    pw.println("Error: package name not specified");
                    return 1;
                }
                String splitName = getNextArg();
                if (splitName != null) {
                    return runRemoveSplit(packageName, splitName);
                }
                int userId2 = translateUserId(userId, true, "runUninstall");
                if (userId2 == -1) {
                    userId2 = 0;
                    flags |= 2;
                } else {
                    PackageInfo info = this.mInterface.getPackageInfo(packageName, 67108864, userId2);
                    if (info == null) {
                        pw.println("Failure [not installed for " + userId2 + "]");
                        return 1;
                    }
                    if ((info.applicationInfo.flags & 1) != 0) {
                        flags |= 4;
                    }
                }
                LocalIntentReceiver receiver = new LocalIntentReceiver();
                this.mInterface.getPackageInstaller().uninstall(new VersionedPackage(packageName, versionCode), null, flags, receiver.getIntentSender(), userId2);
                if (receiver.getResult().getIntExtra("android.content.pm.extra.STATUS", 1) == 0) {
                    pw.println("Success");
                    return 0;
                }
                pw.println("Failure [" + result.getStringExtra("android.content.pm.extra.STATUS_MESSAGE") + "]");
                return 1;
            }
        }
    }

    private int runRemoveSplit(String packageName, String splitName) throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(2);
        sessionParams.installFlags = 2 | sessionParams.installFlags;
        sessionParams.appPackageName = packageName;
        int sessionId = doCreateSession(sessionParams, null, -1);
        boolean abandonSession = true;
        try {
            if (doRemoveSplit(sessionId, splitName, false) != 0) {
                if (abandonSession) {
                    try {
                        doAbandonSession(sessionId, false);
                    } catch (Exception e) {
                    }
                }
                return 1;
            } else if (doCommitSession(sessionId, false) != 0) {
                if (abandonSession) {
                    try {
                        doAbandonSession(sessionId, false);
                    } catch (Exception e2) {
                    }
                }
                return 1;
            } else {
                abandonSession = false;
                pw.println("Success");
                return 0;
            }
        } finally {
            if (abandonSession) {
                try {
                    doAbandonSession(sessionId, false);
                } catch (Exception e3) {
                }
            }
        }
    }

    private int runClear() throws RemoteException {
        int userId = 0;
        String option = getNextOption();
        if (option != null && option.equals("--user")) {
            userId = UserHandle.parseUserArg(getNextArgRequired());
        }
        String pkg = getNextArg();
        if (pkg == null) {
            getErrPrintWriter().println("Error: no package specified");
            return 1;
        }
        ClearDataObserver obs = new ClearDataObserver();
        ActivityManager.getService().clearApplicationUserData(pkg, false, obs, userId);
        synchronized (obs) {
            while (!obs.finished) {
                try {
                    obs.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        if (obs.result) {
            getOutPrintWriter().println("Success");
            return 0;
        }
        getErrPrintWriter().println("Failed");
        return 1;
    }

    private static String enabledSettingToString(int state) {
        switch (state) {
            case 0:
                return BatteryService.HealthServiceWrapper.INSTANCE_VENDOR;
            case 1:
                return "enabled";
            case 2:
                return "disabled";
            case 3:
                return "disabled-user";
            case 4:
                return "disabled-until-used";
            default:
                return UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
        }
    }

    private int runSetEnabledSetting(int state) throws RemoteException {
        String pkgName;
        int userId = 0;
        String option = getNextOption();
        if (option != null && option.equals("--user")) {
            userId = UserHandle.parseUserArg(getNextArgRequired());
        }
        String pkg = getNextArg();
        if (pkg == null) {
            getErrPrintWriter().println("Error: no package or component specified");
            return 1;
        }
        ComponentName cn = ComponentName.unflattenFromString(pkg);
        if (HwPackageManagerServiceUtils.isDisableStatus(state)) {
            if (cn == null) {
                pkgName = pkg;
            } else {
                pkgName = cn.getPackageName();
            }
            if (HwPackageManagerServiceUtils.isInAntiFillingWhiteList(pkgName, this.mInterface.hasSystemFeature("android.software.home_screen", 0))) {
                getErrPrintWriter().println("Error: not allowed to disable this package");
                return 1;
            }
        }
        if (cn == null) {
            IPackageManager iPackageManager = this.mInterface;
            iPackageManager.setApplicationEnabledSetting(pkg, state, 0, userId, "shell:" + Process.myUid());
            PrintWriter outPrintWriter = getOutPrintWriter();
            outPrintWriter.println("Package " + pkg + " new state: " + enabledSettingToString(this.mInterface.getApplicationEnabledSetting(pkg, userId)));
            return 0;
        }
        this.mInterface.setComponentEnabledSetting(cn, state, 0, userId);
        PrintWriter outPrintWriter2 = getOutPrintWriter();
        outPrintWriter2.println("Component " + cn.toShortString() + " new state: " + enabledSettingToString(this.mInterface.getComponentEnabledSetting(cn, userId)));
        return 0;
    }

    private int runSetHiddenSetting(boolean state) throws RemoteException {
        int userId = 0;
        String option = getNextOption();
        if (option != null && option.equals("--user")) {
            userId = UserHandle.parseUserArg(getNextArgRequired());
        }
        String pkg = getNextArg();
        if (pkg == null) {
            getErrPrintWriter().println("Error: no package or component specified");
            return 1;
        }
        this.mInterface.setApplicationHiddenSettingAsUser(pkg, state, userId);
        PrintWriter outPrintWriter = getOutPrintWriter();
        outPrintWriter.println("Package " + pkg + " new hidden state: " + this.mInterface.getApplicationHiddenSettingAsUser(pkg, userId));
        return 0;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x002d, code lost:
        if (r13.equals("--user") != false) goto L_0x0077;
     */
    private int runSuspend(boolean suspendedState) {
        PrintWriter pw = getOutPrintWriter();
        String dialogMessage = null;
        PersistableBundle appExtras = new PersistableBundle();
        int userId = 0;
        PersistableBundle launcherExtras = new PersistableBundle();
        while (true) {
            String nextOption = getNextOption();
            String opt = nextOption;
            char c = 0;
            if (nextOption != null) {
                switch (opt.hashCode()) {
                    case -39471105:
                        if (opt.equals("--dialogMessage")) {
                            c = 1;
                            break;
                        }
                    case 42995488:
                        if (opt.equals("--aed")) {
                            c = 4;
                            break;
                        }
                    case 42995496:
                        if (opt.equals("--ael")) {
                            c = 2;
                            break;
                        }
                    case 42995503:
                        if (opt.equals("--aes")) {
                            c = 3;
                            break;
                        }
                    case 43006059:
                        if (opt.equals("--led")) {
                            c = 7;
                            break;
                        }
                    case 43006067:
                        if (opt.equals("--lel")) {
                            c = 5;
                            break;
                        }
                    case 43006074:
                        if (opt.equals("--les")) {
                            c = 6;
                            break;
                        }
                    case 1333469547:
                        break;
                    default:
                        c = 65535;
                        break;
                }
                switch (c) {
                    case 0:
                        userId = UserHandle.parseUserArg(getNextArgRequired());
                        break;
                    case 1:
                        dialogMessage = getNextArgRequired();
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        String key = getNextArgRequired();
                        String val = getNextArgRequired();
                        if (!suspendedState) {
                            break;
                        } else {
                            PersistableBundle bundleToInsert = opt.startsWith("--a") ? appExtras : launcherExtras;
                            char charAt = opt.charAt(4);
                            if (charAt != 'd') {
                                if (charAt != 'l') {
                                    if (charAt == 's') {
                                        bundleToInsert.putString(key, val);
                                        break;
                                    } else {
                                        break;
                                    }
                                } else {
                                    bundleToInsert.putLong(key, Long.valueOf(val).longValue());
                                    break;
                                }
                            } else {
                                bundleToInsert.putDouble(key, Double.valueOf(val).doubleValue());
                                break;
                            }
                        }
                    default:
                        pw.println("Error: Unknown option: " + opt);
                        return 1;
                }
            } else {
                String packageName = getNextArg();
                if (packageName == null) {
                    pw.println("Error: package name not specified");
                    return 1;
                }
                try {
                    try {
                        this.mInterface.setPackagesSuspendedAsUser(new String[]{packageName}, suspendedState, appExtras, launcherExtras, dialogMessage, Binder.getCallingUid() == 0 ? "root" : "com.android.shell", userId);
                        pw.println("Package " + packageName + " new suspended state: " + this.mInterface.isPackageSuspendedForUser(packageName, userId));
                        return 0;
                    } catch (RemoteException | IllegalArgumentException e) {
                        e = e;
                        pw.println(e.toString());
                        return 1;
                    }
                } catch (RemoteException | IllegalArgumentException e2) {
                    e = e2;
                    String str = packageName;
                    pw.println(e.toString());
                    return 1;
                }
            }
        }
    }

    private int runGrantRevokePermission(boolean grant) throws RemoteException {
        int userId = 0;
        while (true) {
            String nextOption = getNextOption();
            String opt = nextOption;
            if (nextOption == null) {
                break;
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            }
        }
        String pkg = getNextArg();
        if (pkg == null) {
            getErrPrintWriter().println("Error: no package specified");
            return 1;
        }
        String perm = getNextArg();
        if (perm == null) {
            getErrPrintWriter().println("Error: no permission specified");
            return 1;
        }
        if (grant) {
            this.mInterface.grantRuntimePermission(pkg, perm, userId);
        } else {
            this.mInterface.revokeRuntimePermission(pkg, perm, userId);
        }
        return 0;
    }

    private int runResetPermissions() throws RemoteException {
        this.mInterface.resetRuntimePermissions();
        return 0;
    }

    private int runSetPermissionEnforced() throws RemoteException {
        String permission = getNextArg();
        if (permission == null) {
            getErrPrintWriter().println("Error: no permission specified");
            return 1;
        }
        String enforcedRaw = getNextArg();
        if (enforcedRaw == null) {
            getErrPrintWriter().println("Error: no enforcement specified");
            return 1;
        }
        this.mInterface.setPermissionEnforced(permission, Boolean.parseBoolean(enforcedRaw));
        return 0;
    }

    private boolean isVendorApp(String pkg) {
        boolean z = false;
        try {
            PackageInfo info = this.mInterface.getPackageInfo(pkg, 0, 0);
            if (info != null && info.applicationInfo.isVendor()) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            return false;
        }
    }

    private boolean isProductApp(String pkg) {
        boolean z = false;
        try {
            PackageInfo info = this.mInterface.getPackageInfo(pkg, 0, 0);
            if (info != null && info.applicationInfo.isProduct()) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            return false;
        }
    }

    private int runGetPrivappPermissions() {
        ArraySet<String> privAppPermissions;
        String pkg = getNextArg();
        if (pkg == null) {
            getErrPrintWriter().println("Error: no package specified.");
            return 1;
        }
        if (isVendorApp(pkg)) {
            privAppPermissions = SystemConfig.getInstance().getVendorPrivAppPermissions(pkg);
        } else if (isProductApp(pkg)) {
            privAppPermissions = SystemConfig.getInstance().getProductPrivAppPermissions(pkg);
        } else {
            privAppPermissions = SystemConfig.getInstance().getPrivAppPermissions(pkg);
        }
        getOutPrintWriter().println(privAppPermissions == null ? "{}" : privAppPermissions.toString());
        return 0;
    }

    private int runGetPrivappDenyPermissions() {
        ArraySet<String> privAppPermissions;
        String pkg = getNextArg();
        if (pkg == null) {
            getErrPrintWriter().println("Error: no package specified.");
            return 1;
        }
        if (isVendorApp(pkg)) {
            privAppPermissions = SystemConfig.getInstance().getVendorPrivAppDenyPermissions(pkg);
        } else if (isProductApp(pkg)) {
            privAppPermissions = SystemConfig.getInstance().getProductPrivAppDenyPermissions(pkg);
        } else {
            privAppPermissions = SystemConfig.getInstance().getPrivAppDenyPermissions(pkg);
        }
        getOutPrintWriter().println(privAppPermissions == null ? "{}" : privAppPermissions.toString());
        return 0;
    }

    private int runGetOemPermissions() {
        String pkg = getNextArg();
        if (pkg == null) {
            getErrPrintWriter().println("Error: no package specified.");
            return 1;
        }
        Map<String, Boolean> oemPermissions = SystemConfig.getInstance().getOemPermissions(pkg);
        if (oemPermissions == null || oemPermissions.isEmpty()) {
            getOutPrintWriter().println("{}");
        } else {
            oemPermissions.forEach(new BiConsumer() {
                public final void accept(Object obj, Object obj2) {
                    PackageManagerShellCommand.lambda$runGetOemPermissions$0(PackageManagerShellCommand.this, (String) obj, (Boolean) obj2);
                }
            });
        }
        return 0;
    }

    public static /* synthetic */ void lambda$runGetOemPermissions$0(PackageManagerShellCommand packageManagerShellCommand, String permission, Boolean granted) {
        PrintWriter outPrintWriter = packageManagerShellCommand.getOutPrintWriter();
        outPrintWriter.println(permission + " granted:" + granted);
    }

    private String linkStateToString(int state) {
        switch (state) {
            case 0:
                return "undefined";
            case 1:
                return "ask";
            case 2:
                return "always";
            case 3:
                return "never";
            case 4:
                return "always ask";
            default:
                return "Unknown link state: " + state;
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private int runSetAppLink() throws RemoteException {
        char c;
        int newMode;
        int userId = 0;
        while (true) {
            String nextOption = getNextOption();
            String opt = nextOption;
            if (nextOption == null) {
                String pkg = getNextArg();
                if (pkg == null) {
                    getErrPrintWriter().println("Error: no package specified.");
                    return 1;
                }
                String modeString = getNextArg();
                if (modeString == null) {
                    getErrPrintWriter().println("Error: no app link state specified.");
                    return 1;
                }
                String lowerCase = modeString.toLowerCase();
                switch (lowerCase.hashCode()) {
                    case -1414557169:
                        if (lowerCase.equals("always")) {
                            c = 1;
                            break;
                        }
                    case -1038130864:
                        if (lowerCase.equals("undefined")) {
                            c = 0;
                            break;
                        }
                    case 96889:
                        if (lowerCase.equals("ask")) {
                            c = 2;
                            break;
                        }
                    case 104712844:
                        if (lowerCase.equals("never")) {
                            c = 4;
                            break;
                        }
                    case 1182785979:
                        if (lowerCase.equals("always-ask")) {
                            c = 3;
                            break;
                        }
                    default:
                        c = 65535;
                        break;
                }
                switch (c) {
                    case 0:
                        newMode = 0;
                        break;
                    case 1:
                        newMode = 2;
                        break;
                    case 2:
                        newMode = 1;
                        break;
                    case 3:
                        newMode = 4;
                        break;
                    case 4:
                        newMode = 3;
                        break;
                    default:
                        getErrPrintWriter().println("Error: unknown app link state '" + modeString + "'");
                        return 1;
                }
                PackageInfo info = this.mInterface.getPackageInfo(pkg, 0, userId);
                if (info == null) {
                    getErrPrintWriter().println("Error: package " + pkg + " not found.");
                    return 1;
                } else if ((info.applicationInfo.privateFlags & 16) == 0) {
                    getErrPrintWriter().println("Error: package " + pkg + " does not handle web links.");
                    return 1;
                } else if (this.mInterface.updateIntentVerificationStatus(pkg, newMode, userId)) {
                    return 0;
                } else {
                    getErrPrintWriter().println("Error: unable to update app link status for " + pkg);
                    return 1;
                }
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                getErrPrintWriter().println("Error: unknown option: " + opt);
                return 1;
            }
        }
    }

    private int runGetAppLink() throws RemoteException {
        int userId = 0;
        while (true) {
            String nextOption = getNextOption();
            String opt = nextOption;
            if (nextOption == null) {
                String pkg = getNextArg();
                if (pkg == null) {
                    getErrPrintWriter().println("Error: no package specified.");
                    return 1;
                }
                PackageInfo info = this.mInterface.getPackageInfo(pkg, 0, userId);
                if (info == null) {
                    getErrPrintWriter().println("Error: package " + pkg + " not found.");
                    return 1;
                } else if ((info.applicationInfo.privateFlags & 16) == 0) {
                    getErrPrintWriter().println("Error: package " + pkg + " does not handle web links.");
                    return 1;
                } else {
                    getOutPrintWriter().println(linkStateToString(this.mInterface.getIntentVerificationStatus(pkg, userId)));
                    return 0;
                }
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                getErrPrintWriter().println("Error: unknown option: " + opt);
                return 1;
            }
        }
    }

    private int runTrimCaches() throws RemoteException {
        String size = getNextArg();
        if (size == null) {
            getErrPrintWriter().println("Error: no size specified");
            return 1;
        }
        long multiplier = 1;
        int len = size.length();
        char c = size.charAt(len - 1);
        if (c < '0' || c > '9') {
            if (c == 'K' || c == 'k') {
                multiplier = 1024;
            } else if (c == 'M' || c == 'm') {
                multiplier = 1048576;
            } else if (c == 'G' || c == 'g') {
                multiplier = 1073741824;
            } else {
                PrintWriter errPrintWriter = getErrPrintWriter();
                errPrintWriter.println("Invalid suffix: " + c);
                return 1;
            }
            size = size.substring(0, len - 1);
        }
        long multiplier2 = multiplier;
        String size2 = size;
        try {
            long sizeVal = Long.parseLong(size2) * multiplier2;
            String volumeUuid = getNextArg();
            if ("internal".equals(volumeUuid)) {
                volumeUuid = null;
            }
            ClearDataObserver obs = new ClearDataObserver();
            this.mInterface.freeStorageAndNotify(volumeUuid, sizeVal, 2, obs);
            synchronized (obs) {
                while (!obs.finished) {
                    try {
                        obs.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            return 0;
        } catch (NumberFormatException e2) {
            NumberFormatException numberFormatException = e2;
            PrintWriter errPrintWriter2 = getErrPrintWriter();
            errPrintWriter2.println("Error: expected number at: " + size2);
            return 1;
        }
    }

    private static boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public int runCreateUser() throws RemoteException {
        UserInfo info;
        int userId = -1;
        int flags = 0;
        while (true) {
            String nextOption = getNextOption();
            String opt = nextOption;
            if (nextOption == null) {
                String arg = getNextArg();
                if (arg == null) {
                    getErrPrintWriter().println("Error: no user name specified.");
                    return 1;
                }
                String name = arg;
                IUserManager um = IUserManager.Stub.asInterface(ServiceManager.getService("user"));
                IAccountManager accm = IAccountManager.Stub.asInterface(ServiceManager.getService("account"));
                if ((flags & 8) != 0) {
                    int parentUserId = userId >= 0 ? userId : 0;
                    info = um.createRestrictedProfile(name, parentUserId);
                    accm.addSharedAccountsFromParentUser(parentUserId, userId, Process.myUid() == 0 ? "root" : "com.android.shell");
                } else if (userId < 0) {
                    info = um.createUser(name, flags);
                } else {
                    info = um.createProfileForUser(name, flags, userId, null);
                }
                if (info != null) {
                    getOutPrintWriter().println("Success: created user id " + info.id);
                    return 0;
                }
                getErrPrintWriter().println("Error: couldn't create User.");
                return 1;
            } else if ("--profileOf".equals(opt)) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else if ("--managed".equals(opt)) {
                flags |= 32;
            } else if ("--restricted".equals(opt)) {
                flags |= 8;
            } else if ("--ephemeral".equals(opt)) {
                flags |= 256;
            } else if ("--guest".equals(opt)) {
                flags |= 4;
            } else if ("--demo".equals(opt)) {
                flags |= 512;
            } else {
                getErrPrintWriter().println("Error: unknown option " + opt);
                return 1;
            }
        }
    }

    public int runRemoveUser() throws RemoteException {
        String arg = getNextArg();
        if (arg == null) {
            getErrPrintWriter().println("Error: no user id specified.");
            return 1;
        }
        int userId = UserHandle.parseUserArg(arg);
        if (IUserManager.Stub.asInterface(ServiceManager.getService("user")).removeUser(userId)) {
            getOutPrintWriter().println("Success: removed user");
            return 0;
        }
        PrintWriter errPrintWriter = getErrPrintWriter();
        errPrintWriter.println("Error: couldn't remove user id " + userId);
        return 1;
    }

    public int runSetUserRestriction() throws RemoteException {
        boolean value;
        int userId = 0;
        String opt = getNextOption();
        if (opt != null && "--user".equals(opt)) {
            userId = UserHandle.parseUserArg(getNextArgRequired());
        }
        String restriction = getNextArg();
        String arg = getNextArg();
        if ("1".equals(arg)) {
            value = true;
        } else if ("0".equals(arg)) {
            value = false;
        } else {
            getErrPrintWriter().println("Error: valid value not specified");
            return 1;
        }
        IUserManager.Stub.asInterface(ServiceManager.getService("user")).setUserRestriction(restriction, value, userId);
        return 0;
    }

    public int runGetMaxUsers() {
        PrintWriter outPrintWriter = getOutPrintWriter();
        outPrintWriter.println("Maximum supported users: " + UserManager.getMaxSupportedUsers());
        return 0;
    }

    public int runGetMaxRunningUsers() {
        PrintWriter outPrintWriter = getOutPrintWriter();
        outPrintWriter.println("Maximum supported running users: " + ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).getMaxRunningUsers());
        return 0;
    }

    private InstallParams makeInstallParams() {
        PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(1);
        InstallParams params = new InstallParams();
        params.sessionParams = sessionParams;
        boolean replaceExisting = true;
        while (true) {
            String nextOption = getNextOption();
            String opt = nextOption;
            if (nextOption == null) {
                return params;
            }
            char c = 65535;
            switch (opt.hashCode()) {
                case -1950997763:
                    if (opt.equals("--force-uuid")) {
                        c = 23;
                        break;
                    }
                    break;
                case -1777984902:
                    if (opt.equals("--dont-kill")) {
                        c = 9;
                        break;
                    }
                    break;
                case -1624001065:
                    if (opt.equals("--hwhdb")) {
                        c = 25;
                        break;
                    }
                    break;
                case -1313152697:
                    if (opt.equals("--install-location")) {
                        c = 22;
                        break;
                    }
                    break;
                case -1137116608:
                    if (opt.equals("--instantapp")) {
                        c = 18;
                        break;
                    }
                    break;
                case -951415743:
                    if (opt.equals("--instant")) {
                        c = 17;
                        break;
                    }
                    break;
                case -706813505:
                    if (opt.equals("--referrer")) {
                        c = 11;
                        break;
                    }
                    break;
                case 1477:
                    if (opt.equals("-R")) {
                        c = 2;
                        break;
                    }
                    break;
                case 1478:
                    if (opt.equals("-S")) {
                        c = 14;
                        break;
                    }
                    break;
                case 1495:
                    if (opt.equals("-d")) {
                        c = 7;
                        break;
                    }
                    break;
                case 1497:
                    if (opt.equals("-f")) {
                        c = 6;
                        break;
                    }
                    break;
                case 1498:
                    if (opt.equals("-g")) {
                        c = 8;
                        break;
                    }
                    break;
                case NetworkConstants.ETHER_MTU:
                    if (opt.equals("-i")) {
                        c = 3;
                        break;
                    }
                    break;
                case 1503:
                    if (opt.equals("-l")) {
                        c = 0;
                        break;
                    }
                    break;
                case 1507:
                    if (opt.equals("-p")) {
                        c = 12;
                        break;
                    }
                    break;
                case 1509:
                    if (opt.equals("-r")) {
                        c = 1;
                        break;
                    }
                    break;
                case 1510:
                    if (opt.equals("-s")) {
                        c = 5;
                        break;
                    }
                    break;
                case 1511:
                    if (opt.equals("-t")) {
                        c = 4;
                        break;
                    }
                    break;
                case 42995400:
                    if (opt.equals("--abi")) {
                        c = 15;
                        break;
                    }
                    break;
                case 43010092:
                    if (opt.equals("--pkg")) {
                        c = 13;
                        break;
                    }
                    break;
                case 148207464:
                    if (opt.equals("--originating-uri")) {
                        c = 10;
                        break;
                    }
                    break;
                case 1051781117:
                    if (opt.equals("--ephemeral")) {
                        c = 16;
                        break;
                    }
                    break;
                case 1067504745:
                    if (opt.equals("--preload")) {
                        c = 20;
                        break;
                    }
                    break;
                case 1333024815:
                    if (opt.equals("--full")) {
                        c = 19;
                        break;
                    }
                    break;
                case 1333469547:
                    if (opt.equals("--user")) {
                        c = 21;
                        break;
                    }
                    break;
                case 2015272120:
                    if (opt.equals("--force-sdk")) {
                        c = 24;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    sessionParams.installFlags |= 1;
                    break;
                case 1:
                    break;
                case 2:
                    replaceExisting = false;
                    break;
                case 3:
                    params.installerPackageName = getNextArg();
                    if (params.installerPackageName == null) {
                        throw new IllegalArgumentException("Missing installer package");
                    }
                    break;
                case 4:
                    sessionParams.installFlags |= 4;
                    break;
                case 5:
                    sessionParams.installFlags |= 8;
                    break;
                case 6:
                    sessionParams.installFlags |= 16;
                    break;
                case 7:
                    sessionParams.installFlags |= 128;
                    break;
                case 8:
                    sessionParams.installFlags |= 256;
                    break;
                case 9:
                    sessionParams.installFlags |= 4096;
                    break;
                case 10:
                    sessionParams.originatingUri = Uri.parse(getNextArg());
                    break;
                case 11:
                    sessionParams.referrerUri = Uri.parse(getNextArg());
                    break;
                case 12:
                    sessionParams.mode = 2;
                    sessionParams.appPackageName = getNextArg();
                    if (sessionParams.appPackageName == null) {
                        throw new IllegalArgumentException("Missing inherit package name");
                    }
                    break;
                case 13:
                    sessionParams.appPackageName = getNextArg();
                    if (sessionParams.appPackageName == null) {
                        throw new IllegalArgumentException("Missing package name");
                    }
                    break;
                case 14:
                    long sizeBytes = Long.parseLong(getNextArg());
                    if (sizeBytes > 0) {
                        sessionParams.setSize(sizeBytes);
                        break;
                    } else {
                        throw new IllegalArgumentException("Size must be positive");
                    }
                case 15:
                    sessionParams.abiOverride = checkAbiArgument(getNextArg());
                    break;
                case 16:
                case 17:
                case 18:
                    sessionParams.setInstallAsInstantApp(true);
                    break;
                case WindowManagerService.H.REPORT_WINDOWS_CHANGE /*19*/:
                    sessionParams.setInstallAsInstantApp(false);
                    break;
                case 20:
                    sessionParams.setInstallAsVirtualPreload();
                    break;
                case BackupHandler.MSG_OP_COMPLETE:
                    params.userId = UserHandle.parseUserArg(getNextArgRequired());
                    break;
                case WindowManagerService.H.REPORT_HARD_KEYBOARD_STATUS_CHANGE /*22*/:
                    sessionParams.installLocation = Integer.parseInt(getNextArg());
                    break;
                case WindowManagerService.H.BOOT_TIMEOUT /*23*/:
                    sessionParams.installFlags |= 512;
                    sessionParams.volumeUuid = getNextArg();
                    if ("internal".equals(sessionParams.volumeUuid)) {
                        sessionParams.volumeUuid = null;
                        break;
                    }
                    break;
                case 24:
                    sessionParams.installFlags |= 8192;
                    break;
                case WindowManagerService.H.SHOW_STRICT_MODE_VIOLATION /*25*/:
                    params.sessionParams.hdbEncode = getNextArg();
                    params.sessionParams.hdbArgIndex = getArgPos();
                    params.sessionParams.hdbArgs = getArgs();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown option " + opt);
            }
            if (replaceExisting) {
                sessionParams.installFlags |= 2;
            }
        }
    }

    private int runSetHomeActivity() {
        PrintWriter pw = getOutPrintWriter();
        int userId = 0;
        while (true) {
            String nextOption = getNextOption();
            String opt = nextOption;
            if (nextOption != null) {
                char c = 65535;
                if (opt.hashCode() == 1333469547 && opt.equals("--user")) {
                    c = 0;
                }
                if (c != 0) {
                    pw.println("Error: Unknown option: " + opt);
                    return 1;
                }
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                String component = getNextArg();
                ComponentName componentName = component != null ? ComponentName.unflattenFromString(component) : null;
                if (componentName == null) {
                    pw.println("Error: component name not specified or invalid");
                    return 1;
                }
                try {
                    this.mInterface.setHomeActivity(componentName, userId);
                    pw.println("Success");
                    return 0;
                } catch (Exception e) {
                    pw.println(e.toString());
                    return 1;
                }
            }
        }
    }

    private int runSetInstaller() throws RemoteException {
        String targetPackage = getNextArg();
        String installerPackageName = getNextArg();
        if (targetPackage == null || installerPackageName == null) {
            getErrPrintWriter().println("Must provide both target and installer package names");
            return 1;
        }
        this.mInterface.setInstallerPackageName(targetPackage, installerPackageName);
        getOutPrintWriter().println("Success");
        return 0;
    }

    private int runGetInstantAppResolver() {
        PrintWriter pw = getOutPrintWriter();
        try {
            ComponentName instantAppsResolver = this.mInterface.getInstantAppResolverComponent();
            if (instantAppsResolver == null) {
                return 1;
            }
            pw.println(instantAppsResolver.flattenToString());
            return 0;
        } catch (Exception e) {
            pw.println(e.toString());
            return 1;
        }
    }

    private int runHasFeature() {
        int version;
        PrintWriter err = getErrPrintWriter();
        String featureName = getNextArg();
        int i = 1;
        if (featureName == null) {
            err.println("Error: expected FEATURE name");
            return 1;
        }
        String versionString = getNextArg();
        if (versionString == null) {
            version = 0;
        } else {
            try {
                version = Integer.parseInt(versionString);
            } catch (NumberFormatException e) {
                err.println("Error: illegal version number " + versionString);
                return 1;
            } catch (RemoteException e2) {
                err.println(e2.toString());
                return 1;
            }
        }
        boolean hasFeature = this.mInterface.hasSystemFeature(featureName, version);
        getOutPrintWriter().println(hasFeature);
        if (hasFeature) {
            i = 0;
        }
        return i;
    }

    private int runDump() {
        String pkg = getNextArg();
        if (pkg == null) {
            getErrPrintWriter().println("Error: no package specified");
            return 1;
        }
        ActivityManager.dumpPackageStateStatic(getOutFileDescriptor(), pkg);
        return 0;
    }

    private int runSetHarmfulAppWarning() throws RemoteException {
        int userId = -2;
        while (true) {
            String nextOption = getNextOption();
            String opt = nextOption;
            if (nextOption == null) {
                int userId2 = translateUserId(userId, false, "runSetHarmfulAppWarning");
                this.mInterface.setHarmfulAppWarning(getNextArgRequired(), getNextArg(), userId2);
                return 0;
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                PrintWriter errPrintWriter = getErrPrintWriter();
                errPrintWriter.println("Error: Unknown option: " + opt);
                return -1;
            }
        }
    }

    private int runGetHarmfulAppWarning() throws RemoteException {
        int userId = -2;
        while (true) {
            String nextOption = getNextOption();
            String opt = nextOption;
            if (nextOption == null) {
                int userId2 = translateUserId(userId, false, "runGetHarmfulAppWarning");
                CharSequence warning = this.mInterface.getHarmfulAppWarning(getNextArgRequired(), userId2);
                if (TextUtils.isEmpty(warning)) {
                    return 1;
                }
                getOutPrintWriter().println(warning);
                return 0;
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                PrintWriter errPrintWriter = getErrPrintWriter();
                errPrintWriter.println("Error: Unknown option: " + opt);
                return -1;
            }
        }
    }

    private static String checkAbiArgument(String abi) {
        if (TextUtils.isEmpty(abi)) {
            throw new IllegalArgumentException("Missing ABI argument");
        } else if (STDIN_PATH.equals(abi)) {
            return abi;
        } else {
            for (String supportedAbi : Build.SUPPORTED_ABIS) {
                if (supportedAbi.equals(abi)) {
                    return abi;
                }
            }
            throw new IllegalArgumentException("ABI " + abi + " not supported on this device");
        }
    }

    private int translateUserId(int userId, boolean allowAll, String logContext) {
        return ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, allowAll, true, logContext, "pm command");
    }

    private int doCreateSession(PackageInstaller.SessionParams params, String installerPackageName, int userId) throws RemoteException {
        int userId2 = translateUserId(userId, true, "runInstallCreate");
        if (userId2 == -1) {
            userId2 = 0;
            params.installFlags |= 64;
        }
        return this.mInterface.getPackageInstaller().createSession(params, installerPackageName, userId2);
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x005f  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0069  */
    private int doWriteSplit(int sessionId, String inPath, long sizeBytes, String splitName, boolean logSuccess) throws RemoteException {
        long sizeBytes2;
        ParcelFileDescriptor fd;
        ParcelFileDescriptor parcelFileDescriptor;
        String str = inPath;
        PrintWriter pw = getOutPrintWriter();
        if (STDIN_PATH.equals(str)) {
            parcelFileDescriptor = new ParcelFileDescriptor(getInFileDescriptor());
        } else if (str != null) {
            ParcelFileDescriptor fd2 = openFileForSystem(str, "r");
            if (fd2 == null) {
                return -1;
            }
            long sizeBytes3 = fd2.getStatSize();
            if (sizeBytes3 < 0) {
                getErrPrintWriter().println("Unable to get size of: " + str);
                return -1;
            }
            fd = fd2;
            sizeBytes2 = sizeBytes3;
            if (sizeBytes2 > 0) {
                getErrPrintWriter().println("Error: must specify a APK size");
                return 1;
            }
            PackageInstaller.Session session = null;
            try {
                session = new PackageInstaller.Session(this.mInterface.getPackageInstaller().openSession(sessionId));
                session.write(splitName, 0, sizeBytes2, fd);
                if (logSuccess) {
                    pw.println("Success: streamed " + sizeBytes2 + " bytes");
                }
                return 0;
            } catch (IOException e) {
                getErrPrintWriter().println("Error: failed to write; " + e.getMessage());
                return 1;
            } finally {
                IoUtils.closeQuietly(session);
            }
        } else {
            parcelFileDescriptor = new ParcelFileDescriptor(getInFileDescriptor());
        }
        sizeBytes2 = sizeBytes;
        fd = parcelFileDescriptor;
        if (sizeBytes2 > 0) {
        }
    }

    private int doRemoveSplit(int sessionId, String splitName, boolean logSuccess) throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        PackageInstaller.Session session = null;
        try {
            session = new PackageInstaller.Session(this.mInterface.getPackageInstaller().openSession(sessionId));
            session.removeSplit(splitName);
            if (logSuccess) {
                pw.println("Success");
            }
            return 0;
        } catch (IOException e) {
            pw.println("Error: failed to remove split; " + e.getMessage());
            return 1;
        } finally {
            IoUtils.closeQuietly(session);
        }
    }

    private int doCommitSession(int sessionId, boolean logSuccess) throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        PackageInstaller.Session session = null;
        try {
            session = new PackageInstaller.Session(this.mInterface.getPackageInstaller().openSession(sessionId));
            DexMetadataHelper.validateDexPaths(session.getNames());
        } catch (IOException | IllegalStateException e) {
            pw.println("Warning [Could not validate the dex paths: " + e.getMessage() + "]");
        } catch (Throwable th) {
            IoUtils.closeQuietly(session);
            throw th;
        }
        LocalIntentReceiver receiver = new LocalIntentReceiver();
        session.commit(receiver.getIntentSender());
        int status = receiver.getResult().getIntExtra("android.content.pm.extra.STATUS", 1);
        if (status != 0) {
            pw.println("Failure [" + result.getStringExtra("android.content.pm.extra.STATUS_MESSAGE") + "]");
        } else if (logSuccess) {
            pw.println("Success");
        }
        IoUtils.closeQuietly(session);
        return status;
    }

    private int doAbandonSession(int sessionId, boolean logSuccess) throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        PackageInstaller.Session session = null;
        try {
            session = new PackageInstaller.Session(this.mInterface.getPackageInstaller().openSession(sessionId));
            session.abandon();
            if (logSuccess) {
                pw.println("Success");
            }
            return 0;
        } finally {
            IoUtils.closeQuietly(session);
        }
    }

    private void doListPermissions(ArrayList<String> groupList, boolean groups, boolean labels, boolean summary, int startProtectionLevel, int endProtectionLevel) throws RemoteException {
        ArrayList<String> arrayList = groupList;
        PrintWriter pw = getOutPrintWriter();
        int groupCount = groupList.size();
        int i = 0;
        int i2 = 0;
        while (i2 < groupCount) {
            String groupName = arrayList.get(i2);
            String prefix = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            if (groups) {
                if (i2 > 0) {
                    pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                }
                if (groupName != null) {
                    PermissionGroupInfo pgi = this.mInterface.getPermissionGroupInfo(groupName, i);
                    if (!summary) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(labels ? "+ " : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                        sb.append("group:");
                        sb.append(pgi.name);
                        pw.println(sb.toString());
                        if (labels) {
                            pw.println("  package:" + pgi.packageName);
                            if (getResources(pgi) != null) {
                                pw.println("  label:" + loadText(pgi, pgi.labelRes, pgi.nonLocalizedLabel));
                                pw.println("  description:" + loadText(pgi, pgi.descriptionRes, pgi.nonLocalizedDescription));
                            }
                        }
                    } else if (getResources(pgi) != null) {
                        pw.print(loadText(pgi, pgi.labelRes, pgi.nonLocalizedLabel) + ": ");
                    } else {
                        pw.print(pgi.name + ": ");
                    }
                } else {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append((!labels || summary) ? BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS : "+ ");
                    sb2.append("ungrouped:");
                    pw.println(sb2.toString());
                }
                prefix = "  ";
            }
            List<PermissionInfo> ps = this.mInterface.queryPermissionsByGroup(arrayList.get(i2), i).getList();
            int count = ps.size();
            boolean first = true;
            int p = i;
            while (p < count) {
                PermissionInfo pi = ps.get(p);
                if (!groups || groupName != null || pi.group == null) {
                    int base = pi.protectionLevel & 15;
                    if (base >= startProtectionLevel && base <= endProtectionLevel) {
                        if (summary) {
                            if (first) {
                                first = false;
                            } else {
                                pw.print(", ");
                            }
                            Resources res = getResources(pi);
                            if (res != null) {
                                Resources resources = res;
                                pw.print(loadText(pi, pi.labelRes, pi.nonLocalizedLabel));
                            } else {
                                pw.print(pi.name);
                            }
                        } else {
                            StringBuilder sb3 = new StringBuilder();
                            sb3.append(prefix);
                            sb3.append(labels ? "+ " : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                            sb3.append("permission:");
                            sb3.append(pi.name);
                            pw.println(sb3.toString());
                            if (labels) {
                                pw.println(prefix + "  package:" + pi.packageName);
                                Resources res2 = getResources(pi);
                                if (res2 != null) {
                                    StringBuilder sb4 = new StringBuilder();
                                    sb4.append(prefix);
                                    Resources resources2 = res2;
                                    sb4.append("  label:");
                                    sb4.append(loadText(pi, pi.labelRes, pi.nonLocalizedLabel));
                                    pw.println(sb4.toString());
                                    pw.println(prefix + "  description:" + loadText(pi, pi.descriptionRes, pi.nonLocalizedDescription));
                                }
                                pw.println(prefix + "  protectionLevel:" + PermissionInfo.protectionToString(pi.protectionLevel));
                            }
                        }
                    }
                }
                p++;
                ArrayList<String> arrayList2 = groupList;
            }
            if (summary) {
                pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            }
            i2++;
            arrayList = groupList;
            i = 0;
        }
    }

    private String loadText(PackageItemInfo pii, int res, CharSequence nonLocalized) throws RemoteException {
        if (nonLocalized != null) {
            return nonLocalized.toString();
        }
        if (res != 0) {
            Resources r = getResources(pii);
            if (r != null) {
                try {
                    return r.getString(res);
                } catch (Resources.NotFoundException e) {
                }
            }
        }
        return null;
    }

    private Resources getResources(PackageItemInfo pii) throws RemoteException {
        Resources res = this.mResourceCache.get(pii.packageName);
        if (res != null) {
            return res;
        }
        ApplicationInfo ai = this.mInterface.getApplicationInfo(pii.packageName, 0, 0);
        AssetManager am = new AssetManager();
        am.addAssetPath(ai.publicSourceDir);
        Resources res2 = new Resources(am, null, null);
        this.mResourceCache.put(pii.packageName, res2);
        return res2;
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("Package manager (package) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  path [--user USER_ID] PACKAGE");
        pw.println("    Print the path to the .apk of the given PACKAGE.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  dump PACKAGE");
        pw.println("    Print various system state associated with the given PACKAGE.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  list features");
        pw.println("    Prints all features of the system.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  has-feature FEATURE_NAME [version]");
        pw.println("    Prints true and returns exit status 0 when system has a FEATURE_NAME,");
        pw.println("    otherwise prints false and returns exit status 1");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  list instrumentation [-f] [TARGET-PACKAGE]");
        pw.println("    Prints all test packages; optionally only those targeting TARGET-PACKAGE");
        pw.println("    Options:");
        pw.println("      -f: dump the name of the .apk file containing the test package");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  list libraries");
        pw.println("    Prints all system libraries.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  list packages [-f] [-d] [-e] [-s] [-3] [-i] [-l] [-u] [-U] ");
        pw.println("      [--uid UID] [--user USER_ID] [FILTER]");
        pw.println("    Prints all packages; optionally only those whose name contains");
        pw.println("    the text in FILTER.  Options are:");
        pw.println("      -f: see their associated file");
        pw.println("      -d: filter to only show disabled packages");
        pw.println("      -e: filter to only show enabled packages");
        pw.println("      -s: filter to only show system packages");
        pw.println("      -3: filter to only show third party packages");
        pw.println("      -i: see the installer for the packages");
        pw.println("      -l: ignored (used for compatibility with older releases)");
        pw.println("      -U: also show the package UID");
        pw.println("      -u: also include uninstalled packages");
        pw.println("      --uid UID: filter to only show packages with the given UID");
        pw.println("      --user USER_ID: only list packages belonging to the given user");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  list permission-groups");
        pw.println("    Prints all known permission groups.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  list permissions [-g] [-f] [-d] [-u] [GROUP]");
        pw.println("    Prints all known permissions; optionally only those in GROUP.  Options are:");
        pw.println("      -g: organize by group");
        pw.println("      -f: print all information");
        pw.println("      -s: short summary");
        pw.println("      -d: only list dangerous permissions");
        pw.println("      -u: list only the permissions users will see");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  resolve-activity [--brief] [--components] [--user USER_ID] INTENT");
        pw.println("    Prints the activity that resolves to the given INTENT.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  query-activities [--brief] [--components] [--user USER_ID] INTENT");
        pw.println("    Prints all activities that can handle the given INTENT.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  query-services [--brief] [--components] [--user USER_ID] INTENT");
        pw.println("    Prints all services that can handle the given INTENT.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  query-receivers [--brief] [--components] [--user USER_ID] INTENT");
        pw.println("    Prints all broadcast receivers that can handle the given INTENT.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  install [-lrtsfdg] [-i PACKAGE] [--user USER_ID|all|current]");
        pw.println("       [-p INHERIT_PACKAGE] [--install-location 0/1/2]");
        pw.println("       [--originating-uri URI] [---referrer URI]");
        pw.println("       [--abi ABI_NAME] [--force-sdk]");
        pw.println("       [--preload] [--instantapp] [--full] [--dont-kill]");
        pw.println("       [--force-uuid internal|UUID] [--pkg PACKAGE] [-S BYTES] [PATH|-]");
        pw.println("    Install an application.  Must provide the apk data to install, either as a");
        pw.println("    file path or '-' to read from stdin.  Options are:");
        pw.println("      -l: forward lock application");
        pw.println("      -R: disallow replacement of existing application");
        pw.println("      -t: allow test packages");
        pw.println("      -i: specify package name of installer owning the app");
        pw.println("      -s: install application on sdcard");
        pw.println("      -f: install application on internal flash");
        pw.println("      -d: allow version code downgrade (debuggable packages only)");
        pw.println("      -p: partial application install (new split on top of existing pkg)");
        pw.println("      -g: grant all runtime permissions");
        pw.println("      -S: size in bytes of package, required for stdin");
        pw.println("      --user: install under the given user.");
        pw.println("      --dont-kill: installing a new feature split, don't kill running app");
        pw.println("      --originating-uri: set URI where app was downloaded from");
        pw.println("      --referrer: set URI that instigated the install of the app");
        pw.println("      --pkg: specify expected package name of app being installed");
        pw.println("      --abi: override the default ABI of the platform");
        pw.println("      --instantapp: cause the app to be installed as an ephemeral install app");
        pw.println("      --full: cause the app to be installed as a non-ephemeral full app");
        pw.println("      --install-location: force the install location:");
        pw.println("          0=auto, 1=internal only, 2=prefer external");
        pw.println("      --force-uuid: force install on to disk volume with given UUID");
        pw.println("      --force-sdk: allow install even when existing app targets platform");
        pw.println("          codename but new one targets a final API level");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  install-create [-lrtsfdg] [-i PACKAGE] [--user USER_ID|all|current]");
        pw.println("       [-p INHERIT_PACKAGE] [--install-location 0/1/2]");
        pw.println("       [--originating-uri URI] [---referrer URI]");
        pw.println("       [--abi ABI_NAME] [--force-sdk]");
        pw.println("       [--preload] [--instantapp] [--full] [--dont-kill]");
        pw.println("       [--force-uuid internal|UUID] [--pkg PACKAGE] [-S BYTES]");
        pw.println("    Like \"install\", but starts an install session.  Use \"install-write\"");
        pw.println("    to push data into the session, and \"install-commit\" to finish.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  install-write [-S BYTES] SESSION_ID SPLIT_NAME [PATH|-]");
        pw.println("    Write an apk into the given install session.  If the path is '-', data");
        pw.println("    will be read from stdin.  Options are:");
        pw.println("      -S: size in bytes of package, required for stdin");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  install-commit SESSION_ID");
        pw.println("    Commit the given active install session, installing the app.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  install-abandon SESSION_ID");
        pw.println("    Delete the given active install session.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  set-install-location LOCATION");
        pw.println("    Changes the default install location.  NOTE this is only intended for debugging;");
        pw.println("    using this can cause applications to break and other undersireable behavior.");
        pw.println("    LOCATION is one of:");
        pw.println("    0 [auto]: Let system decide the best location");
        pw.println("    1 [internal]: Install on internal device storage");
        pw.println("    2 [external]: Install on external media");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  get-install-location");
        pw.println("    Returns the current install location: 0, 1 or 2 as per set-install-location.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  move-package PACKAGE [internal|UUID]");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  move-primary-storage [internal|UUID]");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  pm uninstall [-k] [--user USER_ID] [--versionCode VERSION_CODE] PACKAGE [SPLIT]");
        pw.println("    Remove the given package name from the system.  May remove an entire app");
        pw.println("    if no SPLIT name is specified, otherwise will remove only the split of the");
        pw.println("    given app.  Options are:");
        pw.println("      -k: keep the data and cache directories around after package removal.");
        pw.println("      --user: remove the app from the given user.");
        pw.println("      --versionCode: only uninstall if the app has the given version code.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  clear [--user USER_ID] PACKAGE");
        pw.println("    Deletes all data associated with a package.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  enable [--user USER_ID] PACKAGE_OR_COMPONENT");
        pw.println("  disable [--user USER_ID] PACKAGE_OR_COMPONENT");
        pw.println("  disable-user [--user USER_ID] PACKAGE_OR_COMPONENT");
        pw.println("  disable-until-used [--user USER_ID] PACKAGE_OR_COMPONENT");
        pw.println("  default-state [--user USER_ID] PACKAGE_OR_COMPONENT");
        pw.println("    These commands change the enabled state of a given package or");
        pw.println("    component (written as \"package/class\").");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  hide [--user USER_ID] PACKAGE_OR_COMPONENT");
        pw.println("  unhide [--user USER_ID] PACKAGE_OR_COMPONENT");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  suspend [--user USER_ID] TARGET-PACKAGE");
        pw.println("    Suspends the specified package (as user).");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  unsuspend [--user USER_ID] TARGET-PACKAGE");
        pw.println("    Unsuspends the specified package (as user).");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  grant [--user USER_ID] PACKAGE PERMISSION");
        pw.println("  revoke [--user USER_ID] PACKAGE PERMISSION");
        pw.println("    These commands either grant or revoke permissions to apps.  The permissions");
        pw.println("    must be declared as used in the app's manifest, be runtime permissions");
        pw.println("    (protection level dangerous), and the app targeting SDK greater than Lollipop MR1.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  reset-permissions");
        pw.println("    Revert all runtime permissions to their default state.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  set-permission-enforced PERMISSION [true|false]");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  get-privapp-permissions TARGET-PACKAGE");
        pw.println("    Prints all privileged permissions for a package.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  get-privapp-deny-permissions TARGET-PACKAGE");
        pw.println("    Prints all privileged permissions that are denied for a package.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  get-oem-permissions TARGET-PACKAGE");
        pw.println("    Prints all OEM permissions for a package.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  set-app-link [--user USER_ID] PACKAGE {always|ask|never|undefined}");
        pw.println("  get-app-link [--user USER_ID] PACKAGE");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  trim-caches DESIRED_FREE_SPACE [internal|UUID]");
        pw.println("    Trim cache files to reach the given free space.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  create-user [--profileOf USER_ID] [--managed] [--restricted] [--ephemeral]");
        pw.println("      [--guest] USER_NAME");
        pw.println("    Create a new user with the given USER_NAME, printing the new user identifier");
        pw.println("    of the user.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  remove-user USER_ID");
        pw.println("    Remove the user with the given USER_IDENTIFIER, deleting all data");
        pw.println("    associated with that user");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  set-user-restriction [--user USER_ID] RESTRICTION VALUE");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  get-max-users");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  get-max-running-users");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  compile [-m MODE | -r REASON] [-f] [-c] [--split SPLIT_NAME]");
        pw.println("          [--reset] [--check-prof (true | false)] (-a | TARGET-PACKAGE)");
        pw.println("    Trigger compilation of TARGET-PACKAGE or all packages if \"-a\".  Options are:");
        pw.println("      -a: compile all packages");
        pw.println("      -c: clear profile data before compiling");
        pw.println("      -f: force compilation even if not needed");
        pw.println("      -m: select compilation mode");
        pw.println("          MODE is one of the dex2oat compiler filters:");
        pw.println("            assume-verified");
        pw.println("            extract");
        pw.println("            verify");
        pw.println("            quicken");
        pw.println("            space-profile");
        pw.println("            space");
        pw.println("            speed-profile");
        pw.println("            speed");
        pw.println("            everything");
        pw.println("      -r: select compilation reason");
        pw.println("          REASON is one of:");
        for (int i = 0; i < PackageManagerServiceCompilerMapping.REASON_STRINGS.length; i++) {
            pw.println("            " + PackageManagerServiceCompilerMapping.REASON_STRINGS[i]);
        }
        pw.println("      --reset: restore package to its post-install state");
        pw.println("      --check-prof (true | false): look at profiles when doing dexopt?");
        pw.println("      --secondary-dex: compile app secondary dex files");
        pw.println("      --split SPLIT: compile only the given split name");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  force-dex-opt PACKAGE");
        pw.println("    Force immediate execution of dex opt for the given PACKAGE.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  bg-dexopt-job");
        pw.println("    Execute the background optimizations immediately.");
        pw.println("    Note that the command only runs the background optimizer logic. It may");
        pw.println("    overlap with the actual job but the job scheduler will not be able to");
        pw.println("    cancel it. It will also run even if the device is not in the idle");
        pw.println("    maintenance mode.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  reconcile-secondary-dex-files TARGET-PACKAGE");
        pw.println("    Reconciles the package secondary dex files with the generated oat files.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  dump-profiles TARGET-PACKAGE");
        pw.println("    Dumps method/class profile files to");
        pw.println("    /data/misc/profman/TARGET-PACKAGE.txt");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  snapshot-profile TARGET-PACKAGE [--code-path path]");
        pw.println("    Take a snapshot of the package profiles to");
        pw.println("    /data/misc/profman/TARGET-PACKAGE[-code-path].prof");
        pw.println("    If TARGET-PACKAGE=android it will take a snapshot of the boot image");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  set-home-activity [--user USER_ID] TARGET-COMPONENT");
        pw.println("    Set the default home activity (aka launcher).");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  set-installer PACKAGE INSTALLER");
        pw.println("    Set installer package name");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  get-instantapp-resolver");
        pw.println("    Return the name of the component that is the current instant app installer.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  set-harmful-app-warning [--user <USER_ID>] <PACKAGE> [<WARNING>]");
        pw.println("    Mark the app as harmful with the given warning message.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  get-harmful-app-warning [--user <USER_ID>] <PACKAGE>");
        pw.println("    Return the harmful app warning message for the given app, if present");
        pw.println();
        pw.println("  uninstall-system-updates");
        pw.println("    Remove updates to all system applications and fall back to their /system version.");
        pw.println();
        Intent.printIntentArgsHelp(pw, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
    }
}

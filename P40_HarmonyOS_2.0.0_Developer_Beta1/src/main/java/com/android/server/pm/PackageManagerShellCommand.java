package com.android.server.pm;

import android.accounts.IAccountManager;
import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.role.IRoleManager;
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
import android.content.pm.ModuleInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageParser;
import android.content.pm.ParceledListSlice;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.SuspendDialogInfo;
import android.content.pm.UserInfo;
import android.content.pm.VersionedPackage;
import android.content.pm.dex.DexMetadataHelper;
import android.content.pm.dex.ISnapshotRuntimeProfileCallback;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.rollback.IRollbackManager;
import android.content.rollback.PackageRollbackInfo;
import android.content.rollback.RollbackInfo;
import android.hardware.biometrics.face.V1_0.FaceAcquiredInfo;
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
import android.os.RemoteCallback;
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
import android.util.AndroidException;
import android.util.ArraySet;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import com.android.internal.content.PackageHelper;
import com.android.internal.util.ArrayUtils;
import com.android.server.BatteryService;
import com.android.server.LocalServices;
import com.android.server.SystemConfig;
import com.android.server.TrustedUIService;
import com.android.server.UiModeManagerService;
import com.android.server.hdmi.HdmiCecKeycode;
import com.android.server.location.IHwGnssLocationProvider;
import com.android.server.net.NetworkPolicyManagerService;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.voiceinteraction.DatabaseHelper;
import com.huawei.android.content.pm.HwPackageManager;
import dalvik.system.DexFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import libcore.io.IoUtils;
import libcore.io.Streams;

/* access modifiers changed from: package-private */
public class PackageManagerShellCommand extends ShellCommand {
    private static final String ART_PROFILE_SNAPSHOT_DEBUG_LOCATION = "/data/misc/profman/";
    private static final int REPAIR_MODE_USER_ID = 127;
    private static final String STDIN_PATH = "-";
    private static final String TAG = "PackageManagerShellCommand";
    boolean mBrief;
    boolean mComponents;
    final IPackageManager mInterface;
    int mQueryFlags;
    private final WeakHashMap<String, Resources> mResourceCache = new WeakHashMap<>();
    int mTargetUser;

    PackageManagerShellCommand(PackageManagerService service) {
        this.mInterface = service;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
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
                        c = '8';
                        break;
                    }
                    c = 65535;
                    break;
                case -1967190973:
                    if (cmd.equals("install-abandon")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case -1937348290:
                    if (cmd.equals("get-install-location")) {
                        c = 16;
                        break;
                    }
                    c = 65535;
                    break;
                case -1852006340:
                    if (cmd.equals("suspend")) {
                        c = '#';
                        break;
                    }
                    c = 65535;
                    break;
                case -1846646502:
                    if (cmd.equals("get-max-running-users")) {
                        c = '3';
                        break;
                    }
                    c = 65535;
                    break;
                case -1741208611:
                    if (cmd.equals("set-installer")) {
                        c = '5';
                        break;
                    }
                    c = 65535;
                    break;
                case -1347307837:
                    if (cmd.equals("has-feature")) {
                        c = '7';
                        break;
                    }
                    c = 65535;
                    break;
                case -1298848381:
                    if (cmd.equals("enable")) {
                        c = 28;
                        break;
                    }
                    c = 65535;
                    break;
                case -1267782244:
                    if (cmd.equals("get-instantapp-resolver")) {
                        c = '6';
                        break;
                    }
                    c = 65535;
                    break;
                case -1231004208:
                    if (cmd.equals("resolve-activity")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -1102348235:
                    if (cmd.equals("get-privapp-deny-permissions")) {
                        c = '*';
                        break;
                    }
                    c = 65535;
                    break;
                case -1091400553:
                    if (cmd.equals("get-oem-permissions")) {
                        c = '+';
                        break;
                    }
                    c = 65535;
                    break;
                case -1070704814:
                    if (cmd.equals("get-privapp-permissions")) {
                        c = ')';
                        break;
                    }
                    c = 65535;
                    break;
                case -1032029296:
                    if (cmd.equals("disable-user")) {
                        c = 30;
                        break;
                    }
                    c = 65535;
                    break;
                case -934343034:
                    if (cmd.equals("revoke")) {
                        c = '&';
                        break;
                    }
                    c = 65535;
                    break;
                case -919935069:
                    if (cmd.equals("dump-profiles")) {
                        c = 24;
                        break;
                    }
                    c = 65535;
                    break;
                case -840566949:
                    if (cmd.equals("unhide")) {
                        c = '\"';
                        break;
                    }
                    c = 65535;
                    break;
                case -625596190:
                    if (cmd.equals("uninstall")) {
                        c = 26;
                        break;
                    }
                    c = 65535;
                    break;
                case -623224643:
                    if (cmd.equals("get-app-link")) {
                        c = '-';
                        break;
                    }
                    c = 65535;
                    break;
                case -539710980:
                    if (cmd.equals("create-user")) {
                        c = '/';
                        break;
                    }
                    c = 65535;
                    break;
                case -458695741:
                    if (cmd.equals("query-services")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -444750796:
                    if (cmd.equals("bg-dexopt-job")) {
                        c = 23;
                        break;
                    }
                    c = 65535;
                    break;
                case -440994401:
                    if (cmd.equals("query-receivers")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -416698598:
                    if (cmd.equals("get-stagedsessions")) {
                        c = ':';
                        break;
                    }
                    c = 65535;
                    break;
                case -339687564:
                    if (cmd.equals("remove-user")) {
                        c = '0';
                        break;
                    }
                    c = 65535;
                    break;
                case -297079916:
                    if (cmd.equals("has-hwfeature")) {
                        c = '>';
                        break;
                    }
                    c = 65535;
                    break;
                case -220055275:
                    if (cmd.equals("set-permission-enforced")) {
                        c = '(';
                        break;
                    }
                    c = 65535;
                    break;
                case -140205181:
                    if (cmd.equals("unsuspend")) {
                        c = '$';
                        break;
                    }
                    c = 65535;
                    break;
                case -132384343:
                    if (cmd.equals("install-commit")) {
                        c = '\n';
                        break;
                    }
                    c = 65535;
                    break;
                case -129863314:
                    if (cmd.equals("install-create")) {
                        c = 11;
                        break;
                    }
                    c = 65535;
                    break;
                case -115000827:
                    if (cmd.equals("default-state")) {
                        c = ' ';
                        break;
                    }
                    c = 65535;
                    break;
                case -87258188:
                    if (cmd.equals("move-primary-storage")) {
                        c = 19;
                        break;
                    }
                    c = 65535;
                    break;
                case 3095028:
                    if (cmd.equals("dump")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 3202370:
                    if (cmd.equals("hide")) {
                        c = '!';
                        break;
                    }
                    c = 65535;
                    break;
                case 3322014:
                    if (cmd.equals("list")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 3433509:
                    if (cmd.equals("path")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 18936394:
                    if (cmd.equals("move-package")) {
                        c = 18;
                        break;
                    }
                    c = 65535;
                    break;
                case 86600360:
                    if (cmd.equals("get-max-users")) {
                        c = '2';
                        break;
                    }
                    c = 65535;
                    break;
                case 94746189:
                    if (cmd.equals("clear")) {
                        c = 27;
                        break;
                    }
                    c = 65535;
                    break;
                case 98615580:
                    if (cmd.equals("grant")) {
                        c = '%';
                        break;
                    }
                    c = 65535;
                    break;
                case 107262333:
                    if (cmd.equals("install-existing")) {
                        c = 14;
                        break;
                    }
                    c = 65535;
                    break;
                case 139892533:
                    if (cmd.equals("get-harmful-app-warning")) {
                        c = '9';
                        break;
                    }
                    c = 65535;
                    break;
                case 237392952:
                    if (cmd.equals("install-add-session")) {
                        c = 17;
                        break;
                    }
                    c = 65535;
                    break;
                case 287820022:
                    if (cmd.equals("install-remove")) {
                        c = '\f';
                        break;
                    }
                    c = 65535;
                    break;
                case 359572742:
                    if (cmd.equals("reset-permissions")) {
                        c = '\'';
                        break;
                    }
                    c = 65535;
                    break;
                case 377019320:
                    if (cmd.equals("rollback-app")) {
                        c = '<';
                        break;
                    }
                    c = 65535;
                    break;
                case 467549856:
                    if (cmd.equals("snapshot-profile")) {
                        c = 25;
                        break;
                    }
                    c = 65535;
                    break;
                case 798023112:
                    if (cmd.equals("install-destroy")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case 826473335:
                    if (cmd.equals("uninstall-system-updates")) {
                        c = ';';
                        break;
                    }
                    c = 65535;
                    break;
                case 925176533:
                    if (cmd.equals("set-user-restriction")) {
                        c = '1';
                        break;
                    }
                    c = 65535;
                    break;
                case 925767985:
                    if (cmd.equals("set-app-link")) {
                        c = ',';
                        break;
                    }
                    c = 65535;
                    break;
                case 950491699:
                    if (cmd.equals("compile")) {
                        c = 20;
                        break;
                    }
                    c = 65535;
                    break;
                case 1053409810:
                    if (cmd.equals("query-activities")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 1124603675:
                    if (cmd.equals("force-dex-opt")) {
                        c = 22;
                        break;
                    }
                    c = 65535;
                    break;
                case 1177857340:
                    if (cmd.equals("trim-caches")) {
                        c = '.';
                        break;
                    }
                    c = 65535;
                    break;
                case 1429366290:
                    if (cmd.equals("set-home-activity")) {
                        c = '4';
                        break;
                    }
                    c = 65535;
                    break;
                case 1538306349:
                    if (cmd.equals("install-write")) {
                        c = '\r';
                        break;
                    }
                    c = 65535;
                    break;
                case 1671308008:
                    if (cmd.equals("disable")) {
                        c = 29;
                        break;
                    }
                    c = 65535;
                    break;
                case 1697997009:
                    if (cmd.equals("disable-until-used")) {
                        c = 31;
                        break;
                    }
                    c = 65535;
                    break;
                case 1746695602:
                    if (cmd.equals("set-install-location")) {
                        c = 15;
                        break;
                    }
                    c = 65535;
                    break;
                case 1783979817:
                    if (cmd.equals("reconcile-secondary-dex-files")) {
                        c = 21;
                        break;
                    }
                    c = 65535;
                    break;
                case 1858863089:
                    if (cmd.equals("get-moduleinfo")) {
                        c = '=';
                        break;
                    }
                    c = 65535;
                    break;
                case 1957569947:
                    if (cmd.equals("install")) {
                        c = 7;
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
                case '\b':
                case '\t':
                    return runInstallAbandon();
                case '\n':
                    return runInstallCommit();
                case 11:
                    return runInstallCreate();
                case '\f':
                    return runInstallRemove();
                case '\r':
                    return runInstallWrite();
                case 14:
                    return runInstallExisting();
                case 15:
                    return runSetInstallLocation();
                case 16:
                    return runGetInstallLocation();
                case 17:
                    return runInstallAddSession();
                case 18:
                    return runMovePackage();
                case FaceAcquiredInfo.FACE_OBSCURED /* 19 */:
                    return runMovePrimaryStorage();
                case 20:
                    return runCompile();
                case 21:
                    return runreconcileSecondaryDexFiles();
                case FaceAcquiredInfo.VENDOR /* 22 */:
                    return runForceDexOpt();
                case 23:
                    return runDexoptJob();
                case 24:
                    return runDumpProfiles();
                case 25:
                    return runSnapshotProfile();
                case TrustedUIService.TUI_POLL_FOLD /* 26 */:
                    return runUninstall();
                case 27:
                    if (!isInRepairMode()) {
                        return runClear();
                    }
                    pw.println("Failure [adb shell pm clear is not supported in REPAIR MODE]");
                    return 0;
                case 28:
                    return runSetEnabledSetting(1);
                case HdmiCecKeycode.CEC_KEYCODE_NUMBER_ENTRY_MODE /* 29 */:
                    return runSetEnabledSetting(2);
                case HdmiCecKeycode.CEC_KEYCODE_NUMBER_11 /* 30 */:
                    return runSetEnabledSetting(3);
                case HdmiCecKeycode.CEC_KEYCODE_NUMBER_12 /* 31 */:
                    return runSetEnabledSetting(4);
                case ' ':
                    return runSetEnabledSetting(0);
                case HdmiCecKeycode.CEC_KEYCODE_NUMBERS_1 /* 33 */:
                    return runSetHiddenSetting(true);
                case '\"':
                    return runSetHiddenSetting(false);
                case '#':
                    return runSuspend(true);
                case '$':
                    return runSuspend(false);
                case HdmiCecKeycode.CEC_KEYCODE_NUMBERS_5 /* 37 */:
                    return runGrantRevokePermission(true);
                case HdmiCecKeycode.CEC_KEYCODE_NUMBERS_6 /* 38 */:
                    return runGrantRevokePermission(false);
                case HdmiCecKeycode.CEC_KEYCODE_NUMBERS_7 /* 39 */:
                    return runResetPermissions();
                case '(':
                    return runSetPermissionEnforced();
                case ')':
                    return runGetPrivappPermissions();
                case '*':
                    return runGetPrivappDenyPermissions();
                case '+':
                    return runGetOemPermissions();
                case ',':
                    return runSetAppLink();
                case NetworkPolicyManagerService.TYPE_RAPID /* 45 */:
                    return runGetAppLink();
                case '.':
                    return runTrimCaches();
                case HdmiCecKeycode.CEC_KEYCODE_NEXT_FAVORITE /* 47 */:
                    return runCreateUser();
                case '0':
                    return runRemoveUser();
                case HdmiCecKeycode.CEC_KEYCODE_CHANNEL_DOWN /* 49 */:
                    return runSetUserRestriction();
                case HdmiCecKeycode.CEC_KEYCODE_PREVIOUS_CHANNEL /* 50 */:
                    return runGetMaxUsers();
                case HdmiCecKeycode.CEC_KEYCODE_SOUND_SELECT /* 51 */:
                    return runGetMaxRunningUsers();
                case HdmiCecKeycode.CEC_KEYCODE_INPUT_SELECT /* 52 */:
                    return runSetHomeActivity();
                case '5':
                    return runSetInstaller();
                case HdmiCecKeycode.CEC_KEYCODE_HELP /* 54 */:
                    return runGetInstantAppResolver();
                case '7':
                    return runHasFeature();
                case '8':
                    return runSetHarmfulAppWarning();
                case IHwGnssLocationProvider.GEOFENCE_CALLBACK_FENCE_STATUS /* 57 */:
                    return runGetHarmfulAppWarning();
                case IHwGnssLocationProvider.GEOFENCE_CALLBACK_ADD_RESULT /* 58 */:
                    return getStagedSessions();
                case IHwGnssLocationProvider.GEOFENCE_CALLBACK_REMOVE_RESULT /* 59 */:
                    return uninstallSystemUpdates();
                case '<':
                    return runRollbackApp();
                case '=':
                    return runGetModuleInfo();
                case '>':
                    return runHasHwFeature();
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
        } catch (RemoteException e) {
            pw.println("Remote exception: " + e);
            return -1;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0034  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x004c  */
    private int runGetModuleInfo() {
        boolean z;
        PrintWriter pw = getOutPrintWriter();
        int flags = 0;
        while (true) {
            String opt = getNextOption();
            if (opt != null) {
                int hashCode = opt.hashCode();
                if (hashCode != 42995713) {
                    if (hashCode == 517440986 && opt.equals("--installed")) {
                        z = true;
                        if (!z) {
                            flags |= DumpState.DUMP_INTENT_FILTER_VERIFIERS;
                        } else if (!z) {
                            pw.println("Error: Unknown option: " + opt);
                            return -1;
                        }
                    }
                } else if (opt.equals("--all")) {
                    z = false;
                    if (!z) {
                    }
                }
                z = true;
                if (!z) {
                }
            } else {
                String moduleName = getNextArg();
                if (moduleName != null) {
                    try {
                        ModuleInfo m = this.mInterface.getModuleInfo(moduleName, flags);
                        pw.println(m.toString() + " packageName: " + m.getPackageName());
                    } catch (RemoteException e) {
                        pw.println("Failure [" + e.getClass().getName() + " - " + e.getMessage() + "]");
                        return -1;
                    }
                } else {
                    for (ModuleInfo m2 : this.mInterface.getInstalledModules(flags)) {
                        pw.println(m2.toString() + " packageName: " + m2.getPackageName());
                    }
                }
                return 1;
            }
        }
    }

    private int getStagedSessions() {
        PrintWriter pw = getOutPrintWriter();
        try {
            for (PackageInstaller.SessionInfo session : this.mInterface.getPackageInstaller().getStagedSessions().getList()) {
                pw.println("appPackageName = " + session.getAppPackageName() + "; sessionId = " + session.getSessionId() + "; isStaged = " + session.isStaged() + "; isStagedSessionReady = " + session.isStagedSessionReady() + "; isStagedSessionApplied = " + session.isStagedSessionApplied() + "; isStagedSessionFailed = " + session.isStagedSessionFailed() + ";");
            }
            return 1;
        } catch (RemoteException e) {
            pw.println("Failure [" + e.getClass().getName() + " - " + e.getMessage() + "]");
            return 0;
        }
    }

    private int uninstallSystemUpdates() {
        PrintWriter pw = getOutPrintWriter();
        List<String> failedUninstalls = new LinkedList<>();
        try {
            ParceledListSlice<ApplicationInfo> packages = this.mInterface.getInstalledApplications((int) DumpState.DUMP_DEXOPT, 0);
            IPackageInstaller installer = this.mInterface.getPackageInstaller();
            for (ApplicationInfo info : packages.getList()) {
                if (info.isUpdatedSystemApp()) {
                    pw.println("Uninstalling updates to " + info.packageName + "...");
                    LocalIntentReceiver receiver = new LocalIntentReceiver();
                    installer.uninstall(new VersionedPackage(info.packageName, info.versionCode), (String) null, 0, receiver.getIntentSender(), 0);
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

    private int runRollbackApp() {
        PrintWriter pw = getOutPrintWriter();
        String packageName = getNextArgRequired();
        if (packageName == null) {
            pw.println("Error: package name not specified");
            return 1;
        }
        LocalIntentReceiver receiver = new LocalIntentReceiver();
        try {
            IRollbackManager rm = IRollbackManager.Stub.asInterface(ServiceManager.getService("rollback"));
            RollbackInfo rollback = null;
            for (RollbackInfo r : rm.getAvailableRollbacks().getList()) {
                Iterator it = r.getPackages().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    } else if (packageName.equals(((PackageRollbackInfo) it.next()).getPackageName())) {
                        rollback = r;
                        break;
                    }
                }
            }
            if (rollback == null) {
                pw.println("No available rollbacks for: " + packageName);
                return 1;
            }
            rm.commitRollback(rollback.getRollbackId(), ParceledListSlice.emptyList(), "com.android.shell", receiver.getIntentSender());
            Intent result = receiver.getResult();
            if (result.getIntExtra("android.content.rollback.extra.STATUS", 1) == 0) {
                pw.println("Success");
                return 0;
            }
            pw.println("Failure [" + result.getStringExtra("android.content.rollback.extra.STATUS_MESSAGE") + "]");
            return 1;
        } catch (RemoteException e) {
        }
    }

    private void setParamsSize(InstallParams params, String inPath) {
        if (params.sessionParams.sizeBytes == -1 && !STDIN_PATH.equals(inPath)) {
            ParcelFileDescriptor fd = openFileForSystem(inPath, "r");
            if (fd != null) {
                try {
                    params.sessionParams.setSize(PackageHelper.calculateInstalledSize(new PackageParser.PackageLite((String) null, PackageParser.parseApkLite(fd.getFileDescriptor(), inPath, 0), (String[]) null, (boolean[]) null, (String[]) null, (String[]) null, (String[]) null, (int[]) null), params.sessionParams.abiOverride, fd.getFileDescriptor()));
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
            String[] strArr = info.applicationInfo.splitSourceDirs;
            for (String splitSourceDir : strArr) {
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

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
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
                c = 65535;
                break;
            case -807062458:
                if (type.equals("package")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -290659267:
                if (type.equals("features")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 111578632:
                if (type.equals(DatabaseHelper.SoundModelContract.KEY_USERS)) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 408056396:
                if (type.equals("hwfeatures")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 544550766:
                if (type.equals("instrumentation")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 750867693:
                if (type.equals("packages")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 812757657:
                if (type.equals("libraries")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1133704324:
                if (type.equals("permissions")) {
                    c = 6;
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
            case '\b':
                return runListHwFeatures();
            default:
                pw.println("Error: unknown list type '" + type + "'");
                return -1;
        }
    }

    private int runListFeatures() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        List<FeatureInfo> list = this.mInterface.getSystemAvailableFeatures().getList();
        Collections.sort(list, new Comparator<FeatureInfo>() {
            /* class com.android.server.pm.PackageManagerShellCommand.AnonymousClass1 */

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

    private int runListInstrumentation() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        boolean showSourceDir = false;
        String targetPackage = null;
        while (true) {
            try {
                String opt = getNextArg();
                if (opt == null) {
                    List<InstrumentationInfo> list = this.mInterface.queryInstrumentation(targetPackage, 0).getList();
                    Collections.sort(list, new Comparator<InstrumentationInfo>() {
                        /* class com.android.server.pm.PackageManagerShellCommand.AnonymousClass2 */

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
                } else if (!((opt.hashCode() == 1497 && opt.equals("-f")) ? false : true)) {
                    showSourceDir = true;
                } else if (opt.charAt(0) != '-') {
                    targetPackage = opt;
                } else {
                    pw.println("Error: Unknown option: " + opt);
                    return -1;
                }
            } catch (RuntimeException ex) {
                pw.println("Error: " + ex.toString());
                return -1;
            }
        }
    }

    private int runListLibraries() throws RemoteException {
        String[] rawList;
        PrintWriter pw = getOutPrintWriter();
        List<String> list = new ArrayList<>();
        for (String str : this.mInterface.getSystemSharedLibraryNames()) {
            list.add(str);
        }
        Collections.sort(list, new Comparator<String>() {
            /* class com.android.server.pm.PackageManagerShellCommand.AnonymousClass3 */

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

    /* JADX INFO: Multiple debug info for r12v4 boolean: [D('userId' int), D('isApex' boolean)] */
    /* JADX INFO: Multiple debug info for r0v41 int: [D('listThirdParty' boolean), D('getFlags' int)] */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x0220  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x0241  */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x0247  */
    private int runListPackages(boolean showSourceDir) throws RemoteException {
        RuntimeException ex;
        boolean showSourceDir2;
        boolean listDisabled;
        String filter;
        List<PackageInfo> packages;
        int userId;
        boolean isEnabled;
        String opt;
        char c;
        PrintWriter pw = getOutPrintWriter();
        boolean showUid = false;
        boolean showVersionCode = false;
        boolean listApexOnly = false;
        int uid = -1;
        int userId2 = 0;
        boolean listThirdParty = false;
        boolean listInstaller = false;
        boolean listEnabled = false;
        boolean listSystem = false;
        boolean showSourceDir3 = showSourceDir;
        boolean listDisabled2 = false;
        int getFlags = 0;
        while (true) {
            try {
                String opt2 = getNextOption();
                if (opt2 != null) {
                    try {
                        switch (opt2.hashCode()) {
                            case -493830763:
                                opt = opt2;
                                if (opt.equals("--show-versioncode")) {
                                    c = '\n';
                                    break;
                                }
                                c = 65535;
                                break;
                            case 1446:
                                opt = opt2;
                                if (opt.equals("-3")) {
                                    c = '\t';
                                    break;
                                }
                                c = 65535;
                                break;
                            case 1480:
                                opt = opt2;
                                if (opt.equals("-U")) {
                                    c = 7;
                                    break;
                                }
                                c = 65535;
                                break;
                            case 1492:
                                opt = opt2;
                                if (opt.equals("-a")) {
                                    c = 2;
                                    break;
                                }
                                c = 65535;
                                break;
                            case 1495:
                                opt = opt2;
                                if (opt.equals("-d")) {
                                    c = 0;
                                    break;
                                }
                                c = 65535;
                                break;
                            case 1496:
                                opt = opt2;
                                if (opt.equals("-e")) {
                                    c = 1;
                                    break;
                                }
                                c = 65535;
                                break;
                            case 1497:
                                opt = opt2;
                                if (opt.equals("-f")) {
                                    c = 3;
                                    break;
                                }
                                c = 65535;
                                break;
                            case NetworkConstants.ETHER_MTU /* 1500 */:
                                opt = opt2;
                                if (opt.equals("-i")) {
                                    c = 4;
                                    break;
                                }
                                c = 65535;
                                break;
                            case 1503:
                                opt = opt2;
                                if (opt.equals("-l")) {
                                    c = 5;
                                    break;
                                }
                                c = 65535;
                                break;
                            case 1510:
                                opt = opt2;
                                if (opt.equals("-s")) {
                                    c = 6;
                                    break;
                                }
                                c = 65535;
                                break;
                            case 1512:
                                opt = opt2;
                                if (opt.equals("-u")) {
                                    c = '\b';
                                    break;
                                }
                                c = 65535;
                                break;
                            case 43014832:
                                opt = opt2;
                                if (opt.equals("--uid")) {
                                    c = '\r';
                                    break;
                                }
                                c = 65535;
                                break;
                            case 1333469547:
                                opt = opt2;
                                if (opt.equals("--user")) {
                                    c = '\f';
                                    break;
                                }
                                c = 65535;
                                break;
                            case 1809263575:
                                opt = opt2;
                                try {
                                    if (opt.equals("--apex-only")) {
                                        c = 11;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                } catch (RuntimeException e) {
                                    ex = e;
                                    pw.println("Error: " + ex.toString());
                                    return -1;
                                }
                            default:
                                opt = opt2;
                                c = 65535;
                                break;
                        }
                        switch (c) {
                            case 0:
                                listDisabled2 = true;
                                break;
                            case 1:
                                listEnabled = true;
                                break;
                            case 2:
                                getFlags = 4202496 | getFlags | 536870912;
                                break;
                            case 3:
                                showSourceDir3 = true;
                                break;
                            case 4:
                                listInstaller = true;
                                break;
                            case 5:
                                break;
                            case 6:
                                listSystem = true;
                                break;
                            case 7:
                                showUid = true;
                                break;
                            case '\b':
                                getFlags |= 8192;
                                break;
                            case '\t':
                                listThirdParty = true;
                                break;
                            case '\n':
                                showVersionCode = true;
                                break;
                            case 11:
                                listApexOnly = true;
                                getFlags = 1073741824 | getFlags;
                                break;
                            case '\f':
                                userId2 = UserHandle.parseUserArg(getNextArgRequired());
                                break;
                            case '\r':
                                showUid = true;
                                uid = Integer.parseInt(getNextArgRequired());
                                break;
                            default:
                                StringBuilder sb = new StringBuilder();
                                try {
                                    sb.append("Error: Unknown option: ");
                                    sb.append(opt);
                                    pw.println(sb.toString());
                                    return -1;
                                } catch (RuntimeException e2) {
                                    ex = e2;
                                    pw.println("Error: " + ex.toString());
                                    return -1;
                                }
                        }
                    } catch (RuntimeException e3) {
                        ex = e3;
                        pw.println("Error: " + ex.toString());
                        return -1;
                    }
                } else {
                    String filter2 = getNextArg();
                    List<PackageInfo> packages2 = this.mInterface.getInstalledPackages(getFlags, userId2).getList();
                    int count = packages2.size();
                    int p = 0;
                    while (p < count) {
                        PackageInfo info = packages2.get(p);
                        if (filter2 != null) {
                            userId = userId2;
                            if (!info.packageName.contains(filter2)) {
                                filter = filter2;
                                showSourceDir2 = showSourceDir3;
                                listDisabled = listDisabled2;
                                packages = packages2;
                                p++;
                                userId2 = userId;
                                count = count;
                                packages2 = packages;
                                filter2 = filter;
                                listDisabled2 = listDisabled;
                                showSourceDir3 = showSourceDir2;
                            }
                        } else {
                            userId = userId2;
                        }
                        boolean isApex = info.isApex;
                        packages = packages2;
                        if (uid == -1 || isApex || info.applicationInfo.uid == uid) {
                            boolean isSystem = !isApex && (info.applicationInfo.flags & 1) != 0;
                            if (!isApex) {
                                filter = filter2;
                                if (info.applicationInfo.enabled) {
                                    isEnabled = true;
                                    if ((listDisabled2 || !isEnabled) && ((!listEnabled || isEnabled) && ((!listSystem || isSystem) && ((!listThirdParty || !isSystem) && (!listApexOnly || isApex))))) {
                                        pw.print("package:");
                                        if (showSourceDir3 && !isApex) {
                                            pw.print(info.applicationInfo.sourceDir);
                                            pw.print("=");
                                        }
                                        pw.print(info.packageName);
                                        if (!showVersionCode) {
                                            pw.print(" versionCode:");
                                            if (info.applicationInfo != null) {
                                                showSourceDir2 = showSourceDir3;
                                                listDisabled = listDisabled2;
                                                pw.print(info.applicationInfo.longVersionCode);
                                            } else {
                                                showSourceDir2 = showSourceDir3;
                                                listDisabled = listDisabled2;
                                                pw.print(info.getLongVersionCode());
                                            }
                                        } else {
                                            showSourceDir2 = showSourceDir3;
                                            listDisabled = listDisabled2;
                                        }
                                        if (listInstaller) {
                                            pw.print("  installer=");
                                            pw.print(this.mInterface.getInstallerPackageName(info.packageName));
                                        }
                                        if (showUid && !isApex) {
                                            pw.print(" uid:");
                                            pw.print(info.applicationInfo.uid);
                                        }
                                        pw.println();
                                        p++;
                                        userId2 = userId;
                                        count = count;
                                        packages2 = packages;
                                        filter2 = filter;
                                        listDisabled2 = listDisabled;
                                        showSourceDir3 = showSourceDir2;
                                    } else {
                                        showSourceDir2 = showSourceDir3;
                                        listDisabled = listDisabled2;
                                        p++;
                                        userId2 = userId;
                                        count = count;
                                        packages2 = packages;
                                        filter2 = filter;
                                        listDisabled2 = listDisabled;
                                        showSourceDir3 = showSourceDir2;
                                    }
                                }
                            } else {
                                filter = filter2;
                            }
                            isEnabled = false;
                            if (listDisabled2) {
                            }
                            pw.print("package:");
                            pw.print(info.applicationInfo.sourceDir);
                            pw.print("=");
                            pw.print(info.packageName);
                            if (!showVersionCode) {
                            }
                            if (listInstaller) {
                            }
                            pw.print(" uid:");
                            pw.print(info.applicationInfo.uid);
                            pw.println();
                            p++;
                            userId2 = userId;
                            count = count;
                            packages2 = packages;
                            filter2 = filter;
                            listDisabled2 = listDisabled;
                            showSourceDir3 = showSourceDir2;
                        } else {
                            filter = filter2;
                            showSourceDir2 = showSourceDir3;
                            listDisabled = listDisabled2;
                            p++;
                            userId2 = userId;
                            count = count;
                            packages2 = packages;
                            filter2 = filter;
                            listDisabled2 = listDisabled;
                            showSourceDir3 = showSourceDir2;
                        }
                    }
                    return 0;
                }
            } catch (RuntimeException e4) {
                ex = e4;
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
        PrintWriter pw = getOutPrintWriter();
        boolean labels = false;
        boolean groups = false;
        boolean userOnly = false;
        boolean summary = false;
        boolean dangerousOnly = false;
        while (true) {
            String opt = getNextOption();
            if (opt != null) {
                char c = 65535;
                int hashCode = opt.hashCode();
                if (hashCode != 1495) {
                    if (hashCode != 1510) {
                        if (hashCode != 1512) {
                            if (hashCode != 1497) {
                                if (hashCode == 1498 && opt.equals("-g")) {
                                    c = 2;
                                }
                            } else if (opt.equals("-f")) {
                                c = 1;
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
                if (c == 0) {
                    dangerousOnly = true;
                } else if (c == 1) {
                    labels = true;
                } else if (c == 2) {
                    groups = true;
                } else if (c == 3) {
                    groups = true;
                    labels = true;
                    summary = true;
                } else if (c != 4) {
                    pw.println("Error: Unknown option: " + opt);
                    return 1;
                } else {
                    userOnly = true;
                }
            } else {
                ArrayList<String> groupList = new ArrayList<>();
                if (groups) {
                    List<PermissionGroupInfo> infos = this.mInterface.getAllPermissionGroups(0).getList();
                    int count = infos.size();
                    for (int i = 0; i < count; i++) {
                        groupList.add(infos.get(i).name);
                    }
                    groupList.add(null);
                } else {
                    groupList.add(getNextArg());
                }
                if (dangerousOnly) {
                    pw.println("Dangerous Permissions:");
                    pw.println("");
                    doListPermissions(groupList, groups, labels, summary, 1, 1);
                    if (!userOnly) {
                        return 0;
                    }
                    pw.println("Normal Permissions:");
                    pw.println("");
                    doListPermissions(groupList, groups, labels, summary, 0, 0);
                    return 0;
                } else if (userOnly) {
                    pw.println("Dangerous and Normal Permissions:");
                    pw.println("");
                    doListPermissions(groupList, groups, labels, summary, 0, 1);
                    return 0;
                } else {
                    pw.println("All Permissions:");
                    pw.println("");
                    doListPermissions(groupList, groups, labels, summary, -10000, 10000);
                    return 0;
                }
            }
        }
    }

    private Intent parseIntentAndUser() throws URISyntaxException {
        this.mTargetUser = -2;
        this.mBrief = false;
        this.mComponents = false;
        Intent intent = Intent.parseCommandArgs(this, new Intent.CommandOptionHandler() {
            /* class com.android.server.pm.PackageManagerShellCommand.AnonymousClass4 */

            public boolean handleOption(String opt, ShellCommand cmd) {
                if ("--user".equals(opt)) {
                    PackageManagerShellCommand.this.mTargetUser = UserHandle.parseUserArg(cmd.getNextArgRequired());
                    return true;
                } else if ("--brief".equals(opt)) {
                    PackageManagerShellCommand.this.mBrief = true;
                    return true;
                } else if ("--components".equals(opt)) {
                    PackageManagerShellCommand.this.mComponents = true;
                    return true;
                } else if (!"--query-flags".equals(opt)) {
                    return false;
                } else {
                    PackageManagerShellCommand.this.mQueryFlags = Integer.decode(cmd.getNextArgRequired()).intValue();
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
                ResolveInfo ri = this.mInterface.resolveIntent(intent, intent.getType(), this.mQueryFlags, this.mTargetUser);
                PrintWriter pw = getOutPrintWriter();
                if (ri == null) {
                    pw.println("No activity found");
                    return 0;
                }
                printResolveInfo(new PrintWriterPrinter(pw), "", ri, this.mBrief, this.mComponents);
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
                List<ResolveInfo> result = this.mInterface.queryIntentActivities(intent, intent.getType(), this.mQueryFlags, this.mTargetUser).getList();
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
                                printResolveInfo(pr2, "", result.get(i2), this.mBrief, this.mComponents);
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
                List<ResolveInfo> result = this.mInterface.queryIntentServices(intent, intent.getType(), this.mQueryFlags, this.mTargetUser).getList();
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
                                printResolveInfo(pr2, "", result.get(i2), this.mBrief, this.mComponents);
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
                List<ResolveInfo> result = this.mInterface.queryIntentReceivers(intent, intent.getType(), this.mQueryFlags, this.mTargetUser).getList();
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
                                printResolveInfo(pr2, "", result.get(i2), this.mBrief, this.mComponents);
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
        if (inPath == null) {
            try {
                if (params.sessionParams.sizeBytes == -1) {
                    pw.println("Error: must either specify a package size or an APK file");
                    return 1;
                }
            } finally {
                if (1 != 0) {
                    try {
                        doAbandonSession(sessionId, false);
                    } catch (Exception e) {
                    }
                }
            }
        }
        if (HwDeviceManager.disallowOp(6)) {
            pw.println("Failure [MDM_FORBID_ADB_INSTALL]");
            if (1 != 0) {
                try {
                    doAbandonSession(sessionId, false);
                } catch (Exception e2) {
                }
            }
            return 1;
        }
        boolean isApex = (params.sessionParams.installFlags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) != 0;
        StringBuilder sb = new StringBuilder();
        sb.append("base.");
        sb.append(isApex ? "apex" : "apk");
        if (doWriteSplit(sessionId, inPath, params.sessionParams.sizeBytes, sb.toString(), false) != 0) {
            if (1 != 0) {
                try {
                    doAbandonSession(sessionId, false);
                } catch (Exception e3) {
                }
            }
            return 1;
        } else if (doCommitSession(sessionId, false) != 0) {
            if (1 != 0) {
                try {
                    doAbandonSession(sessionId, false);
                } catch (Exception e4) {
                }
            }
            return 1;
        } else {
            pw.println("Success");
            if (0 != 0) {
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
            String opt = getNextOption();
            if (opt == null) {
                return doWriteSplit(Integer.parseInt(getNextArg()), getNextArg(), sizeBytes, getNextArg(), true);
            } else if (opt.equals("-S")) {
                sizeBytes = Long.parseLong(getNextArg());
            } else {
                throw new IllegalArgumentException("Unknown option: " + opt);
            }
        }
    }

    private int runInstallAddSession() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        int parentSessionId = Integer.parseInt(getNextArg());
        List<Integer> otherSessionIds = new ArrayList<>();
        while (true) {
            String opt = getNextArg();
            if (opt == null) {
                break;
            }
            otherSessionIds.add(Integer.valueOf(Integer.parseInt(opt)));
        }
        if (otherSessionIds.size() != 0) {
            return doInstallAddSession(parentSessionId, ArrayUtils.convertToIntArray(otherSessionIds), true);
        }
        pw.println("Error: At least two sessions are required.");
        return 1;
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

    /* JADX INFO: Multiple debug info for r0v16 int: [D('waitTillComplete' boolean), D('installFlags' int)] */
    private int runInstallExisting() throws RemoteException {
        AndroidException e;
        PrintWriter pw = getOutPrintWriter();
        boolean waitTillComplete = false;
        int installFlags = 4194304;
        int userId = 0;
        while (true) {
            String opt = getNextOption();
            if (opt != null) {
                char c = 65535;
                switch (opt.hashCode()) {
                    case -951415743:
                        if (opt.equals("--instant")) {
                            c = 2;
                            break;
                        }
                        break;
                    case 1051781117:
                        if (opt.equals("--ephemeral")) {
                            c = 1;
                            break;
                        }
                        break;
                    case 1333024815:
                        if (opt.equals("--full")) {
                            c = 3;
                            break;
                        }
                        break;
                    case 1333469547:
                        if (opt.equals("--user")) {
                            c = 0;
                            break;
                        }
                        break;
                    case 1333511957:
                        if (opt.equals("--wait")) {
                            c = 4;
                            break;
                        }
                        break;
                    case 1494514835:
                        if (opt.equals("--restrict-permissions")) {
                            c = 5;
                            break;
                        }
                        break;
                }
                if (c == 0) {
                    userId = UserHandle.parseUserArg(getNextArgRequired());
                } else if (c == 1 || c == 2) {
                    installFlags = (installFlags | 2048) & -16385;
                } else if (c == 3) {
                    installFlags = (installFlags & -2049) | DumpState.DUMP_KEYSETS;
                } else if (c == 4) {
                    waitTillComplete = true;
                } else if (c != 5) {
                    pw.println("Error: Unknown option: " + opt);
                    return 1;
                } else {
                    installFlags = -4194305 & installFlags;
                }
            } else {
                String packageName = getNextArg();
                if (packageName == null) {
                    pw.println("Error: package name not specified");
                    return 1;
                } else if (waitTillComplete) {
                    try {
                        LocalIntentReceiver receiver = new LocalIntentReceiver();
                        IPackageInstaller installer = this.mInterface.getPackageInstaller();
                        pw.println("Installing package " + packageName + " for user: " + userId);
                        try {
                            installer.installExistingPackage(packageName, installFlags, 0, receiver.getIntentSender(), userId, (List) null);
                            int status = receiver.getResult().getIntExtra("android.content.pm.extra.STATUS", 1);
                            pw.println("Received intent for package install");
                            return status == 0 ? 0 : 1;
                        } catch (PackageManager.NameNotFoundException | RemoteException e2) {
                            e = e2;
                            pw.println(e.toString());
                            return 1;
                        }
                    } catch (PackageManager.NameNotFoundException | RemoteException e3) {
                        e = e3;
                        pw.println(e.toString());
                        return 1;
                    }
                } else if (this.mInterface.installExistingPackageAsUser(packageName, userId, installFlags, 0, (List) null) != -3) {
                    try {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Package ");
                        try {
                            sb.append(packageName);
                            sb.append(" installed for user: ");
                            sb.append(userId);
                            pw.println(sb.toString());
                            return 0;
                        } catch (PackageManager.NameNotFoundException | RemoteException e4) {
                            e = e4;
                            pw.println(e.toString());
                            return 1;
                        }
                    } catch (PackageManager.NameNotFoundException | RemoteException e5) {
                        e = e5;
                        pw.println(e.toString());
                        return 1;
                    }
                } else {
                    throw new PackageManager.NameNotFoundException("Package " + packageName + " doesn't exist");
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

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private int runCompile() throws RemoteException {
        String compilerFilter;
        List<String> packageNames;
        String clearProfileData;
        boolean allPackages;
        int index;
        String opt;
        boolean result;
        List<String> failedPackages;
        boolean z;
        int reason;
        char c;
        PackageManagerShellCommand packageManagerShellCommand = this;
        PrintWriter pw = getOutPrintWriter();
        boolean checkProfiles = SystemProperties.getBoolean("dalvik.vm.usejitprofiles", false);
        boolean clearProfileData2 = false;
        boolean allPackages2 = false;
        String compilationReason = null;
        String compilerFilter2 = null;
        String compilationReason2 = null;
        String checkProfilesRaw = null;
        boolean secondaryDex = false;
        String split = null;
        boolean compileLayouts = false;
        while (true) {
            String nextOption = getNextOption();
            String opt2 = nextOption;
            if (nextOption != null) {
                switch (opt2.hashCode()) {
                    case -1615291473:
                        if (opt2.equals("--reset")) {
                            c = 7;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1614046854:
                        if (opt2.equals("--split")) {
                            c = '\t';
                            break;
                        }
                        c = 65535;
                        break;
                    case 1492:
                        if (opt2.equals("-a")) {
                            c = 0;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1494:
                        if (opt2.equals("-c")) {
                            c = 1;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1497:
                        if (opt2.equals("-f")) {
                            c = 2;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1504:
                        if (opt2.equals("-m")) {
                            c = 3;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1509:
                        if (opt2.equals("-r")) {
                            c = 4;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1269477022:
                        if (opt2.equals("--secondary-dex")) {
                            c = '\b';
                            break;
                        }
                        c = 65535;
                        break;
                    case 1323879247:
                        if (opt2.equals("--compile-layouts")) {
                            c = 5;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1690714782:
                        if (opt2.equals("--check-prof")) {
                            c = 6;
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
                        allPackages2 = true;
                        break;
                    case 1:
                        compilationReason = 1;
                        break;
                    case 2:
                        clearProfileData2 = true;
                        break;
                    case 3:
                        compilerFilter2 = getNextArgRequired();
                        break;
                    case 4:
                        compilationReason2 = getNextArgRequired();
                        break;
                    case 5:
                        compileLayouts = true;
                        break;
                    case 6:
                        checkProfilesRaw = getNextArgRequired();
                        break;
                    case 7:
                        compilationReason2 = "install";
                        compilationReason = 1;
                        clearProfileData2 = true;
                        break;
                    case '\b':
                        secondaryDex = true;
                        break;
                    case '\t':
                        split = getNextArgRequired();
                        break;
                    default:
                        pw.println("Error: Unknown option: " + opt2);
                        return 1;
                }
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
                boolean compilerFilterGiven = compilerFilter2 != null;
                boolean compilationReasonGiven = compilationReason2 != null;
                if (!(compilerFilterGiven || compilationReasonGiven || compileLayouts) || ((!compilerFilterGiven && compilationReasonGiven && compileLayouts) || ((compilerFilterGiven && !compilationReasonGiven && compileLayouts) || ((compilerFilterGiven && compilationReasonGiven && !compileLayouts) || (compilerFilterGiven && compilationReasonGiven && compileLayouts))))) {
                    pw.println("Must specify exactly one of compilation filter (\"-m\"), compilation reason (\"-r\"), or compile layouts (\"--compile-layouts\")");
                    return 1;
                } else if (allPackages2 && split != null) {
                    pw.println("-a cannot be specified together with --split");
                    return 1;
                } else if (!secondaryDex || split == null) {
                    String targetCompilerFilter = null;
                    if (compilerFilterGiven) {
                        if (!DexFile.isValidCompilerFilter(compilerFilter2)) {
                            pw.println("Error: \"" + compilerFilter2 + "\" is not a valid compilation filter.");
                            return 1;
                        }
                        targetCompilerFilter = compilerFilter2;
                    }
                    if (compilationReasonGiven) {
                        int reason2 = -1;
                        int i = 0;
                        while (true) {
                            if (i >= PackageManagerServiceCompilerMapping.REASON_STRINGS.length) {
                                reason = reason2;
                            } else if (PackageManagerServiceCompilerMapping.REASON_STRINGS[i].equals(compilationReason2)) {
                                reason = i;
                            } else {
                                i++;
                                reason2 = reason2;
                            }
                        }
                        if (reason == -1) {
                            pw.println("Error: Unknown compilation reason: " + compilationReason2);
                            return 1;
                        }
                        compilerFilter = PackageManagerServiceCompilerMapping.getCompilerFilterForReason(reason);
                    } else {
                        compilerFilter = targetCompilerFilter;
                    }
                    if (allPackages2) {
                        packageNames = packageManagerShellCommand.mInterface.getAllPackages();
                    } else {
                        String packageName = getNextArg();
                        if (packageName == null) {
                            pw.println("Error: package name not specified");
                            return 1;
                        }
                        packageNames = Collections.singletonList(packageName);
                    }
                    List<String> failedPackages2 = new ArrayList<>();
                    int index2 = 0;
                    for (String packageName2 : packageNames) {
                        if (compilationReason != null) {
                            clearProfileData = compilationReason;
                            packageManagerShellCommand.mInterface.clearApplicationProfileData(packageName2);
                        } else {
                            clearProfileData = compilationReason;
                        }
                        if (allPackages2) {
                            StringBuilder sb = new StringBuilder();
                            int index3 = index2 + 1;
                            sb.append(index3);
                            allPackages = allPackages2;
                            sb.append(SliceClientPermissions.SliceAuthority.DELIMITER);
                            sb.append(packageNames.size());
                            sb.append(": ");
                            sb.append(packageName2);
                            pw.println(sb.toString());
                            pw.flush();
                            index = index3;
                        } else {
                            allPackages = allPackages2;
                            index = index2;
                        }
                        if (compileLayouts) {
                            result = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).compileLayouts(packageName2);
                            failedPackages = failedPackages2;
                            opt = opt2;
                        } else {
                            if (secondaryDex) {
                                z = packageManagerShellCommand.mInterface.performDexOptSecondary(packageName2, compilerFilter, clearProfileData2);
                                failedPackages = failedPackages2;
                                opt = opt2;
                            } else {
                                IPackageManager iPackageManager = packageManagerShellCommand.mInterface;
                                failedPackages = failedPackages2;
                                opt = opt2;
                                z = iPackageManager.performDexOptMode(packageName2, checkProfiles, compilerFilter, clearProfileData2, true, split);
                            }
                            result = z;
                        }
                        if (!result) {
                            failedPackages.add(packageName2);
                        }
                        failedPackages2 = failedPackages;
                        index2 = index;
                        compilationReason2 = compilationReason2;
                        compilationReason = clearProfileData;
                        allPackages2 = allPackages;
                        opt2 = opt;
                        packageManagerShellCommand = this;
                    }
                    if (failedPackages2.isEmpty()) {
                        pw.println("Success");
                        return 0;
                    } else if (failedPackages2.size() == 1) {
                        pw.println("Failure: package " + failedPackages2.get(0) + " could not be compiled");
                        return 1;
                    } else {
                        pw.print("Failure: the following packages could not be compiled: ");
                        boolean is_first = true;
                        for (String packageName3 : failedPackages2) {
                            if (is_first) {
                                is_first = false;
                            } else {
                                pw.print(", ");
                            }
                            pw.print(packageName3);
                        }
                        pw.println();
                        return 1;
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
        List<String> list;
        List<String> packageNames = new ArrayList<>();
        while (true) {
            String arg = getNextArg();
            if (arg == null) {
                break;
            }
            packageNames.add(arg);
        }
        IPackageManager iPackageManager = this.mInterface;
        if (packageNames.isEmpty()) {
            list = null;
        } else {
            list = packageNames;
        }
        boolean result = iPackageManager.runBackgroundDexoptJob(list);
        getOutPrintWriter().println(result ? "Success" : "Failure");
        return result ? 0 : -1;
    }

    private int runDumpProfiles() throws RemoteException {
        this.mInterface.dumpProfiles(getNextArg());
        return 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:61:0x012f, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0130, code lost:
        $closeResource(r0, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0134, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0138, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0139, code lost:
        $closeResource(r0, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x013d, code lost:
        throw r0;
     */
    private int runSnapshotProfile() throws RemoteException {
        String baseCodePath;
        String codePath;
        String outputFileSuffix;
        PrintWriter pw = getOutPrintWriter();
        String packageName = getNextArg();
        boolean isBootImage = PackageManagerService.PLATFORM_PACKAGE_NAME.equals(packageName);
        String codePath2 = null;
        while (true) {
            String opt = getNextArg();
            boolean z = false;
            if (opt != null) {
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
                    codePath2 = getNextArg();
                }
            } else {
                if (!isBootImage) {
                    PackageInfo packageInfo = this.mInterface.getPackageInfo(packageName, 0, 0);
                    if (packageInfo == null) {
                        pw.write("Package not found " + packageName);
                        return -1;
                    }
                    String baseCodePath2 = packageInfo.applicationInfo.getBaseCodePath();
                    if (codePath2 == null) {
                        codePath = baseCodePath2;
                        baseCodePath = baseCodePath2;
                    } else {
                        codePath = codePath2;
                        baseCodePath = baseCodePath2;
                    }
                } else {
                    codePath = codePath2;
                    baseCodePath = null;
                }
                SnapshotRuntimeProfileCallback callback = new SnapshotRuntimeProfileCallback();
                String callingPackage = Binder.getCallingUid() == 0 ? "root" : "com.android.shell";
                int profileType = isBootImage ? 1 : 0;
                if (!this.mInterface.getArtManager().isRuntimeProfilingEnabled(profileType, callingPackage)) {
                    pw.println("Error: Runtime profiling is not enabled");
                    return -1;
                }
                this.mInterface.getArtManager().snapshotRuntimeProfile(profileType, packageName, codePath, callback, callingPackage);
                if (!callback.waitTillDone()) {
                    pw.println("Error: callback not called");
                    return callback.mErrCode;
                }
                InputStream inStream = new ParcelFileDescriptor.AutoCloseInputStream(callback.mProfileReadFd);
                if (!isBootImage) {
                    if (!Objects.equals(baseCodePath, codePath)) {
                        outputFileSuffix = STDIN_PATH + new File(codePath).getName();
                        String outputProfilePath = ART_PROFILE_SNAPSHOT_DEBUG_LOCATION + packageName + outputFileSuffix + ".prof";
                        OutputStream outStream = new FileOutputStream(outputProfilePath);
                        Streams.copy(inStream, outStream);
                        $closeResource(null, outStream);
                        Os.chmod(outputProfilePath, 420);
                        $closeResource(null, inStream);
                        return 0;
                    }
                }
                outputFileSuffix = "";
                String outputProfilePath2 = ART_PROFILE_SNAPSHOT_DEBUG_LOCATION + packageName + outputFileSuffix + ".prof";
                OutputStream outStream2 = new FileOutputStream(outputProfilePath2);
                Streams.copy(inStream, outStream2);
                $closeResource(null, outStream2);
                Os.chmod(outputProfilePath2, 420);
                try {
                    $closeResource(null, inStream);
                    return 0;
                } catch (ErrnoException | IOException e) {
                    pw.println("Error when reading the profile fd: " + e.getMessage());
                    e.printStackTrace(pw);
                    return -1;
                }
            }
        }
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

    /* access modifiers changed from: private */
    public static class SnapshotRuntimeProfileCallback extends ISnapshotRuntimeProfileCallback.Stub {
        private CountDownLatch mDoneSignal;
        private int mErrCode;
        private ParcelFileDescriptor mProfileReadFd;
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
            return done && this.mSuccess;
        }
    }

    private int runUninstall() throws RemoteException {
        String str;
        PrintWriter pw = getOutPrintWriter();
        int flags = 0;
        int userId = -1;
        long versionCode = -1;
        while (true) {
            String opt = getNextOption();
            char c = 65535;
            if (opt != null) {
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
                if (c == 0) {
                    flags |= 1;
                } else if (c == 1) {
                    userId = UserHandle.parseUserArg(getNextArgRequired());
                } else if (c != 2) {
                    pw.println("Error: Unknown option: " + opt);
                    return 1;
                } else {
                    versionCode = Long.parseLong(getNextArgRequired());
                }
            } else {
                String packageName = getNextArg();
                if (packageName == null) {
                    pw.println("Error: package name not specified");
                    return 1;
                }
                String splitName = getNextArg();
                if (splitName == null) {
                    int userId2 = translateUserId(userId, true, "runUninstall");
                    LocalIntentReceiver receiver = new LocalIntentReceiver();
                    PackageManagerInternal internal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
                    if (internal.isApexPackage(packageName)) {
                        str = "]";
                        internal.uninstallApex(packageName, versionCode, userId2, receiver.getIntentSender());
                    } else {
                        str = "]";
                        if (userId2 == -1) {
                            userId2 = 0;
                            if (isInRepairMode()) {
                                userId2 = REPAIR_MODE_USER_ID;
                                if (this.mInterface.getPackageInfo(packageName, (int) DumpState.DUMP_HANDLE, (int) REPAIR_MODE_USER_ID) == null) {
                                    pw.println("Failure [not installed for REPAIR MODE: " + REPAIR_MODE_USER_ID + str);
                                    return 1;
                                }
                            }
                            flags |= 2;
                        } else {
                            PackageInfo info = this.mInterface.getPackageInfo(packageName, (int) DumpState.DUMP_HANDLE, userId2);
                            if (info == null) {
                                pw.println("Failure [not installed for " + userId2 + str);
                                return 1;
                            }
                            if ((info.applicationInfo.flags & 1) != 0) {
                                flags |= 4;
                            }
                        }
                        this.mInterface.getPackageInstaller().uninstall(new VersionedPackage(packageName, versionCode), (String) null, flags, receiver.getIntentSender(), userId2);
                    }
                    Intent result = receiver.getResult();
                    if (result.getIntExtra("android.content.pm.extra.STATUS", 1) == 0) {
                        pw.println("Success");
                        return 0;
                    }
                    pw.println("Failure [" + result.getStringExtra("android.content.pm.extra.STATUS_MESSAGE") + str);
                    return 1;
                } else if (!HwPackageManagerServiceUtils.isForbidUninstallSplitPlugin(packageName, splitName)) {
                    return runRemoveSplit(packageName, splitName);
                } else {
                    pw.println("Error: Forbid uninstalling plugins: " + splitName);
                    return 1;
                }
            }
        }
    }

    private int runRemoveSplit(String packageName, String splitName) throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(2);
        sessionParams.installFlags = 2 | sessionParams.installFlags;
        sessionParams.appPackageName = packageName;
        int sessionId = doCreateSession(sessionParams, null, -1);
        try {
            if (doRemoveSplit(sessionId, splitName, false) != 0) {
                return 1;
            }
            if (doCommitSession(sessionId, false) != 0) {
                if (1 != 0) {
                    try {
                        doAbandonSession(sessionId, false);
                    } catch (Exception e) {
                    }
                }
                return 1;
            }
            pw.println("Success");
            if (0 != 0) {
                try {
                    doAbandonSession(sessionId, false);
                } catch (Exception e2) {
                }
            }
            return 0;
        } finally {
            if (1 != 0) {
                try {
                    doAbandonSession(sessionId, false);
                } catch (Exception e3) {
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class ClearDataObserver extends IPackageDataObserver.Stub {
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
        if (state == 0) {
            return BatteryService.HealthServiceWrapper.INSTANCE_VENDOR;
        }
        if (state == 1) {
            return "enabled";
        }
        if (state == 2) {
            return "disabled";
        }
        if (state == 3) {
            return "disabled-user";
        }
        if (state != 4) {
            return UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
        }
        return "disabled-until-used";
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

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x002c, code lost:
        if (r0.equals("--user") != false) goto L_0x0076;
     */
    private int runSuspend(boolean suspendedState) {
        SuspendDialogInfo info;
        Exception e;
        PrintWriter pw = getOutPrintWriter();
        String dialogMessage = null;
        PersistableBundle appExtras = new PersistableBundle();
        PersistableBundle launcherExtras = new PersistableBundle();
        int userId = 0;
        while (true) {
            String opt = getNextOption();
            char c = 0;
            if (opt != null) {
                switch (opt.hashCode()) {
                    case -39471105:
                        if (opt.equals("--dialogMessage")) {
                            c = 1;
                            break;
                        }
                        c = 65535;
                        break;
                    case 42995488:
                        if (opt.equals("--aed")) {
                            c = 4;
                            break;
                        }
                        c = 65535;
                        break;
                    case 42995496:
                        if (opt.equals("--ael")) {
                            c = 2;
                            break;
                        }
                        c = 65535;
                        break;
                    case 42995503:
                        if (opt.equals("--aes")) {
                            c = 3;
                            break;
                        }
                        c = 65535;
                        break;
                    case 43006059:
                        if (opt.equals("--led")) {
                            c = 7;
                            break;
                        }
                        c = 65535;
                        break;
                    case 43006067:
                        if (opt.equals("--lel")) {
                            c = 5;
                            break;
                        }
                        c = 65535;
                        break;
                    case 43006074:
                        if (opt.equals("--les")) {
                            c = 6;
                            break;
                        }
                        c = 65535;
                        break;
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
                String callingPackage = Binder.getCallingUid() == 0 ? "root" : "com.android.shell";
                if (!TextUtils.isEmpty(dialogMessage)) {
                    info = new SuspendDialogInfo.Builder().setMessage(dialogMessage).build();
                } else {
                    info = null;
                }
                try {
                    try {
                        this.mInterface.setPackagesSuspendedAsUser(new String[]{packageName}, suspendedState, appExtras, launcherExtras, info, callingPackage, userId);
                        pw.println("Package " + packageName + " new suspended state: " + this.mInterface.isPackageSuspendedForUser(packageName, userId));
                        return 0;
                    } catch (RemoteException | IllegalArgumentException e2) {
                        e = e2;
                        pw.println(e.toString());
                        return 1;
                    }
                } catch (RemoteException | IllegalArgumentException e3) {
                    e = e3;
                    pw.println(e.toString());
                    return 1;
                }
            }
        }
    }

    private int runGrantRevokePermission(boolean grant) throws RemoteException {
        int userId = 0;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
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
        } else if (grant) {
            this.mInterface.grantRuntimePermission(pkg, perm, userId);
            return 0;
        } else {
            this.mInterface.revokeRuntimePermission(pkg, perm, userId);
            return 0;
        }
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
        try {
            PackageInfo info = this.mInterface.getPackageInfo(pkg, 0, 0);
            if (info == null || !info.applicationInfo.isVendor()) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    private boolean isProductApp(String pkg) {
        try {
            PackageInfo info = this.mInterface.getPackageInfo(pkg, 0, 0);
            if (info == null || !info.applicationInfo.isProduct()) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    private boolean isProductServicesApp(String pkg) {
        try {
            PackageInfo info = this.mInterface.getPackageInfo(pkg, 0, 0);
            if (info == null || !info.applicationInfo.isProductServices()) {
                return false;
            }
            return true;
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
        } else if (isProductServicesApp(pkg)) {
            privAppPermissions = SystemConfig.getInstance().getProductServicesPrivAppPermissions(pkg);
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
        } else if (isProductServicesApp(pkg)) {
            privAppPermissions = SystemConfig.getInstance().getProductServicesPrivAppDenyPermissions(pkg);
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
            return 0;
        }
        oemPermissions.forEach(new BiConsumer() {
            /* class com.android.server.pm.$$Lambda$PackageManagerShellCommand$OZpz58K2HXVuHDuVYKnCu6oo4c */

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                PackageManagerShellCommand.this.lambda$runGetOemPermissions$0$PackageManagerShellCommand((String) obj, (Boolean) obj2);
            }
        });
        return 0;
    }

    public /* synthetic */ void lambda$runGetOemPermissions$0$PackageManagerShellCommand(String permission, Boolean granted) {
        PrintWriter outPrintWriter = getOutPrintWriter();
        outPrintWriter.println(permission + " granted:" + granted);
    }

    private String linkStateToString(int state) {
        if (state == 0) {
            return "undefined";
        }
        if (state == 1) {
            return "ask";
        }
        if (state == 2) {
            return "always";
        }
        if (state == 3) {
            return "never";
        }
        if (state == 4) {
            return "always ask";
        }
        return "Unknown link state: " + state;
    }

    private int runSetAppLink() throws RemoteException {
        int newMode;
        int userId = 0;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
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
                char c = 65535;
                switch (lowerCase.hashCode()) {
                    case -1414557169:
                        if (lowerCase.equals("always")) {
                            c = 1;
                            break;
                        }
                        break;
                    case -1038130864:
                        if (lowerCase.equals("undefined")) {
                            c = 0;
                            break;
                        }
                        break;
                    case 96889:
                        if (lowerCase.equals("ask")) {
                            c = 2;
                            break;
                        }
                        break;
                    case 104712844:
                        if (lowerCase.equals("never")) {
                            c = 4;
                            break;
                        }
                        break;
                    case 1182785979:
                        if (lowerCase.equals("always-ask")) {
                            c = 3;
                            break;
                        }
                        break;
                }
                if (c == 0) {
                    newMode = 0;
                } else if (c == 1) {
                    newMode = 2;
                } else if (c == 2) {
                    newMode = 1;
                } else if (c == 3) {
                    newMode = 4;
                } else if (c != 4) {
                    getErrPrintWriter().println("Error: unknown app link state '" + modeString + "'");
                    return 1;
                } else {
                    newMode = 3;
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
            String opt = getNextOption();
            if (opt == null) {
                String pkg = getNextArg();
                if (pkg == null) {
                    getErrPrintWriter().println("Error: no package specified.");
                    return 1;
                }
                PackageInfo info = this.mInterface.getPackageInfo(pkg, 0, userId);
                if (info == null) {
                    PrintWriter errPrintWriter = getErrPrintWriter();
                    errPrintWriter.println("Error: package " + pkg + " not found.");
                    return 1;
                } else if ((info.applicationInfo.privateFlags & 16) == 0) {
                    PrintWriter errPrintWriter2 = getErrPrintWriter();
                    errPrintWriter2.println("Error: package " + pkg + " does not handle web links.");
                    return 1;
                } else {
                    getOutPrintWriter().println(linkStateToString(this.mInterface.getIntentVerificationStatus(pkg, userId)));
                    return 0;
                }
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                PrintWriter errPrintWriter3 = getErrPrintWriter();
                errPrintWriter3.println("Error: unknown option: " + opt);
                return 1;
            }
        }
    }

    private int runTrimCaches() throws RemoteException {
        long multiplier;
        String size;
        String volumeUuid;
        long multiplier2;
        String size2 = getNextArg();
        if (size2 == null) {
            getErrPrintWriter().println("Error: no size specified");
            return 1;
        }
        int len = size2.length();
        char c = size2.charAt(len - 1);
        if (c < '0' || c > '9') {
            if (c == 'K' || c == 'k') {
                multiplier2 = 1024;
            } else if (c == 'M' || c == 'm') {
                multiplier2 = 1048576;
            } else if (c == 'G' || c == 'g') {
                multiplier2 = 1073741824;
            } else {
                getErrPrintWriter().println("Invalid suffix: " + c);
                return 1;
            }
            multiplier = multiplier2;
            size = size2.substring(0, len - 1);
        } else {
            multiplier = 1;
            size = size2;
        }
        try {
            long sizeVal = Long.parseLong(size) * multiplier;
            String volumeUuid2 = getNextArg();
            if ("internal".equals(volumeUuid2)) {
                volumeUuid = null;
            } else {
                volumeUuid = volumeUuid2;
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
            getErrPrintWriter().println("Error: expected number at: " + size);
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
            String opt = getNextOption();
            if (opt == null) {
                String arg = getNextArg();
                if (arg == null) {
                    getErrPrintWriter().println("Error: no user name specified.");
                    return 1;
                }
                IUserManager um = IUserManager.Stub.asInterface(ServiceManager.getService("user"));
                IAccountManager accm = IAccountManager.Stub.asInterface(ServiceManager.getService("account"));
                if ((flags & 8) != 0) {
                    int parentUserId = userId >= 0 ? userId : 0;
                    info = um.createRestrictedProfile(arg, parentUserId);
                    accm.addSharedAccountsFromParentUser(parentUserId, userId, Process.myUid() == 0 ? "root" : "com.android.shell");
                } else if (userId < 0) {
                    info = um.createUser(arg, flags);
                } else {
                    info = um.createProfileForUser(arg, flags, userId, (String[]) null);
                }
                if (info != null) {
                    PrintWriter outPrintWriter = getOutPrintWriter();
                    outPrintWriter.println("Success: created user id " + info.id);
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
                PrintWriter errPrintWriter = getErrPrintWriter();
                errPrintWriter.println("Error: unknown option " + opt);
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

    /* access modifiers changed from: private */
    public static class InstallParams {
        String installerPackageName;
        PackageInstaller.SessionParams sessionParams;
        int userId;

        private InstallParams() {
            this.userId = -1;
        }
    }

    private InstallParams makeInstallParams() {
        PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(1);
        InstallParams params = new InstallParams();
        params.sessionParams = sessionParams;
        sessionParams.installFlags |= DumpState.DUMP_CHANGES;
        boolean replaceExisting = true;
        while (true) {
            String opt = getNextOption();
            if (opt != null) {
                char c = 65535;
                switch (opt.hashCode()) {
                    case -2091380650:
                        if (opt.equals("--install-reason")) {
                            c = 22;
                            break;
                        }
                        break;
                    case -1950997763:
                        if (opt.equals("--force-uuid")) {
                            c = 23;
                            break;
                        }
                        break;
                    case -1777984902:
                        if (opt.equals("--dont-kill")) {
                            c = '\b';
                            break;
                        }
                        break;
                    case -1624001065:
                        if (opt.equals("--hwhdb")) {
                            c = 28;
                            break;
                        }
                        break;
                    case -1313152697:
                        if (opt.equals("--install-location")) {
                            c = 21;
                            break;
                        }
                        break;
                    case -1137116608:
                        if (opt.equals("--instantapp")) {
                            c = 17;
                            break;
                        }
                        break;
                    case -951415743:
                        if (opt.equals("--instant")) {
                            c = 16;
                            break;
                        }
                        break;
                    case -706813505:
                        if (opt.equals("--referrer")) {
                            c = '\n';
                            break;
                        }
                        break;
                    case -653924786:
                        if (opt.equals("--enable-rollback")) {
                            c = 29;
                            break;
                        }
                        break;
                    case -170474990:
                        if (opt.equals("--multi-package")) {
                            c = 26;
                            break;
                        }
                        break;
                    case 1477:
                        if (opt.equals("-R")) {
                            c = 1;
                            break;
                        }
                        break;
                    case 1478:
                        if (opt.equals("-S")) {
                            c = '\r';
                            break;
                        }
                        break;
                    case 1495:
                        if (opt.equals("-d")) {
                            c = 5;
                            break;
                        }
                        break;
                    case 1497:
                        if (opt.equals("-f")) {
                            c = 4;
                            break;
                        }
                        break;
                    case 1498:
                        if (opt.equals("-g")) {
                            c = 6;
                            break;
                        }
                        break;
                    case NetworkConstants.ETHER_MTU /* 1500 */:
                        if (opt.equals("-i")) {
                            c = 2;
                            break;
                        }
                        break;
                    case 1507:
                        if (opt.equals("-p")) {
                            c = 11;
                            break;
                        }
                        break;
                    case 1509:
                        if (opt.equals("-r")) {
                            c = 0;
                            break;
                        }
                        break;
                    case 1511:
                        if (opt.equals("-t")) {
                            c = 3;
                            break;
                        }
                        break;
                    case 42995400:
                        if (opt.equals("--abi")) {
                            c = 14;
                            break;
                        }
                        break;
                    case 43010092:
                        if (opt.equals("--pkg")) {
                            c = '\f';
                            break;
                        }
                        break;
                    case 148207464:
                        if (opt.equals("--originating-uri")) {
                            c = '\t';
                            break;
                        }
                        break;
                    case 1051781117:
                        if (opt.equals("--ephemeral")) {
                            c = 15;
                            break;
                        }
                        break;
                    case 1067504745:
                        if (opt.equals("--preload")) {
                            c = 19;
                            break;
                        }
                        break;
                    case 1332870850:
                        if (opt.equals("--apex")) {
                            c = 25;
                            break;
                        }
                        break;
                    case 1333024815:
                        if (opt.equals("--full")) {
                            c = 18;
                            break;
                        }
                        break;
                    case 1333469547:
                        if (opt.equals("--user")) {
                            c = 20;
                            break;
                        }
                        break;
                    case 1494514835:
                        if (opt.equals("--restrict-permissions")) {
                            c = 7;
                            break;
                        }
                        break;
                    case 1507519174:
                        if (opt.equals("--staged")) {
                            c = 27;
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
                    case 24:
                        break;
                    case 1:
                        replaceExisting = false;
                        break;
                    case 2:
                        params.installerPackageName = getNextArg();
                        if (params.installerPackageName != null) {
                            break;
                        } else {
                            throw new IllegalArgumentException("Missing installer package");
                        }
                    case 3:
                        sessionParams.installFlags |= 4;
                        break;
                    case 4:
                        sessionParams.installFlags |= 16;
                        break;
                    case 5:
                        sessionParams.installFlags |= 128;
                        break;
                    case 6:
                        sessionParams.installFlags |= 256;
                        break;
                    case 7:
                        sessionParams.installFlags &= -4194305;
                        break;
                    case '\b':
                        sessionParams.installFlags |= 4096;
                        break;
                    case '\t':
                        sessionParams.originatingUri = Uri.parse(getNextArg());
                        break;
                    case '\n':
                        sessionParams.referrerUri = Uri.parse(getNextArg());
                        break;
                    case 11:
                        sessionParams.mode = 2;
                        sessionParams.appPackageName = getNextArg();
                        if (sessionParams.appPackageName != null) {
                            break;
                        } else {
                            throw new IllegalArgumentException("Missing inherit package name");
                        }
                    case '\f':
                        sessionParams.appPackageName = getNextArg();
                        if (sessionParams.appPackageName != null) {
                            break;
                        } else {
                            throw new IllegalArgumentException("Missing package name");
                        }
                    case '\r':
                        long sizeBytes = Long.parseLong(getNextArg());
                        if (sizeBytes > 0) {
                            sessionParams.setSize(sizeBytes);
                            break;
                        } else {
                            throw new IllegalArgumentException("Size must be positive");
                        }
                    case 14:
                        sessionParams.abiOverride = checkAbiArgument(getNextArg());
                        break;
                    case 15:
                    case 16:
                    case 17:
                        sessionParams.setInstallAsInstantApp(true);
                        break;
                    case 18:
                        sessionParams.setInstallAsInstantApp(false);
                        break;
                    case FaceAcquiredInfo.FACE_OBSCURED /* 19 */:
                        sessionParams.setInstallAsVirtualPreload();
                        break;
                    case 20:
                        params.userId = UserHandle.parseUserArg(getNextArgRequired());
                        break;
                    case 21:
                        sessionParams.installLocation = Integer.parseInt(getNextArg());
                        break;
                    case FaceAcquiredInfo.VENDOR /* 22 */:
                        sessionParams.installReason = Integer.parseInt(getNextArg());
                        break;
                    case 23:
                        sessionParams.installFlags |= 512;
                        sessionParams.volumeUuid = getNextArg();
                        if (!"internal".equals(sessionParams.volumeUuid)) {
                            break;
                        } else {
                            sessionParams.volumeUuid = null;
                            break;
                        }
                    case 25:
                        sessionParams.setInstallAsApex();
                        sessionParams.setStaged();
                        break;
                    case TrustedUIService.TUI_POLL_FOLD /* 26 */:
                        sessionParams.setMultiPackage();
                        break;
                    case 27:
                        sessionParams.setStaged();
                        break;
                    case 28:
                        params.sessionParams.hdbEncode = getNextArg();
                        params.sessionParams.hdbArgIndex = getArgPos();
                        params.sessionParams.hdbArgs = getArgs();
                        break;
                    case HdmiCecKeycode.CEC_KEYCODE_NUMBER_ENTRY_MODE /* 29 */:
                        if (params.installerPackageName == null) {
                            params.installerPackageName = "com.android.shell";
                        }
                        sessionParams.installFlags |= DumpState.DUMP_DOMAIN_PREFERRED;
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown option " + opt);
                }
            } else {
                if (replaceExisting) {
                    sessionParams.installFlags |= 2;
                }
                if (isInRepairMode()) {
                    Slog.i(TAG, "It's in repair mode, install only in current user.");
                    params.userId = REPAIR_MODE_USER_ID;
                }
                return params;
            }
        }
    }

    private boolean isInRepairMode() {
        int userId = UserHandle.getCallingUserId();
        try {
            userId = ActivityManager.getCurrentUser();
        } catch (SecurityException e) {
            Slog.i(TAG, "Security exception happended when checking is in repair mode.");
        } catch (Exception e2) {
            Slog.i(TAG, "Exception happended when checking is in repair mode.");
        }
        return userId == REPAIR_MODE_USER_ID;
    }

    private int runSetHomeActivity() {
        String pkgName;
        PrintWriter pw = getOutPrintWriter();
        int userId = 0;
        while (true) {
            String opt = getNextOption();
            if (opt != null) {
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
                if (component == null) {
                    pw.println("Error: invalid component name");
                    return 1;
                }
                if (component.indexOf(47) < 0) {
                    pkgName = component;
                } else {
                    ComponentName componentName = ComponentName.unflattenFromString(component);
                    if (componentName == null) {
                        pw.println("Error: invalid component name");
                        return 1;
                    }
                    pkgName = componentName.getPackageName();
                }
                CompletableFuture<Boolean> future = new CompletableFuture<>();
                try {
                    IRoleManager.Stub.asInterface(ServiceManager.getServiceOrThrow("role")).addRoleHolderAsUser("android.app.role.HOME", pkgName, 0, userId, new RemoteCallback(new RemoteCallback.OnResultListener(future) {
                        /* class com.android.server.pm.$$Lambda$PackageManagerShellCommand$v3vXA2YvCwaE7J0QfR1IQ122iTI */
                        private final /* synthetic */ CompletableFuture f$0;

                        {
                            this.f$0 = r1;
                        }

                        public final void onResult(Bundle bundle) {
                            PackageManagerShellCommand.lambda$runSetHomeActivity$1(this.f$0, bundle);
                        }
                    }));
                    if (future.get().booleanValue()) {
                        pw.println("Success");
                        return 0;
                    }
                    pw.println("Error: Failed to set default home.");
                    return 1;
                } catch (Exception e) {
                    pw.println(e.toString());
                    return 1;
                }
            }
        }
    }

    static /* synthetic */ void lambda$runSetHomeActivity$1(CompletableFuture future, Bundle res) {
        future.complete(Boolean.valueOf(res != null));
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
            return 0;
        }
        return 1;
    }

    private int runHasHwFeature() {
        int version;
        PrintWriter err = getErrPrintWriter();
        String featureName = getNextArg();
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
            }
        }
        boolean hasFeature = HwPackageManager.hasHwSystemFeature(featureName, version);
        getOutPrintWriter().println(hasFeature);
        if (hasFeature) {
            return 0;
        }
        return 1;
    }

    private int runListHwFeatures() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        List<FeatureInfo> list = new ArrayList<>(Arrays.asList(HwPackageManager.getHwSystemAvailableFeatures()));
        Collections.sort(list, new Comparator<FeatureInfo>() {
            /* class com.android.server.pm.PackageManagerShellCommand.AnonymousClass5 */

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
        int count = list.size();
        for (int p = 0; p < count; p++) {
            FeatureInfo fi = list.get(p);
            if (fi.name != null) {
                pw.print("hwFeature:" + fi.name);
                if (fi.version > 0) {
                    pw.print("=");
                    pw.print(fi.version);
                }
                pw.println();
            }
        }
        return 0;
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
            String opt = getNextOption();
            if (opt == null) {
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
            String opt = getNextOption();
            if (opt == null) {
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

    private int doWriteSplit(int sessionId, String inPath, long sizeBytes, String splitName, boolean logSuccess) throws RemoteException {
        IOException e;
        IOException e2;
        long sizeBytes2;
        ParcelFileDescriptor fd;
        PackageInstaller.Session session;
        PackageInstaller.Session session2 = null;
        try {
            PrintWriter pw = getOutPrintWriter();
            if (STDIN_PATH.equals(inPath)) {
                fd = ParcelFileDescriptor.dup(getInFileDescriptor());
                sizeBytes2 = sizeBytes;
            } else if (inPath != null) {
                fd = openFileForSystem(inPath, "r");
                if (fd == null) {
                    IoUtils.closeQuietly((AutoCloseable) null);
                    return -1;
                }
                long sizeBytes3 = fd.getStatSize();
                if (sizeBytes3 < 0) {
                    try {
                        PrintWriter errPrintWriter = getErrPrintWriter();
                        errPrintWriter.println("Unable to get size of: " + inPath);
                        IoUtils.closeQuietly((AutoCloseable) null);
                        return -1;
                    } catch (IOException e3) {
                        e2 = e3;
                        try {
                            PrintWriter errPrintWriter2 = getErrPrintWriter();
                            errPrintWriter2.println("Error: failed to write; " + e2.getMessage());
                            IoUtils.closeQuietly(session2);
                            return 1;
                        } catch (Throwable th) {
                            e = th;
                            IoUtils.closeQuietly(session2);
                            throw e;
                        }
                    } catch (Throwable th2) {
                        e = th2;
                        IoUtils.closeQuietly(session2);
                        throw e;
                    }
                } else {
                    sizeBytes2 = sizeBytes3;
                }
            } else {
                fd = ParcelFileDescriptor.dup(getInFileDescriptor());
                sizeBytes2 = sizeBytes;
            }
            if (sizeBytes2 <= 0) {
                try {
                    getErrPrintWriter().println("Error: must specify a APK size");
                    IoUtils.closeQuietly((AutoCloseable) null);
                    return 1;
                } catch (IOException e4) {
                    e2 = e4;
                    PrintWriter errPrintWriter22 = getErrPrintWriter();
                    errPrintWriter22.println("Error: failed to write; " + e2.getMessage());
                    IoUtils.closeQuietly(session2);
                    return 1;
                } catch (Throwable th3) {
                    e = th3;
                    IoUtils.closeQuietly(session2);
                    throw e;
                }
            } else {
                try {
                } catch (IOException e5) {
                    e2 = e5;
                    PrintWriter errPrintWriter222 = getErrPrintWriter();
                    errPrintWriter222.println("Error: failed to write; " + e2.getMessage());
                    IoUtils.closeQuietly(session2);
                    return 1;
                } catch (Throwable th4) {
                    e = th4;
                    IoUtils.closeQuietly(session2);
                    throw e;
                }
                try {
                    session = new PackageInstaller.Session(this.mInterface.getPackageInstaller().openSession(sessionId));
                } catch (IOException e6) {
                    e2 = e6;
                    PrintWriter errPrintWriter2222 = getErrPrintWriter();
                    errPrintWriter2222.println("Error: failed to write; " + e2.getMessage());
                    IoUtils.closeQuietly(session2);
                    return 1;
                } catch (Throwable th5) {
                    e = th5;
                    IoUtils.closeQuietly(session2);
                    throw e;
                }
                try {
                    session.write(splitName, 0, sizeBytes2, fd);
                    if (logSuccess) {
                        pw.println("Success: streamed " + sizeBytes2 + " bytes");
                    }
                    IoUtils.closeQuietly(session);
                    return 0;
                } catch (IOException e7) {
                    e2 = e7;
                    session2 = session;
                    PrintWriter errPrintWriter22222 = getErrPrintWriter();
                    errPrintWriter22222.println("Error: failed to write; " + e2.getMessage());
                    IoUtils.closeQuietly(session2);
                    return 1;
                } catch (Throwable th6) {
                    e = th6;
                    session2 = session;
                    IoUtils.closeQuietly(session2);
                    throw e;
                }
            }
        } catch (IOException e8) {
            e2 = e8;
            PrintWriter errPrintWriter222222 = getErrPrintWriter();
            errPrintWriter222222.println("Error: failed to write; " + e2.getMessage());
            IoUtils.closeQuietly(session2);
            return 1;
        } catch (Throwable th7) {
            e = th7;
            IoUtils.closeQuietly(session2);
            throw e;
        }
    }

    /* JADX INFO: finally extract failed */
    private int doInstallAddSession(int parentId, int[] sessionIds, boolean logSuccess) throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        try {
            PackageInstaller.Session session = new PackageInstaller.Session(this.mInterface.getPackageInstaller().openSession(parentId));
            if (!session.isMultiPackage()) {
                getErrPrintWriter().println("Error: parent session ID is not a multi-package session");
                IoUtils.closeQuietly(session);
                return 1;
            }
            for (int i : sessionIds) {
                session.addChildSessionId(i);
            }
            if (logSuccess) {
                pw.println("Success");
            }
            IoUtils.closeQuietly(session);
            return 0;
        } catch (Throwable th) {
            IoUtils.closeQuietly((AutoCloseable) null);
            throw th;
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
            if (!session.isMultiPackage() && !session.isStaged()) {
                try {
                    DexMetadataHelper.validateDexPaths(session.getNames());
                } catch (IOException | IllegalStateException e) {
                    pw.println("Warning [Could not validate the dex paths: " + e.getMessage() + "]");
                }
            }
            LocalIntentReceiver receiver = new LocalIntentReceiver();
            session.commit(receiver.getIntentSender());
            Intent result = receiver.getResult();
            int status = result.getIntExtra("android.content.pm.extra.STATUS", 1);
            if (status != 0) {
                pw.println("Failure [" + result.getStringExtra("android.content.pm.extra.STATUS_MESSAGE") + "]");
            } else if (logSuccess) {
                pw.println("Success");
            }
            return status;
        } finally {
            IoUtils.closeQuietly(session);
        }
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
        int groupCount;
        String groupName;
        List<PermissionInfo> ps;
        ArrayList<String> arrayList = groupList;
        PrintWriter pw = getOutPrintWriter();
        int groupCount2 = groupList.size();
        int i = 0;
        while (i < groupCount2) {
            String groupName2 = arrayList.get(i);
            String prefix = "";
            if (groups) {
                if (i > 0) {
                    pw.println("");
                }
                if (groupName2 != null) {
                    PermissionGroupInfo pgi = this.mInterface.getPermissionGroupInfo(groupName2, 0);
                    if (!summary) {
                        groupCount = groupCount2;
                        StringBuilder sb = new StringBuilder();
                        sb.append(labels ? "+ " : "");
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
                        StringBuilder sb2 = new StringBuilder();
                        groupCount = groupCount2;
                        sb2.append(loadText(pgi, pgi.labelRes, pgi.nonLocalizedLabel));
                        sb2.append(": ");
                        pw.print(sb2.toString());
                    } else {
                        groupCount = groupCount2;
                        pw.print(pgi.name + ": ");
                    }
                } else {
                    groupCount = groupCount2;
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append((!labels || summary) ? "" : "+ ");
                    sb3.append("ungrouped:");
                    pw.println(sb3.toString());
                }
                prefix = "  ";
            } else {
                groupCount = groupCount2;
            }
            List<PermissionInfo> ps2 = this.mInterface.queryPermissionsByGroup(arrayList.get(i), 0).getList();
            int count = ps2.size();
            boolean first = true;
            int p = 0;
            while (p < count) {
                PermissionInfo pi = ps2.get(p);
                if (!groups || groupName2 != null || pi.group == null) {
                    int base = pi.protectionLevel & 15;
                    ps = ps2;
                    if (base < startProtectionLevel) {
                        groupName = groupName2;
                    } else if (base > endProtectionLevel) {
                        groupName = groupName2;
                    } else if (summary) {
                        if (first) {
                            first = false;
                        } else {
                            pw.print(", ");
                        }
                        if (getResources(pi) != null) {
                            pw.print(loadText(pi, pi.labelRes, pi.nonLocalizedLabel));
                        } else {
                            pw.print(pi.name);
                        }
                        groupName = groupName2;
                    } else {
                        StringBuilder sb4 = new StringBuilder();
                        sb4.append(prefix);
                        sb4.append(labels ? "+ " : "");
                        sb4.append("permission:");
                        sb4.append(pi.name);
                        pw.println(sb4.toString());
                        if (labels) {
                            pw.println(prefix + "  package:" + pi.packageName);
                            if (getResources(pi) != null) {
                                StringBuilder sb5 = new StringBuilder();
                                sb5.append(prefix);
                                sb5.append("  label:");
                                groupName = groupName2;
                                sb5.append(loadText(pi, pi.labelRes, pi.nonLocalizedLabel));
                                pw.println(sb5.toString());
                                pw.println(prefix + "  description:" + loadText(pi, pi.descriptionRes, pi.nonLocalizedDescription));
                            } else {
                                groupName = groupName2;
                            }
                            pw.println(prefix + "  protectionLevel:" + PermissionInfo.protectionToString(pi.protectionLevel));
                        } else {
                            groupName = groupName2;
                        }
                    }
                } else {
                    ps = ps2;
                    groupName = groupName2;
                }
                p++;
                ps2 = ps;
                groupName2 = groupName;
            }
            if (summary) {
                pw.println("");
            }
            i++;
            arrayList = groupList;
            groupCount2 = groupCount;
        }
    }

    private String loadText(PackageItemInfo pii, int res, CharSequence nonLocalized) throws RemoteException {
        Resources r;
        if (nonLocalized != null) {
            return nonLocalized.toString();
        }
        if (res == 0 || (r = getResources(pii)) == null) {
            return null;
        }
        try {
            return r.getString(res);
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }

    private Resources getResources(PackageItemInfo pii) throws RemoteException {
        Resources res = this.mResourceCache.get(pii.packageName);
        if (res != null) {
            return res;
        }
        ApplicationInfo ai = this.mInterface.getApplicationInfo(pii.packageName, 0, 0);
        AssetManager am = new AssetManager();
        if (ai != null) {
            am.addAssetPath(ai.publicSourceDir);
        } else {
            Slog.i(TAG, "application info is null, packageName:" + pii.packageName);
        }
        Resources res2 = new Resources(am, null, null);
        this.mResourceCache.put(pii.packageName, res2);
        return res2;
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("Package manager (package) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("");
        pw.println("  path [--user USER_ID] PACKAGE");
        pw.println("    Print the path to the .apk of the given PACKAGE.");
        pw.println("");
        pw.println("  dump PACKAGE");
        pw.println("    Print various system state associated with the given PACKAGE.");
        pw.println("");
        pw.println("  list features");
        pw.println("    Prints all features of the system.");
        pw.println("");
        pw.println("  has-feature FEATURE_NAME [version]");
        pw.println("    Prints true and returns exit status 0 when system has a FEATURE_NAME,");
        pw.println("    otherwise prints false and returns exit status 1");
        pw.println("");
        pw.println("  list hwfeatures");
        pw.println("    Prints all hwFeatures of the system.");
        pw.println("");
        pw.println("  has-hwfeature FEATURE_NAME [version]");
        pw.println("    Prints true and returns exit status 0 when system has a FEATURE_NAME,");
        pw.println("    otherwise prints false and returns exit status 1");
        pw.println("");
        pw.println("  list instrumentation [-f] [TARGET-PACKAGE]");
        pw.println("    Prints all test packages; optionally only those targeting TARGET-PACKAGE");
        pw.println("    Options:");
        pw.println("      -f: dump the name of the .apk file containing the test package");
        pw.println("");
        pw.println("  list libraries");
        pw.println("    Prints all system libraries.");
        pw.println("");
        pw.println("  list packages [-f] [-d] [-e] [-s] [-3] [-i] [-l] [-u] [-U] ");
        pw.println("      [--show-versioncode] [--apex-only] [--uid UID] [--user USER_ID] [FILTER]");
        pw.println("    Prints all packages; optionally only those whose name contains");
        pw.println("    the text in FILTER.  Options are:");
        pw.println("      -f: see their associated file");
        pw.println("      -a: all known packages (but excluding APEXes)");
        pw.println("      -d: filter to only show disabled packages");
        pw.println("      -e: filter to only show enabled packages");
        pw.println("      -s: filter to only show system packages");
        pw.println("      -3: filter to only show third party packages");
        pw.println("      -i: see the installer for the packages");
        pw.println("      -l: ignored (used for compatibility with older releases)");
        pw.println("      -U: also show the package UID");
        pw.println("      -u: also include uninstalled packages");
        pw.println("      --show-versioncode: also show the version code");
        pw.println("      --apex-only: only show APEX packages");
        pw.println("      --uid UID: filter to only show packages with the given UID");
        pw.println("      --user USER_ID: only list packages belonging to the given user");
        pw.println("");
        pw.println("  list permission-groups");
        pw.println("    Prints all known permission groups.");
        pw.println("");
        pw.println("  list permissions [-g] [-f] [-d] [-u] [GROUP]");
        pw.println("    Prints all known permissions; optionally only those in GROUP.  Options are:");
        pw.println("      -g: organize by group");
        pw.println("      -f: print all information");
        pw.println("      -s: short summary");
        pw.println("      -d: only list dangerous permissions");
        pw.println("      -u: list only the permissions users will see");
        pw.println("");
        pw.println("  resolve-activity [--brief] [--components] [--query-flags FLAGS]");
        pw.println("       [--user USER_ID] INTENT");
        pw.println("    Prints the activity that resolves to the given INTENT.");
        pw.println("");
        pw.println("  query-activities [--brief] [--components] [--query-flags FLAGS]");
        pw.println("       [--user USER_ID] INTENT");
        pw.println("    Prints all activities that can handle the given INTENT.");
        pw.println("");
        pw.println("  query-services [--brief] [--components] [--query-flags FLAGS]");
        pw.println("       [--user USER_ID] INTENT");
        pw.println("    Prints all services that can handle the given INTENT.");
        pw.println("");
        pw.println("  query-receivers [--brief] [--components] [--query-flags FLAGS]");
        pw.println("       [--user USER_ID] INTENT");
        pw.println("    Prints all broadcast receivers that can handle the given INTENT.");
        pw.println("");
        pw.println("  install [-lrtsfdgw] [-i PACKAGE] [--user USER_ID|all|current]");
        pw.println("       [-p INHERIT_PACKAGE] [--install-location 0/1/2]");
        pw.println("       [--install-reason 0/1/2/3/4] [--originating-uri URI]");
        pw.println("       [--referrer URI] [--abi ABI_NAME] [--force-sdk]");
        pw.println("       [--preload] [--instantapp] [--full] [--dont-kill]");
        pw.println("       [--enable-rollback]");
        pw.println("       [--force-uuid internal|UUID] [--pkg PACKAGE] [-S BYTES] [--apex]");
        pw.println("       [PATH|-]");
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
        pw.println("      --restrict-permissions: don't whitelist restricted permissions at install");
        pw.println("      --originating-uri: set URI where app was downloaded from");
        pw.println("      --referrer: set URI that instigated the install of the app");
        pw.println("      --pkg: specify expected package name of app being installed");
        pw.println("      --abi: override the default ABI of the platform");
        pw.println("      --instantapp: cause the app to be installed as an ephemeral install app");
        pw.println("      --full: cause the app to be installed as a non-ephemeral full app");
        pw.println("      --install-location: force the install location:");
        pw.println("          0=auto, 1=internal only, 2=prefer external");
        pw.println("      --install-reason: indicates why the app is being installed:");
        pw.println("          0=unknown, 1=admin policy, 2=device restore,");
        pw.println("          3=device setup, 4=user request");
        pw.println("      --force-uuid: force install on to disk volume with given UUID");
        pw.println("      --apex: install an .apex file, not an .apk");
        pw.println("");
        pw.println("  install-create [-lrtsfdg] [-i PACKAGE] [--user USER_ID|all|current]");
        pw.println("       [-p INHERIT_PACKAGE] [--install-location 0/1/2]");
        pw.println("       [--install-reason 0/1/2/3/4] [--originating-uri URI]");
        pw.println("       [--referrer URI] [--abi ABI_NAME] [--force-sdk]");
        pw.println("       [--preload] [--instantapp] [--full] [--dont-kill]");
        pw.println("       [--force-uuid internal|UUID] [--pkg PACKAGE] [--apex] [-S BYTES]");
        pw.println("       [--multi-package] [--staged]");
        pw.println("    Like \"install\", but starts an install session.  Use \"install-write\"");
        pw.println("    to push data into the session, and \"install-commit\" to finish.");
        pw.println("");
        pw.println("  install-write [-S BYTES] SESSION_ID SPLIT_NAME [PATH|-]");
        pw.println("    Write an apk into the given install session.  If the path is '-', data");
        pw.println("    will be read from stdin.  Options are:");
        pw.println("      -S: size in bytes of package, required for stdin");
        pw.println("");
        pw.println("  install-add-session MULTI_PACKAGE_SESSION_ID CHILD_SESSION_IDs");
        pw.println("    Add one or more session IDs to a multi-package session.");
        pw.println("");
        pw.println("  install-commit SESSION_ID");
        pw.println("    Commit the given active install session, installing the app.");
        pw.println("");
        pw.println("  install-abandon SESSION_ID");
        pw.println("    Delete the given active install session.");
        pw.println("");
        pw.println("  set-install-location LOCATION");
        pw.println("    Changes the default install location.  NOTE this is only intended for debugging;");
        pw.println("    using this can cause applications to break and other undersireable behavior.");
        pw.println("    LOCATION is one of:");
        pw.println("    0 [auto]: Let system decide the best location");
        pw.println("    1 [internal]: Install on internal device storage");
        pw.println("    2 [external]: Install on external media");
        pw.println("");
        pw.println("  get-install-location");
        pw.println("    Returns the current install location: 0, 1 or 2 as per set-install-location.");
        pw.println("");
        pw.println("  move-package PACKAGE [internal|UUID]");
        pw.println("");
        pw.println("  move-primary-storage [internal|UUID]");
        pw.println("");
        pw.println("  pm uninstall [-k] [--user USER_ID] [--versionCode VERSION_CODE] PACKAGE [SPLIT]");
        pw.println("    Remove the given package name from the system.  May remove an entire app");
        pw.println("    if no SPLIT name is specified, otherwise will remove only the split of the");
        pw.println("    given app.  Options are:");
        pw.println("      -k: keep the data and cache directories around after package removal.");
        pw.println("      --user: remove the app from the given user.");
        pw.println("      --versionCode: only uninstall if the app has the given version code.");
        pw.println("");
        pw.println("  clear [--user USER_ID] PACKAGE");
        pw.println("    Deletes all data associated with a package.");
        pw.println("");
        pw.println("  enable [--user USER_ID] PACKAGE_OR_COMPONENT");
        pw.println("  disable [--user USER_ID] PACKAGE_OR_COMPONENT");
        pw.println("  disable-user [--user USER_ID] PACKAGE_OR_COMPONENT");
        pw.println("  disable-until-used [--user USER_ID] PACKAGE_OR_COMPONENT");
        pw.println("  default-state [--user USER_ID] PACKAGE_OR_COMPONENT");
        pw.println("    These commands change the enabled state of a given package or");
        pw.println("    component (written as \"package/class\").");
        pw.println("");
        pw.println("  hide [--user USER_ID] PACKAGE_OR_COMPONENT");
        pw.println("  unhide [--user USER_ID] PACKAGE_OR_COMPONENT");
        pw.println("");
        pw.println("  suspend [--user USER_ID] TARGET-PACKAGE");
        pw.println("    Suspends the specified package (as user).");
        pw.println("");
        pw.println("  unsuspend [--user USER_ID] TARGET-PACKAGE");
        pw.println("    Unsuspends the specified package (as user).");
        pw.println("");
        pw.println("  grant [--user USER_ID] PACKAGE PERMISSION");
        pw.println("  revoke [--user USER_ID] PACKAGE PERMISSION");
        pw.println("    These commands either grant or revoke permissions to apps.  The permissions");
        pw.println("    must be declared as used in the app's manifest, be runtime permissions");
        pw.println("    (protection level dangerous), and the app targeting SDK greater than Lollipop MR1.");
        pw.println("");
        pw.println("  reset-permissions");
        pw.println("    Revert all runtime permissions to their default state.");
        pw.println("");
        pw.println("  set-permission-enforced PERMISSION [true|false]");
        pw.println("");
        pw.println("  get-privapp-permissions TARGET-PACKAGE");
        pw.println("    Prints all privileged permissions for a package.");
        pw.println("");
        pw.println("  get-privapp-deny-permissions TARGET-PACKAGE");
        pw.println("    Prints all privileged permissions that are denied for a package.");
        pw.println("");
        pw.println("  get-oem-permissions TARGET-PACKAGE");
        pw.println("    Prints all OEM permissions for a package.");
        pw.println("");
        pw.println("  set-app-link [--user USER_ID] PACKAGE {always|ask|never|undefined}");
        pw.println("  get-app-link [--user USER_ID] PACKAGE");
        pw.println("");
        pw.println("  trim-caches DESIRED_FREE_SPACE [internal|UUID]");
        pw.println("    Trim cache files to reach the given free space.");
        pw.println("");
        pw.println("  create-user [--profileOf USER_ID] [--managed] [--restricted] [--ephemeral]");
        pw.println("      [--guest] USER_NAME");
        pw.println("    Create a new user with the given USER_NAME, printing the new user identifier");
        pw.println("    of the user.");
        pw.println("");
        pw.println("  remove-user USER_ID");
        pw.println("    Remove the user with the given USER_IDENTIFIER, deleting all data");
        pw.println("    associated with that user");
        pw.println("");
        pw.println("  set-user-restriction [--user USER_ID] RESTRICTION VALUE");
        pw.println("");
        pw.println("  get-max-users");
        pw.println("");
        pw.println("  get-max-running-users");
        pw.println("");
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
        pw.println("      --compile-layouts: compile layout resources for faster inflation");
        pw.println("");
        pw.println("  force-dex-opt PACKAGE");
        pw.println("    Force immediate execution of dex opt for the given PACKAGE.");
        pw.println("");
        pw.println("  bg-dexopt-job");
        pw.println("    Execute the background optimizations immediately.");
        pw.println("    Note that the command only runs the background optimizer logic. It may");
        pw.println("    overlap with the actual job but the job scheduler will not be able to");
        pw.println("    cancel it. It will also run even if the device is not in the idle");
        pw.println("    maintenance mode.");
        pw.println("");
        pw.println("  reconcile-secondary-dex-files TARGET-PACKAGE");
        pw.println("    Reconciles the package secondary dex files with the generated oat files.");
        pw.println("");
        pw.println("  dump-profiles TARGET-PACKAGE");
        pw.println("    Dumps method/class profile files to");
        pw.println("    /data/misc/profman/TARGET-PACKAGE.txt");
        pw.println("");
        pw.println("  snapshot-profile TARGET-PACKAGE [--code-path path]");
        pw.println("    Take a snapshot of the package profiles to");
        pw.println("    /data/misc/profman/TARGET-PACKAGE[-code-path].prof");
        pw.println("    If TARGET-PACKAGE=android it will take a snapshot of the boot image");
        pw.println("");
        pw.println("  set-home-activity [--user USER_ID] TARGET-COMPONENT");
        pw.println("    Set the default home activity (aka launcher).");
        pw.println("    TARGET-COMPONENT can be a package name (com.package.my) or a full");
        pw.println("    component (com.package.my/component.name). However, only the package name");
        pw.println("    matters: the actual component used will be determined automatically from");
        pw.println("    the package.");
        pw.println("");
        pw.println("  set-installer PACKAGE INSTALLER");
        pw.println("    Set installer package name");
        pw.println("");
        pw.println("  get-instantapp-resolver");
        pw.println("    Return the name of the component that is the current instant app installer.");
        pw.println("");
        pw.println("  set-harmful-app-warning [--user <USER_ID>] <PACKAGE> [<WARNING>]");
        pw.println("    Mark the app as harmful with the given warning message.");
        pw.println("");
        pw.println("  get-harmful-app-warning [--user <USER_ID>] <PACKAGE>");
        pw.println("    Return the harmful app warning message for the given app, if present");
        pw.println();
        pw.println("  uninstall-system-updates");
        pw.println("    Remove updates to all system applications and fall back to their /system version.");
        pw.println();
        pw.println("  get-moduleinfo [--all | --installed] [module-name]");
        pw.println("    Displays module info. If module-name is specified only that info is shown");
        pw.println("    By default, without any argument only installed modules are shown.");
        pw.println("      --all: show all module info");
        pw.println("      --installed: show only installed modules");
        pw.println("");
        Intent.printIntentArgsHelp(pw, "");
    }

    /* access modifiers changed from: private */
    public static class LocalIntentReceiver {
        private IIntentSender.Stub mLocalSender;
        private final LinkedBlockingQueue<Intent> mResult;

        private LocalIntentReceiver() {
            this.mResult = new LinkedBlockingQueue<>();
            this.mLocalSender = new IIntentSender.Stub() {
                /* class com.android.server.pm.PackageManagerShellCommand.LocalIntentReceiver.AnonymousClass1 */

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
}

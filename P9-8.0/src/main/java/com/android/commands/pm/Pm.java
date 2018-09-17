package com.android.commands.pm;

import android.accounts.IAccountManager;
import android.app.ActivityManager;
import android.app.PackageInstallObserver;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.IPackageDataObserver.Stub;
import android.content.pm.IPackageInstaller;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller.Session;
import android.content.pm.PackageInstaller.SessionInfo;
import android.content.pm.PackageInstaller.SessionParams;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.PackageLite;
import android.content.pm.UserInfo;
import android.hdm.HwDeviceManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IUserManager;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.internal.content.PackageHelper;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.SizedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import libcore.io.IoUtils;

public final class Pm {
    private static final String PM_NOT_RUNNING_ERR = "Error: Could not access the Package Manager.  Is the system running?";
    private static final String STDIN_PATH = "-";
    private static final String TAG = "Pm";
    IAccountManager mAm;
    private String[] mArgs;
    private String mCurArgData;
    IPackageInstaller mInstaller;
    private int mNextArg;
    IPackageManager mPm;
    IUserManager mUm;

    static class ClearCacheObserver extends Stub {
        boolean finished;
        boolean result;

        ClearCacheObserver() {
        }

        public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
            synchronized (this) {
                this.finished = true;
                this.result = succeeded;
                notifyAll();
            }
        }
    }

    static class ClearDataObserver extends Stub {
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
        SessionParams sessionParams;
        int userId;

        /* synthetic */ InstallParams(InstallParams -this0) {
            this();
        }

        private InstallParams() {
            this.userId = -1;
        }
    }

    private static class LocalIntentReceiver {
        private IIntentSender.Stub mLocalSender;
        private final SynchronousQueue<Intent> mResult;

        /* synthetic */ LocalIntentReceiver(LocalIntentReceiver -this0) {
            this();
        }

        private LocalIntentReceiver() {
            this.mResult = new SynchronousQueue();
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
                return (Intent) this.mResult.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class LocalPackageInstallObserver extends PackageInstallObserver {
        String extraPackage;
        String extraPermission;
        boolean finished;
        int result;

        LocalPackageInstallObserver() {
        }

        public void onPackageInstalled(String name, int status, String msg, Bundle extras) {
            synchronized (this) {
                this.finished = true;
                this.result = status;
                if (status == -112) {
                    this.extraPermission = extras.getString("android.content.pm.extra.FAILURE_EXISTING_PERMISSION");
                    this.extraPackage = extras.getString("android.content.pm.extra.FAILURE_EXISTING_PACKAGE");
                }
                notifyAll();
            }
        }
    }

    static final class MyShellCallback extends ShellCallback {
        MyShellCallback() {
        }

        public ParcelFileDescriptor onOpenOutputFile(String path, String seLinuxContext) {
            File file = new File(path);
            String msg;
            try {
                ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, 738197504);
                if (seLinuxContext != null) {
                    String tcon = SELinux.getFileContext(file.getAbsolutePath());
                    if (!SELinux.checkSELinuxAccess(seLinuxContext, tcon, "file", "write")) {
                        try {
                            fd.close();
                        } catch (IOException e) {
                        }
                        msg = "System server has no access to file context " + tcon;
                        System.err.println(msg + " (from path " + file.getAbsolutePath() + ", context " + seLinuxContext + ")");
                        throw new IllegalArgumentException(msg);
                    }
                }
                return fd;
            } catch (FileNotFoundException e2) {
                msg = "Unable to open file " + path + ": " + e2;
                System.err.println(msg);
                throw new IllegalArgumentException(msg);
            }
        }
    }

    public static void main(String[] args) {
        int exitCode = 1;
        try {
            exitCode = new Pm().run(args);
        } catch (Exception e) {
            Log.e(TAG, "Error", e);
            System.err.println("Error: " + e);
            if (e instanceof RemoteException) {
                System.err.println(PM_NOT_RUNNING_ERR);
            }
        }
        System.exit(exitCode);
    }

    public int run(String[] args) throws RemoteException {
        if (args.length < 1) {
            return showUsage();
        }
        this.mAm = IAccountManager.Stub.asInterface(ServiceManager.getService("account"));
        this.mUm = IUserManager.Stub.asInterface(ServiceManager.getService("user"));
        this.mPm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (this.mPm == null) {
            System.err.println(PM_NOT_RUNNING_ERR);
            return 1;
        }
        this.mInstaller = this.mPm.getPackageInstaller();
        this.mArgs = args;
        String op = args[0];
        this.mNextArg = 1;
        if ("list".equals(op)) {
            return runList();
        }
        if ("path".equals(op)) {
            return runPath();
        }
        if ("dump".equals(op)) {
            return runDump();
        }
        if ("install".equals(op)) {
            return runInstall();
        }
        if ("install-create".equals(op)) {
            return runInstallCreate();
        }
        if ("install-write".equals(op)) {
            return runInstallWrite();
        }
        if ("install-commit".equals(op)) {
            return runInstallCommit();
        }
        if ("install-abandon".equals(op) || "install-destroy".equals(op)) {
            return runInstallAbandon();
        }
        if ("set-installer".equals(op)) {
            return runSetInstaller();
        }
        if ("uninstall".equals(op)) {
            return runUninstall();
        }
        if ("clear".equals(op)) {
            return runClear();
        }
        if ("enable".equals(op)) {
            return runSetEnabledSetting(1);
        }
        if ("disable".equals(op)) {
            return runSetEnabledSetting(2);
        }
        if ("disable-user".equals(op)) {
            return runSetEnabledSetting(3);
        }
        if ("disable-until-used".equals(op)) {
            return runSetEnabledSetting(4);
        }
        if ("default-state".equals(op)) {
            return runSetEnabledSetting(0);
        }
        if ("hide".equals(op)) {
            return runSetHiddenSetting(true);
        }
        if ("unhide".equals(op)) {
            return runSetHiddenSetting(false);
        }
        if ("grant".equals(op)) {
            return runGrantRevokePermission(true);
        }
        if ("revoke".equals(op)) {
            return runGrantRevokePermission(false);
        }
        if ("reset-permissions".equals(op)) {
            return runResetPermissions();
        }
        if ("set-permission-enforced".equals(op)) {
            return runSetPermissionEnforced();
        }
        if ("set-app-link".equals(op)) {
            return runSetAppLink();
        }
        if ("get-app-link".equals(op)) {
            return runGetAppLink();
        }
        if ("set-install-location".equals(op)) {
            return runSetInstallLocation();
        }
        if ("get-install-location".equals(op)) {
            return runGetInstallLocation();
        }
        if ("trim-caches".equals(op)) {
            return runTrimCaches();
        }
        if ("create-user".equals(op)) {
            return runCreateUser();
        }
        if ("remove-user".equals(op)) {
            return runRemoveUser();
        }
        if ("get-max-users".equals(op)) {
            return runGetMaxUsers();
        }
        if ("force-dex-opt".equals(op)) {
            return runForceDexOpt();
        }
        if ("move-package".equals(op)) {
            return runMovePackage();
        }
        if ("move-primary-storage".equals(op)) {
            return runMovePrimaryStorage();
        }
        if ("set-user-restriction".equals(op)) {
            return runSetUserRestriction();
        }
        try {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("-l")) {
                    return runShellCommand("package", new String[]{"list", "package"});
                } else if (args[0].equalsIgnoreCase("-lf")) {
                    return runShellCommand("package", new String[]{"list", "package", "-f"});
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("-p")) {
                return displayPackageFilePath(args[1], 0);
            }
            if (op != null) {
                System.err.println("Error: unknown command '" + op + "'");
            }
            showUsage();
            return 1;
        } catch (Throwable th) {
            if (!false) {
                if (op != null) {
                    System.err.println("Error: unknown command '" + op + "'");
                }
                showUsage();
            }
        }
    }

    private int runShellCommand(String serviceName, String[] args) {
        int i = "results";
        HandlerThread handlerThread = new HandlerThread(i);
        handlerThread.start();
        try {
            i = ServiceManager.getService(serviceName);
            i.shellCommand(FileDescriptor.in, FileDescriptor.out, FileDescriptor.err, args, new MyShellCallback(), new ResultReceiver(new Handler(handlerThread.getLooper())));
            i = 0;
            return i;
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        } finally {
            handlerThread.quitSafely();
        }
    }

    private int translateUserId(int userId, String logContext) {
        return ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, true, logContext, "pm command");
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

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0082 A:{Splitter: B:7:0x002f, ExcHandler: android.content.pm.PackageParser.PackageParserException (r12_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:18:0x0082, code:
            r12 = move-exception;
     */
    /* JADX WARNING: Missing block: B:19:0x0083, code:
            java.lang.System.err.println("Error: Failed to parse APK file: " + r12);
     */
    /* JADX WARNING: Missing block: B:20:0x009d, code:
            return 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int runInstall() throws RemoteException {
        long startedTime = SystemClock.elapsedRealtime();
        InstallParams params = makeInstallParams();
        String inPath = nextArg();
        if (params.sessionParams.sizeBytes == -1 && (STDIN_PATH.equals(inPath) ^ 1) != 0) {
            File file = new File(inPath);
            if (file.isFile()) {
                try {
                    params.sessionParams.setSize(PackageHelper.calculateInstalledSize(new PackageLite(null, PackageParser.parseApkLite(file, 0), null, null, null, null, null, null), false, params.sessionParams.abiOverride));
                } catch (Exception e) {
                }
            } else {
                System.err.println("Error: Can't open non-file: " + inPath);
                return 1;
            }
        }
        int sessionId = doCreateSession(params.sessionParams, params.installerPackageName, params.userId);
        if (inPath == null) {
            try {
                if (params.sessionParams.sizeBytes == -1) {
                    System.err.println("Error: must either specify a package size or an APK file");
                    int i = 1;
                    return i;
                }
            } finally {
                try {
                    this.mInstaller.abandonSession(sessionId);
                } catch (Exception e2) {
                }
            }
        }
        if (HwDeviceManager.disallowOp(6)) {
            System.err.println("Failure [MDM_FORBID_ADB_INSTALL]");
            try {
                this.mInstaller.abandonSession(sessionId);
            } catch (Exception e3) {
            }
            return 1;
        }
        if (doWriteSession(sessionId, inPath, params.sessionParams.sizeBytes, "base.apk", false) != 0) {
            try {
                this.mInstaller.abandonSession(sessionId);
            } catch (Exception e4) {
            }
            return 1;
        }
        Pair<String, Integer> status = doCommitSession(sessionId, false);
        if (((Integer) status.second).intValue() != 0) {
            try {
                this.mInstaller.abandonSession(sessionId);
            } catch (Exception e5) {
            }
            return 1;
        }
        Log.i(TAG, "Package " + ((String) status.first) + " installed in " + (SystemClock.elapsedRealtime() - startedTime) + " ms");
        System.out.println("Success");
        try {
            this.mInstaller.abandonSession(sessionId);
        } catch (Exception e6) {
        }
        return 0;
    }

    private int runInstallAbandon() throws RemoteException {
        return doAbandonSession(Integer.parseInt(nextArg()), true);
    }

    private int runInstallCommit() throws RemoteException {
        return ((Integer) doCommitSession(Integer.parseInt(nextArg()), true).second).intValue();
    }

    private int runInstallCreate() throws RemoteException {
        InstallParams installParams = makeInstallParams();
        System.out.println("Success: created install session [" + doCreateSession(installParams.sessionParams, installParams.installerPackageName, installParams.userId) + "]");
        return 0;
    }

    private int runInstallWrite() throws RemoteException {
        long sizeBytes = -1;
        while (true) {
            String opt = nextOption();
            if (opt == null) {
                return doWriteSession(Integer.parseInt(nextArg()), nextArg(), sizeBytes, nextArg(), true);
            } else if (opt.equals("-S")) {
                sizeBytes = Long.parseLong(nextArg());
            } else {
                throw new IllegalArgumentException("Unknown option: " + opt);
            }
        }
    }

    private InstallParams makeInstallParams() {
        SessionParams sessionParams = new SessionParams(1);
        InstallParams params = new InstallParams();
        params.sessionParams = sessionParams;
        while (true) {
            String opt = nextOption();
            if (opt == null) {
                return params;
            }
            if (opt.equals("-l")) {
                sessionParams.installFlags |= 1;
            } else if (opt.equals("-r")) {
                sessionParams.installFlags |= 2;
            } else if (opt.equals("-i")) {
                params.installerPackageName = nextArg();
                if (params.installerPackageName == null) {
                    throw new IllegalArgumentException("Missing installer package");
                }
            } else if (opt.equals("-t")) {
                sessionParams.installFlags |= 4;
            } else if (opt.equals("-s")) {
                sessionParams.installFlags |= 8;
            } else if (opt.equals("-f")) {
                sessionParams.installFlags |= 16;
            } else if (opt.equals("-d")) {
                sessionParams.installFlags |= 128;
            } else if (opt.equals("-g")) {
                sessionParams.installFlags |= 256;
            } else if (opt.equals("--dont-kill")) {
                sessionParams.installFlags |= 4096;
            } else if (opt.equals("--originating-uri")) {
                sessionParams.originatingUri = Uri.parse(nextOptionData());
            } else if (opt.equals("--referrer")) {
                sessionParams.referrerUri = Uri.parse(nextOptionData());
            } else if (opt.equals("-p")) {
                sessionParams.mode = 2;
                sessionParams.appPackageName = nextOptionData();
                if (sessionParams.appPackageName == null) {
                    throw new IllegalArgumentException("Missing inherit package name");
                }
            } else if (opt.equals("--pkg")) {
                sessionParams.appPackageName = nextOptionData();
                if (sessionParams.appPackageName == null) {
                    throw new IllegalArgumentException("Missing package name");
                }
            } else if (opt.equals("-S")) {
                long sizeBytes = Long.parseLong(nextOptionData());
                if (sizeBytes <= 0) {
                    throw new IllegalArgumentException("Size must be positive");
                }
                sessionParams.setSize(sizeBytes);
            } else if (opt.equals("--abi")) {
                sessionParams.abiOverride = checkAbiArgument(nextOptionData());
            } else if (opt.equals("--ephemeral") || opt.equals("--instant")) {
                sessionParams.setInstallAsInstantApp(true);
            } else if (opt.equals("--full")) {
                sessionParams.setInstallAsInstantApp(false);
            } else if (opt.equals("--user")) {
                params.userId = UserHandle.parseUserArg(nextOptionData());
            } else if (opt.equals("--install-location")) {
                sessionParams.installLocation = Integer.parseInt(nextOptionData());
            } else if (opt.equals("--force-uuid")) {
                sessionParams.installFlags |= 512;
                sessionParams.volumeUuid = nextOptionData();
                if ("internal".equals(sessionParams.volumeUuid)) {
                    sessionParams.volumeUuid = null;
                }
            } else if (opt.equals("--force-sdk")) {
                sessionParams.installFlags |= 8192;
            } else if (opt.equals("--hwhdb")) {
                params.sessionParams.hdbEncode = nextOptionData();
                params.sessionParams.hdbArgIndex = this.mNextArg;
                params.sessionParams.hdbArgs = this.mArgs;
            } else {
                throw new IllegalArgumentException("Unknown option " + opt);
            }
        }
    }

    private int doCreateSession(SessionParams params, String installerPackageName, int userId) throws RemoteException {
        userId = translateUserId(userId, "runInstallCreate");
        if (userId == -1) {
            userId = 0;
            params.installFlags |= 64;
        }
        return this.mInstaller.createSession(params, installerPackageName, userId);
    }

    private int doWriteSession(int sessionId, String inPath, long sizeBytes, String splitName, boolean logSuccess) throws RemoteException {
        Session session;
        IOException e;
        Throwable th;
        if (STDIN_PATH.equals(inPath)) {
            inPath = null;
        } else if (inPath != null) {
            File file = new File(inPath);
            if (file.isFile()) {
                sizeBytes = file.length();
            }
        }
        SessionInfo info = this.mInstaller.getSessionInfo(sessionId);
        try {
            InputStream in;
            session = new Session(this.mInstaller.openSession(sessionId));
            if (inPath != null) {
                try {
                    in = new FileInputStream(inPath);
                } catch (IOException e2) {
                    e = e2;
                    try {
                        System.err.println("Error: failed to write; " + e.getMessage());
                        IoUtils.closeQuietly(null);
                        IoUtils.closeQuietly(null);
                        IoUtils.closeQuietly(session);
                        return 1;
                    } catch (Throwable th2) {
                        th = th2;
                        IoUtils.closeQuietly(null);
                        IoUtils.closeQuietly(null);
                        IoUtils.closeQuietly(session);
                        throw th;
                    }
                }
            }
            in = new SizedInputStream(System.in, sizeBytes);
            OutputStream out = session.openWrite(splitName, 0, sizeBytes);
            int total = 0;
            byte[] buffer = new byte[65536];
            while (true) {
                int c = in.read(buffer);
                if (c == -1) {
                    break;
                }
                total += c;
                out.write(buffer, 0, c);
                if (info.sizeBytes > 0) {
                    session.addProgress(((float) c) / ((float) info.sizeBytes));
                }
            }
            session.fsync(out);
            if (logSuccess) {
                System.out.println("Success: streamed " + total + " bytes");
            }
            IoUtils.closeQuietly(out);
            IoUtils.closeQuietly(in);
            IoUtils.closeQuietly(session);
            return 0;
        } catch (IOException e3) {
            e = e3;
            session = null;
            System.err.println("Error: failed to write; " + e.getMessage());
            IoUtils.closeQuietly(null);
            IoUtils.closeQuietly(null);
            IoUtils.closeQuietly(session);
            return 1;
        } catch (Throwable th3) {
            th = th3;
            session = null;
            IoUtils.closeQuietly(null);
            IoUtils.closeQuietly(null);
            IoUtils.closeQuietly(session);
            throw th;
        }
    }

    private Pair<String, Integer> doCommitSession(int sessionId, boolean logSuccess) throws RemoteException {
        Throwable th;
        AutoCloseable autoCloseable = null;
        try {
            Session session = new Session(this.mInstaller.openSession(sessionId));
            try {
                LocalIntentReceiver receiver = new LocalIntentReceiver();
                session.commit(receiver.getIntentSender());
                Intent result = receiver.getResult();
                int status = result.getIntExtra("android.content.pm.extra.STATUS", 1);
                if (status != 0) {
                    System.err.println("Failure [" + result.getStringExtra("android.content.pm.extra.STATUS_MESSAGE") + "]");
                } else if (logSuccess) {
                    System.out.println("Success");
                }
                Pair<String, Integer> pair = new Pair(result.getStringExtra("android.content.pm.extra.PACKAGE_NAME"), Integer.valueOf(status));
                IoUtils.closeQuietly(session);
                return pair;
            } catch (Throwable th2) {
                th = th2;
                autoCloseable = session;
            }
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(autoCloseable);
            throw th;
        }
    }

    private int doAbandonSession(int sessionId, boolean logSuccess) throws RemoteException {
        Throwable th;
        Session session = null;
        try {
            Session session2 = new Session(this.mInstaller.openSession(sessionId));
            try {
                session2.abandon();
                if (logSuccess) {
                    System.out.println("Success");
                }
                IoUtils.closeQuietly(session2);
                return 0;
            } catch (Throwable th2) {
                th = th2;
                session = session2;
                IoUtils.closeQuietly(session);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(session);
            throw th;
        }
    }

    private int runList() {
        if (!"users".equals(nextArg())) {
            return runShellCommand("package", this.mArgs);
        }
        return runShellCommand("user", new String[]{"list"});
    }

    private int runUninstall() {
        return runShellCommand("package", this.mArgs);
    }

    private int runPath() {
        int userId = 0;
        String option = nextOption();
        if (option != null && option.equals("--user")) {
            String optionData = nextOptionData();
            if (optionData == null || (isNumber(optionData) ^ 1) != 0) {
                System.err.println("Error: no USER_ID specified");
                return showUsage();
            }
            userId = Integer.parseInt(optionData);
        }
        String pkg = nextArg();
        if (pkg != null) {
            return displayPackageFilePath(pkg, userId);
        }
        System.err.println("Error: no package specified");
        return 1;
    }

    private int runDump() {
        String pkg = nextArg();
        if (pkg == null) {
            System.err.println("Error: no package specified");
            return 1;
        }
        ActivityManager.dumpPackageStateStatic(FileDescriptor.out, pkg);
        return 0;
    }

    private int runSetAppLink() {
        int userId = 0;
        do {
            String opt = nextOption();
            if (opt == null) {
                String pkg = nextArg();
                if (pkg == null) {
                    System.err.println("Error: no package specified.");
                    return showUsage();
                }
                String modeString = nextArg();
                if (modeString == null) {
                    System.err.println("Error: no app link state specified.");
                    return showUsage();
                }
                int newMode;
                String toLowerCase = modeString.toLowerCase();
                if (toLowerCase.equals("undefined")) {
                    newMode = 0;
                } else if (toLowerCase.equals("always")) {
                    newMode = 2;
                } else if (toLowerCase.equals("ask")) {
                    newMode = 1;
                } else if (toLowerCase.equals("always-ask")) {
                    newMode = 4;
                } else if (toLowerCase.equals("never")) {
                    newMode = 3;
                } else {
                    System.err.println("Error: unknown app link state '" + modeString + "'");
                    return 1;
                }
                try {
                    PackageInfo info = this.mPm.getPackageInfo(pkg, 0, userId);
                    if (info == null) {
                        System.err.println("Error: package " + pkg + " not found.");
                        return 1;
                    } else if ((info.applicationInfo.privateFlags & 16) == 0) {
                        System.err.println("Error: package " + pkg + " does not handle web links.");
                        return 1;
                    } else if (this.mPm.updateIntentVerificationStatus(pkg, newMode, userId)) {
                        return 0;
                    } else {
                        System.err.println("Error: unable to update app link status for " + pkg);
                        return 1;
                    }
                } catch (Exception e) {
                    System.err.println(e.toString());
                    System.err.println(PM_NOT_RUNNING_ERR);
                    return 1;
                }
            } else if (opt.equals("--user")) {
                userId = Integer.parseInt(nextOptionData());
            } else {
                System.err.println("Error: unknown option: " + opt);
                return showUsage();
            }
        } while (userId >= 0);
        System.err.println("Error: user must be >= 0");
        return 1;
    }

    private int runGetAppLink() {
        int userId = 0;
        do {
            String opt = nextOption();
            if (opt == null) {
                String pkg = nextArg();
                if (pkg == null) {
                    System.err.println("Error: no package specified.");
                    return showUsage();
                }
                try {
                    PackageInfo info = this.mPm.getPackageInfo(pkg, 0, userId);
                    if (info == null) {
                        System.err.println("Error: package " + pkg + " not found.");
                        return 1;
                    } else if ((info.applicationInfo.privateFlags & 16) == 0) {
                        System.err.println("Error: package " + pkg + " does not handle web links.");
                        return 1;
                    } else {
                        System.out.println(linkStateToString(this.mPm.getIntentVerificationStatus(pkg, userId)));
                        return 0;
                    }
                } catch (Exception e) {
                    System.err.println(e.toString());
                    System.err.println(PM_NOT_RUNNING_ERR);
                    return 1;
                }
            } else if (opt.equals("--user")) {
                userId = Integer.parseInt(nextOptionData());
            } else {
                System.err.println("Error: unknown option: " + opt);
                return showUsage();
            }
        } while (userId >= 0);
        System.err.println("Error: user must be >= 0");
        return 1;
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

    private int runSetInstallLocation() {
        String arg = nextArg();
        if (arg == null) {
            System.err.println("Error: no install location specified.");
            return 1;
        }
        try {
            try {
                if (this.mPm.setInstallLocation(Integer.parseInt(arg))) {
                    return 0;
                }
                System.err.println("Error: install location has to be a number.");
                return 1;
            } catch (RemoteException e) {
                System.err.println(e.toString());
                System.err.println(PM_NOT_RUNNING_ERR);
                return 1;
            }
        } catch (NumberFormatException e2) {
            System.err.println("Error: install location has to be a number.");
            return 1;
        }
    }

    private int runGetInstallLocation() {
        try {
            int loc = this.mPm.getInstallLocation();
            String locStr = "invalid";
            if (loc == 0) {
                locStr = "auto";
            } else if (loc == 1) {
                locStr = "internal";
            } else if (loc == 2) {
                locStr = "external";
            }
            System.out.println(loc + "[" + locStr + "]");
            return 0;
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(PM_NOT_RUNNING_ERR);
            return 1;
        }
    }

    private int runSetInstaller() throws RemoteException {
        String targetPackage = nextArg();
        String installerPackageName = nextArg();
        if (targetPackage == null || installerPackageName == null) {
            throw new IllegalArgumentException("must provide both target and installer package names");
        }
        this.mPm.setInstallerPackageName(targetPackage, installerPackageName);
        System.out.println("Success");
        return 0;
    }

    public int runCreateUser() {
        int userId = -1;
        int flags = 0;
        while (true) {
            String opt = nextOption();
            if (opt == null) {
                String arg = nextArg();
                if (arg == null) {
                    System.err.println("Error: no user name specified.");
                    return 1;
                }
                UserInfo info;
                String name = arg;
                if ((flags & 8) != 0) {
                    int parentUserId = userId >= 0 ? userId : 0;
                    try {
                        info = this.mUm.createRestrictedProfile(arg, parentUserId);
                        this.mAm.addSharedAccountsFromParentUser(parentUserId, userId, Process.myUid() == 0 ? "root" : "com.android.shell");
                    } catch (RemoteException e) {
                        System.err.println(e.toString());
                        System.err.println(PM_NOT_RUNNING_ERR);
                        return 1;
                    }
                } else if (userId < 0) {
                    info = this.mUm.createUser(arg, flags);
                } else {
                    info = this.mUm.createProfileForUser(arg, flags, userId, null);
                }
                if (info != null) {
                    System.out.println("Success: created user id " + info.id);
                    return 1;
                }
                System.err.println("Error: couldn't create User.");
                return 1;
            } else if ("--profileOf".equals(opt)) {
                String optionData = nextOptionData();
                if (optionData == null || (isNumber(optionData) ^ 1) != 0) {
                    System.err.println("Error: no USER_ID specified");
                } else {
                    userId = Integer.parseInt(optionData);
                }
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
                System.err.println("Error: unknown option " + opt);
                return showUsage();
            }
        }
        System.err.println("Error: no USER_ID specified");
        return showUsage();
    }

    public int runRemoveUser() {
        String arg = nextArg();
        if (arg == null) {
            System.err.println("Error: no user id specified.");
            return 1;
        }
        try {
            int userId = Integer.parseInt(arg);
            try {
                if (this.mUm.removeUser(userId)) {
                    System.out.println("Success: removed user");
                    return 0;
                }
                System.err.println("Error: couldn't remove user id " + userId);
                return 1;
            } catch (RemoteException e) {
                System.err.println(e.toString());
                System.err.println(PM_NOT_RUNNING_ERR);
                return 1;
            }
        } catch (NumberFormatException e2) {
            System.err.println("Error: user id '" + arg + "' is not a number.");
            return 1;
        }
    }

    public int runGetMaxUsers() {
        System.out.println("Maximum supported users: " + UserManager.getMaxSupportedUsers());
        return 0;
    }

    public int runForceDexOpt() {
        try {
            this.mPm.forceDexOpt(nextArg());
            return 0;
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int runMovePackage() {
        String packageName = nextArg();
        String volumeUuid = nextArg();
        if ("internal".equals(volumeUuid)) {
            volumeUuid = null;
        }
        try {
            int moveId = this.mPm.movePackage(packageName, volumeUuid);
            int status = this.mPm.getMoveStatus(moveId);
            while (!PackageManager.isMoveStatusFinished(status)) {
                SystemClock.sleep(1000);
                status = this.mPm.getMoveStatus(moveId);
            }
            if (status == -100) {
                System.out.println("Success");
                return 0;
            }
            System.err.println("Failure [" + status + "]");
            return 1;
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int runMovePrimaryStorage() {
        String volumeUuid = nextArg();
        if ("internal".equals(volumeUuid)) {
            volumeUuid = null;
        }
        try {
            int moveId = this.mPm.movePrimaryStorage(volumeUuid);
            int status = this.mPm.getMoveStatus(moveId);
            while (!PackageManager.isMoveStatusFinished(status)) {
                SystemClock.sleep(1000);
                status = this.mPm.getMoveStatus(moveId);
            }
            if (status == -100) {
                System.out.println("Success");
                return 0;
            }
            System.err.println("Failure [" + status + "]");
            return 1;
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public int runSetUserRestriction() {
        String arg;
        boolean value;
        int userId = 0;
        String opt = nextOption();
        if (opt != null && "--user".equals(opt)) {
            arg = nextArg();
            if (arg == null || (isNumber(arg) ^ 1) != 0) {
                System.err.println("Error: valid userId not specified");
                return 1;
            }
            userId = Integer.parseInt(arg);
        }
        String restriction = nextArg();
        arg = nextArg();
        if ("1".equals(arg)) {
            value = true;
        } else if ("0".equals(arg)) {
            value = false;
        } else {
            System.err.println("Error: valid value not specified");
            return 1;
        }
        try {
            this.mUm.setUserRestriction(restriction, value, userId);
            return 0;
        } catch (RemoteException e) {
            System.err.println(e.toString());
            return 1;
        }
    }

    private int runClear() {
        int userId = 0;
        String option = nextOption();
        if (option != null && option.equals("--user")) {
            String optionData = nextOptionData();
            if (optionData == null || (isNumber(optionData) ^ 1) != 0) {
                System.err.println("Error: no USER_ID specified");
                return showUsage();
            }
            userId = Integer.parseInt(optionData);
        }
        String pkg = nextArg();
        if (pkg == null) {
            System.err.println("Error: no package specified");
            return showUsage();
        }
        ClearDataObserver obs = new ClearDataObserver();
        try {
            ActivityManager.getService().clearApplicationUserData(pkg, obs, userId);
            synchronized (obs) {
                while (!obs.finished) {
                    try {
                        obs.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            if (obs.result) {
                System.out.println("Success");
                return 0;
            }
            System.err.println("Failed");
            return 1;
        } catch (RemoteException e2) {
            System.err.println(e2.toString());
            System.err.println(PM_NOT_RUNNING_ERR);
            return 1;
        }
    }

    private static String enabledSettingToString(int state) {
        switch (state) {
            case 0:
                return "default";
            case 1:
                return "enabled";
            case 2:
                return "disabled";
            case 3:
                return "disabled-user";
            case 4:
                return "disabled-until-used";
            default:
                return "unknown";
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

    private int runSetEnabledSetting(int state) {
        int userId = 0;
        String option = nextOption();
        if (option != null && option.equals("--user")) {
            String optionData = nextOptionData();
            if (optionData == null || (isNumber(optionData) ^ 1) != 0) {
                System.err.println("Error: no USER_ID specified");
                return showUsage();
            }
            userId = Integer.parseInt(optionData);
        }
        String pkg = nextArg();
        if (pkg == null) {
            System.err.println("Error: no package or component specified");
            return showUsage();
        }
        ComponentName cn = ComponentName.unflattenFromString(pkg);
        if (cn == null) {
            try {
                this.mPm.setApplicationEnabledSetting(pkg, state, 0, userId, "shell:" + Process.myUid());
                System.out.println("Package " + pkg + " new state: " + enabledSettingToString(this.mPm.getApplicationEnabledSetting(pkg, userId)));
                return 0;
            } catch (RemoteException e) {
                System.err.println(e.toString());
                System.err.println(PM_NOT_RUNNING_ERR);
                return 1;
            }
        }
        try {
            this.mPm.setComponentEnabledSetting(cn, state, 0, userId);
            System.out.println("Component " + cn.toShortString() + " new state: " + enabledSettingToString(this.mPm.getComponentEnabledSetting(cn, userId)));
            return 0;
        } catch (RemoteException e2) {
            System.err.println(e2.toString());
            System.err.println(PM_NOT_RUNNING_ERR);
            return 1;
        }
    }

    private int runSetHiddenSetting(boolean state) {
        int userId = 0;
        String option = nextOption();
        if (option != null && option.equals("--user")) {
            String optionData = nextOptionData();
            if (optionData == null || (isNumber(optionData) ^ 1) != 0) {
                System.err.println("Error: no USER_ID specified");
                return showUsage();
            }
            userId = Integer.parseInt(optionData);
        }
        String pkg = nextArg();
        if (pkg == null) {
            System.err.println("Error: no package or component specified");
            return showUsage();
        }
        try {
            this.mPm.setApplicationHiddenSettingAsUser(pkg, state, userId);
            System.out.println("Package " + pkg + " new hidden state: " + this.mPm.getApplicationHiddenSettingAsUser(pkg, userId));
            return 0;
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(PM_NOT_RUNNING_ERR);
            return 1;
        }
    }

    private int runGrantRevokePermission(boolean grant) {
        int userId = 0;
        while (true) {
            String opt = nextOption();
            if (opt == null) {
                break;
            } else if (opt.equals("--user")) {
                userId = Integer.parseInt(nextArg());
            }
        }
        String pkg = nextArg();
        if (pkg == null) {
            System.err.println("Error: no package specified");
            return showUsage();
        }
        String perm = nextArg();
        if (perm == null) {
            System.err.println("Error: no permission specified");
            return showUsage();
        }
        if (grant) {
            try {
                this.mPm.grantRuntimePermission(pkg, perm, userId);
            } catch (RemoteException e) {
                System.err.println(e.toString());
                System.err.println(PM_NOT_RUNNING_ERR);
                return 1;
            } catch (IllegalArgumentException e2) {
                System.err.println("Bad argument: " + e2.toString());
                return showUsage();
            } catch (SecurityException e3) {
                System.err.println("Operation not allowed: " + e3.toString());
                return 1;
            }
        }
        this.mPm.revokeRuntimePermission(pkg, perm, userId);
        return 0;
    }

    private int runResetPermissions() {
        try {
            this.mPm.resetRuntimePermissions();
            return 0;
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(PM_NOT_RUNNING_ERR);
            return 1;
        } catch (IllegalArgumentException e2) {
            System.err.println("Bad argument: " + e2.toString());
            return showUsage();
        } catch (SecurityException e3) {
            System.err.println("Operation not allowed: " + e3.toString());
            return 1;
        }
    }

    private int runSetPermissionEnforced() {
        String permission = nextArg();
        if (permission == null) {
            System.err.println("Error: no permission specified");
            return showUsage();
        }
        String enforcedRaw = nextArg();
        if (enforcedRaw == null) {
            System.err.println("Error: no enforcement specified");
            return showUsage();
        }
        try {
            this.mPm.setPermissionEnforced(permission, Boolean.parseBoolean(enforcedRaw));
            return 0;
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(PM_NOT_RUNNING_ERR);
            return 1;
        } catch (IllegalArgumentException e2) {
            System.err.println("Bad argument: " + e2.toString());
            return showUsage();
        } catch (SecurityException e3) {
            System.err.println("Operation not allowed: " + e3.toString());
            return 1;
        }
    }

    private int runTrimCaches() {
        String size = nextArg();
        if (size == null) {
            System.err.println("Error: no size specified");
            return showUsage();
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
                System.err.println("Invalid suffix: " + c);
                return showUsage();
            }
            size = size.substring(0, len - 1);
        }
        try {
            long sizeVal = Long.parseLong(size) * multiplier;
            String volumeUuid = nextArg();
            if ("internal".equals(volumeUuid)) {
                volumeUuid = null;
            }
            ClearDataObserver obs = new ClearDataObserver();
            try {
                this.mPm.freeStorageAndNotify(volumeUuid, sizeVal, 2, obs);
                synchronized (obs) {
                    while (!obs.finished) {
                        try {
                            obs.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
                return 0;
            } catch (RemoteException e2) {
                System.err.println(e2.toString());
                System.err.println(PM_NOT_RUNNING_ERR);
                return 1;
            } catch (IllegalArgumentException e3) {
                System.err.println("Bad argument: " + e3.toString());
                return showUsage();
            } catch (SecurityException e4) {
                System.err.println("Operation not allowed: " + e4.toString());
                return 1;
            }
        } catch (NumberFormatException e5) {
            System.err.println("Error: expected number at: " + size);
            return showUsage();
        }
    }

    private int displayPackageFilePath(String pckg, int userId) {
        try {
            PackageInfo info = this.mPm.getPackageInfo(pckg, 0, userId);
            if (!(info == null || info.applicationInfo == null)) {
                System.out.print("package:");
                System.out.println(info.applicationInfo.sourceDir);
                if (!ArrayUtils.isEmpty(info.applicationInfo.splitSourceDirs)) {
                    for (String splitSourceDir : info.applicationInfo.splitSourceDirs) {
                        System.out.print("package:");
                        System.out.println(splitSourceDir);
                    }
                }
                return 0;
            }
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(PM_NOT_RUNNING_ERR);
        }
        return 1;
    }

    private String nextOption() {
        if (this.mNextArg >= this.mArgs.length) {
            return null;
        }
        String arg = this.mArgs[this.mNextArg];
        if (!arg.startsWith(STDIN_PATH)) {
            return null;
        }
        this.mNextArg++;
        if (arg.equals("--")) {
            return null;
        }
        if (arg.length() <= 1 || arg.charAt(1) == '-') {
            this.mCurArgData = null;
            return arg;
        } else if (arg.length() > 2) {
            this.mCurArgData = arg.substring(2);
            return arg.substring(0, 2);
        } else {
            this.mCurArgData = null;
            return arg;
        }
    }

    private String nextOptionData() {
        if (this.mCurArgData != null) {
            return this.mCurArgData;
        }
        if (this.mNextArg >= this.mArgs.length) {
            return null;
        }
        String data = this.mArgs[this.mNextArg];
        this.mNextArg++;
        return data;
    }

    private String nextArg() {
        if (this.mNextArg >= this.mArgs.length) {
            return null;
        }
        String arg = this.mArgs[this.mNextArg];
        this.mNextArg++;
        return arg;
    }

    private static int showUsage() {
        System.err.println("usage: pm path [--user USER_ID] PACKAGE");
        System.err.println("       pm dump PACKAGE");
        System.err.println("       pm install [-lrtsfd] [-i PACKAGE] [--user USER_ID] [PATH]");
        System.err.println("       pm install-create [-lrtsfdp] [-i PACKAGE] [-S BYTES]");
        System.err.println("               [--install-location 0/1/2]");
        System.err.println("               [--force-uuid internal|UUID]");
        System.err.println("       pm install-write [-S BYTES] SESSION_ID SPLIT_NAME [PATH]");
        System.err.println("       pm install-commit SESSION_ID");
        System.err.println("       pm install-abandon SESSION_ID");
        System.err.println("       pm uninstall [-k] [--user USER_ID] [--versionCode VERSION_CODE] PACKAGE");
        System.err.println("       pm set-installer PACKAGE INSTALLER");
        System.err.println("       pm move-package PACKAGE [internal|UUID]");
        System.err.println("       pm move-primary-storage [internal|UUID]");
        System.err.println("       pm clear [--user USER_ID] PACKAGE");
        System.err.println("       pm enable [--user USER_ID] PACKAGE_OR_COMPONENT");
        System.err.println("       pm disable [--user USER_ID] PACKAGE_OR_COMPONENT");
        System.err.println("       pm disable-user [--user USER_ID] PACKAGE_OR_COMPONENT");
        System.err.println("       pm disable-until-used [--user USER_ID] PACKAGE_OR_COMPONENT");
        System.err.println("       pm default-state [--user USER_ID] PACKAGE_OR_COMPONENT");
        System.err.println("       pm set-user-restriction [--user USER_ID] RESTRICTION VALUE");
        System.err.println("       pm hide [--user USER_ID] PACKAGE_OR_COMPONENT");
        System.err.println("       pm unhide [--user USER_ID] PACKAGE_OR_COMPONENT");
        System.err.println("       pm grant [--user USER_ID] PACKAGE PERMISSION");
        System.err.println("       pm revoke [--user USER_ID] PACKAGE PERMISSION");
        System.err.println("       pm reset-permissions");
        System.err.println("       pm set-app-link [--user USER_ID] PACKAGE {always|ask|never|undefined}");
        System.err.println("       pm get-app-link [--user USER_ID] PACKAGE");
        System.err.println("       pm set-install-location [0/auto] [1/internal] [2/external]");
        System.err.println("       pm get-install-location");
        System.err.println("       pm set-permission-enforced PERMISSION [true|false]");
        System.err.println("       pm trim-caches DESIRED_FREE_SPACE [internal|UUID]");
        System.err.println("       pm create-user [--profileOf USER_ID] [--managed] [--restricted] [--ephemeral] [--guest] USER_NAME");
        System.err.println("       pm remove-user USER_ID");
        System.err.println("       pm get-max-users");
        System.err.println("");
        System.err.println("NOTE: 'pm list' commands have moved! Run 'adb shell cmd package'");
        System.err.println("  to display the new commands.");
        System.err.println("");
        System.err.println("pm path: print the path to the .apk of the given PACKAGE.");
        System.err.println("");
        System.err.println("pm dump: print system state associated with the given PACKAGE.");
        System.err.println("");
        System.err.println("pm install: install a single legacy package");
        System.err.println("pm install-create: create an install session");
        System.err.println("    -l: forward lock application");
        System.err.println("    -r: replace existing application");
        System.err.println("    -t: allow test packages");
        System.err.println("    -i: specify the installer package name");
        System.err.println("    -s: install application on sdcard");
        System.err.println("    -f: install application on internal flash");
        System.err.println("    -d: allow version code downgrade (debuggable packages only)");
        System.err.println("    -p: partial application install");
        System.err.println("    -g: grant all runtime permissions");
        System.err.println("    -S: size in bytes of entire session");
        System.err.println("");
        System.err.println("pm install-write: write a package into existing session; path may");
        System.err.println("  be '-' to read from stdin");
        System.err.println("    -S: size in bytes of package, required for stdin");
        System.err.println("");
        System.err.println("pm install-commit: perform install of fully staged session");
        System.err.println("pm install-abandon: abandon session");
        System.err.println("");
        System.err.println("pm set-installer: set installer package name");
        System.err.println("");
        System.err.println("pm uninstall: removes a package from the system. Options:");
        System.err.println("    -k: keep the data and cache directories around after package removal.");
        System.err.println("");
        System.err.println("pm clear: deletes all data associated with a package.");
        System.err.println("");
        System.err.println("pm enable, disable, disable-user, disable-until-used, default-state:");
        System.err.println("  these commands change the enabled state of a given package or");
        System.err.println("  component (written as \"package/class\").");
        System.err.println("");
        System.err.println("pm grant, revoke: these commands either grant or revoke permissions");
        System.err.println("    to apps. The permissions must be declared as used in the app's");
        System.err.println("    manifest, be runtime permissions (protection level dangerous),");
        System.err.println("    and the app targeting SDK greater than Lollipop MR1.");
        System.err.println("");
        System.err.println("pm reset-permissions: revert all runtime permissions to their default state.");
        System.err.println("");
        System.err.println("pm get-install-location: returns the current install location.");
        System.err.println("    0 [auto]: Let system decide the best location");
        System.err.println("    1 [internal]: Install on internal device storage");
        System.err.println("    2 [external]: Install on external media");
        System.err.println("");
        System.err.println("pm set-install-location: changes the default install location.");
        System.err.println("  NOTE: this is only intended for debugging; using this can cause");
        System.err.println("  applications to break and other undersireable behavior.");
        System.err.println("    0 [auto]: Let system decide the best location");
        System.err.println("    1 [internal]: Install on internal device storage");
        System.err.println("    2 [external]: Install on external media");
        System.err.println("");
        System.err.println("pm trim-caches: trim cache files to reach the given free space.");
        System.err.println("");
        System.err.println("pm create-user: create a new user with the given USER_NAME,");
        System.err.println("  printing the new user identifier of the user.");
        System.err.println("");
        System.err.println("pm remove-user: remove the user with the given USER_IDENTIFIER,");
        System.err.println("  deleting all data associated with that user");
        System.err.println("");
        return 1;
    }
}

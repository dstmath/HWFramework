package com.android.commands.bmgr;

import android.app.backup.BackupProgress;
import android.app.backup.IBackupManager;
import android.app.backup.IBackupObserver;
import android.app.backup.IRestoreObserver;
import android.app.backup.IRestoreSession;
import android.app.backup.ISelectBackupTransportCallback;
import android.app.backup.RestoreSet;
import android.content.ComponentName;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.ArraySet;
import com.android.internal.annotations.GuardedBy;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public final class Bmgr {
    static final String BMGR_NOT_RUNNING_ERR = "Error: Could not access the Backup Manager.  Is the system running?";
    static final String PM_NOT_RUNNING_ERR = "Error: Could not access the Package Manager.  Is the system running?";
    static final String TRANSPORT_NOT_RUNNING_ERR = "Error: Could not access the backup transport.  Is the system running?";
    private String[] mArgs;
    IBackupManager mBmgr;
    private int mNextArg;
    IRestoreSession mRestore;

    class BackupObserver extends Observer {
        BackupObserver() {
            super();
        }

        public void onUpdate(String currentPackage, BackupProgress backupProgress) {
            super.onUpdate(currentPackage, backupProgress);
            PrintStream printStream = System.out;
            printStream.println("Package " + currentPackage + " with progress: " + backupProgress.bytesTransferred + "/" + backupProgress.bytesExpected);
        }

        public void onResult(String currentPackage, int status) {
            super.onResult(currentPackage, status);
            PrintStream printStream = System.out;
            printStream.println("Package " + currentPackage + " with result: " + Bmgr.convertBackupStatusToString(status));
        }

        public void backupFinished(int status) {
            super.backupFinished(status);
            PrintStream printStream = System.out;
            printStream.println("Backup finished with result: " + Bmgr.convertBackupStatusToString(status));
            if (status == -2003) {
                System.out.println("Backups can be cancelled if a backup is already running, check backup dumpsys");
            }
        }
    }

    class InitObserver extends Observer {
        public int result = -1000;

        InitObserver() {
            super();
        }

        public void backupFinished(int status) {
            super.backupFinished(status);
            this.result = status;
        }
    }

    abstract class Observer extends IBackupObserver.Stub {
        @GuardedBy("trigger")
        private volatile boolean done = false;
        private final Object trigger = new Object();

        Observer() {
        }

        public void onUpdate(String currentPackage, BackupProgress backupProgress) {
        }

        public void onResult(String currentPackage, int status) {
        }

        public void backupFinished(int status) {
            synchronized (this.trigger) {
                this.done = true;
                this.trigger.notify();
            }
        }

        public boolean done() {
            return this.done;
        }

        public void waitForCompletion() {
            waitForCompletion(0);
        }

        public void waitForCompletion(long timeout) {
            long targetTime = SystemClock.elapsedRealtime() + timeout;
            synchronized (this.trigger) {
                while (!this.done && (timeout <= 0 || SystemClock.elapsedRealtime() < targetTime)) {
                    try {
                        this.trigger.wait(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    class RestoreObserver extends IRestoreObserver.Stub {
        boolean done;
        RestoreSet[] sets = null;

        RestoreObserver() {
        }

        public void restoreSetsAvailable(RestoreSet[] result) {
            synchronized (this) {
                this.sets = result;
                this.done = true;
                notify();
            }
        }

        public void restoreStarting(int numPackages) {
            PrintStream printStream = System.out;
            printStream.println("restoreStarting: " + numPackages + " packages");
        }

        public void onUpdate(int nowBeingRestored, String currentPackage) {
            PrintStream printStream = System.out;
            printStream.println("onUpdate: " + nowBeingRestored + " = " + currentPackage);
        }

        public void restoreFinished(int error) {
            PrintStream printStream = System.out;
            printStream.println("restoreFinished: " + error);
            synchronized (this) {
                this.done = true;
                notify();
            }
        }

        public void waitForCompletion() {
            synchronized (this) {
                while (!this.done) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
                this.done = false;
            }
        }
    }

    public static void main(String[] args) {
        try {
            new Bmgr().run(args);
        } catch (Exception e) {
            System.err.println("Exception caught:");
            e.printStackTrace();
        }
    }

    public void run(String[] args) {
        if (args.length < 1) {
            showUsage();
            return;
        }
        this.mBmgr = IBackupManager.Stub.asInterface(ServiceManager.getService("backup"));
        if (this.mBmgr == null) {
            System.err.println(BMGR_NOT_RUNNING_ERR);
            return;
        }
        this.mArgs = args;
        String op = args[0];
        this.mNextArg = 1;
        if ("enabled".equals(op)) {
            doEnabled();
        } else if ("enable".equals(op)) {
            doEnable();
        } else if ("run".equals(op)) {
            doRun();
        } else if ("backup".equals(op)) {
            doBackup();
        } else if ("init".equals(op)) {
            doInit();
        } else if ("list".equals(op)) {
            doList();
        } else if ("restore".equals(op)) {
            doRestore();
        } else if ("transport".equals(op)) {
            doTransport();
        } else if ("wipe".equals(op)) {
            doWipe();
        } else if ("fullbackup".equals(op)) {
            doFullTransportBackup();
        } else if ("backupnow".equals(op)) {
            doBackupNow();
        } else if ("cancel".equals(op)) {
            doCancel();
        } else if ("whitelist".equals(op)) {
            doPrintWhitelist();
        } else {
            System.err.println("Unknown command");
            showUsage();
        }
    }

    private String enableToString(boolean enabled) {
        return enabled ? "enabled" : "disabled";
    }

    private void doEnabled() {
        try {
            boolean isEnabled = this.mBmgr.isBackupEnabled();
            PrintStream printStream = System.out;
            printStream.println("Backup Manager currently " + enableToString(isEnabled));
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(BMGR_NOT_RUNNING_ERR);
        }
    }

    private void doEnable() {
        String arg = nextArg();
        if (arg == null) {
            showUsage();
            return;
        }
        try {
            boolean enable = Boolean.parseBoolean(arg);
            this.mBmgr.setBackupEnabled(enable);
            PrintStream printStream = System.out;
            printStream.println("Backup Manager now " + enableToString(enable));
        } catch (NumberFormatException e) {
            showUsage();
        } catch (RemoteException e2) {
            System.err.println(e2.toString());
            System.err.println(BMGR_NOT_RUNNING_ERR);
        }
    }

    private void doRun() {
        try {
            this.mBmgr.backupNow();
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(BMGR_NOT_RUNNING_ERR);
        }
    }

    private void doBackup() {
        String pkg = nextArg();
        if (pkg == null) {
            showUsage();
            return;
        }
        try {
            this.mBmgr.dataChanged(pkg);
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(BMGR_NOT_RUNNING_ERR);
        }
    }

    private void doFullTransportBackup() {
        System.out.println("Performing full transport backup");
        ArraySet<String> allPkgs = new ArraySet<>();
        while (true) {
            String nextArg = nextArg();
            String pkg = nextArg;
            if (nextArg == null) {
                break;
            }
            allPkgs.add(pkg);
        }
        if (allPkgs.size() > 0) {
            try {
                this.mBmgr.fullTransportBackup((String[]) allPkgs.toArray(new String[allPkgs.size()]));
            } catch (RemoteException e) {
                System.err.println(e.toString());
                System.err.println(BMGR_NOT_RUNNING_ERR);
            }
        }
    }

    /* access modifiers changed from: private */
    public static String convertBackupStatusToString(int errorCode) {
        if (errorCode == -1005) {
            return "Size quota exceeded";
        }
        if (errorCode == -1000) {
            return "Transport error";
        }
        if (errorCode == 0) {
            return "Success";
        }
        switch (errorCode) {
            case -2003:
                return "Backup cancelled";
            case -2002:
                return "Package not found";
            case -2001:
                return "Backup is not allowed";
            default:
                switch (errorCode) {
                    case -1003:
                        return "Agent error";
                    case -1002:
                        return "Transport rejected package because it wasn't able to process it at the time";
                    default:
                        return "Unknown error";
                }
        }
    }

    private void backupNowAllPackages(boolean nonIncrementalBackup) {
        IPackageManager mPm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (mPm == null) {
            System.err.println(PM_NOT_RUNNING_ERR);
            return;
        }
        List<PackageInfo> installedPackages = null;
        try {
            installedPackages = mPm.getInstalledPackages(0, 0).getList();
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(PM_NOT_RUNNING_ERR);
        }
        if (installedPackages != null) {
            String[] filteredPackages = new String[0];
            try {
                filteredPackages = this.mBmgr.filterAppsEligibleForBackup((String[]) installedPackages.stream().map($$Lambda$Bmgr$q6dkg0QPsICRBdCVDH_xoI7L7DE.INSTANCE).toArray($$Lambda$Bmgr$veRYTIgMXojhOx5oIjGwkqACXg0.INSTANCE));
            } catch (RemoteException e2) {
                System.err.println(e2.toString());
                System.err.println(BMGR_NOT_RUNNING_ERR);
            }
            backupNowPackages(Arrays.asList(filteredPackages), nonIncrementalBackup);
        }
    }

    static /* synthetic */ String[] lambda$backupNowAllPackages$1(int x$0) {
        return new String[x$0];
    }

    private void backupNowPackages(List<String> packages, boolean nonIncrementalBackup) {
        int flags = 0;
        if (nonIncrementalBackup) {
            flags = 0 | 1;
        }
        try {
            BackupObserver observer = new BackupObserver();
            if (this.mBmgr.requestBackup((String[]) packages.toArray(new String[packages.size()]), observer, null, flags) == 0) {
                observer.waitForCompletion();
            } else {
                System.err.println("Unable to run backup");
            }
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(BMGR_NOT_RUNNING_ERR);
        }
    }

    private void doBackupNow() {
        boolean backupAll = false;
        boolean nonIncrementalBackup = false;
        ArrayList<String> allPkgs = new ArrayList<>();
        while (true) {
            String nextArg = nextArg();
            String pkg = nextArg;
            if (nextArg == null) {
                break;
            } else if (pkg.equals("--all")) {
                backupAll = true;
            } else if (pkg.equals("--non-incremental")) {
                nonIncrementalBackup = true;
            } else if (pkg.equals("--incremental")) {
                nonIncrementalBackup = false;
            } else if (!allPkgs.contains(pkg)) {
                allPkgs.add(pkg);
            }
        }
        if (backupAll) {
            if (allPkgs.size() == 0) {
                PrintStream printStream = System.out;
                StringBuilder sb = new StringBuilder();
                sb.append("Running ");
                sb.append(nonIncrementalBackup ? "non-" : "");
                sb.append("incremental backup for all packages.");
                printStream.println(sb.toString());
                backupNowAllPackages(nonIncrementalBackup);
                return;
            }
            System.err.println("Provide only '--all' flag or list of packages.");
        } else if (allPkgs.size() > 0) {
            PrintStream printStream2 = System.out;
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Running ");
            sb2.append(nonIncrementalBackup ? "non-" : "");
            sb2.append("incremental backup for ");
            sb2.append(allPkgs.size());
            sb2.append(" requested packages.");
            printStream2.println(sb2.toString());
            backupNowPackages(allPkgs, nonIncrementalBackup);
        } else {
            System.err.println("Provide '--all' flag or list of packages.");
        }
    }

    private void doCancel() {
        if ("backups".equals(nextArg())) {
            try {
                this.mBmgr.cancelBackups();
            } catch (RemoteException e) {
                System.err.println(e.toString());
                System.err.println(BMGR_NOT_RUNNING_ERR);
            }
            return;
        }
        System.err.println("Unknown command.");
    }

    private void doTransport() {
        try {
            String which = nextArg();
            if (which == null) {
                showUsage();
            } else if ("-c".equals(which)) {
                doTransportByComponent();
            } else {
                String old = this.mBmgr.selectBackupTransport(which);
                if (old == null) {
                    PrintStream printStream = System.out;
                    printStream.println("Unknown transport '" + which + "' specified; no changes made.");
                } else {
                    PrintStream printStream2 = System.out;
                    printStream2.println("Selected transport " + which + " (formerly " + old + ")");
                }
            }
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(BMGR_NOT_RUNNING_ERR);
        }
    }

    private void doTransportByComponent() {
        String which = nextArg();
        if (which == null) {
            showUsage();
            return;
        }
        final CountDownLatch latch = new CountDownLatch(1);
        try {
            this.mBmgr.selectBackupTransportAsync(ComponentName.unflattenFromString(which), new ISelectBackupTransportCallback.Stub() {
                public void onSuccess(String transportName) {
                    PrintStream printStream = System.out;
                    printStream.println("Success. Selected transport: " + transportName);
                    latch.countDown();
                }

                public void onFailure(int reason) {
                    PrintStream printStream = System.err;
                    printStream.println("Failure. error=" + reason);
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                System.err.println("Operation interrupted.");
            }
        } catch (RemoteException e2) {
            System.err.println(e2.toString());
            System.err.println(BMGR_NOT_RUNNING_ERR);
        }
    }

    private void doWipe() {
        String transport = nextArg();
        if (transport == null) {
            showUsage();
            return;
        }
        String pkg = nextArg();
        if (pkg == null) {
            showUsage();
            return;
        }
        try {
            this.mBmgr.clearBackupData(transport, pkg);
            PrintStream printStream = System.out;
            printStream.println("Wiped backup data for " + pkg + " on " + transport);
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(BMGR_NOT_RUNNING_ERR);
        }
    }

    private void doInit() {
        ArraySet<String> transports = new ArraySet<>();
        while (true) {
            String nextArg = nextArg();
            String transport = nextArg;
            if (nextArg == null) {
                break;
            }
            transports.add(transport);
        }
        if (transports.size() == 0) {
            showUsage();
            return;
        }
        InitObserver observer = new InitObserver();
        try {
            PrintStream printStream = System.out;
            printStream.println("Initializing transports: " + transports);
            this.mBmgr.initializeTransports((String[]) transports.toArray(new String[transports.size()]), observer);
            observer.waitForCompletion(30000);
            PrintStream printStream2 = System.out;
            printStream2.println("Initialization result: " + observer.result);
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(BMGR_NOT_RUNNING_ERR);
        }
    }

    private void doList() {
        String arg = nextArg();
        if ("transports".equals(arg)) {
            doListTransports();
            return;
        }
        try {
            this.mRestore = this.mBmgr.beginRestoreSession(null, null);
            if (this.mRestore == null) {
                System.err.println(BMGR_NOT_RUNNING_ERR);
                return;
            }
            if ("sets".equals(arg)) {
                doListRestoreSets();
            } else if ("transports".equals(arg)) {
                doListTransports();
            }
            this.mRestore.endRestoreSession();
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(BMGR_NOT_RUNNING_ERR);
        }
    }

    private void doListTransports() {
        try {
            int i = 0;
            if ("-c".equals(nextArg())) {
                ComponentName[] listAllTransportComponents = this.mBmgr.listAllTransportComponents();
                int length = listAllTransportComponents.length;
                while (i < length) {
                    System.out.println(listAllTransportComponents[i].flattenToShortString());
                    i++;
                }
                return;
            }
            String current = this.mBmgr.getCurrentTransport();
            String[] transports = this.mBmgr.listAllTransports();
            if (transports != null) {
                if (transports.length != 0) {
                    int length2 = transports.length;
                    while (i < length2) {
                        String t = transports[i];
                        String pad = t.equals(current) ? "  * " : "    ";
                        PrintStream printStream = System.out;
                        printStream.println(pad + t);
                        i++;
                    }
                    return;
                }
            }
            System.out.println("No transports available.");
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(BMGR_NOT_RUNNING_ERR);
        }
    }

    private void doListRestoreSets() {
        try {
            RestoreObserver observer = new RestoreObserver();
            if (this.mRestore.getAvailableRestoreSets(observer, null) != 0) {
                System.out.println("Unable to request restore sets");
                return;
            }
            observer.waitForCompletion();
            printRestoreSets(observer.sets);
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(TRANSPORT_NOT_RUNNING_ERR);
        }
    }

    private void printRestoreSets(RestoreSet[] sets) {
        if (sets == null || sets.length == 0) {
            System.out.println("No restore sets");
            return;
        }
        for (RestoreSet s : sets) {
            System.out.println("  " + Long.toHexString(s.token) + " : " + s.name);
        }
    }

    private void doRestore() {
        String arg = nextArg();
        if (arg == null) {
            showUsage();
            return;
        }
        if (arg.indexOf(46) >= 0 || arg.equals("android")) {
            doRestorePackage(arg);
        } else {
            try {
                long token = Long.parseLong(arg, 16);
                HashSet<String> filter = null;
                while (true) {
                    String nextArg = nextArg();
                    String arg2 = nextArg;
                    if (nextArg == null) {
                        break;
                    }
                    if (filter == null) {
                        filter = new HashSet<>();
                    }
                    filter.add(arg2);
                }
                doRestoreAll(token, filter);
            } catch (NumberFormatException e) {
                showUsage();
                return;
            }
        }
        System.out.println("done");
    }

    private void doRestorePackage(String pkg) {
        try {
            this.mRestore = this.mBmgr.beginRestoreSession(pkg, null);
            if (this.mRestore == null) {
                System.err.println(BMGR_NOT_RUNNING_ERR);
                return;
            }
            RestoreObserver observer = new RestoreObserver();
            if (this.mRestore.restorePackage(pkg, observer, null) == 0) {
                observer.waitForCompletion();
            } else {
                PrintStream printStream = System.err;
                printStream.println("Unable to restore package " + pkg);
            }
            this.mRestore.endRestoreSession();
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(BMGR_NOT_RUNNING_ERR);
        }
    }

    private void doRestoreAll(long token, HashSet<String> filter) {
        RestoreSet[] sets;
        long j = token;
        HashSet<String> hashSet = filter;
        RestoreObserver observer = new RestoreObserver();
        boolean didRestore = false;
        try {
            this.mRestore = this.mBmgr.beginRestoreSession(null, null);
            if (this.mRestore == null) {
                System.err.println(BMGR_NOT_RUNNING_ERR);
                return;
            }
            if (this.mRestore.getAvailableRestoreSets(observer, null) == 0) {
                observer.waitForCompletion();
                sets = observer.sets;
                if (sets != null) {
                    int length = sets.length;
                    boolean z = false;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            break;
                        }
                        if (sets[i].token == j) {
                            System.out.println("Scheduling restore: " + s.name);
                            if (hashSet == null) {
                                if (this.mRestore.restoreAll(j, observer, null) == 0) {
                                    z = true;
                                }
                                didRestore = z;
                            } else {
                                String[] names = new String[filter.size()];
                                hashSet.toArray(names);
                                String[] strArr = names;
                                if (this.mRestore.restoreSome(j, observer, null, names) == 0) {
                                    z = true;
                                }
                                didRestore = z;
                            }
                        } else {
                            i++;
                        }
                    }
                }
            } else {
                sets = null;
            }
            if (!didRestore) {
                if (sets != null) {
                    if (sets.length != 0) {
                        System.out.println("No matching restore set token.  Available sets:");
                        printRestoreSets(sets);
                    }
                }
                System.out.println("No available restore sets; no restore performed");
            }
            if (didRestore) {
                observer.waitForCompletion();
            }
            this.mRestore.endRestoreSession();
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(BMGR_NOT_RUNNING_ERR);
        }
    }

    private void doPrintWhitelist() {
        try {
            String[] whitelist = this.mBmgr.getTransportWhitelist();
            if (whitelist != null) {
                for (String transport : whitelist) {
                    System.out.println(transport);
                }
            }
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(BMGR_NOT_RUNNING_ERR);
        }
    }

    private String nextArg() {
        if (this.mNextArg >= this.mArgs.length) {
            return null;
        }
        String arg = this.mArgs[this.mNextArg];
        this.mNextArg++;
        return arg;
    }

    private static void showUsage() {
        System.err.println("usage: bmgr [backup|restore|list|transport|run]");
        System.err.println("       bmgr backup PACKAGE");
        System.err.println("       bmgr enable BOOL");
        System.err.println("       bmgr enabled");
        System.err.println("       bmgr list transports [-c]");
        System.err.println("       bmgr list sets");
        System.err.println("       bmgr transport WHICH|-c WHICH_COMPONENT");
        System.err.println("       bmgr restore TOKEN");
        System.err.println("       bmgr restore TOKEN PACKAGE...");
        System.err.println("       bmgr restore PACKAGE");
        System.err.println("       bmgr run");
        System.err.println("       bmgr wipe TRANSPORT PACKAGE");
        System.err.println("       bmgr fullbackup PACKAGE...");
        System.err.println("       bmgr backupnow --all|PACKAGE...");
        System.err.println("       bmgr cancel backups");
        System.err.println("");
        System.err.println("The 'backup' command schedules a backup pass for the named package.");
        System.err.println("Note that the backup pass will effectively be a no-op if the package");
        System.err.println("does not actually have changed data to store.");
        System.err.println("");
        System.err.println("The 'enable' command enables or disables the entire backup mechanism.");
        System.err.println("If the argument is 'true' it will be enabled, otherwise it will be");
        System.err.println("disabled.  When disabled, neither backup or restore operations will");
        System.err.println("be performed.");
        System.err.println("");
        System.err.println("The 'enabled' command reports the current enabled/disabled state of");
        System.err.println("the backup mechanism.");
        System.err.println("");
        System.err.println("The 'list transports' command reports the names of the backup transports");
        System.err.println("BackupManager is currently bound to. These names can be passed as arguments");
        System.err.println("to the 'transport' and 'wipe' commands.  The currently active transport");
        System.err.println("is indicated with a '*' character. If -c flag is used, all available");
        System.err.println("transport components on the device are listed. These can be used with");
        System.err.println("the component variant of 'transport' command.");
        System.err.println("");
        System.err.println("The 'list sets' command reports the token and name of each restore set");
        System.err.println("available to the device via the currently active transport.");
        System.err.println("");
        System.err.println("The 'transport' command designates the named transport as the currently");
        System.err.println("active one.  This setting is persistent across reboots. If -c flag is");
        System.err.println("specified, the following string is treated as a component name.");
        System.err.println("");
        System.err.println("The 'restore' command when given just a restore token initiates a full-system");
        System.err.println("restore operation from the currently active transport.  It will deliver");
        System.err.println("the restore set designated by the TOKEN argument to each application");
        System.err.println("that had contributed data to that restore set.");
        System.err.println("");
        System.err.println("The 'restore' command when given a token and one or more package names");
        System.err.println("initiates a restore operation of just those given packages from the restore");
        System.err.println("set designated by the TOKEN argument.  It is effectively the same as the");
        System.err.println("'restore' operation supplying only a token, but applies a filter to the");
        System.err.println("set of applications to be restored.");
        System.err.println("");
        System.err.println("The 'restore' command when given just a package name intiates a restore of");
        System.err.println("just that one package according to the restore set selection algorithm");
        System.err.println("used by the RestoreSession.restorePackage() method.");
        System.err.println("");
        System.err.println("The 'run' command causes any scheduled backup operation to be initiated");
        System.err.println("immediately, without the usual waiting period for batching together");
        System.err.println("data changes.");
        System.err.println("");
        System.err.println("The 'wipe' command causes all backed-up data for the given package to be");
        System.err.println("erased from the given transport's storage.  The next backup operation");
        System.err.println("that the given application performs will rewrite its entire data set.");
        System.err.println("Transport names to use here are those reported by 'list transports'.");
        System.err.println("");
        System.err.println("The 'fullbackup' command induces a full-data stream backup for one or more");
        System.err.println("packages.  The data is sent via the currently active transport.");
        System.err.println("");
        System.err.println("The 'backupnow' command runs an immediate backup for one or more packages.");
        System.err.println("    --all flag runs backup for all eligible packages.");
        System.err.println("For each package it will run key/value or full data backup ");
        System.err.println("depending on the package's manifest declarations.");
        System.err.println("The data is sent via the currently active transport.");
        System.err.println("The 'cancel backups' command cancels all running backups.");
    }
}

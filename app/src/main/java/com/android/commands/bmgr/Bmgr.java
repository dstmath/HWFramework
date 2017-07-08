package com.android.commands.bmgr;

import android.app.backup.BackupProgress;
import android.app.backup.IBackupManager;
import android.app.backup.IBackupObserver.Stub;
import android.app.backup.IRestoreObserver;
import android.app.backup.IRestoreSession;
import android.app.backup.RestoreSet;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class Bmgr {
    static final String BMGR_NOT_RUNNING_ERR = "Error: Could not access the Backup Manager.  Is the system running?";
    static final String PM_NOT_RUNNING_ERR = "Error: Could not access the Package Manager.  Is the system running?";
    static final String TRANSPORT_NOT_RUNNING_ERR = "Error: Could not access the backup transport.  Is the system running?";
    private String[] mArgs;
    IBackupManager mBmgr;
    private int mNextArg;
    IRestoreSession mRestore;

    class BackupObserver extends Stub {
        boolean done;

        BackupObserver() {
            this.done = false;
        }

        public void onUpdate(String currentPackage, BackupProgress backupProgress) {
            System.out.println("Package " + currentPackage + " with progress: " + backupProgress.bytesTransferred + "/" + backupProgress.bytesExpected);
        }

        public void onResult(String currentPackage, int status) {
            System.out.println("Package " + currentPackage + " with result: " + Bmgr.convertBackupStatusToString(status));
        }

        public void backupFinished(int status) {
            System.out.println("Backup finished with result: " + Bmgr.convertBackupStatusToString(status));
            synchronized (this) {
                this.done = true;
                notify();
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void waitForCompletion() {
            synchronized (this) {
                while (true) {
                    if (this.done) {
                    } else {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }
    }

    class RestoreObserver extends IRestoreObserver.Stub {
        boolean done;
        RestoreSet[] sets;

        RestoreObserver() {
            this.sets = null;
        }

        public void restoreSetsAvailable(RestoreSet[] result) {
            synchronized (this) {
                this.sets = result;
                this.done = true;
                notify();
            }
        }

        public void restoreStarting(int numPackages) {
            System.out.println("restoreStarting: " + numPackages + " packages");
        }

        public void onUpdate(int nowBeingRestored, String currentPackage) {
            System.out.println("onUpdate: " + nowBeingRestored + " = " + currentPackage);
        }

        public void restoreFinished(int error) {
            System.out.println("restoreFinished: " + error);
            synchronized (this) {
                this.done = true;
                notify();
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void waitForCompletion() {
            synchronized (this) {
                while (true) {
                    if (this.done) {
                    } else {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
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
            System.out.println("Backup Manager currently " + enableToString(this.mBmgr.isBackupEnabled()));
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
            System.out.println("Backup Manager now " + enableToString(enable));
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
        ArrayList<String> allPkgs = new ArrayList();
        while (true) {
            String pkg = nextArg();
            if (pkg == null) {
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

    private static String convertBackupStatusToString(int errorCode) {
        switch (errorCode) {
            case -2002:
                return "Package not found";
            case -2001:
                return "Backup is not allowed";
            case -1005:
                return "Size quota exceeded";
            case -1003:
                return "Agent error";
            case -1002:
                return "Transport rejected package";
            case -1000:
                return "Transport error";
            case 0:
                return "Success";
            default:
                return "Unknown error";
        }
    }

    private void backupNowAllPackages() {
        IPackageManager mPm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (mPm == null) {
            System.err.println(PM_NOT_RUNNING_ERR);
            return;
        }
        Iterable installedPackages = null;
        try {
            installedPackages = mPm.getInstalledPackages(0, 0).getList();
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(PM_NOT_RUNNING_ERR);
        }
        if (r1 != null) {
            List<String> packages = new ArrayList();
            for (PackageInfo pi : r1) {
                try {
                    if (this.mBmgr.isAppEligibleForBackup(pi.packageName)) {
                        packages.add(pi.packageName);
                    }
                } catch (RemoteException e2) {
                    System.err.println(e2.toString());
                    System.err.println(BMGR_NOT_RUNNING_ERR);
                }
            }
            backupNowPackages(packages);
        }
    }

    private void backupNowPackages(List<String> packages) {
        try {
            BackupObserver observer = new BackupObserver();
            if (this.mBmgr.requestBackup((String[]) packages.toArray(new String[packages.size()]), observer) == 0) {
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
        ArrayList<String> allPkgs = new ArrayList();
        while (true) {
            String pkg = nextArg();
            if (pkg == null) {
                break;
            } else if (pkg.equals("--all")) {
                backupAll = true;
            } else {
                allPkgs.add(pkg);
            }
        }
        if (backupAll) {
            if (allPkgs.size() == 0) {
                System.out.println("Running backup for all packages.");
                backupNowAllPackages();
                return;
            }
            System.err.println("Provide only '--all' flag or list of packages.");
        } else if (allPkgs.size() > 0) {
            System.out.println("Running backup for " + allPkgs.size() + " requested packages.");
            backupNowPackages(allPkgs);
        } else {
            System.err.println("Provide '--all' flag or list of packages.");
        }
    }

    private void doTransport() {
        try {
            String which = nextArg();
            if (which == null) {
                showUsage();
                return;
            }
            String old = this.mBmgr.selectBackupTransport(which);
            if (old == null) {
                System.out.println("Unknown transport '" + which + "' specified; no changes made.");
            } else {
                System.out.println("Selected transport " + which + " (formerly " + old + ")");
            }
        } catch (RemoteException e) {
            System.err.println(e.toString());
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
            System.out.println("Wiped backup data for " + pkg + " on " + transport);
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
            String current = this.mBmgr.getCurrentTransport();
            String[] transports = this.mBmgr.listAllTransports();
            if (transports == null || transports.length == 0) {
                System.out.println("No transports available.");
                return;
            }
            for (String t : transports) {
                System.out.println((t.equals(current) ? "  * " : "    ") + t);
            }
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(BMGR_NOT_RUNNING_ERR);
        }
    }

    private void doListRestoreSets() {
        try {
            RestoreObserver observer = new RestoreObserver();
            if (this.mRestore.getAvailableRestoreSets(observer) != 0) {
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
                HashSet hashSet = null;
                while (true) {
                    arg = nextArg();
                    if (arg == null) {
                        break;
                    }
                    if (hashSet == null) {
                        hashSet = new HashSet();
                    }
                    hashSet.add(arg);
                }
                doRestoreAll(token, hashSet);
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
            if (this.mRestore.restorePackage(pkg, observer) == 0) {
                observer.waitForCompletion();
            } else {
                System.err.println("Unable to restore package " + pkg);
            }
            this.mRestore.endRestoreSession();
        } catch (RemoteException e) {
            System.err.println(e.toString());
            System.err.println(BMGR_NOT_RUNNING_ERR);
        }
    }

    private void doRestoreAll(long token, HashSet<String> filter) {
        int i = 0;
        RestoreObserver observer = new RestoreObserver();
        boolean didRestore = false;
        try {
            this.mRestore = this.mBmgr.beginRestoreSession(null, null);
            if (this.mRestore == null) {
                System.err.println(BMGR_NOT_RUNNING_ERR);
                return;
            }
            RestoreSet[] restoreSetArr = null;
            if (this.mRestore.getAvailableRestoreSets(observer) == 0) {
                observer.waitForCompletion();
                restoreSetArr = observer.sets;
                if (restoreSetArr != null) {
                    int length = restoreSetArr.length;
                    while (i < length) {
                        RestoreSet s = restoreSetArr[i];
                        if (s.token == token) {
                            System.out.println("Scheduling restore: " + s.name);
                            if (filter == null) {
                                didRestore = this.mRestore.restoreAll(token, observer) == 0;
                            } else {
                                String[] names = new String[filter.size()];
                                filter.toArray(names);
                                didRestore = this.mRestore.restoreSome(token, observer, names) == 0;
                            }
                        } else {
                            i++;
                        }
                    }
                }
            }
            if (!didRestore) {
                if (restoreSetArr == null || restoreSetArr.length == 0) {
                    System.out.println("No available restore sets; no restore performed");
                } else {
                    System.out.println("No matching restore set token.  Available sets:");
                    printRestoreSets(restoreSetArr);
                }
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
        System.err.println("       bmgr list transports");
        System.err.println("       bmgr list sets");
        System.err.println("       bmgr transport WHICH");
        System.err.println("       bmgr restore TOKEN");
        System.err.println("       bmgr restore TOKEN PACKAGE...");
        System.err.println("       bmgr restore PACKAGE");
        System.err.println("       bmgr run");
        System.err.println("       bmgr wipe TRANSPORT PACKAGE");
        System.err.println("       bmgr fullbackup PACKAGE...");
        System.err.println("       bmgr backupnow --all|PACKAGE...");
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
        System.err.println("currently available on the device.  These names can be passed as arguments");
        System.err.println("to the 'transport' and 'wipe' commands.  The currently active transport");
        System.err.println("is indicated with a '*' character.");
        System.err.println("");
        System.err.println("The 'list sets' command reports the token and name of each restore set");
        System.err.println("available to the device via the currently active transport.");
        System.err.println("");
        System.err.println("The 'transport' command designates the named transport as the currently");
        System.err.println("active one.  This setting is persistent across reboots.");
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
    }
}

package com.android.server.am;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.os.RemoteException;
import android.os.ShellCommand;
import android.os.UserHandle;
import android.util.DebugUtils;
import java.io.PrintWriter;

class ActivityManagerShellCommand extends ShellCommand {
    final boolean mDumping;
    final IActivityManager mInterface;
    final ActivityManagerService mInternal;

    ActivityManagerShellCommand(ActivityManagerService service, boolean dumping) {
        this.mInterface = service;
        this.mInternal = service;
        this.mDumping = dumping;
    }

    public int onCommand(String cmd) {
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter pw = getOutPrintWriter();
        try {
            if (cmd.equals("force-stop")) {
                return runForceStop(pw);
            }
            if (cmd.equals("kill")) {
                return runKill(pw);
            }
            if (cmd.equals("kill-all")) {
                return runKillAll(pw);
            }
            if (cmd.equals("write")) {
                return runWrite(pw);
            }
            if (cmd.equals("track-associations")) {
                return runTrackAssociations(pw);
            }
            if (cmd.equals("untrack-associations")) {
                return runUntrackAssociations(pw);
            }
            if (cmd.equals("is-user-stopped")) {
                return runIsUserStopped(pw);
            }
            if (cmd.equals("lenient-background-check")) {
                return runLenientBackgroundCheck(pw);
            }
            if (cmd.equals("get-uid-state")) {
                return getUidState(pw);
            }
            return handleDefaultCommands(cmd);
        } catch (RemoteException e) {
            pw.println("Remote exception: " + e);
            return -1;
        }
    }

    int runIsUserStopped(PrintWriter pw) {
        pw.println(this.mInternal.isUserStopped(UserHandle.parseUserArg(getNextArgRequired())));
        return 0;
    }

    int runForceStop(PrintWriter pw) throws RemoteException {
        int userId = -1;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                this.mInterface.forceStopPackage(getNextArgRequired(), userId);
                return 0;
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                pw.println("Error: Unknown option: " + opt);
                return -1;
            }
        }
    }

    int runKill(PrintWriter pw) throws RemoteException {
        int userId = -1;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                this.mInterface.killBackgroundProcesses(getNextArgRequired(), userId);
                return 0;
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                pw.println("Error: Unknown option: " + opt);
                return -1;
            }
        }
    }

    int runKillAll(PrintWriter pw) throws RemoteException {
        this.mInterface.killAllBackgroundProcesses();
        return 0;
    }

    int runWrite(PrintWriter pw) {
        this.mInternal.enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "registerUidObserver()");
        this.mInternal.mRecentTasks.flush();
        pw.println("All tasks persisted.");
        return 0;
    }

    int runTrackAssociations(PrintWriter pw) {
        this.mInternal.enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "registerUidObserver()");
        synchronized (this.mInternal) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (this.mInternal.mTrackingAssociations) {
                    pw.println("Association tracking already enabled.");
                } else {
                    this.mInternal.mTrackingAssociations = true;
                    pw.println("Association tracking started.");
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return 0;
    }

    int runUntrackAssociations(PrintWriter pw) {
        this.mInternal.enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "registerUidObserver()");
        synchronized (this.mInternal) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (this.mInternal.mTrackingAssociations) {
                    this.mInternal.mTrackingAssociations = false;
                    this.mInternal.mAssociations.clear();
                    pw.println("Association tracking stopped.");
                } else {
                    pw.println("Association tracking not running.");
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return 0;
    }

    int runLenientBackgroundCheck(PrintWriter pw) throws RemoteException {
        String arg = getNextArg();
        if (arg != null) {
            this.mInterface.setLenientBackgroundCheck(!Boolean.valueOf(arg).booleanValue() ? "1".equals(arg) : true);
        }
        synchronized (this.mInternal) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (this.mInternal.mLenientBackgroundCheck) {
                    pw.println("Lenient background check enabled");
                } else {
                    pw.println("Lenient background check disabled");
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return 0;
    }

    int getUidState(PrintWriter pw) throws RemoteException {
        this.mInternal.enforceCallingPermission("android.permission.DUMP", "getUidState()");
        int state = this.mInternal.getUidState(Integer.parseInt(getNextArgRequired()));
        pw.print(state);
        pw.print(" (");
        pw.printf(DebugUtils.valueToString(ActivityManager.class, "PROCESS_STATE_", state), new Object[0]);
        pw.println(")");
        return 0;
    }

    public void onHelp() {
        dumpHelp(getOutPrintWriter(), this.mDumping);
    }

    static void dumpHelp(PrintWriter pw, boolean dumping) {
        if (dumping) {
            pw.println("Activity manager dump options:");
            pw.println("  [-a] [-c] [-p PACKAGE] [-h] [WHAT] ...");
            pw.println("  WHAT may be one of:");
            pw.println("    a[ctivities]: activity stack state");
            pw.println("    r[recents]: recent activities state");
            pw.println("    b[roadcasts] [PACKAGE_NAME] [history [-s]]: broadcast state");
            pw.println("    broadcast-stats [PACKAGE_NAME]: aggregated broadcast statistics");
            pw.println("    i[ntents] [PACKAGE_NAME]: pending intent state");
            pw.println("    p[rocesses] [PACKAGE_NAME]: process state");
            pw.println("    o[om]: out of memory management");
            pw.println("    perm[issions]: URI permission grant state");
            pw.println("    prov[iders] [COMP_SPEC ...]: content provider state");
            pw.println("    provider [COMP_SPEC]: provider client-side state");
            pw.println("    s[ervices] [COMP_SPEC ...]: service state");
            pw.println("    as[sociations]: tracked app associations");
            pw.println("    service [COMP_SPEC]: service client-side state");
            pw.println("    package [PACKAGE_NAME]: all state related to given package");
            pw.println("    all: dump all activities");
            pw.println("    top: dump the top activity");
            pw.println("  WHAT may also be a COMP_SPEC to dump activities.");
            pw.println("  COMP_SPEC may be a component name (com.foo/.myApp),");
            pw.println("    a partial substring in a component name, a");
            pw.println("    hex object identifier.");
            pw.println("  -a: include all available server state.");
            pw.println("  -c: include client state.");
            pw.println("  -p: limit output to given package.");
            pw.println("  --checkin: output checkin format, resetting data.");
            pw.println("  --C: output checkin format, not resetting data.");
            return;
        }
        pw.println("Activity manager (activity) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("  force-stop [--user <USER_ID> | all | current] <PACKAGE>");
        pw.println("    Completely stop the given application package.");
        pw.println("  kill [--user <USER_ID> | all | current] <PACKAGE>");
        pw.println("    Kill all processes associated with the given application.");
        pw.println("  kill-all");
        pw.println("    Kill all processes that are safe to kill (cached, etc).");
        pw.println("  write");
        pw.println("    Write all pending state to storage.");
        pw.println("  track-associations");
        pw.println("    Enable association tracking.");
        pw.println("  untrack-associations");
        pw.println("    Disable and clear association tracking.");
        pw.println("  is-user-stopped <USER_ID>");
        pw.println("    Returns whether <USER_ID> has been stopped or not.");
        pw.println("  lenient-background-check [<true|false>]");
        pw.println("    Optionally controls lenient background check mode, returns current mode.");
        pw.println("  get-uid-state <UID>");
        pw.println("    Gets the process state of an app given its <UID>.");
    }
}

package com.android.server.role;

import android.app.role.IRoleManager;
import android.os.Bundle;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.ShellCommand;
import android.os.UserHandle;
import android.util.Log;
import com.android.server.role.RoleManagerShellCommand;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/* access modifiers changed from: package-private */
public class RoleManagerShellCommand extends ShellCommand {
    private final IRoleManager mRoleManager;

    RoleManagerShellCommand(IRoleManager roleManager) {
        this.mRoleManager = roleManager;
    }

    /* access modifiers changed from: private */
    public class CallbackFuture extends CompletableFuture<Void> {
        private CallbackFuture() {
        }

        public RemoteCallback createCallback() {
            return new RemoteCallback(new RemoteCallback.OnResultListener() {
                /* class com.android.server.role.$$Lambda$RoleManagerShellCommand$CallbackFuture$ya02agfKUbaiv_zXc0xWEop421Q */

                public final void onResult(Bundle bundle) {
                    RoleManagerShellCommand.CallbackFuture.this.lambda$createCallback$0$RoleManagerShellCommand$CallbackFuture(bundle);
                }
            });
        }

        public /* synthetic */ void lambda$createCallback$0$RoleManagerShellCommand$CallbackFuture(Bundle result) {
            if (result != null) {
                complete(null);
            } else {
                completeExceptionally(new RuntimeException("Failed"));
            }
        }

        public int waitForResult() {
            try {
                get(5, TimeUnit.SECONDS);
                return 0;
            } catch (Exception e) {
                PrintWriter errPrintWriter = RoleManagerShellCommand.this.getErrPrintWriter();
                errPrintWriter.println("Error: see logcat for details.\n" + Log.getStackTraceString(e));
                return -1;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0044 A[Catch:{ RemoteException -> 0x005c }] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0057 A[Catch:{ RemoteException -> 0x005c }] */
    public int onCommand(String cmd) {
        boolean z;
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter pw = getOutPrintWriter();
        try {
            int hashCode = cmd.hashCode();
            if (hashCode != -1831663689) {
                if (hashCode != -1502066320) {
                    if (hashCode == -1274754278 && cmd.equals("remove-role-holder")) {
                        z = true;
                        if (z) {
                            return runAddRoleHolder();
                        }
                        if (z) {
                            return runRemoveRoleHolder();
                        }
                        if (!z) {
                            return handleDefaultCommands(cmd);
                        }
                        return runClearRoleHolders();
                    }
                } else if (cmd.equals("clear-role-holders")) {
                    z = true;
                    if (z) {
                    }
                }
            } else if (cmd.equals("add-role-holder")) {
                z = false;
                if (z) {
                }
            }
            z = true;
            if (z) {
            }
        } catch (RemoteException e) {
            pw.println("Remote exception: " + e);
            return -1;
        }
    }

    private int getUserIdMaybe() {
        String option = getNextOption();
        if (option == null || !option.equals("--user")) {
            return 0;
        }
        return UserHandle.parseUserArg(getNextArgRequired());
    }

    private int getFlagsMaybe() {
        String flags = getNextArg();
        if (flags == null) {
            return 0;
        }
        return Integer.parseInt(flags);
    }

    private int runAddRoleHolder() throws RemoteException {
        int userId = getUserIdMaybe();
        String roleName = getNextArgRequired();
        String packageName = getNextArgRequired();
        int flags = getFlagsMaybe();
        CallbackFuture future = new CallbackFuture();
        this.mRoleManager.addRoleHolderAsUser(roleName, packageName, flags, userId, future.createCallback());
        return future.waitForResult();
    }

    private int runRemoveRoleHolder() throws RemoteException {
        int userId = getUserIdMaybe();
        String roleName = getNextArgRequired();
        String packageName = getNextArgRequired();
        int flags = getFlagsMaybe();
        CallbackFuture future = new CallbackFuture();
        this.mRoleManager.removeRoleHolderAsUser(roleName, packageName, flags, userId, future.createCallback());
        return future.waitForResult();
    }

    private int runClearRoleHolders() throws RemoteException {
        int userId = getUserIdMaybe();
        String roleName = getNextArgRequired();
        int flags = getFlagsMaybe();
        CallbackFuture future = new CallbackFuture();
        this.mRoleManager.clearRoleHoldersAsUser(roleName, flags, userId, future.createCallback());
        return future.waitForResult();
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("Role manager (role) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println();
        pw.println("  add-role-holder [--user USER_ID] ROLE PACKAGE [FLAGS]");
        pw.println("  remove-role-holder [--user USER_ID] ROLE PACKAGE [FLAGS]");
        pw.println("  clear-role-holders [--user USER_ID] ROLE [FLAGS]");
        pw.println();
    }
}

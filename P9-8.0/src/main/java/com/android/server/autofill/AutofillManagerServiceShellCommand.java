package com.android.server.autofill;

import android.os.Bundle;
import android.os.ShellCommand;
import android.os.UserHandle;
import com.android.internal.os.IResultReceiver;
import com.android.internal.os.IResultReceiver.Stub;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class AutofillManagerServiceShellCommand extends ShellCommand {
    private final AutofillManagerService mService;

    public AutofillManagerServiceShellCommand(AutofillManagerService service) {
        this.mService = service;
    }

    public int onCommand(String cmd) {
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter pw = getOutPrintWriter();
        if (cmd.equals("list")) {
            return requestList(pw);
        }
        if (cmd.equals("destroy")) {
            return requestDestroy(pw);
        }
        if (cmd.equals("reset")) {
            return requestReset();
        }
        if (cmd.equals("get")) {
            return requestGet(pw);
        }
        if (cmd.equals("set")) {
            return requestSet(pw);
        }
        return handleDefaultCommands(cmd);
    }

    public void onHelp() {
        Throwable th;
        Throwable th2 = null;
        PrintWriter printWriter = null;
        try {
            printWriter = getOutPrintWriter();
            printWriter.println("AutoFill Service (autofill) commands:");
            printWriter.println("  help");
            printWriter.println("    Prints this help text.");
            printWriter.println("");
            printWriter.println("  get log_level ");
            printWriter.println("    Gets the Autofill log level (off | debug | verbose).");
            printWriter.println("");
            printWriter.println("  get max_partitions");
            printWriter.println("    Gets the maximum number of partitions per session.");
            printWriter.println("");
            printWriter.println("  set log_level [off | debug | verbose]");
            printWriter.println("    Sets the Autofill log level.");
            printWriter.println("");
            printWriter.println("  set max_partitions number");
            printWriter.println("    Sets the maximum number of partitions per session.");
            printWriter.println("");
            printWriter.println("  list sessions [--user USER_ID]");
            printWriter.println("    List all pending sessions.");
            printWriter.println("");
            printWriter.println("  destroy sessions [--user USER_ID]");
            printWriter.println("    Destroy all pending sessions.");
            printWriter.println("");
            printWriter.println("  reset");
            printWriter.println("    Reset all pending sessions and cached service connections.");
            printWriter.println("");
            if (printWriter != null) {
                try {
                    printWriter.close();
                } catch (Throwable th3) {
                    th2 = th3;
                }
            }
            if (th2 != null) {
                throw th2;
            }
            return;
        } catch (Throwable th22) {
            Throwable th4 = th22;
            th22 = th;
            th = th4;
        }
        if (printWriter != null) {
            try {
                printWriter.close();
            } catch (Throwable th5) {
                if (th22 == null) {
                    th22 = th5;
                } else if (th22 != th5) {
                    th22.addSuppressed(th5);
                }
            }
        }
        if (th22 != null) {
            throw th22;
        }
        throw th;
    }

    private int requestGet(PrintWriter pw) {
        String what = getNextArgRequired();
        if (what.equals("log_level")) {
            return getLogLevel(pw);
        }
        if (what.equals("max_partitions")) {
            return getMaxPartitions(pw);
        }
        pw.println("Invalid set: " + what);
        return -1;
    }

    private int requestSet(PrintWriter pw) {
        String what = getNextArgRequired();
        if (what.equals("log_level")) {
            return setLogLevel(pw);
        }
        if (what.equals("max_partitions")) {
            return setMaxPartitions();
        }
        pw.println("Invalid set: " + what);
        return -1;
    }

    private int getLogLevel(PrintWriter pw) {
        int logLevel = this.mService.getLogLevel();
        switch (logLevel) {
            case 0:
                pw.println("off");
                return 0;
            case 2:
                pw.println("debug");
                return 0;
            case 4:
                pw.println("verbose");
                return 0;
            default:
                pw.println("unknow (" + logLevel + ")");
                return 0;
        }
    }

    private int setLogLevel(PrintWriter pw) {
        String logLevel = getNextArgRequired();
        String toLowerCase = logLevel.toLowerCase();
        if (toLowerCase.equals("verbose")) {
            this.mService.setLogLevel(4);
            return 0;
        } else if (toLowerCase.equals("debug")) {
            this.mService.setLogLevel(2);
            return 0;
        } else if (toLowerCase.equals("off")) {
            this.mService.setLogLevel(0);
            return 0;
        } else {
            pw.println("Invalid level: " + logLevel);
            return -1;
        }
    }

    private int getMaxPartitions(PrintWriter pw) {
        pw.println(this.mService.getMaxPartitions());
        return 0;
    }

    private int setMaxPartitions() {
        this.mService.setMaxPartitions(Integer.parseInt(getNextArgRequired()));
        return 0;
    }

    private int requestDestroy(PrintWriter pw) {
        if (!isNextArgSessions(pw)) {
            return -1;
        }
        int userId = getUserIdFromArgsOrAllUsers();
        final CountDownLatch latch = new CountDownLatch(1);
        return requestSessionCommon(pw, latch, new -$Lambda$mpPqaCtNJERkwd7tRkFrIaSM3WQ(userId, this, new Stub() {
            public void send(int resultCode, Bundle resultData) {
                latch.countDown();
            }
        }));
    }

    /* synthetic */ void lambda$-com_android_server_autofill_AutofillManagerServiceShellCommand_6061(int userId, IResultReceiver receiver) {
        this.mService.destroySessions(userId, receiver);
    }

    private int requestList(final PrintWriter pw) {
        if (!isNextArgSessions(pw)) {
            return -1;
        }
        int userId = getUserIdFromArgsOrAllUsers();
        final CountDownLatch latch = new CountDownLatch(1);
        return requestSessionCommon(pw, latch, new com.android.server.autofill.-$Lambda$mpPqaCtNJERkwd7tRkFrIaSM3WQ.AnonymousClass1(userId, this, new Stub() {
            public void send(int resultCode, Bundle resultData) {
                for (String session : resultData.getStringArrayList("sessions")) {
                    pw.println(session);
                }
                latch.countDown();
            }
        }));
    }

    /* synthetic */ void lambda$-com_android_server_autofill_AutofillManagerServiceShellCommand_6868(int userId, IResultReceiver receiver) {
        this.mService.listSessions(userId, receiver);
    }

    private boolean isNextArgSessions(PrintWriter pw) {
        if (getNextArgRequired().equals("sessions")) {
            return true;
        }
        pw.println("Error: invalid list type");
        return false;
    }

    private boolean isNextArgLogLevel(PrintWriter pw, String cmd) {
        String type = getNextArgRequired();
        if (type.equals("log_level")) {
            return true;
        }
        pw.println("Error: invalid " + cmd + " type: " + type);
        return false;
    }

    private int requestSessionCommon(PrintWriter pw, CountDownLatch latch, Runnable command) {
        command.run();
        try {
            if (latch.await(5, TimeUnit.SECONDS)) {
                return 0;
            }
            pw.println("Timed out after 5 seconds");
            return -1;
        } catch (InterruptedException e) {
            pw.println("System call interrupted");
            Thread.currentThread().interrupt();
            return -1;
        }
    }

    private int requestReset() {
        this.mService.reset();
        return 0;
    }

    private int getUserIdFromArgsOrAllUsers() {
        if ("--user".equals(getNextArg())) {
            return UserHandle.parseUserArg(getNextArgRequired());
        }
        return -1;
    }
}

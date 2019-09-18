package com.android.server.autofill;

import android.os.Bundle;
import android.os.RemoteCallback;
import android.os.ShellCommand;
import android.os.UserHandle;
import android.service.autofill.AutofillFieldClassificationService;
import com.android.internal.os.IResultReceiver;
import com.android.server.BatteryService;
import java.io.PrintWriter;
import java.util.Iterator;
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
        char c = 65535;
        switch (cmd.hashCode()) {
            case 102230:
                if (cmd.equals("get")) {
                    c = 3;
                    break;
                }
                break;
            case 113762:
                if (cmd.equals("set")) {
                    c = 4;
                    break;
                }
                break;
            case 3322014:
                if (cmd.equals("list")) {
                    c = 0;
                    break;
                }
                break;
            case 108404047:
                if (cmd.equals("reset")) {
                    c = 2;
                    break;
                }
                break;
            case 1557372922:
                if (cmd.equals("destroy")) {
                    c = 1;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return requestList(pw);
            case 1:
                return requestDestroy(pw);
            case 2:
                return requestReset();
            case 3:
                return requestGet(pw);
            case 4:
                return requestSet(pw);
            default:
                return handleDefaultCommands(cmd);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x00f7, code lost:
        if (r1 != null) goto L_0x00f9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x00fd, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x00fe, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0102, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x00f1, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x00f5, code lost:
        if (r0 != null) goto L_0x00f7;
     */
    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("AutoFill Service (autofill) commands:");
        pw.println("  help");
        pw.println("    Prints this help text.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  get log_level ");
        pw.println("    Gets the Autofill log level (off | debug | verbose).");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  get max_partitions");
        pw.println("    Gets the maximum number of partitions per session.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  get max_visible_datasets");
        pw.println("    Gets the maximum number of visible datasets in the UI.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  get full_screen_mode");
        pw.println("    Gets the Fill UI full screen mode");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  get fc_score [--algorithm ALGORITHM] value1 value2");
        pw.println("    Gets the field classification score for 2 fields.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  get bind-instant-service-allowed");
        pw.println("    Gets whether binding to services provided by instant apps is allowed");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  set log_level [off | debug | verbose]");
        pw.println("    Sets the Autofill log level.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  set max_partitions number");
        pw.println("    Sets the maximum number of partitions per session.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  set max_visible_datasets number");
        pw.println("    Sets the maximum number of visible datasets in the UI.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  set full_screen_mode [true | false | default]");
        pw.println("    Sets the Fill UI full screen mode");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  set bind-instant-service-allowed [true | false]");
        pw.println("    Sets whether binding to services provided by instant apps is allowed");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  list sessions [--user USER_ID]");
        pw.println("    Lists all pending sessions.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  destroy sessions [--user USER_ID]");
        pw.println("    Destroys all pending sessions.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  reset");
        pw.println("    Resets all pending sessions and cached service connections.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        if (pw != null) {
            pw.close();
            return;
        }
        return;
        throw th;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private int requestGet(PrintWriter pw) {
        char c;
        String what = getNextArgRequired();
        switch (what.hashCode()) {
            case -2124387184:
                if (what.equals("fc_score")) {
                    c = 3;
                    break;
                }
            case -2006901047:
                if (what.equals("log_level")) {
                    c = 0;
                    break;
                }
            case -1298810906:
                if (what.equals("full_screen_mode")) {
                    c = 4;
                    break;
                }
            case 809633044:
                if (what.equals("bind-instant-service-allowed")) {
                    c = 5;
                    break;
                }
            case 1393110435:
                if (what.equals("max_visible_datasets")) {
                    c = 2;
                    break;
                }
            case 1772188804:
                if (what.equals("max_partitions")) {
                    c = 1;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return getLogLevel(pw);
            case 1:
                return getMaxPartitions(pw);
            case 2:
                return getMaxVisibileDatasets(pw);
            case 3:
                return getFieldClassificationScore(pw);
            case 4:
                return getFullScreenMode(pw);
            case 5:
                return getBindInstantService(pw);
            default:
                pw.println("Invalid set: " + what);
                return -1;
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private int requestSet(PrintWriter pw) {
        char c;
        String what = getNextArgRequired();
        switch (what.hashCode()) {
            case -2006901047:
                if (what.equals("log_level")) {
                    c = 0;
                    break;
                }
            case -1298810906:
                if (what.equals("full_screen_mode")) {
                    c = 3;
                    break;
                }
            case 809633044:
                if (what.equals("bind-instant-service-allowed")) {
                    c = 4;
                    break;
                }
            case 1393110435:
                if (what.equals("max_visible_datasets")) {
                    c = 2;
                    break;
                }
            case 1772188804:
                if (what.equals("max_partitions")) {
                    c = 1;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return setLogLevel(pw);
            case 1:
                return setMaxPartitions();
            case 2:
                return setMaxVisibileDatasets();
            case 3:
                return setFullScreenMode(pw);
            case 4:
                return setBindInstantService(pw);
            default:
                pw.println("Invalid set: " + what);
                return -1;
        }
    }

    private int getLogLevel(PrintWriter pw) {
        int logLevel = this.mService.getLogLevel();
        if (logLevel == 0) {
            pw.println("off");
            return 0;
        } else if (logLevel == 2) {
            pw.println("debug");
            return 0;
        } else if (logLevel != 4) {
            pw.println("unknow (" + logLevel + ")");
            return 0;
        } else {
            pw.println("verbose");
            return 0;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0043  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0058  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x005e  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0064  */
    private int setLogLevel(PrintWriter pw) {
        boolean z;
        String lowerCase = getNextArgRequired().toLowerCase();
        int hashCode = lowerCase.hashCode();
        if (hashCode == 109935) {
            if (lowerCase.equals("off")) {
                z = true;
                switch (z) {
                    case false:
                        break;
                    case true:
                        break;
                    case true:
                        break;
                }
            }
        } else if (hashCode == 95458899) {
            if (lowerCase.equals("debug")) {
                z = true;
                switch (z) {
                    case false:
                        break;
                    case true:
                        break;
                    case true:
                        break;
                }
            }
        } else if (hashCode == 351107458 && lowerCase.equals("verbose")) {
            z = false;
            switch (z) {
                case false:
                    this.mService.setLogLevel(4);
                    return 0;
                case true:
                    this.mService.setLogLevel(2);
                    return 0;
                case true:
                    this.mService.setLogLevel(0);
                    return 0;
                default:
                    pw.println("Invalid level: " + logLevel);
                    return -1;
            }
        }
        z = true;
        switch (z) {
            case false:
                break;
            case true:
                break;
            case true:
                break;
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

    private int getMaxVisibileDatasets(PrintWriter pw) {
        pw.println(this.mService.getMaxVisibleDatasets());
        return 0;
    }

    private int setMaxVisibileDatasets() {
        this.mService.setMaxVisibleDatasets(Integer.parseInt(getNextArgRequired()));
        return 0;
    }

    private int getFieldClassificationScore(PrintWriter pw) {
        String value1;
        String algorithm;
        String nextArg = getNextArgRequired();
        if ("--algorithm".equals(nextArg)) {
            algorithm = getNextArgRequired();
            value1 = getNextArgRequired();
        } else {
            algorithm = null;
            value1 = nextArg;
        }
        String value2 = getNextArgRequired();
        CountDownLatch latch = new CountDownLatch(1);
        this.mService.getScore(algorithm, value1, value2, new RemoteCallback(new RemoteCallback.OnResultListener(pw, latch) {
            private final /* synthetic */ PrintWriter f$0;
            private final /* synthetic */ CountDownLatch f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void onResult(Bundle bundle) {
                AutofillManagerServiceShellCommand.lambda$getFieldClassificationScore$0(this.f$0, this.f$1, bundle);
            }
        }));
        return waitForLatch(pw, latch);
    }

    static /* synthetic */ void lambda$getFieldClassificationScore$0(PrintWriter pw, CountDownLatch latch, Bundle result) {
        AutofillFieldClassificationService.Scores scores = result.getParcelable("scores");
        if (scores == null) {
            pw.println("no score");
        } else {
            pw.println(scores.scores[0][0]);
        }
        latch.countDown();
    }

    private int getFullScreenMode(PrintWriter pw) {
        Boolean mode = this.mService.getFullScreenMode();
        if (mode == null) {
            pw.println(BatteryService.HealthServiceWrapper.INSTANCE_VENDOR);
        } else if (mode.booleanValue()) {
            pw.println("true");
        } else {
            pw.println("false");
        }
        return 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0041  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0056  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x005d  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0065  */
    private int setFullScreenMode(PrintWriter pw) {
        char c;
        String lowerCase = getNextArgRequired().toLowerCase();
        int hashCode = lowerCase.hashCode();
        if (hashCode == 3569038) {
            if (lowerCase.equals("true")) {
                c = 0;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        } else if (hashCode == 97196323) {
            if (lowerCase.equals("false")) {
                c = 1;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        } else if (hashCode == 1544803905 && lowerCase.equals(BatteryService.HealthServiceWrapper.INSTANCE_VENDOR)) {
            c = 2;
            switch (c) {
                case 0:
                    this.mService.setFullScreenMode(Boolean.TRUE);
                    return 0;
                case 1:
                    this.mService.setFullScreenMode(Boolean.FALSE);
                    return 0;
                case 2:
                    this.mService.setFullScreenMode(null);
                    return 0;
                default:
                    pw.println("Invalid mode: " + mode);
                    return -1;
            }
        }
        c = 65535;
        switch (c) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
        }
    }

    private int getBindInstantService(PrintWriter pw) {
        if (this.mService.getAllowInstantService()) {
            pw.println("true");
        } else {
            pw.println("false");
        }
        return 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0033  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0048  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x004e  */
    private int setBindInstantService(PrintWriter pw) {
        boolean z;
        String lowerCase = getNextArgRequired().toLowerCase();
        int hashCode = lowerCase.hashCode();
        if (hashCode == 3569038) {
            if (lowerCase.equals("true")) {
                z = false;
                switch (z) {
                    case false:
                        break;
                    case true:
                        break;
                }
            }
        } else if (hashCode == 97196323 && lowerCase.equals("false")) {
            z = true;
            switch (z) {
                case false:
                    this.mService.setAllowInstantService(true);
                    return 0;
                case true:
                    this.mService.setAllowInstantService(false);
                    return 0;
                default:
                    pw.println("Invalid mode: " + mode);
                    return -1;
            }
        }
        z = true;
        switch (z) {
            case false:
                break;
            case true:
                break;
        }
    }

    private int requestDestroy(PrintWriter pw) {
        if (!isNextArgSessions(pw)) {
            return -1;
        }
        int userId = getUserIdFromArgsOrAllUsers();
        final CountDownLatch latch = new CountDownLatch(1);
        return requestSessionCommon(pw, latch, new Runnable(userId, new IResultReceiver.Stub() {
            public void send(int resultCode, Bundle resultData) {
                latch.countDown();
            }
        }) {
            private final /* synthetic */ int f$1;
            private final /* synthetic */ IResultReceiver f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                AutofillManagerServiceShellCommand.this.mService.destroySessions(this.f$1, this.f$2);
            }
        });
    }

    private int requestList(final PrintWriter pw) {
        if (!isNextArgSessions(pw)) {
            return -1;
        }
        int userId = getUserIdFromArgsOrAllUsers();
        final CountDownLatch latch = new CountDownLatch(1);
        return requestSessionCommon(pw, latch, new Runnable(userId, new IResultReceiver.Stub() {
            public void send(int resultCode, Bundle resultData) {
                Iterator<String> it = resultData.getStringArrayList("sessions").iterator();
                while (it.hasNext()) {
                    pw.println(it.next());
                }
                latch.countDown();
            }
        }) {
            private final /* synthetic */ int f$1;
            private final /* synthetic */ IResultReceiver f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                AutofillManagerServiceShellCommand.this.mService.listSessions(this.f$1, this.f$2);
            }
        });
    }

    private boolean isNextArgSessions(PrintWriter pw) {
        if (getNextArgRequired().equals("sessions")) {
            return true;
        }
        pw.println("Error: invalid list type");
        return false;
    }

    private int requestSessionCommon(PrintWriter pw, CountDownLatch latch, Runnable command) {
        command.run();
        return waitForLatch(pw, latch);
    }

    private int waitForLatch(PrintWriter pw, CountDownLatch latch) {
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

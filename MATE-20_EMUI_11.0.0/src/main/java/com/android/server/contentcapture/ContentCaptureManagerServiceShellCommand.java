package com.android.server.contentcapture;

import android.os.Bundle;
import android.os.ShellCommand;
import android.os.UserHandle;
import com.android.internal.os.IResultReceiver;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class ContentCaptureManagerServiceShellCommand extends ShellCommand {
    private final ContentCaptureManagerService mService;

    public ContentCaptureManagerServiceShellCommand(ContentCaptureManagerService service) {
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
                    c = 2;
                    break;
                }
                break;
            case 113762:
                if (cmd.equals("set")) {
                    c = 3;
                    break;
                }
                break;
            case 3322014:
                if (cmd.equals("list")) {
                    c = 0;
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
        if (c == 0) {
            return requestList(pw);
        }
        if (c == 1) {
            return requestDestroy(pw);
        }
        if (c == 2) {
            return requestGet(pw);
        }
        if (c != 3) {
            return handleDefaultCommands(cmd);
        }
        return requestSet(pw);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0085, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0086, code lost:
        r0.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0089, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x007e, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x007f, code lost:
        if (r1 != null) goto L_0x0081;
     */
    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("ContentCapture Service (content_capture) commands:");
        pw.println("  help");
        pw.println("    Prints this help text.");
        pw.println("");
        pw.println("  get bind-instant-service-allowed");
        pw.println("    Gets whether binding to services provided by instant apps is allowed");
        pw.println("");
        pw.println("  set bind-instant-service-allowed [true | false]");
        pw.println("    Sets whether binding to services provided by instant apps is allowed");
        pw.println("");
        pw.println("  set temporary-service USER_ID [COMPONENT_NAME DURATION]");
        pw.println("    Temporarily (for DURATION ms) changes the service implemtation.");
        pw.println("    To reset, call with just the USER_ID argument.");
        pw.println("");
        pw.println("  set default-service-enabled USER_ID [true|false]");
        pw.println("    Enable / disable the default service for the user.");
        pw.println("");
        pw.println("  get default-service-enabled USER_ID");
        pw.println("    Checks whether the default service is enabled for the user.");
        pw.println("");
        pw.println("  list sessions [--user USER_ID]");
        pw.println("    Lists all pending sessions.");
        pw.println("");
        pw.println("  destroy sessions [--user USER_ID]");
        pw.println("    Destroys all pending sessions.");
        pw.println("");
        pw.close();
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x002c  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0048  */
    private int requestGet(PrintWriter pw) {
        boolean z;
        String what = getNextArgRequired();
        int hashCode = what.hashCode();
        if (hashCode != 529654941) {
            if (hashCode == 809633044 && what.equals("bind-instant-service-allowed")) {
                z = false;
                if (!z) {
                    return getBindInstantService(pw);
                }
                if (z) {
                    return getDefaultServiceEnabled(pw);
                }
                pw.println("Invalid set: " + what);
                return -1;
            }
        } else if (what.equals("default-service-enabled")) {
            z = true;
            if (!z) {
            }
        }
        z = true;
        if (!z) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x003d  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0060  */
    private int requestSet(PrintWriter pw) {
        boolean z;
        String what = getNextArgRequired();
        int hashCode = what.hashCode();
        if (hashCode != 529654941) {
            if (hashCode != 809633044) {
                if (hashCode == 2003978041 && what.equals("temporary-service")) {
                    z = true;
                    if (z) {
                        return setBindInstantService(pw);
                    }
                    if (z) {
                        return setTemporaryService(pw);
                    }
                    if (z) {
                        return setDefaultServiceEnabled(pw);
                    }
                    pw.println("Invalid set: " + what);
                    return -1;
                }
            } else if (what.equals("bind-instant-service-allowed")) {
                z = false;
                if (z) {
                }
            }
        } else if (what.equals("default-service-enabled")) {
            z = true;
            if (z) {
            }
        }
        z = true;
        if (z) {
        }
    }

    private int getBindInstantService(PrintWriter pw) {
        if (this.mService.getAllowInstantService()) {
            pw.println("true");
            return 0;
        }
        pw.println("false");
        return 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0032  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x004f  */
    private int setBindInstantService(PrintWriter pw) {
        boolean z;
        String mode = getNextArgRequired();
        String lowerCase = mode.toLowerCase();
        int hashCode = lowerCase.hashCode();
        if (hashCode != 3569038) {
            if (hashCode == 97196323 && lowerCase.equals("false")) {
                z = true;
                if (!z) {
                    this.mService.setAllowInstantService(true);
                    return 0;
                } else if (!z) {
                    pw.println("Invalid mode: " + mode);
                    return -1;
                } else {
                    this.mService.setAllowInstantService(false);
                    return 0;
                }
            }
        } else if (lowerCase.equals("true")) {
            z = false;
            if (!z) {
            }
        }
        z = true;
        if (!z) {
        }
    }

    private int setTemporaryService(PrintWriter pw) {
        int userId = getNextIntArgRequired();
        String serviceName = getNextArg();
        if (serviceName == null) {
            this.mService.resetTemporaryService(userId);
            return 0;
        }
        int duration = getNextIntArgRequired();
        this.mService.setTemporaryService(userId, serviceName, duration);
        pw.println("ContentCaptureService temporarily set to " + serviceName + " for " + duration + "ms");
        return 0;
    }

    private int setDefaultServiceEnabled(PrintWriter pw) {
        int userId = getNextIntArgRequired();
        boolean enabled = Boolean.parseBoolean(getNextArgRequired());
        if (this.mService.setDefaultServiceEnabled(userId, enabled)) {
            return 0;
        }
        pw.println("already " + enabled);
        return 0;
    }

    private int getDefaultServiceEnabled(PrintWriter pw) {
        pw.println(this.mService.isDefaultServiceEnabled(getNextIntArgRequired()));
        return 0;
    }

    private int requestDestroy(PrintWriter pw) {
        if (!isNextArgSessions(pw)) {
            return -1;
        }
        int userId = getUserIdFromArgsOrAllUsers();
        final CountDownLatch latch = new CountDownLatch(1);
        return requestSessionCommon(pw, latch, new Runnable(userId, new IResultReceiver.Stub() {
            /* class com.android.server.contentcapture.ContentCaptureManagerServiceShellCommand.AnonymousClass1 */

            public void send(int resultCode, Bundle resultData) {
                latch.countDown();
            }
        }) {
            /* class com.android.server.contentcapture.$$Lambda$ContentCaptureManagerServiceShellCommand$JID7gMfFJshMFIl2pXHCkZLd6tI */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ IResultReceiver f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ContentCaptureManagerServiceShellCommand.this.lambda$requestDestroy$0$ContentCaptureManagerServiceShellCommand(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$requestDestroy$0$ContentCaptureManagerServiceShellCommand(int userId, IResultReceiver receiver) {
        this.mService.destroySessions(userId, receiver);
    }

    private int requestList(final PrintWriter pw) {
        if (!isNextArgSessions(pw)) {
            return -1;
        }
        int userId = getUserIdFromArgsOrAllUsers();
        final CountDownLatch latch = new CountDownLatch(1);
        return requestSessionCommon(pw, latch, new Runnable(userId, new IResultReceiver.Stub() {
            /* class com.android.server.contentcapture.ContentCaptureManagerServiceShellCommand.AnonymousClass2 */

            public void send(int resultCode, Bundle resultData) {
                Iterator<String> it = resultData.getStringArrayList("sessions").iterator();
                while (it.hasNext()) {
                    pw.println(it.next());
                }
                latch.countDown();
            }
        }) {
            /* class com.android.server.contentcapture.$$Lambda$ContentCaptureManagerServiceShellCommand$vv2l3dpITkvTtrSG9pnNuz8Nsc */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ IResultReceiver f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ContentCaptureManagerServiceShellCommand.this.lambda$requestList$1$ContentCaptureManagerServiceShellCommand(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$requestList$1$ContentCaptureManagerServiceShellCommand(int userId, IResultReceiver receiver) {
        this.mService.listSessions(userId, receiver);
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

    private int getUserIdFromArgsOrAllUsers() {
        if ("--user".equals(getNextArg())) {
            return UserHandle.parseUserArg(getNextArgRequired());
        }
        return -1;
    }

    private int getNextIntArgRequired() {
        return Integer.parseInt(getNextArgRequired());
    }
}

package com.android.server.contentsuggestions;

import android.os.ShellCommand;
import java.io.PrintWriter;

public class ContentSuggestionsManagerServiceShellCommand extends ShellCommand {
    private static final String TAG = ContentSuggestionsManagerServiceShellCommand.class.getSimpleName();
    private final ContentSuggestionsManagerService mService;

    public ContentSuggestionsManagerServiceShellCommand(ContentSuggestionsManagerService service) {
        this.mService = service;
    }

    public int onCommand(String cmd) {
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter pw = getOutPrintWriter();
        char c = 65535;
        int hashCode = cmd.hashCode();
        if (hashCode != 102230) {
            if (hashCode == 113762 && cmd.equals("set")) {
                c = 0;
            }
        } else if (cmd.equals("get")) {
            c = 1;
        }
        if (c == 0) {
            return requestSet(pw);
        }
        if (c != 1) {
            return handleDefaultCommands(cmd);
        }
        return requestGet(pw);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0051, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0052, code lost:
        r0.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0055, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x004a, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x004b, code lost:
        if (r1 != null) goto L_0x004d;
     */
    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("ContentSuggestionsManagerService commands:");
        pw.println("  help");
        pw.println("    Prints this help text.");
        pw.println("");
        pw.println("  set temporary-service USER_ID [COMPONENT_NAME DURATION]");
        pw.println("    Temporarily (for DURATION ms) changes the service implementation.");
        pw.println("    To reset, call with just the USER_ID argument.");
        pw.println("");
        pw.println("  set default-service-enabled USER_ID [true|false]");
        pw.println("    Enable / disable the default service for the user.");
        pw.println("");
        pw.println("  get default-service-enabled USER_ID");
        pw.println("    Checks whether the default service is enabled for the user.");
        pw.println("");
        pw.close();
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x002d  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0049  */
    private int requestSet(PrintWriter pw) {
        boolean z;
        String what = getNextArgRequired();
        int hashCode = what.hashCode();
        if (hashCode != 529654941) {
            if (hashCode == 2003978041 && what.equals("temporary-service")) {
                z = false;
                if (!z) {
                    return setTemporaryService(pw);
                }
                if (z) {
                    return setDefaultServiceEnabled();
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

    private int requestGet(PrintWriter pw) {
        String what = getNextArgRequired();
        if (!((what.hashCode() == 529654941 && what.equals("default-service-enabled")) ? false : true)) {
            return getDefaultServiceEnabled(pw);
        }
        pw.println("Invalid get: " + what);
        return -1;
    }

    private int setTemporaryService(PrintWriter pw) {
        int userId = Integer.parseInt(getNextArgRequired());
        String serviceName = getNextArg();
        if (serviceName == null) {
            this.mService.resetTemporaryService(userId);
            return 0;
        }
        int duration = Integer.parseInt(getNextArgRequired());
        this.mService.setTemporaryService(userId, serviceName, duration);
        pw.println("ContentSuggestionsService temporarily set to " + serviceName + " for " + duration + "ms");
        return 0;
    }

    private int setDefaultServiceEnabled() {
        this.mService.setDefaultServiceEnabled(getNextIntArgRequired(), Boolean.parseBoolean(getNextArg()));
        return 0;
    }

    private int getDefaultServiceEnabled(PrintWriter pw) {
        pw.println(this.mService.isDefaultServiceEnabled(getNextIntArgRequired()));
        return 0;
    }

    private int getNextIntArgRequired() {
        return Integer.parseInt(getNextArgRequired());
    }
}

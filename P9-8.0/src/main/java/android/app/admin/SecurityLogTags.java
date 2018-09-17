package android.app.admin;

import android.util.EventLog;

public class SecurityLogTags {
    public static final int SECURITY_ADB_SHELL_COMMAND = 210002;
    public static final int SECURITY_ADB_SHELL_INTERACTIVE = 210001;
    public static final int SECURITY_ADB_SYNC_RECV = 210003;
    public static final int SECURITY_ADB_SYNC_SEND = 210004;
    public static final int SECURITY_APP_PROCESS_START = 210005;
    public static final int SECURITY_KEYGUARD_DISMISSED = 210006;
    public static final int SECURITY_KEYGUARD_DISMISS_AUTH_ATTEMPT = 210007;
    public static final int SECURITY_KEYGUARD_SECURED = 210008;

    private SecurityLogTags() {
    }

    public static void writeSecurityAdbShellInteractive() {
        EventLog.writeEvent(210001, new Object[0]);
    }

    public static void writeSecurityAdbShellCommand(String command) {
        EventLog.writeEvent(210002, command);
    }

    public static void writeSecurityAdbSyncRecv(String path) {
        EventLog.writeEvent(210003, path);
    }

    public static void writeSecurityAdbSyncSend(String path) {
        EventLog.writeEvent(210004, path);
    }

    public static void writeSecurityAppProcessStart(String process, long startTime, int uid, int pid, String seinfo, String sha256) {
        EventLog.writeEvent(210005, new Object[]{process, Long.valueOf(startTime), Integer.valueOf(uid), Integer.valueOf(pid), seinfo, sha256});
    }

    public static void writeSecurityKeyguardDismissed() {
        EventLog.writeEvent(210006, new Object[0]);
    }

    public static void writeSecurityKeyguardDismissAuthAttempt(int success, int methodStrength) {
        EventLog.writeEvent(210007, new Object[]{Integer.valueOf(success), Integer.valueOf(methodStrength)});
    }

    public static void writeSecurityKeyguardSecured() {
        EventLog.writeEvent(210008, new Object[0]);
    }
}

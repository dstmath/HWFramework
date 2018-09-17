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
        EventLog.writeEvent(SECURITY_ADB_SHELL_INTERACTIVE, new Object[0]);
    }

    public static void writeSecurityAdbShellCommand(String command) {
        EventLog.writeEvent(SECURITY_ADB_SHELL_COMMAND, command);
    }

    public static void writeSecurityAdbSyncRecv(String path) {
        EventLog.writeEvent(SECURITY_ADB_SYNC_RECV, path);
    }

    public static void writeSecurityAdbSyncSend(String path) {
        EventLog.writeEvent(SECURITY_ADB_SYNC_SEND, path);
    }

    public static void writeSecurityAppProcessStart(String process, long startTime, int uid, int pid, String seinfo, String sha256) {
        EventLog.writeEvent(SECURITY_APP_PROCESS_START, new Object[]{process, Long.valueOf(startTime), Integer.valueOf(uid), Integer.valueOf(pid), seinfo, sha256});
    }

    public static void writeSecurityKeyguardDismissed() {
        EventLog.writeEvent(SECURITY_KEYGUARD_DISMISSED, new Object[0]);
    }

    public static void writeSecurityKeyguardDismissAuthAttempt(int success, int methodStrength) {
        EventLog.writeEvent(SECURITY_KEYGUARD_DISMISS_AUTH_ATTEMPT, new Object[]{Integer.valueOf(success), Integer.valueOf(methodStrength)});
    }

    public static void writeSecurityKeyguardSecured() {
        EventLog.writeEvent(SECURITY_KEYGUARD_SECURED, new Object[0]);
    }
}

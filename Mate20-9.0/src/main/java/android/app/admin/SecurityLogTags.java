package android.app.admin;

import android.util.EventLog;

public class SecurityLogTags {
    public static final int SECURITY_ADB_SHELL_COMMAND = 210002;
    public static final int SECURITY_ADB_SHELL_INTERACTIVE = 210001;
    public static final int SECURITY_ADB_SYNC_RECV = 210003;
    public static final int SECURITY_ADB_SYNC_SEND = 210004;
    public static final int SECURITY_APP_PROCESS_START = 210005;
    public static final int SECURITY_CERT_AUTHORITY_INSTALLED = 210029;
    public static final int SECURITY_CERT_AUTHORITY_REMOVED = 210030;
    public static final int SECURITY_CERT_VALIDATION_FAILURE = 210033;
    public static final int SECURITY_CRYPTO_SELF_TEST_COMPLETED = 210031;
    public static final int SECURITY_KEYGUARD_DISABLED_FEATURES_SET = 210021;
    public static final int SECURITY_KEYGUARD_DISMISSED = 210006;
    public static final int SECURITY_KEYGUARD_DISMISS_AUTH_ATTEMPT = 210007;
    public static final int SECURITY_KEYGUARD_SECURED = 210008;
    public static final int SECURITY_KEY_DESTROYED = 210026;
    public static final int SECURITY_KEY_GENERATED = 210024;
    public static final int SECURITY_KEY_IMPORTED = 210025;
    public static final int SECURITY_KEY_INTEGRITY_VIOLATION = 210032;
    public static final int SECURITY_LOGGING_STARTED = 210011;
    public static final int SECURITY_LOGGING_STOPPED = 210012;
    public static final int SECURITY_LOG_BUFFER_SIZE_CRITICAL = 210015;
    public static final int SECURITY_MAX_PASSWORD_ATTEMPTS_SET = 210020;
    public static final int SECURITY_MAX_SCREEN_LOCK_TIMEOUT_SET = 210019;
    public static final int SECURITY_MEDIA_MOUNTED = 210013;
    public static final int SECURITY_MEDIA_UNMOUNTED = 210014;
    public static final int SECURITY_OS_SHUTDOWN = 210010;
    public static final int SECURITY_OS_STARTUP = 210009;
    public static final int SECURITY_PASSWORD_COMPLEXITY_SET = 210017;
    public static final int SECURITY_PASSWORD_EXPIRATION_SET = 210016;
    public static final int SECURITY_PASSWORD_HISTORY_LENGTH_SET = 210018;
    public static final int SECURITY_REMOTE_LOCK = 210022;
    public static final int SECURITY_USER_RESTRICTION_ADDED = 210027;
    public static final int SECURITY_USER_RESTRICTION_REMOVED = 210028;
    public static final int SECURITY_WIPE_FAILED = 210023;

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

    public static void writeSecurityOsStartup(String bootState, String verityMode) {
        EventLog.writeEvent(210009, new Object[]{bootState, verityMode});
    }

    public static void writeSecurityOsShutdown() {
        EventLog.writeEvent(210010, new Object[0]);
    }

    public static void writeSecurityLoggingStarted() {
        EventLog.writeEvent(210011, new Object[0]);
    }

    public static void writeSecurityLoggingStopped() {
        EventLog.writeEvent(210012, new Object[0]);
    }

    public static void writeSecurityMediaMounted(String path, String label) {
        EventLog.writeEvent(210013, new Object[]{path, label});
    }

    public static void writeSecurityMediaUnmounted(String path, String label) {
        EventLog.writeEvent(210014, new Object[]{path, label});
    }

    public static void writeSecurityLogBufferSizeCritical() {
        EventLog.writeEvent(210015, new Object[0]);
    }

    public static void writeSecurityPasswordExpirationSet(String package_, int adminUser, int targetUser, long timeout) {
        EventLog.writeEvent(210016, new Object[]{package_, Integer.valueOf(adminUser), Integer.valueOf(targetUser), Long.valueOf(timeout)});
    }

    public static void writeSecurityPasswordComplexitySet(String package_, int adminUser, int targetUser, int length, int quality, int numLetters, int numNonLetters, int numNumeric, int numUppercase, int numLowercase, int numSymbols) {
        EventLog.writeEvent(210017, new Object[]{package_, Integer.valueOf(adminUser), Integer.valueOf(targetUser), Integer.valueOf(length), Integer.valueOf(quality), Integer.valueOf(numLetters), Integer.valueOf(numNonLetters), Integer.valueOf(numNumeric), Integer.valueOf(numUppercase), Integer.valueOf(numLowercase), Integer.valueOf(numSymbols)});
    }

    public static void writeSecurityPasswordHistoryLengthSet(String package_, int adminUser, int targetUser, int length) {
        EventLog.writeEvent(210018, new Object[]{package_, Integer.valueOf(adminUser), Integer.valueOf(targetUser), Integer.valueOf(length)});
    }

    public static void writeSecurityMaxScreenLockTimeoutSet(String package_, int adminUser, int targetUser, long timeout) {
        EventLog.writeEvent(210019, new Object[]{package_, Integer.valueOf(adminUser), Integer.valueOf(targetUser), Long.valueOf(timeout)});
    }

    public static void writeSecurityMaxPasswordAttemptsSet(String package_, int adminUser, int targetUser, int numFailures) {
        EventLog.writeEvent(210020, new Object[]{package_, Integer.valueOf(adminUser), Integer.valueOf(targetUser), Integer.valueOf(numFailures)});
    }

    public static void writeSecurityKeyguardDisabledFeaturesSet(String package_, int adminUser, int targetUser, int features) {
        EventLog.writeEvent(210021, new Object[]{package_, Integer.valueOf(adminUser), Integer.valueOf(targetUser), Integer.valueOf(features)});
    }

    public static void writeSecurityRemoteLock(String package_, int adminUser, int targetUser) {
        EventLog.writeEvent(210022, new Object[]{package_, Integer.valueOf(adminUser), Integer.valueOf(targetUser)});
    }

    public static void writeSecurityWipeFailed(String package_, int adminUser) {
        EventLog.writeEvent(210023, new Object[]{package_, Integer.valueOf(adminUser)});
    }

    public static void writeSecurityKeyGenerated(int success, String keyId, int uid) {
        EventLog.writeEvent(210024, new Object[]{Integer.valueOf(success), keyId, Integer.valueOf(uid)});
    }

    public static void writeSecurityKeyImported(int success, String keyId, int uid) {
        EventLog.writeEvent(210025, new Object[]{Integer.valueOf(success), keyId, Integer.valueOf(uid)});
    }

    public static void writeSecurityKeyDestroyed(int success, String keyId, int uid) {
        EventLog.writeEvent(210026, new Object[]{Integer.valueOf(success), keyId, Integer.valueOf(uid)});
    }

    public static void writeSecurityUserRestrictionAdded(String package_, int adminUser, String restriction) {
        EventLog.writeEvent(210027, new Object[]{package_, Integer.valueOf(adminUser), restriction});
    }

    public static void writeSecurityUserRestrictionRemoved(String package_, int adminUser, String restriction) {
        EventLog.writeEvent(210028, new Object[]{package_, Integer.valueOf(adminUser), restriction});
    }

    public static void writeSecurityCertAuthorityInstalled(int success, String subject) {
        EventLog.writeEvent(210029, new Object[]{Integer.valueOf(success), subject});
    }

    public static void writeSecurityCertAuthorityRemoved(int success, String subject) {
        EventLog.writeEvent(210030, new Object[]{Integer.valueOf(success), subject});
    }

    public static void writeSecurityCryptoSelfTestCompleted(int success) {
        EventLog.writeEvent(210031, success);
    }

    public static void writeSecurityKeyIntegrityViolation(String keyId, int uid) {
        EventLog.writeEvent(210032, new Object[]{keyId, Integer.valueOf(uid)});
    }

    public static void writeSecurityCertValidationFailure(String reason) {
        EventLog.writeEvent(210033, reason);
    }
}

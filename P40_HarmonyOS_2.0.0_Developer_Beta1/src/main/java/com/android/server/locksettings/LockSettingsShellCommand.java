package com.android.server.locksettings;

import android.app.ActivityManager;
import android.os.ShellCommand;
import android.os.SystemClock;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.pm.DumpState;
import java.io.PrintWriter;
import java.util.HashMap;

class LockSettingsShellCommand extends ShellCommand {
    private static final String COMMAND_CLEAR = "clear";
    private static final String COMMAND_GET_DISABLED = "get-disabled";
    private static final String COMMAND_HELP = "help";
    private static final String COMMAND_SET_DISABLED = "set-disabled";
    private static final String COMMAND_SET_PASSWORD = "set-password";
    private static final String COMMAND_SET_PATTERN = "set-pattern";
    private static final String COMMAND_SET_PIN = "set-pin";
    private static final String COMMAND_SP = "sp";
    private static final String COMMAND_VERIFY = "verify";
    private static final long REQUEST_THROTTLED_TIME = 30000;
    private static final int REQUEST_THROTTLED_TIMES = 5;
    private static HashMap<Integer, Long> sThrottledTimeMaps = new HashMap<>();
    private static HashMap<Integer, Integer> sThrottledTimesMaps = new HashMap<>();
    private int mCurrentUserId;
    private final LockPatternUtils mLockPatternUtils;
    private String mNew = "";
    private String mOld = "";

    LockSettingsShellCommand(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public int onCommand(String cmd) {
        char c;
        boolean z;
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        try {
            this.mCurrentUserId = ActivityManager.getService().getCurrentUser().id;
            parseArgs();
            if (!this.mLockPatternUtils.hasSecureLockScreen()) {
                int hashCode = cmd.hashCode();
                if (hashCode != -1473704173) {
                    if (hashCode != 3198785) {
                        if (hashCode == 75288455 && cmd.equals(COMMAND_SET_DISABLED)) {
                            z = true;
                            if (!(!z || z || z)) {
                                getErrPrintWriter().println("The device does not support lock screen - ignoring the command.");
                                return -1;
                            }
                        }
                    } else if (cmd.equals(COMMAND_HELP)) {
                        z = false;
                        getErrPrintWriter().println("The device does not support lock screen - ignoring the command.");
                        return -1;
                    }
                } else if (cmd.equals(COMMAND_GET_DISABLED)) {
                    z = true;
                    getErrPrintWriter().println("The device does not support lock screen - ignoring the command.");
                    return -1;
                }
                z = true;
                getErrPrintWriter().println("The device does not support lock screen - ignoring the command.");
                return -1;
            }
            if (!checkCredential()) {
                return -1;
            }
            switch (cmd.hashCode()) {
                case -2044327643:
                    if (cmd.equals(COMMAND_SET_PATTERN)) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -1473704173:
                    if (cmd.equals(COMMAND_GET_DISABLED)) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -819951495:
                    if (cmd.equals(COMMAND_VERIFY)) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 3677:
                    if (cmd.equals(COMMAND_SP)) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 3198785:
                    if (cmd.equals(COMMAND_HELP)) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 75288455:
                    if (cmd.equals(COMMAND_SET_DISABLED)) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 94746189:
                    if (cmd.equals(COMMAND_CLEAR)) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 1021333414:
                    if (cmd.equals(COMMAND_SET_PASSWORD)) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1983832490:
                    if (cmd.equals(COMMAND_SET_PIN)) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    runSetPattern();
                    break;
                case 1:
                    runSetPassword();
                    break;
                case 2:
                    runSetPin();
                    break;
                case 3:
                    runClear();
                    break;
                case 4:
                    runChangeSp();
                    break;
                case 5:
                    runSetDisabled();
                    break;
                case 6:
                    runVerify();
                    break;
                case 7:
                    runGetDisabled();
                    break;
                case '\b':
                    onHelp();
                    break;
                default:
                    getErrPrintWriter().println("Unknown command: " + cmd);
                    break;
            }
            return 0;
        } catch (Exception e) {
            getErrPrintWriter().println("Error while executing command: " + cmd);
            e.printStackTrace(getErrPrintWriter());
            return -1;
        }
    }

    private void runVerify() {
        getOutPrintWriter().println("Lock credential verified successfully");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x00a6, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x00a7, code lost:
        r0.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x00aa, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x009f, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x00a0, code lost:
        if (r1 != null) goto L_0x00a2;
     */
    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("lockSettings service commands:");
        pw.println("");
        pw.println("NOTE: when lock screen is set, all commands require the --old <CREDENTIAL> argument.");
        pw.println("");
        pw.println("  help");
        pw.println("    Prints this help text.");
        pw.println("");
        pw.println("  get-disabled [--old <CREDENTIAL>] [--user USER_ID]");
        pw.println("    Checks whether lock screen is disabled.");
        pw.println("");
        pw.println("  set-disabled [--old <CREDENTIAL>] [--user USER_ID] <true|false>");
        pw.println("    When true, disables lock screen.");
        pw.println("");
        pw.println("  set-pattern [--old <CREDENTIAL>] [--user USER_ID] <PATTERN>");
        pw.println("    Sets the lock screen as pattern, using the given PATTERN to unlock.");
        pw.println("");
        pw.println("  set-pin [--old <CREDENTIAL>] [--user USER_ID] <PIN>");
        pw.println("    Sets the lock screen as PIN, using the given PIN to unlock.");
        pw.println("");
        pw.println("  set-pin [--old <CREDENTIAL>] [--user USER_ID] <PASSWORD>");
        pw.println("    Sets the lock screen as password, using the given PASSOWRD to unlock.");
        pw.println("");
        pw.println("  sp [--old <CREDENTIAL>] [--user USER_ID]");
        pw.println("    Gets whether synthetic password is enabled.");
        pw.println("");
        pw.println("  sp [--old <CREDENTIAL>] [--user USER_ID] <1|0>");
        pw.println("    Enables / disables synthetic password.");
        pw.println("");
        pw.println("  clear [--old <CREDENTIAL>] [--user USER_ID]");
        pw.println("    Clears the lock credentials.");
        pw.println("");
        pw.println("  verify [--old <CREDENTIAL>] [--user USER_ID]");
        pw.println("    Verifies the lock credentials.");
        pw.println("");
        pw.close();
    }

    private void parseArgs() {
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                this.mNew = getNextArg();
                return;
            } else if ("--old".equals(opt)) {
                this.mOld = getNextArgRequired();
            } else if ("--user".equals(opt)) {
                this.mCurrentUserId = Integer.parseInt(getNextArgRequired());
            } else {
                PrintWriter errPrintWriter = getErrPrintWriter();
                errPrintWriter.println("Unknown option: " + opt);
                throw new IllegalArgumentException();
            }
        }
    }

    private void runChangeSp() {
        String str = this.mNew;
        if (str != null) {
            if ("1".equals(str)) {
                this.mLockPatternUtils.enableSyntheticPassword();
                getOutPrintWriter().println("Synthetic password enabled");
            } else if ("0".equals(this.mNew)) {
                this.mLockPatternUtils.disableSyntheticPassword();
                getOutPrintWriter().println("Synthetic password disabled");
            }
        }
        getOutPrintWriter().println(String.format("SP Enabled = %b", Boolean.valueOf(this.mLockPatternUtils.isSyntheticPasswordEnabled())));
    }

    private void runSetPattern() {
        String str = this.mOld;
        this.mLockPatternUtils.saveLockPattern(LockPatternUtils.stringToPattern(this.mNew), str != null ? str.getBytes() : null, this.mCurrentUserId);
        PrintWriter outPrintWriter = getOutPrintWriter();
        outPrintWriter.println("Pattern set to '" + this.mNew + "'");
    }

    private void runSetPassword() {
        String str = this.mNew;
        byte[] oldBytes = null;
        byte[] newBytes = str != null ? str.getBytes() : null;
        String str2 = this.mOld;
        if (str2 != null) {
            oldBytes = str2.getBytes();
        }
        this.mLockPatternUtils.saveLockPassword(newBytes, oldBytes, (int) DumpState.DUMP_DOMAIN_PREFERRED, this.mCurrentUserId);
        PrintWriter outPrintWriter = getOutPrintWriter();
        outPrintWriter.println("Password set to '" + this.mNew + "'");
    }

    private void runSetPin() {
        String str = this.mNew;
        byte[] oldBytes = null;
        byte[] newBytes = str != null ? str.getBytes() : null;
        String str2 = this.mOld;
        if (str2 != null) {
            oldBytes = str2.getBytes();
        }
        this.mLockPatternUtils.saveLockPassword(newBytes, oldBytes, (int) DumpState.DUMP_INTENT_FILTER_VERIFIERS, this.mCurrentUserId);
        PrintWriter outPrintWriter = getOutPrintWriter();
        outPrintWriter.println("Pin set to '" + this.mNew + "'");
    }

    private void runClear() {
        String str = this.mOld;
        this.mLockPatternUtils.clearLock(str != null ? str.getBytes() : null, this.mCurrentUserId);
        getOutPrintWriter().println("Lock credential cleared");
    }

    private void runSetDisabled() {
        boolean disabled = Boolean.parseBoolean(this.mNew);
        this.mLockPatternUtils.setLockScreenDisabled(disabled, this.mCurrentUserId);
        PrintWriter outPrintWriter = getOutPrintWriter();
        outPrintWriter.println("Lock screen disabled set to " + disabled);
    }

    private void runGetDisabled() {
        getOutPrintWriter().println(this.mLockPatternUtils.isLockScreenDisabled(this.mCurrentUserId));
    }

    private void updateLockTime(int userId, boolean result) {
        if (result) {
            sThrottledTimesMaps.put(Integer.valueOf(userId), 0);
            sThrottledTimeMaps.put(Integer.valueOf(userId), 0L);
            return;
        }
        int times = sThrottledTimesMaps.getOrDefault(Integer.valueOf(userId), 0).intValue() + 1;
        if (times >= 5) {
            sThrottledTimesMaps.put(Integer.valueOf(userId), 0);
            sThrottledTimeMaps.put(Integer.valueOf(userId), Long.valueOf(SystemClock.elapsedRealtime() + 30000));
            return;
        }
        sThrottledTimesMaps.put(Integer.valueOf(userId), Integer.valueOf(times));
    }

    private boolean isRequestThrottled(int userId) {
        return SystemClock.elapsedRealtime() < sThrottledTimeMaps.getOrDefault(Integer.valueOf(userId), 0L).longValue();
    }

    private boolean checkCredential() {
        boolean result;
        boolean havePassword = this.mLockPatternUtils.isLockPasswordEnabled(this.mCurrentUserId);
        boolean havePattern = this.mLockPatternUtils.isLockPatternEnabled(this.mCurrentUserId);
        if (havePassword || havePattern) {
            if (this.mLockPatternUtils.isManagedProfileWithUnifiedChallenge(this.mCurrentUserId)) {
                getOutPrintWriter().println("Profile uses unified challenge");
                return false;
            } else if (isRequestThrottled(this.mCurrentUserId)) {
                getOutPrintWriter().println("Request throttled.");
                return false;
            } else {
                if (havePassword) {
                    try {
                        result = this.mLockPatternUtils.checkPassword(this.mOld != null ? this.mOld.getBytes() : null, this.mCurrentUserId);
                    } catch (LockPatternUtils.RequestThrottledException e) {
                        getOutPrintWriter().println("Request throttled");
                        return false;
                    }
                } else {
                    result = this.mLockPatternUtils.checkPattern(LockPatternUtils.stringToPattern(this.mOld), this.mCurrentUserId);
                }
                if (!result) {
                    if (!this.mLockPatternUtils.isManagedProfileWithUnifiedChallenge(this.mCurrentUserId)) {
                        this.mLockPatternUtils.reportFailedPasswordAttempt(this.mCurrentUserId);
                    }
                    PrintWriter outPrintWriter = getOutPrintWriter();
                    outPrintWriter.println("Old password '" + this.mOld + "' didn't match");
                } else {
                    this.mLockPatternUtils.reportSuccessfulPasswordAttempt(this.mCurrentUserId);
                }
                updateLockTime(this.mCurrentUserId, result);
                return result;
            }
        } else if (this.mOld.isEmpty()) {
            return true;
        } else {
            getOutPrintWriter().println("Old password provided but user has no password");
            return false;
        }
    }
}

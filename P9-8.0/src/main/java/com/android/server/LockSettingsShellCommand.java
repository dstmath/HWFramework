package com.android.server;

import android.app.ActivityManager;
import android.content.Context;
import android.os.RemoteException;
import android.os.ShellCommand;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternUtils.RequestThrottledException;

class LockSettingsShellCommand extends ShellCommand {
    private static final String COMMAND_CLEAR = "clear";
    private static final String COMMAND_SET_DISABLED = "set-disabled";
    private static final String COMMAND_SET_PASSWORD = "set-password";
    private static final String COMMAND_SET_PATTERN = "set-pattern";
    private static final String COMMAND_SET_PIN = "set-pin";
    private static final String COMMAND_SP = "sp";
    private final Context mContext;
    private int mCurrentUserId;
    private final LockPatternUtils mLockPatternUtils;
    private String mNew = "";
    private String mOld = "";

    LockSettingsShellCommand(Context context, LockPatternUtils lockPatternUtils) {
        this.mContext = context;
        this.mLockPatternUtils = lockPatternUtils;
    }

    public int onCommand(String cmd) {
        try {
            this.mCurrentUserId = ActivityManager.getService().getCurrentUser().id;
            parseArgs();
            if (!checkCredential()) {
                return -1;
            }
            if (cmd.equals(COMMAND_SET_PATTERN)) {
                runSetPattern();
            } else if (cmd.equals(COMMAND_SET_PASSWORD)) {
                runSetPassword();
            } else if (cmd.equals(COMMAND_SET_PIN)) {
                runSetPin();
            } else if (cmd.equals(COMMAND_CLEAR)) {
                runClear();
            } else if (cmd.equals(COMMAND_SP)) {
                runEnableSp();
            } else if (cmd.equals(COMMAND_SET_DISABLED)) {
                runSetDisabled();
            } else {
                getErrPrintWriter().println("Unknown command: " + cmd);
            }
            return 0;
        } catch (Exception e) {
            getErrPrintWriter().println("Error while executing command: " + cmd);
            e.printStackTrace(getErrPrintWriter());
            return -1;
        }
    }

    public void onHelp() {
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
                getErrPrintWriter().println("Unknown option: " + opt);
                throw new IllegalArgumentException();
            }
        }
    }

    private void runEnableSp() {
        if (this.mNew != null) {
            this.mLockPatternUtils.enableSyntheticPassword();
            getOutPrintWriter().println("Synthetic password enabled");
        }
        getOutPrintWriter().println(String.format("SP Enabled = %b", new Object[]{Boolean.valueOf(this.mLockPatternUtils.isSyntheticPasswordEnabled())}));
    }

    private void runSetPattern() throws RemoteException {
        this.mLockPatternUtils.saveLockPattern(LockPatternUtils.stringToPattern(this.mNew), this.mOld, this.mCurrentUserId);
        getOutPrintWriter().println("Pattern set to '" + this.mNew + "'");
    }

    private void runSetPassword() throws RemoteException {
        this.mLockPatternUtils.saveLockPassword(this.mNew, this.mOld, DumpState.DUMP_DOMAIN_PREFERRED, this.mCurrentUserId);
        getOutPrintWriter().println("Password set to '" + this.mNew + "'");
    }

    private void runSetPin() throws RemoteException {
        this.mLockPatternUtils.saveLockPassword(this.mNew, this.mOld, DumpState.DUMP_INTENT_FILTER_VERIFIERS, this.mCurrentUserId);
        getOutPrintWriter().println("Pin set to '" + this.mNew + "'");
    }

    private void runClear() throws RemoteException {
        this.mLockPatternUtils.clearLock(this.mOld, this.mCurrentUserId);
        getOutPrintWriter().println("Lock credential cleared");
    }

    private void runSetDisabled() throws RemoteException {
        boolean disabled = Boolean.parseBoolean(this.mNew);
        this.mLockPatternUtils.setLockScreenDisabled(disabled, this.mCurrentUserId);
        getOutPrintWriter().println("Lock screen disabled set to " + disabled);
    }

    private boolean checkCredential() throws RemoteException, RequestThrottledException {
        boolean havePassword = this.mLockPatternUtils.isLockPasswordEnabled(this.mCurrentUserId);
        boolean havePattern = this.mLockPatternUtils.isLockPatternEnabled(this.mCurrentUserId);
        if (!havePassword && !havePattern) {
            return true;
        }
        boolean result;
        if (havePassword) {
            result = this.mLockPatternUtils.checkPassword(this.mOld, this.mCurrentUserId);
        } else {
            result = this.mLockPatternUtils.checkPattern(LockPatternUtils.stringToPattern(this.mOld), this.mCurrentUserId);
        }
        if (result) {
            return true;
        }
        getOutPrintWriter().println("Old password '" + this.mOld + "' didn't match");
        return false;
    }
}

package com.android.commands.locksettings;

import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import com.android.internal.os.BaseCommand;
import com.android.internal.widget.ILockSettings;
import java.io.FileDescriptor;
import java.io.PrintStream;

public final class LockSettingsCmd extends BaseCommand {
    private static final String USAGE = "usage: locksettings set-pattern [--old OLD_CREDENTIAL] NEW_PATTERN\n       locksettings set-pin [--old OLD_CREDENTIAL] NEW_PIN\n       locksettings set-password [--old OLD_CREDENTIAL] NEW_PASSWORD\n       locksettings clear [--old OLD_CREDENTIAL]\n       locksettings verify [--old OLD_CREDENTIAL]\n       locksettings set-disabled DISABLED\n       locksettings get-disabled\n\nflags: \n       --user USER_ID: specify the user, default value is current user\n\nlocksettings set-pattern: sets a pattern\n    A pattern is specified by a non-separated list of numbers that index the cell\n    on the pattern in a 1-based manner in left to right and top to bottom order,\n    i.e. the top-left cell is indexed with 1, whereas the bottom-right cell\n    is indexed with 9. Example: 1234\n\nlocksettings set-pin: sets a PIN\n\nlocksettings set-password: sets a password\n\nlocksettings clear: clears the unlock credential\n\nlocksettings verify: verifies the credential and unlocks the user\n\nlocksettings set-disabled: sets whether the lock screen should be disabled\n\nlocksettings get-disabled: retrieves whether the lock screen is disabled\n";

    public static void main(String[] args) {
        new LockSettingsCmd().run(args);
    }

    public void onShowUsage(PrintStream out) {
        out.println(USAGE);
    }

    public void onRun() throws Exception {
        ILockSettings.Stub.asInterface(ServiceManager.getService("lock_settings")).asBinder().shellCommand(FileDescriptor.in, FileDescriptor.out, FileDescriptor.err, getRawArgs(), new ShellCallback(), new ResultReceiver(null) {
            /* class com.android.commands.locksettings.LockSettingsCmd.AnonymousClass1 */
        });
    }
}

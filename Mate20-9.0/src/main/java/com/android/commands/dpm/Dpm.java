package com.android.commands.dpm;

import android.app.ActivityManager;
import android.app.admin.IDevicePolicyManager;
import android.content.ComponentName;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.internal.os.BaseCommand;
import java.io.PrintStream;

public final class Dpm extends BaseCommand {
    private static final String COMMAND_CLEAR_FREEZE_PERIOD_RECORD = "clear-freeze-period-record";
    private static final String COMMAND_FORCE_SECURITY_LOGS = "force-security-logs";
    private static final String COMMAND_REMOVE_ACTIVE_ADMIN = "remove-active-admin";
    private static final String COMMAND_SET_ACTIVE_ADMIN = "set-active-admin";
    private static final String COMMAND_SET_DEVICE_OWNER = "set-device-owner";
    private static final String COMMAND_SET_PROFILE_OWNER = "set-profile-owner";
    private ComponentName mComponent = null;
    private IDevicePolicyManager mDevicePolicyManager;
    private String mName = "";
    private int mUserId = 0;

    public static void main(String[] args) {
        new Dpm().run(args);
    }

    public void onShowUsage(PrintStream out) {
        out.println("usage: dpm [subcommand] [options]\nusage: dpm set-active-admin [ --user <USER_ID> | current ] <COMPONENT>\nusage: dpm set-device-owner [ --user <USER_ID> | current *EXPERIMENTAL* ] [ --name <NAME> ] <COMPONENT>\nusage: dpm set-profile-owner [ --user <USER_ID> | current ] [ --name <NAME> ] <COMPONENT>\nusage: dpm remove-active-admin [ --user <USER_ID> | current ] [ --name <NAME> ] <COMPONENT>\n\ndpm set-active-admin: Sets the given component as active admin for an existing user.\n\ndpm set-device-owner: Sets the given component as active admin, and its package as device owner.\n\ndpm set-profile-owner: Sets the given component as active admin and profile owner for an existing user.\n\ndpm remove-active-admin: Disables an active admin, the admin must have declared android:testOnly in the application in its manifest. This will also remove device and profile owners.\n\ndpm clear-freeze-period-record: clears framework-maintained record of past freeze periods that the device went through. For use during feature development to prevent triggering restriction on setting freeze periods.\n\ndpm force-security-logs: makes all security logs available to the DPC and triggers DeviceAdminReceiver.onSecurityLogsAvailable() if needed.");
    }

    public void onRun() throws Exception {
        this.mDevicePolicyManager = IDevicePolicyManager.Stub.asInterface(ServiceManager.getService("device_policy"));
        if (this.mDevicePolicyManager == null) {
            showError("Error: Could not access the Device Policy Manager. Is the system running?");
            return;
        }
        String command = nextArgRequired();
        char c = 65535;
        switch (command.hashCode()) {
            case -1791908857:
                if (command.equals(COMMAND_SET_DEVICE_OWNER)) {
                    c = 1;
                    break;
                }
                break;
            case -776610703:
                if (command.equals(COMMAND_REMOVE_ACTIVE_ADMIN)) {
                    c = 3;
                    break;
                }
                break;
            case -536624985:
                if (command.equals(COMMAND_CLEAR_FREEZE_PERIOD_RECORD)) {
                    c = 4;
                    break;
                }
                break;
            case 547934547:
                if (command.equals(COMMAND_SET_ACTIVE_ADMIN)) {
                    c = 0;
                    break;
                }
                break;
            case 639813476:
                if (command.equals(COMMAND_SET_PROFILE_OWNER)) {
                    c = 2;
                    break;
                }
                break;
            case 1325530298:
                if (command.equals(COMMAND_FORCE_SECURITY_LOGS)) {
                    c = 5;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                runSetActiveAdmin();
                break;
            case 1:
                runSetDeviceOwner();
                break;
            case 2:
                runSetProfileOwner();
                break;
            case 3:
                runRemoveActiveAdmin();
                break;
            case 4:
                runClearFreezePeriodRecord();
                break;
            case 5:
                runForceSecurityLogs();
                break;
            default:
                throw new IllegalArgumentException("unknown command '" + command + "'");
        }
    }

    private void runForceSecurityLogs() throws RemoteException, InterruptedException {
        while (true) {
            long toWait = this.mDevicePolicyManager.forceSecurityLogs();
            if (toWait == 0) {
                System.out.println("Success");
                return;
            }
            PrintStream printStream = System.out;
            printStream.println("We have to wait for " + toWait + " milliseconds...");
            Thread.sleep(toWait);
        }
    }

    private void parseArgs(boolean canHaveName) {
        String opt;
        while (true) {
            String nextOption = nextOption();
            opt = nextOption;
            if (nextOption == null) {
                this.mComponent = parseComponentName(nextArgRequired());
                return;
            } else if ("--user".equals(opt)) {
                String arg = nextArgRequired();
                if ("current".equals(arg) || "cur".equals(arg)) {
                    this.mUserId = -2;
                } else {
                    this.mUserId = parseInt(arg);
                }
                if (this.mUserId == -2) {
                    try {
                        this.mUserId = ActivityManager.getService().getCurrentUser().id;
                    } catch (RemoteException e) {
                        e.rethrowAsRuntimeException();
                    }
                }
            } else if (!canHaveName || !"--name".equals(opt)) {
            } else {
                this.mName = nextArgRequired();
            }
        }
        throw new IllegalArgumentException("Unknown option: " + opt);
    }

    private void runSetActiveAdmin() throws RemoteException {
        parseArgs(false);
        this.mDevicePolicyManager.setActiveAdmin(this.mComponent, true, this.mUserId);
        PrintStream printStream = System.out;
        printStream.println("Success: Active admin set to component " + this.mComponent.toShortString());
    }

    private void runSetDeviceOwner() throws RemoteException {
        parseArgs(true);
        this.mDevicePolicyManager.setActiveAdmin(this.mComponent, true, this.mUserId);
        try {
            if (this.mDevicePolicyManager.setDeviceOwner(this.mComponent, this.mName, this.mUserId)) {
                this.mDevicePolicyManager.setUserProvisioningState(3, this.mUserId);
                PrintStream printStream = System.out;
                printStream.println("Success: Device owner set to package " + this.mComponent);
                PrintStream printStream2 = System.out;
                printStream2.println("Active admin set to component " + this.mComponent.toShortString());
                return;
            }
            throw new RuntimeException("Can't set package " + this.mComponent + " as device owner.");
        } catch (Exception e) {
            this.mDevicePolicyManager.removeActiveAdmin(this.mComponent, 0);
            throw e;
        }
    }

    private void runRemoveActiveAdmin() throws RemoteException {
        parseArgs(false);
        this.mDevicePolicyManager.forceRemoveActiveAdmin(this.mComponent, this.mUserId);
        PrintStream printStream = System.out;
        printStream.println("Success: Admin removed " + this.mComponent);
    }

    private void runSetProfileOwner() throws RemoteException {
        parseArgs(true);
        this.mDevicePolicyManager.setActiveAdmin(this.mComponent, true, this.mUserId);
        try {
            if (this.mDevicePolicyManager.setProfileOwner(this.mComponent, this.mName, this.mUserId)) {
                this.mDevicePolicyManager.setUserProvisioningState(3, this.mUserId);
                PrintStream printStream = System.out;
                printStream.println("Success: Active admin and profile owner set to " + this.mComponent.toShortString() + " for user " + this.mUserId);
                return;
            }
            throw new RuntimeException("Can't set component " + this.mComponent.toShortString() + " as profile owner for user " + this.mUserId);
        } catch (Exception e) {
            this.mDevicePolicyManager.removeActiveAdmin(this.mComponent, this.mUserId);
            throw e;
        }
    }

    private void runClearFreezePeriodRecord() throws RemoteException {
        this.mDevicePolicyManager.clearSystemUpdatePolicyFreezePeriodRecord();
        System.out.println("Success");
    }

    private ComponentName parseComponentName(String component) {
        ComponentName cn = ComponentName.unflattenFromString(component);
        if (cn != null) {
            return cn;
        }
        throw new IllegalArgumentException("Invalid component " + component);
    }

    private int parseInt(String argument) {
        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer argument '" + argument + "'", e);
        }
    }
}

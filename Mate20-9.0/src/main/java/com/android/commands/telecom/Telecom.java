package com.android.commands.telecom;

import android.content.ComponentName;
import android.net.Uri;
import android.os.IUserManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import com.android.internal.os.BaseCommand;
import com.android.internal.telecom.ITelecomService;
import java.io.PrintStream;

public final class Telecom extends BaseCommand {
    private static final String COMMAND_GET_DEFAULT_DIALER = "get-default-dialer";
    private static final String COMMAND_GET_SYSTEM_DIALER = "get-system-dialer";
    private static final String COMMAND_REGISTER_PHONE_ACCOUNT = "register-phone-account";
    private static final String COMMAND_REGISTER_SIM_PHONE_ACCOUNT = "register-sim-phone-account";
    private static final String COMMAND_SET_DEFAULT_DIALER = "set-default-dialer";
    private static final String COMMAND_SET_PHONE_ACCOUNT_DISABLED = "set-phone-account-disabled";
    private static final String COMMAND_SET_PHONE_ACCOUNT_ENABLED = "set-phone-account-enabled";
    private static final String COMMAND_UNREGISTER_PHONE_ACCOUNT = "unregister-phone-account";
    private static final String COMMAND_WAIT_ON_HANDLERS = "wait-on-handlers";
    private String mAccountId;
    private ComponentName mComponent;
    private ITelecomService mTelecomService;
    private IUserManager mUserManager;

    public static void main(String[] args) {
        new Telecom().run(args);
    }

    public void onShowUsage(PrintStream out) {
        out.println("usage: telecom [subcommand] [options]\nusage: telecom set-phone-account-enabled <COMPONENT> <ID> <USER_SN>\nusage: telecom set-phone-account-disabled <COMPONENT> <ID> <USER_SN>\nusage: telecom register-phone-account <COMPONENT> <ID> <USER_SN> <LABEL>\nusage: telecom register-sim-phone-account <COMPONENT> <ID> <USER_SN> <LABEL> <ADDRESS>\nusage: telecom unregister-phone-account <COMPONENT> <ID> <USER_SN>\nusage: telecom set-default-dialer <PACKAGE>\nusage: telecom get-default-dialer\nusage: telecom get-system-dialer\nusage: telecom wait-on-handlers\n\ntelecom set-phone-account-enabled: Enables the given phone account, if it has \n already been registered with Telecom.\n\ntelecom set-phone-account-disabled: Disables the given phone account, if it \n has already been registered with telecom.\n\ntelecom set-default-dialer: Sets the default dialer to the given component. \n\ntelecom get-default-dialer: Displays the current default dialer. \n\ntelecom get-system-dialer: Displays the current system dialer. \n\ntelecom wait-on-handlers: Wait until all handlers finish their work. \n");
    }

    public void onRun() throws Exception {
        this.mTelecomService = ITelecomService.Stub.asInterface(ServiceManager.getService("telecom"));
        if (this.mTelecomService == null) {
            showError("Error: Could not access the Telecom Manager. Is the system running?");
            return;
        }
        this.mUserManager = IUserManager.Stub.asInterface(ServiceManager.getService("user"));
        if (this.mUserManager == null) {
            showError("Error: Could not access the User Manager. Is the system running?");
            return;
        }
        String command = nextArgRequired();
        char c = 65535;
        switch (command.hashCode()) {
            case -2025240323:
                if (command.equals(COMMAND_UNREGISTER_PHONE_ACCOUNT)) {
                    c = 4;
                    break;
                }
                break;
            case -1889448385:
                if (command.equals(COMMAND_WAIT_ON_HANDLERS)) {
                    c = 8;
                    break;
                }
                break;
            case -1447595602:
                if (command.equals(COMMAND_REGISTER_SIM_PHONE_ACCOUNT)) {
                    c = 3;
                    break;
                }
                break;
            case -645705193:
                if (command.equals(COMMAND_SET_PHONE_ACCOUNT_ENABLED)) {
                    c = 0;
                    break;
                }
                break;
            case -250191036:
                if (command.equals(COMMAND_GET_SYSTEM_DIALER)) {
                    c = 7;
                    break;
                }
                break;
            case -55640960:
                if (command.equals(COMMAND_GET_DEFAULT_DIALER)) {
                    c = 6;
                    break;
                }
                break;
            case 86724198:
                if (command.equals(COMMAND_SET_PHONE_ACCOUNT_DISABLED)) {
                    c = 1;
                    break;
                }
                break;
            case 864392692:
                if (command.equals(COMMAND_SET_DEFAULT_DIALER)) {
                    c = 5;
                    break;
                }
                break;
            case 2034443044:
                if (command.equals(COMMAND_REGISTER_PHONE_ACCOUNT)) {
                    c = 2;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                runSetPhoneAccountEnabled(true);
                break;
            case 1:
                runSetPhoneAccountEnabled(false);
                break;
            case 2:
                runRegisterPhoneAccount();
                break;
            case 3:
                runRegisterSimPhoneAccount();
                break;
            case 4:
                runUnregisterPhoneAccount();
                break;
            case 5:
                runSetDefaultDialer();
                break;
            case 6:
                runGetDefaultDialer();
                break;
            case 7:
                runGetSystemDialer();
                break;
            case 8:
                runWaitOnHandler();
                break;
            default:
                throw new IllegalArgumentException("unknown command '" + command + "'");
        }
    }

    private void runSetPhoneAccountEnabled(boolean enabled) throws RemoteException {
        PhoneAccountHandle handle = getPhoneAccountHandleFromArgs();
        if (this.mTelecomService.enablePhoneAccount(handle, enabled)) {
            PrintStream printStream = System.out;
            StringBuilder sb = new StringBuilder();
            sb.append("Success - ");
            sb.append(handle);
            sb.append(enabled ? " enabled." : " disabled.");
            printStream.println(sb.toString());
            return;
        }
        PrintStream printStream2 = System.out;
        printStream2.println("Error - is " + handle + " a valid PhoneAccount?");
    }

    private void runRegisterPhoneAccount() throws RemoteException {
        PhoneAccountHandle handle = getPhoneAccountHandleFromArgs();
        this.mTelecomService.registerPhoneAccount(PhoneAccount.builder(handle, nextArgRequired()).setCapabilities(2).build());
        PrintStream printStream = System.out;
        printStream.println("Success - " + handle + " registered.");
    }

    private void runRegisterSimPhoneAccount() throws RemoteException {
        PhoneAccountHandle handle = getPhoneAccountHandleFromArgs();
        String label = nextArgRequired();
        String address = nextArgRequired();
        this.mTelecomService.registerPhoneAccount(PhoneAccount.builder(handle, label).setAddress(Uri.parse(address)).setSubscriptionAddress(Uri.parse(address)).setCapabilities(6).setShortDescription(label).addSupportedUriScheme("tel").addSupportedUriScheme("voicemail").build());
        PrintStream printStream = System.out;
        printStream.println("Success - " + handle + " registered.");
    }

    private void runUnregisterPhoneAccount() throws RemoteException {
        PhoneAccountHandle handle = getPhoneAccountHandleFromArgs();
        this.mTelecomService.unregisterPhoneAccount(handle);
        PrintStream printStream = System.out;
        printStream.println("Success - " + handle + " unregistered.");
    }

    private void runSetDefaultDialer() throws RemoteException {
        String packageName = nextArgRequired();
        if (this.mTelecomService.setDefaultDialer(packageName)) {
            PrintStream printStream = System.out;
            printStream.println("Success - " + packageName + " set as default dialer.");
            return;
        }
        PrintStream printStream2 = System.out;
        printStream2.println("Error - " + packageName + " is not an installed Dialer app, \n or is already the default dialer.");
    }

    private void runGetDefaultDialer() throws RemoteException {
        System.out.println(this.mTelecomService.getDefaultDialerPackage());
    }

    private void runGetSystemDialer() throws RemoteException {
        System.out.println(this.mTelecomService.getSystemDialerPackage());
    }

    private void runWaitOnHandler() throws RemoteException {
    }

    private PhoneAccountHandle getPhoneAccountHandleFromArgs() throws RemoteException {
        ComponentName component = parseComponentName(nextArgRequired());
        String accountId = nextArgRequired();
        String userSnInStr = nextArgRequired();
        try {
            return new PhoneAccountHandle(component, accountId, UserHandle.of(this.mUserManager.getUserHandle(Integer.parseInt(userSnInStr))));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user serial number " + userSnInStr);
        }
    }

    private ComponentName parseComponentName(String component) {
        ComponentName cn = ComponentName.unflattenFromString(component);
        if (cn != null) {
            return cn;
        }
        throw new IllegalArgumentException("Invalid component " + component);
    }
}

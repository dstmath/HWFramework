package com.android.commands.telecom;

import android.content.ComponentName;
import android.net.Uri;
import android.os.IUserManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telecom.Log;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.text.TextUtils;
import com.android.internal.os.BaseCommand;
import com.android.internal.telecom.ITelecomService;
import com.android.internal.telephony.ITelephony;
import java.io.PrintStream;

public final class Telecom extends BaseCommand {
    private static final String COMMAND_ADD_OR_REMOVE_CALL_COMPANION_APP = "add-or-remove-call-companion-app";
    private static final String COMMAND_GET_DEFAULT_DIALER = "get-default-dialer";
    private static final String COMMAND_GET_MAX_PHONES = "get-max-phones";
    private static final String COMMAND_GET_SIM_CONFIG = "get-sim-config";
    private static final String COMMAND_GET_SYSTEM_DIALER = "get-system-dialer";
    private static final String COMMAND_REGISTER_PHONE_ACCOUNT = "register-phone-account";
    private static final String COMMAND_REGISTER_SIM_PHONE_ACCOUNT = "register-sim-phone-account";
    private static final String COMMAND_SET_DEFAULT_DIALER = "set-default-dialer";
    private static final String COMMAND_SET_PHONE_ACCOUNT_DISABLED = "set-phone-account-disabled";
    private static final String COMMAND_SET_PHONE_ACCOUNT_ENABLED = "set-phone-account-enabled";
    private static final String COMMAND_SET_PHONE_ACCOUNT_SUGGESTION_COMPONENT = "set-phone-acct-suggestion-component";
    private static final String COMMAND_SET_SIM_COUNT = "set-sim-count";
    private static final String COMMAND_SET_TEST_AUTO_MODE_APP = "set-test-auto-mode-app";
    private static final String COMMAND_SET_TEST_CALL_REDIRECTION_APP = "set-test-call-redirection-app";
    private static final String COMMAND_SET_TEST_CALL_SCREENING_APP = "set-test-call-screening-app";
    private static final String COMMAND_SET_USER_SELECTED_OUTGOING_PHONE_ACCOUNT = "set-user-selected-outgoing-phone-account";
    private static final String COMMAND_UNREGISTER_PHONE_ACCOUNT = "unregister-phone-account";
    private static final String COMMAND_WAIT_ON_HANDLERS = "wait-on-handlers";
    private String mAccountId;
    private ComponentName mComponent;
    private ITelecomService mTelecomService;
    private ITelephony mTelephonyService;
    private IUserManager mUserManager;

    public static void main(String[] args) {
        new Telecom().run(args);
    }

    public void onShowUsage(PrintStream out) {
        out.println("usage: telecom [subcommand] [options]\nusage: telecom set-phone-account-enabled <COMPONENT> <ID> <USER_SN>\nusage: telecom set-phone-account-disabled <COMPONENT> <ID> <USER_SN>\nusage: telecom register-phone-account <COMPONENT> <ID> <USER_SN> <LABEL>\nusage: telecom set-user-selected-outgoing-phone-account <COMPONENT> <ID> <USER_SN>\nusage: telecom set-test-call-redirection-app <PACKAGE>\nusage: telecom set-test-call-screening-app <PACKAGE>\nusage: telecom set-test-auto-mode-app <PACKAGE>\nusage: telecom set-phone-acct-suggestion-component <COMPONENT>\nusage: telecom add-or-remove-call-companion-app <PACKAGE> <1/0>\nusage: telecom register-sim-phone-account <COMPONENT> <ID> <USER_SN> <LABEL> <ADDRESS>\nusage: telecom unregister-phone-account <COMPONENT> <ID> <USER_SN>\nusage: telecom set-default-dialer <PACKAGE>\nusage: telecom get-default-dialer\nusage: telecom get-system-dialer\nusage: telecom wait-on-handlers\nusage: telecom set-sim-count <COUNT>\nusage: telecom get-sim-config\nusage: telecom get-max-phones\n\ntelecom set-phone-account-enabled: Enables the given phone account, if it has already been registered with Telecom.\n\ntelecom set-phone-account-disabled: Disables the given phone account, if it has already been registered with telecom.\n\ntelecom set-default-dialer: Sets the override default dialer to the given component; this will override whatever the dialer role is set to.\n\ntelecom get-default-dialer: Displays the current default dialer.\n\ntelecom get-system-dialer: Displays the current system dialer.\n\ntelecom wait-on-handlers: Wait until all handlers finish their work.\n\ntelecom set-sim-count: Set num SIMs (2 for DSDS, 1 for single SIM. This may restart the device.\n\ntelecom get-sim-config: Get the mSIM config string. \"DSDS\" for DSDS mode, or \"\" for single SIM\n\ntelecom get-max-phones: Get the max supported phones from the modem.\n");
    }

    public void onRun() throws Exception {
        this.mTelecomService = ITelecomService.Stub.asInterface(ServiceManager.getService("telecom"));
        if (this.mTelecomService == null) {
            Log.w(this, "onRun: Can't access telecom manager.", new Object[0]);
            showError("Error: Could not access the Telecom Manager. Is the system running?");
            return;
        }
        this.mTelephonyService = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
        if (this.mTelephonyService == null) {
            Log.w(this, "onRun: Can't access telephony service.", new Object[0]);
            showError("Error: Could not access the Telephony Service. Is the system running?");
            return;
        }
        this.mUserManager = IUserManager.Stub.asInterface(ServiceManager.getService("user"));
        if (this.mUserManager == null) {
            Log.w(this, "onRun: Can't access user manager.", new Object[0]);
            showError("Error: Could not access the User Manager. Is the system running?");
            return;
        }
        Log.i(this, "onRun: parsing command.", new Object[0]);
        String command = nextArgRequired();
        char c = 65535;
        switch (command.hashCode()) {
            case -2056063960:
                if (command.equals(COMMAND_SET_USER_SELECTED_OUTGOING_PHONE_ACCOUNT)) {
                    c = '\t';
                    break;
                }
                break;
            case -2025240323:
                if (command.equals(COMMAND_UNREGISTER_PHONE_ACCOUNT)) {
                    c = '\n';
                    break;
                }
                break;
            case -1889448385:
                if (command.equals(COMMAND_WAIT_ON_HANDLERS)) {
                    c = 14;
                    break;
                }
                break;
            case -1763366875:
                if (command.equals(COMMAND_GET_MAX_PHONES)) {
                    c = 17;
                    break;
                }
                break;
            case -1763082020:
                if (command.equals(COMMAND_ADD_OR_REMOVE_CALL_COMPANION_APP)) {
                    c = 5;
                    break;
                }
                break;
            case -1525813010:
                if (command.equals(COMMAND_SET_SIM_COUNT)) {
                    c = 15;
                    break;
                }
                break;
            case -1447595602:
                if (command.equals(COMMAND_REGISTER_SIM_PHONE_ACCOUNT)) {
                    c = '\b';
                    break;
                }
                break;
            case -853897535:
                if (command.equals(COMMAND_SET_TEST_CALL_REDIRECTION_APP)) {
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
            case -529505461:
                if (command.equals(COMMAND_SET_TEST_CALL_SCREENING_APP)) {
                    c = 4;
                    break;
                }
                break;
            case -250191036:
                if (command.equals(COMMAND_GET_SYSTEM_DIALER)) {
                    c = '\r';
                    break;
                }
                break;
            case -55640960:
                if (command.equals(COMMAND_GET_DEFAULT_DIALER)) {
                    c = '\f';
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
                    c = 11;
                    break;
                }
                break;
            case 1511426725:
                if (command.equals(COMMAND_SET_TEST_AUTO_MODE_APP)) {
                    c = 6;
                    break;
                }
                break;
            case 1715956687:
                if (command.equals(COMMAND_GET_SIM_CONFIG)) {
                    c = 16;
                    break;
                }
                break;
            case 2034443044:
                if (command.equals(COMMAND_REGISTER_PHONE_ACCOUNT)) {
                    c = 2;
                    break;
                }
                break;
            case 2081437924:
                if (command.equals(COMMAND_SET_PHONE_ACCOUNT_SUGGESTION_COMPONENT)) {
                    c = 7;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                runSetPhoneAccountEnabled(true);
                return;
            case 1:
                runSetPhoneAccountEnabled(false);
                return;
            case 2:
                runRegisterPhoneAccount();
                return;
            case 3:
                runSetTestCallRedirectionApp();
                return;
            case 4:
                runSetTestCallScreeningApp();
                return;
            case 5:
                runAddOrRemoveCallCompanionApp();
                return;
            case 6:
                runSetTestAutoModeApp();
                return;
            case 7:
                runSetTestPhoneAcctSuggestionComponent();
                return;
            case '\b':
                runRegisterSimPhoneAccount();
                return;
            case '\t':
                runSetUserSelectedOutgoingPhoneAccount();
                return;
            case '\n':
                runUnregisterPhoneAccount();
                return;
            case 11:
                runSetDefaultDialer();
                return;
            case '\f':
                runGetDefaultDialer();
                return;
            case '\r':
                runGetSystemDialer();
                return;
            case 14:
                runWaitOnHandler();
                return;
            case 15:
                runSetSimCount();
                return;
            case 16:
                runGetSimConfig();
                return;
            case 17:
                runGetMaxPhones();
                return;
            default:
                Log.w(this, "onRun: unknown command: %s", new Object[]{command});
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

    private void runSetTestCallRedirectionApp() throws RemoteException {
        this.mTelecomService.setTestDefaultCallRedirectionApp(nextArg());
    }

    private void runSetTestCallScreeningApp() throws RemoteException {
        this.mTelecomService.setTestDefaultCallScreeningApp(nextArg());
    }

    private void runAddOrRemoveCallCompanionApp() throws RemoteException {
        this.mTelecomService.addOrRemoveTestCallCompanionApp(nextArgRequired(), "1".equals(nextArgRequired()));
    }

    private void runSetTestAutoModeApp() throws RemoteException {
        this.mTelecomService.setTestAutoModeApp(nextArg());
    }

    private void runSetTestPhoneAcctSuggestionComponent() throws RemoteException {
        this.mTelecomService.setTestPhoneAcctSuggestionComponent(nextArg());
    }

    private void runSetUserSelectedOutgoingPhoneAccount() throws RemoteException {
        Log.i(this, "runSetUserSelectedOutgoingPhoneAccount", new Object[0]);
        PhoneAccountHandle handle = getPhoneAccountHandleFromArgs();
        this.mTelecomService.setUserSelectedOutgoingPhoneAccount(handle);
        PrintStream printStream = System.out;
        printStream.println("Success - " + handle + " set as default outgoing account.");
    }

    private void runUnregisterPhoneAccount() throws RemoteException {
        PhoneAccountHandle handle = getPhoneAccountHandleFromArgs();
        this.mTelecomService.unregisterPhoneAccount(handle);
        PrintStream printStream = System.out;
        printStream.println("Success - " + handle + " unregistered.");
    }

    private void runSetDefaultDialer() throws RemoteException {
        String packageName = nextArgRequired();
        this.mTelecomService.setTestDefaultDialer(packageName);
        PrintStream printStream = System.out;
        printStream.println("Success - " + packageName + " set as override default dialer.");
    }

    private void runGetDefaultDialer() throws RemoteException {
        System.out.println(this.mTelecomService.getDefaultDialerPackage());
    }

    private void runGetSystemDialer() throws RemoteException {
        System.out.println(this.mTelecomService.getSystemDialerPackage());
    }

    private void runWaitOnHandler() throws RemoteException {
    }

    private void runSetSimCount() throws RemoteException {
        if (!callerIsRoot()) {
            System.out.println("set-sim-count requires adb root");
            return;
        }
        int numSims = Integer.parseInt(nextArgRequired());
        PrintStream printStream = System.out;
        printStream.println("Setting sim count to " + numSims + ". Device may reboot");
        this.mTelephonyService.switchMultiSimConfig(numSims);
    }

    private void runGetSimConfig() throws RemoteException {
        System.out.println(SystemProperties.get("persist.radio.multisim.config"));
    }

    private void runGetMaxPhones() throws RemoteException {
        if (this.mTelephonyService.isMultiSimSupported("com.android.commands.telecom") == 0) {
            System.out.println("2");
        } else {
            System.out.println("1");
        }
    }

    private PhoneAccountHandle getPhoneAccountHandleFromArgs() throws RemoteException {
        if (TextUtils.isEmpty(this.mArgs.peekNextArg())) {
            return null;
        }
        ComponentName component = parseComponentName(nextArgRequired());
        String accountId = nextArgRequired();
        String userSnInStr = nextArgRequired();
        try {
            return new PhoneAccountHandle(component, accountId, UserHandle.of(this.mUserManager.getUserHandle(Integer.parseInt(userSnInStr))));
        } catch (NumberFormatException e) {
            Log.w(this, "getPhoneAccountHandleFromArgs - invalid user %s", new Object[]{userSnInStr});
            throw new IllegalArgumentException("Invalid user serial number " + userSnInStr);
        }
    }

    private boolean callerIsRoot() {
        return Process.myUid() == 0;
    }

    private ComponentName parseComponentName(String component) {
        ComponentName cn = ComponentName.unflattenFromString(component);
        if (cn != null) {
            return cn;
        }
        throw new IllegalArgumentException("Invalid component " + component);
    }
}

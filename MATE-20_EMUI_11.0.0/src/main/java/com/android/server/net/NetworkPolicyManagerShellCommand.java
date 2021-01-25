package com.android.server.net;

import android.content.Context;
import android.net.NetworkPolicyManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.os.ShellCommand;
import java.io.PrintWriter;

class NetworkPolicyManagerShellCommand extends ShellCommand {
    private final NetworkPolicyManagerService mInterface;
    private final WifiManager mWifiManager;

    NetworkPolicyManagerShellCommand(Context context, NetworkPolicyManagerService service) {
        this.mInterface = service;
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public int onCommand(String cmd) {
        boolean z;
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter pw = getOutPrintWriter();
        try {
            switch (cmd.hashCode()) {
                case -934610812:
                    if (cmd.equals("remove")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case 96417:
                    if (cmd.equals("add")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case 102230:
                    if (cmd.equals("get")) {
                        z = false;
                        break;
                    }
                    z = true;
                    break;
                case 113762:
                    if (cmd.equals("set")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case 3322014:
                    if (cmd.equals("list")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                default:
                    z = true;
                    break;
            }
            if (!z) {
                return runGet();
            }
            if (z) {
                return runSet();
            }
            if (z) {
                return runList();
            }
            if (z) {
                return runAdd();
            }
            if (!z) {
                return handleDefaultCommands(cmd);
            }
            return runRemove();
        } catch (RemoteException e) {
            pw.println("Remote exception: " + e);
            return -1;
        }
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("Network policy manager (netpolicy) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("");
        pw.println("  add restrict-background-whitelist UID");
        pw.println("    Adds a UID to the whitelist for restrict background usage.");
        pw.println("  add restrict-background-blacklist UID");
        pw.println("    Adds a UID to the blacklist for restrict background usage.");
        pw.println("  add app-idle-whitelist UID");
        pw.println("    Adds a UID to the temporary app idle whitelist.");
        pw.println("  get restrict-background");
        pw.println("    Gets the global restrict background usage status.");
        pw.println("  list wifi-networks [true|false]");
        pw.println("    Lists all saved wifi networks and whether they are metered or not.");
        pw.println("    If a boolean argument is passed, filters just the metered (or unmetered)");
        pw.println("    networks.");
        pw.println("  list restrict-background-whitelist");
        pw.println("    Lists UIDs that are whitelisted for restrict background usage.");
        pw.println("  list restrict-background-blacklist");
        pw.println("    Lists UIDs that are blacklisted for restrict background usage.");
        pw.println("  remove restrict-background-whitelist UID");
        pw.println("    Removes a UID from the whitelist for restrict background usage.");
        pw.println("  remove restrict-background-blacklist UID");
        pw.println("    Removes a UID from the blacklist for restrict background usage.");
        pw.println("  remove app-idle-whitelist UID");
        pw.println("    Removes a UID from the temporary app idle whitelist.");
        pw.println("  set metered-network ID [undefined|true|false]");
        pw.println("    Toggles whether the given wi-fi network is metered.");
        pw.println("  set restrict-background BOOLEAN");
        pw.println("    Sets the global restrict background usage status.");
        pw.println("  set sub-plan-owner subId [packageName]");
        pw.println("    Sets the data plan owner package for subId.");
    }

    private int runGet() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        String type = getNextArg();
        if (type == null) {
            pw.println("Error: didn't specify type of data to get");
            return -1;
        }
        if (!((type.hashCode() == -747095841 && type.equals("restrict-background")) ? false : true)) {
            return getRestrictBackground();
        }
        pw.println("Error: unknown get type '" + type + "'");
        return -1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x004b  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0073  */
    private int runSet() throws RemoteException {
        boolean z;
        PrintWriter pw = getOutPrintWriter();
        String type = getNextArg();
        if (type == null) {
            pw.println("Error: didn't specify type of data to set");
            return -1;
        }
        int hashCode = type.hashCode();
        if (hashCode != -983249079) {
            if (hashCode != -747095841) {
                if (hashCode == 1846940860 && type.equals("sub-plan-owner")) {
                    z = true;
                    if (z) {
                        return setMeteredWifiNetwork();
                    }
                    if (z) {
                        return setRestrictBackground();
                    }
                    if (z) {
                        return setSubPlanOwner();
                    }
                    pw.println("Error: unknown set type '" + type + "'");
                    return -1;
                }
            } else if (type.equals("restrict-background")) {
                z = true;
                if (z) {
                }
            }
        } else if (type.equals("metered-network")) {
            z = false;
            if (z) {
            }
        }
        z = true;
        if (z) {
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private int runList() throws RemoteException {
        boolean z;
        PrintWriter pw = getOutPrintWriter();
        String type = getNextArg();
        if (type == null) {
            pw.println("Error: didn't specify type of data to list");
            return -1;
        }
        switch (type.hashCode()) {
            case -1683867974:
                if (type.equals("app-idle-whitelist")) {
                    z = false;
                    break;
                }
                z = true;
                break;
            case -668534353:
                if (type.equals("restrict-background-blacklist")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -363534403:
                if (type.equals("wifi-networks")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 639570137:
                if (type.equals("restrict-background-whitelist")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            default:
                z = true;
                break;
        }
        if (!z) {
            return listAppIdleWhitelist();
        }
        if (z) {
            return listWifiNetworks();
        }
        if (z) {
            return listRestrictBackgroundWhitelist();
        }
        if (z) {
            return listRestrictBackgroundBlacklist();
        }
        pw.println("Error: unknown list type '" + type + "'");
        return -1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0072  */
    private int runAdd() throws RemoteException {
        boolean z;
        PrintWriter pw = getOutPrintWriter();
        String type = getNextArg();
        if (type == null) {
            pw.println("Error: didn't specify type of data to add");
            return -1;
        }
        int hashCode = type.hashCode();
        if (hashCode != -1683867974) {
            if (hashCode != -668534353) {
                if (hashCode == 639570137 && type.equals("restrict-background-whitelist")) {
                    z = false;
                    if (z) {
                        return addRestrictBackgroundWhitelist();
                    }
                    if (z) {
                        return addRestrictBackgroundBlacklist();
                    }
                    if (z) {
                        return addAppIdleWhitelist();
                    }
                    pw.println("Error: unknown add type '" + type + "'");
                    return -1;
                }
            } else if (type.equals("restrict-background-blacklist")) {
                z = true;
                if (z) {
                }
            }
        } else if (type.equals("app-idle-whitelist")) {
            z = true;
            if (z) {
            }
        }
        z = true;
        if (z) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0072  */
    private int runRemove() throws RemoteException {
        boolean z;
        PrintWriter pw = getOutPrintWriter();
        String type = getNextArg();
        if (type == null) {
            pw.println("Error: didn't specify type of data to remove");
            return -1;
        }
        int hashCode = type.hashCode();
        if (hashCode != -1683867974) {
            if (hashCode != -668534353) {
                if (hashCode == 639570137 && type.equals("restrict-background-whitelist")) {
                    z = false;
                    if (z) {
                        return removeRestrictBackgroundWhitelist();
                    }
                    if (z) {
                        return removeRestrictBackgroundBlacklist();
                    }
                    if (z) {
                        return removeAppIdleWhitelist();
                    }
                    pw.println("Error: unknown remove type '" + type + "'");
                    return -1;
                }
            } else if (type.equals("restrict-background-blacklist")) {
                z = true;
                if (z) {
                }
            }
        } else if (type.equals("app-idle-whitelist")) {
            z = true;
            if (z) {
            }
        }
        z = true;
        if (z) {
        }
    }

    private int listUidPolicies(String msg, int policy) throws RemoteException {
        return listUidList(msg, this.mInterface.getUidsWithPolicy(policy));
    }

    private int listUidList(String msg, int[] uids) {
        PrintWriter pw = getOutPrintWriter();
        pw.print(msg);
        pw.print(": ");
        if (uids.length == 0) {
            pw.println("none");
        } else {
            for (int uid : uids) {
                pw.print(uid);
                pw.print(' ');
            }
        }
        pw.println();
        return 0;
    }

    private int listRestrictBackgroundWhitelist() throws RemoteException {
        return listUidPolicies("Restrict background whitelisted UIDs", 4);
    }

    private int listRestrictBackgroundBlacklist() throws RemoteException {
        return listUidPolicies("Restrict background blacklisted UIDs", 1);
    }

    private int listAppIdleWhitelist() throws RemoteException {
        getOutPrintWriter();
        return listUidList("App Idle whitelisted UIDs", this.mInterface.getAppIdleWhitelist());
    }

    private int getRestrictBackground() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        pw.print("Restrict background status: ");
        pw.println(this.mInterface.getRestrictBackground() ? "enabled" : "disabled");
        return 0;
    }

    private int setRestrictBackground() throws RemoteException {
        int enabled = getNextBooleanArg();
        if (enabled < 0) {
            return enabled;
        }
        this.mInterface.setRestrictBackground(enabled > 0);
        return 0;
    }

    private int setSubPlanOwner() throws RemoteException {
        this.mInterface.setSubscriptionPlansOwner(Integer.parseInt(getNextArgRequired()), getNextArg());
        return 0;
    }

    private int setUidPolicy(int policy) throws RemoteException {
        int uid = getUidFromNextArg();
        if (uid < 0) {
            return uid;
        }
        this.mInterface.setUidPolicy(uid, policy);
        return 0;
    }

    private int resetUidPolicy(String errorMessage, int expectedPolicy) throws RemoteException {
        int uid = getUidFromNextArg();
        if (uid < 0) {
            return uid;
        }
        if (this.mInterface.getUidPolicy(uid) != expectedPolicy) {
            PrintWriter pw = getOutPrintWriter();
            pw.print("Error: UID ");
            pw.print(uid);
            pw.print(' ');
            pw.println(errorMessage);
            return -1;
        }
        this.mInterface.setUidPolicy(uid, 0);
        return 0;
    }

    private int addRestrictBackgroundWhitelist() throws RemoteException {
        return setUidPolicy(4);
    }

    private int removeRestrictBackgroundWhitelist() throws RemoteException {
        return resetUidPolicy("not whitelisted", 4);
    }

    private int addRestrictBackgroundBlacklist() throws RemoteException {
        return setUidPolicy(1);
    }

    private int removeRestrictBackgroundBlacklist() throws RemoteException {
        return resetUidPolicy("not blacklisted", 1);
    }

    private int setAppIdleWhitelist(boolean isWhitelisted) {
        int uid = getUidFromNextArg();
        if (uid < 0) {
            return uid;
        }
        this.mInterface.setAppIdleWhitelist(uid, isWhitelisted);
        return 0;
    }

    private int addAppIdleWhitelist() throws RemoteException {
        return setAppIdleWhitelist(true);
    }

    private int removeAppIdleWhitelist() throws RemoteException {
        return setAppIdleWhitelist(false);
    }

    private int listWifiNetworks() {
        int match;
        PrintWriter pw = getOutPrintWriter();
        String arg = getNextArg();
        if (arg == null) {
            match = 0;
        } else if (Boolean.parseBoolean(arg)) {
            match = 1;
        } else {
            match = 2;
        }
        for (WifiConfiguration config : this.mWifiManager.getConfiguredNetworks()) {
            if (arg == null || config.meteredOverride == match) {
                pw.print(NetworkPolicyManager.resolveNetworkId(config));
                pw.print(';');
                pw.println(overrideToString(config.meteredOverride));
            }
        }
        return 0;
    }

    private int setMeteredWifiNetwork() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        String networkId = getNextArg();
        if (networkId == null) {
            pw.println("Error: didn't specify networkId");
            return -1;
        }
        String arg = getNextArg();
        if (arg == null) {
            pw.println("Error: didn't specify meteredOverride");
            return -1;
        }
        this.mInterface.setWifiMeteredOverride(NetworkPolicyManager.resolveNetworkId(networkId), stringToOverride(arg));
        return -1;
    }

    private static String overrideToString(int override) {
        if (override == 1) {
            return "true";
        }
        if (override != 2) {
            return "none";
        }
        return "false";
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0029  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x002e A[RETURN] */
    private static int stringToOverride(String override) {
        char c;
        int hashCode = override.hashCode();
        if (hashCode != 3569038) {
            if (hashCode == 97196323 && override.equals("false")) {
                c = 1;
                if (c == 0) {
                    return 1;
                }
                if (c != 1) {
                    return 0;
                }
                return 2;
            }
        } else if (override.equals("true")) {
            c = 0;
            if (c == 0) {
            }
        }
        c = 65535;
        if (c == 0) {
        }
    }

    private int getNextBooleanArg() {
        PrintWriter pw = getOutPrintWriter();
        String arg = getNextArg();
        if (arg != null) {
            return Boolean.valueOf(arg).booleanValue() ? 1 : 0;
        }
        pw.println("Error: didn't specify BOOLEAN");
        return -1;
    }

    private int getUidFromNextArg() {
        PrintWriter pw = getOutPrintWriter();
        String arg = getNextArg();
        if (arg == null) {
            pw.println("Error: didn't specify UID");
            return -1;
        }
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            pw.println("Error: UID (" + arg + ") should be a number");
            return -2;
        }
    }
}

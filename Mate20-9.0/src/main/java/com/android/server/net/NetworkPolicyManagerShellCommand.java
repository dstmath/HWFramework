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

    /* JADX WARNING: Can't fix incorrect switch cases order */
    public int onCommand(String cmd) {
        char c;
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter pw = getOutPrintWriter();
        try {
            switch (cmd.hashCode()) {
                case -934610812:
                    if (cmd.equals("remove")) {
                        c = 4;
                        break;
                    }
                case 96417:
                    if (cmd.equals("add")) {
                        c = 3;
                        break;
                    }
                case 102230:
                    if (cmd.equals("get")) {
                        c = 0;
                        break;
                    }
                case 113762:
                    if (cmd.equals("set")) {
                        c = 1;
                        break;
                    }
                case 3322014:
                    if (cmd.equals("list")) {
                        c = 2;
                        break;
                    }
            }
            c = 65535;
            switch (c) {
                case 0:
                    return runGet();
                case 1:
                    return runSet();
                case 2:
                    return runList();
                case 3:
                    return runAdd();
                case 4:
                    return runRemove();
                default:
                    return handleDefaultCommands(cmd);
            }
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
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  add restrict-background-whitelist UID");
        pw.println("    Adds a UID to the whitelist for restrict background usage.");
        pw.println("  add restrict-background-blacklist UID");
        pw.println("    Adds a UID to the blacklist for restrict background usage.");
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

    /* JADX WARNING: Removed duplicated region for block: B:21:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0064  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0069  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x006e  */
    private int runSet() throws RemoteException {
        char c;
        PrintWriter pw = getOutPrintWriter();
        String type = getNextArg();
        if (type != null) {
            int hashCode = type.hashCode();
            if (hashCode == -983249079) {
                if (type.equals("metered-network")) {
                    c = 0;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                }
            } else if (hashCode == -747095841) {
                if (type.equals("restrict-background")) {
                    c = 1;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                }
            } else if (hashCode == 1846940860 && type.equals("sub-plan-owner")) {
                c = 2;
                switch (c) {
                    case 0:
                        return setMeteredWifiNetwork();
                    case 1:
                        return setRestrictBackground();
                    case 2:
                        return setSubPlanOwner();
                    default:
                        pw.println("Error: unknown set type '" + type + "'");
                        return -1;
                }
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
            }
        } else {
            pw.println("Error: didn't specify type of data to set");
            return -1;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0064  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0069  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x006e  */
    private int runList() throws RemoteException {
        char c;
        PrintWriter pw = getOutPrintWriter();
        String type = getNextArg();
        if (type != null) {
            int hashCode = type.hashCode();
            if (hashCode == -668534353) {
                if (type.equals("restrict-background-blacklist")) {
                    c = 2;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                }
            } else if (hashCode == -363534403) {
                if (type.equals("wifi-networks")) {
                    c = 0;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                }
            } else if (hashCode == 639570137 && type.equals("restrict-background-whitelist")) {
                c = 1;
                switch (c) {
                    case 0:
                        return listWifiNetworks();
                    case 1:
                        return listRestrictBackgroundWhitelist();
                    case 2:
                        return listRestrictBackgroundBlacklist();
                    default:
                        pw.println("Error: unknown list type '" + type + "'");
                        return -1;
                }
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
            }
        } else {
            pw.println("Error: didn't specify type of data to list");
            return -1;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x003a  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0054  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0059  */
    private int runAdd() throws RemoteException {
        boolean z;
        PrintWriter pw = getOutPrintWriter();
        String type = getNextArg();
        if (type != null) {
            int hashCode = type.hashCode();
            if (hashCode == -668534353) {
                if (type.equals("restrict-background-blacklist")) {
                    z = true;
                    switch (z) {
                        case false:
                            break;
                        case true:
                            break;
                    }
                }
            } else if (hashCode == 639570137 && type.equals("restrict-background-whitelist")) {
                z = false;
                switch (z) {
                    case false:
                        return addRestrictBackgroundWhitelist();
                    case true:
                        return addRestrictBackgroundBlacklist();
                    default:
                        pw.println("Error: unknown add type '" + type + "'");
                        return -1;
                }
            }
            z = true;
            switch (z) {
                case false:
                    break;
                case true:
                    break;
            }
        } else {
            pw.println("Error: didn't specify type of data to add");
            return -1;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x003a  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0054  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0059  */
    private int runRemove() throws RemoteException {
        boolean z;
        PrintWriter pw = getOutPrintWriter();
        String type = getNextArg();
        if (type != null) {
            int hashCode = type.hashCode();
            if (hashCode == -668534353) {
                if (type.equals("restrict-background-blacklist")) {
                    z = true;
                    switch (z) {
                        case false:
                            break;
                        case true:
                            break;
                    }
                }
            } else if (hashCode == 639570137 && type.equals("restrict-background-whitelist")) {
                z = false;
                switch (z) {
                    case false:
                        return removeRestrictBackgroundWhitelist();
                    case true:
                        return removeRestrictBackgroundBlacklist();
                    default:
                        pw.println("Error: unknown remove type '" + type + "'");
                        return -1;
                }
            }
            z = true;
            switch (z) {
                case false:
                    break;
                case true:
                    break;
            }
        } else {
            pw.println("Error: didn't specify type of data to remove");
            return -1;
        }
    }

    private int listUidPolicies(String msg, int policy) throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        int[] uids = this.mInterface.getUidsWithPolicy(policy);
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

    private int listWifiNetworks() {
        int match;
        PrintWriter pw = getOutPrintWriter();
        String arg = getNextArg();
        if (arg == null) {
            match = 0;
        } else if (Boolean.parseBoolean(arg) != 0) {
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
        switch (override) {
            case 1:
                return "true";
            case 2:
                return "false";
            default:
                return "none";
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x002a A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x002b  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x002d A[RETURN] */
    private static int stringToOverride(String override) {
        char c;
        int hashCode = override.hashCode();
        if (hashCode == 3569038) {
            if (override.equals("true")) {
                c = 0;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                }
            }
        } else if (hashCode == 97196323 && override.equals("false")) {
            c = 1;
            switch (c) {
                case 0:
                    return 1;
                case 1:
                    return 2;
                default:
                    return 0;
            }
        }
        c = 65535;
        switch (c) {
            case 0:
                break;
            case 1:
                break;
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

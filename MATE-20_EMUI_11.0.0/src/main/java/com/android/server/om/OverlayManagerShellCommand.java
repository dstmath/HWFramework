package com.android.server.om;

import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.os.RemoteException;
import android.os.ShellCommand;
import android.os.UserHandle;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

final class OverlayManagerShellCommand extends ShellCommand {
    private final IOverlayManager mInterface;

    OverlayManagerShellCommand(IOverlayManager iom) {
        this.mInterface = iom;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public int onCommand(String cmd) {
        boolean z;
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter err = getErrPrintWriter();
        try {
            switch (cmd.hashCode()) {
                case -1361113425:
                    if (cmd.equals("set-priority")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case -1298848381:
                    if (cmd.equals("enable")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case -794624300:
                    if (cmd.equals("enable-exclusive")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case 3322014:
                    if (cmd.equals("list")) {
                        z = false;
                        break;
                    }
                    z = true;
                    break;
                case 1671308008:
                    if (cmd.equals("disable")) {
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
                return runList();
            }
            if (z) {
                return runEnableDisable(true);
            }
            if (z) {
                return runEnableDisable(false);
            }
            if (z) {
                return runEnableExclusive();
            }
            if (!z) {
                return handleDefaultCommands(cmd);
            }
            return runSetPriority();
        } catch (IllegalArgumentException e) {
            err.println("Error: " + e.getMessage());
            return -1;
        } catch (RemoteException e2) {
            err.println("Remote exception: " + e2);
            return -1;
        }
    }

    public void onHelp() {
        PrintWriter out = getOutPrintWriter();
        out.println("Overlay manager (overlay) commands:");
        out.println("  help");
        out.println("    Print this help text.");
        out.println("  dump [--verbose] [--user USER_ID] [[FIELD] PACKAGE]");
        out.println("    Print debugging information about the overlay manager.");
        out.println("    With optional parameter PACKAGE, limit output to the specified");
        out.println("    package. With optional parameter FIELD, limit output to");
        out.println("    the value of that SettingsItem field. Field names are");
        out.println("    case insensitive and out.println the m prefix can be omitted,");
        out.println("    so the following are equivalent: mState, mstate, State, state.");
        out.println("  list [--user USER_ID] [PACKAGE]");
        out.println("    Print information about target and overlay packages.");
        out.println("    Overlay packages are printed in priority order. With optional");
        out.println("    parameter PACKAGE, limit output to the specified package.");
        out.println("  enable [--user USER_ID] PACKAGE");
        out.println("    Enable overlay package PACKAGE.");
        out.println("  disable [--user USER_ID] PACKAGE");
        out.println("    Disable overlay package PACKAGE.");
        out.println("  enable-exclusive [--user USER_ID] [--category] PACKAGE");
        out.println("    Enable overlay package PACKAGE and disable all other overlays for");
        out.println("    its target package. If the --category option is given, only disables");
        out.println("    other overlays in the same category.");
        out.println("  set-priority [--user USER_ID] PACKAGE PARENT|lowest|highest");
        out.println("    Change the priority of the overlay PACKAGE to be just higher than");
        out.println("    the priority of PACKAGE_PARENT If PARENT is the special keyword");
        out.println("    'lowest', change priority of PACKAGE to the lowest priority.");
        out.println("    If PARENT is the special keyword 'highest', change priority of");
        out.println("    PACKAGE to the highest priority.");
    }

    private int runList() throws RemoteException {
        PrintWriter out = getOutPrintWriter();
        PrintWriter err = getErrPrintWriter();
        int userId = 0;
        while (true) {
            String opt = getNextOption();
            if (opt != null) {
                char c = 65535;
                if (opt.hashCode() == 1333469547 && opt.equals("--user")) {
                    c = 0;
                }
                if (c != 0) {
                    err.println("Error: Unknown option: " + opt);
                    return 1;
                }
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                String packageName = getNextArg();
                if (packageName != null) {
                    List<OverlayInfo> overlaysForTarget = this.mInterface.getOverlayInfosForTarget(packageName, userId);
                    if (overlaysForTarget.isEmpty()) {
                        OverlayInfo info = this.mInterface.getOverlayInfo(packageName, userId);
                        if (info != null) {
                            printListOverlay(out, info);
                        }
                        return 0;
                    }
                    out.println(packageName);
                    int n = overlaysForTarget.size();
                    for (int i = 0; i < n; i++) {
                        printListOverlay(out, overlaysForTarget.get(i));
                    }
                    return 0;
                }
                Map<String, List<OverlayInfo>> allOverlays = this.mInterface.getAllOverlays(userId);
                for (String targetPackageName : allOverlays.keySet()) {
                    out.println(targetPackageName);
                    List<OverlayInfo> overlaysForTarget2 = allOverlays.get(targetPackageName);
                    int n2 = overlaysForTarget2.size();
                    for (int i2 = 0; i2 < n2; i2++) {
                        printListOverlay(out, overlaysForTarget2.get(i2));
                    }
                    out.println();
                }
                return 0;
            }
        }
    }

    private void printListOverlay(PrintWriter out, OverlayInfo oi) {
        String status;
        int i = oi.state;
        if (i == 2) {
            status = "[ ]";
        } else if (i == 3 || i == 6) {
            status = "[x]";
        } else {
            status = "---";
        }
        out.println(String.format("%s %s", status, oi.packageName));
    }

    private int runEnableDisable(boolean enable) throws RemoteException {
        PrintWriter err = getErrPrintWriter();
        int userId = 0;
        while (true) {
            String opt = getNextOption();
            if (opt != null) {
                char c = 65535;
                if (opt.hashCode() == 1333469547 && opt.equals("--user")) {
                    c = 0;
                }
                if (c != 0) {
                    err.println("Error: Unknown option: " + opt);
                    return 1;
                }
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                return 1 ^ (this.mInterface.setEnabled(getNextArgRequired(), enable, userId) ? 1 : 0);
            }
        }
    }

    private int runEnableExclusive() throws RemoteException {
        PrintWriter err = getErrPrintWriter();
        int userId = 0;
        boolean inCategory = false;
        while (true) {
            String opt = getNextOption();
            if (opt != null) {
                char c = 65535;
                int hashCode = opt.hashCode();
                if (hashCode != 66265758) {
                    if (hashCode == 1333469547 && opt.equals("--user")) {
                        c = 0;
                    }
                } else if (opt.equals("--category")) {
                    c = 1;
                }
                if (c == 0) {
                    userId = UserHandle.parseUserArg(getNextArgRequired());
                } else if (c != 1) {
                    err.println("Error: Unknown option: " + opt);
                    return 1;
                } else {
                    inCategory = true;
                }
            } else {
                String overlay = getNextArgRequired();
                if (inCategory) {
                    return 1 ^ (this.mInterface.setEnabledExclusiveInCategory(overlay, userId) ? 1 : 0);
                }
                return 1 ^ (this.mInterface.setEnabledExclusive(overlay, true, userId) ? 1 : 0);
            }
        }
    }

    private int runSetPriority() throws RemoteException {
        PrintWriter err = getErrPrintWriter();
        int userId = 0;
        while (true) {
            String opt = getNextOption();
            if (opt != null) {
                char c = 65535;
                if (opt.hashCode() == 1333469547 && opt.equals("--user")) {
                    c = 0;
                }
                if (c != 0) {
                    err.println("Error: Unknown option: " + opt);
                    return 1;
                }
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                String packageName = getNextArgRequired();
                String newParentPackageName = getNextArgRequired();
                if ("highest".equals(newParentPackageName)) {
                    return 1 ^ (this.mInterface.setHighestPriority(packageName, userId) ? 1 : 0);
                }
                if ("lowest".equals(newParentPackageName)) {
                    return 1 ^ (this.mInterface.setLowestPriority(packageName, userId) ? 1 : 0);
                }
                return 1 ^ (this.mInterface.setPriority(packageName, newParentPackageName, userId) ? 1 : 0);
            }
        }
    }
}

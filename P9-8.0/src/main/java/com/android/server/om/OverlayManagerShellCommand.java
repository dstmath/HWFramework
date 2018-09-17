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

    public int onCommand(String cmd) {
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter err = getErrPrintWriter();
        try {
            if (cmd.equals("list")) {
                return runList();
            }
            if (cmd.equals("enable")) {
                return runEnableDisable(true);
            }
            if (cmd.equals("disable")) {
                return runEnableDisable(false);
            }
            if (cmd.equals("set-priority")) {
                return runSetPriority();
            }
            return handleDefaultCommands(cmd);
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
        out.println("  dump [--verbose] [--user USER_ID] [PACKAGE [PACKAGE [...]]]");
        out.println("    Print debugging information about the overlay manager.");
        out.println("  list [--user USER_ID] [PACKAGE [PACKAGE [...]]]");
        out.println("    Print information about target and overlay packages.");
        out.println("    Overlay packages are printed in priority order. With optional");
        out.println("    parameters PACKAGEs, limit output to the specified packages");
        out.println("    but include more information about each package.");
        out.println("  enable [--user USER_ID] PACKAGE");
        out.println("    Enable overlay package PACKAGE.");
        out.println("  disable [--user USER_ID] PACKAGE");
        out.println("    Disable overlay package PACKAGE.");
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
            if (opt == null) {
                Map<String, List<OverlayInfo>> allOverlays = this.mInterface.getAllOverlays(userId);
                for (String targetPackageName : allOverlays.keySet()) {
                    out.println(targetPackageName);
                    List<OverlayInfo> overlaysForTarget = (List) allOverlays.get(targetPackageName);
                    int N = overlaysForTarget.size();
                    for (int i = 0; i < N; i++) {
                        String status;
                        switch (((OverlayInfo) overlaysForTarget.get(i)).state) {
                            case 2:
                                status = "[ ]";
                                break;
                            case 3:
                                status = "[x]";
                                break;
                            default:
                                status = "---";
                                break;
                        }
                        out.println(String.format("%s %s", new Object[]{status, ((OverlayInfo) overlaysForTarget.get(i)).packageName}));
                    }
                    out.println();
                }
                return 0;
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                err.println("Error: Unknown option: " + opt);
                return 1;
            }
        }
    }

    private int runEnableDisable(boolean enable) throws RemoteException {
        int i = 1;
        PrintWriter err = getErrPrintWriter();
        int userId = 0;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                if (this.mInterface.setEnabled(getNextArgRequired(), enable, userId)) {
                    i = 0;
                }
                return i;
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                err.println("Error: Unknown option: " + opt);
                return 1;
            }
        }
    }

    private int runSetPriority() throws RemoteException {
        int i = 0;
        PrintWriter err = getErrPrintWriter();
        int userId = 0;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                String packageName = getNextArgRequired();
                String newParentPackageName = getNextArgRequired();
                if ("highest".equals(newParentPackageName)) {
                    if (!this.mInterface.setHighestPriority(packageName, userId)) {
                        i = 1;
                    }
                    return i;
                } else if ("lowest".equals(newParentPackageName)) {
                    if (!this.mInterface.setLowestPriority(packageName, userId)) {
                        i = 1;
                    }
                    return i;
                } else {
                    if (!this.mInterface.setPriority(packageName, newParentPackageName, userId)) {
                        i = 1;
                    }
                    return i;
                }
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                err.println("Error: Unknown option: " + opt);
                return 1;
            }
        }
    }
}

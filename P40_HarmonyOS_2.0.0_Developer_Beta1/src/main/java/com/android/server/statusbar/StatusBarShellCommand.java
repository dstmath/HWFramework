package com.android.server.statusbar;

import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ShellCommand;
import android.service.quicksettings.TileService;
import android.util.Pair;
import java.io.PrintWriter;

public class StatusBarShellCommand extends ShellCommand {
    private static final IBinder sToken = new StatusBarShellCommandToken();
    private final Context mContext;
    private final StatusBarManagerService mInterface;

    public StatusBarShellCommand(StatusBarManagerService service, Context context) {
        this.mInterface = service;
        this.mContext = context;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public int onCommand(String cmd) {
        char c;
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        try {
            switch (cmd.hashCode()) {
                case -1282000806:
                    if (cmd.equals("add-tile")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -1239176554:
                    if (cmd.equals("get-status-icons")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -1052548778:
                    if (cmd.equals("send-disable-flag")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case -823073837:
                    if (cmd.equals("click-tile")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -632085587:
                    if (cmd.equals("collapse")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -339726761:
                    if (cmd.equals("remove-tile")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 901899220:
                    if (cmd.equals("disable-for-setup")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 1612300298:
                    if (cmd.equals("check-support")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 1629310709:
                    if (cmd.equals("expand-notifications")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1672031734:
                    if (cmd.equals("expand-settings")) {
                        c = 1;
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
                    return runExpandNotifications();
                case 1:
                    return runExpandSettings();
                case 2:
                    return runCollapse();
                case 3:
                    return runAddTile();
                case 4:
                    return runRemoveTile();
                case 5:
                    return runClickTile();
                case 6:
                    getOutPrintWriter().println(String.valueOf(TileService.isQuickSettingsSupported()));
                    return 0;
                case 7:
                    return runGetStatusIcons();
                case '\b':
                    return runDisableForSetup();
                case '\t':
                    return runSendDisableFlag();
                default:
                    return handleDefaultCommands(cmd);
            }
        } catch (RemoteException e) {
            getOutPrintWriter().println("Remote exception: " + e);
            return -1;
        }
    }

    private int runAddTile() throws RemoteException {
        this.mInterface.addTile(ComponentName.unflattenFromString(getNextArgRequired()));
        return 0;
    }

    private int runRemoveTile() throws RemoteException {
        this.mInterface.remTile(ComponentName.unflattenFromString(getNextArgRequired()));
        return 0;
    }

    private int runClickTile() throws RemoteException {
        this.mInterface.clickTile(ComponentName.unflattenFromString(getNextArgRequired()));
        return 0;
    }

    private int runCollapse() throws RemoteException {
        this.mInterface.collapsePanels();
        return 0;
    }

    private int runExpandSettings() throws RemoteException {
        this.mInterface.expandSettingsPanel(null);
        return 0;
    }

    private int runExpandNotifications() throws RemoteException {
        this.mInterface.expandNotificationsPanel();
        return 0;
    }

    private int runGetStatusIcons() {
        PrintWriter pw = getOutPrintWriter();
        for (String icon : this.mInterface.getStatusBarIcons()) {
            pw.println(icon);
        }
        return 0;
    }

    private int runDisableForSetup() {
        String arg = getNextArgRequired();
        String pkg = this.mContext.getPackageName();
        if (Boolean.parseBoolean(arg)) {
            this.mInterface.disable(61145088, sToken, pkg);
            this.mInterface.disable2(16, sToken, pkg);
        } else {
            this.mInterface.disable(0, sToken, pkg);
            this.mInterface.disable2(0, sToken, pkg);
        }
        return 0;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0054, code lost:
        if (r4.equals("search") != false) goto L_0x0058;
     */
    private int runSendDisableFlag() {
        String pkg = this.mContext.getPackageName();
        StatusBarManager.DisableInfo info = new StatusBarManager.DisableInfo();
        String arg = getNextArg();
        while (true) {
            boolean z = false;
            if (arg != null) {
                switch (arg.hashCode()) {
                    case -906336856:
                        break;
                    case -755976775:
                        if (arg.equals("notification-alerts")) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    case 3208415:
                        if (arg.equals("home")) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    case 1011652819:
                        if (arg.equals("statusbar-expansion")) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    case 1082295672:
                        if (arg.equals("recents")) {
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
                    info.setSearchDisabled(true);
                } else if (z) {
                    info.setNagivationHomeDisabled(true);
                } else if (z) {
                    info.setRecentsDisabled(true);
                } else if (z) {
                    info.setNotificationPeekingDisabled(true);
                } else if (z) {
                    info.setStatusBarExpansionDisabled(true);
                }
                arg = getNextArg();
            } else {
                Pair<Integer, Integer> flagPair = info.toFlags();
                this.mInterface.disable(((Integer) flagPair.first).intValue(), sToken, pkg);
                this.mInterface.disable2(((Integer) flagPair.second).intValue(), sToken, pkg);
                return 0;
            }
        }
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("Status bar commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("");
        pw.println("  expand-notifications");
        pw.println("    Open the notifications panel.");
        pw.println("");
        pw.println("  expand-settings");
        pw.println("    Open the notifications panel and expand quick settings if present.");
        pw.println("");
        pw.println("  collapse");
        pw.println("    Collapse the notifications and settings panel.");
        pw.println("");
        pw.println("  add-tile COMPONENT");
        pw.println("    Add a TileService of the specified component");
        pw.println("");
        pw.println("  remove-tile COMPONENT");
        pw.println("    Remove a TileService of the specified component");
        pw.println("");
        pw.println("  click-tile COMPONENT");
        pw.println("    Click on a TileService of the specified component");
        pw.println("");
        pw.println("  check-support");
        pw.println("    Check if this device supports QS + APIs");
        pw.println("");
        pw.println("  get-status-icons");
        pw.println("    Print the list of status bar icons and the order they appear in");
        pw.println("");
        pw.println("  disable-for-setup DISABLE");
        pw.println("    If true, disable status bar components unsuitable for device setup");
        pw.println("");
        pw.println("  send-disable-flag FLAG...");
        pw.println("    Send zero or more disable flags (parsed individually) to StatusBarManager");
        pw.println("    Valid options:");
        pw.println("        <blank>             - equivalent to \"none\"");
        pw.println("        none                - re-enables all components");
        pw.println("        search              - disable search");
        pw.println("        home                - disable naviagation home");
        pw.println("        recents             - disable recents/overview");
        pw.println("        notification-peek   - disable notification peeking");
        pw.println("        statusbar-expansion - disable status bar expansion");
        pw.println("");
    }

    private static final class StatusBarShellCommandToken extends Binder {
        private StatusBarShellCommandToken() {
        }
    }
}

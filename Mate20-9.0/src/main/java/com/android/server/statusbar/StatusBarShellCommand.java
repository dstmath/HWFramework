package com.android.server.statusbar;

import android.content.ComponentName;
import android.os.RemoteException;
import android.os.ShellCommand;
import android.service.quicksettings.TileService;
import java.io.PrintWriter;

public class StatusBarShellCommand extends ShellCommand {
    private final StatusBarManagerService mInterface;

    public StatusBarShellCommand(StatusBarManagerService service) {
        this.mInterface = service;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
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
                case -1239176554:
                    if (cmd.equals("get-status-icons")) {
                        c = 7;
                        break;
                    }
                case -823073837:
                    if (cmd.equals("click-tile")) {
                        c = 5;
                        break;
                    }
                case -632085587:
                    if (cmd.equals("collapse")) {
                        c = 2;
                        break;
                    }
                case -339726761:
                    if (cmd.equals("remove-tile")) {
                        c = 4;
                        break;
                    }
                case 1612300298:
                    if (cmd.equals("check-support")) {
                        c = 6;
                        break;
                    }
                case 1629310709:
                    if (cmd.equals("expand-notifications")) {
                        c = 0;
                        break;
                    }
                case 1672031734:
                    if (cmd.equals("expand-settings")) {
                        c = 1;
                        break;
                    }
            }
            c = 65535;
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

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("Status bar commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  expand-notifications");
        pw.println("    Open the notifications panel.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  expand-settings");
        pw.println("    Open the notifications panel and expand quick settings if present.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  collapse");
        pw.println("    Collapse the notifications and settings panel.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  add-tile COMPONENT");
        pw.println("    Add a TileService of the specified component");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  remove-tile COMPONENT");
        pw.println("    Remove a TileService of the specified component");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  click-tile COMPONENT");
        pw.println("    Click on a TileService of the specified component");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  check-support");
        pw.println("    Check if this device supports QS + APIs");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  get-status-icons");
        pw.println("    Print the list of status bar icons and the order they appear in");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
    }
}

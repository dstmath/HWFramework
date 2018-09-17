package com.android.server.statusbar;

import android.content.ComponentName;
import android.os.RemoteException;
import android.os.ShellCommand;
import android.service.quicksettings.TileService;
import com.android.internal.statusbar.IStatusBarService;
import java.io.PrintWriter;

public class StatusBarShellCommand extends ShellCommand {
    private final IStatusBarService mInterface;

    public StatusBarShellCommand(StatusBarManagerService service) {
        this.mInterface = service;
    }

    public int onCommand(String cmd) {
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        try {
            if (cmd.equals("expand-notifications")) {
                return runExpandNotifications();
            }
            if (cmd.equals("expand-settings")) {
                return runExpandSettings();
            }
            if (cmd.equals("collapse")) {
                return runCollapse();
            }
            if (cmd.equals("add-tile")) {
                return runAddTile();
            }
            if (cmd.equals("remove-tile")) {
                return runRemoveTile();
            }
            if (cmd.equals("click-tile")) {
                return runClickTile();
            }
            if (!cmd.equals("check-support")) {
                return handleDefaultCommands(cmd);
            }
            getOutPrintWriter().println(String.valueOf(TileService.isQuickSettingsSupported()));
            return 0;
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
    }
}

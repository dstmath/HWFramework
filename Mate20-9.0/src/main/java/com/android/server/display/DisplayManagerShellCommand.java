package com.android.server.display;

import android.content.Intent;
import android.os.ShellCommand;
import com.android.server.display.DisplayManagerService;
import java.io.PrintWriter;

class DisplayManagerShellCommand extends ShellCommand {
    private static final String TAG = "DisplayManagerShellCommand";
    private final DisplayManagerService.BinderService mService;

    DisplayManagerShellCommand(DisplayManagerService.BinderService service) {
        this.mService = service;
    }

    public int onCommand(String cmd) {
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter outPrintWriter = getOutPrintWriter();
        char c = 65535;
        int hashCode = cmd.hashCode();
        if (hashCode != -1505467592) {
            if (hashCode == 1604823708 && cmd.equals("set-brightness")) {
                c = 0;
            }
        } else if (cmd.equals("reset-brightness-configuration")) {
            c = 1;
        }
        switch (c) {
            case 0:
                return setBrightness();
            case 1:
                return resetBrightnessConfiguration();
            default:
                return handleDefaultCommands(cmd);
        }
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("Display manager commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println();
        pw.println("  set-brightness BRIGHTNESS");
        pw.println("    Sets the current brightness to BRIGHTNESS (a number between 0 and 1).");
        pw.println("  reset-brightness-configuration");
        pw.println("    Reset the brightness to its default configuration.");
        pw.println();
        Intent.printIntentArgsHelp(pw, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
    }

    private int setBrightness() {
        String brightnessText = getNextArg();
        if (brightnessText == null) {
            getErrPrintWriter().println("Error: no brightness specified");
            return 1;
        }
        float brightness = -1.0f;
        try {
            brightness = Float.parseFloat(brightnessText);
        } catch (NumberFormatException e) {
        }
        if (brightness < 0.0f || brightness > 1.0f) {
            getErrPrintWriter().println("Error: brightness should be a number between 0 and 1");
            return 1;
        }
        this.mService.setBrightness(((int) brightness) * 255);
        return 0;
    }

    private int resetBrightnessConfiguration() {
        this.mService.resetBrightnessConfiguration();
        return 0;
    }
}

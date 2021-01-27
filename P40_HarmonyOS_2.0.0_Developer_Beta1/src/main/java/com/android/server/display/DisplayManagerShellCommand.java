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
        getOutPrintWriter();
        char c = 65535;
        switch (cmd.hashCode()) {
            case -1505467592:
                if (cmd.equals("reset-brightness-configuration")) {
                    c = 1;
                    break;
                }
                break;
            case -731435249:
                if (cmd.equals("dwb-logging-enable")) {
                    c = 4;
                    break;
                }
                break;
            case 483509981:
                if (cmd.equals("ab-logging-enable")) {
                    c = 2;
                    break;
                }
                break;
            case 847215243:
                if (cmd.equals("dwb-set-cct")) {
                    c = 6;
                    break;
                }
                break;
            case 1089842382:
                if (cmd.equals("ab-logging-disable")) {
                    c = 3;
                    break;
                }
                break;
            case 1604823708:
                if (cmd.equals("set-brightness")) {
                    c = 0;
                    break;
                }
                break;
            case 2081245916:
                if (cmd.equals("dwb-logging-disable")) {
                    c = 5;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return setBrightness();
            case 1:
                return resetBrightnessConfiguration();
            case 2:
                return setAutoBrightnessLoggingEnabled(true);
            case 3:
                return setAutoBrightnessLoggingEnabled(false);
            case 4:
                return setDisplayWhiteBalanceLoggingEnabled(true);
            case 5:
                return setDisplayWhiteBalanceLoggingEnabled(false);
            case 6:
                return setAmbientColorTemperatureOverride();
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
        pw.println("  ab-logging-enable");
        pw.println("    Enable auto-brightness logging.");
        pw.println("  ab-logging-disable");
        pw.println("    Disable auto-brightness logging.");
        pw.println("  dwb-logging-enable");
        pw.println("    Enable display white-balance logging.");
        pw.println("  dwb-logging-disable");
        pw.println("    Disable display white-balance logging.");
        pw.println("  dwb-set-cct CCT");
        pw.println("    Sets the ambient color temperature override to CCT (use -1 to disable).");
        pw.println();
        Intent.printIntentArgsHelp(pw, "");
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
        this.mService.setBrightness((int) (255.0f * brightness));
        return 0;
    }

    private int resetBrightnessConfiguration() {
        this.mService.resetBrightnessConfiguration();
        return 0;
    }

    private int setAutoBrightnessLoggingEnabled(boolean enabled) {
        this.mService.setAutoBrightnessLoggingEnabled(enabled);
        return 0;
    }

    private int setDisplayWhiteBalanceLoggingEnabled(boolean enabled) {
        this.mService.setDisplayWhiteBalanceLoggingEnabled(enabled);
        return 0;
    }

    private int setAmbientColorTemperatureOverride() {
        String cctText = getNextArg();
        if (cctText == null) {
            getErrPrintWriter().println("Error: no cct specified");
            return 1;
        }
        try {
            this.mService.setAmbientColorTemperatureOverride(Float.parseFloat(cctText));
            return 0;
        } catch (NumberFormatException e) {
            getErrPrintWriter().println("Error: cct should be a number");
            return 1;
        }
    }
}

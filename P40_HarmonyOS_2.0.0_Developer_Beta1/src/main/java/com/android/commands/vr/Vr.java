package com.android.commands.vr;

import android.app.Vr2dDisplayProperties;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.vr.IVrManager;
import com.android.internal.os.BaseCommand;
import java.io.PrintStream;

public final class Vr extends BaseCommand {
    private static final String COMMAND_ENABLE_VD = "enable-virtual-display";
    private static final String COMMAND_SET_PERSISTENT_VR_MODE_ENABLED = "set-persistent-vr-mode-enabled";
    private static final String COMMAND_SET_VR2D_DISPLAY_PROPERTIES = "set-display-props";
    private IVrManager mVrService;

    public static void main(String[] args) {
        new Vr().run(args);
    }

    public void onShowUsage(PrintStream out) {
        out.println("usage: vr [subcommand]\nusage: vr set-persistent-vr-mode-enabled [true|false]\nusage: vr set-display-props [width] [height] [dpi]\nusage: vr enable-virtual-display [true|false]\n");
    }

    public void onRun() throws Exception {
        this.mVrService = IVrManager.Stub.asInterface(ServiceManager.getService("vrmanager"));
        if (this.mVrService == null) {
            showError("Error: Could not access the Vr Manager. Is the system running?");
            return;
        }
        String command = nextArgRequired();
        char c = 65535;
        int hashCode = command.hashCode();
        if (hashCode != -190799946) {
            if (hashCode != -111561094) {
                if (hashCode == 2040743325 && command.equals(COMMAND_SET_PERSISTENT_VR_MODE_ENABLED)) {
                    c = 1;
                }
            } else if (command.equals(COMMAND_SET_VR2D_DISPLAY_PROPERTIES)) {
                c = 0;
            }
        } else if (command.equals(COMMAND_ENABLE_VD)) {
            c = 2;
        }
        if (c == 0) {
            runSetVr2dDisplayProperties();
        } else if (c == 1) {
            runSetPersistentVrModeEnabled();
        } else if (c == 2) {
            runEnableVd();
        } else {
            throw new IllegalArgumentException("unknown command '" + command + "'");
        }
    }

    private void runSetVr2dDisplayProperties() throws RemoteException {
        try {
            this.mVrService.setVr2dDisplayProperties(new Vr2dDisplayProperties(Integer.parseInt(nextArgRequired()), Integer.parseInt(nextArgRequired()), Integer.parseInt(nextArgRequired())));
        } catch (RemoteException re) {
            PrintStream printStream = System.err;
            printStream.println("Error: Can't set persistent mode " + re);
        }
    }

    private void runEnableVd() throws RemoteException {
        Vr2dDisplayProperties.Builder builder = new Vr2dDisplayProperties.Builder();
        String value = nextArgRequired();
        if ("true".equals(value)) {
            builder.setEnabled(true);
        } else if ("false".equals(value)) {
            builder.setEnabled(false);
        }
        try {
            this.mVrService.setVr2dDisplayProperties(builder.build());
        } catch (RemoteException re) {
            PrintStream printStream = System.err;
            printStream.println("Error: Can't enable (" + value + ") virtual display" + re);
        }
    }

    private void runSetPersistentVrModeEnabled() throws RemoteException {
        try {
            this.mVrService.setPersistentVrModeEnabled(Boolean.parseBoolean(nextArg()));
        } catch (RemoteException re) {
            PrintStream printStream = System.err;
            printStream.println("Error: Can't set persistent mode " + re);
        }
    }
}

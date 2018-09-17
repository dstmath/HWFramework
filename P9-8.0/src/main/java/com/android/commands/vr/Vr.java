package com.android.commands.vr;

import android.app.Vr2dDisplayProperties;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.vr.IVrManager;
import android.service.vr.IVrManager.Stub;
import com.android.internal.os.BaseCommand;
import java.io.PrintStream;

public final class Vr extends BaseCommand {
    private static final String COMMAND_SET_PERSISTENT_VR_MODE_ENABLED = "set-persistent-vr-mode-enabled";
    private static final String COMMAND_SET_VR2D_DISPLAY_PROPERTIES = "set-display-props";
    private IVrManager mVrService;

    public static void main(String[] args) {
        new Vr().run(args);
    }

    public void onShowUsage(PrintStream out) {
        out.println("usage: vr [subcommand]\nusage: vr set-persistent-vr-mode-enabled [true|false]\nusage: vr set-display-props [width] [height] [dpi]\n");
    }

    public void onRun() throws Exception {
        this.mVrService = Stub.asInterface(ServiceManager.getService("vrmanager"));
        if (this.mVrService == null) {
            showError("Error: Could not access the Vr Manager. Is the system running?");
            return;
        }
        String command = nextArgRequired();
        if (command.equals(COMMAND_SET_VR2D_DISPLAY_PROPERTIES)) {
            runSetVr2dDisplayProperties();
        } else if (command.equals(COMMAND_SET_PERSISTENT_VR_MODE_ENABLED)) {
            runSetPersistentVrModeEnabled();
        } else {
            throw new IllegalArgumentException("unknown command '" + command + "'");
        }
    }

    private void runSetVr2dDisplayProperties() throws RemoteException {
        try {
            this.mVrService.setVr2dDisplayProperties(new Vr2dDisplayProperties(Integer.parseInt(nextArgRequired()), Integer.parseInt(nextArgRequired()), Integer.parseInt(nextArgRequired())));
        } catch (RemoteException re) {
            System.err.println("Error: Can't set persistent mode " + re);
        }
    }

    private void runSetPersistentVrModeEnabled() throws RemoteException {
        try {
            this.mVrService.setPersistentVrModeEnabled(Boolean.parseBoolean(nextArg()));
        } catch (RemoteException re) {
            System.err.println("Error: Can't set persistent mode " + re);
        }
    }
}

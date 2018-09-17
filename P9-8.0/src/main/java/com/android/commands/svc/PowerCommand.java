package com.android.commands.svc;

import android.os.IPowerManager;
import android.os.IPowerManager.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import com.android.commands.svc.Svc.Command;

public class PowerCommand extends Command {
    public PowerCommand() {
        super("power");
    }

    public String shortHelp() {
        return "Control the power manager";
    }

    public String longHelp() {
        return shortHelp() + "\n" + "\n" + "usage: svc power stayon [true|false|usb|ac|wireless]\n" + "         Set the 'keep awake while plugged in' setting.\n" + "       svc power reboot [reason]\n" + "         Perform a runtime shutdown and reboot device with specified reason.\n" + "       svc power shutdown\n" + "         Perform a runtime shutdown and power off the device.\n";
    }

    public void run(String[] args) {
        if (args.length >= 2) {
            IPowerManager pm = Stub.asInterface(ServiceManager.getService("power"));
            if ("stayon".equals(args[1]) && args.length == 3) {
                int val;
                if ("true".equals(args[2])) {
                    val = 7;
                } else if ("false".equals(args[2])) {
                    val = 0;
                } else if ("usb".equals(args[2])) {
                    val = 2;
                } else if ("ac".equals(args[2])) {
                    val = 1;
                } else if ("wireless".equals(args[2])) {
                    val = 4;
                }
                if (val != 0) {
                    try {
                        pm.wakeUp(SystemClock.uptimeMillis(), "PowerCommand", null);
                    } catch (RemoteException e) {
                        System.err.println("Faild to set setting: " + e);
                    }
                }
                pm.setStayOnSetting(val);
                return;
            } else if ("reboot".equals(args[1])) {
                String mode = null;
                if (args.length == 3) {
                    mode = args[2];
                }
                try {
                    pm.reboot(false, mode, true);
                } catch (RemoteException e2) {
                    maybeLogRemoteException("Failed to reboot.");
                }
                return;
            } else if ("shutdown".equals(args[1])) {
                try {
                    pm.shutdown(false, null, true);
                } catch (RemoteException e3) {
                    maybeLogRemoteException("Failed to shutdown.");
                }
                return;
            }
        }
        System.err.println(longHelp());
    }

    private void maybeLogRemoteException(String msg) {
        if (SystemProperties.get("sys.powerctl").isEmpty()) {
            System.err.println(msg);
        }
    }
}

package com.android.commands.svc;

import android.hardware.usb.IUsbManager;
import android.hardware.usb.IUsbManager.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import com.android.commands.svc.Svc.Command;

public class UsbCommand extends Command {
    public UsbCommand() {
        super("usb");
    }

    public String shortHelp() {
        return "Control Usb state";
    }

    public String longHelp() {
        return shortHelp() + "\n" + "\n" + "usage: svc usb setFunction [function] [usbDataUnlocked=false]\n" + "         Set the current usb function and optionally the data lock state.\n\n" + "       svc usb getFunction\n" + "          Gets the list of currently enabled functions\n";
    }

    public void run(String[] args) {
        if (args.length >= 2) {
            if ("setFunction".equals(args[1])) {
                IUsbManager usbMgr = Stub.asInterface(ServiceManager.getService("usb"));
                boolean unlockData = false;
                if (args.length >= 4) {
                    unlockData = Boolean.valueOf(args[3]).booleanValue();
                }
                try {
                    usbMgr.setCurrentFunction(args.length >= 3 ? args[2] : null, unlockData);
                } catch (RemoteException e) {
                    System.err.println("Error communicating with UsbManager: " + e);
                }
                return;
            } else if ("getFunction".equals(args[1])) {
                System.err.println(SystemProperties.get("sys.usb.config"));
                return;
            }
        }
        System.err.println(longHelp());
    }
}

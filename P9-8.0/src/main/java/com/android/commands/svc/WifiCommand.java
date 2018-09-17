package com.android.commands.svc;

import android.net.wifi.IWifiManager.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.commands.svc.Svc.Command;

public class WifiCommand extends Command {
    public WifiCommand() {
        super("wifi");
    }

    public String shortHelp() {
        return "Control the Wi-Fi manager";
    }

    public String longHelp() {
        return shortHelp() + "\n" + "\n" + "usage: svc wifi [enable|disable]\n" + "         Turn Wi-Fi on or off.\n\n";
    }

    public void run(String[] args) {
        boolean validCommand = false;
        if (args.length >= 2) {
            boolean flag = false;
            if ("enable".equals(args[1])) {
                flag = true;
                validCommand = true;
            } else if ("disable".equals(args[1])) {
                flag = false;
                validCommand = true;
            }
            if (validCommand) {
                try {
                    Stub.asInterface(ServiceManager.getService("wifi")).setWifiEnabled("com.android.shell", flag);
                } catch (RemoteException e) {
                    System.err.println("Wi-Fi operation failed: " + e);
                }
                return;
            }
        }
        System.err.println(longHelp());
    }
}

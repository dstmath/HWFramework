package com.android.commands.svc;

import android.net.wifi.IWifiManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.commands.svc.Svc;
import java.io.PrintStream;

public class WifiCommand extends Svc.Command {
    public WifiCommand() {
        super("wifi");
    }

    @Override // com.android.commands.svc.Svc.Command
    public String shortHelp() {
        return "Control the Wi-Fi manager";
    }

    @Override // com.android.commands.svc.Svc.Command
    public String longHelp() {
        return shortHelp() + "\n\nusage: svc wifi [enable|disable]\n         Turn Wi-Fi on or off.\n\n";
    }

    @Override // com.android.commands.svc.Svc.Command
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
                IWifiManager wifiMgr = IWifiManager.Stub.asInterface(ServiceManager.getService("wifi"));
                if (wifiMgr == null) {
                    System.err.println("Wi-Fi service is not ready");
                    return;
                }
                try {
                    wifiMgr.setWifiEnabled("com.android.shell", flag);
                    return;
                } catch (RemoteException e) {
                    PrintStream printStream = System.err;
                    printStream.println("Wi-Fi operation failed: " + e);
                    return;
                }
            }
        }
        System.err.println(longHelp());
    }
}

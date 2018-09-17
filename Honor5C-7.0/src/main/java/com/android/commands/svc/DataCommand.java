package com.android.commands.svc;

import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.commands.svc.Svc.Command;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephony.Stub;

public class DataCommand extends Command {
    public DataCommand() {
        super("data");
    }

    public String shortHelp() {
        return "Control mobile data connectivity";
    }

    public String longHelp() {
        return shortHelp() + "\n" + "\n" + "usage: svc data [enable|disable]\n" + "         Turn mobile data on or off.\n\n";
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
                ITelephony phoneMgr = Stub.asInterface(ServiceManager.getService("phone"));
                if (flag) {
                    try {
                        phoneMgr.enableDataConnectivity();
                    } catch (RemoteException e) {
                        System.err.println("Mobile data operation failed: " + e);
                    }
                } else {
                    phoneMgr.disableDataConnectivity();
                }
                return;
            }
        }
        System.err.println(longHelp());
    }
}

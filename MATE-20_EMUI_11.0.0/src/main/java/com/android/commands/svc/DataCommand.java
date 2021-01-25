package com.android.commands.svc;

import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.commands.svc.Svc;
import com.android.internal.telephony.ITelephony;
import java.io.PrintStream;

public class DataCommand extends Svc.Command {
    public DataCommand() {
        super("data");
    }

    @Override // com.android.commands.svc.Svc.Command
    public String shortHelp() {
        return "Control mobile data connectivity";
    }

    @Override // com.android.commands.svc.Svc.Command
    public String longHelp() {
        return shortHelp() + "\n\nusage: svc data [enable|disable]\n         Turn mobile data on or off.\n\n";
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
                ITelephony phoneMgr = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
                if (flag) {
                    try {
                        phoneMgr.enableDataConnectivity();
                        return;
                    } catch (RemoteException e) {
                        PrintStream printStream = System.err;
                        printStream.println("Mobile data operation failed: " + e);
                        return;
                    }
                } else {
                    phoneMgr.disableDataConnectivity();
                    return;
                }
            }
        }
        System.err.println(longHelp());
    }
}

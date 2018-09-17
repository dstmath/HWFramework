package com.android.commands.svc;

import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.nfc.INfcAdapter;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.commands.svc.Svc.Command;

public class NfcCommand extends Command {
    public NfcCommand() {
        super("nfc");
    }

    public String shortHelp() {
        return "Control NFC functions";
    }

    public String longHelp() {
        return shortHelp() + "\n" + "\n" + "usage: svc nfc [enable|disable]\n" + "         Turn NFC on or off.\n\n";
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
                IPackageManager pm = Stub.asInterface(ServiceManager.getService("package"));
                try {
                    if (pm.hasSystemFeature("android.hardware.nfc", 0) || pm.hasSystemFeature("android.hardware.nfc.hce", 0)) {
                        INfcAdapter nfc = INfcAdapter.Stub.asInterface(ServiceManager.getService("nfc"));
                        if (flag) {
                            try {
                                nfc.enable();
                            } catch (RemoteException e) {
                                System.err.println("NFC operation failed: " + e);
                            }
                            return;
                        }
                        nfc.disable(true);
                        return;
                    }
                    System.err.println("NFC feature not supported.");
                    return;
                } catch (RemoteException e2) {
                    System.err.println("RemoteException while calling PackageManager, is the system running?");
                }
            }
        }
        System.err.println(longHelp());
    }
}

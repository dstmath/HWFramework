package com.android.commands.svc;

import android.hardware.usb.IUsbManager;
import android.hardware.usb.UsbManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.commands.svc.Svc;
import java.io.PrintStream;

public class UsbCommand extends Svc.Command {
    public UsbCommand() {
        super("usb");
    }

    @Override // com.android.commands.svc.Svc.Command
    public String shortHelp() {
        return "Control Usb state";
    }

    @Override // com.android.commands.svc.Svc.Command
    public String longHelp() {
        return shortHelp() + "\n\nusage: svc usb setFunctions [function]\n         Set the current usb function. If function is blank, sets to charging.\n       svc usb setScreenUnlockedFunctions [function]\n         Sets the functions which, if the device was charging, become current onscreen unlock. If function is blank, turn off this feature.\n       svc usb getFunctions\n          Gets the list of currently enabled functions\n\npossible values of [function] are any of 'mtp', 'ptp', 'rndis', 'midi'\n";
    }

    @Override // com.android.commands.svc.Svc.Command
    public void run(String[] args) {
        if (args.length >= 2) {
            IUsbManager usbMgr = IUsbManager.Stub.asInterface(ServiceManager.getService("usb"));
            String str = "";
            if ("setFunctions".equals(args[1])) {
                try {
                    if (args.length >= 3) {
                        str = args[2];
                    }
                    usbMgr.setCurrentFunctions(UsbManager.usbFunctionsFromString(str));
                    return;
                } catch (RemoteException e) {
                    PrintStream printStream = System.err;
                    printStream.println("Error communicating with UsbManager: " + e);
                    return;
                }
            } else if ("getFunctions".equals(args[1])) {
                try {
                    System.err.println(UsbManager.usbFunctionsToString(usbMgr.getCurrentFunctions()));
                    return;
                } catch (RemoteException e2) {
                    PrintStream printStream2 = System.err;
                    printStream2.println("Error communicating with UsbManager: " + e2);
                    return;
                }
            } else if ("setScreenUnlockedFunctions".equals(args[1])) {
                try {
                    if (args.length >= 3) {
                        str = args[2];
                    }
                    usbMgr.setScreenUnlockedFunctions(UsbManager.usbFunctionsFromString(str));
                    return;
                } catch (RemoteException e3) {
                    PrintStream printStream3 = System.err;
                    printStream3.println("Error communicating with UsbManager: " + e3);
                    return;
                }
            }
        }
        System.err.println(longHelp());
    }
}

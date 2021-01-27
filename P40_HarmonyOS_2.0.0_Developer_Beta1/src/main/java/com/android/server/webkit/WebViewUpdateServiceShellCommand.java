package com.android.server.webkit;

import android.os.RemoteException;
import android.os.ShellCommand;
import android.webkit.IWebViewUpdateService;
import java.io.PrintWriter;

class WebViewUpdateServiceShellCommand extends ShellCommand {
    final IWebViewUpdateService mInterface;

    WebViewUpdateServiceShellCommand(IWebViewUpdateService service) {
        this.mInterface = service;
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0045 A[Catch:{ RemoteException -> 0x005d }] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0058 A[Catch:{ RemoteException -> 0x005d }] */
    public int onCommand(String cmd) {
        boolean z;
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter pw = getOutPrintWriter();
        try {
            int hashCode = cmd.hashCode();
            if (hashCode != -1857752288) {
                if (hashCode != -1381305903) {
                    if (hashCode == 436183515 && cmd.equals("disable-multiprocess")) {
                        z = true;
                        if (z) {
                            return setWebViewImplementation();
                        }
                        if (z) {
                            return enableMultiProcess(true);
                        }
                        if (!z) {
                            return handleDefaultCommands(cmd);
                        }
                        return enableMultiProcess(false);
                    }
                } else if (cmd.equals("set-webview-implementation")) {
                    z = false;
                    if (z) {
                    }
                }
            } else if (cmd.equals("enable-multiprocess")) {
                z = true;
                if (z) {
                }
            }
            z = true;
            if (z) {
            }
        } catch (RemoteException e) {
            pw.println("Remote exception: " + e);
            return -1;
        }
    }

    private int setWebViewImplementation() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        String shellChosenPackage = getNextArg();
        if (shellChosenPackage == null) {
            pw.println("Failed to switch, no PACKAGE provided.");
            pw.println("");
            helpSetWebViewImplementation();
            return 1;
        }
        String newPackage = this.mInterface.changeProviderAndSetting(shellChosenPackage);
        if (shellChosenPackage.equals(newPackage)) {
            pw.println("Success");
            return 0;
        }
        pw.println(String.format("Failed to switch to %s, the WebView implementation is now provided by %s.", shellChosenPackage, newPackage));
        return 1;
    }

    private int enableMultiProcess(boolean enable) throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        this.mInterface.enableMultiProcess(enable);
        pw.println("Success");
        return 0;
    }

    public void helpSetWebViewImplementation() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("  set-webview-implementation PACKAGE");
        pw.println("    Set the WebView implementation to the specified package.");
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("WebView updater commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("");
        helpSetWebViewImplementation();
        pw.println("  enable-multiprocess");
        pw.println("    Enable multi-process mode for WebView");
        pw.println("  disable-multiprocess");
        pw.println("    Disable multi-process mode for WebView");
        pw.println();
    }
}

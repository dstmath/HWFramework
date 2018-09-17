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

    public int onCommand(String cmd) {
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter pw = getOutPrintWriter();
        try {
            if (cmd.equals("enable-redundant-packages")) {
                return enableFallbackLogic(false);
            }
            if (cmd.equals("disable-redundant-packages")) {
                return enableFallbackLogic(true);
            }
            if (cmd.equals("set-webview-implementation")) {
                return setWebViewImplementation();
            }
            if (cmd.equals("enable-multiprocess")) {
                return enableMultiProcess(true);
            }
            if (cmd.equals("disable-multiprocess")) {
                return enableMultiProcess(false);
            }
            return handleDefaultCommands(cmd);
        } catch (RemoteException e) {
            pw.println("Remote exception: " + e);
            return -1;
        }
    }

    private int enableFallbackLogic(boolean enable) throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        this.mInterface.enableFallbackLogic(enable);
        pw.println("Success");
        return 0;
    }

    private int setWebViewImplementation() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        String shellChosenPackage = getNextArg();
        if (shellChosenPackage.equals(this.mInterface.changeProviderAndSetting(shellChosenPackage))) {
            pw.println("Success");
            return 0;
        }
        pw.println(String.format("Failed to switch to %s, the WebView implementation is now provided by %s.", new Object[]{shellChosenPackage, this.mInterface.changeProviderAndSetting(shellChosenPackage)}));
        return 1;
    }

    private int enableMultiProcess(boolean enable) throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        this.mInterface.enableMultiProcess(enable);
        pw.println("Success");
        return 0;
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("WebView updater commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("");
        pw.println("  enable-redundant-packages");
        pw.println("    Allow a fallback package to be installed and enabled even when a");
        pw.println("    more-preferred package is available. This command is useful when testing");
        pw.println("    fallback packages.");
        pw.println("  disable-redundant-packages");
        pw.println("    Disallow installing and enabling fallback packages when a more-preferred");
        pw.println("    package is available.");
        pw.println("  set-webview-implementation PACKAGE");
        pw.println("    Set the WebView implementation to the specified package.");
        pw.println("  enable-multiprocess");
        pw.println("    Enable multi-process mode for WebView");
        pw.println("  disable-multiprocess");
        pw.println("    Disable multi-process mode for WebView");
        pw.println();
    }
}

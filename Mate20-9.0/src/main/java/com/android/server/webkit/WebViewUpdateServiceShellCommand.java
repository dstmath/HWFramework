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

    /* JADX WARNING: Can't fix incorrect switch cases order */
    public int onCommand(String cmd) {
        char c;
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter pw = getOutPrintWriter();
        try {
            switch (cmd.hashCode()) {
                case -1857752288:
                    if (cmd.equals("enable-multiprocess")) {
                        c = 3;
                        break;
                    }
                case -1381305903:
                    if (cmd.equals("set-webview-implementation")) {
                        c = 2;
                        break;
                    }
                case 436183515:
                    if (cmd.equals("disable-multiprocess")) {
                        c = 4;
                        break;
                    }
                case 823481554:
                    if (cmd.equals("disable-redundant-packages")) {
                        c = 1;
                        break;
                    }
                case 2070404695:
                    if (cmd.equals("enable-redundant-packages")) {
                        c = 0;
                        break;
                    }
            }
            c = 65535;
            switch (c) {
                case 0:
                    return enableFallbackLogic(false);
                case 1:
                    return enableFallbackLogic(true);
                case 2:
                    return setWebViewImplementation();
                case 3:
                    return enableMultiProcess(true);
                case 4:
                    return enableMultiProcess(false);
                default:
                    return handleDefaultCommands(cmd);
            }
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
        String newPackage = this.mInterface.changeProviderAndSetting(shellChosenPackage);
        if (shellChosenPackage.equals(newPackage)) {
            pw.println("Success");
            return 0;
        }
        pw.println(String.format("Failed to switch to %s, the WebView implementation is now provided by %s.", new Object[]{shellChosenPackage, newPackage}));
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
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
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

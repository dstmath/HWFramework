package com.android.server.net.watchlist;

import android.content.Context;
import android.os.Binder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ShellCommand;
import android.provider.Settings;
import java.io.FileInputStream;
import java.io.PrintWriter;

class NetworkWatchlistShellCommand extends ShellCommand {
    final Context mContext;
    final NetworkWatchlistService mService;

    NetworkWatchlistShellCommand(NetworkWatchlistService service, Context context) {
        this.mContext = context;
        this.mService = service;
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0034 A[Catch:{ Exception -> 0x0045 }] */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0040 A[Catch:{ Exception -> 0x0045 }] */
    public int onCommand(String cmd) {
        boolean z;
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter pw = getOutPrintWriter();
        try {
            int hashCode = cmd.hashCode();
            if (hashCode != 1757613042) {
                if (hashCode == 1854202282 && cmd.equals("force-generate-report")) {
                    z = true;
                    if (!z) {
                        return runSetTestConfig();
                    }
                    if (!z) {
                        return handleDefaultCommands(cmd);
                    }
                    return runForceGenerateReport();
                }
            } else if (cmd.equals("set-test-config")) {
                z = false;
                if (!z) {
                }
            }
            z = true;
            if (!z) {
            }
        } catch (Exception e) {
            pw.println("Exception: " + e);
            return -1;
        }
    }

    private int runSetTestConfig() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        try {
            ParcelFileDescriptor pfd = openFileForSystem(getNextArgRequired(), "r");
            if (pfd != null) {
                WatchlistConfig.getInstance().setTestMode(new FileInputStream(pfd.getFileDescriptor()));
            }
            pw.println("Success!");
            return 0;
        } catch (Exception ex) {
            pw.println("Error: " + ex.toString());
            return -1;
        }
    }

    private int runForceGenerateReport() throws RemoteException {
        PrintWriter pw = getOutPrintWriter();
        long ident = Binder.clearCallingIdentity();
        try {
            if (WatchlistConfig.getInstance().isConfigSecure()) {
                pw.println("Error: Cannot force generate report under production config");
                return -1;
            }
            Settings.Global.putLong(this.mContext.getContentResolver(), "network_watchlist_last_report_time", 0);
            this.mService.forceReportWatchlistForTest(System.currentTimeMillis());
            pw.println("Success!");
            Binder.restoreCallingIdentity(ident);
            return 0;
        } catch (Exception ex) {
            pw.println("Error: " + ex);
            return -1;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("Network watchlist manager commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("  set-test-config your_watchlist_config.xml");
        pw.println("    Set network watchlist test config file.");
        pw.println("  force-generate-report");
        pw.println("    Force generate watchlist test report.");
    }
}

package com.android.server.wifi;

import android.app.AppGlobals;
import android.content.pm.IPackageManager;
import android.os.Binder;
import android.os.ShellCommand;
import com.android.server.wifi.WifiNative;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class WifiShellCommand extends ShellCommand {
    private final ClientModeImpl mClientModeImpl;
    private final IPackageManager mPM = AppGlobals.getPackageManager();
    private final WifiConfigManager mWifiConfigManager;
    private final WifiLockManager mWifiLockManager;
    private final WifiNetworkSuggestionsManager mWifiNetworkSuggestionsManager;

    WifiShellCommand(WifiInjector wifiInjector) {
        this.mClientModeImpl = wifiInjector.getClientModeImpl();
        this.mWifiLockManager = wifiInjector.getWifiLockManager();
        this.mWifiNetworkSuggestionsManager = wifiInjector.getWifiNetworkSuggestionsManager();
        this.mWifiConfigManager = wifiInjector.getWifiConfigManager();
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public int onCommand(String cmd) {
        char c;
        boolean enabled;
        boolean enabled2;
        boolean enabled3;
        boolean approved;
        checkRootPermission();
        PrintWriter pw = getOutPrintWriter();
        String str = cmd != null ? cmd : "";
        try {
            switch (str.hashCode()) {
                case -1972405815:
                    if (str.equals("network-suggestions-has-user-approved")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -1861126232:
                    if (str.equals("set-ipreach-disconnect")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -1267819290:
                    if (str.equals("get-poll-rssi-interval-msecs")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -1006001187:
                    if (str.equals("force-hi-perf-mode")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -505485935:
                    if (str.equals("network-suggestions-set-user-approved")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -29690534:
                    if (str.equals("set-poll-rssi-interval-msecs")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 355024770:
                    if (str.equals("send-link-probe")) {
                        c = '\n';
                        break;
                    }
                    c = 65535;
                    break;
                case 571096281:
                    if (str.equals("network-requests-remove-user-approved-access-points")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 918812649:
                    if (str.equals("clear-deleted-ephemeral-networks")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case 1120712756:
                    if (str.equals("get-ipreach-disconnect")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1201296781:
                    if (str.equals("force-low-latency-mode")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            String str2 = "no";
            switch (c) {
                case 0:
                    String nextArg = getNextArgRequired();
                    if ("enabled".equals(nextArg)) {
                        enabled = true;
                    } else if ("disabled".equals(nextArg)) {
                        enabled = false;
                    } else {
                        pw.println("Invalid argument to 'set-ipreach-disconnect' - must be 'enabled' or 'disabled'");
                        return -1;
                    }
                    this.mClientModeImpl.setIpReachabilityDisconnectEnabled(enabled);
                    return 0;
                case 1:
                    pw.println("IPREACH_DISCONNECT state is " + this.mClientModeImpl.getIpReachabilityDisconnectEnabled());
                    return 0;
                case 2:
                    try {
                        int newPollIntervalMsecs = Integer.parseInt(getNextArgRequired());
                        if (newPollIntervalMsecs < 1) {
                            pw.println("Invalid argument to 'set-poll-rssi-interval-msecs' - must be a positive integer");
                            return -1;
                        }
                        this.mClientModeImpl.setPollRssiIntervalMsecs(newPollIntervalMsecs);
                        return 0;
                    } catch (NumberFormatException e) {
                        pw.println("Invalid argument to 'set-poll-rssi-interval-msecs' - must be a positive integer");
                        return -1;
                    }
                case 3:
                    pw.println("ClientModeImpl.mPollRssiIntervalMsecs = " + this.mClientModeImpl.getPollRssiIntervalMsecs());
                    return 0;
                case 4:
                    String nextArg2 = getNextArgRequired();
                    if ("enabled".equals(nextArg2)) {
                        enabled2 = true;
                    } else if ("disabled".equals(nextArg2)) {
                        enabled2 = false;
                    } else {
                        pw.println("Invalid argument to 'force-hi-perf-mode' - must be 'enabled' or 'disabled'");
                        return -1;
                    }
                    if (!this.mWifiLockManager.forceHiPerfMode(enabled2)) {
                        pw.println("Command execution failed");
                    }
                    return 0;
                case 5:
                    String nextArg3 = getNextArgRequired();
                    if ("enabled".equals(nextArg3)) {
                        enabled3 = true;
                    } else if ("disabled".equals(nextArg3)) {
                        enabled3 = false;
                    } else {
                        pw.println("Invalid argument to 'force-low-latency-mode' - must be 'enabled' or 'disabled'");
                        return -1;
                    }
                    if (!this.mWifiLockManager.forceLowLatencyMode(enabled3)) {
                        pw.println("Command execution failed");
                    }
                    return 0;
                case 6:
                    String packageName = getNextArgRequired();
                    String nextArg4 = getNextArgRequired();
                    if ("yes".equals(nextArg4)) {
                        approved = true;
                    } else if (str2.equals(nextArg4)) {
                        approved = false;
                    } else {
                        pw.println("Invalid argument to 'network-suggestions-set-user-approved' - must be 'yes' or 'no'");
                        return -1;
                    }
                    this.mWifiNetworkSuggestionsManager.setHasUserApprovedForApp(approved, packageName);
                    return 0;
                case 7:
                    if (this.mWifiNetworkSuggestionsManager.hasUserApprovedForApp(getNextArgRequired())) {
                        str2 = "yes";
                    }
                    pw.println(str2);
                    return 0;
                case '\b':
                    this.mClientModeImpl.removeNetworkRequestUserApprovedAccessPointsForApp(getNextArgRequired());
                    return 0;
                case '\t':
                    this.mWifiConfigManager.clearDeletedEphemeralNetworks();
                    return 0;
                case '\n':
                    return sendLinkProbe(pw);
                default:
                    return handleDefaultCommands(cmd);
            }
        } catch (Exception e2) {
            pw.println("Exception while executing WifiShellCommand: ");
            e2.printStackTrace(pw);
            return -1;
        }
    }

    private int sendLinkProbe(PrintWriter pw) throws InterruptedException {
        final ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(1);
        this.mClientModeImpl.probeLink(new WifiNative.SendMgmtFrameCallback() {
            /* class com.android.server.wifi.WifiShellCommand.AnonymousClass1 */

            @Override // com.android.server.wifi.WifiNative.SendMgmtFrameCallback
            public void onAck(int elapsedTimeMs) {
                ArrayBlockingQueue arrayBlockingQueue = queue;
                arrayBlockingQueue.offer("Link probe succeeded after " + elapsedTimeMs + " ms");
            }

            @Override // com.android.server.wifi.WifiNative.SendMgmtFrameCallback
            public void onFailure(int reason) {
                ArrayBlockingQueue arrayBlockingQueue = queue;
                arrayBlockingQueue.offer("Link probe failed with reason " + reason);
            }
        }, -1);
        String msg = queue.poll(2000, TimeUnit.MILLISECONDS);
        if (msg == null) {
            pw.println("Link probe timed out");
            return 0;
        }
        pw.println(msg);
        return 0;
    }

    private void checkRootPermission() {
        int uid = Binder.getCallingUid();
        if (uid != 0) {
            throw new SecurityException("Uid " + uid + " does not have access to wifi commands");
        }
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("Wi-Fi (wifi) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("  set-ipreach-disconnect enabled|disabled");
        pw.println("    Sets whether CMD_IP_REACHABILITY_LOST events should trigger disconnects.");
        pw.println("  get-ipreach-disconnect");
        pw.println("    Gets setting of CMD_IP_REACHABILITY_LOST events triggering disconnects.");
        pw.println("  set-poll-rssi-interval-msecs <int>");
        pw.println("    Sets the interval between RSSI polls to <int> milliseconds.");
        pw.println("  get-poll-rssi-interval-msecs");
        pw.println("    Gets current interval between RSSI polls, in milliseconds.");
        pw.println("  force-hi-perf-mode enabled|disabled");
        pw.println("    Sets whether hi-perf mode is forced or left for normal operation.");
        pw.println("  force-low-latency-mode enabled|disabled");
        pw.println("    Sets whether low latency mode is forced or left for normal operation.");
        pw.println("  network-suggestions-set-user-approved <package name> yes|no");
        pw.println("    Sets whether network suggestions from the app is approved or not.");
        pw.println("  network-suggestions-has-user-approved <package name>");
        pw.println("    Queries whether network suggestions from the app is approved or not.");
        pw.println("  network-requests-remove-user-approved-access-points <package name>");
        pw.println("    Removes all user approved network requests for the app.");
        pw.println("  clear-deleted-ephemeral-networks");
        pw.println("    Clears the deleted ephemeral networks list.");
        pw.println("  send-link-probe");
        pw.println("    Manually triggers a link probe.");
        pw.println();
    }
}

package com.android.server.wifi;

import android.app.AppGlobals;
import android.content.pm.IPackageManager;
import android.os.Binder;
import android.os.ShellCommand;
import java.io.PrintWriter;

public class WifiShellCommand extends ShellCommand {
    private final IPackageManager mPM = AppGlobals.getPackageManager();
    private final WifiStateMachine mStateMachine;

    WifiShellCommand(WifiStateMachine stateMachine) {
        this.mStateMachine = stateMachine;
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0055 A[Catch:{ Exception -> 0x00d6 }] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x005b A[Catch:{ Exception -> 0x00d6 }] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0076 A[SYNTHETIC, Splitter:B:31:0x0076] */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0095 A[Catch:{ Exception -> 0x00d6 }] */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00b0 A[Catch:{ Exception -> 0x00d6 }] */
    public int onCommand(String cmd) {
        char c;
        boolean enabled;
        checkRootPermission();
        PrintWriter pw = getOutPrintWriter();
        String str = cmd != null ? cmd : "";
        try {
            int hashCode = str.hashCode();
            if (hashCode != -1861126232) {
                if (hashCode != -1267819290) {
                    if (hashCode != -29690534) {
                        if (hashCode == 1120712756) {
                            if (str.equals("get-ipreach-disconnect")) {
                                c = 1;
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
                                        this.mStateMachine.setIpReachabilityDisconnectEnabled(enabled);
                                        return 0;
                                    case 1:
                                        pw.println("IPREACH_DISCONNECT state is " + this.mStateMachine.getIpReachabilityDisconnectEnabled());
                                        return 0;
                                    case 2:
                                        try {
                                            int newPollIntervalMsecs = Integer.parseInt(getNextArgRequired());
                                            if (newPollIntervalMsecs < 1) {
                                                pw.println("Invalid argument to 'set-poll-rssi-interval-msecs' - must be a positive integer");
                                                return -1;
                                            }
                                            this.mStateMachine.setPollRssiIntervalMsecs(newPollIntervalMsecs);
                                            return 0;
                                        } catch (NumberFormatException e) {
                                            pw.println("Invalid argument to 'set-poll-rssi-interval-msecs' - must be a positive integer");
                                            return -1;
                                        }
                                    case 3:
                                        pw.println("WifiStateMachine.mPollRssiIntervalMsecs = " + this.mStateMachine.getPollRssiIntervalMsecs());
                                        return 0;
                                    default:
                                        return handleDefaultCommands(cmd);
                                }
                                pw.println("Exception: " + e);
                                return -1;
                            }
                        }
                    } else if (str.equals("set-poll-rssi-interval-msecs")) {
                        c = 2;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                        }
                        pw.println("Exception: " + e);
                        return -1;
                    }
                } else if (str.equals("get-poll-rssi-interval-msecs")) {
                    c = 3;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                    pw.println("Exception: " + e);
                    return -1;
                }
            } else if (str.equals("set-ipreach-disconnect")) {
                c = 0;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
                pw.println("Exception: " + e);
                return -1;
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
            }
        } catch (Exception e2) {
            pw.println("Exception: " + e2);
            return -1;
        }
        pw.println("Exception: " + e2);
        return -1;
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
        pw.println();
    }
}

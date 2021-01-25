package com.android.server.wifi.aware;

import android.os.Binder;
import android.os.ShellCommand;
import android.text.TextUtils;
import android.util.Log;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class WifiAwareShellCommand extends ShellCommand {
    private static final String TAG = "WifiAwareShellCommand";
    private Map<String, DelegatedShellCommand> mDelegatedCommands = new HashMap();

    public interface DelegatedShellCommand {
        int onCommand(ShellCommand shellCommand);

        void onHelp(String str, ShellCommand shellCommand);

        void onReset();
    }

    public void register(String command, DelegatedShellCommand shellCommand) {
        if (this.mDelegatedCommands.containsKey(command)) {
            Log.e(TAG, "register: overwriting existing command -- '" + command + "'");
        }
        this.mDelegatedCommands.put(command, shellCommand);
    }

    public int onCommand(String cmd) {
        checkRootPermission();
        try {
            if ("reset".equals(cmd)) {
                for (DelegatedShellCommand dsc : this.mDelegatedCommands.values()) {
                    dsc.onReset();
                }
                return 0;
            }
            DelegatedShellCommand delegatedCmd = null;
            if (!TextUtils.isEmpty(cmd)) {
                delegatedCmd = this.mDelegatedCommands.get(cmd);
            }
            if (delegatedCmd != null) {
                return delegatedCmd.onCommand(this);
            }
            return handleDefaultCommands(cmd);
        } catch (Exception e) {
            getErrPrintWriter().println("Exception in onCommand()");
            return -1;
        }
    }

    private void checkRootPermission() {
        int uid = Binder.getCallingUid();
        if (uid != 0) {
            throw new SecurityException("Uid " + uid + " does not have access to wifiaware commands");
        }
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("Wi-Fi Aware (wifiaware) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("  reset");
        pw.println("    Reset parameters to default values.");
        for (Map.Entry<String, DelegatedShellCommand> sce : this.mDelegatedCommands.entrySet()) {
            sce.getValue().onHelp(sce.getKey(), this);
        }
        pw.println();
    }
}

package com.android.server.updates;

import com.android.server.firewall.IntentFirewall;

public class IntentFirewallInstallReceiver extends ConfigUpdateInstallReceiver {
    public IntentFirewallInstallReceiver() {
        super(IntentFirewall.getRulesDir().getAbsolutePath(), "ifw.xml", "metadata/", "gservices.version");
    }
}

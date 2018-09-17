package com.android.server.updates;

public class CarrierProvisioningUrlsInstallReceiver extends ConfigUpdateInstallReceiver {
    public CarrierProvisioningUrlsInstallReceiver() {
        super("/data/misc/radio/", "provisioning_urls.xml", "metadata/", "version");
    }
}

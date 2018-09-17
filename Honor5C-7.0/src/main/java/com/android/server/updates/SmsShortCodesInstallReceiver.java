package com.android.server.updates;

public class SmsShortCodesInstallReceiver extends ConfigUpdateInstallReceiver {
    public SmsShortCodesInstallReceiver() {
        super("/data/misc/sms/", "codes", "metadata/", "version");
    }
}

package com.android.server.updates;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Telephony.Carriers;

public class ApnDbInstallReceiver extends ConfigUpdateInstallReceiver {
    private static final Uri UPDATE_APN_DB = Uri.withAppendedPath(Carriers.CONTENT_URI, "update_db");

    public ApnDbInstallReceiver() {
        super("/data/misc/", "apns-conf.xml", "metadata/", "version");
    }

    protected void postInstall(Context context, Intent intent) {
        context.getContentResolver().delete(UPDATE_APN_DB, null, null);
    }
}

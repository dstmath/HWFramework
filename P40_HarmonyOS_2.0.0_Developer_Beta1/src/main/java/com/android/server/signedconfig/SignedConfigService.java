package com.android.server.signedconfig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManagerInternal;
import android.net.Uri;
import android.os.Bundle;
import android.util.Slog;
import com.android.server.LocalServices;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SignedConfigService {
    private static final boolean DBG = false;
    private static final String KEY_GLOBAL_SETTINGS = "android.settings.global";
    private static final String KEY_GLOBAL_SETTINGS_SIGNATURE = "android.settings.global.signature";
    private static final String TAG = "SignedConfig";
    private final Context mContext;
    private final PackageManagerInternal mPacMan = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class));

    /* access modifiers changed from: private */
    public static class UpdateReceiver extends BroadcastReceiver {
        private UpdateReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            new SignedConfigService(context).handlePackageBroadcast(intent);
        }
    }

    public SignedConfigService(Context context) {
        this.mContext = context;
    }

    /* access modifiers changed from: package-private */
    public void handlePackageBroadcast(Intent intent) {
        Uri packageData = intent.getData();
        String packageName = packageData == null ? null : packageData.getSchemeSpecificPart();
        if (packageName != null) {
            int userId = this.mContext.getUser().getIdentifier();
            PackageInfo pi = this.mPacMan.getPackageInfo(packageName, 128, 1000, userId);
            if (pi == null) {
                Slog.w(TAG, "Got null PackageInfo for " + packageName + "; user " + userId);
                return;
            }
            Bundle metaData = pi.applicationInfo.metaData;
            if (metaData != null && metaData.containsKey(KEY_GLOBAL_SETTINGS) && metaData.containsKey(KEY_GLOBAL_SETTINGS_SIGNATURE)) {
                SignedConfigEvent event = new SignedConfigEvent();
                try {
                    event.type = 1;
                    event.fromPackage = packageName;
                    String config = metaData.getString(KEY_GLOBAL_SETTINGS);
                    try {
                        new GlobalSettingsConfigApplicator(this.mContext, packageName, event).applyConfig(new String(Base64.getDecoder().decode(config), StandardCharsets.UTF_8), metaData.getString(KEY_GLOBAL_SETTINGS_SIGNATURE));
                    } catch (IllegalArgumentException e) {
                        Slog.e(TAG, "Failed to base64 decode global settings config from " + packageName);
                        event.status = 2;
                        event.send();
                    }
                } finally {
                    event.send();
                }
            }
        }
    }

    public static void registerUpdateReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addDataScheme("package");
        context.registerReceiver(new UpdateReceiver(), filter);
    }
}

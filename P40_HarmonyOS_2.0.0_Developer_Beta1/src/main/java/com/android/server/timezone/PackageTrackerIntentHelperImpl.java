package com.android.server.timezone;

import android.app.timezone.RulesUpdaterContract;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.EventLogTags;

/* access modifiers changed from: package-private */
public final class PackageTrackerIntentHelperImpl implements PackageTrackerIntentHelper {
    private static final String TAG = "timezone.PackageTrackerIntentHelperImpl";
    private final Context mContext;
    private String mUpdaterAppPackageName;

    PackageTrackerIntentHelperImpl(Context context) {
        this.mContext = context;
    }

    @Override // com.android.server.timezone.PackageTrackerIntentHelper
    public void initialize(String updaterAppPackageName, String dataAppPackageName, PackageTracker packageTracker) {
        this.mUpdaterAppPackageName = updaterAppPackageName;
        IntentFilter packageIntentFilter = new IntentFilter();
        packageIntentFilter.addDataScheme("package");
        packageIntentFilter.addDataSchemeSpecificPart(updaterAppPackageName, 0);
        packageIntentFilter.addDataSchemeSpecificPart(dataAppPackageName, 0);
        packageIntentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        packageIntentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        this.mContext.registerReceiverAsUser(new Receiver(packageTracker), UserHandle.SYSTEM, packageIntentFilter, null, null);
    }

    @Override // com.android.server.timezone.PackageTrackerIntentHelper
    public void sendTriggerUpdateCheck(CheckToken checkToken) {
        RulesUpdaterContract.sendBroadcast(this.mContext, this.mUpdaterAppPackageName, checkToken.toByteArray());
        EventLogTags.writeTimezoneTriggerCheck(checkToken.toString());
    }

    @Override // com.android.server.timezone.PackageTrackerIntentHelper
    public synchronized void scheduleReliabilityTrigger(long minimumDelayMillis) {
        TimeZoneUpdateIdler.schedule(this.mContext, minimumDelayMillis);
    }

    @Override // com.android.server.timezone.PackageTrackerIntentHelper
    public synchronized void unscheduleReliabilityTrigger() {
        TimeZoneUpdateIdler.unschedule(this.mContext);
    }

    private static class Receiver extends BroadcastReceiver {
        private final PackageTracker mPackageTracker;

        private Receiver(PackageTracker packageTracker) {
            this.mPackageTracker = packageTracker;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Slog.d(PackageTrackerIntentHelperImpl.TAG, "Received intent: " + intent.toString());
            this.mPackageTracker.triggerUpdateIfNeeded(true);
        }
    }
}

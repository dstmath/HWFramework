package com.android.server.wm;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import com.android.internal.util.function.pooled.PooledLambda;
import java.util.ArrayList;

/* access modifiers changed from: package-private */
public class LaunchObserverRegistryImpl implements ActivityMetricsLaunchObserverRegistry, ActivityMetricsLaunchObserver {
    private final Handler mHandler;
    private final ArrayList<ActivityMetricsLaunchObserver> mList = new ArrayList<>();

    public LaunchObserverRegistryImpl(Looper looper) {
        this.mHandler = new Handler(looper);
    }

    @Override // com.android.server.wm.ActivityMetricsLaunchObserverRegistry
    public void registerLaunchObserver(ActivityMetricsLaunchObserver launchObserver) {
        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$LaunchObserverRegistryImpl$pWUDt4Ot3BWLJOTAhXMkkhHUhpc.INSTANCE, this, launchObserver));
    }

    @Override // com.android.server.wm.ActivityMetricsLaunchObserverRegistry
    public void unregisterLaunchObserver(ActivityMetricsLaunchObserver launchObserver) {
        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$LaunchObserverRegistryImpl$850Ez4IkbH192NuVFW_l12sZL_E.INSTANCE, this, launchObserver));
    }

    @Override // com.android.server.wm.ActivityMetricsLaunchObserver
    public void onIntentStarted(Intent intent) {
        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$LaunchObserverRegistryImpl$kM3MnXbSpyNkUV4eUyr4OwWCqqA.INSTANCE, this, intent));
    }

    @Override // com.android.server.wm.ActivityMetricsLaunchObserver
    public void onIntentFailed() {
        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$LaunchObserverRegistryImpl$KukKmVpn5W_1xSV6Dnp8wW2H2Ks.INSTANCE, this));
    }

    @Override // com.android.server.wm.ActivityMetricsLaunchObserver
    public void onActivityLaunched(byte[] activity, int temperature) {
        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$LaunchObserverRegistryImpl$UGY1OclnLIQLMEL9B55qjERFf4o.INSTANCE, this, activity, Integer.valueOf(temperature)));
    }

    @Override // com.android.server.wm.ActivityMetricsLaunchObserver
    public void onActivityLaunchCancelled(byte[] activity) {
        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$LaunchObserverRegistryImpl$lAGPwfsXJvBWsyG2rbEfo3sTv34.INSTANCE, this, activity));
    }

    @Override // com.android.server.wm.ActivityMetricsLaunchObserver
    public void onActivityLaunchFinished(byte[] activity) {
        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$LaunchObserverRegistryImpl$iVXZh14_jAo_Gegs5q3ygQDWow.INSTANCE, this, activity));
    }

    /* access modifiers changed from: private */
    public void handleRegisterLaunchObserver(ActivityMetricsLaunchObserver observer) {
        this.mList.add(observer);
    }

    /* access modifiers changed from: private */
    public void handleUnregisterLaunchObserver(ActivityMetricsLaunchObserver observer) {
        this.mList.remove(observer);
    }

    /* access modifiers changed from: private */
    public void handleOnIntentStarted(Intent intent) {
        for (int i = 0; i < this.mList.size(); i++) {
            this.mList.get(i).onIntentStarted(intent);
        }
    }

    /* access modifiers changed from: private */
    public void handleOnIntentFailed() {
        for (int i = 0; i < this.mList.size(); i++) {
            this.mList.get(i).onIntentFailed();
        }
    }

    /* access modifiers changed from: private */
    public void handleOnActivityLaunched(byte[] activity, int temperature) {
        for (int i = 0; i < this.mList.size(); i++) {
            this.mList.get(i).onActivityLaunched(activity, temperature);
        }
    }

    /* access modifiers changed from: private */
    public void handleOnActivityLaunchCancelled(byte[] activity) {
        for (int i = 0; i < this.mList.size(); i++) {
            this.mList.get(i).onActivityLaunchCancelled(activity);
        }
    }

    /* access modifiers changed from: private */
    public void handleOnActivityLaunchFinished(byte[] activity) {
        for (int i = 0; i < this.mList.size(); i++) {
            this.mList.get(i).onActivityLaunchFinished(activity);
        }
    }
}

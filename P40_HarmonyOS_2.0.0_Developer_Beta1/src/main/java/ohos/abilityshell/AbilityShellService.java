package ohos.abilityshell;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Optional;
import ohos.aafwk.ability.ProfileMissingConfigException;
import ohos.appexecfwk.utils.AppLog;
import ohos.event.notification.NotificationConvert;
import ohos.event.notification.NotificationRequest;
import ohos.hiviewdfx.HiLogLabel;

public class AbilityShellService extends Service {
    private static final int DEFAULT_BACKGROUND_MODE = 0;
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    private AbilityShellServiceDelegate delegate = new AbilityShellServiceDelegate(this);

    @Override // android.app.Service
    public void onCreate() {
        AppLog.d(SHELL_LABEL, "AbilityShellService::onCreate called", new Object[0]);
        this.delegate.onCreate();
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        AppLog.d(SHELL_LABEL, "AbilityShellService::onStartCommand called", new Object[0]);
        return this.delegate.onStartCommand(intent, i, i2);
    }

    @Override // android.app.Service
    public void onDestroy() {
        AppLog.d(SHELL_LABEL, "AbilityShellService::onDestroy called", new Object[0]);
        this.delegate.onDestroy();
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        AppLog.d(SHELL_LABEL, "AbilityShellService::onBind called", new Object[0]);
        return this.delegate.onBind(intent);
    }

    @Override // android.app.Service
    public void onRebind(Intent intent) {
        AppLog.d(SHELL_LABEL, "AbilityShellService::onRebind called", new Object[0]);
        this.delegate.onRebind(intent);
    }

    @Override // android.app.Service
    public void onTaskRemoved(Intent intent) {
        AppLog.d(SHELL_LABEL, "AbilityShellService::onTaskRemoved called", new Object[0]);
        this.delegate.onTaskRemoved(intent);
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        AppLog.d(SHELL_LABEL, "AbilityShellService::onUnbind called", new Object[0]);
        return this.delegate.onUnbind(intent);
    }

    @Override // android.app.Service, android.content.ComponentCallbacks2
    public void onTrimMemory(int i) {
        AppLog.d(SHELL_LABEL, "AbilityShellService::onTrimMemory called", new Object[0]);
        super.onTrimMemory(i);
        this.delegate.onTrimMemory(i);
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Service
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        AppLog.d(SHELL_LABEL, "AbilityShellService::dump called", new Object[0]);
        this.delegate.dump("", fileDescriptor, printWriter, strArr);
    }

    public void keepBackgroundRunning(int i, NotificationRequest notificationRequest) {
        AppLog.d(SHELL_LABEL, "AbilityShellService::keepBackgroundRunning called", new Object[0]);
        Optional<Notification> convertForegroundServiceNotification = NotificationConvert.convertForegroundServiceNotification(notificationRequest);
        if (!convertForegroundServiceNotification.isPresent()) {
            AppLog.e(SHELL_LABEL, "keepBackgroundRunning androidNotification invalid", new Object[0]);
            return;
        }
        int backgroundModes = this.delegate.getBackgroundModes(getPackageName(), getClass().getName());
        AppLog.d(SHELL_LABEL, "keepBackgroundRunning::getBackgroundModes %{public}d, id: %{public}d", Integer.valueOf(backgroundModes), Integer.valueOf(i));
        if (backgroundModes != 0) {
            try {
                startForeground(i, convertForegroundServiceNotification.get(), backgroundModes);
                AppLog.d(SHELL_LABEL, "AbilityShellService::keepBackgroundRunning called end", new Object[0]);
            } catch (IllegalArgumentException unused) {
                AppLog.e(SHELL_LABEL, "keepBackgroundRunning::config missing backgroundModes configuration", new Object[0]);
                throw new ProfileMissingConfigException("config missing backgroundModes configuration");
            }
        } else {
            AppLog.e(SHELL_LABEL, "keepBackgroundRunning::config missing backgroundModes configuration", new Object[0]);
            throw new ProfileMissingConfigException("config missing backgroundModes configuration");
        }
    }

    public void cancelBackgroundRunning() {
        AppLog.d(SHELL_LABEL, "AbilityShellService::cancelBackgroundRunning called", new Object[0]);
        stopForeground(true);
    }

    @Override // android.app.Service, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.delegate.updateConfiguration(configuration);
    }
}

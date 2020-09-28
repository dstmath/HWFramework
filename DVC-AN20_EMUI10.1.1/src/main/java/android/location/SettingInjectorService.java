package android.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public abstract class SettingInjectorService extends Service {
    public static final String ACTION_INJECTED_SETTING_CHANGED = "android.location.InjectedSettingChanged";
    public static final String ACTION_SERVICE_INTENT = "android.location.SettingInjectorService";
    public static final String ATTRIBUTES_NAME = "injected-location-setting";
    public static final String ENABLED_KEY = "enabled";
    public static final String MESSENGER_KEY = "messenger";
    public static final String META_DATA_NAME = "android.location.SettingInjectorService";
    public static final String SUMMARY_KEY = "summary";
    private static final String TAG = "SettingInjectorService";
    private final String mName;

    /* access modifiers changed from: protected */
    public abstract boolean onGetEnabled();

    /* access modifiers changed from: protected */
    public abstract String onGetSummary();

    public SettingInjectorService(String name) {
        this.mName = name;
    }

    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public final void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override // android.app.Service
    public final int onStartCommand(Intent intent, int flags, int startId) {
        onHandleIntent(intent);
        stopSelf(startId);
        return 2;
    }

    private void onHandleIntent(Intent intent) {
        String summary = null;
        boolean enabled = false;
        try {
            summary = onGetSummary();
            enabled = onGetEnabled();
        } finally {
            sendStatus(intent, summary, enabled);
        }
    }

    private void sendStatus(Intent intent, String summary, boolean enabled) {
        Messenger messenger = (Messenger) intent.getParcelableExtra("messenger");
        if (messenger != null) {
            Message message = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString("summary", summary);
            bundle.putBoolean("enabled", enabled);
            message.setData(bundle);
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, this.mName + ": received " + intent + ", summary=" + summary + ", enabled=" + enabled + ", sending message: " + message);
            }
            try {
                messenger.send(message);
            } catch (RemoteException e) {
                Log.e(TAG, this.mName + ": sending dynamic status failed", e);
            }
        }
    }

    public static final void refreshSettings(Context context) {
        context.sendBroadcast(new Intent(ACTION_INJECTED_SETTING_CHANGED));
    }
}

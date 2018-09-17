package android.service.notification;

import android.app.INotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.notification.IConditionProvider.Stub;
import android.util.Log;

public abstract class ConditionProviderService extends Service {
    public static final String EXTRA_RULE_ID = "android.service.notification.extra.RULE_ID";
    public static final String META_DATA_CONFIGURATION_ACTIVITY = "android.service.zen.automatic.configurationActivity";
    public static final String META_DATA_RULE_INSTANCE_LIMIT = "android.service.zen.automatic.ruleInstanceLimit";
    public static final String META_DATA_RULE_TYPE = "android.service.zen.automatic.ruleType";
    public static final String SERVICE_INTERFACE = "android.service.notification.ConditionProviderService";
    private final String TAG;
    private final H mHandler;
    private INotificationManager mNoMan;
    private Provider mProvider;

    private final class H extends Handler {
        private static final int ON_CONNECTED = 1;
        private static final int ON_SUBSCRIBE = 3;
        private static final int ON_UNSUBSCRIBE = 4;

        private H() {
        }

        public void handleMessage(Message msg) {
            try {
                String name;
                switch (msg.what) {
                    case ON_CONNECTED /*1*/:
                        name = "onConnected";
                        ConditionProviderService.this.onConnected();
                        return;
                    case ON_SUBSCRIBE /*3*/:
                        name = "onSubscribe";
                        ConditionProviderService.this.onSubscribe((Uri) msg.obj);
                        return;
                    case ON_UNSUBSCRIBE /*4*/:
                        name = "onUnsubscribe";
                        ConditionProviderService.this.onUnsubscribe((Uri) msg.obj);
                        return;
                    default:
                        return;
                }
            } catch (Throwable t) {
                Log.w(ConditionProviderService.this.TAG, "Error running " + null, t);
            }
            Log.w(ConditionProviderService.this.TAG, "Error running " + null, t);
        }
    }

    private final class Provider extends Stub {
        private Provider() {
        }

        public void onConnected() {
            ConditionProviderService.this.mHandler.obtainMessage(1).sendToTarget();
        }

        public void onSubscribe(Uri conditionId) {
            ConditionProviderService.this.mHandler.obtainMessage(3, conditionId).sendToTarget();
        }

        public void onUnsubscribe(Uri conditionId) {
            ConditionProviderService.this.mHandler.obtainMessage(4, conditionId).sendToTarget();
        }
    }

    public abstract void onConnected();

    public abstract void onSubscribe(Uri uri);

    public abstract void onUnsubscribe(Uri uri);

    public ConditionProviderService() {
        this.TAG = ConditionProviderService.class.getSimpleName() + "[" + getClass().getSimpleName() + "]";
        this.mHandler = new H();
    }

    public void onRequestConditions(int relevance) {
    }

    private final INotificationManager getNotificationInterface() {
        if (this.mNoMan == null) {
            this.mNoMan = INotificationManager.Stub.asInterface(ServiceManager.getService(Context.NOTIFICATION_SERVICE));
        }
        return this.mNoMan;
    }

    public final void notifyCondition(Condition condition) {
        if (condition != null) {
            notifyConditions(condition);
        }
    }

    public final void notifyConditions(Condition... conditions) {
        if (isBound() && conditions != null) {
            try {
                getNotificationInterface().notifyConditions(getPackageName(), this.mProvider, conditions);
            } catch (RemoteException ex) {
                Log.v(this.TAG, "Unable to contact notification manager", ex);
            }
        }
    }

    public IBinder onBind(Intent intent) {
        if (this.mProvider == null) {
            this.mProvider = new Provider();
        }
        return this.mProvider;
    }

    private boolean isBound() {
        if (this.mProvider != null) {
            return true;
        }
        Log.w(this.TAG, "Condition provider service not yet bound.");
        return false;
    }
}

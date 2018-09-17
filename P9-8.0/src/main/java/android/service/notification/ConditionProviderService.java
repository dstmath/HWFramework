package android.service.notification;

import android.app.INotificationManager;
import android.app.Service;
import android.content.ComponentName;
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
    private final String TAG = (ConditionProviderService.class.getSimpleName() + "[" + getClass().getSimpleName() + "]");
    private final H mHandler = new H(this, null);
    private INotificationManager mNoMan;
    private Provider mProvider;

    private final class H extends Handler {
        private static final int ON_CONNECTED = 1;
        private static final int ON_SUBSCRIBE = 3;
        private static final int ON_UNSUBSCRIBE = 4;

        /* synthetic */ H(ConditionProviderService this$0, H -this1) {
            this();
        }

        private H() {
        }

        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            if (ConditionProviderService.this.isBound()) {
                try {
                    String name;
                    switch (msg.what) {
                        case 1:
                            name = "onConnected";
                            ConditionProviderService.this.onConnected();
                            break;
                        case 3:
                            name = "onSubscribe";
                            ConditionProviderService.this.onSubscribe((Uri) msg.obj);
                            break;
                        case 4:
                            name = "onUnsubscribe";
                            ConditionProviderService.this.onUnsubscribe((Uri) msg.obj);
                            break;
                    }
                } catch (Throwable t) {
                    Log.w(ConditionProviderService.this.TAG, "Error running " + null, t);
                }
            }
        }
    }

    private final class Provider extends Stub {
        /* synthetic */ Provider(ConditionProviderService this$0, Provider -this1) {
            this();
        }

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

    public void onRequestConditions(int relevance) {
    }

    private final INotificationManager getNotificationInterface() {
        if (this.mNoMan == null) {
            this.mNoMan = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
        }
        return this.mNoMan;
    }

    public static final void requestRebind(ComponentName componentName) {
        try {
            INotificationManager.Stub.asInterface(ServiceManager.getService("notification")).requestBindProvider(componentName);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public final void requestUnbind() {
        try {
            getNotificationInterface().requestUnbindProvider(this.mProvider);
            this.mProvider = null;
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
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
            this.mProvider = new Provider(this, null);
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

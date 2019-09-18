package android.service.notification;

import android.annotation.SystemApi;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.service.notification.NotificationListenerService;
import android.util.Log;
import com.android.internal.os.SomeArgs;
import java.util.List;

@SystemApi
public abstract class NotificationAssistantService extends NotificationListenerService {
    public static final String SERVICE_INTERFACE = "android.service.notification.NotificationAssistantService";
    private static final String TAG = "NotificationAssistants";
    protected Handler mHandler;

    private final class MyHandler extends Handler {
        public static final int MSG_ON_NOTIFICATION_ENQUEUED = 1;
        public static final int MSG_ON_NOTIFICATION_SNOOZED = 2;

        public MyHandler(Looper looper) {
            super(looper, null, false);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SomeArgs args = (SomeArgs) msg.obj;
                    args.recycle();
                    Adjustment adjustment = NotificationAssistantService.this.onNotificationEnqueued((StatusBarNotification) args.arg1);
                    if (adjustment != null && NotificationAssistantService.this.isBound()) {
                        try {
                            NotificationAssistantService.this.getNotificationInterface().applyEnqueuedAdjustmentFromAssistant(NotificationAssistantService.this.mWrapper, adjustment);
                            break;
                        } catch (RemoteException ex) {
                            Log.v(NotificationAssistantService.TAG, "Unable to contact notification manager", ex);
                            throw ex.rethrowFromSystemServer();
                        }
                    } else {
                        return;
                    }
                case 2:
                    SomeArgs args2 = (SomeArgs) msg.obj;
                    args2.recycle();
                    NotificationAssistantService.this.onNotificationSnoozedUntilContext((StatusBarNotification) args2.arg1, (String) args2.arg2);
                    break;
            }
        }
    }

    private class NotificationAssistantServiceWrapper extends NotificationListenerService.NotificationListenerWrapper {
        private NotificationAssistantServiceWrapper() {
            super();
        }

        public void onNotificationEnqueued(IStatusBarNotificationHolder sbnHolder) {
            try {
                StatusBarNotification sbn = sbnHolder.get();
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = sbn;
                NotificationAssistantService.this.mHandler.obtainMessage(1, args).sendToTarget();
            } catch (RemoteException e) {
                Log.w(NotificationAssistantService.TAG, "onNotificationEnqueued: Error receiving StatusBarNotification", e);
            }
        }

        public void onNotificationSnoozedUntilContext(IStatusBarNotificationHolder sbnHolder, String snoozeCriterionId) throws RemoteException {
            try {
                StatusBarNotification sbn = sbnHolder.get();
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = sbn;
                args.arg2 = snoozeCriterionId;
                NotificationAssistantService.this.mHandler.obtainMessage(2, args).sendToTarget();
            } catch (RemoteException e) {
                Log.w(NotificationAssistantService.TAG, "onNotificationSnoozed: Error receiving StatusBarNotification", e);
            }
        }
    }

    public abstract Adjustment onNotificationEnqueued(StatusBarNotification statusBarNotification);

    public abstract void onNotificationSnoozedUntilContext(StatusBarNotification statusBarNotification, String str);

    /* access modifiers changed from: protected */
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        this.mHandler = new MyHandler(getContext().getMainLooper());
    }

    public final IBinder onBind(Intent intent) {
        if (this.mWrapper == null) {
            this.mWrapper = new NotificationAssistantServiceWrapper();
        }
        return this.mWrapper;
    }

    public void onNotificationRemoved(StatusBarNotification sbn, NotificationListenerService.RankingMap rankingMap, NotificationStats stats, int reason) {
        onNotificationRemoved(sbn, rankingMap, reason);
    }

    public final void adjustNotification(Adjustment adjustment) {
        if (isBound()) {
            try {
                getNotificationInterface().applyAdjustmentFromAssistant(this.mWrapper, adjustment);
            } catch (RemoteException ex) {
                Log.v(TAG, "Unable to contact notification manager", ex);
                throw ex.rethrowFromSystemServer();
            }
        }
    }

    public final void adjustNotifications(List<Adjustment> adjustments) {
        if (isBound()) {
            try {
                getNotificationInterface().applyAdjustmentsFromAssistant(this.mWrapper, adjustments);
            } catch (RemoteException ex) {
                Log.v(TAG, "Unable to contact notification manager", ex);
                throw ex.rethrowFromSystemServer();
            }
        }
    }

    public final void unsnoozeNotification(String key) {
        if (isBound()) {
            try {
                getNotificationInterface().unsnoozeNotificationFromAssistant(this.mWrapper, key);
            } catch (RemoteException ex) {
                Log.v(TAG, "Unable to contact notification manager", ex);
            }
        }
    }
}

package android.service.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.os.SomeArgs;
import java.util.List;

public abstract class NotificationAssistantService extends NotificationListenerService {
    public static final String SERVICE_INTERFACE = "android.service.notification.NotificationAssistantService";
    private static final String TAG = "NotificationAssistants";
    private Handler mHandler;

    private final class MyHandler extends Handler {
        public static final int MSG_ON_NOTIFICATION_ENQUEUED = 1;
        public static final int MSG_ON_NOTIFICATION_SNOOZED = 2;

        public MyHandler(Looper looper) {
            super(looper, null, false);
        }

        public void handleMessage(Message msg) {
            SomeArgs args;
            StatusBarNotification sbn;
            switch (msg.what) {
                case 1:
                    args = msg.obj;
                    sbn = args.arg1;
                    args.recycle();
                    Adjustment adjustment = NotificationAssistantService.this.onNotificationEnqueued(sbn);
                    if (adjustment != null && NotificationAssistantService.this.isBound()) {
                        try {
                            NotificationAssistantService.this.getNotificationInterface().applyEnqueuedAdjustmentFromAssistant(NotificationAssistantService.this.mWrapper, adjustment);
                            break;
                        } catch (RemoteException ex) {
                            Log.v(NotificationAssistantService.TAG, "Unable to contact notification manager", ex);
                            throw ex.rethrowFromSystemServer();
                        }
                    }
                    return;
                case 2:
                    args = (SomeArgs) msg.obj;
                    sbn = (StatusBarNotification) args.arg1;
                    String snoozeCriterionId = args.arg2;
                    args.recycle();
                    NotificationAssistantService.this.onNotificationSnoozedUntilContext(sbn, snoozeCriterionId);
                    break;
            }
        }
    }

    private class NotificationAssistantServiceWrapper extends NotificationListenerWrapper {
        /* synthetic */ NotificationAssistantServiceWrapper(NotificationAssistantService this$0, NotificationAssistantServiceWrapper -this1) {
            this();
        }

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

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        this.mHandler = new MyHandler(getContext().getMainLooper());
    }

    public final IBinder onBind(Intent intent) {
        if (this.mWrapper == null) {
            this.mWrapper = new NotificationAssistantServiceWrapper(this, null);
        }
        return this.mWrapper;
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

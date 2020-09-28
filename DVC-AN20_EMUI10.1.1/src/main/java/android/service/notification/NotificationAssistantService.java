package android.service.notification;

import android.annotation.SystemApi;
import android.app.Notification;
import android.app.NotificationChannel;
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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

@SystemApi
public abstract class NotificationAssistantService extends NotificationListenerService {
    public static final String SERVICE_INTERFACE = "android.service.notification.NotificationAssistantService";
    public static final int SOURCE_FROM_APP = 0;
    public static final int SOURCE_FROM_ASSISTANT = 1;
    private static final String TAG = "NotificationAssistants";
    protected Handler mHandler;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Source {
    }

    public abstract Adjustment onNotificationEnqueued(StatusBarNotification statusBarNotification);

    public abstract void onNotificationSnoozedUntilContext(StatusBarNotification statusBarNotification, String str);

    /* access modifiers changed from: protected */
    @Override // android.service.notification.NotificationListenerService, android.content.ContextWrapper
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        this.mHandler = new MyHandler(getContext().getMainLooper());
    }

    @Override // android.app.Service, android.service.notification.NotificationListenerService
    public final IBinder onBind(Intent intent) {
        if (this.mWrapper == null) {
            this.mWrapper = new NotificationAssistantServiceWrapper();
        }
        return this.mWrapper;
    }

    public Adjustment onNotificationEnqueued(StatusBarNotification sbn, NotificationChannel channel) {
        return onNotificationEnqueued(sbn);
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationRemoved(StatusBarNotification sbn, NotificationListenerService.RankingMap rankingMap, NotificationStats stats, int reason) {
        onNotificationRemoved(sbn, rankingMap, reason);
    }

    public void onNotificationsSeen(List<String> list) {
    }

    public void onNotificationExpansionChanged(String key, boolean isUserAction, boolean isExpanded) {
    }

    public void onNotificationDirectReplied(String key) {
    }

    public void onSuggestedReplySent(String key, CharSequence reply, int source) {
    }

    public void onActionInvoked(String key, Notification.Action action, int source) {
    }

    public void onAllowedAdjustmentsChanged() {
    }

    public final void adjustNotification(Adjustment adjustment) {
        if (isBound()) {
            try {
                setAdjustmentIssuer(adjustment);
                getNotificationInterface().applyEnqueuedAdjustmentFromAssistant(this.mWrapper, adjustment);
            } catch (RemoteException ex) {
                Log.v(TAG, "Unable to contact notification manager", ex);
                throw ex.rethrowFromSystemServer();
            }
        }
    }

    public final void adjustNotifications(List<Adjustment> adjustments) {
        if (isBound()) {
            try {
                for (Adjustment adjustment : adjustments) {
                    setAdjustmentIssuer(adjustment);
                }
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

    private class NotificationAssistantServiceWrapper extends NotificationListenerService.NotificationListenerWrapper {
        private NotificationAssistantServiceWrapper() {
            super();
        }

        @Override // android.service.notification.NotificationListenerService.NotificationListenerWrapper, android.service.notification.INotificationListener
        public void onNotificationEnqueuedWithChannel(IStatusBarNotificationHolder sbnHolder, NotificationChannel channel) {
            try {
                StatusBarNotification sbn = sbnHolder.get();
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = sbn;
                args.arg2 = channel;
                NotificationAssistantService.this.mHandler.obtainMessage(1, args).sendToTarget();
            } catch (RemoteException e) {
                Log.w(NotificationAssistantService.TAG, "onNotificationEnqueued: Error receiving StatusBarNotification", e);
            }
        }

        @Override // android.service.notification.NotificationListenerService.NotificationListenerWrapper, android.service.notification.INotificationListener
        public void onNotificationSnoozedUntilContext(IStatusBarNotificationHolder sbnHolder, String snoozeCriterionId) {
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

        @Override // android.service.notification.NotificationListenerService.NotificationListenerWrapper, android.service.notification.INotificationListener
        public void onNotificationsSeen(List<String> keys) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = keys;
            NotificationAssistantService.this.mHandler.obtainMessage(3, args).sendToTarget();
        }

        @Override // android.service.notification.NotificationListenerService.NotificationListenerWrapper, android.service.notification.INotificationListener
        public void onNotificationExpansionChanged(String key, boolean isUserAction, boolean isExpanded) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = key;
            args.argi1 = isUserAction ? 1 : 0;
            args.argi2 = isExpanded ? 1 : 0;
            NotificationAssistantService.this.mHandler.obtainMessage(4, args).sendToTarget();
        }

        @Override // android.service.notification.NotificationListenerService.NotificationListenerWrapper, android.service.notification.INotificationListener
        public void onNotificationDirectReply(String key) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = key;
            NotificationAssistantService.this.mHandler.obtainMessage(5, args).sendToTarget();
        }

        @Override // android.service.notification.NotificationListenerService.NotificationListenerWrapper, android.service.notification.INotificationListener
        public void onSuggestedReplySent(String key, CharSequence reply, int source) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = key;
            args.arg2 = reply;
            args.argi2 = source;
            NotificationAssistantService.this.mHandler.obtainMessage(6, args).sendToTarget();
        }

        @Override // android.service.notification.NotificationListenerService.NotificationListenerWrapper, android.service.notification.INotificationListener
        public void onActionClicked(String key, Notification.Action action, int source) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = key;
            args.arg2 = action;
            args.argi2 = source;
            NotificationAssistantService.this.mHandler.obtainMessage(7, args).sendToTarget();
        }

        @Override // android.service.notification.NotificationListenerService.NotificationListenerWrapper, android.service.notification.INotificationListener
        public void onAllowedAdjustmentsChanged() {
            NotificationAssistantService.this.mHandler.obtainMessage(8).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAdjustmentIssuer(Adjustment adjustment) {
        if (adjustment != null) {
            adjustment.setIssuer(getOpPackageName() + "/" + getClass().getName());
        }
    }

    private final class MyHandler extends Handler {
        public static final int MSG_ON_ACTION_INVOKED = 7;
        public static final int MSG_ON_ALLOWED_ADJUSTMENTS_CHANGED = 8;
        public static final int MSG_ON_NOTIFICATIONS_SEEN = 3;
        public static final int MSG_ON_NOTIFICATION_DIRECT_REPLY_SENT = 5;
        public static final int MSG_ON_NOTIFICATION_ENQUEUED = 1;
        public static final int MSG_ON_NOTIFICATION_EXPANSION_CHANGED = 4;
        public static final int MSG_ON_NOTIFICATION_SNOOZED = 2;
        public static final int MSG_ON_SUGGESTED_REPLY_SENT = 6;

        public MyHandler(Looper looper) {
            super(looper, null, false);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SomeArgs args = (SomeArgs) msg.obj;
                    args.recycle();
                    Adjustment adjustment = NotificationAssistantService.this.onNotificationEnqueued((StatusBarNotification) args.arg1, (NotificationChannel) args.arg2);
                    NotificationAssistantService.this.setAdjustmentIssuer(adjustment);
                    if (adjustment == null) {
                        return;
                    }
                    if (!NotificationAssistantService.this.isBound()) {
                        Log.w(NotificationAssistantService.TAG, "MSG_ON_NOTIFICATION_ENQUEUED: service not bound, skip.");
                        return;
                    }
                    try {
                        NotificationAssistantService.this.getNotificationInterface().applyEnqueuedAdjustmentFromAssistant(NotificationAssistantService.this.mWrapper, adjustment);
                        return;
                    } catch (RemoteException ex) {
                        Log.v(NotificationAssistantService.TAG, "Unable to contact notification manager", ex);
                        throw ex.rethrowFromSystemServer();
                    } catch (SecurityException e) {
                        Log.w(NotificationAssistantService.TAG, "Enqueue adjustment failed; no longer connected", e);
                        return;
                    }
                case 2:
                    SomeArgs args2 = (SomeArgs) msg.obj;
                    args2.recycle();
                    NotificationAssistantService.this.onNotificationSnoozedUntilContext((StatusBarNotification) args2.arg1, (String) args2.arg2);
                    return;
                case 3:
                    SomeArgs args3 = (SomeArgs) msg.obj;
                    args3.recycle();
                    NotificationAssistantService.this.onNotificationsSeen((List) args3.arg1);
                    return;
                case 4:
                    SomeArgs args4 = (SomeArgs) msg.obj;
                    String key = (String) args4.arg1;
                    boolean isExpanded = false;
                    boolean isUserAction = args4.argi1 == 1;
                    if (args4.argi2 == 1) {
                        isExpanded = true;
                    }
                    args4.recycle();
                    NotificationAssistantService.this.onNotificationExpansionChanged(key, isUserAction, isExpanded);
                    return;
                case 5:
                    SomeArgs args5 = (SomeArgs) msg.obj;
                    args5.recycle();
                    NotificationAssistantService.this.onNotificationDirectReplied((String) args5.arg1);
                    return;
                case 6:
                    SomeArgs args6 = (SomeArgs) msg.obj;
                    int source = args6.argi2;
                    args6.recycle();
                    NotificationAssistantService.this.onSuggestedReplySent((String) args6.arg1, (CharSequence) args6.arg2, source);
                    return;
                case 7:
                    SomeArgs args7 = (SomeArgs) msg.obj;
                    int source2 = args7.argi2;
                    args7.recycle();
                    NotificationAssistantService.this.onActionInvoked((String) args7.arg1, (Notification.Action) args7.arg2, source2);
                    return;
                case 8:
                    NotificationAssistantService.this.onAllowedAdjustmentsChanged();
                    return;
                default:
                    return;
            }
        }
    }
}

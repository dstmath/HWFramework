package android.service.notification;

import android.content.ComponentName;
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

public abstract class NotificationRankerService extends NotificationListenerService {
    public static final int REASON_APP_CANCEL = 8;
    public static final int REASON_APP_CANCEL_ALL = 9;
    public static final int REASON_DELEGATE_CANCEL = 2;
    public static final int REASON_DELEGATE_CANCEL_ALL = 3;
    public static final int REASON_DELEGATE_CLICK = 1;
    public static final int REASON_DELEGATE_ERROR = 4;
    public static final int REASON_GROUP_OPTIMIZATION = 13;
    public static final int REASON_GROUP_SUMMARY_CANCELED = 12;
    public static final int REASON_LISTENER_CANCEL = 10;
    public static final int REASON_LISTENER_CANCEL_ALL = 11;
    public static final int REASON_PACKAGE_BANNED = 7;
    public static final int REASON_PACKAGE_CHANGED = 5;
    public static final int REASON_PACKAGE_SUSPENDED = 14;
    public static final int REASON_PROFILE_TURNED_OFF = 15;
    public static final int REASON_UNAUTOBUNDLED = 16;
    public static final int REASON_USER_STOPPED = 6;
    public static final String SERVICE_INTERFACE = "android.service.notification.NotificationRankerService";
    private static final String TAG = "NotificationRankers";
    private Handler mHandler;

    private final class MyHandler extends Handler {
        public static final int MSG_ON_NOTIFICATION_ACTION_CLICK = 4;
        public static final int MSG_ON_NOTIFICATION_CLICK = 3;
        public static final int MSG_ON_NOTIFICATION_ENQUEUED = 1;
        public static final int MSG_ON_NOTIFICATION_REMOVED_REASON = 5;
        public static final int MSG_ON_NOTIFICATION_VISIBILITY_CHANGED = 2;

        public MyHandler(Looper looper) {
            super(looper, null, false);
        }

        public void handleMessage(Message msg) {
            SomeArgs args;
            String key;
            long time;
            switch (msg.what) {
                case MSG_ON_NOTIFICATION_ENQUEUED /*1*/:
                    args = msg.obj;
                    StatusBarNotification sbn = args.arg1;
                    int importance = args.argi1;
                    boolean user = args.argi2 == MSG_ON_NOTIFICATION_ENQUEUED;
                    args.recycle();
                    Adjustment adjustment = NotificationRankerService.this.onNotificationEnqueued(sbn, importance, user);
                    if (adjustment != null) {
                        NotificationRankerService.this.adjustNotification(adjustment);
                    }
                case MSG_ON_NOTIFICATION_VISIBILITY_CHANGED /*2*/:
                    args = (SomeArgs) msg.obj;
                    key = args.arg1;
                    time = ((Long) args.arg2).longValue();
                    boolean visible = args.argi1 == MSG_ON_NOTIFICATION_ENQUEUED;
                    args.recycle();
                    NotificationRankerService.this.onNotificationVisibilityChanged(key, time, visible);
                case MSG_ON_NOTIFICATION_CLICK /*3*/:
                    args = (SomeArgs) msg.obj;
                    key = (String) args.arg1;
                    time = ((Long) args.arg2).longValue();
                    args.recycle();
                    NotificationRankerService.this.onNotificationClick(key, time);
                case MSG_ON_NOTIFICATION_ACTION_CLICK /*4*/:
                    args = (SomeArgs) msg.obj;
                    key = (String) args.arg1;
                    time = ((Long) args.arg2).longValue();
                    int actionIndex = args.argi1;
                    args.recycle();
                    NotificationRankerService.this.onNotificationActionClick(key, time, actionIndex);
                case MSG_ON_NOTIFICATION_REMOVED_REASON /*5*/:
                    args = (SomeArgs) msg.obj;
                    key = (String) args.arg1;
                    time = ((Long) args.arg2).longValue();
                    int reason = args.argi1;
                    args.recycle();
                    NotificationRankerService.this.onNotificationRemoved(key, time, reason);
                default:
            }
        }
    }

    private class NotificationRankingServiceWrapper extends NotificationListenerWrapper {
        private NotificationRankingServiceWrapper() {
            super();
        }

        public void onNotificationEnqueued(IStatusBarNotificationHolder sbnHolder, int importance, boolean user) {
            try {
                StatusBarNotification sbn = sbnHolder.get();
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = sbn;
                args.argi1 = importance;
                args.argi2 = user ? NotificationRankerService.REASON_DELEGATE_CLICK : 0;
                NotificationRankerService.this.mHandler.obtainMessage(NotificationRankerService.REASON_DELEGATE_CLICK, args).sendToTarget();
            } catch (RemoteException e) {
                Log.w(NotificationRankerService.TAG, "onNotificationEnqueued: Error receiving StatusBarNotification", e);
            }
        }

        public void onNotificationVisibilityChanged(String key, long time, boolean visible) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = key;
            args.arg2 = Long.valueOf(time);
            args.argi1 = visible ? NotificationRankerService.REASON_DELEGATE_CLICK : 0;
            NotificationRankerService.this.mHandler.obtainMessage(NotificationRankerService.REASON_DELEGATE_CANCEL, args).sendToTarget();
        }

        public void onNotificationClick(String key, long time) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = key;
            args.arg2 = Long.valueOf(time);
            NotificationRankerService.this.mHandler.obtainMessage(NotificationRankerService.REASON_DELEGATE_CANCEL_ALL, args).sendToTarget();
        }

        public void onNotificationActionClick(String key, long time, int actionIndex) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = key;
            args.arg2 = Long.valueOf(time);
            args.argi1 = actionIndex;
            NotificationRankerService.this.mHandler.obtainMessage(NotificationRankerService.REASON_DELEGATE_ERROR, args).sendToTarget();
        }

        public void onNotificationRemovedReason(String key, long time, int reason) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = key;
            args.arg2 = Long.valueOf(time);
            args.argi1 = reason;
            NotificationRankerService.this.mHandler.obtainMessage(NotificationRankerService.REASON_PACKAGE_CHANGED, args).sendToTarget();
        }
    }

    public abstract Adjustment onNotificationEnqueued(StatusBarNotification statusBarNotification, int i, boolean z);

    public void registerAsSystemService(Context context, ComponentName componentName, int currentUser) {
        throw new UnsupportedOperationException("the ranker lifecycle is managed by the system.");
    }

    public void unregisterAsSystemService() {
        throw new UnsupportedOperationException("the ranker lifecycle is managed by the system.");
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        this.mHandler = new MyHandler(getContext().getMainLooper());
    }

    public final IBinder onBind(Intent intent) {
        if (this.mWrapper == null) {
            this.mWrapper = new NotificationRankingServiceWrapper();
        }
        return this.mWrapper;
    }

    public void onNotificationVisibilityChanged(String key, long time, boolean visible) {
    }

    public void onNotificationClick(String key, long time) {
    }

    public void onNotificationActionClick(String key, long time, int actionIndex) {
    }

    public void onNotificationRemoved(String key, long time, int reason) {
    }

    public final void adjustNotification(Adjustment adjustment) {
        if (isBound()) {
            try {
                getNotificationInterface().applyAdjustmentFromRankerService(this.mWrapper, adjustment);
            } catch (RemoteException ex) {
                Log.v(TAG, "Unable to contact notification manager", ex);
            }
        }
    }

    public final void adjustNotifications(List<Adjustment> adjustments) {
        if (isBound()) {
            try {
                getNotificationInterface().applyAdjustmentsFromRankerService(this.mWrapper, adjustments);
            } catch (RemoteException ex) {
                Log.v(TAG, "Unable to contact notification manager", ex);
            }
        }
    }
}

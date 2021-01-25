package android.support.v4.app;

import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.INotificationSideChannel;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NotificationManagerCompat {
    public static final String ACTION_BIND_SIDE_CHANNEL = "android.support.BIND_NOTIFICATION_SIDE_CHANNEL";
    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    public static final String EXTRA_USE_SIDE_CHANNEL = "android.support.useSideChannel";
    public static final int IMPORTANCE_DEFAULT = 3;
    public static final int IMPORTANCE_HIGH = 4;
    public static final int IMPORTANCE_LOW = 2;
    public static final int IMPORTANCE_MAX = 5;
    public static final int IMPORTANCE_MIN = 1;
    public static final int IMPORTANCE_NONE = 0;
    public static final int IMPORTANCE_UNSPECIFIED = -1000;
    static final int MAX_SIDE_CHANNEL_SDK_VERSION = 19;
    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";
    private static final String SETTING_ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final int SIDE_CHANNEL_RETRY_BASE_INTERVAL_MS = 1000;
    private static final int SIDE_CHANNEL_RETRY_MAX_COUNT = 6;
    private static final String TAG = "NotifManCompat";
    @GuardedBy("sEnabledNotificationListenersLock")
    private static Set<String> sEnabledNotificationListenerPackages = new HashSet();
    @GuardedBy("sEnabledNotificationListenersLock")
    private static String sEnabledNotificationListeners;
    private static final Object sEnabledNotificationListenersLock = new Object();
    private static final Object sLock = new Object();
    @GuardedBy("sLock")
    private static SideChannelManager sSideChannelManager;
    private final Context mContext;
    private final NotificationManager mNotificationManager = ((NotificationManager) this.mContext.getSystemService("notification"));

    /* access modifiers changed from: private */
    public interface Task {
        void send(INotificationSideChannel iNotificationSideChannel) throws RemoteException;
    }

    @NonNull
    public static NotificationManagerCompat from(@NonNull Context context) {
        return new NotificationManagerCompat(context);
    }

    private NotificationManagerCompat(Context context) {
        this.mContext = context;
    }

    public void cancel(int id) {
        cancel(null, id);
    }

    public void cancel(@Nullable String tag, int id) {
        this.mNotificationManager.cancel(tag, id);
        if (Build.VERSION.SDK_INT <= 19) {
            pushSideChannelQueue(new CancelTask(this.mContext.getPackageName(), id, tag));
        }
    }

    public void cancelAll() {
        this.mNotificationManager.cancelAll();
        if (Build.VERSION.SDK_INT <= 19) {
            pushSideChannelQueue(new CancelTask(this.mContext.getPackageName()));
        }
    }

    public void notify(int id, @NonNull Notification notification) {
        notify(null, id, notification);
    }

    public void notify(@Nullable String tag, int id, @NonNull Notification notification) {
        if (useSideChannelForNotification(notification)) {
            pushSideChannelQueue(new NotifyTask(this.mContext.getPackageName(), id, tag, notification));
            this.mNotificationManager.cancel(tag, id);
            return;
        }
        this.mNotificationManager.notify(tag, id, notification);
    }

    public boolean areNotificationsEnabled() {
        if (Build.VERSION.SDK_INT >= 24) {
            return this.mNotificationManager.areNotificationsEnabled();
        }
        if (Build.VERSION.SDK_INT < 19) {
            return true;
        }
        AppOpsManager appOps = (AppOpsManager) this.mContext.getSystemService("appops");
        ApplicationInfo appInfo = this.mContext.getApplicationInfo();
        String pkg = this.mContext.getApplicationContext().getPackageName();
        int uid = appInfo.uid;
        try {
            Class<?> appOpsClass = Class.forName(AppOpsManager.class.getName());
            if (((Integer) appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE, String.class).invoke(appOps, Integer.valueOf(((Integer) appOpsClass.getDeclaredField(OP_POST_NOTIFICATION).get(Integer.class)).intValue()), Integer.valueOf(uid), pkg)).intValue() == 0) {
                return true;
            }
            return false;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException | RuntimeException | InvocationTargetException e) {
            return true;
        }
    }

    public int getImportance() {
        if (Build.VERSION.SDK_INT >= 24) {
            return this.mNotificationManager.getImportance();
        }
        return IMPORTANCE_UNSPECIFIED;
    }

    @NonNull
    public static Set<String> getEnabledListenerPackages(@NonNull Context context) {
        Set<String> set;
        String enabledNotificationListeners = Settings.Secure.getString(context.getContentResolver(), SETTING_ENABLED_NOTIFICATION_LISTENERS);
        synchronized (sEnabledNotificationListenersLock) {
            if (enabledNotificationListeners != null) {
                try {
                    if (!enabledNotificationListeners.equals(sEnabledNotificationListeners)) {
                        String[] components = enabledNotificationListeners.split(":", -1);
                        Set<String> packageNames = new HashSet<>(components.length);
                        for (String component : components) {
                            ComponentName componentName = ComponentName.unflattenFromString(component);
                            if (componentName != null) {
                                packageNames.add(componentName.getPackageName());
                            }
                        }
                        sEnabledNotificationListenerPackages = packageNames;
                        sEnabledNotificationListeners = enabledNotificationListeners;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            set = sEnabledNotificationListenerPackages;
        }
        return set;
    }

    private static boolean useSideChannelForNotification(Notification notification) {
        Bundle extras = NotificationCompat.getExtras(notification);
        return extras != null && extras.getBoolean(EXTRA_USE_SIDE_CHANNEL);
    }

    private void pushSideChannelQueue(Task task) {
        synchronized (sLock) {
            if (sSideChannelManager == null) {
                sSideChannelManager = new SideChannelManager(this.mContext.getApplicationContext());
            }
            sSideChannelManager.queueTask(task);
        }
    }

    /* access modifiers changed from: private */
    public static class SideChannelManager implements Handler.Callback, ServiceConnection {
        private static final int MSG_QUEUE_TASK = 0;
        private static final int MSG_RETRY_LISTENER_QUEUE = 3;
        private static final int MSG_SERVICE_CONNECTED = 1;
        private static final int MSG_SERVICE_DISCONNECTED = 2;
        private Set<String> mCachedEnabledPackages = new HashSet();
        private final Context mContext;
        private final Handler mHandler;
        private final HandlerThread mHandlerThread;
        private final Map<ComponentName, ListenerRecord> mRecordMap = new HashMap();

        SideChannelManager(Context context) {
            this.mContext = context;
            this.mHandlerThread = new HandlerThread("NotificationManagerCompat");
            this.mHandlerThread.start();
            this.mHandler = new Handler(this.mHandlerThread.getLooper(), this);
        }

        public void queueTask(Task task) {
            this.mHandler.obtainMessage(0, task).sendToTarget();
        }

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    handleQueueTask((Task) msg.obj);
                    return true;
                case 1:
                    ServiceConnectedEvent event = (ServiceConnectedEvent) msg.obj;
                    handleServiceConnected(event.componentName, event.iBinder);
                    return true;
                case 2:
                    handleServiceDisconnected((ComponentName) msg.obj);
                    return true;
                case 3:
                    handleRetryListenerQueue((ComponentName) msg.obj);
                    return true;
                default:
                    return false;
            }
        }

        private void handleQueueTask(Task task) {
            updateListenerMap();
            for (ListenerRecord record : this.mRecordMap.values()) {
                record.taskQueue.add(task);
                processListenerQueue(record);
            }
        }

        private void handleServiceConnected(ComponentName componentName, IBinder iBinder) {
            ListenerRecord record = this.mRecordMap.get(componentName);
            if (record != null) {
                record.service = INotificationSideChannel.Stub.asInterface(iBinder);
                record.retryCount = 0;
                processListenerQueue(record);
            }
        }

        private void handleServiceDisconnected(ComponentName componentName) {
            ListenerRecord record = this.mRecordMap.get(componentName);
            if (record != null) {
                ensureServiceUnbound(record);
            }
        }

        private void handleRetryListenerQueue(ComponentName componentName) {
            ListenerRecord record = this.mRecordMap.get(componentName);
            if (record != null) {
                processListenerQueue(record);
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (Log.isLoggable(NotificationManagerCompat.TAG, 3)) {
                Log.d(NotificationManagerCompat.TAG, "Connected to service " + componentName);
            }
            this.mHandler.obtainMessage(1, new ServiceConnectedEvent(componentName, iBinder)).sendToTarget();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            if (Log.isLoggable(NotificationManagerCompat.TAG, 3)) {
                Log.d(NotificationManagerCompat.TAG, "Disconnected from service " + componentName);
            }
            this.mHandler.obtainMessage(2, componentName).sendToTarget();
        }

        private void updateListenerMap() {
            Set<String> enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(this.mContext);
            if (!enabledPackages.equals(this.mCachedEnabledPackages)) {
                this.mCachedEnabledPackages = enabledPackages;
                List<ResolveInfo> resolveInfos = this.mContext.getPackageManager().queryIntentServices(new Intent().setAction(NotificationManagerCompat.ACTION_BIND_SIDE_CHANNEL), 0);
                Set<ComponentName> enabledComponents = new HashSet<>();
                for (ResolveInfo resolveInfo : resolveInfos) {
                    if (enabledPackages.contains(resolveInfo.serviceInfo.packageName)) {
                        ComponentName componentName = new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
                        if (resolveInfo.serviceInfo.permission != null) {
                            Log.w(NotificationManagerCompat.TAG, "Permission present on component " + componentName + ", not adding listener record.");
                        } else {
                            enabledComponents.add(componentName);
                        }
                    }
                }
                for (ComponentName componentName2 : enabledComponents) {
                    if (!this.mRecordMap.containsKey(componentName2)) {
                        if (Log.isLoggable(NotificationManagerCompat.TAG, 3)) {
                            Log.d(NotificationManagerCompat.TAG, "Adding listener record for " + componentName2);
                        }
                        this.mRecordMap.put(componentName2, new ListenerRecord(componentName2));
                    }
                }
                Iterator<Map.Entry<ComponentName, ListenerRecord>> it = this.mRecordMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<ComponentName, ListenerRecord> entry = it.next();
                    if (!enabledComponents.contains(entry.getKey())) {
                        if (Log.isLoggable(NotificationManagerCompat.TAG, 3)) {
                            Log.d(NotificationManagerCompat.TAG, "Removing listener record for " + entry.getKey());
                        }
                        ensureServiceUnbound(entry.getValue());
                        it.remove();
                    }
                }
            }
        }

        private boolean ensureServiceBound(ListenerRecord record) {
            if (record.bound) {
                return true;
            }
            record.bound = this.mContext.bindService(new Intent(NotificationManagerCompat.ACTION_BIND_SIDE_CHANNEL).setComponent(record.componentName), this, 33);
            if (record.bound) {
                record.retryCount = 0;
            } else {
                Log.w(NotificationManagerCompat.TAG, "Unable to bind to listener " + record.componentName);
                this.mContext.unbindService(this);
            }
            return record.bound;
        }

        private void ensureServiceUnbound(ListenerRecord record) {
            if (record.bound) {
                this.mContext.unbindService(this);
                record.bound = false;
            }
            record.service = null;
        }

        private void scheduleListenerRetry(ListenerRecord record) {
            if (!this.mHandler.hasMessages(3, record.componentName)) {
                record.retryCount++;
                if (record.retryCount > 6) {
                    Log.w(NotificationManagerCompat.TAG, "Giving up on delivering " + record.taskQueue.size() + " tasks to " + record.componentName + " after " + record.retryCount + " retries");
                    record.taskQueue.clear();
                    return;
                }
                int delayMs = 1000 * (1 << (record.retryCount - 1));
                if (Log.isLoggable(NotificationManagerCompat.TAG, 3)) {
                    Log.d(NotificationManagerCompat.TAG, "Scheduling retry for " + delayMs + " ms");
                }
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3, record.componentName), (long) delayMs);
            }
        }

        private void processListenerQueue(ListenerRecord record) {
            if (Log.isLoggable(NotificationManagerCompat.TAG, 3)) {
                Log.d(NotificationManagerCompat.TAG, "Processing component " + record.componentName + ", " + record.taskQueue.size() + " queued tasks");
            }
            if (!record.taskQueue.isEmpty()) {
                if (!ensureServiceBound(record) || record.service == null) {
                    scheduleListenerRetry(record);
                    return;
                }
                while (true) {
                    Task task = record.taskQueue.peek();
                    if (task == null) {
                        break;
                    }
                    try {
                        if (Log.isLoggable(NotificationManagerCompat.TAG, 3)) {
                            Log.d(NotificationManagerCompat.TAG, "Sending task " + task);
                        }
                        task.send(record.service);
                        record.taskQueue.remove();
                    } catch (DeadObjectException e) {
                        if (Log.isLoggable(NotificationManagerCompat.TAG, 3)) {
                            Log.d(NotificationManagerCompat.TAG, "Remote service has died: " + record.componentName);
                        }
                    } catch (RemoteException e2) {
                        Log.w(NotificationManagerCompat.TAG, "RemoteException communicating with " + record.componentName, e2);
                    }
                }
                if (!record.taskQueue.isEmpty()) {
                    scheduleListenerRetry(record);
                }
            }
        }

        /* access modifiers changed from: private */
        public static class ListenerRecord {
            boolean bound = false;
            final ComponentName componentName;
            int retryCount = 0;
            INotificationSideChannel service;
            ArrayDeque<Task> taskQueue = new ArrayDeque<>();

            ListenerRecord(ComponentName componentName2) {
                this.componentName = componentName2;
            }
        }
    }

    private static class ServiceConnectedEvent {
        final ComponentName componentName;
        final IBinder iBinder;

        ServiceConnectedEvent(ComponentName componentName2, IBinder iBinder2) {
            this.componentName = componentName2;
            this.iBinder = iBinder2;
        }
    }

    /* access modifiers changed from: private */
    public static class NotifyTask implements Task {
        final int id;
        final Notification notif;
        final String packageName;
        final String tag;

        NotifyTask(String packageName2, int id2, String tag2, Notification notif2) {
            this.packageName = packageName2;
            this.id = id2;
            this.tag = tag2;
            this.notif = notif2;
        }

        @Override // android.support.v4.app.NotificationManagerCompat.Task
        public void send(INotificationSideChannel service) throws RemoteException {
            service.notify(this.packageName, this.id, this.tag, this.notif);
        }

        public String toString() {
            return "NotifyTask[packageName:" + this.packageName + ", id:" + this.id + ", tag:" + this.tag + "]";
        }
    }

    /* access modifiers changed from: private */
    public static class CancelTask implements Task {
        final boolean all;
        final int id;
        final String packageName;
        final String tag;

        CancelTask(String packageName2) {
            this.packageName = packageName2;
            this.id = 0;
            this.tag = null;
            this.all = true;
        }

        CancelTask(String packageName2, int id2, String tag2) {
            this.packageName = packageName2;
            this.id = id2;
            this.tag = tag2;
            this.all = false;
        }

        @Override // android.support.v4.app.NotificationManagerCompat.Task
        public void send(INotificationSideChannel service) throws RemoteException {
            if (this.all) {
                service.cancelAll(this.packageName);
            } else {
                service.cancel(this.packageName, this.id, this.tag);
            }
        }

        public String toString() {
            return "CancelTask[packageName:" + this.packageName + ", id:" + this.id + ", tag:" + this.tag + ", all:" + this.all + "]";
        }
    }
}

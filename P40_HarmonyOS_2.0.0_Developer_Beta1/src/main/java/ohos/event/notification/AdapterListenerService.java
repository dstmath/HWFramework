package ohos.event.notification;

import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;

public class AdapterListenerService extends NotificationListenerService {
    private static final int DEFAULT_USER_ID = -1;
    private static final int INDEX_PACKAGE_NAME = 1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.NOTIFICATION_DOMAIN, TAG);
    private static final String LISTENER_THREAD_NAME = "notification_listener";
    private static final int MIN_LENGTH = 2;
    private static final int MSG_DISTURB_MODE_CHANGE = 6;
    private static final int MSG_NOTIFICATION_CANCELED = 2;
    private static final int MSG_NOTIFICATION_POSTED = 1;
    private static final int MSG_NOTIFICATION_UPDATE = 5;
    private static final int MSG_SUBSCRIBER_CONNECTED = 3;
    private static final int MSG_SUBSCRIBER_DISCONNECTED = 4;
    private static final String SPLIT_CHAR = "_";
    private static final String TAG = "AdapterListenerService";
    private static Method registerMethod;
    private final Object LOCK = new Object();
    private String currentPkg = null;
    private volatile boolean isInitial = false;
    private Handler listenerHandler = null;
    private ManagedSubscriber managedSubscriber = new ManagedSubscriber(this);

    private int getReason(int i) {
        if (i > 9) {
            return 10;
        }
        return i;
    }

    static {
        registerMethod = null;
        try {
            registerMethod = NotificationListenerService.class.getDeclaredMethod("registerAsSystemService", Context.class, ComponentName.class, Integer.TYPE);
        } catch (NoSuchMethodException unused) {
            HiLog.error(LABEL, "Unable to obtain the reflect method", new Object[0]);
        }
    }

    public void subscribeNotification(NotificationSubscriberHost notificationSubscriberHost, NotificationSubscribeInfo notificationSubscribeInfo) {
        init();
        this.managedSubscriber.addSubscriber(notificationSubscriberHost, notificationSubscribeInfo);
    }

    public void unsubscribeNotification(NotificationSubscriberHost notificationSubscriberHost, NotificationSubscribeInfo notificationSubscribeInfo) {
        this.managedSubscriber.removeSubscriber(notificationSubscriberHost, notificationSubscribeInfo);
    }

    public void subscribeNotification(NotificationSubscriberHost notificationSubscriberHost, Set<String> set) {
        init();
        this.managedSubscriber.addSubscriber(notificationSubscriberHost, set);
    }

    public void unsubscribeNotification(NotificationSubscriberHost notificationSubscriberHost, Set<String> set) {
        this.managedSubscriber.removeSubscriber(notificationSubscriberHost, set);
    }

    public void connectSubscriber(NotificationSubscriberHost notificationSubscriberHost) {
        if (this.listenerHandler != null && notificationSubscriberHost != null) {
            Message obtain = Message.obtain();
            obtain.what = 3;
            obtain.obj = notificationSubscriberHost;
            this.listenerHandler.sendMessage(obtain);
        }
    }

    public void disconnectSubscriber(NotificationSubscriberHost notificationSubscriberHost) {
        if (this.listenerHandler != null && notificationSubscriberHost != null) {
            Message obtain = Message.obtain();
            obtain.what = 4;
            obtain.obj = notificationSubscriberHost;
            this.listenerHandler.sendMessage(obtain);
        }
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationPosted(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
        if (this.listenerHandler != null && statusBarNotification != null) {
            Message obtain = Message.obtain();
            obtain.what = 1;
            obtain.obj = new CallBackInfo(statusBarNotification, rankingMap);
            this.listenerHandler.sendMessage(obtain);
        }
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationRemoved(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap, int i) {
        if (this.listenerHandler != null) {
            Message obtain = Message.obtain();
            obtain.what = 2;
            obtain.obj = new CallBackInfo(statusBarNotification, rankingMap, i);
            this.listenerHandler.sendMessage(obtain);
        }
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationRankingUpdate(NotificationListenerService.RankingMap rankingMap) {
        if (this.listenerHandler != null && rankingMap != null) {
            Message obtain = Message.obtain();
            obtain.what = 5;
            obtain.obj = rankingMap;
            this.listenerHandler.sendMessage(obtain);
        }
    }

    @Override // android.service.notification.NotificationListenerService
    public void onInterruptionFilterChanged(int i) {
        if (this.listenerHandler != null) {
            Message obtain = Message.obtain();
            obtain.what = 6;
            obtain.arg1 = i;
            this.listenerHandler.sendMessage(obtain);
        }
    }

    @Override // android.service.notification.NotificationListenerService
    public StatusBarNotification[] getActiveNotifications(String[] strArr) {
        init();
        return this.isInitial ? super.getActiveNotifications(strArr) : new StatusBarNotification[0];
    }

    /* access modifiers changed from: package-private */
    public NotificationSortingMap getCurrentAppSorting() {
        Set<NotificationSubscriberHost> matchedSubscriber;
        NotificationListenerService.RankingMap currentRanking = getCurrentRanking();
        if (currentRanking == null) {
            HiLog.debug(LABEL, "NotificationSortingMap::getCurrentSorting get RankingMap failed.", new Object[0]);
            return null;
        }
        String[] orderedKeys = currentRanking.getOrderedKeys();
        if (orderedKeys == null) {
            return null;
        }
        String currentPkg2 = getCurrentPkg(NotificationTransformer.getInstance().getAospContext());
        ArrayList arrayList = new ArrayList();
        for (String str : orderedKeys) {
            if (str != null && !str.isEmpty()) {
                String[] split = str.split("\\|");
                if (split.length >= 2 && (matchedSubscriber = this.managedSubscriber.getMatchedSubscriber(split[1])) != null && !matchedSubscriber.isEmpty()) {
                    NotificationListenerService.Ranking ranking = new NotificationListenerService.Ranking();
                    if (currentRanking.getRanking(str, ranking)) {
                        Optional<NotificationSorting> convertRankingToNotificationSorting = NotificationSortingConvert.convertRankingToNotificationSorting(ranking, currentPkg2);
                        if (convertRankingToNotificationSorting.isPresent()) {
                            arrayList.add(convertRankingToNotificationSorting.get());
                        }
                    }
                }
            }
        }
        return new NotificationSortingMap(arrayList);
    }

    /* access modifiers changed from: package-private */
    public void removeNotification(String str) {
        init();
        cancelNotification(str);
    }

    /* access modifiers changed from: package-private */
    public void removeNotifications(String str) {
        String key;
        init();
        StatusBarNotification[] activeNotifications = super.getActiveNotifications();
        if (!(activeNotifications == null || activeNotifications.length == 0)) {
            HashSet hashSet = new HashSet();
            for (StatusBarNotification statusBarNotification : activeNotifications) {
                if (!(statusBarNotification == null || (key = statusBarNotification.getKey()) == null || key.isEmpty())) {
                    String[] split = key.split("\\|");
                    if (split.length >= 2 && str.equals(split[1])) {
                        hashSet.add(key);
                    }
                }
            }
            if (!hashSet.isEmpty()) {
                cancelNotifications((String[]) hashSet.toArray(new String[0]));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeNotifications() {
        init();
        cancelAllNotifications();
    }

    private void init() {
        if (!this.isInitial) {
            synchronized (this.LOCK) {
                if (!this.isInitial) {
                    this.isInitial = registerService();
                }
            }
        }
    }

    private boolean registerService() {
        Context aospContext;
        String currentPkg2;
        if (registerMethod == null || (aospContext = NotificationTransformer.getInstance().getAospContext()) == null || (currentPkg2 = getCurrentPkg(aospContext)) == null) {
            return false;
        }
        try {
            ComponentName componentName = new ComponentName(currentPkg2, getClass().getCanonicalName());
            registerMethod.setAccessible(true);
            registerMethod.invoke(this, aospContext, componentName, -1);
            HandlerThread handlerThread = new HandlerThread(LISTENER_THREAD_NAME);
            handlerThread.start();
            Looper looper = handlerThread.getLooper();
            if (looper != null) {
                this.listenerHandler = new ListenerHandler(looper);
                return true;
            }
            HiLog.error(LABEL, "looper is null, listenerHandler create failed!", new Object[0]);
            return false;
        } catch (IllegalAccessException | NullPointerException | InvocationTargetException unused) {
            HiLog.error(LABEL, "Unable to register notification listener", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotificationPostedOrRemoved(CallBackInfo callBackInfo, int i) {
        if (callBackInfo == null) {
            HiLog.warn(LABEL, "AdapterListenerService::the param is invalid.", new Object[0]);
            return;
        }
        StatusBarNotification statusBarNotification = callBackInfo.sbn;
        if (statusBarNotification == null) {
            HiLog.warn(LABEL, "AdapterListenerService::gets the sbn from info failed.", new Object[0]);
            return;
        }
        Set<NotificationSubscriberHost> matchedSubscriber = this.managedSubscriber.getMatchedSubscriber(statusBarNotification.getPackageName());
        if (matchedSubscriber == null || matchedSubscriber.isEmpty()) {
            HiLog.warn(LABEL, "AdapterListenerService::the package name is %{public}s.", new Object[]{statusBarNotification.getPackageName()});
            return;
        }
        Optional<NotificationRequest> transformToNotificationRequest = NotificationTransformer.getInstance().transformToNotificationRequest(statusBarNotification);
        if (!transformToNotificationRequest.isPresent()) {
            HiLog.warn(LABEL, "AdapterListenerService::convert StatusBarNotification to NotificationRequest failed.", new Object[0]);
            return;
        }
        Optional<NotificationSortingMap> convertRankingMapToSorttingMap = NotificationSortingConvert.convertRankingMapToSorttingMap(callBackInfo.rankingMap);
        if (!convertRankingMapToSorttingMap.isPresent()) {
            HiLog.warn(LABEL, "AdapterListenerService::get sortingMap failed.", new Object[0]);
        }
        callPostedOrRemoved(matchedSubscriber, convertRankingMapToSorttingMap.orElse(null), transformToNotificationRequest.get(), getReason(callBackInfo.reason), i);
    }

    private void callPostedOrRemoved(Set<NotificationSubscriberHost> set, NotificationSortingMap notificationSortingMap, NotificationRequest notificationRequest, int i, int i2) {
        for (NotificationSubscriberHost notificationSubscriberHost : set) {
            if (notificationSubscriberHost != null) {
                NotificationSortingMap orElse = getSubscriberSortingMap(notificationSubscriberHost, notificationSortingMap).orElse(null);
                if (i2 == 1) {
                    try {
                        notificationSubscriberHost.onNotificationPosted(notificationRequest);
                        notificationSubscriberHost.onNotificationPosted(notificationRequest, orElse);
                    } catch (RemoteException unused) {
                        HiLog.warn(LABEL, "handleNotificationPostedOrRemoved callback failed", new Object[0]);
                    }
                } else if (i2 == 2) {
                    notificationSubscriberHost.onNotificationRemoved(notificationRequest);
                    notificationSubscriberHost.onNotificationRemoved(notificationRequest, orElse, i);
                } else {
                    HiLog.warn(LABEL, "callPostedOrRemoved::flag is unknown.", new Object[0]);
                }
            }
        }
    }

    private Optional<NotificationSortingMap> getSubscriberSortingMap(NotificationSubscriberHost notificationSubscriberHost, NotificationSortingMap notificationSortingMap) {
        if (notificationSortingMap == null) {
            return Optional.empty();
        }
        List<String> hashCode = notificationSortingMap.getHashCode();
        if (hashCode == null) {
            return Optional.empty();
        }
        ArrayList arrayList = new ArrayList();
        for (String str : hashCode) {
            if (str != null && !str.isEmpty()) {
                String[] split = str.split("_");
                if (split.length >= 2 && this.managedSubscriber.checkSubscriberAppNames(notificationSubscriberHost, split[1])) {
                    NotificationSorting notificationSorting = new NotificationSorting();
                    if (notificationSortingMap.getNotificationSorting(str, notificationSorting)) {
                        arrayList.add(notificationSorting);
                    }
                }
            }
        }
        return Optional.of(new NotificationSortingMap(arrayList));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotificationUpdate(NotificationListenerService.RankingMap rankingMap, int i) {
        NotificationSortingMap orElse = NotificationSortingConvert.convertRankingMapToSorttingMap(rankingMap).orElse(null);
        if (orElse == null) {
            HiLog.warn(LABEL, "AdapterListenerService::get sortingMap failed.", new Object[0]);
        } else {
            callbackMethods(orElse, i);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSubscriberConnectedOrDisconnected(NotificationSubscriberHost notificationSubscriberHost, int i) {
        if (notificationSubscriberHost != null) {
            if (i == 3) {
                try {
                    notificationSubscriberHost.onSubscribeConnected();
                } catch (RemoteException unused) {
                    HiLog.warn(LABEL, "AdapterListenerService::handleSubscriberConnectedOrDisconnected exception", new Object[0]);
                }
            } else if (i == 4) {
                notificationSubscriberHost.onSubscribeDisConnected();
            } else {
                HiLog.warn(LABEL, "handleSubscriberConnectedOrDisconnected::flag is unknown.", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private <T> void callbackMethods(T t, int i) {
        Set<NotificationSubscriberHost> orElse = this.managedSubscriber.getAllSubscribers().orElse(null);
        if (orElse == null || orElse.isEmpty()) {
            HiLog.warn(LABEL, "AdapterListenerService::get subscribers failed.", new Object[0]);
            return;
        }
        for (NotificationSubscriberHost notificationSubscriberHost : orElse) {
            if (notificationSubscriberHost != null) {
                if (i == 6) {
                    try {
                        if (t instanceof Integer) {
                            notificationSubscriberHost.onDisturbModeChange(t.intValue());
                        }
                    } catch (RemoteException unused) {
                        HiLog.warn(LABEL, "AdapterListenerService::happened exception.", new Object[0]);
                    }
                }
                if (i != 5 || !(t instanceof NotificationSortingMap)) {
                    HiLog.warn(LABEL, "AdapterListenerService::flag is unknown.", new Object[0]);
                } else {
                    notificationSubscriberHost.onNotificationRankingUpdate(t);
                }
            }
        }
    }

    private String getCurrentPkg(Context context) {
        if (this.currentPkg == null && context != null) {
            this.currentPkg = context.getPackageName();
        }
        return this.currentPkg;
    }

    /* access modifiers changed from: private */
    public final class ListenerHandler extends Handler {
        ListenerHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message == null) {
                HiLog.warn(AdapterListenerService.LABEL, "AdapterListenerService::message is invalid.", new Object[0]);
            } else if (message.obj instanceof CallBackInfo) {
                AdapterListenerService.this.handleNotificationPostedOrRemoved((CallBackInfo) message.obj, message.what);
            } else if (message.obj instanceof NotificationListenerService.RankingMap) {
                AdapterListenerService.this.handleNotificationUpdate((NotificationListenerService.RankingMap) message.obj, message.what);
            } else if (message.obj instanceof NotificationSubscriberHost) {
                AdapterListenerService.this.handleSubscriberConnectedOrDisconnected((NotificationSubscriberHost) message.obj, message.what);
            } else {
                AdapterListenerService.this.callbackMethods(Integer.valueOf(message.arg1), message.what);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class CallBackInfo {
        private NotificationListenerService.RankingMap rankingMap;
        private int reason;
        private StatusBarNotification sbn;

        CallBackInfo(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap2) {
            this(statusBarNotification, rankingMap2, 0);
        }

        CallBackInfo(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap2, int i) {
            this.sbn = statusBarNotification;
            this.rankingMap = rankingMap2;
            this.reason = i;
        }
    }
}

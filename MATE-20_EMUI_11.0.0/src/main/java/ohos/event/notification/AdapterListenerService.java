package ohos.event.notification;

import android.app.ActivityThread;
import android.app.Application;
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
    private static final int HASHCODE_MAX_LENGTH = 7;
    private static final int INDEX_ID = 0;
    private static final int INDEX_IDENTIFIER = 4;
    private static final int INDEX_OVERRIDEGROUPKEY = 6;
    private static final int INDEX_PACKAGE_NAME = 1;
    private static final int INDEX_PKG_NAME = 1;
    private static final int INDEX_TAG = 5;
    private static final int INDEX_UID = 2;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.NOTIFICATION_DOMAIN, TAG);
    private static final String LISTENER_THREAD_NAME = "notification_listener";
    private static final int MIN_LENGTH = 2;
    private static final int MSG_NOTIFICATION_CANCELED = 2;
    private static final int MSG_NOTIFICATION_POSTED = 1;
    private static final int MSG_NOTIFICATION_UPDATE = 5;
    private static final int MSG_SUBSCRIBER_CONNECTED = 3;
    private static final int MSG_SUBSCRIBER_DISCONNECTED = 4;
    private static final String SPLIT_CHAR = "|";
    private static final String TAG = "AdapterListenerService";
    private static final String Z_DELIMITER = "_";
    private static final int Z_HASHCODE_LENGTH = 6;
    private static Method registerMethod;
    private final Object LOCK = new Object();
    private Context aospContext = null;
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
    public StatusBarNotification[] getActiveNotifications() {
        init();
        return this.isInitial ? super.getActiveNotifications() : new StatusBarNotification[0];
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
        String currentPkg2 = getCurrentPkg(getAospContext());
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
        String[] split = str.split("_");
        if (split.length < 6) {
            HiLog.error(LABEL, "Hash code in illegal.", new Object[0]);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(split[4]);
        sb.append(SPLIT_CHAR);
        sb.append(split[1]);
        sb.append(SPLIT_CHAR);
        sb.append(split[0]);
        sb.append(SPLIT_CHAR);
        sb.append(split[5]);
        sb.append(SPLIT_CHAR);
        sb.append(split[2]);
        if (split.length == 7) {
            sb.append(SPLIT_CHAR);
            sb.append(split[6]);
        }
        cancelNotification(sb.toString());
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
        Context aospContext2;
        String currentPkg2;
        if (registerMethod == null || (aospContext2 = getAospContext()) == null || (currentPkg2 = getCurrentPkg(aospContext2)) == null) {
            return false;
        }
        try {
            ComponentName componentName = new ComponentName(currentPkg2, getClass().getCanonicalName());
            registerMethod.setAccessible(true);
            registerMethod.invoke(this, aospContext2, componentName, -1);
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
    private void handleNotificationPostedOrRemoved(CallBackInfo callBackInfo, boolean z) {
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
        callPostedOrRemoved(matchedSubscriber, convertRankingMapToSorttingMap.orElse(null), transformToNotificationRequest.get(), getReason(callBackInfo.reason), z);
    }

    private void callPostedOrRemoved(Set<NotificationSubscriberHost> set, NotificationSortingMap notificationSortingMap, NotificationRequest notificationRequest, int i, boolean z) {
        for (NotificationSubscriberHost notificationSubscriberHost : set) {
            if (notificationSubscriberHost != null) {
                NotificationSortingMap subscriberSortingMap = getSubscriberSortingMap(notificationSubscriberHost, notificationSortingMap);
                if (z) {
                    try {
                        notificationSubscriberHost.onNotificationPosted(notificationRequest);
                        notificationSubscriberHost.onNotificationPosted(notificationRequest, subscriberSortingMap);
                    } catch (RemoteException unused) {
                        HiLog.warn(LABEL, "handleNotificationPostedOrRemoved callback failed", new Object[0]);
                    }
                } else {
                    notificationSubscriberHost.onNotificationRemoved(notificationRequest);
                    notificationSubscriberHost.onNotificationRemoved(notificationRequest, subscriberSortingMap, i);
                }
            }
        }
    }

    private NotificationSortingMap getSubscriberSortingMap(NotificationSubscriberHost notificationSubscriberHost, NotificationSortingMap notificationSortingMap) {
        List<String> hashCode;
        if (notificationSortingMap == null || (hashCode = notificationSortingMap.getHashCode()) == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (String str : hashCode) {
            if (this.managedSubscriber.checkSubscriberAppNames(notificationSubscriberHost, str)) {
                NotificationSorting notificationSorting = new NotificationSorting();
                if (notificationSortingMap.getNotificationSorting(str, notificationSorting)) {
                    arrayList.add(notificationSorting);
                }
            }
        }
        return new NotificationSortingMap(arrayList);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotificationUpdate(NotificationListenerService.RankingMap rankingMap) {
        Optional<Set<NotificationSubscriberHost>> allSubscribers = this.managedSubscriber.getAllSubscribers();
        if (!allSubscribers.isPresent()) {
            HiLog.warn(LABEL, "AdapterListenerService::handleNotificationUpdate get subscribers failed.", new Object[0]);
            return;
        }
        Optional<NotificationSortingMap> convertRankingMapToSorttingMap = NotificationSortingConvert.convertRankingMapToSorttingMap(rankingMap);
        if (!convertRankingMapToSorttingMap.isPresent()) {
            HiLog.warn(LABEL, "AdapterListenerService::get sortingMap failed.", new Object[0]);
            return;
        }
        for (NotificationSubscriberHost notificationSubscriberHost : allSubscribers.get()) {
            if (notificationSubscriberHost != null) {
                try {
                    notificationSubscriberHost.onNotificationRankingUpdate(convertRankingMapToSorttingMap.get());
                } catch (RemoteException unused) {
                    HiLog.warn(LABEL, "AdapterListenerService::handleNotificationUpdate update exception", new Object[0]);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSubscriberConnectedOrDisconnected(NotificationSubscriberHost notificationSubscriberHost, boolean z) {
        if (notificationSubscriberHost != null) {
            if (z) {
                try {
                    notificationSubscriberHost.onSubscribeConnected();
                } catch (RemoteException unused) {
                    HiLog.warn(LABEL, "AdapterListenerService::handleSubscriberConnectedOrDisconnected exception", new Object[0]);
                }
            } else {
                notificationSubscriberHost.onSubscribeDisConnected();
            }
        }
    }

    private Context getAospContext() {
        Application currentApplication;
        if (this.aospContext == null && (currentApplication = ActivityThread.currentApplication()) != null) {
            this.aospContext = currentApplication.getApplicationContext();
        }
        return this.aospContext;
    }

    private String getCurrentPkg(Context context) {
        if (this.currentPkg == null && context != null) {
            this.currentPkg = context.getPackageName();
        }
        return this.currentPkg;
    }

    /* access modifiers changed from: private */
    public final class ListenerHandler extends Handler {
        public ListenerHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message != null && message.obj != null) {
                int i = message.what;
                if (i != 1) {
                    if (i != 2) {
                        if (i != 3) {
                            if (i != 4) {
                                if (i == 5 && (message.obj instanceof NotificationListenerService.RankingMap)) {
                                    AdapterListenerService.this.handleNotificationUpdate((NotificationListenerService.RankingMap) message.obj);
                                }
                            } else if (message.obj instanceof NotificationSubscriberHost) {
                                AdapterListenerService.this.handleSubscriberConnectedOrDisconnected((NotificationSubscriberHost) message.obj, false);
                            }
                        } else if (message.obj instanceof NotificationSubscriberHost) {
                            AdapterListenerService.this.handleSubscriberConnectedOrDisconnected((NotificationSubscriberHost) message.obj, true);
                        }
                    } else if (message.obj instanceof CallBackInfo) {
                        AdapterListenerService.this.handleNotificationPostedOrRemoved((CallBackInfo) message.obj, false);
                    }
                } else if (message.obj instanceof CallBackInfo) {
                    AdapterListenerService.this.handleNotificationPostedOrRemoved((CallBackInfo) message.obj, true);
                }
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

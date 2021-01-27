package ohos.event.notification;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ManagedSubscriber {
    private final Object LOCK = new Object();
    private AdapterListenerService adapterService;
    private Map<NotificationSubscriberHost, SubscriberInfo> subscriberMap = new ConcurrentHashMap();

    /* access modifiers changed from: private */
    public static class SubscriberInfo {
        boolean subscribedAll;
        Set<String> subscribedApp;
        Set<String> unsubscribedApp;

        private SubscriberInfo() {
            this.subscribedAll = false;
            this.subscribedApp = new HashSet();
            this.unsubscribedApp = new HashSet();
        }
    }

    public ManagedSubscriber(AdapterListenerService adapterListenerService) {
        this.adapterService = adapterListenerService;
    }

    /* access modifiers changed from: package-private */
    public void addSubscriber(NotificationSubscriberHost notificationSubscriberHost, Set<String> set) {
        if (notificationSubscriberHost != null) {
            synchronized (this.LOCK) {
                SubscriberInfo subscriberInfo = this.subscriberMap.get(notificationSubscriberHost);
                if (subscriberInfo == null) {
                    if (set == null) {
                        addAllAppToSubscriber(notificationSubscriberHost);
                    } else {
                        addAppToSubscriber(notificationSubscriberHost, set);
                    }
                    if (this.adapterService != null) {
                        this.adapterService.connectSubscriber(notificationSubscriberHost);
                    }
                } else if (set == null) {
                    addAllAppToSubscriber(notificationSubscriberHost);
                } else {
                    if (subscriberInfo.subscribedAll) {
                        subscriberInfo.unsubscribedApp.removeAll(set);
                    } else {
                        subscriberInfo.subscribedApp.addAll(set);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeSubscriber(NotificationSubscriberHost notificationSubscriberHost, Set<String> set) {
        if (notificationSubscriberHost != null) {
            synchronized (this.LOCK) {
                SubscriberInfo subscriberInfo = this.subscriberMap.get(notificationSubscriberHost);
                if (subscriberInfo != null) {
                    if (set == null) {
                        this.subscriberMap.remove(notificationSubscriberHost);
                        if (this.adapterService != null) {
                            this.adapterService.disconnectSubscriber(notificationSubscriberHost);
                        }
                        return;
                    }
                    if (subscriberInfo.subscribedAll) {
                        subscriberInfo.unsubscribedApp.addAll(set);
                        this.subscriberMap.put(notificationSubscriberHost, subscriberInfo);
                    } else {
                        subscriberInfo.subscribedApp.removeAll(set);
                        this.subscriberMap.put(notificationSubscriberHost, subscriberInfo);
                        if (subscriberInfo.subscribedApp.isEmpty()) {
                            this.subscriberMap.remove(notificationSubscriberHost);
                            if (this.adapterService != null) {
                                this.adapterService.disconnectSubscriber(notificationSubscriberHost);
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void addSubscriber(NotificationSubscriberHost notificationSubscriberHost, NotificationSubscribeInfo notificationSubscribeInfo) {
        addSubscriber(notificationSubscriberHost, notificationSubscribeInfo != null ? notificationSubscribeInfo.getAppNames() : null);
    }

    /* access modifiers changed from: package-private */
    public void removeSubscriber(NotificationSubscriberHost notificationSubscriberHost, NotificationSubscribeInfo notificationSubscribeInfo) {
        removeSubscriber(notificationSubscriberHost, notificationSubscribeInfo != null ? notificationSubscribeInfo.getAppNames() : null);
    }

    /* access modifiers changed from: package-private */
    public Set<NotificationSubscriberHost> getMatchedSubscriber(String str) {
        if (str == null || str.isEmpty()) {
            return new HashSet();
        }
        HashSet hashSet = new HashSet();
        synchronized (this.LOCK) {
            for (Map.Entry<NotificationSubscriberHost, SubscriberInfo> entry : this.subscriberMap.entrySet()) {
                if (isSubscribed(str, entry.getValue())) {
                    hashSet.add(entry.getKey());
                }
            }
        }
        return hashSet;
    }

    /* access modifiers changed from: package-private */
    public boolean checkSubscriberAppNames(NotificationSubscriberHost notificationSubscriberHost, String str) {
        if (notificationSubscriberHost == null) {
            return false;
        }
        synchronized (this.LOCK) {
            SubscriberInfo subscriberInfo = this.subscriberMap.get(notificationSubscriberHost);
            if (subscriberInfo == null) {
                return false;
            }
            return isSubscribed(str, subscriberInfo);
        }
    }

    /* access modifiers changed from: package-private */
    public Optional<Set<NotificationSubscriberHost>> getAllSubscribers() {
        Optional<Set<NotificationSubscriberHost>> ofNullable;
        synchronized (this.LOCK) {
            ofNullable = Optional.ofNullable(this.subscriberMap.keySet());
        }
        return ofNullable;
    }

    private void addAllAppToSubscriber(NotificationSubscriberHost notificationSubscriberHost) {
        SubscriberInfo subscriberInfo = new SubscriberInfo();
        subscriberInfo.subscribedAll = true;
        this.subscriberMap.put(notificationSubscriberHost, subscriberInfo);
    }

    private void addAppToSubscriber(NotificationSubscriberHost notificationSubscriberHost, Set<String> set) {
        SubscriberInfo subscriberInfo = new SubscriberInfo();
        subscriberInfo.subscribedApp = set;
        this.subscriberMap.put(notificationSubscriberHost, subscriberInfo);
    }

    private boolean isSubscribed(String str, SubscriberInfo subscriberInfo) {
        if (subscriberInfo == null || str == null) {
            return false;
        }
        if (subscriberInfo.subscribedAll) {
            return !subscriberInfo.unsubscribedApp.contains(str);
        }
        return subscriberInfo.subscribedApp.contains(str);
    }
}

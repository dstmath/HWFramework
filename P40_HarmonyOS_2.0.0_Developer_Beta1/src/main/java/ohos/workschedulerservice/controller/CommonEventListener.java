package ohos.workschedulerservice.controller;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import ohos.aafwk.content.Intent;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.event.commonevent.CommonEventSubscriber;
import ohos.event.commonevent.MatchingSkills;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;
import ohos.utils.LightweightMap;
import ohos.utils.LightweightSet;
import ohos.workschedulerservice.WorkQueueManager;

public final class CommonEventListener {
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, "CommonEventListener");
    private final Object lock = new Object();
    private final LightweightMap<String, ArrayList<SubscriberStatus>> subscriberMap = new LightweightMap<>();
    private final LightweightMap<SubscriberStatus, ArrayList<CommonEventStatus>> uidSubscriberMap = new LightweightMap<>();
    private WorkQueueManager workQueueMgr;

    /* access modifiers changed from: private */
    public enum ListType {
        DATA,
        TYPE
    }

    public CommonEventListener(WorkQueueManager workQueueManager) {
        this.workQueueMgr = workQueueManager;
    }

    public void registCommonEvent(CommonEventStatus commonEventStatus) {
        if (commonEventStatus != null) {
            synchronized (this.lock) {
                SubscriberStatus subscriberStatus = null;
                String eventName = commonEventStatus.getEventName();
                HiLog.debug(LOG_LABEL, "regist %{public}s", eventName);
                ArrayList<SubscriberStatus> arrayList = this.subscriberMap.get(eventName);
                if (arrayList != null) {
                    Iterator<SubscriberStatus> it = arrayList.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        SubscriberStatus next = it.next();
                        if (next.checkSameList(commonEventStatus.getEventData(), ListType.DATA) && next.checkSameList(commonEventStatus.getEventType(), ListType.TYPE) && next.checkSamePermission(commonEventStatus.getEventPermission())) {
                            HiLog.debug(LOG_LABEL, "regist find a subscriber!", new Object[0]);
                            subscriberStatus = next;
                            break;
                        }
                    }
                } else {
                    HiLog.debug(LOG_LABEL, "regist subscriberList is null, new here!", new Object[0]);
                    arrayList = new ArrayList<>();
                    this.subscriberMap.put(eventName, arrayList);
                }
                if (subscriberStatus == null) {
                    HiLog.debug(LOG_LABEL, "regist subscriber is null, new here!", new Object[0]);
                    subscriberStatus = new SubscriberStatus();
                    if (subscriberStatus.setSubscriber(commonEventStatus) == null) {
                        HiLog.error(LOG_LABEL, "setSubscriber fail, Subscriber is null!", new Object[0]);
                        return;
                    }
                    subscriberStatus.setDataList(commonEventStatus.getEventData());
                    subscriberStatus.setTypeList(commonEventStatus.getEventType());
                    subscriberStatus.setPermission(commonEventStatus.getEventPermission());
                    arrayList.add(subscriberStatus);
                }
                ArrayList<CommonEventStatus> arrayList2 = this.uidSubscriberMap.get(subscriberStatus);
                if (arrayList2 == null) {
                    HiLog.debug(LOG_LABEL, "regist eventStatusList is null, new here!", new Object[0]);
                    arrayList2 = new ArrayList<>();
                    this.uidSubscriberMap.put(subscriberStatus, arrayList2);
                }
                arrayList2.add(commonEventStatus);
                HiLog.debug(LOG_LABEL, "regist %{public}d - %{public}d!", Integer.valueOf(this.uidSubscriberMap.size()), Integer.valueOf(arrayList2.size()));
            }
        }
    }

    public void unRegistCommonEvent(CommonEventStatus commonEventStatus) {
        if (commonEventStatus != null) {
            synchronized (this.lock) {
                HiLog.debug(LOG_LABEL, "%{public}s unRegist %{public}s", commonEventStatus.getBundleName(), commonEventStatus.getEventName());
                ArrayList<SubscriberStatus> arrayList = this.subscriberMap.get(commonEventStatus.getEventName());
                if (arrayList == null) {
                    HiLog.debug(LOG_LABEL, "unRegist fail, no event has regist here!", new Object[0]);
                    return;
                }
                Iterator<SubscriberStatus> it = arrayList.iterator();
                while (it.hasNext()) {
                    SubscriberStatus next = it.next();
                    ArrayList<CommonEventStatus> arrayList2 = this.uidSubscriberMap.get(next);
                    if (arrayList2 == null) {
                        HiLog.debug(LOG_LABEL, "unRegist fail, no uid has regist here!", new Object[0]);
                        return;
                    }
                    HiLog.debug(LOG_LABEL, "unRegist before remove size is %{public}d!", Integer.valueOf(arrayList2.size()));
                    arrayList2.removeIf(new Predicate() {
                        /* class ohos.workschedulerservice.controller.$$Lambda$CommonEventListener$3xKL71VZHgsQBUEn640VIQgFdvw */

                        @Override // java.util.function.Predicate
                        public final boolean test(Object obj) {
                            return ((CommonEventStatus) obj).isSameStatus(CommonEventStatus.this);
                        }
                    });
                    HiLog.debug(LOG_LABEL, "unRegist after remove size is %{public}d!", Integer.valueOf(arrayList2.size()));
                    if (arrayList2.size() == 0) {
                        if (next.getSubscriber() != null && next.getRegistStatus()) {
                            try {
                                CommonEventManager.unsubscribeCommonEvent(next.getSubscriber());
                                next.setRegistStatus(false);
                                HiLog.debug(LOG_LABEL, "unsubscribeCommonEvent success", new Object[0]);
                            } catch (RemoteException unused) {
                                HiLog.error(LOG_LABEL, "unsubscribeCommonEvent occur exception", new Object[0]);
                            }
                        }
                        this.uidSubscriberMap.remove(next);
                        next.setNeedRemove(true);
                        HiLog.debug(LOG_LABEL, "unRegist remove all subscriber for this event done!", new Object[0]);
                    }
                }
                HiLog.debug(LOG_LABEL, "subscriberList before remove size is %{public}d!", Integer.valueOf(arrayList.size()));
                arrayList.removeIf($$Lambda$CommonEventListener$JWx1PDMibSw7aMOy_KjzgvMKk.INSTANCE);
                HiLog.debug(LOG_LABEL, "subscriberList after remove size is %{public}d!", Integer.valueOf(arrayList.size()));
                if (arrayList.size() == 0) {
                    this.subscriberMap.remove(commonEventStatus.getEventName());
                }
            }
        }
    }

    public boolean updateCommonEventListener() {
        synchronized (this.lock) {
            for (SubscriberStatus subscriberStatus : this.uidSubscriberMap.keySet()) {
                if (subscriberStatus.getSubscriber() == null || subscriberStatus.getRegistStatus()) {
                    HiLog.error(LOG_LABEL, "updateCommonEventListener subscriber is null, no need not regist!", new Object[0]);
                } else {
                    try {
                        CommonEventManager.subscribeCommonEvent(subscriberStatus.getSubscriber());
                        subscriberStatus.setRegistStatus(true);
                    } catch (RemoteException unused) {
                        HiLog.error(LOG_LABEL, "subscribeCommonEvent occur exception.", new Object[0]);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void dumpStateListenerStatus(PrintWriter printWriter) {
        if (printWriter != null) {
            printWriter.println();
            printWriter.println("CommonEventListener latest event:");
        }
    }

    /* access modifiers changed from: private */
    public final class StaticCommonEventSubscriber extends CommonEventSubscriber {
        StaticCommonEventSubscriber(CommonEventSubscribeInfo commonEventSubscribeInfo) {
            super(commonEventSubscribeInfo);
        }

        public void onReceiveEvent(CommonEventData commonEventData) {
            if (commonEventData == null || CommonEventListener.this.workQueueMgr == null) {
                HiLog.error(CommonEventListener.LOG_LABEL, "CommonEventListener onReceiveEvent, event data invalid", new Object[0]);
                return;
            }
            Intent intent = commonEventData.getIntent();
            if (intent == null) {
                HiLog.error(CommonEventListener.LOG_LABEL, "CommonEventListener onReceiveEvent, intent invalid", new Object[0]);
                return;
            }
            HiLog.debug(CommonEventListener.LOG_LABEL, "onReceiveEvent %{public}s!", intent.getAction());
            synchronized (CommonEventListener.this.lock) {
                ArrayList<CommonEventStatus> arrayList = null;
                Iterator it = CommonEventListener.this.uidSubscriberMap.keySet().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    SubscriberStatus subscriberStatus = (SubscriberStatus) it.next();
                    if (equals(subscriberStatus.getSubscriber())) {
                        HiLog.debug(CommonEventListener.LOG_LABEL, "onReceiveEvent %{public}d find Subscriber here!", Integer.valueOf(CommonEventListener.this.uidSubscriberMap.indexOfKey(subscriberStatus)));
                        arrayList = (ArrayList) CommonEventListener.this.uidSubscriberMap.get(subscriberStatus);
                        break;
                    }
                }
                if (arrayList == null) {
                    HiLog.error(CommonEventListener.LOG_LABEL, "CommonEventListener statusList is null, start fail!", new Object[0]);
                } else {
                    CommonEventListener.this.workQueueMgr.onCommonEventChanged(intent, arrayList);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public final class SubscriberStatus {
        private LightweightSet<String> dataList;
        private boolean isRegist = false;
        private boolean needRemove = false;
        private String permission;
        private StaticCommonEventSubscriber subscriber;
        private LightweightSet<String> typeList;

        SubscriberStatus() {
        }

        public StaticCommonEventSubscriber setSubscriber(CommonEventStatus commonEventStatus) {
            if (commonEventStatus == null) {
                return this.subscriber;
            }
            String eventName = commonEventStatus.getEventName();
            MatchingSkills matchingSkills = new MatchingSkills();
            matchingSkills.addEvent(eventName);
            for (String str : commonEventStatus.getEventData()) {
                if (!matchingSkills.hasScheme(str)) {
                    matchingSkills.addScheme(str);
                }
            }
            for (String str2 : commonEventStatus.getEventType()) {
                if (!matchingSkills.hasType(str2)) {
                    matchingSkills.addType(str2);
                }
            }
            CommonEventSubscribeInfo commonEventSubscribeInfo = new CommonEventSubscribeInfo(matchingSkills);
            if (commonEventStatus.getEventPermission() != null && !"".equals(commonEventStatus.getEventPermission())) {
                commonEventSubscribeInfo.setPermission(commonEventStatus.getEventPermission());
            }
            this.subscriber = new StaticCommonEventSubscriber(commonEventSubscribeInfo);
            return this.subscriber;
        }

        public StaticCommonEventSubscriber getSubscriber() {
            return this.subscriber;
        }

        public boolean getNeedRemove() {
            return this.needRemove;
        }

        public void setNeedRemove(boolean z) {
            this.needRemove = z;
        }

        public boolean getRegistStatus() {
            return this.isRegist;
        }

        public void setRegistStatus(boolean z) {
            this.isRegist = z;
        }

        public void setPermission(String str) {
            this.permission = str;
        }

        public void setDataList(List<String> list) {
            if (list != null && this.dataList == null) {
                this.dataList = new LightweightSet<>();
                this.dataList.addAll(list);
            }
        }

        public void setTypeList(List<String> list) {
            if (list != null && this.typeList == null) {
                this.typeList = new LightweightSet<>();
                this.typeList.addAll(list);
            }
        }

        public boolean checkSameList(List<String> list, ListType listType) {
            LightweightSet<String> lightweightSet;
            if (listType == ListType.DATA) {
                lightweightSet = this.dataList;
            } else {
                lightweightSet = this.typeList;
            }
            if (lightweightSet == null || list == null) {
                return lightweightSet == null && list == null;
            }
            if (list.size() > lightweightSet.size()) {
                return false;
            }
            for (String str : list) {
                if (!lightweightSet.contains(str)) {
                    return false;
                }
            }
            return true;
        }

        public boolean checkSamePermission(String str) {
            String str2 = this.permission;
            return (str2 == null || str == null || !str2.equals(str)) ? false : true;
        }

        public boolean isNeedRemove() {
            return this.needRemove;
        }
    }
}

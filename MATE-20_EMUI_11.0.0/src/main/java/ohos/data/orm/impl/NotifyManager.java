package ohos.data.orm.impl;

import java.lang.Thread;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import ohos.data.orm.AllChangeToTarget;
import ohos.data.orm.ObjectId;
import ohos.data.orm.OrmContext;
import ohos.data.orm.OrmObject;
import ohos.data.orm.OrmObjectObserver;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

/* access modifiers changed from: package-private */
public class NotifyManager {
    static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "NotifyManager");
    private final Map<OrmContext, NotifyClient> contextToClient = new ConcurrentHashMap();
    private final HandleMessageThread messageThread = new HandleMessageThread();
    private final Queue<MessageNode> messagesQueue = new LinkedList();

    public NotifyManager() {
        this.messageThread.setDaemon(true);
        this.messageThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            /* class ohos.data.orm.impl.NotifyManager.AnonymousClass1 */

            @Override // java.lang.Thread.UncaughtExceptionHandler
            public void uncaughtException(Thread thread, Throwable th) {
                HiLog.error(NotifyManager.LABEL, "An exception %{private}s %{private}s happened in thread %{private}s", new Object[]{th.getClass().getName(), th.getMessage(), thread.getName()});
            }
        });
        this.messageThread.start();
    }

    public List<OrmContext> hasListeners(String str) {
        ArrayList arrayList = new ArrayList();
        for (Map.Entry<OrmContext, NotifyClient> entry : this.contextToClient.entrySet()) {
            OrmContext key = entry.getKey();
            NotifyClient value = entry.getValue();
            if (value.getAlias().equals(str) && value.hasObserver()) {
                arrayList.add(key);
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMessageToContext(AllChangeToThread allChangeToThread, List<OrmContext> list) {
        if (allChangeToThread != null) {
            Map<String, AllChangeToTarget> psMap = allChangeToThread.getPsMap();
            Map<OrmContext, AllChangeToTarget> ormContextMap = allChangeToThread.getOrmContextMap();
            Map<String, AllChangeToTarget> entityMap = allChangeToThread.getEntityMap();
            Map<ObjectId, AllChangeToTarget> manageObjMap = allChangeToThread.getManageObjMap();
            OrmContext changeContext = allChangeToThread.getChangeContext();
            for (OrmContext ormContext : list) {
                AllChangeToThread allChangeToThread2 = new AllChangeToThread(changeContext);
                NotifyClient notifyClient = this.contextToClient.get(ormContext);
                boolean z = false;
                for (String str : psMap.keySet()) {
                    if (notifyClient.hasPsObserver(str)) {
                        allChangeToThread2.getPsMap().put(str, psMap.get(str));
                        z = true;
                    }
                }
                for (OrmContext ormContext2 : ormContextMap.keySet()) {
                    if (notifyClient.hasOrmContextObserver(ormContext2)) {
                        allChangeToThread2.getOrmContextMap().put(ormContext2, ormContextMap.get(ormContext2));
                        z = true;
                    }
                }
                for (String str2 : entityMap.keySet()) {
                    if (notifyClient.hasEntityObserver(str2)) {
                        allChangeToThread2.getEntityMap().put(str2, entityMap.get(str2));
                        z = true;
                    }
                }
                for (ObjectId objectId : manageObjMap.keySet()) {
                    if (notifyClient.hasOrmObjObserver(objectId)) {
                        allChangeToThread2.getManageObjMap().put(objectId, manageObjMap.get(objectId));
                        z = true;
                    }
                }
                if (z) {
                    notifyClient.sendChangedMessage(allChangeToThread2);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void addMessageToQueue(SaveRequest saveRequest, OrmContext ormContext, String str) {
        List<OrmContext> hasListeners = hasListeners(str);
        synchronized (this.messagesQueue) {
            this.messagesQueue.add(new MessageNode(saveRequest.getInsertedObjects(), saveRequest.getUpdatedObjects(), saveRequest.getDeletedObjects(), ormContext, str, hasListeners));
            this.messagesQueue.notifyAll();
        }
    }

    /* access modifiers changed from: package-private */
    public void registerStoreListener(String str, OrmObjectObserver ormObjectObserver, OrmContext ormContext) {
        NotifyClient notifyClient = this.contextToClient.get(ormContext);
        if (notifyClient == null) {
            notifyClient = new NotifyClient(ormContext);
            this.contextToClient.put(ormContext, notifyClient);
        }
        notifyClient.registerStoreObserver(str, ormObjectObserver);
    }

    /* access modifiers changed from: package-private */
    public void registerContextListener(OrmContext ormContext, OrmObjectObserver ormObjectObserver, OrmContext ormContext2) {
        NotifyClient notifyClient = this.contextToClient.get(ormContext2);
        if (notifyClient == null) {
            notifyClient = new NotifyClient(ormContext2);
            this.contextToClient.put(ormContext2, notifyClient);
        }
        notifyClient.registerContextObserver(ormContext, ormObjectObserver);
    }

    /* access modifiers changed from: package-private */
    public void registerEntityListener(String str, OrmObjectObserver ormObjectObserver, OrmContext ormContext) {
        NotifyClient notifyClient = this.contextToClient.get(ormContext);
        if (notifyClient == null) {
            notifyClient = new NotifyClient(ormContext);
            this.contextToClient.put(ormContext, notifyClient);
        }
        notifyClient.registerEntityObserver(str, ormObjectObserver);
    }

    /* access modifiers changed from: package-private */
    public void registerObjcetListener(OrmObject ormObject, OrmObjectObserver ormObjectObserver, OrmContext ormContext) {
        NotifyClient notifyClient = this.contextToClient.get(ormContext);
        if (notifyClient == null) {
            notifyClient = new NotifyClient(ormContext);
            this.contextToClient.put(ormContext, notifyClient);
        }
        notifyClient.registerOrmObjectObserver(ormObject, ormObjectObserver);
    }

    /* access modifiers changed from: package-private */
    public void unregisterStoreListener(String str, OrmObjectObserver ormObjectObserver, OrmContext ormContext) {
        NotifyClient notifyClient = this.contextToClient.get(ormContext);
        if (notifyClient == null) {
            notifyClient = new NotifyClient(ormContext);
            this.contextToClient.put(ormContext, notifyClient);
        }
        notifyClient.unregisterStoreObserver(str, ormObjectObserver);
    }

    /* access modifiers changed from: package-private */
    public void unregisterContextListener(OrmContext ormContext, OrmObjectObserver ormObjectObserver, OrmContext ormContext2) {
        NotifyClient notifyClient = this.contextToClient.get(ormContext2);
        if (notifyClient == null) {
            notifyClient = new NotifyClient(ormContext2);
            this.contextToClient.put(ormContext2, notifyClient);
        }
        notifyClient.unregisterContextObserver(ormContext, ormObjectObserver);
    }

    /* access modifiers changed from: package-private */
    public void unregisterEntityListener(String str, OrmObjectObserver ormObjectObserver, OrmContext ormContext) {
        NotifyClient notifyClient = this.contextToClient.get(ormContext);
        if (notifyClient == null) {
            notifyClient = new NotifyClient(ormContext);
            this.contextToClient.put(ormContext, notifyClient);
        }
        notifyClient.unregisterEntityObserver(str, ormObjectObserver);
    }

    /* access modifiers changed from: package-private */
    public void unregisterObjectListener(OrmObject ormObject, OrmObjectObserver ormObjectObserver, OrmContext ormContext) {
        NotifyClient notifyClient = this.contextToClient.get(ormContext);
        if (notifyClient == null) {
            notifyClient = new NotifyClient(ormContext);
            this.contextToClient.put(ormContext, notifyClient);
        }
        notifyClient.unregisterObjectObserver(ormObject, ormObjectObserver);
    }

    private class HandleMessageThread extends Thread {
        private HandleMessageThread() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            MessageNode messageNode;
            while (true) {
                synchronized (NotifyManager.this.messagesQueue) {
                    while (NotifyManager.this.messagesQueue.isEmpty()) {
                        try {
                            NotifyManager.this.messagesQueue.wait();
                        } catch (InterruptedException unused) {
                            HiLog.error(NotifyManager.LABEL, "The message handle thread interrupted by another thread.", new Object[0]);
                        }
                    }
                    messageNode = (MessageNode) NotifyManager.this.messagesQueue.poll();
                }
                if (messageNode != null) {
                    NotifyManager.this.sendMessageToContext(new AllChangeToThread(messageNode.getInsertedObjects(), messageNode.getUpdatedObjects(), messageNode.getDeletedObjects(), messageNode.getChangeContext()), messageNode.getNotifyTargets());
                    messageNode.releaseReferences();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class MessageNode {
        private String alias;
        private OrmContext changeContext;
        private List<OrmObject> deletedObjects;
        private List<OrmObject> insertedObjects;
        private List<OrmContext> notifyTargets;
        private List<OrmObject> updatedObjects;

        MessageNode(List<OrmObject> list, List<OrmObject> list2, List<OrmObject> list3, OrmContext ormContext, String str, List<OrmContext> list4) {
            this.insertedObjects = new ArrayList(list);
            this.updatedObjects = new ArrayList(list2);
            this.deletedObjects = new ArrayList(list3);
            this.changeContext = ormContext;
            this.alias = str;
            this.notifyTargets = list4;
        }

        public List<OrmContext> getNotifyTargets() {
            return this.notifyTargets;
        }

        public List<OrmObject> getInsertedObjects() {
            return this.insertedObjects;
        }

        public List<OrmObject> getUpdatedObjects() {
            return this.updatedObjects;
        }

        public List<OrmObject> getDeletedObjects() {
            return this.deletedObjects;
        }

        public OrmContext getChangeContext() {
            return this.changeContext;
        }

        public String getAlias() {
            return this.alias;
        }

        public void releaseReferences() {
            this.notifyTargets.clear();
            this.notifyTargets = null;
            this.insertedObjects.clear();
            this.insertedObjects = null;
            this.updatedObjects.clear();
            this.updatedObjects = null;
            this.deletedObjects.clear();
            this.deletedObjects = null;
            this.changeContext = null;
            this.alias = null;
        }
    }
}

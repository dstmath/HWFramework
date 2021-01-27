package com.huawei.odmf.core;

import android.util.ArrayMap;
import com.huawei.odmf.predicate.SaveRequest;
import com.huawei.odmf.user.api.AllChangeToTarget;
import com.huawei.odmf.user.api.ObjectContext;
import com.huawei.odmf.utils.LOG;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/* access modifiers changed from: package-private */
public class NotifyManager {
    private HandleMessageThread messageThread = new HandleMessageThread();
    private final Queue<MessageNode> messagesQueue = new LinkedList();

    public NotifyManager() {
        this.messageThread.start();
    }

    public List<ObjectContext> hasListeners(String str) {
        ArrayList arrayList = new ArrayList();
        for (Map.Entry<ObjectContext, PersistentStore> entry : PersistentStoreCoordinator.getDefault().getContextToStore().entrySet()) {
            AObjectContext aObjectContext = (AObjectContext) entry.getKey();
            NotifyClient notifyClient = aObjectContext.getNotifyClient();
            if (entry.getValue().getUriString().equals(str) && (notifyClient.hasPsListener() || notifyClient.hasObjectContextListener() || notifyClient.hasEntityListener() || notifyClient.hasManageObjListener())) {
                arrayList.add(aObjectContext);
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMessageToObjectContext(ObjectContext objectContext, String str, String str2, List<ObjectContext> list) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            NotifyClient notifyClient = ((AObjectContext) list.get(i)).getNotifyClient();
            if (notifyClient.hasEntityListener(str) || notifyClient.hasPsListener()) {
                notifyClient.sendMessage(2, new AllChangeToThread(objectContext, str, str2));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void sendMessageToObjectContext(AllChangeToThread allChangeToThread, List<ObjectContext> list) {
        ArrayMap<String, AllChangeToTarget> arrayMap;
        if (allChangeToThread == null) {
            LOG.logI("allChange is empty,do nothing");
            return;
        }
        ArrayMap<String, AllChangeToTarget> psMap = allChangeToThread.getPsMap();
        ArrayMap<ObjectContext, AllChangeToTarget> objContextMap = allChangeToThread.getObjContextMap();
        ArrayMap<String, AllChangeToTarget> entityMap = allChangeToThread.getEntityMap();
        ArrayMap<ObjectId, AllChangeToTarget> manageObjMap = allChangeToThread.getManageObjMap();
        ObjectContext changeContext = allChangeToThread.getChangeContext();
        int size = list.size();
        int i = 0;
        while (i < size) {
            AllChangeToThread allChangeToThread2 = new AllChangeToThread(changeContext);
            NotifyClient notifyClient = ((AObjectContext) list.get(i)).getNotifyClient();
            boolean z = false;
            for (String str : psMap.keySet()) {
                if (notifyClient.hasPsListener(str)) {
                    allChangeToThread2.getPsMap().put(str, psMap.get(str));
                    z = true;
                }
            }
            boolean z2 = false;
            for (ObjectContext objectContext : objContextMap.keySet()) {
                if (notifyClient.hasObjectContextListener(objectContext)) {
                    allChangeToThread2.getObjContextMap().put(objectContext, objContextMap.get(objectContext));
                    z2 = true;
                }
            }
            boolean z3 = false;
            for (String str2 : entityMap.keySet()) {
                if (notifyClient.hasEntityListener(str2)) {
                    allChangeToThread2.getEntityMap().put(str2, entityMap.get(str2));
                    z3 = true;
                }
            }
            boolean z4 = false;
            for (ObjectId objectId : manageObjMap.keySet()) {
                if (notifyClient.hasManageObjListener(objectId)) {
                    arrayMap = psMap;
                    allChangeToThread2.getManageObjMap().put(objectId, manageObjMap.get(objectId));
                    z4 = true;
                } else {
                    arrayMap = psMap;
                }
                psMap = arrayMap;
            }
            if (z || z2 || z3 || z4) {
                notifyClient.sendMessage(1, allChangeToThread2);
            }
            i++;
            psMap = psMap;
        }
    }

    /* access modifiers changed from: package-private */
    public void addMessageToQueue(SaveRequest saveRequest, ObjectContext objectContext, String str, List<ObjectContext> list) {
        synchronized (this.messagesQueue) {
            this.messagesQueue.add(new MessageNode(saveRequest.getInsertedObjects(), saveRequest.getUpdatedObjects(), saveRequest.getDeletedObjects(), objectContext, str, list));
            this.messagesQueue.notifyAll();
        }
    }

    /* access modifiers changed from: package-private */
    public void addMessageToQueue(ObjectContext objectContext, String str, String str2, List<ObjectContext> list) {
        synchronized (this.messagesQueue) {
            this.messagesQueue.add(new MessageNode(objectContext, str, str2, true, list));
            this.messagesQueue.notifyAll();
        }
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
                            LOG.logW("The message handle thread interrupted by another thread.");
                        }
                    }
                    messageNode = (MessageNode) NotifyManager.this.messagesQueue.poll();
                }
                if (messageNode != null) {
                    try {
                        if (messageNode.isClearEntityMsg()) {
                            NotifyManager.this.sendMessageToObjectContext(messageNode.getChangeContext(), messageNode.getEntityName(), messageNode.getPsUri(), messageNode.getNotifyTargets());
                            messageNode.releaseReferences();
                        } else {
                            NotifyManager.this.sendMessageToObjectContext(new AllChangeToThread(messageNode.getInsertedObjects(), messageNode.getUpdatedObjects(), messageNode.getDeletedObjects(), messageNode.getChangeContext(), messageNode.getPsUri()), messageNode.getNotifyTargets());
                            messageNode.releaseReferences();
                        }
                    } catch (RuntimeException e) {
                        LOG.logE("A RuntimeException occurred during send message : " + e.getMessage());
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class MessageNode {
        private ObjectContext changeContext;
        private List<ManagedObject> deletedObjects = new ArrayList();
        private String entityName;
        private List<ManagedObject> insertedObjects = new ArrayList();
        private boolean isClearEntityMsg = false;
        private List<ObjectContext> notifyTargets;
        private String psUri;
        private List<ManagedObject> updatedObjects = new ArrayList();

        MessageNode(List<ManagedObject> list, List<ManagedObject> list2, List<ManagedObject> list3, ObjectContext objectContext, String str, List<ObjectContext> list4) {
            this.insertedObjects = new ArrayList(list);
            this.updatedObjects = new ArrayList(list2);
            this.deletedObjects = new ArrayList(list3);
            this.changeContext = objectContext;
            this.psUri = str;
            this.notifyTargets = list4;
        }

        MessageNode(ObjectContext objectContext, String str, String str2, boolean z, List<ObjectContext> list) {
            this.changeContext = objectContext;
            this.entityName = str;
            this.psUri = str2;
            this.isClearEntityMsg = z;
            this.notifyTargets = list;
        }

        public List<ManagedObject> getInsertedObjects() {
            return this.insertedObjects;
        }

        public List<ManagedObject> getUpdatedObjects() {
            return this.updatedObjects;
        }

        public List<ManagedObject> getDeletedObjects() {
            return this.deletedObjects;
        }

        public ObjectContext getChangeContext() {
            return this.changeContext;
        }

        public String getPsUri() {
            return this.psUri;
        }

        public String getEntityName() {
            return this.entityName;
        }

        public boolean isClearEntityMsg() {
            return this.isClearEntityMsg;
        }

        public List<ObjectContext> getNotifyTargets() {
            return this.notifyTargets;
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
            this.psUri = null;
            this.entityName = null;
        }
    }
}

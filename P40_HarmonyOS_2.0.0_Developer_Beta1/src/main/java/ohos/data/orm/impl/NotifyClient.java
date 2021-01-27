package ohos.data.orm.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import ohos.data.orm.AllChangeToTarget;
import ohos.data.orm.ObjectId;
import ohos.data.orm.OrmContext;
import ohos.data.orm.OrmObject;
import ohos.data.orm.OrmObjectObserver;
import ohos.data.utils.ExecutorUtils;

/* access modifiers changed from: package-private */
public class NotifyClient {
    private static final int BLOCK_QUEUE_SIZE = 1000;
    private static final int CORE_POOL_SIZE = 0;
    private static final long KEEP_ALIVE_SECONDS = 60;
    private static final int MAXIMUM_POOL_SIZE = 20;
    private final OrmContext context;
    private Map<String, List<OrmObjectObserver>> entityMap = Collections.synchronizedMap(new HashMap());
    private final Object observerLock = new Object();
    private Map<OrmContext, List<OrmObjectObserver>> ormContextMap = Collections.synchronizedMap(new HashMap());
    private Map<ObjectId, List<OrmObjectObserver>> ormObjectMap = Collections.synchronizedMap(new HashMap());
    private final ExecutorService pool;
    private List<OrmObjectObserver> psChangeObservers = Collections.synchronizedList(new ArrayList());

    NotifyClient(OrmContext ormContext) {
        this.context = ormContext;
        this.pool = ExecutorUtils.getExecutorService("NotifyClient#", 0, 20, KEEP_ALIVE_SECONDS, 1000);
    }

    /* access modifiers changed from: package-private */
    public String getAlias() {
        return this.context.getAlias();
    }

    /* access modifiers changed from: package-private */
    public void registerStoreObserver(String str, OrmObjectObserver ormObjectObserver) {
        if (str == null || ormObjectObserver == null) {
            throw new IllegalArgumentException("Alias or IListener should not be null.");
        } else if (str.equals(this.context.getAlias())) {
            synchronized (this.observerLock) {
                if (!this.psChangeObservers.contains(ormObjectObserver)) {
                    this.psChangeObservers.add(ormObjectObserver);
                }
            }
        } else {
            throw new IllegalArgumentException("The store not match current context.");
        }
    }

    /* access modifiers changed from: package-private */
    public void registerContextObserver(OrmContext ormContext, OrmObjectObserver ormObjectObserver) {
        if (ormContext == null || ormObjectObserver == null) {
            throw new IllegalArgumentException("OrmContext or IListener should not be null.");
        } else if (this.ormContextMap.containsKey(ormContext)) {
            List<OrmObjectObserver> list = this.ormContextMap.get(ormContext);
            synchronized (this.observerLock) {
                if (!list.contains(ormObjectObserver)) {
                    list.add(ormObjectObserver);
                }
            }
        } else {
            ArrayList arrayList = new ArrayList();
            synchronized (this.observerLock) {
                arrayList.add(ormObjectObserver);
            }
            this.ormContextMap.put(ormContext, arrayList);
        }
    }

    /* access modifiers changed from: package-private */
    public void registerEntityObserver(String str, OrmObjectObserver ormObjectObserver) {
        if (str == null || ormObjectObserver == null) {
            throw new IllegalArgumentException("EntityName or IListener should not be null.");
        } else if (this.entityMap.containsKey(str)) {
            List<OrmObjectObserver> list = this.entityMap.get(str);
            synchronized (this.observerLock) {
                if (!list.contains(ormObjectObserver)) {
                    list.add(ormObjectObserver);
                }
            }
        } else {
            ArrayList arrayList = new ArrayList();
            synchronized (this.observerLock) {
                arrayList.add(ormObjectObserver);
            }
            this.entityMap.put(str, arrayList);
        }
    }

    /* access modifiers changed from: package-private */
    public void registerOrmObjectObserver(OrmObject ormObject, OrmObjectObserver ormObjectObserver) {
        if (ormObject == null || ormObjectObserver == null) {
            throw new IllegalArgumentException("OrmObject or IListener should not be null.");
        }
        ObjectId objectId = ormObject.getObjectId();
        if (this.ormObjectMap.containsKey(objectId)) {
            List<OrmObjectObserver> list = this.ormObjectMap.get(objectId);
            synchronized (this.observerLock) {
                if (!list.contains(ormObjectObserver)) {
                    list.add(ormObjectObserver);
                }
            }
            return;
        }
        ArrayList arrayList = new ArrayList();
        synchronized (this.observerLock) {
            arrayList.add(ormObjectObserver);
        }
        this.ormObjectMap.put(objectId, arrayList);
    }

    /* access modifiers changed from: package-private */
    public void unregisterStoreObserver(String str, OrmObjectObserver ormObjectObserver) {
        if (str == null || ormObjectObserver == null) {
            throw new IllegalArgumentException("Alias or IListener should not be null.");
        } else if (str.equals(this.context.getAlias())) {
            synchronized (this.observerLock) {
                if (this.psChangeObservers.contains(ormObjectObserver)) {
                    this.psChangeObservers.remove(ormObjectObserver);
                }
            }
        } else {
            throw new IllegalArgumentException("The store not match current context.");
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterContextObserver(OrmContext ormContext, OrmObjectObserver ormObjectObserver) {
        if (ormContext == null || ormObjectObserver == null) {
            throw new IllegalArgumentException("OrmObject or IListener should not be null.");
        } else if (this.ormContextMap.containsKey(ormContext)) {
            List<OrmObjectObserver> list = this.ormContextMap.get(ormContext);
            synchronized (this.observerLock) {
                if (list.contains(ormObjectObserver)) {
                    list.remove(ormObjectObserver);
                    if (list.size() == 0) {
                        this.ormContextMap.remove(ormContext);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterEntityObserver(String str, OrmObjectObserver ormObjectObserver) {
        if (str == null || ormObjectObserver == null) {
            throw new IllegalArgumentException("EntityName or IListener should not be null.");
        } else if (this.entityMap.containsKey(str)) {
            List<OrmObjectObserver> list = this.entityMap.get(str);
            synchronized (this.observerLock) {
                if (list.contains(ormObjectObserver)) {
                    list.remove(ormObjectObserver);
                    if (list.size() == 0) {
                        this.entityMap.remove(str);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterObjectObserver(OrmObject ormObject, OrmObjectObserver ormObjectObserver) {
        if (ormObject == null || ormObjectObserver == null) {
            throw new IllegalArgumentException("ormObject or IListener should not be null.");
        }
        ObjectId objectId = ormObject.getObjectId();
        if (this.ormObjectMap.containsKey(objectId)) {
            List<OrmObjectObserver> list = this.ormObjectMap.get(objectId);
            synchronized (this.observerLock) {
                if (list.contains(ormObjectObserver)) {
                    list.remove(ormObjectObserver);
                    if (list.size() == 0) {
                        this.ormObjectMap.remove(objectId);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasObserver() {
        return hasStoreObserver() || hasOrmContextObserver() || hasEntityObserver() || hasOrmObjObserver();
    }

    /* access modifiers changed from: package-private */
    public boolean hasStoreObserver() {
        boolean z;
        synchronized (this.observerLock) {
            z = this.psChangeObservers.size() > 0;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean hasOrmContextObserver() {
        return this.ormContextMap.size() > 0;
    }

    /* access modifiers changed from: package-private */
    public boolean hasEntityObserver() {
        return this.entityMap.size() > 0;
    }

    /* access modifiers changed from: package-private */
    public boolean hasOrmObjObserver() {
        return this.ormObjectMap.size() > 0;
    }

    /* access modifiers changed from: package-private */
    public boolean hasPsObserver(String str) {
        boolean z = false;
        if (str == null || !str.equals(this.context.getAlias())) {
            return false;
        }
        synchronized (this.observerLock) {
            if (this.psChangeObservers.size() > 0) {
                z = true;
            }
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean hasOrmContextObserver(OrmContext ormContext) {
        return this.ormContextMap.containsKey(ormContext);
    }

    /* access modifiers changed from: package-private */
    public boolean hasEntityObserver(String str) {
        return this.entityMap.containsKey(str);
    }

    /* access modifiers changed from: package-private */
    public boolean hasOrmObjObserver(ObjectId objectId) {
        return this.ormObjectMap.containsKey(objectId);
    }

    /* access modifiers changed from: package-private */
    public void sendChangedMessage(AllChangeToThread allChangeToThread) {
        if (hasStoreObserver()) {
            sendChangedMessageToPsObserver(allChangeToThread);
        }
        if (allChangeToThread.getOrmContextMap().size() > 0) {
            sendChangedMessageToContextObserver(allChangeToThread);
        }
        if (allChangeToThread.getEntityMap().size() > 0) {
            sendChangedMessageToEntityObserver(allChangeToThread);
        }
        if (allChangeToThread.getManageObjMap().size() > 0) {
            sendChangedMessageToOrmObjObserver(allChangeToThread);
        }
    }

    private void sendChangedMessageToOrmObjObserver(AllChangeToThread allChangeToThread) {
        Map<ObjectId, AllChangeToTarget> manageObjMap = allChangeToThread.getManageObjMap();
        for (Map.Entry<ObjectId, List<OrmObjectObserver>> entry : this.ormObjectMap.entrySet()) {
            ObjectId key = entry.getKey();
            ArrayList<OrmObjectObserver> arrayList = new ArrayList();
            synchronized (this.observerLock) {
                copyList(entry.getValue(), arrayList);
            }
            AllChangeToTarget allChangeToTarget = manageObjMap.get(key);
            if (allChangeToTarget != null) {
                for (OrmObjectObserver ormObjectObserver : arrayList) {
                    this.pool.execute(new HandlerThread(allChangeToThread.getChangeContext(), allChangeToTarget, ormObjectObserver));
                }
            }
            arrayList.clear();
        }
    }

    private void sendChangedMessageToEntityObserver(AllChangeToThread allChangeToThread) {
        Map<String, AllChangeToTarget> entityMap2 = allChangeToThread.getEntityMap();
        for (Map.Entry<String, List<OrmObjectObserver>> entry : this.entityMap.entrySet()) {
            String key = entry.getKey();
            ArrayList arrayList = new ArrayList();
            synchronized (this.observerLock) {
                copyList(entry.getValue(), arrayList);
            }
            AllChangeToTarget allChangeToTarget = entityMap2.get(key);
            if (allChangeToTarget != null) {
                int size = arrayList.size();
                for (int i = 0; i < size; i++) {
                    this.pool.execute(new HandlerThread(allChangeToThread.getChangeContext(), allChangeToTarget, (OrmObjectObserver) arrayList.get(i)));
                }
            }
            arrayList.clear();
        }
    }

    private void sendChangedMessageToContextObserver(AllChangeToThread allChangeToThread) {
        Map<OrmContext, AllChangeToTarget> ormContextMap2 = allChangeToThread.getOrmContextMap();
        for (Map.Entry<OrmContext, List<OrmObjectObserver>> entry : this.ormContextMap.entrySet()) {
            OrmContext key = entry.getKey();
            ArrayList arrayList = new ArrayList();
            synchronized (this.observerLock) {
                copyList(entry.getValue(), arrayList);
            }
            AllChangeToTarget allChangeToTarget = ormContextMap2.get(key);
            if (allChangeToTarget != null) {
                int size = arrayList.size();
                for (int i = 0; i < size; i++) {
                    this.pool.execute(new HandlerThread(allChangeToThread.getChangeContext(), allChangeToTarget, (OrmObjectObserver) arrayList.get(i)));
                }
            }
            arrayList.clear();
        }
    }

    private void sendChangedMessageToPsObserver(AllChangeToThread allChangeToThread) {
        AllChangeToTarget allChangeToTarget = allChangeToThread.getPsMap().get(getAlias());
        ArrayList<OrmObjectObserver> arrayList = new ArrayList();
        synchronized (this.observerLock) {
            copyList(this.psChangeObservers, arrayList);
        }
        if (allChangeToTarget != null) {
            for (OrmObjectObserver ormObjectObserver : arrayList) {
                this.pool.execute(new HandlerThread(allChangeToThread.getChangeContext(), allChangeToTarget, ormObjectObserver));
            }
        }
        arrayList.clear();
    }

    private <E> void copyList(List<E> list, List<E> list2) {
        if (list != null && list2 != null) {
            list2.addAll(list);
        }
    }

    /* access modifiers changed from: private */
    public static class HandlerThread implements Runnable {
        private AllChangeToTarget allChange;
        private OrmContext changedContext;
        private OrmObjectObserver observer;

        HandlerThread(OrmContext ormContext, AllChangeToTarget allChangeToTarget, OrmObjectObserver ormObjectObserver) {
            this.changedContext = ormContext;
            this.allChange = allChangeToTarget;
            this.observer = ormObjectObserver;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.observer.onChange(this.changedContext, this.allChange);
        }
    }
}

package com.huawei.odmf.core;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ArrayMap;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.user.api.AllChangeToTarget;
import com.huawei.odmf.user.api.IListener;
import com.huawei.odmf.user.api.ObjectContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* access modifiers changed from: package-private */
public class NotifyClient {
    static final int NOTIFY_OBJECTS_CHANGED = 1;
    static final int NOTIFY_TABLE_DELETE = 2;
    ConcurrentHashMap<String, List<IListener>> entityMap = new ConcurrentHashMap<>();
    private final Object listenerLock = new Object();
    private Uri mUri;
    ConcurrentHashMap<ObjectId, List<IListener>> manageObjectMap = new ConcurrentHashMap<>();
    private final Handler msgHandler;
    ConcurrentHashMap<ObjectContext, List<IListener>> objectContextMap = new ConcurrentHashMap<>();
    List<IListener> psChangeListeners = Collections.synchronizedList(new ArrayList());

    NotifyClient(Looper looper, ObjectContext objectContext, Uri uri) {
        this.msgHandler = new MessageHandler(looper, objectContext);
        this.mUri = uri;
    }

    /* access modifiers changed from: package-private */
    public void registerListener(Uri uri, IListener iListener) {
        if (uri == null || iListener == null) {
            throw new ODMFIllegalArgumentException("Uri or IListener should not be null");
        } else if (uri.toString().equals(this.mUri.toString())) {
            synchronized (this.listenerLock) {
                if (!this.psChangeListeners.contains(iListener)) {
                    this.psChangeListeners.add(iListener);
                }
            }
        } else {
            throw new ODMFIllegalArgumentException("Uri error!!");
        }
    }

    /* access modifiers changed from: package-private */
    public void registerListener(ObjectContext objectContext, IListener iListener) {
        if (objectContext == null || iListener == null) {
            throw new ODMFIllegalArgumentException("ObjectContext or IListener should not be null");
        } else if (this.objectContextMap.containsKey(objectContext)) {
            List<IListener> list = this.objectContextMap.get(objectContext);
            synchronized (this.listenerLock) {
                if (!list.contains(iListener)) {
                    list.add(iListener);
                }
            }
        } else {
            ArrayList arrayList = new ArrayList();
            synchronized (this.listenerLock) {
                arrayList.add(iListener);
            }
            this.objectContextMap.put(objectContext, arrayList);
        }
    }

    /* access modifiers changed from: package-private */
    public void registerListener(String str, IListener iListener) {
        if (str == null || iListener == null) {
            throw new ODMFIllegalArgumentException("EntityName or IListener should not be null");
        } else if (this.entityMap.containsKey(str)) {
            List<IListener> list = this.entityMap.get(str);
            synchronized (this.listenerLock) {
                if (!list.contains(iListener)) {
                    list.add(iListener);
                }
            }
        } else {
            ArrayList arrayList = new ArrayList();
            synchronized (this.listenerLock) {
                arrayList.add(iListener);
            }
            this.entityMap.put(str, arrayList);
        }
    }

    /* access modifiers changed from: package-private */
    public void registerListener(ManagedObject managedObject, IListener iListener) {
        if (managedObject == null || iListener == null) {
            throw new ODMFIllegalArgumentException("ManageObject or IListener should not be null");
        }
        ObjectId objectId = managedObject.getObjectId();
        if (this.manageObjectMap.containsKey(objectId)) {
            List<IListener> list = this.manageObjectMap.get(objectId);
            synchronized (this.listenerLock) {
                if (!list.contains(iListener)) {
                    list.add(iListener);
                }
            }
            return;
        }
        ArrayList arrayList = new ArrayList();
        synchronized (this.listenerLock) {
            arrayList.add(iListener);
        }
        this.manageObjectMap.put(objectId, arrayList);
    }

    /* access modifiers changed from: package-private */
    public void unregisterListener(Uri uri, IListener iListener) {
        if (uri == null || iListener == null) {
            throw new ODMFIllegalArgumentException("Uri or IListener should not be null");
        } else if (uri.toString().equals(this.mUri.toString())) {
            synchronized (this.listenerLock) {
                if (this.psChangeListeners.contains(iListener)) {
                    this.psChangeListeners.remove(iListener);
                }
            }
        } else {
            throw new ODMFIllegalArgumentException("Uri error!!");
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterListener(ObjectContext objectContext, IListener iListener) {
        if (objectContext == null || iListener == null) {
            throw new ODMFIllegalArgumentException("ManageObject or IListener should not be null");
        } else if (this.objectContextMap.containsKey(objectContext)) {
            List<IListener> list = this.objectContextMap.get(objectContext);
            synchronized (this.listenerLock) {
                if (list.contains(iListener)) {
                    list.remove(iListener);
                    if (list.size() == 0) {
                        this.objectContextMap.remove(objectContext);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterListener(String str, IListener iListener) {
        if (str == null || iListener == null) {
            throw new ODMFIllegalArgumentException("EntityName or IListener should not be null");
        } else if (this.entityMap.containsKey(str)) {
            List<IListener> list = this.entityMap.get(str);
            synchronized (this.listenerLock) {
                if (list.contains(iListener)) {
                    list.remove(iListener);
                    if (list.size() == 0) {
                        this.entityMap.remove(str);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterListener(ManagedObject managedObject, IListener iListener) {
        if (managedObject == null || iListener == null) {
            throw new ODMFIllegalArgumentException("ManageObject or IListener should not be null");
        }
        ObjectId objectId = managedObject.getObjectId();
        if (this.manageObjectMap.containsKey(objectId)) {
            List<IListener> list = this.manageObjectMap.get(objectId);
            synchronized (this.listenerLock) {
                if (list.contains(iListener)) {
                    list.remove(iListener);
                    if (list.size() == 0) {
                        this.manageObjectMap.remove(objectId);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasPsListener() {
        boolean z;
        synchronized (this.listenerLock) {
            z = this.psChangeListeners.size() > 0;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean hasObjectContextListener() {
        return this.objectContextMap.size() > 0;
    }

    /* access modifiers changed from: package-private */
    public boolean hasEntityListener() {
        return this.entityMap.size() > 0;
    }

    /* access modifiers changed from: package-private */
    public boolean hasManageObjListener() {
        return this.manageObjectMap.size() > 0;
    }

    /* access modifiers changed from: package-private */
    public boolean hasPsListener(String str) {
        boolean z = false;
        if (str == null || !str.equals(this.mUri.toString())) {
            return false;
        }
        synchronized (this.listenerLock) {
            if (this.psChangeListeners.size() > 0) {
                z = true;
            }
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean hasObjectContextListener(ObjectContext objectContext) {
        return this.objectContextMap.containsKey(objectContext);
    }

    /* access modifiers changed from: package-private */
    public boolean hasEntityListener(String str) {
        return this.entityMap.containsKey(str);
    }

    /* access modifiers changed from: package-private */
    public boolean hasManageObjListener(ObjectId objectId) {
        return this.manageObjectMap.containsKey(objectId);
    }

    /* access modifiers changed from: package-private */
    public void sendMessage(int i, AllChangeToThread allChangeToThread) {
        Message obtainMessage = this.msgHandler.obtainMessage(i);
        obtainMessage.obj = allChangeToThread;
        this.msgHandler.sendMessage(obtainMessage);
    }

    private class MessageHandler extends Handler {
        private AObjectContext objContext;

        MessageHandler(Looper looper, ObjectContext objectContext) {
            super(looper);
            this.objContext = (AObjectContext) objectContext;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            AllChangeToThread allChangeToThread = (AllChangeToThread) message.obj;
            int i = message.what;
            if (i == 1) {
                NotifyClient.this.handleChangedMessage(this.objContext, allChangeToThread);
            } else if (i == 2) {
                NotifyClient.this.handleEntityClearMessage(this.objContext, allChangeToThread);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleChangedMessage(AObjectContext aObjectContext, AllChangeToThread allChangeToThread) {
        ObjectContext changeContext = allChangeToThread.getChangeContext();
        if (aObjectContext.getNotifyClient().hasPsListener()) {
            handleChangedMessageToPsListener(allChangeToThread, changeContext);
        }
        if (allChangeToThread.getObjContextMap().size() > 0) {
            handleChangedMessageToContextListener(allChangeToThread, changeContext);
        }
        if (allChangeToThread.getEntityMap().size() > 0) {
            handleChangedMessageToEntityListener(allChangeToThread, changeContext);
        }
        if (allChangeToThread.getManageObjMap().size() > 0) {
            handleChangedMessageToManagedObjListener(allChangeToThread, changeContext);
        }
    }

    private void handleChangedMessageToManagedObjListener(AllChangeToThread allChangeToThread, ObjectContext objectContext) {
        ArrayMap<ObjectId, AllChangeToTarget> manageObjMap = allChangeToThread.getManageObjMap();
        for (Map.Entry<ObjectId, List<IListener>> entry : this.manageObjectMap.entrySet()) {
            ObjectId key = entry.getKey();
            ArrayList arrayList = new ArrayList();
            synchronized (this.listenerLock) {
                copyList(entry.getValue(), arrayList);
            }
            AllChangeToTarget allChangeToTarget = manageObjMap.get(key);
            if (allChangeToTarget != null) {
                int size = arrayList.size();
                for (int i = 0; i < size; i++) {
                    ((IListener) arrayList.get(i)).onObjectsChanged(objectContext, allChangeToTarget);
                }
            }
            arrayList.clear();
        }
    }

    private void handleChangedMessageToEntityListener(AllChangeToThread allChangeToThread, ObjectContext objectContext) {
        ArrayMap<String, AllChangeToTarget> entityMap2 = allChangeToThread.getEntityMap();
        for (Map.Entry<String, List<IListener>> entry : this.entityMap.entrySet()) {
            String key = entry.getKey();
            ArrayList arrayList = new ArrayList();
            synchronized (this.listenerLock) {
                copyList(entry.getValue(), arrayList);
            }
            AllChangeToTarget allChangeToTarget = entityMap2.get(key);
            if (allChangeToTarget != null) {
                int size = arrayList.size();
                for (int i = 0; i < size; i++) {
                    ((IListener) arrayList.get(i)).onObjectsChanged(objectContext, allChangeToTarget);
                }
            }
            arrayList.clear();
        }
    }

    private void handleChangedMessageToContextListener(AllChangeToThread allChangeToThread, ObjectContext objectContext) {
        ArrayMap<ObjectContext, AllChangeToTarget> objContextMap = allChangeToThread.getObjContextMap();
        for (Map.Entry<ObjectContext, List<IListener>> entry : this.objectContextMap.entrySet()) {
            ObjectContext key = entry.getKey();
            ArrayList arrayList = new ArrayList();
            synchronized (this.listenerLock) {
                copyList(entry.getValue(), arrayList);
            }
            AllChangeToTarget allChangeToTarget = objContextMap.get(key);
            if (allChangeToTarget != null) {
                int size = arrayList.size();
                for (int i = 0; i < size; i++) {
                    ((IListener) arrayList.get(i)).onObjectsChanged(objectContext, allChangeToTarget);
                }
            }
            arrayList.clear();
        }
    }

    private void handleChangedMessageToPsListener(AllChangeToThread allChangeToThread, ObjectContext objectContext) {
        AllChangeToTarget allChangeToTarget = allChangeToThread.getPsMap().get(this.mUri.toString());
        ArrayList<IListener> arrayList = new ArrayList();
        synchronized (this.listenerLock) {
            copyList(this.psChangeListeners, arrayList);
        }
        if (allChangeToTarget != null) {
            for (IListener iListener : arrayList) {
                iListener.onObjectsChanged(objectContext, allChangeToTarget);
            }
        }
        arrayList.clear();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleEntityClearMessage(AObjectContext aObjectContext, AllChangeToThread allChangeToThread) {
        ObjectContext changeContext = allChangeToThread.getChangeContext();
        ArrayList<IListener> arrayList = new ArrayList();
        synchronized (this.listenerLock) {
            copyList(this.psChangeListeners, arrayList);
        }
        if (aObjectContext.getNotifyClient().hasPsListener()) {
            for (IListener iListener : arrayList) {
                iListener.onObjectsChanged(changeContext, new AllChangeToTargetImpl(true));
            }
        }
        arrayList.clear();
        if (aObjectContext.getNotifyClient().hasEntityListener(allChangeToThread.getEntityName())) {
            for (Map.Entry<String, List<IListener>> entry : this.entityMap.entrySet()) {
                String key = entry.getKey();
                ArrayList arrayList2 = new ArrayList();
                synchronized (this.listenerLock) {
                    copyList(entry.getValue(), arrayList2);
                }
                if (allChangeToThread.getEntityName().equals(key)) {
                    int size = arrayList2.size();
                    for (int i = 0; i < size; i++) {
                        ((IListener) arrayList2.get(i)).onObjectsChanged(changeContext, new AllChangeToTargetImpl(true));
                    }
                }
                arrayList2.clear();
            }
        }
    }

    private void copyList(List list, List list2) {
        if (!(list == null || list2 == null)) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                list2.add(list.get(i));
            }
        }
    }
}

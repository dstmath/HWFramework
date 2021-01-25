package com.huawei.odmf.core;

import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFIllegalStateException;
import com.huawei.odmf.predicate.SaveRequest;
import com.huawei.odmf.utils.LOG;
import java.util.ArrayList;
import java.util.List;

class TransactionImpl {
    private final AObjectContext mObjectContext;
    private final PersistentStoreCoordinator mPersistentStoreCoordinator;
    private List<ManagedObject> transactionDeleteObjectList;
    private List<ManagedObject> transactionInsertObjectList;
    private List<ManagedObject> transactionNotifyDeleteObjectList;
    private List<ManagedObject> transactionNotifyInsertObjectList;
    private List<ManagedObject> transactionNotifyUpdateObjectList;
    private List<ManagedObject> transactionUpdateObjectList;

    TransactionImpl(AObjectContext aObjectContext) {
        if (aObjectContext != null) {
            this.mPersistentStoreCoordinator = aObjectContext.getDefaultCoordinator();
            this.transactionInsertObjectList = new ArrayList();
            this.transactionUpdateObjectList = new ArrayList();
            this.transactionDeleteObjectList = new ArrayList();
            this.transactionNotifyInsertObjectList = new ArrayList();
            this.transactionNotifyUpdateObjectList = new ArrayList();
            this.transactionNotifyDeleteObjectList = new ArrayList();
            this.mObjectContext = aObjectContext;
            return;
        }
        throw new ODMFIllegalArgumentException("objectContext is null");
    }

    /* access modifiers changed from: package-private */
    public void setTransactionInsertObjectList(List<ManagedObject> list) {
        if (!list.isEmpty()) {
            this.transactionInsertObjectList.addAll(list);
        }
        if (this.mPersistentStoreCoordinator.hasListeners(this.mObjectContext).size() != 0) {
            for (ManagedObject managedObject : list) {
                if (managedObject.getObjectId().getId() != null) {
                    ManagedObject listContainObject = listContainObject(this.transactionNotifyUpdateObjectList, managedObject);
                    if (listContainObject != null) {
                        this.transactionNotifyUpdateObjectList.remove(listContainObject);
                        this.transactionNotifyUpdateObjectList.add(managedObject);
                    } else {
                        ManagedObject listContainObject2 = listContainObject(this.transactionNotifyDeleteObjectList, managedObject);
                        if (listContainObject2 != null) {
                            this.transactionNotifyDeleteObjectList.remove(listContainObject2);
                        }
                        ManagedObject listContainObject3 = listContainObject(this.transactionNotifyInsertObjectList, managedObject);
                        if (listContainObject3 != null) {
                            this.transactionNotifyInsertObjectList.remove(listContainObject3);
                        }
                        this.transactionNotifyInsertObjectList.add(managedObject);
                    }
                }
            }
        }
    }

    private ManagedObject listContainObject(List<ManagedObject> list, ManagedObject managedObject) {
        for (ManagedObject managedObject2 : list) {
            if (managedObject2.getObjectId().equals(managedObject.getObjectId())) {
                return managedObject2;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void setTransactionUpdateObjectList(List<ManagedObject> list) {
        if (!list.isEmpty()) {
            this.transactionUpdateObjectList.addAll(list);
        }
        if (this.mPersistentStoreCoordinator.hasListeners(this.mObjectContext).size() != 0) {
            for (ManagedObject managedObject : list) {
                ManagedObject listContainObject = listContainObject(this.transactionNotifyInsertObjectList, managedObject);
                if (listContainObject != null) {
                    this.transactionNotifyInsertObjectList.remove(listContainObject);
                    this.transactionNotifyInsertObjectList.add(managedObject);
                } else {
                    ManagedObject listContainObject2 = listContainObject(this.transactionNotifyDeleteObjectList, managedObject);
                    if (listContainObject2 != null) {
                        this.transactionNotifyDeleteObjectList.remove(listContainObject2);
                    }
                    ManagedObject listContainObject3 = listContainObject(this.transactionNotifyUpdateObjectList, managedObject);
                    if (listContainObject3 != null) {
                        this.transactionNotifyUpdateObjectList.remove(listContainObject3);
                    }
                    this.transactionNotifyUpdateObjectList.add(managedObject);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setTransactionDeleteObjectList(List<ManagedObject> list) {
        if (!list.isEmpty()) {
            this.transactionDeleteObjectList.addAll(list);
        }
        if (this.mPersistentStoreCoordinator.hasListeners(this.mObjectContext).size() != 0) {
            for (ManagedObject managedObject : list) {
                ManagedObject listContainObject = listContainObject(this.transactionNotifyInsertObjectList, managedObject);
                if (listContainObject != null) {
                    this.transactionNotifyInsertObjectList.remove(listContainObject);
                }
                ManagedObject listContainObject2 = listContainObject(this.transactionNotifyUpdateObjectList, managedObject);
                if (listContainObject2 != null) {
                    this.transactionNotifyUpdateObjectList.remove(listContainObject2);
                }
                ManagedObject listContainObject3 = listContainObject(this.transactionNotifyDeleteObjectList, managedObject);
                if (listContainObject3 != null) {
                    this.transactionNotifyDeleteObjectList.remove(listContainObject3);
                }
                this.transactionNotifyDeleteObjectList.add(managedObject);
            }
        }
    }

    public void beginTransaction() {
        if (!this.mPersistentStoreCoordinator.inTransaction(this.mObjectContext)) {
            this.mPersistentStoreCoordinator.beginTransaction(this.mObjectContext);
        } else {
            LOG.logE("beginTransaction failed.");
            throw new ODMFIllegalStateException("The database is already in a transaction.");
        }
    }

    public void commit() {
        if (this.mPersistentStoreCoordinator.inTransaction(this.mObjectContext)) {
            SaveRequest saveRequest = null;
            if (this.mPersistentStoreCoordinator.hasListeners(this.mObjectContext).size() != 0) {
                saveRequest = new SaveRequest(this.transactionNotifyInsertObjectList, this.transactionNotifyUpdateObjectList, this.transactionNotifyDeleteObjectList);
            }
            this.mPersistentStoreCoordinator.commit(this.mObjectContext, saveRequest);
            clearObjectList();
            return;
        }
        LOG.logE("commit failed.");
        clearObjectList();
        throw new ODMFIllegalStateException("The database is not in a transaction.");
    }

    public void rollback() {
        if (this.mPersistentStoreCoordinator.inTransaction(this.mObjectContext)) {
            rollBackObjectList();
            clearObjectList();
            this.mPersistentStoreCoordinator.rollback(this.mObjectContext);
            return;
        }
        LOG.logE("rollback failed.");
        clearObjectList();
        throw new ODMFIllegalStateException("The database is not in a transaction.");
    }

    public boolean inTransaction() {
        return this.mPersistentStoreCoordinator.inTransaction(this.mObjectContext);
    }

    private void rollBackObjectList() {
        for (ManagedObject managedObject : this.transactionInsertObjectList) {
            managedObject.setState(0);
        }
        for (ManagedObject managedObject2 : this.transactionUpdateObjectList) {
            managedObject2.setState(4);
        }
        for (ManagedObject managedObject3 : this.transactionDeleteObjectList) {
            managedObject3.setState(4);
        }
    }

    private void clearObjectList() {
        this.transactionInsertObjectList.clear();
        this.transactionUpdateObjectList.clear();
        this.transactionDeleteObjectList.clear();
        this.transactionNotifyInsertObjectList.clear();
        this.transactionNotifyUpdateObjectList.clear();
        this.transactionNotifyDeleteObjectList.clear();
    }
}

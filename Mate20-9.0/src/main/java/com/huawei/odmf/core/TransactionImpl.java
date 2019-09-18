package com.huawei.odmf.core;

import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFIllegalStateException;
import com.huawei.odmf.predicate.SaveRequest;
import com.huawei.odmf.user.api.ObjectContext;
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

    public TransactionImpl(AObjectContext objectContext) {
        if (objectContext == null) {
            throw new ODMFIllegalArgumentException("objectContext is null");
        }
        this.mPersistentStoreCoordinator = objectContext.getDefaultCoordinator();
        this.transactionInsertObjectList = new ArrayList();
        this.transactionUpdateObjectList = new ArrayList();
        this.transactionDeleteObjectList = new ArrayList();
        this.transactionNotifyInsertObjectList = new ArrayList();
        this.transactionNotifyUpdateObjectList = new ArrayList();
        this.transactionNotifyDeleteObjectList = new ArrayList();
        this.mObjectContext = objectContext;
    }

    /* access modifiers changed from: package-private */
    public void setTransactionInsertObjectList(List<ManagedObject> list) {
        if (!list.isEmpty()) {
            this.transactionInsertObjectList.addAll(list);
        }
        if (this.mPersistentStoreCoordinator.hasListeners((ObjectContext) this.mObjectContext).size() != 0) {
            for (ManagedObject manageObj : list) {
                if (manageObj.getObjectId().getId() != null) {
                    ManagedObject existManageObjUpdate = listContainObject(this.transactionNotifyUpdateObjectList, manageObj);
                    if (existManageObjUpdate != null) {
                        this.transactionNotifyUpdateObjectList.remove(existManageObjUpdate);
                        this.transactionNotifyUpdateObjectList.add(manageObj);
                    } else {
                        ManagedObject existManageObjDelete = listContainObject(this.transactionNotifyDeleteObjectList, manageObj);
                        if (existManageObjDelete != null) {
                            this.transactionNotifyDeleteObjectList.remove(existManageObjDelete);
                        }
                        ManagedObject existManageObjInsert = listContainObject(this.transactionNotifyInsertObjectList, manageObj);
                        if (existManageObjInsert != null) {
                            this.transactionNotifyInsertObjectList.remove(existManageObjInsert);
                        }
                        this.transactionNotifyInsertObjectList.add(manageObj);
                    }
                }
            }
        }
    }

    private ManagedObject listContainObject(List<ManagedObject> searchList, ManagedObject manObj) {
        for (ManagedObject verifyManageObject : searchList) {
            if (verifyManageObject.getObjectId().equals(manObj.getObjectId())) {
                return verifyManageObject;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void setTransactionUpdateObjectList(List<ManagedObject> list) {
        if (!list.isEmpty()) {
            this.transactionUpdateObjectList.addAll(list);
        }
        if (this.mPersistentStoreCoordinator.hasListeners((ObjectContext) this.mObjectContext).size() != 0) {
            for (ManagedObject manageObj : list) {
                ManagedObject existManageObjInsert = listContainObject(this.transactionNotifyInsertObjectList, manageObj);
                if (existManageObjInsert != null) {
                    this.transactionNotifyInsertObjectList.remove(existManageObjInsert);
                    this.transactionNotifyInsertObjectList.add(manageObj);
                } else {
                    ManagedObject existManageObjDelete = listContainObject(this.transactionNotifyDeleteObjectList, manageObj);
                    if (existManageObjDelete != null) {
                        this.transactionNotifyDeleteObjectList.remove(existManageObjDelete);
                    }
                    ManagedObject existManageObjUpdate = listContainObject(this.transactionNotifyUpdateObjectList, manageObj);
                    if (existManageObjUpdate != null) {
                        this.transactionNotifyUpdateObjectList.remove(existManageObjUpdate);
                    }
                    this.transactionNotifyUpdateObjectList.add(manageObj);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setTransactionDeleteObjectList(List<ManagedObject> list) {
        if (!list.isEmpty()) {
            this.transactionDeleteObjectList.addAll(list);
        }
        if (this.mPersistentStoreCoordinator.hasListeners((ObjectContext) this.mObjectContext).size() != 0) {
            for (ManagedObject manageObj : list) {
                ManagedObject existManageObjInsert = listContainObject(this.transactionNotifyInsertObjectList, manageObj);
                if (existManageObjInsert != null) {
                    this.transactionNotifyInsertObjectList.remove(existManageObjInsert);
                }
                ManagedObject existManageObjUpdate = listContainObject(this.transactionNotifyUpdateObjectList, manageObj);
                if (existManageObjUpdate != null) {
                    this.transactionNotifyUpdateObjectList.remove(existManageObjUpdate);
                }
                ManagedObject existManageObjDelete = listContainObject(this.transactionNotifyDeleteObjectList, manageObj);
                if (existManageObjDelete != null) {
                    this.transactionNotifyDeleteObjectList.remove(existManageObjDelete);
                }
                this.transactionNotifyDeleteObjectList.add(manageObj);
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
            if (this.mPersistentStoreCoordinator.hasListeners((ObjectContext) this.mObjectContext).size() != 0) {
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
        for (ManagedObject object : this.transactionInsertObjectList) {
            object.setState(0);
        }
        for (ManagedObject object2 : this.transactionUpdateObjectList) {
            object2.setState(4);
        }
        for (ManagedObject object3 : this.transactionDeleteObjectList) {
            object3.setState(4);
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

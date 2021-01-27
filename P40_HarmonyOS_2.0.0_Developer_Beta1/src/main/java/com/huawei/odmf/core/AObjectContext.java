package com.huawei.odmf.core;

import android.database.Cursor;
import android.net.Uri;
import android.os.Looper;
import android.text.TextUtils;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFIllegalStateException;
import com.huawei.odmf.exception.ODMFRuntimeException;
import com.huawei.odmf.model.api.Attribute;
import com.huawei.odmf.predicate.FetchRequest;
import com.huawei.odmf.predicate.SaveRequest;
import com.huawei.odmf.user.QueryBuilder;
import com.huawei.odmf.user.api.IListener;
import com.huawei.odmf.user.api.ObjectContext;
import com.huawei.odmf.user.api.Query;
import com.huawei.odmf.utils.Cache;
import com.huawei.odmf.utils.LOG;
import com.huawei.odmf.utils.ODMFCache;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AObjectContext implements ObjectContext {
    private static PersistentStoreCoordinator defaultCoordinator = PersistentStoreCoordinator.getDefault();
    private List<ManagedObject> deletedObjects = new ArrayList();
    private List<ManagedObject> insertedObjects = new ArrayList();
    private TransactionImpl mTransactionImpl;
    private Uri mUri;
    private NotifyClient notifyClient;
    private boolean openQueryCache = false;
    private long persistentStoreVersion = -1;
    private Cache<String, List<ManagedObject>> queryCache;
    private int queryCacheNumbers = 100;
    private List<ManagedObject> updatedObjects = new ArrayList();

    private AObjectContext(boolean z) {
        this.openQueryCache = z;
        if (z) {
            this.queryCache = new ODMFCache(this.queryCacheNumbers);
        }
    }

    public static AObjectContext openObjectContext(Uri uri) {
        return openObjectContext(uri, false);
    }

    public static AObjectContext openObjectContext(Uri uri, boolean z) {
        Looper myLooper = Looper.myLooper();
        if (myLooper == null) {
            myLooper = Looper.getMainLooper();
        }
        return openObjectContext(uri, z, myLooper);
    }

    public static AObjectContext openObjectContext(Uri uri, boolean z, Looper looper) {
        if (uri == null || looper == null) {
            LOG.logE("Execute openObjectContext failed : The input parameters has null.");
            throw new ODMFIllegalArgumentException("Execute openObjectContext failed : The input parameters has null.");
        } else if (defaultCoordinator.getPersistentStore(uri) == null) {
            return null;
        } else {
            AObjectContext aObjectContext = new AObjectContext(z);
            aObjectContext.mUri = uri;
            aObjectContext.notifyClient = new NotifyClient(looper, aObjectContext, aObjectContext.mUri);
            if (defaultCoordinator.connectPersistentStore(uri, aObjectContext) == 0) {
                return aObjectContext;
            }
            return null;
        }
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public boolean insert(ManagedObject managedObject) {
        if (managedObject == null) {
            LOG.logE("Execute insert failed : The parameters object is null.");
            throw new ODMFIllegalArgumentException("Execute insert failed : The parameters object is null.");
        } else if (this.insertedObjects == null) {
            LOG.logE("Execute insert failed : This objectContext has been closed.");
            return false;
        } else {
            managedObject.setObjectContext(this);
            if (managedObject.getState() == 0) {
                managedObject.setState(1);
                return this.insertedObjects.add(managedObject);
            } else if (managedObject.getState() == 1) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public boolean update(ManagedObject managedObject) {
        if (managedObject == null) {
            LOG.logE("Execute update failed : The parameters object is null.");
            throw new ODMFIllegalArgumentException("Execute update failed : The parameters object is null.");
        } else if (this.updatedObjects == null) {
            LOG.logE("Execute update failed : This objectContext has been closed.");
            return false;
        } else {
            managedObject.setObjectContext(this);
            if (managedObject.getDirty() == 1 || managedObject.getDirty() == 2) {
                managedObject.setDirty(2);
                managedObject.setLastObjectContext(this);
            }
            if (managedObject.getState() == 4) {
                managedObject.setState(2);
                return this.updatedObjects.add(managedObject);
            } else if (managedObject.getState() == 2) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public boolean delete(ManagedObject managedObject) {
        if (managedObject == null) {
            LOG.logE("Execute delete failed : The parameters object is null.");
            throw new ODMFIllegalArgumentException("Execute delete failed : The parameters object is null.");
        } else if (this.deletedObjects == null) {
            LOG.logE("Execute delete failed : This objectContext has been closed.");
            return false;
        } else {
            managedObject.setObjectContext(this);
            if (managedObject.getState() == 4 || managedObject.getState() == 2) {
                managedObject.setState(3);
                return this.deletedObjects.add(managedObject);
            } else if (managedObject.getState() == 3) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void setQueryCacheNumbers(int i) {
        this.queryCacheNumbers = i;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void beginTransaction() {
        this.mTransactionImpl = new TransactionImpl(this);
        this.mTransactionImpl.beginTransaction();
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void commit() {
        TransactionImpl transactionImpl = this.mTransactionImpl;
        if (transactionImpl != null) {
            transactionImpl.commit();
        } else {
            LOG.logE("Execute commit failed : The transaction has not started.");
            throw new ODMFIllegalStateException("Execute commit failed : The transaction has not started.");
        }
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public boolean inTransaction() {
        TransactionImpl transactionImpl = this.mTransactionImpl;
        if (transactionImpl == null) {
            return false;
        }
        return transactionImpl.inTransaction();
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void rollback() {
        TransactionImpl transactionImpl = this.mTransactionImpl;
        if (transactionImpl != null) {
            transactionImpl.rollback();
        } else {
            LOG.logE("Execute rollback failed : The transaction has not started.");
            throw new ODMFIllegalStateException("Execute rollback failed : The transaction has not started.");
        }
    }

    private void failedTransaction() {
        for (ManagedObject managedObject : this.deletedObjects) {
            managedObject.setState(4);
        }
        for (ManagedObject managedObject2 : this.insertedObjects) {
            managedObject2.setState(0);
        }
        for (ManagedObject managedObject3 : this.updatedObjects) {
            managedObject3.setState(4);
        }
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public boolean flush() {
        if (this.insertedObjects.isEmpty() && this.updatedObjects.isEmpty() && this.deletedObjects.isEmpty()) {
            return false;
        }
        try {
            defaultCoordinator.save(new SaveRequest(this.insertedObjects, this.updatedObjects, this.deletedObjects), this);
            if (this.mTransactionImpl != null && this.mTransactionImpl.inTransaction()) {
                this.mTransactionImpl.setTransactionInsertObjectList(this.insertedObjects);
                this.mTransactionImpl.setTransactionUpdateObjectList(this.updatedObjects);
                this.mTransactionImpl.setTransactionDeleteObjectList(this.deletedObjects);
            }
            if (!this.openQueryCache) {
                return true;
            }
            this.queryCache.clear();
            this.persistentStoreVersion = -1;
            return true;
        } catch (ODMFRuntimeException e) {
            failedTransaction();
            LOG.logE("Flush failed : " + e.getMessage());
            throw e;
        } catch (RuntimeException e2) {
            failedTransaction();
            LOG.logE("Flush failed : " + e2.getMessage());
            throw new ODMFRuntimeException("Flush failed : " + e2.getMessage());
        } catch (Throwable th) {
            if (this.openQueryCache) {
                this.queryCache.clear();
                this.persistentStoreVersion = -1;
            }
            throw th;
        }
    }

    public void closeStatement() {
        closeInsert();
        closeUpdate();
        closeDelete();
        this.insertedObjects.clear();
        this.updatedObjects.clear();
        this.deletedObjects.clear();
    }

    private void closeInsert() {
        Iterator<ManagedObject> it = this.insertedObjects.iterator();
        while (it.hasNext()) {
            ((AManagedObject) it.next()).getHelper().getEntity().getStatements().clearInsertStatement();
        }
    }

    private void closeUpdate() {
        Iterator<ManagedObject> it = this.updatedObjects.iterator();
        while (it.hasNext()) {
            ((AManagedObject) it.next()).getHelper().getEntity().getStatements().clearUpdateStatement();
        }
    }

    private void closeDelete() {
        Iterator<ManagedObject> it = this.deletedObjects.iterator();
        while (it.hasNext()) {
            ((AManagedObject) it.next()).getHelper().getEntity().getStatements().clearDeleteStatement();
        }
    }

    public ManagedObject get(ObjectId objectId) {
        if (objectId != null) {
            return defaultCoordinator.getObjectValues(objectId, this);
        }
        LOG.logE("Execute get failed : The parameter objectID is null.");
        throw new ODMFIllegalArgumentException("Execute get failed : The parameter objectID is null.");
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void deleteEntityData(Class cls) {
        if (cls != null) {
            defaultCoordinator.clearTable(this, cls.getName());
            if (this.openQueryCache) {
                this.queryCache.clear();
                return;
            }
            return;
        }
        LOG.logE("Execute deleteTableData failed : The parameter clz is null.");
        throw new ODMFIllegalArgumentException("Execute deleteTableData failed : The parameter clz is null.");
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public <T extends ManagedObject> List<T> get(Class<T> cls) {
        if (cls != null) {
            return where(cls).findAll();
        }
        LOG.logE("Execute get failed : The parameter clz is null.");
        throw new ODMFIllegalArgumentException("Execute get failed : The parameter clz is null.");
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public <T extends ManagedObject> Query<T> where(Class<T> cls) {
        if (cls != null) {
            String name = cls.getName();
            return QueryBuilder.buildQuery(name, new FetchRequest(name, cls), this);
        }
        LOG.logE("Execute where failed : The parameter clz is null.");
        throw new ODMFIllegalArgumentException("Execute where failed : The parameter clz is null.");
    }

    public <T extends ManagedObject> List<T> executeFetchRequest(FetchRequest<T> fetchRequest) {
        List<T> list;
        if (fetchRequest != null) {
            String fetchRequest2 = fetchRequest.toString();
            ODMFList oDMFList = new ODMFList(this, fetchRequest.getEntityName());
            if (this.openQueryCache) {
                long persistentStoreVersion2 = defaultCoordinator.getPersistentStoreVersion(this);
                if (this.persistentStoreVersion != persistentStoreVersion2) {
                    this.queryCache.clear();
                    list = defaultCoordinator.executeFetchRequest(fetchRequest, this);
                    putValuesIntoCache(fetchRequest2, list);
                    this.persistentStoreVersion = persistentStoreVersion2;
                } else {
                    List<ManagedObject> valuesFromCache = getValuesFromCache(fetchRequest2);
                    if (valuesFromCache == null) {
                        list = defaultCoordinator.executeFetchRequest(fetchRequest, this);
                        this.queryCache.put(fetchRequest2, list);
                    } else {
                        list = valuesFromCache;
                    }
                }
            } else {
                list = defaultCoordinator.executeFetchRequest(fetchRequest, this);
            }
            oDMFList.addObjAll(oDMFList.size(), list);
            return oDMFList;
        }
        LOG.logE("Execute executeFetchRequest failed : The parameter fetchRequest is null.");
        throw new ODMFIllegalArgumentException("Execute executeFetchRequest failed : The parameter fetchRequest is null.");
    }

    public <T extends ManagedObject> LazyList<T> executeFetchRequestLazyList(FetchRequest<T> fetchRequest) {
        if (fetchRequest != null) {
            return new LazyList<>(defaultCoordinator.executeFetchRequestGetObjectId(fetchRequest, this), this, fetchRequest.getEntityName());
        }
        LOG.logE("Execute executeFetchRequestLazyList failed : The parameter fetchRequest is null.");
        throw new ODMFIllegalArgumentException("Execute executeFetchRequestLazyList failed : The parameter fetchRequest is null.");
    }

    public <T extends ManagedObject> Cursor executeFetchRequestGetCursor(FetchRequest<T> fetchRequest) {
        if (fetchRequest != null) {
            return defaultCoordinator.executeFetchRequestGetCursor(fetchRequest, this);
        }
        LOG.logE("Execute executeFetchRequestGetCursor failed : The parameter fetchRequest is null.");
        throw new ODMFIllegalArgumentException("Execute executeFetchRequestGetCursor failed : The parameter fetchRequest is null.");
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

    public PersistentStoreCoordinator getDefaultCoordinator() {
        return defaultCoordinator;
    }

    public long getPersistentStoreVersion() {
        return this.persistentStoreVersion;
    }

    public void setPersistentStoreVersion(int i) {
        this.persistentStoreVersion = (long) i;
    }

    public boolean isOpenQueryCache() {
        return this.openQueryCache;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void setOpenQueryCache(boolean z) {
        this.openQueryCache = z;
        Cache<String, List<ManagedObject>> cache = this.queryCache;
        if (cache == null) {
            if (z) {
                this.queryCache = new ODMFCache(this.queryCacheNumbers);
            }
        } else if (!z) {
            cache.clear();
            this.queryCache = null;
        }
    }

    public boolean clearCache() {
        if (this.openQueryCache) {
            this.queryCache.clear();
            return true;
        }
        LOG.logE("Execute clearCache failed : The queryCache is closed.");
        throw new ODMFIllegalStateException("Execute clearCache failed : The queryCache is closed.");
    }

    /* access modifiers changed from: protected */
    public void putValuesIntoCache(String str, List<ManagedObject> list) {
        if (TextUtils.isEmpty(str)) {
            LOG.logE("Execute putValuesIntoCache failed : The input parameters has null.");
            throw new ODMFIllegalArgumentException("Execute putValuesIntoCache failed : The input parameters has null.");
        } else if (this.openQueryCache) {
            this.queryCache.put(str, list);
        }
    }

    /* access modifiers changed from: protected */
    public List<ManagedObject> getValuesFromCache(String str) {
        if (this.openQueryCache) {
            return this.queryCache.get(str);
        }
        return null;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void close() {
        if (this.openQueryCache) {
            this.queryCache.clear();
        }
        this.insertedObjects = null;
        this.updatedObjects = null;
        this.deletedObjects = null;
        defaultCoordinator.disconnectPersistentStore(this.mUri, this);
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void registerListener(Object obj, IListener iListener) {
        if (obj instanceof Uri) {
            this.notifyClient.registerListener((Uri) obj, iListener);
        } else if (obj instanceof AObjectContext) {
            this.notifyClient.registerListener((AObjectContext) obj, iListener);
        } else if (obj instanceof String) {
            this.notifyClient.registerListener((String) obj, iListener);
        } else if (obj instanceof ManagedObject) {
            this.notifyClient.registerListener((ManagedObject) obj, iListener);
        } else {
            LOG.logE("Execute registerListener failed : Wrong listener type.");
            throw new ODMFIllegalArgumentException("Execute registerListener failed : Wrong listener type.");
        }
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void unregisterListener(Object obj, IListener iListener) {
        if (obj instanceof Uri) {
            this.notifyClient.unregisterListener((Uri) obj, iListener);
        } else if (obj instanceof AObjectContext) {
            this.notifyClient.unregisterListener((AObjectContext) obj, iListener);
        } else if (obj instanceof String) {
            this.notifyClient.unregisterListener((String) obj, iListener);
        } else if (obj instanceof ManagedObject) {
            this.notifyClient.unregisterListener((ManagedObject) obj, iListener);
        } else {
            LOG.logE("Execute unregisterListener failed : Wrong listener type.");
            throw new ODMFIllegalArgumentException("Execute unregisterListener failed : Wrong listener type.");
        }
    }

    public NotifyClient getNotifyClient() {
        return this.notifyClient;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public ManagedObject getToOneRelationshipValue(String str, ManagedObject managedObject) {
        if (str != null && managedObject != null) {
            return defaultCoordinator.getToOneRelationshipValue(str, managedObject, this);
        }
        LOG.logE("Execute getToOneRelationshipValue failed : The input parameters has null.");
        throw new ODMFIllegalArgumentException("Execute getToOneRelationshipValue failed : The input parameters has null.");
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public List<? extends ManagedObject> getToManyRelationshipValue(String str, ManagedObject managedObject) {
        if (str != null && managedObject != null) {
            return defaultCoordinator.getToManyRelationshipValue(str, managedObject, this);
        }
        LOG.logE("Execute getToManyRelationshipValue failed : The input parameters has null.");
        throw new ODMFIllegalArgumentException("Execute getToManyRelationshipValue failed : The input parameters has null.");
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public ManagedObject remoteGetToOneRelationshipValue(String str, ObjectId objectId) {
        if (str != null && objectId != null) {
            return defaultCoordinator.remoteGetToOneRelationshipValue(str, objectId, this);
        }
        LOG.logE("Execute remoteGetToOneRelationshipValue failed : The input parameters has null.");
        throw new ODMFIllegalArgumentException("Execute remoteGetToOneRelationshipValue failed : The input parameters has null.");
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public List<? extends ManagedObject> remoteGetToManyRelationshipValue(String str, ObjectId objectId) {
        if (str != null && objectId != null) {
            return defaultCoordinator.remoteGetToManyRelationshipValue(str, objectId, this);
        }
        LOG.logE("Execute remoteGetToManyRelationshipValue failed : The input parameters has null.");
        throw new ODMFIllegalArgumentException("Execute remoteGetToManyRelationshipValue failed : The input parameters has null.");
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public Cursor executeRawQuerySQL(String str) {
        if (str != null && !str.equals("")) {
            return defaultCoordinator.executeRawQuerySQL(str, this);
        }
        LOG.logE("Execute RawQuerySQL failed : The parameters has null.");
        throw new ODMFIllegalArgumentException("Execute RawQuerySQL failed : The input parameters has null.");
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void executeRawSQL(String str) {
        if (str == null || str.equals("")) {
            LOG.logE("Execute RawSQL failed : Then parameters sql is null.");
            throw new ODMFIllegalArgumentException("Execute RawSQL failed : The parameters sql is null.");
        } else {
            defaultCoordinator.executeRawSQL(str, this);
        }
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public Cursor query(boolean z, String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        return defaultCoordinator.query(z, str, strArr, str2, strArr2, str3, str4, str5, str6, this);
    }

    public List<Object> executeFetchRequestWithAggregateFunction(FetchRequest fetchRequest) {
        if (fetchRequest != null) {
            return defaultCoordinator.executeFetchRequestWithAggregateFunction(fetchRequest, this);
        }
        LOG.logE("Execute executeFetchRequestWithAggregateFunction failed : The input parameters has null.");
        throw new ODMFIllegalArgumentException("Execute executeFetchRequestWithAggregateFunction failed : The input parameters has null.");
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public String getDbVersion() {
        return defaultCoordinator.getDbVersion(this);
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public String getEntityVersion(String str) {
        if (!TextUtils.isEmpty(str)) {
            return defaultCoordinator.getEntityVersion(str, this);
        }
        LOG.logE("Execute getEntityVersion failed : The tableName is null.");
        throw new ODMFIllegalArgumentException("Execute getEntityVersion failed : The tableName is null.");
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void setEntityVersions(String str, String str2, int i) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            LOG.logE("Execute getEntityVersion failed : The tableName or the entityVersion is null.");
            throw new ODMFIllegalArgumentException("Execute getEntityVersion failed : The tableName or the entityVersion is null.");
        } else {
            defaultCoordinator.setEntityVersions(str, str2, i, this);
        }
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public int getDbVersionCode() {
        return defaultCoordinator.getDbVersionCode(this);
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public int getEntityVersionCode(String str) {
        if (!TextUtils.isEmpty(str)) {
            return defaultCoordinator.getEntityVersionCode(str, this);
        }
        LOG.logE("Execute getEntityVersionCode failed : The tableName is null.");
        throw new ODMFIllegalArgumentException("Execute getEntityVersionCode failed : The tableName is null.");
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void setDbVersions(String str, int i) {
        if (!TextUtils.isEmpty(str)) {
            defaultCoordinator.setDbVersions(str, i, this);
        } else {
            LOG.logE("Execute setDbVersions failed : The dbVersion is null.");
            throw new ODMFIllegalArgumentException("Execute setDBVersion failed : The dbVersion is null.");
        }
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void exportDatabase(String str, byte[] bArr) {
        if (!TextUtils.isEmpty(str)) {
            defaultCoordinator.exportDatabase(str, bArr, this);
        } else {
            LOG.logE("Execute exportDatabase failed : The input parameter newDbName is null.");
            throw new ODMFIllegalArgumentException("Execute exportDatabase failed : The input parameter newDbName is null.");
        }
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void resetMetadata() {
        defaultCoordinator.resetMetadata(this);
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public List<? extends Attribute> getEntityAttributes(String str) {
        return defaultCoordinator.getEntityAttributes(this, str);
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public Set<String> getTableInvolvedInSQL(String str) {
        return defaultCoordinator.getTableInvolvedInSQL(this, str);
    }
}

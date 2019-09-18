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
import java.util.List;

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

    private AObjectContext(boolean openQueryCache2) {
        this.openQueryCache = openQueryCache2;
        if (openQueryCache2) {
            this.queryCache = new ODMFCache(this.queryCacheNumbers);
        }
    }

    public static AObjectContext openObjectContext(Uri path) {
        return openObjectContext(path, false);
    }

    public static AObjectContext openObjectContext(Uri path, boolean openCache) {
        Looper mLooper = Looper.myLooper();
        if (mLooper == null) {
            mLooper = Looper.getMainLooper();
        }
        return openObjectContext(path, openCache, mLooper);
    }

    public static AObjectContext openObjectContext(Uri path, boolean openCache, Looper looper) {
        if (path == null || looper == null) {
            LOG.logE("Execute openObjectContext failed : The input parameters has null.");
            throw new ODMFIllegalArgumentException("Execute openObjectContext failed : The input parameters has null.");
        } else if (defaultCoordinator.getPersistentStore(path) == null) {
            return null;
        } else {
            AObjectContext objectContext = new AObjectContext(openCache);
            objectContext.mUri = path;
            objectContext.notifyClient = new NotifyClient(looper, objectContext, objectContext.mUri);
            if (defaultCoordinator.connectPersistentStore(path, objectContext) != 0) {
                return null;
            }
            return objectContext;
        }
    }

    public boolean insert(ManagedObject object) {
        if (object == null) {
            LOG.logE("Execute insert failed : The parameters object is null.");
            throw new ODMFIllegalArgumentException("Execute insert failed : The parameters object is null.");
        } else if (this.insertedObjects == null) {
            LOG.logE("Execute insert failed : This objectContext has been closed.");
            return false;
        } else {
            object.setObjectContext(this);
            if (object.getState() == 0) {
                object.setState(1);
                return this.insertedObjects.add(object);
            } else if (object.getState() == 1) {
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean update(ManagedObject object) {
        if (object == null) {
            LOG.logE("Execute update failed : The parameters object is null.");
            throw new ODMFIllegalArgumentException("Execute update failed : The parameters object is null.");
        } else if (this.updatedObjects == null) {
            LOG.logE("Execute update failed : This objectContext has been closed.");
            return false;
        } else {
            object.setObjectContext(this);
            if (object.getDirty() == 1 || object.getDirty() == 2) {
                object.setDirty(2);
                object.setLastObjectContext(this);
            }
            if (object.getState() == 4) {
                object.setState(2);
                return this.updatedObjects.add(object);
            } else if (object.getState() == 2) {
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean delete(ManagedObject object) {
        if (object == null) {
            LOG.logE("Execute delete failed : The parameters object is null.");
            throw new ODMFIllegalArgumentException("Execute delete failed : The parameters object is null.");
        } else if (this.deletedObjects == null) {
            LOG.logE("Execute delete failed : This objectContext has been closed.");
            return false;
        } else {
            object.setObjectContext(this);
            if (object.getState() == 4 || object.getState() == 2) {
                object.setState(3);
                return this.deletedObjects.add(object);
            } else if (object.getState() == 3) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void setQueryCacheNumbers(int queryCacheNumbers2) {
        this.queryCacheNumbers = queryCacheNumbers2;
    }

    public void beginTransaction() {
        this.mTransactionImpl = new TransactionImpl(this);
        this.mTransactionImpl.beginTransaction();
    }

    public void commit() {
        if (this.mTransactionImpl == null) {
            LOG.logE("Execute commit failed : The transaction has not started.");
            throw new ODMFIllegalStateException("Execute commit failed : The transaction has not started.");
        } else {
            this.mTransactionImpl.commit();
        }
    }

    public boolean inTransaction() {
        if (this.mTransactionImpl == null) {
            return false;
        }
        return this.mTransactionImpl.inTransaction();
    }

    public void rollback() {
        if (this.mTransactionImpl == null) {
            LOG.logE("Execute rollback failed : The transaction has not started.");
            throw new ODMFIllegalStateException("Execute rollback failed : The transaction has not started.");
        } else {
            this.mTransactionImpl.rollback();
        }
    }

    private void failedTransaction() {
        for (ManagedObject tItDeleted : this.deletedObjects) {
            tItDeleted.setState(4);
        }
        for (ManagedObject tInsertedObj : this.insertedObjects) {
            tInsertedObj.setState(0);
        }
        for (ManagedObject tUpdateObj : this.updatedObjects) {
            tUpdateObj.setState(4);
        }
    }

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
            this.insertedObjects.clear();
            this.updatedObjects.clear();
            this.deletedObjects.clear();
            if (this.openQueryCache) {
                this.queryCache.clear();
                this.persistentStoreVersion = -1;
            }
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
            this.insertedObjects.clear();
            this.updatedObjects.clear();
            this.deletedObjects.clear();
            if (this.openQueryCache) {
                this.queryCache.clear();
                this.persistentStoreVersion = -1;
            }
            throw th;
        }
    }

    public ManagedObject get(ObjectId objectID) {
        if (objectID != null) {
            return defaultCoordinator.getObjectValues(objectID, this);
        }
        LOG.logE("Execute get failed : The parameter objectID is null.");
        throw new ODMFIllegalArgumentException("Execute get failed : The parameter objectID is null.");
    }

    public void deleteEntityData(Class clz) {
        if (clz == null) {
            LOG.logE("Execute deleteTableData failed : The parameter clz is null.");
            throw new ODMFIllegalArgumentException("Execute deleteTableData failed : The parameter clz is null.");
        }
        defaultCoordinator.clearTable(this, clz.getName());
        if (this.openQueryCache) {
            this.queryCache.clear();
        }
    }

    public <T extends ManagedObject> List<T> get(Class<T> clz) {
        if (clz != null) {
            return where(clz).findAll();
        }
        LOG.logE("Execute get failed : The parameter clz is null.");
        throw new ODMFIllegalArgumentException("Execute get failed : The parameter clz is null.");
    }

    public <T extends ManagedObject> Query<T> where(Class<T> clz) {
        if (clz == null) {
            LOG.logE("Execute where failed : The parameter clz is null.");
            throw new ODMFIllegalArgumentException("Execute where failed : The parameter clz is null.");
        }
        String entityName = clz.getName();
        return QueryBuilder.buildQuery(entityName, new FetchRequest(entityName, clz), this);
    }

    /* JADX WARNING: type inference failed for: r9v0, types: [com.huawei.odmf.predicate.FetchRequest, com.huawei.odmf.predicate.FetchRequest<T>] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public <T extends ManagedObject> List<T> executeFetchRequest(FetchRequest<T> r9) {
        List<ManagedObject> executeFetchRequest;
        if (r9 == 0) {
            LOG.logE("Execute executeFetchRequest failed : The parameter fetchRequest is null.");
            throw new ODMFIllegalArgumentException("Execute executeFetchRequest failed : The parameter fetchRequest is null.");
        }
        String fetchSql = r9.toString();
        ODMFList<T> manageObjects = new ODMFList<>(this, r9.getEntityName());
        if (this.openQueryCache) {
            long version = defaultCoordinator.getPersistentStoreVersion(this);
            if (this.persistentStoreVersion != version) {
                this.queryCache.clear();
                executeFetchRequest = defaultCoordinator.executeFetchRequest(r9, this);
                putValuesIntoCache(fetchSql, executeFetchRequest);
                this.persistentStoreVersion = version;
            } else {
                executeFetchRequest = getValuesFromCache(fetchSql);
                if (executeFetchRequest == null) {
                    executeFetchRequest = defaultCoordinator.executeFetchRequest(r9, this);
                    this.queryCache.put(fetchSql, executeFetchRequest);
                }
            }
        } else {
            executeFetchRequest = defaultCoordinator.executeFetchRequest(r9, this);
        }
        manageObjects.addObjAll(manageObjects.size(), executeFetchRequest);
        return manageObjects;
    }

    public <T extends ManagedObject> LazyList<T> executeFetchRequestLazyList(FetchRequest<T> fetchRequest) {
        if (fetchRequest != null) {
            return new LazyList<>(defaultCoordinator.executeFetchRequestGetObjectID(fetchRequest, this), this, fetchRequest.getEntityName());
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

    public void setPersistentStoreVersion(int persistentStoreVersion2) {
        this.persistentStoreVersion = (long) persistentStoreVersion2;
    }

    public boolean isOpenQueryCache() {
        return this.openQueryCache;
    }

    public void setOpenQueryCache(boolean openQueryCache2) {
        this.openQueryCache = openQueryCache2;
        if (this.queryCache == null) {
            if (openQueryCache2) {
                this.queryCache = new ODMFCache(this.queryCacheNumbers);
            }
        } else if (!openQueryCache2) {
            this.queryCache.clear();
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
    public void putValuesIntoCache(String key, List<ManagedObject> manageObjects) {
        if (TextUtils.isEmpty(key)) {
            LOG.logE("Execute putValuesIntoCache failed : The input parameters has null.");
            throw new ODMFIllegalArgumentException("Execute putValuesIntoCache failed : The input parameters has null.");
        } else if (this.openQueryCache) {
            this.queryCache.put(key, manageObjects);
        }
    }

    /* access modifiers changed from: protected */
    public List<ManagedObject> getValuesFromCache(String key) {
        if (this.openQueryCache) {
            return this.queryCache.get(key);
        }
        return null;
    }

    public void close() {
        if (this.openQueryCache) {
            this.queryCache.clear();
        }
        this.insertedObjects = null;
        this.updatedObjects = null;
        this.deletedObjects = null;
        defaultCoordinator.disconnectPersistentStore(this.mUri, this);
    }

    public void registerListener(Object obj, IListener listener) {
        if (obj instanceof Uri) {
            this.notifyClient.registerListener((Uri) obj, listener);
        } else if (obj instanceof AObjectContext) {
            this.notifyClient.registerListener((ObjectContext) (AObjectContext) obj, listener);
        } else if (obj instanceof String) {
            this.notifyClient.registerListener((String) obj, listener);
        } else if (obj instanceof ManagedObject) {
            this.notifyClient.registerListener((ManagedObject) obj, listener);
        } else {
            LOG.logE("Execute registerListener failed : Wrong listener type.");
            throw new ODMFIllegalArgumentException("Execute registerListener failed : Wrong listener type.");
        }
    }

    public void unregisterListener(Object obj, IListener listener) {
        if (obj instanceof Uri) {
            this.notifyClient.unregisterListener((Uri) obj, listener);
        } else if (obj instanceof AObjectContext) {
            this.notifyClient.unregisterListener((ObjectContext) (AObjectContext) obj, listener);
        } else if (obj instanceof String) {
            this.notifyClient.unregisterListener((String) obj, listener);
        } else if (obj instanceof ManagedObject) {
            this.notifyClient.unregisterListener((ManagedObject) obj, listener);
        } else {
            LOG.logE("Execute unregisterListener failed : Wrong listener type.");
            throw new ODMFIllegalArgumentException("Execute unregisterListener failed : Wrong listener type.");
        }
    }

    public NotifyClient getNotifyClient() {
        return this.notifyClient;
    }

    public ManagedObject getToOneRelationshipValue(String fieldName, ManagedObject object) {
        if (fieldName != null && object != null) {
            return defaultCoordinator.getToOneRelationshipValue(fieldName, object, this);
        }
        LOG.logE("Execute getToOneRelationshipValue failed : The input parameters has null.");
        throw new ODMFIllegalArgumentException("Execute getToOneRelationshipValue failed : The input parameters has null.");
    }

    public List<? extends ManagedObject> getToManyRelationshipValue(String fieldName, ManagedObject object) {
        if (fieldName != null && object != null) {
            return defaultCoordinator.getToManyRelationshipValue(fieldName, object, this);
        }
        LOG.logE("Execute getToManyRelationshipValue failed : The input parameters has null.");
        throw new ODMFIllegalArgumentException("Execute getToManyRelationshipValue failed : The input parameters has null.");
    }

    public ManagedObject remoteGetToOneRelationshipValue(String fieldName, ObjectId objectId) {
        if (fieldName != null && objectId != null) {
            return defaultCoordinator.remoteGetToOneRelationshipValue(fieldName, objectId, this);
        }
        LOG.logE("Execute remoteGetToOneRelationshipValue failed : The input parameters has null.");
        throw new ODMFIllegalArgumentException("Execute remoteGetToOneRelationshipValue failed : The input parameters has null.");
    }

    public List<? extends ManagedObject> remoteGetToManyRelationshipValue(String fieldName, ObjectId objectId) {
        if (fieldName != null && objectId != null) {
            return defaultCoordinator.remoteGetToManyRelationshipValue(fieldName, objectId, this);
        }
        LOG.logE("Execute remoteGetToManyRelationshipValue failed : The input parameters has null.");
        throw new ODMFIllegalArgumentException("Execute remoteGetToManyRelationshipValue failed : The input parameters has null.");
    }

    public Cursor executeRawQuerySQL(String sql) {
        if (sql != null && !sql.equals("")) {
            return defaultCoordinator.executeRawQuerySQL(sql, this);
        }
        LOG.logE("Execute RawQuerySQL failed : The parameters has null.");
        throw new ODMFIllegalArgumentException("Execute RawQuerySQL failed : The input parameters has null.");
    }

    public void executeRawSQL(String sql) {
        if (sql == null || sql.equals("")) {
            LOG.logE("Execute RawSQL failed : Then parameters sql is null.");
            throw new ODMFIllegalArgumentException("Execute RawSQL failed : The parameters sql is null.");
        } else {
            defaultCoordinator.executeRawSQL(sql, this);
        }
    }

    public Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return defaultCoordinator.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, this);
    }

    public List<Object> executeFetchRequestWithAggregateFunction(FetchRequest fetchRequest) {
        if (fetchRequest != null) {
            return defaultCoordinator.executeFetchRequestWithAggregateFunction(fetchRequest, this);
        }
        LOG.logE("Execute executeFetchRequestWithAggregateFunction failed : The input parameters has null.");
        throw new ODMFIllegalArgumentException("Execute executeFetchRequestWithAggregateFunction failed : The input parameters has null.");
    }

    public String getDBVersion() {
        return defaultCoordinator.getDBVersion(this);
    }

    public String getEntityVersion(String tableName) {
        if (!TextUtils.isEmpty(tableName)) {
            return defaultCoordinator.getEntityVersion(tableName, this);
        }
        LOG.logE("Execute getEntityVersion failed : The tableName is null.");
        throw new ODMFIllegalArgumentException("Execute getEntityVersion failed : The tableName is null.");
    }

    public void setEntityVersions(String tableName, String entityVersion, int entityVersionCode) {
        if (TextUtils.isEmpty(tableName) || TextUtils.isEmpty(entityVersion)) {
            LOG.logE("Execute getEntityVersion failed : The tableName or the entityVersion is null.");
            throw new ODMFIllegalArgumentException("Execute getEntityVersion failed : The tableName or the entityVersion is null.");
        } else {
            defaultCoordinator.setEntityVersions(tableName, entityVersion, entityVersionCode, this);
        }
    }

    public int getDBVersionCode() {
        return defaultCoordinator.getDBVersionCode(this);
    }

    public int getEntityVersionCode(String tableName) {
        if (!TextUtils.isEmpty(tableName)) {
            return defaultCoordinator.getEntityVersionCode(tableName, this);
        }
        LOG.logE("Execute getEntityVersionCode failed : The tableName is null.");
        throw new ODMFIllegalArgumentException("Execute getEntityVersionCode failed : The tableName is null.");
    }

    public void setDBVersions(String dbVersion, int dbVersionCode) {
        if (TextUtils.isEmpty(dbVersion)) {
            LOG.logE("Execute setDBVersions failed : The dbVersion is null.");
            throw new ODMFIllegalArgumentException("Execute setDBVersion failed : The dbVersion is null.");
        } else {
            defaultCoordinator.setDBVersions(dbVersion, dbVersionCode, this);
        }
    }

    public void exportDatabase(String newDBName, byte[] newKey) {
        if (TextUtils.isEmpty(newDBName)) {
            LOG.logE("Execute exportDatabase failed : The input parameter newDBName is null.");
            throw new ODMFIllegalArgumentException("Execute exportDatabase failed : The input parameter newDBName is null.");
        } else {
            defaultCoordinator.exportDatabase(newDBName, newKey, this);
        }
    }

    public void resetMetadata() {
        defaultCoordinator.resetMetadata(this);
    }

    public List<? extends Attribute> getEntityAttributes(String entityName) {
        return defaultCoordinator.getEntityAttributes(this, entityName);
    }
}

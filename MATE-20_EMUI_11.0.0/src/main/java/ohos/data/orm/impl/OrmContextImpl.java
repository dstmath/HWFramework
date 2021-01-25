package ohos.data.orm.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import ohos.data.orm.OrmContext;
import ohos.data.orm.OrmObject;
import ohos.data.orm.OrmObjectObserver;
import ohos.data.orm.OrmPredicates;
import ohos.data.rdb.RdbException;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;

public class OrmContextImpl implements OrmContext {
    private static final int DEFAULT_QUEUE_LENGTH = 20;
    private String alias;
    private ThreadLocal<Cache> cache = new ThreadLocal<>();
    private OrmStore store;
    private final Object storeLock = new Object();
    private ThreadLocal<Transaction> transaction = new ThreadLocal<>();

    enum Aggregate {
        MAX,
        MIN,
        SUM,
        AVG
    }

    private OrmContextImpl(String str) {
        this.alias = str;
    }

    private List<OrmObject> getInsertedObjects() {
        if (this.cache.get() == null) {
            this.cache.set(new Cache());
        }
        return this.cache.get().getInsertList();
    }

    private List<OrmObject> getUpdatedObjects() {
        if (this.cache.get() == null) {
            this.cache.set(new Cache());
        }
        return this.cache.get().getUpdateList();
    }

    private List<OrmObject> getDeletedObjects() {
        if (this.cache.get() == null) {
            this.cache.set(new Cache());
        }
        return this.cache.get().getDeleteList();
    }

    private Transaction getTransaction() {
        return this.transaction.get();
    }

    public static OrmContext openOrmContext(String str) {
        OrmContextImpl ormContextImpl = new OrmContextImpl(str);
        ormContextImpl.setStore(StoreCoordinator.getInstance().acquireOrmStore(str, ormContextImpl));
        return ormContextImpl;
    }

    @Override // ohos.data.orm.OrmContext
    public String getAlias() {
        return this.alias;
    }

    @Override // ohos.data.orm.OrmContext
    public void changeEncryptKey(byte[] bArr) {
        getStore().changeEncryptKey(bArr);
    }

    private OrmStore getStore() {
        OrmStore ormStore;
        synchronized (this.storeLock) {
            if (this.store != null) {
                ormStore = this.store;
            } else {
                throw new IllegalStateException("The orm store has been closed.");
            }
        }
        return ormStore;
    }

    private void setStore(OrmStore ormStore) {
        synchronized (this.storeLock) {
            this.store = ormStore;
        }
    }

    @Override // ohos.data.orm.OrmContext, java.lang.AutoCloseable
    public void close() {
        setStore(null);
        StoreCoordinator.getInstance().releaseOrmStore(this.alias, this);
    }

    @Override // ohos.data.orm.OrmContext
    public <T extends OrmObject> boolean insert(T t) {
        if (t != null) {
            return getInsertedObjects().add(t);
        }
        throw new IllegalArgumentException("Execute insert object failed : The parameters object is null.");
    }

    @Override // ohos.data.orm.OrmContext
    public int update(OrmPredicates ormPredicates, ValuesBucket valuesBucket) {
        if (ormPredicates != null) {
            return getStore().executeUpdateRequest(ormPredicates, valuesBucket);
        }
        throw new IllegalArgumentException("Execute delete failed : The parameter predicates is null.");
    }

    @Override // ohos.data.orm.OrmContext
    public <T extends OrmObject> boolean update(T t) {
        if (t != null) {
            return getUpdatedObjects().add(t);
        }
        throw new IllegalArgumentException("Execute update object failed : The parameters object is null.");
    }

    @Override // ohos.data.orm.OrmContext
    public <T extends OrmObject> boolean delete(T t) {
        if (t != null) {
            return getDeletedObjects().add(t);
        }
        throw new IllegalArgumentException("Execute delete object failed : The parameters object is null.");
    }

    @Override // ohos.data.orm.OrmContext
    public int delete(OrmPredicates ormPredicates) {
        if (ormPredicates != null) {
            return getStore().executeDeleteRequest(ormPredicates);
        }
        throw new IllegalArgumentException("Execute delete failed : The parameter predicates is null.");
    }

    @Override // ohos.data.orm.OrmContext
    public <T extends OrmObject> List<T> query(OrmPredicates ormPredicates) {
        if (ormPredicates != null) {
            return (List<T>) getStore().executeFetchRequest(ormPredicates);
        }
        throw new IllegalArgumentException("Execute query failed : The parameter predicates is null.");
    }

    @Override // ohos.data.orm.OrmContext
    public ResultSet query(OrmPredicates ormPredicates, String[] strArr) {
        if (ormPredicates != null) {
            return getStore().executeSharedFetchRequest(ormPredicates, strArr);
        }
        throw new IllegalArgumentException("Execute query failed : The parameter predicates is null.");
    }

    private BigDecimal aggregate(OrmPredicates ormPredicates, Aggregate aggregate, String str) {
        if (ormPredicates == null) {
            throw new IllegalArgumentException("Execute " + aggregate.name().toLowerCase() + " failed : The parameter predicates is null.");
        } else if (str != null) {
            String executeAggregateRequest = getStore().executeAggregateRequest(ormPredicates, aggregate.name(), str);
            if (executeAggregateRequest != null) {
                return new BigDecimal(executeAggregateRequest);
            }
            return null;
        } else {
            throw new IllegalArgumentException("Execute " + aggregate.name().toLowerCase() + " failed : no field specified.");
        }
    }

    @Override // ohos.data.orm.OrmContext
    public Long count(OrmPredicates ormPredicates) {
        if (ormPredicates != null) {
            return Long.valueOf(getStore().count(ormPredicates));
        }
        throw new IllegalArgumentException("Execute count failed : The parameter predicates is null.");
    }

    @Override // ohos.data.orm.OrmContext
    public Double max(OrmPredicates ormPredicates, String str) {
        BigDecimal aggregate = aggregate(ormPredicates, Aggregate.MAX, str);
        if (aggregate != null) {
            return Double.valueOf(aggregate.doubleValue());
        }
        return null;
    }

    @Override // ohos.data.orm.OrmContext
    public Double min(OrmPredicates ormPredicates, String str) {
        BigDecimal aggregate = aggregate(ormPredicates, Aggregate.MIN, str);
        if (aggregate != null) {
            return Double.valueOf(aggregate.doubleValue());
        }
        return null;
    }

    @Override // ohos.data.orm.OrmContext
    public Double avg(OrmPredicates ormPredicates, String str) {
        BigDecimal aggregate = aggregate(ormPredicates, Aggregate.AVG, str);
        if (aggregate != null) {
            return Double.valueOf(aggregate.doubleValue());
        }
        return null;
    }

    @Override // ohos.data.orm.OrmContext
    public Double sum(OrmPredicates ormPredicates, String str) {
        BigDecimal aggregate = aggregate(ormPredicates, Aggregate.SUM, str);
        if (aggregate != null) {
            return Double.valueOf(aggregate.doubleValue());
        }
        return null;
    }

    @Override // ohos.data.orm.OrmContext
    public <T extends OrmObject> OrmPredicates where(Class<T> cls) {
        if (cls != null) {
            return new OrmPredicates(cls);
        }
        throw new IllegalArgumentException("Execute where failed : The parameter clz is null.");
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.data.orm.OrmContext
    public boolean flush() {
        List<OrmObject> insertedObjects = getInsertedObjects();
        List<OrmObject> updatedObjects = getUpdatedObjects();
        List<OrmObject> deletedObjects = getDeletedObjects();
        if (insertedObjects.isEmpty() && updatedObjects.isEmpty() && deletedObjects.isEmpty()) {
            return false;
        }
        SaveRequest saveRequest = new SaveRequest(insertedObjects, updatedObjects, deletedObjects);
        try {
            getStore().save(saveRequest);
            if (getTransaction() != null) {
                getTransaction().setInsertObjectList(insertedObjects);
                getTransaction().setUpdateObjectList(updatedObjects);
                getTransaction().setDeleteObjectList(deletedObjects);
            } else {
                StoreCoordinator.getInstance().sendMessage(saveRequest, this.alias, this);
            }
            insertedObjects.clear();
            updatedObjects.clear();
            deletedObjects.clear();
            this.cache.remove();
            return true;
        } catch (Throwable th) {
            insertedObjects.clear();
            updatedObjects.clear();
            deletedObjects.clear();
            this.cache.remove();
            throw th;
        }
    }

    @Override // ohos.data.orm.OrmContext
    public void beginTransaction() {
        if (getTransaction() == null) {
            if (getInsertedObjects().size() > 0 || getUpdatedObjects().size() > 0 || getDeletedObjects().size() > 0) {
                flush();
            }
            this.transaction.set(new Transaction());
            try {
                getStore().beginTransaction();
            } catch (RdbException e) {
                clearTransaction();
                throw e;
            }
        } else {
            throw new IllegalStateException("Execute beginTransaction failed : Another transaction has been started.");
        }
    }

    @Override // ohos.data.orm.OrmContext
    public void commit() {
        if (getTransaction() != null) {
            try {
                getStore().commit();
                StoreCoordinator.getInstance().sendMessage(getTransaction().getSaveRequest(), this.alias, this);
            } finally {
                clearTransaction();
            }
        } else {
            throw new IllegalStateException("Execute commit failed : The transaction has not started.");
        }
    }

    @Override // ohos.data.orm.OrmContext
    public boolean isInTransaction() {
        return getTransaction() != null;
    }

    @Override // ohos.data.orm.OrmContext
    public void rollback() {
        if (getTransaction() != null) {
            try {
                getStore().rollback();
            } finally {
                clearTransaction();
            }
        } else {
            throw new IllegalStateException("Execute rollback failed : The transaction has not started.");
        }
    }

    private void clearTransaction() {
        if (getTransaction() != null) {
            getTransaction().clearObjectList();
            this.transaction.remove();
        }
    }

    @Override // ohos.data.orm.OrmContext
    public void registerStoreObserver(String str, OrmObjectObserver ormObjectObserver) {
        StoreCoordinator.getInstance().registerStoreListener(str, ormObjectObserver, this);
    }

    @Override // ohos.data.orm.OrmContext
    public void registerContextObserver(OrmContext ormContext, OrmObjectObserver ormObjectObserver) {
        StoreCoordinator.getInstance().registerContextListener(ormContext, ormObjectObserver, this);
    }

    @Override // ohos.data.orm.OrmContext
    public void registerEntityObserver(String str, OrmObjectObserver ormObjectObserver) {
        StoreCoordinator.getInstance().registerEntityListener(str, ormObjectObserver, this);
    }

    @Override // ohos.data.orm.OrmContext
    public void registerObjectObserver(OrmObject ormObject, OrmObjectObserver ormObjectObserver) {
        StoreCoordinator.getInstance().registerObjcetListener(ormObject, ormObjectObserver, this);
    }

    @Override // ohos.data.orm.OrmContext
    public void unregisterStoreObserver(String str, OrmObjectObserver ormObjectObserver) {
        StoreCoordinator.getInstance().unregisterStoreListener(str, ormObjectObserver, this);
    }

    @Override // ohos.data.orm.OrmContext
    public void unregisterContextObserver(OrmContext ormContext, OrmObjectObserver ormObjectObserver) {
        StoreCoordinator.getInstance().unregisterContextListener(ormContext, ormObjectObserver, this);
    }

    @Override // ohos.data.orm.OrmContext
    public void unregisterEntityObserver(String str, OrmObjectObserver ormObjectObserver) {
        StoreCoordinator.getInstance().unregisterEntityListener(str, ormObjectObserver, this);
    }

    @Override // ohos.data.orm.OrmContext
    public void unregisterObjectObserver(OrmObject ormObject, OrmObjectObserver ormObjectObserver) {
        StoreCoordinator.getInstance().unregisterObjectListener(ormObject, ormObjectObserver, this);
    }

    @Override // ohos.data.orm.OrmContext
    public boolean backup(String str) {
        return getStore().backup(str);
    }

    @Override // ohos.data.orm.OrmContext
    public boolean restore(String str) {
        return getStore().restore(str);
    }

    @Override // ohos.data.orm.OrmContext
    public boolean backup(String str, byte[] bArr) {
        return getStore().backup(str, bArr);
    }

    @Override // ohos.data.orm.OrmContext
    public boolean restore(String str, byte[] bArr, byte[] bArr2) {
        return getStore().restore(str, bArr, bArr2);
    }

    /* access modifiers changed from: private */
    public static class Cache {
        private List<OrmObject> deleteList;
        private List<OrmObject> insertList;
        private List<OrmObject> updateList;

        private Cache() {
            this.insertList = new ArrayList(20);
            this.updateList = new ArrayList(20);
            this.deleteList = new ArrayList(20);
        }

        public List<OrmObject> getInsertList() {
            return this.insertList;
        }

        public List<OrmObject> getUpdateList() {
            return this.updateList;
        }

        public List<OrmObject> getDeleteList() {
            return this.deleteList;
        }
    }
}

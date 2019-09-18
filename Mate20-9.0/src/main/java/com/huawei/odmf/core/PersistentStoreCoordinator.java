package com.huawei.odmf.core;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFRuntimeException;
import com.huawei.odmf.model.api.Attribute;
import com.huawei.odmf.predicate.FetchRequest;
import com.huawei.odmf.predicate.SaveRequest;
import com.huawei.odmf.user.api.ObjectContext;
import com.huawei.odmf.utils.LOG;
import com.huawei.odmf.utils.ODMFCache;
import com.huawei.odmf.utils.Singleton;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PersistentStoreCoordinator {
    private static final int EXIST = 1;
    private static final int FAIL = -1;
    private static final int SUCCESS = 0;
    private static final Singleton<PersistentStoreCoordinator> gDefault = new Singleton<PersistentStoreCoordinator>() {
        public PersistentStoreCoordinator create() {
            return new PersistentStoreCoordinator();
        }
    };
    private final Object cacheLock;
    private final ConcurrentHashMap<ObjectContext, PersistentStore> mapContextToPersistentStore;
    private final ConcurrentHashMap<String, PersistentStore> mapUriToPersistentStore;
    private NotifyManager notifyManager;
    private ODMFCache<ObjectId, ManagedObject> objectsCache;
    private final Object persistentStoreLock;

    /* access modifiers changed from: protected */
    public ConcurrentHashMap<String, PersistentStore> getMapUriToPersistentStore() {
        return this.mapUriToPersistentStore;
    }

    /* access modifiers changed from: package-private */
    public ConcurrentHashMap<ObjectContext, PersistentStore> getMapContextToPersistentStore() {
        return this.mapContextToPersistentStore;
    }

    private PersistentStoreCoordinator() {
        this.mapUriToPersistentStore = new ConcurrentHashMap<>();
        this.mapContextToPersistentStore = new ConcurrentHashMap<>();
        this.objectsCache = null;
        this.persistentStoreLock = new Object();
        this.cacheLock = new Object();
        CacheConfig cacheConfig = CacheConfig.getDefault();
        if (cacheConfig.isOpenObjectCache()) {
            this.objectsCache = new ODMFCache<>(cacheConfig.getObjectCacheNum());
        }
        this.notifyManager = new NotifyManager();
    }

    public static PersistentStoreCoordinator getDefault() {
        return gDefault.get();
    }

    /* access modifiers changed from: protected */
    public int createPersistentStore(Uri uri, Configuration storeConfiguration, Context appCtx, String modelPath) {
        return addPersistentStore(uri, storeConfiguration, appCtx, modelPath, null);
    }

    /* access modifiers changed from: package-private */
    public int createEncryptedPersistentStore(Uri uri, Configuration storeConfiguration, Context appCtx, String modelPath, byte[] key) {
        return addPersistentStore(uri, storeConfiguration, appCtx, modelPath, key);
    }

    /*  JADX ERROR: StackOverflow in pass: MarkFinallyVisitor
        jadx.core.utils.exceptions.JadxOverflowException: 
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:47)
        	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:81)
        */
    int createCrossPersistentStore(android.net.Uri r13, com.huawei.odmf.core.Configuration r14, android.content.Context r15, java.util.Map<android.net.Uri, byte[]> r16) {
        /*
            r12 = this;
            if (r13 == 0) goto L_0x0006
            if (r15 == 0) goto L_0x0006
            if (r16 != 0) goto L_0x0013
        L_0x0006:
            java.lang.String r1 = "createCrossPersistentStore : The parameters has null."
            com.huawei.odmf.utils.LOG.logE(r1)
            com.huawei.odmf.exception.ODMFIllegalArgumentException r1 = new com.huawei.odmf.exception.ODMFIllegalArgumentException
            java.lang.String r2 = "The input parameter of uri, appCtx and the uriList has null"
            r1.<init>((java.lang.String) r2)
            throw r1
        L_0x0013:
            int r1 = r16.size()
            if (r1 != 0) goto L_0x0026
            java.lang.String r1 = "createCrossPersistentStore : The uriMap contains nothing."
            com.huawei.odmf.utils.LOG.logE(r1)
            com.huawei.odmf.exception.ODMFIllegalArgumentException r1 = new com.huawei.odmf.exception.ODMFIllegalArgumentException
            java.lang.String r2 = "The uriList contains nothing."
            r1.<init>((java.lang.String) r2)
            throw r1
        L_0x0026:
            java.util.ArrayList r5 = new java.util.ArrayList
            r5.<init>()
            java.util.ArrayList r6 = new java.util.ArrayList
            r6.<init>()
            java.lang.Object r11 = r12.persistentStoreLock     // Catch:{ all -> 0x006b }
            monitor-enter(r11)     // Catch:{ all -> 0x006b }
            java.util.Set r1 = r16.entrySet()     // Catch:{ all -> 0x0068 }
            java.util.Iterator r2 = r1.iterator()     // Catch:{ all -> 0x0068 }
        L_0x003b:
            boolean r1 = r2.hasNext()     // Catch:{ all -> 0x0068 }
            if (r1 == 0) goto L_0x00a1
            java.lang.Object r7 = r2.next()     // Catch:{ all -> 0x0068 }
            java.util.Map$Entry r7 = (java.util.Map.Entry) r7     // Catch:{ all -> 0x0068 }
            java.util.concurrent.ConcurrentHashMap<java.lang.String, com.huawei.odmf.core.PersistentStore> r3 = r12.mapUriToPersistentStore     // Catch:{ all -> 0x0068 }
            java.lang.Object r1 = r7.getKey()     // Catch:{ all -> 0x0068 }
            android.net.Uri r1 = (android.net.Uri) r1     // Catch:{ all -> 0x0068 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0068 }
            java.lang.Object r10 = r3.get(r1)     // Catch:{ all -> 0x0068 }
            com.huawei.odmf.core.PersistentStore r10 = (com.huawei.odmf.core.PersistentStore) r10     // Catch:{ all -> 0x0068 }
            if (r10 != 0) goto L_0x0092
            java.lang.String r1 = "createCrossPersistentStore : Some uri in uriList does not indicates a persistentStore."
            com.huawei.odmf.utils.LOG.logE(r1)     // Catch:{ all -> 0x0068 }
            com.huawei.odmf.exception.ODMFIllegalArgumentException r1 = new com.huawei.odmf.exception.ODMFIllegalArgumentException     // Catch:{ all -> 0x0068 }
            java.lang.String r2 = "Some uri in uriList does not indicates a persistentStore."
            r1.<init>((java.lang.String) r2)     // Catch:{ all -> 0x0068 }
            throw r1     // Catch:{ all -> 0x0068 }
        L_0x0068:
            r1 = move-exception
            monitor-exit(r11)     // Catch:{ all -> 0x0068 }
            throw r1     // Catch:{ all -> 0x006b }
        L_0x006b:
            r1 = move-exception
            java.util.Set r2 = r16.entrySet()
            java.util.Iterator r2 = r2.iterator()
        L_0x0074:
            boolean r3 = r2.hasNext()
            if (r3 == 0) goto L_0x00e7
            java.lang.Object r7 = r2.next()
            java.util.Map$Entry r7 = (java.util.Map.Entry) r7
            java.lang.Object r9 = r7.getValue()
            byte[] r9 = (byte[]) r9
            if (r9 == 0) goto L_0x0074
            r8 = 0
        L_0x0089:
            int r3 = r9.length
            if (r8 >= r3) goto L_0x0074
            r3 = 0
            r9[r8] = r3
            int r8 = r8 + 1
            goto L_0x0089
        L_0x0092:
            java.lang.String r1 = r10.getPath()     // Catch:{ all -> 0x0068 }
            r5.add(r1)     // Catch:{ all -> 0x0068 }
            java.lang.Object r1 = r7.getValue()     // Catch:{ all -> 0x0068 }
            r6.add(r1)     // Catch:{ all -> 0x0068 }
            goto L_0x003b
        L_0x00a1:
            r1 = 301(0x12d, float:4.22E-43)
            r14.setDatabaseType(r1)     // Catch:{ all -> 0x0068 }
            r1 = 401(0x191, float:5.62E-43)
            r14.setStorageMode(r1)     // Catch:{ all -> 0x0068 }
            com.huawei.odmf.core.CrossPersistentStore r0 = new com.huawei.odmf.core.CrossPersistentStore     // Catch:{ all -> 0x0068 }
            r2 = 0
            java.lang.String r3 = r13.toString()     // Catch:{ all -> 0x0068 }
            r1 = r15
            r4 = r14
            r0.<init>(r1, r2, r3, r4, r5, r6)     // Catch:{ all -> 0x0068 }
            java.util.concurrent.ConcurrentHashMap<java.lang.String, com.huawei.odmf.core.PersistentStore> r1 = r12.mapUriToPersistentStore     // Catch:{ all -> 0x0068 }
            java.lang.String r2 = r13.toString()     // Catch:{ all -> 0x0068 }
            r1.put(r2, r0)     // Catch:{ all -> 0x0068 }
            monitor-exit(r11)     // Catch:{ all -> 0x0068 }
            java.util.Set r1 = r16.entrySet()
            java.util.Iterator r1 = r1.iterator()
        L_0x00c9:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x00e8
            java.lang.Object r7 = r1.next()
            java.util.Map$Entry r7 = (java.util.Map.Entry) r7
            java.lang.Object r9 = r7.getValue()
            byte[] r9 = (byte[]) r9
            if (r9 == 0) goto L_0x00c9
            r8 = 0
        L_0x00de:
            int r2 = r9.length
            if (r8 >= r2) goto L_0x00c9
            r2 = 0
            r9[r8] = r2
            int r8 = r8 + 1
            goto L_0x00de
        L_0x00e7:
            throw r1
        L_0x00e8:
            r1 = 0
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.odmf.core.PersistentStoreCoordinator.createCrossPersistentStore(android.net.Uri, com.huawei.odmf.core.Configuration, android.content.Context, java.util.Map):int");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:72:?, code lost:
        return 0;
     */
    private int addPersistentStore(Uri uri, Configuration storeConfiguration, Context appCtx, String modelPath, byte[] key) {
        PersistentStore cacheStore;
        if (uri != null && storeConfiguration != null && appCtx != null) {
            synchronized (this.persistentStoreLock) {
                if (!this.mapUriToPersistentStore.containsKey(uri.toString())) {
                    switch (storeConfiguration.getType()) {
                        case 200:
                            for (PersistentStore psVal : this.mapUriToPersistentStore.values()) {
                                if (psVal.getPath() != null && psVal.getPath().equals(storeConfiguration.getPath())) {
                                    LOG.logE("addPersistentStore : database path is exist, so return directly");
                                    return 1;
                                }
                            }
                            if (key == null) {
                                cacheStore = new AndroidSqlPersistentStore(appCtx, modelPath, uri.toString(), storeConfiguration);
                            } else {
                                storeConfiguration.setDatabaseType(Configuration.CONFIGURATION_DATABASE_ODMF);
                                storeConfiguration.setStorageMode(Configuration.CONFIGURATION_STORAGE_MODE_DISK);
                                cacheStore = new EncryptedAndroidSqlPersistentStore(appCtx, modelPath, uri.toString(), storeConfiguration, key);
                            }
                            for (PersistentStore psVal2 : this.mapUriToPersistentStore.values()) {
                                if (psVal2.getPath() != null && psVal2.equals(cacheStore)) {
                                    LOG.logE("addPersistentStore : database path is exist, so return directly");
                                    cacheStore.close();
                                    return 1;
                                }
                            }
                            this.mapUriToPersistentStore.put(uri.toString(), cacheStore);
                            break;
                        case Configuration.CONFIGURATION_TYPE_PROVIDER:
                            for (PersistentStore psVal3 : this.mapUriToPersistentStore.values()) {
                                if (psVal3.getPath() != null && psVal3.getPath().equals(storeConfiguration.getPath())) {
                                    LOG.logW("addPersistentStore : database path exists already, so return directly");
                                    return 1;
                                }
                            }
                            break;
                    }
                } else {
                    LOG.logE("addPersistentStore : uri is exist, so return directly");
                    return 1;
                }
            }
        } else {
            LOG.logE("addPersistentStore : The parameters has null.");
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public int removePersistentStore(Uri uri) {
        int i = -1;
        if (uri == null) {
            LOG.logE("removePersistentStore : The parameter uri is null.");
        } else {
            synchronized (this.persistentStoreLock) {
                PersistentStore ps = this.mapUriToPersistentStore.get(uri.toString());
                if (ps == null) {
                    LOG.logE("the uri " + uri.toString() + "with the corresponding PersistentStore not exist.");
                } else {
                    if (ps.getPath() != null) {
                        Iterator<Map.Entry<ObjectContext, PersistentStore>> it = this.mapContextToPersistentStore.entrySet().iterator();
                        while (true) {
                            if (it.hasNext()) {
                                if (ps.equals(it.next().getValue())) {
                                    LOG.logE("because persistentStore is used by other, so cannot remove the correspondence between uri and persistentStore.");
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    }
                    if (this.mapUriToPersistentStore.containsKey(uri.toString())) {
                        this.mapUriToPersistentStore.remove(uri.toString());
                        ps.close();
                        i = 0;
                    }
                }
            }
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    public int connectPersistentStore(Uri uri, ObjectContext context) {
        int i = -1;
        if (uri == null || context == null) {
            LOG.logE("connectPersistentStore : The parameters has null.");
        } else {
            synchronized (this.persistentStoreLock) {
                PersistentStore ps = this.mapUriToPersistentStore.get(uri.toString());
                if (ps != null) {
                    this.mapContextToPersistentStore.putIfAbsent(context, ps);
                    i = 0;
                } else {
                    LOG.logE("connectPersistentStore : The context can not connect to the persistentStore correspond to the input uri.");
                }
            }
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    public PersistentStore getPersistentStore(Uri uri) {
        if (uri == null) {
            LOG.logE("getPersistentStore : The parameter uri is null.");
            return null;
        }
        synchronized (this.persistentStoreLock) {
            PersistentStore ps = this.mapUriToPersistentStore.get(uri.toString());
            if (ps != null) {
                return ps;
            }
            LOG.logE("getPersistentStore : Can not get the persistentStore correspond to the input uri.");
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public int disconnectPersistentStore(Uri uri, ObjectContext context) {
        int i = -1;
        if (uri == null || context == null) {
            LOG.logE("disconnectPersistentStore : The parameters has null.");
        } else {
            synchronized (this.persistentStoreLock) {
                if (this.mapContextToPersistentStore.containsKey(context)) {
                    this.mapContextToPersistentStore.remove(context);
                    removePersistentStore(uri);
                    i = 0;
                }
            }
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    public <T extends ManagedObject> List<T> executeFetchRequest(FetchRequest<T> request, ObjectContext context) {
        if (request == null) {
            LOG.logE("executeFetchRequest : The parameter has null.");
            throw new ODMFIllegalArgumentException("The parameter has null.");
        }
        List<T> managedObjectsList = this.mapContextToPersistentStore.get(context).executeFetchRequest(request, context);
        int size = managedObjectsList.size();
        for (int i = 0; i < size; i++) {
            ManagedObject managedObject = (ManagedObject) managedObjectsList.get(i);
            managedObject.setObjectContext(context);
            putObjectIntoCache(managedObject);
        }
        return managedObjectsList;
    }

    /* access modifiers changed from: package-private */
    public List<ObjectId> executeFetchRequestGetObjectID(FetchRequest request, ObjectContext context) {
        return getPersistentStore(context).executeFetchRequestGetObjectID(request);
    }

    /* access modifiers changed from: protected */
    public Cursor executeFetchRequestGetCursor(FetchRequest request, ObjectContext objectContext) {
        return getPersistentStore(objectContext).executeFetchRequestGetCursor(request);
    }

    /* access modifiers changed from: package-private */
    public long getPersistentStoreVersion(ObjectContext context) {
        return getPersistentStore(context).getVersion();
    }

    /* access modifiers changed from: package-private */
    public void save(SaveRequest saveRequest, ObjectContext context) {
        if (saveRequest == null) {
            LOG.logE("save : The parameter saveRequest is null.");
            return;
        }
        PersistentStore ps = getPersistentStore(context);
        if (ps.inTransaction()) {
            ps.executeSaveRequest(saveRequest);
            successFinishWork(saveRequest);
            return;
        }
        ps.executeSaveRequestWithTransaction(saveRequest);
        successFinishWork(saveRequest);
        List<ObjectContext> notifyTarget = hasListeners(ps.getUriString());
        if (notifyTarget.size() != 0) {
            sendMessageToObjectContext(saveRequest, context, ps.getUriString(), notifyTarget);
        }
    }

    /* access modifiers changed from: package-private */
    public List<ObjectContext> hasListeners(ObjectContext objectContext) {
        return this.notifyManager.hasListeners(getPersistentStore(objectContext).getUriString());
    }

    /* access modifiers changed from: package-private */
    public List<ObjectContext> hasListeners(String uriString) {
        return this.notifyManager.hasListeners(uriString);
    }

    private void sendMessageToObjectContext(SaveRequest saveRequest, ObjectContext context, String psUri, List<ObjectContext> notifyTarget) {
        this.notifyManager.addMessageToQueue(saveRequest, context, psUri, notifyTarget);
    }

    private void successFinishWork(SaveRequest saveRequest) {
        if (CacheConfig.getDefault().isOpenObjectCache()) {
            for (ManagedObject tItDeleted : saveRequest.getDeletedObjects()) {
                removeObjectIntoCache(tItDeleted);
            }
            for (ManagedObject tInsertedObj : saveRequest.getInsertedObjects()) {
                putObjectIntoCache(tInsertedObj);
            }
            for (ManagedObject tUpdateObj : saveRequest.getUpdatedObjects()) {
                putObjectIntoCache(tUpdateObj);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ManagedObject getObjectValues(ObjectId objectID, ObjectContext objectContext) {
        if (objectID == null) {
            LOG.logE("getObjectValues : The parameter objectID is null.");
            return null;
        }
        ManagedObject cacheObject = getObjectFromCache(objectID);
        if (cacheObject != null && (!cacheObject.isDirty() || cacheObject.getLastObjectContext() == objectContext)) {
            return cacheObject;
        }
        PersistentStore ps = getPersistentStore(objectContext);
        if (!objectID.getUriString().equals(ps.getUriString())) {
            objectID.setUriString(ps.getUriString());
        }
        ManagedObject object = ps.getObjectValues(objectID);
        if (object == null) {
            LOG.logE("getObjectValues : Can not get the objectNode correspond to the input objectID.");
            return null;
        }
        object.setState(4);
        object.setObjectContext(objectContext);
        return object;
    }

    /* access modifiers changed from: protected */
    public void beginTransaction(ObjectContext context) {
        getPersistentStore(context).beginTransaction();
    }

    /* access modifiers changed from: protected */
    public boolean inTransaction(ObjectContext context) {
        return getPersistentStore(context).inTransaction();
    }

    /* access modifiers changed from: protected */
    public void commit(ObjectContext context, SaveRequest request) {
        PersistentStore ps = getPersistentStore(context);
        ps.commit();
        if (request != null) {
            List<ObjectContext> notifyTarget = this.notifyManager.hasListeners(ps.getUriString());
            if (notifyTarget.size() != 0) {
                sendMessageToObjectContext(request, context, ps.getUriString(), notifyTarget);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void rollback(ObjectContext context) {
        getPersistentStore(context).rollback();
    }

    /* access modifiers changed from: package-private */
    public void clearTable(ObjectContext objectContext, String entityName) {
        PersistentStore ps = getPersistentStore(objectContext);
        ps.clearTable(entityName);
        List<ObjectContext> notifyTarget = hasListeners(ps.getUriString());
        if (notifyTarget.size() != 0) {
            this.notifyManager.addMessageToQueue(objectContext, entityName, ps.getUriString(), notifyTarget);
        }
    }

    /* access modifiers changed from: protected */
    public ManagedObject getToOneRelationshipValue(String fieldName, ManagedObject object, ObjectContext objectContext) {
        if (!TextUtils.isEmpty(fieldName) && object != null) {
            return getPersistentStore(objectContext).getToOneRelationshipValue(fieldName, object, objectContext);
        }
        LOG.logE("getToOneRelationshipValue : The parameters has null.");
        throw new ODMFIllegalArgumentException("The parameters has null.");
    }

    /* access modifiers changed from: protected */
    public List<ManagedObject> getToManyRelationshipValue(String fieldName, ManagedObject object, ObjectContext objectContext) {
        if (!TextUtils.isEmpty(fieldName) && object != null) {
            return getPersistentStore(objectContext).getToManyRelationshipValue(fieldName, object, objectContext);
        }
        LOG.logE("getToManyRelationshipValue : The parameters has null.");
        throw new ODMFIllegalArgumentException("The parameters has null.");
    }

    /* access modifiers changed from: protected */
    public ManagedObject remoteGetToOneRelationshipValue(String fieldName, ObjectId objectID, ObjectContext objectContext) {
        if (!TextUtils.isEmpty(fieldName) && objectID != null) {
            return getPersistentStore(objectContext).remoteGetToOneRelationshipValue(fieldName, objectID, objectContext);
        }
        LOG.logE("remoteGetToOneRelationshipValue : The parameters has null.");
        throw new ODMFIllegalArgumentException("The parameters has null.");
    }

    /* access modifiers changed from: protected */
    public List<ManagedObject> remoteGetToManyRelationshipValue(String fieldName, ObjectId objectID, ObjectContext objectContext) {
        if (!TextUtils.isEmpty(fieldName) && objectID != null) {
            return getPersistentStore(objectContext).remoteGetToManyRelationshipValue(fieldName, objectID, objectContext);
        }
        LOG.logE("remoteGetToManyRelationshipValue : The parameters has null.");
        throw new ODMFIllegalArgumentException("The parameters has null.");
    }

    /* access modifiers changed from: protected */
    public Cursor executeRawQuerySQL(String sql, ObjectContext objectContext) {
        if (!TextUtils.isEmpty(sql)) {
            return getPersistentStore(objectContext).executeRawQuerySQL(sql);
        }
        LOG.logE("executeRawQuerySQL : The parameter sql is null.");
        throw new ODMFIllegalArgumentException("The parameter sql is null.");
    }

    /* access modifiers changed from: protected */
    public void executeRawSQL(String sql, ObjectContext objectContext) {
        if (TextUtils.isEmpty(sql)) {
            LOG.logE("executeRawSQL : The parameter sql is null.");
            throw new ODMFIllegalArgumentException("The parameter sql is null.");
        } else {
            getPersistentStore(objectContext).executeRawSQL(sql);
        }
    }

    /* access modifiers changed from: protected */
    public Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit, ObjectContext objectContext) {
        return getPersistentStore(objectContext).query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    /* access modifiers changed from: package-private */
    public ODMFCache<ObjectId, ManagedObject> getObjectsCache() {
        return this.objectsCache;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        return;
     */
    public void createObjectsCache() {
        synchronized (this.cacheLock) {
            if (this.objectsCache == null) {
                CacheConfig cacheConfig = CacheConfig.getDefault();
                if (cacheConfig.isOpenObjectCache()) {
                    this.objectsCache = new ODMFCache<>(cacheConfig.getObjectCacheNum());
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void putObjectIntoCache(ManagedObject managedObject) {
        if (CacheConfig.getDefault().isOpenObjectCache()) {
            ManagedObject temp = this.objectsCache.get(managedObject.getObjectId());
            if (temp == null || !temp.isDirty()) {
                this.objectsCache.put(managedObject.getObjectId(), managedObject);
                managedObject.setDirty(1);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ManagedObject getObjectFromCache(ObjectId objectId) {
        if (CacheConfig.getDefault().isOpenObjectCache()) {
            return this.objectsCache.get(objectId);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void removeObjectIntoCache(ManagedObject managedObject) {
        if (CacheConfig.getDefault().isOpenObjectCache()) {
            this.objectsCache.remove(managedObject.getObjectId());
        }
    }

    /* access modifiers changed from: package-private */
    public List<Object> executeFetchRequestWithAggregateFunction(FetchRequest fetchRequest, ObjectContext objectContext) {
        if (fetchRequest != null) {
            return getPersistentStore(objectContext).executeFetchRequestWithAggregateFunction(fetchRequest);
        }
        LOG.logE("executeFetchRequestWithAggregateFunction : The input parameter fetchRequest is null.");
        throw new ODMFIllegalArgumentException("The input parameter fetchRequest is null.");
    }

    /* access modifiers changed from: package-private */
    public String getDBVersion(ObjectContext objectContext) {
        return getPersistentStore(objectContext).getCurrentDBVersion();
    }

    /* access modifiers changed from: protected */
    public String getEntityVersion(String tableName, ObjectContext objectContext) {
        return getPersistentStore(objectContext).getCurrentEntityVersion(tableName);
    }

    /* access modifiers changed from: package-private */
    public void setDBVersions(String dbVersion, int dbVersionCode, ObjectContext objectContext) {
        getPersistentStore(objectContext).setNewDBVersions(dbVersion, dbVersionCode);
    }

    /* access modifiers changed from: protected */
    public void setEntityVersions(String tableName, String entityVersion, int entityVersionCode, ObjectContext objectContext) {
        getPersistentStore(objectContext).setNewEntityVersions(tableName, entityVersion, entityVersionCode);
    }

    /* access modifiers changed from: package-private */
    public int getDBVersionCode(ObjectContext objectContext) {
        return getPersistentStore(objectContext).getCurrentDBVersionCode();
    }

    /* access modifiers changed from: protected */
    public int getEntityVersionCode(String tableName, ObjectContext objectContext) {
        return getPersistentStore(objectContext).getCurrentEntityVersionCode(tableName);
    }

    /* access modifiers changed from: package-private */
    public void exportDatabase(String newDBName, byte[] newKey, ObjectContext objectContext) {
        getPersistentStore(objectContext).exportDatabase(newDBName, newKey);
    }

    /* access modifiers changed from: package-private */
    public void resetMetadata(ObjectContext objectContext) {
        getPersistentStore(objectContext).resetMetadata();
    }

    private PersistentStore getPersistentStore(ObjectContext objectContext) {
        if (objectContext == null) {
            LOG.logE("getPersistentStore : The input parameter objectContext is null.");
            throw new ODMFIllegalArgumentException("The input parameter objectContext is null.");
        }
        PersistentStore ps = this.mapContextToPersistentStore.get(objectContext);
        if (ps != null) {
            return ps;
        }
        LOG.logE("not found persistentStore.");
        throw new ODMFRuntimeException("The persistentStore correspond to the ObjectContext does not found.This may because you had close this object context.");
    }

    public List<? extends Attribute> getEntityAttributes(ObjectContext objectContext, String entityName) {
        return getPersistentStore(objectContext).getEntityAttributes(entityName);
    }
}

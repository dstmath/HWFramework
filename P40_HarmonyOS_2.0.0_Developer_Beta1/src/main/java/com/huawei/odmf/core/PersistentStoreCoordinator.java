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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class PersistentStoreCoordinator {
    private static final Singleton<PersistentStoreCoordinator> DEFAULT = new Singleton<PersistentStoreCoordinator>() {
        /* class com.huawei.odmf.core.PersistentStoreCoordinator.AnonymousClass1 */

        @Override // com.huawei.odmf.utils.Singleton
        public PersistentStoreCoordinator create() {
            return new PersistentStoreCoordinator();
        }
    };
    private static final int EXIST = 1;
    private static final int FAIL = -1;
    private static final int SUCCESS = 0;
    private final Object cacheLock;
    private final ConcurrentHashMap<ObjectContext, PersistentStore> contextToStore;
    private NotifyManager notifyManager;
    private ODMFCache<ObjectId, ManagedObject> objectsCache;
    private final Object persistentStoreLock;
    private final ConcurrentHashMap<String, PersistentStore> uriToStore;

    private PersistentStoreCoordinator() {
        this.uriToStore = new ConcurrentHashMap<>();
        this.contextToStore = new ConcurrentHashMap<>();
        this.persistentStoreLock = new Object();
        this.cacheLock = new Object();
        this.objectsCache = null;
        CacheConfig cacheConfig = CacheConfig.getDefault();
        if (cacheConfig.isOpenObjectCache()) {
            this.objectsCache = new ODMFCache<>(cacheConfig.getObjectCacheNum());
        }
        this.notifyManager = new NotifyManager();
    }

    public static PersistentStoreCoordinator getDefault() {
        return DEFAULT.get();
    }

    /* access modifiers changed from: package-private */
    public ConcurrentHashMap<String, PersistentStore> getUriToStore() {
        return this.uriToStore;
    }

    /* access modifiers changed from: package-private */
    public ConcurrentHashMap<ObjectContext, PersistentStore> getContextToStore() {
        return this.contextToStore;
    }

    /* access modifiers changed from: package-private */
    public int createPersistentStore(Uri uri, Configuration configuration, Context context, String str) {
        return addPersistentStore(uri, configuration, context, str, null);
    }

    /* access modifiers changed from: package-private */
    public int createEncryptedPersistentStore(Uri uri, Configuration configuration, Context context, String str, byte[] bArr) {
        return addPersistentStore(uri, configuration, context, str, bArr);
    }

    /*  JADX ERROR: StackOverflowError in pass: MarkFinallyVisitor
        java.lang.StackOverflowError
        	at jadx.core.dex.nodes.InsnNode.isSame(InsnNode.java:303)
        	at jadx.core.dex.instructions.IfNode.isSame(IfNode.java:122)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.sameInsns(MarkFinallyVisitor.java:451)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.compareBlocks(MarkFinallyVisitor.java:436)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.checkBlocksTree(MarkFinallyVisitor.java:408)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.checkBlocksTree(MarkFinallyVisitor.java:411)
        */
    int createCrossPersistentStore(android.net.Uri r11, com.huawei.odmf.core.Configuration r12, android.content.Context r13, java.util.Map<android.net.Uri, byte[]> r14) {
        /*
            r10 = this;
            if (r11 == 0) goto L_0x00dc
            if (r13 == 0) goto L_0x00dc
            if (r14 == 0) goto L_0x00dc
            int r0 = r14.size()
            if (r0 == 0) goto L_0x00cf
            java.util.ArrayList r6 = new java.util.ArrayList
            r6.<init>()
            java.util.ArrayList r7 = new java.util.ArrayList
            r7.<init>()
            r0 = 0
            java.lang.Object r8 = r10.persistentStoreLock     // Catch:{ all -> 0x00a8 }
            monitor-enter(r8)     // Catch:{ all -> 0x00a8 }
            java.util.Set r1 = r14.entrySet()     // Catch:{ all -> 0x00a5 }
            java.util.Iterator r1 = r1.iterator()     // Catch:{ all -> 0x00a5 }
        L_0x0022:
            boolean r2 = r1.hasNext()     // Catch:{ all -> 0x00a5 }
            if (r2 == 0) goto L_0x005e
            java.lang.Object r2 = r1.next()     // Catch:{ all -> 0x00a5 }
            java.util.Map$Entry r2 = (java.util.Map.Entry) r2     // Catch:{ all -> 0x00a5 }
            java.util.concurrent.ConcurrentHashMap<java.lang.String, com.huawei.odmf.core.PersistentStore> r3 = r10.uriToStore     // Catch:{ all -> 0x00a5 }
            java.lang.Object r4 = r2.getKey()     // Catch:{ all -> 0x00a5 }
            android.net.Uri r4 = (android.net.Uri) r4     // Catch:{ all -> 0x00a5 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x00a5 }
            java.lang.Object r3 = r3.get(r4)     // Catch:{ all -> 0x00a5 }
            com.huawei.odmf.core.PersistentStore r3 = (com.huawei.odmf.core.PersistentStore) r3     // Catch:{ all -> 0x00a5 }
            if (r3 == 0) goto L_0x0051
            java.lang.String r3 = r3.getPath()     // Catch:{ all -> 0x00a5 }
            r6.add(r3)     // Catch:{ all -> 0x00a5 }
            java.lang.Object r2 = r2.getValue()     // Catch:{ all -> 0x00a5 }
            r7.add(r2)     // Catch:{ all -> 0x00a5 }
            goto L_0x0022
        L_0x0051:
            java.lang.String r11 = "createCrossPersistentStore : Some uri in uriList does not indicates a persistentStore."
            com.huawei.odmf.utils.LOG.logE(r11)     // Catch:{ all -> 0x00a5 }
            com.huawei.odmf.exception.ODMFIllegalArgumentException r11 = new com.huawei.odmf.exception.ODMFIllegalArgumentException     // Catch:{ all -> 0x00a5 }
            java.lang.String r12 = "Some uri in uriList does not indicates a persistentStore."
            r11.<init>(r12)     // Catch:{ all -> 0x00a5 }
            throw r11     // Catch:{ all -> 0x00a5 }
        L_0x005e:
            r1 = 301(0x12d, float:4.22E-43)
            r12.setDatabaseType(r1)     // Catch:{ all -> 0x00a5 }
            r1 = 401(0x191, float:5.62E-43)
            r12.setStorageMode(r1)     // Catch:{ all -> 0x00a5 }
            com.huawei.odmf.core.CrossPersistentStore r9 = new com.huawei.odmf.core.CrossPersistentStore     // Catch:{ all -> 0x00a5 }
            r3 = 0
            java.lang.String r4 = r11.toString()     // Catch:{ all -> 0x00a5 }
            r1 = r9
            r2 = r13
            r5 = r12
            r1.<init>(r2, r3, r4, r5, r6, r7)     // Catch:{ all -> 0x00a5 }
            java.util.concurrent.ConcurrentHashMap<java.lang.String, com.huawei.odmf.core.PersistentStore> r12 = r10.uriToStore     // Catch:{ all -> 0x00a5 }
            java.lang.String r11 = r11.toString()     // Catch:{ all -> 0x00a5 }
            r12.put(r11, r9)     // Catch:{ all -> 0x00a5 }
            monitor-exit(r8)     // Catch:{ all -> 0x00a5 }
            java.util.Set r11 = r14.entrySet()
            java.util.Iterator r11 = r11.iterator()
        L_0x0087:
            boolean r12 = r11.hasNext()
            if (r12 == 0) goto L_0x00a4
            java.lang.Object r12 = r11.next()
            java.util.Map$Entry r12 = (java.util.Map.Entry) r12
            java.lang.Object r12 = r12.getValue()
            byte[] r12 = (byte[]) r12
            if (r12 == 0) goto L_0x0087
            r13 = r0
        L_0x009c:
            int r14 = r12.length
            if (r13 >= r14) goto L_0x0087
            r12[r13] = r0
            int r13 = r13 + 1
            goto L_0x009c
        L_0x00a4:
            return r0
        L_0x00a5:
            r11 = move-exception
            monitor-exit(r8)
            throw r11
        L_0x00a8:
            r11 = move-exception
            java.util.Set r12 = r14.entrySet()
            java.util.Iterator r12 = r12.iterator()
        L_0x00b1:
            boolean r13 = r12.hasNext()
            if (r13 == 0) goto L_0x00ce
            java.lang.Object r13 = r12.next()
            java.util.Map$Entry r13 = (java.util.Map.Entry) r13
            java.lang.Object r13 = r13.getValue()
            byte[] r13 = (byte[]) r13
            if (r13 == 0) goto L_0x00b1
            r14 = r0
        L_0x00c6:
            int r1 = r13.length
            if (r14 >= r1) goto L_0x00b1
            r13[r14] = r0
            int r14 = r14 + 1
            goto L_0x00c6
        L_0x00ce:
            throw r11
        L_0x00cf:
            java.lang.String r11 = "createCrossPersistentStore : The uriMap contains nothing."
            com.huawei.odmf.utils.LOG.logE(r11)
            com.huawei.odmf.exception.ODMFIllegalArgumentException r11 = new com.huawei.odmf.exception.ODMFIllegalArgumentException
            java.lang.String r12 = "The uriList contains nothing."
            r11.<init>(r12)
            throw r11
        L_0x00dc:
            java.lang.String r11 = "createCrossPersistentStore : The parameters has null."
            com.huawei.odmf.utils.LOG.logE(r11)
            com.huawei.odmf.exception.ODMFIllegalArgumentException r11 = new com.huawei.odmf.exception.ODMFIllegalArgumentException
            java.lang.String r12 = "The input parameter of uri, appCtx and the uriList has null."
            r11.<init>(r12)
            throw r11
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.odmf.core.PersistentStoreCoordinator.createCrossPersistentStore(android.net.Uri, com.huawei.odmf.core.Configuration, android.content.Context, java.util.Map):int");
    }

    private int addPersistentStore(Uri uri, Configuration configuration, Context context, String str, byte[] bArr) {
        AndroidSqlPersistentStore androidSqlPersistentStore;
        if (uri == null || configuration == null || context == null) {
            LOG.logE("addPersistentStore : The parameters has null.");
            return -1;
        }
        synchronized (this.persistentStoreLock) {
            if (this.uriToStore.containsKey(uri.toString())) {
                LOG.logE("addPersistentStore : uri is exist, so return directly");
                return 1;
            }
            int type = configuration.getType();
            if (type != 200) {
                if (type == 201) {
                    if (isStoreExist(configuration.getPath())) {
                        LOG.logW("addPersistentStore : database path exists already, so return directly");
                        return 1;
                    }
                }
            } else if (isStoreExist(configuration.getPath())) {
                LOG.logW("addPersistentStore : database path exists already, so return directly");
                return 1;
            } else {
                if (bArr == null) {
                    androidSqlPersistentStore = new AndroidSqlPersistentStore(context, str, uri.toString(), configuration);
                } else {
                    configuration.setDatabaseType(Configuration.CONFIGURATION_DATABASE_ODMF);
                    configuration.setStorageMode(Configuration.CONFIGURATION_STORAGE_MODE_DISK);
                    androidSqlPersistentStore = new EncryptedAndroidSqlPersistentStore(context, str, uri.toString(), configuration, bArr);
                }
                for (PersistentStore persistentStore : this.uriToStore.values()) {
                    if (persistentStore.getPath() != null && persistentStore.equals(androidSqlPersistentStore)) {
                        LOG.logE("addPersistentStore : database path is exist, so return directly");
                        androidSqlPersistentStore.close();
                        return 1;
                    }
                }
                this.uriToStore.put(uri.toString(), androidSqlPersistentStore);
            }
            return 0;
        }
    }

    private boolean isStoreExist(String str) {
        synchronized (this.persistentStoreLock) {
            for (PersistentStore persistentStore : this.uriToStore.values()) {
                if (persistentStore.getPath() != null && persistentStore.getPath().equals(str)) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public int removePersistentStore(Uri uri) {
        if (uri == null) {
            LOG.logE("removePersistentStore : The parameter uri is null.");
            return -1;
        }
        synchronized (this.persistentStoreLock) {
            PersistentStore persistentStore = this.uriToStore.get(uri.toString());
            if (persistentStore == null) {
                LOG.logE("the uri " + uri.toString() + "with the corresponding PersistentStore not exist.");
                return -1;
            }
            if (persistentStore.getPath() != null) {
                for (Map.Entry<ObjectContext, PersistentStore> entry : this.contextToStore.entrySet()) {
                    if (persistentStore.equals(entry.getValue())) {
                        LOG.logE("Because the persistentStore is used by other context, so cannot remove the persistentStore currently.");
                        return -1;
                    }
                }
            }
            if (!this.uriToStore.containsKey(uri.toString())) {
                return -1;
            }
            this.uriToStore.remove(uri.toString());
            persistentStore.close();
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public int connectPersistentStore(Uri uri, ObjectContext objectContext) {
        if (uri == null || objectContext == null) {
            LOG.logE("connectPersistentStore : The parameters has null.");
            return -1;
        }
        synchronized (this.persistentStoreLock) {
            PersistentStore persistentStore = this.uriToStore.get(uri.toString());
            if (persistentStore != null) {
                this.contextToStore.putIfAbsent(objectContext, persistentStore);
                return 0;
            }
            LOG.logE("connectPersistentStore : The context can not connect to the persistentStore correspond to the input uri.");
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public PersistentStore getPersistentStore(Uri uri) {
        if (uri == null) {
            LOG.logE("getPersistentStore : The parameter uri is null.");
            return null;
        }
        synchronized (this.persistentStoreLock) {
            PersistentStore persistentStore = this.uriToStore.get(uri.toString());
            if (persistentStore != null) {
                return persistentStore;
            }
            LOG.logE("getPersistentStore : Can not to get the persistentStore correspond to the input uri.");
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public int disconnectPersistentStore(Uri uri, ObjectContext objectContext) {
        if (uri == null || objectContext == null) {
            LOG.logE("disconnectPersistentStore : The parameters has null.");
            return -1;
        }
        synchronized (this.persistentStoreLock) {
            if (!this.contextToStore.containsKey(objectContext)) {
                return -1;
            }
            this.contextToStore.remove(objectContext);
            removePersistentStore(uri);
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public <T extends ManagedObject> List<T> executeFetchRequest(FetchRequest<T> fetchRequest, ObjectContext objectContext) {
        if (fetchRequest != null) {
            List<T> executeFetchRequest = getPersistentStore(objectContext).executeFetchRequest(fetchRequest, objectContext);
            int size = executeFetchRequest.size();
            for (int i = 0; i < size; i++) {
                T t = executeFetchRequest.get(i);
                t.setObjectContext(objectContext);
                putObjectIntoCache(t);
            }
            return executeFetchRequest;
        }
        LOG.logE("executeFetchRequest : The parameter has null.");
        throw new ODMFIllegalArgumentException("The parameter has null.");
    }

    /* access modifiers changed from: package-private */
    public List<ObjectId> executeFetchRequestGetObjectId(FetchRequest fetchRequest, ObjectContext objectContext) {
        return getPersistentStore(objectContext).executeFetchRequestGetObjectId(fetchRequest);
    }

    /* access modifiers changed from: package-private */
    public Cursor executeFetchRequestGetCursor(FetchRequest fetchRequest, ObjectContext objectContext) {
        return getPersistentStore(objectContext).executeFetchRequestGetCursor(fetchRequest);
    }

    /* access modifiers changed from: package-private */
    public long getPersistentStoreVersion(ObjectContext objectContext) {
        return getPersistentStore(objectContext).getVersion();
    }

    /* access modifiers changed from: package-private */
    public void save(SaveRequest saveRequest, ObjectContext objectContext) {
        if (saveRequest == null) {
            LOG.logE("save : The parameter saveRequest is null.");
            return;
        }
        PersistentStore persistentStore = getPersistentStore(objectContext);
        if (persistentStore.inTransaction()) {
            persistentStore.executeSaveRequest(saveRequest);
            successFinishWork(saveRequest);
            return;
        }
        persistentStore.executeSaveRequestWithTransaction(saveRequest);
        successFinishWork(saveRequest);
        List<ObjectContext> hasListeners = hasListeners(persistentStore.getUriString());
        if (hasListeners.size() != 0) {
            sendMessageToObjectContext(saveRequest, objectContext, persistentStore.getUriString(), hasListeners);
        }
    }

    /* access modifiers changed from: package-private */
    public List<ObjectContext> hasListeners(ObjectContext objectContext) {
        return this.notifyManager.hasListeners(getPersistentStore(objectContext).getUriString());
    }

    /* access modifiers changed from: package-private */
    public List<ObjectContext> hasListeners(String str) {
        return this.notifyManager.hasListeners(str);
    }

    private void sendMessageToObjectContext(SaveRequest saveRequest, ObjectContext objectContext, String str, List<ObjectContext> list) {
        this.notifyManager.addMessageToQueue(saveRequest, objectContext, str, list);
    }

    private void successFinishWork(SaveRequest saveRequest) {
        if (CacheConfig.getDefault().isOpenObjectCache()) {
            for (ManagedObject managedObject : saveRequest.getDeletedObjects()) {
                removeObjectInCache(managedObject);
            }
            for (ManagedObject managedObject2 : saveRequest.getInsertedObjects()) {
                putObjectIntoCache(managedObject2);
            }
            for (ManagedObject managedObject3 : saveRequest.getUpdatedObjects()) {
                putObjectIntoCache(managedObject3);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ManagedObject getObjectValues(ObjectId objectId, ObjectContext objectContext) {
        if (objectId == null) {
            LOG.logE("getObjectValues : The parameter objectID is null.");
            return null;
        }
        ManagedObject objectFromCache = getObjectFromCache(objectId);
        if (objectFromCache != null && (!objectFromCache.isDirty() || objectFromCache.getLastObjectContext() == objectContext)) {
            return objectFromCache;
        }
        PersistentStore persistentStore = getPersistentStore(objectContext);
        if (!objectId.getUriString().equals(persistentStore.getUriString())) {
            objectId.setUriString(persistentStore.getUriString());
        }
        ManagedObject objectValues = persistentStore.getObjectValues(objectId);
        if (objectValues == null) {
            LOG.logE("getObjectValues : Can not get the object correspond to the input objectID.");
            return null;
        }
        objectValues.setState(4);
        objectValues.setObjectContext(objectContext);
        return objectValues;
    }

    /* access modifiers changed from: package-private */
    public void beginTransaction(ObjectContext objectContext) {
        getPersistentStore(objectContext).beginTransaction();
    }

    /* access modifiers changed from: package-private */
    public boolean inTransaction(ObjectContext objectContext) {
        return getPersistentStore(objectContext).inTransaction();
    }

    /* access modifiers changed from: package-private */
    public void commit(ObjectContext objectContext, SaveRequest saveRequest) {
        PersistentStore persistentStore = getPersistentStore(objectContext);
        persistentStore.commit();
        if (saveRequest != null) {
            List<ObjectContext> hasListeners = this.notifyManager.hasListeners(persistentStore.getUriString());
            if (hasListeners.size() != 0) {
                sendMessageToObjectContext(saveRequest, objectContext, persistentStore.getUriString(), hasListeners);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void rollback(ObjectContext objectContext) {
        getPersistentStore(objectContext).rollback();
    }

    /* access modifiers changed from: package-private */
    public void clearTable(ObjectContext objectContext, String str) {
        PersistentStore persistentStore = getPersistentStore(objectContext);
        persistentStore.clearTable(str);
        List<ObjectContext> hasListeners = hasListeners(persistentStore.getUriString());
        if (hasListeners.size() != 0) {
            this.notifyManager.addMessageToQueue(objectContext, str, persistentStore.getUriString(), hasListeners);
        }
    }

    /* access modifiers changed from: package-private */
    public ManagedObject getToOneRelationshipValue(String str, ManagedObject managedObject, ObjectContext objectContext) {
        if (!TextUtils.isEmpty(str) && managedObject != null) {
            return getPersistentStore(objectContext).getToOneRelationshipValue(str, managedObject, objectContext);
        }
        LOG.logE("getToOneRelationshipValue : The parameters has null.");
        throw new ODMFIllegalArgumentException("The parameters has null.");
    }

    /* access modifiers changed from: package-private */
    public List<ManagedObject> getToManyRelationshipValue(String str, ManagedObject managedObject, ObjectContext objectContext) {
        if (!TextUtils.isEmpty(str) && managedObject != null) {
            return getPersistentStore(objectContext).getToManyRelationshipValue(str, managedObject, objectContext);
        }
        LOG.logE("getToManyRelationshipValue : The parameters has null.");
        throw new ODMFIllegalArgumentException("The parameters has null.");
    }

    /* access modifiers changed from: package-private */
    public ManagedObject remoteGetToOneRelationshipValue(String str, ObjectId objectId, ObjectContext objectContext) {
        if (!TextUtils.isEmpty(str) && objectId != null) {
            return getPersistentStore(objectContext).remoteGetToOneRelationshipValue(str, objectId, objectContext);
        }
        LOG.logE("remoteGetToOneRelationshipValue : The parameters has null.");
        throw new ODMFIllegalArgumentException("The parameters has null.");
    }

    /* access modifiers changed from: package-private */
    public List<ManagedObject> remoteGetToManyRelationshipValue(String str, ObjectId objectId, ObjectContext objectContext) {
        if (!TextUtils.isEmpty(str) && objectId != null) {
            return getPersistentStore(objectContext).remoteGetToManyRelationshipValue(str, objectId, objectContext);
        }
        LOG.logE("remoteGetToManyRelationshipValue : The parameters has null.");
        throw new ODMFIllegalArgumentException("The parameters has null.");
    }

    /* access modifiers changed from: package-private */
    public Cursor executeRawQuerySQL(String str, ObjectContext objectContext) {
        if (!TextUtils.isEmpty(str)) {
            return getPersistentStore(objectContext).executeRawQuerySQL(str);
        }
        LOG.logE("executeRawQuerySql : The parameter sql is null.");
        throw new ODMFIllegalArgumentException("The parameter sql is null.");
    }

    /* access modifiers changed from: package-private */
    public void executeRawSQL(String str, ObjectContext objectContext) {
        if (!TextUtils.isEmpty(str)) {
            getPersistentStore(objectContext).executeRawSQL(str);
        } else {
            LOG.logE("executeRawSql : The parameter sql is null.");
            throw new ODMFIllegalArgumentException("The parameter sql is null.");
        }
    }

    /* access modifiers changed from: package-private */
    public Cursor query(boolean z, String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6, ObjectContext objectContext) {
        return getPersistentStore(objectContext).query(z, str, strArr, str2, strArr2, str3, str4, str5, str6);
    }

    /* access modifiers changed from: package-private */
    public ODMFCache<ObjectId, ManagedObject> getObjectsCache() {
        return this.objectsCache;
    }

    /* access modifiers changed from: package-private */
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
            ManagedObject managedObject2 = this.objectsCache.get(managedObject.getObjectId());
            if (managedObject2 == null || !managedObject2.isDirty()) {
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
    public void removeObjectInCache(ManagedObject managedObject) {
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
    public String getDbVersion(ObjectContext objectContext) {
        return getPersistentStore(objectContext).getCurrentDbVersion();
    }

    /* access modifiers changed from: package-private */
    public String getEntityVersion(String str, ObjectContext objectContext) {
        return getPersistentStore(objectContext).getCurrentEntityVersion(str);
    }

    /* access modifiers changed from: package-private */
    public void setDbVersions(String str, int i, ObjectContext objectContext) {
        getPersistentStore(objectContext).setNewDbVersions(str, i);
    }

    /* access modifiers changed from: package-private */
    public void setEntityVersions(String str, String str2, int i, ObjectContext objectContext) {
        getPersistentStore(objectContext).setNewEntityVersions(str, str2, i);
    }

    /* access modifiers changed from: package-private */
    public int getDbVersionCode(ObjectContext objectContext) {
        return getPersistentStore(objectContext).getCurrentDbVersionCode();
    }

    /* access modifiers changed from: package-private */
    public int getEntityVersionCode(String str, ObjectContext objectContext) {
        return getPersistentStore(objectContext).getCurrentEntityVersionCode(str);
    }

    /* access modifiers changed from: package-private */
    public void exportDatabase(String str, byte[] bArr, ObjectContext objectContext) {
        getPersistentStore(objectContext).exportDatabase(str, bArr);
    }

    /* access modifiers changed from: package-private */
    public void resetMetadata(ObjectContext objectContext) {
        getPersistentStore(objectContext).resetMetadata();
    }

    private PersistentStore getPersistentStore(ObjectContext objectContext) {
        if (objectContext != null) {
            PersistentStore persistentStore = this.contextToStore.get(objectContext);
            if (persistentStore != null) {
                return persistentStore;
            }
            LOG.logE("getPersistentStore : not found persistentStore.");
            throw new ODMFRuntimeException("The persistentStore correspond to the ObjectContext does not found.This may because you had close this object context.");
        }
        LOG.logE("getPersistentStore : The input parameter objectContext is null.");
        throw new ODMFIllegalArgumentException("The input parameter objectContext is null.");
    }

    /* access modifiers changed from: package-private */
    public List<? extends Attribute> getEntityAttributes(ObjectContext objectContext, String str) {
        return getPersistentStore(objectContext).getEntityAttributes(str);
    }

    /* access modifiers changed from: package-private */
    public Set<String> getTableInvolvedInSQL(ObjectContext objectContext, String str) {
        return getPersistentStore(objectContext).getTableInvolvedInSQL(str);
    }
}

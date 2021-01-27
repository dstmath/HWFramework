package com.huawei.odmf.core;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.huawei.odmf.exception.ODMFIllegalStateException;
import com.huawei.odmf.exception.ODMFUnsupportedOperationException;
import com.huawei.odmf.model.api.Attribute;
import com.huawei.odmf.model.api.Entity;
import com.huawei.odmf.model.api.ObjectModel;
import com.huawei.odmf.model.api.Relationship;
import com.huawei.odmf.predicate.FetchRequest;
import com.huawei.odmf.predicate.SaveRequest;
import com.huawei.odmf.user.api.ObjectContext;
import com.huawei.odmf.utils.LOG;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PersistentStore {
    protected static final String METADATA_DATABASE_VERSION = "databaseVersion";
    protected static final String METADATA_DATABASE_VERSION_CODE = "databaseVersionCode";
    protected static final String METADATA_ENTITY_VERSION_CODE_SUFFIX = "_versionCode";
    protected static final String METADATA_ENTITY_VERSION_SUFFIX = "_version";
    private static final int MULTIPLIER = 31;
    private final int databaseType;
    private final Object lock;
    private final Map<String, Object> metadata;
    private ObjectModel model;
    private String path;
    private final int storageMode;
    private Uri uri;
    private long version;

    protected PersistentStore() {
        this.lock = new Object();
        this.metadata = new HashMap();
        this.version = 0;
        this.model = null;
        this.databaseType = Configuration.CONFIGURATION_DATABASE_ANDROID;
        this.storageMode = Configuration.CONFIGURATION_STORAGE_MODE_DISK;
        this.path = null;
        this.uri = null;
    }

    protected PersistentStore(String str, int i, int i2, String str2) {
        this.lock = new Object();
        this.metadata = new HashMap();
        this.path = str;
        this.databaseType = i;
        this.storageMode = i2;
        this.version = 0;
        if (str2 != null) {
            this.uri = Uri.parse(str2);
        } else {
            this.uri = null;
        }
    }

    /* access modifiers changed from: protected */
    public void increaseVersion() {
        synchronized (this.lock) {
            this.version++;
        }
    }

    /* access modifiers changed from: protected */
    public long getVersion() {
        long j;
        synchronized (this.lock) {
            j = this.version;
        }
        return j;
    }

    /* access modifiers changed from: protected */
    public Map<String, Object> getMetadata() {
        return this.metadata;
    }

    /* access modifiers changed from: protected */
    public void setMetadata(String str, Object obj) {
        this.metadata.put(str, obj);
    }

    public String getCurrentDbVersion() {
        Object obj = this.metadata.get(METADATA_DATABASE_VERSION);
        if (obj != null) {
            return (String) obj;
        }
        LOG.logE("Execute getCurrentDbVersion Failed : The metadata of database version does not exist.");
        throw new ODMFIllegalStateException("The metadata of database version does not exist.");
    }

    public String getCurrentEntityVersion(String str) {
        Map<String, Object> map = this.metadata;
        Object obj = map.get(str + METADATA_ENTITY_VERSION_SUFFIX);
        if (obj != null) {
            return (String) obj;
        }
        LOG.logE("Execute getCurrentEntityVersion Failed : The metadata of this table does not exist.");
        throw new ODMFIllegalStateException("The metadata of this table does not exist.");
    }

    /* access modifiers changed from: protected */
    public void setNewDbVersions(String str, int i) {
        setMetadata(METADATA_DATABASE_VERSION, str);
        setMetadata(METADATA_DATABASE_VERSION_CODE, Integer.valueOf(i));
        setDbVersions(str, i);
    }

    /* access modifiers changed from: protected */
    public void setNewEntityVersions(String str, String str2, int i) {
        setMetadata(str + METADATA_ENTITY_VERSION_SUFFIX, str2);
        setMetadata(str + METADATA_ENTITY_VERSION_CODE_SUFFIX, Integer.valueOf(i));
        setEntityVersions(str, str2, i);
    }

    public int getCurrentDbVersionCode() {
        Object obj = this.metadata.get(METADATA_DATABASE_VERSION_CODE);
        if (obj != null) {
            return ((Integer) obj).intValue();
        }
        LOG.logE("Execute getCurrentDbVersion Failed : The metadata of database version does not exist.");
        throw new ODMFIllegalStateException("The metadata of database version does not exist.");
    }

    public int getCurrentEntityVersionCode(String str) {
        Map<String, Object> map = this.metadata;
        Object obj = map.get(str + METADATA_ENTITY_VERSION_CODE_SUFFIX);
        if (obj != null) {
            return ((Integer) obj).intValue();
        }
        LOG.logE("Execute getCurrentEntityVersionCode Failed : The metadata of this entity does not exist.");
        throw new ODMFIllegalStateException("The metadata of this entity does not exist.");
    }

    public ObjectModel getModel() {
        return this.model;
    }

    public void setModel(ObjectModel objectModel) {
        this.model = objectModel;
    }

    /* access modifiers changed from: package-private */
    public int getDatabaseType() {
        return this.databaseType;
    }

    /* access modifiers changed from: package-private */
    public String getPath() {
        return this.path;
    }

    public void setPath(String str) {
        this.path = str;
    }

    /* access modifiers changed from: package-private */
    public String getUriString() {
        return this.uri.toString();
    }

    /* access modifiers changed from: package-private */
    public Uri getUri() {
        return this.uri;
    }

    /* access modifiers changed from: package-private */
    public void clearKey(byte[] bArr) {
        if (bArr != null && bArr.length > 0) {
            int length = bArr.length;
            for (int i = 0; i < length; i++) {
                bArr[i] = 0;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ObjectId createObjectId(Entity entity, Object obj) {
        return new AObjectId(entity.getEntityName(), obj, getUriString());
    }

    /* access modifiers changed from: protected */
    public ManagedObject getObjectValues(ObjectId objectId) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method getObjectValues ");
    }

    /* access modifiers changed from: protected */
    public List<ObjectId> getRelationshipObjectId(ObjectId objectId, Relationship relationship) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method getRelationshipObjectId ");
    }

    /* access modifiers changed from: protected */
    public <T extends ManagedObject> List<T> executeFetchRequest(FetchRequest fetchRequest, ObjectContext objectContext) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method executeFetchRequest ");
    }

    /* access modifiers changed from: protected */
    public List<ObjectId> executeFetchRequestGetObjectId(FetchRequest fetchRequest) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method executeFetchRequestGetObjectId ");
    }

    /* access modifiers changed from: protected */
    public Cursor executeFetchRequestGetCursor(FetchRequest fetchRequest) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method executeFetchRequestGetCursor ");
    }

    /* access modifiers changed from: protected */
    public void executeSaveRequest(SaveRequest saveRequest) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method executeSaveRequest ");
    }

    /* access modifiers changed from: protected */
    public void executeSaveRequestWithTransaction(SaveRequest saveRequest) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method executeSaveRequestWithTransaction ");
    }

    /* access modifiers changed from: protected */
    public void close() {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method close ");
    }

    /* access modifiers changed from: protected */
    public void beginTransaction() {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method beginTransaction ");
    }

    /* access modifiers changed from: protected */
    public boolean inTransaction() {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method inTransaction ");
    }

    /* access modifiers changed from: protected */
    public void rollback() {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method rollback ");
    }

    /* access modifiers changed from: protected */
    public void commit() {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method commit ");
    }

    /* access modifiers changed from: protected */
    public void clearTable(String str) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method clearTable ");
    }

    /* access modifiers changed from: protected */
    public void createEntityForMigration(Entity entity) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method createTable ");
    }

    /* access modifiers changed from: protected */
    public void renameEntityForMigration(String str, String str2) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method renameTable ");
    }

    /* access modifiers changed from: protected */
    public void addColumnForMigration(Entity entity, Attribute attribute) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method addColumn ");
    }

    /* access modifiers changed from: protected */
    public void addRelationshipForMigration(Entity entity, Relationship relationship) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method addRelationship ");
    }

    /* access modifiers changed from: protected */
    public void dropEntityForMigration(String str) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method dropTable ");
    }

    /* access modifiers changed from: protected */
    public void insertDataForMigration(String str, ContentValues contentValues) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method insertData ");
    }

    /* access modifiers changed from: protected */
    public void setDbVersions(String str, int i) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method setDBVersion ");
    }

    /* access modifiers changed from: protected */
    public void setEntityVersions(String str, String str2, int i) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method setEntityVersion ");
    }

    /* access modifiers changed from: protected */
    public ManagedObject getToOneRelationshipValue(String str, ManagedObject managedObject, ObjectContext objectContext) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method getToOneRelationshipValue ");
    }

    /* access modifiers changed from: protected */
    public List<ManagedObject> getToManyRelationshipValue(String str, ManagedObject managedObject, ObjectContext objectContext) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method getToManyRelationshipValue ");
    }

    /* access modifiers changed from: protected */
    public Cursor executeRawQuerySQL(String str) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method executeRawQuerySql ");
    }

    /* access modifiers changed from: protected */
    public void executeRawSQL(String str) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method executeRawSql.");
    }

    /* access modifiers changed from: protected */
    public Cursor query(boolean z, String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method query.");
    }

    /* access modifiers changed from: protected */
    public List<Object> executeFetchRequestWithAggregateFunction(FetchRequest fetchRequest) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the query with aggregate function ");
    }

    /* access modifiers changed from: protected */
    public List<ManagedObject> remoteGetToManyRelationshipValue(String str, ObjectId objectId, ObjectContext objectContext) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support remoteGetToManyRelationshipValue function ");
    }

    /* access modifiers changed from: protected */
    public ManagedObject remoteGetToOneRelationshipValue(String str, ObjectId objectId, ObjectContext objectContext) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support remoteGetToOneRelationshipValue function ");
    }

    public void resetDatabaseEncryptKey(byte[] bArr, byte[] bArr2) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support to reset thr key of an encrypted database.");
    }

    public void exportDatabase(String str, byte[] bArr) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support to export database.");
    }

    public void resetMetadata() {
        throw new ODMFUnsupportedOperationException("The persistentStore not support reset metaData.");
    }

    /* access modifiers changed from: package-private */
    public List<? extends Attribute> getEntityAttributes(String str) {
        Entity entity = getModel().getEntity(str);
        if (entity == null) {
            return null;
        }
        return entity.getAttributes();
    }

    public Set<String> getTableInvolvedInSQL(String str) {
        throw new ODMFUnsupportedOperationException("The persistentStore does not support to get tables involved in SQL.");
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PersistentStore persistentStore = (PersistentStore) obj;
        if (getDatabaseType() == persistentStore.getDatabaseType() && this.storageMode == persistentStore.storageMode) {
            return getPath().equals(persistentStore.getPath());
        }
        return false;
    }

    public int hashCode() {
        return (((getDatabaseType() * MULTIPLIER) + this.storageMode) * MULTIPLIER) + getPath().hashCode();
    }
}

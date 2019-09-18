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

public class PersistentStore {
    protected static final String METADATA_DATABASE_VERSION = "databaseVersion";
    protected static final String METADATA_DATABASE_VERSION_CODE = "databaseVersionCode";
    protected static final String METADATA_ENTITY_VERSION_CODE_SUFFIX = "_versionCode";
    protected static final String METADATA_ENTITY_VERSION_SUFFIX = "_version";
    private final int databaseType;
    private final Object lock;
    private final Map<String, Object> metadata;
    protected ObjectModel model;
    protected String path;
    private final int storageMode;
    protected Uri uri;
    private long version;

    protected PersistentStore() {
        this.metadata = new HashMap();
        this.lock = new Object();
        this.version = 0;
        this.model = null;
        this.databaseType = Configuration.CONFIGURATION_DATABASE_ANDROID;
        this.storageMode = Configuration.CONFIGURATION_STORAGE_MODE_DISK;
        this.path = null;
        this.uri = null;
    }

    protected PersistentStore(String path2, int databaseType2, int storageMode2, String uriString) {
        this.metadata = new HashMap();
        this.lock = new Object();
        this.path = path2;
        this.databaseType = databaseType2;
        this.storageMode = storageMode2;
        this.version = 0;
        if (uriString != null) {
            this.uri = Uri.parse(uriString);
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
    public void setMetadata(String key, Object metadata2) {
        this.metadata.put(key, metadata2);
    }

    public String getCurrentDBVersion() {
        Object obj = this.metadata.get(METADATA_DATABASE_VERSION);
        if (obj != null) {
            return (String) obj;
        }
        LOG.logE("Execute getCurrentDBVersion Failed : The metadata of database version does not exist.");
        throw new ODMFIllegalStateException("The metadata of database version does not exist.");
    }

    public String getCurrentEntityVersion(String tableName) {
        Object obj = this.metadata.get(tableName + METADATA_ENTITY_VERSION_SUFFIX);
        if (obj != null) {
            return (String) obj;
        }
        LOG.logE("Execute getCurrentEntityVersion Failed : The metadata of this table does not exist.");
        throw new ODMFIllegalStateException("The metadata of this table does not exist.");
    }

    /* access modifiers changed from: protected */
    public void setNewDBVersions(String newDBVersion, int newDBVersionCode) {
        setMetadata(METADATA_DATABASE_VERSION, newDBVersion);
        setMetadata(METADATA_DATABASE_VERSION_CODE, Integer.valueOf(newDBVersionCode));
        setDBVersions(newDBVersion, newDBVersionCode);
    }

    /* access modifiers changed from: protected */
    public void setNewEntityVersions(String tableName, String newEntityVersion, int newEntityVersionCode) {
        setMetadata(tableName + METADATA_ENTITY_VERSION_SUFFIX, newEntityVersion);
        setMetadata(tableName + METADATA_ENTITY_VERSION_CODE_SUFFIX, Integer.valueOf(newEntityVersionCode));
        setEntityVersions(tableName, newEntityVersion, newEntityVersionCode);
    }

    public int getCurrentDBVersionCode() {
        Object obj = this.metadata.get(METADATA_DATABASE_VERSION_CODE);
        if (obj != null) {
            return ((Integer) obj).intValue();
        }
        LOG.logE("Execute getCurrentDBVersion Failed : The metadata of database version does not exist.");
        throw new ODMFIllegalStateException("The metadata of database version does not exist.");
    }

    public int getCurrentEntityVersionCode(String tableName) {
        Object obj = this.metadata.get(tableName + METADATA_ENTITY_VERSION_CODE_SUFFIX);
        if (obj != null) {
            return ((Integer) obj).intValue();
        }
        LOG.logE("Execute getCurrentEntityVersionCode Failed : The metadata of this entity does not exist.");
        throw new ODMFIllegalStateException("The metadata of this entity does not exist.");
    }

    public ObjectModel getModel() {
        return this.model;
    }

    /* access modifiers changed from: package-private */
    public int getDatabaseType() {
        return this.databaseType;
    }

    /* access modifiers changed from: package-private */
    public String getPath() {
        return this.path;
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
    public void clearKey(byte[] key) {
        if (key != null && key.length > 0) {
            int length = key.length;
            for (int i = 0; i < length; i++) {
                key[i] = 0;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ObjectId createObjectID(Entity entity, Object referenceObject) {
        return new AObjectId(entity.getEntityName(), referenceObject, getUriString());
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
    public <T extends ManagedObject> List<T> executeFetchRequest(FetchRequest request, ObjectContext objectContext) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method executeFetchRequest ");
    }

    /* access modifiers changed from: protected */
    public List<ObjectId> executeFetchRequestGetObjectID(FetchRequest request) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method executeFetchRequestGetObjectID ");
    }

    /* access modifiers changed from: protected */
    public Cursor executeFetchRequestGetCursor(FetchRequest request) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method executeFetchRequestGetCursor ");
    }

    /* access modifiers changed from: protected */
    public void executeSaveRequest(SaveRequest request) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method executeSaveRequest ");
    }

    /* access modifiers changed from: protected */
    public void executeSaveRequestWithTransaction(SaveRequest request) {
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
    public void clearTable(String entityName) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method clearTable ");
    }

    /* access modifiers changed from: protected */
    public void createEntityForMigration(Entity entity) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method createTable ");
    }

    /* access modifiers changed from: protected */
    public void renameEntityForMigration(String newName, String oldName) {
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
    public void dropEntityForMigration(String tableName) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method dropTable ");
    }

    /* access modifiers changed from: protected */
    public void insertDataForMigration(String tableName, ContentValues values) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method insertData ");
    }

    /* access modifiers changed from: protected */
    public void setDBVersions(String dbVersion, int dbVersionCode) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method setDBVersion ");
    }

    /* access modifiers changed from: protected */
    public void setEntityVersions(String tableName, String entityVersion, int entityVersionCode) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method setEntityVersion ");
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PersistentStore that = (PersistentStore) o;
        if (getDatabaseType() == that.getDatabaseType() && this.storageMode == that.storageMode) {
            return getPath().equals(that.getPath());
        }
        return false;
    }

    public int hashCode() {
        return (((getDatabaseType() * 31) + this.storageMode) * 31) + getPath().hashCode();
    }

    /* access modifiers changed from: protected */
    public ManagedObject getToOneRelationshipValue(String fieldName, ManagedObject object, ObjectContext objectContext) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method getToOneRelationshipValue ");
    }

    /* access modifiers changed from: protected */
    public List<ManagedObject> getToManyRelationshipValue(String fieldName, ManagedObject object, ObjectContext objectContext) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method getToManyRelationshipValue ");
    }

    /* access modifiers changed from: protected */
    public Cursor executeRawQuerySQL(String sql) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method executeRawQuerySQL ");
    }

    /* access modifiers changed from: protected */
    public void executeRawSQL(String sql) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method executeRawSQL ");
    }

    /* access modifiers changed from: protected */
    public Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the method query ");
    }

    /* access modifiers changed from: protected */
    public List<Object> executeFetchRequestWithAggregateFunction(FetchRequest fetchRequest) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support the query with aggregate function ");
    }

    /* access modifiers changed from: protected */
    public List<ManagedObject> remoteGetToManyRelationshipValue(String fieldName, ObjectId objectID, ObjectContext objectContext) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support remoteGetToManyRelationshipValue function ");
    }

    /* access modifiers changed from: protected */
    public ManagedObject remoteGetToOneRelationshipValue(String fieldName, ObjectId objectID, ObjectContext objectContext) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support remoteGetToOneRelationshipValue function ");
    }

    public void resetDatabaseEncryptKey(byte[] oldKey, byte[] newKey) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support to reset thr key of an encrypted database.");
    }

    public void exportDatabase(String newDBName, byte[] newKey) {
        throw new ODMFUnsupportedOperationException("The persistentStore not support to export database.");
    }

    public void resetMetadata() {
        throw new ODMFUnsupportedOperationException("The persistentStore not support reset metaData.");
    }

    public List<? extends Attribute> getEntityAttributes(String entityName) {
        Entity entity = getModel().getEntity(entityName);
        if (entity == null) {
            return null;
        }
        return entity.getAttributes();
    }
}

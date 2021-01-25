package com.huawei.odmf.core;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import com.huawei.odmf.database.DataBase;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFIllegalStateException;
import com.huawei.odmf.exception.ODMFRelatedObjectNotFoundException;
import com.huawei.odmf.exception.ODMFRuntimeException;
import com.huawei.odmf.model.AEntityHelper;
import com.huawei.odmf.model.ARelationship;
import com.huawei.odmf.model.api.Entity;
import com.huawei.odmf.model.api.ObjectModel;
import com.huawei.odmf.model.api.Relationship;
import com.huawei.odmf.store.DatabaseTableHelper;
import com.huawei.odmf.utils.LOG;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/* access modifiers changed from: package-private */
public class RelationshipLoader {
    private DataBase db;
    private Map<String, AEntityHelper> helperMap;
    private ObjectModel model;
    private List<LazyList<ManagedObject>> needClearLazyListModify = new ArrayList();
    private List<ODMFList<ManagedObject>> needClearODMFListModify = new ArrayList();

    RelationshipLoader(DataBase dataBase, ObjectModel objectModel, Map<String, AEntityHelper> map) {
        this.db = dataBase;
        this.model = objectModel;
        this.helperMap = map;
    }

    /* access modifiers changed from: package-private */
    public void handleRelationship(Collection<ManagedObject> collection) {
        for (ManagedObject managedObject : collection) {
            AEntityHelper helper = ((AManagedObject) managedObject).getHelper();
            Entity entity = helper.getEntity();
            if (entity.getRelationships() != null) {
                List<? extends Relationship> relationships = entity.getRelationships();
                int size = relationships.size();
                for (int i = 0; i < size; i++) {
                    if (managedObject.isRelationshipUpdate(i)) {
                        Relationship relationship = (Relationship) relationships.get(i);
                        Object relationshipObject = helper.getRelationshipObject(relationship.getFieldName(), managedObject);
                        Long rowId = managedObject.getRowId();
                        if (relationship.getRelationShipType() == 0) {
                            handleManyToManyRelationship(relationship, managedObject, relationshipObject, rowId.longValue());
                        } else if (relationship.getRelationShipType() == 4) {
                            handleOneToManyRelationship(relationship, managedObject, relationshipObject, rowId.longValue());
                        } else if (relationship.getRelationShipType() == 2) {
                            handleManyToOneRelationship(relationship, relationshipObject, rowId);
                        } else if (relationship.getRelationShipType() == 6) {
                            handleOneToOneRelationship(relationship, managedObject, relationshipObject, rowId);
                        } else {
                            throw new ODMFIllegalArgumentException("Illegal relationship");
                        }
                    }
                }
                continue;
            }
            managedObject.reSetRelationshipUpdateSigns();
        }
        if (this.needClearLazyListModify.size() > 0 || this.needClearODMFListModify.size() > 0) {
            clearAllModify();
        }
    }

    private void handleOneToOneRelationship(Relationship relationship, ManagedObject managedObject, Object obj, Long l) {
        if (obj instanceof ManagedObject) {
            ManagedObject managedObject2 = (ManagedObject) obj;
            if (managedObject2.getRowId().longValue() >= 0) {
                updateOneToOneValue(relationship, l.longValue(), managedObject2.getRowId().longValue());
            } else {
                throw new ODMFRelatedObjectNotFoundException("The object in the relationship of the insert object are not yet persist in the database");
            }
        } else if (obj instanceof Long) {
            Long l2 = (Long) obj;
            if (l2.longValue() >= 0) {
                updateOneToOneValue(relationship, l.longValue(), l2.longValue());
            } else {
                throw new ODMFRelatedObjectNotFoundException("The object in the relationship of the insert object are not yet persist in the database");
            }
        }
        updateOneToOneRelationshipRemoveOldValue(relationship, managedObject, obj);
    }

    private void handleManyToOneRelationship(Relationship relationship, Object obj, Long l) {
        if (obj instanceof ManagedObject) {
            ManagedObject managedObject = (ManagedObject) obj;
            if (managedObject.getRowId().longValue() >= 0) {
                updateRelationValue(relationship.getBaseEntity().getTableName(), relationship.getFieldName(), l.longValue(), managedObject.getRowId());
                return;
            }
            throw new ODMFRelatedObjectNotFoundException("The object in the relationship of the insert object are not yet persist in the database");
        } else if (obj instanceof Long) {
            Long l2 = (Long) obj;
            if (l2.longValue() >= 0) {
                updateRelationValue(relationship.getBaseEntity().getTableName(), relationship.getFieldName(), l.longValue(), l2);
                return;
            }
            throw new ODMFRelatedObjectNotFoundException("The object in the relationship of the insert object are not yet persist in the database");
        } else {
            updateRelationValue(relationship.getBaseEntity().getTableName(), relationship.getFieldName(), l.longValue(), null);
        }
    }

    private void handleOneToManyRelationship(Relationship relationship, ManagedObject managedObject, Object obj, long j) {
        if (obj != null) {
            handleOneToManyValue(relationship, managedObject, obj);
        } else {
            removeOneToManyValue(relationship, j);
        }
    }

    private void handleManyToManyRelationship(Relationship relationship, ManagedObject managedObject, Object obj, long j) {
        if (obj != null) {
            handleManyToManyValue(relationship, managedObject, obj);
        } else {
            removeManyToManyValue(relationship, j);
        }
    }

    private void clearAllModify() {
        int size = this.needClearLazyListModify.size();
        for (int i = 0; i < size; i++) {
            this.needClearLazyListModify.get(i).clearModify();
        }
        int size2 = this.needClearODMFListModify.size();
        for (int i2 = 0; i2 < size2; i2++) {
            this.needClearODMFListModify.get(i2).clearModify();
        }
        this.needClearLazyListModify.clear();
        this.needClearODMFListModify.clear();
    }

    private void updatingLazyListOneToManyValue(Relationship relationship, ManagedObject managedObject, Object obj) {
        LazyList<ManagedObject> lazyList = (LazyList) obj;
        if (lazyList.getBaseObj() == managedObject) {
            List<ManagedObject> removeList = lazyList.getRemoveList();
            List<ManagedObject> insertList = lazyList.getInsertList();
            if (removeList == null || insertList == null || (insertList.size() == 0 && removeList.size() == 0)) {
                handleOneToManyRelationshipUseJavaList(relationship, obj, managedObject.getRowId().longValue());
                return;
            }
            removeOneToManyValue(relationship, managedObject.getRowId().longValue(), removeList);
            addOneToManyValue(relationship, insertList, managedObject.getRowId().longValue());
            this.needClearLazyListModify.add(lazyList);
            return;
        }
        handleOneToManyRelationshipUseJavaList(relationship, obj, managedObject.getRowId().longValue());
    }

    private void updatingODMFListOneToManyValue(Relationship relationship, ManagedObject managedObject, Object obj) {
        ODMFList<ManagedObject> oDMFList = (ODMFList) obj;
        if (oDMFList.getBaseObj() == managedObject) {
            List<ManagedObject> removeList = oDMFList.getRemoveList();
            List<ManagedObject> insertList = oDMFList.getInsertList();
            if (removeList == null || insertList == null || (insertList.size() == 0 && removeList.size() == 0)) {
                handleOneToManyRelationshipUseJavaList(relationship, obj, managedObject.getRowId().longValue());
                return;
            }
            removeOneToManyValue(relationship, managedObject.getRowId().longValue(), removeList);
            addOneToManyValue(relationship, insertList, managedObject.getRowId().longValue());
            this.needClearODMFListModify.add(oDMFList);
            return;
        }
        handleOneToManyRelationshipUseJavaList(relationship, obj, managedObject.getRowId().longValue());
    }

    private void handleOneToManyValue(Relationship relationship, ManagedObject managedObject, Object obj) {
        if (obj instanceof LazyList) {
            updatingLazyListOneToManyValue(relationship, managedObject, obj);
        } else if (obj instanceof ODMFList) {
            updatingODMFListOneToManyValue(relationship, managedObject, obj);
        } else {
            handleOneToManyRelationshipUseJavaList(relationship, obj, managedObject.getRowId().longValue());
        }
    }

    private void updatingLazyListManyToManyValue(Relationship relationship, ManagedObject managedObject, Object obj) {
        LazyList<ManagedObject> lazyList = (LazyList) obj;
        if (lazyList.getBaseObj() == managedObject) {
            List<ManagedObject> removeList = lazyList.getRemoveList();
            List<ManagedObject> insertList = lazyList.getInsertList();
            if (removeList == null || insertList == null || (insertList.size() == 0 && removeList.size() == 0)) {
                handleManyToManyRelationshipUseJavaList(relationship, obj, managedObject.getRowId().longValue());
                return;
            }
            removeManyToManyValue(relationship, managedObject.getRowId().longValue(), removeList);
            addManyToManyValue(relationship, insertList, managedObject.getRowId().longValue());
            this.needClearLazyListModify.add(lazyList);
            return;
        }
        handleManyToManyRelationshipUseJavaList(relationship, obj, managedObject.getRowId().longValue());
    }

    private void updatingODMFListManyToManyValue(Relationship relationship, ManagedObject managedObject, Object obj) {
        ODMFList<ManagedObject> oDMFList = (ODMFList) obj;
        if (oDMFList.getBaseObj() == managedObject) {
            List<ManagedObject> removeList = oDMFList.getRemoveList();
            List<ManagedObject> insertList = oDMFList.getInsertList();
            if (removeList == null || insertList == null || (insertList.size() == 0 && removeList.size() == 0)) {
                handleManyToManyRelationshipUseJavaList(relationship, obj, managedObject.getRowId().longValue());
                return;
            }
            removeManyToManyValue(relationship, managedObject.getRowId().longValue(), removeList);
            addManyToManyValue(relationship, insertList, managedObject.getRowId().longValue());
            this.needClearODMFListModify.add(oDMFList);
            return;
        }
        handleManyToManyRelationshipUseJavaList(relationship, obj, managedObject.getRowId().longValue());
    }

    private void handleManyToManyValue(Relationship relationship, ManagedObject managedObject, Object obj) {
        if (obj instanceof LazyList) {
            updatingLazyListManyToManyValue(relationship, managedObject, obj);
        } else if (obj instanceof ODMFList) {
            updatingODMFListManyToManyValue(relationship, managedObject, obj);
        } else {
            handleManyToManyRelationshipUseJavaList(relationship, obj, managedObject.getRowId().longValue());
        }
    }

    /* access modifiers changed from: package-private */
    public void handleCascadeDelete(ManagedObject managedObject, List<ManagedObject> list) {
        Entity entity = this.model.getEntity(managedObject.getEntityName());
        AEntityHelper helper = ((AManagedObject) managedObject).getHelper();
        if (entity.getRelationships() != null) {
            List<? extends Relationship> relationships = entity.getRelationships();
            int size = relationships.size();
            for (int i = 0; i < size; i++) {
                Relationship relationship = (Relationship) relationships.get(i);
                if (relationship.getCascade().equals(ARelationship.DELETE_CASCADE)) {
                    if (relationship.getRelationShipType() == 6) {
                        handleOneToOneCascade(helper, relationship, managedObject, list);
                    } else if (relationship.getRelationShipType() == 2) {
                        handleManyToOneCascade(helper, relationship, managedObject, list);
                    } else if (relationship.getRelationShipType() == 4) {
                        handleOneToManyCascade(helper, relationship, managedObject, list);
                    } else {
                        handleManyToManyCascade(helper, relationship, managedObject, list);
                    }
                }
                if (relationship.getRelationShipType() == 0) {
                    this.db.delete(DatabaseTableHelper.getManyToManyMidTableName(relationship), DatabaseTableHelper.getRelationshipColumnName(relationship.getBaseEntity()) + " = ?", new String[]{String.valueOf(managedObject.getRowId())});
                }
            }
        }
    }

    private void handleManyToManyCascade(AEntityHelper aEntityHelper, Relationship relationship, ManagedObject managedObject, List<ManagedObject> list) {
        try {
            List list2 = (List) aEntityHelper.getRelationshipObject(relationship.getFieldName(), managedObject);
            if (list2 != null) {
                int size = list2.size();
                for (int i = 0; i < size; i++) {
                    Object obj = list2.get(i);
                    ManagedObject managedObject2 = null;
                    if (obj instanceof ManagedObject) {
                        managedObject2 = (ManagedObject) obj;
                    } else if (obj instanceof Long) {
                        managedObject2 = getTheRelatedObj(relationship, (Long) obj);
                    }
                    if (managedObject2 != null && checkManyToManyCascadeDelete(relationship, managedObject, managedObject2) && !isContainedObject(list, managedObject2)) {
                        list.add(managedObject2);
                        handleCascadeDelete(managedObject2, list);
                    }
                }
            }
        } catch (ODMFRelatedObjectNotFoundException unused) {
        }
    }

    private void handleOneToManyCascade(AEntityHelper aEntityHelper, Relationship relationship, ManagedObject managedObject, List<ManagedObject> list) {
        ManagedObject theRelatedObj;
        try {
            List list2 = (List) aEntityHelper.getRelationshipObject(relationship.getFieldName(), managedObject);
            if (list2 != null) {
                int size = list2.size();
                for (int i = 0; i < size; i++) {
                    Object obj = list2.get(i);
                    if (obj instanceof ManagedObject) {
                        ManagedObject managedObject2 = (ManagedObject) obj;
                        if (!isContainedObject(list, managedObject2)) {
                            list.add(managedObject2);
                            handleCascadeDelete(managedObject2, list);
                        }
                    } else if ((obj instanceof Long) && (theRelatedObj = getTheRelatedObj(relationship, (Long) obj)) != null && !isContainedObject(list, theRelatedObj)) {
                        list.add(theRelatedObj);
                        handleCascadeDelete(theRelatedObj, list);
                    }
                }
            }
        } catch (ODMFRelatedObjectNotFoundException unused) {
        }
    }

    private void handleManyToOneCascade(AEntityHelper aEntityHelper, Relationship relationship, ManagedObject managedObject, List<ManagedObject> list) {
        ManagedObject theRelatedObj;
        try {
            Object relationshipObject = aEntityHelper.getRelationshipObject(relationship.getFieldName(), managedObject);
            if (relationshipObject instanceof ManagedObject) {
                ManagedObject managedObject2 = (ManagedObject) relationshipObject;
                if (checkManyToOneCascadeDelete(relationship, managedObject, managedObject2) && !isContainedObject(list, managedObject2)) {
                    list.add(managedObject2);
                    handleCascadeDelete(managedObject2, list);
                }
            } else if (relationshipObject instanceof Long) {
                Long l = (Long) relationshipObject;
                if (checkManyToOneCascadeDelete(relationship, managedObject, l) && (theRelatedObj = getTheRelatedObj(relationship, l)) != null && !isContainedObject(list, theRelatedObj)) {
                    list.add(theRelatedObj);
                    handleCascadeDelete(theRelatedObj, list);
                }
            }
        } catch (ODMFRelatedObjectNotFoundException unused) {
        }
    }

    private void handleOneToOneCascade(AEntityHelper aEntityHelper, Relationship relationship, ManagedObject managedObject, List<ManagedObject> list) {
        try {
            Object relationshipObject = aEntityHelper.getRelationshipObject(relationship.getFieldName(), managedObject);
            if (relationshipObject instanceof ManagedObject) {
                ManagedObject managedObject2 = (ManagedObject) relationshipObject;
                if (!isContainedObject(list, managedObject2)) {
                    list.add(managedObject2);
                    handleCascadeDelete(managedObject2, list);
                }
            } else if (relationshipObject instanceof Long) {
                Long l = (Long) relationshipObject;
                ManagedObject theRelatedObj = getTheRelatedObj(relationship, l);
                if (theRelatedObj == null) {
                    LOG.logW("Cascade delete failed : the object not found which rowId is " + l);
                } else if (!isContainedObject(list, theRelatedObj)) {
                    list.add(theRelatedObj);
                    handleCascadeDelete(theRelatedObj, list);
                }
            }
        } catch (ODMFRelatedObjectNotFoundException unused) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0083  */
    private ManagedObject getTheRelatedObj(Relationship relationship, Long l) {
        Throwable th;
        Entity relatedEntity = relationship.getRelatedEntity();
        AEntityHelper helper = getHelper(relatedEntity.getEntityName());
        Cursor cursor = null;
        ManagedObject managedObject = null;
        try {
            DataBase dataBase = this.db;
            String tableName = relatedEntity.getTableName();
            String[] strArr = {DatabaseQueryService.getRowidColumnName() + " AS " + DatabaseQueryService.getRowidColumnName(), "*"};
            StringBuilder sb = new StringBuilder();
            sb.append(DatabaseQueryService.getRowidColumnName());
            sb.append("=?");
            Cursor query = DatabaseQueryService.query(dataBase, tableName, strArr, sb.toString(), new String[]{String.valueOf(l)});
            try {
                if (query.moveToNext()) {
                    managedObject = (ManagedObject) helper.readObject(query, 0);
                    managedObject.setObjectId(new AObjectId(relatedEntity.getEntityName(), l));
                    managedObject.setRowId(l);
                }
                if (query != null) {
                    query.close();
                }
                return managedObject;
            } catch (Throwable th2) {
                th = th2;
                cursor = query;
                if (cursor != null) {
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private AEntityHelper getHelper(String str) {
        return this.helperMap.get(str);
    }

    private void updateOneToOneRelationshipRemoveOldValue(Relationship relationship, ManagedObject managedObject, Object obj) {
        Entity entity;
        StringBuilder sb;
        if (relationship.isMajor()) {
            entity = relationship.getBaseEntity();
        } else {
            entity = relationship.getRelatedEntity();
        }
        String tableName = entity.getTableName();
        String foreignKeyName = relationship.getForeignKeyName();
        if (obj instanceof ManagedObject) {
            ManagedObject managedObject2 = (ManagedObject) obj;
            String str = foreignKeyName + " = ? And " + DatabaseQueryService.getRowidColumnName() + " <> ?";
            String[] strArr = relationship.isMajor() ? new String[]{String.valueOf(managedObject2.getRowId()), String.valueOf(managedObject.getRowId())} : new String[]{String.valueOf(managedObject.getRowId()), String.valueOf(managedObject2.getRowId())};
            ContentValues contentValues = new ContentValues();
            contentValues.putNull(foreignKeyName);
            this.db.update(tableName, contentValues, str, strArr);
        } else if (obj instanceof Long) {
            Long l = (Long) obj;
            String str2 = foreignKeyName + " = ? And " + DatabaseQueryService.getRowidColumnName() + " <> ?";
            String[] strArr2 = relationship.isMajor() ? new String[]{String.valueOf(l), String.valueOf(managedObject.getRowId())} : new String[]{String.valueOf(managedObject.getRowId()), String.valueOf(l)};
            ContentValues contentValues2 = new ContentValues();
            contentValues2.putNull(foreignKeyName);
            this.db.update(tableName, contentValues2, str2, strArr2);
        } else {
            if (relationship.isMajor()) {
                sb = new StringBuilder();
                sb.append(DatabaseQueryService.getRowidColumnName());
            } else {
                sb = new StringBuilder();
                sb.append(foreignKeyName);
            }
            sb.append(" = ?");
            String sb2 = sb.toString();
            String[] strArr3 = {String.valueOf(managedObject.getRowId())};
            ContentValues contentValues3 = new ContentValues();
            contentValues3.putNull(foreignKeyName);
            this.db.update(tableName, contentValues3, sb2, strArr3);
        }
    }

    private void updateOneToOneValue(Relationship relationship, long j, long j2) {
        Long l;
        if (relationship.isMajor()) {
            String tableName = relationship.getBaseEntity().getTableName();
            String foreignKeyName = relationship.getForeignKeyName();
            if (j2 < 0) {
                l = null;
            } else {
                l = Long.valueOf(j2);
            }
            updateRelationValue(tableName, foreignKeyName, j, l);
        } else if (j2 < 0) {
            String tableName2 = relationship.getInverseRelationship().getBaseEntity().getTableName();
            String foreignKeyName2 = relationship.getForeignKeyName();
            ContentValues contentValues = new ContentValues();
            contentValues.putNull(foreignKeyName2);
            DataBase dataBase = this.db;
            dataBase.update(tableName2, contentValues, foreignKeyName2 + " = ? ", new String[]{String.valueOf(j)});
        } else {
            updateRelationValue(relationship.getInverseRelationship().getBaseEntity().getTableName(), relationship.getForeignKeyName(), j2, Long.valueOf(j));
        }
    }

    private void addOneToManyValue(Relationship relationship, List list, long j) {
        Long l;
        int size = list.size();
        for (int i = 0; i < size; i++) {
            Object obj = list.get(i);
            if (obj instanceof ManagedObject) {
                l = ((ManagedObject) obj).getRowId();
            } else if (obj instanceof Long) {
                l = (Long) obj;
            } else {
                throw new ODMFIllegalArgumentException("the related id is null!");
            }
            updateRelationValue(relationship.getRelatedEntity().getTableName(), relationship.getInverseRelationship().getFieldName(), l.longValue(), Long.valueOf(j));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x00eb  */
    private void addManyToManyValue(Relationship relationship, List list, long j) {
        Throwable th;
        SQLException e;
        Long l;
        String manyToManyMidTableName = DatabaseTableHelper.getManyToManyMidTableName(relationship);
        HashSet hashSet = new HashSet();
        Cursor cursor = null;
        try {
            DataBase dataBase = this.db;
            String[] strArr = {DatabaseTableHelper.getRelationshipColumnName(relationship.getRelatedEntity())};
            Cursor query = dataBase.query(false, manyToManyMidTableName, strArr, DatabaseTableHelper.getRelationshipColumnName(relationship.getBaseEntity()) + " = ?", new String[]{String.valueOf(j)}, null, null, null, null);
            while (query.moveToNext()) {
                try {
                    hashSet.add(Long.valueOf(query.getLong(DatabaseQueryService.getOdmfRowidIndex())));
                } catch (SQLException e2) {
                    e = e2;
                    cursor = query;
                    try {
                        LOG.logE("execute addManyToManyValue error : A SQLException happens when query related ids.");
                        throw new ODMFRuntimeException("execute addManyToManyValue error : " + e.getMessage());
                    } catch (Throwable th2) {
                        th = th2;
                        if (cursor != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    cursor = query;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
            if (query != null) {
                query.close();
            }
            int size = list.size();
            for (int i = 0; i < size; i++) {
                Object obj = list.get(i);
                if (obj instanceof ManagedObject) {
                    l = ((ManagedObject) list.get(i)).getRowId();
                } else {
                    l = obj instanceof Long ? (Long) obj : null;
                }
                if (l != null) {
                    if (!hashSet.contains(l)) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(DatabaseTableHelper.getRelationshipColumnName(relationship.getBaseEntity()), Long.valueOf(j));
                        contentValues.put(DatabaseTableHelper.getRelationshipColumnName(relationship.getRelatedEntity()), l);
                        this.db.insertOrThrow(manyToManyMidTableName, null, contentValues);
                        hashSet.add(l);
                    }
                } else {
                    throw new ODMFIllegalStateException("The object in the relationship has not be inserted yet.");
                }
            }
        } catch (SQLException e3) {
            e = e3;
            LOG.logE("execute addManyToManyValue error : A SQLException happens when query related ids.");
            throw new ODMFRuntimeException("execute addManyToManyValue error : " + e.getMessage());
        }
    }

    private void removeManyToManyValue(Relationship relationship, long j) {
        String manyToManyMidTableName = DatabaseTableHelper.getManyToManyMidTableName(relationship);
        this.db.delete(manyToManyMidTableName, DatabaseTableHelper.getRelationshipColumnName(relationship.getBaseEntity()) + " = ?", new String[]{String.valueOf(j)});
    }

    private void removeManyToManyValue(Relationship relationship, long j, List<ManagedObject> list) {
        String manyToManyMidTableName = DatabaseTableHelper.getManyToManyMidTableName(relationship);
        this.db.delete(manyToManyMidTableName, DatabaseTableHelper.getRelationshipColumnName(relationship.getBaseEntity()) + " = ? AND " + DatabaseTableHelper.getRelationshipColumnName(relationship.getRelatedEntity()) + " IN (" + getIdString(list) + ")", new String[]{String.valueOf(j)});
    }

    private void removeOneToManyValue(Relationship relationship, long j) {
        String[] strArr = {String.valueOf(j)};
        ContentValues contentValues = new ContentValues();
        contentValues.putNull(relationship.getInverseRelationship().getFieldName());
        this.db.update(relationship.getRelatedEntity().getTableName(), contentValues, relationship.getInverseRelationship().getFieldName() + " = ?", strArr);
    }

    private void removeOneToManyValue(Relationship relationship, long j, List<ManagedObject> list) {
        String[] strArr = {String.valueOf(j)};
        ContentValues contentValues = new ContentValues();
        contentValues.putNull(relationship.getInverseRelationship().getFieldName());
        this.db.update(relationship.getRelatedEntity().getTableName(), contentValues, relationship.getInverseRelationship().getFieldName() + " = ? AND " + DatabaseQueryService.getRowidColumnName() + " IN (" + getIdString(list) + ")", strArr);
    }

    private String getIdString(List<ManagedObject> list) {
        StringBuilder sb = new StringBuilder();
        int size = list.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                sb.append(list.get(i).getRowId());
                sb.append(',');
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private void updateRelationValue(String str, String str2, long j, Long l) {
        ContentValues contentValues = new ContentValues();
        if (l != null) {
            contentValues.put(str2, l);
        } else {
            contentValues.putNull(str2);
        }
        DataBase dataBase = this.db;
        dataBase.update(str, contentValues, DatabaseQueryService.getRowidColumnName() + " = ?", new String[]{String.valueOf(j)});
    }

    private boolean checkManyToOneCascadeDelete(Relationship relationship, ManagedObject managedObject, ManagedObject managedObject2) {
        return checkManyToOneCascadeDelete(relationship, managedObject, managedObject2.getRowId());
    }

    private boolean checkManyToOneCascadeDelete(Relationship relationship, ManagedObject managedObject, Long l) {
        String tableName = relationship.getBaseEntity().getTableName();
        String[] strArr = {relationship.getForeignKeyName()};
        String str = relationship.getForeignKeyName() + " = ? And " + DatabaseQueryService.getRowidColumnName() + " <> ?";
        String[] strArr2 = {String.valueOf(l), String.valueOf(managedObject.getRowId())};
        Cursor cursor = null;
        try {
            cursor = DatabaseQueryService.query(this.db, tableName, strArr, str, strArr2);
            int count = cursor.getCount();
            if (cursor != null) {
                cursor.close();
            }
            if (count <= 0) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            LOG.logE("execute checkManyToOneCascadeDelete error : A SQLException happens when query related count.");
            throw new ODMFRuntimeException("execute checkManyToOneCascadeDelete error : " + e.getMessage());
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private boolean checkManyToManyCascadeDelete(Relationship relationship, ManagedObject managedObject, ManagedObject managedObject2) {
        String manyToManyMidTableName = DatabaseTableHelper.getManyToManyMidTableName(relationship);
        String[] strArr = {DatabaseTableHelper.getRelationshipColumnName(relationship.getRelatedEntity())};
        String str = DatabaseTableHelper.getRelationshipColumnName(relationship.getRelatedEntity()) + " = ? And " + DatabaseTableHelper.getRelationshipColumnName(relationship.getBaseEntity()) + " <> ?";
        String[] strArr2 = {String.valueOf(managedObject2.getRowId()), String.valueOf(managedObject.getRowId())};
        Cursor cursor = null;
        try {
            cursor = DatabaseQueryService.query(this.db, manyToManyMidTableName, strArr, str, strArr2);
            int count = cursor.getCount();
            if (cursor != null) {
                cursor.close();
            }
            if (count <= 0) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            LOG.logE("execute checkManyToManyCascadeDelete error : A SQLException happens when query related count.");
            throw new ODMFRuntimeException("execute checkManyToManyCascadeDelete error : " + e.getMessage());
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private boolean isContainedObject(List<ManagedObject> list, ManagedObject managedObject) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            ManagedObject managedObject2 = list.get(i);
            if (managedObject2.getRowId().equals(managedObject.getRowId()) && managedObject2.getEntityName().equals(managedObject.getEntityName())) {
                return true;
            }
        }
        return false;
    }

    private void handleManyToManyRelationshipUseJavaList(Relationship relationship, Object obj, long j) {
        removeManyToManyValue(relationship, j);
        addManyToManyValue(relationship, (List) obj, j);
    }

    private void handleOneToManyRelationshipUseJavaList(Relationship relationship, Object obj, long j) {
        removeOneToManyValue(relationship, j);
        addOneToManyValue(relationship, (List) obj, j);
    }

    /* access modifiers changed from: package-private */
    public Cursor getManyToOneRelationshipCursor(ObjectId objectId, Relationship relationship) {
        Entity baseEntity = relationship.getBaseEntity();
        String fieldName = relationship.getFieldName();
        String tableName = baseEntity.getTableName();
        String[] strArr = {fieldName};
        return DatabaseQueryService.query(this.db, tableName, strArr, DatabaseQueryService.getRowidColumnName() + " = " + objectId.getId(), null);
    }

    /* access modifiers changed from: package-private */
    public Cursor getOneToOneRelationshipCursor(ObjectId objectId, Relationship relationship) {
        String str;
        String[] strArr;
        String str2;
        Entity relatedEntity = relationship.getRelatedEntity();
        Entity baseEntity = relationship.getBaseEntity();
        boolean isMajor = relationship.isMajor();
        if (isMajor) {
            str = relationship.getFieldName();
        } else {
            str = relationship.getInverseRelationship().getFieldName();
        }
        String tableName = isMajor ? baseEntity.getTableName() : relatedEntity.getTableName();
        if (isMajor) {
            strArr = new String[]{str + " AS " + str};
        } else {
            strArr = new String[]{DatabaseQueryService.getRowidColumnName() + " AS " + str};
        }
        if (isMajor) {
            str2 = DatabaseQueryService.getRowidColumnName() + " = ?";
        } else {
            str2 = str + " = ? ";
        }
        return DatabaseQueryService.query(this.db, tableName, strArr, str2, new String[]{"" + objectId.getId()});
    }

    /* access modifiers changed from: package-private */
    public Cursor getOneToManyRelationshipCursor(ObjectId objectId, Relationship relationship) {
        String tableName = relationship.getRelatedEntity().getTableName();
        String[] strArr = {DatabaseQueryService.getRowidColumnName() + " AS " + DatabaseQueryService.getRowidColumnName()};
        StringBuilder sb = new StringBuilder();
        sb.append(relationship.getInverseRelationship().getFieldName());
        sb.append(" = ?");
        Cursor query = DatabaseQueryService.query(this.db, tableName, strArr, sb.toString(), new String[]{String.valueOf(objectId.getId())});
        if (!relationship.getNotFound().equals(ARelationship.EXCEPTION) || query == null || query.getCount() > 0) {
            return query;
        }
        query.close();
        throw new ODMFRelatedObjectNotFoundException("The relevant object not found");
    }

    /* access modifiers changed from: package-private */
    public Cursor getManyToManyRelationshipCursor(ObjectId objectId, Relationship relationship) {
        Entity relatedEntity = relationship.getRelatedEntity();
        Entity baseEntity = relationship.getBaseEntity();
        String[] strArr = {"t2." + DatabaseTableHelper.getRelationshipColumnName(relatedEntity)};
        String rowidColumnName = DatabaseQueryService.getRowidColumnName();
        Cursor query = DatabaseQueryService.query(this.db, baseEntity.getTableName() + " AS t1," + DatabaseTableHelper.getManyToManyMidTableName(relationship) + " AS t2", strArr, "t1." + rowidColumnName + " = " + objectId.getId() + " AND t1." + rowidColumnName + " = t2." + DatabaseTableHelper.getRelationshipColumnName(baseEntity), null);
        if (!relationship.getNotFound().equals(ARelationship.EXCEPTION) || query == null || query.getCount() > 0) {
            return query;
        }
        query.close();
        throw new ODMFRelatedObjectNotFoundException("The relevant object not found");
    }
}

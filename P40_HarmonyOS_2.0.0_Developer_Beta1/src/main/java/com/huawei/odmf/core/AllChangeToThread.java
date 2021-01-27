package com.huawei.odmf.core;

import android.util.ArrayMap;
import com.huawei.odmf.user.api.AllChangeToTarget;
import com.huawei.odmf.user.api.ObjectContext;
import java.util.List;

/* access modifiers changed from: package-private */
public class AllChangeToThread {
    private ObjectContext changeContext;
    private ArrayMap<String, AllChangeToTarget> entityMap = new ArrayMap<>();
    private String entityName;
    private ArrayMap<ObjectId, AllChangeToTarget> manageObjectIDMap = new ArrayMap<>();
    private ArrayMap<ObjectContext, AllChangeToTarget> objectContextMap = new ArrayMap<>();
    private ArrayMap<String, AllChangeToTarget> persistStoreMap = new ArrayMap<>();
    private String uriString;

    public AllChangeToThread(List<ManagedObject> list, List<ManagedObject> list2, List<ManagedObject> list3, ObjectContext objectContext, String str) {
        analysisChange(list, list2, list3);
        this.changeContext = objectContext;
        this.uriString = str;
    }

    public AllChangeToThread(List<ManagedObject> list, List<ManagedObject> list2, List<ManagedObject> list3, ObjectContext objectContext) {
        analysisChange(list, list2, list3);
        this.changeContext = objectContext;
    }

    public AllChangeToThread(ObjectContext objectContext) {
        this.changeContext = objectContext;
    }

    public AllChangeToThread(ObjectContext objectContext, String str, String str2) {
        this.changeContext = objectContext;
        this.entityName = str;
        this.uriString = str2;
    }

    public String getUriString() {
        return this.uriString;
    }

    public void setUriString(String str) {
        this.uriString = str;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public void setEntityName(String str) {
        this.entityName = str;
    }

    private void analysisChange(List<ManagedObject> list, List<ManagedObject> list2, List<ManagedObject> list3) {
        if (list.size() != 0) {
            analysisInsertList(list);
        }
        if (list2.size() != 0) {
            analysisUpdateList(list2);
        }
        if (list3.size() != 0) {
            analysisDeleteList(list3);
        }
    }

    private void analysisDeleteList(List<ManagedObject> list) {
        for (ManagedObject managedObject : list) {
            String uriString2 = managedObject.getUriString();
            AllChangeToTarget allChangeToTarget = this.persistStoreMap.get(uriString2);
            if (allChangeToTarget != null) {
                allChangeToTarget.addToDeletedList(managedObject);
            } else {
                AllChangeToTargetImpl allChangeToTargetImpl = new AllChangeToTargetImpl();
                allChangeToTargetImpl.addToDeletedList(managedObject);
                this.persistStoreMap.put(uriString2, allChangeToTargetImpl);
            }
            ObjectContext objectContext = managedObject.getObjectContext();
            AllChangeToTarget allChangeToTarget2 = this.objectContextMap.get(objectContext);
            if (allChangeToTarget2 != null) {
                allChangeToTarget2.addToDeletedList(managedObject);
            } else {
                AllChangeToTargetImpl allChangeToTargetImpl2 = new AllChangeToTargetImpl();
                allChangeToTargetImpl2.addToDeletedList(managedObject);
                this.objectContextMap.put(objectContext, allChangeToTargetImpl2);
            }
            String entityName2 = managedObject.getEntityName();
            AllChangeToTarget allChangeToTarget3 = this.entityMap.get(entityName2);
            if (allChangeToTarget3 != null) {
                allChangeToTarget3.addToDeletedList(managedObject);
            } else {
                AllChangeToTargetImpl allChangeToTargetImpl3 = new AllChangeToTargetImpl();
                allChangeToTargetImpl3.addToDeletedList(managedObject);
                this.entityMap.put(entityName2, allChangeToTargetImpl3);
            }
            ObjectId objectId = managedObject.getObjectId();
            AllChangeToTarget allChangeToTarget4 = this.manageObjectIDMap.get(objectId);
            if (allChangeToTarget4 != null) {
                allChangeToTarget4.addToDeletedList(managedObject);
            } else {
                AllChangeToTargetImpl allChangeToTargetImpl4 = new AllChangeToTargetImpl();
                allChangeToTargetImpl4.addToDeletedList(managedObject);
                this.manageObjectIDMap.put(objectId, allChangeToTargetImpl4);
            }
        }
    }

    private void analysisUpdateList(List<ManagedObject> list) {
        for (ManagedObject managedObject : list) {
            String uriString2 = managedObject.getUriString();
            AllChangeToTarget allChangeToTarget = this.persistStoreMap.get(uriString2);
            if (allChangeToTarget != null) {
                allChangeToTarget.addToUpdatedList(managedObject);
            } else {
                AllChangeToTargetImpl allChangeToTargetImpl = new AllChangeToTargetImpl();
                allChangeToTargetImpl.addToUpdatedList(managedObject);
                this.persistStoreMap.put(uriString2, allChangeToTargetImpl);
            }
            ObjectContext objectContext = managedObject.getObjectContext();
            AllChangeToTarget allChangeToTarget2 = this.objectContextMap.get(objectContext);
            if (allChangeToTarget2 != null) {
                allChangeToTarget2.addToUpdatedList(managedObject);
            } else {
                AllChangeToTargetImpl allChangeToTargetImpl2 = new AllChangeToTargetImpl();
                allChangeToTargetImpl2.addToUpdatedList(managedObject);
                this.objectContextMap.put(objectContext, allChangeToTargetImpl2);
            }
            String entityName2 = managedObject.getEntityName();
            AllChangeToTarget allChangeToTarget3 = this.entityMap.get(entityName2);
            if (allChangeToTarget3 != null) {
                allChangeToTarget3.addToUpdatedList(managedObject);
            } else {
                AllChangeToTargetImpl allChangeToTargetImpl3 = new AllChangeToTargetImpl();
                allChangeToTargetImpl3.addToUpdatedList(managedObject);
                this.entityMap.put(entityName2, allChangeToTargetImpl3);
            }
            ObjectId objectId = managedObject.getObjectId();
            AllChangeToTarget allChangeToTarget4 = this.manageObjectIDMap.get(objectId);
            if (allChangeToTarget4 != null) {
                allChangeToTarget4.addToUpdatedList(managedObject);
            } else {
                AllChangeToTargetImpl allChangeToTargetImpl4 = new AllChangeToTargetImpl();
                allChangeToTargetImpl4.addToUpdatedList(managedObject);
                this.manageObjectIDMap.put(objectId, allChangeToTargetImpl4);
            }
        }
    }

    private void analysisInsertList(List<ManagedObject> list) {
        for (ManagedObject managedObject : list) {
            String uriString2 = managedObject.getUriString();
            AllChangeToTarget allChangeToTarget = this.persistStoreMap.get(uriString2);
            if (allChangeToTarget != null) {
                allChangeToTarget.addToInsertList(managedObject);
            } else {
                AllChangeToTargetImpl allChangeToTargetImpl = new AllChangeToTargetImpl();
                allChangeToTargetImpl.addToInsertList(managedObject);
                this.persistStoreMap.put(uriString2, allChangeToTargetImpl);
            }
            ObjectContext objectContext = managedObject.getObjectContext();
            AllChangeToTarget allChangeToTarget2 = this.objectContextMap.get(objectContext);
            if (allChangeToTarget2 != null) {
                allChangeToTarget2.addToInsertList(managedObject);
            } else {
                AllChangeToTargetImpl allChangeToTargetImpl2 = new AllChangeToTargetImpl();
                allChangeToTargetImpl2.addToInsertList(managedObject);
                this.objectContextMap.put(objectContext, allChangeToTargetImpl2);
            }
            String entityName2 = managedObject.getEntityName();
            AllChangeToTarget allChangeToTarget3 = this.entityMap.get(entityName2);
            if (allChangeToTarget3 != null) {
                allChangeToTarget3.addToInsertList(managedObject);
            } else {
                AllChangeToTargetImpl allChangeToTargetImpl3 = new AllChangeToTargetImpl();
                allChangeToTargetImpl3.addToInsertList(managedObject);
                this.entityMap.put(entityName2, allChangeToTargetImpl3);
            }
            ObjectId objectId = managedObject.getObjectId();
            AllChangeToTarget allChangeToTarget4 = this.manageObjectIDMap.get(objectId);
            if (allChangeToTarget4 != null) {
                allChangeToTarget4.addToInsertList(managedObject);
            } else {
                AllChangeToTargetImpl allChangeToTargetImpl4 = new AllChangeToTargetImpl();
                allChangeToTargetImpl4.addToInsertList(managedObject);
                this.manageObjectIDMap.put(objectId, allChangeToTargetImpl4);
            }
        }
    }

    public ArrayMap<String, AllChangeToTarget> getPsMap() {
        return this.persistStoreMap;
    }

    public ArrayMap<ObjectContext, AllChangeToTarget> getObjContextMap() {
        return this.objectContextMap;
    }

    public ArrayMap<String, AllChangeToTarget> getEntityMap() {
        return this.entityMap;
    }

    public ArrayMap<ObjectId, AllChangeToTarget> getManageObjMap() {
        return this.manageObjectIDMap;
    }

    public void setPsChangeMap(ArrayMap<String, AllChangeToTarget> arrayMap) {
        this.persistStoreMap = arrayMap;
    }

    public void setObjectContextChangeMap(ArrayMap<ObjectContext, AllChangeToTarget> arrayMap) {
        this.objectContextMap = arrayMap;
    }

    public void setEntityChangeMap(ArrayMap<String, AllChangeToTarget> arrayMap) {
        this.entityMap = arrayMap;
    }

    public void setManageObjectChangeMap(ArrayMap<ObjectId, AllChangeToTarget> arrayMap) {
        this.manageObjectIDMap = arrayMap;
    }

    public ObjectContext getChangeContext() {
        return this.changeContext;
    }

    public String toString() {
        return "Ps Map size:" + this.persistStoreMap.size() + ",objectContext Map size:" + this.objectContextMap.size() + ", entity Map size:" + this.entityMap.size() + ", manageObjectID Map size:" + this.manageObjectIDMap.size();
    }
}

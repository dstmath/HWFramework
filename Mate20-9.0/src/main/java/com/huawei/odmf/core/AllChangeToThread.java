package com.huawei.odmf.core;

import android.util.ArrayMap;
import com.huawei.odmf.user.api.AllChangeToTarget;
import com.huawei.odmf.user.api.ObjectContext;
import java.util.List;

class AllChangeToThread {
    private ObjectContext changeContext;
    private ArrayMap<String, AllChangeToTarget> entityMap = new ArrayMap<>();
    private String entityName;
    private ArrayMap<ObjectId, AllChangeToTarget> manageObjectIDMap = new ArrayMap<>();
    private ArrayMap<ObjectContext, AllChangeToTarget> objectContextMap = new ArrayMap<>();
    private ArrayMap<String, AllChangeToTarget> persistStoreMap = new ArrayMap<>();
    private String uriString;

    public AllChangeToThread(List<ManagedObject> insert, List<ManagedObject> updated, List<ManagedObject> deleted, ObjectContext context, String uriString2) {
        analysisChange(insert, updated, deleted);
        this.changeContext = context;
        this.uriString = uriString2;
    }

    public AllChangeToThread(List<ManagedObject> insert, List<ManagedObject> updated, List<ManagedObject> deleted, ObjectContext context) {
        analysisChange(insert, updated, deleted);
        this.changeContext = context;
    }

    public AllChangeToThread(ObjectContext context) {
        this.changeContext = context;
    }

    public AllChangeToThread(ObjectContext context, String entityName2, String uriString2) {
        this.changeContext = context;
        this.entityName = entityName2;
        this.uriString = uriString2;
    }

    public String getUriString() {
        return this.uriString;
    }

    public void setUriString(String uriString2) {
        this.uriString = uriString2;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public void setEntityName(String entityName2) {
        this.entityName = entityName2;
    }

    private void analysisChange(List<ManagedObject> insertedObjectsList, List<ManagedObject> updatedObjectsList, List<ManagedObject> deletedObjectsList) {
        if (insertedObjectsList.size() != 0) {
            analysisInsertList(insertedObjectsList);
        }
        if (updatedObjectsList.size() != 0) {
            analysisUpdateList(updatedObjectsList);
        }
        if (deletedObjectsList.size() != 0) {
            analysisDeleteList(deletedObjectsList);
        }
    }

    private void analysisDeleteList(List<ManagedObject> deletedObjectsList) {
        for (ManagedObject manageObject : deletedObjectsList) {
            String uriStr = manageObject.getUriString();
            AllChangeToTarget psAllChangeToTarget = this.persistStoreMap.get(uriStr);
            if (psAllChangeToTarget != null) {
                psAllChangeToTarget.addToDeletedList(manageObject);
            } else {
                AllChangeToTarget subAllChangePs = new AllChangeToTargetImpl();
                subAllChangePs.addToDeletedList(manageObject);
                this.persistStoreMap.put(uriStr, subAllChangePs);
            }
            ObjectContext objContext = manageObject.getObjectContext();
            AllChangeToTarget contextAllChangeToTarget = this.objectContextMap.get(objContext);
            if (contextAllChangeToTarget != null) {
                contextAllChangeToTarget.addToDeletedList(manageObject);
            } else {
                AllChangeToTarget subAllChangeObjContext = new AllChangeToTargetImpl();
                subAllChangeObjContext.addToDeletedList(manageObject);
                this.objectContextMap.put(objContext, subAllChangeObjContext);
            }
            String entityName2 = manageObject.getEntityName();
            AllChangeToTarget entityAllChangeToTarget = this.entityMap.get(entityName2);
            if (entityAllChangeToTarget != null) {
                entityAllChangeToTarget.addToDeletedList(manageObject);
            } else {
                AllChangeToTarget subAllChangeEntityName = new AllChangeToTargetImpl();
                subAllChangeEntityName.addToDeletedList(manageObject);
                this.entityMap.put(entityName2, subAllChangeEntityName);
            }
            ObjectId objID = manageObject.getObjectId();
            AllChangeToTarget objAllChangeToTarget = this.manageObjectIDMap.get(objID);
            if (objAllChangeToTarget != null) {
                objAllChangeToTarget.addToDeletedList(manageObject);
            } else {
                AllChangeToTarget subAllChangeObjectID = new AllChangeToTargetImpl();
                subAllChangeObjectID.addToDeletedList(manageObject);
                this.manageObjectIDMap.put(objID, subAllChangeObjectID);
            }
        }
    }

    private void analysisUpdateList(List<ManagedObject> updatedObjectsList) {
        for (ManagedObject manageObject : updatedObjectsList) {
            String uriStr = manageObject.getUriString();
            AllChangeToTarget psAllChangeToTarget = this.persistStoreMap.get(uriStr);
            if (psAllChangeToTarget != null) {
                psAllChangeToTarget.addToUpdatedList(manageObject);
            } else {
                AllChangeToTarget subAllChangePs = new AllChangeToTargetImpl();
                subAllChangePs.addToUpdatedList(manageObject);
                this.persistStoreMap.put(uriStr, subAllChangePs);
            }
            ObjectContext objContext = manageObject.getObjectContext();
            AllChangeToTarget contextAllChangeToTarget = this.objectContextMap.get(objContext);
            if (contextAllChangeToTarget != null) {
                contextAllChangeToTarget.addToUpdatedList(manageObject);
            } else {
                AllChangeToTarget subAllChangeObjContext = new AllChangeToTargetImpl();
                subAllChangeObjContext.addToUpdatedList(manageObject);
                this.objectContextMap.put(objContext, subAllChangeObjContext);
            }
            String entityName2 = manageObject.getEntityName();
            AllChangeToTarget entityAllChangeToTarget = this.entityMap.get(entityName2);
            if (entityAllChangeToTarget != null) {
                entityAllChangeToTarget.addToUpdatedList(manageObject);
            } else {
                AllChangeToTarget subAllChangeEntityName = new AllChangeToTargetImpl();
                subAllChangeEntityName.addToUpdatedList(manageObject);
                this.entityMap.put(entityName2, subAllChangeEntityName);
            }
            ObjectId objID = manageObject.getObjectId();
            AllChangeToTarget objAllChangeToTarget = this.manageObjectIDMap.get(objID);
            if (objAllChangeToTarget != null) {
                objAllChangeToTarget.addToUpdatedList(manageObject);
            } else {
                AllChangeToTarget subAllChangeObjectID = new AllChangeToTargetImpl();
                subAllChangeObjectID.addToUpdatedList(manageObject);
                this.manageObjectIDMap.put(objID, subAllChangeObjectID);
            }
        }
    }

    private void analysisInsertList(List<ManagedObject> insertedObjectsList) {
        for (ManagedObject manageObject : insertedObjectsList) {
            String uriStr = manageObject.getUriString();
            AllChangeToTarget psAllChangeToTarget = this.persistStoreMap.get(uriStr);
            if (psAllChangeToTarget != null) {
                psAllChangeToTarget.addToInsertList(manageObject);
            } else {
                AllChangeToTarget subAllChangePs = new AllChangeToTargetImpl();
                subAllChangePs.addToInsertList(manageObject);
                this.persistStoreMap.put(uriStr, subAllChangePs);
            }
            ObjectContext objContext = manageObject.getObjectContext();
            AllChangeToTarget contextAllChangeToTarget = this.objectContextMap.get(objContext);
            if (contextAllChangeToTarget != null) {
                contextAllChangeToTarget.addToInsertList(manageObject);
            } else {
                AllChangeToTarget subAllChangeObjContext = new AllChangeToTargetImpl();
                subAllChangeObjContext.addToInsertList(manageObject);
                this.objectContextMap.put(objContext, subAllChangeObjContext);
            }
            String entityName2 = manageObject.getEntityName();
            AllChangeToTarget entityAllChangeToTarget = this.entityMap.get(entityName2);
            if (entityAllChangeToTarget != null) {
                entityAllChangeToTarget.addToInsertList(manageObject);
            } else {
                AllChangeToTarget subAllChangeEntityName = new AllChangeToTargetImpl();
                subAllChangeEntityName.addToInsertList(manageObject);
                this.entityMap.put(entityName2, subAllChangeEntityName);
            }
            ObjectId objID = manageObject.getObjectId();
            AllChangeToTarget objAllChangeToTarget = this.manageObjectIDMap.get(objID);
            if (objAllChangeToTarget != null) {
                objAllChangeToTarget.addToInsertList(manageObject);
            } else {
                AllChangeToTarget subAllChangeObjectID = new AllChangeToTargetImpl();
                subAllChangeObjectID.addToInsertList(manageObject);
                this.manageObjectIDMap.put(objID, subAllChangeObjectID);
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

    public void setPsChangeMap(ArrayMap<String, AllChangeToTarget> map) {
        this.persistStoreMap = map;
    }

    public void setObjectContextChangeMap(ArrayMap<ObjectContext, AllChangeToTarget> map) {
        this.objectContextMap = map;
    }

    public void setEntityChangeMap(ArrayMap<String, AllChangeToTarget> map) {
        this.entityMap = map;
    }

    public void setManageObjectChangeMap(ArrayMap<ObjectId, AllChangeToTarget> change) {
        this.manageObjectIDMap = change;
    }

    public ObjectContext getChangeContext() {
        return this.changeContext;
    }

    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Ps Map size:").append(this.persistStoreMap.size()).append(",objectContext Map size:").append(this.objectContextMap.size()).append(", ").append("entity Map size:").append(this.entityMap.size()).append(", ").append("manageObjectID Map size:").append(this.manageObjectIDMap.size());
        return strBuilder.toString();
    }
}

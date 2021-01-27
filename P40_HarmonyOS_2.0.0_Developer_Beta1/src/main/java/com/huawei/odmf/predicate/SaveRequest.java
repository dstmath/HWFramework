package com.huawei.odmf.predicate;

import com.huawei.odmf.core.ManagedObject;
import java.util.List;

public class SaveRequest {
    private List<ManagedObject> deletedObjects = null;
    private List<ManagedObject> insertedObjects = null;
    private List<ManagedObject> updatedObjects = null;

    public SaveRequest(List<ManagedObject> list, List<ManagedObject> list2, List<ManagedObject> list3) {
        this.insertedObjects = list;
        this.updatedObjects = list2;
        this.deletedObjects = list3;
    }

    public List<ManagedObject> getInsertedObjects() {
        return this.insertedObjects;
    }

    public void setInsertedObjects(List<ManagedObject> list) {
        this.insertedObjects = list;
    }

    public List<ManagedObject> getUpdatedObjects() {
        return this.updatedObjects;
    }

    public void setUpdatedObjects(List<ManagedObject> list) {
        this.updatedObjects = list;
    }

    public List<ManagedObject> getDeletedObjects() {
        return this.deletedObjects;
    }

    public void setDeletedObjects(List<ManagedObject> list) {
        this.deletedObjects = list;
    }
}

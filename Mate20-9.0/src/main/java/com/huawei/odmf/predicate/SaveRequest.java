package com.huawei.odmf.predicate;

import com.huawei.odmf.core.ManagedObject;
import java.util.List;

public class SaveRequest {
    private List<ManagedObject> deletedObjects = null;
    private List<ManagedObject> insertedObjects = null;
    private List<ManagedObject> updatedObjects = null;

    public SaveRequest(List<ManagedObject> insertedObjects2, List<ManagedObject> updatedObjects2, List<ManagedObject> deletedObjects2) {
        this.insertedObjects = insertedObjects2;
        this.updatedObjects = updatedObjects2;
        this.deletedObjects = deletedObjects2;
    }

    public List<ManagedObject> getInsertedObjects() {
        return this.insertedObjects;
    }

    public void setInsertedObjects(List<ManagedObject> insertedObjects2) {
        this.insertedObjects = insertedObjects2;
    }

    public List<ManagedObject> getUpdatedObjects() {
        return this.updatedObjects;
    }

    public void setUpdatedObjects(List<ManagedObject> updatedObjects2) {
        this.updatedObjects = updatedObjects2;
    }

    public List<ManagedObject> getDeletedObjects() {
        return this.deletedObjects;
    }

    public void setDeletedObjects(List<ManagedObject> deletedObjects2) {
        this.deletedObjects = deletedObjects2;
    }
}

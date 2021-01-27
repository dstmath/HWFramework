package ohos.data.orm.impl;

import java.util.List;
import ohos.data.orm.OrmObject;

public class SaveRequest {
    private List<OrmObject> deletedObjects;
    private List<OrmObject> insertedObjects;
    private List<OrmObject> updatedObjects;

    public SaveRequest(List<OrmObject> list, List<OrmObject> list2, List<OrmObject> list3) {
        this.insertedObjects = list;
        this.updatedObjects = list2;
        this.deletedObjects = list3;
    }

    public List<OrmObject> getInsertedObjects() {
        return this.insertedObjects;
    }

    public List<OrmObject> getUpdatedObjects() {
        return this.updatedObjects;
    }

    public List<OrmObject> getDeletedObjects() {
        return this.deletedObjects;
    }
}

package ohos.data.orm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllChangeToTarget {
    private Set<OrmObject> deletedObjectsSet = new HashSet();
    private Set<OrmObject> insertedObjectsSet = new HashSet();
    private Set<OrmObject> updatedObjectsSet = new HashSet();

    public void addToInsertList(OrmObject ormObject) {
        this.insertedObjectsSet.add(ormObject);
    }

    public void addToUpdatedList(OrmObject ormObject) {
        this.updatedObjectsSet.add(ormObject);
    }

    public void addToDeletedList(OrmObject ormObject) {
        this.deletedObjectsSet.add(ormObject);
    }

    public List<OrmObject> getInsertedList() {
        return new ArrayList(this.insertedObjectsSet);
    }

    public List<OrmObject> getUpdatedList() {
        return new ArrayList(this.updatedObjectsSet);
    }

    public List<OrmObject> getDeletedList() {
        return new ArrayList(this.deletedObjectsSet);
    }
}

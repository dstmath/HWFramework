package com.huawei.odmf.core;

import com.huawei.odmf.user.api.AllChangeToTarget;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class AllChangeToTargetImpl implements AllChangeToTarget {
    private boolean deleteAll = false;
    private Set<ManagedObject> deletedObjectsSet = new HashSet();
    private Set<ManagedObject> insertedObjectsSet = new HashSet();
    private Set<ManagedObject> updatedObjectsSet = new HashSet();

    public AllChangeToTargetImpl() {
    }

    public AllChangeToTargetImpl(boolean deleteAll2) {
        this.deleteAll = deleteAll2;
    }

    public void addToInsertList(ManagedObject manageObject) {
        if (!this.insertedObjectsSet.contains(manageObject)) {
            this.insertedObjectsSet.add(manageObject);
        }
    }

    public void addToUpdatedList(ManagedObject manageObject) {
        if (!this.updatedObjectsSet.contains(manageObject)) {
            this.updatedObjectsSet.add(manageObject);
        }
    }

    public void addToDeletedList(ManagedObject manageObject) {
        if (!this.deletedObjectsSet.contains(manageObject)) {
            this.deletedObjectsSet.add(manageObject);
        }
    }

    public List<ManagedObject> getInsertList() {
        return new ArrayList(this.insertedObjectsSet);
    }

    public List<ManagedObject> getUpdatedList() {
        return new ArrayList(this.updatedObjectsSet);
    }

    public List<ManagedObject> getDeletedList() {
        return new ArrayList(this.deletedObjectsSet);
    }

    public boolean isDeleteAll() {
        return this.deleteAll;
    }
}

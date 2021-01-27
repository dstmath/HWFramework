package com.huawei.odmf.core;

import com.huawei.odmf.user.api.AllChangeToTarget;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class AllChangeToTargetImpl implements AllChangeToTarget {
    private Set<ManagedObject> deletedObjectsSet = new HashSet();
    private Set<ManagedObject> insertedObjectsSet = new HashSet();
    private boolean isDeleteAll = false;
    private Set<ManagedObject> updatedObjectsSet = new HashSet();

    public AllChangeToTargetImpl() {
    }

    public AllChangeToTargetImpl(boolean z) {
        this.isDeleteAll = z;
    }

    @Override // com.huawei.odmf.user.api.AllChangeToTarget
    public void addToInsertList(ManagedObject managedObject) {
        if (!this.insertedObjectsSet.contains(managedObject)) {
            this.insertedObjectsSet.add(managedObject);
        }
    }

    @Override // com.huawei.odmf.user.api.AllChangeToTarget
    public void addToUpdatedList(ManagedObject managedObject) {
        if (!this.updatedObjectsSet.contains(managedObject)) {
            this.updatedObjectsSet.add(managedObject);
        }
    }

    @Override // com.huawei.odmf.user.api.AllChangeToTarget
    public void addToDeletedList(ManagedObject managedObject) {
        if (!this.deletedObjectsSet.contains(managedObject)) {
            this.deletedObjectsSet.add(managedObject);
        }
    }

    @Override // com.huawei.odmf.user.api.AllChangeToTarget
    public List<ManagedObject> getInsertList() {
        return new ArrayList(this.insertedObjectsSet);
    }

    @Override // com.huawei.odmf.user.api.AllChangeToTarget
    public List<ManagedObject> getUpdatedList() {
        return new ArrayList(this.updatedObjectsSet);
    }

    @Override // com.huawei.odmf.user.api.AllChangeToTarget
    public List<ManagedObject> getDeletedList() {
        return new ArrayList(this.deletedObjectsSet);
    }

    @Override // com.huawei.odmf.user.api.AllChangeToTarget
    public boolean isDeleteAll() {
        return this.isDeleteAll;
    }
}

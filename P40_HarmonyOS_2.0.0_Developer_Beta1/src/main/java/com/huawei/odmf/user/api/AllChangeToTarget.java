package com.huawei.odmf.user.api;

import com.huawei.odmf.core.ManagedObject;
import java.util.List;

public interface AllChangeToTarget {
    void addToDeletedList(ManagedObject managedObject);

    void addToInsertList(ManagedObject managedObject);

    void addToUpdatedList(ManagedObject managedObject);

    List<ManagedObject> getDeletedList();

    List<ManagedObject> getInsertList();

    List<ManagedObject> getUpdatedList();

    boolean isDeleteAll();
}

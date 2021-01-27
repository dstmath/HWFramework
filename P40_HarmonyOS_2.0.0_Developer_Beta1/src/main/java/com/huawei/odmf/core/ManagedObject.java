package com.huawei.odmf.core;

import com.huawei.odmf.user.api.ObjectContext;

public abstract class ManagedObject {
    public abstract String getDatabaseName();

    /* access modifiers changed from: protected */
    public abstract int getDirty();

    public abstract String getEntityName();

    /* access modifiers changed from: protected */
    public abstract ObjectContext getLastObjectContext();

    /* access modifiers changed from: protected */
    public abstract ObjectContext getObjectContext();

    public abstract ObjectId getObjectId();

    public abstract Long getRowId();

    /* access modifiers changed from: protected */
    public abstract int getState();

    /* access modifiers changed from: protected */
    public abstract String getUriString();

    /* access modifiers changed from: protected */
    public abstract boolean isDirty();

    /* access modifiers changed from: protected */
    public abstract boolean isPersistent();

    /* access modifiers changed from: protected */
    public abstract boolean isRelationshipUpdate(int i);

    /* access modifiers changed from: protected */
    public abstract void reSetRelationshipUpdateSigns();

    /* access modifiers changed from: protected */
    public abstract void setDirty(int i);

    /* access modifiers changed from: protected */
    public abstract void setLastObjectContext(ObjectContext objectContext);

    public abstract void setObjectContext(ObjectContext objectContext);

    /* access modifiers changed from: protected */
    public abstract void setObjectId(ObjectId objectId);

    public abstract void setRowId(Long l);

    public abstract void setState(int i);

    /* access modifiers changed from: protected */
    public abstract void setUriString(String str);
}

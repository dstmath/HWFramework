package com.huawei.odmf.model;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.api.Entity;

public abstract class AEntityHelper<T> {
    protected Entity mEntity = null;

    public abstract void bindValue(Statement statement, T t);

    public abstract int getNumberOfRelationships();

    public abstract Object getRelationshipObject(String str, T t);

    public abstract T readObject(Cursor cursor, int i);

    public abstract void setPrimaryKeyValue(T t, long j);

    public Entity getEntity() {
        return this.mEntity;
    }

    public void setEntity(Entity entity) {
        this.mEntity = entity;
    }
}

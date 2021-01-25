package ohos.data.orm;

import java.util.Objects;

public class OrmObject {
    private ObjectId objectId;
    private long rowId;

    public long getRowId() {
        return this.rowId;
    }

    public void setRowId(long j) {
        this.rowId = j;
    }

    public ObjectId getObjectId() {
        return this.objectId;
    }

    public void setObjectId(ObjectId objectId2) {
        this.objectId = objectId2;
    }

    public String getStoreAlias() {
        return this.objectId.getAlias();
    }

    public String getEntityName() {
        return this.objectId.getEntityName();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        OrmObject ormObject = (OrmObject) obj;
        return Objects.equals(Long.valueOf(this.rowId), Long.valueOf(ormObject.rowId)) && Objects.equals(this.objectId, ormObject.objectId);
    }

    public int hashCode() {
        return Objects.hash(Long.valueOf(this.rowId), this.objectId);
    }
}

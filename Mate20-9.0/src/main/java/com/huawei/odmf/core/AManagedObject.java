package com.huawei.odmf.core;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFIllegalStateException;
import com.huawei.odmf.model.AEntityHelper;
import com.huawei.odmf.user.api.ObjectContext;
import com.huawei.odmf.utils.LOG;

public class AManagedObject extends ManagedObject implements Parcelable {
    public static final Parcelable.Creator<AManagedObject> CREATOR = new Parcelable.Creator<AManagedObject>() {
        public AManagedObject createFromParcel(Parcel in) {
            return new AManagedObject(in);
        }

        public AManagedObject[] newArray(int size) {
            return new AManagedObject[size];
        }
    };
    public static final int IN_CACHE_DIRTY = 2;
    public static final int IN_CACHE_FRESH = 1;
    public static final int IN_DELETE_LIST = 3;
    public static final int IN_INSERT_LIST = 1;
    public static final int IN_UPDATE_LIST = 2;
    public static final int NEW_OBJ = 0;
    public static final int NOT_IN_CACHE = 0;
    public static final int PERSISTED = 4;
    private int dirty = 0;
    protected ObjectContext lastObjectContext = null;
    protected ObjectContext objectContext = null;
    private AObjectId objectID;
    protected int[] relationshipUpdateSigns;
    private Long rowId;
    private int state;
    private String uriString;

    /* access modifiers changed from: protected */
    public int getDirty() {
        return this.dirty;
    }

    /* access modifiers changed from: protected */
    public boolean isDirty() {
        return this.dirty == 2;
    }

    /* access modifiers changed from: protected */
    public void setDirty(int dirty2) {
        this.dirty = dirty2;
    }

    protected AManagedObject() {
        initRelationShipSigns();
    }

    private void initRelationShipSigns() {
        this.relationshipUpdateSigns = new int[getHelper().getNumberOfRelationships()];
    }

    /* access modifiers changed from: protected */
    public String getUriString() {
        return this.uriString;
    }

    public void setUriString(String uriString2) {
        this.uriString = uriString2;
        if (this.objectID != null) {
            this.objectID.setUriString(uriString2);
        }
    }

    /* access modifiers changed from: protected */
    public void reSetRelationshipUpdateSigns() {
        for (int i = 0; i < this.relationshipUpdateSigns.length; i++) {
            this.relationshipUpdateSigns[i] = 0;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isRelationshipUpdate(int i) {
        if (i >= this.relationshipUpdateSigns.length || i < 0) {
            throw new ODMFIllegalArgumentException("The index of relationship is out of range");
        } else if (this.relationshipUpdateSigns[i] == 1) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void setRelationshipUpdateSignsTrue(int index) {
        this.relationshipUpdateSigns[index] = 1;
    }

    /* access modifiers changed from: protected */
    public ObjectContext getObjectContext() {
        return this.objectContext;
    }

    /* access modifiers changed from: protected */
    public ObjectContext getLastObjectContext() {
        return this.lastObjectContext;
    }

    public void setObjectContext(ObjectContext context) {
        this.objectContext = context;
    }

    /* access modifiers changed from: protected */
    public void setLastObjectContext(ObjectContext context) {
        this.lastObjectContext = context;
    }

    /* access modifiers changed from: protected */
    public void setValue() {
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return getObjectId().equals(((AManagedObject) o).getObjectId());
    }

    public int hashCode() {
        return getObjectId().hashCode();
    }

    public ObjectId getObjectId() {
        if (this.objectID != null) {
            return this.objectID;
        }
        this.objectID = new AObjectId(getEntityName(), getRowId(), this.uriString);
        return this.objectID;
    }

    /* access modifiers changed from: protected */
    public void setObjectId(ObjectId id) {
        this.objectID = (AObjectId) id;
    }

    public void setState(int state2) {
        this.state = state2;
    }

    /* access modifiers changed from: protected */
    public int getState() {
        return this.state;
    }

    public Long getRowId() {
        return this.rowId;
    }

    /* access modifiers changed from: protected */
    public void setRowId(Long rowId2) {
        this.rowId = rowId2;
        if (this.objectID != null) {
            this.objectID.setId(rowId2);
        }
    }

    public AEntityHelper getHelper() {
        LOG.logE("execute getHelper error : A model class must implements this method, please check the class.");
        throw new ODMFIllegalStateException("execute getHelper error : A model class must implements this method, please check the class.");
    }

    public String getEntityName() {
        LOG.logE("execute getEntityName error : A model class must implements this method, please check the class.");
        throw new ODMFIllegalStateException("execute getEntityName error : A model class must implements this method, please check the class.");
    }

    /* access modifiers changed from: protected */
    public boolean isPersistent() {
        return this.state > 1;
    }

    /* access modifiers changed from: protected */
    public boolean isInUpdateList() {
        return this.state == 2;
    }

    protected AManagedObject(Parcel in) {
        this.objectID = AObjectId.CREATOR.createFromParcel(in);
        if (in.readInt() == 0) {
            this.relationshipUpdateSigns = in.createIntArray();
        } else {
            this.relationshipUpdateSigns = new int[0];
        }
        this.rowId = Long.valueOf(in.readLong());
        if (this.rowId.longValue() == -1) {
            this.rowId = null;
        }
        this.state = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (this.objectID == null) {
            this.objectID = new AObjectId(getEntityName(), this.rowId == null ? -1L : this.rowId, this.uriString);
        }
        this.objectID.writeToParcel(dest, 0);
        if (this.relationshipUpdateSigns.length == 0) {
            dest.writeInt(-1);
        } else {
            dest.writeInt(0);
            dest.writeIntArray(this.relationshipUpdateSigns);
        }
        dest.writeLong(this.rowId == null ? -1 : this.rowId.longValue());
        dest.writeInt(this.state);
    }

    public String getDatabaseName() {
        LOG.logE("execute getDatabaseName error : A model class must implements this method, please check the class.");
        throw new ODMFIllegalStateException("execute getDatabaseName error : A model class must implements this method, please check the class.");
    }
}

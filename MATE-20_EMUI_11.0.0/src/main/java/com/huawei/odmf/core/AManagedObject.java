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
        /* class com.huawei.odmf.core.AManagedObject.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AManagedObject createFromParcel(Parcel parcel) {
            return new AManagedObject(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public AManagedObject[] newArray(int i) {
            return new AManagedObject[i];
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
    private AObjectId objectId;
    protected int[] relationshipUpdateSigns;
    private Long rowId;
    private int state;
    private String uriString;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public void setValue() {
    }

    protected AManagedObject() {
        initRelationShipSigns();
    }

    protected AManagedObject(Parcel parcel) {
        this.objectId = AObjectId.CREATOR.createFromParcel(parcel);
        if (parcel.readInt() == 0) {
            this.relationshipUpdateSigns = parcel.createIntArray();
        } else {
            this.relationshipUpdateSigns = new int[0];
        }
        this.rowId = Long.valueOf(parcel.readLong());
        if (this.rowId.longValue() == -1) {
            this.rowId = null;
        }
        this.state = parcel.readInt();
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.ManagedObject
    public int getDirty() {
        return this.dirty;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.ManagedObject
    public boolean isDirty() {
        return this.dirty == 2;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.ManagedObject
    public void setDirty(int i) {
        this.dirty = i;
    }

    private void initRelationShipSigns() {
        this.relationshipUpdateSigns = new int[getHelper().getNumberOfRelationships()];
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.ManagedObject
    public String getUriString() {
        return this.uriString;
    }

    @Override // com.huawei.odmf.core.ManagedObject
    public void setUriString(String str) {
        this.uriString = str;
        AObjectId aObjectId = this.objectId;
        if (aObjectId != null) {
            aObjectId.setUriString(str);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.ManagedObject
    public void reSetRelationshipUpdateSigns() {
        int i = 0;
        while (true) {
            int[] iArr = this.relationshipUpdateSigns;
            if (i < iArr.length) {
                iArr[i] = 0;
                i++;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.ManagedObject
    public boolean isRelationshipUpdate(int i) {
        int[] iArr = this.relationshipUpdateSigns;
        if (i < iArr.length && i >= 0) {
            return iArr[i] == 1;
        }
        throw new ODMFIllegalArgumentException("The index of relationship is out of range");
    }

    /* access modifiers changed from: protected */
    public void setRelationshipUpdateSignsTrue(int i) {
        this.relationshipUpdateSigns[i] = 1;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.ManagedObject
    public ObjectContext getObjectContext() {
        return this.objectContext;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.ManagedObject
    public ObjectContext getLastObjectContext() {
        return this.lastObjectContext;
    }

    @Override // com.huawei.odmf.core.ManagedObject
    public void setObjectContext(ObjectContext objectContext2) {
        this.objectContext = objectContext2;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.ManagedObject
    public void setLastObjectContext(ObjectContext objectContext2) {
        this.lastObjectContext = objectContext2;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return getObjectId().equals(((AManagedObject) obj).getObjectId());
    }

    @Override // java.lang.Object
    public int hashCode() {
        return getObjectId().hashCode();
    }

    @Override // com.huawei.odmf.core.ManagedObject
    public ObjectId getObjectId() {
        AObjectId aObjectId = this.objectId;
        if (aObjectId != null) {
            return aObjectId;
        }
        this.objectId = new AObjectId(getEntityName(), getRowId(), this.uriString);
        return this.objectId;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.ManagedObject
    public void setObjectId(ObjectId objectId2) {
        this.objectId = (AObjectId) objectId2;
    }

    @Override // com.huawei.odmf.core.ManagedObject
    public void setState(int i) {
        this.state = i;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.ManagedObject
    public int getState() {
        return this.state;
    }

    @Override // com.huawei.odmf.core.ManagedObject
    public Long getRowId() {
        return this.rowId;
    }

    @Override // com.huawei.odmf.core.ManagedObject
    public void setRowId(Long l) {
        this.rowId = l;
        AObjectId aObjectId = this.objectId;
        if (aObjectId != null) {
            aObjectId.setId(l);
        }
    }

    public AEntityHelper getHelper() {
        LOG.logE("execute getHelper error : A model class must implements this method, please check the class.");
        throw new ODMFIllegalStateException("execute getHelper error : A model class must implements this method, please check the class.");
    }

    @Override // com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        LOG.logE("execute getEntityName error : A model class must implements this method, please check the class.");
        throw new ODMFIllegalStateException("execute getEntityName error : A model class must implements this method, please check the class.");
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.ManagedObject
    public boolean isPersistent() {
        return this.state > 1;
    }

    /* access modifiers changed from: protected */
    public boolean isInUpdateList() {
        return this.state == 2;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        long j = -1;
        if (this.objectId == null) {
            String entityName = getEntityName();
            long j2 = this.rowId;
            if (j2 == null) {
                j2 = -1L;
            }
            this.objectId = new AObjectId(entityName, j2, this.uriString);
        }
        this.objectId.writeToParcel(parcel, 0);
        if (this.relationshipUpdateSigns.length == 0) {
            parcel.writeInt(-1);
        } else {
            parcel.writeInt(0);
            parcel.writeIntArray(this.relationshipUpdateSigns);
        }
        Long l = this.rowId;
        if (l != null) {
            j = l.longValue();
        }
        parcel.writeLong(j);
        parcel.writeInt(this.state);
    }

    @Override // com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        LOG.logE("execute getDatabaseName error : A model class must implements this method, please check the class.");
        throw new ODMFIllegalStateException("execute getDatabaseName error : A model class must implements this method, please check the class.");
    }
}

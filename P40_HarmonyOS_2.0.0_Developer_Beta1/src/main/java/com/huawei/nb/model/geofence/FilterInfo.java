package com.huawei.nb.model.geofence;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import com.huawei.odmf.utils.BindUtils;
import java.sql.Blob;

public class FilterInfo extends AManagedObject {
    public static final Parcelable.Creator<FilterInfo> CREATOR = new Parcelable.Creator<FilterInfo>() {
        /* class com.huawei.nb.model.geofence.FilterInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public FilterInfo createFromParcel(Parcel parcel) {
            return new FilterInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public FilterInfo[] newArray(int i) {
            return new FilterInfo[i];
        }
    };
    private Integer mBlockId;
    private Blob mContent;
    private Long mLastUpdated;
    private Integer mReserved1;
    private Integer mReserved2;
    private Integer mRuleId;
    private Integer mType;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String getDatabaseVersion() {
        return "0.0.13";
    }

    public int getDatabaseVersionCode() {
        return 13;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.geofence.FilterInfo";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public FilterInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mRuleId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mBlockId = cursor.isNull(2) ? null : Integer.valueOf(cursor.getInt(2));
        this.mType = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.mContent = cursor.isNull(4) ? null : new com.huawei.odmf.data.Blob(cursor.getBlob(4));
        this.mLastUpdated = cursor.isNull(5) ? null : Long.valueOf(cursor.getLong(5));
        this.mReserved1 = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.mReserved2 = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
    }

    public FilterInfo(Parcel parcel) {
        super(parcel);
        Integer num = null;
        if (parcel.readByte() == 0) {
            this.mRuleId = null;
            parcel.readInt();
        } else {
            this.mRuleId = Integer.valueOf(parcel.readInt());
        }
        this.mBlockId = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mContent = parcel.readByte() == 0 ? null : com.huawei.odmf.data.Blob.CREATOR.createFromParcel(parcel);
        this.mLastUpdated = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.mReserved1 = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReserved2 = parcel.readByte() != 0 ? Integer.valueOf(parcel.readInt()) : num;
    }

    private FilterInfo(Integer num, Integer num2, Integer num3, Blob blob, Long l, Integer num4, Integer num5) {
        this.mRuleId = num;
        this.mBlockId = num2;
        this.mType = num3;
        this.mContent = blob;
        this.mLastUpdated = l;
        this.mReserved1 = num4;
        this.mReserved2 = num5;
    }

    public FilterInfo() {
    }

    public Integer getMRuleId() {
        return this.mRuleId;
    }

    public void setMRuleId(Integer num) {
        this.mRuleId = num;
        setValue();
    }

    public Integer getMBlockId() {
        return this.mBlockId;
    }

    public void setMBlockId(Integer num) {
        this.mBlockId = num;
        setValue();
    }

    public Integer getMType() {
        return this.mType;
    }

    public void setMType(Integer num) {
        this.mType = num;
        setValue();
    }

    public Blob getMContent() {
        return this.mContent;
    }

    public void setMContent(Blob blob) {
        this.mContent = blob;
        setValue();
    }

    public Long getMLastUpdated() {
        return this.mLastUpdated;
    }

    public void setMLastUpdated(Long l) {
        this.mLastUpdated = l;
        setValue();
    }

    public Integer getMReserved1() {
        return this.mReserved1;
    }

    public void setMReserved1(Integer num) {
        this.mReserved1 = num;
        setValue();
    }

    public Integer getMReserved2() {
        return this.mReserved2;
    }

    public void setMReserved2(Integer num) {
        this.mReserved2 = num;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.mRuleId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mRuleId.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.mBlockId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mBlockId.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mType.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mContent != null) {
            parcel.writeByte((byte) 1);
            Blob blob = this.mContent;
            if (blob instanceof com.huawei.odmf.data.Blob) {
                ((com.huawei.odmf.data.Blob) blob).writeToParcel(parcel, 0);
            } else {
                parcel.writeByteArray(BindUtils.bindBlob(blob));
            }
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mLastUpdated != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mLastUpdated.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mReserved1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mReserved1.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mReserved2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mReserved2.intValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<FilterInfo> getHelper() {
        return FilterInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "FilterInfo { mRuleId: " + this.mRuleId + ", mBlockId: " + this.mBlockId + ", mType: " + this.mType + ", mContent: " + this.mContent + ", mLastUpdated: " + this.mLastUpdated + ", mReserved1: " + this.mReserved1 + ", mReserved2: " + this.mReserved2 + " }";
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public int hashCode() {
        return super.hashCode();
    }
}

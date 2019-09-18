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
        public FilterInfo createFromParcel(Parcel in) {
            return new FilterInfo(in);
        }

        public FilterInfo[] newArray(int size) {
            return new FilterInfo[size];
        }
    };
    private Integer mBlockId;
    private Blob mContent;
    private Long mLastUpdated;
    private Integer mReserved1;
    private Integer mReserved2;
    private Integer mRuleId;
    private Integer mType;

    public FilterInfo(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mRuleId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mBlockId = cursor.isNull(2) ? null : Integer.valueOf(cursor.getInt(2));
        this.mType = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.mContent = cursor.isNull(4) ? null : new com.huawei.odmf.data.Blob(cursor.getBlob(4));
        this.mLastUpdated = cursor.isNull(5) ? null : Long.valueOf(cursor.getLong(5));
        this.mReserved1 = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.mReserved2 = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public FilterInfo(Parcel in) {
        super(in);
        Integer num = null;
        if (in.readByte() == 0) {
            this.mRuleId = null;
            in.readInt();
        } else {
            this.mRuleId = Integer.valueOf(in.readInt());
        }
        this.mBlockId = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mType = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mContent = in.readByte() == 0 ? null : com.huawei.odmf.data.Blob.CREATOR.createFromParcel(in);
        this.mLastUpdated = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.mReserved1 = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReserved2 = in.readByte() != 0 ? Integer.valueOf(in.readInt()) : num;
    }

    private FilterInfo(Integer mRuleId2, Integer mBlockId2, Integer mType2, Blob mContent2, Long mLastUpdated2, Integer mReserved12, Integer mReserved22) {
        this.mRuleId = mRuleId2;
        this.mBlockId = mBlockId2;
        this.mType = mType2;
        this.mContent = mContent2;
        this.mLastUpdated = mLastUpdated2;
        this.mReserved1 = mReserved12;
        this.mReserved2 = mReserved22;
    }

    public FilterInfo() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getMRuleId() {
        return this.mRuleId;
    }

    public void setMRuleId(Integer mRuleId2) {
        this.mRuleId = mRuleId2;
        setValue();
    }

    public Integer getMBlockId() {
        return this.mBlockId;
    }

    public void setMBlockId(Integer mBlockId2) {
        this.mBlockId = mBlockId2;
        setValue();
    }

    public Integer getMType() {
        return this.mType;
    }

    public void setMType(Integer mType2) {
        this.mType = mType2;
        setValue();
    }

    public Blob getMContent() {
        return this.mContent;
    }

    public void setMContent(Blob mContent2) {
        this.mContent = mContent2;
        setValue();
    }

    public Long getMLastUpdated() {
        return this.mLastUpdated;
    }

    public void setMLastUpdated(Long mLastUpdated2) {
        this.mLastUpdated = mLastUpdated2;
        setValue();
    }

    public Integer getMReserved1() {
        return this.mReserved1;
    }

    public void setMReserved1(Integer mReserved12) {
        this.mReserved1 = mReserved12;
        setValue();
    }

    public Integer getMReserved2() {
        return this.mReserved2;
    }

    public void setMReserved2(Integer mReserved22) {
        this.mReserved2 = mReserved22;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.mRuleId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mRuleId.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.mBlockId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mBlockId.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mType != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mType.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mContent != null) {
            out.writeByte((byte) 1);
            if (this.mContent instanceof com.huawei.odmf.data.Blob) {
                ((com.huawei.odmf.data.Blob) this.mContent).writeToParcel(out, 0);
            } else {
                out.writeByteArray(BindUtils.bindBlob(this.mContent));
            }
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mLastUpdated != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mLastUpdated.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReserved1 != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mReserved1.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReserved2 != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mReserved2.intValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<FilterInfo> getHelper() {
        return FilterInfoHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.geofence.FilterInfo";
    }

    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("FilterInfo { mRuleId: ").append(this.mRuleId);
        sb.append(", mBlockId: ").append(this.mBlockId);
        sb.append(", mType: ").append(this.mType);
        sb.append(", mContent: ").append(this.mContent);
        sb.append(", mLastUpdated: ").append(this.mLastUpdated);
        sb.append(", mReserved1: ").append(this.mReserved1);
        sb.append(", mReserved2: ").append(this.mReserved2);
        sb.append(" }");
        return sb.toString();
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String getDatabaseVersion() {
        return "0.0.12";
    }

    public int getDatabaseVersionCode() {
        return 12;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}

package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class CollectSwitch extends AManagedObject {
    public static final Parcelable.Creator<CollectSwitch> CREATOR = new Parcelable.Creator<CollectSwitch>() {
        public CollectSwitch createFromParcel(Parcel in) {
            return new CollectSwitch(in);
        }

        public CollectSwitch[] newArray(int size) {
            return new CollectSwitch[size];
        }
    };
    private String mDataName;
    private String mModuleName;
    private Integer mReservedInt;
    private String mReservedText;
    private String mTimeText;

    public CollectSwitch(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mDataName = cursor.getString(1);
        this.mModuleName = cursor.getString(2);
        this.mTimeText = cursor.getString(3);
        this.mReservedInt = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.mReservedText = cursor.getString(5);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public CollectSwitch(Parcel in) {
        super(in);
        String str = null;
        this.mDataName = in.readByte() == 0 ? null : in.readString();
        this.mModuleName = in.readByte() == 0 ? null : in.readString();
        this.mTimeText = in.readByte() == 0 ? null : in.readString();
        this.mReservedInt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedText = in.readByte() != 0 ? in.readString() : str;
    }

    private CollectSwitch(String mDataName2, String mModuleName2, String mTimeText2, Integer mReservedInt2, String mReservedText2) {
        this.mDataName = mDataName2;
        this.mModuleName = mModuleName2;
        this.mTimeText = mTimeText2;
        this.mReservedInt = mReservedInt2;
        this.mReservedText = mReservedText2;
    }

    public CollectSwitch() {
    }

    public int describeContents() {
        return 0;
    }

    public String getMDataName() {
        return this.mDataName;
    }

    public void setMDataName(String mDataName2) {
        this.mDataName = mDataName2;
        setValue();
    }

    public String getMModuleName() {
        return this.mModuleName;
    }

    public void setMModuleName(String mModuleName2) {
        this.mModuleName = mModuleName2;
        setValue();
    }

    public String getMTimeText() {
        return this.mTimeText;
    }

    public void setMTimeText(String mTimeText2) {
        this.mTimeText = mTimeText2;
        setValue();
    }

    public Integer getMReservedInt() {
        return this.mReservedInt;
    }

    public void setMReservedInt(Integer mReservedInt2) {
        this.mReservedInt = mReservedInt2;
        setValue();
    }

    public String getMReservedText() {
        return this.mReservedText;
    }

    public void setMReservedText(String mReservedText2) {
        this.mReservedText = mReservedText2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.mDataName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mDataName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mModuleName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mModuleName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mTimeText != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mTimeText);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReservedInt != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mReservedInt.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReservedText != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mReservedText);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<CollectSwitch> getHelper() {
        return CollectSwitchHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.CollectSwitch";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("CollectSwitch { mDataName: ").append(this.mDataName);
        sb.append(", mModuleName: ").append(this.mModuleName);
        sb.append(", mTimeText: ").append(this.mTimeText);
        sb.append(", mReservedInt: ").append(this.mReservedInt);
        sb.append(", mReservedText: ").append(this.mReservedText);
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
        return "0.0.14";
    }

    public int getDatabaseVersionCode() {
        return 14;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}

package com.huawei.nb.searchmanager.client;

import android.os.Parcel;
import android.os.Parcelable;

public class Attributes implements Parcelable {
    public static final Parcelable.Creator<Attributes> CREATOR = new Parcelable.Creator<Attributes>() {
        public Attributes createFromParcel(Parcel in) {
            return new Attributes(in);
        }

        public Attributes[] newArray(int size) {
            return new Attributes[size];
        }
    };
    private String dataFieldName;
    private String indexFieldName;
    private String indexFieldValue;
    private String indexStatus;
    private boolean isPrimaryKey;
    private String storeStatus;

    public Attributes() {
    }

    public String getDataFieldName() {
        return this.dataFieldName;
    }

    public void setDataFieldName(String dataFieldName2) {
        this.dataFieldName = dataFieldName2;
    }

    public boolean isPrimaryKey() {
        return this.isPrimaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.isPrimaryKey = primaryKey;
    }

    public String getIndexFieldName() {
        return this.indexFieldName;
    }

    public void setIndexFieldName(String indexFieldName2) {
        this.indexFieldName = indexFieldName2;
    }

    public String getIndexFieldValue() {
        return this.indexFieldValue;
    }

    public void setIndexFieldValue(String indexFieldValue2) {
        this.indexFieldValue = indexFieldValue2;
    }

    public String getStoreStatus() {
        return this.storeStatus;
    }

    public void setStoreStatus(String storeStatus2) {
        this.storeStatus = storeStatus2;
    }

    public String getIndexStatus() {
        return this.indexStatus;
    }

    public void setIndexStatus(String indexStatus2) {
        this.indexStatus = indexStatus2;
    }

    protected Attributes(Parcel in) {
        this.dataFieldName = in.readString();
        this.isPrimaryKey = in.readByte() != 0;
        this.indexFieldName = in.readString();
        this.indexFieldValue = in.readString();
        this.storeStatus = in.readString();
        this.indexStatus = in.readString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.dataFieldName);
        dest.writeByte((byte) (this.isPrimaryKey ? 1 : 0));
        dest.writeString(this.indexFieldName);
        dest.writeString(this.indexFieldValue);
        dest.writeString(this.storeStatus);
        dest.writeString(this.indexStatus);
    }

    public int describeContents() {
        return 0;
    }
}

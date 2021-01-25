package com.huawei.nb.searchmanager.client;

import android.os.Parcel;
import android.os.Parcelable;

public class Attributes implements Parcelable {
    public static final Parcelable.Creator<Attributes> CREATOR = new Parcelable.Creator<Attributes>() {
        /* class com.huawei.nb.searchmanager.client.Attributes.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Attributes createFromParcel(Parcel parcel) {
            return new Attributes(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public Attributes[] newArray(int i) {
            return new Attributes[i];
        }
    };
    private String dataFieldName;
    private String indexFieldName;
    private String indexFieldValue;
    private String indexStatus;
    private boolean isPrimaryKey;
    private String storeStatus;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public Attributes() {
    }

    protected Attributes(Parcel parcel) {
        this.dataFieldName = parcel.readString();
        this.isPrimaryKey = parcel.readByte() != 0;
        this.indexFieldName = parcel.readString();
        this.indexFieldValue = parcel.readString();
        this.storeStatus = parcel.readString();
        this.indexStatus = parcel.readString();
    }

    public String getDataFieldName() {
        return this.dataFieldName;
    }

    public void setDataFieldName(String str) {
        this.dataFieldName = str;
    }

    public boolean isPrimaryKey() {
        return this.isPrimaryKey;
    }

    public void setPrimaryKey(boolean z) {
        this.isPrimaryKey = z;
    }

    public String getIndexFieldName() {
        return this.indexFieldName;
    }

    public void setIndexFieldName(String str) {
        this.indexFieldName = str;
    }

    public String getIndexFieldValue() {
        return this.indexFieldValue;
    }

    public void setIndexFieldValue(String str) {
        this.indexFieldValue = str;
    }

    public String getStoreStatus() {
        return this.storeStatus;
    }

    public void setStoreStatus(String str) {
        this.storeStatus = str;
    }

    public String getIndexStatus() {
        return this.indexStatus;
    }

    public void setIndexStatus(String str) {
        this.indexStatus = str;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.dataFieldName);
        parcel.writeByte(this.isPrimaryKey ? (byte) 1 : 0);
        parcel.writeString(this.indexFieldName);
        parcel.writeString(this.indexFieldValue);
        parcel.writeString(this.storeStatus);
        parcel.writeString(this.indexStatus);
    }
}

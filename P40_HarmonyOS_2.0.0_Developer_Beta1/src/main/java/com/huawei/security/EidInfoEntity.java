package com.huawei.security;

import android.os.Parcel;
import android.os.Parcelable;

public class EidInfoEntity implements Parcelable {
    public static final Parcelable.Creator<EidInfoEntity> CREATOR = new Parcelable.Creator<EidInfoEntity>() {
        /* class com.huawei.security.EidInfoEntity.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public EidInfoEntity createFromParcel(Parcel source) {
            return new EidInfoEntity(source);
        }

        @Override // android.os.Parcelable.Creator
        public EidInfoEntity[] newArray(int size) {
            return new EidInfoEntity[size];
        }
    };
    private byte[] content;
    private int contentLen;

    public EidInfoEntity() {
        this.contentLen = 0;
    }

    private EidInfoEntity(Parcel source) {
        this.contentLen = 0;
        this.content = source.createByteArray();
        this.contentLen = source.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel source) {
        this.content = source.createByteArray();
        this.contentLen = source.readInt();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(this.content);
        dest.writeInt(this.contentLen);
    }

    public byte[] getContent() {
        return this.content;
    }

    public void setContent(byte[] content2) {
        this.content = content2;
    }

    public int getContentLen() {
        return this.contentLen;
    }

    public void setContentLen(int contentLen2) {
        this.contentLen = contentLen2;
    }
}

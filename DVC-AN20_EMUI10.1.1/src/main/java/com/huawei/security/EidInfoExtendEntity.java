package com.huawei.security;

import android.os.Parcel;
import android.os.Parcelable;

public class EidInfoExtendEntity implements Parcelable {
    private static final int CONTENT_LEN = 1;
    public static final Parcelable.Creator<EidInfoExtendEntity> CREATOR = new Parcelable.Creator<EidInfoExtendEntity>() {
        /* class com.huawei.security.EidInfoExtendEntity.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public EidInfoExtendEntity createFromParcel(Parcel source) {
            return new EidInfoExtendEntity(source);
        }

        @Override // android.os.Parcelable.Creator
        public EidInfoExtendEntity[] newArray(int size) {
            return new EidInfoExtendEntity[size];
        }
    };
    private byte[] content;
    private int[] contentLen;

    public EidInfoExtendEntity() {
        this.contentLen = new int[1];
    }

    private EidInfoExtendEntity(Parcel source) {
        this.contentLen = new int[1];
        this.content = source.createByteArray();
        this.contentLen = source.createIntArray();
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel source) {
        this.content = source.createByteArray();
        this.contentLen = source.createIntArray();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(this.content);
        dest.writeIntArray(this.contentLen);
    }

    public byte[] getContent() {
        return this.content;
    }

    public void setContent(byte[] content2) {
        this.content = content2;
    }

    public int[] getContentLen() {
        return this.contentLen;
    }

    public void setContentLen(int[] contentLen2) {
        this.contentLen = contentLen2;
    }
}

package com.huawei.security;

import android.os.Parcel;
import android.os.Parcelable;

public class CoordinateEntity implements Parcelable {
    public static final Parcelable.Creator<CoordinateEntity> CREATOR = new Parcelable.Creator<CoordinateEntity>() {
        /* class com.huawei.security.CoordinateEntity.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CoordinateEntity createFromParcel(Parcel source) {
            return new CoordinateEntity(source);
        }

        @Override // android.os.Parcelable.Creator
        public CoordinateEntity[] newArray(int size) {
            return new CoordinateEntity[size];
        }
    };
    private int down;
    private int left;
    private int right;
    private int up;

    public CoordinateEntity() {
    }

    private CoordinateEntity(Parcel source) {
        this.up = source.readInt();
        this.down = source.readInt();
        this.left = source.readInt();
        this.right = source.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel source) {
        this.up = source.readInt();
        this.down = source.readInt();
        this.left = source.readInt();
        this.right = source.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.up);
        dest.writeInt(this.down);
        dest.writeInt(this.left);
        dest.writeInt(this.right);
    }

    public int getUp() {
        return this.up;
    }

    public void setUp(int up2) {
        this.up = up2;
    }

    public int getDown() {
        return this.down;
    }

    public void setDown(int down2) {
        this.down = down2;
    }

    public int getLeft() {
        return this.left;
    }

    public void setLeft(int left2) {
        this.left = left2;
    }

    public int getRight() {
        return this.right;
    }

    public void setRight(int right2) {
        this.right = right2;
    }
}

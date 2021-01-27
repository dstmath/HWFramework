package com.huawei.nb.searchmanager.client.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Token implements Parcelable {
    public static final Parcelable.Creator<Token> CREATOR = new Parcelable.Creator<Token>() {
        /* class com.huawei.nb.searchmanager.client.model.Token.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Token createFromParcel(Parcel parcel) {
            return new Token(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public Token[] newArray(int i) {
            return new Token[i];
        }
    };
    private boolean finish;
    private String tokenDescription;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public Token() {
        this.finish = false;
        this.finish = false;
        this.tokenDescription = toString() + System.currentTimeMillis();
    }

    private Token(Parcel parcel) {
        boolean z = false;
        this.finish = false;
        this.finish = parcel.readInt() == 1 ? true : z;
        this.tokenDescription = parcel.readString();
    }

    public boolean isFinish() {
        return this.finish;
    }

    public void setFinish(boolean z) {
        this.finish = z;
    }

    public String getTokenDescription() {
        return this.tokenDescription;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.finish ? 1 : 0);
        parcel.writeString(this.tokenDescription);
    }

    public void readFromParcel(Parcel parcel) {
        boolean z = true;
        if (parcel.readInt() != 1) {
            z = false;
        }
        this.finish = z;
        this.tokenDescription = parcel.readString();
    }
}

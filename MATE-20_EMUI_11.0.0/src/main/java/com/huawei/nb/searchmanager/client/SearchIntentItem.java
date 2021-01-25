package com.huawei.nb.searchmanager.client;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchIntentItem implements Parcelable {
    public static final Parcelable.Creator<SearchIntentItem> CREATOR = new Parcelable.Creator<SearchIntentItem>() {
        /* class com.huawei.nb.searchmanager.client.SearchIntentItem.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SearchIntentItem createFromParcel(Parcel parcel) {
            return new SearchIntentItem(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public SearchIntentItem[] newArray(int i) {
            return new SearchIntentItem[i];
        }
    };
    private String businessType;
    private String id;
    private String name;
    private String text1;
    private String text2;
    private String text3;
    private String text4;
    private String userId;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public SearchIntentItem() {
        this.id = "";
        this.userId = "";
        this.name = "";
        this.text1 = "";
        this.text2 = "";
        this.text3 = "";
        this.text4 = "";
        this.businessType = "";
    }

    protected SearchIntentItem(Parcel parcel) {
        this.id = parcel.readString();
        this.userId = parcel.readString();
        this.name = parcel.readString();
        this.text1 = parcel.readString();
        this.text2 = parcel.readString();
        this.text3 = parcel.readString();
        this.text4 = parcel.readString();
        this.businessType = parcel.readString();
    }

    public String getId() {
        return this.id;
    }

    public void setId(String str) {
        this.id = str;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String str) {
        this.userId = str;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public String getText1() {
        return this.text1;
    }

    public void setText1(String str) {
        this.text1 = str;
    }

    public String getText2() {
        return this.text2;
    }

    public void setText2(String str) {
        this.text2 = str;
    }

    public String getText3() {
        return this.text3;
    }

    public void setText3(String str) {
        this.text3 = str;
    }

    public String getText4() {
        return this.text4;
    }

    public void setText4(String str) {
        this.text4 = str;
    }

    public String getBusinessType() {
        return this.businessType;
    }

    public void setBusinessType(String str) {
        this.businessType = str;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.id);
        parcel.writeString(this.userId);
        parcel.writeString(this.name);
        parcel.writeString(this.text1);
        parcel.writeString(this.text2);
        parcel.writeString(this.text3);
        parcel.writeString(this.text4);
        parcel.writeString(this.businessType);
    }
}

package com.huawei.nb.searchmanager.client;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchIntentItem implements Parcelable {
    public static final Parcelable.Creator<SearchIntentItem> CREATOR = new Parcelable.Creator<SearchIntentItem>() {
        public SearchIntentItem createFromParcel(Parcel in) {
            return new SearchIntentItem(in);
        }

        public SearchIntentItem[] newArray(int size) {
            return new SearchIntentItem[size];
        }
    };
    private String _id;
    private String businessType;
    private String name;
    private String text1;
    private String text2;
    private String text3;
    private String text4;
    private String userId;

    public SearchIntentItem() {
        this._id = "";
        this.userId = "";
        this.name = "";
        this.text1 = "";
        this.text2 = "";
        this.text3 = "";
        this.text4 = "";
        this.businessType = "";
    }

    protected SearchIntentItem(Parcel in) {
        this._id = in.readString();
        this.userId = in.readString();
        this.name = in.readString();
        this.text1 = in.readString();
        this.text2 = in.readString();
        this.text3 = in.readString();
        this.text4 = in.readString();
        this.businessType = in.readString();
    }

    public String get_id() {
        return this._id;
    }

    public void set_id(String _id2) {
        this._id = _id2;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId2) {
        this.userId = userId2;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
    }

    public String getText1() {
        return this.text1;
    }

    public void setText1(String text12) {
        this.text1 = text12;
    }

    public String getText2() {
        return this.text2;
    }

    public void setText2(String text22) {
        this.text2 = text22;
    }

    public String getText3() {
        return this.text3;
    }

    public void setText3(String text32) {
        this.text3 = text32;
    }

    public String getText4() {
        return this.text4;
    }

    public void setText4(String text42) {
        this.text4 = text42;
    }

    public String getBusinessType() {
        return this.businessType;
    }

    public void setBusinessType(String businessType2) {
        this.businessType = businessType2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this._id);
        dest.writeString(this.userId);
        dest.writeString(this.name);
        dest.writeString(this.text1);
        dest.writeString(this.text2);
        dest.writeString(this.text3);
        dest.writeString(this.text4);
        dest.writeString(this.businessType);
    }
}

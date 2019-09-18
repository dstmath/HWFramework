package com.huawei.recsys.aidl;

import android.annotation.TargetApi;
import android.os.Parcel;
import android.os.Parcelable;

public class HwRecResult implements Parcelable {
    public static final Parcelable.Creator<HwRecResult> CREATOR = new Parcelable.Creator<HwRecResult>() {
        public HwRecResult createFromParcel(Parcel source) {
            return new HwRecResult(source);
        }

        public HwRecResult[] newArray(int size) {
            return new HwRecResult[size];
        }
    };
    private String activity;
    private String hurl;
    private String icon;
    private int id;
    private String intent;
    private String pkg;
    private double proba;
    private String service;
    private int shortcut;
    private int startMode;
    private String titleCn;
    private String titleEn;

    public String toString() {
        return "HwRecResult{id=" + this.id + ", activity='" + this.activity + '\'' + ", hurl='" + this.hurl + '\'' + ", icon='" + this.icon + '\'' + ", intent='" + this.intent + '\'' + ", pkg='" + this.pkg + '\'' + ", service='" + this.service + '\'' + ", startMode=" + this.startMode + ", titleCn='" + this.titleCn + '\'' + ", titleEn='" + this.titleEn + '\'' + ", proba=" + this.proba + ", shortcut=" + this.shortcut + '}';
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id2) {
        this.id = id2;
    }

    public String getActivity() {
        return this.activity;
    }

    public void setActivity(String activity2) {
        this.activity = activity2;
    }

    public String getHurl() {
        return this.hurl;
    }

    public void setHurl(String hurl2) {
        this.hurl = hurl2;
    }

    public String getIcon() {
        return this.icon;
    }

    public void setIcon(String icon2) {
        this.icon = icon2;
    }

    public String getIntent() {
        return this.intent;
    }

    public void setIntent(String intent2) {
        this.intent = intent2;
    }

    public String getPkg() {
        return this.pkg;
    }

    public void setPkg(String pkg2) {
        this.pkg = pkg2;
    }

    public String getService() {
        return this.service;
    }

    public void setService(String service2) {
        this.service = service2;
    }

    public int getStartMode() {
        return this.startMode;
    }

    public void setStartMode(int startMode2) {
        this.startMode = startMode2;
    }

    public String getTitleCn() {
        return this.titleCn;
    }

    public void setTitleCn(String titleCn2) {
        this.titleCn = titleCn2;
    }

    public String getTitleEn() {
        return this.titleEn;
    }

    public void setTitleEn(String titleEn2) {
        this.titleEn = titleEn2;
    }

    public double getProba() {
        return this.proba;
    }

    public void setProba(double proba2) {
        this.proba = proba2;
    }

    public int getShortcut() {
        return this.shortcut;
    }

    public void setShortcut(int shortcut2) {
        this.shortcut = shortcut2;
    }

    public HwRecResult() {
    }

    public HwRecResult(Parcel in) {
        this.id = in.readInt();
        this.activity = in.readString();
        this.hurl = in.readString();
        this.icon = in.readString();
        this.intent = in.readString();
        this.pkg = in.readString();
        this.service = in.readString();
        this.startMode = in.readInt();
        this.titleCn = in.readString();
        this.titleEn = in.readString();
        this.proba = in.readDouble();
        this.shortcut = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel in) {
        this.id = in.readInt();
        this.activity = in.readString();
        this.hurl = in.readString();
        this.icon = in.readString();
        this.intent = in.readString();
        this.pkg = in.readString();
        this.service = in.readString();
        this.startMode = in.readInt();
        this.titleCn = in.readString();
        this.titleEn = in.readString();
        this.proba = in.readDouble();
        this.shortcut = in.readInt();
    }

    @TargetApi(25)
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.activity);
        dest.writeString(this.hurl);
        dest.writeString(this.icon);
        dest.writeString(this.intent);
        dest.writeString(this.pkg);
        dest.writeString(this.service);
        dest.writeInt(this.startMode);
        dest.writeString(this.titleCn);
        dest.writeString(this.titleEn);
        dest.writeDouble(this.proba);
        dest.writeInt(this.shortcut);
    }
}

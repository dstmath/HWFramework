package com.huawei.recsys.aidl;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.List;

public class HwHighLightRecResult implements Parcelable {
    public static final Creator<HwHighLightRecResult> CREATOR = new Creator<HwHighLightRecResult>() {
        public HwHighLightRecResult createFromParcel(Parcel in) {
            return new HwHighLightRecResult(in);
        }

        public HwHighLightRecResult[] newArray(int size) {
            return new HwHighLightRecResult[size];
        }
    };
    private String action;
    private int actionType;
    private List<String> content;
    private String extraInfo;
    private int priority;
    private String rule;

    public String getRule() {
        return this.rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getExtraInfo() {
        return this.extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public List<String> getContent() {
        return this.content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }

    public int getActionType() {
        return this.actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    protected HwHighLightRecResult(Parcel in) {
        this.rule = in.readString();
        this.action = in.readString();
        this.extraInfo = in.readString();
        this.content = in.createStringArrayList();
        this.actionType = in.readInt();
        this.priority = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.rule);
        dest.writeString(this.action);
        dest.writeString(this.extraInfo);
        dest.writeStringList(this.content);
        dest.writeInt(this.actionType);
        dest.writeInt(this.priority);
    }
}

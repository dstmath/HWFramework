package com.huawei.nb.diffprivacy;

import android.os.Parcel;
import android.os.Parcelable;

public class DiffPrivacyRequestInfo implements Parcelable {
    public static final Parcelable.Creator<DiffPrivacyRequestInfo> CREATOR = new Parcelable.Creator<DiffPrivacyRequestInfo>() {
        public DiffPrivacyRequestInfo createFromParcel(Parcel in) {
            return new DiffPrivacyRequestInfo(in);
        }

        public DiffPrivacyRequestInfo[] newArray(int size) {
            return new DiffPrivacyRequestInfo[size];
        }
    };
    private String data;
    private String filter;
    private String parameter;
    private String taskName;

    public DiffPrivacyRequestInfo(String taskName2, String filter2, String parameter2, String data2) {
        this.taskName = taskName2;
        this.filter = filter2;
        this.parameter = parameter2;
        this.data = data2;
    }

    public String getTaskName() {
        return this.taskName;
    }

    public void setTaskName(String taskName2) {
        this.taskName = taskName2;
    }

    public String getFilter() {
        return this.filter;
    }

    public void setFilter(String filter2) {
        this.filter = filter2;
    }

    public String getParameter() {
        return this.parameter;
    }

    public void setParameter(String parameter2) {
        this.parameter = parameter2;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data2) {
        this.data = data2;
    }

    protected DiffPrivacyRequestInfo(Parcel in) {
        this.taskName = in.readString();
        this.filter = in.readString();
        this.parameter = in.readString();
        this.data = in.readString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.taskName);
        dest.writeString(this.filter);
        dest.writeString(this.parameter);
        dest.writeString(this.data);
    }

    public int describeContents() {
        return 0;
    }
}

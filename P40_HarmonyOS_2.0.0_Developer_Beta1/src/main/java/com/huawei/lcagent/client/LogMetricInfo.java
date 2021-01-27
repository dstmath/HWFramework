package com.huawei.lcagent.client;

import android.os.Parcel;
import android.os.Parcelable;

public class LogMetricInfo implements Parcelable {
    public static final Parcelable.Creator<LogMetricInfo> CREATOR = new Parcelable.Creator<LogMetricInfo>() {
        /* class com.huawei.lcagent.client.LogMetricInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public LogMetricInfo createFromParcel(Parcel in) {
            return new LogMetricInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public LogMetricInfo[] newArray(int size) {
            return new LogMetricInfo[size];
        }
    };
    public String description;
    public String[] files;
    public long id;
    public String logDetailedInfo;
    public String path;
    public String zipTime;

    public LogMetricInfo() {
        this.id = 0;
        this.description = null;
        this.files = null;
        this.path = null;
        this.zipTime = null;
        this.logDetailedInfo = null;
    }

    public LogMetricInfo(long id2, String path2, String description2, String[] files2, String zipTime2, String logDetailedInfo2) {
        this.id = id2;
        this.path = path2;
        this.description = description2;
        this.zipTime = zipTime2;
        this.logDetailedInfo = logDetailedInfo2;
        if (files2 == null || files2.length == 0) {
            this.files = null;
            return;
        }
        this.files = new String[files2.length];
        int length = files2.length;
        for (int i = 0; i < length; i++) {
            this.files[i] = files2[i];
        }
    }

    private LogMetricInfo(Parcel in) {
        this.id = in.readLong();
        this.path = in.readString();
        this.description = in.readString();
        this.files = in.createStringArray();
        this.zipTime = in.readString();
        this.logDetailedInfo = in.readString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.path);
        dest.writeString(this.description);
        dest.writeStringArray(this.files);
        dest.writeString(this.zipTime);
        dest.writeString(this.logDetailedInfo);
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id = ");
        sb.append(this.id);
        sb.append("\n");
        sb.append("path = ");
        sb.append(this.path);
        sb.append("\n");
        sb.append("description = ");
        sb.append(this.description);
        sb.append("\n");
        String[] strArr = this.files;
        if (strArr == null) {
            return sb.toString();
        }
        int length = strArr.length;
        for (int i = 0; i < length; i++) {
            sb.append("files[");
            sb.append(i);
            sb.append("]=");
            sb.append(this.files[i]);
            sb.append("\n");
        }
        sb.append("zipTime = ");
        sb.append(this.zipTime);
        sb.append("\n");
        sb.append("logDetailedInfo = ");
        sb.append(this.logDetailedInfo);
        sb.append("\n");
        return sb.toString();
    }
}

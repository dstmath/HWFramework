package com.huawei.lcagent.client;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class LogMetricInfo implements Parcelable {
    public static final Creator<LogMetricInfo> CREATOR = new Creator<LogMetricInfo>() {
        public LogMetricInfo createFromParcel(Parcel in) {
            return new LogMetricInfo(in, null);
        }

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

    /* synthetic */ LogMetricInfo(Parcel in, LogMetricInfo -this1) {
        this(in);
    }

    public LogMetricInfo() {
        this.id = 0;
        this.description = null;
        this.files = null;
        this.path = null;
        this.zipTime = null;
        this.logDetailedInfo = null;
    }

    public LogMetricInfo(long id, String path, String description, String[] files, String zipTime, String logDetailedInfo) {
        this.id = id;
        this.path = path;
        this.description = description;
        this.zipTime = zipTime;
        this.logDetailedInfo = logDetailedInfo;
        if (files == null || files.length == 0) {
            this.files = null;
            return;
        }
        this.files = new String[files.length];
        int length = files.length;
        for (int i = 0; i < length; i++) {
            this.files[i] = files[i];
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.path);
        dest.writeString(this.description);
        dest.writeStringArray(this.files);
        dest.writeString(this.zipTime);
        dest.writeString(this.logDetailedInfo);
    }

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
        if (this.files == null) {
            return sb.toString();
        }
        int length = this.files.length;
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

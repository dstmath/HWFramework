package com.huawei.lcagent.client;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class CompressInfo implements Parcelable {
    public static final int COLLECT_LOG = 0;
    public static final int COMPRESS_LOG = 1;
    public static final Creator<CompressInfo> CREATOR = new Creator<CompressInfo>() {
        public CompressInfo createFromParcel(Parcel in) {
            return new CompressInfo(in, null);
        }

        public CompressInfo[] newArray(int size) {
            return new CompressInfo[size];
        }
    };
    public static final int FINISHED = 2;
    public String description;
    public String path;
    public int progress;
    public int status;

    /* synthetic */ CompressInfo(Parcel in, CompressInfo -this1) {
        this(in);
    }

    public CompressInfo() {
        this.status = 0;
        this.progress = 0;
        this.path = "";
        this.description = "";
    }

    private CompressInfo(Parcel in) {
        this.status = in.readInt();
        this.progress = in.readInt();
        this.path = in.readString();
        this.description = in.readString();
    }

    public void setCompressInfo(int status, int progress, String path, String description) {
        this.status = status;
        this.progress = progress;
        this.path = path;
        this.description = description;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.status);
        dest.writeInt(this.progress);
        dest.writeString(this.path);
        dest.writeString(this.description);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("status = ");
        sb.append(this.status);
        sb.append("\n");
        sb.append("progress = ");
        sb.append(this.progress);
        sb.append("%");
        sb.append("\n");
        sb.append("path = ");
        sb.append(this.path);
        sb.append("\n");
        sb.append("description = ");
        sb.append(this.description);
        sb.append("\n");
        return sb.toString();
    }

    public void readFromParcel(Parcel in) {
        this.status = in.readInt();
        this.progress = in.readInt();
        this.path = in.readString();
        this.description = in.readString();
    }
}

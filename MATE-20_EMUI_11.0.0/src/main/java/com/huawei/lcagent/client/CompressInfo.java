package com.huawei.lcagent.client;

import android.os.Parcel;
import android.os.Parcelable;

public class CompressInfo implements Parcelable {
    public static final int COLLECT_LOG = 0;
    public static final int COMPRESS_LOG = 1;
    public static final Parcelable.Creator<CompressInfo> CREATOR = new Parcelable.Creator<CompressInfo>() {
        /* class com.huawei.lcagent.client.CompressInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CompressInfo createFromParcel(Parcel in) {
            return new CompressInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public CompressInfo[] newArray(int size) {
            return new CompressInfo[size];
        }
    };
    public static final int FINISHED = 2;
    public String description;
    public String path;
    public int progress;
    public int status;

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

    public void setCompressInfo(int status2, int progress2, String path2, String description2) {
        this.status = status2;
        this.progress = progress2;
        this.path = path2;
        this.description = description2;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.status);
        dest.writeInt(this.progress);
        dest.writeString(this.path);
        dest.writeString(this.description);
    }

    @Override // java.lang.Object
    public String toString() {
        return "status = " + this.status + "\nprogress = " + this.progress + "%\npath = " + this.path + "\ndescription = " + this.description + "\n";
    }

    public void readFromParcel(Parcel in) {
        this.status = in.readInt();
        this.progress = in.readInt();
        this.path = in.readString();
        this.description = in.readString();
    }
}

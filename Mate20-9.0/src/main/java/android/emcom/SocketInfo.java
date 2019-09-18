package android.emcom;

import android.os.Parcel;
import android.os.Parcelable;

public class SocketInfo implements Parcelable {
    public static final Parcelable.Creator<SocketInfo> CREATOR = new Parcelable.Creator<SocketInfo>() {
        public SocketInfo createFromParcel(Parcel in) {
            return new SocketInfo(in);
        }

        public SocketInfo[] newArray(int size) {
            return new SocketInfo[size];
        }
    };
    protected int fd = -1;
    protected int pid = -1;

    public SocketInfo() {
    }

    protected SocketInfo(Parcel in) {
        this.fd = in.readInt();
        this.pid = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.fd);
        parcel.writeInt(this.pid);
    }

    public int getFd() {
        return this.fd;
    }

    public void setFd(int fd2) {
        this.fd = fd2;
    }

    public int getPid() {
        return this.pid;
    }

    public void setPid(int pid2) {
        this.pid = pid2;
    }

    public String toString() {
        return "SocketInfo{fd=" + this.fd + ", pid=" + this.pid + '}';
    }
}

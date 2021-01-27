package android.emcom;

import android.os.Parcel;
import android.os.Parcelable;

public class SocketInfo implements Parcelable {
    public static final Parcelable.Creator<SocketInfo> CREATOR = new Parcelable.Creator<SocketInfo>() {
        /* class android.emcom.SocketInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SocketInfo createFromParcel(Parcel in) {
            return new SocketInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public SocketInfo[] newArray(int size) {
            if (size > 0) {
                return new SocketInfo[size];
            }
            return new SocketInfo[0];
        }
    };
    private static final int FD_DEFAULT = -1;
    private static final int PID_DEFAULT = -1;
    protected int fd = -1;
    protected int pid = -1;

    public SocketInfo() {
    }

    protected SocketInfo(Parcel in) {
        if (in != null) {
            this.fd = in.readInt();
            this.pid = in.readInt();
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        if (parcel != null) {
            parcel.writeInt(this.fd);
            parcel.writeInt(this.pid);
        }
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

    @Override // java.lang.Object
    public String toString() {
        return "SocketInfo{fd=" + this.fd + ", pid=" + this.pid + '}';
    }
}

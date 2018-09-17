package com.hisi.mapcon;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;

public class ServerAddr implements Parcelable {
    public static final Creator<ServerAddr> CREATOR = new Creator<ServerAddr>() {
        public ServerAddr createFromParcel(Parcel in) {
            return new ServerAddr(in);
        }

        public ServerAddr[] newArray(int size) {
            return new ServerAddr[size];
        }
    };
    ArrayList<ServerAddrPair> mAddress;
    int mSize;

    public static class ServerAddrPair {
        String address;
        int type;

        public ServerAddrPair(int type, String addr) {
            this.type = type;
            this.address = addr;
        }
    }

    public ServerAddr() {
        this.mAddress = new ArrayList();
        this.mSize = 0;
    }

    public ServerAddr(Parcel in) {
        this.mSize = in.readInt();
        this.mAddress = new ArrayList();
        for (int index = 0; index < this.mSize; index++) {
            this.mAddress.add(new ServerAddrPair(in.readInt(), in.readString()));
        }
    }

    public void add(int type, String addr) {
        this.mAddress.add(new ServerAddrPair(type, addr));
        this.mSize++;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int arg1) {
        int size = this.mAddress.size();
        out.writeInt(this.mSize);
        for (int index = 0; index < size; index++) {
            ServerAddrPair pair = (ServerAddrPair) this.mAddress.get(index);
            out.writeInt(pair.type);
            out.writeString(pair.address);
        }
    }

    public String toString() {
        StringBuffer retString = new StringBuffer();
        retString.append("serverAddr:size is:");
        retString.append(this.mAddress.size());
        for (int index = 0; index < this.mAddress.size(); index++) {
            ServerAddrPair pair = (ServerAddrPair) this.mAddress.get(index);
            retString.append("index:" + index + " type:" + pair.type + " addr:" + pair.address);
        }
        return retString.toString();
    }
}

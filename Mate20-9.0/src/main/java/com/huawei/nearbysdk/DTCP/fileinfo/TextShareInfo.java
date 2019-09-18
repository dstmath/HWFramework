package com.huawei.nearbysdk.DTCP.fileinfo;

import android.os.Parcel;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TextShareInfo extends BaseShareInfo {
    public TextShareInfo() {
        this.mInfoType = 5;
    }

    public TextShareInfo(BasePreviewInfo pInfo) {
        super(pInfo);
        this.mInfoType = 5;
    }

    protected TextShareInfo(Parcel in) {
        super(in);
        this.mInfoType = 5;
    }

    public String toString() {
        return "TextShareInfo:Now not use";
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mInfoType);
        super.writeToParcel(dest, flags);
    }

    public void writeToDTCPStream(DataOutputStream dos, int dtcpVersion) throws IOException {
        super.writeToDTCPStream(dos, dtcpVersion);
    }

    public void readFromDTCPStream(DataInputStream dis, int dtcpVersion) throws IOException {
        super.readFromDTCPStream(dis, dtcpVersion);
    }
}

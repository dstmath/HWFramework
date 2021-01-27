package com.huawei.nearbysdk.DTCP.fileinfo;

import android.os.Parcel;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TextShareInfo extends BaseShareInfo {
    private static final long serialVersionUID = -4768043125556585366L;

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

    @Override // com.huawei.nearbysdk.DTCP.fileinfo.BaseShareInfo, java.lang.Object
    public String toString() {
        return "TextShareInfo:Now not use";
    }

    @Override // com.huawei.nearbysdk.DTCP.fileinfo.BaseShareInfo, android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mInfoType);
        super.writeToParcel(dest, flags);
    }

    @Override // com.huawei.nearbysdk.DTCP.fileinfo.BaseShareInfo, com.huawei.nearbysdk.DTCP.fileinfo.IDTCPSerialize
    public void writeToDTCPStream(DataOutputStream dos, int dtcpVersion) throws IOException {
        super.writeToDTCPStream(dos, dtcpVersion);
    }

    @Override // com.huawei.nearbysdk.DTCP.fileinfo.BaseShareInfo, com.huawei.nearbysdk.DTCP.fileinfo.IDTCPSerialize
    public void readFromDTCPStream(DataInputStream dis, int dtcpVersion) throws IOException {
        super.readFromDTCPStream(dis, dtcpVersion);
    }
}

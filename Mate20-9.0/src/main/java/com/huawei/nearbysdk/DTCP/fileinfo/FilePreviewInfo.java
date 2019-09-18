package com.huawei.nearbysdk.DTCP.fileinfo;

import android.os.Parcel;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FilePreviewInfo extends BaseFilePreviewInfo {
    public FilePreviewInfo() {
        this.mInfoType = 2;
    }

    public FilePreviewInfo(String sender, String type, String fileName, long fileSize, int fileNum, String content, String source) {
        super(sender, type, fileName, fileSize, fileNum, content, source);
        this.mInfoType = 2;
    }

    protected FilePreviewInfo(Parcel in) {
        super(in);
        this.mInfoType = 2;
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

package com.huawei.nearbysdk.DTCP.fileinfo;

import android.os.Parcel;
import com.huawei.nearbysdk.HwLog;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FilePreviewInfo extends BaseFilePreviewInfo {
    private static final String TAG = "FilePreviewInfo";

    public FilePreviewInfo() {
        this.mInfoType = 2;
    }

    public FilePreviewInfo(int dtcpVersion) {
        this.mInfoType = 2;
        this.mPeerDtcpVersion = dtcpVersion;
    }

    public FilePreviewInfo(String sender, String type, String fileName, long fileSize, int fileNum, String content, String source) {
        super(sender, type, fileName, fileSize, fileNum, content, source);
        this.mInfoType = 2;
        HwLog.d(TAG, "dtcpVersion is default, infoType is " + this.mInfoType);
    }

    public FilePreviewInfo(int localdtcpVersion, String sender, String type, String fileName, long fileSize, int fileNum, String content, String source) {
        super(sender, type, fileName, fileSize, fileNum, content, source);
        this.mInfoType = 2;
        this.mPeerDtcpVersion = localdtcpVersion;
        HwLog.d(TAG, "dtcpVersion is " + this.mPeerDtcpVersion);
    }

    protected FilePreviewInfo(Parcel in) {
        super(in);
        this.mInfoType = 2;
        this.mPeerDtcpVersion = in.readInt();
    }

    @Override // com.huawei.nearbysdk.DTCP.fileinfo.BaseFilePreviewInfo, com.huawei.nearbysdk.DTCP.fileinfo.BasePreviewInfo, android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mInfoType);
        super.writeToParcel(dest, flags);
        dest.writeInt(this.mPeerDtcpVersion);
        HwLog.d(TAG, "writeToParcel :localDtcpVersion is" + this.mPeerDtcpVersion);
    }

    @Override // com.huawei.nearbysdk.DTCP.fileinfo.BaseFilePreviewInfo, com.huawei.nearbysdk.DTCP.fileinfo.BasePreviewInfo, com.huawei.nearbysdk.DTCP.fileinfo.IDTCPSerialize
    public void writeToDTCPStream(DataOutputStream dos, int dtcpVersion) throws IOException {
        super.writeToDTCPStream(dos, dtcpVersion);
        this.mPeerDtcpVersion = dtcpVersion;
        HwLog.d(TAG, "writeToDTCPStream :localDtcpVersion is " + this.mPeerDtcpVersion);
    }

    @Override // com.huawei.nearbysdk.DTCP.fileinfo.BaseFilePreviewInfo, com.huawei.nearbysdk.DTCP.fileinfo.BasePreviewInfo, com.huawei.nearbysdk.DTCP.fileinfo.IDTCPSerialize
    public void readFromDTCPStream(DataInputStream dis, int dtcpVersion) throws IOException {
        super.readFromDTCPStream(dis, dtcpVersion);
        this.mPeerDtcpVersion = dtcpVersion;
        HwLog.d(TAG, "readFromDTCPStream :dtcpVersion is " + dtcpVersion);
    }

    @Override // com.huawei.nearbysdk.DTCP.fileinfo.BaseFilePreviewInfo
    public int getPeerDtcpVersion() {
        return this.mPeerDtcpVersion;
    }
}

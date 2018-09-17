package com.huawei.nearbysdk.DTCP.fileinfo;

import android.os.Parcel;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BaseFilePreviewInfo extends BasePreviewInfo {
    private String mFileName;
    private int mFileNum;
    private long mFileSize;

    protected BaseFilePreviewInfo() {
    }

    protected BaseFilePreviewInfo(String sender, String type, String fileName, long fileSize, int fileNum, String content, String source) {
        super(sender, type, content, source);
        this.mFileName = fileName;
        this.mFileSize = fileSize;
        this.mFileNum = fileNum;
    }

    public String getFileName() {
        return this.mFileName;
    }

    public void setFileName(String fileName) {
        this.mFileName = fileName;
    }

    public long getFileSize() {
        return this.mFileSize;
    }

    public void setFileSize(long fileSize) {
        this.mFileSize = fileSize;
    }

    public int getFileNum() {
        return this.mFileNum;
    }

    public void setFileNum(int mFileNum) {
        this.mFileNum = mFileNum;
    }

    public String toString() {
        return super.toString() + ", mFileName=" + this.mFileName + ", mFileSize=" + this.mFileSize;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mFileName);
        dest.writeLong(this.mFileSize);
        dest.writeInt(this.mFileNum);
    }

    protected BaseFilePreviewInfo(Parcel in) {
        super(in);
        this.mFileName = in.readString();
        this.mFileSize = in.readLong();
        this.mFileNum = in.readInt();
    }

    public void writeToDTCPStream(DataOutputStream dos, int dtcpVersion) throws IOException {
        super.writeToDTCPStream(dos, dtcpVersion);
        writeStrToDTCPStream(this.mFileName, dos, dtcpVersion);
        dos.writeLong(this.mFileSize);
        dos.writeInt(this.mFileNum);
    }

    public void readFromDTCPStream(DataInputStream dis, int dtcpVersion) throws IOException {
        super.readFromDTCPStream(dis, dtcpVersion);
        this.mFileName = dis.readUTF();
        this.mFileSize = dis.readLong();
        this.mFileNum = dis.readInt();
    }
}

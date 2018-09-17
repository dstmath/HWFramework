package com.huawei.nearbysdk.DTCP.fileinfo;

import android.os.Parcel;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

public class MediaPreviewInfo extends BaseFilePreviewInfo {
    private byte[] mThumbnail;

    public MediaPreviewInfo() {
        this.mInfoType = 1;
    }

    public MediaPreviewInfo(String sender, String type, String fileName, long fileSize, int fileNum, byte[] thumbnail, String content, String source) {
        super(sender, type, fileName, fileSize, fileNum, content, source);
        this.mThumbnail = thumbnail;
        this.mInfoType = 1;
    }

    public byte[] getThumbnail() {
        return this.mThumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.mThumbnail = thumbnail;
    }

    public String toString() {
        String describe;
        if (this.mThumbnail == null) {
            describe = ", mThumbnail is null";
        } else {
            describe = ", mThumbnail.length=" + this.mThumbnail.length;
        }
        return super.toString() + describe;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mInfoType);
        super.writeToParcel(dest, flags);
        int count = 0;
        if (this.mThumbnail != null) {
            count = this.mThumbnail.length;
        }
        dest.writeInt(count);
        if (count > 0) {
            dest.writeByteArray(this.mThumbnail);
        }
    }

    protected MediaPreviewInfo(Parcel in) {
        super(in);
        int count = in.readInt();
        if (count > 0) {
            this.mThumbnail = new byte[count];
            in.readByteArray(this.mThumbnail);
        }
        this.mInfoType = 1;
    }

    public void writeToDTCPStream(DataOutputStream dos, int dtcpVersion) throws IOException {
        super.writeToDTCPStream(dos, dtcpVersion);
        dos.writeInt(this.mThumbnail.length);
        if (this.mThumbnail.length > 0) {
            dos.write(this.mThumbnail);
        }
    }

    public void readFromDTCPStream(DataInputStream dis, int dtcpVersion) throws IOException {
        super.readFromDTCPStream(dis, dtcpVersion);
        int size = dis.readInt();
        if (size > 0) {
            this.mThumbnail = new byte[size];
            int idx = 0;
            do {
                int readSize = dis.read(this.mThumbnail, idx, size - idx);
                if (-1 == readSize) {
                    throw new EOFException("Thumbnail data size error!");
                }
                idx += readSize;
            } while (idx < size);
            return;
        }
        this.mThumbnail = null;
    }
}

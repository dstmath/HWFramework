package com.huawei.nearbysdk.DTCP.fileinfo;

import android.os.Parcel;
import com.huawei.nearbysdk.HwLog;
import com.huawei.nearbysdk.util.TlvUtils;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

public class MediaPreviewInfo extends BaseFilePreviewInfo {
    public static final byte APK_NUM = 3;
    private static final int BITMAP_MAX_SIZE = 900000;
    private static final int BITMAP_MIN_SIZE = 0;
    public static final byte IMAGE_NUM = 1;
    public static final int INT_LENGTH = 4;
    private static final int LENGTH_BYTE = 1;
    private static final int LENGTH_INT = 4;
    private static final String TAG = "MediaPreviewInfo";
    public static final int TLV_LENGTH = 27;
    public static final byte VIDEO_NUM = 2;
    private static final long serialVersionUID = 6908982607074009484L;
    private int mApkNum = 0;
    private int mImageNum = 0;
    private int mLocalDtcpVersion = 600;
    private byte[] mThumbnail;
    private int mVideoNum = 0;

    public MediaPreviewInfo() {
        this.mInfoType = 1;
    }

    public MediaPreviewInfo(int peerDtcpVersion) {
        this.mPeerDtcpVersion = peerDtcpVersion;
        this.mInfoType = 1;
    }

    public MediaPreviewInfo(String sender, String type, String fileName, long fileSize, int fileNum, byte[] thumbnail, String content, String source) {
        super(sender, type, fileName, fileSize, fileNum, content, source);
        this.mThumbnail = thumbnail;
        this.mInfoType = 1;
        HwLog.d(TAG, "dtcpVersion is default, infoType is " + this.mInfoType);
    }

    public MediaPreviewInfo(int peerDftpVersion, String sender, String type, String fileName, long fileSize, int fileNum, byte[] thumbnail, int imageNum, int videoNum, int apkNum, String content, String source) {
        super(sender, type, fileName, fileSize, fileNum, content, source);
        this.mThumbnail = thumbnail;
        this.mInfoType = 1;
        if (this.mLocalDtcpVersion >= 400 && peerDftpVersion >= 400) {
            this.mPeerDtcpVersion = peerDftpVersion;
            this.mImageNum = imageNum;
            this.mVideoNum = videoNum;
            this.mApkNum = apkNum;
        }
        HwLog.d(TAG, "dtcpVersion is " + this.mPeerDtcpVersion + ", imageNum is " + this.mImageNum + ", videoNum is " + this.mVideoNum + ", apkNum is " + this.mApkNum);
    }

    public byte[] getThumbnail() {
        return this.mThumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.mThumbnail = thumbnail;
    }

    @Override // com.huawei.nearbysdk.DTCP.fileinfo.BaseFilePreviewInfo, com.huawei.nearbysdk.DTCP.fileinfo.BasePreviewInfo, java.lang.Object
    public String toString() {
        return super.toString() + (this.mThumbnail == null ? ", mThumbnail is null" : ", mThumbnail.length=" + this.mThumbnail.length);
    }

    @Override // com.huawei.nearbysdk.DTCP.fileinfo.BaseFilePreviewInfo, com.huawei.nearbysdk.DTCP.fileinfo.BasePreviewInfo, android.os.Parcelable
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
        dest.writeInt(this.mPeerDtcpVersion);
        dest.writeInt(this.mImageNum);
        dest.writeInt(this.mVideoNum);
        dest.writeInt(this.mApkNum);
        HwLog.d(TAG, "writeToParcel :dtcpVersion" + this.mPeerDtcpVersion + ", imageNum is " + this.mImageNum + ", videoNum is " + this.mVideoNum + ", apkNum is " + this.mApkNum);
    }

    protected MediaPreviewInfo(Parcel in) {
        super(in);
        int count = in.readInt();
        if (count > 0 && count <= BITMAP_MAX_SIZE) {
            this.mThumbnail = new byte[count];
            in.readByteArray(this.mThumbnail);
        }
        this.mPeerDtcpVersion = in.readInt();
        this.mImageNum = in.readInt();
        this.mVideoNum = in.readInt();
        this.mApkNum = in.readInt();
        this.mInfoType = 1;
        HwLog.d(TAG, "MediaPreviewInfo :dtcpVersion" + this.mPeerDtcpVersion + ", imageNum is " + this.mImageNum + ", videoNum is " + this.mVideoNum + ", apkNum is " + this.mApkNum);
    }

    @Override // com.huawei.nearbysdk.DTCP.fileinfo.BaseFilePreviewInfo, com.huawei.nearbysdk.DTCP.fileinfo.BasePreviewInfo, com.huawei.nearbysdk.DTCP.fileinfo.IDTCPSerialize
    public void writeToDTCPStream(DataOutputStream dos, int dtcpVersion) throws IOException {
        this.mPeerDtcpVersion = dtcpVersion;
        super.writeToDTCPStream(dos, dtcpVersion);
        dos.writeInt(this.mThumbnail.length);
        if (this.mThumbnail.length > 0) {
            dos.write(this.mThumbnail);
        }
        HwLog.d(TAG, "writeToDTCPStream: dtcpVersion" + this.mPeerDtcpVersion + ", imageNum is " + this.mImageNum + ", videoNum is " + this.mVideoNum + ", apkNum is " + this.mApkNum);
        if (this.mLocalDtcpVersion >= 400 && this.mPeerDtcpVersion >= 400) {
            dos.writeInt(27);
            TlvUtils.writeTlv(dos, (byte) 1, 4, TlvUtils.int2Bytes(this.mImageNum, 4));
            TlvUtils.writeTlv(dos, (byte) 2, 4, TlvUtils.int2Bytes(this.mVideoNum, 4));
            TlvUtils.writeTlv(dos, (byte) 3, 4, TlvUtils.int2Bytes(this.mApkNum, 4));
        }
    }

    @Override // com.huawei.nearbysdk.DTCP.fileinfo.BaseFilePreviewInfo, com.huawei.nearbysdk.DTCP.fileinfo.BasePreviewInfo, com.huawei.nearbysdk.DTCP.fileinfo.IDTCPSerialize
    public void readFromDTCPStream(DataInputStream dis, int dtcpVersion) throws IOException {
        this.mPeerDtcpVersion = dtcpVersion;
        HwLog.d(TAG, "dtcpVersion is " + this.mPeerDtcpVersion);
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
        } else {
            this.mThumbnail = null;
        }
        if (this.mLocalDtcpVersion >= 400 && this.mPeerDtcpVersion >= 400) {
            int totalSize = dis.readInt();
            HwLog.d(TAG, "totalSize is " + totalSize);
            readTlv(totalSize, dis);
        }
        HwLog.d(TAG, "readFromDTCPStream: dtcpVersion" + this.mPeerDtcpVersion + ", imageNum is " + this.mImageNum + ", videoNum is " + this.mVideoNum + ", apkNum is " + this.mApkNum);
    }

    public int getImageNum() {
        return this.mImageNum;
    }

    public int getVideoNum() {
        return this.mVideoNum;
    }

    public int getApkNum() {
        return this.mApkNum;
    }

    @Override // com.huawei.nearbysdk.DTCP.fileinfo.BaseFilePreviewInfo
    public int getPeerDtcpVersion() {
        return this.mPeerDtcpVersion;
    }

    private void readTlv(int size, DataInputStream dis) {
        int length;
        int totalSize = size;
        while (totalSize > 0) {
            byte tag = TlvUtils.getTag(dis);
            if (tag != -1 && (length = TlvUtils.getLen(dis)) != -1) {
                switch (tag) {
                    case 1:
                        this.mImageNum = TlvUtils.getIntValue(dis);
                        if (this.mImageNum != -1) {
                            totalSize = ((totalSize - 1) - 4) - length;
                            break;
                        } else {
                            this.mImageNum = 0;
                            totalSize = 0;
                            break;
                        }
                    case 2:
                        this.mVideoNum = TlvUtils.getIntValue(dis);
                        if (this.mVideoNum != -1) {
                            totalSize = ((totalSize - 1) - 4) - length;
                            break;
                        } else {
                            this.mVideoNum = 0;
                            totalSize = 0;
                            break;
                        }
                    case 3:
                        this.mApkNum = TlvUtils.getIntValue(dis);
                        if (this.mApkNum != -1) {
                            totalSize = ((totalSize - 1) - 4) - length;
                            break;
                        } else {
                            this.mApkNum = 0;
                            totalSize = 0;
                            break;
                        }
                    default:
                        totalSize = 0;
                        break;
                }
            } else {
                return;
            }
        }
    }
}

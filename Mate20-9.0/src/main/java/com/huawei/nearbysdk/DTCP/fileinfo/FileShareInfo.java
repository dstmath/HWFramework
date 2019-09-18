package com.huawei.nearbysdk.DTCP.fileinfo;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.media.session.PlaybackStateCompat;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileShareInfo extends BaseShareInfo {
    private DFTPFileInfo mDftpFileInfo;
    private List<FileShareInfoItem> mFileItemList;

    public static class FileShareInfoItem implements Parcelable, Serializable, IDTCPSerialize {
        public static final Parcelable.Creator<FileShareInfoItem> CREATOR = new FileShareInfoItemCreator();
        private static final long serialVersionUID = -3049281932299494193L;
        /* access modifiers changed from: private */
        public String fileName;
        /* access modifiers changed from: private */
        public long fileSize;
        /* access modifiers changed from: private */
        public String mimeType;
        /* access modifiers changed from: private */
        public String path;
        /* access modifiers changed from: private */
        public String uri;

        static class FileShareInfoItemCreator implements Parcelable.Creator<FileShareInfoItem> {
            FileShareInfoItemCreator() {
            }

            public FileShareInfoItem createFromParcel(Parcel source) {
                FileShareInfoItem mFileShareInfoItem = new FileShareInfoItem();
                String unused = mFileShareInfoItem.path = source.readString();
                String unused2 = mFileShareInfoItem.uri = source.readString();
                String unused3 = mFileShareInfoItem.fileName = source.readString();
                long unused4 = mFileShareInfoItem.fileSize = source.readLong();
                String unused5 = mFileShareInfoItem.mimeType = source.readString();
                return mFileShareInfoItem;
            }

            public FileShareInfoItem[] newArray(int size) {
                return new FileShareInfoItem[size];
            }
        }

        public String getFilePath() {
            return this.path;
        }

        public Uri getUri() {
            if (this.uri == null) {
                return null;
            }
            return Uri.parse(this.uri);
        }

        public void setUri(Uri uri2) {
            if (uri2 == null) {
                this.uri = null;
            } else {
                this.uri = uri2.toString();
            }
        }

        public String getFileName() {
            return this.fileName;
        }

        public void setFileName(String fileName2) {
            this.fileName = fileName2;
            this.path = fileName2;
        }

        public long getFileSize() {
            return this.fileSize;
        }

        public void setFileSize(long fileSize2) {
            this.fileSize = fileSize2;
        }

        public String getMimeType() {
            return this.mimeType;
        }

        public void setMimeType(String mimeType2) {
            this.mimeType = mimeType2;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.path);
            dest.writeString(this.uri);
            dest.writeString(this.fileName);
            dest.writeLong(this.fileSize);
            dest.writeString(this.mimeType);
        }

        public String toString() {
            return String.valueOf(this.uri) + " " + this.fileName + " " + this.fileSize + " " + this.mimeType;
        }

        public void writeToDTCPStream(DataOutputStream dos, int dtcpVersion) throws IOException {
            writeStrToDTCPStream(this.path, dos, dtcpVersion);
            writeStrToDTCPStream(this.uri, dos, dtcpVersion);
            writeStrToDTCPStream(this.fileName, dos, dtcpVersion);
            dos.writeLong(this.fileSize);
            writeStrToDTCPStream(this.mimeType, dos, dtcpVersion);
        }

        /* access modifiers changed from: protected */
        public void writeStrToDTCPStream(String str, DataOutputStream dos, int dtcpVersion) throws IOException {
            if (str != null) {
                dos.writeUTF(str);
            } else {
                dos.writeUTF("");
            }
        }

        public void readFromDTCPStream(DataInputStream dis, int dtcpVersion) throws IOException {
            this.path = dis.readUTF();
            this.uri = dis.readUTF();
            this.fileName = dis.readUTF();
            this.fileSize = dis.readLong();
            this.mimeType = dis.readUTF();
        }
    }

    public DFTPFileInfo getDftpFileInfo() {
        return this.mDftpFileInfo;
    }

    public void setDftpFileInfo(DFTPFileInfo dftpFileInfo) {
        this.mDftpFileInfo = dftpFileInfo == null ? new DFTPFileInfo() : dftpFileInfo;
    }

    public FileShareInfo() {
        this.mInfoType = 6;
    }

    public FileShareInfo(List<FileShareInfoItem> mList, BasePreviewInfo previewInfo) {
        super(previewInfo);
        this.mFileItemList = mList;
        this.mInfoType = 6;
    }

    public List<FileShareInfoItem> getFileItemList() {
        return this.mFileItemList;
    }

    public void setFileItemList(List<FileShareInfoItem> mFileItemList2) {
        this.mFileItemList = mFileItemList2;
    }

    public int getFileCount() {
        return this.mFileItemList.size();
    }

    public long getFileSize() {
        BasePreviewInfo previewInfo = getPreviewInfo();
        if (previewInfo == null || !(previewInfo instanceof BaseFilePreviewInfo)) {
            return 0;
        }
        return ((BaseFilePreviewInfo) previewInfo).getFileSize();
    }

    public String toString() {
        return "{FileShareInfo:FileCount=" + getFileCount() + " Size=" + (getFileSize() / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) + "KB}";
    }

    protected FileShareInfo(Parcel in) {
        super(in);
        this.mFileItemList = in.createTypedArrayList(FileShareInfoItem.CREATOR);
        this.mInfoType = 6;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mInfoType);
        super.writeToParcel(dest, flags);
        dest.writeTypedList(this.mFileItemList);
    }

    public void writeToDTCPStream(DataOutputStream dos, int dtcpVersion) throws IOException {
        super.writeToDTCPStream(dos, dtcpVersion);
        int fileNum = this.mFileItemList.size();
        dos.writeInt(fileNum);
        for (int idx = 0; idx < fileNum; idx++) {
            this.mFileItemList.get(idx).writeToDTCPStream(dos, dtcpVersion);
        }
    }

    public void readFromDTCPStream(DataInputStream dis, int dtcpVersion) throws IOException {
        super.readFromDTCPStream(dis, dtcpVersion);
        int fileNum = dis.readInt();
        if (fileNum > 0) {
            this.mFileItemList = new ArrayList(fileNum);
            for (int idx = 0; idx < fileNum; idx++) {
                FileShareInfoItem fileItem = new FileShareInfoItem();
                fileItem.readFromDTCPStream(dis, dtcpVersion);
                this.mFileItemList.add(fileItem);
            }
        } else if (this.mFileItemList != null) {
            this.mFileItemList.clear();
        }
    }
}

package com.huawei.nearbysdk.DTCP.fileinfo;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v4.media.session.PlaybackStateCompat;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileShareInfo extends BaseShareInfo {
    private List<FileShareInfoItem> mFileItemList;

    public static class FileShareInfoItem implements Parcelable, Serializable, IDTCPSerialize {
        public static final Creator<FileShareInfoItem> CREATOR = new FileShareInfoItemCreator();
        private static final long serialVersionUID = -3049281932299494193L;
        private String fileName;
        private long fileSize;
        private String mimeType;
        private String path;
        private String uri;

        static class FileShareInfoItemCreator implements Creator<FileShareInfoItem> {
            FileShareInfoItemCreator() {
            }

            public FileShareInfoItem createFromParcel(Parcel source) {
                FileShareInfoItem mFileShareInfoItem = new FileShareInfoItem();
                mFileShareInfoItem.path = source.readString();
                mFileShareInfoItem.uri = source.readString();
                mFileShareInfoItem.fileName = source.readString();
                mFileShareInfoItem.fileSize = source.readLong();
                mFileShareInfoItem.mimeType = source.readString();
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

        public void setUri(Uri uri) {
            if (uri == null) {
                this.uri = null;
            } else {
                this.uri = uri.toString();
            }
        }

        public String getFileName() {
            return this.fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
            this.path = fileName;
        }

        public long getFileSize() {
            return this.fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public String getMimeType() {
            return this.mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
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

        protected void writeStrToDTCPStream(String str, DataOutputStream dos, int dtcpVersion) throws IOException {
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

    public void setFileItemList(List<FileShareInfoItem> mFileItemList) {
        this.mFileItemList = mFileItemList;
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
            ((FileShareInfoItem) this.mFileItemList.get(idx)).writeToDTCPStream(dos, dtcpVersion);
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

package com.huawei.nearbysdk.DTCP.fileinfo;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.media.session.PlaybackStateCompat;
import com.huawei.nearbysdk.HwLog;
import com.huawei.nearbysdk.util.Util;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class FileShareInfo extends BaseShareInfo {
    private static final String TAG = "FileShareInfo";
    private DFTPFileInfo mDftpFileInfo;
    private List<FileShareInfoItem> mFileItemList;

    public DFTPFileInfo getDftpFileInfo() {
        return this.mDftpFileInfo;
    }

    public void setDftpFileInfo(DFTPFileInfo dftpFileInfo) {
        this.mDftpFileInfo = dftpFileInfo == null ? new DFTPFileInfo() : dftpFileInfo;
    }

    public FileShareInfo() {
        this.mInfoType = 6;
    }

    @Override // com.huawei.nearbysdk.DTCP.fileinfo.BaseShareInfo
    public void setInfoType(int shareInfoType) {
        super.setInfoType(shareInfoType);
    }

    public FileShareInfo(List<FileShareInfoItem> list) {
        this.mFileItemList = list;
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

    public void setFileItemList(List<FileShareInfoItem> fileItemList) {
        this.mFileItemList = fileItemList;
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

    @Override // com.huawei.nearbysdk.DTCP.fileinfo.BaseShareInfo, java.lang.Object
    public String toString() {
        return "{FileShareInfo:FileCount=" + getFileCount() + " Size=" + (getFileSize() / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) + "KB}";
    }

    protected FileShareInfo(Parcel in) {
        super(in);
        this.mInfoType = 6;
        try {
            Object boolObj = Parcel.class.getMethod("readBoolean", new Class[0]).invoke(in, new Object[0]);
            if (boolObj instanceof Boolean) {
                boolean flag = ((Boolean) boolObj).booleanValue();
                this.mInfoType = 6;
                if (flag) {
                    this.mFileItemList = in.createTypedArrayList(FileShareInfoItem.CREATOR);
                } else {
                    Method methodBlob = Parcel.class.getMethod("readBlob", new Class[0]);
                    Parcel data = Parcel.obtain();
                    Object bytesObj = methodBlob.invoke(in, new Object[0]);
                    if (bytesObj instanceof byte[]) {
                        byte[] bytes = (byte[]) bytesObj;
                        HwLog.d(TAG, "CreateFromParcel readBlob size :" + bytes.length);
                        data.unmarshall(bytes, 0, bytes.length);
                        data.setDataPosition(0);
                        this.mFileItemList = data.createTypedArrayList(FileShareInfoItem.CREATOR);
                        data.recycle();
                    } else {
                        HwLog.e(TAG, "ReadBlob type failed.");
                        data.recycle();
                        return;
                    }
                }
                return;
            }
            HwLog.e(TAG, "ReadBoolean type failed.");
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            HwLog.e(TAG, "Invoke method failed.");
        }
    }

    @Override // com.huawei.nearbysdk.DTCP.fileinfo.BaseShareInfo, android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mInfoType);
        super.writeToParcel(dest, flags);
        HwLog.d(TAG, "WriteToParcel file list size:" + this.mFileItemList.size());
        if (this.mFileItemList.size() <= 500) {
            try {
                Parcel.class.getMethod("writeBoolean", Boolean.TYPE).invoke(dest, true);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                HwLog.e(TAG, "Invoke writeBoolean failed.");
            }
            dest.writeTypedList(this.mFileItemList);
            return;
        }
        Parcel data = Parcel.obtain();
        data.writeTypedList(this.mFileItemList);
        byte[] bytes = data.marshall();
        data.recycle();
        try {
            Method methodBool = Parcel.class.getMethod("writeBoolean", Boolean.TYPE);
            Method methodBlob = Parcel.class.getMethod("writeBlob", byte[].class);
            if (bytes == null) {
                methodBool.invoke(dest, true);
                dest.writeTypedList(this.mFileItemList);
                return;
            }
            HwLog.d(TAG, "WriteToParcel writeBlob size :" + this.mFileItemList.size());
            methodBool.invoke(dest, false);
            methodBlob.invoke(dest, bytes);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e2) {
            HwLog.e(TAG, "WriteToParcel failed.");
        }
    }

    @Override // com.huawei.nearbysdk.DTCP.fileinfo.BaseShareInfo, com.huawei.nearbysdk.DTCP.fileinfo.IDTCPSerialize
    public void writeToDTCPStream(DataOutputStream dos, int dtcpVersion) throws IOException {
        super.writeToDTCPStream(dos, dtcpVersion);
        int fileNum = this.mFileItemList.size();
        dos.writeInt(fileNum);
        for (int idx = 0; idx < fileNum; idx++) {
            this.mFileItemList.get(idx).writeToDTCPStream(dos, dtcpVersion);
        }
    }

    @Override // com.huawei.nearbysdk.DTCP.fileinfo.BaseShareInfo, com.huawei.nearbysdk.DTCP.fileinfo.IDTCPSerialize
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

    public static class FileShareInfoItem implements Parcelable, Serializable, IDTCPSerialize {
        public static final Parcelable.Creator<FileShareInfoItem> CREATOR = new FileShareInfoItemCreator();
        private static final long serialVersionUID = -3049281932299494193L;
        private String fileName;
        private long fileSize;
        private String mimeType;
        private int orientation;
        private String path;
        private String uri;

        public String getFilePath() {
            return this.path;
        }

        public void setFilePath(String filePath) {
            this.path = filePath;
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
            String realFileName = Util.getFileNameByPath(fileName2);
            this.fileName = realFileName;
            this.path = realFileName;
        }

        public void setFileNameStr(String fileName2) {
            this.fileName = fileName2;
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

        public int getOrientation() {
            return this.orientation;
        }

        public void setOrientation(int orientation2) {
            this.orientation = orientation2;
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.path);
            dest.writeString(this.uri);
            dest.writeString(this.fileName);
            dest.writeLong(this.fileSize);
            dest.writeString(this.mimeType);
        }

        static class FileShareInfoItemCreator implements Parcelable.Creator<FileShareInfoItem> {
            FileShareInfoItemCreator() {
            }

            @Override // android.os.Parcelable.Creator
            public FileShareInfoItem createFromParcel(Parcel source) {
                FileShareInfoItem mFileShareInfoItem = new FileShareInfoItem();
                mFileShareInfoItem.path = source.readString();
                mFileShareInfoItem.uri = source.readString();
                mFileShareInfoItem.fileName = source.readString();
                mFileShareInfoItem.fileSize = source.readLong();
                mFileShareInfoItem.mimeType = source.readString();
                return mFileShareInfoItem;
            }

            @Override // android.os.Parcelable.Creator
            public FileShareInfoItem[] newArray(int size) {
                return new FileShareInfoItem[size];
            }
        }

        @Override // java.lang.Object
        public String toString() {
            return String.valueOf(this.uri) + " " + this.fileName + " " + this.fileSize + " " + this.mimeType;
        }

        @Override // com.huawei.nearbysdk.DTCP.fileinfo.IDTCPSerialize
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

        @Override // com.huawei.nearbysdk.DTCP.fileinfo.IDTCPSerialize
        public void readFromDTCPStream(DataInputStream dis, int dtcpVersion) throws IOException {
            this.path = dis.readUTF();
            this.uri = dis.readUTF();
            this.fileName = dis.readUTF();
            this.fileSize = dis.readLong();
            this.mimeType = dis.readUTF();
        }
    }
}

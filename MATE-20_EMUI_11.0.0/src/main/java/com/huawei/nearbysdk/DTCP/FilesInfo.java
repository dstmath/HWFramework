package com.huawei.nearbysdk.DTCP;

import com.huawei.nearbysdk.DTCP.fileinfo.BasePreviewInfo;
import com.huawei.nearbysdk.DTCP.fileinfo.FileShareInfo;
import com.huawei.nearbysdk.DTCP.fileinfo.MediaPreviewInfo;
import java.util.List;

public class FilesInfo {
    private static String TAG = "FilesInfo";
    private List<FileShareInfo.FileShareInfoItem> mFileList = null;
    private FileShareInfo mFileShareInfo = null;

    public final class FileItem {
        private FileShareInfo.FileShareInfoItem mInnerFileItem;

        FileItem(FileShareInfo.FileShareInfoItem innerFileItem) {
            this.mInnerFileItem = innerFileItem;
        }

        public String getFileName() {
            return this.mInnerFileItem.getFileName();
        }

        public String getFilePath() {
            return this.mInnerFileItem.getFilePath();
        }

        public String getMimeType() {
            return this.mInnerFileItem.getMimeType();
        }

        public long getFileSize() {
            return this.mInnerFileItem.getFileSize();
        }
    }

    FilesInfo(FileShareInfo innerShareinfo) {
        this.mFileShareInfo = innerShareinfo;
        this.mFileList = this.mFileShareInfo.getFileItemList();
    }

    public long getTotalFileSize() {
        return this.mFileShareInfo.getFileSize();
    }

    public int getFileCount() {
        return this.mFileList.size();
    }

    public FileItem getFileItem(int idx) {
        if (idx >= getFileCount()) {
            return null;
        }
        return new FileItem(this.mFileList.get(idx));
    }

    public FileItem[] getAllFiles() {
        int fileCount = getFileCount();
        if (fileCount == 0) {
            return null;
        }
        FileItem[] fileItems = new FileItem[fileCount];
        for (int idx = 0; idx < fileCount; idx++) {
            fileItems[idx] = new FileItem(this.mFileList.get(idx));
        }
        return fileItems;
    }

    public byte[] getThumbnail() {
        byte[] thumbnail;
        BasePreviewInfo basePreviewInfo = this.mFileShareInfo.getPreviewInfo();
        if (basePreviewInfo == null || !(basePreviewInfo instanceof MediaPreviewInfo) || (thumbnail = ((MediaPreviewInfo) basePreviewInfo).getThumbnail()) == null) {
            return null;
        }
        return (byte[]) thumbnail.clone();
    }
}

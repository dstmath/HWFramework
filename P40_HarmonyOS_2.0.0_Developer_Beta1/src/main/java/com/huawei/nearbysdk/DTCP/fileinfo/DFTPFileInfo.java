package com.huawei.nearbysdk.DTCP.fileinfo;

import com.huawei.nearbysdk.DTCP.fileinfo.FileShareInfo;
import com.huawei.nearbysdk.HwLog;
import java.util.ArrayList;
import java.util.List;

public class DFTPFileInfo {
    public static final String TAG = "DFTPFileInfo";
    private BigFileInfo mBigFileInfo;
    private List<TarFileInfo> mTarFileList;

    public DFTPFileInfo() {
        this.mTarFileList = new ArrayList();
        this.mBigFileInfo = new BigFileInfo();
    }

    public DFTPFileInfo(List<TarFileInfo> tarFileInfoList, BigFileInfo bigFileInfo) {
        if (tarFileInfoList == null) {
            this.mTarFileList = new ArrayList();
        } else {
            this.mTarFileList = tarFileInfoList;
            HwLog.d(TAG, "TarFileList.size is " + this.mTarFileList.size());
        }
        if (bigFileInfo == null) {
            this.mBigFileInfo = new BigFileInfo();
            return;
        }
        this.mBigFileInfo = bigFileInfo;
        HwLog.d(TAG, "BigFileList.size is " + this.mBigFileInfo.getBigFilePathList().size());
    }

    public List<TarFileInfo> getTarFileList() {
        return this.mTarFileList;
    }

    public BigFileInfo getBigFileList() {
        return this.mBigFileInfo;
    }

    public static class TarFileInfo {
        private List<FileShareInfo.FileShareInfoItem> smallFileList = new ArrayList();
        private List<String> tarFilePathList = new ArrayList();

        public TarFileInfo() {
        }

        public TarFileInfo(TarFileInfo tarFileInfo) {
            this.smallFileList = tarFileInfo.getSmallFileList();
            this.tarFilePathList = tarFileInfo.getTarFilePathList();
        }

        public List<String> getTarFilePathList() {
            return this.tarFilePathList;
        }

        public List<FileShareInfo.FileShareInfoItem> getSmallFileList() {
            return this.smallFileList;
        }

        public void updateTarFileInfo(FileShareInfo.FileShareInfoItem smallFile, String filePath) {
            if (smallFile != null && filePath != null) {
                this.smallFileList.add(smallFile);
                this.tarFilePathList.add(filePath);
            }
        }
    }

    public static class BigFileInfo {
        private List<FileShareInfo.FileShareInfoItem> bigFileInfoList = new ArrayList();
        private List<String> bigFilePathList = new ArrayList();

        public List<String> getBigFilePathList() {
            return this.bigFilePathList;
        }

        public List<FileShareInfo.FileShareInfoItem> getBigFileInfoList() {
            return this.bigFileInfoList;
        }

        public void updateBigFileInfo(FileShareInfo.FileShareInfoItem bigFileInfo, String filePath) {
            if (bigFileInfo != null && filePath != null) {
                this.bigFileInfoList.add(bigFileInfo);
                this.bigFilePathList.add(filePath);
            }
        }
    }
}

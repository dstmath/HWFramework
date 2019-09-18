package com.msic.qarth;

import android.content.Context;
import android.system.ErrnoException;
import android.system.Os;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecordProcessUtil {
    private final String FILE_LOG_SUFFIX = ".log";
    private final String FILE_NAME_SEPARATOR = "_";
    private final String FILE_SUC_SUFFIX = ".success";
    public final int NO_ERROR = 0;
    public final int QARTH_FIND_CLASS_ERR = 3;
    public final int QARTH_FIND_METHOD_ERR = 4;
    public final int QARTH_HOOK_UNAUTHORIZED = 1;
    public final int QARTH_ORIGINAL_METHOD = 2;
    private final String RECORD_DIR = "/data/hotpatch/fwkpatchdir";
    private final String TAG = RecordProcessUtil.class.getSimpleName();
    private Context mContext;
    private String mPackageName;
    private String mPatchFileName;
    private String mRecordFileDir;
    private String mRecordFileName;
    private int mRecordFileNum = 0;

    private enum RecordStatusEnum {
        PATCH_STATUS_DOWNLOADED,
        PATCH_STATUS_LOADING,
        PATCH_STATUS_FAILED,
        PATCH_STATUS_SUCCESS
    }

    RecordProcessUtil(QarthContext qc) {
        this.mContext = qc.context;
        this.mPackageName = qc.packageName;
        this.mPatchFileName = qc.patchFile.getFile().getName();
        this.mRecordFileDir = getRecordFileDir();
    }

    private String getRecordFileDir() {
        String dir = "/data/hotpatch/fwkpatchdir" + File.separator + this.mPackageName;
        if ("COMMON_HOOK".equals(this.mPackageName)) {
            return "/data/hotpatch/fwkpatchdir/system/all";
        }
        if (this.mPackageName.equals("systemserver")) {
            return "/data/hotpatch/fwkpatchdir/system/systemserver";
        }
        QarthLog.d(this.TAG, "getRecordFileDir PackageName: " + this.mPackageName);
        return dir;
    }

    private boolean getRecordFileInfo() {
        String fileRegex = "([a-zA-Z]+)_([0-9])_(" + this.mPatchFileName + ")_([0-9]).log$";
        if (Constants.COMMON_PATCH_PKG_NAME.equals(this.mPackageName) && this.mContext != null && (this.mContext.getApplicationInfo().flags & 1) == 0) {
            this.mRecordFileDir = "/data/hotpatch/fwkpatchdir" + File.separator + this.mContext.getPackageName();
        }
        File file = new File(this.mRecordFileDir);
        if (!file.exists()) {
            return false;
        }
        File[] mRecordFiles = file.listFiles();
        if (mRecordFiles == null || mRecordFiles.length == 0) {
            return false;
        }
        for (File f : mRecordFiles) {
            Matcher mFileMatcher = Pattern.compile(fileRegex).matcher(f.getName());
            if (mFileMatcher.find()) {
                this.mRecordFileName = f.getName();
                this.mRecordFileNum = Integer.parseInt(mFileMatcher.group(4));
                return true;
            }
        }
        this.mRecordFileNum = 0;
        return false;
    }

    private void createRecordFile(String mPath) {
        try {
            File file = new File(mPath);
            if (!file.getParentFile().exists()) {
                if (file.getParentFile().mkdirs()) {
                    try {
                        Os.chmod(file.getParentFile().toString(), 484);
                    } catch (ErrnoException e) {
                        String str = this.TAG;
                        QarthLog.e(str, "createRecordFile exception " + e.getMessage());
                        return;
                    }
                } else {
                    QarthLog.e(this.TAG, "creat file failed, parent file error!!!");
                    return;
                }
            }
            if (file.exists()) {
                QarthLog.e(this.TAG, "creat file failed, the file already exists!!!");
                return;
            }
            if (file.createNewFile()) {
                try {
                    Os.chmod(file.toString(), 420);
                } catch (ErrnoException e2) {
                    String str2 = this.TAG;
                    QarthLog.e(str2, "createRecordFile chmod exception " + e2.getMessage());
                }
            }
        } catch (IOException ex) {
            String str3 = this.TAG;
            QarthLog.e(str3, "creat file failed, Exception!!!" + ex.getMessage());
        }
    }

    private boolean renameRecordFile(String path, String oldName, String newName) {
        if (oldName.equals(newName)) {
            return true;
        }
        File oldFile = new File(path + File.separator + oldName);
        File newFile = new File(path + File.separator + newName);
        if (!oldFile.exists() || newFile.exists() || !oldFile.renameTo(newFile)) {
            return false;
        }
        return true;
    }

    private void updateRecordFile(RecordStatusEnum recordStatusEnum, int errorCode) {
        boolean isRecorded = getRecordFileInfo();
        if (!isRecorded || this.mRecordFileNum < 1) {
            String recordFileStatus = "";
            switch (recordStatusEnum) {
                case PATCH_STATUS_DOWNLOADED:
                    recordFileStatus = "downloaded";
                    break;
                case PATCH_STATUS_LOADING:
                    recordFileStatus = "loading";
                    break;
                case PATCH_STATUS_FAILED:
                    recordFileStatus = "failed";
                    this.mRecordFileNum++;
                    break;
                case PATCH_STATUS_SUCCESS:
                    recordFileStatus = "success";
                    this.mRecordFileNum++;
                    break;
            }
            String fileName = recordFileStatus + "_" + String.valueOf(errorCode) + "_" + this.mPatchFileName + "_" + String.valueOf(this.mRecordFileNum) + ".log";
            if (Constants.COMMON_PATCH_PKG_NAME.equals(this.mPackageName) && this.mContext != null && (1 & this.mContext.getApplicationInfo().flags) == 0) {
                this.mRecordFileDir = "/data/hotpatch/fwkpatchdir" + File.separator + this.mContext.getPackageName();
            }
            String path = this.mRecordFileDir + File.separator + fileName;
            if (isRecorded) {
                renameRecordFile(this.mRecordFileDir, this.mRecordFileName, fileName);
            } else {
                createRecordFile(path);
            }
        }
    }

    public void updateRecordFileDownloaded() {
        updateRecordFile(RecordStatusEnum.PATCH_STATUS_DOWNLOADED, 0);
    }

    public void updateRecordFileLoading() {
        updateRecordFile(RecordStatusEnum.PATCH_STATUS_LOADING, 0);
    }

    public void updateRecordFileHookStatus(int errorCode) {
        if (errorCode == 0) {
            updateRecordFile(RecordStatusEnum.PATCH_STATUS_SUCCESS, 0);
        } else {
            updateRecordFile(RecordStatusEnum.PATCH_STATUS_FAILED, errorCode);
        }
    }

    public void createInvokeFileSuccess() {
        if (Constants.COMMON_PATCH_PKG_NAME.equals(this.mPackageName) && this.mContext != null && (this.mContext.getApplicationInfo().flags & 1) == 0) {
            this.mRecordFileDir = "/data/hotpatch/fwkpatchdir" + File.separator + this.mContext.getPackageName();
        }
        String fileName = this.mPatchFileName + ".success";
        createRecordFile(this.mRecordFileDir + File.separator + fileName);
    }
}

package com.msic.qarth;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.system.ErrnoException;
import android.system.Os;
import com.huawei.uikit.effect.BuildConfig;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecordProcessUtil {
    private static final String ERROR_CODE_FILE_SUFFIX = "_error_code";
    static final int NO_ERROR = 0;
    static final int QARTH_FIND_CLASS_ERR = 2;
    static final int QARTH_FIND_METHOD_ERR = 4;
    static final int QARTH_SIGNATURE_NOT_MATCH = 1;
    private static final String TAG = RecordProcessUtil.class.getSimpleName();
    private static HashSet<String> invokeResultSet = new HashSet<>();
    private final String FILE_LOG_SUFFIX = ".log";
    private final String FILE_NAME_SEPARATOR = "_";
    private final String FILE_SUC_SUFFIX = ".success";
    private final String RECORD_DIR = "/data/hotpatch/fwkpatchdir";
    private Context mContext;
    private String mPackageName;
    private String mPatchFileName;
    private String mRecordFileDir;
    private String mRecordFileName;
    private int mRecordFileNum = 0;

    /* access modifiers changed from: private */
    public enum RecordStatusEnum {
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
        QarthLog.d(TAG, "getRecordFileDir PackageName: " + this.mPackageName);
        return dir;
    }

    private boolean getRecordFileInfo() {
        Context context;
        String fileRegex = "([a-zA-Z]+)_([0-9])_(" + this.mPatchFileName + ")_([0-9]).log$";
        if (Constants.COMMON_PATCH_PKG_NAME.equals(this.mPackageName) && (context = this.mContext) != null && !"android".equals(context.getPackageName()) && (this.mContext.getApplicationInfo().flags & 1) == 0) {
            this.mRecordFileDir = "/data/hotpatch/fwkpatchdir" + File.separator + this.mContext.getPackageName();
        }
        File file = new File(this.mRecordFileDir);
        if (!file.exists()) {
            QarthLog.e(TAG, "the recorded dir is not exist:" + this.mRecordFileDir);
            return false;
        }
        File[] mRecordFiles = file.listFiles();
        if (mRecordFiles == null || mRecordFiles.length == 0) {
            QarthLog.e(TAG, "there is no recorded file in the dir");
            return false;
        }
        for (File f : mRecordFiles) {
            Matcher mFileMatcher = Pattern.compile(fileRegex).matcher(f.getName());
            if (mFileMatcher.find()) {
                this.mRecordFileName = f.getName();
                try {
                    this.mRecordFileNum = Integer.parseInt(mFileMatcher.group(4));
                    return true;
                } catch (NumberFormatException e) {
                    QarthLog.e(TAG, "parse record number exception");
                }
            }
        }
        this.mRecordFileNum = 0;
        return false;
    }

    private void createRecordFile(String mPath) {
        Context context = this.mContext;
        if (context != null && context.getApplicationInfo() != null && this.mContext.getApplicationInfo().uid == 1000 && !invokeResultSet.contains(mPath)) {
            try {
                File file = new File(mPath);
                if (!file.getParentFile().exists()) {
                    if (file.getParentFile().mkdirs()) {
                        try {
                            Os.chmod(file.getParentFile().toString(), 484);
                        } catch (ErrnoException e) {
                            String str = TAG;
                            QarthLog.e(str, "createRecordFile exception " + e.getMessage());
                            return;
                        }
                    } else {
                        QarthLog.e(TAG, "create file failed, parent file error!!!");
                        return;
                    }
                }
                if (file.exists()) {
                    if (mPath.endsWith(".success") && !invokeResultSet.contains(mPath)) {
                        invokeResultSet.add(mPath);
                    }
                    QarthLog.e(TAG, "create file failed, the file already exists!!!");
                } else if (file.createNewFile()) {
                    try {
                        String str2 = TAG;
                        QarthLog.i(str2, "create status file success, and change the permission for " + mPath);
                        if (mPath.endsWith(".success")) {
                            invokeResultSet.add(mPath);
                        }
                        Os.chmod(file.toString(), 420);
                    } catch (ErrnoException e2) {
                        String str3 = TAG;
                        QarthLog.e(str3, "createRecordFile chmod exception " + e2.getMessage());
                    }
                }
            } catch (IOException ex) {
                String str4 = TAG;
                QarthLog.e(str4, "create file failed, Exception!!!" + ex.getMessage());
            }
        }
    }

    private boolean renameRecordFile(String path, String oldName, String newName) {
        if (oldName.equals(newName)) {
            String str = TAG;
            QarthLog.e(str, "the renamed status file name is same:" + newName);
            return true;
        }
        File oldFile = new File(path + File.separator + oldName);
        File newFile = new File(path + File.separator + newName);
        if (!oldFile.exists() || newFile.exists() || !oldFile.renameTo(newFile)) {
            QarthLog.i(TAG, "rename recorded file failed.");
            return false;
        }
        String str2 = TAG;
        QarthLog.i(str2, "rename file success, old name:" + oldName + " new name:" + newName);
        return true;
    }

    private void updateRecordFile(RecordStatusEnum recordStatusEnum, int errorCode) {
        Context context;
        boolean isRecorded = getRecordFileInfo();
        if (!isRecorded || this.mRecordFileNum < 1) {
            String recordFileStatus = BuildConfig.FLAVOR;
            int i = AnonymousClass1.$SwitchMap$com$msic$qarth$RecordProcessUtil$RecordStatusEnum[recordStatusEnum.ordinal()];
            if (i == 1) {
                recordFileStatus = "downloaded";
            } else if (i == 2) {
                recordFileStatus = "loading";
            } else if (i == 3) {
                recordFileStatus = "failed";
                this.mRecordFileNum++;
            } else if (i == 4) {
                recordFileStatus = "success";
                this.mRecordFileNum++;
            }
            String fileName = recordFileStatus + "_" + String.valueOf(errorCode) + "_" + this.mPatchFileName + "_" + String.valueOf(this.mRecordFileNum) + ".log";
            if (Constants.COMMON_PATCH_PKG_NAME.equals(this.mPackageName) && (context = this.mContext) != null && !"android".equals(context.getPackageName()) && (1 & this.mContext.getApplicationInfo().flags) == 0) {
                this.mRecordFileDir = "/data/hotpatch/fwkpatchdir" + File.separator + this.mContext.getPackageName();
            }
            String path = this.mRecordFileDir + File.separator + fileName;
            if (isRecorded) {
                QarthLog.i(TAG, "the dir is " + this.mRecordFileDir + " rename file " + this.mRecordFileName + " to " + fileName);
                renameRecordFile(this.mRecordFileDir, this.mRecordFileName, fileName);
                return;
            }
            QarthLog.i(TAG, "create new recorded file " + path);
            createRecordFile(path);
            return;
        }
        QarthLog.i(TAG, "the status file count is " + this.mRecordFileNum + " file name " + this.mRecordFileName);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.msic.qarth.RecordProcessUtil$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$msic$qarth$RecordProcessUtil$RecordStatusEnum = new int[RecordStatusEnum.values().length];

        static {
            try {
                $SwitchMap$com$msic$qarth$RecordProcessUtil$RecordStatusEnum[RecordStatusEnum.PATCH_STATUS_DOWNLOADED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$msic$qarth$RecordProcessUtil$RecordStatusEnum[RecordStatusEnum.PATCH_STATUS_LOADING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$msic$qarth$RecordProcessUtil$RecordStatusEnum[RecordStatusEnum.PATCH_STATUS_FAILED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$msic$qarth$RecordProcessUtil$RecordStatusEnum[RecordStatusEnum.PATCH_STATUS_SUCCESS.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
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
        Context context;
        if (Constants.COMMON_PATCH_PKG_NAME.equals(this.mPackageName) && (context = this.mContext) != null && !"android".equals(context.getPackageName()) && (this.mContext.getApplicationInfo().flags & 1) == 0) {
            this.mRecordFileDir = "/data/hotpatch/fwkpatchdir" + File.separator + this.mContext.getPackageName();
        }
        createRecordFile(this.mRecordFileDir + File.separator + (this.mPatchFileName + ".success"));
    }

    /* access modifiers changed from: package-private */
    public void writeErrorCodeToFile(int errCode) {
        Context context = this.mContext;
        if (context == null || context.getApplicationInfo() == null) {
            QarthLog.i(TAG, "the app info is null and return");
            return;
        }
        ApplicationInfo appInfo = this.mContext.getApplicationInfo();
        if (appInfo == null || appInfo.uid != 1000) {
            QarthLog.i(TAG, "no permission to write error code, the app is " + appInfo.packageName + " uid is " + appInfo.uid);
            return;
        }
        String errCodePath = this.mRecordFileDir + File.separator + this.mPatchFileName + ERROR_CODE_FILE_SUFFIX;
        File errcodeFile = new File(errCodePath);
        if (errcodeFile.exists()) {
            QarthLog.i(TAG, "the error code file is exists before, no need to write again");
            return;
        }
        createRecordFile(errCodePath);
        if (!errcodeFile.exists()) {
            QarthLog.e(TAG, "create the error code file failed");
        } else {
            writeErrorCodeToFileLock(errCodePath, errCode);
        }
    }

    private void writeErrorCodeToFileLock(String errCodePath, int errCode) {
        FileLock fileLock = null;
        FileChannel channel = null;
        FileOutputStream outputStream = null;
        try {
            FileOutputStream outputStream2 = new FileOutputStream(errCodePath);
            FileChannel channel2 = outputStream2.getChannel();
            FileLock fileLock2 = channel2.tryLock();
            if (new File(errCodePath).length() == 0) {
                String str = TAG;
                QarthLog.i(str, "write the error code to file and code is " + errCode);
                ByteBuffer byteBuff = Charset.forName("UTF-8").newEncoder().encode(CharBuffer.wrap(String.valueOf(errCode)));
                byte[] bytes = new byte[byteBuff.limit()];
                byteBuff.get(bytes);
                outputStream2.write(bytes);
            }
            if (fileLock2 != null) {
                try {
                    fileLock2.release();
                } catch (IOException e) {
                    QarthLog.e(TAG, "close file lock failed.");
                }
            }
            try {
                channel2.close();
            } catch (IOException e2) {
                QarthLog.e(TAG, "close channel failed.");
            }
            try {
                outputStream2.close();
            } catch (IOException e3) {
                QarthLog.e(TAG, "close file output stream failed.");
            }
        } catch (IllegalArgumentException e4) {
            QarthLog.e(TAG, "encode charset illegal argument exception");
            if (0 != 0) {
                try {
                    fileLock.release();
                } catch (IOException e5) {
                    QarthLog.e(TAG, "close file lock failed.");
                }
            }
            if (0 != 0) {
                try {
                    channel.close();
                } catch (IOException e6) {
                    QarthLog.e(TAG, "close channel failed.");
                }
            }
            if (0 != 0) {
                outputStream.close();
            }
        } catch (IllegalStateException e7) {
            QarthLog.e(TAG, "encode charset illegal state exception");
            if (0 != 0) {
                try {
                    fileLock.release();
                } catch (IOException e8) {
                    QarthLog.e(TAG, "close file lock failed.");
                }
            }
            if (0 != 0) {
                try {
                    channel.close();
                } catch (IOException e9) {
                    QarthLog.e(TAG, "close channel failed.");
                }
            }
            if (0 != 0) {
                outputStream.close();
            }
        } catch (IOException e10) {
            QarthLog.e(TAG, "current app get the file lock failed.");
            if (0 != 0) {
                try {
                    fileLock.release();
                } catch (IOException e11) {
                    QarthLog.e(TAG, "close file lock failed.");
                }
            }
            if (0 != 0) {
                try {
                    channel.close();
                } catch (IOException e12) {
                    QarthLog.e(TAG, "close channel failed.");
                }
            }
            if (0 != 0) {
                outputStream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fileLock.release();
                } catch (IOException e13) {
                    QarthLog.e(TAG, "close file lock failed.");
                }
            }
            if (0 != 0) {
                try {
                    channel.close();
                } catch (IOException e14) {
                    QarthLog.e(TAG, "close channel failed.");
                }
            }
            if (0 != 0) {
                try {
                    outputStream.close();
                } catch (IOException e15) {
                    QarthLog.e(TAG, "close file output stream failed.");
                }
            }
            throw th;
        }
    }
}

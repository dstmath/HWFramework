package com.android.server.rms.iaware.bigdata;

import android.rms.iaware.AwareLog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AwareBigDataFile {
    private static final String TAG = "IAwareStatFile";
    private String mTargetStatFilePath = null;

    public AwareBigDataFile(String filePath) {
        this.mTargetStatFilePath = filePath;
        AwareLog.d(TAG, "AwareBigDataFile path = " + this.mTargetStatFilePath);
    }

    private boolean bigDataFileExists(String filePath, String fileName) {
        return new File(filePath, fileName).exists();
    }

    private void mkDir(String path) {
        File fileDir = new File(path);
        if (fileDir.exists()) {
            AwareLog.d(TAG, path + " already exists");
            return;
        }
        AwareLog.d(TAG, "Create " + path);
        if (!fileDir.mkdirs()) {
            AwareLog.w(TAG, "Failed to create " + path);
        }
    }

    private File createFileForWrite(String filePath, String fileName) {
        File file = new File(filePath, fileName);
        if (!file.exists() || file.delete()) {
            try {
                if (file.createNewFile()) {
                    return file;
                }
                AwareLog.w(TAG, "createFileForWrite createNewFile error!");
                return null;
            } catch (IOException e) {
                AwareLog.e(TAG, "createFileForWrite createNewFile ioException!");
                return null;
            }
        } else {
            AwareLog.w(TAG, "delete file error!");
            return null;
        }
    }

    public void saveData(String data, String fileName, int flag) {
        File bigDataFile;
        String filePath = this.mTargetStatFilePath;
        if (filePath != null && data != null && fileName != null) {
            String data2 = data + System.lineSeparator();
            mkDir(filePath);
            FileOutputStream outputStream = null;
            if (bigDataFileExists(filePath, fileName)) {
                bigDataFile = new File(filePath, fileName);
            } else {
                bigDataFile = createFileForWrite(filePath, fileName);
            }
            if (bigDataFile != null) {
                if (flag == 0) {
                    try {
                        outputStream = new FileOutputStream(bigDataFile, true);
                    } catch (IOException e) {
                        AwareLog.e(TAG, "saveData IOException");
                    } catch (RuntimeException e2) {
                        AwareLog.e(TAG, "saveData RuntimeException");
                    } catch (Throwable th) {
                        closeFileOutputStream(null);
                        throw th;
                    }
                } else {
                    outputStream = new FileOutputStream(bigDataFile, false);
                }
                byte[] outputString = data2.getBytes("utf-8");
                outputStream.write(outputString, 0, outputString.length);
                AwareLog.d(TAG, "saveData success!");
                closeFileOutputStream(outputStream);
            }
        }
    }

    private void closeFileOutputStream(FileOutputStream fileOutputStream) {
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "closeFileOutputStream error!");
            }
        }
    }
}

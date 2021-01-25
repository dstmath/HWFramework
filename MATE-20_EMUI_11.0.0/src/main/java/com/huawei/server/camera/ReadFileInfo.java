package com.huawei.server.camera;

import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReadFileInfo {
    private static final String HUAWEI_CAMERA_RISE_DESCEND_TEST = "HUAWEI_CAMERA_RISE_DESCEND_TEST";
    private static final int NEGATIVE_NUMBER_ONE = -1;
    private static final String READ_NO_CONTENT = "";
    private static final String TAG = ReadFileInfo.class.getName();
    private String fileName = "";
    private boolean isForTest = false;

    public ReadFileInfo(String file, boolean isTestFile) {
        this.fileName = file;
        this.isForTest = isTestFile;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x006f, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0074, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0075, code lost:
        r6.addSuppressed(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0078, code lost:
        throw r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x007b, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0080, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0081, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0084, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0087, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x008c, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x008d, code lost:
        r4.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0090, code lost:
        throw r5;
     */
    public String readFileToString() {
        String content = "";
        File file = new File(this.fileName);
        if (!file.exists()) {
            return "";
        }
        try {
            FileInputStream inputStream = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader br = new BufferedReader(reader);
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                if (!this.isForTest || line.indexOf(HUAWEI_CAMERA_RISE_DESCEND_TEST) == NEGATIVE_NUMBER_ONE) {
                    content = content + line;
                } else {
                    String str = line.split("=")[1];
                    br.close();
                    reader.close();
                    inputStream.close();
                    return str;
                }
            }
            br.close();
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "ReadFileInfo io exception");
        }
        if (this.isForTest) {
            return "";
        }
        return content;
    }
}

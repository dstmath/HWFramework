package com.android.server.camera;

import android.util.Slog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReadFileInfo {
    private static final String HUAWEI_CAMERA_RISE_DESCEND_TEST = "HUAWEI_CAMERA_RISE_DESCEND_TEST";
    private static final String READ_NO_CONTENT = "";
    private static final String TAG = ReadFileInfo.class.getName();
    private String mFileName = "";
    private boolean mIsTestFile = false;

    public ReadFileInfo(String file, boolean isTestFile) {
        this.mFileName = file;
        this.mIsTestFile = isTestFile;
    }

    public String readFileToString() {
        BufferedReader br = null;
        InputStreamReader reader = null;
        FileInputStream inputStream = null;
        String content = "";
        try {
            File file = new File(this.mFileName);
            if (!file.exists()) {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        Slog.e(TAG, "br close fail");
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e2) {
                        Slog.e(TAG, "reader close fail");
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e3) {
                        Slog.e(TAG, "file close fail");
                    }
                }
                return "";
            }
            inputStream = new FileInputStream(file);
            reader = new InputStreamReader(inputStream, "UTF-8");
            br = new BufferedReader(reader);
            String line = br.readLine();
            while (line != null) {
                if (!this.mIsTestFile || line.indexOf(HUAWEI_CAMERA_RISE_DESCEND_TEST) == -1) {
                    content = content + line;
                    line = br.readLine();
                } else {
                    String str = line.split("=")[1];
                    try {
                        br.close();
                    } catch (IOException e4) {
                        Slog.e(TAG, "br close fail");
                    }
                    try {
                        reader.close();
                    } catch (IOException e5) {
                        Slog.e(TAG, "reader close fail");
                    }
                    try {
                        inputStream.close();
                    } catch (IOException e6) {
                        Slog.e(TAG, "file close fail");
                    }
                    return str;
                }
            }
            try {
                br.close();
            } catch (IOException e7) {
                Slog.e(TAG, "br close fail");
            }
            try {
                reader.close();
            } catch (IOException e8) {
                Slog.e(TAG, "reader close fail");
            }
            try {
                inputStream.close();
            } catch (IOException e9) {
                Slog.e(TAG, "file close fail");
            }
            return this.mIsTestFile ? "" : content;
        } catch (IOException e10) {
            Slog.e(TAG, "ReadFileInfo io exception");
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e11) {
                    Slog.e(TAG, "br close fail");
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e12) {
                    Slog.e(TAG, "reader close fail");
                }
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e13) {
                    Slog.e(TAG, "br close fail");
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e14) {
                    Slog.e(TAG, "reader close fail");
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e15) {
                    Slog.e(TAG, "file close fail");
                }
            }
            throw th;
        }
    }
}

package com.android.server.hidata.wavemapping.util;

import com.android.server.hidata.wavemapping.cons.Constant;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileUtils {
    public static final String ERROR_RET = "FILE_BIGGER";
    public static final int FILEPATH_MAX_SIZE = 500;
    private static final String TAG = ("WMapping." + FileUtils.class.getSimpleName());

    public static boolean delFile(String path) {
        if (path == null || path.equals("")) {
            LogUtil.d(" delFile path=null || path== \"\" ");
            return false;
        }
        try {
            File file = new File(path);
            if (!checkFilePath(file)) {
                return false;
            }
            if (file.exists() && !file.delete()) {
                LogUtil.e("delFile failure.path:" + path);
            }
            return true;
        } catch (Exception e) {
            LogUtil.e("Exception delFile----" + e);
            return false;
        }
    }

    public static boolean mkdir(String tileFilePath) {
        boolean ret = false;
        if (tileFilePath == null || tileFilePath.equals("")) {
            LogUtil.e(" mkdir tileFilePath=null or tileFilePath == ");
            return false;
        }
        try {
            File file = new File(tileFilePath);
            if (!checkFilePath(file)) {
                return false;
            }
            if (!file.exists() && !file.isDirectory() && !file.mkdirs()) {
                return false;
            }
            if (file.exists()) {
                ret = true;
            }
            return ret;
        } catch (Exception e) {
            LogUtil.e("Exception mkdir" + e);
            return false;
        }
    }

    public static boolean addFileHead(String filePath, String content) {
        FileOutputStream fout;
        StringBuilder sb;
        File tgFile = new File(filePath);
        if (!checkFilePath(tgFile)) {
            return false;
        }
        fout = null;
        OutputStreamWriter osw = null;
        try {
            if (!tgFile.exists()) {
                fout = new FileOutputStream(new File(filePath), true);
                osw = new OutputStreamWriter(fout, "UTF-8");
                osw.write(content);
            }
            if (osw != null) {
                try {
                    osw.flush();
                } catch (IOException e) {
                    LogUtil.e("saveFile:" + e);
                }
                closeFileStreamNotThrow(osw);
            }
            if (fout != null) {
                try {
                    fout.flush();
                } catch (IOException e2) {
                    e = e2;
                    sb = new StringBuilder();
                }
                closeFileStreamNotThrow(fout);
            }
        } catch (Exception e3) {
            LogUtil.e("addFileHead failure.filePath:" + filePath + ",content:" + content);
            if (osw != null) {
                try {
                    osw.flush();
                } catch (IOException e4) {
                    LogUtil.e("saveFile:" + e4);
                }
                closeFileStreamNotThrow(osw);
            }
            if (fout != null) {
                try {
                    fout.flush();
                } catch (IOException e5) {
                    e = e5;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (osw != null) {
                try {
                    osw.flush();
                } catch (IOException e6) {
                    LogUtil.e("saveFile:" + e6);
                }
                closeFileStreamNotThrow(osw);
            }
            if (fout != null) {
                try {
                    fout.flush();
                } catch (IOException e7) {
                    LogUtil.e("saveFile:" + e7);
                }
                closeFileStreamNotThrow(fout);
            }
            throw th;
        }
        return true;
        sb.append("saveFile:");
        sb.append(e);
        LogUtil.e(sb.toString());
        closeFileStreamNotThrow(fout);
        return true;
    }

    public static boolean writeFile(String filePath, String content) {
        StringBuilder sb;
        boolean ret = false;
        File tgFile = new File(filePath);
        if (!checkFilePath(tgFile)) {
            return false;
        }
        FileOutputStream fout = null;
        OutputStreamWriter osw = null;
        try {
            if (!tgFile.exists()) {
                tgFile = new File(filePath);
            }
            fout = new FileOutputStream(tgFile, true);
            OutputStreamWriter osw2 = new OutputStreamWriter(fout, "UTF-8");
            osw2.write(content);
            ret = true;
            try {
                osw2.flush();
            } catch (IOException e) {
                LogUtil.e("saveFile:" + e);
            }
            closeFileStreamNotThrow(osw2);
            try {
                fout.flush();
            } catch (IOException e2) {
                e = e2;
                sb = new StringBuilder();
            }
        } catch (Exception e3) {
            LogUtil.e("writeFile failure.filePath:" + filePath + ",content:" + content);
            if (osw != null) {
                try {
                    osw.flush();
                } catch (IOException e4) {
                    LogUtil.e("saveFile:" + e4);
                }
                closeFileStreamNotThrow(osw);
            }
            if (fout != null) {
                try {
                    fout.flush();
                } catch (IOException e5) {
                    e = e5;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (osw != null) {
                try {
                    osw.flush();
                } catch (IOException e6) {
                    LogUtil.e("saveFile:" + e6);
                }
                closeFileStreamNotThrow(osw);
            }
            if (fout != null) {
                try {
                    fout.flush();
                } catch (IOException e7) {
                    LogUtil.e("saveFile:" + e7);
                }
                closeFileStreamNotThrow(fout);
            }
            throw th;
        }
        closeFileStreamNotThrow(fout);
        return ret;
        sb.append("saveFile:");
        sb.append(e);
        LogUtil.e(sb.toString());
        closeFileStreamNotThrow(fout);
        return ret;
    }

    public static boolean saveFile(String filePath, String content) {
        StringBuilder sb;
        BufferedWriter bfw = null;
        boolean ret = false;
        FileOutputStream writerStream = null;
        try {
            writerStream = new FileOutputStream(filePath);
            if (!checkFilePath(filePath)) {
                if (bfw != null) {
                    try {
                        bfw.flush();
                    } catch (IOException e) {
                        LogUtil.e("saveFile:" + e);
                    }
                    closeFileStreamNotThrow(bfw);
                }
                try {
                    writerStream.flush();
                } catch (IOException e2) {
                    LogUtil.e("saveFile:" + e2);
                }
                closeFileStreamNotThrow(writerStream);
                return false;
            }
            bfw = new BufferedWriter(new OutputStreamWriter(writerStream, "UTF-8"));
            bfw.write(content);
            bfw.newLine();
            ret = true;
            try {
                bfw.flush();
            } catch (IOException e3) {
                LogUtil.e("saveFile:" + e3);
            }
            closeFileStreamNotThrow(bfw);
            try {
                writerStream.flush();
            } catch (IOException e4) {
                e = e4;
                sb = new StringBuilder();
            }
            closeFileStreamNotThrow(writerStream);
            return ret;
            sb.append("saveFile:");
            sb.append(e);
            LogUtil.e(sb.toString());
            closeFileStreamNotThrow(writerStream);
            return ret;
        } catch (IOException e5) {
            e5.printStackTrace();
            if (bfw != null) {
                try {
                    bfw.flush();
                } catch (IOException e6) {
                    LogUtil.e("saveFile:" + e6);
                }
                closeFileStreamNotThrow(bfw);
            }
            if (writerStream != null) {
                try {
                    writerStream.flush();
                } catch (IOException e7) {
                    e = e7;
                    sb = new StringBuilder();
                }
            }
        } catch (Exception e8) {
            LogUtil.e("saveFile,e" + e8.getMessage());
            if (bfw != null) {
                try {
                    bfw.flush();
                } catch (IOException e9) {
                    LogUtil.e("saveFile:" + e9);
                }
                closeFileStreamNotThrow(bfw);
            }
            if (writerStream != null) {
                try {
                    writerStream.flush();
                } catch (IOException e10) {
                    e = e10;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (bfw != null) {
                try {
                    bfw.flush();
                } catch (IOException e11) {
                    LogUtil.e("saveFile:" + e11);
                }
                closeFileStreamNotThrow(bfw);
            }
            if (writerStream != null) {
                try {
                    writerStream.flush();
                } catch (IOException e12) {
                    LogUtil.e("saveFile:" + e12);
                }
                closeFileStreamNotThrow(writerStream);
            }
            throw th;
        }
    }

    public static String getFileContent(String fPath) {
        String res = "";
        if (fPath == null || fPath.equals("")) {
            return res;
        }
        File tgFile = new File(fPath);
        if (!checkFilePath(tgFile)) {
            return res;
        }
        FileInputStream fin = null;
        try {
            if (tgFile.isFile()) {
                fin = new FileInputStream(fPath);
                int length = fin.available();
                if (length > 5000000) {
                    LogUtil.d("fPath:" + fPath + ",len:" + length);
                    closeFileStreamNotThrow(fin);
                    return ERROR_RET;
                }
                byte[] buffer = new byte[length];
                if (fin.read(buffer) != -1) {
                    res = new String(buffer, "utf-8");
                }
            }
        } catch (Exception e) {
            LogUtil.e("getFileContent,e" + e.getMessage());
        } catch (Throwable th) {
            closeFileStreamNotThrow(fin);
            throw th;
        }
        closeFileStreamNotThrow(fin);
        return res;
    }

    public static boolean isFileExists(String fPath) {
        if (fPath == null || fPath.equals("")) {
            return false;
        }
        File tgFile = new File(fPath);
        if (!checkFilePath(tgFile)) {
            return false;
        }
        try {
            if (tgFile.isFile()) {
                return true;
            }
        } catch (Exception e) {
            LogUtil.e("isFileExists:" + e);
        }
        return false;
    }

    public static boolean checkFilePath(File file) {
        boolean ret = false;
        try {
            String filePath = file.getCanonicalPath();
            if (filePath != null) {
                if (!filePath.equals("")) {
                    if (filePath.length() > 500) {
                        return false;
                    }
                    String filePathSlash = filePath + "//";
                    if (filePath.startsWith(Constant.getROOTPath()) || filePathSlash.startsWith(Constant.getROOTPath())) {
                        ret = true;
                    }
                    return ret;
                }
            }
            return false;
        } catch (IOException e) {
            LogUtil.d("checkFilePath,filePath:" + "");
        }
    }

    public static boolean checkFilePath(String filePath) {
        if (filePath == null || filePath.equals("") || !filePath.startsWith(Constant.getROOTPath()) || filePath.length() > 500) {
            return false;
        }
        return true;
    }

    public static long getDirSize(File file) {
        if (file == null) {
            try {
                LogUtil.e(" getDirSize file=null ");
                return 0;
            } catch (Exception e) {
                if (file != null) {
                    LogUtil.e("getDirSize e:" + e + ",file=" + file.getAbsolutePath());
                }
                return 0;
            }
        } else if (!file.exists()) {
            LogUtil.d("getDirSize file do not exist!");
            return 0;
        } else if (!file.isDirectory()) {
            return file.length();
        } else {
            File[] children = file.listFiles();
            long size = 0;
            if (children != null) {
                for (File f : children) {
                    size += getDirSize(f);
                }
            }
            return size;
        }
    }

    private static void closeFileStreamNotThrow(Closeable fis) {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
            }
        }
    }
}

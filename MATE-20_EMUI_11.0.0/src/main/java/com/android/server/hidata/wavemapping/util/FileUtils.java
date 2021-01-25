package com.android.server.hidata.wavemapping.util;

import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileUtils {
    private static final int DEFAULT_VALUE = -1;
    public static final String ERROR_RET = "FILE_BIGGER";
    private static final int MAX_FILE_SIZE = 5000000;
    private static final String TAG = ("WMapping." + FileUtils.class.getSimpleName());
    private static final String UTF8_SYMBOL = "UTF-8";

    private FileUtils() {
    }

    public static boolean delFile(String path) {
        if (path == null || "".equals(path)) {
            LogUtil.d(false, " delFile path=null || path== \"\" ", new Object[0]);
            return false;
        }
        try {
            File file = new File(path);
            if (file.exists() && !file.delete()) {
                LogUtil.e(false, "delFile failure.path:%{public}s", path);
            }
            return true;
        } catch (SecurityException e) {
            LogUtil.e(false, "delFile failed by Exception", new Object[0]);
            return false;
        }
    }

    public static boolean mkdir(String tileFilePath) {
        if (tileFilePath == null || "".equals(tileFilePath)) {
            LogUtil.e(false, " mkdir tileFilePath=null or tileFilePath == ", new Object[0]);
            return false;
        }
        try {
            File file = new File(tileFilePath);
            if ((file.exists() || file.isDirectory() || file.mkdirs()) && file.exists()) {
                return true;
            }
            return false;
        } catch (SecurityException e) {
            LogUtil.e(false, "mkdir failed by Exception", new Object[0]);
            return false;
        }
    }

    public static boolean addFileHead(String filePath, String content) {
        Object[] objArr;
        FileOutputStream fout = null;
        OutputStreamWriter osw = null;
        try {
            if (!new File(filePath).exists()) {
                fout = new FileOutputStream(new File(filePath), true);
                osw = new OutputStreamWriter(fout, UTF8_SYMBOL);
                osw.write(content);
            }
            if (osw != null) {
                try {
                    osw.flush();
                } catch (IOException e) {
                    LogUtil.e(false, "saveFile:%{public}s", e.getMessage());
                }
                closeFileStreamNotThrow(osw);
            }
            if (fout != null) {
                try {
                    fout.flush();
                } catch (IOException e2) {
                    objArr = new Object[]{e2.getMessage()};
                }
                closeFileStreamNotThrow(fout);
            }
        } catch (IOException e3) {
            LogUtil.e(false, "addFileHead failure.filePath:%{public}s,content:%{public}s", filePath, content);
            if (0 != 0) {
                try {
                    osw.flush();
                } catch (IOException e4) {
                    LogUtil.e(false, "saveFile:%{public}s", e4.getMessage());
                }
                closeFileStreamNotThrow(null);
            }
            if (0 != 0) {
                try {
                    fout.flush();
                } catch (IOException e5) {
                    objArr = new Object[]{e5.getMessage()};
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    osw.flush();
                } catch (IOException e6) {
                    LogUtil.e(false, "saveFile:%{public}s", e6.getMessage());
                }
                closeFileStreamNotThrow(null);
            }
            if (0 != 0) {
                try {
                    fout.flush();
                } catch (IOException e7) {
                    LogUtil.e(false, "saveFile:%{public}s", e7.getMessage());
                }
                closeFileStreamNotThrow(null);
            }
            throw th;
        }
        return true;
        LogUtil.e(false, "saveFile:%{public}s", objArr);
        closeFileStreamNotThrow(fout);
        return true;
    }

    public static boolean writeFile(String filePath, String content) {
        Object[] objArr;
        File tgFile = new File(filePath);
        FileOutputStream fout = null;
        OutputStreamWriter osw = null;
        boolean isWrite = false;
        try {
            if (!tgFile.exists()) {
                tgFile = new File(filePath);
            }
            fout = new FileOutputStream(tgFile, true);
            OutputStreamWriter osw2 = new OutputStreamWriter(fout, UTF8_SYMBOL);
            osw2.write(content);
            isWrite = true;
            try {
                osw2.flush();
            } catch (IOException e) {
                LogUtil.e(false, "saveFile:%{public}s", e.getMessage());
            }
            closeFileStreamNotThrow(osw2);
            try {
                fout.flush();
            } catch (IOException e2) {
                objArr = new Object[]{e2.getMessage()};
            }
        } catch (IOException e3) {
            LogUtil.e(false, "writeFile failure.filePath:%{public}s,content:%{public}s", filePath, content);
            if (0 != 0) {
                try {
                    osw.flush();
                } catch (IOException e4) {
                    LogUtil.e(false, "saveFile:%{public}s", e4.getMessage());
                }
                closeFileStreamNotThrow(null);
            }
            if (0 != 0) {
                try {
                    fout.flush();
                } catch (IOException e5) {
                    objArr = new Object[]{e5.getMessage()};
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    osw.flush();
                } catch (IOException e6) {
                    LogUtil.e(false, "saveFile:%{public}s", e6.getMessage());
                }
                closeFileStreamNotThrow(null);
            }
            if (0 != 0) {
                try {
                    fout.flush();
                } catch (IOException e7) {
                    LogUtil.e(false, "saveFile:%{public}s", e7.getMessage());
                }
                closeFileStreamNotThrow(null);
            }
            throw th;
        }
        closeFileStreamNotThrow(fout);
        return isWrite;
        LogUtil.e(false, "saveFile:%{public}s", objArr);
        closeFileStreamNotThrow(fout);
        return isWrite;
    }

    public static boolean saveFile(String filePath, String content) {
        Object[] objArr;
        BufferedWriter bfw = null;
        boolean isSave = false;
        FileOutputStream writerStream = null;
        try {
            writerStream = new FileOutputStream(filePath);
            bfw = new BufferedWriter(new OutputStreamWriter(writerStream, UTF8_SYMBOL));
            bfw.write(content);
            bfw.newLine();
            isSave = true;
            try {
                bfw.flush();
            } catch (IOException e) {
                LogUtil.e(false, "saveFile:%{public}s", e.getMessage());
            }
            closeFileStreamNotThrow(bfw);
            try {
                writerStream.flush();
            } catch (IOException e2) {
                objArr = new Object[]{e2.getMessage()};
            }
        } catch (IOException e3) {
            LogUtil.e(false, "saveFile failed by IOException", new Object[0]);
            if (bfw != null) {
                try {
                    bfw.flush();
                } catch (IOException e4) {
                    LogUtil.e(false, "saveFile:%{public}s", e4.getMessage());
                }
                closeFileStreamNotThrow(bfw);
            }
            if (writerStream != null) {
                try {
                    writerStream.flush();
                } catch (IOException e5) {
                    objArr = new Object[]{e5.getMessage()};
                }
            }
        } catch (Throwable th) {
            if (bfw != null) {
                try {
                    bfw.flush();
                } catch (IOException e6) {
                    LogUtil.e(false, "saveFile:%{public}s", e6.getMessage());
                }
                closeFileStreamNotThrow(bfw);
            }
            if (writerStream != null) {
                try {
                    writerStream.flush();
                } catch (IOException e7) {
                    LogUtil.e(false, "saveFile:%{public}s", e7.getMessage());
                }
                closeFileStreamNotThrow(writerStream);
            }
            throw th;
        }
        closeFileStreamNotThrow(writerStream);
        return isSave;
        LogUtil.e(false, "saveFile:%{public}s", objArr);
        closeFileStreamNotThrow(writerStream);
        return isSave;
    }

    public static String getFileContent(String filePath) {
        String res = "";
        if (TextUtils.isEmpty(filePath)) {
            return res;
        }
        File tgFile = new File(filePath);
        if (!tgFile.isFile() || !tgFile.exists()) {
            return res;
        }
        InputStreamReader streamReader = null;
        BufferedReader bufferedReader = null;
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(filePath);
            if (fin.available() > MAX_FILE_SIZE) {
                closeFileStreamNotThrow(null);
                closeFileStreamNotThrow(null);
                closeFileStreamNotThrow(fin);
                return ERROR_RET;
            }
            streamReader = new InputStreamReader(fin, UTF8_SYMBOL);
            bufferedReader = new BufferedReader(streamReader);
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                stringBuffer.append(line);
                stringBuffer.append(System.lineSeparator());
            }
            res = stringBuffer.toString();
            closeFileStreamNotThrow(bufferedReader);
            closeFileStreamNotThrow(streamReader);
            closeFileStreamNotThrow(fin);
            return res;
        } catch (IOException e) {
            LogUtil.e(false, "getFileContent failed by Exception", new Object[0]);
        } catch (Throwable th) {
            closeFileStreamNotThrow(null);
            closeFileStreamNotThrow(null);
            closeFileStreamNotThrow(null);
            throw th;
        }
    }

    public static boolean isFileExists(String filePath) {
        if (filePath == null || "".equals(filePath)) {
            return false;
        }
        try {
            if (new File(filePath).isFile()) {
                return true;
            }
            return false;
        } catch (SecurityException e) {
            LogUtil.e(false, "isFileExists failed by Exception", new Object[0]);
        }
    }

    public static long getDirSize(File file) {
        if (file == null) {
            try {
                LogUtil.e(false, " getDirSize file=null ", new Object[0]);
                return 0;
            } catch (SecurityException e) {
                LogUtil.e(false, "getDirSize failed by Exception", new Object[0]);
                return 0;
            }
        } else if (!file.exists()) {
            LogUtil.d(false, "getDirSize file do not exist!", new Object[0]);
            return 0;
        } else if (!file.isDirectory()) {
            return file.length();
        } else {
            File[] childrens = file.listFiles();
            if (childrens == null) {
                return 0;
            }
            long size = 0;
            for (File f : childrens) {
                size += getDirSize(f);
            }
            return size;
        }
    }

    private static void closeFileStreamNotThrow(Closeable fis) {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                LogUtil.e(false, "close file Exception", new Object[0]);
            }
        }
    }
}

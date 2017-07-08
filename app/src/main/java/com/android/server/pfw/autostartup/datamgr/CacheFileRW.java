package com.android.server.pfw.autostartup.datamgr;

import android.util.AtomicFile;
import com.android.server.pfw.log.HwPFWLogger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

class CacheFileRW {
    private static final String TAG = "CacheFileRW";
    private AtomicFile mAtomicFile;

    CacheFileRW(File file) {
        this.mAtomicFile = new AtomicFile(file);
    }

    List<String> readFileLines() {
        Throwable th;
        List<String> result = new ArrayList();
        BufferedReader bufferedReader = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(this.mAtomicFile.openRead(), Charset.defaultCharset()));
            while (true) {
                try {
                    String tmp = br.readLine();
                    if (tmp == null) {
                        break;
                    }
                    result.add(tmp);
                } catch (FileNotFoundException e) {
                    bufferedReader = br;
                } catch (IOException e2) {
                    bufferedReader = br;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedReader = br;
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
            }
        } catch (FileNotFoundException e4) {
            try {
                HwPFWLogger.e(TAG, "readFileLines file not exist: " + this.mAtomicFile);
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e32) {
                        e32.printStackTrace();
                    }
                }
                HwPFWLogger.d(TAG, "readFileLines result: " + result);
                return result;
            } catch (Throwable th3) {
                th = th3;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e322) {
                        e322.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (IOException e5) {
            HwPFWLogger.e(TAG, "readFileLines catch IOException!");
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e3222) {
                    e3222.printStackTrace();
                }
            }
            HwPFWLogger.d(TAG, "readFileLines result: " + result);
            return result;
        }
        HwPFWLogger.d(TAG, "readFileLines result: " + result);
        return result;
    }

    void writeFileLines(List<String> lines) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = this.mAtomicFile.startWrite();
            StringBuffer buf = new StringBuffer();
            for (String line : lines) {
                buf.append(line).append("\n");
            }
            fileOutputStream.write(buf.toString().getBytes(Charset.defaultCharset()));
            this.mAtomicFile.finishWrite(fileOutputStream);
        } catch (IOException ex) {
            HwPFWLogger.e(TAG, "writeFileLines catch IOException: " + ex);
            this.mAtomicFile.failWrite(fileOutputStream);
        }
    }
}

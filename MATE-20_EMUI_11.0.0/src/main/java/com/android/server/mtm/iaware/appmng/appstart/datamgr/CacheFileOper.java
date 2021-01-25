package com.android.server.mtm.iaware.appmng.appstart.datamgr;

import android.rms.iaware.AwareLog;
import android.util.AtomicFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
public class CacheFileOper {
    private static final int FILE_MAX_SIZE = 262144;
    private static final String TAG = "CacheFileOper";
    private AtomicFile mAtomicFile;

    CacheFileOper(File file) {
        this.mAtomicFile = new AtomicFile(file);
    }

    /* access modifiers changed from: package-private */
    public List<String> readFileLines() {
        List<String> result = new ArrayList<>();
        FileInputStream stream = null;
        BufferedReader br = null;
        try {
            FileInputStream stream2 = this.mAtomicFile.openRead();
            if (stream2 == null) {
                if (0 != 0) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        AwareLog.e(TAG, "readFileLines br catch IOException in finally!");
                    }
                }
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e2) {
                        AwareLog.e(TAG, "readFileLines stream catch IOException in finally!");
                    }
                }
                return result;
            } else if (stream2.available() > FILE_MAX_SIZE) {
                AwareLog.e(TAG, "readFileLines file size is more than 32K!");
                if (0 != 0) {
                    try {
                        br.close();
                    } catch (IOException e3) {
                        AwareLog.e(TAG, "readFileLines br catch IOException in finally!");
                    }
                }
                try {
                    stream2.close();
                } catch (IOException e4) {
                    AwareLog.e(TAG, "readFileLines stream catch IOException in finally!");
                }
                return result;
            } else {
                BufferedReader br2 = new BufferedReader(new InputStreamReader(stream2, "utf-8"));
                while (true) {
                    String tmp = br2.readLine();
                    if (tmp != null) {
                        result.add(tmp);
                    } else {
                        try {
                            break;
                        } catch (IOException e5) {
                            AwareLog.e(TAG, "readFileLines br catch IOException in finally!");
                        }
                    }
                }
                br2.close();
                try {
                    stream2.close();
                } catch (IOException e6) {
                    AwareLog.e(TAG, "readFileLines stream catch IOException in finally!");
                }
                AwareLog.d(TAG, "readFileLines result: " + result);
                return result;
            }
        } catch (FileNotFoundException e7) {
            AwareLog.e(TAG, "readFileLines file not exist: " + this.mAtomicFile);
            if (0 != 0) {
                try {
                    br.close();
                } catch (IOException e8) {
                    AwareLog.e(TAG, "readFileLines br catch IOException in finally!");
                }
            }
            if (0 != 0) {
                stream.close();
            }
        } catch (IOException e9) {
            AwareLog.e(TAG, "readFileLines catch IOException!");
            if (0 != 0) {
                try {
                    br.close();
                } catch (IOException e10) {
                    AwareLog.e(TAG, "readFileLines br catch IOException in finally!");
                }
            }
            if (0 != 0) {
                stream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    br.close();
                } catch (IOException e11) {
                    AwareLog.e(TAG, "readFileLines br catch IOException in finally!");
                }
            }
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e12) {
                    AwareLog.e(TAG, "readFileLines stream catch IOException in finally!");
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void writeFileLines(List<String> lines) {
        try {
            FileOutputStream fos = this.mAtomicFile.startWrite();
            StringBuffer buf = new StringBuffer();
            for (String line : lines) {
                buf.append(line);
                buf.append("\n");
            }
            fos.write(buf.toString().getBytes("utf-8"));
            this.mAtomicFile.finishWrite(fos);
        } catch (IOException e) {
            AwareLog.e(TAG, "writeFileLines catch IOException!");
            this.mAtomicFile.failWrite(null);
        }
    }
}

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

class CacheFileRW {
    private static final String TAG = "CacheFileRW";
    private AtomicFile mAtomicFile;

    CacheFileRW(File file) {
        this.mAtomicFile = new AtomicFile(file);
    }

    /* JADX WARNING: Removed duplicated region for block: B:70:0x0106 A:{SYNTHETIC, Splitter: B:70:0x0106} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x010b A:{SYNTHETIC, Splitter: B:73:0x010b} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00ce A:{SYNTHETIC, Splitter: B:55:0x00ce} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00d3 A:{SYNTHETIC, Splitter: B:58:0x00d3} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    List<String> readFileLines() {
        Throwable th;
        List<String> result = new ArrayList();
        FileInputStream fileInputStream = null;
        BufferedReader br = null;
        try {
            fileInputStream = this.mAtomicFile.openRead();
            if (fileInputStream == null) {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        AwareLog.e(TAG, "readFileLines stream catch IOException in finally!");
                    }
                }
                return result;
            } else if (fileInputStream.available() > 32768) {
                AwareLog.e(TAG, "readFileLines file size is more than 32K!");
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e2) {
                        AwareLog.e(TAG, "readFileLines stream catch IOException in finally!");
                    }
                }
                return result;
            } else {
                BufferedReader br2 = new BufferedReader(new InputStreamReader(fileInputStream, "utf-8"));
                while (true) {
                    try {
                        String tmp = br2.readLine();
                        if (tmp == null) {
                            break;
                        }
                        result.add(tmp);
                    } catch (FileNotFoundException e3) {
                        br = br2;
                        try {
                            AwareLog.e(TAG, "readFileLines file not exist: " + this.mAtomicFile);
                            if (br != null) {
                                try {
                                    br.close();
                                } catch (IOException e4) {
                                    AwareLog.e(TAG, "readFileLines br catch IOException in finally!");
                                }
                            }
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e5) {
                                    AwareLog.e(TAG, "readFileLines stream catch IOException in finally!");
                                }
                            }
                            AwareLog.d(TAG, "readFileLines result: " + result);
                            return result;
                        } catch (Throwable th2) {
                            th = th2;
                            if (br != null) {
                                try {
                                    br.close();
                                } catch (IOException e6) {
                                    AwareLog.e(TAG, "readFileLines br catch IOException in finally!");
                                }
                            }
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e7) {
                                    AwareLog.e(TAG, "readFileLines stream catch IOException in finally!");
                                }
                            }
                            throw th;
                        }
                    } catch (IOException e8) {
                        br = br2;
                        AwareLog.e(TAG, "readFileLines catch IOException!");
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException e9) {
                                AwareLog.e(TAG, "readFileLines br catch IOException in finally!");
                            }
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e10) {
                                AwareLog.e(TAG, "readFileLines stream catch IOException in finally!");
                            }
                        }
                        AwareLog.d(TAG, "readFileLines result: " + result);
                        return result;
                    } catch (Throwable th3) {
                        th = th3;
                        br = br2;
                        if (br != null) {
                        }
                        if (fileInputStream != null) {
                        }
                        throw th;
                    }
                }
                if (br2 != null) {
                    try {
                        br2.close();
                    } catch (IOException e11) {
                        AwareLog.e(TAG, "readFileLines br catch IOException in finally!");
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e12) {
                        AwareLog.e(TAG, "readFileLines stream catch IOException in finally!");
                    }
                }
                AwareLog.d(TAG, "readFileLines result: " + result);
                return result;
            }
        } catch (FileNotFoundException e13) {
        } catch (IOException e14) {
            AwareLog.e(TAG, "readFileLines catch IOException!");
            if (br != null) {
            }
            if (fileInputStream != null) {
            }
            AwareLog.d(TAG, "readFileLines result: " + result);
            return result;
        }
    }

    void writeFileLines(List<String> lines) {
        FileOutputStream fos = null;
        try {
            fos = this.mAtomicFile.startWrite();
            StringBuffer buf = new StringBuffer();
            for (String line : lines) {
                buf.append(line).append("\n");
            }
            fos.write(buf.toString().getBytes("utf-8"));
            this.mAtomicFile.finishWrite(fos);
        } catch (IOException ex) {
            AwareLog.e(TAG, "writeFileLines catch IOException: " + ex);
            this.mAtomicFile.failWrite(fos);
        }
    }
}

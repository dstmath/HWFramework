package com.android.server;

import android.common.HwFrameworkFactory;
import android.os.IPowerManager;
import android.os.IPowerManager.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.LogException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;

public class FingerprintUnlockDataCollector {
    static final int AUTHENTICATE_FAIL = 2;
    static final int AUTHENTICATE_NONE = 0;
    static final int AUTHENTICATE_SUCCESS = 1;
    private static boolean DEBUG = false;
    private static boolean DEBUG_FPLOG = false;
    static String ENROLL_LOG_FULL_PATH = "/data/log/fingerprint/fpc_enroll.json";
    private static final long SLEEP_TIME = 50;
    static String STATS_UNLOCK_FILE = "/data/log/fingerprint/fp_unlock";
    private static final String SYNC_NODE = "/sys/devices/platform/fingerprint/read_image_flag";
    private static final long SYNC_TIMEOUT = 2000;
    public static final String TAG = "FpDataCollector";
    static String UNLOCK_LOG_FULL_PATH = "/data/log/fingerprint/fpc_unlock.json";
    static int UPLOAD_HOUR = 24;
    static String UPLOAD_TAG = "fingerprint";
    static long UPLOAD_TIME_MILL_SEC = ((long) (((UPLOAD_HOUR * 60) * 60) * 1000));
    private static FingerprintUnlockDataCollector instance = null;
    private static boolean isUseImonitorUpload = false;
    static final Object mLock = new Object[0];
    private static LogException mLogException = HwFrameworkFactory.getLogException();
    private int isAuthenticated = 0;
    private boolean isScreenStateOn;
    private String mAuthenticatedTime;
    private String mAuthenticatedTimeToWrite;
    private String mCaptureCompletedTime;
    private String mCaptureCompletedTimeToWrite;
    private String mFingerDownTime;
    private String mFingerDownTimeToWrite;
    private long mLastUploadTime = 0;
    private Runnable mRunnable = new Runnable() {
        /* JADX WARNING: Removed duplicated region for block: B:54:0x0187  */
        /* JADX WARNING: Removed duplicated region for block: B:54:0x0187  */
        /* JADX WARNING: Removed duplicated region for block: B:134:0x02a7 A:{SYNTHETIC, Splitter: B:134:0x02a7} */
        /* JADX WARNING: Removed duplicated region for block: B:137:0x02ac A:{SYNTHETIC, Splitter: B:137:0x02ac} */
        /* JADX WARNING: Removed duplicated region for block: B:140:0x02b1 A:{SYNTHETIC, Splitter: B:140:0x02b1} */
        /* JADX WARNING: Removed duplicated region for block: B:143:0x02b6 A:{SYNTHETIC, Splitter: B:143:0x02b6} */
        /* JADX WARNING: Removed duplicated region for block: B:146:0x02bb A:{SYNTHETIC, Splitter: B:146:0x02bb} */
        /* JADX WARNING: Removed duplicated region for block: B:149:0x02c0 A:{SYNTHETIC, Splitter: B:149:0x02c0} */
        /* JADX WARNING: Removed duplicated region for block: B:35:0x0153 A:{SYNTHETIC, Splitter: B:35:0x0153} */
        /* JADX WARNING: Removed duplicated region for block: B:38:0x0158 A:{SYNTHETIC, Splitter: B:38:0x0158} */
        /* JADX WARNING: Removed duplicated region for block: B:41:0x015d A:{SYNTHETIC, Splitter: B:41:0x015d} */
        /* JADX WARNING: Removed duplicated region for block: B:44:0x0162 A:{SYNTHETIC, Splitter: B:44:0x0162} */
        /* JADX WARNING: Removed duplicated region for block: B:47:0x0167 A:{SYNTHETIC, Splitter: B:47:0x0167} */
        /* JADX WARNING: Removed duplicated region for block: B:50:0x016c A:{SYNTHETIC, Splitter: B:50:0x016c} */
        /* JADX WARNING: Removed duplicated region for block: B:54:0x0187  */
        /* JADX WARNING: Removed duplicated region for block: B:97:0x025b A:{SYNTHETIC, Splitter: B:97:0x025b} */
        /* JADX WARNING: Removed duplicated region for block: B:100:0x0260 A:{SYNTHETIC, Splitter: B:100:0x0260} */
        /* JADX WARNING: Removed duplicated region for block: B:103:0x0265 A:{SYNTHETIC, Splitter: B:103:0x0265} */
        /* JADX WARNING: Removed duplicated region for block: B:106:0x026a A:{SYNTHETIC, Splitter: B:106:0x026a} */
        /* JADX WARNING: Removed duplicated region for block: B:109:0x026f A:{SYNTHETIC, Splitter: B:109:0x026f} */
        /* JADX WARNING: Removed duplicated region for block: B:112:0x0274 A:{SYNTHETIC, Splitter: B:112:0x0274} */
        /* JADX WARNING: Removed duplicated region for block: B:54:0x0187  */
        /* JADX WARNING: Removed duplicated region for block: B:35:0x0153 A:{SYNTHETIC, Splitter: B:35:0x0153} */
        /* JADX WARNING: Removed duplicated region for block: B:38:0x0158 A:{SYNTHETIC, Splitter: B:38:0x0158} */
        /* JADX WARNING: Removed duplicated region for block: B:41:0x015d A:{SYNTHETIC, Splitter: B:41:0x015d} */
        /* JADX WARNING: Removed duplicated region for block: B:44:0x0162 A:{SYNTHETIC, Splitter: B:44:0x0162} */
        /* JADX WARNING: Removed duplicated region for block: B:47:0x0167 A:{SYNTHETIC, Splitter: B:47:0x0167} */
        /* JADX WARNING: Removed duplicated region for block: B:50:0x016c A:{SYNTHETIC, Splitter: B:50:0x016c} */
        /* JADX WARNING: Removed duplicated region for block: B:54:0x0187  */
        /* JADX WARNING: Removed duplicated region for block: B:97:0x025b A:{SYNTHETIC, Splitter: B:97:0x025b} */
        /* JADX WARNING: Removed duplicated region for block: B:100:0x0260 A:{SYNTHETIC, Splitter: B:100:0x0260} */
        /* JADX WARNING: Removed duplicated region for block: B:103:0x0265 A:{SYNTHETIC, Splitter: B:103:0x0265} */
        /* JADX WARNING: Removed duplicated region for block: B:106:0x026a A:{SYNTHETIC, Splitter: B:106:0x026a} */
        /* JADX WARNING: Removed duplicated region for block: B:109:0x026f A:{SYNTHETIC, Splitter: B:109:0x026f} */
        /* JADX WARNING: Removed duplicated region for block: B:112:0x0274 A:{SYNTHETIC, Splitter: B:112:0x0274} */
        /* JADX WARNING: Removed duplicated region for block: B:54:0x0187  */
        /* JADX WARNING: Removed duplicated region for block: B:134:0x02a7 A:{SYNTHETIC, Splitter: B:134:0x02a7} */
        /* JADX WARNING: Removed duplicated region for block: B:137:0x02ac A:{SYNTHETIC, Splitter: B:137:0x02ac} */
        /* JADX WARNING: Removed duplicated region for block: B:140:0x02b1 A:{SYNTHETIC, Splitter: B:140:0x02b1} */
        /* JADX WARNING: Removed duplicated region for block: B:143:0x02b6 A:{SYNTHETIC, Splitter: B:143:0x02b6} */
        /* JADX WARNING: Removed duplicated region for block: B:146:0x02bb A:{SYNTHETIC, Splitter: B:146:0x02bb} */
        /* JADX WARNING: Removed duplicated region for block: B:149:0x02c0 A:{SYNTHETIC, Splitter: B:149:0x02c0} */
        /* JADX WARNING: Removed duplicated region for block: B:97:0x025b A:{SYNTHETIC, Splitter: B:97:0x025b} */
        /* JADX WARNING: Removed duplicated region for block: B:100:0x0260 A:{SYNTHETIC, Splitter: B:100:0x0260} */
        /* JADX WARNING: Removed duplicated region for block: B:103:0x0265 A:{SYNTHETIC, Splitter: B:103:0x0265} */
        /* JADX WARNING: Removed duplicated region for block: B:106:0x026a A:{SYNTHETIC, Splitter: B:106:0x026a} */
        /* JADX WARNING: Removed duplicated region for block: B:109:0x026f A:{SYNTHETIC, Splitter: B:109:0x026f} */
        /* JADX WARNING: Removed duplicated region for block: B:112:0x0274 A:{SYNTHETIC, Splitter: B:112:0x0274} */
        /* JADX WARNING: Removed duplicated region for block: B:54:0x0187  */
        /* JADX WARNING: Removed duplicated region for block: B:134:0x02a7 A:{SYNTHETIC, Splitter: B:134:0x02a7} */
        /* JADX WARNING: Removed duplicated region for block: B:137:0x02ac A:{SYNTHETIC, Splitter: B:137:0x02ac} */
        /* JADX WARNING: Removed duplicated region for block: B:140:0x02b1 A:{SYNTHETIC, Splitter: B:140:0x02b1} */
        /* JADX WARNING: Removed duplicated region for block: B:143:0x02b6 A:{SYNTHETIC, Splitter: B:143:0x02b6} */
        /* JADX WARNING: Removed duplicated region for block: B:146:0x02bb A:{SYNTHETIC, Splitter: B:146:0x02bb} */
        /* JADX WARNING: Removed duplicated region for block: B:149:0x02c0 A:{SYNTHETIC, Splitter: B:149:0x02c0} */
        /* JADX WARNING: Removed duplicated region for block: B:35:0x0153 A:{SYNTHETIC, Splitter: B:35:0x0153} */
        /* JADX WARNING: Removed duplicated region for block: B:38:0x0158 A:{SYNTHETIC, Splitter: B:38:0x0158} */
        /* JADX WARNING: Removed duplicated region for block: B:41:0x015d A:{SYNTHETIC, Splitter: B:41:0x015d} */
        /* JADX WARNING: Removed duplicated region for block: B:44:0x0162 A:{SYNTHETIC, Splitter: B:44:0x0162} */
        /* JADX WARNING: Removed duplicated region for block: B:47:0x0167 A:{SYNTHETIC, Splitter: B:47:0x0167} */
        /* JADX WARNING: Removed duplicated region for block: B:50:0x016c A:{SYNTHETIC, Splitter: B:50:0x016c} */
        /* JADX WARNING: Removed duplicated region for block: B:54:0x0187  */
        /* JADX WARNING: Removed duplicated region for block: B:97:0x025b A:{SYNTHETIC, Splitter: B:97:0x025b} */
        /* JADX WARNING: Removed duplicated region for block: B:100:0x0260 A:{SYNTHETIC, Splitter: B:100:0x0260} */
        /* JADX WARNING: Removed duplicated region for block: B:103:0x0265 A:{SYNTHETIC, Splitter: B:103:0x0265} */
        /* JADX WARNING: Removed duplicated region for block: B:106:0x026a A:{SYNTHETIC, Splitter: B:106:0x026a} */
        /* JADX WARNING: Removed duplicated region for block: B:109:0x026f A:{SYNTHETIC, Splitter: B:109:0x026f} */
        /* JADX WARNING: Removed duplicated region for block: B:112:0x0274 A:{SYNTHETIC, Splitter: B:112:0x0274} */
        /* JADX WARNING: Removed duplicated region for block: B:54:0x0187  */
        /* JADX WARNING: Removed duplicated region for block: B:134:0x02a7 A:{SYNTHETIC, Splitter: B:134:0x02a7} */
        /* JADX WARNING: Removed duplicated region for block: B:137:0x02ac A:{SYNTHETIC, Splitter: B:137:0x02ac} */
        /* JADX WARNING: Removed duplicated region for block: B:140:0x02b1 A:{SYNTHETIC, Splitter: B:140:0x02b1} */
        /* JADX WARNING: Removed duplicated region for block: B:143:0x02b6 A:{SYNTHETIC, Splitter: B:143:0x02b6} */
        /* JADX WARNING: Removed duplicated region for block: B:146:0x02bb A:{SYNTHETIC, Splitter: B:146:0x02bb} */
        /* JADX WARNING: Removed duplicated region for block: B:149:0x02c0 A:{SYNTHETIC, Splitter: B:149:0x02c0} */
        /* JADX WARNING: Removed duplicated region for block: B:35:0x0153 A:{SYNTHETIC, Splitter: B:35:0x0153} */
        /* JADX WARNING: Removed duplicated region for block: B:38:0x0158 A:{SYNTHETIC, Splitter: B:38:0x0158} */
        /* JADX WARNING: Removed duplicated region for block: B:41:0x015d A:{SYNTHETIC, Splitter: B:41:0x015d} */
        /* JADX WARNING: Removed duplicated region for block: B:44:0x0162 A:{SYNTHETIC, Splitter: B:44:0x0162} */
        /* JADX WARNING: Removed duplicated region for block: B:47:0x0167 A:{SYNTHETIC, Splitter: B:47:0x0167} */
        /* JADX WARNING: Removed duplicated region for block: B:50:0x016c A:{SYNTHETIC, Splitter: B:50:0x016c} */
        /* JADX WARNING: Removed duplicated region for block: B:54:0x0187  */
        /* JADX WARNING: Removed duplicated region for block: B:97:0x025b A:{SYNTHETIC, Splitter: B:97:0x025b} */
        /* JADX WARNING: Removed duplicated region for block: B:100:0x0260 A:{SYNTHETIC, Splitter: B:100:0x0260} */
        /* JADX WARNING: Removed duplicated region for block: B:103:0x0265 A:{SYNTHETIC, Splitter: B:103:0x0265} */
        /* JADX WARNING: Removed duplicated region for block: B:106:0x026a A:{SYNTHETIC, Splitter: B:106:0x026a} */
        /* JADX WARNING: Removed duplicated region for block: B:109:0x026f A:{SYNTHETIC, Splitter: B:109:0x026f} */
        /* JADX WARNING: Removed duplicated region for block: B:112:0x0274 A:{SYNTHETIC, Splitter: B:112:0x0274} */
        /* JADX WARNING: Removed duplicated region for block: B:54:0x0187  */
        /* JADX WARNING: Removed duplicated region for block: B:134:0x02a7 A:{SYNTHETIC, Splitter: B:134:0x02a7} */
        /* JADX WARNING: Removed duplicated region for block: B:137:0x02ac A:{SYNTHETIC, Splitter: B:137:0x02ac} */
        /* JADX WARNING: Removed duplicated region for block: B:140:0x02b1 A:{SYNTHETIC, Splitter: B:140:0x02b1} */
        /* JADX WARNING: Removed duplicated region for block: B:143:0x02b6 A:{SYNTHETIC, Splitter: B:143:0x02b6} */
        /* JADX WARNING: Removed duplicated region for block: B:146:0x02bb A:{SYNTHETIC, Splitter: B:146:0x02bb} */
        /* JADX WARNING: Removed duplicated region for block: B:149:0x02c0 A:{SYNTHETIC, Splitter: B:149:0x02c0} */
        /* JADX WARNING: Removed duplicated region for block: B:35:0x0153 A:{SYNTHETIC, Splitter: B:35:0x0153} */
        /* JADX WARNING: Removed duplicated region for block: B:38:0x0158 A:{SYNTHETIC, Splitter: B:38:0x0158} */
        /* JADX WARNING: Removed duplicated region for block: B:41:0x015d A:{SYNTHETIC, Splitter: B:41:0x015d} */
        /* JADX WARNING: Removed duplicated region for block: B:44:0x0162 A:{SYNTHETIC, Splitter: B:44:0x0162} */
        /* JADX WARNING: Removed duplicated region for block: B:47:0x0167 A:{SYNTHETIC, Splitter: B:47:0x0167} */
        /* JADX WARNING: Removed duplicated region for block: B:50:0x016c A:{SYNTHETIC, Splitter: B:50:0x016c} */
        /* JADX WARNING: Removed duplicated region for block: B:54:0x0187  */
        /* JADX WARNING: Removed duplicated region for block: B:97:0x025b A:{SYNTHETIC, Splitter: B:97:0x025b} */
        /* JADX WARNING: Removed duplicated region for block: B:100:0x0260 A:{SYNTHETIC, Splitter: B:100:0x0260} */
        /* JADX WARNING: Removed duplicated region for block: B:103:0x0265 A:{SYNTHETIC, Splitter: B:103:0x0265} */
        /* JADX WARNING: Removed duplicated region for block: B:106:0x026a A:{SYNTHETIC, Splitter: B:106:0x026a} */
        /* JADX WARNING: Removed duplicated region for block: B:109:0x026f A:{SYNTHETIC, Splitter: B:109:0x026f} */
        /* JADX WARNING: Removed duplicated region for block: B:112:0x0274 A:{SYNTHETIC, Splitter: B:112:0x0274} */
        /* JADX WARNING: Removed duplicated region for block: B:54:0x0187  */
        /* JADX WARNING: Removed duplicated region for block: B:134:0x02a7 A:{SYNTHETIC, Splitter: B:134:0x02a7} */
        /* JADX WARNING: Removed duplicated region for block: B:137:0x02ac A:{SYNTHETIC, Splitter: B:137:0x02ac} */
        /* JADX WARNING: Removed duplicated region for block: B:140:0x02b1 A:{SYNTHETIC, Splitter: B:140:0x02b1} */
        /* JADX WARNING: Removed duplicated region for block: B:143:0x02b6 A:{SYNTHETIC, Splitter: B:143:0x02b6} */
        /* JADX WARNING: Removed duplicated region for block: B:146:0x02bb A:{SYNTHETIC, Splitter: B:146:0x02bb} */
        /* JADX WARNING: Removed duplicated region for block: B:149:0x02c0 A:{SYNTHETIC, Splitter: B:149:0x02c0} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            IOException e;
            Writer osw;
            OutputStream fos;
            Reader isr;
            Throwable th;
            StringIndexOutOfBoundsException e2;
            long currTime = System.currentTimeMillis();
            long tempTime = currTime;
            String syncNode = FingerprintUnlockDataCollector.this.readSyncNode();
            while (tempTime - currTime < FingerprintUnlockDataCollector.SYNC_TIMEOUT && "0".equals(syncNode)) {
                try {
                    Thread.sleep(FingerprintUnlockDataCollector.SLEEP_TIME);
                } catch (Exception e3) {
                    Log.e(FingerprintUnlockDataCollector.TAG, e3.toString());
                }
                tempTime = System.currentTimeMillis();
                syncNode = FingerprintUnlockDataCollector.this.readSyncNode();
            }
            BufferedReader br = null;
            BufferedWriter bw = null;
            FileInputStream fis = null;
            InputStreamReader isr2 = null;
            FileOutputStream fos2 = null;
            OutputStreamWriter osw2 = null;
            FingerprintUnlockDataCollector.isUseImonitorUpload = FingerprintUnlockDataCollector.this.checkUseImonitorUpload();
            if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                FingerprintUnlockDataCollector.UNLOCK_LOG_FULL_PATH = FingerprintUnlockDataCollector.STATS_UNLOCK_FILE;
            }
            try {
                File file = new File(FingerprintUnlockDataCollector.UNLOCK_LOG_FULL_PATH);
                if (file.exists()) {
                    String timeStr = "\n\t\"fingerprintDownTime\":\t\"" + FingerprintUnlockDataCollector.this.mFingerDownTimeToWrite + "\",\n" + "\t\"CaptureCompletedTime\":\t\"" + FingerprintUnlockDataCollector.this.mCaptureCompletedTimeToWrite + "\",\n" + "\t\"AuthenticatedTime\":\t\"" + FingerprintUnlockDataCollector.this.mAuthenticatedTimeToWrite + "\",\n" + "\t\"screenOnTime\":\t\"" + FingerprintUnlockDataCollector.this.mScreenOnTimeToWrite + "\"";
                    FileInputStream fis2 = new FileInputStream(file);
                    try {
                        Reader inputStreamReader = new InputStreamReader(fis2, "UTF-8");
                        try {
                            BufferedReader br2 = new BufferedReader(inputStreamReader);
                            try {
                                StringBuilder str = new StringBuilder();
                                String str2 = "";
                                while (true) {
                                    str2 = br2.readLine();
                                    if (str2 == null) {
                                        break;
                                    }
                                    str.append(str2);
                                    str.append("\n");
                                }
                                int index = str.lastIndexOf("}");
                                int index_start = str.lastIndexOf("{");
                                int index_done = 0;
                                if (index - index_start > 0) {
                                    index_done = str.indexOf("fingerprintDownTime", index_start);
                                }
                                if (index_done > 0 || index <= 1) {
                                    Log.e(FingerprintUnlockDataCollector.TAG, "timeStr has been written");
                                } else {
                                    str.insert(index - 1, ",");
                                    str.insert(index, timeStr);
                                    OutputStream fileOutputStream = new FileOutputStream(file);
                                    try {
                                        Writer outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
                                        try {
                                            BufferedWriter bw2 = new BufferedWriter(outputStreamWriter);
                                            try {
                                                bw2.write(str.toString());
                                                bw2.flush();
                                                osw2 = outputStreamWriter;
                                                fos2 = fileOutputStream;
                                                bw = bw2;
                                            } catch (IOException e4) {
                                                e = e4;
                                                osw = outputStreamWriter;
                                                fos = fileOutputStream;
                                                isr = inputStreamReader;
                                                fis = fis2;
                                                bw = bw2;
                                                br = br2;
                                                try {
                                                    Log.e(FingerprintUnlockDataCollector.TAG, e.toString());
                                                    if (br != null) {
                                                    }
                                                    if (bw != null) {
                                                    }
                                                    if (isr2 != null) {
                                                    }
                                                    if (fis != null) {
                                                    }
                                                    if (osw2 != null) {
                                                    }
                                                    if (fos2 != null) {
                                                    }
                                                    FingerprintUnlockDataCollector.this.writeSyncNode();
                                                    FingerprintUnlockDataCollector.this.resetTime();
                                                    if (!FingerprintUnlockDataCollector.isUseImonitorUpload) {
                                                    }
                                                } catch (Throwable th2) {
                                                    th = th2;
                                                    if (br != null) {
                                                        try {
                                                            br.close();
                                                        } catch (Exception e5) {
                                                        }
                                                    }
                                                    if (bw != null) {
                                                        try {
                                                            bw.close();
                                                        } catch (Exception e6) {
                                                        }
                                                    }
                                                    if (isr2 != null) {
                                                        try {
                                                            isr2.close();
                                                        } catch (Exception e7) {
                                                        }
                                                    }
                                                    if (fis != null) {
                                                        try {
                                                            fis.close();
                                                        } catch (Exception e8) {
                                                        }
                                                    }
                                                    if (osw2 != null) {
                                                        try {
                                                            osw2.close();
                                                        } catch (Exception e9) {
                                                        }
                                                    }
                                                    if (fos2 != null) {
                                                        try {
                                                            fos2.close();
                                                        } catch (Exception e10) {
                                                        }
                                                    }
                                                    throw th;
                                                }
                                            } catch (StringIndexOutOfBoundsException e11) {
                                                e2 = e11;
                                                osw = outputStreamWriter;
                                                fos = fileOutputStream;
                                                isr = inputStreamReader;
                                                fis = fis2;
                                                bw = bw2;
                                                br = br2;
                                                Log.e(FingerprintUnlockDataCollector.TAG, e2.toString() + "(fpc_unlock.json don't have a full json data!)");
                                                if (br != null) {
                                                }
                                                if (bw != null) {
                                                }
                                                if (isr2 != null) {
                                                }
                                                if (fis != null) {
                                                }
                                                if (osw2 != null) {
                                                }
                                                if (fos2 != null) {
                                                }
                                                FingerprintUnlockDataCollector.this.writeSyncNode();
                                                FingerprintUnlockDataCollector.this.resetTime();
                                                if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                                                }
                                            } catch (Throwable th3) {
                                                th = th3;
                                                osw = outputStreamWriter;
                                                fos = fileOutputStream;
                                                isr = inputStreamReader;
                                                fis = fis2;
                                                bw = bw2;
                                                br = br2;
                                                if (br != null) {
                                                }
                                                if (bw != null) {
                                                }
                                                if (isr2 != null) {
                                                }
                                                if (fis != null) {
                                                }
                                                if (osw2 != null) {
                                                }
                                                if (fos2 != null) {
                                                }
                                                throw th;
                                            }
                                        } catch (IOException e12) {
                                            e = e12;
                                            osw = outputStreamWriter;
                                            fos = fileOutputStream;
                                            isr = inputStreamReader;
                                            fis = fis2;
                                            br = br2;
                                            Log.e(FingerprintUnlockDataCollector.TAG, e.toString());
                                            if (br != null) {
                                            }
                                            if (bw != null) {
                                            }
                                            if (isr2 != null) {
                                            }
                                            if (fis != null) {
                                            }
                                            if (osw2 != null) {
                                            }
                                            if (fos2 != null) {
                                            }
                                            FingerprintUnlockDataCollector.this.writeSyncNode();
                                            FingerprintUnlockDataCollector.this.resetTime();
                                            if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                                            }
                                        } catch (StringIndexOutOfBoundsException e13) {
                                            e2 = e13;
                                            osw = outputStreamWriter;
                                            fos = fileOutputStream;
                                            isr = inputStreamReader;
                                            fis = fis2;
                                            br = br2;
                                            Log.e(FingerprintUnlockDataCollector.TAG, e2.toString() + "(fpc_unlock.json don't have a full json data!)");
                                            if (br != null) {
                                            }
                                            if (bw != null) {
                                            }
                                            if (isr2 != null) {
                                            }
                                            if (fis != null) {
                                            }
                                            if (osw2 != null) {
                                            }
                                            if (fos2 != null) {
                                            }
                                            FingerprintUnlockDataCollector.this.writeSyncNode();
                                            FingerprintUnlockDataCollector.this.resetTime();
                                            if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                                            }
                                        } catch (Throwable th4) {
                                            th = th4;
                                            osw = outputStreamWriter;
                                            fos = fileOutputStream;
                                            isr = inputStreamReader;
                                            fis = fis2;
                                            br = br2;
                                            if (br != null) {
                                            }
                                            if (bw != null) {
                                            }
                                            if (isr2 != null) {
                                            }
                                            if (fis != null) {
                                            }
                                            if (osw2 != null) {
                                            }
                                            if (fos2 != null) {
                                            }
                                            throw th;
                                        }
                                    } catch (IOException e14) {
                                        e = e14;
                                        fos = fileOutputStream;
                                        isr = inputStreamReader;
                                        fis = fis2;
                                        br = br2;
                                        Log.e(FingerprintUnlockDataCollector.TAG, e.toString());
                                        if (br != null) {
                                            try {
                                                br.close();
                                            } catch (Exception e15) {
                                            }
                                        }
                                        if (bw != null) {
                                            try {
                                                bw.close();
                                            } catch (Exception e16) {
                                            }
                                        }
                                        if (isr2 != null) {
                                            try {
                                                isr2.close();
                                            } catch (Exception e17) {
                                            }
                                        }
                                        if (fis != null) {
                                            try {
                                                fis.close();
                                            } catch (Exception e18) {
                                            }
                                        }
                                        if (osw2 != null) {
                                            try {
                                                osw2.close();
                                            } catch (Exception e19) {
                                            }
                                        }
                                        if (fos2 != null) {
                                            try {
                                                fos2.close();
                                            } catch (Exception e20) {
                                            }
                                        }
                                        FingerprintUnlockDataCollector.this.writeSyncNode();
                                        FingerprintUnlockDataCollector.this.resetTime();
                                        if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                                        }
                                    } catch (StringIndexOutOfBoundsException e21) {
                                        e2 = e21;
                                        fos = fileOutputStream;
                                        isr = inputStreamReader;
                                        fis = fis2;
                                        br = br2;
                                        Log.e(FingerprintUnlockDataCollector.TAG, e2.toString() + "(fpc_unlock.json don't have a full json data!)");
                                        if (br != null) {
                                            try {
                                                br.close();
                                            } catch (Exception e22) {
                                            }
                                        }
                                        if (bw != null) {
                                            try {
                                                bw.close();
                                            } catch (Exception e23) {
                                            }
                                        }
                                        if (isr2 != null) {
                                            try {
                                                isr2.close();
                                            } catch (Exception e24) {
                                            }
                                        }
                                        if (fis != null) {
                                            try {
                                                fis.close();
                                            } catch (Exception e25) {
                                            }
                                        }
                                        if (osw2 != null) {
                                            try {
                                                osw2.close();
                                            } catch (Exception e26) {
                                            }
                                        }
                                        if (fos2 != null) {
                                            try {
                                                fos2.close();
                                            } catch (Exception e27) {
                                            }
                                        }
                                        FingerprintUnlockDataCollector.this.writeSyncNode();
                                        FingerprintUnlockDataCollector.this.resetTime();
                                        if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                                        }
                                    } catch (Throwable th5) {
                                        th = th5;
                                        fos = fileOutputStream;
                                        isr = inputStreamReader;
                                        fis = fis2;
                                        br = br2;
                                        if (br != null) {
                                        }
                                        if (bw != null) {
                                        }
                                        if (isr2 != null) {
                                        }
                                        if (fis != null) {
                                        }
                                        if (osw2 != null) {
                                        }
                                        if (fos2 != null) {
                                        }
                                        throw th;
                                    }
                                }
                                if (br2 != null) {
                                    try {
                                        br2.close();
                                    } catch (Exception e28) {
                                    }
                                }
                                if (bw != null) {
                                    try {
                                        bw.close();
                                    } catch (Exception e29) {
                                    }
                                }
                                if (inputStreamReader != null) {
                                    try {
                                        inputStreamReader.close();
                                    } catch (Exception e30) {
                                    }
                                }
                                if (fis2 != null) {
                                    try {
                                        fis2.close();
                                    } catch (Exception e31) {
                                    }
                                }
                                if (osw2 != null) {
                                    try {
                                        osw2.close();
                                    } catch (Exception e32) {
                                    }
                                }
                                if (fos2 != null) {
                                    try {
                                        fos2.close();
                                    } catch (Exception e33) {
                                    }
                                }
                                isr = inputStreamReader;
                                fis = fis2;
                            } catch (IOException e34) {
                                e = e34;
                                isr2 = inputStreamReader;
                                fis = fis2;
                                br = br2;
                            } catch (StringIndexOutOfBoundsException e35) {
                                e2 = e35;
                                isr2 = inputStreamReader;
                                fis = fis2;
                                br = br2;
                                Log.e(FingerprintUnlockDataCollector.TAG, e2.toString() + "(fpc_unlock.json don't have a full json data!)");
                                if (br != null) {
                                }
                                if (bw != null) {
                                }
                                if (isr2 != null) {
                                }
                                if (fis != null) {
                                }
                                if (osw2 != null) {
                                }
                                if (fos2 != null) {
                                }
                                FingerprintUnlockDataCollector.this.writeSyncNode();
                                FingerprintUnlockDataCollector.this.resetTime();
                                if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                                }
                            } catch (Throwable th6) {
                                th = th6;
                                isr = inputStreamReader;
                                fis = fis2;
                                br = br2;
                                if (br != null) {
                                }
                                if (bw != null) {
                                }
                                if (isr2 != null) {
                                }
                                if (fis != null) {
                                }
                                if (osw2 != null) {
                                }
                                if (fos2 != null) {
                                }
                                throw th;
                            }
                        } catch (IOException e36) {
                            e = e36;
                            isr = inputStreamReader;
                            fis = fis2;
                            Log.e(FingerprintUnlockDataCollector.TAG, e.toString());
                            if (br != null) {
                            }
                            if (bw != null) {
                            }
                            if (isr2 != null) {
                            }
                            if (fis != null) {
                            }
                            if (osw2 != null) {
                            }
                            if (fos2 != null) {
                            }
                            FingerprintUnlockDataCollector.this.writeSyncNode();
                            FingerprintUnlockDataCollector.this.resetTime();
                            if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                            }
                        } catch (StringIndexOutOfBoundsException e37) {
                            e2 = e37;
                            isr = inputStreamReader;
                            fis = fis2;
                            Log.e(FingerprintUnlockDataCollector.TAG, e2.toString() + "(fpc_unlock.json don't have a full json data!)");
                            if (br != null) {
                            }
                            if (bw != null) {
                            }
                            if (isr2 != null) {
                            }
                            if (fis != null) {
                            }
                            if (osw2 != null) {
                            }
                            if (fos2 != null) {
                            }
                            FingerprintUnlockDataCollector.this.writeSyncNode();
                            FingerprintUnlockDataCollector.this.resetTime();
                            if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            isr = inputStreamReader;
                            fis = fis2;
                            if (br != null) {
                            }
                            if (bw != null) {
                            }
                            if (isr2 != null) {
                            }
                            if (fis != null) {
                            }
                            if (osw2 != null) {
                            }
                            if (fos2 != null) {
                            }
                            throw th;
                        }
                    } catch (IOException e38) {
                        e = e38;
                        fis = fis2;
                        Log.e(FingerprintUnlockDataCollector.TAG, e.toString());
                        if (br != null) {
                        }
                        if (bw != null) {
                        }
                        if (isr2 != null) {
                        }
                        if (fis != null) {
                        }
                        if (osw2 != null) {
                        }
                        if (fos2 != null) {
                        }
                        FingerprintUnlockDataCollector.this.writeSyncNode();
                        FingerprintUnlockDataCollector.this.resetTime();
                        if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                        }
                    } catch (StringIndexOutOfBoundsException e39) {
                        e2 = e39;
                        fis = fis2;
                        Log.e(FingerprintUnlockDataCollector.TAG, e2.toString() + "(fpc_unlock.json don't have a full json data!)");
                        if (br != null) {
                        }
                        if (bw != null) {
                        }
                        if (isr2 != null) {
                        }
                        if (fis != null) {
                        }
                        if (osw2 != null) {
                        }
                        if (fos2 != null) {
                        }
                        FingerprintUnlockDataCollector.this.writeSyncNode();
                        FingerprintUnlockDataCollector.this.resetTime();
                        if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        fis = fis2;
                        if (br != null) {
                        }
                        if (bw != null) {
                        }
                        if (isr2 != null) {
                        }
                        if (fis != null) {
                        }
                        if (osw2 != null) {
                        }
                        if (fos2 != null) {
                        }
                        throw th;
                    }
                    FingerprintUnlockDataCollector.this.writeSyncNode();
                    FingerprintUnlockDataCollector.this.resetTime();
                    if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                        FingerprintUnlockDataCollector.this.sendLog();
                    }
                }
                Log.e(FingerprintUnlockDataCollector.TAG, FingerprintUnlockDataCollector.UNLOCK_LOG_FULL_PATH + " doesn't exist!");
            } catch (IOException e40) {
                e = e40;
                Log.e(FingerprintUnlockDataCollector.TAG, e.toString());
                if (br != null) {
                }
                if (bw != null) {
                }
                if (isr2 != null) {
                }
                if (fis != null) {
                }
                if (osw2 != null) {
                }
                if (fos2 != null) {
                }
                FingerprintUnlockDataCollector.this.writeSyncNode();
                FingerprintUnlockDataCollector.this.resetTime();
                if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                }
            } catch (StringIndexOutOfBoundsException e41) {
                e2 = e41;
                Log.e(FingerprintUnlockDataCollector.TAG, e2.toString() + "(fpc_unlock.json don't have a full json data!)");
                if (br != null) {
                }
                if (bw != null) {
                }
                if (isr2 != null) {
                }
                if (fis != null) {
                }
                if (osw2 != null) {
                }
                if (fos2 != null) {
                }
                FingerprintUnlockDataCollector.this.writeSyncNode();
                FingerprintUnlockDataCollector.this.resetTime();
                if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                }
            }
        }
    };
    private boolean mScreenOnAuthenticated;
    private boolean mScreenOnCaptureCompleted;
    private boolean mScreenOnFingerDown;
    private String mScreenOnTime;
    private String mScreenOnTimeToWrite;

    static {
        boolean z = true;
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
        if (!DEBUG) {
            z = false;
        }
        DEBUG_FPLOG = z;
    }

    public static FingerprintUnlockDataCollector getInstance() {
        FingerprintUnlockDataCollector fingerprintUnlockDataCollector;
        if (DEBUG_FPLOG) {
            Log.d(TAG, "FingerprintUnlockDataCollector.getInstance()");
        }
        synchronized (mLock) {
            if (instance == null) {
                if (DEBUG_FPLOG) {
                    Log.d(TAG, "new intance in getInstance");
                }
                instance = new FingerprintUnlockDataCollector();
            }
            fingerprintUnlockDataCollector = instance;
        }
        return fingerprintUnlockDataCollector;
    }

    public void reportFingerDown() {
        if (DEBUG_FPLOG) {
            Log.d(TAG, "receive finger press down");
        }
        SimpleDateFormat sdfMicrosecond = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        long timeStamp = System.currentTimeMillis();
        boolean ScreenOnTmp = isScreenOn();
        synchronized (this) {
            this.mFingerDownTime = sdfMicrosecond.format(Long.valueOf(timeStamp));
            this.mScreenOnFingerDown = ScreenOnTmp;
            this.mScreenOnAuthenticated = false;
            this.mScreenOnCaptureCompleted = false;
            this.mScreenOnTime = null;
            this.mAuthenticatedTime = null;
            this.mCaptureCompletedTime = null;
        }
    }

    public void reportCaptureCompleted() {
        if (DEBUG_FPLOG) {
            Log.d(TAG, "fingerprint capture completed");
        }
        SimpleDateFormat sdfMicrosecond = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        long timeStamp = System.currentTimeMillis();
        boolean ScreenOnTmp = isScreenOn();
        synchronized (this) {
            this.mCaptureCompletedTime = sdfMicrosecond.format(Long.valueOf(timeStamp));
            this.mScreenOnCaptureCompleted = ScreenOnTmp;
            this.mScreenOnAuthenticated = false;
            this.mScreenOnTime = null;
            this.mAuthenticatedTime = null;
        }
    }

    public void reportFingerprintAuthenticated(boolean authenticated) {
        if (DEBUG_FPLOG) {
            Log.d(TAG, "fingerprint authenticated result:" + authenticated);
        }
        SimpleDateFormat sdfMicrosecond = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        long timeStamp = System.currentTimeMillis();
        boolean ScreenOnTmp = isScreenOn();
        synchronized (this) {
            this.isAuthenticated = authenticated ? 1 : 2;
            this.mAuthenticatedTime = sdfMicrosecond.format(Long.valueOf(timeStamp));
            this.mScreenOnAuthenticated = ScreenOnTmp;
            this.mScreenOnTime = null;
        }
        reportScreenTurnedOn();
    }

    public void reportScreenStateOn(String stateStr) {
        if (DEBUG_FPLOG) {
            Log.d(TAG, "DisplayPowerState :" + stateStr);
        }
        if ("ON".equals(stateStr)) {
            this.isScreenStateOn = true;
        } else {
            this.isScreenStateOn = false;
        }
    }

    public void reportScreenTurnedOn() {
        boolean isScreenStateOnCurr = isScreenOn();
        synchronized (this) {
            boolean mScreenOnInAuthenticating = (this.mScreenOnFingerDown || this.mScreenOnCaptureCompleted) ? true : this.mScreenOnAuthenticated;
            if (this.isAuthenticated == 0) {
                Log.d(TAG, "case xxx, not a fingerprint unlock ");
                return;
            }
            if (mScreenOnInAuthenticating) {
                if (isScreenStateOnCurr) {
                    if (this.isAuthenticated == 2) {
                        Log.d(TAG, "case 110, unlock fail during screen on");
                        this.mScreenOnTime = "null";
                    } else {
                        Log.d(TAG, "case 111, unlock succ during screen on");
                        this.mScreenOnTime = "null";
                    }
                } else if (this.isAuthenticated == 2) {
                    Log.d(TAG, "case 100, unlock fail and screen off by hand");
                    this.mScreenOnTime = "null";
                } else {
                    Log.d(TAG, "case 101, unlock succ but screen off by hand");
                    this.mScreenOnTime = "null";
                }
            } else if (isScreenStateOnCurr) {
                if (this.isAuthenticated == 2) {
                    Log.d(TAG, "case 010, screen on after unlock fail");
                    this.mScreenOnTime = "null";
                } else {
                    Log.d(TAG, "case 011, screen on after unlock succ");
                    this.mScreenOnTime = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(Long.valueOf(System.currentTimeMillis()));
                }
            } else if (this.isAuthenticated == 2) {
                Log.d(TAG, "case 000, black unlock fail");
                this.mScreenOnTime = "null";
            } else {
                Log.d(TAG, "case 001, wait for unlock screen on report");
                return;
            }
            this.isAuthenticated = 0;
            this.mFingerDownTimeToWrite = this.mFingerDownTime;
            this.mCaptureCompletedTimeToWrite = this.mCaptureCompletedTime;
            this.mAuthenticatedTimeToWrite = this.mAuthenticatedTime;
            this.mScreenOnTimeToWrite = this.mScreenOnTime;
            new Thread(this.mRunnable).start();
        }
    }

    private void resetTime() {
        synchronized (this) {
            this.mFingerDownTimeToWrite = null;
            this.mCaptureCompletedTimeToWrite = null;
            this.mAuthenticatedTimeToWrite = null;
            this.mScreenOnTimeToWrite = null;
        }
    }

    private void sendLog() {
        if (isTimeOverThreshold()) {
            String archiveMsg = "archive -i " + UNLOCK_LOG_FULL_PATH + " -i " + ENROLL_LOG_FULL_PATH + " -d " + UNLOCK_LOG_FULL_PATH + " -d " + ENROLL_LOG_FULL_PATH + " -o " + (new SimpleDateFormat("yyyyMMddHHmmss").format(Long.valueOf(System.currentTimeMillis())) + "_fingerprint") + " -z zip";
            if (DEBUG_FPLOG) {
                Log.d(TAG, archiveMsg);
            }
            if (mLogException != null) {
                mLogException.cmd(UPLOAD_TAG, archiveMsg);
            }
            this.mLastUploadTime = System.currentTimeMillis();
            return;
        }
        if (DEBUG_FPLOG) {
            Log.d(TAG, "time is not access threshold, don't send the log");
        }
    }

    private boolean isTimeOverThreshold() {
        if (System.currentTimeMillis() - this.mLastUploadTime >= UPLOAD_TIME_MILL_SEC) {
            return true;
        }
        return false;
    }

    private boolean isScreenOn() {
        try {
            IPowerManager power = Stub.asInterface(ServiceManager.getService("power"));
            if (power != null) {
                return power.isInteractive();
            }
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "can not connect to powermanagerservice");
            return true;
        }
    }

    private boolean checkUseImonitorUpload() {
        if (new File(STATS_UNLOCK_FILE).exists()) {
            return true;
        }
        Log.e(TAG, "STATS_UNLOCK_FILE doesn't exist!");
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:50:0x00ae A:{SYNTHETIC, Splitter: B:50:0x00ae} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00b3 A:{SYNTHETIC, Splitter: B:53:0x00b3} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00b8 A:{SYNTHETIC, Splitter: B:56:0x00b8} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0079 A:{SYNTHETIC, Splitter: B:34:0x0079} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x007e A:{SYNTHETIC, Splitter: B:37:0x007e} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0083 A:{SYNTHETIC, Splitter: B:40:0x0083} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00ae A:{SYNTHETIC, Splitter: B:50:0x00ae} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00b3 A:{SYNTHETIC, Splitter: B:53:0x00b3} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00b8 A:{SYNTHETIC, Splitter: B:56:0x00b8} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00ae A:{SYNTHETIC, Splitter: B:50:0x00ae} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00b3 A:{SYNTHETIC, Splitter: B:53:0x00b3} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00b8 A:{SYNTHETIC, Splitter: B:56:0x00b8} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0079 A:{SYNTHETIC, Splitter: B:34:0x0079} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x007e A:{SYNTHETIC, Splitter: B:37:0x007e} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0083 A:{SYNTHETIC, Splitter: B:40:0x0083} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String readSyncNode() {
        IOException e;
        Throwable th;
        BufferedReader br = null;
        FileInputStream fis = null;
        InputStreamReader isr = null;
        String res = null;
        try {
            File file = new File(SYNC_NODE);
            if (file.exists()) {
                FileInputStream fis2 = new FileInputStream(file);
                try {
                    InputStreamReader isr2 = new InputStreamReader(fis2, "UTF-8");
                    try {
                        BufferedReader br2 = new BufferedReader(isr2);
                        try {
                            res = br2.readLine();
                            if (br2 != null) {
                                try {
                                    br2.close();
                                } catch (IOException e2) {
                                    Log.e(TAG, e2.toString());
                                }
                            }
                            if (isr2 != null) {
                                try {
                                    isr2.close();
                                } catch (IOException e22) {
                                    Log.e(TAG, e22.toString());
                                }
                            }
                            if (fis2 != null) {
                                try {
                                    fis2.close();
                                } catch (IOException e222) {
                                    Log.e(TAG, e222.toString());
                                }
                            }
                            br = br2;
                        } catch (IOException e3) {
                            e222 = e3;
                            isr = isr2;
                            fis = fis2;
                            br = br2;
                        } catch (Throwable th2) {
                            th = th2;
                            isr = isr2;
                            fis = fis2;
                            br = br2;
                            if (br != null) {
                                try {
                                    br.close();
                                } catch (IOException e2222) {
                                    Log.e(TAG, e2222.toString());
                                }
                            }
                            if (isr != null) {
                                try {
                                    isr.close();
                                } catch (IOException e22222) {
                                    Log.e(TAG, e22222.toString());
                                }
                            }
                            if (fis != null) {
                                try {
                                    fis.close();
                                } catch (IOException e222222) {
                                    Log.e(TAG, e222222.toString());
                                }
                            }
                            throw th;
                        }
                    } catch (IOException e4) {
                        e222222 = e4;
                        isr = isr2;
                        fis = fis2;
                        try {
                            Log.e(TAG, e222222.toString());
                            if (br != null) {
                                try {
                                    br.close();
                                } catch (IOException e2222222) {
                                    Log.e(TAG, e2222222.toString());
                                }
                            }
                            if (isr != null) {
                                try {
                                    isr.close();
                                } catch (IOException e22222222) {
                                    Log.e(TAG, e22222222.toString());
                                }
                            }
                            if (fis != null) {
                                try {
                                    fis.close();
                                } catch (IOException e222222222) {
                                    Log.e(TAG, e222222222.toString());
                                }
                            }
                            return res;
                        } catch (Throwable th3) {
                            th = th3;
                            if (br != null) {
                            }
                            if (isr != null) {
                            }
                            if (fis != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        isr = isr2;
                        fis = fis2;
                        if (br != null) {
                        }
                        if (isr != null) {
                        }
                        if (fis != null) {
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    e222222222 = e5;
                    fis = fis2;
                    Log.e(TAG, e222222222.toString());
                    if (br != null) {
                    }
                    if (isr != null) {
                    }
                    if (fis != null) {
                    }
                    return res;
                } catch (Throwable th5) {
                    th = th5;
                    fis = fis2;
                    if (br != null) {
                    }
                    if (isr != null) {
                    }
                    if (fis != null) {
                    }
                    throw th;
                }
                return res;
            }
            Log.e(TAG, "sync operation node doesn't exist! just return null");
            return "";
        } catch (IOException e6) {
            e222222222 = e6;
            Log.e(TAG, e222222222.toString());
            if (br != null) {
            }
            if (isr != null) {
            }
            if (fis != null) {
            }
            return res;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x007a A:{SYNTHETIC, Splitter: B:34:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x007f A:{SYNTHETIC, Splitter: B:37:0x007f} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0084 A:{SYNTHETIC, Splitter: B:40:0x0084} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00af A:{SYNTHETIC, Splitter: B:50:0x00af} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00b4 A:{SYNTHETIC, Splitter: B:53:0x00b4} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00b9 A:{SYNTHETIC, Splitter: B:56:0x00b9} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x007a A:{SYNTHETIC, Splitter: B:34:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x007f A:{SYNTHETIC, Splitter: B:37:0x007f} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0084 A:{SYNTHETIC, Splitter: B:40:0x0084} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00af A:{SYNTHETIC, Splitter: B:50:0x00af} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00b4 A:{SYNTHETIC, Splitter: B:53:0x00b4} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00b9 A:{SYNTHETIC, Splitter: B:56:0x00b9} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00af A:{SYNTHETIC, Splitter: B:50:0x00af} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00b4 A:{SYNTHETIC, Splitter: B:53:0x00b4} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00b9 A:{SYNTHETIC, Splitter: B:56:0x00b9} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x007a A:{SYNTHETIC, Splitter: B:34:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x007f A:{SYNTHETIC, Splitter: B:37:0x007f} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0084 A:{SYNTHETIC, Splitter: B:40:0x0084} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeSyncNode() {
        IOException e;
        Throwable th;
        BufferedWriter bw = null;
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            File file = new File(SYNC_NODE);
            if (file.exists()) {
                FileOutputStream fos2 = new FileOutputStream(file);
                try {
                    OutputStreamWriter osw2 = new OutputStreamWriter(fos2, "UTF-8");
                    try {
                        BufferedWriter bw2 = new BufferedWriter(osw2);
                        try {
                            bw2.write("0");
                            bw2.flush();
                            if (bw2 != null) {
                                try {
                                    bw2.close();
                                } catch (IOException e2) {
                                    Log.e(TAG, e2.toString());
                                }
                            }
                            if (osw2 != null) {
                                try {
                                    osw2.close();
                                } catch (IOException e22) {
                                    Log.e(TAG, e22.toString());
                                }
                            }
                            if (fos2 != null) {
                                try {
                                    fos2.close();
                                } catch (IOException e222) {
                                    Log.e(TAG, e222.toString());
                                }
                            }
                            bw = bw2;
                        } catch (IOException e3) {
                            e222 = e3;
                            osw = osw2;
                            fos = fos2;
                            bw = bw2;
                            try {
                                Log.e(TAG, e222.toString());
                                if (bw != null) {
                                    try {
                                        bw.close();
                                    } catch (IOException e2222) {
                                        Log.e(TAG, e2222.toString());
                                    }
                                }
                                if (osw != null) {
                                    try {
                                        osw.close();
                                    } catch (IOException e22222) {
                                        Log.e(TAG, e22222.toString());
                                    }
                                }
                                if (fos != null) {
                                    try {
                                        fos.close();
                                    } catch (IOException e222222) {
                                        Log.e(TAG, e222222.toString());
                                    }
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                if (bw != null) {
                                }
                                if (osw != null) {
                                }
                                if (fos != null) {
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            osw = osw2;
                            fos = fos2;
                            bw = bw2;
                            if (bw != null) {
                                try {
                                    bw.close();
                                } catch (IOException e2222222) {
                                    Log.e(TAG, e2222222.toString());
                                }
                            }
                            if (osw != null) {
                                try {
                                    osw.close();
                                } catch (IOException e22222222) {
                                    Log.e(TAG, e22222222.toString());
                                }
                            }
                            if (fos != null) {
                                try {
                                    fos.close();
                                } catch (IOException e222222222) {
                                    Log.e(TAG, e222222222.toString());
                                }
                            }
                            throw th;
                        }
                    } catch (IOException e4) {
                        e222222222 = e4;
                        osw = osw2;
                        fos = fos2;
                        Log.e(TAG, e222222222.toString());
                        if (bw != null) {
                        }
                        if (osw != null) {
                        }
                        if (fos != null) {
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        osw = osw2;
                        fos = fos2;
                        if (bw != null) {
                        }
                        if (osw != null) {
                        }
                        if (fos != null) {
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    e222222222 = e5;
                    fos = fos2;
                    Log.e(TAG, e222222222.toString());
                    if (bw != null) {
                    }
                    if (osw != null) {
                    }
                    if (fos != null) {
                    }
                } catch (Throwable th5) {
                    th = th5;
                    fos = fos2;
                    if (bw != null) {
                    }
                    if (osw != null) {
                    }
                    if (fos != null) {
                    }
                    throw th;
                }
            }
            Log.e(TAG, "sync operation node doesn't exist! just return");
        } catch (IOException e6) {
            e222222222 = e6;
            Log.e(TAG, e222222222.toString());
            if (bw != null) {
            }
            if (osw != null) {
            }
            if (fos != null) {
            }
        }
    }
}

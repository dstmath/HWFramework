package com.android.server;

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
    static String ENROLL_LOG_FULL_PATH = null;
    private static final long SLEEP_TIME = 50;
    static String STATS_UNLOCK_FILE = null;
    private static final String SYNC_NODE = "/sys/devices/platform/fingerprint/read_image_flag";
    private static final long SYNC_TIMEOUT = 2000;
    public static final String TAG = "FpDataCollector";
    static String UNLOCK_LOG_FULL_PATH;
    static int UPLOAD_HOUR;
    static String UPLOAD_TAG;
    static long UPLOAD_TIME_MILL_SEC;
    private static FingerprintUnlockDataCollector instance;
    private static boolean isUseImonitorUpload;
    static final Object mLock = null;
    private static LogException mLogException;
    private int isAuthenticated;
    private boolean isScreenStateOn;
    private String mAuthenticatedTime;
    private String mAuthenticatedTimeToWrite;
    private String mCaptureCompletedTime;
    private String mCaptureCompletedTimeToWrite;
    private String mFingerDownTime;
    private String mFingerDownTimeToWrite;
    private long mLastUploadTime;
    private Runnable mRunnable;
    private boolean mScreenOnAuthenticated;
    private boolean mScreenOnCaptureCompleted;
    private boolean mScreenOnFingerDown;
    private String mScreenOnTime;
    private String mScreenOnTimeToWrite;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.FingerprintUnlockDataCollector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.FingerprintUnlockDataCollector.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.FingerprintUnlockDataCollector.<clinit>():void");
    }

    public FingerprintUnlockDataCollector() {
        this.mRunnable = new Runnable() {
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
                while (tempTime - currTime < FingerprintUnlockDataCollector.SYNC_TIMEOUT) {
                    if (!"0".equals(syncNode)) {
                        break;
                    }
                    try {
                        Thread.sleep(FingerprintUnlockDataCollector.SLEEP_TIME);
                    } catch (Exception e3) {
                        Log.e(FingerprintUnlockDataCollector.TAG, e3.toString());
                    }
                    tempTime = System.currentTimeMillis();
                    syncNode = FingerprintUnlockDataCollector.this.readSyncNode();
                }
                BufferedReader br = null;
                BufferedWriter bufferedWriter = null;
                FileInputStream fileInputStream = null;
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
                        FileInputStream fis = new FileInputStream(file);
                        try {
                            Reader inputStreamReader = new InputStreamReader(fis, "UTF-8");
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
                                    int index_done = FingerprintUnlockDataCollector.AUTHENTICATE_NONE;
                                    if (index - index_start > 0) {
                                        index_done = str.indexOf("fingerprintDownTime", index_start);
                                    }
                                    if (index_done > 0 || index <= FingerprintUnlockDataCollector.AUTHENTICATE_SUCCESS) {
                                        Log.e(FingerprintUnlockDataCollector.TAG, "timeStr has been written");
                                    } else {
                                        str.insert(index - 1, ",");
                                        str.insert(index, timeStr);
                                        OutputStream fileOutputStream = new FileOutputStream(file);
                                        try {
                                            Writer outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
                                            try {
                                                BufferedWriter bw = new BufferedWriter(outputStreamWriter);
                                                try {
                                                    bw.write(str.toString());
                                                    bw.flush();
                                                    osw2 = outputStreamWriter;
                                                    fos2 = fileOutputStream;
                                                    bufferedWriter = bw;
                                                } catch (IOException e4) {
                                                    e = e4;
                                                    osw = outputStreamWriter;
                                                    fos = fileOutputStream;
                                                    isr = inputStreamReader;
                                                    fileInputStream = fis;
                                                    bufferedWriter = bw;
                                                    br = br2;
                                                    try {
                                                        Log.e(FingerprintUnlockDataCollector.TAG, e.toString());
                                                        if (br != null) {
                                                            try {
                                                                br.close();
                                                            } catch (Exception e5) {
                                                            }
                                                        }
                                                        if (bufferedWriter != null) {
                                                            try {
                                                                bufferedWriter.close();
                                                            } catch (Exception e6) {
                                                            }
                                                        }
                                                        if (isr2 != null) {
                                                            try {
                                                                isr2.close();
                                                            } catch (Exception e7) {
                                                            }
                                                        }
                                                        if (fileInputStream != null) {
                                                            try {
                                                                fileInputStream.close();
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
                                                        FingerprintUnlockDataCollector.this.writeSyncNode();
                                                        FingerprintUnlockDataCollector.this.resetTime();
                                                        if (!FingerprintUnlockDataCollector.isUseImonitorUpload) {
                                                            FingerprintUnlockDataCollector.this.sendLog();
                                                        }
                                                    } catch (Throwable th2) {
                                                        th = th2;
                                                        if (br != null) {
                                                            try {
                                                                br.close();
                                                            } catch (Exception e11) {
                                                            }
                                                        }
                                                        if (bufferedWriter != null) {
                                                            try {
                                                                bufferedWriter.close();
                                                            } catch (Exception e12) {
                                                            }
                                                        }
                                                        if (isr2 != null) {
                                                            try {
                                                                isr2.close();
                                                            } catch (Exception e13) {
                                                            }
                                                        }
                                                        if (fileInputStream != null) {
                                                            try {
                                                                fileInputStream.close();
                                                            } catch (Exception e14) {
                                                            }
                                                        }
                                                        if (osw2 != null) {
                                                            try {
                                                                osw2.close();
                                                            } catch (Exception e15) {
                                                            }
                                                        }
                                                        if (fos2 != null) {
                                                            try {
                                                                fos2.close();
                                                            } catch (Exception e16) {
                                                            }
                                                        }
                                                        throw th;
                                                    }
                                                } catch (StringIndexOutOfBoundsException e17) {
                                                    e2 = e17;
                                                    osw = outputStreamWriter;
                                                    fos = fileOutputStream;
                                                    isr = inputStreamReader;
                                                    fileInputStream = fis;
                                                    bufferedWriter = bw;
                                                    br = br2;
                                                    Log.e(FingerprintUnlockDataCollector.TAG, e2.toString() + "(fpc_unlock.json don't have a full json data!)");
                                                    if (br != null) {
                                                        try {
                                                            br.close();
                                                        } catch (Exception e18) {
                                                        }
                                                    }
                                                    if (bufferedWriter != null) {
                                                        try {
                                                            bufferedWriter.close();
                                                        } catch (Exception e19) {
                                                        }
                                                    }
                                                    if (isr2 != null) {
                                                        try {
                                                            isr2.close();
                                                        } catch (Exception e20) {
                                                        }
                                                    }
                                                    if (fileInputStream != null) {
                                                        try {
                                                            fileInputStream.close();
                                                        } catch (Exception e21) {
                                                        }
                                                    }
                                                    if (osw2 != null) {
                                                        try {
                                                            osw2.close();
                                                        } catch (Exception e22) {
                                                        }
                                                    }
                                                    if (fos2 != null) {
                                                        try {
                                                            fos2.close();
                                                        } catch (Exception e23) {
                                                        }
                                                    }
                                                    FingerprintUnlockDataCollector.this.writeSyncNode();
                                                    FingerprintUnlockDataCollector.this.resetTime();
                                                    if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                                                        FingerprintUnlockDataCollector.this.sendLog();
                                                    }
                                                } catch (Throwable th3) {
                                                    th = th3;
                                                    osw = outputStreamWriter;
                                                    fos = fileOutputStream;
                                                    isr = inputStreamReader;
                                                    fileInputStream = fis;
                                                    bufferedWriter = bw;
                                                    br = br2;
                                                    if (br != null) {
                                                        br.close();
                                                    }
                                                    if (bufferedWriter != null) {
                                                        bufferedWriter.close();
                                                    }
                                                    if (isr2 != null) {
                                                        isr2.close();
                                                    }
                                                    if (fileInputStream != null) {
                                                        fileInputStream.close();
                                                    }
                                                    if (osw2 != null) {
                                                        osw2.close();
                                                    }
                                                    if (fos2 != null) {
                                                        fos2.close();
                                                    }
                                                    throw th;
                                                }
                                            } catch (IOException e24) {
                                                e = e24;
                                                osw = outputStreamWriter;
                                                fos = fileOutputStream;
                                                isr = inputStreamReader;
                                                fileInputStream = fis;
                                                br = br2;
                                                Log.e(FingerprintUnlockDataCollector.TAG, e.toString());
                                                if (br != null) {
                                                    br.close();
                                                }
                                                if (bufferedWriter != null) {
                                                    bufferedWriter.close();
                                                }
                                                if (isr2 != null) {
                                                    isr2.close();
                                                }
                                                if (fileInputStream != null) {
                                                    fileInputStream.close();
                                                }
                                                if (osw2 != null) {
                                                    osw2.close();
                                                }
                                                if (fos2 != null) {
                                                    fos2.close();
                                                }
                                                FingerprintUnlockDataCollector.this.writeSyncNode();
                                                FingerprintUnlockDataCollector.this.resetTime();
                                                if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                                                    FingerprintUnlockDataCollector.this.sendLog();
                                                }
                                            } catch (StringIndexOutOfBoundsException e25) {
                                                e2 = e25;
                                                osw = outputStreamWriter;
                                                fos = fileOutputStream;
                                                isr = inputStreamReader;
                                                fileInputStream = fis;
                                                br = br2;
                                                Log.e(FingerprintUnlockDataCollector.TAG, e2.toString() + "(fpc_unlock.json don't have a full json data!)");
                                                if (br != null) {
                                                    br.close();
                                                }
                                                if (bufferedWriter != null) {
                                                    bufferedWriter.close();
                                                }
                                                if (isr2 != null) {
                                                    isr2.close();
                                                }
                                                if (fileInputStream != null) {
                                                    fileInputStream.close();
                                                }
                                                if (osw2 != null) {
                                                    osw2.close();
                                                }
                                                if (fos2 != null) {
                                                    fos2.close();
                                                }
                                                FingerprintUnlockDataCollector.this.writeSyncNode();
                                                FingerprintUnlockDataCollector.this.resetTime();
                                                if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                                                    FingerprintUnlockDataCollector.this.sendLog();
                                                }
                                            } catch (Throwable th4) {
                                                th = th4;
                                                osw = outputStreamWriter;
                                                fos = fileOutputStream;
                                                isr = inputStreamReader;
                                                fileInputStream = fis;
                                                br = br2;
                                                if (br != null) {
                                                    br.close();
                                                }
                                                if (bufferedWriter != null) {
                                                    bufferedWriter.close();
                                                }
                                                if (isr2 != null) {
                                                    isr2.close();
                                                }
                                                if (fileInputStream != null) {
                                                    fileInputStream.close();
                                                }
                                                if (osw2 != null) {
                                                    osw2.close();
                                                }
                                                if (fos2 != null) {
                                                    fos2.close();
                                                }
                                                throw th;
                                            }
                                        } catch (IOException e26) {
                                            e = e26;
                                            fos = fileOutputStream;
                                            isr = inputStreamReader;
                                            fileInputStream = fis;
                                            br = br2;
                                            Log.e(FingerprintUnlockDataCollector.TAG, e.toString());
                                            if (br != null) {
                                                br.close();
                                            }
                                            if (bufferedWriter != null) {
                                                bufferedWriter.close();
                                            }
                                            if (isr2 != null) {
                                                isr2.close();
                                            }
                                            if (fileInputStream != null) {
                                                fileInputStream.close();
                                            }
                                            if (osw2 != null) {
                                                osw2.close();
                                            }
                                            if (fos2 != null) {
                                                fos2.close();
                                            }
                                            FingerprintUnlockDataCollector.this.writeSyncNode();
                                            FingerprintUnlockDataCollector.this.resetTime();
                                            if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                                                FingerprintUnlockDataCollector.this.sendLog();
                                            }
                                        } catch (StringIndexOutOfBoundsException e27) {
                                            e2 = e27;
                                            fos = fileOutputStream;
                                            isr = inputStreamReader;
                                            fileInputStream = fis;
                                            br = br2;
                                            Log.e(FingerprintUnlockDataCollector.TAG, e2.toString() + "(fpc_unlock.json don't have a full json data!)");
                                            if (br != null) {
                                                br.close();
                                            }
                                            if (bufferedWriter != null) {
                                                bufferedWriter.close();
                                            }
                                            if (isr2 != null) {
                                                isr2.close();
                                            }
                                            if (fileInputStream != null) {
                                                fileInputStream.close();
                                            }
                                            if (osw2 != null) {
                                                osw2.close();
                                            }
                                            if (fos2 != null) {
                                                fos2.close();
                                            }
                                            FingerprintUnlockDataCollector.this.writeSyncNode();
                                            FingerprintUnlockDataCollector.this.resetTime();
                                            if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                                                FingerprintUnlockDataCollector.this.sendLog();
                                            }
                                        } catch (Throwable th5) {
                                            th = th5;
                                            fos = fileOutputStream;
                                            isr = inputStreamReader;
                                            fileInputStream = fis;
                                            br = br2;
                                            if (br != null) {
                                                br.close();
                                            }
                                            if (bufferedWriter != null) {
                                                bufferedWriter.close();
                                            }
                                            if (isr2 != null) {
                                                isr2.close();
                                            }
                                            if (fileInputStream != null) {
                                                fileInputStream.close();
                                            }
                                            if (osw2 != null) {
                                                osw2.close();
                                            }
                                            if (fos2 != null) {
                                                fos2.close();
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
                                    if (bufferedWriter != null) {
                                        try {
                                            bufferedWriter.close();
                                        } catch (Exception e29) {
                                        }
                                    }
                                    if (inputStreamReader != null) {
                                        try {
                                            inputStreamReader.close();
                                        } catch (Exception e30) {
                                        }
                                    }
                                    if (fis != null) {
                                        try {
                                            fis.close();
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
                                    fileInputStream = fis;
                                } catch (IOException e34) {
                                    e = e34;
                                    isr2 = inputStreamReader;
                                    fileInputStream = fis;
                                    br = br2;
                                    Log.e(FingerprintUnlockDataCollector.TAG, e.toString());
                                    if (br != null) {
                                        br.close();
                                    }
                                    if (bufferedWriter != null) {
                                        bufferedWriter.close();
                                    }
                                    if (isr2 != null) {
                                        isr2.close();
                                    }
                                    if (fileInputStream != null) {
                                        fileInputStream.close();
                                    }
                                    if (osw2 != null) {
                                        osw2.close();
                                    }
                                    if (fos2 != null) {
                                        fos2.close();
                                    }
                                    FingerprintUnlockDataCollector.this.writeSyncNode();
                                    FingerprintUnlockDataCollector.this.resetTime();
                                    if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                                        FingerprintUnlockDataCollector.this.sendLog();
                                    }
                                } catch (StringIndexOutOfBoundsException e35) {
                                    e2 = e35;
                                    isr2 = inputStreamReader;
                                    fileInputStream = fis;
                                    br = br2;
                                    Log.e(FingerprintUnlockDataCollector.TAG, e2.toString() + "(fpc_unlock.json don't have a full json data!)");
                                    if (br != null) {
                                        br.close();
                                    }
                                    if (bufferedWriter != null) {
                                        bufferedWriter.close();
                                    }
                                    if (isr2 != null) {
                                        isr2.close();
                                    }
                                    if (fileInputStream != null) {
                                        fileInputStream.close();
                                    }
                                    if (osw2 != null) {
                                        osw2.close();
                                    }
                                    if (fos2 != null) {
                                        fos2.close();
                                    }
                                    FingerprintUnlockDataCollector.this.writeSyncNode();
                                    FingerprintUnlockDataCollector.this.resetTime();
                                    if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                                        FingerprintUnlockDataCollector.this.sendLog();
                                    }
                                } catch (Throwable th6) {
                                    th = th6;
                                    isr = inputStreamReader;
                                    fileInputStream = fis;
                                    br = br2;
                                    if (br != null) {
                                        br.close();
                                    }
                                    if (bufferedWriter != null) {
                                        bufferedWriter.close();
                                    }
                                    if (isr2 != null) {
                                        isr2.close();
                                    }
                                    if (fileInputStream != null) {
                                        fileInputStream.close();
                                    }
                                    if (osw2 != null) {
                                        osw2.close();
                                    }
                                    if (fos2 != null) {
                                        fos2.close();
                                    }
                                    throw th;
                                }
                            } catch (IOException e36) {
                                e = e36;
                                isr = inputStreamReader;
                                fileInputStream = fis;
                                Log.e(FingerprintUnlockDataCollector.TAG, e.toString());
                                if (br != null) {
                                    br.close();
                                }
                                if (bufferedWriter != null) {
                                    bufferedWriter.close();
                                }
                                if (isr2 != null) {
                                    isr2.close();
                                }
                                if (fileInputStream != null) {
                                    fileInputStream.close();
                                }
                                if (osw2 != null) {
                                    osw2.close();
                                }
                                if (fos2 != null) {
                                    fos2.close();
                                }
                                FingerprintUnlockDataCollector.this.writeSyncNode();
                                FingerprintUnlockDataCollector.this.resetTime();
                                if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                                    FingerprintUnlockDataCollector.this.sendLog();
                                }
                            } catch (StringIndexOutOfBoundsException e37) {
                                e2 = e37;
                                isr = inputStreamReader;
                                fileInputStream = fis;
                                Log.e(FingerprintUnlockDataCollector.TAG, e2.toString() + "(fpc_unlock.json don't have a full json data!)");
                                if (br != null) {
                                    br.close();
                                }
                                if (bufferedWriter != null) {
                                    bufferedWriter.close();
                                }
                                if (isr2 != null) {
                                    isr2.close();
                                }
                                if (fileInputStream != null) {
                                    fileInputStream.close();
                                }
                                if (osw2 != null) {
                                    osw2.close();
                                }
                                if (fos2 != null) {
                                    fos2.close();
                                }
                                FingerprintUnlockDataCollector.this.writeSyncNode();
                                FingerprintUnlockDataCollector.this.resetTime();
                                if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                                    FingerprintUnlockDataCollector.this.sendLog();
                                }
                            } catch (Throwable th7) {
                                th = th7;
                                isr = inputStreamReader;
                                fileInputStream = fis;
                                if (br != null) {
                                    br.close();
                                }
                                if (bufferedWriter != null) {
                                    bufferedWriter.close();
                                }
                                if (isr2 != null) {
                                    isr2.close();
                                }
                                if (fileInputStream != null) {
                                    fileInputStream.close();
                                }
                                if (osw2 != null) {
                                    osw2.close();
                                }
                                if (fos2 != null) {
                                    fos2.close();
                                }
                                throw th;
                            }
                        } catch (IOException e38) {
                            e = e38;
                            fileInputStream = fis;
                            Log.e(FingerprintUnlockDataCollector.TAG, e.toString());
                            if (br != null) {
                                br.close();
                            }
                            if (bufferedWriter != null) {
                                bufferedWriter.close();
                            }
                            if (isr2 != null) {
                                isr2.close();
                            }
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            if (osw2 != null) {
                                osw2.close();
                            }
                            if (fos2 != null) {
                                fos2.close();
                            }
                            FingerprintUnlockDataCollector.this.writeSyncNode();
                            FingerprintUnlockDataCollector.this.resetTime();
                            if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                                FingerprintUnlockDataCollector.this.sendLog();
                            }
                        } catch (StringIndexOutOfBoundsException e39) {
                            e2 = e39;
                            fileInputStream = fis;
                            Log.e(FingerprintUnlockDataCollector.TAG, e2.toString() + "(fpc_unlock.json don't have a full json data!)");
                            if (br != null) {
                                br.close();
                            }
                            if (bufferedWriter != null) {
                                bufferedWriter.close();
                            }
                            if (isr2 != null) {
                                isr2.close();
                            }
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            if (osw2 != null) {
                                osw2.close();
                            }
                            if (fos2 != null) {
                                fos2.close();
                            }
                            FingerprintUnlockDataCollector.this.writeSyncNode();
                            FingerprintUnlockDataCollector.this.resetTime();
                            if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                                FingerprintUnlockDataCollector.this.sendLog();
                            }
                        } catch (Throwable th8) {
                            th = th8;
                            fileInputStream = fis;
                            if (br != null) {
                                br.close();
                            }
                            if (bufferedWriter != null) {
                                bufferedWriter.close();
                            }
                            if (isr2 != null) {
                                isr2.close();
                            }
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            if (osw2 != null) {
                                osw2.close();
                            }
                            if (fos2 != null) {
                                fos2.close();
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
                        br.close();
                    }
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    }
                    if (isr2 != null) {
                        isr2.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (osw2 != null) {
                        osw2.close();
                    }
                    if (fos2 != null) {
                        fos2.close();
                    }
                    FingerprintUnlockDataCollector.this.writeSyncNode();
                    FingerprintUnlockDataCollector.this.resetTime();
                    if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                        FingerprintUnlockDataCollector.this.sendLog();
                    }
                } catch (StringIndexOutOfBoundsException e41) {
                    e2 = e41;
                    Log.e(FingerprintUnlockDataCollector.TAG, e2.toString() + "(fpc_unlock.json don't have a full json data!)");
                    if (br != null) {
                        br.close();
                    }
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    }
                    if (isr2 != null) {
                        isr2.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (osw2 != null) {
                        osw2.close();
                    }
                    if (fos2 != null) {
                        fos2.close();
                    }
                    FingerprintUnlockDataCollector.this.writeSyncNode();
                    FingerprintUnlockDataCollector.this.resetTime();
                    if (FingerprintUnlockDataCollector.isUseImonitorUpload) {
                        FingerprintUnlockDataCollector.this.sendLog();
                    }
                }
            }
        };
        this.mLastUploadTime = 0;
        this.isAuthenticated = AUTHENTICATE_NONE;
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
            this.isAuthenticated = authenticated ? AUTHENTICATE_SUCCESS : AUTHENTICATE_FAIL;
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
                    if (this.isAuthenticated == AUTHENTICATE_FAIL) {
                        Log.d(TAG, "case 110, unlock fail during screen on");
                        this.mScreenOnTime = "null";
                    } else {
                        Log.d(TAG, "case 111, unlock succ during screen on");
                        this.mScreenOnTime = "null";
                    }
                } else if (this.isAuthenticated == AUTHENTICATE_FAIL) {
                    Log.d(TAG, "case 100, unlock fail and screen off by hand");
                    this.mScreenOnTime = "null";
                } else {
                    Log.d(TAG, "case 101, unlock succ but screen off by hand");
                    this.mScreenOnTime = "null";
                }
            } else if (isScreenStateOnCurr) {
                if (this.isAuthenticated == AUTHENTICATE_FAIL) {
                    Log.d(TAG, "case 010, screen on after unlock fail");
                    this.mScreenOnTime = "null";
                } else {
                    Log.d(TAG, "case 011, screen on after unlock succ");
                    this.mScreenOnTime = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(Long.valueOf(System.currentTimeMillis()));
                }
            } else if (this.isAuthenticated == AUTHENTICATE_FAIL) {
                Log.d(TAG, "case 000, black unlock fail");
                this.mScreenOnTime = "null";
            } else {
                Log.d(TAG, "case 001, wait for unlock screen on report");
                return;
            }
            this.isAuthenticated = AUTHENTICATE_NONE;
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
        boolean isScreenOn = true;
        try {
            IPowerManager power = Stub.asInterface(ServiceManager.getService("power"));
            if (power != null) {
                isScreenOn = power.isInteractive();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "can not connect to powermanagerservice");
        }
        return isScreenOn;
    }

    private boolean checkUseImonitorUpload() {
        if (new File(STATS_UNLOCK_FILE).exists()) {
            return true;
        }
        Log.e(TAG, "STATS_UNLOCK_FILE doesn't exist!");
        return false;
    }

    private String readSyncNode() {
        IOException e;
        Throwable th;
        BufferedReader bufferedReader = null;
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        String res = null;
        try {
            File file = new File(SYNC_NODE);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                try {
                    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                    try {
                        BufferedReader br = new BufferedReader(isr);
                        try {
                            res = br.readLine();
                            if (br != null) {
                                try {
                                    br.close();
                                } catch (IOException e2) {
                                    Log.e(TAG, e2.toString());
                                }
                            }
                            if (isr != null) {
                                try {
                                    isr.close();
                                } catch (IOException e22) {
                                    Log.e(TAG, e22.toString());
                                }
                            }
                            if (fis != null) {
                                try {
                                    fis.close();
                                } catch (IOException e222) {
                                    Log.e(TAG, e222.toString());
                                }
                            }
                            bufferedReader = br;
                        } catch (IOException e3) {
                            e222 = e3;
                            inputStreamReader = isr;
                            fileInputStream = fis;
                            bufferedReader = br;
                            try {
                                Log.e(TAG, e222.toString());
                                if (bufferedReader != null) {
                                    try {
                                        bufferedReader.close();
                                    } catch (IOException e2222) {
                                        Log.e(TAG, e2222.toString());
                                    }
                                }
                                if (inputStreamReader != null) {
                                    try {
                                        inputStreamReader.close();
                                    } catch (IOException e22222) {
                                        Log.e(TAG, e22222.toString());
                                    }
                                }
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (IOException e222222) {
                                        Log.e(TAG, e222222.toString());
                                    }
                                }
                                return res;
                            } catch (Throwable th2) {
                                th = th2;
                                if (bufferedReader != null) {
                                    try {
                                        bufferedReader.close();
                                    } catch (IOException e2222222) {
                                        Log.e(TAG, e2222222.toString());
                                    }
                                }
                                if (inputStreamReader != null) {
                                    try {
                                        inputStreamReader.close();
                                    } catch (IOException e22222222) {
                                        Log.e(TAG, e22222222.toString());
                                    }
                                }
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (IOException e222222222) {
                                        Log.e(TAG, e222222222.toString());
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            inputStreamReader = isr;
                            fileInputStream = fis;
                            bufferedReader = br;
                            if (bufferedReader != null) {
                                bufferedReader.close();
                            }
                            if (inputStreamReader != null) {
                                inputStreamReader.close();
                            }
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            throw th;
                        }
                    } catch (IOException e4) {
                        e222222222 = e4;
                        inputStreamReader = isr;
                        fileInputStream = fis;
                        Log.e(TAG, e222222222.toString());
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                        if (inputStreamReader != null) {
                            inputStreamReader.close();
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        return res;
                    } catch (Throwable th4) {
                        th = th4;
                        inputStreamReader = isr;
                        fileInputStream = fis;
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                        if (inputStreamReader != null) {
                            inputStreamReader.close();
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    e222222222 = e5;
                    fileInputStream = fis;
                    Log.e(TAG, e222222222.toString());
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    return res;
                } catch (Throwable th5) {
                    th = th5;
                    fileInputStream = fis;
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
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
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return res;
        }
    }

    private void writeSyncNode() {
        IOException e;
        Throwable th;
        BufferedWriter bufferedWriter = null;
        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        try {
            File file = new File(SYNC_NODE);
            if (file.exists()) {
                FileOutputStream fos = new FileOutputStream(file);
                try {
                    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                    try {
                        BufferedWriter bw = new BufferedWriter(osw);
                        try {
                            bw.write("0");
                            bw.flush();
                            if (bw != null) {
                                try {
                                    bw.close();
                                } catch (IOException e2) {
                                    Log.e(TAG, e2.toString());
                                }
                            }
                            if (osw != null) {
                                try {
                                    osw.close();
                                } catch (IOException e22) {
                                    Log.e(TAG, e22.toString());
                                }
                            }
                            if (fos != null) {
                                try {
                                    fos.close();
                                } catch (IOException e222) {
                                    Log.e(TAG, e222.toString());
                                }
                            }
                            bufferedWriter = bw;
                        } catch (IOException e3) {
                            e222 = e3;
                            outputStreamWriter = osw;
                            fileOutputStream = fos;
                            bufferedWriter = bw;
                            try {
                                Log.e(TAG, e222.toString());
                                if (bufferedWriter != null) {
                                    try {
                                        bufferedWriter.close();
                                    } catch (IOException e2222) {
                                        Log.e(TAG, e2222.toString());
                                    }
                                }
                                if (outputStreamWriter != null) {
                                    try {
                                        outputStreamWriter.close();
                                    } catch (IOException e22222) {
                                        Log.e(TAG, e22222.toString());
                                    }
                                }
                                if (fileOutputStream != null) {
                                    try {
                                        fileOutputStream.close();
                                    } catch (IOException e222222) {
                                        Log.e(TAG, e222222.toString());
                                    }
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                if (bufferedWriter != null) {
                                    try {
                                        bufferedWriter.close();
                                    } catch (IOException e2222222) {
                                        Log.e(TAG, e2222222.toString());
                                    }
                                }
                                if (outputStreamWriter != null) {
                                    try {
                                        outputStreamWriter.close();
                                    } catch (IOException e22222222) {
                                        Log.e(TAG, e22222222.toString());
                                    }
                                }
                                if (fileOutputStream != null) {
                                    try {
                                        fileOutputStream.close();
                                    } catch (IOException e222222222) {
                                        Log.e(TAG, e222222222.toString());
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            outputStreamWriter = osw;
                            fileOutputStream = fos;
                            bufferedWriter = bw;
                            if (bufferedWriter != null) {
                                bufferedWriter.close();
                            }
                            if (outputStreamWriter != null) {
                                outputStreamWriter.close();
                            }
                            if (fileOutputStream != null) {
                                fileOutputStream.close();
                            }
                            throw th;
                        }
                    } catch (IOException e4) {
                        e222222222 = e4;
                        outputStreamWriter = osw;
                        fileOutputStream = fos;
                        Log.e(TAG, e222222222.toString());
                        if (bufferedWriter != null) {
                            bufferedWriter.close();
                        }
                        if (outputStreamWriter != null) {
                            outputStreamWriter.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        outputStreamWriter = osw;
                        fileOutputStream = fos;
                        if (bufferedWriter != null) {
                            bufferedWriter.close();
                        }
                        if (outputStreamWriter != null) {
                            outputStreamWriter.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    e222222222 = e5;
                    fileOutputStream = fos;
                    Log.e(TAG, e222222222.toString());
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    }
                    if (outputStreamWriter != null) {
                        outputStreamWriter.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (Throwable th5) {
                    th = th5;
                    fileOutputStream = fos;
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    }
                    if (outputStreamWriter != null) {
                        outputStreamWriter.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    throw th;
                }
            }
            Log.e(TAG, "sync operation node doesn't exist! just return");
        } catch (IOException e6) {
            e222222222 = e6;
            Log.e(TAG, e222222222.toString());
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }
}

package com.android.server.location;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import com.android.server.location.gnsschrlog.CSegEVENT_GPS_DAILY_CNT_REPORT;
import com.android.server.location.gnsschrlog.ChrLogBaseModel;
import com.android.server.location.gnsschrlog.GnssChrCommonInfo;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import com.android.server.location.gnsschrlog.GnssLogManager;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class GpsDailyReportEvent {
    private static final long COMM_UPLOAD_MIN_SPAN = 86400000;
    private static final int DAILY_REPORT = 2;
    private static final boolean DEBUG = false;
    private static final int GPS_DAILY_CNT_REPORT = 71;
    public static final int GPS_DAILY_CNT_REPORT_FAILD = 25;
    private static final String GPS_STATE_CNT_FILE = "/data/misc/gps/HwGpsStateCnt.txt";
    private static final String KEY_GPS_ERR_UPLOAD_CNT = "key_gps_err_upload_cnt:";
    private static final String KEY_GPS_REQ_CNT = "key_gps_req_cnt:";
    private static final String KEY_GPS_RESTART_CNT = "key_gps_restart_cnt:";
    private static final String KEY_GPS_RF_GOOD_STATUS = "key_gps_rf_good_status:";
    private static final String KEY_GPS_RF_VALIED_STATUS = "key_gps_rf_valied_status:";
    private static final String KEY_NETWORK_REQ_CNT = "key_network_req_cnt:";
    private static final String KEY_NETWORK_TIMEOUT_CNT = "key_network_timeout_cnt:";
    private static final String KEY_NTP_FLASH_SUCC_CNT = "key_ntp_flash_succ_cnt:";
    private static final String KEY_NTP_MOBILE_FAIL_CNT = "key_ntp_mobile_fail_cnt:";
    private static final String KEY_NTP_REQ_CNT = "key_ntp_req_cnt:";
    private static final String KEY_NTP_WIFI_FAIL_CNT = "key_ntp_wifi_fail_cnt:";
    private static final String KEY_TIMESTAMP = "key_timestamp:";
    private static final String KEY_XTRA_DLOAD_CNT = "key_xtra_dload_cnt:";
    private static final String KEY_XTRA_REQ_CNT = "key_xtra_req_cnt:";
    private static final int MAX_NUM_TRIGGER_BETA = 5;
    private static final int MAX_NUM_TRIGGER_COMM = 1;
    private static final long MIN_PERIOD_TRIGGER_BETA = 86400000;
    private static final long MIN_PERIOD_TRIGGER_COMM = 604800000;
    private static int MIN_WRITE_STAT_SPAN = 0;
    private static final String SEPARATOR_KEY = "\n";
    private static final String TAG = "HwGpsLog_DailyRptEvent";
    private static final int TRIGGER_NOW = 1;
    private static final boolean VERBOSE = false;
    private static final Object mLock = null;
    File logFile;
    private int mAgpsConnCnt;
    private int mAgpsConnFailedCnt;
    protected GnssChrCommonInfo mChrComInfo;
    private Context mContext;
    private int mGpsErrorUploadCnt;
    private int mGpsReqCnt;
    private boolean mIsCn0Good;
    private boolean mIsCn0Valied;
    private int mNetworkReqCnt;
    private int mNetworkTimeOutCnt;
    private int mNtpFlashSuccCnt;
    private int mNtpMobileFailCnt;
    private int mNtpReqCnt;
    private int mNtpWifiFailCnt;
    private long mTimestamp;
    private long mWriteStatTimestamp;
    private int mXtraDloadCnt;
    private int mXtraReqCnt;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.GpsDailyReportEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.GpsDailyReportEvent.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.GpsDailyReportEvent.<clinit>():void");
    }

    GpsDailyReportEvent(Context context) {
        this.logFile = new File(GPS_STATE_CNT_FILE);
        this.mWriteStatTimestamp = 0;
        this.mTimestamp = 0;
        this.mChrComInfo = new GnssChrCommonInfo();
        this.mXtraDloadCnt = 0;
        this.mXtraReqCnt = 0;
        this.mNtpWifiFailCnt = 0;
        this.mNtpMobileFailCnt = 0;
        this.mNtpReqCnt = 0;
        this.mNtpFlashSuccCnt = 0;
        this.mNetworkTimeOutCnt = 0;
        this.mNetworkReqCnt = 0;
        this.mGpsErrorUploadCnt = 0;
        this.mGpsReqCnt = 0;
        this.mAgpsConnFailedCnt = 0;
        this.mAgpsConnCnt = 0;
        this.mContext = context;
    }

    public void updateCn0Status(boolean isCn0Valied, boolean IsCn0Good) {
        this.mIsCn0Valied = isCn0Valied;
        this.mIsCn0Good = IsCn0Good;
        saveGpsDailyRptInfo(DEBUG, true);
    }

    public void updateGpsPosReqCnt(boolean success) {
        if (success) {
            this.mGpsReqCnt += TRIGGER_NOW;
        } else if (!success) {
            this.mGpsErrorUploadCnt += TRIGGER_NOW;
        }
        saveGpsDailyRptInfo(DEBUG, true);
    }

    public void updateXtraDownLoadCnt(boolean reqXtraCnt, boolean xtraSuccCnt) {
        if (reqXtraCnt) {
            this.mXtraDloadCnt += TRIGGER_NOW;
        }
        if (xtraSuccCnt) {
            this.mXtraReqCnt += TRIGGER_NOW;
        }
        saveGpsDailyRptInfo(DEBUG, true);
    }

    public void updateNtpDownLoadCnt(boolean reqNtpCnt, boolean NtpSuccCnt, boolean wifi, boolean datacall) {
        if (reqNtpCnt) {
            this.mNtpReqCnt += TRIGGER_NOW;
        }
        if (NtpSuccCnt) {
            this.mNtpFlashSuccCnt += TRIGGER_NOW;
        }
        if (wifi) {
            this.mNtpWifiFailCnt += TRIGGER_NOW;
        }
        if (datacall) {
            this.mNtpMobileFailCnt += TRIGGER_NOW;
        }
        saveGpsDailyRptInfo(DEBUG, true);
    }

    public void updateNetworkReqCnt(boolean networkFailState, boolean networkReqState) {
        if (networkFailState) {
            this.mNetworkTimeOutCnt += TRIGGER_NOW;
        }
        if (networkReqState) {
            this.mNetworkReqCnt += TRIGGER_NOW;
        }
        saveGpsDailyRptInfo(DEBUG, true);
    }

    public void updateAgpsReqCnt(boolean agpsConnFailCnt, boolean agpsReqCnt) {
        if (agpsConnFailCnt) {
            this.mAgpsConnFailedCnt += TRIGGER_NOW;
        }
        if (agpsReqCnt) {
            this.mAgpsConnCnt += TRIGGER_NOW;
        }
        saveGpsDailyRptInfo(DEBUG, true);
    }

    private void saveGpsDailyRptInfo(boolean flushNow, boolean triggerNow) {
        Throwable th;
        if (DEBUG) {
            Log.d(TAG, "saveGpsDailyRptInfo , flushNow is : " + flushNow + " ,triggerNow is : " + triggerNow);
        }
        if (!createLogFile()) {
            Log.e(TAG, "create file HwGpsStateCnt.txt filed");
        }
        synchronized (mLock) {
            long now = SystemClock.elapsedRealtime();
            if (flushNow || now - this.mWriteStatTimestamp >= ((long) MIN_WRITE_STAT_SPAN)) {
                this.mWriteStatTimestamp = now;
                if (0 == this.mTimestamp) {
                    this.mTimestamp = System.currentTimeMillis();
                }
                DataOutputStream dataOutputStream = null;
                try {
                    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(GPS_STATE_CNT_FILE)));
                    try {
                        out.writeUTF(KEY_TIMESTAMP + Long.toString(this.mTimestamp) + SEPARATOR_KEY);
                        out.writeUTF(KEY_XTRA_REQ_CNT + Integer.toString(this.mXtraReqCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_XTRA_DLOAD_CNT + Integer.toString(this.mXtraDloadCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_NTP_REQ_CNT + Integer.toString(this.mNtpReqCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_NTP_FLASH_SUCC_CNT + Integer.toString(this.mNtpFlashSuccCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_NTP_WIFI_FAIL_CNT + Integer.toString(this.mNtpWifiFailCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_NTP_MOBILE_FAIL_CNT + Integer.toString(this.mNtpMobileFailCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_NETWORK_TIMEOUT_CNT + Integer.toString(this.mNetworkTimeOutCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_NETWORK_REQ_CNT + Integer.toString(this.mNetworkReqCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_GPS_ERR_UPLOAD_CNT + Integer.toString(this.mGpsErrorUploadCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_GPS_REQ_CNT + Integer.toString(this.mGpsReqCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_GPS_RF_GOOD_STATUS + Boolean.toString(this.mIsCn0Good) + SEPARATOR_KEY);
                        out.writeUTF(KEY_GPS_RF_VALIED_STATUS + Boolean.toString(this.mIsCn0Valied) + SEPARATOR_KEY);
                        if (out != null) {
                            try {
                                out.close();
                            } catch (Exception e) {
                            }
                        }
                        dataOutputStream = out;
                    } catch (IOException e2) {
                        dataOutputStream = out;
                        try {
                            Log.e(TAG, "Error writing data file /data/misc/gps/HwGpsStateCnt.txt");
                            if (dataOutputStream != null) {
                                try {
                                    dataOutputStream.close();
                                } catch (Exception e3) {
                                }
                            }
                            if (triggerNow) {
                                triggerUploadIfNeed();
                            }
                            return;
                        } catch (Throwable th2) {
                            th = th2;
                            if (dataOutputStream != null) {
                                try {
                                    dataOutputStream.close();
                                } catch (Exception e4) {
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        dataOutputStream = out;
                        if (dataOutputStream != null) {
                            dataOutputStream.close();
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    Log.e(TAG, "Error writing data file /data/misc/gps/HwGpsStateCnt.txt");
                    if (dataOutputStream != null) {
                        dataOutputStream.close();
                    }
                    if (triggerNow) {
                        triggerUploadIfNeed();
                    }
                    return;
                }
                if (triggerNow) {
                    triggerUploadIfNeed();
                }
                return;
            }
        }
    }

    private boolean createLogFile() {
        try {
            File directory = new File("/data/misc/gps");
            if (!directory.exists()) {
                Log.e(TAG, "create dir sdcard/gps ,status: " + directory.mkdirs());
            }
            if (!this.logFile.exists()) {
                Log.d(TAG, "create /data/gps/HwGpsStateCnt.txt,status : " + this.logFile.createNewFile());
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "create /data/gps/HwGpsStateCnt.txt failed");
            return DEBUG;
        }
    }

    public void reloadGpsDailyRptInfo() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.android.server.location.GpsDailyReportEvent.reloadGpsDailyRptInfo():void. bs: [B:14:0x0057, B:26:0x0086]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:57)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r10 = this;
        r6 = "HwGpsLog_DailyRptEvent";
        r7 = "reloadGpsDailyRptInfo";
        android.util.Log.d(r6, r7);
        r2 = 0;
        r6 = r10.createLogFile();	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
        if (r6 != 0) goto L_0x0019;	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
    L_0x0010:
        r6 = "HwGpsLog_DailyRptEvent";	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
        r7 = "create file HwGpsStateCnt.txt filed";	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
        android.util.Log.e(r6, r7);	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
    L_0x0019:
        r3 = new java.io.DataInputStream;	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
        r6 = new java.io.BufferedInputStream;	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
        r7 = new java.io.FileInputStream;	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
        r8 = "/data/misc/gps/HwGpsStateCnt.txt";	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
        r7.<init>(r8);	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
        r6.<init>(r7);	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
        r3.<init>(r6);	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
    L_0x002b:
        r4 = r3.readUTF();	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "key_timestamp:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x0060;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x0038:
        r6 = "key_timestamp:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Long.parseLong(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mTimestamp = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;
    L_0x0053:
        r1 = move-exception;
        r2 = r3;
    L_0x0055:
        if (r2 == 0) goto L_0x005a;
    L_0x0057:
        r2.close();	 Catch:{ Exception -> 0x0261 }
    L_0x005a:
        if (r2 == 0) goto L_0x005f;
    L_0x005c:
        r2.close();	 Catch:{ Exception -> 0x0281 }
    L_0x005f:
        return;
    L_0x0060:
        r6 = "key_xtra_req_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x00c2;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x0069:
        r6 = "key_xtra_req_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Integer.parseInt(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mXtraReqCnt = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;
    L_0x0084:
        r0 = move-exception;
        r2 = r3;
    L_0x0086:
        r6 = "HwGpsLog_DailyRptEvent";	 Catch:{ all -> 0x027e }
        r7 = new java.lang.StringBuilder;	 Catch:{ all -> 0x027e }
        r7.<init>();	 Catch:{ all -> 0x027e }
        r8 = "readGPSCHRStat: No config file, revert to default";	 Catch:{ all -> 0x027e }
        r7 = r7.append(r8);	 Catch:{ all -> 0x027e }
        r7 = r7.append(r0);	 Catch:{ all -> 0x027e }
        r7 = r7.toString();	 Catch:{ all -> 0x027e }
        android.util.Log.e(r6, r7);	 Catch:{ all -> 0x027e }
        if (r2 == 0) goto L_0x005f;
    L_0x00a2:
        r2.close();	 Catch:{ Exception -> 0x00a6 }
        goto L_0x005f;
    L_0x00a6:
        r0 = move-exception;
        r6 = "HwGpsLog_DailyRptEvent";
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r8 = "readGPSCHRStat: Error closing file";
        r7 = r7.append(r8);
        r7 = r7.append(r0);
        r7 = r7.toString();
        android.util.Log.e(r6, r7);
        goto L_0x005f;
    L_0x00c2:
        r6 = "key_xtra_dload_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x00ef;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x00cb:
        r6 = "key_xtra_dload_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Integer.parseInt(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mXtraDloadCnt = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;
    L_0x00e7:
        r6 = move-exception;
        r2 = r3;
    L_0x00e9:
        if (r2 == 0) goto L_0x00ee;
    L_0x00eb:
        r2.close();	 Catch:{ Exception -> 0x029e }
    L_0x00ee:
        throw r6;
    L_0x00ef:
        r6 = "key_ntp_req_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x0114;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x00f8:
        r6 = "key_ntp_req_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Integer.parseInt(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mNtpReqCnt = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x0114:
        r6 = "key_ntp_flash_succ_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x0139;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x011d:
        r6 = "key_ntp_flash_succ_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Integer.parseInt(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mNtpFlashSuccCnt = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x0139:
        r6 = "key_ntp_wifi_fail_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x015e;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x0142:
        r6 = "key_ntp_wifi_fail_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Integer.parseInt(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mNtpWifiFailCnt = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x015e:
        r6 = "key_ntp_mobile_fail_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x0183;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x0167:
        r6 = "key_ntp_mobile_fail_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Integer.parseInt(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mNtpMobileFailCnt = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x0183:
        r6 = "key_network_timeout_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x01a8;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x018c:
        r6 = "key_network_timeout_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Integer.parseInt(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mNetworkTimeOutCnt = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x01a8:
        r6 = "key_network_req_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x01cd;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x01b1:
        r6 = "key_network_req_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Integer.parseInt(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mNetworkReqCnt = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x01cd:
        r6 = "key_gps_err_upload_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x01f2;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x01d6:
        r6 = "key_gps_err_upload_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Integer.parseInt(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mGpsErrorUploadCnt = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x01f2:
        r6 = "key_gps_req_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x0217;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x01fb:
        r6 = "key_gps_req_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Integer.parseInt(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mGpsReqCnt = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x0217:
        r6 = "key_gps_rf_good_status:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x023c;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x0220:
        r6 = "key_gps_rf_good_status:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Boolean.parseBoolean(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mIsCn0Good = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x023c:
        r6 = "key_gps_rf_valied_status:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x002b;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x0245:
        r6 = "key_gps_rf_valied_status:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Boolean.parseBoolean(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mIsCn0Valied = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;
    L_0x0261:
        r0 = move-exception;
        r6 = "HwGpsLog_DailyRptEvent";	 Catch:{ all -> 0x027e }
        r7 = new java.lang.StringBuilder;	 Catch:{ all -> 0x027e }
        r7.<init>();	 Catch:{ all -> 0x027e }
        r8 = "readGPSCHRStat: Error reading file:";	 Catch:{ all -> 0x027e }
        r7 = r7.append(r8);	 Catch:{ all -> 0x027e }
        r7 = r7.append(r0);	 Catch:{ all -> 0x027e }
        r7 = r7.toString();	 Catch:{ all -> 0x027e }
        android.util.Log.e(r6, r7);	 Catch:{ all -> 0x027e }
        goto L_0x005a;
    L_0x027e:
        r6 = move-exception;
        goto L_0x00e9;
    L_0x0281:
        r0 = move-exception;
        r6 = "HwGpsLog_DailyRptEvent";
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r8 = "readGPSCHRStat: Error closing file";
        r7 = r7.append(r8);
        r7 = r7.append(r0);
        r7 = r7.toString();
        android.util.Log.e(r6, r7);
        goto L_0x005f;
    L_0x029e:
        r0 = move-exception;
        r7 = "HwGpsLog_DailyRptEvent";
        r8 = new java.lang.StringBuilder;
        r8.<init>();
        r9 = "readGPSCHRStat: Error closing file";
        r8 = r8.append(r9);
        r8 = r8.append(r0);
        r8 = r8.toString();
        android.util.Log.e(r7, r8);
        goto L_0x00ee;
    L_0x02bb:
        r1 = move-exception;
        goto L_0x0055;
    L_0x02be:
        r0 = move-exception;
        goto L_0x0086;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.GpsDailyReportEvent.reloadGpsDailyRptInfo():void");
    }

    private void triggerUploadIfNeed() {
        if (DEBUG) {
            Log.d(TAG, "triggerUploadIfNeed ");
        }
        long now = System.currentTimeMillis();
        long minPeriod = GnssLogManager.getInstance().isCommercialUser() ? MIN_PERIOD_TRIGGER_COMM : MIN_PERIOD_TRIGGER_BETA;
        if (DEBUG) {
            Log.d(TAG, "minPeriod : " + minPeriod + " ,now : " + now + " ,mTimestamp : " + this.mTimestamp);
        }
        if (now - this.mTimestamp >= minPeriod && hasDataToTrigger()) {
            writeDailyRptToImonitor();
            writeDailyRptInfo(GPS_DAILY_CNT_REPORT);
            this.mTimestamp = now;
            clearDailyRptInfo();
        }
    }

    private boolean hasDataToTrigger() {
        if (((long) ((((this.mXtraReqCnt + this.mNtpReqCnt) + this.mNetworkReqCnt) + this.mGpsReqCnt) + this.mAgpsConnCnt)) > 0) {
            return true;
        }
        return DEBUG;
    }

    private void clearDailyRptInfo() {
        this.mXtraDloadCnt = 0;
        this.mXtraReqCnt = 0;
        this.mNtpReqCnt = 0;
        this.mNtpWifiFailCnt = 0;
        this.mNtpMobileFailCnt = 0;
        this.mNtpFlashSuccCnt = 0;
        this.mNetworkTimeOutCnt = 0;
        this.mNetworkReqCnt = 0;
        this.mGpsErrorUploadCnt = 0;
        this.mGpsReqCnt = 0;
        this.mIsCn0Good = DEBUG;
        this.mIsCn0Valied = DEBUG;
        saveGpsDailyRptInfo(true, DEBUG);
    }

    private void writeDailyRptInfo(int type) {
        Date date = new Date();
        ChrLogBaseModel cCSegEVENT_GPS_DAILY_CNT_REPORT = new CSegEVENT_GPS_DAILY_CNT_REPORT();
        cCSegEVENT_GPS_DAILY_CNT_REPORT.ucErrorCode.setValue((int) GPS_DAILY_CNT_REPORT_FAILD);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
        cCSegEVENT_GPS_DAILY_CNT_REPORT.tmTimeStamp.setValue(date);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.inetworktimeoutCnt.setValue(this.mNetworkTimeOutCnt);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.inetworkReqCnt.setValue(this.mNetworkReqCnt);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.igpserroruploadCnt.setValue(this.mGpsErrorUploadCnt);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.igpsreqCnt.setValue(this.mGpsReqCnt);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.iXtraDloadCnt.setValue(this.mXtraDloadCnt);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.iXtraReqCnt.setValue(this.mXtraReqCnt);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.iNtpFlashSuccCnt.setValue(this.mNtpFlashSuccCnt);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.iNtpWifiFailCnt.setValue(this.mNtpWifiFailCnt);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.iNtpMobileFailCnt.setValue(this.mNtpMobileFailCnt);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.iNtpReqCnt.setValue(this.mNtpReqCnt);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.strIsCn0Valied.setValue(Boolean.toString(this.mIsCn0Valied));
        cCSegEVENT_GPS_DAILY_CNT_REPORT.strIsCn0Good.setValue(Boolean.toString(this.mIsCn0Good));
        ChrLogBaseModel cChrLogBaseModel = cCSegEVENT_GPS_DAILY_CNT_REPORT;
        Log.d(TAG, "writeDailyRptInfo: " + type + "  ,ErrorCode:" + GPS_DAILY_CNT_REPORT_FAILD);
        GnssConnectivityLogManager.getInstance().reportAbnormalEventEx(cCSegEVENT_GPS_DAILY_CNT_REPORT, 14, TRIGGER_NOW, type, date, TRIGGER_NOW);
    }

    private void writeDailyRptToImonitor() {
        HwGnssDftManager hwGnssDftManager = new HwGnssDftManager(this.mContext);
        HwGnssDftGnssDailyParam mHwGnssDftGnssDailyParam = new HwGnssDftGnssDailyParam();
        mHwGnssDftGnssDailyParam.mDftGpsErrorUploadCnt = this.mGpsErrorUploadCnt;
        mHwGnssDftGnssDailyParam.mDftGpsRqCnt = this.mGpsReqCnt;
        mHwGnssDftGnssDailyParam.mDftNetworkTimeoutCnt = this.mNetworkTimeOutCnt;
        mHwGnssDftGnssDailyParam.mDftNetworkReqCnt = this.mNetworkReqCnt;
        hwGnssDftManager.sendDailyDataToImonitor(GPS_DAILY_CNT_REPORT, mHwGnssDftGnssDailyParam);
    }
}

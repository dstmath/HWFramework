package com.android.server.location;

import android.content.Context;
import android.util.Log;
import com.android.server.location.gnsschrlog.CSegEVENT_GNSS_DATA_COLLECT_EVENT;
import com.android.server.location.gnsschrlog.ChrLogBaseModel;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;

public class NtpIpCollector {
    private static final boolean DEBUG = false;
    private static final int MAX_FILE_SIZE = 1048576;
    private static final String NTP_IP_STORE_DIR = "/data/misc/gps";
    private static final String NTP_IP_STORE_DIR_FILE = "/data/misc/gps/ntp_ip.txt";
    private static final String TAG = "HwGnssLog_DataCollector_NtpIpCollector";
    private static final int TRIGGER_NOW = 1;
    private Context mContext;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.NtpIpCollector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.NtpIpCollector.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.NtpIpCollector.<clinit>():void");
    }

    public NtpIpCollector(Context context) {
        this.mContext = context;
    }

    public void uploadNtpServerIp(int event, int errorcode, String ip) {
        String data = AppHibernateCst.INVALID_PKG;
        String L = AppHibernateCst.INVALID_PKG;
        String R = AppHibernateCst.INVALID_PKG;
        int k = ip.length();
        for (int i = 0; i < ip.length(); i += TRIGGER_NOW) {
            if (ip.substring(i, i + TRIGGER_NOW).equals("/")) {
                L = ip.substring(0, i).trim();
                R = ip.substring(i + TRIGGER_NOW, k).trim();
            }
        }
        data = L + "," + R;
        if (DEBUG) {
            Log.d(TAG, "uploadNtpServerIp, DNS return : " + ip + " ,decode server is : " + L + " ,ip is : " + R);
        }
        if (!checkNtpIpRecorded(R)) {
            sendNtpInfoToChr(event, errorcode, L, R);
            writeFile(NTP_IP_STORE_DIR_FILE, data);
        }
    }

    private boolean checkNtpIpRecorded(String ip) {
        IOException e1;
        RuntimeException e2;
        Exception ex;
        Throwable th;
        File ntpfile = new File(NTP_IP_STORE_DIR_FILE);
        boolean IsContainsIp = DEBUG;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            if (ntpfile.isFile() && ntpfile.exists()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(ntpfile), "UTF-8");
                try {
                    BufferedReader bufferedReader2 = new BufferedReader(read);
                    String lineTxt;
                    do {
                        try {
                            lineTxt = bufferedReader2.readLine();
                            if (lineTxt == null) {
                                break;
                            }
                        } catch (IOException e) {
                            e1 = e;
                            bufferedReader = bufferedReader2;
                            inputStreamReader = read;
                        } catch (RuntimeException e3) {
                            e2 = e3;
                            bufferedReader = bufferedReader2;
                            inputStreamReader = read;
                        } catch (Exception e4) {
                            ex = e4;
                            bufferedReader = bufferedReader2;
                            inputStreamReader = read;
                        } catch (Throwable th2) {
                            th = th2;
                            bufferedReader = bufferedReader2;
                            inputStreamReader = read;
                        }
                    } while (lineTxt.indexOf(ip) == -1);
                    IsContainsIp = true;
                    bufferedReader2.close();
                    read.close();
                    bufferedReader = bufferedReader2;
                    inputStreamReader = read;
                } catch (IOException e5) {
                    e1 = e5;
                    inputStreamReader = read;
                    try {
                        e1.printStackTrace();
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException ioex) {
                                ioex.printStackTrace();
                            }
                        }
                        if (inputStreamReader != null) {
                            inputStreamReader.close();
                        }
                        Log.d(TAG, "IsContainsIp value is : " + IsContainsIp);
                        return IsContainsIp;
                    } catch (Throwable th3) {
                        th = th3;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException ioex2) {
                                ioex2.printStackTrace();
                                throw th;
                            }
                        }
                        if (inputStreamReader != null) {
                            inputStreamReader.close();
                        }
                        throw th;
                    }
                } catch (RuntimeException e6) {
                    e2 = e6;
                    inputStreamReader = read;
                    e2.printStackTrace();
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException ioex22) {
                            ioex22.printStackTrace();
                        }
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    Log.d(TAG, "IsContainsIp value is : " + IsContainsIp);
                    return IsContainsIp;
                } catch (Exception e7) {
                    ex = e7;
                    inputStreamReader = read;
                    Log.d(TAG, "read file failed!");
                    ex.printStackTrace();
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException ioex222) {
                            ioex222.printStackTrace();
                            return DEBUG;
                        }
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    return DEBUG;
                } catch (Throwable th4) {
                    th = th4;
                    inputStreamReader = read;
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    throw th;
                }
            }
            Log.d(TAG, "can not find file /data/misc/gps/ntp_ip.txt");
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ioex2222) {
                    ioex2222.printStackTrace();
                }
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
        } catch (IOException e8) {
            e1 = e8;
            e1.printStackTrace();
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            Log.d(TAG, "IsContainsIp value is : " + IsContainsIp);
            return IsContainsIp;
        } catch (RuntimeException e9) {
            e2 = e9;
            e2.printStackTrace();
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            Log.d(TAG, "IsContainsIp value is : " + IsContainsIp);
            return IsContainsIp;
        } catch (Exception e10) {
            ex = e10;
            Log.d(TAG, "read file failed!");
            ex.printStackTrace();
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            return DEBUG;
        }
        Log.d(TAG, "IsContainsIp value is : " + IsContainsIp);
        return IsContainsIp;
    }

    private boolean createLogFile() {
        try {
            File directory = new File(NTP_IP_STORE_DIR);
            File file = new File(NTP_IP_STORE_DIR_FILE);
            if (!directory.exists()) {
                Log.d(TAG, "create dir /data/misc/gps , return value is : " + directory.mkdirs());
            }
            if (!file.exists()) {
                Log.d(TAG, "create ntp_ip.txt,return val is : " + file.createNewFile());
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "create ntp_ip.txt failed");
            return DEBUG;
        }
    }

    private boolean checkFileSize() {
        File file = new File(NTP_IP_STORE_DIR_FILE);
        if (file.exists() || createLogFile()) {
            if (file.length() > MemoryConstant.MB_SIZE) {
                Log.d(TAG, "larger than 1M,delete file, return : " + file.delete());
            }
            if (createLogFile()) {
                return true;
            }
            Log.e(TAG, " file create failed ,return!");
            return DEBUG;
        }
        Log.e(TAG, " file create failed ,return!");
        return DEBUG;
    }

    private void writeFile(String fileName, String writestr) {
        IOException e1;
        RuntimeException e2;
        Exception ex;
        Throwable th;
        if (checkFileSize()) {
            BufferedWriter bufferedWriter = null;
            try {
                File file = new File(fileName);
                if (!file.exists()) {
                    Log.d(TAG, "Create the file:" + fileName);
                    if (!file.createNewFile()) {
                        Log.d(TAG, "Create file failed!");
                        return;
                    }
                }
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
                try {
                    writer.write(writestr);
                    writer.write("\r\n");
                    writer.flush();
                    writer.close();
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    bufferedWriter = writer;
                } catch (IOException e3) {
                    e1 = e3;
                    bufferedWriter = writer;
                    e1.printStackTrace();
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                    return;
                } catch (RuntimeException e5) {
                    e2 = e5;
                    bufferedWriter = writer;
                    e2.printStackTrace();
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e42) {
                            e42.printStackTrace();
                        }
                    }
                    return;
                } catch (Exception e6) {
                    ex = e6;
                    bufferedWriter = writer;
                    try {
                        ex.printStackTrace();
                        if (bufferedWriter != null) {
                            try {
                                bufferedWriter.close();
                            } catch (IOException e422) {
                                e422.printStackTrace();
                            }
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedWriter != null) {
                            try {
                                bufferedWriter.close();
                            } catch (IOException e4222) {
                                e4222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bufferedWriter = writer;
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    }
                    throw th;
                }
            } catch (IOException e7) {
                e1 = e7;
                e1.printStackTrace();
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                return;
            } catch (RuntimeException e8) {
                e2 = e8;
                e2.printStackTrace();
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                return;
            } catch (Exception e9) {
                ex = e9;
                ex.printStackTrace();
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                return;
            }
            return;
        }
        Log.e(TAG, "check filse size failed ,return!");
    }

    private void sendNtpInfoToChr(int event, int errorcode, String server, String ip) {
        Date date = new Date();
        HwGnssNetWorkStatus netWorkStatus = new HwGnssNetWorkStatus(this.mContext);
        netWorkStatus.triggerNetworkRelatedStatus();
        ChrLogBaseModel mCSegEVENT_GNSS_DATA_COLLECT_EVENT = new CSegEVENT_GNSS_DATA_COLLECT_EVENT();
        mCSegEVENT_GNSS_DATA_COLLECT_EVENT.strNtpServer.setValue(server);
        mCSegEVENT_GNSS_DATA_COLLECT_EVENT.strNtpIP.setValue(ip);
        mCSegEVENT_GNSS_DATA_COLLECT_EVENT.tmTimeStamp.setValue(date);
        mCSegEVENT_GNSS_DATA_COLLECT_EVENT.ucErrorCode.setValue(errorcode);
        mCSegEVENT_GNSS_DATA_COLLECT_EVENT.iCell_Mcc.setValue(netWorkStatus.getMcc());
        mCSegEVENT_GNSS_DATA_COLLECT_EVENT.iCell_Mnc.setValue(netWorkStatus.getMnc());
        mCSegEVENT_GNSS_DATA_COLLECT_EVENT.iCell_Lac.setValue(netWorkStatus.getLac());
        mCSegEVENT_GNSS_DATA_COLLECT_EVENT.usCell_SID.setValue(netWorkStatus.getCdmaSid());
        mCSegEVENT_GNSS_DATA_COLLECT_EVENT.usCell_NID.setValue(netWorkStatus.getCdmaNid());
        mCSegEVENT_GNSS_DATA_COLLECT_EVENT.strWifi_Bssid.setValue(netWorkStatus.getWifiBssid());
        mCSegEVENT_GNSS_DATA_COLLECT_EVENT.enNetworkStatus.setValue(netWorkStatus.getNetworkType());
        ChrLogBaseModel cChrLogBaseModel = mCSegEVENT_GNSS_DATA_COLLECT_EVENT;
        Log.d(TAG, "sendNtpInfoToChr, event : " + event + "  ,ErrorCode : " + errorcode);
        GnssConnectivityLogManager.getInstance().reportAbnormalEventEx(mCSegEVENT_GNSS_DATA_COLLECT_EVENT, 14, TRIGGER_NOW, event, date, TRIGGER_NOW);
    }
}

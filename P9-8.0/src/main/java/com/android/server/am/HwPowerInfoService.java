package com.android.server.am;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Environment;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.os.ProcessCpuTracker;
import com.android.server.emcom.SmartcareConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class HwPowerInfoService implements IHwPowerInfoService {
    private static String CONFIG_FILE = "PowerInfoImpl.xml";
    private static final int MAX_CPU_INDEX = 8;
    private static String PRODUCT_ETC_DIR = "/product/etc/";
    private static String SYSTEM_ETC_DIR = "/system/etc/";
    private static final boolean debugOn = true;
    private static Context mContext = null;
    private static final Object mLock = new Object();
    private static final Object mLockInit = new Object();
    private static final Object mLock_wakelock = new Object();
    private static LogInfo mLogInfo;
    private static PowerInfoServiceReceiver mReceiver = null;
    private static HwPowerInfoService mSingleInstance = null;
    private static int musicValume = 0;
    private String BAT_TEMP;
    private String BIGZIPFILEPATH;
    private String BOARD_TEMP;
    private String CPU0_TEMP;
    private String CPU1_TEMP;
    private String CPU_FREQ_HEAD;
    private String CPU_FREQ_TAIL;
    private String CPU_MAX_FREQ_TAIL;
    private String CPU_ONLINE;
    private String CURRENT;
    private String CURRENT_LIMIT;
    private String PA_TEMP;
    private String SOC_RM;
    private String TAG;
    private int TOP_PROCESS_NUM;
    private int WAKELOCK_NUM;
    private String WAKEUPSOURCE;
    private int WAKEUPSOURCE_NUM;
    private SimpleDateFormat dateFormate;
    private int hasCreateBigZip;
    private boolean isThreadAlive;
    private String logFileName;
    private ProcessCpuTracker mCpuTracker;
    private String mLastTopAppName;
    private int mLastTopAppUid;
    private boolean mSuspendState;
    private int mWakeLockNumber;
    private WorkerThread mWorker;
    private int timestep;
    private int zipEndHour;
    private int zipFileMax;
    private int zipStartHour;

    static class PowerInfoServiceReceiver extends BroadcastReceiver {
        PowerInfoServiceReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.HEADSET_PLUG")) {
                int state = intent.getIntExtra("state", 0);
                if (state == 1) {
                    HwPowerInfoService.mLogInfo.mHeadSet = 1;
                } else if (state == 0) {
                    HwPowerInfoService.mLogInfo.mHeadSet = 0;
                }
            } else if (action.equals("android.nfc.action.ADAPTER_STATE_CHANGED")) {
                int NFCState = intent.getIntExtra("android.nfc.extra.ADAPTER_STATE", 1);
                if (NFCState == 3 || NFCState == 2) {
                    HwPowerInfoService.mLogInfo.mNFCOn = 1;
                } else {
                    HwPowerInfoService.mLogInfo.mNFCOn = 0;
                }
            } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                int wifi_state = intent.getIntExtra("wifi_state", 1);
                if (wifi_state == 3 || wifi_state == 2) {
                    HwPowerInfoService.mLogInfo.mWifiStatus = 1;
                } else if (wifi_state == 1 || wifi_state == 0) {
                    HwPowerInfoService.mLogInfo.mWifiStatus = 0;
                }
            } else if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                int wifi_ap_state = intent.getIntExtra("wifi_state", 11);
                if (wifi_ap_state == 13 || wifi_ap_state == 12) {
                    HwPowerInfoService.mLogInfo.mWifiStatus = 3;
                } else if (wifi_ap_state == 11 || wifi_ap_state == 10) {
                    HwPowerInfoService.mLogInfo.mWifiStatus = 0;
                }
            } else if (action.equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
                int bluetooth_state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE);
                if (bluetooth_state == 12 || bluetooth_state == 11) {
                    HwPowerInfoService.mLogInfo.mBTState = 1;
                } else if (bluetooth_state == 10 || bluetooth_state == 13) {
                    HwPowerInfoService.mLogInfo.mBTState = 0;
                }
            } else if (action.equals("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED")) {
                int bluetooth_connect = intent.getIntExtra("android.bluetooth.adapter.extra.CONNECTION_STATE", Integer.MIN_VALUE);
                if (bluetooth_connect == 2) {
                    HwPowerInfoService.mLogInfo.mBTState = 2;
                } else if (bluetooth_connect == 0) {
                    HwPowerInfoService.mLogInfo.mBTState = 1;
                }
            } else if (action.equals("android.media.VOLUME_CHANGED_ACTION") && intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1) == 3) {
                HwPowerInfoService.musicValume = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1);
            }
        }
    }

    private class WorkerThread extends Thread {
        public WorkerThread(String name) {
            super(name);
        }

        public void run() {
            synchronized (HwPowerInfoService.mLock) {
                while (HwPowerInfoService.this.isThreadAlive) {
                    try {
                        HwPowerInfoService.mLock.wait((long) HwPowerInfoService.this.timestep);
                        if (SystemProperties.getBoolean("persist.sys.huawei.debug.on", false)) {
                            HwPowerInfoService.this.enter();
                            HwPowerInfoService.mLogInfo.mAlarmName = null;
                            HwPowerInfoService.mLogInfo.mWakeupReason = null;
                        }
                    } catch (InterruptedException e) {
                        Log.i(HwPowerInfoService.this.TAG, "InterruptedException error");
                    }
                }
            }
            return;
        }
    }

    private void parseNode(Node node) {
        if (node != null) {
            NodeList nodeList = node.getChildNodes();
            int size = nodeList.getLength();
            for (int i = 0; i < size; i++) {
                Node child = nodeList.item(i);
                if (child instanceof Element) {
                    String childName = child.getNodeName();
                    String text = child.getFirstChild().getNodeValue();
                    if (childName.equals("CPU_freq")) {
                        this.CPU_FREQ_HEAD = text;
                    } else if (childName.equals("CPU0_temp")) {
                        this.CPU0_TEMP = text;
                    } else if (childName.equals("CPU1_temp")) {
                        this.CPU1_TEMP = text;
                    } else if (childName.equals("Board_temp")) {
                        this.BOARD_TEMP = text;
                    } else if (childName.equals("PA_temp")) {
                        this.PA_TEMP = text;
                    } else if (childName.equals("Battery_temp")) {
                        this.BAT_TEMP = text;
                    } else if (childName.equals("CPU_online")) {
                        this.CPU_ONLINE = text;
                    } else if (childName.equals("WakeupSource")) {
                        this.WAKEUPSOURCE = text;
                    } else if (childName.equals("Current")) {
                        this.CURRENT = text;
                    } else if (childName.equals("Current_limit")) {
                        this.CURRENT_LIMIT = text;
                    } else if (childName.equals("Capacity_rm")) {
                        this.SOC_RM = text;
                    } else if (childName.equals("TimeStep")) {
                        this.timestep = Integer.parseInt(text);
                    } else if (childName.equals("ZipFileMax")) {
                        this.zipFileMax = Integer.parseInt(text);
                    } else if (childName.equals("Log_path")) {
                        this.BIGZIPFILEPATH = text;
                    } else if (childName.equals("Log_name")) {
                        this.logFileName = text;
                    } else if (childName.equals("WakeLock_num")) {
                        this.WAKELOCK_NUM = text == null ? 3 : Integer.parseInt(text);
                    } else if (childName.equals("WakeupSource_num")) {
                        this.WAKEUPSOURCE_NUM = text == null ? 3 : Integer.parseInt(text);
                    } else if (childName.equals("Top_process_num")) {
                        this.TOP_PROCESS_NUM = text == null ? 3 : Integer.parseInt(text);
                    }
                }
            }
        }
    }

    private void parseXML() {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            File file = new File(PRODUCT_ETC_DIR + CONFIG_FILE);
            File file2;
            try {
                if (file.exists()) {
                    file2 = file;
                } else {
                    Log.i(this.TAG, PRODUCT_ETC_DIR + CONFIG_FILE + "is not exists");
                    file2 = new File(SYSTEM_ETC_DIR + CONFIG_FILE);
                    if (!file2.exists()) {
                        Log.i(this.TAG, SYSTEM_ETC_DIR + CONFIG_FILE + "is not exists");
                        this.isThreadAlive = false;
                        return;
                    }
                }
                if (builder != null) {
                    NodeList nodeList = builder.parse(file2).getDocumentElement().getChildNodes();
                    int size = nodeList.getLength();
                    for (int i = 0; i < size; i++) {
                        Node child = nodeList.item(i);
                        if (child instanceof Element) {
                            parseNode(child);
                        }
                    }
                }
            } catch (ParserConfigurationException e) {
                file2 = file;
                Log.i(this.TAG, "ParserConfigurationException error");
            } catch (SAXException e2) {
                file2 = file;
                Log.i(this.TAG, "SAXException error");
            } catch (IOException e3) {
                file2 = file;
                Log.i(this.TAG, "IOException error");
            }
        } catch (ParserConfigurationException e4) {
            Log.i(this.TAG, "ParserConfigurationException error");
        } catch (SAXException e5) {
            Log.i(this.TAG, "SAXException error");
        } catch (IOException e6) {
            Log.i(this.TAG, "IOException error");
        }
    }

    private void enter() {
        Calendar now = Calendar.getInstance();
        int hours = now.get(11);
        if (hours < this.zipStartHour || hours >= this.zipEndHour || this.hasCreateBigZip != 0) {
            if (hours >= this.zipEndHour) {
                this.hasCreateBigZip = 0;
            }
            beginWriteFile();
            return;
        }
        long nowTime = now.getTimeInMillis();
        Log.i(this.TAG, "zipFile");
        try {
            beginWriteFile();
            zipFile(nowTime);
        } catch (Exception e) {
            Log.i(this.TAG, "failed to zipFile");
        }
    }

    private String writeWakeLockToFile(LogInfo Data) {
        StringBuffer result = new StringBuffer();
        int index = 0;
        synchronized (mLock_wakelock) {
            int i;
            if (mLogInfo.mWakeupReason != null) {
                for (i = 0; i < this.WAKELOCK_NUM; i++) {
                    result.append(String.format("%-41s", new Object[]{SmartcareConstants.UNAVAIBLE_VALUE}));
                }
            } else {
                for (i = 0; i < this.WAKELOCK_NUM; i++) {
                    if (i < mLogInfo.mWakeLocks.size()) {
                        WakeLock wakelock = (WakeLock) mLogInfo.mWakeLocks.get(i);
                        String tempString = wakelock.mWakeLockPID + "/" + wakelock.mWakeLockName;
                        if (tempString.length() > 40) {
                            tempString = tempString.substring(0, 40);
                        }
                        result.append(String.format("%-41s", new Object[]{tempString}));
                        index++;
                    }
                }
                while (index < this.WAKELOCK_NUM) {
                    result.append(String.format("%-41s", new Object[]{SmartcareConstants.UNAVAIBLE_VALUE}));
                    index++;
                }
            }
        }
        return result.toString();
    }

    private void getCurrentTime(LogInfo Data) {
        Data.mTime = this.dateFormate.format(new Date(System.currentTimeMillis()));
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x00d0 A:{SYNTHETIC, Splitter: B:27:0x00d0} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00d5 A:{SYNTHETIC, Splitter: B:30:0x00d5} */
    /* JADX WARNING: Removed duplicated region for block: B:131:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00da A:{SYNTHETIC, Splitter: B:33:0x00da} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x019c A:{SYNTHETIC, Splitter: B:74:0x019c} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x01a1 A:{SYNTHETIC, Splitter: B:77:0x01a1} */
    /* JADX WARNING: Removed duplicated region for block: B:134:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x01a6 A:{SYNTHETIC, Splitter: B:80:0x01a6} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0177 A:{SYNTHETIC, Splitter: B:59:0x0177} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x017c A:{SYNTHETIC, Splitter: B:62:0x017c} */
    /* JADX WARNING: Removed duplicated region for block: B:133:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0181 A:{SYNTHETIC, Splitter: B:65:0x0181} */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x01be A:{SYNTHETIC, Splitter: B:90:0x01be} */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x01c3 A:{SYNTHETIC, Splitter: B:93:0x01c3} */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x01c8 A:{SYNTHETIC, Splitter: B:96:0x01c8} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00d0 A:{SYNTHETIC, Splitter: B:27:0x00d0} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00d5 A:{SYNTHETIC, Splitter: B:30:0x00d5} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00da A:{SYNTHETIC, Splitter: B:33:0x00da} */
    /* JADX WARNING: Removed duplicated region for block: B:131:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x019c A:{SYNTHETIC, Splitter: B:74:0x019c} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x01a1 A:{SYNTHETIC, Splitter: B:77:0x01a1} */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x01a6 A:{SYNTHETIC, Splitter: B:80:0x01a6} */
    /* JADX WARNING: Removed duplicated region for block: B:134:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0177 A:{SYNTHETIC, Splitter: B:59:0x0177} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x017c A:{SYNTHETIC, Splitter: B:62:0x017c} */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0181 A:{SYNTHETIC, Splitter: B:65:0x0181} */
    /* JADX WARNING: Removed duplicated region for block: B:133:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00d0 A:{SYNTHETIC, Splitter: B:27:0x00d0} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00d5 A:{SYNTHETIC, Splitter: B:30:0x00d5} */
    /* JADX WARNING: Removed duplicated region for block: B:131:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00da A:{SYNTHETIC, Splitter: B:33:0x00da} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x019c A:{SYNTHETIC, Splitter: B:74:0x019c} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x01a1 A:{SYNTHETIC, Splitter: B:77:0x01a1} */
    /* JADX WARNING: Removed duplicated region for block: B:134:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x01a6 A:{SYNTHETIC, Splitter: B:80:0x01a6} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0177 A:{SYNTHETIC, Splitter: B:59:0x0177} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x017c A:{SYNTHETIC, Splitter: B:62:0x017c} */
    /* JADX WARNING: Removed duplicated region for block: B:133:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0181 A:{SYNTHETIC, Splitter: B:65:0x0181} */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x01be A:{SYNTHETIC, Splitter: B:90:0x01be} */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x01c3 A:{SYNTHETIC, Splitter: B:93:0x01c3} */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x01c8 A:{SYNTHETIC, Splitter: B:96:0x01c8} */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x01be A:{SYNTHETIC, Splitter: B:90:0x01be} */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x01c3 A:{SYNTHETIC, Splitter: B:93:0x01c3} */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x01c8 A:{SYNTHETIC, Splitter: B:96:0x01c8} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getDataFromWakeupSource(LogInfo Data) {
        Throwable th;
        FileInputStream fInputStream = null;
        InputStreamReader inputReader = null;
        BufferedReader read = null;
        int counter = 0;
        try {
            InputStreamReader inputReader2;
            FileInputStream fInputStream2 = new FileInputStream(this.WAKEUPSOURCE);
            try {
                inputReader2 = new InputStreamReader(fInputStream2, "utf-8");
            } catch (FileNotFoundException e) {
                fInputStream = fInputStream2;
                try {
                    Log.e(this.TAG, "not found the wakeupsource");
                    if (inputReader != null) {
                    }
                    if (read != null) {
                    }
                    if (fInputStream == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (inputReader != null) {
                    }
                    if (read != null) {
                    }
                    if (fInputStream != null) {
                    }
                    throw th;
                }
            } catch (UnsupportedEncodingException e2) {
                fInputStream = fInputStream2;
                Log.e(this.TAG, "not support utf-8");
                if (inputReader != null) {
                }
                if (read != null) {
                }
                if (fInputStream == null) {
                }
            } catch (IOException e3) {
                fInputStream = fInputStream2;
                Log.e(this.TAG, "read wakeupsource failed");
                if (inputReader != null) {
                }
                if (read != null) {
                }
                if (fInputStream == null) {
                }
            } catch (Throwable th3) {
                th = th3;
                fInputStream = fInputStream2;
                if (inputReader != null) {
                }
                if (read != null) {
                }
                if (fInputStream != null) {
                }
                throw th;
            }
            try {
                BufferedReader read2 = new BufferedReader(inputReader2);
                try {
                    for (String tempString = read2.readLine(); tempString != null && counter < this.WAKEUPSOURCE_NUM; tempString = read2.readLine()) {
                        if (tempString.startsWith("Active resource:")) {
                            int index = tempString.indexOf("\t");
                            String temp = tempString.substring(17, index);
                            if (temp.indexOf(" ") == -1) {
                                ((WakeupSource) Data.mWakeupSources.get(counter)).mWakeupSourceName = temp;
                            } else {
                                ((WakeupSource) Data.mWakeupSources.get(counter)).mWakeupSourceName = temp.substring(0, temp.indexOf(" "));
                            }
                            index = tempString.indexOf("\t\t", index + 1);
                            for (int i = 0; i < 3; i++) {
                                index = tempString.indexOf("\t\t", index + 2);
                            }
                            ((WakeupSource) Data.mWakeupSources.get(counter)).mActiveTime = tempString.substring(index + 2, tempString.indexOf("\t\t", index + 2));
                            counter++;
                        }
                    }
                    while (counter < this.WAKEUPSOURCE_NUM) {
                        ((WakeupSource) Data.mWakeupSources.get(counter)).mWakeupSourceName = null;
                        ((WakeupSource) Data.mWakeupSources.get(counter)).mActiveTime = null;
                        counter++;
                    }
                    if (inputReader2 != null) {
                        try {
                            inputReader2.close();
                        } catch (Exception e4) {
                        }
                    }
                    if (read2 != null) {
                        try {
                            read2.close();
                        } catch (Exception e5) {
                        }
                    }
                    if (fInputStream2 != null) {
                        try {
                            fInputStream2.close();
                        } catch (IOException e6) {
                        }
                    }
                } catch (FileNotFoundException e7) {
                    read = read2;
                    inputReader = inputReader2;
                    fInputStream = fInputStream2;
                    Log.e(this.TAG, "not found the wakeupsource");
                    if (inputReader != null) {
                    }
                    if (read != null) {
                    }
                    if (fInputStream == null) {
                    }
                } catch (UnsupportedEncodingException e8) {
                    read = read2;
                    inputReader = inputReader2;
                    fInputStream = fInputStream2;
                    Log.e(this.TAG, "not support utf-8");
                    if (inputReader != null) {
                    }
                    if (read != null) {
                    }
                    if (fInputStream == null) {
                    }
                } catch (IOException e9) {
                    read = read2;
                    inputReader = inputReader2;
                    fInputStream = fInputStream2;
                    Log.e(this.TAG, "read wakeupsource failed");
                    if (inputReader != null) {
                    }
                    if (read != null) {
                    }
                    if (fInputStream == null) {
                    }
                } catch (Throwable th4) {
                    th = th4;
                    read = read2;
                    inputReader = inputReader2;
                    fInputStream = fInputStream2;
                    if (inputReader != null) {
                    }
                    if (read != null) {
                    }
                    if (fInputStream != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e10) {
                inputReader = inputReader2;
                fInputStream = fInputStream2;
                Log.e(this.TAG, "not found the wakeupsource");
                if (inputReader != null) {
                    try {
                        inputReader.close();
                    } catch (Exception e11) {
                    }
                }
                if (read != null) {
                    try {
                        read.close();
                    } catch (Exception e12) {
                    }
                }
                if (fInputStream == null) {
                    try {
                        fInputStream.close();
                    } catch (IOException e13) {
                    }
                }
            } catch (UnsupportedEncodingException e14) {
                inputReader = inputReader2;
                fInputStream = fInputStream2;
                Log.e(this.TAG, "not support utf-8");
                if (inputReader != null) {
                    try {
                        inputReader.close();
                    } catch (Exception e15) {
                    }
                }
                if (read != null) {
                    try {
                        read.close();
                    } catch (Exception e16) {
                    }
                }
                if (fInputStream == null) {
                    try {
                        fInputStream.close();
                    } catch (IOException e17) {
                    }
                }
            } catch (IOException e18) {
                inputReader = inputReader2;
                fInputStream = fInputStream2;
                Log.e(this.TAG, "read wakeupsource failed");
                if (inputReader != null) {
                    try {
                        inputReader.close();
                    } catch (Exception e19) {
                    }
                }
                if (read != null) {
                    try {
                        read.close();
                    } catch (Exception e20) {
                    }
                }
                if (fInputStream == null) {
                    try {
                        fInputStream.close();
                    } catch (IOException e21) {
                    }
                }
            } catch (Throwable th5) {
                th = th5;
                inputReader = inputReader2;
                fInputStream = fInputStream2;
                if (inputReader != null) {
                    try {
                        inputReader.close();
                    } catch (Exception e22) {
                    }
                }
                if (read != null) {
                    try {
                        read.close();
                    } catch (Exception e23) {
                    }
                }
                if (fInputStream != null) {
                    try {
                        fInputStream.close();
                    } catch (IOException e24) {
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e25) {
            Log.e(this.TAG, "not found the wakeupsource");
            if (inputReader != null) {
            }
            if (read != null) {
            }
            if (fInputStream == null) {
            }
        } catch (UnsupportedEncodingException e26) {
            Log.e(this.TAG, "not support utf-8");
            if (inputReader != null) {
            }
            if (read != null) {
            }
            if (fInputStream == null) {
            }
        } catch (IOException e27) {
            Log.e(this.TAG, "read wakeupsource failed");
            if (inputReader != null) {
            }
            if (read != null) {
            }
            if (fInputStream == null) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:45:0x0079 A:{SYNTHETIC, Splitter: B:45:0x0079} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x007e A:{SYNTHETIC, Splitter: B:48:0x007e} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0083 A:{SYNTHETIC, Splitter: B:51:0x0083} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x005a A:{SYNTHETIC, Splitter: B:30:0x005a} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x005f A:{SYNTHETIC, Splitter: B:33:0x005f} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0064 A:{SYNTHETIC, Splitter: B:36:0x0064} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x00c2 A:{SYNTHETIC, Splitter: B:73:0x00c2} */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x00c7 A:{SYNTHETIC, Splitter: B:76:0x00c7} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00cc A:{SYNTHETIC, Splitter: B:79:0x00cc} */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x00a9 A:{SYNTHETIC, Splitter: B:60:0x00a9} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00ae A:{SYNTHETIC, Splitter: B:63:0x00ae} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x00b3 A:{SYNTHETIC, Splitter: B:66:0x00b3} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0079 A:{SYNTHETIC, Splitter: B:45:0x0079} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x007e A:{SYNTHETIC, Splitter: B:48:0x007e} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0083 A:{SYNTHETIC, Splitter: B:51:0x0083} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x005a A:{SYNTHETIC, Splitter: B:30:0x005a} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x005f A:{SYNTHETIC, Splitter: B:33:0x005f} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0064 A:{SYNTHETIC, Splitter: B:36:0x0064} */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x00a9 A:{SYNTHETIC, Splitter: B:60:0x00a9} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00ae A:{SYNTHETIC, Splitter: B:63:0x00ae} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x00b3 A:{SYNTHETIC, Splitter: B:66:0x00b3} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0079 A:{SYNTHETIC, Splitter: B:45:0x0079} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x007e A:{SYNTHETIC, Splitter: B:48:0x007e} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0083 A:{SYNTHETIC, Splitter: B:51:0x0083} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x005a A:{SYNTHETIC, Splitter: B:30:0x005a} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x005f A:{SYNTHETIC, Splitter: B:33:0x005f} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0064 A:{SYNTHETIC, Splitter: B:36:0x0064} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x00c2 A:{SYNTHETIC, Splitter: B:73:0x00c2} */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x00c7 A:{SYNTHETIC, Splitter: B:76:0x00c7} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00cc A:{SYNTHETIC, Splitter: B:79:0x00cc} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x00c2 A:{SYNTHETIC, Splitter: B:73:0x00c2} */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x00c7 A:{SYNTHETIC, Splitter: B:76:0x00c7} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00cc A:{SYNTHETIC, Splitter: B:79:0x00cc} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String getOneLineString(String path) {
        Throwable th;
        if (path == null) {
            return null;
        }
        String tempString = null;
        FileInputStream fInputStream = null;
        InputStreamReader inputReader = null;
        BufferedReader bReader = null;
        try {
            FileInputStream fInputStream2 = new FileInputStream(path);
            try {
                InputStreamReader inputReader2 = new InputStreamReader(fInputStream2, "utf-8");
                try {
                    BufferedReader bReader2 = new BufferedReader(inputReader2);
                    try {
                        tempString = bReader2.readLine();
                        if (bReader2 != null) {
                            try {
                                bReader2.close();
                            } catch (IOException e) {
                            }
                        }
                        if (inputReader2 != null) {
                            try {
                                inputReader2.close();
                            } catch (Exception e2) {
                            }
                        }
                        if (fInputStream2 != null) {
                            try {
                                fInputStream2.close();
                            } catch (IOException e3) {
                            }
                        }
                        fInputStream = fInputStream2;
                    } catch (FileNotFoundException e4) {
                        bReader = bReader2;
                        inputReader = inputReader2;
                        fInputStream = fInputStream2;
                    } catch (UnsupportedEncodingException e5) {
                        bReader = bReader2;
                        inputReader = inputReader2;
                        fInputStream = fInputStream2;
                        Log.e(this.TAG, "not support utf-8");
                        if (bReader != null) {
                        }
                        if (inputReader != null) {
                        }
                        if (fInputStream != null) {
                        }
                        return tempString;
                    } catch (IOException e6) {
                        bReader = bReader2;
                        inputReader = inputReader2;
                        fInputStream = fInputStream2;
                        try {
                            Log.e(this.TAG, "read " + path + " failed");
                            if (bReader != null) {
                            }
                            if (inputReader != null) {
                            }
                            if (fInputStream != null) {
                            }
                            return tempString;
                        } catch (Throwable th2) {
                            th = th2;
                            if (bReader != null) {
                            }
                            if (inputReader != null) {
                            }
                            if (fInputStream != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        bReader = bReader2;
                        inputReader = inputReader2;
                        fInputStream = fInputStream2;
                        if (bReader != null) {
                        }
                        if (inputReader != null) {
                        }
                        if (fInputStream != null) {
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e7) {
                    inputReader = inputReader2;
                    fInputStream = fInputStream2;
                    Log.e(this.TAG, "not found the " + path);
                    if (bReader != null) {
                        try {
                            bReader.close();
                        } catch (IOException e8) {
                        }
                    }
                    if (inputReader != null) {
                        try {
                            inputReader.close();
                        } catch (Exception e9) {
                        }
                    }
                    if (fInputStream != null) {
                        try {
                            fInputStream.close();
                        } catch (IOException e10) {
                        }
                    }
                    return tempString;
                } catch (UnsupportedEncodingException e11) {
                    inputReader = inputReader2;
                    fInputStream = fInputStream2;
                    Log.e(this.TAG, "not support utf-8");
                    if (bReader != null) {
                        try {
                            bReader.close();
                        } catch (IOException e12) {
                        }
                    }
                    if (inputReader != null) {
                        try {
                            inputReader.close();
                        } catch (Exception e13) {
                        }
                    }
                    if (fInputStream != null) {
                        try {
                            fInputStream.close();
                        } catch (IOException e14) {
                        }
                    }
                    return tempString;
                } catch (IOException e15) {
                    inputReader = inputReader2;
                    fInputStream = fInputStream2;
                    Log.e(this.TAG, "read " + path + " failed");
                    if (bReader != null) {
                        try {
                            bReader.close();
                        } catch (IOException e16) {
                        }
                    }
                    if (inputReader != null) {
                        try {
                            inputReader.close();
                        } catch (Exception e17) {
                        }
                    }
                    if (fInputStream != null) {
                        try {
                            fInputStream.close();
                        } catch (IOException e18) {
                        }
                    }
                    return tempString;
                } catch (Throwable th4) {
                    th = th4;
                    inputReader = inputReader2;
                    fInputStream = fInputStream2;
                    if (bReader != null) {
                        try {
                            bReader.close();
                        } catch (IOException e19) {
                        }
                    }
                    if (inputReader != null) {
                        try {
                            inputReader.close();
                        } catch (Exception e20) {
                        }
                    }
                    if (fInputStream != null) {
                        try {
                            fInputStream.close();
                        } catch (IOException e21) {
                        }
                    }
                    throw th;
                }
            } catch (FileNotFoundException e22) {
                fInputStream = fInputStream2;
                Log.e(this.TAG, "not found the " + path);
                if (bReader != null) {
                }
                if (inputReader != null) {
                }
                if (fInputStream != null) {
                }
                return tempString;
            } catch (UnsupportedEncodingException e23) {
                fInputStream = fInputStream2;
                Log.e(this.TAG, "not support utf-8");
                if (bReader != null) {
                }
                if (inputReader != null) {
                }
                if (fInputStream != null) {
                }
                return tempString;
            } catch (IOException e24) {
                fInputStream = fInputStream2;
                Log.e(this.TAG, "read " + path + " failed");
                if (bReader != null) {
                }
                if (inputReader != null) {
                }
                if (fInputStream != null) {
                }
                return tempString;
            } catch (Throwable th5) {
                th = th5;
                fInputStream = fInputStream2;
                if (bReader != null) {
                }
                if (inputReader != null) {
                }
                if (fInputStream != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e25) {
            Log.e(this.TAG, "not found the " + path);
            if (bReader != null) {
            }
            if (inputReader != null) {
            }
            if (fInputStream != null) {
            }
            return tempString;
        } catch (UnsupportedEncodingException e26) {
            Log.e(this.TAG, "not support utf-8");
            if (bReader != null) {
            }
            if (inputReader != null) {
            }
            if (fInputStream != null) {
            }
            return tempString;
        } catch (IOException e27) {
            Log.e(this.TAG, "read " + path + " failed");
            if (bReader != null) {
            }
            if (inputReader != null) {
            }
            if (fInputStream != null) {
            }
            return tempString;
        }
        return tempString;
    }

    private void getCpuFreq() {
        int i = 0;
        while (i < 4 && !new File(this.CPU_FREQ_HEAD + i + this.CPU_FREQ_TAIL).exists()) {
            i++;
        }
        mLogInfo.mCPU0Freq = i < 4 ? getOneLineString(this.CPU_FREQ_HEAD + i + this.CPU_FREQ_TAIL) : SmartcareConstants.UNAVAIBLE_VALUE;
        mLogInfo.mCPU0Freq_Max = i < 4 ? getOneLineString(this.CPU_FREQ_HEAD + i + this.CPU_MAX_FREQ_TAIL) : SmartcareConstants.UNAVAIBLE_VALUE;
        i = 4;
        while (i < 8 && !new File(this.CPU_FREQ_HEAD + i + this.CPU_FREQ_TAIL).exists()) {
            i++;
        }
        mLogInfo.mCPU4Freq = i < 8 ? getOneLineString(this.CPU_FREQ_HEAD + i + this.CPU_FREQ_TAIL) : SmartcareConstants.UNAVAIBLE_VALUE;
        mLogInfo.mCPU4Freq_Max = i < 8 ? getOneLineString(this.CPU_FREQ_HEAD + i + this.CPU_MAX_FREQ_TAIL) : SmartcareConstants.UNAVAIBLE_VALUE;
    }

    private void beginWriteFile() {
        getCurrentTime(mLogInfo);
        mLogInfo.mCPUOnLine = getOneLineString(this.CPU_ONLINE);
        getCpuFreq();
        String s = getOneLineString(this.CURRENT);
        LogInfo logInfo = mLogInfo;
        if (s == null) {
            s = "0";
        }
        logInfo.mCurrent = s;
        s = getOneLineString(this.CURRENT_LIMIT);
        logInfo = mLogInfo;
        if (s == null) {
            s = "0";
        }
        logInfo.mCurrentLimit = s;
        s = getOneLineString(this.SOC_RM);
        logInfo = mLogInfo;
        if (s == null) {
            s = SmartcareConstants.UNAVAIBLE_VALUE;
        }
        logInfo.mSOC_rm = s;
        s = getOneLineString(this.CPU0_TEMP);
        logInfo = mLogInfo;
        if (s == null) {
            s = "0";
        }
        logInfo.mCPU0Temp = s;
        s = getOneLineString(this.CPU1_TEMP);
        logInfo = mLogInfo;
        if (s == null) {
            s = "0";
        }
        logInfo.mCPU1Temp = s;
        s = getOneLineString(this.BOARD_TEMP);
        logInfo = mLogInfo;
        if (s == null) {
            s = "0";
        }
        logInfo.mBoardTemp = s;
        s = getOneLineString(this.PA_TEMP);
        logInfo = mLogInfo;
        if (s == null) {
            s = "0";
        }
        logInfo.mPA_temp = s;
        s = getOneLineString(this.BAT_TEMP);
        logInfo = mLogInfo;
        if (s == null) {
            s = "0";
        }
        logInfo.mBattery_temp = s;
        try {
            getCpuInfo(mLogInfo);
        } catch (Exception e) {
            Log.w(this.TAG, "failed to getCpuInfo");
        }
        if (mLogInfo.mWakeupReason != null) {
            for (WakeupSource wakeupsource : mLogInfo.mWakeupSources) {
                wakeupsource.mWakeupSourceName = null;
            }
        } else {
            try {
                getDataFromWakeupSource(mLogInfo);
            } catch (Exception e2) {
                Log.w(this.TAG, "failed to getDataFromWakeupSource");
            }
        }
        writeFile();
    }

    private void writeTitleToFile(PrintWriter printWriter) {
        int i;
        StringBuffer writeData = new StringBuffer();
        writeData.append(String.format("%-11s", new Object[]{"Date"})).append(String.format("%-15s", new Object[]{"Time"})).append(String.format("%-7s", new Object[]{"Bright"})).append(String.format("%-5s", new Object[]{"SOC"}));
        writeData.append(String.format("%-7s", new Object[]{"SOC_rm"})).append(String.format("%-10s", new Object[]{"Current"})).append(String.format("%-10s", new Object[]{"Cur_Limit"})).append(String.format("%-9s", new Object[]{"Charging"}));
        writeData.append(String.format("%-6s", new Object[]{"Modem"})).append(String.format("%-11s", new Object[]{"SignalType"})).append(String.format("%-15s", new Object[]{"SignalStrength"})).append(String.format("%-15s", new Object[]{"DateConnection"}));
        writeData.append(String.format("%-5s", new Object[]{"WIFI"})).append(String.format("%-4s", new Object[]{"BT"})).append(String.format("%-4s", new Object[]{"GPS"})).append(String.format("%-7s", new Object[]{"Camera"}));
        writeData.append(String.format("%-4s", new Object[]{"NFC"})).append(String.format("%-8s", new Object[]{"Headset"})).append(String.format("%-9s", new Object[]{"musicVal"})).append(String.format("%-41s", new Object[]{"FrontAPP"}));
        for (i = 1; i <= this.WAKELOCK_NUM; i++) {
            writeData.append(String.format("%-41s", new Object[]{"PID" + i + "/WakeLock" + i}));
        }
        for (i = 1; i <= this.WAKEUPSOURCE_NUM; i++) {
            writeData.append(String.format("%-46s", new Object[]{"ActiveWakeSource" + i + "/Time" + i}));
        }
        writeData.append(String.format("%-13s", new Object[]{"SuspendState"})).append(String.format("%-9s", new Object[]{"CPU0Temp"})).append(String.format("%-9s", new Object[]{"CPU1Temp"})).append(String.format("%-10s", new Object[]{"BoardTemp"}));
        writeData.append(String.format("%-9s", new Object[]{"PA_Temp"})).append(String.format("%-9s", new Object[]{"BAT_TEMP"})).append(String.format("%-11s", new Object[]{"CPULoading"}));
        for (i = 1; i <= this.TOP_PROCESS_NUM; i++) {
            writeData.append(String.format("%-41s", new Object[]{"cpu_load" + i + "/PID" + i + "/TOP_process" + i}));
        }
        writeData.append(String.format("%-10s", new Object[]{"CPU0Freq"})).append(String.format("%-10s", new Object[]{"CPU0_Max"})).append(String.format("%-10s", new Object[]{"CPU4Freq"})).append(String.format("%-10s", new Object[]{"CPU4_Max"}));
        writeData.append(String.format("%-10s", new Object[]{"CPUOnline"})).append(String.format("%-71s", new Object[]{"WakeupReason"})).append("AlarmName");
        printWriter.println(writeData.toString());
    }

    private void writeDataToFile(PrintWriter printWriter, LogInfo Data) {
        StringBuffer writeData = new StringBuffer();
        writeData.append(String.format("%-26s", new Object[]{Data.mTime})).append(String.format("%-7d", new Object[]{Integer.valueOf(Data.mBrightness)})).append(String.format("%-5d", new Object[]{Integer.valueOf(Data.mBatteryLevel)}));
        writeData.append(String.format("%-7s", new Object[]{Data.mSOC_rm})).append(String.format("%-10s", new Object[]{Data.mCurrent})).append(String.format("%-10s", new Object[]{Data.mCurrentLimit})).append(String.format("%-9d", new Object[]{Integer.valueOf(Data.mChargeStatus)}));
        writeData.append(String.format("%-6s", new Object[]{SystemProperties.get("persist.sys.logsystem.modem", SmartcareConstants.UNAVAIBLE_VALUE)})).append(String.format("%-11d", new Object[]{Integer.valueOf(Data.mConnectionStatus)})).append(String.format("%-15d", new Object[]{Integer.valueOf(Data.mSignalStrength)}));
        writeData.append(String.format("%-15d", new Object[]{Integer.valueOf(Data.mDataConnection)})).append(String.format("%-5d", new Object[]{Integer.valueOf(Data.mWifiStatus)})).append(String.format("%-4d", new Object[]{Integer.valueOf(Data.mBTState)})).append(String.format("%-4d", new Object[]{Integer.valueOf(Data.mGPSStatus)}));
        writeData.append(String.format("%-7d", new Object[]{Integer.valueOf(Data.mCameraState)})).append(String.format("%-4d", new Object[]{Integer.valueOf(Data.mNFCOn)})).append(String.format("%-8d", new Object[]{Integer.valueOf(Data.mHeadSet)})).append(String.format("%-9d", new Object[]{Integer.valueOf(musicValume)}));
        String tempString = Data.mTopAppName == null ? SmartcareConstants.UNAVAIBLE_VALUE : Data.mTopAppUID + "/" + Data.mTopAppName;
        if (tempString.length() > 40) {
            tempString = tempString.substring(0, 40);
        }
        writeData.append(String.format("%-41s", new Object[]{tempString}));
        writeData.append(writeWakeLockToFile(Data));
        for (WakeupSource wakeupsource : Data.mWakeupSources) {
            if (wakeupsource.mWakeupSourceName != null) {
                tempString = wakeupsource.mWakeupSourceName + "/" + wakeupsource.mActiveTime;
                if (tempString.length() > 45) {
                    tempString = tempString.substring(0, 45);
                }
                writeData.append(String.format("%-46s", new Object[]{tempString}));
            } else {
                writeData.append(String.format("%-46s", new Object[]{SmartcareConstants.UNAVAIBLE_VALUE}));
            }
        }
        int enterAsleep = (this.mSuspendState && this.mWakeLockNumber == 0) ? 1 : 0;
        writeData.append(String.format("%-13d", new Object[]{Integer.valueOf(enterAsleep)})).append(String.format("%-9s", new Object[]{Data.mCPU0Temp})).append(String.format("%-9s", new Object[]{Data.mCPU1Temp})).append(String.format("%-10s", new Object[]{Data.mBoardTemp}));
        writeData.append(String.format("%-9s", new Object[]{Data.mPA_temp})).append(String.format("%-9s", new Object[]{Data.mBattery_temp})).append(String.format("%-11d", new Object[]{Integer.valueOf(Data.mCpuTotalLoad)}));
        for (CpuTopLoad cputopload : Data.mCpuTopLoads) {
            tempString = cputopload.mCpuTopLoad + "/" + cputopload.mCpuTopPid + "/" + cputopload.mCpuTopName;
            if (tempString.length() > 40) {
                tempString = tempString.substring(0, 40);
            }
            writeData.append(String.format("%-41s", new Object[]{tempString}));
        }
        tempString = Data.mWakeupReason == null ? SmartcareConstants.UNAVAIBLE_VALUE : Data.mWakeupReason;
        if (tempString.length() > 70) {
            tempString = tempString.substring(0, 70);
        }
        writeData.append(String.format("%-10s", new Object[]{Data.mCPU0Freq})).append(String.format("%-10s", new Object[]{Data.mCPU0Freq_Max})).append(String.format("%-10s", new Object[]{Data.mCPU4Freq}));
        writeData.append(String.format("%-10s", new Object[]{Data.mCPU4Freq_Max})).append(String.format("%-10s", new Object[]{Data.mCPUOnLine})).append(String.format("%-71s", new Object[]{tempString})).append(Data.mAlarmName == null ? SmartcareConstants.UNAVAIBLE_VALUE : Data.mAlarmName);
        printWriter.println(writeData.toString());
    }

    /* JADX WARNING: Removed duplicated region for block: B:63:0x00db  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x00e0 A:{SYNTHETIC, Splitter: B:65:0x00e0} */
    /* JADX WARNING: Removed duplicated region for block: B:111:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x00e5 A:{SYNTHETIC, Splitter: B:68:0x00e5} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00be  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00c3 A:{SYNTHETIC, Splitter: B:52:0x00c3} */
    /* JADX WARNING: Removed duplicated region for block: B:110:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00c8 A:{SYNTHETIC, Splitter: B:55:0x00c8} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00a1  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00a6 A:{SYNTHETIC, Splitter: B:39:0x00a6} */
    /* JADX WARNING: Removed duplicated region for block: B:109:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00ab A:{SYNTHETIC, Splitter: B:42:0x00ab} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x00f0  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x00f5 A:{SYNTHETIC, Splitter: B:76:0x00f5} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00fa A:{SYNTHETIC, Splitter: B:79:0x00fa} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00db  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x00e0 A:{SYNTHETIC, Splitter: B:65:0x00e0} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x00e5 A:{SYNTHETIC, Splitter: B:68:0x00e5} */
    /* JADX WARNING: Removed duplicated region for block: B:111:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00be  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00c3 A:{SYNTHETIC, Splitter: B:52:0x00c3} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00c8 A:{SYNTHETIC, Splitter: B:55:0x00c8} */
    /* JADX WARNING: Removed duplicated region for block: B:110:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00a1  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00a6 A:{SYNTHETIC, Splitter: B:39:0x00a6} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00ab A:{SYNTHETIC, Splitter: B:42:0x00ab} */
    /* JADX WARNING: Removed duplicated region for block: B:109:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x00f0  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x00f5 A:{SYNTHETIC, Splitter: B:76:0x00f5} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00fa A:{SYNTHETIC, Splitter: B:79:0x00fa} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00db  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x00e0 A:{SYNTHETIC, Splitter: B:65:0x00e0} */
    /* JADX WARNING: Removed duplicated region for block: B:111:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x00e5 A:{SYNTHETIC, Splitter: B:68:0x00e5} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00be  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00c3 A:{SYNTHETIC, Splitter: B:52:0x00c3} */
    /* JADX WARNING: Removed duplicated region for block: B:110:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00c8 A:{SYNTHETIC, Splitter: B:55:0x00c8} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00a1  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00a6 A:{SYNTHETIC, Splitter: B:39:0x00a6} */
    /* JADX WARNING: Removed duplicated region for block: B:109:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00ab A:{SYNTHETIC, Splitter: B:42:0x00ab} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x00f0  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x00f5 A:{SYNTHETIC, Splitter: B:76:0x00f5} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00fa A:{SYNTHETIC, Splitter: B:79:0x00fa} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeFile() {
        Throwable th;
        int flag = 0;
        File powerinfologtxt = new File(Environment.getDataDirectory(), this.BIGZIPFILEPATH + File.separator + this.logFileName);
        FileOutputStream fOutputStream = null;
        OutputStreamWriter outWriter = null;
        PrintWriter pw = null;
        try {
            if (!(powerinfologtxt.getParentFile().exists() || powerinfologtxt.getParentFile().mkdirs())) {
                Log.w(this.TAG, "fail to create dir");
            }
            if (!powerinfologtxt.exists()) {
                if (!powerinfologtxt.createNewFile()) {
                    Log.w(this.TAG, "fail to create new file");
                }
                powerinfologtxt.setReadable(true, false);
                flag = 1;
            }
            FileOutputStream fOutputStream2 = new FileOutputStream(powerinfologtxt, true);
            try {
                OutputStreamWriter outWriter2 = new OutputStreamWriter(fOutputStream2, "utf-8");
                try {
                    PrintWriter pw2 = new PrintWriter(outWriter2);
                    if (flag == 1) {
                        try {
                            writeTitleToFile(pw2);
                        } catch (FileNotFoundException e) {
                            pw = pw2;
                            outWriter = outWriter2;
                            fOutputStream = fOutputStream2;
                            Log.e(this.TAG, "not found log file");
                            if (pw != null) {
                            }
                            if (outWriter != null) {
                            }
                            if (fOutputStream == null) {
                            }
                        } catch (UnsupportedEncodingException e2) {
                            pw = pw2;
                            outWriter = outWriter2;
                            fOutputStream = fOutputStream2;
                            Log.e(this.TAG, "not support utf-8");
                            if (pw != null) {
                            }
                            if (outWriter != null) {
                            }
                            if (fOutputStream == null) {
                            }
                        } catch (IOException e3) {
                            pw = pw2;
                            outWriter = outWriter2;
                            fOutputStream = fOutputStream2;
                            try {
                                Log.e(this.TAG, "IOException.");
                                if (pw != null) {
                                }
                                if (outWriter != null) {
                                }
                                if (fOutputStream == null) {
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                if (pw != null) {
                                    pw.close();
                                }
                                if (outWriter != null) {
                                    try {
                                        outWriter.close();
                                    } catch (IOException e4) {
                                    }
                                }
                                if (fOutputStream != null) {
                                    try {
                                        fOutputStream.close();
                                    } catch (IOException e5) {
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            pw = pw2;
                            outWriter = outWriter2;
                            fOutputStream = fOutputStream2;
                            if (pw != null) {
                            }
                            if (outWriter != null) {
                            }
                            if (fOutputStream != null) {
                            }
                            throw th;
                        }
                    }
                    writeDataToFile(pw2, mLogInfo);
                    pw2.flush();
                    if (pw2 != null) {
                        pw2.close();
                    }
                    if (outWriter2 != null) {
                        try {
                            outWriter2.close();
                        } catch (IOException e6) {
                        }
                    }
                    if (fOutputStream2 != null) {
                        try {
                            fOutputStream2.close();
                        } catch (IOException e7) {
                        }
                    }
                    fOutputStream = fOutputStream2;
                } catch (FileNotFoundException e8) {
                    outWriter = outWriter2;
                    fOutputStream = fOutputStream2;
                    Log.e(this.TAG, "not found log file");
                    if (pw != null) {
                    }
                    if (outWriter != null) {
                    }
                    if (fOutputStream == null) {
                    }
                } catch (UnsupportedEncodingException e9) {
                    outWriter = outWriter2;
                    fOutputStream = fOutputStream2;
                    Log.e(this.TAG, "not support utf-8");
                    if (pw != null) {
                    }
                    if (outWriter != null) {
                    }
                    if (fOutputStream == null) {
                    }
                } catch (IOException e10) {
                    outWriter = outWriter2;
                    fOutputStream = fOutputStream2;
                    Log.e(this.TAG, "IOException.");
                    if (pw != null) {
                    }
                    if (outWriter != null) {
                    }
                    if (fOutputStream == null) {
                    }
                } catch (Throwable th4) {
                    th = th4;
                    outWriter = outWriter2;
                    fOutputStream = fOutputStream2;
                    if (pw != null) {
                    }
                    if (outWriter != null) {
                    }
                    if (fOutputStream != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e11) {
                fOutputStream = fOutputStream2;
                Log.e(this.TAG, "not found log file");
                if (pw != null) {
                }
                if (outWriter != null) {
                }
                if (fOutputStream == null) {
                }
            } catch (UnsupportedEncodingException e12) {
                fOutputStream = fOutputStream2;
                Log.e(this.TAG, "not support utf-8");
                if (pw != null) {
                }
                if (outWriter != null) {
                }
                if (fOutputStream == null) {
                }
            } catch (IOException e13) {
                fOutputStream = fOutputStream2;
                Log.e(this.TAG, "IOException.");
                if (pw != null) {
                }
                if (outWriter != null) {
                }
                if (fOutputStream == null) {
                }
            } catch (Throwable th5) {
                th = th5;
                fOutputStream = fOutputStream2;
                if (pw != null) {
                }
                if (outWriter != null) {
                }
                if (fOutputStream != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e14) {
            Log.e(this.TAG, "not found log file");
            if (pw != null) {
                pw.close();
            }
            if (outWriter != null) {
                try {
                    outWriter.close();
                } catch (IOException e15) {
                }
            }
            if (fOutputStream == null) {
                try {
                    fOutputStream.close();
                } catch (IOException e16) {
                }
            }
        } catch (UnsupportedEncodingException e17) {
            Log.e(this.TAG, "not support utf-8");
            if (pw != null) {
                pw.close();
            }
            if (outWriter != null) {
                try {
                    outWriter.close();
                } catch (IOException e18) {
                }
            }
            if (fOutputStream == null) {
                try {
                    fOutputStream.close();
                } catch (IOException e19) {
                }
            }
        } catch (IOException e20) {
            Log.e(this.TAG, "IOException.");
            if (pw != null) {
                pw.close();
            }
            if (outWriter != null) {
                try {
                    outWriter.close();
                } catch (IOException e21) {
                }
            }
            if (fOutputStream == null) {
                try {
                    fOutputStream.close();
                } catch (IOException e22) {
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x0235 A:{SYNTHETIC, Splitter: B:41:0x0235} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x023a A:{SYNTHETIC, Splitter: B:44:0x023a} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x023f A:{SYNTHETIC, Splitter: B:47:0x023f} */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0293 A:{SYNTHETIC, Splitter: B:72:0x0293} */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0298 A:{SYNTHETIC, Splitter: B:75:0x0298} */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x029d A:{SYNTHETIC, Splitter: B:78:0x029d} */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x02b0 A:{SYNTHETIC, Splitter: B:88:0x02b0} */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x02b5 A:{SYNTHETIC, Splitter: B:91:0x02b5} */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x02ba A:{SYNTHETIC, Splitter: B:94:0x02ba} */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x02b0 A:{SYNTHETIC, Splitter: B:88:0x02b0} */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x02b5 A:{SYNTHETIC, Splitter: B:91:0x02b5} */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x02ba A:{SYNTHETIC, Splitter: B:94:0x02ba} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0235 A:{SYNTHETIC, Splitter: B:41:0x0235} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x023a A:{SYNTHETIC, Splitter: B:44:0x023a} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x023f A:{SYNTHETIC, Splitter: B:47:0x023f} */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0293 A:{SYNTHETIC, Splitter: B:72:0x0293} */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0298 A:{SYNTHETIC, Splitter: B:75:0x0298} */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x029d A:{SYNTHETIC, Splitter: B:78:0x029d} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0235 A:{SYNTHETIC, Splitter: B:41:0x0235} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x023a A:{SYNTHETIC, Splitter: B:44:0x023a} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x023f A:{SYNTHETIC, Splitter: B:47:0x023f} */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0293 A:{SYNTHETIC, Splitter: B:72:0x0293} */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0298 A:{SYNTHETIC, Splitter: B:75:0x0298} */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x029d A:{SYNTHETIC, Splitter: B:78:0x029d} */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x02b0 A:{SYNTHETIC, Splitter: B:88:0x02b0} */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x02b5 A:{SYNTHETIC, Splitter: B:91:0x02b5} */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x02ba A:{SYNTHETIC, Splitter: B:94:0x02ba} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void zipFile(long num) {
        ZipOutputStream out;
        Throwable th;
        File file = new File(Environment.getDataDirectory(), this.BIGZIPFILEPATH + File.separator + this.logFileName + "-" + this.zipFileMax + ".tar.gz");
        if (file.exists() && !file.delete()) {
            Log.w(this.TAG, "delete zip file failed! ");
        }
        for (int i = this.zipFileMax - 1; i >= 0; i--) {
            file = new File(Environment.getDataDirectory(), this.BIGZIPFILEPATH + File.separator + this.logFileName + "-" + i + ".tar.gz");
            if (file.exists()) {
                if (!file.renameTo(new File(Environment.getDataDirectory(), this.BIGZIPFILEPATH + File.separator + this.logFileName + "-" + (i + 1) + ".tar.gz"))) {
                    Log.w(this.TAG, "failed rename file! ");
                }
            }
        }
        File powerLogZipBig = new File(Environment.getDataDirectory(), this.BIGZIPFILEPATH + File.separator + this.logFileName + "-0" + ".tar.gz");
        if (powerLogZipBig.exists()) {
            this.hasCreateBigZip = 1;
            return;
        }
        FileOutputStream fOutputStream = null;
        ZipOutputStream out2 = null;
        FileInputStream in = null;
        try {
            FileInputStream in2;
            byte[] buf = new byte[1024];
            FileOutputStream fOutputStream2 = new FileOutputStream(powerLogZipBig);
            try {
                out = new ZipOutputStream(fOutputStream2);
                try {
                    file = new File(Environment.getDataDirectory(), this.BIGZIPFILEPATH + File.separator + this.logFileName);
                    if (!file.exists()) {
                        Log.w(this.TAG, "No txt file to zip ");
                    }
                    file = new File(Environment.getDataDirectory(), this.BIGZIPFILEPATH + File.separator + this.logFileName + "-0");
                    if (!file.renameTo(file)) {
                        Log.w(this.TAG, "failed rename file! ");
                    }
                    in2 = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    out2 = out;
                    fOutputStream = fOutputStream2;
                    try {
                        Log.e(this.TAG, "not found file when ZIP file");
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e2) {
                            }
                        }
                        if (out2 != null) {
                            try {
                                out2.close();
                            } catch (IOException e3) {
                            }
                        }
                        if (fOutputStream != null) {
                            try {
                                fOutputStream.close();
                            } catch (IOException e4) {
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (in != null) {
                        }
                        if (out2 != null) {
                        }
                        if (fOutputStream != null) {
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    out2 = out;
                    fOutputStream = fOutputStream2;
                    Log.e(this.TAG, "zip file failed");
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e6) {
                        }
                    }
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (IOException e7) {
                        }
                    }
                    if (fOutputStream != null) {
                        try {
                            fOutputStream.close();
                        } catch (IOException e8) {
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    out2 = out;
                    fOutputStream = fOutputStream2;
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e9) {
                        }
                    }
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (IOException e10) {
                        }
                    }
                    if (fOutputStream != null) {
                        try {
                            fOutputStream.close();
                        } catch (IOException e11) {
                        }
                    }
                    throw th;
                }
            } catch (FileNotFoundException e12) {
                fOutputStream = fOutputStream2;
                Log.e(this.TAG, "not found file when ZIP file");
                if (in != null) {
                }
                if (out2 != null) {
                }
                if (fOutputStream != null) {
                }
            } catch (IOException e13) {
                fOutputStream = fOutputStream2;
                Log.e(this.TAG, "zip file failed");
                if (in != null) {
                }
                if (out2 != null) {
                }
                if (fOutputStream != null) {
                }
            } catch (Throwable th4) {
                th = th4;
                fOutputStream = fOutputStream2;
                if (in != null) {
                }
                if (out2 != null) {
                }
                if (fOutputStream != null) {
                }
                throw th;
            }
            try {
                out.putNextEntry(new ZipEntry(file.getName()));
                while (true) {
                    int len = in2.read(buf);
                    if (len <= 0) {
                        break;
                    }
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                if (!file.delete()) {
                    Log.w(this.TAG, "delete powerLog file failed!");
                }
                powerLogZipBig.setReadable(true, false);
                this.hasCreateBigZip = 1;
                if (in2 != null) {
                    try {
                        in2.close();
                    } catch (IOException e14) {
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e15) {
                    }
                }
                if (fOutputStream2 != null) {
                    try {
                        fOutputStream2.close();
                    } catch (IOException e16) {
                    }
                }
            } catch (FileNotFoundException e17) {
                in = in2;
                out2 = out;
                fOutputStream = fOutputStream2;
                Log.e(this.TAG, "not found file when ZIP file");
                if (in != null) {
                }
                if (out2 != null) {
                }
                if (fOutputStream != null) {
                }
            } catch (IOException e18) {
                in = in2;
                out2 = out;
                fOutputStream = fOutputStream2;
                Log.e(this.TAG, "zip file failed");
                if (in != null) {
                }
                if (out2 != null) {
                }
                if (fOutputStream != null) {
                }
            } catch (Throwable th5) {
                th = th5;
                in = in2;
                out2 = out;
                fOutputStream = fOutputStream2;
                if (in != null) {
                }
                if (out2 != null) {
                }
                if (fOutputStream != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e19) {
            Log.e(this.TAG, "not found file when ZIP file");
            if (in != null) {
            }
            if (out2 != null) {
            }
            if (fOutputStream != null) {
            }
        } catch (IOException e20) {
            Log.e(this.TAG, "zip file failed");
            if (in != null) {
            }
            if (out2 != null) {
            }
            if (fOutputStream != null) {
            }
        }
    }

    private void initArrayList() {
        int i;
        for (i = 0; i < this.WAKEUPSOURCE_NUM; i++) {
            mLogInfo.mWakeupSources.add(new WakeupSource());
        }
        for (i = 0; i < this.TOP_PROCESS_NUM; i++) {
            mLogInfo.mCpuTopLoads.add(new CpuTopLoad());
        }
    }

    private static void setLogInfo(LogInfo loginfo) {
        mLogInfo = loginfo;
    }

    private static void setReceiver(PowerInfoServiceReceiver receiver) {
        mReceiver = receiver;
    }

    private HwPowerInfoService() {
        this.TAG = "HwPowerInfoService";
        this.CPU_FREQ_HEAD = "/sys/devices/system/cpu/cpu";
        this.CPU_FREQ_TAIL = "/cpufreq/scaling_cur_freq";
        this.CPU_MAX_FREQ_TAIL = "/cpufreq/scaling_max_freq";
        this.CPU0_TEMP = "";
        this.CPU1_TEMP = "";
        this.BOARD_TEMP = "";
        this.PA_TEMP = "";
        this.BAT_TEMP = "";
        this.CPU_ONLINE = "/sys/devices/system/cpu/online";
        this.WAKEUPSOURCE = "/sys/kernel/debug/wakeup_sources";
        this.CURRENT = "";
        this.CURRENT_LIMIT = "/sys/class/hw_power/charger/charge_data/iin_thermal";
        this.SOC_RM = "/sys/class/power_supply/Battery/capacity_rm";
        this.BIGZIPFILEPATH = "log/sleeplog";
        this.logFileName = "power_stats-log";
        this.WAKELOCK_NUM = 3;
        this.WAKEUPSOURCE_NUM = 3;
        this.TOP_PROCESS_NUM = 3;
        this.timestep = 5000;
        this.zipFileMax = 9;
        this.hasCreateBigZip = 0;
        this.zipStartHour = 0;
        this.zipEndHour = 12;
        this.dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        this.mLastTopAppName = null;
        this.mLastTopAppUid = 0;
        this.isThreadAlive = true;
        this.mWakeLockNumber = 0;
        this.mSuspendState = false;
        this.hasCreateBigZip = 0;
        setLogInfo(new LogInfo());
        parseXML();
        initArrayList();
        this.mCpuTracker = new ProcessCpuTracker(false);
        this.mCpuTracker.init();
        setReceiver(new PowerInfoServiceReceiver());
        this.mWorker = new WorkerThread("PowerInfoService");
        this.mWorker.start();
        Log.i(this.TAG, "construct the powerinfoservice");
    }

    private static void registerMyReceiver(Context context) {
        mContext = context;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.HEADSET_PLUG");
        filter.addAction("android.nfc.action.ADAPTER_STATE_CHANGED");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        filter.addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        mContext.registerReceiver(mReceiver, filter);
        musicValume = ((AudioManager) mContext.getSystemService("audio")).getStreamVolume(3);
    }

    public static HwPowerInfoService getInstance(Context context, boolean isSystemReady) {
        HwPowerInfoService hwPowerInfoService;
        synchronized (mLockInit) {
            if (isSystemReady && context != null) {
                registerMyReceiver(context);
            }
            if (mSingleInstance == null) {
                mSingleInstance = new HwPowerInfoService();
            }
            hwPowerInfoService = mSingleInstance;
        }
        return hwPowerInfoService;
    }

    public void notePowerInfoBrightness(int brightness) {
        if (brightness == 0) {
            this.mLastTopAppName = mLogInfo.mTopAppName;
            this.mLastTopAppUid = mLogInfo.mTopAppUID;
            mLogInfo.mTopAppName = null;
            mLogInfo.mTopAppUID = 0;
        } else if (mLogInfo.mTopAppName == null) {
            mLogInfo.mTopAppName = this.mLastTopAppName;
            mLogInfo.mTopAppUID = this.mLastTopAppUid;
        }
        mLogInfo.mBrightness = brightness;
    }

    public void notePowerInfoBatteryState(int plugType, int level) {
        mLogInfo.mChargeStatus = plugType;
        mLogInfo.mBatteryLevel = level;
    }

    public void notePowerInfoConnectionState(int dataType, boolean hasData) {
        mLogInfo.mConnectionStatus = dataType;
        mLogInfo.mDataConnection = hasData ? 1 : 0;
    }

    public void notePowerInfoSignalStrength(int strengthLevel) {
        mLogInfo.mSignalStrength = strengthLevel;
    }

    public void noteStartCamera() {
        mLogInfo.mCameraState = 1;
    }

    public void noteStopCamera() {
        mLogInfo.mCameraState = 0;
    }

    public void notePowerInfoWifiState(int supplState) {
        if (1 == supplState) {
            mLogInfo.mWifiStatus = 1;
        } else if (10 == supplState) {
            mLogInfo.mWifiStatus = 2;
        }
    }

    public void notePowerInfoGPSStatus(int status) {
        mLogInfo.mGPSStatus = status;
    }

    public void notePowerInfoTopApp(String packageName, int uid) {
        mLogInfo.mTopAppName = packageName;
        mLogInfo.mTopAppUID = uid;
    }

    public void notePowerInfoAcquireWakeLock(String name, int pid) {
        synchronized (mLock_wakelock) {
            WakeLock wakelock = new WakeLock();
            wakelock.mWakeLockName = name;
            wakelock.mWakeLockPID = pid;
            mLogInfo.mWakeLocks.add(wakelock);
            this.mWakeLockNumber++;
        }
    }

    public void notePowerInfoChangeWakeLock(String name, int pid, String newName, int newPid) {
        notePowerInfoReleaseWakeLock(name, pid);
        notePowerInfoAcquireWakeLock(newName, newPid);
    }

    public void notePowerInfoReleaseWakeLock(String name, int pid) {
        synchronized (mLock_wakelock) {
            for (WakeLock wakelock : mLogInfo.mWakeLocks) {
                if (name.equals(wakelock.mWakeLockName)) {
                    mLogInfo.mWakeLocks.remove(wakelock);
                    break;
                }
            }
            this.mWakeLockNumber--;
        }
    }

    public void notePowerInfoStartAlarm(String name, int uid) {
        if (name.charAt(1) == 'w') {
            name = name.substring(name.indexOf(58) + 1);
            if (mLogInfo.mAlarmName == null) {
                mLogInfo.mAlarmName = uid + "/" + name;
            } else {
                LogInfo logInfo = mLogInfo;
                logInfo.mAlarmName += " | " + uid + "/" + name;
            }
        }
    }

    public void notePowerInfoWakeupReason(String reason) {
        synchronized (mLock) {
            mLogInfo.mWakeupReason = reason;
            mLock.notifyAll();
        }
    }

    public void notePowerInfoSuspendState(boolean enable) {
        this.mSuspendState = enable;
    }

    private void getCpuInfo(LogInfo Data) {
        long now = SystemClock.uptimeMillis();
        this.mCpuTracker.update();
        String[] cpuInfoItem = this.mCpuTracker.printCurrentState(now).split("\n");
        if (cpuInfoItem.length > 5) {
            for (int i = 1; i < this.TOP_PROCESS_NUM + 1; i++) {
                int indexEnd = cpuInfoItem[i].indexOf("%");
                CpuTopLoad cpuTopLoad = (CpuTopLoad) Data.mCpuTopLoads.get(i - 1);
                cpuTopLoad.mCpuTopLoad = (int) Math.rint((double) Float.valueOf(cpuInfoItem[i].substring(2, indexEnd)).floatValue());
                int indexStart = indexEnd + 2;
                indexEnd = cpuInfoItem[i].indexOf("/", indexStart);
                cpuTopLoad = (CpuTopLoad) Data.mCpuTopLoads.get(i - 1);
                cpuTopLoad.mCpuTopPid = Integer.parseInt(cpuInfoItem[i].substring(indexStart, indexEnd));
                indexStart = indexEnd + 1;
                cpuTopLoad = (CpuTopLoad) Data.mCpuTopLoads.get(i - 1);
                cpuTopLoad.mCpuTopName = cpuInfoItem[i].substring(indexStart, cpuInfoItem[i].indexOf(":", indexStart));
            }
            int num = cpuInfoItem.length;
            String temp = cpuInfoItem[num - 1].substring(0, cpuInfoItem[num - 1].indexOf("%"));
            if (temp.contains("-")) {
                temp = temp.replace("-", "");
            }
            Data.mCpuTotalLoad = (int) Math.rint((double) Float.valueOf(temp).floatValue());
        }
    }

    public void noteShutdown() {
        Log.i(this.TAG, "System begin to shutdown,write the remainder to file");
        this.isThreadAlive = false;
    }
}

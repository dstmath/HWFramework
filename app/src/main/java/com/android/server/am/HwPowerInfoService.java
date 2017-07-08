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
import com.android.server.PPPOEStateMachine;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.com.android.server.policy.HwGlobalActionsData;
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
    private static String CONFIG_FILE = null;
    private static final int MAX_CPU_INDEX = 8;
    private static String PRODUCT_ETC_DIR = null;
    private static String SYSTEM_ETC_DIR = null;
    private static final boolean debugOn = true;
    private static Context mContext;
    private static final Object mLock;
    private static final Object mLockInit;
    private static final Object mLock_wakelock;
    private static LogInfo mLogInfo;
    private static PowerInfoServiceReceiver mReceiver;
    private static HwPowerInfoService mSingleInstance;
    private static int musicValume;
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
        }
    }

    static {
        SYSTEM_ETC_DIR = "/system/etc/";
        PRODUCT_ETC_DIR = "/product/etc/";
        CONFIG_FILE = "PowerInfoImpl.xml";
        mSingleInstance = null;
        mLock = new Object();
        mLockInit = new Object();
        mLock_wakelock = new Object();
        mContext = null;
        mReceiver = null;
        musicValume = 0;
    }

    private void parseNode(Node node) {
        if (node != null) {
            NodeList nodeList = node.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
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
                    for (int i = 0; i < nodeList.getLength(); i++) {
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
                    result.append(String.format("%-41s", new Object[]{"NA"}));
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
                    result.append(String.format("%-41s", new Object[]{"NA"}));
                    index++;
                }
            }
        }
        return result.toString();
    }

    private void getCurrentTime(LogInfo Data) {
        Data.mTime = this.dateFormate.format(new Date(System.currentTimeMillis()));
    }

    private void getDataFromWakeupSource(LogInfo Data) {
        InputStreamReader inputReader;
        Throwable th;
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        int counter = 0;
        try {
            FileInputStream fInputStream = new FileInputStream(this.WAKEUPSOURCE);
            try {
                inputReader = new InputStreamReader(fInputStream, "utf-8");
            } catch (FileNotFoundException e) {
                fileInputStream = fInputStream;
                try {
                    Log.e(this.TAG, "not found the wakeupsource");
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (Exception e2) {
                        }
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Exception e3) {
                        }
                    }
                    if (fileInputStream == null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e4) {
                            return;
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (Exception e5) {
                        }
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Exception e6) {
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e7) {
                        }
                    }
                    throw th;
                }
            } catch (UnsupportedEncodingException e8) {
                fileInputStream = fInputStream;
                Log.e(this.TAG, "not support utf-8");
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (Exception e9) {
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e10) {
                    }
                }
                if (fileInputStream == null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e11) {
                        return;
                    }
                }
            } catch (IOException e12) {
                fileInputStream = fInputStream;
                Log.e(this.TAG, "read wakeupsource failed");
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (Exception e13) {
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e14) {
                    }
                }
                if (fileInputStream == null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e15) {
                        return;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = fInputStream;
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
            try {
                BufferedReader read = new BufferedReader(inputReader);
                try {
                    int i;
                    for (String tempString = read.readLine(); tempString != null; tempString = read.readLine()) {
                        i = this.WAKEUPSOURCE_NUM;
                        if (counter >= r0) {
                            break;
                        }
                        if (tempString.startsWith("Active resource:")) {
                            WakeupSource wakeupSource;
                            int index = tempString.indexOf("\t");
                            String temp = tempString.substring(17, index);
                            if (temp.indexOf(" ") == -1) {
                                ((WakeupSource) Data.mWakeupSources.get(counter)).mWakeupSourceName = temp;
                            } else {
                                wakeupSource = (WakeupSource) Data.mWakeupSources.get(counter);
                                wakeupSource.mWakeupSourceName = temp.substring(0, temp.indexOf(" "));
                            }
                            index = tempString.indexOf("\t\t", index + 1);
                            for (int i2 = 0; i2 < 3; i2++) {
                                index = tempString.indexOf("\t\t", index + 2);
                            }
                            wakeupSource = (WakeupSource) Data.mWakeupSources.get(counter);
                            wakeupSource.mActiveTime = tempString.substring(index + 2, tempString.indexOf("\t\t", index + 2));
                            counter++;
                        }
                    }
                    while (true) {
                        i = this.WAKEUPSOURCE_NUM;
                        if (counter >= r0) {
                            break;
                        }
                        ((WakeupSource) Data.mWakeupSources.get(counter)).mWakeupSourceName = null;
                        ((WakeupSource) Data.mWakeupSources.get(counter)).mActiveTime = null;
                        counter++;
                    }
                    if (inputReader != null) {
                        try {
                            inputReader.close();
                        } catch (Exception e16) {
                        }
                    }
                    if (read != null) {
                        try {
                            read.close();
                        } catch (Exception e17) {
                        }
                    }
                    if (fInputStream != null) {
                        try {
                            fInputStream.close();
                        } catch (IOException e18) {
                        }
                    }
                } catch (FileNotFoundException e19) {
                    bufferedReader = read;
                    inputStreamReader = inputReader;
                    fileInputStream = fInputStream;
                } catch (UnsupportedEncodingException e20) {
                    bufferedReader = read;
                    inputStreamReader = inputReader;
                    fileInputStream = fInputStream;
                } catch (IOException e21) {
                    bufferedReader = read;
                    inputStreamReader = inputReader;
                    fileInputStream = fInputStream;
                } catch (Throwable th4) {
                    th = th4;
                    bufferedReader = read;
                    inputStreamReader = inputReader;
                    fileInputStream = fInputStream;
                }
            } catch (FileNotFoundException e22) {
                inputStreamReader = inputReader;
                fileInputStream = fInputStream;
                Log.e(this.TAG, "not found the wakeupsource");
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream == null) {
                    fileInputStream.close();
                }
            } catch (UnsupportedEncodingException e23) {
                inputStreamReader = inputReader;
                fileInputStream = fInputStream;
                Log.e(this.TAG, "not support utf-8");
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream == null) {
                    fileInputStream.close();
                }
            } catch (IOException e24) {
                inputStreamReader = inputReader;
                fileInputStream = fInputStream;
                Log.e(this.TAG, "read wakeupsource failed");
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream == null) {
                    fileInputStream.close();
                }
            } catch (Throwable th5) {
                th = th5;
                inputStreamReader = inputReader;
                fileInputStream = fInputStream;
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e25) {
            Log.e(this.TAG, "not found the wakeupsource");
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (fileInputStream == null) {
                fileInputStream.close();
            }
        } catch (UnsupportedEncodingException e26) {
            Log.e(this.TAG, "not support utf-8");
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (fileInputStream == null) {
                fileInputStream.close();
            }
        } catch (IOException e27) {
            Log.e(this.TAG, "read wakeupsource failed");
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (fileInputStream == null) {
                fileInputStream.close();
            }
        }
    }

    private String getOneLineString(String path) {
        Throwable th;
        if (path == null) {
            return null;
        }
        String tempString = null;
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            FileInputStream fInputStream = new FileInputStream(path);
            try {
                InputStreamReader inputReader = new InputStreamReader(fInputStream, "utf-8");
                try {
                    BufferedReader bReader = new BufferedReader(inputReader);
                    try {
                        tempString = bReader.readLine();
                        if (bReader != null) {
                            try {
                                bReader.close();
                            } catch (IOException e) {
                            }
                        }
                        if (inputReader != null) {
                            try {
                                inputReader.close();
                            } catch (Exception e2) {
                            }
                        }
                        if (fInputStream != null) {
                            try {
                                fInputStream.close();
                            } catch (IOException e3) {
                            }
                        }
                        fileInputStream = fInputStream;
                    } catch (FileNotFoundException e4) {
                        bufferedReader = bReader;
                        inputStreamReader = inputReader;
                        fileInputStream = fInputStream;
                        Log.e(this.TAG, "not found the " + path);
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e5) {
                            }
                        }
                        if (inputStreamReader != null) {
                            try {
                                inputStreamReader.close();
                            } catch (Exception e6) {
                            }
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e7) {
                            }
                        }
                        return tempString;
                    } catch (UnsupportedEncodingException e8) {
                        bufferedReader = bReader;
                        inputStreamReader = inputReader;
                        fileInputStream = fInputStream;
                        Log.e(this.TAG, "not support utf-8");
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e9) {
                            }
                        }
                        if (inputStreamReader != null) {
                            try {
                                inputStreamReader.close();
                            } catch (Exception e10) {
                            }
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e11) {
                            }
                        }
                        return tempString;
                    } catch (IOException e12) {
                        bufferedReader = bReader;
                        inputStreamReader = inputReader;
                        fileInputStream = fInputStream;
                        try {
                            Log.e(this.TAG, "read " + path + " failed");
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (IOException e13) {
                                }
                            }
                            if (inputStreamReader != null) {
                                try {
                                    inputStreamReader.close();
                                } catch (Exception e14) {
                                }
                            }
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e15) {
                                }
                            }
                            return tempString;
                        } catch (Throwable th2) {
                            th = th2;
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (IOException e16) {
                                }
                            }
                            if (inputStreamReader != null) {
                                try {
                                    inputStreamReader.close();
                                } catch (Exception e17) {
                                }
                            }
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e18) {
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        bufferedReader = bReader;
                        inputStreamReader = inputReader;
                        fileInputStream = fInputStream;
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
                } catch (FileNotFoundException e19) {
                    inputStreamReader = inputReader;
                    fileInputStream = fInputStream;
                    Log.e(this.TAG, "not found the " + path);
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    return tempString;
                } catch (UnsupportedEncodingException e20) {
                    inputStreamReader = inputReader;
                    fileInputStream = fInputStream;
                    Log.e(this.TAG, "not support utf-8");
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    return tempString;
                } catch (IOException e21) {
                    inputStreamReader = inputReader;
                    fileInputStream = fInputStream;
                    Log.e(this.TAG, "read " + path + " failed");
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    return tempString;
                } catch (Throwable th4) {
                    th = th4;
                    inputStreamReader = inputReader;
                    fileInputStream = fInputStream;
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
            } catch (FileNotFoundException e22) {
                fileInputStream = fInputStream;
                Log.e(this.TAG, "not found the " + path);
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return tempString;
            } catch (UnsupportedEncodingException e23) {
                fileInputStream = fInputStream;
                Log.e(this.TAG, "not support utf-8");
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return tempString;
            } catch (IOException e24) {
                fileInputStream = fInputStream;
                Log.e(this.TAG, "read " + path + " failed");
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return tempString;
            } catch (Throwable th5) {
                th = th5;
                fileInputStream = fInputStream;
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
        } catch (FileNotFoundException e25) {
            Log.e(this.TAG, "not found the " + path);
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return tempString;
        } catch (UnsupportedEncodingException e26) {
            Log.e(this.TAG, "not support utf-8");
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return tempString;
        } catch (IOException e27) {
            Log.e(this.TAG, "read " + path + " failed");
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return tempString;
        }
        return tempString;
    }

    private void getCpuFreq() {
        String oneLineString;
        int i = 0;
        while (i < 4 && !new File(this.CPU_FREQ_HEAD + i + this.CPU_FREQ_TAIL).exists()) {
            i++;
        }
        mLogInfo.mCPU0Freq = i < 4 ? getOneLineString(this.CPU_FREQ_HEAD + i + this.CPU_FREQ_TAIL) : "NA";
        mLogInfo.mCPU0Freq_Max = i < 4 ? getOneLineString(this.CPU_FREQ_HEAD + i + this.CPU_MAX_FREQ_TAIL) : "NA";
        i = 4;
        while (i < MAX_CPU_INDEX && !new File(this.CPU_FREQ_HEAD + i + this.CPU_FREQ_TAIL).exists()) {
            i++;
        }
        mLogInfo.mCPU4Freq = i < MAX_CPU_INDEX ? getOneLineString(this.CPU_FREQ_HEAD + i + this.CPU_FREQ_TAIL) : "NA";
        LogInfo logInfo = mLogInfo;
        if (i < MAX_CPU_INDEX) {
            oneLineString = getOneLineString(this.CPU_FREQ_HEAD + i + this.CPU_MAX_FREQ_TAIL);
        } else {
            oneLineString = "NA";
        }
        logInfo.mCPU4Freq_Max = oneLineString;
    }

    private void beginWriteFile() {
        getCurrentTime(mLogInfo);
        mLogInfo.mCPUOnLine = getOneLineString(this.CPU_ONLINE);
        getCpuFreq();
        String s = getOneLineString(this.CURRENT);
        LogInfo logInfo = mLogInfo;
        if (s == null) {
            s = PPPOEStateMachine.PHASE_DEAD;
        }
        logInfo.mCurrent = s;
        s = getOneLineString(this.CURRENT_LIMIT);
        logInfo = mLogInfo;
        if (s == null) {
            s = PPPOEStateMachine.PHASE_DEAD;
        }
        logInfo.mCurrentLimit = s;
        s = getOneLineString(this.SOC_RM);
        logInfo = mLogInfo;
        if (s == null) {
            s = "NA";
        }
        logInfo.mSOC_rm = s;
        s = getOneLineString(this.CPU0_TEMP);
        logInfo = mLogInfo;
        if (s == null) {
            s = PPPOEStateMachine.PHASE_DEAD;
        }
        logInfo.mCPU0Temp = s;
        s = getOneLineString(this.CPU1_TEMP);
        logInfo = mLogInfo;
        if (s == null) {
            s = PPPOEStateMachine.PHASE_DEAD;
        }
        logInfo.mCPU1Temp = s;
        s = getOneLineString(this.BOARD_TEMP);
        logInfo = mLogInfo;
        if (s == null) {
            s = PPPOEStateMachine.PHASE_DEAD;
        }
        logInfo.mBoardTemp = s;
        s = getOneLineString(this.PA_TEMP);
        logInfo = mLogInfo;
        if (s == null) {
            s = PPPOEStateMachine.PHASE_DEAD;
        }
        logInfo.mPA_temp = s;
        s = getOneLineString(this.BAT_TEMP);
        logInfo = mLogInfo;
        if (s == null) {
            s = PPPOEStateMachine.PHASE_DEAD;
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
        String tempString;
        StringBuffer writeData = new StringBuffer();
        writeData.append(String.format("%-26s", new Object[]{Data.mTime})).append(String.format("%-7d", new Object[]{Integer.valueOf(Data.mBrightness)})).append(String.format("%-5d", new Object[]{Integer.valueOf(Data.mBatteryLevel)}));
        writeData.append(String.format("%-7s", new Object[]{Data.mSOC_rm})).append(String.format("%-10s", new Object[]{Data.mCurrent})).append(String.format("%-10s", new Object[]{Data.mCurrentLimit})).append(String.format("%-9d", new Object[]{Integer.valueOf(Data.mChargeStatus)}));
        writeData.append(String.format("%-6s", new Object[]{SystemProperties.get("persist.sys.logsystem.modem", "NA")})).append(String.format("%-11d", new Object[]{Integer.valueOf(Data.mConnectionStatus)})).append(String.format("%-15d", new Object[]{Integer.valueOf(Data.mSignalStrength)}));
        writeData.append(String.format("%-15d", new Object[]{Integer.valueOf(Data.mDataConnection)})).append(String.format("%-5d", new Object[]{Integer.valueOf(Data.mWifiStatus)})).append(String.format("%-4d", new Object[]{Integer.valueOf(Data.mBTState)})).append(String.format("%-4d", new Object[]{Integer.valueOf(Data.mGPSStatus)}));
        writeData.append(String.format("%-7d", new Object[]{Integer.valueOf(Data.mCameraState)})).append(String.format("%-4d", new Object[]{Integer.valueOf(Data.mNFCOn)})).append(String.format("%-8d", new Object[]{Integer.valueOf(Data.mHeadSet)})).append(String.format("%-9d", new Object[]{Integer.valueOf(musicValume)}));
        if (Data.mTopAppName == null) {
            tempString = "NA";
        } else {
            tempString = Data.mTopAppUID + "/" + Data.mTopAppName;
        }
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
                writeData.append(String.format("%-46s", new Object[]{"NA"}));
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
        tempString = Data.mWakeupReason == null ? "NA" : Data.mWakeupReason;
        if (tempString.length() > 70) {
            tempString = tempString.substring(0, 70);
        }
        writeData.append(String.format("%-10s", new Object[]{Data.mCPU0Freq})).append(String.format("%-10s", new Object[]{Data.mCPU0Freq_Max})).append(String.format("%-10s", new Object[]{Data.mCPU4Freq}));
        writeData.append(String.format("%-10s", new Object[]{Data.mCPU4Freq_Max})).append(String.format("%-10s", new Object[]{Data.mCPUOnLine})).append(String.format("%-71s", new Object[]{tempString})).append(Data.mAlarmName == null ? "NA" : Data.mAlarmName);
        printWriter.println(writeData.toString());
    }

    private void writeFile() {
        Throwable th;
        int flag = 0;
        File powerinfologtxt = new File(Environment.getDataDirectory(), this.BIGZIPFILEPATH + File.separator + this.logFileName);
        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        PrintWriter printWriter = null;
        try {
            if (!(powerinfologtxt.getParentFile().exists() || powerinfologtxt.getParentFile().mkdirs())) {
                Log.w(this.TAG, "fail to create dir");
            }
            if (!powerinfologtxt.exists()) {
                if (!powerinfologtxt.createNewFile()) {
                    Log.w(this.TAG, "fail to create new file");
                }
                powerinfologtxt.setReadable(debugOn, false);
                flag = 1;
            }
            FileOutputStream fOutputStream = new FileOutputStream(powerinfologtxt, debugOn);
            try {
                OutputStreamWriter outWriter = new OutputStreamWriter(fOutputStream, "utf-8");
                try {
                    PrintWriter pw = new PrintWriter(outWriter);
                    if (flag == 1) {
                        try {
                            writeTitleToFile(pw);
                        } catch (FileNotFoundException e) {
                            printWriter = pw;
                            outputStreamWriter = outWriter;
                            fileOutputStream = fOutputStream;
                            Log.e(this.TAG, "not found log file");
                            if (printWriter != null) {
                                printWriter.close();
                            }
                            if (outputStreamWriter != null) {
                                try {
                                    outputStreamWriter.close();
                                } catch (IOException e2) {
                                }
                            }
                            if (fileOutputStream == null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e3) {
                                    return;
                                }
                            }
                        } catch (UnsupportedEncodingException e4) {
                            printWriter = pw;
                            outputStreamWriter = outWriter;
                            fileOutputStream = fOutputStream;
                            Log.e(this.TAG, "not support utf-8");
                            if (printWriter != null) {
                                printWriter.close();
                            }
                            if (outputStreamWriter != null) {
                                try {
                                    outputStreamWriter.close();
                                } catch (IOException e5) {
                                }
                            }
                            if (fileOutputStream == null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e6) {
                                    return;
                                }
                            }
                        } catch (IOException e7) {
                            printWriter = pw;
                            outputStreamWriter = outWriter;
                            fileOutputStream = fOutputStream;
                            try {
                                Log.e(this.TAG, "IOException.");
                                if (printWriter != null) {
                                    printWriter.close();
                                }
                                if (outputStreamWriter != null) {
                                    try {
                                        outputStreamWriter.close();
                                    } catch (IOException e8) {
                                    }
                                }
                                if (fileOutputStream == null) {
                                    try {
                                        fileOutputStream.close();
                                    } catch (IOException e9) {
                                        return;
                                    }
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                if (printWriter != null) {
                                    printWriter.close();
                                }
                                if (outputStreamWriter != null) {
                                    try {
                                        outputStreamWriter.close();
                                    } catch (IOException e10) {
                                    }
                                }
                                if (fileOutputStream != null) {
                                    try {
                                        fileOutputStream.close();
                                    } catch (IOException e11) {
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            printWriter = pw;
                            outputStreamWriter = outWriter;
                            fileOutputStream = fOutputStream;
                            if (printWriter != null) {
                                printWriter.close();
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
                    writeDataToFile(pw, mLogInfo);
                    pw.flush();
                    if (pw != null) {
                        pw.close();
                    }
                    if (outWriter != null) {
                        try {
                            outWriter.close();
                        } catch (IOException e12) {
                        }
                    }
                    if (fOutputStream != null) {
                        try {
                            fOutputStream.close();
                        } catch (IOException e13) {
                        }
                    }
                    fileOutputStream = fOutputStream;
                } catch (FileNotFoundException e14) {
                    outputStreamWriter = outWriter;
                    fileOutputStream = fOutputStream;
                    Log.e(this.TAG, "not found log file");
                    if (printWriter != null) {
                        printWriter.close();
                    }
                    if (outputStreamWriter != null) {
                        outputStreamWriter.close();
                    }
                    if (fileOutputStream == null) {
                        fileOutputStream.close();
                    }
                } catch (UnsupportedEncodingException e15) {
                    outputStreamWriter = outWriter;
                    fileOutputStream = fOutputStream;
                    Log.e(this.TAG, "not support utf-8");
                    if (printWriter != null) {
                        printWriter.close();
                    }
                    if (outputStreamWriter != null) {
                        outputStreamWriter.close();
                    }
                    if (fileOutputStream == null) {
                        fileOutputStream.close();
                    }
                } catch (IOException e16) {
                    outputStreamWriter = outWriter;
                    fileOutputStream = fOutputStream;
                    Log.e(this.TAG, "IOException.");
                    if (printWriter != null) {
                        printWriter.close();
                    }
                    if (outputStreamWriter != null) {
                        outputStreamWriter.close();
                    }
                    if (fileOutputStream == null) {
                        fileOutputStream.close();
                    }
                } catch (Throwable th4) {
                    th = th4;
                    outputStreamWriter = outWriter;
                    fileOutputStream = fOutputStream;
                    if (printWriter != null) {
                        printWriter.close();
                    }
                    if (outputStreamWriter != null) {
                        outputStreamWriter.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e17) {
                fileOutputStream = fOutputStream;
                Log.e(this.TAG, "not found log file");
                if (printWriter != null) {
                    printWriter.close();
                }
                if (outputStreamWriter != null) {
                    outputStreamWriter.close();
                }
                if (fileOutputStream == null) {
                    fileOutputStream.close();
                }
            } catch (UnsupportedEncodingException e18) {
                fileOutputStream = fOutputStream;
                Log.e(this.TAG, "not support utf-8");
                if (printWriter != null) {
                    printWriter.close();
                }
                if (outputStreamWriter != null) {
                    outputStreamWriter.close();
                }
                if (fileOutputStream == null) {
                    fileOutputStream.close();
                }
            } catch (IOException e19) {
                fileOutputStream = fOutputStream;
                Log.e(this.TAG, "IOException.");
                if (printWriter != null) {
                    printWriter.close();
                }
                if (outputStreamWriter != null) {
                    outputStreamWriter.close();
                }
                if (fileOutputStream == null) {
                    fileOutputStream.close();
                }
            } catch (Throwable th5) {
                th = th5;
                fileOutputStream = fOutputStream;
                if (printWriter != null) {
                    printWriter.close();
                }
                if (outputStreamWriter != null) {
                    outputStreamWriter.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e20) {
            Log.e(this.TAG, "not found log file");
            if (printWriter != null) {
                printWriter.close();
            }
            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }
            if (fileOutputStream == null) {
                fileOutputStream.close();
            }
        } catch (UnsupportedEncodingException e21) {
            Log.e(this.TAG, "not support utf-8");
            if (printWriter != null) {
                printWriter.close();
            }
            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }
            if (fileOutputStream == null) {
                fileOutputStream.close();
            }
        } catch (IOException e22) {
            Log.e(this.TAG, "IOException.");
            if (printWriter != null) {
                printWriter.close();
            }
            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }
            if (fileOutputStream == null) {
                fileOutputStream.close();
            }
        }
    }

    private void zipFile(long num) {
        FileInputStream in;
        Throwable th;
        File dataDirectory = Environment.getDataDirectory();
        String str = this.BIGZIPFILEPATH;
        str = this.logFileName;
        File file = new File(dataDirectory, r0 + File.separator + r0 + "-" + this.zipFileMax + ".tar.gz");
        if (file.exists() && !file.delete()) {
            Log.w(this.TAG, "delete zip file failed! ");
        }
        for (int i = this.zipFileMax - 1; i >= 0; i--) {
            dataDirectory = Environment.getDataDirectory();
            str = this.BIGZIPFILEPATH;
            file = new File(dataDirectory, r0 + File.separator + this.logFileName + "-" + i + ".tar.gz");
            if (file.exists()) {
                int j = i + 1;
                str = this.BIGZIPFILEPATH;
                if (!file.renameTo(new File(Environment.getDataDirectory(), r0 + File.separator + this.logFileName + "-" + j + ".tar.gz"))) {
                    Log.w(this.TAG, "failed rename file! ");
                }
            }
        }
        dataDirectory = Environment.getDataDirectory();
        str = this.BIGZIPFILEPATH;
        File powerLogZipBig = new File(dataDirectory, r0 + File.separator + this.logFileName + "-0" + ".tar.gz");
        if (powerLogZipBig.exists()) {
            this.hasCreateBigZip = 1;
            return;
        }
        FileOutputStream fileOutputStream = null;
        ZipOutputStream zipOutputStream = null;
        FileInputStream fileInputStream = null;
        try {
            ZipOutputStream out;
            byte[] buf = new byte[HwGlobalActionsData.FLAG_SILENTMODE_NORMAL];
            FileOutputStream fOutputStream = new FileOutputStream(powerLogZipBig);
            try {
                out = new ZipOutputStream(fOutputStream);
                try {
                    dataDirectory = Environment.getDataDirectory();
                    str = this.BIGZIPFILEPATH;
                    file = new File(dataDirectory, r0 + File.separator + this.logFileName);
                    if (!file.exists()) {
                        Log.w(this.TAG, "No txt file to zip ");
                    }
                    dataDirectory = Environment.getDataDirectory();
                    str = this.BIGZIPFILEPATH;
                    file = new File(dataDirectory, r0 + File.separator + this.logFileName + "-0");
                    if (!file.renameTo(file)) {
                        Log.w(this.TAG, "failed rename file! ");
                    }
                    in = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    zipOutputStream = out;
                    fileOutputStream = fOutputStream;
                    try {
                        Log.e(this.TAG, "not found file when ZIP file");
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e2) {
                            }
                        }
                        if (zipOutputStream != null) {
                            try {
                                zipOutputStream.close();
                            } catch (IOException e3) {
                            }
                        }
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e4) {
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e5) {
                            }
                        }
                        if (zipOutputStream != null) {
                            try {
                                zipOutputStream.close();
                            } catch (IOException e6) {
                            }
                        }
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e7) {
                            }
                        }
                        throw th;
                    }
                } catch (IOException e8) {
                    zipOutputStream = out;
                    fileOutputStream = fOutputStream;
                    Log.e(this.TAG, "zip file failed");
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e9) {
                        }
                    }
                    if (zipOutputStream != null) {
                        try {
                            zipOutputStream.close();
                        } catch (IOException e10) {
                        }
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e11) {
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    zipOutputStream = out;
                    fileOutputStream = fOutputStream;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (zipOutputStream != null) {
                        zipOutputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e12) {
                fileOutputStream = fOutputStream;
                Log.e(this.TAG, "not found file when ZIP file");
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (zipOutputStream != null) {
                    zipOutputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e13) {
                fileOutputStream = fOutputStream;
                Log.e(this.TAG, "zip file failed");
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (zipOutputStream != null) {
                    zipOutputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (Throwable th4) {
                th = th4;
                fileOutputStream = fOutputStream;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (zipOutputStream != null) {
                    zipOutputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
            try {
                out.putNextEntry(new ZipEntry(file.getName()));
                while (true) {
                    int len = in.read(buf);
                    if (len <= 0) {
                        break;
                    }
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                if (!file.delete()) {
                    Log.w(this.TAG, "delete powerLog file failed!");
                }
                powerLogZipBig.setReadable(debugOn, false);
                this.hasCreateBigZip = 1;
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e14) {
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e15) {
                    }
                }
                if (fOutputStream != null) {
                    try {
                        fOutputStream.close();
                    } catch (IOException e16) {
                    }
                }
            } catch (FileNotFoundException e17) {
                fileInputStream = in;
                zipOutputStream = out;
                fileOutputStream = fOutputStream;
                Log.e(this.TAG, "not found file when ZIP file");
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (zipOutputStream != null) {
                    zipOutputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e18) {
                fileInputStream = in;
                zipOutputStream = out;
                fileOutputStream = fOutputStream;
                Log.e(this.TAG, "zip file failed");
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (zipOutputStream != null) {
                    zipOutputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (Throwable th5) {
                th = th5;
                fileInputStream = in;
                zipOutputStream = out;
                fileOutputStream = fOutputStream;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (zipOutputStream != null) {
                    zipOutputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e19) {
            Log.e(this.TAG, "not found file when ZIP file");
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (zipOutputStream != null) {
                zipOutputStream.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (IOException e20) {
            Log.e(this.TAG, "zip file failed");
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (zipOutputStream != null) {
                zipOutputStream.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
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
        this.CPU0_TEMP = AppHibernateCst.INVALID_PKG;
        this.CPU1_TEMP = AppHibernateCst.INVALID_PKG;
        this.BOARD_TEMP = AppHibernateCst.INVALID_PKG;
        this.PA_TEMP = AppHibernateCst.INVALID_PKG;
        this.BAT_TEMP = AppHibernateCst.INVALID_PKG;
        this.CPU_ONLINE = "/sys/devices/system/cpu/online";
        this.WAKEUPSOURCE = "/sys/kernel/debug/wakeup_sources";
        this.CURRENT = AppHibernateCst.INVALID_PKG;
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
        this.isThreadAlive = debugOn;
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
            } else if (mLogInfo.mAlarmName.length() < WifiProCommonUtils.HTTP_REACHALBE_HOME) {
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
                temp = temp.replace("-", AppHibernateCst.INVALID_PKG);
            }
            Data.mCpuTotalLoad = (int) Math.rint((double) Float.valueOf(temp).floatValue());
        }
    }

    public void noteShutdown() {
        Log.i(this.TAG, "System begin to shutdown,write the remainder to file");
        this.isThreadAlive = false;
    }
}

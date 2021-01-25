package com.android.server.wifi;

import android.net.MacAddress;
import android.net.wifi.IWifiActionListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SELinux;
import android.text.TextUtils;
import android.util.wifi.HwHiSLog;
import com.android.server.wifi.HwQoE.HwQoEService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class HwQueryCsiSnifferArp {
    private static final Object LOCK = new Object();
    private static final String PERMISSION = "com.huawei.permission.QUERY_WIFI_SNIFFER_CSI_ARP";
    private static final String TAG = "HwQueryCsiSnifferArp";
    private static final String[] WHITELIST = {"com.huawei.hwfindcamera"};
    private static HwQueryCsiSnifferArp instance = null;
    private final String ARP_FILE = "/proc/net/arp";
    private final String CSI_COMMAND_FILE = "/sys/hisys/hipriv";
    private final String CSI_FILE = "/data/log/location/wlan0/CSI.TXT";
    private final String CSI_LOCATION = "/data/log/location/";
    private final String CSI_WLAN_DIR = "/data/log/location/wlan0/";
    private final int FILE_RW_TIMEOUT_LONG = 1400;
    private final int FILE_RW_TIMEOUT_SHORT = 50;
    private final int MAX_SNIFFER_SIZE = 209715200;
    private final int MSG_CSI_START = 100;
    private final int MSG_CSI_STOP = 101;
    private final int MSG_DEAD_BINDER = 300;
    private final int MSG_SNIFF_START = HwQoEService.KOG_LATENCY_TIME_THRESHOLD;
    private final int MSG_SNIFF_STOP = 201;
    private final String SNIFFER_FILE = "/proc/sys/net/ipv4/net_sniffer";
    private final int SNIFF_CMD_BEGIN = 0;
    private final int SNIFF_CMD_END = 2;
    private final int SNIFF_CMD_GETSIZE = 2;
    private final int SNIFF_CMD_START = 0;
    private final int SNIFF_CMD_STOP = 1;
    private final int SNIFF_STATE_START = 2;
    private final int SNIFF_STATE_STOP = 0;
    private List<AppDeathRecipient> aliveBinderList = new ArrayList();
    private Handler asyncWorkHandler = null;
    private volatile int csiBwMask = 0;
    private volatile int csiFrMask = 0;
    private volatile List<MacAddress> csiTargetList = new ArrayList();
    private final Object handlerLock = new Object();
    private volatile boolean isCsiLocCreated = false;
    private volatile boolean isCsiStarted = false;
    private volatile boolean isCsiWlanCreated = false;
    private volatile boolean isSnifferStarted = false;
    private final Object recipientLock = new Object();
    private final int releaseDelay = 120000;
    private Runnable releaseRunnable = null;
    private volatile String sniffChannel = "";
    private volatile MacAddress sniffTarget = null;
    private volatile boolean snifferFirstSet = true;
    private HwWifiStateMachine wifiStateMachine;

    /* access modifiers changed from: package-private */
    public class CsiMessage {
        public IWifiActionListener actionListener;
        public int bandwidthMask;
        public int frameTypemask;
        public int pid;
        public List<MacAddress> targets;

        public CsiMessage(List<MacAddress> taLists, int mask, IWifiActionListener listener, int callingPid) {
            this.targets = taLists;
            this.frameTypemask = mask & 255;
            this.bandwidthMask = (mask >> 8) & 255;
            this.actionListener = listener;
            this.pid = callingPid;
        }
    }

    /* access modifiers changed from: package-private */
    public class SniffMessage {
        public IWifiActionListener actionListener;
        public int pid;
        public String targetChannel;
        public MacAddress targetMac;

        public SniffMessage(MacAddress mac, String channel, IWifiActionListener listener, int callingPid) {
            this.targetMac = mac;
            this.targetChannel = channel;
            this.actionListener = listener;
            this.pid = callingPid;
        }
    }

    /* access modifiers changed from: private */
    public final class AppDeathRecipient implements IBinder.DeathRecipient {
        public final IBinder binder;
        public final int pid;

        AppDeathRecipient(IBinder callingIbinder, int callingPid) {
            this.binder = callingIbinder;
            this.pid = callingPid;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (HwQueryCsiSnifferArp.this.recipientLock) {
                HwHiSLog.w(HwQueryCsiSnifferArp.TAG, false, "Binder Died", new Object[0]);
                int index = HwQueryCsiSnifferArp.this.findRecipientIndex(this.pid);
                if (index < 0) {
                    HwHiSLog.w(HwQueryCsiSnifferArp.TAG, false, "AppDeathRecipient: cannot find binder", new Object[0]);
                    return;
                }
                HwQueryCsiSnifferArp.this.aliveBinderList.remove(index);
                Message msg = Message.obtain();
                msg.what = 300;
                msg.obj = null;
                HwQueryCsiSnifferArp.this.sendMsg(msg);
            }
        }
    }

    public static HwQueryCsiSnifferArp getInstance(HwWifiStateMachine stateMachine) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new HwQueryCsiSnifferArp(stateMachine);
                }
            }
        }
        return instance;
    }

    private HwQueryCsiSnifferArp(HwWifiStateMachine stateMachine) {
        this.wifiStateMachine = stateMachine;
    }

    public boolean isInWhiteList(String packageName) {
        for (String whitePackageName : WHITELIST) {
            if (whitePackageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public String getPermission() {
        return PERMISSION;
    }

    public synchronized int queryCsi(boolean enable, List<MacAddress> taList, int mask, IWifiActionListener actionListener, int callingPid) {
        if (!(taList.size() == 0 || actionListener == null)) {
            if (checkCsiPrepard()) {
                if (!addNewRecipient(actionListener.asBinder(), callingPid)) {
                    return -1;
                }
                Message msg = Message.obtain();
                msg.what = enable ? 100 : 101;
                msg.obj = new CsiMessage(taList, mask, actionListener, callingPid);
                if (sendMsg(msg)) {
                    return 0;
                }
                return -1;
            }
        }
        return -1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x00af  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00c1  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00ca A[SYNTHETIC, Splitter:B:50:0x00ca] */
    public String getArpByIp(List<String> ipList) {
        String ret = "";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            StringBuffer sb = new StringBuffer();
            while (true) {
                int readValue = br.read();
                if (readValue == -1) {
                    break;
                }
                char word = (char) readValue;
                sb.append(word);
                if (sb.length() >= 300) {
                    break;
                } else if (word == '\n') {
                    String line = sb.toString();
                    if (8192 < ret.length() + line.length() + 1) {
                        break;
                    }
                    try {
                        if (findArpByIp(line, ipList)) {
                            ret = ret + line;
                        }
                        try {
                            sb.delete(0, sb.length());
                        } catch (StringIndexOutOfBoundsException e) {
                            HwHiSLog.e(TAG, false, "handleQueryArp error : outofbounds", new Object[0]);
                        }
                    } catch (FileNotFoundException e2) {
                        HwHiSLog.e(TAG, false, "handleQueryArp error : filenotfound", new Object[0]);
                        if (br != null) {
                            br.close();
                        }
                        return ret;
                    } catch (IOException e3) {
                        try {
                            HwHiSLog.e(TAG, false, "handleQueryArp error : file read exception", new Object[0]);
                            if (br != null) {
                                br.close();
                            }
                            return ret;
                        } catch (Throwable th) {
                            e = th;
                            if (br != null) {
                                try {
                                    br.close();
                                } catch (IOException e4) {
                                    HwHiSLog.e(TAG, false, "handleQueryArp error : close reader exception", new Object[0]);
                                }
                            }
                            throw e;
                        }
                    }
                }
            }
            try {
                br.close();
            } catch (IOException e5) {
                HwHiSLog.e(TAG, false, "handleQueryArp error : close reader exception", new Object[0]);
            }
        } catch (FileNotFoundException e6) {
            HwHiSLog.e(TAG, false, "handleQueryArp error : filenotfound", new Object[0]);
            if (br != null) {
            }
            return ret;
        } catch (IOException e7) {
            HwHiSLog.e(TAG, false, "handleQueryArp error : file read exception", new Object[0]);
            if (br != null) {
            }
            return ret;
        } catch (Throwable th2) {
            e = th2;
            if (br != null) {
            }
            throw e;
        }
        return ret;
    }

    public synchronized int querySniffer(int cmdId, MacAddress filterMac, String channel, IWifiActionListener actionListener, int callingPid) {
        if (cmdId >= 0 && cmdId <= 2 && filterMac != null) {
            if (!TextUtils.isEmpty(channel)) {
                if (actionListener != null) {
                    if (cmdId != 0) {
                        if (cmdId != 1) {
                            return getSnifferSize();
                        }
                    }
                    if (!addNewRecipient(actionListener.asBinder(), callingPid)) {
                        return -1;
                    }
                    Message msg = Message.obtain();
                    msg.what = cmdId == 0 ? HwQoEService.KOG_LATENCY_TIME_THRESHOLD : 201;
                    msg.obj = new SniffMessage(filterMac, channel, actionListener, callingPid);
                    if (sendMsg(msg)) {
                        return 0;
                    }
                    return -1;
                }
            }
        }
        return -1;
    }

    private void createHandler() {
        HandlerThread handlerThread = new HandlerThread("HwQueryCsiSnifferArpThread");
        try {
            handlerThread.start();
            this.asyncWorkHandler = new Handler(handlerThread.getLooper()) {
                /* class com.android.server.wifi.HwQueryCsiSnifferArp.AnonymousClass1 */

                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    int i = msg.what;
                    if (i != 100) {
                        if (i != 101) {
                            if (i != 200) {
                                if (i != 201) {
                                    if (i != 300) {
                                        HwHiSLog.w(HwQueryCsiSnifferArp.TAG, false, "Unknown message : %{public}d", new Object[]{Integer.valueOf(msg.what)});
                                        return;
                                    }
                                    HwHiSLog.d(HwQueryCsiSnifferArp.TAG, false, "CALL MSG_DEAD_BINDER", new Object[0]);
                                    HwQueryCsiSnifferArp.this.stopAliveCsiSniff();
                                } else if (msg.obj instanceof SniffMessage) {
                                    HwQueryCsiSnifferArp.this.stopSniff((SniffMessage) msg.obj);
                                }
                            } else if (msg.obj instanceof SniffMessage) {
                                HwQueryCsiSnifferArp.this.startSniff((SniffMessage) msg.obj);
                            }
                        } else if (msg.obj instanceof CsiMessage) {
                            HwQueryCsiSnifferArp.this.stopCsi((CsiMessage) msg.obj);
                        }
                    } else if (msg.obj instanceof CsiMessage) {
                        HwQueryCsiSnifferArp.this.startCsi((CsiMessage) msg.obj);
                    }
                }
            };
        } catch (IllegalThreadStateException e) {
            HwHiSLog.e(TAG, false, "Handler error: IllegalThreadStateException", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean sendMsg(Message msg) {
        synchronized (this.handlerLock) {
            if (this.asyncWorkHandler == null) {
                createHandler();
            }
            if (this.asyncWorkHandler == null) {
                HwHiSLog.e(TAG, false, "Failed to create work thread", new Object[0]);
                return false;
            }
            if (this.releaseRunnable != null) {
                this.asyncWorkHandler.removeCallbacks(this.releaseRunnable);
            }
            this.releaseRunnable = new Runnable() {
                /* class com.android.server.wifi.HwQueryCsiSnifferArp.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    synchronized (HwQueryCsiSnifferArp.this.handlerLock) {
                        if (HwQueryCsiSnifferArp.this.asyncWorkHandler != null) {
                            HwQueryCsiSnifferArp.this.asyncWorkHandler.getLooper().quit();
                            HwQueryCsiSnifferArp.this.asyncWorkHandler = null;
                        }
                        HwQueryCsiSnifferArp.this.releaseRunnable = null;
                        HwQueryCsiSnifferArp.this.finishWorkThread();
                    }
                }
            };
            if (this.releaseRunnable == null) {
                HwHiSLog.e(TAG, false, "Failed to create release runnable", new Object[0]);
                return false;
            }
            this.asyncWorkHandler.postDelayed(this.releaseRunnable, 120000);
            return this.asyncWorkHandler.sendMessage(msg);
        }
    }

    private void notifyResult(IWifiActionListener listener, boolean succ) {
        if (listener == null) {
            HwHiSLog.w(TAG, false, "Callback listener is null", new Object[0]);
        } else if (succ) {
            try {
                listener.onSuccess();
            } catch (RemoteException e) {
                HwHiSLog.e(TAG, false, "Callback listener RemoteException", new Object[0]);
            }
        } else {
            listener.onFailure(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean startCsi(CsiMessage msg) {
        if (msg.targets == null || msg.targets.isEmpty() || msg.actionListener == null || this.wifiStateMachine == null) {
            return false;
        }
        deleteCsiFile("/data/log/location/wlan0/CSI.TXT");
        boolean result = this.wifiStateMachine.queryCsi(msg.targets.get(0), true, msg.bandwidthMask, msg.frameTypemask);
        notifyResult(msg.actionListener, result);
        if (result) {
            this.isCsiStarted = true;
            this.csiTargetList.clear();
            this.csiTargetList.addAll(msg.targets);
            this.csiBwMask = msg.bandwidthMask;
            this.csiFrMask = msg.frameTypemask;
            HwHiSLog.d(TAG, false, "CSI STAT: START", new Object[0]);
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean stopCsi(CsiMessage msg) {
        if (msg.targets == null || msg.targets.isEmpty() || msg.actionListener == null) {
            return false;
        }
        boolean result = this.wifiStateMachine.queryCsi(msg.targets.get(0), false, msg.bandwidthMask, msg.frameTypemask);
        deleteCsiFile("/data/log/location/wlan0/CSI.TXT");
        deleteCsiFolder();
        notifyResult(msg.actionListener, result);
        if (result) {
            this.isCsiStarted = false;
            this.csiTargetList.clear();
            HwHiSLog.d(TAG, false, "CSI STAT: STOP", new Object[0]);
            if (!this.isSnifferStarted) {
                removeRecipient(msg.pid);
            }
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean startSniff(SniffMessage msg) {
        if (msg.targetMac == null || TextUtils.isEmpty(msg.targetChannel) || msg.actionListener == null) {
            return false;
        }
        if (this.snifferFirstSet) {
            setSnifferParams(209715200, 1);
            this.snifferFirstSet = false;
        }
        boolean result = setSnifferStat(2, msg.targetChannel, msg.targetMac);
        notifyResult(msg.actionListener, result);
        if (result) {
            this.isSnifferStarted = true;
            this.sniffChannel = msg.targetChannel;
            this.sniffTarget = msg.targetMac;
            HwHiSLog.d(TAG, false, "SNIFFER STAT: START", new Object[0]);
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean stopSniff(SniffMessage msg) {
        if (msg.targetMac == null || TextUtils.isEmpty(msg.targetChannel) || msg.actionListener == null) {
            return false;
        }
        boolean result = setSnifferStat(0, msg.targetChannel, msg.targetMac);
        notifyResult(msg.actionListener, result);
        if (result) {
            this.isSnifferStarted = false;
            HwHiSLog.d(TAG, false, "SNIFFER STAT: STOP", new Object[0]);
            if (!this.isCsiStarted) {
                removeRecipient(msg.pid);
            }
        }
        return result;
    }

    private int checkAndCreate(String dir) {
        File fd = new File(dir);
        try {
            if (fd.exists()) {
                return 0;
            }
            if (!fd.mkdirs()) {
                return -1;
            }
            fd.setReadable(true, false);
            fd.setWritable(true, false);
            fd.setExecutable(true, false);
            SELinux.restorecon(dir);
            return 1;
        } catch (SecurityException e) {
            HwHiSLog.w(TAG, false, "Modify CSI directory error: SecurityException", new Object[0]);
            return -1;
        }
    }

    private boolean checkCsiPrepard() {
        boolean z = false;
        this.isCsiWlanCreated = false;
        this.isCsiLocCreated = false;
        int ret = checkAndCreate("/data/log/location/");
        if (ret == -1) {
            HwHiSLog.w(TAG, false, "CODE_QUERY_CSI location directory not exist", new Object[0]);
            return false;
        }
        this.isCsiLocCreated = ret == 1;
        int ret2 = checkAndCreate("/data/log/location/wlan0/");
        if (ret2 == -1) {
            HwHiSLog.w(TAG, false, "CODE_QUERY_CSI wlan directory not exist", new Object[0]);
            deleteCsiFolder();
            return false;
        }
        if (ret2 == 1) {
            z = true;
        }
        this.isCsiWlanCreated = z;
        return true;
    }

    private void deleteCsiFile(String csiPath) {
        boolean ret = true;
        try {
            File file = new File(csiPath);
            if (file.exists()) {
                ret = file.delete();
            }
            HwHiSLog.i(TAG, false, "delete csi : %{public}s", new Object[]{String.valueOf(ret)});
        } catch (SecurityException e) {
            HwHiSLog.e(TAG, false, "delete csi error : SecurityException", new Object[0]);
        }
    }

    private void deleteCsiFolder() {
        try {
            File wlanDir = new File("/data/log/location/wlan0/");
            if (this.isCsiWlanCreated && wlanDir.exists()) {
                HwHiSLog.i(TAG, false, "delete CSI_WLAN_DIR : %{public}s", new Object[]{String.valueOf(wlanDir.delete())});
                this.isCsiWlanCreated = false;
            }
            File locationDir = new File("/data/log/location/");
            if (this.isCsiLocCreated && locationDir.exists()) {
                HwHiSLog.i(TAG, false, "delete CSI_LOCATION : %{public}s", new Object[]{String.valueOf(locationDir.delete())});
                this.isCsiLocCreated = false;
            }
        } catch (SecurityException e) {
            HwHiSLog.e(TAG, false, "delete csi folder error : SecurityException", new Object[0]);
        }
    }

    private void sniffIntToBytesReverse(byte[] buffer, int start, int value) {
        if (buffer != null && start + 4 <= buffer.length && start >= 0) {
            for (int idx = 0; idx < 4; idx++) {
                buffer[(start + 3) - idx] = (byte) (value >> (24 - (idx * 8)));
            }
        }
    }

    private void sniffChannelToBytes(byte[] buffer, int start, String channel) {
        if (buffer != null && start + 4 <= buffer.length && start >= 0) {
            for (int idx = 0; idx < 4; idx++) {
                if (idx < channel.length()) {
                    buffer[start + idx] = (byte) channel.charAt(idx);
                } else {
                    buffer[start + idx] = -1;
                }
            }
        }
    }

    private void writeSniffBuffHead(byte[] buff, boolean doWrite, int commandCode, int paramNum) {
        if (buff != null && buff.length >= 16) {
            sniffIntToBytesReverse(buff, 0, -1430532899);
            sniffIntToBytesReverse(buff, 4, commandCode);
            sniffIntToBytesReverse(buff, 8, doWrite ? 2 : 1);
            sniffIntToBytesReverse(buff, 12, paramNum);
        }
    }

    private boolean setSnifferStat(int cmd, String channel, MacAddress mac) {
        if (channel == null) {
            return false;
        }
        if (mac == null) {
            return false;
        }
        String[] macSplit = mac.toString().split(":");
        if (macSplit == null) {
            return false;
        }
        if (macSplit.length != 6) {
            return false;
        }
        byte[] buff = new byte[48];
        writeSniffBuffHead(buff, true, -1442775039, 8);
        sniffIntToBytesReverse(buff, 16, cmd);
        sniffChannelToBytes(buff, 20, channel);
        int start = 24;
        for (String singleMac : macSplit) {
            sniffIntToBytesReverse(buff, start, Integer.parseInt(singleMac, 16));
            start += 4;
        }
        int outValue = snifferCommand(buff);
        if (outValue != -1) {
            return true;
        }
        HwHiSLog.e(TAG, false, "SnifferCommand error : SetSnifferStat failed, read %{public}d", new Object[]{Integer.valueOf(outValue)});
        return false;
    }

    private boolean setSnifferParams(int fileSize, int fileNum) {
        if (fileSize < 1 || fileNum < 1) {
            return false;
        }
        byte[] buff = new byte[24];
        writeSniffBuffHead(buff, true, -1442381823, 2);
        sniffIntToBytesReverse(buff, 16, fileNum);
        sniffIntToBytesReverse(buff, 20, fileSize);
        int outValue = snifferCommand(buff);
        if (outValue != -1) {
            return true;
        }
        HwHiSLog.e(TAG, false, "SnifferCommand error : SetSnifferCond failed, read %{public}d", new Object[]{Integer.valueOf(outValue)});
        return false;
    }

    private int readHexReverse(byte[] buff, int start) {
        if (buff == null || start + 4 > buff.length || start < 0) {
            return 0;
        }
        int tail = (start + 4) - 1;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 4; i++) {
            String hex = Integer.toHexString(buff[tail - i] & 255);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return Integer.parseInt(sb.toString(), 16);
    }

    private int getSnifferSize() {
        byte[] inOutBuff = new byte[32];
        writeSniffBuffHead(inOutBuff, true, -1442447359, 4);
        for (int i = 16; i < inOutBuff.length; i++) {
            inOutBuff[i] = 0;
        }
        int fileSize = 0;
        FileInputStream inputStream = null;
        try {
            FileInputStream inputStream2 = new FileInputStream("/proc/sys/net/ipv4/net_sniffer");
            if (inputStream2.read(inOutBuff, 0, 16) != -1) {
                fileSize = readHexReverse(inOutBuff, 16);
            }
            try {
                inputStream2.close();
            } catch (IOException e) {
                HwHiSLog.e(TAG, false, "getSnifferSize error : close exception", new Object[0]);
            }
        } catch (FileNotFoundException e2) {
            HwHiSLog.e(TAG, false, "getSnifferSize error : file not found", new Object[0]);
            if (0 != 0) {
                inputStream.close();
            }
        } catch (SecurityException e3) {
            HwHiSLog.e(TAG, false, "getSnifferSize error : SecurityException", new Object[0]);
            if (0 != 0) {
                inputStream.close();
            }
        } catch (IOException e4) {
            HwHiSLog.e(TAG, false, "getSnifferSize error : read file exception", new Object[0]);
            if (0 != 0) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    HwHiSLog.e(TAG, false, "getSnifferSize error : close exception", new Object[0]);
                }
            }
            throw th;
        }
        return fileSize;
    }

    private int snifferCommand(byte[] inOutBuff) {
        if (inOutBuff == null || inOutBuff.length < 16) {
            return -1;
        }
        FileInputStream inputStream = null;
        int ret = -1;
        try {
            inputStream = new FileInputStream(new File("/proc/sys/net/ipv4/net_sniffer"));
            ret = inputStream.read(inOutBuff, 0, 16);
            try {
                inputStream.close();
            } catch (IOException e) {
                HwHiSLog.e(TAG, false, "snifferCommand error : close exception", new Object[0]);
            }
        } catch (FileNotFoundException e2) {
            HwHiSLog.e(TAG, false, "snifferCommand error : file not found", new Object[0]);
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (SecurityException e3) {
            HwHiSLog.e(TAG, false, "snifferCommand error : SecurityException", new Object[0]);
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e4) {
            HwHiSLog.e(TAG, false, "snifferCommand error : read file exception", new Object[0]);
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    HwHiSLog.e(TAG, false, "snifferCommand error : close exception", new Object[0]);
                }
            }
            throw th;
        }
        return ret;
    }

    private boolean findArpByIp(String line, List<String> ipList) {
        String[] splitted;
        if (TextUtils.isEmpty(line) || (splitted = line.split(" +")) == null || splitted.length != 6) {
            return false;
        }
        if (ipList == null || ipList.isEmpty()) {
            return true;
        }
        for (String ip : ipList) {
            if (ip.equals(splitted[0])) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int findRecipientIndex(int pid) {
        synchronized (this.recipientLock) {
            int idx = 0;
            for (AppDeathRecipient recipient : this.aliveBinderList) {
                if (recipient != null && recipient.pid == pid) {
                    return idx;
                }
                idx++;
            }
            return -1;
        }
    }

    private boolean addNewRecipient(IBinder binder, int callingPid) {
        synchronized (this.recipientLock) {
            if (binder == null) {
                return false;
            }
            if (findRecipientIndex(callingPid) != -1) {
                return true;
            }
            AppDeathRecipient recipient = new AppDeathRecipient(binder, callingPid);
            try {
                binder.linkToDeath(recipient, 0);
                this.aliveBinderList.add(recipient);
                return true;
            } catch (RemoteException e) {
                HwHiSLog.d(TAG, false, "Binder is already dead.", new Object[0]);
                return false;
            }
        }
    }

    private void removeRecipient(int callingPid) {
        synchronized (this.recipientLock) {
            int idx = findRecipientIndex(callingPid);
            if (idx < 0) {
                HwHiSLog.w(TAG, false, "cannot find alive binder by pid", new Object[0]);
                return;
            }
            AppDeathRecipient recipient = this.aliveBinderList.get(idx);
            this.aliveBinderList.remove(idx);
            if (!(recipient == null || recipient.binder == null)) {
                try {
                    recipient.binder.unlinkToDeath(recipient, 0);
                } catch (NoSuchElementException e) {
                    HwHiSLog.w(TAG, false, "unlink binder error: NoSuchElementException", new Object[0]);
                }
            }
        }
    }

    private void clearRecipientList() {
        synchronized (this.recipientLock) {
            this.aliveBinderList.size();
            for (AppDeathRecipient recipient : this.aliveBinderList) {
                if (!(recipient == null || recipient.binder == null)) {
                    try {
                        recipient.binder.unlinkToDeath(recipient, 0);
                    } catch (NoSuchElementException e) {
                        HwHiSLog.w(TAG, false, "unlink binder error: NoSuchElementException", new Object[0]);
                    }
                }
            }
            this.aliveBinderList.clear();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopAliveCsiSniff() {
        if (this.isSnifferStarted) {
            HwHiSLog.d(TAG, false, "Stop alive sniffer", new Object[0]);
            if (setSnifferStat(0, this.sniffChannel, this.sniffTarget)) {
                this.isSnifferStarted = false;
                HwHiSLog.d(TAG, false, "SNIFFER STAT: STOP", new Object[0]);
            } else {
                HwHiSLog.w(TAG, false, "stop sniffer failed", new Object[0]);
            }
        }
        if (this.isCsiStarted) {
            HwHiSLog.w(TAG, false, "Stop alive csi", new Object[0]);
            if (this.wifiStateMachine == null || this.csiTargetList == null || this.csiTargetList.isEmpty() || !this.wifiStateMachine.queryCsi(this.csiTargetList.get(0), false, this.csiBwMask, this.csiFrMask)) {
                HwHiSLog.w(TAG, false, "stop csi failed", new Object[0]);
                return;
            }
            deleteCsiFile("/data/log/location/wlan0/CSI.TXT");
            deleteCsiFolder();
            this.isCsiStarted = false;
            this.csiTargetList.clear();
            HwHiSLog.d(TAG, false, "CSI STAT: STOP", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void finishWorkThread() {
        stopAliveCsiSniff();
        this.isSnifferStarted = false;
        this.isCsiStarted = false;
        this.csiTargetList.clear();
        clearRecipientList();
    }
}

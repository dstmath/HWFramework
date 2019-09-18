package com.android.server.hidata.channelqoe;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.IMonitor;
import android.util.Log;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import com.android.server.wifipro.WifiProCommonUtils;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.net.ssl.SSLContext;

public class HwChannelQoEManager {
    private static final int AVAILABLE_CONNECT_TIMEOUT = 3000;
    private static final int AVAILABLE_INPUT_WAIT = 2000;
    private static final int AVAILABLE_TIMEOUT = 5000;
    private static final int CELL_SIGNAL_LEVEL_GOOD = 0;
    private static final int CELL_SIGNAL_LEVEL_MODERATE = 1;
    private static final int CELL_SIGNAL_LEVEL_POOR = 2;
    private static final int CHQOE_RTT_GET_TIMES = 3;
    private static final int CHQOE_TIMEOUT = 5500;
    private static final int CHR_CELL_PARAMETERS = 0;
    private static final int CHR_PARAMETERS_ARRAY_MAX_LEN = 2;
    private static final int CHR_WIFI_PARAMETERS = 1;
    private static final String COUNTRY_CODE_CHINA = "460";
    private static final String Cnt = "CNT";
    private static final int DNS_EXCEPTION_WAIT_TIME = 1000;
    private static final int DNS_TIMEOUT = 1000;
    private static final int E909002054 = 909002054;
    private static final int E909009039 = 909009039;
    private static final int E909009040 = 909009040;
    private static final int GET_CURRENT_RTT = 0;
    private static final int GET_CURRENT_RTT_SCENCE = -1;
    private static final int GET_CURRENT_RTT_UID = -1;
    private static final boolean IS_CHINA = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    private static final String LVl = "LVL";
    private static final int MESSAGE_AVAILABLE_EXCEPTION = 61447;
    private static final int MESSAGE_AVAILABLE_OK = 61446;
    private static final int MESSAGE_AVAILABLE_TIMEOUT = 61445;
    private static final int MESSAGE_BAK_RTT = 61444;
    private static final int MESSAGE_DNS_TIMEOUT = 61448;
    private static final int MESSAGE_EXCEPTION = 61440;
    private static final int MESSAGE_EXCEPTION_BAK = 61441;
    private static final int MESSAGE_FIRST_RTT = 61443;
    private static final int MESSAGE_START_QUERY = 61449;
    private static final int MESSAGE_TIMEOUT = 61442;
    private static final int NOT_GET_CURRENT_RTT = 1;
    private static final int SOCKET_CONNECT_TIMEOUT = 4000;
    private static final int SOCKET_INPUT_WAIT_TIME = 500;
    private static final int SOCKET_MAX_TIMEOUT = 999;
    private static final String TAG = "HiDATA_ChannelQoE";
    private static final String getBaidu = "GET http://www.baidu.com/ HTTP/1.1\r\nHost: www.baidu.com\r\nConnection: Keep-Alive\r\n\r\n";
    private static final String getGoogle = "GET http://www.google.com/ HTTP/1.1\r\nHost: www.google.com\r\nConnection: Keep-Alive\r\n\r\n";
    private static final String getGstatic = "GET /generate_204 HTTP/1.1\r\nHost: connectivitycheck.gstatic.com\r\nConnection: Keep-Alive\r\n\r\n";
    private static final String getHiCloud = "GET /generate_204 HTTP/1.1\r\nHost: connectivitycheck.platform.hi\r\nConnection: Keep-Alive\r\n\r\n";
    private static volatile HwChannelQoEManager mChannelQoEManager = null;
    private static final String urlBaidu = "www.baidu.com";
    private static final String urlGoogle = "www.google.com";
    private static final String urlGstatic = "connectivitycheck.gstatic.com";
    private static final String urlHiCloud = "connectivitycheck.platform.hicloud.com";
    /* access modifiers changed from: private */
    public String getBak = getBaidu;
    /* access modifiers changed from: private */
    public String getFirst = getHiCloud;
    PhoneStateListener listenerSim0;
    PhoneStateListener listenerSim1;
    private CHMeasureObject mCell;
    /* access modifiers changed from: private */
    public HistoryMseasureInfo mCellHistoryMseasureInfo = null;
    private HwCHQciManager mChQciManager;
    /* access modifiers changed from: private */
    public HwChannelQoEParmStatistics[] mChannelQoEParm = new HwChannelQoEParmStatistics[2];
    /* access modifiers changed from: private */
    public Context mContext;
    private CurrentSignalState mCurrentSignalState = null;
    /* access modifiers changed from: private */
    public int mGetCurrentRttFlag = 1;
    private long mLastUploadTime;
    private HwChannelQoEMonitor mQoEMonitor;
    /* access modifiers changed from: private */
    public SignalStrength mSignalStrengthSim0 = null;
    /* access modifiers changed from: private */
    public SignalStrength mSignalStrengthSim1 = null;
    private TelephonyManager mTelephonyManager;
    private CHMeasureObject mWifi;
    /* access modifiers changed from: private */
    public HistoryMseasureInfo mWifiHistoryMseasureInfo = null;
    /* access modifiers changed from: private */
    public String urlBak = urlBaidu;
    /* access modifiers changed from: private */
    public String urlFirst = urlHiCloud;

    public class CHAvailableThread extends CHThread {
        public CHAvailableThread(int networkType, Handler handler, Context context) {
            super(networkType, handler, context);
        }

        public void run() {
            StringBuilder sb;
            HwChannelQoEManager.log("CHAvailableThread thread start at ." + String.valueOf(System.currentTimeMillis()));
            Socket sk = new Socket();
            try {
                super.bindSocketToNetwork(sk);
                sk.connect(getByName(HwChannelQoEManager.this.urlFirst, 80), 3000);
                sk.setSoTimeout(2000);
                OutputStreamWriter osw = new OutputStreamWriter(sk.getOutputStream(), Charset.defaultCharset().name());
                osw.write(HwChannelQoEManager.this.getFirst.toCharArray());
                osw.flush();
                if (sk.getInputStream().read(new byte[1000]) >= 5) {
                    Message message = Message.obtain();
                    message.what = HwChannelQoEManager.MESSAGE_AVAILABLE_OK;
                    this.threadHandler.sendMessage(message);
                    if (sk.isConnected()) {
                        try {
                            sk.close();
                            HwChannelQoEManager.log("CHAvailableThread socket closed.");
                            return;
                        } catch (IOException e) {
                            e = e;
                            sb = new StringBuilder();
                        }
                    } else {
                        return;
                    }
                } else {
                    throw new Exception("not invalid read length.");
                }
                sb.append("CHAvailableThread socket closed exception.");
                sb.append(e.toString());
                HwChannelQoEManager.log(sb.toString());
            } catch (Exception e2) {
                HwChannelQoEManager.log("CHAvailableThread thread exception. " + e2.toString());
                Message message2 = Message.obtain();
                message2.what = HwChannelQoEManager.MESSAGE_AVAILABLE_EXCEPTION;
                this.threadHandler.sendMessage(message2);
                if (sk.isConnected()) {
                    try {
                        sk.close();
                        HwChannelQoEManager.log("CHAvailableThread socket closed.");
                    } catch (IOException e3) {
                        e = e3;
                        sb = new StringBuilder();
                    }
                }
            } catch (Throwable th) {
                if (sk.isConnected()) {
                    try {
                        sk.close();
                        HwChannelQoEManager.log("CHAvailableThread socket closed.");
                    } catch (IOException e4) {
                        HwChannelQoEManager.log("CHAvailableThread socket closed exception." + e4.toString());
                    }
                }
                throw th;
            }
        }
    }

    public class CHBakThread extends CHThread {
        public CHBakThread(int networkType, Handler handler, Context context) {
            super(networkType, handler, context);
            if (801 == networkType) {
                if (HwChannelQoEManager.this.isChina()) {
                    int[] iArr = HwChannelQoEManager.this.mChannelQoEParm[0].mSvr.mSvr;
                    iArr[1] = iArr[1] + 1;
                    return;
                }
                int[] iArr2 = HwChannelQoEManager.this.mChannelQoEParm[0].mSvr.mSvr;
                iArr2[3] = iArr2[3] + 1;
            } else if (800 != networkType) {
            } else {
                if (HwChannelQoEManager.this.isChina()) {
                    int[] iArr3 = HwChannelQoEManager.this.mChannelQoEParm[1].mSvr.mSvr;
                    iArr3[1] = iArr3[1] + 1;
                    return;
                }
                int[] iArr4 = HwChannelQoEManager.this.mChannelQoEParm[1].mSvr.mSvr;
                iArr4[3] = iArr4[3] + 1;
            }
        }

        public void run() {
            try {
                HwChannelQoEManager.log("CHBakThread run.");
                SSLContext context = SSLContext.getInstance("SSL");
                context.init(null, null, new SecureRandom());
                Socket sk = context.getSocketFactory().createSocket();
                super.bindSocketToNetwork(sk);
                sk.connect(getByName(HwChannelQoEManager.this.urlBak, 443), 4000);
                OutputStreamWriter osw = new OutputStreamWriter(sk.getOutputStream(), Charset.defaultCharset().name());
                long getstart = System.currentTimeMillis();
                osw.write(HwChannelQoEManager.this.getBak);
                osw.flush();
                if (sk.getInputStream().read(new byte[1000]) >= 10) {
                    long rtt = System.currentTimeMillis() - getstart;
                    StringBuilder sb = new StringBuilder();
                    SSLContext sSLContext = context;
                    sb.append("CHBakThread socket Rtt is ");
                    sb.append(String.valueOf(rtt));
                    HwChannelQoEManager.log(sb.toString());
                    Message message = Message.obtain();
                    message.what = HwChannelQoEManager.MESSAGE_BAK_RTT;
                    message.arg1 = (int) rtt;
                    this.threadHandler.sendMessage(message);
                    return;
                }
                SSLContext sSLContext2 = context;
                throw new Exception("not invalid read length.");
            } catch (Exception e) {
                Message message2 = Message.obtain();
                message2.what = HwChannelQoEManager.MESSAGE_EXCEPTION_BAK;
                this.threadHandler.sendMessage(message2);
                HwChannelQoEManager.log("CHBakThread socket err " + e.toString());
                Message message3 = message2;
            }
        }
    }

    public class CHFirstThread extends CHThread {
        public CHFirstThread(int networkType, Handler handler, Context context) {
            super(networkType, handler, context);
            HwChannelQoEManager.log("CHFirstThread create.");
            if (801 == networkType) {
                if (HwChannelQoEManager.this.isChina()) {
                    int[] iArr = HwChannelQoEManager.this.mChannelQoEParm[0].mSvr.mSvr;
                    iArr[0] = iArr[0] + 1;
                    return;
                }
                int[] iArr2 = HwChannelQoEManager.this.mChannelQoEParm[0].mSvr.mSvr;
                iArr2[2] = iArr2[2] + 1;
            } else if (800 != networkType) {
            } else {
                if (HwChannelQoEManager.this.isChina()) {
                    int[] iArr3 = HwChannelQoEManager.this.mChannelQoEParm[1].mSvr.mSvr;
                    iArr3[0] = iArr3[0] + 1;
                    return;
                }
                int[] iArr4 = HwChannelQoEManager.this.mChannelQoEParm[1].mSvr.mSvr;
                iArr4[2] = iArr4[2] + 1;
            }
        }

        public void run() {
            StringBuilder sb;
            HwChannelQoEManager.log("CHFirstThread thread start at ." + String.valueOf(System.currentTimeMillis()));
            Socket sk = new Socket();
            try {
                super.bindSocketToNetwork(sk);
                Message msg = Message.obtain();
                msg.what = HwChannelQoEManager.MESSAGE_DNS_TIMEOUT;
                this.threadHandler.sendMessageDelayed(msg, 1000);
                InetSocketAddress addr = getByName(HwChannelQoEManager.this.urlFirst, 80);
                this.threadHandler.removeMessages(HwChannelQoEManager.MESSAGE_DNS_TIMEOUT);
                sk.connect(addr, 4000);
                sk.setSoTimeout(500);
                OutputStreamWriter osw = new OutputStreamWriter(sk.getOutputStream(), Charset.defaultCharset().name());
                for (int i = 0; i < 3; i++) {
                    getHttpRtt(osw, sk);
                }
                try {
                    sk.close();
                    HwChannelQoEManager.log("First socket closed.");
                    return;
                } catch (IOException e) {
                    e = e;
                    sb = new StringBuilder();
                }
                sb.append("First socket closed exception.");
                sb.append(e.toString());
                HwChannelQoEManager.log(sb.toString());
            } catch (Exception e2) {
                HwChannelQoEManager.log("First socket exception. " + e2.toString());
                Message message = Message.obtain();
                message.what = 61440;
                message.obj = e2;
                if (this.threadHandler.hasMessages(HwChannelQoEManager.MESSAGE_DNS_TIMEOUT)) {
                    HwChannelQoEManager.log("DNS exception. MESSAGE_DNS_TIMEOUT will be deleted.");
                    this.threadHandler.removeMessages(HwChannelQoEManager.MESSAGE_DNS_TIMEOUT);
                    this.threadHandler.sendMessageDelayed(message, 1000);
                } else {
                    this.threadHandler.sendMessage(message);
                }
                try {
                    sk.close();
                    HwChannelQoEManager.log("First socket closed.");
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            } catch (Throwable e4) {
                try {
                    sk.close();
                    HwChannelQoEManager.log("First socket closed.");
                } catch (IOException e5) {
                    HwChannelQoEManager.log("First socket closed exception." + e5.toString());
                }
                throw e4;
            }
        }

        private void getHttpRtt(OutputStreamWriter osw, Socket sk) throws Exception {
            HwChannelQoEManager.log("getHttpRtt enter.");
            long getstart = System.currentTimeMillis();
            osw.write(HwChannelQoEManager.this.getFirst.toCharArray());
            osw.flush();
            byte[] buffer = new byte[1000];
            int len = sk.getInputStream().read(buffer);
            if (len >= 5) {
                String sCheck = new String(buffer, "UTF-8");
                if (sCheck.contains("HTTP/1.1 204 No Content")) {
                    long rtt = System.currentTimeMillis() - getstart;
                    HwChannelQoEManager.log("First socket Rtt is " + String.valueOf(rtt));
                    Message message = Message.obtain();
                    message.what = HwChannelQoEManager.MESSAGE_FIRST_RTT;
                    message.arg1 = (int) rtt;
                    this.threadHandler.sendMessage(message);
                    return;
                }
                HwChannelQoEManager.log("getHttpRtt read error!Socket input doesn't contain HTTP/1.1 204 No Content");
                HwChannelQoEManager.log(sCheck);
                throw new Exception("not invalid String.");
            }
            HwChannelQoEManager.log("getHttpRtt read length is" + String.valueOf(len));
            throw new Exception("not invalid read length.");
        }
    }

    public class CHMeasureObject {
        /* access modifiers changed from: private */
        public boolean isBakRunning = false;
        /* access modifiers changed from: private */
        public IChannelQoECallback mAvailableCallBack = null;
        private CopyOnWriteArrayList<HwChannelQoEAppInfo> mCallbackList = new CopyOnWriteArrayList<>();
        /* access modifiers changed from: private */
        public Handler mHandler;
        /* access modifiers changed from: private */
        public int mMutex = 0;
        /* access modifiers changed from: private */
        public String mName;
        /* access modifiers changed from: private */
        public RttRecords mRttRecords = new RttRecords();
        /* access modifiers changed from: private */
        public int netType;

        public CHMeasureObject(String name, int networkType) {
            this.mName = name;
            this.netType = networkType;
            this.mHandler = new Handler(HwChannelQoEManager.this) {
                public void handleMessage(Message msg) {
                    HwChannelQoEManager.log(CHMeasureObject.this.mName + " handleMessage what is " + String.valueOf(msg.what));
                    switch (msg.what) {
                        case 61440:
                            if (!CHMeasureObject.this.isBakRunning) {
                                if (CHMeasureObject.this.mMutex != 0) {
                                    if (!CHMeasureObject.this.mRttRecords.hasValues()) {
                                        HwChannelQoEManager.log("exception but record has no value. will start bakprocess");
                                        boolean unused = CHMeasureObject.this.isBakRunning = true;
                                        CHMeasureObject.this.bakProcess(CHMeasureObject.this.netType);
                                        break;
                                    } else {
                                        HwChannelQoEManager.log("exception but record has values.");
                                        removeMessages(HwChannelQoEManager.MESSAGE_TIMEOUT);
                                        CHMeasureObject.this.processAll(CHMeasureObject.this.mRttRecords.getFinalRtt());
                                        break;
                                    }
                                } else {
                                    HwChannelQoEManager.log("MESSAGE_EXCEPTION: mMutex is 0, will do nothing.");
                                    return;
                                }
                            } else {
                                HwChannelQoEManager.log("Bak is Running. Do not process hicloud exception anymore.");
                                return;
                            }
                        case HwChannelQoEManager.MESSAGE_EXCEPTION_BAK /*61441*/:
                            boolean unused2 = CHMeasureObject.this.isBakRunning = false;
                            if (CHMeasureObject.this.mMutex != 0) {
                                removeMessages(HwChannelQoEManager.MESSAGE_TIMEOUT);
                                CHMeasureObject.this.processAll(999);
                                break;
                            } else {
                                HwChannelQoEManager.this.logE("MESSAGE_EXCEPTION_BAK, there is no item in callbacklist.");
                                return;
                            }
                        case HwChannelQoEManager.MESSAGE_TIMEOUT /*61442*/:
                            boolean unused3 = CHMeasureObject.this.isBakRunning = false;
                            if (CHMeasureObject.this.mMutex != 0) {
                                if (!CHMeasureObject.this.mRttRecords.hasValues()) {
                                    HwChannelQoEManager.log("timeout but record has no value.");
                                    CHMeasureObject.this.processAll(999);
                                    break;
                                } else {
                                    HwChannelQoEManager.log("timeout but record has values.");
                                    CHMeasureObject.this.processAll(CHMeasureObject.this.mRttRecords.getFinalRtt());
                                    break;
                                }
                            } else {
                                HwChannelQoEManager.log("MESSAGE_TIMEOUT: mMutex is 0, will do nothing.");
                                return;
                            }
                        case HwChannelQoEManager.MESSAGE_FIRST_RTT /*61443*/:
                            if (!CHMeasureObject.this.isBakRunning) {
                                if (CHMeasureObject.this.mMutex != 0) {
                                    CHMeasureObject.this.mRttRecords.insert((long) msg.arg1);
                                    if (CHMeasureObject.this.mRttRecords.isReady()) {
                                        HwChannelQoEManager.log("all records are ready.");
                                        removeMessages(HwChannelQoEManager.MESSAGE_TIMEOUT);
                                        CHMeasureObject.this.processAll(CHMeasureObject.this.mRttRecords.getFinalRtt());
                                        break;
                                    }
                                } else {
                                    HwChannelQoEManager.log("MESSAGE_FIRST_RTT: mMutex is 0, do nothing.");
                                    return;
                                }
                            } else {
                                HwChannelQoEManager.log("Bak is Running. Do not process hicloud anymore.");
                                return;
                            }
                            break;
                        case HwChannelQoEManager.MESSAGE_BAK_RTT /*61444*/:
                            boolean unused4 = CHMeasureObject.this.isBakRunning = false;
                            if (CHMeasureObject.this.mMutex != 0) {
                                removeMessages(HwChannelQoEManager.MESSAGE_TIMEOUT);
                                CHMeasureObject.this.processAll((long) msg.arg1);
                                break;
                            } else {
                                HwChannelQoEManager.this.logE("MESSAGE_BAK_RTT, there is no item in callbacklist.");
                                return;
                            }
                        case HwChannelQoEManager.MESSAGE_AVAILABLE_TIMEOUT /*61445*/:
                            if (CHMeasureObject.this.mAvailableCallBack != null) {
                                HwChannelQoEManager.log("onCellPSAvailable false, reason CONNECT_TIMEOUT.");
                                CHMeasureObject.this.mAvailableCallBack.onCellPSAvailable(false, 1);
                                IChannelQoECallback unused5 = CHMeasureObject.this.mAvailableCallBack = null;
                                break;
                            }
                            break;
                        case HwChannelQoEManager.MESSAGE_AVAILABLE_OK /*61446*/:
                            removeMessages(HwChannelQoEManager.MESSAGE_AVAILABLE_TIMEOUT);
                            if (CHMeasureObject.this.mAvailableCallBack != null) {
                                HwChannelQoEManager.log("onCellPSAvailable true, reason CONNECT_AVAILABLE.");
                                CHMeasureObject.this.mAvailableCallBack.onCellPSAvailable(true, 0);
                                IChannelQoECallback unused6 = CHMeasureObject.this.mAvailableCallBack = null;
                                break;
                            }
                            break;
                        case HwChannelQoEManager.MESSAGE_AVAILABLE_EXCEPTION /*61447*/:
                            removeMessages(HwChannelQoEManager.MESSAGE_AVAILABLE_TIMEOUT);
                            if (CHMeasureObject.this.mAvailableCallBack != null) {
                                HwChannelQoEManager.log("onCellPSAvailable false, reason exception.");
                                CHMeasureObject.this.mAvailableCallBack.onCellPSAvailable(false, 1);
                                IChannelQoECallback unused7 = CHMeasureObject.this.mAvailableCallBack = null;
                                break;
                            }
                            break;
                        case HwChannelQoEManager.MESSAGE_DNS_TIMEOUT /*61448*/:
                            HwChannelQoEManager.log("MESSAGE_DNS_TIMEOUT with netType " + String.valueOf(CHMeasureObject.this.netType));
                            if (801 == CHMeasureObject.this.netType) {
                                int[] iArr = HwChannelQoEManager.this.mChannelQoEParm[0].mRst.mRst;
                                iArr[3] = iArr[3] + 1;
                            } else if (800 == CHMeasureObject.this.netType) {
                                int[] iArr2 = HwChannelQoEManager.this.mChannelQoEParm[1].mRst.mRst;
                                iArr2[3] = iArr2[3] + 1;
                            }
                            boolean unused8 = CHMeasureObject.this.isBakRunning = true;
                            CHMeasureObject.this.bakProcess(CHMeasureObject.this.netType);
                            break;
                        case HwChannelQoEManager.MESSAGE_START_QUERY /*61449*/:
                            HwChannelQoEManager.log("MESSAGE_START_QUERY enter.");
                            CHMeasureObject.this.queryQuality((HwChannelQoEAppInfo) msg.obj);
                            break;
                        default:
                            HwChannelQoEManager.log("handler receive unknown message " + String.valueOf(msg.what));
                            break;
                    }
                }
            };
        }

        private void statisticsRttScope(int mChannelQoEParmIndex, int rtt) {
            HwChannelQoEManager.log("enter statisticsRttScope: rtt is " + String.valueOf(rtt));
            if (mChannelQoEParmIndex >= 2) {
                HwChannelQoEManager.log("statisticsRttScope index err.");
            } else if (rtt <= 0) {
                HwChannelQoEManager.log("statisticsRttScope: rtt <= 0");
            } else {
                if (rtt < 50) {
                    int[] iArr = HwChannelQoEManager.this.mChannelQoEParm[mChannelQoEParmIndex].mRtt.mRtt;
                    iArr[0] = iArr[0] + 1;
                } else if (rtt < 100) {
                    int[] iArr2 = HwChannelQoEManager.this.mChannelQoEParm[mChannelQoEParmIndex].mRtt.mRtt;
                    iArr2[1] = iArr2[1] + 1;
                } else if (rtt < 150) {
                    int[] iArr3 = HwChannelQoEManager.this.mChannelQoEParm[mChannelQoEParmIndex].mRtt.mRtt;
                    iArr3[2] = iArr3[2] + 1;
                } else if (rtt < 200) {
                    int[] iArr4 = HwChannelQoEManager.this.mChannelQoEParm[mChannelQoEParmIndex].mRtt.mRtt;
                    iArr4[3] = iArr4[3] + 1;
                } else if (rtt < 250) {
                    int[] iArr5 = HwChannelQoEManager.this.mChannelQoEParm[mChannelQoEParmIndex].mRtt.mRtt;
                    iArr5[4] = iArr5[4] + 1;
                } else if (rtt < 300) {
                    int[] iArr6 = HwChannelQoEManager.this.mChannelQoEParm[mChannelQoEParmIndex].mRtt.mRtt;
                    iArr6[5] = iArr6[5] + 1;
                } else if (rtt < 350) {
                    int[] iArr7 = HwChannelQoEManager.this.mChannelQoEParm[mChannelQoEParmIndex].mRtt.mRtt;
                    iArr7[6] = iArr7[6] + 1;
                } else if (rtt < 400) {
                    int[] iArr8 = HwChannelQoEManager.this.mChannelQoEParm[mChannelQoEParmIndex].mRtt.mRtt;
                    iArr8[7] = iArr8[7] + 1;
                } else if (rtt < 500) {
                    int[] iArr9 = HwChannelQoEManager.this.mChannelQoEParm[mChannelQoEParmIndex].mRtt.mRtt;
                    iArr9[8] = iArr9[8] + 1;
                } else {
                    int[] iArr10 = HwChannelQoEManager.this.mChannelQoEParm[mChannelQoEParmIndex].mRtt.mRtt;
                    iArr10[9] = iArr10[9] + 1;
                }
            }
        }

        /* access modifiers changed from: private */
        public void processAll(long finalRtt) {
            HwChannelQoEManager hwChannelQoEManager = HwChannelQoEManager.this;
            hwChannelQoEManager.logE("processAll enter. mCallbackList size is " + String.valueOf(this.mCallbackList.size()));
            HwChannelQoEManager hwChannelQoEManager2 = HwChannelQoEManager.this;
            hwChannelQoEManager2.logE("finalRtt is " + String.valueOf(finalRtt));
            HwChannelQoEManager.log("processAll():clt CHR Param statistics,networkType is " + String.valueOf(this.netType));
            if (801 == this.netType) {
                statisticsRttScope(0, (int) finalRtt);
                int unused = HwChannelQoEManager.this.mCellHistoryMseasureInfo.rttBef = (int) finalRtt;
            } else if (800 == this.netType) {
                statisticsRttScope(1, (int) finalRtt);
                int unused2 = HwChannelQoEManager.this.mWifiHistoryMseasureInfo.rttBef = (int) finalRtt;
            }
            int lable = 1;
            Iterator<HwChannelQoEAppInfo> it = this.mCallbackList.iterator();
            while (it.hasNext()) {
                HwChannelQoEAppInfo appqoeInfo = it.next();
                if (HwCHQciManager.getInstance().getChQciConfig(appqoeInfo.mQci).mRtt == 0) {
                    if (801 == this.netType) {
                        int[] iArr = HwChannelQoEManager.this.mChannelQoEParm[0].mRst.mRst;
                        iArr[4] = iArr[4] + 1;
                    } else if (800 == this.netType) {
                        int[] iArr2 = HwChannelQoEManager.this.mChannelQoEParm[1].mRst.mRst;
                        iArr2[4] = iArr2[4] + 1;
                    }
                } else if (((long) HwCHQciManager.getInstance().getChQciConfig(appqoeInfo.mQci).mRtt) <= finalRtt) {
                    lable = 1;
                    if (801 == this.netType) {
                        int[] iArr3 = HwChannelQoEManager.this.mChannelQoEParm[0].mRst.mRst;
                        iArr3[2] = iArr3[2] + 1;
                    } else if (800 == this.netType) {
                        int[] iArr4 = HwChannelQoEManager.this.mChannelQoEParm[1].mRst.mRst;
                        iArr4[2] = iArr4[2] + 1;
                    }
                } else {
                    lable = 0;
                    if (801 == this.netType) {
                        int[] iArr5 = HwChannelQoEManager.this.mChannelQoEParm[0].mRst.mRst;
                        iArr5[0] = iArr5[0] + 1;
                    } else if (800 == this.netType) {
                        int[] iArr6 = HwChannelQoEManager.this.mChannelQoEParm[1].mRst.mRst;
                        iArr6[0] = iArr6[0] + 1;
                    }
                }
                appqoeInfo.callback.onChannelQuality(appqoeInfo.mUID, appqoeInfo.mScence, appqoeInfo.mNetwork, lable);
                HwChannelQoEManager hwChannelQoEManager3 = HwChannelQoEManager.this;
                hwChannelQoEManager3.logE("label is " + String.valueOf(lable));
                if (-1 == appqoeInfo.mUID && HwChannelQoEManager.this.mGetCurrentRttFlag == 0) {
                    HwChannelQoEManager.log("processAll(): get current rtt, rtt is " + HwCHQciManager.getInstance().getChQciConfig(appqoeInfo.mQci).mRtt);
                    appqoeInfo.callback.onCurrentRtt((int) finalRtt);
                    int unused3 = HwChannelQoEManager.this.mGetCurrentRttFlag = 1;
                }
            }
            this.mCallbackList.clear();
            this.mMutex = 0;
            this.mRttRecords.reset();
        }

        /* access modifiers changed from: private */
        public void queryQuality(HwChannelQoEAppInfo appqoeInfo) {
            HwChannelQoEManager.log(this.mName + " queryQuality.");
            this.mCallbackList.add(appqoeInfo);
            if (this.mMutex > 0) {
                HwChannelQoEManager.this.logE("There is another RTT processing now.");
                return;
            }
            this.mMutex++;
            firstRttProcess(appqoeInfo.mNetwork);
            Message msg = Message.obtain();
            msg.what = HwChannelQoEManager.MESSAGE_TIMEOUT;
            this.mHandler.sendMessageDelayed(msg, 5500);
            HwChannelQoEManager.log(this.mName + " send timer MESSAGE_TIMEOUT.");
        }

        public void queryAvailable(IChannelQoECallback callBack) {
            this.mAvailableCallBack = callBack;
            new CHAvailableThread(801, this.mHandler, HwChannelQoEManager.this.mContext).start();
            Message message = Message.obtain();
            message.what = HwChannelQoEManager.MESSAGE_AVAILABLE_TIMEOUT;
            this.mHandler.sendMessageDelayed(message, 5000);
        }

        private void firstRttProcess(int networkType) {
            HwChannelQoEManager.log("firstRttProcess enter.");
            new CHFirstThread(networkType, this.mHandler, HwChannelQoEManager.this.mContext).start();
        }

        /* access modifiers changed from: private */
        public void bakProcess(int networkType) {
            HwChannelQoEManager.log("bakProcess enter.");
            new CHBakThread(networkType, this.mHandler, HwChannelQoEManager.this.mContext).start();
        }
    }

    public class CHThread extends Thread {
        private ConnectivityManager mCM;
        private Network mCellNetwork;
        private int mNetworkType;
        private Network mWifiNetwork;
        public Handler threadHandler;

        public CHThread(int networkType, Handler handler, Context context) {
            this.threadHandler = handler;
            this.mCM = (ConnectivityManager) context.getSystemService("connectivity");
            this.mNetworkType = networkType;
        }

        public void run() {
        }

        public InetSocketAddress getByName(String host, int port) throws Exception {
            try {
                if (this.mNetworkType == 801 && this.mCellNetwork != null) {
                    HwChannelQoEManager.log("mCellNetwork get dns.");
                    return new InetSocketAddress(this.mCellNetwork.getByName(host), port);
                } else if (this.mNetworkType != 800 || this.mWifiNetwork == null) {
                    HwChannelQoEManager.log("network null error. network type is " + String.valueOf(this.mNetworkType));
                    throw new Exception("network null error. network type is " + String.valueOf(this.mNetworkType));
                } else {
                    HwChannelQoEManager.log("mWifiNetwork get dns.");
                    return new InetSocketAddress(this.mWifiNetwork.getByName(host), port);
                }
            } catch (Exception e) {
                HwChannelQoEManager.log("get dns Exception. " + e.toString());
                throw e;
            }
        }

        public void bindSocketToNetwork(Socket sk) throws Exception {
            Network[] networks = this.mCM.getAllNetworks();
            HwChannelQoEManager.log("networks size is " + String.valueOf(networks.length));
            for (int i = 0; i < networks.length; i++) {
                NetworkInfo networkInfo = this.mCM.getNetworkInfo(networks[i]);
                HwChannelQoEManager.log("networksInfo is " + networkInfo.toString());
                if (networkInfo.getType() == 0) {
                    HwChannelQoEManager.log("networks TYPE_MOBILE " + String.valueOf(i));
                    this.mCellNetwork = networks[i];
                } else if (networkInfo.getType() == 1) {
                    HwChannelQoEManager.log("networks[i].netId " + networks[i].toString());
                    this.mWifiNetwork = networks[i];
                }
            }
            if (this.mNetworkType == 801) {
                if (this.mCellNetwork != null) {
                    HwChannelQoEManager.log("mCellNetwork bind socket.");
                    this.mCellNetwork.bindSocket(sk);
                } else {
                    HwChannelQoEManager.log("mCellNetwork is null.");
                    throw new Exception("mCellNetwork is null.");
                }
            } else if (this.mWifiNetwork != null) {
                HwChannelQoEManager.log("mWifiNetwork bind socket.");
                this.mWifiNetwork.bindSocket(sk);
            } else {
                HwChannelQoEManager.log("mWifiNetwork is null.");
                throw new Exception("mWifiNetwork is null.");
            }
            HwChannelQoEManager.log("socket bind to network succ. socketfd is " + String.valueOf(sk.getFileDescriptor$().getInt$()));
        }
    }

    public static class CurrentSignalState {
        /* access modifiers changed from: private */
        public int networkType = 0;
        /* access modifiers changed from: private */
        public int sigLoad;
        /* access modifiers changed from: private */
        public int sigPwr;
        /* access modifiers changed from: private */
        public int sigQual;
        /* access modifiers changed from: private */
        public int sigSnr;

        public int getNetwork() {
            return this.networkType;
        }

        public int getSigPwr() {
            return this.sigPwr;
        }

        public int getSigSnr() {
            return this.sigSnr;
        }

        public int getSigQual() {
            return this.sigQual;
        }

        public int getSigLoad() {
            return this.sigLoad;
        }
    }

    public static class HistoryMseasureInfo {
        /* access modifiers changed from: private */
        public int rat = -1;
        /* access modifiers changed from: private */
        public int rttBef = 0;
        /* access modifiers changed from: private */
        public int sigLoad = 255;
        /* access modifiers changed from: private */
        public int sigPwr = 255;
        /* access modifiers changed from: private */
        public int sigQual = 255;
        /* access modifiers changed from: private */
        public int sigSnr = 255;
        /* access modifiers changed from: private */
        public int tupBef = 0;

        public String toString() {
            return "tupBef:" + String.valueOf(this.tupBef) + ",rttBef:" + String.valueOf(this.rttBef) + ",sigPwr:" + String.valueOf(this.sigPwr) + ",sigSnr:" + String.valueOf(this.sigSnr) + ",sigQual:" + String.valueOf(this.sigQual) + ",sigLoad:" + String.valueOf(this.sigLoad) + ",rat:" + String.valueOf(this.rat);
        }

        public void reset() {
            this.tupBef = 0;
            this.rttBef = 0;
            this.sigPwr = 255;
            this.sigSnr = 255;
            this.sigQual = 255;
            this.sigLoad = 255;
            this.rat = -1;
        }

        public int getRttBef() {
            return this.rttBef;
        }

        public int getTupBef() {
            return this.tupBef;
        }

        public int getPwr() {
            return this.sigPwr;
        }

        public int getSnr() {
            return this.sigSnr;
        }

        public int getQual() {
            return this.sigQual;
        }

        public int getLoad() {
            return this.sigLoad;
        }

        public int getRat() {
            return this.rat;
        }
    }

    public static class RttRecords {
        private long[] channel_rtt = new long[3];
        private boolean ready;

        public RttRecords() {
            initValues();
        }

        public void reset() {
            initValues();
        }

        private void initValues() {
            for (int i = 0; i < 3; i++) {
                this.channel_rtt[i] = 0;
            }
            this.ready = false;
        }

        public void insert(long rtt) {
            int i = 0;
            while (true) {
                if (i >= 3) {
                    break;
                } else if (this.channel_rtt[i] == 0) {
                    this.channel_rtt[i] = rtt;
                    break;
                } else {
                    i++;
                }
            }
            if (i == 2) {
                this.ready = true;
            }
        }

        public boolean isReady() {
            return this.ready;
        }

        public boolean hasValues() {
            for (int i = 0; i < 3; i++) {
                if (this.channel_rtt[i] != 0) {
                    return true;
                }
            }
            return false;
        }

        public long getFinalRtt() {
            long result = 0;
            int count = 0;
            int i = 0;
            while (i < 3 && this.channel_rtt[i] != 0) {
                result += this.channel_rtt[i];
                count++;
                i++;
            }
            return result / ((long) count);
        }
    }

    public static HwChannelQoEManager createInstance(Context context) {
        if (mChannelQoEManager == null) {
            synchronized (HwChannelQoEManager.class) {
                if (mChannelQoEManager == null) {
                    log("createInstance enter.");
                    mChannelQoEManager = new HwChannelQoEManager(context);
                }
            }
        }
        return mChannelQoEManager;
    }

    public static HwChannelQoEManager getInstance() {
        return mChannelQoEManager;
    }

    private HwChannelQoEManager(Context context) {
        this.mChannelQoEParm[0] = new HwChannelQoEParmStatistics();
        this.mChannelQoEParm[1] = new HwChannelQoEParmStatistics();
        this.mLastUploadTime = 0;
        this.listenerSim0 = new PhoneStateListener(0) {
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                SignalStrength unused = HwChannelQoEManager.this.mSignalStrengthSim0 = signalStrength;
            }
        };
        this.listenerSim1 = new PhoneStateListener(1) {
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                SignalStrength unused = HwChannelQoEManager.this.mSignalStrengthSim1 = signalStrength;
            }
        };
        logE("HwChannelQoEManager enter.");
        this.mContext = context;
        this.mCell = new CHMeasureObject("mCell", 801);
        this.mWifi = new CHMeasureObject("mWifi", 800);
        this.mQoEMonitor = new HwChannelQoEMonitor(this.mContext);
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mCurrentSignalState = new CurrentSignalState();
        regSignalListener();
        this.mCellHistoryMseasureInfo = new HistoryMseasureInfo();
        this.mWifiHistoryMseasureInfo = new HistoryMseasureInfo();
        this.mChannelQoEParm[0].mNetworkType = 801;
        this.mChannelQoEParm[1].mNetworkType = 800;
        this.mChQciManager = HwCHQciManager.getInstance();
        logE("HwChannelQoEManager create succ.");
    }

    /* access modifiers changed from: private */
    public void logE(String info) {
        Log.e(TAG, info);
    }

    public static void log(String info) {
        Log.e(TAG, info);
    }

    public void startWifiLinkMonitor(int UID, int scence, int qci, IChannelQoECallback callback) {
        logE("startWifiLinkMonitor enter.");
        logE("startWifiLinkMonitor enter, UID is " + UID);
        logE("startWifiLinkMonitor enter, scence is " + scence);
        logE("startWifiLinkMonitor enter, qci is " + qci);
        HwCHQciConfig chQciConfig = HwCHQciManager.getInstance().getChQciConfig(qci);
        HwChannelQoEAppInfo appQoeInfo = new HwChannelQoEAppInfo(UID, scence, 800, qci, callback);
        this.mQoEMonitor.startMonitor(appQoeInfo);
    }

    public void stopWifiLinkMonitor(int UID, boolean stopAll) {
        if (stopAll) {
            log("stopWifiLinkMonitor stop all.");
            this.mQoEMonitor.stopAll();
        } else if (UID == -1) {
            log("stopWifiLinkMonitor invalid UID.");
        } else {
            this.mQoEMonitor.stopMonitor(UID);
        }
    }

    public void queryCellPSAvailable(IChannelQoECallback callBack) {
        logE("queryCellPSAvailable enter.");
        if (queryCellSignalLevel(0) == 0) {
            this.mCell.queryAvailable(callBack);
            return;
        }
        logE("onCellPSAvailable false, reason CONNECT_SIGNAL_POOR.");
        callBack.onCellPSAvailable(false, 2);
    }

    public void queryChannelQuality(int UID, int scence, int networkType, int qci, IChannelQoECallback callback) {
        int i = networkType;
        logE("queryChannelQuality enter, UID:" + String.valueOf(UID) + ", scence:" + String.valueOf(scence) + ", networkType:" + String.valueOf(networkType) + ", qci:" + String.valueOf(qci));
        int i2 = qci;
        HwCHQciConfig config = HwCHQciManager.getInstance().getChQciConfig(i2);
        try {
            if (IS_CHINA) {
                if (isChina()) {
                    this.urlFirst = urlHiCloud;
                    this.getFirst = getHiCloud;
                    this.urlBak = urlBaidu;
                    this.getBak = getBaidu;
                    log("In China, using hicloud and baidu.");
                } else {
                    this.urlFirst = urlGstatic;
                    this.getFirst = getGstatic;
                    this.urlBak = urlGoogle;
                    this.getBak = getGoogle;
                    log("over sea, using gstatic and google.");
                }
            }
        } catch (Exception e) {
            log("set url exception. " + e.toString());
        }
        if (801 == i) {
            this.mCellHistoryMseasureInfo.reset();
            int unused = this.mCellHistoryMseasureInfo.tupBef = -1;
            int signal_level = queryCellSignalLevel(config.mTput);
            if (signal_level == 0) {
                logE("queryChannelQuality signal level good.");
                Message startMessage = Message.obtain();
                startMessage.what = MESSAGE_START_QUERY;
                HwChannelQoEAppInfo hwChannelQoEAppInfo = new HwChannelQoEAppInfo(UID, scence, i, i2, callback);
                startMessage.obj = hwChannelQoEAppInfo;
                this.mCell.mHandler.sendMessage(startMessage);
            } else if (signal_level == 1) {
                logE("queryChannelQuality signal level MODERATE.");
                Message startMessage2 = Message.obtain();
                startMessage2.what = MESSAGE_START_QUERY;
                HwChannelQoEAppInfo hwChannelQoEAppInfo2 = new HwChannelQoEAppInfo(UID, scence, i, i2, callback);
                startMessage2.obj = hwChannelQoEAppInfo2;
                this.mCell.mHandler.sendMessage(startMessage2);
            } else {
                logE("queryChannelQuality signal level bad.");
                int[] iArr = this.mChannelQoEParm[0].mRst.mRst;
                iArr[1] = iArr[1] + 1;
                callback.onChannelQuality(UID, scence, i, 1);
                return;
            }
            int i3 = UID;
            int i4 = scence;
            IChannelQoECallback iChannelQoECallback = callback;
            return;
        }
        int i5 = UID;
        int i6 = scence;
        IChannelQoECallback iChannelQoECallback2 = callback;
        if (800 == i) {
            this.mWifiHistoryMseasureInfo.reset();
            WifiManager mWManager = (WifiManager) this.mContext.getSystemService(DevSchedFeatureRT.WIFI_FEATURE);
            WifiInfo info = mWManager.getConnectionInfo();
            if (info != null) {
                int unused2 = this.mWifiHistoryMseasureInfo.sigPwr = info.getRssi();
                int unused3 = this.mWifiHistoryMseasureInfo.sigSnr = info.getSnr();
                int unused4 = this.mWifiHistoryMseasureInfo.sigQual = info.getNoise();
                int unused5 = this.mWifiHistoryMseasureInfo.sigLoad = info.getChload();
                int unused6 = this.mWifiHistoryMseasureInfo.tupBef = -1;
            }
            Message startMessage3 = Message.obtain();
            startMessage3.what = MESSAGE_START_QUERY;
            WifiManager wifiManager = mWManager;
            HwChannelQoEAppInfo hwChannelQoEAppInfo3 = r2;
            HwChannelQoEAppInfo hwChannelQoEAppInfo4 = new HwChannelQoEAppInfo(i5, i6, i, i2, iChannelQoECallback2);
            startMessage3.obj = hwChannelQoEAppInfo3;
            this.mWifi.mHandler.sendMessage(startMessage3);
            return;
        }
        logE("queryChannelQuality networkType error");
    }

    /* access modifiers changed from: private */
    public boolean isChina() {
        String operator = TelephonyManager.getDefault().getNetworkOperator();
        return (operator == null || operator.length() == 0 || !operator.startsWith("460")) ? false : true;
    }

    private void regSignalListener() {
        this.mTelephonyManager.listen(this.listenerSim0, 256);
        this.mTelephonyManager.listen(this.listenerSim1, 256);
    }

    public int queryCellSignalLevel(int tput_thresh) {
        SignalStrength signalStrength;
        int signal_level = 2;
        int subId = SubscriptionManager.getDefaultDataSubscriptionId();
        int net_type = this.mTelephonyManager.getNetworkType(subId);
        int RAT_class = TelephonyManager.getNetworkClass(net_type);
        if (subId == 0) {
            signalStrength = this.mSignalStrengthSim0;
        } else {
            signalStrength = this.mSignalStrengthSim1;
        }
        if (signalStrength == null) {
            return 2;
        }
        if (1 == RAT_class) {
            log("CellSignalLevel 2G");
            signal_level = 2;
        } else if (2 == RAT_class) {
            int unused = this.mCellHistoryMseasureInfo.rat = 2;
            if (tput_thresh > 2) {
                log("CellSignalLevel 3G");
                signal_level = 2;
            }
            int ecio = -1;
            if (net_type != 15) {
                switch (net_type) {
                    case 3:
                        break;
                    case 4:
                        ecio = signalStrength.getCdmaEcio();
                        break;
                    case 5:
                    case 6:
                        ecio = signalStrength.getEvdoEcio();
                        break;
                    default:
                        switch (net_type) {
                            case 8:
                            case 9:
                            case 10:
                                break;
                        }
                }
            }
            ecio = signalStrength.getWcdmaEcio();
            log("CellSignalLevel 3G = " + signalStrength.getLevel() + " ecio = " + ecio);
            int unused2 = this.mCellHistoryMseasureInfo.sigQual = ecio;
            int unused3 = this.mCellHistoryMseasureInfo.sigPwr = signalStrength.getWcdmaRscp();
            if (ecio == -1) {
                if (signalStrength.getLevel() >= 3) {
                    signal_level = 0;
                }
            } else if (signalStrength.getLevel() >= 2 && ecio > -12) {
                signal_level = 1;
            }
        } else if (3 == RAT_class) {
            int unused4 = this.mCellHistoryMseasureInfo.rat = 1;
            int rsrq = signalStrength.getLteRsrq();
            int sinr = signalStrength.getLteRssnr();
            int unused5 = this.mCellHistoryMseasureInfo.sigPwr = signalStrength.getLteRsrp();
            int unused6 = this.mCellHistoryMseasureInfo.sigSnr = sinr;
            int unused7 = this.mCellHistoryMseasureInfo.sigQual = rsrq;
            log("CellSignalLevel LTE = " + signalStrength.getLevel() + " rsrq = " + rsrq + " sinr = " + sinr);
            if (signalStrength.getLevel() >= 2) {
                if (rsrq <= -15 || sinr <= 3) {
                    signal_level = 1;
                } else {
                    signal_level = 0;
                }
            } else if (signalStrength.getLevel() != 1 || rsrq <= -12 || sinr <= 11) {
                signal_level = 2;
            } else {
                signal_level = 1;
            }
        } else {
            log("CellSignalLevel unknown RAT!");
        }
        log("signal_level is " + String.valueOf(signal_level));
        return signal_level;
    }

    public HwChannelQoEParmStatistics[] getCHQoEParmStatistics() {
        return (HwChannelQoEParmStatistics[]) this.mChannelQoEParm.clone();
    }

    public HistoryMseasureInfo getHistoryMseasureInfo(int networkType) {
        log("enter getHistoryMseasureInfo: networkType " + String.valueOf(networkType));
        if (801 == networkType) {
            return this.mCellHistoryMseasureInfo;
        }
        return this.mWifiHistoryMseasureInfo;
    }

    public CurrentSignalState getCurrentSignalState(int networkType, boolean probeRTT, IChannelQoECallback callback) {
        SignalStrength signalStrength;
        log("enter getCurrentSignalState: networkType probeRTT  " + String.valueOf(networkType) + " " + String.valueOf(probeRTT));
        if (801 == networkType) {
            int subId = SubscriptionManager.getDefaultDataSubscriptionId();
            int RAT_class = TelephonyManager.getNetworkClass(this.mTelephonyManager.getNetworkType(subId));
            if (subId == 0) {
                signalStrength = this.mSignalStrengthSim0;
            } else {
                signalStrength = this.mSignalStrengthSim1;
            }
            if (signalStrength == null) {
                return null;
            }
            log("getCurrentSignalState RAT is " + String.valueOf(RAT_class));
            if (2 == RAT_class) {
                int unused = this.mCurrentSignalState.networkType = 2;
                int unused2 = this.mCurrentSignalState.sigPwr = signalStrength.getWcdmaRscp();
                int unused3 = this.mCurrentSignalState.sigSnr = -1;
                int unused4 = this.mCurrentSignalState.sigQual = signalStrength.getWcdmaEcio();
                int unused5 = this.mCurrentSignalState.sigLoad = -1;
            } else if (3 == RAT_class) {
                int unused6 = this.mCurrentSignalState.networkType = 1;
                int unused7 = this.mCurrentSignalState.sigPwr = signalStrength.getLteRsrp();
                int unused8 = this.mCurrentSignalState.sigSnr = signalStrength.getLteRssnr();
                int unused9 = this.mCurrentSignalState.sigQual = signalStrength.getLteRsrq();
                int unused10 = this.mCurrentSignalState.sigLoad = -1;
            } else {
                int unused11 = this.mCurrentSignalState.networkType = -1;
                int unused12 = this.mCurrentSignalState.sigPwr = -1;
                int unused13 = this.mCurrentSignalState.sigSnr = -1;
                int unused14 = this.mCurrentSignalState.sigQual = -1;
                int unused15 = this.mCurrentSignalState.sigLoad = -1;
            }
            log("getCurrentSignalState sigPwr = " + this.mCurrentSignalState.sigPwr + " sigSnr = " + this.mCurrentSignalState.sigSnr + " sigQual = " + this.mCurrentSignalState.sigQual + " sigLoad = " + this.mCurrentSignalState.sigLoad);
        } else if (800 == networkType) {
            WifiInfo info = ((WifiManager) this.mContext.getSystemService(DevSchedFeatureRT.WIFI_FEATURE)).getConnectionInfo();
            if (info != null) {
                int unused16 = this.mCurrentSignalState.sigPwr = info.getRssi();
                int unused17 = this.mCurrentSignalState.sigSnr = info.getSnr();
                int unused18 = this.mCurrentSignalState.sigQual = info.getNoise();
                int unused19 = this.mCurrentSignalState.sigLoad = info.getChload();
                log("getCurrentSignalState sigPwr = " + this.mCurrentSignalState.sigPwr + " sigSnr = " + this.mCurrentSignalState.sigSnr + " sigQual = " + this.mCurrentSignalState.sigQual + " sigLoad = " + this.mCurrentSignalState.sigLoad);
            }
            int unused20 = this.mCurrentSignalState.networkType = 0;
        }
        if (true == probeRTT) {
            this.mGetCurrentRttFlag = 0;
            queryChannelQuality(-1, -1, networkType, 0, callback);
        }
        return this.mCurrentSignalState;
    }

    public void uploadChannelQoEParmStatistics(long uploadInterval) {
        if (this.mLastUploadTime == 0 || System.currentTimeMillis() - this.mLastUploadTime > uploadInterval) {
            try {
                log("enter uploadChannelQoEparamStatistics.");
                IMonitor.EventStream event_909002054 = IMonitor.openEventStream(E909002054);
                for (int i = 0; i < 2; i++) {
                    IMonitor.EventStream event_E909009040 = IMonitor.openEventStream(E909009040);
                    event_E909009040.setParam("NET", this.mChannelQoEParm[i].mNetworkType);
                    IMonitor.EventStream event_E909009039 = IMonitor.openEventStream(E909009039);
                    for (int rst = 0; rst < 10; rst++) {
                        event_E909009039.setParam(LVl, 101 + rst).setParam(Cnt, this.mChannelQoEParm[i].mRst.mRst[rst]);
                        event_E909009040.fillArrayParam("RST", event_E909009039);
                    }
                    for (int svr = 0; svr < 5; svr++) {
                        event_E909009039.setParam(LVl, 201 + svr).setParam(Cnt, this.mChannelQoEParm[i].mSvr.mSvr[svr]);
                        event_E909009040.fillArrayParam("SVR", event_E909009039);
                    }
                    for (int rtt = 0; rtt < 10; rtt++) {
                        event_E909009039.setParam(LVl, 301 + rtt).setParam(Cnt, this.mChannelQoEParm[i].mRtt.mRtt[rtt]);
                        event_E909009040.fillArrayParam("RTT", event_E909009039);
                    }
                    for (int drtt = 0; drtt < 10; drtt++) {
                        event_E909009039.setParam(LVl, AwareJobSchedulerService.MSG_JOB_EXPIRED + drtt).setParam(Cnt, this.mChannelQoEParm[i].mdRtt.reserved[drtt]);
                        event_E909009040.fillArrayParam("DRTT", event_E909009039);
                    }
                    for (int tpt = 0; tpt < 10; tpt++) {
                        event_E909009039.setParam(LVl, 501 + tpt).setParam(Cnt, this.mChannelQoEParm[i].mTpt.reserved[tpt]);
                        event_E909009040.fillArrayParam("TPT", event_E909009039);
                    }
                    IMonitor.closeEventStream(event_E909009039);
                    event_909002054.fillArrayParam("CHQOEINFO", event_E909009040);
                    IMonitor.closeEventStream(event_E909009040);
                }
                IMonitor.sendEvent(event_909002054);
                IMonitor.closeEventStream(event_909002054);
                this.mChannelQoEParm[0].reset();
                this.mChannelQoEParm[1].reset();
                this.mLastUploadTime = System.currentTimeMillis();
            } catch (RuntimeException e) {
                log("uploadChannelQoEParmStatistics RuntimeException");
                throw e;
            } catch (Exception e2) {
                log("uploadChannelQoEparamStatistics exception" + e2.toString());
            }
        } else {
            log("uploadChannelQoEParmStatistics: upload condition not allowed.");
        }
    }
}

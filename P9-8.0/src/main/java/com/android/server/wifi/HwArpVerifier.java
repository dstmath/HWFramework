package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.IpConfiguration.IpAssignment;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.Uri;
import android.net.arp.HWArpPeer;
import android.net.arp.HWMultiGW;
import android.net.netlink.NetlinkMessage;
import android.net.netlink.NetlinkSocket;
import android.net.netlink.RtNetlinkMessage;
import android.net.netlink.StructNlMsgHdr;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.INetworkManagementService;
import android.os.INetworkManagementService.Stub;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.HwServiceFactory;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.ncdft.HwWifiDFTConnManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;
import org.json.JSONException;
import org.json.JSONObject;

public class HwArpVerifier {
    private static final long ACCESS_BAIDU_TIMEOUT = 10000;
    private static final String ACTION_ARP_RECONNECT_WIFI = "android.net.wifi.ARP_RECONNECT_WIFI";
    private static final String APP_PACKAGE_NAME = "packageName";
    private static final String APP_UID = "uid";
    private static final int ARP_REASSOC_OK = 8;
    private static final String AUTHORITY_NETAPP = "com.huawei.systemmanager.NetAssistantDBProvider";
    private static final String BCM_ROAMING_FLAG_FILE = "";
    private static final boolean BETA_VER;
    private static final String CMD52 = "CMD52_TOTAL";
    private static final String CMD53 = "CMD53_TOTAL";
    private static final String CMD_READ = "READ";
    private static final String CMD_SLEEPCSR_CLR = "CMD_SLEEPCSR_CLR";
    private static final String CMD_SLEEPCSR_SET = "CMD_SLEEPCSR_SET";
    private static final String CMD_WRITE = "WIRTE";
    private static final boolean DBG = true;
    private static final int DEFAULT_ARP_NUM = 1;
    private static final int DEFAULT_ARP_PING_TIMEOUT_MS = 100;
    private static final int DEFAULT_FUL_PING_TIMEOUT_MS = 5000;
    private static final int DEFAULT_GARP_TIMEOUT_MS = 1000;
    private static final int DEFAULT_MIN_ARP_RESPONSES = 1;
    private static final int DEFAULT_MIN_RESPONSE = 1;
    private static final int DEFAULT_NUM_ARP_PINGS = 2;
    private static final int DEFAULT_SIG_PING_TIMEOUT_MS = 1000;
    private static final String DIAGNOSE_COMPLETE_ACTION = "com.huawei.network.DIAGNOSE_COMPLETE";
    private static final int DYNAMIC_ARP_CHECK = 2;
    private static final int FULL_ARP_CHECK = 1;
    private static final String HI1102_ROAMING_FLAG_FILE = "/sys/hisys/hmac/vap/roam_status";
    private static final String HI110X_ROAMING_FLAG_FILE = "/sys/hi110x/roam_status";
    private static final String HISI_AP_DISTANCE = "ap_distance";
    private static String HISI_CHIPSET_DEBUG_FILE = "/sys/hisys/wal/dev_wifi_info";
    private static final String HISI_DISTURBING_DEGREE = "disturbing_degree";
    private static final String HISI_LOST_BEACON_AMOUNT = "lost_beacon_amount";
    private static final String HISI_MONITOR_INTERFAL = "monitor_interval";
    private static final String HISI_RX_BEACON_FROM_ASSOC_AP = "rx_beacon_from_assoc_ap";
    private static final String HISI_RX_BYTE_AMOUNT = "rx_byte_amount";
    private static final String HISI_RX_FRAME_AMOUNT = "rx_frame_amount";
    private static final String HISI_TX_BYTE_AMOUNT = "tx_byte_amount";
    private static final String HISI_TX_DATA_FRAME_ERROR_AMOUNT = "tx_data_frame_err_amount";
    private static final String HISI_TX_FRAME_AMOUNT = "tx_frame_amount";
    private static final String HISI_TX_RETRANS_AMOUNT = "tx_retrans_amount";
    private static final int HTTP_ACCESS_OK = 200;
    private static final int HTTP_ACCESS_TIMEOUT_RESP = 599;
    private static final String IFACE = "wlan0";
    private static final long LONG_ARP_FAIL_DURATION = 86400000;
    private static final int LONG_ARP_FAIL_TIMES_THRESHOLD = 6;
    private static final int MAX_ARP_FAIL_COUNT = 15;
    public static final int MSG_DUMP_LOG = 1010;
    private static final int MSG_WIFI_ARP_FAILED = 14;
    private static final String PACKAGE_NAME = "HwArpVerifier";
    private static final String PERMISSION_CFG = "net_permissionCfg";
    public static final String SDIO_DEBUG_FILENAME = "/sys/kernel/debug/bcmdhd/debug_sdio_quality";
    private static final long SHORT_ARP_FAIL_DURATION = 3600000;
    private static final int SHORT_ARP_FAIL_TIMES_THRESHOLD = 2;
    private static final int SINGLE_ARP_CHECK = 0;
    private static final long SLEEP_PERIOD_TIMEOUT = 30000;
    private static final long SPEED_OF_WEB = 20480;
    private static final String TABLE_NETAPP = "permissionCfg";
    private static final String TABLE_NETAPP_SYSTEM = "permissionCfg_system";
    private static final String TAG = "HwArpVerifier";
    private static final Uri URI_NETAPP = Uri.parse("content://com.huawei.systemmanager.NetAssistantDBProvider/permissionCfg");
    private static final Uri URI_NETAPP_SYSTEM = Uri.parse("content://com.huawei.systemmanager.NetAssistantDBProvider/permissionCfg_system");
    private static final int WEAK_SIGNAL_THRESHOLD = -83;
    public static final String WEB_BAIDU = "http://www.baidu.com";
    public static final String WEB_CHINAZ_GETIP = "http://ip.chinaz.com/getip.aspx";
    private static final String WIFI_ARP_TIMEOUT = "/sys/devices/platform/bcmdhd_wlan.1/wifi_arp_timeout";
    private static final int WIFI_DISABLE1 = 16384;
    private static final int WIFI_DISABLE2 = 24567;
    private static final int WIFI_STATE_CONNECTED = 1;
    private static final int WIFI_STATE_DISCONNECTED = -1;
    private static final int WIFI_STATE_INITIALED = 0;
    private static final String WIFI_WRONG_ACTION_FLAG = "/sys/devices/platform/bcmdhd_wlan.1/wifi_wrong_action_flag";
    private static HwArpVerifier arp_instance = null;
    private static final int[] dynamicPings = new int[]{1, 2, 4, 5, 5};
    private static ReentrantLock mLock = new ReentrantLock();
    private static int mRSSI = 0;
    private int errCode = 0;
    private boolean isMobileDateActive = false;
    private boolean isRouteRepareSwitchEnabled = SystemProperties.getBoolean("ro.config.hw_route_repare", false);
    private AccessWebStatus mAccessWebStatus = new AccessWebStatus();
    private ArrayList<ArpItem> mArpBlacklist = new ArrayList();
    private ArrayList<ArpItem> mArpItems = new ArrayList();
    private ConnectivityManager mCM = null;
    private int mCheckStateToken = 0;
    private ClientHandler mClientHandler = null;
    private Context mContext = null;
    private int mCurrentWiFiState = 0;
    private boolean mFirstDetect = true;
    private String mGateway = null;
    private HWGatewayVerifier mHWGatewayVerifier = new HWGatewayVerifier(this, null);
    private HwWiFiLogUtils mHwLogUtils = null;
    private boolean mIsFirstCheck = true;
    private int mLastNetworkId = -1;
    private String mLastSSID = null;
    private long mLongDurationStartTime = 0;
    private int mLongTriggerCnt = 0;
    private HWNetstatManager mNetstatManager = null;
    private NetworkInfo mNetworkInfo = new NetworkInfo(1, 0, "WIFI", BCM_ROAMING_FLAG_FILE);
    private INetworkManagementService mNwService;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        private static final /* synthetic */ int[] -android-net-NetworkInfo$DetailedStateSwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$android$net$NetworkInfo$DetailedState;

        private static /* synthetic */ int[] -getandroid-net-NetworkInfo$DetailedStateSwitchesValues() {
            if (-android-net-NetworkInfo$DetailedStateSwitchesValues != null) {
                return -android-net-NetworkInfo$DetailedStateSwitchesValues;
            }
            int[] iArr = new int[DetailedState.values().length];
            try {
                iArr[DetailedState.AUTHENTICATING.ordinal()] = 3;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[DetailedState.BLOCKED.ordinal()] = 4;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[DetailedState.CAPTIVE_PORTAL_CHECK.ordinal()] = 5;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[DetailedState.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[DetailedState.CONNECTING.ordinal()] = 6;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[DetailedState.DISCONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[DetailedState.DISCONNECTING.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[DetailedState.FAILED.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[DetailedState.IDLE.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[DetailedState.OBTAINING_IPADDR.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[DetailedState.SCANNING.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[DetailedState.SUSPENDED.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                iArr[DetailedState.VERIFYING_POOR_LINK.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            -android-net-NetworkInfo$DetailedStateSwitchesValues = iArr;
            return iArr;
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("android.net.wifi.STATE_CHANGE")) {
                    HwArpVerifier.this.mNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (HwArpVerifier.this.mNetworkInfo != null) {
                        switch (AnonymousClass1.-getandroid-net-NetworkInfo$DetailedStateSwitchesValues()[HwArpVerifier.this.mNetworkInfo.getDetailedState().ordinal()]) {
                            case 1:
                                if (intent.hasExtra("linkProperties")) {
                                    HwArpVerifier.this.mRevLinkProperties = (LinkProperties) intent.getParcelableExtra("linkProperties");
                                } else {
                                    HwArpVerifier.this.mRevLinkProperties = null;
                                }
                                HwArpVerifier.this.rssi_summery.resetRSSIGroup();
                                break;
                            case 2:
                                break;
                        }
                        if (HwArpVerifier.this.mClientHandler != null) {
                            HwArpVerifier.this.mClientHandler.monitorWifiNetworkState();
                        }
                        HwArpVerifier.this.mWM = (WifiManager) HwArpVerifier.this.mContext.getSystemService("wifi");
                    }
                } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                    HwArpVerifier.this.handleWifiSwitchChanged(intent.getIntExtra("wifi_state", 4));
                } else if (action.equals("android.intent.action.SCREEN_ON")) {
                    if (HwArpVerifier.this.isConnectedToWifi()) {
                        HwArpVerifier.this.stopWifiRouteCheck();
                        HwArpVerifier.this.startWifiRouteCheck();
                    }
                } else if (action.equals("android.intent.action.ANY_DATA_STATE")) {
                    String dataState = intent.getStringExtra("state");
                    if ("CONNECTED".equalsIgnoreCase(dataState)) {
                        HwArpVerifier.this.isMobileDateActive = true;
                    } else if ("DISCONNECTED".equalsIgnoreCase(dataState)) {
                        HwArpVerifier.this.isMobileDateActive = false;
                    }
                }
            }
        }
    };
    private boolean mRegisterReceiver = false;
    private LinkProperties mRevLinkProperties = null;
    private String mRoamingFlagFile = null;
    private int mRouteDetectCnt = 0;
    private Handler mServiceHandler = null;
    private long mShortDurationStartTime = 0;
    private int mShortTriggerCnt = 0;
    private int mSpendTime = 0;
    private HandlerThread mThread = null;
    private WifiManager mWM = null;
    private WifiNative mWifiNative = null;
    private long mlDetectWebCounter = 0;
    private HwCHRWifiRSSIGroupSummery rssi_summery = null;
    private HwWifiCHRStateManager wcsm = null;
    private HwCHRWebMonitor webMonitor = null;

    protected static class AccessWebStatus {
        int mAccessNetFailedCount;
        private int mAp_distance;
        int mDetectWebStatus;
        String mDisableWlanApps;
        private int mDisturbing_degree;
        int mDnsFailedCnt;
        String mIpRoute;
        private int mLost_beacon_amount;
        private int mMonitor_interval;
        int mNetAccessibleTime = 0;
        int mNetDisableCnt;
        int mNetUnusableTime = 0;
        int mOldDnsFailedCnt;
        long mPeriodTime;
        private int mPrevAp_distance;
        private int mPrevDisturbing_degree;
        private int mPrevLost_beacon_amount;
        private int mPrevRx_beacon_from_assoc_ap;
        private int mPrevRx_byte_amount;
        private int mPrevRx_frame_amount;
        private int mPrevTx_byte_amount;
        private int mPrevTx_data_frame_error_amount;
        private int mPrevTx_frame_amount;
        private int mPrevTx_retrans_amount;
        int mRttArp;
        int mRttBaidu;
        int mRxCnt;
        int mRxSleepCnt;
        private int mRx_beacon_from_assoc_ap;
        private int mRx_byte_amount;
        private int mRx_frame_amount;
        int mSdio_info_ksoclrreq;
        int mSdio_info_ksoclrretry;
        int mSdio_info_ksosetreq;
        int mSdio_info_ksosetretry;
        int mSdio_info_readb;
        int mSdio_info_readbreq;
        int mSdio_info_readw;
        int mSdio_info_readwreq;
        int mSdio_info_writeb;
        int mSdio_info_writebreq;
        int mSdio_info_writew;
        int mSdio_info_writewreq;
        String mStrChinazAddr;
        String mStrChinazIp;
        int mTxCnt;
        private int mTx_byte_amount;
        private int mTx_data_frame_error_amount;
        private int mTx_frame_amount;
        private int mTx_retrans_amount;
        WebParam mWP;

        public void setMonitor_interval(int interval) {
            this.mMonitor_interval = interval;
        }

        public int getMonitor_interval() {
            return this.mMonitor_interval;
        }

        public void setTx_frame_amount(int txAmount) {
            this.mTx_frame_amount = txAmount;
        }

        public int getTx_frame_amount() {
            return this.mTx_frame_amount;
        }

        public void setTx_byte_amount(int txBytes) {
            this.mTx_byte_amount = txBytes;
        }

        public int getTx_byte_amount() {
            return this.mTx_byte_amount;
        }

        public void setTx_data_frame_error_amount(int txDataCount) {
            this.mTx_data_frame_error_amount = txDataCount;
        }

        public int getTx_data_frame_error_amount() {
            return this.mTx_data_frame_error_amount;
        }

        public void setTx_retrans_amount(int txRetransCount) {
            this.mTx_retrans_amount = txRetransCount;
        }

        public int getTx_retrans_amount() {
            return this.mTx_retrans_amount;
        }

        public void setRx_frame_amount(int rxFrameCount) {
            this.mRx_frame_amount = rxFrameCount;
        }

        public int getRx_frame_amount() {
            return this.mRx_frame_amount;
        }

        public void setRx_byte_amount(int rxByteCount) {
            this.mRx_byte_amount = rxByteCount;
        }

        public int getRx_byte_amount() {
            return this.mRx_byte_amount;
        }

        public void setRx_beacon_from_assoc_ap(int rxBeacon) {
            this.mRx_beacon_from_assoc_ap = rxBeacon;
        }

        public int getRx_beacon_from_assoc_ap() {
            return this.mRx_beacon_from_assoc_ap;
        }

        public void setAp_distance(int apDistance) {
            this.mAp_distance = apDistance;
        }

        public int getAp_distance() {
            return this.mAp_distance;
        }

        public void setDisturbing_degree(int degree) {
            this.mDisturbing_degree = degree;
        }

        public int getDisturbing_degree() {
            return this.mDisturbing_degree;
        }

        public void setLost_beacon_amount(int lostBeacon) {
            this.mLost_beacon_amount = lostBeacon;
        }

        public int getLost_beacon_amount() {
            return this.mLost_beacon_amount;
        }

        public void setPrevTx_frame_amount(int txAmount) {
            this.mPrevTx_frame_amount = txAmount;
        }

        public int getPrevTx_frame_amount() {
            return this.mPrevTx_frame_amount;
        }

        public void setPrevTx_byte_amount(int txBytes) {
            this.mPrevTx_byte_amount = txBytes;
        }

        public int getPrevTx_byte_amount() {
            return this.mPrevTx_byte_amount;
        }

        public void setPrevTx_data_frame_error_amount(int txDataCount) {
            this.mPrevTx_data_frame_error_amount = txDataCount;
        }

        public int getPrevTx_data_frame_error_amount() {
            return this.mPrevTx_data_frame_error_amount;
        }

        public void setPrevTx_retrans_amount(int txRetransCount) {
            this.mPrevTx_retrans_amount = txRetransCount;
        }

        public int getPrevTx_retrans_amount() {
            return this.mPrevTx_retrans_amount;
        }

        public void setPrevRx_frame_amount(int rxFrameCount) {
            this.mPrevRx_frame_amount = rxFrameCount;
        }

        public int getPrevRx_frame_amount() {
            return this.mPrevRx_frame_amount;
        }

        public void setPrevRx_byte_amount(int rxByteCount) {
            this.mPrevRx_byte_amount = rxByteCount;
        }

        public int getPrevRx_byte_amount() {
            return this.mPrevRx_byte_amount;
        }

        public void setPrevRx_beacon_from_assoc_ap(int rxBeacon) {
            this.mPrevRx_beacon_from_assoc_ap = rxBeacon;
        }

        public int getPrevRx_beacon_from_assoc_ap() {
            return this.mPrevRx_beacon_from_assoc_ap;
        }

        public void setPrevAp_distance(int apDistance) {
            this.mPrevAp_distance = apDistance;
        }

        public int getPrevAp_distance() {
            return this.mPrevAp_distance;
        }

        public void setPrevDisturbing_degree(int degree) {
            this.mPrevDisturbing_degree = degree;
        }

        public int getPrevDisturbing_degree() {
            return this.mPrevDisturbing_degree;
        }

        public void setPrevLost_beacon_amount(int lostBeacon) {
            this.mPrevLost_beacon_amount = lostBeacon;
        }

        public int getPrevLost_beacon_amount() {
            return this.mPrevLost_beacon_amount;
        }

        public void savePrevChipsetData() {
            setPrevTx_frame_amount(getTx_frame_amount());
            setPrevTx_byte_amount(getTx_byte_amount());
            setPrevTx_data_frame_error_amount(getTx_data_frame_error_amount());
            setPrevTx_retrans_amount(getTx_retrans_amount());
            setPrevRx_frame_amount(getRx_frame_amount());
            setPrevRx_byte_amount(getRx_byte_amount());
            setPrevRx_beacon_from_assoc_ap(getRx_beacon_from_assoc_ap());
            setPrevAp_distance(getAp_distance());
            setPrevDisturbing_degree(getDisturbing_degree());
            setPrevLost_beacon_amount(getLost_beacon_amount());
        }

        public AccessWebStatus() {
            reset();
        }

        public AccessWebStatus(AccessWebStatus aws) {
            setRxCnt(aws.getRxCnt());
            setTxCnt(aws.getTxCnt());
            setNetDisableCnt(aws.getNetDisableCnt());
            setDNSFailed(aws.getDNSFailed());
            setChinazIp(aws.getChinazIp());
            setChinazAddr(aws.getChinazAddr());
            setRTTArp(aws.getRTTArp());
            setRTTBaidu(aws.getRTTBaidu());
            setIPRouteRet(aws.getIPRouteRet());
            setDisableWlanApps(aws.getDisableWlanApps());
            setRxSleepCnt(aws.getRxSleepCnt());
            setPeriodTime(aws.getPeriodTime());
            set_sdio_info_readbreq(aws.get_sdio_info_readbreq());
            set_sdio_info_readb(aws.get_sdio_info_readb());
            set_sdio_info_writebreq(aws.get_sdio_info_writebreq());
            set_sdio_info_writeb(aws.get_sdio_info_writeb());
            set_sdio_info_readwreq(aws.get_sdio_info_readwreq());
            set_sdio_info_readw(aws.get_sdio_info_readw());
            set_sdio_info_writewreq(aws.get_sdio_info_writewreq());
            set_sdio_info_writew(aws.get_sdio_info_writew());
            set_sdio_info_ksosetreq(aws.get_sdio_info_ksosetreq());
            set_sdio_info_ksosetretry(aws.get_sdio_info_ksosetretry());
            set_sdio_info_ksoclrreq(aws.get_sdio_info_ksoclrreq());
            set_sdio_info_ksoclrretry(aws.get_sdio_info_ksoclrretry());
            setOldDnsFailedCnt(aws.getOldDnsFailedCnt());
            setWebParam(aws.getWebParam());
            setDetectWebStatus(aws.getDetectWebStatus());
            setAccessNetFailedCount(aws.getAccessNetFailedCount());
            setNetAccessibleTime((long) aws.getNetAccessibleTime());
            setNetUnusableTime((long) aws.getNetUnusableTime());
            setMonitor_interval(aws.getMonitor_interval());
            setTx_frame_amount(aws.getTx_frame_amount());
            setTx_byte_amount(aws.getTx_byte_amount());
            setTx_data_frame_error_amount(aws.getTx_data_frame_error_amount());
            setTx_retrans_amount(aws.getTx_retrans_amount());
            setRx_frame_amount(aws.getRx_frame_amount());
            setRx_byte_amount(aws.getRx_byte_amount());
            setRx_beacon_from_assoc_ap(aws.getRx_beacon_from_assoc_ap());
            setAp_distance(aws.getAp_distance());
            setDisturbing_degree(aws.getDisturbing_degree());
            setLost_beacon_amount(aws.getLost_beacon_amount());
            setPrevTx_frame_amount(aws.getPrevTx_frame_amount());
            setPrevTx_byte_amount(aws.getPrevTx_byte_amount());
            setPrevTx_data_frame_error_amount(aws.getPrevTx_data_frame_error_amount());
            setPrevTx_retrans_amount(aws.getPrevTx_retrans_amount());
            setPrevRx_frame_amount(aws.getPrevRx_frame_amount());
            setPrevRx_byte_amount(aws.getPrevRx_byte_amount());
            setPrevRx_beacon_from_assoc_ap(aws.getPrevRx_beacon_from_assoc_ap());
            setPrevAp_distance(aws.getPrevAp_distance());
            setPrevDisturbing_degree(aws.getPrevDisturbing_degree());
            setPrevLost_beacon_amount(aws.getPrevLost_beacon_amount());
        }

        public int getAccessNetFailedCount() {
            return this.mAccessNetFailedCount;
        }

        public void setAccessNetFailedCount(int count) {
            this.mAccessNetFailedCount = count;
        }

        public int getDetectWebStatus() {
            return this.mDetectWebStatus;
        }

        public void setDetectWebStatus(int counter) {
            this.mDetectWebStatus = counter;
        }

        public WebParam getWebParam() {
            return this.mWP;
        }

        public void setWebParam(WebParam wp) {
            this.mWP = wp;
        }

        public int getRxCnt() {
            return this.mRxCnt;
        }

        public void setRxCnt(int r) {
            this.mRxCnt = r;
        }

        public int getTxCnt() {
            return this.mTxCnt;
        }

        public void setTxCnt(int t) {
            this.mTxCnt = t;
        }

        public int getNetDisableCnt() {
            return this.mNetDisableCnt;
        }

        public void setNetDisableCnt(int cnt) {
            this.mNetDisableCnt = cnt;
        }

        public int getDNSFailed() {
            return this.mDnsFailedCnt;
        }

        public void setDNSFailed(int cnt) {
            this.mDnsFailedCnt = cnt;
        }

        public String getChinazIp() {
            return this.mStrChinazIp;
        }

        public void setChinazIp(String ip) {
            this.mStrChinazIp = ip;
        }

        public String getChinazAddr() {
            return this.mStrChinazAddr;
        }

        public void setChinazAddr(String addr) {
            this.mStrChinazAddr = addr;
        }

        public int getRTTArp() {
            return this.mRttArp;
        }

        public void setRTTArp(int arp) {
            this.mRttArp = arp;
        }

        public int getRTTBaidu() {
            return this.mRttBaidu;
        }

        public void setRTTBaidu(int baidu) {
            this.mRttBaidu = baidu;
        }

        public String getIPRouteRet() {
            return this.mIpRoute;
        }

        public void setIPRouteRet(String ipRoute) {
            this.mIpRoute = ipRoute;
        }

        public String getDisableWlanApps() {
            return this.mDisableWlanApps;
        }

        public void setDisableWlanApps(String apps) {
            this.mDisableWlanApps = apps;
        }

        public void setRxSleepCnt(int rx) {
            this.mRxSleepCnt = rx;
        }

        public int getRxSleepCnt() {
            return this.mRxSleepCnt;
        }

        public void setPeriodTime(long timeout) {
            this.mPeriodTime = timeout;
        }

        public long getPeriodTime() {
            return this.mPeriodTime;
        }

        public int get_sdio_info_readbreq() {
            return this.mSdio_info_readbreq;
        }

        public void set_sdio_info_readbreq(int req) {
            this.mSdio_info_readbreq = req;
        }

        public int get_sdio_info_readb() {
            return this.mSdio_info_readb;
        }

        public void set_sdio_info_readb(int real) {
            this.mSdio_info_readb = real;
        }

        public int get_sdio_info_writebreq() {
            return this.mSdio_info_writebreq;
        }

        public void set_sdio_info_writebreq(int req) {
            this.mSdio_info_writebreq = req;
        }

        public int get_sdio_info_writeb() {
            return this.mSdio_info_writeb;
        }

        public void set_sdio_info_writeb(int real) {
            this.mSdio_info_writeb = real;
        }

        public int get_sdio_info_readwreq() {
            return this.mSdio_info_readwreq;
        }

        public void set_sdio_info_readwreq(int req) {
            this.mSdio_info_readwreq = req;
        }

        public int get_sdio_info_readw() {
            return this.mSdio_info_readw;
        }

        public void set_sdio_info_readw(int real) {
            this.mSdio_info_readw = real;
        }

        public int get_sdio_info_writewreq() {
            return this.mSdio_info_writewreq;
        }

        public void set_sdio_info_writewreq(int req) {
            this.mSdio_info_writewreq = req;
        }

        public int get_sdio_info_writew() {
            return this.mSdio_info_writew;
        }

        public void set_sdio_info_writew(int real) {
            this.mSdio_info_writew = real;
        }

        public int get_sdio_info_ksosetreq() {
            return this.mSdio_info_ksosetreq;
        }

        public void set_sdio_info_ksosetreq(int req) {
            this.mSdio_info_ksosetreq = req;
        }

        public int get_sdio_info_ksosetretry() {
            return this.mSdio_info_ksosetretry;
        }

        public void set_sdio_info_ksosetretry(int real) {
            this.mSdio_info_ksosetretry = real;
        }

        public int get_sdio_info_ksoclrreq() {
            return this.mSdio_info_ksoclrreq;
        }

        public void set_sdio_info_ksoclrreq(int req) {
            this.mSdio_info_ksoclrreq = req;
        }

        public int get_sdio_info_ksoclrretry() {
            return this.mSdio_info_ksoclrretry;
        }

        public void set_sdio_info_ksoclrretry(int real) {
            this.mSdio_info_ksoclrretry = real;
        }

        public int getOldDnsFailedCnt() {
            return this.mOldDnsFailedCnt;
        }

        public void setOldDnsFailedCnt(int cnt) {
            this.mOldDnsFailedCnt = cnt;
        }

        public int getNetAccessibleTime() {
            return this.mNetAccessibleTime;
        }

        public void setNetAccessibleTime(long duration) {
            this.mNetAccessibleTime = Integer.parseInt(String.valueOf(duration));
        }

        public int getNetUnusableTime() {
            return this.mNetUnusableTime;
        }

        public void setNetUnusableTime(long duration) {
            this.mNetUnusableTime = Integer.parseInt(String.valueOf(duration));
        }

        public String toString() {
            return "rx = " + this.mRxCnt + ", tx = " + this.mTxCnt + ", mNetDisableCnt = " + this.mNetDisableCnt + ", mDnsFailedCnt = " + this.mDnsFailedCnt + ", mStrChinazIp = " + this.mStrChinazIp + ", mStrChinazAddr = " + this.mStrChinazAddr + ", mRttArp = " + this.mRttArp + ", mRttBaidu = " + this.mRttBaidu + ", mIpRoute = " + this.mIpRoute + ", disable_wlan_apps = " + this.mDisableWlanApps + ", rxCntSleep = " + this.mRxSleepCnt + ", OldDnsFailedCnt = " + this.mOldDnsFailedCnt + ", mDetectWebStatus = " + this.mDetectWebStatus + ", mAccessNetFailedCount = " + this.mAccessNetFailedCount + ", mNetAccessibleTime = " + this.mNetAccessibleTime + ", mNetUnusableTime = " + this.mNetUnusableTime + ", mMonitor_interval = " + this.mMonitor_interval + " , mTx_frame_amount = " + this.mTx_frame_amount + " , mTx_byte_amount = " + this.mTx_byte_amount + " , mTx_data_frame_error_amount = " + this.mTx_data_frame_error_amount + " , mTx_retrans_amount = " + this.mTx_retrans_amount + " , mRx_frame_amount = " + this.mRx_frame_amount + " , mRx_byte_amount =" + this.mRx_byte_amount + " , mRx_beacon_from_assoc_ap = " + this.mRx_beacon_from_assoc_ap + " , mAp_distance = " + this.mAp_distance + " , mDisturbing_degree = " + this.mDisturbing_degree + " , mLost_beacon_amount = " + this.mLost_beacon_amount;
        }

        public String toSdioString() {
            return "AccessWebStatus sdio info : mSdio_info_readbreq = " + get_sdio_info_readbreq() + ", mSdio_info_readb = " + get_sdio_info_readb() + ", mSdio_info_writebreq = " + get_sdio_info_writebreq() + ", mSdio_info_writeb = " + get_sdio_info_writeb() + ", mSdio_info_readwreq = " + get_sdio_info_readwreq() + ", mSdio_info_readw = " + get_sdio_info_readw() + ", mSdio_info_writewreq = " + get_sdio_info_writewreq() + ", mSdio_info_writew = " + get_sdio_info_writew() + ", mSdio_info_ksosetreq = " + get_sdio_info_ksosetreq() + ", mSdio_info_ksosetretry = " + get_sdio_info_ksosetretry() + ", mSdio_info_ksoclrreq = " + get_sdio_info_ksoclrreq() + ", mSdio_info_ksoclrretry = " + get_sdio_info_ksoclrretry();
        }

        public void reset() {
            this.mRxCnt = 0;
            this.mTxCnt = 0;
            this.mNetDisableCnt = 0;
            this.mDnsFailedCnt = 0;
            this.mRxSleepCnt = 0;
            this.mPeriodTime = 0;
            this.mIpRoute = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
            this.mDisableWlanApps = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
            this.mOldDnsFailedCnt = 0;
            this.mSdio_info_readbreq = 0;
            this.mSdio_info_readb = 0;
            this.mSdio_info_writebreq = 0;
            this.mSdio_info_writeb = 0;
            this.mSdio_info_readwreq = 0;
            this.mSdio_info_readw = 0;
            this.mSdio_info_writewreq = 0;
            this.mSdio_info_writew = 0;
            this.mSdio_info_ksosetreq = 0;
            this.mSdio_info_ksosetretry = 0;
            this.mSdio_info_ksoclrreq = 0;
            this.mSdio_info_ksoclrretry = 0;
            this.mWP = null;
            Log.d("HwArpVerifier", "AccessWebStatus : reset all");
        }
    }

    private class ArpItem {
        private static final int ATF_COM = 2;
        private static final int ATF_PERM = 4;
        public static final int MAX_FAIL_CNT = 10;
        public String device;
        private int failcnt;
        public int flag;
        public String hwaddr;
        public String ipaddr;

        public ArpItem(String ip, String mac, int flag, String ifname) {
            this.failcnt = 0;
            this.ipaddr = ip;
            this.hwaddr = mac.toLowerCase(Locale.ENGLISH);
            this.device = ifname;
            this.flag = flag;
        }

        public ArpItem(String mac, int failcnt) {
            this.failcnt = 0;
            this.ipaddr = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
            this.hwaddr = mac.toLowerCase(Locale.ENGLISH);
            this.device = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
            this.flag = 0;
            this.failcnt = failcnt;
        }

        public boolean matchMaxRetried() {
            return this.failcnt >= 10;
        }

        public void putFail() {
            this.failcnt++;
        }

        public boolean sameIpaddress(String ip) {
            return !TextUtils.isEmpty(ip) ? ip.equals(this.ipaddr) : false;
        }

        public boolean isStaticArp() {
            return (this.flag & 4) == 4;
        }

        public boolean sameMacAddress(String mac) {
            return mac != null ? mac.toLowerCase(Locale.ENGLISH).equals(this.hwaddr) : false;
        }

        public boolean isValid() {
            return (((this.flag & 2) == 2) && HwArpVerifier.IFACE.equals(this.device)) ? this.hwaddr.length() == 17 : false;
        }

        public String toString() {
            return String.format(Locale.ENGLISH, "%s %d %s %s", new Object[]{this.ipaddr, Integer.valueOf(this.flag), this.hwaddr, this.device});
        }
    }

    private enum ArpState {
        DONT_CHECK,
        HEART_CHECK,
        NORMAL_CHECK,
        CONFIRM_CHECK,
        DEAD_CHECK
    }

    protected class ClientHandler extends Handler {
        public static final int DEFAULT_WIFI_ROUTE_CHECK_CNT = 5;
        public static final int DEFAULT_WIFI_ROUTE_CHECK_TIME = 50000;
        public static final int DEFAULT_WIFI_ROUTE_CHECK_TIME_FIRST = 5000;
        public static final int DEFAULT_WIFI_ROUTE_CHECK_TIME_QUICK = 15000;
        private static final int MSG_CHECK_WIFI_STATE = 124;
        private static final int MSG_DO_ARP_ASYNC = 125;
        public static final int MSG_DO_ROUTE_CHECK = 126;
        public static final int MSG_PROBE_WEB_RET = 128;
        public static final int MSG_PROBE_WEB_START = 127;
        private static final int SOCKET_TIMEOUT_MS = 8000;
        private static final int STATIC_IP_DUP = 8;
        private static final int STATIC_IP_OPTIMIZE = 4;
        private static final int STATIC_IP_UNKNOWN = 0;
        private static final int STATIC_IP_UNUSED = 1;
        private static final int STATIC_IP_USER = 2;
        private static final int THRESHOLD_NORMAL_CHECK_FAIL = 5;
        private static final int TIME_CONFIRM_CHECK = 5000;
        private static final int TIME_DEAD_CHECK = 120000;
        private static final int TIME_FIRST_CHECK = 2000;
        private static final int TIME_HEART_CHECK = 30000;
        private static final int TIME_NORMAL_CHECK = 1000;
        private static final int TIME_POLL_AFTER_CONNECT_DELAYED = 2000;
        private static final int TIME_POLL_AFTER_DISCONNECT_DELAYED = 1500;
        private static final int TIME_POLL_TRAFFIC_STATS_INTERVAL = 5000;
        private static final int TRAFFIC_STATS_POLL = 111;
        private static final int TRAFFIC_STATS_POLL_START = 110;
        private static final int TRAFFIC_STATS_POLL_STOP = 112;
        private boolean mArpRunning = false;
        private ArpState mArpState = ArpState.HEART_CHECK;
        private boolean mArpSuccLeastOnce = false;
        private boolean mLinkLayerLogRunning = false;
        private long mNetAccessibleDuration = 0;
        private long mNetUnusableDuration = 0;
        private int mNormalArpFail = 0;
        private long mStartNetAccessibleTime = 0;
        private long mStartNetUnusableTime = 0;
        private int mStaticIpStatus = 0;
        private int mTrafficStatsPollToken = 0;

        private class DetectWebThread extends Thread {
            protected WebParam mWP;

            protected DetectWebThread(WebParam wp) {
                this.mWP = wp;
            }

            public WebParam getWebParam() {
                return this.mWP;
            }

            public void setWebParam(WebParam wp) {
                this.mWP = wp;
            }

            public void run() {
                if (this.mWP != null) {
                    String url = this.mWP.getUrl();
                    if (url != null && !url.isEmpty()) {
                        Log.d("HwArpVerifier", "start accessWeb, web = " + url);
                        int ret = ClientHandler.this.accessWeb(url);
                        HwArpVerifier.this.updatePortalStatus(ret);
                        this.mWP.setRespCode(ret);
                        if (ret != HwArpVerifier.HTTP_ACCESS_TIMEOUT_RESP) {
                            Log.d("HwArpVerifier", "DetectWebThread, sendMessage MSG_PROBE_WEB_RET ,  resp : " + ret);
                            HwArpVerifier.this.mIsFirstCheck = false;
                            ClientHandler.this.sendMessage(ClientHandler.this.obtainMessage(ClientHandler.MSG_PROBE_WEB_RET, ret, 0, HwArpVerifier.this.mAccessWebStatus));
                        } else {
                            Log.d("HwArpVerifier", "DetectWebThread, timeout ,  resp : " + ret);
                        }
                    }
                }
            }
        }

        private class WebCheckThread extends Thread {
            protected String mURL;

            protected WebCheckThread(String url) {
                this.mURL = url;
            }

            public void run() {
                if (ClientHandler.this.accessWeb(this.mURL) != 200) {
                    return;
                }
                if (HwArpVerifier.this.mAccessWebStatus.getChinazIp() != null && !HwArpVerifier.this.mAccessWebStatus.getChinazIp().isEmpty()) {
                    return;
                }
                if ((HwArpVerifier.this.mAccessWebStatus.getChinazAddr() == null || HwArpVerifier.this.mAccessWebStatus.getChinazAddr().isEmpty()) && !HwWifiDFTConnManager.getInstance().isCommercialUser()) {
                    ClientHandler.this.accessWeb(HwArpVerifier.WEB_CHINAZ_GETIP);
                }
            }
        }

        public ClientHandler(Looper looper) {
            super(looper);
        }

        private void createAndSendMsgDelayed(int what, int arg1, int arg2, long delayMillis) {
            sendMessageDelayed(Message.obtain(this, what, arg1, arg2), delayMillis);
            Log.d("HwArpVerifier", "msg what=" + what + " arg1(token)=" + arg1 + " arg2(mode)=" + arg2 + " delay=" + delayMillis);
        }

        public void monitorWifiNetworkState() {
            if (HwArpVerifier.this.isConnectedToWifi() && HwArpVerifier.this.mCurrentWiFiState == 1) {
                Log.d("HwArpVerifier", "dont handle monitorWifiNetworkState started becauseof running");
                return;
            }
            HwArpVerifier hwArpVerifier = HwArpVerifier.this;
            hwArpVerifier.mCheckStateToken = hwArpVerifier.mCheckStateToken + 1;
            HwArpVerifier.this.mArpBlacklist.clear();
            HwArpVerifier.this.mFirstDetect = true;
            this.mTrafficStatsPollToken++;
            this.mArpSuccLeastOnce = false;
            if (HwArpVerifier.this.isConnectedToWifi()) {
                HwArpVerifier.this.mCurrentWiFiState = 1;
                if (HwArpVerifier.this.isEnableChecker()) {
                    Log.d("HwArpVerifier", "monitorWifiNetworkState: started.");
                    this.mStaticIpStatus = 0;
                    sendMessageDelayed(Message.obtain(this, 110, this.mTrafficStatsPollToken, 0), 2000);
                    HwArpVerifier.this.updateDurationControlParamsIfNeed();
                    transmitState(ArpState.HEART_CHECK);
                    createAndSendMsgDelayed(124, HwArpVerifier.this.mCheckStateToken, 0, 2000);
                }
                HwArpVerifier.this.startWifiRouteCheck();
                HwArpVerifier.this.handleWiFiDnsStats(0);
            } else {
                Log.d("HwArpVerifier", "monitorWifiNetworkState: stopped.");
                HwArpVerifier.this.mCurrentWiFiState = -1;
                this.mStaticIpStatus = 0;
                transmitState(ArpState.DONT_CHECK);
                sendMessageDelayed(Message.obtain(this, 112, this.mTrafficStatsPollToken, 0), 1500);
                HwArpVerifier.this.stopWifiRouteCheck();
                HwArpVerifier.this.handleWiFiDnsStats(HwArpVerifier.this.mLastNetworkId);
            }
        }

        private void doCheckWebSpeed() {
            if (HwArpVerifier.this.webMonitor.getAppSuckTime() == 2 && HwArpVerifier.this.mHWGatewayVerifier.isEnableDectction()) {
                new WebCheckThread(HwArpVerifier.WEB_BAIDU).start();
                HwArpVerifier.this.mHWGatewayVerifier.getGateWayARPResponses();
                readSdioQuality(HwArpVerifier.this.mAccessWebStatus);
            }
        }

        private int getDNSFailedCntDiff(AccessWebStatus aws) {
            int dnsNewFailCnt = 0;
            String strDNSFailedCnt = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
            try {
                dnsNewFailCnt = Integer.parseInt(SystemProperties.get(HwSelfCureUtils.DNS_MONITOR_FLAG, "0"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            int dnsDiff = dnsNewFailCnt - aws.getOldDnsFailedCnt();
            Log.d("HwArpVerifier", "getDNSFailedCntDiff  dnsNewFailCnt =  " + dnsNewFailCnt + ", aws.getOldDnsFailedCnt() = " + aws.getOldDnsFailedCnt() + ", dnsDiff = " + dnsDiff);
            aws.setOldDnsFailedCnt(dnsNewFailCnt);
            aws.setDNSFailed(dnsDiff);
            return dnsDiff;
        }

        private void doCheckAccessInternet() {
            Log.d("HwArpVerifier", "doCheckAccessInternet");
            if (HwArpVerifier.this.isConnectedToWifi() && HwArpVerifier.this.mCurrentWiFiState == 1 && (HwArpVerifier.this.mHWGatewayVerifier.isEnableDectction() ^ 1) == 0) {
                HwArpVerifier hwArpVerifier = HwArpVerifier.this;
                hwArpVerifier.mlDetectWebCounter = hwArpVerifier.mlDetectWebCounter + 1;
                WebParam wp = HwArpVerifier.this.mAccessWebStatus.getWebParam();
                if (wp != null) {
                    int respCode = wp.getRespCode();
                    if (HwArpVerifier.HTTP_ACCESS_TIMEOUT_RESP == respCode) {
                        long diff = SystemClock.elapsedRealtime() - wp.getStartTime();
                        if (diff > HwArpVerifier.ACCESS_BAIDU_TIMEOUT) {
                            Log.d("HwArpVerifier", "trigger CHR exception report because access baidu timeout : " + (diff / 1000) + "s");
                            HwArpVerifier.this.mAccessWebStatus.setWebParam(null);
                            sendMessage(obtainMessage(MSG_PROBE_WEB_RET, respCode, 0, HwArpVerifier.this.mAccessWebStatus));
                        } else {
                            Log.d("HwArpVerifier", "doCheckAccessInternet return because wait access baidu wait is : " + (diff / 1000) + "s");
                        }
                        return;
                    }
                }
                HwArpVerifier.this.mAccessWebStatus.setWebParam(null);
                HwArpVerifier.this.webMonitor.checkAccessWebStatus(HwArpVerifier.this.mAccessWebStatus);
                long lastTime = HwArpVerifier.this.mAccessWebStatus.getPeriodTime();
                if (0 != lastTime && SystemClock.elapsedRealtime() - lastTime > HwArpVerifier.SLEEP_PERIOD_TIMEOUT) {
                    HwArpVerifier.this.mAccessWebStatus.setRxSleepCnt(HwArpVerifier.this.mAccessWebStatus.getRxCnt() > 0 ? 1 : 0);
                    Log.d("HwArpVerifier", "mAccessWebStatus : mRxSleepCnt = " + HwArpVerifier.this.mAccessWebStatus.getRxSleepCnt());
                }
                HwArpVerifier.this.mAccessWebStatus.setPeriodTime(SystemClock.elapsedRealtime());
                Log.d("HwArpVerifier", "mAccessWebStatus : rx = " + HwArpVerifier.this.mAccessWebStatus.getRxCnt() + ", tx = " + HwArpVerifier.this.mAccessWebStatus.getTxCnt());
                if (HwArpVerifier.this.mAccessWebStatus.getRxCnt() > 0) {
                    HwArpVerifier.this.mAccessWebStatus.reset();
                    HwArpVerifier.this.mAccessWebStatus.setDetectWebStatus(2);
                    getNetStatusHoldTimes();
                    return;
                }
                if (HwArpVerifier.this.mAccessWebStatus.getTxCnt() >= 2) {
                    HwArpVerifier.this.mAccessWebStatus.setNetDisableCnt(HwArpVerifier.this.mAccessWebStatus.getNetDisableCnt() + 1);
                } else if (getDNSFailedCntDiff(HwArpVerifier.this.mAccessWebStatus) > 0) {
                    HwArpVerifier.this.mAccessWebStatus.setNetDisableCnt(HwArpVerifier.this.mAccessWebStatus.getNetDisableCnt() + 1);
                } else {
                    getNetStatusHoldTimes();
                    return;
                }
                Log.d("HwArpVerifier", "mAccessWebStatus : getNetDisableCnt = " + HwArpVerifier.this.mAccessWebStatus.getNetDisableCnt() + ", rx = " + HwArpVerifier.this.mAccessWebStatus.getRxCnt() + ", tx = " + HwArpVerifier.this.mAccessWebStatus.getTxCnt());
                if (HwArpVerifier.this.mAccessWebStatus.getNetDisableCnt() == 1) {
                    HwArpVerifier.this.webMonitor.getPktcnt().fetchPktcntNative();
                    HwCHRWifiLinkMonitor.getDefault().runCounters();
                } else if (HwArpVerifier.this.mAccessWebStatus.getNetDisableCnt() > 1) {
                    WebParam webParam = new WebParam(HwArpVerifier.WEB_BAIDU, SystemClock.elapsedRealtime(), HwArpVerifier.HTTP_ACCESS_TIMEOUT_RESP);
                    DetectWebThread dwt = new DetectWebThread(webParam);
                    HwArpVerifier.this.mAccessWebStatus.setWebParam(webParam);
                    Log.d("HwArpVerifier", "mAccessWebStatus : reset mNetDisableCnt = 0 , and start thread to access baidu = http://www.baidu.com");
                    HwArpVerifier.this.mAccessWebStatus.setNetDisableCnt(0);
                    dwt.start();
                }
                return;
            }
            Log.d("HwArpVerifier", "doCheckAccessInternet return because not wifi connected");
        }

        private void getNetStatusHoldTimes() {
            if (0 == this.mStartNetAccessibleTime) {
                this.mStartNetAccessibleTime = SystemClock.elapsedRealtime();
            }
            if (0 != this.mStartNetUnusableTime) {
                this.mNetUnusableDuration = SystemClock.elapsedRealtime() - this.mStartNetUnusableTime;
                this.mStartNetUnusableTime = 0;
            }
        }

        private void setConnectionProperty(HttpURLConnection conn) {
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(SOCKET_TIMEOUT_MS);
            conn.setReadTimeout(SOCKET_TIMEOUT_MS);
            conn.setUseCaches(false);
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("contentType", "utf-8");
        }

        protected int accessWeb(String dlUrl) {
            HttpURLConnection urlConn = null;
            InputStream inputStream = null;
            int respCode = HwArpVerifier.HTTP_ACCESS_TIMEOUT_RESP;
            try {
                URLConnection conn = new URL(dlUrl).openConnection();
                if (!(conn instanceof HttpURLConnection)) {
                    return HwArpVerifier.HTTP_ACCESS_TIMEOUT_RESP;
                }
                long lStart = SystemClock.elapsedRealtime();
                urlConn = (HttpURLConnection) conn;
                setConnectionProperty(urlConn);
                inputStream = urlConn.getInputStream();
                respCode = urlConn.getResponseCode();
                Log.d("HwArpVerifier", "accessWeb, respCode = " + respCode + ", url=" + dlUrl);
                if (dlUrl.equals(HwArpVerifier.WEB_CHINAZ_GETIP) && respCode == 200) {
                    String strBody = readWebBody(inputStream);
                    JSONObject jSONObject = null;
                    String strIP = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
                    String strAddr = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
                    try {
                        jSONObject = new JSONObject(strBody);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (jSONObject != null) {
                        try {
                            strIP = jSONObject.getString("ip");
                            strAddr = jSONObject.getString("address");
                        } catch (JSONException e2) {
                            e2.printStackTrace();
                        }
                    }
                    HwArpVerifier.this.mAccessWebStatus.setChinazIp(strIP);
                    HwArpVerifier.this.mAccessWebStatus.setChinazAddr(strAddr);
                    Log.d("HwArpVerifier", "accessWeb, ip = " + strIP + ", addr = " + strAddr);
                } else if (dlUrl.equals(HwArpVerifier.WEB_BAIDU)) {
                    HwArpVerifier.this.mAccessWebStatus.setRTTBaidu((int) (SystemClock.elapsedRealtime() - lStart));
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e3) {
                        Log.d("HwArpVerifier", "exception of close, msg = " + e3.getMessage());
                    }
                }
                if (urlConn != null) {
                    urlConn.disconnect();
                }
                return respCode;
            } catch (IOException e32) {
                Log.d("HwArpVerifier", "IOException, msg = " + e32.getMessage());
                String msg = e32.getMessage();
                Log.d("HwArpVerifier", "accessWeb, IOException, msg = " + msg);
                if (dlUrl != null && dlUrl.equals(HwArpVerifier.WEB_BAIDU) && msg != null && (msg.contains("ECONNREFUSED") || msg.contains("ECONNRESET"))) {
                    respCode = 200;
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e322) {
                        Log.d("HwArpVerifier", "exception of close, msg = " + e322.getMessage());
                    }
                }
                if (urlConn != null) {
                    urlConn.disconnect();
                }
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e3222) {
                        Log.d("HwArpVerifier", "exception of close, msg = " + e3222.getMessage());
                    }
                }
                if (urlConn != null) {
                    urlConn.disconnect();
                }
            }
        }

        private String readWebBody(InputStream ins) {
            String strBody = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
            int readBytes = 0;
            int totalBytes = 0;
            byte[] buffer = new byte[512];
            if (ins == null) {
                return strBody;
            }
            while (totalBytes < 512) {
                Arrays.fill(buffer, (byte) 0);
                try {
                    readBytes = ins.read(buffer, 0, buffer.length);
                } catch (IOException e) {
                    Log.d("HwArpVerifier", "route_cmd instream, IOException");
                    e.printStackTrace();
                }
                if (readBytes <= 0) {
                    break;
                }
                totalBytes += readBytes;
                if (totalBytes < 512) {
                    strBody = strBody + new String(buffer, Charset.defaultCharset()).trim();
                }
                Log.d("HwArpVerifier", "route_cmd:" + strBody);
            }
            return strBody;
        }

        /* JADX WARNING: Removed duplicated region for block: B:51:0x00cd  */
        /* JADX WARNING: Removed duplicated region for block: B:45:0x00bb  */
        /* JADX WARNING: Removed duplicated region for block: B:36:0x009e  */
        /* JADX WARNING: Removed duplicated region for block: B:55:0x00d6  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private String getIpRouteTable() {
            ErrnoException e;
            InterruptedIOException e2;
            SocketException e3;
            Throwable th;
            String msgSnippet = "getIpRouteTable";
            int errno = -OsConstants.EPROTO;
            String route = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
            Log.d("HwArpVerifier", "getIpRouteTable");
            byte[] msg = RtNetlinkMessage.newNewGetRouteMessage();
            NetlinkSocket s;
            try {
                s = new NetlinkSocket(OsConstants.NETLINK_ROUTE);
                try {
                    s.connectToKernel();
                    if (s.getLocalAddress() == null) {
                        s.close();
                        return null;
                    }
                    s.sendMessage(msg, 0, msg.length, 300);
                    int doneMessageCount = 0;
                    while (doneMessageCount == 0) {
                        ByteBuffer response = s.recvMessage(500);
                        if (response != null) {
                            while (response.remaining() > 0) {
                                NetlinkMessage resmsg = NetlinkMessage.parse(response);
                                if (resmsg != null) {
                                    StructNlMsgHdr hdr = resmsg.getHeader();
                                    if (hdr == null) {
                                        s.close();
                                        return null;
                                    } else if (hdr.nlmsg_type == (short) 3) {
                                        doneMessageCount++;
                                    } else if (hdr.nlmsg_type == (short) 24 || hdr.nlmsg_type == (short) 26) {
                                        route = route + resmsg.toString();
                                    }
                                }
                            }
                            continue;
                        }
                    }
                    s.close();
                    return route;
                } catch (ErrnoException e4) {
                    e = e4;
                    Log.e("HwArpVerifier", "Error getIpRouteTable", e);
                    if (s != null) {
                        s.close();
                    }
                    Log.e("HwArpVerifier", "fail getIpRouteTable");
                    return null;
                } catch (InterruptedIOException e5) {
                    e2 = e5;
                    Log.e("HwArpVerifier", "Error getIpRouteTable", e2);
                    if (s != null) {
                        s.close();
                    }
                    Log.e("HwArpVerifier", "fail getIpRouteTable");
                    return null;
                } catch (SocketException e6) {
                    e3 = e6;
                    try {
                        Log.e("HwArpVerifier", "Error getIpRouteTable", e3);
                        if (s != null) {
                            s.close();
                        }
                        Log.e("HwArpVerifier", "fail getIpRouteTable");
                        return null;
                    } catch (Throwable th2) {
                        th = th2;
                        if (s != null) {
                        }
                        throw th;
                    }
                }
            } catch (ErrnoException e7) {
                e = e7;
                s = null;
                Log.e("HwArpVerifier", "Error getIpRouteTable", e);
                if (s != null) {
                }
                Log.e("HwArpVerifier", "fail getIpRouteTable");
                return null;
            } catch (InterruptedIOException e8) {
                e2 = e8;
                s = null;
                Log.e("HwArpVerifier", "Error getIpRouteTable", e2);
                if (s != null) {
                }
                Log.e("HwArpVerifier", "fail getIpRouteTable");
                return null;
            } catch (SocketException e9) {
                e3 = e9;
                s = null;
                Log.e("HwArpVerifier", "Error getIpRouteTable", e3);
                if (s != null) {
                }
                Log.e("HwArpVerifier", "fail getIpRouteTable");
                return null;
            } catch (Throwable th3) {
                th = th3;
                s = null;
                if (s != null) {
                    s.close();
                }
                throw th;
            }
        }

        private boolean isBackgroundRunning(String processName) {
            ActivityManager activityManager = (ActivityManager) HwArpVerifier.this.mContext.getSystemService("activity");
            KeyguardManager keyguardManager = (KeyguardManager) HwArpVerifier.this.mContext.getSystemService("keyguard");
            if (activityManager == null) {
                return false;
            }
            List<RunningAppProcessInfo> processList = activityManager.getRunningAppProcesses();
            if (processList == null) {
                return false;
            }
            for (RunningAppProcessInfo process : processList) {
                if (process.processName.startsWith(processName)) {
                    boolean isBackground = process.importance != 100 ? process.importance != 200 : false;
                    boolean isLockedState = keyguardManager.inKeyguardRestrictedInputMode();
                    if (isBackground || isLockedState) {
                        return true;
                    }
                    return false;
                }
            }
            return false;
        }

        private String getAllDisableWlanAppsFromDB() {
            String strRet = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
            Cursor cur = HwArpVerifier.this.mContext.getContentResolver().query(HwArpVerifier.URI_NETAPP, null, null, null, null);
            if (isNullOrEmptyCursor(cur, true)) {
                return null;
            }
            int INDEX_PKGNAME = cur.getColumnIndex(HwArpVerifier.APP_PACKAGE_NAME);
            int INDEX_UID = cur.getColumnIndex(HwArpVerifier.APP_UID);
            int INDEX_PERMISSION = cur.getColumnIndex(HwArpVerifier.PERMISSION_CFG);
            int iCount = 0;
            while (cur.moveToNext()) {
                String pkg = cur.getString(INDEX_PKGNAME);
                int uid = cur.getInt(INDEX_UID);
                int permission = cur.getInt(INDEX_PERMISSION);
                if ((HwArpVerifier.WIFI_DISABLE1 == permission || HwArpVerifier.WIFI_DISABLE2 == permission) && pkg != null && !pkg.isEmpty() && isBackgroundRunning(pkg)) {
                    Log.i("HwArpVerifier", "getAllDisableWlanAppsFromDB added pkg = " + pkg + " uid = " + uid + " permission = " + permission);
                    String pkgTmp = pkg;
                    if (pkg.length() > 24) {
                        pkgTmp = pkg.substring(pkg.length() - 24, pkg.length());
                    }
                    if (strRet.isEmpty()) {
                        strRet = pkgTmp;
                    } else {
                        strRet = strRet + pkgTmp;
                    }
                    strRet = strRet + ";";
                    if (strRet.length() > 124) {
                        strRet = strRet.substring(0, 124);
                        break;
                    }
                    iCount++;
                    if (iCount < 5) {
                    }
                }
            }
            try {
                cur.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return strRet;
        }

        public boolean isNullOrEmptyCursor(Cursor cursor, boolean isCloseIfEmpty) {
            if (cursor == null) {
                return true;
            }
            if (cursor.getCount() > 0) {
                return false;
            }
            if (isCloseIfEmpty) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        /* JADX WARNING: Removed duplicated region for block: B:25:0x0092 A:{SYNTHETIC, Splitter: B:25:0x0092} */
        /* JADX WARNING: Removed duplicated region for block: B:28:0x0097 A:{Catch:{ Exception -> 0x0296 }} */
        /* JADX WARNING: Removed duplicated region for block: B:41:0x0198 A:{SYNTHETIC, Splitter: B:41:0x0198} */
        /* JADX WARNING: Removed duplicated region for block: B:44:0x019d A:{Catch:{ Exception -> 0x01a2 }} */
        /* JADX WARNING: Removed duplicated region for block: B:58:0x01f7 A:{SYNTHETIC, Splitter: B:58:0x01f7} */
        /* JADX WARNING: Removed duplicated region for block: B:61:0x01fc A:{Catch:{ Exception -> 0x02b3 }} */
        /* JADX WARNING: Removed duplicated region for block: B:58:0x01f7 A:{SYNTHETIC, Splitter: B:58:0x01f7} */
        /* JADX WARNING: Removed duplicated region for block: B:61:0x01fc A:{Catch:{ Exception -> 0x02b3 }} */
        /* JADX WARNING: Removed duplicated region for block: B:25:0x0092 A:{SYNTHETIC, Splitter: B:25:0x0092} */
        /* JADX WARNING: Removed duplicated region for block: B:28:0x0097 A:{Catch:{ Exception -> 0x0296 }} */
        /* JADX WARNING: Removed duplicated region for block: B:41:0x0198 A:{SYNTHETIC, Splitter: B:41:0x0198} */
        /* JADX WARNING: Removed duplicated region for block: B:44:0x019d A:{Catch:{ Exception -> 0x01a2 }} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void readSdioQuality(AccessWebStatus aws) {
            FileNotFoundException e;
            Throwable th;
            IOException e2;
            FileInputStream f = null;
            BufferedReader dr = null;
            try {
                BufferedReader dr2;
                FileInputStream f2 = new FileInputStream(HwArpVerifier.SDIO_DEBUG_FILENAME);
                try {
                    dr2 = new BufferedReader(new InputStreamReader(f2, Charset.defaultCharset()));
                } catch (FileNotFoundException e3) {
                    e = e3;
                    f = f2;
                    try {
                        Log.d("HwArpVerifier", "readSdioQuality exception 1" + e);
                        if (dr != null) {
                            try {
                                dr.close();
                            } catch (Exception e4) {
                                Log.d("HwArpVerifier", "readSdioQuality exception 3" + e4);
                            }
                        }
                        if (f != null) {
                            f.close();
                        }
                        Log.d("HwArpVerifier", "readSdioQuality : mSdio_info_readbreq = " + aws.get_sdio_info_readbreq() + ", mSdio_info_readb = " + aws.get_sdio_info_readb() + ", mSdio_info_writebreq = " + aws.get_sdio_info_writebreq() + ", mSdio_info_writeb = " + aws.get_sdio_info_writeb() + ", mSdio_info_readwreq = " + aws.get_sdio_info_readwreq() + ", mSdio_info_readw = " + aws.get_sdio_info_readw() + ", mSdio_info_writewreq = " + aws.get_sdio_info_writewreq() + ", mSdio_info_writew = " + aws.get_sdio_info_writew() + ", mSdio_info_ksosetreq = " + aws.get_sdio_info_ksosetreq() + ", mSdio_info_ksosetretry = " + aws.get_sdio_info_ksosetretry() + ", mSdio_info_ksoclrreq = " + aws.get_sdio_info_ksoclrreq() + ", mSdio_info_ksoclrretry = " + aws.get_sdio_info_ksoclrretry());
                    } catch (Throwable th2) {
                        th = th2;
                        if (dr != null) {
                        }
                        if (f != null) {
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    e2 = e5;
                    f = f2;
                    Log.d("HwArpVerifier", "readSdioQuality exception 2" + e2);
                    e2.printStackTrace();
                    if (dr != null) {
                        try {
                            dr.close();
                        } catch (Exception e42) {
                            Log.d("HwArpVerifier", "readSdioQuality exception 3" + e42);
                        }
                    }
                    if (f != null) {
                        f.close();
                    }
                    Log.d("HwArpVerifier", "readSdioQuality : mSdio_info_readbreq = " + aws.get_sdio_info_readbreq() + ", mSdio_info_readb = " + aws.get_sdio_info_readb() + ", mSdio_info_writebreq = " + aws.get_sdio_info_writebreq() + ", mSdio_info_writeb = " + aws.get_sdio_info_writeb() + ", mSdio_info_readwreq = " + aws.get_sdio_info_readwreq() + ", mSdio_info_readw = " + aws.get_sdio_info_readw() + ", mSdio_info_writewreq = " + aws.get_sdio_info_writewreq() + ", mSdio_info_writew = " + aws.get_sdio_info_writew() + ", mSdio_info_ksosetreq = " + aws.get_sdio_info_ksosetreq() + ", mSdio_info_ksosetretry = " + aws.get_sdio_info_ksosetretry() + ", mSdio_info_ksoclrreq = " + aws.get_sdio_info_ksoclrreq() + ", mSdio_info_ksoclrretry = " + aws.get_sdio_info_ksoclrretry());
                } catch (Throwable th3) {
                    th = th3;
                    f = f2;
                    if (dr != null) {
                        try {
                            dr.close();
                        } catch (Exception e422) {
                            Log.d("HwArpVerifier", "readSdioQuality exception 3" + e422);
                            throw th;
                        }
                    }
                    if (f != null) {
                        f.close();
                    }
                    throw th;
                }
                try {
                    for (String line = dr2.readLine(); line != null; line = dr2.readLine()) {
                        line = line.trim();
                        if (!line.equals(HwArpVerifier.BCM_ROAMING_FLAG_FILE)) {
                            String[] arr = line.split("[ \t]+");
                            if (arr.length > 0) {
                                String tmp = arr[0];
                                int v1;
                                int v2;
                                if (tmp.equals(HwArpVerifier.CMD52)) {
                                    if (arr.length >= 4) {
                                        v1 = Integer.parseInt(arr[2]);
                                        v2 = Integer.parseInt(arr[3]);
                                        if (arr[1].equals(HwArpVerifier.CMD_READ)) {
                                            aws.set_sdio_info_readbreq(v1);
                                            aws.set_sdio_info_readb(v2);
                                        } else if (arr[1].equals(HwArpVerifier.CMD_WRITE)) {
                                            aws.set_sdio_info_writebreq(v1);
                                            aws.set_sdio_info_writeb(v2);
                                        }
                                    }
                                } else if (tmp.equals(HwArpVerifier.CMD53)) {
                                    if (arr.length >= 4) {
                                        v1 = Integer.parseInt(arr[2]);
                                        v2 = Integer.parseInt(arr[3]);
                                        if (arr[1].equals(HwArpVerifier.CMD_READ)) {
                                            aws.set_sdio_info_readwreq(v1);
                                            aws.set_sdio_info_readw(v2);
                                        } else if (arr[1].equals(HwArpVerifier.CMD_WRITE)) {
                                            aws.set_sdio_info_writewreq(v1);
                                            aws.set_sdio_info_writew(v2);
                                        }
                                    }
                                } else if (tmp.equals(HwArpVerifier.CMD_SLEEPCSR_SET)) {
                                    if (arr.length >= 4) {
                                        v1 = Integer.parseInt(arr[2]);
                                        v2 = Integer.parseInt(arr[3]);
                                        aws.set_sdio_info_ksosetreq(v1);
                                        aws.set_sdio_info_ksosetretry(v2);
                                    }
                                } else if (tmp.equals(HwArpVerifier.CMD_SLEEPCSR_CLR) && arr.length >= 4) {
                                    v1 = Integer.parseInt(arr[2]);
                                    v2 = Integer.parseInt(arr[3]);
                                    aws.set_sdio_info_ksoclrreq(v1);
                                    aws.set_sdio_info_ksoclrretry(v2);
                                }
                            }
                        }
                    }
                    dr2.close();
                    f2.close();
                    if (dr2 != null) {
                        try {
                            dr2.close();
                        } catch (Exception e4222) {
                            Log.d("HwArpVerifier", "readSdioQuality exception 3" + e4222);
                        }
                    }
                    if (f2 != null) {
                        f2.close();
                    }
                } catch (FileNotFoundException e6) {
                    e = e6;
                    dr = dr2;
                    f = f2;
                    Log.d("HwArpVerifier", "readSdioQuality exception 1" + e);
                    if (dr != null) {
                    }
                    if (f != null) {
                    }
                    Log.d("HwArpVerifier", "readSdioQuality : mSdio_info_readbreq = " + aws.get_sdio_info_readbreq() + ", mSdio_info_readb = " + aws.get_sdio_info_readb() + ", mSdio_info_writebreq = " + aws.get_sdio_info_writebreq() + ", mSdio_info_writeb = " + aws.get_sdio_info_writeb() + ", mSdio_info_readwreq = " + aws.get_sdio_info_readwreq() + ", mSdio_info_readw = " + aws.get_sdio_info_readw() + ", mSdio_info_writewreq = " + aws.get_sdio_info_writewreq() + ", mSdio_info_writew = " + aws.get_sdio_info_writew() + ", mSdio_info_ksosetreq = " + aws.get_sdio_info_ksosetreq() + ", mSdio_info_ksosetretry = " + aws.get_sdio_info_ksosetretry() + ", mSdio_info_ksoclrreq = " + aws.get_sdio_info_ksoclrreq() + ", mSdio_info_ksoclrretry = " + aws.get_sdio_info_ksoclrretry());
                } catch (IOException e7) {
                    e2 = e7;
                    dr = dr2;
                    f = f2;
                    Log.d("HwArpVerifier", "readSdioQuality exception 2" + e2);
                    e2.printStackTrace();
                    if (dr != null) {
                    }
                    if (f != null) {
                    }
                    Log.d("HwArpVerifier", "readSdioQuality : mSdio_info_readbreq = " + aws.get_sdio_info_readbreq() + ", mSdio_info_readb = " + aws.get_sdio_info_readb() + ", mSdio_info_writebreq = " + aws.get_sdio_info_writebreq() + ", mSdio_info_writeb = " + aws.get_sdio_info_writeb() + ", mSdio_info_readwreq = " + aws.get_sdio_info_readwreq() + ", mSdio_info_readw = " + aws.get_sdio_info_readw() + ", mSdio_info_writewreq = " + aws.get_sdio_info_writewreq() + ", mSdio_info_writew = " + aws.get_sdio_info_writew() + ", mSdio_info_ksosetreq = " + aws.get_sdio_info_ksosetreq() + ", mSdio_info_ksosetretry = " + aws.get_sdio_info_ksosetretry() + ", mSdio_info_ksoclrreq = " + aws.get_sdio_info_ksoclrreq() + ", mSdio_info_ksoclrretry = " + aws.get_sdio_info_ksoclrretry());
                } catch (Throwable th4) {
                    th = th4;
                    dr = dr2;
                    f = f2;
                    if (dr != null) {
                    }
                    if (f != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e8) {
                e = e8;
                Log.d("HwArpVerifier", "readSdioQuality exception 1" + e);
                if (dr != null) {
                }
                if (f != null) {
                }
                Log.d("HwArpVerifier", "readSdioQuality : mSdio_info_readbreq = " + aws.get_sdio_info_readbreq() + ", mSdio_info_readb = " + aws.get_sdio_info_readb() + ", mSdio_info_writebreq = " + aws.get_sdio_info_writebreq() + ", mSdio_info_writeb = " + aws.get_sdio_info_writeb() + ", mSdio_info_readwreq = " + aws.get_sdio_info_readwreq() + ", mSdio_info_readw = " + aws.get_sdio_info_readw() + ", mSdio_info_writewreq = " + aws.get_sdio_info_writewreq() + ", mSdio_info_writew = " + aws.get_sdio_info_writew() + ", mSdio_info_ksosetreq = " + aws.get_sdio_info_ksosetreq() + ", mSdio_info_ksosetretry = " + aws.get_sdio_info_ksosetretry() + ", mSdio_info_ksoclrreq = " + aws.get_sdio_info_ksoclrreq() + ", mSdio_info_ksoclrretry = " + aws.get_sdio_info_ksoclrretry());
            } catch (IOException e9) {
                e2 = e9;
                Log.d("HwArpVerifier", "readSdioQuality exception 2" + e2);
                e2.printStackTrace();
                if (dr != null) {
                }
                if (f != null) {
                }
                Log.d("HwArpVerifier", "readSdioQuality : mSdio_info_readbreq = " + aws.get_sdio_info_readbreq() + ", mSdio_info_readb = " + aws.get_sdio_info_readb() + ", mSdio_info_writebreq = " + aws.get_sdio_info_writebreq() + ", mSdio_info_writeb = " + aws.get_sdio_info_writeb() + ", mSdio_info_readwreq = " + aws.get_sdio_info_readwreq() + ", mSdio_info_readw = " + aws.get_sdio_info_readw() + ", mSdio_info_writewreq = " + aws.get_sdio_info_writewreq() + ", mSdio_info_writew = " + aws.get_sdio_info_writew() + ", mSdio_info_ksosetreq = " + aws.get_sdio_info_ksosetreq() + ", mSdio_info_ksosetretry = " + aws.get_sdio_info_ksosetretry() + ", mSdio_info_ksoclrreq = " + aws.get_sdio_info_ksoclrreq() + ", mSdio_info_ksoclrretry = " + aws.get_sdio_info_ksoclrretry());
            }
            Log.d("HwArpVerifier", "readSdioQuality : mSdio_info_readbreq = " + aws.get_sdio_info_readbreq() + ", mSdio_info_readb = " + aws.get_sdio_info_readb() + ", mSdio_info_writebreq = " + aws.get_sdio_info_writebreq() + ", mSdio_info_writeb = " + aws.get_sdio_info_writeb() + ", mSdio_info_readwreq = " + aws.get_sdio_info_readwreq() + ", mSdio_info_readw = " + aws.get_sdio_info_readw() + ", mSdio_info_writewreq = " + aws.get_sdio_info_writewreq() + ", mSdio_info_writew = " + aws.get_sdio_info_writew() + ", mSdio_info_ksosetreq = " + aws.get_sdio_info_ksosetreq() + ", mSdio_info_ksosetretry = " + aws.get_sdio_info_ksosetretry() + ", mSdio_info_ksoclrreq = " + aws.get_sdio_info_ksoclrreq() + ", mSdio_info_ksoclrretry = " + aws.get_sdio_info_ksoclrretry());
        }

        /* JADX WARNING: Removed duplicated region for block: B:58:0x02af A:{SYNTHETIC, Splitter: B:58:0x02af} */
        /* JADX WARNING: Removed duplicated region for block: B:61:0x02b4 A:{Catch:{ Exception -> 0x03e5 }} */
        /* JADX WARNING: Removed duplicated region for block: B:30:0x00b0 A:{SYNTHETIC, Splitter: B:30:0x00b0} */
        /* JADX WARNING: Removed duplicated region for block: B:33:0x00b5 A:{Catch:{ Exception -> 0x03c8 }} */
        /* JADX WARNING: Removed duplicated region for block: B:45:0x0263 A:{SYNTHETIC, Splitter: B:45:0x0263} */
        /* JADX WARNING: Removed duplicated region for block: B:48:0x0268 A:{Catch:{ Exception -> 0x026d }} */
        /* JADX WARNING: Removed duplicated region for block: B:58:0x02af A:{SYNTHETIC, Splitter: B:58:0x02af} */
        /* JADX WARNING: Removed duplicated region for block: B:61:0x02b4 A:{Catch:{ Exception -> 0x03e5 }} */
        /* JADX WARNING: Removed duplicated region for block: B:30:0x00b0 A:{SYNTHETIC, Splitter: B:30:0x00b0} */
        /* JADX WARNING: Removed duplicated region for block: B:33:0x00b5 A:{Catch:{ Exception -> 0x03c8 }} */
        /* JADX WARNING: Removed duplicated region for block: B:45:0x0263 A:{SYNTHETIC, Splitter: B:45:0x0263} */
        /* JADX WARNING: Removed duplicated region for block: B:48:0x0268 A:{Catch:{ Exception -> 0x026d }} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void readHisiChipsetDebugInfo(AccessWebStatus aws) {
            FileNotFoundException e;
            Throwable th;
            IOException e2;
            FileInputStream f = null;
            BufferedReader dr = null;
            String chipType = SystemProperties.get("ro.connectivity.chiptype", HwArpVerifier.BCM_ROAMING_FLAG_FILE);
            if (chipType == null || (chipType.isEmpty() ^ 1) == 0 || chipType.equalsIgnoreCase("hi110x") || (chipType.equalsIgnoreCase("hisi") ^ 1) == 0) {
                aws.setMonitor_interval(5000);
                try {
                    BufferedReader dr2;
                    FileInputStream f2 = new FileInputStream(HwArpVerifier.HISI_CHIPSET_DEBUG_FILE);
                    try {
                        dr2 = new BufferedReader(new InputStreamReader(f2, Charset.defaultCharset()));
                    } catch (FileNotFoundException e3) {
                        e = e3;
                        f = f2;
                        try {
                            Log.d("HwArpVerifier", "readHisiChipsetDebugInfo exception 1" + e);
                            if (dr != null) {
                            }
                            if (f != null) {
                            }
                            Log.d("HwArpVerifier", "readHisiChipsetDebugInfo prev, mMonitor_interval = " + aws.getMonitor_interval() + ", mPrevTx_frame_amount = " + aws.getPrevTx_frame_amount() + ", mPrevTx_byte_amount = " + aws.getPrevTx_byte_amount() + ", mPrevTx_data_frame_error_amount = " + aws.getPrevTx_data_frame_error_amount() + ", mPrevTx_retrans_amount = " + aws.getPrevTx_retrans_amount() + ", mPrevRx_frame_amount = " + aws.getPrevRx_frame_amount() + ", mPrevRx_byte_amount = " + aws.getPrevRx_byte_amount() + ", mPrevRx_beacon_from_assoc_ap = " + aws.getPrevRx_beacon_from_assoc_ap() + ", mPrevAp_distance = " + aws.getPrevAp_distance() + ", mPrevDisturbing_degree = " + aws.getPrevDisturbing_degree() + ", mPrevLost_beacon_amount = " + aws.getPrevLost_beacon_amount());
                            Log.d("HwArpVerifier", "readHisiChipsetDebugInfo : , mMonitor_interval = " + aws.getMonitor_interval() + " , mTx_frame_amount = " + aws.getTx_frame_amount() + " , mTx_byte_amount = " + aws.getTx_byte_amount() + " , mTx_data_frame_error_amount = " + aws.getTx_data_frame_error_amount() + " , mTx_retrans_amount = " + aws.getTx_retrans_amount() + " , mRx_frame_amount = " + aws.getRx_frame_amount() + " , mRx_byte_amount =" + aws.getRx_byte_amount() + " , mRx_beacon_from_assoc_ap = " + aws.getRx_beacon_from_assoc_ap() + " , mAp_distance = " + aws.getAp_distance() + " , mDisturbing_degree = " + aws.getDisturbing_degree() + " , mLost_beacon_amount = " + aws.getLost_beacon_amount());
                        } catch (Throwable th2) {
                            th = th2;
                            if (dr != null) {
                            }
                            if (f != null) {
                            }
                            throw th;
                        }
                    } catch (IOException e4) {
                        e2 = e4;
                        f = f2;
                        Log.d("HwArpVerifier", "readHisiChipsetDebugInfo exception 2" + e2);
                        e2.printStackTrace();
                        if (dr != null) {
                        }
                        if (f != null) {
                        }
                        Log.d("HwArpVerifier", "readHisiChipsetDebugInfo prev, mMonitor_interval = " + aws.getMonitor_interval() + ", mPrevTx_frame_amount = " + aws.getPrevTx_frame_amount() + ", mPrevTx_byte_amount = " + aws.getPrevTx_byte_amount() + ", mPrevTx_data_frame_error_amount = " + aws.getPrevTx_data_frame_error_amount() + ", mPrevTx_retrans_amount = " + aws.getPrevTx_retrans_amount() + ", mPrevRx_frame_amount = " + aws.getPrevRx_frame_amount() + ", mPrevRx_byte_amount = " + aws.getPrevRx_byte_amount() + ", mPrevRx_beacon_from_assoc_ap = " + aws.getPrevRx_beacon_from_assoc_ap() + ", mPrevAp_distance = " + aws.getPrevAp_distance() + ", mPrevDisturbing_degree = " + aws.getPrevDisturbing_degree() + ", mPrevLost_beacon_amount = " + aws.getPrevLost_beacon_amount());
                        Log.d("HwArpVerifier", "readHisiChipsetDebugInfo : , mMonitor_interval = " + aws.getMonitor_interval() + " , mTx_frame_amount = " + aws.getTx_frame_amount() + " , mTx_byte_amount = " + aws.getTx_byte_amount() + " , mTx_data_frame_error_amount = " + aws.getTx_data_frame_error_amount() + " , mTx_retrans_amount = " + aws.getTx_retrans_amount() + " , mRx_frame_amount = " + aws.getRx_frame_amount() + " , mRx_byte_amount =" + aws.getRx_byte_amount() + " , mRx_beacon_from_assoc_ap = " + aws.getRx_beacon_from_assoc_ap() + " , mAp_distance = " + aws.getAp_distance() + " , mDisturbing_degree = " + aws.getDisturbing_degree() + " , mLost_beacon_amount = " + aws.getLost_beacon_amount());
                    } catch (Throwable th3) {
                        th = th3;
                        f = f2;
                        if (dr != null) {
                        }
                        if (f != null) {
                        }
                        throw th;
                    }
                    try {
                        for (String line = dr2.readLine(); line != null; line = dr2.readLine()) {
                            line = line.trim();
                            if (!line.equals(HwArpVerifier.BCM_ROAMING_FLAG_FILE)) {
                                String[] arr = line.split("[ \t:]+");
                                if (arr.length >= 2) {
                                    String tmp = arr[0];
                                    int value;
                                    if (tmp.equals(HwArpVerifier.HISI_TX_FRAME_AMOUNT)) {
                                        value = Integer.parseInt(arr[1]);
                                        aws.setPrevTx_frame_amount(aws.getTx_frame_amount());
                                        aws.setTx_frame_amount(value);
                                    } else if (tmp.equals(HwArpVerifier.HISI_TX_BYTE_AMOUNT)) {
                                        value = Integer.parseInt(arr[1]);
                                        aws.setPrevTx_byte_amount(aws.getTx_byte_amount());
                                        aws.setTx_byte_amount(value);
                                    } else if (tmp.equals(HwArpVerifier.HISI_TX_DATA_FRAME_ERROR_AMOUNT)) {
                                        value = Integer.parseInt(arr[1]);
                                        aws.setPrevTx_data_frame_error_amount(aws.getTx_data_frame_error_amount());
                                        aws.setTx_data_frame_error_amount(value);
                                    } else if (tmp.equals(HwArpVerifier.HISI_TX_RETRANS_AMOUNT)) {
                                        value = Integer.parseInt(arr[1]);
                                        aws.setPrevTx_retrans_amount(aws.getTx_retrans_amount());
                                        aws.setTx_retrans_amount(value);
                                    } else if (tmp.equals(HwArpVerifier.HISI_RX_FRAME_AMOUNT)) {
                                        value = Integer.parseInt(arr[1]);
                                        aws.setPrevRx_frame_amount(aws.getRx_frame_amount());
                                        aws.setRx_frame_amount(value);
                                    } else if (tmp.equals(HwArpVerifier.HISI_RX_BYTE_AMOUNT)) {
                                        value = Integer.parseInt(arr[1]);
                                        aws.setPrevRx_byte_amount(aws.getRx_byte_amount());
                                        aws.setRx_byte_amount(value);
                                    } else if (tmp.equals(HwArpVerifier.HISI_RX_BEACON_FROM_ASSOC_AP)) {
                                        value = Integer.parseInt(arr[1]);
                                        aws.setPrevRx_beacon_from_assoc_ap(aws.getRx_beacon_from_assoc_ap());
                                        aws.setRx_beacon_from_assoc_ap(value);
                                    } else if (tmp.equals(HwArpVerifier.HISI_AP_DISTANCE)) {
                                        value = Integer.parseInt(arr[1]);
                                        aws.setPrevAp_distance(aws.getAp_distance());
                                        aws.setAp_distance(value);
                                    } else if (tmp.equals(HwArpVerifier.HISI_DISTURBING_DEGREE)) {
                                        value = Integer.parseInt(arr[1]);
                                        aws.setPrevDisturbing_degree(aws.getDisturbing_degree());
                                        aws.setDisturbing_degree(value);
                                    } else if (tmp.equals(HwArpVerifier.HISI_LOST_BEACON_AMOUNT)) {
                                        value = Integer.parseInt(arr[1]);
                                        aws.setPrevLost_beacon_amount(aws.getLost_beacon_amount());
                                        aws.setLost_beacon_amount(value);
                                    }
                                }
                            }
                        }
                        dr2.close();
                        f2.close();
                        if (dr2 != null) {
                            try {
                                dr2.close();
                            } catch (Exception e5) {
                                Log.d("HwArpVerifier", "readHisiChipsetDebugInfo exception 3" + e5);
                            }
                        }
                        if (f2 != null) {
                            f2.close();
                        }
                    } catch (FileNotFoundException e6) {
                        e = e6;
                        dr = dr2;
                        f = f2;
                        Log.d("HwArpVerifier", "readHisiChipsetDebugInfo exception 1" + e);
                        if (dr != null) {
                            try {
                                dr.close();
                            } catch (Exception e52) {
                                Log.d("HwArpVerifier", "readHisiChipsetDebugInfo exception 3" + e52);
                            }
                        }
                        if (f != null) {
                            f.close();
                        }
                        Log.d("HwArpVerifier", "readHisiChipsetDebugInfo prev, mMonitor_interval = " + aws.getMonitor_interval() + ", mPrevTx_frame_amount = " + aws.getPrevTx_frame_amount() + ", mPrevTx_byte_amount = " + aws.getPrevTx_byte_amount() + ", mPrevTx_data_frame_error_amount = " + aws.getPrevTx_data_frame_error_amount() + ", mPrevTx_retrans_amount = " + aws.getPrevTx_retrans_amount() + ", mPrevRx_frame_amount = " + aws.getPrevRx_frame_amount() + ", mPrevRx_byte_amount = " + aws.getPrevRx_byte_amount() + ", mPrevRx_beacon_from_assoc_ap = " + aws.getPrevRx_beacon_from_assoc_ap() + ", mPrevAp_distance = " + aws.getPrevAp_distance() + ", mPrevDisturbing_degree = " + aws.getPrevDisturbing_degree() + ", mPrevLost_beacon_amount = " + aws.getPrevLost_beacon_amount());
                        Log.d("HwArpVerifier", "readHisiChipsetDebugInfo : , mMonitor_interval = " + aws.getMonitor_interval() + " , mTx_frame_amount = " + aws.getTx_frame_amount() + " , mTx_byte_amount = " + aws.getTx_byte_amount() + " , mTx_data_frame_error_amount = " + aws.getTx_data_frame_error_amount() + " , mTx_retrans_amount = " + aws.getTx_retrans_amount() + " , mRx_frame_amount = " + aws.getRx_frame_amount() + " , mRx_byte_amount =" + aws.getRx_byte_amount() + " , mRx_beacon_from_assoc_ap = " + aws.getRx_beacon_from_assoc_ap() + " , mAp_distance = " + aws.getAp_distance() + " , mDisturbing_degree = " + aws.getDisturbing_degree() + " , mLost_beacon_amount = " + aws.getLost_beacon_amount());
                    } catch (IOException e7) {
                        e2 = e7;
                        dr = dr2;
                        f = f2;
                        Log.d("HwArpVerifier", "readHisiChipsetDebugInfo exception 2" + e2);
                        e2.printStackTrace();
                        if (dr != null) {
                            try {
                                dr.close();
                            } catch (Exception e522) {
                                Log.d("HwArpVerifier", "readHisiChipsetDebugInfo exception 3" + e522);
                            }
                        }
                        if (f != null) {
                            f.close();
                        }
                        Log.d("HwArpVerifier", "readHisiChipsetDebugInfo prev, mMonitor_interval = " + aws.getMonitor_interval() + ", mPrevTx_frame_amount = " + aws.getPrevTx_frame_amount() + ", mPrevTx_byte_amount = " + aws.getPrevTx_byte_amount() + ", mPrevTx_data_frame_error_amount = " + aws.getPrevTx_data_frame_error_amount() + ", mPrevTx_retrans_amount = " + aws.getPrevTx_retrans_amount() + ", mPrevRx_frame_amount = " + aws.getPrevRx_frame_amount() + ", mPrevRx_byte_amount = " + aws.getPrevRx_byte_amount() + ", mPrevRx_beacon_from_assoc_ap = " + aws.getPrevRx_beacon_from_assoc_ap() + ", mPrevAp_distance = " + aws.getPrevAp_distance() + ", mPrevDisturbing_degree = " + aws.getPrevDisturbing_degree() + ", mPrevLost_beacon_amount = " + aws.getPrevLost_beacon_amount());
                        Log.d("HwArpVerifier", "readHisiChipsetDebugInfo : , mMonitor_interval = " + aws.getMonitor_interval() + " , mTx_frame_amount = " + aws.getTx_frame_amount() + " , mTx_byte_amount = " + aws.getTx_byte_amount() + " , mTx_data_frame_error_amount = " + aws.getTx_data_frame_error_amount() + " , mTx_retrans_amount = " + aws.getTx_retrans_amount() + " , mRx_frame_amount = " + aws.getRx_frame_amount() + " , mRx_byte_amount =" + aws.getRx_byte_amount() + " , mRx_beacon_from_assoc_ap = " + aws.getRx_beacon_from_assoc_ap() + " , mAp_distance = " + aws.getAp_distance() + " , mDisturbing_degree = " + aws.getDisturbing_degree() + " , mLost_beacon_amount = " + aws.getLost_beacon_amount());
                    } catch (Throwable th4) {
                        th = th4;
                        dr = dr2;
                        f = f2;
                        if (dr != null) {
                            try {
                                dr.close();
                            } catch (Exception e5222) {
                                Log.d("HwArpVerifier", "readHisiChipsetDebugInfo exception 3" + e5222);
                                throw th;
                            }
                        }
                        if (f != null) {
                            f.close();
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e8) {
                    e = e8;
                    Log.d("HwArpVerifier", "readHisiChipsetDebugInfo exception 1" + e);
                    if (dr != null) {
                    }
                    if (f != null) {
                    }
                    Log.d("HwArpVerifier", "readHisiChipsetDebugInfo prev, mMonitor_interval = " + aws.getMonitor_interval() + ", mPrevTx_frame_amount = " + aws.getPrevTx_frame_amount() + ", mPrevTx_byte_amount = " + aws.getPrevTx_byte_amount() + ", mPrevTx_data_frame_error_amount = " + aws.getPrevTx_data_frame_error_amount() + ", mPrevTx_retrans_amount = " + aws.getPrevTx_retrans_amount() + ", mPrevRx_frame_amount = " + aws.getPrevRx_frame_amount() + ", mPrevRx_byte_amount = " + aws.getPrevRx_byte_amount() + ", mPrevRx_beacon_from_assoc_ap = " + aws.getPrevRx_beacon_from_assoc_ap() + ", mPrevAp_distance = " + aws.getPrevAp_distance() + ", mPrevDisturbing_degree = " + aws.getPrevDisturbing_degree() + ", mPrevLost_beacon_amount = " + aws.getPrevLost_beacon_amount());
                    Log.d("HwArpVerifier", "readHisiChipsetDebugInfo : , mMonitor_interval = " + aws.getMonitor_interval() + " , mTx_frame_amount = " + aws.getTx_frame_amount() + " , mTx_byte_amount = " + aws.getTx_byte_amount() + " , mTx_data_frame_error_amount = " + aws.getTx_data_frame_error_amount() + " , mTx_retrans_amount = " + aws.getTx_retrans_amount() + " , mRx_frame_amount = " + aws.getRx_frame_amount() + " , mRx_byte_amount =" + aws.getRx_byte_amount() + " , mRx_beacon_from_assoc_ap = " + aws.getRx_beacon_from_assoc_ap() + " , mAp_distance = " + aws.getAp_distance() + " , mDisturbing_degree = " + aws.getDisturbing_degree() + " , mLost_beacon_amount = " + aws.getLost_beacon_amount());
                } catch (IOException e9) {
                    e2 = e9;
                    Log.d("HwArpVerifier", "readHisiChipsetDebugInfo exception 2" + e2);
                    e2.printStackTrace();
                    if (dr != null) {
                    }
                    if (f != null) {
                    }
                    Log.d("HwArpVerifier", "readHisiChipsetDebugInfo prev, mMonitor_interval = " + aws.getMonitor_interval() + ", mPrevTx_frame_amount = " + aws.getPrevTx_frame_amount() + ", mPrevTx_byte_amount = " + aws.getPrevTx_byte_amount() + ", mPrevTx_data_frame_error_amount = " + aws.getPrevTx_data_frame_error_amount() + ", mPrevTx_retrans_amount = " + aws.getPrevTx_retrans_amount() + ", mPrevRx_frame_amount = " + aws.getPrevRx_frame_amount() + ", mPrevRx_byte_amount = " + aws.getPrevRx_byte_amount() + ", mPrevRx_beacon_from_assoc_ap = " + aws.getPrevRx_beacon_from_assoc_ap() + ", mPrevAp_distance = " + aws.getPrevAp_distance() + ", mPrevDisturbing_degree = " + aws.getPrevDisturbing_degree() + ", mPrevLost_beacon_amount = " + aws.getPrevLost_beacon_amount());
                    Log.d("HwArpVerifier", "readHisiChipsetDebugInfo : , mMonitor_interval = " + aws.getMonitor_interval() + " , mTx_frame_amount = " + aws.getTx_frame_amount() + " , mTx_byte_amount = " + aws.getTx_byte_amount() + " , mTx_data_frame_error_amount = " + aws.getTx_data_frame_error_amount() + " , mTx_retrans_amount = " + aws.getTx_retrans_amount() + " , mRx_frame_amount = " + aws.getRx_frame_amount() + " , mRx_byte_amount =" + aws.getRx_byte_amount() + " , mRx_beacon_from_assoc_ap = " + aws.getRx_beacon_from_assoc_ap() + " , mAp_distance = " + aws.getAp_distance() + " , mDisturbing_degree = " + aws.getDisturbing_degree() + " , mLost_beacon_amount = " + aws.getLost_beacon_amount());
                }
                Log.d("HwArpVerifier", "readHisiChipsetDebugInfo prev, mMonitor_interval = " + aws.getMonitor_interval() + ", mPrevTx_frame_amount = " + aws.getPrevTx_frame_amount() + ", mPrevTx_byte_amount = " + aws.getPrevTx_byte_amount() + ", mPrevTx_data_frame_error_amount = " + aws.getPrevTx_data_frame_error_amount() + ", mPrevTx_retrans_amount = " + aws.getPrevTx_retrans_amount() + ", mPrevRx_frame_amount = " + aws.getPrevRx_frame_amount() + ", mPrevRx_byte_amount = " + aws.getPrevRx_byte_amount() + ", mPrevRx_beacon_from_assoc_ap = " + aws.getPrevRx_beacon_from_assoc_ap() + ", mPrevAp_distance = " + aws.getPrevAp_distance() + ", mPrevDisturbing_degree = " + aws.getPrevDisturbing_degree() + ", mPrevLost_beacon_amount = " + aws.getPrevLost_beacon_amount());
                Log.d("HwArpVerifier", "readHisiChipsetDebugInfo : , mMonitor_interval = " + aws.getMonitor_interval() + " , mTx_frame_amount = " + aws.getTx_frame_amount() + " , mTx_byte_amount = " + aws.getTx_byte_amount() + " , mTx_data_frame_error_amount = " + aws.getTx_data_frame_error_amount() + " , mTx_retrans_amount = " + aws.getTx_retrans_amount() + " , mRx_frame_amount = " + aws.getRx_frame_amount() + " , mRx_byte_amount =" + aws.getRx_byte_amount() + " , mRx_beacon_from_assoc_ap = " + aws.getRx_beacon_from_assoc_ap() + " , mAp_distance = " + aws.getAp_distance() + " , mDisturbing_degree = " + aws.getDisturbing_degree() + " , mLost_beacon_amount = " + aws.getLost_beacon_amount());
            }
        }

        public boolean doArpTestAsync(int arpNum, int minResponse, int timeout) {
            Log.d("HwArpVerifier", "doArpTestAsync");
            if (this.mArpRunning) {
                return false;
            }
            sendMessage(obtainMessage(MSG_DO_ARP_ASYNC, new MsgItem(arpNum, minResponse, timeout)));
            return true;
        }

        public void handleMessage(Message msg) {
            if (msg.what == 124) {
                int token = msg.arg1;
                int mode = msg.arg2;
                if (msg.arg1 != HwArpVerifier.this.mCheckStateToken) {
                    Log.w("HwArpVerifier", "ignore msg MSG_CHECK_WIFI_STATE, msg token = " + token + ", expected token = " + HwArpVerifier.this.mCheckStateToken);
                    return;
                }
                checkWifiNetworkState(token, mode);
            } else if (msg.what == MSG_DO_ARP_ASYNC) {
                this.mArpRunning = true;
                MsgItem msgItem = msg.obj;
                if (msgItem != null) {
                    boolean result = HwArpVerifier.this.doArp(msgItem.arpNum, msgItem.minResponse, msgItem.timeout);
                    Log.d("HwArpVerifier", "MSG_DO_ARP_ASYNC:" + result);
                    if (!result) {
                        Intent intent = new Intent(HwArpVerifier.DIAGNOSE_COMPLETE_ACTION);
                        intent.putExtra("MSG_CODE", 14);
                        intent.putExtra("MaxTime", HwArpVerifier.this.mSpendTime);
                        intent.putExtra("PackageName", "HwArpVerifier");
                        HwArpVerifier.this.mContext.sendBroadcast(intent);
                    }
                }
                this.mArpRunning = false;
            } else if (msg.what == 110 || msg.what == 111) {
                if (msg.arg1 == this.mTrafficStatsPollToken) {
                    removeMessages(this.mTrafficStatsPollToken);
                    if (msg.what == 110) {
                        HwWifiCHRNative.setTcpMonitorStat(1);
                        HwArpVerifier.this.mNetstatManager.resetNetstats();
                    }
                    Log.d("HwArpVerifier", "performPollAndLog:");
                    HwArpVerifier.this.mNetstatManager.performPollAndLog();
                    sendMessageDelayed(Message.obtain(this, 111, this.mTrafficStatsPollToken, 0), 5000);
                    doCheckWebSpeed();
                } else {
                    Log.d("HwArpVerifier", "ignore msg " + msg.what + ", current token = " + msg.arg1 + ", expected token = " + this.mTrafficStatsPollToken);
                }
            } else if (msg.what == 112) {
                if (msg.arg1 == this.mTrafficStatsPollToken) {
                    Log.d("HwArpVerifier", "disconnected, trafficStats:");
                    HwArpVerifier.this.mNetstatManager.resetNetstats();
                    HwWifiCHRNative.setTcpMonitorStat(0);
                    HwArpVerifier.this.mlDetectWebCounter = 0;
                    HwArpVerifier.this.mIsFirstCheck = true;
                    removeMessages(110);
                    removeMessages(111);
                    HwArpVerifier.this.mAccessWebStatus.reset();
                }
            } else if (msg.what == MSG_DO_ROUTE_CHECK) {
                if (HwArpVerifier.this.isConnectedToWifi() && HwArpVerifier.this.mRouteDetectCnt <= 5) {
                    try {
                        if (HwArpVerifier.this.isWifiDefaultRouteExist()) {
                            HwArpVerifier.this.mRouteDetectCnt = 0;
                            sendEmptyMessageDelayed(MSG_DO_ROUTE_CHECK, 50000);
                        } else {
                            HwArpVerifier hwArpVerifier = HwArpVerifier.this;
                            hwArpVerifier.mRouteDetectCnt = hwArpVerifier.mRouteDetectCnt + 1;
                            if (!HwArpVerifier.this.isMobileDateActive && HwArpVerifier.this.isRouteRepareSwitchEnabled) {
                                Log.e("HwArpVerifier", "try to reparier wifi default route");
                                HwArpVerifier.this.wifiRepairRoute();
                            }
                            sendEmptyMessageDelayed(MSG_DO_ROUTE_CHECK, 15000);
                        }
                    } catch (Exception e) {
                        Log.e("HwArpVerifier", "exception in wifi route check: " + e);
                    }
                }
            } else if (MSG_PROBE_WEB_RET == msg.what) {
                Log.d("HwArpVerifier", "handleMessage MSG_PROBE_WEB_RET resp = " + msg.arg1);
                if (msg.arg1 < HwCHRWifiSpeedBaseChecker.RTT_THRESHOLD_400 && HwArpVerifier.HTTP_ACCESS_TIMEOUT_RESP != msg.arg1) {
                    HwArpVerifier.this.mAccessWebStatus.setDetectWebStatus(2);
                } else if (HwArpVerifier.this.wcsm == null || msg.obj == null) {
                    Log.e("HwArpVerifier", "handleMessage MSG_PROBE_WEB_RET null == wcsm || null == msg.obj");
                    return;
                } else if (HwArpVerifier.this.wcsm instanceof HwWifiCHRStateManagerImpl) {
                    HwWifiCHRStateManagerImpl hwcsmImpl = (HwWifiCHRStateManagerImpl) HwArpVerifier.this.wcsm;
                    if (msg.obj instanceof AccessWebStatus) {
                        AccessWebStatus aws = msg.obj;
                        if (0 == this.mStartNetUnusableTime) {
                            this.mStartNetUnusableTime = SystemClock.elapsedRealtime();
                        }
                        this.mNetUnusableDuration = SystemClock.elapsedRealtime() - this.mStartNetUnusableTime;
                        if (0 != this.mStartNetAccessibleTime) {
                            this.mNetAccessibleDuration = SystemClock.elapsedRealtime() - this.mStartNetAccessibleTime;
                            this.mStartNetAccessibleTime = 0;
                        }
                        aws.setNetAccessibleTime(this.mNetAccessibleDuration / 1000);
                        aws.setNetUnusableTime(this.mNetUnusableDuration / 1000);
                        HwArpVerifier.this.webMonitor.getPktcnt().fetchPktcntNative();
                        HwCHRWifiLinkMonitor.getDefault().runCounters();
                        HwArpVerifier.this.mAccessWebStatus.setIPRouteRet(getIpRouteTable());
                        String strPkgs = getAllDisableWlanAppsFromDB();
                        HwArpVerifier.this.mAccessWebStatus.setDisableWlanApps(strPkgs);
                        HwArpVerifier.this.mHWGatewayVerifier.getGateWayARPResponses();
                        readSdioQuality(HwArpVerifier.this.mAccessWebStatus);
                        Log.d("HwArpVerifier", "getAllDisableWlanAppsFromDB return : " + strPkgs + ", getDisableWlanAppsApp()" + HwArpVerifier.this.mAccessWebStatus.getDisableWlanApps());
                        Log.d("HwArpVerifier", "accessWeb, mAccessWebStatus : " + HwArpVerifier.this.mAccessWebStatus.toString());
                        Log.d("HwArpVerifier", "accessWeb, mAccessWebStatus  sdio : " + HwArpVerifier.this.mAccessWebStatus.toSdioString());
                        Log.d("HwArpVerifier", "handleMessage MSG_PROBE_WEB_RET tx = " + HwArpVerifier.this.mAccessWebStatus.getTxCnt());
                        HwArpVerifier.this.mAccessWebStatus.setAccessNetFailedCount(HwArpVerifier.this.mAccessWebStatus.getAccessNetFailedCount() + 1);
                        readHisiChipsetDebugInfo(HwArpVerifier.this.mAccessWebStatus);
                        if (HwArpVerifier.this.mIsFirstCheck) {
                            HwArpVerifier.this.mIsFirstCheck = false;
                            HwArpVerifier.this.mAccessWebStatus.setDetectWebStatus(1);
                            hwcsmImpl.updateAccessWebException(87, "FIRST_CONNECT_INTERNET_FAILED", aws);
                        } else if (HwArpVerifier.this.mAccessWebStatus.getTxCnt() >= 2) {
                            hwcsmImpl.updateAccessWebException(87, "ONLY_THE_TX_NO_RX", aws);
                        } else if (HwArpVerifier.this.mAccessWebStatus.getDNSFailed() > 0) {
                            hwcsmImpl.updateAccessWebException(87, "DNS_PARSE_FAILED", aws);
                        } else {
                            hwcsmImpl.updateAccessWebException(87, "OTHER", aws);
                        }
                    } else {
                        Log.e("HwArpVerifier", "handleMessage MSG_PROBE_WEB_RET aws instanceof AccessWebStatus error");
                        return;
                    }
                } else {
                    Log.e("HwArpVerifier", "handleMessage MSG_PROBE_WEB_RET hwcsmImpl instanceof HwWifiCHRStateManagerImpl error");
                    return;
                }
                HwArpVerifier.this.mAccessWebStatus.reset();
            } else {
                HwArpVerifier.this.mHWGatewayVerifier.handleMultiGatewayMessage(msg);
            }
        }

        private boolean isNeedCheck() {
            return (this.mArpState == ArpState.DONT_CHECK || this.mArpState == ArpState.DEAD_CHECK) ? false : true;
        }

        private void transmitState(ArpState state) {
            Log.d("HwArpVerifier", "from " + strState(this.mArpState) + " transmit to state:" + strState(state));
            if (this.mArpState == ArpState.CONFIRM_CHECK && state == ArpState.HEART_CHECK && HwArpVerifier.this.wcsm != null) {
                HwArpVerifier.this.wcsm.updateWifiException(87, "ARP_REASSOC_OK");
            }
            this.mArpState = state;
            if (this.mArpState == ArpState.NORMAL_CHECK) {
                this.mNormalArpFail = 0;
            } else if ((this.mArpState == ArpState.DONT_CHECK || this.mArpState == ArpState.HEART_CHECK) && this.mLinkLayerLogRunning) {
                HwArpVerifier.this.mHwLogUtils.stopLinkLayerLog();
                this.mLinkLayerLogRunning = false;
            }
        }

        private String strState(ArpState state) {
            if (state == ArpState.DONT_CHECK) {
                return "DONT_CHECK";
            }
            if (state == ArpState.HEART_CHECK) {
                return "HEART_CHECK";
            }
            if (state == ArpState.NORMAL_CHECK) {
                return "NORMAL_CHECK";
            }
            if (state == ArpState.CONFIRM_CHECK) {
                return "CONFIRM_CHECK";
            }
            if (state == ArpState.DEAD_CHECK) {
                return "DEAD_CHECK";
            }
            return HwArpVerifier.BCM_ROAMING_FLAG_FILE;
        }

        private int genDynamicCheckPings() {
            int index;
            if (this.mNormalArpFail < 0 || this.mNormalArpFail >= HwArpVerifier.dynamicPings.length) {
                index = HwArpVerifier.dynamicPings.length - 1;
            } else {
                index = this.mNormalArpFail;
            }
            Log.d("HwArpVerifier", "mNormalArpFail: " + this.mNormalArpFail + ", dynamicPings[" + index + "]:" + HwArpVerifier.dynamicPings[index]);
            return HwArpVerifier.dynamicPings[index];
        }

        private void doCheckWifiNetworkState(int token) {
            if (this.mArpState == ArpState.DONT_CHECK) {
                HwArpVerifier hwArpVerifier = HwArpVerifier.this;
                hwArpVerifier.mCheckStateToken = hwArpVerifier.mCheckStateToken + 1;
            } else if (this.mArpState == ArpState.HEART_CHECK) {
                createAndSendMsgDelayed(124, token, 0, HwArpVerifier.SLEEP_PERIOD_TIMEOUT);
            } else if (this.mArpState == ArpState.NORMAL_CHECK) {
                createAndSendMsgDelayed(124, token, 2, 1000);
            } else if (this.mArpState == ArpState.CONFIRM_CHECK) {
                createAndSendMsgDelayed(124, token, 1, 5000);
            } else if (this.mArpState == ArpState.DEAD_CHECK) {
                createAndSendMsgDelayed(124, token, 0, 120000);
            }
        }

        private void handleCheckResult(int token, boolean result) {
            if (this.mArpState == ArpState.HEART_CHECK || this.mArpState == ArpState.NORMAL_CHECK || this.mArpState == ArpState.CONFIRM_CHECK) {
                if (isUsingStaticIp()) {
                    Log.d("HwArpVerifier", "dhcp result not ok, maybe static IP, go on Arp heart check");
                    result = true;
                }
                if (result) {
                    transmitState(ArpState.HEART_CHECK);
                } else if (this.mArpState == ArpState.HEART_CHECK) {
                    transmitState(ArpState.NORMAL_CHECK);
                } else if (this.mArpState == ArpState.CONFIRM_CHECK) {
                    if (!this.mArpSuccLeastOnce) {
                        Log.d("HwArpVerifier", "all Arp test failed in this network, disable Arp check");
                        transmitState(ArpState.DONT_CHECK);
                    } else if (!HwArpVerifier.this.isNeedTriggerReconnectWifi() || (HwArpVerifier.this.isIgnoreArpCheck() ^ 1) == 0) {
                        transmitState(ArpState.HEART_CHECK);
                    } else {
                        HwArpVerifier.this.reconnectWifiNetwork();
                    }
                } else if (this.mArpState == ArpState.NORMAL_CHECK) {
                    this.mNormalArpFail++;
                    if (this.mNormalArpFail >= 2 && (this.mLinkLayerLogRunning ^ 1) != 0) {
                        HwArpVerifier.this.mHwLogUtils.startLinkLayerLog();
                        this.mLinkLayerLogRunning = true;
                    }
                    Log.d("HwArpVerifier", "Notify wifi network is down for NO." + this.mNormalArpFail);
                    if (this.mNormalArpFail >= 5 && (HwArpVerifier.this.isIgnoreArpCheck() ^ 1) != 0) {
                        if (!this.mArpSuccLeastOnce) {
                            Log.d("HwArpVerifier", "all Arp test failed in this network, disable Arp check");
                            transmitState(ArpState.DONT_CHECK);
                        } else if (HwArpVerifier.this.isNeedTriggerReconnectWifi()) {
                            if (!WifiProCommonUtils.isWifiSelfCuring()) {
                                HwArpVerifier.this.recoveryWifiNetwork();
                            }
                            HwArpVerifier.this.notifyNetworkUnreachable();
                            transmitState(ArpState.CONFIRM_CHECK);
                        } else if (this.mNormalArpFail > 15) {
                            Log.d("HwArpVerifier", "Arp failed reach to " + this.mNormalArpFail + " in this network, disable Arp check");
                            transmitState(ArpState.DONT_CHECK);
                        }
                    }
                }
            }
            doCheckWifiNetworkState(token);
        }

        private void checkWifiNetworkState(int token, int mode) {
            Log.d("HwArpVerifier", "check_wifi_state_mode = " + mode + " mCheckStateToken=" + HwArpVerifier.this.mCheckStateToken + " token" + token);
            if (!HwArpVerifier.this.isConnectedToWifi()) {
                Log.d("HwArpVerifier", "Notify network is not connected, need not to do ARP test.");
                HwArpVerifier.this.mCurrentWiFiState = -1;
                transmitState(ArpState.DONT_CHECK);
            }
            if (isNeedCheck()) {
                boolean ret;
                if (this.mArpState == ArpState.HEART_CHECK && HwArpVerifier.this.mHWGatewayVerifier.isEnableDectction()) {
                    HwArpVerifier.this.readArpFromFile();
                    HwArpVerifier.this.mHWGatewayVerifier.getGateWayARPResponses();
                    int gatewayNumber = HwArpVerifier.this.mHWGatewayVerifier.mGW.getGWNum();
                    Log.d("HwArpVerifier", "There are " + gatewayNumber + " mac address for gateway ");
                    if (HwArpVerifier.this.needToDetectGateway()) {
                        Message.obtain(HwArpVerifier.this.mClientHandler, 1004, token, 1).sendToTarget();
                        return;
                    }
                    ret = gatewayNumber > 0;
                } else {
                    ret = HwArpVerifier.this.doArpTest(mode);
                }
                if (!ret) {
                    HwArpVerifier.this.doGratuitousArp(1000);
                    ret = HwArpVerifier.this.pingGateway(2000);
                }
                if (!this.mArpSuccLeastOnce && ret) {
                    this.mArpSuccLeastOnce = true;
                }
                handleCheckResult(token, ret);
            }
        }

        private boolean isUsingStaticIp() {
            if (this.mStaticIpStatus == 0) {
                getStaticIpStatus();
            }
            if ((this.mStaticIpStatus & 2) > 0) {
                return true;
            }
            return false;
        }

        private void getStaticIpStatus() {
            if (isUsingStaticIpFromConfig()) {
                this.mStaticIpStatus = 2;
            } else {
                this.mStaticIpStatus = "ok".equals(SystemProperties.get(new StringBuilder().append("dhcp.").append(SystemProperties.get("wifi.interface", HwArpVerifier.IFACE)).append(".result").toString(), HwArpVerifier.BCM_ROAMING_FLAG_FILE)) ? 1 : 4;
            }
            HwWifiCHRStateManagerImpl wcsmImpl = HwWifiCHRStateManagerImpl.getDefaultImpl();
            if (this.mStaticIpStatus == 4) {
                wcsmImpl.setIpType(2);
            }
            if (this.mStaticIpStatus == 2) {
                wcsmImpl.setIpType(1);
            }
            Log.d("HwArpVerifier", "getStaticIpStatus:" + this.mStaticIpStatus);
        }

        private boolean isUsingStaticIpFromConfig() {
            if (HwArpVerifier.this.mWM == null) {
                HwArpVerifier.this.mWM = (WifiManager) HwArpVerifier.this.mContext.getSystemService("wifi");
            }
            List<WifiConfiguration> configuredNetworks = HwArpVerifier.this.mWM.getConfiguredNetworks();
            if (configuredNetworks != null) {
                int netid = -1;
                WifiInfo info = HwArpVerifier.this.mWM.getConnectionInfo();
                if (info != null) {
                    netid = info.getNetworkId();
                }
                if (netid == -1) {
                    return false;
                }
                for (WifiConfiguration config : configuredNetworks) {
                    if (config != null && config.networkId == netid && config.getIpAssignment() == IpAssignment.STATIC) {
                        return true;
                    }
                }
            }
            return false;
        }

        private void doStaticIpHandler(int result) {
            if (this.mStaticIpStatus == 0) {
                getStaticIpStatus();
            }
            Log.d("HwArpVerifier", "doStaticIpHandler mStaticIpStatus" + this.mStaticIpStatus);
            if (this.mStaticIpStatus == 4) {
                this.mStaticIpStatus |= 8;
            }
        }
    }

    private class HWGatewayVerifier {
        private static final String COUNTRY_CODE_CN = "CN";
        private static final int DEFAULT_ARP_NUMBER = 1;
        private static final int DEFAULT_ARP_TIMEOUT_MS = 1000;
        private static final int DEFAULT_GATEWAY_NUMBER = 1;
        private static final int DETECTION_TIMEOUT = 10000;
        private static final int MSG_NET_ACCESS_DETECT = 1001;
        private static final int MSG_NET_ACCESS_DETECT_END = 1003;
        private static final int MSG_NET_ACCESS_DETECT_FAILED = 1002;
        private static final int MSG_NET_ACCESS_DETECT_REAL = 1004;
        private static final int NET_ACCESS_DETECT_DELAY = 2000;
        private static final int TIME_CLEAR_DNS = 1000;
        private String mCurrentMac;
        private int mEnableAccessDetect;
        private HWMultiGW mGW;

        /* synthetic */ HWGatewayVerifier(HwArpVerifier this$0, HWGatewayVerifier -this1) {
            this();
        }

        private HWGatewayVerifier() {
            this.mEnableAccessDetect = -1;
            this.mGW = new HWMultiGW();
            this.mCurrentMac = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
        }

        private void handleMultiGatewayMessage(Message msg) {
            int token = msg.arg1;
            if (token != HwArpVerifier.this.mCheckStateToken) {
                Log.d("HwArpVerifier", "ignore msg " + msg.what + ", current token = " + token + ", expected token = " + HwArpVerifier.this.mCheckStateToken);
                return;
            }
            String gateway;
            switch (msg.what) {
                case 1001:
                    String mac = this.mGW.getNextGWMACAddr();
                    gateway = this.mGW.getGWIPAddr();
                    if (mac != null && gateway != null) {
                        this.mCurrentMac = mac;
                        HwArpVerifier.this.mWifiNative.setStaticARP(gateway, mac);
                        HwArpVerifier.this.flushNetworkDnsCache();
                        HwArpVerifier.this.flushVmDnsCache();
                        HwArpVerifier.this.mClientHandler.sendMessageDelayed(Message.obtain(HwArpVerifier.this.mClientHandler, MSG_NET_ACCESS_DETECT_REAL, token, 0), 1000);
                        break;
                    }
                    Message.obtain(HwArpVerifier.this.mClientHandler, 1003, token, 0).sendToTarget();
                    break;
                    break;
                case 1002:
                    if (!TextUtils.isEmpty(this.mCurrentMac)) {
                        HwArpVerifier hwArpVerifier = HwArpVerifier.this;
                        hwArpVerifier.mCheckStateToken = hwArpVerifier.mCheckStateToken + 1;
                        gateway = this.mGW.getGWIPAddr();
                        if (!TextUtils.isEmpty(gateway)) {
                            addToBlacklist();
                            HwArpVerifier.this.mWifiNative.delStaticARP(gateway);
                            Message.obtain(HwArpVerifier.this.mClientHandler, 1001, HwArpVerifier.this.mCheckStateToken, 0).sendToTarget();
                            break;
                        }
                    }
                    Message.obtain(HwArpVerifier.this.mClientHandler, 1001, HwArpVerifier.this.mCheckStateToken, 0).sendToTarget();
                    break;
                    break;
                case 1003:
                    HwArpVerifier.this.mClientHandler.doStaticIpHandler(msg.arg2);
                    HwArpVerifier.this.mClientHandler.handleCheckResult(HwArpVerifier.this.mCheckStateToken, true);
                    break;
                case MSG_NET_ACCESS_DETECT_REAL /*1004*/:
                    if (msg.arg2 == 1) {
                        this.mCurrentMac = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
                    }
                    startNetAccessDetection(token);
                    break;
            }
        }

        private void getGateWayARPResponses() {
            getGateWayARPResponses(1, 1000);
        }

        private void getGateWayARPResponses(int arpNum, int timeout) {
            this.mGW.clearGW();
            HWArpPeer hWArpPeer = null;
            try {
                hWArpPeer = HwArpVerifier.this.constructArpPeer();
                if (hWArpPeer == null) {
                    if (hWArpPeer != null) {
                        hWArpPeer.close();
                    }
                    return;
                }
                this.mGW.setGWIPAddr(HwArpVerifier.this.mGateway);
                for (int i = 0; i < arpNum; i++) {
                    boolean isSucc = false;
                    HWMultiGW multiGW = hWArpPeer.getGateWayARPResponses(timeout);
                    if (multiGW != null) {
                        HwArpVerifier.this.mAccessWebStatus.setRTTArp((int) multiGW.getArpRTT());
                        Log.d("HwArpVerifier", "getGateWayARPResponses: arp rtt = " + multiGW.getArpRTT());
                        for (String macAddr : multiGW.getGWMACAddrList()) {
                            this.mGW.setGWMACAddr(macAddr);
                            isSucc = true;
                        }
                        HwArpVerifier.this.wcsm.updateMultiGWCount((byte) this.mGW.getGWNum());
                        HwArpVerifier.this.rssi_summery.updateArpSummery(isSucc, (int) multiGW.getArpRTT(), HwArpVerifier.mRSSI);
                    }
                }
                if (hWArpPeer != null) {
                    hWArpPeer.close();
                }
            } catch (Exception e) {
                if (hWArpPeer != null) {
                    hWArpPeer.close();
                }
            } catch (Throwable th) {
                if (hWArpPeer != null) {
                    hWArpPeer.close();
                }
            }
        }

        private void startNetAccessDetection(int token) {
            new WebDetectThread(token).start();
            Log.d("HwArpVerifier", "access internet timeout:10000");
            HwArpVerifier.this.mClientHandler.sendMessageDelayed(Message.obtain(HwArpVerifier.this.mClientHandler, 1002, token, 0), HwArpVerifier.ACCESS_BAIDU_TIMEOUT);
        }

        private boolean isEnableDectction() {
            if (this.mEnableAccessDetect < 0) {
                String countryCode = SystemProperties.get("ro.product.locale.region", HwArpVerifier.BCM_ROAMING_FLAG_FILE);
                Log.d("HwArpVerifier", "local region:" + countryCode);
                if (COUNTRY_CODE_CN.equalsIgnoreCase(countryCode)) {
                    this.mEnableAccessDetect = 1;
                    HwCHRWebDetectThread.setEnableCheck(true);
                } else {
                    this.mEnableAccessDetect = 0;
                }
            }
            if (this.mEnableAccessDetect == 1) {
                return true;
            }
            return false;
        }

        private void addToBlacklist() {
            if (!TextUtils.isEmpty(this.mCurrentMac)) {
                for (ArpItem item : HwArpVerifier.this.mArpBlacklist) {
                    if (item.sameMacAddress(this.mCurrentMac)) {
                        item.putFail();
                        return;
                    }
                }
                HwArpVerifier.this.mArpBlacklist.add(new ArpItem(this.mCurrentMac, 1));
            }
        }
    }

    private static class MsgItem {
        public int arpNum;
        public int minResponse;
        public int timeout;

        public MsgItem(int arpNum, int minResponse, int timeout) {
            this.arpNum = arpNum;
            this.minResponse = minResponse;
            this.timeout = timeout;
        }
    }

    private class WebDetectThread extends Thread {
        private HwCHRWebDetectThread detect = new HwCHRWebDetectThread(0);
        private int mInnerToken;

        public WebDetectThread(int token) {
            this.mInnerToken = token;
        }

        public void run() {
            boolean ret = this.detect.isInternetConnected();
            if (this.mInnerToken == HwArpVerifier.this.mCheckStateToken) {
                HwArpVerifier.this.mClientHandler.removeMessages(HwWifiLogMsgID.EVENT_UPLOAD_MSS);
                if (ret) {
                    if (!HwWifiDFTConnManager.getInstance().isCommercialUser()) {
                        HwArpVerifier.this.mClientHandler.accessWeb(HwArpVerifier.WEB_CHINAZ_GETIP);
                    }
                    Message.obtain(HwArpVerifier.this.mClientHandler, HwWifiLogMsgID.EVENT_UPLOAD_APVENDORINFO, this.mInnerToken, 1).sendToTarget();
                    return;
                }
                Log.d("HwArpVerifier", "browse web failed, will del static ARP item");
                Message.obtain(HwArpVerifier.this.mClientHandler, HwWifiLogMsgID.EVENT_UPLOAD_MSS, this.mInnerToken, 0).sendToTarget();
                HwArpVerifier.this.errCode = this.detect.getErrorCode();
            }
        }
    }

    protected static class WebParam {
        int mRespCode = 0;
        long mStartTime = 0;
        String mUrl = HwArpVerifier.BCM_ROAMING_FLAG_FILE;

        public WebParam(String url, long time, int code) {
            this.mUrl = url;
            this.mStartTime = time;
            this.mRespCode = code;
        }

        public int getRespCode() {
            return this.mRespCode;
        }

        public void setRespCode(int code) {
            this.mRespCode = code;
        }

        public String getUrl() {
            return this.mUrl;
        }

        public void setUrl(String url) {
            this.mUrl = url;
        }

        public long getStartTime() {
            return this.mStartTime;
        }

        public void setStartTime(long time) {
            this.mStartTime = time;
        }
    }

    private static native void class_init_native();

    private native int nativeReadArpDetail();

    private native void native_init();

    static {
        boolean z;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) == 3) {
            z = true;
        } else {
            z = false;
        }
        BETA_VER = z;
        System.loadLibrary("huaweiwifi-service");
        class_init_native();
        Log.d("HwArpVerifier", "load huaweiwifi-service ");
    }

    public HwArpVerifier(Context context) {
        this.mContext = context;
        this.mWifiNative = WifiInjector.getInstance().getWifiNative();
        this.mNetstatManager = new HWNetstatManager(this.mContext);
        startArpChecker();
        registerForBroadcasts();
        initRoamingFlagFile();
        this.mNwService = Stub.asInterface(ServiceManager.getService("network_management"));
        this.webMonitor = HwCHRWebMonitor.newInstance(context, this.mWifiNative);
        this.wcsm = HwWifiCHRStateManagerImpl.getDefault();
        HwWiFiLogUtils.init(this.mWifiNative);
        this.mHwLogUtils = HwWiFiLogUtils.getDefault();
        this.rssi_summery = new HwCHRWifiRSSIGroupSummery();
        HwCHRWifiRelatedStateMonitor.make(context);
        native_init();
    }

    public static HwArpVerifier getDefault() {
        mLock.lock();
        HwArpVerifier instance = arp_instance;
        mLock.unlock();
        return instance;
    }

    public static synchronized HwArpVerifier newInstance(Context context) {
        HwArpVerifier instance;
        synchronized (HwArpVerifier.class) {
            mLock.lock();
            instance = arp_instance;
            try {
                if (arp_instance == null) {
                    arp_instance = new HwArpVerifier(context);
                    instance = arp_instance;
                }
                mLock.unlock();
            } catch (RuntimeException e) {
                mLock.unlock();
            } catch (Throwable th) {
                mLock.unlock();
            }
        }
        return instance;
    }

    public static void setRssi(int rssi) {
        mRSSI = rssi;
    }

    public HwCHRWifiRSSIGroupSummery getRSSIGroup() {
        return this.rssi_summery.newRSSIGroup();
    }

    public AccessWebStatus getAccessWebStatus() {
        return this.mAccessWebStatus;
    }

    public void startArpChecker() {
        Log.d("HwArpVerifier", "startArpChecker 1");
        if (isEnableChecker()) {
            startLooper();
        }
    }

    public void stopArpChecker() {
        Log.d("HwArpVerifier", "stopArpChecker");
        stopLooper();
    }

    public boolean doArpTest(int arpNum, int minResponse, int timeout, boolean async) {
        if (arpNum <= 0) {
            arpNum = 1;
        }
        if (minResponse > arpNum || minResponse <= 0) {
            minResponse = 1;
        }
        if (timeout <= 0) {
            timeout = 100;
        }
        if (!async) {
            return doArp(arpNum, minResponse, timeout);
        }
        if (!isLooperRunning()) {
            startLooper();
        }
        if (this.mClientHandler != null) {
            return this.mClientHandler.doArpTestAsync(arpNum, minResponse, timeout);
        }
        return true;
    }

    private void startLooper() {
        if (this.mThread == null) {
            this.mThread = new HandlerThread("WiFiArpStateMachine");
            this.mThread.start();
            this.mClientHandler = new ClientHandler(this.mThread.getLooper());
            Log.d("HwArpVerifier", "startLooper");
        }
    }

    private void stopLooper() {
        if (this.mThread != null) {
            this.mThread.quit();
            this.mClientHandler = null;
            this.mThread = null;
            Log.d("HwArpVerifier", "stopLooper");
        }
    }

    private boolean isLooperRunning() {
        return this.mThread != null;
    }

    private boolean isEnableChecker() {
        return true;
    }

    private void registerForBroadcasts() {
        if (!this.mRegisterReceiver) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.net.wifi.STATE_CHANGE");
            intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            intentFilter.addAction("android.intent.action.ANY_DATA_STATE");
            this.mContext.registerReceiver(this.mReceiver, intentFilter);
        }
    }

    public void unregisterForBroadcasts() {
        if (this.mRegisterReceiver) {
            this.mContext.unregisterReceiver(this.mReceiver);
        }
    }

    private void initRoamingFlagFile() {
        String chipType = SystemProperties.get("ro.connectivity.chiptype", BCM_ROAMING_FLAG_FILE);
        if (chipType != null && chipType.equalsIgnoreCase("hi110x")) {
            this.mRoamingFlagFile = HI110X_ROAMING_FLAG_FILE;
        } else if (chipType == null || !chipType.equalsIgnoreCase("hisi")) {
            this.mRoamingFlagFile = BCM_ROAMING_FLAG_FILE;
        } else {
            this.mRoamingFlagFile = HI1102_ROAMING_FLAG_FILE;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x0075 A:{SYNTHETIC, Splitter: B:32:0x0075} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0081 A:{SYNTHETIC, Splitter: B:38:0x0081} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean readRoamingFlag() {
        Exception e;
        Throwable th;
        boolean ret = false;
        String roam_flag = "roam_status=";
        BufferedReader in = null;
        try {
            if (this.mRoamingFlagFile == null || this.mRoamingFlagFile.isEmpty()) {
                return false;
            }
            File file = new File(this.mRoamingFlagFile);
            if (!file.exists()) {
                return false;
            }
            BufferedReader in2 = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            try {
                String s = in2.readLine();
                if (s != null) {
                    int pos = s.indexOf("roam_status=");
                    if (pos >= 0 && "roam_status=".length() + pos < s.length()) {
                        String flag = s.substring("roam_status=".length() + pos);
                        if (flag != null) {
                            ret = "1".equals(flag);
                        }
                    }
                }
                if (in2 != null) {
                    try {
                        in2.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                in = in2;
            } catch (Exception e3) {
                e2 = e3;
                in = in2;
                try {
                    e2.printStackTrace();
                    if (in != null) {
                    }
                    return ret;
                } catch (Throwable th2) {
                    th = th2;
                    if (in != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                in = in2;
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e22) {
                        e22.printStackTrace();
                    }
                }
                throw th;
            }
            return ret;
        } catch (Exception e4) {
            e22 = e4;
            e22.printStackTrace();
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e222) {
                    e222.printStackTrace();
                }
            }
            return ret;
        }
    }

    private boolean doArpTest(int mode) {
        if (mode == 0) {
            return doArp(1, 1, 1000);
        }
        if (mode == 1) {
            return doArp(2, 1, 5000);
        }
        if (mode != 2 || this.mClientHandler == null) {
            return true;
        }
        return doArp(this.mClientHandler.genDynamicCheckPings(), 1, 1000);
    }

    private boolean doArp(int arpNum, int minResponse, int timeout) {
        this.mSpendTime = 0;
        HWArpPeer hWArpPeer = null;
        Log.d("HwArpVerifier", "doArp() arpnum:" + arpNum + ", minResponse:" + minResponse + ", timeout:" + timeout);
        boolean retArp;
        try {
            hWArpPeer = constructArpPeer();
            if (hWArpPeer == null) {
                if (hWArpPeer != null) {
                    hWArpPeer.close();
                }
                return true;
            }
            int responses = 0;
            for (int i = 0; i < arpNum; i++) {
                long startTimestamp = System.currentTimeMillis();
                if (isIgnoreArpCheck()) {
                    Log.d("HwArpVerifier", "isIgnoreArpCheck is ture, ignore ARP check");
                    responses++;
                } else if (hWArpPeer.doArp(timeout) != null) {
                    responses++;
                }
                int spendTime = (int) (System.currentTimeMillis() - startTimestamp);
                if (spendTime > this.mSpendTime) {
                    this.mSpendTime = spendTime;
                }
            }
            Log.d("HwArpVerifier", "ARP test result: " + responses + "/" + arpNum);
            if (responses >= minResponse) {
                retArp = true;
            } else {
                retArp = false;
            }
            if (hWArpPeer != null) {
                hWArpPeer.close();
            }
            this.rssi_summery.updateArpSummery(retArp, this.mSpendTime, mRSSI);
            return retArp;
        } catch (SocketException se) {
            Log.e("HwArpVerifier", "exception in ARP test: " + se);
            retArp = true;
            if (hWArpPeer != null) {
                hWArpPeer.close();
            }
        } catch (IllegalArgumentException ae) {
            Log.e("HwArpVerifier", "exception in ARP test:" + ae);
            retArp = true;
            if (hWArpPeer != null) {
                hWArpPeer.close();
            }
        } catch (Exception e) {
            retArp = false;
            if (hWArpPeer != null) {
                hWArpPeer.close();
            }
        } catch (Throwable th) {
            if (hWArpPeer != null) {
                hWArpPeer.close();
            }
        }
    }

    private void doGratuitousArp(int timeout) {
        byte[] rspMac = null;
        HWArpPeer hWArpPeer = null;
        try {
            hWArpPeer = constructArpPeer();
            if (hWArpPeer == null) {
                if (hWArpPeer != null) {
                    hWArpPeer.close();
                }
                return;
            }
            if (isIgnoreArpCheck()) {
                Log.d("HwArpVerifier", "isIgnoreArpCheck is ture, ignore doGratuitousArp");
            } else {
                rspMac = hWArpPeer.doGratuitousArp(timeout);
            }
            if (hWArpPeer != null) {
                hWArpPeer.close();
            }
            if (rspMac != null && rspMac.length == 6) {
                Log.w("HwArpVerifier", String.format("%02x:%02x:%02x:%02x:%02x:%02x", new Object[]{Byte.valueOf(rspMac[0]), Byte.valueOf(rspMac[1]), Byte.valueOf(rspMac[2]), Byte.valueOf(rspMac[3]), Byte.valueOf(rspMac[4]), Byte.valueOf(rspMac[5])}) + "alse use My IP(IP conflict detected)");
            }
        } catch (Exception e) {
            Log.e("HwArpVerifier", "exception in GARP test:" + e);
            if (hWArpPeer != null) {
                hWArpPeer.close();
            }
        } catch (Throwable th) {
            if (hWArpPeer != null) {
                hWArpPeer.close();
            }
        }
    }

    private LinkProperties getCurrentLinkProperties() {
        if (this.mCM == null) {
            this.mCM = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
        return this.mCM.getLinkProperties(1);
    }

    private boolean isConnectedToWifi() {
        boolean z = true;
        if (this.mNetworkInfo == null) {
            return false;
        }
        DetailedState state = this.mNetworkInfo.getDetailedState();
        if (!(state == DetailedState.CONNECTED || state == DetailedState.VERIFYING_POOR_LINK)) {
            z = false;
        }
        return z;
    }

    private HWArpPeer constructArpPeer() throws SocketException {
        if (this.mWM == null) {
            this.mWM = (WifiManager) this.mContext.getSystemService("wifi");
        }
        WifiInfo wifiInfo = this.mWM.getConnectionInfo();
        LinkProperties linkProperties = getCurrentLinkProperties();
        String linkIFName = linkProperties != null ? linkProperties.getInterfaceName() : IFACE;
        String macAddr = null;
        InetAddress linkAddr = null;
        if (wifiInfo != null) {
            macAddr = wifiInfo.getMacAddress();
            linkAddr = NetworkUtils.intToInetAddress(wifiInfo.getIpAddress());
        }
        InetAddress gateway = null;
        DhcpInfo dhcpInfo = this.mWM.getDhcpInfo();
        if (!(dhcpInfo == null || dhcpInfo.gateway == 0)) {
            gateway = NetworkUtils.intToInetAddress(dhcpInfo.gateway);
        }
        if (gateway == null) {
            return null;
        }
        this.mGateway = gateway.getHostAddress();
        return new HWArpPeer(linkIFName, linkAddr, macAddr, gateway);
    }

    private void handleWifiSwitchChanged(int state) {
        Log.d("HwArpVerifier", "handleWifiSwitchChanged state:" + state);
        switch (state) {
            case 3:
                resetDurationControlParams();
                return;
            default:
                return;
        }
    }

    private void resetDurationControlParams() {
        this.mShortDurationStartTime = 0;
        this.mLongDurationStartTime = 0;
        this.mShortTriggerCnt = 0;
        this.mLongTriggerCnt = 0;
        this.mLastSSID = null;
    }

    private void updateDurationControlParamsIfNeed() {
        String ssid = getCurrentSsid();
        if (ssid == null || "<unknown ssid>".equals(ssid)) {
            Log.e("HwArpVerifier", "current SSID is empty.");
            return;
        }
        if (this.mLastSSID == null) {
            this.mLastSSID = ssid;
        } else if (!ssid.equals(this.mLastSSID)) {
            Log.d("HwArpVerifier", "connected SSID " + this.mLastSSID + " changed to " + ssid);
            resetDurationControlParams();
            this.mLastSSID = ssid;
        }
    }

    private WifiInfo getCurrentWifiInfo() {
        if (this.mWM == null) {
            this.mWM = (WifiManager) this.mContext.getSystemService("wifi");
        }
        return this.mWM.getConnectionInfo();
    }

    private String getCurrentSsid() {
        WifiInfo curWifi = getCurrentWifiInfo();
        if (curWifi != null) {
            return curWifi.getSSID();
        }
        Log.e("HwArpVerifier", "fail to get current wifi info in getCurrentSsid");
        return null;
    }

    private boolean isPassDurationControl() {
        boolean ret;
        long now = System.currentTimeMillis();
        if (this.mShortDurationStartTime == 0) {
            this.mShortDurationStartTime = now;
        }
        if (this.mLongDurationStartTime == 0) {
            this.mLongDurationStartTime = now;
        }
        Log.d("HwArpVerifier", "short duration: [now:" + now + ", lastTime:" + this.mShortDurationStartTime + ", duration:" + SHORT_ARP_FAIL_DURATION + ", failTimes:" + this.mShortTriggerCnt + ", failThreshold:" + 2);
        Log.d("HwArpVerifier", "long duration: [now:" + now + ", lastTime:" + this.mLongDurationStartTime + ", duration:" + LONG_ARP_FAIL_DURATION + ", failTimes:" + this.mLongTriggerCnt + ", failThreshold:" + 6);
        if (now - this.mShortDurationStartTime > SHORT_ARP_FAIL_DURATION) {
            this.mShortTriggerCnt = 0;
            this.mShortDurationStartTime = now;
            ret = true;
        } else if (this.mShortTriggerCnt >= 2) {
            ret = false;
        } else {
            ret = true;
        }
        Log.d("HwArpVerifier", "short duration control ret is:" + ret);
        if (!ret) {
            return false;
        }
        this.mShortTriggerCnt++;
        if (now - this.mLongDurationStartTime > LONG_ARP_FAIL_DURATION) {
            this.mLongTriggerCnt = 0;
            this.mLongDurationStartTime = now;
            ret = true;
        } else if (this.mLongTriggerCnt >= 6) {
            ret = false;
        } else {
            ret = true;
        }
        if (ret) {
            this.mLongTriggerCnt++;
        }
        Log.d("HwArpVerifier", "long duration control ret is:" + ret);
        return ret;
    }

    private boolean isNeedTriggerReconnectWifi() {
        if (isPassDurationControl()) {
            return true;
        }
        Log.e("HwArpVerifier", "don't pass duration control, skip Wifi reconnect.");
        return false;
    }

    private boolean isIgnoreArpCheck() {
        if (isWeakSignal()) {
            return true;
        }
        if (!readRoamingFlag()) {
            return false;
        }
        Log.d("HwArpVerifier", "It's WiFi roaming now, ignore arp check");
        return true;
    }

    private boolean isWeakSignal() {
        WifiInfo curWifi = getCurrentWifiInfo();
        if (curWifi == null) {
            Log.e("HwArpVerifier", "fail to get current wifi info in isWeakSignal.");
            return false;
        }
        int rssi = curWifi.getRssi();
        if (rssi > WEAK_SIGNAL_THRESHOLD) {
            return false;
        }
        Log.e("HwArpVerifier", "current WIFI rssi:" + rssi + " is weak");
        return true;
    }

    private void recoveryWifiNetwork() {
        if (!isSupplicantStopped() && hasWrongAction()) {
            triggerDisableNMode();
        }
    }

    private void reconnectWifiNetwork() {
        Log.d("HwArpVerifier", "Atfer reassociate, network is still broken");
        WifiInfo wifiInfo = this.mWM.getConnectionInfo();
        Intent intent = new Intent(ACTION_ARP_RECONNECT_WIFI);
        if (wifiInfo != null) {
            intent.putExtra("ssid", wifiInfo.getSSID());
        }
        this.mContext.sendBroadcast(intent, "android.permission.ACCESS_WIFI_STATE");
    }

    private void notifyNetworkUnreachable() {
        this.wcsm.updateWifiException(87, "ARP_UNREACHABLE");
    }

    public static String readFileByChars(String fileName) {
        File file = new File(fileName);
        if (!file.exists() || (file.canRead() ^ 1) != 0) {
            return BCM_ROAMING_FLAG_FILE;
        }
        InputStreamReader inputStreamReader = null;
        char[] tempChars = new char[512];
        StringBuilder sb = new StringBuilder();
        try {
            if (Charset.isSupported("UTF-8")) {
                inputStreamReader = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
            } else if (Charset.isSupported("US-ASCII")) {
                inputStreamReader = new InputStreamReader(new FileInputStream(fileName), "US-ASCII");
            } else {
                inputStreamReader = new InputStreamReader(new FileInputStream(fileName), Charset.defaultCharset());
            }
            while (true) {
                int charRead = inputStreamReader.read(tempChars);
                if (charRead == -1) {
                    break;
                }
                sb.append(tempChars, 0, charRead);
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e2) {
                }
            }
        } catch (Throwable th) {
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e3) {
                }
            }
        }
        return sb.toString();
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x006d A:{SYNTHETIC, Splitter: B:21:0x006d} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0076 A:{SYNTHETIC, Splitter: B:26:0x0076} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String writeFile(String fileName, String ctrl) {
        IOException ie;
        Throwable th;
        String result = "success";
        File file = new File(fileName);
        if (file.exists() && (file.canWrite() ^ 1) == 0) {
            OutputStream out = null;
            try {
                OutputStream out2 = new FileOutputStream(file);
                try {
                    out2.write(ctrl.getBytes(Charset.defaultCharset()));
                    out2.flush();
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (IOException e) {
                        }
                    }
                    out = out2;
                } catch (IOException e2) {
                    ie = e2;
                    out = out2;
                    try {
                        result = "IOException occured";
                        ie.printStackTrace();
                        if (out != null) {
                        }
                        return result;
                    } catch (Throwable th2) {
                        th = th2;
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e3) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    out = out2;
                    if (out != null) {
                    }
                    throw th;
                }
            } catch (IOException e4) {
                ie = e4;
                result = "IOException occured";
                ie.printStackTrace();
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e5) {
                    }
                }
                return result;
            }
            return result;
        }
        Log.d("HwArpVerifier", "file is exists " + file.exists() + " file can write " + file.canWrite());
        return BCM_ROAMING_FLAG_FILE;
    }

    private final boolean pingGateway(int timeout) {
        if (this.mGateway == null) {
            return false;
        }
        if (!isIgnoreArpCheck()) {
            return NetWorkisReachable(this.mGateway, timeout);
        }
        Log.d("HwArpVerifier", "isIgnoreArpCheck is ture, ignore ping gateway");
        return true;
    }

    private final boolean NetWorkisReachable(String ipAddress, int timeout) {
        boolean z = false;
        Log.d("HwArpVerifier", "NetWorkisReachable  enter");
        try {
            z = Inet4Address.getByName(ipAddress).isReachable(timeout);
            Log.d("HwArpVerifier", "NetWorkisReachable  ipAddress =" + ipAddress + ",ret=" + z);
        } catch (Exception e) {
            Log.e("HwArpVerifier", "NetWorkisReachable fail");
        }
        Log.d("HwArpVerifier", "NetWorkisReachable");
        return z;
    }

    private boolean isSupplicantStopped() {
        String suppcantStatus = BCM_ROAMING_FLAG_FILE;
        String chipType = SystemProperties.get("ro.connectivity.chiptype", BCM_ROAMING_FLAG_FILE);
        if (chipType == null || !chipType.equalsIgnoreCase("hi110x")) {
            suppcantStatus = SystemProperties.get("init.svc.p2p_supplicant", "running");
        } else {
            suppcantStatus = SystemProperties.get("init.svc.wpa_supplicant", "running");
        }
        Log.d("HwArpVerifier", "wpa_supplicant state:" + suppcantStatus);
        return "stopped".equals(suppcantStatus);
    }

    private boolean hasWrongAction() {
        String value = readFileByChars(WIFI_WRONG_ACTION_FLAG);
        Log.d("HwArpVerifier", "hasWrongAction:" + value);
        return "1".equals(value.trim());
    }

    private void triggerDisableNMode() {
        Log.d("HwArpVerifier", "triggerDisableNMode enter");
        writeFile(WIFI_ARP_TIMEOUT, "1");
    }

    private void reportArpDetail(String ipaddr, String hwaddr, int flag, String device) {
        this.mArpItems.add(new ArpItem(ipaddr, hwaddr, flag, device));
    }

    private void readArpFromFile() {
        this.mArpItems.clear();
        nativeReadArpDetail();
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x0055 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0090  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean needToDetectGateway() {
        HwCHRWebDetectThread.setFirstDetect(this.mFirstDetect);
        if (this.mFirstDetect) {
            this.mFirstDetect = false;
            return true;
        }
        ArrayList<String> unfoundlist = new ArrayList();
        ArrayList<String> maclist = this.mHWGatewayVerifier.mGW.getGWMACAddrList();
        int i = maclist.size() - 1;
        while (i >= 0) {
            for (ArpItem blackitem : this.mArpBlacklist) {
                if (blackitem.matchMaxRetried() && blackitem.sameMacAddress((String) maclist.get(i))) {
                    maclist.remove(i);
                    break;
                }
            }
            i--;
        }
        for (ArpItem arpitem : this.mArpItems) {
            boolean found = true;
            for (String mac : maclist) {
                if (arpitem.isValid() && arpitem.sameIpaddress(this.mGateway)) {
                    if (!arpitem.sameMacAddress(mac)) {
                        found = false;
                    } else if (arpitem.isStaticArp()) {
                        return false;
                    } else {
                        found = true;
                        if (found) {
                            unfoundlist.add(arpitem.hwaddr);
                        }
                    }
                }
            }
            if (found) {
            }
        }
        maclist.addAll(unfoundlist);
        return maclist.size() > 1;
    }

    public void startWifiRouteCheck() {
        if (this.mClientHandler != null) {
            Log.d("HwArpVerifier", "startWifiRouteCheck");
            if (this.mClientHandler.hasMessages(ClientHandler.MSG_DO_ROUTE_CHECK)) {
                this.mClientHandler.removeMessages(ClientHandler.MSG_DO_ROUTE_CHECK);
            }
            this.mClientHandler.sendEmptyMessageDelayed(ClientHandler.MSG_DO_ROUTE_CHECK, 5000);
        }
    }

    public void stopWifiRouteCheck() {
        if (this.mClientHandler != null && this.mClientHandler.hasMessages(ClientHandler.MSG_DO_ROUTE_CHECK)) {
            this.mClientHandler.removeMessages(ClientHandler.MSG_DO_ROUTE_CHECK);
        }
    }

    private boolean isWifiDefaultRouteExist() {
        if (this.mRevLinkProperties == null) {
            throw new NullPointerException("mRevLinkProperties is null");
        } else if (this.mClientHandler == null) {
            return false;
        } else {
            String wifiRoutes = this.mClientHandler.getIpRouteTable();
            if (BETA_VER) {
                Log.d("HwArpVerifier", "---------  wifi route notify -------");
            }
            if (BETA_VER) {
                Log.d("HwArpVerifier", wifiRoutes);
            }
            if (BETA_VER) {
                Log.d("HwArpVerifier", "------------------------------------");
            }
            if (wifiRoutes != null) {
                String[] tok = wifiRoutes.toString().split("\n");
                if (tok == null) {
                    Log.e("HwArpVerifier", "wifi default route is not exist, tok==null");
                    return false;
                }
                int length = tok.length;
                int i = 0;
                while (i < length) {
                    String routeline = tok[i];
                    if (routeline.length() <= 10 || !routeline.startsWith("default") || routeline.indexOf(IFACE) < 0) {
                        i++;
                    } else {
                        Log.d("HwArpVerifier", "Notify wifi default route is ok");
                        return true;
                    }
                }
            }
            Log.e("HwArpVerifier", "wifi default route is not exist!");
            return false;
        }
    }

    private boolean wifiRepairRoute() {
        int netid = -1;
        if (((ConnectivityManager) this.mContext.getSystemService("connectivity")) != null) {
            Network network = HwServiceFactory.getHwConnectivityManager().getNetworkForTypeWifi();
            if (network == null) {
                Log.e("HwArpVerifier", "wifiRepairRoute, network is null");
                return false;
            }
            netid = network.netId;
            Log.d("HwArpVerifier", "netid = " + netid);
        }
        Log.e("HwArpVerifier", "Enter wifiReparierRoute");
        if (this.mNwService == null || this.mRevLinkProperties == null) {
            Log.e("HwArpVerifier", "Repair wifi default Route failed, mNwService mRevLinkProperties is null");
            return false;
        } else if (!isConnectedToWifi()) {
            return false;
        } else {
            for (RouteInfo r : this.mRevLinkProperties.getRoutes()) {
                if (r.isDefaultRoute()) {
                    Log.d("HwArpVerifier", "mRevLinkProperties=" + this.mRevLinkProperties);
                    if (netid > 0) {
                        try {
                            Log.d("HwArpVerifier", "ifacename=" + this.mRevLinkProperties.getInterfaceName());
                            this.mNwService.addRoute(netid, r);
                            this.mNwService.setDefaultNetId(netid);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d("HwArpVerifier", "netid is not available");
                        return false;
                    }
                    Log.d("HwArpVerifier", "RouteDetect addRoute finish");
                    return true;
                }
            }
            Log.d("HwArpVerifier", "Repair wifi default failed, no default route in mRevLinkProperties");
            return false;
        }
    }

    private void flushNetworkDnsCache() {
        if (this.mCM == null || this.mNwService == null) {
            Log.d("HwArpVerifier", "flushNetworkDnsCache failed: mCM or mNwService is null");
        } else if (isConnectedToWifi()) {
            Network network = HwServiceFactory.getHwConnectivityManager().getNetworkForTypeWifi();
            int netid = network == null ? -1 : network.netId;
            this.mLastNetworkId = netid;
            Log.d("HwArpVerifier", "flushNetworkDnsCache netid:" + netid);
        }
    }

    private void flushVmDnsCache() {
        Intent intent = new Intent("android.intent.action.CLEAR_DNS_CACHE");
        intent.addFlags(536870912);
        intent.addFlags(67108864);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void handleWiFiDnsStats(int networkid) {
        HwWifiStatStoreImpl.getDefault().handleWiFiDnsStats(networkid);
    }

    public int getAccessNetFailedCount() {
        return this.mAccessWebStatus.getAccessNetFailedCount();
    }

    public void updatePortalStatus(int respCode) {
        HwWifiCHRStateManagerImpl.getDefaultImpl().updatePortalStatus(respCode);
    }

    public long getGateWayArpRTT(int timeout) {
        HWArpPeer hWArpPeer = null;
        try {
            hWArpPeer = constructArpPeer();
            if (hWArpPeer == null) {
                if (hWArpPeer != null) {
                    hWArpPeer.close();
                }
                return -1;
            }
            HWMultiGW multiGW = hWArpPeer.getGateWayARPResponses(timeout);
            if (multiGW != null) {
                long arpRtt = multiGW.getArpRTT();
                if (hWArpPeer != null) {
                    hWArpPeer.close();
                }
                return arpRtt;
            }
            if (hWArpPeer != null) {
                hWArpPeer.close();
            }
            return -1;
        } catch (SocketException e) {
            if (hWArpPeer != null) {
                hWArpPeer.close();
            }
        } catch (Throwable th) {
            if (hWArpPeer != null) {
                hWArpPeer.close();
            }
        }
    }

    public boolean mssGatewayVerifier() {
        boolean arpRet = false;
        if (this.mGateway == null) {
            Log.d("HwArpVerifier", "HwMSSHandler: mGateway is null");
            return false;
        }
        for (int i = 0; i < 3; i++) {
            long ret = getGateWayArpRTT(1000);
            Log.d("HwArpVerifier", "HwMSSHandler: mssGatewayVerifier ret:" + ret);
            if (ret != -1) {
                arpRet = true;
                break;
            }
        }
        return arpRet;
    }
}

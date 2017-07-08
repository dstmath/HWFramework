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
import android.text.TextUtils;
import android.util.Log;
import com.android.server.HwServiceFactory;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieDataBaseImpl;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.connectivitylog.LogManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
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
    private static String HISI_CHIPSET_DEBUG_FILE = null;
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
    private static final Uri URI_NETAPP = null;
    private static final Uri URI_NETAPP_SYSTEM = null;
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
    private static HwArpVerifier arp_instance;
    private static final int[] dynamicPings = null;
    private static ReentrantLock mLock;
    private static int mRSSI;
    private int errCode;
    private boolean isMobileDateActive;
    private boolean isRouteRepareSwitchEnabled;
    private AccessWebStatus mAccessWebStatus;
    private ArrayList<ArpItem> mArpBlacklist;
    private ArrayList<ArpItem> mArpItems;
    private ConnectivityManager mCM;
    private int mCheckStateToken;
    private ClientHandler mClientHandler;
    private Context mContext;
    private int mCurrentWiFiState;
    private boolean mFirstDetect;
    private String mGateway;
    private HWGatewayVerifier mHWGatewayVerifier;
    private HwWiFiLogUtils mHwLogUtils;
    private boolean mIsFirstCheck;
    private int mLastNetworkId;
    private String mLastSSID;
    private long mLongDurationStartTime;
    private int mLongTriggerCnt;
    private HWNetstatManager mNetstatManager;
    private NetworkInfo mNetworkInfo;
    private INetworkManagementService mNwService;
    private final BroadcastReceiver mReceiver;
    private boolean mRegisterReceiver;
    private LinkProperties mRevLinkProperties;
    private String mRoamingFlagFile;
    private int mRouteDetectCnt;
    private Handler mServiceHandler;
    private long mShortDurationStartTime;
    private int mShortTriggerCnt;
    private int mSpendTime;
    private HandlerThread mThread;
    private WifiManager mWM;
    private WifiNative mWifiNative;
    private long mlDetectWebCounter;
    private HwCHRWifiRSSIGroupSummery rssi_summery;
    private HwWifiCHRStateManager wcsm;
    private HwCHRWebMonitor webMonitor;

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
        int mNetAccessibleTime;
        int mNetDisableCnt;
        int mNetUnusableTime;
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
            this.mNetAccessibleTime = HwArpVerifier.WIFI_STATE_INITIALED;
            this.mNetUnusableTime = HwArpVerifier.WIFI_STATE_INITIALED;
            reset();
        }

        public AccessWebStatus(AccessWebStatus aws) {
            this.mNetAccessibleTime = HwArpVerifier.WIFI_STATE_INITIALED;
            this.mNetUnusableTime = HwArpVerifier.WIFI_STATE_INITIALED;
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
            this.mRxCnt = HwArpVerifier.WIFI_STATE_INITIALED;
            this.mTxCnt = HwArpVerifier.WIFI_STATE_INITIALED;
            this.mNetDisableCnt = HwArpVerifier.WIFI_STATE_INITIALED;
            this.mDnsFailedCnt = HwArpVerifier.WIFI_STATE_INITIALED;
            this.mRxSleepCnt = HwArpVerifier.WIFI_STATE_INITIALED;
            this.mPeriodTime = 0;
            this.mIpRoute = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
            this.mDisableWlanApps = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
            this.mOldDnsFailedCnt = HwArpVerifier.WIFI_STATE_INITIALED;
            this.mSdio_info_readbreq = HwArpVerifier.WIFI_STATE_INITIALED;
            this.mSdio_info_readb = HwArpVerifier.WIFI_STATE_INITIALED;
            this.mSdio_info_writebreq = HwArpVerifier.WIFI_STATE_INITIALED;
            this.mSdio_info_writeb = HwArpVerifier.WIFI_STATE_INITIALED;
            this.mSdio_info_readwreq = HwArpVerifier.WIFI_STATE_INITIALED;
            this.mSdio_info_readw = HwArpVerifier.WIFI_STATE_INITIALED;
            this.mSdio_info_writewreq = HwArpVerifier.WIFI_STATE_INITIALED;
            this.mSdio_info_writew = HwArpVerifier.WIFI_STATE_INITIALED;
            this.mSdio_info_ksosetreq = HwArpVerifier.WIFI_STATE_INITIALED;
            this.mSdio_info_ksosetretry = HwArpVerifier.WIFI_STATE_INITIALED;
            this.mSdio_info_ksoclrreq = HwArpVerifier.WIFI_STATE_INITIALED;
            this.mSdio_info_ksoclrretry = HwArpVerifier.WIFI_STATE_INITIALED;
            this.mWP = null;
            Log.d(HwArpVerifier.TAG, "AccessWebStatus : reset all");
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
            this.failcnt = HwArpVerifier.WIFI_STATE_INITIALED;
            this.ipaddr = ip;
            this.hwaddr = mac.toLowerCase(Locale.ENGLISH);
            this.device = ifname;
            this.flag = flag;
        }

        public ArpItem(String mac, int failcnt) {
            this.failcnt = HwArpVerifier.WIFI_STATE_INITIALED;
            this.ipaddr = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
            this.hwaddr = mac.toLowerCase(Locale.ENGLISH);
            this.device = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
            this.flag = HwArpVerifier.WIFI_STATE_INITIALED;
            this.failcnt = failcnt;
        }

        public boolean matchMaxRetried() {
            return this.failcnt >= MAX_FAIL_CNT ? HwArpVerifier.DBG : false;
        }

        public void putFail() {
            this.failcnt += HwArpVerifier.WIFI_STATE_CONNECTED;
        }

        public boolean sameIpaddress(String ip) {
            return !TextUtils.isEmpty(ip) ? ip.equals(this.ipaddr) : false;
        }

        public boolean isStaticArp() {
            return (this.flag & ATF_PERM) == ATF_PERM ? HwArpVerifier.DBG : false;
        }

        public boolean sameMacAddress(String mac) {
            return mac != null ? mac.toLowerCase(Locale.ENGLISH).equals(this.hwaddr) : false;
        }

        public boolean isValid() {
            return (((this.flag & ATF_COM) == ATF_COM ? HwArpVerifier.DBG : false) && HwArpVerifier.IFACE.equals(this.device)) ? this.hwaddr.length() == 17 ? HwArpVerifier.DBG : false : false;
        }

        public String toString() {
            Object[] objArr = new Object[ATF_PERM];
            objArr[HwArpVerifier.WIFI_STATE_INITIALED] = this.ipaddr;
            objArr[HwArpVerifier.WIFI_STATE_CONNECTED] = Integer.valueOf(this.flag);
            objArr[ATF_COM] = this.hwaddr;
            objArr[3] = this.device;
            return String.format(Locale.ENGLISH, "%s %d %s %s", objArr);
        }
    }

    private enum ArpState {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.HwArpVerifier.ArpState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.HwArpVerifier.ArpState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.HwArpVerifier.ArpState.<clinit>():void");
        }
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
        private boolean mArpRunning;
        private ArpState mArpState;
        private boolean mArpSuccLeastOnce;
        private boolean mLinkLayerLogRunning;
        private long mNetAccessibleDuration;
        private long mNetUnusableDuration;
        private int mNormalArpFail;
        private long mStartNetAccessibleTime;
        private long mStartNetUnusableTime;
        private int mStaticIpStatus;
        private int mTrafficStatsPollToken;
        final /* synthetic */ HwArpVerifier this$0;

        private class DetectWebThread extends Thread {
            protected WebParam mWP;
            final /* synthetic */ ClientHandler this$1;

            protected DetectWebThread(ClientHandler this$1, WebParam wp) {
                this.this$1 = this$1;
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
                        Log.d(HwArpVerifier.TAG, "start accessWeb, web = " + url);
                        int ret = this.this$1.accessWeb(url);
                        this.this$1.this$0.updatePortalStatus(ret);
                        this.mWP.setRespCode(ret);
                        if (ret != HwArpVerifier.HTTP_ACCESS_TIMEOUT_RESP) {
                            Log.d(HwArpVerifier.TAG, "DetectWebThread, sendMessage MSG_PROBE_WEB_RET ,  resp : " + ret);
                            this.this$1.this$0.mIsFirstCheck = false;
                            this.this$1.sendMessage(this.this$1.obtainMessage(ClientHandler.MSG_PROBE_WEB_RET, ret, ClientHandler.STATIC_IP_UNKNOWN, this.this$1.this$0.mAccessWebStatus));
                        } else {
                            Log.d(HwArpVerifier.TAG, "DetectWebThread, timeout ,  resp : " + ret);
                        }
                        this.this$1.this$0.mHwLogUtils.firmwareLog(false);
                    }
                }
            }
        }

        private class WebCheckThread extends Thread {
            protected String mURL;
            final /* synthetic */ ClientHandler this$1;

            protected WebCheckThread(ClientHandler this$1, String url) {
                this.this$1 = this$1;
                this.mURL = url;
            }

            public void run() {
                if (this.this$1.accessWeb(this.mURL) != HwArpVerifier.HTTP_ACCESS_OK) {
                    return;
                }
                if (this.this$1.this$0.mAccessWebStatus.getChinazIp() != null && !this.this$1.this$0.mAccessWebStatus.getChinazIp().isEmpty()) {
                    return;
                }
                if ((this.this$1.this$0.mAccessWebStatus.getChinazAddr() == null || this.this$1.this$0.mAccessWebStatus.getChinazAddr().isEmpty()) && !LogManager.getInstance().isCommercialUser()) {
                    this.this$1.accessWeb(HwArpVerifier.WEB_CHINAZ_GETIP);
                }
            }
        }

        public ClientHandler(HwArpVerifier this$0, Looper looper) {
            this.this$0 = this$0;
            super(looper);
            this.mStaticIpStatus = STATIC_IP_UNKNOWN;
            this.mTrafficStatsPollToken = STATIC_IP_UNKNOWN;
            this.mNormalArpFail = STATIC_IP_UNKNOWN;
            this.mArpRunning = false;
            this.mArpState = ArpState.HEART_CHECK;
            this.mLinkLayerLogRunning = false;
            this.mArpSuccLeastOnce = false;
            this.mStartNetAccessibleTime = 0;
            this.mStartNetUnusableTime = 0;
            this.mNetAccessibleDuration = 0;
            this.mNetUnusableDuration = 0;
        }

        private void createAndSendMsgDelayed(int what, int arg1, int arg2, long delayMillis) {
            sendMessageDelayed(Message.obtain(this, what, arg1, arg2), delayMillis);
            Log.d(HwArpVerifier.TAG, "msg what=" + what + " arg1(token)=" + arg1 + " arg2(mode)=" + arg2 + " delay=" + delayMillis);
        }

        public void monitorWifiNetworkState() {
            if (this.this$0.isConnectedToWifi() && this.this$0.mCurrentWiFiState == STATIC_IP_UNUSED) {
                Log.d(HwArpVerifier.TAG, "dont handle monitorWifiNetworkState started becauseof running");
                return;
            }
            HwArpVerifier hwArpVerifier = this.this$0;
            hwArpVerifier.mCheckStateToken = hwArpVerifier.mCheckStateToken + STATIC_IP_UNUSED;
            this.this$0.mArpBlacklist.clear();
            this.this$0.mFirstDetect = HwArpVerifier.DBG;
            this.mTrafficStatsPollToken += STATIC_IP_UNUSED;
            this.mArpSuccLeastOnce = false;
            if (this.this$0.isConnectedToWifi()) {
                this.this$0.mCurrentWiFiState = STATIC_IP_UNUSED;
                if (this.this$0.isEnableChecker()) {
                    Log.d(HwArpVerifier.TAG, "monitorWifiNetworkState: started.");
                    this.mStaticIpStatus = STATIC_IP_UNKNOWN;
                    sendMessageDelayed(Message.obtain(this, TRAFFIC_STATS_POLL_START, this.mTrafficStatsPollToken, STATIC_IP_UNKNOWN), 2000);
                    this.this$0.updateDurationControlParamsIfNeed();
                    transmitState(ArpState.HEART_CHECK);
                    createAndSendMsgDelayed(MSG_CHECK_WIFI_STATE, this.this$0.mCheckStateToken, STATIC_IP_UNKNOWN, 2000);
                }
                this.this$0.startWifiRouteCheck();
                this.this$0.handleWiFiDnsStats(STATIC_IP_UNKNOWN);
            } else {
                Log.d(HwArpVerifier.TAG, "monitorWifiNetworkState: stopped.");
                this.this$0.mCurrentWiFiState = HwArpVerifier.WIFI_STATE_DISCONNECTED;
                this.mStaticIpStatus = STATIC_IP_UNKNOWN;
                transmitState(ArpState.DONT_CHECK);
                sendMessageDelayed(Message.obtain(this, TRAFFIC_STATS_POLL_STOP, this.mTrafficStatsPollToken, STATIC_IP_UNKNOWN), 1500);
                this.this$0.stopWifiRouteCheck();
                this.this$0.handleWiFiDnsStats(this.this$0.mLastNetworkId);
            }
        }

        private void doCheckWebSpeed() {
            if (this.this$0.webMonitor.getAppSuckTime() == STATIC_IP_USER) {
                new WebCheckThread(this, HwArpVerifier.WEB_BAIDU).start();
                this.this$0.mHWGatewayVerifier.getGateWayARPResponses();
                readSdioQuality(this.this$0.mAccessWebStatus);
            }
            this.this$0.webMonitor.checkWebSpeed(this.this$0.mNetstatManager.getRxBytes(), this.this$0.mNetstatManager.getTxBytes(), HwArpVerifier.mRSSI, this.this$0.rssi_summery, this, this.this$0.mAccessWebStatus);
        }

        private int getDNSFailedCntDiff(AccessWebStatus aws) {
            int dnsNewFailCnt = STATIC_IP_UNKNOWN;
            String strDNSFailedCnt = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
            try {
                dnsNewFailCnt = Integer.parseInt(SystemProperties.get(HwSelfCureUtils.DNS_MONITOR_FLAG, "0"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            int dnsDiff = dnsNewFailCnt - aws.getOldDnsFailedCnt();
            Log.d(HwArpVerifier.TAG, "getDNSFailedCntDiff  dnsNewFailCnt =  " + dnsNewFailCnt + ", aws.getOldDnsFailedCnt() = " + aws.getOldDnsFailedCnt() + ", dnsDiff = " + dnsDiff);
            aws.setOldDnsFailedCnt(dnsNewFailCnt);
            aws.setDNSFailed(dnsDiff);
            return dnsDiff;
        }

        private void doCheckAccessInternet() {
            Log.d(HwArpVerifier.TAG, "doCheckAccessInternet");
            if (this.this$0.isConnectedToWifi()) {
                if (this.this$0.mCurrentWiFiState == STATIC_IP_UNUSED) {
                    HwArpVerifier hwArpVerifier = this.this$0;
                    hwArpVerifier.mlDetectWebCounter = hwArpVerifier.mlDetectWebCounter + 1;
                    WebParam wp = this.this$0.mAccessWebStatus.getWebParam();
                    if (wp != null) {
                        int respCode = wp.getRespCode();
                        if (HwArpVerifier.HTTP_ACCESS_TIMEOUT_RESP == respCode) {
                            long diff = SystemClock.elapsedRealtime() - wp.getStartTime();
                            if (diff > HwArpVerifier.ACCESS_BAIDU_TIMEOUT) {
                                Log.d(HwArpVerifier.TAG, "trigger CHR exception report because access baidu timeout : " + (diff / 1000) + "s");
                                this.this$0.mAccessWebStatus.setWebParam(null);
                                sendMessage(obtainMessage(MSG_PROBE_WEB_RET, respCode, STATIC_IP_UNKNOWN, this.this$0.mAccessWebStatus));
                            } else {
                                Log.d(HwArpVerifier.TAG, "doCheckAccessInternet return because wait access baidu wait is : " + (diff / 1000) + "s");
                            }
                            return;
                        }
                    }
                    this.this$0.mAccessWebStatus.setWebParam(null);
                    this.this$0.webMonitor.checkAccessWebStatus(this.this$0.mAccessWebStatus);
                    long lastTime = this.this$0.mAccessWebStatus.getPeriodTime();
                    if (0 != lastTime && SystemClock.elapsedRealtime() - lastTime > HwArpVerifier.SLEEP_PERIOD_TIMEOUT) {
                        this.this$0.mAccessWebStatus.setRxSleepCnt(this.this$0.mAccessWebStatus.getRxCnt() > 0 ? STATIC_IP_UNUSED : STATIC_IP_UNKNOWN);
                        Log.d(HwArpVerifier.TAG, "mAccessWebStatus : mRxSleepCnt = " + this.this$0.mAccessWebStatus.getRxSleepCnt());
                    }
                    this.this$0.mAccessWebStatus.setPeriodTime(SystemClock.elapsedRealtime());
                    Log.d(HwArpVerifier.TAG, "mAccessWebStatus : rx = " + this.this$0.mAccessWebStatus.getRxCnt() + ", tx = " + this.this$0.mAccessWebStatus.getTxCnt());
                    if (this.this$0.mAccessWebStatus.getRxCnt() > 0) {
                        this.this$0.mAccessWebStatus.reset();
                        this.this$0.mAccessWebStatus.setDetectWebStatus(STATIC_IP_USER);
                        getNetStatusHoldTimes();
                        return;
                    }
                    if (this.this$0.mAccessWebStatus.getTxCnt() >= STATIC_IP_USER) {
                        this.this$0.mAccessWebStatus.setNetDisableCnt(this.this$0.mAccessWebStatus.getNetDisableCnt() + STATIC_IP_UNUSED);
                    } else {
                        if (getDNSFailedCntDiff(this.this$0.mAccessWebStatus) > 0) {
                            this.this$0.mAccessWebStatus.setNetDisableCnt(this.this$0.mAccessWebStatus.getNetDisableCnt() + STATIC_IP_UNUSED);
                        } else {
                            getNetStatusHoldTimes();
                            return;
                        }
                    }
                    Log.d(HwArpVerifier.TAG, "mAccessWebStatus : getNetDisableCnt = " + this.this$0.mAccessWebStatus.getNetDisableCnt() + ", rx = " + this.this$0.mAccessWebStatus.getRxCnt() + ", tx = " + this.this$0.mAccessWebStatus.getTxCnt());
                    if (this.this$0.mAccessWebStatus.getNetDisableCnt() == STATIC_IP_UNUSED) {
                        this.this$0.webMonitor.getPktcnt().fetchPktcntNative();
                        HwCHRWifiLinkMonitor.getDefault().runCounters();
                    } else {
                        if (this.this$0.mAccessWebStatus.getNetDisableCnt() > STATIC_IP_UNUSED) {
                            WebParam webParam = new WebParam(HwArpVerifier.WEB_BAIDU, SystemClock.elapsedRealtime(), HwArpVerifier.HTTP_ACCESS_TIMEOUT_RESP);
                            DetectWebThread dwt = new DetectWebThread(this, webParam);
                            this.this$0.mAccessWebStatus.setWebParam(webParam);
                            Log.d(HwArpVerifier.TAG, "mAccessWebStatus : reset mNetDisableCnt = 0 , and start thread to access baidu = http://www.baidu.com");
                            this.this$0.mAccessWebStatus.setNetDisableCnt(STATIC_IP_UNKNOWN);
                            this.this$0.mHwLogUtils.firmwareLog(HwArpVerifier.DBG);
                            dwt.start();
                        }
                    }
                    return;
                }
            }
            Log.d(HwArpVerifier.TAG, "doCheckAccessInternet return because not wifi connected");
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

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected int accessWeb(String dlUrl) {
            HttpURLConnection httpURLConnection = null;
            InputStream inputStream = null;
            int respCode = HwArpVerifier.HTTP_ACCESS_TIMEOUT_RESP;
            try {
                URLConnection conn = new URL(dlUrl).openConnection();
                if (!(conn instanceof HttpURLConnection)) {
                    return HwArpVerifier.HTTP_ACCESS_TIMEOUT_RESP;
                }
                long lStart = SystemClock.elapsedRealtime();
                httpURLConnection = (HttpURLConnection) conn;
                setConnectionProperty(httpURLConnection);
                inputStream = httpURLConnection.getInputStream();
                respCode = httpURLConnection.getResponseCode();
                Log.d(HwArpVerifier.TAG, "accessWeb, respCode = " + respCode + ", url=" + dlUrl);
                if (dlUrl.equals(HwArpVerifier.WEB_CHINAZ_GETIP) && respCode == HwArpVerifier.HTTP_ACCESS_OK) {
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
                    this.this$0.mAccessWebStatus.setChinazIp(strIP);
                    this.this$0.mAccessWebStatus.setChinazAddr(strAddr);
                    Log.d(HwArpVerifier.TAG, "accessWeb, ip = " + strIP + ", addr = " + strAddr);
                } else {
                    if (dlUrl.equals(HwArpVerifier.WEB_BAIDU)) {
                        int diff = (int) (SystemClock.elapsedRealtime() - lStart);
                        this.this$0.mAccessWebStatus.setRTTBaidu(diff);
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e3) {
                        Log.d(HwArpVerifier.TAG, "exception of close, msg = " + e3.getMessage());
                    }
                }
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                return respCode;
            } catch (IOException e32) {
                Log.d(HwArpVerifier.TAG, "IOException, msg = " + e32.getMessage());
                String msg = e32.getMessage();
                Log.d(HwArpVerifier.TAG, "accessWeb, IOException, msg = " + msg);
                if (dlUrl != null) {
                    if (dlUrl.equals(HwArpVerifier.WEB_BAIDU) && msg != null) {
                        if (!msg.contains("ECONNREFUSED")) {
                        }
                        respCode = HwArpVerifier.HTTP_ACCESS_OK;
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e322) {
                        Log.d(HwArpVerifier.TAG, "exception of close, msg = " + e322.getMessage());
                    }
                }
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e3222) {
                        Log.d(HwArpVerifier.TAG, "exception of close, msg = " + e3222.getMessage());
                    }
                }
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
        }

        private String readWebBody(InputStream ins) {
            String strBody = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
            int readBytes = STATIC_IP_UNKNOWN;
            int totalBytes = STATIC_IP_UNKNOWN;
            byte[] buffer = new byte[512];
            if (ins == null) {
                return strBody;
            }
            while (totalBytes < 512) {
                Arrays.fill(buffer, (byte) 0);
                try {
                    readBytes = ins.read(buffer, STATIC_IP_UNKNOWN, buffer.length);
                } catch (IOException e) {
                    Log.d(HwArpVerifier.TAG, "route_cmd instream, IOException");
                    e.printStackTrace();
                }
                if (readBytes <= 0) {
                    break;
                }
                totalBytes += readBytes;
                if (totalBytes < 512) {
                    strBody = strBody + new String(buffer, Charset.defaultCharset()).trim();
                }
                Log.d(HwArpVerifier.TAG, "route_cmd:" + strBody);
            }
            return strBody;
        }

        private String ipRouteCmd() {
            String cmd = "ip route show table all";
            byte[] bufByte = new byte[HwCHRWifiSpeedBaseChecker.RTT_THRESHOLD_400];
            String strRet = null;
            int readBytes = STATIC_IP_UNKNOWN;
            int totalBytes = STATIC_IP_UNKNOWN;
            Process proc = null;
            try {
                proc = Runtime.getRuntime().exec(cmd);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if (proc == null) {
                return null;
            }
            InputStream ins = proc.getInputStream();
            if (ins != null) {
                while (totalBytes < HwCHRWifiSpeedBaseChecker.RTT_THRESHOLD_400) {
                    Arrays.fill(bufByte, (byte) 0);
                    try {
                        readBytes = ins.read(bufByte, STATIC_IP_UNKNOWN, bufByte.length);
                    } catch (IOException e) {
                        Log.d(HwArpVerifier.TAG, "route_cmd instream, IOException");
                        e.printStackTrace();
                    }
                    if (readBytes > 0) {
                        totalBytes += readBytes;
                        if (totalBytes <= HwCHRWifiSpeedBaseChecker.RTT_THRESHOLD_400) {
                            strRet = strRet + new String(bufByte, Charset.defaultCharset());
                        }
                        if (strRet == null || strRet.isEmpty()) {
                            Log.d(HwArpVerifier.TAG, "route_cmd is null or empty");
                        } else {
                            strRet = strRet.trim();
                            Log.d(HwArpVerifier.TAG, "route_cmd");
                        }
                    }
                }
                try {
                    break;
                    ins.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            InputStream eins = proc.getErrorStream();
            if (eins != null) {
                while (true) {
                    Arrays.fill(bufByte, (byte) 0);
                    try {
                        readBytes = eins.read(bufByte, STATIC_IP_UNKNOWN, bufByte.length);
                    } catch (IOException e22) {
                        Log.d(HwArpVerifier.TAG, "route_cmd errorInstream, IOException ");
                        e22.printStackTrace();
                    }
                    if (readBytes <= 0) {
                        try {
                            break;
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    } else {
                        totalBytes += readBytes;
                        Log.d(HwArpVerifier.TAG, new String(bufByte, Charset.defaultCharset()));
                    }
                }
                eins.close();
            }
            try {
                proc.waitFor();
            } catch (InterruptedException e12) {
                e12.printStackTrace();
            }
            return strRet;
        }

        private boolean isBackgroundRunning(String processName) {
            ActivityManager activityManager = (ActivityManager) this.this$0.mContext.getSystemService("activity");
            KeyguardManager keyguardManager = (KeyguardManager) this.this$0.mContext.getSystemService("keyguard");
            if (activityManager == null) {
                return false;
            }
            List<RunningAppProcessInfo> processList = activityManager.getRunningAppProcesses();
            if (processList == null) {
                return false;
            }
            for (RunningAppProcessInfo process : processList) {
                if (process.processName.startsWith(processName)) {
                    boolean isBackground = process.importance != HwArpVerifier.DEFAULT_ARP_PING_TIMEOUT_MS ? process.importance != HwArpVerifier.HTTP_ACCESS_OK ? HwArpVerifier.DBG : false : false;
                    boolean isLockedState = keyguardManager.inKeyguardRestrictedInputMode();
                    if (isBackground || isLockedState) {
                        return HwArpVerifier.DBG;
                    }
                    return false;
                }
            }
            return false;
        }

        private String getAllDisableWlanAppsFromDB() {
            String strRet = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
            Cursor cur = this.this$0.mContext.getContentResolver().query(HwArpVerifier.URI_NETAPP, null, null, null, null);
            if (isNullOrEmptyCursor(cur, HwArpVerifier.DBG)) {
                return null;
            }
            int INDEX_PKGNAME = cur.getColumnIndex(HwArpVerifier.APP_PACKAGE_NAME);
            int INDEX_UID = cur.getColumnIndex(HwArpVerifier.APP_UID);
            int INDEX_PERMISSION = cur.getColumnIndex(HwArpVerifier.PERMISSION_CFG);
            int iCount = STATIC_IP_UNKNOWN;
            while (cur.moveToNext()) {
                String pkg = cur.getString(INDEX_PKGNAME);
                int uid = cur.getInt(INDEX_UID);
                int permission = cur.getInt(INDEX_PERMISSION);
                if ((HwArpVerifier.WIFI_DISABLE1 == permission || HwArpVerifier.WIFI_DISABLE2 == permission) && pkg != null && !pkg.isEmpty() && isBackgroundRunning(pkg)) {
                    Log.i(HwArpVerifier.TAG, "getAllDisableWlanAppsFromDB added pkg = " + pkg + " uid = " + uid + " permission = " + permission);
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
                    if (strRet.length() > MSG_CHECK_WIFI_STATE) {
                        strRet = strRet.substring(STATIC_IP_UNKNOWN, MSG_CHECK_WIFI_STATE);
                        break;
                    }
                    iCount += STATIC_IP_UNUSED;
                    if (iCount >= THRESHOLD_NORMAL_CHECK_FAIL) {
                        break;
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
                return HwArpVerifier.DBG;
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
            return HwArpVerifier.DBG;
        }

        public void readSdioQuality(AccessWebStatus aws) {
            FileNotFoundException e;
            IOException e2;
            Throwable th;
            FileInputStream fileInputStream = null;
            BufferedReader bufferedReader = null;
            try {
                FileInputStream f = new FileInputStream(HwArpVerifier.SDIO_DEBUG_FILENAME);
                try {
                    BufferedReader dr = new BufferedReader(new InputStreamReader(f, Charset.defaultCharset()));
                    try {
                        for (String line = dr.readLine(); line != null; line = dr.readLine()) {
                            line = line.trim();
                            if (!line.equals(HwArpVerifier.BCM_ROAMING_FLAG_FILE)) {
                                String[] arr = line.split("[ \t]+");
                                if (arr.length > 0) {
                                    String tmp = arr[STATIC_IP_UNKNOWN];
                                    int v1;
                                    int v2;
                                    if (tmp.equals(HwArpVerifier.CMD52)) {
                                        if (arr.length >= STATIC_IP_OPTIMIZE) {
                                            v1 = Integer.parseInt(arr[STATIC_IP_USER]);
                                            v2 = Integer.parseInt(arr[3]);
                                            if (arr[STATIC_IP_UNUSED].equals(HwArpVerifier.CMD_READ)) {
                                                aws.set_sdio_info_readbreq(v1);
                                                aws.set_sdio_info_readb(v2);
                                            } else if (arr[STATIC_IP_UNUSED].equals(HwArpVerifier.CMD_WRITE)) {
                                                aws.set_sdio_info_writebreq(v1);
                                                aws.set_sdio_info_writeb(v2);
                                            }
                                        } else {
                                            continue;
                                        }
                                    } else if (tmp.equals(HwArpVerifier.CMD53)) {
                                        if (arr.length >= STATIC_IP_OPTIMIZE) {
                                            v1 = Integer.parseInt(arr[STATIC_IP_USER]);
                                            v2 = Integer.parseInt(arr[3]);
                                            if (arr[STATIC_IP_UNUSED].equals(HwArpVerifier.CMD_READ)) {
                                                aws.set_sdio_info_readwreq(v1);
                                                aws.set_sdio_info_readw(v2);
                                            } else if (arr[STATIC_IP_UNUSED].equals(HwArpVerifier.CMD_WRITE)) {
                                                aws.set_sdio_info_writewreq(v1);
                                                aws.set_sdio_info_writew(v2);
                                            }
                                        } else {
                                            continue;
                                        }
                                    } else if (tmp.equals(HwArpVerifier.CMD_SLEEPCSR_SET)) {
                                        if (arr.length >= STATIC_IP_OPTIMIZE) {
                                            v1 = Integer.parseInt(arr[STATIC_IP_USER]);
                                            v2 = Integer.parseInt(arr[3]);
                                            aws.set_sdio_info_ksosetreq(v1);
                                            aws.set_sdio_info_ksosetretry(v2);
                                        }
                                    } else if (tmp.equals(HwArpVerifier.CMD_SLEEPCSR_CLR) && arr.length >= STATIC_IP_OPTIMIZE) {
                                        v1 = Integer.parseInt(arr[STATIC_IP_USER]);
                                        v2 = Integer.parseInt(arr[3]);
                                        aws.set_sdio_info_ksoclrreq(v1);
                                        aws.set_sdio_info_ksoclrretry(v2);
                                    }
                                } else {
                                    continue;
                                }
                            }
                        }
                        dr.close();
                        f.close();
                        if (dr != null) {
                            try {
                                dr.close();
                            } catch (Exception e3) {
                                Log.d(HwArpVerifier.TAG, "readSdioQuality exception 3" + e3);
                            }
                        }
                        if (f != null) {
                            f.close();
                        }
                    } catch (FileNotFoundException e4) {
                        e = e4;
                        bufferedReader = dr;
                        fileInputStream = f;
                    } catch (IOException e5) {
                        e2 = e5;
                        bufferedReader = dr;
                        fileInputStream = f;
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedReader = dr;
                        fileInputStream = f;
                    }
                } catch (FileNotFoundException e6) {
                    e = e6;
                    fileInputStream = f;
                    try {
                        Log.d(HwArpVerifier.TAG, "readSdioQuality exception 1" + e);
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (Exception e32) {
                                Log.d(HwArpVerifier.TAG, "readSdioQuality exception 3" + e32);
                            }
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        Log.d(HwArpVerifier.TAG, "readSdioQuality : mSdio_info_readbreq = " + aws.get_sdio_info_readbreq() + ", mSdio_info_readb = " + aws.get_sdio_info_readb() + ", mSdio_info_writebreq = " + aws.get_sdio_info_writebreq() + ", mSdio_info_writeb = " + aws.get_sdio_info_writeb() + ", mSdio_info_readwreq = " + aws.get_sdio_info_readwreq() + ", mSdio_info_readw = " + aws.get_sdio_info_readw() + ", mSdio_info_writewreq = " + aws.get_sdio_info_writewreq() + ", mSdio_info_writew = " + aws.get_sdio_info_writew() + ", mSdio_info_ksosetreq = " + aws.get_sdio_info_ksosetreq() + ", mSdio_info_ksosetretry = " + aws.get_sdio_info_ksosetretry() + ", mSdio_info_ksoclrreq = " + aws.get_sdio_info_ksoclrreq() + ", mSdio_info_ksoclrretry = " + aws.get_sdio_info_ksoclrretry());
                    } catch (Throwable th3) {
                        th = th3;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (Exception e322) {
                                Log.d(HwArpVerifier.TAG, "readSdioQuality exception 3" + e322);
                                throw th;
                            }
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        throw th;
                    }
                } catch (IOException e7) {
                    e2 = e7;
                    fileInputStream = f;
                    Log.d(HwArpVerifier.TAG, "readSdioQuality exception 2" + e2);
                    e2.printStackTrace();
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Exception e3222) {
                            Log.d(HwArpVerifier.TAG, "readSdioQuality exception 3" + e3222);
                        }
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    Log.d(HwArpVerifier.TAG, "readSdioQuality : mSdio_info_readbreq = " + aws.get_sdio_info_readbreq() + ", mSdio_info_readb = " + aws.get_sdio_info_readb() + ", mSdio_info_writebreq = " + aws.get_sdio_info_writebreq() + ", mSdio_info_writeb = " + aws.get_sdio_info_writeb() + ", mSdio_info_readwreq = " + aws.get_sdio_info_readwreq() + ", mSdio_info_readw = " + aws.get_sdio_info_readw() + ", mSdio_info_writewreq = " + aws.get_sdio_info_writewreq() + ", mSdio_info_writew = " + aws.get_sdio_info_writew() + ", mSdio_info_ksosetreq = " + aws.get_sdio_info_ksosetreq() + ", mSdio_info_ksosetretry = " + aws.get_sdio_info_ksosetretry() + ", mSdio_info_ksoclrreq = " + aws.get_sdio_info_ksoclrreq() + ", mSdio_info_ksoclrretry = " + aws.get_sdio_info_ksoclrretry());
                } catch (Throwable th4) {
                    th = th4;
                    fileInputStream = f;
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e8) {
                e = e8;
                Log.d(HwArpVerifier.TAG, "readSdioQuality exception 1" + e);
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                Log.d(HwArpVerifier.TAG, "readSdioQuality : mSdio_info_readbreq = " + aws.get_sdio_info_readbreq() + ", mSdio_info_readb = " + aws.get_sdio_info_readb() + ", mSdio_info_writebreq = " + aws.get_sdio_info_writebreq() + ", mSdio_info_writeb = " + aws.get_sdio_info_writeb() + ", mSdio_info_readwreq = " + aws.get_sdio_info_readwreq() + ", mSdio_info_readw = " + aws.get_sdio_info_readw() + ", mSdio_info_writewreq = " + aws.get_sdio_info_writewreq() + ", mSdio_info_writew = " + aws.get_sdio_info_writew() + ", mSdio_info_ksosetreq = " + aws.get_sdio_info_ksosetreq() + ", mSdio_info_ksosetretry = " + aws.get_sdio_info_ksosetretry() + ", mSdio_info_ksoclrreq = " + aws.get_sdio_info_ksoclrreq() + ", mSdio_info_ksoclrretry = " + aws.get_sdio_info_ksoclrretry());
            } catch (IOException e9) {
                e2 = e9;
                Log.d(HwArpVerifier.TAG, "readSdioQuality exception 2" + e2);
                e2.printStackTrace();
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                Log.d(HwArpVerifier.TAG, "readSdioQuality : mSdio_info_readbreq = " + aws.get_sdio_info_readbreq() + ", mSdio_info_readb = " + aws.get_sdio_info_readb() + ", mSdio_info_writebreq = " + aws.get_sdio_info_writebreq() + ", mSdio_info_writeb = " + aws.get_sdio_info_writeb() + ", mSdio_info_readwreq = " + aws.get_sdio_info_readwreq() + ", mSdio_info_readw = " + aws.get_sdio_info_readw() + ", mSdio_info_writewreq = " + aws.get_sdio_info_writewreq() + ", mSdio_info_writew = " + aws.get_sdio_info_writew() + ", mSdio_info_ksosetreq = " + aws.get_sdio_info_ksosetreq() + ", mSdio_info_ksosetretry = " + aws.get_sdio_info_ksosetretry() + ", mSdio_info_ksoclrreq = " + aws.get_sdio_info_ksoclrreq() + ", mSdio_info_ksoclrretry = " + aws.get_sdio_info_ksoclrretry());
            }
            Log.d(HwArpVerifier.TAG, "readSdioQuality : mSdio_info_readbreq = " + aws.get_sdio_info_readbreq() + ", mSdio_info_readb = " + aws.get_sdio_info_readb() + ", mSdio_info_writebreq = " + aws.get_sdio_info_writebreq() + ", mSdio_info_writeb = " + aws.get_sdio_info_writeb() + ", mSdio_info_readwreq = " + aws.get_sdio_info_readwreq() + ", mSdio_info_readw = " + aws.get_sdio_info_readw() + ", mSdio_info_writewreq = " + aws.get_sdio_info_writewreq() + ", mSdio_info_writew = " + aws.get_sdio_info_writew() + ", mSdio_info_ksosetreq = " + aws.get_sdio_info_ksosetreq() + ", mSdio_info_ksosetretry = " + aws.get_sdio_info_ksosetretry() + ", mSdio_info_ksoclrreq = " + aws.get_sdio_info_ksoclrreq() + ", mSdio_info_ksoclrretry = " + aws.get_sdio_info_ksoclrretry());
        }

        public void readHisiChipsetDebugInfo(AccessWebStatus aws) {
            FileNotFoundException e;
            IOException e2;
            Throwable th;
            FileInputStream fileInputStream = null;
            BufferedReader bufferedReader = null;
            String chipType = SystemProperties.get("ro.connectivity.chiptype", HwArpVerifier.BCM_ROAMING_FLAG_FILE);
            if (chipType == null || chipType.isEmpty() || chipType.equalsIgnoreCase("hi110x") || chipType.equalsIgnoreCase("hisi")) {
                aws.setMonitor_interval(TIME_POLL_TRAFFIC_STATS_INTERVAL);
                try {
                    FileInputStream f = new FileInputStream(HwArpVerifier.HISI_CHIPSET_DEBUG_FILE);
                    try {
                        BufferedReader dr = new BufferedReader(new InputStreamReader(f, Charset.defaultCharset()));
                        try {
                            for (String line = dr.readLine(); line != null; line = dr.readLine()) {
                                line = line.trim();
                                if (!line.equals(HwArpVerifier.BCM_ROAMING_FLAG_FILE)) {
                                    String[] arr = line.split("[ \t:]+");
                                    if (arr.length >= STATIC_IP_USER) {
                                        String tmp = arr[STATIC_IP_UNKNOWN];
                                        int value;
                                        if (tmp.equals(HwArpVerifier.HISI_TX_FRAME_AMOUNT)) {
                                            value = Integer.parseInt(arr[STATIC_IP_UNUSED]);
                                            aws.setPrevTx_frame_amount(aws.getTx_frame_amount());
                                            aws.setTx_frame_amount(value);
                                        } else if (tmp.equals(HwArpVerifier.HISI_TX_BYTE_AMOUNT)) {
                                            value = Integer.parseInt(arr[STATIC_IP_UNUSED]);
                                            aws.setPrevTx_byte_amount(aws.getTx_byte_amount());
                                            aws.setTx_byte_amount(value);
                                        } else if (tmp.equals(HwArpVerifier.HISI_TX_DATA_FRAME_ERROR_AMOUNT)) {
                                            value = Integer.parseInt(arr[STATIC_IP_UNUSED]);
                                            aws.setPrevTx_data_frame_error_amount(aws.getTx_data_frame_error_amount());
                                            aws.setTx_data_frame_error_amount(value);
                                        } else if (tmp.equals(HwArpVerifier.HISI_TX_RETRANS_AMOUNT)) {
                                            value = Integer.parseInt(arr[STATIC_IP_UNUSED]);
                                            aws.setPrevTx_retrans_amount(aws.getTx_retrans_amount());
                                            aws.setTx_retrans_amount(value);
                                        } else if (tmp.equals(HwArpVerifier.HISI_RX_FRAME_AMOUNT)) {
                                            value = Integer.parseInt(arr[STATIC_IP_UNUSED]);
                                            aws.setPrevRx_frame_amount(aws.getRx_frame_amount());
                                            aws.setRx_frame_amount(value);
                                        } else if (tmp.equals(HwArpVerifier.HISI_RX_BYTE_AMOUNT)) {
                                            value = Integer.parseInt(arr[STATIC_IP_UNUSED]);
                                            aws.setPrevRx_byte_amount(aws.getRx_byte_amount());
                                            aws.setRx_byte_amount(value);
                                        } else if (tmp.equals(HwArpVerifier.HISI_RX_BEACON_FROM_ASSOC_AP)) {
                                            value = Integer.parseInt(arr[STATIC_IP_UNUSED]);
                                            aws.setPrevRx_beacon_from_assoc_ap(aws.getRx_beacon_from_assoc_ap());
                                            aws.setRx_beacon_from_assoc_ap(value);
                                        } else if (tmp.equals(HwArpVerifier.HISI_AP_DISTANCE)) {
                                            value = Integer.parseInt(arr[STATIC_IP_UNUSED]);
                                            aws.setPrevAp_distance(aws.getAp_distance());
                                            aws.setAp_distance(value);
                                        } else if (tmp.equals(HwArpVerifier.HISI_DISTURBING_DEGREE)) {
                                            value = Integer.parseInt(arr[STATIC_IP_UNUSED]);
                                            aws.setPrevDisturbing_degree(aws.getDisturbing_degree());
                                            aws.setDisturbing_degree(value);
                                        } else if (tmp.equals(HwArpVerifier.HISI_LOST_BEACON_AMOUNT)) {
                                            value = Integer.parseInt(arr[STATIC_IP_UNUSED]);
                                            aws.setPrevLost_beacon_amount(aws.getLost_beacon_amount());
                                            aws.setLost_beacon_amount(value);
                                        }
                                    }
                                }
                            }
                            dr.close();
                            f.close();
                            if (dr != null) {
                                try {
                                    dr.close();
                                } catch (Exception e3) {
                                    Log.d(HwArpVerifier.TAG, "readHisiChipsetDebugInfo exception 3" + e3);
                                }
                            }
                            if (f != null) {
                                f.close();
                            }
                        } catch (FileNotFoundException e4) {
                            e = e4;
                            bufferedReader = dr;
                            fileInputStream = f;
                        } catch (IOException e5) {
                            e2 = e5;
                            bufferedReader = dr;
                            fileInputStream = f;
                        } catch (Throwable th2) {
                            th = th2;
                            bufferedReader = dr;
                            fileInputStream = f;
                        }
                    } catch (FileNotFoundException e6) {
                        e = e6;
                        fileInputStream = f;
                        try {
                            Log.d(HwArpVerifier.TAG, "readHisiChipsetDebugInfo exception 1" + e);
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (Exception e32) {
                                    Log.d(HwArpVerifier.TAG, "readHisiChipsetDebugInfo exception 3" + e32);
                                }
                            }
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            Log.d(HwArpVerifier.TAG, "readHisiChipsetDebugInfo prev, mMonitor_interval = " + aws.getMonitor_interval() + ", mPrevTx_frame_amount = " + aws.getPrevTx_frame_amount() + ", mPrevTx_byte_amount = " + aws.getPrevTx_byte_amount() + ", mPrevTx_data_frame_error_amount = " + aws.getPrevTx_data_frame_error_amount() + ", mPrevTx_retrans_amount = " + aws.getPrevTx_retrans_amount() + ", mPrevRx_frame_amount = " + aws.getPrevRx_frame_amount() + ", mPrevRx_byte_amount = " + aws.getPrevRx_byte_amount() + ", mPrevRx_beacon_from_assoc_ap = " + aws.getPrevRx_beacon_from_assoc_ap() + ", mPrevAp_distance = " + aws.getPrevAp_distance() + ", mPrevDisturbing_degree = " + aws.getPrevDisturbing_degree() + ", mPrevLost_beacon_amount = " + aws.getPrevLost_beacon_amount());
                            Log.d(HwArpVerifier.TAG, "readHisiChipsetDebugInfo : , mMonitor_interval = " + aws.getMonitor_interval() + " , mTx_frame_amount = " + aws.getTx_frame_amount() + " , mTx_byte_amount = " + aws.getTx_byte_amount() + " , mTx_data_frame_error_amount = " + aws.getTx_data_frame_error_amount() + " , mTx_retrans_amount = " + aws.getTx_retrans_amount() + " , mRx_frame_amount = " + aws.getRx_frame_amount() + " , mRx_byte_amount =" + aws.getRx_byte_amount() + " , mRx_beacon_from_assoc_ap = " + aws.getRx_beacon_from_assoc_ap() + " , mAp_distance = " + aws.getAp_distance() + " , mDisturbing_degree = " + aws.getDisturbing_degree() + " , mLost_beacon_amount = " + aws.getLost_beacon_amount());
                        } catch (Throwable th3) {
                            th = th3;
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (Exception e322) {
                                    Log.d(HwArpVerifier.TAG, "readHisiChipsetDebugInfo exception 3" + e322);
                                    throw th;
                                }
                            }
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            throw th;
                        }
                    } catch (IOException e7) {
                        e2 = e7;
                        fileInputStream = f;
                        Log.d(HwArpVerifier.TAG, "readHisiChipsetDebugInfo exception 2" + e2);
                        e2.printStackTrace();
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (Exception e3222) {
                                Log.d(HwArpVerifier.TAG, "readHisiChipsetDebugInfo exception 3" + e3222);
                            }
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        Log.d(HwArpVerifier.TAG, "readHisiChipsetDebugInfo prev, mMonitor_interval = " + aws.getMonitor_interval() + ", mPrevTx_frame_amount = " + aws.getPrevTx_frame_amount() + ", mPrevTx_byte_amount = " + aws.getPrevTx_byte_amount() + ", mPrevTx_data_frame_error_amount = " + aws.getPrevTx_data_frame_error_amount() + ", mPrevTx_retrans_amount = " + aws.getPrevTx_retrans_amount() + ", mPrevRx_frame_amount = " + aws.getPrevRx_frame_amount() + ", mPrevRx_byte_amount = " + aws.getPrevRx_byte_amount() + ", mPrevRx_beacon_from_assoc_ap = " + aws.getPrevRx_beacon_from_assoc_ap() + ", mPrevAp_distance = " + aws.getPrevAp_distance() + ", mPrevDisturbing_degree = " + aws.getPrevDisturbing_degree() + ", mPrevLost_beacon_amount = " + aws.getPrevLost_beacon_amount());
                        Log.d(HwArpVerifier.TAG, "readHisiChipsetDebugInfo : , mMonitor_interval = " + aws.getMonitor_interval() + " , mTx_frame_amount = " + aws.getTx_frame_amount() + " , mTx_byte_amount = " + aws.getTx_byte_amount() + " , mTx_data_frame_error_amount = " + aws.getTx_data_frame_error_amount() + " , mTx_retrans_amount = " + aws.getTx_retrans_amount() + " , mRx_frame_amount = " + aws.getRx_frame_amount() + " , mRx_byte_amount =" + aws.getRx_byte_amount() + " , mRx_beacon_from_assoc_ap = " + aws.getRx_beacon_from_assoc_ap() + " , mAp_distance = " + aws.getAp_distance() + " , mDisturbing_degree = " + aws.getDisturbing_degree() + " , mLost_beacon_amount = " + aws.getLost_beacon_amount());
                    } catch (Throwable th4) {
                        th = th4;
                        fileInputStream = f;
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e8) {
                    e = e8;
                    Log.d(HwArpVerifier.TAG, "readHisiChipsetDebugInfo exception 1" + e);
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    Log.d(HwArpVerifier.TAG, "readHisiChipsetDebugInfo prev, mMonitor_interval = " + aws.getMonitor_interval() + ", mPrevTx_frame_amount = " + aws.getPrevTx_frame_amount() + ", mPrevTx_byte_amount = " + aws.getPrevTx_byte_amount() + ", mPrevTx_data_frame_error_amount = " + aws.getPrevTx_data_frame_error_amount() + ", mPrevTx_retrans_amount = " + aws.getPrevTx_retrans_amount() + ", mPrevRx_frame_amount = " + aws.getPrevRx_frame_amount() + ", mPrevRx_byte_amount = " + aws.getPrevRx_byte_amount() + ", mPrevRx_beacon_from_assoc_ap = " + aws.getPrevRx_beacon_from_assoc_ap() + ", mPrevAp_distance = " + aws.getPrevAp_distance() + ", mPrevDisturbing_degree = " + aws.getPrevDisturbing_degree() + ", mPrevLost_beacon_amount = " + aws.getPrevLost_beacon_amount());
                    Log.d(HwArpVerifier.TAG, "readHisiChipsetDebugInfo : , mMonitor_interval = " + aws.getMonitor_interval() + " , mTx_frame_amount = " + aws.getTx_frame_amount() + " , mTx_byte_amount = " + aws.getTx_byte_amount() + " , mTx_data_frame_error_amount = " + aws.getTx_data_frame_error_amount() + " , mTx_retrans_amount = " + aws.getTx_retrans_amount() + " , mRx_frame_amount = " + aws.getRx_frame_amount() + " , mRx_byte_amount =" + aws.getRx_byte_amount() + " , mRx_beacon_from_assoc_ap = " + aws.getRx_beacon_from_assoc_ap() + " , mAp_distance = " + aws.getAp_distance() + " , mDisturbing_degree = " + aws.getDisturbing_degree() + " , mLost_beacon_amount = " + aws.getLost_beacon_amount());
                } catch (IOException e9) {
                    e2 = e9;
                    Log.d(HwArpVerifier.TAG, "readHisiChipsetDebugInfo exception 2" + e2);
                    e2.printStackTrace();
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    Log.d(HwArpVerifier.TAG, "readHisiChipsetDebugInfo prev, mMonitor_interval = " + aws.getMonitor_interval() + ", mPrevTx_frame_amount = " + aws.getPrevTx_frame_amount() + ", mPrevTx_byte_amount = " + aws.getPrevTx_byte_amount() + ", mPrevTx_data_frame_error_amount = " + aws.getPrevTx_data_frame_error_amount() + ", mPrevTx_retrans_amount = " + aws.getPrevTx_retrans_amount() + ", mPrevRx_frame_amount = " + aws.getPrevRx_frame_amount() + ", mPrevRx_byte_amount = " + aws.getPrevRx_byte_amount() + ", mPrevRx_beacon_from_assoc_ap = " + aws.getPrevRx_beacon_from_assoc_ap() + ", mPrevAp_distance = " + aws.getPrevAp_distance() + ", mPrevDisturbing_degree = " + aws.getPrevDisturbing_degree() + ", mPrevLost_beacon_amount = " + aws.getPrevLost_beacon_amount());
                    Log.d(HwArpVerifier.TAG, "readHisiChipsetDebugInfo : , mMonitor_interval = " + aws.getMonitor_interval() + " , mTx_frame_amount = " + aws.getTx_frame_amount() + " , mTx_byte_amount = " + aws.getTx_byte_amount() + " , mTx_data_frame_error_amount = " + aws.getTx_data_frame_error_amount() + " , mTx_retrans_amount = " + aws.getTx_retrans_amount() + " , mRx_frame_amount = " + aws.getRx_frame_amount() + " , mRx_byte_amount =" + aws.getRx_byte_amount() + " , mRx_beacon_from_assoc_ap = " + aws.getRx_beacon_from_assoc_ap() + " , mAp_distance = " + aws.getAp_distance() + " , mDisturbing_degree = " + aws.getDisturbing_degree() + " , mLost_beacon_amount = " + aws.getLost_beacon_amount());
                }
                Log.d(HwArpVerifier.TAG, "readHisiChipsetDebugInfo prev, mMonitor_interval = " + aws.getMonitor_interval() + ", mPrevTx_frame_amount = " + aws.getPrevTx_frame_amount() + ", mPrevTx_byte_amount = " + aws.getPrevTx_byte_amount() + ", mPrevTx_data_frame_error_amount = " + aws.getPrevTx_data_frame_error_amount() + ", mPrevTx_retrans_amount = " + aws.getPrevTx_retrans_amount() + ", mPrevRx_frame_amount = " + aws.getPrevRx_frame_amount() + ", mPrevRx_byte_amount = " + aws.getPrevRx_byte_amount() + ", mPrevRx_beacon_from_assoc_ap = " + aws.getPrevRx_beacon_from_assoc_ap() + ", mPrevAp_distance = " + aws.getPrevAp_distance() + ", mPrevDisturbing_degree = " + aws.getPrevDisturbing_degree() + ", mPrevLost_beacon_amount = " + aws.getPrevLost_beacon_amount());
                Log.d(HwArpVerifier.TAG, "readHisiChipsetDebugInfo : , mMonitor_interval = " + aws.getMonitor_interval() + " , mTx_frame_amount = " + aws.getTx_frame_amount() + " , mTx_byte_amount = " + aws.getTx_byte_amount() + " , mTx_data_frame_error_amount = " + aws.getTx_data_frame_error_amount() + " , mTx_retrans_amount = " + aws.getTx_retrans_amount() + " , mRx_frame_amount = " + aws.getRx_frame_amount() + " , mRx_byte_amount =" + aws.getRx_byte_amount() + " , mRx_beacon_from_assoc_ap = " + aws.getRx_beacon_from_assoc_ap() + " , mAp_distance = " + aws.getAp_distance() + " , mDisturbing_degree = " + aws.getDisturbing_degree() + " , mLost_beacon_amount = " + aws.getLost_beacon_amount());
            }
        }

        public boolean doArpTestAsync(int arpNum, int minResponse, int timeout) {
            Log.d(HwArpVerifier.TAG, "doArpTestAsync");
            if (this.mArpRunning) {
                return false;
            }
            sendMessage(obtainMessage(MSG_DO_ARP_ASYNC, new MsgItem(arpNum, minResponse, timeout)));
            return HwArpVerifier.DBG;
        }

        public void handleMessage(Message msg) {
            if (msg.what == MSG_CHECK_WIFI_STATE) {
                int token = msg.arg1;
                int mode = msg.arg2;
                if (msg.arg1 != this.this$0.mCheckStateToken) {
                    Log.w(HwArpVerifier.TAG, "ignore msg MSG_CHECK_WIFI_STATE, msg token = " + token + ", expected token = " + this.this$0.mCheckStateToken);
                    return;
                }
                checkWifiNetworkState(token, mode);
            } else if (msg.what == MSG_DO_ARP_ASYNC) {
                this.mArpRunning = HwArpVerifier.DBG;
                MsgItem msgItem = msg.obj;
                if (msgItem != null) {
                    boolean result = this.this$0.doArp(msgItem.arpNum, msgItem.minResponse, msgItem.timeout);
                    Log.d(HwArpVerifier.TAG, "MSG_DO_ARP_ASYNC:" + result);
                    if (!result) {
                        Intent intent = new Intent(HwArpVerifier.DIAGNOSE_COMPLETE_ACTION);
                        intent.putExtra("MSG_CODE", HwArpVerifier.MSG_WIFI_ARP_FAILED);
                        intent.putExtra("MaxTime", this.this$0.mSpendTime);
                        intent.putExtra("PackageName", HwArpVerifier.TAG);
                        this.this$0.mContext.sendBroadcast(intent);
                    }
                }
                this.mArpRunning = false;
            } else if (msg.what == TRAFFIC_STATS_POLL_START || msg.what == TRAFFIC_STATS_POLL) {
                if (msg.arg1 == this.mTrafficStatsPollToken) {
                    removeMessages(this.mTrafficStatsPollToken);
                    if (msg.what == TRAFFIC_STATS_POLL_START) {
                        HwWifiCHRNative.setTcpMonitorStat(STATIC_IP_UNUSED);
                        this.this$0.mNetstatManager.resetNetstats();
                    }
                    Log.d(HwArpVerifier.TAG, "performPollAndLog:");
                    this.this$0.mNetstatManager.performPollAndLog();
                    sendMessageDelayed(Message.obtain(this, TRAFFIC_STATS_POLL, this.mTrafficStatsPollToken, STATIC_IP_UNKNOWN), 5000);
                    doCheckWebSpeed();
                    doCheckAccessInternet();
                } else {
                    Log.d(HwArpVerifier.TAG, "ignore msg " + msg.what + ", current token = " + msg.arg1 + ", expected token = " + this.mTrafficStatsPollToken);
                }
            } else if (msg.what == TRAFFIC_STATS_POLL_STOP) {
                if (msg.arg1 == this.mTrafficStatsPollToken) {
                    Log.d(HwArpVerifier.TAG, "disconnected, trafficStats:");
                    this.this$0.mNetstatManager.resetNetstats();
                    HwWifiCHRNative.setTcpMonitorStat(STATIC_IP_UNKNOWN);
                    this.this$0.mlDetectWebCounter = 0;
                    this.this$0.mIsFirstCheck = HwArpVerifier.DBG;
                    removeMessages(TRAFFIC_STATS_POLL_START);
                    removeMessages(TRAFFIC_STATS_POLL);
                    this.this$0.mAccessWebStatus.reset();
                }
            } else if (msg.what == MSG_DO_ROUTE_CHECK) {
                if (this.this$0.isConnectedToWifi() && this.this$0.mRouteDetectCnt <= THRESHOLD_NORMAL_CHECK_FAIL) {
                    try {
                        if (this.this$0.isWifiDefaultRouteExist()) {
                            this.this$0.mRouteDetectCnt = STATIC_IP_UNKNOWN;
                            sendEmptyMessageDelayed(MSG_DO_ROUTE_CHECK, 50000);
                        } else {
                            HwArpVerifier hwArpVerifier = this.this$0;
                            hwArpVerifier.mRouteDetectCnt = hwArpVerifier.mRouteDetectCnt + STATIC_IP_UNUSED;
                            if (!this.this$0.isMobileDateActive && this.this$0.isRouteRepareSwitchEnabled) {
                                Log.e(HwArpVerifier.TAG, "try to reparier wifi default route");
                                this.this$0.wifiRepairRoute();
                            }
                            sendEmptyMessageDelayed(MSG_DO_ROUTE_CHECK, 15000);
                        }
                    } catch (Exception e) {
                        Log.e(HwArpVerifier.TAG, "exception in wifi route check: " + e);
                    }
                }
            } else if (MSG_PROBE_WEB_RET == msg.what) {
                Log.d(HwArpVerifier.TAG, "handleMessage MSG_PROBE_WEB_RET resp = " + msg.arg1);
                if (msg.arg1 < HwCHRWifiSpeedBaseChecker.RTT_THRESHOLD_400 && HwArpVerifier.HTTP_ACCESS_TIMEOUT_RESP != msg.arg1) {
                    this.this$0.mAccessWebStatus.setDetectWebStatus(STATIC_IP_USER);
                } else if (this.this$0.wcsm == null || msg.obj == null) {
                    Log.e(HwArpVerifier.TAG, "handleMessage MSG_PROBE_WEB_RET null == wcsm || null == msg.obj");
                    return;
                } else if (this.this$0.wcsm instanceof HwWifiCHRStateManagerImpl) {
                    HwWifiCHRStateManagerImpl hwcsmImpl = (HwWifiCHRStateManagerImpl) this.this$0.wcsm;
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
                        this.this$0.webMonitor.getPktcnt().fetchPktcntNative();
                        HwCHRWifiLinkMonitor.getDefault().runCounters();
                        this.this$0.mAccessWebStatus.setIPRouteRet(ipRouteCmd());
                        String strPkgs = getAllDisableWlanAppsFromDB();
                        this.this$0.mAccessWebStatus.setDisableWlanApps(strPkgs);
                        this.this$0.mHWGatewayVerifier.getGateWayARPResponses();
                        readSdioQuality(this.this$0.mAccessWebStatus);
                        Log.d(HwArpVerifier.TAG, "getAllDisableWlanAppsFromDB return : " + strPkgs + ", getDisableWlanAppsApp()" + this.this$0.mAccessWebStatus.getDisableWlanApps());
                        Log.d(HwArpVerifier.TAG, "accessWeb, mAccessWebStatus : " + this.this$0.mAccessWebStatus.toString());
                        Log.d(HwArpVerifier.TAG, "accessWeb, mAccessWebStatus  sdio : " + this.this$0.mAccessWebStatus.toSdioString());
                        Log.d(HwArpVerifier.TAG, "handleMessage MSG_PROBE_WEB_RET tx = " + this.this$0.mAccessWebStatus.getTxCnt());
                        this.this$0.mAccessWebStatus.setAccessNetFailedCount(this.this$0.mAccessWebStatus.getAccessNetFailedCount() + STATIC_IP_UNUSED);
                        readHisiChipsetDebugInfo(this.this$0.mAccessWebStatus);
                        if (this.this$0.mIsFirstCheck) {
                            this.this$0.mIsFirstCheck = false;
                            this.this$0.mAccessWebStatus.setDetectWebStatus(STATIC_IP_UNUSED);
                            hwcsmImpl.updateAccessWebException(87, "FIRST_CONNECT_INTERNET_FAILED", aws);
                        } else if (this.this$0.mAccessWebStatus.getTxCnt() >= STATIC_IP_USER) {
                            hwcsmImpl.updateAccessWebException(87, "ONLY_THE_TX_NO_RX", aws);
                        } else if (this.this$0.mAccessWebStatus.getDNSFailed() > 0) {
                            hwcsmImpl.updateAccessWebException(87, "DNS_PARSE_FAILED", aws);
                        } else {
                            hwcsmImpl.updateAccessWebException(87, "OTHER", aws);
                        }
                    } else {
                        Log.e(HwArpVerifier.TAG, "handleMessage MSG_PROBE_WEB_RET aws instanceof AccessWebStatus error");
                        return;
                    }
                } else {
                    Log.e(HwArpVerifier.TAG, "handleMessage MSG_PROBE_WEB_RET hwcsmImpl instanceof HwWifiCHRStateManagerImpl error");
                    return;
                }
                this.this$0.mAccessWebStatus.reset();
            } else {
                this.this$0.mHWGatewayVerifier.handleMultiGatewayMessage(msg);
            }
        }

        private boolean isNeedCheck() {
            return (this.mArpState == ArpState.DONT_CHECK || this.mArpState == ArpState.DEAD_CHECK) ? false : HwArpVerifier.DBG;
        }

        private void transmitState(ArpState state) {
            Log.d(HwArpVerifier.TAG, "from " + strState(this.mArpState) + " transmit to state:" + strState(state));
            if (this.mArpState == ArpState.CONFIRM_CHECK && state == ArpState.HEART_CHECK && this.this$0.wcsm != null) {
                this.this$0.wcsm.updateAccessWebException(STATIC_IP_UNKNOWN, "ARP_REASSOC_OK");
            }
            this.mArpState = state;
            if (this.mArpState == ArpState.NORMAL_CHECK) {
                this.mNormalArpFail = STATIC_IP_UNKNOWN;
            } else if ((this.mArpState == ArpState.DONT_CHECK || this.mArpState == ArpState.HEART_CHECK) && this.mLinkLayerLogRunning) {
                this.this$0.mHwLogUtils.stopLinkLayerLog();
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
                index = HwArpVerifier.dynamicPings.length + HwArpVerifier.WIFI_STATE_DISCONNECTED;
            } else {
                index = this.mNormalArpFail;
            }
            Log.d(HwArpVerifier.TAG, "mNormalArpFail: " + this.mNormalArpFail + ", dynamicPings[" + index + "]:" + HwArpVerifier.dynamicPings[index]);
            return HwArpVerifier.dynamicPings[index];
        }

        private void doCheckWifiNetworkState(int token) {
            if (this.mArpState == ArpState.DONT_CHECK) {
                HwArpVerifier hwArpVerifier = this.this$0;
                hwArpVerifier.mCheckStateToken = hwArpVerifier.mCheckStateToken + STATIC_IP_UNUSED;
            } else if (this.mArpState == ArpState.HEART_CHECK) {
                createAndSendMsgDelayed(MSG_CHECK_WIFI_STATE, token, STATIC_IP_UNKNOWN, HwArpVerifier.SLEEP_PERIOD_TIMEOUT);
            } else if (this.mArpState == ArpState.NORMAL_CHECK) {
                createAndSendMsgDelayed(MSG_CHECK_WIFI_STATE, token, STATIC_IP_USER, 1000);
            } else if (this.mArpState == ArpState.CONFIRM_CHECK) {
                createAndSendMsgDelayed(MSG_CHECK_WIFI_STATE, token, STATIC_IP_UNUSED, 5000);
            } else if (this.mArpState == ArpState.DEAD_CHECK) {
                createAndSendMsgDelayed(MSG_CHECK_WIFI_STATE, token, STATIC_IP_UNKNOWN, 120000);
            }
        }

        private void handleCheckResult(int token, boolean result) {
            if (!(this.mArpState == ArpState.HEART_CHECK || this.mArpState == ArpState.NORMAL_CHECK)) {
                if (this.mArpState == ArpState.CONFIRM_CHECK) {
                }
                doCheckWifiNetworkState(token);
            }
            if (isUsingStaticIp()) {
                Log.d(HwArpVerifier.TAG, "dhcp result not ok, maybe static IP, go on Arp heart check");
                result = HwArpVerifier.DBG;
            }
            if (result) {
                transmitState(ArpState.HEART_CHECK);
            } else if (this.mArpState == ArpState.HEART_CHECK) {
                transmitState(ArpState.NORMAL_CHECK);
            } else if (this.mArpState == ArpState.CONFIRM_CHECK) {
                if (!this.mArpSuccLeastOnce) {
                    Log.d(HwArpVerifier.TAG, "all Arp test failed in this network, disable Arp check");
                    transmitState(ArpState.DONT_CHECK);
                } else if (!this.this$0.isNeedTriggerReconnectWifi() || this.this$0.isIgnoreArpCheck()) {
                    transmitState(ArpState.HEART_CHECK);
                } else {
                    this.this$0.reconnectWifiNetwork();
                }
            } else if (this.mArpState == ArpState.NORMAL_CHECK) {
                this.mNormalArpFail += STATIC_IP_UNUSED;
                if (this.mNormalArpFail >= STATIC_IP_USER && !this.mLinkLayerLogRunning) {
                    this.this$0.mHwLogUtils.startLinkLayerLog();
                    this.mLinkLayerLogRunning = HwArpVerifier.DBG;
                }
                Log.d(HwArpVerifier.TAG, "Notify wifi network is down for NO." + this.mNormalArpFail);
                if (this.mNormalArpFail >= THRESHOLD_NORMAL_CHECK_FAIL && !this.this$0.isIgnoreArpCheck()) {
                    if (!this.mArpSuccLeastOnce) {
                        Log.d(HwArpVerifier.TAG, "all Arp test failed in this network, disable Arp check");
                        transmitState(ArpState.DONT_CHECK);
                    } else if (this.this$0.isNeedTriggerReconnectWifi()) {
                        if (!WifiProCommonUtils.isWifiSelfCuring()) {
                            this.this$0.recoveryWifiNetwork();
                        }
                        this.this$0.notifyNetworkUnreachable();
                        transmitState(ArpState.CONFIRM_CHECK);
                    } else if (this.mNormalArpFail > HwArpVerifier.MAX_ARP_FAIL_COUNT) {
                        Log.d(HwArpVerifier.TAG, "Arp failed reach to " + this.mNormalArpFail + " in this network, disable Arp check");
                        transmitState(ArpState.DONT_CHECK);
                    }
                }
            }
            doCheckWifiNetworkState(token);
        }

        private void checkWifiNetworkState(int token, int mode) {
            Log.d(HwArpVerifier.TAG, "check_wifi_state_mode = " + mode + " mCheckStateToken=" + this.this$0.mCheckStateToken + " token" + token);
            if (!this.this$0.isConnectedToWifi()) {
                Log.d(HwArpVerifier.TAG, "Notify network is not connected, need not to do ARP test.");
                this.this$0.mCurrentWiFiState = HwArpVerifier.WIFI_STATE_DISCONNECTED;
                transmitState(ArpState.DONT_CHECK);
            }
            if (isNeedCheck()) {
                boolean ret;
                if (this.mArpState == ArpState.HEART_CHECK && this.this$0.mHWGatewayVerifier.isEnableDectction()) {
                    this.this$0.readArpFromFile();
                    this.this$0.mHWGatewayVerifier.getGateWayARPResponses();
                    int gatewayNumber = this.this$0.mHWGatewayVerifier.mGW.getGWNum();
                    Log.d(HwArpVerifier.TAG, "There are " + gatewayNumber + " mac address for gateway ");
                    if (this.this$0.needToDetectGateway()) {
                        Message.obtain(this.this$0.mClientHandler, 1001, token, STATIC_IP_UNKNOWN).sendToTarget();
                        return;
                    }
                    ret = gatewayNumber > 0 ? HwArpVerifier.DBG : false;
                } else {
                    ret = this.this$0.doArpTest(mode);
                }
                if (!ret) {
                    this.this$0.doGratuitousArp(TIME_NORMAL_CHECK);
                    ret = this.this$0.pingGateway(STATIC_IP_USER);
                }
                if (!this.mArpSuccLeastOnce && ret) {
                    this.mArpSuccLeastOnce = HwArpVerifier.DBG;
                }
                handleCheckResult(token, ret);
            }
        }

        private boolean isUsingStaticIp() {
            if (this.mStaticIpStatus == 0) {
                getStaticIpStatus();
            }
            if ((this.mStaticIpStatus & STATIC_IP_USER) > 0) {
                return HwArpVerifier.DBG;
            }
            return false;
        }

        private void getStaticIpStatus() {
            if (isUsingStaticIpFromConfig()) {
                this.mStaticIpStatus = STATIC_IP_USER;
            } else {
                this.mStaticIpStatus = "ok".equals(SystemProperties.get(new StringBuilder().append("dhcp.").append(SystemProperties.get("wifi.interface", HwArpVerifier.IFACE)).append(".result").toString(), HwArpVerifier.BCM_ROAMING_FLAG_FILE)) ? STATIC_IP_UNUSED : STATIC_IP_OPTIMIZE;
            }
            HwWifiCHRStateManagerImpl wcsmImpl = HwWifiCHRStateManagerImpl.getDefaultImpl();
            if (this.mStaticIpStatus == STATIC_IP_OPTIMIZE) {
                wcsmImpl.setIpType(STATIC_IP_USER);
            }
            if (this.mStaticIpStatus == STATIC_IP_USER) {
                wcsmImpl.setIpType(STATIC_IP_UNUSED);
            }
            Log.d(HwArpVerifier.TAG, "getStaticIpStatus:" + this.mStaticIpStatus);
        }

        private boolean isUsingStaticIpFromConfig() {
            if (this.this$0.mWM == null) {
                this.this$0.mWM = (WifiManager) this.this$0.mContext.getSystemService("wifi");
            }
            List<WifiConfiguration> configuredNetworks = this.this$0.mWM.getConfiguredNetworks();
            if (configuredNetworks != null) {
                int netid = HwArpVerifier.WIFI_STATE_DISCONNECTED;
                WifiInfo info = this.this$0.mWM.getConnectionInfo();
                if (info != null) {
                    netid = info.getNetworkId();
                }
                if (netid == HwArpVerifier.WIFI_STATE_DISCONNECTED) {
                    return false;
                }
                for (WifiConfiguration config : configuredNetworks) {
                    if (config != null && config.networkId == netid && config.getIpAssignment() == IpAssignment.STATIC) {
                        return HwArpVerifier.DBG;
                    }
                }
            }
            return false;
        }

        private void doStaticIpHandler(int result) {
            if (this.mStaticIpStatus == 0) {
                getStaticIpStatus();
            }
            Log.d(HwArpVerifier.TAG, "doStaticIpHandler mStaticIpStatus" + this.mStaticIpStatus);
            if (this.mStaticIpStatus == STATIC_IP_OPTIMIZE) {
                this.mStaticIpStatus |= STATIC_IP_DUP;
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
        final /* synthetic */ HwArpVerifier this$0;

        /* synthetic */ HWGatewayVerifier(HwArpVerifier this$0, HWGatewayVerifier hWGatewayVerifier) {
            this(this$0);
        }

        private HWGatewayVerifier(HwArpVerifier this$0) {
            this.this$0 = this$0;
            this.mEnableAccessDetect = HwArpVerifier.WIFI_STATE_DISCONNECTED;
            this.mGW = new HWMultiGW();
            this.mCurrentMac = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
        }

        private void handleMultiGatewayMessage(Message msg) {
            int token = msg.arg1;
            if (token != this.this$0.mCheckStateToken) {
                Log.d(HwArpVerifier.TAG, "ignore msg " + msg.what + ", current token = " + token + ", expected token = " + this.this$0.mCheckStateToken);
                return;
            }
            String gateway;
            switch (msg.what) {
                case MSG_NET_ACCESS_DETECT /*1001*/:
                    String mac = this.mGW.getNextGWMACAddr();
                    gateway = this.mGW.getGWIPAddr();
                    Log.w(HwArpVerifier.TAG, "MSG_NET_ACCESS_DETECT: gateway = " + gateway + ", mac = " + mac);
                    if (mac != null && gateway != null) {
                        this.mCurrentMac = mac;
                        this.this$0.mWifiNative.setStaticARP(HwArpVerifier.IFACE, gateway, mac);
                        this.this$0.flushNetworkDnsCache();
                        this.this$0.flushVmDnsCache();
                        this.this$0.mClientHandler.sendMessageDelayed(Message.obtain(this.this$0.mClientHandler, MSG_NET_ACCESS_DETECT_REAL, token, HwArpVerifier.WIFI_STATE_INITIALED), 1000);
                        break;
                    }
                    Message.obtain(this.this$0.mClientHandler, MSG_NET_ACCESS_DETECT_END, token, HwArpVerifier.WIFI_STATE_INITIALED).sendToTarget();
                    break;
                    break;
                case MSG_NET_ACCESS_DETECT_FAILED /*1002*/:
                    HwArpVerifier hwArpVerifier = this.this$0;
                    hwArpVerifier.mCheckStateToken = hwArpVerifier.mCheckStateToken + DEFAULT_GATEWAY_NUMBER;
                    gateway = this.mGW.getGWIPAddr();
                    if (!TextUtils.isEmpty(gateway)) {
                        addToBlacklist();
                        this.this$0.mWifiNative.delStaticARP(HwArpVerifier.IFACE, gateway);
                        Message.obtain(this.this$0.mClientHandler, MSG_NET_ACCESS_DETECT, this.this$0.mCheckStateToken, HwArpVerifier.WIFI_STATE_INITIALED).sendToTarget();
                        break;
                    }
                    break;
                case MSG_NET_ACCESS_DETECT_END /*1003*/:
                    this.this$0.mClientHandler.doStaticIpHandler(msg.arg2);
                    this.this$0.mClientHandler.handleCheckResult(this.this$0.mCheckStateToken, HwArpVerifier.DBG);
                    break;
                case MSG_NET_ACCESS_DETECT_REAL /*1004*/:
                    startNetAccessDetection(token);
                    break;
            }
        }

        private void getGateWayARPResponses() {
            getGateWayARPResponses(DEFAULT_GATEWAY_NUMBER, TIME_CLEAR_DNS);
        }

        private void getGateWayARPResponses(int arpNum, int timeout) {
            this.mGW.clearGW();
            HWArpPeer hWArpPeer = null;
            try {
                hWArpPeer = this.this$0.constructArpPeer();
                if (hWArpPeer == null) {
                    if (hWArpPeer != null) {
                        hWArpPeer.close();
                    }
                    return;
                }
                this.mGW.setGWIPAddr(this.this$0.mGateway);
                for (int i = HwArpVerifier.WIFI_STATE_INITIALED; i < arpNum; i += DEFAULT_GATEWAY_NUMBER) {
                    boolean isSucc = false;
                    HWMultiGW multiGW = hWArpPeer.getGateWayARPResponses(timeout);
                    if (multiGW != null) {
                        this.this$0.mAccessWebStatus.setRTTArp((int) multiGW.getArpRTT());
                        Log.d(HwArpVerifier.TAG, "getGateWayARPResponses: arp rtt = " + multiGW.getArpRTT());
                        for (String macAddr : multiGW.getGWMACAddrList()) {
                            this.mGW.setGWMACAddr(macAddr);
                            isSucc = HwArpVerifier.DBG;
                        }
                        this.this$0.wcsm.updateMultiGWCount((byte) this.mGW.getGWNum());
                        this.this$0.rssi_summery.updateArpSummery(isSucc, (int) multiGW.getArpRTT(), HwArpVerifier.mRSSI);
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
            new WebDetectThread(this.this$0, token).start();
            Log.d(HwArpVerifier.TAG, "access internet timeout:10000");
            this.this$0.mClientHandler.sendMessageDelayed(Message.obtain(this.this$0.mClientHandler, MSG_NET_ACCESS_DETECT_FAILED, token, HwArpVerifier.WIFI_STATE_INITIALED), HwArpVerifier.ACCESS_BAIDU_TIMEOUT);
        }

        private boolean isEnableDectction() {
            if (this.mEnableAccessDetect < 0) {
                String countryCode = SystemProperties.get("ro.product.locale.region", HwArpVerifier.BCM_ROAMING_FLAG_FILE);
                Log.d(HwArpVerifier.TAG, "local region:" + countryCode);
                if (COUNTRY_CODE_CN.equalsIgnoreCase(countryCode)) {
                    this.mEnableAccessDetect = DEFAULT_GATEWAY_NUMBER;
                    HwCHRWebDetectThread.setEnableCheck(HwArpVerifier.DBG);
                } else {
                    this.mEnableAccessDetect = HwArpVerifier.WIFI_STATE_INITIALED;
                }
            }
            if (this.mEnableAccessDetect == DEFAULT_GATEWAY_NUMBER) {
                return HwArpVerifier.DBG;
            }
            return false;
        }

        private void addToBlacklist() {
            if (!TextUtils.isEmpty(this.mCurrentMac)) {
                for (ArpItem item : this.this$0.mArpBlacklist) {
                    if (item.sameMacAddress(this.mCurrentMac)) {
                        item.putFail();
                        return;
                    }
                }
                this.this$0.mArpBlacklist.add(new ArpItem(this.mCurrentMac, DEFAULT_GATEWAY_NUMBER));
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
        private HwCHRWebDetectThread detect;
        private int mInnerToken;
        final /* synthetic */ HwArpVerifier this$0;

        public WebDetectThread(HwArpVerifier this$0, int token) {
            this.this$0 = this$0;
            this.mInnerToken = token;
            this.detect = new HwCHRWebDetectThread(HwArpVerifier.WIFI_STATE_INITIALED);
        }

        public void run() {
            boolean ret = this.detect.isInternetConnected();
            if (this.mInnerToken == this.this$0.mCheckStateToken) {
                this.this$0.mClientHandler.removeMessages(1002);
                if (ret) {
                    if (!LogManager.getInstance().isCommercialUser()) {
                        this.this$0.mClientHandler.accessWeb(HwArpVerifier.WEB_CHINAZ_GETIP);
                    }
                    Message.obtain(this.this$0.mClientHandler, 1003, this.mInnerToken, HwArpVerifier.WIFI_STATE_CONNECTED).sendToTarget();
                    return;
                }
                Log.d(HwArpVerifier.TAG, "browse web failed, will del static ARP item");
                Message.obtain(this.this$0.mClientHandler, 1002, this.mInnerToken, HwArpVerifier.WIFI_STATE_INITIALED).sendToTarget();
                this.this$0.errCode = this.detect.getErrorCode();
            }
        }
    }

    protected static class WebParam {
        int mRespCode;
        long mStartTime;
        String mUrl;

        public WebParam(String url, long time, int code) {
            this.mUrl = HwArpVerifier.BCM_ROAMING_FLAG_FILE;
            this.mStartTime = 0;
            this.mRespCode = HwArpVerifier.WIFI_STATE_INITIALED;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.HwArpVerifier.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.HwArpVerifier.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.HwArpVerifier.<clinit>():void");
    }

    private static native void class_init_native();

    private native int nativeReadArpDetail();

    private native void native_init();

    public HwArpVerifier(Context context) {
        this.mRoamingFlagFile = null;
        this.mShortDurationStartTime = 0;
        this.mLongDurationStartTime = 0;
        this.mShortTriggerCnt = WIFI_STATE_INITIALED;
        this.mLongTriggerCnt = WIFI_STATE_INITIALED;
        this.mLastSSID = null;
        this.mSpendTime = WIFI_STATE_INITIALED;
        this.mCurrentWiFiState = WIFI_STATE_INITIALED;
        this.mCheckStateToken = WIFI_STATE_INITIALED;
        this.mRegisterReceiver = false;
        this.isMobileDateActive = false;
        this.isRouteRepareSwitchEnabled = SystemProperties.getBoolean("ro.config.hw_route_repare", false);
        this.mGateway = null;
        this.mContext = null;
        this.mThread = null;
        this.mServiceHandler = null;
        this.mClientHandler = null;
        this.mWM = null;
        this.mCM = null;
        this.mNetworkInfo = new NetworkInfo(WIFI_STATE_CONNECTED, WIFI_STATE_INITIALED, "WIFI", BCM_ROAMING_FLAG_FILE);
        this.mWifiNative = null;
        this.mNetstatManager = null;
        this.mRevLinkProperties = null;
        this.mRouteDetectCnt = WIFI_STATE_INITIALED;
        this.webMonitor = null;
        this.wcsm = null;
        this.errCode = WIFI_STATE_INITIALED;
        this.mHwLogUtils = null;
        this.rssi_summery = null;
        this.mlDetectWebCounter = 0;
        this.mIsFirstCheck = DBG;
        this.mAccessWebStatus = new AccessWebStatus();
        this.mReceiver = new BroadcastReceiver() {
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
                    iArr[DetailedState.CONNECTED.ordinal()] = HwArpVerifier.WIFI_STATE_CONNECTED;
                } catch (NoSuchFieldError e4) {
                }
                try {
                    iArr[DetailedState.CONNECTING.ordinal()] = HwArpVerifier.LONG_ARP_FAIL_TIMES_THRESHOLD;
                } catch (NoSuchFieldError e5) {
                }
                try {
                    iArr[DetailedState.DISCONNECTED.ordinal()] = HwArpVerifier.SHORT_ARP_FAIL_TIMES_THRESHOLD;
                } catch (NoSuchFieldError e6) {
                }
                try {
                    iArr[DetailedState.DISCONNECTING.ordinal()] = 7;
                } catch (NoSuchFieldError e7) {
                }
                try {
                    iArr[DetailedState.FAILED.ordinal()] = HwArpVerifier.ARP_REASSOC_OK;
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
                                case HwArpVerifier.WIFI_STATE_CONNECTED /*1*/:
                                    if (intent.hasExtra("linkProperties")) {
                                        HwArpVerifier.this.mRevLinkProperties = (LinkProperties) intent.getParcelableExtra("linkProperties");
                                    } else {
                                        HwArpVerifier.this.mRevLinkProperties = null;
                                    }
                                    HwArpVerifier.this.rssi_summery.resetRSSIGroup();
                                    break;
                                case HwArpVerifier.SHORT_ARP_FAIL_TIMES_THRESHOLD /*2*/:
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
                            HwArpVerifier.this.isMobileDateActive = HwArpVerifier.DBG;
                        } else if ("DISCONNECTED".equalsIgnoreCase(dataState)) {
                            HwArpVerifier.this.isMobileDateActive = false;
                        }
                    }
                }
            }
        };
        this.mFirstDetect = DBG;
        this.mArpItems = new ArrayList();
        this.mArpBlacklist = new ArrayList();
        this.mHWGatewayVerifier = new HWGatewayVerifier();
        this.mLastNetworkId = WIFI_STATE_DISCONNECTED;
        this.mContext = context;
        this.mWifiNative = WifiNative.getWlanNativeInterface();
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

    public void startArpChecker() {
        Log.d(TAG, "startArpChecker 1");
        if (isEnableChecker()) {
            startLooper();
        }
    }

    public void stopArpChecker() {
        Log.d(TAG, "stopArpChecker");
        stopLooper();
    }

    public boolean doArpTest(int arpNum, int minResponse, int timeout, boolean async) {
        if (arpNum <= 0) {
            arpNum = WIFI_STATE_CONNECTED;
        }
        if (minResponse > arpNum || minResponse <= 0) {
            minResponse = WIFI_STATE_CONNECTED;
        }
        if (timeout <= 0) {
            timeout = DEFAULT_ARP_PING_TIMEOUT_MS;
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
        return DBG;
    }

    private void startLooper() {
        if (this.mThread == null) {
            this.mThread = new HandlerThread("WiFiArpStateMachine");
            this.mThread.start();
            this.mClientHandler = new ClientHandler(this, this.mThread.getLooper());
            Log.d(TAG, "startLooper");
        }
    }

    private void stopLooper() {
        if (this.mThread != null) {
            this.mThread.quit();
            this.mClientHandler = null;
            this.mThread = null;
            Log.d(TAG, "stopLooper");
        }
    }

    private boolean isLooperRunning() {
        return this.mThread != null ? DBG : false;
    }

    private boolean isEnableChecker() {
        return DBG;
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

    private boolean readRoamingFlag() {
        Exception e;
        Throwable th;
        boolean z = false;
        String roam_flag = "roam_status=";
        BufferedReader bufferedReader = null;
        try {
            if (this.mRoamingFlagFile == null || this.mRoamingFlagFile.isEmpty()) {
                return false;
            }
            File file = new File(this.mRoamingFlagFile);
            if (!file.exists()) {
                return false;
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            try {
                String s = in.readLine();
                if (s != null) {
                    int pos = s.indexOf("roam_status=");
                    if (pos >= 0 && "roam_status=".length() + pos < s.length()) {
                        String flag = s.substring("roam_status=".length() + pos);
                        if (flag != null) {
                            z = "1".equals(flag);
                        }
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                bufferedReader = in;
            } catch (Exception e3) {
                e2 = e3;
                bufferedReader = in;
                try {
                    e2.printStackTrace();
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Exception e22) {
                            e22.printStackTrace();
                        }
                    }
                    return z;
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Exception e222) {
                            e222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                bufferedReader = in;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                throw th;
            }
            return z;
        } catch (Exception e4) {
            e222 = e4;
            e222.printStackTrace();
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            return z;
        }
    }

    private boolean doArpTest(int mode) {
        if (mode == 0) {
            return doArp(WIFI_STATE_CONNECTED, WIFI_STATE_CONNECTED, DEFAULT_SIG_PING_TIMEOUT_MS);
        }
        if (mode == WIFI_STATE_CONNECTED) {
            return doArp(SHORT_ARP_FAIL_TIMES_THRESHOLD, WIFI_STATE_CONNECTED, DEFAULT_FUL_PING_TIMEOUT_MS);
        }
        if (mode != SHORT_ARP_FAIL_TIMES_THRESHOLD || this.mClientHandler == null) {
            return DBG;
        }
        return doArp(this.mClientHandler.genDynamicCheckPings(), WIFI_STATE_CONNECTED, DEFAULT_SIG_PING_TIMEOUT_MS);
    }

    private boolean doArp(int arpNum, int minResponse, int timeout) {
        this.mSpendTime = WIFI_STATE_INITIALED;
        HWArpPeer hWArpPeer = null;
        Log.d(TAG, "doArp() arpnum:" + arpNum + ", minResponse:" + minResponse + ", timeout:" + timeout);
        boolean retArp;
        try {
            hWArpPeer = constructArpPeer();
            if (hWArpPeer == null) {
                if (hWArpPeer != null) {
                    hWArpPeer.close();
                }
                return DBG;
            }
            int responses = WIFI_STATE_INITIALED;
            for (int i = WIFI_STATE_INITIALED; i < arpNum; i += WIFI_STATE_CONNECTED) {
                long startTimestamp = System.currentTimeMillis();
                if (isIgnoreArpCheck()) {
                    Log.d(TAG, "isIgnoreArpCheck is ture, ignore ARP check");
                    responses += WIFI_STATE_CONNECTED;
                } else if (hWArpPeer.doArp(timeout) != null) {
                    responses += WIFI_STATE_CONNECTED;
                }
                int spendTime = (int) (System.currentTimeMillis() - startTimestamp);
                if (spendTime > this.mSpendTime) {
                    this.mSpendTime = spendTime;
                }
            }
            Log.d(TAG, "ARP test result: " + responses + "/" + arpNum);
            if (responses >= minResponse) {
                retArp = DBG;
            } else {
                retArp = false;
            }
            if (hWArpPeer != null) {
                hWArpPeer.close();
            }
            this.rssi_summery.updateArpSummery(retArp, this.mSpendTime, mRSSI);
            return retArp;
        } catch (SocketException se) {
            Log.e(TAG, "exception in ARP test: " + se);
            retArp = DBG;
            if (hWArpPeer != null) {
                hWArpPeer.close();
            }
        } catch (IllegalArgumentException ae) {
            Log.e(TAG, "exception in ARP test:" + ae);
            retArp = DBG;
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
        byte[] bArr = null;
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
                Log.d(TAG, "isIgnoreArpCheck is ture, ignore doGratuitousArp");
            } else {
                bArr = hWArpPeer.doGratuitousArp(timeout);
            }
            if (hWArpPeer != null) {
                hWArpPeer.close();
            }
            if (bArr != null && bArr.length == LONG_ARP_FAIL_TIMES_THRESHOLD) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                Object[] objArr = new Object[LONG_ARP_FAIL_TIMES_THRESHOLD];
                objArr[WIFI_STATE_INITIALED] = Byte.valueOf(bArr[WIFI_STATE_INITIALED]);
                objArr[WIFI_STATE_CONNECTED] = Byte.valueOf(bArr[WIFI_STATE_CONNECTED]);
                objArr[SHORT_ARP_FAIL_TIMES_THRESHOLD] = Byte.valueOf(bArr[SHORT_ARP_FAIL_TIMES_THRESHOLD]);
                objArr[3] = Byte.valueOf(bArr[3]);
                objArr[4] = Byte.valueOf(bArr[4]);
                objArr[5] = Byte.valueOf(bArr[5]);
                Log.w(str, stringBuilder.append(String.format("%02x:%02x:%02x:%02x:%02x:%02x", objArr)).append("alse use My IP(IP conflict detected)").toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "exception in GARP test:" + e);
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
        return this.mCM.getLinkProperties(WIFI_STATE_CONNECTED);
    }

    private boolean isConnectedToWifi() {
        boolean z = DBG;
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
        String str = null;
        InetAddress linkAddr = null;
        if (wifiInfo != null) {
            str = wifiInfo.getMacAddress();
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
        return new HWArpPeer(linkIFName, linkAddr, str, gateway);
    }

    private void handleWifiSwitchChanged(int state) {
        Log.d(TAG, "handleWifiSwitchChanged state:" + state);
        switch (state) {
            case MessageUtil.MSG_WIFI_ENABLED /*3*/:
                resetDurationControlParams();
            default:
        }
    }

    private void resetDurationControlParams() {
        this.mShortDurationStartTime = 0;
        this.mLongDurationStartTime = 0;
        this.mShortTriggerCnt = WIFI_STATE_INITIALED;
        this.mLongTriggerCnt = WIFI_STATE_INITIALED;
        this.mLastSSID = null;
    }

    private void updateDurationControlParamsIfNeed() {
        String ssid = getCurrentSsid();
        if (ssid == null || "<unknown ssid>".equals(ssid)) {
            Log.e(TAG, "current SSID is empty.");
            return;
        }
        if (this.mLastSSID == null) {
            this.mLastSSID = ssid;
        } else if (!ssid.equals(this.mLastSSID)) {
            Log.d(TAG, "connected SSID " + this.mLastSSID + " changed to " + ssid);
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
        Log.e(TAG, "fail to get current wifi info in getCurrentSsid");
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
        Log.d(TAG, "short duration: [now:" + now + ", lastTime:" + this.mShortDurationStartTime + ", duration:" + SHORT_ARP_FAIL_DURATION + ", failTimes:" + this.mShortTriggerCnt + ", failThreshold:" + SHORT_ARP_FAIL_TIMES_THRESHOLD);
        Log.d(TAG, "long duration: [now:" + now + ", lastTime:" + this.mLongDurationStartTime + ", duration:" + LONG_ARP_FAIL_DURATION + ", failTimes:" + this.mLongTriggerCnt + ", failThreshold:" + LONG_ARP_FAIL_TIMES_THRESHOLD);
        if (now - this.mShortDurationStartTime > SHORT_ARP_FAIL_DURATION) {
            this.mShortTriggerCnt = WIFI_STATE_INITIALED;
            this.mShortDurationStartTime = now;
            ret = DBG;
        } else if (this.mShortTriggerCnt >= SHORT_ARP_FAIL_TIMES_THRESHOLD) {
            ret = false;
        } else {
            ret = DBG;
        }
        Log.d(TAG, "short duration control ret is:" + ret);
        if (!ret) {
            return false;
        }
        this.mShortTriggerCnt += WIFI_STATE_CONNECTED;
        if (now - this.mLongDurationStartTime > LONG_ARP_FAIL_DURATION) {
            this.mLongTriggerCnt = WIFI_STATE_INITIALED;
            this.mLongDurationStartTime = now;
            ret = DBG;
        } else if (this.mLongTriggerCnt >= LONG_ARP_FAIL_TIMES_THRESHOLD) {
            ret = false;
        } else {
            ret = DBG;
        }
        if (ret) {
            this.mLongTriggerCnt += WIFI_STATE_CONNECTED;
        }
        Log.d(TAG, "long duration control ret is:" + ret);
        return ret;
    }

    private boolean isNeedTriggerReconnectWifi() {
        if (isPassDurationControl()) {
            return DBG;
        }
        Log.e(TAG, "don't pass duration control, skip Wifi reconnect.");
        return false;
    }

    private boolean isIgnoreArpCheck() {
        if (isWeakSignal()) {
            return DBG;
        }
        if (!readRoamingFlag()) {
            return false;
        }
        Log.d(TAG, "It's WiFi roaming now, ignore arp check");
        return DBG;
    }

    private boolean isWeakSignal() {
        WifiInfo curWifi = getCurrentWifiInfo();
        if (curWifi == null) {
            Log.e(TAG, "fail to get current wifi info in isWeakSignal.");
            return false;
        }
        int rssi = curWifi.getRssi();
        if (rssi > WEAK_SIGNAL_THRESHOLD) {
            return false;
        }
        Log.e(TAG, "current WIFI rssi:" + rssi + " is weak");
        return DBG;
    }

    private void recoveryWifiNetwork() {
        if (isSupplicantStopped()) {
            reopenWifi();
        } else if (hasWrongAction()) {
            triggerDisableNMode();
        } else {
            Log.d(TAG, "wifi network broken, try to reset interface.");
            Log.d(TAG, "reassociate to the previous BSSID");
            this.mWifiNative.reassociate();
        }
    }

    private void reconnectWifiNetwork() {
        Log.d(TAG, "Atfer reassociate, network is still broken");
        WifiInfo wifiInfo = this.mWM.getConnectionInfo();
        Intent intent = new Intent(ACTION_ARP_RECONNECT_WIFI);
        int networkid = WIFI_STATE_INITIALED;
        if (wifiInfo != null) {
            intent.putExtra(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_SSID, wifiInfo.getSSID());
            networkid = wifiInfo.getNetworkId();
        }
        this.mContext.sendBroadcast(intent, "android.permission.ACCESS_WIFI_STATE");
        Log.d(TAG, "disconnect from the previous BSSID");
        this.mWifiNative.disconnect();
        if (networkid != WIFI_STATE_DISCONNECTED) {
            Log.d(TAG, "enable the previous network id=" + networkid);
            this.mWM.enableNetwork(networkid, false);
            this.mWM.startScan();
            return;
        }
        Log.d(TAG, "reconnect to the previous BSSID");
        this.mWifiNative.reconnect();
    }

    private void notifyNetworkUnreachable() {
        this.wcsm.updateAccessWebException(WIFI_STATE_INITIALED, "ARP_UNREACHABLE");
    }

    public static String readFileByChars(String fileName) {
        File file = new File(fileName);
        if (!file.exists() || !file.canRead()) {
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
                if (charRead == WIFI_STATE_DISCONNECTED) {
                    break;
                }
                sb.append(tempChars, WIFI_STATE_INITIALED, charRead);
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

    public static String writeFile(String fileName, String ctrl) {
        IOException ie;
        Throwable th;
        String result = "success";
        File file = new File(fileName);
        if (file.exists() && file.canWrite()) {
            OutputStream outputStream = null;
            try {
                OutputStream out = new FileOutputStream(file);
                try {
                    out.write(ctrl.getBytes(Charset.defaultCharset()));
                    out.flush();
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                        }
                    }
                    outputStream = out;
                } catch (IOException e2) {
                    ie = e2;
                    outputStream = out;
                    try {
                        result = "IOException occured";
                        ie.printStackTrace();
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e3) {
                            }
                        }
                        return result;
                    } catch (Throwable th2) {
                        th = th2;
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e4) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    outputStream = out;
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    throw th;
                }
            } catch (IOException e5) {
                ie = e5;
                result = "IOException occured";
                ie.printStackTrace();
                if (outputStream != null) {
                    outputStream.close();
                }
                return result;
            }
            return result;
        }
        Log.d(TAG, "file is exists " + file.exists() + " file can write " + file.canWrite());
        return BCM_ROAMING_FLAG_FILE;
    }

    private final boolean pingGateway(int timeout) {
        if (this.mGateway == null) {
            return false;
        }
        if (!isIgnoreArpCheck()) {
            return ping(this.mGateway, timeout);
        }
        Log.d(TAG, "isIgnoreArpCheck is ture, ignore ping gateway");
        return DBG;
    }

    private final boolean ping(String ipAddress, int timeout) {
        Object[] objArr = new Object[SHORT_ARP_FAIL_TIMES_THRESHOLD];
        objArr[WIFI_STATE_INITIALED] = Integer.valueOf(timeout);
        objArr[WIFI_STATE_CONNECTED] = ipAddress;
        String cmd = String.format("ping -c 1 -w %d %s", objArr);
        Log.d(TAG, "ping: " + cmd);
        boolean ret = false;
        try {
            ret = Runtime.getRuntime().exec(cmd).waitFor() == 0 ? DBG : false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "execute cmd=" + cmd + ",ret=" + ret);
        return ret;
    }

    private boolean isSupplicantStopped() {
        String suppcantStatus = BCM_ROAMING_FLAG_FILE;
        String chipType = SystemProperties.get("ro.connectivity.chiptype", BCM_ROAMING_FLAG_FILE);
        if (chipType == null || !chipType.equalsIgnoreCase("hi110x")) {
            suppcantStatus = SystemProperties.get("init.svc.p2p_supplicant", "running");
        } else {
            suppcantStatus = SystemProperties.get("init.svc.wpa_supplicant", "running");
        }
        Log.d(TAG, "wpa_supplicant state:" + suppcantStatus);
        return "stopped".equals(suppcantStatus);
    }

    private void reopenWifi() {
        if (this.mWM == null) {
            this.mWM = (WifiManager) this.mContext.getSystemService("wifi");
        }
        Log.d(TAG, "reopen wifi because wpa_supplicant is stopped!");
        this.mWM.setWifiEnabled(false);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        this.mWM.setWifiEnabled(DBG);
    }

    private boolean hasWrongAction() {
        String value = readFileByChars(WIFI_WRONG_ACTION_FLAG);
        Log.d(TAG, "hasWrongAction:" + value);
        return "1".equals(value.trim());
    }

    private void triggerDisableNMode() {
        Log.d(TAG, "triggerDisableNMode enter");
        writeFile(WIFI_ARP_TIMEOUT, "1");
    }

    private void reportArpDetail(String ipaddr, String hwaddr, int flag, String device) {
        Log.d(TAG, "reportArpDetail: " + ipaddr + "  " + hwaddr + "   " + flag + "   " + device);
        this.mArpItems.add(new ArpItem(ipaddr, hwaddr, flag, device));
    }

    private void readArpFromFile() {
        this.mArpItems.clear();
        nativeReadArpDetail();
    }

    private boolean needToDetectGateway() {
        HwCHRWebDetectThread.setFirstDetect(this.mFirstDetect);
        if (this.mFirstDetect) {
            this.mFirstDetect = false;
            return DBG;
        }
        ArrayList<String> unfoundlist = new ArrayList();
        ArrayList<String> maclist = this.mHWGatewayVerifier.mGW.getGWMACAddrList();
        int i = maclist.size() + WIFI_STATE_DISCONNECTED;
        while (i >= 0) {
            for (ArpItem blackitem : this.mArpBlacklist) {
                if (blackitem.matchMaxRetried() && blackitem.sameMacAddress((String) maclist.get(i))) {
                    maclist.remove(i);
                    break;
                }
            }
            i += WIFI_STATE_DISCONNECTED;
        }
        for (ArpItem arpitem : this.mArpItems) {
            boolean found = DBG;
            for (String mac : maclist) {
                if (arpitem.isValid() && arpitem.sameIpaddress(this.mGateway)) {
                    if (!arpitem.sameMacAddress(mac)) {
                        found = false;
                    } else if (arpitem.isStaticArp()) {
                        Log.d(TAG, "mac is static ARP: " + mac);
                        return false;
                    } else {
                        found = DBG;
                        if (!found) {
                            unfoundlist.add(arpitem.hwaddr);
                        }
                    }
                }
            }
            if (!found) {
                unfoundlist.add(arpitem.hwaddr);
            }
        }
        maclist.addAll(unfoundlist);
        return maclist.size() > WIFI_STATE_CONNECTED ? DBG : false;
    }

    public void startWifiRouteCheck() {
        if (this.mClientHandler != null) {
            Log.d(TAG, "startWifiRouteCheck");
            if (this.mClientHandler.hasMessages(HwWifiCHRStateManagerImpl.WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT)) {
                this.mClientHandler.removeMessages(HwWifiCHRStateManagerImpl.WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT);
            }
            this.mClientHandler.sendEmptyMessageDelayed(HwWifiCHRStateManagerImpl.WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT, 5000);
        }
    }

    public void stopWifiRouteCheck() {
        if (this.mClientHandler != null && this.mClientHandler.hasMessages(HwWifiCHRStateManagerImpl.WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT)) {
            this.mClientHandler.removeMessages(HwWifiCHRStateManagerImpl.WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT);
        }
    }

    private boolean isWifiDefaultRouteExist() {
        if (this.mRevLinkProperties == null) {
            throw new NullPointerException("mRevLinkProperties is null");
        }
        String wifiRoutes = runShellCmd(new String[]{"/system/bin/ip", "route", "show", "table", IFACE});
        if (wifiRoutes == null || wifiRoutes.length() == 0) {
            Log.d(TAG, "try to get main route table");
            String[] args2 = new String[SHORT_ARP_FAIL_TIMES_THRESHOLD];
            args2[WIFI_STATE_INITIALED] = "/system/bin/ip";
            args2[WIFI_STATE_CONNECTED] = "route";
            wifiRoutes = runShellCmd(args2);
        }
        Log.d(TAG, "---------  wifi route notify -------");
        Log.d(TAG, wifiRoutes);
        Log.d(TAG, "------------------------------------");
        if (wifiRoutes != null) {
            String[] tok = wifiRoutes.toString().split("\n");
            if (tok == null) {
                Log.e(TAG, "wifi default route is not exist, tok==null");
                return false;
            }
            int length = tok.length;
            int i = WIFI_STATE_INITIALED;
            while (i < length) {
                String routeline = tok[i];
                if (routeline.length() <= 10 || !routeline.startsWith("default") || routeline.indexOf(IFACE) < 0) {
                    i += WIFI_STATE_CONNECTED;
                } else {
                    Log.d(TAG, "Notify wifi default route is ok");
                    return DBG;
                }
            }
        }
        Log.e(TAG, "wifi default route is not exist!");
        return false;
    }

    private boolean wifiRepairRoute() {
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        int netid = WIFI_STATE_DISCONNECTED;
        if (cm != null) {
            Network network = HwServiceFactory.getHwConnectivityManager().getNetworkForTypeWifi();
            if (network == null) {
                Log.e(TAG, "wifiRepairRoute, network is null");
                return false;
            }
            netid = network.netId;
            Log.d(TAG, "netid = " + netid);
        }
        Log.e(TAG, "Enter wifiReparierRoute");
        if (this.mNwService == null || this.mRevLinkProperties == null) {
            Log.e(TAG, "Repair wifi default Route failed, mNwService mRevLinkProperties is null");
            return false;
        } else if (!isConnectedToWifi()) {
            return false;
        } else {
            for (RouteInfo r : this.mRevLinkProperties.getRoutes()) {
                if (r.isDefaultRoute()) {
                    Log.d(TAG, "mRevLinkProperties=" + this.mRevLinkProperties);
                    if (netid > 0) {
                        try {
                            Log.d(TAG, "ifacename=" + this.mRevLinkProperties.getInterfaceName());
                            this.mNwService.addRoute(netid, r);
                            this.mNwService.setDefaultNetId(netid);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "netid is not available");
                        return false;
                    }
                    Log.d(TAG, "RouteDetect addRoute finish");
                    return DBG;
                }
            }
            Log.d(TAG, "Repair wifi default failed, no default route in mRevLinkProperties");
            return false;
        }
    }

    public static String runShellCmd(String[] args) {
        StringBuilder result = new StringBuilder();
        StringBuffer sbuff = new StringBuffer();
        for (int i = WIFI_STATE_INITIALED; i < args.length; i += WIFI_STATE_CONNECTED) {
            sbuff.append(args[i]).append(HwCHRWifiCPUUsage.COL_SEP);
        }
        if (TextUtils.isEmpty(sbuff)) {
            return null;
        }
        try {
            Process process = new ProcessBuilder(args).start();
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while (true) {
                String s = reader.readLine();
                if (s == null) {
                    return result.toString();
                }
                result.append(s).append("\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void flushNetworkDnsCache() {
        if (this.mCM == null || this.mNwService == null) {
            Log.d(TAG, "flushNetworkDnsCache failed: mCM or mNwService is null");
        } else if (isConnectedToWifi()) {
            Network network = HwServiceFactory.getHwConnectivityManager().getNetworkForTypeWifi();
            int netid = network == null ? WIFI_STATE_DISCONNECTED : network.netId;
            this.mLastNetworkId = netid;
            Log.d(TAG, "flushNetworkDnsCache netid:" + netid);
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
}

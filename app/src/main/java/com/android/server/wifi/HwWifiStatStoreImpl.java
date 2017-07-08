package com.android.server.wifi;

import android.common.HwFrameworkFactory;
import android.net.TrafficStats;
import android.net.wifi.SupplicantState;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.wifipro.WifiProStatisticsManager;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
import com.huawei.connectivitylog.LogManager;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_STABILITY_SSIDSTAT;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_STABILITY_STAT;
import com.huawei.device.connectivitychrlog.ChrLogBaseModel;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HwWifiStatStoreImpl implements HwWifiStatStore {
    private static final int ASSOC_REJECT_ACCESSFULL = 17;
    private static final String DEFAULT_WLAN_IFACE = "wlan0";
    private static final String DNAMESTR = "dname=";
    private static final int HW_CONNECT_REASON_CONNECTING = 1;
    private static final int HW_CONNECT_REASON_REKEY = 3;
    private static final int HW_CONNECT_REASON_ROAMING = 2;
    private static final int HW_CONNECT_REASON_UNINITIAL = 0;
    private static final String KEY_ABDISCONNECT_CNT = "key_abdisconnect_cnt:";
    private static final String KEY_ACCESS_WEB_CNT = "key_on_access_web_cnt:";
    private static final String KEY_ACCESS_WEB_FAILED_BY_PORTAL_CONNECT = "key_access_web_failed_portal_connect:";
    private static final String KEY_ACCESS_WEB_FAILED_BY_PORTAL_REDHCP = "key_access_web_failed_portal_redhcp:";
    private static final String KEY_ACCESS_WEB_FAILED_BY_PORTAL_ROAMING = "key_access_web_failed_portal_roaming:";
    private static final String KEY_ACCESS_WEB_SUCC_CNT = "key_on_access_web_succ_cnt:";
    private static final String KEY_APP_DISABLED_ABNORMAL = "key_app_disabled_abnormal:";
    private static final String KEY_APP_DISABLED_SC_SUCC = "key_disabled_sc_succ:";
    private static final String KEY_AP_AUTH_ALG = "key_auth_alg:";
    private static final String KEY_AP_EAP = "key_EAP:";
    private static final String KEY_AP_GROUP = "key_gruop:";
    private static final String KEY_AP_KEY_MGMT = "key_key_mgmt:";
    private static final String KEY_AP_PAIRWISE = "key_pairwise:";
    private static final String KEY_AP_PROTO = "key_PROTO:";
    private static final String KEY_AP_VENDORINFO = "key_APVENDORINFO:";
    private static final String KEY_ARP_REASSOC_OK_CNT = "key_arp_reassoc_ok_cnt:";
    private static final String KEY_ARP_UNREACHABLE_CNT = "key_arp_unreachable_cnt:";
    private static final String KEY_ASSOC_BY_ABS_CNT = "key_assoc_by_abs_cnt:";
    private static final String KEY_ASSOC_CNT = "key_assoc_cnt:";
    private static final String KEY_ASSOC_DURATION = "key_assoc_duration:";
    private static final String KEY_ASSOC_REJECTED_ABNORMAL = "key_assoc_rejected_abnormal:";
    private static final String KEY_ASSOC_REJECTED_SC_SUCC = "key_assoc_rejected_sc_succ:";
    private static final String KEY_ASSOC_REJECT_CNT = "key_assoc_reject_cnt:";
    private static final String KEY_ASSOC_SUCC_CNT = "key_assoc_succ_cnt:";
    private static final String KEY_AUTH_CNT = "key_auth_cnt:";
    private static final String KEY_AUTH_DURATION = "key_auth_duration:";
    private static final String KEY_AUTH_FAILED_ABNORMAL = "key_auth_failed_abnormal:";
    private static final String KEY_AUTH_FAILED_SC_SUCC = "key_auth_failed_sc_succ:";
    private static final String KEY_AUTH_SUCC_CNT = "key_auth_succ_cnt:";
    private static final String KEY_BSSID = "key_BSSID:";
    private static final String KEY_CHR_CONNECTING_DURATION = "key_chr_connecting_duration:";
    private static final String KEY_CLOSE_CNT = "key_close_cnt:";
    private static final String KEY_CLOSE_DURATION = "key_close_duration:";
    private static final String KEY_CLOSE_SUCC_CNT = "key_close_succ_cnt:";
    private static final String KEY_CONNECTED_CNT = "key_connected_cnt:";
    private static final String KEY_CONNECTED_DURATION = "key_connected_duration:";
    private static final String KEY_CONNECT_TOTAL_CNT = "key_connect_total_cnt:";
    private static final String KEY_DHCP_CNT = "key_dhcp_cnt:";
    private static final String KEY_DHCP_DURATION = "key_dhcp_duration:";
    private static final String KEY_DHCP_FAILED_ABNORMAL = "key_dhcp_failed_abnormal:";
    private static final String KEY_DHCP_FAILED_SC_SUCC = "key_dhcp_failed_sc_succ:";
    private static final String KEY_DHCP_STATIC_CNT = "key_dhcp_static_cnt:";
    private static final String KEY_DHCP_STATIC_SUCC_CNT = "key_dhcp_static_succ_cnt:";
    private static final String KEY_DHCP_SUCC_CNT = "key_dhcp_succ_cnt:";
    private static final String KEY_DISCONNECT_CNT = "key_disconnect_cnt:";
    private static final String KEY_DNS_ABNORMAL = "key_dns_abnormal:";
    private static final String KEY_DNS_MAX_TIME = "key_dns_max_time:";
    private static final String KEY_DNS_MIN_TIME = "key_dns_min_time:";
    private static final String KEY_DNS_PARSE_FAIL_CNT = "key_dns_parse_fail_cnt:";
    private static final String KEY_DNS_REQ_CNT = "key_dns_req_cnt:";
    private static final String KEY_DNS_REQ_FAIL = "key_dns_req_fail:";
    private static final String KEY_DNS_SC_SUCC = "key_dns_sc_succ:";
    private static final String KEY_DNS_TOT_TIME = "key_dns_tot_time:";
    private static final String KEY_FIRST_CONN_INTERNERT_FAIL_CNT = "key_first_conn_fail_cnt:";
    private static final String KEY_FIRST_CONN_INTERNET_FAIL_DURATION = "key_first_conn_fail_duration:";
    private static final String KEY_GATEWAY_ABNORMAL = "key_gateway_abnormal:";
    private static final String KEY_GOOD_RECONNECTSUCC_CNT = "key_good_reconnectsucc_cnt:";
    private static final String KEY_GOOD_RECONNECT_CNT = "key_good_reconnect_cnt:";
    private static final String KEY_LAST_TIMESTAMP = "key_last_timestamp:";
    private static final String KEY_MOBILE_CONNECTED_DURATION = "key_mobile_connected_duration:";
    private static final String KEY_MOBILE_TRAFFIC_BYTES = "key_mobile_traffic_bytes:";
    private static final String KEY_MULTIGWCOUNT = "key_MultiGWCount:";
    private static final String KEY_NO_USERPROC_CNT = "key_no_user_proc_cnt:";
    private static final String KEY_ONLY_THE_TX_NO_RX_CNT = "key_only_tx_no_rx_cnt:";
    private static final String KEY_ONSCREEN_ABDICONNECT_CNT = "key_on_abdisconnected_cnt:";
    private static final String KEY_ONSCREEN_CONNECTED_CNT = "key_on_connected_cnt:";
    private static final String KEY_ONSCREEN_CONNECT_CNT = "key_on_connect_cnt:";
    private static final String KEY_ONSCREEN_CONNECT_DURATION = "key_on_connect_duration:";
    private static final String KEY_ONSCREEN_DISCONNECT_CNT = "key_on_disconnected_cnt:";
    private static final String KEY_ONSCREEN_RECONNECT_CNT = "key_on_reconnect_cnt:";
    private static final String KEY_ONSCREEN_RECONNECT_DURATION = "key_on_reconnect_duration:";
    private static final String KEY_OPEN_CNT = "key_open_cnt:";
    private static final String KEY_OPEN_DURATION = "key_open_duration:";
    private static final String KEY_OPEN_SUCC_CNT = "key_open_succ_cnt:";
    private static final String KEY_REASSOC_SC_SUCC = "key_reassoc_sc_succ:";
    private static final String KEY_REDHCP_ACCESS_WEB_SUCC_CNT = "key_redhcp_access_web_succ_cnt:";
    private static final String KEY_REDHCP_CNT = "key_redhcp_cnt:";
    private static final String KEY_REDHCP_DURATION = "key_redhcp_duration:";
    private static final String KEY_REDHCP_SUCC_CNT = "key_redhcp_succ_cnt:";
    private static final String KEY_REKEY_CNT = "key_rekey_cnt:";
    private static final String KEY_REKEY_DURATION = "key_rekey_duration:";
    private static final String KEY_REKEY_SUCC_CNT = "key_rekey_succ_cnt:";
    private static final String KEY_RESET_SC_SUCC = "key_reset_sc_succ:";
    private static final String KEY_RE_DHCP_SC_SUCC = "key_re_dhcp_sc_succ:";
    private static final String KEY_ROAMING_ABNORMAL = "key_roaming_abnormal:";
    private static final String KEY_ROAMING_ACCESS_WEB_CNT = "key_roaming_access_web_succ_cnt:";
    private static final String KEY_ROAMING_CNT = "key_roaming_cnt:";
    private static final String KEY_ROAMING_DURATION = "key_roaming_duration:";
    private static final String KEY_ROAMING_SUCC_CNT = "key_roaming_succ_cnt:";
    private static final String KEY_SSID = "key_SSID:";
    private static final String KEY_START_TIMESTAMP = "key_start_timestamp:";
    private static final String KEY_STATIC_IP_SC_SUCC = "key_static_ip_sc_succ:";
    private static final String KEY_TCP_RX_ABNORMAL = "key_tcp_rx_abnormal:";
    private static final String KEY_TIMESTAMP = "key_timestamp:";
    private static final String KEY_USER_ENABLE_STATIC_IP = "key_user_enable_static_ip:";
    private static final String KEY_USER_IN_LONGWAITED_CNT = "key_user_in_longwaiting_cnt:";
    private static final String KEY_WEAK_RECONNECTSUCC_CNT = "key_weak_reconnectsucc_cnt:";
    private static final String KEY_WEAK_RECONNECT_CNT = "key_weak_reconnect_cnt:";
    private static final String KEY_WLAN_CONNECTED_DURATION = "key_wlan_connected_duration:";
    private static final String KEY_WLAN_TRAFFIC_BYTES = "key_wlan_traffic_bytes:";
    private static final int MINLENOFDNAME;
    private static final long MIN_PERIOD_TRIGGER_BETA = 86400000;
    private static int MIN_WRITE_STAT_SPAN = 0;
    private static final long MSG_SEND_DELAY_DURATION = 1800000;
    private static final int MSG_SEND_DELAY_ID = 100;
    private static final String SEPARATOR_KEY = "\n";
    private static final String TAG = "HwWifiStatStore";
    private static HwWifiStatStore hwStatStoreIns;
    private static final String mWifiStatConf;
    private final String WLAN_IFACE;
    private String connectInternetFailedType;
    private int connectedNetwork;
    private String disConnectSSID;
    private long disconnectDate;
    private boolean isAbnormalDisconnect;
    private boolean isConnectToNetwork;
    private boolean isScreen;
    private int mCloseCnt;
    private int mCloseDuration;
    private int mCloseSuccCnt;
    private long mConnectingStartTimestamp;
    private SSIDStat mCurrentStat;
    private long mDhcpTimestamp;
    private int mDnsMaxTime;
    private int mDnsMinTime;
    private int mDnsReqCnt;
    private int mDnsReqFail;
    private int mDnsTotTime;
    private Handler mHandler;
    private int mLastConnetReason;
    private long mLastDnsStatReq;
    private int mLastUpdDHCPReason;
    private SupplicantState mLastWpaState;
    private long mMobileTotalConnectedDuration;
    private long mMobileTotalTrafficBytes;
    private int mOpenCnt;
    private int mOpenDuration;
    private int mOpenSuccCnt;
    private long mPreMobileBytes;
    private long mPreTimestamp;
    private long mPreWLANBytes;
    private SSIDStat mPreviousStat;
    private ArrayList<SSIDStat> mSSIDStatList;
    private long mTimestamp;
    private boolean mUserTypeCommercial;
    private long mWifiConnectTimestamp;
    private long mWifiConnectedTimestamp;
    private long mWifiSwitchTimestamp;
    private long mWlanTotalConnectedDuration;
    private long mWlanTotalTrafficBytes;
    private Object mWriteStatLock;
    private long mWriteStatTimestamp;
    private long onScreenTimestamp;

    private class SSIDStat {
        public String BSSID;
        public String SSID;
        private String apVendorInfo;
        private int mAbDisconnectCnt;
        private int mAccessWEBCnt;
        private int mAccessWEBSuccCnt;
        private int mAccessWebFailedPortal;
        private int mAccessWebReDHCPFailedPortal;
        private int mAccessWebRoamingFailedPortal;
        private int mAppDisabledAbnromalCnt;
        private int mAppDisabledScSuccCnt;
        private int mArpReassocOkCnt;
        private int mArpUnreachableCnt;
        private int mAssocByABSCnt;
        public int mAssocCnt;
        public int mAssocDuration;
        private int mAssocRejectAccessFullCnt;
        private int mAssocRejectedAbnormalCnt;
        private int mAssocRejectedScSuccCnt;
        public int mAssocSuccCnt;
        public long mAssocingTimestamp;
        public int mAuthCnt;
        public int mAuthDuration;
        private int mAuthFailedAbnormalCnt;
        private int mAuthFailedScSuccCnt;
        public int mAuthSuccCnt;
        public int mCHRConnectingDuration;
        private Date mConStart;
        public int mConnectTotalCnt;
        public int mConnectedCnt;
        public int mConnectedDuration;
        public int mConnectingDuration;
        public int mDhcpAutoIpCnt;
        public int mDhcpCnt;
        public int mDhcpDuration;
        private int mDhcpFailedAbnormalCnt;
        private int mDhcpFailedScSuccCnt;
        public int mDhcpStaticCnt;
        public int mDhcpStaticSuccCnt;
        public int mDhcpSuccCnt;
        public int mDisconnectCnt;
        private int mDnsAbnormalCnt;
        private int mDnsParseFailCnt;
        private int mDnsScSuccCnt;
        private int mFirstConnInternetFailCnt;
        public int mFirstConnInternetFailDuration;
        private int mGatewayAbnormalCnt;
        private int mGoodReConnectCnt;
        private int mGoodReConnectSuccCnt;
        private boolean mIsWifiproFlag;
        private Date mLastUpdate;
        private int mMultiGWCount;
        private int mNoUserProcCnt;
        private int mOnScreenAbDisconnectCnt;
        private int mOnScreenConnectCnt;
        private int mOnScreenConnectDuration;
        private int mOnScreenConnectedCnt;
        private int mOnScreenDisconnectCnt;
        private int mOnScreenReConnectDuration;
        private int mOnScreenReConnectedCnt;
        private int mOnlyTheTxNoRxCnt;
        private int mReDHCPAccessWebSuccCnt;
        private int mReDHCPCnt;
        private int mReDHCPDuration;
        private int mReDHCPSuccCnt;
        private int mReDhcpScSuccCnt;
        private int mReKEYCnt;
        private int mReKEYDuration;
        private int mReKEYSuccCnt;
        private int mReassocScCnt;
        private int mResetScSuccCnt;
        private int mRoamingAbnormalCnt;
        private int mRoamingAccessWebSuccCnt;
        private int mRoamingCnt;
        private int mRoamingDuration;
        private int mRoamingSuccCnt;
        private int mStaticIpScSuccCnt;
        private int mTcpRxAbnormalCnt;
        private int mUserEnableStaticIpCnt;
        private int mUserLongTimeWaitedCnt;
        private int mWeakReConnectCnt;
        private int mWeakReConnectSuccCnt;
        private String strAP_auth_alg;
        private String strAP_eap;
        private String strAP_gruop;
        private String strAP_key_mgmt;
        private String strAP_pairwise;
        private String strAP_proto;

        private SSIDStat() {
            this.SSID = "";
            this.BSSID = "";
            this.mAssocCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mAssocSuccCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mAuthCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mAuthSuccCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mDhcpCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mDhcpSuccCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mDhcpStaticCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mDhcpAutoIpCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mAssocingTimestamp = 0;
            this.mConnectingDuration = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mDhcpStaticSuccCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mConnectedCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mDisconnectCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mAssocDuration = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mAuthDuration = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mDhcpDuration = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mConnectedDuration = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mFirstConnInternetFailDuration = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mConnectTotalCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mCHRConnectingDuration = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mRoamingCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mRoamingSuccCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mRoamingDuration = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mReDHCPCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mReDHCPSuccCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mReDHCPDuration = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mReKEYCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mReKEYSuccCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mReKEYDuration = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mWeakReConnectCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mWeakReConnectSuccCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mGoodReConnectCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mGoodReConnectSuccCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mOnScreenConnectCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mOnScreenConnectedCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mOnScreenAbDisconnectCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mOnScreenReConnectedCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mOnScreenDisconnectCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mOnScreenConnectDuration = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mOnScreenReConnectDuration = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mAccessWEBCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mAccessWEBSuccCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mFirstConnInternetFailCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mOnlyTheTxNoRxCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mDnsParseFailCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mArpUnreachableCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mArpReassocOkCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mDnsAbnormalCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mTcpRxAbnormalCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mRoamingAbnormalCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mGatewayAbnormalCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mDnsScSuccCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mReDhcpScSuccCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mStaticIpScSuccCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mReassocScCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mResetScSuccCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mUserEnableStaticIpCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mAuthFailedAbnormalCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mAssocRejectedAbnormalCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mDhcpFailedAbnormalCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mAppDisabledAbnromalCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mAuthFailedScSuccCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mAssocRejectedScSuccCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mDhcpFailedScSuccCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mAppDisabledScSuccCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.apVendorInfo = "";
            this.strAP_proto = "";
            this.strAP_key_mgmt = "";
            this.strAP_auth_alg = "";
            this.strAP_pairwise = "";
            this.strAP_gruop = "";
            this.strAP_eap = "";
            this.mConStart = new Date();
            this.mLastUpdate = null;
            this.mRoamingAccessWebSuccCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mReDHCPAccessWebSuccCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mNoUserProcCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mUserLongTimeWaitedCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mMultiGWCount = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mAccessWebFailedPortal = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mAccessWebRoamingFailedPortal = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mAccessWebReDHCPFailedPortal = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mAbDisconnectCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mIsWifiproFlag = false;
            this.mAssocRejectAccessFullCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
            this.mAssocByABSCnt = HwWifiStatStoreImpl.MINLENOFDNAME;
        }

        public boolean cmp(String ssid) {
            if (ssid == null || this.SSID == null) {
                return false;
            }
            return this.SSID.equals(ssid);
        }

        public boolean hasDataToTrigger() {
            if ((((((this.mAssocCnt + this.mAssocSuccCnt) + this.mAuthCnt) + this.mAuthSuccCnt) + (((this.mDhcpCnt + this.mDhcpSuccCnt) + this.mDhcpStaticCnt) + this.mDhcpAutoIpCnt)) + ((this.mConnectedCnt + this.mDisconnectCnt) + this.mAccessWEBSuccCnt)) + ((this.mRoamingAccessWebSuccCnt + this.mReDHCPAccessWebSuccCnt) + this.mAbDisconnectCnt) > 0) {
                return true;
            }
            return false;
        }
    }

    static {
        mWifiStatConf = Environment.getDataDirectory() + "/misc/wifi/wifiStatistics.txt";
        hwStatStoreIns = new HwWifiStatStoreImpl();
        MIN_WRITE_STAT_SPAN = HwWifiStateMachine.AP_CAP_CACHE_COUNT;
        MINLENOFDNAME = DNAMESTR.length();
    }

    public static HwWifiStatStore getDefault() {
        return hwStatStoreIns;
    }

    private HwWifiStatStoreImpl() {
        this.WLAN_IFACE = SystemProperties.get("wifi.interface", DEFAULT_WLAN_IFACE);
        this.mTimestamp = 0;
        this.mOpenCnt = MINLENOFDNAME;
        this.mOpenSuccCnt = MINLENOFDNAME;
        this.mCloseCnt = MINLENOFDNAME;
        this.mCloseSuccCnt = MINLENOFDNAME;
        this.mOpenDuration = MINLENOFDNAME;
        this.mCloseDuration = MINLENOFDNAME;
        this.mDnsReqCnt = MINLENOFDNAME;
        this.mDnsReqFail = MINLENOFDNAME;
        this.mDnsMaxTime = MINLENOFDNAME;
        this.mDnsMinTime = MINLENOFDNAME;
        this.mDnsTotTime = MINLENOFDNAME;
        this.disconnectDate = 0;
        this.isAbnormalDisconnect = false;
        this.disConnectSSID = "";
        this.isScreen = false;
        this.onScreenTimestamp = 0;
        this.mConnectingStartTimestamp = 0;
        this.mPreWLANBytes = 0;
        this.mPreMobileBytes = 0;
        this.mWlanTotalTrafficBytes = 0;
        this.mMobileTotalTrafficBytes = 0;
        this.mPreTimestamp = 0;
        this.mWlanTotalConnectedDuration = 0;
        this.mMobileTotalConnectedDuration = 0;
        this.connectedNetwork = MINLENOFDNAME;
        this.isConnectToNetwork = false;
        this.connectInternetFailedType = "CONNECT_INTERNET_INITIAL";
        this.mCurrentStat = null;
        this.mPreviousStat = null;
        this.mSSIDStatList = new ArrayList();
        this.mLastConnetReason = MINLENOFDNAME;
        this.mLastUpdDHCPReason = MINLENOFDNAME;
        this.mUserTypeCommercial = true;
        this.mWriteStatTimestamp = 0;
        this.mWifiSwitchTimestamp = 0;
        this.mWifiConnectTimestamp = 0;
        this.mWifiConnectedTimestamp = 0;
        this.mDhcpTimestamp = 0;
        this.mWriteStatLock = new Object();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HwWifiStatStoreImpl.MSG_SEND_DELAY_ID /*100*/:
                        HwWifiStatStoreImpl.this.triggerTotalConnetedDuration(HwWifiStatStoreImpl.this.connectedNetwork);
                        HwWifiStatStoreImpl.this.triggerTotalTrafficBytes();
                        HwWifiStatStoreImpl.this.triggerConnectedDuration(SystemClock.elapsedRealtime(), HwWifiStatStoreImpl.this.mCurrentStat);
                        HwWifiStatStoreImpl.this.writeWifiCHRStat(true, true);
                        HwWifiStatStoreImpl.this.mHandler.sendEmptyMessageDelayed(HwWifiStatStoreImpl.MSG_SEND_DELAY_ID, HwWifiStatStoreImpl.MSG_SEND_DELAY_DURATION);
                    default:
                }
            }
        };
        this.mLastDnsStatReq = 0;
    }

    public void updateScreenState(boolean on) {
        this.isScreen = on;
    }

    private void rstDisconnectFlg() {
        this.disconnectDate = 0;
        this.isAbnormalDisconnect = false;
        this.disConnectSSID = "";
    }

    public void setMultiGWCount(byte count) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            stat.mMultiGWCount = count;
        }
    }

    public void incrWebSpeedStatus(int addNoUsrCnt, int addLongWaitingCnt) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            stat.mNoUserProcCnt = stat.mNoUserProcCnt + addNoUsrCnt;
            stat.mUserLongTimeWaitedCnt = stat.mUserLongTimeWaitedCnt + addLongWaitingCnt;
        }
    }

    public int getLastUpdDHCPReason() {
        return this.mLastUpdDHCPReason;
    }

    public int getLastUpdConnectReason() {
        return this.mLastConnetReason;
    }

    public void updateUserType(boolean commercialUser) {
        this.mUserTypeCommercial = commercialUser;
        LOGW("This method is no longer use");
    }

    public void updateConnectState(boolean connected) {
        SSIDStat stat = this.mCurrentStat;
        boolean flushNow = false;
        if (stat != null && !stat.mIsWifiproFlag) {
            LOGD("updateConnectState connected: " + connected + " mLastUpdDHCPReason:" + this.mLastUpdDHCPReason);
            long now = SystemClock.elapsedRealtime();
            if (connected) {
                if (this.mLastUpdDHCPReason == HW_CONNECT_REASON_ROAMING || this.mLastUpdDHCPReason == 8) {
                    stat.mConnectedCnt += HW_CONNECT_REASON_CONNECTING;
                    stat.mConnectingDuration += (int) (now - stat.mAssocingTimestamp);
                    this.mWifiConnectedTimestamp = now;
                    stat.mConnectTotalCnt += HW_CONNECT_REASON_CONNECTING;
                    this.isConnectToNetwork = true;
                    if (this.isScreen && this.onScreenTimestamp > 0) {
                        stat.mOnScreenConnectedCnt = stat.mOnScreenConnectedCnt + HW_CONNECT_REASON_CONNECTING;
                        stat.mOnScreenConnectDuration = stat.mOnScreenConnectDuration + ((int) (now - this.onScreenTimestamp));
                        this.onScreenTimestamp = 0;
                    }
                    flushNow = true;
                }
                triggerConnectedDuration(now, stat);
                writeWifiCHRStat(flushNow, false);
            } else {
                updateDisconnectCnt();
                if (this.mWifiConnectedTimestamp > 0) {
                    stat.mConnectedDuration = (int) (((long) stat.mConnectedDuration) + (now - this.mWifiConnectedTimestamp));
                    this.mWifiConnectedTimestamp = 0;
                    this.isAbnormalDisconnect = true;
                    if (this.isScreen) {
                        stat.mOnScreenAbDisconnectCnt = stat.mOnScreenAbDisconnectCnt + HW_CONNECT_REASON_CONNECTING;
                    }
                }
                triggerConnectedDuration(now, stat);
                updateConnectInternetFailedType("CONNECT_INTERNET_INITIAL");
                writeWifiCHRStat(false, true);
            }
        }
    }

    public void updateCHRConnectFailedCount(int type) {
        SSIDStat stat = null;
        if (type == 0) {
            stat = this.mCurrentStat;
        } else if (type == HW_CONNECT_REASON_CONNECTING) {
            stat = this.mPreviousStat;
        }
        if (stat != null) {
            stat.mConnectTotalCnt += HW_CONNECT_REASON_CONNECTING;
        }
    }

    private SSIDStat geStatBySSID(String SSID) {
        for (int i = MINLENOFDNAME; i < this.mSSIDStatList.size(); i += HW_CONNECT_REASON_CONNECTING) {
            SSIDStat item = (SSIDStat) this.mSSIDStatList.get(i);
            if (item.cmp(SSID)) {
                return item;
            }
        }
        return null;
    }

    public void setAPSSID(String ssid) {
        if (!TextUtils.isEmpty(ssid) && ssid.length() > 0 && (this.mCurrentStat == null || !ssid.equals(this.mCurrentStat.SSID))) {
            this.mPreviousStat = this.mCurrentStat;
            this.mCurrentStat = geStatBySSID(ssid);
            if (this.mCurrentStat == null) {
                this.mCurrentStat = new SSIDStat();
                this.mCurrentStat.SSID = ssid;
                this.mSSIDStatList.add(this.mCurrentStat);
            }
            LOGD("setAPSSID: " + ssid);
        }
    }

    public void updateAssocByABS() {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            stat.mAssocByABSCnt = stat.mAssocByABSCnt + HW_CONNECT_REASON_CONNECTING;
        }
    }

    private void incrAccesFailedByPortal(int reason, boolean isFailedByPortal, SSIDStat stat) {
        if (isFailedByPortal) {
            switch (reason) {
                case MINLENOFDNAME:
                    stat.mAccessWebFailedPortal = stat.mAccessWebFailedPortal + HW_CONNECT_REASON_CONNECTING;
                    break;
                case HW_CONNECT_REASON_ROAMING /*2*/:
                    stat.mAccessWebRoamingFailedPortal = stat.mAccessWebRoamingFailedPortal + HW_CONNECT_REASON_CONNECTING;
                    break;
                case HW_CONNECT_REASON_REKEY /*3*/:
                    stat.mAccessWebReDHCPFailedPortal = stat.mAccessWebReDHCPFailedPortal + HW_CONNECT_REASON_CONNECTING;
                    break;
            }
        }
    }

    public void incrAccessWebRecord(int reason, boolean succ, boolean isFailedByPortal) {
        LOGD(" incrAccessWebRecord mCurrentStat= " + this.mCurrentStat + " succ=" + succ + "  reason=" + reason);
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            if (reason == 0) {
                stat.mAccessWEBCnt = stat.mAccessWEBCnt + HW_CONNECT_REASON_CONNECTING;
            }
            if (succ) {
                switch (reason) {
                    case MINLENOFDNAME:
                        stat.mAccessWEBSuccCnt = stat.mAccessWEBSuccCnt + HW_CONNECT_REASON_CONNECTING;
                        break;
                    case HW_CONNECT_REASON_ROAMING /*2*/:
                        stat.mRoamingAccessWebSuccCnt = stat.mRoamingAccessWebSuccCnt + HW_CONNECT_REASON_CONNECTING;
                        break;
                    case HW_CONNECT_REASON_REKEY /*3*/:
                        stat.mReDHCPAccessWebSuccCnt = stat.mReDHCPAccessWebSuccCnt + HW_CONNECT_REASON_CONNECTING;
                        break;
                }
                triggerConnectedDuration(SystemClock.elapsedRealtime(), stat);
                writeWifiCHRStat(false, false);
                return;
            }
            incrAccesFailedByPortal(reason, isFailedByPortal, stat);
        }
    }

    public void setApVendorInfo(String apVendorInfo) {
        SSIDStat stat = this.mCurrentStat;
        if (stat == null) {
            LOGD("ignore setApVendorInfo because mCurrentStat is null, apVendorInfo:" + apVendorInfo);
            return;
        }
        int dnameIndex = stat.apVendorInfo.indexOf(DNAMESTR);
        if (dnameIndex < 0 || stat.apVendorInfo.substring(MINLENOFDNAME + dnameIndex).equals("")) {
            stat.apVendorInfo = apVendorInfo;
        } else {
            LOGD("ignore setApVendorInfo because dname is not null,apVendorInfo:" + apVendorInfo);
        }
    }

    public void setApencInfo(String strAP_proto, String strAP_key_mgmt, String strAP_auth_alg, String strAP_pairwise, String strAP_gruop, String strAP_eap) {
        SSIDStat stat = this.mCurrentStat;
        if (stat == null) {
            LOGD("ignore setApencInfo because mCurrentStat is null, strAP_key_mgmt:" + strAP_key_mgmt);
            return;
        }
        stat.strAP_proto = strAP_proto;
        stat.strAP_key_mgmt = strAP_key_mgmt;
        stat.strAP_auth_alg = strAP_auth_alg;
        stat.strAP_pairwise = strAP_pairwise;
        stat.strAP_gruop = strAP_gruop;
        stat.strAP_eap = strAP_eap;
    }

    public void setCHRConnectingSartTimestamp(long connectingStartTimestamp) {
        if (connectingStartTimestamp > 0) {
            this.mConnectingStartTimestamp = connectingStartTimestamp;
        }
    }

    private void addReConnectCnt() {
        long now = SystemClock.elapsedRealtime();
        if (this.disconnectDate == 0 || now - this.disconnectDate >= 60000) {
            LOGD("addReConnectCnt return disconnectDate=" + this.disconnectDate + ", now=" + now);
            rstDisconnectFlg();
            return;
        }
        SSIDStat stat = geStatBySSID(this.disConnectSSID);
        if (stat == null) {
            stat = new SSIDStat();
            stat.SSID = this.disConnectSSID;
            this.mSSIDStatList.add(stat);
        }
        LOGD("addReConnectCnt return disConnectSSID=" + this.disConnectSSID + ", isAbnormalDisconnect=" + this.isAbnormalDisconnect);
        if (this.isAbnormalDisconnect) {
            stat.mGoodReConnectCnt = stat.mGoodReConnectCnt + HW_CONNECT_REASON_CONNECTING;
        } else {
            stat.mWeakReConnectCnt = stat.mWeakReConnectCnt + HW_CONNECT_REASON_CONNECTING;
        }
    }

    private void addReConnectSuccCnt() {
        long now = SystemClock.elapsedRealtime();
        if (this.disconnectDate == 0 || now - this.disconnectDate >= 60000) {
            LOGD("addReConnectCnt return disconnectDate=" + this.disconnectDate + ", now=" + now);
            rstDisconnectFlg();
            return;
        }
        SSIDStat stat = geStatBySSID(this.disConnectSSID);
        if (stat == null) {
            stat = new SSIDStat();
            stat.SSID = this.disConnectSSID;
            this.mSSIDStatList.add(stat);
        }
        LOGD("addReConnectSuccCnt  isAbnormalDisconnect=" + this.isAbnormalDisconnect);
        if (this.isAbnormalDisconnect) {
            if (this.isScreen || this.onScreenTimestamp > 0) {
                stat.mOnScreenReConnectedCnt = stat.mOnScreenReConnectedCnt + HW_CONNECT_REASON_CONNECTING;
                stat.mOnScreenReConnectDuration = (int) (((long) stat.mOnScreenReConnectDuration) + (now - this.disconnectDate));
            }
            stat.mGoodReConnectSuccCnt = stat.mGoodReConnectSuccCnt + HW_CONNECT_REASON_CONNECTING;
        } else {
            stat.mWeakReConnectSuccCnt = stat.mWeakReConnectSuccCnt + HW_CONNECT_REASON_CONNECTING;
        }
    }

    private void triggerConnectedDuration(long now, SSIDStat stat) {
        if (stat != null && this.mWifiConnectedTimestamp > 0) {
            stat.mConnectedDuration = (int) (((long) stat.mConnectedDuration) + (now - this.mWifiConnectedTimestamp));
            if ("FIRST_CONNECT_INTERNET_FAILED".equals(this.connectInternetFailedType)) {
                stat.mFirstConnInternetFailDuration = (int) (((long) stat.mFirstConnInternetFailDuration) + (now - this.mWifiConnectedTimestamp));
            }
            this.mWifiConnectedTimestamp = now;
        }
    }

    public void triggerConnectedDuration() {
        SSIDStat stat = this.mCurrentStat;
        long now = SystemClock.elapsedRealtime();
        if (stat != null && this.mWifiConnectedTimestamp > 0) {
            stat.mConnectedDuration = (int) (((long) stat.mConnectedDuration) + (now - this.mWifiConnectedTimestamp));
            if ("FIRST_CONNECT_INTERNET_FAILED".equals(this.connectInternetFailedType)) {
                stat.mFirstConnInternetFailDuration = (int) (((long) stat.mFirstConnInternetFailDuration) + (now - this.mWifiConnectedTimestamp));
            }
            this.mWifiConnectedTimestamp = now;
            triggerTotalTrafficBytes();
            triggerTotalConnetedDuration(this.connectedNetwork);
            writeWifiCHRStat(true, false);
        }
    }

    public void triggerCHRConnectingDuration(long connectingDuration) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null && connectingDuration > 0) {
            HwWifiCHRStateManager hwWifiCHRStateManager = HwWifiCHRStateManagerImpl.getDefault();
            if (hwWifiCHRStateManager != null) {
                hwWifiCHRStateManager.updateConnectSuccessTime(connectingDuration);
            }
            stat.mCHRConnectingDuration = (int) (((long) stat.mCHRConnectingDuration) + connectingDuration);
        }
    }

    public void setAbDisconnectFlg(String AP_SSID) {
        SSIDStat stat = this.mCurrentStat;
        this.isAbnormalDisconnect = true;
        this.disConnectSSID = AP_SSID;
        this.disconnectDate = SystemClock.elapsedRealtime();
        if (stat != null) {
            stat.mAbDisconnectCnt = stat.mAbDisconnectCnt + HW_CONNECT_REASON_CONNECTING;
        }
    }

    public void updateCurrentConnectType(int type) {
        long now = SystemClock.elapsedRealtime();
        switch (type) {
            case MINLENOFDNAME:
                triggerTotalTrafficBytes();
                triggerTotalConnetedDuration(this.connectedNetwork);
                this.connectedNetwork = MINLENOFDNAME;
                this.mHandler.removeMessages(MSG_SEND_DELAY_ID);
            case HW_CONNECT_REASON_CONNECTING /*1*/:
                this.mPreTimestamp = now;
                this.connectedNetwork = HW_CONNECT_REASON_CONNECTING;
                this.mHandler.removeMessages(MSG_SEND_DELAY_ID);
                this.mHandler.sendEmptyMessageDelayed(MSG_SEND_DELAY_ID, MSG_SEND_DELAY_DURATION);
            case HW_CONNECT_REASON_ROAMING /*2*/:
                this.mPreTimestamp = now;
                triggerTotalTrafficBytes();
                this.connectedNetwork = HW_CONNECT_REASON_ROAMING;
                this.mHandler.removeMessages(MSG_SEND_DELAY_ID);
                this.mHandler.sendEmptyMessageDelayed(MSG_SEND_DELAY_ID, MSG_SEND_DELAY_DURATION);
            default:
        }
    }

    public void triggerTotalTrafficBytes() {
        long currentMobileTrafficBytes = (TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes()) - this.mPreMobileBytes;
        long currentWlanTrafficByts = (TrafficStats.getTxBytes(this.WLAN_IFACE) + TrafficStats.getRxBytes(this.WLAN_IFACE)) - this.mPreWLANBytes;
        if (currentMobileTrafficBytes > 0) {
            this.mMobileTotalTrafficBytes += currentMobileTrafficBytes;
        }
        if (currentWlanTrafficByts > 0) {
            this.mWlanTotalTrafficBytes += currentWlanTrafficByts;
        }
        if (TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes() > 0) {
            this.mPreMobileBytes = TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes();
        }
        if (TrafficStats.getTxBytes(this.WLAN_IFACE) + TrafficStats.getRxBytes(this.WLAN_IFACE) > 0) {
            this.mPreWLANBytes = TrafficStats.getTxBytes(this.WLAN_IFACE) + TrafficStats.getRxBytes(this.WLAN_IFACE);
        }
    }

    public void triggerTotalConnetedDuration(int connectedType) {
        long now = SystemClock.elapsedRealtime();
        if (connectedType != 0) {
            switch (connectedType) {
                case HW_CONNECT_REASON_CONNECTING /*1*/:
                    long currentWlanConnectedDuration = now - this.mPreTimestamp;
                    if (currentWlanConnectedDuration > 0) {
                        this.mWlanTotalConnectedDuration += currentWlanConnectedDuration;
                        this.mPreTimestamp = now;
                        break;
                    }
                case HW_CONNECT_REASON_ROAMING /*2*/:
                    long currentMobileConnectedDuration = now - this.mPreTimestamp;
                    if (currentMobileConnectedDuration > 0) {
                        this.mMobileTotalConnectedDuration += currentMobileConnectedDuration;
                        this.mPreTimestamp = now;
                        break;
                    }
            }
        }
    }

    public void setApMac(String apMac) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            if (LogManager.getInstance().isCommercialUser()) {
                apMac = maskMacAddress(apMac);
            }
            stat.BSSID = apMac;
        }
    }

    private String maskMacAddress(String macAddress) {
        if (macAddress != null) {
            if (macAddress.split(":").length >= 4) {
                return String.format("%s:%s:%s:%s:FF:FF", new Object[]{macAddress.split(":")[MINLENOFDNAME], macAddress.split(":")[HW_CONNECT_REASON_CONNECTING], macAddress.split(":")[HW_CONNECT_REASON_ROAMING], macAddress.split(":")[HW_CONNECT_REASON_REKEY]});
            }
        }
        return macAddress;
    }

    public void updateConnectInternetFailedType(String reasonType) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            if (reasonType.equals("CONNECT_INTERNET_INITIAL")) {
                this.connectInternetFailedType = "CONNECT_INTERNET_INITIAL";
            } else if (reasonType.equals("FIRST_CONNECT_INTERNET_FAILED")) {
                this.connectInternetFailedType = "FIRST_CONNECT_INTERNET_FAILED";
                stat.mFirstConnInternetFailCnt = stat.mFirstConnInternetFailCnt + HW_CONNECT_REASON_CONNECTING;
            } else if (reasonType.equals("ONLY_THE_TX_NO_RX")) {
                this.connectInternetFailedType = "ONLY_THE_TX_NO_RX";
                stat.mOnlyTheTxNoRxCnt = stat.mOnlyTheTxNoRxCnt + HW_CONNECT_REASON_CONNECTING;
            } else if (reasonType.equals("DNS_PARSE_FAILED")) {
                this.connectInternetFailedType = "DNS_PARSE_FAILED";
                stat.mDnsParseFailCnt = stat.mDnsParseFailCnt + HW_CONNECT_REASON_CONNECTING;
            } else if (reasonType.equals("ARP_UNREACHABLE")) {
                this.connectInternetFailedType = "ARP_UNREACHABLE";
                stat.mArpUnreachableCnt = stat.mArpUnreachableCnt + HW_CONNECT_REASON_CONNECTING;
            } else if (reasonType.equals("ARP_REASSOC_OK")) {
                this.connectInternetFailedType = "ARP_REASSOC_OK";
                stat.mArpReassocOkCnt = stat.mArpReassocOkCnt + HW_CONNECT_REASON_CONNECTING;
            }
            writeWifiCHRStat(true, false);
        }
    }

    public void handleSupplicantStateChange(SupplicantState state, boolean wifiprotempflag) {
        SSIDStat stat = this.mCurrentStat;
        if (stat == null) {
            LOGD("ignore handleSupplicantStateChange because stat is null, state:" + state);
            return;
        }
        if (state == SupplicantState.ASSOCIATING) {
            stat.mIsWifiproFlag = wifiprotempflag;
        }
        if (!stat.mIsWifiproFlag) {
            long now = SystemClock.elapsedRealtime();
            boolean triggerChr = false;
            if (state == SupplicantState.ASSOCIATING) {
                this.mLastConnetReason = HW_CONNECT_REASON_CONNECTING;
                stat.mAssocCnt += HW_CONNECT_REASON_CONNECTING;
                addReConnectCnt();
                this.onScreenTimestamp = 0;
                if (this.isScreen) {
                    stat.mOnScreenConnectCnt = stat.mOnScreenConnectCnt + HW_CONNECT_REASON_CONNECTING;
                    this.onScreenTimestamp = now;
                }
                this.mWifiConnectTimestamp = now;
                stat.mAssocingTimestamp = this.mWifiConnectTimestamp;
            } else if (state == SupplicantState.ASSOCIATED) {
                if (this.mLastWpaState == SupplicantState.ASSOCIATING) {
                    stat.mAssocSuccCnt += HW_CONNECT_REASON_CONNECTING;
                    stat.mAssocDuration += (int) (now - this.mWifiConnectTimestamp);
                    stat.mAuthCnt += HW_CONNECT_REASON_CONNECTING;
                    this.mWifiConnectTimestamp = now;
                } else if (this.mLastWpaState == SupplicantState.COMPLETED) {
                    this.mLastConnetReason = HW_CONNECT_REASON_ROAMING;
                    stat.mRoamingCnt = stat.mRoamingCnt + HW_CONNECT_REASON_CONNECTING;
                    this.mWifiConnectTimestamp = now;
                }
            } else if (state == SupplicantState.COMPLETED) {
                if (HW_CONNECT_REASON_CONNECTING == this.mLastConnetReason) {
                    stat.mAuthSuccCnt += HW_CONNECT_REASON_CONNECTING;
                    stat.mAuthDuration += (int) (now - this.mWifiConnectTimestamp);
                    this.mWifiConnectTimestamp = now;
                    addReConnectSuccCnt();
                    rstDisconnectFlg();
                } else if (HW_CONNECT_REASON_ROAMING == this.mLastConnetReason) {
                    stat.mRoamingSuccCnt = stat.mRoamingSuccCnt + HW_CONNECT_REASON_CONNECTING;
                    stat.mRoamingDuration = stat.mRoamingDuration + ((int) (now - this.mWifiConnectTimestamp));
                    this.mWifiConnectTimestamp = now;
                    triggerChr = false;
                } else if (HW_CONNECT_REASON_REKEY == this.mLastConnetReason) {
                    stat.mReKEYSuccCnt = stat.mReKEYSuccCnt + HW_CONNECT_REASON_CONNECTING;
                    stat.mReKEYDuration = stat.mReKEYDuration + ((int) (now - this.mWifiConnectTimestamp));
                    this.mWifiConnectTimestamp = now;
                    triggerChr = false;
                }
                this.mLastConnetReason = MINLENOFDNAME;
            } else if (state == SupplicantState.FOUR_WAY_HANDSHAKE || state == SupplicantState.GROUP_HANDSHAKE) {
                if (this.mLastWpaState == SupplicantState.COMPLETED) {
                    this.mLastConnetReason = HW_CONNECT_REASON_REKEY;
                    stat.mReKEYCnt = stat.mReKEYCnt + HW_CONNECT_REASON_CONNECTING;
                    this.mWifiConnectTimestamp = now;
                }
            } else if (state == SupplicantState.DISCONNECTED) {
                this.disConnectSSID = stat.SSID;
                if (this.mLastConnetReason != 0) {
                    this.mWifiConnectTimestamp = now;
                    triggerChr = true;
                    this.mLastConnetReason = MINLENOFDNAME;
                }
                if (this.mWifiConnectedTimestamp > 0) {
                    stat.mConnectedDuration = (int) (((long) stat.mConnectedDuration) + (now - this.mWifiConnectedTimestamp));
                    this.mWifiConnectedTimestamp = 0;
                    this.disconnectDate = now;
                    triggerChr = false;
                    if (this.isScreen) {
                        stat.mOnScreenDisconnectCnt = stat.mOnScreenDisconnectCnt + HW_CONNECT_REASON_CONNECTING;
                    }
                }
            }
            stat.mLastUpdate = new Date();
            this.mLastWpaState = state;
            if (this.mWifiConnectTimestamp == now) {
                triggerConnectedDuration(now, stat);
                writeWifiCHRStat(false, triggerChr);
            }
        }
    }

    public void updateDhcpState(int state) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null && !stat.mIsWifiproFlag) {
            long now = SystemClock.elapsedRealtime();
            boolean triggerChr = false;
            if (state == 0) {
                this.mDhcpTimestamp = now;
            } else if (state == HW_CONNECT_REASON_ROAMING) {
                stat.mDhcpDuration += (int) (now - this.mDhcpTimestamp);
                stat.mDhcpCnt += HW_CONNECT_REASON_CONNECTING;
                stat.mDhcpSuccCnt += HW_CONNECT_REASON_CONNECTING;
                triggerChr = false;
            } else if (state == HW_CONNECT_REASON_REKEY) {
                stat.mReDHCPDuration = stat.mReDHCPDuration + ((int) (now - this.mDhcpTimestamp));
                stat.mReDHCPCnt = stat.mReDHCPCnt + HW_CONNECT_REASON_CONNECTING;
                stat.mReDHCPSuccCnt = stat.mReDHCPSuccCnt + HW_CONNECT_REASON_CONNECTING;
            } else if (state == 4) {
                stat.mDhcpCnt += HW_CONNECT_REASON_CONNECTING;
            } else if (state == 5) {
                stat.mReDHCPCnt = stat.mReDHCPCnt + HW_CONNECT_REASON_CONNECTING;
            } else if (state == 8) {
                stat.mDhcpStaticCnt += HW_CONNECT_REASON_CONNECTING;
            } else if (state == 16) {
                stat.mDhcpAutoIpCnt += HW_CONNECT_REASON_CONNECTING;
                stat.mDhcpStaticSuccCnt = stat.mDhcpAutoIpCnt;
            } else {
                return;
            }
            this.mLastUpdDHCPReason = state;
            triggerConnectedDuration(now, stat);
            writeWifiCHRStat(false, triggerChr);
        }
    }

    public void updateWifiState(int state) {
    }

    public void updateWifiState(boolean enable, boolean success) {
        if (enable) {
            this.mOpenCnt += HW_CONNECT_REASON_CONNECTING;
            if (success) {
                this.mOpenSuccCnt += HW_CONNECT_REASON_CONNECTING;
                this.mOpenDuration += (int) (SystemClock.elapsedRealtime() - this.mWifiSwitchTimestamp);
            }
            triggerConnectedDuration(SystemClock.elapsedRealtime(), this.mCurrentStat);
            writeWifiCHRStat(true, false);
        } else {
            this.mCloseCnt += HW_CONNECT_REASON_CONNECTING;
            if (success) {
                this.mCloseSuccCnt += HW_CONNECT_REASON_CONNECTING;
                this.mCloseDuration += (int) (SystemClock.elapsedRealtime() - this.mWifiSwitchTimestamp);
                updateDisconnectCnt();
            }
            triggerConnectedDuration(SystemClock.elapsedRealtime(), this.mCurrentStat);
            updateConnectInternetFailedType("CONNECT_INTERNET_INITIAL");
            this.mWifiConnectedTimestamp = 0;
            writeWifiCHRStat(true, true);
        }
        this.mWifiSwitchTimestamp = SystemClock.elapsedRealtime();
    }

    public void handleWiFiDnsStats(int netid) {
        long now = SystemClock.elapsedRealtime();
        if (netid == 0) {
            this.mLastDnsStatReq = now;
            return;
        }
        if (netid >= 0) {
            if (now - this.mLastDnsStatReq >= 300000) {
                this.mLastDnsStatReq = now;
                String dnsstats = "";
                try {
                    dnsstats = HwFrameworkFactory.getHwInnerNetworkManager().getWiFiDnsStats(netid);
                } catch (Exception e) {
                    Log.e(TAG, "Exception in handleWiFiDnsStats");
                }
                if (!TextUtils.isEmpty(dnsstats)) {
                    String[] stats = dnsstats.split(";");
                    int i = MINLENOFDNAME;
                    while (true) {
                        int length = stats.length;
                        if (i >= r0) {
                            break;
                        }
                        String[] statItem = stats[i].split(",");
                        length = statItem.length;
                        if (r0 == 6) {
                            try {
                                int reqcnt = Integer.parseInt(statItem[HW_CONNECT_REASON_CONNECTING]);
                                int fcnt = Integer.parseInt(statItem[HW_CONNECT_REASON_ROAMING]);
                                int max = Integer.parseInt(statItem[HW_CONNECT_REASON_REKEY]);
                                int min = Integer.parseInt(statItem[4]);
                                int tot = Integer.parseInt(statItem[5]);
                                this.mDnsReqCnt += reqcnt;
                                this.mDnsReqFail += fcnt;
                                this.mDnsMaxTime += max;
                                this.mDnsMinTime += min;
                                this.mDnsTotTime += tot;
                            } catch (NumberFormatException e2) {
                            }
                        }
                        i += HW_CONNECT_REASON_CONNECTING;
                    }
                    writeWifiCHRStat(true, false);
                }
            }
        }
    }

    public void updateWifiTriggerState(boolean enable) {
        this.mWifiSwitchTimestamp = SystemClock.elapsedRealtime();
        if (!enable) {
            HwWifiCHRStateManagerImpl.getDefault().clearDisconnectData();
        }
    }

    public void updateReasonCode(int EventId, int reasonCode) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null && !stat.mIsWifiproFlag && EventId == 83 && reasonCode == ASSOC_REJECT_ACCESSFULL) {
            stat.mAssocRejectAccessFullCnt = stat.mAssocRejectAccessFullCnt + HW_CONNECT_REASON_CONNECTING;
        }
    }

    public void updateScCHRCount(int type) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            switch (type) {
                case MINLENOFDNAME:
                    stat.mDnsAbnormalCnt = stat.mDnsAbnormalCnt + HW_CONNECT_REASON_CONNECTING;
                    break;
                case HW_CONNECT_REASON_CONNECTING /*1*/:
                    stat.mTcpRxAbnormalCnt = stat.mTcpRxAbnormalCnt + HW_CONNECT_REASON_CONNECTING;
                    break;
                case HW_CONNECT_REASON_ROAMING /*2*/:
                    stat.mRoamingAbnormalCnt = stat.mRoamingAbnormalCnt + HW_CONNECT_REASON_CONNECTING;
                    break;
                case HW_CONNECT_REASON_REKEY /*3*/:
                    stat.mGatewayAbnormalCnt = stat.mGatewayAbnormalCnt + HW_CONNECT_REASON_CONNECTING;
                    break;
                case MessageUtil.MSG_WIFI_DISABLE /*4*/:
                    stat.mDnsScSuccCnt = stat.mDnsScSuccCnt + HW_CONNECT_REASON_CONNECTING;
                    break;
                case MessageUtil.MSG_WIFI_FIND_TARGET /*5*/:
                    stat.mReDhcpScSuccCnt = stat.mReDhcpScSuccCnt + HW_CONNECT_REASON_CONNECTING;
                    break;
                case MessageUtil.MSG_WIFI_DISABLEING /*6*/:
                    stat.mStaticIpScSuccCnt = stat.mStaticIpScSuccCnt + HW_CONNECT_REASON_CONNECTING;
                    break;
                case MessageUtil.MSG_WIFI_UPDATE_SCAN_RESULT /*7*/:
                    stat.mReassocScCnt = stat.mReassocScCnt + HW_CONNECT_REASON_CONNECTING;
                    break;
                case MessageUtil.MSG_WIFI_CONFIG_CHANGED /*8*/:
                    stat.mResetScSuccCnt = stat.mResetScSuccCnt + HW_CONNECT_REASON_CONNECTING;
                    break;
                case MessageUtil.MSG_WIFI_HANDLE_DISABLE /*9*/:
                    stat.mUserEnableStaticIpCnt = stat.mUserEnableStaticIpCnt + HW_CONNECT_REASON_CONNECTING;
                    break;
                case MessageUtil.MSG_WIFI_AUTO_OPEN /*10*/:
                    stat.mAuthFailedAbnormalCnt = stat.mAuthFailedAbnormalCnt + HW_CONNECT_REASON_CONNECTING;
                    break;
                case MessageUtil.MSG_WIFI_INTERNET_CONNECTED /*11*/:
                    stat.mAssocRejectedAbnormalCnt = stat.mAssocRejectedAbnormalCnt + HW_CONNECT_REASON_CONNECTING;
                    break;
                case MessageUtil.MSG_WIFI_INTERNET_DISCONNECTED /*12*/:
                    stat.mDhcpFailedAbnormalCnt = stat.mDhcpFailedAbnormalCnt + HW_CONNECT_REASON_CONNECTING;
                    break;
                case MessageUtil.MSG_WIFI_IS_PORTAL /*13*/:
                    stat.mAppDisabledAbnromalCnt = stat.mAppDisabledAbnromalCnt + HW_CONNECT_REASON_CONNECTING;
                    break;
                case MessageUtil.MSG_WIFI_P2P_CONNECTED /*14*/:
                    stat.mAuthFailedScSuccCnt = stat.mAuthFailedScSuccCnt + HW_CONNECT_REASON_CONNECTING;
                    break;
                case MessageUtil.MSG_WIFI_P2P_DISCONNECTED /*15*/:
                    stat.mAssocRejectedScSuccCnt = stat.mAssocRejectedScSuccCnt + HW_CONNECT_REASON_CONNECTING;
                    break;
                case WifiProStatisticsManager.DUALBAND_MIX_AP_SATISFIED_COUNT /*16*/:
                    stat.mDhcpFailedScSuccCnt = stat.mDhcpFailedScSuccCnt + HW_CONNECT_REASON_CONNECTING;
                    break;
                case ASSOC_REJECT_ACCESSFULL /*17*/:
                    stat.mAppDisabledScSuccCnt = stat.mAppDisabledScSuccCnt + HW_CONNECT_REASON_CONNECTING;
                    break;
            }
        }
    }

    public void updateDisconnectCnt() {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null && this.isConnectToNetwork) {
            stat.mDisconnectCnt += HW_CONNECT_REASON_CONNECTING;
            this.isConnectToNetwork = false;
        }
    }

    public boolean isConnectToNetwork() {
        return this.isConnectToNetwork;
    }

    public List<ChrLogBaseModel> getWifiStatModel(Date date) {
        List<ChrLogBaseModel> result = new ArrayList();
        CSegEVENT_WIFI_STABILITY_STAT model = new CSegEVENT_WIFI_STABILITY_STAT();
        model.tmTimeStamp.setValue(date);
        model.ucCardIndex.setValue(MINLENOFDNAME);
        if ((((this.mOpenCnt + this.mOpenSuccCnt) + this.mCloseCnt) + this.mCloseSuccCnt) + this.mDnsReqCnt > 0) {
            model.iOpenCount.setValue(this.mOpenCnt);
            model.iOpenSuccCount.setValue(this.mOpenSuccCnt);
            model.iCloseCount.setValue(this.mCloseCnt);
            model.iCloseSuccCount.setValue(this.mCloseSuccCnt);
            model.iOpenDuration.setValue(this.mOpenDuration);
            model.iCloseDuration.setValue(this.mCloseDuration);
            model.iDnsReqCnt.setValue(this.mDnsReqCnt);
            model.iDnsReqFail.setValue(this.mDnsReqFail);
            model.iDnsMaxTime.setValue(this.mDnsMaxTime);
            model.iDnsMinTime.setValue(this.mDnsMinTime);
            model.iDnsTotTime.setValue(this.mDnsTotTime);
            model.lWlanTotalTrafficBytes.setValue(this.mWlanTotalTrafficBytes);
            model.lMobileTotalTrafficBytes.setValue(this.mMobileTotalTrafficBytes);
            model.lWlanTotalConnectedDuration.setValue(this.mWlanTotalConnectedDuration);
            model.lMobileTotalConnectedDuration.setValue(this.mMobileTotalConnectedDuration);
            result.add(model);
        }
        for (int i = MINLENOFDNAME; i < this.mSSIDStatList.size(); i += HW_CONNECT_REASON_CONNECTING) {
            SSIDStat stat = (SSIDStat) this.mSSIDStatList.get(i);
            if (stat.hasDataToTrigger()) {
                CSegEVENT_WIFI_STABILITY_SSIDSTAT SSIDItem = new CSegEVENT_WIFI_STABILITY_SSIDSTAT();
                SSIDItem.tmTimeStamp.setValue(date);
                SSIDItem.strSSID.setValue(stat.SSID);
                SSIDItem.strBSSID.setValue(stat.BSSID);
                SSIDItem.iAssocCount.setValue(stat.mAssocCnt);
                SSIDItem.iAssocRejectAccessFullCnt.setValue(stat.mAssocRejectAccessFullCnt);
                SSIDItem.iAssocByABSCnt.setValue(stat.mAssocByABSCnt);
                SSIDItem.iAssocSuccCount.setValue(stat.mAssocSuccCnt);
                SSIDItem.iAuthCount.setValue(stat.mAuthCnt);
                SSIDItem.iAuthSuccCount.setValue(stat.mAuthSuccCnt);
                SSIDItem.iDhcpCount.setValue(stat.mDhcpCnt);
                SSIDItem.iDhcpSuccCount.setValue(stat.mDhcpSuccCnt);
                SSIDItem.iDhcpStaticCount.setValue(stat.mDhcpStaticCnt);
                SSIDItem.iDHCPStaticAccessCount.setValue(stat.mDhcpStaticSuccCnt);
                SSIDItem.iConnectedCount.setValue(stat.mConnectedCnt);
                SSIDItem.iConnectTotalCount.setValue(stat.mConnectTotalCnt);
                SSIDItem.iAbnormalDisconnCount.setValue(stat.mAbDisconnectCnt);
                SSIDItem.iDisconnectCnt.setValue(stat.mDisconnectCnt);
                SSIDItem.iAssocDuration.setValue(stat.mAssocDuration);
                SSIDItem.iAuthDuration.setValue(stat.mAuthDuration);
                SSIDItem.iDhcpDuration.setValue(stat.mDhcpDuration);
                SSIDItem.iConnectedDuration.setValue(stat.mConnectedDuration);
                SSIDItem.iFirstConnInternetFailDuration.setValue(stat.mFirstConnInternetFailDuration);
                SSIDItem.iCHRConnectingDuration.setValue(stat.mCHRConnectingDuration);
                SSIDItem.iRoamingCnt.setValue(stat.mRoamingCnt);
                SSIDItem.iRoamingSuccCnt.setValue(stat.mRoamingSuccCnt);
                SSIDItem.iRoamingDuration.setValue(stat.mRoamingDuration);
                SSIDItem.iReDHCPCnt.setValue(stat.mReDHCPCnt);
                SSIDItem.iReDHCPSuccCnt.setValue(stat.mReDHCPSuccCnt);
                SSIDItem.iReDHCPDuration.setValue(stat.mReDHCPDuration);
                SSIDItem.iReKeyCnt.setValue(stat.mReKEYCnt);
                SSIDItem.iReKeySuccCnt.setValue(stat.mReKEYSuccCnt);
                SSIDItem.iReKeyDuration.setValue(stat.mReKEYDuration);
                SSIDItem.iGoodReConnectCnt.setValue(stat.mGoodReConnectCnt);
                SSIDItem.iGoodReConnectSuccCnt.setValue(stat.mGoodReConnectSuccCnt);
                SSIDItem.iWeakReConnectCnt.setValue(stat.mWeakReConnectCnt);
                SSIDItem.iWeakReConnectSuccCnt.setValue(stat.mWeakReConnectSuccCnt);
                SSIDItem.iOnScreenConnectCnt.setValue(stat.mOnScreenConnectCnt);
                SSIDItem.iOnScreenConnectedCnt.setValue(stat.mOnScreenConnectedCnt);
                SSIDItem.iOnScreenAbDisconnectCnt.setValue(stat.mOnScreenAbDisconnectCnt);
                SSIDItem.iOnScreenDisconnectCnt.setValue(stat.mOnScreenDisconnectCnt);
                SSIDItem.iOnSceenConnectedDuration.setValue(stat.mOnScreenConnectDuration);
                SSIDItem.iOnSceenReConnectedCnt.setValue(stat.mOnScreenReConnectedCnt);
                SSIDItem.iDnsAbnormalCnt.setValue(stat.mDnsAbnormalCnt);
                SSIDItem.iTcpRxAbnormalCnt.setValue(stat.mTcpRxAbnormalCnt);
                SSIDItem.iRoamingAbnormalCnt.setValue(stat.mRoamingAbnormalCnt);
                SSIDItem.iGatewayAbnormalCnt.setValue(stat.mGatewayAbnormalCnt);
                SSIDItem.iDnsScSuccCnt.setValue(stat.mDnsScSuccCnt);
                SSIDItem.iReDhcpScSuccCnt.setValue(stat.mReDhcpScSuccCnt);
                SSIDItem.iStaticIpScSuccCnt.setValue(stat.mStaticIpScSuccCnt);
                SSIDItem.iReassocScSuccCnt.setValue(stat.mReassocScCnt);
                SSIDItem.iResetScSuccCnt.setValue(stat.mResetScSuccCnt);
                SSIDItem.iUserEnableStaticIpCnt.setValue(stat.mUserEnableStaticIpCnt);
                SSIDItem.iAuthFailedAbnormalCnt.setValue(stat.mAuthFailedAbnormalCnt);
                SSIDItem.iAssocRejectedAbnormalCnt.setValue(stat.mAssocRejectedAbnormalCnt);
                SSIDItem.iDhcpFailedAbnormalCnt.setValue(stat.mDhcpFailedAbnormalCnt);
                SSIDItem.iAppDisabledAbnromalCnt.setValue(stat.mAppDisabledAbnromalCnt);
                SSIDItem.iAuthFailedScSuccCnt.setValue(stat.mAuthFailedScSuccCnt);
                SSIDItem.iAssocRejectedScSuccCnt.setValue(stat.mAssocRejectedScSuccCnt);
                SSIDItem.iDhcpFailedScSuccCnt.setValue(stat.mDhcpFailedScSuccCnt);
                SSIDItem.iAppDisabledScSuccCnt.setValue(stat.mAppDisabledScSuccCnt);
                SSIDItem.strapVendorInfo.setValue(stat.apVendorInfo);
                SSIDItem.strAP_proto.setValue(stat.strAP_proto);
                SSIDItem.strAP_key_mgmt.setValue(stat.strAP_key_mgmt);
                SSIDItem.strAP_auth_alg.setValue(stat.strAP_auth_alg);
                SSIDItem.strAP_pairwise.setValue(stat.strAP_pairwise);
                SSIDItem.strAP_eap.setValue(stat.strAP_eap);
                SSIDItem.strAP_group.setValue(stat.strAP_gruop);
                SSIDItem.iAccessWebCnt.setValue(stat.mAccessWEBCnt);
                SSIDItem.iAccessWebSuccCnt.setValue(stat.mAccessWEBSuccCnt);
                SSIDItem.iFirstConnInternetFailCnt.setValue(stat.mFirstConnInternetFailCnt);
                SSIDItem.iOnlyTheTxNoRxCnt.setValue(stat.mOnlyTheTxNoRxCnt);
                SSIDItem.iDnsParseFailCnt.setValue(stat.mDnsParseFailCnt);
                SSIDItem.iArpUnreachableCnt.setValue(stat.mArpUnreachableCnt);
                SSIDItem.iArpReassocOkCnt.setValue(stat.mArpReassocOkCnt);
                SSIDItem.tmTimeStartedStamp.setValue(stat.mConStart);
                SSIDItem.tmTimeLastUpdateStamp.setValue(stat.mLastUpdate);
                SSIDItem.iRoamingAccessWebSuccCnt.setValue(stat.mRoamingAccessWebSuccCnt);
                SSIDItem.iReDHCPAccessWebSuccCnt.setValue(stat.mReDHCPAccessWebSuccCnt);
                SSIDItem.iNoUserProcRunCnt.setValue(stat.mNoUserProcCnt);
                SSIDItem.iAccessWebSlowlyCnt.setValue(stat.mUserLongTimeWaitedCnt);
                SSIDItem.ucMultiGWCount.setValue(stat.mMultiGWCount);
                SSIDItem.iAccessWebFailedPortal.setValue(stat.mAccessWebFailedPortal);
                SSIDItem.iAccessWebRoamingFailedPortal.setValue(stat.mAccessWebRoamingFailedPortal);
                SSIDItem.iAccessWebReDHCPFailedPortal.setValue(stat.mAccessWebReDHCPFailedPortal);
                result.add(SSIDItem);
            }
        }
        return result;
    }

    private void writeWifiCHRStat(boolean flushNow, boolean triggerChr) {
        Throwable th;
        synchronized (this.mWriteStatLock) {
            long now = SystemClock.elapsedRealtime();
            if (flushNow || now - this.mWriteStatTimestamp >= ((long) MIN_WRITE_STAT_SPAN)) {
                this.mWriteStatTimestamp = now;
                if (0 == this.mTimestamp) {
                    this.mTimestamp = System.currentTimeMillis();
                }
                DataOutputStream dataOutputStream = null;
                try {
                    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mWifiStatConf)));
                    try {
                        out.writeUTF(KEY_TIMESTAMP + Long.toString(this.mTimestamp) + SEPARATOR_KEY);
                        out.writeUTF(KEY_OPEN_CNT + Integer.toString(this.mOpenCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_OPEN_SUCC_CNT + Integer.toString(this.mOpenSuccCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_CLOSE_CNT + Integer.toString(this.mCloseCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_CLOSE_SUCC_CNT + Integer.toString(this.mCloseSuccCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_OPEN_DURATION + Integer.toString(this.mOpenDuration) + SEPARATOR_KEY);
                        out.writeUTF(KEY_CLOSE_DURATION + Integer.toString(this.mCloseDuration) + SEPARATOR_KEY);
                        out.writeUTF(KEY_DNS_REQ_CNT + Integer.toString(this.mDnsReqCnt) + SEPARATOR_KEY);
                        out.writeUTF(KEY_DNS_REQ_FAIL + Integer.toString(this.mDnsReqFail) + SEPARATOR_KEY);
                        out.writeUTF(KEY_DNS_MAX_TIME + Integer.toString(this.mDnsMaxTime) + SEPARATOR_KEY);
                        out.writeUTF(KEY_DNS_MIN_TIME + Integer.toString(this.mDnsMinTime) + SEPARATOR_KEY);
                        out.writeUTF(KEY_DNS_TOT_TIME + Integer.toString(this.mDnsTotTime) + SEPARATOR_KEY);
                        out.writeUTF(KEY_WLAN_TRAFFIC_BYTES + Long.toString(this.mWlanTotalTrafficBytes) + SEPARATOR_KEY);
                        out.writeUTF(KEY_MOBILE_TRAFFIC_BYTES + Long.toString(this.mMobileTotalTrafficBytes) + SEPARATOR_KEY);
                        out.writeUTF(KEY_WLAN_CONNECTED_DURATION + Long.toString(this.mWlanTotalConnectedDuration) + SEPARATOR_KEY);
                        out.writeUTF(KEY_MOBILE_CONNECTED_DURATION + Long.toString(this.mMobileTotalConnectedDuration) + SEPARATOR_KEY);
                        for (int i = MINLENOFDNAME; i < this.mSSIDStatList.size(); i += HW_CONNECT_REASON_CONNECTING) {
                            SSIDStat stat = (SSIDStat) this.mSSIDStatList.get(i);
                            out.writeUTF(KEY_SSID + stat.SSID + SEPARATOR_KEY);
                            out.writeUTF(KEY_BSSID + stat.BSSID + SEPARATOR_KEY);
                            out.writeUTF(KEY_ASSOC_CNT + Integer.toString(stat.mAssocCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ASSOC_BY_ABS_CNT + Integer.toString(stat.mAssocByABSCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ASSOC_REJECT_CNT + Integer.toString(stat.mAssocRejectAccessFullCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ASSOC_SUCC_CNT + Integer.toString(stat.mAssocSuccCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_AUTH_CNT + Integer.toString(stat.mAuthCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_AUTH_SUCC_CNT + Integer.toString(stat.mAuthSuccCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_DHCP_CNT + Integer.toString(stat.mDhcpCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_DHCP_SUCC_CNT + Integer.toString(stat.mDhcpSuccCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_DHCP_STATIC_CNT + Integer.toString(stat.mDhcpStaticCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_DHCP_STATIC_SUCC_CNT + Integer.toString(stat.mDhcpStaticSuccCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_CONNECTED_CNT + Integer.toString(stat.mConnectedCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_CONNECT_TOTAL_CNT + Integer.toString(stat.mConnectTotalCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ABDISCONNECT_CNT + Integer.toString(stat.mAbDisconnectCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_DISCONNECT_CNT + Integer.toString(stat.mDisconnectCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ASSOC_DURATION + Integer.toString(stat.mAssocDuration) + SEPARATOR_KEY);
                            out.writeUTF(KEY_AUTH_DURATION + Integer.toString(stat.mAuthDuration) + SEPARATOR_KEY);
                            out.writeUTF(KEY_DHCP_DURATION + Integer.toString(stat.mDhcpDuration) + SEPARATOR_KEY);
                            out.writeUTF(KEY_CONNECTED_DURATION + Integer.toString(stat.mConnectedDuration) + SEPARATOR_KEY);
                            out.writeUTF(KEY_FIRST_CONN_INTERNET_FAIL_DURATION + Integer.toString(stat.mFirstConnInternetFailDuration) + SEPARATOR_KEY);
                            out.writeUTF(KEY_CHR_CONNECTING_DURATION + Integer.toString(stat.mCHRConnectingDuration) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ROAMING_CNT + Integer.toString(stat.mRoamingCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ROAMING_SUCC_CNT + Integer.toString(stat.mRoamingSuccCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ROAMING_DURATION + Integer.toString(stat.mRoamingDuration) + SEPARATOR_KEY);
                            out.writeUTF(KEY_REDHCP_CNT + Integer.toString(stat.mReDHCPCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_REDHCP_SUCC_CNT + Integer.toString(stat.mReDHCPSuccCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_REDHCP_DURATION + Integer.toString(stat.mReDHCPDuration) + SEPARATOR_KEY);
                            out.writeUTF(KEY_REKEY_CNT + Integer.toString(stat.mReKEYCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_REKEY_SUCC_CNT + Integer.toString(stat.mReKEYSuccCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_REKEY_DURATION + Integer.toString(stat.mReKEYDuration) + SEPARATOR_KEY);
                            out.writeUTF(KEY_GOOD_RECONNECT_CNT + Integer.toString(stat.mGoodReConnectCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_GOOD_RECONNECTSUCC_CNT + Integer.toString(stat.mGoodReConnectSuccCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_WEAK_RECONNECT_CNT + Integer.toString(stat.mWeakReConnectCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_WEAK_RECONNECTSUCC_CNT + Integer.toString(stat.mWeakReConnectSuccCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ONSCREEN_CONNECT_CNT + Integer.toString(stat.mOnScreenConnectCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ONSCREEN_CONNECTED_CNT + Integer.toString(stat.mOnScreenConnectedCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ONSCREEN_ABDICONNECT_CNT + Integer.toString(stat.mOnScreenAbDisconnectCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ONSCREEN_DISCONNECT_CNT + Integer.toString(stat.mOnScreenDisconnectCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ONSCREEN_CONNECT_DURATION + Integer.toString(stat.mOnScreenConnectDuration) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ONSCREEN_RECONNECT_CNT + Integer.toString(stat.mOnScreenReConnectedCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ONSCREEN_RECONNECT_DURATION + Integer.toString(stat.mOnScreenReConnectDuration) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ACCESS_WEB_CNT + Integer.toString(stat.mAccessWEBCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ACCESS_WEB_SUCC_CNT + Integer.toString(stat.mAccessWEBSuccCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_FIRST_CONN_INTERNERT_FAIL_CNT + Integer.toString(stat.mFirstConnInternetFailCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ONLY_THE_TX_NO_RX_CNT + Integer.toString(stat.mOnlyTheTxNoRxCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_DNS_PARSE_FAIL_CNT + Integer.toString(stat.mDnsParseFailCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ARP_UNREACHABLE_CNT + Integer.toString(stat.mArpUnreachableCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ARP_REASSOC_OK_CNT + Integer.toString(stat.mArpReassocOkCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_START_TIMESTAMP + Long.toString(stat.mConStart.getTime()) + SEPARATOR_KEY);
                            out.writeUTF(KEY_LAST_TIMESTAMP + Long.toString(stat.mLastUpdate.getTime()) + SEPARATOR_KEY);
                            out.writeUTF(KEY_DNS_ABNORMAL + Integer.toString(stat.mDnsAbnormalCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_TCP_RX_ABNORMAL + Integer.toString(stat.mTcpRxAbnormalCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ROAMING_ABNORMAL + Integer.toString(stat.mRoamingAbnormalCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_GATEWAY_ABNORMAL + Integer.toString(stat.mGatewayAbnormalCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_DNS_SC_SUCC + Integer.toString(stat.mDnsScSuccCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_RE_DHCP_SC_SUCC + Integer.toString(stat.mReDhcpScSuccCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_STATIC_IP_SC_SUCC + Integer.toString(stat.mStaticIpScSuccCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_REASSOC_SC_SUCC + Integer.toString(stat.mReassocScCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_RESET_SC_SUCC + Integer.toString(stat.mResetScSuccCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_USER_ENABLE_STATIC_IP + Integer.toString(stat.mUserEnableStaticIpCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_AUTH_FAILED_ABNORMAL + Integer.toString(stat.mAuthFailedAbnormalCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ASSOC_REJECTED_ABNORMAL + Integer.toString(stat.mAssocRejectedAbnormalCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_DHCP_FAILED_ABNORMAL + Integer.toString(stat.mDhcpFailedAbnormalCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_APP_DISABLED_ABNORMAL + Integer.toString(stat.mAppDisabledAbnromalCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_AUTH_FAILED_SC_SUCC + Integer.toString(stat.mAuthFailedScSuccCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ASSOC_REJECTED_SC_SUCC + Integer.toString(stat.mAssocRejectedScSuccCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_DHCP_FAILED_SC_SUCC + Integer.toString(stat.mDhcpFailedScSuccCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_APP_DISABLED_SC_SUCC + Integer.toString(stat.mAppDisabledScSuccCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_AP_VENDORINFO + stat.apVendorInfo + SEPARATOR_KEY);
                            out.writeUTF(KEY_AP_PROTO + stat.strAP_proto + SEPARATOR_KEY);
                            out.writeUTF(KEY_AP_KEY_MGMT + stat.strAP_key_mgmt + SEPARATOR_KEY);
                            out.writeUTF(KEY_AP_AUTH_ALG + stat.strAP_auth_alg + SEPARATOR_KEY);
                            out.writeUTF(KEY_AP_PAIRWISE + stat.strAP_pairwise + SEPARATOR_KEY);
                            out.writeUTF(KEY_AP_EAP + stat.strAP_eap + SEPARATOR_KEY);
                            out.writeUTF(KEY_AP_GROUP + stat.strAP_gruop + SEPARATOR_KEY);
                            out.writeUTF(KEY_ROAMING_ACCESS_WEB_CNT + Integer.toString(stat.mRoamingAccessWebSuccCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_REDHCP_ACCESS_WEB_SUCC_CNT + Integer.toString(stat.mReDHCPAccessWebSuccCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_NO_USERPROC_CNT + Integer.toString(stat.mNoUserProcCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_USER_IN_LONGWAITED_CNT + Integer.toString(stat.mUserLongTimeWaitedCnt) + SEPARATOR_KEY);
                            out.writeUTF(KEY_MULTIGWCOUNT + Integer.toString(stat.mMultiGWCount) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ACCESS_WEB_FAILED_BY_PORTAL_CONNECT + Integer.toString(stat.mAccessWebFailedPortal) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ACCESS_WEB_FAILED_BY_PORTAL_ROAMING + Integer.toString(stat.mAccessWebRoamingFailedPortal) + SEPARATOR_KEY);
                            out.writeUTF(KEY_ACCESS_WEB_FAILED_BY_PORTAL_REDHCP + Integer.toString(stat.mAccessWebReDHCPFailedPortal) + SEPARATOR_KEY);
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (Exception e) {
                            }
                        }
                        dataOutputStream = out;
                    } catch (Exception e2) {
                        dataOutputStream = out;
                        try {
                            LOGW("Error writing data file " + mWifiStatConf);
                            if (dataOutputStream != null) {
                                try {
                                    dataOutputStream.close();
                                } catch (Exception e3) {
                                }
                            }
                            if (triggerChr) {
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
                } catch (Exception e5) {
                    LOGW("Error writing data file " + mWifiStatConf);
                    if (dataOutputStream != null) {
                        dataOutputStream.close();
                    }
                    if (triggerChr) {
                        triggerUploadIfNeed();
                    }
                    return;
                }
                if (triggerChr) {
                    triggerUploadIfNeed();
                }
                return;
            }
        }
    }

    private void triggerUploadIfNeed() {
        long now = System.currentTimeMillis();
        if (now - this.mTimestamp >= MIN_PERIOD_TRIGGER_BETA && hasDataToTrigger()) {
            HwWifiCHRStateManager hwWifiCHRStateManager = HwWifiCHRStateManagerImpl.getDefault();
            if (hwWifiCHRStateManager != null) {
                hwWifiCHRStateManager.uploadWifiStat();
                hwWifiCHRStateManager.uploadDFTEvent(909001001);
                hwWifiCHRStateManager.uploadDFTEvent(909001002);
            }
            this.mTimestamp = now;
            clearStatInfo();
        }
    }

    private boolean hasDataToTrigger() {
        if (((long) ((((this.mOpenCnt + this.mOpenSuccCnt) + this.mCloseCnt) + this.mCloseSuccCnt) + this.mDnsReqCnt)) > 0) {
            return true;
        }
        for (int i = MINLENOFDNAME; i < this.mSSIDStatList.size(); i += HW_CONNECT_REASON_CONNECTING) {
            if (((SSIDStat) this.mSSIDStatList.get(i)).hasDataToTrigger()) {
                return true;
            }
        }
        return false;
    }

    public void readWifiCHRStat() {
        Exception e;
        LOGD("readWifiCHRStat");
        DataInputStream dataInputStream = null;
        try {
            this.mSSIDStatList.clear();
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(mWifiStatConf)));
            SSIDStat stat = null;
            while (true) {
                SSIDStat stat2;
                try {
                    String key = in.readUTF();
                    readWifiCHRTrafficBytesStat(key);
                    if (key.startsWith(KEY_TIMESTAMP)) {
                        this.mTimestamp = Long.parseLong(key.replace(KEY_TIMESTAMP, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_OPEN_CNT)) {
                        this.mOpenCnt = Integer.parseInt(key.replace(KEY_OPEN_CNT, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_OPEN_SUCC_CNT)) {
                        this.mOpenSuccCnt = Integer.parseInt(key.replace(KEY_OPEN_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_CLOSE_CNT)) {
                        this.mCloseCnt = Integer.parseInt(key.replace(KEY_CLOSE_CNT, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_CLOSE_SUCC_CNT)) {
                        this.mCloseSuccCnt = Integer.parseInt(key.replace(KEY_CLOSE_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_OPEN_DURATION)) {
                        this.mOpenDuration = Integer.parseInt(key.replace(KEY_OPEN_DURATION, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_CLOSE_DURATION)) {
                        this.mCloseDuration = Integer.parseInt(key.replace(KEY_CLOSE_DURATION, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_DNS_REQ_CNT)) {
                        this.mDnsReqCnt = Integer.parseInt(key.replace(KEY_DNS_REQ_CNT, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_DNS_REQ_FAIL)) {
                        this.mDnsReqFail = Integer.parseInt(key.replace(KEY_DNS_REQ_FAIL, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_DNS_MAX_TIME)) {
                        this.mDnsMaxTime = Integer.parseInt(key.replace(KEY_DNS_MAX_TIME, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_DNS_MIN_TIME)) {
                        this.mDnsMinTime = Integer.parseInt(key.replace(KEY_DNS_MIN_TIME, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_DNS_TOT_TIME)) {
                        this.mDnsTotTime = Integer.parseInt(key.replace(KEY_DNS_TOT_TIME, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_SSID)) {
                        String tmp = key.replace(KEY_SSID, "").replace(SEPARATOR_KEY, "");
                        stat2 = new SSIDStat();
                        try {
                            stat2.SSID = tmp;
                            this.mSSIDStatList.add(stat2);
                        } catch (EOFException e2) {
                            dataInputStream = in;
                        } catch (Exception e3) {
                            e = e3;
                            dataInputStream = in;
                        }
                    } else {
                        if (stat != null) {
                            readWifiCHRScStat(stat, key);
                            if (key.startsWith(KEY_ASSOC_CNT)) {
                                stat.mAssocCnt = Integer.parseInt(key.replace(KEY_ASSOC_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_ASSOC_SUCC_CNT)) {
                                stat.mAssocSuccCnt = Integer.parseInt(key.replace(KEY_ASSOC_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_AUTH_CNT)) {
                                stat.mAuthCnt = Integer.parseInt(key.replace(KEY_AUTH_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_AUTH_SUCC_CNT)) {
                                stat.mAuthSuccCnt = Integer.parseInt(key.replace(KEY_AUTH_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_DHCP_CNT)) {
                                stat.mDhcpCnt = Integer.parseInt(key.replace(KEY_DHCP_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_DHCP_SUCC_CNT)) {
                                stat.mDhcpSuccCnt = Integer.parseInt(key.replace(KEY_DHCP_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_DHCP_STATIC_CNT)) {
                                stat.mDhcpStaticCnt = Integer.parseInt(key.replace(KEY_DHCP_STATIC_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_DHCP_STATIC_SUCC_CNT)) {
                                stat.mDhcpStaticSuccCnt = Integer.parseInt(key.replace(KEY_DHCP_STATIC_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_CONNECTED_CNT)) {
                                stat.mConnectedCnt = Integer.parseInt(key.replace(KEY_CONNECTED_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_CONNECT_TOTAL_CNT)) {
                                stat.mConnectTotalCnt = Integer.parseInt(key.replace(KEY_CONNECT_TOTAL_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_ABDISCONNECT_CNT)) {
                                stat.mAbDisconnectCnt = Integer.parseInt(key.replace(KEY_ABDISCONNECT_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_ASSOC_DURATION)) {
                                stat.mAssocDuration = Integer.parseInt(key.replace(KEY_ASSOC_DURATION, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_AUTH_DURATION)) {
                                stat.mAuthDuration = Integer.parseInt(key.replace(KEY_AUTH_DURATION, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_DHCP_DURATION)) {
                                stat.mDhcpDuration = Integer.parseInt(key.replace(KEY_DHCP_DURATION, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_CONNECTED_DURATION)) {
                                stat.mConnectedDuration = Integer.parseInt(key.replace(KEY_CONNECTED_DURATION, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_CHR_CONNECTING_DURATION)) {
                                stat.mCHRConnectingDuration = Integer.parseInt(key.replace(KEY_CHR_CONNECTING_DURATION, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_AP_VENDORINFO)) {
                                stat.apVendorInfo = key.replace(KEY_AP_VENDORINFO, "").replace(SEPARATOR_KEY, "");
                                stat2 = stat;
                            } else if (key.startsWith(KEY_AP_PROTO)) {
                                stat.strAP_proto = key.replace(KEY_AP_PROTO, "").replace(SEPARATOR_KEY, "");
                                stat2 = stat;
                            } else if (key.startsWith(KEY_AP_KEY_MGMT)) {
                                stat.strAP_key_mgmt = key.replace(KEY_AP_KEY_MGMT, "").replace(SEPARATOR_KEY, "");
                                stat2 = stat;
                            } else if (key.startsWith(KEY_AP_AUTH_ALG)) {
                                stat.strAP_auth_alg = key.replace(KEY_AP_AUTH_ALG, "").replace(SEPARATOR_KEY, "");
                                stat2 = stat;
                            } else if (key.startsWith(KEY_AP_PAIRWISE)) {
                                stat.strAP_pairwise = key.replace(KEY_AP_PAIRWISE, "").replace(SEPARATOR_KEY, "");
                                stat2 = stat;
                            } else if (key.startsWith(KEY_AP_EAP)) {
                                stat.strAP_eap = key.replace(KEY_AP_EAP, "").replace(SEPARATOR_KEY, "");
                                stat2 = stat;
                            } else if (key.startsWith(KEY_AP_GROUP)) {
                                stat.strAP_gruop = key.replace(KEY_AP_GROUP, "").replace(SEPARATOR_KEY, "");
                                stat2 = stat;
                            } else if (key.startsWith(KEY_ROAMING_CNT)) {
                                stat.mRoamingCnt = Integer.parseInt(key.replace(KEY_ROAMING_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_ROAMING_SUCC_CNT)) {
                                stat.mRoamingSuccCnt = Integer.parseInt(key.replace(KEY_ROAMING_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_ROAMING_DURATION)) {
                                stat.mRoamingDuration = Integer.parseInt(key.replace(KEY_ROAMING_DURATION, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_REDHCP_CNT)) {
                                stat.mReDHCPCnt = Integer.parseInt(key.replace(KEY_REDHCP_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_REDHCP_SUCC_CNT)) {
                                stat.mReDHCPSuccCnt = Integer.parseInt(key.replace(KEY_REDHCP_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_REDHCP_DURATION)) {
                                stat.mReDHCPDuration = Integer.parseInt(key.replace(KEY_REDHCP_DURATION, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_REKEY_CNT)) {
                                stat.mReKEYCnt = Integer.parseInt(key.replace(KEY_REKEY_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_REKEY_SUCC_CNT)) {
                                stat.mReKEYSuccCnt = Integer.parseInt(key.replace(KEY_REKEY_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_REKEY_DURATION)) {
                                stat.mReKEYDuration = Integer.parseInt(key.replace(KEY_REKEY_DURATION, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_GOOD_RECONNECT_CNT)) {
                                stat.mGoodReConnectCnt = Integer.parseInt(key.replace(KEY_GOOD_RECONNECT_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_GOOD_RECONNECTSUCC_CNT)) {
                                stat.mGoodReConnectSuccCnt = Integer.parseInt(key.replace(KEY_GOOD_RECONNECTSUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_WEAK_RECONNECT_CNT)) {
                                stat.mWeakReConnectCnt = Integer.parseInt(key.replace(KEY_WEAK_RECONNECT_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_WEAK_RECONNECTSUCC_CNT)) {
                                stat.mWeakReConnectSuccCnt = Integer.parseInt(key.replace(KEY_WEAK_RECONNECTSUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_ONSCREEN_CONNECT_CNT)) {
                                stat.mOnScreenConnectCnt = Integer.parseInt(key.replace(KEY_ONSCREEN_CONNECT_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_ONSCREEN_CONNECTED_CNT)) {
                                stat.mOnScreenConnectedCnt = Integer.parseInt(key.replace(KEY_ONSCREEN_CONNECTED_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_ONSCREEN_ABDICONNECT_CNT)) {
                                stat.mOnScreenAbDisconnectCnt = Integer.parseInt(key.replace(KEY_ONSCREEN_ABDICONNECT_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_ONSCREEN_DISCONNECT_CNT)) {
                                stat.mOnScreenDisconnectCnt = Integer.parseInt(key.replace(KEY_ONSCREEN_DISCONNECT_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_ONSCREEN_CONNECT_DURATION)) {
                                stat.mOnScreenConnectDuration = Integer.parseInt(key.replace(KEY_ONSCREEN_CONNECT_DURATION, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_ONSCREEN_RECONNECT_CNT)) {
                                stat.mOnScreenReConnectedCnt = Integer.parseInt(key.replace(KEY_ONSCREEN_RECONNECT_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_ONSCREEN_RECONNECT_DURATION)) {
                                stat.mOnScreenReConnectDuration = Integer.parseInt(key.replace(KEY_ONSCREEN_RECONNECT_DURATION, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_ACCESS_WEB_CNT)) {
                                stat.mAccessWEBCnt = Integer.parseInt(key.replace(KEY_ACCESS_WEB_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_ACCESS_WEB_SUCC_CNT)) {
                                stat.mAccessWEBSuccCnt = Integer.parseInt(key.replace(KEY_ACCESS_WEB_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_START_TIMESTAMP)) {
                                stat.mConStart = new Date(Long.parseLong(key.replace(KEY_START_TIMESTAMP, "").replace(SEPARATOR_KEY, "")));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_LAST_TIMESTAMP)) {
                                stat.mLastUpdate = new Date(Long.parseLong(key.replace(KEY_LAST_TIMESTAMP, "").replace(SEPARATOR_KEY, "")));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_ROAMING_ACCESS_WEB_CNT)) {
                                stat.mRoamingAccessWebSuccCnt = Integer.parseInt(key.replace(KEY_ROAMING_ACCESS_WEB_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_REDHCP_ACCESS_WEB_SUCC_CNT)) {
                                stat.mReDHCPAccessWebSuccCnt = Integer.parseInt(key.replace(KEY_REDHCP_ACCESS_WEB_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_NO_USERPROC_CNT)) {
                                stat.mNoUserProcCnt = Integer.parseInt(key.replace(KEY_NO_USERPROC_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_USER_IN_LONGWAITED_CNT)) {
                                stat.mUserLongTimeWaitedCnt = Integer.parseInt(key.replace(KEY_USER_IN_LONGWAITED_CNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_MULTIGWCOUNT)) {
                                stat.mMultiGWCount = Integer.parseInt(key.replace(KEY_MULTIGWCOUNT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_ACCESS_WEB_FAILED_BY_PORTAL_CONNECT)) {
                                stat.mAccessWebFailedPortal = Integer.parseInt(key.replace(KEY_ACCESS_WEB_FAILED_BY_PORTAL_CONNECT, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_ACCESS_WEB_FAILED_BY_PORTAL_ROAMING)) {
                                stat.mAccessWebRoamingFailedPortal = Integer.parseInt(key.replace(KEY_ACCESS_WEB_FAILED_BY_PORTAL_ROAMING, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            } else if (key.startsWith(KEY_ACCESS_WEB_FAILED_BY_PORTAL_REDHCP)) {
                                stat.mAccessWebReDHCPFailedPortal = Integer.parseInt(key.replace(KEY_ACCESS_WEB_FAILED_BY_PORTAL_REDHCP, "").replace(SEPARATOR_KEY, ""));
                                stat2 = stat;
                            }
                        }
                        stat2 = stat;
                    }
                    stat = stat2;
                } catch (EOFException e4) {
                    dataInputStream = in;
                } catch (Exception e5) {
                    e = e5;
                    stat2 = stat;
                    dataInputStream = in;
                }
            }
        } catch (EOFException e6) {
            if (dataInputStream != null) {
                try {
                    dataInputStream.close();
                } catch (Exception e7) {
                    LOGW("readWifiCHRStat: Error reading file" + e7);
                }
            }
            if (dataInputStream != null) {
                try {
                    dataInputStream.close();
                } catch (Exception e72) {
                    LOGW("readWifiCHRStat: Error closing file" + e72);
                }
            }
        } catch (Exception e8) {
            e72 = e8;
            LOGW("readWifiCHRStat: No config file, revert to default" + e72);
            if (dataInputStream != null) {
                dataInputStream.close();
            }
        }
    }

    private void readWifiCHRTrafficBytesStat(String key) {
        if (key.startsWith(KEY_WLAN_TRAFFIC_BYTES)) {
            this.mWlanTotalTrafficBytes = Long.parseLong(key.replace(KEY_WLAN_TRAFFIC_BYTES, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_MOBILE_TRAFFIC_BYTES)) {
            this.mMobileTotalTrafficBytes = Long.parseLong(key.replace(KEY_MOBILE_TRAFFIC_BYTES, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_WLAN_CONNECTED_DURATION)) {
            this.mWlanTotalConnectedDuration = Long.parseLong(key.replace(KEY_WLAN_CONNECTED_DURATION, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_MOBILE_CONNECTED_DURATION)) {
            this.mMobileTotalConnectedDuration = Long.parseLong(key.replace(KEY_MOBILE_CONNECTED_DURATION, "").replace(SEPARATOR_KEY, ""));
        }
    }

    private void readWifiCHRScStat(SSIDStat stat, String key) {
        if (key.startsWith(KEY_BSSID)) {
            stat.BSSID = key.replace(KEY_BSSID, "").replace(SEPARATOR_KEY, "");
        } else if (key.startsWith(KEY_ASSOC_BY_ABS_CNT)) {
            stat.mAssocByABSCnt = Integer.parseInt(key.replace(KEY_ASSOC_BY_ABS_CNT, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_ASSOC_REJECT_CNT)) {
            stat.mAssocRejectAccessFullCnt = Integer.parseInt(key.replace(KEY_ASSOC_REJECT_CNT, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_DNS_ABNORMAL)) {
            stat.mDnsAbnormalCnt = Integer.parseInt(key.replace(KEY_DNS_ABNORMAL, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_TCP_RX_ABNORMAL)) {
            stat.mTcpRxAbnormalCnt = Integer.parseInt(key.replace(KEY_TCP_RX_ABNORMAL, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_ROAMING_ABNORMAL)) {
            stat.mRoamingAbnormalCnt = Integer.parseInt(key.replace(KEY_ROAMING_ABNORMAL, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_GATEWAY_ABNORMAL)) {
            stat.mGatewayAbnormalCnt = Integer.parseInt(key.replace(KEY_GATEWAY_ABNORMAL, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_DNS_SC_SUCC)) {
            stat.mDnsScSuccCnt = Integer.parseInt(key.replace(KEY_DNS_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_RE_DHCP_SC_SUCC)) {
            stat.mReDhcpScSuccCnt = Integer.parseInt(key.replace(KEY_RE_DHCP_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_STATIC_IP_SC_SUCC)) {
            stat.mStaticIpScSuccCnt = Integer.parseInt(key.replace(KEY_STATIC_IP_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_REASSOC_SC_SUCC)) {
            stat.mReassocScCnt = Integer.parseInt(key.replace(KEY_REASSOC_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_RESET_SC_SUCC)) {
            stat.mResetScSuccCnt = Integer.parseInt(key.replace(KEY_RESET_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_USER_ENABLE_STATIC_IP)) {
            stat.mUserEnableStaticIpCnt = Integer.parseInt(key.replace(KEY_USER_ENABLE_STATIC_IP, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_AUTH_FAILED_ABNORMAL)) {
            stat.mAuthFailedAbnormalCnt = Integer.parseInt(key.replace(KEY_AUTH_FAILED_ABNORMAL, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_ASSOC_REJECTED_ABNORMAL)) {
            stat.mAssocRejectedAbnormalCnt = Integer.parseInt(key.replace(KEY_ASSOC_REJECTED_ABNORMAL, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_DHCP_FAILED_ABNORMAL)) {
            stat.mDhcpFailedAbnormalCnt = Integer.parseInt(key.replace(KEY_DHCP_FAILED_ABNORMAL, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_APP_DISABLED_ABNORMAL)) {
            stat.mAppDisabledAbnromalCnt = Integer.parseInt(key.replace(KEY_APP_DISABLED_ABNORMAL, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_AUTH_FAILED_SC_SUCC)) {
            stat.mAuthFailedScSuccCnt = Integer.parseInt(key.replace(KEY_AUTH_FAILED_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_ASSOC_REJECTED_SC_SUCC)) {
            stat.mAssocRejectedScSuccCnt = Integer.parseInt(key.replace(KEY_ASSOC_REJECTED_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_DHCP_FAILED_SC_SUCC)) {
            stat.mDhcpFailedScSuccCnt = Integer.parseInt(key.replace(KEY_DHCP_FAILED_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_APP_DISABLED_SC_SUCC)) {
            stat.mAppDisabledScSuccCnt = Integer.parseInt(key.replace(KEY_APP_DISABLED_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_DISCONNECT_CNT)) {
            stat.mDisconnectCnt = Integer.parseInt(key.replace(KEY_DISCONNECT_CNT, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_FIRST_CONN_INTERNET_FAIL_DURATION)) {
            stat.mFirstConnInternetFailDuration = Integer.parseInt(key.replace(KEY_FIRST_CONN_INTERNET_FAIL_DURATION, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_FIRST_CONN_INTERNERT_FAIL_CNT)) {
            stat.mFirstConnInternetFailCnt = Integer.parseInt(key.replace(KEY_FIRST_CONN_INTERNERT_FAIL_CNT, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_ONLY_THE_TX_NO_RX_CNT)) {
            stat.mOnlyTheTxNoRxCnt = Integer.parseInt(key.replace(KEY_ONLY_THE_TX_NO_RX_CNT, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_DNS_PARSE_FAIL_CNT)) {
            stat.mDnsParseFailCnt = Integer.parseInt(key.replace(KEY_DNS_PARSE_FAIL_CNT, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_ARP_UNREACHABLE_CNT)) {
            stat.mArpUnreachableCnt = Integer.parseInt(key.replace(KEY_ARP_UNREACHABLE_CNT, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_ARP_REASSOC_OK_CNT)) {
            stat.mArpReassocOkCnt = Integer.parseInt(key.replace(KEY_ARP_REASSOC_OK_CNT, "").replace(SEPARATOR_KEY, ""));
        }
    }

    private void clearStatInfo() {
        this.mOpenCnt = MINLENOFDNAME;
        this.mOpenSuccCnt = MINLENOFDNAME;
        this.mCloseCnt = MINLENOFDNAME;
        this.mCloseSuccCnt = MINLENOFDNAME;
        this.mOpenDuration = MINLENOFDNAME;
        this.mCloseDuration = MINLENOFDNAME;
        this.mDnsReqCnt = MINLENOFDNAME;
        this.mDnsReqFail = MINLENOFDNAME;
        this.mDnsMaxTime = MINLENOFDNAME;
        this.mDnsMinTime = MINLENOFDNAME;
        this.mDnsTotTime = MINLENOFDNAME;
        this.mWlanTotalTrafficBytes = 0;
        this.mMobileTotalTrafficBytes = 0;
        this.mWlanTotalConnectedDuration = 0;
        this.mMobileTotalConnectedDuration = 0;
        this.mSSIDStatList.clear();
        new HwWifiDFTUtilImpl().clearSwCnt();
        if (this.mCurrentStat != null) {
            String currSSID = this.mCurrentStat.SSID;
            this.mCurrentStat = null;
            setAPSSID(currSSID);
        }
        writeWifiCHRStat(true, false);
    }

    private void LOGD(String msg) {
        Log.d(TAG, msg);
    }

    private void LOGW(String msg) {
        Log.e(TAG, msg);
    }

    public void getWifiStabilityStat(HwWifiDFTStabilityStat hwWifiDFTStabilityStat) {
        try {
            HwWifiDFTUtilImpl hwWifiDFTUtilImpl = new HwWifiDFTUtilImpl();
            hwWifiDFTStabilityStat.mOpenCount = this.mOpenCnt;
            hwWifiDFTStabilityStat.mOPenSuccCount = this.mOpenSuccCnt;
            hwWifiDFTStabilityStat.mOPenDuration = this.mOpenDuration;
            hwWifiDFTStabilityStat.mCloseCount = this.mCloseCnt;
            hwWifiDFTStabilityStat.mCloseSuccCount = this.mCloseSuccCnt;
            hwWifiDFTStabilityStat.mCloseDuration = this.mCloseDuration;
            hwWifiDFTStabilityStat.mIsWifiProON = hwWifiDFTUtilImpl.getWifiProState();
            hwWifiDFTStabilityStat.mWifiProSwcnt = hwWifiDFTUtilImpl.getWifiProSwcnt();
            hwWifiDFTStabilityStat.mIsScanAlwaysAvalible = hwWifiDFTUtilImpl.getWifiAlwaysScanState();
            hwWifiDFTStabilityStat.mScanAlwaysSwCnt = hwWifiDFTUtilImpl.getScanAlwaysSwCnt();
            hwWifiDFTStabilityStat.mIsWifiNotificationOn = hwWifiDFTUtilImpl.getWifiNetworkNotificationState();
            hwWifiDFTStabilityStat.mWifiNotifationSwCnt = hwWifiDFTUtilImpl.getWifiNotifationSwCnt();
            hwWifiDFTStabilityStat.mWifiSleepPolicy = hwWifiDFTUtilImpl.getWifiSleepPolicyState();
            hwWifiDFTStabilityStat.mWifiSleepSwCnt = hwWifiDFTUtilImpl.getWifiSleepSwCnt();
            hwWifiDFTStabilityStat.mWifiToPdp = hwWifiDFTUtilImpl.getWifiToPdpState();
            hwWifiDFTStabilityStat.mWifiToPdpSwCnt = hwWifiDFTUtilImpl.getWifiToPdpSwCnt();
        } catch (Exception e) {
            Log.e(TAG, "setWifiStabilityStat error.");
            e.printStackTrace();
        }
    }

    public void getWifiStabilitySsidStat(List<HwWifiDFTStabilitySsidStat> listHwWifiDFTStabilitySsidStat) {
        int i = MINLENOFDNAME;
        while (i < this.mSSIDStatList.size()) {
            try {
                int i2;
                SSIDStat stat = (SSIDStat) this.mSSIDStatList.get(i);
                HwWifiDFTUtilImpl hwWifiDFTUtilImpl = new HwWifiDFTUtilImpl();
                HwWifiDFTStabilitySsidStat hwWifiDFTStabilitySsidStat = new HwWifiDFTStabilitySsidStat();
                hwWifiDFTStabilitySsidStat.mApSsid = stat.SSID;
                hwWifiDFTStabilitySsidStat.mPublicEssCount = (byte) 0;
                hwWifiDFTStabilitySsidStat.mAssocCount = (short) ((stat.mAssocCnt - stat.mAssocRejectAccessFullCnt) - stat.mAssocByABSCnt);
                hwWifiDFTStabilitySsidStat.mAssocSuccCount = (short) stat.mAssocSuccCnt;
                hwWifiDFTStabilitySsidStat.mAuthCount = (short) stat.mAuthCnt;
                hwWifiDFTStabilitySsidStat.mAuthSuccCount = (short) stat.mAuthSuccCnt;
                hwWifiDFTStabilitySsidStat.mIpDhcpCount = (short) stat.mDhcpCnt;
                hwWifiDFTStabilitySsidStat.mDhcpSuccCount = (short) stat.mDhcpSuccCnt;
                hwWifiDFTStabilitySsidStat.mIpStaticCount = (short) stat.mDhcpStaticCnt;
                hwWifiDFTStabilitySsidStat.mIpAutoCount = (short) stat.mDhcpAutoIpCnt;
                hwWifiDFTStabilitySsidStat.mConnectedCount = (short) stat.mConnectedCnt;
                hwWifiDFTStabilitySsidStat.mAbnormalDisconnCount = (short) stat.mAbDisconnectCnt;
                hwWifiDFTStabilitySsidStat.mAssocDuration = stat.mAssocDuration;
                hwWifiDFTStabilitySsidStat.mAuthDuration = stat.mAuthDuration;
                hwWifiDFTStabilitySsidStat.mDhcpDuration = stat.mDhcpDuration;
                hwWifiDFTStabilitySsidStat.mConnectingDuration = stat.mConnectingDuration;
                hwWifiDFTStabilitySsidStat.mConnectionDuration = stat.mConnectedDuration;
                hwWifiDFTStabilitySsidStat.mDnsReqCnt = this.mDnsReqCnt;
                hwWifiDFTStabilitySsidStat.mDnsReqFail = this.mDnsReqFail;
                if (this.mDnsReqCnt - this.mDnsReqFail == 0) {
                    i2 = MINLENOFDNAME;
                } else {
                    i2 = this.mDnsTotTime / (this.mDnsReqCnt - this.mDnsReqFail);
                }
                hwWifiDFTStabilitySsidStat.mDnsAvgTime = i2;
                hwWifiDFTStabilitySsidStat.mDhcpRenewCount = stat.mReDHCPCnt;
                hwWifiDFTStabilitySsidStat.mDhcpRenewSuccCount = stat.mReDHCPSuccCnt;
                hwWifiDFTStabilitySsidStat.mDhcpRenewDuration = stat.mReDHCPDuration;
                hwWifiDFTStabilitySsidStat.mRoamingCount = stat.mRoamingCnt;
                hwWifiDFTStabilitySsidStat.mRoamingSuccCount = stat.mRoamingSuccCnt;
                hwWifiDFTStabilitySsidStat.mRoamingDuration = stat.mRoamingDuration;
                hwWifiDFTStabilitySsidStat.mRekeyCount = stat.mReKEYCnt;
                hwWifiDFTStabilitySsidStat.mRekeySuccCount = stat.mReKEYSuccCnt;
                hwWifiDFTStabilitySsidStat.mRekeyDuration = stat.mReKEYDuration;
                hwWifiDFTStabilitySsidStat.mAccessWebfailCnt = hwWifiDFTUtilImpl.getAccessNetFailedCount();
                hwWifiDFTStabilitySsidStat.mAccessWebSlowlyCnt = (short) stat.mUserLongTimeWaitedCnt;
                hwWifiDFTStabilitySsidStat.mGwIpCount = (byte) stat.mMultiGWCount;
                hwWifiDFTStabilitySsidStat.mGwMacCount = (byte) 0;
                hwWifiDFTStabilitySsidStat.mRssiAvg = MINLENOFDNAME;
                listHwWifiDFTStabilitySsidStat.add(hwWifiDFTStabilitySsidStat);
                i += HW_CONNECT_REASON_CONNECTING;
            } catch (Exception e) {
                Log.e(TAG, "setWifiStabilitySsidStat error.");
                e.printStackTrace();
                return;
            }
        }
    }
}

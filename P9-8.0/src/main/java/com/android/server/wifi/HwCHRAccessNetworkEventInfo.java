package com.android.server.wifi;

import java.util.Date;

public class HwCHRAccessNetworkEventInfo {
    private int iAP_RSSI;
    private int iEventId;
    private int iIsOnScreen;
    private long lTimeStampFinish;
    private long lTimeStampSessionFirstConnect;
    private long lTimeStampSessionStart;
    private HwCHRWifiRSSIGroupSummery rssi_summery = new HwCHRWifiRSSIGroupSummery();
    private String strAP_MAC = "";
    private String strAP_SSID = "";
    private String strAP_auth_alg = "";
    private String strAP_eap = "";
    private String strAP_group = "";
    private String strAP_key_mgmt = "";
    private String strAP_pairwise = "";
    private String strAP_proto = "";
    private String strConnectType;
    private String strDHCP_FAILED = "";
    private Date tmEventTriggerDate = new Date();
    private short ucBTConnState;
    private short ucBTState;
    private short ucIsApCanFound;
    private short ucPublicEss;
    private int ucSubErrorCode;
    private short ucWifiConnectFailedReason;
    private short usAP_channel;
    private short usAP_link_speed;

    public int getEventId() {
        return this.iEventId;
    }

    public void setEventId(int EventId) {
        this.iEventId = EventId;
    }

    public int getSubErrorCode() {
        return this.ucSubErrorCode;
    }

    public void setSubErrorCode(int SubErrorCode) {
        this.ucSubErrorCode = SubErrorCode;
    }

    public short getWifiConnectFailedReason() {
        return this.ucWifiConnectFailedReason;
    }

    public void setWifiConnectFailedReason(short WifiConnectFailedReason) {
        this.ucWifiConnectFailedReason = WifiConnectFailedReason;
    }

    public long geTimeStampSessionStart() {
        return this.lTimeStampSessionStart;
    }

    public void setTimeStampSessionStart(long TimeStampSessionStart) {
        this.lTimeStampSessionStart = TimeStampSessionStart;
    }

    public long getTimeStampSessionFirstConnect() {
        return this.lTimeStampSessionFirstConnect;
    }

    public void setTimeStampSessionFirstConnect(long TimeStampSessionFirstConnect) {
        this.lTimeStampSessionFirstConnect = TimeStampSessionFirstConnect;
    }

    public long getTimeStampFinish() {
        return this.lTimeStampFinish;
    }

    public void setTimeStampFinish(long TimeStampFinish) {
        this.lTimeStampFinish = TimeStampFinish;
    }

    public String getConnectType() {
        return this.strConnectType;
    }

    public void setConnectType(String ConnectType) {
        this.strConnectType = ConnectType;
    }

    public String getAP_MAC() {
        return this.strAP_MAC;
    }

    public void setAP_MAC(String AP_MAC) {
        this.strAP_MAC = AP_MAC;
    }

    public String getAP_SSID() {
        return this.strAP_SSID;
    }

    public void setAP_SSID(String AP_SSID) {
        this.strAP_SSID = AP_SSID;
    }

    public String getAP_proto() {
        return this.strAP_proto;
    }

    public void setAP_proto(String AP_proto) {
        this.strAP_proto = AP_proto;
    }

    public String getAP_key_mgmt() {
        return this.strAP_key_mgmt;
    }

    public void setAP_key_mgmt(String AP_key_mgmt) {
        this.strAP_key_mgmt = AP_key_mgmt;
    }

    public String getAP_auth_alg() {
        return this.strAP_auth_alg;
    }

    public void setAP_auth_alg(String AP_auth_alg) {
        this.strAP_auth_alg = AP_auth_alg;
    }

    public String getAP_pairwise() {
        return this.strAP_pairwise;
    }

    public void setAP_pairwise(String AP_auth_alg) {
        this.strAP_pairwise = AP_auth_alg;
    }

    public String getAP_group() {
        return this.strAP_group;
    }

    public void setAP_group(String AP_group) {
        this.strAP_group = AP_group;
    }

    public String getAP_eap() {
        return this.strAP_eap;
    }

    public void setAP_eap(String AP_eap) {
        this.strAP_eap = AP_eap;
    }

    public String getDHCP_FAILED() {
        return this.strDHCP_FAILED;
    }

    public void setDHCP_FAILED(String DHCP_FAILED) {
        this.strDHCP_FAILED = DHCP_FAILED;
    }

    public short getAP_link_speed() {
        return this.usAP_link_speed;
    }

    public void setAP_link_speed(short AP_link_speed) {
        this.usAP_link_speed = AP_link_speed;
    }

    public short getAP_channel() {
        return this.usAP_channel;
    }

    public void setAP_channel(short AP_channel) {
        this.usAP_channel = AP_channel;
    }

    public int getAP_RSSI() {
        return this.iAP_RSSI;
    }

    public void setAP_RSSI(int AP_RSSI) {
        this.iAP_RSSI = AP_RSSI;
    }

    public short getBTState() {
        return this.ucBTState;
    }

    public void setBTState(short BTState) {
        this.ucBTState = BTState;
    }

    public short getBTConnState() {
        return this.ucBTConnState;
    }

    public void setBTConnState(short BTConnState) {
        this.ucBTConnState = BTConnState;
    }

    public short getPublicEss() {
        return this.ucPublicEss;
    }

    public void setPublicEss(short PublicEss) {
        this.ucPublicEss = PublicEss;
    }

    public HwCHRWifiRSSIGroupSummery get_rssi_summery() {
        return this.rssi_summery;
    }

    public void set_rssi_summery(HwCHRWifiRSSIGroupSummery st_rssi_summery) {
        this.rssi_summery = st_rssi_summery;
    }

    public int getIsOnScreen() {
        return this.iIsOnScreen;
    }

    public void setIsOnScreen(int IsOnScreen) {
        this.iIsOnScreen = IsOnScreen;
    }

    public short getIsApCanFound() {
        return this.ucIsApCanFound;
    }

    public void setIsApCanFound(short IsApCanFound) {
        this.ucIsApCanFound = IsApCanFound;
    }

    public Date getEventTriggerDate() {
        return (Date) this.tmEventTriggerDate.clone();
    }

    public void setEventTriggerDate(Date EventTriggerDate) {
        if (EventTriggerDate == null) {
            this.tmEventTriggerDate = new Date();
        } else {
            this.tmEventTriggerDate = (Date) EventTriggerDate.clone();
        }
    }
}

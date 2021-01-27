package ohos.wifi;

import ohos.annotation.SystemApi;

public final class WifiEvents {
    public static final int BAND_INVALID = -1;
    public static final int BAND_WIDTH_160MHZ = 3;
    public static final int BAND_WIDTH_20MHZ = 0;
    public static final int BAND_WIDTH_40MHZ = 1;
    public static final int BAND_WIDTH_80MHZ = 2;
    public static final int BAND_WIDTH_80MHZ_PLUS = 4;
    public static final int BAND_WIFI_2G = 0;
    public static final int BAND_WIFI_5G = 1;
    @SystemApi
    public static final int CONN_DISABLED_REASON_AS_WRONG_PASSWORD = 13;
    public static final int CONN_P2P_INFO_AVAILABLE = 1;
    public static final int DEVICE_CHANGED_P2P_AVAILABLE = 1;
    public static final String EVENT_ACTIVE_STATE = "usual.event.wifi.POWER_STATE";
    public static final String EVENT_CONN_STATE = "usual.event.wifi.CONN_STATE";
    public static final String EVENT_DC_CONN_STATE = "usual.event.wifi.dc.STATE_CHANGE";
    public static final String EVENT_HOTSPOT_STATE = "usual.event.wifi.HOTSPOT_STATE";
    public static final String EVENT_HOTSPOT_STATION_JOIN = "usual.event.wifi.WIFI_HS_STA_JOIN";
    public static final String EVENT_HOTSPOT_STATION_LEAVE = "usual.event.wifi.WIFI_HS_STA_LEAVE";
    public static final String EVENT_MPLINK_STATE = "usual.event.wifi.mplink.STATE_CHANGE";
    public static final String EVENT_P2P_CONN_STATE_CHANGED = "usual.event.wifi.p2p.CONN_STATE_CHANGE";
    public static final String EVENT_P2P_CURRENT_DEVICE_STATE_CHANGED = "usual.event.wifi.p2p.CURRENT_DEVICE_CHANGE";
    public static final String EVENT_P2P_DEVICES_CHANGED = "usual.event.wifi.p2p.DEVICES_CHANGE";
    public static final String EVENT_P2P_DEVICES_DISCOVERY_STATE = "usual.event.wifi.p2p.PEER_DISCOVERY_STATE_CHANGE";
    @SystemApi
    public static final String EVENT_P2P_GROUP_STATE_CHANGED = "usual.event.wifi.p2p.GROUP_STATE_CHANGED";
    public static final String EVENT_P2P_STATE_CHANGED = "usual.event.wifi.p2p.STATE_CHANGE";
    public static final String EVENT_RSSI_VALUE = "usual.event.wifi.RSSI_VALUE";
    public static final String EVENT_SCAN_STATE = "usual.event.wifi.SCAN_FINISHED";
    public static final String PARAM_ACTIVE_STATE = "active_state";
    public static final String PARAM_CONN_STATE = "conn_state";
    public static final String PARAM_DC_CONN_STATE = "dc_conn_state";
    @SystemApi
    public static final String PARAM_HOTSPOT_CURRENT_TIME = "currentTime";
    public static final String PARAM_HOTSPOT_STATE = "hotspot_active_state";
    @SystemApi
    public static final String PARAM_HOTSPOT_STATION_COUNT = "staCount";
    @SystemApi
    public static final String PARAM_HOTSPOT_STATION_INFO = "macInfo";
    public static final String PARAM_MPLINK_STATE = "mp_bind_state";
    public static final String PARAM_P2P_CONN_GROUP_INFO = "group_info";
    public static final String PARAM_P2P_CONN_LINKED_INFO = "linked_info";
    public static final String PARAM_P2P_CONN_NETWORK_INFO = "net_info";
    public static final String PARAM_P2P_CURRENT_DEVICE = "p2p_device";
    public static final String PARAM_P2P_DEVICE_LIST = "dveice_list";
    public static final String PARAM_P2P_DISCOVERY = "peers_discovery";
    public static final String PARAM_P2P_STATE_CHANGED = "p2p_state";
    public static final String PARAM_RSSI_VALUE = "rssi_value";
    public static final String PARAM_SCAN_STATE = "scan_state";
    public static final int PEERS_P2P_DISCOVERY_OFF = 1;
    public static final int PEERS_P2P_DISCOVERY_ON = 2;
    public static final int PEERS_P2P_INFO_AVAILABLE = 1;
    public static final int STATE_ACTIVE = 0;
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_DC_CONNECTED = 1;
    public static final int STATE_DC_DISCONNECTED = 0;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_HOTSPOT_ACTIVE = 1;
    public static final int STATE_HOTSPOT_INACTIVE = 0;
    public static final int STATE_INACTIVE = 1;
    public static final int STATE_MPLINK_BIND = 1;
    public static final int STATE_MPLINK_UNBIND = 0;
    public static final int STATE_P2P_OFF = 1;
    public static final int STATE_P2P_ON = 2;
    public static final int STATE_SCAN_FAIL = 0;
    public static final int STATE_SCAN_SUCCESS = 1;
}

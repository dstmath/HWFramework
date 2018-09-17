package com.android.server.location.gnsschrlog;

public class ENCEventId extends Cenum {
    public ENCEventId() {
        this.map.put("NETWK_POS_TIMEOUT_EVENT", Integer.valueOf(64));
        this.map.put("GPS_POS_FLOW_ERROR_EVENT", Integer.valueOf(65));
        this.map.put("GPS_POS_TIMEOUT_EVENT", Integer.valueOf(66));
        this.map.put("NETWK_POS_TIMEOUT_EVENT_EX", Integer.valueOf(67));
        this.map.put("GPS_POS_FLOW_ERROR_EVENT_EX", Integer.valueOf(68));
        this.map.put("GPS_POS_TIMEOUT_EVENT_EX", Integer.valueOf(69));
        this.map.put("GPS_DAILY_UPLOAD", Integer.valueOf(70));
        this.map.put("GPS_DAILY_CNT_REPORT", Integer.valueOf(71));
        this.map.put("GPS_POS_ERROR_EVENT", Integer.valueOf(72));
        this.map.put("GPS_SESSION_EVENT", Integer.valueOf(73));
        this.map.put("GNSS_DATA_COLLECT_EVENT", Integer.valueOf(74));
        this.map.put("GEOLOCATION_DATA_COLLECT_EVENT", Integer.valueOf(75));
        this.map.put("CHR_GNSS_HAL_EVENT_SYSCALL", Integer.valueOf(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL));
        this.map.put("CHR_GNSS_HAL_EVENT_EXCEPTION", Integer.valueOf(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_EXCEPTION));
        this.map.put("CHR_GNSS_HAL_EVENT_INJECT", Integer.valueOf(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_INJECT));
        this.map.put("CHR_GNSS_HAL_EVENT_SYSCALL_EX", Integer.valueOf(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX));
        this.map.put("CHR_GNSS_HAL_EVENT_EXCEPTION_EX", Integer.valueOf(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_EXCEPTION_EX));
        this.map.put("CHR_GNSS_HAL_EVENT_INJECT_EX", Integer.valueOf(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_INJECT_EX));
        setLength(1);
    }
}

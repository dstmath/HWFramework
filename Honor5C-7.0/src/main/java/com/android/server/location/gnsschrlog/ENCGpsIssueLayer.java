package com.android.server.location.gnsschrlog;

public class ENCGpsIssueLayer extends Cenum {
    public ENCGpsIssueLayer() {
        this.map.put("GPS_APPLICATION_LAYER", Integer.valueOf(1));
        this.map.put("GPS_FRAMEWORK_LAYER", Integer.valueOf(2));
        this.map.put("GPS_JNI_LAYER", Integer.valueOf(3));
        this.map.put("GPS_HAL_LAYER", Integer.valueOf(4));
        this.map.put("GPS_DRV_LAYER", Integer.valueOf(5));
        this.map.put("GPS_FIRMWARE_LAYER", Integer.valueOf(6));
        this.map.put("GPS_OTA_LAYER", Integer.valueOf(7));
        setLength(1);
    }
}

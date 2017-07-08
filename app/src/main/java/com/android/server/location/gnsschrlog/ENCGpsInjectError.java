package com.android.server.location.gnsschrlog;

public class ENCGpsInjectError extends Cenum {
    public ENCGpsInjectError() {
        this.map.put("CHR_GNSS_HAL_ERROR_TIME_INJECT_CMD", Integer.valueOf(1));
        this.map.put("CHR_GNSS_HAL_ERROR_LOC_INJECT_CMD", Integer.valueOf(2));
        this.map.put("CHR_GNSS_HAL_ERROR_EPH_INJECT_CMD", Integer.valueOf(3));
        setLength(1);
    }
}

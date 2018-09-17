package com.android.server.location.gnsschrlog;

public class ENCGpsExceptionReason extends Cenum {
    public ENCGpsExceptionReason() {
        this.map.put("CHR_GNSS_HAL_ERROR_REBOOT_CMD", Integer.valueOf(1));
        this.map.put("CHR_GNSS_HAL_ERROR_TIMEOUT_CMD", Integer.valueOf(2));
        this.map.put("CHR_GNSS_HAL_ERROR_DATA_LOST_CMD", Integer.valueOf(3));
        this.map.put("CHR_GNSS_HAL_ERROR_DATA_WRONG_CMD", Integer.valueOf(4));
        this.map.put("CHR_GNSS_HAL_ERROR_ACK_LOST_CMD", Integer.valueOf(5));
        setLength(1);
    }
}

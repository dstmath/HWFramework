package com.android.server.location.gnsschrlog;

public class ENCGpsSysCallErrorReason extends Cenum {
    public ENCGpsSysCallErrorReason() {
        this.map.put("CHR_GNSS_HAL_ERROR_SOCKET_CREATE_CMD", Integer.valueOf(1));
        this.map.put("CHR_GNSS_HAL_ERROR_SOCKET_CONNECT_CMD", Integer.valueOf(2));
        this.map.put("CHR_GNSS_HAL_ERROR_PIPE_CREATE_CMD", Integer.valueOf(3));
        this.map.put("CHR_GNSS_HAL_ERROR_EPOLL_REGISTER_CMD", Integer.valueOf(4));
        this.map.put("CHR_GNSS_HAL_ERROR_EPOLL_HUP_CMD", Integer.valueOf(5));
        this.map.put("CHR_GNSS_HAL_ERROR_THREAD_CREATE_CMD", Integer.valueOf(6));
        setLength(1);
    }
}

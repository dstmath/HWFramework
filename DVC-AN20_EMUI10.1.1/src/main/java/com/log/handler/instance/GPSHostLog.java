package com.log.handler.instance;

import com.log.handler.LogHandlerUtils;
import com.log.handler.connection.ILogConnection;

public class GPSHostLog extends AbstractLogInstance {
    private static final String SYSTEM_PROPERTY_LOG_STATUS = "vendor.gpsdbglog.enable";

    public GPSHostLog(ILogConnection logConnection, LogHandlerUtils.LogType logType) {
        super(logConnection, logType);
    }

    @Override // com.log.handler.instance.AbstractLogInstance
    public String getLogStatusSystemProperty() {
        return SYSTEM_PROPERTY_LOG_STATUS;
    }
}

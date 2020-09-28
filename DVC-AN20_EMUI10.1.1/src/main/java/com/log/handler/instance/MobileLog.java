package com.log.handler.instance;

import com.log.handler.LogHandlerUtils;
import com.log.handler.connection.ILogConnection;

public class MobileLog extends AbstractLogInstance {
    public static final String PREFIX_CONFIG_SUB_LOG = "sublog_";
    private static final String PREFIX_CONFIG_TOTAL_LOG_SIZE = "totallogsize=";
    private static final String SYSTEM_PROPERTY_LOG_STATUS = "vendor.MB.running";

    public MobileLog(ILogConnection logConnection, LogHandlerUtils.LogType logType) {
        super(logConnection, logType);
    }

    @Override // com.log.handler.instance.AbstractLogInstance
    public String getLogStatusSystemProperty() {
        return SYSTEM_PROPERTY_LOG_STATUS;
    }

    public boolean setTotalRecycleSize(int logSize) {
        return executeCommand(PREFIX_CONFIG_TOTAL_LOG_SIZE + logSize);
    }

    public boolean setSubLogEnable(String subLogName, boolean enable) {
        StringBuilder sb = new StringBuilder();
        sb.append(PREFIX_CONFIG_SUB_LOG);
        sb.append(subLogName);
        sb.append("=");
        sb.append(enable ? "1" : 0);
        return executeCommand(sb.toString());
    }
}

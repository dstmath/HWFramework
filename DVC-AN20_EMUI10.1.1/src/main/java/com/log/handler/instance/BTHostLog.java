package com.log.handler.instance;

import com.log.handler.LogHandlerUtils;
import com.log.handler.connection.ILogConnection;

public class BTHostLog extends AbstractLogInstance {
    private static final String COMMAND_SET_BTFW_LOG_LEVEL = "set_btfw_log_level";
    private static final String COMMAND_SET_BTHOST_DEBUGLOG_ENABLE = "set_bthost_debuglog_enable";
    private static final String SYSTEM_PROPERTY_LOG_STATUS = "vendor.bthcisnoop.running";

    public BTHostLog(ILogConnection logConnection, LogHandlerUtils.LogType logType) {
        super(logConnection, logType);
    }

    @Override // com.log.handler.instance.AbstractLogInstance
    public String getLogStatusSystemProperty() {
        return SYSTEM_PROPERTY_LOG_STATUS;
    }

    public boolean setBTHostDebuglogEnable(boolean enable) {
        StringBuilder sb = new StringBuilder();
        sb.append("set_bthost_debuglog_enable,");
        sb.append(enable ? "1" : "0");
        return executeCommand(sb.toString());
    }

    public boolean setBTFWLogLevel(LogHandlerUtils.BTFWLogLevel btFWLogLevel) {
        return executeCommand("set_btfw_log_level," + btFWLogLevel);
    }
}

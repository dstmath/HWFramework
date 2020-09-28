package com.log.handler.instance;

import android.os.SystemProperties;
import com.log.handler.LogHandlerUtils;
import com.log.handler.connection.ILogConnection;

public class ConnsysFWLog extends AbstractLogInstance {
    private static final String COMMAND_SET_BTFW_LOG_LEVEL = "set_btfw_log_level";
    public static final String PREFIX_SET_BTHOST_DEBUGLOG_ENABLE = "set_bthost_debuglog_enable";
    private static final String SYSTEM_PROPERTY_LOG_STATUS = "vendor.connsysfw.running";

    public ConnsysFWLog(ILogConnection logConnection, LogHandlerUtils.LogType logType) {
        super(logConnection, logType);
    }

    @Override // com.log.handler.instance.AbstractLogInstance
    public String getLogStatusSystemProperty() {
        return SYSTEM_PROPERTY_LOG_STATUS;
    }

    @Override // com.log.handler.instance.AbstractLogInstance
    public synchronized boolean startLog(String logPath) {
        return startLog(logPath, LogHandlerUtils.BTFWLogLevel.SQC);
    }

    public boolean startLog(String logPath, LogHandlerUtils.BTFWLogLevel btFWLogLevel) {
        return super.startLog(logPath);
    }

    public boolean setBTFWLogLevel(LogHandlerUtils.BTFWLogLevel btFWLogLevel) {
        return executeCommand("set_btfw_log_level," + btFWLogLevel);
    }

    public boolean isConnsysFWFeatureSupport() {
        return SystemProperties.get("ro.vendor.connsys.dedicated.log", "0").equals("1");
    }
}

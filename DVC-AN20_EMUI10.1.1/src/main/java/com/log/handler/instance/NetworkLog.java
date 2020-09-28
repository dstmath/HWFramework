package com.log.handler.instance;

import com.log.handler.LogHandlerUtils;
import com.log.handler.connection.ILogConnection;

public class NetworkLog extends AbstractLogInstance {
    private static final String COMMAND_ENABLE_ROHC_COMPRESSION = "enable_rohc_compression";
    private static final String COMMAND_IS_ROHC_COMPRESSION_SUPPORT = "is_rohc_compression_support";
    private static final String COMMAND_NETWORKLOG_START = "tcpdump_sdcard_start";
    private static final String COMMAND_NETWORKLOG_STOP = "tcpdump_sdcard_stop";
    private static final String COMMAND_NETWORKLOG_STOP_WITHOUT_PING = "tcpdump_sdcard_stop_noping";
    private static final String COMMAND_SET_ROHC_TOTAL_FILE = "set_rohc_total_file";
    private static final int DEFAULT_LOG_RECYCLE_SIZE = 600;
    private static final String SYSTEM_PROPERTY_LOG_STATUS = "vendor.mtklog.netlog.Running";

    public NetworkLog(ILogConnection logConnection, LogHandlerUtils.LogType logType) {
        super(logConnection, logType);
    }

    @Override // com.log.handler.instance.AbstractLogInstance
    public String getLogStatusSystemProperty() {
        return SYSTEM_PROPERTY_LOG_STATUS;
    }

    @Override // com.log.handler.instance.AbstractLogInstance
    public synchronized boolean startLog(String logPath) {
        return startLog(logPath, DEFAULT_LOG_RECYCLE_SIZE, 90);
    }

    public synchronized boolean startLog(String logPath, int recycleSize, int packageSize) {
        if (recycleSize < 100) {
            recycleSize = DEFAULT_LOG_RECYCLE_SIZE;
        }
        String startCommand = "tcpdump_sdcard_start_" + recycleSize;
        if (packageSize > 0) {
            startCommand = startCommand + ",-s" + packageSize;
        }
        setStartCommand(startCommand);
        return super.startLog(logPath);
    }

    @Override // com.log.handler.instance.AbstractLogInstance
    public synchronized boolean stopLog() {
        return stopLog(false);
    }

    public boolean stopLog(boolean isCheckEnvironment) {
        return executeCommand(isCheckEnvironment ? COMMAND_NETWORKLOG_STOP : COMMAND_NETWORKLOG_STOP_WITHOUT_PING, true);
    }

    @Override // com.log.handler.instance.AbstractLogInstance
    public boolean setBootupLogSaved(boolean enable) {
        return true;
    }

    @Override // com.log.handler.instance.AbstractLogInstance
    public boolean setLogRecycleSize(int logSize) {
        return true;
    }

    public boolean isRohcCompressionSupport() {
        return getValueFromServer(COMMAND_IS_ROHC_COMPRESSION_SUPPORT).equals("1");
    }

    public boolean enableRohcCompression(boolean enable) {
        StringBuilder sb = new StringBuilder();
        sb.append("enable_rohc_compression,");
        sb.append(enable ? "1" : "0");
        return executeCommand(sb.toString());
    }

    public boolean setRohcTotalFileNumber(int totalNumber) {
        return executeCommand("set_rohc_total_file," + totalNumber);
    }
}

package com.log.handler.instance;

import com.log.handler.LogHandlerUtils;
import com.log.handler.connection.ILogConnection;

public class METLog extends AbstractLogInstance {
    public static final String COMMAND_GET_METLOG_INIT_VALUES = "get_metlog_init_values";
    private static final String COMMAND_IS_METLOG_SUPPORT = "is_metlog_support";
    public static final String COMMAND_SET_CPU_BUFFER_SIZE = "cpu_buff_size=";
    public static final String COMMAND_SET_MET_HEAVY_RECORD = "met_heavy_record=";
    public static final String COMMAND_SET_PEROID_SIZE = "period=";
    public static final String COMMAND_SET_SSPM_SIZE = "set_sspmsize=";
    private static final String SYSTEM_PROPERTY_LOG_STATUS = "vendor.met.running";

    public METLog(ILogConnection logConnection, LogHandlerUtils.LogType logType) {
        super(logConnection, logType);
    }

    @Override // com.log.handler.instance.AbstractLogInstance
    public String getLogStatusSystemProperty() {
        return SYSTEM_PROPERTY_LOG_STATUS;
    }

    public boolean isMETLogFeatureSupport() {
        return "1".equals(getValueFromServer(COMMAND_IS_METLOG_SUPPORT));
    }

    public boolean setMETLogPeriod(int periodSize) {
        return executeCommand(COMMAND_SET_PEROID_SIZE + periodSize);
    }

    public boolean setMETLogCPUBuffer(int bufferSize) {
        return executeCommand(COMMAND_SET_CPU_BUFFER_SIZE + bufferSize);
    }

    public boolean setMETLogSSPMSize(int sspmSize) {
        return executeCommand(COMMAND_SET_SSPM_SIZE + sspmSize);
    }

    public boolean setHeavyLoadRecordingEnable(boolean enable) {
        StringBuilder sb = new StringBuilder();
        sb.append(COMMAND_SET_MET_HEAVY_RECORD);
        sb.append(enable ? "1" : "0");
        return executeCommand(sb.toString());
    }

    public String getMETLogInitValues() {
        return getValueFromServer(COMMAND_GET_METLOG_INIT_VALUES);
    }
}

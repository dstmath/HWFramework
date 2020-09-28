package com.log.handler.instance;

import com.log.handler.LogHandlerUtils;
import com.log.handler.connection.ILogConnection;
import com.log.handler.connection.LogSocketConnection;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

public class ModemLog extends AbstractLogInstance {
    private static final String[] COMMANDS_WITH_MODE = {"deep_start", COMMAND_SETAUTO};
    private static final String COMMAND_DEEP_PAUSE = "deep_pause";
    private static final String COMMAND_DISABLE_GPS_LOCATION = "disable_gps_location";
    private static final String COMMAND_ENABLE_GPS_LOCATION = "enable_gps_location";
    private static final String COMMAND_GET_CCB_BUFFER_CONFIGURE_LIST = "get_ccb_gear_id_list";
    private static final String COMMAND_GET_CCB_GEAR_ID = "get_ccb_gear_id";
    private static final String COMMAND_GET_FILTER_INFO = "get_filter_info";
    public static final String COMMAND_GET_STATUS = "getstatus";
    private static final String COMMAND_IS_GPS_SUPPORT = "is_gps_support";
    private static final String COMMAND_LOG_FLUSH = "log_flush";
    private static final String COMMAND_NOTIFY_TETHER_CHANGE = "usbtethering";
    private static final String COMMAND_POLLING = "polling";
    private static final String COMMAND_RESET = "resetmd";
    private static final String COMMAND_SETAUTO = "setauto,";
    private static final String COMMAND_SET_CCB_GEAR_ID = "set_ccb_gear_id";
    private static final String COMMAND_SET_EE_LOG_PATH = "set_ee_log_path";
    private static final String COMMAND_SET_FILE_SIZE = "setfilesize,";
    private static final String COMMAND_SET_FLUSH_LOG_PATH = "set_flush_log_path";
    private static final String COMMAND_SET_LOGSIZE = "setlogsize,";
    private static final String COMMAND_SET_MINI_DUMP_MUXZ_SIZE = "set_mini_dump_muxz_size,";
    private static final String COMMAND_SET_MODEM_LOG_CONFIGURE = "set_modem_log_configure,";
    private static final String MODEM_LOG_SERVER_NAME_3 = "com.mediatek.mdlogger.socket3";
    private static final String MODEM_LOG_SERVER_NAME_3G = "com.mediatek.mdlogger.socket";
    private static final String MODEM_LOG_SERVER_NAME_C2K_MEMORY_DUMP = "com.mediatek.mdlogger.socket3";
    private static final String RESPONSE_FINISH_MEMORY_DUMP = "MEMORYDUMP_DONE";
    private static final String RESPONSE_MEMORYDUMP_FILE = "MEMORYDUMP_FILE";
    private static final String RESPONSE_START_MEMORY_DUMP = "MEMORYDUMP_START";
    private static final String SYSTEM_PROPERTY_LOG_STATUS = "vendor.mdlogger.Running";
    private static final String TAG = "LogHandler/ModemLog";
    private ILogConnection mC2KModemLogConnection = new LogSocketConnection("com.mediatek.mdlogger.socket3");
    private String mFirstModemEEPath = "";
    private ILogConnection mModem3GLogConnection = new LogSocketConnection(MODEM_LOG_SERVER_NAME_3G);
    private ILogConnection mModem3LogConnection = new LogSocketConnection("com.mediatek.mdlogger.socket3");
    private Set<LogHandlerUtils.IModemEEMonitor> mModemEEMonitorList = new HashSet();
    private String[] mModemLogModes;

    public ModemLog(ILogConnection logConnection, LogHandlerUtils.LogType logType) {
        super(logConnection, logType);
        doInit();
    }

    private void doInit() {
        if (!this.mModem3LogConnection.connect()) {
            this.mModem3LogConnection = null;
        }
        if (!isC2KModemSupport()) {
            this.mC2KModemLogConnection = null;
        } else {
            this.mC2KModemLogConnection.connect();
        }
        if (!this.mModem3GLogConnection.connect()) {
            this.mModem3GLogConnection = null;
        } else {
            this.mLogConnection = this.mModem3GLogConnection;
        }
        ILogConnection iLogConnection = this.mModem3LogConnection;
        if (iLogConnection != null) {
            iLogConnection.addServerObserver(this);
        }
        ILogConnection iLogConnection2 = this.mC2KModemLogConnection;
        if (iLogConnection2 != null) {
            iLogConnection2.addServerObserver(this);
        }
        ILogConnection iLogConnection3 = this.mModem3GLogConnection;
        if (iLogConnection3 != null) {
            iLogConnection3.addServerObserver(this);
        }
    }

    private boolean isC2KModemSupport() {
        return false;
    }

    @Override // com.log.handler.instance.AbstractLogInstance
    public boolean executeCommand(String command, boolean isWaitingResponse) {
        if (this.mModem3LogConnection != null) {
            String md3Command = command;
            String[] strArr = COMMANDS_WITH_MODE;
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                String commandWithMode = strArr[i];
                if (!md3Command.startsWith(commandWithMode) || this.mModemLogModes.length <= 1) {
                    i++;
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(commandWithMode);
                    String str = ",";
                    if (commandWithMode.endsWith(str)) {
                        str = "";
                    }
                    sb.append(str);
                    sb.append(this.mModemLogModes[1]);
                    md3Command = sb.toString();
                }
            }
            this.mModem3LogConnection.sendToServer(md3Command);
        }
        return super.executeCommand(command, isWaitingResponse);
    }

    @Override // com.log.handler.instance.AbstractLogInstance
    public String getLogStatusSystemProperty() {
        return SYSTEM_PROPERTY_LOG_STATUS;
    }

    @Override // com.log.handler.instance.AbstractLogInstance
    public synchronized boolean startLog(String logPath) {
        return startLog(logPath, LogHandlerUtils.ModemLogMode.SD);
    }

    @Override // com.log.handler.instance.AbstractLogInstance
    public boolean setLogRecycleSize(int logSize) {
        return executeCommand(COMMAND_SET_LOGSIZE + logSize);
    }

    public boolean startLog(String logPath, LogHandlerUtils.ModemLogMode modemMode) {
        this.mModemLogModes = modemMode.toString().split("_");
        setStartCommand("deep_start," + this.mModemLogModes[0]);
        return super.startLog(logPath);
    }

    @Override // com.log.handler.instance.AbstractLogInstance
    public synchronized boolean stopLog() {
        return executeCommand(COMMAND_DEEP_PAUSE, true);
    }

    public boolean isSaveGPSLocationFeatureSupport() {
        String isFeatureSupportValue = getValueFromServer(COMMAND_IS_GPS_SUPPORT);
        return "1".equals(isFeatureSupportValue) || "3".equals(isFeatureSupportValue);
    }

    public boolean setSaveGPSLocationToModemLog(boolean enable) {
        return executeCommand(enable ? COMMAND_ENABLE_GPS_LOCATION : COMMAND_DISABLE_GPS_LOCATION);
    }

    public boolean isCCBBufferFeatureSupport() {
        String isFeatureSupportValue = getValueFromServer(COMMAND_IS_GPS_SUPPORT);
        return "2".equals(isFeatureSupportValue) || "3".equals(isFeatureSupportValue);
    }

    public String getCCBBufferConfigureList() {
        return getValueFromServer(COMMAND_GET_CCB_BUFFER_CONFIGURE_LIST);
    }

    public String getCCBBufferGearID() {
        return getValueFromServer(COMMAND_GET_CCB_GEAR_ID);
    }

    public boolean setCCBBufferGearID(String id) {
        return executeCommand("set_ccb_gear_id," + id);
    }

    public String getFilterFileInformation() {
        return getValueFromServer(COMMAND_GET_FILTER_INFO);
    }

    public boolean resetModem() {
        return executeCommand(COMMAND_RESET);
    }

    public boolean forceModemAssert() {
        return super.executeCommand(COMMAND_POLLING, false);
    }

    public LogHandlerUtils.ModemLogStatus getStatus() {
        int statusId;
        try {
            statusId = Integer.parseInt(getValueFromServer(COMMAND_GET_STATUS));
        } catch (NumberFormatException e) {
            statusId = 0;
        }
        return LogHandlerUtils.ModemLogStatus.getModemLogStatusById(statusId);
    }

    public boolean notifyUSBModeChanged() {
        return executeCommand(COMMAND_NOTIFY_TETHER_CHANGE);
    }

    public boolean sendCommandToServer(String command) {
        return executeCommand(command, true);
    }

    public boolean setModemLogFileSize(int size) {
        return executeCommand(COMMAND_SET_FILE_SIZE + size);
    }

    public boolean setMiniDumpMuxzFileMaxSize(float size) {
        if (size < 0.0f) {
            return false;
        }
        return executeCommand(COMMAND_SET_MINI_DUMP_MUXZ_SIZE + ((long) (1024.0f * size)));
    }

    public String triggerPLSModeFlush(String flushLogPath) {
        if (flushLogPath != null && !flushLogPath.isEmpty()) {
            executeCommand("set_flush_log_path," + flushLogPath, true);
        }
        return getValueFromServer(COMMAND_LOG_FLUSH);
    }

    public boolean setModemEEPath(String modemEEPath) {
        return executeCommand("set_ee_log_path," + modemEEPath);
    }

    public boolean registerModemEEMonitor(LogHandlerUtils.IModemEEMonitor modemEEMonitor) {
        synchronized (this.mModemEEMonitorList) {
            if (modemEEMonitor == null) {
                return false;
            }
            if (!this.mLogConnection.isConnection() && !this.mLogConnection.connect()) {
                return false;
            }
            return this.mModemEEMonitorList.add(modemEEMonitor);
        }
    }

    public boolean unregisterModemEEMonitor(LogHandlerUtils.IModemEEMonitor modemEEMonitor) {
        synchronized (this.mModemEEMonitorList) {
            if (modemEEMonitor != null) {
                if (this.mModemEEMonitorList.contains(modemEEMonitor)) {
                    return this.mModemEEMonitorList.remove(modemEEMonitor);
                }
            }
            return false;
        }
    }

    @Override // com.log.handler.instance.AbstractLogInstance
    public boolean setBootupLogSaved(boolean enable) {
        return setBootupLogSaved(enable, LogHandlerUtils.ModemLogMode.SD);
    }

    public boolean setBootupLogSaved(boolean enable, LogHandlerUtils.ModemLogMode modemMode) {
        this.mModemLogModes = modemMode.toString().split("_");
        StringBuilder sb = new StringBuilder();
        sb.append(COMMAND_SETAUTO);
        sb.append(enable ? this.mModemLogModes[0] : 0);
        return executeCommand(sb.toString());
    }

    public boolean setModemLogConfigure(int value) {
        return executeCommand(COMMAND_SET_MODEM_LOG_CONFIGURE + value);
    }

    @Override // com.log.handler.instance.AbstractLogInstance
    public void update(Observable o, Object arg) {
        String serverResponseStr = "";
        if (arg != null && (arg instanceof String)) {
            serverResponseStr = (String) arg;
        }
        LogHandlerUtils.logi(TAG, "update, serverResponseStr = " + serverResponseStr);
        String notifyStr = "";
        if (serverResponseStr.startsWith(RESPONSE_FINISH_MEMORY_DUMP)) {
            String modemEEPath = serverResponseStr.substring(RESPONSE_FINISH_MEMORY_DUMP.length() + 1);
            if (isDualModemLogSupport()) {
                if (this.mFirstModemEEPath.isEmpty()) {
                    this.mFirstModemEEPath = modemEEPath;
                    return;
                }
                modemEEPath = this.mFirstModemEEPath + ";" + modemEEPath;
            }
            this.mFirstModemEEPath = "";
            notifyStr = modemEEPath;
        } else if (serverResponseStr.startsWith(RESPONSE_START_MEMORY_DUMP)) {
            notifyStr = RESPONSE_START_MEMORY_DUMP;
        } else if (serverResponseStr.startsWith("need_dump_file")) {
            notifyStr = "need_dump_file";
        } else if (serverResponseStr.startsWith(RESPONSE_MEMORYDUMP_FILE)) {
            notifyStr = serverResponseStr;
        }
        if (!notifyStr.isEmpty()) {
            for (LogHandlerUtils.IModemEEMonitor modemEEMonitor : this.mModemEEMonitorList) {
                modemEEMonitor.modemEEHappened(notifyStr);
            }
        }
        super.update(o, arg);
    }

    public boolean isDualModemLogSupport() {
        return (this.mModem3LogConnection == null && this.mC2KModemLogConnection == null) ? false : true;
    }
}

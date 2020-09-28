package com.log.handler.instance;

import android.os.SystemProperties;
import com.log.handler.LogHandlerUtils;
import com.log.handler.connection.ILogConnection;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public abstract class AbstractLogInstance implements Observer {
    protected static final String COMMAND_EXECUTE_SUCCESS = "1";
    protected static final String COMMAND_SET_LOG_SIZE = "logsize=";
    protected static final String COMMAND_SET_SAVE_BOOTUP_LOG = "autostart=";
    protected static final String COMMAND_SET_STORAGE_PATH = "set_storage_path,";
    protected static final String COMMAND_START = "deep_start";
    protected static final String COMMAND_STOP = "deep_stop";
    private static final String TAG = "LogHandler/AbstractLogInstance";
    private Set<LogHandlerUtils.IAbnormalEventMonitor> mAbnormalEventMonitorList = new HashSet();
    protected ILogConnection mLogConnection;
    protected LogHandlerUtils.LogType mLogType;
    private String mStartCommand = COMMAND_START;

    public abstract String getLogStatusSystemProperty();

    public AbstractLogInstance(ILogConnection logConnection, LogHandlerUtils.LogType logType) {
        this.mLogConnection = logConnection;
        this.mLogType = logType;
        this.mLogConnection.addServerObserver(this);
    }

    public boolean executeCommand(String command) {
        return executeCommand(command, false);
    }

    public boolean executeCommand(String command, boolean isWaitingResponse) {
        LogHandlerUtils.logi(TAG, "-->executeCommand(), command = " + command + ", isWaitingResponse = " + isWaitingResponse);
        boolean sendSuccess = this.mLogConnection.sendToServer(command);
        if (!sendSuccess || !isWaitingResponse) {
            LogHandlerUtils.logd(TAG, "executeCommand result, sendSuccess = " + sendSuccess);
            return sendSuccess;
        }
        boolean isSuccess = false;
        String response = getResponse(command);
        if (response != null) {
            if (response.startsWith(command + ",")) {
                isSuccess = response.substring(command.length() + 1).equals(COMMAND_EXECUTE_SUCCESS);
            }
        }
        LogHandlerUtils.logd(TAG, "<--executeCommand result, isSuccess = " + isSuccess);
        return isSuccess;
    }

    public String getValueFromServer(String command) {
        LogHandlerUtils.logi(TAG, "-->getValueFromServer(), command = " + command);
        if (!this.mLogConnection.sendToServer(command)) {
            LogHandlerUtils.loge(TAG, "sendToServer failed, command = " + command);
            return "";
        }
        String serverValue = "";
        String response = getResponse(command);
        if (response != null) {
            if (response.startsWith(command + ",")) {
                serverValue = response.substring(command.length() + 1);
            }
        }
        LogHandlerUtils.logi(TAG, "<--getValueFromServer(), serverValue = " + serverValue);
        return serverValue;
    }

    private String getResponse(String command) {
        String responseStr = this.mLogConnection.getResponseFromServer(command);
        long timeout = 15000;
        while (true) {
            if (responseStr != null && !responseStr.isEmpty()) {
                break;
            }
            try {
                Thread.sleep(100);
                timeout -= 100;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!this.mLogConnection.isConnection() || timeout <= 0) {
                LogHandlerUtils.logw(TAG, "receiveFromServer timeout, command = " + command);
            } else {
                responseStr = this.mLogConnection.getResponseFromServer(command);
            }
        }
        LogHandlerUtils.logi(TAG, "getResponse, responseStr = " + responseStr + " and waiting time = " + (15000 - timeout));
        return responseStr;
    }

    public synchronized boolean startLog(String logPath) {
        executeCommand(COMMAND_SET_STORAGE_PATH + logPath);
        return executeCommand(getStartCommand(), true);
    }

    /* access modifiers changed from: protected */
    public String getStartCommand() {
        return this.mStartCommand;
    }

    /* access modifiers changed from: protected */
    public void setStartCommand(String startCommand) {
        this.mStartCommand = startCommand;
    }

    public synchronized boolean stopLog() {
        return executeCommand(COMMAND_STOP, true);
    }

    public boolean setLogRecycleSize(int logSize) {
        return executeCommand(COMMAND_SET_LOG_SIZE + logSize);
    }

    public boolean setBootupLogSaved(boolean enable) {
        StringBuilder sb = new StringBuilder();
        sb.append(COMMAND_SET_SAVE_BOOTUP_LOG);
        sb.append(enable ? COMMAND_EXECUTE_SUCCESS : "0");
        return executeCommand(sb.toString());
    }

    public boolean isLogRunning() {
        return SystemProperties.get(getLogStatusSystemProperty(), "0").equals(COMMAND_EXECUTE_SUCCESS);
    }

    public LogHandlerUtils.LogType getLogType() {
        return this.mLogType;
    }

    public void disConnect() {
        this.mLogConnection.disConnect();
        this.mLogConnection.deleteServerObserver(this);
    }

    public boolean registerAbnormalEventMonitor(LogHandlerUtils.IAbnormalEventMonitor abnormalEventMonitor) {
        synchronized (this.mAbnormalEventMonitorList) {
            if (abnormalEventMonitor != null) {
                if (!this.mAbnormalEventMonitorList.contains(abnormalEventMonitor)) {
                    return this.mAbnormalEventMonitorList.add(abnormalEventMonitor);
                }
            }
            return false;
        }
    }

    public boolean unregisterAbnormalEventMonitor(LogHandlerUtils.IAbnormalEventMonitor abnormalEventMonitor) {
        synchronized (this.mAbnormalEventMonitorList) {
            if (abnormalEventMonitor != null) {
                if (this.mAbnormalEventMonitorList.contains(abnormalEventMonitor)) {
                    return this.mAbnormalEventMonitorList.remove(abnormalEventMonitor);
                }
            }
            return false;
        }
    }

    public void update(Observable o, Object arg) {
        synchronized (this.mAbnormalEventMonitorList) {
            for (LogHandlerUtils.IAbnormalEventMonitor abnormalEventMonitor : this.mAbnormalEventMonitorList) {
                abnormalEventMonitor.abnormalEvenHappened(getLogType(), LogHandlerUtils.AbnormalEvent.WRITE_FILE_FAILED);
            }
        }
    }
}

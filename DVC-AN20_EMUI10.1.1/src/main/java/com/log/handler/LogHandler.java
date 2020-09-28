package com.log.handler;

import com.log.handler.LogHandlerUtils;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LogHandler {
    private static LogHandler sInstance = new LogHandler();
    private boolean mIsExecuteSuccess = true;

    public static LogHandler getInstance() {
        return sInstance;
    }

    public boolean startTypeLog(LogHandlerUtils.LogType logType, String logPath) {
        return LogFactory.getTypeLogInstance(logType).startLog(logPath);
    }

    public boolean stopTypeLog(LogHandlerUtils.LogType logType) {
        return LogFactory.getTypeLogInstance(logType).stopLog();
    }

    public boolean isTypeLogRunning(LogHandlerUtils.LogType logType) {
        return LogFactory.getTypeLogInstance(logType).isLogRunning();
    }

    public boolean setTypeLogRecycleSize(LogHandlerUtils.LogType logType, int logSize) {
        return LogFactory.getTypeLogInstance(logType).setLogRecycleSize(logSize);
    }

    public boolean setBootupTypeLogSaved(LogHandlerUtils.LogType logType, boolean enable) {
        return LogFactory.getTypeLogInstance(logType).setBootupLogSaved(enable);
    }

    public boolean registerLogAbnormalEventMonitor(LogHandlerUtils.IAbnormalEventMonitor abnormalEventMonitor) {
        for (LogHandlerUtils.LogType logType : LogHandlerUtils.LogType.getAllLogTypes()) {
            LogFactory.getTypeLogInstance(logType).registerAbnormalEventMonitor(abnormalEventMonitor);
        }
        return true;
    }

    public boolean unregisterLogAbnormalEventMonitor(LogHandlerUtils.IAbnormalEventMonitor abnormalEventMonitor) {
        for (LogHandlerUtils.LogType logType : LogHandlerUtils.LogType.getAllLogTypes()) {
            LogFactory.getTypeLogInstance(logType).unregisterAbnormalEventMonitor(abnormalEventMonitor);
        }
        return true;
    }

    public boolean executeMultiLogThreads(Set<LogHandlerUtils.LogType> logTypeSet, long timeout, final LogHandlerUtils.ILogExecute logExecute) {
        if (logTypeSet == null || logTypeSet.size() == 0) {
            return false;
        }
        if (logTypeSet.size() == 1) {
            return logExecute.execute((LogHandlerUtils.LogType) logTypeSet.toArray()[0]);
        }
        this.mIsExecuteSuccess = true;
        ExecutorService executorPool = Executors.newCachedThreadPool();
        for (final LogHandlerUtils.LogType logType : logTypeSet) {
            executorPool.execute(new Thread(new Runnable() {
                /* class com.log.handler.LogHandler.AnonymousClass1 */

                public void run() {
                    if (!logExecute.execute(logType)) {
                        LogHandler.this.mIsExecuteSuccess = false;
                    }
                }
            }));
        }
        executorPool.shutdown();
        try {
            executorPool.awaitTermination(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this.mIsExecuteSuccess;
    }

    public boolean setSubMobileLogEnable(LogHandlerUtils.MobileLogSubLog subMobileLog, boolean enable) {
        return LogFactory.getMobileLogInstance().setSubLogEnable(subMobileLog.toString(), enable);
    }

    public boolean setMobileLogTotalRecycleSize(int size) {
        return LogFactory.getMobileLogInstance().setTotalRecycleSize(size);
    }

    public boolean startModemLog(String logPath, LogHandlerUtils.ModemLogMode modemLogMode) {
        return LogFactory.getModemLogInstance().startLog(logPath, modemLogMode);
    }

    public boolean isSaveGPSLocationFeatureSupport() {
        return LogFactory.getModemLogInstance().isSaveGPSLocationFeatureSupport();
    }

    public boolean setSaveGPSLocationToModemLog(boolean enable) {
        return LogFactory.getModemLogInstance().setSaveGPSLocationToModemLog(enable);
    }

    public boolean isCCBBufferFeatureSupport() {
        return LogFactory.getModemLogInstance().isCCBBufferFeatureSupport();
    }

    public String getCCBBufferConfigureList() {
        return LogFactory.getModemLogInstance().getCCBBufferConfigureList();
    }

    public String getCCBBufferGearID() {
        return LogFactory.getModemLogInstance().getCCBBufferGearID();
    }

    public boolean setCCBBufferGearID(String id) {
        return LogFactory.getModemLogInstance().setCCBBufferGearID(id);
    }

    public String getModemLogFilterFileInformation() {
        return LogFactory.getModemLogInstance().getFilterFileInformation();
    }

    public boolean resetModem() {
        return LogFactory.getModemLogInstance().resetModem();
    }

    public boolean forceModemAssert() {
        return LogFactory.getModemLogInstance().forceModemAssert();
    }

    public String triggerModemLogPLSModeFlush() {
        return triggerModemLogPLSModeFlush(null);
    }

    public String triggerModemLogPLSModeFlush(String logPath) {
        return LogFactory.getModemLogInstance().triggerPLSModeFlush(logPath);
    }

    public boolean setModemEEPath(String modemEELogPath) {
        return LogFactory.getModemLogInstance().setModemEEPath(modemEELogPath);
    }

    public boolean registerModemEEMonitor(LogHandlerUtils.IModemEEMonitor modemEEMonitor) {
        return LogFactory.getModemLogInstance().registerModemEEMonitor(modemEEMonitor);
    }

    public boolean unregisterModemEEMonitor(LogHandlerUtils.IModemEEMonitor modemEEMonitor) {
        return LogFactory.getModemLogInstance().unregisterModemEEMonitor(modemEEMonitor);
    }

    public LogHandlerUtils.ModemLogStatus getModemLogStatus() {
        return LogFactory.getModemLogInstance().getStatus();
    }

    public boolean notifyUSBModeChanged() {
        return LogFactory.getModemLogInstance().notifyUSBModeChanged();
    }

    public boolean sendCommandToModemLog(String commandStr) {
        return LogFactory.getModemLogInstance().sendCommandToServer(commandStr);
    }

    public boolean setModemLogFileSize(int size) {
        return LogFactory.getModemLogInstance().setModemLogFileSize(size);
    }

    public boolean setMiniDumpMuxzFileMaxSize(float size) {
        return LogFactory.getModemLogInstance().setMiniDumpMuxzFileMaxSize(size);
    }

    public boolean setBootupLogSaved(boolean enable, LogHandlerUtils.ModemLogMode modemMode) {
        return LogFactory.getModemLogInstance().setBootupLogSaved(enable, modemMode);
    }

    public boolean setModemLogConfigure(int value) {
        return LogFactory.getModemLogInstance().setModemLogConfigure(value);
    }

    public boolean startNetworkLogWithRecycleSize(String logPath, int recycleSize) {
        return startNetworkLog(logPath, recycleSize, 90);
    }

    public boolean startNetworkLogWithPackageSize(String logPath, int packageSize) {
        return startNetworkLog(logPath, 600, packageSize);
    }

    public boolean startNetworkLog(String logPath, int recycleSize, int packageSize) {
        return LogFactory.getNetworkLogInstance().startLog(logPath, recycleSize, packageSize);
    }

    public boolean stopNetworkLog(boolean isCheckEnvironment) {
        return LogFactory.getNetworkLogInstance().stopLog(isCheckEnvironment);
    }

    public boolean isNetworkLogRohcCompressionSupport() {
        return LogFactory.getNetworkLogInstance().isRohcCompressionSupport();
    }

    public boolean enableNetworkLogRohcCompression(boolean enable) {
        return LogFactory.getNetworkLogInstance().enableRohcCompression(enable);
    }

    public boolean setNetworkLogRohcTotalFileNumber(int totalNumber) {
        return LogFactory.getNetworkLogInstance().setRohcTotalFileNumber(totalNumber);
    }

    public boolean isMETLogFeatureSupport() {
        return LogFactory.getMETLogInstance().isMETLogFeatureSupport();
    }

    public boolean setMETLogPeriod(int periodSize) {
        return LogFactory.getMETLogInstance().setMETLogPeriod(periodSize);
    }

    public boolean setMETLogCPUBuffer(int bufferSize) {
        return LogFactory.getMETLogInstance().setMETLogCPUBuffer(bufferSize);
    }

    public boolean setMETLogSSPMSize(int sspmSize) {
        return LogFactory.getMETLogInstance().setMETLogSSPMSize(sspmSize);
    }

    public boolean setHeavyLoadRecordingEnable(boolean enable) {
        return LogFactory.getMETLogInstance().setHeavyLoadRecordingEnable(enable);
    }

    public String getMETLogInitValues() {
        return LogFactory.getMETLogInstance().getMETLogInitValues();
    }

    public boolean startConnsysFWLog(String logPath, LogHandlerUtils.BTFWLogLevel btFWLogLevel) {
        return LogFactory.getConnsysFWLogInstance().startLog(logPath, btFWLogLevel);
    }

    public boolean setConnsysFWLogDuringBootupSaved(boolean enable) {
        return LogFactory.getConnsysFWLogInstance().setBootupLogSaved(enable);
    }

    public boolean isConnsysFWFeatureSupport() {
        return LogFactory.getConnsysFWLogInstance().isConnsysFWFeatureSupport();
    }

    public boolean setBTHostDebuglogEnable(boolean enable) {
        return LogFactory.getBTHostLogInstance().setBTHostDebuglogEnable(enable);
    }

    public boolean setBTFWLogLevel(LogHandlerUtils.BTFWLogLevel btFWLogLevel) {
        if (isConnsysFWFeatureSupport()) {
            return LogFactory.getConnsysFWLogInstance().setBTFWLogLevel(btFWLogLevel);
        }
        return LogFactory.getBTHostLogInstance().setBTFWLogLevel(btFWLogLevel);
    }
}

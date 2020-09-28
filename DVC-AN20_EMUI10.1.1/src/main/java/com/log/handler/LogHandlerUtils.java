package com.log.handler;

import android.os.Build;
import android.util.Log;
import java.util.HashSet;
import java.util.Set;

public class LogHandlerUtils {
    public static final int CATCH_DUMP = 2;
    public static final int CATCH_LOG = 1;
    public static final String TAG = "LogHandler";

    public enum AbnormalEvent {
        DISCONNECT,
        STORAGE_FULL,
        LOG_FILE_LOST,
        WRITE_FILE_FAILED
    }

    public interface IAbnormalEventMonitor {
        void abnormalEvenHappened(LogType logType, AbnormalEvent abnormalEvent);
    }

    public interface ILogExecute {
        boolean execute(LogType logType);
    }

    public interface IModemEEMonitor {
        void modemEEHappened(String str);
    }

    public enum MobileLogSubLog {
        AndroidLog,
        KernelLog,
        SCPLog,
        ATFLog,
        BSPLog,
        MmediaLog,
        SSPMLog,
        ADSPLog,
        MCUPMLog
    }

    public enum LogType {
        MOBILE_LOG(1),
        MODEM_LOG(2),
        NETWORK_LOG(3),
        CONNSYSFW_LOG(4),
        GPSHOST_LOG(5),
        BTHOST_LOG(6),
        MET_LOG(7);
        
        public static final int MAX_ID = 7;
        private int mId;

        private LogType(int id) {
            this.mId = id;
        }

        public int getLogTypeId() {
            return this.mId;
        }

        public static LogType getLogTypeById(int logId) {
            switch (logId) {
                case 1:
                    return MOBILE_LOG;
                case 2:
                    return MODEM_LOG;
                case 3:
                    return NETWORK_LOG;
                case 4:
                    return CONNSYSFW_LOG;
                case 5:
                    return GPSHOST_LOG;
                case 6:
                    return BTHOST_LOG;
                case MAX_ID:
                    return MET_LOG;
                default:
                    return MOBILE_LOG;
            }
        }

        public static Set<LogType> getAllLogTypes() {
            Set<LogType> logTypes = new HashSet<>();
            for (int i = 1; i <= 7; i++) {
                logTypes.add(getLogTypeById(i));
            }
            return logTypes;
        }
    }

    public enum ModemLogMode {
        USB("1"),
        SD("2"),
        PLS("3"),
        USB_USB("1_1"),
        USB_SD("1_2"),
        USB_PLS("1_3"),
        SD_USB("2_1"),
        SD_SD("2_2"),
        SD_PLS("2_3"),
        PLS_USB("3_1"),
        PLS_SD("3_2"),
        PLS_PLS("3_3");
        
        private String mId;

        private ModemLogMode(String id) {
            this.mId = id;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        public static ModemLogMode getModemLogModeById(String id) {
            char c;
            switch (id.hashCode()) {
                case 49:
                    if (id.equals("1")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 50:
                    if (id.equals("2")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 51:
                    if (id.equals("3")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 50083:
                    if (id.equals("1_1")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 50084:
                    if (id.equals("1_2")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 50085:
                    if (id.equals("1_3")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 51044:
                    if (id.equals("2_1")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 51045:
                    if (id.equals("2_2")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 51046:
                    if (id.equals("2_3")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 52005:
                    if (id.equals("3_1")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case 52006:
                    if (id.equals("3_2")) {
                        c = '\n';
                        break;
                    }
                    c = 65535;
                    break;
                case 52007:
                    if (id.equals("3_3")) {
                        c = 11;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    return USB;
                case 1:
                    return USB_USB;
                case 2:
                    return USB_SD;
                case 3:
                    return USB_PLS;
                case 4:
                    return SD;
                case 5:
                    return SD_USB;
                case 6:
                    return SD_SD;
                case MAX_ID:
                    return SD_PLS;
                case '\b':
                    return PLS;
                case '\t':
                    return PLS_USB;
                case '\n':
                    return PLS_SD;
                case 11:
                    return PLS_PLS;
                default:
                    return SD;
            }
        }

        public String toString() {
            return this.mId;
        }
    }

    public enum ModemLogStatus {
        PAUSE(0),
        RUNNING(1),
        POLLING(2),
        COPYING(3);
        
        private int mId;

        private ModemLogStatus(int id) {
            this.mId = id;
        }

        public static ModemLogStatus getModemLogStatusById(int id) {
            if (id == 0) {
                return PAUSE;
            }
            if (id == 1) {
                return RUNNING;
            }
            if (id == 2) {
                return POLLING;
            }
            if (id != 3) {
                return PAUSE;
            }
            return COPYING;
        }

        public int getId() {
            return this.mId;
        }
    }

    public enum BTFWLogLevel {
        OFF("0"),
        LOW_POWER("1"),
        SQC("2"),
        DEBUG("3");
        
        private String mID;

        private BTFWLogLevel(String logLevel) {
            this.mID = logLevel;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        public static BTFWLogLevel getBTFWLogLevelByID(String id) {
            char c;
            switch (id.hashCode()) {
                case 48:
                    if (id.equals("0")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 49:
                    if (id.equals("1")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 50:
                    if (id.equals("2")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 51:
                    if (id.equals("3")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                return OFF;
            }
            if (c == 1) {
                return LOW_POWER;
            }
            if (c == 2) {
                return SQC;
            }
            if (c != 3) {
                return SQC;
            }
            return DEBUG;
        }

        public String toString() {
            return this.mID;
        }
    }

    public static void logv(String tag, String msg) {
        if (Build.TYPE.equals("eng")) {
            Log.v(tag, msg);
        }
    }

    public static void logd(String tag, String msg) {
        if (Build.TYPE.equals("eng")) {
            Log.d(tag, msg);
        }
    }

    public static void logi(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void logw(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void loge(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void loge(String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
    }
}

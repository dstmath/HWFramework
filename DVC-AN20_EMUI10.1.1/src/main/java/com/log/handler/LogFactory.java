package com.log.handler;

import com.log.handler.LogHandlerUtils;
import com.log.handler.connection.LbsHidlConnection;
import com.log.handler.connection.LogHidlConnection;
import com.log.handler.connection.LogSocketConnection;
import com.log.handler.instance.AbstractLogInstance;
import com.log.handler.instance.BTHostLog;
import com.log.handler.instance.ConnsysFWLog;
import com.log.handler.instance.GPSHostLog;
import com.log.handler.instance.METLog;
import com.log.handler.instance.MobileLog;
import com.log.handler.instance.ModemLog;
import com.log.handler.instance.NetworkLog;

public class LogFactory {
    private static BTHostLog sBTHostLog;
    private static ConnsysFWLog sConnsysFWLog;
    private static GPSHostLog sGPSHostLog;
    private static METLog sMETLog;
    private static MobileLog sMobileLog;
    private static ModemLog sModemLog;
    private static NetworkLog sNetworkLog;

    /* renamed from: com.log.handler.LogFactory$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$log$handler$LogHandlerUtils$LogType = new int[LogHandlerUtils.LogType.values().length];

        static {
            try {
                $SwitchMap$com$log$handler$LogHandlerUtils$LogType[LogHandlerUtils.LogType.MOBILE_LOG.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$log$handler$LogHandlerUtils$LogType[LogHandlerUtils.LogType.MODEM_LOG.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$log$handler$LogHandlerUtils$LogType[LogHandlerUtils.LogType.NETWORK_LOG.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$log$handler$LogHandlerUtils$LogType[LogHandlerUtils.LogType.CONNSYSFW_LOG.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$log$handler$LogHandlerUtils$LogType[LogHandlerUtils.LogType.GPSHOST_LOG.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$log$handler$LogHandlerUtils$LogType[LogHandlerUtils.LogType.BTHOST_LOG.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$log$handler$LogHandlerUtils$LogType[LogHandlerUtils.LogType.MET_LOG.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

    public static AbstractLogInstance getTypeLogInstance(LogHandlerUtils.LogType logType) {
        switch (AnonymousClass1.$SwitchMap$com$log$handler$LogHandlerUtils$LogType[logType.ordinal()]) {
            case 1:
                return getMobileLogInstance();
            case 2:
                return getModemLogInstance();
            case 3:
                return getNetworkLogInstance();
            case 4:
                return getConnsysFWLogInstance();
            case 5:
                return getGPSHostLogInstance();
            case 6:
                return getBTHostLogInstance();
            case MAX_ID:
                return getMETLogInstance();
            default:
                return null;
        }
    }

    public static MobileLog getMobileLogInstance() {
        if (sMobileLog == null) {
            synchronized (LogFactory.class) {
                if (sMobileLog == null) {
                    sMobileLog = new MobileLog(new LogSocketConnection("mobilelogd"), LogHandlerUtils.LogType.MOBILE_LOG);
                }
            }
        }
        return sMobileLog;
    }

    public static ModemLog getModemLogInstance() {
        if (sModemLog == null) {
            synchronized (LogFactory.class) {
                if (sModemLog == null) {
                    sModemLog = new ModemLog(new LogSocketConnection("com.mediatek.mdlogger.socket1"), LogHandlerUtils.LogType.MODEM_LOG);
                }
            }
        }
        return sModemLog;
    }

    public static NetworkLog getNetworkLogInstance() {
        if (sNetworkLog == null) {
            synchronized (LogFactory.class) {
                if (sNetworkLog == null) {
                    sNetworkLog = new NetworkLog(new LogSocketConnection("netdiag"), LogHandlerUtils.LogType.NETWORK_LOG);
                }
            }
        }
        return sNetworkLog;
    }

    public static BTHostLog getBTHostLogInstance() {
        if (sBTHostLog == null) {
            synchronized (LogFactory.class) {
                if (sBTHostLog == null) {
                    sBTHostLog = new BTHostLog(new LogSocketConnection("bthostlogd"), LogHandlerUtils.LogType.BTHOST_LOG);
                }
            }
        }
        return sBTHostLog;
    }

    public static METLog getMETLogInstance() {
        if (sMETLog == null) {
            synchronized (LogFactory.class) {
                if (sMETLog == null) {
                    sMETLog = new METLog(new LogHidlConnection("METLogHidlServer"), LogHandlerUtils.LogType.MET_LOG);
                }
            }
        }
        return sMETLog;
    }

    public static ConnsysFWLog getConnsysFWLogInstance() {
        if (sConnsysFWLog == null) {
            synchronized (LogFactory.class) {
                if (sConnsysFWLog == null) {
                    sConnsysFWLog = new ConnsysFWLog(new LogSocketConnection("connsysfwlogd"), LogHandlerUtils.LogType.CONNSYSFW_LOG);
                }
            }
        }
        return sConnsysFWLog;
    }

    public static GPSHostLog getGPSHostLogInstance() {
        if (sGPSHostLog == null) {
            synchronized (LogFactory.class) {
                if (sGPSHostLog == null) {
                    sGPSHostLog = new GPSHostLog(new LbsHidlConnection("mtk_mtklogger2mnld"), LogHandlerUtils.LogType.GPSHOST_LOG);
                }
            }
        }
        return sGPSHostLog;
    }
}

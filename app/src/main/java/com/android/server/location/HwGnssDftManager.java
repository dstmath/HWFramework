package com.android.server.location;

import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings.Global;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import android.util.Log;
import com.android.server.rms.algorithm.utils.IAwareHabitUtils;
import com.android.server.rms.iaware.appmng.AwareAppMngDFX;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.io.IOStatsHistory;
import com.android.server.rms.utils.Utils;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.Date;
import java.util.HashMap;

public class HwGnssDftManager {
    private static final boolean DEBUG = false;
    private static final int GPS_DAILY_CNT_REPORT = 71;
    private static final String GPS_LOG_ENABLE = "gps_log_enable";
    private static final int GPS_POS_ERROR_EVENT = 72;
    private static final int GPS_SESSION_EVENT = 73;
    private static final int IMONITOR_UPLOAD_MIN_SPAN = 86400000;
    private static final String TAG = "HwGnssLog_Imonitor";
    private static final HashMap<Integer, Long> mapErrorCodeTrigger = null;
    private Context mContext;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.HwGnssDftManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.HwGnssDftManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.HwGnssDftManager.<clinit>():void");
    }

    public HwGnssDftManager(Context context) {
        this.mContext = context;
    }

    public void sendSessionDataToImonitor(int type, HwGnssDftGnssSessionParam mHwGnssDftGnssSessionParam) {
        Log.d(TAG, "IMonitor upload event " + type);
        if (type == GPS_SESSION_EVENT) {
            try {
                EventStream EventGpsSessionRpt = IMonitor.openEventStream(HwGnssDftEvent.DFT_GPS_SESSION_EVENT);
                if (EventGpsSessionRpt == null) {
                    Log.e(TAG, "EventStabilityStat is null.");
                    return;
                }
                int startTime = (int) (mHwGnssDftGnssSessionParam.startTime / 1000);
                int stopTime = (int) (mHwGnssDftGnssSessionParam.stopTime / 1000);
                int catchSvTime = (int) (mHwGnssDftGnssSessionParam.catchSvTime / 1000);
                if (DEBUG) {
                    Log.d(TAG, "START TIME : " + startTime + " , stopTime : " + stopTime + " , catchSvTime : " + catchSvTime);
                }
                EventGpsSessionRpt.setParam((short) 2, startTime);
                EventGpsSessionRpt.setParam((short) 7, mHwGnssDftGnssSessionParam.ttff);
                EventGpsSessionRpt.setParam((short) 20, stopTime);
                EventGpsSessionRpt.setParam((short) 6, catchSvTime);
                EventGpsSessionRpt.setParam((short) 17, mHwGnssDftGnssSessionParam.isGpsdResart ? 1 : 0);
                EventGpsSessionRpt.setParam((short) 21, mHwGnssDftGnssSessionParam.lostPosCnt);
                IMonitor.sendEvent(EventGpsSessionRpt);
                IMonitor.closeEventStream(EventGpsSessionRpt);
            } catch (Exception e) {
                Log.e(TAG, "uploadDFTEvent error.");
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "type is not match,return! ");
        }
    }

    public void sendDailyDataToImonitor(int type, HwGnssDftGnssDailyParam mHwGnssDftGnssDailyParam) {
        Log.d(TAG, "IMonitor upload event " + type);
        if (type == GPS_DAILY_CNT_REPORT) {
            try {
                EventStream EventDailyRpt = IMonitor.openEventStream(HwGnssDftEvent.DFT_GPS_DAILY_CNT_REPORT);
                if (EventDailyRpt == null) {
                    Log.e(TAG, "EventStabilityStat is null.");
                    return;
                }
                EventDailyRpt.setParam((short) 2, mHwGnssDftGnssDailyParam.mDftGpsErrorUploadCnt);
                EventDailyRpt.setParam((short) 3, mHwGnssDftGnssDailyParam.mDftGpsRqCnt);
                EventDailyRpt.setParam((short) 0, mHwGnssDftGnssDailyParam.mDftNetworkTimeoutCnt);
                EventDailyRpt.setParam((short) 1, mHwGnssDftGnssDailyParam.mDftNetworkReqCnt);
                IMonitor.sendEvent(EventDailyRpt);
                IMonitor.closeEventStream(EventDailyRpt);
            } catch (Exception e) {
                Log.e(TAG, "uploadDFTEvent error.");
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "type is not match,return! ");
        }
    }

    public void sendExceptionDataToImonitor(int type, Date time, int errorCode) {
        switch (type) {
            case GPS_POS_ERROR_EVENT /*72*/:
                EventStream eStream;
                if (needTriggerChipsetLog(errorCode)) {
                    eStream = IMonitor.openEventStream(HwGnssDftEvent.DFT_GPS_CHIPSET_LOG_EVENT);
                    eStream.setParam((short) 0, errorCode);
                    eStream.setParam((short) 1, type);
                } else {
                    eStream = IMonitor.openEventStream(HwGnssDftEvent.DFT_GPS_SESSION_ERROR_EVENT);
                    eStream.setParam((short) 2, errorCode);
                }
                eStream.setTime(time.getTime());
                IMonitor.sendEvent(eStream);
                IMonitor.closeEventStream(eStream);
            default:
                Log.d(TAG, "unkown type");
        }
    }

    private boolean needTriggerChipsetLog(int errorCode) {
        if (isErrorCodeMatchToTrigger(errorCode) && isBetaClubChipLogSwitchOpen() && isErrorCodeHasTriggered(errorCode)) {
            return true;
        }
        return DEBUG;
    }

    private boolean isErrorCodeMatchToTrigger(int errorCode) {
        switch (errorCode) {
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_ON /*1*/:
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
            case LifeCycleStateMachine.LOGOUT /*5*/:
            case HwGnssLogHandlerMsgID.UPDATESVSTATUS /*9*/:
            case AwareAppMngDFX.APPLICATION_STARTTYPE_COLD /*10*/:
            case HwGnssLogHandlerMsgID.PERMISSIONERR /*13*/:
            case IAwareHabitUtils.HABIT_PROTECT_MAX_TRAIN_COUNTS /*14*/:
            case HwGnssLogHandlerMsgID.ADDGEOFENCESTATUS /*15*/:
            case HwGnssLogHandlerMsgID.NTP_ADDRESS_MSG /*23*/:
            case HwGnssLogHandlerMsgID.INJECT_EXTRA_PARAM /*24*/:
            case HwSecDiagnoseConstant.BD_PRIORITY /*27*/:
            case IOStatsHistory.ENTRY_BYTE_SIZE /*28*/:
            case IAwareHabitUtils.DECREASE_ROUNDS /*29*/:
            case MemoryConstant.DEFAULT_DIRECT_SWAPPINESS /*30*/:
                return true;
            default:
                if (!DEBUG) {
                    return DEBUG;
                }
                Log.d(TAG, "errorcode is : " + errorCode + " ,no need to ctach chip log");
                return DEBUG;
        }
    }

    private boolean isBetaClubChipLogSwitchOpen() {
        try {
            String result = Global.getString(this.mContext.getContentResolver(), GPS_LOG_ENABLE);
            if (result == null || AppHibernateCst.INVALID_PKG.equals(result)) {
            }
            return Boolean.parseBoolean(result);
        } catch (Exception ex) {
            ex.printStackTrace();
            return DEBUG;
        } catch (Throwable th) {
            return DEBUG;
        }
    }

    private boolean isErrorCodeHasTriggered(int errorCode) {
        long nowTime = SystemClock.elapsedRealtime();
        long lastUploadTime;
        if (!mapErrorCodeTrigger.containsKey(Integer.valueOf(errorCode))) {
            lastUploadTime = nowTime;
            mapErrorCodeTrigger.put(Integer.valueOf(errorCode), Long.valueOf(nowTime));
            return true;
        } else if (nowTime - ((Long) mapErrorCodeTrigger.get(Integer.valueOf(errorCode))).longValue() <= Utils.DATE_TIME_24HOURS) {
            return DEBUG;
        } else {
            lastUploadTime = nowTime;
            mapErrorCodeTrigger.put(Integer.valueOf(errorCode), Long.valueOf(nowTime));
            return true;
        }
    }
}

package com.android.server.location;

import android.os.SystemClock;
import android.util.Log;
import com.android.server.location.gnsschrlog.CSegEVENT_CHR_GNSS_HAL_EVENT_EXCEPTION;
import com.android.server.location.gnsschrlog.CSegEVENT_CHR_GNSS_HAL_EVENT_INJECT;
import com.android.server.location.gnsschrlog.CSegEVENT_CHR_GNSS_HAL_EVENT_SYSCALL;
import com.android.server.location.gnsschrlog.ChrLogBaseEventModel;
import com.android.server.location.gnsschrlog.ChrLogBaseModel;
import com.android.server.location.gnsschrlog.GnssChrCommonInfo;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import com.android.server.location.gnsschrlog.GnssLogManager;
import java.util.Date;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

public class HwHisiGnssManager {
    private static final long COMM_UPLOAD_MIN_SPAN = 86400000;
    protected static final String ESCEP_ERROR = "error";
    protected static final String ESCEP_EVENT = "event";
    private static final int GpsType = 14;
    protected static final boolean HWFLOW = false;
    private static final int MAX_NUM_TRIGGER_BETA = 100;
    private static final int MAX_NUM_TRIGGER_COMM = 50;
    private static final String TAG = "HwGnssLog_HisiGnss";
    protected static final HashMap<Integer, HashMap<Integer, String>> mapGpsEventReason = null;
    protected static final HashMap<String, TriggerLimit> mapHalDriverEventTriggerFreq = null;
    protected static final HashMap<Integer, ChrLogBaseEventModel> mapSaveLogModel = null;
    protected GnssChrCommonInfo mChrComInfo;

    private static class TriggerLimit {
        long lastUploadTime;
        int triggerNum;

        private TriggerLimit() {
        }

        public void reset() {
            this.lastUploadTime = 0;
            this.triggerNum = 0;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.HwHisiGnssManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.HwHisiGnssManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.HwHisiGnssManager.<clinit>():void");
    }

    HwHisiGnssManager() {
        this.mChrComInfo = new GnssChrCommonInfo();
        initGpsEventReasonMap();
    }

    protected void initGpsEventReasonMap() {
        HashMap<Integer, String> mapReason1 = new HashMap();
        mapReason1.put(Integer.valueOf(1), "CHR_GNSS_HAL_ERROR_SOCKET_CREATE_CMD");
        mapReason1.put(Integer.valueOf(2), "CHR_GNSS_HAL_ERROR_SOCKET_CONNECT_CMD");
        mapReason1.put(Integer.valueOf(3), "CHR_GNSS_HAL_ERROR_PIPE_CREATE_CMD");
        mapReason1.put(Integer.valueOf(4), "CHR_GNSS_HAL_ERROR_EPOLL_REGISTER_CMD");
        mapReason1.put(Integer.valueOf(5), "CHR_GNSS_HAL_ERROR_EPOLL_HUP_CMD");
        mapReason1.put(Integer.valueOf(6), "CHR_GNSS_HAL_ERROR_THREAD_CREATE_CMD");
        mapGpsEventReason.put(Integer.valueOf(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL), mapReason1);
        HashMap<Integer, String> mapReason2 = new HashMap();
        mapReason2.put(Integer.valueOf(1), "CHR_GNSS_HAL_ERROR_REBOOT_CMD");
        mapReason2.put(Integer.valueOf(2), "CHR_GNSS_HAL_ERROR_TIMEOUT_CMD");
        mapReason2.put(Integer.valueOf(3), "CHR_GNSS_HAL_ERROR_DATA_LOST_CMD");
        mapReason2.put(Integer.valueOf(4), "CHR_GNSS_HAL_ERROR_DATA_WRONG_CMD");
        mapReason2.put(Integer.valueOf(5), "CHR_GNSS_HAL_ERROR_ACK_LOST_CMD");
        mapGpsEventReason.put(Integer.valueOf(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_EXCEPTION), mapReason2);
        HashMap<Integer, String> mapReason3 = new HashMap();
        mapReason3.put(Integer.valueOf(1), "CHR_GNSS_HAL_ERROR_TIME_INJECT_CMD");
        mapReason3.put(Integer.valueOf(2), "CHR_GNSS_HAL_ERROR_LOC_INJECT_CMD");
        mapReason3.put(Integer.valueOf(3), "CHR_GNSS_HAL_ERROR_EPH_INJECT_CMD");
        mapGpsEventReason.put(Integer.valueOf(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_INJECT), mapReason3);
        if (HWFLOW) {
            Log.d(TAG, "initGpsEventReasonMap, mapGpsEventReason.size() = " + mapGpsEventReason.size());
        }
    }

    public void handleGnssHalDriverEvent(String strJsonExceptionBody) {
        JSONException e;
        if (HWFLOW) {
            Log.d(TAG, "processGnssHalDriverEvent, " + strJsonExceptionBody);
        }
        JSONObject jSONObject = null;
        int eventNo = -1;
        int errNo = -1;
        String str = null;
        try {
            JSONObject jsonStr = new JSONObject(strJsonExceptionBody);
            try {
                eventNo = jsonStr.getInt(ESCEP_EVENT);
                errNo = jsonStr.getInt(ESCEP_ERROR);
                jSONObject = jsonStr;
            } catch (JSONException e2) {
                e = e2;
                jSONObject = jsonStr;
                e.printStackTrace();
                if (jSONObject != null) {
                }
                if (HWFLOW) {
                    Log.d(TAG, "processGnssHalDriverEvent,  null == jsonStr || -1 == eventNo || -1 == errNo, return");
                }
                return;
            }
        } catch (JSONException e3) {
            e = e3;
            e.printStackTrace();
            if (jSONObject != null) {
            }
            if (HWFLOW) {
                Log.d(TAG, "processGnssHalDriverEvent,  null == jsonStr || -1 == eventNo || -1 == errNo, return");
            }
            return;
        }
        if (jSONObject != null || -1 == eventNo || -1 == errNo) {
            if (HWFLOW) {
                Log.d(TAG, "processGnssHalDriverEvent,  null == jsonStr || -1 == eventNo || -1 == errNo, return");
            }
            return;
        }
        HashMap<Integer, String> mapReason = (HashMap) mapGpsEventReason.get(Integer.valueOf(eventNo));
        if (mapReason != null) {
            if (mapReason.containsKey(Integer.valueOf(errNo))) {
                str = (String) mapReason.get(Integer.valueOf(errNo));
            }
            if (str != null && matchHalDriverEventTriggerFreq(GnssLogManager.getInstance().isCommercialUser(), str)) {
                writeNETInfo(eventNo, str);
            }
        }
    }

    private boolean matchHalDriverEventTriggerFreq(boolean commercialUser, String suberror) {
        if (suberror == null) {
            return HWFLOW;
        }
        boolean isMatch = true;
        long nowTime = SystemClock.elapsedRealtime();
        TriggerLimit triggerLimit;
        if (mapHalDriverEventTriggerFreq.containsKey(suberror)) {
            triggerLimit = (TriggerLimit) mapHalDriverEventTriggerFreq.get(suberror);
            if (triggerLimit != null) {
                if (nowTime - triggerLimit.lastUploadTime > COMM_UPLOAD_MIN_SPAN) {
                    triggerLimit.triggerNum = 0;
                } else {
                    if (triggerLimit.triggerNum > (commercialUser ? MAX_NUM_TRIGGER_COMM : MAX_NUM_TRIGGER_BETA)) {
                        isMatch = HWFLOW;
                    }
                }
                if (isMatch) {
                    triggerLimit.triggerNum++;
                    triggerLimit.lastUploadTime = nowTime;
                }
            }
        } else {
            triggerLimit = new TriggerLimit();
            triggerLimit.triggerNum = 1;
            triggerLimit.lastUploadTime = nowTime;
            mapHalDriverEventTriggerFreq.put(suberror, triggerLimit);
        }
        if (HWFLOW) {
            Log.d(TAG, "GPS matchHalDriverEventTriggerFreq , isMatch = " + isMatch);
        }
        return isMatch;
    }

    private void writeNETInfo(int type, String errReason) {
        Date date = new Date();
        Object segLog = null;
        if (HWFLOW) {
            Log.d(TAG, "writeNETInfo, type = " + type + ", errReason = " + errReason);
        }
        switch (type) {
            case GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL /*201*/:
                CSegEVENT_CHR_GNSS_HAL_EVENT_SYSCALL segSyscall = new CSegEVENT_CHR_GNSS_HAL_EVENT_SYSCALL();
                segSyscall.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                segSyscall.enGpsSysCallErrorReason.setValue(errReason);
                segSyscall.tmTimeStamp.setValue(date);
                CSegEVENT_CHR_GNSS_HAL_EVENT_SYSCALL segLog2 = segSyscall;
                break;
            case GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_EXCEPTION /*202*/:
                CSegEVENT_CHR_GNSS_HAL_EVENT_EXCEPTION segException = new CSegEVENT_CHR_GNSS_HAL_EVENT_EXCEPTION();
                segException.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                segException.enGpsExceptionReason.setValue(errReason);
                segException.tmTimeStamp.setValue(date);
                CSegEVENT_CHR_GNSS_HAL_EVENT_EXCEPTION segLog3 = segException;
                break;
            case GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_INJECT /*203*/:
                CSegEVENT_CHR_GNSS_HAL_EVENT_INJECT segInject = new CSegEVENT_CHR_GNSS_HAL_EVENT_INJECT();
                segInject.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                segInject.enGpsInjectError.setValue(errReason);
                segInject.tmTimeStamp.setValue(date);
                CSegEVENT_CHR_GNSS_HAL_EVENT_INJECT segLog4 = segInject;
                break;
        }
        mapSaveLogModel.put(Integer.valueOf(type), segLog);
        writeCHRInfo(type, true);
    }

    private void writeCHRInfo(int type, boolean ChipLogEnable) {
        ChrLogBaseModel cChrLogBaseModel = null;
        Date date = new Date();
        if (ChipLogEnable) {
            Log.d(TAG, "need to catch gpslog,not used now");
        }
        switch (type) {
            case GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL /*201*/:
            case GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_EXCEPTION /*202*/:
            case GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_INJECT /*203*/:
                cChrLogBaseModel = (ChrLogBaseModel) mapSaveLogModel.get(Integer.valueOf(type));
                mapSaveLogModel.remove(Integer.valueOf(type));
                break;
            default:
                Log.d(TAG, "writeNETInfo: error:type = " + type);
                break;
        }
        if (cChrLogBaseModel == null) {
            if (HWFLOW) {
                Log.d(TAG, "writeNETInfo, null == cChrLogBaseModel , return");
            }
            return;
        }
        Log.d(TAG, "writeNETInfo: " + type);
        GnssConnectivityLogManager.getInstance().reportAbnormalEventEx(cChrLogBaseModel, GpsType, 1, type, date, 1);
    }
}

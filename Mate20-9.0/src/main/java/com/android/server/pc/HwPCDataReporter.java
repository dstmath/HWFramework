package com.android.server.pc;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.HwPCUtils;
import android.util.IMonitor;
import android.view.DisplayInfo;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HwPCDataReporter {
    public static final int BAD_VIEWPORT_EVENT_ID = 907503008;
    public static final short E907503001_DEVICENAME_VARCHAR = 6;
    public static final short E907503001_DISPLAYTYPE_INT = 2;
    public static final short E907503001_DPI_INT = 3;
    public static final short E907503001_EXCEPTIONTYPE_INT = 1;
    public static final short E907503001_HEIGHT_INT = 5;
    public static final short E907503001_SERVICENAME_VARCHAR = 0;
    public static final short E907503001_WIDTH_INT = 4;
    public static final short E907503002_DEVICENAME_VARCHAR = 6;
    public static final short E907503002_DISPLAYID_INT = 7;
    public static final short E907503002_DISPLAYTYPE_INT = 2;
    public static final short E907503002_DPI_INT = 3;
    public static final short E907503002_FAILUREREASON_INT = 0;
    public static final short E907503002_HEIGHT_INT = 5;
    public static final short E907503002_PROJECTMODE_INT = 1;
    public static final short E907503002_WIDTH_INT = 4;
    public static final short E907503003_DISPLAYID_INT = 5;
    public static final short E907503003_DISPLAYTYPE_INT = 1;
    public static final short E907503003_DPI_INT = 2;
    public static final short E907503003_FAILUREREASON_INT = 0;
    public static final short E907503003_HEIGHT_INT = 4;
    public static final short E907503003_WIDTH_INT = 3;
    public static final short E907503004_PACKAGENAME_VARCHAR = 0;
    public static final short E907503004_PROCESSNAME_VARCHAR = 1;
    public static final short E907503004_SOURCEDISPLAYID_INT = 2;
    public static final short E907503004_TARGETDISPLAYID_INT = 3;
    public static final short E907503007_EXCEPTIONTYPE_INT = 0;
    public static final short E907503007_KEYCODE_INT = 1;
    public static final short E907503007_PACKAGENAME_VARCHAR = 2;
    public static final short E907503008_DEVICENAME_VARCHAR = 5;
    public static final short E907503008_DISPLAYID_INT = 4;
    public static final short E907503008_DISPLAYTYPE_INT = 0;
    public static final short E907503008_DPI_INT = 1;
    public static final short E907503008_HEIGHT_INT = 3;
    public static final short E907503008_WIDTH_INT = 2;
    public static final int FAIL_TO_CONNECT_EVENT_ID = 907503001;
    public static final int FAIL_TO_ENTER_PAD_PC_EVENT_ID = 907503003;
    public static final int FAIL_TO_ENTER_PAD_PC_REASON_INCALLING = 3;
    public static final int FAIL_TO_ENTER_PAD_PC_REASON_NOTCOMPLETED = 2;
    public static final int FAIL_TO_ENTER_PAD_PC_REASON_NOTFOUNDRESOURCE = 4;
    public static final int FAIL_TO_ENTER_PAD_PC_REASON_NOTINSCENE = 1;
    public static final int FAIL_TO_LIGHT_SCREEN_EVENT_ID = 907503007;
    public static final int FAIL_TO_LIGHT_SCREEN_REASON_APPNOTRESPONSE = 2;
    public static final int FAIL_TO_LIGHT_SCREEN_REASON_IOEXCEPTION = 3;
    public static final int FAIL_TO_LIGHT_SCREEN_REASON_KEYNOTRESPONSE = 1;
    public static final int FAIL_TO_SWITCH_EVENT_ID = 907503002;
    public static final int FAIL_TO_SWITCH_REASON_DISPLAY_READDED = 1;
    public static final int FAIL_TO_SWITCH_REASON_INCALLING = 4;
    public static final int FAIL_TO_SWITCH_REASON_NOPERMISSION = 2;
    public static final int FAIL_TO_SWITCH_REASON_NOTUSEROWNER = 3;
    public static final int KILL_PROCESS_EVENT_ID = 907503004;
    private static final String TAG = "HwPCDataReporter";
    private static volatile HwPCDataReporter mInstance = null;
    private static final Object mLock = new Object();
    private boolean mBadViewportHasReportOnce = false;
    private boolean mFailEnterPadHasReportOnce = false;
    private boolean mFailToConnHasReportOnce = false;
    private boolean mFailToLightHasReportOnce = false;
    private boolean mFailToSwitchHasReportOnce = false;
    private Handler mHandler = null;
    private HandlerThread mHandlerThread = null;

    private static class EventSender implements Runnable {
        private int mEventId = 0;
        private String mName = null;
        private Map<Short, Object> mParams = null;

        public void setParams(Map<Short, Object> params, String name, int eventId) {
            this.mParams = params;
            this.mName = name;
            this.mEventId = eventId;
        }

        public void run() {
            if (this.mParams != null) {
                try {
                    IMonitor.EventStream eStream = IMonitor.openEventStream(this.mEventId);
                    int size = this.mParams.size();
                    Short[] keyArray = new Short[size];
                    this.mParams.keySet().toArray(keyArray);
                    HwPCUtils.log(HwPCDataReporter.TAG, "send event, mEventId:" + this.mEventId);
                    for (int i = 0; i < size; i++) {
                        Short key = keyArray[i];
                        Object value = this.mParams.get(key);
                        HwPCUtils.log(HwPCDataReporter.TAG, "key:" + key + " value:" + value);
                        if (value instanceof Integer) {
                            eStream.setParam(key.shortValue(), ((Integer) value).intValue());
                        } else if (value instanceof String) {
                            eStream.setParam(key.shortValue(), (String) value);
                        } else {
                            HwPCUtils.log(HwPCDataReporter.TAG, "error, not known value type, value:" + value);
                        }
                    }
                    IMonitor.sendEvent(eStream);
                    eStream.close();
                } catch (IOException e) {
                    HwPCUtils.log(HwPCDataReporter.TAG, "fail to " + this.mName + " error message:" + e.getMessage());
                }
            }
        }
    }

    private HwPCDataReporter() {
    }

    public static HwPCDataReporter getInstance() {
        if (mInstance == null) {
            synchronized (mLock) {
                if (mInstance == null) {
                    mInstance = new HwPCDataReporter();
                }
            }
        }
        return mInstance;
    }

    private void resetStatus() {
        this.mFailToConnHasReportOnce = false;
        this.mFailToSwitchHasReportOnce = false;
        this.mFailEnterPadHasReportOnce = false;
        this.mFailToLightHasReportOnce = false;
        this.mBadViewportHasReportOnce = false;
    }

    public void startPCDisplay() {
        HwPCUtils.log(TAG, "startPCDisplay");
        this.mHandlerThread = new HandlerThread("PCDataReporterThread");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        resetStatus();
    }

    public void stopPCDisplay() {
        HwPCUtils.log(TAG, "stopPCDisplay");
        resetStatus();
        if (this.mHandlerThread != null) {
            this.mHandlerThread.quit();
            this.mHandlerThread = null;
            this.mHandler = null;
        }
    }

    public void reportFailToConnEvent(int exceptionType, String serviceName, DisplayInfo di) {
        if (this.mHandler == null) {
            HwPCUtils.log(TAG, "fail to reportFailToConnEvent, handler is null");
        } else if (this.mFailToConnHasReportOnce) {
            HwPCUtils.log(TAG, "mFailToConnHasReportOnce");
        } else if (di == null) {
            HwPCUtils.log(TAG, "not valid reportFailToConnEvent, displayInfo info is null");
        } else {
            HwPCUtils.log(TAG, "start reportFailToConnEvent");
            Map<Short, Object> eventParams = new HashMap<>();
            eventParams.put((short) 0, serviceName);
            eventParams.put((short) 1, Integer.valueOf(exceptionType));
            eventParams.put((short) 2, Integer.valueOf(di.type));
            eventParams.put((short) 3, Integer.valueOf(di.logicalDensityDpi));
            eventParams.put((short) 4, Integer.valueOf(di.logicalWidth));
            eventParams.put((short) 5, Integer.valueOf(di.logicalHeight));
            eventParams.put((short) 6, di.name);
            EventSender sender = new EventSender();
            sender.setParams(eventParams, "reportFailToConnEvent", FAIL_TO_CONNECT_EVENT_ID);
            this.mHandler.post(sender);
            this.mFailToConnHasReportOnce = true;
        }
    }

    public void reportFailSwitchEvent(int reason, int projMode, DisplayInfo di) {
        if (this.mHandler == null) {
            HwPCUtils.log(TAG, "fail to reportFailSwitchEvent, handler is null");
        } else if (this.mFailToSwitchHasReportOnce) {
            HwPCUtils.log(TAG, "mFailToSwitchHasReportOnce");
        } else if (di == null) {
            HwPCUtils.log(TAG, "not valid reportFailSwitchEvent, displayInfo info is null");
        } else {
            HwPCUtils.log(TAG, "start reportFailSwitchEvent");
            Map<Short, Object> eventParams = new HashMap<>();
            eventParams.put((short) 0, Integer.valueOf(reason));
            eventParams.put((short) 1, Integer.valueOf(projMode));
            eventParams.put((short) 2, Integer.valueOf(di.type));
            eventParams.put((short) 3, Integer.valueOf(di.logicalDensityDpi));
            eventParams.put((short) 4, Integer.valueOf(di.logicalWidth));
            eventParams.put((short) 5, Integer.valueOf(di.logicalHeight));
            try {
                eventParams.put((short) 7, Integer.valueOf(Integer.parseInt(di.uniqueId)));
            } catch (NumberFormatException e) {
                HwPCUtils.log(TAG, "fail to convert uniqueId");
            }
            eventParams.put((short) 6, di.name);
            EventSender sender = new EventSender();
            sender.setParams(eventParams, "reportFailToConnEvent", FAIL_TO_SWITCH_EVENT_ID);
            this.mHandler.post(sender);
            this.mFailToSwitchHasReportOnce = true;
        }
    }

    public void reportFailEnterPadEvent(int reason, DisplayInfo di) {
        if (this.mHandler == null) {
            HwPCUtils.log(TAG, "fail to reportFailEnterPadEvent, handler is null");
        } else if (this.mFailEnterPadHasReportOnce) {
            HwPCUtils.log(TAG, "mFailEnterPadHasReportOnce");
        } else if (di == null) {
            HwPCUtils.log(TAG, "not valid reportFailEnterPadEvent, displayInfo info is null");
        } else {
            HwPCUtils.log(TAG, "start reportFailEnterPadEvent");
            Map<Short, Object> eventParams = new HashMap<>();
            eventParams.put((short) 0, Integer.valueOf(reason));
            eventParams.put((short) 1, Integer.valueOf(di.type));
            eventParams.put((short) 2, Integer.valueOf(di.logicalDensityDpi));
            eventParams.put((short) 3, Integer.valueOf(di.logicalWidth));
            eventParams.put((short) 4, Integer.valueOf(di.logicalHeight));
            try {
                eventParams.put((short) 5, Integer.valueOf(Integer.parseInt(di.uniqueId)));
            } catch (NumberFormatException e) {
                HwPCUtils.log(TAG, "fail to convert uniqueId");
            }
            EventSender sender = new EventSender();
            sender.setParams(eventParams, "reportFailToConnEvent", FAIL_TO_ENTER_PAD_PC_EVENT_ID);
            this.mHandler.post(sender);
            this.mFailEnterPadHasReportOnce = true;
        }
    }

    public void reportKillProcessEvent(String packageName, String processName, int sourceDisplayId, int targetDisplayId) {
        if (this.mHandler == null) {
            HwPCUtils.log(TAG, "fail to reportKillProcessEvent, handler is null");
            return;
        }
        HwPCUtils.log(TAG, "start reportKillProcessEvent");
        Map<Short, Object> eventParams = new HashMap<>();
        eventParams.put((short) 0, packageName);
        eventParams.put((short) 1, processName);
        eventParams.put((short) 2, Integer.valueOf(sourceDisplayId));
        eventParams.put((short) 3, Integer.valueOf(targetDisplayId));
        EventSender sender = new EventSender();
        sender.setParams(eventParams, "reportFailToConnEvent", KILL_PROCESS_EVENT_ID);
        this.mHandler.post(sender);
    }

    public void reportFailLightScreen(int exceptionType, int keyCode, String packageName) {
        if (this.mHandler == null) {
            HwPCUtils.log(TAG, "fail to reportFailLightScreen, handler is null");
        } else if (this.mFailToLightHasReportOnce) {
            HwPCUtils.log(TAG, "mFailToLightHasReportOnce");
        } else {
            HwPCUtils.log(TAG, "start reportFailLightScreen");
            Map<Short, Object> eventParams = new HashMap<>();
            eventParams.put((short) 0, Integer.valueOf(exceptionType));
            eventParams.put((short) 1, Integer.valueOf(keyCode));
            eventParams.put((short) 2, packageName);
            EventSender sender = new EventSender();
            sender.setParams(eventParams, "reportFailToConnEvent", FAIL_TO_LIGHT_SCREEN_EVENT_ID);
            this.mHandler.post(sender);
            this.mFailToLightHasReportOnce = true;
        }
    }

    public void reportBadViewportEvent(DisplayInfo di) {
        if (this.mHandler == null) {
            HwPCUtils.log(TAG, "fail to reportBadViewportEvent, handler is null");
        } else if (this.mBadViewportHasReportOnce) {
            HwPCUtils.log(TAG, "mBadViewportHasReportOnce");
        } else if (di == null) {
            HwPCUtils.log(TAG, "not valid reportBadViewportEvent, displayInfo info is null");
        } else {
            HwPCUtils.log(TAG, "start reportBadViewportEvent");
            Map<Short, Object> eventParams = new HashMap<>();
            eventParams.put((short) 0, Integer.valueOf(di.type));
            eventParams.put((short) 1, Integer.valueOf(di.logicalDensityDpi));
            eventParams.put((short) 2, Integer.valueOf(di.logicalWidth));
            eventParams.put((short) 3, Integer.valueOf(di.logicalHeight));
            try {
                eventParams.put((short) 4, Integer.valueOf(Integer.parseInt(di.uniqueId)));
            } catch (NumberFormatException e) {
                HwPCUtils.log(TAG, "fail to convert uniqueId");
            }
            eventParams.put((short) 5, di.name);
            EventSender sender = new EventSender();
            sender.setParams(eventParams, "reportFailToConnEvent", BAD_VIEWPORT_EVENT_ID);
            this.mHandler.post(sender);
            this.mBadViewportHasReportOnce = true;
        }
    }
}

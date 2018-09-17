package com.android.server.display;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.DisplayEffectMonitor.MonitorModule;
import java.util.ArrayList;
import java.util.List;

class BackLightMonitorManager implements MonitorModule {
    private static final boolean HWFLOW;
    private static final int PARAM_MSG = 1;
    private static final String TAG = "BackLightMonitorManager";
    private static final int TIMER_MSG = 2;
    private List<MonitorModule> mChildMonitorList;
    private HandlerThread mHandlerThread;
    private MessageHandler mMessageHandler;

    private class MessageHandler extends Handler {
        public MessageHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                BackLightMonitorManager.this.processMonitorParam((ArrayMap) msg.obj);
            } else if (msg.what == 2) {
                BackLightMonitorManager.this.processUploadTimer();
            }
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    public BackLightMonitorManager(DisplayEffectMonitor monitor) {
        if (monitor != null) {
            this.mHandlerThread = new HandlerThread(TAG);
            this.mHandlerThread.start();
            this.mMessageHandler = new MessageHandler(this.mHandlerThread.getLooper());
            this.mChildMonitorList = new ArrayList();
            this.mChildMonitorList.add(new AmbientLightMonitor(monitor));
            this.mChildMonitorList.add(new BrightnessSeekBarMonitor(monitor));
            this.mChildMonitorList.add(new BrightnessSettingsMonitor(monitor));
            if (HWFLOW) {
                Slog.i(TAG, "new instance success");
            }
        }
    }

    public boolean isParamOwner(String paramType) {
        if (paramType == null) {
            return false;
        }
        for (MonitorModule module : this.mChildMonitorList) {
            if (module.isParamOwner(paramType)) {
                return true;
            }
        }
        return false;
    }

    public void sendMonitorParam(ArrayMap<String, Object> params) {
        if (this.mMessageHandler != null) {
            this.mMessageHandler.sendMessage(this.mMessageHandler.obtainMessage(1, params));
        }
    }

    public void triggerUploadTimer() {
        if (this.mMessageHandler != null) {
            this.mMessageHandler.sendMessage(this.mMessageHandler.obtainMessage(2));
        }
    }

    private void processMonitorParam(ArrayMap<String, Object> params) {
        String paramType = (String) params.get(MonitorModule.PARAM_TYPE);
        for (MonitorModule module : this.mChildMonitorList) {
            if (module.isParamOwner(paramType)) {
                module.sendMonitorParam(params);
            }
        }
    }

    private void processUploadTimer() {
        for (MonitorModule module : this.mChildMonitorList) {
            module.triggerUploadTimer();
        }
    }
}

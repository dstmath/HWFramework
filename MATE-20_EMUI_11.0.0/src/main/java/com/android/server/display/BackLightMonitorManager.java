package com.android.server.display;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.DisplayEffectMonitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* access modifiers changed from: package-private */
public class BackLightMonitorManager implements DisplayEffectMonitor.MonitorModule {
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final boolean IS_BACK_LIGHT_MONITOR_DISABLED = SystemProperties.getBoolean("ro.config.backlightmonitorthread.disable", false);
    private static final int MSG_PARAM = 0;
    private static final int MSG_STATE_TIMER = 2;
    private static final int MSG_UPLOAD_TIMER = 1;
    private static final String PARAM_ENABLE = "enable";
    private static final String TAG = "BackLightMonitorManager";
    public static final String TYPE_STATE_TIMER = "stateTimer";
    private static final String TYPE_XML_CONFIG = "xmlConfig";
    private List<DisplayEffectMonitor.MonitorModule> mChildMonitorList;
    private BackLightCommonData mCommonData;
    private HandlerThread mHandlerThread;
    private MessageHandler mMessageHandler;

    /* access modifiers changed from: private */
    public class MessageHandler extends Handler {
        MessageHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 0) {
                if (i == 1) {
                    BackLightMonitorManager.this.processUploadTimer();
                    return;
                } else if (i != 2) {
                    Slog.e(BackLightMonitorManager.TAG, "handleMessage unknow msg = " + msg.what);
                    return;
                }
            }
            BackLightMonitorManager.this.processMonitorParam((ArrayMap) msg.obj);
        }
    }

    BackLightMonitorManager(DisplayEffectMonitor monitor) {
        if (monitor != null) {
            if (IS_BACK_LIGHT_MONITOR_DISABLED) {
                Slog.w(TAG, "BackLightMonitorManager thread is disabled.");
            } else {
                this.mHandlerThread = new HandlerThread(TAG);
                this.mHandlerThread.start();
                this.mMessageHandler = new MessageHandler(this.mHandlerThread.getLooper());
            }
            this.mCommonData = new BackLightCommonData();
            boolean isCommercialVersion = false;
            int userType = SystemProperties.getInt("ro.logsystem.usertype", 0);
            isCommercialVersion = (userType == 1 || userType == 6) ? true : isCommercialVersion;
            this.mCommonData.setCommercialVersion(isCommercialVersion);
            this.mChildMonitorList = new ArrayList();
            this.mChildMonitorList.add(new AmbientLightMonitor(this));
            this.mChildMonitorList.add(new BrightnessSeekBarMonitor(monitor, this));
            this.mChildMonitorList.add(new BrightnessSettingsMonitor(monitor, this));
            this.mChildMonitorList.add(new BrightnessStateMonitor(monitor, this));
            if (HWFLOW) {
                Slog.i(TAG, "new instance success, isCommercialVersion=" + isCommercialVersion);
            }
        }
    }

    @Override // com.android.server.display.DisplayEffectMonitor.MonitorModule
    public boolean isParamOwner(String paramType) {
        if (paramType == null) {
            return false;
        }
        if (paramType.equals(TYPE_XML_CONFIG)) {
            return true;
        }
        for (DisplayEffectMonitor.MonitorModule module : this.mChildMonitorList) {
            if (module.isParamOwner(paramType)) {
                return true;
            }
        }
        return false;
    }

    @Override // com.android.server.display.DisplayEffectMonitor.MonitorModule
    public void sendMonitorParam(String paramType, ArrayMap<String, Object> params) {
        MessageHandler messageHandler = this.mMessageHandler;
        if (messageHandler != null) {
            this.mMessageHandler.sendMessage(messageHandler.obtainMessage(0, params));
        }
    }

    @Override // com.android.server.display.DisplayEffectMonitor.MonitorModule
    public void triggerUploadTimer() {
        MessageHandler messageHandler = this.mMessageHandler;
        if (messageHandler != null) {
            this.mMessageHandler.sendMessage(messageHandler.obtainMessage(1));
        }
    }

    public void setStateTimer(long delayMillis) {
        if (this.mMessageHandler != null && delayMillis > 0) {
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, TYPE_STATE_TIMER);
            this.mMessageHandler.sendMessageDelayed(this.mMessageHandler.obtainMessage(2, params), delayMillis);
        }
    }

    public void resetStateTimer() {
        MessageHandler messageHandler = this.mMessageHandler;
        if (messageHandler != null) {
            messageHandler.removeMessages(2);
        }
    }

    public BackLightCommonData getBackLightCommonData() {
        return this.mCommonData;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processMonitorParam(ArrayMap<String, Object> params) {
        String paramType = (String) params.get(DisplayEffectMonitor.MonitorModule.PARAM_TYPE);
        if (TYPE_XML_CONFIG.equals(paramType)) {
            receiveParamForXmlConfig(params);
            return;
        }
        for (DisplayEffectMonitor.MonitorModule module : this.mChildMonitorList) {
            if (module.isParamOwner(paramType)) {
                module.sendMonitorParam(paramType, params);
            }
        }
    }

    private void receiveParamForXmlConfig(ArrayMap<String, Object> params) {
        Object productEnableObj = params.get(PARAM_ENABLE);
        if (!(productEnableObj instanceof Boolean)) {
            Slog.e(TAG, "receiveParamForXmlConfig() can't get param: enable");
            return;
        }
        boolean isProductEnable = ((Boolean) productEnableObj).booleanValue();
        if (HWFLOW) {
            Slog.i(TAG, "receiveParamForXmlConfig() isProductEnable=" + isProductEnable);
        }
        this.mCommonData.setProductEnable(isProductEnable);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processUploadTimer() {
        for (DisplayEffectMonitor.MonitorModule module : this.mChildMonitorList) {
            module.triggerUploadTimer();
        }
    }

    public boolean needHourTimer() {
        return this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion();
    }

    public boolean needSceneRecognition() {
        return this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion();
    }

    public static <T extends Number> List<T> getParamList(ArrayMap<String, Object> params, Class<T> type, String name, int maxLength) {
        if (params == null) {
            return Collections.emptyList();
        }
        Object listObj = params.get(name);
        if (!(listObj instanceof List)) {
            Slog.e(TAG, "getParamList() can't get list: " + name);
            return Collections.emptyList();
        }
        List<T> list = (List) listObj;
        int length = list.size();
        if (length == 0 || length > maxLength) {
            Slog.e(TAG, "getParamList() list " + name + ", length error: " + length);
            return Collections.emptyList();
        }
        for (Object obj : list) {
            if (!type.isInstance(obj)) {
                Slog.e(TAG, "getParamList() list " + name + ", type error obj = " + obj.getClass());
                return Collections.emptyList();
            }
        }
        return list;
    }
}

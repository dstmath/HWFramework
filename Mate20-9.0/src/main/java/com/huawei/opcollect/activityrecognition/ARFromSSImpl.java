package com.huawei.opcollect.activityrecognition;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.text.TextUtils;
import com.huawei.android.location.activityrecognition.HwActivityChangedEvent;
import com.huawei.android.location.activityrecognition.HwActivityChangedExtendEvent;
import com.huawei.android.location.activityrecognition.HwActivityRecognition;
import com.huawei.android.location.activityrecognition.HwActivityRecognitionEvent;
import com.huawei.android.location.activityrecognition.HwActivityRecognitionHardwareSink;
import com.huawei.android.location.activityrecognition.HwActivityRecognitionServiceConnection;
import com.huawei.android.location.activityrecognition.HwEnvironmentChangedEvent;
import com.huawei.opcollect.collector.servicecollection.ARStatusAction;
import com.huawei.opcollect.utils.OPCollectLog;
import java.lang.ref.WeakReference;

public class ARFromSSImpl extends ARStatusAction.ARProvider {
    private static final String TAG = "ARFromSSImpl";
    private static final int TYPE_CONNECTED = 0;
    private static final int TYPE_SCREEN_OFF = 1;
    /* access modifiers changed from: private */
    public Context mContext;
    private HwActivityRecognition mHwActivityRecognition;
    private HwActivityRecognitionHardwareSink mHwActivityRecognitionHardwareSink = null;
    private HwActivityRecognitionServiceConnection mHwActivityRecognitionServiceConnection = null;
    private final Object mLock = new Object();

    private static class MyActivityRecognitionHardwareSink implements HwActivityRecognitionHardwareSink {
        private final WeakReference<ARFromSSImpl> service;

        MyActivityRecognitionHardwareSink(ARFromSSImpl service2) {
            this.service = new WeakReference<>(service2);
        }

        public void onActivityChanged(HwActivityChangedEvent activityChangedEvent) {
            if (activityChangedEvent != null) {
                ARFromSSImpl action = (ARFromSSImpl) this.service.get();
                if (action != null) {
                    for (HwActivityRecognitionEvent event : activityChangedEvent.getActivityRecognitionEvents()) {
                        if (event != null) {
                            int eventType = event.getEventType();
                            long timestampNs = event.getTimestampNs();
                            String activityType = event.getActivity();
                            if (!TextUtils.isEmpty(activityType)) {
                                int motionType = ARStatusAction.activityName2Type(activityType);
                                if (ARStatusAction.ActivityType.ACTIVITY_UNKNOWN.getType() != motionType) {
                                    action.storeARStatus(motionType, eventType, timestampNs);
                                }
                            }
                        }
                    }
                }
            }
        }

        public void onActivityExtendChanged(HwActivityChangedExtendEvent hwActivityChangedExtendEvent) {
        }

        public void onEnvironmentChanged(HwEnvironmentChangedEvent hwEnvironmentChangedEvent) {
        }
    }

    private static class MyActivityRecognitionServiceConnection implements HwActivityRecognitionServiceConnection {
        private final WeakReference<ARFromSSImpl> service;

        MyActivityRecognitionServiceConnection(ARFromSSImpl service2) {
            this.service = new WeakReference<>(service2);
        }

        public void onServiceConnected() {
            OPCollectLog.r(ARFromSSImpl.TAG, "onServiceConnected");
            ARFromSSImpl action = (ARFromSSImpl) this.service.get();
            if (action != null) {
                boolean screenStatus = false;
                PowerManager pm = null;
                if (action.mContext != null) {
                    pm = (PowerManager) action.mContext.getSystemService("power");
                }
                if (pm != null && Build.VERSION.SDK_INT >= 20) {
                    screenStatus = pm.isInteractive();
                }
                if (!action.enableAREvent(screenStatus ? 0 : 1)) {
                    OPCollectLog.i(ARFromSSImpl.TAG, "No supported activity.");
                }
            }
        }

        public void onServiceDisconnected() {
            OPCollectLog.r(ARFromSSImpl.TAG, "onServiceDisconnected()");
        }
    }

    public ARFromSSImpl(Context context, ARStatusAction action) {
        super(action);
        this.mContext = context;
    }

    public void enable() {
        synchronized (this.mLock) {
            if (this.mHwActivityRecognition == null) {
                this.mHwActivityRecognition = new HwActivityRecognition(this.mContext);
            }
            if (this.mHwActivityRecognitionHardwareSink == null) {
                this.mHwActivityRecognitionHardwareSink = new MyActivityRecognitionHardwareSink(this);
            }
            if (this.mHwActivityRecognitionServiceConnection == null) {
                this.mHwActivityRecognitionServiceConnection = new MyActivityRecognitionServiceConnection(this);
            }
            this.mHwActivityRecognition.connectService(this.mHwActivityRecognitionHardwareSink, this.mHwActivityRecognitionServiceConnection);
        }
    }

    public void disable() {
        synchronized (this.mLock) {
            if (this.mHwActivityRecognition != null) {
                this.mHwActivityRecognition.disconnectService();
                this.mHwActivityRecognitionServiceConnection = null;
                this.mHwActivityRecognitionHardwareSink = null;
                this.mHwActivityRecognition = null;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0128, code lost:
        if (r5 != false) goto L_0x0136;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x012a, code lost:
        if (r0 != false) goto L_0x0136;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x012c, code lost:
        if (r1 != false) goto L_0x0136;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x012e, code lost:
        if (r4 != false) goto L_0x0136;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0130, code lost:
        if (r6 != false) goto L_0x0136;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0132, code lost:
        if (r3 != false) goto L_0x0136;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0134, code lost:
        if (r2 == false) goto L_0x014e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:?, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:?, code lost:
        return false;
     */
    public boolean enableAREvent(int type) {
        OPCollectLog.r(TAG, "enableAREvent, type is " + type);
        long reportLantencyNs = type == 1 ? 200000000000L : ARStatusAction.REPORT_LATENCY_NS;
        boolean isEnableVehicle = false;
        boolean isEnableBicycle = false;
        boolean isEnableWalking = false;
        boolean isEnableRunning = false;
        boolean isEnableStill = false;
        boolean isEnableVeHighSpeedRail = false;
        boolean isEnableFastWalking = false;
        synchronized (this.mLock) {
            if (this.mHwActivityRecognition != null) {
                if (this.mHwActivityRecognition.getSupportedActivities().length == 0) {
                    OPCollectLog.i(TAG, "getSupportedActivities length is 0");
                    return false;
                }
                isEnableVehicle = this.mHwActivityRecognition.enableActivityEvent("android.activity_recognition.in_vehicle", 1, reportLantencyNs) && this.mHwActivityRecognition.enableActivityEvent("android.activity_recognition.in_vehicle", 2, reportLantencyNs);
                if (!isEnableVehicle) {
                    OPCollectLog.r(TAG, "enable vehicle enter exit failed.");
                }
                isEnableBicycle = this.mHwActivityRecognition.enableActivityEvent("android.activity_recognition.on_bicycle", 1, reportLantencyNs) && this.mHwActivityRecognition.enableActivityEvent("android.activity_recognition.on_bicycle", 2, reportLantencyNs);
                if (!isEnableBicycle) {
                    OPCollectLog.r(TAG, "enable bicycle enter exit failed.");
                }
                isEnableWalking = this.mHwActivityRecognition.enableActivityEvent("android.activity_recognition.walking", 1, reportLantencyNs) && this.mHwActivityRecognition.enableActivityEvent("android.activity_recognition.walking", 2, reportLantencyNs);
                if (!isEnableWalking) {
                    OPCollectLog.r(TAG, "enable walk enter exit failed.");
                }
                isEnableRunning = this.mHwActivityRecognition.enableActivityEvent("android.activity_recognition.running", 1, reportLantencyNs) && this.mHwActivityRecognition.enableActivityEvent("android.activity_recognition.running", 2, reportLantencyNs);
                if (!isEnableRunning) {
                    OPCollectLog.r(TAG, "enable running enter exit failed.");
                }
                isEnableStill = this.mHwActivityRecognition.enableActivityEvent("android.activity_recognition.still", 1, reportLantencyNs) && this.mHwActivityRecognition.enableActivityEvent("android.activity_recognition.still", 2, reportLantencyNs);
                if (!isEnableStill) {
                    OPCollectLog.r(TAG, "enable still enter exit failed.");
                }
                isEnableVeHighSpeedRail = this.mHwActivityRecognition.enableActivityEvent("android.activity_recognition.high_speed_rail", 1, reportLantencyNs) && this.mHwActivityRecognition.enableActivityEvent("android.activity_recognition.high_speed_rail", 2, reportLantencyNs);
                if (!isEnableVeHighSpeedRail) {
                    OPCollectLog.r(TAG, "enable high speed rail enter exit failed.");
                }
                isEnableFastWalking = this.mHwActivityRecognition.enableActivityEvent("android.activity_recognition.fast_walking", 1, reportLantencyNs) && this.mHwActivityRecognition.enableActivityEvent("android.activity_recognition.fast_walking", 2, reportLantencyNs);
                if (!isEnableFastWalking) {
                    OPCollectLog.r(TAG, "enable fast walking enter exit failed.");
                }
            }
        }
    }

    public boolean disableAREvent() {
        OPCollectLog.r(TAG, "disableAREvent.");
        boolean isDisableVehicle = false;
        boolean isDisableBicycle = false;
        boolean isDisableWalking = false;
        boolean isDisableRunning = false;
        boolean isDisableStill = false;
        boolean isEnableVeHighSpeedRail = false;
        boolean isEnableFastWalking = false;
        synchronized (this.mLock) {
            if (this.mHwActivityRecognition != null) {
                isDisableVehicle = this.mHwActivityRecognition.disableActivityEvent("android.activity_recognition.in_vehicle", 1) && this.mHwActivityRecognition.disableActivityEvent("android.activity_recognition.in_vehicle", 2);
                if (!isDisableVehicle) {
                    OPCollectLog.r(TAG, "disable vehicle enter exit failed.");
                }
                isDisableBicycle = this.mHwActivityRecognition.disableActivityEvent("android.activity_recognition.on_bicycle", 1) && this.mHwActivityRecognition.disableActivityEvent("android.activity_recognition.on_bicycle", 2);
                if (!isDisableBicycle) {
                    OPCollectLog.r(TAG, "disable bicycle enter exit failed.");
                }
                isDisableWalking = this.mHwActivityRecognition.disableActivityEvent("android.activity_recognition.walking", 1) && this.mHwActivityRecognition.disableActivityEvent("android.activity_recognition.walking", 2);
                if (!isDisableWalking) {
                    OPCollectLog.r(TAG, "disable walk enter exit failed.");
                }
                isDisableRunning = this.mHwActivityRecognition.disableActivityEvent("android.activity_recognition.running", 1) && this.mHwActivityRecognition.disableActivityEvent("android.activity_recognition.running", 2);
                if (!isDisableRunning) {
                    OPCollectLog.r(TAG, "disable running enter exit failed.");
                }
                isDisableStill = this.mHwActivityRecognition.disableActivityEvent("android.activity_recognition.still", 1) && this.mHwActivityRecognition.disableActivityEvent("android.activity_recognition.still", 2);
                if (!isDisableStill) {
                    OPCollectLog.r(TAG, "disable still enter exit failed.");
                }
                isEnableVeHighSpeedRail = this.mHwActivityRecognition.disableActivityEvent("android.activity_recognition.high_speed_rail", 1) && this.mHwActivityRecognition.disableActivityEvent("android.activity_recognition.high_speed_rail", 2);
                if (!isEnableVeHighSpeedRail) {
                    OPCollectLog.r(TAG, "disable high speed rail enter exit failed.");
                }
                isEnableFastWalking = this.mHwActivityRecognition.disableActivityEvent("android.activity_recognition.fast_walking", 1) && this.mHwActivityRecognition.disableActivityEvent("android.activity_recognition.fast_walking", 2);
                if (!isEnableFastWalking) {
                    OPCollectLog.r(TAG, "disable fast walking enter exit failed.");
                }
            }
        }
        if (isDisableVehicle || isDisableBicycle || isDisableWalking || isEnableFastWalking || isDisableStill || isDisableRunning || isEnableVeHighSpeedRail) {
            return true;
        }
        return false;
    }
}

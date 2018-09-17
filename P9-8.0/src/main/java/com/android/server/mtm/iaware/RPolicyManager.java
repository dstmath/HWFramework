package com.android.server.mtm.iaware;

import android.app.mtm.iaware.RSceneData;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.RPolicyData;
import com.android.server.mtm.iaware.policy.RPolicyCreator;

public class RPolicyManager implements RPolicyCreator {
    private static final /* synthetic */ int[] -android-rms-iaware-AwareConstant$FeatureTypeSwitchesValues = null;
    private static final int MSG_REPORT_SCENE = 1;
    private static final String TAG = "RPolicyManager";
    private final RPolicyCreator mDefaultRPolicyCreator = this;
    private final DispatchHandler mDispatchHandler;

    private final class DispatchHandler extends Handler {
        public DispatchHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            AwareLog.d(RPolicyManager.TAG, "handleMessage msg.what = " + msg.what);
            switch (msg.what) {
                case 1:
                    RSceneData scene = msg.obj;
                    RPolicyManager.this.getRPolicyCreator(FeatureType.getFeatureType(msg.arg1)).reportScene(scene);
                    return;
                default:
                    return;
            }
        }
    }

    private static /* synthetic */ int[] -getandroid-rms-iaware-AwareConstant$FeatureTypeSwitchesValues() {
        if (-android-rms-iaware-AwareConstant$FeatureTypeSwitchesValues != null) {
            return -android-rms-iaware-AwareConstant$FeatureTypeSwitchesValues;
        }
        int[] iArr = new int[FeatureType.values().length];
        try {
            iArr[FeatureType.FEATURE_ALL.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[FeatureType.FEATURE_APPACC.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[FeatureType.FEATURE_APPCLEANUP.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[FeatureType.FEATURE_APPFREEZE.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[FeatureType.FEATURE_APPHIBER.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[FeatureType.FEATURE_APPMNG.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[FeatureType.FEATURE_APPSTARTUP.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[FeatureType.FEATURE_BROADCAST.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[FeatureType.FEATURE_CPU.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[FeatureType.FEATURE_DEFRAG.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[FeatureType.FEATURE_DEVSCHED.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[FeatureType.FEATURE_INTELLI_REC.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[FeatureType.FEATURE_INVALIDE_TYPE.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[FeatureType.FEATURE_IO.ordinal()] = 14;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[FeatureType.FEATURE_IO_LIMIT.ordinal()] = 15;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[FeatureType.FEATURE_MEMORY.ordinal()] = 16;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[FeatureType.FEATURE_MEMORY2.ordinal()] = 17;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[FeatureType.FEATURE_NETWORK_TCP.ordinal()] = 18;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[FeatureType.FEATURE_RECG_FAKEACTIVITY.ordinal()] = 19;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[FeatureType.FEATURE_RESOURCE.ordinal()] = 20;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[FeatureType.FEATURE_VSYNC.ordinal()] = 21;
        } catch (NoSuchFieldError e21) {
        }
        -android-rms-iaware-AwareConstant$FeatureTypeSwitchesValues = iArr;
        return iArr;
    }

    public RPolicyManager(Context context, HandlerThread handlerThread) {
        this.mDispatchHandler = new DispatchHandler(handlerThread.getLooper());
    }

    public boolean reportScene(int featureId, RSceneData scene) {
        if (scene == null || FeatureType.getFeatureType(featureId) == FeatureType.FEATURE_INVALIDE_TYPE) {
            AwareLog.e(TAG, "reportScene error params");
            return false;
        }
        Message sceneMessage = this.mDispatchHandler.obtainMessage();
        sceneMessage.what = 1;
        sceneMessage.arg1 = featureId;
        sceneMessage.obj = scene;
        return this.mDispatchHandler.sendMessage(sceneMessage);
    }

    public RPolicyData acquirePolicyData(int featureId, RSceneData scene) {
        FeatureType featureType = FeatureType.getFeatureType(featureId);
        if (scene != null && featureType != FeatureType.FEATURE_INVALIDE_TYPE) {
            return getRPolicyCreator(featureType).createPolicyData(scene);
        }
        AwareLog.e(TAG, "acquirePolicyData error params");
        return null;
    }

    public RPolicyData createPolicyData(RSceneData scene) {
        AwareLog.d(TAG, "default createPolicyData");
        return null;
    }

    public void reportScene(RSceneData scene) {
        AwareLog.d(TAG, "default reportScene");
    }

    private RPolicyCreator getRPolicyCreator(FeatureType featureType) {
        int i = -getandroid-rms-iaware-AwareConstant$FeatureTypeSwitchesValues()[featureType.ordinal()];
        return this.mDefaultRPolicyCreator;
    }
}

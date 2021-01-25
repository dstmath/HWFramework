package com.android.server.mtm.iaware;

import android.app.mtm.iaware.SceneData;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.RPolicyData;
import com.android.server.mtm.iaware.policy.PolicyCreator;

public class PolicyManager implements PolicyCreator {
    private static final int MSG_REPORT_SCENE = 1;
    private static final String TAG = "PolicyManager";
    private final PolicyCreator mDefaultPolicyCreator = this;
    private final DispatchHandler mDispatchHandler;

    public PolicyManager(Context context, HandlerThread handlerThread) {
        this.mDispatchHandler = new DispatchHandler(handlerThread.getLooper());
    }

    public boolean reportScene(int featureId, SceneData scene) {
        if (scene == null || AwareConstant.FeatureType.getFeatureType(featureId) == AwareConstant.FeatureType.FEATURE_INVALIDE_TYPE) {
            AwareLog.e(TAG, "reportScene error params");
            return false;
        }
        Message sceneMessage = this.mDispatchHandler.obtainMessage();
        sceneMessage.what = 1;
        sceneMessage.arg1 = featureId;
        sceneMessage.obj = scene;
        return this.mDispatchHandler.sendMessage(sceneMessage);
    }

    public RPolicyData acquirePolicyData(int featureId, SceneData scene) {
        AwareConstant.FeatureType featureType = AwareConstant.FeatureType.getFeatureType(featureId);
        if (scene != null && featureType != AwareConstant.FeatureType.FEATURE_INVALIDE_TYPE) {
            return getPolicyCreator(featureType).createPolicyData(scene);
        }
        AwareLog.e(TAG, "acquirePolicyData error params");
        return null;
    }

    @Override // com.android.server.mtm.iaware.policy.PolicyCreator
    public RPolicyData createPolicyData(SceneData scene) {
        AwareLog.d(TAG, "default createPolicyData");
        return null;
    }

    @Override // com.android.server.mtm.iaware.policy.PolicyCreator
    public void reportScene(SceneData scene) {
        AwareLog.d(TAG, "default reportScene");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private PolicyCreator getPolicyCreator(AwareConstant.FeatureType featureType) {
        return this.mDefaultPolicyCreator;
    }

    private final class DispatchHandler extends Handler {
        public DispatchHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            AwareLog.d(PolicyManager.TAG, "handleMessage msg.what = " + msg.what);
            if (msg.what == 1) {
                AwareConstant.FeatureType featureType = AwareConstant.FeatureType.getFeatureType(msg.arg1);
                if (msg.obj instanceof SceneData) {
                    PolicyManager.this.getPolicyCreator(featureType).reportScene((SceneData) msg.obj);
                }
            }
        }
    }
}

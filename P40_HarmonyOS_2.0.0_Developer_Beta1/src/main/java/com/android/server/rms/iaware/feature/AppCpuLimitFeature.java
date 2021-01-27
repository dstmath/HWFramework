package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.os.Bundle;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.mtm.iaware.appmng.appcpulimit.AwareAppCpuLimitMng;
import com.android.server.rms.iaware.IRDataRegister;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppCpuLimitFeature extends RFeature {
    private static final int MIN_VERSION = 2;
    private static final String TAG = "AppCpuLimitFeature";
    private static AtomicBoolean sIsInitialized = new AtomicBoolean(false);

    public AppCpuLimitFeature(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        initConfig();
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        if (data == null) {
            return false;
        }
        AwareConstant.ResourceType type = AwareConstant.ResourceType.getResourceType(data.getResId());
        Bundle bundle = data.getBundle();
        int i = AnonymousClass1.$SwitchMap$android$rms$iaware$AwareConstant$ResourceType[type.ordinal()];
        if (i == 1) {
            if (bundle != null) {
                AwareAppCpuLimitMng.getInstance().report(bundle.getInt("relationType"), bundle);
            }
            return true;
        } else if (i != 2) {
            return false;
        } else {
            if (bundle != null) {
                AwareAppCpuLimitMng.getInstance().report(bundle.getInt("eventid"), bundle);
            }
            return true;
        }
    }

    /* renamed from: com.android.server.rms.iaware.feature.AppCpuLimitFeature$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$rms$iaware$AwareConstant$ResourceType = new int[AwareConstant.ResourceType.values().length];

        static {
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_APPASSOC.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_SHOW_INPUTMETHOD.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        if (this.mDataRegister != null) {
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_APPASSOC, this.mFeatureType);
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SHOW_INPUTMETHOD, this.mFeatureType);
        }
        AwareAppCpuLimitMng.disable();
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enableFeatureEx(int realVersion) {
        AwareLog.d(TAG, "enable App Cpu Limit Feature version:!" + realVersion);
        if (realVersion < 2) {
            return true;
        }
        if (this.mDataRegister != null) {
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_APPASSOC, this.mFeatureType);
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SHOW_INPUTMETHOD, this.mFeatureType);
        }
        AwareAppCpuLimitMng.setVersion(realVersion);
        AwareAppCpuLimitMng.enable();
        return true;
    }

    private void initConfig() {
        if (!sIsInitialized.get()) {
            AwareAppCpuLimitMng.getInstance();
            sIsInitialized.set(true);
        }
    }
}

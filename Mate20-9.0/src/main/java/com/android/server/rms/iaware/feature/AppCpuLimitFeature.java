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
    private static AtomicBoolean mIsInitialized = new AtomicBoolean(false);

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

    public AppCpuLimitFeature(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        initConfig();
    }

    public boolean reportData(CollectData data) {
        if (data == null) {
            return false;
        }
        AwareConstant.ResourceType type = AwareConstant.ResourceType.getResourceType(data.getResId());
        Bundle bAppSSoc = data.getBundle();
        switch (AnonymousClass1.$SwitchMap$android$rms$iaware$AwareConstant$ResourceType[type.ordinal()]) {
            case 1:
                if (bAppSSoc != null) {
                    AwareAppCpuLimitMng.getInstance().report(bAppSSoc.getInt("relationType"), bAppSSoc);
                }
                return true;
            case 2:
                if (bAppSSoc != null) {
                    AwareAppCpuLimitMng.getInstance().report(bAppSSoc.getInt("eventid"), bAppSSoc);
                }
                return true;
            default:
                return false;
        }
    }

    public boolean enable() {
        return false;
    }

    public boolean disable() {
        if (this.mIRDataRegister != null) {
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_APPASSOC, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SHOW_INPUTMETHOD, this.mFeatureType);
        }
        AwareAppCpuLimitMng.disable();
        return true;
    }

    public boolean enableFeatureEx(int realVersion) {
        AwareLog.d(TAG, "enable App Cpu Limit Feature version:!" + realVersion);
        if (realVersion >= 2) {
            if (this.mIRDataRegister != null) {
                this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_APPASSOC, this.mFeatureType);
                this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SHOW_INPUTMETHOD, this.mFeatureType);
            }
            AwareAppCpuLimitMng.enable();
        }
        return true;
    }

    private void initConfig() {
        if (!mIsInitialized.get()) {
            AwareAppCpuLimitMng.getInstance();
            mIsInitialized.set(true);
        }
    }
}

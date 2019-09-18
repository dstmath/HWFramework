package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.os.Bundle;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;
import com.android.server.mtm.iaware.appmng.appfreeze.AwareAppFreezeMng;
import com.android.server.rms.iaware.IRDataRegister;
import java.util.concurrent.atomic.AtomicBoolean;

public class APPFreezeFeature extends RFeature {
    private static final int MIN_VERSION = 2;
    private static final String TAG = "APPFreezeFeature";
    private static AtomicBoolean mIsInitialized = new AtomicBoolean(false);

    /* renamed from: com.android.server.rms.iaware.feature.APPFreezeFeature$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$rms$iaware$AwareConstant$ResourceType = new int[AwareConstant.ResourceType.values().length];

        static {
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_SCREEN_ON.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_SCREEN_OFF.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_STATUS_BAR.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_INSTALLER_MANAGER.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public APPFreezeFeature(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        initConfig();
    }

    public boolean reportData(CollectData data) {
        if (data == null) {
            return false;
        }
        AwareAppFreezeMng freeMng = AwareAppFreezeMng.getInstance();
        AwareConstant.ResourceType type = AwareConstant.ResourceType.getResourceType(data.getResId());
        switch (AnonymousClass1.$SwitchMap$android$rms$iaware$AwareConstant$ResourceType[type.ordinal()]) {
            case 1:
                if (freeMng != null) {
                    freeMng.report(20011);
                }
                return true;
            case 2:
                if (freeMng != null) {
                    freeMng.report(90011);
                }
                return true;
            case 3:
                Bundle bundle = data.getBundle();
                if (!(bundle == null || freeMng == null)) {
                    freeMng.report(bundle.getInt("eventid"));
                }
                return true;
            case 4:
                if (freeMng != null) {
                    freeMng.report(type, data);
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
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_STATUS_BAR, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_INSTALLER_MANAGER, this.mFeatureType);
        }
        AwareAppFreezeMng.getInstance().disable();
        return true;
    }

    public boolean enableFeatureEx(int realVersion) {
        if (realVersion >= 2) {
            if (this.mIRDataRegister != null) {
                this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_STATUS_BAR, this.mFeatureType);
                this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
                this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
                this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_INSTALLER_MANAGER, this.mFeatureType);
            }
            AwareAppFreezeMng.getInstance().enable();
        }
        return true;
    }

    private void initConfig() {
        if (!mIsInitialized.get()) {
            AwareAppFreezeMng.getInstance();
            mIsInitialized.set(true);
        }
    }
}

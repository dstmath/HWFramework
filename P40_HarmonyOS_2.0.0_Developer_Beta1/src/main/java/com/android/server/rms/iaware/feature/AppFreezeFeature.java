package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.os.Bundle;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;
import com.android.server.mtm.iaware.appmng.appfreeze.AwareAppFreezeMng;
import com.android.server.rms.iaware.IRDataRegister;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppFreezeFeature extends RFeature {
    private static final int MIN_VERSION = 2;
    private static final String TAG = "AppFreezeFeature";
    private static AtomicBoolean sIsInitialized = new AtomicBoolean(false);

    public AppFreezeFeature(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        initConfig();
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        if (data == null) {
            return false;
        }
        AwareAppFreezeMng freeMng = AwareAppFreezeMng.getInstance();
        AwareConstant.ResourceType type = AwareConstant.ResourceType.getResourceType(data.getResId());
        int i = AnonymousClass1.$SwitchMap$android$rms$iaware$AwareConstant$ResourceType[type.ordinal()];
        if (i == 1) {
            if (freeMng != null) {
                freeMng.report(20011);
            }
            return true;
        } else if (i == 2) {
            if (freeMng != null) {
                freeMng.report(90011);
            }
            return true;
        } else if (i == 3) {
            Bundle bundle = data.getBundle();
            if (!(bundle == null || freeMng == null)) {
                freeMng.report(bundle.getInt("eventid"));
            }
            return true;
        } else if (i != 4) {
            return false;
        } else {
            if (freeMng != null) {
                freeMng.report(type, data);
            }
            return true;
        }
    }

    /* renamed from: com.android.server.rms.iaware.feature.AppFreezeFeature$1  reason: invalid class name */
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

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        if (this.mDataRegister != null) {
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_STATUS_BAR, this.mFeatureType);
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_INSTALLER_MANAGER, this.mFeatureType);
        }
        AwareAppFreezeMng.getInstance().disable();
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enableFeatureEx(int realVersion) {
        if (realVersion < 2) {
            return true;
        }
        if (this.mDataRegister != null) {
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_STATUS_BAR, this.mFeatureType);
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_INSTALLER_MANAGER, this.mFeatureType);
        }
        AwareAppFreezeMng.getInstance().enable();
        return true;
    }

    private void initConfig() {
        if (!sIsInitialized.get()) {
            AwareAppFreezeMng.getInstance();
            sIsInitialized.set(true);
        }
    }
}

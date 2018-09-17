package com.android.server.rms.iaware.feature;

import android.app.mtm.iaware.appmng.AppMngConstant.AppMngFeature;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.CollectData;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.appmng.AwareAppMngDFX;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import com.android.server.rms.iaware.appmng.AwareFakeActivityRecg;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.appmng.AwareSceneRecognize;
import java.util.concurrent.atomic.AtomicBoolean;

public class APPMngFeature extends RFeature {
    private static final String TAG = "APPMngFeature";
    private static AtomicBoolean mIsInitialized = new AtomicBoolean(false);

    public APPMngFeature(Context context, FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
        initConfig();
    }

    public boolean reportData(CollectData data) {
        if (data == null) {
            return false;
        }
        Bundle bundle;
        if (data.getResId() == ResourceType.RESOURCE_APPASSOC.ordinal()) {
            bundle = data.getBundle();
            if (bundle == null) {
                return false;
            }
            if (isIAwareIIFeature(bundle)) {
                AwareIntelligentRecg.getInstance().report(bundle.getInt("relationType"), bundle);
            } else {
                AwareAppAssociate.getInstance().report(bundle.getInt("relationType"), bundle);
            }
            return true;
        } else if (data.getResId() == ResourceType.RESOURCE_USERHABIT.ordinal()) {
            return handleResourceUserHabit(data);
        } else {
            AwareSceneRecognize sceneRec;
            if (data.getResId() == ResourceType.RESOURCE_SCENE_REC.ordinal()) {
                bundle = data.getBundle();
                if (bundle == null) {
                    return false;
                }
                sceneRec = AwareSceneRecognize.getInstance();
                if (sceneRec != null) {
                    sceneRec.report(bundle.getInt("relationType"), bundle);
                }
                return true;
            } else if (data.getResId() == ResourceType.RES_APP.ordinal()) {
                sceneRec = AwareSceneRecognize.getInstance();
                if (sceneRec != null) {
                    sceneRec.reportActivityStart(data);
                }
                return true;
            } else if (data.getResId() == ResourceType.RESOURCE_SCREEN_OFF.ordinal()) {
                AwareIntelligentRecg.getInstance().reportScreenChangedTime(SystemClock.elapsedRealtime());
                AwareUserHabit habit = AwareUserHabit.getInstance();
                if (habit != null) {
                    habit.reportScreenState(data.getResId());
                }
                AwareIntelligentRecg.getInstance().report(90011, new Bundle());
                return true;
            } else if (data.getResId() != ResourceType.RESOURCE_SCREEN_ON.ordinal()) {
                return false;
            } else {
                AwareIntelligentRecg.getInstance().report(20011, new Bundle());
                return true;
            }
        }
    }

    private boolean handleResourceUserHabit(CollectData data) {
        Bundle bundle = data.getBundle();
        if (bundle == null) {
            return false;
        }
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit != null) {
            habit.report(bundle.getInt(AwareUserHabit.USERHABIT_INSTALL_APP_UPDATE), bundle);
        }
        AwareIntelligentRecg intlRecg = AwareIntelligentRecg.getInstance();
        if (intlRecg != null) {
            intlRecg.reportAppUpdate(bundle.getInt(AwareUserHabit.USERHABIT_INSTALL_APP_UPDATE), bundle);
        }
        AwareFakeActivityRecg recgFakeActivity = AwareFakeActivityRecg.self();
        if (recgFakeActivity != null) {
            recgFakeActivity.reportAppUpdate(bundle.getInt(AwareUserHabit.USERHABIT_INSTALL_APP_UPDATE), bundle);
        }
        return true;
    }

    public boolean enable() {
        if (this.mIRDataRegister == null) {
            return false;
        }
        this.mIRDataRegister.subscribeData(ResourceType.RESOURCE_APPASSOC, this.mFeatureType);
        this.mIRDataRegister.subscribeData(ResourceType.RESOURCE_USERHABIT, this.mFeatureType);
        this.mIRDataRegister.subscribeData(ResourceType.RESOURCE_SCENE_REC, this.mFeatureType);
        this.mIRDataRegister.subscribeData(ResourceType.RES_APP, this.mFeatureType);
        this.mIRDataRegister.subscribeData(ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
        this.mIRDataRegister.subscribeData(ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
        AwareAppMngSort.enable();
        AwareAppAssociate.enable();
        AwareAppKeyBackgroup.enable(this.mContext);
        AwareUserHabit.enable();
        AwareDefaultConfigList.enable(this.mContext);
        return true;
    }

    public boolean disable() {
        if (this.mIRDataRegister == null) {
            return false;
        }
        this.mIRDataRegister.unSubscribeData(ResourceType.RESOURCE_APPASSOC, this.mFeatureType);
        this.mIRDataRegister.unSubscribeData(ResourceType.RESOURCE_USERHABIT, this.mFeatureType);
        this.mIRDataRegister.unSubscribeData(ResourceType.RESOURCE_SCENE_REC, this.mFeatureType);
        this.mIRDataRegister.unSubscribeData(ResourceType.RES_APP, this.mFeatureType);
        this.mIRDataRegister.unSubscribeData(ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
        this.mIRDataRegister.unSubscribeData(ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
        AwareAppMngSort.disable();
        AwareAppAssociate.disable();
        AwareAppKeyBackgroup.disable();
        AwareDefaultConfigList.disable();
        AwareUserHabit.disable();
        return true;
    }

    public String saveBigData(boolean clear) {
        if (AwareAppMngSort.checkAppMngEnable()) {
            return AwareAppMngDFX.getInstance().getAppMngDfxData(clear);
        }
        return null;
    }

    public boolean configUpdate() {
        return updateHabitConfig();
    }

    private boolean updateHabitConfig() {
        Bundle bdl = new Bundle();
        bdl.putInt(AwareUserHabit.USERHABIT_INSTALL_APP_UPDATE, 4);
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit == null) {
            return false;
        }
        habit.report(4, bdl);
        return true;
    }

    private void initConfig() {
        if (!mIsInitialized.get()) {
            mIsInitialized.set(true);
            AppMngConfig.init();
            DecisionMaker.getInstance().updateRule(AppMngFeature.APP_CLEAN, this.mContext);
        }
    }

    private boolean isIAwareIIFeature(Bundle bundle) {
        boolean z = true;
        switch (bundle.getInt("relationType")) {
            case 8:
            case 9:
                if (bundle.getInt("windowtype") != 45) {
                    z = false;
                }
                return z;
            case 17:
            case 19:
            case 20:
            case 22:
                return true;
            default:
                return false;
        }
    }
}

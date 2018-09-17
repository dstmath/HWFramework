package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.os.Bundle;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.CollectData;
import android.rms.iaware.DumpData;
import android.rms.iaware.StatisticsData;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.appmng.AppMngDumpRadar;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.appmng.AwareAppMngDFX;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import com.android.server.rms.iaware.appmng.AwareSceneRecognize;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class APPMngFeature extends RFeature {
    private static final String TAG = "APPMngFeature";
    private static AtomicBoolean mIsInitialized;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.feature.APPMngFeature.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.feature.APPMngFeature.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.feature.APPMngFeature.<clinit>():void");
    }

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
            AwareAppAssociate.getInstance().report(bundle.getInt("relationType"), bundle);
            return true;
        } else if (data.getResId() == ResourceType.RESOURCE_USERHABIT.ordinal()) {
            bundle = data.getBundle();
            if (bundle == null) {
                return false;
            }
            AwareUserHabit habit = AwareUserHabit.getInstance();
            if (habit != null) {
                habit.report(bundle.getInt(AwareUserHabit.USERHABIT_INSTALL_APP_UPDATE), bundle);
            }
            return true;
        } else if (data.getResId() == ResourceType.RESOURCE_SCENE_REC.ordinal()) {
            bundle = data.getBundle();
            if (bundle == null) {
                return false;
            }
            sceneRec = AwareSceneRecognize.getInstance();
            if (sceneRec != null) {
                sceneRec.report(bundle.getInt("relationType"), bundle);
            }
            return true;
        } else if (data.getResId() != ResourceType.RES_APP.ordinal()) {
            return false;
        } else {
            sceneRec = AwareSceneRecognize.getInstance();
            if (sceneRec != null) {
                sceneRec.reportActivityStart(data);
            }
            return true;
        }
    }

    public boolean enable() {
        if (this.mIRDataRegister == null) {
            return false;
        }
        this.mIRDataRegister.subscribeData(ResourceType.RESOURCE_APPASSOC, this.mFeatureType);
        this.mIRDataRegister.subscribeData(ResourceType.RESOURCE_USERHABIT, this.mFeatureType);
        this.mIRDataRegister.subscribeData(ResourceType.RESOURCE_SCENE_REC, this.mFeatureType);
        this.mIRDataRegister.subscribeData(ResourceType.RES_APP, this.mFeatureType);
        AwareAppMngSort.enable();
        AwareAppAssociate.enable();
        AwareSceneRecognize.enable();
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
        AwareAppMngSort.disable();
        AwareSceneRecognize.disable();
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

    public ArrayList<DumpData> getDumpData(int time) {
        return AppMngDumpRadar.getInstance().getDumpData(time);
    }

    public ArrayList<StatisticsData> getStatisticsData() {
        return AppMngDumpRadar.getInstance().getStatisticsData();
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
        }
    }
}

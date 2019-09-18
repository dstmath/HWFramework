package com.android.server.rms.iaware.memory.data.dispatch;

import android.os.Bundle;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import com.android.server.rms.iaware.memory.data.handle.DataAppHandle;
import com.android.server.rms.iaware.memory.data.handle.DataDevStatusHandle;
import com.android.server.rms.iaware.memory.data.handle.DataInputHandle;
import com.android.server.rms.iaware.memory.utils.PrereadUtils;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataDispatch {
    private static final String TAG = "AwareMem_DataDispatch";
    private static DataDispatch sDataDispatch;
    private final AtomicBoolean mRunning = new AtomicBoolean(false);

    /* renamed from: com.android.server.rms.iaware.memory.data.dispatch.DataDispatch$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$rms$iaware$AwareConstant$ResourceType = new int[AwareConstant.ResourceType.values().length];

        static {
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RES_APP.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RES_INPUT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_SCREEN_ON.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_SCREEN_OFF.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_USERHABIT.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_FACE_RECOGNIZE.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    public static DataDispatch getInstance() {
        DataDispatch dataDispatch;
        synchronized (DataDispatch.class) {
            if (sDataDispatch == null) {
                sDataDispatch = new DataDispatch();
            }
            dataDispatch = sDataDispatch;
        }
        return dataDispatch;
    }

    public void start() {
        AwareLog.i(TAG, "start");
        this.mRunning.set(true);
    }

    public void stop() {
        AwareLog.i(TAG, "stop");
        this.mRunning.set(false);
    }

    public int reportData(CollectData data) {
        if (!this.mRunning.get() || data == null) {
            AwareLog.e(TAG, "DataDispatch not start");
            return -1;
        }
        long timestamp = data.getTimeStamp();
        int ret = -1;
        switch (AnonymousClass1.$SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.getResourceType(data.getResId()).ordinal()]) {
            case 1:
                AttrSegments attrSegments = parseCollectData(data);
                if (attrSegments.isValid()) {
                    ret = DataAppHandle.getInstance().reportData(timestamp, attrSegments.getEvent().intValue(), attrSegments);
                    break;
                }
                break;
            case 2:
                AttrSegments attrSegments2 = parseCollectData(data);
                if (attrSegments2.isValid()) {
                    ret = DataInputHandle.getInstance().reportData(timestamp, attrSegments2.getEvent().intValue(), attrSegments2);
                    break;
                }
                break;
            case 3:
                ret = DataDevStatusHandle.getInstance().reportData(timestamp, 20011, null);
                break;
            case 4:
                ret = DataDevStatusHandle.getInstance().reportData(timestamp, 90011, null);
                break;
            case 5:
                Bundle bundle = data.getBundle();
                if (bundle != null && 2 == bundle.getInt(AwareUserHabit.USERHABIT_INSTALL_APP_UPDATE)) {
                    String pkgName = bundle.getString(AwareUserHabit.USERHABIT_PACKAGE_NAME);
                    if (pkgName != null) {
                        PrereadUtils.getInstance();
                        PrereadUtils.removePackageFiles(pkgName);
                        break;
                    }
                }
                break;
            case 6:
                Bundle bundle2 = data.getBundle();
                if (bundle2 != null) {
                    ret = DataDevStatusHandle.getInstance().reportData(timestamp, bundle2.getInt("eventid"), null);
                    break;
                }
                break;
            default:
                AwareLog.e(TAG, "Invalid ResourceType");
                ret = -1;
                break;
        }
        return ret;
    }

    private AttrSegments parseCollectData(CollectData data) {
        String eventData = data.getData();
        AttrSegments.Builder builder = new AttrSegments.Builder();
        builder.addCollectData(eventData);
        return builder.build();
    }
}

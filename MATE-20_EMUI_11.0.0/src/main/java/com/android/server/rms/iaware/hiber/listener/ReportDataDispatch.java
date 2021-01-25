package com.android.server.rms.iaware.hiber.listener;

import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ReportDataDispatch {
    private static final Object LOCK = new Object();
    private static final String TAG = "AppHiber_ReportDataDispatch";
    private static ReportDataDispatch sReportDataDispatch = null;
    private DevStatusHandler mDevStatusHandler = DevStatusHandler.getInstance();
    private ResAppHandler mResAppDataHandler = ResAppHandler.getInstance();
    private ResInputHandler mResInputHandler = ResInputHandler.getInstance();
    private final AtomicBoolean mRunning = new AtomicBoolean(false);

    private ReportDataDispatch() {
    }

    public static ReportDataDispatch getInstance() {
        ReportDataDispatch reportDataDispatch;
        synchronized (LOCK) {
            if (sReportDataDispatch == null) {
                sReportDataDispatch = new ReportDataDispatch();
            }
            reportDataDispatch = sReportDataDispatch;
        }
        return reportDataDispatch;
    }

    public void start() {
        if (sReportDataDispatch == null) {
            AwareLog.w(TAG, "ReportDataDispatch is only permitted for system user");
            return;
        }
        AwareLog.d(TAG, "start");
        this.mRunning.set(true);
    }

    public void stop() {
        if (sReportDataDispatch == null) {
            AwareLog.w(TAG, "ReportDataDispatch is only permitted for system user");
            return;
        }
        AwareLog.d(TAG, "stop");
        this.mRunning.set(false);
    }

    public int reportData(CollectData data) {
        if (data == null) {
            AwareLog.e(TAG, "data is null, invalid");
            return -1;
        } else if (!this.mRunning.get()) {
            AwareLog.e(TAG, "dispatch not start");
            return -1;
        } else {
            long timeStamp = data.getTimeStamp();
            AwareConstant.ResourceType type = AwareConstant.ResourceType.getResourceType(data.getResId());
            if (type == AwareConstant.ResourceType.RESOURCE_SCREEN_ON || type == AwareConstant.ResourceType.RESOURCE_SCREEN_OFF) {
                int state = 20011;
                if (type == AwareConstant.ResourceType.RESOURCE_SCREEN_OFF) {
                    state = 90011;
                }
                return this.mDevStatusHandler.reportData(timeStamp, state, null);
            } else if (type == AwareConstant.ResourceType.RES_APP || type == AwareConstant.ResourceType.RES_INPUT) {
                String eventData = data.getData();
                AttrSegments.Builder builder = new AttrSegments.Builder();
                builder.addCollectData(eventData);
                AttrSegments attrSegments = builder.build();
                if (!attrSegments.isValid()) {
                    AwareLog.e(TAG, "Invalid collectData, or event");
                    return -1;
                } else if (type == AwareConstant.ResourceType.RES_APP) {
                    return this.mResAppDataHandler.reportData(timeStamp, attrSegments.getEvent().intValue(), attrSegments);
                } else {
                    return this.mResInputHandler.reportData(timeStamp, attrSegments.getEvent().intValue(), attrSegments);
                }
            } else {
                AwareLog.w(TAG, "Invalid ResourceType");
                return -1;
            }
        }
    }
}

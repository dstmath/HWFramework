package com.android.server.rms.iaware.hiber.listener;

import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ReportDataDispatch {
    private static final String TAG = "AppHiber_ReportDataDispatch";
    private static ReportDataDispatch mReportDataDispatch = null;
    private ResAppHandler mResAppDataHandler = ResAppHandler.getInstance();
    private ResInputHandler mResInputHandler = ResInputHandler.getInstance();
    private final AtomicBoolean mRunning = new AtomicBoolean(false);
    private DevStatusHandler msDevStatusHandler = DevStatusHandler.getInstance();

    private ReportDataDispatch() {
    }

    public static synchronized ReportDataDispatch getInstance() {
        ReportDataDispatch reportDataDispatch;
        synchronized (ReportDataDispatch.class) {
            if (mReportDataDispatch == null) {
                mReportDataDispatch = new ReportDataDispatch();
            }
            reportDataDispatch = mReportDataDispatch;
        }
        return reportDataDispatch;
    }

    public void start() {
        if (mReportDataDispatch == null) {
            AwareLog.w(TAG, "ReportDataDispatch is only permitted for system user");
            return;
        }
        AwareLog.d(TAG, "start");
        this.mRunning.set(true);
    }

    public void stop() {
        if (mReportDataDispatch == null) {
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
            long timestamp = data.getTimeStamp();
            AwareConstant.ResourceType type = AwareConstant.ResourceType.getResourceType(data.getResId());
            if (type == AwareConstant.ResourceType.RESOURCE_SCREEN_ON || type == AwareConstant.ResourceType.RESOURCE_SCREEN_OFF) {
                int state = 20011;
                if (AwareConstant.ResourceType.RESOURCE_SCREEN_OFF == type) {
                    state = 90011;
                }
                return this.msDevStatusHandler.reportData(timestamp, state, null);
            } else if (type == AwareConstant.ResourceType.RES_APP || type == AwareConstant.ResourceType.RES_INPUT) {
                String eventData = data.getData();
                AttrSegments.Builder builder = new AttrSegments.Builder();
                builder.addCollectData(eventData);
                AttrSegments attrSegments = builder.build();
                if (!attrSegments.isValid()) {
                    AwareLog.e(TAG, "Invalid collectData, or event");
                    return -1;
                } else if (AwareConstant.ResourceType.RES_APP == type) {
                    return this.mResAppDataHandler.reportData(timestamp, attrSegments.getEvent().intValue(), attrSegments);
                } else {
                    return this.mResInputHandler.reportData(timestamp, attrSegments.getEvent().intValue(), attrSegments);
                }
            } else {
                AwareLog.w(TAG, "Invalid ResourceType");
                return -1;
            }
        }
    }
}

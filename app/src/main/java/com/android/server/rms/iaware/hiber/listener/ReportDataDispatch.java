package com.android.server.rms.iaware.hiber.listener;

import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import com.android.server.rms.iaware.memory.data.content.AttrSegments.Builder;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ReportDataDispatch {
    private static final String TAG = "AppHiber_ReportDataDispatch";
    private static ReportDataDispatch sReportDataDispatch;
    private ResAppHandler mResAppDataHandler;
    private ResInputHandler mResInputHandler;
    private final AtomicBoolean mRunning;
    private DevStatusHandler msDevStatusHandler;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.hiber.listener.ReportDataDispatch.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.hiber.listener.ReportDataDispatch.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.hiber.listener.ReportDataDispatch.<clinit>():void");
    }

    private ReportDataDispatch() {
        this.mRunning = new AtomicBoolean(false);
        this.mResAppDataHandler = ResAppHandler.getInstance();
        this.msDevStatusHandler = DevStatusHandler.getInstance();
        this.mResInputHandler = ResInputHandler.getInstance();
    }

    public static synchronized ReportDataDispatch getInstance() {
        ReportDataDispatch reportDataDispatch;
        synchronized (ReportDataDispatch.class) {
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
        } else if (this.mRunning.get()) {
            long timestamp = data.getTimeStamp();
            ResourceType type = ResourceType.getResourceType(data.getResId());
            if (type == ResourceType.RESOURCE_SCREEN_ON || type == ResourceType.RESOURCE_SCREEN_OFF) {
                int state = 20011;
                if (ResourceType.RESOURCE_SCREEN_OFF == type) {
                    state = 90011;
                }
                return this.msDevStatusHandler.reportData(timestamp, state, null);
            } else if (type == ResourceType.RES_APP || type == ResourceType.RES_INPUT) {
                String eventData = data.getData();
                Builder builder = new Builder();
                builder.addCollectData(eventData);
                AttrSegments attrSegments = builder.build();
                if (!attrSegments.isValid()) {
                    AwareLog.e(TAG, "Invalid collectData, or event");
                    return -1;
                } else if (ResourceType.RES_APP == type) {
                    return this.mResAppDataHandler.reportData(timestamp, attrSegments.getEvent().intValue(), attrSegments);
                } else {
                    return this.mResInputHandler.reportData(timestamp, attrSegments.getEvent().intValue(), attrSegments);
                }
            } else {
                AwareLog.w(TAG, "Invalid ResourceType");
                return -1;
            }
        } else {
            AwareLog.e(TAG, "dispatch not start");
            return -1;
        }
    }
}

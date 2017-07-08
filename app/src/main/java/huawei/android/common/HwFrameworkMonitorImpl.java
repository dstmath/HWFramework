package huawei.android.common;

import android.common.HwFrameworkMonitor;
import android.content.Intent;
import android.os.Bundle;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import android.util.Slog;

public class HwFrameworkMonitorImpl implements HwFrameworkMonitor {
    private static final String TAG = "HwFrameworkMonitor";
    private static HwFrameworkMonitorImpl mInstance;
    private EventStream mEventStream;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.common.HwFrameworkMonitorImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.common.HwFrameworkMonitorImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.common.HwFrameworkMonitorImpl.<clinit>():void");
    }

    private HwFrameworkMonitorImpl() {
        this.mEventStream = null;
    }

    public static synchronized HwFrameworkMonitorImpl getInstance() {
        HwFrameworkMonitorImpl hwFrameworkMonitorImpl;
        synchronized (HwFrameworkMonitorImpl.class) {
            if (mInstance == null) {
                mInstance = new HwFrameworkMonitorImpl();
            }
            hwFrameworkMonitorImpl = mInstance;
        }
        return hwFrameworkMonitorImpl;
    }

    public boolean monitor(int sceneId, Bundle params) {
        this.mEventStream = IMonitor.openEventStream(sceneId);
        if (this.mEventStream == null) {
            return false;
        }
        if (params != null) {
            switch (sceneId) {
                case 907400000:
                    this.mEventStream.setParam((short) 0, params.getString("package", "unknown"));
                    this.mEventStream.setParam((short) 1, params.getString("versionName", "unknown"));
                    this.mEventStream.setParam((short) 3, params.getString("extra", "unknown"));
                    break;
                case 907400002:
                    this.mEventStream.setParam((short) 0, params.getString("package", "unknown"));
                    this.mEventStream.setParam((short) 1, params.getString("versionName", "unknown"));
                    this.mEventStream.setParam((short) 3, params.getString("action", "unknown"));
                    this.mEventStream.setParam((short) 4, params.getInt("actionCount", 0));
                    this.mEventStream.setParam((short) 5, Boolean.valueOf(params.getBoolean("mmsFlag", false)));
                    this.mEventStream.setParam((short) 6, params.getString("receiver", "unknown"));
                    this.mEventStream.setParam((short) 7, params.getString("package", "unknown"));
                    break;
                case 907400003:
                    this.mEventStream.setParam((short) 0, params.getString("package", "unknown"));
                    this.mEventStream.setParam((short) 1, params.getString("versionName", "unknown"));
                    this.mEventStream.setParam((short) 3, params.getString("action", "unknown"));
                    this.mEventStream.setParam((short) 4, params.getFloat("receiveTime", 0.0f));
                    this.mEventStream.setParam((short) 5, params.getString("receiver", "unknown"));
                    Object objIntent = params.getParcelable("intent");
                    if (objIntent != null) {
                        this.mEventStream.setParam((short) 6, ((Intent) objIntent).toString());
                        break;
                    }
                    break;
                case 907400016:
                    this.mEventStream.setParam((short) 0, params.getString("cpuState", "unknown"));
                    this.mEventStream.setParam((short) 1, params.getString("cpuTime", "unknown"));
                    this.mEventStream.setParam((short) 2, params.getString("extra", "unknown"));
                    break;
            }
        }
        boolean result = IMonitor.sendEvent(this.mEventStream);
        Slog.i(TAG, "Monitor for " + sceneId);
        IMonitor.closeEventStream(this.mEventStream);
        return result;
    }
}

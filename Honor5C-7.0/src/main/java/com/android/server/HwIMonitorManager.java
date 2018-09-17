package com.android.server;

import android.util.IMonitor;
import android.util.IMonitor.EventStream;

public class HwIMonitorManager {
    public static final short FAIL_REASON_VARCHAR = (short) 0;
    public static final String TAG = "HwIMonitorManager";
    private static HwIMonitorManager mHwIMonitorManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.HwIMonitorManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.HwIMonitorManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.HwIMonitorManager.<clinit>():void");
    }

    public static synchronized HwIMonitorManager getInstance() {
        HwIMonitorManager hwIMonitorManager;
        synchronized (HwIMonitorManager.class) {
            if (mHwIMonitorManager == null) {
                mHwIMonitorManager = new HwIMonitorManager();
            }
            hwIMonitorManager = mHwIMonitorManager;
        }
        return hwIMonitorManager;
    }

    public boolean uploadBtRadarEvent(int event, String exception) {
        if (exception == null) {
            return false;
        }
        EventStream eStream = IMonitor.openEventStream(event);
        if (eStream != null) {
            eStream.setParam(FAIL_REASON_VARCHAR, exception);
        }
        return uploadIMonitorEvent(eStream);
    }

    private boolean uploadIMonitorEvent(EventStream eStream) {
        if (eStream == null) {
            HwLog.d(TAG, "eStream is null!");
            return false;
        }
        boolean ret;
        synchronized (this) {
            ret = IMonitor.sendEvent(eStream);
            IMonitor.closeEventStream(eStream);
        }
        return ret;
    }
}

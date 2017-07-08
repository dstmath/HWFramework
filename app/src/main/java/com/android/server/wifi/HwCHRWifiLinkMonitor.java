package com.android.server.wifi;

import android.util.Log;

class HwCHRWifiLinkMonitor {
    private static final String DEBUGWL_FILENAME = "/sys/kernel/debug/bcmdhd/debug_wl_counters";
    private static final String TAG = "HwCHRWifiLinkMonitor";
    private static HwCHRWifiLinkMonitor monitor;
    private HwCHRWifiBcmIncrCounterLst bcmLst;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.HwCHRWifiLinkMonitor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.HwCHRWifiLinkMonitor.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.HwCHRWifiLinkMonitor.<clinit>():void");
    }

    public HwCHRWifiLinkMonitor() {
        this.bcmLst = null;
        this.bcmLst = new HwCHRWifiBcmIncrCounterLst();
    }

    public static synchronized HwCHRWifiLinkMonitor getDefault() {
        HwCHRWifiLinkMonitor hwCHRWifiLinkMonitor;
        synchronized (HwCHRWifiLinkMonitor.class) {
            if (monitor == null) {
                monitor = new HwCHRWifiLinkMonitor();
            }
            hwCHRWifiLinkMonitor = monitor;
        }
        return hwCHRWifiLinkMonitor;
    }

    public void runCounters() {
        HwCHRWifiBCMCounterReader reader = new HwCHRWifiBCMCounterReader();
        reader.parseValue(HwCHRWifiFile.getFileResult(DEBUGWL_FILENAME));
        this.bcmLst.updateIncrCounters(reader);
        Log.e(TAG, this.bcmLst.toString());
    }

    public HwCHRWifiBcmIncrCounterLst getCounterLst() {
        return this.bcmLst;
    }

    public void setCounterLst(HwCHRWifiBcmIncrCounterLst src) {
        this.bcmLst = src;
    }
}

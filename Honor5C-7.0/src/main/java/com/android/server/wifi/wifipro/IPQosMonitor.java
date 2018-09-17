package com.android.server.wifi.wifipro;

import android.os.Handler;
import android.util.Log;

public class IPQosMonitor {
    private static final int CMD_GET_RTT = 19;
    private static final int CMD_NOTIFY_MCC = 17;
    private static final int CMD_QUERY_PKTS = 15;
    private static final int CMD_QUERY_RETRANS_RTO = 16;
    private static final int CMD_RESET_RTT = 18;
    private static final int CMD_START_MONITOR = 10;
    private static final int CMD_STOP_MONITOR = 11;
    public static final int MSG_REPORT_IPQOS = 100;
    private static String TAG = null;
    private static final int WIFIPRO_MOBILE_BQE_RTT = 2;
    private static final int WIFIPRO_WLAN_BQE_RTT = 1;
    private static final int WIFIPRO_WLAN_SAMPLE_RTT = 3;
    private static boolean isAPK;
    private Handler mHandler;
    private boolean mInit;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.IPQosMonitor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.IPQosMonitor.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.IPQosMonitor.<clinit>():void");
    }

    private native int init(int i);

    private static native int registerNatives();

    private native int release(int i);

    public native int sendcmd(int i, int i2);

    public IPQosMonitor(Handler h) {
        this.mInit = false;
        this.mHandler = h;
        if (!this.mInit) {
            init(0);
            this.mInit = true;
        }
    }

    private synchronized void postEventFromNative(int what, int arg1, int arg2, Object obj) {
        Log.i(TAG, "postEventFromNative: msg=" + what + ",arg1=" + arg1 + ",arg2=" + arg2);
        if (this.mHandler != null) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(what, arg1, arg2, obj));
        }
    }

    public void init() {
        if (!this.mInit) {
            init(0);
            this.mInit = true;
        }
    }

    public void release() {
        if (this.mInit) {
            this.mInit = false;
            release(0);
        }
    }

    public void startMonitor() {
        if (this.mInit) {
            sendcmd(CMD_START_MONITOR, 0);
        } else {
            Log.e(TAG, "start:IPQos is not initial!!");
        }
    }

    public void stopMonitor() {
        if (this.mInit) {
            sendcmd(CMD_STOP_MONITOR, 0);
        } else {
            Log.e(TAG, "stop:IPQos is not initial!!");
        }
    }

    public synchronized void queryPackets(int arg) {
        if (this.mInit) {
            sendcmd(CMD_QUERY_PKTS, arg);
        } else {
            Log.e(TAG, "query:IPQos is not initial!!");
        }
    }

    public void notifyMCC(int mcc_code) {
        if (this.mInit) {
            sendcmd(CMD_NOTIFY_MCC, mcc_code);
        } else {
            Log.e(TAG, "notifyMCC:IPQos is not initial!!");
        }
    }

    public void resetRtt(int rtt_type) {
        if (!this.mInit) {
            Log.e(TAG, "resetRtt:IPQos is not initial!!");
        } else if (rtt_type == WIFIPRO_WLAN_BQE_RTT || rtt_type == WIFIPRO_MOBILE_BQE_RTT || rtt_type == WIFIPRO_WLAN_SAMPLE_RTT) {
            Log.i(TAG, "resetRtt: rtt_type = " + rtt_type);
            sendcmd(CMD_RESET_RTT, rtt_type);
        } else {
            Log.e(TAG, "resetRtt: unvalid rtt_type!!");
        }
    }

    public void queryRtt(int rtt_type) {
        if (!this.mInit) {
            Log.e(TAG, "queryRtt:IPQos is not initial!!");
        } else if (rtt_type == WIFIPRO_WLAN_BQE_RTT || rtt_type == WIFIPRO_MOBILE_BQE_RTT || rtt_type == WIFIPRO_WLAN_SAMPLE_RTT) {
            Log.i(TAG, "queryRtt: rtt_type = " + rtt_type);
            sendcmd(CMD_GET_RTT, rtt_type);
        } else {
            Log.e(TAG, "queryRtt: unvalid rtt_type!!");
        }
    }
}

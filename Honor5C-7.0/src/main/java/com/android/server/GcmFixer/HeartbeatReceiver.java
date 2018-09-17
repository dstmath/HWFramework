package com.android.server.GcmFixer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Slog;

public class HeartbeatReceiver extends BroadcastReceiver {
    private static final Intent GTALK_HEART_BEAT_INTENT = null;
    public static final String HEARTBEAT_FIXER_ACTION = "com.android.intent.action.HEARTBEAT_FIXER";
    private static final Intent MCS_MCS_HEARTBEAT_INTENT = null;
    private static final String TAG = "HeartbeatReceiver";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.GcmFixer.HeartbeatReceiver.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.GcmFixer.HeartbeatReceiver.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.GcmFixer.HeartbeatReceiver.<clinit>():void");
    }

    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(GTALK_HEART_BEAT_INTENT);
        context.sendBroadcast(MCS_MCS_HEARTBEAT_INTENT);
        Slog.i(TAG, "Sent heartbeat request...");
        GcmHeartBeatFixer.scheduleHeartbeatRequest(context, false, false);
    }
}

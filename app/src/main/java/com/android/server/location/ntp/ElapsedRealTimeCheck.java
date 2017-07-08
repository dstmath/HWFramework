package com.android.server.location.ntp;

import android.os.SystemClock;
import android.util.Log;

public class ElapsedRealTimeCheck {
    private static boolean DBG = false;
    private static final long MAX_MISS_MS = 24000;
    private static final String TAG = "NtpElapsedRealTimeCheck";
    private static ElapsedRealTimeCheck mElapsedRealTimeCheck;
    private boolean mCanTrustElapsedRealTime;
    private long mTimeBegin;
    private long mTimeBeginElapsed;
    private long mTimeCheck;
    private long mTimeCheckElapsed;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.ntp.ElapsedRealTimeCheck.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.ntp.ElapsedRealTimeCheck.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.ntp.ElapsedRealTimeCheck.<clinit>():void");
    }

    public static synchronized ElapsedRealTimeCheck getInstance() {
        ElapsedRealTimeCheck elapsedRealTimeCheck;
        synchronized (ElapsedRealTimeCheck.class) {
            if (mElapsedRealTimeCheck == null) {
                mElapsedRealTimeCheck = new ElapsedRealTimeCheck();
            }
            elapsedRealTimeCheck = mElapsedRealTimeCheck;
        }
        return elapsedRealTimeCheck;
    }

    private ElapsedRealTimeCheck() {
        this.mCanTrustElapsedRealTime = true;
    }

    public void checkRealTime(long time) {
        if (this.mTimeBegin == 0) {
            this.mTimeBegin = time;
            this.mTimeBeginElapsed = SystemClock.elapsedRealtime();
        } else if (this.mTimeCheck == 0) {
            this.mTimeCheck = time;
            this.mTimeCheckElapsed = SystemClock.elapsedRealtime();
        }
        if (this.mTimeBegin != 0 && this.mTimeCheck != 0) {
            long missTime = (this.mTimeCheck - this.mTimeBegin) - (this.mTimeCheckElapsed - this.mTimeBeginElapsed);
            if (DBG) {
                Log.d(TAG, "checkRealTime missTime:" + missTime);
            }
            if (Math.abs(missTime) >= MAX_MISS_MS) {
                this.mCanTrustElapsedRealTime = false;
            } else {
                this.mCanTrustElapsedRealTime = true;
            }
            this.mTimeCheck = 0;
            this.mTimeCheckElapsed = 0;
        }
    }

    public boolean canTrustElapsedRealTime() {
        return this.mCanTrustElapsedRealTime;
    }
}

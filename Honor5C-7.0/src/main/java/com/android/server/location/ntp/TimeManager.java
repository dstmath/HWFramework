package com.android.server.location.ntp;

import android.os.SystemClock;
import android.util.Log;

public class TimeManager {
    private static boolean DBG;
    private static final long INVAILID_TIME = 0;
    private long mExpireTime;
    private String mTag;
    private long mTimeSynsBoot;
    private long mTimestamp;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.ntp.TimeManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.ntp.TimeManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.ntp.TimeManager.<clinit>():void");
    }

    public TimeManager(String tag, long expireTime) {
        this.mExpireTime = expireTime;
        this.mTag = tag;
    }

    public long getCurrentTime() {
        if (this.mTimestamp == 0) {
            return 0;
        }
        if (ElapsedRealTimeCheck.getInstance().canTrustElapsedRealTime()) {
            long timeTillNow = SystemClock.elapsedRealtime() - this.mTimeSynsBoot;
            if (timeTillNow >= this.mExpireTime) {
                if (DBG) {
                    Log.d(this.mTag, "getCurrentTime INVAILID_TIME");
                }
                return 0;
            }
            if (DBG) {
                Log.d(this.mTag, "getCurrentTime:" + (this.mTimestamp + timeTillNow));
            }
            return this.mTimestamp + timeTillNow;
        }
        if (DBG) {
            Log.d(this.mTag, "getCurrentTime ElapsedRealTime INVAILID_TIME");
        }
        return 0;
    }

    public void setCurrentTime(long msTime, long msTimeSynsBoot) {
        this.mTimestamp = msTime;
        this.mTimeSynsBoot = msTimeSynsBoot;
        if (DBG) {
            Log.d(this.mTag, "setCurrentTime mTimestamp:" + this.mTimestamp + " mTimeReference:" + this.mTimeSynsBoot);
        }
    }

    public long getmTimestamp() {
        return this.mTimestamp;
    }
}

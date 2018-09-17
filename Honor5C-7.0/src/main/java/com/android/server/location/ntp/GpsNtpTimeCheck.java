package com.android.server.location.ntp;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import com.android.server.location.HwGpsLogServices;
import com.android.server.location.InjectTimeRecord;

public class GpsNtpTimeCheck {
    private static boolean DBG = false;
    private static final long INVAILID_TIME = 0;
    private static final long MISSTAKE_TIME = 50000;
    private static final String TAG = "HwGpsNtpTimeCheck";
    private static GpsNtpTimeCheck mGpsNtpTimeCheck;
    private Context mContext;
    private GpsTimeManager mGpsTimeManager;
    private NitzTimeManager mNitzTimeManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.ntp.GpsNtpTimeCheck.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.ntp.GpsNtpTimeCheck.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.ntp.GpsNtpTimeCheck.<clinit>():void");
    }

    public static synchronized GpsNtpTimeCheck getInstance(Context context) {
        GpsNtpTimeCheck gpsNtpTimeCheck;
        synchronized (GpsNtpTimeCheck.class) {
            if (mGpsNtpTimeCheck == null) {
                mGpsNtpTimeCheck = new GpsNtpTimeCheck(context);
            }
            gpsNtpTimeCheck = mGpsNtpTimeCheck;
        }
        return gpsNtpTimeCheck;
    }

    private GpsNtpTimeCheck(Context context) {
        this.mContext = context;
        this.mGpsTimeManager = new GpsTimeManager();
        this.mNitzTimeManager = new NitzTimeManager(context);
        if (DBG) {
            Log.d(TAG, " created");
        }
    }

    public boolean checkNtpTime(long ntpMsTime, long msTimeSynsBoot) {
        long currentNtpTime = (SystemClock.elapsedRealtime() + ntpMsTime) - msTimeSynsBoot;
        if (this.mGpsTimeManager.getGpsTime() != INVAILID_TIME) {
            return compareTime(currentNtpTime, this.mGpsTimeManager.getGpsTime());
        }
        if (this.mNitzTimeManager.getNitzTime() != INVAILID_TIME) {
            return compareTime(currentNtpTime, this.mNitzTimeManager.getNitzTime());
        }
        if (DBG) {
            Log.d(TAG, "checkNtpTime return false");
        }
        return false;
    }

    private boolean compareTime(long currentNtpTime, long compareTime) {
        if (DBG) {
            Log.d(TAG, "compareTime currentNtpTime:" + currentNtpTime + " compareTime:" + compareTime);
        }
        long misstake = Math.abs(currentNtpTime - compareTime);
        if (misstake > MISSTAKE_TIME) {
            if (DBG) {
                Log.d(TAG, "find error ntp time:" + misstake);
            }
            HwGpsLogServices.getInstance(this.mContext).reportErrorNtpTime(currentNtpTime, compareTime);
            return false;
        }
        if (DBG) {
            Log.d(TAG, "compareTime return true");
        }
        return true;
    }

    public void setGpsTime(long gpsMsTime, long nanosSynsBoot) {
        this.mGpsTimeManager.setGpsTime(gpsMsTime, nanosSynsBoot);
    }

    public InjectTimeRecord getInjectTime(long ntpTime) {
        return MajorityTimeManager.getInjectTime(this.mContext, this.mGpsTimeManager.getGpsTime(), ntpTime, this.mNitzTimeManager.getNitzTime());
    }
}

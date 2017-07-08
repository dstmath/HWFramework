package com.android.server.radar;

import android.util.LogException;
import android.util.Slog;
import java.util.HashMap;

public class FrameworkRadar {
    private static final int BODY_MAX_SIZE = 512;
    private static boolean DEBUG = false;
    public static final int LEVEL_A = 65;
    public static final int LEVEL_B = 66;
    public static final int LEVEL_C = 67;
    private static final long ONE_DAY_MILL_SEC = 0;
    public static final int RADAR_FWK_ERR_APP_CRASH_AT_START = 2802;
    public static final int RADAR_FWK_ERR_INSTALL_SD = 2700;
    public static final int RADAR_FWK_ERR_INSTALL_SHARED_UID = 2701;
    private static final String TAG = "FrameworkRadar";
    private static LogException mLogException;
    private static HashMap<Integer, Long> mLogTime;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.radar.FrameworkRadar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.radar.FrameworkRadar.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.radar.FrameworkRadar.<clinit>():void");
    }

    private static boolean isIntervalLargeEnough(int scene) {
        synchronized (mLogTime) {
            if (mLogTime.get(Integer.valueOf(scene)) != null) {
                if (System.currentTimeMillis() - (mLogTime.get(Integer.valueOf(scene)) == null ? ONE_DAY_MILL_SEC : ((Long) mLogTime.get(Integer.valueOf(scene))).longValue()) < ONE_DAY_MILL_SEC) {
                    return false;
                }
            }
            return true;
        }
    }

    private static void updateSceneTimestamp(int scene) {
        synchronized (mLogTime) {
            mLogTime.put(Integer.valueOf(scene), Long.valueOf(System.currentTimeMillis()));
        }
    }

    public static void msg(RadarHeader radarHeader, String reason) {
        if (radarHeader != null && mLogException != null && reason != null) {
            int scene = radarHeader.getScene();
            if (isIntervalLargeEnough(scene)) {
                String header = radarHeader.getRadarHeader();
                String body = new StringBuilder(BODY_MAX_SIZE).append("Reason:").append(reason).append("\n").toString();
                mLogException.msg("framework", radarHeader.getLevel(), header, body);
                if (DEBUG) {
                    Slog.w(TAG, header + body);
                }
                updateSceneTimestamp(scene);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void msg(int level, int scene, String func, String reason) {
        if (func != null && mLogException != null && isIntervalLargeEnough(scene)) {
            String header = new RadarHeader("PMS", "0", 100, scene, level).getRadarHeader();
            String body = new StringBuilder(BODY_MAX_SIZE).append("Failfunc:").append(func).append(";").append("Reason:").append(reason).append("\n").toString();
            mLogException.msg("framework", level, header, body);
            if (DEBUG) {
                Slog.w(TAG, header + body);
            }
            updateSceneTimestamp(scene);
        }
    }
}

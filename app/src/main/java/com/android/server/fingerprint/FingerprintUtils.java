package com.android.server.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.os.SystemVibrator;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import java.util.List;

public class FingerprintUtils {
    private static final long[] FP_ERROR_VIBRATE_PATTERN = null;
    private static final long[] FP_SUCCESS_VIBRATE_PATTERN = null;
    private static final boolean FRONT_FINGERPRINT_NAVIGATION = false;
    private static final int FRONT_FP_ERROR_VIBRATE_PATTERN = 14;
    private static final long[] HW_FP_ERROR_VIBRATE_PATTERN = null;
    private static FingerprintUtils sInstance;
    private static final Object sInstanceLock = null;
    @GuardedBy("this")
    private final SparseArray<FingerprintsUserState> mUsers;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.fingerprint.FingerprintUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.fingerprint.FingerprintUtils.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.fingerprint.FingerprintUtils.<clinit>():void");
    }

    public static FingerprintUtils getInstance() {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new FingerprintUtils();
            }
        }
        return sInstance;
    }

    private FingerprintUtils() {
        this.mUsers = new SparseArray();
    }

    public List<Fingerprint> getFingerprintsForUser(Context ctx, int userId) {
        return getStateForUser(ctx, userId).getFingerprints();
    }

    public void addFingerprintForUser(Context ctx, int fingerId, int userId) {
        getStateForUser(ctx, userId).addFingerprint(fingerId, userId);
    }

    public void removeFingerprintIdForUser(Context ctx, int fingerId, int userId) {
        getStateForUser(ctx, userId).removeFingerprint(fingerId);
    }

    public void renameFingerprintForUser(Context ctx, int fingerId, int userId, CharSequence name) {
        if (!TextUtils.isEmpty(name)) {
            getStateForUser(ctx, userId).renameFingerprint(fingerId, name);
        }
    }

    public static void vibrateFingerprintError(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Vibrator.class);
        if (vibrator != null) {
            vibrator.vibrate(FP_ERROR_VIBRATE_PATTERN, -1);
        }
    }

    public static void vibrateFingerprintErrorHw(Context context) {
        if (context != null) {
            if (FRONT_FINGERPRINT_NAVIGATION) {
                Vibrator vibrator = (Vibrator) context.getSystemService("vibrator");
                if (vibrator != null) {
                    SystemVibrator sysVibrator = (SystemVibrator) vibrator;
                    if (sysVibrator != null) {
                        sysVibrator.hwVibrate(null, FRONT_FP_ERROR_VIBRATE_PATTERN);
                    }
                }
            } else {
                vibrateFingerprintError(context);
            }
        }
    }

    public static void vibrateFingerprintSuccess(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Vibrator.class);
        if (vibrator != null) {
            vibrator.vibrate(FP_SUCCESS_VIBRATE_PATTERN, -1);
        }
    }

    private FingerprintsUserState getStateForUser(Context ctx, int userId) {
        FingerprintsUserState state;
        synchronized (this) {
            state = (FingerprintsUserState) this.mUsers.get(userId);
            if (state == null) {
                state = new FingerprintsUserState(ctx, userId);
                this.mUsers.put(userId, state);
            }
        }
        return state;
    }
}

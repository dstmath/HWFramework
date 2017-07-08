package com.android.server.audio;

import android.app.KeyguardManager;
import android.content.Context;
import android.media.AudioSystem;
import android.os.Handler;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.WindowManager;
import com.android.server.SystemService;
import com.android.server.policy.WindowOrientationListener;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;

class RotationHelper {
    private static final boolean DUAL_SMARTPA_SUPPORT = false;
    private static final boolean SPK_RCV_STEREO_SUPPORT = false;
    private static final String TAG = "AudioService.RotationHelper";
    private static Context sContext;
    private static int sDeviceRotation;
    private static KeyguardManager sKeyguardManager;
    private static AudioOrientationListener sOrientationListener;
    private static final Object sRotationLock = null;
    private static AudioWindowOrientationListener sWindowOrientationListener;

    static final class AudioOrientationListener extends OrientationEventListener {
        AudioOrientationListener(Context context) {
            super(context);
        }

        public void onOrientationChanged(int orientation) {
            RotationHelper.updateOrientation();
        }
    }

    static final class AudioWindowOrientationListener extends WindowOrientationListener {
        private static RotationCheckThread sRotationCheckThread;

        AudioWindowOrientationListener(Context context, Handler handler) {
            super(context, handler);
        }

        public void onProposedRotationChanged(int rotation) {
            RotationHelper.updateOrientation();
            if (sRotationCheckThread != null) {
                sRotationCheckThread.endCheck();
                sRotationCheckThread.interrupt();
                sRotationCheckThread = null;
            }
            sRotationCheckThread = new RotationCheckThread();
            sRotationCheckThread.beginCheck();
        }
    }

    static final class RotationCheckThread extends Thread {
        private final int[] WAIT_TIMES_MS;
        private final Object mCounterLock;
        private int mWaitCounter;

        RotationCheckThread() {
            super("RotationCheck");
            this.WAIT_TIMES_MS = new int[]{10, 20, 50, 100, 100, 200, 200, SystemService.PHASE_SYSTEM_SERVICES_READY};
            this.mCounterLock = new Object();
        }

        void beginCheck() {
            synchronized (this.mCounterLock) {
                this.mWaitCounter = 0;
            }
            try {
                start();
            } catch (IllegalStateException e) {
            }
        }

        void endCheck() {
            synchronized (this.mCounterLock) {
                this.mWaitCounter = this.WAIT_TIMES_MS.length;
            }
        }

        public void run() {
            while (this.mWaitCounter < this.WAIT_TIMES_MS.length) {
                synchronized (this.mCounterLock) {
                    int waitTimeMs = this.mWaitCounter < this.WAIT_TIMES_MS.length ? this.WAIT_TIMES_MS[this.mWaitCounter] : 0;
                    this.mWaitCounter++;
                }
                if (waitTimeMs > 0) {
                    try {
                        sleep((long) waitTimeMs);
                        RotationHelper.updateOrientation();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.audio.RotationHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.audio.RotationHelper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.audio.RotationHelper.<clinit>():void");
    }

    RotationHelper() {
    }

    static void init(Context context, Handler handler) {
        if (context == null) {
            throw new IllegalArgumentException("Invalid null context");
        }
        sContext = context;
        sWindowOrientationListener = new AudioWindowOrientationListener(context, handler);
        sWindowOrientationListener.enable();
        if (!sWindowOrientationListener.canDetectOrientation()) {
            Log.i(TAG, "Not using WindowOrientationListener, reverting to OrientationListener");
            sWindowOrientationListener.disable();
            sWindowOrientationListener = null;
            sOrientationListener = new AudioOrientationListener(context);
            sOrientationListener.enable();
        }
        sKeyguardManager = (KeyguardManager) context.getSystemService("keyguard");
    }

    static void enable() {
        if (sWindowOrientationListener != null) {
            sWindowOrientationListener.enable();
        } else {
            sOrientationListener.enable();
        }
        updateOrientation();
    }

    static void disable() {
        if (sWindowOrientationListener != null) {
            sWindowOrientationListener.disable();
        } else {
            sOrientationListener.disable();
        }
    }

    static void updateOrientation() {
        if (!SPK_RCV_STEREO_SUPPORT || sKeyguardManager == null || !sKeyguardManager.isKeyguardLocked()) {
            int newRotation = ((WindowManager) sContext.getSystemService("window")).getDefaultDisplay().getRotation();
            synchronized (sRotationLock) {
                if (newRotation != sDeviceRotation) {
                    sDeviceRotation = newRotation;
                    publishRotation(sDeviceRotation);
                }
            }
        }
    }

    private static void publishRotation(int rotation) {
        Log.v(TAG, "publishing device rotation =" + rotation + " (x90deg)");
        switch (rotation) {
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                AudioSystem.setParameters("rotation=0");
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                AudioSystem.setParameters("rotation=90");
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                AudioSystem.setParameters("rotation=180");
            case H.REPORT_LOSING_FOCUS /*3*/:
                AudioSystem.setParameters("rotation=270");
            default:
                Log.e(TAG, "Unknown device rotation");
        }
    }
}

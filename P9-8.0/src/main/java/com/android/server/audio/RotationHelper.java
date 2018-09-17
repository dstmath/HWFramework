package com.android.server.audio;

import android.app.KeyguardManager;
import android.content.Context;
import android.media.AudioSystem;
import android.os.Handler;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.WindowManager;
import com.android.server.display.DisplayTransformManager;
import com.android.server.policy.WindowOrientationListener;

class RotationHelper {
    private static final boolean DUAL_SMARTPA_SUPPORT = "true".equals(AudioSystem.getParameters("audio_capability#dual_smartpa_support"));
    private static final boolean SPK_RCV_STEREO_SUPPORT;
    private static final String TAG = "AudioService.RotationHelper";
    private static Context sContext;
    private static int sDeviceRotation = 0;
    private static KeyguardManager sKeyguardManager;
    private static AudioOrientationListener sOrientationListener;
    private static final Object sRotationLock = new Object();
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
        private final int[] WAIT_TIMES_MS = new int[]{10, 20, 50, 100, 100, DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE, DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE, 500};
        private final Object mCounterLock = new Object();
        private int mWaitCounter;

        RotationCheckThread() {
            super("RotationCheck");
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
                int waitTimeMs;
                synchronized (this.mCounterLock) {
                    waitTimeMs = this.mWaitCounter < this.WAIT_TIMES_MS.length ? this.WAIT_TIMES_MS[this.mWaitCounter] : 0;
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

    RotationHelper() {
    }

    static {
        boolean z = false;
        if (DUAL_SMARTPA_SUPPORT) {
            z = "true".equals(AudioSystem.getParameters("audio_capability#spk_rcv_stereo_support"));
        }
        SPK_RCV_STEREO_SUPPORT = z;
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
            case 0:
                AudioSystem.setParameters("rotation=0");
                return;
            case 1:
                AudioSystem.setParameters("rotation=90");
                return;
            case 2:
                AudioSystem.setParameters("rotation=180");
                return;
            case 3:
                AudioSystem.setParameters("rotation=270");
                return;
            default:
                Log.e(TAG, "Unknown device rotation");
                return;
        }
    }
}

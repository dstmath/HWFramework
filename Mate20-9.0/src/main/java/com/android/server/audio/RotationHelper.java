package com.android.server.audio;

import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.media.AudioSystem;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

class RotationHelper {
    private static final boolean DUAL_SMARTPA_SUPPORT = "true".equals(AudioSystem.getParameters("audio_capability#dual_smartpa_support"));
    private static final boolean SPK_RCV_STEREO_SUPPORT;
    private static final String TAG = "AudioService.RotationHelper";
    private static Context sContext;
    private static int sDeviceRotation = 0;
    private static AudioDisplayListener sDisplayListener;
    private static Handler sHandler;
    private static KeyguardManager sKeyguardManager;
    private static final Object sRotationLock = new Object();

    static final class AudioDisplayListener implements DisplayManager.DisplayListener {
        AudioDisplayListener() {
        }

        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayRemoved(int displayId) {
        }

        public void onDisplayChanged(int displayId) {
            RotationHelper.updateOrientation();
        }
    }

    RotationHelper() {
    }

    static {
        boolean z = false;
        if (DUAL_SMARTPA_SUPPORT && !"false".equals(AudioSystem.getParameters("audio_capability#spk_rcv_stereo_support"))) {
            z = true;
        }
        SPK_RCV_STEREO_SUPPORT = z;
    }

    static void init(Context context, Handler handler) {
        if (context != null) {
            sContext = context;
            sHandler = handler;
            sDisplayListener = new AudioDisplayListener();
            enable();
            sKeyguardManager = (KeyguardManager) context.getSystemService("keyguard");
            return;
        }
        throw new IllegalArgumentException("Invalid null context");
    }

    static void enable() {
        ((DisplayManager) sContext.getSystemService("display")).registerDisplayListener(sDisplayListener, sHandler);
        updateOrientation();
    }

    static void disable() {
        ((DisplayManager) sContext.getSystemService("display")).unregisterDisplayListener(sDisplayListener);
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

    public static void reset() {
        publishRotation(sDeviceRotation);
    }

    private static void publishRotation(int rotation) {
        Log.v(TAG, "publishing device rotation =" + rotation + " (x90deg)");
        switch (rotation) {
            case 0:
                AudioSystem.setParameters("orientation=portrait");
                AudioSystem.setParameters("rotation=0");
                return;
            case 1:
                AudioSystem.setParameters("orientation=landscape");
                AudioSystem.setParameters("rotation=90");
                return;
            case 2:
                AudioSystem.setParameters("orientation=portrait");
                AudioSystem.setParameters("rotation=180");
                return;
            case 3:
                AudioSystem.setParameters("orientation=landscape");
                AudioSystem.setParameters("rotation=270");
                return;
            default:
                Log.e(TAG, "Unknown device rotation");
                return;
        }
    }
}

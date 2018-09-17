package android.os;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.IVibratorService.Stub;
import android.util.Log;

public class SystemVibrator extends Vibrator {
    private static final String TAG = "Vibrator";
    private final IVibratorService mService = Stub.asInterface(ServiceManager.getService(Context.VIBRATOR_SERVICE));
    private final Binder mToken = new Binder();

    public SystemVibrator(Context context) {
        super(context);
    }

    public boolean hasVibrator() {
        if (this.mService == null) {
            Log.w(TAG, "Failed to vibrate; no vibrator service.");
            return false;
        }
        try {
            return this.mService.hasVibrator();
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean hasAmplitudeControl() {
        if (this.mService == null) {
            Log.w(TAG, "Failed to check amplitude control; no vibrator service.");
            return false;
        }
        try {
            return this.mService.hasAmplitudeControl();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to excute hasAmplitudeControl.");
            return false;
        }
    }

    public void vibrate(int uid, String opPkg, VibrationEffect effect, AudioAttributes attributes) {
        if (this.mService == null) {
            Log.w(TAG, "Failed to vibrate; no vibrator service.");
            return;
        }
        try {
            this.mService.vibrate(uid, opPkg, effect, usageForAttributes(attributes), this.mToken);
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to vibrate.", e);
        }
    }

    protected void hwVibrate(int uid, String opPkg, AudioAttributes attributes, int mode) {
        if (this.mService == null) {
            Log.w(TAG, "Failed to vibrate; no vibrator service.");
            return;
        }
        try {
            this.mService.hwVibrate(uid, opPkg, usageForAttributes(attributes), this.mToken, mode);
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to vibrate.", e);
        }
    }

    private static int usageForAttributes(AudioAttributes attributes) {
        return attributes != null ? attributes.getUsage() : 0;
    }

    public void cancel() {
        if (this.mService != null) {
            try {
                this.mService.cancelVibrate(this.mToken);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to cancel vibration.", e);
            }
        }
    }
}

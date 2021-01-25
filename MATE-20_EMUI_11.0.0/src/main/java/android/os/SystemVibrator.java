package android.os;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.media.AudioAttributes;
import android.os.IVibratorService;
import android.util.Log;

public class SystemVibrator extends Vibrator {
    private static final boolean IS_VIBRATOR_DISABLED = SystemProperties.getBoolean("hw.no_vibrator", false);
    private static final String TAG = "Vibrator";
    private final IVibratorService mService = IVibratorService.Stub.asInterface(ServiceManager.getService(Context.VIBRATOR_SERVICE));
    private final Binder mToken = new Binder();

    @UnsupportedAppUsage
    public SystemVibrator() {
    }

    @UnsupportedAppUsage
    public SystemVibrator(Context context) {
        super(context);
    }

    @Override // android.os.Vibrator
    public boolean hasVibrator() {
        if (IS_VIBRATOR_DISABLED) {
            Log.d(TAG, "Failed to vibrate; no vibrator.");
            return false;
        }
        IVibratorService iVibratorService = this.mService;
        if (iVibratorService == null) {
            Log.w(TAG, "Failed to vibrate; no vibrator service.");
            return false;
        }
        try {
            return iVibratorService.hasVibrator();
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.os.Vibrator
    public boolean hasAmplitudeControl() {
        IVibratorService iVibratorService = this.mService;
        if (iVibratorService == null) {
            Log.w(TAG, "Failed to check amplitude control; no vibrator service.");
            return false;
        }
        try {
            return iVibratorService.hasAmplitudeControl();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to excute hasAmplitudeControl.");
            return false;
        }
    }

    @Override // android.os.Vibrator
    public void vibrate(int uid, String opPkg, VibrationEffect effect, String reason, AudioAttributes attributes) {
        IVibratorService iVibratorService = this.mService;
        if (iVibratorService == null) {
            Log.w(TAG, "Failed to vibrate; no vibrator service.");
            return;
        }
        try {
            iVibratorService.vibrate(uid, opPkg, effect, usageForAttributes(attributes), reason, this.mToken);
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to vibrate.", e);
        }
    }

    private static int usageForAttributes(AudioAttributes attributes) {
        if (attributes != null) {
            return attributes.getUsage();
        }
        return 0;
    }

    @Override // android.os.Vibrator
    public void cancel() {
        IVibratorService iVibratorService = this.mService;
        if (iVibratorService != null) {
            try {
                iVibratorService.cancelVibrate(this.mToken);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to cancel vibration.", e);
            }
        }
    }
}

package android.os;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.IVibratorService.Stub;
import android.util.Log;

public class SystemVibrator extends Vibrator {
    private static final String TAG = "Vibrator";
    private final IVibratorService mService;
    private final Binder mToken;

    public SystemVibrator() {
        this.mToken = new Binder();
        this.mService = Stub.asInterface(ServiceManager.getService(Context.VIBRATOR_SERVICE));
    }

    public SystemVibrator(Context context) {
        super(context);
        this.mToken = new Binder();
        this.mService = Stub.asInterface(ServiceManager.getService(Context.VIBRATOR_SERVICE));
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

    public void vibrate(int uid, String opPkg, long milliseconds, AudioAttributes attributes) {
        if (this.mService == null) {
            Log.w(TAG, "Failed to vibrate; no vibrator service.");
            return;
        }
        try {
            this.mService.vibrate(uid, opPkg, milliseconds, usageForAttributes(attributes), this.mToken);
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to vibrate.", e);
        }
    }

    public void vibrate(int uid, String opPkg, long[] pattern, int repeat, AudioAttributes attributes) {
        if (this.mService == null) {
            Log.w(TAG, "Failed to vibrate; no vibrator service.");
        } else if (repeat < pattern.length) {
            try {
                this.mService.vibratePattern(uid, opPkg, pattern, repeat, usageForAttributes(attributes), this.mToken);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to vibrate.", e);
            }
        } else {
            throw new ArrayIndexOutOfBoundsException();
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

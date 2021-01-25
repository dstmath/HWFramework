package com.android.server.biometrics;

import android.content.Context;
import android.hardware.biometrics.BiometricAuthenticator;
import android.media.AudioAttributes;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Slog;
import com.android.internal.logging.MetricsLogger;
import com.android.server.biometrics.BiometricServiceBase;
import com.huawei.android.os.HwVibrator;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public abstract class ClientMonitor extends LoggableMonitor implements IBinder.DeathRecipient {
    private static final String AUTHENTICATION_ERROR_VIBRATE_TYPE = "haptic.common.fail_pattern2";
    private static final String AUTHENTICATION_SUCCESS_VIBRATE_TYPE = "haptic.common.notice1";
    protected static final boolean DEBUG = true;
    private static final long[] DEFAULT_SUCCESS_VIBRATION_PATTERN = {0, (long) SystemProperties.getInt("ro.config.enroll_vibrate_time", 30)};
    private static final int ENROLL_VIBRATE_TIME = SystemProperties.getInt("ro.config.enroll_vibrate_time", -1);
    protected static final int ERROR_ESRCH = 3;
    private static final AudioAttributes FINGERPRINT_SONFICATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    public static int mAcquiredInfo = -1;
    private boolean isSupportHwFingerVibrateError = false;
    private boolean isSupportHwVibratorSuccess = false;
    protected boolean mAlreadyCancelled;
    protected boolean mAlreadyDone;
    protected final Constants mConstants;
    private final Context mContext;
    private final int mCookie;
    private final BiometricServiceBase.DaemonWrapper mDaemon;
    private final VibrationEffect mErrorVibrationEffect;
    private int mGroupId;
    private final long mHalDeviceId;
    private final boolean mIsRestricted;
    private BiometricServiceBase.ServiceListener mListener;
    protected final MetricsLogger mMetricsLogger;
    private final String mOwner;
    private final VibrationEffect mSuccessVibrationEffect;
    private int mTargetDevice;
    private final int mTargetUserId;
    private IBinder mToken;

    public abstract void notifyUserActivity();

    public abstract boolean onAuthenticated(BiometricAuthenticator.Identifier identifier, boolean z, ArrayList<Byte> arrayList);

    public abstract boolean onEnrollResult(BiometricAuthenticator.Identifier identifier, int i);

    public abstract boolean onEnumerationResult(BiometricAuthenticator.Identifier identifier, int i);

    public abstract boolean onRemoved(BiometricAuthenticator.Identifier identifier, int i);

    public abstract int start();

    public abstract int stop(boolean z);

    public ClientMonitor(Context context, Constants constants, BiometricServiceBase.DaemonWrapper daemon, long halDeviceId, IBinder token, BiometricServiceBase.ServiceListener listener, int userId, int groupId, boolean restricted, String owner, int cookie) {
        this.mContext = context;
        this.mConstants = constants;
        this.mDaemon = daemon;
        this.mHalDeviceId = halDeviceId;
        this.mToken = token;
        this.mListener = listener;
        this.mTargetUserId = userId;
        this.mGroupId = groupId;
        this.mIsRestricted = restricted;
        this.mOwner = owner;
        this.mCookie = cookie;
        this.mSuccessVibrationEffect = getSuccessVibrationEffect(context);
        this.mErrorVibrationEffect = VibrationEffect.get(1);
        this.mMetricsLogger = new MetricsLogger();
        if (token != null) {
            try {
                token.linkToDeath(this, 0);
            } catch (RemoteException e) {
                Slog.w(getLogTag(), "caught remote exception in linkToDeath: ", e);
            }
        }
        this.mTargetDevice = 0;
        this.isSupportHwVibratorSuccess = HwVibrator.isSupportHwVibrator(AUTHENTICATION_SUCCESS_VIBRATE_TYPE);
        this.isSupportHwFingerVibrateError = HwVibrator.isSupportHwVibrator(AUTHENTICATION_ERROR_VIBRATE_TYPE);
    }

    /* access modifiers changed from: protected */
    public String getLogTag() {
        return this.mConstants.logTag();
    }

    public int getCookie() {
        return this.mCookie;
    }

    public int getTargetDevice() {
        return this.mTargetDevice;
    }

    public void setTargetDevice(int deviceIndex) {
        this.mTargetDevice = deviceIndex;
    }

    public int[] getAcquireIgnorelist() {
        return new int[0];
    }

    public int[] getAcquireVendorIgnorelist() {
        return new int[0];
    }

    private boolean blacklistContains(int acquiredInfo, int vendorCode) {
        if (acquiredInfo == this.mConstants.acquireVendorCode()) {
            for (int i = 0; i < getAcquireVendorIgnorelist().length; i++) {
                if (getAcquireVendorIgnorelist()[i] == vendorCode) {
                    String logTag = getLogTag();
                    Slog.v(logTag, "Ignoring vendor message: " + vendorCode);
                    return true;
                }
            }
            return false;
        }
        for (int i2 = 0; i2 < getAcquireIgnorelist().length; i2++) {
            if (getAcquireIgnorelist()[i2] == acquiredInfo) {
                String logTag2 = getLogTag();
                Slog.v(logTag2, "Ignoring message: " + acquiredInfo);
                return true;
            }
        }
        return false;
    }

    public boolean isAlreadyDone() {
        return this.mAlreadyDone;
    }

    public boolean onAcquired(int acquiredInfo, int vendorCode) {
        super.logOnAcquired(this.mContext, acquiredInfo, vendorCode, getTargetUserId());
        String logTag = getLogTag();
        Slog.v(logTag, "Acquired: " + acquiredInfo + " " + vendorCode);
        try {
            if (this.mListener != null && !blacklistContains(acquiredInfo, vendorCode)) {
                this.mListener.onAcquired(getHalDeviceId(), acquiredInfo, vendorCode);
            }
            return false;
        } catch (RemoteException e) {
            Slog.w(getLogTag(), "Failed to invoke sendAcquired", e);
            return true;
        } finally {
            mAcquiredInfo = acquiredInfo;
        }
    }

    public boolean onError(long deviceId, int error, int vendorCode) {
        super.logOnError(this.mContext, error, vendorCode, getTargetUserId());
        try {
            if (this.mListener == null) {
                return true;
            }
            this.mListener.onError(deviceId, error, vendorCode, getCookie());
            return true;
        } catch (RemoteException e) {
            Slog.w(getLogTag(), "Failed to invoke sendError", e);
            return true;
        }
    }

    public void destroy() {
        IBinder iBinder = this.mToken;
        if (iBinder != null) {
            try {
                iBinder.unlinkToDeath(this, 0);
            } catch (NoSuchElementException e) {
                String logTag = getLogTag();
                Slog.e(logTag, "destroy(): " + this + ":", new Exception("here"));
            }
            this.mToken = null;
        }
        this.mListener = null;
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        Slog.e(getLogTag(), "Binder died, cancelling client");
        stop(false);
        this.mToken = null;
        this.mListener = null;
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            if (this.mToken != null) {
                String logTag = getLogTag();
                Slog.w(logTag, "removing leaked reference: " + this.mToken);
                onError(getHalDeviceId(), 1, 0);
            }
        } finally {
            super.finalize();
        }
    }

    public final Context getContext() {
        return this.mContext;
    }

    public final long getHalDeviceId() {
        return this.mHalDeviceId;
    }

    public final String getOwnerString() {
        return this.mOwner;
    }

    public final BiometricServiceBase.ServiceListener getListener() {
        return this.mListener;
    }

    public final BiometricServiceBase.DaemonWrapper getDaemonWrapper() {
        return this.mDaemon;
    }

    public final boolean getIsRestricted() {
        return this.mIsRestricted;
    }

    public final int getTargetUserId() {
        return this.mTargetUserId;
    }

    public final int getGroupId() {
        return this.mGroupId;
    }

    public void setGroupId(int groupId) {
        this.mGroupId = groupId;
    }

    public final IBinder getToken() {
        return this.mToken;
    }

    public final void vibrateSuccess() {
        authenticationResultVibrate(this.isSupportHwVibratorSuccess, AUTHENTICATION_SUCCESS_VIBRATE_TYPE, true);
    }

    public final void vibrateError() {
        authenticationResultVibrate(this.isSupportHwFingerVibrateError, AUTHENTICATION_ERROR_VIBRATE_TYPE, false);
    }

    private void authenticationResultVibrate(boolean isSupportHwVibrator, String vibrateType, boolean isSuccess) {
        String logTag = getLogTag();
        Slog.i(logTag, "isSupportHwVibrator:" + isSupportHwVibrator + ",vibrateType:" + vibrateType);
        Context context = this.mContext;
        if (context == null) {
            Slog.w(getLogTag(), "mContext = null, return");
        } else if (isSupportHwVibrator) {
            HwVibrator.setHwVibrator(Process.myUid(), this.mContext.getPackageName(), vibrateType);
        } else {
            Vibrator vibrator = (Vibrator) context.getSystemService(Vibrator.class);
            if (vibrator == null) {
                Slog.w(getLogTag(), "Vibrator is null, return");
            } else if (isSuccess) {
                vibrator.vibrate(this.mSuccessVibrationEffect, FINGERPRINT_SONFICATION_ATTRIBUTES);
            } else {
                vibrator.vibrate(this.mErrorVibrationEffect, FINGERPRINT_SONFICATION_ATTRIBUTES);
            }
        }
    }

    private VibrationEffect getSuccessVibrationEffect(Context ctx) {
        long[] vibePattern;
        if (ENROLL_VIBRATE_TIME > 0) {
            String logTag = getLogTag();
            Slog.d(logTag, "use enroll time with prop :" + ENROLL_VIBRATE_TIME);
            vibePattern = DEFAULT_SUCCESS_VIBRATION_PATTERN;
        } else {
            int[] arr = ctx.getResources().getIntArray(17236034);
            if (arr == null || arr.length == 0) {
                vibePattern = DEFAULT_SUCCESS_VIBRATION_PATTERN;
            } else {
                vibePattern = new long[arr.length];
                for (int i = 0; i < arr.length; i++) {
                    vibePattern[i] = (long) arr[i];
                }
            }
        }
        if (vibePattern.length == 1) {
            return VibrationEffect.createOneShot(vibePattern[0], -1);
        }
        return VibrationEffect.createWaveform(vibePattern, -1);
    }
}

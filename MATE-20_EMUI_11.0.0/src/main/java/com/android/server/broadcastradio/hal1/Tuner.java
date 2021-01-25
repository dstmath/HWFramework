package com.android.server.broadcastradio.hal1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.radio.ITuner;
import android.hardware.radio.ITunerCallback;
import android.hardware.radio.ProgramList;
import android.hardware.radio.ProgramSelector;
import android.hardware.radio.RadioManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Slog;
import java.util.List;
import java.util.Map;

/* access modifiers changed from: package-private */
public class Tuner extends ITuner.Stub {
    private static final String TAG = "BroadcastRadioService.Tuner";
    private final ITunerCallback mClientCallback;
    private final IBinder.DeathRecipient mDeathRecipient;
    private boolean mIsClosed = false;
    private boolean mIsMuted = false;
    private final Object mLock = new Object();
    private final long mNativeContext;
    private int mRegion;
    private final TunerCallback mTunerCallback;
    private final boolean mWithAudio;

    private native void nativeCancel(long j);

    private native void nativeCancelAnnouncement(long j);

    private native void nativeClose(long j);

    private native void nativeFinalize(long j);

    private native RadioManager.BandConfig nativeGetConfiguration(long j, int i);

    private native byte[] nativeGetImage(long j, int i);

    private native List<RadioManager.ProgramInfo> nativeGetProgramList(long j, Map<String, String> map);

    private native long nativeInit(int i, boolean z, int i2);

    private native boolean nativeIsAnalogForced(long j);

    private native void nativeScan(long j, boolean z, boolean z2);

    private native void nativeSetAnalogForced(long j, boolean z);

    private native void nativeSetConfiguration(long j, RadioManager.BandConfig bandConfig);

    private native boolean nativeStartBackgroundScan(long j);

    private native void nativeStep(long j, boolean z, boolean z2);

    private native void nativeTune(long j, ProgramSelector programSelector);

    Tuner(ITunerCallback clientCallback, int halRev, int region, boolean withAudio, int band) {
        this.mClientCallback = clientCallback;
        this.mTunerCallback = new TunerCallback(this, clientCallback, halRev);
        this.mRegion = region;
        this.mWithAudio = withAudio;
        this.mNativeContext = nativeInit(halRev, withAudio, band);
        this.mDeathRecipient = new IBinder.DeathRecipient() {
            /* class com.android.server.broadcastradio.hal1.$$Lambda$XcW_oxw3YwSco8d8bZQoqwUTnM */

            @Override // android.os.IBinder.DeathRecipient
            public final void binderDied() {
                Tuner.this.close();
            }
        };
        try {
            this.mClientCallback.asBinder().linkToDeath(this.mDeathRecipient, 0);
        } catch (RemoteException e) {
            close();
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        nativeFinalize(this.mNativeContext);
        Tuner.super.finalize();
    }

    public void close() {
        synchronized (this.mLock) {
            if (!this.mIsClosed) {
                this.mIsClosed = true;
                this.mTunerCallback.detach();
                this.mClientCallback.asBinder().unlinkToDeath(this.mDeathRecipient, 0);
                nativeClose(this.mNativeContext);
            }
        }
    }

    public boolean isClosed() {
        return this.mIsClosed;
    }

    private void checkNotClosedLocked() {
        if (this.mIsClosed) {
            throw new IllegalStateException("Tuner is closed, no further operations are allowed");
        }
    }

    private boolean checkConfiguredLocked() {
        if (this.mTunerCallback.isInitialConfigurationDone()) {
            return true;
        }
        Slog.w(TAG, "Initial configuration is still pending, skipping the operation");
        return false;
    }

    public void setConfiguration(RadioManager.BandConfig config) {
        if (config != null) {
            synchronized (this.mLock) {
                checkNotClosedLocked();
                nativeSetConfiguration(this.mNativeContext, config);
                this.mRegion = config.getRegion();
            }
            return;
        }
        throw new IllegalArgumentException("The argument must not be a null pointer");
    }

    public RadioManager.BandConfig getConfiguration() {
        RadioManager.BandConfig nativeGetConfiguration;
        synchronized (this.mLock) {
            checkNotClosedLocked();
            nativeGetConfiguration = nativeGetConfiguration(this.mNativeContext, this.mRegion);
        }
        return nativeGetConfiguration;
    }

    public void setMuted(boolean mute) {
        if (this.mWithAudio) {
            synchronized (this.mLock) {
                checkNotClosedLocked();
                if (this.mIsMuted != mute) {
                    this.mIsMuted = mute;
                    Slog.w(TAG, "Mute via RadioService is not implemented - please handle it via app");
                    return;
                }
                return;
            }
        }
        throw new IllegalStateException("Can't operate on mute - no audio requested");
    }

    public boolean isMuted() {
        boolean z;
        if (!this.mWithAudio) {
            Slog.w(TAG, "Tuner did not request audio, pretending it was muted");
            return true;
        }
        synchronized (this.mLock) {
            checkNotClosedLocked();
            z = this.mIsMuted;
        }
        return z;
    }

    public void step(boolean directionDown, boolean skipSubChannel) {
        synchronized (this.mLock) {
            checkNotClosedLocked();
            if (checkConfiguredLocked()) {
                nativeStep(this.mNativeContext, directionDown, skipSubChannel);
            }
        }
    }

    public void scan(boolean directionDown, boolean skipSubChannel) {
        synchronized (this.mLock) {
            checkNotClosedLocked();
            if (checkConfiguredLocked()) {
                nativeScan(this.mNativeContext, directionDown, skipSubChannel);
            }
        }
    }

    public void tune(ProgramSelector selector) {
        if (selector != null) {
            Slog.i(TAG, "Tuning to " + selector);
            synchronized (this.mLock) {
                checkNotClosedLocked();
                if (checkConfiguredLocked()) {
                    nativeTune(this.mNativeContext, selector);
                    return;
                }
                return;
            }
        }
        throw new IllegalArgumentException("The argument must not be a null pointer");
    }

    public void cancel() {
        synchronized (this.mLock) {
            checkNotClosedLocked();
            nativeCancel(this.mNativeContext);
        }
    }

    public void cancelAnnouncement() {
        synchronized (this.mLock) {
            checkNotClosedLocked();
            nativeCancelAnnouncement(this.mNativeContext);
        }
    }

    public Bitmap getImage(int id) {
        byte[] rawImage;
        if (id != 0) {
            synchronized (this.mLock) {
                rawImage = nativeGetImage(this.mNativeContext, id);
            }
            if (rawImage == null || rawImage.length == 0) {
                return null;
            }
            return BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length);
        }
        throw new IllegalArgumentException("Image ID is missing");
    }

    public boolean startBackgroundScan() {
        boolean nativeStartBackgroundScan;
        synchronized (this.mLock) {
            checkNotClosedLocked();
            nativeStartBackgroundScan = nativeStartBackgroundScan(this.mNativeContext);
        }
        return nativeStartBackgroundScan;
    }

    /* access modifiers changed from: package-private */
    public List<RadioManager.ProgramInfo> getProgramList(Map vendorFilter) {
        List<RadioManager.ProgramInfo> list;
        synchronized (this.mLock) {
            checkNotClosedLocked();
            list = nativeGetProgramList(this.mNativeContext, vendorFilter);
            if (list == null) {
                throw new IllegalStateException("Program list is not ready");
            }
        }
        return list;
    }

    public void startProgramListUpdates(ProgramList.Filter filter) {
        this.mTunerCallback.startProgramListUpdates(filter);
    }

    public void stopProgramListUpdates() {
        this.mTunerCallback.stopProgramListUpdates();
    }

    public boolean isConfigFlagSupported(int flag) {
        return flag == 2;
    }

    public boolean isConfigFlagSet(int flag) {
        boolean nativeIsAnalogForced;
        if (flag == 2) {
            synchronized (this.mLock) {
                checkNotClosedLocked();
                nativeIsAnalogForced = nativeIsAnalogForced(this.mNativeContext);
            }
            return nativeIsAnalogForced;
        }
        throw new UnsupportedOperationException("Not supported by HAL 1.x");
    }

    public void setConfigFlag(int flag, boolean value) {
        if (flag == 2) {
            synchronized (this.mLock) {
                checkNotClosedLocked();
                nativeSetAnalogForced(this.mNativeContext, value);
            }
            return;
        }
        throw new UnsupportedOperationException("Not supported by HAL 1.x");
    }

    public Map setParameters(Map parameters) {
        throw new UnsupportedOperationException("Not supported by HAL 1.x");
    }

    public Map getParameters(List<String> list) {
        throw new UnsupportedOperationException("Not supported by HAL 1.x");
    }
}

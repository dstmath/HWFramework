package com.android.server.broadcastradio.hal2;

import android.graphics.Bitmap;
import android.hardware.broadcastradio.V2_0.ConfigFlag;
import android.hardware.broadcastradio.V2_0.ITunerSession;
import android.hardware.radio.ITuner;
import android.hardware.radio.ITunerCallback;
import android.hardware.radio.ProgramList;
import android.hardware.radio.ProgramSelector;
import android.hardware.radio.RadioManager;
import android.os.RemoteException;
import android.util.MutableBoolean;
import android.util.MutableInt;
import android.util.Slog;
import com.android.server.broadcastradio.hal2.RadioModule;
import com.android.server.broadcastradio.hal2.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/* access modifiers changed from: package-private */
public class TunerSession extends ITuner.Stub {
    private static final String TAG = "BcRadio2Srv.session";
    private static final String kAudioDeviceName = "Radio tuner source";
    final ITunerCallback mCallback;
    private RadioManager.BandConfig mDummyConfig = null;
    private final ITunerSession mHwSession;
    private boolean mIsClosed = false;
    private boolean mIsMuted = false;
    private final Object mLock = new Object();
    private final RadioModule mModule;

    TunerSession(RadioModule module, ITunerSession hwSession, ITunerCallback callback) {
        this.mModule = (RadioModule) Objects.requireNonNull(module);
        this.mHwSession = (ITunerSession) Objects.requireNonNull(hwSession);
        this.mCallback = (ITunerCallback) Objects.requireNonNull(callback);
    }

    public void close() {
        close(null);
    }

    public void close(Integer error) {
        synchronized (this.mLock) {
            if (!this.mIsClosed) {
                if (error != null) {
                    try {
                        this.mCallback.onError(error.intValue());
                    } catch (RemoteException ex) {
                        Slog.w(TAG, "mCallback.onError() failed: ", ex);
                    }
                }
                this.mIsClosed = true;
                this.mModule.onTunerSessionClosed(this);
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

    public void setConfiguration(RadioManager.BandConfig config) {
        synchronized (this.mLock) {
            checkNotClosedLocked();
            this.mDummyConfig = (RadioManager.BandConfig) Objects.requireNonNull(config);
            Slog.i(TAG, "Ignoring setConfiguration - not applicable for broadcastradio HAL 2.x");
            this.mModule.fanoutAidlCallback(new RadioModule.AidlCallbackRunnable(config) {
                /* class com.android.server.broadcastradio.hal2.$$Lambda$TunerSession$Q1jB_2_HaoKIbsSvZQ_1kfSsk */
                private final /* synthetic */ RadioManager.BandConfig f$0;

                {
                    this.f$0 = r1;
                }

                @Override // com.android.server.broadcastradio.hal2.RadioModule.AidlCallbackRunnable
                public final void run(ITunerCallback iTunerCallback) {
                    iTunerCallback.onConfigurationChanged(this.f$0);
                }
            });
        }
    }

    public RadioManager.BandConfig getConfiguration() {
        RadioManager.BandConfig bandConfig;
        synchronized (this.mLock) {
            checkNotClosedLocked();
            bandConfig = this.mDummyConfig;
        }
        return bandConfig;
    }

    public void setMuted(boolean mute) {
        synchronized (this.mLock) {
            checkNotClosedLocked();
            if (this.mIsMuted != mute) {
                this.mIsMuted = mute;
                Slog.w(TAG, "Mute via RadioService is not implemented - please handle it via app");
            }
        }
    }

    public boolean isMuted() {
        boolean z;
        synchronized (this.mLock) {
            checkNotClosedLocked();
            z = this.mIsMuted;
        }
        return z;
    }

    public void step(boolean directionDown, boolean skipSubChannel) throws RemoteException {
        synchronized (this.mLock) {
            checkNotClosedLocked();
            Convert.throwOnError("step", this.mHwSession.step(!directionDown));
        }
    }

    public void scan(boolean directionDown, boolean skipSubChannel) throws RemoteException {
        synchronized (this.mLock) {
            checkNotClosedLocked();
            Convert.throwOnError("step", this.mHwSession.scan(!directionDown, skipSubChannel));
        }
    }

    public void tune(ProgramSelector selector) throws RemoteException {
        synchronized (this.mLock) {
            checkNotClosedLocked();
            Convert.throwOnError("tune", this.mHwSession.tune(Convert.programSelectorToHal(selector)));
        }
    }

    public void cancel() {
        synchronized (this.mLock) {
            checkNotClosedLocked();
            ITunerSession iTunerSession = this.mHwSession;
            Objects.requireNonNull(iTunerSession);
            Utils.maybeRethrow(new Utils.VoidFuncThrowingRemoteException() {
                /* class com.android.server.broadcastradio.hal2.$$Lambda$YOfksBuizvGCCXXC3xdyOet2Yr8 */

                @Override // com.android.server.broadcastradio.hal2.Utils.VoidFuncThrowingRemoteException
                public final void exec() {
                    ITunerSession.this.cancel();
                }
            });
        }
    }

    public void cancelAnnouncement() {
        Slog.i(TAG, "Announcements control doesn't involve cancelling at the HAL level in 2.x");
    }

    public Bitmap getImage(int id) {
        return this.mModule.getImage(id);
    }

    public boolean startBackgroundScan() {
        Slog.i(TAG, "Explicit background scan trigger is not supported with HAL 2.x");
        this.mModule.fanoutAidlCallback($$Lambda$TunerSession$RN6YGky4fEzp_y9hG2yxYfo0XPs.INSTANCE);
        return true;
    }

    public void startProgramListUpdates(ProgramList.Filter filter) throws RemoteException {
        synchronized (this.mLock) {
            checkNotClosedLocked();
            Convert.throwOnError("startProgramListUpdates", this.mHwSession.startProgramListUpdates(Convert.programFilterToHal(filter)));
        }
    }

    public void stopProgramListUpdates() throws RemoteException {
        synchronized (this.mLock) {
            checkNotClosedLocked();
            this.mHwSession.stopProgramListUpdates();
        }
    }

    public boolean isConfigFlagSupported(int flag) {
        try {
            isConfigFlagSet(flag);
            return true;
        } catch (IllegalStateException e) {
            return true;
        } catch (UnsupportedOperationException e2) {
            return false;
        }
    }

    public boolean isConfigFlagSet(int flag) {
        boolean z;
        Slog.v(TAG, "isConfigFlagSet " + ConfigFlag.toString(flag));
        synchronized (this.mLock) {
            checkNotClosedLocked();
            MutableInt halResult = new MutableInt(1);
            MutableBoolean flagState = new MutableBoolean(false);
            try {
                this.mHwSession.isConfigFlagSet(flag, new ITunerSession.isConfigFlagSetCallback(halResult, flagState) {
                    /* class com.android.server.broadcastradio.hal2.$$Lambda$TunerSession$ypybq6SvfCU67BzHDgrQ7oDdspw */
                    private final /* synthetic */ MutableInt f$0;
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                    }

                    @Override // android.hardware.broadcastradio.V2_0.ITunerSession.isConfigFlagSetCallback
                    public final void onValues(int i, boolean z) {
                        TunerSession.lambda$isConfigFlagSet$2(this.f$0, this.f$1, i, z);
                    }
                });
                Convert.throwOnError("isConfigFlagSet", halResult.value);
                z = flagState.value;
            } catch (RemoteException ex) {
                throw new RuntimeException("Failed to check flag " + ConfigFlag.toString(flag), ex);
            }
        }
        return z;
    }

    static /* synthetic */ void lambda$isConfigFlagSet$2(MutableInt halResult, MutableBoolean flagState, int result, boolean value) {
        halResult.value = result;
        flagState.value = value;
    }

    public void setConfigFlag(int flag, boolean value) throws RemoteException {
        Slog.v(TAG, "setConfigFlag " + ConfigFlag.toString(flag) + " = " + value);
        synchronized (this.mLock) {
            checkNotClosedLocked();
            Convert.throwOnError("setConfigFlag", this.mHwSession.setConfigFlag(flag, value));
        }
    }

    public Map setParameters(Map parameters) {
        Map<String, String> vendorInfoFromHal;
        synchronized (this.mLock) {
            checkNotClosedLocked();
            vendorInfoFromHal = Convert.vendorInfoFromHal((List) Utils.maybeRethrow(new Utils.FuncThrowingRemoteException(parameters) {
                /* class com.android.server.broadcastradio.hal2.$$Lambda$TunerSession$hsnpNw6TT5c0D5uUev9VuiIUUg */
                private final /* synthetic */ Map f$1;

                {
                    this.f$1 = r2;
                }

                @Override // com.android.server.broadcastradio.hal2.Utils.FuncThrowingRemoteException
                public final Object exec() {
                    return TunerSession.this.lambda$setParameters$3$TunerSession(this.f$1);
                }
            }));
        }
        return vendorInfoFromHal;
    }

    public /* synthetic */ ArrayList lambda$setParameters$3$TunerSession(Map parameters) throws RemoteException {
        return this.mHwSession.setParameters(Convert.vendorInfoToHal(parameters));
    }

    public Map getParameters(List<String> keys) {
        Map<String, String> vendorInfoFromHal;
        synchronized (this.mLock) {
            checkNotClosedLocked();
            vendorInfoFromHal = Convert.vendorInfoFromHal((List) Utils.maybeRethrow(new Utils.FuncThrowingRemoteException(keys) {
                /* class com.android.server.broadcastradio.hal2.$$Lambda$TunerSession$UmZx38YMX_OHk94g5WH0WyZPNu0 */
                private final /* synthetic */ List f$1;

                {
                    this.f$1 = r2;
                }

                @Override // com.android.server.broadcastradio.hal2.Utils.FuncThrowingRemoteException
                public final Object exec() {
                    return TunerSession.this.lambda$getParameters$4$TunerSession(this.f$1);
                }
            }));
        }
        return vendorInfoFromHal;
    }

    public /* synthetic */ ArrayList lambda$getParameters$4$TunerSession(List keys) throws RemoteException {
        return this.mHwSession.getParameters(Convert.listToArrayList(keys));
    }
}

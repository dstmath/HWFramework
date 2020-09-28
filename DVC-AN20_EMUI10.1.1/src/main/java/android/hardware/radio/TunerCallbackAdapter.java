package android.hardware.radio;

import android.hardware.radio.ITunerCallback;
import android.hardware.radio.ProgramList;
import android.hardware.radio.RadioManager;
import android.hardware.radio.RadioTuner;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/* access modifiers changed from: package-private */
public class TunerCallbackAdapter extends ITunerCallback.Stub {
    private static final String TAG = "BroadcastRadio.TunerCallbackAdapter";
    private final RadioTuner.Callback mCallback;
    RadioManager.ProgramInfo mCurrentProgramInfo;
    private boolean mDelayedCompleteCallback = false;
    private final Handler mHandler;
    boolean mIsAntennaConnected = true;
    List<RadioManager.ProgramInfo> mLastCompleteList;
    private final Object mLock = new Object();
    ProgramList mProgramList;

    TunerCallbackAdapter(RadioTuner.Callback callback, Handler handler) {
        this.mCallback = callback;
        if (handler == null) {
            this.mHandler = new Handler(Looper.getMainLooper());
        } else {
            this.mHandler = handler;
        }
    }

    /* access modifiers changed from: package-private */
    public void close() {
        synchronized (this.mLock) {
            if (this.mProgramList != null) {
                this.mProgramList.close();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setProgramListObserver(ProgramList programList, ProgramList.OnCloseListener closeListener) {
        Objects.requireNonNull(closeListener);
        synchronized (this.mLock) {
            if (this.mProgramList != null) {
                Log.w(TAG, "Previous program list observer wasn't properly closed, closing it...");
                this.mProgramList.close();
            }
            this.mProgramList = programList;
            if (programList != null) {
                programList.setOnCloseListener(new ProgramList.OnCloseListener(programList, closeListener) {
                    /* class android.hardware.radio.$$Lambda$TunerCallbackAdapter$Hl800ppQ17uTjZuGamwBQMrO6Y */
                    private final /* synthetic */ ProgramList f$1;
                    private final /* synthetic */ ProgramList.OnCloseListener f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // android.hardware.radio.ProgramList.OnCloseListener
                    public final void onClose() {
                        TunerCallbackAdapter.this.lambda$setProgramListObserver$0$TunerCallbackAdapter(this.f$1, this.f$2);
                    }
                });
                programList.addOnCompleteListener(new ProgramList.OnCompleteListener(programList) {
                    /* class android.hardware.radio.$$Lambda$TunerCallbackAdapter$VmJUy8dIlOVjsZ1ckkgn490jFI */
                    private final /* synthetic */ ProgramList f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // android.hardware.radio.ProgramList.OnCompleteListener
                    public final void onComplete() {
                        TunerCallbackAdapter.this.lambda$setProgramListObserver$1$TunerCallbackAdapter(this.f$1);
                    }
                });
            }
        }
    }

    public /* synthetic */ void lambda$setProgramListObserver$0$TunerCallbackAdapter(ProgramList programList, ProgramList.OnCloseListener closeListener) {
        synchronized (this.mLock) {
            if (this.mProgramList == programList) {
                this.mProgramList = null;
                this.mLastCompleteList = null;
                closeListener.onClose();
            }
        }
    }

    public /* synthetic */ void lambda$setProgramListObserver$1$TunerCallbackAdapter(ProgramList programList) {
        synchronized (this.mLock) {
            if (this.mProgramList == programList) {
                this.mLastCompleteList = programList.toList();
                if (this.mDelayedCompleteCallback) {
                    Log.d(TAG, "Sending delayed onBackgroundScanComplete callback");
                    sendBackgroundScanCompleteLocked();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public List<RadioManager.ProgramInfo> getLastCompleteList() {
        List<RadioManager.ProgramInfo> list;
        synchronized (this.mLock) {
            list = this.mLastCompleteList;
        }
        return list;
    }

    /* access modifiers changed from: package-private */
    public void clearLastCompleteList() {
        synchronized (this.mLock) {
            this.mLastCompleteList = null;
        }
    }

    /* access modifiers changed from: package-private */
    public RadioManager.ProgramInfo getCurrentProgramInformation() {
        RadioManager.ProgramInfo programInfo;
        synchronized (this.mLock) {
            programInfo = this.mCurrentProgramInfo;
        }
        return programInfo;
    }

    /* access modifiers changed from: package-private */
    public boolean isAntennaConnected() {
        return this.mIsAntennaConnected;
    }

    public /* synthetic */ void lambda$onError$2$TunerCallbackAdapter(int status) {
        this.mCallback.onError(status);
    }

    @Override // android.hardware.radio.ITunerCallback
    public void onError(int status) {
        this.mHandler.post(new Runnable(status) {
            /* class android.hardware.radio.$$Lambda$TunerCallbackAdapter$jl29exheqPoYrltfLs9fLsjsI1A */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TunerCallbackAdapter.this.lambda$onError$2$TunerCallbackAdapter(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onTuneFailed$3$TunerCallbackAdapter(int status, ProgramSelector selector) {
        this.mCallback.onTuneFailed(status, selector);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001f, code lost:
        if (r4 != -1) goto L_0x003f;
     */
    @Override // android.hardware.radio.ITunerCallback
    public void onTuneFailed(int status, ProgramSelector selector) {
        int errorCode;
        this.mHandler.post(new Runnable(status, selector) {
            /* class android.hardware.radio.$$Lambda$TunerCallbackAdapter$Hj_P___HTEx_8p7qvYVPXmhwu7w */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ ProgramSelector f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                TunerCallbackAdapter.this.lambda$onTuneFailed$3$TunerCallbackAdapter(this.f$1, this.f$2);
            }
        });
        if (!(status == Integer.MIN_VALUE || status == -38)) {
            if (status != -32) {
                if (!(status == -22 || status == -19)) {
                }
            }
            errorCode = 1;
            this.mHandler.post(new Runnable(errorCode) {
                /* class android.hardware.radio.$$Lambda$TunerCallbackAdapter$HcS5_voI1xju970_jCP6Iz0LgPE */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    TunerCallbackAdapter.this.lambda$onTuneFailed$4$TunerCallbackAdapter(this.f$1);
                }
            });
        }
        Log.i(TAG, "Got an error with no mapping to the legacy API (" + status + "), doing a best-effort conversion to ERROR_SCAN_TIMEOUT");
        errorCode = 3;
        this.mHandler.post(new Runnable(errorCode) {
            /* class android.hardware.radio.$$Lambda$TunerCallbackAdapter$HcS5_voI1xju970_jCP6Iz0LgPE */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TunerCallbackAdapter.this.lambda$onTuneFailed$4$TunerCallbackAdapter(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onTuneFailed$4$TunerCallbackAdapter(int errorCode) {
        this.mCallback.onError(errorCode);
    }

    public /* synthetic */ void lambda$onConfigurationChanged$5$TunerCallbackAdapter(RadioManager.BandConfig config) {
        this.mCallback.onConfigurationChanged(config);
    }

    @Override // android.hardware.radio.ITunerCallback
    public void onConfigurationChanged(RadioManager.BandConfig config) {
        this.mHandler.post(new Runnable(config) {
            /* class android.hardware.radio.$$Lambda$TunerCallbackAdapter$B4BuskgdSatfXt5wzgLniEltQk */
            private final /* synthetic */ RadioManager.BandConfig f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TunerCallbackAdapter.this.lambda$onConfigurationChanged$5$TunerCallbackAdapter(this.f$1);
            }
        });
    }

    @Override // android.hardware.radio.ITunerCallback
    public void onCurrentProgramInfoChanged(RadioManager.ProgramInfo info) {
        if (info == null) {
            Log.e(TAG, "ProgramInfo must not be null");
            return;
        }
        synchronized (this.mLock) {
            this.mCurrentProgramInfo = info;
        }
        this.mHandler.post(new Runnable(info) {
            /* class android.hardware.radio.$$Lambda$TunerCallbackAdapter$RSNrzX5O3nayC2_jg0kAR6KkKY */
            private final /* synthetic */ RadioManager.ProgramInfo f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TunerCallbackAdapter.this.lambda$onCurrentProgramInfoChanged$6$TunerCallbackAdapter(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onCurrentProgramInfoChanged$6$TunerCallbackAdapter(RadioManager.ProgramInfo info) {
        this.mCallback.onProgramInfoChanged(info);
        RadioMetadata metadata = info.getMetadata();
        if (metadata != null) {
            this.mCallback.onMetadataChanged(metadata);
        }
    }

    public /* synthetic */ void lambda$onTrafficAnnouncement$7$TunerCallbackAdapter(boolean active) {
        this.mCallback.onTrafficAnnouncement(active);
    }

    @Override // android.hardware.radio.ITunerCallback
    public void onTrafficAnnouncement(boolean active) {
        this.mHandler.post(new Runnable(active) {
            /* class android.hardware.radio.$$Lambda$TunerCallbackAdapter$tiaoLZrR2K56rYeqHvSRh5lRdBI */
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TunerCallbackAdapter.this.lambda$onTrafficAnnouncement$7$TunerCallbackAdapter(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onEmergencyAnnouncement$8$TunerCallbackAdapter(boolean active) {
        this.mCallback.onEmergencyAnnouncement(active);
    }

    @Override // android.hardware.radio.ITunerCallback
    public void onEmergencyAnnouncement(boolean active) {
        this.mHandler.post(new Runnable(active) {
            /* class android.hardware.radio.$$Lambda$TunerCallbackAdapter$ZwPm3xxjeLvbP12KweyzqFJVnj4 */
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TunerCallbackAdapter.this.lambda$onEmergencyAnnouncement$8$TunerCallbackAdapter(this.f$1);
            }
        });
    }

    @Override // android.hardware.radio.ITunerCallback
    public void onAntennaState(boolean connected) {
        this.mIsAntennaConnected = connected;
        this.mHandler.post(new Runnable(connected) {
            /* class android.hardware.radio.$$Lambda$TunerCallbackAdapter$dRVQmFrL_tBD2wpNvborTd8W08 */
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TunerCallbackAdapter.this.lambda$onAntennaState$9$TunerCallbackAdapter(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onAntennaState$9$TunerCallbackAdapter(boolean connected) {
        this.mCallback.onAntennaState(connected);
    }

    public /* synthetic */ void lambda$onBackgroundScanAvailabilityChange$10$TunerCallbackAdapter(boolean isAvailable) {
        this.mCallback.onBackgroundScanAvailabilityChange(isAvailable);
    }

    @Override // android.hardware.radio.ITunerCallback
    public void onBackgroundScanAvailabilityChange(boolean isAvailable) {
        this.mHandler.post(new Runnable(isAvailable) {
            /* class android.hardware.radio.$$Lambda$TunerCallbackAdapter$4zf9n0sz_rU8z6a9GJmRInWrYkQ */
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TunerCallbackAdapter.this.lambda$onBackgroundScanAvailabilityChange$10$TunerCallbackAdapter(this.f$1);
            }
        });
    }

    private void sendBackgroundScanCompleteLocked() {
        this.mDelayedCompleteCallback = false;
        this.mHandler.post(new Runnable() {
            /* class android.hardware.radio.$$Lambda$TunerCallbackAdapter$xIUT1Qu5TkA83V8ttYy1zvJuFo */

            public final void run() {
                TunerCallbackAdapter.this.lambda$sendBackgroundScanCompleteLocked$11$TunerCallbackAdapter();
            }
        });
    }

    public /* synthetic */ void lambda$sendBackgroundScanCompleteLocked$11$TunerCallbackAdapter() {
        this.mCallback.onBackgroundScanComplete();
    }

    @Override // android.hardware.radio.ITunerCallback
    public void onBackgroundScanComplete() {
        synchronized (this.mLock) {
            if (this.mLastCompleteList == null) {
                Log.i(TAG, "Got onBackgroundScanComplete callback, but the program list didn't get through yet. Delaying it...");
                this.mDelayedCompleteCallback = true;
                return;
            }
            sendBackgroundScanCompleteLocked();
        }
    }

    public /* synthetic */ void lambda$onProgramListChanged$12$TunerCallbackAdapter() {
        this.mCallback.onProgramListChanged();
    }

    @Override // android.hardware.radio.ITunerCallback
    public void onProgramListChanged() {
        this.mHandler.post(new Runnable() {
            /* class android.hardware.radio.$$Lambda$TunerCallbackAdapter$UsmGhKordXy4lhCylRP0mm2NcYc */

            public final void run() {
                TunerCallbackAdapter.this.lambda$onProgramListChanged$12$TunerCallbackAdapter();
            }
        });
    }

    @Override // android.hardware.radio.ITunerCallback
    public void onProgramListUpdated(ProgramList.Chunk chunk) {
        synchronized (this.mLock) {
            if (this.mProgramList != null) {
                this.mProgramList.apply((ProgramList.Chunk) Objects.requireNonNull(chunk));
            }
        }
    }

    public /* synthetic */ void lambda$onParametersUpdated$13$TunerCallbackAdapter(Map parameters) {
        this.mCallback.onParametersUpdated(parameters);
    }

    @Override // android.hardware.radio.ITunerCallback
    public void onParametersUpdated(Map parameters) {
        this.mHandler.post(new Runnable(parameters) {
            /* class android.hardware.radio.$$Lambda$TunerCallbackAdapter$Yz4KCDu1MOynGdkDf_oMxqhjeY */
            private final /* synthetic */ Map f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TunerCallbackAdapter.this.lambda$onParametersUpdated$13$TunerCallbackAdapter(this.f$1);
            }
        });
    }
}

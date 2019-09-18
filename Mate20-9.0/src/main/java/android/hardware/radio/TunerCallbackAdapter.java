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

class TunerCallbackAdapter extends ITunerCallback.Stub {
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
                    private final /* synthetic */ ProgramList f$1;
                    private final /* synthetic */ ProgramList.OnCloseListener f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void onClose() {
                        TunerCallbackAdapter.lambda$setProgramListObserver$0(TunerCallbackAdapter.this, this.f$1, this.f$2);
                    }
                });
                programList.addOnCompleteListener(new ProgramList.OnCompleteListener(programList) {
                    private final /* synthetic */ ProgramList f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onComplete() {
                        TunerCallbackAdapter.lambda$setProgramListObserver$1(TunerCallbackAdapter.this, this.f$1);
                    }
                });
            }
        }
    }

    public static /* synthetic */ void lambda$setProgramListObserver$0(TunerCallbackAdapter tunerCallbackAdapter, ProgramList programList, ProgramList.OnCloseListener closeListener) {
        synchronized (tunerCallbackAdapter.mLock) {
            if (tunerCallbackAdapter.mProgramList == programList) {
                tunerCallbackAdapter.mProgramList = null;
                tunerCallbackAdapter.mLastCompleteList = null;
                closeListener.onClose();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001e, code lost:
        return;
     */
    public static /* synthetic */ void lambda$setProgramListObserver$1(TunerCallbackAdapter tunerCallbackAdapter, ProgramList programList) {
        synchronized (tunerCallbackAdapter.mLock) {
            if (tunerCallbackAdapter.mProgramList == programList) {
                tunerCallbackAdapter.mLastCompleteList = programList.toList();
                if (tunerCallbackAdapter.mDelayedCompleteCallback) {
                    Log.d(TAG, "Sending delayed onBackgroundScanComplete callback");
                    tunerCallbackAdapter.sendBackgroundScanCompleteLocked();
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

    public void onError(int status) {
        this.mHandler.post(new Runnable(status) {
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TunerCallbackAdapter.this.mCallback.onError(this.f$1);
            }
        });
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001f, code lost:
        if (r4 != -1) goto L_0x003f;
     */
    public void onTuneFailed(int status, ProgramSelector selector) {
        int errorCode;
        this.mHandler.post(new Runnable(status, selector) {
            private final /* synthetic */ int f$1;
            private final /* synthetic */ ProgramSelector f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                TunerCallbackAdapter.this.mCallback.onTuneFailed(this.f$1, this.f$2);
            }
        });
        if (!(status == Integer.MIN_VALUE || status == -38)) {
            if (status != -32) {
                if (!(status == -22 || status == -19)) {
                }
            }
            errorCode = 1;
            this.mHandler.post(new Runnable(errorCode) {
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    TunerCallbackAdapter.this.mCallback.onError(this.f$1);
                }
            });
        }
        Log.i(TAG, "Got an error with no mapping to the legacy API (" + status + "), doing a best-effort conversion to ERROR_SCAN_TIMEOUT");
        errorCode = 3;
        this.mHandler.post(new Runnable(errorCode) {
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TunerCallbackAdapter.this.mCallback.onError(this.f$1);
            }
        });
    }

    public void onConfigurationChanged(RadioManager.BandConfig config) {
        this.mHandler.post(new Runnable(config) {
            private final /* synthetic */ RadioManager.BandConfig f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TunerCallbackAdapter.this.mCallback.onConfigurationChanged(this.f$1);
            }
        });
    }

    public void onCurrentProgramInfoChanged(RadioManager.ProgramInfo info) {
        if (info == null) {
            Log.e(TAG, "ProgramInfo must not be null");
            return;
        }
        synchronized (this.mLock) {
            this.mCurrentProgramInfo = info;
        }
        this.mHandler.post(new Runnable(info) {
            private final /* synthetic */ RadioManager.ProgramInfo f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TunerCallbackAdapter.lambda$onCurrentProgramInfoChanged$6(TunerCallbackAdapter.this, this.f$1);
            }
        });
    }

    public static /* synthetic */ void lambda$onCurrentProgramInfoChanged$6(TunerCallbackAdapter tunerCallbackAdapter, RadioManager.ProgramInfo info) {
        tunerCallbackAdapter.mCallback.onProgramInfoChanged(info);
        RadioMetadata metadata = info.getMetadata();
        if (metadata != null) {
            tunerCallbackAdapter.mCallback.onMetadataChanged(metadata);
        }
    }

    public void onTrafficAnnouncement(boolean active) {
        this.mHandler.post(new Runnable(active) {
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TunerCallbackAdapter.this.mCallback.onTrafficAnnouncement(this.f$1);
            }
        });
    }

    public void onEmergencyAnnouncement(boolean active) {
        this.mHandler.post(new Runnable(active) {
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TunerCallbackAdapter.this.mCallback.onEmergencyAnnouncement(this.f$1);
            }
        });
    }

    public void onAntennaState(boolean connected) {
        this.mIsAntennaConnected = connected;
        this.mHandler.post(new Runnable(connected) {
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TunerCallbackAdapter.this.mCallback.onAntennaState(this.f$1);
            }
        });
    }

    public void onBackgroundScanAvailabilityChange(boolean isAvailable) {
        this.mHandler.post(new Runnable(isAvailable) {
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TunerCallbackAdapter.this.mCallback.onBackgroundScanAvailabilityChange(this.f$1);
            }
        });
    }

    private void sendBackgroundScanCompleteLocked() {
        this.mDelayedCompleteCallback = false;
        this.mHandler.post(new Runnable() {
            public final void run() {
                TunerCallbackAdapter.this.mCallback.onBackgroundScanComplete();
            }
        });
    }

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

    public void onProgramListChanged() {
        this.mHandler.post(new Runnable() {
            public final void run() {
                TunerCallbackAdapter.this.mCallback.onProgramListChanged();
            }
        });
    }

    public void onProgramListUpdated(ProgramList.Chunk chunk) {
        synchronized (this.mLock) {
            if (this.mProgramList != null) {
                this.mProgramList.apply((ProgramList.Chunk) Objects.requireNonNull(chunk));
            }
        }
    }

    public void onParametersUpdated(Map parameters) {
        this.mHandler.post(new Runnable(parameters) {
            private final /* synthetic */ Map f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TunerCallbackAdapter.this.mCallback.onParametersUpdated(this.f$1);
            }
        });
    }
}

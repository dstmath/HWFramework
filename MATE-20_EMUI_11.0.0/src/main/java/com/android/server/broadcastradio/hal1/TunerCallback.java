package com.android.server.broadcastradio.hal1;

import android.hardware.radio.ITunerCallback;
import android.hardware.radio.ProgramList;
import android.hardware.radio.ProgramSelector;
import android.hardware.radio.RadioManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Slog;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/* access modifiers changed from: package-private */
public class TunerCallback implements ITunerCallback {
    private static final String TAG = "BroadcastRadioService.TunerCallback";
    private final ITunerCallback mClientCallback;
    private boolean mInitialConfigurationDone = false;
    private final long mNativeContext;
    private final AtomicReference<ProgramList.Filter> mProgramListFilter = new AtomicReference<>();
    private final Tuner mTuner;

    /* access modifiers changed from: private */
    public interface RunnableThrowingRemoteException {
        void run() throws RemoteException;
    }

    private native void nativeDetach(long j);

    private native void nativeFinalize(long j);

    private native long nativeInit(Tuner tuner, int i);

    TunerCallback(Tuner tuner, ITunerCallback clientCallback, int halRev) {
        this.mTuner = tuner;
        this.mClientCallback = clientCallback;
        this.mNativeContext = nativeInit(tuner, halRev);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        nativeFinalize(this.mNativeContext);
        super.finalize();
    }

    public void detach() {
        nativeDetach(this.mNativeContext);
    }

    private void dispatch(RunnableThrowingRemoteException func) {
        try {
            func.run();
        } catch (RemoteException e) {
            Slog.e(TAG, "client died", e);
        }
    }

    private void handleHwFailure() {
        onError(0);
        this.mTuner.close();
    }

    /* access modifiers changed from: package-private */
    public void startProgramListUpdates(ProgramList.Filter filter) {
        if (filter == null) {
            filter = new ProgramList.Filter();
        }
        this.mProgramListFilter.set(filter);
        sendProgramListUpdate();
    }

    /* access modifiers changed from: package-private */
    public void stopProgramListUpdates() {
        this.mProgramListFilter.set(null);
    }

    /* access modifiers changed from: package-private */
    public boolean isInitialConfigurationDone() {
        return this.mInitialConfigurationDone;
    }

    public /* synthetic */ void lambda$onError$0$TunerCallback(int status) throws RemoteException {
        this.mClientCallback.onError(status);
    }

    public void onError(int status) {
        dispatch(new RunnableThrowingRemoteException(status) {
            /* class com.android.server.broadcastradio.hal1.$$Lambda$TunerCallback$QwopTG5nMx1CO2s6KecqSuCqviA */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.server.broadcastradio.hal1.TunerCallback.RunnableThrowingRemoteException
            public final void run() {
                TunerCallback.this.lambda$onError$0$TunerCallback(this.f$1);
            }
        });
    }

    public void onTuneFailed(int result, ProgramSelector selector) {
        Slog.e(TAG, "Not applicable for HAL 1.x");
    }

    public void onConfigurationChanged(RadioManager.BandConfig config) {
        this.mInitialConfigurationDone = true;
        dispatch(new RunnableThrowingRemoteException(config) {
            /* class com.android.server.broadcastradio.hal1.$$Lambda$TunerCallback$qRbdRNnpcaEQYaUWeumt5lHhtY */
            private final /* synthetic */ RadioManager.BandConfig f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.server.broadcastradio.hal1.TunerCallback.RunnableThrowingRemoteException
            public final void run() {
                TunerCallback.this.lambda$onConfigurationChanged$1$TunerCallback(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onConfigurationChanged$1$TunerCallback(RadioManager.BandConfig config) throws RemoteException {
        this.mClientCallback.onConfigurationChanged(config);
    }

    public /* synthetic */ void lambda$onCurrentProgramInfoChanged$2$TunerCallback(RadioManager.ProgramInfo info) throws RemoteException {
        this.mClientCallback.onCurrentProgramInfoChanged(info);
    }

    public void onCurrentProgramInfoChanged(RadioManager.ProgramInfo info) {
        dispatch(new RunnableThrowingRemoteException(info) {
            /* class com.android.server.broadcastradio.hal1.$$Lambda$TunerCallback$yDfY5pWuRHaQpNiYhPjLkNUUrc0 */
            private final /* synthetic */ RadioManager.ProgramInfo f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.server.broadcastradio.hal1.TunerCallback.RunnableThrowingRemoteException
            public final void run() {
                TunerCallback.this.lambda$onCurrentProgramInfoChanged$2$TunerCallback(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onTrafficAnnouncement$3$TunerCallback(boolean active) throws RemoteException {
        this.mClientCallback.onTrafficAnnouncement(active);
    }

    public void onTrafficAnnouncement(boolean active) {
        dispatch(new RunnableThrowingRemoteException(active) {
            /* class com.android.server.broadcastradio.hal1.$$Lambda$TunerCallback$nm8WiKzJMmmFFCbXZdjr71O3V8Q */
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.server.broadcastradio.hal1.TunerCallback.RunnableThrowingRemoteException
            public final void run() {
                TunerCallback.this.lambda$onTrafficAnnouncement$3$TunerCallback(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onEmergencyAnnouncement$4$TunerCallback(boolean active) throws RemoteException {
        this.mClientCallback.onEmergencyAnnouncement(active);
    }

    public void onEmergencyAnnouncement(boolean active) {
        dispatch(new RunnableThrowingRemoteException(active) {
            /* class com.android.server.broadcastradio.hal1.$$Lambda$TunerCallback$h4udaDmWtNrprVGi_U0x7oSJc */
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.server.broadcastradio.hal1.TunerCallback.RunnableThrowingRemoteException
            public final void run() {
                TunerCallback.this.lambda$onEmergencyAnnouncement$4$TunerCallback(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onAntennaState$5$TunerCallback(boolean connected) throws RemoteException {
        this.mClientCallback.onAntennaState(connected);
    }

    public void onAntennaState(boolean connected) {
        dispatch(new RunnableThrowingRemoteException(connected) {
            /* class com.android.server.broadcastradio.hal1.$$Lambda$TunerCallback$QNBMPvImBEGMe4jaw6iOF4QPjns */
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.server.broadcastradio.hal1.TunerCallback.RunnableThrowingRemoteException
            public final void run() {
                TunerCallback.this.lambda$onAntennaState$5$TunerCallback(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onBackgroundScanAvailabilityChange$6$TunerCallback(boolean isAvailable) throws RemoteException {
        this.mClientCallback.onBackgroundScanAvailabilityChange(isAvailable);
    }

    public void onBackgroundScanAvailabilityChange(boolean isAvailable) {
        dispatch(new RunnableThrowingRemoteException(isAvailable) {
            /* class com.android.server.broadcastradio.hal1.$$Lambda$TunerCallback$ndOBpfBmClsz77tzZfe3mvcA1lI */
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.server.broadcastradio.hal1.TunerCallback.RunnableThrowingRemoteException
            public final void run() {
                TunerCallback.this.lambda$onBackgroundScanAvailabilityChange$6$TunerCallback(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onBackgroundScanComplete$7$TunerCallback() throws RemoteException {
        this.mClientCallback.onBackgroundScanComplete();
    }

    public void onBackgroundScanComplete() {
        dispatch(new RunnableThrowingRemoteException() {
            /* class com.android.server.broadcastradio.hal1.$$Lambda$TunerCallback$YlDkqdeYbHPdKcgZh23aJ5Yw8mg */

            @Override // com.android.server.broadcastradio.hal1.TunerCallback.RunnableThrowingRemoteException
            public final void run() {
                TunerCallback.this.lambda$onBackgroundScanComplete$7$TunerCallback();
            }
        });
    }

    public /* synthetic */ void lambda$onProgramListChanged$8$TunerCallback() throws RemoteException {
        this.mClientCallback.onProgramListChanged();
    }

    public void onProgramListChanged() {
        dispatch(new RunnableThrowingRemoteException() {
            /* class com.android.server.broadcastradio.hal1.$$Lambda$TunerCallback$mdqODkiuJlYCJRXqdXBCd6vdp4 */

            @Override // com.android.server.broadcastradio.hal1.TunerCallback.RunnableThrowingRemoteException
            public final void run() {
                TunerCallback.this.lambda$onProgramListChanged$8$TunerCallback();
            }
        });
        sendProgramListUpdate();
    }

    private void sendProgramListUpdate() {
        ProgramList.Filter filter = this.mProgramListFilter.get();
        if (filter != null) {
            try {
                dispatch(new RunnableThrowingRemoteException(new ProgramList.Chunk(true, true, (Set) this.mTuner.getProgramList(filter.getVendorFilter()).stream().collect(Collectors.toSet()), (Set) null)) {
                    /* class com.android.server.broadcastradio.hal1.$$Lambda$TunerCallback$C_9BcvTpHXxQjChu9LBHT0XU */
                    private final /* synthetic */ ProgramList.Chunk f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // com.android.server.broadcastradio.hal1.TunerCallback.RunnableThrowingRemoteException
                    public final void run() {
                        TunerCallback.this.lambda$sendProgramListUpdate$9$TunerCallback(this.f$1);
                    }
                });
            } catch (IllegalStateException e) {
                Slog.d(TAG, "Program list not ready yet");
            }
        }
    }

    public /* synthetic */ void lambda$sendProgramListUpdate$9$TunerCallback(ProgramList.Chunk chunk) throws RemoteException {
        this.mClientCallback.onProgramListUpdated(chunk);
    }

    public /* synthetic */ void lambda$onProgramListUpdated$10$TunerCallback(ProgramList.Chunk chunk) throws RemoteException {
        this.mClientCallback.onProgramListUpdated(chunk);
    }

    public void onProgramListUpdated(ProgramList.Chunk chunk) {
        dispatch(new RunnableThrowingRemoteException(chunk) {
            /* class com.android.server.broadcastradio.hal1.$$Lambda$TunerCallback$yVJR7oPW6kDozlkthdDAOaT7L4 */
            private final /* synthetic */ ProgramList.Chunk f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.server.broadcastradio.hal1.TunerCallback.RunnableThrowingRemoteException
            public final void run() {
                TunerCallback.this.lambda$onProgramListUpdated$10$TunerCallback(this.f$1);
            }
        });
    }

    public void onParametersUpdated(Map parameters) {
        Slog.e(TAG, "Not applicable for HAL 1.x");
    }

    public IBinder asBinder() {
        throw new RuntimeException("Not a binder");
    }
}

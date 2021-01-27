package com.android.server.broadcastradio;

import android.content.Context;
import android.hardware.radio.IAnnouncementListener;
import android.hardware.radio.ICloseHandle;
import android.hardware.radio.IRadioService;
import android.hardware.radio.ITuner;
import android.hardware.radio.ITunerCallback;
import android.hardware.radio.RadioManager;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.SystemService;
import com.android.server.broadcastradio.hal2.AnnouncementAggregator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

public class BroadcastRadioService extends SystemService {
    private static final boolean DEBUG = false;
    private static final String TAG = "BcRadioSrv";
    private final com.android.server.broadcastradio.hal1.BroadcastRadioService mHal1;
    private final com.android.server.broadcastradio.hal2.BroadcastRadioService mHal2;
    private final Object mLock;
    private final ServiceImpl mServiceImpl;
    private List<RadioManager.ModuleProperties> mV1Modules;

    public BroadcastRadioService(Context context) {
        super(context);
        this.mServiceImpl = new ServiceImpl();
        this.mLock = new Object();
        this.mV1Modules = null;
        this.mHal1 = new com.android.server.broadcastradio.hal1.BroadcastRadioService();
        this.mV1Modules = this.mHal1.loadModules();
        OptionalInt max = this.mV1Modules.stream().mapToInt($$Lambda$h9uu6awtPxlZjabQhUCMBWQXSFM.INSTANCE).max();
        this.mHal2 = new com.android.server.broadcastradio.hal2.BroadcastRadioService(max.isPresent() ? max.getAsInt() + 1 : 0);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.broadcastradio.BroadcastRadioService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.broadcastradio.BroadcastRadioService$ServiceImpl, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("broadcastradio", this.mServiceImpl);
    }

    private class ServiceImpl extends IRadioService.Stub {
        private ServiceImpl() {
        }

        private void enforcePolicyAccess() {
            if (BroadcastRadioService.this.getContext().checkCallingPermission("android.permission.ACCESS_BROADCAST_RADIO") != 0) {
                throw new SecurityException("ACCESS_BROADCAST_RADIO permission not granted");
            }
        }

        public List<RadioManager.ModuleProperties> listModules() {
            enforcePolicyAccess();
            List<RadioManager.ModuleProperties> modules = new ArrayList<>();
            modules.addAll(BroadcastRadioService.this.mV1Modules);
            modules.addAll(BroadcastRadioService.this.mHal2.listModules());
            return modules;
        }

        public ITuner openTuner(int moduleId, RadioManager.BandConfig bandConfig, boolean withAudio, ITunerCallback callback) throws RemoteException {
            enforcePolicyAccess();
            if (callback != null) {
                synchronized (BroadcastRadioService.this.mLock) {
                    if (BroadcastRadioService.this.mHal2.hasModule(moduleId)) {
                        return BroadcastRadioService.this.mHal2.openSession(moduleId, bandConfig, withAudio, callback);
                    }
                    return BroadcastRadioService.this.mHal1.openTuner(moduleId, bandConfig, withAudio, callback);
                }
            }
            throw new IllegalArgumentException("Callback must not be empty");
        }

        public ICloseHandle addAnnouncementListener(int[] enabledTypes, IAnnouncementListener listener) {
            Objects.requireNonNull(enabledTypes);
            Objects.requireNonNull(listener);
            enforcePolicyAccess();
            synchronized (BroadcastRadioService.this.mLock) {
                if (!BroadcastRadioService.this.mHal2.hasAnyModules()) {
                    Slog.i(BroadcastRadioService.TAG, "There are no HAL 2.x modules registered");
                    return new AnnouncementAggregator(listener);
                }
                return BroadcastRadioService.this.mHal2.addAnnouncementListener(enabledTypes, listener);
            }
        }
    }
}

package com.android.server.broadcastradio.hal2;

import android.hardware.broadcastradio.V2_0.IBroadcastRadio;
import android.hardware.radio.IAnnouncementListener;
import android.hardware.radio.ICloseHandle;
import android.hardware.radio.ITuner;
import android.hardware.radio.ITunerCallback;
import android.hardware.radio.RadioManager;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.os.IHwBinder;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BroadcastRadioService {
    private static final String TAG = "BcRadio2Srv";
    private IHwBinder.DeathRecipient mDeathRecipient = new IHwBinder.DeathRecipient() {
        /* class com.android.server.broadcastradio.hal2.BroadcastRadioService.AnonymousClass2 */

        public void serviceDied(long cookie) {
            Slog.v(BroadcastRadioService.TAG, "serviceDied(" + cookie + ")");
            synchronized (BroadcastRadioService.this.mLock) {
                int moduleId = (int) cookie;
                RadioModule prevModule = (RadioModule) BroadcastRadioService.this.mModules.remove(Integer.valueOf(moduleId));
                if (prevModule != null) {
                    prevModule.closeSessions(0);
                }
                for (Map.Entry<String, Integer> entry : BroadcastRadioService.this.mServiceNameToModuleIdMap.entrySet()) {
                    if (entry.getValue().intValue() == moduleId) {
                        Slog.i(BroadcastRadioService.TAG, "service " + entry.getKey() + " died; removed RadioModule with ID " + moduleId);
                        return;
                    }
                }
            }
        }
    };
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
    private final Map<Integer, RadioModule> mModules = new HashMap();
    @GuardedBy({"mLock"})
    private int mNextModuleId = 0;
    private IServiceNotification.Stub mServiceListener = new IServiceNotification.Stub() {
        /* class com.android.server.broadcastradio.hal2.BroadcastRadioService.AnonymousClass1 */

        @Override // android.hidl.manager.V1_0.IServiceNotification
        public void onRegistration(String fqName, String serviceName, boolean preexisting) {
            Slog.v(BroadcastRadioService.TAG, "onRegistration(" + fqName + ", " + serviceName + ", " + preexisting + ")");
            synchronized (BroadcastRadioService.this.mLock) {
                Integer moduleId = (Integer) BroadcastRadioService.this.mServiceNameToModuleIdMap.get(serviceName);
                boolean newService = false;
                if (moduleId == null) {
                    newService = true;
                    moduleId = Integer.valueOf(BroadcastRadioService.this.mNextModuleId);
                }
                RadioModule module = RadioModule.tryLoadingModule(moduleId.intValue(), serviceName);
                if (module != null) {
                    Slog.v(BroadcastRadioService.TAG, "loaded broadcast radio module " + moduleId + ": " + serviceName + " (HAL 2.0)");
                    RadioModule prevModule = (RadioModule) BroadcastRadioService.this.mModules.put(moduleId, module);
                    if (prevModule != null) {
                        prevModule.closeSessions(0);
                    }
                    if (newService) {
                        BroadcastRadioService.this.mServiceNameToModuleIdMap.put(serviceName, moduleId);
                        BroadcastRadioService.access$208(BroadcastRadioService.this);
                    }
                    try {
                        module.getService().linkToDeath(BroadcastRadioService.this.mDeathRecipient, (long) moduleId.intValue());
                    } catch (RemoteException e) {
                        BroadcastRadioService.this.mModules.remove(moduleId);
                    }
                }
            }
        }
    };
    @GuardedBy({"mLock"})
    private final Map<String, Integer> mServiceNameToModuleIdMap = new HashMap();

    static /* synthetic */ int access$208(BroadcastRadioService x0) {
        int i = x0.mNextModuleId;
        x0.mNextModuleId = i + 1;
        return i;
    }

    public BroadcastRadioService(int nextModuleId) {
        this.mNextModuleId = nextModuleId;
        try {
            IServiceManager manager = IServiceManager.getService();
            if (manager == null) {
                Slog.e(TAG, "failed to get HIDL Service Manager");
            } else {
                manager.registerForNotifications(IBroadcastRadio.kInterfaceName, "", this.mServiceListener);
            }
        } catch (RemoteException ex) {
            Slog.e(TAG, "failed to register for service notifications: ", ex);
        }
    }

    public Collection<RadioManager.ModuleProperties> listModules() {
        Collection<RadioManager.ModuleProperties> collection;
        synchronized (this.mLock) {
            collection = (Collection) this.mModules.values().stream().map($$Lambda$BroadcastRadioService$NfRbSZZMVkp9di5eX6n4aCxsJVI.INSTANCE).collect(Collectors.toList());
        }
        return collection;
    }

    public boolean hasModule(int id) {
        boolean containsKey;
        synchronized (this.mLock) {
            containsKey = this.mModules.containsKey(Integer.valueOf(id));
        }
        return containsKey;
    }

    public boolean hasAnyModules() {
        boolean z;
        synchronized (this.mLock) {
            z = !this.mModules.isEmpty();
        }
        return z;
    }

    public ITuner openSession(int moduleId, RadioManager.BandConfig legacyConfig, boolean withAudio, ITunerCallback callback) throws RemoteException {
        RadioModule module;
        Objects.requireNonNull(callback);
        if (withAudio) {
            synchronized (this.mLock) {
                module = this.mModules.get(Integer.valueOf(moduleId));
                if (module == null) {
                    throw new IllegalArgumentException("Invalid module ID");
                }
            }
            TunerSession tunerSession = module.openSession(callback);
            if (legacyConfig != null) {
                tunerSession.setConfiguration(legacyConfig);
            }
            return tunerSession;
        }
        throw new IllegalArgumentException("Non-audio sessions not supported with HAL 2.x");
    }

    public ICloseHandle addAnnouncementListener(int[] enabledTypes, IAnnouncementListener listener) {
        AnnouncementAggregator aggregator = new AnnouncementAggregator(listener);
        boolean anySupported = false;
        synchronized (this.mLock) {
            for (RadioModule module : this.mModules.values()) {
                try {
                    aggregator.watchModule(module, enabledTypes);
                    anySupported = true;
                } catch (UnsupportedOperationException ex) {
                    Slog.v(TAG, "Announcements not supported for this module", ex);
                }
            }
        }
        if (!anySupported) {
            Slog.i(TAG, "There are no HAL modules that support announcements");
        }
        return aggregator;
    }
}

package android.app;

import android.app.servertransaction.ClientTransaction;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ParceledListSlice;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import com.android.internal.app.IVoiceInteractor;
import java.util.List;
import java.util.Map;

public interface IApplicationThread extends IInterface {

    public static abstract class Stub extends Binder implements IApplicationThread {
        private static final String DESCRIPTOR = "android.app.IApplicationThread";
        static final int TRANSACTION_attachAgent = 49;
        static final int TRANSACTION_bindApplication = 4;
        static final int TRANSACTION_clearDnsCache = 26;
        static final int TRANSACTION_dispatchPackageBroadcast = 22;
        static final int TRANSACTION_dumpActivity = 25;
        static final int TRANSACTION_dumpDbInfo = 36;
        static final int TRANSACTION_dumpGfxInfo = 34;
        static final int TRANSACTION_dumpHeap = 24;
        static final int TRANSACTION_dumpMemInfo = 32;
        static final int TRANSACTION_dumpMemInfoProto = 33;
        static final int TRANSACTION_dumpProvider = 35;
        static final int TRANSACTION_dumpService = 12;
        static final int TRANSACTION_handleTrustStorageUpdate = 48;
        static final int TRANSACTION_iawareTrimMemory = 31;
        static final int TRANSACTION_notifyCleartextNetwork = 44;
        static final int TRANSACTION_processInBackground = 9;
        static final int TRANSACTION_profilerControl = 16;
        static final int TRANSACTION_requestAssistContextExtras = 38;
        static final int TRANSACTION_requestContentNode = 55;
        static final int TRANSACTION_requestContentOther = 56;
        static final int TRANSACTION_runIsolatedEntryPoint = 5;
        static final int TRANSACTION_scheduleApplicationInfoChanged = 50;
        static final int TRANSACTION_scheduleApplicationThemeInfoChanged = 51;
        static final int TRANSACTION_scheduleBindService = 10;
        static final int TRANSACTION_scheduleCrash = 23;
        static final int TRANSACTION_scheduleCreateBackupAgent = 18;
        static final int TRANSACTION_scheduleCreateService = 2;
        static final int TRANSACTION_scheduleDestroyBackupAgent = 19;
        static final int TRANSACTION_scheduleEnterAnimationComplete = 43;
        static final int TRANSACTION_scheduleExit = 6;
        static final int TRANSACTION_scheduleFreeFormOutLineChanged = 57;
        static final int TRANSACTION_scheduleInstallProvider = 41;
        static final int TRANSACTION_scheduleLocalVoiceInteractionStarted = 47;
        static final int TRANSACTION_scheduleLowMemory = 14;
        static final int TRANSACTION_scheduleOnNewActivityOptions = 20;
        static final int TRANSACTION_schedulePCWindowStateChanged = 54;
        static final int TRANSACTION_scheduleReceiver = 1;
        static final int TRANSACTION_scheduleRegisteredReceiver = 13;
        static final int TRANSACTION_scheduleRestoreFreeFormConfig = 58;
        static final int TRANSACTION_scheduleServiceArgs = 7;
        static final int TRANSACTION_scheduleSleeping = 15;
        static final int TRANSACTION_scheduleStopService = 3;
        static final int TRANSACTION_scheduleSuicide = 21;
        static final int TRANSACTION_scheduleTransaction = 53;
        static final int TRANSACTION_scheduleTranslucentConversionComplete = 39;
        static final int TRANSACTION_scheduleTrimMemory = 30;
        static final int TRANSACTION_scheduleUnbindService = 11;
        static final int TRANSACTION_setCoreSettings = 28;
        static final int TRANSACTION_setHttpProxy = 27;
        static final int TRANSACTION_setNetworkBlockSeq = 52;
        static final int TRANSACTION_setProcessState = 40;
        static final int TRANSACTION_setSchedulingGroup = 17;
        static final int TRANSACTION_startBinderTracking = 45;
        static final int TRANSACTION_stopBinderTrackingAndDump = 46;
        static final int TRANSACTION_unstableProviderDied = 37;
        static final int TRANSACTION_updatePackageCompatibilityInfo = 29;
        static final int TRANSACTION_updateTimePrefs = 42;
        static final int TRANSACTION_updateTimeZone = 8;

        private static class Proxy implements IApplicationThread {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void scheduleReceiver(Intent intent, ActivityInfo info, CompatibilityInfo compatInfo, int resultCode, String data, Bundle extras, boolean sync, int sendingUser, int processState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (compatInfo != null) {
                        _data.writeInt(1);
                        compatInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(resultCode);
                    _data.writeString(data);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sync);
                    _data.writeInt(sendingUser);
                    _data.writeInt(processState);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleCreateService(IBinder token, ServiceInfo info, CompatibilityInfo compatInfo, int processState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (compatInfo != null) {
                        _data.writeInt(1);
                        compatInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(processState);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleStopService(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void bindApplication(String packageName, ApplicationInfo info, List<ProviderInfo> providers, ComponentName testName, ProfilerInfo profilerInfo, Bundle testArguments, IInstrumentationWatcher testWatcher, IUiAutomationConnection uiAutomationConnection, int debugMode, boolean enableBinderTracking, boolean trackAllocation, boolean restrictedBackupMode, boolean persistent, Configuration config, CompatibilityInfo compatInfo, Map services, Bundle coreSettings, String buildSerial, boolean isAutofillCompatEnabled) throws RemoteException {
                int i;
                ApplicationInfo applicationInfo = info;
                ComponentName componentName = testName;
                ProfilerInfo profilerInfo2 = profilerInfo;
                Bundle bundle = testArguments;
                Configuration configuration = config;
                CompatibilityInfo compatibilityInfo = compatInfo;
                Bundle bundle2 = coreSettings;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(packageName);
                        if (applicationInfo != null) {
                            _data.writeInt(1);
                            applicationInfo.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeTypedList(providers);
                            if (componentName != null) {
                                _data.writeInt(1);
                                componentName.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (profilerInfo2 != null) {
                                _data.writeInt(1);
                                profilerInfo2.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (bundle != null) {
                                _data.writeInt(1);
                                bundle.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            _data.writeStrongBinder(testWatcher != null ? testWatcher.asBinder() : null);
                            _data.writeStrongBinder(uiAutomationConnection != null ? uiAutomationConnection.asBinder() : null);
                        } catch (Throwable th) {
                            th = th;
                            int i2 = debugMode;
                            boolean z = enableBinderTracking;
                            boolean z2 = trackAllocation;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(debugMode);
                        } catch (Throwable th2) {
                            th = th2;
                            boolean z3 = enableBinderTracking;
                            boolean z22 = trackAllocation;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        List<ProviderInfo> list = providers;
                        int i22 = debugMode;
                        boolean z32 = enableBinderTracking;
                        boolean z222 = trackAllocation;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(enableBinderTracking ? 1 : 0);
                        try {
                            _data.writeInt(trackAllocation ? 1 : 0);
                            _data.writeInt(restrictedBackupMode ? 1 : 0);
                            _data.writeInt(persistent ? 1 : 0);
                            if (configuration != null) {
                                _data.writeInt(1);
                                i = 0;
                                configuration.writeToParcel(_data, 0);
                            } else {
                                i = 0;
                                _data.writeInt(0);
                            }
                            if (compatibilityInfo != null) {
                                _data.writeInt(1);
                                compatibilityInfo.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(i);
                            }
                            _data.writeMap(services);
                            if (bundle2 != null) {
                                _data.writeInt(1);
                                bundle2.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            _data.writeString(buildSerial);
                            _data.writeInt(isAutofillCompatEnabled ? 1 : 0);
                            this.mRemote.transact(4, _data, null, 1);
                            _data.recycle();
                        } catch (Throwable th4) {
                            th = th4;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        boolean z2222 = trackAllocation;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    String str = packageName;
                    List<ProviderInfo> list2 = providers;
                    int i222 = debugMode;
                    boolean z322 = enableBinderTracking;
                    boolean z22222 = trackAllocation;
                    _data.recycle();
                    throw th;
                }
            }

            public void runIsolatedEntryPoint(String entryPoint, String[] entryPointArgs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(entryPoint);
                    _data.writeStringArray(entryPointArgs);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleExit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleServiceArgs(IBinder token, ParceledListSlice args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void updateTimeZone() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void processInBackground() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleBindService(IBinder token, Intent intent, boolean rebind, int processState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(rebind);
                    _data.writeInt(processState);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleUnbindService(IBinder token, Intent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dumpService(ParcelFileDescriptor fd, IBinder servicetoken, String[] args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fd != null) {
                        _data.writeInt(1);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(servicetoken);
                    _data.writeStringArray(args);
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleRegisteredReceiver(IIntentReceiver receiver, Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser, int processState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(resultCode);
                    _data.writeString(data);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(ordered);
                    _data.writeInt(sticky);
                    _data.writeInt(sendingUser);
                    _data.writeInt(processState);
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleLowMemory() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleSleeping(IBinder token, boolean sleeping) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(sleeping);
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void profilerControl(boolean start, ProfilerInfo profilerInfo, int profileType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(start);
                    if (profilerInfo != null) {
                        _data.writeInt(1);
                        profilerInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(profileType);
                    this.mRemote.transact(16, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setSchedulingGroup(int group) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(group);
                    this.mRemote.transact(17, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleCreateBackupAgent(ApplicationInfo app, CompatibilityInfo compatInfo, int backupMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (app != null) {
                        _data.writeInt(1);
                        app.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (compatInfo != null) {
                        _data.writeInt(1);
                        compatInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(backupMode);
                    this.mRemote.transact(18, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleDestroyBackupAgent(ApplicationInfo app, CompatibilityInfo compatInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (app != null) {
                        _data.writeInt(1);
                        app.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (compatInfo != null) {
                        _data.writeInt(1);
                        compatInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(19, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleOnNewActivityOptions(IBinder token, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(20, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleSuicide() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(21, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchPackageBroadcast(int cmd, String[] packages) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cmd);
                    _data.writeStringArray(packages);
                    this.mRemote.transact(22, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleCrash(String msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(msg);
                    this.mRemote.transact(23, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dumpHeap(boolean managed, boolean mallocInfo, boolean runGc, String path, ParcelFileDescriptor fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(managed);
                    _data.writeInt(mallocInfo);
                    _data.writeInt(runGc);
                    _data.writeString(path);
                    if (fd != null) {
                        _data.writeInt(1);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(24, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dumpActivity(ParcelFileDescriptor fd, IBinder servicetoken, String prefix, String[] args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fd != null) {
                        _data.writeInt(1);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(servicetoken);
                    _data.writeString(prefix);
                    _data.writeStringArray(args);
                    this.mRemote.transact(25, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void clearDnsCache() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(26, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setHttpProxy(String proxy, String port, String exclList, Uri pacFileUrl) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(proxy);
                    _data.writeString(port);
                    _data.writeString(exclList);
                    if (pacFileUrl != null) {
                        _data.writeInt(1);
                        pacFileUrl.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(27, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setCoreSettings(Bundle coreSettings) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (coreSettings != null) {
                        _data.writeInt(1);
                        coreSettings.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(28, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void updatePackageCompatibilityInfo(String pkg, CompatibilityInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(29, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleTrimMemory(int level) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(level);
                    this.mRemote.transact(30, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void iawareTrimMemory(int level, boolean fromIAware) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(level);
                    _data.writeInt(fromIAware);
                    this.mRemote.transact(31, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dumpMemInfo(ParcelFileDescriptor fd, Debug.MemoryInfo mem, boolean checkin, boolean dumpInfo, boolean dumpDalvik, boolean dumpSummaryOnly, boolean dumpUnreachable, String[] args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fd != null) {
                        _data.writeInt(1);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (mem != null) {
                        _data.writeInt(1);
                        mem.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(checkin);
                    _data.writeInt(dumpInfo);
                    _data.writeInt(dumpDalvik);
                    _data.writeInt(dumpSummaryOnly);
                    _data.writeInt(dumpUnreachable);
                    _data.writeStringArray(args);
                    this.mRemote.transact(32, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dumpMemInfoProto(ParcelFileDescriptor fd, Debug.MemoryInfo mem, boolean dumpInfo, boolean dumpDalvik, boolean dumpSummaryOnly, boolean dumpUnreachable, String[] args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fd != null) {
                        _data.writeInt(1);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (mem != null) {
                        _data.writeInt(1);
                        mem.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(dumpInfo);
                    _data.writeInt(dumpDalvik);
                    _data.writeInt(dumpSummaryOnly);
                    _data.writeInt(dumpUnreachable);
                    _data.writeStringArray(args);
                    this.mRemote.transact(33, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dumpGfxInfo(ParcelFileDescriptor fd, String[] args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fd != null) {
                        _data.writeInt(1);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringArray(args);
                    this.mRemote.transact(34, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dumpProvider(ParcelFileDescriptor fd, IBinder servicetoken, String[] args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fd != null) {
                        _data.writeInt(1);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(servicetoken);
                    _data.writeStringArray(args);
                    this.mRemote.transact(35, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dumpDbInfo(ParcelFileDescriptor fd, String[] args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fd != null) {
                        _data.writeInt(1);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringArray(args);
                    this.mRemote.transact(36, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void unstableProviderDied(IBinder provider) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(provider);
                    this.mRemote.transact(37, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void requestAssistContextExtras(IBinder activityToken, IBinder requestToken, int requestType, int sessionId, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(activityToken);
                    _data.writeStrongBinder(requestToken);
                    _data.writeInt(requestType);
                    _data.writeInt(sessionId);
                    _data.writeInt(flags);
                    this.mRemote.transact(38, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleTranslucentConversionComplete(IBinder token, boolean timeout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(timeout);
                    this.mRemote.transact(39, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setProcessState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(40, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleInstallProvider(ProviderInfo provider) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (provider != null) {
                        _data.writeInt(1);
                        provider.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(41, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void updateTimePrefs(int timeFormatPreference) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(timeFormatPreference);
                    this.mRemote.transact(42, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleEnterAnimationComplete(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(43, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void notifyCleartextNetwork(byte[] firstPacket) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(firstPacket);
                    this.mRemote.transact(44, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void startBinderTracking() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(45, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void stopBinderTrackingAndDump(ParcelFileDescriptor fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fd != null) {
                        _data.writeInt(1);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(46, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleLocalVoiceInteractionStarted(IBinder token, IVoiceInteractor voiceInteractor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeStrongBinder(voiceInteractor != null ? voiceInteractor.asBinder() : null);
                    this.mRemote.transact(47, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void handleTrustStorageUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(48, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void attachAgent(String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    this.mRemote.transact(49, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleApplicationInfoChanged(ApplicationInfo ai) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ai != null) {
                        _data.writeInt(1);
                        ai.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(50, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleApplicationThemeInfoChanged(ApplicationInfo ai, boolean fromThemeChange) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ai != null) {
                        _data.writeInt(1);
                        ai.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(fromThemeChange);
                    this.mRemote.transact(51, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setNetworkBlockSeq(long procStateSeq) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(procStateSeq);
                    this.mRemote.transact(52, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleTransaction(ClientTransaction transaction) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (transaction != null) {
                        _data.writeInt(1);
                        transaction.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(53, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void schedulePCWindowStateChanged(IBinder token, int windowState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(windowState);
                    this.mRemote.transact(54, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void requestContentNode(IBinder appToken, Bundle data, int token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(appToken);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(token);
                    this.mRemote.transact(55, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void requestContentOther(IBinder appToken, Bundle data, int token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(appToken);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(token);
                    this.mRemote.transact(56, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleFreeFormOutLineChanged(IBinder token, int touchingState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(touchingState);
                    this.mRemote.transact(57, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void scheduleRestoreFreeFormConfig(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(58, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IApplicationThread asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IApplicationThread)) {
                return new Proxy(obj);
            }
            return (IApplicationThread) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v0, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v4, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v7, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v0, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v14, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v58, resolved type: android.content.pm.ParceledListSlice} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v17, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v20, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v23, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v29, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v82, resolved type: android.app.ProfilerInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v32, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v35, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v38, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v94, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v41, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v44, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v47, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v48, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v50, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v58, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v61, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v64, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v68, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v75, resolved type: android.content.pm.ProviderInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v71, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v74, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v88, resolved type: android.content.pm.ApplicationInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v77, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v129, resolved type: android.content.pm.ApplicationInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v80, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v95, resolved type: android.app.servertransaction.ClientTransaction} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v83, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v134, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v86, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v138, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v89, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v90, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v91, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v92, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v93, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v94, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v95, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v96, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v97, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v98, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v99, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v100, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v101, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v102, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v103, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v104, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v105, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v106, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v107, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v108, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v109, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v110, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v111, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v112, resolved type: android.content.res.CompatibilityInfo} */
        /* JADX WARNING: type inference failed for: r7v13, types: [android.os.Bundle] */
        /* JADX WARNING: type inference failed for: r7v16, types: [android.content.pm.ParceledListSlice] */
        /* JADX WARNING: type inference failed for: r2v14, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r7v19, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r1v63, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r7v22, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r0v13, types: [android.os.ParcelFileDescriptor] */
        /* JADX WARNING: type inference failed for: r7v25, types: [android.os.ParcelFileDescriptor] */
        /* JADX WARNING: type inference failed for: r7v31, types: [android.app.ProfilerInfo] */
        /* JADX WARNING: type inference failed for: r7v40, types: [android.os.Bundle] */
        /* JADX WARNING: type inference failed for: r0v43, types: [android.os.ParcelFileDescriptor] */
        /* JADX WARNING: type inference failed for: r7v43, types: [android.os.ParcelFileDescriptor] */
        /* JADX WARNING: type inference failed for: r3v18, types: [android.net.Uri] */
        /* JADX WARNING: type inference failed for: r7v46, types: [android.net.Uri] */
        /* JADX WARNING: type inference failed for: r7v49, types: [android.os.Bundle] */
        /* JADX WARNING: type inference failed for: r0v58, types: [android.os.ParcelFileDescriptor] */
        /* JADX WARNING: type inference failed for: r7v60, types: [android.os.ParcelFileDescriptor] */
        /* JADX WARNING: type inference failed for: r0v62, types: [android.os.ParcelFileDescriptor] */
        /* JADX WARNING: type inference failed for: r7v63, types: [android.os.ParcelFileDescriptor] */
        /* JADX WARNING: type inference failed for: r0v66, types: [android.os.ParcelFileDescriptor] */
        /* JADX WARNING: type inference failed for: r7v66, types: [android.os.ParcelFileDescriptor] */
        /* JADX WARNING: type inference failed for: r7v70, types: [android.content.pm.ProviderInfo] */
        /* JADX WARNING: type inference failed for: r0v82, types: [android.os.ParcelFileDescriptor] */
        /* JADX WARNING: type inference failed for: r7v73, types: [android.os.ParcelFileDescriptor] */
        /* JADX WARNING: type inference failed for: r7v76, types: [android.content.pm.ApplicationInfo] */
        /* JADX WARNING: type inference failed for: r7v79, types: [android.content.pm.ApplicationInfo] */
        /* JADX WARNING: type inference failed for: r7v82, types: [android.app.servertransaction.ClientTransaction] */
        /* JADX WARNING: type inference failed for: r7v85, types: [android.os.Bundle] */
        /* JADX WARNING: type inference failed for: r7v88, types: [android.os.Bundle] */
        /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
            java.lang.NullPointerException
            	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
            */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int r31, android.os.Parcel r32, android.os.Parcel r33, int r34) throws android.os.RemoteException {
            /*
                r30 = this;
                r15 = r30
                r14 = r31
                r13 = r32
                java.lang.String r12 = "android.app.IApplicationThread"
                r0 = 1598968902(0x5f4e5446, float:1.4867585E19)
                r20 = 1
                if (r14 == r0) goto L_0x0730
                r0 = 0
                r7 = 0
                switch(r14) {
                    case 1: goto L_0x06c5;
                    case 2: goto L_0x0691;
                    case 3: goto L_0x067e;
                    case 4: goto L_0x0585;
                    case 5: goto L_0x0576;
                    case 6: goto L_0x056f;
                    case 7: goto L_0x0552;
                    case 8: goto L_0x054b;
                    case 9: goto L_0x0544;
                    case 10: goto L_0x051a;
                    case 11: goto L_0x04fd;
                    case 12: goto L_0x04dc;
                    case 13: goto L_0x047e;
                    case 14: goto L_0x0477;
                    case 15: goto L_0x0463;
                    case 16: goto L_0x043d;
                    case 17: goto L_0x0432;
                    case 18: goto L_0x0405;
                    case 19: goto L_0x03dc;
                    case 20: goto L_0x03bf;
                    case 21: goto L_0x03b8;
                    case 22: goto L_0x03a9;
                    case 23: goto L_0x039e;
                    case 24: goto L_0x0362;
                    case 25: goto L_0x033d;
                    case 26: goto L_0x0336;
                    case 27: goto L_0x0311;
                    case 28: goto L_0x02f8;
                    case 29: goto L_0x02db;
                    case 30: goto L_0x02d0;
                    case 31: goto L_0x02bc;
                    case 32: goto L_0x025d;
                    case 33: goto L_0x0208;
                    case 34: goto L_0x01eb;
                    case 35: goto L_0x01ca;
                    case 36: goto L_0x01ad;
                    case 37: goto L_0x01a2;
                    case 38: goto L_0x0181;
                    case 39: goto L_0x016d;
                    case 40: goto L_0x0162;
                    case 41: goto L_0x0149;
                    case 42: goto L_0x013e;
                    case 43: goto L_0x0133;
                    case 44: goto L_0x0128;
                    case 45: goto L_0x0121;
                    case 46: goto L_0x0108;
                    case 47: goto L_0x00f5;
                    case 48: goto L_0x00ee;
                    case 49: goto L_0x00e3;
                    case 50: goto L_0x00ca;
                    case 51: goto L_0x00a8;
                    case 52: goto L_0x009d;
                    case 53: goto L_0x0084;
                    case 54: goto L_0x0075;
                    case 55: goto L_0x0054;
                    case 56: goto L_0x0033;
                    case 57: goto L_0x0024;
                    case 58: goto L_0x0019;
                    default: goto L_0x0014;
                }
            L_0x0014:
                boolean r0 = super.onTransact(r31, r32, r33, r34)
                return r0
            L_0x0019:
                r13.enforceInterface(r12)
                android.os.IBinder r0 = r32.readStrongBinder()
                r15.scheduleRestoreFreeFormConfig(r0)
                return r20
            L_0x0024:
                r13.enforceInterface(r12)
                android.os.IBinder r0 = r32.readStrongBinder()
                int r1 = r32.readInt()
                r15.scheduleFreeFormOutLineChanged(r0, r1)
                return r20
            L_0x0033:
                r13.enforceInterface(r12)
                android.os.IBinder r0 = r32.readStrongBinder()
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x004a
                android.os.Parcelable$Creator r1 = android.os.Bundle.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                r7 = r1
                android.os.Bundle r7 = (android.os.Bundle) r7
                goto L_0x004b
            L_0x004a:
            L_0x004b:
                r1 = r7
                int r2 = r32.readInt()
                r15.requestContentOther(r0, r1, r2)
                return r20
            L_0x0054:
                r13.enforceInterface(r12)
                android.os.IBinder r0 = r32.readStrongBinder()
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x006b
                android.os.Parcelable$Creator r1 = android.os.Bundle.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                r7 = r1
                android.os.Bundle r7 = (android.os.Bundle) r7
                goto L_0x006c
            L_0x006b:
            L_0x006c:
                r1 = r7
                int r2 = r32.readInt()
                r15.requestContentNode(r0, r1, r2)
                return r20
            L_0x0075:
                r13.enforceInterface(r12)
                android.os.IBinder r0 = r32.readStrongBinder()
                int r1 = r32.readInt()
                r15.schedulePCWindowStateChanged(r0, r1)
                return r20
            L_0x0084:
                r13.enforceInterface(r12)
                int r0 = r32.readInt()
                if (r0 == 0) goto L_0x0097
                android.os.Parcelable$Creator<android.app.servertransaction.ClientTransaction> r0 = android.app.servertransaction.ClientTransaction.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r13)
                r7 = r0
                android.app.servertransaction.ClientTransaction r7 = (android.app.servertransaction.ClientTransaction) r7
                goto L_0x0098
            L_0x0097:
            L_0x0098:
                r0 = r7
                r15.scheduleTransaction(r0)
                return r20
            L_0x009d:
                r13.enforceInterface(r12)
                long r0 = r32.readLong()
                r15.setNetworkBlockSeq(r0)
                return r20
            L_0x00a8:
                r13.enforceInterface(r12)
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x00bb
                android.os.Parcelable$Creator<android.content.pm.ApplicationInfo> r1 = android.content.pm.ApplicationInfo.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                r7 = r1
                android.content.pm.ApplicationInfo r7 = (android.content.pm.ApplicationInfo) r7
                goto L_0x00bc
            L_0x00bb:
            L_0x00bc:
                r1 = r7
                int r2 = r32.readInt()
                if (r2 == 0) goto L_0x00c6
                r0 = r20
            L_0x00c6:
                r15.scheduleApplicationThemeInfoChanged(r1, r0)
                return r20
            L_0x00ca:
                r13.enforceInterface(r12)
                int r0 = r32.readInt()
                if (r0 == 0) goto L_0x00dd
                android.os.Parcelable$Creator<android.content.pm.ApplicationInfo> r0 = android.content.pm.ApplicationInfo.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r13)
                r7 = r0
                android.content.pm.ApplicationInfo r7 = (android.content.pm.ApplicationInfo) r7
                goto L_0x00de
            L_0x00dd:
            L_0x00de:
                r0 = r7
                r15.scheduleApplicationInfoChanged(r0)
                return r20
            L_0x00e3:
                r13.enforceInterface(r12)
                java.lang.String r0 = r32.readString()
                r15.attachAgent(r0)
                return r20
            L_0x00ee:
                r13.enforceInterface(r12)
                r30.handleTrustStorageUpdate()
                return r20
            L_0x00f5:
                r13.enforceInterface(r12)
                android.os.IBinder r0 = r32.readStrongBinder()
                android.os.IBinder r1 = r32.readStrongBinder()
                com.android.internal.app.IVoiceInteractor r1 = com.android.internal.app.IVoiceInteractor.Stub.asInterface(r1)
                r15.scheduleLocalVoiceInteractionStarted(r0, r1)
                return r20
            L_0x0108:
                r13.enforceInterface(r12)
                int r0 = r32.readInt()
                if (r0 == 0) goto L_0x011b
                android.os.Parcelable$Creator r0 = android.os.ParcelFileDescriptor.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r13)
                r7 = r0
                android.os.ParcelFileDescriptor r7 = (android.os.ParcelFileDescriptor) r7
                goto L_0x011c
            L_0x011b:
            L_0x011c:
                r0 = r7
                r15.stopBinderTrackingAndDump(r0)
                return r20
            L_0x0121:
                r13.enforceInterface(r12)
                r30.startBinderTracking()
                return r20
            L_0x0128:
                r13.enforceInterface(r12)
                byte[] r0 = r32.createByteArray()
                r15.notifyCleartextNetwork(r0)
                return r20
            L_0x0133:
                r13.enforceInterface(r12)
                android.os.IBinder r0 = r32.readStrongBinder()
                r15.scheduleEnterAnimationComplete(r0)
                return r20
            L_0x013e:
                r13.enforceInterface(r12)
                int r0 = r32.readInt()
                r15.updateTimePrefs(r0)
                return r20
            L_0x0149:
                r13.enforceInterface(r12)
                int r0 = r32.readInt()
                if (r0 == 0) goto L_0x015c
                android.os.Parcelable$Creator<android.content.pm.ProviderInfo> r0 = android.content.pm.ProviderInfo.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r13)
                r7 = r0
                android.content.pm.ProviderInfo r7 = (android.content.pm.ProviderInfo) r7
                goto L_0x015d
            L_0x015c:
            L_0x015d:
                r0 = r7
                r15.scheduleInstallProvider(r0)
                return r20
            L_0x0162:
                r13.enforceInterface(r12)
                int r0 = r32.readInt()
                r15.setProcessState(r0)
                return r20
            L_0x016d:
                r13.enforceInterface(r12)
                android.os.IBinder r1 = r32.readStrongBinder()
                int r2 = r32.readInt()
                if (r2 == 0) goto L_0x017d
                r0 = r20
            L_0x017d:
                r15.scheduleTranslucentConversionComplete(r1, r0)
                return r20
            L_0x0181:
                r13.enforceInterface(r12)
                android.os.IBinder r6 = r32.readStrongBinder()
                android.os.IBinder r7 = r32.readStrongBinder()
                int r8 = r32.readInt()
                int r9 = r32.readInt()
                int r10 = r32.readInt()
                r0 = r15
                r1 = r6
                r2 = r7
                r3 = r8
                r4 = r9
                r5 = r10
                r0.requestAssistContextExtras(r1, r2, r3, r4, r5)
                return r20
            L_0x01a2:
                r13.enforceInterface(r12)
                android.os.IBinder r0 = r32.readStrongBinder()
                r15.unstableProviderDied(r0)
                return r20
            L_0x01ad:
                r13.enforceInterface(r12)
                int r0 = r32.readInt()
                if (r0 == 0) goto L_0x01c0
                android.os.Parcelable$Creator r0 = android.os.ParcelFileDescriptor.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r13)
                r7 = r0
                android.os.ParcelFileDescriptor r7 = (android.os.ParcelFileDescriptor) r7
                goto L_0x01c1
            L_0x01c0:
            L_0x01c1:
                r0 = r7
                java.lang.String[] r1 = r32.createStringArray()
                r15.dumpDbInfo(r0, r1)
                return r20
            L_0x01ca:
                r13.enforceInterface(r12)
                int r0 = r32.readInt()
                if (r0 == 0) goto L_0x01dd
                android.os.Parcelable$Creator r0 = android.os.ParcelFileDescriptor.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r13)
                r7 = r0
                android.os.ParcelFileDescriptor r7 = (android.os.ParcelFileDescriptor) r7
                goto L_0x01de
            L_0x01dd:
            L_0x01de:
                r0 = r7
                android.os.IBinder r1 = r32.readStrongBinder()
                java.lang.String[] r2 = r32.createStringArray()
                r15.dumpProvider(r0, r1, r2)
                return r20
            L_0x01eb:
                r13.enforceInterface(r12)
                int r0 = r32.readInt()
                if (r0 == 0) goto L_0x01fe
                android.os.Parcelable$Creator r0 = android.os.ParcelFileDescriptor.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r13)
                r7 = r0
                android.os.ParcelFileDescriptor r7 = (android.os.ParcelFileDescriptor) r7
                goto L_0x01ff
            L_0x01fe:
            L_0x01ff:
                r0 = r7
                java.lang.String[] r1 = r32.createStringArray()
                r15.dumpGfxInfo(r0, r1)
                return r20
            L_0x0208:
                r13.enforceInterface(r12)
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x021a
                android.os.Parcelable$Creator r1 = android.os.ParcelFileDescriptor.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                android.os.ParcelFileDescriptor r1 = (android.os.ParcelFileDescriptor) r1
                goto L_0x021b
            L_0x021a:
                r1 = r7
            L_0x021b:
                int r2 = r32.readInt()
                if (r2 == 0) goto L_0x022a
                android.os.Parcelable$Creator r2 = android.os.Debug.MemoryInfo.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r13)
                android.os.Debug$MemoryInfo r2 = (android.os.Debug.MemoryInfo) r2
                goto L_0x022b
            L_0x022a:
                r2 = r7
            L_0x022b:
                int r3 = r32.readInt()
                if (r3 == 0) goto L_0x0234
                r3 = r20
                goto L_0x0235
            L_0x0234:
                r3 = r0
            L_0x0235:
                int r4 = r32.readInt()
                if (r4 == 0) goto L_0x023e
                r4 = r20
                goto L_0x023f
            L_0x023e:
                r4 = r0
            L_0x023f:
                int r5 = r32.readInt()
                if (r5 == 0) goto L_0x0248
                r5 = r20
                goto L_0x0249
            L_0x0248:
                r5 = r0
            L_0x0249:
                int r6 = r32.readInt()
                if (r6 == 0) goto L_0x0252
                r6 = r20
                goto L_0x0253
            L_0x0252:
                r6 = r0
            L_0x0253:
                java.lang.String[] r8 = r32.createStringArray()
                r0 = r15
                r7 = r8
                r0.dumpMemInfoProto(r1, r2, r3, r4, r5, r6, r7)
                return r20
            L_0x025d:
                r13.enforceInterface(r12)
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x026f
                android.os.Parcelable$Creator r1 = android.os.ParcelFileDescriptor.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                android.os.ParcelFileDescriptor r1 = (android.os.ParcelFileDescriptor) r1
                goto L_0x0270
            L_0x026f:
                r1 = r7
            L_0x0270:
                int r2 = r32.readInt()
                if (r2 == 0) goto L_0x027f
                android.os.Parcelable$Creator r2 = android.os.Debug.MemoryInfo.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r13)
                android.os.Debug$MemoryInfo r2 = (android.os.Debug.MemoryInfo) r2
                goto L_0x0280
            L_0x027f:
                r2 = r7
            L_0x0280:
                int r3 = r32.readInt()
                if (r3 == 0) goto L_0x0289
                r3 = r20
                goto L_0x028a
            L_0x0289:
                r3 = r0
            L_0x028a:
                int r4 = r32.readInt()
                if (r4 == 0) goto L_0x0293
                r4 = r20
                goto L_0x0294
            L_0x0293:
                r4 = r0
            L_0x0294:
                int r5 = r32.readInt()
                if (r5 == 0) goto L_0x029d
                r5 = r20
                goto L_0x029e
            L_0x029d:
                r5 = r0
            L_0x029e:
                int r6 = r32.readInt()
                if (r6 == 0) goto L_0x02a7
                r6 = r20
                goto L_0x02a8
            L_0x02a7:
                r6 = r0
            L_0x02a8:
                int r7 = r32.readInt()
                if (r7 == 0) goto L_0x02b1
                r7 = r20
                goto L_0x02b2
            L_0x02b1:
                r7 = r0
            L_0x02b2:
                java.lang.String[] r9 = r32.createStringArray()
                r0 = r15
                r8 = r9
                r0.dumpMemInfo(r1, r2, r3, r4, r5, r6, r7, r8)
                return r20
            L_0x02bc:
                r13.enforceInterface(r12)
                int r1 = r32.readInt()
                int r2 = r32.readInt()
                if (r2 == 0) goto L_0x02cc
                r0 = r20
            L_0x02cc:
                r15.iawareTrimMemory(r1, r0)
                return r20
            L_0x02d0:
                r13.enforceInterface(r12)
                int r0 = r32.readInt()
                r15.scheduleTrimMemory(r0)
                return r20
            L_0x02db:
                r13.enforceInterface(r12)
                java.lang.String r0 = r32.readString()
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x02f2
                android.os.Parcelable$Creator<android.content.res.CompatibilityInfo> r1 = android.content.res.CompatibilityInfo.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                r7 = r1
                android.content.res.CompatibilityInfo r7 = (android.content.res.CompatibilityInfo) r7
                goto L_0x02f3
            L_0x02f2:
            L_0x02f3:
                r1 = r7
                r15.updatePackageCompatibilityInfo(r0, r1)
                return r20
            L_0x02f8:
                r13.enforceInterface(r12)
                int r0 = r32.readInt()
                if (r0 == 0) goto L_0x030b
                android.os.Parcelable$Creator r0 = android.os.Bundle.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r13)
                r7 = r0
                android.os.Bundle r7 = (android.os.Bundle) r7
                goto L_0x030c
            L_0x030b:
            L_0x030c:
                r0 = r7
                r15.setCoreSettings(r0)
                return r20
            L_0x0311:
                r13.enforceInterface(r12)
                java.lang.String r0 = r32.readString()
                java.lang.String r1 = r32.readString()
                java.lang.String r2 = r32.readString()
                int r3 = r32.readInt()
                if (r3 == 0) goto L_0x0330
                android.os.Parcelable$Creator<android.net.Uri> r3 = android.net.Uri.CREATOR
                java.lang.Object r3 = r3.createFromParcel(r13)
                r7 = r3
                android.net.Uri r7 = (android.net.Uri) r7
                goto L_0x0331
            L_0x0330:
            L_0x0331:
                r3 = r7
                r15.setHttpProxy(r0, r1, r2, r3)
                return r20
            L_0x0336:
                r13.enforceInterface(r12)
                r30.clearDnsCache()
                return r20
            L_0x033d:
                r13.enforceInterface(r12)
                int r0 = r32.readInt()
                if (r0 == 0) goto L_0x0350
                android.os.Parcelable$Creator r0 = android.os.ParcelFileDescriptor.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r13)
                r7 = r0
                android.os.ParcelFileDescriptor r7 = (android.os.ParcelFileDescriptor) r7
                goto L_0x0351
            L_0x0350:
            L_0x0351:
                r0 = r7
                android.os.IBinder r1 = r32.readStrongBinder()
                java.lang.String r2 = r32.readString()
                java.lang.String[] r3 = r32.createStringArray()
                r15.dumpActivity(r0, r1, r2, r3)
                return r20
            L_0x0362:
                r13.enforceInterface(r12)
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x036e
                r1 = r20
                goto L_0x036f
            L_0x036e:
                r1 = r0
            L_0x036f:
                int r2 = r32.readInt()
                if (r2 == 0) goto L_0x0378
                r2 = r20
                goto L_0x0379
            L_0x0378:
                r2 = r0
            L_0x0379:
                int r3 = r32.readInt()
                if (r3 == 0) goto L_0x0382
                r3 = r20
                goto L_0x0383
            L_0x0382:
                r3 = r0
            L_0x0383:
                java.lang.String r6 = r32.readString()
                int r0 = r32.readInt()
                if (r0 == 0) goto L_0x0397
                android.os.Parcelable$Creator r0 = android.os.ParcelFileDescriptor.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r13)
                android.os.ParcelFileDescriptor r0 = (android.os.ParcelFileDescriptor) r0
                r5 = r0
                goto L_0x0398
            L_0x0397:
                r5 = r7
            L_0x0398:
                r0 = r15
                r4 = r6
                r0.dumpHeap(r1, r2, r3, r4, r5)
                return r20
            L_0x039e:
                r13.enforceInterface(r12)
                java.lang.String r0 = r32.readString()
                r15.scheduleCrash(r0)
                return r20
            L_0x03a9:
                r13.enforceInterface(r12)
                int r0 = r32.readInt()
                java.lang.String[] r1 = r32.createStringArray()
                r15.dispatchPackageBroadcast(r0, r1)
                return r20
            L_0x03b8:
                r13.enforceInterface(r12)
                r30.scheduleSuicide()
                return r20
            L_0x03bf:
                r13.enforceInterface(r12)
                android.os.IBinder r0 = r32.readStrongBinder()
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x03d6
                android.os.Parcelable$Creator r1 = android.os.Bundle.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                r7 = r1
                android.os.Bundle r7 = (android.os.Bundle) r7
                goto L_0x03d7
            L_0x03d6:
            L_0x03d7:
                r1 = r7
                r15.scheduleOnNewActivityOptions(r0, r1)
                return r20
            L_0x03dc:
                r13.enforceInterface(r12)
                int r0 = r32.readInt()
                if (r0 == 0) goto L_0x03ee
                android.os.Parcelable$Creator<android.content.pm.ApplicationInfo> r0 = android.content.pm.ApplicationInfo.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r13)
                android.content.pm.ApplicationInfo r0 = (android.content.pm.ApplicationInfo) r0
                goto L_0x03ef
            L_0x03ee:
                r0 = r7
            L_0x03ef:
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x03ff
                android.os.Parcelable$Creator<android.content.res.CompatibilityInfo> r1 = android.content.res.CompatibilityInfo.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                r7 = r1
                android.content.res.CompatibilityInfo r7 = (android.content.res.CompatibilityInfo) r7
                goto L_0x0400
            L_0x03ff:
            L_0x0400:
                r1 = r7
                r15.scheduleDestroyBackupAgent(r0, r1)
                return r20
            L_0x0405:
                r13.enforceInterface(r12)
                int r0 = r32.readInt()
                if (r0 == 0) goto L_0x0417
                android.os.Parcelable$Creator<android.content.pm.ApplicationInfo> r0 = android.content.pm.ApplicationInfo.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r13)
                android.content.pm.ApplicationInfo r0 = (android.content.pm.ApplicationInfo) r0
                goto L_0x0418
            L_0x0417:
                r0 = r7
            L_0x0418:
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x0428
                android.os.Parcelable$Creator<android.content.res.CompatibilityInfo> r1 = android.content.res.CompatibilityInfo.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                r7 = r1
                android.content.res.CompatibilityInfo r7 = (android.content.res.CompatibilityInfo) r7
                goto L_0x0429
            L_0x0428:
            L_0x0429:
                r1 = r7
                int r2 = r32.readInt()
                r15.scheduleCreateBackupAgent(r0, r1, r2)
                return r20
            L_0x0432:
                r13.enforceInterface(r12)
                int r0 = r32.readInt()
                r15.setSchedulingGroup(r0)
                return r20
            L_0x043d:
                r13.enforceInterface(r12)
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x0449
                r0 = r20
            L_0x0449:
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x0459
                android.os.Parcelable$Creator<android.app.ProfilerInfo> r1 = android.app.ProfilerInfo.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                r7 = r1
                android.app.ProfilerInfo r7 = (android.app.ProfilerInfo) r7
                goto L_0x045a
            L_0x0459:
            L_0x045a:
                r1 = r7
                int r2 = r32.readInt()
                r15.profilerControl(r0, r1, r2)
                return r20
            L_0x0463:
                r13.enforceInterface(r12)
                android.os.IBinder r1 = r32.readStrongBinder()
                int r2 = r32.readInt()
                if (r2 == 0) goto L_0x0473
                r0 = r20
            L_0x0473:
                r15.scheduleSleeping(r1, r0)
                return r20
            L_0x0477:
                r13.enforceInterface(r12)
                r30.scheduleLowMemory()
                return r20
            L_0x047e:
                r13.enforceInterface(r12)
                android.os.IBinder r1 = r32.readStrongBinder()
                android.content.IIntentReceiver r10 = android.content.IIntentReceiver.Stub.asInterface(r1)
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x0499
                android.os.Parcelable$Creator<android.content.Intent> r1 = android.content.Intent.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                android.content.Intent r1 = (android.content.Intent) r1
                r2 = r1
                goto L_0x049a
            L_0x0499:
                r2 = r7
            L_0x049a:
                int r11 = r32.readInt()
                java.lang.String r16 = r32.readString()
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x04b2
                android.os.Parcelable$Creator r1 = android.os.Bundle.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                android.os.Bundle r1 = (android.os.Bundle) r1
                r5 = r1
                goto L_0x04b3
            L_0x04b2:
                r5 = r7
            L_0x04b3:
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x04bc
                r6 = r20
                goto L_0x04bd
            L_0x04bc:
                r6 = r0
            L_0x04bd:
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x04c6
                r7 = r20
                goto L_0x04c7
            L_0x04c6:
                r7 = r0
            L_0x04c7:
                int r17 = r32.readInt()
                int r18 = r32.readInt()
                r0 = r15
                r1 = r10
                r3 = r11
                r4 = r16
                r8 = r17
                r9 = r18
                r0.scheduleRegisteredReceiver(r1, r2, r3, r4, r5, r6, r7, r8, r9)
                return r20
            L_0x04dc:
                r13.enforceInterface(r12)
                int r0 = r32.readInt()
                if (r0 == 0) goto L_0x04ef
                android.os.Parcelable$Creator r0 = android.os.ParcelFileDescriptor.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r13)
                r7 = r0
                android.os.ParcelFileDescriptor r7 = (android.os.ParcelFileDescriptor) r7
                goto L_0x04f0
            L_0x04ef:
            L_0x04f0:
                r0 = r7
                android.os.IBinder r1 = r32.readStrongBinder()
                java.lang.String[] r2 = r32.createStringArray()
                r15.dumpService(r0, r1, r2)
                return r20
            L_0x04fd:
                r13.enforceInterface(r12)
                android.os.IBinder r0 = r32.readStrongBinder()
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x0514
                android.os.Parcelable$Creator<android.content.Intent> r1 = android.content.Intent.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                r7 = r1
                android.content.Intent r7 = (android.content.Intent) r7
                goto L_0x0515
            L_0x0514:
            L_0x0515:
                r1 = r7
                r15.scheduleUnbindService(r0, r1)
                return r20
            L_0x051a:
                r13.enforceInterface(r12)
                android.os.IBinder r1 = r32.readStrongBinder()
                int r2 = r32.readInt()
                if (r2 == 0) goto L_0x0531
                android.os.Parcelable$Creator<android.content.Intent> r2 = android.content.Intent.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r13)
                r7 = r2
                android.content.Intent r7 = (android.content.Intent) r7
                goto L_0x0532
            L_0x0531:
            L_0x0532:
                r2 = r7
                int r3 = r32.readInt()
                if (r3 == 0) goto L_0x053c
                r0 = r20
            L_0x053c:
                int r3 = r32.readInt()
                r15.scheduleBindService(r1, r2, r0, r3)
                return r20
            L_0x0544:
                r13.enforceInterface(r12)
                r30.processInBackground()
                return r20
            L_0x054b:
                r13.enforceInterface(r12)
                r30.updateTimeZone()
                return r20
            L_0x0552:
                r13.enforceInterface(r12)
                android.os.IBinder r0 = r32.readStrongBinder()
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x0569
                android.os.Parcelable$ClassLoaderCreator<android.content.pm.ParceledListSlice> r1 = android.content.pm.ParceledListSlice.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                r7 = r1
                android.content.pm.ParceledListSlice r7 = (android.content.pm.ParceledListSlice) r7
                goto L_0x056a
            L_0x0569:
            L_0x056a:
                r1 = r7
                r15.scheduleServiceArgs(r0, r1)
                return r20
            L_0x056f:
                r13.enforceInterface(r12)
                r30.scheduleExit()
                return r20
            L_0x0576:
                r13.enforceInterface(r12)
                java.lang.String r0 = r32.readString()
                java.lang.String[] r1 = r32.createStringArray()
                r15.runIsolatedEntryPoint(r0, r1)
                return r20
            L_0x0585:
                r13.enforceInterface(r12)
                java.lang.String r21 = r32.readString()
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x059c
                android.os.Parcelable$Creator<android.content.pm.ApplicationInfo> r1 = android.content.pm.ApplicationInfo.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                android.content.pm.ApplicationInfo r1 = (android.content.pm.ApplicationInfo) r1
                r2 = r1
                goto L_0x059d
            L_0x059c:
                r2 = r7
            L_0x059d:
                android.os.Parcelable$Creator<android.content.pm.ProviderInfo> r1 = android.content.pm.ProviderInfo.CREATOR
                java.util.ArrayList r22 = r13.createTypedArrayList(r1)
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x05b3
                android.os.Parcelable$Creator<android.content.ComponentName> r1 = android.content.ComponentName.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                android.content.ComponentName r1 = (android.content.ComponentName) r1
                r4 = r1
                goto L_0x05b4
            L_0x05b3:
                r4 = r7
            L_0x05b4:
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x05c4
                android.os.Parcelable$Creator<android.app.ProfilerInfo> r1 = android.app.ProfilerInfo.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                android.app.ProfilerInfo r1 = (android.app.ProfilerInfo) r1
                r5 = r1
                goto L_0x05c5
            L_0x05c4:
                r5 = r7
            L_0x05c5:
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x05d5
                android.os.Parcelable$Creator r1 = android.os.Bundle.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r13)
                android.os.Bundle r1 = (android.os.Bundle) r1
                r6 = r1
                goto L_0x05d6
            L_0x05d5:
                r6 = r7
            L_0x05d6:
                android.os.IBinder r1 = r32.readStrongBinder()
                android.app.IInstrumentationWatcher r23 = android.app.IInstrumentationWatcher.Stub.asInterface(r1)
                android.os.IBinder r1 = r32.readStrongBinder()
                android.app.IUiAutomationConnection r24 = android.app.IUiAutomationConnection.Stub.asInterface(r1)
                int r25 = r32.readInt()
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x05f3
                r10 = r20
                goto L_0x05f4
            L_0x05f3:
                r10 = r0
            L_0x05f4:
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x05fd
                r11 = r20
                goto L_0x05fe
            L_0x05fd:
                r11 = r0
            L_0x05fe:
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x0607
                r1 = r20
                goto L_0x0608
            L_0x0607:
                r1 = r0
            L_0x0608:
                r9 = r12
                r12 = r1
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x0613
                r1 = r20
                goto L_0x0614
            L_0x0613:
                r1 = r0
            L_0x0614:
                r8 = r13
                r13 = r1
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x0625
                android.os.Parcelable$Creator<android.content.res.Configuration> r1 = android.content.res.Configuration.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r8)
                android.content.res.Configuration r1 = (android.content.res.Configuration) r1
                goto L_0x0626
            L_0x0625:
                r1 = r7
            L_0x0626:
                r14 = r1
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x0636
                android.os.Parcelable$Creator<android.content.res.CompatibilityInfo> r1 = android.content.res.CompatibilityInfo.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r8)
                android.content.res.CompatibilityInfo r1 = (android.content.res.CompatibilityInfo) r1
                goto L_0x0637
            L_0x0636:
                r1 = r7
            L_0x0637:
                r3 = r15
                r15 = r1
                java.lang.Class r1 = r30.getClass()
                java.lang.ClassLoader r1 = r1.getClassLoader()
                java.util.HashMap r26 = r8.readHashMap(r1)
                int r16 = r32.readInt()
                if (r16 == 0) goto L_0x0656
                android.os.Parcelable$Creator r7 = android.os.Bundle.CREATOR
                java.lang.Object r7 = r7.createFromParcel(r8)
                android.os.Bundle r7 = (android.os.Bundle) r7
            L_0x0653:
                r17 = r7
                goto L_0x0657
            L_0x0656:
                goto L_0x0653
            L_0x0657:
                java.lang.String r27 = r32.readString()
                int r7 = r32.readInt()
                if (r7 == 0) goto L_0x0664
                r19 = r20
                goto L_0x0666
            L_0x0664:
                r19 = r0
            L_0x0666:
                r0 = r3
                r28 = r1
                r1 = r21
                r7 = r3
                r3 = r22
                r7 = r23
                r8 = r24
                r29 = r9
                r9 = r25
                r16 = r26
                r18 = r27
                r0.bindApplication(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19)
                return r20
            L_0x067e:
                r29 = r12
                r11 = r29
                r10 = r32
                r10.enforceInterface(r11)
                android.os.IBinder r0 = r32.readStrongBinder()
                r12 = r30
                r12.scheduleStopService(r0)
                return r20
            L_0x0691:
                r11 = r12
                r10 = r13
                r12 = r15
                r10.enforceInterface(r11)
                android.os.IBinder r0 = r32.readStrongBinder()
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x06aa
                android.os.Parcelable$Creator<android.content.pm.ServiceInfo> r1 = android.content.pm.ServiceInfo.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r10)
                android.content.pm.ServiceInfo r1 = (android.content.pm.ServiceInfo) r1
                goto L_0x06ab
            L_0x06aa:
                r1 = r7
            L_0x06ab:
                int r2 = r32.readInt()
                if (r2 == 0) goto L_0x06bb
                android.os.Parcelable$Creator<android.content.res.CompatibilityInfo> r2 = android.content.res.CompatibilityInfo.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r10)
                r7 = r2
                android.content.res.CompatibilityInfo r7 = (android.content.res.CompatibilityInfo) r7
                goto L_0x06bc
            L_0x06bb:
            L_0x06bc:
                r2 = r7
                int r3 = r32.readInt()
                r12.scheduleCreateService(r0, r1, r2, r3)
                return r20
            L_0x06c5:
                r11 = r12
                r10 = r13
                r12 = r15
                r10.enforceInterface(r11)
                int r1 = r32.readInt()
                if (r1 == 0) goto L_0x06da
                android.os.Parcelable$Creator<android.content.Intent> r1 = android.content.Intent.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r10)
                android.content.Intent r1 = (android.content.Intent) r1
                goto L_0x06db
            L_0x06da:
                r1 = r7
            L_0x06db:
                int r2 = r32.readInt()
                if (r2 == 0) goto L_0x06ea
                android.os.Parcelable$Creator<android.content.pm.ActivityInfo> r2 = android.content.pm.ActivityInfo.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r10)
                android.content.pm.ActivityInfo r2 = (android.content.pm.ActivityInfo) r2
                goto L_0x06eb
            L_0x06ea:
                r2 = r7
            L_0x06eb:
                int r3 = r32.readInt()
                if (r3 == 0) goto L_0x06fa
                android.os.Parcelable$Creator<android.content.res.CompatibilityInfo> r3 = android.content.res.CompatibilityInfo.CREATOR
                java.lang.Object r3 = r3.createFromParcel(r10)
                android.content.res.CompatibilityInfo r3 = (android.content.res.CompatibilityInfo) r3
                goto L_0x06fb
            L_0x06fa:
                r3 = r7
            L_0x06fb:
                int r13 = r32.readInt()
                java.lang.String r14 = r32.readString()
                int r4 = r32.readInt()
                if (r4 == 0) goto L_0x0713
                android.os.Parcelable$Creator r4 = android.os.Bundle.CREATOR
                java.lang.Object r4 = r4.createFromParcel(r10)
                android.os.Bundle r4 = (android.os.Bundle) r4
                r6 = r4
                goto L_0x0714
            L_0x0713:
                r6 = r7
            L_0x0714:
                int r4 = r32.readInt()
                if (r4 == 0) goto L_0x071d
                r7 = r20
                goto L_0x071e
            L_0x071d:
                r7 = r0
            L_0x071e:
                int r15 = r32.readInt()
                int r16 = r32.readInt()
                r0 = r12
                r4 = r13
                r5 = r14
                r8 = r15
                r9 = r16
                r0.scheduleReceiver(r1, r2, r3, r4, r5, r6, r7, r8, r9)
                return r20
            L_0x0730:
                r11 = r12
                r10 = r13
                r12 = r15
                r0 = r33
                r0.writeString(r11)
                return r20
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.IApplicationThread.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }
    }

    void attachAgent(String str) throws RemoteException;

    void bindApplication(String str, ApplicationInfo applicationInfo, List<ProviderInfo> list, ComponentName componentName, ProfilerInfo profilerInfo, Bundle bundle, IInstrumentationWatcher iInstrumentationWatcher, IUiAutomationConnection iUiAutomationConnection, int i, boolean z, boolean z2, boolean z3, boolean z4, Configuration configuration, CompatibilityInfo compatibilityInfo, Map map, Bundle bundle2, String str2, boolean z5) throws RemoteException;

    void clearDnsCache() throws RemoteException;

    void dispatchPackageBroadcast(int i, String[] strArr) throws RemoteException;

    void dumpActivity(ParcelFileDescriptor parcelFileDescriptor, IBinder iBinder, String str, String[] strArr) throws RemoteException;

    void dumpDbInfo(ParcelFileDescriptor parcelFileDescriptor, String[] strArr) throws RemoteException;

    void dumpGfxInfo(ParcelFileDescriptor parcelFileDescriptor, String[] strArr) throws RemoteException;

    void dumpHeap(boolean z, boolean z2, boolean z3, String str, ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    void dumpMemInfo(ParcelFileDescriptor parcelFileDescriptor, Debug.MemoryInfo memoryInfo, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, String[] strArr) throws RemoteException;

    void dumpMemInfoProto(ParcelFileDescriptor parcelFileDescriptor, Debug.MemoryInfo memoryInfo, boolean z, boolean z2, boolean z3, boolean z4, String[] strArr) throws RemoteException;

    void dumpProvider(ParcelFileDescriptor parcelFileDescriptor, IBinder iBinder, String[] strArr) throws RemoteException;

    void dumpService(ParcelFileDescriptor parcelFileDescriptor, IBinder iBinder, String[] strArr) throws RemoteException;

    void handleTrustStorageUpdate() throws RemoteException;

    void iawareTrimMemory(int i, boolean z) throws RemoteException;

    void notifyCleartextNetwork(byte[] bArr) throws RemoteException;

    void processInBackground() throws RemoteException;

    void profilerControl(boolean z, ProfilerInfo profilerInfo, int i) throws RemoteException;

    void requestAssistContextExtras(IBinder iBinder, IBinder iBinder2, int i, int i2, int i3) throws RemoteException;

    void requestContentNode(IBinder iBinder, Bundle bundle, int i) throws RemoteException;

    void requestContentOther(IBinder iBinder, Bundle bundle, int i) throws RemoteException;

    void runIsolatedEntryPoint(String str, String[] strArr) throws RemoteException;

    void scheduleApplicationInfoChanged(ApplicationInfo applicationInfo) throws RemoteException;

    void scheduleApplicationThemeInfoChanged(ApplicationInfo applicationInfo, boolean z) throws RemoteException;

    void scheduleBindService(IBinder iBinder, Intent intent, boolean z, int i) throws RemoteException;

    void scheduleCrash(String str) throws RemoteException;

    void scheduleCreateBackupAgent(ApplicationInfo applicationInfo, CompatibilityInfo compatibilityInfo, int i) throws RemoteException;

    void scheduleCreateService(IBinder iBinder, ServiceInfo serviceInfo, CompatibilityInfo compatibilityInfo, int i) throws RemoteException;

    void scheduleDestroyBackupAgent(ApplicationInfo applicationInfo, CompatibilityInfo compatibilityInfo) throws RemoteException;

    void scheduleEnterAnimationComplete(IBinder iBinder) throws RemoteException;

    void scheduleExit() throws RemoteException;

    void scheduleFreeFormOutLineChanged(IBinder iBinder, int i) throws RemoteException;

    void scheduleInstallProvider(ProviderInfo providerInfo) throws RemoteException;

    void scheduleLocalVoiceInteractionStarted(IBinder iBinder, IVoiceInteractor iVoiceInteractor) throws RemoteException;

    void scheduleLowMemory() throws RemoteException;

    void scheduleOnNewActivityOptions(IBinder iBinder, Bundle bundle) throws RemoteException;

    void schedulePCWindowStateChanged(IBinder iBinder, int i) throws RemoteException;

    void scheduleReceiver(Intent intent, ActivityInfo activityInfo, CompatibilityInfo compatibilityInfo, int i, String str, Bundle bundle, boolean z, int i2, int i3) throws RemoteException;

    void scheduleRegisteredReceiver(IIntentReceiver iIntentReceiver, Intent intent, int i, String str, Bundle bundle, boolean z, boolean z2, int i2, int i3) throws RemoteException;

    void scheduleRestoreFreeFormConfig(IBinder iBinder) throws RemoteException;

    void scheduleServiceArgs(IBinder iBinder, ParceledListSlice parceledListSlice) throws RemoteException;

    void scheduleSleeping(IBinder iBinder, boolean z) throws RemoteException;

    void scheduleStopService(IBinder iBinder) throws RemoteException;

    void scheduleSuicide() throws RemoteException;

    void scheduleTransaction(ClientTransaction clientTransaction) throws RemoteException;

    void scheduleTranslucentConversionComplete(IBinder iBinder, boolean z) throws RemoteException;

    void scheduleTrimMemory(int i) throws RemoteException;

    void scheduleUnbindService(IBinder iBinder, Intent intent) throws RemoteException;

    void setCoreSettings(Bundle bundle) throws RemoteException;

    void setHttpProxy(String str, String str2, String str3, Uri uri) throws RemoteException;

    void setNetworkBlockSeq(long j) throws RemoteException;

    void setProcessState(int i) throws RemoteException;

    void setSchedulingGroup(int i) throws RemoteException;

    void startBinderTracking() throws RemoteException;

    void stopBinderTrackingAndDump(ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    void unstableProviderDied(IBinder iBinder) throws RemoteException;

    void updatePackageCompatibilityInfo(String str, CompatibilityInfo compatibilityInfo) throws RemoteException;

    void updateTimePrefs(int i) throws RemoteException;

    void updateTimeZone() throws RemoteException;
}

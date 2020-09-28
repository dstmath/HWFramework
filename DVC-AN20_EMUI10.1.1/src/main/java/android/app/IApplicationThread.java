package android.app;

import android.annotation.UnsupportedAppUsage;
import android.app.IInstrumentationWatcher;
import android.app.IUiAutomationConnection;
import android.app.servertransaction.ClientTransaction;
import android.content.AutofillOptions;
import android.content.ComponentName;
import android.content.ContentCaptureOptions;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ParceledListSlice;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallback;
import android.os.RemoteException;
import com.android.internal.app.IVoiceInteractor;
import java.util.List;
import java.util.Map;

public interface IApplicationThread extends IInterface {
    void attachAgent(String str) throws RemoteException;

    void bindApplication(String str, ApplicationInfo applicationInfo, List<ProviderInfo> list, ComponentName componentName, ProfilerInfo profilerInfo, Bundle bundle, IInstrumentationWatcher iInstrumentationWatcher, IUiAutomationConnection iUiAutomationConnection, int i, boolean z, boolean z2, boolean z3, boolean z4, Configuration configuration, CompatibilityInfo compatibilityInfo, Map map, Bundle bundle2, String str2, AutofillOptions autofillOptions, ContentCaptureOptions contentCaptureOptions) throws RemoteException;

    void clearDnsCache() throws RemoteException;

    void dispatchPackageBroadcast(int i, String[] strArr) throws RemoteException;

    void dumpActivity(ParcelFileDescriptor parcelFileDescriptor, IBinder iBinder, String str, String[] strArr) throws RemoteException;

    void dumpDbInfo(ParcelFileDescriptor parcelFileDescriptor, String[] strArr) throws RemoteException;

    void dumpGfxInfo(ParcelFileDescriptor parcelFileDescriptor, String[] strArr) throws RemoteException;

    void dumpHeap(boolean z, boolean z2, boolean z3, String str, ParcelFileDescriptor parcelFileDescriptor, RemoteCallback remoteCallback) throws RemoteException;

    void dumpMemInfo(ParcelFileDescriptor parcelFileDescriptor, Debug.MemoryInfo memoryInfo, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, String[] strArr) throws RemoteException;

    void dumpMemInfoProto(ParcelFileDescriptor parcelFileDescriptor, Debug.MemoryInfo memoryInfo, boolean z, boolean z2, boolean z3, boolean z4, String[] strArr) throws RemoteException;

    void dumpProvider(ParcelFileDescriptor parcelFileDescriptor, IBinder iBinder, String[] strArr) throws RemoteException;

    void dumpService(ParcelFileDescriptor parcelFileDescriptor, IBinder iBinder, String[] strArr) throws RemoteException;

    void handleTrustStorageUpdate() throws RemoteException;

    void iawareTrimMemory(int i, boolean z) throws RemoteException;

    void notifyAppToTop(int i) throws RemoteException;

    void notifyCleartextNetwork(byte[] bArr) throws RemoteException;

    void notifyHwMultiWindowCaption(IBinder iBinder, Bundle bundle) throws RemoteException;

    void performDirectAction(IBinder iBinder, String str, Bundle bundle, RemoteCallback remoteCallback, RemoteCallback remoteCallback2) throws RemoteException;

    void preLoadClass(ApplicationInfo applicationInfo, CompatibilityInfo compatibilityInfo) throws RemoteException;

    void processInBackground() throws RemoteException;

    void profilerControl(boolean z, ProfilerInfo profilerInfo, int i) throws RemoteException;

    void requestAssistContextExtras(IBinder iBinder, IBinder iBinder2, int i, int i2, int i3) throws RemoteException;

    void requestContentNode(IBinder iBinder, Bundle bundle, int i) throws RemoteException;

    void requestContentOther(IBinder iBinder, Bundle bundle, int i) throws RemoteException;

    void requestDirectActions(IBinder iBinder, IVoiceInteractor iVoiceInteractor, RemoteCallback remoteCallback, RemoteCallback remoteCallback2) throws RemoteException;

    void runIsolatedEntryPoint(String str, String[] strArr) throws RemoteException;

    void scheduleApplicationInfoChanged(ApplicationInfo applicationInfo) throws RemoteException;

    void scheduleApplicationThemeInfoChanged(ApplicationInfo applicationInfo, boolean z) throws RemoteException;

    @UnsupportedAppUsage
    void scheduleBindService(IBinder iBinder, Intent intent, boolean z, int i) throws RemoteException;

    void scheduleCrash(String str) throws RemoteException;

    void scheduleCreateBackupAgent(ApplicationInfo applicationInfo, CompatibilityInfo compatibilityInfo, int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    void scheduleCreateService(IBinder iBinder, ServiceInfo serviceInfo, CompatibilityInfo compatibilityInfo, int i) throws RemoteException;

    void scheduleDestroyBackupAgent(ApplicationInfo applicationInfo, CompatibilityInfo compatibilityInfo, int i) throws RemoteException;

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

    @UnsupportedAppUsage
    void scheduleStopService(IBinder iBinder) throws RemoteException;

    void scheduleSuicide() throws RemoteException;

    void scheduleTransaction(ClientTransaction clientTransaction) throws RemoteException;

    void scheduleTranslucentConversionComplete(IBinder iBinder, boolean z) throws RemoteException;

    @UnsupportedAppUsage
    void scheduleTrimMemory(int i) throws RemoteException;

    @UnsupportedAppUsage
    void scheduleUnbindService(IBinder iBinder, Intent intent) throws RemoteException;

    void setCoreSettings(Bundle bundle) throws RemoteException;

    void setNetworkBlockSeq(long j) throws RemoteException;

    void setProcessState(int i) throws RemoteException;

    void setSchedulingGroup(int i) throws RemoteException;

    void startBinderTracking() throws RemoteException;

    void stopBinderTrackingAndDump(ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    void unstableProviderDied(IBinder iBinder) throws RemoteException;

    void updateHttpProxy() throws RemoteException;

    void updatePackageCompatibilityInfo(String str, CompatibilityInfo compatibilityInfo) throws RemoteException;

    void updateTimePrefs(int i) throws RemoteException;

    void updateTimeZone() throws RemoteException;

    public static class Default implements IApplicationThread {
        @Override // android.app.IApplicationThread
        public void scheduleReceiver(Intent intent, ActivityInfo info, CompatibilityInfo compatInfo, int resultCode, String data, Bundle extras, boolean sync, int sendingUser, int processState) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleCreateService(IBinder token, ServiceInfo info, CompatibilityInfo compatInfo, int processState) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleStopService(IBinder token) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void bindApplication(String packageName, ApplicationInfo info, List<ProviderInfo> list, ComponentName testName, ProfilerInfo profilerInfo, Bundle testArguments, IInstrumentationWatcher testWatcher, IUiAutomationConnection uiAutomationConnection, int debugMode, boolean enableBinderTracking, boolean trackAllocation, boolean restrictedBackupMode, boolean persistent, Configuration config, CompatibilityInfo compatInfo, Map services, Bundle coreSettings, String buildSerial, AutofillOptions autofillOptions, ContentCaptureOptions contentCaptureOptions) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void runIsolatedEntryPoint(String entryPoint, String[] entryPointArgs) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleExit() throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleServiceArgs(IBinder token, ParceledListSlice args) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void updateTimeZone() throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void processInBackground() throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleBindService(IBinder token, Intent intent, boolean rebind, int processState) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleUnbindService(IBinder token, Intent intent) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void dumpService(ParcelFileDescriptor fd, IBinder servicetoken, String[] args) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleRegisteredReceiver(IIntentReceiver receiver, Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser, int processState) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleLowMemory() throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleSleeping(IBinder token, boolean sleeping) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void profilerControl(boolean start, ProfilerInfo profilerInfo, int profileType) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void setSchedulingGroup(int group) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleCreateBackupAgent(ApplicationInfo app, CompatibilityInfo compatInfo, int backupMode, int userId) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleDestroyBackupAgent(ApplicationInfo app, CompatibilityInfo compatInfo, int userId) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleOnNewActivityOptions(IBinder token, Bundle options) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleSuicide() throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void dispatchPackageBroadcast(int cmd, String[] packages) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleCrash(String msg) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void dumpHeap(boolean managed, boolean mallocInfo, boolean runGc, String path, ParcelFileDescriptor fd, RemoteCallback finishCallback) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void dumpActivity(ParcelFileDescriptor fd, IBinder servicetoken, String prefix, String[] args) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void clearDnsCache() throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void updateHttpProxy() throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void setCoreSettings(Bundle coreSettings) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void updatePackageCompatibilityInfo(String pkg, CompatibilityInfo info) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleTrimMemory(int level) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void iawareTrimMemory(int level, boolean fromIAware) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void dumpMemInfo(ParcelFileDescriptor fd, Debug.MemoryInfo mem, boolean checkin, boolean dumpInfo, boolean dumpDalvik, boolean dumpSummaryOnly, boolean dumpUnreachable, String[] args) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void dumpMemInfoProto(ParcelFileDescriptor fd, Debug.MemoryInfo mem, boolean dumpInfo, boolean dumpDalvik, boolean dumpSummaryOnly, boolean dumpUnreachable, String[] args) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void dumpGfxInfo(ParcelFileDescriptor fd, String[] args) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void dumpProvider(ParcelFileDescriptor fd, IBinder servicetoken, String[] args) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void dumpDbInfo(ParcelFileDescriptor fd, String[] args) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void unstableProviderDied(IBinder provider) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void requestAssistContextExtras(IBinder activityToken, IBinder requestToken, int requestType, int sessionId, int flags) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleTranslucentConversionComplete(IBinder token, boolean timeout) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void setProcessState(int state) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleInstallProvider(ProviderInfo provider) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void updateTimePrefs(int timeFormatPreference) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleEnterAnimationComplete(IBinder token) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void notifyCleartextNetwork(byte[] firstPacket) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void startBinderTracking() throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void stopBinderTrackingAndDump(ParcelFileDescriptor fd) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleLocalVoiceInteractionStarted(IBinder token, IVoiceInteractor voiceInteractor) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void handleTrustStorageUpdate() throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void attachAgent(String path) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleApplicationInfoChanged(ApplicationInfo ai) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleApplicationThemeInfoChanged(ApplicationInfo ai, boolean fromThemeChange) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void setNetworkBlockSeq(long procStateSeq) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleTransaction(ClientTransaction transaction) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void schedulePCWindowStateChanged(IBinder token, int windowState) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void requestContentNode(IBinder appToken, Bundle data, int token) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void requestContentOther(IBinder appToken, Bundle data, int token) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleFreeFormOutLineChanged(IBinder token, int touchingState) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void scheduleRestoreFreeFormConfig(IBinder token) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void requestDirectActions(IBinder activityToken, IVoiceInteractor intractor, RemoteCallback cancellationCallback, RemoteCallback callback) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void performDirectAction(IBinder activityToken, String actionId, Bundle arguments, RemoteCallback cancellationCallback, RemoteCallback resultCallback) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void notifyAppToTop(int enable) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void notifyHwMultiWindowCaption(IBinder token, Bundle info) throws RemoteException {
        }

        @Override // android.app.IApplicationThread
        public void preLoadClass(ApplicationInfo info, CompatibilityInfo compatInfo) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

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
        static final int TRANSACTION_notifyAppToTop = 61;
        static final int TRANSACTION_notifyCleartextNetwork = 44;
        static final int TRANSACTION_notifyHwMultiWindowCaption = 62;
        static final int TRANSACTION_performDirectAction = 60;
        static final int TRANSACTION_preLoadClass = 63;
        static final int TRANSACTION_processInBackground = 9;
        static final int TRANSACTION_profilerControl = 16;
        static final int TRANSACTION_requestAssistContextExtras = 38;
        static final int TRANSACTION_requestContentNode = 55;
        static final int TRANSACTION_requestContentOther = 56;
        static final int TRANSACTION_requestDirectActions = 59;
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
        static final int TRANSACTION_setNetworkBlockSeq = 52;
        static final int TRANSACTION_setProcessState = 40;
        static final int TRANSACTION_setSchedulingGroup = 17;
        static final int TRANSACTION_startBinderTracking = 45;
        static final int TRANSACTION_stopBinderTrackingAndDump = 46;
        static final int TRANSACTION_unstableProviderDied = 37;
        static final int TRANSACTION_updateHttpProxy = 27;
        static final int TRANSACTION_updatePackageCompatibilityInfo = 29;
        static final int TRANSACTION_updateTimePrefs = 42;
        static final int TRANSACTION_updateTimeZone = 8;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "scheduleReceiver";
                case 2:
                    return "scheduleCreateService";
                case 3:
                    return "scheduleStopService";
                case 4:
                    return "bindApplication";
                case 5:
                    return "runIsolatedEntryPoint";
                case 6:
                    return "scheduleExit";
                case 7:
                    return "scheduleServiceArgs";
                case 8:
                    return "updateTimeZone";
                case 9:
                    return "processInBackground";
                case 10:
                    return "scheduleBindService";
                case 11:
                    return "scheduleUnbindService";
                case 12:
                    return "dumpService";
                case 13:
                    return "scheduleRegisteredReceiver";
                case 14:
                    return "scheduleLowMemory";
                case 15:
                    return "scheduleSleeping";
                case 16:
                    return "profilerControl";
                case 17:
                    return "setSchedulingGroup";
                case 18:
                    return "scheduleCreateBackupAgent";
                case 19:
                    return "scheduleDestroyBackupAgent";
                case 20:
                    return "scheduleOnNewActivityOptions";
                case 21:
                    return "scheduleSuicide";
                case 22:
                    return "dispatchPackageBroadcast";
                case 23:
                    return "scheduleCrash";
                case 24:
                    return "dumpHeap";
                case 25:
                    return "dumpActivity";
                case 26:
                    return "clearDnsCache";
                case 27:
                    return "updateHttpProxy";
                case 28:
                    return "setCoreSettings";
                case 29:
                    return "updatePackageCompatibilityInfo";
                case 30:
                    return "scheduleTrimMemory";
                case 31:
                    return "iawareTrimMemory";
                case 32:
                    return "dumpMemInfo";
                case 33:
                    return "dumpMemInfoProto";
                case 34:
                    return "dumpGfxInfo";
                case 35:
                    return "dumpProvider";
                case 36:
                    return "dumpDbInfo";
                case 37:
                    return "unstableProviderDied";
                case 38:
                    return "requestAssistContextExtras";
                case 39:
                    return "scheduleTranslucentConversionComplete";
                case 40:
                    return "setProcessState";
                case 41:
                    return "scheduleInstallProvider";
                case 42:
                    return "updateTimePrefs";
                case 43:
                    return "scheduleEnterAnimationComplete";
                case 44:
                    return "notifyCleartextNetwork";
                case 45:
                    return "startBinderTracking";
                case 46:
                    return "stopBinderTrackingAndDump";
                case 47:
                    return "scheduleLocalVoiceInteractionStarted";
                case 48:
                    return "handleTrustStorageUpdate";
                case 49:
                    return "attachAgent";
                case 50:
                    return "scheduleApplicationInfoChanged";
                case 51:
                    return "scheduleApplicationThemeInfoChanged";
                case 52:
                    return "setNetworkBlockSeq";
                case 53:
                    return "scheduleTransaction";
                case 54:
                    return "schedulePCWindowStateChanged";
                case 55:
                    return "requestContentNode";
                case 56:
                    return "requestContentOther";
                case 57:
                    return "scheduleFreeFormOutLineChanged";
                case 58:
                    return "scheduleRestoreFreeFormConfig";
                case 59:
                    return "requestDirectActions";
                case 60:
                    return "performDirectAction";
                case 61:
                    return "notifyAppToTop";
                case 62:
                    return "notifyHwMultiWindowCaption";
                case 63:
                    return "preLoadClass";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Intent _arg0;
            ActivityInfo _arg1;
            CompatibilityInfo _arg2;
            Bundle _arg5;
            ServiceInfo _arg12;
            CompatibilityInfo _arg22;
            ApplicationInfo _arg13;
            ComponentName _arg3;
            ProfilerInfo _arg4;
            Bundle _arg52;
            Configuration _arg132;
            CompatibilityInfo _arg14;
            Bundle _arg16;
            AutofillOptions _arg18;
            ContentCaptureOptions _arg19;
            ParceledListSlice _arg15;
            Intent _arg17;
            Intent _arg110;
            ParcelFileDescriptor _arg02;
            Intent _arg111;
            Bundle _arg42;
            ProfilerInfo _arg112;
            ApplicationInfo _arg03;
            CompatibilityInfo _arg113;
            ApplicationInfo _arg04;
            CompatibilityInfo _arg114;
            Bundle _arg115;
            ParcelFileDescriptor _arg43;
            RemoteCallback _arg53;
            ParcelFileDescriptor _arg05;
            Bundle _arg06;
            CompatibilityInfo _arg116;
            ParcelFileDescriptor _arg07;
            Debug.MemoryInfo _arg117;
            ParcelFileDescriptor _arg08;
            Debug.MemoryInfo _arg118;
            ParcelFileDescriptor _arg09;
            ParcelFileDescriptor _arg010;
            ParcelFileDescriptor _arg011;
            ProviderInfo _arg012;
            ParcelFileDescriptor _arg013;
            ApplicationInfo _arg014;
            ApplicationInfo _arg015;
            ClientTransaction _arg016;
            Bundle _arg119;
            Bundle _arg120;
            RemoteCallback _arg23;
            RemoteCallback _arg32;
            Bundle _arg24;
            RemoteCallback _arg33;
            RemoteCallback _arg44;
            Bundle _arg121;
            ApplicationInfo _arg017;
            CompatibilityInfo _arg122;
            if (code != 1598968902) {
                boolean _arg123 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = ActivityInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = CompatibilityInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        int _arg34 = data.readInt();
                        String _arg45 = data.readString();
                        if (data.readInt() != 0) {
                            _arg5 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg5 = null;
                        }
                        scheduleReceiver(_arg0, _arg1, _arg2, _arg34, _arg45, _arg5, data.readInt() != 0, data.readInt(), data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg018 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg12 = ServiceInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = CompatibilityInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        scheduleCreateService(_arg018, _arg12, _arg22, data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        scheduleStopService(data.readStrongBinder());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg019 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = ApplicationInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        List<ProviderInfo> _arg25 = data.createTypedArrayList(ProviderInfo.CREATOR);
                        if (data.readInt() != 0) {
                            _arg3 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg4 = ProfilerInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg52 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg52 = null;
                        }
                        IInstrumentationWatcher _arg6 = IInstrumentationWatcher.Stub.asInterface(data.readStrongBinder());
                        IUiAutomationConnection _arg7 = IUiAutomationConnection.Stub.asInterface(data.readStrongBinder());
                        int _arg8 = data.readInt();
                        boolean _arg9 = data.readInt() != 0;
                        boolean _arg10 = data.readInt() != 0;
                        boolean _arg11 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg123 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg132 = Configuration.CREATOR.createFromParcel(data);
                        } else {
                            _arg132 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = CompatibilityInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        Map _arg152 = data.readHashMap(getClass().getClassLoader());
                        if (data.readInt() != 0) {
                            _arg16 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg16 = null;
                        }
                        String _arg172 = data.readString();
                        if (data.readInt() != 0) {
                            _arg18 = AutofillOptions.CREATOR.createFromParcel(data);
                        } else {
                            _arg18 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg19 = ContentCaptureOptions.CREATOR.createFromParcel(data);
                        } else {
                            _arg19 = null;
                        }
                        bindApplication(_arg019, _arg13, _arg25, _arg3, _arg4, _arg52, _arg6, _arg7, _arg8, _arg9, _arg10, _arg11, _arg123, _arg132, _arg14, _arg152, _arg16, _arg172, _arg18, _arg19);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        runIsolatedEntryPoint(data.readString(), data.createStringArray());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        scheduleExit();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg020 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg15 = ParceledListSlice.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        scheduleServiceArgs(_arg020, _arg15);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        updateTimeZone();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        processInBackground();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg021 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg17 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg17 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg123 = true;
                        }
                        scheduleBindService(_arg021, _arg17, _arg123, data.readInt());
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg022 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg110 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg110 = null;
                        }
                        scheduleUnbindService(_arg022, _arg110);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        dumpService(_arg02, data.readStrongBinder(), data.createStringArray());
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        IIntentReceiver _arg023 = IIntentReceiver.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg111 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg111 = null;
                        }
                        int _arg26 = data.readInt();
                        String _arg35 = data.readString();
                        if (data.readInt() != 0) {
                            _arg42 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg42 = null;
                        }
                        scheduleRegisteredReceiver(_arg023, _arg111, _arg26, _arg35, _arg42, data.readInt() != 0, data.readInt() != 0, data.readInt(), data.readInt());
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        scheduleLowMemory();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg024 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg123 = true;
                        }
                        scheduleSleeping(_arg024, _arg123);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg123 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg112 = ProfilerInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg112 = null;
                        }
                        profilerControl(_arg123, _arg112, data.readInt());
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        setSchedulingGroup(data.readInt());
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = ApplicationInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg113 = CompatibilityInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg113 = null;
                        }
                        scheduleCreateBackupAgent(_arg03, _arg113, data.readInt(), data.readInt());
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = ApplicationInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg114 = CompatibilityInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg114 = null;
                        }
                        scheduleDestroyBackupAgent(_arg04, _arg114, data.readInt());
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg025 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg115 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg115 = null;
                        }
                        scheduleOnNewActivityOptions(_arg025, _arg115);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        scheduleSuicide();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        dispatchPackageBroadcast(data.readInt(), data.createStringArray());
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        scheduleCrash(data.readString());
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _arg026 = data.readInt() != 0;
                        boolean _arg124 = data.readInt() != 0;
                        boolean _arg27 = data.readInt() != 0;
                        String _arg36 = data.readString();
                        if (data.readInt() != 0) {
                            _arg43 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg43 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg53 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg53 = null;
                        }
                        dumpHeap(_arg026, _arg124, _arg27, _arg36, _arg43, _arg53);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        dumpActivity(_arg05, data.readStrongBinder(), data.readString(), data.createStringArray());
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        clearDnsCache();
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        updateHttpProxy();
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        setCoreSettings(_arg06);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg027 = data.readString();
                        if (data.readInt() != 0) {
                            _arg116 = CompatibilityInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg116 = null;
                        }
                        updatePackageCompatibilityInfo(_arg027, _arg116);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        scheduleTrimMemory(data.readInt());
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg028 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg123 = true;
                        }
                        iawareTrimMemory(_arg028, _arg123);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg117 = Debug.MemoryInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg117 = null;
                        }
                        dumpMemInfo(_arg07, _arg117, data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, data.createStringArray());
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg08 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg118 = Debug.MemoryInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg118 = null;
                        }
                        dumpMemInfoProto(_arg08, _arg118, data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, data.createStringArray());
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg09 = null;
                        }
                        dumpGfxInfo(_arg09, data.createStringArray());
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg010 = null;
                        }
                        dumpProvider(_arg010, data.readStrongBinder(), data.createStringArray());
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg011 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg011 = null;
                        }
                        dumpDbInfo(_arg011, data.createStringArray());
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        unstableProviderDied(data.readStrongBinder());
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        requestAssistContextExtras(data.readStrongBinder(), data.readStrongBinder(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg029 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg123 = true;
                        }
                        scheduleTranslucentConversionComplete(_arg029, _arg123);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        setProcessState(data.readInt());
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg012 = ProviderInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg012 = null;
                        }
                        scheduleInstallProvider(_arg012);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        updateTimePrefs(data.readInt());
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        scheduleEnterAnimationComplete(data.readStrongBinder());
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        notifyCleartextNetwork(data.createByteArray());
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        startBinderTracking();
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg013 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg013 = null;
                        }
                        stopBinderTrackingAndDump(_arg013);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        scheduleLocalVoiceInteractionStarted(data.readStrongBinder(), IVoiceInteractor.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        handleTrustStorageUpdate();
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        attachAgent(data.readString());
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg014 = ApplicationInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg014 = null;
                        }
                        scheduleApplicationInfoChanged(_arg014);
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg015 = ApplicationInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg015 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg123 = true;
                        }
                        scheduleApplicationThemeInfoChanged(_arg015, _arg123);
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        setNetworkBlockSeq(data.readLong());
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg016 = ClientTransaction.CREATOR.createFromParcel(data);
                        } else {
                            _arg016 = null;
                        }
                        scheduleTransaction(_arg016);
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        schedulePCWindowStateChanged(data.readStrongBinder(), data.readInt());
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg030 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg119 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg119 = null;
                        }
                        requestContentNode(_arg030, _arg119, data.readInt());
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg031 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg120 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg120 = null;
                        }
                        requestContentOther(_arg031, _arg120, data.readInt());
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        scheduleFreeFormOutLineChanged(data.readStrongBinder(), data.readInt());
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        scheduleRestoreFreeFormConfig(data.readStrongBinder());
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg032 = data.readStrongBinder();
                        IVoiceInteractor _arg125 = IVoiceInteractor.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg23 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg32 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        requestDirectActions(_arg032, _arg125, _arg23, _arg32);
                        return true;
                    case 60:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg033 = data.readStrongBinder();
                        String _arg126 = data.readString();
                        if (data.readInt() != 0) {
                            _arg24 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg33 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg33 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg44 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg44 = null;
                        }
                        performDirectAction(_arg033, _arg126, _arg24, _arg33, _arg44);
                        return true;
                    case 61:
                        data.enforceInterface(DESCRIPTOR);
                        notifyAppToTop(data.readInt());
                        return true;
                    case 62:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg034 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg121 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg121 = null;
                        }
                        notifyHwMultiWindowCaption(_arg034, _arg121);
                        return true;
                    case 63:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg017 = ApplicationInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg017 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg122 = CompatibilityInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg122 = null;
                        }
                        preLoadClass(_arg017, _arg122);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IApplicationThread {
            public static IApplicationThread sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.app.IApplicationThread
            public void scheduleReceiver(Intent intent, ActivityInfo info, CompatibilityInfo compatInfo, int resultCode, String data, Bundle extras, boolean sync, int sendingUser, int processState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 0;
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
                    if (sync) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(sendingUser);
                    _data.writeInt(processState);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleReceiver(intent, info, compatInfo, resultCode, data, extras, sync, sendingUser, processState);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
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
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleCreateService(token, info, compatInfo, processState);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void scheduleStopService(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleStopService(token);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void bindApplication(String packageName, ApplicationInfo info, List<ProviderInfo> providers, ComponentName testName, ProfilerInfo profilerInfo, Bundle testArguments, IInstrumentationWatcher testWatcher, IUiAutomationConnection uiAutomationConnection, int debugMode, boolean enableBinderTracking, boolean trackAllocation, boolean restrictedBackupMode, boolean persistent, Configuration config, CompatibilityInfo compatInfo, Map services, Bundle coreSettings, String buildSerial, AutofillOptions autofillOptions, ContentCaptureOptions contentCaptureOptions) throws RemoteException {
                Parcel _data;
                int i;
                Parcel _data2 = Parcel.obtain();
                try {
                    _data2.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data2.writeString(packageName);
                    if (info != null) {
                        try {
                            _data2.writeInt(1);
                            info.writeToParcel(_data2, 0);
                        } catch (Throwable th) {
                            th = th;
                            _data = _data2;
                        }
                    } else {
                        _data2.writeInt(0);
                    }
                    _data2.writeTypedList(providers);
                    if (testName != null) {
                        _data2.writeInt(1);
                        testName.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    if (profilerInfo != null) {
                        _data2.writeInt(1);
                        profilerInfo.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    if (testArguments != null) {
                        _data2.writeInt(1);
                        testArguments.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    _data2.writeStrongBinder(testWatcher != null ? testWatcher.asBinder() : null);
                    _data2.writeStrongBinder(uiAutomationConnection != null ? uiAutomationConnection.asBinder() : null);
                    _data2.writeInt(debugMode);
                    _data2.writeInt(enableBinderTracking ? 1 : 0);
                    _data2.writeInt(trackAllocation ? 1 : 0);
                    _data2.writeInt(restrictedBackupMode ? 1 : 0);
                    _data2.writeInt(persistent ? 1 : 0);
                    if (config != null) {
                        _data2.writeInt(1);
                        config.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    if (compatInfo != null) {
                        _data2.writeInt(1);
                        compatInfo.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    _data2.writeMap(services);
                    if (coreSettings != null) {
                        _data2.writeInt(1);
                        coreSettings.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    _data2.writeString(buildSerial);
                    if (autofillOptions != null) {
                        _data2.writeInt(1);
                        i = 0;
                        autofillOptions.writeToParcel(_data2, 0);
                    } else {
                        i = 0;
                        _data2.writeInt(0);
                    }
                    if (contentCaptureOptions != null) {
                        _data2.writeInt(1);
                        contentCaptureOptions.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(i);
                    }
                    if (this.mRemote.transact(4, _data2, null, 1) || Stub.getDefaultImpl() == null) {
                        _data2.recycle();
                        return;
                    }
                    _data = _data2;
                    try {
                        Stub.getDefaultImpl().bindApplication(packageName, info, providers, testName, profilerInfo, testArguments, testWatcher, uiAutomationConnection, debugMode, enableBinderTracking, trackAllocation, restrictedBackupMode, persistent, config, compatInfo, services, coreSettings, buildSerial, autofillOptions, contentCaptureOptions);
                        _data.recycle();
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _data = _data2;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IApplicationThread
            public void runIsolatedEntryPoint(String entryPoint, String[] entryPointArgs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(entryPoint);
                    _data.writeStringArray(entryPointArgs);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().runIsolatedEntryPoint(entryPoint, entryPointArgs);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void scheduleExit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleExit();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
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
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleServiceArgs(token, args);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void updateTimeZone() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().updateTimeZone();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void processInBackground() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().processInBackground();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void scheduleBindService(IBinder token, Intent intent, boolean rebind, int processState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    int i = 0;
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (rebind) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(processState);
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleBindService(token, intent, rebind, processState);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
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
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleUnbindService(token, intent);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
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
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dumpService(fd, servicetoken, args);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void scheduleRegisteredReceiver(IIntentReceiver receiver, Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser, int processState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    int i = 0;
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeInt(resultCode);
                        try {
                            _data.writeString(data);
                            if (extras != null) {
                                _data.writeInt(1);
                                extras.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            _data.writeInt(ordered ? 1 : 0);
                            if (sticky) {
                                i = 1;
                            }
                            _data.writeInt(i);
                            _data.writeInt(sendingUser);
                            _data.writeInt(processState);
                            if (this.mRemote.transact(13, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().scheduleRegisteredReceiver(receiver, intent, resultCode, data, extras, ordered, sticky, sendingUser, processState);
                            _data.recycle();
                        } catch (Throwable th) {
                            th = th;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IApplicationThread
            public void scheduleLowMemory() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleLowMemory();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void scheduleSleeping(IBinder token, boolean sleeping) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(sleeping ? 1 : 0);
                    if (this.mRemote.transact(15, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleSleeping(token, sleeping);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void profilerControl(boolean start, ProfilerInfo profilerInfo, int profileType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(start ? 1 : 0);
                    if (profilerInfo != null) {
                        _data.writeInt(1);
                        profilerInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(profileType);
                    if (this.mRemote.transact(16, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().profilerControl(start, profilerInfo, profileType);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void setSchedulingGroup(int group) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(group);
                    if (this.mRemote.transact(17, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setSchedulingGroup(group);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void scheduleCreateBackupAgent(ApplicationInfo app, CompatibilityInfo compatInfo, int backupMode, int userId) throws RemoteException {
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
                    _data.writeInt(userId);
                    if (this.mRemote.transact(18, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleCreateBackupAgent(app, compatInfo, backupMode, userId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void scheduleDestroyBackupAgent(ApplicationInfo app, CompatibilityInfo compatInfo, int userId) throws RemoteException {
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
                    _data.writeInt(userId);
                    if (this.mRemote.transact(19, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleDestroyBackupAgent(app, compatInfo, userId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
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
                    if (this.mRemote.transact(20, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleOnNewActivityOptions(token, options);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void scheduleSuicide() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(21, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleSuicide();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void dispatchPackageBroadcast(int cmd, String[] packages) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cmd);
                    _data.writeStringArray(packages);
                    if (this.mRemote.transact(22, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dispatchPackageBroadcast(cmd, packages);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void scheduleCrash(String msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(msg);
                    if (this.mRemote.transact(23, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleCrash(msg);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void dumpHeap(boolean managed, boolean mallocInfo, boolean runGc, String path, ParcelFileDescriptor fd, RemoteCallback finishCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(managed ? 1 : 0);
                    _data.writeInt(mallocInfo ? 1 : 0);
                    _data.writeInt(runGc ? 1 : 0);
                    try {
                        _data.writeString(path);
                        if (fd != null) {
                            _data.writeInt(1);
                            fd.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (finishCallback != null) {
                            _data.writeInt(1);
                            finishCallback.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                    } catch (Throwable th) {
                        th = th;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(24, _data, null, 1) || Stub.getDefaultImpl() == null) {
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().dumpHeap(managed, mallocInfo, runGc, path, fd, finishCallback);
                        _data.recycle();
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IApplicationThread
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
                    if (this.mRemote.transact(25, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dumpActivity(fd, servicetoken, prefix, args);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void clearDnsCache() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(26, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().clearDnsCache();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void updateHttpProxy() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(27, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().updateHttpProxy();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
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
                    if (this.mRemote.transact(28, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setCoreSettings(coreSettings);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
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
                    if (this.mRemote.transact(29, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().updatePackageCompatibilityInfo(pkg, info);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void scheduleTrimMemory(int level) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(level);
                    if (this.mRemote.transact(30, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleTrimMemory(level);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void iawareTrimMemory(int level, boolean fromIAware) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(level);
                    _data.writeInt(fromIAware ? 1 : 0);
                    if (this.mRemote.transact(31, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().iawareTrimMemory(level, fromIAware);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void dumpMemInfo(ParcelFileDescriptor fd, Debug.MemoryInfo mem, boolean checkin, boolean dumpInfo, boolean dumpDalvik, boolean dumpSummaryOnly, boolean dumpUnreachable, String[] args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 0;
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
                    _data.writeInt(checkin ? 1 : 0);
                    _data.writeInt(dumpInfo ? 1 : 0);
                    _data.writeInt(dumpDalvik ? 1 : 0);
                    _data.writeInt(dumpSummaryOnly ? 1 : 0);
                    if (dumpUnreachable) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    try {
                        _data.writeStringArray(args);
                    } catch (Throwable th) {
                        th = th;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(32, _data, null, 1) || Stub.getDefaultImpl() == null) {
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().dumpMemInfo(fd, mem, checkin, dumpInfo, dumpDalvik, dumpSummaryOnly, dumpUnreachable, args);
                        _data.recycle();
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IApplicationThread
            public void dumpMemInfoProto(ParcelFileDescriptor fd, Debug.MemoryInfo mem, boolean dumpInfo, boolean dumpDalvik, boolean dumpSummaryOnly, boolean dumpUnreachable, String[] args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 0;
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
                    _data.writeInt(dumpInfo ? 1 : 0);
                    _data.writeInt(dumpDalvik ? 1 : 0);
                    _data.writeInt(dumpSummaryOnly ? 1 : 0);
                    if (dumpUnreachable) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    try {
                        _data.writeStringArray(args);
                    } catch (Throwable th) {
                        th = th;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(33, _data, null, 1) || Stub.getDefaultImpl() == null) {
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().dumpMemInfoProto(fd, mem, dumpInfo, dumpDalvik, dumpSummaryOnly, dumpUnreachable, args);
                        _data.recycle();
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IApplicationThread
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
                    if (this.mRemote.transact(34, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dumpGfxInfo(fd, args);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
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
                    if (this.mRemote.transact(35, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dumpProvider(fd, servicetoken, args);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
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
                    if (this.mRemote.transact(36, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dumpDbInfo(fd, args);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void unstableProviderDied(IBinder provider) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(provider);
                    if (this.mRemote.transact(37, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().unstableProviderDied(provider);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void requestAssistContextExtras(IBinder activityToken, IBinder requestToken, int requestType, int sessionId, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(activityToken);
                    _data.writeStrongBinder(requestToken);
                    _data.writeInt(requestType);
                    _data.writeInt(sessionId);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(38, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().requestAssistContextExtras(activityToken, requestToken, requestType, sessionId, flags);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void scheduleTranslucentConversionComplete(IBinder token, boolean timeout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(timeout ? 1 : 0);
                    if (this.mRemote.transact(39, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleTranslucentConversionComplete(token, timeout);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void setProcessState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    if (this.mRemote.transact(40, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setProcessState(state);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
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
                    if (this.mRemote.transact(41, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleInstallProvider(provider);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void updateTimePrefs(int timeFormatPreference) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(timeFormatPreference);
                    if (this.mRemote.transact(42, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().updateTimePrefs(timeFormatPreference);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void scheduleEnterAnimationComplete(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(43, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleEnterAnimationComplete(token);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void notifyCleartextNetwork(byte[] firstPacket) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(firstPacket);
                    if (this.mRemote.transact(44, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyCleartextNetwork(firstPacket);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void startBinderTracking() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(45, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().startBinderTracking();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
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
                    if (this.mRemote.transact(46, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().stopBinderTrackingAndDump(fd);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void scheduleLocalVoiceInteractionStarted(IBinder token, IVoiceInteractor voiceInteractor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeStrongBinder(voiceInteractor != null ? voiceInteractor.asBinder() : null);
                    if (this.mRemote.transact(47, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleLocalVoiceInteractionStarted(token, voiceInteractor);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void handleTrustStorageUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(48, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().handleTrustStorageUpdate();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void attachAgent(String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    if (this.mRemote.transact(49, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().attachAgent(path);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
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
                    if (this.mRemote.transact(50, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleApplicationInfoChanged(ai);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void scheduleApplicationThemeInfoChanged(ApplicationInfo ai, boolean fromThemeChange) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 0;
                    if (ai != null) {
                        _data.writeInt(1);
                        ai.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (fromThemeChange) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(51, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleApplicationThemeInfoChanged(ai, fromThemeChange);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void setNetworkBlockSeq(long procStateSeq) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(procStateSeq);
                    if (this.mRemote.transact(52, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setNetworkBlockSeq(procStateSeq);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
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
                    if (this.mRemote.transact(53, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleTransaction(transaction);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void schedulePCWindowStateChanged(IBinder token, int windowState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(windowState);
                    if (this.mRemote.transact(54, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().schedulePCWindowStateChanged(token, windowState);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
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
                    if (this.mRemote.transact(55, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().requestContentNode(appToken, data, token);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
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
                    if (this.mRemote.transact(56, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().requestContentOther(appToken, data, token);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void scheduleFreeFormOutLineChanged(IBinder token, int touchingState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(touchingState);
                    if (this.mRemote.transact(57, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleFreeFormOutLineChanged(token, touchingState);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void scheduleRestoreFreeFormConfig(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(58, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleRestoreFreeFormConfig(token);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void requestDirectActions(IBinder activityToken, IVoiceInteractor intractor, RemoteCallback cancellationCallback, RemoteCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(activityToken);
                    _data.writeStrongBinder(intractor != null ? intractor.asBinder() : null);
                    if (cancellationCallback != null) {
                        _data.writeInt(1);
                        cancellationCallback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callback != null) {
                        _data.writeInt(1);
                        callback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(59, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().requestDirectActions(activityToken, intractor, cancellationCallback, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void performDirectAction(IBinder activityToken, String actionId, Bundle arguments, RemoteCallback cancellationCallback, RemoteCallback resultCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(activityToken);
                    _data.writeString(actionId);
                    if (arguments != null) {
                        _data.writeInt(1);
                        arguments.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (cancellationCallback != null) {
                        _data.writeInt(1);
                        cancellationCallback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (resultCallback != null) {
                        _data.writeInt(1);
                        resultCallback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(60, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().performDirectAction(activityToken, actionId, arguments, cancellationCallback, resultCallback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void notifyAppToTop(int enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    if (this.mRemote.transact(61, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyAppToTop(enable);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void notifyHwMultiWindowCaption(IBinder token, Bundle info) throws RemoteException {
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
                    if (this.mRemote.transact(62, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyHwMultiWindowCaption(token, info);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IApplicationThread
            public void preLoadClass(ApplicationInfo info, CompatibilityInfo compatInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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
                    if (this.mRemote.transact(63, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().preLoadClass(info, compatInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IApplicationThread impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IApplicationThread getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

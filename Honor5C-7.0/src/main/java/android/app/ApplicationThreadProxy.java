package android.app;

import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug.MemoryInfo;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.TransactionTooLargeException;
import android.util.Log;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.content.ReferrerIntent;
import java.io.FileDescriptor;
import java.util.List;
import java.util.Map;

/* compiled from: ApplicationThreadNative */
class ApplicationThreadProxy implements IApplicationThread {
    private final IBinder mRemote;

    public ApplicationThreadProxy(IBinder remote) {
        this.mRemote = remote;
    }

    public final IBinder asBinder() {
        return this.mRemote;
    }

    public final void schedulePauseActivity(IBinder token, boolean finished, boolean userLeaving, int configChanges, boolean dontReport) throws RemoteException {
        int i;
        int i2 = 0;
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        if (finished) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        if (userLeaving) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        data.writeInt(configChanges);
        if (dontReport) {
            i2 = 1;
        }
        data.writeInt(i2);
        this.mRemote.transact(1, data, null, 1);
        data.recycle();
    }

    public final void scheduleStopActivity(IBinder token, boolean showWindow, int configChanges) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        data.writeInt(showWindow ? 1 : 0);
        data.writeInt(configChanges);
        this.mRemote.transact(3, data, null, 1);
        data.recycle();
    }

    public final void scheduleWindowVisibility(IBinder token, boolean showWindow) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        data.writeInt(showWindow ? 1 : 0);
        this.mRemote.transact(4, data, null, 1);
        data.recycle();
    }

    public final void scheduleSleeping(IBinder token, boolean sleeping) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        data.writeInt(sleeping ? 1 : 0);
        this.mRemote.transact(27, data, null, 1);
        data.recycle();
    }

    public final void scheduleResumeActivity(IBinder token, int procState, boolean isForward, Bundle resumeArgs) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        data.writeInt(procState);
        data.writeInt(isForward ? 1 : 0);
        data.writeBundle(resumeArgs);
        this.mRemote.transact(5, data, null, 1);
        data.recycle();
    }

    public final void scheduleSendResult(IBinder token, List<ResultInfo> results) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        data.writeTypedList(results);
        this.mRemote.transact(6, data, null, 1);
        data.recycle();
    }

    public final void scheduleLaunchActivity(Intent intent, IBinder token, int ident, ActivityInfo info, Configuration curConfig, Configuration overrideConfig, CompatibilityInfo compatInfo, String referrer, IVoiceInteractor voiceInteractor, int procState, Bundle state, PersistableBundle persistentState, List<ResultInfo> pendingResults, List<ReferrerIntent> pendingNewIntents, boolean notResumed, boolean isForward, ProfilerInfo profilerInfo) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        intent.writeToParcel(data, 0);
        data.writeStrongBinder(token);
        data.writeInt(ident);
        info.writeToParcel(data, 0);
        curConfig.writeToParcel(data, 0);
        if (overrideConfig != null) {
            data.writeInt(1);
            overrideConfig.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        compatInfo.writeToParcel(data, 0);
        data.writeString(referrer);
        data.writeStrongBinder(voiceInteractor != null ? voiceInteractor.asBinder() : null);
        data.writeInt(procState);
        data.writeBundle(state);
        data.writePersistableBundle(persistentState);
        data.writeTypedList(pendingResults);
        data.writeTypedList(pendingNewIntents);
        data.writeInt(notResumed ? 1 : 0);
        data.writeInt(isForward ? 1 : 0);
        if (profilerInfo != null) {
            data.writeInt(1);
            profilerInfo.writeToParcel(data, 1);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(7, data, null, 1);
        data.recycle();
    }

    public final void scheduleRelaunchActivity(IBinder token, List<ResultInfo> pendingResults, List<ReferrerIntent> pendingNewIntents, int configChanges, boolean notResumed, Configuration config, Configuration overrideConfig, boolean preserveWindow) throws RemoteException {
        int i;
        int i2 = 0;
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        data.writeTypedList(pendingResults);
        data.writeTypedList(pendingNewIntents);
        data.writeInt(configChanges);
        if (notResumed) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        config.writeToParcel(data, 0);
        if (overrideConfig != null) {
            data.writeInt(1);
            overrideConfig.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        if (preserveWindow) {
            i2 = 1;
        }
        data.writeInt(i2);
        this.mRemote.transact(26, data, null, 1);
        data.recycle();
    }

    public void scheduleNewIntent(List<ReferrerIntent> intents, IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeTypedList(intents);
        data.writeStrongBinder(token);
        this.mRemote.transact(8, data, null, 1);
        data.recycle();
    }

    public final void scheduleDestroyActivity(IBinder token, boolean finishing, int configChanges) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        data.writeInt(finishing ? 1 : 0);
        data.writeInt(configChanges);
        this.mRemote.transact(9, data, null, 1);
        data.recycle();
    }

    public final void scheduleReceiver(Intent intent, ActivityInfo info, CompatibilityInfo compatInfo, int resultCode, String resultData, Bundle map, boolean sync, int sendingUser, int processState) throws RemoteException {
        int i = 0;
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        intent.writeToParcel(data, 0);
        info.writeToParcel(data, 0);
        compatInfo.writeToParcel(data, 0);
        data.writeInt(resultCode);
        data.writeString(resultData);
        data.writeBundle(map);
        if (sync) {
            i = 1;
        }
        data.writeInt(i);
        data.writeInt(sendingUser);
        data.writeInt(processState);
        this.mRemote.transact(10, data, null, 1);
        data.recycle();
    }

    public final void scheduleCreateBackupAgent(ApplicationInfo app, CompatibilityInfo compatInfo, int backupMode) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        app.writeToParcel(data, 0);
        compatInfo.writeToParcel(data, 0);
        data.writeInt(backupMode);
        this.mRemote.transact(30, data, null, 1);
        data.recycle();
    }

    public final void scheduleDestroyBackupAgent(ApplicationInfo app, CompatibilityInfo compatInfo) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        app.writeToParcel(data, 0);
        compatInfo.writeToParcel(data, 0);
        this.mRemote.transact(31, data, null, 1);
        data.recycle();
    }

    public final void scheduleCreateService(IBinder token, ServiceInfo info, CompatibilityInfo compatInfo, int processState) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        info.writeToParcel(data, 0);
        compatInfo.writeToParcel(data, 0);
        data.writeInt(processState);
        try {
            this.mRemote.transact(11, data, null, 1);
            data.recycle();
        } catch (TransactionTooLargeException e) {
            Log.e("CREATE_SERVICE", "Binder failure starting service; service=" + info);
            throw e;
        }
    }

    public final void scheduleBindService(IBinder token, Intent intent, boolean rebind, int processState) throws RemoteException {
        int i = 0;
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        intent.writeToParcel(data, 0);
        if (rebind) {
            i = 1;
        }
        data.writeInt(i);
        data.writeInt(processState);
        this.mRemote.transact(20, data, null, 1);
        data.recycle();
    }

    public final void scheduleUnbindService(IBinder token, Intent intent) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        intent.writeToParcel(data, 0);
        this.mRemote.transact(21, data, null, 1);
        data.recycle();
    }

    public final void scheduleServiceArgs(IBinder token, boolean taskRemoved, int startId, int flags, Intent args) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        if (taskRemoved) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        data.writeInt(startId);
        data.writeInt(flags);
        if (args != null) {
            data.writeInt(1);
            args.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(17, data, null, 1);
        data.recycle();
    }

    public final void scheduleStopService(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(12, data, null, 1);
        data.recycle();
    }

    public final void bindApplication(String packageName, ApplicationInfo info, List<ProviderInfo> providers, ComponentName testName, ProfilerInfo profilerInfo, Bundle testArgs, IInstrumentationWatcher testWatcher, IUiAutomationConnection uiAutomationConnection, int debugMode, boolean enableBinderTracking, boolean trackAllocation, boolean restrictedBackupMode, boolean persistent, Configuration config, CompatibilityInfo compatInfo, Map<String, IBinder> services, Bundle coreSettings) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeString(packageName);
        info.writeToParcel(data, 0);
        data.writeTypedList(providers);
        if (testName == null) {
            data.writeInt(0);
        } else {
            data.writeInt(1);
            testName.writeToParcel(data, 0);
        }
        if (profilerInfo != null) {
            data.writeInt(1);
            profilerInfo.writeToParcel(data, 1);
        } else {
            data.writeInt(0);
        }
        data.writeBundle(testArgs);
        data.writeStrongInterface(testWatcher);
        data.writeStrongInterface(uiAutomationConnection);
        data.writeInt(debugMode);
        data.writeInt(enableBinderTracking ? 1 : 0);
        data.writeInt(trackAllocation ? 1 : 0);
        data.writeInt(restrictedBackupMode ? 1 : 0);
        data.writeInt(persistent ? 1 : 0);
        config.writeToParcel(data, 0);
        compatInfo.writeToParcel(data, 0);
        data.writeMap(services);
        data.writeBundle(coreSettings);
        this.mRemote.transact(13, data, null, 1);
        data.recycle();
    }

    public final void scheduleExit() throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        this.mRemote.transact(14, data, null, 1);
        data.recycle();
    }

    public final void scheduleSuicide() throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        this.mRemote.transact(33, data, null, 1);
        data.recycle();
    }

    public final void scheduleConfigurationChanged(Configuration config) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        config.writeToParcel(data, 0);
        this.mRemote.transact(16, data, null, 1);
        data.recycle();
    }

    public final void scheduleLocalVoiceInteractionStarted(IBinder token, IVoiceInteractor voiceInteractor) throws RemoteException {
        IBinder asBinder;
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        if (voiceInteractor != null) {
            asBinder = voiceInteractor.asBinder();
        } else {
            asBinder = null;
        }
        data.writeStrongBinder(asBinder);
        this.mRemote.transact(61, data, null, 1);
        data.recycle();
    }

    public void updateTimeZone() throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        this.mRemote.transact(18, data, null, 1);
        data.recycle();
    }

    public void clearDnsCache() throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        this.mRemote.transact(38, data, null, 1);
        data.recycle();
    }

    public void setHttpProxy(String proxy, String port, String exclList, Uri pacFileUrl) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeString(proxy);
        data.writeString(port);
        data.writeString(exclList);
        pacFileUrl.writeToParcel(data, 0);
        this.mRemote.transact(39, data, null, 1);
        data.recycle();
    }

    public void processInBackground() throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        this.mRemote.transact(19, data, null, 1);
        data.recycle();
    }

    public void dumpService(FileDescriptor fd, IBinder token, String[] args) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeFileDescriptor(fd);
        data.writeStrongBinder(token);
        data.writeStringArray(args);
        this.mRemote.transact(22, data, null, 1);
        data.recycle();
    }

    public void dumpProvider(FileDescriptor fd, IBinder token, String[] args) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeFileDescriptor(fd);
        data.writeStrongBinder(token);
        data.writeStringArray(args);
        this.mRemote.transact(45, data, null, 1);
        data.recycle();
    }

    public void scheduleRegisteredReceiver(IIntentReceiver receiver, Intent intent, int resultCode, String dataStr, Bundle extras, boolean ordered, boolean sticky, int sendingUser, int processState) throws RemoteException {
        int i;
        int i2 = 0;
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(receiver.asBinder());
        intent.writeToParcel(data, 0);
        data.writeInt(resultCode);
        data.writeString(dataStr);
        data.writeBundle(extras);
        if (ordered) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        if (sticky) {
            i2 = 1;
        }
        data.writeInt(i2);
        data.writeInt(sendingUser);
        data.writeInt(processState);
        this.mRemote.transact(23, data, null, 1);
        data.recycle();
    }

    public final void scheduleLowMemory() throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        this.mRemote.transact(24, data, null, 1);
        data.recycle();
    }

    public final void scheduleActivityConfigurationChanged(IBinder token, Configuration overrideConfig, boolean reportToActivity) throws RemoteException {
        int i = 0;
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        if (overrideConfig != null) {
            data.writeInt(1);
            overrideConfig.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        if (reportToActivity) {
            i = 1;
        }
        data.writeInt(i);
        this.mRemote.transact(25, data, null, 1);
        data.recycle();
    }

    public void profilerControl(boolean start, ProfilerInfo profilerInfo, int profileType) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        if (start) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        data.writeInt(profileType);
        if (profilerInfo != null) {
            data.writeInt(1);
            profilerInfo.writeToParcel(data, 1);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(28, data, null, 1);
        data.recycle();
    }

    public void setSchedulingGroup(int group) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeInt(group);
        this.mRemote.transact(29, data, null, 1);
        data.recycle();
    }

    public void dispatchPackageBroadcast(int cmd, String[] packages) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeInt(cmd);
        data.writeStringArray(packages);
        this.mRemote.transact(34, data, null, 1);
        data.recycle();
    }

    public void scheduleCrash(String msg) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeString(msg);
        this.mRemote.transact(35, data, null, 1);
        data.recycle();
    }

    public void dumpHeap(boolean managed, String path, ParcelFileDescriptor fd) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        if (managed) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        data.writeString(path);
        if (fd != null) {
            data.writeInt(1);
            fd.writeToParcel(data, 1);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(36, data, null, 1);
        data.recycle();
    }

    public void dumpActivity(FileDescriptor fd, IBinder token, String prefix, String[] args) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeFileDescriptor(fd);
        data.writeStrongBinder(token);
        data.writeString(prefix);
        data.writeStringArray(args);
        this.mRemote.transact(37, data, null, 1);
        data.recycle();
    }

    public void setCoreSettings(Bundle coreSettings) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeBundle(coreSettings);
        this.mRemote.transact(40, data, null, 1);
    }

    public void updatePackageCompatibilityInfo(String pkg, CompatibilityInfo info) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeString(pkg);
        info.writeToParcel(data, 0);
        this.mRemote.transact(41, data, null, 1);
    }

    public void scheduleTrimMemory(int level) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeInt(level);
        this.mRemote.transact(42, data, null, 1);
        data.recycle();
    }

    public void dumpMemInfo(FileDescriptor fd, MemoryInfo mem, boolean checkin, boolean dumpInfo, boolean dumpDalvik, boolean dumpSummaryOnly, boolean dumpUnreachable, String[] args) throws RemoteException {
        int i;
        int i2 = 1;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeFileDescriptor(fd);
        mem.writeToParcel(data, 0);
        if (checkin) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        if (dumpInfo) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        if (dumpDalvik) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        if (dumpSummaryOnly) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        if (!dumpUnreachable) {
            i2 = 0;
        }
        data.writeInt(i2);
        data.writeStringArray(args);
        this.mRemote.transact(43, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void dumpGfxInfo(FileDescriptor fd, String[] args) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeFileDescriptor(fd);
        data.writeStringArray(args);
        this.mRemote.transact(44, data, null, 1);
        data.recycle();
    }

    public void dumpDbInfo(FileDescriptor fd, String[] args) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeFileDescriptor(fd);
        data.writeStringArray(args);
        this.mRemote.transact(46, data, null, 1);
        data.recycle();
    }

    public void unstableProviderDied(IBinder provider) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(provider);
        this.mRemote.transact(47, data, null, 1);
        data.recycle();
    }

    public void requestAssistContextExtras(IBinder activityToken, IBinder requestToken, int requestType, int sessionId) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(activityToken);
        data.writeStrongBinder(requestToken);
        data.writeInt(requestType);
        data.writeInt(sessionId);
        this.mRemote.transact(48, data, null, 1);
        data.recycle();
    }

    public void scheduleTranslucentConversionComplete(IBinder token, boolean timeout) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        data.writeInt(timeout ? 1 : 0);
        this.mRemote.transact(49, data, null, 1);
        data.recycle();
    }

    public void scheduleOnNewActivityOptions(IBinder token, ActivityOptions options) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        data.writeBundle(options == null ? null : options.toBundle());
        this.mRemote.transact(32, data, null, 1);
        data.recycle();
    }

    public void setProcessState(int state) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeInt(state);
        this.mRemote.transact(50, data, null, 1);
        data.recycle();
    }

    public void scheduleInstallProvider(ProviderInfo provider) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        provider.writeToParcel(data, 0);
        this.mRemote.transact(51, data, null, 1);
        data.recycle();
    }

    public void updateTimePrefs(boolean is24Hour) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeByte(is24Hour ? (byte) 1 : (byte) 0);
        this.mRemote.transact(52, data, null, 1);
        data.recycle();
    }

    public void scheduleCancelVisibleBehind(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(53, data, null, 1);
        data.recycle();
    }

    public void scheduleBackgroundVisibleBehindChanged(IBinder token, boolean enabled) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        data.writeInt(enabled ? 1 : 0);
        this.mRemote.transact(54, data, null, 1);
        data.recycle();
    }

    public void scheduleEnterAnimationComplete(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(55, data, null, 1);
        data.recycle();
    }

    public void notifyCleartextNetwork(byte[] firstPacket) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeByteArray(firstPacket);
        this.mRemote.transact(56, data, null, 1);
        data.recycle();
    }

    public void startBinderTracking() throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        this.mRemote.transact(57, data, null, 1);
        data.recycle();
    }

    public void stopBinderTrackingAndDump(FileDescriptor fd) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeFileDescriptor(fd);
        this.mRemote.transact(58, data, null, 1);
        data.recycle();
    }

    public final void scheduleMultiWindowModeChanged(IBinder token, boolean isInMultiWindowMode) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        data.writeInt(isInMultiWindowMode ? 1 : 0);
        this.mRemote.transact(59, data, null, 1);
        data.recycle();
    }

    public final void schedulePictureInPictureModeChanged(IBinder token, boolean isInPipMode) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IApplicationThread.descriptor);
        data.writeStrongBinder(token);
        data.writeInt(isInPipMode ? 1 : 0);
        this.mRemote.transact(60, data, null, 1);
        data.recycle();
    }
}

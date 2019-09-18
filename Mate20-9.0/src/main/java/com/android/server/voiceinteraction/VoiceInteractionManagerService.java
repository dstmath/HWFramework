package com.android.server.voiceinteraction;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.ShortcutServiceInternal;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.soundtrigger.IRecognitionStatusCallback;
import android.hardware.soundtrigger.SoundTrigger;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.service.voice.IVoiceInteractionService;
import android.service.voice.IVoiceInteractionSession;
import android.service.voice.VoiceInteractionManagerInternal;
import android.service.voice.VoiceInteractionServiceInfo;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import com.android.internal.app.IVoiceInteractionManagerService;
import com.android.internal.app.IVoiceInteractionSessionListener;
import com.android.internal.app.IVoiceInteractionSessionShowCallback;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.Preconditions;
import com.android.server.FgThread;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.UiThread;
import com.android.server.soundtrigger.SoundTriggerInternal;
import com.android.server.voiceinteraction.VoiceInteractionManagerService;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

public class VoiceInteractionManagerService extends SystemService {
    static final boolean DEBUG = false;
    static final String TAG = "VoiceInteractionManagerService";
    final ActivityManagerInternal mAmInternal;
    final Context mContext;
    final DatabaseHelper mDbHelper;
    final ArraySet<Integer> mLoadedKeyphraseIds = new ArraySet<>();
    final ContentResolver mResolver;
    /* access modifiers changed from: private */
    public final VoiceInteractionManagerServiceStub mServiceStub;
    ShortcutServiceInternal mShortcutServiceInternal;
    SoundTriggerInternal mSoundTriggerInternal;
    final UserManager mUserManager;
    /* access modifiers changed from: private */
    public final RemoteCallbackList<IVoiceInteractionSessionListener> mVoiceInteractionSessionListeners = new RemoteCallbackList<>();

    class LocalService extends VoiceInteractionManagerInternal {
        LocalService() {
        }

        public void startLocalVoiceInteraction(IBinder callingActivity, Bundle options) {
            VoiceInteractionManagerService.this.mServiceStub.startLocalVoiceInteraction(callingActivity, options);
        }

        public boolean supportsLocalVoiceInteraction() {
            return VoiceInteractionManagerService.this.mServiceStub.supportsLocalVoiceInteraction();
        }

        public void stopLocalVoiceInteraction(IBinder callingActivity) {
            VoiceInteractionManagerService.this.mServiceStub.stopLocalVoiceInteraction(callingActivity);
        }
    }

    class VoiceInteractionManagerServiceStub extends IVoiceInteractionManagerService.Stub {
        /* access modifiers changed from: private */
        public int mCurUser;
        private boolean mCurUserUnlocked;
        private final boolean mEnableService;
        VoiceInteractionManagerServiceImpl mImpl;
        PackageMonitor mPackageMonitor = new PackageMonitor() {
            public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
                int userHandle = UserHandle.getUserId(uid);
                ComponentName curInteractor = VoiceInteractionManagerServiceStub.this.getCurInteractor(userHandle);
                ComponentName curRecognizer = VoiceInteractionManagerServiceStub.this.getCurRecognizer(userHandle);
                boolean hit = false;
                int length = packages.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    String pkg = packages[i];
                    if (curInteractor == null || !pkg.equals(curInteractor.getPackageName())) {
                        if (curRecognizer != null && pkg.equals(curRecognizer.getPackageName())) {
                            hit = true;
                            break;
                        }
                        i++;
                    } else {
                        hit = true;
                        break;
                    }
                }
                if (hit && doit) {
                    synchronized (VoiceInteractionManagerServiceStub.this) {
                        VoiceInteractionManagerServiceStub.this.unloadAllKeyphraseModels();
                        if (VoiceInteractionManagerServiceStub.this.mImpl != null) {
                            VoiceInteractionManagerServiceStub.this.mImpl.shutdownLocked();
                            VoiceInteractionManagerServiceStub.this.setImplLocked(null);
                        }
                        VoiceInteractionManagerServiceStub.this.setCurInteractor(null, userHandle);
                        VoiceInteractionManagerServiceStub.this.setCurRecognizer(null, userHandle);
                        VoiceInteractionManagerServiceStub.this.resetCurAssistant(userHandle);
                        VoiceInteractionManagerServiceStub.this.initForUser(userHandle);
                        VoiceInteractionManagerServiceStub.this.switchImplementationIfNeededLocked(true);
                    }
                }
                return doit;
            }

            public void onHandleUserStop(Intent intent, int userHandle) {
            }

            public void onPackageModified(String pkgName) {
                if (VoiceInteractionManagerServiceStub.this.mCurUser == getChangingUserId() && isPackageAppearing(pkgName) == 0) {
                    ComponentName curInteractor = VoiceInteractionManagerServiceStub.this.getCurInteractor(VoiceInteractionManagerServiceStub.this.mCurUser);
                    if (curInteractor == null) {
                        VoiceInteractionServiceInfo availInteractorInfo = VoiceInteractionManagerServiceStub.this.findAvailInteractor(VoiceInteractionManagerServiceStub.this.mCurUser, pkgName);
                        if (availInteractorInfo != null) {
                            VoiceInteractionManagerServiceStub.this.setCurInteractor(new ComponentName(availInteractorInfo.getServiceInfo().packageName, availInteractorInfo.getServiceInfo().name), VoiceInteractionManagerServiceStub.this.mCurUser);
                            if (VoiceInteractionManagerServiceStub.this.getCurRecognizer(VoiceInteractionManagerServiceStub.this.mCurUser) == null && availInteractorInfo.getRecognitionService() != null) {
                                VoiceInteractionManagerServiceStub.this.setCurRecognizer(new ComponentName(availInteractorInfo.getServiceInfo().packageName, availInteractorInfo.getRecognitionService()), VoiceInteractionManagerServiceStub.this.mCurUser);
                            }
                        }
                    } else if (didSomePackagesChange()) {
                        if (curInteractor != null && pkgName.equals(curInteractor.getPackageName())) {
                            VoiceInteractionManagerServiceStub.this.switchImplementationIfNeeded(true);
                        }
                    } else if (curInteractor != null && isComponentModified(curInteractor.getClassName())) {
                        VoiceInteractionManagerServiceStub.this.switchImplementationIfNeeded(true);
                    }
                }
            }

            /* JADX WARNING: Code restructure failed: missing block: B:11:0x0031, code lost:
                return;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:27:0x0080, code lost:
                return;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:44:0x00d7, code lost:
                return;
             */
            public void onSomePackagesChanged() {
                int userHandle = getChangingUserId();
                synchronized (VoiceInteractionManagerServiceStub.this) {
                    ComponentName curInteractor = VoiceInteractionManagerServiceStub.this.getCurInteractor(userHandle);
                    ComponentName curRecognizer = VoiceInteractionManagerServiceStub.this.getCurRecognizer(userHandle);
                    ComponentName curAssistant = VoiceInteractionManagerServiceStub.this.getCurAssistant(userHandle);
                    if (curRecognizer == null) {
                        if (anyPackagesAppearing()) {
                            ComponentName curRecognizer2 = VoiceInteractionManagerServiceStub.this.findAvailRecognizer(null, userHandle);
                            if (curRecognizer2 != null) {
                                VoiceInteractionManagerServiceStub.this.setCurRecognizer(curRecognizer2, userHandle);
                            }
                        }
                    } else if (curInteractor != null) {
                        if (isPackageDisappearing(curInteractor.getPackageName()) == 3) {
                            VoiceInteractionManagerServiceStub.this.setCurInteractor(null, userHandle);
                            VoiceInteractionManagerServiceStub.this.setCurRecognizer(null, userHandle);
                            VoiceInteractionManagerServiceStub.this.resetCurAssistant(userHandle);
                            VoiceInteractionManagerServiceStub.this.initForUser(userHandle);
                        } else if (!(isPackageAppearing(curInteractor.getPackageName()) == 0 || VoiceInteractionManagerServiceStub.this.mImpl == null || !curInteractor.getPackageName().equals(VoiceInteractionManagerServiceStub.this.mImpl.mComponent.getPackageName()))) {
                            VoiceInteractionManagerServiceStub.this.switchImplementationIfNeededLocked(true);
                        }
                    } else if (curAssistant == null || isPackageDisappearing(curAssistant.getPackageName()) != 3) {
                        int change = isPackageDisappearing(curRecognizer.getPackageName());
                        if (change != 3) {
                            if (change != 2) {
                                if (isPackageModified(curRecognizer.getPackageName())) {
                                    VoiceInteractionManagerServiceStub.this.setCurRecognizer(VoiceInteractionManagerServiceStub.this.findAvailRecognizer(curRecognizer.getPackageName(), userHandle), userHandle);
                                }
                            }
                        }
                        VoiceInteractionManagerServiceStub.this.setCurRecognizer(VoiceInteractionManagerServiceStub.this.findAvailRecognizer(null, userHandle), userHandle);
                    } else {
                        VoiceInteractionManagerServiceStub.this.setCurInteractor(null, userHandle);
                        VoiceInteractionManagerServiceStub.this.setCurRecognizer(null, userHandle);
                        VoiceInteractionManagerServiceStub.this.resetCurAssistant(userHandle);
                        VoiceInteractionManagerServiceStub.this.initForUser(userHandle);
                    }
                }
            }
        };
        private boolean mSafeMode;

        class SettingsObserver extends ContentObserver {
            SettingsObserver(Handler handler) {
                super(handler);
                VoiceInteractionManagerService.this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("voice_interaction_service"), false, this, -1);
            }

            public void onChange(boolean selfChange) {
                synchronized (VoiceInteractionManagerServiceStub.this) {
                    VoiceInteractionManagerServiceStub.this.switchImplementationIfNeededLocked(false);
                }
            }
        }

        VoiceInteractionManagerServiceStub() {
            this.mEnableService = shouldEnableService(VoiceInteractionManagerService.this.mContext);
        }

        /* access modifiers changed from: package-private */
        public void startLocalVoiceInteraction(final IBinder token, Bundle options) {
            if (this.mImpl != null) {
                long caller = Binder.clearCallingIdentity();
                try {
                    this.mImpl.showSessionLocked(options, 16, new IVoiceInteractionSessionShowCallback.Stub() {
                        public void onFailed() {
                        }

                        public void onShown() {
                            VoiceInteractionManagerService.this.mAmInternal.onLocalVoiceInteractionStarted(token, VoiceInteractionManagerServiceStub.this.mImpl.mActiveSession.mSession, VoiceInteractionManagerServiceStub.this.mImpl.mActiveSession.mInteractor);
                        }
                    }, token);
                } finally {
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public void stopLocalVoiceInteraction(IBinder callingActivity) {
            if (this.mImpl != null) {
                long caller = Binder.clearCallingIdentity();
                try {
                    this.mImpl.finishLocked(callingActivity, true);
                } finally {
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public boolean supportsLocalVoiceInteraction() {
            if (this.mImpl == null) {
                return false;
            }
            return this.mImpl.supportsLocalVoiceInteraction();
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            try {
                return VoiceInteractionManagerService.super.onTransact(code, data, reply, flags);
            } catch (RuntimeException e) {
                if (!(e instanceof SecurityException)) {
                    Slog.e(VoiceInteractionManagerService.TAG, "VoiceInteractionManagerService Crash", e);
                }
                throw e;
            }
        }

        public void initForUser(int userHandle) {
            String curInteractorStr = Settings.Secure.getStringForUser(VoiceInteractionManagerService.this.mContext.getContentResolver(), "voice_interaction_service", userHandle);
            ComponentName curRecognizer = getCurRecognizer(userHandle);
            VoiceInteractionServiceInfo curInteractorInfo = null;
            if (curInteractorStr == null && curRecognizer != null && this.mEnableService) {
                curInteractorInfo = findAvailInteractor(userHandle, curRecognizer.getPackageName());
                if (curInteractorInfo != null) {
                    curRecognizer = null;
                }
            }
            String forceInteractorPackage = getForceVoiceInteractionServicePackage(VoiceInteractionManagerService.this.mContext.getResources());
            if (forceInteractorPackage != null) {
                curInteractorInfo = findAvailInteractor(userHandle, forceInteractorPackage);
                if (curInteractorInfo != null) {
                    curRecognizer = null;
                }
            }
            if (!this.mEnableService && curInteractorStr != null && !TextUtils.isEmpty(curInteractorStr)) {
                setCurInteractor(null, userHandle);
                curInteractorStr = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            }
            if (curRecognizer != null) {
                IPackageManager pm = AppGlobals.getPackageManager();
                ServiceInfo interactorInfo = null;
                ServiceInfo recognizerInfo = null;
                ComponentName curInteractor = !TextUtils.isEmpty(curInteractorStr) ? ComponentName.unflattenFromString(curInteractorStr) : null;
                try {
                    recognizerInfo = pm.getServiceInfo(curRecognizer, 786432, userHandle);
                    if (curInteractor != null) {
                        interactorInfo = pm.getServiceInfo(curInteractor, 786432, userHandle);
                    }
                } catch (RemoteException e) {
                }
                if (recognizerInfo != null && (curInteractor == null || interactorInfo != null)) {
                    return;
                }
            }
            if (curInteractorInfo == null && this.mEnableService) {
                curInteractorInfo = findAvailInteractor(userHandle, null);
            }
            if (curInteractorInfo != null) {
                setCurInteractor(new ComponentName(curInteractorInfo.getServiceInfo().packageName, curInteractorInfo.getServiceInfo().name), userHandle);
                if (curInteractorInfo.getRecognitionService() != null) {
                    setCurRecognizer(new ComponentName(curInteractorInfo.getServiceInfo().packageName, curInteractorInfo.getRecognitionService()), userHandle);
                    return;
                }
            }
            ComponentName curRecognizer2 = findAvailRecognizer(null, userHandle);
            if (curRecognizer2 != null) {
                if (curInteractorInfo == null) {
                    setCurInteractor(null, userHandle);
                }
                setCurRecognizer(curRecognizer2, userHandle);
            }
        }

        private boolean shouldEnableService(Context context) {
            return (!ActivityManager.isLowRamDeviceStatic() && context.getPackageManager().hasSystemFeature("android.software.voice_recognizers")) || getForceVoiceInteractionServicePackage(context.getResources()) != null;
        }

        private String getForceVoiceInteractionServicePackage(Resources res) {
            String interactorPackage = res.getString(17039807);
            if (TextUtils.isEmpty(interactorPackage)) {
                return null;
            }
            return interactorPackage;
        }

        public void systemRunning(boolean safeMode) {
            this.mSafeMode = safeMode;
            this.mPackageMonitor.register(VoiceInteractionManagerService.this.mContext, BackgroundThread.getHandler().getLooper(), UserHandle.ALL, true);
            new SettingsObserver(UiThread.getHandler());
            synchronized (this) {
                this.mCurUser = ActivityManager.getCurrentUser();
                switchImplementationIfNeededLocked(false);
            }
        }

        public void switchUser(int userHandle) {
            FgThread.getHandler().post(new Runnable(userHandle) {
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    VoiceInteractionManagerService.VoiceInteractionManagerServiceStub.lambda$switchUser$0(VoiceInteractionManagerService.VoiceInteractionManagerServiceStub.this, this.f$1);
                }
            });
        }

        public static /* synthetic */ void lambda$switchUser$0(VoiceInteractionManagerServiceStub voiceInteractionManagerServiceStub, int userHandle) {
            synchronized (voiceInteractionManagerServiceStub) {
                voiceInteractionManagerServiceStub.mCurUser = userHandle;
                voiceInteractionManagerServiceStub.mCurUserUnlocked = false;
                voiceInteractionManagerServiceStub.switchImplementationIfNeededLocked(false);
            }
        }

        /* access modifiers changed from: package-private */
        public void switchImplementationIfNeeded(boolean force) {
            synchronized (this) {
                switchImplementationIfNeededLocked(force);
            }
        }

        /* access modifiers changed from: package-private */
        public void switchImplementationIfNeededLocked(boolean force) {
            if (!this.mSafeMode) {
                String curService = Settings.Secure.getStringForUser(VoiceInteractionManagerService.this.mResolver, "voice_interaction_service", this.mCurUser);
                ComponentName serviceComponent = null;
                ServiceInfo serviceInfo = null;
                boolean hasComponent = false;
                if (curService != null && !curService.isEmpty()) {
                    try {
                        serviceComponent = ComponentName.unflattenFromString(curService);
                        serviceInfo = AppGlobals.getPackageManager().getServiceInfo(serviceComponent, 0, this.mCurUser);
                    } catch (RemoteException | RuntimeException e) {
                        Slog.e(VoiceInteractionManagerService.TAG, "Bad voice interaction service name " + curService, e);
                        serviceComponent = null;
                        serviceInfo = null;
                    }
                }
                if (!(serviceComponent == null || serviceInfo == null)) {
                    hasComponent = true;
                }
                if (VoiceInteractionManagerService.this.mUserManager.isUserUnlockingOrUnlocked(this.mCurUser)) {
                    if (hasComponent) {
                        VoiceInteractionManagerService.this.mShortcutServiceInternal.setShortcutHostPackage(VoiceInteractionManagerService.TAG, serviceComponent.getPackageName(), this.mCurUser);
                        VoiceInteractionManagerService.this.mAmInternal.setAllowAppSwitches(VoiceInteractionManagerService.TAG, serviceInfo.applicationInfo.uid, this.mCurUser);
                    } else {
                        VoiceInteractionManagerService.this.mShortcutServiceInternal.setShortcutHostPackage(VoiceInteractionManagerService.TAG, null, this.mCurUser);
                        VoiceInteractionManagerService.this.mAmInternal.setAllowAppSwitches(VoiceInteractionManagerService.TAG, -1, this.mCurUser);
                    }
                }
                if (force || this.mImpl == null || this.mImpl.mUser != this.mCurUser || !this.mImpl.mComponent.equals(serviceComponent)) {
                    unloadAllKeyphraseModels();
                    if (this.mImpl != null) {
                        this.mImpl.shutdownLocked();
                    }
                    if (hasComponent) {
                        VoiceInteractionManagerServiceImpl voiceInteractionManagerServiceImpl = new VoiceInteractionManagerServiceImpl(VoiceInteractionManagerService.this.mContext, UiThread.getHandler(), this, this.mCurUser, serviceComponent);
                        setImplLocked(voiceInteractionManagerServiceImpl);
                        this.mImpl.startLocked();
                        return;
                    }
                    setImplLocked(null);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public VoiceInteractionServiceInfo findAvailInteractor(int userHandle, String packageName) {
            List<ResolveInfo> available = VoiceInteractionManagerService.this.mContext.getPackageManager().queryIntentServicesAsUser(new Intent("android.service.voice.VoiceInteractionService"), 269221888, userHandle);
            int numAvailable = available.size();
            if (numAvailable == 0) {
                Slog.w(VoiceInteractionManagerService.TAG, "no available voice interaction services found for user " + userHandle);
                return null;
            }
            VoiceInteractionServiceInfo foundInfo = null;
            for (int i = 0; i < numAvailable; i++) {
                ServiceInfo cur = available.get(i).serviceInfo;
                if ((cur.applicationInfo.flags & 1) != 0) {
                    ComponentName comp = new ComponentName(cur.packageName, cur.name);
                    try {
                        VoiceInteractionServiceInfo info = new VoiceInteractionServiceInfo(VoiceInteractionManagerService.this.mContext.getPackageManager(), comp, userHandle);
                        if (info.getParseError() != null) {
                            Slog.w(VoiceInteractionManagerService.TAG, "Bad interaction service " + comp + ": " + info.getParseError());
                        } else if (packageName == null || info.getServiceInfo().packageName.equals(packageName)) {
                            if (foundInfo == null) {
                                foundInfo = info;
                            } else {
                                Slog.w(VoiceInteractionManagerService.TAG, "More than one voice interaction service, picking first " + new ComponentName(foundInfo.getServiceInfo().packageName, foundInfo.getServiceInfo().name) + " over " + new ComponentName(cur.packageName, cur.name));
                            }
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        Slog.w(VoiceInteractionManagerService.TAG, "Failure looking up interaction service " + comp);
                    }
                }
            }
            return foundInfo;
        }

        /* access modifiers changed from: package-private */
        public ComponentName getCurInteractor(int userHandle) {
            String curInteractor = Settings.Secure.getStringForUser(VoiceInteractionManagerService.this.mContext.getContentResolver(), "voice_interaction_service", userHandle);
            if (TextUtils.isEmpty(curInteractor)) {
                return null;
            }
            return ComponentName.unflattenFromString(curInteractor);
        }

        /* access modifiers changed from: package-private */
        public void setCurInteractor(ComponentName comp, int userHandle) {
            Settings.Secure.putStringForUser(VoiceInteractionManagerService.this.mContext.getContentResolver(), "voice_interaction_service", comp != null ? comp.flattenToShortString() : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, userHandle);
        }

        /* access modifiers changed from: package-private */
        public ComponentName findAvailRecognizer(String prefPackage, int userHandle) {
            List<ResolveInfo> available = VoiceInteractionManagerService.this.mContext.getPackageManager().queryIntentServicesAsUser(new Intent("android.speech.RecognitionService"), 786432, userHandle);
            int numAvailable = available.size();
            if (numAvailable == 0) {
                Slog.w(VoiceInteractionManagerService.TAG, "no available voice recognition services found for user " + userHandle);
                return null;
            }
            if (prefPackage != null) {
                for (int i = 0; i < numAvailable; i++) {
                    ServiceInfo serviceInfo = available.get(i).serviceInfo;
                    if (prefPackage.equals(serviceInfo.packageName)) {
                        return new ComponentName(serviceInfo.packageName, serviceInfo.name);
                    }
                }
            }
            if (numAvailable > 1) {
                Slog.w(VoiceInteractionManagerService.TAG, "more than one voice recognition service found, picking first");
            }
            ServiceInfo serviceInfo2 = available.get(0).serviceInfo;
            return new ComponentName(serviceInfo2.packageName, serviceInfo2.name);
        }

        /* access modifiers changed from: package-private */
        public ComponentName getCurRecognizer(int userHandle) {
            String curRecognizer = Settings.Secure.getStringForUser(VoiceInteractionManagerService.this.mContext.getContentResolver(), "voice_recognition_service", userHandle);
            if (TextUtils.isEmpty(curRecognizer)) {
                return null;
            }
            return ComponentName.unflattenFromString(curRecognizer);
        }

        /* access modifiers changed from: package-private */
        public void setCurRecognizer(ComponentName comp, int userHandle) {
            Settings.Secure.putStringForUser(VoiceInteractionManagerService.this.mContext.getContentResolver(), "voice_recognition_service", comp != null ? comp.flattenToShortString() : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, userHandle);
        }

        /* access modifiers changed from: package-private */
        public ComponentName getCurAssistant(int userHandle) {
            String curAssistant = Settings.Secure.getStringForUser(VoiceInteractionManagerService.this.mContext.getContentResolver(), "assistant", userHandle);
            if (TextUtils.isEmpty(curAssistant)) {
                return null;
            }
            return ComponentName.unflattenFromString(curAssistant);
        }

        /* access modifiers changed from: package-private */
        public void resetCurAssistant(int userHandle) {
            Settings.Secure.putStringForUser(VoiceInteractionManagerService.this.mContext.getContentResolver(), "assistant", null, userHandle);
        }

        public void showSession(IVoiceInteractionService service, Bundle args, int flags) {
            synchronized (this) {
                if (this.mImpl == null || this.mImpl.mService == null || service == null || service.asBinder() != this.mImpl.mService.asBinder()) {
                    throw new SecurityException("Caller is not the current voice interaction service");
                }
                long caller = Binder.clearCallingIdentity();
                try {
                    this.mImpl.showSessionLocked(args, flags, null, null);
                } finally {
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public boolean deliverNewSession(IBinder token, IVoiceInteractionSession session, IVoiceInteractor interactor) {
            boolean deliverNewSessionLocked;
            synchronized (this) {
                if (this.mImpl != null) {
                    long caller = Binder.clearCallingIdentity();
                    try {
                        deliverNewSessionLocked = this.mImpl.deliverNewSessionLocked(token, session, interactor);
                    } finally {
                        Binder.restoreCallingIdentity(caller);
                    }
                } else {
                    throw new SecurityException("deliverNewSession without running voice interaction service");
                }
            }
            return deliverNewSessionLocked;
        }

        public boolean showSessionFromSession(IBinder token, Bundle sessionArgs, int flags) {
            synchronized (this) {
                if (this.mImpl == null) {
                    Slog.w(VoiceInteractionManagerService.TAG, "showSessionFromSession without running voice interaction service");
                    return false;
                }
                long caller = Binder.clearCallingIdentity();
                try {
                    boolean showSessionLocked = this.mImpl.showSessionLocked(sessionArgs, flags, null, null);
                    return showSessionLocked;
                } finally {
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public boolean hideSessionFromSession(IBinder token) {
            synchronized (this) {
                if (this.mImpl == null) {
                    Slog.w(VoiceInteractionManagerService.TAG, "hideSessionFromSession without running voice interaction service");
                    return false;
                }
                long caller = Binder.clearCallingIdentity();
                try {
                    boolean hideSessionLocked = this.mImpl.hideSessionLocked();
                    return hideSessionLocked;
                } finally {
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public int startVoiceActivity(IBinder token, Intent intent, String resolvedType) {
            synchronized (this) {
                if (this.mImpl == null) {
                    Slog.w(VoiceInteractionManagerService.TAG, "startVoiceActivity without running voice interaction service");
                    return -96;
                }
                int callingPid = Binder.getCallingPid();
                int callingUid = Binder.getCallingUid();
                long caller = Binder.clearCallingIdentity();
                try {
                    int startVoiceActivityLocked = this.mImpl.startVoiceActivityLocked(callingPid, callingUid, token, intent, resolvedType);
                    return startVoiceActivityLocked;
                } finally {
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public int startAssistantActivity(IBinder token, Intent intent, String resolvedType) {
            synchronized (this) {
                if (this.mImpl == null) {
                    Slog.w(VoiceInteractionManagerService.TAG, "startAssistantActivity without running voice interaction service");
                    return -96;
                }
                int callingPid = Binder.getCallingPid();
                int callingUid = Binder.getCallingUid();
                long caller = Binder.clearCallingIdentity();
                try {
                    int startAssistantActivityLocked = this.mImpl.startAssistantActivityLocked(callingPid, callingUid, token, intent, resolvedType);
                    return startAssistantActivityLocked;
                } finally {
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public void setKeepAwake(IBinder token, boolean keepAwake) {
            synchronized (this) {
                if (this.mImpl == null) {
                    Slog.w(VoiceInteractionManagerService.TAG, "setKeepAwake without running voice interaction service");
                    return;
                }
                long caller = Binder.clearCallingIdentity();
                try {
                    this.mImpl.setKeepAwakeLocked(token, keepAwake);
                } finally {
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public void closeSystemDialogs(IBinder token) {
            synchronized (this) {
                if (this.mImpl == null) {
                    Slog.w(VoiceInteractionManagerService.TAG, "closeSystemDialogs without running voice interaction service");
                    return;
                }
                long caller = Binder.clearCallingIdentity();
                try {
                    this.mImpl.closeSystemDialogsLocked(token);
                } finally {
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public void finish(IBinder token) {
            synchronized (this) {
                if (this.mImpl == null) {
                    Slog.w(VoiceInteractionManagerService.TAG, "finish without running voice interaction service");
                    return;
                }
                long caller = Binder.clearCallingIdentity();
                try {
                    this.mImpl.finishLocked(token, false);
                } finally {
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public void setDisabledShowContext(int flags) {
            synchronized (this) {
                if (this.mImpl == null) {
                    Slog.w(VoiceInteractionManagerService.TAG, "setDisabledShowContext without running voice interaction service");
                    return;
                }
                int callingUid = Binder.getCallingUid();
                long caller = Binder.clearCallingIdentity();
                try {
                    this.mImpl.setDisabledShowContextLocked(callingUid, flags);
                } finally {
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public int getDisabledShowContext() {
            synchronized (this) {
                if (this.mImpl == null) {
                    Slog.w(VoiceInteractionManagerService.TAG, "getDisabledShowContext without running voice interaction service");
                    return 0;
                }
                int callingUid = Binder.getCallingUid();
                long caller = Binder.clearCallingIdentity();
                try {
                    int disabledShowContextLocked = this.mImpl.getDisabledShowContextLocked(callingUid);
                    return disabledShowContextLocked;
                } finally {
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public int getUserDisabledShowContext() {
            synchronized (this) {
                if (this.mImpl == null) {
                    Slog.w(VoiceInteractionManagerService.TAG, "getUserDisabledShowContext without running voice interaction service");
                    return 0;
                }
                int callingUid = Binder.getCallingUid();
                long caller = Binder.clearCallingIdentity();
                try {
                    int userDisabledShowContextLocked = this.mImpl.getUserDisabledShowContextLocked(callingUid);
                    return userDisabledShowContextLocked;
                } finally {
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public SoundTrigger.KeyphraseSoundModel getKeyphraseSoundModel(int keyphraseId, String bcp47Locale) {
            enforceCallingPermission("android.permission.MANAGE_VOICE_KEYPHRASES");
            if (bcp47Locale != null) {
                int callingUid = UserHandle.getCallingUserId();
                long caller = Binder.clearCallingIdentity();
                try {
                    return VoiceInteractionManagerService.this.mDbHelper.getKeyphraseSoundModel(keyphraseId, callingUid, bcp47Locale);
                } finally {
                    Binder.restoreCallingIdentity(caller);
                }
            } else {
                throw new IllegalArgumentException("Illegal argument(s) in getKeyphraseSoundModel");
            }
        }

        public int updateKeyphraseSoundModel(SoundTrigger.KeyphraseSoundModel model) {
            enforceCallingPermission("android.permission.MANAGE_VOICE_KEYPHRASES");
            if (model != null) {
                long caller = Binder.clearCallingIdentity();
                try {
                    if (VoiceInteractionManagerService.this.mDbHelper.updateKeyphraseSoundModel(model)) {
                        synchronized (this) {
                            if (!(this.mImpl == null || this.mImpl.mService == null)) {
                                this.mImpl.notifySoundModelsChangedLocked();
                            }
                        }
                        Binder.restoreCallingIdentity(caller);
                        return 0;
                    }
                    Binder.restoreCallingIdentity(caller);
                    return Integer.MIN_VALUE;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(caller);
                    throw th;
                }
            } else {
                throw new IllegalArgumentException("Model must not be null");
            }
        }

        public int deleteKeyphraseSoundModel(int keyphraseId, String bcp47Locale) {
            enforceCallingPermission("android.permission.MANAGE_VOICE_KEYPHRASES");
            if (bcp47Locale != null) {
                int callingUid = UserHandle.getCallingUserId();
                long caller = Binder.clearCallingIdentity();
                int i = 0;
                try {
                    int unloadStatus = VoiceInteractionManagerService.this.mSoundTriggerInternal.unloadKeyphraseModel(keyphraseId);
                    if (unloadStatus != 0) {
                        Slog.w(VoiceInteractionManagerService.TAG, "Unable to unload keyphrase sound model:" + unloadStatus);
                    }
                    boolean deleted = VoiceInteractionManagerService.this.mDbHelper.deleteKeyphraseSoundModel(keyphraseId, callingUid, bcp47Locale);
                    if (!deleted) {
                        i = Integer.MIN_VALUE;
                    }
                    if (deleted) {
                        synchronized (this) {
                            if (!(this.mImpl == null || this.mImpl.mService == null)) {
                                this.mImpl.notifySoundModelsChangedLocked();
                            }
                            VoiceInteractionManagerService.this.mLoadedKeyphraseIds.remove(Integer.valueOf(keyphraseId));
                        }
                    }
                    Binder.restoreCallingIdentity(caller);
                    return i;
                } catch (Throwable th) {
                    if (0 != 0) {
                        synchronized (this) {
                            if (!(this.mImpl == null || this.mImpl.mService == null)) {
                                this.mImpl.notifySoundModelsChangedLocked();
                            }
                            VoiceInteractionManagerService.this.mLoadedKeyphraseIds.remove(Integer.valueOf(keyphraseId));
                        }
                    }
                    Binder.restoreCallingIdentity(caller);
                    throw th;
                }
            } else {
                throw new IllegalArgumentException("Illegal argument(s) in deleteKeyphraseSoundModel");
            }
        }

        public boolean isEnrolledForKeyphrase(IVoiceInteractionService service, int keyphraseId, String bcp47Locale) {
            synchronized (this) {
                if (this.mImpl == null || this.mImpl.mService == null || service == null || service.asBinder() != this.mImpl.mService.asBinder()) {
                    throw new SecurityException("Caller is not the current voice interaction service");
                }
            }
            if (bcp47Locale != null) {
                int callingUid = UserHandle.getCallingUserId();
                long caller = Binder.clearCallingIdentity();
                try {
                    return VoiceInteractionManagerService.this.mDbHelper.getKeyphraseSoundModel(keyphraseId, callingUid, bcp47Locale) != null;
                } finally {
                    Binder.restoreCallingIdentity(caller);
                }
            } else {
                throw new IllegalArgumentException("Illegal argument(s) in isEnrolledForKeyphrase");
            }
        }

        public SoundTrigger.ModuleProperties getDspModuleProperties(IVoiceInteractionService service) {
            SoundTrigger.ModuleProperties moduleProperties;
            synchronized (this) {
                if (this.mImpl == null || this.mImpl.mService == null || service == null || service.asBinder() != this.mImpl.mService.asBinder()) {
                    throw new SecurityException("Caller is not the current voice interaction service");
                }
                long caller = Binder.clearCallingIdentity();
                try {
                    moduleProperties = VoiceInteractionManagerService.this.mSoundTriggerInternal.getModuleProperties();
                } finally {
                    Binder.restoreCallingIdentity(caller);
                }
            }
            return moduleProperties;
        }

        public int startRecognition(IVoiceInteractionService service, int keyphraseId, String bcp47Locale, IRecognitionStatusCallback callback, SoundTrigger.RecognitionConfig recognitionConfig) {
            synchronized (this) {
                if (this.mImpl == null || this.mImpl.mService == null || service == null || service.asBinder() != this.mImpl.mService.asBinder()) {
                    throw new SecurityException("Caller is not the current voice interaction service");
                } else if (callback == null || recognitionConfig == null || bcp47Locale == null) {
                    throw new IllegalArgumentException("Illegal argument(s) in startRecognition");
                }
            }
            int callingUid = UserHandle.getCallingUserId();
            long caller = Binder.clearCallingIdentity();
            try {
                SoundTrigger.KeyphraseSoundModel soundModel = VoiceInteractionManagerService.this.mDbHelper.getKeyphraseSoundModel(keyphraseId, callingUid, bcp47Locale);
                if (!(soundModel == null || soundModel.uuid == null)) {
                    if (soundModel.keyphrases != null) {
                        synchronized (this) {
                            VoiceInteractionManagerService.this.mLoadedKeyphraseIds.add(Integer.valueOf(keyphraseId));
                        }
                        int startRecognition = VoiceInteractionManagerService.this.mSoundTriggerInternal.startRecognition(keyphraseId, soundModel, callback, recognitionConfig);
                        Binder.restoreCallingIdentity(caller);
                        return startRecognition;
                    }
                }
                Slog.w(VoiceInteractionManagerService.TAG, "No matching sound model found in startRecognition");
                Binder.restoreCallingIdentity(caller);
                return Integer.MIN_VALUE;
            } catch (Throwable soundModel2) {
                Binder.restoreCallingIdentity(caller);
                throw soundModel2;
            }
        }

        public int stopRecognition(IVoiceInteractionService service, int keyphraseId, IRecognitionStatusCallback callback) {
            synchronized (this) {
                if (this.mImpl == null || this.mImpl.mService == null || service == null || service.asBinder() != this.mImpl.mService.asBinder()) {
                    throw new SecurityException("Caller is not the current voice interaction service");
                }
            }
            long caller = Binder.clearCallingIdentity();
            try {
                return VoiceInteractionManagerService.this.mSoundTriggerInternal.stopRecognition(keyphraseId, callback);
            } finally {
                Binder.restoreCallingIdentity(caller);
            }
        }

        /* JADX INFO: finally extract failed */
        /* access modifiers changed from: private */
        public synchronized void unloadAllKeyphraseModels() {
            int i = 0;
            while (i < VoiceInteractionManagerService.this.mLoadedKeyphraseIds.size()) {
                long caller = Binder.clearCallingIdentity();
                try {
                    int status = VoiceInteractionManagerService.this.mSoundTriggerInternal.unloadKeyphraseModel(VoiceInteractionManagerService.this.mLoadedKeyphraseIds.valueAt(i).intValue());
                    if (status != 0) {
                        Slog.w(VoiceInteractionManagerService.TAG, "Failed to unload keyphrase " + VoiceInteractionManagerService.this.mLoadedKeyphraseIds.valueAt(i) + ":" + status);
                    }
                    Binder.restoreCallingIdentity(caller);
                    i++;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(caller);
                    throw th;
                }
            }
            VoiceInteractionManagerService.this.mLoadedKeyphraseIds.clear();
        }

        public ComponentName getActiveServiceComponentName() {
            ComponentName componentName;
            enforceCallingPermission("android.permission.ACCESS_VOICE_INTERACTION_SERVICE");
            synchronized (this) {
                componentName = this.mImpl != null ? this.mImpl.mComponent : null;
            }
            return componentName;
        }

        public boolean showSessionForActiveService(Bundle args, int sourceFlags, IVoiceInteractionSessionShowCallback showCallback, IBinder activityToken) {
            enforceCallingPermission("android.permission.ACCESS_VOICE_INTERACTION_SERVICE");
            synchronized (this) {
                if (this.mImpl == null) {
                    Slog.w(VoiceInteractionManagerService.TAG, "showSessionForActiveService without running voice interactionservice");
                    return false;
                }
                long caller = Binder.clearCallingIdentity();
                try {
                    boolean showSessionLocked = this.mImpl.showSessionLocked(args, sourceFlags | 1 | 2, showCallback, activityToken);
                    return showSessionLocked;
                } finally {
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public void hideCurrentSession() throws RemoteException {
            enforceCallingPermission("android.permission.ACCESS_VOICE_INTERACTION_SERVICE");
            synchronized (this) {
                if (this.mImpl != null) {
                    long caller = Binder.clearCallingIdentity();
                    try {
                        if (!(this.mImpl.mActiveSession == null || this.mImpl.mActiveSession.mSession == null)) {
                            this.mImpl.mActiveSession.mSession.closeSystemDialogs();
                        }
                    } catch (RemoteException e) {
                        Log.w(VoiceInteractionManagerService.TAG, "Failed to call closeSystemDialogs", e);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(caller);
                        throw th;
                    }
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public void launchVoiceAssistFromKeyguard() {
            enforceCallingPermission("android.permission.ACCESS_VOICE_INTERACTION_SERVICE");
            synchronized (this) {
                if (this.mImpl == null) {
                    Slog.w(VoiceInteractionManagerService.TAG, "launchVoiceAssistFromKeyguard without running voice interactionservice");
                    return;
                }
                long caller = Binder.clearCallingIdentity();
                try {
                    this.mImpl.launchVoiceAssistFromKeyguard();
                } finally {
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public boolean isSessionRunning() {
            boolean z;
            enforceCallingPermission("android.permission.ACCESS_VOICE_INTERACTION_SERVICE");
            synchronized (this) {
                z = (this.mImpl == null || this.mImpl.mActiveSession == null) ? false : true;
            }
            return z;
        }

        public boolean activeServiceSupportsAssist() {
            boolean z;
            enforceCallingPermission("android.permission.ACCESS_VOICE_INTERACTION_SERVICE");
            synchronized (this) {
                z = (this.mImpl == null || this.mImpl.mInfo == null || !this.mImpl.mInfo.getSupportsAssist()) ? false : true;
            }
            return z;
        }

        public boolean activeServiceSupportsLaunchFromKeyguard() throws RemoteException {
            boolean z;
            enforceCallingPermission("android.permission.ACCESS_VOICE_INTERACTION_SERVICE");
            synchronized (this) {
                z = (this.mImpl == null || this.mImpl.mInfo == null || !this.mImpl.mInfo.getSupportsLaunchFromKeyguard()) ? false : true;
            }
            return z;
        }

        public void onLockscreenShown() {
            enforceCallingPermission("android.permission.ACCESS_VOICE_INTERACTION_SERVICE");
            synchronized (this) {
                if (this.mImpl != null) {
                    long caller = Binder.clearCallingIdentity();
                    try {
                        if (!(this.mImpl.mActiveSession == null || this.mImpl.mActiveSession.mSession == null)) {
                            this.mImpl.mActiveSession.mSession.onLockscreenShown();
                        }
                    } catch (RemoteException e) {
                        Log.w(VoiceInteractionManagerService.TAG, "Failed to call onLockscreenShown", e);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(caller);
                        throw th;
                    }
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public void registerVoiceInteractionSessionListener(IVoiceInteractionSessionListener listener) {
            enforceCallingPermission("android.permission.ACCESS_VOICE_INTERACTION_SERVICE");
            synchronized (this) {
                VoiceInteractionManagerService.this.mVoiceInteractionSessionListeners.register(listener);
            }
        }

        public void onSessionShown() {
            synchronized (this) {
                int size = VoiceInteractionManagerService.this.mVoiceInteractionSessionListeners.beginBroadcast();
                for (int i = 0; i < size; i++) {
                    try {
                        VoiceInteractionManagerService.this.mVoiceInteractionSessionListeners.getBroadcastItem(i).onVoiceSessionShown();
                    } catch (RemoteException e) {
                        Slog.e(VoiceInteractionManagerService.TAG, "Error delivering voice interaction open event.", e);
                    }
                }
                VoiceInteractionManagerService.this.mVoiceInteractionSessionListeners.finishBroadcast();
            }
        }

        public void onSessionHidden() {
            synchronized (this) {
                int size = VoiceInteractionManagerService.this.mVoiceInteractionSessionListeners.beginBroadcast();
                for (int i = 0; i < size; i++) {
                    try {
                        VoiceInteractionManagerService.this.mVoiceInteractionSessionListeners.getBroadcastItem(i).onVoiceSessionHidden();
                    } catch (RemoteException e) {
                        Slog.e(VoiceInteractionManagerService.TAG, "Error delivering voice interaction closed event.", e);
                    }
                }
                VoiceInteractionManagerService.this.mVoiceInteractionSessionListeners.finishBroadcast();
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(VoiceInteractionManagerService.this.mContext, VoiceInteractionManagerService.TAG, pw)) {
                synchronized (this) {
                    pw.println("VOICE INTERACTION MANAGER (dumpsys voiceinteraction)");
                    pw.println("  mEnableService: " + this.mEnableService);
                    if (this.mImpl == null) {
                        pw.println("  (No active implementation)");
                        return;
                    }
                    this.mImpl.dumpLocked(fd, pw, args);
                    VoiceInteractionManagerService.this.mSoundTriggerInternal.dump(fd, pw, args);
                }
            }
        }

        private void enforceCallingPermission(String permission) {
            if (VoiceInteractionManagerService.this.mContext.checkCallingOrSelfPermission(permission) != 0) {
                throw new SecurityException("Caller does not hold the permission " + permission);
            }
        }

        /* access modifiers changed from: private */
        public void setImplLocked(VoiceInteractionManagerServiceImpl impl) {
            this.mImpl = impl;
            VoiceInteractionManagerService.this.mAmInternal.notifyActiveVoiceInteractionServiceChanged(getActiveServiceComponentName());
        }
    }

    public VoiceInteractionManagerService(Context context) {
        super(context);
        this.mContext = context;
        this.mResolver = context.getContentResolver();
        this.mDbHelper = new DatabaseHelper(context);
        this.mServiceStub = new VoiceInteractionManagerServiceStub();
        this.mAmInternal = (ActivityManagerInternal) Preconditions.checkNotNull((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class));
        this.mUserManager = (UserManager) Preconditions.checkNotNull((UserManager) context.getSystemService(UserManager.class));
        ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).setVoiceInteractionPackagesProvider(new PackageManagerInternal.PackagesProvider() {
            public String[] getPackages(int userId) {
                VoiceInteractionManagerService.this.mServiceStub.initForUser(userId);
                ComponentName interactor = VoiceInteractionManagerService.this.mServiceStub.getCurInteractor(userId);
                if (interactor == null) {
                    return null;
                }
                return new String[]{interactor.getPackageName()};
            }
        });
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [com.android.server.voiceinteraction.VoiceInteractionManagerService$VoiceInteractionManagerServiceStub, android.os.IBinder] */
    public void onStart() {
        publishBinderService("voiceinteraction", this.mServiceStub);
        publishLocalService(VoiceInteractionManagerInternal.class, new LocalService());
    }

    public void onBootPhase(int phase) {
        if (500 == phase) {
            this.mShortcutServiceInternal = (ShortcutServiceInternal) Preconditions.checkNotNull((ShortcutServiceInternal) LocalServices.getService(ShortcutServiceInternal.class));
            this.mSoundTriggerInternal = (SoundTriggerInternal) LocalServices.getService(SoundTriggerInternal.class);
        } else if (phase == 600) {
            this.mServiceStub.systemRunning(isSafeMode());
        }
    }

    public void onStartUser(int userHandle) {
        this.mServiceStub.initForUser(userHandle);
    }

    public void onUnlockUser(int userHandle) {
        this.mServiceStub.initForUser(userHandle);
        this.mServiceStub.switchImplementationIfNeeded(false);
    }

    public void onSwitchUser(int userHandle) {
        this.mServiceStub.switchUser(userHandle);
    }
}

package com.android.server.voiceinteraction;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageManagerInternal.PackagesProvider;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.soundtrigger.IRecognitionStatusCallback;
import android.hardware.soundtrigger.SoundTrigger.KeyphraseSoundModel;
import android.hardware.soundtrigger.SoundTrigger.ModuleProperties;
import android.hardware.soundtrigger.SoundTrigger.RecognitionConfig;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.service.voice.IVoiceInteractionService;
import android.service.voice.IVoiceInteractionSession;
import android.service.voice.VoiceInteractionManagerInternal;
import android.service.voice.VoiceInteractionServiceInfo;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.internal.app.IVoiceInteractionManagerService.Stub;
import com.android.internal.app.IVoiceInteractionSessionShowCallback;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.BackgroundThread;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.UiThread;
import com.android.server.soundtrigger.SoundTriggerInternal;
import com.android.server.usb.UsbAudioDevice;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.TreeSet;

public class VoiceInteractionManagerService extends SystemService {
    static final boolean DEBUG = false;
    static final String TAG = "VoiceInteractionManagerService";
    final ActivityManagerInternal mAmInternal;
    final Context mContext;
    final DatabaseHelper mDbHelper;
    final TreeSet<Integer> mLoadedKeyphraseIds;
    final ContentResolver mResolver;
    private final VoiceInteractionManagerServiceStub mServiceStub;
    SoundTriggerInternal mSoundTriggerInternal;

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

    class VoiceInteractionManagerServiceStub extends Stub {
        private int mCurUser;
        private final boolean mEnableService;
        VoiceInteractionManagerServiceImpl mImpl;
        PackageMonitor mPackageMonitor;
        private boolean mSafeMode;

        /* renamed from: com.android.server.voiceinteraction.VoiceInteractionManagerService.VoiceInteractionManagerServiceStub.2 */
        class AnonymousClass2 extends IVoiceInteractionSessionShowCallback.Stub {
            final /* synthetic */ IBinder val$token;

            AnonymousClass2(IBinder val$token) {
                this.val$token = val$token;
            }

            public void onFailed() {
            }

            public void onShown() {
                VoiceInteractionManagerService.this.mAmInternal.onLocalVoiceInteractionStarted(this.val$token, VoiceInteractionManagerServiceStub.this.mImpl.mActiveSession.mSession, VoiceInteractionManagerServiceStub.this.mImpl.mActiveSession.mInteractor);
            }
        }

        class SettingsObserver extends ContentObserver {
            SettingsObserver(Handler handler) {
                super(handler);
                VoiceInteractionManagerService.this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("voice_interaction_service"), VoiceInteractionManagerService.DEBUG, this, -1);
            }

            public void onChange(boolean selfChange) {
                synchronized (VoiceInteractionManagerServiceStub.this) {
                    VoiceInteractionManagerServiceStub.this.switchImplementationIfNeededLocked(VoiceInteractionManagerService.DEBUG);
                }
            }
        }

        private synchronized void unloadAllKeyphraseModels() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:20:0x0050 in {10, 12, 14, 16, 19, 21, 24} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r8 = this;
            monitor-enter(r8);
            r5 = com.android.server.voiceinteraction.VoiceInteractionManagerService.this;	 Catch:{ all -> 0x0053 }
            r5 = r5.mLoadedKeyphraseIds;	 Catch:{ all -> 0x0053 }
            r3 = r5.iterator();	 Catch:{ all -> 0x0053 }
        L_0x0009:
            r5 = r3.hasNext();	 Catch:{ all -> 0x0053 }
            if (r5 == 0) goto L_0x0058;	 Catch:{ all -> 0x0053 }
        L_0x000f:
            r5 = r3.next();	 Catch:{ all -> 0x0053 }
            r5 = (java.lang.Integer) r5;	 Catch:{ all -> 0x0053 }
            r2 = r5.intValue();	 Catch:{ all -> 0x0053 }
            r0 = android.os.Binder.clearCallingIdentity();	 Catch:{ all -> 0x0053 }
            r5 = com.android.server.voiceinteraction.VoiceInteractionManagerService.this;	 Catch:{ all -> 0x0053 }
            r5 = r5.mSoundTriggerInternal;	 Catch:{ all -> 0x0053 }
            r4 = r5.unloadKeyphraseModel(r2);	 Catch:{ all -> 0x0053 }
            if (r4 == 0) goto L_0x004c;	 Catch:{ all -> 0x0053 }
        L_0x0027:
            r5 = "VoiceInteractionManagerService";	 Catch:{ all -> 0x0053 }
            r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0053 }
            r6.<init>();	 Catch:{ all -> 0x0053 }
            r7 = "Failed to unload keyphrase ";	 Catch:{ all -> 0x0053 }
            r6 = r6.append(r7);	 Catch:{ all -> 0x0053 }
            r6 = r6.append(r2);	 Catch:{ all -> 0x0053 }
            r7 = ":";	 Catch:{ all -> 0x0053 }
            r6 = r6.append(r7);	 Catch:{ all -> 0x0053 }
            r6 = r6.append(r4);	 Catch:{ all -> 0x0053 }
            r6 = r6.toString();	 Catch:{ all -> 0x0053 }
            android.util.Slog.w(r5, r6);	 Catch:{ all -> 0x0053 }
        L_0x004c:
            android.os.Binder.restoreCallingIdentity(r0);
            goto L_0x0009;
        L_0x0050:
            r5 = move-exception;
            monitor-exit(r8);
            throw r5;
        L_0x0053:
            r5 = move-exception;
            android.os.Binder.restoreCallingIdentity(r0);	 Catch:{ all -> 0x0053 }
            throw r5;	 Catch:{ all -> 0x0053 }
        L_0x0058:
            r5 = com.android.server.voiceinteraction.VoiceInteractionManagerService.this;	 Catch:{ all -> 0x0053 }
            r5 = r5.mLoadedKeyphraseIds;	 Catch:{ all -> 0x0053 }
            r5.clear();	 Catch:{ all -> 0x0053 }
            monitor-exit(r8);
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.voiceinteraction.VoiceInteractionManagerService.VoiceInteractionManagerServiceStub.unloadAllKeyphraseModels():void");
        }

        VoiceInteractionManagerServiceStub() {
            this.mPackageMonitor = new PackageMonitor() {
                public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
                    int userHandle = UserHandle.getUserId(uid);
                    ComponentName curInteractor = VoiceInteractionManagerServiceStub.this.getCurInteractor(userHandle);
                    ComponentName curRecognizer = VoiceInteractionManagerServiceStub.this.getCurRecognizer(userHandle);
                    boolean hit = VoiceInteractionManagerService.DEBUG;
                    int i = 0;
                    int length = packages.length;
                    while (i < length) {
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
                                VoiceInteractionManagerServiceStub.this.mImpl = null;
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

                public void onSomePackagesChanged() {
                    int userHandle = getChangingUserId();
                    synchronized (VoiceInteractionManagerServiceStub.this) {
                        ComponentName curInteractor = VoiceInteractionManagerServiceStub.this.getCurInteractor(userHandle);
                        ComponentName curRecognizer = VoiceInteractionManagerServiceStub.this.getCurRecognizer(userHandle);
                        if (curRecognizer == null) {
                            if (anyPackagesAppearing()) {
                                curRecognizer = VoiceInteractionManagerServiceStub.this.findAvailRecognizer(null, userHandle);
                                if (curRecognizer != null) {
                                    VoiceInteractionManagerServiceStub.this.setCurRecognizer(curRecognizer, userHandle);
                                }
                            }
                            return;
                        }
                        if (curInteractor == null) {
                            int change = isPackageDisappearing(curRecognizer.getPackageName());
                            if (change == 3 || change == 2) {
                                VoiceInteractionManagerServiceStub.this.setCurRecognizer(VoiceInteractionManagerServiceStub.this.findAvailRecognizer(null, userHandle), userHandle);
                            } else if (isPackageModified(curRecognizer.getPackageName())) {
                                VoiceInteractionManagerServiceStub.this.setCurRecognizer(VoiceInteractionManagerServiceStub.this.findAvailRecognizer(curRecognizer.getPackageName(), userHandle), userHandle);
                            }
                            return;
                        } else if (isPackageDisappearing(curInteractor.getPackageName()) == 3) {
                            VoiceInteractionManagerServiceStub.this.setCurInteractor(null, userHandle);
                            VoiceInteractionManagerServiceStub.this.setCurRecognizer(null, userHandle);
                            VoiceInteractionManagerServiceStub.this.initForUser(userHandle);
                            return;
                        } else {
                            if (!(isPackageAppearing(curInteractor.getPackageName()) == 0 || VoiceInteractionManagerServiceStub.this.mImpl == null || !curInteractor.getPackageName().equals(VoiceInteractionManagerServiceStub.this.mImpl.mComponent.getPackageName()))) {
                                VoiceInteractionManagerServiceStub.this.switchImplementationIfNeededLocked(true);
                            }
                            return;
                        }
                    }
                }
            };
            this.mEnableService = shouldEnableService(VoiceInteractionManagerService.this.mContext.getResources());
        }

        void startLocalVoiceInteraction(IBinder token, Bundle options) {
            if (this.mImpl != null) {
                long caller = Binder.clearCallingIdentity();
                try {
                    this.mImpl.showSessionLocked(options, 16, new AnonymousClass2(token), token);
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
                return VoiceInteractionManagerService.DEBUG;
            }
            return this.mImpl.supportsLocalVoiceInteraction();
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            try {
                return super.onTransact(code, data, reply, flags);
            } catch (RuntimeException e) {
                if (!(e instanceof SecurityException)) {
                    Slog.wtf(VoiceInteractionManagerService.TAG, "VoiceInteractionManagerService Crash", e);
                }
                throw e;
            }
        }

        public void initForUser(int userHandle) {
            String curInteractorStr = Secure.getStringForUser(VoiceInteractionManagerService.this.mContext.getContentResolver(), "voice_interaction_service", userHandle);
            ComponentName curRecognizer = getCurRecognizer(userHandle);
            VoiceInteractionServiceInfo voiceInteractionServiceInfo = null;
            if (curInteractorStr == null && curRecognizer != null && this.mEnableService) {
                voiceInteractionServiceInfo = findAvailInteractor(userHandle, curRecognizer.getPackageName());
                if (voiceInteractionServiceInfo != null) {
                    curRecognizer = null;
                }
            }
            String forceInteractorPackage = getForceVoiceInteractionServicePackage(VoiceInteractionManagerService.this.mContext.getResources());
            if (forceInteractorPackage != null) {
                voiceInteractionServiceInfo = findAvailInteractor(userHandle, forceInteractorPackage);
                if (voiceInteractionServiceInfo != null) {
                    curRecognizer = null;
                }
            }
            if (!(this.mEnableService || curInteractorStr == null || TextUtils.isEmpty(curInteractorStr))) {
                setCurInteractor(null, userHandle);
                curInteractorStr = "";
            }
            if (curRecognizer != null) {
                IPackageManager pm = AppGlobals.getPackageManager();
                ServiceInfo interactorInfo = null;
                ServiceInfo recognizerInfo = null;
                ComponentName unflattenFromString = !TextUtils.isEmpty(curInteractorStr) ? ComponentName.unflattenFromString(curInteractorStr) : null;
                try {
                    recognizerInfo = pm.getServiceInfo(curRecognizer, 0, userHandle);
                    if (unflattenFromString != null) {
                        interactorInfo = pm.getServiceInfo(unflattenFromString, 0, userHandle);
                    }
                } catch (RemoteException e) {
                }
                if (recognizerInfo != null && (unflattenFromString == null || r6 != null)) {
                    return;
                }
            }
            if (voiceInteractionServiceInfo == null && this.mEnableService) {
                voiceInteractionServiceInfo = findAvailInteractor(userHandle, null);
            }
            if (voiceInteractionServiceInfo != null) {
                setCurInteractor(new ComponentName(voiceInteractionServiceInfo.getServiceInfo().packageName, voiceInteractionServiceInfo.getServiceInfo().name), userHandle);
                if (voiceInteractionServiceInfo.getRecognitionService() != null) {
                    setCurRecognizer(new ComponentName(voiceInteractionServiceInfo.getServiceInfo().packageName, voiceInteractionServiceInfo.getRecognitionService()), userHandle);
                    return;
                }
            }
            curRecognizer = findAvailRecognizer(null, userHandle);
            if (curRecognizer != null) {
                if (voiceInteractionServiceInfo == null) {
                    setCurInteractor(null, userHandle);
                }
                setCurRecognizer(curRecognizer, userHandle);
            }
        }

        private boolean shouldEnableService(Resources res) {
            if (ActivityManager.isLowRamDeviceStatic() && getForceVoiceInteractionServicePackage(res) == null) {
                return VoiceInteractionManagerService.DEBUG;
            }
            return true;
        }

        private String getForceVoiceInteractionServicePackage(Resources res) {
            String interactorPackage = res.getString(17039464);
            return TextUtils.isEmpty(interactorPackage) ? null : interactorPackage;
        }

        public void systemRunning(boolean safeMode) {
            this.mSafeMode = safeMode;
            this.mPackageMonitor.register(VoiceInteractionManagerService.this.mContext, BackgroundThread.getHandler().getLooper(), UserHandle.ALL, true);
            SettingsObserver settingsObserver = new SettingsObserver(UiThread.getHandler());
            synchronized (this) {
                this.mCurUser = ActivityManager.getCurrentUser();
                switchImplementationIfNeededLocked(VoiceInteractionManagerService.DEBUG);
            }
        }

        public void switchUser(int userHandle) {
            synchronized (this) {
                this.mCurUser = userHandle;
                switchImplementationIfNeededLocked(VoiceInteractionManagerService.DEBUG);
            }
        }

        void switchImplementationIfNeeded(boolean force) {
            synchronized (this) {
                switchImplementationIfNeededLocked(force);
            }
        }

        void switchImplementationIfNeededLocked(boolean force) {
            if (!this.mSafeMode) {
                String curService = Secure.getStringForUser(VoiceInteractionManagerService.this.mResolver, "voice_interaction_service", this.mCurUser);
                ComponentName serviceComponent = null;
                ServiceInfo serviceInfo = null;
                if (!(curService == null || curService.isEmpty())) {
                    try {
                        serviceComponent = ComponentName.unflattenFromString(curService);
                        serviceInfo = AppGlobals.getPackageManager().getServiceInfo(serviceComponent, 0, this.mCurUser);
                    } catch (Exception e) {
                        Slog.wtf(VoiceInteractionManagerService.TAG, "Bad voice interaction service name " + curService, e);
                        serviceComponent = null;
                        serviceInfo = null;
                    }
                }
                if (force || this.mImpl == null || this.mImpl.mUser != this.mCurUser || !this.mImpl.mComponent.equals(serviceComponent)) {
                    unloadAllKeyphraseModels();
                    if (this.mImpl != null) {
                        this.mImpl.shutdownLocked();
                    }
                    if (serviceComponent == null || r8 == null) {
                        this.mImpl = null;
                        return;
                    }
                    this.mImpl = new VoiceInteractionManagerServiceImpl(VoiceInteractionManagerService.this.mContext, UiThread.getHandler(), this, this.mCurUser, serviceComponent);
                    this.mImpl.startLocked();
                }
            }
        }

        VoiceInteractionServiceInfo findAvailInteractor(int userHandle, String packageName) {
            List<ResolveInfo> available = VoiceInteractionManagerService.this.mContext.getPackageManager().queryIntentServicesAsUser(new Intent("android.service.voice.VoiceInteractionService"), 269221888, userHandle);
            int numAvailable = available.size();
            if (numAvailable == 0) {
                Slog.w(VoiceInteractionManagerService.TAG, "no available voice interaction services found for user " + userHandle);
                return null;
            }
            VoiceInteractionServiceInfo foundInfo = null;
            for (int i = 0; i < numAvailable; i++) {
                ServiceInfo cur = ((ResolveInfo) available.get(i)).serviceInfo;
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
                    } catch (NameNotFoundException e) {
                        Slog.w(VoiceInteractionManagerService.TAG, "Failure looking up interaction service " + comp);
                    }
                }
            }
            return foundInfo;
        }

        ComponentName getCurInteractor(int userHandle) {
            String curInteractor = Secure.getStringForUser(VoiceInteractionManagerService.this.mContext.getContentResolver(), "voice_interaction_service", userHandle);
            if (TextUtils.isEmpty(curInteractor)) {
                return null;
            }
            return ComponentName.unflattenFromString(curInteractor);
        }

        void setCurInteractor(ComponentName comp, int userHandle) {
            Secure.putStringForUser(VoiceInteractionManagerService.this.mContext.getContentResolver(), "voice_interaction_service", comp != null ? comp.flattenToShortString() : "", userHandle);
        }

        ComponentName findAvailRecognizer(String prefPackage, int userHandle) {
            List<ResolveInfo> available = VoiceInteractionManagerService.this.mContext.getPackageManager().queryIntentServicesAsUser(new Intent("android.speech.RecognitionService"), 0, userHandle);
            int numAvailable = available.size();
            if (numAvailable == 0) {
                Slog.w(VoiceInteractionManagerService.TAG, "no available voice recognition services found for user " + userHandle);
                return null;
            }
            ServiceInfo serviceInfo;
            if (prefPackage != null) {
                for (int i = 0; i < numAvailable; i++) {
                    serviceInfo = ((ResolveInfo) available.get(i)).serviceInfo;
                    if (prefPackage.equals(serviceInfo.packageName)) {
                        return new ComponentName(serviceInfo.packageName, serviceInfo.name);
                    }
                }
            }
            if (numAvailable > 1) {
                Slog.w(VoiceInteractionManagerService.TAG, "more than one voice recognition service found, picking first");
            }
            serviceInfo = ((ResolveInfo) available.get(0)).serviceInfo;
            return new ComponentName(serviceInfo.packageName, serviceInfo.name);
        }

        ComponentName getCurRecognizer(int userHandle) {
            String curRecognizer = Secure.getStringForUser(VoiceInteractionManagerService.this.mContext.getContentResolver(), "voice_recognition_service", userHandle);
            if (TextUtils.isEmpty(curRecognizer)) {
                return null;
            }
            return ComponentName.unflattenFromString(curRecognizer);
        }

        void setCurRecognizer(ComponentName comp, int userHandle) {
            Secure.putStringForUser(VoiceInteractionManagerService.this.mContext.getContentResolver(), "voice_recognition_service", comp != null ? comp.flattenToShortString() : "", userHandle);
        }

        void resetCurAssistant(int userHandle) {
            Secure.putStringForUser(VoiceInteractionManagerService.this.mContext.getContentResolver(), "assistant", null, userHandle);
        }

        public void showSession(IVoiceInteractionService service, Bundle args, int flags) {
            synchronized (this) {
                if (this.mImpl == null || this.mImpl.mService == null || service.asBinder() != this.mImpl.mService.asBinder()) {
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
                if (this.mImpl == null) {
                    throw new SecurityException("deliverNewSession without running voice interaction service");
                }
                long caller = Binder.clearCallingIdentity();
                try {
                    deliverNewSessionLocked = this.mImpl.deliverNewSessionLocked(token, session, interactor);
                } finally {
                    Binder.restoreCallingIdentity(caller);
                }
            }
            return deliverNewSessionLocked;
        }

        public boolean showSessionFromSession(IBinder token, Bundle sessionArgs, int flags) {
            synchronized (this) {
                if (this.mImpl == null) {
                    Slog.w(VoiceInteractionManagerService.TAG, "showSessionFromSession without running voice interaction service");
                    return VoiceInteractionManagerService.DEBUG;
                }
                long caller = Binder.clearCallingIdentity();
                try {
                    boolean showSessionLocked = this.mImpl.showSessionLocked(sessionArgs, flags, null, null);
                    Binder.restoreCallingIdentity(caller);
                    return showSessionLocked;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public boolean hideSessionFromSession(IBinder token) {
            synchronized (this) {
                if (this.mImpl == null) {
                    Slog.w(VoiceInteractionManagerService.TAG, "hideSessionFromSession without running voice interaction service");
                    return VoiceInteractionManagerService.DEBUG;
                }
                long caller = Binder.clearCallingIdentity();
                try {
                    boolean hideSessionLocked = this.mImpl.hideSessionLocked();
                    Binder.restoreCallingIdentity(caller);
                    return hideSessionLocked;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public int startVoiceActivity(IBinder token, Intent intent, String resolvedType) {
            synchronized (this) {
                if (this.mImpl == null) {
                    Slog.w(VoiceInteractionManagerService.TAG, "startVoiceActivity without running voice interaction service");
                    return -6;
                }
                int callingPid = Binder.getCallingPid();
                int callingUid = Binder.getCallingUid();
                long caller = Binder.clearCallingIdentity();
                try {
                    int startVoiceActivityLocked = this.mImpl.startVoiceActivityLocked(callingPid, callingUid, token, intent, resolvedType);
                    Binder.restoreCallingIdentity(caller);
                    return startVoiceActivityLocked;
                } catch (Throwable th) {
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
                    Binder.restoreCallingIdentity(caller);
                } catch (Throwable th) {
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
                    Binder.restoreCallingIdentity(caller);
                } catch (Throwable th) {
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
                    this.mImpl.finishLocked(token, VoiceInteractionManagerService.DEBUG);
                    Binder.restoreCallingIdentity(caller);
                } catch (Throwable th) {
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
                    Binder.restoreCallingIdentity(caller);
                } catch (Throwable th) {
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
                    Binder.restoreCallingIdentity(caller);
                    return disabledShowContextLocked;
                } catch (Throwable th) {
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
                    Binder.restoreCallingIdentity(caller);
                    return userDisabledShowContextLocked;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public KeyphraseSoundModel getKeyphraseSoundModel(int keyphraseId, String bcp47Locale) {
            enforceCallingPermission("android.permission.MANAGE_VOICE_KEYPHRASES");
            if (bcp47Locale == null) {
                throw new IllegalArgumentException("Illegal argument(s) in getKeyphraseSoundModel");
            }
            int callingUid = UserHandle.getCallingUserId();
            long caller = Binder.clearCallingIdentity();
            try {
                KeyphraseSoundModel keyphraseSoundModel = VoiceInteractionManagerService.this.mDbHelper.getKeyphraseSoundModel(keyphraseId, callingUid, bcp47Locale);
                return keyphraseSoundModel;
            } finally {
                Binder.restoreCallingIdentity(caller);
            }
        }

        public int updateKeyphraseSoundModel(KeyphraseSoundModel model) {
            enforceCallingPermission("android.permission.MANAGE_VOICE_KEYPHRASES");
            if (model == null) {
                throw new IllegalArgumentException("Model must not be null");
            }
            long caller = Binder.clearCallingIdentity();
            try {
                if (VoiceInteractionManagerService.this.mDbHelper.updateKeyphraseSoundModel(model)) {
                    synchronized (this) {
                        if (!(this.mImpl == null || this.mImpl.mService == null)) {
                            this.mImpl.notifySoundModelsChangedLocked();
                        }
                    }
                    return 0;
                }
                Binder.restoreCallingIdentity(caller);
                return UsbAudioDevice.kAudioDeviceMeta_Alsa;
            } finally {
                Binder.restoreCallingIdentity(caller);
            }
        }

        public int deleteKeyphraseSoundModel(int keyphraseId, String bcp47Locale) {
            int i = 0;
            enforceCallingPermission("android.permission.MANAGE_VOICE_KEYPHRASES");
            if (bcp47Locale == null) {
                throw new IllegalArgumentException("Illegal argument(s) in deleteKeyphraseSoundModel");
            }
            int callingUid = UserHandle.getCallingUserId();
            long caller = Binder.clearCallingIdentity();
            try {
                int unloadStatus = VoiceInteractionManagerService.this.mSoundTriggerInternal.unloadKeyphraseModel(keyphraseId);
                if (unloadStatus != 0) {
                    Slog.w(VoiceInteractionManagerService.TAG, "Unable to unload keyphrase sound model:" + unloadStatus);
                }
                boolean deleted = VoiceInteractionManagerService.this.mDbHelper.deleteKeyphraseSoundModel(keyphraseId, callingUid, bcp47Locale);
                if (!deleted) {
                    i = UsbAudioDevice.kAudioDeviceMeta_Alsa;
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
                if (VoiceInteractionManagerService.DEBUG) {
                    synchronized (this) {
                    }
                    if (!(this.mImpl == null || this.mImpl.mService == null)) {
                        this.mImpl.notifySoundModelsChangedLocked();
                    }
                    VoiceInteractionManagerService.this.mLoadedKeyphraseIds.remove(Integer.valueOf(keyphraseId));
                }
                Binder.restoreCallingIdentity(caller);
            }
        }

        public boolean isEnrolledForKeyphrase(IVoiceInteractionService service, int keyphraseId, String bcp47Locale) {
            synchronized (this) {
                if (!(this.mImpl == null || this.mImpl.mService == null)) {
                    if (service.asBinder() == this.mImpl.mService.asBinder()) {
                    }
                }
                throw new SecurityException("Caller is not the current voice interaction service");
            }
            if (bcp47Locale == null) {
                throw new IllegalArgumentException("Illegal argument(s) in isEnrolledForKeyphrase");
            }
            int callingUid = UserHandle.getCallingUserId();
            long caller = Binder.clearCallingIdentity();
            try {
                boolean z = VoiceInteractionManagerService.this.mDbHelper.getKeyphraseSoundModel(keyphraseId, callingUid, bcp47Locale) != null ? true : VoiceInteractionManagerService.DEBUG;
                Binder.restoreCallingIdentity(caller);
                return z;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(caller);
            }
        }

        public ModuleProperties getDspModuleProperties(IVoiceInteractionService service) {
            ModuleProperties moduleProperties;
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

        public int startRecognition(IVoiceInteractionService service, int keyphraseId, String bcp47Locale, IRecognitionStatusCallback callback, RecognitionConfig recognitionConfig) {
            synchronized (this) {
                if (!(this.mImpl == null || this.mImpl.mService == null || service == null)) {
                    if (service.asBinder() == this.mImpl.mService.asBinder()) {
                        if (callback == null || recognitionConfig == null || bcp47Locale == null) {
                            throw new IllegalArgumentException("Illegal argument(s) in startRecognition");
                        }
                    }
                }
                throw new SecurityException("Caller is not the current voice interaction service");
            }
            int callingUid = UserHandle.getCallingUserId();
            long caller = Binder.clearCallingIdentity();
            try {
                KeyphraseSoundModel soundModel = VoiceInteractionManagerService.this.mDbHelper.getKeyphraseSoundModel(keyphraseId, callingUid, bcp47Locale);
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
                return UsbAudioDevice.kAudioDeviceMeta_Alsa;
            } finally {
                Binder.restoreCallingIdentity(caller);
            }
        }

        public int stopRecognition(IVoiceInteractionService service, int keyphraseId, IRecognitionStatusCallback callback) {
            synchronized (this) {
                if (!(this.mImpl == null || this.mImpl.mService == null || service == null)) {
                    if (service.asBinder() == this.mImpl.mService.asBinder()) {
                    }
                }
                throw new SecurityException("Caller is not the current voice interaction service");
            }
            long caller = Binder.clearCallingIdentity();
            try {
                int stopRecognition = VoiceInteractionManagerService.this.mSoundTriggerInternal.stopRecognition(keyphraseId, callback);
                return stopRecognition;
            } finally {
                Binder.restoreCallingIdentity(caller);
            }
        }

        public ComponentName getActiveServiceComponentName() {
            ComponentName componentName = null;
            enforceCallingPermission("android.permission.ACCESS_VOICE_INTERACTION_SERVICE");
            synchronized (this) {
                if (this.mImpl != null) {
                    componentName = this.mImpl.mComponent;
                }
            }
            return componentName;
        }

        public boolean showSessionForActiveService(Bundle args, int sourceFlags, IVoiceInteractionSessionShowCallback showCallback, IBinder activityToken) {
            enforceCallingPermission("android.permission.ACCESS_VOICE_INTERACTION_SERVICE");
            synchronized (this) {
                if (this.mImpl == null) {
                    Slog.w(VoiceInteractionManagerService.TAG, "showSessionForActiveService without running voice interactionservice");
                    return VoiceInteractionManagerService.DEBUG;
                }
                long caller = Binder.clearCallingIdentity();
                try {
                    boolean showSessionLocked = this.mImpl.showSessionLocked(args, (sourceFlags | 1) | 2, showCallback, activityToken);
                    Binder.restoreCallingIdentity(caller);
                    return showSessionLocked;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public void hideCurrentSession() throws RemoteException {
            enforceCallingPermission("android.permission.ACCESS_VOICE_INTERACTION_SERVICE");
            synchronized (this) {
                if (this.mImpl == null) {
                    return;
                }
                long caller = Binder.clearCallingIdentity();
                try {
                    if (!(this.mImpl.mActiveSession == null || this.mImpl.mActiveSession.mSession == null)) {
                        this.mImpl.mActiveSession.mSession.closeSystemDialogs();
                    }
                } catch (RemoteException e) {
                    Log.w(VoiceInteractionManagerService.TAG, "Failed to call closeSystemDialogs", e);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(caller);
                }
                Binder.restoreCallingIdentity(caller);
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
                    Binder.restoreCallingIdentity(caller);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(caller);
                }
            }
        }

        public boolean isSessionRunning() {
            boolean z = VoiceInteractionManagerService.DEBUG;
            enforceCallingPermission("android.permission.ACCESS_VOICE_INTERACTION_SERVICE");
            synchronized (this) {
                if (!(this.mImpl == null || this.mImpl.mActiveSession == null)) {
                    z = true;
                }
            }
            return z;
        }

        public boolean activeServiceSupportsAssist() {
            boolean supportsAssist;
            enforceCallingPermission("android.permission.ACCESS_VOICE_INTERACTION_SERVICE");
            synchronized (this) {
                supportsAssist = (this.mImpl == null || this.mImpl.mInfo == null) ? VoiceInteractionManagerService.DEBUG : this.mImpl.mInfo.getSupportsAssist();
            }
            return supportsAssist;
        }

        public boolean activeServiceSupportsLaunchFromKeyguard() throws RemoteException {
            boolean z;
            enforceCallingPermission("android.permission.ACCESS_VOICE_INTERACTION_SERVICE");
            synchronized (this) {
                if (this.mImpl == null || this.mImpl.mInfo == null) {
                    z = VoiceInteractionManagerService.DEBUG;
                } else {
                    z = this.mImpl.mInfo.getSupportsLaunchFromKeyguard();
                }
            }
            return z;
        }

        public void onLockscreenShown() {
            enforceCallingPermission("android.permission.ACCESS_VOICE_INTERACTION_SERVICE");
            synchronized (this) {
                if (this.mImpl == null) {
                    return;
                }
                long caller = Binder.clearCallingIdentity();
                try {
                    if (!(this.mImpl.mActiveSession == null || this.mImpl.mActiveSession.mSession == null)) {
                        this.mImpl.mActiveSession.mSession.onLockscreenShown();
                    }
                } catch (RemoteException e) {
                    Log.w(VoiceInteractionManagerService.TAG, "Failed to call onLockscreenShown", e);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(caller);
                }
                Binder.restoreCallingIdentity(caller);
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (VoiceInteractionManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump PowerManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
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

        private void enforceCallingPermission(String permission) {
            if (VoiceInteractionManagerService.this.mContext.checkCallingOrSelfPermission(permission) != 0) {
                throw new SecurityException("Caller does not hold the permission " + permission);
            }
        }
    }

    public VoiceInteractionManagerService(Context context) {
        super(context);
        this.mContext = context;
        this.mResolver = context.getContentResolver();
        this.mDbHelper = new DatabaseHelper(context);
        this.mServiceStub = new VoiceInteractionManagerServiceStub();
        this.mAmInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mLoadedKeyphraseIds = new TreeSet();
        ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).setVoiceInteractionPackagesProvider(new PackagesProvider() {
            public String[] getPackages(int userId) {
                VoiceInteractionManagerService.this.mServiceStub.initForUser(userId);
                if (VoiceInteractionManagerService.this.mServiceStub.getCurInteractor(userId) == null) {
                    return null;
                }
                return new String[]{VoiceInteractionManagerService.this.mServiceStub.getCurInteractor(userId).getPackageName()};
            }
        });
    }

    public void onStart() {
        publishBinderService("voiceinteraction", this.mServiceStub);
        publishLocalService(VoiceInteractionManagerInternal.class, new LocalService());
    }

    public void onBootPhase(int phase) {
        if (SystemService.PHASE_SYSTEM_SERVICES_READY == phase) {
            this.mSoundTriggerInternal = (SoundTriggerInternal) LocalServices.getService(SoundTriggerInternal.class);
        } else if (phase == NetdResponseCode.InterfaceChange) {
            this.mServiceStub.systemRunning(isSafeMode());
        }
    }

    public void onStartUser(int userHandle) {
        this.mServiceStub.initForUser(userHandle);
    }

    public void onUnlockUser(int userHandle) {
        this.mServiceStub.initForUser(userHandle);
        this.mServiceStub.switchImplementationIfNeeded(DEBUG);
    }

    public void onSwitchUser(int userHandle) {
        this.mServiceStub.switchUser(userHandle);
    }
}

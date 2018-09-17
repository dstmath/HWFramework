package com.android.server.voiceinteraction;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityOptions;
import android.app.IActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.service.voice.IVoiceInteractionService;
import android.service.voice.IVoiceInteractionService.Stub;
import android.service.voice.IVoiceInteractionSession;
import android.service.voice.VoiceInteractionServiceInfo;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.view.IWindowManager;
import com.android.internal.app.IVoiceInteractionSessionShowCallback;
import com.android.internal.app.IVoiceInteractor;
import com.android.server.LocalServices;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.voiceinteraction.VoiceInteractionSessionConnection.Callback;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

class VoiceInteractionManagerServiceImpl implements Callback {
    static final String CLOSE_REASON_VOICE_INTERACTION = "voiceinteraction";
    static final String TAG = "VoiceInteractionServiceManager";
    VoiceInteractionSessionConnection mActiveSession;
    final IActivityManager mAm;
    boolean mBound = false;
    final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                String reason = intent.getStringExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY);
                if (!VoiceInteractionManagerServiceImpl.CLOSE_REASON_VOICE_INTERACTION.equals(reason) && ("dream".equals(reason) ^ 1) != 0) {
                    synchronized (VoiceInteractionManagerServiceImpl.this.mServiceStub) {
                        if (!(VoiceInteractionManagerServiceImpl.this.mActiveSession == null || VoiceInteractionManagerServiceImpl.this.mActiveSession.mSession == null)) {
                            try {
                                VoiceInteractionManagerServiceImpl.this.mActiveSession.mSession.closeSystemDialogs();
                            } catch (RemoteException e) {
                            }
                        }
                    }
                }
            }
        }
    };
    final ComponentName mComponent;
    final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (VoiceInteractionManagerServiceImpl.this.mServiceStub) {
                VoiceInteractionManagerServiceImpl.this.mService = Stub.asInterface(service);
                try {
                    VoiceInteractionManagerServiceImpl.this.mService.ready();
                } catch (RemoteException e) {
                }
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            VoiceInteractionManagerServiceImpl.this.mService = null;
        }
    };
    final Context mContext;
    int mDisabledShowContext;
    final Handler mHandler;
    final IWindowManager mIWindowManager;
    final VoiceInteractionServiceInfo mInfo;
    IVoiceInteractionService mService;
    final VoiceInteractionManagerServiceStub mServiceStub;
    final ComponentName mSessionComponentName;
    final int mUser;
    final boolean mValid;

    VoiceInteractionManagerServiceImpl(Context context, Handler handler, VoiceInteractionManagerServiceStub stub, int userHandle, ComponentName service) {
        this.mContext = context;
        this.mHandler = handler;
        this.mServiceStub = stub;
        this.mUser = userHandle;
        this.mComponent = service;
        this.mAm = ActivityManager.getService();
        try {
            this.mInfo = new VoiceInteractionServiceInfo(context.getPackageManager(), service, this.mUser);
            if (this.mInfo.getParseError() != null) {
                Slog.w(TAG, "Bad voice interaction service: " + this.mInfo.getParseError());
                this.mSessionComponentName = null;
                this.mIWindowManager = null;
                this.mValid = false;
                return;
            }
            this.mValid = true;
            this.mSessionComponentName = new ComponentName(service.getPackageName(), this.mInfo.getSessionService());
            this.mIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
            this.mContext.registerReceiver(this.mBroadcastReceiver, filter, null, handler);
        } catch (NameNotFoundException e) {
            Slog.w(TAG, "Voice interaction service not found: " + service, e);
            this.mInfo = null;
            this.mSessionComponentName = null;
            this.mIWindowManager = null;
            this.mValid = false;
        }
    }

    public boolean showSessionLocked(Bundle args, int flags, IVoiceInteractionSessionShowCallback showCallback, IBinder activityToken) {
        if (this.mActiveSession == null) {
            this.mActiveSession = new VoiceInteractionSessionConnection(this.mServiceStub, this.mSessionComponentName, this.mUser, this.mContext, this, this.mInfo.getServiceInfo().applicationInfo.uid, this.mHandler);
        }
        List activityTokens = null;
        if (activityToken == null) {
            activityTokens = ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).getTopVisibleActivities();
        }
        return this.mActiveSession.showLocked(args, flags, this.mDisabledShowContext, showCallback, activityToken, activityTokens);
    }

    public boolean hideSessionLocked() {
        if (this.mActiveSession != null) {
            return this.mActiveSession.hideLocked();
        }
        return false;
    }

    public boolean deliverNewSessionLocked(IBinder token, IVoiceInteractionSession session, IVoiceInteractor interactor) {
        if (this.mActiveSession == null || token != this.mActiveSession.mToken) {
            Slog.w(TAG, "deliverNewSession does not match active session");
            return false;
        }
        this.mActiveSession.deliverNewSessionLocked(session, interactor);
        return true;
    }

    public int startVoiceActivityLocked(int callingPid, int callingUid, IBinder token, Intent intent, String resolvedType) {
        RemoteException e;
        try {
            if (this.mActiveSession == null || token != this.mActiveSession.mToken) {
                Slog.w(TAG, "startVoiceActivity does not match active session");
                return -99;
            } else if (this.mActiveSession.mShown) {
                Intent intent2 = new Intent(intent);
                try {
                    intent2.addCategory("android.intent.category.VOICE");
                    intent2.addFlags(402653184);
                    return this.mAm.startVoiceActivity(this.mComponent.getPackageName(), callingPid, callingUid, intent2, resolvedType, this.mActiveSession.mSession, this.mActiveSession.mInteractor, 0, null, null, this.mUser);
                } catch (RemoteException e2) {
                    e = e2;
                    intent = intent2;
                }
            } else {
                Slog.w(TAG, "startVoiceActivity not allowed on hidden session");
                return -100;
            }
        } catch (RemoteException e3) {
            e = e3;
            throw new IllegalStateException("Unexpected remote error", e);
        }
    }

    public int startAssistantActivityLocked(int callingPid, int callingUid, IBinder token, Intent intent, String resolvedType) {
        RemoteException e;
        try {
            if (this.mActiveSession == null || token != this.mActiveSession.mToken) {
                Slog.w(TAG, "startAssistantActivity does not match active session");
                return -89;
            } else if (this.mActiveSession.mShown) {
                Intent intent2 = new Intent(intent);
                try {
                    intent2.addFlags(268435456);
                    ActivityOptions options = ActivityOptions.makeBasic();
                    options.setLaunchStackId(6);
                    return this.mAm.startAssistantActivity(this.mComponent.getPackageName(), callingPid, callingUid, intent2, resolvedType, options.toBundle(), this.mUser);
                } catch (RemoteException e2) {
                    e = e2;
                    intent = intent2;
                }
            } else {
                Slog.w(TAG, "startAssistantActivity not allowed on hidden session");
                return -90;
            }
        } catch (RemoteException e3) {
            e = e3;
            throw new IllegalStateException("Unexpected remote error", e);
        }
    }

    public void setKeepAwakeLocked(IBinder token, boolean keepAwake) {
        try {
            if (this.mActiveSession == null || token != this.mActiveSession.mToken) {
                Slog.w(TAG, "setKeepAwake does not match active session");
            } else {
                this.mAm.setVoiceKeepAwake(this.mActiveSession.mSession, keepAwake);
            }
        } catch (RemoteException e) {
            throw new IllegalStateException("Unexpected remote error", e);
        }
    }

    public void closeSystemDialogsLocked(IBinder token) {
        try {
            if (this.mActiveSession == null || token != this.mActiveSession.mToken) {
                Slog.w(TAG, "closeSystemDialogs does not match active session");
            } else {
                this.mAm.closeSystemDialogs(CLOSE_REASON_VOICE_INTERACTION);
            }
        } catch (RemoteException e) {
            throw new IllegalStateException("Unexpected remote error", e);
        }
    }

    public void finishLocked(IBinder token, boolean finishTask) {
        if (this.mActiveSession == null || !(finishTask || token == this.mActiveSession.mToken)) {
            Slog.w(TAG, "finish does not match active session");
            return;
        }
        this.mActiveSession.cancelLocked(finishTask);
        this.mActiveSession = null;
    }

    public void setDisabledShowContextLocked(int callingUid, int flags) {
        int activeUid = this.mInfo.getServiceInfo().applicationInfo.uid;
        if (callingUid != activeUid) {
            throw new SecurityException("Calling uid " + callingUid + " does not match active uid " + activeUid);
        }
        this.mDisabledShowContext = flags;
    }

    public int getDisabledShowContextLocked(int callingUid) {
        int activeUid = this.mInfo.getServiceInfo().applicationInfo.uid;
        if (callingUid == activeUid) {
            return this.mDisabledShowContext;
        }
        throw new SecurityException("Calling uid " + callingUid + " does not match active uid " + activeUid);
    }

    public int getUserDisabledShowContextLocked(int callingUid) {
        int activeUid = this.mInfo.getServiceInfo().applicationInfo.uid;
        if (callingUid == activeUid) {
            return this.mActiveSession != null ? this.mActiveSession.getUserDisabledShowContextLocked() : 0;
        } else {
            throw new SecurityException("Calling uid " + callingUid + " does not match active uid " + activeUid);
        }
    }

    public boolean supportsLocalVoiceInteraction() {
        return this.mInfo.getSupportsLocalInteraction();
    }

    public void dumpLocked(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mValid) {
            pw.print("  mUser=");
            pw.println(this.mUser);
            pw.print("  mComponent=");
            pw.println(this.mComponent.flattenToShortString());
            pw.print("  Session service=");
            pw.println(this.mInfo.getSessionService());
            pw.println("  Service info:");
            this.mInfo.getServiceInfo().dump(new PrintWriterPrinter(pw), "    ");
            pw.print("  Recognition service=");
            pw.println(this.mInfo.getRecognitionService());
            pw.print("  Settings activity=");
            pw.println(this.mInfo.getSettingsActivity());
            pw.print("  Supports assist=");
            pw.println(this.mInfo.getSupportsAssist());
            pw.print("  Supports launch from keyguard=");
            pw.println(this.mInfo.getSupportsLaunchFromKeyguard());
            if (this.mDisabledShowContext != 0) {
                pw.print("  mDisabledShowContext=");
                pw.println(Integer.toHexString(this.mDisabledShowContext));
            }
            pw.print("  mBound=");
            pw.print(this.mBound);
            pw.print(" mService=");
            pw.println(this.mService);
            if (this.mActiveSession != null) {
                pw.println("  Active session:");
                this.mActiveSession.dump("    ", pw);
            }
            return;
        }
        pw.print("  NOT VALID: ");
        if (this.mInfo == null) {
            pw.println("no info");
        } else {
            pw.println(this.mInfo.getParseError());
        }
    }

    void startLocked() {
        Intent intent = new Intent("android.service.voice.VoiceInteractionService");
        intent.setComponent(this.mComponent);
        this.mBound = this.mContext.bindServiceAsUser(intent, this.mConnection, 67108865, new UserHandle(this.mUser));
        if (!this.mBound) {
            Slog.w(TAG, "Failed binding to voice interaction service " + this.mComponent);
        }
    }

    public void launchVoiceAssistFromKeyguard() {
        if (this.mService == null) {
            Slog.w(TAG, "Not bound to voice interaction service " + this.mComponent);
            return;
        }
        try {
            this.mService.launchVoiceAssistFromKeyguard();
        } catch (RemoteException e) {
            Slog.w(TAG, "RemoteException while calling launchVoiceAssistFromKeyguard", e);
        }
    }

    void shutdownLocked() {
        if (this.mActiveSession != null) {
            this.mActiveSession.cancelLocked(false);
            this.mActiveSession = null;
        }
        try {
            if (this.mService != null) {
                this.mService.shutdown();
            }
        } catch (RemoteException e) {
            Slog.w(TAG, "RemoteException in shutdown", e);
        }
        if (this.mBound) {
            this.mContext.unbindService(this.mConnection);
            this.mBound = false;
        }
        if (this.mValid) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        }
    }

    void notifySoundModelsChangedLocked() {
        if (this.mService == null) {
            Slog.w(TAG, "Not bound to voice interaction service " + this.mComponent);
            return;
        }
        try {
            this.mService.soundModelsChanged();
        } catch (RemoteException e) {
            Slog.w(TAG, "RemoteException while calling soundModelsChanged", e);
        }
    }

    public void sessionConnectionGone(VoiceInteractionSessionConnection connection) {
        synchronized (this.mServiceStub) {
            finishLocked(connection.mToken, false);
        }
    }

    public void onSessionShown(VoiceInteractionSessionConnection connection) {
        this.mServiceStub.onSessionShown();
    }

    public void onSessionHidden(VoiceInteractionSessionConnection connection) {
        this.mServiceStub.onSessionHidden();
    }
}

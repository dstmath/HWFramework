package com.android.server.voiceinteraction;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.UriGrantsManager;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.voice.IVoiceInteractionSession;
import android.service.voice.IVoiceInteractionSessionService;
import android.util.Slog;
import android.view.IWindowManager;
import com.android.internal.app.AssistUtils;
import com.android.internal.app.IVoiceInteractionSessionShowCallback;
import com.android.internal.app.IVoiceInteractor;
import com.android.server.LocalServices;
import com.android.server.am.AssistDataRequester;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.uri.UriGrantsManagerInternal;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
public final class VoiceInteractionSessionConnection implements ServiceConnection, AssistDataRequester.AssistDataRequesterCallbacks {
    static final String TAG = "VoiceInteractionServiceManager";
    final IActivityManager mAm;
    final AppOpsManager mAppOps;
    AssistDataRequester mAssistDataRequester;
    final Intent mBindIntent;
    boolean mBound;
    final Callback mCallback;
    final int mCallingUid;
    boolean mCanceled;
    final Context mContext;
    final ServiceConnection mFullConnection = new ServiceConnection() {
        /* class com.android.server.voiceinteraction.VoiceInteractionSessionConnection.AnonymousClass2 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    boolean mFullyBound;
    final Handler mHandler;
    final IWindowManager mIWindowManager;
    IVoiceInteractor mInteractor;
    final Object mLock;
    ArrayList<IVoiceInteractionSessionShowCallback> mPendingShowCallbacks = new ArrayList<>();
    final IBinder mPermissionOwner;
    IVoiceInteractionSessionService mService;
    IVoiceInteractionSession mSession;
    final ComponentName mSessionComponentName;
    Bundle mShowArgs;
    private Runnable mShowAssistDisclosureRunnable = new Runnable() {
        /* class com.android.server.voiceinteraction.VoiceInteractionSessionConnection.AnonymousClass3 */

        @Override // java.lang.Runnable
        public void run() {
            StatusBarManagerInternal statusBarInternal = (StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class);
            if (statusBarInternal != null) {
                statusBarInternal.showAssistDisclosure();
            }
        }
    };
    IVoiceInteractionSessionShowCallback mShowCallback = new IVoiceInteractionSessionShowCallback.Stub() {
        /* class com.android.server.voiceinteraction.VoiceInteractionSessionConnection.AnonymousClass1 */

        public void onFailed() throws RemoteException {
            synchronized (VoiceInteractionSessionConnection.this.mLock) {
                VoiceInteractionSessionConnection.this.notifyPendingShowCallbacksFailedLocked();
            }
        }

        public void onShown() throws RemoteException {
            synchronized (VoiceInteractionSessionConnection.this.mLock) {
                VoiceInteractionSessionConnection.this.notifyPendingShowCallbacksShownLocked();
            }
        }
    };
    int mShowFlags;
    boolean mShown;
    final IBinder mToken = new Binder();
    final UriGrantsManagerInternal mUgmInternal;
    final int mUser;

    public interface Callback {
        void onSessionHidden(VoiceInteractionSessionConnection voiceInteractionSessionConnection);

        void onSessionShown(VoiceInteractionSessionConnection voiceInteractionSessionConnection);

        void sessionConnectionGone(VoiceInteractionSessionConnection voiceInteractionSessionConnection);
    }

    public VoiceInteractionSessionConnection(Object lock, ComponentName component, int user, Context context, Callback callback, int callingUid, Handler handler) {
        this.mLock = lock;
        this.mSessionComponentName = component;
        this.mUser = user;
        this.mContext = context;
        this.mCallback = callback;
        this.mCallingUid = callingUid;
        this.mHandler = handler;
        this.mAm = ActivityManager.getService();
        this.mUgmInternal = (UriGrantsManagerInternal) LocalServices.getService(UriGrantsManagerInternal.class);
        this.mIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        this.mAppOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        Context context2 = this.mContext;
        this.mAssistDataRequester = new AssistDataRequester(context2, this.mIWindowManager, (AppOpsManager) context2.getSystemService("appops"), this, this.mLock, 49, 50);
        UriGrantsManagerInternal uriGrantsManagerInternal = this.mUgmInternal;
        this.mPermissionOwner = uriGrantsManagerInternal.newUriPermissionOwner("voicesession:" + component.flattenToShortString());
        this.mBindIntent = new Intent("android.service.voice.VoiceInteractionService");
        this.mBindIntent.setComponent(this.mSessionComponentName);
        this.mBound = this.mContext.bindServiceAsUser(this.mBindIntent, this, 1048625, new UserHandle(this.mUser));
        if (this.mBound) {
            try {
                this.mIWindowManager.addWindowToken(this.mToken, 2031, 0);
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed adding window token", e);
            }
        } else {
            Slog.w(TAG, "Failed binding to voice interaction session service " + this.mSessionComponentName);
        }
    }

    public int getUserDisabledShowContextLocked() {
        int flags = 0;
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "assist_structure_enabled", 1, this.mUser) == 0) {
            flags = 0 | 1;
        }
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "assist_screenshot_enabled", 1, this.mUser) == 0) {
            return flags | 2;
        }
        return flags;
    }

    public boolean showLocked(Bundle args, int flags, int disabledContext, IVoiceInteractionSessionShowCallback showCallback, List<IBinder> topActivities) {
        if (this.mBound) {
            if (!this.mFullyBound) {
                this.mFullyBound = this.mContext.bindServiceAsUser(this.mBindIntent, this.mFullConnection, 202375169, new UserHandle(this.mUser));
            }
            this.mShown = true;
            this.mShowArgs = args;
            this.mShowFlags = flags;
            int disabledContext2 = disabledContext | getUserDisabledShowContextLocked();
            this.mAssistDataRequester.requestAssistData(topActivities, (flags & 1) != 0, (flags & 2) != 0, (disabledContext2 & 1) == 0, (disabledContext2 & 2) == 0, this.mCallingUid, this.mSessionComponentName.getPackageName());
            if ((this.mAssistDataRequester.getPendingDataCount() > 0 || this.mAssistDataRequester.getPendingScreenshotCount() > 0) && AssistUtils.shouldDisclose(this.mContext, this.mSessionComponentName)) {
                this.mHandler.post(this.mShowAssistDisclosureRunnable);
            }
            IVoiceInteractionSession iVoiceInteractionSession = this.mSession;
            if (iVoiceInteractionSession != null) {
                try {
                    iVoiceInteractionSession.show(this.mShowArgs, this.mShowFlags, showCallback);
                    this.mShowArgs = null;
                    this.mShowFlags = 0;
                } catch (RemoteException e) {
                }
                this.mAssistDataRequester.processPendingAssistData();
            } else if (showCallback != null) {
                this.mPendingShowCallbacks.add(showCallback);
            }
            this.mCallback.onSessionShown(this);
            return true;
        }
        if (showCallback != null) {
            try {
                showCallback.onFailed();
            } catch (RemoteException e2) {
            }
        }
        return false;
    }

    @Override // com.android.server.am.AssistDataRequester.AssistDataRequesterCallbacks
    public boolean canHandleReceivedAssistDataLocked() {
        return this.mSession != null;
    }

    @Override // com.android.server.am.AssistDataRequester.AssistDataRequesterCallbacks
    public void onAssistDataReceivedLocked(Bundle data, int activityIndex, int activityCount) {
        int uid;
        ClipData clipData;
        IVoiceInteractionSession iVoiceInteractionSession = this.mSession;
        if (iVoiceInteractionSession != null) {
            if (data == null) {
                try {
                    iVoiceInteractionSession.handleAssist(-1, (IBinder) null, (Bundle) null, (AssistStructure) null, (AssistContent) null, 0, 0);
                } catch (RemoteException e) {
                }
            } else {
                int taskId = data.getInt("taskId");
                IBinder activityId = data.getBinder("activityId");
                Bundle assistData = data.getBundle("data");
                AssistStructure structure = (AssistStructure) data.getParcelable("structure");
                AssistContent content = (AssistContent) data.getParcelable("content");
                if (assistData != null) {
                    uid = assistData.getInt("android.intent.extra.ASSIST_UID", -1);
                } else {
                    uid = -1;
                }
                if (uid >= 0 && content != null) {
                    Intent intent = content.getIntent();
                    if (!(intent == null || (clipData = intent.getClipData()) == null || !Intent.isAccessUriMode(intent.getFlags()))) {
                        grantClipDataPermissions(clipData, intent.getFlags(), uid, this.mCallingUid, this.mSessionComponentName.getPackageName());
                    }
                    ClipData clipData2 = content.getClipData();
                    if (clipData2 != null) {
                        grantClipDataPermissions(clipData2, 1, uid, this.mCallingUid, this.mSessionComponentName.getPackageName());
                    }
                }
                try {
                    try {
                        this.mSession.handleAssist(taskId, activityId, assistData, structure, content, activityIndex, activityCount);
                    } catch (RemoteException e2) {
                    }
                } catch (RemoteException e3) {
                }
            }
        }
    }

    @Override // com.android.server.am.AssistDataRequester.AssistDataRequesterCallbacks
    public void onAssistScreenshotReceivedLocked(Bitmap screenshot) {
        IVoiceInteractionSession iVoiceInteractionSession = this.mSession;
        if (iVoiceInteractionSession != null) {
            try {
                iVoiceInteractionSession.handleScreenshot(screenshot);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void grantUriPermission(Uri uri, int mode, int srcUid, int destUid, String destPkg) {
        Throwable th;
        SecurityException e;
        if ("content".equals(uri.getScheme())) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mUgmInternal.checkGrantUriPermission(srcUid, (String) null, ContentProvider.getUriWithoutUserId(uri), mode, ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(srcUid)));
                int sourceUserId = ContentProvider.getUserIdFromUri(uri, this.mUser);
                try {
                    UriGrantsManager.getService().grantUriPermissionFromOwner(this.mPermissionOwner, srcUid, destPkg, ContentProvider.getUriWithoutUserId(uri), 1, sourceUserId, this.mUser);
                    Binder.restoreCallingIdentity(ident);
                } catch (RemoteException e2) {
                    Binder.restoreCallingIdentity(ident);
                } catch (SecurityException e3) {
                    e = e3;
                    try {
                        Slog.w(TAG, "Can't propagate permission", e);
                        Binder.restoreCallingIdentity(ident);
                    } catch (Throwable th2) {
                        th = th2;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            } catch (RemoteException e4) {
                Binder.restoreCallingIdentity(ident);
            } catch (SecurityException e5) {
                e = e5;
                Slog.w(TAG, "Can't propagate permission", e);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th4) {
                th = th4;
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void grantClipDataItemPermission(ClipData.Item item, int mode, int srcUid, int destUid, String destPkg) {
        if (item.getUri() != null) {
            grantUriPermission(item.getUri(), mode, srcUid, destUid, destPkg);
        }
        Intent intent = item.getIntent();
        if (intent != null && intent.getData() != null) {
            grantUriPermission(intent.getData(), mode, srcUid, destUid, destPkg);
        }
    }

    /* access modifiers changed from: package-private */
    public void grantClipDataPermissions(ClipData data, int mode, int srcUid, int destUid, String destPkg) {
        int N = data.getItemCount();
        for (int i = 0; i < N; i++) {
            grantClipDataItemPermission(data.getItemAt(i), mode, srcUid, destUid, destPkg);
        }
    }

    public boolean hideLocked() {
        if (!this.mBound) {
            return false;
        }
        if (this.mShown) {
            this.mShown = false;
            this.mShowArgs = null;
            this.mShowFlags = 0;
            this.mAssistDataRequester.cancel();
            this.mPendingShowCallbacks.clear();
            IVoiceInteractionSession iVoiceInteractionSession = this.mSession;
            if (iVoiceInteractionSession != null) {
                try {
                    iVoiceInteractionSession.hide();
                } catch (RemoteException e) {
                }
            }
            this.mUgmInternal.revokeUriPermissionFromOwner(this.mPermissionOwner, null, 3, this.mUser);
            if (this.mSession != null) {
                try {
                    ActivityTaskManager.getService().finishVoiceTask(this.mSession);
                } catch (RemoteException e2) {
                }
            }
            this.mCallback.onSessionHidden(this);
        }
        if (!this.mFullyBound) {
            return true;
        }
        this.mContext.unbindService(this.mFullConnection);
        this.mFullyBound = false;
        return true;
    }

    public void cancelLocked(boolean finishTask) {
        hideLocked();
        this.mCanceled = true;
        if (this.mBound) {
            IVoiceInteractionSession iVoiceInteractionSession = this.mSession;
            if (iVoiceInteractionSession != null) {
                try {
                    iVoiceInteractionSession.destroy();
                } catch (RemoteException e) {
                    Slog.w(TAG, "Voice interation session already dead");
                }
            }
            if (finishTask && this.mSession != null) {
                try {
                    ActivityTaskManager.getService().finishVoiceTask(this.mSession);
                } catch (RemoteException e2) {
                }
            }
            this.mContext.unbindService(this);
            try {
                this.mIWindowManager.removeWindowToken(this.mToken, 0);
            } catch (RemoteException e3) {
                Slog.w(TAG, "Failed removing window token", e3);
            }
            this.mBound = false;
            this.mService = null;
            this.mSession = null;
            this.mInteractor = null;
        }
        if (this.mFullyBound) {
            this.mContext.unbindService(this.mFullConnection);
            this.mFullyBound = false;
        }
    }

    public boolean deliverNewSessionLocked(IVoiceInteractionSession session, IVoiceInteractor interactor) {
        this.mSession = session;
        this.mInteractor = interactor;
        if (!this.mShown) {
            return true;
        }
        try {
            session.show(this.mShowArgs, this.mShowFlags, this.mShowCallback);
            this.mShowArgs = null;
            this.mShowFlags = 0;
        } catch (RemoteException e) {
        }
        this.mAssistDataRequester.processPendingAssistData();
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyPendingShowCallbacksShownLocked() {
        for (int i = 0; i < this.mPendingShowCallbacks.size(); i++) {
            try {
                this.mPendingShowCallbacks.get(i).onShown();
            } catch (RemoteException e) {
            }
        }
        this.mPendingShowCallbacks.clear();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyPendingShowCallbacksFailedLocked() {
        for (int i = 0; i < this.mPendingShowCallbacks.size(); i++) {
            try {
                this.mPendingShowCallbacks.get(i).onFailed();
            } catch (RemoteException e) {
            }
        }
        this.mPendingShowCallbacks.clear();
    }

    @Override // android.content.ServiceConnection
    public void onServiceConnected(ComponentName name, IBinder service) {
        synchronized (this.mLock) {
            this.mService = IVoiceInteractionSessionService.Stub.asInterface(service);
            if (!this.mCanceled) {
                try {
                    this.mService.newSession(this.mToken, this.mShowArgs, this.mShowFlags);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed adding window token", e);
                }
            }
        }
    }

    @Override // android.content.ServiceConnection
    public void onServiceDisconnected(ComponentName name) {
        this.mCallback.sessionConnectionGone(this);
        synchronized (this.mLock) {
            this.mService = null;
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("mToken=");
        pw.println(this.mToken);
        pw.print(prefix);
        pw.print("mShown=");
        pw.println(this.mShown);
        pw.print(prefix);
        pw.print("mShowArgs=");
        pw.println(this.mShowArgs);
        pw.print(prefix);
        pw.print("mShowFlags=0x");
        pw.println(Integer.toHexString(this.mShowFlags));
        pw.print(prefix);
        pw.print("mBound=");
        pw.println(this.mBound);
        if (this.mBound) {
            pw.print(prefix);
            pw.print("mService=");
            pw.println(this.mService);
            pw.print(prefix);
            pw.print("mSession=");
            pw.println(this.mSession);
            pw.print(prefix);
            pw.print("mInteractor=");
            pw.println(this.mInteractor);
        }
        this.mAssistDataRequester.dump(prefix, pw);
    }
}

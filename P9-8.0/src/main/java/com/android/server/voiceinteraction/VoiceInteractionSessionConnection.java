package com.android.server.voiceinteraction;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.ClipData;
import android.content.ClipData.Item;
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
import android.provider.Settings.Secure;
import android.service.voice.IVoiceInteractionSession;
import android.service.voice.IVoiceInteractionSessionService;
import android.util.Slog;
import android.view.IWindowManager;
import com.android.internal.app.AssistUtils;
import com.android.internal.app.IAssistScreenshotReceiver;
import com.android.internal.app.IVoiceInteractionSessionShowCallback;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.IResultReceiver;
import com.android.internal.os.IResultReceiver.Stub;
import com.android.server.LocalServices;
import com.android.server.statusbar.StatusBarManagerInternal;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

final class VoiceInteractionSessionConnection implements ServiceConnection {
    private static final String KEY_RECEIVER_EXTRA_COUNT = "count";
    private static final String KEY_RECEIVER_EXTRA_INDEX = "index";
    static final String TAG = "VoiceInteractionServiceManager";
    final IActivityManager mAm;
    final AppOpsManager mAppOps;
    ArrayList<AssistDataForActivity> mAssistData = new ArrayList();
    final IResultReceiver mAssistReceiver = new Stub() {
        public void send(int resultCode, Bundle resultData) throws RemoteException {
            synchronized (VoiceInteractionSessionConnection.this.mLock) {
                if (VoiceInteractionSessionConnection.this.mShown) {
                    VoiceInteractionSessionConnection.this.mHaveAssistData = true;
                    VoiceInteractionSessionConnection.this.mAssistData.add(new AssistDataForActivity(resultData));
                    VoiceInteractionSessionConnection.this.deliverSessionDataLocked();
                }
            }
        }
    };
    final Intent mBindIntent;
    boolean mBound;
    final Callback mCallback;
    final int mCallingUid;
    boolean mCanceled;
    final Context mContext;
    final ServiceConnection mFullConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    };
    boolean mFullyBound;
    final Handler mHandler;
    boolean mHaveAssistData;
    boolean mHaveScreenshot;
    final IWindowManager mIWindowManager;
    IVoiceInteractor mInteractor;
    final Object mLock;
    int mPendingAssistDataCount;
    ArrayList<IVoiceInteractionSessionShowCallback> mPendingShowCallbacks = new ArrayList();
    final IBinder mPermissionOwner;
    Bitmap mScreenshot;
    final IAssistScreenshotReceiver mScreenshotReceiver = new IAssistScreenshotReceiver.Stub() {
        public void send(Bitmap screenshot) throws RemoteException {
            synchronized (VoiceInteractionSessionConnection.this.mLock) {
                if (VoiceInteractionSessionConnection.this.mShown) {
                    VoiceInteractionSessionConnection.this.mHaveScreenshot = true;
                    VoiceInteractionSessionConnection.this.mScreenshot = screenshot;
                    VoiceInteractionSessionConnection.this.deliverSessionDataLocked();
                }
            }
        }
    };
    IVoiceInteractionSessionService mService;
    IVoiceInteractionSession mSession;
    final ComponentName mSessionComponentName;
    Bundle mShowArgs;
    private Runnable mShowAssistDisclosureRunnable = new Runnable() {
        public void run() {
            StatusBarManagerInternal statusBarInternal = (StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class);
            if (statusBarInternal != null) {
                statusBarInternal.showAssistDisclosure();
            }
        }
    };
    IVoiceInteractionSessionShowCallback mShowCallback = new IVoiceInteractionSessionShowCallback.Stub() {
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
    final int mUser;

    public interface Callback {
        void onSessionHidden(VoiceInteractionSessionConnection voiceInteractionSessionConnection);

        void onSessionShown(VoiceInteractionSessionConnection voiceInteractionSessionConnection);

        void sessionConnectionGone(VoiceInteractionSessionConnection voiceInteractionSessionConnection);
    }

    static class AssistDataForActivity {
        int activityCount;
        int activityIndex;
        Bundle data;

        public AssistDataForActivity(Bundle data) {
            this.data = data;
            Bundle receiverExtras = data.getBundle("receiverExtras");
            if (receiverExtras != null) {
                this.activityIndex = receiverExtras.getInt(VoiceInteractionSessionConnection.KEY_RECEIVER_EXTRA_INDEX);
                this.activityCount = receiverExtras.getInt(VoiceInteractionSessionConnection.KEY_RECEIVER_EXTRA_COUNT);
            }
        }
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
        this.mIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        this.mAppOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        IBinder permOwner = null;
        try {
            permOwner = this.mAm.newUriPermissionOwner("voicesession:" + component.flattenToShortString());
        } catch (RemoteException e) {
            Slog.w("voicesession", "AM dead", e);
        }
        this.mPermissionOwner = permOwner;
        this.mBindIntent = new Intent("android.service.voice.VoiceInteractionService");
        this.mBindIntent.setComponent(this.mSessionComponentName);
        this.mBound = this.mContext.bindServiceAsUser(this.mBindIntent, this, 49, new UserHandle(this.mUser));
        if (this.mBound) {
            try {
                this.mIWindowManager.addWindowToken(this.mToken, 2031, 0);
                return;
            } catch (RemoteException e2) {
                Slog.w(TAG, "Failed adding window token", e2);
                return;
            }
        }
        Slog.w(TAG, "Failed binding to voice interaction session service " + this.mSessionComponentName);
    }

    public int getUserDisabledShowContextLocked() {
        int flags = 0;
        if (Secure.getIntForUser(this.mContext.getContentResolver(), "assist_structure_enabled", 1, this.mUser) == 0) {
            flags = 1;
        }
        if (Secure.getIntForUser(this.mContext.getContentResolver(), "assist_screenshot_enabled", 1, this.mUser) == 0) {
            return flags | 2;
        }
        return flags;
    }

    public boolean showLocked(Bundle args, int flags, int disabledContext, IVoiceInteractionSessionShowCallback showCallback, IBinder activityToken, List<IBinder> topActivities) {
        if (this.mBound) {
            if (!this.mFullyBound) {
                this.mFullyBound = this.mContext.bindServiceAsUser(this.mBindIntent, this.mFullConnection, 201326593, new UserHandle(this.mUser));
            }
            this.mShown = true;
            boolean isAssistDataAllowed = true;
            try {
                isAssistDataAllowed = this.mAm.isAssistDataAllowedOnCurrentActivity();
            } catch (RemoteException e) {
            }
            disabledContext |= getUserDisabledShowContextLocked();
            boolean structureEnabled = isAssistDataAllowed ? (disabledContext & 1) == 0 : false;
            boolean screenshotEnabled = (isAssistDataAllowed && structureEnabled) ? (disabledContext & 2) == 0 : false;
            this.mShowArgs = args;
            this.mShowFlags = flags;
            this.mHaveAssistData = false;
            this.mPendingAssistDataCount = 0;
            boolean needDisclosure = false;
            if ((flags & 1) == 0) {
                this.mAssistData.clear();
            } else if (this.mAppOps.noteOpNoThrow(49, this.mCallingUid, this.mSessionComponentName.getPackageName()) == 0 && structureEnabled) {
                this.mAssistData.clear();
                int count = activityToken != null ? 1 : topActivities.size();
                int i = 0;
                while (i < count) {
                    IBinder topActivity = count == 1 ? activityToken : (IBinder) topActivities.get(i);
                    try {
                        MetricsLogger.count(this.mContext, "assist_with_context", 1);
                        Bundle receiverExtras = new Bundle();
                        receiverExtras.putInt(KEY_RECEIVER_EXTRA_INDEX, i);
                        receiverExtras.putInt(KEY_RECEIVER_EXTRA_COUNT, count);
                        if (this.mAm.requestAssistContextExtras(1, this.mAssistReceiver, receiverExtras, topActivity, i == 0, i == 0)) {
                            needDisclosure = true;
                            this.mPendingAssistDataCount++;
                        } else if (i == 0) {
                            this.mHaveAssistData = true;
                            this.mAssistData.clear();
                            screenshotEnabled = false;
                            break;
                        } else {
                            continue;
                        }
                    } catch (RemoteException e2) {
                    }
                    i++;
                }
            } else {
                this.mHaveAssistData = true;
                this.mAssistData.clear();
            }
            this.mHaveScreenshot = false;
            if ((flags & 2) == 0) {
                this.mScreenshot = null;
            } else if (this.mAppOps.noteOpNoThrow(50, this.mCallingUid, this.mSessionComponentName.getPackageName()) == 0 && screenshotEnabled) {
                try {
                    MetricsLogger.count(this.mContext, "assist_with_screen", 1);
                    needDisclosure = true;
                    this.mIWindowManager.requestAssistScreenshot(this.mScreenshotReceiver);
                } catch (RemoteException e3) {
                }
            } else {
                this.mHaveScreenshot = true;
                this.mScreenshot = null;
            }
            if (needDisclosure && AssistUtils.shouldDisclose(this.mContext, this.mSessionComponentName)) {
                this.mHandler.post(this.mShowAssistDisclosureRunnable);
            }
            if (this.mSession != null) {
                try {
                    this.mSession.show(this.mShowArgs, this.mShowFlags, showCallback);
                    this.mShowArgs = null;
                    this.mShowFlags = 0;
                } catch (RemoteException e4) {
                }
                deliverSessionDataLocked();
            } else if (showCallback != null) {
                this.mPendingShowCallbacks.add(showCallback);
            }
            this.mCallback.onSessionShown(this);
            return true;
        }
        if (showCallback != null) {
            try {
                showCallback.onFailed();
            } catch (RemoteException e5) {
            }
        }
        return false;
    }

    void grantUriPermission(Uri uri, int mode, int srcUid, int destUid, String destPkg) {
        if ("content".equals(uri.getScheme())) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mAm.checkGrantUriPermission(srcUid, null, ContentProvider.getUriWithoutUserId(uri), mode, ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(srcUid)));
                int sourceUserId = ContentProvider.getUserIdFromUri(uri, this.mUser);
                this.mAm.grantUriPermissionFromOwner(this.mPermissionOwner, srcUid, destPkg, ContentProvider.getUriWithoutUserId(uri), 1, sourceUserId, this.mUser);
            } catch (RemoteException e) {
            } catch (SecurityException e2) {
                Slog.w(TAG, "Can't propagate permission", e2);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    void grantClipDataItemPermission(Item item, int mode, int srcUid, int destUid, String destPkg) {
        if (item.getUri() != null) {
            grantUriPermission(item.getUri(), mode, srcUid, destUid, destPkg);
        }
        Intent intent = item.getIntent();
        if (intent != null && intent.getData() != null) {
            grantUriPermission(intent.getData(), mode, srcUid, destUid, destPkg);
        }
    }

    void grantClipDataPermissions(ClipData data, int mode, int srcUid, int destUid, String destPkg) {
        int N = data.getItemCount();
        for (int i = 0; i < N; i++) {
            grantClipDataItemPermission(data.getItemAt(i), mode, srcUid, destUid, destPkg);
        }
    }

    void deliverSessionDataLocked() {
        if (this.mSession != null) {
            if (this.mHaveAssistData) {
                if (this.mAssistData.isEmpty()) {
                    try {
                        this.mSession.handleAssist(null, null, null, 0, 0);
                    } catch (RemoteException e) {
                    }
                } else {
                    while (!this.mAssistData.isEmpty()) {
                        if (this.mPendingAssistDataCount <= 0) {
                            Slog.e(TAG, "mPendingAssistDataCount is " + this.mPendingAssistDataCount);
                        }
                        this.mPendingAssistDataCount--;
                        AssistDataForActivity assistData = (AssistDataForActivity) this.mAssistData.remove(0);
                        if (assistData.data == null) {
                            try {
                                this.mSession.handleAssist(null, null, null, assistData.activityIndex, assistData.activityCount);
                            } catch (RemoteException e2) {
                            }
                        } else {
                            deliverSessionDataLocked(assistData);
                        }
                    }
                }
                if (this.mPendingAssistDataCount <= 0) {
                    this.mHaveAssistData = false;
                }
            }
            if (this.mHaveScreenshot) {
                try {
                    this.mSession.handleScreenshot(this.mScreenshot);
                } catch (RemoteException e3) {
                }
                this.mScreenshot = null;
                this.mHaveScreenshot = false;
            }
        }
    }

    private void deliverSessionDataLocked(AssistDataForActivity assistDataForActivity) {
        Bundle assistData = assistDataForActivity.data.getBundle("data");
        AssistStructure structure = (AssistStructure) assistDataForActivity.data.getParcelable("structure");
        AssistContent content = (AssistContent) assistDataForActivity.data.getParcelable("content");
        int uid = assistDataForActivity.data.getInt("android.intent.extra.ASSIST_UID", -1);
        if (uid >= 0 && content != null) {
            ClipData data;
            Intent intent = content.getIntent();
            if (intent != null) {
                data = intent.getClipData();
                if (data != null && Intent.isAccessUriMode(intent.getFlags())) {
                    grantClipDataPermissions(data, intent.getFlags(), uid, this.mCallingUid, this.mSessionComponentName.getPackageName());
                }
            }
            data = content.getClipData();
            if (data != null) {
                grantClipDataPermissions(data, 1, uid, this.mCallingUid, this.mSessionComponentName.getPackageName());
            }
        }
        try {
            this.mSession.handleAssist(assistData, structure, content, assistDataForActivity.activityIndex, assistDataForActivity.activityCount);
        } catch (RemoteException e) {
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
            this.mHaveAssistData = false;
            this.mAssistData.clear();
            this.mPendingShowCallbacks.clear();
            if (this.mSession != null) {
                try {
                    this.mSession.hide();
                } catch (RemoteException e) {
                }
            }
            try {
                this.mAm.revokeUriPermissionFromOwner(this.mPermissionOwner, null, 3, this.mUser);
            } catch (RemoteException e2) {
            }
            if (this.mSession != null) {
                try {
                    this.mAm.finishVoiceTask(this.mSession);
                } catch (RemoteException e3) {
                }
            }
            this.mCallback.onSessionHidden(this);
        }
        if (this.mFullyBound) {
            this.mContext.unbindService(this.mFullConnection);
            this.mFullyBound = false;
        }
        return true;
    }

    public void cancelLocked(boolean finishTask) {
        hideLocked();
        this.mCanceled = true;
        if (this.mBound) {
            if (this.mSession != null) {
                try {
                    this.mSession.destroy();
                } catch (RemoteException e) {
                    Slog.w(TAG, "Voice interation session already dead");
                }
            }
            if (finishTask && this.mSession != null) {
                try {
                    this.mAm.finishVoiceTask(this.mSession);
                } catch (RemoteException e2) {
                }
            }
            try {
                this.mContext.unbindService(this);
            } catch (IllegalArgumentException e3) {
                Slog.w(TAG, "Service not registered: com.android.server.voiceinteraction.VoiceInteractionSessionConnection", e3);
            }
            try {
                this.mIWindowManager.removeWindowToken(this.mToken, 0);
            } catch (RemoteException e4) {
                Slog.w(TAG, "Failed removing window token", e4);
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
        if (this.mShown) {
            try {
                session.show(this.mShowArgs, this.mShowFlags, this.mShowCallback);
                this.mShowArgs = null;
                this.mShowFlags = 0;
            } catch (RemoteException e) {
            }
            deliverSessionDataLocked();
        }
        return true;
    }

    private void notifyPendingShowCallbacksShownLocked() {
        for (int i = 0; i < this.mPendingShowCallbacks.size(); i++) {
            try {
                ((IVoiceInteractionSessionShowCallback) this.mPendingShowCallbacks.get(i)).onShown();
            } catch (RemoteException e) {
            }
        }
        this.mPendingShowCallbacks.clear();
    }

    private void notifyPendingShowCallbacksFailedLocked() {
        for (int i = 0; i < this.mPendingShowCallbacks.size(); i++) {
            try {
                ((IVoiceInteractionSessionShowCallback) this.mPendingShowCallbacks.get(i)).onFailed();
            } catch (RemoteException e) {
            }
        }
        this.mPendingShowCallbacks.clear();
    }

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
        return;
    }

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
        pw.print(prefix);
        pw.print("mHaveAssistData=");
        pw.println(this.mHaveAssistData);
        if (this.mHaveAssistData) {
            pw.print(prefix);
            pw.print("mAssistData=");
            pw.println(this.mAssistData);
        }
    }
}

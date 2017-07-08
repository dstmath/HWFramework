package android.content.pm.permission;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.permission.IRuntimePermissionPresenter.Stub;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallback;
import android.os.RemoteCallback.OnResultListener;
import android.os.RemoteException;
import android.permissionpresenterservice.RuntimePermissionPresenterService;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.SomeArgs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RuntimePermissionPresenter {
    public static final String KEY_RESULT = "android.content.pm.permission.RuntimePermissionPresenter.key.result";
    private static final String TAG = "RuntimePermPresenter";
    @GuardedBy("sLock")
    private static RuntimePermissionPresenter sInstance;
    private static final Object sLock = null;
    private final RemoteService mRemoteService;

    public static abstract class OnResultCallback {
        public void onGetAppPermissions(List<RuntimePermissionPresentationInfo> list) {
        }

        public void getAppsUsingPermissions(boolean system, List<ApplicationInfo> list) {
        }
    }

    private static final class RemoteService extends Handler implements ServiceConnection {
        public static final int MSG_GET_APPS_USING_PERMISSIONS = 2;
        public static final int MSG_GET_APP_PERMISSIONS = 1;
        public static final int MSG_UNBIND = 3;
        private static final long UNBIND_TIMEOUT_MILLIS = 10000;
        @GuardedBy("mLock")
        private boolean mBound;
        private final Context mContext;
        private final Object mLock;
        @GuardedBy("mLock")
        private final List<Message> mPendingWork;
        @GuardedBy("mLock")
        private IRuntimePermissionPresenter mRemoteInstance;

        /* renamed from: android.content.pm.permission.RuntimePermissionPresenter.RemoteService.1 */
        class AnonymousClass1 implements OnResultListener {
            final /* synthetic */ OnResultCallback val$callback;
            final /* synthetic */ Handler val$handler;

            /* renamed from: android.content.pm.permission.RuntimePermissionPresenter.RemoteService.1.1 */
            class AnonymousClass1 implements Runnable {
                final /* synthetic */ OnResultCallback val$callback;
                final /* synthetic */ List val$reportedPermissions;

                AnonymousClass1(OnResultCallback val$callback, List val$reportedPermissions) {
                    this.val$callback = val$callback;
                    this.val$reportedPermissions = val$reportedPermissions;
                }

                public void run() {
                    this.val$callback.onGetAppPermissions(this.val$reportedPermissions);
                }
            }

            AnonymousClass1(Handler val$handler, OnResultCallback val$callback) {
                this.val$handler = val$handler;
                this.val$callback = val$callback;
            }

            public void onResult(Bundle result) {
                List<RuntimePermissionPresentationInfo> permissions = null;
                if (result != null) {
                    permissions = result.getParcelableArrayList(RuntimePermissionPresenter.KEY_RESULT);
                }
                if (permissions == null) {
                    permissions = Collections.emptyList();
                }
                List<RuntimePermissionPresentationInfo> reportedPermissions = permissions;
                if (this.val$handler != null) {
                    this.val$handler.post(new AnonymousClass1(this.val$callback, reportedPermissions));
                } else {
                    this.val$callback.onGetAppPermissions(reportedPermissions);
                }
            }
        }

        /* renamed from: android.content.pm.permission.RuntimePermissionPresenter.RemoteService.2 */
        class AnonymousClass2 implements OnResultListener {
            final /* synthetic */ OnResultCallback val$callback;
            final /* synthetic */ Handler val$handler;
            final /* synthetic */ boolean val$system;

            /* renamed from: android.content.pm.permission.RuntimePermissionPresenter.RemoteService.2.1 */
            class AnonymousClass1 implements Runnable {
                final /* synthetic */ OnResultCallback val$callback;
                final /* synthetic */ List val$reportedApps;
                final /* synthetic */ boolean val$system;

                AnonymousClass1(OnResultCallback val$callback, boolean val$system, List val$reportedApps) {
                    this.val$callback = val$callback;
                    this.val$system = val$system;
                    this.val$reportedApps = val$reportedApps;
                }

                public void run() {
                    this.val$callback.getAppsUsingPermissions(this.val$system, this.val$reportedApps);
                }
            }

            AnonymousClass2(Handler val$handler, OnResultCallback val$callback, boolean val$system) {
                this.val$handler = val$handler;
                this.val$callback = val$callback;
                this.val$system = val$system;
            }

            public void onResult(Bundle result) {
                List<ApplicationInfo> apps = null;
                if (result != null) {
                    apps = result.getParcelableArrayList(RuntimePermissionPresenter.KEY_RESULT);
                }
                if (apps == null) {
                    apps = Collections.emptyList();
                }
                List<ApplicationInfo> reportedApps = apps;
                if (this.val$handler != null) {
                    this.val$handler.post(new AnonymousClass1(this.val$callback, this.val$system, reportedApps));
                } else {
                    this.val$callback.getAppsUsingPermissions(this.val$system, reportedApps);
                }
            }
        }

        public RemoteService(Context context) {
            super(context.getMainLooper(), null, false);
            this.mLock = new Object();
            this.mPendingWork = new ArrayList();
            this.mContext = context;
        }

        public void processMessage(Message message) {
            synchronized (this.mLock) {
                if (!this.mBound) {
                    Intent intent = new Intent(RuntimePermissionPresenterService.SERVICE_INTERFACE);
                    intent.setPackage(this.mContext.getPackageManager().getPermissionControllerPackageName());
                    this.mBound = this.mContext.bindService(intent, this, MSG_GET_APP_PERMISSIONS);
                }
                this.mPendingWork.add(message);
                scheduleNextMessageIfNeededLocked();
            }
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (this.mLock) {
                this.mRemoteInstance = Stub.asInterface(service);
                scheduleNextMessageIfNeededLocked();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (this.mLock) {
                this.mRemoteInstance = null;
            }
        }

        public void handleMessage(Message msg) {
            SomeArgs args;
            OnResultCallback callback;
            Handler handler;
            IRuntimePermissionPresenter remoteInstance;
            switch (msg.what) {
                case MSG_GET_APP_PERMISSIONS /*1*/:
                    args = msg.obj;
                    String packageName = args.arg1;
                    callback = args.arg2;
                    handler = args.arg3;
                    args.recycle();
                    synchronized (this.mLock) {
                        remoteInstance = this.mRemoteInstance;
                        break;
                    }
                    if (remoteInstance != null) {
                        try {
                            remoteInstance.getAppPermissions(packageName, new RemoteCallback(new AnonymousClass1(handler, callback), this));
                        } catch (RemoteException re) {
                            Log.e(RuntimePermissionPresenter.TAG, "Error getting app permissions", re);
                        }
                        scheduleUnbind();
                        break;
                    }
                    return;
                case MSG_GET_APPS_USING_PERMISSIONS /*2*/:
                    args = (SomeArgs) msg.obj;
                    callback = (OnResultCallback) args.arg1;
                    handler = (Handler) args.arg2;
                    boolean system = args.argi1 == MSG_GET_APP_PERMISSIONS;
                    args.recycle();
                    synchronized (this.mLock) {
                        remoteInstance = this.mRemoteInstance;
                        break;
                    }
                    if (remoteInstance != null) {
                        try {
                            remoteInstance.getAppsUsingPermissions(system, new RemoteCallback(new AnonymousClass2(handler, callback, system), this));
                        } catch (RemoteException re2) {
                            Log.e(RuntimePermissionPresenter.TAG, "Error getting apps using permissions", re2);
                        }
                        scheduleUnbind();
                        break;
                    }
                    return;
                case MSG_UNBIND /*3*/:
                    synchronized (this.mLock) {
                        if (this.mBound) {
                            this.mContext.unbindService(this);
                            this.mBound = false;
                        }
                        this.mRemoteInstance = null;
                        break;
                    }
                    break;
            }
            synchronized (this.mLock) {
                scheduleNextMessageIfNeededLocked();
            }
        }

        private void scheduleNextMessageIfNeededLocked() {
            if (this.mBound && this.mRemoteInstance != null && !this.mPendingWork.isEmpty()) {
                sendMessage((Message) this.mPendingWork.remove(0));
            }
        }

        private void scheduleUnbind() {
            removeMessages(MSG_UNBIND);
            sendEmptyMessageDelayed(MSG_UNBIND, UNBIND_TIMEOUT_MILLIS);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.permission.RuntimePermissionPresenter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.permission.RuntimePermissionPresenter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.permission.RuntimePermissionPresenter.<clinit>():void");
    }

    public static RuntimePermissionPresenter getInstance(Context context) {
        RuntimePermissionPresenter runtimePermissionPresenter;
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new RuntimePermissionPresenter(context.getApplicationContext());
            }
            runtimePermissionPresenter = sInstance;
        }
        return runtimePermissionPresenter;
    }

    private RuntimePermissionPresenter(Context context) {
        this.mRemoteService = new RemoteService(context);
    }

    public void getAppPermissions(String packageName, OnResultCallback callback, Handler handler) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = packageName;
        args.arg2 = callback;
        args.arg3 = handler;
        this.mRemoteService.processMessage(this.mRemoteService.obtainMessage(1, args));
    }

    public void getAppsUsingPermissions(boolean system, OnResultCallback callback, Handler handler) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = callback;
        args.arg2 = handler;
        args.argi1 = system ? 1 : 0;
        this.mRemoteService.processMessage(this.mRemoteService.obtainMessage(2, args));
    }
}

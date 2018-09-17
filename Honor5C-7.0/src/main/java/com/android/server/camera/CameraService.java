package com.android.server.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.ICameraService;
import android.hardware.ICameraServiceProxy.Stub;
import android.nfc.INfcAdapter;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserManager;
import android.util.ArraySet;
import android.util.Slog;
import com.android.server.ServiceThread;
import com.android.server.SystemService;
import java.util.Collection;
import java.util.Set;

public class CameraService extends SystemService implements Callback, DeathRecipient {
    private static final String CAMERA_SERVICE_BINDER_NAME = "media.camera";
    public static final String CAMERA_SERVICE_PROXY_BINDER_NAME = "media.camera.proxy";
    public static final int CAMERA_STATE_ACTIVE = 1;
    public static final int CAMERA_STATE_CLOSED = 3;
    public static final int CAMERA_STATE_IDLE = 2;
    public static final int CAMERA_STATE_OPEN = 0;
    private static final boolean DEBUG = false;
    public static final int DISABLE_POLLING_FLAGS = 4096;
    public static final int ENABLE_POLLING_FLAGS = 0;
    private static final int MSG_SWITCH_USER = 1;
    private static final String NFC_NOTIFICATION_PROP = "ro.camera.notify_nfc";
    private static final String NFC_SERVICE_BINDER_NAME = "nfc";
    private static final int RETRY_DELAY_TIME = 20;
    private static final String TAG = "CameraService_proxy";
    private static final IBinder nfcInterfaceToken = null;
    private int mActiveCameraCount;
    private final ArraySet<String> mActiveCameraIds;
    private final Stub mCameraServiceProxy;
    private ICameraService mCameraServiceRaw;
    private final Context mContext;
    private Set<Integer> mEnabledCameraUsers;
    private final Handler mHandler;
    private final ServiceThread mHandlerThread;
    private final BroadcastReceiver mIntentReceiver;
    private int mLastUser;
    private final Object mLock;
    private final boolean mNotifyNfc;
    private UserManager mUserManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.camera.CameraService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.camera.CameraService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.camera.CameraService.<clinit>():void");
    }

    public CameraService(Context context) {
        boolean z = DEBUG;
        super(context);
        this.mLock = new Object();
        this.mActiveCameraIds = new ArraySet();
        this.mActiveCameraCount = ENABLE_POLLING_FLAGS;
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    if (action.equals("android.intent.action.USER_ADDED") || action.equals("android.intent.action.USER_REMOVED") || action.equals("android.intent.action.USER_INFO_CHANGED") || action.equals("android.intent.action.MANAGED_PROFILE_ADDED") || action.equals("android.intent.action.MANAGED_PROFILE_REMOVED")) {
                        synchronized (CameraService.this.mLock) {
                            if (CameraService.this.mEnabledCameraUsers == null) {
                            } else {
                                CameraService.this.switchUserLocked(CameraService.this.mLastUser);
                            }
                        }
                    }
                }
            }
        };
        this.mCameraServiceProxy = new Stub() {
            public void pingForUserUpdate() {
                CameraService.this.notifySwitchWithRetries(30);
            }

            public void notifyCameraState(String cameraId, int newCameraState) {
                String state = CameraService.cameraStateToString(newCameraState);
                CameraService.this.updateActivityCount(cameraId, newCameraState);
            }
        };
        this.mContext = context;
        this.mHandlerThread = new ServiceThread(TAG, -4, DEBUG);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper(), this);
        if (SystemProperties.getInt(NFC_NOTIFICATION_PROP, ENABLE_POLLING_FLAGS) > 0) {
            z = true;
        }
        this.mNotifyNfc = z;
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SWITCH_USER /*1*/:
                notifySwitchWithRetries(msg.arg1);
                break;
            default:
                Slog.e(TAG, "CameraService error, invalid message: " + msg.what);
                break;
        }
        return true;
    }

    public void onStart() {
        this.mUserManager = UserManager.get(this.mContext);
        if (this.mUserManager == null) {
            throw new IllegalStateException("UserManagerService must start before CameraService!");
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_ADDED");
        filter.addAction("android.intent.action.USER_REMOVED");
        filter.addAction("android.intent.action.USER_INFO_CHANGED");
        filter.addAction("android.intent.action.MANAGED_PROFILE_ADDED");
        filter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
        publishBinderService(CAMERA_SERVICE_PROXY_BINDER_NAME, this.mCameraServiceProxy);
    }

    public void onStartUser(int userHandle) {
        synchronized (this.mLock) {
            if (this.mEnabledCameraUsers == null) {
                switchUserLocked(userHandle);
            }
        }
    }

    public void onSwitchUser(int userHandle) {
        synchronized (this.mLock) {
            switchUserLocked(userHandle);
        }
    }

    public void binderDied() {
        synchronized (this.mLock) {
            this.mCameraServiceRaw = null;
            boolean wasEmpty = this.mActiveCameraIds.isEmpty();
            this.mActiveCameraIds.clear();
            if (this.mNotifyNfc && !wasEmpty) {
                notifyNfcService(true);
            }
        }
    }

    private void switchUserLocked(int userHandle) {
        Set<Integer> currentUserHandles = getEnabledUserHandles(userHandle);
        this.mLastUser = userHandle;
        if (this.mEnabledCameraUsers == null || !this.mEnabledCameraUsers.equals(currentUserHandles)) {
            this.mEnabledCameraUsers = currentUserHandles;
            notifyMediaserverLocked(MSG_SWITCH_USER, currentUserHandles);
        }
    }

    private Set<Integer> getEnabledUserHandles(int currentUserHandle) {
        int[] userProfiles = this.mUserManager.getEnabledProfileIds(currentUserHandle);
        Set<Integer> handles = new ArraySet(userProfiles.length);
        int length = userProfiles.length;
        for (int i = ENABLE_POLLING_FLAGS; i < length; i += MSG_SWITCH_USER) {
            handles.add(Integer.valueOf(userProfiles[i]));
        }
        return handles;
    }

    private void notifySwitchWithRetries(int retries) {
        synchronized (this.mLock) {
            if (this.mEnabledCameraUsers == null) {
                return;
            }
            if (notifyMediaserverLocked(MSG_SWITCH_USER, this.mEnabledCameraUsers)) {
                retries = ENABLE_POLLING_FLAGS;
            }
            if (retries > 0) {
                Slog.i(TAG, "Could not notify camera service of user switch, retrying...");
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_SWITCH_USER, retries - 1, ENABLE_POLLING_FLAGS, null), 20);
            }
        }
    }

    private boolean notifyMediaserverLocked(int eventType, Set<Integer> updatedUserHandles) {
        if (this.mCameraServiceRaw == null) {
            IBinder cameraServiceBinder = getBinderService(CAMERA_SERVICE_BINDER_NAME);
            if (cameraServiceBinder == null) {
                Slog.w(TAG, "Could not notify mediaserver, camera service not available.");
                return DEBUG;
            }
            try {
                cameraServiceBinder.linkToDeath(this, ENABLE_POLLING_FLAGS);
                this.mCameraServiceRaw = ICameraService.Stub.asInterface(cameraServiceBinder);
            } catch (RemoteException e) {
                Slog.w(TAG, "Could not link to death of native camera service");
                return DEBUG;
            }
        }
        try {
            this.mCameraServiceRaw.notifySystemEvent(eventType, toArray(updatedUserHandles));
            return true;
        } catch (RemoteException e2) {
            Slog.w(TAG, "Could not notify mediaserver, remote exception: " + e2);
            return DEBUG;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateActivityCount(String cameraId, int newCameraState) {
        synchronized (this.mLock) {
            boolean wasEmpty = this.mActiveCameraIds.isEmpty();
            switch (newCameraState) {
                case MSG_SWITCH_USER /*1*/:
                    this.mActiveCameraIds.add(cameraId);
                    break;
                case CAMERA_STATE_IDLE /*2*/:
                case CAMERA_STATE_CLOSED /*3*/:
                    this.mActiveCameraIds.remove(cameraId);
                    break;
            }
        }
    }

    private void notifyNfcService(boolean enablePolling) {
        IBinder nfcServiceBinder = getBinderService(NFC_SERVICE_BINDER_NAME);
        if (nfcServiceBinder == null) {
            Slog.w(TAG, "Could not connect to NFC service to notify it of camera state");
            return;
        }
        try {
            INfcAdapter.Stub.asInterface(nfcServiceBinder).setReaderMode(nfcInterfaceToken, null, enablePolling ? ENABLE_POLLING_FLAGS : DISABLE_POLLING_FLAGS, null);
        } catch (RemoteException e) {
            Slog.w(TAG, "Could not notify NFC service, remote exception: " + e);
        }
    }

    private static int[] toArray(Collection<Integer> c) {
        int[] ret = new int[c.size()];
        int idx = ENABLE_POLLING_FLAGS;
        for (Integer i : c) {
            int idx2 = idx + MSG_SWITCH_USER;
            ret[idx] = i.intValue();
            idx = idx2;
        }
        return ret;
    }

    private static String cameraStateToString(int newCameraState) {
        switch (newCameraState) {
            case ENABLE_POLLING_FLAGS /*0*/:
                return "CAMERA_STATE_OPEN";
            case MSG_SWITCH_USER /*1*/:
                return "CAMERA_STATE_ACTIVE";
            case CAMERA_STATE_IDLE /*2*/:
                return "CAMERA_STATE_IDLE";
            case CAMERA_STATE_CLOSED /*3*/:
                return "CAMERA_STATE_CLOSED";
            default:
                return "CAMERA_STATE_UNKNOWN";
        }
    }
}

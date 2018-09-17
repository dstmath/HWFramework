package com.android.server.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.ICameraService;
import android.hardware.ICameraServiceProxy.Stub;
import android.nfc.INfcAdapter;
import android.os.Binder;
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

public class CameraServiceProxy extends SystemService implements Callback, DeathRecipient {
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
    private static final IBinder nfcInterfaceToken = new Binder();
    private int mActiveCameraCount = 0;
    private final ArraySet<String> mActiveCameraIds = new ArraySet();
    private final Stub mCameraServiceProxy = new Stub() {
        public void pingForUserUpdate() {
            CameraServiceProxy.this.notifySwitchWithRetries(30);
        }

        public void notifyCameraState(String cameraId, int newCameraState) {
            String state = CameraServiceProxy.cameraStateToString(newCameraState);
            CameraServiceProxy.this.updateActivityCount(cameraId, newCameraState);
        }
    };
    private ICameraService mCameraServiceRaw;
    private final Context mContext;
    private Set<Integer> mEnabledCameraUsers;
    private final Handler mHandler;
    private final ServiceThread mHandlerThread;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("android.intent.action.USER_ADDED") || action.equals("android.intent.action.USER_REMOVED") || action.equals("android.intent.action.USER_INFO_CHANGED") || action.equals("android.intent.action.MANAGED_PROFILE_ADDED") || action.equals("android.intent.action.MANAGED_PROFILE_REMOVED")) {
                    synchronized (CameraServiceProxy.this.mLock) {
                        if (CameraServiceProxy.this.mEnabledCameraUsers == null) {
                            return;
                        }
                        CameraServiceProxy.this.switchUserLocked(CameraServiceProxy.this.mLastUser);
                    }
                }
            }
        }
    };
    private int mLastUser;
    private final Object mLock = new Object();
    private final boolean mNotifyNfc;
    private UserManager mUserManager;

    public CameraServiceProxy(Context context) {
        boolean z = false;
        super(context);
        this.mContext = context;
        this.mHandlerThread = new ServiceThread(TAG, -4, false);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper(), this);
        if (SystemProperties.getInt(NFC_NOTIFICATION_PROP, 0) > 0) {
            z = true;
        }
        this.mNotifyNfc = z;
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                notifySwitchWithRetries(msg.arg1);
                break;
            default:
                Slog.e(TAG, "CameraServiceProxy error, invalid message: " + msg.what);
                break;
        }
        return true;
    }

    public void onStart() {
        this.mUserManager = UserManager.get(this.mContext);
        if (this.mUserManager == null) {
            throw new IllegalStateException("UserManagerService must start before CameraServiceProxy!");
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
            if (this.mNotifyNfc && (wasEmpty ^ 1) != 0) {
                notifyNfcService(true);
            }
        }
    }

    private void switchUserLocked(int userHandle) {
        Set<Integer> currentUserHandles = getEnabledUserHandles(userHandle);
        this.mLastUser = userHandle;
        if (this.mEnabledCameraUsers == null || (this.mEnabledCameraUsers.equals(currentUserHandles) ^ 1) != 0) {
            this.mEnabledCameraUsers = currentUserHandles;
            notifyMediaserverLocked(1, currentUserHandles);
        }
    }

    private Set<Integer> getEnabledUserHandles(int currentUserHandle) {
        int[] userProfiles = this.mUserManager.getEnabledProfileIds(currentUserHandle);
        Set<Integer> handles = new ArraySet(userProfiles.length);
        for (int id : userProfiles) {
            handles.add(Integer.valueOf(id));
        }
        return handles;
    }

    /* JADX WARNING: Missing block: B:12:0x0017, code:
            if (r7 > 0) goto L_0x001d;
     */
    /* JADX WARNING: Missing block: B:13:0x0019, code:
            return;
     */
    /* JADX WARNING: Missing block: B:17:0x001d, code:
            android.util.Slog.i(TAG, "Could not notify camera service of user switch, retrying...");
            r6.mHandler.sendMessageDelayed(r6.mHandler.obtainMessage(1, r7 - 1, 0, null), 20);
     */
    /* JADX WARNING: Missing block: B:18:0x0035, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifySwitchWithRetries(int retries) {
        synchronized (this.mLock) {
            if (this.mEnabledCameraUsers == null) {
            } else if (notifyMediaserverLocked(1, this.mEnabledCameraUsers)) {
                retries = 0;
            }
        }
    }

    private boolean notifyMediaserverLocked(int eventType, Set<Integer> updatedUserHandles) {
        if (this.mCameraServiceRaw == null) {
            IBinder cameraServiceBinder = getBinderService(CAMERA_SERVICE_BINDER_NAME);
            if (cameraServiceBinder == null) {
                Slog.w(TAG, "Could not notify mediaserver, camera service not available.");
                return false;
            }
            try {
                cameraServiceBinder.linkToDeath(this, 0);
                this.mCameraServiceRaw = ICameraService.Stub.asInterface(cameraServiceBinder);
            } catch (RemoteException e) {
                Slog.w(TAG, "Could not link to death of native camera service");
                return false;
            }
        }
        try {
            this.mCameraServiceRaw.notifySystemEvent(eventType, toArray(updatedUserHandles));
            return true;
        } catch (RemoteException e2) {
            Slog.w(TAG, "Could not notify mediaserver, remote exception: " + e2);
            return false;
        }
    }

    private void updateActivityCount(String cameraId, int newCameraState) {
        synchronized (this.mLock) {
            boolean wasEmpty = this.mActiveCameraIds.isEmpty();
            switch (newCameraState) {
                case 1:
                    this.mActiveCameraIds.add(cameraId);
                    break;
                case 2:
                case 3:
                    this.mActiveCameraIds.remove(cameraId);
                    break;
            }
            boolean isEmpty = this.mActiveCameraIds.isEmpty();
            if (this.mNotifyNfc && wasEmpty != isEmpty) {
                notifyNfcService(isEmpty);
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
            INfcAdapter.Stub.asInterface(nfcServiceBinder).setReaderMode(nfcInterfaceToken, null, enablePolling ? 0 : 4096, null);
        } catch (RemoteException e) {
            Slog.w(TAG, "Could not notify NFC service, remote exception: " + e);
        }
    }

    private static int[] toArray(Collection<Integer> c) {
        int[] ret = new int[c.size()];
        int idx = 0;
        for (Integer i : c) {
            int idx2 = idx + 1;
            ret[idx] = i.intValue();
            idx = idx2;
        }
        return ret;
    }

    private static String cameraStateToString(int newCameraState) {
        switch (newCameraState) {
            case 0:
                return "CAMERA_STATE_OPEN";
            case 1:
                return "CAMERA_STATE_ACTIVE";
            case 2:
                return "CAMERA_STATE_IDLE";
            case 3:
                return "CAMERA_STATE_CLOSED";
            default:
                return "CAMERA_STATE_UNKNOWN";
        }
    }
}

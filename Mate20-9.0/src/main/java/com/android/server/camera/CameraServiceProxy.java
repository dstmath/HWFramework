package com.android.server.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.ICameraService;
import android.hardware.ICameraServiceProxy;
import android.metrics.LogMaker;
import android.nfc.INfcAdapter;
import android.os.BatteryManagerInternal;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.logging.MetricsLogger;
import com.android.server.HwServiceFactory;
import com.android.server.ServiceThread;
import com.android.server.SystemService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CameraServiceProxy extends SystemService implements Handler.Callback, IBinder.DeathRecipient {
    private static final String CAMERA_SERVICE_BINDER_NAME = "media.camera";
    public static final String CAMERA_SERVICE_PROXY_BINDER_NAME = "media.camera.proxy";
    private static final boolean DEBUG = false;
    public static final int DISABLE_POLLING_FLAGS = 4096;
    public static final int ENABLE_POLLING_FLAGS = 0;
    private static final int MAX_USAGE_HISTORY = 100;
    private static final int MSG_SWITCH_USER = 1;
    private static final String NFC_NOTIFICATION_PROP = "ro.camera.notify_nfc";
    private static final String NFC_SERVICE_BINDER_NAME = "nfc";
    private static final int RETRY_DELAY_TIME = 20;
    private static final String TAG = "CameraService_proxy";
    private static final IBinder nfcInterfaceToken = new Binder();
    private IHwCameraServiceProxy hwcsp;
    private final ArrayMap<String, CameraUsageEvent> mActiveCameraUsage = new ArrayMap<>();
    private final ICameraServiceProxy.Stub mCameraServiceProxy = new ICameraServiceProxy.Stub() {
        public void pingForUserUpdate() {
            if (Binder.getCallingUid() != 1047) {
                Slog.e(CameraServiceProxy.TAG, "Calling UID: " + Binder.getCallingUid() + " doesn't match expected  camera service UID!");
                return;
            }
            CameraServiceProxy.this.notifySwitchWithRetries(250);
        }

        public void notifyCameraState(String cameraId, int newCameraState, int facing, String clientName, int apiLevel) {
            if (Binder.getCallingUid() != 1047) {
                Slog.e(CameraServiceProxy.TAG, "Calling UID: " + Binder.getCallingUid() + " doesn't match expected  camera service UID!");
                return;
            }
            String access$500 = CameraServiceProxy.cameraStateToString(newCameraState);
            String access$600 = CameraServiceProxy.cameraFacingToString(facing);
            CameraServiceProxy.this.updateActivityCount(cameraId, newCameraState, facing, clientName, apiLevel);
            if (SystemProperties.getBoolean("ro.config.led_close_by_camera", false)) {
                BatteryManagerInternal batteryManager = (BatteryManagerInternal) CameraServiceProxy.this.getLocalService(BatteryManagerInternal.class);
                if (facing == 1 && (newCameraState == 0 || newCameraState == 1)) {
                    batteryManager.notifyFrontCameraStates(true);
                } else {
                    batteryManager.notifyFrontCameraStates(false);
                }
            }
            if (SystemProperties.getBoolean("ro.config.hw_media_assistant", false)) {
                IHwCameraServiceProxy hwcsp = HwServiceFactory.getHwCameraServiceProxy(CameraServiceProxy.this.mContext);
                if (hwcsp != null) {
                    hwcsp.notifyCameraStateChange(cameraId, newCameraState, facing, clientName);
                }
            }
        }
    };
    private ICameraService mCameraServiceRaw;
    private final List<CameraUsageEvent> mCameraUsageHistory = new ArrayList();
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public Set<Integer> mEnabledCameraUsers;
    private final Handler mHandler;
    private final ServiceThread mHandlerThread;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                char c = 65535;
                switch (action.hashCode()) {
                    case -2061058799:
                        if (action.equals("android.intent.action.USER_REMOVED")) {
                            c = 1;
                            break;
                        }
                        break;
                    case -385593787:
                        if (action.equals("android.intent.action.MANAGED_PROFILE_ADDED")) {
                            c = 3;
                            break;
                        }
                        break;
                    case -201513518:
                        if (action.equals("android.intent.action.USER_INFO_CHANGED")) {
                            c = 2;
                            break;
                        }
                        break;
                    case 1051477093:
                        if (action.equals("android.intent.action.MANAGED_PROFILE_REMOVED")) {
                            c = 4;
                            break;
                        }
                        break;
                    case 1121780209:
                        if (action.equals("android.intent.action.USER_ADDED")) {
                            c = 0;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        synchronized (CameraServiceProxy.this.mLock) {
                            if (CameraServiceProxy.this.mEnabledCameraUsers != null) {
                                CameraServiceProxy.this.switchUserLocked(CameraServiceProxy.this.mLastUser);
                                break;
                            } else {
                                return;
                            }
                        }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public int mLastUser;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private final MetricsLogger mLogger = new MetricsLogger();
    private final boolean mNotifyNfc;
    private UserManager mUserManager;

    private static class CameraUsageEvent {
        public final int mAPILevel;
        public final int mCameraFacing;
        public final String mClientName;
        private boolean mCompleted = false;
        private long mDurationOrStartTimeMs = SystemClock.elapsedRealtime();

        public CameraUsageEvent(int facing, String clientName, int apiLevel) {
            this.mCameraFacing = facing;
            this.mClientName = clientName;
            this.mAPILevel = apiLevel;
        }

        public void markCompleted() {
            if (!this.mCompleted) {
                this.mCompleted = true;
                this.mDurationOrStartTimeMs = SystemClock.elapsedRealtime() - this.mDurationOrStartTimeMs;
            }
        }

        public long getDuration() {
            if (this.mCompleted) {
                return this.mDurationOrStartTimeMs;
            }
            return 0;
        }
    }

    public CameraServiceProxy(Context context) {
        super(context);
        this.mContext = context;
        boolean z = false;
        this.mHandlerThread = new ServiceThread(TAG, -4, false);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper(), this);
        this.mNotifyNfc = SystemProperties.getInt(NFC_NOTIFICATION_PROP, 0) > 0 ? true : z;
    }

    public boolean handleMessage(Message msg) {
        if (msg.what != 1) {
            Slog.e(TAG, "CameraServiceProxy error, invalid message: " + msg.what);
        } else {
            notifySwitchWithRetries(msg.arg1);
        }
        return true;
    }

    public void onStart() {
        this.mUserManager = UserManager.get(this.mContext);
        if (this.mUserManager != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.USER_ADDED");
            filter.addAction("android.intent.action.USER_REMOVED");
            filter.addAction("android.intent.action.USER_INFO_CHANGED");
            filter.addAction("android.intent.action.MANAGED_PROFILE_ADDED");
            filter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
            this.mContext.registerReceiver(this.mIntentReceiver, filter);
            publishBinderService(CAMERA_SERVICE_PROXY_BINDER_NAME, this.mCameraServiceProxy);
            publishLocalService(CameraServiceProxy.class, this);
            CameraStatsJobService.schedule(this.mContext);
            return;
        }
        throw new IllegalStateException("UserManagerService must start before CameraServiceProxy!");
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
            boolean wasEmpty = this.mActiveCameraUsage.isEmpty();
            this.mActiveCameraUsage.clear();
            if (this.mNotifyNfc && !wasEmpty) {
                notifyNfcService(true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpUsageEvents() {
        int subtype;
        synchronized (this.mLock) {
            Collections.shuffle(this.mCameraUsageHistory);
            for (CameraUsageEvent e : this.mCameraUsageHistory) {
                switch (e.mCameraFacing) {
                    case 0:
                        subtype = 0;
                        break;
                    case 1:
                        subtype = 1;
                        break;
                    case 2:
                        subtype = 2;
                        break;
                    default:
                        continue;
                }
                this.mLogger.write(new LogMaker(1032).setType(4).setSubtype(subtype).setLatency(e.getDuration()).addTaggedData(1322, Integer.valueOf(e.mAPILevel)).setPackageName(e.mClientName));
            }
            this.mCameraUsageHistory.clear();
        }
        long ident = Binder.clearCallingIdentity();
        try {
            CameraStatsJobService.schedule(this.mContext);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: private */
    public void switchUserLocked(int userHandle) {
        Set<Integer> currentUserHandles = getEnabledUserHandles(userHandle);
        this.mLastUser = userHandle;
        if (this.mEnabledCameraUsers == null || !this.mEnabledCameraUsers.equals(currentUserHandles)) {
            this.mEnabledCameraUsers = currentUserHandles;
            notifyMediaserverLocked(1, currentUserHandles);
        }
    }

    private Set<Integer> getEnabledUserHandles(int currentUserHandle) {
        int[] userProfiles = this.mUserManager.getEnabledProfileIds(currentUserHandle);
        Set<Integer> handles = new ArraySet<>(userProfiles.length);
        for (int id : userProfiles) {
            handles.add(Integer.valueOf(id));
        }
        return handles;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0014, code lost:
        if (r7 > 0) goto L_0x0017;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0016, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0017, code lost:
        android.util.Slog.i(TAG, "Could not notify camera service of user switch, retrying...");
        r6.mHandler.sendMessageDelayed(r6.mHandler.obtainMessage(1, r7 - 1, 0, null), 20);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002f, code lost:
        return;
     */
    public void notifySwitchWithRetries(int retries) {
        synchronized (this.mLock) {
            if (this.mEnabledCameraUsers != null) {
                if (notifyMediaserverLocked(1, this.mEnabledCameraUsers)) {
                    retries = 0;
                }
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

    /* access modifiers changed from: private */
    public void updateActivityCount(String cameraId, int newCameraState, int facing, String clientName, int apiLevel) {
        synchronized (this.mLock) {
            boolean wasEmpty = this.mActiveCameraUsage.isEmpty();
            if (this.hwcsp == null) {
                this.hwcsp = HwServiceFactory.getHwCameraServiceProxy(this.mContext);
            }
            if (this.hwcsp != null) {
                this.hwcsp.updateActivityCount(cameraId, newCameraState, facing, clientName);
            }
            switch (newCameraState) {
                case 0:
                    break;
                case 1:
                    CameraUsageEvent oldEvent = this.mActiveCameraUsage.put(cameraId, new CameraUsageEvent(facing, clientName, apiLevel));
                    if (oldEvent != null) {
                        Slog.w(TAG, "Camera " + cameraId + " was already marked as active");
                        oldEvent.markCompleted();
                        this.mCameraUsageHistory.add(oldEvent);
                        break;
                    }
                    break;
                case 2:
                case 3:
                    CameraUsageEvent doneEvent = this.mActiveCameraUsage.remove(cameraId);
                    if (doneEvent != null) {
                        doneEvent.markCompleted();
                        this.mCameraUsageHistory.add(doneEvent);
                        if (this.mCameraUsageHistory.size() > 100) {
                            dumpUsageEvents();
                            break;
                        }
                    }
                    break;
            }
            boolean isEmpty = this.mActiveCameraUsage.isEmpty();
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
            ret[idx] = i.intValue();
            idx++;
        }
        return ret;
    }

    /* access modifiers changed from: private */
    public static String cameraStateToString(int newCameraState) {
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

    /* access modifiers changed from: private */
    public static String cameraFacingToString(int cameraFacing) {
        switch (cameraFacing) {
            case 0:
                return "CAMERA_FACING_BACK";
            case 1:
                return "CAMERA_FACING_FRONT";
            case 2:
                return "CAMERA_FACING_EXTERNAL";
            default:
                return "CAMERA_FACING_UNKNOWN";
        }
    }
}

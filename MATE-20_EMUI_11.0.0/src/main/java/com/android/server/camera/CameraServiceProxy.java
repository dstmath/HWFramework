package com.android.server.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.ICameraService;
import android.hardware.ICameraServiceProxy;
import android.media.AudioManager;
import android.metrics.LogMaker;
import android.nfc.IAppCallback;
import android.nfc.INfcAdapter;
import android.os.BatteryManagerInternal;
import android.os.Binder;
import android.os.Bundle;
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
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.SystemService;
import com.android.server.wm.WindowManagerInternal;
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
    private static final int RETRY_TIMES = 30;
    private static final boolean SHOULD_CLOSE_LED = SystemProperties.getBoolean("ro.config.led_close_by_camera", false);
    private static final String TAG = "CameraService_proxy";
    private static final IBinder nfcInterfaceToken = new Binder();
    private final ArrayMap<String, CameraUsageEvent> mActiveCameraUsage = new ArrayMap<>();
    private final ICameraServiceProxy.Stub mCameraServiceProxy = new ICameraServiceProxy.Stub() {
        /* class com.android.server.camera.CameraServiceProxy.AnonymousClass2 */

        public void pingForUserUpdate() {
            if (Binder.getCallingUid() != 1047) {
                Slog.e(CameraServiceProxy.TAG, "Calling UID: " + Binder.getCallingUid() + " doesn't match expected  camera service UID!");
                return;
            }
            CameraServiceProxy.this.notifySwitchWithRetries(30);
        }

        public void notifyCameraState(String cameraId, int newCameraState, int facing, String clientName, int apiLevel) {
            if (Binder.getCallingUid() != 1047) {
                Slog.e(CameraServiceProxy.TAG, "Calling UID: " + Binder.getCallingUid() + " doesn't match expected  camera service UID!");
                return;
            }
            CameraServiceProxy.cameraStateToString(newCameraState);
            CameraServiceProxy.cameraFacingToString(facing);
            if (CameraServiceProxy.this.mHwCameraServiceProxy == null) {
                CameraServiceProxy cameraServiceProxy = CameraServiceProxy.this;
                cameraServiceProxy.mHwCameraServiceProxy = HwServiceFactory.getHwCameraServiceProxy(cameraServiceProxy.mContext);
            }
            if (CameraServiceProxy.this.mHwCameraServiceProxy != null) {
                CameraServiceProxy.this.mHwCameraServiceProxy.notifyCameraStateChange(cameraId, newCameraState, facing, clientName);
            }
            CameraServiceProxy.this.updateActivityCount(cameraId, newCameraState, facing, clientName, apiLevel);
            if (CameraServiceProxy.SHOULD_CLOSE_LED) {
                BatteryManagerInternal batteryManager = (BatteryManagerInternal) CameraServiceProxy.this.getLocalService(BatteryManagerInternal.class);
                if (facing == 1 && (newCameraState == 0 || newCameraState == 1)) {
                    batteryManager.notifyFrontCameraStates(true);
                } else {
                    batteryManager.notifyFrontCameraStates(false);
                }
            }
        }

        public boolean isLightStrapCaseOn() {
            if (CameraServiceProxy.this.mHwCameraServiceProxy == null) {
                CameraServiceProxy cameraServiceProxy = CameraServiceProxy.this;
                cameraServiceProxy.mHwCameraServiceProxy = HwServiceFactory.getHwCameraServiceProxy(cameraServiceProxy.mContext);
            }
            Slog.i(CameraServiceProxy.TAG, "Get isLightStrapCaseOn in cameraserviceproxy.");
            return CameraServiceProxy.this.mHwCameraServiceProxy.isLightStrapCaseOn();
        }
    };
    private ICameraService mCameraServiceRaw;
    private final List<CameraUsageEvent> mCameraUsageHistory = new ArrayList();
    private final Context mContext;
    private Set<Integer> mEnabledCameraUsers;
    private final Handler mHandler;
    private final ServiceThread mHandlerThread;
    private IHwCameraServiceProxy mHwCameraServiceProxy = null;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.android.server.camera.CameraServiceProxy.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
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
                if (c == 0 || c == 1 || c == 2 || c == 3 || c == 4) {
                    synchronized (CameraServiceProxy.this.mLock) {
                        if (CameraServiceProxy.this.mEnabledCameraUsers != null) {
                            CameraServiceProxy.this.switchUserLocked(CameraServiceProxy.this.mLastUser);
                        }
                    }
                }
            }
        }
    };
    private int mLastUser;
    private final Object mLock = new Object();
    private final MetricsLogger mLogger = new MetricsLogger();
    private final boolean mNotifyNfc;
    private UserManager mUserManager;

    /* access modifiers changed from: private */
    public static class CameraUsageEvent {
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
        if (this.mHwCameraServiceProxy == null) {
            this.mHwCameraServiceProxy = HwServiceFactory.getHwCameraServiceProxy(this.mContext);
        }
        IHwCameraServiceProxy iHwCameraServiceProxy = this.mHwCameraServiceProxy;
        if (iHwCameraServiceProxy != null) {
            iHwCameraServiceProxy.startObservingLightStrapCaseStatus();
        }
    }

    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message msg) {
        if (msg.what != 1) {
            Slog.e(TAG, "CameraServiceProxy error, invalid message: " + msg.what);
        } else {
            notifySwitchWithRetries(msg.arg1);
        }
        return true;
    }

    @Override // com.android.server.SystemService
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

    @Override // com.android.server.SystemService
    public void onStartUser(int userHandle) {
        synchronized (this.mLock) {
            if (this.mEnabledCameraUsers == null) {
                switchUserLocked(userHandle);
            }
        }
    }

    @Override // com.android.server.SystemService
    public void onSwitchUser(int userHandle) {
        synchronized (this.mLock) {
            switchUserLocked(userHandle);
        }
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        synchronized (this.mLock) {
            this.mCameraServiceRaw = null;
            boolean wasEmpty = this.mActiveCameraUsage.isEmpty();
            this.mActiveCameraUsage.clear();
            if (this.mNotifyNfc && !wasEmpty) {
                notifyNfcService(true);
            }
            if (this.mHwCameraServiceProxy == null) {
                this.mHwCameraServiceProxy = HwServiceFactory.getHwCameraServiceProxy(this.mContext);
            }
            if (this.mHwCameraServiceProxy != null) {
                this.mHwCameraServiceProxy.binderDied();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpUsageEvents() {
        int subtype;
        synchronized (this.mLock) {
            Collections.shuffle(this.mCameraUsageHistory);
            for (CameraUsageEvent e : this.mCameraUsageHistory) {
                int i = e.mCameraFacing;
                if (i == 0) {
                    subtype = 0;
                } else if (i == 1) {
                    subtype = 1;
                } else if (i == 2) {
                    subtype = 2;
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
    /* access modifiers changed from: public */
    private void switchUserLocked(int userHandle) {
        Set<Integer> currentUserHandles = getEnabledUserHandles(userHandle);
        this.mLastUser = userHandle;
        Set<Integer> set = this.mEnabledCameraUsers;
        if (set == null || !set.equals(currentUserHandles)) {
            this.mEnabledCameraUsers = currentUserHandles;
            notifySwitchWithRetriesLocked(30);
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
    /* access modifiers changed from: public */
    private void notifySwitchWithRetries(int retries) {
        synchronized (this.mLock) {
            notifySwitchWithRetriesLocked(retries);
        }
    }

    private void notifySwitchWithRetriesLocked(int retries) {
        int retryCount = retries;
        Set<Integer> set = this.mEnabledCameraUsers;
        if (set != null && notifyCameraserverLocked(1, set)) {
            retryCount = 0;
        }
        if (retryCount > 0) {
            Slog.i(TAG, "Could not notify camera service of user switch, retrying...");
            Handler handler = this.mHandler;
            handler.sendMessageDelayed(handler.obtainMessage(1, retries - 1, 0, null), 20);
        }
    }

    private boolean notifyCameraserverLocked(int eventType, Set<Integer> updatedUserHandles) {
        if (this.mCameraServiceRaw == null) {
            IBinder cameraServiceBinder = getBinderService(CAMERA_SERVICE_BINDER_NAME);
            if (cameraServiceBinder == null) {
                Slog.w(TAG, "Could not notify cameraserver, camera service not available.");
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
            Slog.w(TAG, "Could not notify cameraserver, remote exception: " + e2);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateActivityCount(String cameraId, int newCameraState, int facing, String clientName, int apiLevel) {
        synchronized (this.mLock) {
            boolean wasEmpty = this.mActiveCameraUsage.isEmpty();
            if (newCameraState != 0) {
                int i = 0;
                if (newCameraState == 1) {
                    boolean alreadyActivePackage = false;
                    while (true) {
                        if (i >= this.mActiveCameraUsage.size()) {
                            break;
                        } else if (this.mActiveCameraUsage.valueAt(i).mClientName.equals(clientName)) {
                            alreadyActivePackage = true;
                            break;
                        } else {
                            i++;
                        }
                    }
                    if (!alreadyActivePackage) {
                        ((WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class)).addNonHighRefreshRatePackage(clientName);
                    }
                    CameraUsageEvent oldEvent = this.mActiveCameraUsage.put(cameraId, new CameraUsageEvent(facing, clientName, apiLevel));
                    if (oldEvent != null) {
                        Slog.w(TAG, "Camera " + cameraId + " was already marked as active");
                        oldEvent.markCompleted();
                        this.mCameraUsageHistory.add(oldEvent);
                    }
                } else if (newCameraState == 2 || newCameraState == 3) {
                    CameraUsageEvent doneEvent = this.mActiveCameraUsage.remove(cameraId);
                    if (doneEvent != null) {
                        doneEvent.markCompleted();
                        this.mCameraUsageHistory.add(doneEvent);
                        if (this.mCameraUsageHistory.size() > 100) {
                            dumpUsageEvents();
                        }
                        boolean stillActivePackage = false;
                        while (true) {
                            if (i >= this.mActiveCameraUsage.size()) {
                                break;
                            } else if (this.mActiveCameraUsage.valueAt(i).mClientName.equals(clientName)) {
                                stillActivePackage = true;
                                break;
                            } else {
                                i++;
                            }
                        }
                        if (!stillActivePackage) {
                            ((WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class)).removeNonHighRefreshRatePackage(clientName);
                        }
                    }
                }
            } else {
                AudioManager audioManager = (AudioManager) getContext().getSystemService(AudioManager.class);
                if (audioManager != null) {
                    String facingStr = facing == 0 ? "back" : "front";
                    audioManager.setParameters("cameraFacing=" + facingStr);
                }
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
            INfcAdapter.Stub.asInterface(nfcServiceBinder).setReaderMode(nfcInterfaceToken, (IAppCallback) null, enablePolling ? 0 : 4096, (Bundle) null);
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
        if (newCameraState == 0) {
            return "CAMERA_STATE_OPEN";
        }
        if (newCameraState == 1) {
            return "CAMERA_STATE_ACTIVE";
        }
        if (newCameraState == 2) {
            return "CAMERA_STATE_IDLE";
        }
        if (newCameraState != 3) {
            return "CAMERA_STATE_UNKNOWN";
        }
        return "CAMERA_STATE_CLOSED";
    }

    /* access modifiers changed from: private */
    public static String cameraFacingToString(int cameraFacing) {
        if (cameraFacing == 0) {
            return "CAMERA_FACING_BACK";
        }
        if (cameraFacing == 1) {
            return "CAMERA_FACING_FRONT";
        }
        if (cameraFacing != 2) {
            return "CAMERA_FACING_UNKNOWN";
        }
        return "CAMERA_FACING_EXTERNAL";
    }
}

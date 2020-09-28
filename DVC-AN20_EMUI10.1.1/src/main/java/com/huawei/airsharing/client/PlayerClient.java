package com.huawei.airsharing.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;
import com.huawei.airsharing.api.AidlMediaPlayerListenerStub;
import com.huawei.airsharing.api.AuthenticationInfo;
import com.huawei.airsharing.api.ConfigInfo;
import com.huawei.airsharing.api.ConnectInfo;
import com.huawei.airsharing.api.ERepeatMode;
import com.huawei.airsharing.api.Event;
import com.huawei.airsharing.api.IEventListener;
import com.huawei.airsharing.api.IKitServiceStatusListener;
import com.huawei.airsharing.api.IPlayerManager;
import com.huawei.airsharing.api.PlayInfo;
import com.huawei.airsharing.api.ProjectionDevice;
import com.huawei.airsharing.api.RemoteCtrlEventProcessorStub;
import com.huawei.airsharing.client.IAidlHwAuthenManager;
import com.huawei.airsharing.client.IAidlHwPlayerManager;
import com.huawei.airsharing.constant.Constant;
import com.huawei.airsharing.util.HwLog;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerClient implements IPlayerManager, IEventListener {
    private static final String AIRSHARING_PKG_NAME = "com.huawei.android.airsharing";
    private static final int INVALID_PID = -1;
    private static final String MIRRORSHARE_PKG_NAME = "com.huawei.android.mirrorshare";
    private static final String TAG = "KitPlayerClient";
    private static final int THREAD_NUM = 5;
    private static PlayerClient sInstance = null;
    private static final HwLog sLog = HwLog.getInstance();
    private IBinder.DeathRecipient binderDeath = new BinderDeathRecipient();
    private boolean hasSubscribe = false;
    private IAidlHwAuthenManager mAidlHwAuthenManager = null;
    private IAidlHwPlayerManager mAidlHwPlayerManager = null;
    private final Object mBinderLock = new Object();
    private final Object mCastCLock = new Object();
    private Context mContext;
    private IEventListener mEventListener = null;
    private ExecutorService mExecutor = Executors.newFixedThreadPool(5);
    private KitServiceStatusListener mKitEventListener;
    private final Object mListenerLock = new Object();
    private int mPid = INVALID_PID;
    private ServiceConnection mPlayerServiceConnection = new ServiceConnection() {
        /* class com.huawei.airsharing.client.PlayerClient.AnonymousClass1 */

        public void onServiceDisconnected(ComponentName name) {
            PlayerClient.sLog.d(PlayerClient.TAG, "bind PlayerService onServiceDisconnected");
            synchronized (PlayerClient.this.mBinderLock) {
                PlayerClient.this.mAidlHwPlayerManager = null;
            }
            PlayerClient.this.mServiceConnectStatus = EServiceConnectStatus.SERVICE_DISCONNECTED;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
            com.huawei.airsharing.client.PlayerClient.sLog.d(com.huawei.airsharing.client.PlayerClient.TAG, "set kit listener");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x009e, code lost:
            if (r7.this$0.mKitEventListener != null) goto L_0x00ad;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x00a0, code lost:
            r7.this$0.mKitEventListener = new com.huawei.airsharing.client.PlayerClient.KitServiceStatusListener(r7.this$0, null);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x00ad, code lost:
            r7.this$0.mAidlHwPlayerManager.setKitServiceStatusListener(new com.huawei.airsharing.client.KitServiceStatusAgent(r7.this$0.mKitEventListener));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x00c3, code lost:
            com.huawei.airsharing.client.PlayerClient.sLog.e(com.huawei.airsharing.client.PlayerClient.TAG, "onServiceConnected throw RemoteException");
         */
        public void onServiceConnected(ComponentName name, IBinder authService) {
            PlayerClient.sLog.d(PlayerClient.TAG, "bind PlayerService onServiceConnected");
            PlayerClient.this.mServiceConnectStatus = EServiceConnectStatus.SERVICE_CONNECTED;
            HwLog hwLog = PlayerClient.sLog;
            hwLog.d(PlayerClient.TAG, "mSubscribeServerType = " + PlayerClient.this.mSubscribeServerType);
            synchronized (PlayerClient.this.mBinderLock) {
                PlayerClient.this.mAidlHwAuthenManager = IAidlHwAuthenManager.Stub.asInterface(authService);
                IBinder service = null;
                try {
                    service = PlayerClient.this.mAidlHwAuthenManager.checkPermission();
                } catch (RemoteException e) {
                    HwLog hwLog2 = PlayerClient.sLog;
                    hwLog2.d(PlayerClient.TAG, "onServiceConnected checkPermission failed with exception = " + e.getLocalizedMessage());
                }
                if (service == null) {
                    PlayerClient.this.linkToDeath(authService);
                    PlayerClient.sLog.d(PlayerClient.TAG, "onServiceConnected but don't have permission");
                    return;
                }
                PlayerClient.this.mAidlHwPlayerManager = IAidlHwPlayerManager.Stub.asInterface(service);
            }
            PlayerClient.this.linkToDeath(authService);
        }
    };
    private EServiceConnectStatus mServiceConnectStatus = EServiceConnectStatus.SERVICE_DISCONNECTED;
    private String mSubscribeServerType = Constant.SUBSCRIBE_TYPE_ALL;

    public enum EServiceConnectStatus {
        SERVICE_DISCONNECTED,
        SERVICE_DISCONNECTING,
        SERVICE_CONNECTING,
        SERVICE_CONNECTED
    }

    public enum PortType {
        HISIGHT_PORT,
        HICALL_PORT
    }

    public static synchronized PlayerClient getInstance() {
        PlayerClient playerClient;
        synchronized (PlayerClient.class) {
            if (sInstance == null) {
                sInstance = new PlayerClient();
            }
            playerClient = sInstance;
        }
        return playerClient;
    }

    public boolean isSupportCast(Context context) {
        if (context == null) {
            sLog.e(TAG, "context is null, return false");
            return false;
        }
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            sLog.e(TAG, "PackageManager is null, return false");
            return false;
        }
        for (ApplicationInfo appInfo : pm.getInstalledApplications(Constant.CAMERA)) {
            if (appInfo != null && ("com.huawei.android.airsharing".equals(appInfo.packageName) || "com.huawei.android.mirrorshare".equals(appInfo.packageName))) {
                return true;
            }
        }
        return false;
    }

    @Override // com.huawei.airsharing.api.IServerManager
    public boolean init(Context context) {
        sLog.setLogLevel(false, true, false, true, true);
        sLog.d(TAG, "set log");
        if (context == null) {
            sLog.e(TAG, "context is null");
            return false;
        }
        sLog.d(TAG, "init in with context.");
        this.mContext = context;
        this.mPid = Process.myPid();
        return bindHwPlayerService();
    }

    @Override // com.huawei.airsharing.api.IServerManager
    public void deInit() {
        unsubscribServers();
        unbindHwPlayerService();
        this.mPid = INVALID_PID;
        sLog.d(TAG, "deInit in");
    }

    private class SubServerRunnable implements Runnable {
        private String serverType;

        private SubServerRunnable() {
            this.serverType = null;
        }

        public void run() {
            try {
                HwLog hwLog = PlayerClient.sLog;
                hwLog.d(PlayerClient.TAG, "SubServerRunnable in， hasSubscribe " + PlayerClient.this.hasSubscribe);
                int pid = PlayerClient.this.mPid;
                String serType = this.serverType;
                IAidlHwPlayerManager aidlHwPlayerManager = PlayerClient.this.mAidlHwPlayerManager;
                if (aidlHwPlayerManager != null && !PlayerClient.this.hasSubscribe) {
                    aidlHwPlayerManager.clsHwSharingListener(pid, new EventListenerAgent(PlayerClient.getInstance()));
                    aidlHwPlayerManager.setHwSharingListener(pid, new EventListenerAgent(PlayerClient.getInstance()));
                    aidlHwPlayerManager.subscribServers(pid, serType);
                    PlayerClient.this.hasSubscribe = true;
                    PlayerClient.this.notifyEventAsync(IEventListener.EVENT_ID_NOTIFY_PLAYER_START_RESULT, IEventListener.EVENT_TYPE_PLAYER_START_SUCCESS);
                }
            } catch (RemoteException e) {
                PlayerClient.sLog.w(PlayerClient.TAG, "run RemoteException");
            }
        }
    }

    private void unsubscribServers() {
        sLog.d(TAG, "unsubscribServers in");
        int pid = this.mPid;
        IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
        if (aidlHwPlayerManager == null) {
            sLog.w(TAG, "unsubscribServers without PlayerService Init");
            return;
        }
        try {
            sLog.d(TAG, "mAidlHwPlayerManager.unsubscribServers");
            aidlHwPlayerManager.unsubscribServers(pid, Constant.SUBSCRIBE_TYPE_ALL);
            this.hasSubscribe = false;
        } catch (RemoteException e) {
            sLog.w(TAG, "unsubscribServers aidl throw exception");
        }
        sLog.d(TAG, "deInit out");
    }

    @Override // com.huawei.airsharing.api.IServerManager
    public void registerListener(IEventListener listener) {
        setHwSharingListener(listener);
    }

    @Override // com.huawei.airsharing.api.IServerManager
    public void unregisterListener(IEventListener listener) {
        clsHwSharingListener(listener);
    }

    private void setHwSharingListener(IEventListener mHwSharingListener) {
        HwLog hwLog = sLog;
        hwLog.d(TAG, "setHwSharingListener in mHwSharingListener:" + mHwSharingListener);
        if (this.mServiceConnectStatus == EServiceConnectStatus.SERVICE_DISCONNECTED || this.mServiceConnectStatus == EServiceConnectStatus.SERVICE_DISCONNECTING || mHwSharingListener == null) {
            sLog.w(TAG, "mHwSharingListener is null or PlayerService not init");
            return;
        }
        synchronized (this.mListenerLock) {
            this.mEventListener = mHwSharingListener;
            HwLog hwLog2 = sLog;
            hwLog2.d(TAG, "setHwSharingListener out mEventListener:" + this.mEventListener);
        }
    }

    private void clsHwSharingListener(IEventListener mHwSharingListener) {
        sLog.d(TAG, "clsHwSharingListener in");
        if (mHwSharingListener == null) {
            sLog.w(TAG, "mHwSharingListener is null or PlayerService not init");
            return;
        }
        int pid = this.mPid;
        synchronized (this.mListenerLock) {
            this.mEventListener = null;
        }
        IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
        if (aidlHwPlayerManager != null) {
            try {
                aidlHwPlayerManager.clsHwSharingListener(pid, new EventListenerAgent(this));
            } catch (RemoteException e) {
                sLog.w(TAG, "clsHwSharingListener throw RemoteException");
            }
        }
    }

    public boolean connectDevice(ConnectInfo connInfo) {
        synchronized (this.mCastCLock) {
            sLog.i(TAG, "connectDevice in");
            if (connInfo != null) {
                if (connInfo.getProjectionDevice() != null) {
                    HwLog hwLog = sLog;
                    hwLog.i(TAG, "connInfo: " + connInfo.toString());
                    IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
                    if (aidlHwPlayerManager == null || !this.hasSubscribe) {
                        sLog.e(TAG, "aidlHwPlayerManager is null or playerService not init");
                        return false;
                    }
                    try {
                        return aidlHwPlayerManager.castConnectDevice(connInfo);
                    } catch (RemoteException e) {
                        sLog.e(TAG, "connectDevice throw exception");
                        return false;
                    }
                }
            }
            sLog.e(TAG, "connInfo or projectionDevice is null");
            return false;
        }
    }

    @Override // com.huawei.airsharing.api.IPlayerManager
    public boolean play(PlayInfo info) {
        synchronized (this.mCastCLock) {
            sLog.i(TAG, "play in");
            if (!(info == null || info.getCastPlayInfo() == null)) {
                if (info.getMediaMetadataArray() != null) {
                    HwLog hwLog = sLog;
                    hwLog.i(TAG, "info: " + info.toString());
                    IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
                    if (aidlHwPlayerManager == null || !this.hasSubscribe) {
                        sLog.e(TAG, "aidlHwPlayerManager is null or playerService not init");
                        return false;
                    }
                    try {
                        return aidlHwPlayerManager.castPlay(info);
                    } catch (RemoteException e) {
                        sLog.e(TAG, "play throw RemoteException");
                        return false;
                    }
                }
            }
            sLog.e(TAG, "play failed because info is null");
            return false;
        }
    }

    @Override // com.huawei.airsharing.api.IPlayerManager
    public boolean updatePlayInfo(PlayInfo info) {
        synchronized (this.mCastCLock) {
            sLog.i(TAG, "updatePlayInfo in");
            if (info != null) {
                if (info.getCastPlayInfo() != null) {
                    HwLog hwLog = sLog;
                    hwLog.i(TAG, "info: " + info.toString());
                    IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
                    if (aidlHwPlayerManager == null || !this.hasSubscribe) {
                        sLog.e(TAG, "aidlHwPlayerManager is null or playerService not init");
                        return false;
                    }
                    try {
                        return aidlHwPlayerManager.updatePlayInfo(info);
                    } catch (RemoteException e) {
                        sLog.e(TAG, "play throw RemoteException");
                        return false;
                    }
                }
            }
            sLog.e(TAG, "updatePlayInfo failed because info is null");
            return false;
        }
    }

    public boolean registerMediaPlayerListener(AidlMediaPlayerListenerStub listener) {
        synchronized (this.mCastCLock) {
            sLog.i(TAG, "registerMediaPlayerListener in");
            if (listener == null) {
                sLog.e(TAG, "listener is null");
                return false;
            }
            IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
            if (aidlHwPlayerManager == null || !this.hasSubscribe) {
                sLog.e(TAG, "aidlHwPlayerManager is null or playerService not init");
                return false;
            }
            try {
                return aidlHwPlayerManager.registerMediaPlayerListener(listener);
            } catch (RemoteException e) {
                sLog.e(TAG, "registerMediaPlayerListener throw RemoteException");
                return false;
            }
        }
    }

    public boolean unregisterMediaPlayerListener() {
        synchronized (this.mCastCLock) {
            sLog.i(TAG, "unregisterMediaPlayerListener in");
            IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
            if (aidlHwPlayerManager == null || !this.hasSubscribe) {
                sLog.e(TAG, "aidlHwPlayerManager is null or playerService not init");
                return false;
            }
            try {
                return aidlHwPlayerManager.unregisterMediaPlayerListener();
            } catch (RemoteException e) {
                sLog.e(TAG, "unregisterMediaPlayerListener throw RemoteException");
                return false;
            }
        }
    }

    @Override // com.huawei.airsharing.api.IPlayerManager
    public boolean previous() {
        synchronized (this.mCastCLock) {
            sLog.i(TAG, "previous in");
            IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
            if (aidlHwPlayerManager == null || !this.hasSubscribe) {
                sLog.e(TAG, "aidlHwPlayerManager is null or playerService not init");
                return false;
            }
            try {
                return aidlHwPlayerManager.previous();
            } catch (RemoteException e) {
                sLog.e(TAG, "previous throw RemoteException");
                return false;
            }
        }
    }

    @Override // com.huawei.airsharing.api.IPlayerManager
    public boolean next() {
        synchronized (this.mCastCLock) {
            sLog.i(TAG, "next in");
            IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
            if (aidlHwPlayerManager == null || !this.hasSubscribe) {
                sLog.e(TAG, "aidlHwPlayerManager is null or playerService not init");
                return false;
            }
            try {
                return aidlHwPlayerManager.next();
            } catch (RemoteException e) {
                sLog.e(TAG, "next throw RemoteException");
                return false;
            }
        }
    }

    @Override // com.huawei.airsharing.api.IPlayerManager
    public boolean setRepeatMode(ERepeatMode repeatMode) {
        synchronized (this.mCastCLock) {
            sLog.i(TAG, "setRepeatMode in");
            if (repeatMode == null) {
                sLog.e(TAG, "setRepeatMode failed because repeatMode is null");
                return false;
            }
            HwLog hwLog = sLog;
            hwLog.i(TAG, "repeatMode: " + repeatMode.toString());
            IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
            if (aidlHwPlayerManager == null || !this.hasSubscribe) {
                sLog.e(TAG, "aidlHwPlayerManager is null or playerService not init");
                return false;
            }
            try {
                return aidlHwPlayerManager.setRepeatMode(repeatMode);
            } catch (RemoteException e) {
                sLog.e(TAG, "setRepeatMode throw exception");
                return false;
            }
        }
    }

    @Override // com.huawei.airsharing.api.IPlayerManager
    public boolean playMediaItem(int index) {
        synchronized (this.mCastCLock) {
            sLog.i(TAG, "playMediaItem in");
            if (index < 0) {
                HwLog hwLog = sLog;
                hwLog.e(TAG, "index is invalid: " + index);
                return false;
            }
            HwLog hwLog2 = sLog;
            hwLog2.i(TAG, "index: " + index);
            IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
            if (aidlHwPlayerManager == null || !this.hasSubscribe) {
                sLog.e(TAG, "aidlHwPlayerManager is null or playerService not init");
                return false;
            }
            try {
                return aidlHwPlayerManager.playMediaItem(index);
            } catch (RemoteException e) {
                sLog.e(TAG, "playMediaItem throw exception");
                return false;
            }
        }
    }

    @Override // com.huawei.airsharing.api.IPlayerManager
    public boolean seek(int targetPosition) {
        synchronized (this.mCastCLock) {
            HwLog hwLog = sLog;
            hwLog.d(TAG, "seek in targetPosition: " + targetPosition);
            if (targetPosition < 0) {
                sLog.e(TAG, "seek failed because targetPosition is invalid");
                return false;
            }
            IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
            if (aidlHwPlayerManager == null || !this.hasSubscribe) {
                sLog.e(TAG, "aidlHwPlayerManager is null or playerService not init");
                return false;
            }
            try {
                return aidlHwPlayerManager.seekTo(targetPosition);
            } catch (RemoteException e) {
                sLog.w(TAG, "catch seek throw RemoteException");
                return false;
            }
        }
    }

    @Override // com.huawei.airsharing.api.IPlayerManager
    public boolean setVolumeMute(boolean isMute) {
        synchronized (this.mCastCLock) {
            HwLog hwLog = sLog;
            hwLog.d(TAG, "setVolumeMute in isMute: " + isMute);
            IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
            if (aidlHwPlayerManager == null || !this.hasSubscribe) {
                sLog.e(TAG, "aidlHwPlayerManager is null or playerService not init");
                return false;
            }
            try {
                return aidlHwPlayerManager.setVolumeMute(isMute);
            } catch (RemoteException e) {
                sLog.w(TAG, "catch setVolumeMute throw RemoteException");
                return false;
            }
        }
    }

    @Override // com.huawei.airsharing.api.IPlayerManager
    public boolean pause() {
        synchronized (this.mCastCLock) {
            sLog.i(TAG, "pause in");
            int pid = this.mPid;
            IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
            if (aidlHwPlayerManager == null || !this.hasSubscribe) {
                sLog.e(TAG, "pause without aidlHwPlayerManager set");
                return false;
            }
            try {
                return aidlHwPlayerManager.pause(pid);
            } catch (RemoteException e) {
                sLog.e(TAG, "catch pause throw exception");
                return false;
            }
        }
    }

    @Override // com.huawei.airsharing.api.IPlayerManager
    public boolean resume() {
        synchronized (this.mCastCLock) {
            sLog.d(TAG, "resume in");
            int pid = this.mPid;
            IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
            if (aidlHwPlayerManager == null || !this.hasSubscribe) {
                sLog.w(TAG, "resume without PlayerService Init");
                return false;
            }
            try {
                return aidlHwPlayerManager.resume(pid);
            } catch (RemoteException e) {
                sLog.w(TAG, "catch resume throw exception");
                return false;
            }
        }
    }

    @Override // com.huawei.airsharing.api.IPlayerManager
    public boolean stop() {
        synchronized (this.mCastCLock) {
            sLog.d(TAG, "stop in");
            int pid = this.mPid;
            IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
            if (aidlHwPlayerManager == null || !this.hasSubscribe) {
                sLog.w(TAG, "stop without PlayerService Init");
                return false;
            }
            try {
                return aidlHwPlayerManager.stop(pid);
            } catch (RemoteException e) {
                sLog.w(TAG, "catch Stop throw exception");
                return false;
            }
        }
    }

    @Override // com.huawei.airsharing.api.IPlayerManager
    public int getVolume() {
        sLog.d(TAG, "getVolume in");
        int pid = this.mPid;
        IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
        if (aidlHwPlayerManager == null || !this.hasSubscribe) {
            sLog.w(TAG, "getVolume without PlayerService Init");
            return 0;
        }
        try {
            return aidlHwPlayerManager.getVolume(pid);
        } catch (RemoteException e) {
            sLog.w(TAG, "catch getVolume throw exception");
            return 0;
        }
    }

    @Override // com.huawei.airsharing.api.IPlayerManager
    public boolean setVolume(int volume) {
        synchronized (this.mCastCLock) {
            HwLog hwLog = sLog;
            hwLog.d(TAG, "setVolume in volume=" + volume);
            if (volume < 0) {
                sLog.w(TAG, "volume value is invalid");
                return false;
            }
            int pid = this.mPid;
            IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
            if (!this.hasSubscribe || aidlHwPlayerManager == null) {
                sLog.w(TAG, "setVolume without PlayerService Init");
                return false;
            }
            try {
                return aidlHwPlayerManager.setVolume(pid, volume);
            } catch (RemoteException e) {
                sLog.w(TAG, "catch setVolume throw exception");
                return false;
            }
        }
    }

    private boolean bindHwPlayerService() {
        String pkgName = "com.huawei.android.airsharing";
        boolean isAirSharing = isPackageExist(pkgName);
        boolean bindServiceResult = false;
        HwLog hwLog = sLog;
        hwLog.d(TAG, "bindHwPlayerService in, isAirSharing=" + isAirSharing);
        if (!isAirSharing) {
            pkgName = "com.huawei.android.mirrorshare";
        }
        if (this.mServiceConnectStatus == EServiceConnectStatus.SERVICE_CONNECTED || this.mServiceConnectStatus == EServiceConnectStatus.SERVICE_CONNECTING) {
            sLog.w(TAG, "bindHwPlayerService service has bind");
            return true;
        }
        if (this.mContext != null) {
            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(pkgName, "com.huawei.android.airsharing.kitadapter.KitPlayerService"));
                intent.putExtra("pid", Process.myPid());
                bindServiceResult = this.mContext.bindService(intent, this.mPlayerServiceConnection, 65);
            } catch (IllegalArgumentException e) {
                sLog.w(TAG, "bindService throw IllegalArgumentException");
            } catch (SecurityException e2) {
                sLog.w(TAG, "bindService throw SecurityException");
            }
        }
        if (!bindServiceResult) {
            this.mServiceConnectStatus = EServiceConnectStatus.SERVICE_DISCONNECTED;
        } else if (this.mServiceConnectStatus != EServiceConnectStatus.SERVICE_CONNECTED) {
            this.mServiceConnectStatus = EServiceConnectStatus.SERVICE_CONNECTING;
        }
        HwLog hwLog2 = sLog;
        hwLog2.d(TAG, "bindHwPlayerService out bindServiceResult = " + bindServiceResult);
        return bindServiceResult;
    }

    private void unbindHwPlayerService() {
        sLog.d(TAG, "unbindHwPlayerService in");
        int pid = this.mPid;
        try {
            IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
            if (aidlHwPlayerManager != null) {
                aidlHwPlayerManager.clsHwSharingListener(pid, new EventListenerAgent(getInstance()));
                clsKitServiceStatusListener();
                aidlHwPlayerManager.unregisterMediaPlayerListener();
            }
            if (this.mContext != null && this.mPlayerServiceConnection != null) {
                this.mContext.unbindService(this.mPlayerServiceConnection);
                this.mServiceConnectStatus = EServiceConnectStatus.SERVICE_DISCONNECTED;
                unlinkToDeath();
                synchronized (this.mBinderLock) {
                    this.mAidlHwPlayerManager = null;
                }
            }
        } catch (IllegalArgumentException e) {
            sLog.w(TAG, "unbind throw IllegalArgumentException");
        } catch (RemoteException e2) {
            sLog.w(TAG, "unbindHwPlayerService throw RemoteException");
        }
    }

    /* access modifiers changed from: private */
    public class KitServiceStatusListener implements IKitServiceStatusListener {
        private KitServiceStatusListener() {
        }

        @Override // com.huawei.airsharing.api.IKitServiceStatusListener
        public void onKitEvent(int eventId) {
            HwLog hwLog = PlayerClient.sLog;
            hwLog.d(PlayerClient.TAG, "onkitEvent eventId " + eventId);
            if (eventId == 1000) {
                SubServerRunnable subRunnable = new SubServerRunnable();
                subRunnable.serverType = PlayerClient.this.mSubscribeServerType;
                PlayerClient.this.mExecutor.execute(subRunnable);
            }
        }
    }

    private void clsKitServiceStatusListener() {
        if (this.mKitEventListener == null) {
            sLog.w(TAG, "mKitEventListener is null");
            return;
        }
        IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
        if (aidlHwPlayerManager != null) {
            try {
                aidlHwPlayerManager.clsKitServiceStatusListener();
            } catch (RemoteException e) {
                sLog.e(TAG, "on kit clsKitServiceStatusListener throw RemoteException");
            }
            this.mKitEventListener = null;
        }
    }

    private class BinderDeathRecipient implements IBinder.DeathRecipient {
        private BinderDeathRecipient() {
        }

        public synchronized void binderDied() {
            PlayerClient.sLog.w(PlayerClient.TAG, "binderDied");
            synchronized (PlayerClient.this.mBinderLock) {
                PlayerClient.this.mAidlHwPlayerManager = null;
            }
            PlayerClient.this.notifyEventAsync(IEventListener.EVENT_ID_BINDER_DIED, "");
            PlayerClient.this.mServiceConnectStatus = EServiceConnectStatus.SERVICE_DISCONNECTED;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void linkToDeath(IBinder service) {
        HwLog hwLog = sLog;
        hwLog.d(TAG, "linkToDeath:service=" + service);
        if (service != null) {
            try {
                service.linkToDeath(this.binderDeath, 0);
            } catch (Exception e) {
                sLog.e(TAG, "linkToDeath service fail.");
            }
        }
    }

    private synchronized void unlinkToDeath() {
        sLog.d(TAG, "unlinkToDeath");
        try {
            IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
            if (!(aidlHwPlayerManager == null || aidlHwPlayerManager.asBinder() == null)) {
                aidlHwPlayerManager.asBinder().unlinkToDeath(this.binderDeath, 0);
            }
        } catch (Exception e) {
            sLog.e(TAG, "unlinkToDeath service fail.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyEventAsync(int eventId, String type) {
        EventNotifyRunnable eventNotifyRunnable = new EventNotifyRunnable();
        eventNotifyRunnable.eventId = eventId;
        eventNotifyRunnable.type = type;
        this.mExecutor.execute(eventNotifyRunnable);
    }

    /* access modifiers changed from: private */
    public class EventNotifyRunnable implements Runnable {
        private int eventId;
        private String type;

        private EventNotifyRunnable() {
            this.eventId = PlayerClient.INVALID_PID;
            this.type = null;
        }

        public void run() {
            PlayerClient.this.onEvent(this.eventId, this.type);
        }
    }

    @Override // com.huawei.airsharing.api.IEventListener
    public boolean onEvent(int eventId, String type) {
        HwLog hwLog = sLog;
        hwLog.w(TAG, "onEvent eventId = " + eventId + " || " + type);
        synchronized (this.mListenerLock) {
            if (this.mEventListener != null) {
                if (eventId > 0) {
                    return this.mEventListener.onEvent(eventId, type);
                }
            }
            sLog.w(TAG, "invalid event id or listener has not init");
            return false;
        }
    }

    @Override // com.huawei.airsharing.api.IEventListener
    public void onProjectionDeviceUpdate(int eventId, ProjectionDevice device) {
        HwLog hwLog = sLog;
        hwLog.d(TAG, "onProjectionDeviceUpdate, eventId=" + eventId);
        synchronized (this.mListenerLock) {
            if (this.mEventListener != null) {
                this.mEventListener.onProjectionDeviceUpdate(eventId, device);
            }
        }
    }

    @Override // com.huawei.airsharing.api.IEventListener
    public void onEventHandle(Event event) {
        if (event == null) {
            sLog.d(TAG, "onEventHandle, event is null!");
            return;
        }
        HwLog hwLog = sLog;
        hwLog.d(TAG, "onEventHandle, eventId= " + event.getEventId());
        synchronized (this.mListenerLock) {
            if (this.mEventListener != null) {
                this.mEventListener.onEventHandle(event);
            }
        }
    }

    public boolean startScan() {
        return startScan(48);
    }

    public boolean startScan(int type) {
        int i = this.mPid;
        HwLog hwLog = sLog;
        hwLog.d(TAG, "startScan in, type=" + type);
        IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
        if (aidlHwPlayerManager == null || !this.hasSubscribe) {
            sLog.w(TAG, "startScan without PlayerService Init");
            return false;
        }
        try {
            return aidlHwPlayerManager.startScan(type);
        } catch (RemoteException e) {
            sLog.w(TAG, "catch scanDevice throw exception");
            return false;
        }
    }

    public boolean stopScan() {
        return stopScanDevice(false);
    }

    public boolean stopScan(int type) {
        return stopScanDevice(false);
    }

    private boolean stopScanDevice(boolean isActiveStop) {
        int pid = this.mPid;
        HwLog hwLog = sLog;
        hwLog.d(TAG, "stopScanDevice in, pid=" + pid + ", isActiveScan=" + isActiveStop);
        IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
        if (aidlHwPlayerManager == null || !this.hasSubscribe) {
            sLog.w(TAG, "stopScanDevice without PlayerService Init");
            return false;
        }
        try {
            return aidlHwPlayerManager.stopScanDevice(pid, isActiveStop);
        } catch (RemoteException e) {
            sLog.w(TAG, "catch stopScanDevice throw exception");
            return false;
        }
    }

    public boolean disconnectDevice(ProjectionDevice device) {
        if (device == null) {
            sLog.w(TAG, "device is null, disconnect device");
        }
        return disconnectDevice();
    }

    public boolean disconnectDevice() {
        int pid = this.mPid;
        HwLog hwLog = sLog;
        hwLog.d(TAG, "disconnectDevice in, pid=" + pid);
        IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
        if (aidlHwPlayerManager == null || !this.hasSubscribe) {
            sLog.w(TAG, "disconnectDevice without PlayerService Init");
            return false;
        }
        try {
            return aidlHwPlayerManager.disconnectDevice(pid);
        } catch (RemoteException e) {
            sLog.w(TAG, "catch disconnectDevice throw exception");
            return false;
        }
    }

    public ProjectionDevice getTargetProjectionDevice() {
        HwLog hwLog = sLog;
        hwLog.d(TAG, "getTargetProjectionDevice in, pid=" + this.mPid);
        IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
        if (aidlHwPlayerManager == null || !this.hasSubscribe) {
            sLog.w(TAG, "getTargetProjectionDevice without PlayerService Init");
            return null;
        }
        try {
            return aidlHwPlayerManager.getTargetProjectionDevice(this.mPid);
        } catch (RemoteException e) {
            sLog.w(TAG, "catch getTargetProjectionDevice throw exception");
            return null;
        }
    }

    public boolean isConnected(ProjectionDevice device) {
        return isConnected();
    }

    public boolean isConnected() {
        int pid = this.mPid;
        HwLog hwLog = sLog;
        hwLog.d(TAG, "isConnected in, pid=" + pid);
        IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
        if (aidlHwPlayerManager == null || !this.hasSubscribe) {
            sLog.w(TAG, "isConnected without PlayerService Init");
            return false;
        }
        try {
            return aidlHwPlayerManager.isConnected(pid);
        } catch (RemoteException e) {
            sLog.w(TAG, "catch isConnected throw exception");
            return false;
        }
    }

    public boolean isDisplayConnecting() {
        int pid = this.mPid;
        HwLog hwLog = sLog;
        hwLog.d(TAG, "isDisplayConnecting in, pid=" + pid);
        IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
        if (aidlHwPlayerManager == null || !this.hasSubscribe) {
            sLog.w(TAG, "isDisplayConnecting without PlayerService Init");
            return false;
        }
        try {
            return aidlHwPlayerManager.isDisplayConnecting(pid);
        } catch (RemoteException e) {
            sLog.w(TAG, "catch isDisplayConnecting throw exception");
            return false;
        }
    }

    public boolean isDisplayConnected() {
        int pid = this.mPid;
        HwLog hwLog = sLog;
        hwLog.d(TAG, "isDisplayConnected in, pid=" + pid);
        IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
        if (aidlHwPlayerManager == null || !this.hasSubscribe) {
            sLog.w(TAG, "isDisplayConnected without PlayerService Init");
            return false;
        }
        try {
            return aidlHwPlayerManager.isDisplayConnected(pid);
        } catch (RemoteException e) {
            sLog.w(TAG, "catch isDisplayConnected throw exception");
            return false;
        }
    }

    private boolean isPackageExist(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            PackageManager packageManager = this.mContext.getPackageManager();
            if (packageManager == null) {
                return false;
            }
            packageManager.getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public int getCastKitVersion() {
        return getSdkVersion();
    }

    private int getSdkVersion() {
        int pid = this.mPid;
        HwLog hwLog = sLog;
        hwLog.d(TAG, "getSdkVersion in, pid=" + pid);
        IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
        if (aidlHwPlayerManager == null || !this.hasSubscribe) {
            sLog.w(TAG, "getSdkVersion without PlayerService Init");
            return INVALID_PID;
        }
        try {
            return aidlHwPlayerManager.getSdkVersion();
        } catch (RemoteException e) {
            sLog.w(TAG, "catch getSdkVersion throw exception");
            return 0;
        }
    }

    /* renamed from: com.huawei.airsharing.client.PlayerClient$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$airsharing$client$PlayerClient$PortType = new int[PortType.values().length];

        static {
            try {
                $SwitchMap$com$huawei$airsharing$client$PlayerClient$PortType[PortType.HISIGHT_PORT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$airsharing$client$PlayerClient$PortType[PortType.HICALL_PORT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public int getPort(PortType type) {
        int i = AnonymousClass2.$SwitchMap$com$huawei$airsharing$client$PlayerClient$PortType[type.ordinal()];
        if (i == 1) {
            return getHiSightServerPort();
        }
        if (i == 2) {
            return getMsdpServerPort();
        }
        sLog.e(TAG, "the input type is not supported");
        return INVALID_PID;
    }

    private int getHiSightServerPort() {
        HwLog hwLog = sLog;
        hwLog.d(TAG, "getHiSightServerPort in, pid = " + this.mPid);
        IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
        if (aidlHwPlayerManager == null || !this.hasSubscribe) {
            sLog.w(TAG, "getHiSightServerPort without PlayerService Init");
            return INVALID_PID;
        }
        try {
            return aidlHwPlayerManager.getHiSightServerPort();
        } catch (RemoteException e) {
            sLog.e(TAG, "catch getHiSightServerPort throw exception");
            return INVALID_PID;
        }
    }

    private int getMsdpServerPort() {
        sLog.d(TAG, "get msdp server port in");
        IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
        if (aidlHwPlayerManager == null || !this.hasSubscribe) {
            sLog.w(TAG, "getHiSightServerPort without PlayerService Init");
            return INVALID_PID;
        }
        try {
            return aidlHwPlayerManager.getMsdpServerPort();
        } catch (RemoteException e) {
            sLog.e(TAG, "catch getMsdpServerPort throw exception");
            return INVALID_PID;
        }
    }

    public boolean appendHiSightExInfo(int type, byte[] data, int len, long ts) {
        HwLog hwLog = sLog;
        hwLog.d(TAG, "appendHiSightExInfo in, pid = " + this.mPid);
        IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
        if (aidlHwPlayerManager == null || !this.hasSubscribe) {
            sLog.w(TAG, "appendHiSightExInfo without PlayerService Init");
            return false;
        }
        try {
            return aidlHwPlayerManager.appendHiSightExInfo(type, data, len, ts);
        } catch (RemoteException e) {
            sLog.e(TAG, "catch appendHiSightExInfo throw exception");
            return false;
        }
    }

    public void setRemoteCtrlEventProcessor(RemoteCtrlEventProcessorStub processor) {
        int pid = this.mPid;
        HwLog hwLog = sLog;
        hwLog.d(TAG, "setRemoteCtrlEventProcessor in, pid=" + pid);
        IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
        if (aidlHwPlayerManager == null || !this.hasSubscribe) {
            sLog.w(TAG, "setRemoteCtrlEventProcessor without PlayerService Init");
            return;
        }
        try {
            aidlHwPlayerManager.setRemoteCtrlEventProcessor(processor);
        } catch (RemoteException e) {
            sLog.w(TAG, "catch setRemoteCtrlEventProcessor throw exception");
        }
    }

    public int sendRemoteCtrlData(int eventType, int len, byte[] data) {
        int pid = this.mPid;
        HwLog hwLog = sLog;
        hwLog.d(TAG, "sendRemoteCtrlData in, pid= " + pid);
        IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
        if (aidlHwPlayerManager == null || !this.hasSubscribe) {
            sLog.w(TAG, "sendRemoteCtrlData without PlayerService Init");
            return INVALID_PID;
        }
        try {
            return aidlHwPlayerManager.sendRemoteCtrlData(eventType, len, data);
        } catch (RemoteException e) {
            sLog.w(TAG, "catch sendRemoteCtrlData throw exception");
            return INVALID_PID;
        }
    }

    public boolean setAuthenticationInfo(AuthenticationInfo info) {
        HwLog hwLog = sLog;
        hwLog.d(TAG, "setAuthenticationInfo in, pid= " + this.mPid);
        IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
        if (aidlHwPlayerManager == null || !this.hasSubscribe) {
            sLog.w(TAG, "setAuthenticationInfo without PlayerService Init");
            return false;
        }
        try {
            return aidlHwPlayerManager.setAuthenticationInfo(info);
        } catch (RemoteException e) {
            sLog.w(TAG, "catch setAuthenticationInfo throw exception");
            return false;
        }
    }

    public boolean showProjectionActivity(ConfigInfo info) {
        sLog.i(TAG, "showProjectionActivity");
        IAidlHwPlayerManager aidlHwPlayerManager = this.mAidlHwPlayerManager;
        if (aidlHwPlayerManager == null || !this.hasSubscribe) {
            sLog.w(TAG, "showProjectionActivity without PlayerService Init");
            return false;
        } else if (info == null) {
            sLog.e(TAG, "showProjectionActivity failed because info is null");
            return false;
        } else {
            try {
                return aidlHwPlayerManager.showProjectionActivity(info);
            } catch (RemoteException e) {
                sLog.w(TAG, "catch showProjectionActivity throw exception");
                return false;
            }
        }
    }

    public EServiceConnectStatus getServiceStatus() {
        return this.mServiceConnectStatus;
    }

    public boolean isServiceDisconnected() {
        return this.mServiceConnectStatus == EServiceConnectStatus.SERVICE_DISCONNECTED || this.mServiceConnectStatus == EServiceConnectStatus.SERVICE_DISCONNECTING;
    }
}

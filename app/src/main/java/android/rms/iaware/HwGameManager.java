package android.rms.iaware;

import android.os.Binder;
import android.os.IBinder.DeathRecipient;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.rms.iaware.IHwGameCallback.Stub;

public final class HwGameManager implements DeathRecipient {
    private static final int NOTIFY_FAILURE = 2;
    private static final int NOTIFY_NOTREGISTED = 1;
    private static final int NOTIFY_SUCCESS = 0;
    private static final String TAG = "HwGameManager";
    private static HwGameManager sInstance;
    private GameSDKCallBack gameSDKCbk;
    private IHwGameCallback mHwGameCallback;
    private boolean mIsRegistered;
    private final Object mLock;

    public interface GameSDKCallBack {
        void changeContinuousFpsMissedRate(int i, int i2);

        void changeDxFpsRate(int i, float f);

        void changeFpsRate(int i);

        void changeMuteEnabled(boolean z);

        void changeSpecialEffects(int i);

        void queryExpectedFps(int[] iArr, int[] iArr2);
    }

    private HwGameManager() {
        this.mLock = new Object();
        this.gameSDKCbk = null;
        this.mHwGameCallback = new Stub() {
            public void changeFpsRate(int fps) throws RemoteException {
                if (HwGameManager.this.gameSDKCbk != null) {
                    HwGameManager.this.gameSDKCbk.changeFpsRate(fps);
                }
            }

            public void changeSpecialEffects(int level) throws RemoteException {
                if (HwGameManager.this.gameSDKCbk != null) {
                    HwGameManager.this.gameSDKCbk.changeSpecialEffects(level);
                }
            }

            public void changeMuteEnabled(boolean enabled) throws RemoteException {
                if (HwGameManager.this.gameSDKCbk != null) {
                    HwGameManager.this.gameSDKCbk.changeMuteEnabled(enabled);
                }
            }

            public void changeContinuousFpsMissedRate(int cycle, int maxFrameMissed) throws RemoteException {
                if (HwGameManager.this.gameSDKCbk != null) {
                    HwGameManager.this.gameSDKCbk.changeContinuousFpsMissedRate(cycle, maxFrameMissed);
                }
            }

            public void changeDxFpsRate(int cycle, float maxFrameDx) throws RemoteException {
                if (HwGameManager.this.gameSDKCbk != null) {
                    HwGameManager.this.gameSDKCbk.changeDxFpsRate(cycle, maxFrameDx);
                }
            }

            public void queryExpectedFps(int[] outExpectedFps, int[] outRealFps) throws RemoteException {
                if (HwGameManager.this.gameSDKCbk != null) {
                    HwGameManager.this.gameSDKCbk.queryExpectedFps(outExpectedFps, outRealFps);
                }
            }

            public int getPid() {
                return Process.myPid();
            }
        };
        this.mIsRegistered = false;
    }

    public static HwGameManager getInstance() {
        HwGameManager hwGameManager;
        synchronized (HwGameManager.class) {
            if (sInstance == null) {
                sInstance = new HwGameManager();
            }
            hwGameManager = sInstance;
        }
        return hwGameManager;
    }

    private boolean initGameMgrService() {
        IHwGameManager service = getService();
        if (service != null) {
            try {
                service.asBinder().linkToDeath(this, NOTIFY_SUCCESS);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "linkToDeath failed !");
            }
            return true;
        }
        AwareLog.d(TAG, "Game Server is not found. calling pid: " + Binder.getCallingPid());
        return false;
    }

    private IHwGameManager getService() {
        return IHwGameManager.Stub.asInterface(ServiceManager.getService("hwgamemanager"));
    }

    public void setGameSDKCallBack(GameSDKCallBack gameSDKCallback) {
        this.gameSDKCbk = gameSDKCallback;
    }

    public boolean noteGameProcessStarted(int pid, int uid) {
        IHwGameManager service = getService();
        if (service == null) {
            AwareLog.e(TAG, "noteGameProcessStarted service null!");
            return false;
        }
        try {
            return service.noteGameProcessStarted(pid, uid, this.mHwGameCallback);
        } catch (RemoteException e) {
            AwareLog.e(TAG, "noteGameProcessStarted RemoteException!");
            return false;
        }
    }

    public boolean notifyGameScene(int gameScene, int cpuLevel, int gpuLevel) {
        boolean z = true;
        IHwGameManager service = getService();
        if (service == null) {
            AwareLog.e(TAG, "notifyGameScene service null!");
            return false;
        }
        if (!this.mIsRegistered) {
            initGameMgrService();
            this.mIsRegistered = noteGameProcessStarted(Process.myPid(), Process.myUid());
        }
        try {
            int ret = service.notifyGameScene(gameScene, cpuLevel, gpuLevel);
            if (NOTIFY_NOTREGISTED == ret) {
                binderDied();
            }
            if (ret != 0) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "notifyGameScene RemoteException!");
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void binderDied() {
        AwareLog.d(TAG, "Game Process Binder was died and connecting ...");
        synchronized (this.mLock) {
            int maxCount = 60;
            while (true) {
                if (initGameMgrService() || maxCount <= 0) {
                    noteGameProcessStarted(Process.myPid(), Process.myUid());
                } else {
                    SystemClock.sleep(1000);
                    maxCount--;
                }
            }
        }
    }
}

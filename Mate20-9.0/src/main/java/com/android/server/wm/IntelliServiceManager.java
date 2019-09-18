package com.android.server.wm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import com.android.internal.view.RotationPolicy;
import com.huawei.IntelliServer.intellilib.IIntelliListener;
import com.huawei.IntelliServer.intellilib.IIntelliService;
import com.huawei.IntelliServer.intellilib.IntelliAlgoResult;

public class IntelliServiceManager {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.HWLog;
    private static final int DELAY_TIME = 800;
    private static final int INTELLI_ALGO_FACE_ORIENTION = 2;
    public static final int INTELLI_FACE_ORIENTION_TIMEMOUT = -2;
    private static final String RO_SWITCH = "ro.config.face_detect";
    private static final String RO_SWITCH_ROTATION = "ro.config.face_smart_rotation";
    private static final String SERVICE_ACTION = "com.huawei.intelliServer.intelliServer";
    private static final String SERVICE_CLASS = "com.huawei.intelliServer.intelliServer.IntelliService";
    private static final String SERVICE_PACKAGE = "com.huawei.intelliServer.intelliServer";
    private static final String SETTING_SWITCH = "smart_rotation_key";
    public static final String TAG = "IntelliServiceManager";
    private static int mDisplayRotation;
    private static IntelliServiceManager mInstance = null;
    private static int mSensorRotation;
    /* access modifiers changed from: private */
    public FaceRotationCallback mCallback;
    /* access modifiers changed from: private */
    public ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            IIntelliService unused = IntelliServiceManager.this.mRemote = IIntelliService.Stub.asInterface(iBinder);
            IntelliServiceManager.this.registerListener("serviceconnect");
            IntelliServiceManager.this.mContext.getMainThreadHandler().removeCallbacks(IntelliServiceManager.this.mFinishRunnble);
            IntelliServiceManager.this.mContext.getMainThreadHandler().postDelayed(IntelliServiceManager.this.mFinishRunnble, 800);
        }

        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(IntelliServiceManager.TAG, "onServiceDisconnected");
            IIntelliService unused = IntelliServiceManager.this.mRemote = null;
            boolean unused2 = IntelliServiceManager.this.mIsKeepPortrait = false;
            boolean unused3 = IntelliServiceManager.this.mIsRunning = false;
            FaceRotationCallback unused4 = IntelliServiceManager.this.mCallback = null;
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    private int mFaceRotaion = -2;
    Runnable mFinishRunnble = new Runnable() {
        public void run() {
            Slog.w(IntelliServiceManager.TAG, "detect face timeout");
            IntelliServiceManager.this.stopIntelliService(-2);
        }
    };
    /* access modifiers changed from: private */
    public IIntelliListener mIntelliEventListener = new IIntelliListener.Stub() {
        public void onEvent(IntelliAlgoResult intelliAlgoResult) throws RemoteException {
            if (IntelliServiceManager.this.mCallback == null) {
                return;
            }
            if (intelliAlgoResult == null) {
                Slog.e(IntelliServiceManager.TAG, "intelliAlgoResult is null");
                IntelliServiceManager.this.stopIntelliService(-2);
            } else if (intelliAlgoResult.getRotation() != -1) {
                if (IntelliServiceManager.DEBUG) {
                    Slog.i(IntelliServiceManager.TAG, "intelliAlgoResult rotation: " + intelliAlgoResult.getRotation());
                }
                IntelliServiceManager.this.stopIntelliService(intelliAlgoResult.getRotation());
            }
        }

        public void onErr(int i) throws RemoteException {
            Slog.e(IntelliServiceManager.TAG, "face Rotation onErr " + i);
        }
    };
    /* access modifiers changed from: private */
    public boolean mIsKeepPortrait = false;
    /* access modifiers changed from: private */
    public boolean mIsRunning = false;
    /* access modifiers changed from: private */
    public IIntelliService mRemote;
    /* access modifiers changed from: private */
    public Intent mServiceIntent;
    /* access modifiers changed from: private */
    public long startTime;

    public interface FaceRotationCallback {
        void onEvent(int i);
    }

    private IntelliServiceManager(Context context) {
        this.mContext = context;
        this.mServiceIntent = new Intent("com.huawei.intelliServer.intelliServer");
        this.mServiceIntent.setClassName("com.huawei.intelliServer.intelliServer", SERVICE_CLASS);
    }

    public static IntelliServiceManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (IntelliServiceManager.class) {
                if (mInstance == null) {
                    mInstance = new IntelliServiceManager(context);
                }
            }
        }
        return mInstance;
    }

    public static boolean isRotationConsistentFR() {
        boolean z = true;
        if (mDisplayRotation == -1 || mSensorRotation == -1) {
            return true;
        }
        if (mDisplayRotation != mSensorRotation) {
            z = false;
        }
        return z;
    }

    public static void setSensorRotation(int sensorRotation) {
        mSensorRotation = sensorRotation;
    }

    public static void setDisplayRotation(int displayRotation) {
        mDisplayRotation = displayRotation;
    }

    /* access modifiers changed from: private */
    public void stopIntelliService(final int faceRotation) {
        this.mFaceRotaion = faceRotation;
        this.mIsKeepPortrait = false;
        new Thread(new Runnable() {
            public void run() {
                if (IntelliServiceManager.this.mCallback != null) {
                    IntelliServiceManager.this.mCallback.onEvent(faceRotation);
                }
                IntelliServiceManager.this.mContext.getMainThreadHandler().removeCallbacks(IntelliServiceManager.this.mFinishRunnble);
                boolean unused = IntelliServiceManager.this.mIsRunning = false;
                FaceRotationCallback unused2 = IntelliServiceManager.this.mCallback = null;
                if (IntelliServiceManager.this.mRemote != null) {
                    try {
                        IntelliServiceManager.this.mRemote.unregistListener(2, IntelliServiceManager.this.mIntelliEventListener);
                    } catch (RemoteException e) {
                        Slog.e(IntelliServiceManager.TAG, "unregisiterListener fail");
                    }
                    Slog.i(IntelliServiceManager.TAG, "close camera, detect cost " + Long.toString(IntelliServiceManager.this.startTime - System.currentTimeMillis()));
                }
            }
        }).start();
    }

    public int getFaceRotaion() {
        return this.mFaceRotaion;
    }

    public boolean isKeepPortrait() {
        return this.mIsKeepPortrait;
    }

    public void setKeepPortrait(boolean IsKeepPortrait) {
        this.mIsKeepPortrait = IsKeepPortrait;
        if (DEBUG) {
            Slog.d(TAG, "set IsKeepPortrait = " + this.mIsKeepPortrait);
        }
    }

    /* access modifiers changed from: private */
    public void registerListener(String reason) {
        Slog.i(TAG, "registerListener for:" + reason + "cost " + Long.toString(this.startTime - System.currentTimeMillis()));
        new Thread(new Runnable() {
            public void run() {
                try {
                    if (IntelliServiceManager.this.mRemote != null) {
                        IntelliServiceManager.this.mRemote.registListener(2, IntelliServiceManager.this.mIntelliEventListener);
                    }
                } catch (RemoteException e) {
                    Slog.e(IntelliServiceManager.TAG, "regisiterListener fail ");
                }
            }
        }).start();
    }

    private void bindIntelliService() {
        new Thread(new Runnable() {
            public void run() {
                Slog.d(IntelliServiceManager.TAG, "bindIntelliService");
                IntelliServiceManager.this.mContext.bindServiceAsUser(IntelliServiceManager.this.mServiceIntent, IntelliServiceManager.this.mConnection, 1, UserHandle.CURRENT);
            }
        }).start();
    }

    public void unbindIntelliService() {
        if (this.mRemote != null) {
            new Thread(new Runnable() {
                public void run() {
                    Slog.d(IntelliServiceManager.TAG, "unbindIntelliService");
                    IntelliServiceManager.this.mContext.unbindService(IntelliServiceManager.this.mConnection);
                    IIntelliService unused = IntelliServiceManager.this.mRemote = null;
                }
            }).start();
        }
    }

    public void startIntelliService(FaceRotationCallback faceRotationCallback) {
        if (faceRotationCallback == null) {
            Slog.w(TAG, "face rotation call back is null");
            return;
        }
        if (!this.mIsRunning) {
            this.mIsRunning = true;
            this.startTime = System.currentTimeMillis();
            if (this.mRemote == null) {
                bindIntelliService();
            } else {
                registerListener("startservice");
            }
        } else if (DEBUG) {
            Slog.d(TAG, "IntelliService is already running");
        }
        this.mContext.getMainThreadHandler().removeCallbacks(this.mFinishRunnble);
        this.mContext.getMainThreadHandler().postDelayed(this.mFinishRunnble, 800);
        this.mCallback = faceRotationCallback;
    }

    public static boolean isIntelliServiceEnabled(Context context, int orientation, int userId) {
        if (RotationPolicy.isRotationLocked(context) || SystemProperties.getInt(RO_SWITCH, 0) == 0 || !SystemProperties.getBoolean(RO_SWITCH_ROTATION, true)) {
            return false;
        }
        if (Settings.Secure.getIntForUser(context.getContentResolver(), SETTING_SWITCH, 0, userId) != 1) {
            Slog.d(TAG, "setting switch is off for user " + userId);
            return false;
        } else if (orientation == 0 || orientation == 1 || orientation == 5 || orientation == 6 || orientation == 8) {
            Slog.d(TAG, "skip process orientation " + orientation);
            return false;
        } else if (!isRotationConsistentFR()) {
            return true;
        } else {
            Slog.d(TAG, "sensor rotation consistent with display");
            return false;
        }
    }
}

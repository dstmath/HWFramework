package com.android.server.wm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.util.Slog;
import com.android.internal.view.RotationPolicy;
import com.huawei.IntelliServer.intellilib.IIntelliListener;
import com.huawei.IntelliServer.intellilib.IIntelliService;
import com.huawei.IntelliServer.intellilib.IIntelliService.Stub;
import com.huawei.IntelliServer.intellilib.IntelliAlgoResult;

public class IntelliServiceManager {
    private static final boolean DEBUG = Log.HWLog;
    private static final int DELAY_TIME = 800;
    private static final int INTELLI_ALGO_FACE_ORIENTION = 2;
    public static final int INTELLI_FACE_ORIENTION_TIMEMOUT = -2;
    private static final String RO_SWITCH = "ro.config.face_detect";
    private static final String RO_SWITCH_ROTATION = "ro.config.face_smart_rotation";
    private static final String SERVICE_ACTION = "com.huawei.intelliServer.intelliServer";
    private static final String SERVICE_CLASS = "com.huawei.intelliServer.intelliServer.IntelliService";
    private static final String SERVICE_PACKAGE = "com.huawei.intelliServer.intelliServer";
    private static final String SETTING_SWITCH = "smart_rotation_key";
    private static final String TAG = "WindowManager";
    private static int mDisplayRotation;
    private static IntelliServiceManager mInstance = null;
    private static int mSensorRotation;
    private FaceRotationCallback mCallback;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Slog.d(IntelliServiceManager.TAG, "face_rotation:onServiceConnected time = " + Long.toString(IntelliServiceManager.this.startTime - System.currentTimeMillis()));
            IntelliServiceManager.this.mRemote = Stub.asInterface(iBinder);
            IntelliServiceManager.this.registerListener();
            IntelliServiceManager.this.mContext.getMainThreadHandler().removeCallbacks(IntelliServiceManager.this.mFinishRunnble);
            IntelliServiceManager.this.mContext.getMainThreadHandler().postDelayed(IntelliServiceManager.this.mFinishRunnble, 800);
        }

        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(IntelliServiceManager.TAG, "face_rotation: onServiceDisconnected");
            IntelliServiceManager.this.mRemote = null;
            IntelliServiceManager.this.mIsKeepPortrait = false;
            IntelliServiceManager.this.mIsRunning = false;
            IntelliServiceManager.this.mCallback = null;
        }
    };
    private Context mContext;
    private int mFaceRotaion = -2;
    Runnable mFinishRunnble = new Runnable() {
        public void run() {
            Slog.w(IntelliServiceManager.TAG, "face_rotation: detect face timeout");
            IntelliServiceManager.this.stopIntelliService(-2);
        }
    };
    private IIntelliListener mIntelliEventListener = new IIntelliListener.Stub() {
        public void onEvent(IntelliAlgoResult intelliAlgoResult) throws RemoteException {
            if (IntelliServiceManager.this.mCallback == null) {
                return;
            }
            if (intelliAlgoResult == null) {
                Slog.e(IntelliServiceManager.TAG, "face_rotation: intelliAlgoResult is null");
                IntelliServiceManager.this.stopIntelliService(-2);
            } else if (intelliAlgoResult.getRotation() != -1) {
                if (IntelliServiceManager.DEBUG) {
                    Slog.i(IntelliServiceManager.TAG, " face_rotation: rotation from intelli service  = " + intelliAlgoResult.getRotation());
                }
                IntelliServiceManager.this.stopIntelliService(intelliAlgoResult.getRotation());
            }
        }

        public void onErr(int i) throws RemoteException {
            Slog.e(IntelliServiceManager.TAG, "face_rotation:Face Rotation onErr " + i);
        }
    };
    private boolean mIsKeepPortrait = false;
    private boolean mIsRunning = false;
    private IIntelliService mRemote;
    private Intent mServiceIntent;
    private long startTime;

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

    private void stopIntelliService(final int faceRotation) {
        this.mFaceRotaion = faceRotation;
        this.mIsKeepPortrait = false;
        new Thread(new Runnable() {
            public void run() {
                if (IntelliServiceManager.this.mCallback != null) {
                    IntelliServiceManager.this.mCallback.onEvent(faceRotation);
                }
                IntelliServiceManager.this.mContext.getMainThreadHandler().removeCallbacks(IntelliServiceManager.this.mFinishRunnble);
                IntelliServiceManager.this.mIsRunning = false;
                IntelliServiceManager.this.mCallback = null;
                if (IntelliServiceManager.this.mRemote != null) {
                    try {
                        IntelliServiceManager.this.mRemote.unregistListener(2, IntelliServiceManager.this.mIntelliEventListener);
                    } catch (RemoteException e) {
                        Slog.e(IntelliServiceManager.TAG, "face_rotation:unregisiterListener fail ");
                    }
                    Slog.i(IntelliServiceManager.TAG, "face_rotation: close camera .total detect time = " + Long.toString(IntelliServiceManager.this.startTime - System.currentTimeMillis()));
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
            Slog.d(TAG, "face_rotation: set IsKeepPortrait = " + this.mIsKeepPortrait);
        }
    }

    private void registerListener() {
        Slog.i(TAG, "face_rotation: registerListener, open camera.");
        new Thread(new Runnable() {
            public void run() {
                try {
                    if (IntelliServiceManager.this.mRemote != null) {
                        IntelliServiceManager.this.mRemote.registListener(2, IntelliServiceManager.this.mIntelliEventListener);
                    }
                } catch (RemoteException e) {
                    Slog.e(IntelliServiceManager.TAG, "face_rotation: regisiterListener fail ");
                }
            }
        }).start();
    }

    private void bindIntelliService() {
        new Thread(new Runnable() {
            public void run() {
                IntelliServiceManager.this.mContext.bindServiceAsUser(IntelliServiceManager.this.mServiceIntent, IntelliServiceManager.this.mConnection, 1, UserHandle.CURRENT);
            }
        }).start();
    }

    public void unbindIntelliService() {
        if (this.mRemote != null) {
            new Thread(new Runnable() {
                public void run() {
                    Slog.d(IntelliServiceManager.TAG, "face_rotation: unbindService");
                    IntelliServiceManager.this.mContext.unbindService(IntelliServiceManager.this.mConnection);
                    IntelliServiceManager.this.mRemote = null;
                }
            }).start();
        }
    }

    public void startIntelliService(FaceRotationCallback faceRotationCallback) {
        if (faceRotationCallback == null) {
            Slog.w(TAG, "face_rotation: call back is null.intelliservice not started.");
            return;
        }
        if (!this.mIsRunning) {
            this.mIsRunning = true;
            this.startTime = System.currentTimeMillis();
            if (this.mRemote == null) {
                Slog.i(TAG, "face_rotation: startIntelliService bindIntelliService when mRemote null");
                bindIntelliService();
            } else {
                if (DEBUG) {
                    Slog.i(TAG, "face_rotation: startIntelliService registerListener when mRemote not null");
                }
                registerListener();
            }
        } else if (DEBUG) {
            Slog.d(TAG, "face_rotation:is running not start again.");
        }
        Slog.d(TAG, "face_rotation: post delayed");
        this.mContext.getMainThreadHandler().removeCallbacks(this.mFinishRunnble);
        this.mContext.getMainThreadHandler().postDelayed(this.mFinishRunnble, 800);
        this.mCallback = faceRotationCallback;
    }

    public static boolean isIntelliServiceEnabled(Context context, int orientation, int userId) {
        if (RotationPolicy.isRotationLocked(context)) {
            return false;
        }
        if (SystemProperties.getInt(RO_SWITCH, 0) == 0 || !SystemProperties.getBoolean(RO_SWITCH_ROTATION, true)) {
            Slog.d(TAG, "face_rotation: isIntelliServiceEnabled: ro swtich off");
            return false;
        } else if (Secure.getIntForUser(context.getContentResolver(), SETTING_SWITCH, 0, userId) != 1) {
            Slog.d(TAG, "face_rotation:isIntelliServiceEnabled: setting swtich off for user " + userId);
            return false;
        } else if (orientation == 0 || orientation == 1 || orientation == 5 || orientation == 6 || orientation == 8) {
            Slog.d(TAG, "face_rotation: orientation force not enable " + orientation);
            return false;
        } else if (!isRotationConsistentFR()) {
            return true;
        } else {
            Slog.d(TAG, "face_rotation: not enabled:sensor rotation consistent with display. ");
            return false;
        }
    }
}

package com.huawei.server.fingerprint;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.media.AudioAttributes;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewRootImpl;
import android.widget.ImageView;
import com.android.server.biometrics.fingerprint.FingerprintUtils;
import com.huawei.android.os.HwVibrator;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import vendor.huawei.hardware.biometrics.fingerprint.V2_2.IExtBiometricsFingerprint;

public class FingerprintController {
    private static final int DALTONIZER_BLUE = 13;
    private static final int DALTONIZER_DEFAULT = -1;
    private static final int DALTONIZER_DISABLE = 0;
    private static final int DALTONIZER_ENABLE = 1;
    private static final int DALTONIZER_GREEN = 12;
    private static final int DALTONIZER_RED = 11;
    private static final long[] DEFAULT_SUCCESS_VIBRATION_PATTERNS = {0, 100};
    private static final String DISPLAY_DALTONIZER = "accessibility_display_daltonizer";
    private static final String DISPLAY_DALTONIZER_ENABLED = "accessibility_display_daltonizer_enabled";
    private static final String DISPLAY_INVERSION_ENABLED = "accessibility_display_inversion_enabled";
    private static final AudioAttributes FINGERPRINT_SONFICATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    private static final int FP_CLOSE = 0;
    private static final int FP_OPEN = 1;
    private static final int INVALID_VALUE = -1;
    private static final float[] INVERSION_DALTONIZER_ENABLE_BLUE_MATRIXS = {0.402621f, -0.597511f, -0.598226f, 1.770658f, 2.096248f, -1.5581f, -3.166945f, -2.493749f, 1.154019f};
    private static final float[] INVERSION_DALTONIZER_ENABLE_BLUE_TRANSLATIONS = {0.993666f, 0.995013f, 1.002308f};
    private static final float[] INVERSION_DALTONIZER_ENABLE_GREEN_MATRIXS = {3.422987f, -0.82169f, 0.496602f, -4.194635f, 0.050041f, -2.269793f, -0.227896f, -0.227896f, 0.771648f};
    private static final float[] INVERSION_DALTONIZER_ENABLE_GREEN_TRANSLATIONS = {0.999544f, 0.999544f, 1.001543f};
    private static final float[] INVERSION_DALTONIZER_ENABLE_RED_MATRIXS = {1.454283f, -1.335127f, -1.202954f, -2.225931f, 0.563479f, -0.570237f, -0.227896f, -0.227896f, 0.771648f};
    private static final float[] INVERSION_DALTONIZER_ENABLE_RED_TRANSLATIONS = {0.999544f, 0.999544f, 1.001543f};
    private static final int INVERSION_DISABLE = 0;
    private static final int INVERSION_ENABLE = 1;
    private static final float[] INVERSION_ENABLE_MATRIXS = {0.402f, -0.598f, -0.599f, -1.174f, -0.174f, -1.175f, -0.228f, -0.228f, 0.772f};
    private static final float[] INVERSION_ENABLE_TRANSLATIONS = {1.0f, 1.0f, 1.0f};
    private static final String KEY_KEYGUARD_ENABLE = "fp_keyguard_enable";
    private static final float[] NO_INVERSION_DALTONIZER_ENABLE_BLUE_MATRIXS = {1.0f, -0.0f, 0.0f, 2.94039f, 3.266886f, -0.382545f, -2.940389f, -2.266886f, 1.382545f};
    private static final float[] NO_INVERSION_DALTONIZER_ENABLE_BLUE_TRANSLATIONS = {0.0f, 0.0f, 0.0f};
    private static final float[] NO_INVERSION_DALTONIZER_ENABLE_GREEN_MATRIXS = {4.020943f, -0.223734f, 1.094754f, -3.020942f, 1.223734f, -1.094753f, -0.0f, 0.0f, 1.0f};
    private static final float[] NO_INVERSION_DALTONIZER_ENABLE_GREEN_TRANSLATIONS = {0.0f, 0.0f, 0.0f};
    private static final float[] NO_INVERSION_DALTONIZER_ENABLE_RED_MATRIXS = {2.052239f, -0.737172f, -0.604803f, -1.052239f, 1.737172f, 0.604803f, -0.0f, 0.0f, 1.0f};
    private static final float[] NO_INVERSION_DALTONIZER_ENABLE_RED_TRANSLATIONS = {0.0f, 0.0f, 0.0f};
    private static final String PKGNAME_OF_KEYGUARD = "com.android.systemui";
    private static final String PROXIMITY_TP = "proximity-tp";
    private static final int REFRESH_RATE_CODE = 20000;
    private static final String SUPER_WALLPAPER_EFFECT = "in_wallpaper_effect";
    private static final int SUPER_WALLPAPER_STATE = 1;
    private static final String TAG = "FingerprintController";
    private static FingerprintController sInstance;
    private boolean isFingerViewRemove = false;
    private boolean isForbidGotoSleepFlag = false;
    private boolean isSupportHwFingerVibrateView = false;
    private ContentResolver mContentResolver;
    private String mCurrentPackageName;
    private int mDaltonizer;
    private int mDaltonizerEnable;
    private DisplayManager mDisplayManager;
    private Handler mHandler;
    private int mInversionEnable;
    private String mPackageName;
    private long mPowerDelayTime = SystemProperties.getLong("hw_mc.fingerprint.powerfingerdestorydelay", 0);
    private Runnable mRemoveFingerViewRunnable;
    private final Runnable mResetForbidFlagRunnable = new Runnable() {
        /* class com.huawei.server.fingerprint.FingerprintController.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            synchronized (this) {
                FingerprintController.this.isForbidGotoSleepFlag = false;
                FingerprintController.this.mPackageName = null;
                Log.i(FingerprintController.TAG, "mResetForbidFlagRunnable");
            }
        }
    };
    private int mScreenRefreshRateState = -1;
    private ContentObserver mSettingsDisplayDaltonizerEnableObserver;
    private ContentObserver mSettingsDisplayDaltonizerObserver;
    private ContentObserver mSettingsDisplayInversionObserver;
    private ContentObserver mSettingsSuperWallpaperObserver = new ContentObserver(this.mHandler) {
        /* class com.huawei.server.fingerprint.FingerprintController.AnonymousClass2 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            if (FingerprintController.this.mContentResolver == null || FingerprintController.this.mHandler == null) {
                Log.i(FingerprintController.TAG, "mSettingsSuperWallpaperObserver content resolver or handle is null");
                return;
            }
            FingerprintController fingerprintController = FingerprintController.this;
            fingerprintController.mSuperWallpaperState = Settings.Secure.getIntForUser(fingerprintController.mContentResolver, FingerprintController.SUPER_WALLPAPER_EFFECT, 0, -2);
            Log.i(FingerprintController.TAG, "mSettingsSuperWallpaperObserver mSuperWallpaperState=" + FingerprintController.this.mSuperWallpaperState);
        }
    };
    private int mSuperWallpaperState = 0;
    private TelecomManager mTelecomManager = null;
    private String mTpSensorName = null;

    private FingerprintController() {
    }

    public static synchronized FingerprintController getInstance() {
        FingerprintController fingerprintController;
        synchronized (FingerprintController.class) {
            if (sInstance == null) {
                sInstance = new FingerprintController();
            }
            fingerprintController = sInstance;
        }
        return fingerprintController;
    }

    public long getPowerDelayTime() {
        return this.mPowerDelayTime;
    }

    public void setPowerForbidGotoSleepDelay(Handler handler) {
        String str;
        if (handler != null && (str = this.mPackageName) != null && !"com.android.systemui".equals(str)) {
            handler.removeCallbacks(this.mResetForbidFlagRunnable);
            handler.postDelayed(this.mResetForbidFlagRunnable, this.mPowerDelayTime);
        }
    }

    public void setForbidGotoSleepFlag(boolean isForbidFlag, Handler handler, String packageName) {
        synchronized (this) {
            if ("com.android.systemui".equals(packageName) || packageName == null) {
                Log.i(TAG, "setForbidGotoSleepFlag not set");
            } else {
                if (handler != null) {
                    handler.removeCallbacks(this.mResetForbidFlagRunnable);
                }
                this.mPackageName = packageName;
                this.isForbidGotoSleepFlag = isForbidFlag;
                Log.i(TAG, "setForbidGotoSleepFlag:" + this.isForbidGotoSleepFlag + "mPackageName=" + this.mPackageName);
            }
        }
    }

    public boolean isForbidGotoSleep() {
        String str = this.mPackageName;
        if (str == null || "com.android.systemui".equals(str)) {
            Log.i(TAG, "isForbidGotoSleep systemui packageName:" + this.mPackageName);
            return false;
        }
        Log.i(TAG, "isForbidGotoSleep packageName:" + this.mPackageName);
        return this.isForbidGotoSleepFlag;
    }

    public void setScreenRefreshRate(final int state, final String packageName, Handler handler) {
        if (handler == null) {
            Log.w(TAG, "setScreenRefreshRate handler is null");
            return;
        }
        synchronized (this) {
            if (this.mScreenRefreshRateState != state) {
                this.mScreenRefreshRateState = state;
                handler.post(new Runnable() {
                    /* class com.huawei.server.fingerprint.FingerprintController.AnonymousClass3 */

                    @Override // java.lang.Runnable
                    public void run() {
                        Parcel data = Parcel.obtain();
                        Parcel reply = Parcel.obtain();
                        try {
                            IBinder service = ServiceManager.getService("AGPService");
                            if (service != null) {
                                data.writeString(packageName);
                                data.writeInt(state);
                                service.transact(FingerprintController.REFRESH_RATE_CODE, data, reply, 1);
                                Log.i(FingerprintController.TAG, "refresh rate state:" + state + ",packageName=" + packageName);
                            }
                        } catch (RemoteException e) {
                            Log.e(FingerprintController.TAG, "updateRefreshRate error");
                        } catch (Throwable th) {
                            reply.recycle();
                            data.recycle();
                            throw th;
                        }
                        reply.recycle();
                        data.recycle();
                    }
                });
            }
        }
    }

    public class AuthenticatedParam {
        private long mDeviceId;
        private int mFingerId;
        private int mGroupId;
        private AtomicBoolean mIsWaitPowerEvent = new AtomicBoolean(false);
        private ArrayList<Byte> mTokens;

        public AuthenticatedParam() {
        }

        public void setAuthenticatedParam(long deviceId, int fingerId, int groupId, ArrayList<Byte> token) {
            this.mDeviceId = deviceId;
            this.mFingerId = fingerId;
            this.mGroupId = groupId;
            this.mTokens = token;
            this.mIsWaitPowerEvent.set(true);
        }

        public void setNoWaitPowerEvent() {
            this.mIsWaitPowerEvent.set(false);
        }

        public boolean isWaitPowerEvent() {
            return this.mIsWaitPowerEvent.getAndSet(false);
        }

        public long getDeviceId() {
            return this.mDeviceId;
        }

        public int getFingerId() {
            return this.mFingerId;
        }

        public int getGroupId() {
            return this.mGroupId;
        }

        public ArrayList<Byte> getToken() {
            return this.mTokens;
        }
    }

    public void sendTpStateToHal(boolean isFingerInScreenSupported, Context context, IExtBiometricsFingerprint daemon) {
        if (!isFingerInScreenSupported || context == null || daemon == null) {
            Log.i(TAG, "sendTpStateToHal do not send");
            return;
        }
        int currentUser = ActivityManager.getCurrentUser();
        boolean isHasUdFingerprint = false;
        if (Settings.Secure.getIntForUser(context.getContentResolver(), KEY_KEYGUARD_ENABLE, 0, currentUser) == 0) {
            Log.i(TAG, "sendTpStateToHal fingerprint keyguard disable");
            return;
        }
        FingerprintUtils fingerprintUtils = FingerprintUtils.getInstance();
        if (fingerprintUtils.isDualFp()) {
            if (fingerprintUtils.getFingerprintsForUser(context, currentUser, 1).size() > 0) {
                isHasUdFingerprint = true;
            }
        } else if (fingerprintUtils.getBiometricsForUser(context, currentUser).size() > 0) {
            isHasUdFingerprint = true;
        }
        if (isHasUdFingerprint) {
            try {
                Log.i(TAG, "sendTpStateToHal result:" + daemon.sendCmdToHal(1));
            } catch (RemoteException e) {
                Log.e(TAG, "sendTpStateToHal RemoteException");
            }
        }
    }

    private void registerContentObserver() {
        this.mContentResolver.registerContentObserver(Settings.Secure.getUriFor(DISPLAY_INVERSION_ENABLED), false, this.mSettingsDisplayInversionObserver, -1);
        this.mContentResolver.registerContentObserver(Settings.Secure.getUriFor(DISPLAY_DALTONIZER_ENABLED), false, this.mSettingsDisplayDaltonizerEnableObserver, -1);
        this.mContentResolver.registerContentObserver(Settings.Secure.getUriFor(DISPLAY_DALTONIZER), false, this.mSettingsDisplayDaltonizerObserver, -1);
        this.mContentResolver.registerContentObserver(Settings.Secure.getUriFor(SUPER_WALLPAPER_EFFECT), false, this.mSettingsSuperWallpaperObserver, -1);
        this.mSettingsDisplayInversionObserver.onChange(true);
        this.mSettingsDisplayDaltonizerEnableObserver.onChange(true);
        this.mSettingsDisplayDaltonizerObserver.onChange(true);
        this.mSettingsSuperWallpaperObserver.onChange(true);
    }

    public void getFingerLogoColorSettingsInfor(ContentResolver contentResolver, Handler handler) {
        if (contentResolver == null || handler == null) {
            Log.i(TAG, "get finger logo color content resolver is null");
            return;
        }
        this.mContentResolver = contentResolver;
        this.mHandler = handler;
        this.mSettingsDisplayInversionObserver = new ContentObserver(this.mHandler) {
            /* class com.huawei.server.fingerprint.FingerprintController.AnonymousClass4 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                if (FingerprintController.this.mContentResolver == null || FingerprintController.this.mHandler == null) {
                    Log.i(FingerprintController.TAG, "settingsDisplayInversionObserver content resolver or handle is null");
                    return;
                }
                FingerprintController fingerprintController = FingerprintController.this;
                fingerprintController.mInversionEnable = Settings.Secure.getIntForUser(fingerprintController.mContentResolver, FingerprintController.DISPLAY_INVERSION_ENABLED, 0, -2);
                Log.i(FingerprintController.TAG, "getFingerLogoColorSettingsInfor inversionEnable=" + FingerprintController.this.mInversionEnable);
            }
        };
        this.mSettingsDisplayDaltonizerEnableObserver = new ContentObserver(this.mHandler) {
            /* class com.huawei.server.fingerprint.FingerprintController.AnonymousClass5 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                if (FingerprintController.this.mContentResolver == null || FingerprintController.this.mHandler == null) {
                    Log.i(FingerprintController.TAG, "settingsDisplayDaltonizerEnableObserver content resolver or handle is null");
                    return;
                }
                FingerprintController fingerprintController = FingerprintController.this;
                fingerprintController.mDaltonizerEnable = Settings.Secure.getIntForUser(fingerprintController.mContentResolver, FingerprintController.DISPLAY_DALTONIZER_ENABLED, 0, -2);
                Log.i(FingerprintController.TAG, "getFingerLogoColorSettingsInfor daltonizerEnable=" + FingerprintController.this.mDaltonizerEnable);
            }
        };
        this.mSettingsDisplayDaltonizerObserver = new ContentObserver(this.mHandler) {
            /* class com.huawei.server.fingerprint.FingerprintController.AnonymousClass6 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                if (FingerprintController.this.mContentResolver == null || FingerprintController.this.mHandler == null) {
                    Log.i(FingerprintController.TAG, "settingsDisplayDaltonizerObserver content resolver or handle is null");
                    return;
                }
                FingerprintController fingerprintController = FingerprintController.this;
                fingerprintController.mDaltonizer = Settings.Secure.getIntForUser(fingerprintController.mContentResolver, FingerprintController.DISPLAY_DALTONIZER, 0, -2);
                Log.i(FingerprintController.TAG, "getFingerLogoColorSettingsInfor daltonizer=" + FingerprintController.this.mDaltonizer);
            }
        };
        registerContentObserver();
    }

    public void setSurfaceControlByDisplaySettings(SurfaceControl surfaceControl) {
        if (surfaceControl == null) {
            Log.w(TAG, "surfaceControl is null");
            return;
        }
        if (this.mInversionEnable == 1) {
            int i = this.mDaltonizerEnable;
            if (i == 0 || (i == 1 && this.mDaltonizer == -1)) {
                surfaceControl.setColorTransform(INVERSION_ENABLE_MATRIXS, INVERSION_ENABLE_TRANSLATIONS);
                Log.i(TAG, "set inversion enable matrix");
            }
            if (this.mDaltonizerEnable == 1 && this.mDaltonizer == 12) {
                surfaceControl.setColorTransform(INVERSION_DALTONIZER_ENABLE_GREEN_MATRIXS, INVERSION_DALTONIZER_ENABLE_GREEN_TRANSLATIONS);
                Log.i(TAG, "set inversion deltonizer enable green matrix");
            }
            if (this.mDaltonizerEnable == 1 && this.mDaltonizer == 11) {
                surfaceControl.setColorTransform(INVERSION_DALTONIZER_ENABLE_RED_MATRIXS, INVERSION_DALTONIZER_ENABLE_RED_TRANSLATIONS);
                Log.i(TAG, "set inversion daltonizer enable red matrix");
            }
            if (this.mDaltonizerEnable == 1 && this.mDaltonizer == 13) {
                surfaceControl.setColorTransform(INVERSION_DALTONIZER_ENABLE_BLUE_MATRIXS, INVERSION_DALTONIZER_ENABLE_BLUE_TRANSLATIONS);
                Log.i(TAG, "set inversion daltonizer enable blue matrix");
            }
        }
        if (this.mInversionEnable != 1) {
            if (this.mDaltonizerEnable == 1 && this.mDaltonizer == 12) {
                surfaceControl.setColorTransform(NO_INVERSION_DALTONIZER_ENABLE_GREEN_MATRIXS, NO_INVERSION_DALTONIZER_ENABLE_GREEN_TRANSLATIONS);
                Log.i(TAG, "set no inversion daltonizer enable green matrix");
            }
            if (this.mDaltonizerEnable == 1 && this.mDaltonizer == 11) {
                surfaceControl.setColorTransform(NO_INVERSION_DALTONIZER_ENABLE_RED_MATRIXS, NO_INVERSION_DALTONIZER_ENABLE_RED_TRANSLATIONS);
                Log.i(TAG, "set no inversion daltonizer enable red matrix");
            }
            if (this.mDaltonizerEnable == 1 && this.mDaltonizer == 13) {
                surfaceControl.setColorTransform(NO_INVERSION_DALTONIZER_ENABLE_BLUE_MATRIXS, NO_INVERSION_DALTONIZER_ENABLE_BLUE_TRANSLATIONS);
                Log.i(TAG, "set no inversion daltonizer enable blue matrix");
            }
        }
    }

    public void setViewByDisplaySettings(View view) {
        if (view == null) {
            Log.i(TAG, "setViewByDisplaySettings view is null");
            return;
        }
        ViewRootImpl viewRoot = view.getViewRootImpl();
        if (viewRoot != null) {
            Log.i(TAG, "setViewByDisplaySettings view");
            setSurfaceControlByDisplaySettings(viewRoot.getSurfaceControl());
        }
    }

    public void getDisplaySettingsInformationWhenSwitchUser() {
        ContentResolver contentResolver = this.mContentResolver;
        if (contentResolver == null) {
            Log.i(TAG, "getDisplaySettingsInformationWhenSwitchUser mContentResolver is null");
            return;
        }
        this.mInversionEnable = Settings.Secure.getIntForUser(contentResolver, DISPLAY_INVERSION_ENABLED, 0, -2);
        this.mDaltonizerEnable = Settings.Secure.getIntForUser(this.mContentResolver, DISPLAY_DALTONIZER_ENABLED, 0, -2);
        this.mDaltonizer = Settings.Secure.getIntForUser(this.mContentResolver, DISPLAY_DALTONIZER, 0, -2);
        this.mSuperWallpaperState = Settings.Secure.getIntForUser(this.mContentResolver, SUPER_WALLPAPER_EFFECT, 0, -2);
        Log.i(TAG, "getDisplaySettingsInformationWhenSwitchUser inversionEnable=" + this.mInversionEnable + ",daltonizerEnable=" + this.mDaltonizerEnable + ",daltonizer=" + this.mDaltonizer);
    }

    private boolean isTpSensor(Context context) {
        if (this.mTpSensorName == null) {
            Object obj = context.getSystemService(SensorManager.class);
            if (obj == null || !(obj instanceof SensorManager)) {
                Log.e(TAG, "SensorManager is null");
                return false;
            }
            Sensor proximityTpSensor = ((SensorManager) obj).getDefaultSensor(8);
            if (proximityTpSensor == null) {
                Log.i(TAG, "proximity tp sensor is null .");
                return false;
            }
            this.mTpSensorName = proximityTpSensor.getName();
        }
        return PROXIMITY_TP.equals(this.mTpSensorName);
    }

    private boolean isInCall(Context context) {
        if (this.mTelecomManager == null) {
            Object obj = context.getSystemService("telecom");
            if (obj == null || !(obj instanceof TelecomManager)) {
                Log.e(TAG, "TelecomManager is null");
                return false;
            }
            this.mTelecomManager = (TelecomManager) obj;
        }
        TelecomManager telecomManager = this.mTelecomManager;
        if (telecomManager != null) {
            return telecomManager.isInCall();
        }
        return false;
    }

    public boolean isInCallAndTpSenser(Context context) {
        boolean isInCallAndTpSenser = false;
        if (context == null) {
            Log.i(TAG, "isInCallAndTpSenser context is null");
            return false;
        }
        if (isTpSensor(context) && isInCall(context)) {
            isInCallAndTpSenser = true;
        }
        Log.i(TAG, "current is calling and tp sensor : " + isInCallAndTpSenser);
        return isInCallAndTpSenser;
    }

    public synchronized boolean getFingerViewRemoveFlag() {
        return this.isFingerViewRemove;
    }

    public synchronized void setFingerViewRemoveFlag(boolean isViewRemove) {
        this.isFingerViewRemove = isViewRemove;
        Log.i(TAG, "setFingerViewRemoveFlag isFingerViewRemove = " + this.isFingerViewRemove);
    }

    public synchronized void setFingerViewRemoveRunnable(Runnable removeFingerViewRunnable) {
        if (this.mRemoveFingerViewRunnable == null) {
            this.mRemoveFingerViewRunnable = removeFingerViewRunnable;
        }
    }

    public synchronized void deleteFingerView() {
        if (!(this.mHandler == null || this.mRemoveFingerViewRunnable == null)) {
            this.isFingerViewRemove = false;
            Log.i(TAG, "deleteFingerView isFingerViewRemove set false");
            this.mHandler.post(this.mRemoveFingerViewRunnable);
        }
    }

    public synchronized boolean isInDreamingAndSuperWallpaper(Context context) {
        boolean z = true;
        if (this.mSuperWallpaperState == 1) {
            if (context != null) {
                if (this.mDisplayManager == null) {
                    Object object = context.getSystemService("display");
                    if (object instanceof DisplayManager) {
                        this.mDisplayManager = (DisplayManager) object;
                    } else {
                        Log.e(TAG, "Failed, mDisplayManager is null");
                        return false;
                    }
                }
                int newState = this.mDisplayManager.getDisplay(0).getState();
                Log.i(TAG, "mDisplayManager state " + newState);
                if (newState != 3) {
                    z = false;
                }
                return z;
            }
        }
        Log.i(TAG, "super state " + this.mSuperWallpaperState);
        return false;
    }

    public void setCurrentPackageName(String currentPackageName) {
        this.mCurrentPackageName = currentPackageName;
    }

    public void setFingerViewHoverListener(final ImageView fingerprintView, final Context context) {
        if (context == null || fingerprintView == null) {
            Log.i(TAG, "setFingerViewHoverListener not systemui");
            return;
        }
        this.isSupportHwFingerVibrateView = HwVibrator.isSupportHwVibrator("haptic.common.fail_pattern2");
        fingerprintView.setOnHoverListener(new View.OnHoverListener() {
            /* class com.huawei.server.fingerprint.FingerprintController.AnonymousClass7 */

            @Override // android.view.View.OnHoverListener
            public boolean onHover(View v, MotionEvent event) {
                if (event != null && event.getAction() == 9) {
                    FingerprintController.this.authenticationResultVibrate(fingerprintView, context);
                }
                return false;
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void authenticationResultVibrate(ImageView fingerprintView, Context context) {
        if (fingerprintView == null || context == null) {
            Log.i(TAG, "authenticationResultVibrate fingerview null");
        } else if (!"com.android.systemui".equals(this.mCurrentPackageName)) {
            Log.i(TAG, "authenticationResultVibrate not systemui");
        } else {
            fingerprintView.announceForAccessibility(context.getString(33686273));
            int value = Settings.System.getIntForUser(context.getContentResolver(), "haptic_feedback_enabled", 0, ActivityManager.getCurrentUser());
            Log.i(TAG, "authenticationResultVibrate haptic_feedback_enabled:" + value);
            if (value == 0) {
                Log.i(TAG, "authenticationResultVibrate haptic_feedback_enabled closed");
            } else {
                vibrateFingerprintView(context);
            }
        }
    }

    private void vibrateFingerprintView(Context context) {
        if (context == null) {
            Log.i(TAG, "vibrateFingerprintView null");
        } else if (this.isSupportHwFingerVibrateView) {
            HwVibrator.setHwVibrator(Process.myUid(), context.getPackageName(), "haptic.common.fail_pattern2");
            Log.i(TAG, "authenticationResultVibrate create hwvibrator");
        } else {
            Vibrator vibrator = (Vibrator) context.getSystemService(Vibrator.class);
            if (vibrator == null) {
                Log.w(TAG, "authenticationResultVibrate Vibrator null, return");
                return;
            }
            Log.i(TAG, "authenticationResultVibrate create Vibrator");
            vibrator.vibrate(getSuccessVibrationEffect(), FINGERPRINT_SONFICATION_ATTRIBUTES);
        }
    }

    private VibrationEffect getSuccessVibrationEffect() {
        long[] vibePatterns = DEFAULT_SUCCESS_VIBRATION_PATTERNS;
        if (vibePatterns.length == 1) {
            return VibrationEffect.createOneShot(vibePatterns[0], -1);
        }
        return VibrationEffect.createWaveform(vibePatterns, -1);
    }
}

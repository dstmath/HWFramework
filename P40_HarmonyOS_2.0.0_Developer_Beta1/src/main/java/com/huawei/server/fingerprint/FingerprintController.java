package com.huawei.server.fingerprint;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.media.AudioAttributes;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.biometric.BiometricsFingerprintEx;
import com.huawei.android.biometric.FingerprintSupportEx;
import com.huawei.android.biometric.FingerprintSurfaceEx;
import com.huawei.android.biometric.FingerprintUtilsEx;
import com.huawei.android.os.HwVibrator;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.utils.HwPartResourceUtils;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private static final AudioAttributes FINGERPRINT_SONFICATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(DALTONIZER_BLUE).build();
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
    private static final int REFRESH_RATE_CODE = 20000;
    private static final int SCREEN_REFRESH_RATE_INDEX = 0;
    private static final int SCREEN_REFRESH_RATE_NUM = 4;
    private static final int SCREEN_REFRESH_STATE_INDEX = 1;
    private static final String SUPER_WALLPAPER_EFFECT = "in_wallpaper_effect";
    private static final int SUPER_WALLPAPER_STATE = 1;
    private static final String TAG = "FingerprintController";
    private static FingerprintController sInstance;
    private boolean isFingerViewRemove = false;
    private boolean isForbidGotoSleepFlag = false;
    private boolean isSupportHwFingerVibrateView = false;
    private ContentResolver mContentResolver;
    private Context mContext;
    private String mCurrentPackageName;
    private int mDaltonizer;
    private int mDaltonizerEnable;
    private DisplayManager mDisplayManager;
    private ImageView mFingerprintView;
    private Handler mHandler;
    private int mInversionEnable;
    private String mPackageName;
    private long mPowerDelayTime = SystemPropertiesEx.getLong("hw_mc.fingerprint.powerfingerdestorydelay", 0);
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
    private int mScreenRefreshRate = 0;
    private int mScreenRefreshRateState = -1;
    private int mScreenRefreshState = 0;
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
            fingerprintController.mSuperWallpaperState = SettingsEx.Secure.getIntForUser(fingerprintController.mContentResolver, FingerprintController.SUPER_WALLPAPER_EFFECT, 0, -2);
            Log.i(FingerprintController.TAG, "mSettingsSuperWallpaperObserver mSuperWallpaperState=" + FingerprintController.this.mSuperWallpaperState);
        }
    };
    private int mSuperWallpaperState = 0;

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

    public void initScreenRefreshRate() {
        String refreshRate = SystemPropertiesEx.get("hw_mc.display.fp_auth_rate");
        if (TextUtils.isEmpty(refreshRate)) {
            Log.i(TAG, "initScreenRefreshRate prop empty");
            return;
        }
        String[] refreshProps = refreshRate.split(",");
        if (refreshProps == null || refreshProps.length != 4) {
            Log.i(TAG, "initScreenRefreshRate prop wrong");
            return;
        }
        try {
            this.mScreenRefreshRate = Integer.valueOf(refreshProps[0]).intValue();
            this.mScreenRefreshState = Integer.valueOf(refreshProps[1]).intValue();
        } catch (NumberFormatException e) {
            Log.e(TAG, "initScreenRefreshRate NumberFormatException format exception");
        }
    }

    public synchronized void setScreenRefreshRate(int state, final String packageName, Handler handler) {
        if (handler == null) {
            Log.w(TAG, "setScreenRefreshRate handler is null");
        } else if (this.mScreenRefreshRateState != state) {
            this.mScreenRefreshRateState = state;
            handler.post(new Runnable() {
                /* class com.huawei.server.fingerprint.FingerprintController.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    try {
                        IBinder service = ServiceManagerEx.getService("AGPService");
                        if (service != null) {
                            data.writeString(packageName);
                            if (FingerprintController.this.mScreenRefreshRate == 0 || FingerprintController.this.mScreenRefreshState == 0 || FingerprintController.this.mScreenRefreshRateState != 2) {
                                data.writeInt(FingerprintController.this.mScreenRefreshRateState);
                                Log.i(FingerprintController.TAG, "refresh rate state:" + FingerprintController.this.mScreenRefreshRateState + ",packageName=" + packageName);
                            } else {
                                data.writeInt(FingerprintController.this.mScreenRefreshState);
                                Log.i(FingerprintController.TAG, "refresh rate state:" + FingerprintController.this.mScreenRefreshState + ",packageName=" + packageName + ", refreshRate:" + FingerprintController.this.mScreenRefreshRate);
                            }
                            service.transact(FingerprintController.REFRESH_RATE_CODE, data, reply, 1);
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

    public void sendTpStateToHal(boolean isFingerInScreenSupported, Context context, BiometricsFingerprintEx daemon) {
        if (!isFingerInScreenSupported || context == null || daemon == null) {
            Log.i(TAG, "sendTpStateToHal do not send");
            return;
        }
        int currentUser = ActivityManagerEx.getCurrentUser();
        boolean isHasUdFingerprint = false;
        if (SettingsEx.Secure.getIntForUser(context.getContentResolver(), KEY_KEYGUARD_ENABLE, 0, currentUser) == 0) {
            Log.i(TAG, "sendTpStateToHal fingerprint keyguard disable");
            return;
        }
        FingerprintUtilsEx fingerprintUtils = FingerprintUtilsEx.getInstance();
        if (fingerprintUtils.isDualFp()) {
            if (fingerprintUtils.getFingerprintsForUser(context, currentUser, FingerprintUtilsEx.DEVICE_UD).size() > 0) {
                isHasUdFingerprint = true;
            }
        } else if (fingerprintUtils.getBiometricsForUser(context, currentUser).size() > 0) {
            isHasUdFingerprint = true;
        }
        if (isHasUdFingerprint) {
            Log.i(TAG, "sendTpStateToHal result:" + daemon.sendCmdToHal(1));
        }
    }

    private void registerContentObserver() {
        FingerprintSupportEx.registerContentObserver(this.mContentResolver, Settings.Secure.getUriFor(DISPLAY_INVERSION_ENABLED), false, this.mSettingsDisplayInversionObserver, -1);
        FingerprintSupportEx.registerContentObserver(this.mContentResolver, Settings.Secure.getUriFor(DISPLAY_DALTONIZER_ENABLED), false, this.mSettingsDisplayDaltonizerEnableObserver, -1);
        FingerprintSupportEx.registerContentObserver(this.mContentResolver, Settings.Secure.getUriFor(DISPLAY_DALTONIZER), false, this.mSettingsDisplayDaltonizerObserver, -1);
        FingerprintSupportEx.registerContentObserver(this.mContentResolver, Settings.Secure.getUriFor(SUPER_WALLPAPER_EFFECT), false, this.mSettingsSuperWallpaperObserver, -1);
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
                fingerprintController.mInversionEnable = SettingsEx.Secure.getIntForUser(fingerprintController.mContentResolver, FingerprintController.DISPLAY_INVERSION_ENABLED, 0, -2);
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
                fingerprintController.mDaltonizerEnable = SettingsEx.Secure.getIntForUser(fingerprintController.mContentResolver, FingerprintController.DISPLAY_DALTONIZER_ENABLED, 0, -2);
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
                fingerprintController.mDaltonizer = SettingsEx.Secure.getIntForUser(fingerprintController.mContentResolver, FingerprintController.DISPLAY_DALTONIZER, 0, -2);
                Log.i(FingerprintController.TAG, "getFingerLogoColorSettingsInfor daltonizer=" + FingerprintController.this.mDaltonizer);
            }
        };
        registerContentObserver();
    }

    public void setSurfaceControlByDisplaySettings(FingerprintSurfaceEx surfaceControl) {
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
            if (this.mDaltonizerEnable == 1 && this.mDaltonizer == DALTONIZER_BLUE) {
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
            if (this.mDaltonizerEnable == 1 && this.mDaltonizer == DALTONIZER_BLUE) {
                surfaceControl.setColorTransform(NO_INVERSION_DALTONIZER_ENABLE_BLUE_MATRIXS, NO_INVERSION_DALTONIZER_ENABLE_BLUE_TRANSLATIONS);
                Log.i(TAG, "set no inversion daltonizer enable blue matrix");
            }
        }
    }

    public void setViewByDisplaySettings(View view) {
        Optional<FingerprintSurfaceEx> fingerprintSurfaceEx = FingerprintSupportEx.getFingerprintSurfaceExFromView(view);
        if (fingerprintSurfaceEx.isPresent()) {
            setSurfaceControlByDisplaySettings(fingerprintSurfaceEx.get());
        }
    }

    public void getDisplaySettingsInformationWhenSwitchUser() {
        ContentResolver contentResolver = this.mContentResolver;
        if (contentResolver == null) {
            Log.i(TAG, "getDisplaySettingsInformationWhenSwitchUser mContentResolver is null");
            return;
        }
        this.mInversionEnable = SettingsEx.Secure.getIntForUser(contentResolver, DISPLAY_INVERSION_ENABLED, 0, -2);
        this.mDaltonizerEnable = SettingsEx.Secure.getIntForUser(this.mContentResolver, DISPLAY_DALTONIZER_ENABLED, 0, -2);
        this.mDaltonizer = SettingsEx.Secure.getIntForUser(this.mContentResolver, DISPLAY_DALTONIZER, 0, -2);
        this.mSuperWallpaperState = SettingsEx.Secure.getIntForUser(this.mContentResolver, SUPER_WALLPAPER_EFFECT, 0, -2);
        Log.i(TAG, "getDisplaySettingsInformationWhenSwitchUser inversionEnable=" + this.mInversionEnable + ",daltonizerEnable=" + this.mDaltonizerEnable + ",daltonizer=" + this.mDaltonizer);
    }

    public synchronized boolean getFingerViewRemoveFlag() {
        return this.isFingerViewRemove;
    }

    public synchronized void setFingerViewRemoveInformation(boolean isViewRemove, Runnable removeFingerViewRunnable) {
        this.isFingerViewRemove = isViewRemove;
        Log.i(TAG, "setFingerViewRemoveFlag isViewRemove = " + isViewRemove);
        if (this.mRemoveFingerViewRunnable == null) {
            this.mRemoveFingerViewRunnable = removeFingerViewRunnable;
        }
    }

    public synchronized void deleteFingerView() {
        if (!(this.mHandler == null || this.mRemoveFingerViewRunnable == null)) {
            Log.i(TAG, "deleteFingerView");
            this.isFingerViewRemove = false;
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
                int newState = 0;
                Display display = this.mDisplayManager.getDisplay(0);
                if (display != null) {
                    newState = display.getState();
                }
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

    public synchronized void setCurrentPackageName(String currentPackageName) {
        this.mCurrentPackageName = currentPackageName;
    }

    public synchronized void setFingerViewHoverListener(ImageView fingerprintView, Context context) {
        if (context == null || fingerprintView == null) {
            Log.i(TAG, "setFingerViewHoverListener not systemui");
            return;
        }
        this.mFingerprintView = fingerprintView;
        this.mContext = context;
        this.isSupportHwFingerVibrateView = HwVibrator.isSupportHwVibrator("haptic.common.fail_pattern2");
        this.mFingerprintView.setOnHoverListener(new View.OnHoverListener() {
            /* class com.huawei.server.fingerprint.FingerprintController.AnonymousClass7 */

            @Override // android.view.View.OnHoverListener
            public boolean onHover(View v, MotionEvent event) {
                if (event != null && event.getAction() == 9) {
                    FingerprintController fingerprintController = FingerprintController.this;
                    fingerprintController.authenticationResultVibrate(fingerprintController.mFingerprintView, FingerprintController.this.mContext);
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
            fingerprintView.announceForAccessibility(context.getString(HwPartResourceUtils.getResourceId("voice_fingerprint_checking_area")));
            int value = SettingsEx.System.getIntForUser(context.getContentResolver(), "haptic_feedback_enabled", 0, ActivityManagerEx.getCurrentUser());
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

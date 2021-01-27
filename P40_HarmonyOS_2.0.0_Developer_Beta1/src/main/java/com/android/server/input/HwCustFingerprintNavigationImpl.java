package com.android.server.input;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.ContentObserver;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;
import com.huawei.android.app.HwActivityTaskManager;

public class HwCustFingerprintNavigationImpl extends HwCustFingerprintNavigation {
    public static final String FINGERPRINT_BACK_TO_HOME = "fp_return_desk";
    public static final String FINGERPRINT_GALLERY_SLIDE = "fingerprint_gallery_slide";
    public static final String FINGERPRINT_GO_BACK = "fp_go_back";
    public static final String FINGERPRINT_LAUNCHER_SLIDE = "fingerprint_launcher_slide";
    public static final String FINGERPRINT_LOCK_DEVICE = "fp_lock_device";
    public static final boolean FINGERPRINT_NAVIGATION_ENABLE = SystemProperties.getBoolean("ro.config.fp_navigation_enable", false);
    public static final boolean FINGERPRINT_NAVIGATION_GO_BACK_ENABLE = SystemProperties.getBoolean("ro.config.fp_go_back", false);
    public static final String FINGERPRINT_RECENT_APP = "fp_recent_application";
    public static final String FINGERPRINT_SHOW_NOTIFICATION = "fp_show_notification";
    private static final boolean HWDBG;
    private static final String TAG = HwCustFingerprintNavigationImpl.class.getSimpleName();
    private Context mContext;
    private FingerprintNavigationInspector mGalleryInspector;
    private final Handler mHandler = new Handler();
    private boolean mIsGallerySlide;
    private boolean mIsLauncherSlide;
    private FingerprintNavigationInspector mLauncherNavigationInspector;
    private PowerManager mPowerManager;
    private final ContentResolver mResolver;
    private final SettingsObserver mSettingsObserver;
    private FingerprintNavigationInspector mSingleTapInspector;

    static {
        boolean z = false;
        if (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3))) {
            z = true;
        }
        HWDBG = z;
    }

    public HwCustFingerprintNavigationImpl(Context context) {
        super(context);
        this.mContext = context;
        this.mResolver = context.getContentResolver();
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mGalleryInspector = new GalleryInspector();
        this.mLauncherNavigationInspector = new LauncherNavigationInspector();
        this.mSingleTapInspector = new SingleTapInspector();
        boolean z = false;
        this.mIsLauncherSlide = Settings.Secure.getInt(this.mResolver, FINGERPRINT_LAUNCHER_SLIDE, 0) != 0;
        this.mIsGallerySlide = Settings.Secure.getInt(this.mResolver, FINGERPRINT_GALLERY_SLIDE, 0) != 0 ? true : z;
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
    }

    public boolean handleFingerprintEvent(InputEvent event) {
        if (!FINGERPRINT_NAVIGATION_ENABLE) {
            return false;
        }
        if (this.mGalleryInspector.probe(event)) {
            this.mGalleryInspector.handle(event);
            return true;
        } else if (this.mLauncherNavigationInspector.probe(event)) {
            this.mLauncherNavigationInspector.handle(event);
            return true;
        } else if (!this.mSingleTapInspector.probe(event)) {
            return false;
        } else {
            this.mSingleTapInspector.handle(event);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isScreenOff() {
        PowerManager power = (PowerManager) this.mContext.getSystemService("power");
        if (power != null) {
            return !power.isScreenOn();
        }
        return false;
    }

    private String getTopApp() {
        ActivityInfo topActivity = HwActivityTaskManager.getLastResumedActivity();
        String pkgName = null;
        if (topActivity != null) {
            pkgName = topActivity.getComponentName().flattenToShortString();
            if (HWDBG) {
                String str = TAG;
                Log.d(str, "TopApp is " + pkgName);
            }
        }
        return pkgName;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isGallery() {
        String activityName = getTopApp();
        if (activityName == null) {
            if (HWDBG) {
                Log.d(TAG, "gallery name is null");
            }
            return false;
        } else if (activityName.startsWith("com.huawei.photos/com.huawei.gallery.app") || activityName.startsWith("com.android.gallery3d/com.huawei.gallery.app")) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isLauncher() {
        return "com.huawei.android.launcher/.Launcher".equalsIgnoreCase(getTopApp());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSpecialKey(InputEvent event, int code) {
        if (!(event instanceof KeyEvent)) {
            return false;
        }
        KeyEvent keyEvent = (KeyEvent) event;
        if (HWDBG) {
            String str = TAG;
            Log.d(str, "keycode is : " + keyEvent.getKeyCode());
        }
        if (keyEvent.getKeyCode() == code) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendKeyEvent(int keycode) {
        int[] actions;
        for (int i : new int[]{0, 1}) {
            long curTime = SystemClock.uptimeMillis();
            InputManager.getInstance().injectInputEvent(new KeyEvent(curTime, curTime, i, keycode, 0, 0, 6, 0, 8, 257), 0);
        }
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
            HwCustFingerprintNavigationImpl.this.mResolver.registerContentObserver(Settings.Secure.getUriFor(HwCustFingerprintNavigationImpl.FINGERPRINT_LAUNCHER_SLIDE), false, this);
            HwCustFingerprintNavigationImpl.this.mResolver.registerContentObserver(Settings.Secure.getUriFor(HwCustFingerprintNavigationImpl.FINGERPRINT_GALLERY_SLIDE), false, this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            if (HwCustFingerprintNavigationImpl.HWDBG) {
                Log.d(HwCustFingerprintNavigationImpl.TAG, "SettingDB has changed");
            }
            HwCustFingerprintNavigationImpl hwCustFingerprintNavigationImpl = HwCustFingerprintNavigationImpl.this;
            boolean z = false;
            hwCustFingerprintNavigationImpl.mIsLauncherSlide = Settings.Secure.getInt(hwCustFingerprintNavigationImpl.mResolver, HwCustFingerprintNavigationImpl.FINGERPRINT_LAUNCHER_SLIDE, 0) != 0;
            HwCustFingerprintNavigationImpl hwCustFingerprintNavigationImpl2 = HwCustFingerprintNavigationImpl.this;
            if (Settings.Secure.getInt(hwCustFingerprintNavigationImpl2.mResolver, HwCustFingerprintNavigationImpl.FINGERPRINT_GALLERY_SLIDE, 0) != 0) {
                z = true;
            }
            hwCustFingerprintNavigationImpl2.mIsGallerySlide = z;
        }
    }

    abstract class FingerprintNavigationInspector {
        FingerprintNavigationInspector() {
        }

        public boolean probe(InputEvent event) {
            return false;
        }

        public void handle(InputEvent event) {
        }
    }

    final class GalleryInspector extends FingerprintNavigationInspector {
        GalleryInspector() {
            super();
        }

        @Override // com.android.server.input.HwCustFingerprintNavigationImpl.FingerprintNavigationInspector
        public boolean probe(InputEvent event) {
            if (HwCustFingerprintNavigationImpl.HWDBG) {
                Log.d(HwCustFingerprintNavigationImpl.TAG, "GalleryInspector State probe");
            }
            if (!HwCustFingerprintNavigationImpl.this.mIsGallerySlide || !HwCustFingerprintNavigationImpl.this.isGallery() || HwCustFingerprintNavigationImpl.this.isScreenOff()) {
                return false;
            }
            if (!HwCustFingerprintNavigationImpl.this.isSpecialKey(event, 513) && !HwCustFingerprintNavigationImpl.this.isSpecialKey(event, 514)) {
                return false;
            }
            if (!HwCustFingerprintNavigationImpl.HWDBG) {
                return true;
            }
            Log.d(HwCustFingerprintNavigationImpl.TAG, "GalleryInspector State ok");
            return true;
        }

        @Override // com.android.server.input.HwCustFingerprintNavigationImpl.FingerprintNavigationInspector
        public void handle(InputEvent event) {
            KeyEvent keyEvent = (KeyEvent) event;
            if (keyEvent.getAction() != 0) {
                HwCustFingerprintNavigationImpl.this.sendKeyEvent(keyEvent.getKeyCode());
            }
        }
    }

    final class LauncherNavigationInspector extends FingerprintNavigationInspector {
        LauncherNavigationInspector() {
            super();
        }

        @Override // com.android.server.input.HwCustFingerprintNavigationImpl.FingerprintNavigationInspector
        public boolean probe(InputEvent event) {
            if (!HwCustFingerprintNavigationImpl.this.mIsLauncherSlide || !HwCustFingerprintNavigationImpl.this.isLauncher() || HwCustFingerprintNavigationImpl.this.isScreenOff()) {
                return false;
            }
            if (!HwCustFingerprintNavigationImpl.this.isSpecialKey(event, 513) && !HwCustFingerprintNavigationImpl.this.isSpecialKey(event, 514)) {
                return false;
            }
            if (!HwCustFingerprintNavigationImpl.HWDBG) {
                return true;
            }
            Log.d(HwCustFingerprintNavigationImpl.TAG, "LauncherNavigationInspector State ok");
            return true;
        }

        @Override // com.android.server.input.HwCustFingerprintNavigationImpl.FingerprintNavigationInspector
        public void handle(InputEvent event) {
            KeyEvent keyEvent = (KeyEvent) event;
            if (keyEvent.getAction() != 0) {
                HwCustFingerprintNavigationImpl.this.sendKeyEvent(keyEvent.getKeyCode());
            }
        }
    }

    final class SingleTapInspector extends FingerprintNavigationInspector {
        SingleTapInspector() {
            super();
        }

        @Override // com.android.server.input.HwCustFingerprintNavigationImpl.FingerprintNavigationInspector
        public boolean probe(InputEvent event) {
            if (HwCustFingerprintNavigationImpl.this.isScreenOff() || !HwCustFingerprintNavigationImpl.this.isSpecialKey(event, 601)) {
                return false;
            }
            return true;
        }

        @Override // com.android.server.input.HwCustFingerprintNavigationImpl.FingerprintNavigationInspector
        public void handle(InputEvent event) {
            if (((KeyEvent) event).getAction() != 0) {
                HwCustFingerprintNavigationImpl.this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
            }
        }
    }

    public boolean needCustNavigation() {
        return FINGERPRINT_NAVIGATION_ENABLE;
    }

    public boolean getCustNeedValue(ContentResolver cr, String name, int def, int userHandle, int compareValue) {
        if ((FINGERPRINT_NAVIGATION_GO_BACK_ENABLE || name == null || !FINGERPRINT_GO_BACK.equals(name)) && Settings.Secure.getIntForUser(cr, name, def, userHandle) != compareValue) {
            return true;
        }
        return false;
    }
}

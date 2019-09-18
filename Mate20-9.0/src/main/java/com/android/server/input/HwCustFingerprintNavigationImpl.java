package com.android.server.input;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;

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
    protected static final boolean HWDBG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    /* access modifiers changed from: private */
    public static final String TAG = HwCustFingerprintNavigationImpl.class.getSimpleName();
    private Context mContext;
    private FingerprintNavigationInspector mGalleryInspector;
    /* access modifiers changed from: private */
    public boolean mGallerySlide;
    private final Handler mHandler = new Handler();
    private FingerprintNavigationInspector mLauncherNavigationInspector;
    /* access modifiers changed from: private */
    public boolean mLauncherSlide;
    /* access modifiers changed from: private */
    public PowerManager mPowerManager;
    /* access modifiers changed from: private */
    public final ContentResolver mResolver;
    private final SettingsObserver mSettingsObserver;
    private FingerprintNavigationInspector mSingleTapInspector;

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

        public boolean probe(InputEvent event) {
            if (HwCustFingerprintNavigationImpl.HWDBG) {
                Log.d(HwCustFingerprintNavigationImpl.TAG, "GalleryInspector State probe");
            }
            if (!HwCustFingerprintNavigationImpl.this.mGallerySlide || !HwCustFingerprintNavigationImpl.this.isGallery() || HwCustFingerprintNavigationImpl.this.isScreenOff() || (!HwCustFingerprintNavigationImpl.this.isSpecialKey(event, 513) && !HwCustFingerprintNavigationImpl.this.isSpecialKey(event, 514))) {
                return false;
            }
            if (HwCustFingerprintNavigationImpl.HWDBG) {
                Log.d(HwCustFingerprintNavigationImpl.TAG, "GalleryInspector State ok");
            }
            return true;
        }

        public void handle(InputEvent event) {
            KeyEvent ev = (KeyEvent) event;
            if (ev.getAction() != 0) {
                HwCustFingerprintNavigationImpl.this.sendKeyEvent(ev.getKeyCode());
            }
        }
    }

    final class LauncherNavigationInspector extends FingerprintNavigationInspector {
        LauncherNavigationInspector() {
            super();
        }

        public boolean probe(InputEvent event) {
            if (!HwCustFingerprintNavigationImpl.this.mLauncherSlide || !HwCustFingerprintNavigationImpl.this.isLauncher() || HwCustFingerprintNavigationImpl.this.isScreenOff() || (!HwCustFingerprintNavigationImpl.this.isSpecialKey(event, 513) && !HwCustFingerprintNavigationImpl.this.isSpecialKey(event, 514))) {
                return false;
            }
            if (HwCustFingerprintNavigationImpl.HWDBG) {
                Log.d(HwCustFingerprintNavigationImpl.TAG, "LauncherNavigationInspector State ok");
            }
            return true;
        }

        public void handle(InputEvent event) {
            KeyEvent ev = (KeyEvent) event;
            if (ev.getAction() != 0) {
                HwCustFingerprintNavigationImpl.this.sendKeyEvent(ev.getKeyCode());
            }
        }
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
            HwCustFingerprintNavigationImpl.this.mResolver.registerContentObserver(Settings.Secure.getUriFor(HwCustFingerprintNavigationImpl.FINGERPRINT_LAUNCHER_SLIDE), false, this);
            HwCustFingerprintNavigationImpl.this.mResolver.registerContentObserver(Settings.Secure.getUriFor(HwCustFingerprintNavigationImpl.FINGERPRINT_GALLERY_SLIDE), false, this);
        }

        public void onChange(boolean selfChange) {
            if (HwCustFingerprintNavigationImpl.HWDBG) {
                Log.d(HwCustFingerprintNavigationImpl.TAG, "SettingDB has changed");
            }
            boolean z = true;
            boolean unused = HwCustFingerprintNavigationImpl.this.mLauncherSlide = Settings.Secure.getInt(HwCustFingerprintNavigationImpl.this.mResolver, HwCustFingerprintNavigationImpl.FINGERPRINT_LAUNCHER_SLIDE, 0) != 0;
            HwCustFingerprintNavigationImpl hwCustFingerprintNavigationImpl = HwCustFingerprintNavigationImpl.this;
            if (Settings.Secure.getInt(HwCustFingerprintNavigationImpl.this.mResolver, HwCustFingerprintNavigationImpl.FINGERPRINT_GALLERY_SLIDE, 0) == 0) {
                z = false;
            }
            boolean unused2 = hwCustFingerprintNavigationImpl.mGallerySlide = z;
        }
    }

    final class SingleTapInspector extends FingerprintNavigationInspector {
        SingleTapInspector() {
            super();
        }

        public boolean probe(InputEvent event) {
            if (HwCustFingerprintNavigationImpl.this.isScreenOff() || !HwCustFingerprintNavigationImpl.this.isSpecialKey(event, 601)) {
                return false;
            }
            return true;
        }

        public void handle(InputEvent event) {
            if (((KeyEvent) event).getAction() != 0) {
                HwCustFingerprintNavigationImpl.this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
            }
        }
    }

    public HwCustFingerprintNavigationImpl(Context context) {
        super(context);
        this.mContext = context;
        this.mResolver = context.getContentResolver();
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mGalleryInspector = new GalleryInspector();
        this.mLauncherNavigationInspector = new LauncherNavigationInspector();
        this.mSingleTapInspector = new SingleTapInspector();
        boolean z = true;
        this.mLauncherSlide = Settings.Secure.getInt(this.mResolver, FINGERPRINT_LAUNCHER_SLIDE, 0) != 0;
        this.mGallerySlide = Settings.Secure.getInt(this.mResolver, FINGERPRINT_GALLERY_SLIDE, 0) == 0 ? false : z;
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
    public boolean isScreenOff() {
        PowerManager power = (PowerManager) this.mContext.getSystemService("power");
        if (power != null) {
            return !power.isScreenOn();
        }
        return false;
    }

    private String getTopApp() {
        String pkgName = ServiceManager.getService("activity").topAppName();
        if (HWDBG) {
            String str = TAG;
            Log.d(str, "TopApp is " + pkgName);
        }
        return pkgName;
    }

    /* access modifiers changed from: private */
    public boolean isGallery() {
        String activityName = getTopApp();
        if (activityName != null) {
            return activityName.startsWith("com.android.gallery3d/com.huawei.gallery.app");
        }
        if (HWDBG) {
            Log.d(TAG, "gallery name is null");
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isLauncher() {
        return "com.huawei.android.launcher/.Launcher".equalsIgnoreCase(getTopApp());
    }

    /* access modifiers changed from: private */
    public boolean isSpecialKey(InputEvent event, int code) {
        if (!(event instanceof KeyEvent)) {
            return false;
        }
        KeyEvent ev = (KeyEvent) event;
        if (HWDBG) {
            String str = TAG;
            Log.d(str, "keycode is : " + ev.getKeyCode());
        }
        if (ev.getKeyCode() == code) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void sendKeyEvent(int keycode) {
        int[] actions = {0, 1};
        for (int keyEvent : actions) {
            long curTime = SystemClock.uptimeMillis();
            KeyEvent ev = new KeyEvent(curTime, curTime, keyEvent, keycode, 0, 0, 6, 0, 8, 257);
            InputManager.getInstance().injectInputEvent(ev, 0);
        }
    }

    public boolean needCustNavigation() {
        return FINGERPRINT_NAVIGATION_ENABLE;
    }

    public boolean getCustNeedValue(ContentResolver cr, String name, int def, int userHandle, int compaireValue) {
        boolean need = false;
        if (!FINGERPRINT_NAVIGATION_GO_BACK_ENABLE && name != null && name.equals(FINGERPRINT_GO_BACK)) {
            return false;
        }
        if (Settings.Secure.getIntForUser(cr, name, def, userHandle) != compaireValue) {
            need = true;
        }
        return need;
    }
}

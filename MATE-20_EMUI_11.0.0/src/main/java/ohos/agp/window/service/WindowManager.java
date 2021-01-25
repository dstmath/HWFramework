package ohos.agp.window.service;

import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.ComponentContainer;
import ohos.agp.window.wmc.AGPWindow;
import ohos.agp.window.wmc.AGPWindowManager;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class WindowManager {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "WindowManager");
    private static volatile WindowManager sInstance;
    private AGPWindowManager mAGPWindowManager = AGPWindowManager.getInstance();

    public static class LayoutConfig {
        public static final int FIRST_SUB_WINDOW = 1000;
        public static final int FIRST_SYSTEM_WINDOW = 2000;
        public static final int INPUT_ADJUST_NOTHING = 48;
        public static final int INPUT_ADJUST_PAN = 32;
        public static final int INPUT_ADJUST_RESIZE = 16;
        public static final int INPUT_ADJUST_UNSPECIFIED = 0;
        public static final int INPUT_IS_FORWARD_NAVIGATION = 256;
        public static final int INPUT_MASK_ADJUST = 240;
        public static final int INPUT_MASK_STATE = 15;
        public static final int INPUT_STATE_ALWAYS_HIDDEN = 3;
        public static final int INPUT_STATE_ALWAYS_VISIBLE = 5;
        public static final int INPUT_STATE_HIDDEN = 2;
        public static final int INPUT_STATE_UNCHANGED = 1;
        public static final int INPUT_STATE_UNSPECIFIED = 0;
        public static final int INPUT_STATE_VISIBLE = 4;
        public static final int INVALID_WINDOW = -1;
        public static final int MARK_ALLOW_EXTEND_LAYOUT = 512;
        public static final int MARK_ALLOW_LAYOUT_COVER_SCREEN = 256;
        public static final int MARK_ALLOW_LAYOUT_OVERSCAN = 33554432;
        public static final int MARK_ALT_FOCUSABLE_IM = 131072;
        public static final int MARK_DIM_EVE_WINDOW_BEHIND = 2;
        public static final int MARK_DRAWS_SYSTEM_BAR_BACKGROUNDS = Integer.MIN_VALUE;
        public static final int MARK_FOCUSABLE_IMPOSSIBLE = 8;
        public static final int MARK_FORCE_FULLSCREEN_IMPOSSIBLE = 2048;
        public static final int MARK_FULL_SCREEN = 1024;
        public static final int MARK_LAYOUT_ATTACHED_IN_DECOR = 1073741824;
        public static final int MARK_LAYOUT_INSET_DECOR = 65536;
        public static final int MARK_LOCAL_FOCUS_MODE = 268435456;
        public static final int MARK_LOCK_AS_SCREEN_ON = 1;
        public static final int MARK_NOT_RESPOD_CHEEK_PRESSES = 32768;
        public static final int MARK_OUTSIDE_TOUCH = 8388608;
        public static final int MARK_REMOTE_DEV_INPUT = 1;
        public static final int MARK_SCALED = 16384;
        public static final int MARK_SCREEN_ON_ALWAYS = 128;
        public static final int MARK_SECURE = 8192;
        public static final int MARK_SLIPPERY = 536870912;
        public static final int MARK_TOUCHABLE_IMPOSSIBLE = 16;
        public static final int MARK_TOUCH_MODAL_IMPOSSIBLE = 32;
        public static final int MARK_TRANSLUCENT_NAVIGATION = 134217728;
        public static final int MARK_TRANSLUCENT_STATUS = 67108864;
        public static final int MARK_WALLPAPER_BEHIND = 1048576;
        public static final int MARK_WATCH_OUTSIDE_TOUCH = 262144;
        public static final int MOD_APPLICATION = 2;
        public static final int MOD_APPLICATION_MEDIA = 1001;
        public static final int MOD_APPLICATION_OVERLAY = 2038;
        public static final int MOD_APPLICATION_PANEL = 1000;
        public static final int MOD_DREAM = 2023;
        public static final int MOD_INPUT_METHOD = 2011;
        public static final int MOD_KEYGUARD = 2004;
        public static final int MOD_NAVIGATION_BAR = 2019;
        public static final int MOD_PRESENTATION = 2037;
        public static final int MOD_STATUS_BAR = 2000;
        public static final int MOD_TOAST = 2005;
        public static final int MOD_VOICE_INTERACTION = 2031;
        public float alpha = 1.0f;
        public int animations = -1;
        public float dim = 1.0f;
        public int flags;
        public int gravity;
        public int height;
        public String title;
        public int type;
        public int width;
        public float windowBrightness = -1.0f;
        public int x;
        public int y;
    }

    private WindowManager() {
    }

    public static WindowManager getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (WindowManager.class) {
            if (sInstance == null) {
                sInstance = new WindowManager();
            }
        }
        return sInstance;
    }

    public Optional<Window> getTopWindow() {
        AGPWindowManager aGPWindowManager = this.mAGPWindowManager;
        if (aGPWindowManager == null) {
            return Optional.empty();
        }
        Optional<AGPWindow> topWindow = aGPWindowManager.getTopWindow();
        if (topWindow.isPresent()) {
            return Optional.of(new Window(topWindow.get()));
        }
        HiLog.error(LABEL, "WindowManager getTopWindow return null", new Object[0]);
        return Optional.empty();
    }

    public int getWindowCount(Context context) {
        AGPWindowManager aGPWindowManager = this.mAGPWindowManager;
        if (aGPWindowManager == null) {
            return 0;
        }
        return aGPWindowManager.windowsCount();
    }

    public Window addComponent(ComponentContainer componentContainer, Context context, int i) {
        AGPWindow createWindow = AGPWindowManager.getInstance().createWindow(context, 6, i);
        createWindow.setContentLayout(componentContainer);
        try {
            createWindow.show();
            return new Window(createWindow);
        } catch (AGPWindowManager.BadWindowException e) {
            throw new PermissionException(e.getLocalizedMessage());
        }
    }

    public void destroyWindow(Window window) {
        AGPWindowManager aGPWindowManager;
        if (window == null || (aGPWindowManager = this.mAGPWindowManager) == null) {
            HiLog.error(LABEL, "destroyWindow window is null", new Object[0]);
        } else {
            aGPWindowManager.destroyWindow(window.agpWindow);
        }
    }

    public static class PermissionException extends RuntimeException {
        private static final long serialVersionUID = 551216248691917625L;

        public PermissionException() {
            this(null);
        }

        public PermissionException(String str) {
            super(str);
        }
    }
}

package android.content.res;

import android.util.HwPCUtils;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class HwPCMultiWindowCompatibility {
    public static final int WINDOW_ACTION_FULLSCREENABLE = 1024;
    public static final int WINDOW_ACTION_MAXIMIZABLE = 512;
    public static final int WINDOW_ACTION_MODE_MASK = 65280;
    public static final int WINDOW_ACTION_RESIZABLE = 256;
    public static final int WINDOW_DEFAULT = 1;
    public static final int WINDOW_FULLSCREEN = 4;
    public static final int WINDOW_INVALID = -1;
    public static final int WINDOW_LANDSCAPE = 2;
    public static final int WINDOW_LAYOUT_MODE_MASK = 255;
    public static final int WINDOW_MAXIMIZED = 3;
    public static final int WINDOW_PORTRAIT = 1;
    public static final int WINDOW_SPECIAL_FULLSCREEN = 131072;
    public static final int WINDOW_SPECIAL_VIDEO = 65536;
    public static final int WINDOW_SPLIT_LEFT = 5;
    public static final int WINDOW_SPLIT_RIGHT = 6;
    public static final int WINDOW_VIDEO_MASK_COULD_ONLY_FULLSCREEN = 16711680;

    public static int getWindowStateLayout(int windowState) {
        return windowState & 255;
    }

    public static boolean isLayoutMaximized(int windowState) {
        return getWindowStateLayout(windowState) == 3;
    }

    public static boolean isLayoutSplitLeft(int windowState) {
        return getWindowStateLayout(windowState) == 5;
    }

    public static boolean isLayoutSplitRight(int windowState) {
        return getWindowStateLayout(windowState) == 6;
    }

    public static boolean isLayoutSplit(int windowState) {
        int windowStateLayout = getWindowStateLayout(windowState);
        return windowStateLayout == 5 || windowStateLayout == 6;
    }

    public static boolean isLayoutFullscreen(int windowState) {
        return getWindowStateLayout(windowState) == 4;
    }

    public static boolean isLayoutHadBounds(int windowState) {
        int windowStateLayout = getWindowStateLayout(windowState);
        return windowStateLayout == 2 || windowStateLayout == 1;
    }

    public static boolean isShowTopbar(int windowState) {
        return isLayoutFullscreen(windowState);
    }

    public static boolean isResizable(int windowState) {
        return (windowState & 256) != 0 && !HwPCUtils.isHiCarCastMode();
    }

    public static boolean isMaximizeable(int windowState) {
        return (windowState & 512) != 0;
    }

    public static boolean isFullscreenable(int windowState) {
        return (windowState & 1024) != 0;
    }

    public static boolean isRestorable(int windowState) {
        return (65280 & windowState) != 0;
    }

    public static int getLandscapeWithAllAction() {
        return 1794;
    }

    public static int getPortraitWithAllAction() {
        return 1793;
    }

    public static int getLandscapeWithPartAction() {
        return 1538;
    }

    public static int getFullscreenForSomeVideo() {
        return 67076;
    }

    public static boolean isVideoCouldOnlyFullscreen(int windowState) {
        return (16711680 & windowState) != 0;
    }
}

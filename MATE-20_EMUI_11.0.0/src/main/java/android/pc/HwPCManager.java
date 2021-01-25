package android.pc;

import android.os.Message;
import android.os.RemoteException;

public interface HwPCManager {
    public static final int DESKTOP_MODE = 1;
    public static final String ENTER_DESKTOP_MODE_REMEMBER = "enter-desktop-mode-remember";
    public static final int ENTER_DESKTOP_MODE_REMEMBERED = 1;
    public static final String EXIT_DESKTOP_MODE_REMEMBER = "exit-desktop-mode-remember";
    public static final int EXIT_DESKTOP_MODE_REMEMBERED = 1;
    public static final int GUIDE_EVER_STARTED = 1;
    public static final int GUIDE_NOT_STARTED = 0;
    public static final String GUIDE_STARTED = "guide-started";
    public static final int IS_CONNECTED = 0;
    public static final String IS_DISPLAY_DEVICE_CONNECTED = "is_display_device_connected";
    public static final int NONE_MODE = -1;
    public static final int NOT_CONNECTED = 1;
    public static final String PAD_DESKTOP_MODE_SCREEN_OFF_TIMEOUT = "pad_desktop_mode_screen_off_timeout";
    public static final int PAD_DESKTOP_MODE_SCREEN_OFF_TIMEOUT_DEFAULT = 600000;
    public static final String PAD_SCREEN_OFF_TIMEOUT = "pad_screen_off_timeout";
    public static final int PHONE_MODE = 0;
    public static final String SELECTED_PROJ_MODE = "selected-proj-mode";
    public static final int SHOW_ENTER_DIALOG_REMEMBERED = 1;
    public static final String SHOW_ENTER_DIALOG_USE_KEYBOARD = "show-enter-dialog-use-keyboard";
    public static final int SHOW_EXIT_DIALOG_REMEMBERED = 1;
    public static final String SHOW_EXIT_DIALOG_USE_KEYBOARD = "show-exit-dialog-use-keyboard";

    void execVoiceCmd(Message message) throws RemoteException;

    IHwPCManager getService();
}

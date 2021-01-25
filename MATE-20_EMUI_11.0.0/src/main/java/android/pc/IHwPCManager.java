package android.pc;

import android.app.HwRecentTaskInfo;
import android.app.HwRioClientInfo;
import android.app.IHwRioRuleCb;
import android.app.ITaskStackListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.PointerIcon;
import java.util.List;

public interface IHwPCManager extends IInterface {
    void LaunchMKForWifiMode() throws RemoteException;

    boolean checkPermissionForHwMultiDisplay(int i) throws RemoteException;

    void closeTopWindow() throws RemoteException;

    void disableRio(String str) throws RemoteException;

    void dispatchKeyEventForExclusiveKeyboard(KeyEvent keyEvent) throws RemoteException;

    void enableRio(String str, int i, IHwRioRuleCb iHwRioRuleCb, List<String> list) throws RemoteException;

    void enterFullScreen(int i) throws RemoteException;

    void execVoiceCmd(Message message) throws RemoteException;

    void exitFullScreen(int i) throws RemoteException;

    int forceDisplayMode(int i) throws RemoteException;

    List<String> getAllSupportPcAppList() throws RemoteException;

    List<String> getCarAppList() throws RemoteException;

    boolean getCastMode() throws RemoteException;

    Bitmap getDisplayBitmap(int i, int i2, int i3) throws RemoteException;

    int getFocusedDisplayId() throws RemoteException;

    HwRecentTaskInfo getHwRecentTaskInfo(int i) throws RemoteException;

    String getHwRioRule(HwRioClientInfo hwRioClientInfo) throws RemoteException;

    int getPCDisplayId() throws RemoteException;

    int getPackageSupportPcState(String str) throws RemoteException;

    float[] getPointerCoordinateAxis() throws RemoteException;

    Bitmap getTaskThumbnailEx(int i) throws RemoteException;

    int getWindowState(IBinder iBinder) throws RemoteException;

    void hideImeStatusIcon(String str) throws RemoteException;

    void hwResizeTask(int i, Rect rect) throws RemoteException;

    void hwRestoreTask(int i, float f, float f2) throws RemoteException;

    boolean injectInputEventExternal(InputEvent inputEvent, int i) throws RemoteException;

    boolean isAvoidShowDefaultKeyguard(int i) throws RemoteException;

    boolean isConnectExtDisplayFromPkg(String str) throws RemoteException;

    boolean isDisallowLockScreenForHwMultiDisplay() throws RemoteException;

    boolean isFocusedOnWindowsCastDisplay() throws RemoteException;

    boolean isHiCarCastModeForClient() throws RemoteException;

    boolean isInBasicMode() throws RemoteException;

    boolean isInSinkWindowsCastMode() throws RemoteException;

    boolean isInWindowsCastMode() throws RemoteException;

    boolean isModeSupportDrag() throws RemoteException;

    boolean isPackageRunningOnPCMode(String str, int i) throws RemoteException;

    boolean isPadAssistantMode() throws RemoteException;

    boolean isRioEnable(int i, String str) throws RemoteException;

    boolean isScreenPowerOn() throws RemoteException;

    boolean isShopDemo() throws RemoteException;

    boolean isSinkHasKeyboard() throws RemoteException;

    void lockScreen(boolean z) throws RemoteException;

    void notifyDpState(boolean z) throws RemoteException;

    void onTaskMovedToBack(int i) throws RemoteException;

    void onTaskMovedToFront(int i) throws RemoteException;

    void registHwSystemUIController(Messenger messenger) throws RemoteException;

    void registerHwTaskStackListener(ITaskStackListener iTaskStackListener) throws RemoteException;

    void relaunchIMEIfNecessary() throws RemoteException;

    void removeCarApp(String str) throws RemoteException;

    void saveAppIntent(List<Intent> list) throws RemoteException;

    void saveNeedRestartAppIntent(List<Intent> list) throws RemoteException;

    void scheduleDisplayAdded(int i) throws RemoteException;

    void scheduleDisplayChanged(int i) throws RemoteException;

    void scheduleDisplayRemoved(int i) throws RemoteException;

    void screenshotPc() throws RemoteException;

    void sendLockScreenShowViewMsg() throws RemoteException;

    void setCarApp(String str) throws RemoteException;

    void setCustomPointerIcon(PointerIcon pointerIcon, boolean z) throws RemoteException;

    void setIsInBasicMode(boolean z) throws RemoteException;

    void setIsInSinkWindowsCastMode(boolean z) throws RemoteException;

    void setIsSinkHasKeyboard(boolean z) throws RemoteException;

    void setNetworkReconnectionState(boolean z) throws RemoteException;

    void setPadAssistant(boolean z) throws RemoteException;

    void setPointerIconType(int i, boolean z) throws RemoteException;

    void setScreenPower(boolean z) throws RemoteException;

    boolean shouldInterceptInputEvent(KeyEvent keyEvent, boolean z) throws RemoteException;

    void showDialogForSwitchDisplay(int i, String str) throws RemoteException;

    void showImeStatusIcon(int i, String str) throws RemoteException;

    void showStartMenu() throws RemoteException;

    void showTopBar() throws RemoteException;

    void toggleHome() throws RemoteException;

    void triggerRecentTaskSplitView(int i, int i2) throws RemoteException;

    void triggerSplitWindowPreviewLayer(int i, int i2) throws RemoteException;

    void triggerSwitchTaskView(boolean z) throws RemoteException;

    void unRegisterHwTaskStackListener(ITaskStackListener iTaskStackListener) throws RemoteException;

    void updateFocusDisplayToWindowsCast() throws RemoteException;

    void userActivityOnDesktop() throws RemoteException;

    public static class Default implements IHwPCManager {
        @Override // android.pc.IHwPCManager
        public boolean getCastMode() throws RemoteException {
            return false;
        }

        @Override // android.pc.IHwPCManager
        public int getPackageSupportPcState(String packageName) throws RemoteException {
            return 0;
        }

        @Override // android.pc.IHwPCManager
        public List<String> getAllSupportPcAppList() throws RemoteException {
            return null;
        }

        @Override // android.pc.IHwPCManager
        public void scheduleDisplayAdded(int displayId) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void scheduleDisplayChanged(int displayId) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void scheduleDisplayRemoved(int displayId) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void relaunchIMEIfNecessary() throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void hwRestoreTask(int taskId, float x, float y) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void hwResizeTask(int taskId, Rect bounds) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public int getWindowState(IBinder token) throws RemoteException {
            return 0;
        }

        @Override // android.pc.IHwPCManager
        public HwRecentTaskInfo getHwRecentTaskInfo(int taskId) throws RemoteException {
            return null;
        }

        @Override // android.pc.IHwPCManager
        public void registerHwTaskStackListener(ITaskStackListener listener) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void unRegisterHwTaskStackListener(ITaskStackListener listener) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public Bitmap getDisplayBitmap(int displayId, int width, int height) throws RemoteException {
            return null;
        }

        @Override // android.pc.IHwPCManager
        public void registHwSystemUIController(Messenger messenger) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void showTopBar() throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void showStartMenu() throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void screenshotPc() throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void closeTopWindow() throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void triggerRecentTaskSplitView(int side, int triggerTaskId) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void triggerSplitWindowPreviewLayer(int side, int action) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void triggerSwitchTaskView(boolean show) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void toggleHome() throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public boolean injectInputEventExternal(InputEvent ev, int mode) throws RemoteException {
            return false;
        }

        @Override // android.pc.IHwPCManager
        public float[] getPointerCoordinateAxis() throws RemoteException {
            return null;
        }

        @Override // android.pc.IHwPCManager
        public int forceDisplayMode(int mode) throws RemoteException {
            return 0;
        }

        @Override // android.pc.IHwPCManager
        public void saveAppIntent(List<Intent> list) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public Bitmap getTaskThumbnailEx(int id) throws RemoteException {
            return null;
        }

        @Override // android.pc.IHwPCManager
        public void userActivityOnDesktop() throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void lockScreen(boolean lock) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public boolean isPackageRunningOnPCMode(String packageName, int uid) throws RemoteException {
            return false;
        }

        @Override // android.pc.IHwPCManager
        public boolean isScreenPowerOn() throws RemoteException {
            return false;
        }

        @Override // android.pc.IHwPCManager
        public void setScreenPower(boolean powerOn) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void dispatchKeyEventForExclusiveKeyboard(KeyEvent ke) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void showImeStatusIcon(int iconResId, String pkgName) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void hideImeStatusIcon(String pkgName) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void setCustomPointerIcon(PointerIcon icon, boolean keep) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void setPointerIconType(int iconId, boolean keep) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void notifyDpState(boolean dpState) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public int getPCDisplayId() throws RemoteException {
            return 0;
        }

        @Override // android.pc.IHwPCManager
        public void execVoiceCmd(Message message) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public boolean shouldInterceptInputEvent(KeyEvent ev, boolean forScroll) throws RemoteException {
            return false;
        }

        @Override // android.pc.IHwPCManager
        public void LaunchMKForWifiMode() throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public boolean isConnectExtDisplayFromPkg(String pkgName) throws RemoteException {
            return false;
        }

        @Override // android.pc.IHwPCManager
        public boolean checkPermissionForHwMultiDisplay(int uid) throws RemoteException {
            return false;
        }

        @Override // android.pc.IHwPCManager
        public boolean isHiCarCastModeForClient() throws RemoteException {
            return false;
        }

        @Override // android.pc.IHwPCManager
        public void onTaskMovedToBack(int taskId) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void onTaskMovedToFront(int taskId) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void saveNeedRestartAppIntent(List<Intent> list) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public boolean isDisallowLockScreenForHwMultiDisplay() throws RemoteException {
            return false;
        }

        @Override // android.pc.IHwPCManager
        public boolean isShopDemo() throws RemoteException {
            return false;
        }

        @Override // android.pc.IHwPCManager
        public void setNetworkReconnectionState(boolean IsNetworkReconnecting) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void sendLockScreenShowViewMsg() throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public boolean isInWindowsCastMode() throws RemoteException {
            return false;
        }

        @Override // android.pc.IHwPCManager
        public boolean isInSinkWindowsCastMode() throws RemoteException {
            return false;
        }

        @Override // android.pc.IHwPCManager
        public void setIsInSinkWindowsCastMode(boolean isInCastMode) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public boolean isSinkHasKeyboard() throws RemoteException {
            return false;
        }

        @Override // android.pc.IHwPCManager
        public void setIsSinkHasKeyboard(boolean isKeyboardExist) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void showDialogForSwitchDisplay(int displayId, String pkgName) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public int getFocusedDisplayId() throws RemoteException {
            return 0;
        }

        @Override // android.pc.IHwPCManager
        public boolean isAvoidShowDefaultKeyguard(int displayId) throws RemoteException {
            return false;
        }

        @Override // android.pc.IHwPCManager
        public boolean isFocusedOnWindowsCastDisplay() throws RemoteException {
            return false;
        }

        @Override // android.pc.IHwPCManager
        public void updateFocusDisplayToWindowsCast() throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void setPadAssistant(boolean isAssistWithPad) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public boolean isPadAssistantMode() throws RemoteException {
            return false;
        }

        @Override // android.pc.IHwPCManager
        public void setIsInBasicMode(boolean isInBasicMode) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public boolean isInBasicMode() throws RemoteException {
            return false;
        }

        @Override // android.pc.IHwPCManager
        public boolean isModeSupportDrag() throws RemoteException {
            return false;
        }

        @Override // android.pc.IHwPCManager
        public void enterFullScreen(int taskId) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void exitFullScreen(int taskId) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public String getHwRioRule(HwRioClientInfo info) throws RemoteException {
            return null;
        }

        @Override // android.pc.IHwPCManager
        public void enableRio(String mode, int displayId, IHwRioRuleCb callback, List<String> list) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public void disableRio(String mode) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public boolean isRioEnable(int displayId, String packageName) throws RemoteException {
            return false;
        }

        @Override // android.pc.IHwPCManager
        public void setCarApp(String app) throws RemoteException {
        }

        @Override // android.pc.IHwPCManager
        public List<String> getCarAppList() throws RemoteException {
            return null;
        }

        @Override // android.pc.IHwPCManager
        public void removeCarApp(String app) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwPCManager {
        private static final String DESCRIPTOR = "android.pc.IHwPCManager";
        static final int TRANSACTION_LaunchMKForWifiMode = 43;
        static final int TRANSACTION_checkPermissionForHwMultiDisplay = 45;
        static final int TRANSACTION_closeTopWindow = 19;
        static final int TRANSACTION_disableRio = 73;
        static final int TRANSACTION_dispatchKeyEventForExclusiveKeyboard = 34;
        static final int TRANSACTION_enableRio = 72;
        static final int TRANSACTION_enterFullScreen = 69;
        static final int TRANSACTION_execVoiceCmd = 41;
        static final int TRANSACTION_exitFullScreen = 70;
        static final int TRANSACTION_forceDisplayMode = 26;
        static final int TRANSACTION_getAllSupportPcAppList = 3;
        static final int TRANSACTION_getCarAppList = 76;
        static final int TRANSACTION_getCastMode = 1;
        static final int TRANSACTION_getDisplayBitmap = 14;
        static final int TRANSACTION_getFocusedDisplayId = 60;
        static final int TRANSACTION_getHwRecentTaskInfo = 11;
        static final int TRANSACTION_getHwRioRule = 71;
        static final int TRANSACTION_getPCDisplayId = 40;
        static final int TRANSACTION_getPackageSupportPcState = 2;
        static final int TRANSACTION_getPointerCoordinateAxis = 25;
        static final int TRANSACTION_getTaskThumbnailEx = 28;
        static final int TRANSACTION_getWindowState = 10;
        static final int TRANSACTION_hideImeStatusIcon = 36;
        static final int TRANSACTION_hwResizeTask = 9;
        static final int TRANSACTION_hwRestoreTask = 8;
        static final int TRANSACTION_injectInputEventExternal = 24;
        static final int TRANSACTION_isAvoidShowDefaultKeyguard = 61;
        static final int TRANSACTION_isConnectExtDisplayFromPkg = 44;
        static final int TRANSACTION_isDisallowLockScreenForHwMultiDisplay = 50;
        static final int TRANSACTION_isFocusedOnWindowsCastDisplay = 62;
        static final int TRANSACTION_isHiCarCastModeForClient = 46;
        static final int TRANSACTION_isInBasicMode = 67;
        static final int TRANSACTION_isInSinkWindowsCastMode = 55;
        static final int TRANSACTION_isInWindowsCastMode = 54;
        static final int TRANSACTION_isModeSupportDrag = 68;
        static final int TRANSACTION_isPackageRunningOnPCMode = 31;
        static final int TRANSACTION_isPadAssistantMode = 65;
        static final int TRANSACTION_isRioEnable = 74;
        static final int TRANSACTION_isScreenPowerOn = 32;
        static final int TRANSACTION_isShopDemo = 51;
        static final int TRANSACTION_isSinkHasKeyboard = 57;
        static final int TRANSACTION_lockScreen = 30;
        static final int TRANSACTION_notifyDpState = 39;
        static final int TRANSACTION_onTaskMovedToBack = 47;
        static final int TRANSACTION_onTaskMovedToFront = 48;
        static final int TRANSACTION_registHwSystemUIController = 15;
        static final int TRANSACTION_registerHwTaskStackListener = 12;
        static final int TRANSACTION_relaunchIMEIfNecessary = 7;
        static final int TRANSACTION_removeCarApp = 77;
        static final int TRANSACTION_saveAppIntent = 27;
        static final int TRANSACTION_saveNeedRestartAppIntent = 49;
        static final int TRANSACTION_scheduleDisplayAdded = 4;
        static final int TRANSACTION_scheduleDisplayChanged = 5;
        static final int TRANSACTION_scheduleDisplayRemoved = 6;
        static final int TRANSACTION_screenshotPc = 18;
        static final int TRANSACTION_sendLockScreenShowViewMsg = 53;
        static final int TRANSACTION_setCarApp = 75;
        static final int TRANSACTION_setCustomPointerIcon = 37;
        static final int TRANSACTION_setIsInBasicMode = 66;
        static final int TRANSACTION_setIsInSinkWindowsCastMode = 56;
        static final int TRANSACTION_setIsSinkHasKeyboard = 58;
        static final int TRANSACTION_setNetworkReconnectionState = 52;
        static final int TRANSACTION_setPadAssistant = 64;
        static final int TRANSACTION_setPointerIconType = 38;
        static final int TRANSACTION_setScreenPower = 33;
        static final int TRANSACTION_shouldInterceptInputEvent = 42;
        static final int TRANSACTION_showDialogForSwitchDisplay = 59;
        static final int TRANSACTION_showImeStatusIcon = 35;
        static final int TRANSACTION_showStartMenu = 17;
        static final int TRANSACTION_showTopBar = 16;
        static final int TRANSACTION_toggleHome = 23;
        static final int TRANSACTION_triggerRecentTaskSplitView = 20;
        static final int TRANSACTION_triggerSplitWindowPreviewLayer = 21;
        static final int TRANSACTION_triggerSwitchTaskView = 22;
        static final int TRANSACTION_unRegisterHwTaskStackListener = 13;
        static final int TRANSACTION_updateFocusDisplayToWindowsCast = 63;
        static final int TRANSACTION_userActivityOnDesktop = 29;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwPCManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwPCManager)) {
                return new Proxy(obj);
            }
            return (IHwPCManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getCastMode";
                case 2:
                    return "getPackageSupportPcState";
                case 3:
                    return "getAllSupportPcAppList";
                case 4:
                    return "scheduleDisplayAdded";
                case 5:
                    return "scheduleDisplayChanged";
                case 6:
                    return "scheduleDisplayRemoved";
                case 7:
                    return "relaunchIMEIfNecessary";
                case 8:
                    return "hwRestoreTask";
                case 9:
                    return "hwResizeTask";
                case 10:
                    return "getWindowState";
                case 11:
                    return "getHwRecentTaskInfo";
                case 12:
                    return "registerHwTaskStackListener";
                case 13:
                    return "unRegisterHwTaskStackListener";
                case 14:
                    return "getDisplayBitmap";
                case 15:
                    return "registHwSystemUIController";
                case 16:
                    return "showTopBar";
                case 17:
                    return "showStartMenu";
                case 18:
                    return "screenshotPc";
                case 19:
                    return "closeTopWindow";
                case 20:
                    return "triggerRecentTaskSplitView";
                case 21:
                    return "triggerSplitWindowPreviewLayer";
                case 22:
                    return "triggerSwitchTaskView";
                case 23:
                    return "toggleHome";
                case 24:
                    return "injectInputEventExternal";
                case 25:
                    return "getPointerCoordinateAxis";
                case 26:
                    return "forceDisplayMode";
                case 27:
                    return "saveAppIntent";
                case 28:
                    return "getTaskThumbnailEx";
                case 29:
                    return "userActivityOnDesktop";
                case 30:
                    return "lockScreen";
                case 31:
                    return "isPackageRunningOnPCMode";
                case 32:
                    return "isScreenPowerOn";
                case 33:
                    return "setScreenPower";
                case 34:
                    return "dispatchKeyEventForExclusiveKeyboard";
                case 35:
                    return "showImeStatusIcon";
                case 36:
                    return "hideImeStatusIcon";
                case 37:
                    return "setCustomPointerIcon";
                case 38:
                    return "setPointerIconType";
                case 39:
                    return "notifyDpState";
                case 40:
                    return "getPCDisplayId";
                case 41:
                    return "execVoiceCmd";
                case 42:
                    return "shouldInterceptInputEvent";
                case 43:
                    return "LaunchMKForWifiMode";
                case 44:
                    return "isConnectExtDisplayFromPkg";
                case 45:
                    return "checkPermissionForHwMultiDisplay";
                case 46:
                    return "isHiCarCastModeForClient";
                case 47:
                    return "onTaskMovedToBack";
                case 48:
                    return "onTaskMovedToFront";
                case 49:
                    return "saveNeedRestartAppIntent";
                case 50:
                    return "isDisallowLockScreenForHwMultiDisplay";
                case 51:
                    return "isShopDemo";
                case 52:
                    return "setNetworkReconnectionState";
                case 53:
                    return "sendLockScreenShowViewMsg";
                case 54:
                    return "isInWindowsCastMode";
                case 55:
                    return "isInSinkWindowsCastMode";
                case 56:
                    return "setIsInSinkWindowsCastMode";
                case 57:
                    return "isSinkHasKeyboard";
                case 58:
                    return "setIsSinkHasKeyboard";
                case 59:
                    return "showDialogForSwitchDisplay";
                case 60:
                    return "getFocusedDisplayId";
                case 61:
                    return "isAvoidShowDefaultKeyguard";
                case 62:
                    return "isFocusedOnWindowsCastDisplay";
                case 63:
                    return "updateFocusDisplayToWindowsCast";
                case 64:
                    return "setPadAssistant";
                case 65:
                    return "isPadAssistantMode";
                case 66:
                    return "setIsInBasicMode";
                case 67:
                    return "isInBasicMode";
                case 68:
                    return "isModeSupportDrag";
                case 69:
                    return "enterFullScreen";
                case 70:
                    return "exitFullScreen";
                case 71:
                    return "getHwRioRule";
                case 72:
                    return "enableRio";
                case 73:
                    return "disableRio";
                case 74:
                    return "isRioEnable";
                case 75:
                    return "setCarApp";
                case 76:
                    return "getCarAppList";
                case 77:
                    return "removeCarApp";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Rect _arg1;
            Messenger _arg0;
            InputEvent _arg02;
            KeyEvent _arg03;
            PointerIcon _arg04;
            Message _arg05;
            KeyEvent _arg06;
            HwRioClientInfo _arg07;
            if (code != 1598968902) {
                boolean _arg08 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean castMode = getCastMode();
                        reply.writeNoException();
                        reply.writeInt(castMode ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getPackageSupportPcState(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result2 = getAllSupportPcAppList();
                        reply.writeNoException();
                        reply.writeStringList(_result2);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        scheduleDisplayAdded(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        scheduleDisplayChanged(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        scheduleDisplayRemoved(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        relaunchIMEIfNecessary();
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        hwRestoreTask(data.readInt(), data.readFloat(), data.readFloat());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg09 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        hwResizeTask(_arg09, _arg1);
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getWindowState(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        HwRecentTaskInfo _result4 = getHwRecentTaskInfo(data.readInt());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        registerHwTaskStackListener(ITaskStackListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        unRegisterHwTaskStackListener(ITaskStackListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        Bitmap _result5 = getDisplayBitmap(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Messenger.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        registHwSystemUIController(_arg0);
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        showTopBar();
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        showStartMenu();
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        screenshotPc();
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        closeTopWindow();
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        triggerRecentTaskSplitView(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        triggerSplitWindowPreviewLayer(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = true;
                        }
                        triggerSwitchTaskView(_arg08);
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        toggleHome();
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = InputEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        boolean injectInputEventExternal = injectInputEventExternal(_arg02, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(injectInputEventExternal ? 1 : 0);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        float[] _result6 = getPointerCoordinateAxis();
                        reply.writeNoException();
                        reply.writeFloatArray(_result6);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = forceDisplayMode(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        saveAppIntent(data.createTypedArrayList(Intent.CREATOR));
                        reply.writeNoException();
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        Bitmap _result8 = getTaskThumbnailEx(data.readInt());
                        reply.writeNoException();
                        if (_result8 != null) {
                            reply.writeInt(1);
                            _result8.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        userActivityOnDesktop();
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = true;
                        }
                        lockScreen(_arg08);
                        reply.writeNoException();
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPackageRunningOnPCMode = isPackageRunningOnPCMode(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isPackageRunningOnPCMode ? 1 : 0);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isScreenPowerOn = isScreenPowerOn();
                        reply.writeNoException();
                        reply.writeInt(isScreenPowerOn ? 1 : 0);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = true;
                        }
                        setScreenPower(_arg08);
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = KeyEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        dispatchKeyEventForExclusiveKeyboard(_arg03);
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        showImeStatusIcon(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        hideImeStatusIcon(data.readString());
                        reply.writeNoException();
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = PointerIcon.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg08 = true;
                        }
                        setCustomPointerIcon(_arg04, _arg08);
                        reply.writeNoException();
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg010 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg08 = true;
                        }
                        setPointerIconType(_arg010, _arg08);
                        reply.writeNoException();
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = true;
                        }
                        notifyDpState(_arg08);
                        reply.writeNoException();
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = getPCDisplayId();
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        execVoiceCmd(_arg05);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = KeyEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg08 = true;
                        }
                        boolean shouldInterceptInputEvent = shouldInterceptInputEvent(_arg06, _arg08);
                        reply.writeNoException();
                        reply.writeInt(shouldInterceptInputEvent ? 1 : 0);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        LaunchMKForWifiMode();
                        reply.writeNoException();
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isConnectExtDisplayFromPkg = isConnectExtDisplayFromPkg(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isConnectExtDisplayFromPkg ? 1 : 0);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        boolean checkPermissionForHwMultiDisplay = checkPermissionForHwMultiDisplay(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(checkPermissionForHwMultiDisplay ? 1 : 0);
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isHiCarCastModeForClient = isHiCarCastModeForClient();
                        reply.writeNoException();
                        reply.writeInt(isHiCarCastModeForClient ? 1 : 0);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        onTaskMovedToBack(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        onTaskMovedToFront(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        saveNeedRestartAppIntent(data.createTypedArrayList(Intent.CREATOR));
                        reply.writeNoException();
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDisallowLockScreenForHwMultiDisplay = isDisallowLockScreenForHwMultiDisplay();
                        reply.writeNoException();
                        reply.writeInt(isDisallowLockScreenForHwMultiDisplay ? 1 : 0);
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isShopDemo = isShopDemo();
                        reply.writeNoException();
                        reply.writeInt(isShopDemo ? 1 : 0);
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = true;
                        }
                        setNetworkReconnectionState(_arg08);
                        reply.writeNoException();
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        sendLockScreenShowViewMsg();
                        reply.writeNoException();
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInWindowsCastMode = isInWindowsCastMode();
                        reply.writeNoException();
                        reply.writeInt(isInWindowsCastMode ? 1 : 0);
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInSinkWindowsCastMode = isInSinkWindowsCastMode();
                        reply.writeNoException();
                        reply.writeInt(isInSinkWindowsCastMode ? 1 : 0);
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = true;
                        }
                        setIsInSinkWindowsCastMode(_arg08);
                        reply.writeNoException();
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSinkHasKeyboard = isSinkHasKeyboard();
                        reply.writeNoException();
                        reply.writeInt(isSinkHasKeyboard ? 1 : 0);
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = true;
                        }
                        setIsSinkHasKeyboard(_arg08);
                        reply.writeNoException();
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        showDialogForSwitchDisplay(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 60:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = getFocusedDisplayId();
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 61:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAvoidShowDefaultKeyguard = isAvoidShowDefaultKeyguard(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isAvoidShowDefaultKeyguard ? 1 : 0);
                        return true;
                    case 62:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isFocusedOnWindowsCastDisplay = isFocusedOnWindowsCastDisplay();
                        reply.writeNoException();
                        reply.writeInt(isFocusedOnWindowsCastDisplay ? 1 : 0);
                        return true;
                    case 63:
                        data.enforceInterface(DESCRIPTOR);
                        updateFocusDisplayToWindowsCast();
                        reply.writeNoException();
                        return true;
                    case 64:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = true;
                        }
                        setPadAssistant(_arg08);
                        reply.writeNoException();
                        return true;
                    case 65:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPadAssistantMode = isPadAssistantMode();
                        reply.writeNoException();
                        reply.writeInt(isPadAssistantMode ? 1 : 0);
                        return true;
                    case 66:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = true;
                        }
                        setIsInBasicMode(_arg08);
                        reply.writeNoException();
                        return true;
                    case 67:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInBasicMode = isInBasicMode();
                        reply.writeNoException();
                        reply.writeInt(isInBasicMode ? 1 : 0);
                        return true;
                    case 68:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isModeSupportDrag = isModeSupportDrag();
                        reply.writeNoException();
                        reply.writeInt(isModeSupportDrag ? 1 : 0);
                        return true;
                    case 69:
                        data.enforceInterface(DESCRIPTOR);
                        enterFullScreen(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 70:
                        data.enforceInterface(DESCRIPTOR);
                        exitFullScreen(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 71:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = HwRioClientInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        String _result11 = getHwRioRule(_arg07);
                        reply.writeNoException();
                        reply.writeString(_result11);
                        return true;
                    case 72:
                        data.enforceInterface(DESCRIPTOR);
                        enableRio(data.readString(), data.readInt(), IHwRioRuleCb.Stub.asInterface(data.readStrongBinder()), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 73:
                        data.enforceInterface(DESCRIPTOR);
                        disableRio(data.readString());
                        reply.writeNoException();
                        return true;
                    case 74:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isRioEnable = isRioEnable(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isRioEnable ? 1 : 0);
                        return true;
                    case 75:
                        data.enforceInterface(DESCRIPTOR);
                        setCarApp(data.readString());
                        reply.writeNoException();
                        return true;
                    case 76:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result12 = getCarAppList();
                        reply.writeNoException();
                        reply.writeStringList(_result12);
                        return true;
                    case 77:
                        data.enforceInterface(DESCRIPTOR);
                        removeCarApp(data.readString());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwPCManager {
            public static IHwPCManager sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.pc.IHwPCManager
            public boolean getCastMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCastMode();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public int getPackageSupportPcState(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPackageSupportPcState(packageName);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public List<String> getAllSupportPcAppList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllSupportPcAppList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void scheduleDisplayAdded(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().scheduleDisplayAdded(displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void scheduleDisplayChanged(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().scheduleDisplayChanged(displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void scheduleDisplayRemoved(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().scheduleDisplayRemoved(displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void relaunchIMEIfNecessary() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().relaunchIMEIfNecessary();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void hwRestoreTask(int taskId, float x, float y) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeFloat(x);
                    _data.writeFloat(y);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().hwRestoreTask(taskId, x, y);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void hwResizeTask(int taskId, Rect bounds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (bounds != null) {
                        _data.writeInt(1);
                        bounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().hwResizeTask(taskId, bounds);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public int getWindowState(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWindowState(token);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public HwRecentTaskInfo getHwRecentTaskInfo(int taskId) throws RemoteException {
                HwRecentTaskInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwRecentTaskInfo(taskId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = HwRecentTaskInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void registerHwTaskStackListener(ITaskStackListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerHwTaskStackListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void unRegisterHwTaskStackListener(ITaskStackListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unRegisterHwTaskStackListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public Bitmap getDisplayBitmap(int displayId, int width, int height) throws RemoteException {
                Bitmap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDisplayBitmap(displayId, width, height);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bitmap.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void registHwSystemUIController(Messenger messenger) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registHwSystemUIController(messenger);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void showTopBar() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().showTopBar();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void showStartMenu() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().showStartMenu();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void screenshotPc() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().screenshotPc();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void closeTopWindow() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().closeTopWindow();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void triggerRecentTaskSplitView(int side, int triggerTaskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(side);
                    _data.writeInt(triggerTaskId);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().triggerRecentTaskSplitView(side, triggerTaskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void triggerSplitWindowPreviewLayer(int side, int action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(side);
                    _data.writeInt(action);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().triggerSplitWindowPreviewLayer(side, action);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void triggerSwitchTaskView(boolean show) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(show ? 1 : 0);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().triggerSwitchTaskView(show);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void toggleHome() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().toggleHome();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public boolean injectInputEventExternal(InputEvent ev, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (ev != null) {
                        _data.writeInt(1);
                        ev.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(mode);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().injectInputEventExternal(ev, mode);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public float[] getPointerCoordinateAxis() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPointerCoordinateAxis();
                    }
                    _reply.readException();
                    float[] _result = _reply.createFloatArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public int forceDisplayMode(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().forceDisplayMode(mode);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void saveAppIntent(List<Intent> intents) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(intents);
                    if (this.mRemote.transact(27, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().saveAppIntent(intents);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public Bitmap getTaskThumbnailEx(int id) throws RemoteException {
                Bitmap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTaskThumbnailEx(id);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bitmap.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void userActivityOnDesktop() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().userActivityOnDesktop();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void lockScreen(boolean lock) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(lock ? 1 : 0);
                    if (this.mRemote.transact(30, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().lockScreen(lock);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public boolean isPackageRunningOnPCMode(String packageName, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPackageRunningOnPCMode(packageName, uid);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public boolean isScreenPowerOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isScreenPowerOn();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void setScreenPower(boolean powerOn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(powerOn ? 1 : 0);
                    if (this.mRemote.transact(33, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setScreenPower(powerOn);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void dispatchKeyEventForExclusiveKeyboard(KeyEvent ke) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ke != null) {
                        _data.writeInt(1);
                        ke.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dispatchKeyEventForExclusiveKeyboard(ke);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void showImeStatusIcon(int iconResId, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(iconResId);
                    _data.writeString(pkgName);
                    if (this.mRemote.transact(35, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().showImeStatusIcon(iconResId, pkgName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void hideImeStatusIcon(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (this.mRemote.transact(36, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().hideImeStatusIcon(pkgName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void setCustomPointerIcon(PointerIcon icon, boolean keep) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (icon != null) {
                        _data.writeInt(1);
                        icon.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!keep) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(37, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCustomPointerIcon(icon, keep);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void setPointerIconType(int iconId, boolean keep) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(iconId);
                    _data.writeInt(keep ? 1 : 0);
                    if (this.mRemote.transact(38, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPointerIconType(iconId, keep);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void notifyDpState(boolean dpState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(dpState ? 1 : 0);
                    if (this.mRemote.transact(39, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyDpState(dpState);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public int getPCDisplayId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPCDisplayId();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void execVoiceCmd(Message message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (message != null) {
                        _data.writeInt(1);
                        message.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(41, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().execVoiceCmd(message);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public boolean shouldInterceptInputEvent(KeyEvent ev, boolean forScroll) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (ev != null) {
                        _data.writeInt(1);
                        ev.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(forScroll ? 1 : 0);
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().shouldInterceptInputEvent(ev, forScroll);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void LaunchMKForWifiMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(43, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().LaunchMKForWifiMode();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public boolean isConnectExtDisplayFromPkg(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    boolean _result = false;
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isConnectExtDisplayFromPkg(pkgName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public boolean checkPermissionForHwMultiDisplay(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkPermissionForHwMultiDisplay(uid);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public boolean isHiCarCastModeForClient() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isHiCarCastModeForClient();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void onTaskMovedToBack(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(47, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onTaskMovedToBack(taskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void onTaskMovedToFront(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(48, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onTaskMovedToFront(taskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void saveNeedRestartAppIntent(List<Intent> intents) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(intents);
                    if (this.mRemote.transact(49, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().saveNeedRestartAppIntent(intents);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public boolean isDisallowLockScreenForHwMultiDisplay() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(50, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDisallowLockScreenForHwMultiDisplay();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public boolean isShopDemo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(51, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isShopDemo();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void setNetworkReconnectionState(boolean IsNetworkReconnecting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(IsNetworkReconnecting ? 1 : 0);
                    if (this.mRemote.transact(52, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNetworkReconnectionState(IsNetworkReconnecting);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void sendLockScreenShowViewMsg() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(53, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendLockScreenShowViewMsg();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public boolean isInWindowsCastMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(54, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInWindowsCastMode();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public boolean isInSinkWindowsCastMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(55, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInSinkWindowsCastMode();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void setIsInSinkWindowsCastMode(boolean isInCastMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isInCastMode ? 1 : 0);
                    if (this.mRemote.transact(56, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setIsInSinkWindowsCastMode(isInCastMode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public boolean isSinkHasKeyboard() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(57, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSinkHasKeyboard();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void setIsSinkHasKeyboard(boolean isKeyboardExist) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isKeyboardExist ? 1 : 0);
                    if (this.mRemote.transact(58, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setIsSinkHasKeyboard(isKeyboardExist);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void showDialogForSwitchDisplay(int displayId, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeString(pkgName);
                    if (this.mRemote.transact(59, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().showDialogForSwitchDisplay(displayId, pkgName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public int getFocusedDisplayId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(60, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFocusedDisplayId();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public boolean isAvoidShowDefaultKeyguard(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    boolean _result = false;
                    if (!this.mRemote.transact(61, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAvoidShowDefaultKeyguard(displayId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public boolean isFocusedOnWindowsCastDisplay() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(62, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isFocusedOnWindowsCastDisplay();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void updateFocusDisplayToWindowsCast() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(63, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateFocusDisplayToWindowsCast();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void setPadAssistant(boolean isAssistWithPad) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isAssistWithPad ? 1 : 0);
                    if (this.mRemote.transact(64, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPadAssistant(isAssistWithPad);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public boolean isPadAssistantMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(65, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPadAssistantMode();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void setIsInBasicMode(boolean isInBasicMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isInBasicMode ? 1 : 0);
                    if (this.mRemote.transact(66, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setIsInBasicMode(isInBasicMode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public boolean isInBasicMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(67, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInBasicMode();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public boolean isModeSupportDrag() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(68, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isModeSupportDrag();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void enterFullScreen(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(69, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enterFullScreen(taskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void exitFullScreen(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(70, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().exitFullScreen(taskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public String getHwRioRule(HwRioClientInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(71, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwRioRule(info);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void enableRio(String mode, int displayId, IHwRioRuleCb callback, List<String> whiteList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mode);
                    _data.writeInt(displayId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeStringList(whiteList);
                    if (this.mRemote.transact(72, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enableRio(mode, displayId, callback, whiteList);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void disableRio(String mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mode);
                    if (this.mRemote.transact(73, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().disableRio(mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public boolean isRioEnable(int displayId, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(74, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isRioEnable(displayId, packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void setCarApp(String app) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(app);
                    if (this.mRemote.transact(75, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCarApp(app);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public List<String> getCarAppList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(76, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCarAppList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.pc.IHwPCManager
            public void removeCarApp(String app) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(app);
                    if (this.mRemote.transact(77, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeCarApp(app);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwPCManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwPCManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

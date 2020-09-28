package com.huawei.android.app;

import android.app.ActivityManager;
import android.app.HwRecentTaskInfo;
import android.app.IActivityController;
import android.app.IHwActivityNotifier;
import android.app.ITaskStackListener;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IMWThirdpartyCallback;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.android.app.IGameObserver;
import com.huawei.android.app.IGameObserverEx;
import com.huawei.android.app.IHwAtmDAMonitorCallback;
import java.util.List;

public interface IHwActivityTaskManager extends IInterface {
    boolean addGameSpacePackageList(List<String> list) throws RemoteException;

    boolean checkTaskId(int i) throws RemoteException;

    boolean delGameSpacePackageList(List<String> list) throws RemoteException;

    void dismissSplitScreenToFocusedStack() throws RemoteException;

    boolean enterCoordinationMode(Intent intent) throws RemoteException;

    boolean exitCoordinationMode(boolean z) throws RemoteException;

    int getActivityWindowMode(IBinder iBinder) throws RemoteException;

    int getCaptionState(IBinder iBinder) throws RemoteException;

    List<String> getGameList() throws RemoteException;

    Bundle getHwMultiWindowAppControlLists() throws RemoteException;

    Bundle getHwMultiWindowState() throws RemoteException;

    HwRecentTaskInfo getHwRecentTaskInfo(int i) throws RemoteException;

    ActivityInfo getLastResumedActivity() throws RemoteException;

    boolean getMultiWindowDisabled() throws RemoteException;

    Rect getPCTopTaskBounds(int i) throws RemoteException;

    Bundle getSplitStacksPos(int i, int i2) throws RemoteException;

    ActivityManager.TaskSnapshot getTaskSnapshot(int i, boolean z) throws RemoteException;

    Bitmap getTaskThumbnailOnPCMode(int i) throws RemoteException;

    Bundle getTopActivity() throws RemoteException;

    int getTopTaskIdInDisplay(int i, String str, boolean z) throws RemoteException;

    List<String> getVisiblePackages() throws RemoteException;

    List<ActivityManager.RunningTaskInfo> getVisibleTasks() throws RemoteException;

    int getWindowState(IBinder iBinder) throws RemoteException;

    void handleMultiWindowSwitch(IBinder iBinder, Bundle bundle) throws RemoteException;

    void hwResizeTask(int i, Rect rect) throws RemoteException;

    void hwRestoreTask(int i, float f, float f2) throws RemoteException;

    boolean isFreeFormVisible() throws RemoteException;

    boolean isGameDndOn() throws RemoteException;

    boolean isGameDndOnEx() throws RemoteException;

    boolean isGameGestureDisabled() throws RemoteException;

    boolean isGameKeyControlOn() throws RemoteException;

    boolean isInGameSpace(String str) throws RemoteException;

    boolean isInMultiWindowMode() throws RemoteException;

    boolean isResizableApp(ActivityInfo activityInfo) throws RemoteException;

    boolean isSupportDragForMultiWin(IBinder iBinder) throws RemoteException;

    boolean isTaskSupportResize(int i, boolean z, boolean z2) throws RemoteException;

    boolean isTaskVisible(int i) throws RemoteException;

    void moveTaskBackwards(int i) throws RemoteException;

    void onCaptionDropAnimationDone(IBinder iBinder) throws RemoteException;

    void registerAtmDAMonitorCallback(IHwAtmDAMonitorCallback iHwAtmDAMonitorCallback) throws RemoteException;

    void registerGameObserver(IGameObserver iGameObserver) throws RemoteException;

    void registerGameObserverEx(IGameObserverEx iGameObserverEx) throws RemoteException;

    void registerHwActivityNotifier(IHwActivityNotifier iHwActivityNotifier, String str) throws RemoteException;

    void registerHwTaskStackListener(ITaskStackListener iTaskStackListener) throws RemoteException;

    boolean registerThirdPartyCallBack(IMWThirdpartyCallback iMWThirdpartyCallback) throws RemoteException;

    boolean requestContentNode(ComponentName componentName, Bundle bundle, int i) throws RemoteException;

    boolean requestContentOther(ComponentName componentName, Bundle bundle, int i) throws RemoteException;

    void saveMultiWindowTipState(String str, int i) throws RemoteException;

    boolean setCustomActivityController(IActivityController iActivityController) throws RemoteException;

    int[] setFreeformStackVisibility(int i, int[] iArr, boolean z) throws RemoteException;

    boolean setMultiWindowDisabled(boolean z) throws RemoteException;

    void setSplitBarVisibility(boolean z) throws RemoteException;

    void setWarmColdSwitch(boolean z) throws RemoteException;

    void toggleHome() throws RemoteException;

    void togglePCMode(boolean z, int i) throws RemoteException;

    void unRegisterHwTaskStackListener(ITaskStackListener iTaskStackListener) throws RemoteException;

    void unregisterGameObserver(IGameObserver iGameObserver) throws RemoteException;

    void unregisterGameObserverEx(IGameObserverEx iGameObserverEx) throws RemoteException;

    void unregisterHwActivityNotifier(IHwActivityNotifier iHwActivityNotifier) throws RemoteException;

    boolean unregisterThirdPartyCallBack(IMWThirdpartyCallback iMWThirdpartyCallback) throws RemoteException;

    void updateFreeFormOutLine(int i) throws RemoteException;

    public static class Default implements IHwActivityTaskManager {
        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void registerHwActivityNotifier(IHwActivityNotifier notifier, String reason) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void unregisterHwActivityNotifier(IHwActivityNotifier notifier) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public ActivityInfo getLastResumedActivity() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Bundle getTopActivity() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void registerAtmDAMonitorCallback(IHwAtmDAMonitorCallback callback) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void setWarmColdSwitch(boolean enable) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isInMultiWindowMode() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean registerThirdPartyCallBack(IMWThirdpartyCallback aCallBackHandler) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean unregisterThirdPartyCallBack(IMWThirdpartyCallback aCallBackHandler) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int getTopTaskIdInDisplay(int displayId, String pkgName, boolean invisibleAlso) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isTaskSupportResize(int taskId, boolean isFullscreen, boolean isMaximized) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Rect getPCTopTaskBounds(int displayId) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void hwRestoreTask(int taskId, float xPos, float yPos) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void hwResizeTask(int taskId, Rect bounds) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int getWindowState(IBinder token) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public HwRecentTaskInfo getHwRecentTaskInfo(int taskId) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void togglePCMode(boolean pcMode, int displayId) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void toggleHome() throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void unRegisterHwTaskStackListener(ITaskStackListener listener) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void registerHwTaskStackListener(ITaskStackListener listener) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean checkTaskId(int taskId) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void moveTaskBackwards(int taskId) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Bitmap getTaskThumbnailOnPCMode(int taskId) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean requestContentNode(ComponentName componentName, Bundle bundle, int token) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean requestContentOther(ComponentName componentName, Bundle bundle, int token) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean addGameSpacePackageList(List<String> list) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean delGameSpacePackageList(List<String> list) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void registerGameObserver(IGameObserver observer) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void unregisterGameObserver(IGameObserver observer) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void registerGameObserverEx(IGameObserverEx observer) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void unregisterGameObserverEx(IGameObserverEx observer) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isInGameSpace(String packageName) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public List<String> getGameList() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isGameDndOn() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isGameDndOnEx() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isGameKeyControlOn() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isGameGestureDisabled() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isFreeFormVisible() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isTaskVisible(int id) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void updateFreeFormOutLine(int state) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int getCaptionState(IBinder token) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int getActivityWindowMode(IBinder token) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void onCaptionDropAnimationDone(IBinder token) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public List<ActivityManager.RunningTaskInfo> getVisibleTasks() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public ActivityManager.TaskSnapshot getTaskSnapshot(int taskId, boolean reducedResolution) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int[] setFreeformStackVisibility(int displayId, int[] stackIdArray, boolean isVisible) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void dismissSplitScreenToFocusedStack() throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void handleMultiWindowSwitch(IBinder token, Bundle info) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Bundle getSplitStacksPos(int displayId, int splitRatio) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean enterCoordinationMode(Intent intent) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean exitCoordinationMode(boolean toTop) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void setSplitBarVisibility(boolean isVisibility) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean setCustomActivityController(IActivityController controller) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isResizableApp(ActivityInfo activityInfo) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Bundle getHwMultiWindowAppControlLists() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void saveMultiWindowTipState(String tipKey, int state) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isSupportDragForMultiWin(IBinder token) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public List<String> getVisiblePackages() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean setMultiWindowDisabled(boolean disabled) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean getMultiWindowDisabled() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Bundle getHwMultiWindowState() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwActivityTaskManager {
        private static final String DESCRIPTOR = "com.huawei.android.app.IHwActivityTaskManager";
        static final int TRANSACTION_addGameSpacePackageList = 26;
        static final int TRANSACTION_checkTaskId = 21;
        static final int TRANSACTION_delGameSpacePackageList = 27;
        static final int TRANSACTION_dismissSplitScreenToFocusedStack = 47;
        static final int TRANSACTION_enterCoordinationMode = 50;
        static final int TRANSACTION_exitCoordinationMode = 51;
        static final int TRANSACTION_getActivityWindowMode = 42;
        static final int TRANSACTION_getCaptionState = 41;
        static final int TRANSACTION_getGameList = 33;
        static final int TRANSACTION_getHwMultiWindowAppControlLists = 55;
        static final int TRANSACTION_getHwMultiWindowState = 61;
        static final int TRANSACTION_getHwRecentTaskInfo = 16;
        static final int TRANSACTION_getLastResumedActivity = 3;
        static final int TRANSACTION_getMultiWindowDisabled = 60;
        static final int TRANSACTION_getPCTopTaskBounds = 12;
        static final int TRANSACTION_getSplitStacksPos = 49;
        static final int TRANSACTION_getTaskSnapshot = 45;
        static final int TRANSACTION_getTaskThumbnailOnPCMode = 23;
        static final int TRANSACTION_getTopActivity = 4;
        static final int TRANSACTION_getTopTaskIdInDisplay = 10;
        static final int TRANSACTION_getVisiblePackages = 58;
        static final int TRANSACTION_getVisibleTasks = 44;
        static final int TRANSACTION_getWindowState = 15;
        static final int TRANSACTION_handleMultiWindowSwitch = 48;
        static final int TRANSACTION_hwResizeTask = 14;
        static final int TRANSACTION_hwRestoreTask = 13;
        static final int TRANSACTION_isFreeFormVisible = 38;
        static final int TRANSACTION_isGameDndOn = 34;
        static final int TRANSACTION_isGameDndOnEx = 35;
        static final int TRANSACTION_isGameGestureDisabled = 37;
        static final int TRANSACTION_isGameKeyControlOn = 36;
        static final int TRANSACTION_isInGameSpace = 32;
        static final int TRANSACTION_isInMultiWindowMode = 7;
        static final int TRANSACTION_isResizableApp = 54;
        static final int TRANSACTION_isSupportDragForMultiWin = 57;
        static final int TRANSACTION_isTaskSupportResize = 11;
        static final int TRANSACTION_isTaskVisible = 39;
        static final int TRANSACTION_moveTaskBackwards = 22;
        static final int TRANSACTION_onCaptionDropAnimationDone = 43;
        static final int TRANSACTION_registerAtmDAMonitorCallback = 5;
        static final int TRANSACTION_registerGameObserver = 28;
        static final int TRANSACTION_registerGameObserverEx = 30;
        static final int TRANSACTION_registerHwActivityNotifier = 1;
        static final int TRANSACTION_registerHwTaskStackListener = 20;
        static final int TRANSACTION_registerThirdPartyCallBack = 8;
        static final int TRANSACTION_requestContentNode = 24;
        static final int TRANSACTION_requestContentOther = 25;
        static final int TRANSACTION_saveMultiWindowTipState = 56;
        static final int TRANSACTION_setCustomActivityController = 53;
        static final int TRANSACTION_setFreeformStackVisibility = 46;
        static final int TRANSACTION_setMultiWindowDisabled = 59;
        static final int TRANSACTION_setSplitBarVisibility = 52;
        static final int TRANSACTION_setWarmColdSwitch = 6;
        static final int TRANSACTION_toggleHome = 18;
        static final int TRANSACTION_togglePCMode = 17;
        static final int TRANSACTION_unRegisterHwTaskStackListener = 19;
        static final int TRANSACTION_unregisterGameObserver = 29;
        static final int TRANSACTION_unregisterGameObserverEx = 31;
        static final int TRANSACTION_unregisterHwActivityNotifier = 2;
        static final int TRANSACTION_unregisterThirdPartyCallBack = 9;
        static final int TRANSACTION_updateFreeFormOutLine = 40;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwActivityTaskManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwActivityTaskManager)) {
                return new Proxy(obj);
            }
            return (IHwActivityTaskManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "registerHwActivityNotifier";
                case 2:
                    return "unregisterHwActivityNotifier";
                case 3:
                    return "getLastResumedActivity";
                case 4:
                    return "getTopActivity";
                case 5:
                    return "registerAtmDAMonitorCallback";
                case 6:
                    return "setWarmColdSwitch";
                case 7:
                    return "isInMultiWindowMode";
                case 8:
                    return "registerThirdPartyCallBack";
                case 9:
                    return "unregisterThirdPartyCallBack";
                case 10:
                    return "getTopTaskIdInDisplay";
                case 11:
                    return "isTaskSupportResize";
                case 12:
                    return "getPCTopTaskBounds";
                case 13:
                    return "hwRestoreTask";
                case 14:
                    return "hwResizeTask";
                case 15:
                    return "getWindowState";
                case 16:
                    return "getHwRecentTaskInfo";
                case 17:
                    return "togglePCMode";
                case 18:
                    return "toggleHome";
                case 19:
                    return "unRegisterHwTaskStackListener";
                case 20:
                    return "registerHwTaskStackListener";
                case 21:
                    return "checkTaskId";
                case 22:
                    return "moveTaskBackwards";
                case 23:
                    return "getTaskThumbnailOnPCMode";
                case 24:
                    return "requestContentNode";
                case 25:
                    return "requestContentOther";
                case 26:
                    return "addGameSpacePackageList";
                case 27:
                    return "delGameSpacePackageList";
                case 28:
                    return "registerGameObserver";
                case 29:
                    return "unregisterGameObserver";
                case 30:
                    return "registerGameObserverEx";
                case 31:
                    return "unregisterGameObserverEx";
                case 32:
                    return "isInGameSpace";
                case 33:
                    return "getGameList";
                case 34:
                    return "isGameDndOn";
                case 35:
                    return "isGameDndOnEx";
                case 36:
                    return "isGameKeyControlOn";
                case 37:
                    return "isGameGestureDisabled";
                case 38:
                    return "isFreeFormVisible";
                case 39:
                    return "isTaskVisible";
                case 40:
                    return "updateFreeFormOutLine";
                case 41:
                    return "getCaptionState";
                case 42:
                    return "getActivityWindowMode";
                case 43:
                    return "onCaptionDropAnimationDone";
                case 44:
                    return "getVisibleTasks";
                case 45:
                    return "getTaskSnapshot";
                case 46:
                    return "setFreeformStackVisibility";
                case 47:
                    return "dismissSplitScreenToFocusedStack";
                case 48:
                    return "handleMultiWindowSwitch";
                case 49:
                    return "getSplitStacksPos";
                case 50:
                    return "enterCoordinationMode";
                case 51:
                    return "exitCoordinationMode";
                case 52:
                    return "setSplitBarVisibility";
                case 53:
                    return "setCustomActivityController";
                case 54:
                    return "isResizableApp";
                case 55:
                    return "getHwMultiWindowAppControlLists";
                case 56:
                    return "saveMultiWindowTipState";
                case 57:
                    return "isSupportDragForMultiWin";
                case 58:
                    return "getVisiblePackages";
                case 59:
                    return "setMultiWindowDisabled";
                case 60:
                    return "getMultiWindowDisabled";
                case 61:
                    return "getHwMultiWindowState";
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
            ComponentName _arg0;
            Bundle _arg12;
            ComponentName _arg02;
            Bundle _arg13;
            Bundle _arg14;
            Intent _arg03;
            ActivityInfo _arg04;
            if (code != 1598968902) {
                boolean _arg05 = false;
                boolean _arg06 = false;
                boolean _arg07 = false;
                boolean _arg08 = false;
                boolean _arg2 = false;
                boolean _arg09 = false;
                boolean _arg22 = false;
                boolean _arg23 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        registerHwActivityNotifier(IHwActivityNotifier.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterHwActivityNotifier(IHwActivityNotifier.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        ActivityInfo _result = getLastResumedActivity();
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result2 = getTopActivity();
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        registerAtmDAMonitorCallback(IHwAtmDAMonitorCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = true;
                        }
                        setWarmColdSwitch(_arg05);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInMultiWindowMode = isInMultiWindowMode();
                        reply.writeNoException();
                        reply.writeInt(isInMultiWindowMode ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerThirdPartyCallBack = registerThirdPartyCallBack(IMWThirdpartyCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerThirdPartyCallBack ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unregisterThirdPartyCallBack = unregisterThirdPartyCallBack(IMWThirdpartyCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unregisterThirdPartyCallBack ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg010 = data.readInt();
                        String _arg15 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        int _result3 = getTopTaskIdInDisplay(_arg010, _arg15, _arg23);
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg011 = data.readInt();
                        boolean _arg16 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        boolean isTaskSupportResize = isTaskSupportResize(_arg011, _arg16, _arg22);
                        reply.writeNoException();
                        reply.writeInt(isTaskSupportResize ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        Rect _result4 = getPCTopTaskBounds(data.readInt());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        hwRestoreTask(data.readInt(), data.readFloat(), data.readFloat());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg012 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        hwResizeTask(_arg012, _arg1);
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getWindowState(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        HwRecentTaskInfo _result6 = getHwRecentTaskInfo(data.readInt());
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = true;
                        }
                        togglePCMode(_arg09, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        toggleHome();
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        unRegisterHwTaskStackListener(ITaskStackListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        registerHwTaskStackListener(ITaskStackListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        boolean checkTaskId = checkTaskId(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(checkTaskId ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        moveTaskBackwards(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        Bitmap _result7 = getTaskThumbnailOnPCMode(data.readInt());
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        boolean requestContentNode = requestContentNode(_arg0, _arg12, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(requestContentNode ? 1 : 0);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg13 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        boolean requestContentOther = requestContentOther(_arg02, _arg13, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(requestContentOther ? 1 : 0);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        boolean addGameSpacePackageList = addGameSpacePackageList(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(addGameSpacePackageList ? 1 : 0);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        boolean delGameSpacePackageList = delGameSpacePackageList(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(delGameSpacePackageList ? 1 : 0);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        registerGameObserver(IGameObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterGameObserver(IGameObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        registerGameObserverEx(IGameObserverEx.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterGameObserverEx(IGameObserverEx.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInGameSpace = isInGameSpace(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isInGameSpace ? 1 : 0);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result8 = getGameList();
                        reply.writeNoException();
                        reply.writeStringList(_result8);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isGameDndOn = isGameDndOn();
                        reply.writeNoException();
                        reply.writeInt(isGameDndOn ? 1 : 0);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isGameDndOnEx = isGameDndOnEx();
                        reply.writeNoException();
                        reply.writeInt(isGameDndOnEx ? 1 : 0);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isGameKeyControlOn = isGameKeyControlOn();
                        reply.writeNoException();
                        reply.writeInt(isGameKeyControlOn ? 1 : 0);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isGameGestureDisabled = isGameGestureDisabled();
                        reply.writeNoException();
                        reply.writeInt(isGameGestureDisabled ? 1 : 0);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isFreeFormVisible = isFreeFormVisible();
                        reply.writeNoException();
                        reply.writeInt(isFreeFormVisible ? 1 : 0);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isTaskVisible = isTaskVisible(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isTaskVisible ? 1 : 0);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        updateFreeFormOutLine(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = getCaptionState(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = getActivityWindowMode(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        onCaptionDropAnimationDone(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        List<ActivityManager.RunningTaskInfo> _result11 = getVisibleTasks();
                        reply.writeNoException();
                        reply.writeTypedList(_result11);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        ActivityManager.TaskSnapshot _result12 = getTaskSnapshot(data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result12 != null) {
                            reply.writeInt(1);
                            _result12.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg013 = data.readInt();
                        int[] _arg17 = data.createIntArray();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        int[] _result13 = setFreeformStackVisibility(_arg013, _arg17, _arg2);
                        reply.writeNoException();
                        reply.writeIntArray(_result13);
                        reply.writeIntArray(_arg17);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        dismissSplitScreenToFocusedStack();
                        reply.writeNoException();
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg014 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg14 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        handleMultiWindowSwitch(_arg014, _arg14);
                        reply.writeNoException();
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result14 = getSplitStacksPos(data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result14 != null) {
                            reply.writeInt(1);
                            _result14.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        boolean enterCoordinationMode = enterCoordinationMode(_arg03);
                        reply.writeNoException();
                        reply.writeInt(enterCoordinationMode ? 1 : 0);
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = true;
                        }
                        boolean exitCoordinationMode = exitCoordinationMode(_arg08);
                        reply.writeNoException();
                        reply.writeInt(exitCoordinationMode ? 1 : 0);
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = true;
                        }
                        setSplitBarVisibility(_arg07);
                        reply.writeNoException();
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        boolean customActivityController = setCustomActivityController(IActivityController.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(customActivityController ? 1 : 0);
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = ActivityInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        boolean isResizableApp = isResizableApp(_arg04);
                        reply.writeNoException();
                        reply.writeInt(isResizableApp ? 1 : 0);
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result15 = getHwMultiWindowAppControlLists();
                        reply.writeNoException();
                        if (_result15 != null) {
                            reply.writeInt(1);
                            _result15.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        saveMultiWindowTipState(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSupportDragForMultiWin = isSupportDragForMultiWin(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(isSupportDragForMultiWin ? 1 : 0);
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result16 = getVisiblePackages();
                        reply.writeNoException();
                        reply.writeStringList(_result16);
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = true;
                        }
                        boolean multiWindowDisabled = setMultiWindowDisabled(_arg06);
                        reply.writeNoException();
                        reply.writeInt(multiWindowDisabled ? 1 : 0);
                        return true;
                    case 60:
                        data.enforceInterface(DESCRIPTOR);
                        boolean multiWindowDisabled2 = getMultiWindowDisabled();
                        reply.writeNoException();
                        reply.writeInt(multiWindowDisabled2 ? 1 : 0);
                        return true;
                    case 61:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result17 = getHwMultiWindowState();
                        reply.writeNoException();
                        if (_result17 != null) {
                            reply.writeInt(1);
                            _result17.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
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
        public static class Proxy implements IHwActivityTaskManager {
            public static IHwActivityTaskManager sDefaultImpl;
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void registerHwActivityNotifier(IHwActivityNotifier notifier, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(notifier != null ? notifier.asBinder() : null);
                    _data.writeString(reason);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerHwActivityNotifier(notifier, reason);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void unregisterHwActivityNotifier(IHwActivityNotifier notifier) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(notifier != null ? notifier.asBinder() : null);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterHwActivityNotifier(notifier);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public ActivityInfo getLastResumedActivity() throws RemoteException {
                ActivityInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLastResumedActivity();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ActivityInfo.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Bundle getTopActivity() throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTopActivity();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void registerAtmDAMonitorCallback(IHwAtmDAMonitorCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerAtmDAMonitorCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void setWarmColdSwitch(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setWarmColdSwitch(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isInMultiWindowMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInMultiWindowMode();
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean registerThirdPartyCallBack(IMWThirdpartyCallback aCallBackHandler) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(aCallBackHandler != null ? aCallBackHandler.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerThirdPartyCallBack(aCallBackHandler);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean unregisterThirdPartyCallBack(IMWThirdpartyCallback aCallBackHandler) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(aCallBackHandler != null ? aCallBackHandler.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterThirdPartyCallBack(aCallBackHandler);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int getTopTaskIdInDisplay(int displayId, String pkgName, boolean invisibleAlso) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeString(pkgName);
                    _data.writeInt(invisibleAlso ? 1 : 0);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTopTaskIdInDisplay(displayId, pkgName, invisibleAlso);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isTaskSupportResize(int taskId, boolean isFullscreen, boolean isMaximized) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    boolean _result = true;
                    _data.writeInt(isFullscreen ? 1 : 0);
                    _data.writeInt(isMaximized ? 1 : 0);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isTaskSupportResize(taskId, isFullscreen, isMaximized);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Rect getPCTopTaskBounds(int displayId) throws RemoteException {
                Rect _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPCTopTaskBounds(displayId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Rect.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void hwRestoreTask(int taskId, float xPos, float yPos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeFloat(xPos);
                    _data.writeFloat(yPos);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().hwRestoreTask(taskId, xPos, yPos);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
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
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int getWindowState(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public HwRecentTaskInfo getHwRecentTaskInfo(int taskId) throws RemoteException {
                HwRecentTaskInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void togglePCMode(boolean pcMode, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pcMode ? 1 : 0);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().togglePCMode(pcMode, displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void toggleHome() throws RemoteException {
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
                    Stub.getDefaultImpl().toggleHome();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void unRegisterHwTaskStackListener(ITaskStackListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void registerHwTaskStackListener(ITaskStackListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean checkTaskId(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    boolean _result = false;
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkTaskId(taskId);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void moveTaskBackwards(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().moveTaskBackwards(taskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Bitmap getTaskThumbnailOnPCMode(int taskId) throws RemoteException {
                Bitmap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTaskThumbnailOnPCMode(taskId);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean requestContentNode(ComponentName componentName, Bundle bundle, int token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(token);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestContentNode(componentName, bundle, token);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean requestContentOther(ComponentName componentName, Bundle bundle, int token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(token);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestContentOther(componentName, bundle, token);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean addGameSpacePackageList(List<String> packageList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageList);
                    boolean _result = false;
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addGameSpacePackageList(packageList);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean delGameSpacePackageList(List<String> packageList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageList);
                    boolean _result = false;
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().delGameSpacePackageList(packageList);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void registerGameObserver(IGameObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(28, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerGameObserver(observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void unregisterGameObserver(IGameObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterGameObserver(observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void registerGameObserverEx(IGameObserverEx observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(30, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerGameObserverEx(observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void unregisterGameObserverEx(IGameObserverEx observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(31, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterGameObserverEx(observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isInGameSpace(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInGameSpace(packageName);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public List<String> getGameList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGameList();
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isGameDndOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(34, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isGameDndOn();
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isGameDndOnEx() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isGameDndOnEx();
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isGameKeyControlOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isGameKeyControlOn();
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isGameGestureDisabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isGameGestureDisabled();
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isFreeFormVisible() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isFreeFormVisible();
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isTaskVisible(int id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    boolean _result = false;
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isTaskVisible(id);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void updateFreeFormOutLine(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    if (this.mRemote.transact(40, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateFreeFormOutLine(state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int getCaptionState(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCaptionState(token);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int getActivityWindowMode(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActivityWindowMode(token);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void onCaptionDropAnimationDone(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(43, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onCaptionDropAnimationDone(token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public List<ActivityManager.RunningTaskInfo> getVisibleTasks() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVisibleTasks();
                    }
                    _reply.readException();
                    List<ActivityManager.RunningTaskInfo> _result = _reply.createTypedArrayList(ActivityManager.RunningTaskInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public ActivityManager.TaskSnapshot getTaskSnapshot(int taskId, boolean reducedResolution) throws RemoteException {
                ActivityManager.TaskSnapshot _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(reducedResolution ? 1 : 0);
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTaskSnapshot(taskId, reducedResolution);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ActivityManager.TaskSnapshot.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int[] setFreeformStackVisibility(int displayId, int[] stackIdArray, boolean isVisible) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeIntArray(stackIdArray);
                    _data.writeInt(isVisible ? 1 : 0);
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setFreeformStackVisibility(displayId, stackIdArray, isVisible);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.readIntArray(stackIdArray);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void dismissSplitScreenToFocusedStack() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(47, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dismissSplitScreenToFocusedStack();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void handleMultiWindowSwitch(IBinder token, Bundle info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(48, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().handleMultiWindowSwitch(token, info);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Bundle getSplitStacksPos(int displayId, int splitRatio) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(splitRatio);
                    if (!this.mRemote.transact(49, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSplitStacksPos(displayId, splitRatio);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean enterCoordinationMode(Intent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(50, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enterCoordinationMode(intent);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean exitCoordinationMode(boolean toTop) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(toTop ? 1 : 0);
                    if (!this.mRemote.transact(51, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().exitCoordinationMode(toTop);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void setSplitBarVisibility(boolean isVisibility) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isVisibility ? 1 : 0);
                    if (this.mRemote.transact(52, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSplitBarVisibility(isVisibility);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean setCustomActivityController(IActivityController controller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(controller != null ? controller.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(53, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setCustomActivityController(controller);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isResizableApp(ActivityInfo activityInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (activityInfo != null) {
                        _data.writeInt(1);
                        activityInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(54, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isResizableApp(activityInfo);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Bundle getHwMultiWindowAppControlLists() throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(55, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwMultiWindowAppControlLists();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void saveMultiWindowTipState(String tipKey, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(tipKey);
                    _data.writeInt(state);
                    if (this.mRemote.transact(56, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().saveMultiWindowTipState(tipKey, state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isSupportDragForMultiWin(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    if (!this.mRemote.transact(57, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSupportDragForMultiWin(token);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public List<String> getVisiblePackages() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(58, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVisiblePackages();
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean setMultiWindowDisabled(boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(disabled ? 1 : 0);
                    if (!this.mRemote.transact(59, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setMultiWindowDisabled(disabled);
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean getMultiWindowDisabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(60, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMultiWindowDisabled();
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

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Bundle getHwMultiWindowState() throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(61, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwMultiWindowState();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
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
        }

        public static boolean setDefaultImpl(IHwActivityTaskManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwActivityTaskManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

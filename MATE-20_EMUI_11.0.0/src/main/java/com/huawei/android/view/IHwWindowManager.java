package com.huawei.android.view;

import android.content.ClipData;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.DragEvent;
import android.view.IHwRotateObserver;
import android.widget.RemoteViews;
import com.huawei.android.view.IHwMouseEventListener;
import com.huawei.android.view.IHwMultiDisplayBasicModeDragStartListener;
import com.huawei.android.view.IHwMultiDisplayBitmapDragStartListener;
import com.huawei.android.view.IHwMultiDisplayDragStartListener;
import com.huawei.android.view.IHwMultiDisplayDragStateListener;
import com.huawei.android.view.IHwMultiDisplayDropStartListener;
import com.huawei.android.view.IHwMultiDisplayDroppableListener;
import com.huawei.android.view.IHwMultiDisplayPhoneOperateListener;
import com.huawei.android.view.IHwWMDAMonitorCallback;
import java.util.List;

public interface IHwWindowManager extends IInterface {
    void cancelDragAndDrop(boolean z) throws RemoteException;

    void dismissKeyguardLw() throws RemoteException;

    boolean dragStartForBasicMode(ClipData clipData, Bitmap bitmap) throws RemoteException;

    boolean dragStartForMultiDisplay(ClipData clipData) throws RemoteException;

    boolean dropStartForMultiDisplay(DragEvent dragEvent) throws RemoteException;

    void freezeOrThawRotation(int i) throws RemoteException;

    int getAppUseNotchMode(String str) throws RemoteException;

    List<Rect> getBounds(int i) throws RemoteException;

    void getCurrFocusedWinInExtDisplay(Bundle bundle) throws RemoteException;

    float getDeviceMaxRatio() throws RemoteException;

    Bitmap getDisplayBitmap(int i, int i2, int i3) throws RemoteException;

    String getDragSrcPkgName() throws RemoteException;

    int getFocusWindowWidth() throws RemoteException;

    HwTaskSnapshotWrapper getForegroundTaskSnapshotWrapper(boolean z) throws RemoteException;

    List<String> getNotchSystemApps() throws RemoteException;

    int getRestrictedScreenHeight() throws RemoteException;

    Rect getSafeInsets(int i) throws RemoteException;

    int getTopActivityAdaptNotchState(String str) throws RemoteException;

    Rect getTopAppDisplayBounds(float f, int i) throws RemoteException;

    String getTouchedWinPackageName(float f, float f2, int i) throws RemoteException;

    List<Bundle> getVisibleWindows(int i) throws RemoteException;

    boolean hasLighterViewInPCCastMode() throws RemoteException;

    boolean isAppControlPolicyExists() throws RemoteException;

    boolean isAppNeedExpand(String str) throws RemoteException;

    boolean isDragDropActive() throws RemoteException;

    boolean isFullScreenDevice() throws RemoteException;

    boolean isNavigationBarVisible() throws RemoteException;

    boolean isNeedExceptDisplaySide(Rect rect) throws RemoteException;

    boolean isNeedForbidDialogAct(String str, ComponentName componentName) throws RemoteException;

    boolean isSwitched() throws RemoteException;

    boolean isTaskPositionDebugOpen() throws RemoteException;

    boolean isWindowSupportKnuckle() throws RemoteException;

    boolean notifyDragAndDropForMultiDisplay(float f, float f2, int i, DragEvent dragEvent) throws RemoteException;

    void notifySwingRotation(int i) throws RemoteException;

    void registerBitmapDragListenerForMultiDisplay(IHwMultiDisplayBitmapDragStartListener iHwMultiDisplayBitmapDragStartListener) throws RemoteException;

    void registerDragListenerForMultiDisplay(IHwMultiDisplayDragStartListener iHwMultiDisplayDragStartListener) throws RemoteException;

    void registerDropListenerForMultiDisplay(IHwMultiDisplayDropStartListener iHwMultiDisplayDropStartListener) throws RemoteException;

    void registerHwMultiDisplayBasicModeDragListener(IHwMultiDisplayBasicModeDragStartListener iHwMultiDisplayBasicModeDragStartListener) throws RemoteException;

    void registerHwMultiDisplayDragStateListener(IHwMultiDisplayDragStateListener iHwMultiDisplayDragStateListener) throws RemoteException;

    void registerIsDroppableForMultiDisplay(IHwMultiDisplayDroppableListener iHwMultiDisplayDroppableListener) throws RemoteException;

    void registerMouseEventListener(IHwMouseEventListener iHwMouseEventListener, int i) throws RemoteException;

    void registerPhoneOperateListenerForHwMultiDisplay(IHwMultiDisplayPhoneOperateListener iHwMultiDisplayPhoneOperateListener) throws RemoteException;

    void registerRotateObserver(IHwRotateObserver iHwRotateObserver) throws RemoteException;

    boolean registerWMMonitorCallback(IHwWMDAMonitorCallback iHwWMDAMonitorCallback) throws RemoteException;

    void removeAppView(boolean z) throws RemoteException;

    void restoreShadow() throws RemoteException;

    void setAboveAppWindowsContainersVisible(boolean z) throws RemoteException;

    void setActivityVisibleInFingerBoost(boolean z) throws RemoteException;

    void setAppWindowExitInfo(Bundle bundle, Bitmap bitmap) throws RemoteException;

    void setCoverManagerState(boolean z) throws RemoteException;

    void setDragShadowVisible(boolean z) throws RemoteException;

    boolean setDragStartBitmap(Bitmap bitmap) throws RemoteException;

    void setDroppableForMultiDisplay(float f, float f2, boolean z) throws RemoteException;

    void setForcedDisplayDensityAndSize(int i, int i2, int i3, int i4) throws RemoteException;

    void setGestureNavMode(String str, int i, int i2, int i3) throws RemoteException;

    void setOriginalDropPoint(float f, float f2) throws RemoteException;

    boolean shouldDropMotionEventForTouchPad(float f, float f2) throws RemoteException;

    void startNotifyWindowFocusChange() throws RemoteException;

    void stopNotifyWindowFocusChange() throws RemoteException;

    void unregisterBitmapDragListenerForMultiDisplay() throws RemoteException;

    void unregisterDragListenerForMultiDisplay() throws RemoteException;

    void unregisterDropListenerForMultiDisplay() throws RemoteException;

    void unregisterHwMultiDisplayBasicModeDragListener() throws RemoteException;

    void unregisterHwMultiDisplayDragStateListener() throws RemoteException;

    void unregisterMouseEventListener(int i) throws RemoteException;

    void unregisterPhoneOperateListenerForHwMultiDisplay() throws RemoteException;

    void unregisterRotateObserver(IHwRotateObserver iHwRotateObserver) throws RemoteException;

    void updateAppView(RemoteViews remoteViews) throws RemoteException;

    void updateDragState(int i) throws RemoteException;

    void updateFocusWindowFreezed(boolean z) throws RemoteException;

    public static class Default implements IHwWindowManager {
        @Override // com.huawei.android.view.IHwWindowManager
        public boolean isFullScreenDevice() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public float getDeviceMaxRatio() throws RemoteException {
            return 0.0f;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public Rect getTopAppDisplayBounds(float appMaxRatio, int rotation) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void registerRotateObserver(IHwRotateObserver observer) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void unregisterRotateObserver(IHwRotateObserver observer) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void updateAppView(RemoteViews remoteViews) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void removeAppView(boolean isNeedAddBtnView) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public List<String> getNotchSystemApps() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public int getAppUseNotchMode(String packageName) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void setAppWindowExitInfo(Bundle bundle, Bitmap iconBitmap) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public boolean registerWMMonitorCallback(IHwWMDAMonitorCallback callback) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public List<Bundle> getVisibleWindows(int ops) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public int getFocusWindowWidth() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void startNotifyWindowFocusChange() throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void stopNotifyWindowFocusChange() throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void getCurrFocusedWinInExtDisplay(Bundle outBundle) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public boolean hasLighterViewInPCCastMode() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public boolean shouldDropMotionEventForTouchPad(float x, float y) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public HwTaskSnapshotWrapper getForegroundTaskSnapshotWrapper(boolean refresh) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void setCoverManagerState(boolean isCoverOpen) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void freezeOrThawRotation(int rotation) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public Bitmap getDisplayBitmap(int displayId, int width, int height) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public int getRestrictedScreenHeight() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public boolean isWindowSupportKnuckle() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public boolean isNavigationBarVisible() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void dismissKeyguardLw() throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void setGestureNavMode(String packageName, int leftMode, int rightMode, int bottomMode) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void setForcedDisplayDensityAndSize(int displayId, int density, int width, int height) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void notifySwingRotation(int rotation) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public Rect getSafeInsets(int type) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public List<Rect> getBounds(int type) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public String getTouchedWinPackageName(float x, float y, int displayId) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public boolean notifyDragAndDropForMultiDisplay(float x, float y, int displayId, DragEvent evt) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void registerDragListenerForMultiDisplay(IHwMultiDisplayDragStartListener listener) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void unregisterDragListenerForMultiDisplay() throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void registerBitmapDragListenerForMultiDisplay(IHwMultiDisplayBitmapDragStartListener listener) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void unregisterBitmapDragListenerForMultiDisplay() throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public boolean setDragStartBitmap(Bitmap b) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void registerHwMultiDisplayBasicModeDragListener(IHwMultiDisplayBasicModeDragStartListener listener) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void unregisterHwMultiDisplayBasicModeDragListener() throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public boolean dragStartForBasicMode(ClipData data, Bitmap bitmap) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public boolean isDragDropActive() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void setDragShadowVisible(boolean visible) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public boolean dragStartForMultiDisplay(ClipData clipData) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void registerDropListenerForMultiDisplay(IHwMultiDisplayDropStartListener listener) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void unregisterDropListenerForMultiDisplay() throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public boolean dropStartForMultiDisplay(DragEvent evt) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void registerIsDroppableForMultiDisplay(IHwMultiDisplayDroppableListener listener) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void setDroppableForMultiDisplay(float x, float y, boolean result) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void setOriginalDropPoint(float x, float yIHwWindowManager) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void registerHwMultiDisplayDragStateListener(IHwMultiDisplayDragStateListener listener) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void unregisterHwMultiDisplayDragStateListener() throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void updateDragState(int dragState) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public boolean isNeedExceptDisplaySide(Rect exceptDisplayRect) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public boolean isAppNeedExpand(String pkgName) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public String getDragSrcPkgName() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public boolean isSwitched() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void restoreShadow() throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void updateFocusWindowFreezed(boolean needFreezed) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void registerPhoneOperateListenerForHwMultiDisplay(IHwMultiDisplayPhoneOperateListener listener) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void unregisterPhoneOperateListenerForHwMultiDisplay() throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public int getTopActivityAdaptNotchState(String packageName) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void registerMouseEventListener(IHwMouseEventListener listener, int displayId) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void unregisterMouseEventListener(int displayId) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void setAboveAppWindowsContainersVisible(boolean isVisible) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void cancelDragAndDrop(boolean skipAnimation) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public void setActivityVisibleInFingerBoost(boolean isVisible) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public boolean isNeedForbidDialogAct(String packageName, ComponentName componentName) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public boolean isTaskPositionDebugOpen() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.view.IHwWindowManager
        public boolean isAppControlPolicyExists() throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwWindowManager {
        private static final String DESCRIPTOR = "com.huawei.android.view.IHwWindowManager";
        static final int TRANSACTION_cancelDragAndDrop = 66;
        static final int TRANSACTION_dismissKeyguardLw = 26;
        static final int TRANSACTION_dragStartForBasicMode = 41;
        static final int TRANSACTION_dragStartForMultiDisplay = 44;
        static final int TRANSACTION_dropStartForMultiDisplay = 47;
        static final int TRANSACTION_freezeOrThawRotation = 21;
        static final int TRANSACTION_getAppUseNotchMode = 9;
        static final int TRANSACTION_getBounds = 31;
        static final int TRANSACTION_getCurrFocusedWinInExtDisplay = 16;
        static final int TRANSACTION_getDeviceMaxRatio = 2;
        static final int TRANSACTION_getDisplayBitmap = 22;
        static final int TRANSACTION_getDragSrcPkgName = 56;
        static final int TRANSACTION_getFocusWindowWidth = 13;
        static final int TRANSACTION_getForegroundTaskSnapshotWrapper = 19;
        static final int TRANSACTION_getNotchSystemApps = 8;
        static final int TRANSACTION_getRestrictedScreenHeight = 23;
        static final int TRANSACTION_getSafeInsets = 30;
        static final int TRANSACTION_getTopActivityAdaptNotchState = 62;
        static final int TRANSACTION_getTopAppDisplayBounds = 3;
        static final int TRANSACTION_getTouchedWinPackageName = 32;
        static final int TRANSACTION_getVisibleWindows = 12;
        static final int TRANSACTION_hasLighterViewInPCCastMode = 17;
        static final int TRANSACTION_isAppControlPolicyExists = 70;
        static final int TRANSACTION_isAppNeedExpand = 55;
        static final int TRANSACTION_isDragDropActive = 42;
        static final int TRANSACTION_isFullScreenDevice = 1;
        static final int TRANSACTION_isNavigationBarVisible = 25;
        static final int TRANSACTION_isNeedExceptDisplaySide = 54;
        static final int TRANSACTION_isNeedForbidDialogAct = 68;
        static final int TRANSACTION_isSwitched = 57;
        static final int TRANSACTION_isTaskPositionDebugOpen = 69;
        static final int TRANSACTION_isWindowSupportKnuckle = 24;
        static final int TRANSACTION_notifyDragAndDropForMultiDisplay = 33;
        static final int TRANSACTION_notifySwingRotation = 29;
        static final int TRANSACTION_registerBitmapDragListenerForMultiDisplay = 36;
        static final int TRANSACTION_registerDragListenerForMultiDisplay = 34;
        static final int TRANSACTION_registerDropListenerForMultiDisplay = 45;
        static final int TRANSACTION_registerHwMultiDisplayBasicModeDragListener = 39;
        static final int TRANSACTION_registerHwMultiDisplayDragStateListener = 51;
        static final int TRANSACTION_registerIsDroppableForMultiDisplay = 48;
        static final int TRANSACTION_registerMouseEventListener = 63;
        static final int TRANSACTION_registerPhoneOperateListenerForHwMultiDisplay = 60;
        static final int TRANSACTION_registerRotateObserver = 4;
        static final int TRANSACTION_registerWMMonitorCallback = 11;
        static final int TRANSACTION_removeAppView = 7;
        static final int TRANSACTION_restoreShadow = 58;
        static final int TRANSACTION_setAboveAppWindowsContainersVisible = 65;
        static final int TRANSACTION_setActivityVisibleInFingerBoost = 67;
        static final int TRANSACTION_setAppWindowExitInfo = 10;
        static final int TRANSACTION_setCoverManagerState = 20;
        static final int TRANSACTION_setDragShadowVisible = 43;
        static final int TRANSACTION_setDragStartBitmap = 38;
        static final int TRANSACTION_setDroppableForMultiDisplay = 49;
        static final int TRANSACTION_setForcedDisplayDensityAndSize = 28;
        static final int TRANSACTION_setGestureNavMode = 27;
        static final int TRANSACTION_setOriginalDropPoint = 50;
        static final int TRANSACTION_shouldDropMotionEventForTouchPad = 18;
        static final int TRANSACTION_startNotifyWindowFocusChange = 14;
        static final int TRANSACTION_stopNotifyWindowFocusChange = 15;
        static final int TRANSACTION_unregisterBitmapDragListenerForMultiDisplay = 37;
        static final int TRANSACTION_unregisterDragListenerForMultiDisplay = 35;
        static final int TRANSACTION_unregisterDropListenerForMultiDisplay = 46;
        static final int TRANSACTION_unregisterHwMultiDisplayBasicModeDragListener = 40;
        static final int TRANSACTION_unregisterHwMultiDisplayDragStateListener = 52;
        static final int TRANSACTION_unregisterMouseEventListener = 64;
        static final int TRANSACTION_unregisterPhoneOperateListenerForHwMultiDisplay = 61;
        static final int TRANSACTION_unregisterRotateObserver = 5;
        static final int TRANSACTION_updateAppView = 6;
        static final int TRANSACTION_updateDragState = 53;
        static final int TRANSACTION_updateFocusWindowFreezed = 59;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwWindowManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwWindowManager)) {
                return new Proxy(obj);
            }
            return (IHwWindowManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "isFullScreenDevice";
                case 2:
                    return "getDeviceMaxRatio";
                case 3:
                    return "getTopAppDisplayBounds";
                case 4:
                    return "registerRotateObserver";
                case 5:
                    return "unregisterRotateObserver";
                case 6:
                    return "updateAppView";
                case 7:
                    return "removeAppView";
                case 8:
                    return "getNotchSystemApps";
                case 9:
                    return "getAppUseNotchMode";
                case 10:
                    return "setAppWindowExitInfo";
                case 11:
                    return "registerWMMonitorCallback";
                case 12:
                    return "getVisibleWindows";
                case 13:
                    return "getFocusWindowWidth";
                case 14:
                    return "startNotifyWindowFocusChange";
                case 15:
                    return "stopNotifyWindowFocusChange";
                case 16:
                    return "getCurrFocusedWinInExtDisplay";
                case 17:
                    return "hasLighterViewInPCCastMode";
                case 18:
                    return "shouldDropMotionEventForTouchPad";
                case 19:
                    return "getForegroundTaskSnapshotWrapper";
                case 20:
                    return "setCoverManagerState";
                case 21:
                    return "freezeOrThawRotation";
                case 22:
                    return "getDisplayBitmap";
                case 23:
                    return "getRestrictedScreenHeight";
                case 24:
                    return "isWindowSupportKnuckle";
                case 25:
                    return "isNavigationBarVisible";
                case 26:
                    return "dismissKeyguardLw";
                case 27:
                    return "setGestureNavMode";
                case 28:
                    return "setForcedDisplayDensityAndSize";
                case 29:
                    return "notifySwingRotation";
                case 30:
                    return "getSafeInsets";
                case 31:
                    return "getBounds";
                case 32:
                    return "getTouchedWinPackageName";
                case 33:
                    return "notifyDragAndDropForMultiDisplay";
                case 34:
                    return "registerDragListenerForMultiDisplay";
                case 35:
                    return "unregisterDragListenerForMultiDisplay";
                case 36:
                    return "registerBitmapDragListenerForMultiDisplay";
                case 37:
                    return "unregisterBitmapDragListenerForMultiDisplay";
                case 38:
                    return "setDragStartBitmap";
                case 39:
                    return "registerHwMultiDisplayBasicModeDragListener";
                case 40:
                    return "unregisterHwMultiDisplayBasicModeDragListener";
                case 41:
                    return "dragStartForBasicMode";
                case 42:
                    return "isDragDropActive";
                case 43:
                    return "setDragShadowVisible";
                case 44:
                    return "dragStartForMultiDisplay";
                case 45:
                    return "registerDropListenerForMultiDisplay";
                case 46:
                    return "unregisterDropListenerForMultiDisplay";
                case 47:
                    return "dropStartForMultiDisplay";
                case 48:
                    return "registerIsDroppableForMultiDisplay";
                case 49:
                    return "setDroppableForMultiDisplay";
                case 50:
                    return "setOriginalDropPoint";
                case 51:
                    return "registerHwMultiDisplayDragStateListener";
                case 52:
                    return "unregisterHwMultiDisplayDragStateListener";
                case 53:
                    return "updateDragState";
                case 54:
                    return "isNeedExceptDisplaySide";
                case 55:
                    return "isAppNeedExpand";
                case 56:
                    return "getDragSrcPkgName";
                case 57:
                    return "isSwitched";
                case 58:
                    return "restoreShadow";
                case 59:
                    return "updateFocusWindowFreezed";
                case 60:
                    return "registerPhoneOperateListenerForHwMultiDisplay";
                case 61:
                    return "unregisterPhoneOperateListenerForHwMultiDisplay";
                case 62:
                    return "getTopActivityAdaptNotchState";
                case 63:
                    return "registerMouseEventListener";
                case 64:
                    return "unregisterMouseEventListener";
                case 65:
                    return "setAboveAppWindowsContainersVisible";
                case 66:
                    return "cancelDragAndDrop";
                case 67:
                    return "setActivityVisibleInFingerBoost";
                case 68:
                    return "isNeedForbidDialogAct";
                case 69:
                    return "isTaskPositionDebugOpen";
                case 70:
                    return "isAppControlPolicyExists";
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
            RemoteViews _arg0;
            Bundle _arg02;
            Bitmap _arg1;
            DragEvent _arg3;
            Bitmap _arg03;
            ClipData _arg04;
            Bitmap _arg12;
            ClipData _arg05;
            DragEvent _arg06;
            ComponentName _arg13;
            if (code != 1598968902) {
                boolean _arg07 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isFullScreenDevice = isFullScreenDevice();
                        reply.writeNoException();
                        reply.writeInt(isFullScreenDevice ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        float _result = getDeviceMaxRatio();
                        reply.writeNoException();
                        reply.writeFloat(_result);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        Rect _result2 = getTopAppDisplayBounds(data.readFloat(), data.readInt());
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        registerRotateObserver(IHwRotateObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterRotateObserver(IHwRotateObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = RemoteViews.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        updateAppView(_arg0);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = true;
                        }
                        removeAppView(_arg07);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result3 = getNotchSystemApps();
                        reply.writeNoException();
                        reply.writeStringList(_result3);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getAppUseNotchMode(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = Bitmap.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        setAppWindowExitInfo(_arg02, _arg1);
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerWMMonitorCallback = registerWMMonitorCallback(IHwWMDAMonitorCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerWMMonitorCallback ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        List<Bundle> _result5 = getVisibleWindows(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result5);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = getFocusWindowWidth();
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        startNotifyWindowFocusChange();
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        stopNotifyWindowFocusChange();
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _arg08 = new Bundle();
                        getCurrFocusedWinInExtDisplay(_arg08);
                        reply.writeNoException();
                        reply.writeInt(1);
                        _arg08.writeToParcel(reply, 1);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasLighterViewInPCCastMode = hasLighterViewInPCCastMode();
                        reply.writeNoException();
                        reply.writeInt(hasLighterViewInPCCastMode ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean shouldDropMotionEventForTouchPad = shouldDropMotionEventForTouchPad(data.readFloat(), data.readFloat());
                        reply.writeNoException();
                        reply.writeInt(shouldDropMotionEventForTouchPad ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        HwTaskSnapshotWrapper _result7 = getForegroundTaskSnapshotWrapper(data.readInt() != 0);
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = true;
                        }
                        setCoverManagerState(_arg07);
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        freezeOrThawRotation(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        Bitmap _result8 = getDisplayBitmap(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result8 != null) {
                            reply.writeInt(1);
                            _result8.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = getRestrictedScreenHeight();
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isWindowSupportKnuckle = isWindowSupportKnuckle();
                        reply.writeNoException();
                        reply.writeInt(isWindowSupportKnuckle ? 1 : 0);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isNavigationBarVisible = isNavigationBarVisible();
                        reply.writeNoException();
                        reply.writeInt(isNavigationBarVisible ? 1 : 0);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        dismissKeyguardLw();
                        reply.writeNoException();
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        setGestureNavMode(data.readString(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        setForcedDisplayDensityAndSize(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        notifySwingRotation(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        Rect _result10 = getSafeInsets(data.readInt());
                        reply.writeNoException();
                        if (_result10 != null) {
                            reply.writeInt(1);
                            _result10.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        List<Rect> _result11 = getBounds(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result11);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        String _result12 = getTouchedWinPackageName(data.readFloat(), data.readFloat(), data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result12);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        float _arg09 = data.readFloat();
                        float _arg14 = data.readFloat();
                        int _arg2 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = DragEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        boolean notifyDragAndDropForMultiDisplay = notifyDragAndDropForMultiDisplay(_arg09, _arg14, _arg2, _arg3);
                        reply.writeNoException();
                        reply.writeInt(notifyDragAndDropForMultiDisplay ? 1 : 0);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        registerDragListenerForMultiDisplay(IHwMultiDisplayDragStartListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterDragListenerForMultiDisplay();
                        reply.writeNoException();
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        registerBitmapDragListenerForMultiDisplay(IHwMultiDisplayBitmapDragStartListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterBitmapDragListenerForMultiDisplay();
                        reply.writeNoException();
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = Bitmap.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        boolean dragStartBitmap = setDragStartBitmap(_arg03);
                        reply.writeNoException();
                        reply.writeInt(dragStartBitmap ? 1 : 0);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        registerHwMultiDisplayBasicModeDragListener(IHwMultiDisplayBasicModeDragStartListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterHwMultiDisplayBasicModeDragListener();
                        reply.writeNoException();
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = ClipData.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = Bitmap.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        boolean dragStartForBasicMode = dragStartForBasicMode(_arg04, _arg12);
                        reply.writeNoException();
                        reply.writeInt(dragStartForBasicMode ? 1 : 0);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDragDropActive = isDragDropActive();
                        reply.writeNoException();
                        reply.writeInt(isDragDropActive ? 1 : 0);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = true;
                        }
                        setDragShadowVisible(_arg07);
                        reply.writeNoException();
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = ClipData.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        boolean dragStartForMultiDisplay = dragStartForMultiDisplay(_arg05);
                        reply.writeNoException();
                        reply.writeInt(dragStartForMultiDisplay ? 1 : 0);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        registerDropListenerForMultiDisplay(IHwMultiDisplayDropStartListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterDropListenerForMultiDisplay();
                        reply.writeNoException();
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = DragEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        boolean dropStartForMultiDisplay = dropStartForMultiDisplay(_arg06);
                        reply.writeNoException();
                        reply.writeInt(dropStartForMultiDisplay ? 1 : 0);
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        registerIsDroppableForMultiDisplay(IHwMultiDisplayDroppableListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        float _arg010 = data.readFloat();
                        float _arg15 = data.readFloat();
                        if (data.readInt() != 0) {
                            _arg07 = true;
                        }
                        setDroppableForMultiDisplay(_arg010, _arg15, _arg07);
                        reply.writeNoException();
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        setOriginalDropPoint(data.readFloat(), data.readFloat());
                        reply.writeNoException();
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        registerHwMultiDisplayDragStateListener(IHwMultiDisplayDragStateListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterHwMultiDisplayDragStateListener();
                        reply.writeNoException();
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        updateDragState(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        Rect _arg011 = new Rect();
                        boolean isNeedExceptDisplaySide = isNeedExceptDisplaySide(_arg011);
                        reply.writeNoException();
                        reply.writeInt(isNeedExceptDisplaySide ? 1 : 0);
                        reply.writeInt(1);
                        _arg011.writeToParcel(reply, 1);
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAppNeedExpand = isAppNeedExpand(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isAppNeedExpand ? 1 : 0);
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        String _result13 = getDragSrcPkgName();
                        reply.writeNoException();
                        reply.writeString(_result13);
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSwitched = isSwitched();
                        reply.writeNoException();
                        reply.writeInt(isSwitched ? 1 : 0);
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        restoreShadow();
                        reply.writeNoException();
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = true;
                        }
                        updateFocusWindowFreezed(_arg07);
                        reply.writeNoException();
                        return true;
                    case 60:
                        data.enforceInterface(DESCRIPTOR);
                        registerPhoneOperateListenerForHwMultiDisplay(IHwMultiDisplayPhoneOperateListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 61:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterPhoneOperateListenerForHwMultiDisplay();
                        reply.writeNoException();
                        return true;
                    case 62:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = getTopActivityAdaptNotchState(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 63:
                        data.enforceInterface(DESCRIPTOR);
                        registerMouseEventListener(IHwMouseEventListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 64:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterMouseEventListener(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 65:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = true;
                        }
                        setAboveAppWindowsContainersVisible(_arg07);
                        reply.writeNoException();
                        return true;
                    case 66:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = true;
                        }
                        cancelDragAndDrop(_arg07);
                        reply.writeNoException();
                        return true;
                    case 67:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = true;
                        }
                        setActivityVisibleInFingerBoost(_arg07);
                        reply.writeNoException();
                        return true;
                    case 68:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg012 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        boolean isNeedForbidDialogAct = isNeedForbidDialogAct(_arg012, _arg13);
                        reply.writeNoException();
                        reply.writeInt(isNeedForbidDialogAct ? 1 : 0);
                        return true;
                    case 69:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isTaskPositionDebugOpen = isTaskPositionDebugOpen();
                        reply.writeNoException();
                        reply.writeInt(isTaskPositionDebugOpen ? 1 : 0);
                        return true;
                    case 70:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAppControlPolicyExists = isAppControlPolicyExists();
                        reply.writeNoException();
                        reply.writeInt(isAppControlPolicyExists ? 1 : 0);
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
        public static class Proxy implements IHwWindowManager {
            public static IHwWindowManager sDefaultImpl;
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

            @Override // com.huawei.android.view.IHwWindowManager
            public boolean isFullScreenDevice() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isFullScreenDevice();
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

            @Override // com.huawei.android.view.IHwWindowManager
            public float getDeviceMaxRatio() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceMaxRatio();
                    }
                    _reply.readException();
                    float _result = _reply.readFloat();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public Rect getTopAppDisplayBounds(float appMaxRatio, int rotation) throws RemoteException {
                Rect _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(appMaxRatio);
                    _data.writeInt(rotation);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTopAppDisplayBounds(appMaxRatio, rotation);
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

            @Override // com.huawei.android.view.IHwWindowManager
            public void registerRotateObserver(IHwRotateObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerRotateObserver(observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void unregisterRotateObserver(IHwRotateObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterRotateObserver(observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void updateAppView(RemoteViews remoteViews) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (remoteViews != null) {
                        _data.writeInt(1);
                        remoteViews.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateAppView(remoteViews);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void removeAppView(boolean isNeedAddBtnView) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isNeedAddBtnView ? 1 : 0);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeAppView(isNeedAddBtnView);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public List<String> getNotchSystemApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNotchSystemApps();
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

            @Override // com.huawei.android.view.IHwWindowManager
            public int getAppUseNotchMode(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppUseNotchMode(packageName);
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

            @Override // com.huawei.android.view.IHwWindowManager
            public void setAppWindowExitInfo(Bundle bundle, Bitmap iconBitmap) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (iconBitmap != null) {
                        _data.writeInt(1);
                        iconBitmap.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAppWindowExitInfo(bundle, iconBitmap);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public boolean registerWMMonitorCallback(IHwWMDAMonitorCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerWMMonitorCallback(callback);
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

            @Override // com.huawei.android.view.IHwWindowManager
            public List<Bundle> getVisibleWindows(int ops) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ops);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVisibleWindows(ops);
                    }
                    _reply.readException();
                    List<Bundle> _result = _reply.createTypedArrayList(Bundle.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public int getFocusWindowWidth() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFocusWindowWidth();
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

            @Override // com.huawei.android.view.IHwWindowManager
            public void startNotifyWindowFocusChange() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startNotifyWindowFocusChange();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void stopNotifyWindowFocusChange() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopNotifyWindowFocusChange();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void getCurrFocusedWinInExtDisplay(Bundle outBundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        if (_reply.readInt() != 0) {
                            outBundle.readFromParcel(_reply);
                        }
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getCurrFocusedWinInExtDisplay(outBundle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public boolean hasLighterViewInPCCastMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasLighterViewInPCCastMode();
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

            @Override // com.huawei.android.view.IHwWindowManager
            public boolean shouldDropMotionEventForTouchPad(float x, float y) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(x);
                    _data.writeFloat(y);
                    boolean _result = false;
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().shouldDropMotionEventForTouchPad(x, y);
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

            @Override // com.huawei.android.view.IHwWindowManager
            public HwTaskSnapshotWrapper getForegroundTaskSnapshotWrapper(boolean refresh) throws RemoteException {
                HwTaskSnapshotWrapper _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(refresh ? 1 : 0);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getForegroundTaskSnapshotWrapper(refresh);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = HwTaskSnapshotWrapper.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.android.view.IHwWindowManager
            public void setCoverManagerState(boolean isCoverOpen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isCoverOpen ? 1 : 0);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCoverManagerState(isCoverOpen);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void freezeOrThawRotation(int rotation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rotation);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().freezeOrThawRotation(rotation);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public Bitmap getDisplayBitmap(int displayId, int width, int height) throws RemoteException {
                Bitmap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // com.huawei.android.view.IHwWindowManager
            public int getRestrictedScreenHeight() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRestrictedScreenHeight();
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

            @Override // com.huawei.android.view.IHwWindowManager
            public boolean isWindowSupportKnuckle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isWindowSupportKnuckle();
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

            @Override // com.huawei.android.view.IHwWindowManager
            public boolean isNavigationBarVisible() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isNavigationBarVisible();
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

            @Override // com.huawei.android.view.IHwWindowManager
            public void dismissKeyguardLw() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dismissKeyguardLw();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void setGestureNavMode(String packageName, int leftMode, int rightMode, int bottomMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(leftMode);
                    _data.writeInt(rightMode);
                    _data.writeInt(bottomMode);
                    if (this.mRemote.transact(27, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setGestureNavMode(packageName, leftMode, rightMode, bottomMode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void setForcedDisplayDensityAndSize(int displayId, int density, int width, int height) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(density);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    if (this.mRemote.transact(28, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setForcedDisplayDensityAndSize(displayId, density, width, height);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void notifySwingRotation(int rotation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rotation);
                    if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifySwingRotation(rotation);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public Rect getSafeInsets(int type) throws RemoteException {
                Rect _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSafeInsets(type);
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

            @Override // com.huawei.android.view.IHwWindowManager
            public List<Rect> getBounds(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBounds(type);
                    }
                    _reply.readException();
                    List<Rect> _result = _reply.createTypedArrayList(Rect.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public String getTouchedWinPackageName(float x, float y, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(x);
                    _data.writeFloat(y);
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTouchedWinPackageName(x, y, displayId);
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

            @Override // com.huawei.android.view.IHwWindowManager
            public boolean notifyDragAndDropForMultiDisplay(float x, float y, int displayId, DragEvent evt) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(x);
                    _data.writeFloat(y);
                    _data.writeInt(displayId);
                    boolean _result = true;
                    if (evt != null) {
                        _data.writeInt(1);
                        evt.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().notifyDragAndDropForMultiDisplay(x, y, displayId, evt);
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

            @Override // com.huawei.android.view.IHwWindowManager
            public void registerDragListenerForMultiDisplay(IHwMultiDisplayDragStartListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerDragListenerForMultiDisplay(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void unregisterDragListenerForMultiDisplay() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(35, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterDragListenerForMultiDisplay();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void registerBitmapDragListenerForMultiDisplay(IHwMultiDisplayBitmapDragStartListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(36, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerBitmapDragListenerForMultiDisplay(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void unregisterBitmapDragListenerForMultiDisplay() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(37, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterBitmapDragListenerForMultiDisplay();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public boolean setDragStartBitmap(Bitmap b) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (b != null) {
                        _data.writeInt(1);
                        b.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDragStartBitmap(b);
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

            @Override // com.huawei.android.view.IHwWindowManager
            public void registerHwMultiDisplayBasicModeDragListener(IHwMultiDisplayBasicModeDragStartListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(39, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerHwMultiDisplayBasicModeDragListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void unregisterHwMultiDisplayBasicModeDragListener() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(40, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterHwMultiDisplayBasicModeDragListener();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public boolean dragStartForBasicMode(ClipData data, Bitmap bitmap) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bitmap != null) {
                        _data.writeInt(1);
                        bitmap.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().dragStartForBasicMode(data, bitmap);
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

            @Override // com.huawei.android.view.IHwWindowManager
            public boolean isDragDropActive() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDragDropActive();
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

            @Override // com.huawei.android.view.IHwWindowManager
            public void setDragShadowVisible(boolean visible) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(visible ? 1 : 0);
                    if (this.mRemote.transact(43, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDragShadowVisible(visible);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public boolean dragStartForMultiDisplay(ClipData clipData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (clipData != null) {
                        _data.writeInt(1);
                        clipData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().dragStartForMultiDisplay(clipData);
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

            @Override // com.huawei.android.view.IHwWindowManager
            public void registerDropListenerForMultiDisplay(IHwMultiDisplayDropStartListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(45, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerDropListenerForMultiDisplay(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void unregisterDropListenerForMultiDisplay() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(46, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterDropListenerForMultiDisplay();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public boolean dropStartForMultiDisplay(DragEvent evt) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (evt != null) {
                        _data.writeInt(1);
                        evt.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(47, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().dropStartForMultiDisplay(evt);
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

            @Override // com.huawei.android.view.IHwWindowManager
            public void registerIsDroppableForMultiDisplay(IHwMultiDisplayDroppableListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(48, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerIsDroppableForMultiDisplay(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void setDroppableForMultiDisplay(float x, float y, boolean result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(x);
                    _data.writeFloat(y);
                    _data.writeInt(result ? 1 : 0);
                    if (this.mRemote.transact(49, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDroppableForMultiDisplay(x, y, result);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void setOriginalDropPoint(float x, float yIHwWindowManager) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(x);
                    _data.writeFloat(yIHwWindowManager);
                    if (this.mRemote.transact(50, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setOriginalDropPoint(x, yIHwWindowManager);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void registerHwMultiDisplayDragStateListener(IHwMultiDisplayDragStateListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(51, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerHwMultiDisplayDragStateListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void unregisterHwMultiDisplayDragStateListener() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(52, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterHwMultiDisplayDragStateListener();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void updateDragState(int dragState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(dragState);
                    if (this.mRemote.transact(53, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateDragState(dragState);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public boolean isNeedExceptDisplaySide(Rect exceptDisplayRect) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(54, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isNeedExceptDisplaySide(exceptDisplayRect);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    if (_reply.readInt() != 0) {
                        exceptDisplayRect.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public boolean isAppNeedExpand(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    boolean _result = false;
                    if (!this.mRemote.transact(55, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAppNeedExpand(pkgName);
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

            @Override // com.huawei.android.view.IHwWindowManager
            public String getDragSrcPkgName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(56, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDragSrcPkgName();
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

            @Override // com.huawei.android.view.IHwWindowManager
            public boolean isSwitched() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(57, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSwitched();
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

            @Override // com.huawei.android.view.IHwWindowManager
            public void restoreShadow() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(58, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().restoreShadow();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void updateFocusWindowFreezed(boolean needFreezed) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(needFreezed ? 1 : 0);
                    if (this.mRemote.transact(59, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateFocusWindowFreezed(needFreezed);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void registerPhoneOperateListenerForHwMultiDisplay(IHwMultiDisplayPhoneOperateListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(60, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerPhoneOperateListenerForHwMultiDisplay(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void unregisterPhoneOperateListenerForHwMultiDisplay() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(61, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterPhoneOperateListenerForHwMultiDisplay();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public int getTopActivityAdaptNotchState(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(62, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTopActivityAdaptNotchState(packageName);
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

            @Override // com.huawei.android.view.IHwWindowManager
            public void registerMouseEventListener(IHwMouseEventListener listener, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(63, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerMouseEventListener(listener, displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void unregisterMouseEventListener(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(64, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterMouseEventListener(displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void setAboveAppWindowsContainersVisible(boolean isVisible) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isVisible ? 1 : 0);
                    if (this.mRemote.transact(65, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAboveAppWindowsContainersVisible(isVisible);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void cancelDragAndDrop(boolean skipAnimation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(skipAnimation ? 1 : 0);
                    if (this.mRemote.transact(66, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelDragAndDrop(skipAnimation);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public void setActivityVisibleInFingerBoost(boolean isVisible) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isVisible ? 1 : 0);
                    if (this.mRemote.transact(67, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setActivityVisibleInFingerBoost(isVisible);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwWindowManager
            public boolean isNeedForbidDialogAct(String packageName, ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = true;
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(68, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isNeedForbidDialogAct(packageName, componentName);
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

            @Override // com.huawei.android.view.IHwWindowManager
            public boolean isTaskPositionDebugOpen() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(69, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isTaskPositionDebugOpen();
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

            @Override // com.huawei.android.view.IHwWindowManager
            public boolean isAppControlPolicyExists() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(70, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAppControlPolicyExists();
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
        }

        public static boolean setDefaultImpl(IHwWindowManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwWindowManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

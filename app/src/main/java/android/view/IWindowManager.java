package android.view;

import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IRemoteCallback;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;
import com.android.internal.app.IAssistScreenshotReceiver;
import com.android.internal.os.IResultReceiver;
import com.android.internal.policy.IShortcutService;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;

public interface IWindowManager extends IInterface {

    public static abstract class Stub extends Binder implements IWindowManager {
        private static final String DESCRIPTOR = "android.view.IWindowManager";
        static final int TRANSACTION_addAppToken = 21;
        static final int TRANSACTION_addWindowToken = 19;
        static final int TRANSACTION_cancelTaskThumbnailTransition = 69;
        static final int TRANSACTION_cancelTaskWindowTransition = 68;
        static final int TRANSACTION_clearForcedDisplayDensity = 13;
        static final int TRANSACTION_clearForcedDisplaySize = 9;
        static final int TRANSACTION_clearWindowContentFrameStats = 85;
        static final int TRANSACTION_closeSystemDialogs = 58;
        static final int TRANSACTION_createWallpaperInputConsumer = 99;
        static final int TRANSACTION_disableKeyguard = 50;
        static final int TRANSACTION_dismissKeyguard = 56;
        static final int TRANSACTION_enableScreenIfNeeded = 84;
        static final int TRANSACTION_endProlongedAnimations = 44;
        static final int TRANSACTION_executeAppTransition = 37;
        static final int TRANSACTION_exitKeyguardSecurely = 52;
        static final int TRANSACTION_freezeRotation = 75;
        static final int TRANSACTION_getAccelPackages = 97;
        static final int TRANSACTION_getAnimationScale = 59;
        static final int TRANSACTION_getAnimationScales = 60;
        static final int TRANSACTION_getAppOrientation = 24;
        static final int TRANSACTION_getBaseDisplayDensity = 11;
        static final int TRANSACTION_getBaseDisplaySize = 7;
        static final int TRANSACTION_getBoundsForNewConfiguration = 47;
        static final int TRANSACTION_getCurrentAnimatorScale = 63;
        static final int TRANSACTION_getDockedStackSide = 87;
        static final int TRANSACTION_getInitialDisplayDensity = 10;
        static final int TRANSACTION_getInitialDisplaySize = 6;
        static final int TRANSACTION_getNsdWindowInfo = 95;
        static final int TRANSACTION_getNsdWindowTitle = 96;
        static final int TRANSACTION_getPendingAppTransition = 27;
        static final int TRANSACTION_getPreferredOptionsPanelGravity = 74;
        static final int TRANSACTION_getRotation = 71;
        static final int TRANSACTION_getStableInsets = 93;
        static final int TRANSACTION_getWindowContentFrameStats = 86;
        static final int TRANSACTION_hasNavigationBar = 81;
        static final int TRANSACTION_inKeyguardRestrictedInputMode = 55;
        static final int TRANSACTION_inputMethodClientHasFocus = 5;
        static final int TRANSACTION_isKeyguardLocked = 53;
        static final int TRANSACTION_isKeyguardSecure = 54;
        static final int TRANSACTION_isRotationFrozen = 77;
        static final int TRANSACTION_isSafeModeEnabled = 83;
        static final int TRANSACTION_isViewServerRunning = 3;
        static final int TRANSACTION_keyguardGoingAway = 57;
        static final int TRANSACTION_lockNow = 82;
        static final int TRANSACTION_notifyAppStopped = 40;
        static final int TRANSACTION_openSession = 4;
        static final int TRANSACTION_overridePendingAppTransition = 28;
        static final int TRANSACTION_overridePendingAppTransitionAspectScaledThumb = 33;
        static final int TRANSACTION_overridePendingAppTransitionClipReveal = 31;
        static final int TRANSACTION_overridePendingAppTransitionInPlace = 35;
        static final int TRANSACTION_overridePendingAppTransitionMultiThumb = 34;
        static final int TRANSACTION_overridePendingAppTransitionMultiThumbFuture = 36;
        static final int TRANSACTION_overridePendingAppTransitionScaleUp = 30;
        static final int TRANSACTION_overridePendingAppTransitionThumb = 32;
        static final int TRANSACTION_pauseKeyDispatching = 16;
        static final int TRANSACTION_prepareAppTransition = 26;
        static final int TRANSACTION_reenableKeyguard = 51;
        static final int TRANSACTION_registerDockedStackListener = 90;
        static final int TRANSACTION_registerShortcutKey = 94;
        static final int TRANSACTION_removeAppToken = 43;
        static final int TRANSACTION_removeRotationWatcher = 73;
        static final int TRANSACTION_removeWallpaperInputConsumer = 100;
        static final int TRANSACTION_removeWindowToken = 20;
        static final int TRANSACTION_requestAppKeyboardShortcuts = 92;
        static final int TRANSACTION_requestAssistScreenshot = 78;
        static final int TRANSACTION_resumeKeyDispatching = 17;
        static final int TRANSACTION_screenshotApplications = 79;
        static final int TRANSACTION_setAnimationScale = 61;
        static final int TRANSACTION_setAnimationScales = 62;
        static final int TRANSACTION_setAppOrientation = 23;
        static final int TRANSACTION_setAppStartingWindow = 38;
        static final int TRANSACTION_setAppTask = 22;
        static final int TRANSACTION_setAppVisibility = 39;
        static final int TRANSACTION_setDockedStackDividerTouchRegion = 89;
        static final int TRANSACTION_setDockedStackResizing = 88;
        static final int TRANSACTION_setEventDispatching = 18;
        static final int TRANSACTION_setExitPosition = 29;
        static final int TRANSACTION_setFocusedApp = 25;
        static final int TRANSACTION_setForcedDisplayDensity = 12;
        static final int TRANSACTION_setForcedDisplayDensityAndSize = 98;
        static final int TRANSACTION_setForcedDisplayScalingMode = 14;
        static final int TRANSACTION_setForcedDisplaySize = 8;
        static final int TRANSACTION_setInTouchMode = 64;
        static final int TRANSACTION_setNewConfiguration = 46;
        static final int TRANSACTION_setOverscan = 15;
        static final int TRANSACTION_setResizeDimLayer = 91;
        static final int TRANSACTION_setScreenCaptureDisabled = 67;
        static final int TRANSACTION_setStrictModeVisualIndicatorPreference = 66;
        static final int TRANSACTION_showStrictModeViolation = 65;
        static final int TRANSACTION_startAppFreezingScreen = 41;
        static final int TRANSACTION_startFreezingScreen = 48;
        static final int TRANSACTION_startViewServer = 1;
        static final int TRANSACTION_statusBarVisibilityChanged = 80;
        static final int TRANSACTION_stopAppFreezingScreen = 42;
        static final int TRANSACTION_stopFreezingScreen = 49;
        static final int TRANSACTION_stopViewServer = 2;
        static final int TRANSACTION_thawRotation = 76;
        static final int TRANSACTION_updateOrientationFromAppTokens = 45;
        static final int TRANSACTION_updateRotation = 70;
        static final int TRANSACTION_watchRotation = 72;

        private static class Proxy implements IWindowManager {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public boolean startViewServer(int port) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(port);
                    this.mRemote.transact(Stub.TRANSACTION_startViewServer, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean stopViewServer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_stopViewServer, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isViewServerRunning() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isViewServerRunning, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IWindowSession openSession(IWindowSessionCallback callback, IInputMethodClient client, IInputContext inputContext) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    IBinder asBinder;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        asBinder = callback.asBinder();
                    } else {
                        asBinder = null;
                    }
                    _data.writeStrongBinder(asBinder);
                    if (client != null) {
                        asBinder = client.asBinder();
                    } else {
                        asBinder = null;
                    }
                    _data.writeStrongBinder(asBinder);
                    if (inputContext != null) {
                        iBinder = inputContext.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_openSession, _data, _reply, 0);
                    _reply.readException();
                    IWindowSession _result = android.view.IWindowSession.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean inputMethodClientHasFocus(IInputMethodClient client) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_inputMethodClientHasFocus, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getInitialDisplaySize(int displayId, Point size) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    this.mRemote.transact(Stub.TRANSACTION_getInitialDisplaySize, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        size.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getBaseDisplaySize(int displayId, Point size) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    this.mRemote.transact(Stub.TRANSACTION_getBaseDisplaySize, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        size.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setForcedDisplaySize(int displayId, int width, int height) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    this.mRemote.transact(Stub.TRANSACTION_setForcedDisplaySize, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearForcedDisplaySize(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    this.mRemote.transact(Stub.TRANSACTION_clearForcedDisplaySize, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getInitialDisplayDensity(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    this.mRemote.transact(Stub.TRANSACTION_getInitialDisplayDensity, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getBaseDisplayDensity(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    this.mRemote.transact(Stub.TRANSACTION_getBaseDisplayDensity, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setForcedDisplayDensity(int displayId, int density) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(density);
                    this.mRemote.transact(Stub.TRANSACTION_setForcedDisplayDensity, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearForcedDisplayDensity(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    this.mRemote.transact(Stub.TRANSACTION_clearForcedDisplayDensity, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setForcedDisplayScalingMode(int displayId, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(mode);
                    this.mRemote.transact(Stub.TRANSACTION_setForcedDisplayScalingMode, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setOverscan(int displayId, int left, int top, int right, int bottom) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(left);
                    _data.writeInt(top);
                    _data.writeInt(right);
                    _data.writeInt(bottom);
                    this.mRemote.transact(Stub.TRANSACTION_setOverscan, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void pauseKeyDispatching(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_pauseKeyDispatching, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resumeKeyDispatching(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_resumeKeyDispatching, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setEventDispatching(boolean enabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enabled) {
                        i = Stub.TRANSACTION_startViewServer;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setEventDispatching, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addWindowToken(IBinder token, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(type);
                    this.mRemote.transact(Stub.TRANSACTION_addWindowToken, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeWindowToken(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_removeWindowToken, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addAppToken(int addPos, IApplicationToken token, int taskId, int stackId, int requestedOrientation, boolean fullscreen, boolean showWhenLocked, int userId, int configChanges, boolean voiceInteraction, boolean launchTaskBehind, Rect taskBounds, Configuration configuration, int taskResizeMode, boolean alwaysFocusable, boolean homeTask, int targetSdkVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(addPos);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeInt(taskId);
                    _data.writeInt(stackId);
                    _data.writeInt(requestedOrientation);
                    _data.writeInt(fullscreen ? Stub.TRANSACTION_startViewServer : 0);
                    _data.writeInt(showWhenLocked ? Stub.TRANSACTION_startViewServer : 0);
                    _data.writeInt(userId);
                    _data.writeInt(configChanges);
                    _data.writeInt(voiceInteraction ? Stub.TRANSACTION_startViewServer : 0);
                    _data.writeInt(launchTaskBehind ? Stub.TRANSACTION_startViewServer : 0);
                    if (taskBounds != null) {
                        _data.writeInt(Stub.TRANSACTION_startViewServer);
                        taskBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (configuration != null) {
                        _data.writeInt(Stub.TRANSACTION_startViewServer);
                        configuration.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(taskResizeMode);
                    _data.writeInt(alwaysFocusable ? Stub.TRANSACTION_startViewServer : 0);
                    _data.writeInt(homeTask ? Stub.TRANSACTION_startViewServer : 0);
                    _data.writeInt(targetSdkVersion);
                    this.mRemote.transact(Stub.TRANSACTION_addAppToken, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAppTask(IBinder token, int taskId, int stackId, Rect taskBounds, Configuration config, int taskResizeMode, boolean homeTask) throws RemoteException {
                int i = Stub.TRANSACTION_startViewServer;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(taskId);
                    _data.writeInt(stackId);
                    if (taskBounds != null) {
                        _data.writeInt(Stub.TRANSACTION_startViewServer);
                        taskBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (config != null) {
                        _data.writeInt(Stub.TRANSACTION_startViewServer);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(taskResizeMode);
                    if (!homeTask) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setAppTask, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAppOrientation(IApplicationToken token, int requestedOrientation) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(requestedOrientation);
                    this.mRemote.transact(Stub.TRANSACTION_setAppOrientation, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAppOrientation(IApplicationToken token) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_getAppOrientation, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setFocusedApp(IBinder token, boolean moveFocusNow) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (moveFocusNow) {
                        i = Stub.TRANSACTION_startViewServer;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setFocusedApp, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void prepareAppTransition(int transit, boolean alwaysKeepCurrent) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(transit);
                    if (alwaysKeepCurrent) {
                        i = Stub.TRANSACTION_startViewServer;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_prepareAppTransition, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPendingAppTransition() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPendingAppTransition, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void overridePendingAppTransition(String packageName, int enterAnim, int exitAnim, IRemoteCallback startedCallback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(enterAnim);
                    _data.writeInt(exitAnim);
                    if (startedCallback != null) {
                        iBinder = startedCallback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_overridePendingAppTransition, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setExitPosition(int startX, int startY, int width, int height) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(startX);
                    _data.writeInt(startY);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    this.mRemote.transact(Stub.TRANSACTION_setExitPosition, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void overridePendingAppTransitionScaleUp(int startX, int startY, int startWidth, int startHeight) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(startX);
                    _data.writeInt(startY);
                    _data.writeInt(startWidth);
                    _data.writeInt(startHeight);
                    this.mRemote.transact(Stub.TRANSACTION_overridePendingAppTransitionScaleUp, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void overridePendingAppTransitionClipReveal(int startX, int startY, int startWidth, int startHeight) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(startX);
                    _data.writeInt(startY);
                    _data.writeInt(startWidth);
                    _data.writeInt(startHeight);
                    this.mRemote.transact(Stub.TRANSACTION_overridePendingAppTransitionClipReveal, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void overridePendingAppTransitionThumb(Bitmap srcThumb, int startX, int startY, IRemoteCallback startedCallback, boolean scaleUp) throws RemoteException {
                int i = Stub.TRANSACTION_startViewServer;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (srcThumb != null) {
                        _data.writeInt(Stub.TRANSACTION_startViewServer);
                        srcThumb.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(startX);
                    _data.writeInt(startY);
                    if (startedCallback != null) {
                        iBinder = startedCallback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (!scaleUp) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_overridePendingAppTransitionThumb, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void overridePendingAppTransitionAspectScaledThumb(Bitmap srcThumb, int startX, int startY, int targetWidth, int targetHeight, IRemoteCallback startedCallback, boolean scaleUp) throws RemoteException {
                int i = Stub.TRANSACTION_startViewServer;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (srcThumb != null) {
                        _data.writeInt(Stub.TRANSACTION_startViewServer);
                        srcThumb.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(startX);
                    _data.writeInt(startY);
                    _data.writeInt(targetWidth);
                    _data.writeInt(targetHeight);
                    if (startedCallback != null) {
                        iBinder = startedCallback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (!scaleUp) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_overridePendingAppTransitionAspectScaledThumb, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void overridePendingAppTransitionMultiThumb(AppTransitionAnimationSpec[] specs, IRemoteCallback startedCallback, IRemoteCallback finishedCallback, boolean scaleUp) throws RemoteException {
                int i = 0;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    IBinder asBinder;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(specs, 0);
                    if (startedCallback != null) {
                        asBinder = startedCallback.asBinder();
                    } else {
                        asBinder = null;
                    }
                    _data.writeStrongBinder(asBinder);
                    if (finishedCallback != null) {
                        iBinder = finishedCallback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (scaleUp) {
                        i = Stub.TRANSACTION_startViewServer;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_overridePendingAppTransitionMultiThumb, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void overridePendingAppTransitionInPlace(String packageName, int anim) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(anim);
                    this.mRemote.transact(Stub.TRANSACTION_overridePendingAppTransitionInPlace, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void overridePendingAppTransitionMultiThumbFuture(IAppTransitionAnimationSpecsFuture specsFuture, IRemoteCallback startedCallback, boolean scaleUp) throws RemoteException {
                int i = 0;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    IBinder asBinder;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (specsFuture != null) {
                        asBinder = specsFuture.asBinder();
                    } else {
                        asBinder = null;
                    }
                    _data.writeStrongBinder(asBinder);
                    if (startedCallback != null) {
                        iBinder = startedCallback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (scaleUp) {
                        i = Stub.TRANSACTION_startViewServer;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_overridePendingAppTransitionMultiThumbFuture, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void executeAppTransition() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_executeAppTransition, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setAppStartingWindow(IBinder token, String pkg, int theme, CompatibilityInfo compatInfo, CharSequence nonLocalizedLabel, int labelRes, int icon, int logo, int windowFlags, IBinder transferFrom, boolean createIfNeeded) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeString(pkg);
                    _data.writeInt(theme);
                    if (compatInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_startViewServer);
                        compatInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (nonLocalizedLabel != null) {
                        _data.writeInt(Stub.TRANSACTION_startViewServer);
                        TextUtils.writeToParcel(nonLocalizedLabel, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(labelRes);
                    _data.writeInt(icon);
                    _data.writeInt(logo);
                    _data.writeInt(windowFlags);
                    _data.writeStrongBinder(transferFrom);
                    _data.writeInt(createIfNeeded ? Stub.TRANSACTION_startViewServer : 0);
                    this.mRemote.transact(Stub.TRANSACTION_setAppStartingWindow, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAppVisibility(IBinder token, boolean visible) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (visible) {
                        i = Stub.TRANSACTION_startViewServer;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setAppVisibility, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyAppStopped(IBinder token, boolean stopped) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (stopped) {
                        i = Stub.TRANSACTION_startViewServer;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_notifyAppStopped, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startAppFreezingScreen(IBinder token, int configChanges) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(configChanges);
                    this.mRemote.transact(Stub.TRANSACTION_startAppFreezingScreen, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopAppFreezingScreen(IBinder token, boolean force) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (force) {
                        i = Stub.TRANSACTION_startViewServer;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_stopAppFreezingScreen, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeAppToken(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_removeAppToken, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void endProlongedAnimations() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_endProlongedAnimations, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Configuration updateOrientationFromAppTokens(Configuration currentConfig, IBinder freezeThisOneIfNeeded) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Configuration configuration;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (currentConfig != null) {
                        _data.writeInt(Stub.TRANSACTION_startViewServer);
                        currentConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(freezeThisOneIfNeeded);
                    this.mRemote.transact(Stub.TRANSACTION_updateOrientationFromAppTokens, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        configuration = (Configuration) Configuration.CREATOR.createFromParcel(_reply);
                    } else {
                        configuration = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return configuration;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] setNewConfiguration(Configuration config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (config != null) {
                        _data.writeInt(Stub.TRANSACTION_startViewServer);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setNewConfiguration, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Rect getBoundsForNewConfiguration(int stackId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Rect rect;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stackId);
                    this.mRemote.transact(Stub.TRANSACTION_getBoundsForNewConfiguration, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        rect = (Rect) Rect.CREATOR.createFromParcel(_reply);
                    } else {
                        rect = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return rect;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startFreezingScreen(int exitAnim, int enterAnim) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(exitAnim);
                    _data.writeInt(enterAnim);
                    this.mRemote.transact(Stub.TRANSACTION_startFreezingScreen, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopFreezingScreen() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_stopFreezingScreen, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disableKeyguard(IBinder token, String tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeString(tag);
                    this.mRemote.transact(Stub.TRANSACTION_disableKeyguard, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reenableKeyguard(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_reenableKeyguard, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void exitKeyguardSecurely(IOnKeyguardExitResult callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_exitKeyguardSecurely, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isKeyguardLocked() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isKeyguardLocked, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isKeyguardSecure() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isKeyguardSecure, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean inKeyguardRestrictedInputMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_inKeyguardRestrictedInputMode, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void dismissKeyguard() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_dismissKeyguard, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void keyguardGoingAway(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_keyguardGoingAway, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void closeSystemDialogs(String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reason);
                    this.mRemote.transact(Stub.TRANSACTION_closeSystemDialogs, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public float getAnimationScale(int which) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(which);
                    this.mRemote.transact(Stub.TRANSACTION_getAnimationScale, _data, _reply, 0);
                    _reply.readException();
                    float _result = _reply.readFloat();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public float[] getAnimationScales() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAnimationScales, _data, _reply, 0);
                    _reply.readException();
                    float[] _result = _reply.createFloatArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAnimationScale(int which, float scale) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(which);
                    _data.writeFloat(scale);
                    this.mRemote.transact(Stub.TRANSACTION_setAnimationScale, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAnimationScales(float[] scales) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloatArray(scales);
                    this.mRemote.transact(Stub.TRANSACTION_setAnimationScales, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public float getCurrentAnimatorScale() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCurrentAnimatorScale, _data, _reply, 0);
                    _reply.readException();
                    float _result = _reply.readFloat();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setInTouchMode(boolean showFocus) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (showFocus) {
                        i = Stub.TRANSACTION_startViewServer;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setInTouchMode, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void showStrictModeViolation(boolean on) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (on) {
                        i = Stub.TRANSACTION_startViewServer;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_showStrictModeViolation, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setStrictModeVisualIndicatorPreference(String enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(enabled);
                    this.mRemote.transact(Stub.TRANSACTION_setStrictModeVisualIndicatorPreference, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setScreenCaptureDisabled(int userId, boolean disabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (disabled) {
                        i = Stub.TRANSACTION_startViewServer;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setScreenCaptureDisabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelTaskWindowTransition(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    this.mRemote.transact(Stub.TRANSACTION_cancelTaskWindowTransition, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelTaskThumbnailTransition(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    this.mRemote.transact(Stub.TRANSACTION_cancelTaskThumbnailTransition, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateRotation(boolean alwaysSendConfiguration, boolean forceRelayout) throws RemoteException {
                int i = Stub.TRANSACTION_startViewServer;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (alwaysSendConfiguration) {
                        i2 = Stub.TRANSACTION_startViewServer;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!forceRelayout) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_updateRotation, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRotation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getRotation, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int watchRotation(IRotationWatcher watcher) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (watcher != null) {
                        iBinder = watcher.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_watchRotation, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeRotationWatcher(IRotationWatcher watcher) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (watcher != null) {
                        iBinder = watcher.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_removeRotationWatcher, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPreferredOptionsPanelGravity() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPreferredOptionsPanelGravity, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void freezeRotation(int rotation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rotation);
                    this.mRemote.transact(Stub.TRANSACTION_freezeRotation, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void thawRotation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_thawRotation, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isRotationFrozen() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isRotationFrozen, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean requestAssistScreenshot(IAssistScreenshotReceiver receiver) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (receiver != null) {
                        iBinder = receiver.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_requestAssistScreenshot, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bitmap screenshotApplications(IBinder appToken, int displayId, int maxWidth, int maxHeight, float frameScale) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bitmap bitmap;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(appToken);
                    _data.writeInt(displayId);
                    _data.writeInt(maxWidth);
                    _data.writeInt(maxHeight);
                    _data.writeFloat(frameScale);
                    this.mRemote.transact(Stub.TRANSACTION_screenshotApplications, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bitmap = (Bitmap) Bitmap.CREATOR.createFromParcel(_reply);
                    } else {
                        bitmap = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bitmap;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void statusBarVisibilityChanged(int visibility) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(visibility);
                    this.mRemote.transact(Stub.TRANSACTION_statusBarVisibilityChanged, _data, null, Stub.TRANSACTION_startViewServer);
                } finally {
                    _data.recycle();
                }
            }

            public boolean hasNavigationBar() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_hasNavigationBar, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void lockNow(Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (options != null) {
                        _data.writeInt(Stub.TRANSACTION_startViewServer);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_lockNow, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSafeModeEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isSafeModeEnabled, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableScreenIfNeeded() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_enableScreenIfNeeded, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean clearWindowContentFrameStats(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_clearWindowContentFrameStats, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public WindowContentFrameStats getWindowContentFrameStats(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    WindowContentFrameStats windowContentFrameStats;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_getWindowContentFrameStats, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        windowContentFrameStats = (WindowContentFrameStats) WindowContentFrameStats.CREATOR.createFromParcel(_reply);
                    } else {
                        windowContentFrameStats = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return windowContentFrameStats;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDockedStackSide() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getDockedStackSide, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDockedStackResizing(boolean resizing) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (resizing) {
                        i = Stub.TRANSACTION_startViewServer;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setDockedStackResizing, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDockedStackDividerTouchRegion(Rect touchableRegion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (touchableRegion != null) {
                        _data.writeInt(Stub.TRANSACTION_startViewServer);
                        touchableRegion.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setDockedStackDividerTouchRegion, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerDockedStackListener(IDockedStackListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerDockedStackListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setResizeDimLayer(boolean visible, int targetStackId, float alpha) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (visible) {
                        i = Stub.TRANSACTION_startViewServer;
                    }
                    _data.writeInt(i);
                    _data.writeInt(targetStackId);
                    _data.writeFloat(alpha);
                    this.mRemote.transact(Stub.TRANSACTION_setResizeDimLayer, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestAppKeyboardShortcuts(IResultReceiver receiver, int deviceId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (receiver != null) {
                        iBinder = receiver.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(deviceId);
                    this.mRemote.transact(Stub.TRANSACTION_requestAppKeyboardShortcuts, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getStableInsets(Rect outInsets) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getStableInsets, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        outInsets.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerShortcutKey(long shortcutCode, IShortcutService keySubscriber) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(shortcutCode);
                    if (keySubscriber != null) {
                        iBinder = keySubscriber.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerShortcutKey, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getNsdWindowInfo(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_getNsdWindowInfo, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getNsdWindowTitle(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_getNsdWindowTitle, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getAccelPackages(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_getAccelPackages, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setForcedDisplayDensityAndSize(int displayId, int density, int width, int height) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(density);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    this.mRemote.transact(Stub.TRANSACTION_setForcedDisplayDensityAndSize, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void createWallpaperInputConsumer(InputChannel inputChannel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_createWallpaperInputConsumer, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        inputChannel.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeWallpaperInputConsumer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_removeWallpaperInputConsumer, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWindowManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWindowManager)) {
                return new Proxy(obj);
            }
            return (IWindowManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            int _arg0;
            Point _arg1;
            int _result2;
            int _arg2;
            IBinder _arg02;
            Bitmap bitmap;
            Configuration configuration;
            float _result3;
            Rect rect;
            switch (code) {
                case TRANSACTION_startViewServer /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = startViewServer(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_startViewServer : 0);
                    return true;
                case TRANSACTION_stopViewServer /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = stopViewServer();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_startViewServer : 0);
                    return true;
                case TRANSACTION_isViewServerRunning /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isViewServerRunning();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_startViewServer : 0);
                    return true;
                case TRANSACTION_openSession /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    IWindowSession _result4 = openSession(android.view.IWindowSessionCallback.Stub.asInterface(data.readStrongBinder()), com.android.internal.view.IInputMethodClient.Stub.asInterface(data.readStrongBinder()), com.android.internal.view.IInputContext.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeStrongBinder(_result4 != null ? _result4.asBinder() : null);
                    return true;
                case TRANSACTION_inputMethodClientHasFocus /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = inputMethodClientHasFocus(com.android.internal.view.IInputMethodClient.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_startViewServer : 0);
                    return true;
                case TRANSACTION_getInitialDisplaySize /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = new Point();
                    getInitialDisplaySize(_arg0, _arg1);
                    reply.writeNoException();
                    if (_arg1 != null) {
                        reply.writeInt(TRANSACTION_startViewServer);
                        _arg1.writeToParcel(reply, TRANSACTION_startViewServer);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getBaseDisplaySize /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = new Point();
                    getBaseDisplaySize(_arg0, _arg1);
                    reply.writeNoException();
                    if (_arg1 != null) {
                        reply.writeInt(TRANSACTION_startViewServer);
                        _arg1.writeToParcel(reply, TRANSACTION_startViewServer);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setForcedDisplaySize /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    setForcedDisplaySize(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearForcedDisplaySize /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearForcedDisplaySize(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getInitialDisplayDensity /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getInitialDisplayDensity(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getBaseDisplayDensity /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getBaseDisplayDensity(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setForcedDisplayDensity /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    setForcedDisplayDensity(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearForcedDisplayDensity /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearForcedDisplayDensity(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setForcedDisplayScalingMode /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    setForcedDisplayScalingMode(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setOverscan /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    setOverscan(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_pauseKeyDispatching /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    pauseKeyDispatching(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_resumeKeyDispatching /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    resumeKeyDispatching(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setEventDispatching /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    setEventDispatching(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addWindowToken /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    addWindowToken(data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeWindowToken /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeWindowToken(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addAppToken /*21*/:
                    Rect rect2;
                    Configuration configuration2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    IApplicationToken _arg12 = android.view.IApplicationToken.Stub.asInterface(data.readStrongBinder());
                    _arg2 = data.readInt();
                    int _arg3 = data.readInt();
                    int _arg4 = data.readInt();
                    boolean _arg5 = data.readInt() != 0;
                    boolean _arg6 = data.readInt() != 0;
                    int _arg7 = data.readInt();
                    int _arg8 = data.readInt();
                    boolean _arg9 = data.readInt() != 0;
                    boolean _arg10 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        rect2 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect2 = null;
                    }
                    if (data.readInt() != 0) {
                        configuration2 = (Configuration) Configuration.CREATOR.createFromParcel(data);
                    } else {
                        configuration2 = null;
                    }
                    addAppToken(_arg0, _arg12, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8, _arg9, _arg10, rect2, configuration2, data.readInt(), data.readInt() != 0, data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setAppTask /*22*/:
                    Rect rect3;
                    Configuration configuration3;
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readStrongBinder();
                    int _arg13 = data.readInt();
                    _arg2 = data.readInt();
                    if (data.readInt() != 0) {
                        rect3 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect3 = null;
                    }
                    if (data.readInt() != 0) {
                        configuration3 = (Configuration) Configuration.CREATOR.createFromParcel(data);
                    } else {
                        configuration3 = null;
                    }
                    setAppTask(_arg02, _arg13, _arg2, rect3, configuration3, data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setAppOrientation /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    setAppOrientation(android.view.IApplicationToken.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getAppOrientation /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAppOrientation(android.view.IApplicationToken.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setFocusedApp /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    setFocusedApp(data.readStrongBinder(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_prepareAppTransition /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    prepareAppTransition(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getPendingAppTransition /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPendingAppTransition();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_overridePendingAppTransition /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    overridePendingAppTransition(data.readString(), data.readInt(), data.readInt(), android.os.IRemoteCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setExitPosition /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    setExitPosition(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_overridePendingAppTransitionScaleUp /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    overridePendingAppTransitionScaleUp(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_overridePendingAppTransitionClipReveal /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    overridePendingAppTransitionClipReveal(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_overridePendingAppTransitionThumb /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bitmap = (Bitmap) Bitmap.CREATOR.createFromParcel(data);
                    } else {
                        bitmap = null;
                    }
                    overridePendingAppTransitionThumb(bitmap, data.readInt(), data.readInt(), android.os.IRemoteCallback.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_overridePendingAppTransitionAspectScaledThumb /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bitmap = (Bitmap) Bitmap.CREATOR.createFromParcel(data);
                    } else {
                        bitmap = null;
                    }
                    overridePendingAppTransitionAspectScaledThumb(bitmap, data.readInt(), data.readInt(), data.readInt(), data.readInt(), android.os.IRemoteCallback.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_overridePendingAppTransitionMultiThumb /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    overridePendingAppTransitionMultiThumb((AppTransitionAnimationSpec[]) data.createTypedArray(AppTransitionAnimationSpec.CREATOR), android.os.IRemoteCallback.Stub.asInterface(data.readStrongBinder()), android.os.IRemoteCallback.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_overridePendingAppTransitionInPlace /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    overridePendingAppTransitionInPlace(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_overridePendingAppTransitionMultiThumbFuture /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    overridePendingAppTransitionMultiThumbFuture(android.view.IAppTransitionAnimationSpecsFuture.Stub.asInterface(data.readStrongBinder()), android.os.IRemoteCallback.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_executeAppTransition /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    executeAppTransition();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setAppStartingWindow /*38*/:
                    CompatibilityInfo compatibilityInfo;
                    CharSequence charSequence;
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readStrongBinder();
                    String _arg14 = data.readString();
                    _arg2 = data.readInt();
                    if (data.readInt() != 0) {
                        compatibilityInfo = (CompatibilityInfo) CompatibilityInfo.CREATOR.createFromParcel(data);
                    } else {
                        compatibilityInfo = null;
                    }
                    if (data.readInt() != 0) {
                        charSequence = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                    } else {
                        charSequence = null;
                    }
                    _result = setAppStartingWindow(_arg02, _arg14, _arg2, compatibilityInfo, charSequence, data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readStrongBinder(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_startViewServer : 0);
                    return true;
                case TRANSACTION_setAppVisibility /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    setAppVisibility(data.readStrongBinder(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_notifyAppStopped /*40*/:
                    data.enforceInterface(DESCRIPTOR);
                    notifyAppStopped(data.readStrongBinder(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startAppFreezingScreen /*41*/:
                    data.enforceInterface(DESCRIPTOR);
                    startAppFreezingScreen(data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopAppFreezingScreen /*42*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopAppFreezingScreen(data.readStrongBinder(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeAppToken /*43*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeAppToken(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_endProlongedAnimations /*44*/:
                    data.enforceInterface(DESCRIPTOR);
                    endProlongedAnimations();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_updateOrientationFromAppTokens /*45*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        configuration = (Configuration) Configuration.CREATOR.createFromParcel(data);
                    } else {
                        configuration = null;
                    }
                    Configuration _result5 = updateOrientationFromAppTokens(configuration, data.readStrongBinder());
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(TRANSACTION_startViewServer);
                        _result5.writeToParcel(reply, TRANSACTION_startViewServer);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setNewConfiguration /*46*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        configuration = (Configuration) Configuration.CREATOR.createFromParcel(data);
                    } else {
                        configuration = null;
                    }
                    int[] _result6 = setNewConfiguration(configuration);
                    reply.writeNoException();
                    reply.writeIntArray(_result6);
                    return true;
                case TRANSACTION_getBoundsForNewConfiguration /*47*/:
                    data.enforceInterface(DESCRIPTOR);
                    Rect _result7 = getBoundsForNewConfiguration(data.readInt());
                    reply.writeNoException();
                    if (_result7 != null) {
                        reply.writeInt(TRANSACTION_startViewServer);
                        _result7.writeToParcel(reply, TRANSACTION_startViewServer);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_startFreezingScreen /*48*/:
                    data.enforceInterface(DESCRIPTOR);
                    startFreezingScreen(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopFreezingScreen /*49*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopFreezingScreen();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_disableKeyguard /*50*/:
                    data.enforceInterface(DESCRIPTOR);
                    disableKeyguard(data.readStrongBinder(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_reenableKeyguard /*51*/:
                    data.enforceInterface(DESCRIPTOR);
                    reenableKeyguard(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_exitKeyguardSecurely /*52*/:
                    data.enforceInterface(DESCRIPTOR);
                    exitKeyguardSecurely(android.view.IOnKeyguardExitResult.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isKeyguardLocked /*53*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isKeyguardLocked();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_startViewServer : 0);
                    return true;
                case TRANSACTION_isKeyguardSecure /*54*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isKeyguardSecure();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_startViewServer : 0);
                    return true;
                case TRANSACTION_inKeyguardRestrictedInputMode /*55*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = inKeyguardRestrictedInputMode();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_startViewServer : 0);
                    return true;
                case TRANSACTION_dismissKeyguard /*56*/:
                    data.enforceInterface(DESCRIPTOR);
                    dismissKeyguard();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_keyguardGoingAway /*57*/:
                    data.enforceInterface(DESCRIPTOR);
                    keyguardGoingAway(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_closeSystemDialogs /*58*/:
                    data.enforceInterface(DESCRIPTOR);
                    closeSystemDialogs(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getAnimationScale /*59*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getAnimationScale(data.readInt());
                    reply.writeNoException();
                    reply.writeFloat(_result3);
                    return true;
                case TRANSACTION_getAnimationScales /*60*/:
                    data.enforceInterface(DESCRIPTOR);
                    float[] _result8 = getAnimationScales();
                    reply.writeNoException();
                    reply.writeFloatArray(_result8);
                    return true;
                case TRANSACTION_setAnimationScale /*61*/:
                    data.enforceInterface(DESCRIPTOR);
                    setAnimationScale(data.readInt(), data.readFloat());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setAnimationScales /*62*/:
                    data.enforceInterface(DESCRIPTOR);
                    setAnimationScales(data.createFloatArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getCurrentAnimatorScale /*63*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getCurrentAnimatorScale();
                    reply.writeNoException();
                    reply.writeFloat(_result3);
                    return true;
                case TRANSACTION_setInTouchMode /*64*/:
                    data.enforceInterface(DESCRIPTOR);
                    setInTouchMode(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_showStrictModeViolation /*65*/:
                    data.enforceInterface(DESCRIPTOR);
                    showStrictModeViolation(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setStrictModeVisualIndicatorPreference /*66*/:
                    data.enforceInterface(DESCRIPTOR);
                    setStrictModeVisualIndicatorPreference(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setScreenCaptureDisabled /*67*/:
                    data.enforceInterface(DESCRIPTOR);
                    setScreenCaptureDisabled(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cancelTaskWindowTransition /*68*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancelTaskWindowTransition(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cancelTaskThumbnailTransition /*69*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancelTaskThumbnailTransition(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_updateRotation /*70*/:
                    data.enforceInterface(DESCRIPTOR);
                    updateRotation(data.readInt() != 0, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getRotation /*71*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getRotation();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_watchRotation /*72*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = watchRotation(android.view.IRotationWatcher.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_removeRotationWatcher /*73*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeRotationWatcher(android.view.IRotationWatcher.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getPreferredOptionsPanelGravity /*74*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPreferredOptionsPanelGravity();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_freezeRotation /*75*/:
                    data.enforceInterface(DESCRIPTOR);
                    freezeRotation(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_thawRotation /*76*/:
                    data.enforceInterface(DESCRIPTOR);
                    thawRotation();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isRotationFrozen /*77*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isRotationFrozen();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_startViewServer : 0);
                    return true;
                case TRANSACTION_requestAssistScreenshot /*78*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = requestAssistScreenshot(com.android.internal.app.IAssistScreenshotReceiver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_startViewServer : 0);
                    return true;
                case TRANSACTION_screenshotApplications /*79*/:
                    data.enforceInterface(DESCRIPTOR);
                    Bitmap _result9 = screenshotApplications(data.readStrongBinder(), data.readInt(), data.readInt(), data.readInt(), data.readFloat());
                    reply.writeNoException();
                    if (_result9 != null) {
                        reply.writeInt(TRANSACTION_startViewServer);
                        _result9.writeToParcel(reply, TRANSACTION_startViewServer);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_statusBarVisibilityChanged /*80*/:
                    data.enforceInterface(DESCRIPTOR);
                    statusBarVisibilityChanged(data.readInt());
                    return true;
                case TRANSACTION_hasNavigationBar /*81*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = hasNavigationBar();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_startViewServer : 0);
                    return true;
                case TRANSACTION_lockNow /*82*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    lockNow(bundle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isSafeModeEnabled /*83*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isSafeModeEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_startViewServer : 0);
                    return true;
                case TRANSACTION_enableScreenIfNeeded /*84*/:
                    data.enforceInterface(DESCRIPTOR);
                    enableScreenIfNeeded();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearWindowContentFrameStats /*85*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = clearWindowContentFrameStats(data.readStrongBinder());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_startViewServer : 0);
                    return true;
                case TRANSACTION_getWindowContentFrameStats /*86*/:
                    data.enforceInterface(DESCRIPTOR);
                    WindowContentFrameStats _result10 = getWindowContentFrameStats(data.readStrongBinder());
                    reply.writeNoException();
                    if (_result10 != null) {
                        reply.writeInt(TRANSACTION_startViewServer);
                        _result10.writeToParcel(reply, TRANSACTION_startViewServer);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getDockedStackSide /*87*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDockedStackSide();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setDockedStackResizing /*88*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDockedStackResizing(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setDockedStackDividerTouchRegion /*89*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        rect = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect = null;
                    }
                    setDockedStackDividerTouchRegion(rect);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registerDockedStackListener /*90*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerDockedStackListener(android.view.IDockedStackListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setResizeDimLayer /*91*/:
                    data.enforceInterface(DESCRIPTOR);
                    setResizeDimLayer(data.readInt() != 0, data.readInt(), data.readFloat());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_requestAppKeyboardShortcuts /*92*/:
                    data.enforceInterface(DESCRIPTOR);
                    requestAppKeyboardShortcuts(com.android.internal.os.IResultReceiver.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getStableInsets /*93*/:
                    data.enforceInterface(DESCRIPTOR);
                    rect = new Rect();
                    getStableInsets(rect);
                    reply.writeNoException();
                    if (rect != null) {
                        reply.writeInt(TRANSACTION_startViewServer);
                        rect.writeToParcel(reply, TRANSACTION_startViewServer);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_registerShortcutKey /*94*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerShortcutKey(data.readLong(), com.android.internal.policy.IShortcutService.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getNsdWindowInfo /*95*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getNsdWindowInfo(data.readStrongBinder());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getNsdWindowTitle /*96*/:
                    data.enforceInterface(DESCRIPTOR);
                    String _result11 = getNsdWindowTitle(data.readStrongBinder());
                    reply.writeNoException();
                    reply.writeString(_result11);
                    return true;
                case TRANSACTION_getAccelPackages /*97*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAccelPackages(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_startViewServer : 0);
                    return true;
                case TRANSACTION_setForcedDisplayDensityAndSize /*98*/:
                    data.enforceInterface(DESCRIPTOR);
                    setForcedDisplayDensityAndSize(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_createWallpaperInputConsumer /*99*/:
                    data.enforceInterface(DESCRIPTOR);
                    InputChannel _arg03 = new InputChannel();
                    createWallpaperInputConsumer(_arg03);
                    reply.writeNoException();
                    if (_arg03 != null) {
                        reply.writeInt(TRANSACTION_startViewServer);
                        _arg03.writeToParcel(reply, TRANSACTION_startViewServer);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_removeWallpaperInputConsumer /*100*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeWallpaperInputConsumer();
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addAppToken(int i, IApplicationToken iApplicationToken, int i2, int i3, int i4, boolean z, boolean z2, int i5, int i6, boolean z3, boolean z4, Rect rect, Configuration configuration, int i7, boolean z5, boolean z6, int i8) throws RemoteException;

    void addWindowToken(IBinder iBinder, int i) throws RemoteException;

    void cancelTaskThumbnailTransition(int i) throws RemoteException;

    void cancelTaskWindowTransition(int i) throws RemoteException;

    void clearForcedDisplayDensity(int i) throws RemoteException;

    void clearForcedDisplaySize(int i) throws RemoteException;

    boolean clearWindowContentFrameStats(IBinder iBinder) throws RemoteException;

    void closeSystemDialogs(String str) throws RemoteException;

    void createWallpaperInputConsumer(InputChannel inputChannel) throws RemoteException;

    void disableKeyguard(IBinder iBinder, String str) throws RemoteException;

    void dismissKeyguard() throws RemoteException;

    void enableScreenIfNeeded() throws RemoteException;

    void endProlongedAnimations() throws RemoteException;

    void executeAppTransition() throws RemoteException;

    void exitKeyguardSecurely(IOnKeyguardExitResult iOnKeyguardExitResult) throws RemoteException;

    void freezeRotation(int i) throws RemoteException;

    boolean getAccelPackages(String str) throws RemoteException;

    float getAnimationScale(int i) throws RemoteException;

    float[] getAnimationScales() throws RemoteException;

    int getAppOrientation(IApplicationToken iApplicationToken) throws RemoteException;

    int getBaseDisplayDensity(int i) throws RemoteException;

    void getBaseDisplaySize(int i, Point point) throws RemoteException;

    Rect getBoundsForNewConfiguration(int i) throws RemoteException;

    float getCurrentAnimatorScale() throws RemoteException;

    int getDockedStackSide() throws RemoteException;

    int getInitialDisplayDensity(int i) throws RemoteException;

    void getInitialDisplaySize(int i, Point point) throws RemoteException;

    int getNsdWindowInfo(IBinder iBinder) throws RemoteException;

    String getNsdWindowTitle(IBinder iBinder) throws RemoteException;

    int getPendingAppTransition() throws RemoteException;

    int getPreferredOptionsPanelGravity() throws RemoteException;

    int getRotation() throws RemoteException;

    void getStableInsets(Rect rect) throws RemoteException;

    WindowContentFrameStats getWindowContentFrameStats(IBinder iBinder) throws RemoteException;

    boolean hasNavigationBar() throws RemoteException;

    boolean inKeyguardRestrictedInputMode() throws RemoteException;

    boolean inputMethodClientHasFocus(IInputMethodClient iInputMethodClient) throws RemoteException;

    boolean isKeyguardLocked() throws RemoteException;

    boolean isKeyguardSecure() throws RemoteException;

    boolean isRotationFrozen() throws RemoteException;

    boolean isSafeModeEnabled() throws RemoteException;

    boolean isViewServerRunning() throws RemoteException;

    void keyguardGoingAway(int i) throws RemoteException;

    void lockNow(Bundle bundle) throws RemoteException;

    void notifyAppStopped(IBinder iBinder, boolean z) throws RemoteException;

    IWindowSession openSession(IWindowSessionCallback iWindowSessionCallback, IInputMethodClient iInputMethodClient, IInputContext iInputContext) throws RemoteException;

    void overridePendingAppTransition(String str, int i, int i2, IRemoteCallback iRemoteCallback) throws RemoteException;

    void overridePendingAppTransitionAspectScaledThumb(Bitmap bitmap, int i, int i2, int i3, int i4, IRemoteCallback iRemoteCallback, boolean z) throws RemoteException;

    void overridePendingAppTransitionClipReveal(int i, int i2, int i3, int i4) throws RemoteException;

    void overridePendingAppTransitionInPlace(String str, int i) throws RemoteException;

    void overridePendingAppTransitionMultiThumb(AppTransitionAnimationSpec[] appTransitionAnimationSpecArr, IRemoteCallback iRemoteCallback, IRemoteCallback iRemoteCallback2, boolean z) throws RemoteException;

    void overridePendingAppTransitionMultiThumbFuture(IAppTransitionAnimationSpecsFuture iAppTransitionAnimationSpecsFuture, IRemoteCallback iRemoteCallback, boolean z) throws RemoteException;

    void overridePendingAppTransitionScaleUp(int i, int i2, int i3, int i4) throws RemoteException;

    void overridePendingAppTransitionThumb(Bitmap bitmap, int i, int i2, IRemoteCallback iRemoteCallback, boolean z) throws RemoteException;

    void pauseKeyDispatching(IBinder iBinder) throws RemoteException;

    void prepareAppTransition(int i, boolean z) throws RemoteException;

    void reenableKeyguard(IBinder iBinder) throws RemoteException;

    void registerDockedStackListener(IDockedStackListener iDockedStackListener) throws RemoteException;

    void registerShortcutKey(long j, IShortcutService iShortcutService) throws RemoteException;

    void removeAppToken(IBinder iBinder) throws RemoteException;

    void removeRotationWatcher(IRotationWatcher iRotationWatcher) throws RemoteException;

    void removeWallpaperInputConsumer() throws RemoteException;

    void removeWindowToken(IBinder iBinder) throws RemoteException;

    void requestAppKeyboardShortcuts(IResultReceiver iResultReceiver, int i) throws RemoteException;

    boolean requestAssistScreenshot(IAssistScreenshotReceiver iAssistScreenshotReceiver) throws RemoteException;

    void resumeKeyDispatching(IBinder iBinder) throws RemoteException;

    Bitmap screenshotApplications(IBinder iBinder, int i, int i2, int i3, float f) throws RemoteException;

    void setAnimationScale(int i, float f) throws RemoteException;

    void setAnimationScales(float[] fArr) throws RemoteException;

    void setAppOrientation(IApplicationToken iApplicationToken, int i) throws RemoteException;

    boolean setAppStartingWindow(IBinder iBinder, String str, int i, CompatibilityInfo compatibilityInfo, CharSequence charSequence, int i2, int i3, int i4, int i5, IBinder iBinder2, boolean z) throws RemoteException;

    void setAppTask(IBinder iBinder, int i, int i2, Rect rect, Configuration configuration, int i3, boolean z) throws RemoteException;

    void setAppVisibility(IBinder iBinder, boolean z) throws RemoteException;

    void setDockedStackDividerTouchRegion(Rect rect) throws RemoteException;

    void setDockedStackResizing(boolean z) throws RemoteException;

    void setEventDispatching(boolean z) throws RemoteException;

    void setExitPosition(int i, int i2, int i3, int i4) throws RemoteException;

    void setFocusedApp(IBinder iBinder, boolean z) throws RemoteException;

    void setForcedDisplayDensity(int i, int i2) throws RemoteException;

    void setForcedDisplayDensityAndSize(int i, int i2, int i3, int i4) throws RemoteException;

    void setForcedDisplayScalingMode(int i, int i2) throws RemoteException;

    void setForcedDisplaySize(int i, int i2, int i3) throws RemoteException;

    void setInTouchMode(boolean z) throws RemoteException;

    int[] setNewConfiguration(Configuration configuration) throws RemoteException;

    void setOverscan(int i, int i2, int i3, int i4, int i5) throws RemoteException;

    void setResizeDimLayer(boolean z, int i, float f) throws RemoteException;

    void setScreenCaptureDisabled(int i, boolean z) throws RemoteException;

    void setStrictModeVisualIndicatorPreference(String str) throws RemoteException;

    void showStrictModeViolation(boolean z) throws RemoteException;

    void startAppFreezingScreen(IBinder iBinder, int i) throws RemoteException;

    void startFreezingScreen(int i, int i2) throws RemoteException;

    boolean startViewServer(int i) throws RemoteException;

    void statusBarVisibilityChanged(int i) throws RemoteException;

    void stopAppFreezingScreen(IBinder iBinder, boolean z) throws RemoteException;

    void stopFreezingScreen() throws RemoteException;

    boolean stopViewServer() throws RemoteException;

    void thawRotation() throws RemoteException;

    Configuration updateOrientationFromAppTokens(Configuration configuration, IBinder iBinder) throws RemoteException;

    void updateRotation(boolean z, boolean z2) throws RemoteException;

    int watchRotation(IRotationWatcher iRotationWatcher) throws RemoteException;
}

package android.accessibilityservice;

import android.content.pm.ParceledListSlice;
import android.graphics.Region;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.accessibility.AccessibilityWindowInfo;
import android.view.accessibility.IAccessibilityInteractionConnectionCallback;
import java.util.List;

public interface IAccessibilityServiceConnection extends IInterface {

    public static abstract class Stub extends Binder implements IAccessibilityServiceConnection {
        private static final String DESCRIPTOR = "android.accessibilityservice.IAccessibilityServiceConnection";
        static final int TRANSACTION_disableSelf = 12;
        static final int TRANSACTION_findAccessibilityNodeInfoByAccessibilityId = 2;
        static final int TRANSACTION_findAccessibilityNodeInfosByText = 3;
        static final int TRANSACTION_findAccessibilityNodeInfosByViewId = 4;
        static final int TRANSACTION_findFocus = 5;
        static final int TRANSACTION_focusSearch = 6;
        static final int TRANSACTION_getMagnificationCenterX = 15;
        static final int TRANSACTION_getMagnificationCenterY = 16;
        static final int TRANSACTION_getMagnificationRegion = 17;
        static final int TRANSACTION_getMagnificationScale = 14;
        static final int TRANSACTION_getServiceInfo = 10;
        static final int TRANSACTION_getWindow = 8;
        static final int TRANSACTION_getWindows = 9;
        static final int TRANSACTION_performAccessibilityAction = 7;
        static final int TRANSACTION_performGlobalAction = 11;
        static final int TRANSACTION_resetMagnification = 18;
        static final int TRANSACTION_sendGesture = 23;
        static final int TRANSACTION_setMagnificationCallbackEnabled = 20;
        static final int TRANSACTION_setMagnificationScaleAndCenter = 19;
        static final int TRANSACTION_setOnKeyEventResult = 13;
        static final int TRANSACTION_setServiceInfo = 1;
        static final int TRANSACTION_setSoftKeyboardCallbackEnabled = 22;
        static final int TRANSACTION_setSoftKeyboardShowMode = 21;

        private static class Proxy implements IAccessibilityServiceConnection {
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

            public void setServiceInfo(AccessibilityServiceInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(Stub.TRANSACTION_setServiceInfo);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setServiceInfo, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean findAccessibilityNodeInfoByAccessibilityId(int accessibilityWindowId, long accessibilityNodeId, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, long threadId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(accessibilityWindowId);
                    _data.writeLong(accessibilityNodeId);
                    _data.writeInt(interactionId);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(flags);
                    _data.writeLong(threadId);
                    this.mRemote.transact(Stub.TRANSACTION_findAccessibilityNodeInfoByAccessibilityId, _data, _reply, 0);
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

            public boolean findAccessibilityNodeInfosByText(int accessibilityWindowId, long accessibilityNodeId, String text, int interactionId, IAccessibilityInteractionConnectionCallback callback, long threadId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(accessibilityWindowId);
                    _data.writeLong(accessibilityNodeId);
                    _data.writeString(text);
                    _data.writeInt(interactionId);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeLong(threadId);
                    this.mRemote.transact(Stub.TRANSACTION_findAccessibilityNodeInfosByText, _data, _reply, 0);
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

            public boolean findAccessibilityNodeInfosByViewId(int accessibilityWindowId, long accessibilityNodeId, String viewId, int interactionId, IAccessibilityInteractionConnectionCallback callback, long threadId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(accessibilityWindowId);
                    _data.writeLong(accessibilityNodeId);
                    _data.writeString(viewId);
                    _data.writeInt(interactionId);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeLong(threadId);
                    this.mRemote.transact(Stub.TRANSACTION_findAccessibilityNodeInfosByViewId, _data, _reply, 0);
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

            public boolean findFocus(int accessibilityWindowId, long accessibilityNodeId, int focusType, int interactionId, IAccessibilityInteractionConnectionCallback callback, long threadId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(accessibilityWindowId);
                    _data.writeLong(accessibilityNodeId);
                    _data.writeInt(focusType);
                    _data.writeInt(interactionId);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeLong(threadId);
                    this.mRemote.transact(Stub.TRANSACTION_findFocus, _data, _reply, 0);
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

            public boolean focusSearch(int accessibilityWindowId, long accessibilityNodeId, int direction, int interactionId, IAccessibilityInteractionConnectionCallback callback, long threadId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(accessibilityWindowId);
                    _data.writeLong(accessibilityNodeId);
                    _data.writeInt(direction);
                    _data.writeInt(interactionId);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeLong(threadId);
                    this.mRemote.transact(Stub.TRANSACTION_focusSearch, _data, _reply, 0);
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

            public boolean performAccessibilityAction(int accessibilityWindowId, long accessibilityNodeId, int action, Bundle arguments, int interactionId, IAccessibilityInteractionConnectionCallback callback, long threadId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(accessibilityWindowId);
                    _data.writeLong(accessibilityNodeId);
                    _data.writeInt(action);
                    if (arguments != null) {
                        _data.writeInt(Stub.TRANSACTION_setServiceInfo);
                        arguments.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(interactionId);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeLong(threadId);
                    this.mRemote.transact(Stub.TRANSACTION_performAccessibilityAction, _data, _reply, 0);
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

            public AccessibilityWindowInfo getWindow(int windowId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    AccessibilityWindowInfo accessibilityWindowInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(windowId);
                    this.mRemote.transact(Stub.TRANSACTION_getWindow, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        accessibilityWindowInfo = (AccessibilityWindowInfo) AccessibilityWindowInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        accessibilityWindowInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return accessibilityWindowInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<AccessibilityWindowInfo> getWindows() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getWindows, _data, _reply, 0);
                    _reply.readException();
                    List<AccessibilityWindowInfo> _result = _reply.createTypedArrayList(AccessibilityWindowInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public AccessibilityServiceInfo getServiceInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    AccessibilityServiceInfo accessibilityServiceInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getServiceInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        accessibilityServiceInfo = (AccessibilityServiceInfo) AccessibilityServiceInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        accessibilityServiceInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return accessibilityServiceInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean performGlobalAction(int action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action);
                    this.mRemote.transact(Stub.TRANSACTION_performGlobalAction, _data, _reply, 0);
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

            public void disableSelf() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_disableSelf, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setOnKeyEventResult(boolean handled, int sequence) throws RemoteException {
                int i = Stub.TRANSACTION_setServiceInfo;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!handled) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(sequence);
                    this.mRemote.transact(Stub.TRANSACTION_setOnKeyEventResult, _data, null, Stub.TRANSACTION_setServiceInfo);
                } finally {
                    _data.recycle();
                }
            }

            public float getMagnificationScale() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getMagnificationScale, _data, _reply, 0);
                    _reply.readException();
                    float _result = _reply.readFloat();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public float getMagnificationCenterX() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getMagnificationCenterX, _data, _reply, 0);
                    _reply.readException();
                    float _result = _reply.readFloat();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public float getMagnificationCenterY() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getMagnificationCenterY, _data, _reply, 0);
                    _reply.readException();
                    float _result = _reply.readFloat();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Region getMagnificationRegion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Region region;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getMagnificationRegion, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        region = (Region) Region.CREATOR.createFromParcel(_reply);
                    } else {
                        region = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return region;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean resetMagnification(boolean animate) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (animate) {
                        i = Stub.TRANSACTION_setServiceInfo;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_resetMagnification, _data, _reply, 0);
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

            public boolean setMagnificationScaleAndCenter(float scale, float centerX, float centerY, boolean animate) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(scale);
                    _data.writeFloat(centerX);
                    _data.writeFloat(centerY);
                    if (animate) {
                        i = Stub.TRANSACTION_setServiceInfo;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setMagnificationScaleAndCenter, _data, _reply, 0);
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

            public void setMagnificationCallbackEnabled(boolean enabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enabled) {
                        i = Stub.TRANSACTION_setServiceInfo;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setMagnificationCallbackEnabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setSoftKeyboardShowMode(int showMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(showMode);
                    this.mRemote.transact(Stub.TRANSACTION_setSoftKeyboardShowMode, _data, _reply, 0);
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

            public void setSoftKeyboardCallbackEnabled(boolean enabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enabled) {
                        i = Stub.TRANSACTION_setServiceInfo;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setSoftKeyboardCallbackEnabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendGesture(int sequence, ParceledListSlice gestureSteps) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sequence);
                    if (gestureSteps != null) {
                        _data.writeInt(Stub.TRANSACTION_setServiceInfo);
                        gestureSteps.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendGesture, _data, _reply, 0);
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

        public static IAccessibilityServiceConnection asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAccessibilityServiceConnection)) {
                return new Proxy(obj);
            }
            return (IAccessibilityServiceConnection) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            int _arg0;
            float _result2;
            switch (code) {
                case TRANSACTION_setServiceInfo /*1*/:
                    AccessibilityServiceInfo accessibilityServiceInfo;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        accessibilityServiceInfo = (AccessibilityServiceInfo) AccessibilityServiceInfo.CREATOR.createFromParcel(data);
                    } else {
                        accessibilityServiceInfo = null;
                    }
                    setServiceInfo(accessibilityServiceInfo);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_findAccessibilityNodeInfoByAccessibilityId /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = findAccessibilityNodeInfoByAccessibilityId(data.readInt(), data.readLong(), data.readInt(), android.view.accessibility.IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setServiceInfo : 0);
                    return true;
                case TRANSACTION_findAccessibilityNodeInfosByText /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = findAccessibilityNodeInfosByText(data.readInt(), data.readLong(), data.readString(), data.readInt(), android.view.accessibility.IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setServiceInfo : 0);
                    return true;
                case TRANSACTION_findAccessibilityNodeInfosByViewId /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = findAccessibilityNodeInfosByViewId(data.readInt(), data.readLong(), data.readString(), data.readInt(), android.view.accessibility.IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setServiceInfo : 0);
                    return true;
                case TRANSACTION_findFocus /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = findFocus(data.readInt(), data.readLong(), data.readInt(), data.readInt(), android.view.accessibility.IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setServiceInfo : 0);
                    return true;
                case TRANSACTION_focusSearch /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = focusSearch(data.readInt(), data.readLong(), data.readInt(), data.readInt(), android.view.accessibility.IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setServiceInfo : 0);
                    return true;
                case TRANSACTION_performAccessibilityAction /*7*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    long _arg1 = data.readLong();
                    int _arg2 = data.readInt();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = performAccessibilityAction(_arg0, _arg1, _arg2, bundle, data.readInt(), android.view.accessibility.IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setServiceInfo : 0);
                    return true;
                case TRANSACTION_getWindow /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    AccessibilityWindowInfo _result3 = getWindow(data.readInt());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_setServiceInfo);
                        _result3.writeToParcel(reply, TRANSACTION_setServiceInfo);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getWindows /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<AccessibilityWindowInfo> _result4 = getWindows();
                    reply.writeNoException();
                    reply.writeTypedList(_result4);
                    return true;
                case TRANSACTION_getServiceInfo /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    AccessibilityServiceInfo _result5 = getServiceInfo();
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(TRANSACTION_setServiceInfo);
                        _result5.writeToParcel(reply, TRANSACTION_setServiceInfo);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_performGlobalAction /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = performGlobalAction(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setServiceInfo : 0);
                    return true;
                case TRANSACTION_disableSelf /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    disableSelf();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setOnKeyEventResult /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    setOnKeyEventResult(data.readInt() != 0, data.readInt());
                    return true;
                case TRANSACTION_getMagnificationScale /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getMagnificationScale();
                    reply.writeNoException();
                    reply.writeFloat(_result2);
                    return true;
                case TRANSACTION_getMagnificationCenterX /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getMagnificationCenterX();
                    reply.writeNoException();
                    reply.writeFloat(_result2);
                    return true;
                case TRANSACTION_getMagnificationCenterY /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getMagnificationCenterY();
                    reply.writeNoException();
                    reply.writeFloat(_result2);
                    return true;
                case TRANSACTION_getMagnificationRegion /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    Region _result6 = getMagnificationRegion();
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(TRANSACTION_setServiceInfo);
                        _result6.writeToParcel(reply, TRANSACTION_setServiceInfo);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_resetMagnification /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = resetMagnification(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setServiceInfo : 0);
                    return true;
                case TRANSACTION_setMagnificationScaleAndCenter /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setMagnificationScaleAndCenter(data.readFloat(), data.readFloat(), data.readFloat(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setServiceInfo : 0);
                    return true;
                case TRANSACTION_setMagnificationCallbackEnabled /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    setMagnificationCallbackEnabled(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setSoftKeyboardShowMode /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setSoftKeyboardShowMode(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setServiceInfo : 0);
                    return true;
                case TRANSACTION_setSoftKeyboardCallbackEnabled /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    setSoftKeyboardCallbackEnabled(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sendGesture /*23*/:
                    ParceledListSlice parceledListSlice;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(data);
                    } else {
                        parceledListSlice = null;
                    }
                    sendGesture(_arg0, parceledListSlice);
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void disableSelf() throws RemoteException;

    boolean findAccessibilityNodeInfoByAccessibilityId(int i, long j, int i2, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i3, long j2) throws RemoteException;

    boolean findAccessibilityNodeInfosByText(int i, long j, String str, int i2, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, long j2) throws RemoteException;

    boolean findAccessibilityNodeInfosByViewId(int i, long j, String str, int i2, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, long j2) throws RemoteException;

    boolean findFocus(int i, long j, int i2, int i3, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, long j2) throws RemoteException;

    boolean focusSearch(int i, long j, int i2, int i3, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, long j2) throws RemoteException;

    float getMagnificationCenterX() throws RemoteException;

    float getMagnificationCenterY() throws RemoteException;

    Region getMagnificationRegion() throws RemoteException;

    float getMagnificationScale() throws RemoteException;

    AccessibilityServiceInfo getServiceInfo() throws RemoteException;

    AccessibilityWindowInfo getWindow(int i) throws RemoteException;

    List<AccessibilityWindowInfo> getWindows() throws RemoteException;

    boolean performAccessibilityAction(int i, long j, int i2, Bundle bundle, int i3, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, long j2) throws RemoteException;

    boolean performGlobalAction(int i) throws RemoteException;

    boolean resetMagnification(boolean z) throws RemoteException;

    void sendGesture(int i, ParceledListSlice parceledListSlice) throws RemoteException;

    void setMagnificationCallbackEnabled(boolean z) throws RemoteException;

    boolean setMagnificationScaleAndCenter(float f, float f2, float f3, boolean z) throws RemoteException;

    void setOnKeyEventResult(boolean z, int i) throws RemoteException;

    void setServiceInfo(AccessibilityServiceInfo accessibilityServiceInfo) throws RemoteException;

    void setSoftKeyboardCallbackEnabled(boolean z) throws RemoteException;

    boolean setSoftKeyboardShowMode(int i) throws RemoteException;
}

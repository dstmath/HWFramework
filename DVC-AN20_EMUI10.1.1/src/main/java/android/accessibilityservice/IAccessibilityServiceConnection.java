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
    void disableSelf() throws RemoteException;

    String[] findAccessibilityNodeInfoByAccessibilityId(int i, long j, int i2, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i3, long j2, Bundle bundle) throws RemoteException;

    String[] findAccessibilityNodeInfosByText(int i, long j, String str, int i2, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, long j2) throws RemoteException;

    String[] findAccessibilityNodeInfosByViewId(int i, long j, String str, int i2, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, long j2) throws RemoteException;

    String[] findFocus(int i, long j, int i2, int i3, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, long j2) throws RemoteException;

    String[] focusSearch(int i, long j, int i2, int i3, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, long j2) throws RemoteException;

    float getMagnificationCenterX(int i) throws RemoteException;

    float getMagnificationCenterY(int i) throws RemoteException;

    Region getMagnificationRegion(int i) throws RemoteException;

    float getMagnificationScale(int i) throws RemoteException;

    AccessibilityServiceInfo getServiceInfo() throws RemoteException;

    int getSoftKeyboardShowMode() throws RemoteException;

    AccessibilityWindowInfo getWindow(int i) throws RemoteException;

    List<AccessibilityWindowInfo> getWindows() throws RemoteException;

    boolean isAccessibilityButtonAvailable() throws RemoteException;

    boolean isFingerprintGestureDetectionAvailable() throws RemoteException;

    boolean performAccessibilityAction(int i, long j, int i2, Bundle bundle, int i3, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, long j2) throws RemoteException;

    boolean performGlobalAction(int i) throws RemoteException;

    boolean resetMagnification(int i, boolean z) throws RemoteException;

    void sendGesture(int i, ParceledListSlice parceledListSlice) throws RemoteException;

    void setMagnificationCallbackEnabled(int i, boolean z) throws RemoteException;

    boolean setMagnificationScaleAndCenter(int i, float f, float f2, float f3, boolean z) throws RemoteException;

    void setOnKeyEventResult(boolean z, int i) throws RemoteException;

    void setServiceInfo(AccessibilityServiceInfo accessibilityServiceInfo) throws RemoteException;

    void setSoftKeyboardCallbackEnabled(boolean z) throws RemoteException;

    boolean setSoftKeyboardShowMode(int i) throws RemoteException;

    public static class Default implements IAccessibilityServiceConnection {
        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public void setServiceInfo(AccessibilityServiceInfo info) throws RemoteException {
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public String[] findAccessibilityNodeInfoByAccessibilityId(int accessibilityWindowId, long accessibilityNodeId, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, long threadId, Bundle arguments) throws RemoteException {
            return null;
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public String[] findAccessibilityNodeInfosByText(int accessibilityWindowId, long accessibilityNodeId, String text, int interactionId, IAccessibilityInteractionConnectionCallback callback, long threadId) throws RemoteException {
            return null;
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public String[] findAccessibilityNodeInfosByViewId(int accessibilityWindowId, long accessibilityNodeId, String viewId, int interactionId, IAccessibilityInteractionConnectionCallback callback, long threadId) throws RemoteException {
            return null;
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public String[] findFocus(int accessibilityWindowId, long accessibilityNodeId, int focusType, int interactionId, IAccessibilityInteractionConnectionCallback callback, long threadId) throws RemoteException {
            return null;
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public String[] focusSearch(int accessibilityWindowId, long accessibilityNodeId, int direction, int interactionId, IAccessibilityInteractionConnectionCallback callback, long threadId) throws RemoteException {
            return null;
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public boolean performAccessibilityAction(int accessibilityWindowId, long accessibilityNodeId, int action, Bundle arguments, int interactionId, IAccessibilityInteractionConnectionCallback callback, long threadId) throws RemoteException {
            return false;
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public AccessibilityWindowInfo getWindow(int windowId) throws RemoteException {
            return null;
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public List<AccessibilityWindowInfo> getWindows() throws RemoteException {
            return null;
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public AccessibilityServiceInfo getServiceInfo() throws RemoteException {
            return null;
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public boolean performGlobalAction(int action) throws RemoteException {
            return false;
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public void disableSelf() throws RemoteException {
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public void setOnKeyEventResult(boolean handled, int sequence) throws RemoteException {
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public float getMagnificationScale(int displayId) throws RemoteException {
            return 0.0f;
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public float getMagnificationCenterX(int displayId) throws RemoteException {
            return 0.0f;
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public float getMagnificationCenterY(int displayId) throws RemoteException {
            return 0.0f;
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public Region getMagnificationRegion(int displayId) throws RemoteException {
            return null;
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public boolean resetMagnification(int displayId, boolean animate) throws RemoteException {
            return false;
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public boolean setMagnificationScaleAndCenter(int displayId, float scale, float centerX, float centerY, boolean animate) throws RemoteException {
            return false;
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public void setMagnificationCallbackEnabled(int displayId, boolean enabled) throws RemoteException {
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public boolean setSoftKeyboardShowMode(int showMode) throws RemoteException {
            return false;
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public int getSoftKeyboardShowMode() throws RemoteException {
            return 0;
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public void setSoftKeyboardCallbackEnabled(boolean enabled) throws RemoteException {
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public boolean isAccessibilityButtonAvailable() throws RemoteException {
            return false;
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public void sendGesture(int sequence, ParceledListSlice gestureSteps) throws RemoteException {
        }

        @Override // android.accessibilityservice.IAccessibilityServiceConnection
        public boolean isFingerprintGestureDetectionAvailable() throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

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
        static final int TRANSACTION_getSoftKeyboardShowMode = 22;
        static final int TRANSACTION_getWindow = 8;
        static final int TRANSACTION_getWindows = 9;
        static final int TRANSACTION_isAccessibilityButtonAvailable = 24;
        static final int TRANSACTION_isFingerprintGestureDetectionAvailable = 26;
        static final int TRANSACTION_performAccessibilityAction = 7;
        static final int TRANSACTION_performGlobalAction = 11;
        static final int TRANSACTION_resetMagnification = 18;
        static final int TRANSACTION_sendGesture = 25;
        static final int TRANSACTION_setMagnificationCallbackEnabled = 20;
        static final int TRANSACTION_setMagnificationScaleAndCenter = 19;
        static final int TRANSACTION_setOnKeyEventResult = 13;
        static final int TRANSACTION_setServiceInfo = 1;
        static final int TRANSACTION_setSoftKeyboardCallbackEnabled = 23;
        static final int TRANSACTION_setSoftKeyboardShowMode = 21;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "setServiceInfo";
                case 2:
                    return "findAccessibilityNodeInfoByAccessibilityId";
                case 3:
                    return "findAccessibilityNodeInfosByText";
                case 4:
                    return "findAccessibilityNodeInfosByViewId";
                case 5:
                    return "findFocus";
                case 6:
                    return "focusSearch";
                case 7:
                    return "performAccessibilityAction";
                case 8:
                    return "getWindow";
                case 9:
                    return "getWindows";
                case 10:
                    return "getServiceInfo";
                case 11:
                    return "performGlobalAction";
                case 12:
                    return "disableSelf";
                case 13:
                    return "setOnKeyEventResult";
                case 14:
                    return "getMagnificationScale";
                case 15:
                    return "getMagnificationCenterX";
                case 16:
                    return "getMagnificationCenterY";
                case 17:
                    return "getMagnificationRegion";
                case 18:
                    return "resetMagnification";
                case 19:
                    return "setMagnificationScaleAndCenter";
                case 20:
                    return "setMagnificationCallbackEnabled";
                case 21:
                    return "setSoftKeyboardShowMode";
                case 22:
                    return "getSoftKeyboardShowMode";
                case 23:
                    return "setSoftKeyboardCallbackEnabled";
                case 24:
                    return "isAccessibilityButtonAvailable";
                case 25:
                    return "sendGesture";
                case 26:
                    return "isFingerprintGestureDetectionAvailable";
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
            AccessibilityServiceInfo _arg0;
            Bundle _arg6;
            Bundle _arg3;
            ParceledListSlice _arg1;
            if (code != 1598968902) {
                boolean _arg02 = false;
                boolean _arg03 = false;
                boolean _arg12 = false;
                boolean _arg13 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = AccessibilityServiceInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        setServiceInfo(_arg0);
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        long _arg14 = data.readLong();
                        int _arg2 = data.readInt();
                        IAccessibilityInteractionConnectionCallback _arg32 = IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder());
                        int _arg4 = data.readInt();
                        long _arg5 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg6 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg6 = null;
                        }
                        String[] _result = findAccessibilityNodeInfoByAccessibilityId(_arg04, _arg14, _arg2, _arg32, _arg4, _arg5, _arg6);
                        reply.writeNoException();
                        reply.writeStringArray(_result);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result2 = findAccessibilityNodeInfosByText(data.readInt(), data.readLong(), data.readString(), data.readInt(), IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                        reply.writeNoException();
                        reply.writeStringArray(_result2);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result3 = findAccessibilityNodeInfosByViewId(data.readInt(), data.readLong(), data.readString(), data.readInt(), IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                        reply.writeNoException();
                        reply.writeStringArray(_result3);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result4 = findFocus(data.readInt(), data.readLong(), data.readInt(), data.readInt(), IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                        reply.writeNoException();
                        reply.writeStringArray(_result4);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result5 = focusSearch(data.readInt(), data.readLong(), data.readInt(), data.readInt(), IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                        reply.writeNoException();
                        reply.writeStringArray(_result5);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        long _arg15 = data.readLong();
                        int _arg22 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        boolean performAccessibilityAction = performAccessibilityAction(_arg05, _arg15, _arg22, _arg3, data.readInt(), IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                        reply.writeNoException();
                        reply.writeInt(performAccessibilityAction ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        AccessibilityWindowInfo _result6 = getWindow(data.readInt());
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        List<AccessibilityWindowInfo> _result7 = getWindows();
                        reply.writeNoException();
                        reply.writeTypedList(_result7);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        AccessibilityServiceInfo _result8 = getServiceInfo();
                        reply.writeNoException();
                        if (_result8 != null) {
                            reply.writeInt(1);
                            _result8.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean performGlobalAction = performGlobalAction(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(performGlobalAction ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        disableSelf();
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setOnKeyEventResult(_arg02, data.readInt());
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        float _result9 = getMagnificationScale(data.readInt());
                        reply.writeNoException();
                        reply.writeFloat(_result9);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        float _result10 = getMagnificationCenterX(data.readInt());
                        reply.writeNoException();
                        reply.writeFloat(_result10);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        float _result11 = getMagnificationCenterY(data.readInt());
                        reply.writeNoException();
                        reply.writeFloat(_result11);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        Region _result12 = getMagnificationRegion(data.readInt());
                        reply.writeNoException();
                        if (_result12 != null) {
                            reply.writeInt(1);
                            _result12.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        boolean resetMagnification = resetMagnification(_arg06, _arg13);
                        reply.writeNoException();
                        reply.writeInt(resetMagnification ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        boolean magnificationScaleAndCenter = setMagnificationScaleAndCenter(data.readInt(), data.readFloat(), data.readFloat(), data.readFloat(), data.readInt() != 0);
                        reply.writeNoException();
                        reply.writeInt(magnificationScaleAndCenter ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg07 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        setMagnificationCallbackEnabled(_arg07, _arg12);
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        boolean softKeyboardShowMode = setSoftKeyboardShowMode(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(softKeyboardShowMode ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = getSoftKeyboardShowMode();
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        setSoftKeyboardCallbackEnabled(_arg03);
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAccessibilityButtonAvailable = isAccessibilityButtonAvailable();
                        reply.writeNoException();
                        reply.writeInt(isAccessibilityButtonAvailable ? 1 : 0);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = ParceledListSlice.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        sendGesture(_arg08, _arg1);
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isFingerprintGestureDetectionAvailable = isFingerprintGestureDetectionAvailable();
                        reply.writeNoException();
                        reply.writeInt(isFingerprintGestureDetectionAvailable ? 1 : 0);
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
        public static class Proxy implements IAccessibilityServiceConnection {
            public static IAccessibilityServiceConnection sDefaultImpl;
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

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public void setServiceInfo(AccessibilityServiceInfo info) throws RemoteException {
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
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setServiceInfo(info);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public String[] findAccessibilityNodeInfoByAccessibilityId(int accessibilityWindowId, long accessibilityNodeId, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, long threadId, Bundle arguments) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(accessibilityWindowId);
                        _data.writeLong(accessibilityNodeId);
                        try {
                            _data.writeInt(interactionId);
                            _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                            _data.writeInt(flags);
                            _data.writeLong(threadId);
                            if (arguments != null) {
                                _data.writeInt(1);
                                arguments.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                String[] _result = _reply.createStringArray();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            String[] findAccessibilityNodeInfoByAccessibilityId = Stub.getDefaultImpl().findAccessibilityNodeInfoByAccessibilityId(accessibilityWindowId, accessibilityNodeId, interactionId, callback, flags, threadId, arguments);
                            _reply.recycle();
                            _data.recycle();
                            return findAccessibilityNodeInfoByAccessibilityId;
                        } catch (Throwable th) {
                            th = th;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public String[] findAccessibilityNodeInfosByText(int accessibilityWindowId, long accessibilityNodeId, String text, int interactionId, IAccessibilityInteractionConnectionCallback callback, long threadId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(accessibilityWindowId);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(accessibilityNodeId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(text);
                        _data.writeInt(interactionId);
                        _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                        _data.writeLong(threadId);
                        if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            String[] _result = _reply.createStringArray();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        String[] findAccessibilityNodeInfosByText = Stub.getDefaultImpl().findAccessibilityNodeInfosByText(accessibilityWindowId, accessibilityNodeId, text, interactionId, callback, threadId);
                        _reply.recycle();
                        _data.recycle();
                        return findAccessibilityNodeInfosByText;
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public String[] findAccessibilityNodeInfosByViewId(int accessibilityWindowId, long accessibilityNodeId, String viewId, int interactionId, IAccessibilityInteractionConnectionCallback callback, long threadId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(accessibilityWindowId);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(accessibilityNodeId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(viewId);
                        _data.writeInt(interactionId);
                        _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                        _data.writeLong(threadId);
                        if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            String[] _result = _reply.createStringArray();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        String[] findAccessibilityNodeInfosByViewId = Stub.getDefaultImpl().findAccessibilityNodeInfosByViewId(accessibilityWindowId, accessibilityNodeId, viewId, interactionId, callback, threadId);
                        _reply.recycle();
                        _data.recycle();
                        return findAccessibilityNodeInfosByViewId;
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public String[] findFocus(int accessibilityWindowId, long accessibilityNodeId, int focusType, int interactionId, IAccessibilityInteractionConnectionCallback callback, long threadId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(accessibilityWindowId);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(accessibilityNodeId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(focusType);
                        _data.writeInt(interactionId);
                        _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                        _data.writeLong(threadId);
                        if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            String[] _result = _reply.createStringArray();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        String[] findFocus = Stub.getDefaultImpl().findFocus(accessibilityWindowId, accessibilityNodeId, focusType, interactionId, callback, threadId);
                        _reply.recycle();
                        _data.recycle();
                        return findFocus;
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public String[] focusSearch(int accessibilityWindowId, long accessibilityNodeId, int direction, int interactionId, IAccessibilityInteractionConnectionCallback callback, long threadId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(accessibilityWindowId);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(accessibilityNodeId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(direction);
                        _data.writeInt(interactionId);
                        _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                        _data.writeLong(threadId);
                        if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            String[] _result = _reply.createStringArray();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        String[] focusSearch = Stub.getDefaultImpl().focusSearch(accessibilityWindowId, accessibilityNodeId, direction, interactionId, callback, threadId);
                        _reply.recycle();
                        _data.recycle();
                        return focusSearch;
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public boolean performAccessibilityAction(int accessibilityWindowId, long accessibilityNodeId, int action, Bundle arguments, int interactionId, IAccessibilityInteractionConnectionCallback callback, long threadId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(accessibilityWindowId);
                        _data.writeLong(accessibilityNodeId);
                        try {
                            _data.writeInt(action);
                            boolean _result = true;
                            if (arguments != null) {
                                _data.writeInt(1);
                                arguments.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            _data.writeInt(interactionId);
                            _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                            _data.writeLong(threadId);
                            if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() == 0) {
                                    _result = false;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean performAccessibilityAction = Stub.getDefaultImpl().performAccessibilityAction(accessibilityWindowId, accessibilityNodeId, action, arguments, interactionId, callback, threadId);
                            _reply.recycle();
                            _data.recycle();
                            return performAccessibilityAction;
                        } catch (Throwable th) {
                            th = th;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public AccessibilityWindowInfo getWindow(int windowId) throws RemoteException {
                AccessibilityWindowInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(windowId);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWindow(windowId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = AccessibilityWindowInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public List<AccessibilityWindowInfo> getWindows() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWindows();
                    }
                    _reply.readException();
                    List<AccessibilityWindowInfo> _result = _reply.createTypedArrayList(AccessibilityWindowInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public AccessibilityServiceInfo getServiceInfo() throws RemoteException {
                AccessibilityServiceInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getServiceInfo();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = AccessibilityServiceInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public boolean performGlobalAction(int action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action);
                    boolean _result = false;
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().performGlobalAction(action);
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

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public void disableSelf() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().disableSelf();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public void setOnKeyEventResult(boolean handled, int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(handled ? 1 : 0);
                    _data.writeInt(sequence);
                    if (this.mRemote.transact(13, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setOnKeyEventResult(handled, sequence);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public float getMagnificationScale(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMagnificationScale(displayId);
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

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public float getMagnificationCenterX(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMagnificationCenterX(displayId);
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

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public float getMagnificationCenterY(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMagnificationCenterY(displayId);
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

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public Region getMagnificationRegion(int displayId) throws RemoteException {
                Region _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMagnificationRegion(displayId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Region.CREATOR.createFromParcel(_reply);
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

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public boolean resetMagnification(int displayId, boolean animate) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    boolean _result = true;
                    _data.writeInt(animate ? 1 : 0);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().resetMagnification(displayId, animate);
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

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public boolean setMagnificationScaleAndCenter(int displayId, float scale, float centerX, float centerY, boolean animate) throws RemoteException {
                boolean _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(displayId);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeFloat(scale);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeFloat(centerX);
                        try {
                            _data.writeFloat(centerY);
                            _result = true;
                            _data.writeInt(animate ? 1 : 0);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() == 0) {
                                    _result = false;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean magnificationScaleAndCenter = Stub.getDefaultImpl().setMagnificationScaleAndCenter(displayId, scale, centerX, centerY, animate);
                            _reply.recycle();
                            _data.recycle();
                            return magnificationScaleAndCenter;
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public void setMagnificationCallbackEnabled(int displayId, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMagnificationCallbackEnabled(displayId, enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public boolean setSoftKeyboardShowMode(int showMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(showMode);
                    boolean _result = false;
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSoftKeyboardShowMode(showMode);
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

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public int getSoftKeyboardShowMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSoftKeyboardShowMode();
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

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public void setSoftKeyboardCallbackEnabled(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSoftKeyboardCallbackEnabled(enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public boolean isAccessibilityButtonAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAccessibilityButtonAvailable();
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

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public void sendGesture(int sequence, ParceledListSlice gestureSteps) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sequence);
                    if (gestureSteps != null) {
                        _data.writeInt(1);
                        gestureSteps.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendGesture(sequence, gestureSteps);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceConnection
            public boolean isFingerprintGestureDetectionAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isFingerprintGestureDetectionAvailable();
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

        public static boolean setDefaultImpl(IAccessibilityServiceConnection impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAccessibilityServiceConnection getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

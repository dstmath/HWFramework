package com.huawei.android.hardware.input;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.InputEvent;
import com.huawei.android.hardware.input.IHwTHPEventListener;

public interface IHwInputManager extends IInterface {
    void fadeMousePointer() throws RemoteException;

    boolean injectInputEventByDisplayId(InputEvent inputEvent, int i, int i2) throws RemoteException;

    void registerListener(IHwTHPEventListener iHwTHPEventListener, IBinder iBinder) throws RemoteException;

    String runHwTHPCommand(String str, String str2) throws RemoteException;

    String runSideTouchCommand(String str, String str2) throws RemoteException;

    void setInputEventStrategy(boolean z) throws RemoteException;

    void setMousePosition(float f, float f2) throws RemoteException;

    int[] setTPCommand(int i, Bundle bundle) throws RemoteException;

    int setTouchscreenFeatureConfig(int i, String str) throws RemoteException;

    void unregisterListener(IHwTHPEventListener iHwTHPEventListener, IBinder iBinder) throws RemoteException;

    public static class Default implements IHwInputManager {
        @Override // com.huawei.android.hardware.input.IHwInputManager
        public String runHwTHPCommand(String command, String parameter) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.hardware.input.IHwInputManager
        public void registerListener(IHwTHPEventListener listener, IBinder ibinder) throws RemoteException {
        }

        @Override // com.huawei.android.hardware.input.IHwInputManager
        public void unregisterListener(IHwTHPEventListener listener, IBinder ibinder) throws RemoteException {
        }

        @Override // com.huawei.android.hardware.input.IHwInputManager
        public void setInputEventStrategy(boolean isStartInputEventControl) throws RemoteException {
        }

        @Override // com.huawei.android.hardware.input.IHwInputManager
        public String runSideTouchCommand(String command, String parameter) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.hardware.input.IHwInputManager
        public int[] setTPCommand(int type, Bundle bundle) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.hardware.input.IHwInputManager
        public boolean injectInputEventByDisplayId(InputEvent inputEvent, int mode, int displayId) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.hardware.input.IHwInputManager
        public int setTouchscreenFeatureConfig(int feature, String config) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.hardware.input.IHwInputManager
        public void fadeMousePointer() throws RemoteException {
        }

        @Override // com.huawei.android.hardware.input.IHwInputManager
        public void setMousePosition(float xPosition, float yPosition) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwInputManager {
        private static final String DESCRIPTOR = "com.huawei.android.hardware.input.IHwInputManager";
        static final int TRANSACTION_fadeMousePointer = 9;
        static final int TRANSACTION_injectInputEventByDisplayId = 7;
        static final int TRANSACTION_registerListener = 2;
        static final int TRANSACTION_runHwTHPCommand = 1;
        static final int TRANSACTION_runSideTouchCommand = 5;
        static final int TRANSACTION_setInputEventStrategy = 4;
        static final int TRANSACTION_setMousePosition = 10;
        static final int TRANSACTION_setTPCommand = 6;
        static final int TRANSACTION_setTouchscreenFeatureConfig = 8;
        static final int TRANSACTION_unregisterListener = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwInputManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwInputManager)) {
                return new Proxy(obj);
            }
            return (IHwInputManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "runHwTHPCommand";
                case 2:
                    return "registerListener";
                case 3:
                    return "unregisterListener";
                case 4:
                    return "setInputEventStrategy";
                case 5:
                    return "runSideTouchCommand";
                case 6:
                    return "setTPCommand";
                case 7:
                    return "injectInputEventByDisplayId";
                case 8:
                    return "setTouchscreenFeatureConfig";
                case 9:
                    return "fadeMousePointer";
                case 10:
                    return "setMousePosition";
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
            Bundle _arg1;
            InputEvent _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = runHwTHPCommand(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        registerListener(IHwTHPEventListener.Stub.asInterface(data.readStrongBinder()), data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterListener(IHwTHPEventListener.Stub.asInterface(data.readStrongBinder()), data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        setInputEventStrategy(data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String _result2 = runSideTouchCommand(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int[] _result3 = setTPCommand(_arg02, _arg1);
                        reply.writeNoException();
                        reply.writeIntArray(_result3);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = InputEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        boolean injectInputEventByDisplayId = injectInputEventByDisplayId(_arg0, data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(injectInputEventByDisplayId ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = setTouchscreenFeatureConfig(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        fadeMousePointer();
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        setMousePosition(data.readFloat(), data.readFloat());
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
        public static class Proxy implements IHwInputManager {
            public static IHwInputManager sDefaultImpl;
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

            @Override // com.huawei.android.hardware.input.IHwInputManager
            public String runHwTHPCommand(String command, String parameter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(command);
                    _data.writeString(parameter);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().runHwTHPCommand(command, parameter);
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

            @Override // com.huawei.android.hardware.input.IHwInputManager
            public void registerListener(IHwTHPEventListener listener, IBinder ibinder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeStrongBinder(ibinder);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerListener(listener, ibinder);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.input.IHwInputManager
            public void unregisterListener(IHwTHPEventListener listener, IBinder ibinder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeStrongBinder(ibinder);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterListener(listener, ibinder);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.input.IHwInputManager
            public void setInputEventStrategy(boolean isStartInputEventControl) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isStartInputEventControl ? 1 : 0);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setInputEventStrategy(isStartInputEventControl);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.input.IHwInputManager
            public String runSideTouchCommand(String command, String parameter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(command);
                    _data.writeString(parameter);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().runSideTouchCommand(command, parameter);
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

            @Override // com.huawei.android.hardware.input.IHwInputManager
            public int[] setTPCommand(int type, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setTPCommand(type, bundle);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.input.IHwInputManager
            public boolean injectInputEventByDisplayId(InputEvent inputEvent, int mode, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (inputEvent != null) {
                        _data.writeInt(1);
                        inputEvent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(mode);
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().injectInputEventByDisplayId(inputEvent, mode, displayId);
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

            @Override // com.huawei.android.hardware.input.IHwInputManager
            public int setTouchscreenFeatureConfig(int feature, String config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(feature);
                    _data.writeString(config);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setTouchscreenFeatureConfig(feature, config);
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

            @Override // com.huawei.android.hardware.input.IHwInputManager
            public void fadeMousePointer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().fadeMousePointer();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.input.IHwInputManager
            public void setMousePosition(float xPosition, float yPosition) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(xPosition);
                    _data.writeFloat(yPosition);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMousePosition(xPosition, yPosition);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwInputManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwInputManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

package com.android.systemui.shared.recents;

import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.systemui.shared.system.GraphicBufferCompat;

public interface ISystemUiProxy extends IInterface {

    public static abstract class Stub extends Binder implements ISystemUiProxy {
        private static final String DESCRIPTOR = "com.android.systemui.shared.recents.ISystemUiProxy";
        static final int TRANSACTION_getNonMinimizedSplitScreenSecondaryBounds = 8;
        static final int TRANSACTION_onOverviewShown = 7;
        static final int TRANSACTION_onSplitScreenInvoked = 6;
        static final int TRANSACTION_screenshot = 1;
        static final int TRANSACTION_setBackButtonAlpha = 9;
        static final int TRANSACTION_setInteractionState = 5;
        static final int TRANSACTION_startScreenPinning = 2;
        static final int TRANSACTION_updateFullScreenButton = 10;

        private static class Proxy implements ISystemUiProxy {
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

            public GraphicBufferCompat screenshot(Rect sourceCrop, int width, int height, int minLayer, int maxLayer, boolean useIdentityTransform, int rotation) throws RemoteException {
                GraphicBufferCompat _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sourceCrop != null) {
                        _data.writeInt(1);
                        sourceCrop.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(width);
                    _data.writeInt(height);
                    _data.writeInt(minLayer);
                    _data.writeInt(maxLayer);
                    _data.writeInt(useIdentityTransform);
                    _data.writeInt(rotation);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = GraphicBufferCompat.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startScreenPinning(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setInteractionState(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onSplitScreenInvoked() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onOverviewShown(boolean fromHome) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fromHome);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Rect getNonMinimizedSplitScreenSecondaryBounds() throws RemoteException {
                Rect _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Rect) Rect.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setBackButtonAlpha(float alpha, boolean animate) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(alpha);
                    _data.writeInt(animate);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateFullScreenButton(boolean show) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(show);
                    this.mRemote.transact(10, _data, _reply, 0);
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

        public static ISystemUiProxy asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISystemUiProxy)) {
                return new Proxy(obj);
            }
            return (ISystemUiProxy) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Rect _arg0;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                boolean _arg02 = false;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Rect) Rect.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg0 = null;
                        }
                        GraphicBufferCompat _result = screenshot(_arg0, data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt() != 0, data.readInt());
                        reply.writeNoException();
                        if (_result != null) {
                            parcel2.writeInt(1);
                            _result.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        startScreenPinning(data.readInt());
                        reply.writeNoException();
                        return true;
                    default:
                        switch (i) {
                            case 5:
                                parcel.enforceInterface(DESCRIPTOR);
                                setInteractionState(data.readInt());
                                reply.writeNoException();
                                return true;
                            case 6:
                                parcel.enforceInterface(DESCRIPTOR);
                                onSplitScreenInvoked();
                                reply.writeNoException();
                                return true;
                            case 7:
                                parcel.enforceInterface(DESCRIPTOR);
                                if (data.readInt() != 0) {
                                    _arg02 = true;
                                }
                                onOverviewShown(_arg02);
                                reply.writeNoException();
                                return true;
                            case 8:
                                parcel.enforceInterface(DESCRIPTOR);
                                Rect _result2 = getNonMinimizedSplitScreenSecondaryBounds();
                                reply.writeNoException();
                                if (_result2 != null) {
                                    parcel2.writeInt(1);
                                    _result2.writeToParcel(parcel2, 1);
                                } else {
                                    parcel2.writeInt(0);
                                }
                                return true;
                            case 9:
                                parcel.enforceInterface(DESCRIPTOR);
                                float _arg03 = data.readFloat();
                                if (data.readInt() != 0) {
                                    _arg02 = true;
                                }
                                setBackButtonAlpha(_arg03, _arg02);
                                reply.writeNoException();
                                return true;
                            case 10:
                                parcel.enforceInterface(DESCRIPTOR);
                                if (data.readInt() != 0) {
                                    _arg02 = true;
                                }
                                updateFullScreenButton(_arg02);
                                reply.writeNoException();
                                return true;
                            default:
                                return super.onTransact(code, data, reply, flags);
                        }
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    Rect getNonMinimizedSplitScreenSecondaryBounds() throws RemoteException;

    void onOverviewShown(boolean z) throws RemoteException;

    void onSplitScreenInvoked() throws RemoteException;

    GraphicBufferCompat screenshot(Rect rect, int i, int i2, int i3, int i4, boolean z, int i5) throws RemoteException;

    void setBackButtonAlpha(float f, boolean z) throws RemoteException;

    void setInteractionState(int i) throws RemoteException;

    void startScreenPinning(int i) throws RemoteException;

    void updateFullScreenButton(boolean z) throws RemoteException;
}

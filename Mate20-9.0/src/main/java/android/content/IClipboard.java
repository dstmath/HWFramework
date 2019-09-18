package android.content;

import android.content.IOnPrimaryClipChangedListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IClipboard extends IInterface {

    public static abstract class Stub extends Binder implements IClipboard {
        private static final String DESCRIPTOR = "android.content.IClipboard";
        static final int TRANSACTION_addPrimaryClipChangedListener = 6;
        static final int TRANSACTION_clearPrimaryClip = 2;
        static final int TRANSACTION_getHwInnerService = 9;
        static final int TRANSACTION_getPrimaryClip = 3;
        static final int TRANSACTION_getPrimaryClipDescription = 4;
        static final int TRANSACTION_hasClipboardText = 8;
        static final int TRANSACTION_hasPrimaryClip = 5;
        static final int TRANSACTION_removePrimaryClipChangedListener = 7;
        static final int TRANSACTION_setPrimaryClip = 1;

        private static class Proxy implements IClipboard {
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

            public void setPrimaryClip(ClipData clip, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (clip != null) {
                        _data.writeInt(1);
                        clip.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearPrimaryClip(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ClipData getPrimaryClip(String pkg) throws RemoteException {
                ClipData _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ClipData.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ClipDescription getPrimaryClipDescription(String callingPackage) throws RemoteException {
                ClipDescription _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ClipDescription.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasPrimaryClip(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addPrimaryClipChangedListener(IOnPrimaryClipChangedListener listener, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removePrimaryClipChangedListener(IOnPrimaryClipChangedListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasClipboardText(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder getHwInnerService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readStrongBinder();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IClipboard asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IClipboard)) {
                return new Proxy(obj);
            }
            return (IClipboard) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ClipData _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ClipData.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        setPrimaryClip(_arg0, data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        clearPrimaryClip(data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        ClipData _result = getPrimaryClip(data.readString());
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
                        ClipDescription _result2 = getPrimaryClipDescription(data.readString());
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
                        boolean _result3 = hasPrimaryClip(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        addPrimaryClipChangedListener(IOnPrimaryClipChangedListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        removePrimaryClipChangedListener(IOnPrimaryClipChangedListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result4 = hasClipboardText(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result5 = getHwInnerService();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result5);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void addPrimaryClipChangedListener(IOnPrimaryClipChangedListener iOnPrimaryClipChangedListener, String str) throws RemoteException;

    void clearPrimaryClip(String str) throws RemoteException;

    IBinder getHwInnerService() throws RemoteException;

    ClipData getPrimaryClip(String str) throws RemoteException;

    ClipDescription getPrimaryClipDescription(String str) throws RemoteException;

    boolean hasClipboardText(String str) throws RemoteException;

    boolean hasPrimaryClip(String str) throws RemoteException;

    void removePrimaryClipChangedListener(IOnPrimaryClipChangedListener iOnPrimaryClipChangedListener) throws RemoteException;

    void setPrimaryClip(ClipData clipData, String str) throws RemoteException;
}

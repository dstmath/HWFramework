package android.content;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IClipboard extends IInterface {

    public static abstract class Stub extends Binder implements IClipboard {
        private static final String DESCRIPTOR = "android.content.IClipboard";
        static final int TRANSACTION_addPrimaryClipChangedListener = 5;
        static final int TRANSACTION_getPrimaryClip = 2;
        static final int TRANSACTION_getPrimaryClipDescription = 3;
        static final int TRANSACTION_hasClipboardText = 7;
        static final int TRANSACTION_hasPrimaryClip = 4;
        static final int TRANSACTION_removePrimaryClipChangedListener = 6;
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
                        _data.writeInt(Stub.TRANSACTION_setPrimaryClip);
                        clip.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_setPrimaryClip, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ClipData getPrimaryClip(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ClipData clipData;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_getPrimaryClip, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        clipData = (ClipData) ClipData.CREATOR.createFromParcel(_reply);
                    } else {
                        clipData = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return clipData;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ClipDescription getPrimaryClipDescription(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ClipDescription clipDescription;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getPrimaryClipDescription, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        clipDescription = (ClipDescription) ClipDescription.CREATOR.createFromParcel(_reply);
                    } else {
                        clipDescription = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return clipDescription;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_hasPrimaryClip, _data, _reply, 0);
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

            public void addPrimaryClipChangedListener(IOnPrimaryClipChangedListener listener, String callingPackage) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_addPrimaryClipChangedListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removePrimaryClipChangedListener(IOnPrimaryClipChangedListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_removePrimaryClipChangedListener, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_hasClipboardText, _data, _reply, 0);
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
            int i = 0;
            boolean _result;
            switch (code) {
                case TRANSACTION_setPrimaryClip /*1*/:
                    ClipData clipData;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        clipData = (ClipData) ClipData.CREATOR.createFromParcel(data);
                    } else {
                        clipData = null;
                    }
                    setPrimaryClip(clipData, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getPrimaryClip /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    ClipData _result2 = getPrimaryClip(data.readString());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_setPrimaryClip);
                        _result2.writeToParcel(reply, TRANSACTION_setPrimaryClip);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getPrimaryClipDescription /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    ClipDescription _result3 = getPrimaryClipDescription(data.readString());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_setPrimaryClip);
                        _result3.writeToParcel(reply, TRANSACTION_setPrimaryClip);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_hasPrimaryClip /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = hasPrimaryClip(data.readString());
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_setPrimaryClip;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_addPrimaryClipChangedListener /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    addPrimaryClipChangedListener(android.content.IOnPrimaryClipChangedListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removePrimaryClipChangedListener /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    removePrimaryClipChangedListener(android.content.IOnPrimaryClipChangedListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_hasClipboardText /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = hasClipboardText(data.readString());
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_setPrimaryClip;
                    }
                    reply.writeInt(i);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addPrimaryClipChangedListener(IOnPrimaryClipChangedListener iOnPrimaryClipChangedListener, String str) throws RemoteException;

    ClipData getPrimaryClip(String str) throws RemoteException;

    ClipDescription getPrimaryClipDescription(String str) throws RemoteException;

    boolean hasClipboardText(String str) throws RemoteException;

    boolean hasPrimaryClip(String str) throws RemoteException;

    void removePrimaryClipChangedListener(IOnPrimaryClipChangedListener iOnPrimaryClipChangedListener) throws RemoteException;

    void setPrimaryClip(ClipData clipData, String str) throws RemoteException;
}

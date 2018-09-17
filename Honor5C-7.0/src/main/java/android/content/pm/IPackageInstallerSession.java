package android.content.pm;

import android.content.IntentSender;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public interface IPackageInstallerSession extends IInterface {

    public static abstract class Stub extends Binder implements IPackageInstallerSession {
        private static final String DESCRIPTOR = "android.content.pm.IPackageInstallerSession";
        static final int TRANSACTION_abandon = 9;
        static final int TRANSACTION_addClientProgress = 2;
        static final int TRANSACTION_close = 7;
        static final int TRANSACTION_commit = 8;
        static final int TRANSACTION_getNames = 3;
        static final int TRANSACTION_openRead = 5;
        static final int TRANSACTION_openWrite = 4;
        static final int TRANSACTION_removeSplit = 6;
        static final int TRANSACTION_setClientProgress = 1;

        private static class Proxy implements IPackageInstallerSession {
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

            public void setClientProgress(float progress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(progress);
                    this.mRemote.transact(Stub.TRANSACTION_setClientProgress, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addClientProgress(float progress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(progress);
                    this.mRemote.transact(Stub.TRANSACTION_addClientProgress, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getNames() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getNames, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor openWrite(String name, long offsetBytes, long lengthBytes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParcelFileDescriptor parcelFileDescriptor;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeLong(offsetBytes);
                    _data.writeLong(lengthBytes);
                    this.mRemote.transact(Stub.TRANSACTION_openWrite, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parcelFileDescriptor;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor openRead(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParcelFileDescriptor parcelFileDescriptor;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    this.mRemote.transact(Stub.TRANSACTION_openRead, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parcelFileDescriptor;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeSplit(String splitName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(splitName);
                    this.mRemote.transact(Stub.TRANSACTION_removeSplit, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void close() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_close, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void commit(IntentSender statusReceiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (statusReceiver != null) {
                        _data.writeInt(Stub.TRANSACTION_setClientProgress);
                        statusReceiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_commit, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void abandon() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_abandon, _data, _reply, 0);
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

        public static IPackageInstallerSession asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPackageInstallerSession)) {
                return new Proxy(obj);
            }
            return (IPackageInstallerSession) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ParcelFileDescriptor _result;
            switch (code) {
                case TRANSACTION_setClientProgress /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    setClientProgress(data.readFloat());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addClientProgress /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    addClientProgress(data.readFloat());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getNames /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    String[] _result2 = getNames();
                    reply.writeNoException();
                    reply.writeStringArray(_result2);
                    return true;
                case TRANSACTION_openWrite /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = openWrite(data.readString(), data.readLong(), data.readLong());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_setClientProgress);
                        _result.writeToParcel(reply, TRANSACTION_setClientProgress);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_openRead /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = openRead(data.readString());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_setClientProgress);
                        _result.writeToParcel(reply, TRANSACTION_setClientProgress);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_removeSplit /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeSplit(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_close /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    close();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_commit /*8*/:
                    IntentSender intentSender;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intentSender = (IntentSender) IntentSender.CREATOR.createFromParcel(data);
                    } else {
                        intentSender = null;
                    }
                    commit(intentSender);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_abandon /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    abandon();
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

    void abandon() throws RemoteException;

    void addClientProgress(float f) throws RemoteException;

    void close() throws RemoteException;

    void commit(IntentSender intentSender) throws RemoteException;

    String[] getNames() throws RemoteException;

    ParcelFileDescriptor openRead(String str) throws RemoteException;

    ParcelFileDescriptor openWrite(String str, long j, long j2) throws RemoteException;

    void removeSplit(String str) throws RemoteException;

    void setClientProgress(float f) throws RemoteException;
}

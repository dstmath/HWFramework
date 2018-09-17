package android.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAssetAtlas extends IInterface {

    public static abstract class Stub extends Binder implements IAssetAtlas {
        private static final String DESCRIPTOR = "android.view.IAssetAtlas";
        static final int TRANSACTION_getBuffer = 2;
        static final int TRANSACTION_getMap = 3;
        static final int TRANSACTION_isCompatible = 1;

        private static class Proxy implements IAssetAtlas {
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

            public boolean isCompatible(int ppid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ppid);
                    this.mRemote.transact(Stub.TRANSACTION_isCompatible, _data, _reply, 0);
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

            public GraphicBuffer getBuffer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    GraphicBuffer graphicBuffer;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getBuffer, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        graphicBuffer = (GraphicBuffer) GraphicBuffer.CREATOR.createFromParcel(_reply);
                    } else {
                        graphicBuffer = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return graphicBuffer;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long[] getMap() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getMap, _data, _reply, 0);
                    _reply.readException();
                    long[] _result = _reply.createLongArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAssetAtlas asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAssetAtlas)) {
                return new Proxy(obj);
            }
            return (IAssetAtlas) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            switch (code) {
                case TRANSACTION_isCompatible /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = isCompatible(data.readInt());
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_isCompatible;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_getBuffer /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    GraphicBuffer _result2 = getBuffer();
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_isCompatible);
                        _result2.writeToParcel(reply, TRANSACTION_isCompatible);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getMap /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    long[] _result3 = getMap();
                    reply.writeNoException();
                    reply.writeLongArray(_result3);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    GraphicBuffer getBuffer() throws RemoteException;

    long[] getMap() throws RemoteException;

    boolean isCompatible(int i) throws RemoteException;
}

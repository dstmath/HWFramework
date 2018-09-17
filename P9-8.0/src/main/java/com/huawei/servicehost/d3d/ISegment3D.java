package com.huawei.servicehost.d3d;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISegment3D extends IInterface {
    public static final int ATTR_BODY = 3;
    public static final int ATTR_CLOTHES = 4;
    public static final int ATTR_DECORATION = 5;
    public static final int ATTR_HAIR = 1;
    public static final int ATTR_HEAD = 2;
    public static final int ATTR_QY_HAIR = 6;
    public static final int ATTR_QY_HEAD = 7;
    public static final int ATTR_QY_OTHER = 8;

    public static abstract class Stub extends Binder implements ISegment3D {
        private static final String DESCRIPTOR = "com.huawei.servicehost.d3d.ISegment3D";
        static final int TRANSACTION_addTextureJpeg = 11;
        static final int TRANSACTION_getAttribute = 1;
        static final int TRANSACTION_getMtlFile = 7;
        static final int TRANSACTION_getName = 3;
        static final int TRANSACTION_getObjFile = 5;
        static final int TRANSACTION_getTextureJpeg = 10;
        static final int TRANSACTION_getTextureJpegCount = 9;
        static final int TRANSACTION_release = 13;
        static final int TRANSACTION_removeTextureJpeg = 12;
        static final int TRANSACTION_setAttribute = 2;
        static final int TRANSACTION_setMtlFile = 8;
        static final int TRANSACTION_setName = 4;
        static final int TRANSACTION_setObjFile = 6;

        private static class Proxy implements ISegment3D {
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

            public int getAttribute() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAttribute(int attribute) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(attribute);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setName(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IObjFile getObjFile() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    IObjFile _result = com.huawei.servicehost.d3d.IObjFile.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setObjFile(IObjFile file) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (file != null) {
                        iBinder = file.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IMtlFile getMtlFile() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    IMtlFile _result = com.huawei.servicehost.d3d.IMtlFile.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setMtlFile(IMtlFile file) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (file != null) {
                        iBinder = file.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getTextureJpegCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getTextureJpegCount, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ITextureJpeg getTextureJpeg(int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    ITextureJpeg _result = com.huawei.servicehost.d3d.ITextureJpeg.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addTextureJpeg(ITextureJpeg textureJpeg) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (textureJpeg != null) {
                        iBinder = textureJpeg.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeTextureJpeg(int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void release() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
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

        public static ISegment3D asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISegment3D)) {
                return new Proxy(obj);
            }
            return (ISegment3D) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAttribute();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    setAttribute(data.readInt());
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    String _result2 = getName();
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    setName(data.readString());
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    IObjFile _result3 = getObjFile();
                    reply.writeNoException();
                    reply.writeStrongBinder(_result3 != null ? _result3.asBinder() : null);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    setObjFile(com.huawei.servicehost.d3d.IObjFile.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    IMtlFile _result4 = getMtlFile();
                    reply.writeNoException();
                    reply.writeStrongBinder(_result4 != null ? _result4.asBinder() : null);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    setMtlFile(com.huawei.servicehost.d3d.IMtlFile.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getTextureJpegCount /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getTextureJpegCount();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    ITextureJpeg _result5 = getTextureJpeg(data.readInt());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result5 != null ? _result5.asBinder() : null);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    addTextureJpeg(com.huawei.servicehost.d3d.ITextureJpeg.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    removeTextureJpeg(data.readInt());
                    reply.writeNoException();
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    release();
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

    void addTextureJpeg(ITextureJpeg iTextureJpeg) throws RemoteException;

    int getAttribute() throws RemoteException;

    IMtlFile getMtlFile() throws RemoteException;

    String getName() throws RemoteException;

    IObjFile getObjFile() throws RemoteException;

    ITextureJpeg getTextureJpeg(int i) throws RemoteException;

    int getTextureJpegCount() throws RemoteException;

    void release() throws RemoteException;

    void removeTextureJpeg(int i) throws RemoteException;

    void setAttribute(int i) throws RemoteException;

    void setMtlFile(IMtlFile iMtlFile) throws RemoteException;

    void setName(String str) throws RemoteException;

    void setObjFile(IObjFile iObjFile) throws RemoteException;
}

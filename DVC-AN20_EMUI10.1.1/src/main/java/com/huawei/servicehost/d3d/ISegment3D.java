package com.huawei.servicehost.d3d;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.servicehost.d3d.IMtlFile;
import com.huawei.servicehost.d3d.IObjFile;
import com.huawei.servicehost.d3d.ITextureJpeg;

public interface ISegment3D extends IInterface {
    public static final int ATTR_BODY = 3;
    public static final int ATTR_CLOTHES = 4;
    public static final int ATTR_DECORATION = 5;
    public static final int ATTR_HAIR = 1;
    public static final int ATTR_HEAD = 2;
    public static final int ATTR_QY_HAIR = 6;
    public static final int ATTR_QY_HEAD = 7;
    public static final int ATTR_QY_OTHER = 8;

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

    public static class Default implements ISegment3D {
        @Override // com.huawei.servicehost.d3d.ISegment3D
        public int getAttribute() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.servicehost.d3d.ISegment3D
        public void setAttribute(int attribute) throws RemoteException {
        }

        @Override // com.huawei.servicehost.d3d.ISegment3D
        public String getName() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.d3d.ISegment3D
        public void setName(String name) throws RemoteException {
        }

        @Override // com.huawei.servicehost.d3d.ISegment3D
        public IObjFile getObjFile() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.d3d.ISegment3D
        public void setObjFile(IObjFile file) throws RemoteException {
        }

        @Override // com.huawei.servicehost.d3d.ISegment3D
        public IMtlFile getMtlFile() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.d3d.ISegment3D
        public void setMtlFile(IMtlFile file) throws RemoteException {
        }

        @Override // com.huawei.servicehost.d3d.ISegment3D
        public int getTextureJpegCount() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.servicehost.d3d.ISegment3D
        public ITextureJpeg getTextureJpeg(int index) throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.d3d.ISegment3D
        public void addTextureJpeg(ITextureJpeg textureJpeg) throws RemoteException {
        }

        @Override // com.huawei.servicehost.d3d.ISegment3D
        public void removeTextureJpeg(int index) throws RemoteException {
        }

        @Override // com.huawei.servicehost.d3d.ISegment3D
        public void release() throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

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

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                IBinder iBinder = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getAttribute();
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
                        if (_result3 != null) {
                            iBinder = _result3.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        setObjFile(IObjFile.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        IMtlFile _result4 = getMtlFile();
                        reply.writeNoException();
                        if (_result4 != null) {
                            iBinder = _result4.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        setMtlFile(IMtlFile.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getTextureJpegCount /*{ENCODED_INT: 9}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getTextureJpegCount();
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        ITextureJpeg _result6 = getTextureJpeg(data.readInt());
                        reply.writeNoException();
                        if (_result6 != null) {
                            iBinder = _result6.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        addTextureJpeg(ITextureJpeg.Stub.asInterface(data.readStrongBinder()));
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
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ISegment3D {
            public static ISegment3D sDefaultImpl;
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

            @Override // com.huawei.servicehost.d3d.ISegment3D
            public int getAttribute() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAttribute();
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

            @Override // com.huawei.servicehost.d3d.ISegment3D
            public void setAttribute(int attribute) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(attribute);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAttribute(attribute);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.d3d.ISegment3D
            public String getName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getName();
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

            @Override // com.huawei.servicehost.d3d.ISegment3D
            public void setName(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setName(name);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.d3d.ISegment3D
            public IObjFile getObjFile() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getObjFile();
                    }
                    _reply.readException();
                    IObjFile _result = IObjFile.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.d3d.ISegment3D
            public void setObjFile(IObjFile file) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(file != null ? file.asBinder() : null);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setObjFile(file);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.d3d.ISegment3D
            public IMtlFile getMtlFile() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMtlFile();
                    }
                    _reply.readException();
                    IMtlFile _result = IMtlFile.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.d3d.ISegment3D
            public void setMtlFile(IMtlFile file) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(file != null ? file.asBinder() : null);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMtlFile(file);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.d3d.ISegment3D
            public int getTextureJpegCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getTextureJpegCount, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTextureJpegCount();
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

            @Override // com.huawei.servicehost.d3d.ISegment3D
            public ITextureJpeg getTextureJpeg(int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTextureJpeg(index);
                    }
                    _reply.readException();
                    ITextureJpeg _result = ITextureJpeg.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.d3d.ISegment3D
            public void addTextureJpeg(ITextureJpeg textureJpeg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(textureJpeg != null ? textureJpeg.asBinder() : null);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addTextureJpeg(textureJpeg);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.d3d.ISegment3D
            public void removeTextureJpeg(int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeTextureJpeg(index);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.d3d.ISegment3D
            public void release() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().release();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISegment3D impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISegment3D getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

package com.huawei.servicehost.d3d;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.servicehost.ImageWrap;

public interface ISegmentData extends IInterface {
    public static final int ATTR_BONE = 1;
    public static final int ATTR_CHARACTER = 4;
    public static final int ATTR_INPUTS = 3;
    public static final int ATTR_MORPH = 2;

    void addFile(ImageWrap imageWrap) throws RemoteException;

    int getAttribute() throws RemoteException;

    ImageWrap getFile(int i) throws RemoteException;

    int getFileCount() throws RemoteException;

    String getName() throws RemoteException;

    void release() throws RemoteException;

    void removeFile(int i) throws RemoteException;

    void setAttribute(int i) throws RemoteException;

    void setName(String str) throws RemoteException;

    public static class Default implements ISegmentData {
        @Override // com.huawei.servicehost.d3d.ISegmentData
        public int getAttribute() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.servicehost.d3d.ISegmentData
        public void setAttribute(int attribute) throws RemoteException {
        }

        @Override // com.huawei.servicehost.d3d.ISegmentData
        public String getName() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.d3d.ISegmentData
        public void setName(String name) throws RemoteException {
        }

        @Override // com.huawei.servicehost.d3d.ISegmentData
        public int getFileCount() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.servicehost.d3d.ISegmentData
        public ImageWrap getFile(int index) throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.d3d.ISegmentData
        public void addFile(ImageWrap file) throws RemoteException {
        }

        @Override // com.huawei.servicehost.d3d.ISegmentData
        public void removeFile(int index) throws RemoteException {
        }

        @Override // com.huawei.servicehost.d3d.ISegmentData
        public void release() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISegmentData {
        private static final String DESCRIPTOR = "com.huawei.servicehost.d3d.ISegmentData";
        static final int TRANSACTION_addFile = 7;
        static final int TRANSACTION_getAttribute = 1;
        static final int TRANSACTION_getFile = 6;
        static final int TRANSACTION_getFileCount = 5;
        static final int TRANSACTION_getName = 3;
        static final int TRANSACTION_release = 9;
        static final int TRANSACTION_removeFile = 8;
        static final int TRANSACTION_setAttribute = 2;
        static final int TRANSACTION_setName = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISegmentData asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISegmentData)) {
                return new Proxy(obj);
            }
            return (ISegmentData) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ImageWrap _arg0;
            if (code != 1598968902) {
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
                        int _result3 = getFileCount();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        ImageWrap _result4 = getFile(data.readInt());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImageWrap.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        addFile(_arg0);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        removeFile(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_release /* 9 */:
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
        public static class Proxy implements ISegmentData {
            public static ISegmentData sDefaultImpl;
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

            @Override // com.huawei.servicehost.d3d.ISegmentData
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

            @Override // com.huawei.servicehost.d3d.ISegmentData
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

            @Override // com.huawei.servicehost.d3d.ISegmentData
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

            @Override // com.huawei.servicehost.d3d.ISegmentData
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

            @Override // com.huawei.servicehost.d3d.ISegmentData
            public int getFileCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFileCount();
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

            @Override // com.huawei.servicehost.d3d.ISegmentData
            public ImageWrap getFile(int index) throws RemoteException {
                ImageWrap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFile(index);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ImageWrap.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.servicehost.d3d.ISegmentData
            public void addFile(ImageWrap file) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (file != null) {
                        _data.writeInt(1);
                        file.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addFile(file);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.d3d.ISegmentData
            public void removeFile(int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeFile(index);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.d3d.ISegmentData
            public void release() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(Stub.TRANSACTION_release, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
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

        public static boolean setDefaultImpl(ISegmentData impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISegmentData getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

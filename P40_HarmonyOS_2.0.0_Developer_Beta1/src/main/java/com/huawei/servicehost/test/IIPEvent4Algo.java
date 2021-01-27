package com.huawei.servicehost.test;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.Surface;
import com.huawei.servicehost.ImageWrap;

public interface IIPEvent4Algo extends IInterface {
    ImageWrap getInputImage() throws RemoteException;

    ImageWrap getOutputImage() throws RemoteException;

    int getPriority() throws RemoteException;

    String getResult() throws RemoteException;

    Surface getSurface() throws RemoteException;

    void setInputImage(ImageWrap imageWrap) throws RemoteException;

    void setOutputImage(ImageWrap imageWrap) throws RemoteException;

    void setPriority(int i) throws RemoteException;

    void setResult(String str) throws RemoteException;

    void setSurface(Surface surface) throws RemoteException;

    public static class Default implements IIPEvent4Algo {
        @Override // com.huawei.servicehost.test.IIPEvent4Algo
        public ImageWrap getInputImage() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.test.IIPEvent4Algo
        public void setInputImage(ImageWrap val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.test.IIPEvent4Algo
        public ImageWrap getOutputImage() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.test.IIPEvent4Algo
        public void setOutputImage(ImageWrap val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.test.IIPEvent4Algo
        public int getPriority() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.servicehost.test.IIPEvent4Algo
        public void setPriority(int val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.test.IIPEvent4Algo
        public String getResult() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.test.IIPEvent4Algo
        public void setResult(String val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.test.IIPEvent4Algo
        public Surface getSurface() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.test.IIPEvent4Algo
        public void setSurface(Surface val) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIPEvent4Algo {
        private static final String DESCRIPTOR = "com.huawei.servicehost.test.IIPEvent4Algo";
        static final int TRANSACTION_getInputImage = 1;
        static final int TRANSACTION_getOutputImage = 3;
        static final int TRANSACTION_getPriority = 5;
        static final int TRANSACTION_getResult = 7;
        static final int TRANSACTION_getSurface = 9;
        static final int TRANSACTION_setInputImage = 2;
        static final int TRANSACTION_setOutputImage = 4;
        static final int TRANSACTION_setPriority = 6;
        static final int TRANSACTION_setResult = 8;
        static final int TRANSACTION_setSurface = 10;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIPEvent4Algo asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIPEvent4Algo)) {
                return new Proxy(obj);
            }
            return (IIPEvent4Algo) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ImageWrap _arg0;
            ImageWrap _arg02;
            Surface _arg03;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        ImageWrap _result = getInputImage();
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImageWrap.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        setInputImage(_arg0);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        ImageWrap _result2 = getOutputImage();
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ImageWrap.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        setOutputImage(_arg02);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getPriority();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        setPriority(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        String _result4 = getResult();
                        reply.writeNoException();
                        reply.writeString(_result4);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        setResult(data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getSurface /* 9 */:
                        data.enforceInterface(DESCRIPTOR);
                        Surface _result5 = getSurface();
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = (Surface) Surface.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        setSurface(_arg03);
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
        public static class Proxy implements IIPEvent4Algo {
            public static IIPEvent4Algo sDefaultImpl;
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

            @Override // com.huawei.servicehost.test.IIPEvent4Algo
            public ImageWrap getInputImage() throws RemoteException {
                ImageWrap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInputImage();
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

            @Override // com.huawei.servicehost.test.IIPEvent4Algo
            public void setInputImage(ImageWrap val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (val != null) {
                        _data.writeInt(1);
                        val.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setInputImage(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.test.IIPEvent4Algo
            public ImageWrap getOutputImage() throws RemoteException {
                ImageWrap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOutputImage();
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

            @Override // com.huawei.servicehost.test.IIPEvent4Algo
            public void setOutputImage(ImageWrap val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (val != null) {
                        _data.writeInt(1);
                        val.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setOutputImage(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.test.IIPEvent4Algo
            public int getPriority() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPriority();
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

            @Override // com.huawei.servicehost.test.IIPEvent4Algo
            public void setPriority(int val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(val);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPriority(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.test.IIPEvent4Algo
            public String getResult() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getResult();
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

            @Override // com.huawei.servicehost.test.IIPEvent4Algo
            public void setResult(String val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(val);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setResult(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.test.IIPEvent4Algo
            public Surface getSurface() throws RemoteException {
                Surface _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getSurface, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSurface();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Surface) Surface.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.servicehost.test.IIPEvent4Algo
            public void setSurface(Surface val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (val != null) {
                        _data.writeInt(1);
                        val.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSurface(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IIPEvent4Algo impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIPEvent4Algo getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

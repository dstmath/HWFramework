package com.huawei.servicehost;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.Surface;
import com.huawei.servicehost.IFrameAvailableListener;

public interface IImageConsumer extends IInterface {

    public static abstract class Stub extends Binder implements IImageConsumer {
        private static final String DESCRIPTOR = "com.huawei.servicehost.IImageConsumer";
        static final int TRANSACTION_acquire = 1;
        static final int TRANSACTION_attach = 4;
        static final int TRANSACTION_close = 7;
        static final int TRANSACTION_detach = 5;
        static final int TRANSACTION_getImageDescriptor = 8;
        static final int TRANSACTION_getProducer = 10;
        static final int TRANSACTION_open = 6;
        static final int TRANSACTION_open1 = 11;
        static final int TRANSACTION_release = 2;
        static final int TRANSACTION_setFrameAvailableListener = 9;
        static final int TRANSACTION_swap = 3;

        private static class Proxy implements IImageConsumer {
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

            public ImageWrap acquire() throws RemoteException {
                ImageWrap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ImageWrap.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void release(ImageWrap val) throws RemoteException {
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
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ImageWrap swap(ImageWrap val) throws RemoteException {
                ImageWrap _result;
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
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ImageWrap.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void attach(ImageWrap val) throws RemoteException {
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
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ImageWrap detach() throws RemoteException {
                ImageWrap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ImageWrap.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int open(ImageDescriptor val) throws RemoteException {
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
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ImageDescriptor getImageDescriptor() throws RemoteException {
                ImageDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ImageDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setFrameAvailableListener(IFrameAvailableListener val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(val != null ? val.asBinder() : null);
                    this.mRemote.transact(Stub.TRANSACTION_setFrameAvailableListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Surface getProducer() throws RemoteException {
                Surface _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Surface) Surface.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int open1(ImageDescriptor val, int cBuf) throws RemoteException {
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
                    _data.writeInt(cBuf);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImageConsumer asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImageConsumer)) {
                return new Proxy(obj);
            }
            return (IImageConsumer) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v0, resolved type: com.huawei.servicehost.ImageWrap} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v2, resolved type: com.huawei.servicehost.ImageWrap} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v5, resolved type: com.huawei.servicehost.ImageWrap} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v9, resolved type: com.huawei.servicehost.ImageWrap} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v13, resolved type: com.huawei.servicehost.ImageWrap} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v11, resolved type: com.huawei.servicehost.ImageDescriptor} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v19, resolved type: com.huawei.servicehost.ImageWrap} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v17, resolved type: com.huawei.servicehost.ImageDescriptor} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v23, resolved type: com.huawei.servicehost.ImageWrap} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v24, resolved type: com.huawei.servicehost.ImageWrap} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v25, resolved type: com.huawei.servicehost.ImageWrap} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v26, resolved type: com.huawei.servicehost.ImageWrap} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v27, resolved type: com.huawei.servicehost.ImageWrap} */
        /* JADX WARNING: type inference failed for: r3v16, types: [com.huawei.servicehost.ImageDescriptor] */
        /* JADX WARNING: type inference failed for: r3v22, types: [com.huawei.servicehost.ImageDescriptor] */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                ImageWrap _arg0 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        ImageWrap _result = acquire();
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
                        }
                        release(_arg0);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImageWrap.CREATOR.createFromParcel(data);
                        }
                        ImageWrap _result2 = swap(_arg0);
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
                            _arg0 = ImageWrap.CREATOR.createFromParcel(data);
                        }
                        attach(_arg0);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        ImageWrap _result3 = detach();
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImageDescriptor.CREATOR.createFromParcel(data);
                        }
                        int _result4 = open(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        close();
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        ImageDescriptor _result5 = getImageDescriptor();
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_setFrameAvailableListener /*9*/:
                        data.enforceInterface(DESCRIPTOR);
                        setFrameAvailableListener(IFrameAvailableListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        Surface _result6 = getProducer();
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ImageDescriptor.CREATOR.createFromParcel(data);
                        }
                        int _result7 = open1(_arg0, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result7);
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

    ImageWrap acquire() throws RemoteException;

    void attach(ImageWrap imageWrap) throws RemoteException;

    void close() throws RemoteException;

    ImageWrap detach() throws RemoteException;

    ImageDescriptor getImageDescriptor() throws RemoteException;

    Surface getProducer() throws RemoteException;

    int open(ImageDescriptor imageDescriptor) throws RemoteException;

    int open1(ImageDescriptor imageDescriptor, int i) throws RemoteException;

    void release(ImageWrap imageWrap) throws RemoteException;

    void setFrameAvailableListener(IFrameAvailableListener iFrameAvailableListener) throws RemoteException;

    ImageWrap swap(ImageWrap imageWrap) throws RemoteException;
}

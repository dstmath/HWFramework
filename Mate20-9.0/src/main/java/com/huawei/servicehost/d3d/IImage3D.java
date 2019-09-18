package com.huawei.servicehost.d3d;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.servicehost.ImageWrap;
import com.huawei.servicehost.d3d.ISegment3D;
import com.huawei.servicehost.d3d.ISegmentData;

public interface IImage3D extends IInterface {
    public static final int SEX_FEMALE = 0;
    public static final int SEX_MALE = 1;

    public static abstract class Stub extends Binder implements IImage3D {
        private static final String DESCRIPTOR = "com.huawei.servicehost.d3d.IImage3D";
        static final int TRANSACTION_addSegment3D = 5;
        static final int TRANSACTION_addSegmentData = 9;
        static final int TRANSACTION_getFileSource = 11;
        static final int TRANSACTION_getFrontImage = 1;
        static final int TRANSACTION_getSegment3D = 4;
        static final int TRANSACTION_getSegment3DCount = 3;
        static final int TRANSACTION_getSegmentData = 8;
        static final int TRANSACTION_getSegmentDataCount = 7;
        static final int TRANSACTION_getSex = 16;
        static final int TRANSACTION_read = 13;
        static final int TRANSACTION_release = 15;
        static final int TRANSACTION_removeSegment3D = 6;
        static final int TRANSACTION_removeSegmentData = 10;
        static final int TRANSACTION_save = 14;
        static final int TRANSACTION_setFileSource = 12;
        static final int TRANSACTION_setFrontImage = 2;
        static final int TRANSACTION_setSex = 17;

        private static class Proxy implements IImage3D {
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

            public ImageWrap getFrontImage() throws RemoteException {
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

            public void setFrontImage(ImageWrap image) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (image != null) {
                        _data.writeInt(1);
                        image.writeToParcel(_data, 0);
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

            public int getSegment3DCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ISegment3D getSegment3D(int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return ISegment3D.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addSegment3D(ISegment3D segment) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(segment != null ? segment.asBinder() : null);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeSegment3D(int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSegmentDataCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ISegmentData getSegmentData(int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return ISegmentData.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addSegmentData(ISegmentData segment) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(segment != null ? segment.asBinder() : null);
                    this.mRemote.transact(Stub.TRANSACTION_addSegmentData, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeSegmentData(int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getFileSource() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setFileSource(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void read(String fileName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fileName);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void save(String fileName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fileName);
                    this.mRemote.transact(14, _data, _reply, 0);
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
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSex() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSex(int val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(val);
                    this.mRemote.transact(17, _data, _reply, 0);
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

        public static IImage3D asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImage3D)) {
                return new Proxy(obj);
            }
            return (IImage3D) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: com.huawei.servicehost.ImageWrap} */
        /* JADX WARNING: type inference failed for: r1v1 */
        /* JADX WARNING: type inference failed for: r1v8, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r1v14, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r1v26 */
        /* JADX WARNING: type inference failed for: r1v27 */
        /* JADX WARNING: type inference failed for: r1v28 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                ? _arg0 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        ImageWrap _result = getFrontImage();
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
                        setFrontImage(_arg0);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getSegment3DCount();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        ISegment3D _result3 = getSegment3D(data.readInt());
                        reply.writeNoException();
                        if (_result3 != null) {
                            _arg0 = _result3.asBinder();
                        }
                        reply.writeStrongBinder(_arg0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        addSegment3D(ISegment3D.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        removeSegment3D(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getSegmentDataCount();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        ISegmentData _result5 = getSegmentData(data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            _arg0 = _result5.asBinder();
                        }
                        reply.writeStrongBinder(_arg0);
                        return true;
                    case TRANSACTION_addSegmentData /*9*/:
                        data.enforceInterface(DESCRIPTOR);
                        addSegmentData(ISegmentData.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        removeSegmentData(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = getFileSource();
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        setFileSource(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        read(data.readString());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        save(data.readString());
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        release();
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = getSex();
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        setSex(data.readInt());
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
    }

    void addSegment3D(ISegment3D iSegment3D) throws RemoteException;

    void addSegmentData(ISegmentData iSegmentData) throws RemoteException;

    int getFileSource() throws RemoteException;

    ImageWrap getFrontImage() throws RemoteException;

    ISegment3D getSegment3D(int i) throws RemoteException;

    int getSegment3DCount() throws RemoteException;

    ISegmentData getSegmentData(int i) throws RemoteException;

    int getSegmentDataCount() throws RemoteException;

    int getSex() throws RemoteException;

    void read(String str) throws RemoteException;

    void release() throws RemoteException;

    void removeSegment3D(int i) throws RemoteException;

    void removeSegmentData(int i) throws RemoteException;

    void save(String str) throws RemoteException;

    void setFileSource(int i) throws RemoteException;

    void setFrontImage(ImageWrap imageWrap) throws RemoteException;

    void setSex(int i) throws RemoteException;
}

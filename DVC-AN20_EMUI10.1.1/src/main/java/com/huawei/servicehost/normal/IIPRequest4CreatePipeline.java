package com.huawei.servicehost.normal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.Surface;
import com.huawei.servicehost.ImageDescriptor;

public interface IIPRequest4CreatePipeline extends IInterface {
    IBinder getCamera1CapImageConsumer() throws RemoteException;

    Surface getCamera1CapSurface() throws RemoteException;

    IBinder getCamera1ImageConsumer() throws RemoteException;

    Surface getCamera1Surface() throws RemoteException;

    IBinder getCamera1VideoImageConsumer() throws RemoteException;

    Surface getCamera1VideoSurface() throws RemoteException;

    IBinder getCamera2CapImageConsumer() throws RemoteException;

    Surface getCamera2CapSurface() throws RemoteException;

    IBinder getCamera2ImageConsumer() throws RemoteException;

    Surface getCamera2Surface() throws RemoteException;

    IBinder getCamera2VideoImageConsumer() throws RemoteException;

    Surface getCamera2VideoSurface() throws RemoteException;

    IBinder getDmapImageConsumer() throws RemoteException;

    Surface getDmapSurface() throws RemoteException;

    String getLayout() throws RemoteException;

    IBinder getMetadataImageConsumer() throws RemoteException;

    Surface getMetadataSurface() throws RemoteException;

    void setCamera1CapImageConsumer(IBinder iBinder) throws RemoteException;

    void setCamera1ImageConsumer(IBinder iBinder) throws RemoteException;

    void setCamera1VideoImageConsumer(IBinder iBinder) throws RemoteException;

    void setCamera1VideoSurface(Surface surface) throws RemoteException;

    void setCamera2CapImageConsumer(IBinder iBinder) throws RemoteException;

    void setCamera2ImageConsumer(IBinder iBinder) throws RemoteException;

    void setCamera2VideoImageConsumer(IBinder iBinder) throws RemoteException;

    void setCamera2VideoSurface(Surface surface) throws RemoteException;

    void setCaptureFormat(ImageDescriptor imageDescriptor) throws RemoteException;

    void setCaptureFormatLine2(ImageDescriptor imageDescriptor) throws RemoteException;

    void setDmapImageConsumer(IBinder iBinder) throws RemoteException;

    void setLayout(String str) throws RemoteException;

    void setMetadataImageConsumer(IBinder iBinder) throws RemoteException;

    void setPreview1Surface(Surface surface) throws RemoteException;

    void setPreview2Surface(Surface surface) throws RemoteException;

    void setPreviewFormat(ImageDescriptor imageDescriptor) throws RemoteException;

    void setPreviewFormatLine2(ImageDescriptor imageDescriptor) throws RemoteException;

    void setVideoFormat(ImageDescriptor imageDescriptor) throws RemoteException;

    void setVideoFormatLine2(ImageDescriptor imageDescriptor) throws RemoteException;

    public static class Default implements IIPRequest4CreatePipeline {
        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public String getLayout() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public void setLayout(String val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public Surface getCamera1Surface() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public Surface getCamera2Surface() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public void setPreview1Surface(Surface val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public void setPreview2Surface(Surface val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public Surface getMetadataSurface() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public Surface getDmapSurface() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public Surface getCamera1CapSurface() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public Surface getCamera2CapSurface() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public void setCamera1ImageConsumer(IBinder val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public void setCamera2ImageConsumer(IBinder val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public void setMetadataImageConsumer(IBinder val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public void setDmapImageConsumer(IBinder val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public void setCamera1CapImageConsumer(IBinder val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public void setCamera2CapImageConsumer(IBinder val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public IBinder getCamera1ImageConsumer() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public IBinder getCamera2ImageConsumer() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public IBinder getMetadataImageConsumer() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public IBinder getDmapImageConsumer() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public IBinder getCamera1CapImageConsumer() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public IBinder getCamera2CapImageConsumer() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public void setPreviewFormat(ImageDescriptor val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public void setCaptureFormat(ImageDescriptor val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public void setCamera1VideoSurface(Surface val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public void setCamera2VideoSurface(Surface val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public Surface getCamera1VideoSurface() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public Surface getCamera2VideoSurface() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public void setCamera1VideoImageConsumer(IBinder val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public void setCamera2VideoImageConsumer(IBinder val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public IBinder getCamera1VideoImageConsumer() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public IBinder getCamera2VideoImageConsumer() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public void setVideoFormat(ImageDescriptor val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public void setPreviewFormatLine2(ImageDescriptor val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public void setCaptureFormatLine2(ImageDescriptor val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
        public void setVideoFormatLine2(ImageDescriptor val) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIPRequest4CreatePipeline {
        private static final String DESCRIPTOR = "com.huawei.servicehost.normal.IIPRequest4CreatePipeline";
        static final int TRANSACTION_getCamera1CapImageConsumer = 21;
        static final int TRANSACTION_getCamera1CapSurface = 9;
        static final int TRANSACTION_getCamera1ImageConsumer = 17;
        static final int TRANSACTION_getCamera1Surface = 3;
        static final int TRANSACTION_getCamera1VideoImageConsumer = 31;
        static final int TRANSACTION_getCamera1VideoSurface = 27;
        static final int TRANSACTION_getCamera2CapImageConsumer = 22;
        static final int TRANSACTION_getCamera2CapSurface = 10;
        static final int TRANSACTION_getCamera2ImageConsumer = 18;
        static final int TRANSACTION_getCamera2Surface = 4;
        static final int TRANSACTION_getCamera2VideoImageConsumer = 32;
        static final int TRANSACTION_getCamera2VideoSurface = 28;
        static final int TRANSACTION_getDmapImageConsumer = 20;
        static final int TRANSACTION_getDmapSurface = 8;
        static final int TRANSACTION_getLayout = 1;
        static final int TRANSACTION_getMetadataImageConsumer = 19;
        static final int TRANSACTION_getMetadataSurface = 7;
        static final int TRANSACTION_setCamera1CapImageConsumer = 15;
        static final int TRANSACTION_setCamera1ImageConsumer = 11;
        static final int TRANSACTION_setCamera1VideoImageConsumer = 29;
        static final int TRANSACTION_setCamera1VideoSurface = 25;
        static final int TRANSACTION_setCamera2CapImageConsumer = 16;
        static final int TRANSACTION_setCamera2ImageConsumer = 12;
        static final int TRANSACTION_setCamera2VideoImageConsumer = 30;
        static final int TRANSACTION_setCamera2VideoSurface = 26;
        static final int TRANSACTION_setCaptureFormat = 24;
        static final int TRANSACTION_setCaptureFormatLine2 = 35;
        static final int TRANSACTION_setDmapImageConsumer = 14;
        static final int TRANSACTION_setLayout = 2;
        static final int TRANSACTION_setMetadataImageConsumer = 13;
        static final int TRANSACTION_setPreview1Surface = 5;
        static final int TRANSACTION_setPreview2Surface = 6;
        static final int TRANSACTION_setPreviewFormat = 23;
        static final int TRANSACTION_setPreviewFormatLine2 = 34;
        static final int TRANSACTION_setVideoFormat = 33;
        static final int TRANSACTION_setVideoFormatLine2 = 36;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIPRequest4CreatePipeline asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIPRequest4CreatePipeline)) {
                return new Proxy(obj);
            }
            return (IIPRequest4CreatePipeline) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Surface _arg0;
            Surface _arg02;
            ImageDescriptor _arg03;
            ImageDescriptor _arg04;
            Surface _arg05;
            Surface _arg06;
            ImageDescriptor _arg07;
            ImageDescriptor _arg08;
            ImageDescriptor _arg09;
            ImageDescriptor _arg010;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getLayout();
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        setLayout(data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        Surface _result2 = getCamera1Surface();
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
                        Surface _result3 = getCamera2Surface();
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Surface) Surface.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        setPreview1Surface(_arg0);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (Surface) Surface.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        setPreview2Surface(_arg02);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        Surface _result4 = getMetadataSurface();
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        Surface _result5 = getDmapSurface();
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_getCamera1CapSurface /*{ENCODED_INT: 9}*/:
                        data.enforceInterface(DESCRIPTOR);
                        Surface _result6 = getCamera1CapSurface();
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        Surface _result7 = getCamera2CapSurface();
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        setCamera1ImageConsumer(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        setCamera2ImageConsumer(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        setMetadataImageConsumer(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        setDmapImageConsumer(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        setCamera1CapImageConsumer(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        setCamera2CapImageConsumer(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result8 = getCamera1ImageConsumer();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result8);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result9 = getCamera2ImageConsumer();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result9);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result10 = getMetadataImageConsumer();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result10);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result11 = getDmapImageConsumer();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result11);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result12 = getCamera1CapImageConsumer();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result12);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result13 = getCamera2CapImageConsumer();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result13);
                        return true;
                    case TRANSACTION_setPreviewFormat /*{ENCODED_INT: 23}*/:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = ImageDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        setPreviewFormat(_arg03);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setCaptureFormat /*{ENCODED_INT: 24}*/:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = ImageDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        setCaptureFormat(_arg04);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setCamera1VideoSurface /*{ENCODED_INT: 25}*/:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = (Surface) Surface.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        setCamera1VideoSurface(_arg05);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setCamera2VideoSurface /*{ENCODED_INT: 26}*/:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = (Surface) Surface.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        setCamera2VideoSurface(_arg06);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getCamera1VideoSurface /*{ENCODED_INT: 27}*/:
                        data.enforceInterface(DESCRIPTOR);
                        Surface _result14 = getCamera1VideoSurface();
                        reply.writeNoException();
                        if (_result14 != null) {
                            reply.writeInt(1);
                            _result14.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_getCamera2VideoSurface /*{ENCODED_INT: 28}*/:
                        data.enforceInterface(DESCRIPTOR);
                        Surface _result15 = getCamera2VideoSurface();
                        reply.writeNoException();
                        if (_result15 != null) {
                            reply.writeInt(1);
                            _result15.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_setCamera1VideoImageConsumer /*{ENCODED_INT: 29}*/:
                        data.enforceInterface(DESCRIPTOR);
                        setCamera1VideoImageConsumer(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setCamera2VideoImageConsumer /*{ENCODED_INT: 30}*/:
                        data.enforceInterface(DESCRIPTOR);
                        setCamera2VideoImageConsumer(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getCamera1VideoImageConsumer /*{ENCODED_INT: 31}*/:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result16 = getCamera1VideoImageConsumer();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result16);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result17 = getCamera2VideoImageConsumer();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result17);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = ImageDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        setVideoFormat(_arg07);
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = ImageDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg08 = null;
                        }
                        setPreviewFormatLine2(_arg08);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setCaptureFormatLine2 /*{ENCODED_INT: 35}*/:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = ImageDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg09 = null;
                        }
                        setCaptureFormatLine2(_arg09);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setVideoFormatLine2 /*{ENCODED_INT: 36}*/:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = ImageDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg010 = null;
                        }
                        setVideoFormatLine2(_arg010);
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
        public static class Proxy implements IIPRequest4CreatePipeline {
            public static IIPRequest4CreatePipeline sDefaultImpl;
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

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public String getLayout() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLayout();
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

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public void setLayout(String val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(val);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setLayout(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public Surface getCamera1Surface() throws RemoteException {
                Surface _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCamera1Surface();
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

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public Surface getCamera2Surface() throws RemoteException {
                Surface _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCamera2Surface();
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

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public void setPreview1Surface(Surface val) throws RemoteException {
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
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPreview1Surface(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public void setPreview2Surface(Surface val) throws RemoteException {
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
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPreview2Surface(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public Surface getMetadataSurface() throws RemoteException {
                Surface _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMetadataSurface();
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

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public Surface getDmapSurface() throws RemoteException {
                Surface _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDmapSurface();
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

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public Surface getCamera1CapSurface() throws RemoteException {
                Surface _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getCamera1CapSurface, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCamera1CapSurface();
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

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public Surface getCamera2CapSurface() throws RemoteException {
                Surface _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCamera2CapSurface();
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

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public void setCamera1ImageConsumer(IBinder val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(val);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCamera1ImageConsumer(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public void setCamera2ImageConsumer(IBinder val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(val);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCamera2ImageConsumer(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public void setMetadataImageConsumer(IBinder val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(val);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMetadataImageConsumer(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public void setDmapImageConsumer(IBinder val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(val);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDmapImageConsumer(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public void setCamera1CapImageConsumer(IBinder val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(val);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCamera1CapImageConsumer(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public void setCamera2CapImageConsumer(IBinder val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(val);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCamera2CapImageConsumer(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public IBinder getCamera1ImageConsumer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCamera1ImageConsumer();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public IBinder getCamera2ImageConsumer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCamera2ImageConsumer();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public IBinder getMetadataImageConsumer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMetadataImageConsumer();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public IBinder getDmapImageConsumer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDmapImageConsumer();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public IBinder getCamera1CapImageConsumer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCamera1CapImageConsumer();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public IBinder getCamera2CapImageConsumer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCamera2CapImageConsumer();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public void setPreviewFormat(ImageDescriptor val) throws RemoteException {
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
                    if (this.mRemote.transact(Stub.TRANSACTION_setPreviewFormat, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPreviewFormat(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public void setCaptureFormat(ImageDescriptor val) throws RemoteException {
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
                    if (this.mRemote.transact(Stub.TRANSACTION_setCaptureFormat, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCaptureFormat(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public void setCamera1VideoSurface(Surface val) throws RemoteException {
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
                    if (this.mRemote.transact(Stub.TRANSACTION_setCamera1VideoSurface, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCamera1VideoSurface(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public void setCamera2VideoSurface(Surface val) throws RemoteException {
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
                    if (this.mRemote.transact(Stub.TRANSACTION_setCamera2VideoSurface, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCamera2VideoSurface(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public Surface getCamera1VideoSurface() throws RemoteException {
                Surface _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getCamera1VideoSurface, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCamera1VideoSurface();
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

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public Surface getCamera2VideoSurface() throws RemoteException {
                Surface _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getCamera2VideoSurface, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCamera2VideoSurface();
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

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public void setCamera1VideoImageConsumer(IBinder val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(val);
                    if (this.mRemote.transact(Stub.TRANSACTION_setCamera1VideoImageConsumer, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCamera1VideoImageConsumer(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public void setCamera2VideoImageConsumer(IBinder val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(val);
                    if (this.mRemote.transact(Stub.TRANSACTION_setCamera2VideoImageConsumer, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCamera2VideoImageConsumer(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public IBinder getCamera1VideoImageConsumer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getCamera1VideoImageConsumer, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCamera1VideoImageConsumer();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public IBinder getCamera2VideoImageConsumer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCamera2VideoImageConsumer();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public void setVideoFormat(ImageDescriptor val) throws RemoteException {
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
                    if (this.mRemote.transact(33, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVideoFormat(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public void setPreviewFormatLine2(ImageDescriptor val) throws RemoteException {
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
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPreviewFormatLine2(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public void setCaptureFormatLine2(ImageDescriptor val) throws RemoteException {
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
                    if (this.mRemote.transact(Stub.TRANSACTION_setCaptureFormatLine2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCaptureFormatLine2(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPRequest4CreatePipeline
            public void setVideoFormatLine2(ImageDescriptor val) throws RemoteException {
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
                    if (this.mRemote.transact(Stub.TRANSACTION_setVideoFormatLine2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVideoFormatLine2(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IIPRequest4CreatePipeline impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIPRequest4CreatePipeline getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

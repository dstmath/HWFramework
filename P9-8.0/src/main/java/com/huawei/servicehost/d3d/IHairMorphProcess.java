package com.huawei.servicehost.d3d;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.servicehost.ImageWrap;

public interface IHairMorphProcess extends IInterface {

    public static abstract class Stub extends Binder implements IHairMorphProcess {
        private static final String DESCRIPTOR = "com.huawei.servicehost.d3d.IHairMorphProcess";
        static final int TRANSACTION_process = 1;
        static final int TRANSACTION_release = 2;

        private static class Proxy implements IHairMorphProcess {
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

            public boolean process(ImageWrap hairVertexInput, ImageWrap standHeadVertex, ImageWrap targetHeadVertex, ImageWrap headFixScalpLmksInput, ImageWrap hairVertexOutput) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (hairVertexInput != null) {
                        _data.writeInt(1);
                        hairVertexInput.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (standHeadVertex != null) {
                        _data.writeInt(1);
                        standHeadVertex.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (targetHeadVertex != null) {
                        _data.writeInt(1);
                        targetHeadVertex.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (headFixScalpLmksInput != null) {
                        _data.writeInt(1);
                        headFixScalpLmksInput.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (hairVertexOutput != null) {
                        _data.writeInt(1);
                        hairVertexOutput.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
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

            public void release() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
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

        public static IHairMorphProcess asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHairMorphProcess)) {
                return new Proxy(obj);
            }
            return (IHairMorphProcess) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    ImageWrap _arg0;
                    ImageWrap _arg1;
                    ImageWrap _arg2;
                    ImageWrap _arg3;
                    ImageWrap _arg4;
                    int i;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (ImageWrap) ImageWrap.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg1 = (ImageWrap) ImageWrap.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg2 = (ImageWrap) ImageWrap.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg3 = (ImageWrap) ImageWrap.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg4 = (ImageWrap) ImageWrap.CREATOR.createFromParcel(data);
                    } else {
                        _arg4 = null;
                    }
                    boolean _result = process(_arg0, _arg1, _arg2, _arg3, _arg4);
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 2:
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

    boolean process(ImageWrap imageWrap, ImageWrap imageWrap2, ImageWrap imageWrap3, ImageWrap imageWrap4, ImageWrap imageWrap5) throws RemoteException;

    void release() throws RemoteException;
}

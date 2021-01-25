package com.huawei.servicehost.d3d;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.servicehost.ImageWrap;

public interface IHairMorphProcess extends IInterface {
    boolean process(ImageWrap imageWrap, ImageWrap imageWrap2, ImageWrap imageWrap3, ImageWrap imageWrap4, ImageWrap imageWrap5) throws RemoteException;

    void release() throws RemoteException;

    public static class Default implements IHairMorphProcess {
        @Override // com.huawei.servicehost.d3d.IHairMorphProcess
        public boolean process(ImageWrap hairVertexInput, ImageWrap standHeadVertex, ImageWrap targetHeadVertex, ImageWrap headFixScalpLmksInput, ImageWrap hairVertexOutput) throws RemoteException {
            return false;
        }

        @Override // com.huawei.servicehost.d3d.IHairMorphProcess
        public void release() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHairMorphProcess {
        private static final String DESCRIPTOR = "com.huawei.servicehost.d3d.IHairMorphProcess";
        static final int TRANSACTION_process = 1;
        static final int TRANSACTION_release = 2;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ImageWrap _arg0;
            ImageWrap _arg1;
            ImageWrap _arg2;
            ImageWrap _arg3;
            ImageWrap _arg4;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = ImageWrap.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                if (data.readInt() != 0) {
                    _arg1 = ImageWrap.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                if (data.readInt() != 0) {
                    _arg2 = ImageWrap.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                if (data.readInt() != 0) {
                    _arg3 = ImageWrap.CREATOR.createFromParcel(data);
                } else {
                    _arg3 = null;
                }
                if (data.readInt() != 0) {
                    _arg4 = ImageWrap.CREATOR.createFromParcel(data);
                } else {
                    _arg4 = null;
                }
                boolean process = process(_arg0, _arg1, _arg2, _arg3, _arg4);
                reply.writeNoException();
                reply.writeInt(process ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                release();
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHairMorphProcess {
            public static IHairMorphProcess sDefaultImpl;
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

            @Override // com.huawei.servicehost.d3d.IHairMorphProcess
            public boolean process(ImageWrap hairVertexInput, ImageWrap standHeadVertex, ImageWrap targetHeadVertex, ImageWrap headFixScalpLmksInput, ImageWrap hairVertexOutput) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
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
                    try {
                        if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            if (_reply.readInt() == 0) {
                                _result = false;
                            }
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        boolean process = Stub.getDefaultImpl().process(hairVertexInput, standHeadVertex, targetHeadVertex, headFixScalpLmksInput, hairVertexOutput);
                        _reply.recycle();
                        _data.recycle();
                        return process;
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.huawei.servicehost.d3d.IHairMorphProcess
            public void release() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
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

        public static boolean setDefaultImpl(IHairMorphProcess impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHairMorphProcess getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

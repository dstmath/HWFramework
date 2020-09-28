package com.huawei.lighteffect;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ILightEffectService extends IInterface {
    public static final int VUI_STATE_ANSWERING = 4;
    public static final int VUI_STATE_COORDINATE = 7;
    public static final int VUI_STATE_FINISH = 5;
    public static final int VUI_STATE_GUI_EXIT = 6;
    public static final int VUI_STATE_LISTENING = 2;
    public static final int VUI_STATE_PROCESSING = 3;
    public static final int VUI_STATE_SLEEP = 0;
    public static final int VUI_STATE_WAKEUP = 1;

    void updateAdapterEffect(boolean z) throws RemoteException;

    void updateMusicEffect(boolean z, IBinder iBinder) throws RemoteException;

    void updateSettingEffect(boolean z) throws RemoteException;

    void updateVuiState(int i, IBinder iBinder) throws RemoteException;

    public static class Default implements ILightEffectService {
        @Override // com.huawei.lighteffect.ILightEffectService
        public void updateVuiState(int state, IBinder token) throws RemoteException {
        }

        @Override // com.huawei.lighteffect.ILightEffectService
        public void updateMusicEffect(boolean enable, IBinder token) throws RemoteException {
        }

        @Override // com.huawei.lighteffect.ILightEffectService
        public void updateSettingEffect(boolean enable) throws RemoteException {
        }

        @Override // com.huawei.lighteffect.ILightEffectService
        public void updateAdapterEffect(boolean enable) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ILightEffectService {
        private static final String DESCRIPTOR = "com.huawei.lighteffect.ILightEffectService";
        static final int TRANSACTION_updateAdapterEffect = 4;
        static final int TRANSACTION_updateMusicEffect = 2;
        static final int TRANSACTION_updateSettingEffect = 3;
        static final int TRANSACTION_updateVuiState = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILightEffectService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILightEffectService)) {
                return new Proxy(obj);
            }
            return (ILightEffectService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1) {
                boolean _arg0 = false;
                if (code == 2) {
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    updateMusicEffect(_arg0, data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                } else if (code == 3) {
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    updateSettingEffect(_arg0);
                    reply.writeNoException();
                    return true;
                } else if (code == 4) {
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    updateAdapterEffect(_arg0);
                    reply.writeNoException();
                    return true;
                } else if (code != 1598968902) {
                    return super.onTransact(code, data, reply, flags);
                } else {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
            } else {
                data.enforceInterface(DESCRIPTOR);
                updateVuiState(data.readInt(), data.readStrongBinder());
                reply.writeNoException();
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ILightEffectService {
            public static ILightEffectService sDefaultImpl;
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

            @Override // com.huawei.lighteffect.ILightEffectService
            public void updateVuiState(int state, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateVuiState(state, token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lighteffect.ILightEffectService
            public void updateMusicEffect(boolean enable, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateMusicEffect(enable, token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lighteffect.ILightEffectService
            public void updateSettingEffect(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateSettingEffect(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lighteffect.ILightEffectService
            public void updateAdapterEffect(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateAdapterEffect(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ILightEffectService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ILightEffectService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

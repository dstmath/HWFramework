package com.android.internal.telephony;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.internal.telephony.IHwCommonPhoneCallback;
import com.huawei.internal.telephony.vsim.IGetVsimServiceCallback;

public interface IHwTelephonyInner extends IInterface {
    void blockingGetVsimService(IGetVsimServiceCallback iGetVsimServiceCallback) throws RemoteException;

    int getLevelForSa(int i, int i2, int i3) throws RemoteException;

    int getNetworkMode(int i) throws RemoteException;

    int getPlatformSupportVsimVer(int i) throws RemoteException;

    String getRegPlmn(int i) throws RemoteException;

    int getRrcConnectionState(int i) throws RemoteException;

    int getVsimUserReservedSubId() throws RemoteException;

    void handleMessageForServiceEx(Message message) throws RemoteException;

    boolean isBlockNonCustomSlot(int i, int i2) throws RemoteException;

    boolean isCustomSmart() throws RemoteException;

    boolean isSmartCard(int i) throws RemoteException;

    boolean isVsimEnabledByDatabase() throws RemoteException;

    boolean registerForRadioStateChanged(IHwCommonPhoneCallback iHwCommonPhoneCallback) throws RemoteException;

    void setSimPowerStateForSlot(int i, int i2, Message message) throws RemoteException;

    boolean setVsimUserReservedSubId(int i) throws RemoteException;

    boolean unregisterForRadioStateChanged(IHwCommonPhoneCallback iHwCommonPhoneCallback) throws RemoteException;

    public static class Default implements IHwTelephonyInner {
        @Override // com.android.internal.telephony.IHwTelephonyInner
        public int getLevelForSa(int phoneId, int nrLevel, int primaryLevel) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephonyInner
        public int getRrcConnectionState(int slotId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephonyInner
        public int getPlatformSupportVsimVer(int key) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephonyInner
        public String getRegPlmn(int slotId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephonyInner
        public boolean setVsimUserReservedSubId(int slotId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephonyInner
        public int getVsimUserReservedSubId() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephonyInner
        public void blockingGetVsimService(IGetVsimServiceCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephonyInner
        public boolean isVsimEnabledByDatabase() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephonyInner
        public void handleMessageForServiceEx(Message msg) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephonyInner
        public void setSimPowerStateForSlot(int slotIndex, int state, Message msg) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephonyInner
        public boolean isSmartCard(int slotId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephonyInner
        public boolean isCustomSmart() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephonyInner
        public boolean isBlockNonCustomSlot(int slotId, int customBlockType) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephonyInner
        public boolean registerForRadioStateChanged(IHwCommonPhoneCallback callback) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephonyInner
        public boolean unregisterForRadioStateChanged(IHwCommonPhoneCallback callback) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephonyInner
        public int getNetworkMode(int phoneId) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwTelephonyInner {
        private static final String DESCRIPTOR = "com.android.internal.telephony.IHwTelephonyInner";
        static final int TRANSACTION_blockingGetVsimService = 7;
        static final int TRANSACTION_getLevelForSa = 1;
        static final int TRANSACTION_getNetworkMode = 16;
        static final int TRANSACTION_getPlatformSupportVsimVer = 3;
        static final int TRANSACTION_getRegPlmn = 4;
        static final int TRANSACTION_getRrcConnectionState = 2;
        static final int TRANSACTION_getVsimUserReservedSubId = 6;
        static final int TRANSACTION_handleMessageForServiceEx = 9;
        static final int TRANSACTION_isBlockNonCustomSlot = 13;
        static final int TRANSACTION_isCustomSmart = 12;
        static final int TRANSACTION_isSmartCard = 11;
        static final int TRANSACTION_isVsimEnabledByDatabase = 8;
        static final int TRANSACTION_registerForRadioStateChanged = 14;
        static final int TRANSACTION_setSimPowerStateForSlot = 10;
        static final int TRANSACTION_setVsimUserReservedSubId = 5;
        static final int TRANSACTION_unregisterForRadioStateChanged = 15;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwTelephonyInner asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwTelephonyInner)) {
                return new Proxy(obj);
            }
            return (IHwTelephonyInner) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Message _arg0;
            Message _arg2;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getLevelForSa(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getRrcConnectionState(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getPlatformSupportVsimVer(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _result4 = getRegPlmn(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean vsimUserReservedSubId = setVsimUserReservedSubId(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(vsimUserReservedSubId ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getVsimUserReservedSubId();
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        blockingGetVsimService(IGetVsimServiceCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isVsimEnabledByDatabase = isVsimEnabledByDatabase();
                        reply.writeNoException();
                        reply.writeInt(isVsimEnabledByDatabase ? 1 : 0);
                        return true;
                    case TRANSACTION_handleMessageForServiceEx /* 9 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Message) Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        handleMessageForServiceEx(_arg0);
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        int _arg1 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = (Message) Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        setSimPowerStateForSlot(_arg02, _arg1, _arg2);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_isSmartCard /* 11 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSmartCard = isSmartCard(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isSmartCard ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isCustomSmart = isCustomSmart();
                        reply.writeNoException();
                        reply.writeInt(isCustomSmart ? 1 : 0);
                        return true;
                    case TRANSACTION_isBlockNonCustomSlot /* 13 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isBlockNonCustomSlot = isBlockNonCustomSlot(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isBlockNonCustomSlot ? 1 : 0);
                        return true;
                    case TRANSACTION_registerForRadioStateChanged /* 14 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerForRadioStateChanged = registerForRadioStateChanged(IHwCommonPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerForRadioStateChanged ? 1 : 0);
                        return true;
                    case TRANSACTION_unregisterForRadioStateChanged /* 15 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unregisterForRadioStateChanged = unregisterForRadioStateChanged(IHwCommonPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unregisterForRadioStateChanged ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = getNetworkMode(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result6);
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
        public static class Proxy implements IHwTelephonyInner {
            public static IHwTelephonyInner sDefaultImpl;
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

            @Override // com.android.internal.telephony.IHwTelephonyInner
            public int getLevelForSa(int phoneId, int nrLevel, int primaryLevel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(nrLevel);
                    _data.writeInt(primaryLevel);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLevelForSa(phoneId, nrLevel, primaryLevel);
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

            @Override // com.android.internal.telephony.IHwTelephonyInner
            public int getRrcConnectionState(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRrcConnectionState(slotId);
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

            @Override // com.android.internal.telephony.IHwTelephonyInner
            public int getPlatformSupportVsimVer(int key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(key);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPlatformSupportVsimVer(key);
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

            @Override // com.android.internal.telephony.IHwTelephonyInner
            public String getRegPlmn(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRegPlmn(slotId);
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

            @Override // com.android.internal.telephony.IHwTelephonyInner
            public boolean setVsimUserReservedSubId(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setVsimUserReservedSubId(slotId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephonyInner
            public int getVsimUserReservedSubId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVsimUserReservedSubId();
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

            @Override // com.android.internal.telephony.IHwTelephonyInner
            public void blockingGetVsimService(IGetVsimServiceCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().blockingGetVsimService(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephonyInner
            public boolean isVsimEnabledByDatabase() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isVsimEnabledByDatabase();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephonyInner
            public void handleMessageForServiceEx(Message msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (msg != null) {
                        _data.writeInt(1);
                        msg.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(Stub.TRANSACTION_handleMessageForServiceEx, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().handleMessageForServiceEx(msg);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephonyInner
            public void setSimPowerStateForSlot(int slotIndex, int state, Message msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    _data.writeInt(state);
                    if (msg != null) {
                        _data.writeInt(1);
                        msg.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSimPowerStateForSlot(slotIndex, state, msg);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephonyInner
            public boolean isSmartCard(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isSmartCard, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSmartCard(slotId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephonyInner
            public boolean isCustomSmart() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isCustomSmart();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephonyInner
            public boolean isBlockNonCustomSlot(int slotId, int customBlockType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(customBlockType);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isBlockNonCustomSlot, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isBlockNonCustomSlot(slotId, customBlockType);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephonyInner
            public boolean registerForRadioStateChanged(IHwCommonPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_registerForRadioStateChanged, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerForRadioStateChanged(callback);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephonyInner
            public boolean unregisterForRadioStateChanged(IHwCommonPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_unregisterForRadioStateChanged, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterForRadioStateChanged(callback);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephonyInner
            public int getNetworkMode(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNetworkMode(phoneId);
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
        }

        public static boolean setDefaultImpl(IHwTelephonyInner impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwTelephonyInner getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

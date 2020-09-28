package com.mediatek.ims.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.ims.ImsConfigListener;

public interface IMtkImsConfig extends IInterface {
    void getFeatureValue(int i, int i2, ImsConfigListener imsConfigListener) throws RemoteException;

    int getImsResCapability(int i) throws RemoteException;

    String getProvisionedStringValue(int i) throws RemoteException;

    int getProvisionedValue(int i) throws RemoteException;

    void getVideoQuality(ImsConfigListener imsConfigListener) throws RemoteException;

    void setFeatureValue(int i, int i2, int i3, ImsConfigListener imsConfigListener) throws RemoteException;

    void setImsResCapability(int i, int i2) throws RemoteException;

    int[] setModemImsCfg(String[] strArr, String[] strArr2, int i) throws RemoteException;

    int[] setModemImsIwlanCfg(String[] strArr, String[] strArr2, int i) throws RemoteException;

    int[] setModemImsWoCfg(String[] strArr, String[] strArr2, int i) throws RemoteException;

    void setMultiFeatureValues(int[] iArr, int[] iArr2, int[] iArr3, ImsConfigListener imsConfigListener) throws RemoteException;

    int setProvisionedStringValue(int i, String str) throws RemoteException;

    int setProvisionedValue(int i, int i2) throws RemoteException;

    void setVideoQuality(int i, ImsConfigListener imsConfigListener) throws RemoteException;

    void setVoltePreference(int i) throws RemoteException;

    void setWfcMode(int i) throws RemoteException;

    public static class Default implements IMtkImsConfig {
        @Override // com.mediatek.ims.internal.IMtkImsConfig
        public int getProvisionedValue(int item) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.ims.internal.IMtkImsConfig
        public String getProvisionedStringValue(int item) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.ims.internal.IMtkImsConfig
        public int setProvisionedValue(int item, int value) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.ims.internal.IMtkImsConfig
        public int setProvisionedStringValue(int item, String value) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.ims.internal.IMtkImsConfig
        public void getFeatureValue(int feature, int network, ImsConfigListener listener) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsConfig
        public void setFeatureValue(int feature, int network, int value, ImsConfigListener listener) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsConfig
        public void setMultiFeatureValues(int[] feature, int[] network, int[] value, ImsConfigListener listener) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsConfig
        public void getVideoQuality(ImsConfigListener listener) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsConfig
        public void setVideoQuality(int quality, ImsConfigListener listener) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsConfig
        public void setImsResCapability(int feature, int value) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsConfig
        public int getImsResCapability(int feature) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.ims.internal.IMtkImsConfig
        public void setWfcMode(int mode) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsConfig
        public void setVoltePreference(int mode) throws RemoteException {
        }

        @Override // com.mediatek.ims.internal.IMtkImsConfig
        public int[] setModemImsCfg(String[] keys, String[] values, int phoneId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.ims.internal.IMtkImsConfig
        public int[] setModemImsWoCfg(String[] keys, String[] values, int phoneId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.ims.internal.IMtkImsConfig
        public int[] setModemImsIwlanCfg(String[] keys, String[] values, int phoneId) throws RemoteException {
            return null;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMtkImsConfig {
        private static final String DESCRIPTOR = "com.mediatek.ims.internal.IMtkImsConfig";
        static final int TRANSACTION_getFeatureValue = 5;
        static final int TRANSACTION_getImsResCapability = 11;
        static final int TRANSACTION_getProvisionedStringValue = 2;
        static final int TRANSACTION_getProvisionedValue = 1;
        static final int TRANSACTION_getVideoQuality = 8;
        static final int TRANSACTION_setFeatureValue = 6;
        static final int TRANSACTION_setImsResCapability = 10;
        static final int TRANSACTION_setModemImsCfg = 14;
        static final int TRANSACTION_setModemImsIwlanCfg = 16;
        static final int TRANSACTION_setModemImsWoCfg = 15;
        static final int TRANSACTION_setMultiFeatureValues = 7;
        static final int TRANSACTION_setProvisionedStringValue = 4;
        static final int TRANSACTION_setProvisionedValue = 3;
        static final int TRANSACTION_setVideoQuality = 9;
        static final int TRANSACTION_setVoltePreference = 13;
        static final int TRANSACTION_setWfcMode = 12;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMtkImsConfig asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMtkImsConfig)) {
                return new Proxy(obj);
            }
            return (IMtkImsConfig) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getProvisionedValue(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _result2 = getProvisionedStringValue(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = setProvisionedValue(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = setProvisionedStringValue(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        getFeatureValue(data.readInt(), data.readInt(), ImsConfigListener.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        setFeatureValue(data.readInt(), data.readInt(), data.readInt(), ImsConfigListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        setMultiFeatureValues(data.createIntArray(), data.createIntArray(), data.createIntArray(), ImsConfigListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getVideoQuality /*{ENCODED_INT: 8}*/:
                        data.enforceInterface(DESCRIPTOR);
                        getVideoQuality(ImsConfigListener.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case TRANSACTION_setVideoQuality /*{ENCODED_INT: 9}*/:
                        data.enforceInterface(DESCRIPTOR);
                        setVideoQuality(data.readInt(), ImsConfigListener.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case TRANSACTION_setImsResCapability /*{ENCODED_INT: 10}*/:
                        data.enforceInterface(DESCRIPTOR);
                        setImsResCapability(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getImsResCapability /*{ENCODED_INT: 11}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getImsResCapability(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case TRANSACTION_setWfcMode /*{ENCODED_INT: 12}*/:
                        data.enforceInterface(DESCRIPTOR);
                        setWfcMode(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setVoltePreference /*{ENCODED_INT: 13}*/:
                        data.enforceInterface(DESCRIPTOR);
                        setVoltePreference(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setModemImsCfg /*{ENCODED_INT: 14}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result6 = setModemImsCfg(data.createStringArray(), data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeIntArray(_result6);
                        return true;
                    case TRANSACTION_setModemImsWoCfg /*{ENCODED_INT: 15}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result7 = setModemImsWoCfg(data.createStringArray(), data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeIntArray(_result7);
                        return true;
                    case TRANSACTION_setModemImsIwlanCfg /*{ENCODED_INT: 16}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result8 = setModemImsIwlanCfg(data.createStringArray(), data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeIntArray(_result8);
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
        public static class Proxy implements IMtkImsConfig {
            public static IMtkImsConfig sDefaultImpl;
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

            @Override // com.mediatek.ims.internal.IMtkImsConfig
            public int getProvisionedValue(int item) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(item);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProvisionedValue(item);
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

            @Override // com.mediatek.ims.internal.IMtkImsConfig
            public String getProvisionedStringValue(int item) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(item);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProvisionedStringValue(item);
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

            @Override // com.mediatek.ims.internal.IMtkImsConfig
            public int setProvisionedValue(int item, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(item);
                    _data.writeInt(value);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setProvisionedValue(item, value);
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

            @Override // com.mediatek.ims.internal.IMtkImsConfig
            public int setProvisionedStringValue(int item, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(item);
                    _data.writeString(value);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setProvisionedStringValue(item, value);
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

            @Override // com.mediatek.ims.internal.IMtkImsConfig
            public void getFeatureValue(int feature, int network, ImsConfigListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(feature);
                    _data.writeInt(network);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getFeatureValue(feature, network, listener);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsConfig
            public void setFeatureValue(int feature, int network, int value, ImsConfigListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(feature);
                    _data.writeInt(network);
                    _data.writeInt(value);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFeatureValue(feature, network, value, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsConfig
            public void setMultiFeatureValues(int[] feature, int[] network, int[] value, ImsConfigListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(feature);
                    _data.writeIntArray(network);
                    _data.writeIntArray(value);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMultiFeatureValues(feature, network, value, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsConfig
            public void getVideoQuality(ImsConfigListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(Stub.TRANSACTION_getVideoQuality, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getVideoQuality(listener);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsConfig
            public void setVideoQuality(int quality, ImsConfigListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(quality);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(Stub.TRANSACTION_setVideoQuality, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setVideoQuality(quality, listener);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsConfig
            public void setImsResCapability(int feature, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(feature);
                    _data.writeInt(value);
                    if (this.mRemote.transact(Stub.TRANSACTION_setImsResCapability, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setImsResCapability(feature, value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsConfig
            public int getImsResCapability(int feature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(feature);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getImsResCapability, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImsResCapability(feature);
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

            @Override // com.mediatek.ims.internal.IMtkImsConfig
            public void setWfcMode(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(Stub.TRANSACTION_setWfcMode, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setWfcMode(mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsConfig
            public void setVoltePreference(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(Stub.TRANSACTION_setVoltePreference, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVoltePreference(mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsConfig
            public int[] setModemImsCfg(String[] keys, String[] values, int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(keys);
                    _data.writeStringArray(values);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setModemImsCfg, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setModemImsCfg(keys, values, phoneId);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsConfig
            public int[] setModemImsWoCfg(String[] keys, String[] values, int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(keys);
                    _data.writeStringArray(values);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setModemImsWoCfg, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setModemImsWoCfg(keys, values, phoneId);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.ims.internal.IMtkImsConfig
            public int[] setModemImsIwlanCfg(String[] keys, String[] values, int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(keys);
                    _data.writeStringArray(values);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setModemImsIwlanCfg, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setModemImsIwlanCfg(keys, values, phoneId);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMtkImsConfig impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMtkImsConfig getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

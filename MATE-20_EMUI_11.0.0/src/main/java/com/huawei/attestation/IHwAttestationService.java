package com.huawei.attestation;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwAttestationService extends IInterface {
    byte[] getAttestationSignature(int i, int i2, String str, byte[] bArr) throws RemoteException;

    byte[] getAttestationSignatureWithPkgName(int i, int i2, String str, byte[] bArr, String str2) throws RemoteException;

    int getDeviceCert(int i, int i2, byte[] bArr) throws RemoteException;

    int getDeviceCertType(int i) throws RemoteException;

    byte[] getDeviceID(int i) throws RemoteException;

    int getLastError() throws RemoteException;

    int getPublickKey(int i, byte[] bArr) throws RemoteException;

    public static class Default implements IHwAttestationService {
        @Override // com.huawei.attestation.IHwAttestationService
        public byte[] getAttestationSignature(int keyIndex, int deviceIdType, String signatureType, byte[] challenge) throws RemoteException {
            return null;
        }

        @Override // com.huawei.attestation.IHwAttestationService
        public int getLastError() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.attestation.IHwAttestationService
        public byte[] getDeviceID(int deviceIdType) throws RemoteException {
            return null;
        }

        @Override // com.huawei.attestation.IHwAttestationService
        public int getPublickKey(int keyIndex, byte[] keyBuf) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.attestation.IHwAttestationService
        public int getDeviceCertType(int keyIndex) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.attestation.IHwAttestationService
        public int getDeviceCert(int keyIndex, int certType, byte[] certBuf) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.attestation.IHwAttestationService
        public byte[] getAttestationSignatureWithPkgName(int keyIndex, int deviceIdType, String signatureType, byte[] challenge, String packageName) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwAttestationService {
        private static final String DESCRIPTOR = "com.huawei.attestation.IHwAttestationService";
        static final int TRANSACTION_getAttestationSignature = 1;
        static final int TRANSACTION_getAttestationSignatureWithPkgName = 7;
        static final int TRANSACTION_getDeviceCert = 6;
        static final int TRANSACTION_getDeviceCertType = 5;
        static final int TRANSACTION_getDeviceID = 3;
        static final int TRANSACTION_getLastError = 2;
        static final int TRANSACTION_getPublickKey = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwAttestationService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwAttestationService)) {
                return new Proxy(obj);
            }
            return (IHwAttestationService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result = getAttestationSignature(data.readInt(), data.readInt(), data.readString(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeByteArray(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getLastError();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result3 = getDeviceID(data.readInt());
                        reply.writeNoException();
                        reply.writeByteArray(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        byte[] _arg1 = data.createByteArray();
                        int _result4 = getPublickKey(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        reply.writeByteArray(_arg1);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getDeviceCertType(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        int _arg12 = data.readInt();
                        byte[] _arg2 = data.createByteArray();
                        int _result6 = getDeviceCert(_arg02, _arg12, _arg2);
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        reply.writeByteArray(_arg2);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result7 = getAttestationSignatureWithPkgName(data.readInt(), data.readInt(), data.readString(), data.createByteArray(), data.readString());
                        reply.writeNoException();
                        reply.writeByteArray(_result7);
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
        public static class Proxy implements IHwAttestationService {
            public static IHwAttestationService sDefaultImpl;
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

            @Override // com.huawei.attestation.IHwAttestationService
            public byte[] getAttestationSignature(int keyIndex, int deviceIdType, String signatureType, byte[] challenge) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(keyIndex);
                    _data.writeInt(deviceIdType);
                    _data.writeString(signatureType);
                    _data.writeByteArray(challenge);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAttestationSignature(keyIndex, deviceIdType, signatureType, challenge);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.attestation.IHwAttestationService
            public int getLastError() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLastError();
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

            @Override // com.huawei.attestation.IHwAttestationService
            public byte[] getDeviceID(int deviceIdType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceIdType);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceID(deviceIdType);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.attestation.IHwAttestationService
            public int getPublickKey(int keyIndex, byte[] keyBuf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(keyIndex);
                    _data.writeByteArray(keyBuf);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPublickKey(keyIndex, keyBuf);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(keyBuf);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.attestation.IHwAttestationService
            public int getDeviceCertType(int keyIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(keyIndex);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceCertType(keyIndex);
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

            @Override // com.huawei.attestation.IHwAttestationService
            public int getDeviceCert(int keyIndex, int certType, byte[] certBuf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(keyIndex);
                    _data.writeInt(certType);
                    _data.writeByteArray(certBuf);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceCert(keyIndex, certType, certBuf);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(certBuf);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.attestation.IHwAttestationService
            public byte[] getAttestationSignatureWithPkgName(int keyIndex, int deviceIdType, String signatureType, byte[] challenge, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(keyIndex);
                    _data.writeInt(deviceIdType);
                    _data.writeString(signatureType);
                    _data.writeByteArray(challenge);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAttestationSignatureWithPkgName(keyIndex, deviceIdType, signatureType, challenge, packageName);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwAttestationService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwAttestationService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

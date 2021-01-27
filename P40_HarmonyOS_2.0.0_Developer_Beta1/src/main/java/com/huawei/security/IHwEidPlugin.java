package com.huawei.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface IHwEidPlugin extends IInterface {
    int ctidGetSecImage() throws RemoteException;

    int ctidGetServiceVerionInfo(byte[] bArr, int i, String str, int[] iArr, int i2) throws RemoteException;

    int ctidSetSecMode() throws RemoteException;

    int eidFinish() throws RemoteException;

    int eidGetCertificateRequestMessage(byte[] bArr, int[] iArr) throws RemoteException;

    int eidGetFaceIsChanged(int i) throws RemoteException;

    int eidGetIdentityInformation(byte[] bArr, int[] iArr) throws RemoteException;

    int eidGetImage(ControlWordEntity controlWordEntity, EidInfoEntity eidInfoEntity, EidInfoExtendEntity eidInfoExtendEntity, EidInfoExtendEntity eidInfoExtendEntity2) throws RemoteException;

    int eidGetSecImageZip(CoordinateEntity coordinateEntity, ControlWordEntity controlWordEntity, List<EidInfoEntity> list, List<EidInfoExtendEntity> list2) throws RemoteException;

    int eidGetUnsecImage(EidInfoEntity eidInfoEntity, ControlWordEntity controlWordEntity, EidInfoEntity eidInfoEntity2, EidInfoExtendEntity eidInfoExtendEntity, EidInfoExtendEntity eidInfoExtendEntity2) throws RemoteException;

    int eidGetUnsecImageZip(ControlWordEntity controlWordEntity, List<EidInfoEntity> list, List<EidInfoExtendEntity> list2) throws RemoteException;

    String eidGetVersion() throws RemoteException;

    int eidInit(EidInfoEntity eidInfoEntity, EidInfoEntity eidInfoEntity2, EidInfoEntity eidInfoEntity3) throws RemoteException;

    int eidSignInfo(ControlWordEntity controlWordEntity, EidInfoEntity eidInfoEntity, EidInfoExtendEntity eidInfoExtendEntity) throws RemoteException;

    public static class Default implements IHwEidPlugin {
        @Override // com.huawei.security.IHwEidPlugin
        public int eidInit(EidInfoEntity aidInfo, EidInfoEntity eidAidInfo, EidInfoEntity logoInfo) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwEidPlugin
        public int eidFinish() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwEidPlugin
        public int eidGetImage(ControlWordEntity controlWord, EidInfoEntity certificateInfo, EidInfoExtendEntity imageInfo, EidInfoExtendEntity deSkeyInfo) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwEidPlugin
        public int eidGetUnsecImage(EidInfoEntity srcInfoEntity, ControlWordEntity controlWord, EidInfoEntity certificateInfo, EidInfoExtendEntity imageInfo, EidInfoExtendEntity deSkeyInfo) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwEidPlugin
        public int eidGetCertificateRequestMessage(byte[] requestMessage, int[] messageLen) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwEidPlugin
        public int eidSignInfo(ControlWordEntity controlWord, EidInfoEntity infoEntity, EidInfoExtendEntity signEntity) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwEidPlugin
        public int eidGetIdentityInformation(byte[] identityInfo, int[] identityInfoLen) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwEidPlugin
        public int eidGetFaceIsChanged(int cmdId) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwEidPlugin
        public String eidGetVersion() throws RemoteException {
            return null;
        }

        @Override // com.huawei.security.IHwEidPlugin
        public int ctidSetSecMode() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwEidPlugin
        public int ctidGetSecImage() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwEidPlugin
        public int ctidGetServiceVerionInfo(byte[] uuid, int uuidLen, String taPath, int[] cmdList, int cmdCount) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwEidPlugin
        public int eidGetSecImageZip(CoordinateEntity coordinateEntity, ControlWordEntity controlWord, List<EidInfoEntity> list, List<EidInfoExtendEntity> list2) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwEidPlugin
        public int eidGetUnsecImageZip(ControlWordEntity controlWord, List<EidInfoEntity> list, List<EidInfoExtendEntity> list2) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwEidPlugin {
        private static final String DESCRIPTOR = "com.huawei.security.IHwEidPlugin";
        static final int TRANSACTION_ctidGetSecImage = 11;
        static final int TRANSACTION_ctidGetServiceVerionInfo = 12;
        static final int TRANSACTION_ctidSetSecMode = 10;
        static final int TRANSACTION_eidFinish = 2;
        static final int TRANSACTION_eidGetCertificateRequestMessage = 5;
        static final int TRANSACTION_eidGetFaceIsChanged = 8;
        static final int TRANSACTION_eidGetIdentityInformation = 7;
        static final int TRANSACTION_eidGetImage = 3;
        static final int TRANSACTION_eidGetSecImageZip = 13;
        static final int TRANSACTION_eidGetUnsecImage = 4;
        static final int TRANSACTION_eidGetUnsecImageZip = 14;
        static final int TRANSACTION_eidGetVersion = 9;
        static final int TRANSACTION_eidInit = 1;
        static final int TRANSACTION_eidSignInfo = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwEidPlugin asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwEidPlugin)) {
                return new Proxy(obj);
            }
            return (IHwEidPlugin) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            EidInfoEntity _arg0;
            EidInfoEntity _arg1;
            EidInfoEntity _arg2;
            ControlWordEntity _arg02;
            EidInfoEntity _arg12;
            EidInfoExtendEntity _arg22;
            EidInfoExtendEntity _arg3;
            int i;
            EidInfoEntity _arg03;
            ControlWordEntity _arg13;
            EidInfoEntity _arg23;
            EidInfoExtendEntity _arg32;
            EidInfoExtendEntity _arg4;
            int i2;
            byte[] _arg04;
            int[] _arg14;
            ControlWordEntity _arg05;
            EidInfoEntity _arg15;
            EidInfoExtendEntity _arg24;
            byte[] _arg06;
            int[] _arg16;
            CoordinateEntity _arg07;
            ControlWordEntity _arg17;
            ControlWordEntity _arg08;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = EidInfoEntity.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = EidInfoEntity.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = EidInfoEntity.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        int _result = eidInit(_arg0, _arg1, _arg2);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = eidFinish();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ControlWordEntity.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = EidInfoEntity.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = EidInfoExtendEntity.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = EidInfoExtendEntity.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        int _result3 = eidGetImage(_arg02, _arg12, _arg22, _arg3);
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        if (_arg22 != null) {
                            reply.writeInt(1);
                            _arg22.writeToParcel(reply, 1);
                            i = 0;
                        } else {
                            i = 0;
                            reply.writeInt(0);
                        }
                        if (_arg3 != null) {
                            reply.writeInt(1);
                            _arg3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(i);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = EidInfoEntity.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg13 = ControlWordEntity.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg23 = EidInfoEntity.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg32 = EidInfoExtendEntity.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg4 = EidInfoExtendEntity.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        int _result4 = eidGetUnsecImage(_arg03, _arg13, _arg23, _arg32, _arg4);
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        if (_arg32 != null) {
                            reply.writeInt(1);
                            _arg32.writeToParcel(reply, 1);
                            i2 = 0;
                        } else {
                            i2 = 0;
                            reply.writeInt(0);
                        }
                        if (_arg4 != null) {
                            reply.writeInt(1);
                            _arg4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(i2);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0_length = data.readInt();
                        if (_arg0_length < 0) {
                            _arg04 = null;
                        } else {
                            _arg04 = new byte[_arg0_length];
                        }
                        int _arg1_length = data.readInt();
                        if (_arg1_length < 0) {
                            _arg14 = null;
                        } else {
                            _arg14 = new int[_arg1_length];
                        }
                        int _result5 = eidGetCertificateRequestMessage(_arg04, _arg14);
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        reply.writeByteArray(_arg04);
                        reply.writeIntArray(_arg14);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = ControlWordEntity.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg15 = EidInfoEntity.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg24 = EidInfoExtendEntity.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        int _result6 = eidSignInfo(_arg05, _arg15, _arg24);
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        if (_arg24 != null) {
                            reply.writeInt(1);
                            _arg24.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0_length2 = data.readInt();
                        if (_arg0_length2 < 0) {
                            _arg06 = null;
                        } else {
                            _arg06 = new byte[_arg0_length2];
                        }
                        int _arg1_length2 = data.readInt();
                        if (_arg1_length2 < 0) {
                            _arg16 = null;
                        } else {
                            _arg16 = new int[_arg1_length2];
                        }
                        int _result7 = eidGetIdentityInformation(_arg06, _arg16);
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        reply.writeByteArray(_arg06);
                        reply.writeIntArray(_arg16);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = eidGetFaceIsChanged(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String _result9 = eidGetVersion();
                        reply.writeNoException();
                        reply.writeString(_result9);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = ctidSetSecMode();
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = ctidGetSecImage();
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = ctidGetServiceVerionInfo(data.createByteArray(), data.readInt(), data.readString(), data.createIntArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = CoordinateEntity.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg17 = ControlWordEntity.CREATOR.createFromParcel(data);
                        } else {
                            _arg17 = null;
                        }
                        List<EidInfoEntity> _arg25 = data.createTypedArrayList(EidInfoEntity.CREATOR);
                        ArrayList createTypedArrayList = data.createTypedArrayList(EidInfoExtendEntity.CREATOR);
                        int _result13 = eidGetSecImageZip(_arg07, _arg17, _arg25, createTypedArrayList);
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        reply.writeTypedList(createTypedArrayList);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = ControlWordEntity.CREATOR.createFromParcel(data);
                        } else {
                            _arg08 = null;
                        }
                        List<EidInfoEntity> _arg18 = data.createTypedArrayList(EidInfoEntity.CREATOR);
                        ArrayList createTypedArrayList2 = data.createTypedArrayList(EidInfoExtendEntity.CREATOR);
                        int _result14 = eidGetUnsecImageZip(_arg08, _arg18, createTypedArrayList2);
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        reply.writeTypedList(createTypedArrayList2);
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
        public static class Proxy implements IHwEidPlugin {
            public static IHwEidPlugin sDefaultImpl;
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

            @Override // com.huawei.security.IHwEidPlugin
            public int eidInit(EidInfoEntity aidInfo, EidInfoEntity eidAidInfo, EidInfoEntity logoInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (aidInfo != null) {
                        _data.writeInt(1);
                        aidInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (eidAidInfo != null) {
                        _data.writeInt(1);
                        eidAidInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (logoInfo != null) {
                        _data.writeInt(1);
                        logoInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().eidInit(aidInfo, eidAidInfo, logoInfo);
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

            @Override // com.huawei.security.IHwEidPlugin
            public int eidFinish() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().eidFinish();
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

            @Override // com.huawei.security.IHwEidPlugin
            public int eidGetImage(ControlWordEntity controlWord, EidInfoEntity certificateInfo, EidInfoExtendEntity imageInfo, EidInfoExtendEntity deSkeyInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (controlWord != null) {
                        _data.writeInt(1);
                        controlWord.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (certificateInfo != null) {
                        _data.writeInt(1);
                        certificateInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (imageInfo != null) {
                        _data.writeInt(1);
                        imageInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (deSkeyInfo != null) {
                        _data.writeInt(1);
                        deSkeyInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().eidGetImage(controlWord, certificateInfo, imageInfo, deSkeyInfo);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        imageInfo.readFromParcel(_reply);
                    }
                    if (_reply.readInt() != 0) {
                        deSkeyInfo.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.IHwEidPlugin
            public int eidGetUnsecImage(EidInfoEntity srcInfoEntity, ControlWordEntity controlWord, EidInfoEntity certificateInfo, EidInfoExtendEntity imageInfo, EidInfoExtendEntity deSkeyInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (srcInfoEntity != null) {
                        _data.writeInt(1);
                        srcInfoEntity.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (controlWord != null) {
                        _data.writeInt(1);
                        controlWord.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (certificateInfo != null) {
                        _data.writeInt(1);
                        certificateInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (imageInfo != null) {
                        _data.writeInt(1);
                        imageInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (deSkeyInfo != null) {
                        _data.writeInt(1);
                        deSkeyInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().eidGetUnsecImage(srcInfoEntity, controlWord, certificateInfo, imageInfo, deSkeyInfo);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        imageInfo.readFromParcel(_reply);
                    }
                    if (_reply.readInt() != 0) {
                        deSkeyInfo.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.IHwEidPlugin
            public int eidGetCertificateRequestMessage(byte[] requestMessage, int[] messageLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (requestMessage == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(requestMessage.length);
                    }
                    if (messageLen == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(messageLen.length);
                    }
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().eidGetCertificateRequestMessage(requestMessage, messageLen);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(requestMessage);
                    _reply.readIntArray(messageLen);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.IHwEidPlugin
            public int eidSignInfo(ControlWordEntity controlWord, EidInfoEntity infoEntity, EidInfoExtendEntity signEntity) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (controlWord != null) {
                        _data.writeInt(1);
                        controlWord.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (infoEntity != null) {
                        _data.writeInt(1);
                        infoEntity.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (signEntity != null) {
                        _data.writeInt(1);
                        signEntity.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().eidSignInfo(controlWord, infoEntity, signEntity);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        signEntity.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.IHwEidPlugin
            public int eidGetIdentityInformation(byte[] identityInfo, int[] identityInfoLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (identityInfo == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(identityInfo.length);
                    }
                    if (identityInfoLen == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(identityInfoLen.length);
                    }
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().eidGetIdentityInformation(identityInfo, identityInfoLen);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(identityInfo);
                    _reply.readIntArray(identityInfoLen);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.IHwEidPlugin
            public int eidGetFaceIsChanged(int cmdId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cmdId);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().eidGetFaceIsChanged(cmdId);
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

            @Override // com.huawei.security.IHwEidPlugin
            public String eidGetVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().eidGetVersion();
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

            @Override // com.huawei.security.IHwEidPlugin
            public int ctidSetSecMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().ctidSetSecMode();
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

            @Override // com.huawei.security.IHwEidPlugin
            public int ctidGetSecImage() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().ctidGetSecImage();
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

            @Override // com.huawei.security.IHwEidPlugin
            public int ctidGetServiceVerionInfo(byte[] uuid, int uuidLen, String taPath, int[] cmdList, int cmdCount) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(uuid);
                    _data.writeInt(uuidLen);
                    _data.writeString(taPath);
                    _data.writeIntArray(cmdList);
                    _data.writeInt(cmdCount);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().ctidGetServiceVerionInfo(uuid, uuidLen, taPath, cmdList, cmdCount);
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

            @Override // com.huawei.security.IHwEidPlugin
            public int eidGetSecImageZip(CoordinateEntity coordinateEntity, ControlWordEntity controlWord, List<EidInfoEntity> eidInfoEntityMap, List<EidInfoExtendEntity> eidInfoExtendEntityMap) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (coordinateEntity != null) {
                        _data.writeInt(1);
                        coordinateEntity.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (controlWord != null) {
                        _data.writeInt(1);
                        controlWord.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedList(eidInfoEntityMap);
                    _data.writeTypedList(eidInfoExtendEntityMap);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().eidGetSecImageZip(coordinateEntity, controlWord, eidInfoEntityMap, eidInfoExtendEntityMap);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readTypedList(eidInfoExtendEntityMap, EidInfoExtendEntity.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.IHwEidPlugin
            public int eidGetUnsecImageZip(ControlWordEntity controlWord, List<EidInfoEntity> eidInfoEntityMap, List<EidInfoExtendEntity> eidInfoExtendEntityMap) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (controlWord != null) {
                        _data.writeInt(1);
                        controlWord.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedList(eidInfoEntityMap);
                    _data.writeTypedList(eidInfoExtendEntityMap);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().eidGetUnsecImageZip(controlWord, eidInfoEntityMap, eidInfoExtendEntityMap);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readTypedList(eidInfoExtendEntityMap, EidInfoExtendEntity.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwEidPlugin impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwEidPlugin getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

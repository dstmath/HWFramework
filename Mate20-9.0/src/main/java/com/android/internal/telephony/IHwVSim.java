package com.android.internal.telephony;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwVSim extends IInterface {

    public static abstract class Stub extends Binder implements IHwVSim {
        private static final String DESCRIPTOR = "com.android.internal.telephony.IHwVSim";
        static final int TRANSACTION_clearTrafficData = 11;
        static final int TRANSACTION_dialupForVSim = 31;
        static final int TRANSACTION_disableVSim = 5;
        static final int TRANSACTION_dsFlowCfg = 12;
        static final int TRANSACTION_enableVSim = 4;
        static final int TRANSACTION_enableVSimV2 = 30;
        static final int TRANSACTION_enableVSimV3 = 32;
        static final int TRANSACTION_getCpserr = 14;
        static final int TRANSACTION_getDevSubMode = 18;
        static final int TRANSACTION_getPlatformSupportVSimVer = 29;
        static final int TRANSACTION_getPreferredNetworkTypeForVSim = 19;
        static final int TRANSACTION_getRegPlmn = 9;
        static final int TRANSACTION_getSimMode = 7;
        static final int TRANSACTION_getSimStateViaSysinfoEx = 13;
        static final int TRANSACTION_getTrafficData = 10;
        static final int TRANSACTION_getUserReservedSubId = 17;
        static final int TRANSACTION_getVSimCurCardType = 20;
        static final int TRANSACTION_getVSimNetworkType = 21;
        static final int TRANSACTION_getVSimOccupiedSubId = 25;
        static final int TRANSACTION_getVSimSubId = 2;
        static final int TRANSACTION_getVSimSubscriberId = 22;
        static final int TRANSACTION_getVSimULOnlyMode = 24;
        static final int TRANSACTION_hasHardIccCardForVSim = 6;
        static final int TRANSACTION_hasVSimIccCard = 1;
        static final int TRANSACTION_isSupportVSimByOperation = 33;
        static final int TRANSACTION_isVSimEnabled = 3;
        static final int TRANSACTION_isVSimInProcess = 26;
        static final int TRANSACTION_isVSimOn = 27;
        static final int TRANSACTION_recoverSimMode = 8;
        static final int TRANSACTION_scanVsimAvailableNetworks = 15;
        static final int TRANSACTION_setUserReservedSubId = 16;
        static final int TRANSACTION_setVSimULOnlyMode = 23;
        static final int TRANSACTION_switchVSimWorkMode = 28;

        private static class Proxy implements IHwVSim {
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

            public boolean hasVSimIccCard() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVSimSubId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isVSimEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int enableVSim(int operation, String imsi, int cardType, int apnType, String acqorder, String challenge) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(operation);
                    _data.writeString(imsi);
                    _data.writeInt(cardType);
                    _data.writeInt(apnType);
                    _data.writeString(acqorder);
                    _data.writeString(challenge);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean disableVSim() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasHardIccCardForVSim(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSimMode(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void recoverSimMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getRegPlmn(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getTrafficData() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean clearTrafficData() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean dsFlowCfg(int repFlag, int threshold, int totalThreshold, int oper) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(repFlag);
                    _data.writeInt(threshold);
                    _data.writeInt(totalThreshold);
                    _data.writeInt(oper);
                    boolean _result = false;
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSimStateViaSysinfoEx(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCpserr(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int scanVsimAvailableNetworks(int subId, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(type);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setUserReservedSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getUserReservedSubId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getDevSubMode(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getPreferredNetworkTypeForVSim(int subscription) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subscription);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVSimCurCardType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVSimNetworkType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getVSimSubscriberId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setVSimULOnlyMode(boolean isULOnly) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isULOnly);
                    boolean _result = false;
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getVSimULOnlyMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVSimOccupiedSubId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isVSimInProcess() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isVSimOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean switchVSimWorkMode(int workMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(workMode);
                    boolean _result = false;
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPlatformSupportVSimVer(int key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(key);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int enableVSimV2(int operation, String imsi, int cardType, int apnType, String acqorder, String tapath, int vsimloc, String challenge) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(operation);
                    _data.writeString(imsi);
                    _data.writeInt(cardType);
                    _data.writeInt(apnType);
                    _data.writeString(acqorder);
                    _data.writeString(tapath);
                    _data.writeInt(vsimloc);
                    _data.writeString(challenge);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int dialupForVSim() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int enableVSimV3(int operation, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(operation);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSupportVSimByOperation(int operation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(operation);
                    boolean _result = false;
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwVSim asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwVSim)) {
                return new Proxy(obj);
            }
            return (IHwVSim) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg1;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result = hasVSimIccCard();
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result2 = getVSimSubId();
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result3 = isVSimEnabled();
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result4 = enableVSim(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result5 = disableVSim();
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result6 = hasHardIccCardForVSim(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result6);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result7 = getSimMode(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result7);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        recoverSimMode();
                        reply.writeNoException();
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result8 = getRegPlmn(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result8);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result9 = getTrafficData();
                        reply.writeNoException();
                        parcel2.writeString(_result9);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result10 = clearTrafficData();
                        reply.writeNoException();
                        parcel2.writeInt(_result10);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result11 = dsFlowCfg(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result11);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result12 = getSimStateViaSysinfoEx(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result12);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result13 = getCpserr(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result13);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result14 = scanVsimAvailableNetworks(data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result14);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result15 = setUserReservedSubId(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result15);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result16 = getUserReservedSubId();
                        reply.writeNoException();
                        parcel2.writeInt(_result16);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result17 = getDevSubMode(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result17);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result18 = getPreferredNetworkTypeForVSim(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result18);
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result19 = getVSimCurCardType();
                        reply.writeNoException();
                        parcel2.writeInt(_result19);
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result20 = getVSimNetworkType();
                        reply.writeNoException();
                        parcel2.writeInt(_result20);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result21 = getVSimSubscriberId();
                        reply.writeNoException();
                        parcel2.writeString(_result21);
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result22 = setVSimULOnlyMode(data.readInt() != 0);
                        reply.writeNoException();
                        parcel2.writeInt(_result22);
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result23 = getVSimULOnlyMode();
                        reply.writeNoException();
                        parcel2.writeInt(_result23);
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result24 = getVSimOccupiedSubId();
                        reply.writeNoException();
                        parcel2.writeInt(_result24);
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result25 = isVSimInProcess();
                        reply.writeNoException();
                        parcel2.writeInt(_result25);
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result26 = isVSimOn();
                        reply.writeNoException();
                        parcel2.writeInt(_result26);
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result27 = switchVSimWorkMode(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result27);
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result28 = getPlatformSupportVSimVer(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result28);
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result29 = enableVSimV2(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result29);
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result30 = dialupForVSim();
                        reply.writeNoException();
                        parcel2.writeInt(_result30);
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        int _result31 = enableVSimV3(_arg0, _arg1);
                        reply.writeNoException();
                        parcel2.writeInt(_result31);
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result32 = isSupportVSimByOperation(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result32);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    boolean clearTrafficData() throws RemoteException;

    int dialupForVSim() throws RemoteException;

    boolean disableVSim() throws RemoteException;

    boolean dsFlowCfg(int i, int i2, int i3, int i4) throws RemoteException;

    int enableVSim(int i, String str, int i2, int i3, String str2, String str3) throws RemoteException;

    int enableVSimV2(int i, String str, int i2, int i3, String str2, String str3, int i4, String str4) throws RemoteException;

    int enableVSimV3(int i, Bundle bundle) throws RemoteException;

    int getCpserr(int i) throws RemoteException;

    String getDevSubMode(int i) throws RemoteException;

    int getPlatformSupportVSimVer(int i) throws RemoteException;

    String getPreferredNetworkTypeForVSim(int i) throws RemoteException;

    String getRegPlmn(int i) throws RemoteException;

    int getSimMode(int i) throws RemoteException;

    int getSimStateViaSysinfoEx(int i) throws RemoteException;

    String getTrafficData() throws RemoteException;

    int getUserReservedSubId() throws RemoteException;

    int getVSimCurCardType() throws RemoteException;

    int getVSimNetworkType() throws RemoteException;

    int getVSimOccupiedSubId() throws RemoteException;

    int getVSimSubId() throws RemoteException;

    String getVSimSubscriberId() throws RemoteException;

    boolean getVSimULOnlyMode() throws RemoteException;

    boolean hasHardIccCardForVSim(int i) throws RemoteException;

    boolean hasVSimIccCard() throws RemoteException;

    boolean isSupportVSimByOperation(int i) throws RemoteException;

    boolean isVSimEnabled() throws RemoteException;

    boolean isVSimInProcess() throws RemoteException;

    boolean isVSimOn() throws RemoteException;

    void recoverSimMode() throws RemoteException;

    int scanVsimAvailableNetworks(int i, int i2) throws RemoteException;

    boolean setUserReservedSubId(int i) throws RemoteException;

    boolean setVSimULOnlyMode(boolean z) throws RemoteException;

    boolean switchVSimWorkMode(int i) throws RemoteException;
}

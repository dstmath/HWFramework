package com.android.internal.telephony;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwVSim extends IInterface {
    boolean clearTrafficData() throws RemoteException;

    int dialupForVSim() throws RemoteException;

    boolean disableVSim() throws RemoteException;

    boolean dsFlowCfg(int i, int i2, int i3, int i4) throws RemoteException;

    int enableVSim(int i, String str, int i2, int i3, String str2, String str3) throws RemoteException;

    int enableVSimV2(int i, String str, int i2, int i3, String str2, String str3, int i4, String str4) throws RemoteException;

    int enableVSimV3(int i, Bundle bundle) throws RemoteException;

    String getDevSubMode(int i) throws RemoteException;

    String getPreferredNetworkTypeForVSim(int i) throws RemoteException;

    int getSimStateViaSysinfoEx(int i) throws RemoteException;

    String getTrafficData() throws RemoteException;

    int getVSimCurCardType() throws RemoteException;

    int getVSimNetworkType() throws RemoteException;

    int getVSimOccupiedSubId() throws RemoteException;

    int getVSimSubId() throws RemoteException;

    String getVSimSubscriberId() throws RemoteException;

    boolean hasVSimIccCard() throws RemoteException;

    boolean isVSimEnabled() throws RemoteException;

    boolean isVSimInProcess() throws RemoteException;

    boolean isVSimOn() throws RemoteException;

    int scanVsimAvailableNetworks(int i, int i2) throws RemoteException;

    boolean switchVSimWorkMode(int i) throws RemoteException;

    public static class Default implements IHwVSim {
        @Override // com.android.internal.telephony.IHwVSim
        public boolean hasVSimIccCard() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public int getVSimSubId() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public boolean isVSimEnabled() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public int enableVSim(int operation, String imsi, int cardType, int apnType, String acqorder, String challenge) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public boolean disableVSim() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public String getTrafficData() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public boolean clearTrafficData() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public boolean dsFlowCfg(int repFlag, int threshold, int totalThreshold, int oper) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public int getSimStateViaSysinfoEx(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public int scanVsimAvailableNetworks(int subId, int type) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public String getDevSubMode(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public String getPreferredNetworkTypeForVSim(int subscription) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public int getVSimCurCardType() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public int getVSimNetworkType() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public String getVSimSubscriberId() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public int getVSimOccupiedSubId() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public boolean isVSimInProcess() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public boolean isVSimOn() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public boolean switchVSimWorkMode(int workMode) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public int enableVSimV2(int operation, String imsi, int cardType, int apnType, String acqorder, String tapath, int vsimloc, String challenge) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public int dialupForVSim() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwVSim
        public int enableVSimV3(int operation, Bundle bundle) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwVSim {
        private static final String DESCRIPTOR = "com.android.internal.telephony.IHwVSim";
        static final int TRANSACTION_clearTrafficData = 7;
        static final int TRANSACTION_dialupForVSim = 21;
        static final int TRANSACTION_disableVSim = 5;
        static final int TRANSACTION_dsFlowCfg = 8;
        static final int TRANSACTION_enableVSim = 4;
        static final int TRANSACTION_enableVSimV2 = 20;
        static final int TRANSACTION_enableVSimV3 = 22;
        static final int TRANSACTION_getDevSubMode = 11;
        static final int TRANSACTION_getPreferredNetworkTypeForVSim = 12;
        static final int TRANSACTION_getSimStateViaSysinfoEx = 9;
        static final int TRANSACTION_getTrafficData = 6;
        static final int TRANSACTION_getVSimCurCardType = 13;
        static final int TRANSACTION_getVSimNetworkType = 14;
        static final int TRANSACTION_getVSimOccupiedSubId = 16;
        static final int TRANSACTION_getVSimSubId = 2;
        static final int TRANSACTION_getVSimSubscriberId = 15;
        static final int TRANSACTION_hasVSimIccCard = 1;
        static final int TRANSACTION_isVSimEnabled = 3;
        static final int TRANSACTION_isVSimInProcess = 17;
        static final int TRANSACTION_isVSimOn = 18;
        static final int TRANSACTION_scanVsimAvailableNetworks = 10;
        static final int TRANSACTION_switchVSimWorkMode = 19;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg1;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasVSimIccCard = hasVSimIccCard();
                        reply.writeNoException();
                        reply.writeInt(hasVSimIccCard ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getVSimSubId();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isVSimEnabled = isVSimEnabled();
                        reply.writeNoException();
                        reply.writeInt(isVSimEnabled ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = enableVSim(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean disableVSim = disableVSim();
                        reply.writeNoException();
                        reply.writeInt(disableVSim ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _result3 = getTrafficData();
                        reply.writeNoException();
                        reply.writeString(_result3);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean clearTrafficData = clearTrafficData();
                        reply.writeNoException();
                        reply.writeInt(clearTrafficData ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean dsFlowCfg = dsFlowCfg(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(dsFlowCfg ? 1 : 0);
                        return true;
                    case TRANSACTION_getSimStateViaSysinfoEx /* 9 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getSimStateViaSysinfoEx(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = scanVsimAvailableNetworks(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case TRANSACTION_getDevSubMode /* 11 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _result6 = getDevSubMode(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result6);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        String _result7 = getPreferredNetworkTypeForVSim(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result7);
                        return true;
                    case TRANSACTION_getVSimCurCardType /* 13 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = getVSimCurCardType();
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case TRANSACTION_getVSimNetworkType /* 14 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = getVSimNetworkType();
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case TRANSACTION_getVSimSubscriberId /* 15 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _result10 = getVSimSubscriberId();
                        reply.writeNoException();
                        reply.writeString(_result10);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = getVSimOccupiedSubId();
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case TRANSACTION_isVSimInProcess /* 17 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isVSimInProcess = isVSimInProcess();
                        reply.writeNoException();
                        reply.writeInt(isVSimInProcess ? 1 : 0);
                        return true;
                    case TRANSACTION_isVSimOn /* 18 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isVSimOn = isVSimOn();
                        reply.writeNoException();
                        reply.writeInt(isVSimOn ? 1 : 0);
                        return true;
                    case TRANSACTION_switchVSimWorkMode /* 19 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean switchVSimWorkMode = switchVSimWorkMode(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(switchVSimWorkMode ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = enableVSimV2(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case TRANSACTION_dialupForVSim /* 21 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = dialupForVSim();
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case TRANSACTION_enableVSimV3 /* 22 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int _result14 = enableVSimV3(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result14);
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
        public static class Proxy implements IHwVSim {
            public static IHwVSim sDefaultImpl;
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

            @Override // com.android.internal.telephony.IHwVSim
            public boolean hasVSimIccCard() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasVSimIccCard();
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

            @Override // com.android.internal.telephony.IHwVSim
            public int getVSimSubId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVSimSubId();
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

            @Override // com.android.internal.telephony.IHwVSim
            public boolean isVSimEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isVSimEnabled();
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

            @Override // com.android.internal.telephony.IHwVSim
            public int enableVSim(int operation, String imsi, int cardType, int apnType, String acqorder, String challenge) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(operation);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(imsi);
                        try {
                            _data.writeInt(cardType);
                            try {
                                _data.writeInt(apnType);
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(acqorder);
                        try {
                            _data.writeString(challenge);
                            if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int enableVSim = Stub.getDefaultImpl().enableVSim(operation, imsi, cardType, apnType, acqorder, challenge);
                            _reply.recycle();
                            _data.recycle();
                            return enableVSim;
                        } catch (Throwable th6) {
                            th = th6;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.telephony.IHwVSim
            public boolean disableVSim() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disableVSim();
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

            @Override // com.android.internal.telephony.IHwVSim
            public String getTrafficData() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTrafficData();
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

            @Override // com.android.internal.telephony.IHwVSim
            public boolean clearTrafficData() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().clearTrafficData();
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

            @Override // com.android.internal.telephony.IHwVSim
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
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().dsFlowCfg(repFlag, threshold, totalThreshold, oper);
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

            @Override // com.android.internal.telephony.IHwVSim
            public int getSimStateViaSysinfoEx(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getSimStateViaSysinfoEx, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSimStateViaSysinfoEx(subId);
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

            @Override // com.android.internal.telephony.IHwVSim
            public int scanVsimAvailableNetworks(int subId, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().scanVsimAvailableNetworks(subId, type);
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

            @Override // com.android.internal.telephony.IHwVSim
            public String getDevSubMode(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getDevSubMode, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDevSubMode(subId);
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

            @Override // com.android.internal.telephony.IHwVSim
            public String getPreferredNetworkTypeForVSim(int subscription) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subscription);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPreferredNetworkTypeForVSim(subscription);
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

            @Override // com.android.internal.telephony.IHwVSim
            public int getVSimCurCardType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getVSimCurCardType, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVSimCurCardType();
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

            @Override // com.android.internal.telephony.IHwVSim
            public int getVSimNetworkType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getVSimNetworkType, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVSimNetworkType();
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

            @Override // com.android.internal.telephony.IHwVSim
            public String getVSimSubscriberId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getVSimSubscriberId, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVSimSubscriberId();
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

            @Override // com.android.internal.telephony.IHwVSim
            public int getVSimOccupiedSubId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVSimOccupiedSubId();
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

            @Override // com.android.internal.telephony.IHwVSim
            public boolean isVSimInProcess() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isVSimInProcess, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isVSimInProcess();
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

            @Override // com.android.internal.telephony.IHwVSim
            public boolean isVSimOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isVSimOn, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isVSimOn();
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

            @Override // com.android.internal.telephony.IHwVSim
            public boolean switchVSimWorkMode(int workMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(workMode);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_switchVSimWorkMode, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().switchVSimWorkMode(workMode);
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

            @Override // com.android.internal.telephony.IHwVSim
            public int enableVSimV2(int operation, String imsi, int cardType, int apnType, String acqorder, String tapath, int vsimloc, String challenge) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(operation);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(imsi);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(cardType);
                        try {
                            _data.writeInt(apnType);
                            _data.writeString(acqorder);
                            _data.writeString(tapath);
                            _data.writeInt(vsimloc);
                            _data.writeString(challenge);
                            if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int enableVSimV2 = Stub.getDefaultImpl().enableVSimV2(operation, imsi, cardType, apnType, acqorder, tapath, vsimloc, challenge);
                            _reply.recycle();
                            _data.recycle();
                            return enableVSimV2;
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.telephony.IHwVSim
            public int dialupForVSim() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_dialupForVSim, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().dialupForVSim();
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

            @Override // com.android.internal.telephony.IHwVSim
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
                    if (!this.mRemote.transact(Stub.TRANSACTION_enableVSimV3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableVSimV3(operation, bundle);
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

        public static boolean setDefaultImpl(IHwVSim impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwVSim getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

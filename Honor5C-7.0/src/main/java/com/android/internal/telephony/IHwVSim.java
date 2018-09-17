package com.android.internal.telephony;

import android.os.Binder;
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
                    this.mRemote.transact(Stub.TRANSACTION_hasVSimIccCard, _data, _reply, 0);
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

            public int getVSimSubId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getVSimSubId, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_isVSimEnabled, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_enableVSim, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_disableVSim, _data, _reply, 0);
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

            public boolean hasHardIccCardForVSim(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_hasHardIccCardForVSim, _data, _reply, 0);
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

            public int getSimMode(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getSimMode, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_recoverSimMode, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getRegPlmn, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getTrafficData, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_clearTrafficData, _data, _reply, 0);
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

            public boolean dsFlowCfg(int repFlag, int threshold, int totalThreshold, int oper) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(repFlag);
                    _data.writeInt(threshold);
                    _data.writeInt(totalThreshold);
                    _data.writeInt(oper);
                    this.mRemote.transact(Stub.TRANSACTION_dsFlowCfg, _data, _reply, 0);
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

            public int getSimStateViaSysinfoEx(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getSimStateViaSysinfoEx, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getCpserr, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_scanVsimAvailableNetworks, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_setUserReservedSubId, _data, _reply, 0);
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

            public int getUserReservedSubId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getUserReservedSubId, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getDevSubMode, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getPreferredNetworkTypeForVSim, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getVSimCurCardType, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getVSimNetworkType, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getVSimSubscriberId, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setVSimULOnlyMode(boolean isULOnly) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (isULOnly) {
                        i = Stub.TRANSACTION_hasVSimIccCard;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setVSimULOnlyMode, _data, _reply, 0);
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

            public boolean getVSimULOnlyMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getVSimULOnlyMode, _data, _reply, 0);
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

            public int getVSimOccupiedSubId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getVSimOccupiedSubId, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_isVSimInProcess, _data, _reply, 0);
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

            public boolean isVSimOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isVSimOn, _data, _reply, 0);
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

            public boolean switchVSimWorkMode(int workMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(workMode);
                    this.mRemote.transact(Stub.TRANSACTION_switchVSimWorkMode, _data, _reply, 0);
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

            public int getPlatformSupportVSimVer(int key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(key);
                    this.mRemote.transact(Stub.TRANSACTION_getPlatformSupportVSimVer, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_enableVSimV2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_dialupForVSim, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
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
            boolean _result;
            int _result2;
            String _result3;
            switch (code) {
                case TRANSACTION_hasVSimIccCard /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = hasVSimIccCard();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_hasVSimIccCard : 0);
                    return true;
                case TRANSACTION_getVSimSubId /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getVSimSubId();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_isVSimEnabled /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isVSimEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_hasVSimIccCard : 0);
                    return true;
                case TRANSACTION_enableVSim /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = enableVSim(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_disableVSim /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = disableVSim();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_hasVSimIccCard : 0);
                    return true;
                case TRANSACTION_hasHardIccCardForVSim /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = hasHardIccCardForVSim(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_hasVSimIccCard : 0);
                    return true;
                case TRANSACTION_getSimMode /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getSimMode(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_recoverSimMode /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    recoverSimMode();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getRegPlmn /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getRegPlmn(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case TRANSACTION_getTrafficData /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getTrafficData();
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case TRANSACTION_clearTrafficData /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = clearTrafficData();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_hasVSimIccCard : 0);
                    return true;
                case TRANSACTION_dsFlowCfg /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = dsFlowCfg(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_hasVSimIccCard : 0);
                    return true;
                case TRANSACTION_getSimStateViaSysinfoEx /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getSimStateViaSysinfoEx(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getCpserr /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getCpserr(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_scanVsimAvailableNetworks /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = scanVsimAvailableNetworks(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setUserReservedSubId /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setUserReservedSubId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_hasVSimIccCard : 0);
                    return true;
                case TRANSACTION_getUserReservedSubId /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getUserReservedSubId();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getDevSubMode /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getDevSubMode(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case TRANSACTION_getPreferredNetworkTypeForVSim /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getPreferredNetworkTypeForVSim(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case TRANSACTION_getVSimCurCardType /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getVSimCurCardType();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getVSimNetworkType /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getVSimNetworkType();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getVSimSubscriberId /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getVSimSubscriberId();
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case TRANSACTION_setVSimULOnlyMode /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setVSimULOnlyMode(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_hasVSimIccCard : 0);
                    return true;
                case TRANSACTION_getVSimULOnlyMode /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getVSimULOnlyMode();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_hasVSimIccCard : 0);
                    return true;
                case TRANSACTION_getVSimOccupiedSubId /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getVSimOccupiedSubId();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_isVSimInProcess /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isVSimInProcess();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_hasVSimIccCard : 0);
                    return true;
                case TRANSACTION_isVSimOn /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isVSimOn();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_hasVSimIccCard : 0);
                    return true;
                case TRANSACTION_switchVSimWorkMode /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = switchVSimWorkMode(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_hasVSimIccCard : 0);
                    return true;
                case TRANSACTION_getPlatformSupportVSimVer /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPlatformSupportVSimVer(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_enableVSimV2 /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = enableVSimV2(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_dialupForVSim /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = dialupForVSim();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean clearTrafficData() throws RemoteException;

    int dialupForVSim() throws RemoteException;

    boolean disableVSim() throws RemoteException;

    boolean dsFlowCfg(int i, int i2, int i3, int i4) throws RemoteException;

    int enableVSim(int i, String str, int i2, int i3, String str2, String str3) throws RemoteException;

    int enableVSimV2(int i, String str, int i2, int i3, String str2, String str3, int i4, String str4) throws RemoteException;

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

    boolean isVSimEnabled() throws RemoteException;

    boolean isVSimInProcess() throws RemoteException;

    boolean isVSimOn() throws RemoteException;

    void recoverSimMode() throws RemoteException;

    int scanVsimAvailableNetworks(int i, int i2) throws RemoteException;

    boolean setUserReservedSubId(int i) throws RemoteException;

    boolean setVSimULOnlyMode(boolean z) throws RemoteException;

    boolean switchVSimWorkMode(int i) throws RemoteException;
}
